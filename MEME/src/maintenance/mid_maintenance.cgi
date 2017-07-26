#!@PATH_TO_PERL@
#
# File:     mid_maintenance.cgi
# Author:   Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#
# Changes:
# 04/07/2006 jfw (1-AUN6R): Add matrix updater functionality.
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version info:
# 09/30/2003 4.2.0: First version (released)
# 04/25/2003 4.1.0: First version (released)
#
$release = "4";
$version = "2.0";
$version_authority = "BAC";
$version_date = "09/30/2003";

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
		  "state" => 1, "command" => 1, "db" => 1,
		  "meme_home" => 1, "log_name" => 1, "sid" => 1, "serial" => 1,
		  "host" => 1, "port" => 1, "oracle_home" => 1);

#
# Import related code
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
# Set variables (do after readparse)
#
$ENV{"MEME_HOME"} = $meme_home || $ENV{"MEME_HOME"} || $inc;
$ENV{"ORACLE_HOME"} = $oracle_home || $ENV{"ORACLE_HOME"} || die "\$ORACLE_HOME must be set.";
$uniq="t_$$";
$host = "-host $host" if $host;
$port = "-port $port" if $port;

#
# Default Settings for CGI parameters
#
$state = "CHECK_JAVASCRIPT" unless $state;

#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MID, current MID
# 
$db = &meme_utils::midsvcs("production-db") unless $db;
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
# 2. RUNNING. Refresh page shown while script is running
# 3. REVIEW_LOG. Review $log_name log.
# 4. REVIEW_LOGS. Show list of available logs

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	     "CHECK_JAVASCRIPT" => 
	         ["PrintHeader","PrintCHECK_JAVASCRIPT","PrintFooter","Check JavaScript","This page will redirect if JavaScript is enabled."],
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","Mid Maintenance - Index Page","Mid Maintenance"],
	     "RUNNING" => 
	         ["PrintHeader","PrintRUNNING","PrintFooter", "Running Mid Maintenance: $command","Running Mid Maintenance: $command"],
	     "KILL" => 
	         ["PrintHeader","PrintKILL","PrintFooter", "Killing Database Session","Killing Database Session: $sid, $serial"],
	     "CHECK_LOCKS" => 
	         ["PrintHeader","PrintCHECK_LOCKS","PrintFooter", "Research Database Locks","Research Database Locks"],
	     "LIST_SOURCES" => 
	         ["PrintHeader","PrintLIST_SOURCES","PrintFooter", "Recently Inserted Sources","Recently Inserted Sources"],
	     "LIST_ATN" => 
	         ["PrintHeader","PrintLIST_ATN","PrintFooter", "New Attribute Names","New Attribute Names"],
	     "REVIEW_LOG" => 
	         ["PrintHeader","PrintREVIEW_LOG","PrintFooter","Review Log","Review Log: $log_name"],
	     "REVIEW_LOGS" => 
	         ["PrintHeader","PrintREVIEW_LOGS","PrintFooter","Review Logs","Review Logs"],
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
	      function getURL() {
		return "$cgi?state=RUNNING&command=change_df&db=$db&meme_home=$ENV{MEME_HOME}&old_df=" +
		  document.changedf.old_df.options[document.changedf.old_df.selectedIndex].value +
		  "&new_df=" +
		  document.changedf.new_df.options[document.changedf.new_df.selectedIndex].value
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
	<table BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
	  <tr NOSAVE>
	    <td ALIGN=LEFT VALIGN=TOP NOSAVE>
	      <address><a href="$cgi?db=$db">Back to Index</a></address>
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
    chomp($log_name);
    $running_state = "state=RUNNING&db=$db&meme_home=$ENV{MEME_HOME}";

    $selected{$db}="SELECTED";
    $dbs = &meme_utils::midsvcs("databases");
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

    print qq{

<i><div style="width:600">This page provides access to a standard set of MID maintenance tools.  Each one performs
   a standard MID maintenance process which may or may not affect editing.  Please read
   the comments before running a tool.</div></i>
<br>&nbsp;
<center>
<table border="1" cellspacing="2" cellpadding="2" width="90%" nosave="">
  <tr bgcolor="#ffffcc" nosave="">
    <td width="30%" valign="top"><b><font size="-1">Tool</font></b></td><td valign="top"><b><font size="-1">Description</font></b></td>
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1">Change Database</font></td>
    <td valign="top"><font size="-2">$db_select_form.</font></td>
                                     
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?state=REVIEW_LOGS&db=$db" onMouseOver="window.status='Review Logs'; return true;"
	     onMouseOut="window.status=''; return true;">Review Logs</a></font></td>
    <td valign="top"><font size="-2">Review logs from past maintenance operations.</font></td>
 </tr>
 <tr><td colspan="2"> &nbsp;</td></tr>

 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?state=CHECK_LOCKS&db=$db"
	     onMouseOver="window.status='Check database locks'; return true;"
	     onMouseOut="window.status=''; return true;">Check DB Locks</a></font></td>
    <td valign="top"><font size="-2">Queries the database to see who is holding table locks and
	     who is waiting.  Lots of info included.  Safe to run during editing.</font></td>
 </tr>

 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?state=LIST_SOURCES&db=$db"
	     onMouseOver="window.status='List Sources'; return true;"
	     onMouseOut="window.status=''; return true;">List Sources</a></font></td>
    <td valign="top"><font size="-2">List the most recently inserted sources.</font></td>
 </tr>

 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?state=LIST_ATN&db=$db"
	     onMouseOver="window.status='List ATNs'; return true;"
	     onMouseOut="window.status=''; return true;">List New Attribute Names</a></font></td>
    <td valign="top"><font size="-2">List the attribute names and sources that are new with respect to the previous release.</font></td>
 </tr>

 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=set_preference"
	     onClick="return confirm('This operation may take more than 2 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Set preferred atom ids'; return true;"
	     onMouseOut="window.status=''; return true;">Set Preference</a></font></td>
    <td valign="top"><font size="-2">Set preferred <tt>atom_ids</tt> in the <tt>concept_status</tt> table.  If
	     there are problems generating concept reports, this is a good place to start.
	     Potentially unsafe to run during editing.</font></td>
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=fix_concept_reports"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Fix concept reports'; return true;"
	     onMouseOut="window.status=''; return true;">Fix Concept Reports</a></font></td>
    <td valign="top"><font size="-2">This script runs a series of steps to correct
	     the most common problems that prevent concept reports from being generated.
	     Potentially unsafe to run during editing.</font></td>
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=remove_temporary_tables"
	     onClick="return confirm('This operation is potentially dangerous.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Remove temporary tables'; return true;"
	     onMouseOut="window.status=''; return true;">Remove Temporary Tables</a></font></td>
    <td valign="top"><font size="-2">Remove temporary tables starting with <tt>t_</tt>, <tt>qat_</tt>, or <tt>tmp_</tt>.  <b>DO NOT RUN</b> this tool if other batch processes, especially regenerating QA bins are running.
	     Safe to run during editing.</font></td>
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=matrixinit"
	     onClick="return confirm('This operation may take more than 10 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Run matrixinit'; return true;"
	     onMouseOut="window.status=''; return true;">Run Matrixinit</a></font></td>
    <td valign="top"><font size="-2">Run matrix initializer. <b>DO NOT RUN</b> during editing.</font></td>
 </tr>
  <tr>
     <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=matrixupdate"
 	     onClick="return confirm('This operation may take more than 10 minutes.\\nAre you sure you want to do this now?');"
 	     onMouseOver="window.status='Run matrixinit'; return true;"
 	     onMouseOut="window.status=''; return true;">Run Matrix updater</a></font></td>
     <td valign="top"><font size="-2">Run matrix updater in "catch up" mode.</font></td>
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=mthtm_remove"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Remove MM/TMs'; return true;"
	     onMouseOut="window.status=''; return true;">Remove MM/TMs</a></font></td>
    <td valign="top"><font size="-2">Remove redundant MTH/TM atoms. Safe to run during editing (may cause
             slowness).</font></td>
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=mthtm_insert"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Insert MM/TMs'; return true;"
	     onMouseOut="window.status=''; return true;">Insert MM/TMs</a></font></td>
    <td valign="top"><font size="-2">Insert MTH/TM atoms where ambiguities exist. Safe to run
             during editing (may cause slowness).</font></td>
 </tr>
<!--  This one was removed
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=mthpt"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Run MTH/PT patch script'; return true;"
	     onMouseOut="window.status=''; return true;">Run MTH/PT patch script</a></font></td>
    <td valign="top"><font size="-2">Run MTH/PT patch script.</font></td>
 </tr>
--->
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=delete_dup_crels"
	     onClick="return confirm('This operation may take more than 2 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Delete Duplicate Concept Relationships'; return true;"
	     onMouseOut="window.status=''; return true;">Delete Duplicate Concept Relationships</a></font></td>
    <td valign="top"><font size="-2">Run delete duplicate concept relationship patch script. Safe to
             run during editing (may cause slowness).</font></td>
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=delete_empty_concepts"
	     onClick="return confirm('This operation may take more than 2 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Delete Empty Concepts'; return true;"
	     onMouseOut="window.status=''; return true;">Delete Empty Concepts</a></font></td>
    <td valign="top"><font size="-2">Run delete empty concepts patch script. Safe to run during editing
	     (may cause slowness).</font></td>
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=resolve_nec"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Resolve NEC'; return true;"
	     onMouseOut="window.status=''; return true;">Resolve NEC</a></font></td>
    <td valign="top"><font size="-2">Identify all cases of ambiguous NEC atoms and fix up the PNs to reflect the current versions of the various sources involved.  Remove any MTH/PN atoms referring to obsolete sources. Safe
             to run during editing (may cause slowness).</font></td>
 </tr>
 <tr>
    <td width="30%" valign="top"><font size="-1"><a href="$cgi?$running_state&command=set_ranks"
	     onClick="return confirm('This operation may take more than 15 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Set ranks'; return true;"
	     onMouseOut="window.status=''; return true;">Set ranks</a></font></td>
    <td valign="top"><font size="-2">Set rank field in the core tables. <b>DO NOT RUN</b> during editing.</font></td>
 </tr>
</table>
</center>

<p>
    };
}; # end PrintINDEX

