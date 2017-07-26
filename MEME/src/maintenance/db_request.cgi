#!@PATH_TO_PERL@
#
# File:     db_request.cgi
# Author:   Bobby Edrosa
# 
# Dependencies:  This file requires the following:
#      $MEME_HOME/utils/meme_utils.pl
#
# Description: 
#
# Changes:
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
$version_date = "11/24/2003";

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
       	"command" => 1,
       	"page" => 1,
	  	"mail_to" => 1,
		"mail_from" => 1,
        "db_name" => 1,
		"req_date" => 1,
        "req_time" => 1, 
        "backups" => 1,
        "cutoff_editing" => 1,
        "work" => 1,
        "script" => 1,
        "subject" => 1,
        "details" => 1,
		"apelon_mailto" => 1,
		"log_name" => 1,
		"search" => 1
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

#
# Configure Mail Settings
#
unless ($smtp_host) {
$smtp_host = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s smtp-host`;
chop($smtp_host); }
unless ($mail_from) {
$mail_from = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s db-request-from`;
chop($mail_from); }
unless ($mail_to) {
$mail_to = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s db-request-list`;
chop($mail_to); }
unless ($apelon_mailto) {
$apelon_mailto = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s db-response-list`;
chop($apelon_mailto); }

#
# Default Settings for CGI parameters
#
$db_name = &meme_utils::midsvcs("editing-db") unless $db_name;
$subject = "Database Request for <edit this>" unless $subject;
$subject = "$subject ($db_name)" if $subject && $command eq "SUBMIT_REQUEST";
$req_date = `/bin/date +%m/%d/%Y` unless $req_date;
@time=local_time;
$req_time = "6:30 EDT" if (! $req_time && $time[8]);
$req_time = "6:30 EST" if (! $req_time && ! $time[8]);
$backups = "Enabled" unless $backups;
$cutoff_editing = "No" unless $cutoff_editing;
$work = "" unless $work;
$command = "" unless $command;
$request_dir = "DBRequests";
$response_dir = "DBResponses";
$date_Ymd = `/bin/date +%Y%m%d`;
$date_Ymd =~ s/\n//;

#
# The commands array maps commands to procedures for
# printing the header, body, and footer
#
%commands = (
	     "REQUEST_LOGS" =>
	         ["PrintRequestHeader","PrintREQUEST_LOGS","PrintFooter"],
	     "REQUEST_LOG" =>
	         ["PrintResponseHeader","PrintREQUEST_LOG","PrintFooter"],
	     "SUBMIT_REQUEST" => 
	         ["PrintRequestHeader","PrintSUBMIT_REQUEST","PrintFooter"],
	     "RESPONSE_LOGS" =>
	         ["PrintResponseHeader","PrintRESPONSE_LOGS","PrintFooter"],
	     "RESPONSE_LOG" =>
	         ["PrintResponseHeader","PrintRESPONSE_LOG","PrintFooter"],
	     "SUBMIT_RESPONSE" => 
	         ["PrintResponseHeader","PrintSUBMIT_RESPONSE","PrintFooter"],
	     "" => 
	         ["PrintRequestHeader","PrintForm","PrintFooter"]
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
    
  &$header;
  &$body;
  &$footer;
}

exit(0);


# HTML ######################################################################################
#
#  The following procedures generate HTML document
#
#############################################################################################

# Procedure PrintRequestHeader ##############################################################
#
# This procedure prints the request (html) header
#
sub PrintRequestHeader {

    $title="Manage Database Requests and Responses";
    if ($command eq "REQUEST_LOGS") { $title="Select Database Request"; }
    elsif ($command eq "REQUEST_LOG") { $title="Send Database Response"; }
    elsif ($command eq "SUBMIT_REQUEST") { $title="Database Request Sent"; } 
    $fheader = "<center><h2>$title</h2></center>";
    &meme_utils::PrintSimpleHeader($title, "","",$fheader)

}

# Procedure PrintResponseHeader #############################################################
#
# This procedure prints the response (html) header
#
sub PrintResponseHeader {

    $title="Send Database Response";
    if ($command eq "RESPONSE_LOGS") { $title="Select Database Response"; }
    elsif ($command eq "RESPONSE_LOG") { $title="Update Database Response"; }
    elsif ($command eq "SUBMIT_RESPONSE" ){ $title="Database Response Sent"; }
    $fheader = "<center><h2>$title</h2></center>";
    &meme_utils::PrintSimpleHeader($title,"","", $fheader)

}

