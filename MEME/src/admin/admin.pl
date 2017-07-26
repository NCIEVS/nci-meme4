#!@PATH_TO_PERL@
#
# File:     admin.pl
# Author:   Brian Carlsen 
#
# Simple client to print the server log.
#
# Version Information
# 02/20/2004 4.7.0: Support version 41 MEME4 (and beyond)
# 08/19/2003 4.6.0: Support refresh_db
# 06/06/2003 4.5.0: Support -kill
# 05/22/2003 4.4.1: Support database switch
# 05/12/2003 4.4.0: Released
# 05/06/2003 4.3.1: Show server version, show session logs
# 03/19/2003 4.3.0: Relased to NLM
#                    - Fixed exception handling for new document type
# 09/??/2002 4.2.1: Supports is_editing_enabled, is_integrity_enabled
# 08/28/2002 4.2.0: Supports additional services, call with -help to see
# 03/07/2002 4.1.0: First Version - Released to NLM
#
$release = "4";
$version = "7.0";
$version_date = "02/20/2004";
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
($head, $tail) = (0, 0);
($length, $ht_param, $id) = (2,"","1");

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
  
  if ($arg eq "-version") {
    print "Version $version, $version_date ($version_authority).\n";
    exit(0);
  } elsif ($arg eq "-v") {
    print "$version\n";
    exit(0);
  } elsif ($arg eq "-help" || $arg eq "--help") {
    &PrintHelp;
    exit(0);
  } elsif ($arg =~ /^-id=(.*)$/) {
    $id = $1;  
  } elsif ($arg =~ /^-id$/) {
    $id = shift(@ARGV);  
  } elsif ($arg =~ /^-host=(.*)$/) {
    $host = $1;  
  } elsif ($arg =~ /^-host$/) {
    $host = shift(@ARGV);  
  } elsif ($arg =~ /^-port=(.*)$/) {
    $port = $1;  
  } elsif ($arg =~ /^-port$/) {
    $port = shift(@ARGV);  
  } elsif ($arg =~ /^-s=(.*)$/) {
    $function = $1;  
  } elsif ($arg =~ /^-s$/) {
    $function = shift(@ARGV);  
  } elsif ($arg =~ /^-d=(.*)$/) {
    $db = $1;  
  } elsif ($arg =~ /^-d$/) {
    $db = shift(@ARGV);  
  } elsif ($arg =~ /^-from=(.*)$/) {
    $from_db = $1;  
  } elsif ($arg =~ /^-from$/) {
    $from_db = shift(@ARGV);  
  } elsif ($arg =~ /^-to=(.*)$/) {
    $to_db = $1;  
  } elsif ($arg =~ /^-to$/) {
    $to_db = shift(@ARGV);  
  } elsif ($arg =~ /^-head=(.*)$/ ||
	   $arg =~ /^-head$/) {
    $head = $1 if $1; $head = shift(@ARGV) unless $1;
    $tail=0; $length = 3;
    $ht_param = qq{
        <Object name="2" id="5" class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="6">head</Object>
          <Object name="value" id="7" class="java.lang.Integer">$head</Object>
        </Object>};
  } elsif ($arg =~ /^-tail=(.*)$/ ||
	   $arg =~ /^-tail$/) {
    $tail = $1 if $1; $tail = shift(@ARGV) unless $1;
    $head=0; $length = 3;
    $ht_param = qq{
        <Object name="2" id="5" class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="6">tail</Object>
          <Object name="value" id="7" class="java.lang.Integer">$tail</Object>
        </Object>};
  } else {
    $badargs = 1;
    $badvalue = $arg;
  }
}

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
# Indicate whether or not this function requires a data source
#
%function_ds_map = (
  "log" => "N", 
  "server_log" => "N", 
  "session_log" => "N", 
  "session_log_not_seen" => "N", 
  "session_progress" => "N", 
  "transaction_log" => "Y", 
  "stats" => "N", 
  "dummy" => "Y", 
  "refresh_caches" => "Y", 
  "refresh_db" => "Y", 
  "shutdown" => "N", 
  "kill" => "N", 
  "version" => "N", 
  "enable_integrity" => "Y", 
  "disable_integrity" => "Y", 
  "is_integrity_enabled" => "Y", 
  "enable_editing" => "Y", 
  "disable_editing" => "Y", 
  "sync" => "Y", 
  "is_editing_enabled" => "Y", 
  "enable_validate_atomic_action" => "Y", 
  "disable_validate_atomic_action" => "Y", 
  "enable_validate_molecular_action" => "Y", 
  "disable_validate_molecular_action" => "Y");

