#!@PATH_TO_PERL@
#
# This script is a wrapper around the ConceptRanker java
# class that computes the TS, STT, ISPREF and LRL/SRL fields
# on an ORF or RRF directory (which must contain at least MRCON,
# MRSO, MRFILES, MRRANK, and MRSAB, or for RRF, MRCONSO, MRFILES,
# MRDOC, MRRANK, and MRSAB).  The main purpose of this script
# is to show you how to set up the environment needed to allow
# the ConceptRanker java class to function properly.
#
# It can produce either ORF MRCON.out and MRSO.out files or an
# RRF MRCONSO.out file.
#
# You should check each of the settings before the
# "Call ConceptRanker.java" section to ensure that
# they conform to your environment. 
 
# Set the java command and lvg dir
# The java command must invoke a version 1.2 or greater JVM.
$java = "$ENV{JAVA_HOME}/bin/java -server -Xms100M -Xmx400M";
 
# Set META directory
$meta_dir = $ARGV[0];
 
# Set input format (ORF or RRF) of files in $meta_dir
$input_format = "ORF";
 
# Set output format (ORF or RRF) of *.out files to be created in $meta_dir
$output_format = "ORF";

# Add . to the classpath
$ENV{CLASSPATH} = "$ENV{ARCHIVE_HOME}/lib/archive.jar:$ENV{ARCHIVE_HOME}/lib/mms.jar:$ENV{ARCHIVE_HOME}/lib/objects.jar";

# Call ConceptRanker.java
print "----------------------------------------------------\n";
print "Starting ... ",scalar(localtime),"\n";
print "----------------------------------------------------\n";
print "META directory: $meta_dir\n";
print "input format:   $input_format\n";
print "output format:  $output_format\n";
print "CLASSPATH:      $ENV{CLASSPATH}\n";
print "\n";
print qq{This script will take the MRCON/SO input files and
apply the ConceptRanker TS/STT/ISPREF/LRL/SRL computation
algorithm and write the results to MRCON/SO.out.  Any errors 
should be written to standard error.
};
 
$x = system("$java gov.nih.nlm.umls.archive.ConceptRanker -i $input_format -o $output_format $meta_dir > /dev/null");
if ($x != 0) {
  print "Error running ConceptRanker.\n\n";
}
 
print "----------------------------------------------------\n";
print "Finished ... ",scalar(localtime),"\n";
print "----------------------------------------------------\n";
