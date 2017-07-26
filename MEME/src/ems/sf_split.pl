#!@PATH_TO_PERL@

# Split source family
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
    $sourcesql = "and source=\'" . $vsab[0] . "\'";
  } else {
    $sourcesql = "and source in (" . join(',', map { $dbh->quote($_) } @vsab) . ")";
  }
}

$sql = <<"EOD";
SELECT DECODE(flag,1, concept_id_1, concept_id_2), cluster_id FROM
(SELECT concept_id_1, concept_id_2, rownum as cluster_id FROM
 (SELECT  DISTINCT a.concept_id as concept_id_1, b.concept_id as concept_id_2 
 FROM classes a, classes b
 WHERE a.tobereleased in ('Y','y')
 AND   b.tobereleased in ('Y','y')
 AND a.source IN 
  (SELECT r.source FROM source_rank r, source_version v 
   WHERE source_family = '$opt_s' AND r.source = v.current_name)
 AND b.source IN
  (SELECT r.source FROM source_rank r, source_version v 
   WHERE source_family = '$opt_s' AND r.source = v.current_name)
 AND a.$opt_i = b.$opt_i
 AND a.concept_id < b.concept_id
 )
) a,
(SELECT 1 as flag FROM dual
 UNION
 SELECT 2 as flag FROM dual) b
EOD
$dbh->selectToFile($sql, \*STDOUT);
exit 0;
