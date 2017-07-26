#!@PATH_TO_PERL@
#
# File:     validate_mrd.cgi
# Author:   Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#
# Changes:
# 02/26/2007 BAC (1-DLFON): use POST for submitting form.
# 01/09/2007 BAC (1-D5AC1): Wrap queries in description at 80 characters 
# 09/09/2006 JFW (1-C556X): Add notification (*) to counts where there is an adjustment 
#                           Truncate printed check results to 1000
#                           Add adjustment and auto_fix information to description
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
#
# Version info:
# 12/09/2003 4.2.0: descriptions now in a separate state
# 12/02/2003 4.1.0: First version
#
$release = "4";
$version = "2.0";
$version_authority = "JFW";
$version_date = "12/09/2003";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";
use Text::Wrap;


# Set variables for Text::Wrap throughout the entire script
# Text::Wrap is used to format SQL queries with newlines on the displayed page.
local($Text::Wrap::columns) = 80;
local($Text::Wrap::separator) = "<br>";


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
   "state" => 1, "log_name" => 1, "check_name" => 1,
   "command" => 1, "check_type" => 1, "query" => 1,
   "description" => 1, "db" => 1, "adjustment" => 1,
   "adjustment_dsc" => 1, "auto_fix" => 1,
   "check_name_bak" => 1, "db" => 1,
   "meme_home" => 1, "make_checklist" => 1,
   "oracle_home" => 1 );

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
$state = "CHECK_JAVASCRIPT" unless $state;


#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MRD, current MRD
# 
$db = &meme_utils::midsvcs("mrd-db") unless $db;
$date = `/bin/date +%Y%m%d`;
$cgi = $ENV{"SCRIPT_NAME"};
$start_time = time;
$style_sheet = &meme_utils::getStyle;
$checklist_script = "$ENV{EMS_HOME}/bin/make-checklist.pl";
$FATAL = 1;


