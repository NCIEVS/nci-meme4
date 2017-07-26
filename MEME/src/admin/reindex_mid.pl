#!@PATH_TO_PERL@
# File:   reindex_mid.pl
# Author:  BAC
#
# Remarks:  This script reindexes multiple tables at a time
#           using OS-level parallelization
#
#
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version Information
#
# 06/29/2005 (3.5.0):  Released, closes parent connection before
# opening child ones, solves a DBD::Oracle DESTROY problem
# 10/31/2003 (3.4.0):  Released
# 10/09/2003 (3.3.1):  Final fix for alter session set sort_area_size
# 10/01/2003 (3.3.0):  Use rebuild_flag->'Y'
# 09/30/2003 (3.2.0):  Set sort area size and parallel flags
# 04/16/2002 (3.1.0):  First version.
#
$release = "4";
$version = "5.0";
$version_date = "06/29/2005";
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

if (scalar(@ARGS) == 1) {
  ($db) = @ARGS;
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
print "Starting reindex_mid.pl ... ".scalar(localtime)."\n";
print "------------------------------------------------------------\n";
print "MID:   $db\n\n";

use DBI;
use DBD::Oracle;

#
# open connection
#
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
chop($userpass);
($user,$password) = split /\//, $userpass;
$dbh = DBI->connect("dbi:Oracle:$db",$user,$password) ||
  die "Could not connect to $db: $! $?\n";
#$dbh->{InactiveDestroy} = 1;

#
# Read from meme_tables
#
$sh = $dbh->prepare(q{
   SELECT table_name FROM meme_tables ORDER BY 1
    }) || die "Error preparing statement: $! $?\n";

$sh->execute;
while (($table) = $sh->fetchrow_array) {
  unshift @tables, $table;
}
$dbh->disconnect;

foreach $table (sort @tables) {

  if ($pid = fork) {
    $np++; 
  } elsif (defined $pid) {
    #
    # This is the child process, Connect to the MID
    # enable the output buffer, reindex the $table,
    # then flush the output buffer.
    #
    $dbh = DBI->connect("dbi:Oracle:$db",$user,$password) ||
      die "Could not connect to $db: $! $?\n";
    &EnableBuffer(100000);
    $dbh->do(qq{ALTER SESSION SET NLS_DATE_FORMAT='DD-mon-YYYY HH24:MI:SS'});
    $dbh->do(qq{ALTER SESSION SET sort_area_size=200000000});
    $dbh->do(qq{ALTER SESSION SET hash_area_size=200000000});
    &ReindexTable($table,$dbh);
    print "    Reindex $table\n";
    &FlushBuffer;
    print "    Done reindexing $table\n";
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

#
# Wait for all children to finish
#
$x = 0;
while ($x != -1) {
  $x = wait;
}

print "------------------------------------------------------------\n";
print "Finished reindex_mid.pl ... ".scalar(localtime)."\n";
print "------------------------------------------------------------\n";

exit 0;

#####################################################################
# LOCAL PROCEDURES
#####################################################################

sub ReindexTable {
  my($table,$dbh) = @_;
  my($sh);
  $sh = $dbh->do(qq{
    BEGIN meme_system.set_trace_on; meme_system.reindex(?,'N',''); END;}, undef, $table);
}

sub EnableBuffer {
  my($size) = @_;
  $size = 100000 unless $size;
  my($sh);
  $sh = $dbh->prepare(qq{
    BEGIN
      dbms_output.enable(?);
    END;});
  $sh->execute($size);
} # end EnableBuffer

sub FlushBuffer {
  #prepare stmt
  my($sh);
  $sh = $dbh->prepare(q{
    BEGIN
      dbms_output.get_line(:line,:status);
    END;});
  #bind parms
  $sh->bind_param_inout(":line", \$line, 256);
  $sh->bind_param_inout(":status", \$status,38);
  
  # flush buffer
  do {
    $sh->execute;
    $line =~ s/^(.{1,65}).*$/$1/;
    $line = "$line..." if length($line)== 65;
    print "        $line\n" if $line;
  } while (!$status);
  
} # end FlushBuffer

sub PrintVersion {
    my($type) = @_;
    print "Release $release: version $version, $version_date ($version_authority).\n" 
	if $type eq "version";
    print "$version\n" if $type eq "v";
    return 1;
}

sub PrintUsage {

	print qq{ This script has the following usage:
    reindex_mid.pl [-p <num processes>] <database>
};
}

sub PrintHelp {
    &PrintUsage;
    print qq{
 This script is reindexes all tables in the specified MID.

    Options:
     -p <#>:     Degree of parallelism
     -v[ersion]: Print version information.
     -[-]help:   On-line help
     
 };
    &PrintVersion("version");
    return 1;
}

