#!/site/bin/perl5
#
# Wrapper script to generate comparable ORF
# from generated ORF.
#
# input: <Archive release version, e.g 2008AA>
# output: $UMLS_ARCHIVE_ROOT/$input/Comparable/ORF/META dir
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

# Make Metadata
print "    Build comparable ORF metadata ...", scalar(localtime), "\n";
UmlsArchive::buildComparableMetadataORF("Full/ORF/META","Comparable/ORF/META");

# Make Content
print "    Build comparable ORF content ...", scalar(localtime), "\n";
UmlsArchive::buildComparableContentORF("Full/ORF/META","Comparable/ORF/META");

# Make Content
print "    Build comparable ORF history ...", scalar(localtime), "\n";
UmlsArchive::buildComparableHistoryORF("Full/ORF/META","Comparable/ORF/META");

# Make MRCXT
print "    Build MRCXT ...", scalar(localtime), "\n";
UmlsArchive::buildComparableMrcxtFromRRF("Full/RRF/META","Comparable/ORF/META");

# Build comparable indexes
print "    Build comparable ORF indexes ...", scalar(localtime), "\n";
UmlsArchive::buildComparableIndexes("ORF","Comparable/ORF/META");

# Build comparable AMBIG files
print "    Build comparable ORF ambig files ...", scalar(localtime), "\n";
UmlsArchive::buildComparableAmbig("ORF","Comparable/ORF/META");

# Build comparable MRCOLS/MRFILES
print "    Build comparable ORF MRCOLS/MRFILES ...", scalar(localtime), "\n";
UmlsArchive::buildComparableColsFiles("ORF","Comparable/ORF/META");

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
 Usage: compOrf.pl <release>
    };
}

sub PrintHelp {
 &PrintUsage;
 print qq{
  release:      Release name

  This script makes comparable ORF in
    \$UMLS_ARCHIVE_ROOT/<release>/Comparable/ORF/META

 };
 &PrintVersion("version");
 return 1;
}