#
# Determine Which action to run, print the body
# Valid STATES:
#
# 0. CHECK_JAVASCRIPT.  Verify that javascript is enabled and redirect
#    the page.  If not print a message indiciating that JavaScript must
#    be enabled.
# 1. INDEX. Print the index page
# 2. RUNNING_SCRIPT. Refresh page shown while script is running
# 3. REVIEW_LOG. Review $log_name log.
# 4. REVIEW_LOGS. Show list of available logs
# 5. LIST_DATA. Run the check and list affected data, link to
#               concept reports if concept_id data.
# 6. EDIT_INDEX. Index page for check editing.
# 7. EDIT_FORM. Form for editing/inserting checks.
# 8. EDIT_COMPLETE. Page shown upon insert,modify,delete
# 9. MAKE_CHECKLIST. This will produce a checklist from a query
# 10. DESCRIPTION. Show the description of a check

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	     "CHECK_JAVASCRIPT" => 
	         ["PrintHeader","PrintCHECK_JAVASCRIPT","PrintFooter","Check JavaScript","This page will redirect if JavaScript is enabled."],
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","MRD Validation - Index Page","Validate Mrd"],
	     "RUNNING_SCRIPT" => 
	         ["PrintHeader","PrintRUNNING_SCRIPT","PrintFooter", "Running Script","Running validate.pl"],
	     "REVIEW_LOG" => 
	         ["PrintHeader","PrintREVIEW_LOG","PrintFooter","Review Log","Review Log: $log_name"],
	     "REVIEW_LOGS" => 
	         ["PrintHeader","PrintREVIEW_LOGS","PrintFooter","Review Logs","Review Logs"],
	     "LIST_DATA" => 
	         ["PrintHeader","PrintLIST_DATA","PrintFooter","List Data","List Data for <i>$check_name</i>"],
	     "MAKE_CHECKLIST" => 
	         ["PrintHeader","PrintMAKE_CHECKLIST","PrintFooter","Make Checklist","Make Checklist for <i>$check_name</i>"],
	     "EDIT_INDEX" => 
	         ["PrintHeader","PrintEDIT_INDEX","PrintFooter","Edit Checks","Edit Checks"],
	     "EDIT_FORM" => 
	         ["PrintHeader","PrintEDIT_FORM","PrintFooter","Edit Check Form","$command Check"],
	     "EDIT_COMPLETE" => 
	         ["PrintHeader","PrintEDIT_COMPLETE","PrintFooter","Edit Complete","$command Check <i>$check_name</i>"],
	     "DESCRIPTION" => 
	         ["PrintHeader","PrintDESCRIPTION","PrintFooter","Description: $check_name",""],
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
	      <address><a href="$cgi?db=$db">Back to Index</a></address>
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
	    window.location.href='$cgi?state=INDEX&db=$db';
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
    $log_name = `ls -t MRDQALogs/validate.*.log | head -1`;
    chomp($log_name);
    $log_name =~ s/MRDQALogs\///;
    $log_name = &meme_utils::escape($log_name);

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
    $db_select_form .= "			                </select>\n";


    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    if($command eq "RESUME") {
       $dbh->do("UPDATE system_status SET status='ON' WHERE system='mrd_validation'")
       ||
    ((print "<span id=red>Error updating system_status ($DBI::errstr).</span>")
             &&  return);
       $command = "SUSPEND";  
    }
    
    elsif ($command eq "SUSPEND") {
       $dbh->do("UPDATE system_status SET status='OFF' WHERE system='mrd_validation'")
       ||
    ((print "<span id=red>Error updating system_status ($DBI::errstr).</span>")
             &&  return);
       $command = "RESUME";
    }

    else {
     
    $sh = $dbh->prepare(qq{
    SELECT status FROM system_status WHERE system='mrd_validation'
    }) ||
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
             &&  return);
    
    $sh->execute ||
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);
    
    ($status) = $sh->fetchrow_array;
    if ($status eq "ON") {
    $command = "SUSPEND";
    }
        else {
        $command = "RESUME";
        }
    }


    print qq{
<i>Select from one of these choices:</i>
<br>&nbsp;
<center><table WIDTH="90%" NOSAVE >
<tr nosave>
<td width="25%">Change Database:</td><td>$db_select_form</td>
</tr>
<tr><td colspan="2">&nbsp;</td></tr>
<tr NOSAVE>
<td colspan="2" NOSAVE>
<b><a href="$cgi?state=RUNNING_SCRIPT&db=$db" onMouseOver='window.status="Run Script"; return true;' onMouseOut='window.status=""; return true;' onClick="return confirm('Running the script takes between 30 minutes and 1 hour.\\nAre you sure you want to do this now?');" >Run Script</a></b>
<ul>
};
    # prepare and execute statement
    $sh = $dbh->prepare(qq{
    SELECT DISTINCT check_type FROM mrd_validation_queries
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    while (($check_type) = $sh->fetchrow_array) {
      $enc_type = &meme_utils::escape($check_type);
      print qq{  <li><a href="$cgi?state=RUNNING_SCRIPT&db=$db&check_type=$enc_type" onMouseOver='window.status="Run Script: $check_type section only"; return true;' onMouseOut='window.status=""; return true;' onClick="return confirm('Running the script takes between 30 minutes and 1 hour.\\nAre you sure you want to do this now?');" >Only $check_type section</a></li>
};
    }
   
    # disconnect
    $dbh->disconnect;
    print qq{
</ul>
</td>
</tr>
<tr>
<td colspan="2"><b><a href="$cgi?state=REVIEW_LOG&db=$db&log_name=$log_name" onMouseOver='window.status="Review Current Log"; return true;' onMouseOut='window.status=""; return true;'>Review Current Log</a></b></td>
</tr>

<tr NOSAVE>
<td colspan="2" NOSAVE><b><a href="$cgi?state=REVIEW_LOGS&db=$db" onMouseOver='window.status="Review Past Logs"; return true;' onMouseOut='window.status=""; return true;'>Review Past Logs</a></b></td>
</tr>

<tr>
<td colspan="2"><b><a href="$cgi?state=EDIT_INDEX&db=$db" onMouseOver='window.status="Edit Validate Mrd Checks"; return true;' onMouseOut='window.status=""; return true;'>Edit checks</a></b></td>
</tr>

<tr>
<td>
This script runs every Saturday.
<form action="$cgi" method="GET">
  <input type="hidden" name="command" value="$command">
  <input type="submit" value="$command weekly run">
  <input type="hidden" state="INDEX">
  <input type="hidden" db="$db">
</form>
</td>
</tr>


</table></center>

<p>
    };
}; # end PrintINDEX

###################### Procedure PrintDESCRIPTION ######################
#
# This procedure prints the description of a check
#
sub PrintDESCRIPTION {

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);

  # prepare and execute statement
  $sh = $dbh->prepare(qq{
    SELECT description,adjustment,query,auto_fix FROM mid_validation_queries
    WHERE check_name = ?
    }) || 
      ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
       &&  return);
  
  $sh->execute($check_name) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
     &&  return);
  
  ($description,$adjustment,$query,$auto_fix) = $sh->fetchrow_array;

  $enc_name = &meme_utils::escape($check_name);
  $wrap_query = wrap("","",$query);
    
  print qq{
<p><a href="$cgi?state=EDIT_FORM&db=$db&command=Modify&check_name=$enc_name">Edit this check</a></p>
<p>$description</p>
<p>This check currently has an adjustment of $adjustment.</p>
<p>The Query is:<br> 
<tt>$wrap_query</tt>
</p>
<p>The Auto Fix is:<br> 
<tt>$auto_fix</tt>
</p>
<center><form><input type=button onClick='window.close(); return true' value='Close'></form></center>
};  

}; # end PrintDescription


