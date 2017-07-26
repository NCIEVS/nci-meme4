#!@PATH_TO_PERL@
#
# File:    ranges_to_bt_nt.pl 
# Author:  Deborah Shapiro (6/2000)
#
# REMARKS: This script converts a code ranges formatted file
#          into the bt_nt format.  It starts by loading the code_ranges.dat
#          file into a table called code_ranges.  It creates the bt_nt_rels.dat
#          file at the end from the bt_nt_rels table.  If no option flag is
#          indicated the code ranges are assumed to be simple.  If the
#          -prefix flag is included than the ranges are explected to be of
#          the prefix hierarchy type.
#
# Usage: ranges_to_bt_nt.pl [-prefix] [-d<db_name>]
#
# Porting Status: Ported to oracle
#
# CHANGES
# 10/24/2007 BAC (1-FLHKX): Move use open ":utf8" directive.
# 05/31/2007 BAC (1-D9NBZ): in-memory algorithms.
# 11/13/2006 BAC (1-CDMK9): better support for managing multiple users.
# 06/30/2000 (3.1.0) in progress 
$release="3";
$version="1.0";
$version_authority="DSS";
$version_date="06/14/2000";

BEGIN {
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{INV_HOME}/lib";
}
use open ":utf8";
#
# Check required environment variables
# n/a

#
# Set variables
#
$prefix = 0;

#
# Parse arguments
#
while (@ARGV) {
    $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-version") {
	$print_version="version";
    }
    elsif ($arg eq "-v") {
	$print_version="v";
    }
    elsif ($arg eq "-help" || $arg eq "--help") {
	$print_help=1;
    }
    elsif ($arg eq "-prefix") {
      #no longer used
      $prefix = 1;
    } 
    elsif ($arg =~ /^-d*/) {
        # do nothing but preserve for backwards compatability
    }
    else {
	$badargs = 1;
	$badswitch = $arg;
    }
}

#
# Print Help/Version info, exit
#
&PrintHelp && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

#
# Get arguments
#
if (scalar(@ARGS) > 1) {
    $badargs = 2;
    $badopt = $#ARGS+1;
}

#
# Check dependencies
#
$badargs = 4 unless (-e "code_ranges.dat");
$badargs = 6 unless (-e "source_atoms.dat");

#
# Print bad argument errors if any found
#
if ($badargs) {
    %errors = (1 => "Illegal switch: $badswitch",
	       2 => "Wrong number of arguments: $badopt",
	       4 => "Cannot find required file: code_ranges.dat.",
	       6 => "Cannot find required file: source_atoms.dat."
 );
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
};

#
# Program logic
#
use Contexts;

print "-------------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-------------------------------------------------------\n";
&Contexts::configure(".",".",1,0,1);
&Contexts::printConfiguration;
&Contexts::cacheAtoms;
&Contexts::cacheRanges;
if (&Contexts::rangesToRelsFile) {
  print "    Completed successfully ...",scalar(localtime),"\n";
} else {
  print "    Completed with errors...",scalar(localtime),"\n";
  foreach $error (sort &Contexts::getErrors) { 
    print "      $error";
  }
}
print "-------------------------------------------------------\n";
print "Finished ...",scalar(localtime),"\n";
print "-------------------------------------------------------\n";
exit (0);


####### Local Procedures #######
sub PrintVersion {
    my($type) = @_;
    print "Release $release: version $version, ".
          "$version_date ($version_authority).\n"
          if $type eq "version";
    print "$version\n" if $type eq "v";
    return 1;
}

sub PrintUsage {

    print qq{ This script has the following usage:
 Usage: ranges_to_bt_nt.pl [-prefix]
    };
}

sub PrintHelp {
    &PrintUsage;
    print qq{
 This script converts a code ranges formatted file
 into the bt_nt format.  It starts by loading the code_ranges.dat
 file into a table called code_ranges.  It creates the bt_nt_rels.dat
 file at the end from the bt_nt_rels table.  If no option flag is
 indicated the code ranges are assumed to be simple.  If the
 -prefix flag is included than the ranges are explected to be of
 the prefix hierarchy type.

  Options:
         -v[ersion]:             Print version information
         -[-]help:               On-line help
};
    &PrintVersion("version");
    return 1;
}