##############################################################################
# Research max_tab locks
#
sub PrintCHECK_LOCKS {

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  $sh = $dbh->prepare(q{
select b.object_name, b.object_id,
       a.locked_mode,  
       c.sid as session_id, 
       serial#, 
       username, 
       c.osuser, 
       c.process as process_id,
       c.machine as client_machine, 
       c.program as connection_program,
       d.sql_text as current_query, 
       f.ctime as time_since_try_to_acquire,
       DECODE(event,'enqueue','waiting','holds_lock')
from v$locked_object a, user_objects b, v$session c,
  v$sql d, v$session_wait e, v$lock f
where a.object_id=b.object_id
  and a.session_id = c.sid
  and sql_address = d.address (+)  
  and nvl(users_executing,1) > 0
  and f.id1 = b.object_id
  and f.sid = c.sid
  and e.sid = c.sid
  }) ||  ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||  ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
     &&  return);

  while (@f = $sh->fetchrow_array) {
    ($name, $id, $lm, $sid, $serial,$user,$osuser,$process, $machine,
     $program, $query, $time, $mode) = @f;

    if ($mode eq "holds_lock") {
      @{$lock{$name}} = @f;
    } else {
      @{$wait{$name}->[scalar(@{$wait{$name}})]} = @f;
    }
  }

  print "<blockquote><pre>\n";
  foreach $resource (sort keys %lock) {

    if ($afterfirst) {
      print "<hr width=100%>";
    } else {
      $afterfirst=1;
    }

    ($name, $id, $lm, $sid, $serial,$user,$osuser,$process, $machine,
     $program, $query, $time, $mode) = @{$lock{$resource}};

    print qq{\nThe following session is holding a lock on $name

  Resource        $name
  Wait Time       $time Seconds 
  Session ID      $sid
  Serial #        $serial 
    (<a href="$cgi?state=KILL&sid=$sid&serial=$serial&db=$db"
     onMouseOver="window.status='Kill session'; return true;"
     onMouseOut="window.status=''; return true;">ALTER SYSTEM KILL SESSION '$sid, $serial'</a>)
  DB User         $user
  OS User         $osuser
  Process ID      $process
  Client Machine  $machine
  Client Program  $program
  Current Query:  <blockquote><table><tr width="60%"><td>$query</td></tr></table></blockquote>\n\n};
    
    if (scalar(@{$wait{$resource}}) > 0) {

      print "  Sessions WAITING for lock on $resource\n\n";

      for ($i = 0; $i < scalar(@{$wait{$resource}}); $i++) {
	($name, $id, $lm, $sid, $serial,$user,$osuser,$process, $machine,
	 $program, $query, $time, $mode) = @{$wait{$resource}->[$i]};

	print qq{    Resource        $name
    Wait Time       $time Seconds 
    Session ID      $sid
    Serial #        $serial 
    DB User         $user
    OS User         $osuser
    Process ID      $process
    Client Machine  $machine
    Client Program  $program
    Current Query:  $query\n\n};
      }

    }
  }

  unless ($afterfirst) { print "<b>NO LOCKS are being held.</b>"; }

  print "</blockquote></pre>\n";

         
  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}

