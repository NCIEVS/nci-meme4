#!@PATH_TO_PERL@

# Script to initialize the EMS3 environment from an EMS2 environment
# in a database
# Author: Suresh Srinivasan 1/2006

# Environment variables
# EMS_HOME (required)
# EMS_CONFIG (optional)

# Command line arguments:
#
# -d <database>

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use Getopt::Std;
use EMSUtils;
use WMSUtils;
use GeneralUtils;
use Midsvcs;
use OracleIF;
use EMSBinlock;

use Symbol;
use File::Basename;

getopts("d:");

die "ERROR: MIDSVCS_HOME not set\n" unless $ENV{MIDSVCS_HOME};
die "ERROR: DBPASSWORD_HOME not set\n" unless $ENV{DBPASSWORD_HOME};
die "ERROR: EMS_HOME not set\n" unless $ENV{EMS_HOME};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');

$user = $EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);

$dbh = new OracleIF({user=>$user, password=>$password, db=>$db});
die "Error: Oracle Not Available for database: $db" unless $dbh;

# create all EMS3 tables
EMSTables->createAllTables($dbh);

# copy data from ems_daily_snapshot
$dbh->executeStmt("truncate table $EMSNames::DAILYSNAPSHOTTABLE");
$sql = "insert into $EMSNames::DAILYSNAPSHOTTABLE select snapshot_date-1 as snapshot_date, snapshot_type, snapshot_attr, snapshot_count from ems_daily_snapshot";
$dbh->executeStmt($sql);

# copy daily actions
$dbh->executeStmt("truncate table $EMSNames::DAILYACTIONCOUNTTABLE");
$sql = "insert into $EMSNames::DAILYACTIONCOUNTTABLE select * from ems_daily_action_count";
$dbh->executeStmt($sql);

# EDITING_EPOCH
$dbh->executeStmt("truncate table $EMSNames::EDITINGEPOCHTABLE");
$sql = "insert into $EMSNames::EDITINGEPOCHTABLE select * from ems_current_editing_epoch";
$dbh->executeStmt($sql);

# BEING_EDITED
$binsql = WMSUtils->worklist2binSQL();
$dbh->executeStmt("truncate table $EMSNames::BEINGEDITEDTABLE");
$sql = "insert into $EMSNames::BEINGEDITEDTABLE select $binsql as bin_name, worklist_name, concept_id from ems_being_edited";
$dbh->executeStmt($sql);

# AH history
# This is an EMS3 only table and is independently loaded (load_history.pl)

# AH canonical name
$dbh->executeStmt("truncate table $EMSNames::AHCANONICALNAMETABLE");
$sql = "insert into $EMSNames::AHCANONICALNAMETABLE select ah_bin as bin_name, canonical_name from ems_ah_canonical_bin";
$dbh->executeStmt($sql);

# BIN INFO
# some column names were changed, other dropped, no tracking epoch, though data are historical
$dbh->executeStmt("truncate table $EMSNames::BININFOTABLE");
@bin_names = $dbh->selectAllAsArray("select distinct bin_name from ems_bin_info");

foreach $bin_name (@bin_names) {
  $qb = $dbh->quote($bin_name);

  $maxepochid = $dbh->selectFirstAsScalar("select max(bin_epoch_id) from ems_bin_info where bin_name=$qb");

  $sql = <<"EOD";
insert into $EMSNames::BININFOTABLE (
  bin_name, bin_type, generation_date, generation_time, generation_user,
  nextWorklistNum, nextChemWorklistNum, nextNonchemWorklistNum,
  totalClusters, totalConcepts, totalUneditableClusters,
  chemClusters, chemConcepts, chemUneditableClusters,
  nonchemClusters, nonchemConcepts, nonchemUneditableClusters)
select
  bin_name,
  bin_type,
  bin_generated_on as generation_date,
  bin_generation_time as generation_time,
  bin_generated_by as generation_user,
  next_worklist_num as nextWorklistNum,
  next_worklist_num_nc as nextNonchemWorklistNum,
  next_worklist_num_ch as nextChemWorklistNum,
  total_clusters as totalClusters,
  total_concepts as totalConcepts,
  uneditable_clusters as totalUneditableClusters,
  total_chem_clusters as chemClusters,
  total_chem_clusters as totalConcepts,
  uneditable_chem_clusters as chemUneditableClusters,
  total_nonchem_clusters as nonchemClusters,
  total_nonchem_clusters as nonchemConcepts,
  uneditable_nonchem_clusters as nonchemUneditableClusters
from ems_bin_info
where bin_name=$qb
and   bin_generated_on=(select max(bin_generated_on) from ems_bin_info where bin_name=$qb)
and   bin_epoch_id=$maxepochid
EOD
  $dbh->executeStmt($sql);
}

# MAXTAB
EMSMaxtab->clear($dbh);

# access control OK - maxtab entries removed

# clear all locks
foreach $l (EMSBinlock->get_all($dbh)) {
  EMSBinlock->unlock($dbh, $l);
}

# clear TMP files
$d = join("/", $ENV{EMS_HOME}, "tmp");
opendir(D, $d) || die "Cannot readdir $d: $@";
foreach $f (readdir(D)) {
  $p = join("/", $d, $f);
  next unless -f $p;
  next unless $f =~ /^EMS3TMP/i;
  unlink $p;
}
closedir(D);

# Ensure all ME, QA, AH Configs match

$dbh->disconnect;
exit 0;

