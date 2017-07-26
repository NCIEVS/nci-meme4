#!@PATH_TO_PERL@

# for usage instructions run as
# genMappingAttrs.pl -h

# To Do:
# charent conversions don't work correctly and are disconnected

# Changes
# mar-30-2007 WAK
# MAPSETSID value is used as the CODE. All attributes are
# CODE_TERMGROUP. Users are instructed to use the source provided
# MAPSETSID in the template file. If there is no source provided
# MAPSETSID, the code of the XM atom is used.

# sept-05-2006 SSL
# added handling for "EXPR" in FROMIDCHOICE and TOIDCHOICE (documented
# but no functionality existed for this)
# UMLSMAPSETSEPARATOR is MTH_ attribute


# nov-14-2005 BK
# certain MTH attributes are now attributed to the source and 
# are renamed 'MTH_...'. 
# see 'mrmap.txt' for more details

# rel is now required in the map data file

# may-19-2005 BK
# changed the source of the STY becomes 'E-MTH'. This was done to solve the
# problem of having a 20 char. SAB for MEDLINEPLUS_20040814, adding the 'E-'
# prefix and ending up with a 22 char source value, exceeding the limit for
# this field

# may-18-2005 BK
# added Jan's report. created as <name_of_mapping_file>.rpt

# doc
# here's what happens:
# - read the options and do error checking
# - read the config file and do some checking
# - read the map file and create a hash of all the froms's and to's
#	we'll use this later to built XMAPFROM/XMAPTO attributes
# - create the metadata attributes for source owned and MTH owned attribs
# - create the attributes for XMAPFROM/XMAPTO
# - create the mapping attributes.

# to do:
# - write a conversion of mapsetseparatorcode to umlsmapseparator.
#   used if the input file has one separator and the attribute file another
# 
# - gen SIDs?
# 
# - 

$ENV{"LANG"} = "en_US.UTF-8";
$ENV{"LC_COLLATE"} = "C";
use Getopt::Std;
use Digest::MD5 qw(md5_hex);

%valid_rel = (
AQ => 1,
CHD => 1,
DEL => 1,
PAR => 1,
QB => 1,
RB => 1,
RL => 1,
RN => 1,
RO => 1,
RQ => 1,
RU => 1,
SIB => 1,
SUBX => 1,
SY => 1,
XR => 1,
);

# All Attributes must be in either this array or the MTH_ATTR array
@SRC_ATTR = qw(MAPSETNAME MAPSETTYPE MAPSETSCHEMEID MAPSETSCHEMEVERSION
MAPSETREALMID MAPSETSEPARATORCODE MAPSETRULETYPE TARGETSCHEMEID MAPSETSID MAPSETGRAMMAR MTH_UMLSMAPSETSEPARATOR FROMVSAB TOVSAB FROMRSAB TORSAB MAPSETVSAB MAPSETRSAB MAPSETVERSION SOS MTH_MAPSETCOMPLEXITY MTH_MAPTOCOMPLEXITY MTH_MAPFROMCOMPLEXITY MTH_MAPFROMEXHAUSTIVE MTH_MAPTOEXHAUSTIVE );

@MTH_ATTR = qw();

# s - SAID ?? needed ??
# c - config file, required
# m - mapping file, required

# options
# a	assign an attribute id
# c	name the config file
# d	display to terminal not file
# h	display help
# i	supply a attrib_id
# m	mapping file, use to declare the mapping file on the command line
# o   specify an attributes out file
# s	unused
# t	Test/Display the mapping file instead of printing to the attributes file
#		pipe output through 'more' or 'less'
# v	Verbose mode, display Config options and quit
# z	unpublished, for debugging

print STDERR "...Reading options\n";
getopts("a:c:i:s:m:o:dhtvz", \%opt);		# get options
&parse_options();

if($opt{'z'}){
	&convert_charent_table();
	exit;
}

