#!@PATH_TO_PERL@
#
# File:   dump_mid.pl
# Author:  BAC
#
# Remarks:  This script dumpes multiple tables at a time
#           using OS-level parallelization
#
#
# Changes:
# 07/05/2007 JFW (1-ENJU5): Restrict results from all_tables to tables owned by MEOW, MTH
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version Information
#
# 11/19/2004 (4.3.0):  Released, Field separator can be set using -f
# 06/06/2003 (4.2.0):  Supports CHAR fields > 255 chars. and
#                      dumps the schema and load scripts when -a
# 06/20/2003 (4.1.0):  Released
#
$release = "4";
$version = "3.0";
$version_date = "11/19/2004";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Check required environment
#
unless ($ENV{"MEME_HOME"}) {
  print "Required environment variable \$MEME_HOME is not set.";
  exit 1;
}

unless ($ENV{"MEME_HOME"}) {
  print "Required environment variable \$MEME_HOME is not set.";
  exit 1;
}

$| = 1;
$field_sep="|";
%dt_map = (
    "VARCHAR2" => "CHAR",
    "CHAR" => "CHAR",
    "DATE" => qq{DATE "DD-mon-YYYY HH24:MI:SS"},
    "NUMBER" => "DECIMAL EXTERNAL"
     );


#
# Parse arguments
#
$parallel = 4;
while (@ARGV) {
    $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg =~ /^-p=(\d*)/) {
      $parallel = $1; }
    elsif ($arg =~ /^-p$/) {
      $parallel = shift(@ARGV); } 
    elsif ($arg =~ /^-t=(\d*)/) {
      $table = $1; }
    elsif ($arg =~ /^-t$/) {
      $table = shift(@ARGV); } 
    elsif ($arg =~ /^-f=(\d*)/) {
      $field_sep = $1; }
    elsif ($arg =~ /^-f$/) {
      $field_sep = shift(@ARGV); } 
    elsif ($arg eq "-a") {
	$all=1; }
    elsif ($arg eq "-v") {
	$print_version="v"; }
    elsif ($arg eq "-help" || $arg eq "--help") {
	$print_help=1; }
    else {
	$badargs = 1;
	$badswitch = $arg;
    }
}

&PrintHelp && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

if (scalar(@ARGS) == 2) {
  ($db, $dir) = @ARGS;
} else {
  $badargs = 6;
  $badvalue = scalar(@ARGS);
}

if ($badargs) {
  %errors = (1 => "Illegal switch: $badswitch",
	     6 => "Bad number of arguments: $badopt"
	     );
  &PrintUsage;
  print "\n$errors{$badargs}\n";
  exit(0);
}

print "------------------------------------------------------------\n";
print "Starting dump_mid.pl ... ".scalar(localtime)."\n";
print "------------------------------------------------------------\n";
print "MID:   $db\n\n";
print "dir:   $dir\n\n";

use DBI;
use DBD::Oracle;

#
# open connection
#
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
chop($userpass);
($user,$password) = split /\//, $userpass;
$dbh = DBI->connect("dbi:Oracle:$db",$user,$password) ||
  die "Could not connect to $db: $! $?\n";

#
# Read from meme_tables
#
if ($all) {
  # If using -all, then 
  # dump the schema and load scripts also
  $dbh->do(qq{
    BEGIN
        MEME_SYSTEM.dump_mid(?);
    END;
  }, undef, $dir) || die qq{Could not dump schema and load scripts:
   Most likely you are trying to dump files
   to a directory not supported by the
   utl_file_dir parameter for this database.\n};
  
  $query = qq{   SELECT owner,table_name FROM all_tables 
   WHERE owner in ('MTH','MEOW')
    AND table_name NOT IN ('PLAN_TABLE','CHAINED_ROWS')
   };
} elsif ($table) {
  $query = qq{   SELECT owner, table_name FROM all_tables
   WHERE owner in ('MTH','MEOW')
   AND table_name = upper('$table') };
} else {
  $query = "SELECT 'MTH', table_name FROM meme_tables";
}
$sh = $dbh->prepare(qq{$query}) || die "Error preparing statement: $! $?\n";

$sh->execute ||
  die "Error executing ($query): $! $?\n";

open (F,">/tmp/t.$$") || die "could not open file /tmp/t.$$ for write: $! $?\n";
while (($schema,$table) = $sh->fetchrow_array) {
  print F "$schema|$table\n";
}
close(F);
$dbh->disconnect;

