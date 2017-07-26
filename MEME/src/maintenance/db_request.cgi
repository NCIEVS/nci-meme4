#!@PATH_TO_PERL@
#
# File:     db_request.cgi
# Author:   Bobby Edrosa, Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      $MEME_HOME/utils/meme_utils.pl
#
# Description: 
#
# Changes:
# 02/11/2008 BAC (1-GA9K1): Additional tweak to ls -t command used for "Submit an update".
# 01/23/2008 BAC (1-GA9K1): Fixed "Submit an Update" button problem.
# 12/04/2007 BAC (1-FWW2P): Add "Secondary Inversion" State.
# 04/16/2007 BAC (1-DZZ2B): add "Production" state.
# 02/08/2007 BAC (1-DGJJT): Do not allow space chars in "source" names.
# 02/07/2007 BAC (1-DFIVF): Fix state transition problem for "Test Insertion"
# 01/09/2007 TTN (1-D7Q1J): The javascript references to selected index and form are not compatible with IE
# 01/09/2007 BCA (1-D7SR1): Remove link to database_work_instructions.html page
#  Assign all <option> tags a value attribute.
# 01/04/2007 BAC (1-D6GUV): Fix to ensure "To:" list is always set properly.
# 12/28/2006 BAC (1-D0OQN): Ensure proper use of GET/PUT form methods.
# 12/22/2006 TTN,BAC (1-D0OQN): Improvements based on training call and general use.
# 09/10/2006 BAC (1-BO99D): Completion of major enhancements to 
#  support source and DB tracking.  System is now ready for demo/training
# 07/03/2006 RBE (1-BO99D): Major enhancement
# 06/26/2006 RBE (1-BIC5F): More enhancement for db_request script
# 05/22/2006 RBE (1-B9MTK): Tracking DB State
# 03/22/2006 RBE (1-AQRCB): use MID Services mailing list
# 12/22/2005 BAC (1-719SM): use open ":utf8"  added
#
# Version info:
# 11/24/2003 3.5.0: Major ugprade to manage more info & add search functionws
# 03/14/2003 3.4.1: Removes log files older than a year.
# 12/11/2002 3.4.0: Release to support response form and response log functionality.
# 09/16/2002 3.3.0: Additional procedures. PrintREQUEST_LOG, PrintREQUEST_LOGS.
# 05/07/2002 3.2.1: link to laura's guidelines page
#                   Option to cut off editing
# 04/11/2002 3.2.0: mid services changed, so we use
#                   the database service to choose
#                   the database from, logical names are gone
# 01/30/2002 3.1.0: Released
#                   midsvcs names were from things like
#                   current-editing-tns to editing-db
# 11/30/2001 3.0.6: Added current-mrd as a possible database
# 01/19/2001 3.0.5: 1st version created
#
$release = "3";
$version = "5.0";
$version_authority = "RBE";
$version_date = "06/26/2006";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";
use Mail::Sender;
use File::stat;

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
       	"command" => 1,
       	"prefix" => 1,
       	"page" => 1,
  	"mail_to" => 1,
	"mail_from" => 1,
	"mail_cc" => 1,
        "request_from" => 1,
        "db_name" => 1,
	"msg_date" => 1,
        "req_time" => 1, 
	"next" => 1,
	"update" => 1,
        "backups" => 1,
        "cutoff_editing" => 1,
        "state" => 1,
        "sub_state" => 1,
        "source" => 1,
        "work" => 1,
        "notes" => 1,
        "script" => 1,
        "details" => 1,
	"mail_details_to" => 1,
	"log_name" => 1,
	"search" => 1,
	"new_source" => 1,
	"index" => 1
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

#
# This is the HTTP header, it always gets printed
#
&meme_utils::PrintHTTPHeader;

#
# Readparse translates CGI argument string into variables 
# that can be used by the script
#

&meme_utils::ReadParse($_);

#
# Some environment vars have to be redeclared AFTER meme_utils is imported
#
$ENV{"MEME_HOME"} = $meme_home || $ENV{"MEME_HOME"} || die "\$MEME_HOME must be set.";

@state_list = ("Refresh DB", "Test Insertion", "Test Insertion Review", "Real Insertion", "Real Insertion Review", "Editing", "Pre Production", "Maintenance", "Freeze", "Acquisition", "Handoff To Inverter", "Inversion", "Handoff To Inserter", "SRC-SRC QA", "Production","Secondary Inversion");
@sub_state_list = ("REQUESTED", "ACKNOWLEDGED", "IN PROGRESS", "COMPLETED","PROBLEM");
@form_fields = ("mail_to", "mail_details_to", "mail_from", "db_name", "msg_date", "req_time", "backups", "cutoff", "state", "sub_state", "source", "work", "notes", "script", "details");
%display_map = ( 
   "0,0" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, "req_time" => 1,
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "0,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
 "1,0" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, "req_time" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "1,3" => {"mail_to" => 1, "mail_details_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1,
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "script"=>1, "details"=>1},
   "1,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, "backups" => 1,
	     "cutoff" => 1, "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "2,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "3,0" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, "req_time" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "3,3" => {"mail_to" => 1, "mail_details_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1,
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "script"=>1, "details"=>1},
   "3,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1,
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "backups" => 1, "cutoff" => 1},
   "4,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "5,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "6,0" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, "req_time" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "6,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, "req_time" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "7,0" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, "req_time" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "7,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1},
   "8,*" => {"mail_to" => 1, "mail_from" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "notes" => 1},
   "9,*" => {"mail_to" => 1, "mail_from" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "notes" => 1},
   "10,*" => {"mail_to" => 1, "mail_from" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "notes" => 1},
   "11,*" => {"mail_to" => 1, "mail_from" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "notes" => 1},
   "15,*" => {"mail_to" => 1, "mail_from" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "notes" => 1},
   "12,*" => {"mail_to" => 1, "mail_from" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "notes" => 1},
   "13,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "notes" => 1},
   "14,*" => {"mail_to" => 1, "mail_from" => 1, "db_name" => 1, "msg_date" => 1, 
	     "state" => 1, "sub_state" => 1, "source" => 1, "work" => 1, "notes" => 1}
);

%state_transitions = (
   "0,0" => "0,2", "0,1" => "0,2", "0,2" => "0,3", "0,3" => "1,0",
   "1,0" => "1,1", "1,1" => "1,2", "1,2" => "1,3", "1,3" => "2,2",
   "2,0" => "2,3", "2,1" => "2,3", "2,2" => "2,3", "2,3" => "3,0",
   "3,0" => "3,1", "3,1" => "3,2", "3,2" => "3,3", "3,3" => "4,2",
   "4,0" => "4,2", "4,1" => "4,2", "4,2" => "5,2", "4,3" => "5,2",
   "5,0" => "5,2", "5,1" => "5,2", "5,2" => "5,3", "5,3" => "5,3",
   "6,0" => "6,2", "6,1" => "6,2", "6,2" => "6,3", "6,3" => "5,0",
   "7,0" => "7,1", "7,1" => "7,2", "7,2" => "7,3", "7,3" => "5,2",
   "8,0" => "8,1", "8,1" => "6,2", "8,2" => "6,2", "8,3" => "6,2",
   "9,0" => "9,3", "9,1" => "9,3", "9,2" => "9,3", "9,3" => "10,1",
   "10,0" => "10,1", "10,1" => "11,2", "10,2" => "11,2", "10,3" => "11,2",
   "11,0" => "11,1", "11,1" => "11,2", "11,2" => "11,3", "11,3" => "12,1",
   "15,0" => "15,1", "15,1" => "15,2", "15,2" => "15,3", "15,3" => "12,1",
   "12,0" => "12,1", "12,1" => "0,0", "12,2" => "0,0", "12,3" => "0,0",
   "13,0" => "13,3", "13,1" => "13,3", "13,2" => "13,3", "13,3" => "13,3",
   "14,0" => "14,3", "14,1" => "14,3", "14,2" => "14,3", "14,3" => "14,3"
);