print STDERR "...Reading config file\n";
if(&parse_conf($conf_file)){
	print STDERR "...Config file parsed\n";
}
else{
	print STDERR "\n";
	print STDERR "   *** Fatal Error in the config file ***\n";
	print STDERR "\n";
	exit;
}



# for diagnostics
if($opt{'v'}){ print STDERR "Attribute IDs start at: $attr_id\n" }

# unless the map file was assigned via the command line
unless($map_file){
	if($map_file = $conf{'MAPPINGFILENAME'}){}
	else{
		print "\n\tError - no mapping file specified\n\n";
		&print_usage();
		exit 1;
	}
}

open(MAP, $map_file) or die "Can't open $map_file, $!";

print STDERR "...reading mapping file\n";

if(0){
print "FROM Atoms\n";
foreach $x (keys(%ATOM_F)){
	printf "%-25s  %-45s\n",$x,$ATOM_F{$x};
	#print "$x $ATOM_F{$x}\n";
}
print "TO Atoms\n";
foreach $x (keys(%ATOM_T)){
	printf "%-25s  %-45s\n",$x,$ATOM_T{$x};
}
}

print STDERR "...making attributes\n";
&make_attribs();
#print STDERR "\n$dup Duplicates supressed\n\n";

# make report file
$rpt_file = $map_file.".rpt";
open(RPT, ">$rpt_file") or die "Can't open/write $map_file.rpt";
print "See Report File: $rpt_file\n";
$date = `date`;
$login = getlogin || (getpwuid($<)) [0] || "Intruder!!";
@user = getpwnam($login);
$dir = `pwd`;
chomp $dir;
print RPT "Mapping Report\n";
print RPT "Mapping attributes created: $date\n";
printf RPT "%-13s %-40s\n", "Inverter", $user[5];
printf RPT "%-13s %-40s\n", "Working dir", $dir;
printf RPT "%-13s %-40s\n", "Config file", $conf_file;
printf RPT "%-13s %-40s\n", "Map file", $map_file;
print RPT "\n";
print RPT "Configuration Values:\n";

foreach $x (keys(%conf)){
	if($conf{$x}){
		# print "$x $conf{$x}\n";
		printf RPT "%-20s  %-45s\n",$x,$conf{$x};
	}
	if($opt{'v'}){ printf STDERR "%-30s  %-45s\n",$x,$conf{$x} }
}
print RPT "\n";

print "Done\n";
# End of main()

