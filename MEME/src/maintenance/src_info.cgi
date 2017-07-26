#!@PATH_TO_PERL@
#
# File:     src_info.cgi
# Author:   Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version info:
# 07/24/2003 3.4.0: Allows editing of citation field.
# 03/25/2003 3.3.0: Upgraded to use sims_info/source_rank to maintain data
# 07/17/2002 3.2.0: Upgraded to keep track of db if set in URL
# 4/12/2001 3.1.0: 
#    Upgrades: normalized_source,stripped_source,restriction_level added
#              popups on the form fields were added to describe them
#              Mechanism to just report source info (for printing)
#             
# 01/30/2002 3.0.5: Released
#                   midsvcs names changed from things like
#                   current-editing-tns to editing-db 
# 11/12/2001 3.0.4: On Index page, current versions of sources are 
#                   shown in red.
# 09/05/2001 3.0.3: CXTY support added and tested.
# 04/09/2001 3.0.1: First version
#
$release = "4";
$version = "4.0";
$version_authority = "BAC";
$version_date = "07/24/2003";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";

#
# Parse command line arguments to determine if -v{ersion}, or --help 
# is being used
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
if ($ENV{"REQUEST_METHOD"} eq "GET") {
    $_=$ENV{"QUERY_STRING"};
} else {
    $_=<STDIN> || die "Method $ENV{REQUEST_METHOD} not supported.";
}

#
# This is the set of Valid arguments used by readparse. 
# Altering it alters what CGI variables are read.
#
%meme_utils::validargs = ( 
		  "state" => 1, "source_family" => 1,
    "official_name" => 1, "source_version" => 1,
    "valid_start_date" => 1, "valid_end_date" => 1,			  
    "insert_meta_version" => 1, "remove_meta_version" => 1,
    "nlm_contact" => 1, "inverter_contact" => 1, "acquisition_contact" => 1,
    "content_contact" => 1, "release_url_list" => 1, "source" =>  1,
    "license_contact" => 1, "normalized_source" => 1, "citation" => 1,
    "stripped_source" => 1, "restriction_level" => 1, "db" => 1,
    "context_type" => 1, "language" => 1, "current_only" => 1, 
			  "oracle_home" => 1
);

#
# Set environment variables after parsing arguments
#
$ENV{"ORACLE_HOME"} = $oracle_home || $ENV{"ORACLE_HOME"} || die "\$ORACLE_HOME must be set.";

#
# Set environment variables
# This section must be updated when script is moved to another machine
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
$state = "INDEX" unless $state;


#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MID, current MID
#
#$db="oc_testsw"; 
$db = &meme_utils::midsvcs("production-db") unless $db;
$date = `/bin/date +%Y%m%d`;
$cgi = $ENV{"SCRIPT_NAME"};
$start_time = time;
$style_sheet = &meme_utils::getStyle;
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
($user,$password) = split /\//, $userpass;
chop($password);


$FATAL = 1;


#
# Determine Which action to run, print the body
# Valid STATES:
#
# 1. INDEX. Print the index page
# 2. EDIT_FORM.  Form for editing src info
# 3. EDIT_COMPLETE. Page when edit is done
# 4. REPORT_SOURCE. Show all info for a source (for printing)
# 5. REPORT_ALL. Show info for ALL sources.

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","Index Page","Source Info Editor"],
	     "EDIT_FORM" => 
	         ["PrintHeader","PrintEDIT_FORM","PrintFooter","Edit Info for $source","Edit Info for $source"],
	     "EDIT_COMPLETE" => 
	         ["PrintHeader","PrintEDIT_COMPLETE","PrintFooter","Edit Complete","Changes to $source Complete"],
	     "REPORT_SOURCE"=>
	         ["PrintHeader","PrintREPORT_SOURCE","PrintFooter","Report for $source","Report for $source"],
	     "REPORT_ALL"=>
	         ["PrintHeader","PrintREPORT_ALL","PrintFooter","Report All Sources","Report All Source Information"],
	     "" => 
	         ["PrintHeader","None","PrintFooter",""] 
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


