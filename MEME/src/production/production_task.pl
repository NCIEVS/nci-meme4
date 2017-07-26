#!@PATH_TO_PERL@
#
# File:     production_task.pl
# Author:   Brian Carlsen
#
# Perform Production Actions
#
# CHANGES
# 01/12/2006 TTN (1-73ETH): Command-line utility for accessing MRD server production actions
#
$release = "4";
$version = "1.0";
$version_date = "01/12/2006";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set Defaults & Environment
#
unless ($ENV{MRD_HOME}) {
    $badvalue="MRD_HOME";
    $badargs=4;
}

#
# Check options
#
&HandleArguments;

#
# Parse parameters
# There should be zero parameters
if (scalar(@ARGS) != 0) {
    $badargs = 3;
    $badvalue = scalar(@ARGS);
}

unless ($target) {
  $badargs = 2;
  $badvalue = "-t";
}

unless ($release) {
  $badargs = 2;
  $badvalue = "-r";
}

unless ($stage) {
  $badargs = 2;
  $badvalue = "-s";
}

unless ($db) {
  $badargs = 2;
  $badvalue = "-d";
}

unless ($port) {
  $badargs = 2;
  $badvalue = "-host";
}

unless ($host) {
  $badargs = 2;
  $badvalue = "-host";
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

$memerun="$ENV{MRD_HOME}/bin/memerun.pl";
$prop="$ENV{MRD_HOME}/etc/mrd.prop";

print "-----------------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-----------------------------------------------------------\n";

print "Database:     $db\n";
print "Host:         $host\n";
print "Port:         $port\n";
print "Release:      $release\n";
print "Stage:        $stage\n";
print "Target:       $target\n";

system("$memerun -host=$host -port=$port -prop=$prop gov.nih.nlm.mrd.client.FullMRFilesReleaseClient $db $release $stage $target") == 0 or die "$memerun failed: $?\n";

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
       db:      $db
       host:    $host
       port:    $port
       release: $release
       stage:   $stage
       target:  $target
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
  elsif ($arg =~ /^-r=(.*)$/) {
    $release = $1;  }
  elsif ($arg =~ /^-r$/) {
    $release = shift(@ARGV);  }
  elsif ($arg =~ /^-s=(.*)$/) {
    $stage = $1;  }
  elsif ($arg =~ /^-s$/) {
    $stage = shift(@ARGV);  }
  elsif ($arg =~ /^-t=(.*)$/) {
    $target = $1;  }
  elsif ($arg =~ /^-t$/) {
    $target = shift(@ARGV);  }
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
    production_task.pl
                [-d <database/service>]
	        [-host <host>]
	        [-port <port>]
	        [-r <release>]
	        [-s <stage>]
	        [-t <targets>]
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 Options:
       -d <db>      : Specify a database
       -host=<host> : Machine where the application server is running
       -port=<port> : Port on which server is listening
       -r <release> : Specify a release
       -s <stage>   : Specify a stage to perform
                      Stages: prevQA, gold, build, validate, publish
       -t <target>  : Specify a list of targets
                      e.g: MRCONSO or MRCONSO,MRSTY,MRDEF
       -v[ersion]:    Print version information.
       -[-]help:      On-line help

 Version $version, $version_date ($version_authority)
};
}