#
# Cache mid services
#
&cacheMIDServices;

#
# Configure Mail Settings
#
unless ($smtp_host) {
$smtp_host = $midsvcs{"smtp-host"}; }
unless ($mail_cc) {
$mail_cc = $midsvcs{"request-cc-list"}; }

#
# Default Settings for CGI parameters (Except in submit mode)
#
if ($command ne "SUBMIT") {
  #$db_name = $midsvcs{"editing-db"} unless $db_name;
  unless ($msg_date) {
    $msg_date = `/bin/date +%m/%d/%Y`;
    chop ($msg_date);
  }
  unless ($mail_details_to) {
    $mail_details_to = $midsvcs{"request-details-to"}; }
  @time=local_time;
  $req_time = $midsvcs{"request-time"} unless $req_time;
  $req_time = "6:30 EDT" if (! $req_time && $time[8]);
  $req_time = "6:30 EST" if (! $req_time && ! $time[8]);
  $backups = $midsvcs{"request-backups"} unless $backups;
  $backups = "Enabled" unless $backups;
  $cutoff_editing = $midsvcs{"request-cutoff-editing"} unless $cutoff_editing;
  $cutoff_editing = "No" unless $cutoff_editing;
  $source = "" unless $source;
  $work = "" unless $work;
  $command = "" unless $command;
  if (! $state && $command eq "SAB_INDEX") {
  	$state = 9; $sub_state = 3;
  }
  $state = 0 unless $state;
  $sub_state = 0 unless $sub_state;
}

#
# Miscellaneous environment settings
#
$msg_date_Ymd = `/bin/date "+%m/%d/%Y %H:%M:%S"`;
chop($msg_date_Ymd);
$msg_dir = "DBRequests";
$date_Ymd = `/bin/date +%Y%m%d`;
$date_Ymd =~ s/\n//;

#
# The commands array maps commands to procedures for
# printing the header, body, and footer
#
%commands = (
	     "BROWSE_DB_MESSAGES" =>
	         ["PrintHeader","PrintBROWSE_MESSAGES","PrintFooter","Browse Messages"],
	     "BROWSE_SAB_MESSAGES" =>
	         ["PrintHeader","PrintBROWSE_MESSAGES","PrintFooter","Browse Messages"],
	     "VIEW_MESSAGE" =>
	         ["PrintHeader","PrintVIEW_MESSAGE","PrintFooter","Send Message"],
	     "SUBMIT" => 
	         ["PrintHeader","PrintSUBMIT","PrintFooter","Submit Message"],
	     "DB_REPORT" => 
	         ["PrintHeader","PrintDB_REPORT","PrintFooter","View DB Messages"],
	     "SAB_REPORT" => 
	         ["PrintHeader","PrintSAB_REPORT","PrintFooter","View SAB Messages"],
	     "SAB_REPORT_LIST" => 
	         ["PrintHeader","PrintSAB_REPORT_LIST","PrintFooter","View SAB Messages"],
	     "DB_INDEX" => 
	         ["PrintHeader","PrintForm","PrintFooter","Database Tracking"],
	     "SAB_INDEX" => 
	         ["PrintHeader","PrintSABForm","PrintFooter","Source Tracking"],
	     "DELETE_SAB" => 
	         ["PrintHeader","PrintDELETE_SAB","PrintFooter","Delete Source Entry"],
	     "ADD_DB_NOTE" => 
	         ["PrintHeader","PrintADD_DB_NOTE","PrintFooter","Add Note"],
	     "REQUEST_DB_UPDATE" => 
	         ["PrintHeader","PrintREQUEST_DB_UPDATE","PrintFooter","Request Update"],
	     "ADD_SAB_NOTE" => 
	         ["PrintHeader","PrintADD_SAB_NOTE","PrintFooter","Add Note"],
	     "REQUEST_SAB_UPDATE" => 
	         ["PrintHeader","PrintREQUEST_SAB_UPDATE","PrintFooter","Request Update"],
	     "CHANGE_SAB" => 
	         ["PrintHeader","PrintCHANGE_SAB","PrintFooter","Change SAB"],
	     "" => 
	         ["PrintHeader","PrintForm","PrintFooter","Database Tracking"]
             );

#
# Check to see if command exists
#
if ($commands{$command}) {
    
  &RemoveOldLogFiles;

  #
  # Print Header, Body, and Footer
  #
  $header = $commands{$command}->[0];
  $body = $commands{$command}->[1];
  $footer = $commands{$command}->[2];
    
  &$header($commands{$command}->[3]);;
  &$body;
  &$footer;
}

exit(0);


# HTML ######################################################################################
#
#  The following procedures generate HTML document
#
#############################################################################################

# Procedure PrintHeader ##############################################################
#
# This procedure prints the request (html) header
#
sub PrintHeader {
  my($title) = @_;
    $fheader = "<center><h2>$title</h2></center>";
    &meme_utils::PrintSimpleHeader($title, "","",$fheader)

}

# Procedure PrintForm ###################################################
#
# This procedure displays the request form.
#
sub PrintForm {

    print qq{
    <blockquote>
      <p>Select one of the following actions:</p>
      <ol>
        <li><a href="$ENV{SCRIPT_NAME}?command=DB_REPORT">View DB Status Report</a></li>
        <li><a href="$ENV{SCRIPT_NAME}?command=BROWSE_DB_MESSAGES">Browse DB Status Messages</a></li>
	<li>New Request<br>&nbsp;<br>
	 
    };

    &getFormHTML;

    print qq{
       </ol>
    </blockquote>
    };

}

# Procedure PrintSABForm ###################################################
#
# This procedure displays the request form.
#
sub PrintSABForm {

    print qq{
    <blockquote>
      <p>Select one of the following actions:</p>
      <ol>
        <li><a href="$ENV{SCRIPT_NAME}?command=SAB_REPORT_LIST">View Source Status Reports</a></li>
        <li><a href="$ENV{SCRIPT_NAME}?command=BROWSE_SAB_MESSAGES">Browse Source Status Messages</a></li>
	<li>New Request<br>&nbsp;<br>
	 
    };

    &getFormHTML;

    print qq{
       </ol>
    </blockquote>
    };

}

# Procedure PrintFooter #####################################################################
#
# This procedure prints the (html) footer
#
sub PrintFooter {

  $flag = "DB_INDEX"; $switch_flag="SAB_INDEX"; $switch_label="SAB Index";
  if ($command =~ /SAB/ || $log_name =~ /sab/ || $index =~ /SAB/ || $prefix eq "sab") { $flag="SAB_INDEX"; $switch_flag="DB_INDEX"; $switch_label="DB Index"; }

    print qq{
	<P>
	<HR WIDTH="100%">
        <TABLE BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
        <TR NOSAVE>
        <td align=LEFT VALIGN=TOP NOSAVE>
        <ADDRESS>
        <A HREF="$ENV{SCRIPT_NAME}?command=$flag">Back to Index</A> [Switch to: <A HREF="$ENV{SCRIPT_NAME}?command=$switch_flag">$switch_label</A>]</ADDRESS>
        </td>

        <td align=RIGHT VALIGN=TOP NOSAVE>
        <ADDRESS>
	  <font size=-1>Contact: <A HREF="mailto:bcarlsen\@msdinc.com">Brian A. Carlsen</A><br>},scalar(localtime),qq{<br>};
        &PrintVersion("version");
        print qq{</font></ADDRESS>

        </td>
        </tr>
        </TABLE>

</BODY>
</HTML>
}

}