###################### Procedure PrintRUNNING_SCRIPT ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintRUNNING_SCRIPT {

    unless ($log_name){
	$date = `/bin/date +%Y%m%d`; chop($date);
	$log_name="validate.$db.$date.log";
        while (-e "MRDQALogs/$log_name") {
            $i++;
            $log_name = "validate.$db.$date.$i.log";
        }
	unlink "MRDQALogs/$log_name";
        `touch MRDQALOGS/$log_name`;
	# an error here means that the
	# Logs directory is most likely not writeable by suresh
	system qq{$ENV{MEME_HOME}/bin/validate.pl $db mrd "$check_type" > $inc/MRDQALogs/$log_name & };
	if ($?) {
	  print qq{ <span id=red>The script could not be run ($?, $!)</span>
};
	  return;
        }
#	print "$ENV{ORACLE_HOME} $! $?<br>\n";
	`chmod 666 MRDQALogs/$log_name`;
	sleep 1;
    };

    open (F,"MRDQALogs/$log_name") || die "couldn't open file: $log_name\n";
    @lines = <F>;
    close(F);
    $finished=0;

    foreach $line(@lines){
	$finished=1 if $line =~ /Finished/;
	$error=1 if $line =~ /DBD ERROR/;
    }
    
    if ($error) {
	print qq{ <span id=red>There was an error running the Validate MRD script.  The log that was produced appears below:</span><p><pre>
};
	print join("/n",@lines),"/n";
	print qq{</pre>};
    } elsif ($finished){
	print qq{ Validate MRD script is done.
	      Click <a href="$cgi?state=REVIEW_LOG&db=$db&log_name=$log_name"> here</a> to see the log.
	      };
    } else {
	print qq { The Validate MRD Script is running. The resulting log is displayed below.  This page will refresh itself every 10 seconds.  The duration of the process is between 30 minutes to 1 hour.  

If you decide to leave this site after running this procedure, the script will continue to run and you can view the log later simply by visiting the Curent Log page, which can be accessed from the Main Index page<p><pre>
};

	print @lines,"\n";
	print qq{</pre><script> setInterval("window.location.href='$cgi?state=RUNNING_SCRIPT&log_name=$log_name&db=$db'",10000)</script>}

    }; 

}; # end PrintRUNNING_SCRIPT

###################### Procedure PrintREVIEW_LOG ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintREVIEW_LOG {

# set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

# open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

