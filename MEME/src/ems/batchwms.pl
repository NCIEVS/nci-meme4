#!@PATH_TO_PERL@

# Script to run WMS in batch mode

# suresh@nlm.nih.gov - EMS 3 (11/2005)

# Environment variables
# EMS_HOME (required)
# EMS_CONFIG (optional)

# Command line arguments:
# -c (EMS CGI arguments, e.g., db=noa_mis2006 action=qa_generate bin_name=nosty)
unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";
use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";

use Getopt::Std;

getopts("c:");

die "ERROR: EMS_HOME not set\n" unless $ENV{EMS_HOME};

$cmd = $ENV{EMS_HOME} . "/bin/wms.pl $opt_c";
eval { system $cmd };
die $@ if $@;
exit 0;