# Procedure PrintSUBMIT ############################################################################
#
# This procedure displays response status page, generates a response log file and sends mail.
#
sub PrintSUBMIT {

  #
  # Align work and code info
  #
  $work =~ s/(\r?\n)/\r\n    /g;
  $notes =~ s/(\r?\n)/\r\n    /g;
  $details =~ s/(\r?\n)/\r\n    /g;

  #
  # Prepare log file
  #
  $log_response = qq{\r
To:             $mail_to\r
From:           $mail_from\r
Details To:     $mail_details_to\r
Database:       $db_name\r
Date:           $msg_date\r
Time:           $req_time\r
Backups:        $backups\r
Cutoff Editing: $cutoff_editing\r
State:          $state\r
Sub State:      $sub_state\r
Source:   	$source\r
Work:\r
    $work
\r
Script:         $script\r
\r
Code/Details:\r
    $details
\r
Notes:\r
    $notes
\r
};

  if (&WriteDBStatus) {

    #
    # Write message log
    #
    unlink "/tmp/response.$$.txt";
    $log_file = &GetLogFile("$msg_dir/db.$date_Ymd.$db_name");
    $file_name = $log_file;
    $file_name =~ s/[^\/]*\///;
    open (IO_RFILE, ">$log_file") || die "Failed to create $log_file: ($! $?).";
    print IO_RFILE "$log_response";
    close (IO_RFILE);

    #
    # Write DB status file
    #    
    open (IO_RFILE, ">$msg_dir/db.status.$db_name.txt") || 
	die "Failed to create $msg_dir/db.status.$db_name.txt: ($! $?).";
    print IO_RFILE "$log_response";
    close (IO_RFILE);

  }

  if (&WriteSABStatus) {

    #
    # Write sab log
    #
    $log_file = &GetLogFile("$msg_dir/sab.$date_Ymd.$source");
    $file_name = $log_file;
    $file_name =~ s/[^\/]*\///;
    open (IO_RFILE, ">$log_file") || die "Failed to create $log_file: ($! $?).";
    print IO_RFILE "$log_response";
    close (IO_RFILE);

    #
    # Write SAB status file
    #
    open (IO_RFILE, ">>$msg_dir/sab.status.$source.txt") || 
	die "Failed to create $msg_dir/sab.status.$source.txt: ($! $?).";
    print IO_RFILE "---- record break ----\r\n$log_response";
    close (IO_RFILE);
  }

  #
  # Look up next state/substate (for email message)
  #
  $next_state = &GetNextState($state, $sub_state);
  $next_sub_state = &GetNextSubstate($state, $sub_state);

  #
  # Prepare email response
  #
  $email_response = qq{<html><body>\r
  <pre>
Database:       $db_name
Date:           $msg_date
Time:           $req_time
Backups:        $backups
Cutoff Editing: $cutoff_editing
State:          $state_list[$state]
Sub State:      $sub_state_list[$sub_state]
Source:     	$source};
  if ($work) {
    $email_response .= qq{
Work:
    $work};
  }
  if ($notes) {
    $email_response .= qq{
Notes:
    $notes};
  }
  $email_response .= qq{
</pre>
To respond to this message, follow this link:\r<br>
<a href="https://$ENV{SERVER_NAME}:$ENV{SERVER_PORT}$ENV{SCRIPT_NAME}?command=VIEW_MESSAGE&log_name=$file_name&next=1">\r
Next State:     $state_list[$next_state], $sub_state_list[$next_sub_state]\r
</a>\r
</body></html>
};

  if ($mail_details_to) {

  #
  # Prepare "details" email response
  #
  $apelon_response = qq{\r
Database:       $db_name\r
Date:           $msg_date\r
Time:           $req_time\r
Backups:        $backups\r
Cutoff Editing: $cutoff_editing\r
State:          $state_list[$state]\r
Sub State:      $sub_state_list[$sub_state]\r
Source:     	$source\r
Work:\r
    $work
\r
Script:         $script\r
\r
Code/Details:\r
    $details
\r
Notes:\r
    $notes
\r
};

}

  #
  # Send email response
  #
  $for_source = "for $source:" if $source;
  $subject = "$state_list[$state] $for_source $sub_state_list[$sub_state] ($db_name)";
  if ($update) {
    $subject = "UPDATE: $subject";
  }
  $sender = new Mail::Sender{
      smtp => "$smtp_host", 
      from => "$mail_from"};
  $sender->MailMsg({to => "$mail_to", cc => "$mail_cc",
  		    subject => "$subject",
  		    ctype => "text/html",
		    encoding => "quoted-printable",
		    msg => "$email_response"});
  if ($sender->{error}) {
    $msg = qq{<B><FONT COLOR="A00000"> There was an error sending the following response (}.$sender->{error_msg}.qq{):</font></B>};
  } else {
    $msg = "Message successfully submitted:";
  }

  #
  # Send "details" message
  #
  if ($mail_details_to) {

    #
    # Send apelon response
    #
    $sender = new Mail::Sender{
        smtp => "$smtp_host", 
        from => "$mail_from"};
    $sender->MailMsg({to => "$mail_details_to", 
		      subject => "$subject",
		      msg => "$apelon_response"});
    if ($sender->{error}) {
      $msg = qq{<B><FONT COLOR="A00000"> There was an error sending the following response (}.$sender->{error_msg}.qq{):</font></B>};
    } else {
    $msg = "Message successfully submitted:";
    }

}

  print qq {
	$msg<br><br>
   };

  #
  # Show table for message sent.
  #
  open (F, "$log_file") || (print "could not open $log_file: $! $?\n<br>" && return);
  #
  # Obtain information from the file
  #
  &ReadSingleEntryFromFilehandle(F);
  print &GetReportTableHTML;
  close(F);
}


