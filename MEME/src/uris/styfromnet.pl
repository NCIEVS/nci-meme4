#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";


#!/site/bin/perl5

# Are all STYs from the Semantic Network?
# suresh@nlm.nih.gov
# URIS 2.0 - 9/2003

# Options:
# -m <Meta dir>
# -n <Net dir>

#use lib "/site/umls/release";
#use lib "/site/umls/lib/perl";
#use lib "/site/umls/uris-2.0/src";

use Getopt::Std;
use Data::Dumper;
use UrisUtils;
use File::Basename;

getopts("m:n:");

$metadir = $opt_m;
die "ERROR: Need the Metathesaurus directory in the -m option\n" unless $metadir;
$netdir = $opt_n;
die "ERROR: Need the Semantic Network directory in the -n option\n" unless $netdir;

$releaseformat = UrisUtils->getReleaseFormat($metadir);
if ($releaseformat eq "RRF") {
  $mrsty = UrisUtils->getPath($metadir, "MRSTY.RRF");
} else {
  $mrsty = UrisUtils->getPath($metadir, "MRSTY");
}
$srdef = "$netdir/SRDEF";

die "ERROR: $srdef does not exist!" unless -e $srdef;
die "ERROR: $mrsty does not exist!" unless -e $mrsty;

open(SRDEF, $srdef) || die "Cannot open $srdef\n";
while (<SRDEF>) {
  chomp;
  @x = split /\|/, $_;
  $srdefsty{$x[2]}++ if $x[0] eq "STY";
}
close(SRDEF);

$mrstycuiindex = UrisUtils->getColIndex($metadir, basename($mrsty), "CUI");
$mrstystyindex = UrisUtils->getColIndex($metadir, basename($mrsty), "STY");

open(MRSTY, $mrsty) || die "Cannot open $mrsty\n";
while (<MRSTY>) {
  chomp;
  @x = split /\|/, $_;
  $cui = $x[$mrstycuiindex];
  $sty = $x[$mrstystyindex];
  unless ($srdefsty{$sty}) {
    print STDERR "ERROR: ", $cui, " has STY: ", $sty, " which is not in the SRDEF file.\n";
    $error++;
  }
  $mrsty{$sty}++;
}
close(MRSTY);

foreach $sty (keys %srdefsty) {
  next if $mrsty{$sty};
  print STDERR "ERROR: STY \"$sty\" has no concepts in the Metathesaurus.\n";
  $error++;
}
print "All STYs in MRSTY come from the Semantic Network and all\nSemantic Network STYs have at least one matching concept in MRSTY.\n" unless $error;
exit 0;
