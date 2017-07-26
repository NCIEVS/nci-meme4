#!@PATH_TO_PERL@

# Loads all previous history from worklists into EMS3_FULLHISTORY table
# for gathering statistical data.

# suresh@nlm.nih.gov 2/2006

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";

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

$db = $opt_d || Midsvcs->get($opt_s || 'editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Database: $db is unavailable" unless $dbh;
#$ENV{ORACLE_HOME} = $main::EMSCONFIG{ORACLE_HOME} || $ENV{ORACLE_HOME};

$spec = [
	 concept_id,
	 cluster_id,
	 {worklist_name=>'varchar(40)'},
	 {editing_epoch=>'varchar(5)'},
	 {editing_year=>'integer'},
	];

$HISTORY = "EMS3_FULLHISTORY";
$dbh->dropTable($HISTORY);
$dbh->createTable($HISTORY, $spec);

die "Need -p path to where worklist zip files are" unless $opt_p;
opendir(D, $opt_p) || die "Cannot open $opt_p";
@zips = grep { /^worklists\..{3}\.zip$/ } readdir(D);
closedir(D);
die "Cannot find worklist zip files\n" unless @zips;
foreach (@zips) {
  &doit($dbh, join("/", $opt_p, $_));
}
$dbh->disconnect;
exit 0;

sub doit {
  my($dbh, $zippath) = @_;
  my($archive) = Archive::Zip->new($zippath);
  my(@worklists) = $archive->membersMatching('.*\.input');
  my($worklist);
  my($tmpfile) = "/tmp/worklist.$$";
  my($tmpfile2) = "/tmp/worklist2.$$";
  my($tmptable);
  my($w, $b, $bin_name, $worklist_name, $editing_epoch);
  my($colspec) = ['concept_id', 'cluster_id'];
  my($sql);

  foreach $worklist (@worklists) {
    $worklist_name = $worklist->{fileName};
    $worklist_name =~ s/\.input$//;
    $qw = $dbh->quote($worklist_name);

    $b = $worklist_name;
    $b =~ /^wrk(...)_(.*)$/;

    $editing_epoch = $1;
    $qe = $dbh->quote($editing_epoch);

# epoch of 05a is during the editing year 2004
    $epoch = /^(\d\d)/;
    $editing_year = ($1 > 50 ? (1900 + $1 - 1) : (2000 + $1 - 1));

    print "Loading: ", $worklist_name, " (editing year: $editing_year)\n";

    $archive->extractMemberWithoutPaths($worklist, $tmpfile);
    system "awk -F'|' '{print \$1}' $tmpfile|sort -u > $tmpfile2";

    $tmptable = "tmp_suresh_foo";
    $dbh->dropTable($tmptable);
    $dbh->createTable($tmptable, $colspec);
    $dbh->sqlldr($tmpfile2, $tmptable, $colspec);

    $sql = "delete from $HISTORY where worklist_name=$qw";
    $dbh->executeStmt($sql);

    $sql = <<"EOD";
insert into $HISTORY
select concept_id,
cluster_id,
$qw as worklist_name,
$qe as editing_epoch,
$editing_year as editing_year
from $tmptable
EOD
    $dbh->executeStmt($sql);
    $dbh->dropTable($tmptable);
    unlink $tmpfile;
    unlink $tmpfile2;
  }
}