##################################
#### ----- Subroutines ----- #####
##################################
sub make_attribs(){
	$dup = 0;
	# if the mapset_separator_code and UMLS_mapset_separator are used
	# process the conversion.
	if(($conf{'MAPSETSEPARATORCODE'})&&($conf{'MTH_UMLSMAPSETSEPARATOR'})){
		#&convert_charent_table();
		$charent++;
		$mapsetsep = $charent{$conf{'MAPSETSEPARATORCODE'}};
		$umlssep = $conf{'MTH_UMLSMAPSETSEPARATOR'};
		if(($umlssep eq '|') || ($umlssep eq '~')){
			print STDERR "Error: You can't specify '$umlssep' as a delimiter in either the MAPSETSEPARATORCODE or the UMLSMAPSETSEPARATOR\n";
			print STDERR "Change UMLSMAPSETSEPARATOR in $conf_file and try again\n\n";
			exit(1);
		}
		print STDERR "\nConverting MAPSETSEPARATORCODE '$mapsetsep' to '$umlssep'\n\n";

	}
	elsif(($conf{'MAPSETSEPARATORCODE'}eq "" )&&($conf{'MTH_UMLSMAPSETSEPARATOR'})){
		print "UMLSMAPSETSEPARATOR is set but MAPSETSEPARATORCODE is not \n";
		print "You do not need to set this unless you have multi-valued \n";
		print "mappings. See the discussion of UMLSMAPSETSEPARATOR in your\n";
		print "template file\n";
		print "Fix the config file and re-run\n\n";
		exit;
	}
	elsif(($conf{'MAPSETSEPARATORCODE'})&&($conf{'MTH_UMLSMAPSETSEPARATOR'} eq "")){
		print "UMLSMAPSETSEPARATOR is not set but MAPSETSEPARATORCODE is\n";
		print "You do not need to set this unless you have multi-valued \n";
		print "mappings. See the discussion of MAPSETSEPARATORCODE in your\n";
		print "template file\n";
		print "Fix the config file and re-run\n\n";
		exit;
	}
	elsif(($conf{'MAPSETSEPARATORCODE'}eq "" )&&($conf{'MTH_UMLSMAPSETSEPARATOR'} eq "")){
		# this is probably the most common situation

	}
	else{}
	
	open(ATTR_MAP, ">$outfile") or die "Can't open/write $outfile, $!";

	$source = $conf{'MAPSETVSAB'};
	$at_stat = 'R';
	$at_tbr = 'Y';
	$at_rel = 'N';
	$at_supp = 'N';
	$id_type = 'CODE_TERMGROUP';
	$id_qual = $conf{'MAPSETVSAB'}."/XM";
	$src_atui = '';

	# make a STY attribute
	$at_lev = 'C';
	$at_stat = 'N';
	$at_id = $CODE;

	$attr_name = "SEMANTIC_TYPE";
	$attr_val = "Intellectual Product";
	$hashcode = md5_hex("Intellectual Product");
	$source = "E-MTH";
	&print_attr(ATTR_MAP);
	$source = $conf{'MAPSETVSAB'};

	# everything else is source level
	$at_lev = 'S';
	$at_stat = 'R';

#	# make the SOS attribute
#	if($attr_val = $conf{'SOS'}){
#		$attr_name = "SOS";
#		$hashcode = md5_hex($attr_val);
#		&print_attr(ATTR_MAP);
#	}

#	# make the MAPSETCOMPLEXITY attribute
#	if($attr_val = $conf{'MAPSETCOMPLEXITY'}){
#		$attr_name = "MAPSETCOMPLEXITY";
#		$hashcode = md5_hex($attr_val);
#		&print_attr(ATTR_MAP);
#	}

	# make the FROMCOMPLEXITY attribute
	if($attr_val = $conf{'FROMCOMPLEXITY'}){
		$attr_name = "FROMCOMPLEXITY";
		$hashcode = md5_hex($attr_val);
		&print_attr(ATTR_MAP);
	}

	# make the TOCOMPLEXITY attribute
	if($attr_val = $conf{'TOCOMPLEXITY'}){
		$attr_name = "TOCOMPLEXITY";
		$hashcode = md5_hex($attr_val);
		&print_attr(ATTR_MAP);
	}

	$attr_name = 'MAPSETNAME';

	# make the SOURCE owned attributes
	foreach $i (@SRC_ATTR){
		if($attr_val = $conf{$i}){
			$attr_name = $i;
			$hashcode = md5_hex($attr_val);
			&print_attr(ATTR_MAP);
		}
	}

	# make the MTH owned attributes
	$source = 'MTH';
	foreach $i (@MTH_ATTR){
		if($attr_val = $conf{$i}){
			$attr_name = $i;
			$hashcode = md5_hex($attr_val);
			&print_attr(ATTR_MAP);
		}
	}

	$source = $conf{'MAPSETVSAB'};

	# make the XMAPTO/XMAPFROM attributes
	# ID: Identifier mapped to
	# SID: Source asserted identifier mapped to
	# EXPR: Expression mapped to
	# TYPE: Type of mapped to expression
	#	For a list of valid values see the MRDOC editor:
	# 	http://unimed.nlm.nih.gov/webapps-meme/meme/controller?state=MRDOCEditor
	# specifically the FROMTYPE and TOTYPE pages:
	# http://unimed.nlm.nih.gov/webapps-meme/meme/controller?state=ListMRDOC&entry=TOTYPE%7Eexpanded_form
	# and
	# http://unimed.nlm.nih.gov/webapps-meme/meme/controller?state=ListMRDOC&entry=FROMTYPE%7Eexpanded_form
	# RULE: Machine processable rule for when this "mapped from/to" is valid
	# RES: Restriction on when this "mapped from/to" should be used

	# make the XMAP attributes
	#$source = $conf{'MAPSETRSAB'};
	$source = $conf{'MAPSETVSAB'};

	# MAPSUBSETID: Map sub set identifier
	# MAPRANK: Order in which mappings in a subset should be applied
	# FROMID: Identifier mapped from
	# REL: Relationship
	# RELA: Relationship attribute
	# TOID: Identifier mapped to
	# MAPRULE: Machine processable rule for when to apply mapping
	# MAPTYPE: Type of mapping
	# MAPATN: Row level attribute name associated with this mapping
	# MAPATV: Row level attribute value associated with this mapping
	# MAPSID: Source asserted Mapping ID
	# MAPRES: Human readable restriction use of mapping
	
	# mapping file has format:
	# # of fields = 20, no trailing '|'
	# FROM_SID|FROM_EXPR|FROM_RULE|FROM_RES|FROM_TYPE|TO_SID|TO_EXPR|TO_RULE|TO_RES|
	# TO_TYPE|REL|RELA|SUBSETID|RANK|RULE|TYPE|ATN|ATV|MAPSID|MAPRES]
	
	$source = $conf{'MAPSETVSAB'};
	$sep = '~';
	

	# if FROMIDCHOICE/TOIDCHOICE value is set to SID, use the source supplied
	# SID as the ID and SID. 
	# if FROMIDCHOICE/TOIDCHOICE value is set to 'GEN', create an id value

	# start local ids for XMAP_FROM/XMAP_TO 
	$id = 1000;

	while(<MAP>){
		s/\r//;
		chomp;
		$fld_cnt = (tr/\|/\|/);
		$fld_cnt++;
		unless($opt{'t'}){
			if($fld_cnt != 20){
				print "\n";
				print "\tLine $. has $fld_cnt fields, should have 20.\n";
				print "\tCheck input file '$map_file' and re-run\n\n";
				print "\tFor more info see:\n";
				print "\thttp://unimed.nlm.nih.gov/MEME/toprint/mrmap.txt\n\n"; 
				exit;
			}
		}
		($from_sid,$from_expr,$from_rule,$from_res,$from_type,$to_sid,$to_expr,$to_rule,$to_res,$to_type,$rel,$rela,$subsetid,$rank,$rule,$type,$atn,$atv,$mapsid,$mapres) = split(/\|/);
		
	# rel is required, if not found show error message and quit
	unless($valid_rel{$rel}){
		print "\n\n";
		print " *** ERROR *** in data file: $map_file\n"; 
		print "Line: $. - Field 11 REL ('$rel') is missing or incorrect\n";
		print "REL must be one of:\n";
		foreach $rel (sort(keys(%valid_rel))){
			print "\t$rel\n";
		}
		print "\n";
		print "Processing incomplete - Correct $map_file and re-run\n";
		print "\n\n";
		exit 1;
	}

	# convert char ents if necessary
#	if($charent){
#		$from_expr =~ s/$mapsetsep/$umlssep/g;	
#		$to_expr =~ s/$mapsetsep/$umlssep/g;	
#		
#	}
	# XMAP_FROM	
		$attr_name = "XMAPFROM";
		# if source id supplied use that for both the id and sid
		if($conf{'FROMIDCHOICE'} eq 'SID'){ $from_id = $from_sid }
		elsif($conf{'FROMIDCHOICE'} eq 'EXPR'){ $from_id = $from_expr }
		elsif($conf{'FROMIDCHOICE'} eq 'GEN'){
			# if there isn't already an id for this value, assign one
			if(!$seen{$from_expr}){ 
				$seen{$from_expr} = ++$id }
			$from_id = $seen{$from_expr};
		}
		$attr_val = join($sep,$from_id,$from_sid,$from_expr,$from_type,$rule,$res);
		$hashcode = md5_hex($attr_val);

		if($opt{'t'}){	# test mode for reading the file 
			&print_test();
		}
		else{
			&print_attr(ATTR_MAP);
		}

	# XMAP_TO
		$attr_name = "XMAPTO";
		# if source id supplied use that for both the id and sid
		if($conf{'TOIDCHOICE'} eq 'SID'){ $to_id = $from_sid }
		elsif($conf{'TOIDCHOICE'} eq 'EXPR'){ $to_id = $to_expr }
		elsif($conf{'TOIDCHOICE'} eq 'GEN'){
			# if there isn't already an id for this value, assign one
			if(!$seen{$to_expr}){ 
				$seen{$to_expr} = ++$id }
			$to_id = $seen{$to_expr};
		}
		$attr_val = join($sep,$to_id,$to_sid,$to_expr,$to_type,$rule,$res);
		$hashcode = md5_hex($attr_val);


		if($opt{'t'}){	# test mode for reading the file 
			&print_test();
		}
		else{
			&print_attr(ATTR_MAP);
		}

	# XMAP REL
		$attr_name = "XMAP";
		$attr_val = join($sep,$conf{'MAPSUBSETID'},$conf{'MAPRANK'},$from_id,$rel,$rela,$to_id,$rule,$type,$atn,$atv,$mapsid,$mapres);
		$hashcode = md5_hex($attr_val);

		if($opt{'t'}){	# test mode for reading the file 
			&print_test();
		}
		else{
			&print_attr(ATTR_MAP);
		}



	}	# end of while(<MAP>)
	close MAP;
	
}	# end of make_attribs();

