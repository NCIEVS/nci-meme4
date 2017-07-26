#!/site/bin/perl5

# Script to make the test checklist

# suresh@nlm.nih.gov - EMS 3 (4/2006)

# Environment variables
# EMS_HOME (required)
# EMS_CONFIG (optional)

use lib $ENV{EMS_HOME} . "/lib";

use Getopt::Std;
use EMSUtils;

die "ERROR: EMS_HOME not set\n" unless $ENV{EMS_HOME};

$tmpfile = "/tmp/testconcepts.$$";

open(T, ">$tmpfile") || die;
foreach (100..999) {
  print T $_, "\n";
}
close(T);

$cmd = $ENV{EMS_HOME} . "/scripts/make-checklist.pl -c chk_testconcepts < $tmpfile";
eval { system $cmd; };
die $@ if $@;
unlink $tmpfile;
exit 0;
