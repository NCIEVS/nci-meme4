#!@PATH_TO_PERL@
#
# File:     jenscript.pl
# Author:   Bobby Edrosa
#
# Simple client to print in java.
#
# Changes:
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
#
# Version Information
#
# 11/07/2003 4.3.0: Released
# 10/30/2003 4.2.1: Renamed jenscript, runs in headless environment
# 10/24/2003 4.2.0: Additional arguments (-s, -noscale)
# 10/16/2003 4.1.0: First Version
#
$release = "4";
$version = "3.0";
$version_date = "11/07/2003";
$version_authority="RBE";

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

$title = "Untitled";
$pages = 1;
$printer_name = "no printer";
$wrap = "false";
$scale_flag = "true";
$scale = 1.5;

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
  } elsif ($arg =~ /^-wrap/) {
    $wrap = "true";
  } elsif ($arg =~ /^-noscale/) {
    $scale_flag = "false";
  } elsif ($arg =~ /^-1/) {
    $pages = 1;  
  } elsif ($arg =~ /^-2/) {
    $pages = 2;  
  } elsif ($arg =~ /^-s=(.*)$/) {
    $scale = $1;  
  } elsif ($arg =~ /^-s$/) {
    $scale = shift(@ARGV);  
  } elsif ($arg =~ /^-b=(.*)$/) {
    $title = $1;  
  } elsif ($arg =~ /^-b$/) {
    $title = shift(@ARGV);  
  } elsif ($arg =~ /^-p=(.*)$/) {
    $printer_name = $1;  
  } elsif ($arg =~ /^-p$/) {
    $printer_name = shift(@ARGV);  
  } elsif ($arg =~ /^-prop=(.*)/) {
    $prop = "-prop=$1";
  } elsif ($arg =~ /^-prop/) {
    $prop = "-prop=".shift(@ARGV);
  } else {
    $badargs = 1;
    push @ARGS, $arg;
  }
}

#
# Get command line params
#
if (!$pages) {
  $badargs = 5;
} elsif (scalar(@ARGS) == 1) {
  ($file) = @ARGS;
} else {
  $badargs = 3;
  $badvalue = scalar(@ARGS);
}

#
# Process errors
#
%errors = (1 => "Illegal switch: $badvalue",
	   2 => "Illegal service: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set",
           5 => "Required parameter missing -1 or -2",
           6 => "Required parameter missing -b",
           7 => "Required parameter missing -p"
	   );

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}

#
# Make the call
#
open(CMD,qq{$ENV{MEME_HOME}/bin/memerun.pl -headless -view=true $prop gov.nih.nlm.util.Enscript "$file" "$printer_name" "$title" $pages $wrap $scale_flag $scale |}) || die "Error executing command: $! $?\n";
while (<CMD>) {
  print;
}
close(CMD);

exit 0;

######################### LOCAL PROCEDURES #######################

sub PrintUsage {

	print qq{ This script has the following usage:
   jenscript.pl {-1,-2} [-wrap] [-b=<title>] [-p=<printer name>] 
	       [-s=<scale factor>] [-noscale] <file> 
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 This script is a wrapper around gov.nih.nlm.util.Enscript
 and is used for printing HTML or text documents in MEME4.

 Options:
       -1             Print one page per sheet 
       -2             Print two pages per sheet
       -b             Title of the page
       -p             Printer Name
       -wrap          Indicates that document needs to be wrapped
		      with <html>..</html> tags.
       -s             The scaling factor
       -noscale       Indicates that the document should not be scaled
       -v[ersion]:    Print version information.
       -[-]help:      On-line help

 Parameters:
       <file>         File name

 Version $version, $version_date ($version_authority)
};
}



