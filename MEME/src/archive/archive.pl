#!@PATH_TO_PERL@
#
# Archives /umls_prod/dist_root data into archive.
#
# input: <top-level production dir> <release>
# output: <new structure in $ARCHIVE_HOME>
#
# 10/28/2008 BAC (1-J93RX): First version
#
BEGIN {
 unshift @INC, "$ENV{ENV_HOME}/bin";
 require "env.pl";
 unshift @INC, "$ENV{ARCHIVE_HOME}/lib";
}
use strict 'vars';
use strict 'subs';

#
# Parse arguments
#
$| = 1;
our $printVersion = 0;
our $printHelp = 0;
our @ARGS;
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
our $dir = "";
our $release = "";
if ( scalar(@ARGS) == 2 ) {
 ( $dir, $release ) = @ARGS;
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
print "Dir:         $dir\n";
print "Release:     $release\n";
print "    Create archive dirs ...", scalar(localtime), "\n";
UmlsArchive::configure($dir, $release);
if (-e "$ENV{UMLS_ARCHIVE_ROOT}/$release") {
    die "$ENV{UMLS_ARCHIVE_ROOT}/$release already exists\n" .
        "Please remove it before running this script\n";
}
UmlsArchive::createDirectories();

print "    Copy original media ...", scalar(localtime), "\n";
UmlsArchive::copyOriginal();

print "    Copy URIS counts...", scalar(localtime), "\n";
UmlsArchive::copyUris();

print "    Copy production QA info...", scalar(localtime), "\n";
UmlsArchive::copyProductionQa();

print "    Copy production log info...", scalar(localtime), "\n";
UmlsArchive::copyProductionLogs();

print "    Create full RRF...", scalar(localtime), "\n";
UmlsArchive::createFullSubset("RRF");

print "    Create RRF MRCXT", scalar(localtime), "\n";
UmlsArchive::buildMrcxtRRF();

print "    Create full ORF...", scalar(localtime), "\n";
UmlsArchive::createFullSubset("ORF");


print qq{-------------------------------------------------
Finished ... }, scalar(localtime), qq{
-------------------------------------------------
};
exit 0;
####### Local Procedures #######
sub PrintVersion {
 my ($type) = @_;
 print "No version info\n";
 return 1;
}

sub PrintUsage {
 print qq{ This script has the following usage:
 Usage: archive.pl <dir> <release>
    };
}

sub PrintHelp {
 &PrintUsage;
 print qq{
  dir:          Top level dist_root directory for production
  release:      release name to archive

  This script copies moves the specified release from the
  production area to the archive area: \$UMLS_ARCHIVE_ROOT

 };
 &PrintVersion("version");
 return 1;
}
