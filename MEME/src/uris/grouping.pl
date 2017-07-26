#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";

#!/site/bin/perl5

# Collect column statistics from MySQL DB and insert the resulting data into
# both MySQL and Oracle.

# MUST be run on the command line - not as a URIS generator!

# suresh@nlm.nih.gov
# URIS-2.0 1/2005

# Options:
# -d <Meta dir>
# -v <version>
# -f <MRFILE> do only this file
# -g (alternative poth to groupcol.txt file)
# -G (just print the combinations for the file)
# -F (force a re-do)

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

getopts("v:d:g:f:GF");

$mysqldb = "URIS";
$uristable = "URIS_GROUPINGSTATS";
$groupcol = $opt_g || "/site/umls/uris-2.0/generator/groupcol.txt";

$dir = $opt_d;
die "Need Metathesaururs version in -v" unless $opt_v;
die "ERROR: Need path to groupcol.txt file in the -g option\n" unless -e $groupcol;
die "ERROR: Need a Metathesaurus directory in the -d option\n" unless -d $dir;

$version = $opt_v;

$mrfiles = "$dir/MRFILES.RRF";
die "ERROR: Cannot find MRFILES.RRF\n" unless -e $mrfiles;
open(MRFILES, $mrfiles) || die "ERROR: Cannot open $mrfiles";
while (<MRFILES>) {
  chomp;
  next if m/\//; # no directories, e.g., CHANGE
  @x = split /\|/, $_;
  next if $opt_f && $opt_f ne $x[0];
  $cols{$x[0]} = [ split /\,/, $x[2] ];
}
close(MRFILES);

foreach $filename (sort keys %cols) {
  %colnum = ();
  @cols = ();
  %groupable = ();

  $n=0;
  foreach (@{ $cols{$filename} }) {
    push @cols, $_;
    $colnum{$_} = $n++;
  }

# which columns are groupable? read the list from the groupcol.txt file
  open(F, $groupcol) || die "ERROR: Cannot read $groupcol: $!\n";
  while (<F>) {
    chomp;
    next if /^\#/ || /^\s*$/;
    @x = split /\|/, $_;
    foreach $f (split /[,\s]+/, $x[0]) {
      next unless $f eq $filename;
      foreach $c (split /[,\s]+/, $x[1]) {
	next unless $colnum{$c};
	$groupable{$c}++;
      }
    }
  }
  close(F);

  @combos = &combinations(sort keys %groupable);

  foreach $combo (@combos) {
    next if @$combo == 0;
    &doit(@$combo);
  }
}
exit 0;

sub doit {
  my(@cols) = @_;
  my(@x);
  my(%stats);
  my($col);
  my($key, @k);
  my($t)=time;
  my($sql);
  my($mysqlDBH) = new MySqlIF("db=$version");
  my($table) = $filename;

  $table =~ s/\.RRF//;
  die "ERROR: Cannot connect to MySQL DB: $version" unless $mysqlDBH;
  $sql = "select " . join(",", @cols) . ", count(*) from $table group by " . join(",", @cols);

  if ($opt_G) {
    print $sql, "\n";
    $mysqlDBH->disconnect;
    return;
  }

  if (!$opt_F && &exists(\@cols)) {
    print join('|',
	       $version,
	       $filename,
	       join(',', @cols),
	       "Already done",
	       GeneralUtils->date), "\n";
    $mysqlDBH->disconnect;
    return;
  }

  foreach $r ($mysqlDBH->selectAllRowsAsArrayOfRefs($sql)) {
    @k = ();
    for ($i=0; $i<@cols; $i++) {
      push @k, $r->[$i];
    }
    $key = join("|", @k);
    $stats{$key} = $r->[@cols];
  }
  $t = time - $t;
  $mysqlDBH->disconnect;
  &record(\@cols, \%stats, $t);
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
  my($r);

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

# do records for this release/file exist?
sub exists {
  my($cols) = @_;
  my($dbh) = new MySqlIF({ 'db' => $mysqldb });
  my($status);

  die "ERROR: Cannot open MySQL database: $mysqldb\n" unless $dbh;

  return 0 unless $dbh->tableExists($uristable);

  my($sql) = "select timestamp from $uristable where " .
    join(' and ',
	 "version=" . $dbh->quote($version),
	 "mrtable=" . $dbh->quote($filename),
	 "colnames=" . $dbh->quote(join(',', @$cols))) . " limit 1";
  if ($dbh->selectFirstRowAsScalar($sql)) {
    $status = 1;
  } else {
    $status = 0;
  }
  $dbh->disconnect;
  return $status;
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