# Procedure PrintForm ###################################################
#
# This procedure displays the request form.
#
sub PrintForm {

    $selected{$db_name}=" SELECTED";
    $selected{$backups}=" SELECTED";
    $selected{$cutoff_editing}=" SELECTED";

    $dbs = &meme_utils::midsvcs("databases");
    $mrd = &meme_utils::midsvcs("mrd-db");
    $databases{$mrd} = $mrd;
    foreach $db (sort split /,/, $dbs) {
      $databases{$db} = $db;
    }

    print qq{
    <blockquote>
      <p>Please review the <a href="/database_work_instructions.html">
	  database request guidelines</a> before submitting a request.</p>
      <p>Select one of the following actions:</p>
      <ol>
        <li>To browse past database requests and send a response, <a href="$ENV{SCRIPT_NAME}?command=REQUEST_LOGS">
	    click here</a>.</li>
	<li>Search for past database requests: <form method="GET" action="$ENV{SCRIPT_NAME}"><input type="hidden" name="command" value="REQUEST_LOGS"><input type="text" name="search"><input type="submit"></form></li>
	<li>To browse past database responses and send an update, <a href="$ENV{SCRIPT_NAME}?command=RESPONSE_LOGS">
	    click here</a>.</li>
	<li>Search for past database responses: <form method="GET" action="$ENV{SCRIPT_NAME}"><input type="hidden" name="command" value="RESPONSE_LOGS"><input type="text" name="search"><input type="submit"></form></li>	
	<li>Send a database request:<br>&nbsp;<br>
	 
          <form method="GET" action="$ENV{SCRIPT_NAME}"> 
	    <input type="hidden" name="command" value="SUBMIT_REQUEST">
	    <center>
              <table width="90%" cellspacing="2" cellpadding="2">
                <tr>
                  <td align="left"><font size=+1>To:</font></td>
                  <td align="left"><font size=+1>
	             <INPUT TYPE="text" name="mail_to" VALUE="$mail_to" size=60></font></td>
                </tr>
                <tr>
                  <td align="left"><font size=+1>From:</font></td>
                  <td align="left"><font size=+1>
	             <INPUT TYPE="text" name="mail_from" VALUE="$mail_from" size=60></font></td>
                </tr>
                <tr>
                  <td align="left"><font size=+1>Subject:</font></td>
                  <td align="left"><font size=+1>
	             <INPUT TYPE="text" name="subject" VALUE="$subject" size=60></font></td>
                </tr>
                <tr>
                  <td align="left"><font size=+1>Database:</font></td>
                  <td align="left"><font size=+1>
	            <select name="db_name">
        };

    foreach $db (sort keys %databases) {
        print qq{                      <option value="$databases{$db}"$selected{$databases{$db}}>$db</option>
        };
    }

    print qq{
                    </select></font>
                  </td>
                </tr>
                <tr>
                  <td align="left"><font size=+1>Date:</font></td>
                  <td align="left"><font size=+1>
	            <input type="text" name="req_date" value="$req_date" size=10></font></td>
                </tr>
                <tr>
                  <td align="left"><font size=+1>Time:</font></td>
                  <td align="left"><font size=+1>
	            <input type="text" name="req_time" value="$req_time" size=10></font></td>
                </tr>
                <tr>
                  <td align="left"><font size=+1>Backups:</font></td>
                  <td align="left"><font size=+1>
	            <select name="backups">
                      <option>Enabled</option>
                      <option>Disabled</option>
	            </select></font>
                  </td>
                </tr>
                <tr>
                  <td align="left"><font size=+1>Cutoff Editing:</font></td>
                  <td align="left"><font size=+1>
	            <select name="cutoff_editing">
	              <option $selected{"Yes"}>Yes</option>
                      <option $selected{"No"}>No</option>
	            </select></font>
	          </td>
	        </tr>
	        <tr>
	          <td align="left" VALIGN="top"><font size=+1>Work:</font></td>
	          <td align="left"><font size=+1>
	            <textarea wrap=soft cols=60 rows=10 name="work">$work</textarea></font>
	          </td>
	        </tr>
	        <tr>
	          <td colspan="2" align="center"><font size=+1><br>
	            <input type="button" value="Send Database Request"
	               onClick="submitForm(this.form);"
	               onMouseOver="window.status='Send Database Request'; return true;"
                       onMouseOut="window.status=''; return true;">
         <script language = "JavaScript">
            function isDigit(c) {
                return (c >= 0 || c <= 9);
            }
            function submitForm(form) {
                //check_date
                var str = form.req_date.value;
                var error_msg = "The date must have the form mm/dd/yyyy.";
                if (str.length != 10) {
                    alert(error_msg); return false;
                }
                for (var i=0; i<10; i++) {
                    var c = str.substring(i,i+1);
                    if (((i==2||i==5) && c != "/") ||
                        ((i==0||i==1||i==3||i==4||i==6||i==7||i==8||i==9) && !isDigit(c))) {
                        alert(error_msg); return false;
                    }
                }
                form.submit();
                return true;
            }
        </script>
                  </font></td>
                </tr>
              </table>
            </center>
          </form>
        </li>
       </ol>
    </blockquote>
};

}

