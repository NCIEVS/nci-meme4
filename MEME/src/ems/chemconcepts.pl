#!@PATH_TO_PERL@

# identifies concepts that have chemical STYs (for editing)

# Run via cron or daily after backup

# Author: suresh@nlm.nih.gov 8/2002
# suresh@nlm.nih.gov 3/2005 (EMS 3)

# Command line arguments:

# -d database

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";

use OracleIF;
use EMSUtils;
use GeneralUtils;
use Midsvcs;
use EMSMaxtab;
use EMSNames;

use Getopt::Std;
getopts("d:");

&log_and_die("ERROR: EMS_HOME environment variable not set\n") unless $ENV{EMS_HOME};
EMSUtils->loadConfig;

#$ENV{MIDSVCS_HOME} = $EMSCONFIG{MIDSVCS_HOME};
#$ENV{DBPASSWORD_HOME} = $EMSCONFIG{DBPASSWORD_HOME};

chomp($currentyear = `/bin/date "+%Y"`);
$logfile = join("/", $ENV{EMS_HOME}, "data", "log",
		join(".", "chemconcepts", $currentyear, "log"));
unless (-e $logfile) {
  system "/bin/touch $logfile";
  chmod(0775, $logfile) || die $@;
}

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
&log_and_die("Database: $db is unavailable") unless $dbh;

$starttime = time;
$startdate = GeneralUtils->date;

&log("\n" . "-" x 20 . $startdate . "-" x 20 . "\n");
&log("EMS_HOME: " . $ENV{EMS_HOME});
&log("Database: " . $dbh->getDB());

# Are batch EMS scripts allowed?
$batchcutoff = EMSMaxtab->get($dbh, $EMSNames::EMSBATCHCUTOFFKEY);
if ($batchcutoff && lc($batchcutoff->{valuechar}) ne "no") {
  &log("Batch processes are currently disallowed in the EMS.");
  exit 0;
}

$chemtable = $EMSNames::CHEMCONCEPTSTABLE;
$dbh->dropTable($chemtable);

$sql = <<"EOD";
CREATE TABLE $chemtable AS
SELECT DISTINCT concept_id FROM attributes
WHERE  attribute_name || '' = 'SEMANTIC_TYPE'
AND    attribute_value IN (SELECT semantic_type FROM semantic_types where editing_chem='Y')
EOD
$dbh->executeStmt($sql);
$dbh->createIndex($chemtable, 'concept_id');

$sql = "select count(*) from $chemtable";
$count = $dbh->selectFirstAsScalar($sql) || 0;

$hms = GeneralUtils->sec2hms(time-$starttime);
$enddate = GeneralUtils->date;

&log(<<"EOD");
Start date: $startdate
End date: $enddate
Time taken: $hms
Chemical concepts: $count
EOD

$dbh->disconnect;
exit 0;

sub log {
  my($msg) = @_;
  open(L, ">>$logfile") || return;
  print L $msg, "\n";
  close(L);
}

sub log_and_die {
  my($msg) = @_;
  &log($msg);
  die $msg;
}
