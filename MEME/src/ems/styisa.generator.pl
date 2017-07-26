#!@PATH_TO_PERL@

# Concepts with more than 1 STY where one is an ancestor of the other
# suresh@nlm.nih.gov

# Options:
# -d database

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use OracleIF;
use EMSUtils;
use GeneralUtils;
use ZipUtils;
use Midsvcs;
use EMSNames;
use EMSTables;

use File::Basename;
use Getopt::Std;

getopts("d:t:s:");

$starttime = time;

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get($opt_s || 'editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");

$srstre = "SRSTRE2";
die "ERROR: $srstre table does not exist!\n" unless $dbh->tableExists($srstre);

$tmptable = $dbh->tempTable($EMSNames::PREFIX);
$dbh->dropTable($tmptable);

$sql = <<"EOD";
CREATE TABLE $tmptable AS
WITH sty AS (SELECT attribute_value, concept_id FROM attributes WHERE attribute_name='SEMANTIC_TYPE')
SELECT DISTINCT a1.concept_id
FROM sty a1, sty a2, srstre2 b
WHERE  a1.concept_id=a2.concept_id
  AND    a1.attribute_value != a2.attribute_value
  AND    a1.attribute_value = sty1
  AND    a2.attribute_value = sty2
  AND    b.rel='isa'
EOD
$dbh->executeStmt($sql);

$sql = <<"EOD";
SELECT a.concept_id, rownum as cluster_id FROM $tmptable a
EOD


$dbh->selectToFile($sql, \*STDOUT);
$dbh->dropTable($tmptable);
$dbh->disconnect;
exit 0;
