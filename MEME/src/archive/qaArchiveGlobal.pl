#!/site/bin/perl5
#
# Validates an archive directory structure.
#
# input: <Archive release version, e.g 2008AA>
# output: QA Report (text) to STDOUT
#
BEGIN {
 unshift @INC, "$ENV{ENV_HOME}/bin";
 require "env.pl";
 unshift @INC, "$ENV{ARCHIVE_HOME}/lib";
}
use strict 'vars';
use strict 'subs';

#
# Set Defaults & Environment
#
$| = 1;
our $badargs  = 0;
our $badvalue = "";
unless ( $ENV{UMLS_ARCHIVE_ROOT} ) {
 $badvalue = "UMLS_ARCHIVE_ROOT";
 $badargs  = 4;
}

#
# Check options
#
our @ARGS = ();
while (@ARGV) {
 our $arg = shift(@ARGV);
 if ( $arg !~ /^-/ ) {
  push @ARGS, $arg;
  next;
 }
 if ( $arg eq "-help" || $arg eq "--help" ) {
  &PrintHelp;
  exit(0);
 } else {
  $badargs = 1;
 }
}

#
# Get command line params
#
our $release = "";
if ( scalar(@ARGS) == 1 ) {
 ($release) = @ARGS;
 $release = uc($release);
} else {
 $badargs  = 3;
 $badvalue = scalar(@ARGS);
}

#
# Process errors
#
our %errors = (
                1 => "Illegal switch: $badvalue",
                3 => "Bad number of arguments: $badvalue",
                4 => "$badvalue must be set"
);
if ($badargs) {
 &PrintUsage;
 print "\n$errors{$badargs}\n";
 exit(1);
}


#
# Program logic
#
$| = 1;
use UmlsArchive;
print "-------------------------------------------------------\n";
print "Starting ...", scalar(localtime), "\n";
print "-------------------------------------------------------\n";
print "Release:     $release\n";
UmlsArchive::setRelease($release);
#
# Get bit position for this release
#
my $BITPOS = UmlsArchive::getGlobalBitPos($release);
print "Bit Pos:     $BITPOS\n";

print "    Verify Full RRF UIs ...", scalar(localtime), "\n";
UmlsArchive::verifyRrfUis("Full/RRF/META");

print "    Verify Full RRF STY groups ...", scalar(localtime), "\n";
UmlsArchive::verifyRrfStyGroups("Full/RRF/META");

print "    Verify Comparable RRF Grouping Data ...", scalar(localtime), "\n";
UmlsArchive::verifyRrfGroupingData("Comparable/RRF/META");

print "    Verify Full ORF UIs ...", scalar(localtime), "\n";
UmlsArchive::verifyOrfUis("Full/ORF/META");

print "    Verify Full ORF STY groups ...", scalar(localtime), "\n";
UmlsArchive::verifyOrfStyGroups("Full/ORF/META");

print "    Verify Comparable ORF Grouping Data ...", scalar(localtime), "\n";
UmlsArchive::verifyOrfGroupingData("Comparable/ORF/META");

print "--------------------------------------------------------\n";
print "Finished ... " . scalar(localtime) . "\n";
print "--------------------------------------------------------\n";
exit(0);

######################### LOCAL PROCEDURES #######################
 sub PrintUsage {
  print qq{ This script has the following usage:
    $0: <release>
};
 }

 sub PrintHelp {
  &PrintUsage;
  print qq{
 Performs QA checks on an archive dir to validate against \$UMLS_ARCHIVE_ROOT/global.

 Options:
       -[-]help:            On-line help

 Arguments:
       release:             The UMLS release

};
 }
