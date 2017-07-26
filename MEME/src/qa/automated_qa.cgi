#!@PATH_TO_PERL@
# File:     automated_qa.cgi
# Author:   Brian Carlsen
#
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#
# Changes:
# 03/05/2009 BAC (1-J2CSD): Small change to "adj_report" queries to sort results
# 01/14/2009 BAC (1-J2CSD): Final improvements to ensure MID-MID results are communicated to MRD.
# 12/15/2008 BAC (1-J2CSD): Bug fixes to improvements.
# 10/15/2008 BAC (1-J2CSD): Improvements to normalize all comparisons and displays.
# 09/30/2008 BAC (1-IW5ZV): In SRC_OBSOLETE_DIFF display (current,obsolete) instead of (obsolete,current)
# 09/30/2008 BAC (1-IW5ZV): Use ',%' instead of '%' when looking up SRC_OBSOLETE_DIFF
# 02/28/2008 TK (1-GMAC4) : Modified src_src_obsolete to multi-user application.
# 02/24/2008 TK (1-GE1Q9) : Inversion statistics report.
# 01/24/2008 TK (1-G9SHX) : Added SRC data sampling functionality.
# 12/04/2007 TK (1-FWYOL) : Changed mid_mid_diff_done to save results in rsab and changes
#                           to editing qa report accordingly
# 09/10/2007 TK (1-F7VHL) : Implemented all current source selection.
# 07/27/2007 TK (1-ETTV3) : Obsolete_UI report and SQL generation.
# 06/27/2007 TK (1-EK3H7) : Only changed or new sources will show up in src-src obsolete report.
# 05/08/2007 TK (1-E730H) : SRC and Editing QA reports,  Use released_source_version view, etc.
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version info:
# 05/16/2005 3.2.2: Better UI, compare SRC-SRC
# 04/01/2003 3.2.1: change database, compare to mrd functionality
# 07/17/2002 3.2.0: Upgraded to keep track of db if set in URL
# 01/30/2002 3.1.0: Released
#                   midsvcs names changed from things like
#                   current-editing-tns to editing-db
# 12/06/2001 3.0.6: added ability to "review MID counts"
# 03/20/2001 3.0.5: First version
#
$release           = "4";
$version           = "3";
$version_authority = "BAC";
$version_date      = "10/09/2008";
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Parse command line arguments to determine if -v{ersion}, or --help
# is being used
#
while (@ARGV) {
 if ( $arg eq "-v" ) {
  $print_version = "v";
 } elsif ( $arg eq "-version" ) {
  $print_version = "version";
 } elsif ( $arg =~ /-{1,2}help$/ ) {
  $print_help = 1;
 }
}
&PrintHelp && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

#
# Get Parameters
# If request method is GET, parameters are in variable $QUERY_STRING
# If request method is POST, parameters are in STDIN
#
if ( $ENV{"REQUEST_METHOD"} eq "GET" ) {
 $_ = $ENV{"QUERY_STRING"};
} else {
 $_ = <STDIN> || die "Method $ENV{REQUEST_METHOD} not supported.";
}

#
# This is the set of Valid arguments used by readparse.
# Altering it alters what CGI variables are read.
#
%meme_utils::validargs = (
                           "state"          => 1,
                           "log_name"       => 1,
                           "command"        => 1,
                           "adjustment"     => 1,
                           "adjustment_dsc" => 1,
                           "db"             => 1,
                           "meme_home"      => 1,
                           "qa_id"          => 1,
                           "qa_name"        => 1,
                           "qa_value"       => 1,
                           "qa_id_1"        => 1,
                           "qa_id_2"        => 1,
                           "name"           => 1,
                           "query"          => 1,
                           "source"         => 1,
                           "oracle_home"    => 1,
                           "obsolete"       => 1,
                           "threshold"      => 1,
                           "action"         => 1,
                           "service"        => 1,
                           "arg"            => 1
);

#
# Set environment variables after parsing arguments
#
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
$ENV{"MEME_HOME"} = $meme_home || $ENV{"MEME_HOME"} || $inc;
$ENV{"ORACLE_HOME"} =
     $oracle_home
  || $ENV{"ORACLE_HOME"}
  || die "\$ORACLE_HOME must be set.";
$state = "CHECK_JAVASCRIPT" unless $state;

#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MID, current MID
#
$db          = &meme_utils::midsvcs("editing-db") unless $db;
$cgi         = $ENV{"SCRIPT_NAME"};
$start_time  = time;
$style_sheet = &meme_utils::getStyle;
$threshold = 0;

#
# Open Database Connection
#
# set variables
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
( $user, $password ) = split /\//, $userpass;
chop($password);

# open connection
$dbh = DBI->connect( "dbi:Oracle:$db", "$user", "$password" )
  || ( ( print "<span id=red>Error opening $db ($DBI::errstr).</span>" )
       && return );

# set sort and hash areas
$dbh->do("alter session set sort_area_size=67108864");
$dbh->do("alter session set hash_area_size=67108864");

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
 "CHECK_JAVASCRIPT" => [
                         "PrintHeader",
                         "PrintCHECK_JAVASCRIPT",
                         "PrintFooter",
                         "Check JavaScript",
                         "This page will redirect if JavaScript is enabled."
 ],
 "INDEX" => [
              "PrintHeader", "PrintINDEX",
              "PrintFooter", "Monster QA - Index Page",
              "Automated QA"
 ],
 "SAMPLE_DATA" =>
   [ "PrintHeader", "PrintSAMPLE_DATA", "PrintFooter", "Sample Data" ],
 "PRINT_SAMPLES" =>
   [ "PrintHeader", "PrintSAMPLES", "PrintFooter", "SRC Sample" ],
 "INV_OBSOLETE_DIFF" => [
                          "PrintHeader",
                          "PrintINV_OBSOLETE_DIFF",
                          "PrintFooter",
                          "Difference in INV-INV (obsolete) counts",
                          "Difference in INV-INV (obsolete) counts"
 ],
 "MID_MID_ADJ_REPORT" => [
                           "PrintHeader", "PrintMID_MID_ADJ_REPORT",
                           "PrintFooter", "MID-MID QA Report"
 ],
 "MID_SRC_ADJ_REPORT" => [
                           "PrintHeader", "PrintMID_SRC_ADJ_REPORT",
                           "PrintFooter", "MID-SRC QA Report"
 ],
 "OBSOLETE_UI_REPORT" => [
                           "PrintHeader", "PrintOBSOLETE_UI_REPORT",
                           "PrintFooter", "Obsolete UI Report"
 ],
 "OBSOLETE_UI_SQL" =>
   [ "PrintHeader", "PrintOBSOLETE_UI_SQL", "PrintFooter", "Obsolete UI SQL" ],
 "MID_SRC_DIFF" => [
                     "PrintHeader",
                     "PrintMID_SRC_DIFF",
                     "PrintFooter",
                     "Difference in MID-SRC counts",
                     "Difference in MID-SRC counts"
 ],
 "MID_SRC_DIFF_DONE" => [
                          "PrintHeader",
                          "PrintMID_SRC_DIFF_DONE",
                          "PrintFooter",
                          "Adjustments for MID-SRC Differences",
                          "Adjustments for MID-SRC Differences"
 ],
"SRC_INV_DIFF" => [
                     "PrintHeader",
                     "PrintSRC_INV_DIFF",
                     "PrintFooter",
                     "Difference in SRC-INV counts",
                     "Difference in SRC-INV counts"
 ],
 "SRC_OBSOLETE_DIFF" => [
                          "PrintHeader",
                          "PrintSRC_OBSOLETE_DIFF",
                          "PrintFooter",
                          "Difference in SRC-SRC (obsolete) counts",
                          "Difference in SRC-SRC (obsolete) counts"
 ],
 "SRC_OBSOLETE_DIFF_DONE" => [
                               "PrintHeader",
                               "PrintSRC_OBSOLETE_DIFF_DONE",
                               "PrintFooter",
                               "Adjustments for SRC-SRC (obsolete) Differences",
                               "Adjustments for SRC-SRC (obsolete) Differences"
 ],
 "MID_MID_DIFF" => [
                     "PrintHeader",
                     "PrintMID_MID_DIFF",
                     "PrintFooter",
                     "Difference in MID-MID counts",
                     "Difference in MID-MID counts"
 ],
 "MID_MID_DIFF_DONE" => [
                          "PrintHeader",
                          "PrintMID_MID_DIFF_DONE",
                          "PrintFooter",
                          "Adjustments for MID-MID Differences",
                          "Adjustments for MID-MID Differences"
 ],
 "MID_MID_DIFF" => [ "PrintHeader", "PrintMID_MID_DIFF", "PrintFooter" ],
 "SRC_OBSOLETE_ADJ_REPORT" => [
                                "PrintHeader",
                                "PrintSRC_OBSOLETE_ADJ_REPORT",
                                "PrintFooter",
                                "SRC-SRC Obsolete Adjustment Report"
 ],
 "ADJ_EDIT_DONE" => [
                      "PrintHeader", "PrintADJ_EDIT_DONE",
                      "PrintFooter", "Edit QA Adjustment",
                      "Edit QA Adjustment"
 ],
 "REVIEW_MID_COUNTS" => [
                          "PrintHeader", "PrintREVIEW_MID_COUNTS",
                          "PrintFooter", "Review Raw MID Counts",
                          "Review Raw MID Counts"
 ],
 "REVIEW_SRC_COUNTS" => [
                          "PrintHeader", "PrintREVIEW_SRC_COUNTS",
                          "PrintFooter", "Review Raw SRC Counts",
                          "Review Raw SRC Counts"
 ],
 "REVIEW_INV_COUNTS" => [
                          "PrintHeader", "PrintREVIEW_INV_COUNTS",
                          "PrintFooter", "Review Raw INV Counts",
                          "Review Raw INV Counts"
 ],
 "QUERIES" => [
                "PrintHeader", "PrintQUERIES",
                "PrintFooter", "Edit $command Queries",
                "Edit $command Queries"
 ],
 "QUERIES_DONE" => [
                     "PrintHeader",
                     "PrintQUERIES_DONE",
                     "PrintFooter",
                     "Done Editing $command Queries",
                     "Done Editing $command Queries"
 ],
 "MRD_COPY" => [
                 "PrintHeader", "PrintMRD_COPY",
                 "PrintFooter", "Copy Counts to MRD",
                 "Copy Counts to MRD"
 ],
 "" => [ "PrintHeader", "None", "PrintFooter", "" ]
);