# Procedure PrintFooter #####################################################################
#
# This procedure prints the (html) footer
#
sub PrintFooter {

    print qq{
	<P>
	<HR WIDTH="100%">
        <TABLE BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
        <TR NOSAVE>
        <td align=LEFT VALIGN=TOP NOSAVE>
        <ADDRESS>
        <A HREF="$ENV{SCRIPT_NAME}">Back to Index</A></ADDRESS>
        </td>

        <td align=RIGHT VALIGN=TOP NOSAVE>
        <ADDRESS>
	  <font size=-1>Contact: <A HREF="mailto:carlsen\@apelon.com">Brian A. Carlsen</A><br>},scalar(localtime),qq{<br>};
        &PrintVersion("version");
        print qq{</font></ADDRESS>

        </td>
        </tr>
        </TABLE>

</BODY>
</HTML>
}

}

# Procedure PrintSUBMIT_REQUEST ####################################################################
#
# This procedure displays request status page, generates a request log file and sends mail.
#
sub PrintSUBMIT_REQUEST {

  #
  # Align work info
  #
  $work =~ s/(\r?\n)/\r\n    /g;

  #
  # Prepare the email response
  #
  $email_response = qq{\r
Database:       $db_name\r
Date:           $req_date\r
Time:           $req_time\r
Backups:        $backups\r
Cutoff Editing: $cutoff_editing\r
Work:\r
    $work
\r
};

  #
  # Prepare the log file
  #
  $log_response = qq{\r
To:             $mail_to\r
From:           $mail_from\r
Subject:        $subject\r
Database:       $db_name\r
Date:           $req_date\r
Time:           $req_time\r
Backups:        $backups\r
Cutoff Editing: $cutoff_editing\r
Work:\r
    $work
\r
};

  #
  # Save email log response
  #    
  $log_file = &GetLogFile("$request_dir/$date_Ymd.$db_name");
  open (IO_RFILE, ">$log_file") || die "Failed to create $log_file: ($! $?).";
  print IO_RFILE "$log_response";
  close (IO_RFILE);

  #
  # Prepare and send email response
  #
  use Mail::Sender;
  $sender = new Mail::Sender{
      smtp => "$smtp_host", 
      from => "$mail_from"};
  $sender->MailMsg({to => "$mail_to", cc => 'meme@apelon.com',
		    subject => "$subject",
		    msg => "$email_response"});
  if ($sender->{error}) {
    $msg = qq{<B><FONT COLOR="A00000"> There was an error sending the following request (}.$sender->{error_msg}.qq{):</font></B>};
  } else {
    $msg = "The following request was successfully sent:";
  }

  #
  # Print the email response to the screen
  # 
  $response = qq {
        $msg
        <blockquote>
        <pre>$log_response</pre>
        </blockquote>
  };
  print $response;
}

