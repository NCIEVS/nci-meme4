#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";

#!/share_nfs/perl/5.8.6/bin/perl

# Are RUIs maintained across versions?
# suresh@nlm.nih.gov
# URIS 2.0 - 10/2004

# Options
# -t <path to META directory>
# -a <older version>
# -b <newer version>

#use lib "/umls/lib/perl";
#use lib "/site/umls/release";
#use lib "/site/umls/lib/perl";
#use lib "/umls/urisqa/QA/src";

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

# Values in these columns have to be identical if ATUI is the same
@col = qw(AUI1 STYPE1 REL AUI2 STYPE2 RELA SRUI SAB SL RG DIR);
foreach $col (@col) {
  push @colnum, UrisUtils->getColIndex($dir_a, "MRREL.RRF", $col);
}
$ruiindex = UrisUtils->getColIndex($dir_a, "MRREL.RRF", 'RUI');

$tmpa = "/tmp/$version_a.$$";
$tmpb = "/tmp/$version_b.$$";

&load($dir_a, "MRREL.RRF", $tmpa, \@colnum, 1);
&load($dir_b, "MRREL.RRF", $tmpb, \@colnum, 1);

$cmd = "/bin/comm -23 $tmpa $tmpb";
open(C, "$cmd|") || die "ERROR: Cannot open temporary files";
while (<C>) {
  chomp;
  s/^\s*//;
  @x = split /\|/, $_;
  print STDERR $_;
  if ($errors++ > 100) {
    print STDERR "Too many errors.. exiting\n";
    last;
  }
}
close(C);
unless ($errors) {
  print STDOUT "OK - all RUIs in $opt_a were present in ";
}
unlink $tmpa, $tmpb;
exit 0;

sub load {
  my($d) = @_;
  my(@x);
  my(%X);
  my($ruiindex) = UrisUtils->getColIndex($dir_a, "MRREL.RRF", "RUI");

  open(M, UrisUtils->getPath($d, "MRREL.RRF")) || die "ERROR: Cannot open $d/MRREL.RRF";
  while (<M>) {
    chomp;
    @x = split /\|/, $_;
    $X{$x[$ruiindex]} = [ map { $x[$colnum{$_}] } @col ];
  }
  close(M);
  return \%X;
}
