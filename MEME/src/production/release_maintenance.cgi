#!@PATH_TO_PERL@
#
# File:     release_maintenance.cgi
# Author:   Brian Carlsen
#
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#
# Changes:
# 04/07/2006 BAC (1-AUN5X): atx_cui_map has more explicit handling of aui prefix/length for t_delcui_$$ query
# 04/03/2006 BAC (1-ATKBL): new Handle_aui_history query to handle case where
#      legacy CUI2 values are wrong (because last_release_cui was not inherited
#      because "safe replacment" atoms were inserted into different concepts.
# 03/30/2006 BAC (1-AIJVN): Rename "llr" to "lrr"
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
# 03/01/2006 TTN (1-AIJVN): Add assign last_release_rank
# 03/01/2006 BAC (1-7B9EP): DA, MR, ST ATUI computations improved
# 02/09/2006 BAC (1-742OL): fix prod mid cleanup for attributes.
# 02/03/2006 TTN (1-754X9): Extend AUI to 8 chars. Pad AUI to fixed length
# 01/24/2006 BAC (1-75BNZ): in prod_mid_cleanup, keep all sources whose
#    normalized_source is "current".  Also show obsolete sources when
#    performing cleanup (these should be reviewed)
# 01/17/2006 BAC (1-748Y3): Bug fix for Handle_sims_info (for MTH_UMLSMAPSETSEPARATOR)
# 01/16/2006 BAC (1-742OL): Bug fix for Handle_prod_mid_cleanup (rels section)
# 01/06/2005 BAC (1-72TD5): Bug fix for Handle_cui_history
# 01/03/2005 BAC (1-72FMT): Handle_set_imeta_rmeta now has a section to
#    deal with SABs like RXNORM which are updated more than once per release cycle.
#    It bases the computation on unreleasable SRC/VAB atoms.
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
#
# Version info:
# 05/23/2005 4.4.3: Added "CUI History","SIMS Info Maintenance", and
#                   "Set Release CUIs"
# 05/04/2005 4.4.2: Added AUI HISTORY section and "SetIMETA,RMETA section"
# 11/01/2004 4.4.1: Fix to NLM02 codes maintenance to preserve
#                   values across time.
# 06/10/2004 4.4.0: atui_rui section optimized to only load data
#                   for "new" attributes.
# 03/08/2004 4.3.0: Finalized ATX code
# 12/31/2003 4.2.1: Added section for handling ATXs though cui mapping
# 09/30/2003 4.2.0: Released
# 09/04/2003 4.1.1: mappings_for_dead_cuis -> cui_history updated
#                   for new MRCUI schema.
# 04/25/2003 4.1.0: First version (released)
#
$release = "4";
$version = "4.3";
$version_authority = "BAC";
$version_date = "06/23/2005";

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
  "state" => 1, "command" => 1, "db" => 1, "new_release" => 1,
  "old_release" => 1, "oracle_home" => 1,
  "meme_home" => 1, "log_name" => 1, "sid" => 1, "serial" => 1,
  "host" => 1, "port" => 1);

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
$uniq="t_$$";
$host = "-host $host" if $host;
$port = "-port $port" if $port;
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
	         ["PrintHeader","PrintINDEX","PrintFooter","Release Maintenance - Index Page","Release Maintenance"],
	     "RUNNING" =>
	         ["PrintHeader","PrintRUNNING","PrintFooter", "Running Release Maintenance: $command","Running Release Maintenance: $command"],
	     "KILL" =>
	         ["PrintHeader","PrintKILL","PrintFooter", "Killing Database Session","Killing Database Session: $sid, $serial"],
	     "CHECK_LOCKS" =>
	         ["PrintHeader","PrintCHECK_LOCKS","PrintFooter", "Research Database Locks","Research Database Locks"],
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
		return "$cgi?state=RUNNING&command=change_df&db=$db&meme_home=$meme_home&old_df=" +
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
	      <address>Contact: <a href="mailto:carlsen\@apelon.com">Brian A. Carlsen</a></address>
	      <address>Generated on:},scalar(localtime),qq{</address>
              <address>This page took $elapsed_time seconds to generate.</address>
	      <address>};
    &PrintVersion("version");
    print qq{</address>
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
    $dbs = "$dbs,mrd_mrd";
    $db_select_form = qq{
                                     <form name="indexform" action="$cgi">
			               <input type="hidden" name="state" value="INDEX">
			               <select name="db" onChange="this.form.submit(); return true;">
			                  <option>-- SELECT A DATABASE --</option>\n};
    foreach $db (sort split /,/, $dbs) {
      $db_select_form .= "			                  <option $selected{$db}>$db</option>\n";
      $databases{$db} = $db;
    }
    $db_select_form .= "			                </select>\n";

    print qq{

<i><div style="width:600">
 This page provides access to a standard set of processes
 which must be run before each release.  Please read the
 comments before running a tool. <b>DO NOT RUN</b> any
 of these tools while editing is taking place, only run
 after the MID has been frozen.</div></i>
<br>&nbsp;
<center>
<table border="1" cellspacing="2" cellpadding="2" width="90%" nosave="">
  <tr bgcolor="#ffffcc" nosave="">
    <td width="30%" valign="top"><b>Tool</b></td><td valign="top"><b>Description</b></td>
 </tr>
 <tr>
    <td width="30%" valign="top">Change Database</td>
    <td valign="top">$db_select_form.</td>
 </tr>
 <tr>
    <td width="30%" valign="top"><a href="$cgi?state=REVIEW_LOGS&db=$db" onMouseOver="window.status='Review Logs'; return true;"
	     onMouseOut="window.status=''; return true;">Review Logs</a></td>
    <td valign="top">Review logs from past maintenance operations.</td>
 </tr>
 <tr><td colspan="2"> &nbsp;</td></tr>

<!-- START FUNCTION ENTRIES HERE -->

 <!-- Assign ATUI, RUI for DA,MR,ST,MED<year>, and AQ/QB rels -->
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=atui_rui"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Assign ATUI,RUI'; return true;"
	     onMouseOut="window.status=''; return true;">Assign ATUI,RUI</a></td>
    <td valign="top">
	     AQ,QB relationships are built from attributes so they are not assigned
	     RUI values by the normal process.  We must perform this operation before
	     a release, to ensure that all AQ and QB rels which will be added to MRREL
	     will have properly assigned RUIs.  There is an analagous situation for
	     DA, MR, ST, AM, and MED attributes in MRSAT, so we assign ATUI values for
	     all combinations which will appear in MRSAT (based on the data here).
	     Run this AFTER CUI assignment</td>
 </tr>

 <!-- AUI history maintenance -->
 <tr>
    <td width="30%" valign="top"><a href="javascript:window.location.href='$cgi?$running_state&command=aui_history&new_release='+document.indexform.new_release1.value"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='AUI History'; return true;"
	     onMouseOut="window.status=''; return true;">AUI History</a></td>
    <td valign="top">
<table>
 <tr>
   <td>Enter next release version:</td>
   <td><input type="text" name="new_release1"></td>
 </tr>
</table>
	     Maintenance of AUI history information.  Currently generates facts
	     for AUI movement from previous release to the current.  Later it will
	     maintain safe replacement, birth, and death info as well. This data
	     will appear in MRAUI</td>
 </tr>

<!-- CUI History maintenance -->
 <tr>
    <td width="30%" valign="top"><a href="javascript:window.location.href='$cgi?$running_state&command=cui_history&old_release='+document.indexform.old_release1.value"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='CUI History'; return true;"
	     onMouseOut="window.status=''; return true;">CUI History</a></td>
    <td valign="top">
<table>
 <tr>
   <td>Enter previous release version:</td>
   <td><input type="text" name="old_release1"></td>
 </tr>
</table>
	     Maintenance of CUI history information.  Currently generates data
	     for merged CUIs, bequeathed CUIs, and retired CUIs.  Once complete
             the data is ready for use in MRCUI.
   </td>
 </tr>

 <!-- Compute last release rank -->
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=lrr"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Assign last_release_rank'; return true;"
	     onMouseOut="window.status=''; return true;">Assign last_release_rank</a></td>
    <td valign="top">
	     Assign last_release_rank based on rank, last_release_rank, sui and aui value</td>
 </tr>

 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=dead_cui_mappings"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Handle dead CUI mappings'; return true;"
	     onMouseOut="window.status=''; return true;">Dead CUI Mappings</a></td>
    <td valign="top">Merge data from cui_history and meow.mappings_for_dead_cuis in preparation
	     for building MRCUI. This process guarantees that the most editing work on dead CUIs is copied
	     into the cui_history table from which MRCUI is built.</td>
 </tr>

<!-- This is no longer needed
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=mthtm"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Manage MTH/MMs'; return true;"
	     onMouseOut="window.status=''; return true;">Manage MTH/MMs</a></td>
    <td valign="top">Remove all MTH/TM atoms, replace with MTH/MM where ambiguities still exist.
	     Also makes MTH/MM atoms unreleasable where ambiguity no longer exists.</td>
 </tr>
-->

 <!-- ATX CUI maintenance -->
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=atx_cui_map"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Map ATX through CUI history'; return true;"
	     onMouseOut="window.status=''; return true;">Map ATX through CUI history</a></td>
    <td valign="top">
	     When CUIs are merged, the ATX mappings must be updated to accommodate this fact.
	     Additionally, when CUIs are deleted, it will cause ATX rows to be removed.
	     This should be run before each release (after cui assignment?).</td>
 </tr>

 <!-- Prod Mid Cleanup -->
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=prod_mid_cleanup"
	     onClick="return confirm('This operation may take more than 1 hour.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Production MID Cleanup'; return true;"
	     onMouseOut="window.status=''; return true;">Production MID Cleanup</a></td>
    <td valign="top">Clean the database of references to old sources after
	     an end of year production run.</td>
 </tr>

<!-- NO LONGER USED
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=prepare_nlm02_codes"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Prepare NLM02 Codes'; return true;"
	     onMouseOut="window.status=''; return true;">Prepare NLM02 Codes</a></td>
    <td valign="top">Assign RX NLM02 codes for release.</td>
 </tr>
-->
 
 <!-- Set context type -->
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=set_context_type"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Set context type'; return true;"
	     onMouseOut="window.status=''; return true;">Set Context Type</a></td>
    <td valign="top">Set context type in sims_info.</td>
 </tr>

 <!-- Set official name -->
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=set_official_name"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Set official name'; return true;"
	     onMouseOut="window.status=''; return true;">Set Official Name</a></td>
    <td valign="top">Set the source official name and short name in sims_info.</td>
 </tr>

 <!-- IMETA, RMETA -->
 <tr>
    <td width="30%" valign="top"><a href="javascript:window.location.href='$cgi?$running_state&command=set_imeta_rmeta&new_release='+document.indexform.new_release2.value+'&old_release='+document.indexform.old_release2.value"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Set IMETA, RMETA'; return true;"
	     onMouseOut="window.status=''; return true;">Set IMETA, RMETA</a></td>
    <td valign="top">
<table>
 <tr>
   <td>Enter next release version:</td>
   <td><input type="text" name="new_release2"></td>
 </tr><tr>
   <td>Enter prev release version:</td>
   <td><input type="text" name="old_release2"></td>
 </tr>
</table>
	     Maintenance of IMETA and RMETA values for MRSAB.  This also sets the
	     SIMS "Meta ver" field for all sources tracked as "Current".
 </tr>

<!-- Set Release CUIs -->
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=classes_feedback"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Set Release CUIs'; return true;"
	     onMouseOut="window.status=''; return true;">Set Release CUIs</a></td>
    <td valign="top">Takes the final CUI assignments for the upcoming release and applies
             them to classes.last_release_cui.  Also sets the "released" field for the various
	     tables.  Make sure to run this after the copyout and before editing resumes.
    </td>
 </tr>

<!-- SIMS Info Maintenance -->
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=sims_info"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='SIMS Info Maintenance'; return true;"
	     onMouseOut="window.status=''; return true;">SIMS Info Maintenance</a></td>
    <td valign="top">Computes and sets some values in sims_info that will appear in the
             upcoming MRSAB.  Run this after the copyout.  It will set the following fields:
	     <ul><li>root_cui</li><li>versioned_cui</li><li>cui_frequency</li><li>term_frequency</li>
	       <li>attribute_name_list</li><li>term_type_list</li></ul>
    </td>
 </tr>

 <!-- Verify releasability in foreign_classes/context_relationships -->
 <tr>
    <td width="30%" valign="top"><a href="$cgi?$running_state&command=verify_releasability"
	     onClick="return confirm('This operation may take more than 5 minutes.\\nAre you sure you want to do this now?');"
	     onMouseOver="window.status='Verify releasability'; return true;"
	     onMouseOut="window.status=''; return true;">Verify Releasability</a></td>
    <td valign="top">Make sure context_relationships and foreign_classes rows have correctly set releasability values. (Make sure to map foreign atoms across safe replacement facts before doing this).</td>
 </tr>

</table>
</center>
</form>
<p>
    };
}; # end PrintINDEX

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
# Run verify releasability
sub Handle_verify_releasability {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # The following code run verify releasability procedures
  #
  print L "    Verify foreign classes releasability ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t AS
        SELECT /*+ PARALLEL(fc) */ DISTINCT source, 'n' AS tobereleased
        FROM foreign_classes fc
        WHERE source NOT IN (SELECT current_name FROM source_version)
        AND tobereleased IN ('Y','y')
    }) || ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_t
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
    $dbh->do(qq{
          UPDATE foreign_classes
	  SET tobereleased = 'n'
          WHERE source IN (SELECT source from ${uniq}_t ) and tobereleased != 'n'
      }) || ((print L "<span id=red>Error executing update 2 ($DBI::errstr).</span>")
       &&  return);

  }

  print L "    Verify context relationships releasability ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t AS
        SELECT /*+ PARALLEL(cr) */ relationship_id AS row_id, 'n' AS new_value
        FROM context_relationships cr
        WHERE source NOT IN (SELECT current_name FROM source_version)
        AND tobereleased IN ('Y','y')
    }) || ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_t
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

  if ($row_ct > 0) {
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'T',
                id_type => 'CR',
                authority => 'MAINTENANCE',
                table_name => '${uniq}_t',
                work_id => 0,
                status => 'R',
		new_value => 'n');
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

  print L "    Verify SG relationships releasability ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t AS
        SELECT /*+ PARALLEL(r) */ relationship_id AS row_id, 'Y' AS new_value
        FROM relationships r
        WHERE sg_type_1 in (SELECT code FROM code_map WHERE type='map_sg_type')
	  AND concept_id_1 IN (SELECT concept_id FROM concept_status WHERE tobereleased in ('Y','y'))
	  AND tobereleased IN ('n','N')
	  AND source IN (SELECT current_name FROM source_version)
          AND sg_type_1 not in (select code from code_map WHERE type='no_remap_sg_type')
	UNION
        SELECT /*+ PARALLEL(r) */ relationship_id AS row_id, 'Y' AS new_value
        FROM relationships r
        WHERE sg_type_2 in (SELECT code FROM code_map WHERE type='map_sg_type')
	  AND concept_id_2 IN (SELECT concept_id FROM concept_status WHERE tobereleased in ('Y','y'))
          AND tobereleased IN ('n','N')
	  AND source IN (SELECT current_name FROM source_version)
          AND sg_type_2 not in (select code from code_map WHERE type='no_remap_sg_type')
    }) || ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_t
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

  if ($row_ct > 0) {
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'T',
                id_type => 'R',
                authority => 'MAINTENANCE',
                table_name => '${uniq}_t',
                work_id => 0,
                status => 'R',
		new_value => 'Y');
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 1.2 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute ||
    ((print L "<span id=red>Error executing batch action 1.2 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }

  print L "    Verify SG attributes releasability ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_t AS
        SELECT /*+ PARALLEL(a) */ attribute_id AS row_id, 'Y' AS new_value
        FROM attributes a
        WHERE sg_type in (SELECT code FROM code_map WHERE type='map_sg_type')
	  AND concept_id IN (SELECT concept_id FROM concept_status WHERE tobereleased in ('Y','y'))
	  AND tobereleased IN ('n','N')
	  AND source IN (SELECT current_name FROM source_version)
          AND sg_type not in (select code from code_map WHERE type='no_remap_sg_type')
    }) || ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_t
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

  if ($row_ct > 0) {
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'T',
                id_type => 'A',
                authority => 'MAINTENANCE',
                table_name => '${uniq}_t',
                work_id => 0,
                status => 'R',
		new_value => 'Y');
        END;
    }) ||
    ((print L "<span id=red>Error preparing batch action 1.3 ($DBI::errstr).</span>")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute ||
    ((print L "<span id=red>Error executing batch action 1.3 ($DBI::errstr).</span>")
     &&  return);
    print L "      Transaction_id == $transaction_id\n";
  }

  #
  # Cleanup
  #
  print L "    Cleanup ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    BEGIN
      MEME_UTILITY.drop_it('table','${uniq}_t');
    END;}) ||
    ((print L "<span id=red>Error preparing drop 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 3 ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;

}

##############################################################################
# Handle prepare NLM02 Codes
sub Handle_prepare_nlm02_codes {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # The following code prepares nlm02 codes
  #
  print L "    Prepare NLM02 Codes ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_trx_max'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE table ${uniq}_trx_max AS
        SELECT NVL(max(code),0) as code FROM
        (SELECT to_number(substr(code,INSTR(code,':')+3)) AS code
         FROM classes WHERE source='NLM02'
         AND code LIKE '%:RX%'
         UNION SELECT to_number(substr(code,3))
         FROM classes WHERE source='NLM02'
         AND code like 'RX%')
    }) || ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_trx_all'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE table ${uniq}_trx_all AS
        SELECT * from classes WHERE 1=0
    }) || ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  # Get all NLM02 atoms
  print L "    Get all NLM02 atoms ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        INSERT INTO ${uniq}_trx_all
        SELECT * FROM classes WHERE source='NLM02'
        AND tobereleased in ('Y','y')
    }) || ((print L "<span id=red>Error executing insert 1 ($DBI::errstr).</span>")
     &&  return);

  # Set the codes to just the RX part
  print L "    Set the codes to just the RX part ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        UPDATE ${uniq}_trx_all
        SET code = null
	WHERE nvl(code,'null') not like '%:RX%'
          AND nvl(code,'null') not like 'RX%'
    }) || ((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        UPDATE ${uniq}_trx_all
        SET code = SUBSTR(code,INSTR(code,':')+1)
        WHERE code like '%:RX%'
    }) || ((print L "<span id=red>Error executing update 2 ($DBI::errstr).</span>")
     &&  return);


  # Create table to track assignments
  print L "    Create table to track assignments ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_trx_assign'); END;") ||
    ((print L "<span id=red>Error preparing drop 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 3 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_trx_assign (
            concept_id   NUMBER(12),
	    code         VARCHAR2(50))
    }) || ((print L "<span id=red>Error executing create 3 ($DBI::errstr).</span>")
     &&  return);

  # Resolve merges (single concept > 1 codes)
  # Just pick the min code
  # Possibly we should insert attributes to preserve old mappings
  print L "    Resolve merges (single concept > 1 codes) ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        INSERT INTO ${uniq}_trx_assign
        SELECT concept_id, min(code) AS code
        FROM ${uniq}_trx_all
        WHERE code IS NOT NULL
        GROUP BY concept_id HAVING count(distinct code) > 1
    }) || ((print L "<span id=red>Error executing insert 2 ($DBI::errstr).</span>")
     &&  return);

  # Resolve simple cases  (single concept = 1 code)
  print L "    Resolve simple cases (single concept = 1 code) ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        INSERT INTO ${uniq}_trx_assign
        SELECT DISTINCT concept_id, code
        FROM ${uniq}_trx_all
        WHERE code IN (SELECT code FROM ${uniq}_trx_assign
                       GROUP by code HAVING count(distinct concept_id)=1)
          AND concept_id IN (SELECT concept_id FROM ${uniq}_trx_assign
                             GROUP by concept_id HAVING count(distinct code)=1)
    }) || ((print L "<span id=red>Error executing insert 2 ($DBI::errstr).</span>")
     &&  return);

  # Resolve splits (single code > 1 concept)
  # Just pick the min concept
  # Possibly we should insert attributes to preserve old mappings
  print L "    Resolve splits (single code > 1 concept) ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        INSERT INTO ${uniq}_trx_assign
        SELECT MIN(concept_id),code
        FROM ${uniq}_trx_all
        WHERE code IS NOT NULL
        GROUP BY code HAVING count(distinct concept_id) > 1
    }) || ((print L "<span id=red>Error executing insert 3 ($DBI::errstr).</span>")
     &&  return);

  # split/merge don't count as split or merge
  $dbh->do(qq{
        DELETE FROM ${uniq}_trx_assign
        WHERE concept_id IN (SELECT concept_id FROM ${uniq}_trx_assign
        GROUP BY concept_id HAVING count(*) > 1)
    }) || ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        DELETE FROM ${uniq}_trx_assign
        WHERE code IN (SELECT code FROM ${uniq}_trx_assign
        GROUP BY code HAVING count(*) > 1)
    }) || ((print L "<span id=red>Error executing delete 2 ($DBI::errstr).</span>")
     &&  return);

  # Create assignments for new and split/merge cases
  print L "    Create assignments for new and split/merge cases ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        INSERT INTO ${uniq}_trx_assign
        SELECT concept_id, 'RX' || LPAD((b.code + rownum),6,0)
        FROM (SELECT distinct concept_id FROM ${uniq}_trx_all
        MINUS
        SELECT concept_id FROM ${uniq}_trx_assign) a, ${uniq}_trx_max b
    }) || ((print L "<span id=red>Error executing insert 4 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM
	      (SELECT concept_id FROM ${uniq}_trx_assign
	       GROUP BY concept_id HAVING COUNT(*)>1)
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

  if ($row_ct != 0) {
    (print L "<span id=red>Error preparing raise 1 ($DBI::errstr).</span>")
       &&  return;
  }

  $sh = $dbh->prepare(qq{
	      SELECT count(*) FROM
	      (SELECT code FROM ${uniq}_trx_assign
	       GROUP BY code HAVING COUNT(*)>1)
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

  if ($row_ct != 0) {
    (print L "<span id=red>Error preparing raise 2 ($DBI::errstr).</span>")
       &&  return;
  }

  # CREATE an index on trx_assign.concept_id!
  print L "    Create index on trx_assign ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        CREATE INDEX ${uniq}_trx_ind_assign ON ${uniq}_trx_assign (concept_id)
    }) || ((print L "<span id=red>Error executing create index 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        ANALYZE TABLE ${uniq}_trx_assign COMPUTE STATISTICS
    }) || ((print L "<span id=red>Error executing analyze 1 ($DBI::errstr).</span>")
     &&  return);

  # Create table for updating classes and informing MRD
  print L "    Create table for updating classes and informing MRD ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_trx_feedback'); END;") ||
    ((print L "<span id=red>Error preparing drop 4 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 4 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_trx_feedback (
	    concept_id NUMBER(12),
	    atom_id NUMBER(12),
	    code VARCHAR2(50))
    }) || ((print L "<span id=red>Error executing create 4 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        INSERT INTO ${uniq}_trx_feedback
        SELECT concept_id, atom_id, ''
        FROM classes WHERE source = 'NLM02'
        AND nvl(code,'null') not like '%:RX%'
        AND nvl(code,'null') != 'NOCODE'
        UNION
        SELECT concept_id, atom_id, ''
        FROM classes WHERE source = 'NLM02'
        AND code = 'NOCODE'
        UNION
        SELECT concept_id,atom_id, SUBSTR(code,0,INSTR(code,':')-1)
        FROM classes WHERE source = 'NLM02' AND code like '%:RX%'
    }) || ((print L "<span id=red>Error executing insert 5 ($DBI::errstr).</span>")
     &&  return);

  # Append source from classes
  print L "    Append source from classes ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        UPDATE ${uniq}_trx_feedback a
        SET code =
        (SELECT a.code||':'||b.code FROM ${uniq}_trx_assign b
        WHERE a.concept_id = b.concept_id)
    }) || ((print L "<span id=red>Error executing update 3 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        UPDATE ${uniq}_trx_feedback a
        SET code = LTRIM(code,':')
    }) || ((print L "<span id=red>Error executing update 4 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE INDEX ${uniq}_trx_ind_feedback on ${uniq}_trx_feedback (atom_id)
    }) || ((print L "<span id=red>Error executing create index 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        ANALYZE TABLE ${uniq}_trx_feedback COMPUTE STATISTICS
    }) || ((print L "<span id=red>Error executing analyze 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        UPDATE classes  a
        SET code =
        (SELECT code FROM ${uniq}_trx_feedback b
        WHERE a.atom_id = b.atom_id)
        WHERE atom_id in (SELECT atom_id from ${uniq}_trx_feedback WHERE code IS NOT NULL)
    }) || ((print L "<span id=red>Error executing update 5 ($DBI::errstr).</span>")
     &&  return);



  # run this query and print out the results
  # as "ERROR Concept with non-unique code:"
  $dbh->do(qq{
        SELECT concept_id FROM (
        SELECT concept_id, code FROM classes WHERE source='NLM02' AND
         code NOT LIKE '%:%'
         UNION SELECT concept_id, substr(code,instr(code,':')+1)
         FROM classes WHERE source='NLM02' AND code LIKE '%:%')
         GROUP BY concept_id HAVING COUNT(DISTINCT code)>1
    }) ||
    ((print L "<span id=red>Error preparing select 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing select 1 ($DBI::errstr).</span>")
     &&  return);

  #
  # Cleanup
  #
  print L "    Cleanup ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    BEGIN
      MEME_UTILITY.drop_it('table', '${uniq}_trx_max');
      MEME_UTILITY.drop_it('table', '${uniq}_trx_all');
      MEME_UTILITY.drop_it('table', '${uniq}_trx_assign');
      MEME_UTILITY.drop_it('table', '${uniq}_trx_feedback');
    END;}) ||
    ((print L "<span id=red>Error preparing drop 10 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 10 ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;

}

##############################################################################
# Handle dead CUI mappings
sub Handle_dead_cui_mappings {
  $| = 1;

  open(L,">>MIDLogs/$log_name") ||
    ((print L "<span id=red>Error opening $log_name.</span>") &&
     return);

  print L "    THIS TOOL HAS BEEN DEPRECATED..\n";
  return;

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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # The following code run handle dead CUI mappings
  #
  print L "    Handle dead CUI mappings ... ".scalar(localtime)."\n";

  $dbh->do(qq{
        DELETE FROM cui_history WHERE (cui1, cui2) IN
        (SELECT dead_cui, mapped_to_cui FROM meow.mappings_for_dead_cuis
         WHERE timestamp IS NOT NULL)
    }) || ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        DELETE FROM cui_history WHERE (cui1) IN
        (SELECT dead_cui FROM meow.mappings_for_dead_cuis
         WHERE timestamp IS NOT NULL)
    }) || ((print L "<span id=red>Error executing delete 2 ($DBI::errstr).</span>")
     &&  return);

  # Fix cases not specifically assigned a version
  $dbh->do(qq{
        UPDATE cui_history SET ver=ver||'AA'
        WHERE ver LIKE '____'
    }) || ((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return);

  # Insert data from mappings_for_dead_cuis
  # Reverse BBT and BNT because they are backwards
  # in mappings_for_dead_cuis.
  $dbh->do(qq{
        INSERT INTO cui_history
	      (cui1,ver,relationship_name,
	       relationship_attribute,map_reason,cui2)
        SELECT dead_cui, last_valid_version,
          DECODE(map_rel,'BSY','SY','BRT','RO','BBT','RN','BNT','RB','BXR','DEL'),
	      inverse_rel_attribute, reason_for_existence,
          mapped_to_cui
        FROM meow.mappings_for_dead_cuis a, inverse_rel_attributes b
	WHERE timestamp IS NOT NULL AND nvl(map_rela,'null') = nvl(relationship_attribute,'null')
    }) || ((print L "<span id=red>Error executing insert 1 ($DBI::errstr).</span>")
     &&  return);

  # Keep earliest mapping
  $dbh->do(qq{
        DELETE FROM cui_history WHERE (cui1, ver, relationship_name, cui2) IN
        (SELECT cui1, ver, relationship_name, cui2 FROM cui_history
         MINUS SELECT cui1, MIN(ver), relationship_name, cui2
         FROM cui_history GROUP BY cui1, relationship_name, cui2)
    }) || ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;

}