# Procedure getFormHTML ###############################################################
#
# Returns HTML for the form.
#
sub getFormHTML {
  my($l_state, $l_sub_state) = ($state, $sub_state);

  #
  # Initialize select list settings
  #
  $selected{$db_name}=" SELECTED";
  $selected{$backups}=" SELECTED";
  $selected{$cutoff_editing}=" SELECTED";
  $state_selected{$state}=" SELECTED";
  $sub_state_selected{$sub_state}=" SELECTED";
 
  
  #
  # Get databases info
  #
  $dbs = $midsvcs{"databases"};
  $mrd = $midsvcs{"mrd-db"};
  $databases{$mrd} = $mrd;
  foreach $db (sort split /,/, $dbs) {
    $databases{$db} = $db;
  }

  #
  # Configure "mail to", use state specific list if available
  #
  # If we are in a "request" mode (sub_state=0), compute $mail_to
  # Otherwise, reuse what it was before.
  $default_mail_to = $midsvcs{"request-list"};
  if ($sub_state == 0) {
    $mail_to = $midsvcs{"request-state-$state-list"};
    unless ($mail_to) {
      $mail_to = $default_mail_to;
    } ;
  }
  foreach $s (0..(scalar(@state_list)-1)) {
    $val = $midsvcs{"request-state-$s-list"};
    if ($val) {
      $mail_to{"$s"} = $val;
    } else {
      $mail_to{"$s"} = $default_mail_to; 
    }
  }
  # Overwrite current state with actual $mail_to setting
  $mail_to{$state} = "$mail_to" if $mail_to;
  # Set $mail_to to the default for this state if not yet set
  $mail_to = $mail_to{$state} unless $mail_to;
  
  #
  # Prep the "from" list.  
  # 
  $mail_all = $midsvcs{"request-from-list"};
  if ($mail_all !~ /$mail_from/) {
      $mail_all .= ", $mail_from";
  }
  @mail_all = sort split /,/, $mail_all;

  #
  # If in update mode, set "from" as it was.
  #
  if ($update) {
    $selected{$mail_from} = " SELECTED";
  }

  #
  # Print the response form Javascript
  #
  &PrintResponseFormJavascript;

  #
  # Print response form
  #
  print qq{
    <form method="POST" action="$ENV{SCRIPT_NAME}" name="response_form"> 
      <input type="hidden" name="command" value="SUBMIT">
      <input type="hidden" name="index" value="$index">
      <input type="hidden" name="update" value="$update">
      <center>
      <table width="90%" cellspacing="2" cellpadding="2">
        <tr id="mail_to_tr" style="display:">
          <td align="left"><font size=+1>To:</font></td>
          <td align="left"><font size=+1>
	    <input type="text" name="mail_to" VALUE="$mail_to" size=60></font></td>
        </tr>
        <tr id="mail_details_to_tr" style="display:">
          <td align="left"><font size=+1>Mail Details To:</font></td>
          <td align="left"><font size=+1>
	    <input type="text" name="mail_details_to" value="}.$mail_details_to.qq{" size=60></font></td>
        </tr>
	};

  $mail_from_list = qq{<select name="mail_from">\n
    <option SELECTED>-- Choose your email address --</option>};
    foreach $address (sort @mail_all) {
      $address =~ s/(\s*)$//;
      $address =~ s/ //;
      $mail_from_list .= qq{  <option value="$address" $selected{$address}>$address</option>\n} if $address ne $current_address;
      $current_address = $address;
    }
  $mail_from_list .= "</select>";

  print qq{
        <tr id="mail_from_tr" style="display:">
          <td align="left"><font size=+1>From:</font></td>
          <td align="left"><font size=+1>
	    $mail_from_list</font> <a href="javascript:addEmail()">Add Email</a></td>
        </tr>
        <tr id="db_name_tr" style="display:">
          <td align="left"><font size=+1>Database:</font></td>
          <td align="left"><font size=+1>
	   <select name="db_name">
	     <option>-- Choose Database --</option>
};
  foreach $db (sort keys %databases) {
    print qq{	     <option value="$db" $selected{$db}>$db</option>
};
  }
  print qq{
	   </select>
        </tr>
        <tr id="msg_date_tr" style="display:">
        <td align="left"><font size=+1>Date:</font></td>
        <td align="left"><font size=+1>
	   <input type="text" name="msg_date" VALUE="$msg_date_Ymd" size="20"></font></td>
        </tr>
        <tr id="req_time_tr" style="display:">
          <td align="left"><font size=+1>Time:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="req_time" VALUE="$req_time" size=10></font></td>
        </tr>
        <tr id="backups_tr" style="display:">
          <td align="left"><font size=+1>Backups:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="backups" VALUE="$backups" size=10></font></td>
        </tr>
        <tr id="cutoff_tr" style="display:">
          <td align="left"><font size=+1>Cutoff Editing:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="cutoff" VALUE="$cutoff_editing" size=10></font></td>
        </tr>       
       	<tr id="state_tr" style="display:">
	  <td align="left"><font size=+1>State:</font></td>  <td align="left"><font size=+1>
	    <select name="state" onchange="stateChanged()">
		};
		$ct = 0;
		map ($state_map{$_} = $ct++ , @state_list);
		foreach $val (sort keys %state_map) {
			print qq{    <option $state_selected{$state_map{$val}} value="$state_map{$val}">$val ($state_map{$val})</option>\n};
		}
	print qq{  </select></font>
	  </td>
	</tr>        
       	<tr id="sub_state_tr" style="display:">
	  <td align="left"><font size=+1>Sub State:</font></td>  <td align="left"><font size=+1>
	    <select name="sub_state" onchange="subStateChanged()">
		};
		$ct = 0;
		foreach $val (@sub_state_list) {
			print qq{    <option $sub_state_selected{$ct} value="$ct">$val</option>\n};
			$ct++;
		}
	print qq{    </select></font>
	  </td>
	</tr>
        <tr id="source_tr" style="display:">
          <td align="left"><font size=+1>Source:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="source" VALUE="$source" size=10></font></td>
        </tr>
        <tr id="work_tr" style="display:">
          <td align="left" VALIGN="top"><font size=+1>Work:</font></td>
          <td align="left"><font size=+1>
	   <textarea onchange="work_change=true;" wrap cols=60 rows=10 name="work">$work</textarea></font></td>
        </tr>
        <tr id="notes_tr" style="display:">
          <td align="left" VALIGN="top"><font size=+1>Notes:</font></td>
          <td align="left"><font size=+1>
	   <textarea wrap cols=60 rows=10 name="notes">$notes</textarea></font></td>
        </tr>
        <tr id="script_tr" style="display:">
          <td align="left" VALIGN="top"><font size=+1>Script:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="script" value="$script" size=60></font></td>
        </tr>
        <tr id="details_tr" style="display:">
          <td align="left" VALIGN="top"><font size=+1>Code/Details:</font></td>
          <td align="left"><font size=+1>
	   <textarea wrap cols=60 rows=10 name="details">$details</textarea></font></td>
        </tr>
        <tr>
          <td colspan="2" align="center"> <font size=+1>
	   <input type="button" value="Send"
	     onClick="submitForm(this.form);"
	     onMouseOver="window.status='Send Response'; return true;"
	     onMouseOut="window.status=''; return true;">
        </font></td>
      </tr>
    </table>
    </center>
  </form>
  <script language="javascript">
    subStateChanged();
  </script>
  };
}

# Procedure PrintCHANGE_SAB ############################################################################
#
# This procedure changes SAB values in .txt files and reload SAB_REPORT_LIST page.
#
sub PrintCHANGE_SAB {

	while (defined($next = <$msg_dir/sab.*.$source.txt>)) {
		$date = (split /\./, $next)[1];
		if (-e "$msg_dir/sab.$date.$new_source.txt") {
	    	open (IO_WFILE, ">>$next") || die "Failed to append to $next: ($! $?).";
	    	open (IO_RFILE, "<$msg_dir/sab.$date.$new_source.txt") || die "Failed to open $msg_dir/sab.$date.$new_source.txt: ($! $?).";
			while (<IO_RFILE>) { print IO_WFILE; }
			close (IO_RFILE);
			close (IO_WFILE);
		}
    	open (IO_WFILE, ">$msg_dir/sab.$date.$new_source.txt") || die "Failed to create $msg_dir/sab.$date.$new_source.txt: ($! $?).";
    	open (IO_RFILE, "<$next") || die "Failed to open $next: ($! $?).";
    	while(<IO_RFILE>) { s/^(Source:\W*)$source/$1$new_source/; print IO_WFILE;}
    	close (IO_RFILE);
    	unlink $next;
  	}	

	#
	# Do not update the various DB files for a changed record.
	#
	
 	print qq{$source was successfully changed to $new_source.<br>};
 	&PrintSAB_REPORT_LIST;


}