open (F,"/tmp/t.$$") || die "could not open file /tmp/t.$$ for read: $! $?\n";
@f = <F>;
close(F);
foreach $f (@f) {
  chomp($f);
  ($schema,$table) = split /\|/, $f;
  if ($pid = fork) {
    $np++; 
  } elsif (defined $pid) {
    #
    # This is the child process, Connect to the MID
    # enable the output buffer, dump the $table,
    # then flush the output buffer.
    #
    $dbh = DBI->connect("dbi:Oracle:$db",$user,$password) ||
      die "Could not connect to $db: $! $?\n";
    $dbh->{LongReadLen} = 4194302;
    # causing oracle 11g perf problems
    #$dbh->{RowCacheSize} = 1000000;
    $dbh->do(qq{ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS'});
    &DumpTable($schema,$table,$dir);
    print "    Dumped $schema.$table\n";
    $dbh->disconnect();
    exit(0);
  } else {
    die "Can't fork: $!\n";
  }

  if ($np == $parallel) {
    wait;
    $np--;
  }
}
close(F);

#
# Wait for all children to finish
#
$x = 0;
while ($x != -1) {
  $x = wait;
}

print "------------------------------------------------------------\n";
print "Finished dump_mid.pl ... ".scalar(localtime)."\n";
print "------------------------------------------------------------\n";

exit 0;

#####################################################################
# LOCAL PROCEDURES
#####################################################################

sub DumpTable {
  my($schema,$table,$dir) = @_;
  my($lc_table) = lc($table);
  my($uc_table) = uc($table);
  my($uc_schema) = uc($schema);
  my($sh,$ctl_header,$ctl_file,$col_list,$sqlldr_type);
  
  $ctl_header= qq{options (direct=true)
unrecoverable
load data
infile '$lc_table' "str X'7c0a'"
badfile '$lc_table.bad'
discardfile '$lc_table.dsc'
truncate
into table $lc_table
fields terminated by '$field_sep'
trailing nullcols
(
};
  $ctl_file = "";

  $sh = $dbh->prepare(qq{
    SELECT lower(column_name), data_type, data_length,
			 data_precision FROM all_tab_columns
    WHERE owner = ? AND table_name=? }) ||
      die "Could not prepare statement: $? $!\n";

  $sh->execute($uc_schema, $uc_table) ||
    die "Error looking up columns: $! $?\n";
  $col_list = "";
  $lob_col = 1;
  $found = 0;
  my($column_name, $data_type, $length, $precision, $found);
  while (($column_name,$data_type, $length, $precision) = $sh->fetchrow_array) {
    if ($found) {
      $ctl_file .= ",\n";
      $col_list .= ", ";
    }
    $found=1;
    if ($data_type =~ /LOB/) {
      $ctl_header= qq{load data
infile '$lc_table' "str X'7c0a'"
truncate
into table $lc_table
fields terminated by '$field_sep'
trailing nullcols
(
};
      $ctl_file .= (sprintf "%-40s", ($column_name));
      $ctl_file .= qq{LOBFILE(CONSTANT 'lob_${lc_table}_$lob_col.dat')
                                        terminated by '|\\n'
			                enclosed by "<lob>" and "</lob>"};

      $lsh = $dbh->prepare(qq{ SELECT $column_name FROM $schema.$table }) ||
          die "Could not prepare query ($column_name, $lc_table)\n";
      open (LOB,">$dir/lob_${lc_table}_$lob_col.dat") || 
	die "Could not open $dir/lob_${lc_table}_$lob_col.dat: $! $?\n";
      $lsh->execute || 
        die "Error reading lob data ($table, $column_name): $! $?\n";
      while (($lob)= $lsh->fetchrow_array) {
	print LOB "<lob>$lob</lob>|\n";
      }
      close(LOB);
      $lob_col++;
      
    } if ($dt_map{$data_type}) {
      $sqlldr_type = $dt_map{$data_type};
      if ($sqlldr_type eq "CHAR" && $length > 255) {
	$sqlldr_type = "CHAR($length)"; }
      $col_list .= "$column_name";
      $ctl_file .= sprintf "%-40s%s", ($column_name,$sqlldr_type);
    }
  }
  $ctl_file .= "\n)\n";

  unlink "$dir/$table.dat";
  open (DAT,">$dir/$lc_table.dat") ||
    die "Could not open $dir/$lc_table.dat: $! $?\n";

  $sh->finish;  
  $col_list =~ s/,\s*$//;
  $sh = $dbh->prepare(qq{
    SELECT $col_list FROM $schema.$table
   }) || die qq{Could not prepare statement ($!,$?): 
  SELECT $col_list FROM $schema.$table\n};

  $sh->execute;
  while (@f = $sh->fetchrow_array) {
    print DAT join("$field_sep",@f),"|\n";
  }
  close(DAT);

  unlink "$dir/$table.ctl";
  open (CTL,">$dir/$lc_table.ctl") ||
    die "Could not open $dir/$lc_table.ctl: $! $?\n";
  print CTL "$ctl_header$ctl_file";
  close(CTL);


}

sub PrintVersion {
    my($type) = @_;
    print "Release $release: version $version, $version_date ($version_authority).\n" 
	if $type eq "version";
    print "$version\n" if $type eq "v";
    return 1;
}

sub PrintUsage {

	print qq{ This script has the following usage:
    dump_mid.pl [-p <num processes>] [-a] [-f <field sep>] [-t <table>] <database> <dir>
};
}

sub PrintHelp {
    &PrintUsage;
    print qq{
 This script is used to copy out data and control files
 for tables in the MID so they can be loaded into a different
 database.  There are three modes: (1)  all tables listed in 
 meme_tables, (2) the entire MEOW and MTH schemas, (3) a single
 table.  (1) is the default behavior.

    Options:
     -p <#>:     Degree of parallelism
     -a:         Dump all MEOW,MTH tables
     -t <table>: Dump just the specified table
     -f <sep>:   Field separator
     -v[ersion]: Print version information.
     -[-]help:   On-line help
     
 };
    &PrintVersion("version");
    return 1;
}

