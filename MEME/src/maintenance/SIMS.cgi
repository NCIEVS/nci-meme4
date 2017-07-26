#!@PATH_TO_PERL@
# File:	 SIMS.cgi
# Author: WAK, based on Brian Carlsen's template, template.cgi
# 
# CHANGES
# 06/13/2008 JFW (1-HS8VR): Wrap long fields when creating <SAB>.html
# 01/31/2008 BAC (1-GCLNT): Use lt instead of < when comparing meta_ver when updating sources.html
# 11/07/2007 BAC (1-FPFK1): Revise definition of meta_ver when clicked on.
#  03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#  01/30/2006 BAC (1-73QZ0): when building summary pages for sources,
#       include inversion proposal links.
#  01/11/2006 BAC (1-73QZ0): when building summary pages for sources,
#       include links to TEST.<sab>.html and INSERTION.<sab>.html pages.
#
# Version info:
#	 11/15/2001 3.1.0: First version
#
$release = "3";
$version = "1.0";
$version_authority = "BAC";
$version_date = "11/15/2001";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";
use Text::Wrap;

# Set variables for Text::Wrap throughout the entire script
# Text::Wrap is used to format fields with newlines on the displayed page.
local($Text::Wrap::columns) = 80;
local($Text::Wrap::separator) = "<br>";

# **************** Documentation ***********************
# SIMS.cgi is a multi-state CGI application that provides read and write
# access to the SIMS_INFO table in the current editing database
# SIMS_INFO stores metadata for sources in the Metathesaurus.
# 
# In the initial state, SIMS presents the entry screen with selection boxes
# that allows the user to pick a source and/or action. Each action calls
# the same CGI program, but with a different state. States are:
# INDEX		Print the select boxes
# ADD		Define a record to add (enter source name and meta year)
# ADD_COMPLETE	Add the record to SIMS_INFO
# EDIT		Edit source fields in form boxes
# EDIT_COMPLETE	Write the Edits to the table and display the record
# CLONE		Pick a record to clone
# CLONE_COMPLETE	Create the new record in SIMS_INFO with pre-filled fields
# DELETE	Pick a record to delete
# DELETE_COMPLETE	Confirm deletion, copy record to DEAD_SIMS_INFO and remove from SIMS_INFO
# VIEW		Display the record

# When a record is edited it is written to an HTML file, called the 
# Editor's Source Page. This page is found in $MEME_HOME/www/Sources
# URL: /Sources/{SAB}.html 
# This can be turned off by unselecting the box at the bottom of the edit form

# The Source index page is also updated with data from all SIMS_INFO records
#  $ENV{MEME_HOME}/www/Sources/sources.html
# /Sources/sources.html
# This can also be turned off at the bottom of the edit form

# SIMS.cgi is self-updating. It reads the list of available sources and their
# years from the SIMS_INFO table. It should not require any editing to handle
# a new META Version. This is true for the Source Information page as well. SIMS.cgi
# also uses the production database as defined by: 
# $db = &meme_utils::midsvcs("current-editing-tns")
#
# While there is no "help" file, javascript windows provide additional details
# on field contents and field sizes.

# Written by Bill King, January, 2002

# CHANGES
# 09/22/03 - Call to mdsvcs for "production-db" instead of "editing-db"
# should always return the correct production DB, i.e., oa_mid2004 for
# META04, oa_mid2005 for META05, etc.
# &meme_utils::midsvcs("production-db")

# Parse command line arguments to determine if -v{ersion}, or --help 
# is being used.  Do Not Change This Part.
#
while (@ARGV) {
	$arg = shift(@ARGV);
	if ($arg !~ /^-/) {
	push @ARGS, $arg;
	next;
	}
	if ($arg eq "-v") {
	$print_version = "v";
	}
	elsif ($arg eq "-version") {
	$print_version = "version";
	}
	elsif ($arg =~ /-{1,2}help$/) {
	$print_help=1;
	}
}

&PrintHelp && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

#
# Get Parameters
# If request method is GET, parameters are in variable $QUERY_STRING
# If request method is POST, parameters are in STDIN
#
#foreach $env (%ENV){ print "$env : $ENV{$env}\n"}

if ($ENV{"REQUEST_METHOD"} eq "GET") {
	$_=$ENV{"QUERY_STRING"};
} else {
	$_=<STDIN> || die "Method $ENV{REQUEST_METHOD} not supported.";
}
#
# This is the set of valid arguments used by readparse. 
# This list should be configured to suit the variables
# that your CGI script will use
#
%meme_utils::validargs = 
	( 
	"state" => 1, "db" => 1, "meme_home" => 1, "oracle_home" => 1,
	"source" => 1,
	"year" => 1,
	"date_created" => 1,
	"meta_ver" => 1,
	"init_rcpt_date" => 1,
	"clean_rcpt_date" => 1,
	"test_insert_date" => 1,
	"real_insert_date" => 1,
	"source_contact" => 1,
	"inverter_contact" => 1,
	"nlm_path" => 1,
	"apelon_path" => 1,
	"inversion_script" => 1,
	"inverter_notes_file" => 1,
	"conserve_file" => 1,
	"sab_list" => 1,
	"meow_display_name" => 1,
	"source_desc" => 1,
	"status" => 1,
	"worklist_sortkey_loc" => 1,
	"whats_new" => 1,
	"termgroup_list" => 1,
	"attribute_list" => 1,
	"inversion_notes" => 1,
	"internal_url_list" => 1,
	"notes" => 1,
	"nlm_editing_notes" => 1,
	"inv_recipe_loc" => 1,
	"suppress_edit_rec", => 1,
	"editor_report" => 1,
	"update_sources_page" => 1,
	"suppress_editor_info" => 1,
	"new_source" => 1,
	"new_meta_ver" => 1
	);
	
#
# Iclude the meme_utils.pl library
# This library should be installed in the same
# directory as this script on the CGI server
# 
$inc = $ENV{"SCRIPT_FILENAME"};
$inc =~ s/(.*)\/.*$/$1/;
unshift @INC, "$inc";
require "meme_utils.pl";
require DBI;
require DBD::Oracle;

#
# This is the HTTP header, it always gets printed
#
print qq{Expires: Fri, 20 Sep 1998 01:01:01 GMT\n};
&meme_utils::PrintHTTPHeader;

#
# Readparse translates CGI argument string into variables 
# that can be used by the script
#
&meme_utils::ReadParse($_);

#
# Default Settings for CGI parameters
#
$ENV{"MEME_HOME"} = $meme_home || $ENV{"MEME_HOME"} || die "\$MEME_HOME must be set.";
$ENV{"ORACLE_HOME"} = $oracle_home || $ENV{"ORACLE_HOME"} || die "\$ORACLE_HOME must be set.";
#
# SL: TEMPORARY SOLUTION FOR updating non ascii characters into database.
# SOMEHOW the Perl DBI/or Oracle is throwing an error 0ra-01461.
# Setting the NLS_LANG to WE8ISOoo59P1 seems to fix the problem.
$ENV{"NLS_LANG"} = "AMERICAN_AMERICA.WE8ISO8859P1";
$state = "INDEX" unless $state;
#$state = "CHECK_JAVASCRIPT" unless $state;

#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MID, current MID
# 
$db = &meme_utils::midsvcs("production-db") unless $db;
#$date = `/bin/date +%Y%m%d`;
$cgi = $ENV{"SCRIPT_NAME"};
$start_time = time;
$style_sheet = &meme_utils::getStyle;
$FATAL = 1;

#
# Determine Which action to run, print the body
# Valid STATES:
#
# 1. INDEX. Print the index page
# 2. SOME_OTHER_STATE. Does something else.
#
#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
		 "INDEX" => 
			 ["PrintHeader","PrintINDEX","PrintFooter","SIMS","SIMS"],
		 "ADD" => 
			 ["PrintHeader","Print_ADD","PrintFooter", "SIMS - Add Record","SIM  - ADD a new Record"],
		 "ADD_COMPLETE" => 
			 ["PrintHeader","Print_ADD_COMPLETE","PrintFooter", "SIMS - Add Record (Part II)","SIM - ADD a new Record"],
		 "EDIT" => 
			 ["PrintHeader","Print_EDIT","PrintFooter", "SIMS - Edit Record","SIMS - Edit a Record"],
		 "EDIT_COMPLETE" => 
			 ["PrintHeader","Print_EDIT_COMPLETE","PrintFooter", "SIMS - Edit Complete","SIMS - Report for $source"],
		 "CLONE" => 
			 ["PrintHeader","Print_CLONE","PrintFooter", "SIMS - Clone","SIMS - Clone $source"],
		 "CLONE_COMPLETE" => 
			 ["PrintHeader","Print_CLONE_COMPLETE","PrintFooter", "SIMS - Clone Part II","SIMS - Clone $source"],
		 "DELETE_CONF" => 
			 ["PrintHeader","Print_DELETE_CONF","PrintFooter", "SIMS - Confirm Delete Record","SIMS - Confirm Deletion of a Record"],
		 "DELETE" => 
			 ["PrintHeader","Print_DELETE","PrintFooter", "SIMS - Delete Record","SIMS - Delete a Record"],
		 "VIEW" => 
			 ["PrintHeader","Print_VIEW","PrintFooter","SIMS -  View Record","SIMS - View a Record"] 
		 );