##############################################################################
# Handle AUI history maintenance
sub Handle_aui_history {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # Remove cases of past AUI mappings that will be replaced by new ones
  #
  print L "    Remove redundant historical mappings (C) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      DELETE FROM aui_history
	      WHERE map_reason='move'
	        AND aui1 IN
	      (SELECT /*+ parallel(c) */ aui FROM classes c
	       WHERE last_release_cui != last_assigned_cui
	         AND tobereleased IN ('Y','y')
		 AND termgroup not in ('MTH/MM','MTH/TM'))
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      Count == $row_ct\n";

  #
  # Here we have cases of things that moved from foreign_classes to classes
  #
  print L "    Remove redundant historical mappings (C-F-aui) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      DELETE FROM aui_history
	      WHERE map_reason='move'
	      AND aui1 in
	      (SELECT b.aui FROM foreign_classes b, classes c
	       WHERE b.last_release_cui != c.last_assigned_cui
                 AND b.aui = c.aui
	         AND c.tobereleased IN ('Y','y'))
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1b ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      Count == $row_ct\n";

  #
  # Here we have cases of foreign_classes atoms moving
  #
  print L "    Remove redundant historical mappings (C-F-eng_aui) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      DELETE FROM aui_history
	      WHERE map_reason='move'
	      AND aui1 in
	      (SELECT b.aui FROM foreign_classes b, classes c
	       WHERE b.last_release_cui != c.last_assigned_cui
                 AND b.eng_aui = c.aui
	         AND c.tobereleased IN ('Y','y')
	         AND b.tobereleased IN ('Y','y'))
	     });
  unless ($row_ct) {
    (print L "<span id=red>Error executing delete 1c ($DBI::errstr).</span>")
     && return;
  }
  print L "      Count == $row_ct\n";


  #
  # Remove cases where AUI moved back (AUI1,CUI1 are in classes)
  # (are these always handled by the first case?)
  #
  print L "    Remove cases that moved back (C) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      DELETE FROM aui_history
	      WHERE map_reason='move'
	        AND (aui1,cui1) IN
	      (SELECT aui,last_assigned_cui
	       FROM classes c
	       WHERE tobereleased IN ('Y','y')
  	         AND termgroup not in ('MTH/MM','MTH/TM'))
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 2 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      Count == $row_ct\n";

  #
  # Here we have cases of foreign_classes atoms moving
  #
  print L "    Remove cases that moved back (C-F-eng_aui) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      DELETE FROM aui_history
	      WHERE map_reason='move'
	      AND (aui1,cui1) in
	      (SELECT /*+ parallel(b) */ b.aui,c.last_assigned_cui
	       FROM foreign_classes b, classes c
	       WHERE b.eng_aui = c.aui
	         AND c.tobereleased IN ('Y','y')
	         AND b.tobereleased IN ('Y','y'))
	     });
  unless ($row_ct) {
    (print L "<span id=red>Error executing delete 2b ($DBI::errstr).</span>")
     && return;
  }
  print L "      Count == $row_ct\n";

  #
  # Remove entries relating to deleted AUIs (or MTH/MM atoms)
  #
  print L "    Remove move cases with dead AUIs ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      DELETE FROM aui_history
	      WHERE map_reason='move'
	        AND aui1 in
	      (SELECT aui1 FROM aui_history WHERE map_reason='move'
	       MINUS
	       (SELECT aui FROM classes
		    WHERE tobereleased in ('Y','y')
		      AND termgroup not in ('MTH/MM','MTH/TM')
		    UNION
		    SELECT aui FROM foreign_classes WHERE tobereleased IN ('Y','y')))
	     });
  unless ($row_ct) {
    (print L "<span id=red>Error executing delete 3 ($DBI::errstr).</span>")
     && return;
  }
  print L "      Count == $row_ct\n";

  #
  # Update historical cases to have current cui assignment for AUI2
  # This is necessary to handle a data condition like this one:
  # SQL> select aui FROM classes c
  #      WHERE last_release_cui != last_assigned_cui
  #      AND tobereleased IN ('Y','y') and concept_id=
  #CONCEPT_ID LAST_RELEA LAST_ASSIG T
  #---------- ---------- ---------- -
  #30649828 C0795519   C1654669   n
  #30649828            C1654669   Y
  #
  # Here an RXNORM atom with the same AUI (but different suppress) was
  # inserted as part of the final
  #
  #
  # THIS may report ">1 rows returned by subquery".  This means
  # there is >1 row in classes with the AUI in classes with
  # the same distinct last_assigned_cui
  #
  print L "    Update historical mappings with current CUI assignments (C) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      UPDATE aui_history a
	      SET cui2 = (SELECT distinct last_assigned_cui FROM classes b
	      	          WHERE a.aui2 = b.aui
	      	          	AND tobereleased in ('Y','y'))
	      WHERE map_reason='move'
	        AND (aui2, cui2) IN
	      (SELECT aui2, cui2 FROM aui_history 
           MINUS 
           SELECT aui, last_assigned_cui FROM classes 
           WHERE tobereleased in ('Y','y')
             AND termgroup not in ('MTH/MM','MTH/TM'))
             AND aui2 IN (SELECT aui FROM classes 
                         WHERE tobereleased in ('Y','y')
                           AND termgroup not in ('MTH/MM','MTH/TM')) 
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      Count == $row_ct\n";

  #
  # Insert new mappings
  #
  print L "    Insert new mappings (C) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      INSERT INTO aui_history
	      (aui1, cui1, ver, relationship_name, relationship_attribute, map_reason,
	       aui2, cui2, authority, timestamp)
	      SELECT aui, last_release_cui, '$new_release', '', '', 'move',
  	             aui, last_assigned_cui, 'MTH', sysdate
	      FROM classes c
	      WHERE last_release_cui != last_assigned_cui
	        AND tobereleased IN ('Y','y')
	        AND termgroup not in ('MTH/MM','MTH/TM')
    });
  unless ($row_ct) {
    (print L "<span id=red>Error executing insert 1 ($DBI::errstr).</span>")
     &&  return;
  }
  print L "      Count == $row_ct\n";

  #
  # Insert new mappings
  #
  print L "    Insert new mappings (C-FC-aui) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      INSERT INTO aui_history
	      (aui1, cui1, ver, relationship_name, relationship_attribute, map_reason,
	       aui2, cui2, authority, timestamp)
	      SELECT aui1, cui1, '$new_release', '', '', 'move',
	             aui2, cui2, 'MTH', sysdate
	      FROM
	       (SELECT a.aui aui1, a.last_release_cui cui1,
	               a.aui aui2, b.last_assigned_cui cui2
	        FROM foreign_classes a, classes b
	        WHERE a.last_release_cui != b.last_assigned_cui
                  AND a.aui = b.aui
	          AND b.tobereleased IN ('Y','y')
		MINUS
		SELECT aui1, cui1, aui2, cui2
		FROM aui_history
		WHERE ver = ? AND map_reason='move')
    }, undef, $new_release);
  unless ($row_ct) {
    (print L "<span id=red>Error executing insert 2 ($DBI::errstr).</span>")
     &&  return;
  }
  print L "      Count == $row_ct\n";

  #
  # Insert new mappings
  #
  print L "    Insert new mappings (C-FC-eng_aui) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      INSERT INTO aui_history
	      (aui1, cui1, ver, relationship_name, relationship_attribute, map_reason,
	       aui2, cui2, authority, timestamp)
	      SELECT aui1, cui1, '$new_release', '', '', 'move',
	             aui2, cui2, 'MTH', sysdate
	      FROM
	       (SELECT a.aui aui1, a.last_release_cui cui1,
	               a.aui aui2, b.last_assigned_cui cui2
	        FROM foreign_classes a, classes b
	        WHERE a.last_release_cui != b.last_assigned_cui
                  AND a.eng_aui = b.aui
 	          AND b.tobereleased IN ('Y','y')
 	          AND a.tobereleased IN ('Y','y')
		MINUS
		SELECT aui1, cui1, aui2, cui2
		FROM aui_history
		WHERE ver = ? AND map_reason='move')
    }, undef, $new_release);
  unless ($row_ct) {
    (print L "<span id=red>Error executing insert 3 ($DBI::errstr).</span>")
     && return;
  }
  print L "      Count == $row_ct\n";


  ##
  ## QA CHECKS
  ##
  print L "    QA Check - AUI1,CUI1 in classes ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT aui1,cui1,ver,map_reason,cui2 FROM aui_history
    WHERE map_reason = 'move' AND (aui1,cui1) IN
      (SELECT aui,last_assigned_cui FROM classes
       WHERE tobereleased in ('Y','y')
         AND termgroup not in ('MTH/MM','MTH/TM'))
  }) || ((print L "<span id=red>Error preparing qa1 ($DBI::errstr).</span>")
     && return);
  $sh->execute ||
    ((print L "<span id=red>Error executing qa1 ($DBI::errstr).</span>")
     &&  return);
  $ct = 0;
  while (($aui1,$cui1,$ver,$map_reason,$cui2) = $sh->fetchrow_array){
  	$ct++;
    if ($ct == 50) { print L "      ...\n"; last; }
    print L "      $aui1|$cui1|$cui2|$ver|$map_reason\n";
  }
  if ($ct == 0) { print L "      Passed\n"; }

  print L "    QA Check - AUI1,CUI1 in foreign classes ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT aui1,cui1,ver,map_reason,cui2 FROM aui_history
    WHERE map_reason = 'move' AND (aui1,cui1) IN
      (SELECT a.aui,b.last_assigned_cui FROM foreign_classes a, classes b
       WHERE a.tobereleased in ('Y','y') and b.tobereleased in ('Y','y')
         AND a.eng_aui = b.aui)
  }) || ((print L "<span id=red>Error preparing qa1 ($DBI::errstr).</span>")
     && return);
  $sh->execute ||
    ((print L "<span id=red>Error executing qa1 ($DBI::errstr).</span>")
     &&  return);
  $ct = 0;
  while (($aui1,$cui1,$ver,$map_reason,$cui2) = $sh->fetchrow_array){
    $ct++;
    if ($ct == 50) { print L "      ...\n"; last; }
    print L "      $aui1|$cui1|$cui2|$ver|$map_reason\n";
  }
  if ($ct == 0) { print L "      Passed\n"; }

  print L "    QA Check - AUI2,CUI2 not in foreign_classes,classes ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT aui1,cui1,ver,map_reason,cui2 FROM aui_history
    WHERE map_reason = 'move'
      AND (aui2,cui2) IN
      (SELECT aui2,cui2 FROM aui_history WHERE map_reason='move'
       MINUS
        (SELECT aui,last_assigned_cui FROM classes
         WHERE tobereleased in ('Y','y')
	       AND termgroup not in ('MTH/MM','MTH/TM')
         UNION
         SELECT a.aui,b.last_assigned_cui FROM foreign_classes a, classes b
         WHERE a.tobereleased in ('Y','y') and b.tobereleased in ('Y','y')
           AND a.eng_aui = b.aui))
  }) || ((print L "<span id=red>Error preparing qa1 ($DBI::errstr).</span>")
     && return);
  $sh->execute ||
    ((print L "<span id=red>Error executing qa1 ($DBI::errstr).</span>")
     &&  return);
  $ct = 0;
  while (($aui1,$cui1,$ver,$map_reason,$cui2) = $sh->fetchrow_array){
  	$ct++;
    if ($ct == 50) { print L "      ...\n"; last; }
    print L "      $aui1|$cui1|$cui2|$ver|$map_reason\n";
  }
  if ($ct == 0) { print L "      Passed\n"; }

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;

}; # end Handle_aui_history

