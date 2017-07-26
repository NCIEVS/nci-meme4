#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#!/site/bin/perl5

# Are ATUIs unique in all files that have ATUIs?
# suresh@nlm.nih.gov
# URIS 2.0 - 9/2003

# Options:
# -m <Meta dir>
# -n <Net dir>

use lib "$ENV{EXT_LIB}";
#use lib "/site/umls/release";
#use lib "/site/umls/lib/perl";
#use lib "/site/umls/uris-2.0/src";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";

use Getopt::Std;
use Data::Dumper;
use UrisUtils;
use File::Basename;

getopts("m:");

$metadir = $opt_m;
die "ERROR: Need the Metathesaurus directory in the -m option\n" unless $metadir;

$mrfiles = "$metadir/MRFILES";
$mrfiles = "$metadir/MRFILES.RRF" unless -e $mrfiles;
die "ERROR: $mrfiles does not exist or is not readable\n" unless -r $mrfiles;

open(M, $mrfiles) || die "Cannot open $mrfiles\n";
while (<M>) {
  chomp;
  @_ = split /\|/, $_;
  $file = $_[0];
  next unless grep { $_ eq "ATUI" } split /\,/, $_[2];
  $i = UrisUtils->getColIndex($metadir, $file, "ATUI");
  next if $i == -1;
  push @files, $file;
  &load($file, $i);
}
close(M);

for ($i=0; $i<@files; $i++) {
  $file1 = $files[$i];
  for ($j=$i+1; $j<@files; $j++) {
    $file2 = $files[$j];
    while (($a, $value1) = each %{ $atui{$file1} }) {
      next unless $atui{$file2}{$a};
      print STDERR "ERROR: ATUI: $a is present in $file1 and $file2\n";
      $error++;
    }
  }
}
print "OK: All ATUIs are unique across files.\n" unless $error;
exit 0;

sub load {
  my($file, $index) = @_;

  open(F, UrisUtils->getPath($metadir, $file)) || die "Cannot open $file\n";
  while (<F>) {
    chomp;
    @x = split /\|/, $_;
    $atui = $x[$index];
    $atui{$file}{$atui}++;
  }
  close(F);
}
