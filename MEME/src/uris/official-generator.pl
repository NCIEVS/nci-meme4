#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";


#! /site/bin/perl5

# Official counts for a release
# suresh@nlm.nih.gov
# URIS 2.0 - 10/2003

# Options
# -m <path to META directory>
# -v <version>

#use lib "/site/umls/release";
#use lib "/site/umls/lib/perl";
#use lib "/site/umls/uris-2.0/src";

use Getopt::Std;
use Data::Dumper;
use UrisUtils;
use CGI;
use File::Basename;

getopts("d:v:");

die "Need path to the meta directory in -d" unless -d $opt_d;
die "Need Metathesaururs version in -v" unless $opt_v;

$metadir = $opt_d;

$official{releaseversion} = $opt_v;

$releaseformat = UrisUtils->getReleaseFormat($metadir);
$official{releaseformat} = $releaseformat;

if ($releaseformat eq "RRF") {
  &do_rrf;
} else {
  &do_orf;
}
print Dumper(\%official);
exit 0;

sub do_rrf {
  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>"MRCONSO.RRF",
			      field=>'CUI',
			      unique=>1,
			     });
  $concepts = $_;
  $official{concepts} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      field=>"AUI",
			      unique=>1,
			     });
  $names = $_;
  $official{names} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      field=>"SUI",
			      unique=>1,
			     });
  $official{suis} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      field=>"LUI",
			      unique=>1,
			     });
  $official{luis} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      field=>"SAB",
			      unique=>1,
			     });
  $official{mrconsosabs} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      field=>"LAT",
			      unique=>1,
			     });
  $official{mrconsolats} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRSAB.RRF',
			      restrictions=>[{CURVER=>"==\"Y\""}],
			      field=>["SF", "LAT"],
			      unique=>1
			     });
  $official{sabscountbylatsf} = $_;

  $latofsab = {};
  $latindex = UrisUtils->getColIndex($metadir, "MRSAB.RRF", "LAT");
  $sabindex = UrisUtils->getColIndex($metadir, "MRSAB.RRF", "VSAB");
  open(M, UrisUtils->getPath($metadir, "MRSAB.RRF")) || die "ERROR: Cannot open MRSAB.RRF";
  while (<M>) {
    chomp;
    @_ = split /\|/, $_;
    $latofsab->{$_[$sabindex]} = $_[$latindex];
  }
  close(M);
  $official{latofsab} = $latofsab;

  $x = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      field=>"SAB",
			      groupindex=>1
			     });

  foreach (keys %{$x}) {
    $official{namesbysab}{$_} = $x->{$_};
  }

  $x = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      field=>"LAT",
			      groupindex=>1
			     });

  foreach (sort { $x->{$b} <=> $x->{$a} } keys %{$x}) {
    $official{namesbylat}{$_} = $x->{$_};
  }

  $x = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      field=>'SRL',
			      groupindex=>1
			     });

  foreach (sort { $x->{$b} <=> $x->{$b} } keys %{$x}) {
    $official{namesbysrl}{$_} = $x->{$_};
  }

  %srl = ();
  %count = ();
  $cui = "";
  $cuiindex = UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "CUI");
  $srlindex = UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "SRL");

  open(M, UrisUtils->getPath($metadir, "MRCONSO.RRF")) || die "ERROR: Cannot open MRCONSO.RRF";
  while (<M>) {
    chomp;
    @_ = split /\|/, $_;
    if ($cui && $_[$cuiindex] ne $cui) {
      $count{join(',', sort keys %srl)}++;
      %srl = ();
      $cui = "";
    }
    $cui = $_[$cuiindex];
    $srl{$_[$srlindex]}++;
  }
  close(M);
  $count{join(',', sort keys %srl)}++ if (keys %srl > 0);

  foreach (sort { $a cmp $b } keys %count) {
    $official{conceptsbysrlcombo}{$_} = $count{$_};
  }

  $x = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      field=>'SUPPRESS',
			      groupindex=>1
			     });
  foreach (sort { $x->{$b} <=> $x->{$b} } keys %{$x}) {
    $official{suppressible}{$_} = $x->{$_};
  }

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRSAB.RRF',
			      restrictions=>[{CURVER=>'=="Y"'}],
			      field=>'VSAB',
			      unique=>1
			     });
  $official{mrsabsources} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRSAB.RRF',
			      restrictions=>[{CURVER=>'=="Y"'}],
			      field=>'LAT',
			      unique=>1
			     });
  $official{mrsablat} = $_;

  $x = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRSAB.RRF',
			      restrictions=>[{CURVER=>'=="Y"'}],
			      field=>'LAT',
			      groupindex=>1
			     });

  foreach (sort { $x->{$b} <=> $x->{$b} } keys %{$x}) {
    $official{sourcesbylat}{$_} = $x->{$_};
  }

#------------------------
  $mrrel = UrisUtils->fieldcount($metadir, {table=>'MRREL.RRF'})/2;
  $mrcoc = UrisUtils->fieldcount($metadir, {table=>'MRCOC.RRF'})/2;
  $mrmap = UrisUtils->fieldcount($metadir, {table=>'MRMAP.RRF'});
  $official{rels} = int($mrrel+$mrcoc+$mrmap);

#------------------------
  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      restrictions=>[{LAT=>'=="ENG"'}],
			      field=>'STR',
			      unique=>1,
			     });
  $official{engstr} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCONSO.RRF',
			      restrictions=>[{LAT=>'=="ENG"'}],
			      field=>'STR',
			      postfilter=>"/bin/gawk \'{print tolower(\$0)}\'",
			      unique=>1,
			     });
  $official{englstr} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRXNS_ENG.RRF',
			      field=>'NSTR',
			      unique=>1
			     });
  $official{engnstr} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRXW_ENG.RRF',
			      field=>'WD',
			      unique=>1
			     });
  $official{englwd} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRXNW_ENG.RRF',
			      field=>'NWD',
			      unique=>1
			     });
  $official{engnwd} = $_;

# UTF-8 counts
  $cmd = "/bin/gawk -F'|' '{print \$" . UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "CUI", 1) . "\"\|\"\$" . UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "STR", 1) . "}' " . UrisUtils->getPath($metadir, "MRCONSO.RRF") . "|$ENV{URIS_HOME}/bin/grep8.pl|/bin/gawk -F'|' '{print \$1}\'|/bin/sort -u|/bin/wc -l";
  $_ = `$cmd`;
  chomp;
  $official{utf8concepts} = $_;

  $cmd = "/bin/gawk -F'|' '{print \$" . UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "STR", 1) . "}' " . UrisUtils->getPath($metadir, "MRCONSO.RRF") . "|$ENV{URIS_HOME}/bin/grep8.pl -c";
  $_ = `$cmd`;
  chomp;
  $official{utf8strings}{total} = $_;

  open(M, UrisUtils->getPath($metadir, "MRDOC.RRF")) || die "ERROR: MRDOC not found\n";
  while (<M>) {
    chomp;
    @x = split /\|/, $_;
    next unless $x[UrisUtils->getColIndex($metadir, "MRDOC.RRF", "DOCKEY")] eq "LAT" &&
      $x[UrisUtils->getColIndex($metadir, "MRDOC.RRF", "TYPE")] eq "expanded_form";

    $doclat = $x[UrisUtils->getColIndex($metadir, "MRDOC.RRF", "VALUE")];
    $atomlatfield = UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "LAT", 1);
    $cmd = "/bin/gawk -F'|' '\$$atomlatfield==\"$doclat\"{print \$" . UrisUtils->getColIndex($metadir, "MRCONSO.RRF", "STR", 1) . "}' " . UrisUtils->getPath($metadir, "MRCONSO.RRF") . "|$ENV{URIS_HOME}/bin/grep8.pl -c";
    $_ = `$cmd`;
    chomp;
    $official{utf8strings}{$doclat} = $_;

# WD
    $wdindex = "MRXW_" . $doclat . ".RRF";
    $cmd = "/bin/gawk -F'|' '{print \$" . UrisUtils->getColIndex($metadir, $wdindex, "WD", 1) . "}' " . UrisUtils->getPath($metadir, $wdindex) . "|$ENV{URIS_HOME}/bin/grep8.pl -c";
    $_ = `$cmd`;
    chomp;
    $official{utf8words}{$doclat} = $_;
    $official{utf8words}{total} += $_;

    $_ = UrisUtils->fieldcount($metadir, {table=>$wdindex});
    $official{wordsbylat}{$doclat} = $_;
    $official{wordsbylat}{total} += $_;
				
    $_ = `$cmd`;
    chomp;
    $official{utf8words}{$doclat} = $_;
    $official{utf8words}{total} += $_;
  }
  close(M);
}