# $from_sid,$from_expr,$from_rule,$from_res,$from_type,$to_sid,$to_expr,$to_rule,$to_res,$to_type,$rel,$rela,$subsetid,$rank,$rule,$type,$atn,$atv,$mapsid,$mapres
sub print_test{
	print "--------------------------------------------------------------------\n";
	# if pipe count != 16, field missing

	printf "%10s: %-40s\n", "From SID", $from_sid;
	printf "%10s: %-40s\n", "From Expr", $from_expr;
	printf "%10s: %-40s\n", "From Rule", $from_rule;
	printf "%10s: %-40s\n", "From Res", $from_res;
	printf "%10s: %-40s\n", "From Type", $from_type;
	printf "%10s: %-40s\n", "To SID", $to_sid;
	printf "%10s: %-40s\n", "To Expr", $to_expr;
	printf "%10s: %-40s\n", "To Rule", $to_rule;
	printf "%10s: %-40s\n", "To Res", $to_res;
	printf "%10s: %-40s\n", "To Type", $to_type;
	printf "%10s: %-40s\n", "Rel", $rel;
	printf "%10s: %-40s\n", "Rela", $rela;
	printf "%10s: %-40s\n", "SubSetID", $subsetid;
	printf "%10s: %-40s\n", "Rank", $rank;
	printf "%10s: %-40s\n", "Rule", $rule;
	printf "%10s: %-40s\n", "Type", $type;
	printf "%10s: %-40s\n", "ATN", $atn;
	printf "%10s: %-40s\n", "ATV", $atv;
	printf "%10s: %-40s\n", "MAPSID", $mapsid;
	printf "%10s: %-40s\n", "MAPRES", $mapres;
	print "\n";
}