##############################################################################
# List most recently inserted sources
#
sub PrintLIST_SOURCES {

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  $sh = $dbh->prepare(q{
select distinct source, insertion_date
from classes where insertion_date in
  (select insertion_Date from 
    (select insertion_date from classes
     where source not in ('SRC','MTH')
     group by insertion_date having count(*)>4
     order by 1 desc)
    where insertion_date is not null and rownum<6)
  and source not in ('SRC','MTH')
order by 2 desc
  }) ||  ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
	  &&  return);
  $sh->execute ||  ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
		    &&  return);
		      
  print qq{
<p>The following sources are the most recently inserted.</p>
<center><table border="0"><tr bgcolor="#ffffcc"><tr><th>Source</th><th>Insertion Date</th></tr>
 };

  while (@f = $sh->fetchrow_array) {
    ($source, $insertion_date) = @f;
    print qq{    <tr><td>$source</td><td>$insertion_date</td></tr>
};
  }

  print qq{
</table></center>
 };
         
  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}

##############################################################################
# List New ATN,SAB values
#
sub PrintLIST_ATN {

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  $sh = $dbh->prepare(q{
    SELECT DISTINCT attribute_name, b.source
    FROM attributes a, source_version b
    WHERE tobereleased in ('Y','y') and released = 'N'
      AND a.source = current_name
    MINUS 
    SELECT attribute_name, b.stripped_source
    FROM attributes a, source_rank b
    WHERE released != 'N' and a.source = b.source
  }) ||  ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
	  &&  return);
  $sh->execute ||  ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
		    &&  return);
		      
  print qq{
<p>The following attribute_name and source values are those
that are "new" with respect to the previous release.</p>
<center><table border="0"><tr bgcolor="#ffffcc"><tr><th>ATN</th><th>Source</th></tr>
 };

  while (@f = $sh->fetchrow_array) {
    ($atn,$source) = @f;
    print qq{    <tr><td>$atn</td><td>$source</td></tr>
};
  }

  print qq{
</table></center>
 };
         
  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}

###################### Procedure PrintKILL #########################
#
# Kills the specified session
#
sub PrintKILL {

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{ALTER SYSTEM KILL SESSION ?}, undef, '$sid, $serial') ||
    ((print L "<span id=red>Failed to kill '$sid, $serial' in $db ($DBI::errstr).</span>")
     &&  return);

  print qq{
<blockquote><pre>ALTER SYSTEM KILL SESSION '$sid, $serial';</pre><br>
Session killed.</blockquote>
};
  $dbh->disconnect;
}
###################### Procedure PrintRUNNING ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintRUNNING {

  # 
  # Clean up all logs older than 2 months
  #
  &RemoveOldLogFiles;

  #
  # If no log name passed in create one & fork process
  #
  unless ($log_name){
    $date = `/bin/date +%Y%m%d`; chop($date);
    $log_name="$date.$command.$db.log";
    unlink "MIDLogs/$log_name";

  
    #
    # Here, we need to fork off the process & handle the various tasks
    # The task should write to $inc/MIDLogs/$log_name
    #
    # an error here means that the
    # MIDLogs directory is most likely not writeable by suresh
    #
    if ($pid = fork) {
      $parent = 1; 
#      print qq { The <i>$command</i> operation is running.
#		 This page will display the log and 
#		 refresh itself every 10 seconds until 
#		 the process finishes. };
#      print qq{</pre><script> setInterval("window.location.href='$cgi?state=RUNNING&command=$command&log_name=$log_name&db=$db'",10000)</script>}
    } elsif (defined $pid) {
      $child = 1;
      $| = 1;
      open(L,">MIDLogs/$log_name") || 
            ((print "<span id=red>Error opening $log_name.</span>") &&
             return);
      print L "------------------------------------------------------------\n";
      print L "Starting ... " . scalar(localtime). "\n";
      print L "------------------------------------------------------------\n";
      print L "MEME_HOME: $ENV{MEME_HOME}\n";
      print L "database: $db\n";
      $cmd = "Handle_$command";
      print L "command: $cmd\n";
      print L "\n";
      close (L);
      
      #
      # This is the child process, 
      #
      # Handle the task & write
      # log to $inc/MIDLogs/$log_name
      #
      if (defined(&$cmd)) {
	&$cmd;
      } else {
	$notfound=1;
      }

      open(L,">>MIDLogs/$log_name") || 
            ((print "<span id=red>Error opening $log_name.</span>") &&
             return);
      print L "   Command Not Found\n" if $notfound;
      print L "------------------------------------------------------------\n";
      print L "Finished ... " . scalar(localtime). "\n";
      print L "------------------------------------------------------------\n";
      close (L);

    } else {
      print qq{ <span id=red>Cannot fork maintenance operation!: $!</span><p>};
      return;
    }

  } # unless ($log_name)
  
  #
  # This happens if the script is called with a log name
  # or it is the parent process of the fork
  #
  unless ($child) {

   
    #
    # Open log file & display what is left.
    # if there is an error, we are finished.
    #
    sleep 1;
    `chmod 666 MIDLogs/$log_name`;
    open (F,"MIDLogs/$log_name") || die "couldn't open file: $log_name\n";
    @lines = <F>;
    close(F);
    $finished=0;

    foreach $line(@lines){
	$finished=1 if $line =~ /Finished/;
	$error=1 if $line =~ /ERROR/;
    }
    
    #
    # Handle error case
    #
    if ($error) {
	print qq{ <span id=red>There was an error running the maintenance operation..  
		  The log that was produced appears below:</span><p><pre>
};
	print join("/n",@lines),"/n";
	print qq{</pre>};
      } elsif ($finished){
	print qq{ The maintenance operation is finished.
		  Click <a href="$cgi?state=REVIEW_LOG&db=$db&log_name=$log_name"> here</a> to see the log.
		};
      } else {
	print qq { 
		  <blockquote>
		   The <i>$command</i> operation is running. The resulting log is displayed below.  
		   This page will refresh itself every 10 seconds until the process finishes.
		   If you decide to leave this site after running this procedure, the script 
		   will continue to run and you can view the log later simply by visiting the 
		   <a href="$cgi?state=REVIEW_LOGS">Review Logs</a> link, which can be accessed 
                   from the Main Index page<p><pre>
};

	print @lines,"\n";
	print qq{
		  </blockquote>
		 </pre><script> setInterval("window.location.href='$cgi?state=RUNNING&command=$command&log_name=$log_name&db=$db'",10000)</script>}

      };
  } 

}; # end PrintRUNNING

