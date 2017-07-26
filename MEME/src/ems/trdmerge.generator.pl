#!@PATH_TO_PERL@

# An atom with a TRD lexical tag is merged with another atom, different LUI
# in the same concept
# Tammy (powell@nlm) 9/2000
# suresh@nlm.nih.gov 9/2000
# suresh@nlm5/2005 EMS-3

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
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Database $db is not available" unless $dbh;

$trdtable = $dbh->tempTable($EMSNames::PREFIX . "_trd");
$dbh->dropTable($trdtable);

$sql = <<"EOD";
create table $trdtable as
SELECT c.concept_id, c.lui, c.atom_id FROM classes c, attributes a
WHERE  c.atom_id = a.atom_id
AND    c.tobereleased in ('y', 'Y')
AND    a.attribute_name='LEXICAL_TAG'
AND    a.attribute_value='TRD'
EOD
$dbh->executeStmt($sql);
$dbh->createIndex($trdtable, 'atom_id');

$sql = <<"EOD";
select concept_id, rownum as cluster_id from
(
  select distinct c.concept_id FROM classes c, $trdtable t
  WHERE  c.concept_id = t.concept_id
  AND    c.atom_id != t.atom_id
  AND    c.lui != t.lui
  AND    c.tobereleased in ('y', 'Y')
  AND    c.atom_id NOT IN (SELECT atom_id FROM $trdtable)
)
EOD
$dbh->selectToFile($sql, \*STDOUT);

$dbh->dropTable($trdtable);
$dbh->disconnect;
exit 0;
