#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";

#!/site/bin/perl5

# Collect column statistics and insert into the releasestats MySQL database
# suresh@nlm.nih.gov
# URIS-2.0 9/2004

# Options:
# -f <file>
# -d <Meta dir>
# -v <version>
# -g (poth to groupcol.txt file)
# -p <degree of parallelization>
# -G (just print the combinations for the file)

#use lib "/site/umls/uris-2.0/src";
#use lib "/site/umls/lib/perl";

use File::Basename;
use Getopt::Std;
use Data::Dumper;
use UrisUtils;
use MySqlIF;
use OracleIF;
use GeneralUtils;
use Midsvcs;

getopts("v:f:d:g:p:G");

$mysqldb = "URIS";
$uristable = "URIS_GROUPINGSTATS";
$groupcol = $opt_g || "/site/umls/uris-2.0/generator/groupcol.txt";

$opt_f = "MRCONSO.RRF";
$opt_d = "/net/umls-source/net/umls/Releases/2004AC/MMSYSFULL/2004AC/META";
$opt_v = "2004AC";

$file = basename($opt_f);
$dir = $opt_d;
die "Need Metathesaururs version in -v" unless $opt_v;
die "ERROR: Need a Metathesaurus file in the -f option\n" unless $file;
die "ERROR: File $file does not exist or is not readable\n" unless -r "$dir/$file";
die "ERROR: Need path to groupcol.txt file in the -g option\n" unless -e $groupcol;
die "ERROR: Need a Metathesaurus directory in the -d option\n" unless $dir;

$version = $opt_v;
$filename = basename($file);
$table = $filename;
$table =~ s/\.RRF$//;

@cols = UrisUtils->getColsForTable($dir, $filename);
$n=0;
foreach (@cols) {
  $colnum{$_} = $n++;
}

%validcol = map { $_ => 1 } UrisUtils->getColsForTable($dir, $filename);

# which columns are groupable? read the list from the groupcol.txt file
open(F, $groupcol) || die "ERROR: Cannot read $groupcol: $!\n";
while (<F>) {
  chomp;
  next if /^\#/ || /^\s*$/;
  @x = split /\|/, $_;
  foreach $f (split /[,\s]+/, $x[0]) {
    next unless $f eq $filename;
    foreach $c (split /[,\s]+/, $x[1]) {
      next unless $validcol{$c};
      $groupable{$c}++;
    }
  }
}
close(F);

@combos = &combinations(sort keys %groupable);

#$p = new ParallelExec($opt_p) if ($opt_p > 1);
foreach $combo (@combos) {
  next if @$combo == 0;
  &doit(@$combo);
}
exit 0;

sub doit {
  my(@cols) = @_;
  my($path) = UrisUtils->getPath($dir, $filename);
  my(@x);
  my(%stats);
  my($col);
  my($key, @k);
  my($t)=time;

  if ($opt_G) {
    print join("|", $filename, @cols), "\n";
    return;
  }

  print join("|", $filename, @cols), "\n";
  return unless join(',', @cols) eq "LAT,SAB,STT,TTY";

  open(F, $path) || die "ERROR: Cannot open $path";
  while (<F>) {
    chomp;
    @x = split /\|/, $_;
    @k = ();
    foreach $col (@cols) {
      push @k, $x[$colnum{$col}];
    }
    $key = join('|', @k);
    $stats{$key}++;
  }
  close(F);
  $t = time - $t;
  &record(\@cols, \%stats, $t);
}

# Given a list of items, produces combinations of them
sub combinations {
  my(@a) = @_;
  return ([]) unless @a;

  my($first) = $a[0];
  my(@rest) = @a[1..$#a];

  my(@c) = &combinations(@rest);
  my(@final);

  foreach (@c) {
    push @final, $_;
  }

  my($c);
  foreach $c (@c) {
    my(@temp);
    push @temp, $first;
    foreach (@{ $c }) {
      push @temp, $_;
    }
    push @final, \@temp;
  }
  return sort { @$a cmp @$b || $a->[0] cmp $b->[0] } @final;
}

sub record {
  my($cols, $stats, $timetaken) = @_;
  my($key);
  my(%mysqlcoltype) = (
		  version=>'varchar(24)',
		  timestamp=>'datetime',
		  mrtable=>'varchar(24)',
		  colnames=>'varchar(255)',
		  colvalues=>'varchar(255)',
		  howmany=>'integer',
		  timetaken=>'integer',
		 );
  my(%oraclecoltype) = (
		  version=>'varchar(24)',
		  timestamp=>'date',
		  mrtable=>'varchar(24)',
		  colnames=>'varchar(255)',
		  colvalues=>'varchar(255)',
		  howmany=>'integer',
		  timetaken=>'integer',
		 );
  my($editingdb) = Midsvcs->get('editing-db');
  my($dbh) = new MySqlIF({ 'db' => $mysqldb });
  my($oracleDBH) = new OracleIF({ 'db' => $editingdb });

  die "ERROR: Cannot open MySQL database: $mysqldb\n" unless $dbh;
  die "ERROR: Cannot open Oracle database: $editingdb\n" unless $oracleDBH;

  unless ($dbh->tableExists($uristable)) {
    my(@colspec);
    foreach (sort keys %mysqlcoltype) {
      push @colspec, { $_ => $mysqlcoltype{$_} };
    }
    $dbh->createTable($uristable, \@colspec);
    die "ERROR: Cannot create table: $uristable\n" unless $dbh->tableExists($uristable);
  }

  unless ($oracleDBH->tableExists($uristable)) {
    my(@colspec);
    foreach (sort keys %oraclecoltype) {
      push @colspec, { $_ => $oraclecoltype{$_} };
    }
    $oracleDBH->createTable($uristable, \@colspec);
    die "ERROR: Cannot create table: $uristable\n" unless $oracleDBH->tableExists($uristable);
  }

  $sql = "delete from $uristable where " .
    join(' and ',
	 "version=" . $dbh->quote($version),
	 "mrtable=" . $dbh->quote($filename),
	 "colnames=" . $dbh->quote(join(',', @$cols)));
  $dbh->executeStmt($sql);
  $oracleDBH->executeStmt($sql);

  foreach $key (keys %$stats) {

    $sql = "insert into $uristable(version,mrtable,colnames,colvalues,howmany,timestamp,timetaken) values(" .
      join(',',
	   $dbh->quote($version),
	   $dbh->quote($filename),
	   $dbh->quote(join(',', @$cols)),
	   $dbh->quote($key),
	   $stats->{$key},
	   "now()",
	   $timetaken) . ")";
    $dbh->executeStmt($sql);

    $sql = "insert into $uristable(version,mrtable,colnames,colvalues,howmany,timestamp,timetaken) values(" .
      join(',',
	   $oracleDBH->quote($version),
	   $oracleDBH->quote($filename),
	   $oracleDBH->quote(join(',', @$cols)),
	   $oracleDBH->quote($key),
	   $stats->{$key},
	   "SYSDATE",
	   $timetaken) . ")";
    $oracleDBH->executeStmt($sql);

    print join('|',
	       $version,
	       $filename,
	       join(',', @$cols),
	       $key,
	       $stats->{$key},
	       GeneralUtils->date,
	       $timetaken), "\n";
  }
  $dbh->disconnect;
  $oracleDBH->disconnect;
  return;
}

