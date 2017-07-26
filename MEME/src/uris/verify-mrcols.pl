#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";

#!@PATH_TO_PERL@

# Does MRCOLS speak the truth?
# suresh@nlm.nih.gov
# URIS-2.0 1/2004

# Options:
# -d <Meta dir>
# -f <file>

#use lib "/umls/lib/perl";
use File::Basename;
use Getopt::Std;
use Data::Dumper;
getopts("d:f:");

$file = $opt_f;
$base = basename($file);
$dir = $opt_d;
die "ERROR: Need a Metathesaurus file in the -f option\n" unless $file;
die "ERROR: File $file does not exist or is not readable\n" unless -r $file;
die "ERROR: Need a Metathesaurus directory in the -d option\n" unless $dir;

$mrcols = "$dir/MRCOLS";
$mrcols = "$dir/MRCOLS.RRF" unless -e $mrcols;
die "ERROR: $mrcols does not exist or is not readable\n" unless -r $mrcols;

# Load the colstats.stdout file
$colstatsfile = "$opt_o/colstats.stdout";
die "$colstatsfile does not exist!" unless -f $colstatsfile;

open(F, $colstatsfile) || die "Cannot open $colstatsfile\n";
@_ = <F>;
close(F);
$colstats = eval(join("\n", @_));

# Read MRCOLS
open(M, $mrcols) || die "Cannot open $mrcols\n";
while (<M>) {
  chomp;
  @_ = split /\|/, $_;
  $file = $_[6];
  $col = $_[0];

  $minC=$_[3];
  $maxC=$_[5];
  $avC = $_[4];

  $minF = $colstats->{$file}->{$col}->{minlength};
  $maxF = $colstats->{$file}->{$col}->{maxlength};
  $avF = $colstats->{$file}->{$col}->{avglength};

  unless ($colstats->{$file}->{$col}) {
    print STDERR "ERROR: No computed stats for $file:$col\n";
    next;
  }

  if ($minF != $minC) {
    print STDERR "ERROR: minimum length for $file:$col in MRCOLS: $minC, but computed as: $minF", "\n";
  } else {
    print "OK: minimum length for $file:$col was $minF", "\n";
  }

  if ($maxF != $maxC) {
    print STDERR "ERROR: maximum length for $file:$col in MRCOLS: $maxC, but computed as: $maxF", "\n";
  } else {
    print "OK: maximum length for $file:$col was $maxF", "\n";
  }

  if ((($avF-$avC) > $avF/100.0) || (($avC-$avF) > $avF/100.0)) {
    print STDERR sprintf("ERROR: average length for $file:$col in MRCOLS: %.2f, but computed as: %.2f\n", $avC, $avF);
  } else {
    printf("OK: average length for $file:$col was close enough (%.2f, %.2f)\n", $avC, $avF);
  }
}
close(M);
exit 0;
