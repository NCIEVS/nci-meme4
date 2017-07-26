#!@PATH_TO_PERL@

# Script to make the test checklist

# suresh@nlm.nih.gov - EMS 3 (4/2006)

# Environment variables
# EMS_HOME (required)
# EMS_CONFIG (optional)

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use Getopt::Std;
use EMSUtils;

die "ERROR: EMS_HOME not set\n" unless $ENV{EMS_HOME};

$tmpfile = "/tmp/testconcepts.$$";

open(T, ">$tmpfile") || die;
foreach (100..999) {
  print T $_, "\n";
}
close(T);

$cmd = $ENV{EMS_HOME} . "/bin/make-checklist.pl -c chk_testconcepts < $tmpfile";
eval { system $cmd; };
die $@ if $@;
unlink $tmpfile;
exit 0;
