#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";


#!/site/bin/perl5

# Check for UI integrity - e.g. all CUIs, AUIs in all files have to be present in MRCONSO
# suresh@nlm.nih.gov 4/2004

# URIS-2.0 9/2003

# Options:
# -d <Meta dir>
# -f <file>

use File::Basename;
use Getopt::Std;
use Data::Dumper;
getopts("d:f:");

$dir = $opt_d;
die "ERROR: Need a Metathesaurus directory in the -d option\n" unless $dir && -d $dir;

$mrfiles = "$dir/MRFILES";
$mrfiles = "$dir/MRFILES.RRF" unless -e $mrfiles;
die "ERROR: $mrfiles does not exist or is not readable\n" unless -r $mrfiles;

open(MRFILES, $mrfiles) || die "Cannot open $mrfiles\n";
while (<MRFILES>) {
  chomp;
  @x = split /\|/, $_;
  $file = $x[0];

  $col = 1;
  @colnames = split /,/, $x[2];
  foreach (@colnames) {
    if (/^CUI$/ || /^CUI\d+$/) {
      $cuifile{$file}++;
      push @{ $cuiindex{$file } }, $col;
    } elsif (/^AUI$/ || /^AUI\d+$/) {
      $auifile{$file}++;
      push @{ $auiindex{$file } }, $col;
    } elsif (/^RUI$/ || /^RUI\d+$/) {
      $ruifile{$file}++;
      push @{ $ruiindex{$file } }, $col;
    } elsif (/^ATUI/) {
      $atuifile{$file}++;
      push @{ $atuiindex{$file } }, $col;
    } elsif (/^FROMID/ || /^FROMEXPR/) {
      $uifile{$file}++;
      push @{ $uiindex{$file } }, $col;
    }
    $col++;
  }

  for ($col=1; $col<@colnames+1; $col++) {
    $colindex{$file}{$colnames[$col-1]} = $col;
  }
}
close(MRFILES);

# Check to see if all CUIs are in MRCONSO
$mrconso = "$dir/MRSO";
$mrconso = "$dir/MRCONSO.RRF" unless -e $mrconso;
die "ERROR: $mrconso does not exist or is not readable\n" unless -r $mrconso;

open(F, $mrconso) || die "ERROR: Cannot open $mrconso";
$cuiindex = undef;
$cuiindex = $colindex{basename($mrconso)}{CUI};
$auiindex = undef;
if ($colindex{basename($mrconso)}{AUI}) {
  $auiindex = $colindex{basename($mrconso)}{AUI};
}
while (<F>) {
  @x = split /\|/, $_;
  $isacui{$x[$cuiindex-1]}++;
  $isanaui{$x[$auiindex-1]}++ if $auiindex;
}
close(F);

foreach $file (map { "$dir/$_" } sort keys %cuifile) {
  next if $file eq $mrconso;
  die "ERROR: $file does not exist or is not readable\n" unless -r $file;
  open(F, $file) || die "ERROR: Cannot open $file\n";
  $f = $file;
  $f =~ s!^$dir/!!;
  next if ($opt_f && ($opt_f ne $f));

  $linenum=0;
  $error = 0;
  $toomany = 0;
  while (<F>) {
    $linenum++;
    chomp;
    @x = split /\|/, $_;
    foreach $cuiindex (@{ $cuiindex{$f} }) {
      $cui = $x[$cuiindex-1];
      last unless $cui;

# CUI1 of MRCUI should NOT be in MRCONSO!
      if (($f =~ /^MRCUI/) && ($cuiindex eq $colindex{$f}{CUI1})) {


	if ($isacui{$cui}) {
	  print STDERR "ERROR: CUI1: $cui in file: $f, line: $linenum IS in MRCONSO/MRSO\n";
	  $error++;
	}
      } else {
	if ($f !~ /^MRAUI/ && !$isacui{$cui}) {
	  print STDERR "ERROR: CUI: $cui in file: $f, line: $linenum is not in MRCONSO/MRSO\n" ;
	  $error++;
	}
      }
      if ($error > 50) {
	print STDERR "Too many errors in $f - exiting\n";
	$toomany++;
	last;
      }
    }
    last if $toomany;
  }
  close(F);
  print "OK: CUI(s) in $file are all present in MRCONSO/MRSO\n" unless $error;
}

print "\n", "-" x 60, "\n";

foreach $file (map { "$dir/$_" } sort keys %auifile) {
  next if $file eq $mrconso;
  die "ERROR: $file does not exist or is not readable\n" unless -r $file;
  open(F, $file) || die "ERROR: Cannot open $file\n";
  $f = $file;
  $f =~ s!^$dir/!!;
#  $f =~ s/^$dir//;
  next if ($opt_f && ($opt_f ne $f));
  $linenum=0;
  $error = 0;
  $toomany = 0;
  while (<F>) {
    $linenum++;
    chomp;
    @x = split /\|/, $_;
    foreach $auiindex (@{ $auiindex{$f} }) {
      $aui = $x[$auiindex-1];
      last unless $aui;
      unless ($isanaui{$aui}) {
	print STDERR "ERROR: AUI: $aui in file: $f, line: $linenum is not in MRCONSO/MRSO\n";
	$error++;
      }
      if ($error > 50) {
	print STDERR "Too many errors in $f - exiting\n";
	$toomany++;
	last;
      }
    }
    last if $toomany;
  }
  close(F);
  print "OK: AUI(s) in $file are all present in MRCONSO/MRSO\n" unless $error;
}

