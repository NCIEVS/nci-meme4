#!@PATH_TO_PERL@
#
# File:     status_n.cgi
# Author:   Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#    This CGI script investigates status N attached to
#    a concept id and produces a report
#
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
#
# Version info:
# 09/17/2002 3.1.0: First version
$release = "3";
$version = "1.0";
$version_authority = "RBE";
$version_date = "09/17/2002";

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
%meme_utils::validargs = 
  ( 
    "state" => 1, "command" => 1,
    "concept_id" => 1, "meme_home" => 1,
    "db" => 1, "oracle_home" => 1
    );

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
$ENV{"MEME_HOME"} = $meme_home || $ENV{"MEME_HOME"} || $inc;
$ENV{"ORACLE_HOME"} = $oracle_home || $ENV{"ORACLE_HOME"} || die "\$ORACLE_HOME must be set.";

%rel_name_map = 
  (
   'BT' => 'BRD',
   'LK' => 'LIK',
   'NT' => 'NRW',
   'RT' => 'REL',
   'RT?' => 'REL?',
   'XR' => 'NOT',
   'XS' => 'NSY',
   'SFO/LFO' => 'SFO',
   'AQ' => 'AQR',
   'QB' => 'QBR');

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
$db = &meme_utils::midsvcs("editing-db") unless $db;
$date = `/bin/date +%Y%m%d`;
$cgi = $ENV{"SCRIPT_NAME"};
$cgi_dir = $ENV{"SCRIPT_NAME"};
$cgi_dir =~ s/(.*)\/.*\.cgi/$1/;
$start_time = time;
$style_sheet = &meme_utils::getStyle;
$FATAL = 1;


#
# Determine Which action to run, print the body
# Valid STATES:
#
# 1. INDEX. Print the index page (a form accepting concept_id)
# 2. REPORT. Print the report, the results of the investigation

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","Status N Detective","Status N Detective"],
	     "REPORT" => 
	         ["PrintHeader","PrintREPORT","PrintFooter", "Status N Report","Report of Status N for: $concept_id"],
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
	function openDescription (check,dsc) {
	    var html = "<html><head><title>Description: "+check;
	    html = html + "</title></head><body bgcolor=#ffffff>" + dsc + "<center><form><input type=button onClick='window.close(); return true' value='Close'></form></center></body></html>";
	    var win = window.open("","","scrollbars,width=500,height=250,resizable");
	    win.document.open();
	    win.document.write(html);
	    win.document.close();
	}; // end openDescription
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
	      <address><a href="$cgi">Back to Index</a></address>
            </td>
	    <td ALIGN=RIGHT VALIGN=TOP NOSAVE>
	      <font size="-1"><address>Contact: <a href="mailto:bcarlsen\@msdinc.com">Brian A. Carlsen</a></address>
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
	    document.location='$cgi?state=INDEX';
	</script>
        <p><blockquote>
	    You must use a JavaScript enabled browser to run this
	    application (<a href="http://www.netscape.com">Netscape</a>
	    is recommended).  If you are using
	    a JavaScript enabled browser and just have JavaScript
	    disabled, please enable it and click
	    <a href="$cgi?state=INDEX">here</a>.
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
    $databases{"editing-db"} = &meme_utils::midsvcs("editing-db");
    $databases{"testsw-db"} = &meme_utils::midsvcs("testsw-db");
    $databases{"testsrc-db"} = &meme_utils::midsvcs("testsrc-db");

    print qq{
This tool is used to investigate the status N that a particular concept is involved in.  Please select the database you want and enter the concept ID.  Click <i>Submit</i> to generate the report.
    <FORM METHOD="GET" ACTION="$ENV{SCRIPT_NAME}"> 
    <INPUT TYPE="hidden" NAME="state" VALUE="REPORT">
    <CENTER>
        <TABLE WIDTH="90%" CELLSPACING="2" CELLPADDING="2">
        <TR>
            <TD ALIGN="left"><FONT SIZE=+1>Database:</FONT></TD>
            <TD ALIGN="left"><FONT SIZE=+1><SELECT NAME="db">
    };

    foreach $db (keys %databases) {
        print qq{         <OPTION VALUE="$databases{$db}"
            $selected{$databases{$db}}>$db ($databases{$db})</OPTION>
        };
    }

    print qq{
    </SELECT></FONT>
    </TD>
    </TR>
    <TR>
        <TD ALIGN="left"><FONT SIZE=+1>Concept ID:</FONT></TD>
        <TD ALIGN="left"><FONT SIZE=+1><INPUT TYPE="text" NAME="concept_id" VALUE="$concept_id" SIZE=12></FONT></TD>
    </TR>
    <TR>
        <TD COLSPAN="2" ALIGN="left">
        <INPUT TYPE="button" VALUE="Submit"
          onClick="submitForm(this.form);"
          onMouseOver="window.status='Send Database Request'; return true;"
          onMouseOut="window.status=''; return true;">
        <SCRIPT LANGUAGE = "JavaScript">
            function isDigit(c) {
                return (c >= 0 || c <= 9);
            }
            function submitForm(form) {
                form.submit();
                return true;
            }
        </SCRIPT>
        </FONT></TD>
    </TR>
    </TABLE>
    </CENTER>
    </FORM>
 };

}; # end PrintINDEX

