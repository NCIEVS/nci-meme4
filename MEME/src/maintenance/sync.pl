#!@PATH_TO_PERL@
#
# File:     sync.pl
# Author:   Brian Carlsen 
#
# Import and export actions
#
# CHANGES
# 01/11/2006 BAC (1-739BX): fixed -host, -port params.
# 12/13/2005 BAC (1-739BX): created
#
$release = "4";
$version = "1.0";
$version_date = "12/13/2005";
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

#
# Check options
#
&HandleArguments;

#
# Set host/port defaults from MIDSVCS server
#
unless ($host) {
  $host =`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-host`;
  chop($host);
}

unless ($port) {
  $port =`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`;
  chop($port);
}

#
# Parse parameters
# There should be zero parameters
if (scalar(@ARGS) != 0) {
    $badargs = 3;
    $badvalue = scalar(@ARGS);
}

unless ($db) {
  $badargs = 2;
  $badvalue = "-d";
}

unless ($mode) {
  $badargs = 2;
  $badvalue = "-imp or -exp";
}

unless ($type) {
  $badargs = 2;
  $badvalue = "-id or -date";
}

unless ($start) {
  $badargs = 1;
  $badvalue = "-id without value";
}

unless ($dir) {
  $badargs = 1;
  if ($mode eq "imp") {
    $badvalue = "-imp without value";
  } elsif ($mode eq "exp") {
    $badvalue = "-exp without value";
  } 

}

%errors = (1 => "Illegal switch: $badvalue",
	   2 => "Missing required switch: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set"
	   );

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}

if ($debug) {
  &PrintDebug;
  exit 0;
 }

print "-----------------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-----------------------------------------------------------\n";

# set variables
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
($user,$password) = split /\//, $userpass;
chop($password);
print "Database:     $db\n";
print "User:         $user\n";
print "Host:         $host\n";
print "Port:         $port\n";
print "Mode:         $mode\n";
print "Directory:    $dir\n";
print "Clean:        true\n" if $clean;
print "Force:        true\n" if $force;
if ($mode eq "imp") {
  print "Type:         $type\n";
  print "Start:        $start\n";
  print "End:          $end\n" if $end;
}


#
# HANDLE EXPORT Case
#
if ($mode eq "exp") {
  use DBI;
  use DBD::Oracle;
  $dbh = DBI->connect("dbi:Oracle:$db",$user,$password) ||
    die "Could not connect to $db: $! $?\n";
  $dbh->{LongReadLen} = 4194302;

  if ($type eq "id") {
    $condition = "where action_id >= $start";
    $condition .= " and action_id <= $end" if $end;
  }

  if ($type eq "date") {
    $condition = "where timestamp >= '$start'";
    $condition .= " and timestamp <= '$end'" if $end;
  }

  $query = qq{
	 select action_id, transaction_id, work_id, undo_action_id,
	   elapsed_time, action, synchronize, authority, timestamp,
           document
	 from action_log $condition};
  $sh = $dbh->prepare($query) 
    || die "Error preparing to select from action_log ($DBI::errstr).\n";
  
  $sh->execute() || die "Error selecting from action_log ($DBI::errstr).\n";

  $ct = 0;
  while (($action_id, $transaction_id, $work_id, $undo_action_id,
	$elapsed_time, $action, $synchronize, $authority, $timestamp,
	$document)=$sh->fetchrow_array) {
    $ct++;
    if ($ct%100 == 1) { print "    ...exporting...$ct\n"; }
    $first = "${db}.${action_id}.txt" unless $first;
    open (F,">$dir/${db}.${action_id}.txt") ||
      die "Could not open action file $dir/${db}.${action_id}.txt: $! $?\n";
    print F $document;
    close (F);     
    $last = "${db}.${action_id}.txt";
  }
  print "    Finished exporting: $ct actions\n";
  print "      first: $first\n";
  print "      last:  $last\n";

  #
  # Handle $clean
  #
  if ($clean) {
    $rc = $dbh->do(qq{
      DELETE FROM action_log $condition })
    || die "Error removing action_log entries ($DBI::errstr).\n";
    print "      removed db rows ($rc)\n";
  }

  $dbh->disconnect;

}