#
# Check to see if state exists
#
if ($states{$state}) {
	#
	# Print Header, Body, and Footer
	#
	$header = $states{$state}->[0];
	$body = $states{$state}->[1];
	$footer = $states{$state}->[2];


#	print "$header, $body, $db, $footer\n";
#	exit(0);

	# set variables to use database
	$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
	($user,$password) = split /\//, $userpass;
	chop($password);

	# open connection
	$dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
	((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
			 &&	return);
	$dbh->do("ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY'")||
	((print "<span id=red>Error setting date format $db ($DBI::errstr).</span>")
						 &&	 return);


	&$header($states{$state}->[3],$states{$state}->[4]);
	&$body;
	&$footer;

	$sh->finish();
	#$insert_handle->finish();
	#$update_handle->finish();
	$dbh->disconnect if $dbh;
}

#
# We're done, exit 
#
exit(0);


################################# PROCEDURES #################################
#
# The following procedures print HTML code for the script
#
#############################################################################

###################### Procedure None ######################
#
# This prints either No header, No body, or No footer
#
sub None {

}

###################### Procedure PrintHeader ######################
#
# If no_form is passed in, print header without the form
#
sub PrintHeader {

	my($title,$header) = @_;

	&meme_utils::PrintSimpleHeader(
		$title,$style_sheet,&HeaderJavascript,"<h2><center>$header</center></h2>");

}

###################### Procedure HeaderJavascript ######################
#
# This procedure contains the javascript for the standard header
#
sub HeaderJavascript {
# there is a separate validate procedure for ADD and EDIT.	
	my($ver,$source);

	# prepare and execute statement
	 $sh = $dbh->prepare(qq{
	SELECT meta_ver,source FROM sims_info
	ORDER BY meta_ver
	}) ||
	((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
	         &&  return);

	$sh->execute ||
	((print "<span id=red>Error executing query ($DBI::errstr).</span>")
	         &&  return);

	# read everything in
	# all versions are 4 digit, 2 char; e.g., 1999AA
	# even for years before mult. releases

	# this first loop populates a hash where the hash name
	# is the year part of the full version name
	# this is used later to determine if there are mult.
	# releases for that particular year
	# 
	# all of the data is saved as fetched to an array for
	# processing in the second loop
	while (($ver,$source) = $sh->fetchrow_array) {
		$ver =~ /(\d{4})(.*)/;
		$year = $1; $suff = $2;
		$href = \$year;
		${$$href}{$suff}++;
		$src = "$ver|$source";
		push(@SRC,$src);
	}

	# data is processed as it came from the db query
	# if it is determined that there was only one release
	# for that year, there is no separate year entry
	# i.e., 2001AA, 2001AB, 2001AC requires that there is
	# a 2001 entry as well
	foreach $src (@SRC){
		($ver,$source)=split(/\|/,$src);
		if($ver =~ /(\d{4})\D{2}/){
			$year = $1;
			$href = \$year;
			if(scalar(keys(%{$$href}))> 1){
				$select_lists{$year} .= "$source|";
			}
		}
	   $select_lists{$ver} .= "$source|";
	}
	$js = qq{
	<script language="javascript">
	function openDescription (check,dsc) {
		var html = "<html><head><title>Description: "+check;
		html = html + "</title></head><body bgcolor=#ffffff>" + dsc + "<center><form><input type=button onClick='window.close(); return true' value='Close'></form></center></body></html>";
		var win = window.open("","","scrollbars,width=500,height=250,resizable");
		win.document.open();
		win.document.write(html);
		win.document.close();
	}; // end openDescription

	function validate ( form ) {
		if(form.name == 'edit'){
		// Validate the date fields
		if (!checkDate(form.init_rcpt_date)) {
			alert ("The initial receipt date must have the form 'dd-mmm-yyyy' or 'dd-mmm-yyyy hh:mm:ss'");
			form.init_rcpt_date.focus();
		return;  }

		if (!checkDate(form.clean_rcpt_date)) {
			alert ("The clean receipt date must have the form 'dd-mmm-yyyy' or 'dd-mmm-yyyy hh:mm:ss'");
			form.clean_rcpt_date.focus();
		return;  }

		if (!checkDate(form.test_insert_date)) {
			alert ("The test insert date must have the form 'dd-mmm-yyyy' or 'dd-mmm-yyyy hh:mm:ss'");
			form.test_insert_date.focus();
		return;  }

		if (!checkDate(form.real_insert_date)) {
			alert ("The real insert date must have the form 'dd-mmm-yyyy' or 'dd-mmm-yyyy hh:mm:ss'");
			form.real_insert_date.focus();
		return;  }
		if (!checkMetaVer(form.meta_ver)) {
			alert ("The META Version field must have a value of the form 'YYYYXX'");
			form.meta_ver.focus();
			return; }
		form.submit();
		}
		if(form.name == 'add'){
			if (!checkMetaVer(form.meta_ver)) {
				alert ("Correct the META Version field'");
				form.meta_ver.focus();
				return; }
			form.submit();
				
		}
	}

function checkMetaVer(string) {
	str = string.value;
	if (str=="") {
	return false;}
	if (str=="CURRENT") {
	return true;}
	if (str=="NOT_RELEASED") {
	return true;}
	for (var j = 0; j < 3; j++) {
	ch = str.substring(j,j+1);
	if (ch < "0" || ch > "9") {
		alert ("META Version must be CURRENT, NOT_RELEASED or expressed as 4 numbers and 2 letters (e.g. 2005AB)."); return false; }
	}
	ch = str.substring(4,5);
	if(ch == "A"){}
	else{
		alert ("META Version must be CURRENT, NOT_RELEASED or expressed as 4 numbers and 2 letters (e.g. 2005AB)."); return false; }
	ch = str.substring(5,6);
	if(ch =="A" || ch == "B" || ch == "C" || ch == "D"){
	return true;}
	else {
		alert (" Last char is either A, B, C, or D"); return false;}
	}

function checkDigits(string) {
	str = string.value;
	if (str=="") {
	return false;}
	for (var j = 0; j < 3; j++) {
	ch = str.substring(j,j+1);
	if (ch < "0" || ch > "9") {
		alert ("Year must be expressed as 4 numbers (e.g. 1999)."); return false; }
	}
	return true;
	}

function checkDate(date) {
	str = date.value;
	if (str=="") {
	return true;
	}
	if (str.toLowerCase() == "now" || str.toLowerCase() == "today") {
	return true;
	}
	var ch = str.substring(0,1);
	if (ch < "0" || ch > "9") {
	alert ("The day must be expressed as a number between 1 and 31."); return false; }
	var ch2 = str.substring(1,2);
	if (ch2 == "-") { 	i = 1;  }	
	else if (ch2 < "0" || ch2 > "9") {
	alert ("The day must be expressed as a number between 1 and 31."); return false; }
	else {
	if (ch != "0") ch = ch + ch2;
	else ch = ch2;
	if (parseInt(ch) < 1 || parseInt(ch) > 31) {
		alert ("The day must be between 1 and 31: " + ch); return false; }
	i = 2;
	}
	ch = str.substring(i,i+1);
	if (ch != "-" ) { return false; }
	i++;
	ch = str.substring(i,i+3).toLowerCase();
	if (ch != "jan" && ch != "feb" && ch != "mar" && ch != "apr" && ch != "may"
	&& ch != "jun" && ch != "jul" && ch != "aug" && ch != "sep" && ch != "oct"
	 && ch != "nov" && ch != "dec") {
	alert ("Invalid month, not in {jan,feb,mar,apr,may,jun,jul,aug,sep,oct,nov,dec}."); 
	return false; }
	i += 3;
	ch = str.substring(i,i+1);
	if (ch != "-" ) { return false; }
	i++;
	for (var j=i; j < i+4; j++) {
	ch = str.substring(j,j+1);
	if (ch < "0" || ch > "9") {
		alert ("Year must be expressed as 4 numbers (e.g. 1999)."); return false; }
	}
	i += 4;
	if (str.length == i) { return true; }
	ch = str.substring(i,i+1);
	if (ch != " ") { return false; }
	i++;
	ch = str.substring(i,i+2);
	if (parseInt(ch) < 0 || parseInt(ch) > 23) {
	alert ("Hour must be >= 0 and < 24."); return false; }
	i +=2;
	ch = str.substring(i,i+1);
	if (ch != ":") { return false; }
	i++;
	ch = str.substring(i,i+2);
	if (parseInt(ch) < 0 || parseInt(ch) > 59) {
	alert ("Minutes must be >= 0 and < 60"); return false; }
	i += 2;
	ch = str.substring(i,i+1);
	if (ch != ":") { return false; }
	i++;
	ch = str.substring(i,i+2);
	if (parseInt(ch) < 0 || parseInt(ch) > 59) {
	alert ("Seconds must be >= 0 and < 60"); return false; }
	i += 2;

	if (str.length == i) { return true; }
	return false;
	}

	function setSelectList(list,ver) \{
};
	@sorted_vers = reverse sort {
	  if($a eq 'NOT_RELEASED' || $a eq 'TBD'){return -1}
	  elsif($b eq 'NOT_RELEASED' || $b eq 'TBD'){return 1}
	  else{ return $a cmp $b} 
	}(keys(%select_lists));


#print "count in sorted_vers = $#sorted_vers<P>";

#foreach $ver (@sorted_vers){
#	print "[$ver]:$select_lists{$ver}<P>\n";
#}

	foreach $ver (@sorted_vers) {
	  @sources = split /\|/, $select_lists{$ver};
	  $js .= qq{
	      // Add values for $ver
	      if (ver == "$ver") \{};
	  $i = 0;
	  foreach $source (sort @sources) {
	    if ($source) {
	      $js .= qq{
	        var opt = new Option('$source','$source');
	        list.options[$i] = opt;};
	      $i++;
	    }
	  }
	 $js .= qq{
	        // Delete any remaining ones at the end of the list
	        for (var i = $i; i < list.length; ) \{
	          list.options[i] = null;
	        \}
			list.selectedIndex=0;
	      \}};
	}

	$js .= qq{
	      //history.go(0);
	    \}
	</script>
};


	return $js;
} # end HeaderJavascript


###################### Procedure PrintFooter ######################
#
# This procedure prints a standard footer including time to generate
# the page, the current date, and some links
#
sub PrintFooter {

	#
	# Compute the elapsed time
	#
	$end_time=time;
	$elapsed_time = $end_time - $start_time;

	#
	# Print the Footer
	#
	print qq{
	<hr width="100%">
	<table BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
		<tr NOSAVE>
		<td ALIGN=LEFT VALIGN=TOP NOSAVE>
			<address><a href="$cgi">Back to Index</a></address>
			</td>
		<td ALIGN=RIGHT VALIGN=TOP NOSAVE>
			<font size="-1"><address>Contact: <a href="mailto:reg\@msdinc.com">reg\@msdinc.com</a></address>
			<address>Generated on:},scalar(localtime),qq{</address>
			<address>This page took $elapsed_time seconds to generate.</address>
			<address>};
	&PrintVersion("version");
	print qq{</address></font>
			</td>
		</tr>
		</table>
	</body>
</html>
};
} # End of PrintFooter


###################### Procedure PrintINDEX ######################
#
# This procedure prints the index page for the application with
# links to the other functionality
#
sub PrintINDEX {
	# %select_lists is defined in &HeaderJavascript
	#@vers =  (reverse sort keys %select_lists);
	#$max_ver =$vers[0];

	print qq{
	<BLOCKQUOTE>
	SIMS, the Source Information Management System,
        is the repository for source information for NLM editors and 
	is collected by Apelon source inverters. 
	<P> 
	This web interface is primarily for data entry use. 
	To <B>view</B> the editor information,
        see the <A HREF=\"/Sources/sources.html\">sources page </A>which is 
	generated and kept current by SIMS.
	</BLOCKQUOTE>
	};
	# uncomment while doing maintenance
	# print "<FONT SIZE=\"6\" COLOR = \"RED\">\n";
	# print "SIMS is being upgraded<P>";
	# print "Please do not use SIMS until this message has been removed\n";
	# print "</FONT>\n";



	print qq{
		<table width="90%">
		<tr><td align="center">
	    <form name="theform">
	    <SELECT name="source"><option>test</option></SELECT>
	&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;
	&nbsp;&nbsp;&nbsp;
	    <SELECT name="year"
	            onChange="setSelectList(this.form.source,this.form.year.options[this.form.year.selectedIndex].value)">


}
;
	# %select_lists is defined in &HeaderJavascript
	foreach $ver (@sorted_vers) {
	  print qq{         <option value="$ver">$ver</option>
};
	}
	print qq{
	        </SELECT>
		<input type="hidden" name="state" value="EDIT">
		<input type="hidden" name="db" value=$db>
	    <br><br><center>

	        <input type="button" value=" Add " onClick='this.form.state.value="ADD"; this.form.submit(); return true;'>&nbsp;&nbsp;&nbsp;

	        <input type="button" value=" Edit " onClick='this.form.state.value="EDIT"; this.form.submit(); return true;'>&nbsp;&nbsp;&nbsp;

	        <input type="button" value=" Clone " onClick='this.form.state.value="CLONE"; this.form.submit(); return true;'>&nbsp;&nbsp;&nbsp;

	        <input type="button" value=" Delete " onClick='this.form.state.value="DELETE_CONF"; this.form.submit(); return true;'>&nbsp;&nbsp;&nbsp;

	        <input type="button" value=" View " onClick='this.form.state.value="VIEW"; this.form.submit(); return true;'>

	    <center><br>
	    </form>
	</td></tr>
	</table>
	    <script language="JavaScript">
	        setSelectList(document.theform.source,document.theform.year.options[document.theform.year.selectedIndex].value);
	    </script>
	<BLOCKQUOTE>
	Note: You are currently using the database <font size=+1 color="red"><B>$db</B></font>.<BR>
	To specify another database, append this string to the URL:
	<code>?db=<I>database_name</I>

	};

}; # end PrintINDEX

###################### Procedure Print_ADD ######################
#
# This procedure prints all of the table names in user tables
#
sub Print_ADD {

	print qq{
	Add a Record <P>
 <P>
	};# end of print
# Create input form
print qq{
You are editing: <B>$db</B><BR>
<i>Edit the following fields and click "Add"</i>
<br>&nbsp;
<form method="GET" action="$cgi" name="add">
<font size="-1">
<input type="hidden" name="state" value="ADD_COMPLETE">
<input type="hidden" name="db" value=$db>
<center><table CELLPADDING=2 WIDTH="90%" BORDER="0" >
	<tr>
	<td><font size=-1>
		<a href="javascript:void(0)" 
		onClick="openDescription('Source SAB',
				  'This is a source abbreviation. Typically the source will be composed of the stripped source and the version');"> &nbsp;&nbsp;Source (SAB)</a>:</font></td>
	<td><font size="-1">&nbsp;&nbsp;<input type="text" size="40" name="source"></font></td>
</tr>
<tr>
	<td><font size=-1>
		<a href="javascript:void(0)" 
		onClick="openDescription('META Ver',
				  'CURRENT for sources not yet released. If already released use META Version for which this source inserted (even if it was an intermediate SAB).  (YYYYXX)');"> &nbsp;&nbsp;META Version</a>:</font></td>
	<td><font size="-1">&nbsp;&nbsp;<input type="text" size="20" name="meta_ver"></font></td>
	</tr>


	<tr >
	<td>&nbsp;</td>
	<td>&nbsp;&nbsp;&nbsp;&nbsp;
			<input type="button" value="  Add  " onClick='validate(this.form); return true;'>
	</td>
	<td>&nbsp;</td>
	</tr>

</table>
</form>
<p>
		


}
} # end of print_add()


