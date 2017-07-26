#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";


#! /site/bin/perl5

# Counts suppressibles in MRCONSO by the termtypes in MRRANK
# reports to STDERR if counts are 0
# suresh@nlm.nih.gov
# URIS 2.0 - 3/2004

# Options
# -m <path to META directory>
# -v <version>
# -o <path to output directory>

#use lib "/site/umls/release";
#use lib "/site/umls/lib/perl";
#use lib "/site/umls/uris-2.0/src";

use Getopt::Std;
use Data::Dumper;
use UrisUtils;
use CGI;
use File::Basename;

getopts("d:v:");

die "Need path to the meta directory in -d" unless -d $opt_d;
#die "Need path to the output directory in -o" unless -d $opt_o;
die "Need Metathesaurus version in -v" unless $opt_v;

$metadir = $opt_d;
$script = basename($0);

$releaseformat = UrisUtils->getReleaseFormat($metadir);
if ($releaseformat ne "RRF") {
  print "This test only runs on the RRF Metathesaurus.\n";
  exit 0;
}

$sabindex = UrisUtils->getColIndex($metadir, "MRRANK.RRF", "SAB");
$ttyindex = UrisUtils->getColIndex($metadir, "MRRANK.RRF", "TTY");
$suppressindex = UrisUtils->getColIndex($metadir, "MRRANK.RRF", "SUPPRESS");

open(M, UrisUtils->getPath($metadir, "MRRANK.RRF")) || die "ERROR: Cannot open MRRANK.RRF";
while (<M>) {
  chomp;
  @_ = split /\|/, $_;
  next unless $_[$suppressindex] eq "Y";
  $key = join("/", $_[$sabindex], $_[$ttyindex]);
  $suppressibleTermgroup{$key}++;
}
close(M);

$sabindex = UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "SAB");
$ttyindex = UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "TTY");
$suppressindex = UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "SUPPRESS");

open(M, UrisUtils->getPath($metadir, "MRCONSO.RRF")) || die "ERROR: Cannot open MRCONSO.RRF";
while (<M>) {
  chomp;
  @_ = split /\|/, $_;
  $key = join("/", $_[$sabindex], $_[$ttyindex]);

  $suppressible{$key}++ if $_[$suppressindex] eq "Y";
  $editor{$key}++ if $_[$suppressindex] eq "E";
}
close(M);

foreach $key (sort keys %suppressibleTermgroup) {
  if ($suppressible{$key} != 0) {
    print "OK: ", $key, " has ", $suppressible{$key}, " suppressible atoms in MRSONCO\n" ;
  } else {
    print STDERR "ERROR: $key was suppressible in MRRANK, but there were no matching atoms in MRCONSO.", "\n";
  }
}

$msg = "";
$c = 0;
$msg = "Additionally there were these editor-suppressed atoms in MRCONSO:\n\n";
foreach $key (sort keys %editor) {
  $msg .= "OK: " . $key . " has " . $editor{$key} . " suppressible atoms in MRSONCO\n" ;
  $c++;
}
print "\n$msg" if $c;

$msg = "";
$c = 0;
$msg .= "Supressible (SUPPRESS=Y) but not marked as a suppressible term type in MRRANK:\n\n";
foreach $key (sort keys %suppressible) {
  next if $suppressibleTermgroup{$key};
  $msg .= "ERROR: $key was suppressible in MRCONSO (" . $suppressible{$key} . " cases), but term type was not identified as suppressible in MRRANK." . "\n";
  $c++;
}
print STDERR "\n$msg" if $c;

exit 0;
