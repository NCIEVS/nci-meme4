#!@PATH_TO_PERL@
#
use strict;
use Getopt::Std;

use Tk;
use Tk::Tree;

our $srcDir = '../src';
our $tmpDir = '../tmp';

# -g  Generate intermediary files and save.
# -r  Root node said
# -n  root node name
# -l  level to expand the tree
# -s  source directory
# -o  tmpdir

my %options=();
getopts("gr:n:l:s:o:",\%options);
if (defined $options{s}) { $srcDir = $options{s}; }
if (defined $options{o}) { $tmpDir = $options{o}; }



our %defs=();
our %paths=();
our $tree;
our $whichLevel = 0;


sub prTime {
  my $str = shift;
  my $mdt = `date`;
  print "$str => $mdt\n";
}

sub makeData {
  open (PATHS, "< $srcDir/contexts.src") or die "No paths file.\n";
  my ($num, $rel, $ign, $path, $ln, $code, $str, @F);
  while (<PATHS>) {
    s/#.*//;			 
    next if /^(\s)*$/;
    chomp;
    ($num, $rel, $ign, $ign, $ign, $ign, $ign, $path) = split(/\|/, $_);
    if ($rel eq 'PAR') {
      $defs{"$num"} = '';
      @F = split(/\./, $path);
      $ln = @F;
      $paths{$ln}{"$path.$num"} = 1;
    }
  }
  close(PATHS);


  #$defs{'1500295'} = "<1500295^RootCode> LOINCROOT";
  open(DEFS, "< $srcDir/classes_atoms.src") or die "No defs file.\n";
  while (<DEFS>) {
    s/#.*//;			 
    next if /^(\s)*$/;
    chomp;
    ($num, $ign, $ign, $code, $ign, $ign, $ign, $str) = split(/\|/, $_);
    if (defined($defs{"$num"})) {
      $defs{"$num"} = "<$code> $str <$num>";
    }
  }
  close(DEFS);
}

sub getTailDir {
  my $path = shift;
  my @inp = split(/\//, $path);
  return pop(@inp);
}
sub saveData {
  my ($ele, $path, $lastDir);
  $lastDir = getTailDir($srcDir);

  # save defs
  open(OUT, "> $tmpDir/${lastDir}_defs")
    or die "could not open $tmpDir/${lastDir}_defs to write\n";
  foreach $ele (keys (%defs)) {
    print OUT "$ele|$defs{$ele}\n";
  }
  close(OUT);
  # save paths
  open(OUT, "> $tmpDir/${lastDir}_paths")
    or die "could not open $tmpDir/${lastDir}_paths to write\n";
  foreach $ele (sort keys (%paths)) {
    foreach $path (keys (%{$paths{"$ele"}})) {
      print OUT "$ele|$path\n";
    }
  }
  close(OUT);
}

sub readData {
  %defs=();
  %paths=();

  my ($ele, $path, $def, $lastDir);
  $lastDir = getTailDir($srcDir);
  # recover defs
  open(IN, "< $tmpDir/${lastDir}_defs")
    or die "could not open $tmpDir/${lastDir}_defs to write\n";
  while (<IN>) {
    chomp;
    ($ele, $def) = split(/\|/, $_);
    $defs{"$ele"} = $def;
  }
  close(IN);

  # recover paths
  open(IN, "< $tmpDir/${lastDir}_paths")
    or die "could not open $tmpDir/${lastDir}_paths to write\n";
  while (<IN>) {
    chomp;
    ($ele, $path) = split(/\|/, $_);
    $paths{$ele}{$path} = 1;
  }
  close(IN);
}

sub prepareData {
  if (defined $options{g}) {
    &makeData;
    &saveData;
    prTime('After data prep');
    exit;
  } else {
    &readData;
  }
}

sub closeAll {
  my ($ln, $path);
  foreach $ln (sort keys (%paths)) {
    next if ($ln > $whichLevel);
    foreach $path (keys (%{$paths{"$ln"}})) {
      $tree->close($path);
    }
  }
}

sub openAll {
  my ($ln, $path);
  foreach $ln (sort keys (%paths)) {
    next if ($ln > $whichLevel);
    foreach $path (keys (%{$paths{"$ln"}})) {
      $tree->open($path);
    }
  }
}

our $moreToDo = 1;
sub readNextLevel {
  if ($moreToDo > 0) {
    $moreToDo = 0;
    $whichLevel++;
    print "Reading $whichLevel\n";
    my ($path, @F, $num, );
    foreach $path (sort keys (%{$paths{"$whichLevel"}})) {
      @F = split(/\./, $path);
      $num = $F[$#F];
      $tree->add($path, -text => $defs{$num});
      $moreToDo++;
    }
    print "$moreToDo rows added at level $whichLevel\n";
  } else {
    print "No more data\n";
  }
  return $moreToDo;
}

prTime('Begin');
&prepareData;
prTime('After data prep');


# GUI start.
my $mw = MainWindow->new(-title => 'Tree Viewer');
my $bFrm = $mw->Frame;
$bFrm->Button(-text => 'Exit', -command => sub { exit; })->pack(-side => 'left', -anchor => 'n');
$bFrm->Button(-text => 'CloseAll', -command => \&closeAll)->pack(-side => 'left', -anchor => 'n');
$bFrm->Button(-text => 'OpenAll', -command => \&openAll)->pack(-side => 'left', -anchor => 'n');
$bFrm->Button(-text => 'Next', -command => \&readNextLevel)->pack(-side => 'left', -anchor => 'n');

my $tFrm = $mw->Frame;
$tree = $tFrm->Tree->pack(-fill => 'both', -expand => 1);


our ($ln, $path, $num, @F);

#populate the tree here
if (!defined $options{r}) {
  # get the first one from the level1 paths and use it as the root said.
  my $rootSaid = 0;
  foreach $path (keys (%{$paths{'1'}})) {
    ($rootSaid) = split(/\./, $path);
    $tree->add($rootSaid, -text => "<$rootSaid^RootNode>");
    last;
  }
} else {
  $tree->add($options{r}, -text => "<$options{r}^RootNode> $options{n}");
}

if (!defined $options{l}) {
  print "option l is not defined\n";
  while (my $x = &readNextLevel() > 0) {}
} else {
  print "Doing levels upto $options{l}\n";
  my $i = 0;
  my $limit = $options{l};
  while ($i < $limit) { $i++; &readNextLevel; print "\t level $i\n";}
}

&readNextLevel;
$tree->autosetmode();
$bFrm->pack();
$tFrm->pack(-fill => 'both', -expand => 1);

prTime('After adding to tree');

MainLoop;

