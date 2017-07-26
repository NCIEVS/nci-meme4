#!@PATH_TO_PERL@
#
# File:     batch.pl
# Author:   Brian Carlsen
#
# This script is used to batch insert core data.
#
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version Information
#
# 05/16/2003 4.2.0:  -view=false (memerun.pl)
# 03/19/2003 4.1.0:  Release
# 02/07/2003 4.1.0:  First version
#
$release = "4";
$version = "2.0";
$version_date = "05/18/2003";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set Defaults & Environment
#
unless ($ENV{MEME_HOME}) {
    $badvalue="MEME_HOME";
    $badargs=4;
}
$work_id = 0;
$new_value = "X";
$action_field = "X";
$source = "t";

%action_map = (
  "I" => 1, "D" => 1, "S" => 1, "T" => 1,
  "C" => 1, "A" => 1, "CF" => 1, "CA" => 1, "AC" => 1);
%type_map = (
  "C" => 1, "R" => 1, "A" => 1, "CS" => 1,
  "CR" => 1);
%source_map = (
  "i" => 1, "t" => 1, "f" => 1);

# Check options
@ARGS=();
while (@ARGV) {
  $arg = shift(@ARGV);
  if ($arg !~ /^-/) {
    push @ARGS, $arg;
    next;
   }

  if ($arg =~ /^-w=(.*)/) {
    $work_id = $1; }
  elsif ($arg =~ /^-w/) {
    $work_id = shift(@ARGV) }
  elsif ($arg =~ /^-n=(.*)/) {
    $new_value = $1; }
  elsif ($arg =~ /^-n/) {
    $new_value = shift(@ARGV) }
  elsif ($arg =~ /^-s=(.*)/) {
    $source = $1; }
  elsif ($arg =~ /^-s/) {
    $source = shift(@ARGV) }
  elsif ($arg =~ /^-f=(.*)/) {
    $action_field = $1; }
  elsif ($arg =~ /^-f/) {
    $action_field = shift(@ARGV) }
  elsif ($arg =~ /^-a=(.*)/) {
    $action = $1; }
  elsif ($arg =~ /^-a/) {
    $action = shift(@ARGV) }
  elsif ($arg =~ /^-t=(.*)/) {
    $core_data_type = $1; }
  elsif ($arg =~ /^-t/) {
    $core_data_type = shift(@ARGV) }
  elsif ($arg =~ /^-prop=(.*)/) {
    $prop = "-prop=$1"; }
  elsif ($arg =~ /^-prop/) {
    $prop = "-prop=".shift(@ARGV) }
  elsif ($arg =~ /^-host=(.*)/) {
    $host = "-host=$1"; }
  elsif ($arg =~ /^-host$/) {
    $host = "-host=".shift(@ARGV); }
  elsif ($arg =~ /^-port=(.*)/) {
    $port = "-port=$1"; }
  elsif ($arg =~ /^-port$/) {
    $port = "-port=".shift(@ARGV); }
  elsif ($arg eq "-version") {
    print "Version $version, $version_date ($version_authority).\n";
    exit(0);
   }
  elsif ($arg eq "-v") {
    print "$version\n";
    exit(0);
   }
  elsif ($arg eq "-help" || $arg eq "--help") {
    &PrintHelp;
    exit(0);
   }
  else {
    # invalid merge switches may
    # be valid switches for the class being called
    push @ARGS, $arg;
  }
}

#
# Get command line params
#
if (!$core_data_type) {
  $badargs = 5;
} elsif (!$type_map{$core_data_type}) {
  $badargs = 7;
  $badvalue = $core_data_type;
} elsif (!$action) {
  $badargs = 6;
} elsif (!$action_map{$action}) {
  $badargs = 8;
  $badvalue = $action;
} elsif (!$source_map{$source}) {
  $badargs = 9;
  $badvalue = $source;
} elsif (scalar(@ARGS) == 3) {
  ($table_name, $database, $authority) = @ARGS;
} else {
  $badargs = 3;
  $badvalue = scalar(@ARGS);
}

