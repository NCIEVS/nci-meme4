#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";

#! /site/bin/perl5

# Conservation of mass.  For sources not updated between releases, the counts of
# atoms, rels and attributes should be identical.

# suresh@nlm.nih.gov
# URIS 2.0 - 10/2004

# Options
# -t <path to META directory>
# -a <older version>
# -b <newer version>

#use lib "/site/umls/release";
#use lib "/site/umls/lib/perl";
#use lib "/site/umls/uris-2.0/src";

use Getopt::Std;
use Data::Dumper;
use UrisUtils;
use CGI;
use File::Basename;
use Digest::MD5;
use GeneralUtils;

getopts("t:a:b:");

$top = $opt_t;
$version_a = $opt_a;
$version_b = $opt_b;
$dir_a = "$top/$version_a/META";
$dir_b = "$top/$version_b/META";

die "Need path to the older META contents in -a" unless -d $dir_a;
die "Need path to the newer META contents in -b" unless -d $dir_b;

# Load MRSABs
$sabA = &loadMRSAB($dir_a);
$sabB = &loadMRSAB($dir_b);

foreach $rsab (sort keys %$sabA, keys %$sabB) {
  next if $done{$rsab}++;
  if ($sabA->{$rsab} && $sabB->{$rsab} && $sabA->{$rsab} eq $sabB->{$rsab}) {
    print STDERR $rsab, "\n";
    $countA = &getCounts($dir_a, $rsab);
    $countB = &getCounts($dir_b, $rsab);

    if ($countA->{atoms} == $countB->{atoms} &&
	$countA->{relationships}  == $countB->{relationships}  &&
	$countA->{attributes} == $countB->{attributes} &&
	$countA->{contexts} == $countB->{contexts}
       ) {

      $x = join(", ", map { $_ . " (" . $countA->{$_} . ")" } sort keys %$countA);
      print "SAB: $rsab: counts of $x in $version_a and $version_b are identical\n";
    } else {
      foreach $k (sort keys %$countA) {
	next if $countA->{$k} == $countB->{$k};
	print STDERR "ERROR: SAB: $rsab: different counts of $k ", join(', ', $countA->{$k}, $countB->{$k}), " in $version_a and $version_b";
      }
    }
  } elsif ($sabA->{$rsab} && $sabB->{$rsab}) {
    print "OK: SAB: $rsab was updated between $version_a and $version_b\n";
  } elsif ($sabA->{$rsab} && !$sabB->{$rsab}) {
    print "OK: SAB: $rsab was dropped in $version_b\n";
  } elsif ($sabB->{$rsab} && !$sabA->{$rsab}) {
    print "OK: SAB: $rsab was new in $version_b\n";
  }
}
exit 0;

# Loads the data in MRSAB
sub loadMRSAB {
  my($dir) = @_;
  my(%sab);
  my($rsabindex, $vsabindex);

  open(MRSAB, UrisUtils->getPath($dir,"MRSAB")) || die "ERROR: Cannot open MRSAB";
  $rsabindex = UrisUtils->getColIndex($dir_a, "MRSAB.RRF", 'RSAB');
  $vsabindex = UrisUtils->getColIndex($dir_a, "MRSAB.RRF", 'VSAB');
  while (<MRSAB>) {
    chomp;
    @x = split /\|/, $_;
    $sab{$x[$rsabindex]} = $x[$vsabindex];
  }
  close(MRSAB);
  return \%sab;
}

sub getCounts {
  my($dir, $rsab) = @_;
  my(%count);
  my($file, @x, $c, $rsabindex);
  my(%map) = (
	      MRCONSO=>'atoms',
	      MRREL=>'relationships',
	      MRSAT=>'attributes',
	      MRHIER=>'contexts',
	     );
  my(@cmd, $cmd, $path);

  foreach $file (keys %map) {
    $path = UrisUtils->getPath($dir, $file);
    $rsabindex = UrisUtils->getColIndex($dir, $file . ".RRF", 'SAB', 1);
    $cmd = "/bin/gawk -F'|' \'\$" . $rsabindex . "==" . "\"$rsab\"" . "{n++}END{print n}' $path > /tmp/$file.out";
    push @cmd, $cmd;
  }

  $p = new ParallelExec(2);
  $p->run(\@cmd);
  foreach $file (keys %map) {
    $path = "/tmp/$file.out";
    if (-e $path) {
      chomp($c = GeneralUtils->file2str($path));
      $count{$map{$file}} = $c+0;
    } else {
      $count{$map{$file}} = 0;
    }
    unlink $path;
  }
  return \%count;
}