#    print "$header, $body, $footer\n";
#    exit(0);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    &$header($states{$state}->[3],$states{$state}->[4]);
    &$body;
    &$footer;

    $dbh->disconnect;
}

#
# We're done, exit 
#
exit(0);


################################# PROCEDURES #################################
#
#  The following procedures print HTML code for the script
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
    
    if ($state ne "EDIT_FORM") { return ""; };
    return qq{

    <script language="JavaScript">

    function openDescription (check,dsc) {
	    var html = "<html><head><title>Description: "+check;
	    html = html + "</title></head><body bgcolor=#ffffff><font size=-1>" + dsc + "<center><form><input type=button onClick='window.close(); return true' value='Close'></form></center></font></body></html>";
	    var win = window.open("","","scrollbars,width=250,height=150,resize=true");
	    win.document.open();
	    win.document.write(html);
	    win.document.close();
	}; // end openDescription

    function validate ( form ) {

	// Validate the date fields
        if (!checkDate(form.valid_start_date)) {
	    alert ("The valid start date must have the form 'dd-mmm-yyyy' or 'dd-mmm-yyyy hh:mm:ss'");
	    form.valid_start_date.focus();
	return;  }

        if (!checkDate(form.valid_end_date)) {
	    alert ("The valid end date must have the form 'dd-mmm-yyyy' or 'dd-mmm-yyyy hh:mm:ss'");
	    form.valid_end_date.focus();
	return;  }

	// Validate the release versions YYYYAA or YYYYAA_##
        if (!checkMetaVersion(form.insert_meta_version)) {
	    alert ("The insert meta version must have a form like '2000AA' or '2000AA_02'");
	    form.insert_meta_version.focus();
	return;  }


        if (!checkMetaVersion(form.remove_meta_version)) {
	    alert ("The insert meta version must have a form like '2000AA' or '2000AA_02'");
	    form.insert_meta_version.focus();
	return;  }

	form.submit();

    }

function checkMetaVersion(version) 
{
    str = version.value;
    if (str=="") {
	return true;
    }
    if (str.length != 6 && str.length != 9) {
	return false; }

    var ch1 = str.substring(0,1);
    var ch2 = str.substring(1,2);
    var ch3 = str.substring(2,3);
    var ch4 = str.substring(3,4);
    var ch5 = str.substring(4,5);
    var ch6 = str.substring(5,6);
    if ((ch1 < "0" || ch1 > "9") ||
	(ch2 < "0" || ch1 > "9") ||
	(ch3 < "0" || ch1 > "9") ||
	(ch4 < "0" || ch1 > "9") ||
	(ch5 < "A" || ch5 > "Z") ||
	(ch5 < "A" || ch5 > "Z")) {
	return false; }
    if (str.length == 6) { return true; }
    ch1 = str.substring(6,7);
    ch2 = str.substring(7,8);
    ch3 = str.substring(8,9);
    if ((ch1 != "_") ||
	(ch2 < "0" || ch1 > "9") ||
	(ch3 < "0" || ch1 > "9")) {
	return false; }
    return true;
}

function checkDate(date)
{
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
    </script>
};

 
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
	<table BORDER=0 COLS=2 WIDTH="100%"  >
	  <tr >
	    <td ALIGN=LEFT VALIGN=TOP >
	      <address><font size="-2"><a href="/">Meta News Home</a></font></address>
            </td>
	    <td ALIGN=RIGHT VALIGN=TOP >
	      <font size="-2"><address>Contact: <a href="mailto:carlsen\@apelon.com">Brian A. Carlsen</a></address>
	      <address>Generated },scalar(localtime),qq{</address>
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
# This procedure prints a list of sources whose info can be edited
#
sub PrintINDEX {

    if ($current_only) {
      $clause = " AND a.source IN (SELECT current_name FROM source_version) "; 
      $toggle_text = qq{See a list of <a href="$cgi">all sources</a>.};
    } else {
      $toggle_text = qq{Restrict to a list of <a href="$cgi?state=INDEX&current_only=1">current sources only</a>.};
    }


    print qq{
    <p>
    This tool is used for editing information about the Metathesaurus
    sources. Current versions are shown in <span style="color: #000099">blue</span>.
    <ul>
      <li>$toggle_text</li>
      <li>See a <a href="$cgi?state=REPORT_ALL&db=$db" onMouseOver='window.status="Report All sourc einformation"; return true;' onMouseOut='window.status=""; return true;'>report of all sources</a>.</li>
      <li>Edit or view a report for one of the following sources:</li>
    </ul>
    </p> 
    <center>
    <table width="90%">
      <tr><td align="center">
	<form action="$cgi" method="GET">
	  <input type="hidden" name="db" value="$db">
	  <select name="source" size="20"
	     onDblClick='this.form.state.value="EDIT_FORM"; this.form.submit(); return true;'>
};

    #
    # prepare and execute statement
    #
    $sh = $dbh->prepare(qq{
    SELECT DISTINCT a.source,source_official_name,
       DECODE((select count(*) from source_version b
	       where stripped_source=b.source
	         and current_name=a.source),0,'','style="color: #000099;"')
    FROM source_rank a, sims_info c WHERE a.source=c.source $clause
    ORDER BY 1,2 
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    while (($ab,$pt,$style) = $sh->fetchrow_array) {
	$abpt = "$ab, $pt";
	$abpt =~ s/(.{1,90})(.*)/$1/;
	if ($2) {$abpt = "$abpt..."};
	print qq{              <font size="-1"><option $style value="$ab">$abpt</option></font>
};
    }

    # finish select list, form, and table
    print qq{
          </select>
        <input type="hidden" name="state" value="EDIT_FORM">
	<br><br><center>
	    <input type="button" value="  Edit  " onClick='this.form.state.value="EDIT_FORM"; this.form.submit(); return true;'>&nbsp;&nbsp;&nbsp;
	    <input type="button" value="Report" onClick='this.form.state.value="REPORT_SOURCE"; this.form.submit(); return true;'>
        <center><br>      
        </form>
      </td></tr>
    </table>
    </center>
    };

}; # end PrintINDEX


###################### Procedure PrintEDIT_FORM ######################
#
# This procedure prints a form for editing src info
#
sub PrintEDIT_FORM {

    # get source info for $source
    $sh = $dbh->prepare(qq{
    SELECT a.source_family, b.source_official_name, a.version,
	b.valid_start_date, b.valid_end_date, b.insert_meta_version,
	b.remove_meta_version, b.nlm_contact, b.inverter_contact,
	b.acquisition_contact, b.content_contact, b.license_contact, 
        b.release_url_list, b.citation,
        a.normalized_source, a.stripped_source, a.restriction_level,
        b.context_type, b.language
    FROM source_rank a, sims_info b 
    WHERE a.source=b.source and a.source=?
    }) || 
    ((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>")  
             &&  return);

    $sh->execute($source) || 
    ((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
             &&  return);

    unless (($source_family,$official_name,$source_version,
	    $valid_start_date,$valid_end_date,
	    $insert_meta_version, $remove_meta_version,
	    $nlm_contact,$inverter_contact,$acquisition_contact,
	    $content_contact, $license_contact, $release_url_list, 
	    $citation, $normalized_source, $stripped_source,
	    $restriction_level,$context_type, $language) = 
	   $sh->fetchrow_array) {

	 ((print "<span id=red>Source $source is not in source_rank. ($DBI::errstr)</span>")  
             &&  return);

    };

    $sh = $dbh->prepare(qq{
    SELECT '<b><font size="-2">(current version)</font></b>'
    FROM source_version WHERE current_name=?
    }) || 
    ((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>")  
             &&  return);

    $sh->execute($source) || 
    ((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
             &&  return);
    
    ($current) = $sh->fetchrow_array;

    # get source_families
    $sh = $dbh->prepare(qq{
	SELECT distinct stripped_source FROM source_rank
    }) || 
    ((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
             &&  return);

    while (($sf) = $sh->fetchrow_array) {
	push @family, $sf;
    }

    # get sources (for normalized source) 
    $sh = $dbh->prepare(qq{
	SELECT distinct source FROM source_rank
    }) || 
    ((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
             &&  return);

    while (($ns) = $sh->fetchrow_array) {
	push @normalized, $ns;
    }

    # get languages
    $sh = $dbh->prepare(qq{
	SELECT lat FROM language
    }) || 
    ((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
             &&  return);

    while (($lat) = $sh->fetchrow_array) {
	push @lat, $lat;
    }
    push @lat, "";

    # set selected rows
    if ($source_family) {
	$sf{$source_family} = "SELECTED";
    } else {
	($stripped_source) = $source =~ /([^\d]*)(\d*)/;
	$sf{$stripped_source} = "SELECTED"; 
    };

    if ($stripped_source) {
	$ss{$stripped_source} = "SELECTED";
    } else {
	($stripped_source) = $source =~ /([^\d]*)(\d*)/;
	$ss{$stripped_source} = "SELECTED"; 
    };

    if ($normalized_source) {
	$ns{$normalized_source} = "SELECTED";
    } else {
	$ns{$source} = "SELECTED"; 
    };

    if ($language) {
	$lat{$language} = "SELECTED";
    } else {
	$lat{""} = "SELECTED"; 
    };

    $rl{$restriction_level}="SELECTED";
    $cxty{$context_type}="SELECTED";

    print qq{

<i>Edit the following fields and click "Done"</i>
<br>&nbsp;
<form method="GET" action="$cgi">
<font size="-1">
<input type="hidden" name="db" value="$db">
<input type="hidden" name="state" value="EDIT_COMPLETE">
<input type="hidden" name="source" value="$source">
<center><table CELLPADDING=2 WIDTH="90%"  >
    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)" 
		onClick="openDescription('Source',
                  'This is a source abbreviation.  Typically the source will be composed of the stripped source and the version');">Source</a>:</font></td>

	<td><font size="-1">$source $current</font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Official Name',
                  'This is the official name of the source.  Typically it will be the same as the SRC/RPT for this source.');">Official Name</a>:</font></td>
	<td><font size="-1"><input type="text" size="60" name="official_name" value="$official_name"></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Normalized Source',
                  'The normalized source is for weird source abbreviations used in the editing database but not for the release.  For example the source MSH2001HMCE would have a normalized source of MSH2001.  During release, the normalized source is used instead of the actual source.');">Normalized Source</a>:</font></td>
	<td><font size="-1"><select name="normalized_source">
	};
    foreach $n (sort @normalized) {
	print "          <option $ns{$n}>$n</option>\n";
    }
    print qq{
        </select></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Source Family',
                   'The source family is used to associate different SABs with the same source.  For example, MTHCH01 is a collection of Metathesaurus Hierarchical terms used in the CPT2001 hierarchies.  Because of this the source family of MTHCH01 is CPT.');">Source Family</a>:</font></td>
	<td><font size="-1"><select name="source_family">
	};
    foreach $f (sort @family) {
	print "          <option $sf{$f}>$f</option>\n";
    }
    print qq{
        </select></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Stripped Source',
                  'The stripped source is the source minus version information.  It is a way of representing SABs in a versionless way.  For example the source MSH2001 has the stripped source MSH.');">Stripped Source</a>:</font></td>
	<td><font size="-1"><select name="stripped_source">
	};
    foreach $f (sort @family) {
	print "          <option $ss{$f}>$f</option>\n";
    }
    print qq{
        </select></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Version',
                  'The version is the source minus the stripped source.  It represents the version associated with a source.  For example, the source MSH2001 has the version 2001.');">Version</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="source_version" value="$source_version"></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Restriction Level',
                   'The restriction level has to do with the various levels of lisence agreements.  There are currently four possible values: 0,1,2, and 3.');">Restriction Level</a>:</font></td>
	<td><font size="-1"><select name="restriction_level">
	    <option $rl{"0"}>0</option>
	    <option $rl{"1"}>1</option>
	    <option $rl{"2"}>2</option>
	    <option $rl{"3"}>3</option>
	    <option $rl{"4"}>4</option>
        </select></font></td>
    </tr>

    <tr>
	<td><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Language',
                   'The language should be set for sources that have atoms and it should be set to the language used to express the atoms.');">Language</a>:</font></td>
	<td><font size="-1"><select name="language">
	};
    foreach $l (sort @lat) {
	print "          <option $lat{$l}>$l</option>\n";
    }
    print qq{
        </select></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Context Type',
                  'The type of contexts that this source has.  Values come from <a href=\\'http://www.nlm.nih.gov/research/umls/META2.HTML#s232\\' target=\\'_blank\\'>section 2.3.2 of the documentation</a>.');">Context Type</a>:</font></td>
	<td><font size="-1"><select name="context_type">
	    <option value="">NO CONTEXTS</option>
	    <option $cxty{"FULL"}>FULL</option>
	    <option $cxty{"FULL-MULTIPLE"}>FULL-MULTIPLE</option>
	    <option $cxty{"FULL-MULTIPLE-NOSIB"}>FULL-MULTIPLE-NOSIB</option>
	    <option $cxty{"FULL-NOSIB"}>FULL-NOSIB</option>
	    <option $cxty{"FULL-MULTIPLE"}>FULL-MULTIPLE</option>
	    <option $cxty{"TITLE"}>TITLE</option>
	    <option $cxty{"TITLE-MULTIPLE"}>TITLE-MULTIPLE</option>
	    <option $cxty{"MINI"}>MINI</option>
	    <option $cxty{"MINI-MULTIPLE"}>MINI-MULTIPLE</option>
        </select></font></td>
    </tr>


    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Valid Start Date',
                  'The valid start date is the date when this particular version of the source became active.  In other words, it is the date it was inserted into the database.');">Valid Start Date</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="valid_start_date" value="$valid_start_date"></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Valid End Date',
                  'The valid end date is the date when this particular version of the source was either replaced or removed from the database.  It is the date that the source ceases to be valid.');">Valid End Date</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="valid_end_date" value="$valid_end_date"></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Insert Meta Version',
                  'The insert meta version is the version of the Metathesaurus in which this source first appeared.  This field must always have a form like 2000AA or 2000AA_01.');">Insert Meta Version</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="insert_meta_version" value="$insert_meta_version"></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Remove Meta Version',
                  'The remove meta version is the version of the Metathesaurus in which this source no longer appeared, either because it was replaced or removed.  This field must always have a form like 2000AA or 2000AA_01.');">Remove Meta Version</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="remove_meta_version" value="$remove_meta_version"></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('NLM Contact',
                  'The NLM contact is the person at NLM who is responsible for this source.')";>NLM Contact</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="nlm_contact" value="$nlm_contact"></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Inverter contact',
                  'The inverter is the person at Apelon who was responsible for inverting this source (producing .src files).');">Inverter Contact</a>:</font></td>
	<td><font size="-1"><input type="text" size="20" name="inverter_contact" value="$inverter_contact"></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Acquisition Contact',
                  'The acquisition contact is the name, address, phone number, and or email of the person who should be contacted about acquiring the source.');">Acquisition Contact</a>:<br><font size=-2>(including name, address, phone, and email)</font></font></td>

	<td><font size="-1"><textarea name="acquisition_contact" wrap="soft" cols="60" rows="4">$acquisition_contact</textarea></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Content Contact',
                  'The contact contact is the name, address, phone number, and or email of the person who should be contacted with questions or issues regarding the content of a source.');">Content Contact</a>:<br><font size=-2>(including name, address, phone, and email)</font></font></td>

	<td><font size="-1"><textarea name="content_contact" wrap="soft" cols="60" rows="4">$content_contact</textarea></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('License Contact',
                  'The license contact is the name, address, phone number, and or email of the person who should be contacted regarding the license agreement for this source.');">License Contact</a>:<br><font size=-2>(including name, address, phone, and email)</font></font></td>

	<td><font size="-1"><textarea name="license_contact" wrap="soft" cols="60" rows="4">$license_contact</textarea></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Citation',
                  'The citation is information for a citation as it should appear in the literature.');">Citation</a>:<br><font size=-2>(including name, location, organization, and version)</font></font></td>

	<td><font size="-1"><textarea name="citation" wrap="soft" cols="60" rows="4">$citation</textarea></font></td>
    </tr>

    <tr>
	<td ><font size=-1>
	    <a href="javascript:void(0)"
		onClick="openDescription('Release URL List',
                  'These are useful urls associated with this source.');">Release URL List</a>:</font></td>

	<td><font size="-1"><textarea name="release_url_list" wrap="soft" cols="60" rows="2">$release_url_list</textarea></font></td>
    </tr>

    <tr >
	<td COLSPAN="2" ><center>
            <input type="button" value="&nbsp;&nbsp;Done&nbsp;&nbsp;" onClick='validate(this.form); return true;'>
		&nbsp; &nbsp; &nbsp;
	    <input type="button" value="Cancel" onClick='document.location="$cgi?state=INDEX&db=$db"; return true'></center></td>
    </tr>

