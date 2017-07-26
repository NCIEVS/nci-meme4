#!@PATH_TO_PERL@
#
# File:    copy_vsab.pl
# Author:  Tim Kao (10/2007)
#
# REMARKS: This script copy an input vsab from $ARCHIVE_ROOT to $SRC_ROOT.
#
# CHANGES  9/10/2007 TK (1-FQ6VN): Created on this date.

BEGIN
{
	unshift @INC, "$ENV{ENV_HOME}/bin";
	require "env.pl";
	unshift @INC, "$ENV{INV_HOME}/lib";
}

#
# Set variables
#
$ENV{"LANG"}       = "en_US.UTF-8";
$ENV{"LC_COLLATE"} = "C";

#
# Parse arguments
#
while (@ARGV)
{
	$arg = shift(@ARGV);
	push( @ARGS, $arg ) && next unless $arg =~ /^-/;

	if ( $arg eq "-version" )
	{
		$print_version = "version";
	} elsif ( $arg eq "-v" )
	{
		$print_version = "v";
	} elsif ( $arg eq "-help" || $arg eq "--help" )
	{
		$print_help = 1;
	} else
	{
		$print_help = 1;
	}
}

if ( scalar(@ARGS) == 1 )
{
	($vsab) = @ARGS;
} else
{
	$print_help = 1;
}

#
# Print Help/Version info, exit
#
&PrintHelp                    && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

#
# Program logic
#
use Archives;

print "-------------------------------------------------------\n";
print "Starting ...", scalar(localtime), "\n";
print "-------------------------------------------------------\n";

print "   Configuration...", scalar(localtime), "\n";
&Archives::setVSAB("$vsab");
&Archives::setSRCROOT("$ENV{SRC_ROOT}");
&Archives::setARCHIVEROOT("$ENV{ARCHIVE_ROOT}");

my $vsab_path = &Archives::findVSABPath("ARCHIVE");

&Archives::copy_vsab( "$vsab_path" );

print qq{-------------------------------------------------
Finished ... }, scalar(localtime), qq{
-------------------------------------------------
};
exit 0;

####### Local Procedures #######

sub PrintVersion
{
	my ($type) = @_;
	print "No version info\n";
	return 1;
}

sub PrintUsage
{

	print qq{ This script has the following usage:
 Usage: copy_vsab.pl <vsab>
    };
}

sub PrintHelp
{
	&PrintUsage;
	print qq{
  vsab:                 Versioned source name
  
  This script copies the $vsab from the ARCHIVE_ROOT to SRC_ROOT
};
	&PrintVersion("version");
	return 1;
}


