#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";


#!/site/bin/perl5

# MRSAB Q/A
# 1. Do all files with SAB fields have SABs from MRSAB?
# 2. Do VCUI and RCUIs exist and do they have all the right atoms?
# 3. Do all atoms have the correct SRLs?
# 4. more?

# suresh@nlm.nih.gov
# URIS-2.0 4/2004

# Options:
# -d <Meta dir>

use File::Basename;
use Getopt::Std;
use Data::Dumper;
getopts("d:");

$dir = $opt_d;
die "ERROR: Need a Metathesaurus directory in the -d option\n" unless $dir;

$mrfilespath = "$dir/MRFILES";
$mrfilespath = "$dir/MRFILES.RRF" unless -e $mrfilespath;
die "ERROR: $mrfilespath does not exist or is not readable\n" unless -r $mrfilespath;

$mrsabpath = "$dir/MRSAB";
$mrsabpath = "$dir/MRSAB.RRF" unless -e $mrsabpath;
die "ERROR: $mrsabpath does not exist or is not readable\n" unless -r $mrsabpath;

$mrconsopath = "$dir/MRCONSO";
$mrconsopath = "$dir/MRCONSO.RRF" unless -e $mrconsopath;
die "ERROR: $mrconsopath does not exist or is not readable\n" unless -r $mrconsopath;

open(MRFILES, $mrfilespath) || die "Cannot open $mrfilespath\n";
while (<MRFILES>) {
  chomp;
  @x = split /\|/, $_;

  $file = $x[0];
  @colnames = split /,/, $x[2];
  for ($i=0; $i<@colnames; $i++) {
    $colindex{$file}{$colnames[$i]} = $i;
  }
}
close(MRFILES);

$mrsab = basename($mrsabpath);
open(F, $mrsabpath) || die "Cannot read $mrsabpath";
while (<F>) {
  chomp;
  @x = split /\|/, $_;
  $rsab = $x[$colindex{$mrsab}{RSAB}];
  $legalsab{$rsab}++;

  $vsab2cui{$rsab} = $x[$colindex{$mrsab}{VCUI}];
  $rsab2cui{$rsab} = $x[$colindex{$mrsab}{RCUI}];

  $vcui2sab{$x[$colindex{$mrsab}{VCUI}]} = $rsab;
  $rcui2sab{$x[$colindex{$mrsab}{RCUI}]} = $rsab;

  $srl{$rsab} = $x[$colindex{$mrsab}{SRL}];
  $rsab{$rsab}++;
}
close(F);

# every file with a SAB field must have a legal SAB
print "-" x 50, "\n";
foreach $file (sort keys %colindex) {
  next unless $colindex{$file}{SAB};

  $path = "$dir/$file";
  open(F, $path) || die "Cannot open $path";
  $linenum = 0;
  while (<F>) {
    chomp;
    $linenum++;
    @x = split /\|/, $_;
    $sab = $x[$colindex{$file}{SAB}];
    unless ($legalsab{$sab}) {
      $error{$file}++;
      if ($error{$file}>20) {
	print STDERR "Too many errors.. exiting\n";
	last;
      }
      print STDERR "ERROR: file: $file, line: $linenum has a SAB ($sab) not present in MRSAB.RRF\n";
    }
  }
  close(F);
  print "OK: In $file, all SAB values are present in MRSAB.\n" unless $error{$file};
}

# Check SRLs in MRCONSO
print "-" x 50, "\n";
$mrconso = basename($mrconsopath);
open(F, $mrconsopath) || die "Cannot open $mrconsopath";
$linenum = 0;
$error = 0;
while (<F>) {
  chomp;
  $linenum++;
  @x = split /\|/, $_;
  $cui = $x[$colindex{$mrconso}{CUI}];
  $sab = $x[$colindex{$mrconso}{SAB}];
  $srl = $x[$colindex{$mrconso}{SRL}];

  $vcuifound{$vcui2sab{$cui}}++ if $vcui2sab{$cui};
  $rcuifound{$rcui2sab{$cui}}++ if $rcui2sab{$cui};

  unless ($srl eq $srl{$sab}) {
    if (++$error > 20) {
      print STDERR "Too many errors.. exiting\n";
      last;
    }
    print STDERR "ERROR: Inconsistent SRL in line: $linenum, SAB: $sab: MRCONSO.RRF ($srl), MRSAB.RRF (", $srl{$sab}, ")\n";
  }
}
close(F);
print "\nOK: MRCONSO SRLs are consistent with MRSAB.\n\n" unless $error;

print "-" x 50, "\n";
foreach $sab (keys %rsab) {
  if ($vcuifound{$sab}) {
    print "OK: VCUI: ", $vsab2cui{$sab}, " found for $sab\n";
  } elsif ($vsab2cui{$sab}) {
    print STDERR "ERROR: VCUI: ", $vsab2cui{$sab}, " not found for $sab in MRCONSO.RRF\n";
  } else {
    print STDERR "ERROR: No VCUI for $sab\n";
  }

  if ($rcuifound{$sab}) {
    print "OK: RCUI: ", $rsab2cui{$sab}, " found for $sab\n";
  } elsif ($rsab2cui{$sab}) {
    print STDERR "ERROR: RCUI: ", $rsab2cui{$sab}, " not found for $sab in MRCONSO.RRF\n";
  } else {
    print STDERR "ERROR: No RCUI for $sab\n";
  }
}
exit 0;
