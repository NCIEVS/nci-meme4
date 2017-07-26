#!@PATH_TO_PERL@

# Gathers higher level stats from actions tables
# should be run once a day via cron to collect data
# for the previous day

# suresh@nlm.nih.gov 8/99
# suresh@nlm.nih.gov 6/2000 - modified for Oracle
# suresh@nlm.nih.gov 3/2005 - EMS-3 mods

# -d <database>
# -t <date in YYYYMMDD>
# -f (force data to be collected even if present)
# -n (catches up data collection for the previous n days)

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";

use OracleIF;
use EMSUtils;
use GeneralUtils;
use Midsvcs;
use EMSNames;
use EMSTables;

use File::Basename;
use Getopt::Std;

getopts("d:t:fn:");

$starttime = time;

@months = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);

# day is yesterday unless specified
unless ($opt_t) {
  my(@y) = localtime time-24*60*60;
  $yyyymmdd = sprintf("%.4d%.2d%.2d", $y[5]+1900, $y[4]+1, $y[3]);
} else {
  $yyyymmdd = $opt_t;
}

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

#$ENV{MIDSVCS_HOME} = $EMSCONFIG{MIDSVCS_HOME};
#$ENV{LVGIF_HOME} = $EMSCONFIG{LVGIF_HOME};
#$ENV{DBPASSWORD_HOME} = $EMSCONFIG{DBPASSWORD_HOME};


