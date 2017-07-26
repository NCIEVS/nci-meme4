#
# File:     env.pl
# Author:   Brian Carlsen  (2005)
#
# To use:
# unshift @INC,"$ENV{ENV_HOME}/bin";
# require "env.pl";
#
package env;

# Set default encodings for STD file handles
binmode(STDIN,":utf8");
binmode(STDOUT, ":utf8");
binmode(STDERR,":utf8");

# Including this package sets the environment based on $ENV{ENV_FILE}
#
unless ($ENV{"ENV_FILE"}) {
  die '$ENV_FILE must be set.';
}

open (F,"$ENV{ENV_FILE}") || die "Could not open $ENV{ENV_FILE}: $! $?\n";
while (<F>) {
  if (/=/) {
    chop;
    ($var,$val) = split /=/;
    #print "$var = $val\n";
    $ENV{$var} = $val;
  }
}
close (F);
