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
@col1 = qw(METAUI STYPE ATN SAB ATV SATUI);
@col2 = qw(CUI ATN SAB ATV SATUI);
foreach $col (@col1) {
  $colnum1{$col} = UrisUtils->getColIndex($dir_a, "MRSAT.RRF", $col);
}
foreach $col (@col2) {
  $colnum2{$col} = UrisUtils->getColIndex($dir_a, "MRSAT.RRF", $col);
}
$atuiindex = UrisUtils->getColIndex($dir_a, "MRSAT.RRF", 'ATUI');
$stypeindex = UrisUtils->getColIndex($dir_a,"MRSAT.RRF", 'STYPE');
$atnindex = UrisUtils->getColIndex($dir_a,"MRSAT.RRF", 'ATN');

$tmpa = "./$version_a.$$";
$tmpb = "./$version_b.$$";

&dump($dir_a, "MRSAT.RRF", $tmpa);
#FOr Concept level attributes, CUI, CODE, SAB,ATN,ATV are unique.
# for source level attributes AUI CODE SAB ATN ATV are unique.
&check_uniq($tmpa);
&dump($dir_b, "MRSAT.RRF", $tmpb);
&check_uniq($tmpb);

#$cmd = "/bin/comm -3 $tmpa $tmpb";
$cmd  = "join  -j 1 -t'|' $tmpa $tmpb"; 
#$cmd  = "diff -c $tmpa $tmpb";
open(C, "$cmd|") || die "ERROR: Cannot open temporary files";
while (<C>) {
  chomp;
  @x = split /\|/, $_;
  if ($x[1] eq $x[2]) {
  } else {
    print STDERR $_, "\n";
  }
}
close(C);
unlink $tmpa, $tmpb;
exit 0;

sub dump {
  my($dir, $file, $outputfile) = @_;
  my(@x, $x);
  my($cmd);
  my($path) = UrisUtils->getPath($dir, $file);
  my($key);
  my($md5gen) = Digest::MD5->new;

  open(O, "|/bin/sort -T . > $outputfile") || die "ERROR: Cannot open $outputfile";
  open(M, $path) || die "ERROR: Cannot open $path";
  while (<M>) {
    chomp;
    @x = split /\|/, $_;
    $md5gen->reset;
    if ($x[$stypeindex] =~ /^CUI/ ) {
	    foreach $col (@col2) {
	      $md5gen->add($x[$colnum2{$col}]);
	    }
    } else {
	    foreach $col (@col1) {
	      $md5gen->add($x[$colnum1{$col}]);
	    }
    }
           print O join("|", $md5gen->hexdigest, $x[$atuiindex]), "\n";
  }
  close(M);
  close(O);
  return;
}

sub check_uniq {
 my($file) = @_;
  $ct = `cut -f1 -d"|" $file | uniq -d |wc -l`;
  if ($ct != 0) {
   print STDERR "INVALID ATUIS. " . "\n";
  }
}