#
# Process errors
#
%errors = (1 => "Illegal switch: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set",
           5 => "Required parameter missing -t",
           6 => "Required parameter missing -a",
           7 => "Illegal -t value: $badvalue",
           8 => "Illegal -a value: $badvalue",
           9 => "Illegal -s value: $badvalue"
          );

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}

#
# If using a file or a list of ids, load table
#
use DBD::Oracle;
if ($source eq "f" || $source eq "i") {
  $file_name = $table_name;
  @ids = split /,/, $table_name;
  $table_name = "t_${$}_batch";

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $database`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$database", "$user", "$password") ||
    ((print "Error opening $database ($DBI::errstr).")
     &&  return);
 
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table',?); END;") ||
    die "Error preparing query 1 ($DBI::errstr).";
  $sh->execute($table_name) || 
    die "Error executing query 1 ($DBI::errstr).";
          
  $dbh->do(qq{
	CREATE TABLE $table_name AS SELECT concept_id as row_id FROM classes WHERE 1=0
    }) || die "Error executing create 1 ($DBI::errstr).";

  if ($source eq "f") {
    open (F,"$file_name") || die "Could not open file $file_name: $! $?\n";
    while (<F>) {
      chop;
      $dbh->do(qq{
		  INSERT INTO $table_name VALUES ($_)
		 }) || die "Error executing insert 1 ($DBI::errstr).";
    }
    close(F);
  } else {
    foreach $id (@ids) {
      $dbh->do(qq{
		  INSERT INTO $table_name VALUES (?)
		 }, undef, $id) || die "Error executing insert 1 ($DBI::errstr).";
    }
  }
}


#
# Make the call
#
#print "$java $option_line $class @args \n";
$ec = 0;
system ("$ENV{MEME_HOME}/bin/memerun.pl -view=false $prop $host $port gov.nih.nlm.meme.client.ActionClient $database do_batch $action $core_data_type $table_name $authority $work_id Y $new_value $action_field");
if ($?) { $ec=1; }

#
# Drop table if using $file
#
if ($file_name || scalar(@ids)>0) {
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table',?); END;") ||
    die "Error preparing query 1 ($DBI::errstr).";
  $sh->execute($table_name) || 
    die "Error executing query 1 ($DBI::errstr).";

  $dbh->disconnect;
}

exit($ec);

######################### LOCAL PROCEDURES #######################

sub PrintUsage {

	print qq{ This script has the following usage:
    batch.pl [-prop=<file>] [-host=<host>] [-port=<port>] [-w=<work_id>]
	     [-n=<new value>] [-f=<action field>]
             -a={I,D,T,S,A,C,CF,AC} -t={C,A,R,CS,CR} -s={t,f,i}
             <name/ids> <database> <authority>
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 This script is used to perform batch actions.

 Options:
       -prop=<file>:        Name properties file 
	                      Default is $ENV{MEME_HOME}/bin/meme.prop
       -host=<host>:        Name of the machine where server is running 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-host`,qq{       -port=<port>:        The port number that the server is listening on 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`,qq{       -w:                  Specify a work_id
       -n:                  Optional new value parameter (for T,S actions)
       -f:                  Optional action field parameter (for CF actions)
       -a:                  The action to perform (one of I,D,T,S,A,C,CF,CA)
       -t:                  The core data type (one of C,R,A,CS,CR)
       -s:                  The source of the ids, either a table (t), a file (f),
			    or a raw comma-separated list of ids (i)
       -v[ersion]:          Print version information.
       -[-]help:            On-line help

 Arguments:
       name/id:             A table name if -s=t, a file name if -s=f,
                            or a comma-separated list of ids if -s=i
       database:            The database
       authority:           The authority

 Version $version, $version_date ($version_authority)
};
}