# Procedure PrintResposeFormJavascript #######################################################
#
# Print the javascript section for the form
#
sub PrintResponseFormJavascript{

  #
  # Print opeenr
  #
  print qq#
  <script language="JavaScript">
   
    // Handle state changing
    function stateChanged() {
      var lstate = document.response_form.state.value;
      setMailTo(lstate);
      subStateChanged();
    }
    function subStateChanged() {
      var lsub_state = document.response_form.sub_state.value;
      var lstate = document.response_form.state.value;
   #;
   	       foreach $field (@form_fields) {
                print qq{
      document.getElementById('${field}_tr').style.display = getDisplay(lstate, lsub_state, "$field");};
		}
		print qq#
    }

    // Get display style attribute for state,substate,field combo
    function getDisplay(state, sub_state, field) {
	 var value = 0;
	 // Always show notes if there are notes
	 if (field == "notes" && document.response_form.notes.value != "") {
	   return 1;
	 } #;

  #
  # Print dynamically generated entries for getDisplay
  #
  foreach $ss (reverse sort keys %display_map) {
    ($lstate, $lsub_state) = split /,/,$ss;
    foreach $field (sort keys %{$display_map{$ss}}) {
      if (!$display_map{$ss}->{$field}) { next; }
      if ($lsub_state ne "*") {
	print qq/
	 else if (state == $lstate && sub_state == $lsub_state && field == "$field") {
	   value = 1; }/;
      } else {
	print qq/
	 else if (state == $lstate && field == "$field") {
	   value = 1; }/;
      }
    }
  }

  #
  # Print return for getDisplay and start of mail list array
  #
  print qq#
        if (value == 1) {
	 return "";
       } else {
	 return "none";
       }
    }

    // Array of mail lists
    var mail_to_array = new Array();
  #;

  #
  # Print dynamically generated mail list entries
  #
  foreach $key (keys %mail_to) {
    print qq{       mail_to_array[$key] = "$mail_to{$key}";
     };
  }

  #
  # Print remaining functions
  #
  print qq#
    // Set "mail_to" field when state changes
    function setMailTo(state) {
      if (mail_to_array[state]) {
	    if (document.response_form.mail_to.value != mail_to_array[state]) {
	      alert("State change will cause 'To:' list to change.\\nPlease review the new list.");
  	    }
	    document.response_form.mail_to.value = mail_to_array[state];
      }
    }

    // Add an email to "mail from" list
    function addEmail() {
      var email = prompt("Enter your email address","user\@domain");
      document.response_form.mail_from.options[document.response_form.mail_from.options.length] = new Option(email);
    }

    var work_change = false;
    function isDigit(c) {
      return (c >= 0 || c <= 9);
    }

    // Handle submitting of request form
    function submitForm(form) {
      var str = form.msg_date.value;
      //check_date
      //  var error_msg = "The date must have the form mm/dd/yyyy.";
      //  if (str.length != 10) {
      //alert(error_msg); return false;
      //  }
      if (form.mail_from.selectedIndex == 0) {
        alert("You must pick a FROM email address"); return false;
      }
      if (document.getElementById("db_name_tr").style.display != "none" &&
	  form.db_name.selectedIndex == 0) {
        alert("You must pick a database"); return false;
      }
      if (form.source.value.indexOf(' ') != -1) {
        alert("Source field may not contain spaces.");
      }
      if (!work_change) {
        alert("Changes to work field is required."); return false;
      }
      for (var i=0; i<10; i++) {
        var c = str.substring(i,i+1);
        if (((i==2||i==5) && c != "/") ||
            ((i==0||i==1||i==3||i==4||i==6||i==7||i==8||i==9) && !isDigit(c))) {
          alert(error_msg); return false;
        }
      }
#;
      foreach $field (@form_fields) {
	print qq#
      if (document.getElementById("${field}_tr").style.display == "none" &&
	  form.${field}.value != undefined ) {
        form.${field}.value = "";
      }
#;
      }
      print qq#
      form.submit();
      return true;
    }
  </script>
#;

}

# Procedure PrintVIEW_MESSAGE ###############################################################
#
# This procedure displays a log from a previously selected db response. 
#
sub PrintVIEW_MESSAGE {
  #
  # Open the request log
  #
  open(F, "$msg_dir/$log_name") || die "couldn't open file: $log_name\n";

  $index = "SAB" if $log_name =~ /sab/;
  
  #
  # Obtain information from the request log.  Override date/time settings with defaults
  #
  &ReadSingleEntryFromFilehandle(F,1);
  close(F);

  #
  # Override request date with today's date
  #
  $msg_date_Ymd = `/bin/date "+%m/%d/%Y %H:%M:%S"`;

  #
  # Get next state/sub_state values
  #
  if ($next) {
    $lstate = &GetNextState($state, $sub_state);
    $lsub_state = &GetNextSubstate($state, $sub_state);
    ($state, $sub_state) = ($lstate, $lsub_state);
  }

  if ($sub_state == 0 && $next_state == 1) {
     &PrintForm;
     return;
  }

  &getFormHTML;

}

# Procedure PrintBROWSE_MESSAGES ##############################################################
#
# This procedure displays a selection of a list of database responses. 
#
sub PrintBROWSE_MESSAGES {

  #
  # Set page size
  #
  $lpp = 25;

  #
  # Set prefix/mode
  #
  if ($command eq "BROWSE_DB_MESSAGES") {
    $prefix = "db";
  } elsif ($command eq "BROWSE_SAB_MESSAGES") {
    $prefix = "sab";
  }

  #
  # Set "previous" link for pages >!
  #
  if ($page > 0) {
    $prev = $page - 1;
    $previous = qq{<p>[<a href="$ENV{SCRIPT_NAME}?command=$command&page=$prev&search=$search">Previous Page</a>]</p>};
  }

  #
  # Print this page's worth of entries
  #
  print qq{
  <p>Select one of the following messages:</p>
  $previous
  <blockquote>
};
  #
  # Read logs
  #
  chdir $msg_dir;
  if ($search) {
    foreach $word (split / /, $search) {
      unless ($f) {
	@files = `/bin/grep -i -l $word *.html *.txt`;
	$f = 1;
      } elsif (scalar(@files)>0) {
	chomp(@files);
	$files = join " ", @files;
	@files = `/bin/grep -i -l $word $files`
      }
    }
    @logs = reverse sort @files;
  } else {
    @logs = `ls -ta $prefix.[0-9]*.txt`;
  }

  if (scalar(@logs)>0) {  
    $j = ($page*$lpp)+$lpp;
    for ($i=$page*$lpp; $i < $j; $i++)  {
      $log = $logs[$i];
      last unless $log;
      chomp($log);
      print qq{
    <a href="$ENV{SCRIPT_NAME}?command=VIEW_MESSAGE&log_name=$log&prefix=$prefix"><tt>$log</tt></a><br>};
    }
  } else {
    print qq{<b>No matching logs found</b>.};
  }

  print qq{
  </blockquote>
};
  #
  # Determine if there are more pages, print next link
  #
  $next = $page + 1;
  if (scalar(@logs)>=($next*$lpp)) {
      print qq{
      <p>[<a href="$ENV{SCRIPT_NAME}?command=$command&page=$next&search=$search">Next Page</a>]</p>};
    }

}; # end PrintBROWSE_MESSAGES

