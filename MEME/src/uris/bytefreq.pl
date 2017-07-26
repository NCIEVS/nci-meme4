#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#!/site/bin/perl5

# Counts ASCII characters for all MR files
# suresh@nlm.nih.gov
# URIS 2.0 - 9/2003

# Options:
# -f <file>

use Getopt::Std;
use Data::Dumper;
getopts("f:");

$file = $opt_f;
die "ERROR: Need a Metathesaurus file in the -f option\n" unless $file;
die "ERROR: File $file does not exist or is not readable\n" unless -r $file;

if (-z $file) {
  print STDOUT "ERROR: $file is empty\n";
  #exit 0;
}

open(F, $file) || die "Cannot open $file\n";
while (<F>) {
  map { $count->{$_}++ } unpack("C*", $_);
}
close(F);

print Dumper($count);
exit 0;