###################### Procedure PrintADD_Complete ######################
#
# This procedure ADDs the new Record to the Database and sends the
# user to that record for editing

sub Print_ADD_COMPLETE {

my ($sec, $min, $hour, $ora_day, $ora_month, $yr, $wday, $yday, $isdst) = localtime(time);
$month++;	# month starts with 0
if($month<10){$month = "0".$month}
my($ora_year) = `date +%Y`;
my($ora_mon) = `date +%b`;
chomp $ora_mon;
chomp $ora_year;
$ora_date = $ora_day."-".$ora_mon."-".$ora_year;

my $insert_handle = $dbh->prepare(qq{
	INSERT INTO SIMS_INFO (SOURCE, DATE_CREATED, META_VER, META_YEAR) VALUES (?,?,?,-1)
	});
my $row_count = $insert_handle->execute($source, $ora_date, $meta_ver);
unless (defined($row_count)) {
	((print "<span id=red>Error adding source ($DBI::errstr).</span>")
	&& return);
}
	print qq{
	<CENTER>
	<B>$source</B> was successfully added to SIMS_INFO<P>
	Do you want to <A HREF="$cgi?state=EDIT&db=$db&source=$source">edit $source</A> now?
	</CENTER>
	}

}	# end of Print_ADD_COMPLETE