$db = $opt_d || Midsvcs->get($opt_s || 'editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "ERROR: Database $db is unavailable\n" unless $dbh;

#$ENV{MIDSVCS_HOME} = $EMSCONFIG{MIDSVCS_HOME};
#$ENV{LVGIF_HOME} = $EMSCONFIG{LVGIF_HOME};
#$ENV{DBPASSWORD_HOME} = $EMSCONFIG{DBPASSWORD_HOME};


$now = GeneralUtils->date;
$currentdate = GeneralUtils->date("+%d");
$currentmonth = GeneralUtils->date("+%m");
$currentyear = GeneralUtils->date("+%Y");

$logdir = $ENV{EMS_HOME} . "/log";
$logfile = "$logdir/daily_action_counts.$currentyear.log";
unless (-e $logfile) {
  system "/bin/touch $logfile";
  chmod(0775, $logfile) || die $@;
}

# restrict the environment
$ENV{PATH} = "/bin:$ENV{ORACLE_HOME}/bin";

if ($opt_n) {
  &log_and_die("ERROR: $opt_n has to be a numeric") unless $opt_n =~ /^\d+$/;

  for ($i=$opt_n; $i>=1; $i--) {
    my(@y) = localtime time-(24*60*60*$i);
    $yyyymmdd = sprintf("%.4d%.2d%.2d", $y[5]+1900, $y[4]+1, $y[3]);
    &doit($yyyymmdd);
  }
} else {
  &doit($yyyymmdd);
}

$dbh->disconnect;
exit 0;

sub doit {
  my($yyyymmdd) = @_;
  my($yyyy, $mm, $dd);
  my($theday);
  my($prefix);
  my($sql, @sql);

  &log_and_die("ERROR: date expected in format YYYYMMDD") unless $yyyymmdd =~ /^(\d{4})(\d{2})(\d{2})$/;
  $yyyy = $1;
  $mm = $2;
  $dd = $3;
  &log_and_die("ERROR: Year should be a valid year!") unless $yyyy > 1980 && $yyyy < 2500;
  &log_and_die("ERROR: Month should be a valid month!") unless $mm >= 1 && $mm <= 12;
  &log_and_die("ERROR: Date should be a valid date!") unless $dd >= 1 && $dd <= 31;

  $theday = $months[$mm-1] . " $dd, $yyyy";

  &log("\n" . "-" x 20 . " Data for: " . $theday . " " . "-" x 20 . "\n");
  &log("Current time: " . $now);
  &log("EMS_HOME: " . $ENV{EMS_HOME});
  &log("Database: " . $dbh->getDB());
  &log_and_die("Database: $db is unavailable") unless $dbh;
  &log("Gathering stats for: $theday");

# Are batch EMS scripts allowed?
  my($batchcutoff) = EMSMaxtab->get($dbh, $EMSNames::EMSBATCHCUTOFFKEY);
  if ($batchcutoff && lc($batchcutoff->{valuechar}) ne "no") {
    &log("Batch processes are currently disallowed in the EMS.");
    return;
  }

# quoted forms
  my($ymd) = $dbh->quote(join('-', $yyyy, $mm, $dd));
  my($fmt) = $dbh->quote('YYYY-MM-DD');

  $ACTIONCOUNTTABLE = $EMSNames::DAILYACTIONCOUNTTABLE;
  EMSTables->createTable($dbh, $ACTIONCOUNTTABLE);
  $prefix = $EMSNames::PREFIX . "_";

# Has data already been gathered for this day?
  $sql = "select count(report_date) from $ACTIONCOUNTTABLE where to_char(report_date, $fmt) = $ymd";

  if ($dbh->selectFirstAsScalar($sql) > 0) {
    if ($opt_f) {
      $sql = "delete from $ACTIONCOUNTTABLE where to_char(report_date, $fmt)=$ymd";
      $dbh->executeStmt($sql);
    } else {
      &log("\nData for $theday already exists.");
      return;
    }
  }

  $counttable = $dbh->tempTable($prefix . "DAILYACTION");

# get the counts for the day
  my(@rows);
  my($timestampSQL);
  my($tmptable) = $dbh->tempTable($prefix . "TMPTABLE");
  my($ma_table) = $dbh->tempTable($prefix . "MOLECULAR");
  my($aa_table) = $dbh->tempTable($prefix . "ATOMIC");

  $dbh->dropTables([$ma_table, $aa_table]);
  $timestampSQL = "to_char(timestamp, $fmt)=$ymd";

  my($actions) = $dbh->tempTable($prefix . "actions");
  my($touched) = $dbh->tempTable($prefix . "touched");
  my($approved) = $dbh->tempTable($prefix . "approved");
  my($rels) = $dbh->tempTable($prefix . "rels");
  my($stys) = $dbh->tempTable($prefix . "stys");
  my($splits) = $dbh->tempTable($prefix . "split");
  my($merges) = $dbh->tempTable($prefix . "merge");

  $dbh->dropTables([$actions, $touched, $approved, $rels, $stys, $splits, $merges, $tmptable]);

  @sql =
    (
     "create table $ma_table as select * from molecular_actions where $timestampSQL",
     "create table $aa_table as select * from atomic_actions where $timestampSQL",
    );

  foreach $sql (@sql) {
    $dbh->executeStmt($sql);
    my($e) = $@ || $DBI::errstr;
    if ($e) {
      &log("ERROR: $e");
      return;
    }
  }
  $dbh->createIndex($ma_table, 'molecule_id', "x_" . $dbh->tempTable($prefix . "maindex"));
  $dbh->createIndex($aa_table, 'row_id', "x_" . $dbh->tempTable($prefix . "aaindex"));

  @sql =
    (
     "create table $actions as
      select authority, count(*) as num from $ma_table
      group by authority",

     "create table $tmptable as
      select authority, source_id as concept_id from $ma_table
      union
      select authority, target_id from $ma_table",

     "create table $touched as
      select authority, count(distinct concept_id) as num from $tmptable
      group by authority",

     "drop table $tmptable",

     "create table $tmptable as
      select distinct m.authority, m.source_id as concept_id from $ma_table m, $aa_table a
      where  m.molecule_id=a.molecule_id
      and    m.molecular_action='MOLECULAR_CONCEPT_APPROVAL'
      and    a.table_name='CS'
      and    a.action='S' and a.new_value='R'",

     "create table $approved as
      select authority, count(concept_id) as num from $tmptable
      group by authority",

     "drop table $tmptable",

     "create table $rels as
      select m.authority, count(*) as num from $ma_table m, $aa_table a
      where  m.molecule_id=a.molecule_id
      and    m.molecular_action = 'MOLECULAR_INSERT'
      and    a.action='I'
      and    a.table_name='R'
      group by m.authority",

     "create table $stys as
      select m.authority, count(*) as num from $ma_table m, $aa_table a, attributes t
      where  m.molecule_id=a.molecule_id
      and    a.row_id = t.attribute_id
      and    m.molecular_action LIKE '%INSERT%'
      and    a.action='I'
      and    t.attribute_name || '' ='SEMANTIC_TYPE'
      group by m.authority",

     "create table $splits as
      select authority, count(*) as num from $ma_table
      where  molecular_action = 'MOLECULAR_SPLIT'
      group by authority",

     "create table $merges as
      select authority, count(*) as num from $ma_table
      where  molecular_action = 'MOLECULAR_MERGE'
      group by authority",

     "create table $counttable (
       authority VARCHAR(200),
       total_actions INTEGER,
       concepts_touched INTEGER,
       concepts_approved INTEGER,
       rels_inserted INTEGER,
       stys_inserted INTEGER,
       splits INTEGER,
       merges INTEGER
      )",

     "insert into $counttable
      select distinct authority, 0, 0, 0, 0, 0, 0, 0 from $ma_table",

     "update $counttable a
      set    total_actions = (select b.num from $actions b where a.authority = b.authority)",

     "update $counttable a
      set    concepts_touched = (select b.num from $touched b where a.authority = b.authority)",

     "update $counttable a
      set    concepts_approved = (select b.num from $approved b where a.authority = b.authority)",

     "update $counttable a
      set    rels_inserted = (select b.num from $rels b where a.authority = b.authority)",

     "update $counttable a
      set    stys_inserted = (select b.num from $stys b where a.authority = b.authority)",

     "update $counttable a
      set    splits = (select b.num from $splits b where  a.authority = b.authority)",

     "update $counttable a
      set    merges = (select b.num from $merges b where  a.authority = b.authority)",
  );

  foreach $sql (@sql) {
    $dbh->executeStmt($sql);
    my($e) = $@ || $DBI::errstr;
    if ($e) {
      &log("ERROR: $e");
      return;
    }
  }
  $dbh->dropTables([$actions, $touched, $approved, $rels, $stys, $splits, $merges]);
  $dbh->dropTables([$tmptable, $ma_table, $aa_table]);

# update $ACTIONCOUNTTABLE
  $sql = "select count(*) from $counttable";
  $rows = $dbh->selectFirstAsScalar($sql);

  if ($rows == 0) {

    $sql = <<"EOD";
insert into $ACTIONCOUNTTABLE (report_date, total_actions, concepts_touched, concepts_approved, rels_inserted, stys_inserted, splits, merges, authority)
values (to_date($ymd, $fmt), 0, 0, 0, 0, 0, 0, 0, null)
EOD
    $dbh->executeStmt($sql);
    &log("\nNo actions recorded for $theday");

  } else {

    $sql = <<"EOD";
insert into $ACTIONCOUNTTABLE (report_date, total_actions, concepts_touched, concepts_approved, rels_inserted, stys_inserted, splits, merges, authority)
select to_date($ymd, $fmt), total_actions, concepts_touched, concepts_approved, rels_inserted, stys_inserted, splits, merges, authority from $counttable
EOD
    $dbh->executeStmt($sql);

    &log("\nData collected in $ACTIONCOUNTTABLE for $theday\n");
    $sql = "select $ymd, total_actions, concepts_touched, concepts_approved, rels_inserted, stys_inserted, splits, merges, authority from $counttable";
    foreach $r ($dbh->selectAllAsRef($sql)) {
      &log(join("|", map { s/\s*$//; $_; } @$r));
    }
  }

  $dbh->dropTable($counttable);
  &log("\nAction counts for $theday done in " . GeneralUtils->sec2hms(time-$starttime));

  return;
}

# gets the action counts for the day in $counttable
sub get_counts {
  return;
}

sub log {
  my($msg) = @_;
  open(L, ">>$logfile") || return;
  print L $msg, "\n";
  close(L);
}

sub log_and_die {
  my($msg) = @_;
  &log($msg);
  die;
}