#
# HANDLE IMPORT
#
elsif ($mode eq "imp") {

  if ($force) { $force = "true"; } else { $force = "false"; }
  if ($clean) { $clean = "true"; } else { $clean = "false"; }
  system ("$ENV{MEME_HOME}/bin/memerun.pl -view=false -host $host -port $port gov.nih.nlm.meme.client.SynchronizeActionsClient synchronizeActions $db $dir $force $clean");
if ($?) { exit(1); }

}

print "-----------------------------------------------------------\n";
print "Finished ...",scalar(localtime),"\n";
print "-----------------------------------------------------------\n";

exit 0;

######################### LOCAL PROCEDURES #######################

#
# Debugging purposes, print out options
#
sub PrintDebug {
print qq{
       db: $db
       host: $host
       port: $port
       mode: $mode
       type: $type
       start: $start
       end: $end
       force: $force
       clean: clean
};

}
sub HandleArguments {

@ARGS=();
while (@ARGV) {
  $arg = shift(@ARGV);
  if ($arg !~ /^-/) {
    push @ARGS, $arg;
    next;
   }
  
  if ($arg eq "-version") {
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
  elsif ($arg eq "-debug") {
    $debug =1; }

  elsif ($arg =~ /^-force$/) {
    $force = 1; }
  elsif ($arg =~ /^-clean$/) {
    $clean = 1; }
  elsif ($arg =~ /^-imp=(.*)$/) {
    $mode = "imp";
    $dir = $1;  }
  elsif ($arg =~ /^-imp$/) {
    $mode = "imp";
    $dir = shift(@ARGV);  }
  elsif ($arg =~ /^-exp=(.*)$/) {
    $mode = "exp";
    $dir = $1;  }
  elsif ($arg =~ /^-exp$/) {
    $mode = "exp";
    $dir = shift(@ARGV);  }
  elsif ($arg =~ /^-id=(.*)$/) {
    ($start,$end) = split /,/, $1;
    $type = "id"; }
  elsif ($arg =~ /^-id$/) {
    $arg = shift(@ARGV);  
    ($start, $end) = split /,/, $arg; 
    $type = "id"; }
  elsif ($arg =~ /^-date=(.*)$/) {
    ($start,$end) = split /,/, $1;
    $type = "date"; }
  elsif ($arg =~ /^-date$/) {
    $arg = shift(@ARGV);  
    ($start, $end) = split /,/, $arg; 
    $type = "date"; }
  elsif ($arg =~ /^-d=(.*)$/) {
    $db = $1;  }
  elsif ($arg =~ /^-d$/) {
    $db = shift(@ARGV);  }
  elsif ($arg =~ /^-host=(.*)$/) {
    $host = $1;  }
  elsif ($arg =~ /^-host$/) {
    $host = shift(@ARGV);  }
  elsif ($arg =~ /^-port=(.*)$/) {
    $port = $1;  }
  elsif ($arg =~ /^-port$/) {
    $port = shift(@ARGV);  }
  else {
    $badargs = 1;
    $badvalue = $arg;
   }
 }
}

sub UnAndReEncode {
  my($arg) = @_;
  $arg =~ s/&amp;/&/g;
  $arg =~ s/&lt;/</g;
  $arg =~ s/&gt;/>/g;
  $arg =~ s/&/&amp;/g;
  $arg =~ s/</&lt;/g;
  $arg =~ s/>/&gt;/g;
  return $arg;
}


sub PrintUsage {

	print qq{ This script has the following usage:
    sync.pl     [-(imp|exp) <dir>]
                [-d <database/service>]
	        [-host <host>]
	        [-port <port>]
		[-id <start_id,end_id>]
		[-date <dd-mon-yyyy,dd-mon-yyyy>]
		[-clean] [-force]
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 Options:
       -imp <dir>   : Import actions from <dir>
       -exp <dir>   : Export actions to <dir>
       -d <db>      : Specify a database
       -host=<host> : Machine where the application server is running
       -port=<port> : Port on which server is listening
       -id <id,id>  : Specify start and end id range (inclusive). Leave
		      end id off to run through to end. For "-exp" mode only
       -date <sd,ed>: Specify start and end date range (inclusive). Leave
		      end date off to run through to end.  For "-exp" mode only
       -force:        Indicate that if import actions fail, to skip over
		      failed actions.  Otherwise, synchronization will stop.
       -clean:        In import mode, it cleans up action files as they
		      are processed, in export mode it removes actions from
                      the log as they are exported.
       -v[ersion]:    Print version information.
       -[-]help:      On-line help

 Version $version, $version_date ($version_authority)
};
}



