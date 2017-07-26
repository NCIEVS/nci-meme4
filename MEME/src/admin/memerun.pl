#!@PATH_TO_PERL@
#
# File:     memerun.pl
# Author:   Brian Carlsen (2002)
#
# This script is used to run java components in meme4
#
# Changes:
#    03/01/2006 BAC (1-AIDWN): system() used instead of open(CMD,"..|") to allow
#       return value to be properly communicated.
#
# Version Information
# 11/07/2003 4.6.0:  Released
# 10/30/2003 4.5.1:  Support for -headless operation
# 10/16/2003 4.5.0:  Arguments passed in are wrapped in " to preserve multi
#                    word structure
# 04/25/2003 4.4.0:  Released
# 04/17/2003 4.3.1:  meme.view=true instead of false
# 03/06/2003 4.3.0:  Release
# 02/20/2003 4.2.1:  Exit non-zero if java command fails
#                    to return zero status code
# 08/28/2002 4.2.0:  Verify NLM has most current version.
# 03/28/2002 4.1.1:  Keep existing $ENV{CLASSPATH}
# 01/31/2002 4.1.0:  Released to NLM
#                    - Upgrades were made to parameter passing
#                      to support both "-port=8080" and "-port 8080"
#                      style parameter passing.
#                    - Obtain host,port from midsvcs.pl by default
#                    - Environment is a single jar file
$release = "4";
$version = "6.0";
$version_date = "11/07/2003";
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
# Edit for platform
#
$java = "$ENV{JAVA_HOME}/bin/java -server -Xms200M -Xmx2000M ";

#
# edit classpath
#

#
# load lib/ jar files
#
opendir (LIB,"$ENV{MEME_HOME}/lib") || 
  die "Could not open $ENV{MEME_HOME}/lib: $! $?\n";
@f = readdir(LIB);
close(LIB);
foreach $file (@f) {
  if ($file =~ /\.jar/) { $ENV{CLASSPATH} .= ":$ENV{MEME_HOME}/lib/$file"; }
  if ($file =~ /\.zip/) { $ENV{CLASSPATH} .= ":$ENV{MEME_HOME}/lib/$file"; }
}

#
# load lib/ jar files
#
opendir (LIB,"$ENV{EXT_LIB}") || 
  die "Could not open $ENV{EXT_LIB}: $! $?\n";
@f = readdir(LIB);
close(LIB);
foreach $file (@f) {
  if ($file =~ /\.jar/) { $ENV{CLASSPATH} .= ":$ENV{EXT_LIB}/$file"; }
  if ($file =~ /\.zip/) { $ENV{CLASSPATH} .= ":$ENV{EXT_LIB}/$file"; }
}

#
# Default properties file
#
$prop_file = "$ENV{MEME_HOME}/etc/meme.prop";

%options = (
	    "meme.view" => "true",
	    "meme.debug" => "false",
	    "meme.properties.file" => "$prop_file",
	    "meme.mid.services.host" => "$ENV{MIDSVCS_HOST}",
	    "meme.mid.services.port" => "$ENV{MIDSVCS_PORT}",
	    "env.ENV_HOME" => "$ENV{ENV_HOME}",
	    "env.ENV_FILE" => "$ENV{ENV_FILE}",
	    "meme.tmp.directory" => "/tmp"
	    );

%tf_map = (
           "true" => "true",       "false" => "false",
           "1" => "true",          "0" => "false",
           "Y" => "true",          "N" => "false",
           "y" => "true",          "n" => "false",
           "t" => "true",          "f" => "false");

$tf_line = "true|false|t|f|y|n|Y|N|1|0";

