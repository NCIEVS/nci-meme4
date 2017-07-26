#!@PATH_TO_PERL@
#
# File:     start.pl
# Author:   Brian Carlsen
#
# Wrapper script for starting MEME application server.
#
# Version Information
# 4.10.0 03/23/2005:  More generous memeory parameters (2GB max)
# 4.9.0 05/16/2003:  Headless
# 4.8.0 05/12/2003:  Released
# 4.7.1 04/17/2003:  Put ext classpath ahead of regular one
# 4.7.0 04/10/2003:  Released. Includes $ENV{MEME_HOME}/ext 
#                    and .zip,.jar sub-files in classpath
# 4.6.2 03/24/2003:  Remove logs in log directory.
# 4.6.1 03/03/2003:  Use system instead of open(CMD,...)
# 4.6.0 06/20/2002:  Previous code did not correctly remove old logs
#                    this release fixes the problem.
# 4.5.0 05/13/2002:  Released 4.1
# 4.4.1 04/29/2002:  Fixed "remove old logs" functionality
# 4.4.0 04/22/2002:  Removes any server logs older than 2 weeks.
#                    see &RemoveOldLogFiles
# 4.3.0 03/07/2002:  Some property names were changed.
#                    meme.server.mid.driver.class => meme.mid.driver.class
#                    meme.server.mid.service.default => meme.mid.service.def
#                    meme.server.mid.user.default => meme.mid.user.default
#                    meme.server.mid.password.default => 
#                         meme.mid.password.default
#
# 4.2.0 01/31/2002:  Released to NLM
#                    Upgrades were made to parameter passing
#                    to support both "-port=8080" and "-port 8080"
#                    style parameter passing.
#                    Environment is a single jar file
# 4.1.0:  First version - Released to NLM
#
$release = "4";
$version = "10.0";
$version_date = "03/23/2005";
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
$date = `/bin/date +%Y%m%d`;
$time = `/bin/date +%H%M%S`;
chop($date);
chop($time);

#
# Remove old log files
#
&RemoveOldLogFiles;

#
# Set java stuff
#
$java = "$ENV{JAVA_HOME}/bin/java -server -Xms200M -Xmx800M ";
$class = "gov.nih.nlm.meme.server.MEMEApplicationServer";

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

#print "\$ENV{CLASSPATH} = $ENV{CLASSPATH}\n";
%options = (
	    "meme.mid.driver.class" => "oracle.jdbc.driver.OracleDriver",
	    "meme.view" => "false",
	    "meme.properties.file" => "$prop_file",
	    "meme.tmp.directory" => "/tmp",
	    "meme.log.path" => "log/$date.$time.log",
	    "meme.mid.services.host" => "$ENV{MIDSVCS_HOST}",
	    "meme.mid.services.port" => "$ENV{MIDSVCS_PORT}",
	    "env.ENV_HOME" => "$ENV{ENV_HOME}",
	    "env.ENV_FILE" => "$ENV{ENV_FILE}"
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
  elsif ($arg =~ /^-log$/) {
    $options{"meme.log.path"} = shift(@ARGV); }
  elsif ($arg =~ /^-prop=(.*)/) {
    $options{"meme.properties.file"} = "$1"}
  elsif ($arg =~ /^-port=(.*)/) {
    $options{"meme.server.port"} = "$1"; }
  elsif ($arg =~ /^-port$/) {
    $options{"meme.server.port"} = shift(@ARGV)}
  elsif ($arg =~ /^-prop$/) {
    $options{"meme.properties.file"} = shift(@ARGV)}
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
  elsif ($arg =~ /^-mid=(.*)/) {
    $options{"meme.mid.service.default"} = "$1"; }
  elsif ($arg =~ /^-mid$/) {
    $options{"meme.mid.service.default"} = shift(@ARGV) }
  elsif ($arg =~ /^-user=(.*)/) {
    $user = "$1"; }
  elsif ($arg =~ /^-user$/) {
    $user = shift(@ARGV); }
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
    $badargs = 1;
    $badvalue = $arg;
  }
}

#
# Obtain default port setting from midsvcs.pl
#
unless ($options{"meme.server.port"}) {
  $port = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`;
  chop ($port);
  $options{"meme.server.port"} = $port;
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

#
# look up user/password
#
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
chop($userpass);
($user,$pass) = split /\//, $userpass;
$options{"meme.mid.user.default"} = $user;
#$options{"meme.mid.password.default"} = $pass;

$option_line = "";
foreach $key (keys (%options)) {
#    print "$key=$options{$key}\n";
    $option_line .= " -D$key=$options{$key}" if $options{$key};
}
 
#
# Make the call
#
#print "$java $option_line $class \n";
$log_line = "logging to \$MEME_HOME/".$options{"meme.log.path"} 
if $options{"meme.log.path"};
print "Starting server ... $log_line\n";
#print "$java $option_line $class\n";
system ("$java $option_line $class");
if ($?) { exit(1); }
exit(0);

######################### LOCAL PROCEDURES #######################

sub RemoveOldLogFiles {

  #
  # 1209600 is the number of seconds in 14 days
  #
  ($d,$d,$d,$mday,$mon,$year) = localtime(time-1209600);
  $year += 1900;
  $mon += 1;
  $mon = "0$mon" if length($mon) == 1;
  $mday = "0$mday" if length($mday) == 1;
  print "Removing log files on or before $year$mon$mday\n";
  opendir(D,"$ENV{MEME_HOME}/log");
  @files = readdir(D);
  closedir(D);
  foreach $f (@files) {
    if ($f =~ /\.log$/ && $f le "$year$mon$mday.000000.log") {
      print "Removing $f\n";
      unlink "$ENV{MEME_HOME}/log/$f";
    }
  }
}


sub PrintUsage {

	print qq{ This script has the following usage:
    start.pl [-log=<file>] [-prop=<file>] [-debug={true,false}] 
            [-view={true,false}] [-mid=<service name>] [-user=<user>] 
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 Options:
       -log=<file>:        Name of log file (relative to $ENV{MEME_HOME},
			     Default is $options{"meme.log.path"})
	                     Use empty filename to log to STDOUT
       -prop=<file>:       Name properties file 
	                     Default is $options{"meme.properties.file"})
       -port=<port>:       Specify which port the server should listen on
       -debug=(true|false): Debug mode
       -view=(true|false):  View mode
       -mid=<name>:        MID Service name (midsvcs.pl minus the -tns)
       -user=<user>:       Default username when connecting to mid service
       -v[ersion]:         Print version information.
       -[-]help:           On-line help

 Version $version, $version_date ($version_authority)
};
}