###################### Procedure PrintREPORT ######################
#
# This procedure prints a report of the status N connected
# to the $concept_id.  If there none, or if the $concept_id is
# invalid, appropriate errors are reported.
#
sub PrintREPORT {

# set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
# open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
   
  #
  # Get Demotions connected to $concept_id
  #
  $row_count = 0;
 
  # prepare and execute statement
  $sh = $dbh->prepare(qq{
    SELECT relationship_id, atom_id_1, atom_id_2 FROM relationships
    WHERE status = 'D' AND concept_id_1 = ?
    UNION SELECT relationship_id, atom_id_2, atom_id_1 FROM relationships
    WHERE status = 'D' AND concept_id_2 = ?
     }) ||
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute($concept_id, $concept_id) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);


  #
  # Main Loop, print table for each case
  #
  while (($relationship_id, $atom_id_1, $atom_id_2) = $sh->fetchrow_array) {

    # First time through print a header
    unless ($row_count) {
      print qq{
<i>Following are the status N affecting this concept and (hopefully) an explanation of the circumstances behind each status N</i>.<p>
};
     }

    #
    # Get Demoted Atoms
    #
    $sh2 = $dbh->prepare(qq{
      SELECT a.atom_id,atom_name,termgroup,code, sui, lui, isui
      FROM classes a, atoms b WHERE a.atom_id=b.atom_id
	AND a.atom_id IN (?, ?)
     }) ||
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh2->execute($atom_id_1, $atom_id_2) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    while (($atom_id, $atom_name, $termgroup, $code, $sui, $lui, $isui) 
	   =$sh2->fetchrow_array) {
      $demotion_atoms{$atom_id}="<tt>$atom_name [$termgroup/$code/$atom_id]</tt>";
      $luis{$atom_id} = "$lui";
      $suis{$atom_id} = "$sui";
      $isuis{$atom_id} = "$isui";
      $codes{$atom_id} = "$code";
      $termgroup =~ /.*\/(.*)/;
      $ttys{$atom_id} = "$1";
    }
    
    #
    # Get matching details
    #
    $match_info = "<tt>";
    if ($isuis{$atom_id_1} eq $isuis{$atom_id_2}) {
      $match_info .= "EXACT STRING, ";
    } elsif ($luis{$atom_id_1} eq $luis{$atom_id_2}) {
      $match_info .= "SAME NORM STRING, ";
    } else {
      $match_info .= "DIFFERENT STRING, ";
    }

    if ($codes{$atom_id_1} eq $codes{$atom_id_2} &&
	($codes{$atom_id_1} || $codes{$atom_id_2})) {
      $match_info .= "SAME CODE, ";
    } else {
      $match_info .= "DIFFERENT CODE, ";
    }
    
    if ($ttys{$atom_id_1} eq $ttys{$atom_id_2}) {
      $match_info .= "SAME TTY";
    } else {
      $match_info .= "DIFFERENT TTY";
    }

    $match_info .= "</tt>";
    

    #
    # Get related concept id
    #
    $sh2 = $dbh->prepare(qq{
      SELECT concept_id FROM classes WHERE atom_id=?
     }) ||
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh2->execute($atom_id_2) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    while (($c2) = $sh2->fetchrow_array) {
      $concept_id_2=$c2;
    }
    

    #
    # Check for safe replacement
    #
    $sh2 = $dbh->prepare(qq{
      SELECT DISTINCT new_atom_id, ' (<font size="-"><i>safe replacement atom</i></font>)'
      FROM mom_safe_replacement
      WHERE new_atom_id IN (?, ?)
     }) ||
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh2->execute($atom_id_1, $atom_id_2) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    while (($atom_id, $sr_flag) =$sh2->fetchrow_array) {
      $sr_flags{$atom_id}= $sr_flag;
     }

    #
    # Get other relationships between the concepts
    #
    $sh2 = $dbh->prepare(qq{
      SELECT relationship_name, relationship_attribute, source, 
        authority, relationship_level, tobereleased
      FROM relationships WHERE concept_id_1 = ?
	AND concept_id_2 = ?
	AND relationship_id != ?
      UNION
      SELECT b.relationship_name, c.relationship_attribute, source, 
	authority, relationship_level, tobereleased
      FROM relationships a, inverse_relationships b,
	   inverse_rel_attributes c
      WHERE concept_id_2 = ?
	AND concept_id_1 = ?
	AND a.relationship_name=b.inverse_name
	AND relationship_id != ?
	AND NVL(a.relationship_attribute,'null') =
	    NVL(c.inverse_rel_attribute,'null')
     }) ||
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh2->execute($concept_id, $concept_id_2, $relationship_id, $concept_id, $concept_id_2, $relationship_id) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    $other_rel_info = "";
    $ct_flag=0;
    while (($rel, $rela, $sab, $auth, $level, $tbr) = $sh2->fetchrow_array) {
      if ($ct_flag) { $other_rel_info .= "<br>\n"; }
      $other_rel_info .= "<tt>";
      if ($tbr eq "N" || $tbr eq "n") {	$other_rel_info .= "\{"; }
      $other_rel_info .= "$rel_name_map{$rel} $rela $sab $level"; 
      if ($tbr eq "N" || $tbr eq "n") {	$other_rel_info .= "\}"; }
      $other_rel_info .= "</tt>";
      $ct_flag++;
    }

    unless ($ct_flag) {
      $other_rel_info = "<i>No other relationships found</i>";
    }

    #
    # Get Action that created the demotion
    #
    $sh2 = $dbh->prepare(qq{
      SELECT molecular_action, transaction_id, a.molecule_id, work_id,
	a.authority, a.timestamp, atomic_action_id
      FROM molecular_actions a, atomic_actions b
      WHERE b.row_id = $relationship_id AND action='I'
	AND table_name='R' AND a.molecule_id=b.molecule_id
	AND undone='N'
     }) ||
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh2->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    $ct_flag=0;
    while (($molecular_action, $transaction_id, $m_id,
	    $w_id, $auth, $timestamp, $atomic_action_id) 
	   = $sh2->fetchrow_array) {
      $work_id = $w_id; $authority=$auth;
      $molecule_id = $m_id;
      $molecular_action =~ s/\_/ /;
      $enc_timestamp = &meme_utils::escape($timestamp);
      $action_info = qq{<pre>$molecular_action
Transaction ID: <a href="$cgi_dir/action.cgi?command=harvester&transaction_id=$transaction_id&start_date=01-jan-1990&end_date=now" onMouseOver="window.status='Click to get actions for this transaction id.'; return true;" onMouseOut="window.status=''; return true;">$transaction_id</a>
Molecule ID: <a href="$cgi_dir/action.cgi?command=molecular_action_report&molecule_id=$molecule_id" onMouseOver="window.status='Click to see more details on this action.'; return true;" onMouseOut="window.status=''; return true;">$molecule_id</a>
Authority: $authority
Timestamp: $timestamp

ATOMIC ACTION
Atomic Action ID: $atomic_action_id
Relationship ID: $relationship_id</pre>
};
      $ct_flag++;
     }

    unless ($ct_flag) {
      $action_info = qq{<i><span id="red">No Action Was Found</span></id>};
     };

    #
    # Get Work info
    #
    if ($work_id) {
      $sh2 = $dbh->prepare(qq{
        SELECT type, authority, description
        FROM meme_work WHERE work_id = ?
      }) ||
	((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

      $sh2->execute($work_id) || 
	((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

      $ct_flag=0;
      while (($type, $auth, $detail) = $sh2->fetchrow_array) {
	$authority = $auth;
	$work_info = qq{$type - $auth<br>
			  $detail
}
       };
     } else {
       $work_info = qq{<i>No details available</i>};
      };

    #
    # Get Merge Set Info
    #
    if ($authority) {
      if ($work_id) {
	$work_id_clause = " AND work_id = $work_id ";
      }
      $sh2 = $dbh->prepare(qq{
	SELECT merge_set, source, NVL(violations_vector,'null')
	FROM mom_facts_processed WHERE source = ?
	AND atom_id_1=? AND atom_id_2=?
	  $work_id_clause
	UNION
	SELECT merge_set, source, NVL(violations_vector,'null')
	FROM mom_facts_processed WHERE source = ?
	$work_id_clause AND atom_id_2=?
        AND atom_id_1=?
      }) ||
	((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

      $sh2->execute($authority, $atom_id_1, $atom_id_2, $authority, $atom_id_1, $atom_id_2) || 
	((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

      $merge_set_info = "";
      $ct_flag=0;
      while (($ms, $source, $vv) = $sh2->fetchrow_array) {
	$merge_set = $ms;
	$violations_vector = $vv unless ($vv eq "null");
	if ($ct_flag) {
	  $merge_set_info .="<br>"
	};
	$merge_set_info .=
	  qq{<a href="/Sources/RECIPE.}.lc($source).qq{.html" onMouseOver="window.status='Click here to view the recipe.'; return true;" onMouseOut="window.status=''; return true">$merge_set, $source</a>
};
	$ct_flag++;
      }
      unless ($ct_flag) {
	$merge_set_info = "<i>No details available</i>";
      }
    } else {
      $merge_set_info = qq{<i>No details available</i>};
    };

    if ($violations_vector) {
      $sh2 = $dbh->prepare(qq{
	SELECT ic_name, ic_short_dsc, ic_long_dsc
	FROM integrity_constraints
      }) ||
	((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

      $sh2->execute || 
	((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

      while (($ic_name, $short_dsc, $long_dsc) = $sh2->fetchrow_array) {
	$short_dsc{$ic_name} = "$short_dsc";
	$long_dsc =~ s/'/\\'/g;
	$long_dsc{$ic_name} = "$long_dsc";
      }
      
      $ct_flag=0;
      $vv_info = "";
      while ($violations_vector =~ s/^\<([^\>]*)\>//) {
	$check = $1;
	($check) = split /\:/, $check;
	if ($ct_flag) {
	  $vv_info .= "<br>\n";
	}
	$vv_info .= qq{<a href="javascript:openDescription('$check','$long_dsc{$check}')" onMouseOver="window.status='Click for long description.'; return true;" onMouseOut="window.status=''; return true;">$check</a> - $short_dsc{$check}
};
	$ct_flag++;
      }

    } else {
      if ($merge_set =~ /$authority-D.*/) {
	$vv_info = qq{The demotion is not the result of integrity violations, this
		      was created by a recipe step that explicitly adds demotions.};
      } else {
	$vv_info = qq{<i>Cause not known</i>};
      }

    }    
    $dem_ct++;
    print qq{
    <center>
      Demotion #$dem_ct<br>
    <table border="1" width="90%" cellpadding="2">
    <tr><td valign="top" width="25%"><b>Concept:</b></td><td><a href="/cgi-oracle-meowusers/concept-report-mid.pl?action=searchbyconceptid&db=$db&arg=$concept_id\"><tt>$concept_id</tt></td></tr>
    <tr><td valign="top" width="25%"><b>Demoted Atom:</b></td>
      <td>$demotion_atoms{$atom_id_1} $sr_flags{$atom_id_1}</td></tr>
    <tr><td valign="top" width="25%"><b>Related Concept:</b></td><td><a href="/cgi-oracle-meowusers/concept-report-mid.pl?action=searchbyconceptid&db=$db&arg=$concept_id_2"><tt>$concept_id_2</tt></td></tr>
    <tr><td valign="top" width="25%"><b>Related Demoted Atom:</b></td>
      <td>$demotion_atoms{$atom_id_2} $sr_flag{$atom_id_2}</td></tr>
    <tr><td valign="top" width="25%"><b>Matching Predicate<br>Between Atoms:</b></td><td>$match_info</td></tr>
    <tr><td valign="top" width="25%"><b>Other Relationships:</b></td><td>$other_rel_info</td></tr>
    <tr><td valign="top" width="25%"><b>Action That<br>Created Demotion:</b></td><td>$action_info</td></tr>
    <tr><td valign="top" width="25%"><b>Work Performed:</b></td><td>$work_info</td></tr>
    <tr><td valign="top" width="25%"><b>Merge Set, Source:</b></td><td>$merge_set_info </td></tr>
    <tr><td valign="top" width="25%"><b>Reasons for Failure:</b></td><td>$vv_info</td></tr>
    </table>
    </center><p>
};
    $row_count++;
    
   }  

  unless ($row_count) {
    print qq{
<span id="red">There are no status N connected to concept $concept_id</span>
     }
   }

  # disconnect
  $dbh->disconnect;

}; # end PrintREVIEW_LOG

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
 This script is a CGI script used determine the cause of status N
 connected to a concept_id.  It takes CGI arguments in the standard
 form, e.g. "key=value&key=value...". 

 Parameters:

  state         :  This is an internal variable representing what
                   state the application is in.
  concept_id    :  A valid concept id
  db            :  Database name
  meme_home     :  Value of $MEME_HOME that the script should use.

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


