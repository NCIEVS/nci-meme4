#!@PATH_TO_PERL@

# Merge source
# Tammy 10/2003, BAC 10/2003
# suresh@nlm.nih.gov

# Options:

# -d <db>
# -s (restrict to these sources)
# -i (id field name)

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";

use OracleIF;
use EMSUtils;
use MIDUtils;
use Midsvcs;

use Getopt::Std;

getopts("d:s:i:");

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
    $sourcesql .= " and b.source=\'" . $vsab[0] . "\'";
  } else {
    $sourcesql = "and a.source in (" . join(',', map { $dbh->quote($_) } @vsab) . ")";
    $sourcesql .= " and b.source in (" . join(',', map { $dbh->quote($_) } @vsab) . ")";
  }
}

$sql = <<"EOD";
SELECT  DISTINCT a.concept_id 
FROM classes a, classes b
WHERE a.tobereleased in ('Y','y')
AND   b.tobereleased in ('Y','y')
$sourcesql
AND a.$opt_i != b.$opt_i
AND a.concept_id = b.concept_id
EOD
$dbh->selectToFile($sql, \*STDOUT);
exit 0;
