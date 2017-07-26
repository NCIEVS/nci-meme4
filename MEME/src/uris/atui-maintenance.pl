#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

# -- old !/share_nfs/perl/5.8.6/bin/perl


# Are ATUIs maintained across versions?  I.e., are there rows with identical values
# in the columns shown below that have different ATUIs?

# suresh@nlm.nih.gov
# URIS 2.0 - 10/2004

# Options
# -t <path to META directory>
# -a <older version>
# -b <newer version>

use lib "$ENV{EXT_LIB}";
#use lib "/site/umls/release";
#use lib "/site/umls/uris-2.0/src";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";

use Getopt::Std;
use Data::Dumper;
use UrisUtils;
use CGI;
use File::Basename;
use Digest::MD5;

getopts("t:a:b:");

$top = $opt_t;
$version_a = $opt_a;
$version_b = $opt_b;
$dir_a = "$top/$version_a/META";
$dir_b = "$top/$version_b/META";

die "Need path to the older META contents in -a" unless -d $dir_a;
die "Need path to the newer META contents in -b" unless -d $dir_b;

# ATUIs should be identical if values in these columns are the same
@col = qw(CODE ATN SAB ATV);
foreach $col (@col) {
  $colnum{$col} = UrisUtils->getColIndex($dir_a, "MRSAT.RRF", $col);
}
$atuiindex = UrisUtils->getColIndex($dir_a, "MRSAT.RRF", 'ATUI');
$stypeindex = UrisUtils->getColIndex($dir_a, "MRSAT.RRF", 'STYPE');

$tmpa = "/tmp/$version_a.$$";
$tmpb = "/tmp/$version_b.$$";

&dump($dir_a, $tmpa);
&dump($dir_b, $tmpb);

$cmd = "/bin/comm -3 $tmpa $tmpb";
open(C, "$cmd|") || die "ERROR: Cannot open temporary files";
while (<C>) {
  chomp;
  s/^\s*//;
  @x = split /\|/, $_;
  print STDERR $_, "\n";
  if ($errors++ > 100) {
    print STDERR "Too many errors.. exiting\n";
    last;
  }
}
close(C);
unlink $tmpa, $tmpb;
exit 0;

# computes MD5 of all the fields that should make an ATUI unique
sub dump {
  my($dir, $outputfile) = @_;
  my(@x, $x);
  my($cmd);
  my($mrsat) = UrisUtils->getPath($dir, 'MRSAT');
  my($mrconso) = UrisUtils->getPath($dir, 'MRCONSO');
  my($key);
  my($md5gen) = Digest::MD5->new;
  my(%mrconsoref);

# this is needed to look up S*UIs in MRCONSO
  open(MRCONSO, UrisUtils->getPath($dir,"MRCONSO")) || die "ERROR: Cannot open MRCONSO";
  $mrconsoref{mrconsofd} = \*MRCONSO;
  $mrconsoref{sauiindex} = UrisUtils->getColIndex($dir_a, "MRCONSO.RRF", 'SAUI');
  $mrconsoref{scuiindex} = UrisUtils->getColIndex($dir_a, "MRCONSO.RRF", 'SCUI');
  $mrconsoref{sduiindex} = UrisUtils->getColIndex($dir_a, "MRCONSO.RRF", 'SDUI');
  $mrconsoref{cuiindex} = UrisUtils->getColIndex($dir_a, "MRCONSO.RRF", 'CUI');
  $mrconsoref{auiindex} = UrisUtils->getColIndex($dir_a, "MRCONSO.RRF", 'AUI');

  open(O, "|/bin/sort > $outputfile") || die "ERROR: Cannot open $outputfile";
#  open(M, $path) || die "ERROR: Cannot open $path";
open(M, UrisUtils->getPath($dir_a, "MRSAT.RRF")) || die "ERROR: Cannot open $d/MRSAT.RRF";
  while (<M>) {
    chomp;
    @x = split /\|/, $_;
    if (grep { $_ eq $x[$stypeindex]} qw(CUI AUI CODE)) {
      my($r) = &cuiaui2source(\%mrconsoref);
    #  $ui = 
    }

    $md5gen->reset;
    foreach $col (@col) {
      $md5gen->add($x[$colnum{$col}]);
    }
    print O join("|", $md5gen->hexdigest, $x[$atuiindex]), "\n";
  }
  close(M);
  close(O);

  close(MRCONSO);
  return;
}

# Given a CUI and AUI, returns source ui's in a ref
sub cuiaui2source {
  my($ref) = @_;
  my(%sourceref);

  return if (GeneralUtils->seekstr($ref->{mrconsofd}, $ref->{cui}) == -1);
  while ($ref->{mrconsofd}) {
    chomp;
    @x = split /\|/, $_;
    last if $x[$ref->{cuiindex}] ne $ref->{cui};
    next if $x[$ref->{auiindex}] ne $ref->{aui};
    $sourceref->{saui} = $x[$ref->{sauiindex}];
    $sourceref->{scui} = $x[$ref->{scuiindex}];
    $sourceref->{sdui} = $x[$ref->{sduiindex}];
    last;
  }
  return \%sourceref;
}
