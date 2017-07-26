#!@PATH_TO_PERL@
#
# changes
# 02/12/2013 NEO: add -f parameter to dump a source family
# 06/28/2007 WAK (1-ELQTB): changes for subfields in MRSAB 
# 01/08/2007 TTN (1-D7H77): change source_official_name to SRC/RPT atom_name instead of the actual source_official_name from sims_info
# 12/01/2006 TTN (1-CDMK9): Initial version
#
# File:    dump_src_info.pl 
# Author:  Tun Tun Naing (11/2006)
#
#
# Usage: dump_src_info.pl [-d <db>] [<SAB> | -r <RSAB> | -f <SF>]
#
# (-r dumps the current source with RSAB=<RSAB>)
# (-f dumps all current sources with source family=<SF>)
#
$release="1";
$version="1.0";
$version_authority="NEO";
$version_date="02/12/2013";

unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";

#
# Check required environment variables
#
$badargs = 3 if (!($ENV{INV_HOME}));
$badargs = 4 if (!($ENV{ORACLE_HOME}));

#
# Turn on auto-flush
#
$| = 1;

#
# Set variables
#
$db = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s editing-db`;
chop($db);

#
# Parse arguments
#
while (@ARGV) {
  	$arg = shift(@ARGV);
  	if ($arg !~ /^-/) {
    	push @ARGS, $arg;
    	next;
   	}

    if ($arg eq "-version") {
		$print_version="version";
    }
    elsif ($arg eq "-v") {
		$print_version="v";
    }
    elsif ($arg eq "-help" || $arg eq "--help") {
		$print_help=1;
    }
    elsif ($arg =~ /^-d/) {
		$db = shift(@ARGV);
    } 
    elsif ($arg =~ /^-r/) {
		$rsab = shift(@ARGV);
    } 
    elsif ($arg =~ /^-f/) {
		$sf = shift(@ARGV);
    } 
    else {
		$badargs = 1;
		$badswitch = $arg;
    }
}

$userpw=`$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
chop($userpw);
($user,$password) = split /\//, $userpw;

#
# Print Help/Version info, exit
#
&PrintHelp && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

#
# Get arguments

if (scalar(@ARGS) == 1) {
  ($sab) = @ARGS;
} 

$badargs = 2 if $sab eq '' && $rsab eq '' && $sf eq '';
$badargs = 2 if ($sab and ($rsab or $sf));
$badargs = 2 if ($rsab and ($sab or $sf));
$badargs = 2 if ($sf and ($sab or $rsab));

#
# Print bad argument errors if any found
#
if ($badargs) {
    %errors = (1 => "Illegal switch: $badswitch",
	       2 => "Exactly one of SAB or -r RSAB or -f SF must be entered.",
	       3 => "\$INV_HOME must be set.",
	       4 => "\$ORACLE_HOME must be set."
 );
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
};


use DBI;
use DBD::Oracle;

#
# Program logic
#

$dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password")
   or die "Can't connect to Oracle database: $DBI::errstr\n";

$query = qq{
  SELECT a.source, rank, restriction_level, normalized_source, stripped_source,
  version, source_family, nlm_contact, acquisition_contact, content_contact,
  license_contact, inverter_contact, context_type, release_url_list, language,
  citation, license_info, character_set, rel_directionality_flag
  FROM sims_info a, source_rank b
  WHERE a.source = b.source 
    AND (b.source = ?
      OR b.source = (SELECT current_name FROM source_version WHERE source = ?)
      OR (source_family = ?
          AND b.source IN (SELECT distinct current_name from source_version)
         )
        )
};

$sh1 = $dbh->prepare( $query ) ||  ( die "Can't prepare statement: $DBI::errstr");

$sh1->execute($sab, $rsab, $sf) ||  die "Error executing ($query): $! $?\n";

