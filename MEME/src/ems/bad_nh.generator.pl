#!@PATH_TO_PERL@

# Concepts with NH (non-human) attibute, but inconsistent STY
# erlbaum@lexical.com (original)
# carlsen@lexical.com (MEME-II port)
# suresh@nlm.nih.gov 10/98 (installed)
# Oracle port - suresh 8/00
# suresh@nlm.nih.gov - EMS3 5/2005

# Options:
# -d database

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

require "utils.pl";

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

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");

# These STYs can have NH attribute
@nhSTYs = (
	 'Anatomical Structure',
	 'Embryonic Structure',
	 'Anatomical Abnormality',
	 'Congenital Abnormality',
	 'Acquired Abnormality',
	 'Fully Formed Anatomical Structure',
	 'Body Part, Organ, or Organ Component',
	 'Tissue',
	 'Cell',
	 'Cell Component',
	 'Gene or Genome',
	 'Body Substance',
	 'Body Location or Region',
	 'Behavior',
	 'Social Behavior',
	 'Individual Behavior',
	 'Natural Phenomenon or Process',
	 'Biologic Function',
	 'Physiologic Function',
	 'Organism Function',
	 'Mental Process',
	 'Organ or Tissue Function',
	 'Cell Function',
	 'Molecular Function',
	 'Genetic Function',
	 'Pathologic Function',
	 'Disease or Syndrome',
	 'Mental or Behavioral Dysfunction',
	 'Neoplastic Process',
	 'Cell or Molecular Dysfunction',
	 'Experimental Model of Disease'
	 );

$stylist = join(', ', map { $dbh->quote($_) } @nhSTYs);

# table with NH and correct STY
$nhtable = $dbh->tempTable($EMSNames::PREFIX . "_nh");
$stytable = $dbh->tempTable($EMSNames::PREFIX . "_sty");

$dbh->dropTables([$nhtable, $stytable]);

# Concepts with NH flag
$sql = <<"EOD";
CREATE TABLE $nhtable AS
SELECT DISTINCT concept_id FROM attributes
WHERE  attribute_name = 'NON_HUMAN'
EOD
$dbh->executeStmt($sql);
$dbh->createIndex($nhtable, 'concept_id');

# Concepts with NH STYs
$sql = <<"EOD";
CREATE TABLE $stytable AS
SELECT DISTINCT concept_id FROM attributes
WHERE  attribute_name = 'SEMANTIC_TYPE'
AND    attribute_value IN ($stylist)
EOD
$dbh->executeStmt($sql);
$dbh->createIndex($stytable, 'concept_id');

# Select concepts with NH flag but not in STY table
$sql = <<"EOD";
SELECT DISTINCT n.concept_id, rownum as cluster_id from $nhtable n
WHERE  NOT EXISTS (
       SELECT s.concept_id FROM $stytable s WHERE s.concept_id=n.concept_id
)
EOD

$dbh->selectToFile($sql, \*STDOUT);
$dbh->dropTables([$nhtable, $stytable]);
$dbh->disconnect;
exit 0;