###################### Procedure Print_CLONE ######################
#
# This procedure clones an existing record 
#
sub Print_CLONE {
# ask user for new source name
# send back for next state of CLONE_COMPLETE

	print qq{
	Clone a Record <P>
 <P>
	};# end of print
# Create input form
$new_meta_ver = "CURRENT";
print qq{
<I> You are cloning <B>$source</B></I><BR>
<i>Please enter a <B> New, Unique</B> Source Name, a META Version and click "Add"</i>
<br>&nbsp;
<form method="GET" action="$cgi">
<font size="-1">
<input type="hidden" name="source" value=$source>
<input type="hidden" name="db" value=$db>
<input type="hidden" name="state" value="CLONE_COMPLETE">
<center><table CELLPADDING=2 WIDTH="90%"  >
	<tr>
	<td><font size=-1>
		<a href="javascript:void(0)" 
		onClick="openDescription('Source SAB',
				  'This is a source abbreviation. Typically the source will be composed of the stripped source and the version');">Source (SAB)</a>:</font></td>
	<td><font size="-1"><input type="text" size="40" name="new_source"></font></td>
	</tr>

	<tr>
	<td><font size=-1>
		<a href="javascript:void(0)" 
		onClick="openDescription('META Version',
				  'CURRENT, NOT_RELEASED, or the META Version in which this source was released. (YYYYXX)');">META Version</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="new_meta_ver" value="CURRENT"></font></td>
	</tr>

	<tr >
	<td COLSPAN="2" ><center>
			<input type="submit" value="  Add  ">
		&nbsp; &nbsp; &nbsp;
		</center></td>
	</tr>

</table></center>
</form>
<p>
}	# end of print
}	# end of Print_CLONE

###################### Procedure Print_CLONE_COMPLETE ######################
#
# This procedure clones an existing record 
#
sub Print_CLONE_COMPLETE {
	print qq{
		<CENTER>
		Cloning <B>$source</B> as $new_source</B><P>
	};
	my ($sec, $min, $hour, $ora_day, $ora_month, $yr, $wday, $yday, $isdst) = localtime(time);
	$month++;	# month starts with 0
	if($month<10){$month = "0".$month}
	my($ora_year) = `date +%Y`;
	my($ora_mon) = `date +%b`;
	chomp $ora_mon;
	chomp $ora_year;
	$ora_date = $ora_day."-".$ora_mon."-".$ora_year;

	#print "Old Source: $source<BR>\n";
	#print "New Source: $new_source<BR>\n";
	#print "Meta Ver: $new_meta_ver<BR>\n";
	print "Date_created: $ora_date<BR>\n";

	
# read in the old record
my $read_handle = $dbh->prepare(qq{
	    SELECT source, date_created, meta_ver, init_rcpt_date, 
	    clean_rcpt_date, test_insert_date, real_insert_date, source_contact, 
	    inverter_contact, nlm_path, apelon_path, inversion_script, 
	    inverter_notes_file, conserve_file, sab_list, 
	    meow_display_name,source_desc,status,worklist_sortkey_loc,whats_new,
	    termgroup_list, attribute_list, inversion_notes, internal_url_list, 
	    notes, nlm_editing_notes, inv_recipe_loc, suppress_edit_rec
	    FROM sims_info WHERE source=?
	    }) || 
	    ((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>") );
$read_handle-> execute($source) ||
((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
&&  return);
	
unless (($source, $date_created, $meta_ver, $init_rcpt_date, 
$clean_rcpt_date,$test_insert_date, $real_insert_date, $source_contact, 
$inverter_contact, $nlm_path, $apelon_path, $inversion_script, 
$inverter_notes_file, $conserve_file, $sab_list, 
$meow_display_name, $source_desc, $status,
$worklist_sortkey_loc, $whats_new, $termgroup_list, $attribute_list, 
$inversion_notes, $internal_url_list, $notes, $nlm_editing_notes, 
$inv_recipe_loc, $suppress_edit_rec) = $read_handle->fetchrow_array) {
((print "<span id=red>Source $source is not in sims_info. ($DBI::errstr)</span>")
&&  return);
};

$read_handle->finish();

# create the new record
my $insert_handle = $dbh->prepare(qq{
	INSERT INTO SIMS_INFO 
        (source, date_created, meta_ver, init_rcpt_date,
	 clean_rcpt_date, test_insert_date, real_insert_date,
	 source_contact, inverter_contact, nlm_path, apelon_path,
	 inversion_script, inverter_notes_file, conserve_file, sab_list, 
	 meow_display_name, source_desc, status,
	 worklist_sortkey_loc, whats_new, termgroup_list, attribute_list,
	 inversion_notes, internal_url_list, notes, nlm_editing_notes,
	 inv_recipe_loc, suppress_edit_rec, meta_year) VALUES
	(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,-1)
	}); 	# edit done
my($suppress_edit_rec)= "N";
my $row_count = $insert_handle->execute(
$new_source, $ora_date, $new_meta_ver, $init_rcpt_date, 
$clean_rcpt_date,$test_insert_date, $real_insert_date, $source_contact, 
$inverter_contact, $nlm_path, $apelon_path, $inversion_script, 
$inverter_notes_file, $conserve_file, $sab_list, 
$meow_display_name, $source_desc, $status,
$worklist_sortkey_loc, $whats_new,$termgroup_list, $attribute_list, 
$inversion_notes, $internal_url_list, $notes, $nlm_editing_notes,
$inv_recipe_loc, $suppress_edit_rec);

unless (defined($row_count)) {
	((print "<span id=red>Error adding source $new_source ($DBI::errstr).</span>")
	&& return);
}
print qq{
	<CENTER>
	<B>$new_source</B> was successfully added to SIMS_INFO<P>
	Do you want to <A HREF="$cgi?state=EDIT&db=$db&source=$new_source">edit $new_source</A> now?
	</CENTER>
};



}

###################### Procedure Print_EDIT ######################
#
# This procedure prints the edit form pre-filled with values from existing record
#
sub Print_EDIT {
	# removed 8/15/02 - WAK
	#my($r1_editrec_checked,$r1_new_checked,$r1_update_checked);
	my($r1_editrec_checked);

	$sh = $dbh->prepare(qq{
	SELECT source, date_created, meta_ver, init_rcpt_date, 
	clean_rcpt_date, test_insert_date, real_insert_date, source_contact, 
	inverter_contact, nlm_path, apelon_path, inversion_script, 
	inverter_notes_file, conserve_file, sab_list, 
	meow_display_name, source_desc, status, 
	worklist_sortkey_loc, whats_new,
	termgroup_list, attribute_list, inversion_notes, internal_url_list, 
	notes, nlm_editing_notes, inv_recipe_loc, suppress_edit_rec
	FROM sims_info WHERE source=?
	}) || 
	((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>") );
	#((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>") &&  return);

$sh->execute($source) ||
((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
&&  return);

unless (($source, $date_created, $meta_ver, $init_rcpt_date, 
	$clean_rcpt_date,$test_insert_date, $real_insert_date, $source_contact, 
	$inverter_contact, $nlm_path, $apelon_path, $inversion_script, 
	$inverter_notes_file, $conserve_file, $sab_list, 
	$meow_display_name, $source_desc, $status,
	$worklist_sortkey_loc, $whats_new, 
	$termgroup_list, $attribute_list, $inversion_notes, $internal_url_list, 
	$notes, $nlm_editing_notes, $inv_recipe_loc, $suppress_edit_rec 
	) = $sh->fetchrow_array) {
((print "<span id=red>Source $source is not in sims_info. ($DBI::errstr)</span>")
&&  return);
};

# original imported data had pipes encoded as '%7C'
# this converts them back to pipes
map {
	s/%7C/|/g;
} ($source, $meta_ver, $init_rcpt_date, 
$clean_rcpt_date,$test_insert_date, $real_insert_date, $source_contact, 
$inverter_contact, $nlm_path, $apelon_path, $inversion_script, 
$inverter_notes_file, $conserve_file, $sab_list, 
$meow_display_name, $source_desc, $status,
$worklist_sortkey_loc, $whats_new, $termgroup_list, $attribute_list, 
$inversion_notes, $internal_url_list, $notes, $nlm_editing_notes,
$inv_recipe_loc);

print "Meta Ver: $meta_ver<P>";

map{
	s/<BR>/\n/g;
	} ($meow_display_name,$source_desc, $whats_new, $termgroup_list, $attribute_list, 
 $inversion_notes, $whats_new, $internal_url_list, $notes, $nlm_editing_notes);

### set state of check box for 'Status'
if($status eq 'New'){
	$r1_new_checked = "CHECKED";
	$r1_update_checked = "";
}
elsif($status =~ /update/i){
	$r1_new_checked = "";
	$r1_update_checked = "CHECKED";
}	
else{
	$r1_update_checked = "";
	$r1_new_checked = "";
}
if($suppress_edit_rec eq 'Y'){
	$r1_editrec_checked = "CHECKED";
}
else{ $r1_editrec_checked = ""}

# Create input form
print qq{
You are editing in: <B>$db</B><BR>
<i>Edit the following fields and click "Save"</i>
<br>&nbsp;
<form method="POST" action="$cgi" name="edit">
<font size="-1">
<input type="hidden" name="state" value="EDIT_COMPLETE">
<input type="hidden" name="db" value=$db>
<input type="hidden" name="source" value="$source">
<input type="hidden" name="date_created" value="$date_created">
<center><table CELLPADDING=2 WIDTH="90%"  >

	<tr>
	<td><font size=-1>
		<a href="javascript:void(0)" 
		onClick="openDescription('Source',
				  'This is a source abbreviation.  Typically the source will be composed of the stripped source and the version');">Source</a>:</font></td>

	<td><font size="-1">$source</font></td>
	</tr>

	<tr>
	<td><font size=-1>
		<a href="javascript:void(0)" 
		onClick="openDescription('Date Created',
				  'This is the date this record was first created, not editable.');">Date Created</a>:</font></td>

	<td><font size="-1">$date_created</font></td>
	</tr>

	<tr>
	<td ><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('META release version',
				  'CURRENT, NOT_RELEASED or the version of the Metathesaurus in which the source was released, e.g., 2003AA.');">META version</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="meta_ver" value="$meta_ver"></font></td>
	</tr>


	<tr>
	<td ><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Status',
				  'Status of this Source (New/Update)');">Status</a>:</font></td>
	<td><font size="-1"><input type="radio" name="status" value="Update" $r1_update_checked>Update &nbsp;&nbsp;<input type="radio" value="New" name=status $r1_new_checked>New</font></td>
	</tr>
	<tr>
	<td ><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Initial Receipt Date',
				  'The date when we first received this source. (DD-Mon-YYY)');">Initial Receipt Date</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="init_rcpt_date" value="$init_rcpt_date"></font></td>
	</tr>
	<tr>
	<td ><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Clean Receipt Date',
				  'The date when this source was received in its final form. (DD-Mon-YYY)');">Clean Receipt Date</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="clean_rcpt_date" value="$clean_rcpt_date"></font></td>
	</tr>
	<tr>
	<td ><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Test Insertion Date',
				  'The date of the test insertion. (DD-Mon-YYY)');">Test Insertion Date</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="test_insert_date" value="$test_insert_date"></font></td>
	</tr>
	<tr>
	<td ><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Real Insertion Date',
				  'The date of the real insertion. (DD-Mon-YYY)');">Real Insertion Date</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="real_insert_date" value="$real_insert_date"></font></td>
	</tr>

	<tr>
	<td><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Source Contact',
				  'Source Contact for this source. (4000 Chars)');">Source Contact</a>:</font></td>
	<td><font size="-1"><input type="text" size="70" name="source_contact" value="$source_contact"></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Inverter Contact',
				  'Inverter Name and Email address. (200 Chars)');">Inverter Name and email</a>:</font></td>
	<td><font size="-1"><input type="text" size="70" name="inverter_contact" value="$inverter_contact"></font></td>
	</tr>

	<tr>
	<td><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('NLM Path',
				  'The Path to the files on the NLM Machine. (200 Chars)');">NLM Path</a>:</font></td>
	<td><font size="-1"><input type="text" size="70" name="nlm_path" value="$nlm_path"></font></td>
	</tr>
	<tr>
	<td><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Apelon Path',
				  'The Path to the files on the Apelon Machine. (200 Chars)');">Apelon Path</a>:</font></td>
	<td><font size="-1"><input type="text" size="70" name="apelon_path" value="$apelon_path"></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Inversion Script',
				  'Name of the Inversion Script. (100 Chars)');">Inversion Script</a>:</font></td>
	<td><font size="-1"><input type="text" size="70" name="inversion_script" value="$inversion_script"></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Inverter Notes File',
				  'The name of the notes file. (100 Chars)');">Inverter Notes File</a>:</font></td>
	<td VALIGN="TOP"><font size="-1"><input type="text" size="70" name="inverter_notes_file" value="$inverter_notes_file"></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Conserve File',
				  'The name of the conservation of mass file. (100 Chars)');">Conservation of Mass file</a>:</font></td>
	<td><font size="-1"><input type="text" size="70" name="conserve_file" value="$conserve_file"></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Additional SABs',
				  'The list of SABs associated with this source. List only the associated SAB names. Do not include the main SAB. This will appear in the Meta Sources Page. (100 Chars) ');">Additional SABs</a>:</font></td>
	<td><font size="-1"><input type="text" size="70" name="sab_list" value="$sab_list"></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('MEOW Display Name',
				  'This is the description that will appear in the Meta Sources page. (4000 Chars)');">MEOW Display Name</a>:</font></td>
	<td><font size="-1"><textarea wrap="off" cols="70" rows="10" name="meow_display_name">$meow_display_name</textarea></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Source Description',
				  'Description of the Source. (4000 Chars)');">Source Description</a>:</font></td>
	<td><font size="-1"><textarea wrap="on" cols="70" rows="15" name="source_desc">$source_desc</textarea></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Worklist Sortkey Location',
				  'Location for the worklist sortkey. (100 Chars)');">Worklist Sortkey Location</a>:</font></td>
	<td><font size="-1"><input type="text" size="70" name="worklist_sortkey_loc" value="$worklist_sortkey_loc"></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('What is New',
				  'Description of what is new in this version (4000 Chars).');">What's New</a>:</font></td>
	<td><font size="-1"><textarea wrap="on" cols="70" rows="15" name="whats_new">$whats_new</textarea></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Termgroup List',
				  'Termgroup List and Description (4000 Chars).');">Termgroup List</a>:</font></td>
	<td><font size="-1"><textarea wrap="on" cols="70" rows="15" name="termgroup_list">$termgroup_list</textarea></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Attribute List',
	'Attribute List and Description. (4000 chars)');">Attribute List</a>:</font></td>
	<td><font size="-1"><textarea wrap="on" cols="70" rows="15" name="attribute_list">$attribute_list</textarea></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Inversion Notes',
	'Inversion Notes. (4000 chars)');">Inversion Notes</a>:</font></td>
	<td><font size="-1"><textarea name="inversion_notes" wrap="on" cols="70" rows="15">$inversion_notes</textarea></font></td>
	</tr>
	<tr>
	<td  VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('',
	'URLs associated with this source. Comma separated list. Enter with or without http:// prefix (500 Chars)');">URLs</a>:</font></td>
	<td><font size="-1"><textarea name="internal_url_list" wrap="off" cols="70" rows="3">$internal_url_list</textarea></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Additional Notes',
	'Addition notes. This field provides additional space for notes that may not fit in the other fields. (4000 chars)');">Additional Notes</a>:</font></td>
	<td><font size="-1"><textarea name="notes" wrap="on" cols="70" rows="15"">$notes</textarea></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('NLM Notes',
	'Notes field for NLM use. (4000 chars)');">NLM Notes</a>:</font></td>
	<td><font size="-1"><textarea name="nlm_editing_notes" wrap="on" cols="70" rows="15"">$nlm_editing_notes</textarea></font></td>
	</tr>
	<tr>
	<td VALIGN="TOP"><font size=-1>
		<a href="javascript:void(0)"
		onClick="openDescription('Inversion Recipe Location',
				  'Path to the Inversion Recipe file. Use the form Sources/RECIPE.sab.html. No slash is needed before the directory name. (100 Chars)');">Inversion Recipe</a>:</font></td>
	<td><font size="-1"><input type="text" size="70" name="inv_recipe_loc" value="$inv_recipe_loc"></font></td>
	</tr>

