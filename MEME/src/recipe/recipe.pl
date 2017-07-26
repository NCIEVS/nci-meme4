#!@PATH_TO_PERL@
#
# File:     recipe.pl
# Author:   Brian Carlsen
#
# Dependencies:
#    
#
# This script has the following usage:
# 
#
# Options:
#       -l(logfile):    Name logfile
#       -p(paramfile):  Name parameter file (default is recipe.ini)
#       -d(true|false): Debug mode
#       -V(true|false): View mode
#       -db(name):      Name of a midsvcs.pl database service
#                       e.g. "current-editing"
#       -u(user):       username
#       -v[ersion]:Print version information.
#       -[-]help:  On-line help
#
# Changes
# 11/08/2006 BAC (1-CR3OP): One more line needed to alleviate need for user to pass -d param
# 11/07/2006 BAC (1-CR3OP): Better handling of passwords
$version = "1.3";
$version_date = "11/07/2000";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set Defaults & Environment
#
unless ($ENV{RECIPE_HOME}) {
    $badvalue="RECIPE_HOME";
    $badargs=4;
}

#
# Version of java to use
# 
$java = "$ENV{JAVA_HOME}/bin/java -server -Xms400M -Xmx400M ";

#
# $RECIPE_HOME/LIB files
#
opendir (LIB,"$ENV{RECIPE_HOME}/lib") || 
  die "Could not open $ENV{RECIPE_HOME}/lib: $! $?\n";
@f = readdir(LIB);
close(LIB);
foreach $file (@f) {
  if ($file =~ /\.jar/) { $ENV{CLASSPATH} .= ":$ENV{RECIPE_HOME}/lib/$file"; }
  if ($file =~ /\.zip/) { $ENV{CLASSPATH} .= ":$ENV{RECIPE_HOME}/lib/$file"; }
}

%options = (
	    "DB_DRIVER_CLASS" => "oracle.jdbc.driver.OracleDriver",
	    "VIEW" => "true",
	    "PROPERTY_FILE" => "$ENV{RECIPE_HOME}/etc/recipe.ini",
	    "NLS" => "$ENV{LVG_HOME}",
	    "MEME_HOME" => "$ENV{RECIPE_HOME}",
	    "ORACLE_HOME" => "$ENV{ORACLE_HOME}",
	    "TMP_DIRECTORY" => "/tmp"
	    );

%tf_map = (
	   "true" => "true",	   "false" => "false",
	   "1" => "true",	   "0" => "false",
	   "Y" => "true",	   "N" => "false",
	   "y" => "true",	   "n" => "false",
	   "t" => "true",	   "f" => "false");
	
$tf_line = "true|false|t|f|y|n|Y|N|1|0";
$class = "gov.nih.nlm.recipe.RxWriter";

# Check options
@ARGS=();
while (@ARGV) {
    $arg = shift(@ARGV);
    if ($arg !~ /^-/) {
	unshift @ARGS, $arg;
	next;
    }

    if ($arg =~ /^-l(.*)/) {
	$options{LOG_FILE} = "$1"; }
    elsif ($arg =~ /^-p(.*)/) {
	$options{PROPERTY_FILE} = "$1"}
    elsif ($arg =~ /^-d($tf_line)/) {
        $options{DEBUG} = $tf_map{$1} }
    elsif ($arg =~ /^-V($tf_line)/) {
	$options{VIEW} = $tf_map{$1} }
    elsif ($arg =~ /^-db(.*)/) {
	$options{DB_SERVICE} = "$1"; }
    elsif ($arg =~ /^-src=(.*)/) {
	$options{SRC_DIRECTORY} = "$1"; }
    elsif ($arg =~ /^-u(.*)/) {
	$user = "$1"; }
    elsif ($arg =~ /^-writer/) {
	$class = "gov.nih.nlm.recipe.RxWriter"; }
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


if (!$class) {
    $badargs = 2;
} elsif (scalar(@ARGS) == 1) {
    $options{RECIPE_FILE} = $ARGS[0];
} elsif (scalar(@ARGS) == 0) {
    $recipefile = "";
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
# look up user/password
#
$options{DB_SERVICE} = "editing" unless $options{DB_SERVICE};
$edb = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s $options{DB_SERVICE}-db`;
chop($edb);
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $edb`;
chop($userpass);
($user,$pass) = split /\//, $userpass;
$options{DB_USER} = $user;
$options{DB_PASSWORD} = "'$pass'";

$option_line = "";
foreach $key (keys (%options)) {
#    print "$key=$options{$key}\n";
    $option_line .= " -D$key=$options{$key}";
}
 
#
# Make the call
#
print "$java $option_line $class\n";
open (CMD,"$java $option_line $class |") || die "Could not open command: $! $?\n";
while (<CMD>) {
    print;
};
close(CMD);
exit(0);

######################### LOCAL PROCEDURES #######################

sub PrintUsage {

	print qq{ This script has the following usage:
    recipe.pl [-writer] [-l<logfile>] [-p<paramfile>] [-d{true,false}] 
              [-V{true,false}] [-db<service name>] [-u<user>] [-src=<src directory>]
	      [<recipe name>]
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 Options:
       -writer:        Run the recipe writer
       -src=(src_directory):    set the SRC_DIRECTORY
       -l(logfile):    Name logfile
       -p(paramfile):  Name parameter file (default is $options{PARAMTER_FILE})
       -d(true|false): Debug mode
       -V(true|false): View mode
       -db(name):      DB Service name (midsvcs.pl minus the -tns)
       -u(user):       username
       -v[ersion]:Print version information.
       -[-]help:  On-line help

 Version $version, $version_date ($version_authority)
};
}
