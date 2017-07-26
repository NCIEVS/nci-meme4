#!@PATH_TO_PERL@

# ICD9-CM terms with 2-digit codes should be procedures
# wth@nlm.nih.gov 5/99
# suresh@nlm.nih.gov 5/99
# Oracle port - suresh 8/00
# suresh@nlm.nih.gov - EMS3 mods

# Options:
# -d database

nshift @INC, "$ENV{ENV_HOME}/bin";

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

getopts("d:");

$starttime = time;

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");

$srstre = "SRSTRE2";
die "ERROR: $srstre table does not exist!\n" unless $dbh->tableExists($srstre);

# Procedure STYs are descendents of "Health Care Activity"
$hca = $dbh->quote("Health Care Activity");
$sql = <<"EOD";
select sty1 from $srstre where  sty2=$hca and rel='isa'
union
select $hca from dual
EOD
@procedures = $dbh->selectAllAsArray($sql);

$stylist = join(', ', map { $dbh->quote($_) } @procedures);

$tmptable = $dbh->tempTable($EMSNames::PREFIX);
$dbh->dropTable($tmptable);

# Get ICD atoms
$sql = <<"EOD";
CREATE TABLE $tmptable AS
SELECT DISTINCT c.concept_id FROM classes c, source_version s
WHERE  s.source='ICD'
AND    s.current_name = c.source
AND    INSTR(c.code, '.', 1,1)=3
EOD
$dbh->executeStmt($sql);

$sql = <<"EOD";
SELECT DISTINCT t.concept_id, rownum as cluster_id from $tmptable t, attributes a
WHERE  t.concept_id=a.concept_id
AND    a.attribute_name='SEMANTIC_TYPE'
AND    a.attribute_value NOT IN ($stylist)
EOD

$dbh->selectToFile($sql, \*STDOUT);
$dbh->dropTable($tmptable);
$dbh->disconnect;
exit 0;
