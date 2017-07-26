#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";

#!/site/bin/perl5

# Do all concepts have STYs?
# suresh@nlm.nih.gov
# URIS 2.0 - 9/2003

# Options:
# -d <meta dir>

#use lib "/site/umls/release";
#use lib "/site/umls/lib/perl";
#use lib "/site/umls/uris-2.0/src";

use Getopt::Std;
use Data::Dumper;
use UrisUtils;
use File::Basename;

getopts("d:");

$metadir = $opt_d;
die "ERROR: Need the Metathesaurus directory in the -d option\n" unless $metadir;

$releaseformat = UrisUtils->getReleaseFormat($metadir);
if ($releaseformat eq "RRF") {
  $mrso = UrisUtils->getPath($metadir, "MRCONSO.RRF");
  $mrsty = UrisUtils->getPath($metadir, "MRSTY.RRF");
} else {
  $mrso = UrisUtils->getPath($metadir, "MRSO");
  $mrsty = UrisUtils->getPath($metadir, "MRSTY");
}

die "ERROR: $mrso does not exist!" unless -e $mrso;
die "ERROR: $mrsty does not exist!" unless -e $mrsty;

$mrsocuiindex = UrisUtils->getColIndex($metadir, basename($mrso), "CUI");

open(MRSO, $mrso) || die "Cannot open $mrso\n";
while (<MRSO>) {
  chomp;
  @_ = split /\|/, $_;
  $mrsocui{$_[$mrsocuiindex]}++;
}
close(MRSO);

$mrstycuiindex = UrisUtils->getColIndex($metadir, basename($mrsty), "CUI");
open(MRSTY, $mrsty) || die "Cannot open $mrsty\n";
while (<MRSTY>) {
  chomp;
  @_ = split /\|/, $_;
  $mrstycui{$_[$mrstycuiindex]}++;
}
close(MRSTY);

while (($cui, $freq) = each %mrsocui) {
  next if $mrstycui{$cui};
  print STDERR "ERROR: $cui does not have an STY in ", basename($mrsty), "\n";
  $error++;
}

while (($cui, $freq) = each %mrstycui) {
  next if $mrsocui{$cui};
  print STDERR "ERROR: $cui has an STY in ", basename($mrsty), " but no atoms in ", basename($mrso), "\n";
  $error++;
}

print "OK: All CUIs in ", basename($mrso), " have at least one STY and all CUIs in ", basename($mrsty), " have an atom in ", basename($mrso), ".\n";
exit 0;