#
# Check to see if state exists
#
if ( $states{$state} ) {

 #
 # Print Header, Body, and Footer
 #
 $header = $states{$state}->[0];
 $body   = $states{$state}->[1];
 $footer = $states{$state}->[2];

 #    print "$header, $body, $footer\n";
 #    exit(0);
 &$header( $states{$state}->[3], $states{$state}->[4] );
 &$body;
 &$footer;
 $dbh->disconnect if $dbh;
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
 my ( $title, $header ) = @_;
 &meme_utils::PrintSimpleHeader( $title, $style_sheet, &HeaderJavascript,
                                 "<h2><center>$header</center></h2>" );
}
###################### Procedure HeaderJavascript ######################
#
# This procedure contains the javascript for the standard header
#
sub HeaderJavascript {
 return qq{
    <script language="javascript">

	function verifyDelete (check,form) {
	    var yesno = confirm('Are you sure you want to delete this check: '+check+'?');
	    if (yesno) {
		form.submit();
	    }
	}; // end verifyDelete


    </script>
};
}    # end HeaderJavascript
###################### Procedure PrintFooter ######################
#
# This procedure prints a standard footer including time to generate
# the page, the current date, and some links
#
sub PrintFooter {

 #
 # Compute the elapsed time
 #
 $end_time     = time;
 $elapsed_time = $end_time - $start_time;

 #
 # Print the Footer
 #
 print qq{
    <hr width="100%">
	<table BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
	  <tr NOSAVE>
	    <td ALIGN=LEFT VALIGN=TOP NOSAVE>
	      <address><a href="$cgi?state=INDEX&db=$db" onMouseOver="window.status='Return to index page.'; return true;" onMouseOut="window.status=''; return true;">Back to Index</a></address>
            </td>
	    <td ALIGN=RIGHT VALIGN=TOP NOSAVE>
	      <font size="-1"><address>Contact: <a href="mailto:brian.a.carlsen\@lmco.com">Brian A. Carlsen</a></address>
	      <address>Generated on:}, scalar(localtime), qq{</address>
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
}    # End of PrintFooter
###################### Procedure PrintCHECK_JAVASCRIPT ######################
#
# This procedure prints a page that verifies that a javascript
# enabled browser is being used
#
sub PrintCHECK_JAVASCRIPT {
 print qq{
	<script language="javascript">
	    document.location='$cgi?state=INDEX&db=$db';
	</script>
        <p><blockquote>
	    You must use a JavaScript enabled browser to run this
	    application (<a href="http://www.netscape.com">Netscape</a>
	    is recommended).  If you are using
	    a JavaScript enabled browser and just have JavaScript
	    disabled, please enable it and click
	    <a href="$cgi?state=INDEX&db=$db">here</a>.
        </blockquote></p>
	};
};    # end printCHECK_JAVASCRIPT
###################### Procedure PrintINDEX ######################
#
# This procedure prints the index page for the application with
# links to the other functionality
#
sub PrintINDEX {
 $selected{$db}               = "SELECTED";
 $dbs                         = &meme_utils::midsvcs("databases");
 $mrd                         = &meme_utils::midsvcs("mrd-db");
 $dbs                         = "$dbs,$mrd";
 $dbSelect                    = getDbSelectForm();
 $srcInvSelect                = getSrcInvSelect();
 $srcSrcObsoleteCompareSelect = getSrcSrcObsoleteCompareSelect();
 $srcSrcObsoleteReportSelect  = getSrcSrcObsoleteReportSelect();
 $obsoleteUiReportSelect      = getObsoleteUiReportSelect();
 $srcSamplesSelect            = getSrcSamplesSelect();
 $invCompareSelect            = getInvCompareSelect();
 $reviewMidCountSelect        = getReviewMidCountSelect();
 $reviewSrcCountSelect        = getReviewSrcCountSelect();
 $reviewInvCountSelect        = getReviewInvCountSelect();

 #
 # Write form.
 #
 print qq{
<i>Select from one of these choices:</i>
<br>&nbsp;

<center><table WIDTH="90%" NOSAVE >

<!-- CHANGE DATABASE -->
<tr nosave>
  <td width="25%">Change Database:</td><td>$dbSelect</td>
</tr>
<tr><td colspan="2">&nbsp;</td></tr>

<!-- MID-SRC Comparison -->
<tr><th colspan="2">Compare Counts</th></tr>
<tr>
<td colspan="2"><b><a href="$cgi?state=MID_SRC_DIFF&db=$db" onMouseOver='window.status="MID-SRC Diff"; return true;' onMouseOut='window.status=""; return true;' onClick="return confirm('It is necessary for the Monster QA Procedure to have been completed before running this procedure.\\nAre you sure you want to do this now?');">Compare MID-SRC Snapshots</a></b></td>
</tr>

<!-- MID-MID Comparison -->
<tr NOSAVE>
<td colspan="2" NOSAVE><b><a href="$cgi?state=MID_MID_DIFF&db=$db" onMouseOver='window.status="MID_MID_DIFF"; return true;' onMouseOut='window.status=""; return true;'>Compare MID-MID Snapshots</a></b></td>
</tr>

<!-- SRC-INV Comparison : same source list as SRC-SRC obsolete-->
<tr>
<form name="thisform" action="$cgi">
<input type="hidden" name="db" value="$db">
<input type="hidden" name="state" value="SRC_INV_DIFF">
<td colspan="2"><b>Compare SRC-INV Snapshots&nbsp;&nbsp;$srcInvSelect</b></td>
</form>
</tr>

<!-- SRC-SRC Obsolete Comparison -->
<tr>
<form name="thisform" action="$cgi">
<input type="hidden" name="db" value="$db">
<input type="hidden" name="state" value="SRC_OBSOLETE_DIFF">
<td colspan="2"><b>Compare SRC-SRC Obsolete Snapshots&nbsp;&nbsp;$srcSrcObsoleteCompareSelect</b></td>
</form>
</tr>

<!-- INV-INV Obsolete comparison -->
<tr>
<form name="thisform" action="$cgi">
 <input type="hidden" name="db" value="$db">
 <input type="hidden" name="state" value="INV_OBSOLETE_DIFF">
<td colspan="2"><b>Compare INV-INV Obsolete Snapshots&nbsp;&nbsp;$invCompareSelect</b></td>
</form>
</tr>

<tr><td colspan="2">&nbsp;</td></tr>
<tr><th colspan="2">Reports, Review Adjustments, and Counts</th></tr>

<!-- SRC-SRC Obsolete Comparison Report-->
<!-- MID-MID Comparison Report -->
<tr>
<td colspan="2"><b><a href="$cgi?state=MID_SRC_ADJ_REPORT&db=$db" onMouseOver='window.status="Compare current snapshot of MID to SRC"; return true;' onMouseOut='window.status=""; return true;'>MID-SRC QA Report</a></b></td>
</tr>
<tr>
<td colspan="2"><b><a href="$cgi?state=MID_MID_ADJ_REPORT&db=$db" onMouseOver='window.status="Compare current snapshot of MID to the earliest state of this release"; return true;' onMouseOut='window.status=""; return true;'>MID-MID QA Report</a></b></td>
</tr>
<tr>
<form name="thisform" action="$cgi">
 <input type="hidden" name="db" value="$db">
 <input type="hidden" name="state" value="SRC_OBSOLETE_ADJ_REPORT">
<td colspan="2"><b>SRC-SRC Obsolete QA Report&nbsp;&nbsp;$srcSrcObsoleteReportSelect</b></td>
</form>
</tr>

<!-- Break, different kinds of tools -->
<tr><td>&nbsp</td></tr>

<!-- Obsolete UI Report -->
<tr>
<form name="thisform" action="$cgi">
 <input type="hidden" name="db" value="$db">
 <input type="hidden" name="state" value="OBSOLETE_UI_REPORT">
<td colspan="2"><b>Obsolete UI Report&nbsp;&nbsp;$obsoleteUiReportSelect</b></td>
</form>
</tr>

<!-- Samples Report -->
<tr>
<form name="thisform" action="$cgi">
 <input type="hidden" name="db" value="$db">
 <input type="hidden" name="state" value="SAMPLE_DATA">
<td colspan="2"><b>SRC QA Samples&nbsp;&nbsp;$srcSamplesSelect</b></td>
</form>
</tr>


<!-- Raw QA Count Reports-->
<tr>
  <form action="$cgi" method="GET"> 
    <input type="hidden" name="state" value="REVIEW_MID_COUNTS">
    <input type="hidden" name="db" value="$db">
<td colspan="2"><b>Review Raw MID Counts&nbsp;&nbsp;$reviewMidCountSelect</b></td>   </form>
</tr>
<tr>
  <form action="$cgi" method="GET"> 
    <input type="hidden" name="state" value="REVIEW_SRC_COUNTS">
    <input type="hidden" name="db" value="$db">
<td colspan="2"><b>Review Raw SRC Counts&nbsp;&nbsp;$reviewSrcCountSelect</b></td>   </form>
</tr>
<tr>
  <form action="$cgi" method="GET"> 
    <input type="hidden" name="state" value="REVIEW_INV_COUNTS">
    <input type="hidden" name="db" value="$db">
<td colspan="2"><b>Review Raw INV Counts&nbsp;&nbsp;$reviewInvCountSelect</b></td>   </form>
</tr>

<!-- Edit Queries and adjustments -->
<tr><td colspan="2">&nbsp;</td></tr>
<tr><th colspan="2">Edit Tools</th></tr>

<tr>
<td colspan="2"><b><a href="$cgi?state=QUERIES&command=SRC&db=$db" onMouseOver='window.status="SRC Queries"; return true;' onMouseOut='window.status=""; return true;'>SRC Queries</a></b></td>
</tr>

<tr>
<td colspan="2"><b><a href="$cgi?state=QUERIES&command=MID&db=$db" onMouseOver='window.status="MID Queries"; return true;' onMouseOut='window.status=""; return true;'>MID Queries</a></b></td>
</tr>

<!--
<tr>
<td colspan="2"><b><a href="$cgi?state=MRD_COPY&db=$db" onMouseOver='window.status="Copy Counts to MRD"; return true;' onMouseOut='window.status=""; return true;'>Copy Counts to MRD</a></b></td>
</tr>
-->
</table></center>

<p>
    };
};    # end PrintINDEX

#
# Gets the database select form for the index page
#
sub getDbSelectForm {
 my $dbSelect = qq{
                         <form action="$cgi">
			               <input type="hidden" name="state" value="INDEX">
			               <select name="db" onChange="this.form.submit(); return true;">
			                  <option>-- SELECT A DATABASE --</option>
};
 foreach $db ( sort split /,/, $dbs ) {
  $dbSelect .= "			                  <option $selected{$db}>$db</option>\n";
 }
 $dbSelect .= "			                </select></form>\n";
 return $dbSelect;
}

#
# Get <select> options for the "SRC-INV comparison" tool
#
sub getSrcInvSelect {
 
 #
 # Find "new" sources and "updates" to
 # sources previously released
 #
 my $query = qq{
    SELECT current_name
  FROM released_source_version WHERE current_name IS NOT NULL
  AND current_name in (select distinct substr(value,1,instr(value,',')-1) from src_qa_results)
  AND current_name in (
  (SELECT source FROM sims_info
    WHERE insert_meta_version IS NULL
    AND source IN
    (SELECT current_name FROM source_version 
      WHERE previous_name IS NULL)
   UNION
  SELECT a.source FROM sims_info a, source_version b, sims_info c
    WHERE a.insert_meta_version IS NULL
    AND a.source = b.current_name AND b.previous_name = c.source
    AND c.remove_meta_version IS NULL))
  ORDER BY 1
   };

 #
 # This is only the updated source list
 #
 my $options = qq{<select name="source" onChange="this.form.submit();">
  <option value="">-- Select a Source --</option>
  };
 $sh = $dbh->prepare(qq{$query })
   || ( ( print "<span id=red>Error preparing for SRC-SRC Obsolete Comparison.</span>" )
        && return );
 $sh->execute
   || (
    ( print "<span id=red>Error executing for SRC-SRC Obsolete Comparison.</span>" )
    && return );
 while ( ( $cur_name ) = $sh->fetchrow_array ) {
  $options .=
qq{  <option value="$cur_name">$cur_name</option>\n};
 }
 $options .= qq{</select>};
 return $options;
}

#
# Get <select> options for the "SRC-SRC Obsolete Editor" tool
#
sub getSrcSrcObsoleteCompareSelect {

 #
 # Find "new" sources and "updates" to
 # sources previously released
 #
 my $query = qq{
    SELECT current_name, previous_name
	FROM released_source_version WHERE current_name IS NOT NULL
	AND current_name in (select distinct substr(value,1,instr(value,',')-1) from src_qa_results)
	AND current_name in (
	(SELECT source FROM sims_info
		WHERE insert_meta_version IS NULL
		AND source IN
		(SELECT current_name FROM source_version 
			WHERE previous_name IS NULL)
	 UNION
	SELECT a.source FROM sims_info a, source_version b, sims_info c
		WHERE a.insert_meta_version IS NULL
		AND a.source = b.current_name AND b.previous_name = c.source
		AND c.remove_meta_version IS NULL))
	ORDER BY 2
   };

 #
 # Look up previous sources for select list
 # This is only the updated source list
 #
 my $options = qq{<select name="source" onChange="this.form.submit();">
  <option value="">-- Select a Source --</option>
  <option value="all_sources,all_sources">All Sources</option>
  <option value="current_sources,current_sources">All Current Sources</option>
	};
 $sh = $dbh->prepare(qq{$query })
   || ( ( print "<span id=red>Error preparing for SRC-SRC Obsolete Comparison.</span>" )
        && return );
 $sh->execute
   || (
    ( print "<span id=red>Error executing for SRC-SRC Obsolete Comparison.</span>" )
    && return );
 while ( ( $cur_name, $prev_name ) = $sh->fetchrow_array ) {
  $options .=
qq{  <option value="$cur_name,$prev_name">$cur_name - $prev_name</option>\n};
 }
 $options .= qq{</select>};
 return $options;
}

#
# Get <select> options for the "SRC-SRC Obsolete Report" tool
#
sub getSrcSrcObsoleteReportSelect {

 #
 # Get most recent current/previous SABs
 #
 my $query = qq{
    SELECT distinct current_name,previous_name
	FROM released_source_version
	WHERE (previous_name is not null or current_name is not null)
	ORDER BY 2
   };

 #
 # Look up all sources for select list
 #
 my $srcSrcObsoleteReportSelect =
   qq{<select name="source" onChange="this.form.submit();">
	    <option value="">-- Select a Source --</option>
};
 $sh = $dbh->prepare($query)
   || ( ( print "<span id=red>Error preparing for SRC-SRC Report.</span>" )
        && return );
 $sh->execute
   || (
    ( print "<span id=red>Error executing for SRC-SRC Report.</span>" )
    && return );
 while ( ( $cur_name, $prev_name ) = $sh->fetchrow_array ) {
  $srcSrcObsoleteReportSelect .=
qq{  <option value="$cur_name,$prev_name">$cur_name - $prev_name</option>\n};
 }
 $srcSrcObsoleteReportSelect .= qq{</select>};
 return $srcSrcObsoleteReportSelect;
}

#
# Get <select> options for the "obsolete UI report" tool
#
sub getObsoleteUiReportSelect {

 #
 # Look up root source for the obsolete_ui report
 #
 my $obsoleteUiReportSelect =
   qq{<select name="source" onChange="this.form.submit();">
	    <option value="">-- Select a Source --</option>
};
 $sh = $dbh->prepare(
  qq{
    SELECT distinct root_source
    FROM obsolete_ui
    ORDER BY 1
   }
   )
   || (
       ( print "<span id=red>Error looking up obsolete_ui root_source.</span>" )
       && return );
 $sh->execute
   || (
  (
   print
"<span id=red>Error executing query to look up obsolete_ui root_source.</span>"
  )
  && return
   );
 while ( ($root_source) = $sh->fetchrow_array ) {
  $obsoleteUiReportSelect .=
    qq{  <option value="$root_source">$root_source</option>\n};
 }
 $obsoleteUiReportSelect .= qq{</select>};
 return $obsoleteUiReportSelect;
}

#
# Get <select> options for the "Source Samples Report" tool
#
sub getSrcSamplesSelect {

 #
 # Look up sources for the qa samples report
 #
 my $srcSamplesSelect = qq{<select name="source" onChange="this.form.submit();">
            <option value="">-- Select a Source --</option>
};
 $sh = $dbh->prepare(
  qq{
    SELECT distinct current_name, previous_name, source
    FROM released_source_version a, src_qa_samples b
    WHERE substr(b.value,1,instr(b.value||',',',')-1) = a.current_name
    ORDER BY 1
   }
   )
   || (
  (
   print
"<span id=red>Error looking up source from release_source_version and src_qa_samples.</span>"
  )
  && return
   );
 $sh->execute
   || (
  (
   print
"<span id=red>Error executing query to look up source from release_source_version and src_qa_samples.</span>"
  )
  && return
   );
 while ( ( $current_name, $previous_name, $source ) = $sh->fetchrow_array ) {
  $srcSamplesSelect .=
qq{  <option value="$current_name,$previous_name,$source">$current_name</option>\n};
 }
 $srcSamplesSelect .= qq{</select>};
 return $srcSamplesSelect;
}

#
# Get <select> options for the "Inversion QA Report" tool
#
sub getInvCompareSelect {

 #
 # Look up sources for the inv statistics report
 #
 my $invCompareSelect = qq{<select name="source" onChange="this.form.submit();">
            <option value="">-- Select a Source --</option>
};
 $sh = $dbh->prepare(
  qq{
    SELECT distinct current_name, previous_name
    FROM released_source_version a, inv_qa_results b
    WHERE SUBSTR(b.value,1,INSTR(value||',',',')-1) = a.current_name
    ORDER BY 1
   }
   )
   || (
  (
   print
"<span id=red>Error looking up source from release_source_version and inv_qa_results.</span>"
  )
  && return
   );
 $sh->execute
   || (
  (
   print
"<span id=red>Error executing query to look up source from release_source_version and inv_qa_results.</span>"
  )
  && return
   );
 while ( ( $current_name, $previous_name ) = $sh->fetchrow_array ) {
  $invCompareSelect .=
qq{  <option value="$current_name,$previous_name">$current_name - $previous_name</option>\n};
 }
 $invCompareSelect .= qq{</select>};
 return $invCompareSelect;
}

#
# Return "Review mid count" select list
#
sub getReviewMidCountSelect {
 $sh = $dbh->prepare(
  qq{
    SELECT qa_id, timestamp FROM
   	  (SELECT qa_id, timestamp FROM mid_qa_history
	   UNION SELECT qa_id, timestamp FROM mid_qa_results)
    ORDER BY 2 desc
	}
   )
   || ( ( print "<span id=red>Error reading from mid_qa_history.</span>" )
        && return );
 $sh->execute
   || ( ( print "<span id=red>Error reading from mid_qa_history table.</span>" )
        && return );
 $select = qq{
    <select name="qa_id" onChange="this.form.submit(); return true;">
       <option>-- Select a Date --</option>
};
 while ( ( $qa_id, $timestamp ) = $sh->fetchrow_array ) {
  $select .= qq{        <option value="$qa_id">$timestamp</option>\n};
 }    #end while
 $select .= qq{
    </select>
	};
 return $select;
}    # end getReviewMidCountSelect

#
# Return "Review src count" select list
#
sub getReviewSrcCountSelect {
 $sh = $dbh->prepare(
  qq{
    SELECT distinct SUBSTR(value,1,INSTR(value||',',',')-1) source
    FROM src_qa_results
    ORDER BY 1
	}
   )
   || ( ( print "<span id=red>Error reading from src_qa_results.</span>" )
        && return );
 $sh->execute
   || ( ( print "<span id=red>Error reading from src_qa_results table.</span>" )
        && return );
 $select = qq{
    <select name="source" onChange="this.form.submit(); return true;">
       <option>-- Select a Source --</option>
};
 while ( ($source) = $sh->fetchrow_array ) {
  $select .= qq{        <option value="$source">$source</option>\n};
 }    #end while
 $select .= qq{
    </select>
	};
 return $select;
}    # end getReviewSrcCountSelect

#
# Return "Review inv count" select list
#
sub getReviewInvCountSelect {
 $sh = $dbh->prepare(
  qq{
    SELECT * FROM (
    SELECT distinct SUBSTR(value,1,INSTR(value||',',',')-1) source
    FROM inv_qa_results ) WHERE source IN (SELECT current_name FROM released_source_Version)
    ORDER BY 1
	}
   )
   || ( ( print "<span id=red>Error reading from src_qa_results.</span>" )
        && return );
 $sh->execute
   || ( ( print "<span id=red>Error reading from src_qa_results table.</span>" )
        && return );
 $select = qq{
    <select name="source" onChange="this.form.submit(); return true;">
       <option>-- Select a Source --</option>
};
 while ( ($source) = $sh->fetchrow_array ) {
  $select .= qq{        <option value="$source">$source</option>\n};
 }    #end while
 $select .= qq{
    </select>
	};
 return $select;
}    # end getReviewInvCountSelect

###################### Procedure PrintINV_OBSOLETE_DIFF ######################
#
# This procedure prints the entry point to data sample from the SRC insertion process
#
sub PrintINV_OBSOLETE_DIFF {
 my ( $current_name, $previous_name ) = split /,/, $source;

 #
 # Display in terms of "current" names
 # Things without previous versions will be labeled with -NEW
 #
 $sh = $dbh->prepare(
  qq{
	SELECT name, value, count_1, count_2, count_2-count_1, type,
	       replace(value,nvl(previous_name,current_name||'-NEW'),current_name) display_value
  	FROM inv_obsolete_diff_results a, released_source_version b
    WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = nvl(previous_name,current_name||'-NEW')
      AND b.current_name = '$current_name'
	  AND current_name IS NOT NULL
        ORDER BY name, value
	}
   )
   || ( ( print "<span id=red>Error reading from inv_qa_results.</span>" )
        && return );
 $sh->execute
   || (
    ( print "<span id=red>Executing Error reading from inv_qa_results.</span>" )
    && return );
 print qq{
<i>Following is the inversion statistics report between $current_name and $previous_name.</i><p>
<center><table border=1 width="90%">
};
 $prev  = "";
 $found = 0;

 while ( ( $name, $value, $prevct, $curct, $diff, $type, $display_value ) =
         $sh->fetchrow_array )
 {
  next unless $diff;
  $found = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="3" align=left>$name ($current_name,$previous_name)</th></tr>
  };
  }
  $prev = $name;
  print qq{
  <tr><td>
      $display_value ($curct, $prevct)
  </td>
  <td><font size="-2">$type</font></td>
  <td>$diff</td>
  </tr>
};
 }
 unless ($found) {
  print qq{No data found.};
 }
 print qq{
</table></center>
<p>
};
};    # end PrintINV_OBSOLETE_DIFF
###################### Procedure PrintSAMPLE_DATA ######################
#
# This procedure prints the entry point to data sample from the SRC insertion process
#
sub PrintSAMPLE_DATA {
 my ( $current_name, $previous_name, $source ) = split /,/, $source;
 $sh = $dbh->prepare(
  qq{
    WITH src_sample_ct AS (select name,value,count(*) cnt from src_qa_samples group by name,value)
	SELECT a.name, a.value, a.cnt, b.qa_count,
	   a.value display_value,  'CUR' type
	FROM src_sample_ct a, src_qa_results b, released_source_version c
	WHERE a.name = b.name
   	  AND a.value = b.value
   	  AND SUBSTR(a.value,1,INSTR(a.value||',',',')-1) = current_name
   	  AND current_name = '$current_name'
    UNION ALL
	SELECT a.name, a.value, a.cnt, b.qa_count,
	   REPLACE(a.value,c.previous_name,c.current_name) display_value, 'PREV'
	FROM src_sample_ct a, src_obsolete_qa_results b, released_source_version c
	WHERE a.name = b.name
   	  AND a.value = b.value
   	  AND SUBSTR(a.value,1,INSTR(a.value||',',',')-1) = previous_name
   	  AND current_name = '$current_name'
  }
   )
   || (
       ( print "<span id=red>Error reading counts from src_qa_samples.</span>" )
       && return );
 $sh->execute
   || (
     ( print "<span id=red>Error executing counts from src_qa_samples.</span>" )
     && return );
 my %cur_counts    = ();
 my %prev_counts   = ();
 my %cur_values    = ();
 my %prev_values   = ();
 my @sample_counts = ();
 while ( ( $name, $value, $sample_cnt, $total, $display_value, $type ) =
         $sh->fetchrow_array )
 {
  unshift @sample_counts, "$name|$display_value";
  if ( $type eq "CUR" ) {
   $cur_counts{"$name|$display_value"} = "$sample_cnt/$total";
   $cur_values{"$name|$display_value"} = $value;
  } elsif ( $type eq "PREV" ) {
   $prev_counts{"$name|$display_value"} = "$sample_cnt/$total";
   $prev_values{"$name|$display_value"} = $value;
  }
 }
 print qq{
<center>QA Samples from $db for $source.</p>
<i>Current Version: $current_name</i></p>
<i>Previous Version: $previous_name</i></p></center>
<center><table border=1 width="90%">
};
 $prev  = "";
 $found = 0;
 foreach $key ( sort @sample_counts ) {
  ( $name, $value ) = split /\|/, $key;
  $current_ct  = $cur_counts{$key};
  $previous_ct = $prev_counts{$key};
  $current_value  = $cur_values{$key};
  $previous_value = $prev_values{$key};
  $found          = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="6" align=center>$name</th></tr>
  <tr><th>Value</th><th>Current</th><th>Previous</th></tr>
  };
  }
  $prev = $name;
  print qq{
  <tr> 
  <td>$value</td> };
  if ($current_value) {
   print
qq{<td align=center><b><a href="$cgi?state=PRINT_SAMPLES&qa_name=$name&qa_value=$current_value&db=$db">$current_ct</a></b></td>};
  } else {
   print qq{<td>&nbsp</td>};
  }
  if ($previous_value) {
   print
qq{<td align=center><b><a href="$cgi?state=PRINT_SAMPLES&qa_name=$name&qa_value=$previous_value&db=$db">$previous_ct</a></b></td></tr>};
  } else {
   print qq{<td>&nbsp</td></tr>};
  }
 }
 unless ($found) {
  print qq{QA Samples not found for $current_name.};
 }
 print qq{</table></center><p>};
}
###################### Procedure PrintSAMPLES ######################
#
# This procedure prints the sample data from src_qa_samples
#
sub PrintSAMPLES {
 my $num = 1;
 if ( $qa_name eq "sab_lat_tty_tally" ) {

  #atoms
  $sh = $dbh->prepare(
   qq{
	SELECT b.atom_name, a.code, NVL(a.source_cui,'&nbsp'), NVL(a.source_dui,'&nbsp'), NVL(a.source_aui,'&nbsp'), a.concept_id
	FROM classes a, atoms b, src_qa_samples c
	WHERE a.atom_id = b.atom_id
	AND c.sample_id = a.atom_id
	AND c.value = '$qa_value'
	AND c.name = '$qa_name'
	order by 1
    }
    )
    || ( ( print "<span id=red>Error preparing query for $qa_name.</span>" )
         && return );
  $sh->execute
    || ( ( print "<span id=red>Error executing query for $qa_name.</span>" )
         && return );
  print qq{
    <center><table border=1 width="90%">
      <tr><th colspan="7" align=center>Name: $qa_name</th></tr>
  <tr><th colspan="7" align=center>Value: $qa_value</th></tr>
  <tr><th>#</th><th>Atom Name</th><th>Code</th><th>SCUI</th><th>SDUI</th><TH>SAUI</TH><th>Concept_ID</th></tr>
  };
  while ( ( $str, $code, $scui, $sdui, $saui, $concept_id ) =
          $sh->fetchrow_array )
  {
   print qq{<TR>
	<TD>$num</TD>
	<TD>$str</TD>
	<TD>$code</TD>
	<TD>$scui</TD>
	<TD>$sdui</TD>
	<TD>$saui</TD>
	<TD align=center><b><a href="concept-report-mid.pl?db=$db&service=$db&action=searchbyconceptid&arg=$concept_id#report" target="_blank">$concept_id</a></b></TD>
	</TR>};
   $num++;
  }
  print qq{</table></center><p>};
 } elsif ( $qa_name eq "sab_atn_stype_tally" ) {

  #attribute
  $sh = $dbh->prepare(
   qq{
	SELECT a.attribute_value, b.atom_name, d.termgroup, NVL(d.code,'&nbsp'), d.concept_id 
	FROM attributes a, atoms b, src_qa_samples c, classes d
	WHERE a.atom_id = b.atom_id
	AND c.sample_id = a.attribute_id
	AND d.atom_id = a.atom_id
	AND c.value = '$qa_value'
	AND c.name = '$qa_name'
	AND a.attribute_value not like '<>Long_Attribute<>%'
	UNION ALL
	SELECT e.text_value, b.atom_name, d.termgroup, NVL(d.code,'&nbsp'), d.concept_id 
	FROM attributes a, atoms b, src_qa_samples c, classes d, stringtab e
	WHERE a.atom_id = b.atom_id
	AND c.sample_id = a.attribute_id
	AND d.atom_id = a.atom_id
	AND c.value = '$qa_value'
	AND c.name = '$qa_name'
	AND a.attribute_value like '<>Long_Attribute<>%'
	AND e.string_id = to_number(substr(attribute_value,20)) 
	AND e.row_sequence = 1
	order by 1,2
    }
    )
    || ( ( print "<span id=red>Error preparing query for $qa_name.</span>" )
         && return );
  $sh->execute
    || ( ( print "<span id=red>Error executing query for $qa_name.</span>" )
         && return );
  print qq{
    <center><table border=1 width="90%">
   <tr><th colspan="6" align=center>Name: $qa_name</th></tr>
  <tr><th colspan="6" align=center>Value: $qa_value</th></tr>
  <tr><th>#</th><th>Attribute Value</th><th>Associated Atom Name</th><th>Termgroup</th><th>Code</th><th>Concept_ID</th></tr>
  };
  while ( ( $str, $atom_name, $termgroup, $code, $concept_id ) =
          $sh->fetchrow_array )
  {
   print qq{
	<TR>
	<TD>$num</TD>
	<TD>$str</TD>
	<TD>$atom_name</TD>
	<TD>$termgroup</TD>
	<TD>$code</TD>
	<TD align=center><b><a href="concept-report-mid.pl?db=$db&service=$db&action=searchbyconceptid&arg=$concept_id#report" target="_blank">$concept_id</a></b></TD>
	</TR>};
   $num++;
  }
  print qq{</table></center><p>};
 } elsif ( $qa_name eq "sab_def_tally" ) {

  #definition
  $sh = $dbh->prepare(
   qq{
	SELECT a.attribute_value,b.atom_name, d.termgroup, NVL(d.code,'&nbsp'), d.concept_id 
	FROM attributes a, atoms b, src_qa_samples c, classes d
	WHERE a.atom_id = b.atom_id
	AND c.sample_id = a.attribute_id
	AND d.atom_id = a.atom_id
	AND c.value = '$qa_value'
	AND c.name = '$qa_name'
	order by 1
    }
    )
    || ( ( print "<span id=red>Error preparing query for $qa_name.</span>" )
         && return );
  $sh->execute
    || ( ( print "<span id=red>Error executing query for $qa_name.</span>" )
         && return );
  print qq{
    <center><table border=1 width="90%">
   <tr><th colspan="6" align=center>Name: $qa_name</th></tr>
  <tr><th colspan="6" align=center>Value: $qa_value</th></tr>
  <tr><th>#</th><th>Attribute Value</th><th>Associated Atom Name</th><th>Termgroup</th><th>Code</th><th>Concept_ID</th></tr>
  };
  while ( ( $str, $atom_name, $termgroup, $code, $concept_id ) =
          $sh->fetchrow_array )
  {
   print qq{
	<TR>
	<TD>$num</TD>
	<TD>$str</TD>
	<TD>$atom_name</TD>
	<TD>$termgroup</TD>
	<TD>$code</TD>
	<TD align=center><b><a href="concept-report-mid.pl?db=$db&service=$db&action=searchbyconceptid&arg=$concept_id#report" target="_blank">$concept_id</a></b></TD>
	</TR>};
   $num++;
  }
  print qq{</table></center><p>};
 } elsif ( $qa_name eq "sab_rela_par_tally" ) {

  #context
  $sh = $dbh->prepare(
   qq{
	SELECT b.atom_name, d.termgroup, NVL(d.code,'&nbsp'), d.concept_id, a.parent_treenum, 
    e.atom_name, f.termgroup, NVL(f.code,'&nbsp'), f.concept_id
	FROM context_relationships a, atoms b, src_qa_samples c, classes d, atoms e, classes f
	WHERE a.atom_id_1 = b.atom_id
	AND c.sample_id = a.relationship_id
	AND d.atom_id = b.atom_id
	AND e.atom_id = f.atom_id
	AND a.atom_id_2 = e.atom_id
	AND c.value = '$qa_value'
	AND c.name = '$qa_name'
	order by 5
    }
    )
    || ( ( print "<span id=red>Error preparing query for $qa_name.</span>" )
         && return );
  $sh->execute
    || ( ( print "<span id=red>Error executing query for $qa_name.</span>" )
         && return );
  print qq{
    <center><table border=1 width="90%">
   <tr><th colspan="7" align=center>Name: $qa_name</th></tr>
  <tr><th colspan="7" align=center>Value: $qa_value</th></tr>
  <tr><th width="1%">#</th><th width="45%">Atom 1: Atom Name | Termgroup | Code | Concept_ID</th>
  <th width="9%">Parent Treenum</th>
  <th width="45%">Atom 2: Atom Name | Termgroup | Code | Concept_ID</th></tr>
  };
  while (
          (
            $atom_name1,  $termgroup1, $code1, $concept_id1, $ptr,
            $atom_name2, $termgroup2, $code2, $concept_id2
          )
          = $sh->fetchrow_array
    )
  {
   print qq{
	<TR>
	<TD>$num</TD>
	<TD>$atom_name1 | $termgroup1 | $code1 | <b><a href="concept-report-mid.pl?db=$db&service=$db&action=searchbyconceptid&arg=$concept_id1#report" target="_blank">$concept_id1</a></b></TD>
	<TD>$ptr</TD>
	<TD>$atom_name2 | $termgroup2 | $code2 | <b><a href="concept-report-mid.pl?db=$db&service=$db&action=searchbyconceptid&arg=$concept_id2#report" target="_blank">$concept_id2</a></b></TD>
	</TR>};
   $num++;
  }
  print qq{</table></center><p>};
 } elsif (    $qa_name eq "sab_xmapfrom_tally"
           or $qa_name eq "sab_xmapto_tally"
           or $qa_name eq "sab_xmap_tally" )
 {

  #map object
  $sh = $dbh->prepare(
   qq{
	SELECT attribute_name, attribute_value, sg_type
	FROM attributes a, src_qa_samples b
	WHERE b.sample_id = a.attribute_id
	AND b.value = '$qa_value'
	AND b.name = '$qa_name'
	order by 2,1
    }
    )
    || ( ( print "<span id=red>Error preparing query for $qa_name.</span>" )
         && return );
  $sh->execute
    || ( ( print "<span id=red>Error executing query for $qa_name.</span>" )
         && return );
  print qq{
    <center><table border=1 width="90%">
   <tr><th colspan="5" align=center>Name: $qa_name</th></tr>
  <tr><th colspan="5" align=center>Value: $qa_value</th></tr>
  <tr><th>#</th><th>Attribute Name</th><th>Attribute Value</th><th>SG_TYPE</th></tr>
  };
  while ( ( $atn, $atv, $stype ) = $sh->fetchrow_array ) {
   print qq{
	<TR>
	<TD>$num</TD>
	<TD>$atn</TD>
	<TD>$atv</TD>
	<TD>$stype</TD>
	</TR>};
   $num++;
  }
  print qq{</table></center><p>};
 } elsif ( $qa_name eq "sab_hist_tally" ) {

  #source history
  $sh = $dbh->prepare(
   qq{
	SELECT attribute_value
	FROM attributes a, src_qa_samples b
	WHERE b.sample_id = a.attribute_id
	AND b.value = '$qa_value'
	AND b.name = '$qa_name'
	order by 1
    }
    )
    || ( ( print "<span id=red>Error preparing query for $qa_name.</span>" )
         && return );
  $sh->execute
    || ( ( print "<span id=red>Error executing query for $qa_name.</span>" )
         && return );
  print qq{
    <center><table border=1 width="90%">
   <tr><th colspan="5" align=center>Name: $qa_name</th></tr>
  <tr><th colspan="5" align=center>Value: $qa_value</th></tr>
  <tr><th>#</th><th>Attribute Value</th></tr>
  };
  while ( ($atv) = $sh->fetchrow_array ) {
   print qq{
	<TR><TD>$num</TD>
	<TD>$atv</TD>
	</TR>};
   $num++;
  }
  print qq{</table></center><p>};
 } elsif ( $qa_name eq "sab_rel_rela_stype1_stype2_tally" ) {

  #relationship
  $sh = $dbh->prepare(
   qq{
	SELECT b.atom_name, d.termgroup, NVL(d.code,'&nbsp'), d.concept_id,
	NVL(a.relationship_attribute,'&nbsp'), e.atom_name, f.termgroup, NVL(f.code,'&nbsp'), f.concept_id
	FROM relationships a, atoms b, src_qa_samples c, classes d, atoms e, classes f
	WHERE a.atom_id_1 = b.atom_id
	AND c.sample_id = a.relationship_id
	AND d.atom_id = b.atom_id
	AND e.atom_id = f.atom_id
	AND a.atom_id_2 = e.atom_id
	AND c.value = '$qa_value'
	AND c.name = '$qa_name'
	UNION
	SELECT b.atom_name, d.termgroup, NVL(d.code,'&nbsp'), d.concept_id,
	NVL(a.relationship_attribute,'&nbsp'), e.atom_name, f.termgroup, NVL(f.code,'&nbsp'), f.concept_id
	FROM context_relationships a, atoms b, src_qa_samples c, classes d, atoms e, classes f
	WHERE a.atom_id_1 = b.atom_id
	AND c.sample_id = a.relationship_id
	AND d.atom_id = b.atom_id
	AND e.atom_id = f.atom_id
	AND a.atom_id_2 = e.atom_id
	AND c.value = '$qa_value'
	AND c.name = '$qa_name'
	order by 5
    }
    )
    || ( ( print "<span id=red>Error preparing query for $qa_name.</span>" )
         && return );
  $sh->execute
    || ( ( print "<span id=red>Error executing query for $qa_name.</span>" )
         && return );
  print qq{
    <center><table border=1 width="90%">
   <tr><th colspan="6" align=center>Name: $qa_name</th></tr>
  <tr><th colspan="6" align=center>Value: $qa_value</th></tr>
  <tr><th width="1%">#</th><th width="45%">Atom 1: Atom Name | Termgroup | Code | Concept_ID</th>
  <th width="9%">RELA</th>
  <th width="45%">Atom 2: Atom Name | Termgroup | Code | Concept_ID</th></tr>
  };
  while (
          (
            $atom_name1, $termgroup1, $code1, $concept_id1, $rela,
            $atom_name2, $termgroup2, $code2, $concept_id2
          )
          = $sh->fetchrow_array
    )
  {
   print qq{
	<TR>
	<TD>$num</TD>
	<TD>$atom_name1 | $termgroup1 | $code1 | <b><a href="concept-report-mid.pl?db=$db&service=$db&action=searchbyconceptid&arg=$concept_id1#report" target="_blank">$concept_id1</a></b></TD>
	<TD>$rela</TD>
	<TD>$atom_name2 | $termgroup2 | $code2 | <b><a href="concept-report-mid.pl?db=$db&service=$db&action=searchbyconceptid&arg=$concept_id2#report" target="_blank">$concept_id2</a></b></TD>
	</TR>};
   $num++;
  }
  print qq{</table></center><p>};
 }

 if ($num == 1) {
  print qq{<center>No samples found </center>};
 }

}
###################### Procedure PrintMID_SRC_DIFF ######################
#
# This procedure prints a page that shows the difference between
# SRC and MID counts
#
sub PrintMID_SRC_DIFF {

 #
 # No transformation necessary, value expressed in correct terms
 #
 $sh = $dbh->prepare(
  qq{
  SELECT 
    name, value, qa_id_1, 
    count_1 src_ct, 
    count_2 mid_ct, count_2-count_1, type
    FROM mid_src_diff_results order by name,value
    }
   )
   || ( ( print "<span id=red>Error reading from mid_src_diff_results.</span>" )
        && return );
 $sh->execute
   || ( ( print "<span id=red>Error reading from mid_src_diff_results.</span>" )
        && return );
 print qq{
<p><i>Fill out adjustment reasons and edit the adjustment count if necessary. Then click "Submit Adjustments" at the bottom of the page.<br>
[Note: The counts in parenthesis are (MID,SRC) counts]
</i></p>
Some helpful advice for researching these problems:
<ul>
<li>Re-count the data in the insertion files</li>
<li>Review counts in src_qa_results and qa_adjustments for this value to understand how the final count was arrived at</li>
<li>Find the missing or extra data in the MID (it may be in dead tables)</li>
<li>Discrepancies may also be due to duplicate data (with the same META UIs)</li>
<li>Due to the "source replacement" algorithm, if an attribute in the previous version had 2 entries with the same ATUI, but in the current version has only one entry with that ATUI, it will show up as an additional MID count even though it is not.  This type of thing can also happen with atoms or relationships.</li>
</ul>
<center><table border=0 width="90%">
<form method="POST" action="$cgi">
    <input type="hidden" name="state" value="MID_SRC_DIFF_DONE">
    <input type="hidden" name="db" value="$db">
};
 $prev  = "";
 $found = 0;

 while ( ( $name, $value, $id, $srcct, $midct, $diff, $type ) =
         $sh->fetchrow_array )
 {
  $qa_id         = "$id";
  $found         = 1;
  $display_value = "$value";
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="4" align=left>$name</th></tr>
  };
  }
  $prev = $name;
  print qq{
  <tr><td>
      <input type="hidden" name="qa_name" value="$name">
      <input type="hidden" name="qa_value" value="$value">
      $value ($midct, $srcct)
  </td>
  <td><font size="-2">$type</font></td>
  <td>
      <input type="text" name="adjustment" value="$diff" size=10>
  </td>
  <td>
      <input type="text" name="adjustment_dsc" size=50>
  </td></tr>
};
 }
 if ($found) {
  print qq{
<tr><td colspan="4">&nbsp</td></tr>
<tr><td colspan="4">
    <input type="hidden" name="qa_id" value="$qa_id">
<center><input type="submit" value="Submit Adjustments"></center>
</td></tr>
};
 } else {
  print qq{
      <tr><td>There are no differences between the MID and SRC
          counts.</td></tr>
};
 }
 print qq{
</table></center>
<p>
}
};    # end PrintMID_SRC_DIFF
###################### Procedure PrintMID_SRC_DIFF_DONE ######################
#
# This procedure adds the adjustments to the qa_adjustment table
#
sub PrintMID_SRC_DIFF_DONE {

 # use @qa_name, @qa_value, @qa_id, @adjustment, and @adjustmen_dsc
 # to insert rows into qa_adjustment.  where the adjustment_dsc exists
 $sh = $dbh->prepare(
  qq{
	insert into qa_adjustment (qa_id, name, value, qa_count,
				   timestamp, description)
	    values ( $qa_id, ?, ?, ?, SYSDATE, ? )
	}
   )
   || ( ( print "<span id=red>Error preparing insert stmt.</span>" )
        && return );
 $ct = 0;
 for ( $i = 0 ; $i <= $#qa_name ; $i++ ) {

  #	print "$qa_name[$i]: $adjustment_dsc[$i], $adjustment[$i]<br>\n";
  if ( $adjustment_dsc[$i] && $adjustment[$i] ) {
   $sh->execute( $qa_name[$i],    $qa_value[$i],
                 $adjustment[$i], $adjustment_dsc[$i] )
     || (
    (
     print
"<span id=red>Error adding MID-SRC adjustment  ($qa_name[$i], $qa_value[$i],$adjustment[$i], $adjustment_dsc[$i], $DBI::errstr).</span>"
    )
    && return
     );
   $ct++;
  }
 }
 print qq{
	$ct adjustments were added.  Go back to the index page and
	compare MID-SRC counts again to see if any differences remain.
	};
};    # end PrintMID_SRC_DIFF_DONE
###################### Procedure PrintSRC_INV_DIFF ######################
#
# This procedure prints a page that shows the difference between
# SRC and INV counts
#
sub PrintSRC_INV_DIFF {

 #
 # No transformation necessary, value expressed in correct terms
 #
 $sh = $dbh->prepare(
  qq{
  SELECT 
    name, value, qa_id_1, 
    count_1 src_ct, 
    count_2 mid_ct, count_2-count_1, type
    FROM src_inv_diff_results 
    WHERE value like ? ORDER by name,value
    }
   )
   || ( ( print "<span id=red>Error reading from src_inv_diff_results.</span>" )
        && return );
 $sh->execute("$source,%")
   || ( ( print "<span id=red>Error reading from mid_src_diff_results.</span>" )
        && return );
 print qq{
<p><i>INV counts should exactly match SRC counts.
[Note: The counts in parenthesis are (SRC,INV) counts]
</i></p>
<center><table border=0 width="90%">
};
 $prev  = "";
 $found = 0;

 while ( ( $name, $value, $id, $invct, $srcct, $diff, $type ) =
         $sh->fetchrow_array )
 {
  $qa_id         = "$id";
  $found         = 1;
  $display_value = "$value";
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="3" align=left>$name</th></tr>
  };
  }
  $prev = $name;
  print qq{
  <tr><td>
      <input type="hidden" name="qa_name" value="$name">
      <input type="hidden" name="qa_value" value="$value">
      $value ($srcct, $invct)
  </td>
  <td><font size="-2">$type</font></td>
  <td>$diff</td>
</tr>
};
 }
 if ($found) {
  print qq{
<tr><td colspan="4">&nbsp</td></tr>
<tr><td colspan="4">
    <input type="hidden" name="qa_id" value="$qa_id">
<center><input type="submit" value="Submit Adjustments"></center>
</td></tr>
};
 } else {
  print qq{
      <tr><td>There are no differences between the INV and SRC
          counts for $source.</td></tr>
};
 }
 print qq{
</table></center>
<p>
}
};    # end PrintMID_SRC_DIFF
###################### Procedure PrintSRC_OBSOLETE_DIFF ######################
#
# This procedure prints a page that shows the difference between
# SRC and SRC Obsolete counts
#
sub PrintSRC_OBSOLETE_DIFF {
 my ( $current_name, $previous_name ) = split /,/, $source;
 if ( $current_name eq "all_sources" ) {
  $sh = $dbh->prepare(

   #
   # convert value to display as current name
   # Restrict to only updated sources.
   # Things without previous versions will be labeled with -NEW
   #
   qq{
	SELECT name, value, qa_id_1, count_1, count_2, count_2-count_1, type,
	        replace(value,nvl(previous_name,current_name||'-NEW'),current_name) display_value
  	FROM src_obsolete_diff_results a, released_source_version b
    WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = nvl(previous_name,current_name||'-NEW')
  	  AND current_name IS NOT NULL
    ORDER BY name, value
    }
    )
    || ( ( print "<span id=red>Error reading from qa_diff_results.</span>" )
         && return );
 } elsif ( $current_name eq "current_sources" ) {
  $sh = $dbh->prepare(

   #
   # convert value to display as current name
   # Restrict to only updated sources.
   # Things without previous versions will be labeled with -NEW
   #
   qq{
	SELECT name, value, qa_id_1, count_1, count_2, count_2-count_1, type,
	        replace(value,nvl(previous_name,current_name||'-NEW'),current_name) display_value
  	FROM src_obsolete_diff_results a, released_source_version b
    WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = nvl(previous_name,current_name||'-NEW')
	AND current_name IS NOT NULL
	AND b.current_name in (select source from sims_info where INSERT_META_VERSION is null)
        ORDER BY name, value
    }
    )
    || ( ( print "<span id=red>Error reading from qa_diff_results.</span>" )
         && return );
 } else {

  #
  # convert value to display as current name
  # Things without previous versions will be labeled with -NEW
  #
  $sh = $dbh->prepare(
   qq{
	SELECT name, value, qa_id_1, count_1, count_2, count_2-count_1, type,
	       replace(value,nvl(previous_name,current_name||'-NEW'),current_name) display_value
  	FROM src_obsolete_diff_results a, released_source_version b
    WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = nvl(previous_name,current_name||'-NEW')
      AND b.current_name = '$current_name'
    ORDER BY name, value
    }
    )
    || ( ( print "<span id=red>Error reading from qa_diff_results.</span>" )
         && return );
 }
 $sh->execute
   || ( ( print "<span id=red>Error reading from qa_diff_results.</span>" )
        && return );
 print qq{
<p><i>Fill out adjustment reasons and edit the adjustment count if necessary. Then click "Submit Adjustments" at the bottom of the page.<br>
[Note: The counts in parenthesis are (current, obsolete)]
</i></p>
For the adjustment reason, do one of the following:
<ul>
<li>Acknowledge the change with your initials (e.g. BAC)</li>
<li>For cases where current count is 0 and obsolete count is not, 
    provide an explanation of why that data is not in the current version</li> 
<li>For cases where obsolete count is 0 and current count is not, 
    provide an explanation of why there is new data</li>
</ul>
<center><table border=0 width="90%">
<form method="POST" action="$cgi">
    <input type="hidden" name="state" value="SRC_OBSOLETE_DIFF_DONE">
    <input type="hidden" name="db" value="$db">
};
 $prev  = "";
 $found = 0;

 #
 # Here use the $qa_id (negative #)
 #
 while ( ( $name, $value, $id, $prevct, $curct, $diff, $type, $display_value ) =
         $sh->fetchrow_array )
 {
  $qa_id = $id;
  next unless $diff;
  $found = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="4" align=left>$name ($current_name,$previous_name)</th></tr>
  };
  }
  $prev = $name;
  print qq{
  <tr><td>
      <input type="hidden" name="qa_name" value="$name">
      <input type="hidden" name="qa_value" value="$value">
      $display_value ($curct, $prevct)
  </td>
  <td><font size="-2">$type</font></td>
  <td>
      <input type="text" name="adjustment" value="$diff" size=10>
  </td>
  <td>
      <input type="text" name="adjustment_dsc" size=50>
  </td></tr>
};
 }
 if ($found) {
  print qq{
<tr><td colspan="4">&nbsp</td></tr>
<tr><td colspan="4">
    <input type="hidden" name="qa_id" value="$qa_id">
<center><input type="submit" value="Submit Adjustments"></center>
</td></tr>
};
 } else {
  print qq{
	    <tr><td>There are no differences between the SRC and SRC Obsolete
	        counts.</td></tr>
};
 }
 print qq{
</form>
</table></center>
<p>
}
};    # end PrintSRC_OBSOLETE_DIFF
###################### Procedure PrintSRC_OBSOLETE_DIFF_DONE ######################
#
# This procedure adds the adjustments to the qa_diff_adjustment table
#
sub PrintSRC_OBSOLETE_DIFF_DONE {

 # use @qa_name, @qa_value, @qa_id, @adjustment, and @adjustmen_dsc
 # to insert rows into qa_diff_adjustment.  where the adjustment_dsc exists
 $sh = $dbh->prepare(
  qq{
	insert into qa_diff_adjustment 
	  (qa_id_1, qa_id_2, name, value, 
	   qa_count, timestamp, description)
	    values ( $qa_id, $qa_id, ?, ?, ?, SYSDATE, ? )
	}
   )
   || ( ( print "<span id=red>Error preparing insert stmt.</span>" )
        && return );
 $ct = 0;
 for ( $i = 0 ; $i <= $#qa_name ; $i++ ) {

  #print "$qa_name[$i]: $adjustment_dsc[$i], $adjustment[$i]<br>\n";
  if ( $adjustment_dsc[$i] && $adjustment[$i] ) {
   $sh->execute( $qa_name[$i],    $qa_value[$i],
                 $adjustment[$i], $adjustment_dsc[$i] )
     || (
    (
     print
"<span id=red>Error adding SRC-SRC Obsolete adjustment  ($qa_id, $qa_name[$i], $qa_value[$i],$adjustment[$i], $adjustment_dsc[$i], $DBI::errstr).</span>"
    )
    && return
     );
   $ct++;
  }
 }
 print qq{
	$ct adjustments were added.  Go back to the index page and
	compare SRC-SRC Obsolete counts again to see if any differences remain.
	};
};    # end PrintSRC_OBSOLETE_DIFF_DONE
###################### Procedure PrintMID_MID_DIFF ######################
#
# This procedure prints a page that shows the difference between
# an old MID snapshot and current MID counts
#
#
sub PrintMID_MID_DIFF {

 $thresholds{$threshold} = "SELECTED";
 #
 # Value expressed in "current" terms,
 #
 $sh = $dbh->prepare(
  qq{
	  SELECT name, value, qa_id_1, count_1, count_2, count_2-count_1, type,
	       REPLACE(value,nvl(previous_name,current_name||'-NEW'),current_name) display_value
  	FROM mid_mid_diff_results, released_source_version 
  	WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = nvl(previous_name,current_name||'-NEW')
    UNION
    SELECT name, value, qa_id_1, count_1, count_2, count_2-count_1, type,
		   value
  	FROM mid_mid_diff_results, released_source_version 
  	WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = current_name
    UNION 
    SELECT name, value, qa_id_1, count_1, count_2, count_2-count_1, type, value
    FROM mid_mid_diff_results 
    WHERE name = 'mid_sty_tally'
    }
   )
   || ( ( print "<span id=red>Error reading from mid_mid_diff_results.</span>" )
        && return );
 $sh->execute
   || ( ( print "<span id=red>Error reading from mid_mid_diff_results.</span>" )
        && return );
 print qq{
<i>Fill out adjustment reasons and edit the adjustment count if necessary. Then click "Submit Adjustments" at the bottom of the page.  
<p>[Note: The counts in parenthesis are (current MID,historical MID) counts]</p>
</i>
<form name="myform" action="$cgi" method="GET">
<input type="hidden" name="state" value="MID_MID_DIFF">
<input type="hidden" name="db" value="$db">
<div align="center">
<select name="threshold">
<option value="0" $thresholds{"0"}>No Threshold</option>
<option value="10" $thresholds{"10"}>10</option>
<option value="100" $thresholds{"100"}>100</option>
<option value="1000" $thresholds{"1000"}>1000</option>
</select>
<INPUT TYPE=SUBMIT VALUE="Threshold">
</div>
</form>
<center><table border=0 width="90%">
<form method="POST" action="$cgi">
    <input type="hidden" name="state" value="MID_MID_DIFF_DONE">
    <input type="hidden" name="db" value="$db">
};
 $prev  = "";
 $found = 0;

 while (
   ( $name, $value, $qa_id, $historyct, $midct, $diff, $type, $display_value ) =
   $sh->fetchrow_array )
 {
  if (abs($diff) < $threshold) {
   next;
  }
  $id    = "$qa_id";
  $found = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="4" align=left>$name</th></tr>
  };
  }
  $prev = $name;
  $subName = substr($name,4);
  print qq{
  <tr><td>
      <input type="hidden" name="qa_name" value="$subName">
      <input type="hidden" name="qa_value" value="$value">
      $display_value ($midct,$historyct)
  </td>
  <td><font size="-2">$type</font></td>
  <td>
      <input type="text" name="adjustment" value="$diff" size=10>
  </td>
  <td>
      <input type="text" name="adjustment_dsc" size=50>
  </td></tr>
};
 }
 if ($found) {
  print qq{
<tr><td colspan="4">&nbsp</td></tr>
<tr><td colspan="4">
    <input type="hidden" name="qa_id" value="$id">
<center><input type="submit" value="Submit Adjustments"></center>
</td></tr>
};
 } else {
  print qq{
	    <tr><td>There are no differences between the current MID and the historical MID ($qa_id) counts.</td></tr>
};
 }
 print qq{
</form>
</table></center>
<p>
}
};    # end PrintMID_MID_DIFF
###################### Procedure PrintMID_MID_DIFF_DONE ####################
#
# This procedure adds the adjustments to the qa_diff_adjustment table
#
sub PrintMID_MID_DIFF_DONE {
 $ct = 0;
 for ( $i = 0 ; $i <= $#qa_name ; $i++ ) {

  #check to see if the row exist
  $sh = $dbh->prepare(
   qq{
			select count(*) from qa_adjustment
			where qa_id = $qa_id
			and name = ?
			and value = ?
			}
    )
    || ( ( print "<span id=red>Error preparing select stmt.</span>" )
         && return );
  $sh->execute( $qa_name[$i], $qa_value[$i] )
    || ( ( print "<span id=red>Error executing select stmt.</span>" )
         && return );
  my ($count) = $sh->fetchrow_array;

  #update instead of insert
  if ( $count > 0 ) {
   $sh = $dbh->prepare(
    qq{
					update qa_adjustment set qa_count = qa_count + ?, 
					description = description || '<br>' || ?
					where qa_id = $qa_id
					and name = ?
					and value = ?
				}
     )
     || ( ( print "<span id=red>Error preparing update stmt.</span>" )
          && return );
   if ( $adjustment_dsc[$i] && $adjustment[$i] ) {
    $sh->execute( $adjustment[$i], $adjustment_dsc[$i],
                  $qa_name[$i],    $qa_value[$i] )
      || (
     (
      print
"<span id=red>Error updating MID-MID adjustment  ($qa_name[$i], $qa_value[$i],$adjustment[$i], $adjustment_dsc[$i], $DBI::errstr).</span>"
     )
     && return
      );
    $ct++;
   }
  } else {

   # use @qa_name, @qa_value, @adjustment, and @adjustmen_dsc
   # to insert rows into qa_adjustment.  where the adjustment_dsc exists
   $sh = $dbh->prepare(
    qq{
	insert into qa_adjustment (qa_id, name, value, qa_count, timestamp, description)
	    values ( $qa_id, ?, ?, ?, SYSDATE, ? )
	}
     )
     || ( ( print "<span id=red>Error preparing insert stmt.</span>" )
          && return );
   if ( $adjustment_dsc[$i] && $adjustment[$i] ) {
    $sh->execute( $qa_name[$i],    $qa_value[$i],
                  $adjustment[$i], $adjustment_dsc[$i] )
      || (
     (
      print
"<span id=red>Error adding MID-MID adjustment  ($qa_name[$i], $qa_value[$i],$adjustment[$i], $adjustment_dsc[$i], $DBI::errstr).</span>"
     )
     && return
      );
    $ct++;
   }
  }
 }
 print qq{
	$ct adjustments were added.  Go back to the index page and
	compare MID(current)-MID(historic) counts again to see if any differences remain.
	};
};    # end PrintMID_MID_DIFF_DONE