# Procedure PrintSUBMIT_RESPONSE ###################################################################
#
# This procedure displays response status page, generates a response log file and sends mail.
#
sub PrintSUBMIT_RESPONSE {

  #
  # Align work and code info
  #
  $work =~ s/(\r?\n)/\r\n    /g;
  $details =~ s/(\r?\n)/\r\n    /g;

  #
  # Prepare email response
  #
  $email_response = qq{\r
----------------------------------\r
 The following work was performed\r
----------------------------------\r
\r
Database:       $db_name\r
Date:           $req_date\r
Time:           $req_time\r
Backups:        $backups\r
Cutoff Editing: $cutoff_editing\r
Work:\r
    $work
\r
};

  #
  # Prepare apelon email respones
  #
  $apelon_response = qq{\r
----------------------------------\r
 The following work was performed\r
----------------------------------\r
\r
Database:       $db_name\r
Date:           $req_date\r
Time:           $req_time\r
Backups:        $backups\r
Cutoff Editing: $cutoff_editing\r
Work:\r
    $work
\r
Script:         $script\r
\r
Code/Details:\r
    $details
\r
};

  #
  # Prepare log file
  #
  $log_response = qq{\r
To:             $mail_to\r
From:           $mail_from\r
Subject:        $subject\r
Apelon To:      $apelon_mailto\r
Database:       $db_name\r
Date:           $req_date\r
Time:           $req_time\r
Backups:        $backups\r
Cutoff Editing: $cutoff_editing\r
Work:\r
    $work
\r
Script:         $script\r
\r
Code/Details:\r
    $details
\r
};

  #
  # Send email response
  #
  use Mail::Sender;
  $sender = new Mail::Sender{
      smtp => "$smtp_host", 
      from => "$mail_from"};
  $sender->MailMsg({to => "$mail_to", cc => 'meme@apelon.com',
		    subject => "$subject",
		    msg => "$email_response"});
  if ($sender->{error}) {
    $msg = qq{<B><FONT COLOR="A00000"> There was an error sending the following response (}.$sender->{error_msg}.qq{):</font></B>};
  } else {
    $msg = "The following work was successfully completed:";
  }

  #
  # Send apelon response
  #
  use Mail::Sender;
  $sender = new Mail::Sender{
      smtp => "$smtp_host", 
      from => "$mail_from"};
  $sender->MailMsg({to => "$apelon_mailto", 
		    subject => "$subject",
		    msg => "$apelon_response"});
  if ($sender->{error}) {
    $msg = qq{<B><FONT COLOR="A00000"> There was an error sending the following response (}.$sender->{error_msg}.qq{):</font></B>};
  } else {
    $msg = "The following work was successfully completed:";
  }
  
  #
  # Save log response
  #
  unlink "/tmp/response.$$.txt";
  $log_file = &GetLogFile("$response_dir/$date_Ymd.$db_name");
  open (IO_RFILE, ">$log_file") || die "Failed to create $log_file: ($! $?).";
  print IO_RFILE "$log_response";
  close (IO_RFILE);




  $response = qq {
	$msg
        <blockquote>
        <pre>$log_response</pre>
        </blockquote>
    };

    print $response;
}