# Procedure PrintDB_REPORT #####################################################################
#
# This procedure print a report
#
sub PrintDB_REPORT {

  print qq{
    <blockquote>
    <p>
};

  #
  # Print a report for each DB listed.
  #
  $databases = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s databases`;
  chop $databases;
  @databases = split /,/, $databases;
  foreach $db (sort @databases) {
    next unless -e "./$msg_dir/db.status.$db.txt";
    open (F, "$msg_dir/db.status.$db.txt") || next;
    #
    # Obtain information from the file
    #
    &ReadSingleEntryFromFilehandle(F,1);
    print qq{    <h2>$db_name</h2};
    print &GetReportTableButtonsHTML("DB");
    print &GetReportTableHTML;
    print qq{<br><hr width="100%"></br>};
    close(F);

  }

  print qq{
    </p>
    </blockquote>
};

}

# Procedure PrintSAB_REPORT #####################################################################
#
# This procedure print a SAB report
#
sub PrintSAB_REPORT {

  print qq{
    <blockquote>
    <p>
};

  #
  # Save information from the file
  #
  open (F, "$msg_dir/sab.status.$source.txt");
  # read first line (record sep)
  <F>;
  while(&ReadSingleEntryFromFilehandle(F,1)) {
    $table_html = &GetReportTableHTML;
    unshift @html, $table_html;
  }
  
  #
  # Print in reverse chronological order
  #
  print qq{    <h2>$source</h2>};
  print &GetReportTableButtonsHTML("SAB");
  foreach $html (@html) {
      print qq{      $html<br><hr width="100%"><br>};
     }
  close(F);

  print qq{
    </p>
    </blockquote>
};

}

# Procedure PrintSAB_REPORT_LIST ################################################################
#
# This procedure print a SAB report list
#
sub PrintSAB_REPORT_LIST {

  print qq{
    <blockquote>
    <p>
};

  @files = `ls -tr $msg_dir/sab.status.*.txt`;
  chomp @files;
  print qq#
	     <script language="JavaScript">
	     function submitViewForm(form) {
	     	var index = form.source.selectedIndex;
	       if(index == -1) { alert("Please select a source to view.");return false;}
	       form.command.value = "SAB_REPORT";
	       return true;			   
 	     }
	     function submitDeleteForm(form) {
	       var index = form.source.selectedIndex;
	       if(index == -1)  { alert("Please select a source to remove.");return false;}
	       var flag = confirm("Are you sure you want to remove '" + form.source.options[index].value + "'?");
	       if (flag) {form.command.value = "DELETE_SAB";
	       return true; }
	       return false;			   
 	     }
	     function submitChangeForm(form) {
	       var index = form.source.selectedIndex;
	       if(index == -1)  { alert("Please select a source to change.");return false;}
	       var new_source = prompt("Enter new SAB",form.source.options[index].value);
	       if (new_source == null ) { return false; }
	       form.command.value = "CHANGE_SAB";
		   form.new_source.value = new_source; 
	       return true;			   
 	     }
	     </script>
	     <table align="center">
	     <form name="sabform" action="$ENV{SCRIPT_NAME}" method="GET">
	       <input type="hidden" name="command" value="SAB_REPORT">
	       <input type="hidden" name="new_source" value="">
	     <tr><td>
	       <SELECT size="40" name="source">
  #;
  foreach $source (sort @files) {
    $source =~ s/$msg_dir\/sab\.status\.//;
    $source =~ s/(.*)\.txt.*/$1/;
    print qq{
	       <option value="$source">$source</option>
    };
  }

  print qq{
  		   </select>
  		 </td></tr>
	     <tr><td align="center">
	       <input type="submit" value="View Report" onClick="return submitViewForm(this.form);" >
	       <input type="submit" value="Delete" onClick="return submitDeleteForm(this.form);">
	       <input type="submit" value="Change" onClick="return submitChangeForm(this.form);">
  		 </td></tr>
	     </form>
	    </table>
    </p>
    </blockquote>
};

}

# Procedure PrintDELETE_SAB ################################################################
#
# Removes a sab report
#
sub PrintDELETE_SAB{
  unlink "$msg_dir/sab.status.$source.txt";

  print qq{$source entries successfully removed.<br>};
  &PrintSAB_REPORT_LIST;

}

# Procedure PrintADD_DB_NOTE ################################################################
#
# Adds a note to a DB report (creates a new message).
#
sub PrintADD_DB_NOTE{

  unless ($notes) {
    open (F,"$msg_dir/db.status.$db_name.txt") || (print "Couldn't open log file ($db_name)" && return);
    &ReadSingleEntryFromFilehandle(F);
    print qq{    <h2>$db_name</h2};
    print &GetReportTableHTML;
    print qq{
    <p><center><table width="90%" border="0"><tr><td>
    <form method="POST" action="$ENV{SCRIPT_NAME}">
     <input type="hidden" name="command" value="ADD_DB_NOTE">
     <input type="hidden" name="db_name" value="$db_name">
     <textarea wrap cols="60" rows="10" name="notes"></textarea><br>
     <input type="submit" value="Add Note">
    </form></td></tr></table></center></p>
  }
  } else {
    @files = `ls -t $msg_dir/db.[0-9]*.*.txt`;
    $file = $files[0]; chop($file);
    open (F,">>$file") || (print "Couldn't open log file ($file)" && return);
    $notes =~ s/(\r?\n)/\r\n    /g;
    print F "Notes:\r\n    <hr>$notes\r\n\r\n";
    close(F);
    open (F,">>$msg_dir/db.status.$db_name.txt") || (print "Couldn't open log file ($db_name)" && return);
    $notes =~ s/(\r?\n)/\r\n    /g;
    print F "Notes:\r\n    <hr>$notes\r\n\r\n";
    close(F);
    print "Note successfully added.<br><br>";
    &PrintDB_REPORT;
    return;
  }
}

# Procedure PrintREQUEST_DB_UPDATE ################################################################
#
# Requests an update to latest DB state
#
sub PrintREQUEST_DB_UPDATE{
  my($l_request_from, $l_source, $l_db_name) = ($request_from, $source, $db_name);


  open (F, "$msg_dir/db.status.$db_name.txt") || next;
  #
  # Obtain information from the file
  #
  &ReadSingleEntryFromFilehandle(F);

  #
  # Prepare email response
  #
  $email_response = qq{<html><body>\r
<P>Someone has requested that you update the state of the following work.\r
<a href="https://$ENV{SERVER_NAME}:$ENV{SERVER_PORT}$ENV{SCRIPT_NAME}?command=VIEW_MESSAGE&log_name=db.status.$db_name.txt&update=1">\r
Click here to do so.</a></p>\r
  <pre>
Database:       $db_name
Date:           $msg_date
Time:           $req_time
Backups:        $backups
Cutoff Editing: $cutoff_editing
State:          $state_list[$state]
Sub State:      $sub_state_list[$sub_state]
Source:         $source};
  if ($work) {
    $email_response .= qq{
Work:
    $work};
  }
  if ($notes) {
    $email_response .= qq{
Notes:
    $notes};
  }
  $email_response .= qq{
</pre>
</body></html>
};

  #
  # Send email
  #
  $for_source = "for $source:" if $source;
  $sender = new Mail::Sender{
      smtp => "$smtp_host", 
      from => "$request_from"};
  $sender->MailMsg({to => "$mail_from",
  		    subject => "UPDATE REQUESTED $state_list[$state] $for_source $sub_state_list[$sub_state] ($db_name)",
  		    ctype => "text/html",
		    encoding => "quoted-printable",
		    msg => "$email_response"});
  if ($sender->{error}) {
    $msg = qq{<B><FONT COLOR="A00000"> There was an error sending the following email (}.$sender->{error_msg}.qq{):</font></B>};
  } else {
    $msg = "Update successfully requested";
  }


  close(F);

  @files = `ls -t $msg_dir/db.[0-9]*.*.txt`;
  $file = $files[0];
  chop($file);
  
  open (F, ">>$file") || 
    (print "Failed to open $file: $! $?<Br>" &return);
  print F "Notes:\r\n<hr>An update was requested by $request_from\r\n\r\n";
  close (F);
  open (F, ">>$msg_dir/db.status.$db_name.txt") || 
    (print "Failed to open $msg_dir/db.status.$db_name.txt: $! $?<Br>" &return);
  print F "Notes:\r\n<hr>An update was requested by $request_from\r\n\r\n";
  close (F);
  
  print qq {
	$msg<br><br>
   };

    &PrintDB_REPORT;
}


# Procedure PrintADD_SAB_NOTE ################################################################
#
# Adds a note to a SAB report (creates a new message).
#
sub PrintADD_SAB_NOTE{
  unless ($notes) {
    open (F,"$msg_dir/sab.status.$source.txt") || (print "Couldn't open log file ($source)" && return);
    while (&ReadSingleEntryFromFilehandle(F)) {};
    print qq{    <h2>$source</h2};
    print &GetReportTableHTML;
    print qq{
    <p><center><table width="90%" border="0"><tr><td>
    <form method="POST" action="$ENV{SCRIPT_NAME}">
     <input type="hidden" name="command" value="ADD_SAB_NOTE">
     <input type="hidden" name="source" value="$source">
     <textarea wrap cols="60" rows="10" name="notes"></textarea><br>
     <input type="submit" value="Add Note">
    </form></td></tr></table></center></p>
  }
  } else {
    @files = `ls -t $msg_dir/sab.[0-9]*.*.txt`;
    $file = $files[0]; chop($file);
    open (F,">>$file") || (print "Couldn't open log file ($file)" && return);
    $notes =~ s/(\r?\n)/\r\n    /g;
    print F "Notes:\r\n    <hr>$notes\r\n\r\n";
    close(F);
    open (F,">>$msg_dir/sab.status.$source.txt") || (print "Couldn't open log file ($source)" && return);
    $notes =~ s/(\r?\n)/\r\n    /g;
    print F "Notes:\r\n    <hr>$notes\r\n\r\n";
    close(F);
    print "Note successfully added.<br><br>";
    &PrintSAB_REPORT;
    return;
  }
}


# Procedure PrintREQUEST_SAB_UPDATE ################################################################
#
# Requests an update to latest SAB state
#
sub PrintREQUEST_SAB_UPDATE{
  my($l_request_from, $l_source) = ($request_from, $source);

  open (F, "$msg_dir/sab.status.$source.txt") || next;
  #
  # Obtain information from the file
  #
  while (&ReadSingleEntryFromFilehandle(F)) {};

  #
  # Prepare email response
  #
  #
  # Prepare email response
  #
  $email_response = qq{<html><body>\r
<P>Someone has requested that you update the state of the following work.\r
<a href="https://$ENV{SERVER_NAME}:$ENV{SERVER_PORT}$ENV{SCRIPT_NAME}?command=VIEW_MESSAGE&log_name=db.status.$db_name.txt&update=1">\r
Click here to do so.</a></p>\r
  <pre>
Database:       $db_name
Date:           $msg_date
Time:           $req_time
Backups:        $backups
Cutoff Editing: $cutoff_editing
State:          $state_list[$state]
Sub State:      $sub_state_list[$sub_state]
Source:         $source};
  if ($work) {
    $email_response .= qq{
Work:
    $work};
  }
  if ($notes) {
    $email_response .= qq{
Notes:
    $notes};
  }
  $email_response .= qq{
</pre>
</body></html>
};

  #
  # Send email
  #
  $for_source = "for $source:" if $source;
  $sender = new Mail::Sender{
      smtp => "$smtp_host", 
      from => "$request_from"};
  $sender->MailMsg({to => "$mail_from",
  		    subject => "UPDATE REQUESTED $state_list[$state] $for_source $sub_state_list[$sub_state] ($source)",
  		    ctype => "text/html",
		    encoding => "quoted-printable",
		    msg => "$email_response"});
  if ($sender->{error}) {
    $msg = qq{<B><FONT COLOR="A00000"> There was an error sending the following email (}.$sender->{error_msg}.qq{):</font></B>};
  } else {
    $msg = "Update request successfully sent";
  }
  close(F);

  @files = `ls -t $msg_dir/sab.[0-9]*.*.txt`;
  $file = $files[0];
  chop($file);
  
  open (F, ">>$file") || 
    (print "Failed to open $file: $! $?<Br>" &return);
  print F "Notes:\r\n<hr>An update was requested by $request_from\r\n\r\n";
  close (F);
  open (F, ">>$msg_dir/sab.status.$source.txt") || 
    (print "Failed to open $$msg_dir/sab.status.$source.txt: $! $?<Br>" &return);
  print F "Notes:\r\nAn update was requested by $request_from\r\n\r\n";
  close (F);
  
  
  print qq {
	$msg<br><br>
   };

  &PrintSAB_REPORT;

}

# SCRIPT INFORMATION ########################################################################
#
#  The following procedures prints helpful information about the script
#
#############################################################################################

sub cacheMIDServices {
  @entries = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl`;
  foreach $entry (@entries) {
    chop($entry);
    ($k,$v) = split /\|/, $entry;
    $midsvcs{$k}=$v;
  }
}