###################### Procedure PrintSRC_OBSOLETE_ADJ_REPORT ######################
#
# This procedure prints a report of reasons between the current source and
# its last released version
#
sub PrintSRC_OBSOLETE_ADJ_REPORT {

 #
 # Get current name
 #
 my ( $current_name, $previous_name ) = split /,/, $source;
 if ( $current_name eq "" ) {
  $current_name = $previous_name;
 }

 #
 # Look up adjustments, group them by name
 #
 $sh = $dbh->prepare(
  qq{
			SELECT qa_id_1, qa_id_2, name, value, cur_count, prev_count ,adj_count, timestamp, description, type,
   	           replace(value,nvl(previous_name,current_name||'-NEW'),b.current_name) display_value
			FROM src_obsolete_adj_report a, released_source_version b
			WHERE b.current_name = '$current_name'
			  AND SUBSTR(value,1,INSTR(value||',',',')-1) = nvl(previous_name,current_name||'-NEW')
			ORDER BY name,value
	}
   )
   || (
  (
   print
"<span id=red>Error reading from src_qa_results a, src_obsolete_qa_results b, qa_diff_adjustment c, released_source_version d.</span>"
  )
  && return
   );
 $sh->execute
   || (
  (
   print
"<span id=red>Executing Error reading from src_qa_results a, src_obsolete_qa_results b, qa_diff_adjustment c, released_source_version d.</span>"
  )
  && return
   );

 #
 # Print the report
 #
 print qq{
<i>Following is the conservation of mass report between the most recent SRC snapshot and the obsolete SRC snapshot.</i><p>
<center><table border=1 width="90%">
};
 $prev  = "";
 $found = 0;
 while (
         (
           $qa_id_1,       $qa_id_2,        $name,       $value,
           $current_count, $previous_count, $diff_count, $timestamp,
           $description,   $type,           $display_value
         )
         = $sh->fetchrow_array
   )
 {
  $found = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="8" align=center>$name</th></tr>
  <tr><th>Value</th><th>Current</th><th>Previous</th><th>Diff</th><th>Type</th><th width="30%">Description</th><th>Timestamp</th><th>&nbsp;</th></tr>
  };
  }
  $prev = $name;
  $i++;
  $timestamp =~ s/ /&nbsp;/g;
  if (length($display_value)>30){
    $display_value =~ s/,/, /g;
  }
  print qq{
  <tr> 
    <form action="$cgi" name="form$i">
  <td>$display_value</td>
  <td align=right>$current_count</td>
  <td align=right>$previous_count</td>
  <td align=right>$diff_count</td>
  <td><font size="-2">$type</font></td>
  <td>$description</td>
  <td>$timestamp</td>
  <td>
    <input type="hidden" name="db" value="$db">
    <input type="hidden" name="state" value="ADJ_EDIT_DONE">
    <input type="hidden" name="qa_id_1" value="$qa_id_1">
    <input type="hidden" name="qa_id_2" value="$qa_id_2">
    <input type="hidden" name="qa_name" value="$name">
    <input type="hidden" name="qa_value" value="$value">
    <input type="hidden" name="adjustment_dsc" value="$description">
    [<a href="javascript:document.form$i.submit()">DEL</a>]
  </form>
   </td>
  
  </tr>
};
 }
 unless ($found) {
  print qq{No adjustments found.};
 }
 print qq{
</table></center>
<p>
};
};    # end PrintSRC_OBSOLETE_ADJ_REPORT
###################### Procedure PrintADJ_REPORT ######################
#
# This procedure prints a report of adjustments for MID-SRC comparison
#
sub PrintMID_SRC_ADJ_REPORT {

 #
 # Look up adjustments, group them by name
 #
 $sh = $dbh->prepare(
  qq{
      SELECT qa_id, name, value, 
          cur_count, prev_count, adj_count, 
          description, type, timestamp
      FROM mid_src_adj_report
      ORDER BY name,value
	}
   )
   || (
  (
   print
"<span id=red>Error reading from mid_qa_results, mid_qa_history or qa_diff_adjustment.</span>"
  )
  && return
   );
 $sh->execute
   || (
  (
   print
"<span id=red>Executing Error reading from mid_qa_results, mid_qa_history or qa_diff_adjustment.</span>"
  )
  && return
   );
 print qq{
<i>Following are the editing QA report between the most recent MID snapshot and the original state of the MID for this release on $min_timestamp.</i><p>
<center><table border=1 width="90%">
};
 $prev  = "";
 $found = 0;

 while (
         (
           $qa_id,         $name,           $value,
           $current_count, $previous_count, $diff_count,
           $description,   $type,           $timestamp
         )
         = $sh->fetchrow_array
   )
 {
  $found = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="8" align=center>$name</th></tr>
  <tr><th>Value</th><th>Current</th><th>Previous</th><th>Adj</th><th>Type</th><th>Description</th><th>Timetstamp</th><th>&nbsp;</th></tr>
  };
  }
  $prev = $name;
  $i++;
  print qq{
  <tr> 
  <form name="form$i" action="$cgi">
  <td>$value</td>
  <td align=right>$current_count</td>
  <td align=right>$previous_count</td>
  <td align=right>$diff_count</td>
  <td><font size="-2">$type</font></td>
  <td>$description</td>
  <td>$timestamp</td>
  <td>
    <input type="hidden" name="db" value="$db">
    <input type="hidden" name="state" value="ADJ_EDIT_DONE">
    <input type="hidden" name="qa_id" value="$qa_id">
    <input type="hidden" name="qa_name" value="$name">
    <input type="hidden" name="qa_value" value="$value">
    <input type="hidden" name="adjustment_dsc" value="$description">
    [<a href="javascript:document.form$i.submit()">DEL</a>]
  <tr> 
  </form>
  </td>
 </tr>
};
 }
 unless ($found) {
  print qq{No adjustments found.};
 }
 print qq{
</table></center>
<p>
};
};    # end PrintMID_SRC_QA_REPORT
###################### Procedure PrintMID_MID_ADJ_REPORT ######################
#
# This procedure prints a report for comparing the latest snapshot of the MID
# to the earliest snapshot of the MID
#
sub PrintMID_MID_ADJ_REPORT {

 #
 # Look up adjustments, group them by name
 #
 $sh = $dbh->prepare(
  qq{
      SELECT qa_id, name, value, 
         cur_count, prev_count, adj_count, 
         description, type, timestamp
      FROM mid_mid_adj_report
      ORDER BY name,value
	}
   )
   || (
  (
   print
"<span id=red>Error reading from mid_qa_results, mid_qa_history or qa_diff_adjustment.</span>"
  )
  && return
   );
 $sh->execute
   || (
  (
   print
"<span id=red>Executing Error reading from mid_qa_results, mid_qa_history or qa_diff_adjustment.</span>"
  )
  && return
   );
 print qq{
<i>Following are the editing QA report between the most recent MID snapshot and the original state of the MID for this release on $min_timestamp.</i><p>
<center><table border=1 width="90%">
};
 $prev  = "";
 $found = 0;

 while (
         (
           $qa_id,         $name,           $value,
           $current_count, $previous_count, $diff_count,
           $description,   $type,           $timestamp
         )
         = $sh->fetchrow_array
   )
 {
  $found = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="8" align=center>$name</th></tr>
  <tr><th>Value</th><th>Current</th><th>Previous</th><th>Adj</th><th>Type</th><th>Description</th><th>Timetstamp</th><th>&nbsp;</th></tr>
  };
  }
  $prev = $name;
  $i++;
  print qq{
  <tr> 
     <form action="$cgi" name="form$i" >
  <td>$value</td>
  <td align=right>$current_count</td>
  <td align=right>$previous_count</td>
  <td align=right>$diff_count</td>
  <td><font size="-2">$type</font></td>
  <td>$description</td>
  <td>$timestamp</td>
   <td>
    <input type="hidden" name="db" value="$db">
    <input type="hidden" name="state" value="ADJ_EDIT_DONE">
    <input type="hidden" name="qa_id" value="$qa_id">
    <input type="hidden" name="qa_name" value="$name">
    <input type="hidden" name="qa_value" value="$value">
    <input type="hidden" name="adjustment_dsc" value="$description">
    [<a href="javascript:document.form$i.submit()">DEL</a>]
  </form>
   </td>
  </tr>
};
 }
 unless ($found) {
  print qq{No adjustments found.};
 }
 print qq{
</table></center>
<p>
};
};    # end PrintMID_MID_ADJ_REPORT
###################### Procedure PrintOBSOLETE_UI_REPORT ######################
#
# This procedure prints a report for a given root_source of its obsolete
# UI and provide guidance on which ones to delete to free up obsolete_ui tables
#
sub PrintOBSOLETE_UI_REPORT {

 #
 # Look up obsolete_ui info and group them by type
 #
 $sh = $dbh->prepare(
  qq{
SELECT ct, tty, code_flag,saui_flag,scui_flag,sdui_flag
FROM obsolete_ui
WHERE root_source = '$source'
AND type = 'AUI'
ORDER BY 1 DESC
	}
   )
   || ( ( print "<span id=red>Error reading from obsolete_ui for AUI.</span>" )
        && return );
 $sh->execute
   || (
  (
   print "<span id=red>Executing Error reading from obsolete_ui for AUI.</span>"
  )
  && return
   );
 $thresholds{$threshold} = "SELECTED";
 print qq{
<form name="myform" action="$cgi" method="POST">
<input type="hidden" name="state" value="OBSOLETE_UI_REPORT">
<input type="hidden" name="db" value="$db">
<input type="hidden" name="source" value="$source">
<div align="center">
<select name="threshold">
<option value="0" $thresholds{"0"}>0</option>
<option value="5000" $thresholds{"5000"}>5000</option>
<option value="50000" $thresholds{"50000"}>50000</option>
<option value="500000" $thresholds{"500000"}>500000</option>
</select>
<INPUT TYPE=SUBMIT VALUE="Threshold">
</div>
</form>
<form method="POST" action="$cgi">
    <input type="hidden" name="state" value="OBSOLETE_UI_SQL">
    <input type="hidden" name="db" value="$db">
    <center><input type="submit" value="Generate SQL"></center>
};
 $found          = 0;
 $any_data_found = 0;

 while ( ( $ct, $tty, $code_flag, $saui_flag, $scui_flag, $sdui_flag ) =
         $sh->fetchrow_array )
 {
  $found++;
  $any_data_found = 1;
  if ( $found == 1 ) {
   print qq{
				<center><table border=1 width="90%">
  <tr><th colspan="7" align=center>$source : AUI obsolete UI Report</th></tr>
  <tr><th>SQL?</th><th>Count</th><th>TTY</th><th>Code?</th><th>SAUI?</th><th>SCUI?</th><th>SDUI?</th></tr>
  };
  }
  if ( $ct > $threshold ) {
   print
qq{<tr><td><INPUT TYPE=CHECKBOX NAME="obsolete" CHECKED value="AUI,$source,$tty,$code_flag,$saui_flag,$scui_flag,$sdui_flag"></td>};
  } else {
   print
qq{<tr><td><INPUT TYPE=CHECKBOX NAME="obsolete" value="AUI,$source,$tty,$code_flag,$saui_flag,$scui_flag,$sdui_flag"></td>};
  }
  print qq{
  <td align=right>$ct</td>
  <td align=center>$tty</td>
  <td align=center>$code_flag</td>
  <td align=center>$saui_flag</td>
  <td align=center>$scui_flag</td>
  <td align=center>$sdui_flag</td>
  </tr>
};
 }
 if ($found) {
  print qq{
	</table></center>	
	};
 }

 #
 # Look up obsolete_ui info and group them by type
 #
 $sh = $dbh->prepare(
  qq{
SELECT ct, relationship_name, relationship_attribute,sg_type_1,sg_type_2
FROM obsolete_ui
WHERE root_source = '$source'
AND type = 'RUI'
ORDER BY 1 DESC
	}
   )
   || ( ( print "<span id=red>Error reading from obsolete_ui for RUI.</span>" )
        && return );
 $sh->execute
   || (
  (
   print "<span id=red>Executing Error reading from obsolete_ui for RUI.</span>"
  )
  && return
   );
 $found = 0;
 while (
  ( $ct, $relationship_name, $relationship_attribute, $sg_type_1, $sg_type_2 ) =
  $sh->fetchrow_array )
 {
  $found++;
  $any_data_found = 1;
  if ( $found == 1 ) {
   print qq{
				<center><table border=1 width="90%">
  <tr><th colspan="6" align=center>$source : RUI obsolete UI Report</th></tr>
  <tr><th>SQL?</th><th>Count</th><th>Rel Name</th><th>Rel Attribute</th><th>sg_type_1</th><th>sg_type_2</th></tr>
  };
  }
  if ( $ct > $threshold ) {
   print
qq{<tr><td><INPUT TYPE=CHECKBOX NAME="obsolete" value="RUI,$source,$relationship_name,$relationship_attribute,$sg_type_1,$sg_type_2" CHECKED></td>};
  } else {
   print
qq{<tr><td><INPUT TYPE=CHECKBOX NAME="obsolete" value="RUI,$source,$relationship_name,$relationship_attribute,$sg_type_1,$sg_type_2"></td>};
  }
  print qq{
  <td align=right>$ct</td>
  <td align=center>$relationship_name</td>
  <td align=center>$relationship_attribute</td>
  <td align=center>$sg_type_1</td>
  <td align=center>$sg_type_2</td>
  </tr>
};
 }
 if ($found) {
  print qq{
	</table></center>	
	};
 }

 #
 # Look up obsolete_ui info and group them by ATUI
 #
 $sh = $dbh->prepare(
  qq{
SELECT ct,attribute_name,sg_type
FROM obsolete_ui
WHERE root_source = '$source'
AND type = 'ATUI'
ORDER BY 1 DESC
	}
   )
   || ( ( print "<span id=red>Error reading from obsolete_ui for ATUI.</span>" )
        && return );
 $sh->execute
   || (
       (
        print
        "<span id=red>Executing Error reading from obsolete_ui for ATUI.</span>"
       )
       && return
   );
 $found = 0;
 while ( ( $ct, $attribute_name, $sg_type ) = $sh->fetchrow_array ) {
  $found++;
  $any_data_found = 1;
  if ( $found == 1 ) {
   print qq{
				<center><table border=1 width="90%">
  <tr><th colspan="4" align=center>$source : ATUI obsolete UI Report</th></tr>
  <tr><th>SQL?</th><th>Count</th><th>Attribute Name</th><th>sg_type</th></tr>
  };
  }
  if ( $ct > $threshold ) {
   print
qq{<tr><td><INPUT TYPE=CHECKBOX NAME="obsolete" value="ATUI,$source,$attribute_name,$sg_type" CHECKED></td>};
  } else {
   print
qq{<tr><td><INPUT TYPE=CHECKBOX NAME="obsolete" value="ATUI,$source,$attribute_name,$sg_type"></td>};
  }
  print qq{
  <td align=right>$ct</td>
  <td align=center>$attribute_name</td>
  <td align=center>$sg_type</td>
  </tr>
};
 }
 unless ($any_data_found) {
  print qq{No obsolete UI data found.};
 }
 print qq{
</table></center>
</form>
};
};    # end PrintOBSOLETE_UI_REPORT
###################### Procedure PrintOBSOLETE_UI_SQL ######################
#
# This procedure prints a page of the SQL necessary to remove the obsolete
# UI data.
#
sub PrintOBSOLETE_UI_SQL {
 $aui  = 0;
 $rui  = 0;
 $atui = 0;
 for ( $i = 0 ; $i <= $#obsolete ; $i++ ) {
  ($type) = split /,/, $obsolete[$i];
  if ( $type eq "AUI" ) {
   $aui++;
   if ( $aui == 1 ) {
    print qq{
					exec MEME_UTILITY.drop_it('table','atoms_ui_bak'); <br>
					CREATE TABLE atoms_ui_bak AS select * from atoms_ui where 1=0;<br>
				};
   }
   ( $type, $source, $tty, $code_flag, $saui_flag, $scui_flag, $sdui_flag ) =
     split /,/, $obsolete[$i];
   $sql = "INSERT INTO atoms_ui_bak SELECT * FROM atoms_ui<br>";
   $sql .= "WHERE stripped_source='$source'<br>";
   $sql .= "AND tty ='$tty'<br>";
   if ( $code_flag eq "N" ) {
    $sql .= "AND code is null";
   } else {
    $sql .= "AND code is not null";
   }
   if ( $saui_flag eq "N" ) {
    $sql .= " AND source_aui is null";
   } else {
    $sql .= " AND source_aui is not null";
   }
   if ( $scui_flag eq "N" ) {
    $sql .= " AND source_cui is null";
   } else {
    $sql .= " AND source_cui is not null";
   }
   if ( $sdui_flag eq "N" ) {
    $sql .= " AND source_dui is null";
   } else {
    $sql .= " AND source_dui is not null";
   }
   $sql .= ";<br>";
   print $sql;
  } elsif ( $type eq "RUI" ) {
   $rui++;
   if ( $rui == 1 ) {
    print qq{
					exec MEME_UTILITY.drop_it('table','relationships_ui_bak'); <br>
					CREATE TABLE relationships_ui_bak AS select * from relationships_ui where 1=0;<br>
				};
   }
   ( $type, $source, $rel_name, $rel_attribute, $sg_type_1, $sg_type_2 ) =
     split /,/, $obsolete[$i];
   $sql = "INSERT INTO relationships_ui_bak SELECT * FROM relationships_ui<br>";
   $sql .= "WHERE root_source='$source'<br>";
   $sql .= "AND relationship_name='$rel_name'";
   $sql .=
     " AND NVL(relationship_attribute,'null')=NVL('$rel_attribute','null')<br>";
   $sql .= "AND sg_type_1='$sg_type_1' AND sg_type_2='$sg_type_2';<br>";
   print $sql;
  } elsif ( $type eq "ATUI" ) {
   $atui++;
   if ( $atui == 1 ) {
    print qq{
					exec MEME_UTILITY.drop_it('table','attribute_ui_bak'); <br>
					CREATE TABLE attributes_ui_bak AS select * from attributes_ui where 1=0;<br>
				};
   }
   ( $type, $source, $attribute_name, $sg_type ) = split /,/, $obsolete[$i];
   $sql = "INSERT INTO attributes_ui_bak SELECT * FROM attributes_ui<br>";
   $sql .= "WHERE root_source='$source'<br>";
   $sql .= "AND attribute_name='$attribute_name' AND sg_type='$sg_type';<br>";
   print $sql;
  }
 }
 if ($aui) {
  print qq{
			COMMIT;<br>
			DELETE FROM atoms_ui WHERE (stripped_source,tty,<br>
              NVL(code,'null'),NVL(source_aui,'null'),NVL(source_cui,'null'),NVL(source_dui,'null')) IN<br>
              (SELECT stripped_source,tty, NVL(code,'null'),NVL(source_aui,'null'),NVL(source_cui,'null'),NVL(source_dui,'null')<br>
              FROM atoms_ui_bak);<br>
			COMMIT;<br>
			EXEC MEME_SYSTEM.rebuild_table(table_name=>'atoms_ui');<br>		
			};
 }
 if ($rui) {
  print qq{
			COMMIT;<br>
			DELETE FROM relationships_ui WHERE (root_source,relationship_name,NVL(relationship_attribute,'null'),sg_type_1,sg_type_2) IN<br>
                 (SELECT root_source,relationship_name,NVL(relationship_attribute,'null'),sg_type_1,sg_type_2 FROM relationships_ui_bak);<br>
			COMMIT;<br>
    		EXEC MEME_SYSTEM.rebuild_table(table_name=>'relationships_ui');<br>	
			};
 }
 if ($atui) {
  print qq{
			COMMIT;<br>
			DELETE FROM attributes_ui WHERE (root_source,attribute_name,sg_type) IN<br>
            (SELECT root_source,attribute_name,sg_type FROM attributes_ui_bak);<br>
			COMMIT;<br>
			EXEC MEME_SYSTEM.rebuild_table(table_name=>'attributes_ui');<br>			
			};
 }
};    # end PrintOBSOLETE_UI_SQL


