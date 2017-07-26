# Interfaces Perl to Oracle
# Author: suresh@nlm.nih.gov 8/2002

# Use this package to access an Oracle database
package OracleIF;

use lib "/site/umls/lib/perl";

use DBI;
use DBD::Oracle;
use File::Copy;
use GeneralUtils;
use Midsvcs;

$OracleIF::VERSION = '1.00';
$OracleIF::FIELDSEPARATOR = '|';
$OracleIF::DRIVER = "dbi:Oracle";

# parameters
# user (user name)
# password (password for user - default is to use local file)
# db (database, i.e., TNS, name)

# debug=

# if $main::oracleLogFile is defined and writeable, it appends error messages
# to it.

# CONSTANTS
$CONSTANTS = {
	      MAXTABLENAMELENGTH => 30,
	      MAXVARCHARDIMENSION => 4000
	      };

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

  foreach (keys %{ $CONSTANTS }) {
    $self->{$_} = $CONSTANTS->{$_};
  }

  $ENV{'ORACLE_BASE'}="/export/home/oracle" unless $ENV{'ORACLE_BASE'};
  $ENV{'ORACLE_HOME'} = "/export/home/oracle" unless $ENV{'ORACLE_HOME'};
  $ENV{'ORA_ENCRYPT_LOGIN'} = 'TRUE' unless $ENV{'ORA_ENCRYPT_LOGIN'};

  return ($self->connect() ? undef : $self);
}

# for debugging
sub toString {
  my($self, $header) = @_;

  print "-"x10, " $header ", "-"x10, "\n" if $header;
  print "OracleIF: $self, DBH: $self->{dbh}\n";
  foreach (keys %{ $self->{dbh} }) {
    print "\t\t$_ -> ", $self->{dbh}->{$_}, "\n";
  }
  foreach (keys %{ $self }) {
    next if $_ eq "dbh";
    print "\t$_ -> ", $self->{$_}, "\n";
  }
}

sub debug {
  my($self, $val) = @_;
  $self->{debug} = $val;
}

# DUP's a class ref
sub dup {
  my($self) = @_;
  my($new);
  my(%attr);

  foreach (keys %{ $self }) {
    next if $_ eq "dbh";
    $attr{$_} = $self->{$_};
  }

  $new = new OracleIF(\%attr);
#  $new->connect;
  return $new;
}

# Pings the database
sub ping {
  my($self) = @_;
  return $self->{dbh}->ping;
}

# SELECT functions

# returns a row as a scalar
sub selectFirstAsScalar {
  my(@results) = &selectFirstAsArray(@_);
  return join(FIELDSEPARATOR, @results);
}

# returns a row as an array of scalars
sub selectFirstAsArray {
  my(@refs) = &selectAllAsRef(@_);
  return @{ $refs[0] };
}

# returns a row as a reference to an array
sub selectFirstAsRef {
  my(@refs) = &selectAllAsRef(@_);
  return $refs[0];
}

# returns all rows as an array of scalars
sub selectAllAsArray {
  my(@x, $r);
  foreach $r (&selectAllAsRef(@_)) {
    push @x, $r->[0];
  }
  return @x;
}

# returns all rows as an array of refs
sub selectAllAsRef {
  my($self, $stmt) = @_;
  my($sth);
  my(@x);
  my(@results);

  print STDERR $stmt, "\n" if ($self->{debug});
  $self->connect;
  $sth = $self->{dbh}->prepare($stmt);
  &errorlog;
  die "ERROR: Cannot prepare statement for: $stmt\n" . ($self->{dbh}->errstr || $@) . "\n" unless $sth;
  unless ($sth->execute) {
    &errorlog;
    print STDERR $self->{dbh}->errstr, "\n" if ($self->{debug} && $@);
    die "ERROR: " . ($self->{dbh}->errstr || $@);
  }

  while (@x = $sth->fetchrow_array) {
    push @results, [ @x ];
  }
  return @results;
}

