#!/site/bin/perl58
#
# File:     medline_parser.pl
# Author:   Brian Carlsen (2002)
#
# Changes:
#  03/01/2006 BAC (1-AIDWZ): $year changed to 1776 from 1910
#
$release = "4";
$version = "2.0";
$version_date = "08/28/2002";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
#
# load lib/ jar files
#
opendir (LIB,"$ENV{MEME_HOME}/lib") ||
  die "Could not open $ENV{MEME_HOME}/lib: $! $?\n";
@f = readdir(LIB);
close(LIB);
foreach $file (@f) {
  if ($file =~ /\.jar/) { $ENV{CLASSPATH} .= ":$ENV{MEME_HOME}/lib/$file"; }
  if ($file =~ /\.zip/) { $ENV{CLASSPATH} .= ":$ENV{MEME_HOME}/lib/$file"; }
}

#
# load lib/ jar files
#
opendir (LIB,"$ENV{EXT_LIB}") ||
  die "Could not open $ENV{EXT_LIB}: $! $?\n";
@f = readdir(LIB);
close(LIB);
foreach $file (@f) {
  if ($file =~ /\.jar/) { $ENV{CLASSPATH} .= ":$ENV{EXT_LIB}/$file"; }
  if ($file =~ /\.zip/) { $ENV{CLASSPATH} .= ":$ENV{EXT_LIB}/$file"; }
}

#
# Parse arguments
#
$year = "1776";
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
    elsif ($arg =~ /^-year=(.*)$/) {
	$year = $1;
    }
    elsif ($arg eq "-u") {
	$mode = "update";
    }
    elsif ($arg eq "-i") {
	$mode = "init";
    }
    elsif ($arg =~ /^-release_date=(.*)$/) {
	$release_date = $1;
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
# No required arguments

$badargs = 3 if (!($ENV{MEME_HOME}));
$badargs = 4 if (!($ENV{ORACLE_HOME}));
$badargs = 6 if (!$mode);
$badargs = 7 if (!$release_date);

#
# Print bad argument errors if any found
#
if ($badargs) {
    %errors = (1 => "Illegal switch: $badswitch",
	       2 => "Wrong number of arguments: $badopt",
	       3 => "\$MEME_HOME must be set.",
	       6 => "Either the -u or -i option must be specified.",
	       7 => "Required switch -release_date not used.",
	       4 => "\$ORACLE_HOME must be set.");
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
};
if ($mode eq "update") {
	$update_dir="$ENV{MEDLINE_DIR}/update";
	chdir($update_dir);
} else {
	chdir($ENV{MEDLINE_DIR});
}
#
# Configure java version and start class
# MODIFED from gov.nih.nlm.meme.xml.MedlineHandler  to gov.nih.nlm.meme.xml.MedlineHandlerWithNoDB  SOMA LANKA
$java = "$ENV{JAVA_HOME}/bin/java -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl -Xms200M -Xmx800M -server";
$class = "gov.nih.nlm.meme.xml.MedLineHandlerWithNoDB";

#
# Edit classpath
#
# Add classes
$ENV{CLASSPATH} = "$ENV{MEME_HOME}/java/xercesImpl.jar:$ENV{MEME_HOME}/java/meme.jar:$ENV{CLASSPATH}"
    unless $ENV{CLASSPATH} =~ /$ENV{MEME_HOME}/;
#
# look up user/password
#
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
chop($userpass);
($user,$pass) = split /\//, $userpass;

$|=1;

print "-----------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-----------------------------------------------------\n";
print "year:     $year\n";
print "mode:     $mode\n";


#
# Date replacement pattern file (NO LONGER USED)
#
$file = "$ENV{MEME_HOME}/bin/medline.prop";

#
# Make the call
#
open (CMD,qq{$java $class $year $release_date $mode $file @ARGS|}) || 
  die "Could not open command: $! $?\n";
while (<CMD>) {
  print;
};
close(CMD);

if ($? >> 8) {
    print "ERROR parsing medline data.";
    exit($? >> 8);
  }

print "-----------------------------------------------------\n";
print "Finished ...",scalar(localtime),"\n";
print "-----------------------------------------------------\n";

exit(0);


######################### LOCAL PROCEDURES #######################

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
        medline_parser.pl [-year=<year>] -release_date [-u|-i] <file list>
    };
}

sub PrintHelp {
    &PrintUsage;
    print qq{
   This script uses MedlineHandler.java to parse the Medline XML format
   It produces two files
      coc_headings.dat: data for the Descriptors.  The fields are,
      citation_set_id (PMID), publication date, atom_id, D or Q,
      the heading major topic flag, the subheading_set_id, the
      subheading major topic flag, the source (current MSH taken from)
      and the database named and finally the coc_type (not used here). 
      For example,

           90108271|01-Jan-1990|4389203|D|N||N|MSH2001||

      coc_subheadings.dat: data for the SubHeadigns.  the fields are
      citation_set_id, subheading_set_id, QA value, major topic flag.
      For example,

           90108271|0|BL|Y|
           90110001|0|MT|Y|

    It also produces additional file in Update mode. 
      coc_headings_todelete.dat: data for deleted and other citation set ids.
      For example,

           90108271
           90110001

   First load the todelete file in the MID and delete any rows from 
   coc_headings/coc_subheadings where a citation_set_id is in todelete file.

   The new coc_headings and coc_subheadings files can be loaded into the 
   coc_headings and coc_subheadings tables in the MID.  
   They join on (citation_set_id,subheading_id).

    Options:
        -year=....:     Use of this switch will restrict the output
	                to Medline records whose publication date
			is equal to or after the specified year.
		-releasedate : Use of this switch will restrict the output
	                        to Medline records whose publication date
			        is equal to or before the specified date.
                                e.g. "10/01/2005"
        -v[ersion]:	Print version information
        -[-]help:	On-line help
	-u      :       Update data
	-i      :       Initialize data
	                Either the -u or -i option must be specified.
};
    &PrintVersion("version");
    return 1;
}
