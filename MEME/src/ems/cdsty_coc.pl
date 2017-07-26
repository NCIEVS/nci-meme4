#!@PATH_TO_PERL@

# Clinical Drug concept with Non Clinical Drug STY

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

getopts("d:s:");

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");

$logdir = $ENV{EMS_HOME} . "/log";


$sql = <<"EOD";
select distinct concept_id from attributes where attribute_name='SEMANTIC_TYPE'
and attribute_value='Clinical Drug' and concept_id in 
(select /*+ full(a) */ concept_id from attributes a 
where attribute_name='SEMANTIC_TYPE' and attribute_value!='Clinical Drug')
EOD
$dbh->selectToFile($sql, \*STDOUT);
exit 0;