# prepare and execute statement
    $sh = $dbh->prepare(qq{
    SELECT DISTINCT check_name, description,
           adjustment, adjustment_dsc
    FROM mrd_validation_queries
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    while (($check_name,$description,$adjustment,$adjustment_dsc) =
	   $sh->fetchrow_array) {
        $descriptions{$check_name}="$description";
	if ($adjustment) {
	    $descriptions{$check_name} .= qq{<br><br><br>An adjustment of $adjustment was made to this check for the following reason:<br><br><blockquote><tt>$adjustment_dsc</tt></blockquote>};
	}
    }
   
    # disconnect
    $dbh->disconnect;

    open(F,"MRDQALogs/$log_name") || 
            ((print "<span id=red>Error opening $log_name.</span>") &&
             return);

    print qq{
     <i>Clicking on a test name to see a description of that test.  Click
        on a non-zero report count to see the actual results of that check.
	For checks that return concept ids, you will have the option to
        create a worklist from the results.</i>       
	<pre>
};
    while (<F>) {

	if (/    [^\s].* -?[0-9]+/) {
	    /    (.{54}) (-?[0-9]{1,}\*?)(   )?(.*)?/;
	    $name=$1; $ct=$2; $date=$4;
	    $name=~s/(\s+)$//;
            $esc_name = "$name";
	    $space=$1;
	    $esc_name =~ s/'/&#039;/g;
	    $esc_name =~ s/\r//g;
	    $esc_name =~ s/\n//g;
	    $esc_description = "$descriptions{$name}";
	    $esc_description =~ s/"/&quot;/g;
            $esc_description =~ s/'/&#039;/g;
            $esc_description =~ s/</&lt;/g;
            $esc_description =~ s/>/&gt;/g;
	    print qq{    <a href='javascript:void(0);'
			   onClick='window.open("$cgi?state=DESCRIPTION&check_name=$esc_name&db=$db","","scrollbars,width=500,height=250,resizable"); return false;' 
			 onMouseOver="window.status='View Description.'; return true;" onMouseOut="window.status=''; return true;">$name</a>$space };
	if ($ct) {
	    $enc_name = &meme_utils::escape($name);
	    print qq{<a href="$cgi?state=LIST_DATA&db=$db&check_name=$enc_name" onMouseOver="window.status='Show data for: $enc_name'; return true" onMouseOut="window.status=''; return true;" onClick='return confirm("This link will actually run the query.\\nIt may take several minutes and might report\\na different number of rows than the log.\\nAre you sure you want to proceed?");'>$ct</a>\n};
	}
	else {
	    print "$ct\n";
	    };
    } else {
        print;}
}
    close(F);
    print qq{
</pre>
};
}; # end PrintREVIEW_LOG

###################### Procedure PrintREVIEW_LOGS ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintREVIEW_LOGS {
    print qq{
<i>The following is a list of all validate MRD logs:</i>
<br>&nbsp;
<blockquote>
<form action="$cgi" method="GET">
   <input type="hidden" name="state" value="REVIEW_LOG">
   <input type="hidden" name="db" value="$db">
   <select name="log_name" onChange="this.form.submit(); return true;">
      <option>---- SELECT A LOG ----</option>
};
    chdir "MRDQALogs";
    @logs = `ls -t validate.*.log`;
    foreach $log (@logs) {
	chomp($log);
	print qq{      <option>$log</option>
};
    }
    print qq{
    </select>
</form>
</blockquote>
<p>
};
}; # end PrintREVIEW_LOGS

