#!/site/bin/perl5

# UMLS missyn
# Options:

# -d <db>

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";

use OracleIF;
use EMSUtils;
use MIDUtils;
use Midsvcs;

use Getopt::Std;

getopts("d:");

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");

$logdir = $ENV{EMS_HOME} . "/log";

$sql = <<"EOD";
SELECT DECODE(flag,1, concept_id_1, concept_id_2), cluster_id FROM
(SELECT concept_id_1, concept_id_2, rownum as cluster_id FROM
 (SELECT  DISTINCT a.concept_id as concept_id_1, b.concept_id as concept_id_2
 FROM classes a, classes b
 WHERE  a.status = 'N'
 AND   b.status = 'N'
 AND   a.tobereleased in ('Y','y')
 AND   b.tobereleased in ('Y','y')
 AND   a.isui = b.isui
 AND a.last_assigned_cui LIKE 'CL%'
 AND b.last_assigned_cui NOT LIKE 'CL%'
 AND a.concept_id < b.concept_id
 MINUS
 SELECT concept_id_1, concept_id_2 FROM relationships
 WHERE status = 'N'
 AND tobereleased in ('Y','y')
 AND concept_id_1 < concept_id_2
 )
) a,
(SELECT 1 as flag FROM dual
 UNION
 SELECT 2 as flag FROM dual) b
EOD
$dbh->selectToFile($sql, \*STDOUT);
exit 0;
