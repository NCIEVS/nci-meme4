#!@PATH_TO_64PERL@
#!@PATH_TO_PERL@

# Interfaces Perl to Oracle
# Author: suresh@nlm.nih.gov 8/2002

# Use this package to access an Oracle database
package OracleIF;

BEGIN
  {
	unshift @INC, "$ENV{ENV_HOME}/bin";
	require "env.pl";
	unshift @INC, "$ENV{EMS_HOME}/lib";
	unshift @INC, "$ENV{EMS_HOME}/bin";
  }

use DBI;
use DBD::Oracle;
use File::Basename;
use File::Copy;
use GeneralUtils;
use Midsvcs;
use Symbol;

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

  $ENV{ORACLE_BASE}="/export/home/oracle" unless $ENV{ORACLE_BASE};
  $ENV{ORACLE_HOME} = "/export/home/oracle" unless $ENV{ORACLE_HOME};
  $ENV{ORA_ENCRYPT_LOGIN} = 'TRUE' unless $ENV{ORA_ENCRYPT_LOGIN};

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

# for logging
sub sqllog {
  my($self, $msg) = @_;

  return unless $main::SQLLOGGING && $main::SQLLOGFILE;

  my($logfile) = $main::SQLLOGFILE;
  open(L, ">>$logfile") || die "Cannot open $logfile";
  print L "\n\n", "-" x 5, " SQL log for session ID: ", $main::SESSIONID, " ", "-" x 5, "\n";
  print L $msg, "\n";
  close(L);
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

# returns a row as a hash of col/values
sub selectFirstAsHash {
  my($self, $stmt, $cols) = @_;
  my(@values) = $self->selectFirstAsArray($stmt);
  my($n) = 0;
  my(%h);

  while ($n<@$cols) {
    $h{$cols->[$n]} = $values[$n];
    $n++;
  }
  return \%h;
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

  $self->sqllog($stmt);

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
    die ($@ || $DBI::errstr) if ($@ || $DBI::errstr);
    unless ($self->{sth}->{$stmt}->execute) {
      die ($@ || $DBI::errstr);
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

# Loads data (SLOWLY!) into a table (fields must be | separated)
# The colspec is a ref to an array of colnames and their types
# e.g., ['concept_id'], {bin_name=>'varchar'}]
sub file2table {
  my($self, $file, $table, $colspec) = @_;
  my($sql);
  my($fd);
  my(@fields, $i, %row, $colname, $coltype, %coltype, @cols);

  if (ref $file ne "GLOB") {
    use Symbol;
    $fd = gensym;
    open($fd, $file) || die "Cannot open $file to load data into table: $table\n";
  } else {
    $fd = $file;
  }

  foreach $colname (@$colspec) {

    if (ref($colname)) {
      if (ref($colname) eq "HASH") {
		my(@x) = keys %{ $colname };
		$coltype{uc($x[0])} = uc($colname->{$x[0]});
		push @cols, uc($x[0]);
      }
    } else {
      push @cols, uc($colname);
      $coltype{uc($colname)} = 'INTEGER';
    }
  }

  while (<$fd>) {
    chomp;
    @fields = split /\|/, $_;
    %row = ();
    for ($i=0; $i<@cols; $i++) {
      $colname = $cols[$i];
      $coltype = $coltype{$cols[$i]};
      $row{$colname} = ($coltype =~ /^int/i ? $fields[$i] :
						($coltype =~ /^date/i ? ("TO_DATE(" . $self->quote($fields[$i]) . ")") :
						 $self->quote($fields[$i])));
    }
    $self->insertRow($table, \%row);
  }
  close($fd) if ref $file ne "GLOB";

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
  my($self, $file, $table, $colspec) = @_;
  my($datfile, $ctlfile);
  my($sqlldr) = $ENV{ORACLE_HOME} . "/bin/sqlldr";
  my($r, @t, $c, $t);

  $self->dropTable($table);
  $self->createTable($table, $colspec);

  if ($file =~ /\.dat$/) {
    $datfile = $file;
  } else {
    $datfile = "/tmp/" . basename($file) . "_$$" . ".dat";
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
# only integer output can be parsed
sub sqlplus_integer_output {
  my($self, $scriptfile, $output) = @_;
  my($sqlplus) = $sqlplus || ($ENV{'ORACLE_HOME'} . "/bin/sqlplus");
  my($authstr) = $self->{user} . "/" . $self->{password} . "\@" . $self->{db};

  die "Cannot find sqlplus\n" unless -e $sqlplus && -x $sqlplus;

  if (ref($output) eq "GLOB") {
    $fd = $output;
  } else {

    use Symbol;

    $fd = gensym;
    open($fd, ">$output") || die "ERROR: Cannot open $output";
  }
  open(S, "$sqlplus -S \"$authstr\" < $scriptfile|") || die "ERROR: Cannot run sqlplus";
  while (<S>) {
    chomp;
    s/^\s*//;
    s/\s*$//;
    next if /^\s*$/;
    next if /[^\d\s]/;
    s/\s+/ /g;
    s/ /\|/g;
    print $fd $_, "\n";
  }
  close(S);
  close($fd) unless ref($output);
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

  $self->sqllog($stmt);

  $self->connect;
  print STDERR $stmt, "\n" if $self->{debug};
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
    print STDERR "ERROR in: $stmt\n" if $self->{debug};
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
  $self->executeStmt("CREATE TABLE $table (" . join(', ', @c) . ") NOLOGGING");
}

# creates a table with subselect
sub createTableAsSelect {
  my($self, $table, $selectstmt) = @_;

  $self->executeStmt("CREATE TABLE $table NOLOGGING AS $selectstmt");
}

# does a table have this column?
sub tableHasColumn {
  my($self, $table, $col) = @_;
  my($qt) = $self->quote(uc($table));
  my($qc) = $self->quote(uc($col));
  my($row) = $self->selectFirstAsScalar(<<"EOD");
SELECT table_name FROM all_tab_columns
WHERE  table_name=$qt
AND    column_name=$qc
EOD
  return $row;
}

# add a column to a table
sub addColumn {
  my($self, $table, $colname, $coltype) = @_;
  my($sql);

  $sql = <<"EOD";
ALTER TABLE $table ADD $colname $coltype
EOD
  $self->executeStmt($sql);
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
  my($self, $table, $col, $index) = @_;

  $self->connect;
  #  return if $self->colHasIndex($table, $col);

  unless ($index) {
    $index = sprintf("x_%s_%d_%02d", $table, $$, rand(100));
    if (length($index)>30) {
      $index = sprintf("x_%s_%d_%02d", substr(GeneralUtils->str2random($table), 0, 20), $$, rand(100))
    }
	#    $index = "x_" . substr($table, 0, 12) . "_$$_" . substr($col, 0, 10) if (length($index) > 30);
    $index = substr($index, 0, 30);
  }
  $self->dropIndex($table, $index);
  if (ref($col)) {
    $self->executeStmt("CREATE INDEX $index ON $table (" . join(",", @$col) . ")");
  } else {
    $self->executeStmt("CREATE INDEX $index ON $table (" . $col . ")");
  }
  $self->analyzeStats($table);
}

# drops an index on a table/column
sub dropIndex {
  my($self, $table, $index) = @_;
  my($sql);

  $table = uc($table);
  $index = uc($index);

  $sql = "SELECT index_name FROM all_ind_columns WHERE table_name=" . $self->quote($table) . " AND index_name=" . $self->quote($index);
  if ($self->selectFirstAsScalar($sql)) {
    $self->executeStmt("DROP INDEX $index");
  }
}

# A simple way to get a temporary table name
sub tempTable {
  my($class, $prefix) = @_;
  my($table);
  my($n) = 1;

  $prefix = "EMSTMP" unless $prefix;
  while (1) {
    $table = sprintf("%s_%d_%d", $prefix, $$, $n++);
    last unless $class->tableExists($table);
  }
  return $table;
}

# Analyses the statistics on a table
sub analyzeStats {
  my($self, $table) = @_;

  $self->executeStmt("ANALYZE TABLE $table COMPUTE STATISTICS");
}

# Does a given table exist?
sub tableExists {
  my($self, $table) = @_;
  #  my($sql) = "SELECT DISTINCT TABLE_NAME FROM ALL_TABLES WHERE table_name=" . $self->quote(uc($table));
  my($sql) = "SELECT DISTINCT object_name FROM ALL_OBJECTS WHERE object_name=" . $self->quote(uc($table)) . " and (object_type='TABLE' or object_type='VIEW')";
  return $self->selectFirstAsScalar($sql);
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

sub now {
  my($self) = @_;
  return $self->currentDate;
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
  my($password) = $self->{password} || GeneralUtils->getOraclePassword($user,$db);
  my($db) = $self->{db} || Midsvcs->get('editing-db');
  
  print STDERR "$user $password\n";

  if ($self->{failquietly}) {
	print STDERR $DBI::errstr;
    return 1 unless $user && $password && $db;
  } else {
    die "ERROR: missing authentication information in OracleIF: Cannot connect to database\n" unless $user && $password && $db;
  }

  my($dbh) = DBI->connect($DRIVER . ":" . $db, $user, $password, {RaiseError=>0, PrintError=>0});
  $self->{dbh} = $dbh;

  if ($self->{failquietly}) {
	print STDERR $DBI::errstr;
    return 1 if (($@ || $DBI::errstr) || !$self->{dbh});
  } else {
    die "ERROR: failed to connect to Oracle database $db\n" if (($@ || $DBI::errstr) || !$self->{dbh});
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

# quotes a variable
sub quote {
  my($self, $x, $type) = @_;

  $type = "varchar" unless $type;
  if ($type =~ /char/i) {
    return $self->{dbh}->quote($x);
  } elsif ($type =~ /integer/ || $type =~ /number/i) {
    return $x;
  } elsif ($type =~ /date/i) {
    return "TO_DATE(" . $self->{dbh}->quote($x) . ")";
  }
}

# Inserts a row into the database given a hash representing the row
# looks up the data types from ALL_TAB_COLUMNS
sub insertRow {
  my($self, $table, $row) = @_;
  my($sql, $ref);
  my(%r, @cols, $col, @values);

  $sql = "select column_name from ALL_TAB_COLUMNS where table_name=" . $self->quote(uc($table));
  @cols = $self->selectAllAsArray($sql);

  %r = map { lc($_) => $row->{$_} } keys %$row;
  @values = map { $r{lc($_)} } @cols;

  $sql = "INSERT INTO $table (" . join(", ", @cols) . ") VALUES (" . join(', ', @values) . ")";
  $self->executeStmt($sql);
  return;
}

# updates the contents of a row in the database given a hash
# representing all or some columns of the row
# $key and $value identify the row to be updated
sub updateRow {
  my($self, $table, $key, $value, $row) = @_;
  my($sql, $ref);
  my(%type, @cols, $col, @x);
  my($keyval);

  @cols = keys %$row;
  foreach $col (@cols) {
    push @x, $col . "=" . $row->{$col};
  }
  $sql = "UPDATE $table set " . join(", ", @x) . " where $key=$value";
  $self->executeStmt($sql);
  return;
}

# returns the DB
sub getDB {
  my($self) = @_;
  return $self->{db};
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