# Procedure PrintREQUEST_LOG ###############################################
#
# This procedure displays a response form
# from a previously selected db request. 
#
sub PrintREQUEST_LOG {

  #
  # Open the request log
  #
  open(F, "$request_dir/$log_name") || die "couldn't open file: $log_name\n";

  #
  # Obtain information from the request log
  #
  while(<F>) {
    if ($work_flag) { if (/^\r$/) { $work_flag = 0; last;} else { $work .= $_; } }
    elsif (/^To:(\W*)(.*)$/) { $mail_to=$2; } 
    elsif (/^From:(\W*)(.*)$/) { $mail_from=$2; } 
    elsif (/^Subject:(\W*)(.*)$/) { $subject="Status of $2"; } 
    elsif (/^Database:(\W*)(.*)$/) { $database=$2; }
    elsif (/^Date:(\W*)(.*)$/) { $req_date=$2; }
    elsif (/^Time:(\W*)(.*)$/) { $req_time=$2; }
    elsif (/^Backups:(\W*)(.*)$/) { $backups=$2; }
    elsif (/^Cutoff\sEditing:(\W*)(.*)$/) { $cutoff=$2; }
    elsif (/^Work:(.*)$/) { $work_flag = 1; }
  }
  close(F);
  
  $work =~ s/\n    /\n/g;
  $work =~ s/^    //;
  $work =~ s/(\s*)$//;

  #
  # Print the response form
  #
  print qq{
    <p>   
    Make sure you complete the
    <a href="/MEME/Documentation/db_work_checklist.html">
	   Database Work Checklist</a>
    before sending a database response.
    </p>
    <form method="GET" action="$ENV{SCRIPT_NAME}"> 
      <input type="hidden" name="command" value="SUBMIT_RESPONSE">
      <center>
      <table width="90%" cellspacing="2" cellpadding="2">
        <tr>
          <td align="left"><font size=+1>To:</font></td>
          <td align="left"><font size=+1>
	    <input type="text" name="mail_to" VALUE="$mail_to" size=60></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Apelon To:</font></td>
          <td align="left"><font size=+1>
	    <input type="text" name="apelon_mailto" VALUE="$apelon_mailto" size=60></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>From:</font></td>
          <td align="left"><font size=+1>
	    <input type="text" name="mail_from" VALUE="$mail_from" size=60></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Subject:</font></td>
          <td align="left"><font size=+1>
           <INPUT TYPE="text" name="subject" VALUE="$subject" size=60></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Database:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="db_name" VALUE="$database" size=60></font></td>
        </tr>
        <tr>
        <td align="left"><font size=+1>Date:</font></td>
        <td align="left"><font size=+1>
	   <input type="text" name="req_date" VALUE="$req_date" size=10></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Time:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="req_time" VALUE="$req_time" size=10></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Backups:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="backups" VALUE="$backups" size=10></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Cutoff Editing:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="cutoff" VALUE="$cutoff" size=10></font></td>
        </tr>
        <tr>
          <td align="left" VALIGN="top"><font size=+1>Work:</font></td>
          <td align="left"><font size=+1>
	   <textarea wrap cols=60 rows=10 name="work">$work</textarea></font></td>
        </tr>
        <tr>
          <td align="left" VALIGN="top"><font size=+1>Script:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="script" value="$script" size=60></font></td>
        </tr>
        <tr>
          <td align="left" VALIGN="top"><font size=+1>Code/Details:</font></td>
          <td align="left"><font size=+1>
	   <textarea wrap cols=60 rows=10 name="details">$details</textarea></font></td>
        </tr>
        <tr>
          <td colspan="2" align="center"> <font size=+1>
	   <input type="button" value="Send Database Response"
	     onClick="submitForm(this.form);"
	     onMouseOver="window.status='Send Database Response'; return true;"
	     onMouseOut="window.status=''; return true;">
          <script language = "JavaScript">
            function isDigit(c) {
                return (c >= 0 || c <= 9);
            }
            function submitForm(form) {
                //check_date
                var str = form.req_date.value;
                var error_msg = "The date must have the form mm/dd/yyyy.";
                if (str.length != 10) {
                    alert(error_msg); return false;
                }
                for (var i=0; i<10; i++) {
                    var c = str.substring(i,i+1);
                    if (((i==2||i==5) && c != "/") ||
                        ((i==0||i==1||i==3||i==4||i==6||i==7||i==8||i==9) && !isDigit(c))) {
                        alert(error_msg); return false;
                    }
                }
                form.submit();
                return true;
            }
          </script>
        </font></td>
      </tr>
    </table>
    </center>
  </form>
  };
}

# Procedure PrintREQUEST_LOGS ###############################################################
#
# This procedure displays a selection of a list of database requests. 
#
sub PrintREQUEST_LOGS {

  $lpp = 25;
  
  if ($page > 0) {
    $prev = $page - 1;
    $previous = qq{<p>[<a href="$ENV{SCRIPT_NAME}?command=REQUEST_LOGS&page=$prev&search=$search">Previous Page</a>]</p>};
  }

  print qq{
  <p>Select one of the following database requests:</p>
  $previous
  <blockquote>
};
  #
  # Read logs
  #
  chdir $request_dir;
  if ($search) {
    foreach $word (split / /, $search) {
      unless ($f) {
	@files = `/bin/grep -i -l $word *.html *.txt`;
	$f = 1;
      } elsif (scalar(@files)>0) {
	chomp(@files);
	$files = join " ", @files;
	@files = `/bin/grep -i -l $word $files`;
      }
    }
    @logs = reverse sort @files;
  } else {
    @logs = `ls -ta *.html *.txt`;
  }

  if (scalar(@logs)>0) {  
    $j = ($page*$lpp)+$lpp;
    for ($i=$page*$lpp; $i < $j; $i++)  {
      $log = $logs[$i];
      last unless $log;
      chomp($log);
      print qq{
    <a href="$ENV{SCRIPT_NAME}?command=REQUEST_LOG&log_name=$log"><tt>$log</tt></a><br>};
    }
  } else {
    print qq{<b>No matching logs found</b>.};
  }


  print qq{
  </blockquote>
};
  #
  # Determine if there are more pages
  #
  $next = $page + 1;
  if (scalar(@logs)>=($next*$lpp)) {
      print qq{
      <p>[<a href="$ENV{SCRIPT_NAME}?command=REQUEST_LOGS&page=$next&search=$search">Next Page</a>]</p>};
    }

}; # end PrintREQUEST_LOGS

