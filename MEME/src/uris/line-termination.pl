#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";


#!/site/bin/perl5

# Checks the line termination characters in each file
# suresh@nlm.nih.gov
# URIS 2.0 - 9/2003

# Options:
# -f <file>
# -l (iso|unix) type of line termination expected

use Getopt::Std;
use Data::Dumper;
getopts("f:l:");

$file = $opt_f;
die "ERROR: Need a Metathesaurus file in the -f option\n" unless $file;
die "ERROR: File $file does not exist or is not readable\n" unless -r $file;

die "ERROR: Need -l option\n" unless $opt_l;

if (-z $file) {
  print STDERR "ERROR: $file is empty\n";
  exit 0;
}

$crlf=($opt_l eq "iso");

open(F, $file) || die "Cannot open $file\n";
while (<F>) {
  if (/\r\n$/ && !$crlf) {
    $crlferror++;
  } elsif (/[^\r]\n$/ && $crlf) {
    $lferror++;
  }
  if (/\r\r\n$/) {
    $crcrlferror++;
  }
}
close(F);

print STDERR "File: $file: $crcrlferror lines had multiple CR \\r characters\n" if $crcrlferror;
print STDERR "File: $file: $crlferror lines had CRLF - expecting LF\n" if $crlferror;
print STDERR "File: $file: $lferror lines had LF - expecting CRLF\n" if $lferror;

unless ($crcrlferror > 0 || $crlferror > 0 || $lferror > 0) {
  print "File: $file used CRLF termination (ISO-style) consistently\n" if $crlf;
  print "File: $file used LF termination (UNIX-style) consistently\n" unless $crlf;
}
exit 0;