sub print_attr{
	my $str; 
	my $tmp_str;
	# turn on last field printing
	$last = "";

	my $fh = shift;
	my $flg = shift;
	$tmp_str = "$at_id|$at_lev|$attr_name|$attr_val|$source|$at_stat|$at_tbr|$at_rel|$at_supp|$id_type|$id_qual|$src_atui|$hashcode|$last";
	
	if($seen{$tmp_str}){ 
		$dup++;
	}
	else{ 
		$str = "$attr_id|$tmp_str";
		if($opt{'d'}){ print "$str\n"}
		print ATTR_MAP "$str\n";
		$attr_id++;
		$seen{$tmp_str}++;
	}
}	# end of print_attr
	
sub parse_conf{
	$conf_file = shift(@_);
	open(CONF, "$conf_file") or die "Can't open Config File: <$conf_file>, $!";
	while(<CONF>){
		chomp;
		next if(/^#/);	# comment lines are ignored
		next if(/^\s*$/);	# blank lines are ignored
		($label,$value,$comment)=split(/\|/);
		# lines starting with '*' are required
		if($label =~ /^\*/){
			$label =~ s/^\*//;
			unless($value){
				print STDERR "\n   *** A value is required for $label at line $.\n";
				return(0);
			}
		}
		else{
			$req = 0;
		}

		if($conf{$label}){
			print STDERR "\n   *** The identifier \"$label\" is duplicated at line $.\n";
			print STDERR "   *** Fix and re-run\n";
			return(0);
		}
		$conf{$label}=$value;
	}

	# there must be either a CODE or a MAPSETSID
	# if there is a MAPSETSID it is used as the CODE for the attributes
	unless($conf{'MAPSETSID'} || $conf{'CODE'}){
		print STDERR "\n   *** You must supply either a CODE or MAPSETSID in the config file\n";
		return(0);
	}

	if($conf{'MAPSETSID'}){
		$CODE = $conf{'MAPSETSID'}
	}
	else{$CODE = $conf{'CODE'} }

	$cnt = scalar(keys(%conf));
	# print "Keys: $cnt\n\n";
	return(%conf);
} # End of parse_conf()

sub print_help{
	print <<EOT;
   	
   Help for $0

   Command Line Options:
   -h   print this help screen
   -a   specify an attributes.src to find the last attribute ID
        (This will slow things done if you have a large attributes.src file)
        you need to have either the -a or the -i
   -c   specify a config file (required)
   -d   display output to terminal
   -i   specify an attribute ID to start with (much faster)
   -m   specify a mapping file (overrides MAPPINGFILE in the config file)
         (You need to have one or the other)
   -o   Specify a attribute file name
   -t   Test Display the mapping file. Does not write to the attributes file
        Writes a "label: value" line for each field to show you what your
        result will be.
        Remember to pipe output through 'more' or 'less'
        Use this to test your input mapping file
   -v   Verbose, display Config options and quit

   Instructions for use
   Grab a copy of the file '$INV_HOME/etc/template.mapsetattr' and edit 
   it to match your source.

   Values that are prefaced with '*' are required and will cause an
   error if not present.

   Documentation on MRMAP available at:
   http://unimed.nlm.nih.gov/MRD/Documentation/mrmap.txt
   
   The list of valid TO_TYPES and FROM_TYPES can be found at:
   http://unimed.nlm.nih.gov/apelon.html - MRDOC Editor

   Creating the Mapping File
   
EOT
} # End of print_help()

sub print_usage{
	print <<EOT;
		
	$0 -c config_file -h (print help) [-a use 'attributes.src'] [-m map_file] [-i attribute_id] 

EOT
} # End of print_usage()
	
sub parse_options{
if($opt{'h'}){&print_usage; &print_help; exit 1}
if($opt{'a'}){ 
	$attr_file = $opt{'a'};
	open(ATTR, "$attr_file") or die "Can't open/read $attr_file, $!";
	while(<ATTR>){
		($attr_id,$id) = split(/\|/);
	}
	$attr_id++;
}
if($opt{'i'}){ $attr_id = $opt{'i'}}
unless($attr_id){
	print "\n\tNo attributes.src file found or attribute_id specified\n";
	print "\tYou need to specify one or the other\n\n";
	&print_usage();
	exit;
}

if($opt{'c'}){ $conf_file = $opt{'c'}}
else { 
	print "\n\tError - no config file specified\n\n";
	&print_usage();
	exit 1;
}

if($opt{'m'}){ $map_file = $opt{'m'}}

if($opt{'o'}){ $outfile = $opt{'o'}}
else{ $outfile = "attributes_map.src"}
} # End of parse_options()

# this defines the required values
sub make_required_hash{
%required = (
MAPSETVSAB => 1,
MAPSETRSAB => 1,
FROMVSAB => 1,
FROMRSAB => 1,
TOVSAB => 1,
TORSAB => 1,
MAPSETID => 1,
MAPSETTYPE => 1,
);
} # End of make_required_hash()

sub convert_charent_table{
	while(<main::DATA>){
		chomp;
		next if(/^#/);
		next if(/^\s*$/);
		($char,$hex,$dec)=split(/\|/);
		print "Char: $char - Hex: $hex - Dec: $dec\n";

	}
}
 

__DATA__
# comments and blank lines are ignored
# these char ent codes are used for conversions

 |%20|&#32
!|%21|&#33
"|%22|&#34
#|%23|&#35
$|%24|&#36
%|%25|&#37
&|%26|&#38
'|%27|&#39
(|%28|&#40
)|%29|&#41
*|%2A|&#42
+|%2B|&#43
,|%2C|&#44
-|%2D|&#45
.|%2E|&#46
/|%29|&#47
:|%3A|&#58
;|%3B|&#59
<|%3C|&#60
=|%3D|&#61
>|%3E|&#62
?|%40|&#63
@|%41|&#64
^|%5E|&#94
_|%60|&#95
`|%61|&#96
{|%7B|&#123
}|%7D|&#125
~|%7E|&#126


# 
# {
# #  &#09;||Horizontal tab
# #  &#32;||Space
# #  &#33;|!|Exclamation mark
# #  &#34;|"|Quotation mark
# #  &#35;|#|Number sign
# #  &#36;|$|Dollar sign
# #  &#37;|%|Percent sign
# #  &#38;|&|Ampersand
# #  &#39;|'|Apostrophe
# #  &#40;|(|Left parenthesis
# #  &#41;|)|Right parenthesis
# #  &#42;|*|Asterisk
# #  &#43;|+|Plus sign
# #  &#44;|,|Comma
# #  &#45;|-|Hyphen
# #  &#46;|.|Period (fullstop)
# #  &#47;|/|Solidus
# #  &#58;|:|Colon
# #  &#59;|;|Semi-colon
# #  &#60;|<|Less than
# #  &#61;|=|Equals sign
# #  &#62;|>|Greater than
# #  &#63;|?|Question mark
# #  &#64;|@|Commercial at
# #  &#91;|[|Left square bracket
# #  &#92;|\|Reverse solidus
# #  &#93;|]|Right square bracket
# #  &#94;|^|Caret
# #  &#95;|_|Horizontal bar
# #  &#96;|`|Grave accent
# #  &#123;|{|Left curly brace
# #  &#124;|||Vertical bar
# #  &#125;|}|Right curly brace
# #  &#126;|~|Tilde
# }
# 
# 
# {
# %charent = (
# "&#09;" => '\t',
# "&#32;" => ' ',
# "&#33;" => '!',
# "&#34;" => '\"',
# "&#35;" => '#',
# "&#36;" => '$',
# "&#37;" => '%',
# "&#38;" => '&',
# "&#39;" => '\'',
# "&#40;" => '(',
# "&#41;" => ')',
# "&#42;" => '*',
# "&#43;" => '+',
# "&#44;" => '',,
# "&#45;" => '-',
# "&#46;" => '.',
# "&#47;" => '/',
# "&3A;" => ':',
# "&#59;" => ';',
# "&#60;" => '<',
# "&#61;" => '=',
# "&#62;" => '>',
# "&#63;" => '?',
# "&#64;" => '@',
# "&#91;" => '[',
# "&#92;" => '\\',
# "&#93;" => ']',
# "&#94;" => '^',
# "&#95;" => '_',
# "&#96;" => '`',
# "&#123;" => '{',
# "&#124;" => '\|',
# "&#125;" => '}',
# "&#126;" => '~',
# );
# }