print "\n", "-" x 60, "\n";

# One off checks

# The PAUI & PTR AUIs must be in MRCONSO..
$file = "$dir/MRHIER.RRF";
if (-e $file) {
  open(F, $file) || die "ERROR: Cannot open $file\n";
  $f = $file;
  $f =~ s!^$dir/!!;
#  $f =~ s/^$dir//;
  $linenum=0;
  $error = 0;
  $toomany = 0;
  while (<F>) {
    $linenum++;
    chomp;
    @x = split /\|/, $_;
    $pauiindex = $colindex{$f}{PAUI};
    $aui = $x[$pauiindex-1];

    if ($aui && !$isanaui{$aui}) {
      print STDERR "ERROR: AUI: $aui in the PAUI field of $f, line: $linenum is not in MRCONSO/MRSO\n";
      $error++;
    }
    if ($error > 50) {
      print STDERR "Too many errors in $f - exiting\n";
      $toomany++;
      last;
    }

    $ptrindex = $colindex{$f}{PTR};
    foreach $aui (split /\./, $x[$ptrindex-1]) {
      unless ($isanaui{$aui}) {
	print STDERR "ERROR: AUI: $aui in the PTR field of $f, line: $linenum is not in MRCONSO/MRSO\n";
	$error++;
      }
      if ($error > 50) {
	print STDERR "Too many errors in $f - exiting\n";
	$toomany++;
	last;
      }
    }
    last if $toomany;
  }
  close(F);
  print "OK: AUIs in the PAUI and PTR fields of $f are all present in MRCONSO/MRSO\n" unless $error;
}

print "\n", "-" x 60, "\n";

# The MAP files
$file = "$dir/MRMAP.RRF";
if (-e $file) {
  open(F, $file) || die "ERROR: Cannot open $file\n";
  $f = $file;
  $f =~ s!^$dir/!!;
#  $f =~ s/^$dir//;
  $linenum=0;
  $error = 0;
  $mapsetcuiindex = $colindex{$f}{MAPSETCUI};
  $fromexprindex = $colindex{$f}{FROMEXPR};
  $fromtypeindex = $colindex{$f}{FROMTYPE};

  while (<F>) {
    $linenum++;
    chomp;
    @x = split /\|/, $_;
    $cui = $x[$mapsetcuiindex-1];
    unless ($cui && $isacui{$cui}) {
      print STDERR "ERROR: MAPSETCUI: $cui in $f, line: $linenum is not in MRCONSO/MRSO\n";
      $error++;
      if ($error > 50) {
	print STDERR "Too many errors in $f - exiting\n";
	last;
      }
    }
    next unless $x[$fromtypeindex-1] eq "CUI";
    $cui = $x[$fromexprindex-1];
    unless ($cui && $isacui{$cui}) {
      print STDERR "ERROR: FROMEXPR: $cui in $f, line: $linenum is not in MRCONSO/MRSO\n";
      $error++;
      if ($error > 50) {
	print STDERR "Too many errors in $f - exiting\n";
	last;
      }
    }
  }
  close(F);
  print "OK: CUIs in FROMEXPR field of $f are all present in MRCONSO/MRSO\n" unless $error;
}

print "\n", "-" x 60, "\n";

$file = "$dir/MRSMAP.RRF";
if (-e $file) {
  open(F, $file) || die "ERROR: Cannot open $file\n";
  $f = $file;
  $f =~ s!^$dir/!!;
#  $f =~ s/^$dir//;
  $linenum=0;
  $error = 0;
  $mapsetcuiindex = $colindex{$f}{MAPSETCUI};
  $fromexprindex = $colindex{$f}{FROMEXPR};
  $fromtypeindex = $colindex{$f}{FROMTYPE};

  while (<F>) {
    $linenum++;
    chomp;
    @x = split /\|/, $_;
    $cui = $x[$mapsetcuiindex-1];
    unless ($isacui{$cui}) {
      print STDERR "ERROR: MAPSETCUI: $cui in $f, line: $linenum is not in MRCONSO/MRSO\n" unless $isacui{$cui};
      $error++;
      if ($error > 50) {
	print STDERR "Too many errors in $f - exiting\n";
	last;
      }
    }
    next unless $x[$fromtypeindex-1] eq "CUI";
    $cui = $x[$fromexprindex-1];
    unless ($cui && $isacui{$cui}) {
      print STDERR "ERROR: FROMEXPR: $cui in $f, line: $linenum is not in MRCONSO/MRSO\n" unless $isacui{$cui};
      $error++;
      if ($error > 50) {
	print STDERR "Too many errors in $f - exiting\n";
	last;
      }
    }
  }
  close(F);
  print "OK: CUIs in the FROMEXPR field of $f are all present in MRCONSO/MRSO\n" unless $error;
}
exit 0;