##############################################################################
# Handle CUI history maintenance
sub Handle_cui_history {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # Archive current cui_history state
  #
  print L "    Backup cui_history data, in case of failure ... ".scalar(localtime)."\n";
  $dbh->do(qq{
      BEGIN MEME_UTILITY.drop_it('table','cui_history_bak'); END;
  }) || ((print "<span id=red>Error dropping cui_history_bak ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
      CREATE TABLE cui_history_bak AS SELECT * FROM cui_history
  }) || ((print "<span id=red>Error dropping cui_history_bak ($DBI::errstr).</span>") && return);


  #
  # Find "old" CUIs
  #
  print L "    Find old CUIs ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN MEME_UTILITY.drop_it('table','t_old_cuis'); END;
  }) || ((print "<span id=red>Error dropping t_old_cuis ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
	CREATE TABLE t_old_cuis (
        atom_id       NUMBER(12) NOT NULL,
        concept_id    NUMBER(12) NOT NULL,
        cui           VARCHAR2(10) NOT NULL
    )
  }) || ((print "<span id=red>Error creating t_old_cuis ($DBI::errstr).</span>") && return);

  $rc = $dbh->do(qq{
      INSERT INTO t_old_cuis (atom_id,concept_id,cui)
      SELECT atom_id, concept_id, last_release_cui
      FROM classes
      WHERE last_release_cui is not null
        AND released != 'N'
        AND last_release_cui IN
          (SELECT last_release_cui FROM classes
           WHERE last_release_cui IS NOT NULL
           MINUS
           SELECT cui FROM concept_status WHERE tobereleased in ('Y','y'))
      UNION
      SELECT atom_id, 0, last_release_cui
      FROM dead_classes
      WHERE last_release_cui is not null
        AND released != 'N'
        AND last_release_cui IN
          (SELECT last_release_cui FROM dead_classes
           WHERE last_release_cui IS NOT NULL
           MINUS
           SELECT cui FROM concept_status WHERE tobereleased in ('Y','y'))
  }) || ((print "<span id=red>Error loading t_old_cuis ($DBI::errstr).</span>") && return);
    print L "      count == $rc\n";

  $dbh->do(qq{
      CREATE INDEX x_t_old_cuis ON t_old_cuis (atom_id) COMPUTE STATISTICS PARALLEL
  }) || ((print "<span id=red>Error indexing t_old_cuis ($DBI::errstr).</span>") && return);

  #
  # Find "merged" CUIs
  #
  print L "    Find merged CUIs ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN MEME_UTILITY.drop_it('table','t_merged_cuis'); END;
  }) || ((print "<span id=red>Error dropping t_merged_cuis ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
      CREATE TABLE t_merged_cuis (
	      old_cui		VARCHAR2(10) NOT NULL,
	      new_cui		VARCHAR2(10) NOT NULL
      )
  }) || ((print "<span id=red>Error creating t_merged_cuis ($DBI::errstr).</span>") && return);

  $rc = $dbh->do(qq{
      INSERT INTO t_merged_cuis (old_cui,new_cui)
      SELECT DISTINCT a.cui AS old_cui, b.last_assigned_cui AS new_cui
      FROM t_old_cuis a, classes b, concept_status c
      WHERE b.tobereleased in ('Y','y')
        AND a.concept_id = c.concept_id
        AND b.last_assigned_cui = c.cui
        AND a.concept_id != 0
        AND a.cui != b.last_assigned_cui
        AND b.released != 'N'
      UNION
      SELECT DISTINCT a.cui AS old_cui, b.last_assigned_cui AS new_cui
      FROM t_old_cuis a, classes b, classes c
      WHERE c.released != 'N'
        AND a.atom_id = c.atom_id
        AND b.aui = c.aui
        AND a.cui != b.last_assigned_cui
  }) || ((print "<span id=red>Error loading t_merged_cuis ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  print L "    Find split-merged CUIs ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN MEME_UTILITY.drop_it('table','t_split_merged_cuis'); END;
  }) || ((print "<span id=red>Error dropping t_split_merged_cuis ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
      CREATE TABLE t_split_merged_cuis (
	      old_cui		VARCHAR2(10) NOT NULL,
	      new_cui		VARCHAR2(10) NOT NULL
      )
  }) || ((print "<span id=red>Error creating t_split_merged_cuis ($DBI::errstr).</span>") && return);

  $rc = $dbh->do(qq{
      INSERT INTO t_split_merged_cuis (old_cui, new_cui)
      SELECT * FROM t_merged_cuis WHERE old_cui IN
         (SELECT old_cui FROM t_merged_cuis
          GROUP BY old_cui HAVING count(DISTINCT new_cui) > 1)
  }) || ((print "<span id=red>Error loading t_split_merged_cuis ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  print L "    Remove split-merged CUIs from merged_cuis... ".scalar(localtime)."\n";
  $dbh->do(qq{
      DELETE FROM t_merged_cuis WHERE old_cui IN
         (SELECT old_cui FROM t_split_merged_cuis)
  }) || ((print "<span id=red>Error removing splits from t_merged_cuis ($DBI::errstr).</span>") && return);

  #
  # Find "deleted" cuis
  #
  print L "     Find deleted CUIs (old-merged) ... ".scalar(localtime)."\n";
  $dbh->do(qq{
	  BEGIN MEME_UTILITY.drop_it('table','t_deleted_cuis'); END;
  }) || ((print "<span id=red>Error dropping t_deleted_cuis ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
      CREATE TABLE t_deleted_cuis (cui varchar2(10))
  }) || ((print "<span id=red>Error creating t_deleted_cuis ($DBI::errstr).</span>") && return);

  $rc = $dbh->do(qq{
      INSERT INTO t_deleted_cuis
      SELECT cui FROM t_old_cuis
      MINUS
      (SELECT old_cui as cui FROM t_merged_cuis
       UNION
       SELECT old_cui FROM t_split_merged_cuis)
  }) || ((print "<span id=red>Error loading t_deleted_cuis ($DBI::errstr).</span>") && return);

  #
  # Remove "current" merges where there are exact matching historical ones.
  # (preserve VER values)
  #
  print L "    Remove current SY facts where they exactly match historical ones ... ".scalar(localtime)."\n";
  $rc2 = $dbh->do(qq{
      DELETE FROM t_merged_cuis
      WHERE (old_cui, new_cui) IN
      (SELECT cui1,cui2 FROM cui_history
      WHERE relationship_name = 'SY')
  }) || ((print "<span id=red>Error dropping t_deleted_cuis ($DBI::errstr).</span>") && return);
  $rc -= $rc2;
  print L "      count == $rc\n";

  #
  # Remove old CUI1 SY entries for current merges
  #
  print L "    Remove historical SY facts where CUI1 is involved in current SY (or split) ... ".scalar(localtime)."\n";
  $rc2 = $dbh->do(qq{
      DELETE FROM cui_history
      WHERE relationship_name = 'SY' AND cui1 IN
      (SELECT old_cui FROM t_merged_cuis
       UNION SELECT old_cui FROM t_split_merged_cuis)
  }) || ((print "<span id=red>Error dropping t_deleted_cuis ($DBI::errstr).</span>") && return);
  $rc -= $rc2;
  print L "      count == $rc\n";

  #
  # Insert new SY facts
  #
  print L "    Load current merged CUIs into cui_history ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      INSERT INTO cui_history
        (cui1,ver,relationship_name,relationship_attribute, map_reason,cui2)
      SELECT DISTINCT old_cui as cui1, ?, 'SY','','', new_cui
      FROM t_merged_cuis
  }, undef, $old_release) || ((print "<span id=red>Error loading merges into cui_history ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  #
  # Insert new RO bequeathal facts for splits
  #
  print L "    Load RO bequeathal rels for splits into cui_history ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      INSERT INTO cui_history
        (cui1,ver,relationship_name,relationship_attribute, map_reason,cui2)
      SELECT DISTINCT old_cui as cui1, ?, 'RO','','', new_cui
      FROM t_split_merged_cuis
  }, undef, $old_release) || ((print "<span id=red>Error loading split-merges into cui_history ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  #
  # Insert new DEL facts
  #
  print L "    Load current deleted CUIs into cui_history ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      INSERT INTO cui_history
        (cui1,ver,relationship_name,relationship_attribute, map_reason,cui2)
      SELECT DISTINCT cui, ?, 'DEL','','',''
      FROM t_deleted_cuis
      WHERE cui IN
	    (SELECT cui FROM t_deleted_cuis
	     MINUS
	     SELECT cui1 FROM cui_history)
  }, undef, $old_release) || ((print "<span id=red>Error loading deletions into cui_history ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  #
  # Insert new bequeathal rels (except where SY facts exist)
  #
  print L "    Load current bequeathal rels into cui_history ... ".scalar(localtime)."\n";
  $dbh->do(qq{
      BEGIN MEME_UTILITY.drop_it('table','t_bequeathal_rels'); END;
  }) || ((print "<span id=red>Error dropping t_bequeathal_rels ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
      CREATE TABLE t_bequeathal_rels AS
      SELECT last_release_cui as cui1, '$old_release' ver,
	      relationship_name, b.cui as cui2
      FROM relationships r, classes a, concept_status b
      WHERE r.concept_id_1=a.concept_id
        AND r.concept_id_2=b.concept_id
        AND r.relationship_name in ('BBT','BNT','BRT')
        AND r.tobereleased in ('Y','y')
        AND a.tobereleased in ('N','n')
        AND b.tobereleased in ('Y','y')
        AND last_release_cui IS NOT NULL
        AND last_release_cui IN
         (SELECT cui FROM concept_status WHERE tobereleased in ('N','n'))
      UNION
      SELECT last_release_cui as cui1, '$old_release',
         DECODE(relationship_name,'BBT','BNT','BNT','BBT','BRT'), b.cui as cui2
      FROM relationships r, classes a, concept_status b
      WHERE r.concept_id_2=a.concept_id
        AND r.concept_id_1=b.concept_id
        AND r.relationship_name in ('BBT','BNT','BRT')
        AND r.tobereleased in ('Y','y')
        AND a.tobereleased in ('N','n')
        AND b.tobereleased in ('Y','y')
        AND last_release_cui IS NOT NULL
        AND last_release_cui IN
         (SELECT cui FROM concept_status WHERE tobereleased in ('N','n'))
  }) || ((print "<span id=red>Error creating t_bequeathal_rels ($DBI::errstr).</span>") && return);

  # Remove bequeathals matching merges
  $dbh->do(qq{
      DELETE FROM t_bequeathal_rels
      WHERE cui1 IN (SELECT cui1 FROM cui_history WHERE relationship_name='SY')
  }) || ((print "<span id=red>Error removing bequeathal rels for SY facts ($DBI::errstr).</span>") && return);

  # Remove new bequeathal rels where they exactly match historical ones
  $dbh->do(qq{
    DELETE FROM t_bequeathal_rels
    WHERE (cui1,cui2,DECODE(relationship_name,'BBT','RB','BNT','RN','BRT','RO',relationship_name)) IN
       (SELECT cui1,cui2,relationship_name FROM cui_history WHERE relationship_name not in ('SY','DEL'))
  }) || ((print "<span id=red>Error removing bequeathal rels for SY facts ($DBI::errstr).</span>") && return);

  # Remove historical bequeathal rels where there are new ones between same concepts
  $dbh->do(qq{
    DELETE FROM cui_history
    WHERE relationship_name not in ('SY','DEL')
      AND (cui1,cui2) IN
       (SELECT cui1,cui2 FROM t_bequeathal_rels)
  }) || ((print "<span id=red>Error removing bequeathal rels for SY facts ($DBI::errstr).</span>") && return);

  $rc = $dbh->do(qq{
      INSERT INTO cui_history
        (cui1,ver,relationship_name,relationship_attribute, map_reason,cui2)
      SELECT DISTINCT cui1, ver,
        DECODE(relationship_name,'BBT','RB','BNT','RN','BRT','RO',relationship_name), '','',cui2
      FROM t_bequeathal_rels
  }) || ((print "<span id=red>Error insertin bequeathal rels into cui_history ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";


  #
  # Update CUI2 through SY facts
  #
  print L "    Update CUI2 through SY facts ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE cui_history a
      SET cui2 = (SELECT cui2 FROM cui_history b
	              WHERE a.cui2=b.cui1 AND b.relationship_name = 'SY')
      WHERE cui2 IN (SELECT cui1 FROM cui_history WHERE relationship_name='SY')
  }) || ((print "<span id=red>Error updating CUI2 througy SY facts ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  #
  # Remove CUI1=CUI2 facts
  #
  print L "    Remove CUI1=CUI2 cases ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      DELETE FROM cui_history WHERE cui1=cui2
  }) || ((print "<span id=red>Error removing CUI1=CUI2 cases($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  #
  # Compute bequeathal rel transitive closure
  #
  # Map bequeathal rels. If A--RN-->B--RN-->C
  # And B is no longer alive, then A--RN-->C
  #
  print L "    Insert transitive bequeathal rel cases (matching rel) ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      INSERT INTO cui_history
        (cui1,ver,relationship_name,relationship_attribute, map_reason,cui2)
      SELECT DISTINCT a.cui1, a.ver, a.relationship_name, '','', b.cui2
      FROM cui_history a, cui_history b
	  WHERE a.cui2 = b.cui1
	    AND a.relationship_name = b.relationship_name
	    AND a.relationship_name IN ('RB','RN','RO')
  }) || ((print "<span id=red>Error inserting transitive bequeathal rel cases ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  print L "    Insert transitive bequeathal rel cases (non-matching rel) ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      INSERT INTO cui_history
        (cui1,ver,relationship_name,relationship_attribute, map_reason,cui2)
      SELECT DISTINCT a.cui1, a.ver, 'RO', '','', b.cui2
      FROM cui_history a, cui_history b
      WHERE a.cui2 = b.cui1
        AND a.relationship_name != b.relationship_name
        AND a.relationship_name IN ('RB','RN','RO')
        AND b.relationship_name IN ('RB','RN','RO')
  }) || ((print "<span id=red>Error inserting transitive bequeathal rel cases for non-matching rels ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  $dbh->commit;

  #
  # At this point, if CUI1|RN|CUI2 and CUI2|RO|CUI3 and CUI1
  # was re-bequeathed
  # in the database to CUI3, we can have CUI1|RO|CUI3, CUI1|RN|CUI3
  # If we find the same CUI1,CUI2 with different bequeathal rels, make it RO
  #
  print L "    Resolve conflicting bequeathal rels (e.g. cui1,cui2,RB/RN) ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE cui_history a
      SET relationship_name = 'RO'
      WHERE relationship_name not in ('DEL','SY')
        AND (cui1,cui2) IN
	       (SELECT cui1,cui2 FROM cui_history b
	        WHERE relationship_name not in ('DEL','SY')
	        GROUP BY cui1,cui2 HAVING count(distinct relationship_name)>1)
  }) || ((print "<span id=red>Error fixing conflicting bequeathal rels ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  $dbh->commit;

  #
  # Set VER values
  #
  print L "    Set VER to min for each CUI1 ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE cui_history a
      SET ver = (SELECT min(ver) FROM cui_history b
	             WHERE a.cui1=b.cui1)
  	  WHERE (cui1,ver) IN
	     (SELECT cui1,ver FROM cui_history
	      MINUS
	      SELECT cui1,min(ver) FROM cui_history group by cui1)
  }) || ((print "<span id=red>Error setting VER to min ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  print L "    Set VER to $old_release where null ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE cui_history SET ver = ? WHERE ver IS NULL
  }, undef, $old_release) || ((print "<span id=red>Error fixing null VER($DBI::errstr).</span>") && return);
  print L "      count (should be 0) == $rc\n";

  $dbh->commit;

  #
  # Remove DEL facts where CUI1 has non-DEL mapping
  #
  print L "    Remove DEL facts where CUI1 has non-DEL mapping ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      DELETE FROM cui_history
      WHERE relationship_name = 'DEL'
        AND cui1 IN
         (SELECT cui1 from cui_history
          WHERE relationship_name != 'DEL')
        AND cui2 IN
        	(SELECT cui FROM concept_status WHERE tobereleased in ('Y','y'))
  }) || ((print "<span id=red>Error removing DEL facts where better facts exist ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  #
  # Remove bequeathal rel facts where CUI1 has an SY mapping
  #
  print L "    Remove bequeathal rels where CUI1 has SY mapping ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      DELETE FROM cui_history
      WHERE relationship_name IN ('RB','RN','RO') AND cui1 IN
          (SELECT cui1 from cui_history
           WHERE relationship_name = 'SY')
  }) || ((print "<span id=red>Error removing bequeathal rels where SY exists ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  #
  # Remove rows where CUI1 is "live"
  #
  print L "    Remove entries where CUI1 is live ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      DELETE FROM cui_history
      WHERE cui1 IN
         (SELECT cui from concept_status
          WHERE tobereleased in ('Y','y'))
  }) || ((print "<span id=red>Error removing live CUI1 cases ($DBI::errstr).</span>") && return);
  print L "      count (should be 0) == $rc\n";

  #
  # Insert new mappings for CUI1 where CUI2 is dead,
  # Borrow from CUI2 mapping for SY
  # Borrow from CUI2 mapping for non-SY (bequeathal rels)
  #
  print L "    Insert mappings for CUI1 where CUI2 is dead ... ".scalar(localtime)."\n";
  print L "      Handle SY facts (borrow rel from CUI2 fact)\n";
  $rc = $dbh->do(qq{
      INSERT INTO cui_history
        (cui1,ver,relationship_name,relationship_attribute, map_reason,cui2)
      SELECT a.cui1, a.ver, b.relationship_name,'','', b.cui2
      FROM cui_history a, cui_history b
       WHERE a.cui2 IN
           (SELECT cui2 from cui_history
            MINUS
            SELECT cui FROM concept_status
            WHERE tobereleased in ('Y','y'))
         AND a.cui2 = b.cui1
         AND a.relationship_name = 'SY'
  }) || ((print "<span id=red>Error inserting mappings for CUI1 where CUI2 is dead (sy) ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  print L "    Insert mappings for CUI1 where CUI2 is dead ... ".scalar(localtime)."\n";
  print L "      Handle bequeathal facts (create DEL facts)\n";
  $rc = $dbh->do(qq{
      INSERT INTO cui_history
        (cui1,ver,relationship_name,relationship_attribute, map_reason,cui2)
      SELECT a.cui1, a.ver, 'DEL','','',''
      FROM cui_history a, cui_history b
      WHERE a.cui2 IN
          (SELECT cui2 from cui_history
           MINUS
           SELECT cui FROM concept_status
           WHERE tobereleased in ('Y','y'))
        AND a.cui2 = b.cui1
        AND a.relationship_name != 'SY'
  }) || ((print "<span id=red>Error inserting mappings for CUI1 where CUI2 is dead (non-SY) ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";


  #
  # Remove dead CUI2 cases
  #
  print L "    Remove cases where CUI2 is still dead ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      DELETE FROM cui_history
      WHERE cui2 IN
          (SELECT cui2 from cui_history
           MINUS SELECT cui
           FROM concept_status
           WHERE tobereleased in ('Y','y'))
  }) || ((print "<span id=red>Error inserting mappings for CUI1 where CUI2 is dead (non-SY) ($DBI::errstr).</span>") && return);
  print L "      count == $rc\n";

  #
  # Remove DEL where another mapping exists (again)
  # Needed after previous step to avoid this:
  # C0847168|2004AA|DEL|||||
  #  C0847168|2004AA|RO|||C0311262|Y|
  #
  print L "    Remove DEL facts where CUI1 has non-DEL mapping (again) ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      DELETE FROM cui_history
      WHERE relationship_name = 'DEL' AND cui1 IN
         (SELECT cui1 from cui_history
          WHERE relationship_name != 'DEL')
  }) || ((print "<span id=red>Error removing DEL facts where better facts exist ($DBI::errstr).</span>") && return);
    print L "      count == $rc\n";

  $dbh->commit;

  #
  # Cleanup tmp tables
  #
  print L "    Cleanup Temp tables ... ".scalar(localtime)."\n";
  $dbh->do(qq{
      BEGIN
        MEME_UTILITY.drop_it('table','t_old_cuis');
        MEME_UTILITY.drop_it('table','t_merged_cuis');
        MEME_UTILITY.drop_it('table','t_deleted_cuis');
        MEME_UTILITY.drop_it('table','t_bequeathal_rels');
      END;
  }) || ((print "<span id=red>Error dropping temp tables ($DBI::errstr).</span>") && return);

  print L "\n";

  #
  # QA Checks
  #
  print L "    QA Check - CUI1 does not have SY and non-SY rows ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
     SELECT sy.cui1 FROM cui_history sy, cui_history b
     WHERE sy.cui1=b.cui1 AND sy.relationship_name = 'SY'
       AND b.relationship_name != 'SY'
   }) || ((print "<span id=red>Error preparing qa check 1 ($DBI::errstr).</span>") && return);
  $sh->execute || ((print "<span id=red>Error executing qa check 1 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (($cui1) = $sh->fetchrow_array) {
      print L "      $cui1\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }

  print L "    QA Check - CUI1 does not have DEL and non DEL rows ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
      SELECT del.cui1 FROM cui_history del, cui_history b
      WHERE del.cui1=b.cui1 AND del.relationship_name = 'DEL'
        AND b.relationship_name != 'DEL'
   }) || ((print "<span id=red>Error preparing qa check 2 ($DBI::errstr).</span>") && return);
  $sh->execute || ((print "<span id=red>Error executing qa check 2 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (($cui1) = $sh->fetchrow_array) {
      print L "      $cui1\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }

  print L "    QA Check - CUI1 and CUI2 are distinct sets ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
      SELECT cui1 FROM
      (SELECT cui1 FROM cui_history
       INTERSECT
       SELECT cui2 FROM cui_history)
   }) || ((print "<span id=red>Error preparing qa check 3 ($DBI::errstr).</span>") && return);
  $sh->execute || ((print "<span id=red>Error executing qa check 3 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (($cui1) = $sh->fetchrow_array) {
      print L "      $cui1\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }

  print L "    QA Check - CUI1 is releasable ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT * FROM cui_history WHERE cui1 IN
     (SELECT cui FROM concept_status WHERE tobereleased NOT IN ('N','n'))
  }) || ((print "<span id=red>Error preparing qa check 4 ($DBI::errstr).</span>") && return);
  $sh->execute || ((print "<span id=red>Error executing qa check 4 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (@f= $sh->fetchrow_array) {
      print L "      ";
      print L join "|", @f;
      print "\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }


  print L "    QA Check - CUI2 (where REL !='DEL) is not releasable ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
        SELECT * FROM cui_history
        WHERE relationship_name != 'DEL' AND cui2 IN
        (SELECT cui FROM concept_status WHERE tobereleased IN ('N','n'))
  }) || ((print "<span id=red>Error preparing qa check 5 ($DBI::errstr).</span>") && return);
  $sh->execute || ((print "<span id=red>Error executing qa check 5 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (@f= $sh->fetchrow_array) {
      print L "      ";
      print L join "|", @f;
      print "\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }

  print L "    QA Check - All previous CUI releasable or in CUI1 ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
        SELECT DISTINCT last_release_cui FROM classes
        WHERE last_release_cui
           IN (SELECT last_release_cui FROM classes
	       MINUS
	       (SELECT cui1 FROM cui_history
	        UNION
	        SELECT cui FROM concept_status
	        WHERE tobereleased in ('Y','y')))
  }) || ((print "<span id=red>Error preparing qa check 6 ($DBI::errstr).</span>") && return);
  $sh->execute || ((print "<span id=red>Error executing qa check 6 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (@f= $sh->fetchrow_array) {
      print L "      ";
      print L join "|", @f;
      print "\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }

  print L "    QA Check - CUI1 is unique where REL=SY ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
      SELECT cui1 FROM cui_history
      WHERE relationship_name = 'SY'
      GROUP BY cui1 HAVING count(*) > 1
  }) || ((print "<span id=red>Error preparing qa check 7 ($DBI::errstr).</span>") && return);
  $sh->execute || ((print "<span id=red>Error executing qa check 7 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (@f= $sh->fetchrow_array) {
      print L "      ";
      print L join "|", @f;
      print "\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }

  print L "    QA Check - CUI1,CUI2 is unique ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
      SELECT cui1,cui2 FROM cui_history
      GROUP BY cui1,cui2
      HAVING count(distinct relationship_name ||
	       relationship_attribute || map_reason|| ver ) > 1
  }) || ((print "<span id=red>Error preparing qa check 8 ($DBI::errstr).</span>") && return);
  $sh->execute || ((print "<span id=red>Error executing qa check 8 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (@f= $sh->fetchrow_array) {
      print L "      ";
      print L join "|", @f;
      print "\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }

  print L "    QA Check - VER not null ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
      SELECT * FROM cui_history WHERE ver IS NULL
  }) || ((print "<span id=red>Error preparing qa check 9 ($DBI::errstr).</span>") && return);
    $sh->execute || ((print "<span id=red>Error executing qa check 9 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (@f= $sh->fetchrow_array) {
      print L "      ";
      print L join "|", @f;
      print "\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }

  print L "    QA Check - prev CUI2 now unreleasable without CUI1 entry ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    (SELECT cui2 FROM cui_history_bak 
     WHERE cui2 is not null 
     MINUS SELECT cui FROM concept_status WHERE tobereleased IN('Y','y'))
    MINUS SELECT cui1 FROM cui_history
  }) || ((print "<span id=red>Error preparing qa check 10 ($DBI::errstr).</span>") && return);
    $sh->execute || ((print "<span id=red>Error executing qa check 10 ($DBI::errstr).</span>") && return);
  $rc = 0;
  while (@f= $sh->fetchrow_array) {
      print L "      ";
      print L join "|", @f;
      print "\n";
      $rc =1;
  }
  if ($rc) {
    print L "      FAILED\n";
  }  else {
    print L "      PASSED\n";
  }

  print L "\n";

  #
  # Repair CUI Relationships/Attributes
  #
  $dbh->do(qq{
      BEGIN MEME_SYSTEM.rebuild_table('cui_history','N',' '); END;
  }) || ((print "<span id=red>Error rebuilding cui_history ($DBI::errstr).</span>") && return);

  #
  # Update SG_ID values for CUI% type relationships
  #
  print L "    Update sg_id values for CUI% type relationships based on SY facts ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        TYPE ctype IS REF CURSOR;
        cvar     ctype;
        old_cui  VARCHAR2(20);
        new_cui  VARCHAR2(20);
        l_rowid ROWID;
        ct       NUMBER;
    BEGIN
        OPEN cvar FOR
          'SELECT /*+ USE_MERGE(a,b) */
            cui1, cui2, b.rowid FROM cui_history a, relationships b
           WHERE cui1 = sg_id_1 AND sg_type_1 like ''%CUI%''
             AND a.relationship_name=''SY''';
        ct := 0;
        LOOP
            FETCH cvar INTO old_cui, new_cui, l_rowid;
            EXIT WHEN cvar%NOTFOUND;

            UPDATE relationships
            SET sg_id_1 = new_cui, rui = null
            WHERE rowid = l_rowid;

            ct := ct + SQL%ROWCOUNT;
            COMMIT;

        END LOOP;
        MEME_UTILITY.put_message(LPAD(ct || ' rows updated (1).',45,' .'));
        CLOSE cvar;
        OPEN cvar FOR
          'SELECT /*+ USE_MERGE (a,b) */
            cui1, cui2, b.rowid FROM cui_history a, relationships b
           WHERE cui1=sg_id_2 AND sg_type_2 like ''%CUI%''
             AND a.relationship_name=''SY''';
        ct := 0;
        LOOP
            FETCH cvar INTO old_cui, new_cui, l_rowid;
            EXIT WHEN cvar%NOTFOUND;

            UPDATE relationships
            SET sg_id_2 = new_cui, rui = null
            WHERE rowid = l_rowid;

            ct := ct + SQL%ROWCOUNT;
            COMMIT;

        END LOOP;
        MEME_UTILITY.put_message(LPAD(ct || ' rows updated (2).',45,' .'));
        CLOSE cvar;
    END;
  }) || ((print "<span id=red>Error fixing CUI type relationship sg_ids ($DBI::errstr).</span>") && return);
  print &FlushBuffer;

  #
  # Update SG_ID values for CUI% type attributes
  #
  print L "    Update sg_id values for CUI% type attributes based on SY facts ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        TYPE ctype IS REF CURSOR;
        cvar     ctype;
        old_cui  VARCHAR2(20);
        new_cui  VARCHAR2(20);
        l_rowid ROWID;
        ct       NUMBER;
    BEGIN
        OPEN cvar FOR
          'SELECT /*+ USE_MERGE(a,b) */
             cui1, cui2, b.rowid FROM cui_history a, attributes b
           WHERE cui1=sg_id AND sg_type like ''%CUI%''
             AND relationship_name=''SY''';
        ct := 0;
        LOOP
            FETCH cvar INTO old_cui, new_cui, l_rowid;
            EXIT WHEN cvar%NOTFOUND;

            UPDATE attributes
            SET sg_id = new_cui, atui = null
            WHERE rowid = l_rowid;

            ct := ct + SQL%ROWCOUNT;
            COMMIT;
        END LOOP;
        MEME_UTILITY.put_message(LPAD(ct || ' rows updated.',45,' .'));
        CLOSE cvar;
    END;
  }) || ((print "<span id=red>Error fixing CUI type attribute sg_ids ($DBI::errstr).</span>") && return);
  print &FlushBuffer;

  #
  # Reassign CUI relationship RUIs
  #
  print L "    Reassign CUI type relationship RUIs... ".scalar(localtime)."\n";
  $dbh->do(qq{
      BEGIN meme_utility.drop_it('table','t_assign'); END;
  }) || ((print "<span id=red>Error dropping t_assign ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
    CREATE TABLE t_assign AS
    SELECT /*+ parallel(r) */  * FROM relationships r
    WHERE tobereleased in ('Y','y')
      AND rui is null
  }) || ((print "<span id=red>Error creating t_assign ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
    BEGIN meme_source_processing.assign_ruis('t_assign','MTH',0); END;
  }) || ((print "<span id=red>Error assigning RUIs ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
    DELETE FROM relationships WHERE relationship_id IN (SELECT relationship_id FROM t_assign)
  }) || ((print "<span id=red>Error Updating RUI assignments (d) ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
    INSERT INTO relationships SELECT * FROM t_assign
  }) || ((print "<span id=red>Error Updating RUI assignments (d) ($DBI::errstr).</span>") && return);

  #
  # Reassign CUI attribute ATUIs
  #
  print L "    Reassign CUI type attribute ATUIs... ".scalar(localtime)."\n";
  $dbh->do(qq{
      BEGIN meme_utility.drop_it('table','t_assign'); END;
  }) || ((print "<span id=red>Error dropping t_assign ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
    CREATE TABLE t_assign_$$ AS
    SELECT /*+ parallel(a) */  * FROM attributes a
    WHERE tobereleased in ('Y','y')
      AND atui is null
  }) || ((print "<span id=red>Error creating t_assign ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
    BEGIN meme_source_processing.assign_atuis('t_assign','MTH',0); END;
  }) || ((print "<span id=red>Error assigning ATUIs ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
    DELETE FROM attributes WHERE attribute_id IN (SELECT attribute_id FROM t_assign)
  }) || ((print "<span id=red>Error Updating ATUI assignments (d) ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
    INSERT INTO attributes SELECT * FROM t_assign
  }) || ((print "<span id=red>Error Updating ATUI assignments (d) ($DBI::errstr).</span>") && return);

  $dbh->do(qq{
      BEGIN meme_utility.drop_it('table','t_assign'); END;
  }) || ((print "<span id=red>Error dropping t_assign ($DBI::errstr).</span>") && return);

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;

}; # end Handle_cui_history

##############################################################################
# Assign last_relase_rank
#
sub Handle_lrr {
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
    ALTER SESSION set sort_area_size=400000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=400000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  print L "    Create pref_lui_for_cui_lat ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', 'pref_lui_for_cui_lat'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);
  #
  # Create table
  #
  $dbh->do(qq{
    CREATE TABLE pref_lui_for_cui_lat AS
    SELECT SUBSTR(mr,INSTR(mr,'/')+1) lui, concept_id, language
    FROM
    (SELECT max(rank) mr, concept_id, language
     FROM
     (SELECT rank||last_release_rank||sui||
	    LPAD(SUBSTR(aui,INSTR(aui,(SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_prefix'))+1),
	    (SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_length'),'0')||
	    '/'||lui as rank, concept_id, language
      FROM classes
      WHERE tobereleased in ('Y','y')
      UNION
      SELECT a.rank||a.last_release_rank||a.sui||
	    LPAD(SUBSTR(a.aui,INSTR(a.aui,(SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_prefix'))+1),
	    (SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_length'),'0')||
	    '/'||a.lui rank, b.concept_id, a.language
      FROM foreign_classes a, classes b
      WHERE a.eng_atom_id = b.atom_id
        AND a.tobereleased in ('Y','y')
        AND b.tobereleased in ('Y','y'))
      GROUP BY concept_id,language)
    }) || ((print L "<span id=red>Error at 1 ($DBI::errstr).</span>")
     &&  return);

  print L "    Create pref_sui_for_cui_lat_lui ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', 'pref_sui_for_cui_lat_lui'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);
  #
  # Create table
  #
  $dbh->do(qq{
    CREATE TABLE pref_sui_for_cui_lat_lui AS
    SELECT SUBSTR(mr,INSTR(mr,'/')+1) sui, concept_id, language
    FROM
    (SELECT max(rank) mr, concept_id, language
     FROM
     (SELECT rank||last_release_rank||sui||
	    LPAD(SUBSTR(aui,INSTR(aui,(SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_prefix'))+1),
	    (SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_length'),'0')||
	    '/'||sui as rank, concept_id, language, lui
      FROM classes
      WHERE tobereleased in ('Y','y')
      UNION
      SELECT a.rank||a.last_release_rank||a.sui||
	    LPAD(SUBSTR(a.aui,INSTR(a.aui,(SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_prefix'))+1),
	    (SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_length'),'0')||
	    '/'||a.sui rank, b.concept_id, a.language , a.lui
      FROM foreign_classes a, classes b
      WHERE a.eng_atom_id = b.atom_id
        AND a.tobereleased in ('Y','y')
        AND b.tobereleased in ('Y','y'))
      GROUP BY concept_id,language,lui)
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  print L "    Set last_release_rank to 4 for P,PF in classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ classes a SET last_release_rank = 4
      WHERE (concept_id, language, lui) IN
	    (SELECT concept_id, language, lui FROM pref_lui_for_cui_lat)
	AND (concept_id, language, sui) IN
	    (SELECT concept_id, language, sui FROM pref_sui_for_cui_lat_lui)
	AND tobereleased IN ('Y','y')
        AND last_release_rank != 4
    }) || ((print L "<span id=red>Error at 3 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 3 for S,PF in classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ classes a SET last_release_rank = 3
      WHERE (concept_id, language, lui) IN
	    (SELECT concept_id, language, lui FROM classes
             WHERE last_release_rank != 3
	       AND tobereleased IN ('Y','y')
	     MINUS
	     SELECT concept_id, language, lui FROM pref_lui_for_cui_lat)
	AND (concept_id, language, sui) IN
	    (SELECT concept_id, language, sui FROM pref_sui_for_cui_lat_lui)
	AND tobereleased IN ('Y','y')
        AND last_release_rank != 3
    }) || ((print L "<span id=red>Error at 4 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 2 for P,VF in classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ classes a SET last_release_rank = 2
      WHERE (concept_id, language, lui) IN
	    (SELECT concept_id, language, lui FROM pref_lui_for_cui_lat)
	AND (concept_id, language, sui) IN
	    (SELECT concept_id, language, sui FROM classes
             WHERE last_release_rank != 2
	       AND tobereleased IN ('Y','y')
	     MINUS
	     SELECT concept_id, language, sui FROM pref_sui_for_cui_lat_lui)
	AND tobereleased IN ('Y','y')
        AND last_release_rank != 2
    }) || ((print L "<span id=red>Error at 5 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 1 for S,VF in classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ classes a SET last_release_rank = 1
      WHERE (concept_id, language, lui) IN
	    (SELECT concept_id, language, lui FROM classes
             WHERE last_release_rank != 1
	       AND tobereleased IN ('Y','y')
	     MINUS
	     SELECT concept_id, language, lui FROM pref_lui_for_cui_lat)
	AND (concept_id, language, sui) IN
	    (SELECT concept_id, language, sui FROM classes
             WHERE last_release_rank != 1
	       AND tobereleased IN ('Y','y')
	     MINUS
	     SELECT concept_id, language, sui FROM pref_sui_for_cui_lat_lui)
	AND tobereleased IN ('Y','y')
        AND last_release_rank != 1
    }) || ((print L "<span id=red>Error at 6 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 0 in classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ classes a SET last_release_rank = 0
      WHERE tobereleased IN ('N','n')
        AND last_release_rank != 0
    }) || ((print L "<span id=red>Error at 7 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 0 in dead_classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ dead_classes a SET last_release_rank = 0
      WHERE last_release_rank != 0
    }) || ((print L "<span id=red>Error at 8 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 4 for P,PF in foreign_classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ foreign_classes a SET last_release_rank = 4
      WHERE (concept_id, language, lui) IN
	    (SELECT concept_id, language, lui FROM pref_lui_for_cui_lat)
	AND (concept_id, language, sui) IN
	    (SELECT concept_id, language, sui FROM pref_sui_for_cui_lat_lui)
	AND tobereleased IN ('Y','y')
        AND last_release_rank != 4
    }) || ((print L "<span id=red>Error at 9 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 3 for S,PF in foreign_classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ foreign_classes a SET last_release_rank = 3
      WHERE (concept_id, language, lui) IN
	    (SELECT concept_id, language, lui FROM foreign_classes
             WHERE last_release_rank != 3
	       AND tobereleased IN ('Y','y')
	     MINUS
	     SELECT concept_id, language, lui FROM pref_lui_for_cui_lat)
	AND (concept_id, language, sui) IN
	    (SELECT concept_id, language, sui FROM pref_sui_for_cui_lat_lui)
	AND tobereleased IN ('Y','y')
        AND last_release_rank != 3
    }) || ((print L "<span id=red>Error at 9 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 2 for P,VF in foreign_classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ foreign_classes a SET last_release_rank = 2
      WHERE (concept_id, language, lui) IN
	    (SELECT concept_id, language, lui FROM pref_lui_for_cui_lat)
	AND (concept_id, language, sui) IN
	    (SELECT concept_id, language, sui FROM foreign_classes
             WHERE last_release_rank != 2
	       AND tobereleased IN ('Y','y')
	     MINUS
	     SELECT concept_id, language, sui FROM pref_sui_for_cui_lat_lui)
	AND tobereleased IN ('Y','y')
        AND last_release_rank != 2
    }) || ((print L "<span id=red>Error at 10 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 1 for S,VF in foreign_classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ foreign_classes a SET last_release_rank = 1
      WHERE (concept_id, language, lui) IN
	    (SELECT concept_id, language, lui FROM foreign_classes
             WHERE last_release_rank != 1
	       AND tobereleased IN ('Y','y')
	     MINUS
	     SELECT concept_id, language, lui FROM pref_lui_for_cui_lat)
	AND (concept_id, language, sui) IN
	    (SELECT concept_id, language, sui FROM foreign_classes
             WHERE last_release_rank != 1
	       AND tobereleased IN ('Y','y')
	     MINUS
	     SELECT concept_id, language, sui FROM pref_sui_for_cui_lat_lui)
	AND tobereleased IN ('Y','y')
        AND last_release_rank != 1
    }) || ((print L "<span id=red>Error at 11 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  print L "    Set last_release_rank to 0 in foreign_classes ... ".scalar(localtime)."\n";
  $rc = $dbh->do(qq{
      UPDATE /*+ PARALLEL(a) */ foreign_classes a SET last_release_rank = 0
      WHERE tobereleased IN ('N','n')
        AND last_release_rank != 0
    }) || ((print L "<span id=red>Error at 12 ($DBI::errstr).</span>")
     &&  return);
  print "      Count == $rc\n";

  #
  # Cleanup
  #
  print L "    Cleanup ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN
      MEME_UTILITY.drop_it('table','pref_lui_for_cui_lat');
      MEME_UTILITY.drop_it('table','pref_sui_for_cui_lat_lui');
    END;
    }) ||
    ((print L "<span id=red>Error executing drop 13 ($DBI::errstr).</span>") &&  return);

  close(L);
  return 1;
} # Handle_lrr

##############################################################################
# Handle SIMS Info Maintenance
sub Handle_sims_info {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # Set Versioned_cui
  #
  print L "    Set versioned_cui ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
    	UPDATE sims_info a SET versioned_cui =
          (SELECT min(last_assigned_cui) FROM classes b, string_ui c
           WHERE b.sui = c.sui
             AND a.source = c.string
	     AND source = 'SRC'
	     AND tty = 'VAB'
             AND substr(a.source,0,10) = c.string_pre )
        WHERE source IN (SELECT current_name FROM source_version)
          AND source IN (SELECT source FROM source_rank
			 WHERE source = normalized_source)
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      Count == $row_ct\n";

  #
  # Verify no null vcui
  #
  print L "      Verify no null versioned_cuis ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT source FROM sims_info WHERE versioned_cui IS NULL
    AND source in (SELECT current_name FROM source_version WHERE previous_name IS NOT NULL)
    AND source in (SELECT source FROM source_rank
		   WHERE source=normalized_source)
  }) || ((print L "<span id=red>Error preparing qa1 ($DBI::errstr).</span>")
     && return);
  $sh->execute ||
    ((print L "<span id=red>Error executing qa1 ($DBI::errstr).</span>")
     &&  return);
  $ct = 0;
  while (($sab) = $sh->fetchrow_array){
    $ct++;
    if ($ct == 50) { print L "       Failed ...\n"; last; }
    print L "      $sab\n";
  }
  if ($ct == 0) { print L "      Passed\n"; }
  else { return; }



  #
  # Set root cui
  #
  print L "    Set root_cui ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
    	UPDATE sims_info a SET root_cui =
          (SELECT min(last_assigned_cui) FROM classes b, string_ui c, source_rank r
           WHERE b.sui = c.sui
             AND a.source = r.source
	     AND r.stripped_source = c.string
	     AND r.source = r.normalized_source
	     AND b.source = 'SRC'
	     AND tty = 'RAB'
             AND substr(r.stripped_source,0,10) = c.string_pre )
        WHERE source IN (SELECT current_name FROM source_version)
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      Count == $row_ct\n";

  #
  # Verify no null rcui
  #
  print L "      Verify no null versioned_cuis ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT source FROM sims_info WHERE root_cui IS NULL
    AND source in (SELECT current_name FROM source_version)
    AND source in (SELECT source FROM source_rank
		   WHERE source=normalized_source)
  }) || ((print L "<span id=red>Error preparing qa1 ($DBI::errstr).</span>")
     && return);
  $sh->execute ||
    ((print L "<span id=red>Error executing qa1 ($DBI::errstr).</span>")
     &&  return);
  $ct = 0;
  while (($sab) = $sh->fetchrow_array){
    $ct++;
    if ($ct == 50) { print L "       Failed ...\n"; last; }
    print L "      $sab\n";
  }
  if ($ct == 0) { print L "      Passed\n"; }
  else { return; }



  #
  # Set TFR, CFR
  #
  print L "    Set term_frequency, cui_frequency ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
	 SELECT /*+ PARALLEL(b) */
	        count(distinct concept_id||sui) as tfr,
                count(distinct concept_id) as cfr,
		b.source
         FROM classes b, source_rank c, source_version d
         WHERE c.source = c.normalized_source
	   AND c.source = current_name
	   AND b.source = c.source
         GROUP BY b.source
  }) || ((print L "<span id=red>Error preparing qa1 ($DBI::errstr).</span>")
     && return);
  $sh->execute ||
    ((print L "<span id=red>Error executing qa1 ($DBI::errstr).</span>")
     &&  return);
  $ct = 0;
  while (($tfr,$cfr,$sab) = $sh->fetchrow_array){
      print L "      $sab term_frequency=$tfr, cui_frequency=$cfr\n";
      $row_ct = $dbh->do(qq{
 	UPDATE sims_info
	SET term_frequency = ?, cui_frequency = ?
	WHERE source = ?
	     }, undef, $tfr, $cfr, $sab);
      unless ($row_ct) {
	((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
	 &&  return);
      }
  }

  #
  # Set TTYL
  #
  $prev_sab = "";
  $list = "";
  print L "    Set term_type_list ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT /*+ parallel(a) */
        DISTINCT a.source, tty
    FROM classes a, source_rank b, source_version c
    WHERE a.source = b.source
      AND b.normalized_source = c.current_name
      AND tobereleased in ('Y','y') and termgroup not in ('MTH/MM','MTH/TM')
    UNION ALL
    SELECT /*+ parallel(a) */
        DISTINCT a.source, tty
    FROM foreign_classes a, source_rank b, source_version c
    WHERE a.source = b.source
      AND b.normalized_source = c.current_name
      AND tobereleased in ('Y','y')
    ORDER BY source, tty
  }) || ((print L "<span id=red>Error preparing qa1 ($DBI::errstr).</span>")
     && return);
  $sh->execute ||
    ((print L "<span id=red>Error executing qa1 ($DBI::errstr).</span>")
     &&  return);
  $ct = 0;
  do {
    ($sab,$tty) = $sh->fetchrow_array;
    if ($sab eq $prev_sab) {
      $list = "$list$tty,";
    } else {
      $list =~ s/,$//;
      print L "      $prev_sab term_type_list=$list\n";
      $row_ct = $dbh->do(qq{
	    UPDATE sims_info SET term_type_list = ?
   	    WHERE source = ?
	     }, undef, $list, $prev_sab);
      unless ($row_ct) {
	((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
	 &&  return);
      }
      $list = "$tty,";
    }
	$prev_sab = $sab;
  } while ($sab);


  #
  # Set ATNL
  #
  $prev_sab = "";
  $list = "";
  print L "    Set attribute_name_list ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT /*+ PARALLEL(a) */ distinct a.source, attribute_name
    FROM attributes a, source_rank b, source_version c
    WHERE tobereleased in ('Y','y')
      AND a.source = b.source AND b.normalized_source = c.current_name
      AND attribute_level = 'S'
      AND attribute_name not IN
        ('DEFINITION','ATX_REL','MRLO','HDA','HPC','COC','LEXICAL_TAG',
         'XMAPTO','XMAP','XMAPFROM','COMPONENTHISTORY')
    UNION ALL
    SELECT current_name, 'LT' FROM source_version
    WHERE source = 'MSH'
    ORDER BY source, attribute_name
  }) || ((print L "<span id=red>Error preparing qa1 ($DBI::errstr).</span>")
     && return);
  $sh->execute ||
    ((print L "<span id=red>Error executing qa1 ($DBI::errstr).</span>")
     &&  return);
  $ct = 0;
  do {
    ($sab,$atn) = $sh->fetchrow_array;
    if ($sab eq $prev_sab) {
      $list = "$list$atn,";
    } else {
      $list =~ s/,$//;
      print L "      $prev_sab attribute_name_list=$list\n";
      $row_ct = $dbh->do(qq{
	    UPDATE sims_info SET attribute_name_list = ?
   	    WHERE source = ?
	     }, undef, $list, $prev_sab);
      unless ($row_ct) {
	((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
	 &&  return);
      }
      $list = "$atn,";
    }
	$prev_sab = $sab;
  } while ($sab);


  #
  # Add current year for NLM-MED if not already there
  #
  print L "    Set NLM-MED MED<year> ATNL ... ".scalar(localtime)."\n";
  @lt = localtime;
  $year = $lt[5]+1900;
  $row_ct = $dbh->do(qq{
	    UPDATE sims_info SET attribute_name_list = attribute_name_list || ',MED$year'
	    WHERE source = 'NLM-MED'
	      AND attribute_name_list NOT LIKE '%,MED$year'
  });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return);
  }


  #
  # Set MTH list
  #
  $list = "DA,FROMRSAB,FROMVSAB,LT,MAPSETGRAMMAR,MAPSETID,MAPSETNAME,MAPSETRSAB,MAPSETTYPE,MAPSETVSAB,MR,MTH_MAPFROMCOMPLEXITY,MTH_MAPFROMEXHAUSTIVE,MTH_MAPSETCOMPLEXITY,MTH_MAPTOCOMPLEXITY,MTH_MAPTOEXHAUSTIVE,NH,SOS,ST,TORSAB,TOVSAB";
  print L "    Set MTH attribute_name_list ... ".scalar(localtime)."\n";
  print L "      $list\n";
  $row_ct = $dbh->do(qq{
	    UPDATE sims_info SET attribute_name_list = ?
	    WHERE source = 'MTH'
  }, undef, $list);
  unless ($row_ct) {
    ((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return);
  }


  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;

}; # end Handle_sims_info


##############################################################################
# Handle Set Release CUIs
sub Handle_classes_feedback {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);


  #
  # Make sure that each releasable atom has a last_assigned_cui!!
  #
  print L "    Verify each releasable atom has a last_assigned_cui ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT atom_id,source,concept_id
    FROM classes
    WHERE tobereleased in ('Y','y') and last_assigned_cui is null
  }) || ((print L "<span id=red>Error preparing qa1 ($DBI::errstr).</span>")
     && return);
  $sh->execute ||
    ((print L "<span id=red>Error executing qa1 ($DBI::errstr).</span>")
     &&  return);
  $ct = 0;
  while (($atom_id,$sab, $concept_id) = $sh->fetchrow_array){
    $ct++;
    if ($ct == 50) { print L "      ...\n"; last; }
    print L "      $atom_id, $sab, $concept_id\n";
  }
  if ($ct == 0) { print L "      Passed\n"; }
  else { return; }


  #
  # Set classes.last_release_cui, released for releasable atoms!
  #
  print L "    Set last_release_cui,released (tbr=yY) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      UPDATE /*+ parallel(a) */ classes a
	      SET last_release_cui = last_assigned_cui, released = 'A'
          WHERE tobereleased in ('Y','y')
            and termgroup not in ('MTH/MM','MTH/TM')
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      classes Count == $row_ct\n";
  $dbh->commit;
  $row_ct = $dbh->do(qq{
	      UPDATE /*+ parallel(a) */ foreign_classes a
	      SET (last_release_cui,released) =
		(SELECT last_assigned_cui, 'A'
		 FROM classes b WHERE eng_atom_id =  atom_id)
              WHERE tobereleased in ('Y','y')
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      foreign_classes Count == $row_ct\n";
  $dbh->commit;


  #
  # Set classes.last_release_cui, released for unreleasable atoms!
  #
  print L "    Set last_release_cui,released (tbr=nN) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      UPDATE /*+ parallel(a) */ classes a
	      SET last_release_cui = null, released = 'N'
              WHERE tobereleased in ('N','n')
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      classes Count == $row_ct\n";
  $dbh->commit;
  $row_ct = $dbh->do(qq{
	      UPDATE classes a
	      SET last_release_cui = null, released = 'N'
              WHERE source='MTH' and tty in ('TM','MM')
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      classes Count == $row_ct\n";
  $dbh->commit;
  $row_ct = $dbh->do(qq{
	      UPDATE /*+ parallel(a) */ dead_classes a
              SET last_release_cui = null, released = 'N'
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      dead_classes Count == $row_ct\n";
  $dbh->commit;
  $row_ct = $dbh->do(qq{
	      UPDATE /*+ parallel(a) */ foreign_classes
              SET last_release_cui = null, released = 'N'
              WHERE tobereleased in ('N','n')
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      foreign_classes Count == $row_ct\n";
  $dbh->commit;

  #
  # Set relationships.released
  #
  print L "    Set relationships.released (tbr=yY) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      UPDATE /*+ parallel(a) */ relationships a
	      SET released = DECODE(tobereleased,'Y','A','y','A','?','A','N')
          WHERE released != DECODE(tobereleased,'Y','A','y','A','?','A','N')
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      relationships Count == $row_ct\n";
  $dbh->commit;

  #
  # Set context_relationships.released
  #
  print L "    Set context_relationships.released (tbr=yY) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      UPDATE /*+ parallel(a) */ context_relationships a
	      SET released = DECODE(tobereleased,'Y','A','y','A','?','A','N')
              WHERE released != DECODE(tobereleased,'Y','A','y','A','?','A','N')
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      context_relationships Count == $row_ct\n";
  $dbh->commit;

  #
  # Set attributes.released
  #
  print L "    Set attributes.released (tbr=yY) ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
	      UPDATE /*+ parallel(a) */ attributes a
	      SET released = DECODE(tobereleased,'Y','A','y','A','?','A','N')
              WHERE released != DECODE(tobereleased,'Y','A','y','A','?','A','N')
	     });
  unless ($row_ct) {
    ((print L "<span id=red>Error executing delete 1 ($DBI::errstr).</span>")
     &&  return);
  }
  print L "      attributes Count == $row_ct\n";
  $dbh->commit;

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;

}; # end Handle_classes_feedback

##############################################################################
# Handle MRSAB IMETA,RMETA data and SIMS meta ver
sub Handle_set_imeta_rmeta {
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

  #
  # Set RMETA for sources that are obsolete and do not yet have it set
  #
  print L "    Set RMETA for obsolete sources ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
    UPDATE sims_info SET remove_meta_version=?
    WHERE source in (SELECT previous_name FROM source_version)
      AND remove_meta_version IS NULL
	     }, undef, $old_release);
  unless ($row_ct) {
    (print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return;
  }
  print L "      Count == $row_ct\n";

  #
  # Set RMETA for sources that are obsolete but are not the
  # immediate previous version
  #
  print L "    Set RMETA for non-immediate obsolete sources ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    SELECT source FROM sims_info
    WHERE source in (SELECT string FROM string_ui a, classes b
     	             WHERE a.sui=b.sui AND b.source='SRC'
     	               AND b.tty='VAB' and b.tobereleased in ('N','n'))
      AND remove_meta_version IS NULL
	     }) || (print L "<span id=red>Error preparing to select non-immediate obsolete sources ($DBI::errstr).</span>")
     &&  return;
  $sh->execute ||  (print L "<span id=red>Error selecting non-immediate obsolete sources ($DBI::errstr).</span>")
     &&  return;
  while (($sab)=$sh->fetchrow_array) {
    print L "      $sab\n";
  }

  $row_ct = $dbh->do(qq{
    UPDATE sims_info SET remove_meta_version=?
    WHERE source in (SELECT string FROM string_ui a, classes b
     	             WHERE a.sui=b.sui AND b.source='SRC'
     	               AND b.tty='VAB' and b.tobereleased in ('N','n'))
      AND remove_meta_version IS NULL
	     }, undef, $old_release);
  unless ($row_ct) {
    (print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return;
  }
  print L "      Count == $row_ct\n";

  #
  # Set RMETA for deleted sources
  #
  print L "    Set RMETA for deleted sources ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
    UPDATE sims_info SET remove_meta_version = ?
    WHERE source in
     (SELECT a.source FROM source_rank a, source_version b
      WHERE a.stripped_source = b.source
        AND current_name IS NULL)
      AND remove_meta_version IS NULL
	     }, undef, $old_release);
  unless ($row_ct) {
    (print L "<span id=red>Error executing update 2 ($DBI::errstr).</span>")
     && return;
  }
  print L "      Count == $row_ct\n";

  #
  # Set IMETA for new sources
  #
  print L "    Set IMETA for new/update sources ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
    UPDATE sims_info SET insert_meta_version = ?,
    	meta_ver=?
    WHERE insert_meta_version IS NULL AND source IN
    (SELECT current_name FROM source_version a)
	     }, undef, $new_release, $new_release);
  unless ($row_ct) {
    (print L "<span id=red>Error executing update 3 ($DBI::errstr).</span>")
     && return;
  }
  print L "      Count == $row_ct\n";

  #
  #
  #
  print L "    Set meta_ver for 'Current' sources in SMIS ... ".scalar(localtime)."\n";
  $row_ct = $dbh->do(qq{
      UPDATE sims_info
      SET meta_ver = ?
      WHERE meta_ver='Current'
        AND source in (SELECT string FROM string_ui a, classes b
		       WHERE a.sui = b.sui AND b.source='SRC'
		         AND b.termgroup='VAB' and b.tobereleased in ('Y','y'))
	     }, undef, $new_release);
  unless ($row_ct) {
    (print L "<span id=red>Error executing update 4 ($DBI::errstr).</span>")
      && return;
  }
  print L "      Count == $row_ct\n";

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;

}; # end Handle_set_imeta_rmeta

##############################################################################
# Run set context type patch script
sub Handle_set_context_type {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # The following code is from $MEME_HOME/Patch/set_context_type.csh converted into perl
  #
  print L "    Compute context type and update sims_info ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_ct'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  # Initialze to FULL-NOSIB
  $dbh->do(qq{
        CREATE TABLE ${uniq}_ct (
            source VARCHAR2(20),
            context_type VARCHAR2(100))
    }) || ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  print L "      Find sources with contexts, assume NOSIB ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        INSERT INTO ${uniq}_ct
        SELECT /*+ parallel(cr) */ DISTINCT source,'FULL-NOSIB' AS context_type
        FROM context_relationships cr
        WHERE relationship_name='PAR'
    }) || ((print L "<span id=red>Error executing insert 1 ($DBI::errstr).</span>")
     &&  return);

  # Update where sibs exist
  print L "      Remove NOSIB flags ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        UPDATE ${uniq}_ct SET context_type='FULL'
        WHERE source IN
        (SELECT /*+ PARALLEL(cr) */ DISTINCT source
	 FROM context_relationships cr
         WHERE relationship_name='SIB')
    }) || ((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return);

  # Update where multiple contexts exist
  print L "      Compute MULTIPLE flag ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        UPDATE ${uniq}_ct
        SET context_type = context_type || '-MULTIPLE'
        WHERE source IN
        (SELECT /*+ PARALLEL(cr) */ DISTINCT source
	 FROM context_relationships cr
         WHERE relationship_name='PAR'
        GROUP BY atom_id_1,source HAVING COUNT(*)>1)
    }) || ((print L "<span id=red>Error executing update 2 ($DBI::errstr).</span>")
     &&  return);

  # Update where RELA does not matter for context computation
  print L "      Compute IGNORE-RELA flag (for GO,NIC) ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        UPDATE ${uniq}_ct
        SET context_type = context_type || '-IGNORE-RELA'
        WHERE source IN (SELECT current_name FROM source_version WHERE source in ('GO','NIC'))
    }) || ((print L "<span id=red>Error executing update 2 ($DBI::errstr).</span>")
     &&  return);

  # update source_rank table
  print L "      Set in sims_info.context_type ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        UPDATE sims_info a SET context_type=
        (SELECT context_type FROM ${uniq}_ct b
	 WHERE a.source=b.source)
	WHERE source in (SELECT current_name FROM source_version)
    }) || ((print L "<span id=red>Error executing update 3 ($DBI::errstr).</span>")
     &&  return);

#$MEME_HOME/bin/dump_table.pl -u $userpwd -d $db -q "SELECT source,context_type FROM source_rank WHERE source IN (SELECT current_name FROM source_version)" | @PATH_TO_PERL@ -ne '/(.*)\|(.*)/; printf "%20s%20s\n", $1,$2;'

  $sh = $dbh->prepare(qq{
     SELECT source, context_type FROM sims_info WHERE source in
     (SELECT current_name FROM source_version)
    }) || ((print L "<span id=red>Error preparing report 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing report 1 ($DBI::errstr).</span>")
     &&  return);

  printf L "      %20s%20s\n","Source","Context Type";
  printf L "      %20s%20s\n","------","----------------------";
  while (($sab,$cxty) = $sh->fetchrow_array){
    printf L "      %20s%20s\n",$sab,$cxty;
  }

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;

}

##############################################################################
# Run set official name patch script
sub Handle_set_official_name {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # The following code is from $MEME_HOME/Patch/set_official_name.csh converted into perl
  #
  print L "    Compute official name ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tf'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE table ${uniq}_tf AS SELECT a.atom_name ab, b.atom_name pt
        FROM atoms a, atoms b, classes c, classes d
        WHERE a.atom_id=c.atom_id AND b.atom_id=d.atom_id
        AND c.source='SRC' AND d.source='SRC'
        AND c.tobereleased ='Y'
        AND d.tobereleased='Y'
        AND c.termgroup='SRC/VAB'
        AND d.termgroup='SRC/VPT'
        AND c.concept_id=d.concept_id
	UNION
	SELECT a.atom_name ab, b.atom_name pt
        FROM atoms a, atoms b, classes c, classes d
        WHERE a.atom_id=c.atom_id AND b.atom_id=d.atom_id
        AND c.source='SRC' AND d.source='SRC'
        AND c.tobereleased ='Y'
        AND d.tobereleased='Y'
        AND c.termgroup='SRC/RAB'
        AND d.termgroup='SRC/RPT'
        AND c.concept_id=d.concept_id
        AND a.atom_name IN ('SRC','MTH','NLM-MED')
    }) || ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  print L "    Set official name ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        UPDATE sims_info a
        SET source_official_name = (SELECT pt FROM ${uniq}_tf WHERE source=ab)
        WHERE source IN (SELECT ab FROM ${uniq}_tf)
    }) || ((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_sn'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  print L "    Compute short name ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        CREATE table ${uniq}_sn AS SELECT
          (SELECT current_name FROM source_version
           WHERE source=a.atom_name) ab, b.atom_name pt
        FROM atoms a, atoms b, classes c, classes d
        WHERE a.atom_id=c.atom_id AND b.atom_id=d.atom_id
        AND c.source='SRC' AND d.source='SRC'
        AND c.tobereleased ='Y'
        AND d.tobereleased='Y'
        AND c.termgroup='SRC/RAB'
        AND d.termgroup='SRC/SSN'
        AND c.concept_id=d.concept_id
	UNION
	SELECT a.atom_name ab, b.atom_name pt
        FROM atoms a, atoms b, classes c, classes d
        WHERE a.atom_id=c.atom_id AND b.atom_id=d.atom_id
        AND c.source='SRC' AND d.source='SRC'
        AND c.tobereleased ='Y'
        AND d.tobereleased='Y'
        AND c.termgroup='SRC/RAB'
        AND d.termgroup='SRC/SSN'
        AND c.concept_id=d.concept_id
        AND a.atom_name IN ('SRC','MTH','NLM-MED')
    }) || ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  print L "    Set short name ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        UPDATE sims_info a
        SET source_short_name = (SELECT pt FROM ${uniq}_sn WHERE source=ab)
        WHERE source IN (SELECT ab FROM ${uniq}_sn)
    }) || ((print L "<span id=red>Error executing update 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Cleanup
  #
  print L "    Cleanup ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    BEGIN
      MEME_UTILITY.drop_it('table', '${uniq}_sn');
      MEME_UTILITY.drop_it('table', '${uniq}_tf');
    END;}) ||
    ((print L "<span id=red>Error preparing drop 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 3 ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;
}

##############################################################################
# Run MM/MM Management
#
sub Handle_mthtm {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  open(CMD,"$ENV{MEME_HOME}/bin/mthtm.pl $host $port -d ALL -t MTH/MM -s R -i $db MTH | sed 's/^/      /' |") ||
    ((print L "<span id=red>Error running mthtm.pl ($! $?).</span>")
     &&  return);
  while (<CMD>) {
    print L $_;
  }
  close(CMD);

  #
  # Find all MTH/MM atoms
  #
  print L "    Find all MTH/MM atoms ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t1'); END;") ||
    ((print L "<span id=red>Error preparing drop 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE table ${uniq}_t1 AS
        SELECT UPPER(atom_name) AS string, concept_id, a.atom_id
        FROM classes a, atoms b
        WHERE tobereleased IN ('Y','y')
        AND source = 'MTH' AND termgroup = 'MTH/MM'
        AND a.atom_id = b.atom_id
    }) || ((print L "<span id=red>Error executing create 1 ($DBI::errstr).</span>")
     &&  return);

  #
  # Find base strings for all MTH.MM atoms
  #
  print L "    Find base strings for all MTH/MM atoms ... ".scalar(localtime)."\n";

  $dbh->do(qq{
        UPDATE ${uniq}_t1 SET string = SUBSTR(string,0,LENGTH(string)-4)
        WHERE string LIKE '% <_>'
    }) || ((print L "<span id=red>Error executing update 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        UPDATE ${uniq}_t1 SET string = SUBSTR(string,0,LENGTH(string)-5)
        WHERE string LIKE '% <__>'
    }) || ((print L "<span id=red>Error executing update 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        ANALYZE TABLE ${uniq}_t1 COMPUTE STATISTICS
    }) || ((print L "<span id=red>Error executing analyze 1 ($DBI::errstr).</span>")
     &&  return);

  #
  # Find all atoms matching MTH.MMs, exclude MTH/PT atoms
  #
  print L "    Find all atoms matching MTH/MMs, exclude MTH/PT atoms ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_t2'); END;") ||
    ((print L "<span id=red>Error preparing drop 2 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE table ${uniq}_t2 AS
        SELECT UPPER(b.atom_name) AS string, a.concept_id, c.atom_id
        FROM classes a, atoms b, ${uniq}_t1 c
        WHERE a.atom_id = b.atom_id
        AND a.language = 'ENG'
        AND termgroup NOT IN ('MTH/MM','MTH/PT')
        AND tobereleased IN ('Y','y')
        AND a.concept_id = c.concept_id
    }) || ((print L "<span id=red>Error executing create 2 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        ANALYZE TABLE ${uniq}_t2 COMPUTE STATISTICS
    }) || ((print L "<span id=red>Error executing analyze 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Find dangling MTH/MMs and matching MTH/PTs
  #
  print L "    Find dangling MTH/MMs and matching MTH/PTs ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_dangling_mms'); END;") ||
    ((print L "<span id=red>Error preparing drop 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 3 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE table ${uniq}_dangling_mms AS
        SELECT DISTINCT string, concept_id, atom_id FROM ${uniq}_t1
        MINUS SELECT string, concept_id, atom_id FROM ${uniq}_t2
    }) || ((print L "<span id=red>Error executing create 3 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_todelete'); END;") ||
    ((print L "<span id=red>Error preparing drop 4 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop 4 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_todelete AS
        SELECT atom_id AS row_id FROM ${uniq}_dangling_mms
        UNION
        SELECT a.atom_id
        FROM classes a, atoms b, ${uniq}_dangling_mms c
        WHERE a.atom_id = b.atom_id
        AND termgroup IN ('MTH/PT')
        AND tobereleased IN ('Y','y')
        AND a.concept_id = c.concept_id
        AND string = upper(atom_name)
    }) || ((print L "<span id=red>Error executing create 4 ($DBI::errstr).</span>")
     &&  return);

  #
  # Remove dangling MTH/MMs and matching MTH/PTs
  #
  print L "    Remove dangling MTH/MMs and matching MTH/PTs ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_todelete
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
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'T',
                id_type => 'C',
                authority => 'MAINTENANCE',
                table_name => '${uniq}_todelete',
                work_id => 0,
                status => 'R',
		new_value => 'N');
            MEME_UTILITY.drop_it('table','${uniq}_todelete');
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
  # Cleanup
  #
  print L "    Cleanup ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    BEGIN
      MEME_UTILITY.drop_it('table', '${uniq}_dangling_mms');
      MEME_UTILITY.drop_it('table', '${uniq}_t1');
      MEME_UTILITY.drop_it('table', '${uniq}_t2');
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
# Run production mid cleanup patch script
sub Handle_prod_mid_cleanup {
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
  &EnableBuffer(1000000);
  $dbh->do(qq{ALTER SESSION set sort_area_size=200000000
    }) || ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);
  $dbh->do(qq{ALTER SESSION set hash_area_size=200000000
    }) || ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # The following code is from $MEME_HOME/Patch/production_mid_cleanup.csh converted into perl
  #
  print L "    Remove all non-MEME tables ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
	    SELECT table_name from user_tables
	    MINUS
	    (SELECT table_name from meme_tables
	     UNION SELECT 'PLAN_TABLE' FROM dual
	     UNION SELECT 'ACRONYM' FROM dual
	     UNION SELECT 'ANTINORM' FROM dual
	     UNION SELECT 'CANONICAL' FROM dual
	     UNION SELECT 'DERIVATION' FROM dual
	     UNION SELECT 'INFLECTION' FROM dual
	     UNION SELECT 'LEXSYNONYM' FROM dual
	     UNION SELECT 'PROPERNOUN' FROM dual
	     UNION SELECT 'CHAINED_ROWS' FROM dual
	     UNION SELECT 'MTHSTATS' FROM dual
         UNION SELECT 'CONTENT_VIEW_MEMBERS' from dual)
   }) ||
    ((print L "<span id=red>Error preparing to drop non-meme tables ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing drop non-meme tables ($DBI::errstr).</span>")
     &&  return);

  while (($table_name) = $sh->fetchrow_array) {
	if ($table_name !~ /JAVA/) {
	  $dbh->do(qq{BEGIN MEME_UTILITY.drop_it('table',?); END;}, undef, $table_name) ||
    	((print L "<span id=red>Error dropping $table_name ($DBI::errstr).</span>") &&  return);
      print L "      $table_name dropped...\n";
	}
  }

  #
  #  Create table of "current sources"
  #
  print L "    Calculate current sources ... ".scalar(localtime)."\n";
  $dbh->do(qq{BEGIN MEME_UTILITY.drop_it('table', 'current_sources'); END;}) ||
    ((print L "<span id=red>Error dropping current_sources table ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE table current_sources AS
        SELECT source FROM source_rank
        WHERE normalized_source IN (SELECT current_name FROM source_version)
    }) || ((print L "<span id=red>Error creating old_sources ($DBI::errstr).</span>")
     &&  return);

  #
  # Remove known exceptions (EDIT FOR EXCEPTIONS)
  #
  print L "      insert/remove no exceptions ... ".scalar(localtime)."\n";

  #
  # Show current sources
  #
  $sh = $dbh->prepare(qq{SELECT source FROM current_sources ORDER BY 1})
  || ((print L "<span id=red>Error preparing to show old sources ($DBI::errstr).</span>")
     &&  return);
  $sh->execute()
  || ((print L "<span id=red>Error executing to show old sources ($DBI::errstr).</span>")
     &&  return);
  print L "    Current sources include:\n     ";
  while (($sab) = $sh->fetchrow_array) {
    print L " $sab";
  }
  print L "\n";

  #
  # Show obsolete sources
  #
  $sh = $dbh->prepare(qq{
    SELECT source FROM source_rank
    MINUS SELECT source FROM current_sources})
  || ((print L "<span id=red>Error preparing to show old sources ($DBI::errstr).</span>")
     &&  return);
  $sh->execute()
  || ((print L "<span id=red>Error executing to show old sources ($DBI::errstr).</span>")
     &&  return);
  print L "    Obsolete sources include:\n     ";
  while (($sab) = $sh->fetchrow_array) {
    print L " $sab";
  }
  print L "\n";

  #
  # Calculate old atoms
  #
  print L "    Calculate old atoms (atoms with old sources) ... ".scalar(localtime)."\n";
  $dbh->do(qq(BEGIN MEME_UTILITY.drop_it('table', 'old_atom_ids'); END;}) ||
    ((print L "<span id=red>Error dropping old_atom_ids ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE table old_atom_ids AS
        SELECT atom_id FROM classes
  	    WHERE source not IN (SELECT * FROM current_sources)
        UNION
        SELECT atom_id FROM foreign_classes
        WHERE source NOT IN (select * from current_sources)
    }) || ((print L "<span id=red>Error creating old_atom_ids ($DBI::errstr).</span>")
     &&  return);

  #
  # Delete bad SRC/MTH/NLM02 stuff
  # Inform people that to make SRC/MTH stuff go away
  # it must be marked with tbr='N'
  #
  print L "    Calculate old,new atoms (SRC,MTH) ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        INSERT INTO old_atom_ids
        SELECT atom_id FROM classes WHERE source IN ('SRC','MTH')
          AND tobereleased in ('N') AND concept_id > 999
    }) || ((print L "<span id=red>Error adding SRC,MTH to old_atom_ids ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE INDEX x_oai_id on old_atom_ids (atom_id) COMPUTE STATISTICS PARALLEL
    }) || ((print L "<span id=red>Error analyzing old_atom_ids ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE table new_atom_ids AS
        (SELECT atom_id FROM classes UNION SELECT atom_id FROM foreign_classes)
  	    MINUS select atom_id from old_atom_ids
    }) || ((print L "<span id=red>Error creating new_atom_ids ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
        CREATE INDEX x_nai_id on new_atom_ids (atom_id) COMPUTE STATISTICS PARALLEL
    }) || ((print L "<span id=red>Error creating new_atom_ids index ($DBI::errstr).</span>")
     &&  return);


  print L "    Remove references in tables to old atoms ... ".scalar(localtime)."\n";

  #
  # Remove references to old atoms from worklists
  #
  print L "      worklists ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
	  ct                 INTEGER;
	  CURSOR cur IS
	    (SELECT upper(checklist_name) as name FROM meow.ems_checklist_info
	     UNION SELECT upper(worklist_name) FROM meow.wms_worklist_info)
	     INTERSECT SELECT upper(table_name) FROM all_tables WHERE owner='MEOW';
  	  cv cur%rowtype;
    BEGIN
	    ct := 0;
	    OPEN cur;
		LOOP
	    	FETCH cur into cv;
	    	EXIT when cur%NOTFOUND;

			EXECUTE IMMEDIATE
		        'DELETE FROM meow.' || cv.name || ' WHERE atom_id IN
                   (SELECT atom_id FROM old_atom_ids) ';
			COMMIT;
			--MEME_UTILITY.put_message('HANDLED ' || cv.name || '.');
		END LOOP;
    END;
    }) ||
    ((print L "<span id=red>Error cleaning up worklists ($DBI::errstr).</span>") &&  return);
  &FlushBuffer;

  #
  # Remove references to old atoms from atom_ordering
  #
  print L "      atom_ordering ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN
    	MEME_UTILITY.drop_it('table','atom_ordering_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE atom_ordering_$$ AS
             SELECT * FROM atom_ordering WHERE atom_id IN
    	      (SELECT atom_id FROM new_atom_ids)';
	    MEME_UTILITY.drop_it('table','atom_ordering');
    	EXECUTE IMMEDIATE
    		'RENAME atom_ordering_$$ TO atom_ordering';
    	EXECUTE IMMEDIATE 'GRANT ALL ON atom_ordering TO MID_USER';
    END;
    }) ||
    ((print L "<span id=red>Error cleaning up atom_ordering ($DBI::errstr).</span>") &&  return);

  #
  # Remove references to old atoms from atoms
  #
  print L "      atoms ... ".scalar(localtime)."\n";
  $dbh->do(qq{
	BEGIN
        MEME_UTILITY.drop_it('table','atoms_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE atoms_$$ AS
             SELECT /*+ PARALLEL(a) */ * FROM atoms a WHERE atom_id IN
    	      (SELECT atom_id FROM new_atom_ids)';
	    MEME_UTILITY.drop_it('table','atoms');
    	EXECUTE IMMEDIATE
    		'RENAME atoms_$$ TO atoms';
        EXECUTE IMMEDIATE
        	'ALTER TABLE ATOMS ADD CONSTRAINT ATOMS_PK PRIMARY KEY (atom_id)';
    	EXECUTE IMMEDIATE 'GRANT ALL ON atoms TO MID_USER';
    END;
    }) ||
    ((print L "<span id=red>Error cleaning up atoms ($DBI::errstr).</span>") &&  return);

  #
  # Remove references to old atoms from foreign_classes
  #
  print L "      foreign_classes ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN
    	MEME_UTILITY.drop_it('table','foreign_classes_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE foreign_classes_$$ AS
             SELECT * FROM foreign_classes a
             WHERE atom_id IN
    	      (SELECT atom_id FROM new_atom_ids)
               AND eng_atom_id IN
    	      (SELECT atom_id FROM new_atom_ids)';
	    MEME_UTILITY.drop_it('table','foreign_classes');
    	EXECUTE IMMEDIATE
    		'RENAME foreign_classes_$$ TO foreign_classes';
        EXECUTE IMMEDIATE
        	'ALTER TABLE FOREIGN_CLASSES ADD CONSTRAINT F_CLASSES_PK
        	 PRIMARY KEY (atom_id)';
    	EXECUTE IMMEDIATE 'GRANT ALL ON foreign_classes TO MID_USER';
    END;
    }) ||
    ((print L "<span id=red>Error cleaning up foreign_classes ($DBI::errstr).</span>") &&  return);

  #
  # Remove references to old atoms from classes.
  #
  print L "      classes ... ".scalar(localtime)."\n";
  $dbh->do(qq{
  	BEGIN
        MEME_UTILITY.drop_it('table','classes_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE classes_$$ AS
             SELECT * FROM classes a WHERE atom_id IN
    	      (SELECT atom_id FROM new_atom_ids)
    	     ORDER BY concept_id';
	    MEME_UTILITY.drop_it('table','classes');
    	EXECUTE IMMEDIATE
    		'RENAME classes_$$ TO classes';
        EXECUTE IMMEDIATE
        	'ALTER TABLE CLASSES ADD CONSTRAINT CLASSES_PK
        	 PRIMARY KEY (atom_id)';
    	EXECUTE IMMEDIATE 'GRANT ALL ON classes TO MID_USER';
    END;
    }) ||
    ((print L "<span id=red>Error cleaning up classes ($DBI::errstr).</span>") &&  return);

  # Reopen connection
  $dbh->disconnect;
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer(1000000);
  $dbh->do(qq{ALTER SESSION set sort_area_size=200000000
    }) || ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);
  $dbh->do(qq{ALTER SESSION set hash_area_size=200000000
    }) || ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # Find old concepts
  #
  print L "    Calculate old,new concepts ... ".scalar(localtime)."\n";
  $dbh->do(qq{BEGIN MEME_UTILITY.drop_it('table', 'old_concept_ids'); END;}) ||
    ((print L "<span id=red>Error dropping old_concept_ids ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
    CREATE TABLE old_concept_ids AS
  	SELECT concept_id FROM concept_status
 	MINUS
	SELECT concept_id FROM classes
  }) || ((print L "<span id=red>Error creating old_concept_ids ($DBI::errstr).</span>")
	 &&  return);

  $dbh->do(qq{
        CREATE INDEX x_oci_id on old_concept_ids (concept_id) COMPUTE STATISTICS PARALLEL
    }) || ((print L "<span id=red>Error analyzing old_concept_ids ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{BEGIN MEME_UTILITY.drop_it('table', 'new_concept_ids'); END;}) ||
    ((print L "<span id=red>Error dropping new_concept_ids ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
    CREATE TABLE new_concept_ids AS
  	SELECT concept_id FROM concept_status
 	MINUS
	SELECT concept_id FROM old_concept_ids
  }) || ((print L "<span id=red>Error creating new_concept_ids ($DBI::errstr).</span>")
	 &&  return);

  $dbh->do(qq{
        CREATE INDEX x_nci_id on new_concept_ids (concept_id) COMPUTE STATISTICS PARALLEL
    }) || ((print L "<span id=red>Error indexing new_concept_ids ($DBI::errstr).</span>")
     &&  return);

  print L "    Remove references in tables to old atoms, old_concepts ... ".scalar(localtime)."\n";

  #
  # Remove relationships connected to old atoms and concepts
  #
  print L "      relationships ... ".scalar(localtime)."\n";
  $dbh->do(qq{
  	BEGIN
        MEME_UTILITY.drop_it('table','relationships_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE relationships_$$ AS
             SELECT /*+ PARALLEL(a) */ * FROM relationships a
             WHERE relationship_level != ''C''
               AND source IN (SELECT source FROM current_sources)
               AND atom_id_1 IN
    	      (SELECT atom_id FROM new_atom_ids)
    	       AND atom_id_2 IN
    	      (SELECT atom_id FROM new_atom_ids)';
    	EXECUTE IMMEDIATE
    		'INSERT INTO relationships_$$
             SELECT * FROM relationships a
             WHERE relationship_level = ''C''
               AND concept_id_1 IN
    	      (SELECT concept_id FROM new_concept_ids)
    	       AND concept_id_2 IN
    	      (SELECT concept_id FROM new_concept_ids)
    	     ORDER BY concept_id_1';
	    MEME_UTILITY.drop_it('table','relationships');
    	EXECUTE IMMEDIATE
    		'RENAME relationships_$$ TO relationships';
        EXECUTE IMMEDIATE
        	'ALTER TABLE RELATIONSHIPS ADD CONSTRAINT RELATIONSHIPS_PK
        	 PRIMARY KEY (relationship_id)';
    	EXECUTE IMMEDIATE 'GRANT ALL ON relationships TO MID_USER';
    END;
    }) ||
    ((print L "<span id=red>Error fixing relationships ($DBI::errstr).</span>") &&  return);

  # Reopen connection
  $dbh->disconnect;
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer(1000000);
  $dbh->do(qq{ALTER SESSION set sort_area_size=200000000
    }) || ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);
  $dbh->do(qq{ALTER SESSION set hash_area_size=200000000
    }) || ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # Remove context_relationships connected to old atoms and concepts
  #
  print L "      context_relationships ... ".scalar(localtime)."\n";
  $dbh->do(qq{
  	BEGIN
        MEME_UTILITY.drop_it('table','cxt_relationships_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE cxt_relationships_$$ AS
             SELECT * FROM context_relationships a
             WHERE source IN (SELECT source FROM current_sources)
               AND atom_id_1 IN
    	      (SELECT atom_id FROM new_atom_ids)
    	       AND atom_id_2 IN
    	      (SELECT atom_id FROM new_atom_ids)
    	     ORDER BY atom_id_1';
	    MEME_UTILITY.drop_it('table','context_relationships');
    	EXECUTE IMMEDIATE
    		'RENAME cxt_relationships_$$ TO context_relationships';
        EXECUTE IMMEDIATE
        	'ALTER TABLE CONTEXT_RELATIONSHIPS ADD CONSTRAINT
        	 CONTEXT_RELATIONSHIPS_PK PRIMARY KEY (relationship_id)';
    	EXECUTE IMMEDIATE 'GRANT ALL ON context_relationships TO MID_USER';
    END;
    }) ||
    ((print L "<span id=red>Error fixing context_relationships ($DBI::errstr).</span>") &&  return);

  # Reopen connection
  $dbh->disconnect;
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer(1000000);
  $dbh->do(qq{ALTER SESSION set sort_area_size=200000000
    }) || ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);
  $dbh->do(qq{ALTER SESSION set hash_area_size=200000000
    }) || ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # Remove attributes connected to old atoms and concepts
  #
  print L "      attributes ... ".scalar(localtime)."\n";
  $dbh->do(qq{
  	BEGIN
        MEME_UTILITY.drop_it('table','attributes_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE attributes_$$ AS
             SELECT /*+ PARALLEL(a) */ * FROM attributes a
             WHERE attribute_level = ''S''
               AND source IN (SELECT source FROM current_sources)
               AND atom_id IN
    	      (SELECT atom_id FROM new_atom_ids)';
    	EXECUTE IMMEDIATE
    	   'INSERT INTO attributes_$$
            (SELECT /*+ PARALLEL(a) */ * FROM attributes a
             WHERE attribute_level = ''C''
               AND attribute_name != ''SEMANTIC_TYPE''
               AND concept_id IN
    	      (SELECT concept_id FROM new_concept_ids)
			 UNION ALL
             SELECT * FROM attributes a
             WHERE attribute_level = ''C''
               AND attribute_name = ''SEMANTIC_TYPE''
               AND concept_id IN
    	      (SELECT concept_id FROM new_concept_ids)
    	       AND attribute_id IN
    	       	(SELECT min(attribute_id) FROM attributes b
    	       	 WHERE attribute_name=''SEMANTIC_TYPE''
    	       	 GROUP BY concept_id,atui)
    	     ) ORDER BY concept_id';
	    MEME_UTILITY.drop_it('table','attributes');
    	EXECUTE IMMEDIATE
    		'RENAME attributes_$$ TO attributes';
        EXECUTE IMMEDIATE
        	'ALTER TABLE ATTRIBUTES ADD CONSTRAINT
        	 ATTRIBUTES_PK PRIMARY KEY (attribute_id)';
        EXECUTE IMMEDIATE 'GRANT ALL ON attributes TO MID_USER';

    END;
    }) ||
    ((print L "<span id=red>Error fixing attributes ($DBI::errstr).</span>") &&  return);

  # Reopen connection
  $dbh->disconnect;
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer(1000000);
  $dbh->do(qq{ALTER SESSION set sort_area_size=200000000
    }) || ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);
  $dbh->do(qq{ALTER SESSION set hash_area_size=200000000
    }) || ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # Remove stringtab rows connected to old atoms and concepts
  #
  print L "      stringtab ... ".scalar(localtime)."\n";
  $dbh->do(qq{
  	BEGIN
        MEME_UTILITY.drop_it('table','stringtab_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE stringtab_$$ AS
             SELECT * FROM stringtab a
             WHERE string_id in (
			    SELECT /*+ parallel(a) */
			    	to_number(substr(attribute_value,20)) FROM attributes a
	    		WHERE attribute_value LIKE ''<>Long_Attribute<>:%''
	    	 UNION ALL SELECT -1 FROM stringtab where string_id=-1)
	    	 ORDER BY string_id,row_sequence';
	    MEME_UTILITY.drop_it('table','stringtab');
    	EXECUTE IMMEDIATE
    		'RENAME stringtab_$$ TO stringtab';
        EXECUTE IMMEDIATE
        	'ALTER TABLE STRINGTAB ADD CONSTRAINT
        	 STRINGTAB_PK PRIMARY KEY (string_id,row_sequence)';
    	EXECUTE IMMEDIATE 'GRANT ALL ON stringtab TO MID_USER';
    END;
    }) ||
    ((print L "<span id=red>Error fixing stringtab ($DBI::errstr).</span>") &&  return);

  #
  # Remove stringtab rows connected to old atoms and concepts
  #
  print L "      concept_status ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN
        MEME_UTILITY.drop_it('table','concept_status_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE concept_status_$$ AS
             SELECT * FROM concept_status a
             WHERE concept_id in (SELECT concept_id FROM new_concept_ids)';
	    MEME_UTILITY.drop_it('table','concept_status');
    	EXECUTE IMMEDIATE
    		'RENAME concept_status_$$ TO concept_status';
        EXECUTE IMMEDIATE
        	'ALTER TABLE CONCEPT_STATUS ADD CONSTRAINT
        	 CONCEPT_STATUS_PK PRIMARY KEY (concept_id)';
    	EXECUTE IMMEDIATE 'GRANT ALL ON concept_status TO MID_USER';
    END;
    }) ||
    ((print L "<span id=red>Error fixing concept_status ($DBI::errstr).</span>") &&  return);

  # Reopen connection
  $dbh->disconnect;
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print L "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);
  &EnableBuffer(1000000);
  $dbh->do(qq{ALTER SESSION set sort_area_size=200000000
    }) || ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);
  $dbh->do(qq{ALTER SESSION set hash_area_size=200000000
    }) || ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # Remove source_id_map rows connected to old atoms
  #
  print L "      source_id_map ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN
        MEME_UTILITY.drop_it('table','source_id_map_$$');
        EXECUTE IMMEDIATE
        	'CREATE TABLE source_id_map_$$ AS
             SELECT * FROM source_id_map a
             WHERE table_name=''C''
               AND local_row_id in (SELECT atom_id FROM new_atom_ids)';
	    MEME_UTILITY.drop_it('table','source_id_map');
    	EXECUTE IMMEDIATE
    		'RENAME source_id_map_$$ TO source_id_map';
    	EXECUTE IMMEDIATE 'GRANT ALL ON source_id_map TO MID_USER';
    END;
    }) ||
    ((print L "<span id=red>Error fixing source_id_map ($DBI::errstr).</span>") &&  return);

  #
  # Truncate tables which should start out empty
  #
  print L "    Truncate tables which should start out empty ... ".scalar(localtime)."\n";
  print L "      activity_log, atomic_actions, dead_*, ic_violations,\n";
  print L "      lui_assignment, meme_error, meme_progress, --meme_work,\n";
  print L "      mid_qa_history, molecular_actions, mom_*, operations_queue,\n";
  print L "      qa_diff_results, snapshot_results, source_*, sr_predicate\n";
  $dbh->do(qq{
    BEGIN
        MEME_SYSTEM.truncate('activity_log');
        MEME_SYSTEM.truncate('atomic_actions');
        MEME_SYSTEM.truncate('dead_atomic_actions');
        MEME_SYSTEM.truncate('dead_atoms');
        MEME_SYSTEM.truncate('dead_attributes');
        MEME_SYSTEM.truncate('dead_classes');
        MEME_SYSTEM.truncate('dead_concept_status');
        MEME_SYSTEM.truncate('dead_context_relationships');
        MEME_SYSTEM.truncate('dead_normstr');
        MEME_SYSTEM.truncate('dead_normwrd');
        MEME_SYSTEM.truncate('dead_relationships');
        MEME_SYSTEM.truncate('dead_stringtab');
        MEME_SYSTEM.truncate('dead_word_index');
        MEME_SYSTEM.truncate('lui_assignment');
        MEME_SYSTEM.truncate('meme_error');
        MEME_SYSTEM.truncate('meme_progress');
        --MEME_SYSTEM.truncate('meme_work');
        MEME_SYSTEM.truncate('mid_qa_history');
        MEME_SYSTEM.truncate('molecular_actions');
        MEME_SYSTEM.truncate('mom_candidate_facts');
        MEME_SYSTEM.truncate('mom_facts_processed');
        MEME_SYSTEM.truncate('mom_merge_facts');
        MEME_SYSTEM.truncate('mom_precomputed_facts');
        MEME_SYSTEM.truncate('mom_safe_replacement');
        MEME_SYSTEM.truncate('qa_diff_results');
        MEME_SYSTEM.truncate('snapshot_results');
        MEME_SYSTEM.truncate('source_attributes');
        MEME_SYSTEM.truncate('source_classes_atoms');
        MEME_SYSTEM.truncate('source_concept_status');
        MEME_SYSTEM.truncate('source_context_relationships');
        MEME_SYSTEM.truncate('source_relationships');
        MEME_SYSTEM.truncate('source_source_rank');
        MEME_SYSTEM.truncate('source_stringtab');
        MEME_SYSTEM.truncate('source_string_ui');
        MEME_SYSTEM.truncate('source_termgroup_rank');
        MEME_SYSTEM.truncate('sr_predicate');
    END;}) ||
    ((print L "<span id=red>Error truncating tables ($DBI::errstr).</span>")
     &&  return);
	&FlushBuffer;
  #
  # Delete where source in
  #
  print L "    Remove references in tables to old sources ... ".scalar(localtime)."\n";

  print L "      ic_pair ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        DELETE FROM ic_pair WHERE type_1='SOURCE'
        AND value_1 NOT IN (SELECT * FROM current_sources)
    }) ||
    ((print L "<span id=red>Error executing delete 2 ($DBI::errstr).</span>") &&  return);

  $dbh->do(qq{
        DELETE FROM ic_pair WHERE type_2='SOURCE'
        AND value_2 NOT IN (SELECT * FROM current_sources)
    }) ||
    ((print L "<span id=red>Error executing delete 3 ($DBI::errstr).</span>") &&  return);

  print L "      ic_single ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        DELETE FROM ic_single WHERE type='SOURCE'
        AND value NOT IN (SELECT * FROM current_sources)
    }) ||
    ((print L "<span id=red>Error executing delete 4 ($DBI::errstr).</span>") &&  return);

  print L "      mom_exclude_list ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        DELETE FROM mom_exclude_list
        WHERE substr(termgroup,0,instr(termgroup,'/')-1) NOT IN
        (SELECT source FROM current_sources)
    }) ||
    ((print L "<span id=red>Error executing delete 5 ($DBI::errstr).</span>") &&  return);

  print L "      mom_norm_exclude_list ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        DELETE FROM mom_norm_exclude_list
        WHERE substr(termgroup,0,instr(termgroup,'/')-1) NOT IN
        (SELECT source FROM current_sources)
    }) ||
    ((print L "<span id=red>Error executing delete 6 ($DBI::errstr).</span>") &&  return);

  print L "      termgroup_rank ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        DELETE FROM termgroup_rank
        WHERE substr(termgroup,0,instr(termgroup,'/')-1) NOT IN
        (SELECT source FROM current_sources)
    }) ||
    ((print L "<span id=red>Error executing delete 7 ($DBI::errstr).</span>") &&  return);

  #
  # Validate the max tab table
  #
  print L "    Validate max_tab, verify max_id ... ".scalar(localtime)."\n";
  print L "      ISUI ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        ct1     INTEGER;
        ct2     INTEGER;
    BEGIN
        MEME_UTILITY.put_message('Checking ISUI.');
        SELECT max_id into ct1 FROM max_tab WHERE table_name='ISUI';
        SELECT max(to_number(substr(isui,2))) into ct2 FROM string_ui;

        IF ct1 < ct2 THEN
            MEME_UTILITY.put_message('Error with ISUI: max_tab= '||ct1||',string_ui='||ct2||'.');
        ELSIF ct1 > ct2 THEN
            MEME_UTILITY.put_message('ISUI is higher in max_tab:  max_tab='||ct1||', string_ui='||ct2||'.');
        END IF;
    END;
    }) ||
    ((print L "<span id=red>Error executing query 27 ($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  print L "      ATOMS ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        ct1     INTEGER;
        ct2     INTEGER;
    BEGIN
        MEME_UTILITY.put_message('Checking atom_id.');
        SELECT max_id into ct1 FROM max_tab WHERE table_name='ATOMS';
        SELECT max(atom_id) into ct2 FROM classes;

        IF ct1 < ct2 THEN
            MEME_UTILITY.put_message('Error with atom_id: max_tab= '||ct1||',classes='||ct2||'.');
        ELSIF ct1 > ct2 THEN
            MEME_UTILITY.put_message('atom_id is higher in max_tab:  max_tab='||ct1||', classes='||ct2||'.');
        END IF;
    END;
    }) ||
    ((print L "<span id=red>Error executing query 28 ($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  print L "      ATTRIBUTES ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        ct1     INTEGER;
        ct2     INTEGER;
    BEGIN
        MEME_UTILITY.put_message('Checking attribute_id.');
        SELECT max_id into ct1 FROM max_tab WHERE table_name='ATTRIBUTES';
        SELECT max(attribute_id) into ct2 FROM attributes;

        IF ct1 < ct2 THEN
            MEME_UTILITY.put_message('Error with attribute_id: max_tab= '||ct1||',attributes='||ct2||'.');
        ELSIF ct1 > ct2 THEN
            MEME_UTILITY.put_message('attribute_id is higher in max_tab:  max_tab='||ct1||', attributes='||ct2||'.');
        END IF;
    END;
    }) ||
    ((print L "<span id=red>Error executing query 29 ($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  print L "      CONCEPT_STATUS ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        ct1     INTEGER;
        ct2     INTEGER;
    BEGIN
        MEME_UTILITY.put_message('Checking concept_id.');
        SELECT max_id into ct1 FROM max_tab WHERE table_name='CONCEPT_STATUS';
        SELECT max(concept_id) into ct2 FROM concept_status;

        IF ct1 < ct2 THEN
            MEME_UTILITY.put_message('Error with concept_id: max_tab= '||ct1||',concept_status='||ct2||'.');
        ELSIF ct1 > ct2 THEN
            MEME_UTILITY.put_message('concept_id is higher in max_tab:  max_tab='||ct1||', concept_status='||ct2||'.');
        END IF;
    END;
    }) ||
    ((print L "<span id=red>Error executing query 30 ($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  print L "      CUI ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        ct1     INTEGER;
        ct2     INTEGER;
    BEGIN
        MEME_UTILITY.put_message('Checking CUI.');
        SELECT max_id into ct1 FROM max_tab WHERE table_name='CUI';
        SELECT max(to_number(replace(translate(cui,'CL','XX'),'X',''))) into ct2 FROM concept_status;

        IF ct1 < ct2 THEN
            MEME_UTILITY.put_message('Error with CUI: max_tab= '||ct1||',concept_status='||ct2||'.');
        ELSIF ct1 > ct2 THEN
            MEME_UTILITY.put_message('CUI is higher in max_tab:  max_tab='||ct1||', concept_status='||ct2||'.');
        END IF;
    END;
    }) ||
    ((print L "<span id=red>Error executing query 31 ($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  print L "      LUI ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        ct1     INTEGER;
        ct2     INTEGER;
    BEGIN
        MEME_UTILITY.put_message('Checking LUI.');
        SELECT max_id into ct1 FROM max_tab WHERE table_name='LUI';
        SELECT max(to_number(substr(lui,2))) into ct2 FROM string_ui;

        IF ct1 < ct2 THEN
            MEME_UTILITY.put_message('Error with LUI: max_tab= '||ct1||',string_ui='||ct2||'.');
        ELSIF ct1 > ct2 THEN
            MEME_UTILITY.put_message('LUI is higher in max_tab:  max_tab='||ct1||', string_ui='||ct2||'.');
        END IF;
    END;
    }) ||
    ((print L "<span id=red>Error executing query 32 ($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  print L "      RELATIONSHIPS ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        ct1     INTEGER;
        ct2     INTEGER;
    BEGIN
        MEME_UTILITY.put_message('Checking relationship_id.');
        SELECT max_id into ct1 FROM max_tab WHERE table_name='RELATIONSHIPS';
        SELECT max(relationship_id) into ct2 FROM relationships;

        IF ct1 < ct2 THEN
            MEME_UTILITY.put_message('Error with relationship_id: max_tab= '||ct1||',relationship_id='||ct2||'.');
        ELSIF ct1 > ct2 THEN
            MEME_UTILITY.put_message('relationship_id is higher in max_tab:  max_tab='||ct1||', relationship_id='||ct2||'.');
        END IF;
    END;
    }) ||
    ((print L "<span id=red>Error executing query 33 ($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  print L "      SUI ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DECLARE
        ct1     INTEGER;
        ct2     INTEGER;

    BEGIN
        MEME_UTILITY.put_message('Checking SUI.');
        SELECT max_id into ct1 FROM max_tab WHERE table_name='SUI';
        SELECT max(to_number(substr(sui,2))) into ct2 FROM string_ui;

        IF ct1 < ct2 THEN
            MEME_UTILITY.put_message('Error with SUI: max_tab= '||ct1||',string_ui='||ct2||'.');
        ELSIF ct1 > ct2 THEN
            MEME_UTILITY.put_message('SUI is higher in max_tab:  max_tab='||ct1||', string_ui='||ct2||'.');
        END IF;
    END;
    }) ||
    ((print L "<span id=red>Error executing query 34 ($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  #
  # Clean up src_qa_results.
  #     remove references to old sources
  #
  print L "    Remove old sources from qa tables ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        CREATE TABLE tqa_src AS
        SELECT value FROM src_qa_results, current_sources
        WHERE value LIKE '%' || source || ',%'
    }) || ((print L "<span id=red>Error executing create 5 ($DBI::errstr).</span>")
     &&  return);

  print L "      src_qa_results ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        DELETE FROM src_qa_results WHERE value NOT IN (SELECT value FROM tqa_src)
    }) ||
    ((print L "<span id=red>Error executing delete 8 ($DBI::errstr).</span>") &&  return);

  # remove rows from qa_adjustment where the
  # qa_id is not found in src_qa_results
  print L "      qa_adjustment ... ".scalar(localtime)."\n";
  $dbh->do(qq{
        DELETE FROM qa_adjustment WHERE value NOT IN
        (SELECT value FROM src_qa_results)
    }) ||
    ((print L "<span id=red>Error cleaning qa_adjustment ($DBI::errstr).</span>") &&  return);

  #  remove rows from qa_diff_adjustment where qa_id_1 or qa_id_2
  #  is not found in src_qa_results, mid_qa_results, mid_qa_history
  print L "      qa_diff_adjustment ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    DELETE FROM qa_diff_adjustment WHERE qa_id_1 IN
    (SELECT qa_id_1 FROM qa_diff_adjustment
     MINUS
     (SELECT qa_id FROM src_qa_results
      UNION
      SELECT qa_id FROM src_obsolete_qa_results
      UNION
      SELECT qa_id FROM mid_qa_results
      UNION
      SELECT qa_id FROM mid_qa_history))
    }) ||
    ((print L "<span id=red>Error executing delete 10 ($DBI::errstr).</span>") &&  return);

  $dbh->do(qq{
    DELETE FROM qa_diff_adjustment WHERE qa_id_2 IN
    (SELECT qa_id_2 FROM qa_diff_adjustment
     MINUS
     (SELECT qa_id FROM src_qa_results
      UNION
      SELECT qa_id FROM src_obsolete_qa_results
      UNION
      SELECT qa_id FROM mid_qa_results
      UNION
      SELECT qa_id FROM mid_qa_history))
    }) ||
    ((print L "<span id=red>Error executing delete 11 ($DBI::errstr).</span>") &&  return);

  #
  # Set ranks and preferred atom ids
  #
  print L "    Set ranks and preferred ids ... ".scalar(localtime)."\n";
  print L "      classes ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN
      MEME_RANKS.set_ranks(classes_flag => 'Y',  attributes_flag => 'N', relationships_flag => 'N' );
      commit;
    END;
    }) ||
    ((print L "<span id=red>Error setting classes ranks($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  print L "      relationships ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN
      MEME_RANKS.set_ranks(classes_flag => 'N', attributes_flag => 'N',  relationships_flag => 'Y' );
      COMMIT;
    END;
    }) ||
    ((print L "<span id=red>Error setting relationsihps ranks ($DBI::errstr).</span>") &&  return);
  print L &FlushBuffer;

  print L "      concept status ... ".scalar(localtime)."\n";
  $dbh->do(qq{ BEGIN MEME_RANKS.set_preference; END; }) ||
    ((print L "<span id=red>Error computing preferred atom ids ($DBI::errstr).</span>") &&  return);

  print L "    Reindexing MID ... ".scalar(localtime)."\n";
  open (CMD,"$ENV{MEME_HOME}/bin/reindex_mid.pl -p 5 $database |");
  while (<CMD>) {
    chop;
    print L "      $_\r\n";
  }
  ((print L "<span id=red>Error executing reindex_mid ($! $?).</span>") &&  return) if ($?);

  print L "    RECOMMENDED:  \$MEME_HOME/bin/add_words.csh -all $db\n";
  print L "    RECOMMENDED:  \$MEME_HOME/bin/matrixinit.pl -I $db\n";
  #
  # Cleanup
  #
  print L "    Cleanup ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    BEGIN
      MEME_UTILITY.drop_it('table','current_sources');
      MEME_UTILITY.drop_it('table','old_atom_ids');
      MEME_UTILITY.drop_it('table','new_atom_ids');
      MEME_UTILITY.drop_it('table','old_concept_ids');
      MEME_UTILITY.drop_it('table','new_concept_ids');
      MEME_UTILITY.drop_it('table','tqa_src');
    END;
    }) ||
    ((print L "<span id=red>Error executing drop 5 ($DBI::errstr).</span>") &&  return);


  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;
}

##############################################################################
# Assign RUIs for AQ/QB relationships
#
sub Handle_atui_rui {
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
    ALTER SESSION set sort_area_size=400000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=400000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  #
  # Load source_relationships
  #
  print L "    Create AQ relationships ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_SYSTEM.truncate('source_relationships'); END;") ||
    ((print L "<span id=red>Error at 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 1 ($DBI::errstr).</span>")
     &&  return);

  #
  # Insert rows
  #
  $dbh->do(qq{
    INSERT INTO source_relationships
          (switch, source_rel_id, relationship_id, atom_id_1, atom_id_2,
	   concept_id_1, concept_id_2, sg_id_1, sg_type_1, sg_qualifier_1,
	   sg_id_2, sg_type_2, sg_qualifier_2, source, source_of_label,
	   relationship_level, relationship_name, relationship_attribute,
	   generated_status, status, released, tobereleased, source_rank,
	   suppressible)
    SELECT DISTINCT 'R', 0, 0, a.atom_id, b.atom_id,
	   a.concept_id, b.concept_id, to_char(a.atom_id),'ATOM_ID','',
	   to_char(b.atom_id),'ATOM_ID','', current_name, current_name,
	      'S', 'AQ', '', 'Y','R', 'N','Y',0,'N'
    FROM attributes b, attributes a, source_version c
    WHERE b.attribute_name = 'QA'
      AND a.attribute_name IN ('ATN','AQL')
      AND INSTR(a.attribute_value,b.attribute_value) > 0
      AND a.source = current_name
      AND b.source = current_name
      AND a.tobereleased in ('Y','y')
      AND b.tobereleased in ('Y','y')
      AND c.source = 'MSH'
    UNION
    SELECT DISTINCT 'R', 0, 0, a.atom_id, b.atom_id,
	   a.concept_id, b.concept_id, to_char(a.atom_id),'ATOM_ID','',
	   to_char(b.atom_id),'ATOM_ID','', current_name, current_name,
	      'S', 'AQ', '', 'Y','R', 'N','Y',0,'N'
    FROM attributes b, attributes a, source_version c, stringtab d
    WHERE b.attribute_name = 'QA'
      AND a.attribute_name IN ('ATN','AQL')
      AND a.attribute_value like '<>Long%'
      AND string_id = to_number(substr(a.attribute_value,20))
      AND INSTR(d.text_value,b.attribute_value) > 0
      AND a.source = current_name
      AND b.source = current_name
      AND a.tobereleased in ('Y','y')
      AND b.tobereleased in ('Y','y')
      AND c.source = 'MSH'
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Set fake rel id
  #
  $dbh->do(qq{
    UPDATE source_relationships
    SET relationship_id = rownum
    }) || ((print L "<span id=red>Error at 2.2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Assign the RUIs
  #
  print L "    Assign RUIs ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    BEGIN
        MEME_SOURCE_PROCESSING.assign_ruis(
		   table_name => 'SR',
		   authority => 'MTH',
		   work_id => 0);
    END;
  }) ||
    ((print L "<span id=red>Error at 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 3 ($DBI::errstr).</span>")
     &&  return);

  #
  # Load source_attributes
  #
  # Note that the ATUI semantic for these attributes are slightly different
  # We do not actually care what the attribute value is, because what makes
  # the attribute the same is that it is attached to the same CUI
  #
  print L "    Create DA attribute placeholders ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_SYSTEM.truncate('source_attributes'); END;") ||
    ((print L "<span id=red>Error at 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 1 ($DBI::errstr).</span>")
     &&  return);

  #
  # Insert DA
  #
  $dbh->do(qq{
    INSERT /*+ append */ INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
	   sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
	   attribute_value, generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT /*+ PARALLEL(cs) */ DISTINCT 'R', 0, 0, 0, concept_id,
	   cs.cui, 'CUI', '', 'C', 'DA', '', 'Y','MTH',
	   'R', 'N','Y',0,'N','','',''
    FROM concept_status cs
    WHERE tobereleased in ('Y','y')
      AND cui IN
	      (SELECT cui FROM concept_status WHERE tobereleased in ('Y','y') 
	       MINUS
	       SELECT sg_id FROM attributes_ui WHERE sg_type='CUI'
	       AND attribute_name='DA' and root_source='MTH')
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Insert ST
  #
  print L "    Create ST attribute placeholders ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    INSERT /*+ append */ INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
	   sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
	   attribute_value, generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R', 0, 0, 0, concept_id,
	   cui, 'CUI', '', 'C', 'ST', cs.status, 'Y','MTH',
	   'R', 'N','Y',0,'N','','',cs.status
    FROM concept_status cs
    WHERE tobereleased in ('Y','y')
      AND cui IN
	      (SELECT sg_id FROM source_attributes)
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Insert MR
  #
  print L "    Create MR attribute placeholders ... ".scalar(localtime)."\n";
  # get second highest cui from meme_properties for '00000000' values
  $sh = $dbh->prepare(qq{
    SELECT max(key) FROM meme_properties where key_qualifier='MRSAT' 
    and key != (select max(key) from meme_properties where key_qualifier='MRSAT')
  }) ||  ((print L "<span id=red>Error preparing to get prev ver max cui($DBI::errstr).</span>")
     &&  return);
  $sh->execute()
  || ((print L "<span id=red>Error executing toget prev ver max cui ($DBI::errstr).</span>")
     &&  return);
  ($prev_cui) = $sh->fetchrow_array;
  print L "      Previous version max CUI: $prev_cui\n";
    
  $dbh->do(qq{
    INSERT /*+ append */ INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
	   sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
	   attribute_value, generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R', 0, 0, 0, concept_id,
	   cui, 'CUI', '', 'C', 'MR', to_char(cs.timestamp,'YYYYmmDD'), 'Y','MTH',
	   'R', 'N','Y',0,'N','','', to_char(cs.timestamp,'YYYYmmDD')
    FROM concept_status cs
    WHERE tobereleased in ('Y','y')
      AND cui IN
	      (SELECT sg_id FROM source_attributes)
	  AND cui <= ?
    }, undef, $prev_cui) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  $rc = $dbh->do(qq{
    INSERT /*+ append */ INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
	   sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
	   attribute_value, generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R', 0, 0, 0, concept_id,
	   cui, 'CUI', '', 'C', 'MR', '00000000', 'Y','MTH',
	   'R', 'N','Y',0,'N','','', '00000000'
    FROM concept_status cs
    WHERE tobereleased in ('Y','y')
      AND cui IN
	      (SELECT sg_id FROM source_attributes)
	  AND cui > ?
    }, undef, $prev_cui) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);
  print L "      Count == $rc (MR=00000000 entries)\n";
  
  #
  # Insert AM -- NO LONGER BUILT for 2004AC and beyond
  #
#  $dbh->do(qq{
#    INSERT /*+ append */ INTO source_attributes
#          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
#	   sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
#	   attribute_value, generated_status, source, status, released,
#	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
#    SELECT DISTINCT 'R', 0, 0, atom_id, concept_id,
#	   aui, 'AUI', '', 'S', 'AM', '', 'Y','MTH',
#	   'R', 'N','Y',0,'N','','',''
#    FROM classes
#    WHERE isui IN (SELECT /*+ PARALLEL(c) */ isui FROM classes c
#		   WHERE tobereleased in ('Y','y')
#		     AND nvl(language,'ENG') = 'ENG'
#		   GROUP BY isui HAVING count(distinct concept_id)>1)
#      AND tobereleased in ('Y','y')
#      AND aui IN (SELECT aui FROM classes MINUS
#		  SELECT sg_id FROM attributes_ui
#		  WHERE root_source='MTH' and attribute_name='AM'
#		    AND sg_type='AUI')
#    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
#     &&  return);

  #
  # Insert MTH owned LT rows.
  #
  print L "    Create LT attribute placeholders ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    INSERT INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
	   sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
	   attribute_value, generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT /*+ parallel(c) */ DISTINCT 'R', 0, 0, atom_id, concept_id,
	   aui, 'AUI', '', 'S', 'LT', 'TRD', 'Y','MTH',
	   'R', 'N','Y',0,'N','','',''
    FROM classes c
    WHERE (concept_id,sui) IN
     (SELECT a.concept_id,sui
      FROM attributes a, classes b
      WHERE a.atom_id = b.atom_id
        AND attribute_name = 'LEXICAL_TAG'
        AND attribute_value = 'TRD'
        AND b.source != (SELECT current_name FROM source_version WHERE source='MSH')
        AND b.tobereleased in ('Y','y'))
      AND tobereleased in ('Y','y')
      AND aui IN (SELECT aui FROM classes MINUS
		  SELECT sg_id FROM attributes_ui
		  WHERE root_source='MTH' and attribute_name='LT'
		    AND sg_type='AUI')
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Insert MED<year> attributes
  # Any AUIs already in attributes_ui will have entries for every year
  # except possibly the most recent year
  #
  print L "    Create MED attribute placeholders ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    INSERT INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
	   sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
	   attribute_value, generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R', 0, 0, atom_id, concept_id,
	   aui, 'AUI', '', 'S', 'MED'||year, '', 'Y','NLM-MED',
	   'R', 'N','Y',0,'N','','',''
    FROM
    (SELECT a.atom_id, a.concept_id, a.aui FROM classes a
    WHERE tobereleased in ('Y','y')
      AND source = (select current_name FROM source_version WHERE source='MSH')
      AND tty IN ('MH','TQ')
      AND aui IN
	 (SELECT a.aui FROM classes a, source_version b
          WHERE a.tobereleased in ('Y','y')
          AND a.source = b.current_name and b.source='MSH' AND a.tty IN ('MH','TQ')
	  MINUS SELECT /*+ parallel(a) */ sg_id FROM attributes_ui a
	  WHERE root_source = 'NLM-MED' and attribute_name like 'MED____') ) a,
      (SELECT rownum+1900 as year FROM classes
       WHERE rownum< (to_number(to_char(sysdate,'YYYY'))-1899)) b
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Insert entries for current year
  #
  $dbh->do(qq{
    INSERT INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
	   sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
	   attribute_value, generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui,
	   hashcode)
    SELECT DISTINCT 'R', 0, 0, atom_id, concept_id,
	   aui, 'AUI', '', 'S', 'MED'||(to_number(to_char(sysdate,'YYYY'))),
	   '', 'Y','NLM-MED',
	   'R', 'N','Y',0,'N','','',''
    FROM classes a
    WHERE tobereleased in ('Y','y')
      AND source = (select current_name FROM source_version WHERE source='MSH')
      AND tty IN ('MH','TQ')
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);


  #
  # Insert MED<year>* attributes
  # We can safely insert one of these for each MED attribute
  # already inserted into source_attributes
  #
  print L "    Create MED* attribute placeholders ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    INSERT INTO source_attributes
          (switch, source_attribute_id, attribute_id, atom_id, concept_id,
	   sg_id, sg_type, sg_qualifier, attribute_level, attribute_name,
	   attribute_value, generated_status, source, status, released,
	   tobereleased, source_rank, suppressible, atui, source_atui, hashcode)
    SELECT DISTINCT 'R', 0, 0, atom_id, concept_id,
	   sg_id, 'AUI', '', 'S', attribute_name||'*', '', 'Y','NLM-MED',
	   'R', 'N','Y',0,'N','','',''
    FROM source_attributes
    WHERE attribute_name like 'MED____'
      AND source = 'NLM-MED'
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Set fake attribute id
  #
  $dbh->do(qq{
    UPDATE source_attributes
    SET attribute_id = rownum
    }) || ((print L "<span id=red>Error at 2.2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Assign the ATUIs
  #
  print L "    Assign ATUIs ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
    BEGIN
        MEME_SOURCE_PROCESSING.assign_atuis(
		   table_name => 'SA',
		   authority => 'MTH',
		   work_id => 0);
    END;
  }) ||
    ((print L "<span id=red>Error at 3 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 3 ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;
} # Handle_atui_rui


##############################################################################
#
# Map ATXs through cui mappings (and last_release_cui)
#
sub Handle_atx_cui_map {
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
    ALTER SESSION set sort_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=200000000
    }) ||
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);


  #
  # Find CUIs in ATXs that are being deleted and merged
  #
  print L "    Find ATX merged/deleted CUIs ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table','t_delcui_$$'); END;") ||
    ((print L "<span id=red>Error at 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
    CREATE TABLE t_delcui_$$ as
    SELECT substr(attribute_value,1,8) as cui
    FROM attributes a
    WHERE attribute_name = 'XMAPFROM' and attribute_value like 'C%'
      AND tobereleased in ('Y','y')
    MINUS SELECT cui FROM concept_status WHERE tobereleased in ('Y','y')
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table','t_mergecui_$$'); END;") ||
    ((print L "<span id=red>Error at 1.1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 1.1 ($DBI::errstr).</span>")
     &&  return);

  $sh = $dbh->prepare("SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_prefix'") ||
    ((print L "<span id=red>Error at 1.1b ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 1.1b ($DBI::errstr).</span>")
     &&  return);
  ($aui_prefix) = $sh->fetchrow_array;

  $sh = $dbh->prepare("SELECT value FROM code_map WHERE code = 'AUI' AND type = 'ui_length'") ||
    ((print L "<span id=red>Error at 1.1b ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 1.1b ($DBI::errstr).</span>")
     &&  return);
  ($aui_length) = $sh->fetchrow_array;
  
  #
  # identify merges
  #
  $dbh->do(qq{
    CREATE TABLE t_mergecui_$$ as
    SELECT a.cui as old_cui, b.cui as new_cui
    FROM t_delcui_$$ a, concept_status b,
      (SELECT last_release_cui as cui,
          to_number(substr(max(rank||sui||
          LPAD(SUBSTR(aui,INSTR(aui,'$aui_prefix')+1),
          $aui_length,'0')
          ||'/'||concept_id),24)) concept_id
       FROM classes c, t_delcui_$$ d
       WHERE c.last_release_cui = d.cui
       GROUP BY last_release_cui) c
    WHERE b.concept_id = c.concept_id
      AND a.cui = c.cui
      AND b.tobereleased in ('Y','y')
      AND b.cui IS NOT NULL
    }) || ((print L "<span id=red>Error at 2.1 ($DBI::errstr).</span>")
     &&  return);

  #
  # Remove merges from delete candidates
  #
  $dbh->do(qq{
    DELETE FROM t_delcui_$$ WHERE cui IN (SELECT old_cui FROM t_mergecui_$$)
    }) || ((print L "<span id=red>Error at 2.2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Get deleted cui count
  #
  $sh = $dbh->prepare(qq{
     SELECT COUNT(*) FROM t_delcui_$$
  }) ||
    ((print L "<span id=red>Error preparing count 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 1 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Deleted CUI Count == $row_ct\n";

  #
  # Get merged cui count
  #
  $sh = $dbh->prepare(qq{
     SELECT COUNT(*) FROM t_mergecui_$$
  }) ||
    ((print L "<span id=red>Error preparing count 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error executing count 1 ($DBI::errstr).</span>")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print L "      Merged CUI Count == $row_ct\n";

  #
  # CUI History Mappings
  #

  #
  # Find XMAPFROM attributes to update
  #
  print L "    Find XMAPFROM attributes to fix ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table','txmapfrom_$$'); END;") ||
    ((print L "<span id=red>Error at 1 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 1 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
    CREATE TABLE txmapfrom_$$ AS
    SELECT a.*, cui2
    FROM attributes a, cui_history b
    WHERE attribute_name='XMAPFROM'
      AND concept_id in (SELECT concept_id FROM attributes
			 WHERE attribute_name = 'MAPSETTYPE'
			   AND attribute_value='ATX'
			   AND tobereleased in ('Y','y'))
      AND substr(attribute_value,1,instr(attribute_value,'~')-1) = cui1
      AND relationship_name = 'SY'
      AND a.tobereleased in ('Y','y')
    UNION
    SELECT a.*, b.new_cui
    FROM attributes a, t_mergecui_$$ b
    WHERE attribute_name='XMAPFROM'
      AND concept_id in (SELECT concept_id FROM attributes
			 WHERE attribute_name = 'MAPSETTYPE'
			   AND attribute_value='ATX'
			   AND tobereleased in ('Y','y'))
      AND substr(attribute_value,1,instr(attribute_value,'~')-1) = old_cui
      AND a.tobereleased in ('Y','y')
    }) || ((print L "<span id=red>Error at 2 ($DBI::errstr).</span>")
     &&  return);

  #
  # Set new attribute value
  #
  print L "    Set new XMAPFROM attribute value ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    UPDATE txmapfrom_$$
    SET attribute_value = cui2 || '~~' || cui2 || '~CUI~~'
    }) || ((print L "<span id=red>Error at 3 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
    UPDATE txmapfrom_$$
    SET hashcode = meme_utility.md5(attribute_value)
    }) || ((print L "<span id=red>Error at 4 ($DBI::errstr).</span>")
     &&  return);

  #
  # Find XMAP attributes to update
  #
  print L "    Find XMAP attributes to fix ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table','txmap_$$'); END;") ||
    ((print L "<span id=red>Error at 5 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 5 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
    CREATE TABLE txmap_$$ AS
    SELECT a.*, cui2
    FROM attributes a, cui_history b
    WHERE attribute_name='XMAP'
      AND concept_id in (SELECT concept_id FROM attributes
			 WHERE attribute_name = 'MAPSETTYPE'
			   AND attribute_value='ATX'
			   AND tobereleased in ('Y','y'))
      AND substr(attribute_value,3,instr(attribute_value,'~',3)-3) = cui1
      AND relationship_name = 'SY'
      AND a.tobereleased in ('Y','y')
    UNION
    SELECT a.*, new_cui
    FROM attributes a, t_mergecui_$$ b
    WHERE attribute_name='XMAP'
      AND concept_id in (SELECT concept_id FROM attributes
			 WHERE attribute_name = 'MAPSETTYPE'
			   AND attribute_value='ATX'
			   AND tobereleased in ('Y','y'))
      AND substr(attribute_value,3,instr(attribute_value,'~',3)-3) = old_cui
      AND a.tobereleased in ('Y','y')
    }) || ((print L "<span id=red>Error at 6 ($DBI::errstr).</span>")
     &&  return);

  #
  # Set new attribute value
  #
  print L "    Set new XMAP attribute value ... ".scalar(localtime)."\n";
  $dbh->do(qq{
    UPDATE txmap_$$
    SET attribute_value = '~~' || cui2 || substr(attribute_value,instr(attribute_value,'~',1,3))
    }) || ((print L "<span id=red>Error at 7 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
    UPDATE txmap_$$
    SET hashcode = meme_utility.md5(attribute_value)
    }) || ((print L "<span id=red>Error at 8 ($DBI::errstr).</span>")
     &&  return);


  #
  # Find XMAP attributes to update
  #
  print L "    Find XMAP attributes to fix ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table','tdel_$$'); END;") ||
    ((print L "<span id=red>Error at 9 ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at 10 ($DBI::errstr).</span>")
     &&  return);

  $dbh->do(qq{
    CREATE TABLE tdel_$$ AS
    SELECT attribute_id AS row_id FROM txmapfrom_$$
    UNION
    SELECT attribute_id FROM txmap_$$
    UNION
    SELECT attribute_id
    FROM attributes a, cui_history b
    WHERE attribute_name='XMAP'
      AND concept_id in (SELECT concept_id FROM attributes
			 WHERE attribute_name='MAPSETTYPE'
			   AND attribute_value='ATX'
			   AND tobereleased in ('Y','y'))
      AND substr(attribute_value,3,instr(attribute_value,'~',3)-3) = cui1
      AND relationship_name = 'DEL'
    UNION
    SELECT attribute_id
    FROM attributes a, t_delcui_$$ b
    WHERE attribute_name='XMAP'
      AND concept_id in (SELECT concept_id FROM attributes
			 WHERE attribute_name='MAPSETTYPE'
			   AND attribute_value='ATX'
			   AND tobereleased in ('Y','y'))
      AND substr(attribute_value,3,instr(attribute_value,'~',3)-3) = b.cui
    UNION
    SELECT attribute_id
    FROM attributes a, cui_history b
    WHERE attribute_name='XMAPFROM'
      AND concept_id in (SELECT concept_id FROM attributes
			 WHERE attribute_name='MAPSETTYPE'
			   AND attribute_value='ATX'
			   AND tobereleased in ('Y','y'))
      AND substr(attribute_value,1,instr(attribute_value,'~')-1) = cui1
      AND relationship_name = 'DEL'
    UNION
    SELECT attribute_id
    FROM attributes a, t_delcui_$$ b
    WHERE attribute_name='XMAPFROM'
      AND concept_id in (SELECT concept_id FROM attributes
			 WHERE attribute_name='MAPSETTYPE'
			   AND attribute_value='ATX'
			   AND tobereleased in ('Y','y'))
      AND substr(attribute_value,1,instr(attribute_value,'~')-1) = b.cui
    }) || ((print L "<span id=red>Error at 11 ($DBI::errstr).</span>")
     &&  return);

  #
  # Turn off old XMAP and XMAPFROM attributes
  #
  print L "    Remove old XMAP,XMAPFROM ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
     SELECT COUNT(*) FROM tdel_$$
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
    open(CMD,"$ENV{MEME_HOME}/bin/batch.pl $port $host -a=D -t=A -s=t tdel_$$ $db MTH | /bin/sed 's/^/      /' |") ||
      ((print L "<span id=red>Error running batch.pl ($! $?).</span>")
       &&  return);
    while (<CMD>) {
      print L $_;
    }
    close(CMD);
  }

  #
  # Insert new XMAPFROM attributes
  #
  print L "    Insert new XMAPFROM attributes ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
     SELECT COUNT(*) FROM txmapfrom_$$
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
    open(CMD,"$ENV{MEME_HOME}/bin/insert.pl $host $port -atts txmapfrom_$$ $db MTH | sed 's/^/      /' |") ||
      ((print L "<span id=red>Error running insert.pl ($! $?).</span>")
       &&  return);
    while (<CMD>) {
      print L $_;
    }
    close(CMD);
  }

  #
  # Insert new XMAP attributes
  #
  print L "    Insert new XMAP attributes ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare(qq{
     SELECT COUNT(*) FROM txmap_$$
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
    open(CMD,"$ENV{MEME_HOME}/bin/insert.pl $host $port -atts txmap_$$ $db MTH | sed 's/^/      /' |") ||
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
        MEME_UTILITY.drop_it('table','tdel_$$');
        MEME_UTILITY.drop_it('table','txmap_$$');
        MEME_UTILITY.drop_it('table','txmapfrom_$$');
        MEME_UTILITY.drop_it('table','t_delcui_$$');
        MEME_UTILITY.drop_it('table','t_mergecui_$$');
    END;
  }) ||
    ((print L "<span id=red>Error at end ($DBI::errstr).</span>")
     &&  return);
  $sh->execute ||
    ((print L "<span id=red>Error at end ($DBI::errstr).</span>")
     &&  return);

  # disconnect
  $dbh->disconnect;

  close(L);
  return 1;
} # Handle_atx_cui_map

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
  my($retval);
  my($sh);
  my($line);
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
 This CGI script is used to access Release maintenance tools.
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