<!-- removed 08/15/02 - WAK -->
<!--	<tr><td VALIGN="TOP"><font size=-1>  -->
<!--	<a href="javascript:void(0)"  -->
<!--	onClick="openDescription('NLM Editors Report',  -->
<!--	'Selecting this checkbox creates a report for the NLM Editors. Default is ON');"> Create NLM Editors Report</a>:</font></td>  -->
<!--	<td><input type="checkbox" checked="checked" name="editor_report"> </input> </td></tr>   -->
<!--  -->
<!--	<tr> <td VALIGN="TOP"><font size=-1>  -->
<!--	<a href="javascript:void(0)"  -->
<!--	onClick="openDescription('Sources Page',  -->
<!--	'Selecting this checkbox regenerates the Sources Page. Default is ON');">Regenerate Sources Page </a></font></td>  -->
<!--	<td><input type="checkbox" checked="checked" name="update_sources_page"> </input> </td> </tr>  -->

<!-- Added 08/15/02 - WAK -->
	<tr> <td VALIGN="TOP"><font size=-1>
	<a href="javascript:void(0)"
	onClick="openDescription('Suppress Editor Info',
	'Selecting this checkbox suppresses the NLM Editor information. This source will not appear on the sources page and an HTML is not created. Default is OFF');">Suppress Editor Info</a></font></td>
	<td><input type="checkbox" name="suppress_editor_info" $r1_editrec_checked> </input> </td> </tr>
	<tr>
	<td COLSPAN="2"><center>
			<input type="button" value="&nbsp;&nbsp;Save&nbsp;&nbsp;&nbsp;" onClick='validate(this.form); return true;'>
		&nbsp; &nbsp; &nbsp;
		<input type="button" value="Cancel" onClick='document.location="$cgi?state=INDEX&db=$db"; return true'></center></td>
	</tr>