###################### Procedure PrintLIST_DATA ######################
#
# This procedure prints the actual results of running a check
#
sub PrintLIST_DATA {
    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    $dbh->do(qq{
    ALTER SESSION set sort_area_size=200000000
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

    $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

    if ($auto_fix) {
     $dbh->do(qq{$auto_fix}) ||
     ((print "<span id=red>Error running $auto_fix ($DBI::errstr).</span>")
             &&  return);
    
    $auto_fix_status = qq{<p><span id="blue">Auto fix successfully run.</span></p>\n};

    }

    # prepare check name
    $check_name =~ s/'/''/g;

    # prepare and execute statement
    $sh = $dbh->prepare(qq{
    SELECT query, make_checklist, description, adjustment, adjustment_dsc, auto_fix
    FROM mrd_validation_queries
    WHERE check_name = ?
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute($check_name) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    # There should only be one row, get it and run that query
    ($query, $make_checklist, $description, $adjustment, $adjustment_dsc, $auto_fix)
       = $sh->fetchrow_array;

    # prepare and execute query
    $sh = $dbh->prepare($query) ||
    ((print "<span id=red>Error preparing query ($query, $DBI::errstr).</span>")
             &&  return);

    $sh->execute ||
    ((print "<span id=red>Error executing query ($query, $DBI::errstr).</span>")
             &&  return);

    #
    # If $make_checklist is Y, there should be a link for
    # making the checklist
    #
#    if ($make_checklist eq "Y") {
#      $enc_name = &meme_utils::escape($check_name);
#      $mc_text = qq{
#        This MRD validation check can be made into a checklist.
#        <a href="$cgi?state=MAKE_CHECKLIST&db=$db&check_name=$enc_name"
#          onMouseOver="window.status='Make checklist.';return true;"
#          onMouseOut="window.status='';return true;">Click here to proceed.</a>
#}
#    }

    # print the top of the table
    $adjustment_txt = qq{           <tr><td valign="top"><b><font size="-1">Adjustment:</font></b></td>
               <td>$adjustment, $adjustment_dsc</td></tr>\n} if $adjustment;
    $enc_name = &meme_utils::escape($check_name);
    $enc_auto_fix = &meme_utils::escape($auto_fix);
    $auto_fix_txt = qq{           <tr><td valign="top"><b><font size="-1">Auto Fix:</font></b></td>
               <td><a href="$cgi?state=LIST_DATA&db=$db&check_name=$enc_name&auto_fix=$enc_auto_fix" 
                    onMouseOver="window.status='Run auto fix'; return true;"
                    onMouseOut="window.status=''; return true;"
                    onClick="return confirm('Are you sure you want to run this auto fix?');">Run auto fix</a><br>
                <tt>$auto_fix</tt>
               </td></tr>\n} if $auto_fix;
    print qq{
    <blockquote>
    $auto_fix_status
    $mc_text
    <br>&nbsp;<br>
    <table><tr><td valign="top"><b><font size="-1">Description:</font></b></td>
               <td>$description</td></tr>
           $adjustment_txt
           $auto_fix_txt
           <tr><td valign="top"><b><font size="-1">Edit:</font></b></td>
               <td><a href="$cgi?state=EDIT_FORM&db=$db&command=Modify&check_name=$enc_name">Edit this check</a></td></tr>
           <tr><td valign="top"><b><font size="-1">Query:</font></b></td>
               <td><tt>$query</tt></td></tr>
           <tr><td valign="top"><b><font size="-1">Results:</font></b>
           <td align="left"><table>
};

    # AT some point change this to use
    # fetchrow_hashref and print the column headers.

    # fetch the data & print to screen
    $results_html="";
    $ct=0;
    $ct_string="";
    while (@results = $sh->fetchrow_array) {
        $found = 1;
        $ct++;
 
        if($ct > 1000) {
        	$results_html .= qq{      <tr><td align="left">... (results truncated at 1000)</td></tr>\n};
			$ct_string="1000+";
			last;
        }
        
        $results_html .= qq{      <tr>};
        foreach $result (@results) {

            # if concept_ids link to reports 
            if ($query =~ /select concept_id/ ||
                $query =~ /select distinct .\.concept_id/ ) {   
          
                # set up for concept report
                if ($result =~ /^[0-9]*$/) {
                    $result = qq{<a href="/cgi-oracle-meowusers/concept-report-mid.pl?action=searchbyconceptid&arg=$result&db=$db">$result</a>};
                } else {
                    $result = qq{<a href="/cgi-oracle-meowusers/concept-report-mid.pl?action=searchbynormstr&arg=$result&db=$db">$result</a>};
                };
            }
            $results_html .= qq{<td align="left">$result</td>};
        };
        $results_html .= qq{</tr>\n};
    }

    # If there are results, print them out using $results_html
    if (!$ct_string) {
	$ct_string=$ct;
    }
    print qq{
           <tr><td valign="top"><b><font size="-1">Results ($ct_string):</font></b>
           <td align="left"><table>$results_html
    };

    # if no rows, print no rows
    unless ($found) {
        print qq{       <tr><td>No data found</td></tr>\n    }
    }
    # end the table
    print qq{        </table></td></tr>
    </table>
    </blockquote>\n
};

}; # end PrintLIST_DATA


###################### Procedure PrintMAKE_CHECKLIST #####################
#
# This procedure produces a checklist
#
sub PrintMAKE_CHECKLIST{
    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    # prepare check name
    $check_name =~ s/'/''/g;

    # prepare and execute statement
    $sh = $dbh->prepare(qq{
    SELECT query, make_checklist
    FROM mrd_validation_queries
    WHERE check_name = ?
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute($check_name) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    # There should only be one row, get it and run that query
    ($query, $make_checklist) = $sh->fetchrow_array;

    #
    # Query must return a concept_id
    #
    $query = qq{SELECT DISTINCT concept_id FROM ($query)};

    # prepare and execute query
    $sh = $dbh->prepare($query) ||
    ((print "<span id=red>Error preparing query ($query, $DBI::errstr).</span>")
             &&  return);

    $sh->execute ||
    ((print "<span id=red>Error executing query ($query, $DBI::errstr).<br>&nbsp;<br>Most likely, the query does not have a concept_id column.</span>")
             &&  return);

    #
    # Create a temporary table for call to make_checklist.pl
    #
    open (F,">/tmp/t.$$") || 
     ((print "<span id=red>Could not open tmp file: $! $?</span>\n") && return);
    while (($concept_id) = $sh->fetchrow_array) {
      print F "$concept_id\n";
    }
    close (F);

    #
    # Call make_checklist.pl
    #
    # checklist name derived from check name, stripped to 30 chars
    #
    $checklist_name = "chk_$check_name";
    $checklist_name =~ s/[^A-Za-z0-9]/_/g;
    $checklist_name = lc($checklist_name);
    $checklist_name =~ s/^(.{1,30}).*$/$1/;
    $output = `$checklist_script -d $db -c $checklist_name -b validate_mrd -t AH  < /tmp/t.$$`;
    if ($? != 0) {
      print "<span id=red>Error making checklist: $! $?<br>$output</span>" && return;
    }

    #
    # Cleanup
    #
    unlink "/tmp/t.$$";

    #
    # Redirect user to checklist page
    #
    print qq{<script language="javascript">window.location.href="/cgi-oracle-nlmlti/wms.pl?action=view&checklist=$checklist_name&db=$db";</script>
    The checklist <i>$checklist_name</i> was successfully created.  You are being
    redirected to the WMS.  If you are not redirected in 5 seconds, click 
    <a href="/cgi-oracle-nlmlti/wms.pl?db=$db&action=view&checklist=$checklist_name">here</a>.
}


}; # end PrintMAKE_CHECKLIST

###################### Procedure PrintEDIT_INDEX ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintEDIT_INDEX {

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    # look up descriptions
    $sh = $dbh->prepare(qq{
    SELECT DISTINCT check_type, check_name
    FROM mrd_validation_queries
    ORDER BY check_type	
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    while (($check_type, $check_name) =
	   $sh->fetchrow_array) {
	push @checks, $check_name;
	$types{$check_name} = "$check_type";
    }

    if ($check_name) {
	$selected{$checkname} = "SELECTED";
    };

    # Print the forms
    print qq{
<i>Select from one of these choices in order to insert, delete or modify a current query.  Or click <a href="$cgi?db=$db">here</a> to return to the index page.</i>
<br>&nbsp;
    <center><table CELLPADDING=2 WIDTH="90%" NOSAVE >

      <tr NOSAVE>
        <td>
          <form method="GET" action="$cgi">
	  <center></b>
          <font size="-1">
	  <input type="hidden" name="state" value="">
	  <input type="hidden" name="db" value="$db">
	  <input type="hidden" name="command" value="">
	  <input type="button" value="Insert New Check"
	      onMouseOver='window.status="Insert New Check"; return true;'
              onClick='this.form.state.value="EDIT_FORM"; this.form.command.value="Insert"; this.form.submit(); return true;'>
          &nbsp;&nbsp;&nbsp;
	  <input type="button" value="Modify"
	      onMouseOver='window.status="Modify selected check"; return true;'
              onClick='this.form.state.value="EDIT_FORM"; this.form.command.value="Modify"; this.form.submit(); return true;'>
          &nbsp;&nbsp;&nbsp;
	  <input type="button" value="Delete"
	      onMouseOver='window.status="Delete selected check"; return true;'
              onClick='this.form.state.value="EDIT_COMPLETE"; this.form.command.value="Delete"; verifyDelete((this.form.check_name.options[this.form.check_name.selectedIndex]).text,this.form); return true;'>
          </font></b>
	  </center>
	</td>
      </tr>
      <tr>
	<td><center><font size="+0">
	  <select name="check_name" size="20"
              onDblClick='this.form.state.value="EDIT_FORM"; this.form.command.value="Modify"; this.form.submit(); return true;'>
};

    foreach $check (@checks) {
	print qq{          <option value="$check" $selected{$check}>$types{$check}: $check</option>
};
    }

    # end the table
    print qq{
          </select>
	  </font></center>
	  </form>
	</td> 
      </tr>
    </table>
    </center>
</p>
};

}; # end PrintEDIT_INDEX

###################### Procedure PrintEDIT_FORM ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintEDIT_FORM {

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    $adjustment = "0" unless $adjustment;
    $esc_check_name = "$check_name";
    $esc_check_name =~ s/'/''/g;

    # If we are inserting $check_name will be empty
    #   The code should have a space for check name
    # If we are modifying it will be not.
    #   we should preserve check name
    if ($check_name && $command eq "Modify") {
	$cn_code = qq{
	   <input type="text" name="check_name" value="$check_name" size="60">
	   };
	$sh = $dbh->prepare(qq{
	    SELECT check_type,query,description,adjustment,adjustment_dsc,auto_fix,make_checklist
	    FROM mrd_validation_queries WHERE check_name=?
	    }) || 
		((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>")  
		 &&  return);

	$sh->execute($esc_check_name) || 
	((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
	 &&  return);

	$found;
	while (($check_type,$query,$description,$adjustment,$adjustment_dsc,$auto_fix,$make_checklist) = $sh->fetchrow_array) {
#	    print "INFO!!!: ",join("\n",($check_type,$query,$description,$adjustment,$adjustment_dsc,$auto_fix,$make_checklist)),"\n";
	    $found++;
	    last;
	}
	if ($found != 1) {
	    ((print "<span id=red>Error finding check: $check_name query.</span>")  
		 &&  return);
	}

	# if we are modifying, allow the user to copy the check
	$copy = qq{
	    &nbsp;&nbsp;&nbsp;<input type="button" value="Clone" 
                onClick='this.form.command.value="Clone"; this.form.submit(); return true;'>
	    };
    } else {
	$cn_code = qq{
           <input type="text" name="check_name" value="" size=60>
	   };
    };

    #
    # Get the check types by SELECT DISTINCT check_type
    #
    # look up descriptions
    $vmc = " CHECKED" if $make_checklist eq "Y";
    $selected{$check_type} = "SELECTED";
    $ct_code = qq{
           <select name="check_type">
};

    $sh = $dbh->prepare(qq{
    SELECT DISTINCT check_type FROM mrd_validation_queries
    }) || 
    ((print "<span id=red>Error preparing query ($query,$DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($query,$DBI::errstr).</span>")
             &&  return);

    while (($ct) = $sh->fetchrow_array) {
	$ct_code .= qq{            <option $selected{$ct}>$ct</option>
};
    }
    $ct_code .= qq{
           </select>
};

    print qq{

<i>In order to insert or modify a query, please complete the following fields:</i>
<br>&nbsp;
<form method="POST" action="$cgi">
           <input type="hidden" name="check_name_bak" value="$check_name">
           <input type="hidden" name="command" value="$command">
           <input type="hidden" name="state" value="EDIT_COMPLETE">
           <input type="hidden" name="db" value="$db">
<center><table CELLPADDING=2 WIDTH="90%" NOSAVE >
    <tr>
	<td><b>Check Name</b></td>

	<td>$cn_code</td>
    </tr>

    <tr>
	<td ><b>Check Type</b></td>

	<td>$ct_code</td>
    </tr>

    <tr>
	<td ><b>Make Checklist</b></td>

	<td><input type="checkbox" name="make_checklist" $vmc></td>
    </tr>

    <tr>
	<td><b>Query</a></b></td>

	<td><textarea name="query" wrap="soft" cols="60" rows="3">$query</textarea></td>
    </tr>

    <tr>
	<td ><b>Description</b></td>

	<td><textarea name="description" wrap="soft" cols="60" rows="4">$description</textarea></td>
    </tr>

    <tr>
	<td ><b>Adjustment Count</b></td>

	<td><input type="text" name="adjustment" value="$adjustment" size="60"></td>
    </tr>

    <tr>
	<td ><b>Adjustment Description</b></td>

	<td><textarea name="adjustment_dsc" wrap="soft" cols="60" rows="4">$adjustment_dsc</textarea></td>
    </tr>

    <tr>
	<td ><b>Auto Fix</b></td>

	<td><textarea name="auto_fix" wrap="soft" cols="60" rows="4">$auto_fix</textarea></td>
    </tr>
    
    <tr >
	<td COLSPAN="2" ><center><b>
            <input type="button" value="$command" onClick='this.form.submit(); return true;'>$copy&nbsp;&nbsp;&nbsp;<input type="button" value="Cancel" onClick='window.location.href="$cgi?state=EDIT_INDEX&db=$db"; return true;'></b></center></td>
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

  $query =~ s/\n/ /g;
  $query =~ s/\r/ /g;
  $auto_fix =~ s/\n/ /g;
  $auto_fix =~ s/\r/ /g;

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);
    if ($make_checklist) { $make_checklist = "Y"; }
    else { $make_checklist = "N"; };

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    if ($command eq "Insert" || $command eq "Clone") {

      # Insert code 

      # Verify that no rows with this check name already exist
      $row_count = $dbh->do( qq{
        UPDATE mrd_validation_queries
	SET check_name = ?
        WHERE check_name = ? },
        {"dummy"=>1}, $check_name, $check_name );

      if ($row_count > 0) {
        print qq{<b>You may not $command a check with the name <i>$check_name</i> because one already exists.  Please go back and choose a different name.</b> };  
        return;
      }     

      # insert a row
      $row_count = $dbh->do( qq{
        INSERT INTO mrd_validation_queries
        (check_name,check_type,query,description,adjustment,adjustment_dsc,auto_fix,make_checklist)  
        VALUES (?,?,?,?,?,?,?,?)
      }, {"dummy"=>1}, $check_name, $check_type, $query, $description,
        $adjustment, $adjustment_dsc, $auto_fix, $make_checklist );

      # check for error
      (! $DBI::err) || ((print "<span id=red>Error Inserting row ($DBI::errstr).</span>")
			&&  return);


      # Print response
      print qq{
        A check with the following information was inserted:<p>
	};
      &PrintCheckInfo;

    } elsif ($command eq "Delete") {

     # Delete code

     # delete row
      $row_count = $dbh->do( qq{
        DELETE FROM mrd_validation_queries WHERE check_name= ? },
        {"dummy"=>1},$check_name);

     # check for error
      (! $DBI::err) || ((print "<span id=red>Error deleting row ($check_name,($DBI::errstr).</span>")
			&&  return);
 
     # verify row was deleted
      if ($row_count != 1) {
        print qq{ 
           There was a problem deleting the check <i>$check_name</i>.
	       $row_count rows were deleted instead of 1.
};
        return;
      } else {
        print qq{
           The check <i>$check_name</i> was successfully deleted.<p>
	Click <a href="$cgi?state=EDIT_INDEX&db=$db">here</a> to return to the index page.
}
      };

    } elsif ($command eq "Modify") {

     # Update code here

      #update row
      $row_count = $dbh->do( qq{
        UPDATE mrd_validation_queries SET
            check_name = ?,
            check_type = ?,
            query = ?,
            description = ? ,
            adjustment = ?,
            adjustment_dsc = ?,
            auto_fix = ?,
	    make_checklist = ?
        WHERE check_name = ? }, {"dummy"=>1},
        $check_name, $check_type, $query, $description,
        $adjustment,$adjustment_dsc, $auto_fix, $make_checklist, $check_name_bak);
          
      # check for error
      (! $DBI::err) || ((print "<span id=red>Error updating row ($check_name,($DBI::errstr).</span>")
			&&  return);
          
      # verify row was deleted
      if ($row_count != 1) {
        print qq{
           There was a problem updating the check <i>$check_name</i>. 
$row_count rows were updated instead of 1.
};        
        return;
      };

      # Print response
      print qq{
        The check <i>$check_name_bak</i> was modified using the following information:<p> 
	};
      &PrintCheckInfo;

    };

}; # end PrintEDIT_COMPLETE


###################### Procedure PrintCheckInfo ######################
#
# This procedure prints info for a check.  It uses global variables
# so make sure they are set.
#
sub PrintCheckInfo {

      # Wrap query with newlines
      $wrap_query = wrap("","",$query);

      # Print response
      print qq{
        <center>
        <table width="90%">
          <tr><td valign="top"><font size="-1"><b>Check Name:</b></font></td>
          <td><font size="-1">$check_name</font></td></tr>
          <tr><td valign="top"><font size="-1"><b>Check Type:</b></font></td>   
          <td><font size="-1">$check_type</font></td></tr>
          <tr><td valign="top"><font size="-1"><b>Make Checklist:</b></font></td>   
          <td><font size="-1">$make_checklist</font></td></tr>
          <tr><td valign="top"><font size="-1"><b>Query:</b></font></td>   
          <td><font size="-1"><tt>$wrap_query</td></font></td></tr>
          <tr><td valign="top"><font size="-1"><b>Description:</b></font></td>   
          <td><font size="-1">$description</font></td></tr>
          <tr><td valign="top"><font size="-1"><b>Adjustment:</b></font></td>   
          <td><font size="-1">$adjustment</font></td></tr>
          <tr><td valign="top"><font size="-1"><b>Adjustment<br>Description:</b></font></td>
          <td><font size="-1">$adjustment_dsc</font></td></tr>
          <tr><td valign="top"><font size="-1"><b>Auto Fix:</b></font></td>
          <td><font size="-1"><tt>$auto_fix</tt></font></td></tr>
        </table>
        </center><p>
	Click <a href="$cgi?state=EDIT_INDEX&db=$db">here</a> to return to the index page.
};   
}; # end PrintCheckInfo

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
 This script is a CGI script used to access the Validate MRD tools.  It takes
CGI arguments in the standard form "key=value&key=value...". 

 Parmaters:

  state         :  This is an internal variable representing what
                   state the application is in.
  command       :  Some states (EDIT_*) require the use of a command
                   to know whether to insert, clone, modify or delete a
                   check
  db            :  Database name
  meme_home     :  Value of $MEME_HOME that the script should use.
  log_name      :  Parameter to states that review logs.  It should be
                   the name of a file in the MRDQALogs subdirectory of the
		   directory containing the script.
             
  All other parameters are values used for editing checks.  They roughly
  match the fields in the tables that manage checks. They include:
      check_name, check_name_bak (for cloning), description,
      adjustment, adjustment_dsc, auto_fix

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


