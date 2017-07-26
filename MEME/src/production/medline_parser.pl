#!@PATH_TO_PERL@
#
# File:     medline_parser.pl
# Author:   Brian Carlsen (2002)
#
# Changes:
#  06/09/2006 TTN (1-BFPC3): add medline_info entries to meme_properties for release.dat
#  03/01/2006 BAC (1-AIDWZ): $year changed to 1776 from 1910
#
$release = "4";
$version = "2.0";
$version_date = "08/28/2002";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use DBI;
use DBD::Oracle;

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
    elsif ($arg =~ /^-db=(.*)$/) {
	$db = $1;
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
$badargs = 5 if (!$db);
$badargs = 6 if (!$mode);
$badargs = 7 if (!$release_date);

#
# Print bad argument errors if any found
#
if ($badargs) {
    %errors = (1 => "Illegal switch: $badswitch",
	       2 => "Wrong number of arguments: $badopt",
	       3 => "\$MEME_HOME must be set.",
	       5 => "Required switch -db not used.",
	       6 => "Either the -u or -i option must be specified.",
	       7 => "Required switch -release_date not used.",
	       4 => "\$ORACLE_HOME must be set.");
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
};

#
# Configure java version and start class
#
$java = "$ENV{JAVA_HOME}/bin/java -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl -Xms200M -Xmx800M -server";
$class = "gov.nih.nlm.meme.xml.MedlineHandler";

#
# Edit classpath
#

# Add classes
$ENV{CLASSPATH} = "$ENV{EXT_LIB}/ojdbc14.jar:$ENV{MEME_HOME}/lib/memeUtil.jar:$ENV{EXT_LIB}/xerces.jar:$ENV{MEME_HOME}/lib/memeXml.jar:$ENV{MEME_HOME}/lib/memeException.jar:$ENV{MEME_HOME}/lib/memeMeme.jar:$ENV{CLASSPATH}"
    unless $ENV{CLASSPATH} =~ /$ENV{MEME_HOME}/;

#
# look up user/password
#
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
chop($userpass);
($user,$password) = split /\//, $userpass;

$|=1;

print "-----------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-----------------------------------------------------\n";
print "database: $db\n";
print "year:     $year\n";
print "mode:     $mode\n";

#
# Determine machine/SID for $db
#
if ($db =~ /^(.*)-(.*)$/) {
  $db = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s $db`;
  chop($db);}
if ($db =~ /^(.*)_(.*)$/) { $machine="$1.nlm.nih.gov"; $sid="$2" }

#
# Make the call
#
open (CMD,qq{$java $class $machine $sid $user $password $year $release_date $mode @ARGS|}) ||
  die "Could not open command: $! $?\n";
while (<CMD>) {
  print;
};

if ($? >> 8) {
    print "ERROR parsing medline data.";
    exit($? >> 8);
  }

close(CMD);

$file = `ls medline*.xml | tail -1`;
($month,$day,$year)=split /\//,$release_date;
# open connection
$dbh = DBI->connect("dbi:Oracle:$db",$user,$password)
  || die "Could not connect to $db: $! $?\n";

$dbh->do(qq{ DELETE FROM meme_properties WHERE KEY_QUALIFIER = 'MEDLINE_INFO'});

$dbh->do(qq{ INSERT INTO meme_properties (key, key_qualifier,
  value)
  VALUES (?,?,?)
  }, undef, 'umls.medline.date', 'MEDLINE_INFO',$year.$month.$day) || die
 qq{Can not insert medline date info: \n};

$dbh->do(qq{ INSERT INTO meme_properties (key, key_qualifier,
  value)
  VALUES (?,?,?)
  }, undef, 'umls.medline.file', 'MEDLINE_INFO',$file) || die
 qq{Can not insert medline file info : \n};
 
 $dbh->disconnect;

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
        medline_parser.pl -db=<database> [-year=<year>] -release_date=<date> [-u|-i] <file list>
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
        -db=....                Name of a midsvcs.pl database service
	                        e.g. "editing-db"
	                        The database is used to look up the atom ids
			        matching the various MSH strings found in the
			        Medline files.
        -year=....:             Use of this switch will restrict the output
	                        to Medline records whose publication date
			        is equal to or after the specified year.
        -release_date=....:     Use of this switch will restrict the output
	                        to Medline records whose publication date
			        is equal to or before the specified date.
                                e.g. "10/01/2005"
        -v[ersion]:	        Print version information
        -[-]help:	        On-line help
	-u      :               Update data
	-i      :               Initialize data
	                        Either the -u or -i option must be specified.
};
    &PrintVersion("version");
    return 1;
}







