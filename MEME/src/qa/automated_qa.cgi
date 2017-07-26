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
$release = "3";
$version = "2.2";
$version_authority = "BAC";
$version_date = "05/16/2005";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

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
		  "state" => 1, "log_name" => 1, "command" => 1, 
		  "adjustment" => 1, "adjustment_dsc" => 1, "db" => 1,
		  "meme_home" => 1, "qa_id" => 1, "qa_name" => 1,
		  "qa_value" => 1, "qa_id_1" => 1, "qa_id_2" => 1,
		  "name" => 1, "query" => 1, "source" => 1, "oracle_home" => 1);

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
$ENV{"ORACLE_HOME"} = $oracle_home || $ENV{"ORACLE_HOME"} || die "\$ORACLE_HOME must be set.";
$state = "CHECK_JAVASCRIPT" unless $state;


#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MID, current MID
# 
$db = &meme_utils::midsvcs("editing-db") unless $db;
$date = `/bin/date +%Y%m%d`;
$cgi = $ENV{"SCRIPT_NAME"};
$start_time = time;
$style_sheet = &meme_utils::getStyle;

$FATAL = 1;

#
# Open Database Connection
#
# set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

# open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

# set sort and hash areas
    $dbh->do("alter session set sort_area_size=67108864");
    $dbh->do("alter session set hash_area_size=67108864");

#
# Determine Which action to run, print the body
# Valid STATES:
#
# 0. CHECK_JAVASCRIPT.  Verify that javascript is enabled and redirect
#    the page.  If not print a message indiciating that JavaScript must
#    be enabled.
# 1. INDEX. Print the index page
# 2. MONSTER_QA_DONE. The monster QA procedure is done. 
# 3. SRC_MID_DIFF. Difference in SRC-MID counts.
# 3b. SRC_MID_DIFF_DONE.  Submitted changes for SRC_MID_DFF
# 4. MID_MID_DIFF. Difference in MID-MID counts.
# 4b. MID_MID_DIFF_DONE.  Submitted changes for MID_MID_DFF
# 5. MID_MID_DIFF_CHOOSE. Select old MID snapshot to compare with current MID. 
# 6. SRC_OBSOLETE_DIFF. Difference in SRC-OBSOLETE counts.
# 6b. SRC_OBSOLETE_DIFF_DONE.  Submitted changes for SRC_OBSOLETE_DFF
# 7. REVIEW_ADJUSTMENTS. Review current adjustments.
# 7b. REVIEW_ADJUSTMENTS_DONE. Submitted changes to current adjustments.
# 8. REVIEW_COUNTS. Review the raw counts.
# 9. QUERIES. Modify,delete or insert SRC or MID queries.
# 10. QUERIES_DONE. Perform editing changes.

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	     "CHECK_JAVASCRIPT" => 
	         ["PrintHeader","PrintCHECK_JAVASCRIPT","PrintFooter","Check JavaScript","This page will redirect if JavaScript is enabled."],
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","Monster QA - Index Page","Automated QA"],
	     "MONSTER_QA_DONE" => 
	         ["PrintHeader","PrintMONSTER_QA_DONE","PrintFooter", "Monster QA Done","Monster QA Done"],
	     "SRC_MID_DIFF" => 
	         ["PrintHeader","PrintSRC_MID_DIFF","PrintFooter","Difference in SRC-MID counts","Difference in SRC-MID counts"],
	     "SRC_MID_DIFF_DONE" => 
	         ["PrintHeader","PrintSRC_MID_DIFF_DONE","PrintFooter","Adjustments for SRC-MID Differences","Adjustments for SRC-MID Differences"],
	     "SRC_OBSOLETE_DIFF" => 
	         ["PrintHeader","PrintSRC_OBSOLETE_DIFF","PrintFooter","Difference in SRC-SRC (obsolete) counts","Difference in SRC-SRC (obsolete) counts"],
	     "SRC_OBSOLETE_DIFF_DONE" => 
	         ["PrintHeader","PrintSRC_OBSOLETE_DIFF_DONE","PrintFooter","Adjustments for SRC-SRC (obsolete) Differences","Adjustments for SRC-SRC (obsolete) Differences"],
	     "MID_MID_DIFF" => 
	         ["PrintHeader","PrintMID_MID_DIFF","PrintFooter","Difference in MID-MID counts","Difference in MID-MID counts"],
	     "MID_MID_DIFF_DONE" => 
	         ["PrintHeader","PrintMID_MID_DIFF_DONE","PrintFooter","Adjustments for MID-MID Differences","Adjustments for MID-MID Differences"],
	     "MID_MID_DIFF_CHOOSE" => 
	         ["PrintHeader","PrintMID_MID_DIFF_CHOOSE","PrintFooter","Select old MID snapshot to compare with current MID","Select an old MID snapshot"],
	     "REVIEW_ADJUSTMENTS" => 
	         ["PrintHeader","PrintREVIEW_ADJUSTMENTS","PrintFooter","Review Current QA Adjustments","Review Current QA Adjustments"],
	     "REVIEW_ADJUSTMENTS_DONE" => 
	         ["PrintHeader","PrintREVIEW_ADJUSTMENTS_DONE","PrintFooter","Edit Current QA Adjustment","Edit Current QA Adjustment"],
	     "REVIEW_COUNTS" => 
	         ["PrintHeader","PrintREVIEW_COUNTS","PrintFooter","Review MID Counts","Review MID Counts"],
	     "QUERIES" => 
	         ["PrintHeader","PrintQUERIES","PrintFooter","Edit $command Queries","Edit $command Queries"],
	     "QUERIES_DONE" => 
	         ["PrintHeader","PrintQUERIES_DONE","PrintFooter","Done Editing $command Queries","Done Editing $command Queries"],
	     "MRD_COPY" => 
	         ["PrintHeader","PrintMRD_COPY","PrintFooter","Copy Counts to MRD","Copy Counts to MRD"],
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

    &$header($states{$state}->[3],$states{$state}->[4]);
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

    my($title,$header) = @_;

    &meme_utils::PrintSimpleHeader(
	   $title,$style_sheet,&HeaderJavascript,"<h2><center>$header</center></h2>");

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
	      <address><a href="$cgi?state=INDEX&db=$db" onMouseOver="window.status='Return to index page.'; return true;" onMouseOut="window.status=''; return true;">Back to Index</a></address>
            </td>
	    <td ALIGN=RIGHT VALIGN=TOP NOSAVE>
	      <font size="-1"><address>Contact: <a href="mailto:carlsen\@apelon.com">Brian A. Carlsen</a></address>
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
}; # end printCHECK_JAVASCRIPT


