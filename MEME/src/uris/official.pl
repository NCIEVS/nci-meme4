#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";


# The UMLS Release Information System (URIS) version 2.0
# suresh@nlm.nih.gov 10/2003

# Displays official counts

# CGI params:

sub official {
  my($html);
  my($releaseconfig) =	UrisUtils->loadReleaseConfig($query->param('configfile'));
  my($outputfile) = join('/', $releaseconfig->{outputdir}, "/official.stdout");

  if (-e $outputfile) {
    $metadata = UrisUtils->evalfile($outputfile);
    $html .= &dump_data($metadata);
  } else {
    $html .= "The official counts has apparently not been run yet for this release.";
  }

  print join('',
	     $query->header,
	     $query->start_html($uristitle),
	     $html,
	     $query->end_html);
  return;
}


# makes txt and html versions of the data
sub dump_data {
  my($official) = @_;
  my(@txt);

  foreach $a (@{ $urisactions->{menuitem} }) {
    next unless $a->{name} eq "official";
    $description = $a->{description};
    last;
  }
  push @txt, ($description || "Official Counts") . "\n\n";
  push @txt, "Release version: $official->{releaseversion}";
  push @txt, "Release description: $official->{description}";
  push @txt, "Release format: $official->{releaseformat}";
  push @txt, "Concepts: $official->{concepts}";
  push @txt, "Number of concept names (AUIs): $official->{names}";
  push @txt, "Number of distinct concept names (SUIs): $official->{suis}";
  push @txt, "Number of distinct normalized concept names (LUIs): $official->{luis}";
  push @txt, "Number of sources (distinct source families by language): $official->{sabscountbylatsf}";
  push @txt, "\n";
  push @txt, "Number of sources contributing concept names: $official->{mrconsosabs}";
  push @txt, "Number of languages contributing concept names: $official->{mrconsolats}";

  push @txt, "\n" . "-" x 40 . "\n";
  push @txt, "Name count by SAB:\n";
  foreach (sort { $official->{namesbysab}{$b} <=> $official->{namesbysab}{$a} } keys %{$official->{namesbysab}}) {
    push @txt, sprintf("%20s: %15d (%6.2f%)", $_, $official->{namesbysab}{$_}, ($official->{names} > 0 ? ($official->{namesbysab}{$_}*100.0/$official->{names}) : ""));
  }
  push @txt, "\n";
  push @txt, "Name count by language:\n";
  foreach (sort { $official->{namesbylat}{$b} <=> $official->{namesbylat}{$a} } keys %{$official->{namesbylat}}) {
    push @txt, sprintf("%s: %10d (%6.2f%)", $_, $official->{namesbylat}{$_}, ($official->{names} > 0 ? ($official->{namesbylat}{$_}*100.0/$official->{names}) : ""));
  }
  push @txt, "\n";

  push @txt, "Name count by SRL:\n";
  foreach (sort { $official->{namesbysrl}{$b} <=> $official->{namesbysrl}{$b} } keys %{$official->{namesbysrl}}) {
    push @txt, sprintf("%s: %10d (%6.2f%)", $_, $official->{namesbysrl}{$_}, ($official->{names} > 0 ? ($official->{namesbysrl}{$_}*100.0/$official->{names}) : ""));
  }
  push @txt, "\n";

  push @txt, "Concept count by SRL combinations:\n";
  push @txt, "Number of concepts with at least one atom with any of these SRL values:\n";

  if ($official->{releaseformat} eq "RRF") {
    @x = ([0], [1], [2], [3], [0,4]);
  } else {
    @x = ([0], [1], [2], [3]);
  }
  foreach $r (@x) {
    $count = 0;
    foreach $key (keys %{$official->{conceptsbysrlcombo}}) {
      $count += $official->{conceptsbysrlcombo}{$key} if (&intersect($r, [ split /,/, $key ]));
    }
    $_ = join(',', @$r);
    push @txt, sprintf("%15s: %10d (%6.2f%)", $_, $count, ($official->{concepts} > 0 ? ($count*100.0/$official->{concepts}) : ""));
  }
  push @txt, "-" x 40, "\n";

  push @txt, "Count of atoms by suppressibility:\n";
  foreach (sort { $a cmp $b } keys %{$official->{suppressible}}) {
    push @txt, sprintf("%15s: %10d (%6.2f%)", $_, $official->{suppressible}{$_}, ($official->{names} > 0 ? ($official->{suppressible}{$_}*100.0/$official->{names}) : ""));
  }
  push @txt, "-" x 40, "\n";

  push @txt, "Number of sources contributing content (from MRSAB): $official->{mrsabsources}";
  push @txt, "Number of languages in the Metathesaurus (from MRSAB): $official->{mrsablat}";

  push @txt, "\n" . "-" x 60 . "\n";
  push @txt, "Source counts by language (from MRSAB):\n";
  foreach (sort { $official->{sourcesbylat}{$b} <=> $official->{sourcesbylat}{$b} } keys %{$official->{sourcesbylat}}) {
    $s = $_ || "   ";
    push @txt, sprintf("%s: %5d (%6.2f%)", $s, $official->{sourcesbylat}{$_}, ($official->{mrsablat} > 0 ? ($official->{sourcesbylat}{$_}*100.0/$official->{mrsabsources}) : ""));
  }

  push @txt, "\n";
  %sabsinlat = ();
  while (($sab, $lat) = each %{ $official->{latofsab} }) {
    push @{ $sabsinlat{$lat} }, $sab;
  }
  foreach $lat (sort keys %sabsinlat) {
    $s = "";
    $limit = 4;
    $a = $sabsinlat{$lat};
    push @txt, "\n" . $official->{sourcesbylat}{$lat} .
      (scalar(@$a) == 1 ? " source in: " : " sources in: ") . ($lat || "\"no language\"");
    for ($i=0; $i<scalar(@$a); $i++) {
      $sab = $a->[$i];
      $s .= "\n" if (($i%$limit) == 0);
      $s .= " ";
      $s .= sprintf("%18s", $sab);
    }
    push @txt, $s;
  }

  push @txt, "\n" . "-" x 60 . "\n";
  push @txt, sprintf("Approximate number of relationships (1/2 MRREL + 1/2 MRCOC + MRMAP): %d", $official->{rels});

  push @txt, "\n" . "-" x 40 . "\n";
  push @txt, "Distinct English strings (from MRCONSO): $official->{engstr}";
  push @txt, "Distinct lowercase English strings (from MRCONSO): $official->{englstr}";
  push @txt, "Distinct normalized English strings: $official->{engnstr}";
  push @txt, "Distinct lowercase English words (from MRXW_ENG): $official->{englwd}";
  push @txt, "Distinct normalized English words (from MRXNW_ENG): $official->{engnwd}";

  push @txt, "\n" . "-" x 60 . "\n";
  push @txt, sprintf("Concepts with names containing extended UTF-8 chars: %d (%s)", $official->{utf8concepts}, ($official->{concepts}>0 ? sprintf("%6.2f%", $official->{utf8concepts}/$official->{concepts}*100.0) . " of all concepts" : "n/a"));

  push @txt, "\n";
  push @txt, sprintf("All atoms containing extended UTF-8 chars: %d (%s)", $official->{utf8strings}{total}, ($official->{names}>0 ? sprintf("%6.2f%", $official->{utf8strings}{total}/$official->{names}*100.0) . " of all atoms" : "n/a"));
  
  push @txt, "\n";
  foreach $lat (sort keys %{ $official->{utf8strings} }) {
    next if $lat eq "total";
    push @txt, sprintf("  $lat atoms with extended UTF-8 chars: %10d (%s)", $official->{utf8strings}{$lat}, ($official->{namesbylat}{$lat} > 0 ? sprintf("%6.2f%", $official->{utf8strings}{$lat}/$official->{namesbylat}{$lat}*100.0) . " of all atoms in this language" : "n/a"));
  }
  push @txt, "\n";
  push @txt, sprintf("All words containing extended UTF-8 chars: %d (%s)", $official->{utf8words}{total}, ($official->{wordsbylat}{total} > 0 ? sprintf("%6.2f%", $official->{utf8words}{total}/$official->{wordsbylat}{total}*100.0) . " of all entries in all word index files" : "n/a"));

  push @txt, "\n";
  foreach $lat (sort keys %{ $official->{utf8words} }) {
    next if $lat eq "total";
    push @txt, sprintf("  $lat words with extended UTF-8 chars: %10d (%s)", $official->{utf8words}{$lat}, ($official->{wordsbylat}{$lat} > 0 ? sprintf("%6.2f%", $official->{utf8words}{$lat}/$official->{wordsbylat}{$lat}*100.0) . " of all words in this language" : "n/a"));
  }
  return $query->pre(join("\n", @txt));
}

sub intersect {
  my($a, $b) = @_;
  my(%x);

  %x = map { $_ => 1 } @$b;
  foreach (@$a) {
    return 1 if $x{$_};
  }
  return 0;
}
1;