sub WriteDBStatus{
  if ($state > 8) { return 0; }
  else { return 1; }
}

sub WriteSABStatus{
  if ($source) { return 1; }
  else { return 0; }
}

sub GetNextState{
  my($l_state, $l_sub_state) = @_;
  my($x) = ($state_transitions{"$l_state,$l_sub_state"});
  ($l_state, $l_sub_state) = split /,/, $x;
  return $l_state; 
}

sub GetNextSubstate{
  my($l_state, $l_sub_state) = @_;
  my($x) = ($state_transitions{"$l_state,$l_sub_state"});
  ($l_state, $l_sub_state) = split /,/, $x;
  return $l_sub_state; 
}

sub GetLogFile {
  my($prefix) = @_;
  
  if (-e "$prefix.txt") {
    while (1) {
      $i++;
      unless (-e "$prefix.$i.txt") {
	return "$prefix.$i.txt";
      }
    }
  } else {
    return "$prefix.txt";
  }
}

sub RemoveOldLogFiles {

  #
  # 31536000 is the number of seconds in 1 year
  #     
  ($d,$d,$d,$mday,$mon,$year) = localtime(time-31536000);
  $year += 1900;
  $mon += 1;
  $mon = "0$mon" if length($mon) == 1;
  $mday = "0$mday" if length($mday) == 1;
  #print "Removing log files on or before $year$mon$mday\n";
  opendir(D,"$msg_dir");
  @files = readdir(D);
  closedir(D);
  foreach $f (@files) {
    if ($f =~ /\.txt$/ && $f le "db.$year$mon$mday.oaaaaaa.txt") {
      #print "Removing $f\n";
      unlink "$msg_dir/$f";
    }
  }

}

#########################################################################3
# Read fields from a single-entry status file handle
#
sub ReadSingleEntryFromFilehandle {
  my($f, $override_null) = @_;
  #
  # Obtain information from the file
  #
  $work_flag = 0; $notes_flag = 0; $details_flag = 0;
  $work = ""; $notes = ""; $details = "";
  $flag = 0;
  while(<$f>) {
    $flag = 1;
    if ($work_flag) { if (/^\r$/) { $work_flag = 0; } else { $work .= $_; } }
    if ($notes_flag) { if (/^\r$/) { $notes_flag = 0; } else { $notes .= $_; } }
    if ($details_flag) { if (/^\r$/) { $details_flag = 0; } else { $details .= $_; } }
    elsif (/^To:(\W*)(.*)\r$/) { $mail_to=$2 if $override_null && $2; }
    elsif (/^Details To:(\W*)(.*)\r$/) { $mail_details_to=$2 if ($override_null && $2); }
    elsif (/^From:(\W*)(.*)\r$/) { $mail_from=$2; }
    elsif (/^Database:(\W*)(.*)\r$/) { $db_name=$2; }
    elsif (/^Date:(\W*)(.*)\r$/) { $msg_date=$2 if ($override_null && $2); }
    elsif (/^Time:(\W*)(.*)\r$/) { $req_time=$2 if ($override_null && $2); }
    elsif (/^Backups:(\W*)(.*)\r$/) { $backups=$2 if ($override_null && $2); }
    elsif (/^Cutoff\sEditing:(\W*)(.*)\r$/) { $cutoff_editing=$2 if ($override_null && $2); }
    elsif (/^State:(\W*)(.*)\r$/) { $state=$2; }
    elsif (/^Sub\sState:(\W*)(.*)\r$/) { $sub_state=$2; }
    elsif (/^Source:(\W*)(.*)\r$/) { $source=$2; }
    elsif (/^Script:(\W*)(.*)\r$/) { $script=$2; } 
    elsif (/^Work:(.*)$/) { $work_flag = 1; $work .= "\r\n" if $work; }
    elsif (/^Notes:(.*)$/) { $notes_flag = 1; $notes .= "\r\n" if $notes; }
    elsif (/^Code\/Details:(.*)$/) { $details_flag = 1; $details .= "\r\n" if $details;}
    elsif (/\-{1,}\s*record\s*break\s*\-{1,}/) {last; }
  }
  $work =~ s/^\s*//; $work =~ s/\n\s*/\n/g;
  $notes =~ s/^\s*//; $notes=~ s/\n\s*/\n/g;
  $details =~ s/^\s*//; $details =~ s/\n\s*/\n/g;

  return $flag;
}