# Original release format (ORF)
sub do_orf {
  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>"MRSO",
			      field=>'CUI',
			      unique=>1,
			     });
  $concepts = $_;
  $official{concepts} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRSO'
			     });
  $names = $_;
  $official{names} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRSO',
			      field=>"SUI",
			      unique=>1,
			     });
  $official{suis} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCON',
			      field=>"LUI",
			      unique=>1,
			     });
  $official{luis} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRSO',
			      field=>"SAB",
			      unique=>1,
			     });
  $official{mrconsosabs} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCON',
			      field=>"LAT",
			      unique=>1,
			     });
  $official{mrconsolats} = $_;

  $x = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRSO',
			      field=>"SAB",
			      groupindex=>1
			     });

  foreach (keys %{$x}) {
    $official{namesbysab}{$_} = $x->{$_};
  }

  $x = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCON',
			      field=>['LAT', 'CUI', 'SUI'],
			      groupindex=>1,
			      unique=>1
			     });

  foreach (sort { $x->{$b} <=> $x->{$a} } keys %{$x}) {
    $official{namesbylat}{$_} = $x->{$_};
  }

  $x = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRSO',
			      field=>'SRL',
			      groupindex=>1
			     });

  foreach (sort { $x->{$b} <=> $x->{$b} } keys %{$x}) {
    $official{namesbysrl}{$_} = $x->{$_};
  }

  %srl = ();
  %count = ();
  $cui = "";
  $cuiindex = UrisUtils->getColIndex($metadir, "MRSO", "CUI");
  $srlindex = UrisUtils->getColIndex($metadir, "MRSO", "SRL");

  open(M, UrisUtils->getPath($metadir, "MRSO")) || die "ERROR: Cannot open MRSO";
  while (<M>) {
    chomp;
    @_ = split /\|/, $_;
    if ($cui && $_[$cuiindex] ne $cui) {
      $count{join(',', sort keys %srl)}++;
      %srl = ();
      $cui = "";
    }
    $cui = $_[$cuiindex];
    $srl{$_[$srlindex]}++;
  }
  close(M);
  $count{join(',', sort keys %srl)}++ if (keys %srl > 0);

  foreach (sort { $a cmp $b } keys %count) {
    $official{conceptsbysrlcombo}{$_} = $count{$_};
  }

  $x = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCON',
			      field=>'TS',
			      groupindex=>1
			     });
  foreach (sort { $x->{$b} <=> $x->{$b} } keys %{$x}) {
    $official{suppressible}{$_} = $x->{$_};
  }

  if (-e UrisUtils->getPath($metadir, "MRSAB")) {
    $_ = UrisUtils->fieldcount($metadir,
			       {
				table=>'MRSAB',
				restrictions=>[{CURVER=>'=="Y"'}],
				field=>'VSAB',
				unique=>1
			       });
    $official{mrsabsources} = $_;

    $_ = UrisUtils->fieldcount($metadir,
			       {
				table=>'MRSAB',
				restrictions=>[{CURVER=>'=="Y"'}, {LAT=>'!=""'}],
				field=>'LAT',
				unique=>1,
			       });
    $official{mrsablat} = $_;

    $x = UrisUtils->fieldcount($metadir,
			       {
				table=>'MRSAB',
				restrictions=>[{CURVER=>'=="Y"'}],
				field=>'LAT',
				groupindex=>1
			       });

    foreach (sort { $x->{$b} <=> $x->{$b} } keys %{$x}) {
      $official{sourcesbylat}{$_} = $x->{$_};
    }
  }

#------------------------
  $mrrel = UrisUtils->fieldcount($metadir, {table=>'MRREL'})/2;
  $mrcoc = UrisUtils->fieldcount($metadir, {table=>'MRCOC'})/2;
  $mrmap = 0;
  $official{rels} = int($mrrel+$mrcoc+$mrmap);

#------------------------
  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCON',
			      restrictions=>[{LAT=>'=="ENG"'}],
			      field=>'STR',
			      unique=>1,
			     });
  $official{engstr} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRCON',
			      restrictions=>[{LAT=>'=="ENG"'}],
			      field=>'STR',
			      postfilter=>"/bin/gawk \'{print tolower(\$0)}\'",
			      unique=>1,
			     });
  $official{englstr} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRXNS.ENG',
			      field=>'NSTR',
			      unique=>1
			     });
  $official{engnstr} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRXW.ENG',
			      field=>'WD',
			      unique=>1
			     });
  $official{englwd} = $_;

  $_ = UrisUtils->fieldcount($metadir,
			     {
			      table=>'MRXNW.ENG',
			      field=>'NWD',
			      unique=>1
			     });
  $official{engnwd} = $_;

# UTF-8 counts
  $cmd = "/bin/gawk -F'|' '{print \$" . UrisUtils->getColIndex($metadir, "MRCON", "CUI", 1) . "\"\|\"\$" . UrisUtils->getColIndex($metadir, "MRCON", "STR") . "}' " . UrisUtils->getPath($metadir, "MRCON") . "|$ENV{URIS_HOME}/bin/grep8.pl|/bin/gawk -F'|' '{print \$1}\'|/bin/sort -u|/bin/wc -l";
  $_ = `$cmd`;
  chomp;
  $official{utf8concepts} = $_;

  $cmd = "/bin/gawk -F'|' '{print \$" . UrisUtils->getColIndex($metadir, "MRCON", "STR", 1) . "}' " . UrisUtils->getPath($metadir, "MRCON") . "|$ENV{URIS_HOME}/bin/grep8.pl -c";
  $_ = `$cmd`;
  chomp;
  $official{utf8strings} = $_;

  $cmd = "/bin/gawk -F'|' '{print \$" . UrisUtils->getColIndex($metadir, "MRXW.ENG", "WD", 1) . "}' " . UrisUtils->getPath($metadir, "MRXW.ENG") . "|$ENV{URIS_HOME}/bin/grep8.pl -c";
  $_ = `$cmd`;
  chomp;
  $official{utf8words}{ENG} = $_;
  $official{utf8words}{total} = $_;
}
