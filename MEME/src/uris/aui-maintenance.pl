#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";


# Are AUIs maintained across versions?
# suresh@nlm.nih.gov
# URIS 2.0 - 9/2004

# Options
# -t <path to META directory>
# -a <older version>
# _b <newer version>

use lib "$ENV{EXT_LIB}";
#use lib "/site/umls/release";
#use lib "/site/umls/uris-2.0/src";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";
#! /site/bin/perl5


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
while (($aui, $ra) = each %$a) {
  next unless $b->{$aui};
  $rb = $b->{$aui};
  next if ($ra->{sab} eq $rb->{sab} && $ra->{tty} eq $rb->{tty} && $ra->{str} eq $rb->{str});
  print STDERR "ERROR: SAB/TTY/STR for $aui are different: ", join('|',
								   join('/', $ra->{sab}, $ra->{tty}, $ra->{str}),
								   join('/', $rb->{sab}, $rb->{tty}, $rb->{str})), "\n";
  $errors++;
  if ($errors>50) {
    print STDERR "Too many errors.. exiting\n";
    last;
  }
}
unless ($errors) {
  print "OK: identical atoms in $version_a and $version_b had identical AUIs\n";
}
exit 0;

sub load {
  my($d) = @_;
  my($auiindex) = UrisUtils->getColIndex($d, "MRCONSO.RRF", "SUI");
  my($strindex) = UrisUtils->getColIndex($d, "MRCONSO.RRF", "STR");
  my($sabindex) = UrisUtils->getColIndex($d, "MRCONSO.RRF", "SAB");
  my($ttyindex) = UrisUtils->getColIndex($d, "MRCONSO.RRF", "TTY");
  my(%x);
  my($r);

  open(M, UrisUtils->getPath($d, "MRCONSO.RRF")) || die "ERROR: Cannot open $d/MRCONSO.RRF";
  while (<M>) {
    chomp;
    @_ = split /\|/, $_;
    $x{$_[$auiindex]} = { $_[$sabindex]=>1, $_[$ttyindex]=>1, $_[$strindex]=>1};
  }
  close(M);
  return \%x;
}
