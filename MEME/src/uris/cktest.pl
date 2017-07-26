#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#!/usr/local/bin/perl

# Are ATUIs unique in all files that have ATUIs?
# suresh@nlm.nih.gov
# URIS 2.0 - 9/2003

# Options:
# -m <Meta dir>
# -n <Net dir>


use Getopt::Std;
use Data::Dumper;
use File::Basename;


$metadir = "/export/home/chebiyc/QA/generator";
die "ERROR: Need the Metathesaurus directory in the -m option\n" unless $metadir;

$mrfiles = "$metadir/MRFILES";
$mrfiles = "$metadir/MRFILES.RRF" unless -e $mrfiles;
die "ERROR: $mrfiles does not exist or is not readable\n" unless -r $mrfiles;

open(M, $mrfiles) || die "Cannot open $mrfiles\n";
while (<M>) {
  chomp;
  @_ = split /\|/, $_;
  $file = $_[0];
  print "$file \n";
  next unless grep { $_ eq "ATUI" } split /\,/, $_[2];
  print "$_[2] this is after \n";
}
close(M);

exit 0;