# Check options
@ARGS=();
while (@ARGV) {
  $arg = shift(@ARGV);
  if ($arg !~ /^-/) {
    push @ARGS, $arg;
    next;
   }

  if ($arg =~ /^-log=(.*)/) {
    $options{"meme.log.path"} = "$1"; }
  elsif ($arg =~ /^-log/) {
    $options{"meme.log.path"} = shift(@ARGV); }
  elsif ($arg =~ /^-prop=(.*)/) {
    $options{"meme.properties.file"} = "$1" }
  elsif ($arg =~ /^-prop/) {
    $options{"meme.properties.file"} = shift(@ARGV) }
  elsif ($arg =~ /^-debug=($tf_line)$/) {
    $options{"meme.debug"} = $tf_map{$1} }
  elsif ($arg =~ /^-debug$/) {
    if (($argq=shift(@ARGV))=~/^($tf_line)$/) {
      $options{"meme.debug"} = $argq;
    } else {$badargs = 1; $badvalue = "$arg $argq."; }
  }
  elsif ($arg =~ /^-view=($tf_line)$/) {
    $options{"meme.view"} = $tf_map{$1} }
  elsif ($arg =~ /^-view$/) {
    if (($argq=shift(@ARGV))=~/^($tf_line)$/) {
      $options{"meme.view"} = $argq;
    } else {$badargs = 1; $badvalue = "$arg $argq"; }
  }
  elsif ($arg =~ /^-host=(.*)/) {
    $options{"meme.client.server.host"} = $1; }
  elsif ($arg =~ /^-host$/) {
    $options{"meme.client.server.host"} = shift(@ARGV); }
  elsif ($arg =~ /^-port=(.*)/) {
    $options{"meme.client.server.port"} = $1; 
    $options{"meme.server.port"} = $1; 
  }
  elsif ($arg =~ /^-port$/) {
    $options{"meme.client.server.port"} = shift(@ARGV); 
    $options{"meme.server.port"} = $options{"meme.client.server.port"};
  }
  elsif ($arg eq "-headless") {
    $headless = 1;
   }

  elsif ($arg =~ /^-mid=(.*)/) {
    $options{"meme.mid.service.default"} = "$1" }
  elsif ($arg =~ /^-mid/) {
    $options{"meme.mid.service.default"} = shift(@ARGV) }
  elsif ($arg =~ /^-mrd=(.*)/) {
    $options{"meme.mrd.service.default"} = "$1" }
  elsif ($arg =~ /^-mrd/) {
    $options{"meme.mrd.service.default"} = shift(@ARGV) }
  
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
    # invalid memerun switches may
    # be valid switches for the class being called
    push @ARGS, $arg;
  }
}

#
# Set host/port defaults from MIDSVCS server
#
unless ($options{"meme.client.server.host"}) {
  $host =`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-host`;
  chop($host);
  $options{"meme.client.server.host"} = $host;
}

unless ($options{"meme.client.server.port"}) {
  $port =`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`;
  chop($port);
  $options{"meme.client.server.port"} = $port;
}

#
# Get command line params
#
if (scalar(@ARGS) > 0) {
  ($class,@args) = @ARGS;
  map { $_ = qq{"$_"}; } @args;
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



$option_line = "";
foreach $key (keys (%options)) {
    $option_line .= " -D$key=$options{$key}" if $options{$key};
}
if ($headless) {
  $option_line .= " -Djava.awt.headless=true";
}

 
#
# Make the call
#
#print "$java $option_line $class @args \n";
system(qq{$java $option_line $class @args});
if ($?) { exit 1; }
exit(0);

######################### LOCAL PROCEDURES #######################

sub PrintUsage {

	print qq{ This script has the following usage:
    memerun.pl [-log=<file>] [-prop=<file>] [-host=<host>] [-port=<port>] 
               [-debug={true,false}] [-view{true,false}] [-headless]
	       <class> <argument list>
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 Options:
       -headless>:          Run in headless environment
       -log=<file>:         Name of log file (relative to $ENV{MEME_HOME})
			      Default is $options{"meme.log.path"})
       -prop=<file>:        Name properties file 
	                      Default is $options{"meme.properties.file"})
       -host=<host>:        Name of the machine where server is running 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-host`,qq{       -port=<port>:        The port number that the server is listening on 
	                      Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`,qq{       -debug=(true|false): Set debug mode
       -view=(true|false):  Set view mode
       -v[ersion]:          Print version information.
       -[-]help:            On-line help

 Version $version, $version_date ($version_authority)
};
}