##############################################################################
# Set preferred atom_ids in concept_status
# lock max_tab to prevent actions from taking place while it runs
#
sub Handle_set_preference {
  $| = 1;
  
  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;
  
  #
  # The following code is from $MEME_HOME/Patch/scd_complete_graphs.csh
  #
  
  print L "    Call MEME_RANKS.set_preference ... ".scalar(localtime)."\n";

  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
  }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);
  
  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
  }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);
  
  $sh = $dbh->prepare(qq{
    BEGIN 
	MEME_RANKS.set_preference; 
    END; }) ||
    ((print L "<span id=red>Error preparing query 2($DBI::errstr).</span>") &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing query 2($DBI::errstr).</span>") &&  return);
          
  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}


##############################################################################
# Remove temporary table
#
sub Handle_remove_temporary_tables {
  $| = 1;
  
  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;
  
  #
  # The following code will remove temporary tables.
  #
  
  print L "    Call MEME_SYSTEM.cleanup_temporary_tables ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare(qq{
    BEGIN 
	MEME_SYSTEM.cleanup_temporary_tables('t\_');
	MEME_SYSTEM.cleanup_temporary_tables('qat\_');
	MEME_SYSTEM.cleanup_temporary_tables('tmp\_');
    END; }) ||
    ((print L "<span id=red>Error preparing query 1($DBI::errstr).</span>") &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing query 1($DBI::errstr).</span>") &&  return);
          
  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}