my(@row) = ();
while ((@row = $sh1->fetchrow_array) > 0) {
  if ($sh1->rows == 0) {
    die "Can't find $rsab$sab$sf in $db";
  }	

  my ($source, $rank, $restriction_level, $normalized_source, $stripped_source,
    $version, $source_family, $nlm_contact, $acquisition_contact,
    $content_contact, $license_contact, $inverter_contact, $context_type,
    $release_url_list, $language, $citation, $license_info, $character_set,
    $rel_directionality_flag) = @row;

  $query = qq{
	SELECT string FROM string_ui a, classes b
		WHERE a.sui=b.sui AND source='SRC' AND tty='RPT'
			AND code='V-'|| ?
			AND tobereleased in ('Y','y')
  };

  $sh = $dbh->prepare( $query ) ||  ( die "Can't prepare statement: $DBI::errstr");

  $sh->execute($stripped_source) ||  die "Error executing ($query): $! $?\n";
  my ($source_official_name) = $sh->fetchrow_array;

  if ($sh->rows == 0) {
	die "Can't find the SRC/RPT $stripped_source in $db";
  }	

  $query = qq{
	SELECT MAX(source) FROM source_rank a WHERE source < ? and rank = ?
  };
  $sh = $dbh->prepare($query) ||  ( die "Can't prepare statement: $DBI::errstr");
  $sh->execute($source, $rank) ||  die "Error executing ($query): $! $?\n";

  my ($low_source) = $sh->fetchrow_array;

  if ($sh->rows == 0) {
	$query = qq{
		SELECT MAX(source) FROM source_rank a 
		WHERE rank = (SELECT MAX(rank) FROM source_rank 
			WHERE rank < ?)
	};
	$sh = $dbh->prepare($query) ||  ( die "Can't prepare statement: $DBI::errstr");
	$sh->execute($rank) ||  die "Error executing ($query): $! $?\n";

	$low_source = $sh->fetchrow_array;
  }

  print "-=-=-\n" if $doneone;
  print "source_name|$source\n";
  print "low_source|$low_source\n";
  print "restriction_level|$restriction_level\n";
  print "normalized_source|$normalized_source\n"; 
  print "stripped_source|$stripped_source\n"; 
  print "version|$version\n"; 
  print "source_family|$source_family\n"; 
  print "source_official_name|$source_official_name\n"; 
  print "nlm_contact|$nlm_contact\n"; 
  print "acquisition_contact|$acquisition_contact\n"; 

  #print "content_contact|$content_contact\n";
  # break up sub-fields
  @SCC_label = ("Name","Title","Organization","Address 1","Address 2","City","State/Prov.","Country","Zip","Telephone","Fax","Email","URL");
  @SCC = split(';',$content_contact);
  print "***Source Content Contact\n";
  foreach $i (0 .. 12){
	print "  SCC $SCC_label[$i]|$SCC[$i]\n";
  }
  print "- end of content contact\n";

  #print "license_contact|$license_contact\n"; 
  @SLC_label = ("Name","Title","Organization","Address 1","Address 2","City","State/Prov.","Country","Zip","Telephone","Fax","Email","URL");
  @SLC = split(/;/,$license_contact);
  print "***Source License Contact\n";
  foreach $i (0 .. 12){
	print "  SLC $SLC_label[$i]|$SLC[$i]\n";
  }
  print "- end of license contact\n";

  print "inverter_contact|$inverter_contact\n"; 
  print "context_type|$context_type\n"; 
  print "release_url_list|$release_url_list\n"; 
  print "language|$language\n"; 

  #print "citation|$citation\n";
  @SCIT_label = ("Author name(s)","Personal author address","Organization author(s)","Editor(s)","Title","Content Designator","Medium Designator","Edition","Place of Pub.","Publisher","Date of pub. or copyright","Date of revision","Location","Extent","Series","Avail. Statement (URL)","Language","Notes"),
  @SCIT = split(/;/,$citation);
  print "***Citation\n";
  foreach $i (0 .. 19){
	print "  Cite $SCIT_label[$i]|$SCIT[$i]\n";
  }
  print "- end of citation\n";

  print "license_info|$license_info\n"; 
  print "character_set|$character_set\n";
  print "rel_directionality_flag|$rel_directionality_flag\n";
  $doneone = 1;
}

exit 0;


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
 Usage: dump_src_info.pl [-d <db>] [<SAB> | -r <RSAB> | -f <SF>]
    };
}

sub PrintHelp {
    &PrintUsage;
    print qq{

  Options:
         -d <database>:         Specify alternate database
         <SAB>:					Specify versioned source abbreviation
         -r <RSAB>:				Specify root source abbreviation
         -f <SF>:               Specify source family
         -[-]help:              On-line help
};
    &PrintVersion("version");
    return 1;
}
