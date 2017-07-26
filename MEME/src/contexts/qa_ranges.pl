#!@PATH_TO_PERL@
#
# File:		qa_ranges.pl
# Author:	Deborah Shapiro, Brian Carlsen
# 
# This script has the following usage:
# qa_ranges.pl
# code_ranges.dat and source_atoms.dat must exist in calling directory
#
# Options:
#	-v[ersion]:	Print version information
#	-[-]help:	On-line help
#
# 1. Check that every source_atom_id in source_atoms.dat fits into one of the
# ranges from code_ranges.dat
# 2. Check that every code_range in code_ranges.dat fits into a parent 
# code range from the same file.  The root(s) are exceptions and 
# will be printed out.
# 3. Check that there should be no parent (broader) range with a higher
# context level than the context level of the child (narrower) range
# 
# CHANGES
# 10/24/2007 BAC (1-FLHKX): Move use open ":utf8" directive.
# 05/31/2007 BAC (1-D9NBZ): in-memory algorithms.


BEGIN {
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{INV_HOME}/lib";
}
use open ":utf8";
#
# Parse arguments
#
while(@ARGV) {
    $arg = shift(@ARGV);
    if ($arg eq "-v") {
        $print_version="v";
    }
    elsif ($arg eq "-version") {
        $print_version="version";
    }
    elsif ($arg eq "-help" || $arg eq "--help") {
        $print_help=1;
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
# Check dependencies
#
if ( !(-e "code_ranges.dat")) {
   $badargs = 2;
}

if ( !(-e "source_atoms.dat")) {
   $badargs = 3;
}

#
# Print bad argument errors if any found
#
if ($badargs) {
    %errors = (1 => "Illegal switch: $badswitch",
	       2 => "Cannot fine code_ranges.dat; exiting...",
	       3 => "Cannot find source_atoms.dat; exiting..."
	      );
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
}

#
# Program Logic
#
use Contexts;

print "-------------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-------------------------------------------------------\n";
&Contexts::configure(".",".",1,0,1);
&Contexts::printConfiguration;
&Contexts::cacheAtoms;
&Contexts::cacheRanges;
if (&Contexts::checkRanges) {
  print "    Completed successfully ...",scalar(localtime),"\n";
} else{
  print "    Completed with errors ...",scalar(localtime),"\n";
  foreach $error (sort &Contexts::getErrors) {
      print "      $error";
  }
}

print "-------------------------------------------------------\n";
print "Finished ...",scalar(localtime),"\n";
print "-------------------------------------------------------\n";
exit (0);

############################# local procedures ################################
sub PrintVersion {
    my($type) = @_;
    return 1;
}

sub PrintUsage {
    print qq{ This script has the following usage:
  qa_ranges.pl 
    };
}

sub PrintHelp {
    &PrintUsage;
    print qq{
  Options:
        -v[ersion]:     Print version information.
        -[-]help:       On-line help
    };
    &PrintVersion("version");
    return 1;
}