# Procedure PrintRESPONSE_LOG ###############################################################
#
# This procedure displays a log from a previously selected db response. 
#
sub PrintRESPONSE_LOG {
  #
  # Open the request log
  #
  open(F, "$response_dir/$log_name") || die "couldn't open file: $log_name\n";

  #
  # Obtain information from the request log
  #
  while(<F>) {
    if ($details_flag) { if (/^\r$/) { $details_flag = 0;} else { $details .= $_; } }
    elsif ($work_flag) { if (/^\r$/) { $work_flag = 0;} else { $work .= $_; } }
    elsif (/^To:(\W*)(.*)$/) { $mail_to=$2; } 
    elsif (/^Apelon To:(\W*)(.*)$/) { $apelon_mailto=$2; } 
    elsif (/^From:(\W*)(.*)$/) { $mail_from=$2; } 
    elsif (/^Subject:(\W*)(.*)$/) { $subject=$2; } 
    elsif (/^Script:(\W*)(.*)$/) { $script=$2; } 
    elsif (/^Database:(\W*)(.*)$/) { $database=$2; }
    elsif (/^Date:(\W*)(.*)$/) { $req_date=$2; }
    elsif (/^Time:(\W*)(.*)$/) { $req_time=$2; }
    elsif (/^Backups:(\W*)(.*)$/) { $backups=$2; }
    elsif (/^Cutoff\sEditing:(\W*)(.*)$/) { $cutoff=$2; }
    elsif (/^Code\/Details:.*$/) { $details_flag = 1; $work_flag = 0;}
    elsif (/^Work:.*$/) { $work_flag = 1; }
  }
  close(F);

  $details =~ s/\n    /\n/g;
  $details =~ s/^    //;
  $details =~ s/(\s*)$//;
  $work =~ s/\n    /\n/g;
  $work =~ s/^    //;
  $work =~ s/(\s*)$//;

  #
  # Print the response form
  #
  print qq{
    <form method="GET" action="$ENV{SCRIPT_NAME}"> 
      <input type="hidden" name="command" value="SUBMIT_RESPONSE">
      <center>
      <table width="90%" cellspacing="2" cellpadding="2">
        <tr>
          <td align="left"><font size=+1>To:</font></td>
          <td align="left"><font size=+1>
	    <input type="text" name="mail_to" VALUE="$mail_to" size=60></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Apelon To:</font></td>
          <td align="left"><font size=+1>
	    <input type="text" name="apelon_mailto" VALUE="$apelon_mailto" size=60></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>From:</font></td>
          <td align="left"><font size=+1>
	    <input type="text" name="mail_from" VALUE="$mail_from" size=60></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Subject:</font></td>
          <td align="left"><font size=+1>
            <INPUT TYPE="text" name="subject" VALUE="$subject" size=60></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Database:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="db_name" VALUE="$database" size=60></font></td>
        </tr>
        <tr>
        <td align="left"><font size=+1>Date:</font></td>
        <td align="left"><font size=+1>
	   <input type="text" name="req_date" VALUE="$req_date" size=10></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Time:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="req_time" VALUE="$req_time" size=10></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Backups:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="backups" VALUE="$backups" size=10></font></td>
        </tr>
        <tr>
          <td align="left"><font size=+1>Cutoff Editing:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="cutoff" VALUE="$cutoff" size=10></font></td>
        </tr>
        <tr>
          <td align="left" VALIGN="top"><font size=+1>Work:</font></td>
          <td align="left"><font size=+1>
	   <textarea wrap cols=60 rows=10 name="work">$work</textarea></font></td>
        </tr>
        <tr>
          <td align="left" VALIGN="top"><font size=+1>Script:</font></td>
          <td align="left"><font size=+1>
	   <input type="text" name="script" value="$script" size=60></font></td>
        </tr>
        <tr>
          <td align="left" VALIGN="top"><font size=+1>Code/Details:</font></td>
          <td align="left"><font size=+1>
	   <textarea wrap cols=60 rows=10 name="details">$details</textarea></font></td>
        </tr>
        <tr>
          <td colspan="2" align="center"> <font size=+1>
	   <input type="button" value="Update Database Response"
	     onClick="submitForm(this.form);"
	     onMouseOver="window.status='Update Database Response'; return true;"
	     onMouseOut="window.status=''; return true;">
          <script language = "JavaScript">
            function isDigit(c) {
                return (c >= 0 || c <= 9);
            }
            function submitForm(form) {
                //check_date
                var str = form.req_date.value;
                var error_msg = "The date must have the form mm/dd/yyyy.";
                if (str.length != 10) {
                    alert(error_msg); return false;
                }
                for (var i=0; i<10; i++) {
                    var c = str.substring(i,i+1);
                    if (((i==2||i==5) && c != "/") ||
                        ((i==0||i==1||i==3||i==4||i==6||i==7||i==8||i==9) && !isDigit(c))) {
                        alert(error_msg); return false;
                    }
                }
                form.submit();
                return true;
            }
          </script>
        </font></td>
      </tr>
    </table>
    </center>
  </form>
  };
}