if (!$function_ds_map{$function}) {
  $badargs = 2;
  $badvalue = $function;
}

#
# There are no parameters
#
if (scalar(@ARGS) != 0) {
    $badargs = 3;
    $badvalue = scalar(@ARGS);
}

%errors = (1 => "Illegal switch: $badvalue",
	   2 => "Illegal service: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set");

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}

#
# Connect to server, send Request
#
require 5.003;
use Socket;
$proto = getprotobyname("tcp");

#
# Do once if using concept_id/cui, do many times if using file name
#
if ($use_file_name) {
    open (F,"$file_name") || die "Could not open file $file_name: $!\n";
}


socket(SOCK, PF_INET, SOCK_STREAM, $proto);
$sin = sockaddr_in($port, inet_aton($host));
$x = connect(SOCK, $sin);
unless ($x) {
  die qq{
Connection to MEME Application Server refused
The most likely reason is that the server is
not currently running on $host at port $port.
};
}
select(SOCK);
$| = 1;
select(STDOUT);


if ($function_ds_map{$function} eq "N") {
  $service = "";
} else {
  $service= qq{<DataSource service="$db" />};
}

#
# Deal with session parameters
#
if ($function =~ /session_/) {
  $session = qq{<Session id="$id" />};
  die "You must use the -id switch with -s $function\n" unless $id;
} else {
  $session = qq{<Session nosession="true" />};
}

if ($function eq "kill") {
  $ht_param = qq{
        <Object name="2" id="5" class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="false">
          <Object name="name" id="6">kill</Object>
          <Object name="value" id="7" class="java.lang.String">true</Object>
        </Object>};
  $length = 3;
  $function = "shutdown";
}

if ($function eq "sync") {
  $ht_param = qq{
        <Object name="2" id="5" class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="false">
          <Object name="name" id="6">from_mid</Object>
          <Object name="value" id="7" class="java.lang.String">$from_db</Object>
        </Object>
        <Object name="3" id="8" class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="false">
          <Object name="name" id="9">to_mid</Object>
          <Object name="value" id="10" class="java.lang.String">$to_db</Object>
        </Object>};
  $length = 4;
  $function = "synchronize_actions";
}

#<!DOCTYPE MASRequest SYSTEM "MASRequest.dtd">

print SOCK qq{POST / HTTP/1.1

<MASRequest>
  <ConnectionInformation>
    $session
    $service
  </ConnectionInformation>
  <ServiceParameters>
    <Service>AdminService</Service>
    <Parameter>
      <Object name="" id="1" class="gov.nih.nlm.meme.common.Parameter" length="$length">
        <Object name="0" id="2" class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="3">function</Object>
          <Object name="value" id="4" class="java.lang.String" >$function</Object>
        </Object>
        <Object name="1" id="8" class="gov.nih.nlm.meme.common.Parameter\$Default" primitive="true">
          <Object name="name" id="9">transaction_id</Object>
          <Object name="value" id="10" class="java.lang.Integer">$id</Object>
        </Object>$ht_param

      </Object>
    </Parameter>
  </ServiceParameters>
  <ClientResponse>
  </ClientResponse>
</MASRequest>
};


$ret_val = $function;
$ret_val = "log" if $function =~ /log/;
$ret_val = "progress" if $function eq "session_progress";