###################### Procedure PrintADJ_EDIT_DONE ######################
#
# Submit a change to an adjustment
#
sub PrintADJ_EDIT_DONE {

 #
 # Handle $qa_id Case
 #
 if ($qa_id) {

 $subName = substr($qa_name,4) if $qa_name =~ /^mid_/;
 
  #
  # Delete adjustment (for either DELETE or MODIFY)
  #
  $rc = $dbh->do(
   qq{
        DELETE FROM qa_adjustment
        WHERE qa_id = ?
          AND name = ?
          AND value = ?
          AND description = ?
	}, undef, $qa_id, $subName, $qa_value, $adjustment_dsc
    )
    || ( ( print "<span id=red>Error deleting from qa_adjustment.</span>" )
         && return );
  print qq{<br>QA adjustments matching:<ul>
	<li>QA_ID = $qa_id</li>
	<li>NAME = $subName</li>
	<li>VALUE = $qa_value</li>
	<li>DESCRIPTION = $adjustment_dsc</li>
	</ul>were deleted ($rc rows).<br>};
 } elsif ( $qa_id_1 && $qa_id_2 ) {

  #
  # Delete adjustment (for either DELETE or MODIFY)
  #
  $rc = $dbh->do(
   qq{
        DELETE FROM qa_diff_adjustment
        WHERE qa_id_1 = ? AND qa_id_2 = ?
          AND name = ?
          AND value = ?
          AND description = ?
	}, undef, $qa_id_1, $qa_id_2, $qa_name, $qa_value, $adjustment_dsc
    )
    || ( ( print "<span id=red>Error deleting from qa_adjustment.</span>" )
         && return );
  print qq{<br>QA adjustments matching:<ul>
	<li>QA_ID_1 = $qa_id_1</li>
	<li>QA_ID_2 = $qa_id_2</li>
	<li>NAME = $qa_name</li>
	<li>VALUE = $qa_value</li>
	<li>DESCRIPTION = $adjustment_dsc</li>
	</ul>were deleted ($rc rows).<br>};
 } else {
  print
    "<span id=red>Error either qa_id, or qa_id_[12] needs to be set.</span>";
 }
};    # end PrintADJ_EDIT_DONE
###################### Procedure PrintREVIEW_MID_COUNTS ######################
#
# This procedure prints a page that allows one to view current
# Monster QA MID Counts.
#
sub PrintREVIEW_MID_COUNTS {

 #
 # Look up counts, group by name
 #
 $sh = $dbh->prepare(
  qq{
        SELECT qa_id, name, value, qa_count, timestamp
	FROM mid_qa_history where qa_id = ?
	UNION
        SELECT qa_id, name, value, qa_count, timestamp
	FROM mid_qa_results where qa_id = ?
	ORDER BY name asc, timestamp desc
	}
   )
   || ( ( print "<span id=red>Error reading from qa_diff_adjustment.</span>" )
        && return );
 $sh->execute( $qa_id, $qa_id )
   || (
    ( print "<span id=red>Error reading from qa_diff_adjustment table.</span>" )
    && return );
 print qq{
<i>Following are the MID counts for the selected set.</i><p>
<center><table border=0 width="90%">
};
 $prev  = "";
 $found = 0;

 while ( ( $qa_id, $name, $value, $ct, $timestamp ) = $sh->fetchrow_array ) {
  $found = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="2" align=left>$name ($timestamp)</th></tr>
  };
  }
  $prev = $name;
  $timestamp =~ s/ /&nbsp;/g;
  print qq{
  <tr><td width="10%">$ct</td>
  <td><tt>$value</tt></td></tr>
};
 }
 unless ($found) {
  print qq{No counts found.};
 }
 print qq{
</table></center>
<p>
};
};    # end PrintREVIEW_MID_COUNTS
###################### Procedure PrintREVIEW_SRC_COUNTS ######################
#
sub PrintREVIEW_SRC_COUNTS {

 #
 # Look up counts, group by name
 #
 $sh = $dbh->prepare(
  qq{
    SELECT qa_id, name, value, qa_count, timestamp
	FROM src_qa_results
	WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = ?
	}
   )
   || ( ( print "<span id=red>Error reading from src_qa_results.</span>" )
        && return );
 $sh->execute( $source )
   || ( ( print "<span id=red>Error reading from src_qa_results table.</span>" )
        && return );
 print qq{
<i>Following are the SRC counts for: $source.</i><p>
<center><table border=0 width="90%">
};
 $prev  = "";
 $found = 0;

 while ( ( $qa_id, $name, $value, $ct, $timestamp ) = $sh->fetchrow_array ) {
  $found = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="2" align=left>$name ($timestamp)</th></tr>
  };
  }
  $prev = $name;
  $timestamp =~ s/ /&nbsp;/g;
  print qq{
  <tr><td width="10%">$ct</td>
  <td><tt>$value</tt></td></tr>
};
 }
 unless ($found) {
  print qq{No counts found.};
 }
 print qq{
</table></center>
<p>
};
};    # end PrintREVIEW_SRC_COUNTS
###################### Procedure PrintREVIEW_INV_COUNTS ######################
#
sub PrintREVIEW_INV_COUNTS {

 #
 # Look up counts, group by name
 #
 $sh = $dbh->prepare(
  qq{
    SELECT qa_id, name, value, qa_count, timestamp
	FROM inv_qa_results
	WHERE SUBSTR(value,1,INSTR(value||',',',')-1) = ?
	}
   )
   || ( ( print "<span id=red>Error reading from inv_qa_results.</span>" )
        && return );
 $sh->execute( $source)
   || ( ( print "<span id=red>Error reading from inv_qa_results table.</span>" )
        && return );
 print qq{
<i>Following are the INV counts for: $source.</i><p>
<center><table border=0 width="90%">
};
 $prev  = "";
 $found = 0;

 while ( ( $qa_id, $name, $value, $ct, $timestamp ) = $sh->fetchrow_array ) {
  $found = 1;
  if ( $prev ne $name ) {
   print qq{
  <tr><th colspan="2" align=left>$name ($timestamp)</th></tr>
  };
  }
  $prev = $name;
  $timestamp =~ s/ /&nbsp;/g;
  print qq{
  <tr><td width="10%">$ct</td>
  <td><tt>$value</tt></td></tr>
};
 }
 unless ($found) {
  print qq{No counts found.};
 }
 print qq{
</table></center>
<p>
};
};    # end PrintREVIEW_INV_COUNTS
###################### Procedure PrintQUERIES ######################
#
# This procedure prints a page that allows one to modify,delete or insert
# a SRC QA Query.
#
sub PrintQUERIES {
 print qq{
	<i>Following is a list of all of the queries used to generate counts for the $command data.  To change the name for a query, or to change the query itself, just edit the corresponding text field.  To delete a query, erase the contents of the text field and the query field.  To insert a new query, fill out the fields at the bottom of the page.  When you are finished making changes, click the "Submit" button.</i>
	<form action="$cgi" method="POST">
	    <input type="hidden" name="state" value="QUERIES_DONE">
	    <input type="hidden" name="db" value="$db">
	    <input type="hidden" name="command" value="$command">

	<center><table width="90%" border="0">
	    <tr><td>
};
 $sh = $dbh->prepare(
  qq{
	SELECT name,query FROM ${command}_qa_queries
	}
   )
   || (
  (
   print
"<span id=red>Error preparing query to select from ${command}_qa_queries ($DBI::errstr).</span>"
  )
  && return
   );
 $sh->execute
   || (
  (
   print
"<span id=red>Error selecting from ${command}_qa_queries ($DBI::errstr).</span>"
  )
  && return
   );
 while ( ( $name, $query ) = $sh->fetchrow_array ) {
  print qq{
	    <input type="text" name="name" value="$name" size=70><br>
	    <textarea name="query" rows="4" cols="70" wrap="soft">$query</textarea><br>
	    <hr width="100%"><p>
	    };
 }
 print qq{
	    To insert a new query, fill these fields out:<br>
	    <input type="text" name="name" value="" size="70"><br>
	    <textarea name="query" rows="4" cols="70" wrap="soft"></textarea><br>
	    <p><center><input type="submit" value="Submit"></center>
	    </td></tr>
	</table></center>
        </form>
	<p>
	};
};    # end PrintQUERIES
###################### Procedure PrintQUERIES_DONE ######################
#
# This updates the src_qa_queries/mid_qa_queries tables
#
sub PrintQUERIES_DONE {

 #
 # The data is in the @name and @query arrays
 #
 $dbh->{AutoCommit} = 0;
 $dbh->do("DELETE FROM ${command}_qa_queries")
   || (
  (
   print
"<span id=red>Error truncating old ${command}_qa_queries data ($DBI::errstr).</span>"
  )
  && return
   );
 $sh = $dbh->prepare(
  qq{
	INSERT INTO ${command}_qa_queries (name, query) values (?,?) 
	}
   )
   || (
  (
   print
"<span id=red>Error preparing query to replace old ${command}_qa_queries data ($DBI::errstr).</span>"
  )
  && return
   );
 for ( $i = 0 ; $i <= $#name ; $i++ ) {
  if ( $name[$i] && $query[$i] ) {
   $sh->execute( $name[$i], $query[$i] )
     || (
    (
     print
"<span id=red>Error inserting ${command}_qa_queries data ($name[$i], $value[$i], $DBI::errstr).</span>"
    )
    && return
     );
  }
 }
 $dbh->commit;
 print qq{
	<i>The ${command} queries were succesfully replaced.  
          The new data follows</i><p>
        <center><table width="90%" border="0">
    };
 for ( $i = 0 ; $i <= $#name ; $i++ ) {
  if ( $name[$i] && $query[$i] ) {
   print qq{
          <tr><td valign="top"><b>Test&nbsp;Name:</b></td><td>$name[$i]</td></tr>
          <tr><td valign="top"><b>Query:</b></td><td>$query[$i]</td></tr>
	  <tr><td colspan="2">&nbsp;</td></tr>
};
  }
 }
 print qq{
        </table></center>
};
};    # end PrintQUERIES_DONE
###################### Procedure PrintMRD_COPY ######################
#
# This procedure copies the mid_qa_results and mid_qa_history
# tables to mrd from the current db.
#
sub PrintMRD_COPY {

 # set variables
 $mrd_db   = &meme_utils::midsvcs("mrd-db");
 $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $mrd_db`;
 chop($userpass);
 open( F, ">/tmp/t_$$.sql" )
   || ( ( print "<span id=red>Error writing sql file ($DBI::errstr).</span>" )
        && return );
 print F qq{set arraysize 1000
set long 1000
truncate table qa_diff_adjustment;
truncate table mid_qa_history;
truncate table mid_qa_results;
truncate table mid_qa_history;
truncate table src_qa_queries;
truncate table qa_adjustment;
truncate table editing_matrix;
copy from $userpass\@$db to $userpass\@$mrd_db append qa_diff_adjustment using select * from qa_diff_adjustment
copy from $userpass\@$db to $userpass\@$mrd_db append qa_adjustment using select * from qa_adjustment
copy from $userpass\@$db to $userpass\@$mrd_db append mid_qa_history using select * from mid_qa_history
copy from $userpass\@$db to $userpass\@$mrd_db append mid_qa_results using select * from mid_qa_results
copy from $userpass\@$db to $userpass\@$mrd_db append mid_qa_queries using select * from mid_qa_queries
copy from $userpass\@$db to $userpass\@$mrd_db append src_qa_results using select * from src_qa_results
copy from $userpass\@$db to $userpass\@$mrd_db append src_qa_queries using select * from src_qa_queries
copy from $userpass\@$db to $userpass\@$mrd_db append editing_matrix using select * from editing_matrix
};
 close(F);
 print "<pre>";
 open( CMD,
       "$ENV{ORACLE_HOME}/bin/sqlplus $userpass\@$mrd_db < /tmp/t_$$.sql |" )
   || (    ( print qq{ <span id=red>sqlplus error ($?, $!)</span>} )
        && ( unlink "/tmp/t_$$.sql" )
        && return );
 while (<CMD>) { s/\n/<br>/; print; }
 close(CMD);
 print "</pre>";
 print qq{
     Tables copied successfully from <b>$db</b> to <b>$mrd_db</b>.
};
 unlink "/tmp/t_$$.sql";
}
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
 my ($type) = @_;
 print
   "Release $release: version $version, $version_date ($version_authority).\n"
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
 This script is a CGI script used to access the Automated QA system.  It takes
CGI arguments in the standard form "key=value&key=value...". 

 Parmaters:

  Version: $version, $version_date ($version_authority)

};
}    # End Procedure PrintHelp
