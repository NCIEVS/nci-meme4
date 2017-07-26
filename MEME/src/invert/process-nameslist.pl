#!@PATH_TO_PERL@

# processes the "NamesList.txt" file and outputs a perl data structure

use Data::Dumper;

$nameslistfile = "$ENV{INV_HOME}/etc/NamesList.txt";

open(F, $nameslistfile) || die "Cannot open $nameslistfile";
while (<F>) {
  chomp;
  if (/^@@\t([^\t]+)\t([^\t]+\t([^\t]+)$/) {

    if ($current) {
      for ($i=$nextI; $i<=$current->{end}; $i++) {
	push @{ $current->{missing} }, $i;
      }
    }

    $nextI = hex($1);
    $blockname = $2;
    $end = $3;
    push @x, $current if $current;

    $current = {};
    $current->{name} = $blockname;
    $current->{start} = $nextI;
    $current->{end} = hex($end);
    $current->{missing} = [];
    $current->{notacharacter} = [];
    $current->{reserved} = [];

  } elsif (/([^\t]+)\t(.*)$/) {
    $found = $1;
    $name = $2;

    $nextC = sprintf("%X", $nextI);
    if ($nextC ne $found) {
      for ($i=$nextI; $i<hex($found); $i++) {
	push @{ $current->{missing}}, $i;
      }
      $nextI = hex($found)+1;
    } elsif ($name eq "<reserved>") {
	push @{ $current->{reserved} }, hex($found);
    } elsif ($name eq "<not a character>") {
	push @{ $current->{notacharacter} }, hex($found);
    }
  }
}
close(F);
push @x, $current if $current;
print Dumper(\@x);
exit 0;

