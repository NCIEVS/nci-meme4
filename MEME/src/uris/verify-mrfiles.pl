#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";
use encoding 'utf8';

#!@PATH_TO_PERL@

# Does MRFILES speak the truth?
# suresh@nlm.nih.gov
# URIS-2.0 9/2003

# Options:
# -f <file>
# -d <Meta dir>

#use lib "/umls/lib/perl";
use File::Basename;
use Getopt::Std;
use Data::Dumper;
getopts("f:d:");

$file = $opt_f;
$base = basename($file);
$dir = $opt_d;
die "ERROR: Need a Metathesaurus file in the -f option\n" unless $file;
die "ERROR: File $file does not exist or is not readable\n" unless -r $file;
die "ERROR: Need a Metathesaurus directory in the -d option\n" unless $dir;

$mrfiles = "$dir/MRFILES";
$mrfiles = "$dir/MRFILES.RRF" unless -e $mrfiles;
die "ERROR: $mrfiles does not exist or is not readable\n" unless -r $mrfiles;

# Read MRFILES
open(M, $mrfiles) || die "Cannot open $mrfiles\n";
while (<M>) {
  chomp;
  @_ = split /\|/, $_;
  next unless $base eq $_[0];
  $fields = $_[3]+1;
  $rows = $_[4];
  $bytes = $_[5];
  &doit;
}
close(M);
exit 0;

sub doit {
  my($rowcount) = 0;
  my($bytecount) = 0;
  my(@x);
  my($colerror) = 0;

  open(F, $file) || die "Cannot read $file";

  while (<F>) {
    $rowcount++;
    $bytecount += length;
    chomp;

    @x = split /\|/, $_, -1;
    if (@x != $fields) {
      $colerror++;
      print STDERR "File: $base; line: $rowcount; expected $fields fields, found: ", scalar(@x), "\n";
      print STDERR join('|', @x), "\n";
      die "Too many errors... exiting.", "\n" if ($colerror > 100);
    }
  }
  close(F);

  unless ($colerror) {
    print "OK: all rows in $base have $fields fields\n";
  }

  if ($rowcount != $rows) {
    print STDERR "File: $base had $rowcount rows, expected $rows\n";
  } else {
    print "OK: row count for $base in MRFILES matches actual row count\n";
  }

  if ($bytecount != $bytes) {
    print STDERR "File: $base had $bytecount bytes, expected $bytes\n";
  } else {
    print "OK: byte count for $base in MRFILES matches actual byte count\n";
  }
  print "-" x 80, "\n";
  return;
}
