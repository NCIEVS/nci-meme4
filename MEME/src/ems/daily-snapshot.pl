#!@PATH_TO_PERL@

# Collects a daily snapshot of the editing counts - should be run once a day via cron

# suresh@nlm.nih.gov 8/99
# suresh@nlm.nih.gov 6/2000 - modified for Oracle
# suresh@nlm.nih.gov 3/2005 - EMS-3 mods

# suresh@nlm.nih.gov 1/2006 - snapshot_date semantics changed to mean the date
# for which the data are relevant rather than when the snapshot was taken.

# -d <database>
# -t YYYYMMDD (the day for which stats are to be gathered, default is previous day)
# -f (force data to be re-collected)
# -n (catches up data collection for the previous n days)

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";

use OracleIF;
use EMSUtils;
use GeneralUtils;
use ZipUtils;
use Midsvcs;
use EMSNames;
use EMSTables;

use File::Basename;
use Getopt::Std;
getopts("d:t:fn:");

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


$now = GeneralUtils->date;
$currentmonth = GeneralUtils->date("+%m");
$currentyear = GeneralUtils->date("+%Y");

$logdir = $ENV{EMS_HOME} . "/log";
$logfile = "$logdir/daily_snapshot.$currentyear.log";
unless (-e $logfile) {
  system "/bin/touch $logfile";
  chmod(0775, $logfile) || die $@;
}

$db = $opt_d || Midsvcs->get('editing-db');
$oracleuser = $EMSCONFIG{ORACLE_USER};
$oraclepassword = GeneralUtils->getOraclePassword($oracleuser);
$dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword");
die "ERROR: Database $db is unavailable\n" unless $dbh;

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
  my($sql);
  my($conceptsApprovedTable);

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

  $SNAPSHOTTABLE = $EMSNames::DAILYSNAPSHOTTABLE;
  EMSTables->createTable($dbh, $SNAPSHOTTABLE);
  $prefix = $EMSNames::PREFIX . "_";

# quoted forms
  my($ymd) = $dbh->quote(join('-', $yyyy, $mm, $dd));
  my($fmt) = $dbh->quote('YYYY-MM-DD');

# Has data already been gathered for this day?
  $sql = "select count(snapshot_date) from $SNAPSHOTTABLE where to_char(snapshot_date, $fmt) = $ymd";

  if ($dbh->selectFirstAsScalar($sql) > 0) {
    if ($opt_f) {
      $sql = "delete from $SNAPSHOTTABLE where to_char(snapshot_date, $fmt)=$ymd";
      $dbh->executeStmt($sql);
    } else {
      &log("\nData for $theday already exists.");
      return;
    }
  }

  $conceptsApprovedTable = $dbh->tempTable($prefix . "SNAPSHOT");
  my($starttime) = time;

# This record shows that data collection was run for this day
  $sql = <<"EOD";
INSERT INTO $SNAPSHOTTABLE (snapshot_date, snapshot_type, snapshot_attr, snapshot_count)
VALUES (to_date($ymd, $fmt), 'SNAPSHOT', '', 1)
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
INSERT INTO $SNAPSHOTTABLE (snapshot_date, snapshot_type, snapshot_attr, snapshot_count)
SELECT to_date($ymd, $fmt), 'CS', status, COUNT(*) FROM concept_status
GROUP BY status
EOD
  $dbh->executeStmt($sql);

# Concepts approved on the day of interest
  $sql = <<"EOD";
CREATE TABLE $conceptsApprovedTable AS
SELECT DISTINCT m.source_id AS concept_id FROM molecular_actions m, atomic_actions a
WHERE  to_char(m.timestamp, $fmt)=$ymd
AND    (m.authority like 'E-%' OR m.authority like 'S-%')
AND    m.molecule_id=a.molecule_id
AND    m.molecular_action='MOLECULAR_CONCEPT_APPROVAL'
AND    a.table_name='CS'
AND    a.action='S' and a.new_value='R'
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
INSERT INTO $SNAPSHOTTABLE (snapshot_date, snapshot_type, snapshot_attr, snapshot_count)
SELECT to_date($ymd, $fmt), 'APPR', null, COUNT(concept_id) FROM $conceptsApprovedTable
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
INSERT INTO $SNAPSHOTTABLE (snapshot_date, snapshot_type, snapshot_attr, snapshot_count)
SELECT to_date($ymd, $fmt), 'DISTAPPR', null, COUNT(DISTINCT concept_id) FROM $conceptsApprovedTable
EOD
  $dbh->executeStmt($sql);

# STY
  $sql = <<"EOD";
INSERT INTO $SNAPSHOTTABLE (snapshot_date, snapshot_type, snapshot_attr, snapshot_count)
SELECT to_date($ymd, $fmt), 'STY', a.attribute_value, count(*) FROM attributes a, $conceptsApprovedTable r
WHERE  a.concept_id = r.concept_id
AND    a.attribute_name || '' = 'SEMANTIC_TYPE'
GROUP BY a.attribute_value
EOD
  $dbh->executeStmt($sql);

# Source
  $sql = <<"EOD";
INSERT INTO $SNAPSHOTTABLE (snapshot_date, snapshot_type, snapshot_attr, snapshot_count)
SELECT to_date($ymd, $fmt), 'SO', c.source, count(*) FROM classes c, $conceptsApprovedTable r
WHERE  c.concept_id=r.concept_id
AND    c.tobereleased in ('y', 'Y')
GROUP BY c.source
EOD
  $dbh->executeStmt($sql);

# Authority
  $sql = <<"EOD";
INSERT INTO $SNAPSHOTTABLE (snapshot_date, snapshot_type, snapshot_attr, snapshot_count)
SELECT to_date($ymd, $fmt), 'AUTH', cs.editing_authority, count(*) FROM concept_status cs, $conceptsApprovedTable r
WHERE  cs.concept_id=r.concept_id
GROUP BY cs.editing_authority
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
select snapshot_type, count(*) from $SNAPSHOTTABLE where to_char(snapshot_date, $fmt)=$ymd
group by snapshot_type
EOD

  &log("\nRow counts by snapshot_type for $theday\n");
  foreach $r ($dbh->selectAllAsRef($sql)) {
    &log(join("|", map { s/\s*$//; $_; } @$r));
  }

  &log("\nSnapshot for $theday done in " . GeneralUtils->sec2hms(time-$starttime) . " secs.");

# clean up
  $dbh->dropTable($conceptsApprovedTable);
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
