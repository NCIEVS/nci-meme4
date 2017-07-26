#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";


#! /site/bin/perl5

# Are SUIs maintained across versions?
# suresh@nlm.nih.gov
# URIS 2.0 - 9/2004

# Options
# -t <path to META directory>
# -a <older version>
# _b <newer version>

#use lib "/site/umls/release";
#use lib "/site/umls/lib/perl";
#use lib "/site/umls/uris-2.0/src";

use Getopt::Std;
use Data::Dumper;
use UrisUtils;
use CGI;
use File::Basename;

getopts("t:a:b:");

$top = $opt_t;
$version_a = $opt_a;
$version_b = $opt_b;
$dir_a = "$top/$version_a/META";
$dir_b = "$top/$version_b/META";

die "Need path to the older META contents in -a" unless -d $dir_a;
die "Need path to the newer META contents in -b" unless -d $dir_b;

$a = &load($dir_a);
$b = &load($dir_b);

$errors = 0;
while (($sui, $str) = each %$a) {
  next unless $b->{$sui};
  next if $str eq $b->{$sui};
  print STDERR "ERROR: Strings for $sui are different: ", join('|', $str, $b->{$sui}), "\n";
  $errors++;
  if ($errors>50) {
    print STDERR "Too many errors.. exiting\n";
    last;
  }
}
unless ($errors) {
  print "OK: identical strings in $version_a and $version_b had identical SUIs\n";
}
exit 0;

sub load {
  my($d) = @_;
  my($suiindex) = UrisUtils->getColIndex($d, "MRCONSO.RRF", "SUI");
  my($strindex) = UrisUtils->getColIndex($d, "MRCONSO.RRF", "STR");
  my(%str);

  open(M, UrisUtils->getPath($d, "MRCONSO.RRF")) || die "ERROR: Cannot open $d/MRCONSO.RRF";
  while (<M>) {
    chomp;
    @_ = split /\|/, $_;
    $str{$_[$suiindex]} = $_[$strindex];
  }
  close(M);
  return \%str;
}
