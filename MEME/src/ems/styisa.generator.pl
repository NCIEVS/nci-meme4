#!@PATH_TO_PERL@

# Concepts with more than 1 STY where one is an ancestor of the other
# suresh@nlm.nih.gov

# Options:
# -d database

unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
use lib "$ENV{EMS_HOME}/lib";

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
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");

$srstre = "SRSTRE2";
die "ERROR: $srstre table does not exist!\n" unless $dbh->tableExists($srstre);

$tmptable = $dbh->tempTable($EMSNames::PREFIX);
$dbh->dropTable($tmptable);

$sql = <<"EOD";
CREATE TABLE $tmptable AS
SELECT DISTINCT a1.concept_id, a1.attribute_value AS ancestor_sty, a2.attribute_value AS descendant_sty
       FROM attributes a1, attributes a2
WHERE  a1.concept_id=a2.concept_id
AND    a1.attribute_name || '' = 'SEMANTIC_TYPE'
AND    a2.attribute_name || '' = 'SEMANTIC_TYPE'
AND    a1.attribute_value != a2.attribute_value
EOD
$dbh->executeStmt($sql);

$sql = <<"EOD";
SELECT DISTINCT a.concept_id, rownum as cluster_id FROM $tmptable a, $srstre b
WHERE  a.descendant_sty = b.sty1
AND    a.ancestor_sty = b.sty2
AND    b.rel='isa'
EOD

$dbh->selectToFile($sql, \*STDOUT);
$dbh->dropTable($tmptable);
$dbh->disconnect;
exit 0;