#
# Process response
#
$found=0;
$message = "";
$ct=0;
while (<SOCK>) {
  #print;
  if (/<Object name="name" id="3">$ret_val<\/Object>/ ||
      /<Var name="name" value="$ret_val"/) {
    $_ = <SOCK>;
    #print;
    if (/.*name="value".*class="java.lang.Boolean".*\>/) {
      if (/value="true"/) {
        print "Editing is enabled.\n" if $function eq "is_editing_enabled";
        print "Integrity system is enabled.\n" if $function eq "is_integrity_enabled";
      }
      else {
        print "Editing is disabled.\n" if $function eq "is_editing_enabled";
        print "Integrity system is disabled.\n" if $function eq "is_integrity_enabled";
      }
    }
    print "$1\n" if (s/.*name="value".*class="java.lang.Integer">(.*)<".*/$1/);
    print if s/.*name="value".*"><!\[CDATA\[(.*)\]\]><\/Object>/$1/i;
    print if s/.*name="value".*">(.*)<\/Object>/$1/i;
    $found = 1 if s/.*name="value".*DATA\[(.*)/$1/;		   
  }
  $found = 0 if ($found && /<\/Object\>/);
 
  # Exception handling
  if (/"details"/) {
    $inhashmap = 1;
  }
  if ($inhashmap) {
    if (/.*name="key".*DATA\[(.*)\]\]\>\<\/Object\>/ ||
	/.*name="key".*\>(.*)\<\/Object\>/) {
      $key = $1;
    }
    if (s/.*name="value".*DATA\[(.*)\]\]\>\<\/Object\>/$1/ ||
	s/.*name="value".*\>(.*)\<\/Object\>/$1/) {
      $details{$key}="$1";
    }
  }
  if (s/.*name="message".*DATA\[(.*)\]\]\>\<\/Object\>/$1/ ||
      s/.*name="message".*\>(.*)\<\/Object\>/$1/ ||
      /message="(.*)"/) {
      $message = $1;
  }

  print if $found;
 }
close(SOCK);

if ($message) {
  print "Exception: $message\n";
  print "\t{";
  foreach $key (sort keys %details) {
    print ",\n\t " unless ($ct++ == 0);
    print "$key => $details{$key}";
  }
  print "}\n";
}

exit 0;


######################### LOCAL PROCEDURES #######################

sub PrintUsage {

	print qq{ This script has the following usage:
   admin.pl -s <service> [-head <#>] [-tail <#>] [-host (host)] [-port (port)]
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 Options:
       -s <service>:  Admin service, the available services are:
                       server_log:          View the server log
                       session_log:         View a session log
                       session_log_not_seen: View the portion of a session log
		                            generated since last checked
                       transaction_log:     View molecular action log
                       session_progress:    View a session progress
		       stats:               View server statistics
		       dummy:               Test server connection
		       shutdown:            Shut server down
	               kill:                Kill server down
   	               version:             Show server version
		       refresh_caches:      Refresh server caches
		       refresh_db:          Refresh default mid service
		       enable_integrity:    (Globally) enable integrity system
		       disable_integrity:   (Globally) disable integrity system
		       is_integrity_enabled:Determine if integrity system is enable or not
		       enable_editing:      Enable editing system
		       disable_editing:     Disable editing system
		       is_editing_enabled:  Determine if editing is enable or not
		       enable_validate_atomic_action:    Enable validate atomic action
		       disable_validate_atomic_action:   Disable validate atomic action
		       enable_validate_molecular_action:    Enable validate molecular action
		       disable_validate_molecular_action:   Disable validate molecular action
		       sync:                Send actions from one database to another
       -d:            Specify a single database
       -to:           Specify a "to" sync database
       -from:         Specify a "from" sync database
       -id <#>:       Specify a session or transaction_id
       -head <#>:     Show only the first # of lines of the log
       -tail <#>:     Show only the last # of lines of the log
       -host <host>:  Machine where the application server is running,
	              Default is $host
       -port <port>:  Port on which server is listening, default is $port
       -v[ersion]:    Print version information.
       -[-]help:      On-line help

 Version $version, $version_date ($version_authority)
};
}



