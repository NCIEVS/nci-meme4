#!@PATH_TO_PERL@
#
# File:     undo.pl
# Author:   Brian Carlsen
#
# This script is used to undo action.
#
# Version Information
#
# 01/14/2005 4.2.0: Supports work ids, forcing of actions
# 08/01/2003 4.1.0: First version
#
$release = "4";
$version = "2.0";
$version_date = "01/14/2005";
$version_authority="RBE";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set Defaults & Environment
#
unless ($ENV{MEME_HOME}) {
    $badvalue="MEME_HOME";
    $badargs=4;
}
$force = "false";
$work_id = 0;

#
# Check options
#
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
  elsif ($arg =~ /^-w=(.*)/) {
    $work_id = "$1"; }
  elsif ($arg =~ /^-w$/) {
    $work_id = shift(@ARGV); }
  elsif ($arg =~ /^-f$/) {
    $force = "true"; }
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
if (scalar(@ARGS) == 3) {
  ($database, $transaction_id, $authority) = @ARGS;
} else {
  $badargs = 3;
  $badvalue = scalar(@ARGS);
}

#
# Process errors
#
%errors = (1 => "Illegal switch: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set"
          );

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}

#
# Make the call
#
$ec = 0;
system ("$ENV{MEME_HOME}/bin/memerun.pl -view=false $prop $host $port gov.nih.nlm.meme.client.ActionClient $database undo_batch $transaction_id $work_id $force $authority");
if ($?) { $ec=1; }

exit($ec);

######################### LOCAL PROCEDURES #######################

sub PrintUsage {

	print qq{ This script has the following usage:
    undo.pl [-prop=<file>] [-host=<host>] [-port=<port>] [-w <work_id>] [-f]
             <database> <transaction_id> <authority>
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 This script is used to perform undo actions.

 Options:
       -prop=<file>:        Name properties file 
	                      Default is $ENV{MEME_HOME}/bin/meme.prop
       -host=<host>:        Name of the machine where server is running 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-host`,qq{       -port=<port>:        The port number that the server is listening on 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`,qq{
       -w <work_id>:        Work identifier
       -f          :        Force the action
       -v[ersion]  :        Print version information.
       -[-]help:            On-line help

 Arguments:
       database:            The database
       transaction_id:      transaction id

 Version $version, $version_date ($version_authority)
};
}
