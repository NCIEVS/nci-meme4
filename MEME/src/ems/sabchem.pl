#!/site/bin/perl5

# Merge source
# Tammy 10/2003, BAC 10/2003
# suresh@nlm.nih.gov

# Options:

# -d <db>
# -s (restrict to these sources)

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

if ($opt_s) {
  @vsab = MIDUtils->makeVersionedSAB($dbh, [ split /,/, $opt_s ]);
  if (@vsab == 1) {
    $sourcesql = "and a.source=\'" . $vsab[0] . "\'";
  } else {
    $sourcesql = "and a.source in (" . join(',', map { $dbh->quote($_) } @vsab) . ")";
  }
}

$sql = <<"EOD";
SELECT DISTINCT a.concept_id 
FROM classes a
WHERE a.tobereleased in ('Y','y')
$sourcesql
AND concept_id IN (SELECT concept_id FROM attributes a, semantic_types b 
                   WHERE attribute_name='SEMANTIC_TYPE' and attribute_value=semantic_type
                     AND is_chem='Y')
EOD
$dbh->selectToFile($sql, \*STDOUT);
exit 0;
