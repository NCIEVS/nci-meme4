#!@PATH_TO_PERL@

# Loads all previous history for AH bins into AHHISTORY table

# suresh@nlm.nih.gov 5/2005

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";

use Getopt::Std;
use Archive::Zip;

use OracleIF;
use EMSUtils;
use WMSUtils;
use GeneralUtils;
use ZipUtils;
use Midsvcs;

# Options:
# -d <database>
# -p <path to where all worklist zip files are>

getopts("d:p:");

EMSUtils->loadConfig;

# known AH bins and their canonical names (if any)
@ahbins = (
"missyn",
['needrel', 'needsrel'],
'needsrel',
['needsrelgo', 'needsrel'],
'sepstr',
'sfo_lfo',
'rescue_orphan',
'scthl7mergestr',
'multcuis',
['true_orphan', 'trueorph'],
'trueorph',
);

foreach $r (@ahbins) {
  if (scalar($r)) {
    $ahbin{$r}++;
  } else {
    $ahbin{$r->[0]}++;
    $canonical_name{$r->[0]} = $r->[1];
  }
}

$db = $opt_d || Midsvcs->get($opt_s || 'editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Database: $db is unavailable" unless $dbh;
$ENV{ORACLE_HOME} = $main::EMSCONFIG{ORACLE_HOME} || $ENV{ORACLE_HOME};

$AHHISTORY = $EMSNames::AHHISTORYTABLE;
EMSTables->createTable($dbh, $AHHISTORY);
$dbh->executeStmt("truncate table $AHHISTORY");

die "Need -p path to where worklist zip files are" unless $opt_p;
opendir(D, $opt_p) || die "Cannot open $opt_p";
@zips = grep { /^worklists\..{3}\.zip$/ } readdir(D);
closedir(D);
die "Cannot find worklist zip files\n" unless @zips;
foreach (@zips) {
  &doit($dbh, join("/", $opt_p, $_));
}

$n=0;
foreach $col ("min_concept_id", "worklist_name", "bin_name", "canonical_name") {
  next if $dbh->colHasIndex($AHHISTORY, $col);
  $n++;
  $dbh->createIndex($AHHISTORY, $col, sprintf("x%1d_%s", $n, $AHHISTORY)) 
}
$dbh->disconnect;
exit 0;

sub doit {
  my($dbh, $zippath) = @_;
  my($archive) = Archive::Zip->new($zippath);
  my(@worklists) = $archive->membersMatching('.*\.input');
  my($worklist);
  my($tmpfile) = "/tmp/worklist.$$";
  my($tmptable) = EMSUtils->tempTable($dbh);
  my($histtable) = EMSUtils->tempTable($dbh);
  my($colspec) = [{concept_id=>'integer'}, {cluster_id=>'integer'}];
  my($w, $b, $bin_name, $worklist_name, $editing_epoch);
  my($sql);

  foreach $worklist (@worklists) {
    $worklist_name = $worklist->{fileName};
    $worklist_name =~ s/\.input$//;
    $qw = $dbh->quote($worklist_name);

    $b = $worklist_name;
    $b =~ s/_\d+$//;
    $b =~ s/_ch$//;
    $b =~ s/_nc$//;
    $b =~ /^wrk(...)_(.*)$/;

    $bin_name  = $2;
    $qb = $dbh->quote($bin_name);
    $canonical_name = $canonical_name{$bin_name} || $bin_name;
    $qc = $dbh->quote($canonical_name);

    next unless $ahbin{lc($bin_name)};

    $sql = "select count(*) as c from $AHHISTORY where worklist_name=$qw";
    next if $dbh->selectFirstAsScalar($sql) > 0;

    print "Loading: ", $worklist_name, "\n";

    $archive->extractMemberWithoutPaths($worklist, $tmpfile);
    $dbh->dropTables([$tmptable, $histtable]);
    $dbh->createTable($tmptable, $colspec);
#    $dbh->file2table($tmpfile, $tmptable, $colspec);
    $dbh->sqlldr($tmpfile, $tmptable, $colspec);

    EMSUtils->list2history($dbh, $tmptable, $histtable);

    $sql = "delete from $AHHISTORY where worklist_name=$qw";
    $dbh->executeStmt($sql);

    $sql = <<"EOD";
insert into $AHHISTORY
  select
    $qb as bin_name,
    $qc as canonical_name,
    $qw as worklist_name,
    cluster_id,
    min_concept_id,
    md5 from $histtable
EOD
    $dbh->executeStmt($sql);
    $dbh->dropTables([$tmptable, $histtable]);
    unlink $tmpfile;
  }
}