# Procedure PrintRESPONSE_LOGS ##############################################################
#
# This procedure displays a selection of a list of database responses. 
#
sub PrintRESPONSE_LOGS {

  $lpp = 25;
  
  if ($page > 0) {
    $prev = $page - 1;
    $previous = qq{<p>[<a href="$ENV{SCRIPT_NAME}?command=RESPONSE_LOGS&page=$prev&search=$search">Previous Page</a>]</p>};
  }

  print qq{
  <p>Select one of the following database responses:</p>
  $previous
  <blockquote>
};
  #
  # Read logs
  #
  chdir $response_dir;
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
    @logs = `ls -ta *.html *.txt`;
  }

  if (scalar(@logs)>0) {  
    $j = ($page*$lpp)+$lpp;
    for ($i=$page*$lpp; $i < $j; $i++)  {
      $log = $logs[$i];
      last unless $log;
      chomp($log);
      print qq{
    <a href="$ENV{SCRIPT_NAME}?command=RESPONSE_LOG&log_name=$log"><tt>$log</tt></a><br>};
    }
  } else {
    print qq{<b>No matching logs found</b>.};
  }

  print qq{
  </blockquote>
};
  #
  # Determine if there are more pages
  #
  $next = $page + 1;
  if (scalar(@logs)>=($next*$lpp)) {
      print qq{
      <p>[<a href="$ENV{SCRIPT_NAME}?command=RESPONSE_LOGS&page=$next&search=$search">Next Page</a>]</p>};
    }

}; # end PrintRESPONSE_LOGS

# SCRIPT INFORMATION ########################################################################
#
#  The following procedures prints helpful information about the script
#
#############################################################################################

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
  opendir(D,"$request_dir");
  @files = readdir(D);
  closedir(D);
  foreach $f (@files) {
    if ($f =~ /\.txt$/ && $f le "$year$mon$mday.oaaaaaa.txt") {
      #print "Removing $f\n";
      unlink "$request_dir/$f";
    }
  }
  opendir(D,"$response_dir");
  @files = readdir(D);
  closedir(D);
  foreach $f (@files) {
    if ($f =~ /\.txt$/ && $f le "$year$mon$mday.oaaaaaa.txt") {
      unlink "$request_dir/$f";
    }
  }

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
  subject  :  Subject
  db_name  :  Database name
  req_date :  Request date
  req_time :  Request time
  backups  :  "on" if enabled, "off" if disabled 
  cutoff_editing  :  "Yes" or "No"
  work     :  Work to be performed

  Version: $version, $version_date ($version_authority)

};
}
