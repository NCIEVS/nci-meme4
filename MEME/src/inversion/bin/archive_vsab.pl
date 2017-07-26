#!@PATH_TO_PERL@
#
# File:    archive_vsab.pl
# Author:  Tim Kao (9/2007)
#
# REMARKS: This script archives an input vsab from $SRC_ROOT to $ARCHIVE_ROOT
#
# CHANGES

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
$never_inserted    = 0;
$project           = 0;

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
	} elsif ( $arg eq "-never_inserted" )
	{
		$never_inserted = 1;
	} elsif ( $arg eq "-project" )
	{
		$project = 1;
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
&Archives::setMEMEHOME("$ENV{MEME_HOME}");
&Archives::setSRCROOT("$ENV{SRC_ROOT}");
&Archives::setARCHIVEROOT("$ENV{ARCHIVE_ROOT}");

&Archives::configure( $vsab, $never_inserted, $project );

&Archives::archive();

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
 Usage: archive_vsab.pl <vsab> [-project] [-never_inserted]
    };
}

sub PrintHelp
{
	&PrintUsage;
	print qq{
  vsab:                 Versioned source name
  -project:             Will archive to $ENV{ARCHIVE_ROOT}/project
  -never_inserted       Will archive to $ENV{ARCHIVE_ROOT}/NeverInserted
  
  This script archives the following directory structure:
						bin
						cxt
						etc
						orig/source_provider
						src
						insert
						trans (if exist)
	to 
	$ENV{ARCHIVE_ROOT}/SABS/RSAB/VSAB
	
	Unless it is a translation
	translation: $ENV{ARCHIVE_ROOT}/SABS/RSAB/trans/VSAB
	
  And create symbolic links.
};
	&PrintVersion("version");
	return 1;
}


