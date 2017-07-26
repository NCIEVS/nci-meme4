#!@PATH_TO_PERL@
#
# File:     merge.pl
# Author:   Brian Carlsen (2002)
#
# This script is used to run merge sets.
#
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version Information
#
# 03/28/2005 4.3.1: Don't open DB connection unless concept_id params are used
# 12/13/2004 4.3.0: Released
# 12/07/2004 4.2.1: -c concept_id1, concept_id2
# 05/16/2003 4.2.0: -view=false (memerun.pl)
# 03/19/2003 4.1.0: Release
# 02/07/2003 4.1.0: First version
#
$release = "4";
$version = "3.0";
$version_date = "12/13/2004";
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

# Check options
@ARGS=();
while (@ARGV) {
  $arg = shift(@ARGV);
  if ($arg !~ /^-/) {
    push @ARGS, $arg;
    next;
   }

  if ($arg =~ /^-prop=(.*)/) {
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
  elsif ($arg =~ /^-c=(.*)/) {
    ($concept_id1, $concept_id2)= split /,/, $1; 
    if (!$concept_id1 || !$concept_id2) {
       print "Concept ids must be entered in a pair separated by comma.\n";
       exit(0);
     }
    if ($concept_id1 eq $concept_id2) {
       print "Concept_id1 can not be the same as concept_id2.\n";
       exit(0);
     }
   }
  elsif ($arg =~ /^-c$/) {
     ($concept_id1, $concept_id2) = split /,/, shift(@ARGV); 
     if (!$concept_id1 || !$concept_id2) {
       print "Concept ids must be entered in a pair separated by comma.\n";
       exit(0);
     }
     if ($concept_id1 eq $concept_id2) {
       print "Concept_id1 can not be the same as concept_id2.\n";
       exit(0);
      }
   }
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
if (scalar(@ARGS) == 4) {
  ($merge_set, $authority, $work_id, $db) = @ARGS;
} else {
  $badargs = 3;
  $badvalue = scalar(@ARGS);
}

#
# Process errors
#
%errors = (1 => "Illegal switch: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set");

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}

if ($concept_id1) {

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

  $dbh->do(qq{
    TRUNCATE TABLE mom_merge_facts
  }) || die qq{Error truncating mom_merge_facts.\n};

  $dbh->do(qq{ INSERT INTO mom_merge_facts (merge_fact_id, atom_id_1,
  merge_level, atom_id_2, source, make_demotion, change_status,
  authority, merge_set, status, merge_order, molecule_id, work_id)
  SELECT 0, min(a.atom_id), 'SY', min(b.atom_id), 'MTH','N','Y',
  ?,?,'R',0,0,? 
  FROM classes a, classes b
  WHERE a.concept_id = ? 
  AND b.concept_id = ?
  }, undef, $authority, $merge_set, $work_id, $concept_id1, $concept_id2) || die qq{Can not merge by concept_ids: \n};

  $dbh->disconnect;
}

#
# Make the call
#
system ("$ENV{MEME_HOME}/bin/memerun.pl -view=false $prop $host $port gov.nih.nlm.meme.client.MergeEngineClient $merge_set $authority $work_id $db");
if ($?) { exit(1); }
exit(0);

######################### LOCAL PROCEDURES #######################

sub PrintUsage {

	print qq{ This script has the following usage:
    merge.pl [-prop=<file>] [-host=<host>] [-port=<port>] [-c=<id1,id2>]
	     <merge set> <authority> <work id> <database>
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 This script is use to process a merge set
 that has been loaded into the mom_merge_facts
 table in the specified database.

 Options:
       -prop=<file>:        Name properties file 
	                      Default is $ENV{MEME_HOME}/bin/meme.prop
       -host=<host>:        Name of the machine where server is running 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-host`,qq{       -port=<port>:        The port number that the server is listening on 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`,qq{       -v[ersion]:          Print version information.
       -c=<id1,id2>         Concept id pair to be merged
       -[-]help:            On-line help

 Arguments:
       merge set:           The merge set name
       authority:           The authority responsible for this merge set
       work id:             The work id
       database:            The database

 Version $version, $version_date ($version_authority)
};
}
