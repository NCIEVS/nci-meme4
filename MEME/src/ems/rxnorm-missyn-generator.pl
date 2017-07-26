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

$tmptable = $dbh->tempTable($EMSNames::PREFIX);
$dbh->dropTable($tmptable);

$sql = <<"EOD";
create table $tmptable as
with pair as
(select concept_id_1, concept_id_2, rownum as cluster_id from (select distinct a.concept_id as concept_id_1, b.concept_id as
concept_id_2
from classes a, classes b, attributes c, attributes d where a.source in (select current_name from source_Version where
source='RXNORM')
  and b.source in (select current_name from source_Version where
source='MTHFDA')
  and a.concept_id != b.concept_id
  and a.atom_id = c.atom_id
  and a.atom_id = d.atom_id
  and a.tty = 'OCD' and b.tty = 'CD'
  and c.attribute_name = 'ORIG_CODE'
  and d.attribute_name = 'ORIG_SOURCE'
  and b.code = LTRIM(c.attribute_value,'0') and d.attribute_value like 'MTHFDA'
  and NOT (a.concept_id in (select concept_id from classes where source in
                     (select current_name from source_Version where
source='RXNORM')
                      and tty like 'S_D' and tobereleased in ('Y','y'))
       aND b.concept_id in (select concept_id from classes where source in
                     (select current_name from source_Version where
source='RXNORM')
                      and tty like 'S_D' and tobereleased in ('Y','y'))
  )
  and a.concept_id in (select concept_id from attributes
                       where attribute_name='ORIG_CODE'
                         and source in
                            (select current_name from source_Version where source='RXNORM')
                         and tobereleased in ('Y','y')
                       group by concept_id having count(*)<6)
)
)
select concept_id_1 concept_id, cluster_id from pair union select concept_id_2 , cluster_id from pair
EOD
$dbh->executeStmt($sql);

$tmptable_2 = $dbh->tempTable($EMSNames::PREFIX);
$dbh->dropTable($tmptable_2);
$sql = <<"EOD";
create table $tmptable_2 as select a.concept_id as concept_id_1, b.concept_id as concept_id_2 from $tmptable a, $tmptable b where a.concept_id < b.concept_id and a.cluster_id=b.cluster_id
EOD
$dbh->executeStmt($sql);

$dbh->plsql("MEME_UTILITY.set_debug_on");

$tmpTable_3 = $main::EMSCONFIG{ORACLE_USER} . ".$tmptable_2";

$returnvalue = $dbh->plsql("MEME_UTILITY.CLUSTER_PAIR_RECURSIVE('$tmpTable_3')","t");

$returnvalue = "MTH" . ".$returnvalue";


$tmpfile = "/tmp/rxnorm-missym.$$";
$dbh->selectToFile("select * from $returnvalue", $tmpfile);
$dbh->dropTable($tmptable);
$dbh->dropTable($tmptable_2);
$dbh->dropTable($returnvalue);
$dbh->disconnect;

$cmd = "/bin/gawk -F'|' '{print \$1\"|\"\$2\"|\"}' $tmpfile";
system $cmd;
unlink $tmpfile;
exit 0;
