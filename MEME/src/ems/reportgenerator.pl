#!@PATH_TO_PERL@

# Script to generate concept reports
# Author: Suresh Srinivasan 2/98

# suresh@nlm.nih.gov -- Oracle port 4/00
# suresh@nlm.nih.gov - EMS 3 (3/2005)

# Writes report to stdout

# Environment variables
# EMS_HOME (required)
# EMS_CONFIG (optional)

# Command line arguments:
#
# -w <worklist name> (or reads concept_id's from STDIN)
# -d <database> (by default the MID name server is used).
# -m alternate MEME_HOST_PORT, e.g., oc.nlm.nih.gov:1526
# -c (concept_id or comma-separated list of IDs followed by an optional cluster ID)
#    e.g., -c 12312 or -c 12314:2 or -c 12039,23498,23498:44
# -t <report type> {1,2,..}
# -M (truncate to these many reviewed relations)
# -l (use these LATs only)
# -p <report format> {text|html|..}

# Added 5/99
# -r {NONE|DEFAULT|XR|ALL}
# -x {NONE|DEFAULT|SIB|ALL}

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";

use Getopt::Std;
use EMSUtils;
use WMSUtils;
use GeneralUtils;
use EMSReportRequest;
use Midsvcs;
use OracleIF;

use Symbol;
use File::Basename;

getopts("w:d:m:c:t:M:l:p:r:x:");

die "ERROR: EMS_HOME not set\n" unless $ENV{EMS_HOME};
EMSUtils->loadConfig;

$type = $opt_t || 1;

$db = $opt_d || Midsvcs->get('editing-db');

$optref->{maxreviewedrels} = $opt_M if $opt_M;
$optref->{lat} = $opt_l if $opt_l;
$optref->{outputformat} = $opt_p if $opt_p;
$optref->{r} = $opt_r if $opt_r;
$optref->{x} = $opt_x if $opt_x;
$optref->{db} = $db;
$optref->{reporttype} = $type;

$user = $EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF({user=>$user, password=>$password, db=>$optref->{db}});
$optref->{dbh} = $dbh;
die "Error: Oracle Not Available for database: $db" unless $dbh;

if ($opt_m) {
  $optref->{meme_host} = (split(/:/, $opt_m))[0];
  $optref->{meme_port} = (split(/:/, $opt_m))[1];
}
$optref->{env_home} = $EMSCONFIG{ENV_HOME} || $ENV{ENV_HOME};
$optref->{env_file} = $EMSCONFIG{ENV_FILE} || $ENV{ENV_FILE};

if ($opt_w) {
  $worklist = $opt_w;
  $conceptfile = EMSUtils->tempFile("reportgenerator");
  die "ERROR: Worklist: $worklist does not exist in $db\n" if $worklist && !$dbh->tableExists($worklist);

  WMSUtils->worklist2file($dbh, $worklist, $conceptfile);
  print WMSUtils->file2report($conceptfile, $optref);
  unlink $conceptfile;

} elsif ($opt_c) {
  $cluster_id = 0;
  if ($opt_c =~ /^(.*)\:(\d+)$/) {
    $cluster_id = $2;
    @concepts = split /,/, $1;
  } else {
    @concepts = split /,/, $opt_c;
  }
  my($ids);
  $ids->{list} = \@concepts;
  $ids->{idtype} = 'concept_id';
  $ids->{cluster_id} = $cluster_id if $cluster_id;
  $optref->{ids} = $ids;
  print WMSUtils->xreports($optref);

} else {

  my($ids);

  while (<>) {
    chomp;
    next unless /^\d+$/;
    $ids->{list} = [ $_ ];
    $ids->{idtype} = 'concept_id';
    $optref->{ids} = $ids;
    print WMSUtils->xreports($optref);
  }
}
$dbh->disconnect;
exit 0;