</table></center>
</form>
<p>
}

} # End of Print_EDIT

###################### Procedure PrintEDIT_COMPLETE ######################
# Write the fields back to the table
# Write the {SAB}.html page and the Sources index page
# unless these were turned off by the user in the edit form

sub Print_EDIT_COMPLETE {
# these are all hidden fields
#print "Source: $source<P>\n";
#print "META_VER: $meta_ver<P>\n";
#print "Date_Created: $date_created<P>\n";

	# For all non-<textarea> fields
	# Strip multiple spaces, newline characters, tabs, etc
	# Strip leading whitespace
	# Strip trailing whitespace
	# %7C is encoding for pipe char
	map {
		#s/(\t|\s{2,}|\n|\r)/ /g;
		s/^\s{1,}(.*)/$1/;
		s/(.*)\s{1,}$/$1/;
		s/%7C/|/g;
	} ($source, $meta_ver, $status, $init_rcpt_date,
	$clean_rcpt_date, $test_insert_date, $real_insert_date, $source_contact,
	$inverter_contact, $nlm_path, $apelon_path, $inversion_script,
	$inverter_notes_file, $conserve_file, $sab_list,
	$status, $worklist_sortkey_loc, $inv_recipe_loc,
	$suppress_edit_rec);

	# For <textarea> fields
	# Preserve multiple spaces, newline characters, tabs, etc
	# Strip leading whitespace
	# Strip trailing whitespace
	# 2006/01/16 - removed $internal_url_list from mapping and 
	map {
		#s/(\t|\s{2,}|\n|\r)/ /g;
		s/\n/<BR>/g;
		s/^\s{1,}(.*)/$1/;
		s/(.*)\s{1,}$/$1/;
	}	($meow_display_name,$source_desc, $whats_new, $termgroup_list, $attribute_list, 
		$inversion_notes, $whats_new, $notes, $nlm_editing_notes);

{
	#print "$internal_url_list\n";
	my($urls,@ar,$y,$str);
	$urls = $internal_url_list;
	$urls =~ s/http:\/\///g;
	$urls =~ s/[;,]/ /g;
	$urls =~ s/\s+/ /g;
	@ar = split(/ /,$urls);
	foreach $y (@ar){ $str .=  "http://$y, "}
	$str =~ s/, $//;
	$internal_url_list = $str;
}
	# update info
if($inv_recipe_loc eq ''){
	my($lc_src)= lc($source);
	$inv_recipe_loc = "Sources/RECIPE.$lc_src.html";
}
if($suppress_editor_info eq 'on'){$suppress_edit_rec = 'Y'}
else{$suppress_edit_rec = 'N'}

my $update_handle = $dbh->prepare(qq{
	UPDATE sims_info
		SET meta_ver = ?,
			date_created = ?,
			status = ?,
			init_rcpt_date = ?,
			clean_rcpt_date = ?,
			test_insert_date = ?,
			real_insert_date = ?,
			source_contact = ?,
			inverter_contact = ?,
			nlm_path = ?,
			apelon_path = ?,
			inversion_script = ?,
			inverter_notes_file = ?,
			conserve_file = ?,
			sab_list = ?,
			meow_display_name = ?,
			source_desc = ?,
			worklist_sortkey_loc = ?,
			whats_new = ?,
			termgroup_list = ?,
			attribute_list = ?,
			inversion_notes = ?,
			internal_url_list = ?,
			notes = ?,
			nlm_editing_notes = ?,
			inv_recipe_loc = ?,
			suppress_edit_rec = ?
	WHERE source = ? });

	$row_count = $update_handle->execute( $meta_ver, $date_created, $status, $init_rcpt_date, $clean_rcpt_date, $test_insert_date, $real_insert_date, $source_contact, $inverter_contact, $nlm_path, $apelon_path, $inversion_script, $inverter_notes_file, $conserve_file, $sab_list, $meow_display_name, $source_desc, $worklist_sortkey_loc, $whats_new, $termgroup_list, $attribute_list, $inversion_notes, $internal_url_list, $notes, $nlm_editing_notes, $inv_recipe_loc, $suppress_edit_rec, $source);

print "Date Created: $date_created<P>\n";
#print "Row Count: $row_count<P>\n";
	unless (defined($row_count)) {
	((print "<span id=red>Error updating source info ($DBI::errstr).</span>")
	         &&  return);
	}

	($row_count < 2) ||
	((print "<span id=red>Update affected too many rows ($source,$row_count).</span>")             &&  return);

	($row_count == 1) ||
	((print "<span id=red>Update affected too few rows ($source,$row_count.<br><br>Most likely this source '$source' is not in source_rank.</span>")
	         &&  return);

	print qq{
	<i>The source information for <b>$source</b> was updated in <B>$db</B>.<BR>  
	The following values were used:</i><br><br>
	};

	# prints the fields to the screen to show what values were used
	&PrintSourceReport;	

	if($suppress_edit_rec eq 'Y'){ 
		print "The Editors Report<B> <A HREF=\"/Sources/$source.html\">";
		print "$source.html</A> </B> was <B>NOT</B> create/updated.<P>\n";
 		print "The <A HREF=\"/Sources/sources.html\">";
 		#print "<B>Metathesaurus Sources Page</B></A> was <B>NOT</B> updated. (Don't forget to hit 'Reload' or 'Refresh')\n";
	}
	else {
		print "The Editors Report<B> <A HREF=\"/Sources/$source.html\">";
		print "$source.html</A> </B> was updated.<P>\n";
 		print "The <A HREF=\"/Sources/sources.html\">";
		&PrintEditorReport();
	}
 	print "<B>Metathesaurus Source Page</B></A> was updated.\n";
 	&UpdateSourcesPage();


## removed 8/15/02 - WAK
## 	if($update_sources_page eq 'on'){ 
## 		#print "\$update_source_page: $update_sources_page<BR>\n";
## 		print "The <A HREF=\"/Sources/sources.html\">";
## 		print "<B>Metathesaurus Source Page</B></A> was updated.\n";
## 		&UpdateSourcesPage();
## 	}
## 	else {
## 		#print "\$update_source_page: $update_sources_page<BR>\n";
## 		print "The <A HREF=\"/Sources/sources.html\">";
## 		print "<B>Metathesaurus Source Page</B></A> was <B>NOT</B> updated.\n";
##	}

}; # end PrintEDIT_COMPLETE


###################### Procedure Print_DELETE_CONF ######################
## Confirm before Delete a Record

sub Print_DELETE_CONF {
# Delete a Record
	print qq{
	<CENTER>
	Please confirm that you wish to delete <B>$source</B><P>
	</CENTER>
	<form method="GET" action="$cgi">
<font size="-1">
<input type="hidden" name="source" value="$source">
<input type="hidden" name="db" value=$db>
<input type="hidden" name="state" value="DELETE">
<center><table CELLPADDING=2 WIDTH="90%"  >
	<tr >
	<td COLSPAN="2" ><center>
			<input type="submit" value="  DELETE  ">
		&nbsp; &nbsp; &nbsp;
		</center></td>
	</tr>

</table></center>
</form>
<p>

}
}

###################### Procedure Print_DELETE ######################
sub Print_DELETE {
	my($row_count);
# first, copy the record to the dead table
	$row_count = $dbh->do(qq{
	INSERT INTO DEAD_SIMS_INFO 
	SELECT * FROM SIMS_INFO WHERE SOURCE=?},{"dummy"=>1},$source);
	unless (defined($row_count)) {
	((print "<span id=red>Error updating source info ($DBI::errstr).</span>")
	 &&  return);
	}

	$row_count = $dbh->do(qq{
	DELETE FROM SIMS_INFO
	WHERE SOURCE=?},{"dummy"=>1},$source);
	
	unless (defined($row_count)) {
	((print "<span id=red>Error updating source info ($DBI::errstr).</span>")
	 &&  return);
	}
		($row_count < 2) || 
	((print "<span id=red>Update affected too many rows ($source,$row_count).</span>")             &&  return);

print qq{
	<P>
	<CENTER>
	The record <B>$source</B> has been deleted.
	<P>
	A copy of this record has been saved to the table <B>DEAD_SIMS_INFO</B>
	<CENTER>
	<P>
};


}

###################### Procedure MakeLink ######################
#
#	This procedure takes a string of urls and makes each url into a link
sub MakeLink{
	my($internal_url_list,$url_str,@ar,$x);
	$internal_url_list = shift;
	$internal_url_list =~ s/ //g;
	$internal_url_list =~ s/&nbsp;//g;
	@ar = split(/,/,$internal_url_list);
	foreach $x(@ar){ $url_str .= "<A HREF=\"$x\">$x</A><BR> "}
	return($url_str);
}