# This sub can be called to return one row at a time
# It caches the statement (Call clearStmtCache()
# to explicitly clear the cache)
sub selectRowAsRef {
  my($self, $stmt) = @_;
  my($ref);
  my($sth);

  if (!defined($self->{sth}->{$stmt})) {
    $self->connect();
    $self->{sth}->{$stmt} = $self->{dbh}->prepare($stmt);
    &errorlog;
    unless ($self->{sth}->{$stmt}->execute) {
      &errorlog;
      die $self->{dbh}->errstr;
    }
  }

  $sth = $self->{sth}->{$stmt};
  $ref = $sth->fetchrow_arrayref;
  $self->clearStmtCache($stmt) unless $ref;
  return $ref;
}

# maps a reference to a select'ed DB row to a hash ref with column names as keys
# so you can use it like $row->{ColumnName} instead of $rowref->[12]
sub row2ref {
  my($self, $rowref, @column_names) = @_;
  my($h, $r);
  my($i) = 0;
  
  foreach (@column_names) {
    $h->{$column_names[$i]} = $rowref->[$i];
    $i++;
  }
  return $h;
}

# clears the cached statement handle
sub clearStmtCache {
  my($self, $stmt) = @_;
  my($sth);

  $sth = $self->{sth}->{$stmt};
  if ($sth) {
    $sth->finish();
    $self->{sth}->{$stmt} = undef;
  }
}

# This is probably too slow for large tables, but works
# Fields are '|' separated
# file can be a typeglob
sub selectToFile {
  my($self, $stmt, $file) = @_;
  my($fd);
  my($sth);
  my($ref);

  if (ref $file ne "GLOB") {
    use Symbol;
    $fd = gensym;
    open($fd, ">$file") || die "ERROR: Cannot open $file to write in selectToFile\n";
  } else {
    $fd = $file;
  }

  while ($ref = $self->selectRowAsRef($stmt)) {
    print $fd join($FIELDSEPARATOR, @{ $ref }), "\n";
  }
  close($fd) if ref $file ne "GLOB";
}

