#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

binmode(STDIN,":raw");
binmode(STDOUT,":raw");
binmode(STDERR,":raw");

# Greps input for bytes with 8th bit set

use Getopt::Std;

getopts("c");

$n=0;
while (<>) {
  @bytes = unpack("C*", $_);
  foreach $b (@bytes) {
    next unless ($b>>7) > 0;
    if ($opt_c) {
      $n++;
    } else {
      print;
    }
    last;
  }
}
print $n, "\n" if $opt_c;
exit 0;
