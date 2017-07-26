#!@PATH_TO_PERL@

# Script to run the EMS partitioning
#
# Changes:
#  05/10/2005 BAC (1-B6CE3): quotes needed around arguments passed to batchems.pl
#

# suresh@nlm.nih.gov - EMS 3 (11/2005)

# Environment variables
# EMS_HOME (required)
# EMS_CONFIG (optional)

# Command line arguments:
# -d <database> (by default the MID name server is used).
# -c (alternate config)

unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
push @INC, "$ENV{EMS_HOME}/bin";
use lib $ENV{EMS_HOME} . "/lib";

use Getopt::Std;
use EMSUtils;
use WMSUtils;
use GeneralUtils;
use EMSReportRequest;
use Midsvcs;
use OracleIF;

use Symbol;
use File::Basename;

getopts("d:c:");

die "ERROR: EMS_HOME not set\n" unless $ENV{EMS_HOME};
push @c, "db=$opt_d" if $opt_d;
push @c, "config=$opt_c" if $opt_c;
push @c, "action=me_partition";
push @c, "doit=1";
$cmd = $ENV{EMS_HOME} . "/bin/batchems.pl -c '" . join(" ", @c) . "'";
eval { system $cmd; };
die $@ if $@;
exit 0;
