#!@PATH_TO_PERL@
#
# Runs the following steps to regenerate or augment the $UMLS_ARCHIVE_ROOT/global data
#
# * Add new entries to global MRSAB file
# * Add new entries to global word/string index files
# * Update global UI/STR tables and add additional release bit, add entries for new UIs or strings
# * Update the global release bit positions file
# * Obtain the latest semantic groups file
# * Obtain latest copy of semantic_types table from MID
# * Compute grouping data for new release and append to grouping files
# * Compute miscellaneous counts for new release and append to miscellaneous counts file
#
# input: <release>
# output: <new data added to $UMLS_ARCHIVE_ROOT/global
#
# 01/20/2009 BAC (1-J93RX): First version
#

BEGIN {
 unshift @INC, "$ENV{ENV_HOME}/bin";
 require "env.pl";
 unshift @INC, "$ENV{ARCHIVE_HOME}/lib";
}
use strict 'vars';
use strict 'subs';
use open ':utf8';

#
# Parse arguments
#
$| = 1;
our @ARGS;
our $printHelp = 0;
our $printVersion = 0;
while (@ARGV) {
 my $arg = shift(@ARGV);
 push( @ARGS, $arg ) && next unless $arg =~ /^-/;
 if ( $arg eq "-version" ) {
  $printVersion = "version";
 } elsif ( $arg eq "-v" ) {
  $printVersion = "v";
 } elsif ( $arg eq "-help" || $arg eq "--help" ) {
  $printHelp = 1;
 } else {
  $printHelp = 1;
 }
}

our $release = "";
if ( scalar(@ARGS) == 1 ) {
 ( $release ) = @ARGS;
} else {
 $printHelp = 1;
}

#
# Print Help/Version info, exit
#
&PrintHelp && exit(0) if $printHelp;
&PrintVersion($printVersion) && exit(0) if $printVersion;

#
# Program logic
#
use UmlsArchive;
print "-------------------------------------------------------\n";
print "Starting ...", scalar(localtime), "\n";
print "-------------------------------------------------------\n";
print "Release:     $release\n";
print "    Configure ...", scalar(localtime), "\n";
UmlsArchive::setRelease($release);

print "    Backup global files ...", scalar(localtime), "\n";
system "/bin/cp -f $ENV{UMLS_ARCHIVE_ROOT}/global/*txt $ENV{UMLS_ARCHIVE_ROOT}/global/backup";
if ($? != 0) {
 die "Error backing up global files: $! $?\n";
}

# * Add new bit pos entry to global bitpos file
print "    Add new entry to global BitPos file ...", scalar(localtime), "\n";
UmlsArchive::updateGlobalReleaseBitPos();

# * Add new entries to global MRSAB file
print "    Add new entries to global MRSAB file ...", scalar(localtime), "\n";
UmlsArchive::updateGlobalMRSAB();

# * Add new entries to global word/string index files
print "    Add new entries to global word/string index files ...", scalar(localtime), "\n";
UmlsArchive::updateGlobalIndexes();

# * Update global UI/STR tables and add additional release bit, add entries for new UIs or strings
print "    Add new entries to global UI/STR files ...", scalar(localtime), "\n";
UmlsArchive::updateGlobalUI();

# * Obtain the latest semantic groups file
print "    Update SemGroups.txt file ...", scalar(localtime), "\n";
UmlsArchive::updateSemGroups();

# * Compute grouping data for new release and append to grouping files
print "    Compute grouping data and miscellaneous counts...", scalar(localtime), "\n";
UmlsArchive::updateRRFStatistics();
UmlsArchive::updateORFStatistics();

print "-------------------------------------------------\n";
print "Finished ... ", scalar(localtime),"\n";
print "-------------------------------------------------\n";

exit 0;
####### Local Procedures #######
sub PrintVersion {
 my ($type) = @_;
 print "No version info\n";
 return 1;
}

sub PrintUsage {
 print qq{ This script has the following usage:
 Usage: global.pl <release>
    };
}

sub PrintHelp {
 &PrintUsage;
 print qq{
  release:      Release name to generate global data for

  This script takes an archived directory and generates/
  refreshes \$UMLS_ARCHIVE_ROOT/global data for that release.

 };
 &PrintVersion("version");
 return 1;
}