</table></center>
</form>
<p>
}
}; # end PrintEDIT_FORM

###################### Procedure PrintEDIT_COMPLETE ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintEDIT_COMPLETE {

    # Strip multiple spaces, newline characters, tabs, etc
    # Strip leading whitespace
    # Strip trailing whitespace
    map {	
	s/(\t|\s{2,}|\n|\r)/ /g;
	s/^\s{1,}(.*)/$1/;
	s/(.*)\s{1,}$/$1/;
    } ($acquisition_contact, $content_contact, $license_contact, $citation,
       $release_url_list, $official_name, $source_version, $valid_start_date,
       $valid_end_date, $insert_meta_version, $remove_meta_version,
       $nlm_contact, $inverter_contact);

    # update info
    $row_count = $dbh->do(qq{
    UPDATE source_rank
	SET source_family = ?,
	    version = ?,
	    normalized_source = ?, stripped_source = ?,
	    restriction_level = ?
    WHERE source= ?
    },{"dummy"=>1},$source_family,$source_version, $normalized_source, 
	  $stripped_source, $restriction_level, $source );

    unless (defined($row_count)) { 
    ((print "<span id=red>Error updating source info ($DBI::errstr).</span>")  
             &&  return);
    }

    ($row_count < 2) || 
    ((print "<span id=red>Update affected too many rows ($source,$row_count).</span>")
             &&  return);

    ($row_count == 1) || 
    ((print "<span id=red>Update affected too few rows ($source,$row_count.<br><br>Most likely this source '$source' is not in source_rank.</span>")
             &&  return);


    $row_count = $dbh->do(qq{
    UPDATE sims_info
	SET source_official_name = ?,
	    valid_start_date = ?,
	    valid_end_date = ?,
	    insert_meta_version = ?,
	    remove_meta_version = ?,
	    nlm_contact = ?,
            inverter_contact = ?,
            acquisition_contact = ?,
            content_contact = ?,
            license_contact = ?,
            citation = ?,
            release_url_list = ?,
	    context_type = ?, 
	    language = ?
    WHERE source = ?
    },{"dummy"=>1},$official_name,$valid_start_date,
	  $valid_end_date,$insert_meta_version,$remove_meta_version,
	  $nlm_contact,$inverter_contact,$acquisition_contact,$content_contact,
          $license_contact, $citation, $release_url_list, $context_type, $language, 
	  $source );

    unless (defined($row_count)) { 
    ((print "<span id=red>Error updating sims info ($DBI::errstr).</span>")  
             &&  return);
    }

    ($row_count < 2) || 
    ((print "<span id=red>Update affected too many sims_info rows ($source,$row_count).</span>")
             &&  return);

    ($row_count == 1) || 
    ((print "<span id=red>Update affected too few sims_info rows ($source,$row_count.<br><br>Most likely this source '$source' is not in sims_info.</span>")
             &&  return);

    print qq{
    <i>The source information for <b>$source</b> was updated.  The following
    values were used:</i><br><br>
	  };
    &PrintSourceReport;

}; # end PrintEDIT_COMPLETE


