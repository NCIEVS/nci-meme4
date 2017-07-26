#!@PATH_TO_PERL@
#
# File:     stamping.pl
# Author:   Brian Carlsen 
#
# Simple client to run a report
#
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
# 01/30/2006 BAC (1-7689Y): check for $inlog condition differently due to changes
#    in ObjectXMLSerializer.
# 12/22/2005 BAC (1-719SM): use open ":utf8"  added
#
# Version Information
# 09/30/2003 (4.2.0): bug fix
# 03/19/2003 (4.1.0):  First version
#
$release = "4";
$version = "2.0";
$version_date = "09/30/2003";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";

#
# Set Defaults & Environment
#
unless ($ENV{MEME_HOME}) {
    $badvalue="MEME_HOME";
    $badargs=4;
}

# Obtain defaults from midsvcs.pl
# ($host,$port) = ("localhost","8080");
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
   }
  elsif ($arg eq "-v") {
    print "$version\n";
    exit(0);
   }
  elsif ($arg eq "-help" || $arg eq "--help") {
    &PrintHelp;
    exit(0);
   }
  elsif ($arg =~ /^-t=(.*)$/) {
    $table_name = $1;  }
  elsif ($arg =~ /^-t$/) {
    $table_name = shift(@ARGV);  }
  elsif ($arg =~ /^-f=(.*)$/) {
    $file_name = $1;
    $table_name = "t_stamp_$$";  }
  elsif ($arg =~ /^-f$/) {
    $file_name = shift(@ARGV);  
    $table_name = "t_stamp_$$";  }
  elsif ($arg =~ /^-c=(.*)$/) {
    @ids = split /,/, $1;
    $table_name = "t_stamp_$$";  }
  elsif ($arg =~ /^-c$/) {
    @ids = split /,/, shift(@ARGV);  
    $table_name = "t_stamp_$$";  }
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


#
# Parse parameters
#
if (scalar(@ARGS) == 2) {
  ($db, $authority) = @ARGS;
} else {
    $badargs = 3;
    $badvalue = scalar(@ARGS);
}

%errors = (1 => "Illegal switch: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set");

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}

#
# If using a file, load into the database
#
use DBD::Oracle;
if ($file_name || scalar(@ids)>0) {
  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "Error opening $db ($DBI::errstr).")
     &&  return);
 
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table',?); END;") ||
    die "Error preparing query 1 ($DBI::errstr).";
  $sh->execute($table_name) || 
    die "Error executing query 1 ($DBI::errstr).";
          
  $dbh->do(qq{
	CREATE TABLE $table_name AS SELECT concept_id FROM classes WHERE 1=0
    }) || die "Error executing create 1 ($DBI::errstr).";

  if ($file_name) {
    open (F,"$file_name") || die "Could not open file $file_name: $! $?\n";
    while (<F>) {
      chop;
      $dbh->do(qq{
		  INSERT INTO $table_name VALUES (?)
		 }, undef, $_) || die "Error executing insert 1 ($DBI::errstr).";
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
# Connect to server, send ServerShutdown message
#
require 5.003;
use Socket;
$proto = getprotobyname("tcp");

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
binmode(SOCK,":utf8");
select(SOCK);
$| = 1;
select(STDOUT);

print SOCK qq{POST / HTTP/1.1

<MASRequest>
  <ConnectionInformation>
    <Session nosession="true" />
    <DataSource service="$db" />
  </ConnectionInformation>
  <ServiceParameters>
    <Service>ActionService</Service>
    <Parameter>
      <Object name="" id="1"  class="gov.nih.nlm.meme.common.Parameter" length="2">
       <Object name="0" id="2" >
        <Object name="name" id="3">transaction</Object>
        <Object name="value" id="4"  class="gov.nih.nlm.meme.common.BatchMolecularTransaction"  authority="$authority" action_name="AC" elapsed_time="0" status="R" id_type="CS" table_name="$table_name" action_field="NONE">
         <Object name="eiv" id="5" >
          <Object name="checks" id="6">
          </Object>
         </Object>
        </Object>
       </Object>
       <Object name="1" id="7" >
        <Object name="name" id="8">function</Object>
        <Object name="value" class="java.lang.String" id="9">do_batch</Object>
       </Object>
      </Object>
    </Parameter>
  </ServiceParameters>
  <ClientResponse>
  </ClientResponse>
</MASRequest>
};

$found=0;
$message = "";
$ct=0;
while (<SOCK>) {
  #print;

  if ($inlog) {
    if (/<\/Object>/) {
      $inlog=0; 
    } else {
      s/.*name="value".*DATA\[(.*)/$1/;
      s/.*name="value".*\>(.*)/$1/;
      print;
    }
  }
  if (/.*name="name".*value="log"/) {
    $inlog=1;
  }
  
  if (/name="details"/) {
    $inhashmap = 1;
  }
  if ($inhashmap) {
    if (/.*name="key".*DATA\[(.*)\]\]\>\<\/Object\>/ ||
	/.*name="key".*\>(.*)\<\/Object\>/ ||
        /key="(.*)"/) {
      $key = $1;
    }
    if (s/.*name="value".*DATA\[(.*)\]\]\>\<\/Object\>/$1/ ||
	s/.*name="value".*\>(.*)\<\/Object\>/$1/ || 
        /value="(.*)"/) {
      $details{$key}="$1";
    }
  }
  
  if (s/.*name="message".*DATA\[(.*)\]\]\>\<\/Object\>/$1/ ||
      s/.*name="message".*\>(.*)\<\/Object\>/$1/ ||
        /message="(.*)"/) {
    $message = $1;
  }
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

exit 0;


######################### LOCAL PROCEDURES #######################

sub PrintUsage {

	print qq{ This script has the following usage:
    stamping.pl [-host=<host>] [-port=<port>]
		[-t=<table>] [-f=<file>] [-c=<id list>]
	       <db> <authority>
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 Options:
       -t=<table>:    Specify a table with a concept_id field 
       -f=<file>:     Specify a file of concept ids.  In the
		      background, this will be loaded into a table
       -c=<id list>:  Specify a comma separated list of concept_ids.
		      Like -f, these ids will be loaded into a table first.
       -host=<host>:  Machine where the application server is running,
	              Default is $host
       -port=<port>:  Port on which server is listening,
		      Default is $port
       -v[ersion]:    Print version information.
       -[-]help:      On-line help

 Version $version, $version_date ($version_authority)
};
}