#########################################################################3
# Prints report table.  Assumes the variables are set
#
sub GetReportTableHTML {
  my($txt);

  #
  # Convert line termination to <BR> tags
  #
  $br_work = "$work";
  $br_work =~ s/(\r?\n)/$1<br>/g;
  $br_notes = "$notes";
  $br_notes =~ s/(\r?\n)/$1<br>/g;
  $br_details = "$details";
  $br_details =~ s/(\r?\n)/$1<br>/g;


  #
  # Print information from the file
  #
  $txt = qq{
    <center>
    <table width="90%" border=1>
      <tr bgcolor="#ffffcc"><td width="30%"><b>Database:</b></td><td><b>$db_name</b></td></tr>
};
  $txt .= qq{      <tr><td><b>Source:</b></td><td>$source</td></tr>} if $source;
  $txt .= qq{
      <tr><td><b>Authority:</b></td><td><a href="mailto:$mail_from">$mail_from</A></td></tr>
      <tr><td><b>Date:</b></td><td>$msg_date</td></tr>
      <tr><td><b>State:</b></td><td>$state_list[$state]</td></tr>
      <tr><td><b>Sub State:</b></td><td>$sub_state_list[$sub_state]</td></tr>
};
  $lc_source = lc($source);
  $txt .= qq{      <tr><td><b>Time:</b></td><td>$req_time</td></tr>} if $req_time;
  $txt .= qq{      <tr><td><b>Backups:</b></td><td>$backups</td></tr>} if $backups;
  $txt .= qq{      <tr><td><b>Cutoff Editing:</b></td><td>$cutoff_editing</td></tr>} if $cutoff_editing;
  $txt .= qq{      <tr><td valign="top"><b>Work:</b></td><td>$br_work</td></tr>} if $work;
  $txt .= qq{      <tr><td valign="top"><b>Notes:</b></td><td>$br_notes</td></tr>} if $notes;
  $txt .= qq{      <tr><td><b>Script:</b></td><td>$script</td></tr>} if $script;
  $txt .= qq{      <tr><td valign="top"><b>Code/Details:</b></td><td>$br_details</td></tr>} if $details;
  $txt .= qq{      <tr><td valign="top"><b>Test Report:</b></td><td><a href="/Sources/TEST.$lc_source.html">/Sources/TEST.$lc_source.html</a></td></tr>} if $state == 1 && $sub_state == 3;
  $txt .= qq{      <tr><td valign="top"><b>Insertion Report:</b></td><td><a href="/Sources/INSERTION.$lc_source.html">/Sources/INSERTION.$lc_source.html</a></td></tr>} if $state == 3 && $sub_state == 3;
  $txt .= qq{
    </table>
    </center>
    };
  return $txt;
}

# Procedure GetReportTableButtonsHTML #####################################################3
#
# Get HTML for the "add note" and "request update" buttons
#
sub GetReportTableButtonsHTML {
   my($mode) = @_;

   #
   #
   #
   my($lc_mode) = lc("$mode");
   
   @files = `ls -t $msg_dir/$lc_mode.[0-9]*.$source.txt $msg_dir/$lc_mode.[0-9]*.$source.[0-9]*.txt $msg_dir/$lc_mode.[0-9]*.$db_name.txt $msg_dir/$lc_mode.[0-9]*.$db_name.[0-9]*.txt`;
   $file = $files[0];
   chop ($file);
   $file =~ s/$msg_dir\///;

   return qq{
    <center>
    <table width="80%" border=0>
      <tr><td align="center" valign="top"><font size=+1>
        <form method="GET" action="$ENV{SCRIPT_NAME}">
	   <script>
	     function submitRequest(form) {
	       var re = new RegExp(".*\@.*");
	       while (true) {
		 var value = prompt("Enter email address","user\@domain");
		 if (value == null) { return false; }				      
		 if (!re.test(value)) {
		   alert("You must enter a valid email address.");
		 } else {
		   break;
		 }
	       }
	       form.request_from.value = value;
	       form.submit();
	       return true;
	     }
	   </script>
	   <input type="hidden" name="command" value="REQUEST_${mode}_UPDATE">
	   <input type="hidden" name="db_name" value="$db_name">
	   <input type="hidden" name="source" value="$source">
	   <input type="hidden" name="request_from" value="">
           <input type="button" value="Request an Update"
             onClick="submitRequest(this.form);"
             onMouseOver="window.status='Request Update'; return true;"
             onMouseOut="window.status=''; return true;">
	</form>
        </font></td>
      <td align="center" valign="top"><font size=+1>
        <form method="GET" action="$ENV{SCRIPT_NAME}">
	   <input type="hidden" name="command" value="VIEW_MESSAGE">
	   <input type="hidden" name="log_name" value="$file">
	   <input type="hidden" name="index" value="$mode">
	   <input type="hidden" name="update" value="1">
           <input type="button" value="Submit an Update"
             onClick="form.submit(); return true;"
             onMouseOver="window.status='Update Status'; return true;"
             onMouseOut="window.status=''; return true;">
	</form>
        </font></td>
      <td align="center" valign="top"><font size=+1>
        <form method="GET" action="$ENV{SCRIPT_NAME}">
	   <input type="hidden" name="command" value="ADD_${mode}_NOTE">
	   <input type="hidden" name="db_name" value="$db_name">
	   <input type="hidden" name="source" value="$source">
           <input type="button" value="Add Note"
             onClick="form.submit(); return true;"
             onMouseOver="window.status='Add $mode Note'; return true;"
             onMouseOut="window.status=''; return true;">
	</form>
        </font></td>
      </tr>
    </table>
    </center>
};
}

# Procedure PrintVersion ####################################################################
#
# This procedure prints the version information
#
sub PrintVersion {
    my($type) = @_;
    print "Release $release: version $version, $version_date ($version_authority).\n" 
        if $type eq "version";
    print "$version\n" if $type eq "v";
    return 1;
}

# Procedure PrintHelp #######################################################################
#
# This procedure prints the help information
#
sub PrintHelp {
    print qq{
 This script is a CGI script that generates a form used to request database from NLM.
 It takes CGI arguments in the standard form "key=value&key=value...". 

 Paramaters:

  command  :  The command for the script to use.  Valid values are:
              submit (Generates database request form)
              "" (The empty value just reloads the form)
  mail_to  :  Mail to list of recipients
  mail_from:  Sender
  db_name  :  Database name
  msg_date :  Message date
  req_time :  Request time
  backups  :  "on" if enabled, "off" if disabled 
  cutoff_editing  :  "Yes" or "No"
  state    :  State
  sub state :  Sub state
  source   :  Source
  work     :  Work to be performed

  Version: $version, $version_date ($version_authority)

};
}