###################### Procedure PrintSourceReport ######################
#
# This procedure prints info for a source in a table for onscreen display. 
# It uses the CGI variables for data
#
sub PrintSourceReport {

# convert the urls to actual links
$url_str = MakeLink($internal_url_list);

# For all non-<textarea> fields
# Strip multiple spaces, newline characters, tabs, etc
# Strip leading whitespace
# Strip trailing whitespace

# $termgroup_list =~ s/\n/<BR>/g;

# wrap long lines
 $wrap_inverter_notes_file = wrap('','',$inverter_notes_file);
 $wrap_source_desc = wrap('','',$source_desc);
 $wrap_whats_new = wrap('','',$wrap_whats_new);
 $wrap_notes = wrap("","",$notes);
 $wrap_nlm_editing_notes = wrap("","",$wrap_nlm_editing_notes);



# For <textarea> fields
# Preserve multiple spaces, newline characters, tabs, etc
# Strip leading whitespace
# Strip trailing whitespace
 map {
    #s/(\t|\s{2,}|\n|\r)/ /g;
	 #s/<BR>/\n/g;
	 s/\n/<BR>\n/g;
	 s/ /&nbsp;/g;
 } ($meow_display_name,$wrap_source_desc, $wrap_whats_new, $termgroup_list, $attribute_list, $wrap_inversion_notes, $whats_new, $internal_url_list, $wrap_notes, $wrap_nlm_editing_notes);


	print qq{
<TABLE width="90%">
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Source</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$source</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Date Created</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$date_created</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>META Version: </B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$meta_ver</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Status:  </B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$status</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Initial Receipt Date</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$init_rcpt_date</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Clean Receipt Date</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$clean_rcpt_date</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Test Insertion Date:</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$test_insert_date</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Real Insertion Date</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$real_insert_date</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Source Contact</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$source_contact</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inverter Contact</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$inverter_contact</PRE></TD></TR>
<TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Apelon files location</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$apelon_path</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inversion Script</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$inversion_script</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inverter's Notes</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$wrap_inverter_notes_file</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Conservation of Mass File</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$conserve_file</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Additional SABs</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$sab_list</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>MEOW Display Name</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$meow_display_name</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Source Description</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$wrap_source_desc</DIV></td> </tr></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Worklist Sortkey</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$worklist_sortkey_loc</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>What's New</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$whats_new</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Termgroup List</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$termgroup_list</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Attribute List</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$attribute_list</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inversion Notes</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$inversion_notes</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>URLs for this Source</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$url_str</PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Additional Notes</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$wrap_notes</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>NLM Notes</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$nlm_editing_notes</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inversion Recipe Location</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE><A HREF="/$inv_recipe_loc">/$inv_recipe_loc</A></PRE></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Suppress Editor Information</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE>$suppress_edit_rec</PRE></TD></TR>
</TR>
</TABLE>

	<table width="90%">
	  <tr><td colspan="2">&nbsp;</td></tr>
	  <tr><td colspan="2">
	  <i><a href="$cgi?source=$source&meta_ver=$meta_ver&state=EDIT&db=$db">Edit $source</a></i>
	</td> </tr>
	</table>
	<!-- <hr width="90%"> -->
	</center><P>
	}
}; # end PrintSourceReport


###################### Procedure PrintEditorReport ######################
# Create the Editor's HTML page for /Sources/{SAB}.html
# It uses the CGI variables for data
# Files are kept in $ENV{MEME_HOME}/www/Sources

sub PrintEditorReport {

my($sec, $min, $hour, $day, $month, $yr, $wday, $yday, $isdst) = localtime(time);
$month++;	# month starts with 0
if($month<10){$month = "0".$month}
my($year) = `date +%Y`;
chop($year);
my $lc_src = lc($source);
my $out = "$ENV{MEME_HOME}/www/Sources/$source.html";
#print "NLM Editor's Report: $out<P>\n";
unlink "$out";

open(OUT, ">$out") or die "Can't open $out, $!";
$url_str = MakeLink($internal_url_list);

print OUT <<PART_I;
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<HTML>
	<HEAD>
	<TITLE>
	$source : Inversion Notes
	</TITLE>

<!-- PLEASE NOTE!                  -->
<!-- Created automagically by $cgi -->
<!-- DO NOT EDIT BY HAND!          -->
	<BODY BGCOLOR="FFFFFF">
<H2><CENTER>$source : Inversion Notes
</CENTER></H2>
<TABLE width="90%" CELLPADDING="3">
<TR >
<TD VALIGN="TOP" ALIGN="LEFT"><B>Source</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$source</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Date Created</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$date_created</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>META Version: </B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$meta_ver</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Status:  </B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$status</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Initial Receipt Date</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$init_rcpt_date</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Clean Receipt Date</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$clean_rcpt_date</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Test Insertion Date:</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$test_insert_date</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Real Insertion Date</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$real_insert_date</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Source Contact</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$source_contact</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inverter Contact</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$inverter_contact</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>NLM files location</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$nlm_path</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Apelon files location</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$apelon_path</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inversion Script</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$inversion_script</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inverter's Notes </B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$inverter_notes_file</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Conservation of Mass File</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$conserve_file</DIV></TD></TR>
<TR>
<TD ALIGN="LEFT"><B>Additional SABs</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$sab_list</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>MEOW Display Name</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$meow_display_name</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Source Description</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$source_desc</td> </tr></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Worklist Sortkey</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$worklist_sortkey_loc</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>What's New</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$whats_new</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Termgroup List</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$termgroup_list</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Attribute List</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$attribute_list</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inversion Notes</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$inversion_notes</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>URLs for this Source</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$url_str</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Additional Notes</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$notes</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>NLM Notes</B> </TD>
<TD VALIGN="TOP" ALIGN="LEFT"><DIV Style="font-family: monospace">$nlm_editing_notes</DIV></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Inversion Recipe Location</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><A HREF="/$inv_recipe_loc">/$inv_recipe_loc</A></TD></TR>
<TR>
<TD VALIGN="TOP" ALIGN="LEFT"><B>Test Insertion Report</B></TD>
<TD VALIGN="TOP" ALIGN="LEFT"><PRE><a HREF=\"/Sources/TEST.$lc_src.html\">TEST.$lc_src.html</a>
</PRE></TD></TR>
<TR>
<TD VALIGN=\"TOP\" ALIGN=\"LEFT\"><B>Real Insertion Report</B></TD>
<TD VALIGN=\"TOP\" ALIGN=\"LEFT\"><PRE><a HREF=\"/Sources/INSERTION.$lc_src.html\">INSERTION.$lc_src.html</a>
</PRE></TD></TR>
<TR>
<TD VALIGN=\"TOP\" ALIGN=\"LEFT\"><B>Source Inversion Proposal</B></TD>
<TD VALIGN=\"TOP\" ALIGN=\"LEFT\"><PRE>
<a HREF=\"/Sources/si_proposal_$source.html\">si_proposal_$source.html</a>
</PRE></TD></TR>
</TR>
</TABLE>
<HR>
<!-- test -->
<ADDRESS>Contact: <A HREF="mailto:reg\@msdinc.com">reg\@msdinc.com </A></ADDRESS>
<ADDRESS>Page Updated: $hour:$min $month/$day/$year </ADDRESS>
<ADDRESS>Record Created: $date_created </ADDRESS>
<ADDRESS>Last Updated: $hour:$min $month/$day/$year </ADDRESS>
<ADDRESS><A HREF="/index.shtml">Meta News Home</A></ADDRESS>
<!-- These comments are used by the What's new Generator -->
PART_I

printf OUT "<!-- Changed On: %4d/%02d/%02d -->\n", $year,$month,$day;

print OUT <<TAIL;
<!-- Changed by: $inverter_contact -->
<!-- Change Note: Updated $source Inversion Notes -->
<!-- Fresh for: 1 month -->
</BODY>
</HTML>
TAIL

close OUT;


}

###################### Procedure UpdateSourcesPage ######################
# Regenerate the /export/home/meow/Sources/sources.html page
# Read SIMS_INFO for SOURCE, META_YEAR, & MEOW_DISPLAY_NAME