###################### Procedure PrintREPORT_SOURCE ################
#
# This procedure prints a page with the info for one source
#
sub PrintREPORT_SOURCE {

    # get source info for $source
    $sh = $dbh->prepare(qq{
    SELECT a.source_family, b.source_official_name, a.version,
	b.valid_start_date, b.valid_end_date, b.insert_meta_version,
	b.remove_meta_version, b.nlm_contact, b.inverter_contact,
	b.acquisition_contact, b.content_contact, b.license_contact, 
	b.citation, b.release_url_list,
        a.normalized_source, a.stripped_source, a.restriction_level, 
        b.context_type, b.language
    FROM source_rank a, sims_info b WHERE a.source=b.source 
    AND b.source=?
    }) || 
    ((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>")  
             &&  return);

    $sh->execute($source) || 
    ((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
             &&  return);

    unless (($source_family,$official_name,$source_version,
	    $valid_start_date,$valid_end_date,
	    $insert_meta_version, $remove_meta_version,
	    $nlm_contact,$inverter_contact,$acquisition_contact,
	    $content_contact, $license_contact, $citation,
	    $release_url_list, $normalized_source, $stripped_source,
	    $restriction_level, $context_type, $language) = $sh->fetchrow_array) {

	 ((print "<span id=red>Source $source is not in source_rank. ($DBI::errstr)</span>")  
             &&  return);

    };

    print qq{
    <i>Following is the source information for <b>$source</b>:
	  };
    &PrintSourceReport;

}; # end PrintREPORT_SOURCE

###################### Procedure PrintREPORT_SOURCE ################
#
# This procedure prints a page with the info for one source
#
sub PrintREPORT_ALL {

    # get source info for $source
    $sh = $dbh->prepare(qq{
    SELECT DISTINCT a.source, a.source_family, b.source_official_name, 
		    a.version, b.valid_start_date, b.valid_end_date,
		    b.insert_meta_version,
	b.remove_meta_version, b.nlm_contact, b.inverter_contact,
	b.acquisition_contact, b.content_contact, 
        b.license_contact, b.citation, release_url_list,
        a.normalized_source, a.stripped_source, a.restriction_level, 
        b.context_type, b.language
    FROM source_rank a, sims_info b 
    WHERE a.source = b.source ORDER BY 1
    }) || 
    ((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
             &&  return);

    print qq{
    <i>Following is the source information for all sources</i>
	  };

    while (($source,$source_family,$official_name,$source_version,
	    $valid_start_date,$valid_end_date,
	    $insert_meta_version, $remove_meta_version,
	    $nlm_contact,$inverter_contact,$acquisition_contact,
	    $content_contact, $license_contact, $citation,
	    $release_url_list, $normalized_source, $stripped_source,
	    $restriction_level, $context_type, $language) = $sh->fetchrow_array) {
	print qq{<p>};
	&PrintSourceReport;
	print qq{</p>};

    };
}; # end PrintREPORT_ALL


###################### Procedure PrintSourceReport ######################
#
# This procedure prints info for a source in a table.. It uses the
# CGI variables for data
#
sub PrintSourceReport {
    print qq{
    	
    <center>
    <table width="90%">
      <tr><td width="30%"><font size="-1">Source</font></td>
          <td width="70%"align="left"><tt><font size="-1">$source</font></tt></td>
      </tr>
      <tr><td><font size="-1">Official Name</font></td>
          <td><tt><font size="-1">$official_name</font></tt></td>
      </tr>
      <tr><td><font size="-1">Normalized Source</font></td>
          <td><tt><font size="-1">$normalized_source</font></tt></td>
      </tr>
      <tr><td><font size="-1">Source Family</font></td>
          <td><tt><font size="-1">$source_family</font></tt></td>
      </tr>
      <tr><td><font size="-1">Stripped Source</font></td>
          <td><tt><font size="-1">$stripped_source</font></tt></td>
      </tr>
      <tr><td><font size="-1">Version</font></td>
          <td><tt><font size="-1">$source_version</font></tt></td>
      </tr>
      <tr><td><font size="-1">Restriction Level</font></td>
          <td><tt><font size="-1">$restriction_level</font></tt></td>
      </tr>
      <tr><td><font size="-1">Language</font></td>
          <td><tt><font size="-1">$language</font></tt></td>
      </tr>
      <tr><td><font size="-1">Context Type</font></td>
          <td><tt><font size="-1">$context_type</font></tt></td>
      </tr>
      <tr><td><font size="-1">Valid Start Date</font></td>
          <td><tt><font size="-1">$valid_start_date</font></tt></td>
      </tr>
      <tr><td><font size="-1">Valid End Date</font></td>
          <td><tt><font size="-1">$valid_end_date</font></tt></td>
      </tr>
      <tr><td><font size="-1">Insert Meta Version</font></td>
          <td><tt><font size="-1">$insert_meta_version</font></tt></td>
      </tr>
      <tr><td><font size="-1">Remove Meta Version</font></td>
          <td><tt><font size="-1">$remove_meta_version</font></tt></td>
      </tr>
      <tr><td><font size="-1">NLM Contact</font></td>
          <td><tt><font size="-1">$nlm_contact</font></tt></td>
      </tr>
      <tr><td><font size="-1">Inverter Contact</font></td>
          <td><tt><font size="-1">$inverter_contact</font></tt></td>
      </tr>
      <tr><td><font size="-1">Acquisition Contact</font></td>
          <td><tt><font size="-1">$acquisition_contact</font></tt></td>
      </tr>
      <tr><td><font size="-1">Content Contact</font></td>
          <td><tt><font size="-1">$content_contact</font></tt></td>
      </tr>
      <tr><td><font size="-1">License Contact</font></td>
          <td><tt><font size="-1">$license_contact</font></tt></td>
      </tr>
      <tr><td><font size="-1">Citation</font></td>
          <td><tt><font size="-1">$citation</font></tt></td>
      </tr>
      <tr><td><font size="-1">Release URL List</font></td>
          <td><tt><font size="-1">$release_url_list</font></tt></td>
      </tr>
      <tr><td colspan="2">&nbsp;</td></tr>
      <tr><td colspan="2">
	  <font size="-1"><i><a href="$cgi?state=INDEX&db=$db">Back to index</a></i></font>
      </td></tr>
    </table>
    </center>
    }
}; # end PrintSourceReport

################################# PROCEDURES #################################
#
#  The following are useful procedures
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
 This script is a CGI script used to edit source information
 It takes CGI arguments in the standard form "key=value&key=value...". 

 Parmaters:

  state         :  This is an internal variable representing what
                   state the application is in (INDEX,EDIT_FORM,
                   EDIT_COMPLETE).
  source        :  The SAB currently being edited
 
  The remaining parameters are all pieces of information associated
  with a source.  They are:
      source_family, official_name, version, valid_start_date,
      valid_end_date, insert_meta_version, remove_meta_version,
      nlm_contact, acquisition_contact, content_contact, release_url_list,
      inverter_contact, license_contact, citation, context_type, language.

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