# Loads data (SLOW!) into a table (fields must be | separated)
sub file2table {
  my($self, $table, $file, $colspec) = @_;
  my($TABLE) = uc($table);
  my($sql);
  my(@refs, $ref);
  my(@cols, @v, %type);
  my($cols, $v, $col);

  if ($colspec) {
    $self->dropTable($table);
    $self->createTable($table, $colspec);
  }

  @refs = $self->selectAllAsRef(<<"EOD");
SELECT column_name, data_type from ALL_TAB_COLUMNS WHERE table_name=\'$TABLE\'
EOD
  foreach $ref (@refs) {
    $type{$ref->[0]} = $ref->[1];
    push @cols, $ref->[0];
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

# Loads data into a table from a file using sqlldr
# typespec is a scalar representing the different types
# (i: integer, c: character)
# e.g., "iiic" for 3 integer fields, ending with a character field
sub sqlldr {
  my($self, $table, $file, $colspec) = @_;
  my($datfile, $ctlfile);
  my($sqlldr) = $ENV{'ORACLE_HOME'} . "/bin/sqlldr";
  my($r, @t, $c, $t);

  $self->dropTable($table);
  $self->createTable($table, $colspec);

  if ($file =~ /\.dat$/) {
    $datfile = $file;
  } else {
    $datfile = "/tmp/${file}_$$" . ".dat";
    copy($file, $datfile);
  }
  $ctlfile = "/tmp/${table}_$$.ctl";

  die "ERROR: sqlldr not found\n" unless -e $sqlldr;

  my($terminator);
  my(@x) = @{ $colspec };
  for ($i=0; $i<@x; $i++) {
    $r = $x[$i];
    $terminator = ($i == $#x ? "WHITESPACE" : "\'" . $FIELDSEPARATOR . "\'");
    if (ref($r) eq "HASH") {
      foreach $c (keys %{ $r }) {
	if (/CHAR/i) {
	  push @t, "$c CHAR TERMINATED BY $terminator";
	} else {
	  push @t, "$c INTEGER EXTERNAL TERMINATED BY $terminator";
	}
      }
    } else {
      push @t, "$r INTEGER EXTERNAL TERMINATED BY $terminator";
    }
  }
  $t = join(",\n", @t);

  open(CONTROL, ">$ctlfile") || die "Cannot open $ctlfile\n";
  print CONTROL <<"EOD";
LOAD DATA
INFILE \'$datfile\'
INTO TABLE $table
\(
$t
\)
EOD
  close(CONTROL);

  my($authstr) = $self->{user} . "/" . $self->{password} . "\@" . $self->{db};
  my($status) = system "$sqlldr \"$authstr\" CONTROL=$ctlfile LOG=/dev/null silent=all";
  unlink $ctlfile;
  unlink $datfile unless ($file =~ /\.dat$/);
  return $status;
}

# Runs a script using SQL*plus
# ONLY LINES WITH NUMBERS AND '|' ARE PRESENT IN THE OUTPUT
sub sqlplus_integer_output {
  my($self, $script, $output) = @_;
  my($sqlplus) = $ENV{'ORACLE_HOME'} . "/bin/sqlplus";
  my($authstr) = $self->{user} . "/" . $self->{password} . "\@" . $self->{db};

  if (ref($output) eq "GLOB") {
    $fd = $output;
  } else {

    use Symbol;

    $fd = gensym;
    open($fd, ">$output") || die "ERROR: Cannot open $output";
  }
  open(S, "$sqlplus -S \"$authstr\" < $script|") || die "ERROR: Cannot run sqlplus";
  while (<S>) {
    chomp;
    s/^\s*//;
    s/\s*$//;
    next if /^\s*$/;
    next if /[^\d\|]/;
    print $fd $_, "\n";
  }
  close(S);
  close($fd) unless ($output eq $fd);
}

# reads in a list of SQL commands and executes them
# Commands have to be one per line (or continued with a trailing \
# DROP TABLE commands are transformed to &dropTable calls
sub executeFile {
  my($self, $file) = @_;
  my(@cmds, $cmd);

  open(F, $file) || die "ERROR: Cannot read file: $file in executeFile";
  while (<F>) {
    chomp;
    unless (m%\$%) {
      $cmd .= ($cmd ? " " : "") . $_;
      push @cmds, $cmd;
      $cmd = "";
    }
  }
  close(F);

  foreach $cmd (@cmds) {
    $cmd =~ s/^\s*//;
    $cmd =~ s/\s*$//;
    $cmd =~ s/;$//;

    if ($cmd =~ /^DROP TABLE (.*)$/i) {
      $self->dropTable($1);
    } else {
      $self->executeStmt($cmd);
    }
  }
}

# execute some SQL
sub executeStmt {
  my($self, $stmt) = @_;
  my($err);

  $self->connect;
  print STDERR $stmt, "\n" if ($self->{debug});
  $err = ($@ || $self->{dbh}->errstr);
  if ($err) {
    print STDERR "ERROR in: $stmt\n";
    die $err;
  }

  if (ref($stmt) && ref($stmt) eq "ARRAY") {
    foreach $s (@{ $stmt }) {
      $self->executeStmt($s);
      last if ($@ || $self->{dbh}->errstr);
    }
  } else {
    $self->{dbh}->do($stmt);
  }
  $err = ($@ || $self->{dbh}->errstr);
  if ($err) {
    print STDERR "ERROR in: $stmt\n";
    die $err;
  }
}

# Calls a stored PL_SQL procedure
# if $plsqlvar names the variable containing the return value, if any
# e.g., OracleIF->plsql("MEME_INTEGRITY_PROC.msh_sep", "t")
sub plsql {
  my($self, $plsql, $plsqlvar) = @_;
  my($sth);
  my($retval);

  $plsqlvar = ":" . $plsqlvar if $plsqlvar && $plsqlvar !~ /^\:/;

  $plsql .= ";" unless $plsql =~ /\;$/;
  if ($plsqlvar) {
    $plsql = "BEGIN $plsqlvar := $plsql END;";
  } else {
    $plsql = "BEGIN $plsql END;";
  }

  $sth = $self->{dbh}->prepare($plsql);
  $sth->bind_param_inout($plsqlvar, \$retval, 64) if $plsqlvar;
  $sth->execute;
  
  die ($@ || $DBI::errstr) if ($@ || $DBI::errstr);
  return $retval if $plsqlvar;
}

# Drops one or more tables
sub dropTables {
  my($self, @tables) = @_;
  my($t);
  my($tables) = $tables[0];

  if (ref($tables) eq "ARRAY") {
    foreach $t (@{ $tables }) {
      $self->dropTables($t);
    }
  } elsif (ref($tables) eq "HASH") {
    foreach $t (keys %{ $tables }) {
      $self->dropTables($t);
    }
  } else {
    foreach $t (@tables) {
      $self->executeStmt("DROP TABLE $t") if $self->tableExists($t);
    }
  }
}

# drop a table
sub dropTable {
  &dropTables(@_);
}

# renames a table (new table is dropped)
sub renameTable {
  my($self, $old, $new) = @_;

  $self->dropTable($new);
  $self->executeStmt("alter table $old rename to $new");
}

# copies a table to another
sub copyTable {
  my($self, $from, $to) = @_;

  $self->dropTable($to);
  $self->executeStmt("create table $to as select * from $from");
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

# does a table have this column?
sub tableHasColumn {
  my($self, $table, $col) = @_;
  my($row) = $self->selectFirstAsScalar(<<"EOD");
SELECT table_name FROM all_tab_columns
WHERE  table_name=\'$table\'
AND    column_name=\'$col\'
EOD
  return $row;
}

# Size of a VARCHAR column
sub columnSize {
  my($self, $table, $col) = @_;
  $table = uc($table);
  $col = uc($col);
  my($size) = $self->selectFirstAsScalar(<<"EOD");
SELECT data_length FROM all_tab_columns
WHERE  table_name=\'$table\'
AND    column_name=\'$col\'
AND    data_type LIKE '\%CHAR\%'
EOD
  return $size;
}

#  Type of a column
sub columnType {
  my($self, $table, $col) = @_;
  $table = uc($table);
  $col = uc($col);
  my($type) = $self->selectFirstAsScalar(<<"EOD");
SELECT data_type FROM all_tab_columns
WHERE  table_name=\'$table\'
AND    column_name=\'$col\'
EOD
  return $type;
}

# does a column already have an index?
sub colHasIndex {
  my($self, $table, $col) = @_;
  my($row) = $self->selectFirstAsScalar(<<"EOD");
SELECT index_name FROM all_ind_columns
WHERE  table_name=\'$table\'
AND    column_name=\'$col\'
EOD
  return $row;
}

# creates an index on a column
sub createIndex {
  my($self, $table, $col) = @_;
  my($index) = join("_", "x", $table, $col);

  $self->connect;
  return if $self->colHasIndex($table, $col);
  $index = "x_" . substr($table, 0, 12) . "_$$_" . substr($col, 0, 10) if (length($index) > 30);
  $index = substr($index, 0, 30);
  $self->dropIndex($table, $col);
  $self->executeStmt("CREATE INDEX $index ON $table ($col)");
}

# drops an index (or all indexes) on a table/column
sub dropIndex {
  my($self, $table, $col) = @_;
  my($sql);

  $table = uc($table);
  $col = uc($col);

  if ($col) {
    $sql = "SELECT index_name FROM all_ind_columns WHERE table_name=\'$table\' AND column_name=\'$col\'";
  } else {
    $sql = "SELECT index_name FROM all_ind_columns WHERE table_name=\'$table\'";
  }
  foreach $i ($self->selectAllAsArray($sql)) {
    $self->executeStmt("DROP INDEX $i");
  }
}

# Does a given table exist?
sub tableExists {
  my($self, $table) = @_;

  return $self->selectFirstAsScalar("SELECT DISTINCT TABLE_NAME FROM ALL_TABLES WHERE table_name=" . $self->quote(uc($table)));
}

# Does a given index exist?
sub indexExists {
  my($self, $index) = @_;

  return $self->selectFirstAsScalar("SELECT DISTINCT index_name FROM ALL_INDEXES WHERE index_name=" . $self->quote(uc($index)));
}

sub currentDate {
  my($self) = @_;

  return $self->selectFirstAsScalar("SELECT SYSDATE FROM DUAL");
}

# compares two timestamps using the DB
sub compareDates {
  my($self, $d1, $d2) = @_;
  my($tmptable) = "tmp_$$_datecmp";
  my($x);

  $self->dropTable($tmptable);
  $self->createTable($tmptable, [{a=>'DATE'}, {b=>'DATE'}]);
  $self->executeStmt("insert into $tmptable(a,b) VALUES(" . $self->quote($d1) . "," . $self->quote($d2));

  $x = $self->selectFirstAsScalar("SELECT a from $tmptable where a<b");
  if ($x) {
    $self->dropTable($tmptable);
    return -1;
  }
  $x = $self->selectFirstAsScalar("SELECT a from $tmptable where a>b");
  if ($x) {
    $self->dropTable($tmptable);
    return 1;
  }
  $self->dropTable($tmptable);
  return 0;
}

# opens the DB connection if needed
sub connect {
  my($self) = @_;
  my($str);

  return 0 if (defined $self->{dbh});

  my($user) = $self->{user} || 'meow';
  my($password) = $self->{password} || GeneralUtils->getOraclePassword($user);
  my($db) = $self->{db} || Midsvcs->get('editing-db');

  if ($self->{failquietly}) {
    return 1 unless $user && $password && $db;
  } else {
    die "ERROR: missing authentication information in OracleIF: Cannot connect to database\n" unless $user && $password && $db;
  }

  my($dbh) = DBI->connect($DRIVER . ":" . $db, $user, $password, {RaiseError=>0, PrintError=>0});
  $self->{dbh} = $dbh;

  if ($self->{failquietly}) {
    return 1 if (($@ || $DBI::errstr) || !$self->{dbh});
  } else {
    die "ERROR: failed to connect to Oracle database\n" if (($@ || $DBI::errstr) || !$self->{dbh});
  }
  return 0;
}

# sets handle attributes, e.g., InactiveDestroy
sub setAttributes {
  my($self, $ref) = @_;

  return unless ref($ref) eq "HASH";
  foreach (keys %{ $ref }) {
    $self->{dbh}->{ $_ } = $ref->{$_};
  }
}

# a helper function
sub setInactiveDestroy {
  my($self) = @_;

  $self->setAttributes({InactiveDestroy=>1});
}

# disconnect from DB
sub disconnect {
  my($self) = @_;

  $self->{dbh}->{Warn} = 0;
  $self->{dbh}->disconnect if (defined($self->{dbh}));
}

# Escapes special characters in VARCHARs
sub quote {
  my($self, $varchar) = @_;
  return $self->{dbh}->quote($varchar);
}

# can be passed two references to arrays
# or a single reference to a hash
sub insertRow {
  my($self, $table, @refs) = @_;
  my(@a, @b);
  my($sql);

  if (ref($refs[0]) eq "HASH") {
    @a = keys %{ $refs[0] };
    @b = values %{ $refs[0] };
  } elsif (ref($refs[0]) eq "ARRAY") {
    @a = @{ $refs[0] };
    @b = @{ $refs[1] };
  } else {
    return;
  }
  $sql = "INSERT INTO $table (" . join(", ", @a) . ") VALUES (" . join(', ', @b) . ")";
  $self->executeStmt($sql);
  return;
}

# log to error file
sub errorlog {
  my($msg) = $@ || $DBI::errstr;
  return unless $msg;
  return unless $main::oracleLogFile && -e $main::oracleLogFile;
  my($d) = `/bin/date`;
  chomp($d);
  open(E, ">>$main::oracleLogFile") || return;
  print E $d, "|", $msg, "\n";
  close(E);
  return;
}

# Read authentication information from file
sub get_passwd {
    my($self, $passwdFile, $user) = @_;
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
