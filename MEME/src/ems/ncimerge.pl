#!@PATH_TO_PERL@

# An atom with a TRD lexical tag is merged with another atom, different LUI
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
    $sourcesql = "and source=\'" . $vsab[0] . "\'";
  } else {
    $sourcesql = "and source in (" . join(',', map { $dbh->quote($_) } @vsab) . ")";
  }
}

$sql = <<"EOD";
SELECT concept_id
FROM classes 
WHERE source IN (SELECT current_name FROM source_version WHERE source='NCI')
  AND tty='PT'
  AND tobereleased in ('Y','y')
GROUP BY concept_id HAVING count(*)>1
EOD
$dbh->selectToFile($sql, \*STDOUT);
exit 0;