sub UpdateSourcesPage {
my($source,$year,$meow_display_name,$sabs);
#my(%select_lists,%desc);
my(%desc);
my($i,$x);
my($sec, $min, $hour, $day, $month, $yr, $wday, $yday, $isdst) = localtime(time);
$month++;
my($year_date) = `date +%Y`;
chop($year_date);
#source_desc need source, meta_ver, 
$sh = $dbh->prepare(qq{
	SELECT meta_ver,source,meow_display_name,sab_list
	FROM sims_info
	WHERE suppress_edit_rec = 'N' or suppress_edit_rec IS NULL
	ORDER BY meta_ver
	}) ||
	((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
	&&  return);
$sh->execute ||
	((print "<span id=red>Error executing query ($DBI::errstr).</span>")
	&&  return);
# read everything in
# store sources in %select_lists where key == year, value == list of sources
# store desc in %desc where key == source name, value == source_desc
 while (($year,$source,$source_desc,$sabs) = $sh->fetchrow_array) {
 	$init = substr($source,0,1);
 	$init{$init}++;
 	#print "Year: $year  Source: $source  Desc: $str<BR>\n";
 	$curr_year = ($curr_year lt $year)?$year:$curr_year;
# 	$select_lists{$year} .= "$source|";
 	$desc{$source}=$source_desc;
 	$sabs{$source}=$sabs;
 }

my $lc_src = lc($source);
#my $out = "$ENV{MEME_HOME}/www/Sources/sources_test.html";
my $out = "$ENV{MEME_HOME}/www/Sources/sources.html";
#print "NLM Editor's Report: $out<P>\n";
open(OUT, ">$out") or die "Can't open $out, $!";
print OUT <<HEAD;
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
<HTML>
	<HEAD>
	<TITLE>
	Source Information 
	</TITLE>
<!-- PLEASE NOTE!!!                                   -->
<!-- Created automagically by $cgi -->
<!-- Do not edit this file by hand!!                  -->
	<BODY BGCOLOR="FFFFFF">
<!-- <H2><FONT color="red">TEST VERSION of this PAGE</FONT> </H2> -->
<H1><CENTER>Metathesaurus Source Information </CENTER></H1>

HEAD

 # printf OUT "%s","<ADDRESS>Last Updated: ";
 # printf OUT "%2.2d/%2.2d/%d %s\n",$month,$day,$year_date,"&nbsp;&nbsp";
 # printf OUT "%2.2d:%2.2d %s", $hour,$min,"&nbsp;&nbsp;";
 # printf OUT "</ADDRESS><P>";

#print OUT "<P>count in sorted_vers = $#sorted_vers<P>\n";
#foreach $ver (@sorted_vers){
#	print OUT "[$ver]:$select_lists{$ver}<P>\n";
#}

#foreach $ver (@sorted_vers){
#	print OUT "[$ver]:$select_lists{$ver}<P>\n";
#}

# print just the sources for this current year
print OUT "<A NAME=top></A>\n";
# print OUT "<H3>Current Version - $curr_year</H3>\n";
# foreach $ver (reverse sort keys %select_lists) {
foreach $ver (@sorted_vers){
	#next unless($ver =~ /\d{4}\D{2}/);
	@sources = split /\|/, $select_lists{$ver};
	#print OUT "<A NAME=$year></A>";
	print OUT "<H3>$ver Sources </H3><BR>\n";
	foreach $source (sort(@sources)){
		if($source){
			print OUT "<A HREF=\"$source.html\">$source</A><BR>\n";
		}
	}
	last;
}

print OUT "<P>\n";

# print the Alpha List ( A B C D E F G H ...)
# make the letter a link if there are sources with that initial letter
print OUT "<H3>Jump To Individual Sources</H3><P>\n";
foreach $I(A..Z){
	if($init{$I}){ 
		print OUT "<A HREF=\"#$I\">$I</A>&nbsp;&nbsp;\n"; 
	}
	else{ 
		print OUT "$I&nbsp;&nbsp;&nbsp;\n";
	}
}
		
print OUT "<P>\n";
# print a list of Meta Vers if there are sources for that year
print OUT "<H3>View By META Version</H3>\n";
#foreach $ver (reverse sort keys %select_lists){
foreach $ver (@sorted_vers){
	#next unless($ver =~ /\d{4}\D{2}/);
	print OUT "<A HREF=\"#meta$ver\">$ver</A><BR>\n";
}
print OUT "<P>\n";

# print each individual source name, with a link to its source page
# and the MEOW_DISPLAY_NAME (short desc) for each source in SIMS_INFO
# sorted by alpha, with a Header initial for each letter that has sources
%init=();
foreach $source (sort keys %desc){
	if($source){
		$i = substr($source,0,1);
		unless($init{$i}){
			print OUT "<A HREF=\"#top\">Jump to Top</A><P>\n";
			print OUT "<A NAME=\"$i\"><B>$i</B></A><HR>\n";
			$init{$i}++;
		}
		print OUT "<DT><A HREF=\"$source.html\">$source</A>\n";
		if($sabs{$source}){
			print OUT "&nbsp;&nbsp;&nbsp;[$sabs{$source}]";
		}
		print OUT "</DT>\n";
		print OUT "<DD>$desc{$source}</DD><P>\n";
	}
}

# print out a list of sources with links to their editor's notes page
# sorted by Meta Year

$slcount = scalar(keys(%select_lists));

#print OUT "<h3>Sources By Year</H3>\n";
#print OUT "Count of select_lists: $slcount";
#foreach $ver (reverse sort keys %select_lists) {
foreach $ver (@sorted_vers) {
	@sources = split /\|/, $select_lists{$ver};
	print OUT "<P>\n";
	print OUT "<A NAME=meta$ver></A>";
	print OUT "<HR>\n";
	print OUT "<H3> Sources Updated for $ver</H3>\n";
	foreach $source (sort(@sources)){
		if($source){
			print OUT "<A HREF=\"$source.html\">$source</A><BR>\n";
		}
	}
	print OUT "<P><A HREF=\"#top\">Jump to Top</A><P>\n";
	print OUT "<P>\n";
}
print OUT <<TAIL_TEXT;
<HR>
<ADDRESS>Contact: <A HREF="mailto:reg\@msdinc.com">reg\@msdinc.com</A><ADDRESS>
<ADDRESS>Created: March 25, 1997</ADDRESS>
TAIL_TEXT

printf OUT "%s","<ADDRESS>Last Updated: ";
printf OUT "%2.2d:%2.2d %s", $hour,$min,"&nbsp;&nbsp;";
printf OUT "%2.2d/%2.2d/%d %s\n",$month,$day,$year_date,"</ADDRESS>";

print OUT "<ADDRESS><A HREF=\"/index.shtml\">Meta News Home</A></ADDRESS>\n";
print OUT "<!-- These comments are used by the What's new Generator -->\n";
printf OUT "%s %d/%2.2d/%2.2d%s\n","<!-- Changed On:",$year_date,$month,$day," -->\n";
print OUT <<TAIL_TEXT_II;
<!-- Changed by:  $cgi -->
<!-- Change Note: Updated Sources page-->
<!-- Fresh for: 1 month -->
</BODY>
</HTML>
TAIL_TEXT_II


}	# end of UpdateSourcePage()

###################### Procedure Print_VIEW ######################
# This is called from the INDEX state if the user just wants to view the record
# This procedure prints all of the table names in user tables
#
sub Print_VIEW {
my $view_handle = $dbh->prepare(qq{
	SELECT source, meta_ver, date_created, init_rcpt_date, 
	clean_rcpt_date, test_insert_date, real_insert_date, source_contact, 
	inverter_contact, nlm_path, apelon_path, inversion_script, 
	inverter_notes_file, conserve_file, sab_list, 
	meow_display_name, source_desc, status, worklist_sortkey_loc,
	termgroup_list, attribute_list, inversion_notes, internal_url_list, 
	notes, nlm_editing_notes, inv_recipe_loc, whats_new
	FROM sims_info WHERE source=?
	}) || 
	((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>") );
$view_handle-> execute($source) ||
((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
&&  return);

unless (($source, $meta_ver, $date_created, $init_rcpt_date, 
$clean_rcpt_date,$test_insert_date, $real_insert_date, $source_contact, 
$inverter_contact, $nlm_path, $apelon_path, $inversion_script, 
$inverter_notes_file, $conserve_file, $sab_list, 
$meow_display_name, $source_desc, $status,
$worklist_sortkey_loc, $termgroup_list, $attribute_list, 
$inversion_notes, $internal_url_list, $notes, $nlm_editing_notes,
$inv_recipe_loc, $whats_new) = $view_handle->fetchrow_array) {
((print "<span id=red>Source $source is not in sims_info. ($DBI::errstr)</span>")
&&  return);
};

	$view_handle->finish();
	print "META_VERSION: $meta_ver<P>\n";

	&PrintSourceReport;
	print "The Editors Report<B> <A HREF=\"/Sources/$source.html\">";
	print "$source.html</A> </B><P>\n";
}

################################# PROCEDURES #################################
#
#	The following are useful procedures
#
#############################################################################

###################### Procedure PrintVersion ######################
#
# This prints help information from the command line
#
sub PrintVersion {
	my($type) = @_;
	print "Release $release: version $version, $version_date ($version_authority).\n" 
		if $type eq "version";
	print "$version\n" if $type eq "v";
	return 1;
}


###################### Procedure PrintHelp ######################
#
# This prints help information from the command line
#
sub PrintHelp {
	print qq{
This script is a CGI script used as a template for other scripts. 
It takes CGI arguments in the standard form "key=value&key=value...". 

Parameters:

	state		 :	This is an internal variable representing what
					 state the application is in.
	db			:	Database name
	meme_home	 :	Value of $MEME_HOME that the script should use.

	Version: $version, $version_date ($version_authority)

};		
} # End Procedure PrintHelp