##############################################################################
# Fix concept reports
#
sub Handle_fix_concept_reports {
  $| = 1;
  
  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);
  
  #
  # The following code is from $MEME_HOME/Patch/scd_complete_graphs.csh
  #
  
  print L "    Call MEME_RANKS.set_preference ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare(qq{
    BEGIN 
	MEME_RANKS.set_preference; 
    END; }) ||
    ((print L "<span id=red>Error preparing query 2($DBI::errstr).</span>") &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing query 2($DBI::errstr).</span>") &&  return);
          
  #
  # The following code will fix concept reports.
  #

  print L "    Find bad relationships (1) ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq} AS
	SELECT relationship_id as row_id
	FROM relationships WHERE relationship_level = 'S' AND (concept_id_1, atom_id_1) IN
         (SELECT concept_id_1, atom_id_1 FROM relationships WHERE relationship_level = 'S'
         MINUS 
	 SELECT concept_id, atom_id FROM classes)
	UNION
	SELECT relationship_id as row_id
	FROM relationships WHERE relationship_level = 'C' AND (concept_id_1) IN
         (SELECT concept_id_1 FROM relationships WHERE relationship_level = 'C'
         MINUS 
	 SELECT concept_id FROM classes)
    }) ||
    ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}
    }) ||
    ((print L "<span id=red>Error preparing count 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 1 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";
  # Remove bad relationships (1)
  if ($row_ct > 0) {
    print L "    Remove bad relationships (1) ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'R',
                authority => 'MTH',
                table_name => '${uniq}',
                work_id => 0,
                status => 'R');
            MEME_UTILITY.drop_it('table','${uniq}');			    
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 1 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print L "<span id=red>Error executing batch action 1 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }

  print L "    Find bad relationships (2) ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq} AS
	SELECT relationship_id as row_id
	FROM relationships WHERE relationship_level = 'S' AND (concept_id_2, atom_id_2) IN
         (SELECT concept_id_2, atom_id_2 FROM relationships WHERE relationship_level = 'S'
         MINUS 
	 SELECT concept_id, atom_id FROM classes)
	UNION
	SELECT relationship_id as row_id
	FROM relationships WHERE relationship_level = 'C' AND (concept_id_2) IN
         (SELECT concept_id_2 FROM relationships WHERE relationship_level = 'C'
         MINUS 
	 SELECT concept_id FROM classes)
    }) ||
    ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}
    }) ||
    ((print L "<span id=red>Error preparing count 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 2 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";
  # Remove bad relationships (2)
  if ($row_ct > 0) {
    print L "    Remove bad relationships (2) ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'R',
                authority => 'MTH',
                table_name => '${uniq}',
                work_id => 0,
                status => 'R');
            MEME_UTILITY.drop_it('table','${uniq}');			    
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 2 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print L "<span id=red>Error executing batch action 2 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }

  print L "    Find bad attributes (1) ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}'); END;") ||
    ((print L "<span id=red>Error preparing drop 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 3 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq} AS
	SELECT attribute_id as row_id
	FROM attributes WHERE attribute_level = 'S' AND (concept_id, atom_id) IN
         (SELECT concept_id, atom_id FROM attributes WHERE attribute_level = 'S'
         MINUS 
	 SELECT concept_id, atom_id FROM classes)
	UNION
	SELECT attribute_id as row_id
	FROM attributes WHERE attribute_level = 'C' AND (concept_id) IN
         (SELECT concept_id FROM attributes WHERE attribute_level = 'C'
         MINUS 
	 SELECT concept_id FROM classes)
    }) ||
    ((print L "<span id=red>Error executing create 3 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}
    }) ||
    ((print L "<span id=red>Error preparing count 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 3 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";
  # Remove bad attributes (1)
  if ($row_ct > 0) {
    print L "    Remove bad attributes (1) ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'A',
                authority => 'MTH',
                table_name => '${uniq}',
                work_id => 0,
                status => 'R');
            MEME_UTILITY.drop_it('table','${uniq}');			    
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 3 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print L "<span id=red>Error executing batch action 3 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }

  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}


##############################################################################
# Run matrix initializer
#
sub Handle_matrixinit {
  $| = 1;
  
  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  open(CMD,"$ENV{MEME_HOME}/bin/matrixinit.pl -I $db | sed 's/^/      /' |") ||
    ((print L "<span id=red>Error running matrixinit.pl ($! $?).</span>")
     &&  return);
  while (<CMD>) {
    print L $_;
  }	
  close(CMD);
  close(L);
  return 1;
}

##############################################################################
# Run matrix updater
#
sub Handle_matrixupdate {
  $| = 1;
  
  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  open(CMD,"$ENV{MEME_HOME}/bin/matrixinit.pl -U -rCATCHUP $db | sed 's/^/      /' |") ||
    ((print L "<span id=red>Error running matrixinit.pl ($! $?).</span>")
     &&  return);
  while (<CMD>) {
    print L $_;
  }	
  close(CMD);
  close(L);
  return 1;
}


##############################################################################
# Run MM/TM Management
#
sub Handle_mthtm_remove {
  $| = 1;

  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  open(CMD,"$ENV{MEME_HOME}/bin/mthtm.pl $host $port -d MERGED $db MTH | sed 's/^/      /' |") ||
    ((print L "<span id=red>Error running mthtm.pl ($! $?).</span>")
     &&  return);
  while (<CMD>) {
    print L $_;
  }	
  close(CMD);
  close(L);
  &Handle_set_preference;
  return 1;
}

##############################################################################
# Run MM/TM Management
#
sub Handle_mthtm_insert {
  $| = 1;

  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  open(CMD,"$ENV{MEME_HOME}/bin/mthtm.pl $host $port -t MTH/TM -s R -i $db MTH | sed 's/^/      /' |") ||
    ((print L "<span id=red>Error running mthtm.pl ($! $?).</span>")
     &&  return);
  while (<CMD>) {
    print L $_;
  }	
  close(CMD);
  close(L);
  return 1;
}

##############################################################################
# Run MTH/PT patch script
#
sub Handle_mthpt {
  $| = 1;

  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # The following code is from $MEME_HOME/Patch/mth_pt.csh converted into perl
  #
  print L "    Identify Dangling MTH/MM atoms ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t1'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t1 AS
        SELECT UPPER(atom_name) AS string,concept_id,a.atom_id
        FROM classes a, atoms b
        WHERE tobereleased IN ('Y','y')
        AND source='MTH' AND termgroup='MTH/MM'
        AND a.atom_id=b.atom_id
    }) ||
    ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        UPDATE ${uniq}_t1
        SET string=SUBSTR(string,0,LENGTH(string)-4)
        WHERE string LIKE '% <_>'
    }) ||
    ((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        UPDATE ${uniq}_t1 SET string=SUBSTR(string,0,LENGTH(string)-5)
        WHERE string LIKE '% <__>'
    }) ||
    ((print L "<span id=red>Error executing update 1.2 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("BEGIN MEME_SYSTEM.analyze('${uniq}_t1'); END;") ||
    ((print L "<span id=red>Error preparing analyze 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing analyze 1 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t2'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t2 AS
        SELECT UPPER(b.atom_name) AS string, a.concept_id, c.atom_id
        FROM classes a, atoms b, ${uniq}_t1 c
        WHERE a.atom_id=b.atom_id
        AND termgroup !='MTH/MM' 
        AND tobereleased IN ('Y','y')
        AND a.concept_id=c.concept_id
    }) ||
    ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("BEGIN MEME_SYSTEM.analyze('${uniq}_t2'); END;") ||
    ((print L "<span id=red>Error preparing analyze 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing analyze 2 ($DBI::errstr).</span>")
     &&  return);

  # Find the cases of dangling mms
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_dangling_mms'); END;") ||
    ((print L "<span id=red>Error preparing drop 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 3 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_dangling_mms AS
        SELECT DISTINCT string,concept_id,atom_id FROM ${uniq}_t1
        MINUS SELECT string,concept_id,atom_id FROM ${uniq}_t2
    }) ||
    ((print L "<span id=red>Error executing create 3 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("BEGIN MEME_SYSTEM.analyze('${uniq}_dangling_mms'); END;") ||
    ((print L "<span id=red>Error preparing analyze 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing analyze 3 ($DBI::errstr).</span>")
     &&  return);

  print L "    Prepare MTH/PT atoms ... ".scalar(localtime)."\n";
  # prepare to insert new atoms
  # Find the cases of dangling mms
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t_classes'); END;") ||
    ((print L "<span id=red>Error preparing drop 4 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 4 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t_classes AS
        SELECT concept_id,a.atom_id,atom_name,termgroup,source,code,
           status,generated_status,released,tobereleased, suppressible
        FROM classes a,atoms WHERE 1=0
    }) ||
    ((print L "<span id=red>Error executing create 4 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        INSERT INTO ${uniq}_t_classes
        SELECT c.concept_id,0,min(atom_name),'MTH/PT','MTH','0',
	   'R','Y','N','Y','N'
        FROM classes a, atoms b, ${uniq}_dangling_mms c
        WHERE UPPER(b.atom_name)=string
        AND a.atom_id = b.atom_id
        AND a.concept_id=c.concept_id
        GROUP BY c.concept_id
    }) ||
    ((print L "<span id=red>Error executing insert 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        UPDATE ${uniq}_t_classes
        SET code = to_char(rownum)
    }) ||
    ((print L "<span id=red>Error executing update 2 ($DBI::errstr).</span>")
     &&  return);

  # Set U codes starting 1 higher than the previous max MTH/PT U code.
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t3'); END;") ||
    ((print L "<span id=red>Error preparing drop 5 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 5 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t3 AS
        SELECT TO_NUMBER(MAX(SUBSTR(code,INSTR(code,'U')+1))) AS max_code
        FROM classes WHERE termgroup='MTH/PT' AND source='MTH'
        AND code LIKE 'U%'
    }) ||
    ((print L "<span id=red>Error executing create 5 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        UPDATE ${uniq}_t_classes
        SET code = (SELECT 'U'|| LPAD(max_code+TO_NUMBER(code),6,0) FROM ${uniq}_t3)
    }) ||
    ((print L "<span id=red>Error executing update 3 ($DBI::errstr).</span>")
     &&  return);

  #
  # Insert the new atoms
  #
  print L "    Insert MTH/PT atoms ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_t_classes
    }) ||
    ((print L "<span id=red>Error preparing count 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 1 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";

  if ($row_ct > 0) {
    open(CMD,"$ENV{MEME_HOME}/bin/insert.pl $host $port -atoms ${uniq}_t_classes $db MTH | sed 's/^/      /' |") ||
      ((print L "<span id=red>Error running insert.pl ($! $?).</span>")
       &&  return);
    while (<CMD>) {
      print L $_;
    }	
    close(CMD);
  }

  #
  # Cleanup
  #
  print L "    Cleanup ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    BEGIN
      MEME_UTILITY.drop_it('table', '${uniq}_t_classes');
      MEME_UTILITY.drop_it('table', '${uniq}_dangling_mms');
      MEME_UTILITY.drop_it('table', '${uniq}_t1');
      MEME_UTILITY.drop_it('table', '${uniq}_t2');
      MEME_UTILITY.drop_it('table', '${uniq}_t3');
    END;}) ||
    ((print L "<span id=red>Error preparing drop 6 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 6 ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}

##############################################################################
# Run delete duplicate concept relationships patch script
sub Handle_delete_dup_crels {
  $| = 1;

  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # The following code is from $MEME_HOME/Patch/delete_dup_concept_rels.csh converted into perl
  #
  print L "    Identify C level relationships ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t1'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  print L "    Canonicalize relationships ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        CREATE TABLE ${uniq}_t1 AS
        SELECT concept_id_1, concept_id_2, relationship_id, rank
        FROM relationships WHERE relationship_level='C'
    }) ||
    ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        UPDATE ${uniq}_t1
        SET concept_id_1 = concept_id_2, concept_id_2 = concept_id_1
        WHERE concept_id_1 > concept_id_2
    }) ||
    ((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t2'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  print L "    Find duplicates id_1,id_2 ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        CREATE TABLE ${uniq}_t2 AS 
	SELECT concept_id_1, concept_id_2 FROM ${uniq}_t1
        GROUP BY concept_id_1, concept_id_2 
        HAVING COUNT(DISTINCT relationship_id) > 1
    }) ||
    ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  # get all rels involved
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t3'); END;") ||
    ((print L "<span id=red>Error preparing drop 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 3 ($DBI::errstr).</span>")
     &&  return);

  print L "    Find lowest ranking duplicate relationships ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        CREATE TABLE ${uniq}_t3 AS 
	SELECT * FROM ${uniq}_t1 WHERE (concept_id_1,concept_id_2) 
	IN (SELECT concept_id_1, concept_id_2 FROM ${uniq}_t2)
    }) ||
    ((print L "<span id=red>Error executing create 3 ($DBI::errstr).</span>")
     &&  return);

  # get rels to keep
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t4'); END;") ||
    ((print L "<span id=red>Error preparing drop 4 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 4 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t4 AS 
        SELECT TO_NUMBER(SUBSTR(max_rank, INSTR(max_rank,'/')+1)) AS row_id
        FROM
        (SELECT MAX(rank||'/'||LPAD(relationship_id,10,0)) AS max_rank
        FROM ${uniq}_t3 GROUP BY concept_id_1, concept_id_2)
    }) ||
    ((print L "<span id=red>Error executing create 4 ($DBI::errstr).</span>")
     &&  return);

  # get rels to delete
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t5'); END;") ||
    ((print L "<span id=red>Error preparing drop 5 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 5 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t5 AS SELECT relationship_id AS row_id
        FROM ${uniq}_t3 MINUS SELECT row_id FROM ${uniq}_t4
    }) ||
    ((print L "<span id=red>Error executing create 5 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_t5
    }) ||
    ((print L "<span id=red>Error preparing count 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 1 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";

  # Delete them!
  if ($row_ct > 0) {
    print L "    Delete duplicate relationships ... ",scalar(localtime),"\n";
    open(CMD,"$ENV{MEME_HOME}/bin/batch.pl $port $host -a=D -t=R -s=t ${uniq}_t5 $db MTH | /bin/sed 's/^/      /' |") ||
      ((print L "<span id=red>Error running mthtm.pl ($! $?).</span>")
       &&  return);
    while (<CMD>) {
      print L $_;
    }	
    close(CMD);
  }

  #
  # Cleanup
  #
  print L "    Cleanup ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    BEGIN
      MEME_UTILITY.drop_it('table', '${uniq}_t1');
      MEME_UTILITY.drop_it('table', '${uniq}_t2');
      MEME_UTILITY.drop_it('table', '${uniq}_t3');
      MEME_UTILITY.drop_it('table', '${uniq}_t4');
      MEME_UTILITY.drop_it('table', '${uniq}_t5');
    END;}) ||
    ((print L "<span id=red>Error preparing drop 6 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 6 ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}

##############################################################################
# Run delete empty concept patch script
sub Handle_delete_empty_concepts {
  $| = 1;

  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;
  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # The following code is from $MEME_HOME/Patch/delete_empty_concept.csh converted into perl
  #
  print L "    Find empty concepts ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tbac'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_tbac AS
        SELECT DISTINCT concept_id AS row_id FROM concept_status
        MINUS SELECT concept_id FROM classes
    }) ||
    ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_tbac
    }) ||
    ((print L "<span id=red>Error preparing count 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 1 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";
  # Delete concepts
  if ($row_ct > 0) {
    print L "    Delete empty concepts ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'CS',
                authority => 'MTH',
                table_name => '${uniq}_tbac',
                work_id => 3192,
                status => 'R');
            MEME_UTILITY.drop_it('table','${uniq}_tbac');			    
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 1 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print L "<span id=red>Error executing batch action 1 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }

  print L "    Find attributes attached to empty concepts ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tbac2'); END;") ||
    ((print L "<span id=red>Error preparing drop 11 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 11 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_tbac2 AS
        SELECT DISTINCT attribute_id AS row_id FROM attributes
	WHERE concept_id IN (SELECT concept_id FROM dead_concept_status)
          AND attribute_level = 'C'
    }) ||
    ((print L "<span id=red>Error executing create 11 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_tbac2
    }) ||
    ((print L "<span id=red>Error preparing count 11 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 11 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";

  # Delete attributes
  if ($row_ct > 0) {
    print L "    Delete attributes attached to empty concepts ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'A',
                authority => 'MTH',
                table_name => '${uniq}_tbac2',
                work_id => 3192,
                status => 'R');
            MEME_UTILITY.drop_it('table','${uniq}_tbac2');			    
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 11 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print L "<span id=red>Error executing batch action 11 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }


  print L "    Find relationships attached to empty concepts ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tbac2'); END;") ||
    ((print L "<span id=red>Error preparing drop 12 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 12 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_tbac2 AS
        SELECT DISTINCT relationship_id AS row_id FROM relationships
	WHERE concept_id_2 IN (SELECT concept_id FROM dead_concept_status)
          AND relationship_level = 'C'
        UNION
        SELECT DISTINCT relationship_id FROM relationships
	WHERE concept_id_1 IN (SELECT concept_id FROM dead_concept_status)
          AND relationship_level = 'C'
    }) ||
    ((print L "<span id=red>Error executing create 12 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_tbac2
    }) ||
    ((print L "<span id=red>Error preparing count 12 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 12 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";

  # Delete relationships
  if ($row_ct > 0) {
    print L "    Delete relationships attached toempty concepts ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'R',
                authority => 'MTH',
                table_name => '${uniq}_tbac2',
                work_id => 3192,
                status => 'R');
            MEME_UTILITY.drop_it('table','${uniq}_tbac2');			    
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 11 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print L "<span id=red>Error executing batch action 11 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }


  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}

##############################################################################
# Run resolves nec patch script
sub Handle_resolve_nec {
  $| = 1;

  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);


  #
  # The following code is from $MEME_HOME/Patch/resolves_nec.csh converted into perl
  #
  print L "    Find obsolete NEC atoms ... ".scalar(localtime)."\n";

  #
  # 1. Remove any 'NEC in %' atoms where the % is
  #    not a current source (e.g. MDR50).
  #
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_1'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_1 AS
        SELECT /*+ RULE */ b.atom_id AS row_id 
        FROM 
	     (SELECT DISTINCT atom_id+0 AS atom_id, atom_name||'' AS atom_name 
              FROM atoms WHERE atom_name LIKE '% NEC %in %') b, 
          classes c
        WHERE b.atom_id = c.atom_id
          AND c.source = 'MTH'
          AND termgroup = 'MTH/PN'
          AND SUBSTR(atom_name,INSTR(atom_name,' ',-1)+1) NOT IN
          (SELECT current_name FROM source_version)
    }) ||
    ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_1
    }) ||
    ((print L "<span id=red>Error preparing count 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 1 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";

  #
  # Delete them!
  #
  if ($row_ct > 0) {
    print L "    Delete obsolete NEC atoms ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'C',
                authority => 'MAINTENANCE',
                table_name => '${uniq}_1',
                work_id => 0,
                status => 'R',
                set_preferred_flag => 'Y');
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 1 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print L "<span id=red>Error executing batch action 1 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }

  #
  # 2. Prepare 'NEC in %' atoms for all ambiguous NEC atoms 
  #    where % is the current version of the source belonging 
  #    to the NEC atom, and the NEC atom is ambiguous.
  #
  #  We identify an NEC atom as having an atom name like one of these:
  #    '% NEC'
  #    '% NEC (%)'
  #    '% not elsewhere classified'
  #    '% NEC, %'
  #
  print L "    Find ambiguous NEC atom cases ... ",scalar(localtime),"\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_1'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_1 AS
        SELECT concept_id,a.atom_id,atom_name,termgroup,
          source,code,status,generated_status,released,
          tobereleased, suppressible,isui, source_aui, source_cui, source_dui
        FROM classes a, atoms WHERE 1=0
    }) ||
    ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Get atoms based on 'NEC' and 'not elsewhere classified' 
  # THIS CODE ASSUMES THAT THERE IS A SPACE BEFORE NEC!!!
  #
  $dbh->do(qq{
        INSERT INTO ${uniq}_1
	   (concept_id, atom_id, atom_name, termgroup, source, code,
	    status, generated_status, released, tobereleased, suppressible, isui)
        SELECT /*+ RULE */ concept_id, a.atom_id,
           pre || ' NEC' || post || ' in ' || current_name,
           'MTH/PN','MTH','NOCODE', 'R','Y','N','Y','N', isui
        FROM
          (SELECT DISTINCT /*+ RULE */ concept_id, a.isui,
                 b.atom_id, a.source,
  	            SUBSTR(atom_name,0,INSTR(atom_name,' NEC')-1) AS pre,
  	            SUBSTR(atom_name,INSTR(atom_name,' NEC')+4) AS post
           FROM classes a, atoms b
           WHERE a.source IN (SELECT current_name FROM source_version)
             AND source != 'MTH'
	     AND tobereleased IN ('Y','y')
	     AND a.atom_id = b.atom_id
	     AND atom_name LIKE '% NEC%'
             AND (atom_name LIKE '% NEC' OR
	          atom_name LIKE '% NEC (%)' OR
	          atom_name LIKE '% NEC, %')
           UNION
           SELECT DISTINCT /*+ RULE */ concept_id, a.isui,
                 b.atom_id, a.source,
  	            SUBSTR(atom_name,0,INSTR(LOWER(atom_name),
			    ' not elsewhere classified')-1) AS pre,
  	            SUBSTR(atom_name,INSTR(LOWER(atom_name),
			    ' not elsewhere classified')+25) AS post
           FROM classes a, atoms b
           WHERE a.source IN (SELECT current_name FROM source_version)
             AND source != 'MTH'
	     AND tobereleased IN ('Y','y')
	     AND a.atom_id = b.atom_id
             AND LOWER(atom_name) LIKE '% not elsewhere classified%'
          ) a, source_version b
        WHERE a.source = current_name
    }) ||
    ((print L "<span id=red>Error executing insert 1 ($DBI::errstr).</span>")
     &&  return);

  #
  # Remove all cases for concepts that are not ambiguous
  #
  print L "    DO Remove all cases for concepts that are not ambiguous ... ", scalar(localtime),"\n";
  $dbh->do(qq{
        DELETE FROM ${uniq}_1 a WHERE (concept_id) IN
        (SELECT concept_id FROM ${uniq}_1
         MINUS
         SELECT concept_id FROM ${uniq}_1 a,
          (SELECT isui FROM classes 
           WHERE tobereleased IN ('Y','y')
           GROUP BY isui HAVING COUNT(DISTINCT concept_id)>1) b
         WHERE a.isui = b.isui)
    }) ||
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);

  #
  # 3. If there are multiple NEC atoms we keep the PN associated with
  #    only the highest ranking NEC atom in the concept.
  #
  # We changed this so now the rank isn't the real rank but
  # is the length of the atom!
  #
  print L "    Resolve multiple NEC atom case ... ",scalar(localtime),"\n";
  $dbh->do(qq{
        DELETE FROM ${uniq}_1 WHERE atom_id IN
          (SELECT atom_id FROM ${uniq}_1
           MINUS
           SELECT TO_NUMBER(SUBSTR(max_rank,INSTR(max_rank,'/')+1)) AS id FROM
            (SELECT concept_id,MAX(LPAD(LENGTH(atom_name),5,'0')||'/'||atom_id) max_rank
	     FROM ${uniq}_1 GROUP BY concept_id))
    }) ||
    ((print L "<span id=red>Error executing delete 2 ($DBI::errstr).</span>")
     &&  return);

  # Remove all cases for concepts where there is
  # already an existing, matching PN atom.
  $dbh->do(qq{
        DELETE FROM ${uniq}_1 a WHERE concept_id IN
        (SELECT a.concept_id FROM ${uniq}_1 a, atoms b, classes c
         WHERE b.atom_id = c.atom_id
           AND c.source = 'MTH' AND c.termgroup='MTH/PN'
           AND a.atom_name = b.atom_name
           AND a.concept_id = c.concept_id)
    }) ||
    ((print L "<span id=red>Error executing delete 3 ($DBI::errstr).</span>")
     &&  return);

  #     
  # 4. If the respective concepts already have MTH/PN atoms REMOVE THEM
  #    if they are not in the form 'NEC in %' because they are incorrect.
  #    PNs disambiguating NEC concepts must be 'NEC in %'.
  #
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_2'); END;") ||
    ((print L "<span id=red>Error preparing drop 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 3 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_2 AS
        SELECT atom_id AS row_id
        FROM classes WHERE source = 'MTH' AND termgroup = 'MTH/PN'
        AND concept_id IN 
          (SELECT concept_id FROM ${uniq}_1)
    }) ||
    ((print L "<span id=red>Error executing create 3 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_2
    }) ||
    ((print L "<span id=red>Error preparing count 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 1 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";

  # Delete them!
  if ($row_ct > 0) {
    print L "    Delete bad existing MTH/PN atoms ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'C',
                authority => 'MAINTENANCE',
                table_name => '${uniq}_2',
                work_id => 0,
                status => 'R',
                set_preferred_flag => 'Y');
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 2 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print L "<span id=red>Error executing batch action 2 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_2'); END;") ||
    ((print L "<span id=red>Error preparing drop 4 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing drop 4 ($DBI::errstr).</span>")
     &&  return);
    
  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_1
    }) ||
    ((print L "<span id=red>Error preparing count 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 1 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Count == $row_ct\n";

  if ($row_ct > 0) {
    print L "    Insert new PN atoms.\n";
    open(CMD,"$ENV{MEME_HOME}/bin/insert.pl $host $port -atoms ${uniq}_1 $db MAINTENANCE | sed 's/^/      /' |") ||
      ((print L "<span id=red>Error running mthtm.pl ($! $?).</span>")
       &&  return);
    while (<CMD>) {
      print L $_;
    }	
    close(CMD);
  }

  #
  # Cleanup
  #
  print L "    Cleanup ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    BEGIN
      MEME_UTILITY.drop_it('table', '${uniq}_1');
      MEME_UTILITY.drop_it('table', '${uniq}_2');
    END;}) ||
    ((print L "<span id=red>Error preparing drop 5 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 5 ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}

##############################################################################
# Run set ranks
sub Handle_set_ranks {
  $| = 1;

  open(L,">>MIDLogs/$log_name") || 
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer;

  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  print L "    Set ranks 'C'... ".scalar(localtime)."\n";

  $sh = $dbh->prepare( qq{
    BEGIN
      MEME_RANKS.set_ranks(
         classes_flag => 'Y',
         attributes_flag => 'N',
         relationships_flag => 'N');
    END;
    }) ||
    ((print L "<span id=red>Error preparing set ranks 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing set ranks 1 ($DBI::errstr).</span>")
     &&  return);

  print L "    Set ranks 'R'... ".scalar(localtime)."\n";

  $sh = $dbh->prepare( qq{
    BEGIN
      MEME_RANKS.set_ranks(
         classes_flag => 'N',
         attributes_flag => 'N',
         relationships_flag => 'Y');
    END;
    }) ||
    ((print L "<span id=red>Error preparing set ranks 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing set ranks 2 ($DBI::errstr).</span>")
     &&  return);

#  print L "    Set ranks 'A'... ".scalar(localtime)."\n";
#  $sh = $dbh->prepare( qq{
#    BEGIN
#      MEME_RANKS.set_ranks(
#         classes_flag => 'N',
#         attributes_flag => 'Y',
#         relationships_flag => 'N');
#    END;
#    }) ||
#    ((print L "<span id=red>Error preparing set ranks 3 ($DBI::errstr).</span>")
#     &&  return);
#  $sh->execute || 
#    ((print L "<span id=red>Error executing set ranks 3 ($DBI::errstr).</span>")
#     &&  return);
#
  print L "    Set preferred atoms ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_RANKS.set_preference(); END;") ||
    ((print L "<span id=red>Error preparing set preference 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print L "<span id=red>Error executing set preference 1 ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}

##############################################################################
# Enables DBMS_OUTPUT buffer
#
sub EnableBuffer {
  my($size) = @_;
  $size = 100000 unless $size;
  my($sh);
  $sh = $dbh->prepare(qq{
    BEGIN
      dbms_output.enable(?);
    END;});
  $sh->execute($size);
} # end EnableBuffer

##############################################################################
# Flushes DBMS_OUTPUT buffer
#
sub FlushBuffer {
  #prepare stmt
  my($sh);
  $sh = $dbh->prepare(q{
    BEGIN
      dbms_output.get_line(:line,:status);
    END;});
  #bind parms
  $sh->bind_param_inout(":line", \$line, 256);
  $sh->bind_param_inout(":status", \$status,38);
  
  # flush buffer
  do {
    $sh->execute;
    $line =~ s/^(.{1,65}).*$/$1/;
    $line = "$line..." if length($line)== 65;
    $retval .= "$line\n";
    #print "        $line\n" if $line;
  } while (!$status);
  return $retval;
} # end FlushBuffer

###################### Procedure PrintREVIEW_LOG ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintREVIEW_LOG {


    open(F,"MIDLogs/$log_name") || 
            ((print "<span id=red>Error opening $log_name.</span>") &&
             return);

    print qq{
    <blockquote>
     <i>Following is the log.</i>
	<pre>
};
    while (<F>) {
      print;
    }
    close(F);
    print qq{
	</pre>
    </blockquote>
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
<i>The following is a list of all logs:</i>
<br>&nbsp;
<blockquote>
<form action="$cgi" method="GET">
   <input type="hidden" name="state" value="REVIEW_LOG">
   <input type="hidden" name="db" value="$db">
   <select name="log_name" onChange="this.form.submit(); return true;">
      <option>---- SELECT A LOG ----</option>
};
    chdir "MIDLogs";
    @logs = `ls -t *.*.log`;
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


###################### Procedure RemoveOldLogFiles ######################
#
# Remove log files older than 2 months
#
sub RemoveOldLogFiles {

  #
  # 5184000 is the number of seconds in 60 days
  #
  ($d,$d,$d,$mday,$mon,$year) = localtime(time-5184000);
  $year += 1900;
  $mon += 1;
  $mon = "0$mon" if length($mon) == 1;
  $mday = "0$mday" if length($mday) == 1;
  # print "Removing log files on or before $year$mon$mday\n";
  opendir(D,"MIDLogs");
  @files = readdir(D);
  closedir(D);
  foreach $f (@files) {
    if ($f =~ /\.log$/ && $f le "$year$mon$mday.log") {
      #print "Removing $f\n";
      unlink "MIDLogs/$f";
    }
  }
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
 This CGI script is used to access Mid maintenance tools. 
 It takes CGI arguments in the standard form "key=value&key=value...". 

 Parmaters:

  state         :  This is an internal variable representing what
                   state the application is in.
  command       :  The particular maintenance operation to run
  db            :  Database name
  meme_home     :  Value of $MEME_HOME that the script should use.
  log_name      :  Parameter to states that review logs.  It should be
                   the name of a file in the MIDLogs subdirectory of the
		   directory containing the script.
             
  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