###################### Procedure PrintINDEX ######################
#
# This procedure prints the index page for the application with
# links to the other functionality
#
sub PrintINDEX {
    $selected{$db}="SELECTED";
    $dbs = &meme_utils::midsvcs("databases");
    $mrd = &meme_utils::midsvcs("mrd-db");
    $dbs = "$dbs,$mrd";
    $db_select_form = qq{
                                     <form action="$cgi">
			               <input type="hidden" name="state" value="INDEX">
			               <select name="db" onChange="this.form.submit(); return true;">
			                  <option>-- SELECT A DATABASE --</option>\n};
    foreach $db (sort split /,/, $dbs) {
      $db_select_form .= "			                  <option $selected{$db}>$db</option>\n";
      $databases{$db} = $db;
    }
    $db_select_form .= "			                </select></form>\n";


    #
    # Look up previous sources for select list
    #
    $prev_select = qq{<select name="source" onChange="confirm('This process may take up to a minute.\\nAre you sure you want to do this now?') && this.form.submit();">
  <option value="">-- Select a Source --</option>
  <option value="">All Sources</option>
};
    
    $sh = $dbh->prepare( qq{
        SELECT distinct previous_name,current_name
	FROM source_version WHERE previous_name IS NOT NULL
			  AND current_name IS NOT NULL
	ORDER BY 2
   }) || ((print "<span id=red>Error looking up previous names.</span>")
	  &&  return);
    $sh->execute || ((print "<span id=red>Error executing query to look up prev names.</span>") && return);
    while (($prev_name,$cur_name) = $sh->fetchrow_array) {
      $prev_select .= qq{  <option value="$prev_name">$cur_name</option>\n};
    }
    $prev_select .= qq{</select>};
    
    print qq{
<i>Select from one of these choices:</i>
<br>&nbsp;
<center><table WIDTH="90%" NOSAVE >
<tr nosave>
<td width="25%">Change Database:</td><td>$db_select_form</td>
</tr>
<tr><td colspan="2">&nbsp;</td></tr>
<tr><th colspan="2">Generate Counts</th></tr>
<tr NOSAVE>
<td colspan="2" NOSAVE><b><a href="$cgi?state=MONSTER_QA_DONE&db=$db" onMouseOver='window.status="Run Script"; return true;' onMouseOut='window.status=""; return true;' onClick="return confirm('Running the Monster QA script takes about 5 minutes.\\nAre you sure you want to do this now?');">Run Monster QA</a></b></td>
</tr>
<tr><td colspan="2">&nbsp;</td></tr>
<tr><th colspan="2">Compare Counts</th></tr>
<tr>
<td colspan="2"><b><a href="$cgi?state=SRC_MID_DIFF&db=$db" onMouseOver='window.status="SRC-MID Diff"; return true;' onMouseOut='window.status=""; return true;' onClick="return confirm('It is necessary for the Monster QA Procedure to have been completed before running this procedure.\\nAre you sure you want to do this now?');">Compare SRC-MID Snapshots</a></b></td>
</tr>

<tr NOSAVE>
<td colspan="2" NOSAVE><b><a href="$cgi?state=MID_MID_DIFF_CHOOSE&db=$db" onMouseOver='window.status="MID_MID_DIFF_CHOOSE"; return true;' onMouseOut='window.status=""; return true;'>Compare MID-MID Snapshots</a></b></td>
</tr>

<tr>
<form name="thisform" action="$cgi">
 <input type="hidden" name="db" value="$db">
 <input type="hidden" name="state" value="SRC_OBSOLETE_DIFF">
<td colspan="2"><b>Compare SRC-SRC (obsolete) Snapshots&nbsp;&nbsp;$prev_select</b></td>
</form>
</tr>

<tr><td colspan="2">&nbsp;</td></tr>
<tr><th colspan="2">Review Adjustments and Counts</th></tr>
<tr>
<td colspan="2"><b><a href="$cgi?state=REVIEW_ADJUSTMENTS&db=$db" onMouseOver='window.status="Review Current QA Adjustments"; return true;' onMouseOut='window.status=""; return true;'>Review Current QA Adjustments</a></b></td>
</tr>

<tr>
<td colspan="2"><b><a href="$cgi?state=REVIEW_COUNTS&db=$db" onMouseOver='window.status="Review MID Counts"; return true;' onMouseOut='window.status=""; return true;'>Review MID Counts</a></b></td>
</tr>

<tr><td colspan="2">&nbsp;</td></tr>
<tr><th colspan="2">Review and Edit Queries</th></tr>
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
}; # end PrintINDEX

###################### Procedure PrintMONSTER_QA_DONE ######################
#
# This procedure prints a page that indicates that the monster QA script
# is done. 
#

sub PrintMONSTER_QA_DONE {

    $dbh-> do( qq{
	BEGIN MEME_INTEGRITY.monster_qa; END;
    });

    if ($DBI::err) {
        ((print "<span id=red>The Monster QA procedure failed to run.</span>")
             &&  return);
    }    

	print qq{
	    The Monster QA procedure completed successfully.
	    };

}; # end PrintMONSTER_QA_DONE

###################### Procedure PrintSRC_MID_DIFF ######################
#
# This procedure prints a page that shows the difference between
# SRC and MID counts
#

sub PrintSRC_MID_DIFF {

    $dbh-> do( qq{
	BEGIN MEME_INTEGRITY.src_mid_qa_diff; END;
    });

    if ($DBI::err) {
        ((print "<span id=red>The SRC-MID QA procedure failed to run.</span>")
             &&  return);
    }    

    $sh = $dbh->prepare( qq{
	SELECT name, value, qa_id_1, count_1, count_2, count_2-count_1
  	FROM qa_diff_results order by name,value
    }) || ((print "<span id=red>Error reading from qa_diff_results.</span>")
             &&  return);

    $sh->execute || ((print "<span id=red>Error reading from qa_diff_results.</span>") && return);

    print qq{
<p><i>Fill out adjustment reasons and edit the adjustment count if necessary. Then click "Submit Adjustments" at the bottom of the page.<br>
[Note: The counts in parenthesis are (SRC,MID) counts]
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
    <input type="hidden" name="state" value="SRC_MID_DIFF_DONE">
    <input type="hidden" name="db" value="$db">
};
    $prev = "";
    $found =0;
    while (($name,$value,$id, $srcct, $midct,$diff) = $sh->fetchrow_array) {
	$qa_id = "$id";
	$found=1;
	if ($prev ne $name) {
	    print qq{
  <tr><th colspan="3" align=left>Test Name: $name</th></tr>
  };
	};
	$prev = $name;

	print qq{
  <tr><td>
      <input type="hidden" name="qa_name" value="$name">
      <input type="hidden" name="qa_value" value="$value">
      $value ($srcct, $midct)
  </td>
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
<tr><td colspan="3">&nbsp</td></tr>
<tr><td colspan="3">
    <input type="hidden" name="qa_id" value="$qa_id">
<center><input type="submit" value="Submit Adjustments"></center>
</td></tr>
};
    } else {
	print qq{
	    <tr><td>There are no differences between the SRC and MID
	        counts.</td></tr>
};
    };

    print qq{
</table></center>
<p>
}
}; # end PrintSRC_MID_DIFF


###################### Procedure PrintSRC_MID_DIFF_DONE ######################
#
# This procedure adds the adjustments to the qa_adjustment table
#

sub PrintSRC_MID_DIFF_DONE {

    # use @qa_name, @qa_value, @qa_id, @adjustment, and @adjustmen_dsc
    # to insert rows into qa_adjustment.  where the adjustment_dsc exists

    $sh = $dbh->prepare ( qq{
	insert into qa_adjustment (qa_id, name, value, qa_count,
				   timestamp, description)
	    values ( $qa_id, ?, ?, ?, SYSDATE, ? )
	} ) ||  ((print "<span id=red>Error preparing insert stmt.</span>")
             &&  return);

    $ct=0;
    for ($i=0; $i <= $#qa_name; $i++) {
#	print "$qa_name[$i]: $adjustment_dsc[$i], $adjustment[$i]<br>\n";
	if ($adjustment_dsc[$i] && $adjustment[$i]) {
	    $sh->execute($qa_name[$i], $qa_value[$i],
			 $adjustment[$i], $adjustment_dsc[$i]) ||
	    ((print "<span id=red>Error adding SRC-MID adjustment  ($qa_name[$i], $qa_value[$i],$adjustment[$i], $adjustment_dsc[$i], $DBI::errstr).</span>")
             &&  return);     
	    $ct++;
	};
    }
    print qq{
	$ct adjustments were added.  Go back to the index page and
	compare SRC-MID counts again to see if any differences remain.
	};


}; # end PrintSRC_MID_DIFF_DONE

###################### Procedure PrintSRC_OBSOLETE_DIFF ######################
#
# This procedure prints a page that shows the difference between
# SRC and SRC (obsolete) counts
#
sub PrintSRC_OBSOLETE_DIFF {

    $dbh-> do( qq{
	BEGIN MEME_INTEGRITY.src_obsolete_qa_diff; END;
    });

    if ($DBI::err) {
        ((print "<span id=red>The SRC-SRC (obsolete) QA procedure failed to run.</span>")
             &&  return);
    }    

    $sh = $dbh->prepare( qq{
	SELECT name, value, qa_id_1, count_1, count_2, count_2-count_1,
	       replace(value,previous_name,b.source)
  	FROM qa_diff_results a, source_version b
        WHERE value like previous_name || '%'
        AND b.previous_name like '$source%'
	AND previous_name IS NOT NULL
	AND current_name IS NOT NULL
	AND count_2!=count_1
        ORDER BY name, value
    }) || ((print "<span id=red>Error reading from qa_diff_results.</span>")
             &&  return);

    $sh->execute || ((print "<span id=red>Error reading from qa_diff_results.</span>") && return);

    print qq{
<p><i>Fill out adjustment reasons and edit the adjustment count if necessary. Then click "Submit Adjustments" at the bottom of the page.<br>
[Note: The counts in parenthesis are (obsolete, current)]
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
    $prev = "";
    $found =0;
    #
    # Here use the $qa_id (negative #)
    #
    while (($name,$value,$id, $srcct, $midct,$diff,$stripped_value) = $sh->fetchrow_array) {
	$qa_id = $id;
	next unless $diff;
	$found=1;
	if ($prev ne $name) {
	    print qq{
  <tr><th colspan="3" align=left>Test Name: $name</th></tr>
  };
	};
	$prev = $name;

	print qq{
  <tr><td>
      <input type="hidden" name="qa_name" value="$name">
      <input type="hidden" name="qa_value" value="$value">
      $stripped_value ($srcct, $midct)
  </td>
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
<tr><td colspan="3">&nbsp</td></tr>
<tr><td colspan="3">
    <input type="hidden" name="qa_id" value="$qa_id">
<center><input type="submit" value="Submit Adjustments"></center>
</td></tr>
};
    } else {
	print qq{
	    <tr><td>There are no differences between the SRC and SRC (obsolete)
	        counts.</td></tr>
};
    };

    print qq{
</form>
</table></center>
<p>
}
}; # end PrintSRC_OBSOLETE_DIFF


###################### Procedure PrintSRC_OBSOLETE_DIFF_DONE ######################
#
# This procedure adds the adjustments to the qa_diff_adjustment table
#
sub PrintSRC_OBSOLETE_DIFF_DONE {

    # use @qa_name, @qa_value, @qa_id, @adjustment, and @adjustmen_dsc
    # to insert rows into qa_diff_adjustment.  where the adjustment_dsc exists

    $sh = $dbh->prepare ( qq{
	insert into qa_diff_adjustment 
	  (qa_id_1, qa_id_2, name, value, 
	   qa_count, timestamp, description)
	    values ( $qa_id, $qa_id, ?, ?, ?, SYSDATE, ? )
	} ) ||  ((print "<span id=red>Error preparing insert stmt.</span>")
             &&  return);
    $ct=0;
    for ($i=0; $i <= $#qa_name; $i++) {
	#print "$qa_name[$i]: $adjustment_dsc[$i], $adjustment[$i]<br>\n";
	if ($adjustment_dsc[$i] && $adjustment[$i]) {
	    $sh->execute($qa_name[$i], $qa_value[$i],
			 $adjustment[$i], $adjustment_dsc[$i]) ||
	    ((print "<span id=red>Error adding SRC-SRC (obsolete) adjustment  ($qa_id, $qa_name[$i], $qa_value[$i],$adjustment[$i], $adjustment_dsc[$i], $DBI::errstr).</span>")
             &&  return);     
	    $ct++;
	};
    }
    print qq{
	$ct adjustments were added.  Go back to the index page and
	compare SRC-SRC (obsolete) counts again to see if any differences remain.
	};


}; # end PrintSRC_OBSOLETE_DIFF_DONE

###################### Procedure PrintMID_MID_DIFF ######################
#
# This procedure prints a page that shows the difference between
# an old MID snapshot and current MID counts
#
 
#
sub PrintMID_MID_DIFF {

    $dbh-> do( qq{
	BEGIN MEME_INTEGRITY.mid_mid_qa_diff(?); END;
    }, undef, $qa_id);

    if ($DBI::err) {
        ((print "<span id=red>The MID-MID QA procedure failed to run ($DBI::errstr).</span>")
             &&  return);
    }    

    $sh = $dbh->prepare( qq{
	SELECT name, value, qa_id_1, qa_id_2, count_1, count_2, count_2-count_1
  	FROM qa_diff_results order by name,value
    }) || ((print "<span id=red>Error reading from qa_diff_results.</span>")
             &&  return);

    $sh->execute || ((print "<span id=red>Error reading from qa_diff_results.</span>") && return);

    print qq{
<i>Fill out adjustment reasons and edit the adjustment count if necessary. Then click "Submit Adjustments" at the bottom of the page.  
<p>[Note: The counts in parenthesis are (current MID,historical MID) counts]</p>
</i>
<center><table border=0 width="90%">
<form method="POST" action="$cgi">
    <input type="hidden" name="state" value="MID_MID_DIFF_DONE">
    <input type="hidden" name="db" value="$db">
};
    $prev = "";
    $found =0;
    while (($name,$value,$qa_id_1,$qa_id_2,$midct,$historyct,$diff) = $sh->fetchrow_array) {
	$id_1 = "$qa_id_1";
	$id_2 = "$qa_id_2";
	$found=1;
	if ($prev ne $name) {
	    print qq{
  <tr><th colspan="3" align=left>Test Name: $name</th></tr>
  };
	};
	$prev = $name;

	print qq{
  <tr><td>
      <input type="hidden" name="qa_name" value="$name">
      <input type="hidden" name="qa_value" value="$value">
      $value ($midct,$historyct)
  </td>
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
<tr><td colspan="3">&nbsp</td></tr>
<tr><td colspan="3">
    <input type="hidden" name="qa_id_1" value="$id_1">
    <input type="hidden" name="qa_id_2" value="$id_2">
<center><input type="submit" value="Submit Adjustments"></center>
</td></tr>
};
    } else {
	print qq{
	    <tr><td>There are no differences between the current MID and the historical MID ($qa_id) counts.</td></tr>
};
    };

    print qq{
</form>
</table></center>
<p>
}

}; # end PrintMID_MID_DIFF

###################### Procedure PrintMID_MID_DIFF_DONE ####################
#
# This procedure adds the adjustments to the qa_diff_adjustment table
#

sub PrintMID_MID_DIFF_DONE {
    # use @qa_name, @qa_value, @adjustment, and @adjustmen_dsc
    # to insert rows into qa_adjustment.  where the adjustment_dsc exists

   $sh = $dbh->prepare ( qq{
	insert into qa_diff_adjustment (qa_id_1, qa_id_2, name, value, qa_count, timestamp, description)
	    values ( $qa_id_1, $qa_id_2, ?, ?, ?, SYSDATE, ? )
	} ) ||  ((print "<span id=red>Error preparing insert stmt.</span>")
             &&  return);

    $ct=0;
    for ($i=0; $i <= $#qa_name; $i++) {
	if ($adjustment_dsc[$i] && $adjustment[$i]) {
	    $sh->execute($qa_name[$i], $qa_value[$i],
			 $adjustment[$i], $adjustment_dsc[$i]) ||
	    ((print "<span id=red>Error adding SRC-MID adjustment  ($qa_name[$i], $qa_value[$i],$adjustment[$i], $adjustment_dsc[$i], $DBI::errstr).</span>")
             &&  return);     
	    $ct++;
	};
    }
    print qq{
	$ct adjustments were added.  Go back to the index page and
	compare MID(current)-MID(historic) counts again to see if any differences remain.
	};

}; # end PrintMID_MID_DIFF_DONE

###################### Procedure PrintMID_MID_DIFF_CHOOSE ####################
#
# This procedure prints a page that offers you a list of old 
# MID snapshots.  From this list, one can compare old MID counts
# to current counts.
#

sub PrintMID_MID_DIFF_CHOOSE {

	$sh = $dbh->prepare( qq{
        SELECT DISTINCT qa_id, timestamp
	FROM mid_qa_history ORDER BY 2 desc
	}) || ((print "<span id=red>Error reading from mid_qa_history.</span>")
		       &&  return);

	$sh->execute || ((print "<span id=red>Error reading from mid_qa_history table.</span>") && return);

	print qq{
<i>Choose one of the following historical MID snapshots to compare to the current MID snapshot.</i><p>
<blockquote>
  <form action="$cgi" method="GET"> 
    <input type="hidden" name="state" value="MID_MID_DIFF">
    <input type="hidden" name="db" value="$db">
    <select name="qa_id" onChange="this.form.submit(); return true;">
       <option>---- SELECT ONE ----</option>
};
	while (($qa_id,$timestamp) = $sh->fetchrow_array) {
	    print qq{        <option value="$qa_id">$timestamp</option>\n};
	}#end while

	print qq{
    </select>
   </form>
</blockquote>
	    };
}; # end PrintMID_MID_DIFF_CHOOSE

###################### Procedure PrintREVIEW_ADJUSTMENTS ######################
#
# This procedure prints a page that allows one to view current
# Monster QA adjustments.
#

sub PrintREVIEW_ADJUSTMENTS {

  #
  # Look up previous sources
  #
  $prev_select = qq{<select name="source" onChange="this.form.submit()">
  <option value="">-- Select a Source --</option>
  <option value="">All Sources</exioption>
};
  
  $sh = $dbh->prepare( qq{
        SELECT distinct previous_name, current_name
	FROM source_version WHERE previous_name IS NOT NULL
			  AND current_name IS NOT NULL
	ORDER BY 2
	}) || ((print "<span id=red>Error looking up previous names.</span>")
		       &&  return);
  $sh->execute || ((print "<span id=red>Error executing query to look up prev names.</span>") && return);
  while (($prev_name,$cur_name) = $sh->fetchrow_array) {
    $prev_select .= qq{  <option value="$prev_name">$cur_name - $prev_name</option>\n};
  }
  $prev_select .= qq{</select>};

    unless ($command) {
	print qq{
    <i>Select one of the following adjustment sets to review</i>
<form name="thisform" action="$cgi">
 <input type="hidden" name="db" value="$db">
 <input type="hidden" name="command" value="SRCSRC">
 <input type="hidden" name="state" value="$state">
    <ul><li><a href="$cgi?state=$state&command=SRC&db=$db" onMouseOver="window.status='Review SRC adjustment counts'; return true;" onMouseOut="window.status=''; return true;"><b>Review SRC adjustment counts</b></a>.</li><br>
    <li><a href="$cgi?state=$state&command=MID&db=$db" onMouseOver="window.status='Review MID adjustment counts'; return true;" onMouseOut="window.status=''; return true;"><b>Review MID-MID adjustment counts</b></a>.</li><br>
    <li><b>Review SRC-SRC (obsolete) adjustment counts</b></a>&nbsp;&nbsp;$prev_select.</li></ul>
</form>
    }; 
    };


    if ($command eq "SRC") {

	#
	# Look up adjustments, group them by name
	#
	$sh = $dbh->prepare( qq{
        SELECT name, value, qa_count, timestamp, description
	FROM qa_adjustment order by name asc, timestamp desc
	}) || ((print "<span id=red>Error reading from qa_adjustment.</span>")
		       &&  return);

	$sh->execute || ((print "<span id=red>Error reading from adjustment table.</span>") && return);

	print qq{
<i>Following are the adjustments that apply to the SRC data when comparing it against the MID.</i><p>
<center><table border=0 width="90%">
};
	$prev = "";
	$found =0;
	while (($name,$value,$ct, $timestamp, $description) = $sh->fetchrow_array) {
	    $found=1;
	    if ($prev ne $name) {
		print qq{
  <tr><th colspan="4" align=left>Test Name: $name</th></tr>
  };
	};
	    $prev = $name;

	    # format table better
	    $timestamp =~ s/ /&nbsp;/g;
	    print qq{
  <tr><td>$value</td>
  <td align="left" >$ct</td>
  <td align="left">$timestamp</td>
  <td>$description</td></tr>
};	 
	}
	print qq{
</table></center>
<p>
};
    } # if command=SRC

    if ($command eq "MID") {
	#
	# Look up adjustments, group them by name
	#
	$sh = $dbh->prepare( qq{
        SELECT MIN (qa_id)
	FROM mid_qa_results
	}) || ((print "<span id=red>Error reading from mid_qa_results.</span>")
		       &&  return);

	$sh->execute || ((print "<span id=red>Error reading from MID_QA_RESULTS table.</span>") && return);
	($qa_id)=$sh->fetchrow_array;

	$sh = $dbh->prepare( qq{
        SELECT qa_id_1, name, value, qa_count, timestamp, description
	FROM qa_diff_adjustment WHERE qa_id_2=?
	UNION
        SELECT qa_id_2, name, value, qa_count, timestamp, description
	FROM qa_diff_adjustment WHERE qa_id_1=?
	ORDER BY name asc, timestamp desc
	}) || ((print "<span id=red>Error reading from qa_diff_adjustment.</span>")
		       &&  return);

	$sh->execute($qa_id, $qa_id) || ((print "<span id=red>Error reading from qa_diff_adjustment table.</span>") && return);

	print qq{
<i>Following are the adjustment counts between the most recent MID
   snapshot and historical snapshots it has been compared to.</i><p>
<center><table border=0 width="90%">
};
	$prev = "";
	$found =0;
	while (($qa_id_other,$name,$value,$ct, $timestamp, $description) = $sh->fetchrow_array) {
	    $found=1;
	    if ($prev ne $name) {
		print qq{
  <tr><th colspan="5" align=left>Test Name: $name</th></tr>
  };
	};
	    $prev = $name;
	    $timestamp =~ s/ /&nbsp;/g;
	    print qq{
  <tr><td width="5%">$qa_id_other</td>
  <td>$value</td>
  <td align="left">$ct</td>
  <td align="left">$timestamp</td>
  <td>$description</td></tr>
};	 
	}
	
	unless ($found) {
	    print qq{No adjustments found.}
	}

	print qq{
</table></center>
<p>
};
	
      }

    if ($command eq "SRCSRC") {
	#
	# Look up adjustments, group them by name
	#
	$sh = $dbh->prepare( qq{
        SELECT MIN (qa_id)
	FROM src_obsolete_qa_results
	}) || ((print "<span id=red>Error reading from src_obsolete_qa_results.</span>")
		       &&  return);

	$sh->execute || ((print "<span id=red>Error reading from SRC_OBSOLETE_QA_RESULTS table.</span>") && return);
	($qa_id)=$sh->fetchrow_array;

	$sh = $dbh->prepare( qq{
        SELECT qa_id_1, name, value, qa_count, timestamp, description
	FROM qa_diff_adjustment WHERE qa_id_2=$qa_id AND qa_id_1=qa_id_2
        AND value like '%$source%'			       
	ORDER BY name asc, timestamp desc
	}) || ((print "<span id=red>Error reading from qa_diff_adjustment.</span>")
		       &&  return);

	$sh->execute || ((print "<span id=red>Error reading from qa_diff_adjustment table.</span>") && return);

	print qq{
<i>Following are the adjustment counts between the most recent SRC snapshot and the obsolete SRC snapshot.</i><p>
<center><table border=0 width="90%">
};
	$prev = "";
	$found =0;
	while (($qa_id_other,$name,$value,$ct, $timestamp, $description) = $sh->fetchrow_array) {
	    $found=1;
	    if ($prev ne $name) {
		print qq{
  <tr><th colspan="6" align=left>Test Name: $name</th></tr>
  };
	};
	    $prev = $name;
	    $timestamp =~ s/ /&nbsp;/g;
	    print qq{
  <form action="$cgi">
    <input type="hidden" name="db" value="$db">
    <input type="hidden" name="command" value="">
    <input type="hidden" name="state" value="${state}_DONE">
    <input type="hidden" name="qa_id_1" value="$qa_id_other">
    <input type="hidden" name="qa_id_2" value="$qa_id_other">
    <input type="hidden" name="qa_name" value="$name">
    <input type="hidden" name="qa_value" value="$value">
  <tr> 
  <td>$qa_id_other</td>
  <td>$value</td>
  <td>$timestamp</td>
  <td align="left"><input type="text" name="adjustment" value="$ct" size="5"></td>
  <td align="left"><input type="text" name="adjustment_dsc" value="$description" size="60"></td>
  <td><input type="button" value="Modify" onClick="this.form.command.value='MODIFY'; this.form.submit();return true;">&nbsp;&nbsp;
      <input type="button" value="Delete" onClick="this.form.command.value='DELETE'; this.form.submit();return true;"></td>
  </tr>
  </form>
  
};	 
	}
	
	unless ($found) {
	    print qq{No adjustments found.}
	}

	print qq{
</table></center>
<p>
};
      }	
    
}; # end PrintREVIEW_ADJUSTMENTS


###################### Procedure PrintREVIEW_ADJUSTMENTS_DONE ######################
#
# Submit a change to an adjustment
#

sub PrintREVIEW_ADJUSTMENTS_DONE {

  #
  # HANDLE the qa_diff_adjustment case
  #
  
  if ($qa_id_1) {
	#
  	# Delete adjustment (for either DELETE or MODIFY)
  	#
  	$rc = $dbh->do( qq{
        DELETE FROM qa_diff_adjustment
        WHERE qa_id_1 = ?
          AND qa_id_2 = ?
          AND name = ?
          AND value = ?
	}, undef, $qa_id1, $qa_id2, $qa_name, $qa_value) || ((print "<span id=red>Error deleting from qa_diff_adjustment.</span>")
		       &&  return);

  	print qq{QA adjustments matching:<ul><li>QA_ID_1 = $qa_id_1</li><li>QA_ID_2 = $qa_id_2</li><li>NAME = $qa_name</li><li>VALUE = $qa_value</li></ul>were deleted ($rc rows).<br>};
  
  	if ($command eq "MODIFY") {
      $dbh->do( qq{
        INSERT INTO qa_diff_adjustment 
          (qa_id_1, qa_id_2, name, value, qa_count, timestamp, description)
        VALUES
          (?, ?, ?, ?, ?, SYSDATE, ?)
	  }, undef, $qa_id_1, $qa_id_2, $qa_name, $qa_value, $adjustment, $adjustment_dsc) || ((print "<span id=red>Error inserting into qa_diff_adjustment.</span>")
		       &&  return);
	  print qq{<br>QA adjustment:<ul><li>QA_ID_1 = $qa_id_1</li><li>QA_ID_2 = $qa_id_2</li>
	    <li>NAME = $qa_name</li><li>VALUE = $qa_value</li>
	    <li>QA_COUNT = $adjustment</li><li>DESCRIPTION = $adjustment_dsc</li></ul>was inserted.<br>};
  	}
  }
  
  elsif ($qa_id) {
	#
  	# Delete adjustment (for either DELETE or MODIFY)
  	#
  	$rc = $dbh->do( qq{
        DELETE FROM qa_adjustment
        WHERE qa_id = ?
          AND name = ?
          AND value = ?
	}, undef, $qa_id, $qa_name, $qa_value) || ((print "<span id=red>Error deleting from qa_adjustment.</span>")
		       &&  return);

  	print qq{<br>QA adjustments matching:<ul><li>QA_ID = $qa_id</li><li>NAME = $qa_name</li><li>VALUE = $qa_value</li></ul>were deleted ($rc rows).<br>};
  
  	if ($command eq "MODIFY") {
      $dbh->do( qq{
        INSERT INTO qa_adjustment 
          (qa_id, name, value, qa_count, timestamp, description)
        VALUES
          (?, ?, ?, ?, SYSDATE, ?)
	  }, undef, $qa_id, $qa_name, $qa_value, $adjustment, $adjustment_dsc) || ((print "<span id=red>Error inserting into qa_adjustment.</span>")
		       &&  return);
	  print qq{QA adjustment:<ul><li>QA_ID = $qa_id</li>
	    <li>NAME = $qa_name</li><li>VALUE = $qa_value</li>
	    <li>QA_COUNT = $adjustment</li><li>DESCRIPTION = $adjustment_dsc</li></ul>was inserted.<br>};
  	}
  }
  
  print qq{<br><a href="javascript:history.go(-1)">Review/Edit Adjustments</a> (Click reload after going back)<br>};
    
}; # end PrintREVIEW_ADJUSTMENTS_DONE


###################### Procedure PrintREVIEW_COUNTS ######################
#
# This procedure prints a page that allows one to view current
# Monster QA MID Counts.
#

sub PrintREVIEW_COUNTS {

    # If the qa_id is not set
    # then present user with a choice

    unless ($qa_id) {

      $sh = $dbh->prepare( qq{
        SELECT qa_id, timestamp FROM
	(SELECT qa_id, timestamp FROM mid_qa_history
	 UNION SELECT qa_id, timestamp FROM mid_qa_results)
        ORDER BY 2 desc
	}) || ((print "<span id=red>Error reading from mid_qa_history.</span>")
		       &&  return);

	$sh->execute || ((print "<span id=red>Error reading from mid_qa_history table.</span>") && return);

	print qq{
<i>Choose one of the following MID snapshots.</i><p>
<blockquote>
  <form action="$cgi" method="GET"> 
    <input type="hidden" name="state" value="REVIEW_COUNTS">
    <input type="hidden" name="db" value="$db">
    <select name="qa_id" onChange="this.form.submit(); return true;">
       <option>---- SELECT ONE ----</option>
};
	while (($qa_id,$timestamp) = $sh->fetchrow_array) {
	    print qq{        <option value="$qa_id">$timestamp</option>\n};
	}#end while

	print qq{
    </select>
   </form>
</blockquote>
	    };
       return;
    };

    # if we get here, the $qa_id is set, show the raw counts

    #
    # Look up counts, group by name
    #
    $sh = $dbh->prepare( qq{
        SELECT qa_id, name, value, qa_count, timestamp
	FROM mid_qa_history where qa_id = ?
	UNION
        SELECT qa_id, name, value, qa_count, timestamp
	FROM mid_qa_results where qa_id = ?
	ORDER BY name asc, timestamp desc
	}) || ((print "<span id=red>Error reading from qa_diff_adjustment.</span>")
		       &&  return);

	$sh->execute($qa_id, $qa_id) || ((print "<span id=red>Error reading from qa_diff_adjustment table.</span>") && return);

	print qq{
<i>Following are the MID counts for the selected set.</i><p>
<center><table border=0 width="90%">
};
	$prev = "";
	$found =0;
	while (($qa_id,$name,$value, $ct, $timestamp) = $sh->fetchrow_array) {
	    $found=1;
	    if ($prev ne $name) {
		print qq{
  <tr><th colspan="2" align=left>Test Name: $name ($timestamp)</th></tr>
  };
	};
	    $prev = $name;
	    
	    $value =~ s/,/, /g;
	    $timestamp =~ s/ /&nbsp;/g;
	    print qq{
  <tr><td width="10%">$ct</td>
  <td><tt>$value</tt></td></tr>
};	 
	}
	
	unless ($found) {
	    print qq{No counts found.}
	}

	print qq{
</table></center>
<p>
};
	
}; # end PrintREVIEW_COUNTS

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
    
    $sh = $dbh->prepare( qq{
	SELECT name,query FROM ${command}_qa_queries
	}) || ((print "<span id=red>Error preparing query to select from ${command}_qa_queries ($DBI::errstr).</span>")
		       &&  return);

    $sh->execute|| ((print "<span id=red>Error selecting from ${command}_qa_queries ($DBI::errstr).</span>") && return);
    
    while (($name,$query)=$sh->fetchrow_array) {
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

}; # end PrintQUERIES

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
	|| ((print "<span id=red>Error truncating old ${command}_qa_queries data ($DBI::errstr).</span>") && return);
    
    $sh = $dbh->prepare ( qq{
	INSERT INTO ${command}_qa_queries (name, query) values (?,?) 
	}) || ((print "<span id=red>Error preparing query to replace old ${command}_qa_queries data ($DBI::errstr).</span>") && return);

    for ($i=0; $i <= $#name; $i++) {
	if ($name[$i] && $query[$i]) {
	    $sh->execute($name[$i],$query[$i])
	    || ((print "<span id=red>Error inserting ${command}_qa_queries data ($name[$i], $value[$i], $DBI::errstr).</span>") && return);
	}
    }
    $dbh->commit;

    print qq{
	<i>The ${command} queries were succesfully replaced.  
          The new data follows</i><p>
        <center><table width="90%" border="0">
    };

    for ($i=0; $i <= $#name; $i++) {
        if ($name[$i] && $query[$i]) {
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

}; # end PrintQUERIES_DONE

###################### Procedure PrintMRD_COPY ######################
#
# This procedure copies the mid_qa_results and mid_qa_history
# tables to mrd from the current db.
#
sub PrintMRD_COPY {
  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  chop($userpass);
  
  $mrd_db = &meme_utils::midsvcs("mrd-db");
  
  open (F,">/tmp/t_$$.sql") || 
    ((print "<span id=red>Error writing sql file ($DBI::errstr).</span>")
     &&  return);
  print F qq{set arraysize 1000
set long 1000
truncate table qa_diff_adjustment;
truncate table qa_diff_results;
truncate table mid_qa_history;
truncate table mid_qa_results;
truncate table mid_qa_history;
truncate table src_qa_queries;
truncate table qa_adjustment;
truncate table editing_matrix;
copy from $userpass\@$db to $userpass\@$mrd_db append qa_diff_adjustment using select * from qa_diff_adjustment
copy from $userpass\@$db to $userpass\@$mrd_db append qa_diff_results using select * from qa_diff_results
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
  open (CMD,"$ENV{ORACLE_HOME}/bin/sqlplus $userpass\@$mrd_db < /tmp/t_$$.sql |") ||
    ( (print qq{ <span id=red>sqlplus error ($?, $!)</span>} ) &&
      (unlink "/tmp/t_$$.sql") &&
      return);
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
 This script is a CGI script used to access the Automated QA system.  It takes
CGI arguments in the standard form "key=value&key=value...". 

 Parmaters:

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp
