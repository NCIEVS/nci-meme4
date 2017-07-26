# Interfaces Perl to MySQL
# Author: suresh@nlm.nih.gov 1/2001

# Use this package to access tables in a MySQL database
package MySqlIF;

use DBI;

$MySqlIF::VERSION = '1.00';
$MySqlIF::FIELDSEPARATOR = '|';
$MySqlIF::DRIVER = "DBI:mysql";

# recognized parameters
# -host (server host name)
# -user (MySQL user name)
# -password (password for user - default is to use local file)
# -db (database name)

# CONSTRUCTOR
sub new {
  my($class, $initializer) = @_;
  my($self) = {};
  
  if (defined $initializer) {
    if (ref($initializer)) {
      if (ref($initializer) eq "HASH") {
	foreach (keys %{ $initializer }) {
	  $self->{ $_ } = $initializer->{$_};
	}
      } elsif (ref($initializer) eq "ARRAY") {
	for ($i=0; $i<@{$initializer}-1; $i++) {
	  $_[0] = $initializer->[$i];
	  $_[1] = $initializer->[$i+1];
	  $self->{$_[0]} = $_[1];
	}
      } elsif (ref($initializer) eq "SCALAR") {
	foreach (split /\&/, $$initializer) {
	  @_ = split /\=/, $_;
	  $self->{$_[0]} = $_[1];
	}
      }
    } else {
      foreach (split /\&/, $initializer) {
	@_ = split /\=/, $_;
	$self->{$_[0]} = $_[1];
      }
    }
  }
  bless $self, $class;
  $self->connect();
  return $self;
}

# SELECT functions

# returns first column of first row of a result set as a scalar
sub selectFirstRowAsScalar {
  return selectAsScalar(@_);
}

sub selectAsScalar {
  my(@refs) = &selectToArray(@_);
  return $refs[0]->[0];
}

# returns a single (first) row as an array ref
sub selectFirstRowAsRef {
  my(@refs) = &selectToArray(@_);
  return $refs[0];
}

# returns a single (first) row as an array
sub selectFirstRowAsArray {
  my(@refs) = &selectToArray(@_);
  my(@results);

  foreach ( @{ $refs[0] } ) {
    push @results, $_;
  }
  return @results;
}

# All rows as an array of scalars (useful if you are selecting a single column)
sub selectAllRowsAsArrayOfScalars {
  my(@refs) = &selectToArray(@_);
  my($ref);
  my(@results);

  foreach $ref (@refs) {
    push @results, $ref->[0];
  }
  return @results;
}

# All rows as an array of refs
sub selectAllRowsAsArrayOfRefs {
  return &selectToArray(@_);
}

# SELECT matching all rows to an array of references
sub selectToArray {
  my($self, $selectSTMT) = @_;
  my($sth);
  my(@results);
  my(@x);
  my($dbh);

  $self->connect;
  $sth = $self->{'dbh'}->prepare($selectSTMT);
  $sth->execute || die $self->{'dbh'}->errstr;
  while (@x = $sth->fetchrow_array) {
    push @results, [ @x ];
  }
  return @results;
}

sub stmtCache {
  my($self) = @_;
  foreach (keys %{ $self->{'sth'} }) {
    print $_, "\n";
  }
}

# SELECT's and returns a reference to an array data from a row
# Should be called repeatedly with the same SELECT until null
# is returned
sub selectARow {
  my($self, $selectSTMT) = @_;
  my($ref);
  my($sth);

  if (!defined($self->{'sth'}->{$selectSTMT})) {
    $self->connect();
    $self->{'sth'}->{$selectSTMT} = $self->{'dbh'}->prepare($selectSTMT);
    $self->{'sth'}->{$selectSTMT}->execute || die $self->{'dbh'}->errstr;
  }

  $sth = $self->{'sth'}->{$selectSTMT};
  $ref = $sth->fetchrow_arrayref;
  unless ($ref) {
    $sth->finish();
    $self->{'sth'}->{$selectSTMT} = undef;
  }
  return $ref;
}

# When calling selectARow, you may want to clear the statement cache
# if you are done with the select
sub clearStmtCache {
  my($self, $selectSTMT) = @_;
  my($sth);

  $sth = $self->{'sth'}->{$selectSTMT};
  if ($sth) {
    $sth->finish();
    $self->{'sth'}->{$selectSTMT} = undef;
  }
}

# This is probably too slow for large tables, but works
# Fields are '|' separated
# file can be a typeglob
sub selectToFile {
  my($self, $selectSTMT, $file) = @_;
  my($fd);
  my($sth);
  my($ref);

  if (ref $file ne "GLOB") {
    $fd = &utils::filehandle_name;
    open($fd, ">$file") || die "ERROR: Cannot open $file to write in selectToFile\n";
  } else {
    $fd = $file;
  }

  while ($ref = &selectARow($self, $selectSTMT)) {
    print $fd join($FIELDSEPARATOR, @{ $ref }), "\n";
  }
  close($fd) if ref $file ne "GLOB";
}

# Loads data into a table (must be | terminated)
sub file2table {
  my($self, $table, $file, $colspec) = @_;
  my($TABLE) = uc($table);
  my($sql);
  my(@refs, $ref);
  my(@cols, @v, %type);
  my($cols, $v, $col);

  $self->dropTable($table);
  $self->createTable($table, $colspec);

  foreach (@{$colspec}) {
    if (ref($_) eq "HASH") {
      ($col, $type) = each %$_;
    } else {
      push @cols, $_;
    }
  }
  $cols = join(', ', @cols);

  open(F, $file) || die "Cannot open $file to load data into table: $table\n";
  while (<F>) {
    chomp;
    @fields = split /\|/, $_;
    @v = ();
    for ($i=0; $i<@cols; $i++) {
      $v = $fields[$i];
      $v =~ s/\'/\'\'/g;
      push @v, (($type{$cols[$i]} =~ /CHAR/i || $type{$cols[$i]} =~ /CLOB/i) ? "\'" . $v . "\'" : $v);
    }
    $sql = "INSERT INTO $table ($cols) VALUES (" . join(', ', @v) . ")";
    $self->executeStmt($sql);
  }
  close(F);

# commit if needed
  $self->{dbh}->commit unless $self->{dbh}->{AutoCommit};
}

# Dumps a table to a file (or GLOB)
sub table2file {
  my($self, $table, $file) = @_;
  $self->selectToFile("SELECT * FROM $table", $file);
}

# execute some SQL
sub executeStmt {
  my($self, $stmt) = @_;
    
  $self->connect();

  if (ref($stmt) && ref($stmt) eq "ARRAY") {
    foreach $s (@{ $stmt }) {
      $self->executeStmt($s);
      last if ($@ || $self->{'dbh'}->errstr);
    }
  } else {
    $self->{'dbh'}->do($stmt);
  }
  return ($@ || $self->{'dbh'}->errstr);
}

# Drops all tables passed in list ref
sub dropTables {
  my($self, @tables) = @_;
  my($SQL);
  my($tables) = $tables[0];

  if (ref($tables) eq "ARRAY") {
    foreach $t (@{ $tables }) {
      $SQL = "DROP TABLE IF EXISTS $t";
      $self->executeStmt($SQL);
    }
  } else {
    foreach $t (@tables) {
      $SQL = "DROP TABLE IF EXISTS $t";
      $self->executeStmt($SQL);
    }
  }
}

# drop a table
sub dropTable {
  &dropTables(@_);
}

# creates a table (colspec is a anonymous array of scalars and hash refs, e.g.,
# ['a', 'b', {c=>'varchar(20)'}, d] will produce
# a integer, b integer, c varchar(20), d integer
# in other words, the col type defaults to integer
sub createTable {
  my($self, $table, $colspec) = @_;
  my($r, $k, @x, @c);

  foreach $r (@{ $colspec }) {
    if (ref($r) eq "HASH") {
      @x = keys %{ $r };
      foreach $k (@x) {
	push @c, "$k " . uc($r->{$k});
      }
    } else {
      push @c, "$r INTEGER";
    }
  }
  $self->executeStmt("CREATE TABLE $table (" . join(', ', @c) . ")");
}

# creates an index on a column
sub createIndex {
  my($self, $table, $col, $n) = @_;
  my($index) = join("_", "x", $table, $col);

  $self->executeStmt("CREATE INDEX $index ON $table ($col" . ($n ? "($n))" : ")"));
}

# Drops all tables passed in list ref
sub tableExists {
  my($self, $table) = @_;

  return grep { $_ eq $table } &selectAllRowsAsArrayOfScalars($self, "SHOW TABLES");
}

sub currentDate {
  my($self) = @_;

  return $self->selectAsScalar("SELECT CURRENT_DATE");
}

sub currentTime {
  my($self) = @_;

  return $self->selectAsScalar("SELECT CURRENT_TIME");
}

sub currentDateTime {
  my($self) = @_;

  return join(' ', $self->selectAsScalar("SELECT CURRENT_DATE"), $self->selectAsScalar("SELECT CURRENT_TIME"));
}

# opens the DB connection if needed
sub connect {
  my($self) = @_;
  my($str);

  return if (defined $self->{'dbh'});

  $self->{'host'} = $self->{'host'} || "umls-release.nlm.nih.gov";
  $self->{'user'} = $self->{'user'} || "meow";
  $self->{'passwd'} = $self->{'passwd'} || &get_passwd("/etc/umls/oracle.passwd", $self->{'user'});
  $self->{'db'} = $self->{'db'} || "mysql";

  $self->{'dbh'} = DBI->connect($DRIVER . ":database=$self->{'db'};host=$self->{'host'}", $self->{'user'}, $self->{'passwd'}, {'RaiseError' => 1, 'PrintError' => 1});
  die "ERROR: failed to connect to MySQL database\n" if (($@ || $DBI::errstr) || !$self->{'dbh'});
}

# disconnect from DB
sub disconnect {
  my($self) = @_;

  $self->{'dbh'}->{'Warn'} = 0;
  $self->{'dbh'}->disconnect if (defined($self->{'dbh'}));
}

# Escapes special characters in VARCHARs
sub quote {
  my($self, $varchar) = @_;
  return $self->{dbh}->quote($varchar);
}

# Read authentication information from file
sub get_passwd {
    my($passwdFile, $user) = @_;
    my($passwd) = "";

    open(PASSWD, $passwdFile) || return $passwd;
    while (<PASSWD>) {
	chomp;
	next if /^\#/ || /^\s*$/;
	my($u, $p) = split /\|/, $_;
	next unless $u eq $user;
	$passwd = $p;
	$passwd =~ tr/a-zA-Z/n-za-mN-ZA-M/;
	last;
    }
    close(PASSWD);
    $_="";
    @_ = ();
    return $passwd;
}
1;
