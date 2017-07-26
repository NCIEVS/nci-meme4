#!@PATH_TO_PERL@
#
# File:     track_errors.cgi
# Author:   Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      $MEME_HOME/utils/meme_utils.pl
#
# Description:
#
# Changes:
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
#
# Version info:
# 11/25/2003 3.0.5: First version
#
$release = "4";
$version = "0.5";
$version_authority = "BAC";
$version_date = "11/25/2003";

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
                  "log_name" => 1,
                  "mail_from" => 1,
                  "subject" => 1,
                  "original_email" => 1,
                  "db_response_link" => 1,
                  "keywords" => 1,
                  "research_details" => 1,
                  "fix_details" => 1,
                  "script" => 1,
		  "search" => 1,
		  "search_field" => 1
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
# Default Settings for CGI parameters
#
$mail_to = "nlmumls-l\@list.nih.gov" unless $mail_to;
$apelon_mailto = "tkao\@msdinc.com" unless $apelon_mailto;
$db_name = &meme_utils::midsvcs("editing-db") unless $db_name;
$req_date = `/bin/date +%m/%d/%Y` unless $req_date;
@time=local_time;
$req_time = "6:30 EDT" if (! $req_time && $time[8]);
$req_time = "6:30 EST" if (! $req_time && ! $time[8]);
$backups = "Enabled" unless $backups;
$cutoff_editing = "No" unless $cutoff_editing;
$work = "" unless $work;
$command = "" unless $command;
$problems_dir = "DBProblems";
$date_Ymd = `/bin/date +%Y%m%d`;
$date_Ymd =~ s/\n//;

#
# The commands array maps commands to procedures for
# printing the header, body, and footer
#
%commands = (
	     "PROBLEM_LOGS" =>
	         ["PrintProblemHeader","PrintPROBLEM_LOGS","PrintFooter"],
	     "PROBLEM_LOG" =>
	         ["PrintProblemHeader","PrintPROBLEM_LOG","PrintFooter"],
	     "VIEW_PROBLEM_LOG" =>
	         ["PrintProblemHeader","PrintVIEW_PROBLEM_LOG","PrintFooter"],
	     "SUBMIT_PROBLEM" => 
	         ["PrintProblemHeader","PrintSUBMIT_PROBLEM","PrintFooter"],
	     "" => 
	         ["PrintProblemHeader","PrintIndex","PrintFooter"]
             );

#
# Check to see if command exists
#
if ($commands{$command}) {

  #
  # Don't remove old logs
  #
  #&RemoveOldLogFiles;

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


# HTML #####################################################################
#
#  The following procedures generate HTML document
#
#############################################################################

# Procedure PrintProblemHeader ##############################################
#
# This procedure prints the request (html) header
#
sub PrintProblemHeader {

    $title="Manage Database Problems";
    if ($command eq "PROBLEM_LOGS") { $title="Select Problem"; }
    elsif ($command eq "PROBLEM_LOG") { $title="Save Problem Report"; }
    elsif ($command eq "VIEW_PROBLEM_LOG") { $title="View Problem Report"; }
    elsif ($command eq "SUBMIT_PROBLEM") { $title="Problem Report Sent "; } 
    $fheader = "<center><h2>$title</h2></center>";
    &meme_utils::PrintSimpleHeader($title, &meme_utils::getStyle,"",$fheader)

}

# Procedure PrintIndex ###################################################
#
# This procedure displays the index page
#
sub PrintIndex {

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
      <p>Select one of the following actions:</p>
      <ol>
        <li>To browse and view/edit past problem reports, <a href="$ENV{SCRIPT_NAME}?command=PROBLEM_LOGS">
	    click here</a>.</li>
	<li>Search keywords for past problem reports: <form method="GET" action="$ENV{SCRIPT_NAME}"><input type="hidden" name="command" value="PROBLEM_LOGS"><input type="hidden" name="search_field" value="keywords"><input type="text" name="search"><input type="submit"></form></li>
	<li>Full search for past problem reports: <form method="GET" action="$ENV{SCRIPT_NAME}"><input type="hidden" name="command" value="PROBLEM_LOGS"><input type="text" name="search"><input type="submit"></form></li>
	<li>Enter a new problem report:<br>&nbsp;<br>};
    &PrintForm;
    print qq{
        </li>
       </ol>
    </blockquote>
};
}	 

# Procedure PrintIndex ###################################################
#
# This procedure displays the problem form.
#
sub PrintForm {
  $selected{$db_response_link} = " SELECTED";
  print qq{
          <form method="POST" action="$ENV{SCRIPT_NAME}"> 
	    <input type="hidden" name="command" value="SUBMIT_PROBLEM">
	    <center>
              <table width="90%" cellspacing="2" cellpadding="2">
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
	          <td align="left" valign="top"><font size=+1>Original Email:</font></td>
	          <td align="left"><font size=+1>
	            <textarea wrap=soft cols=60 rows=10 name="original_email">$original_email</textarea></font>
	          </td>
                <tr>
                  <td align="left"><font size=+1>Database Response Link:</font></td>
                  <td align="left"><font size=+1>
	            <select name="db_response_link">
	               <option value="None"$selected{$log}>None</option>
        };
  chdir "DBResponses";
  @logs = `ls -t *.html *.txt`;
  foreach $log (@logs) {
    chomp($log);
    print qq{                      <option value="$log"$selected{$log}>$log</option>
      };
  }
    print qq{
                    </select>
	     <input type="button" value="preview"
	       onClick="window.open('/cgi-lti-oracle/db_request.cgi?command=RESPONSE_LOG&log_name='+this.form.db_response_link.value,'Preview','width=800,height=600,scrollbars'); return true;"></font>
                  </td>
                </tr>
                <tr>
                  <td align="left"><font size=+1>Keywords (for search):</font></td>
                  <td align="left"><font size=+1>
	            <input type="text" name="keywords" value="$keywords" size=60></font></td>
                </tr>
                <tr>
                  <td align="left"><font size=+1>Script:</font></td>
                  <td align="left"><font size=+1>
	            <input type="text" name="script" value="$script" size=60></font></td>
                </tr>
	        <tr>
	          <td align="left" valign="top"><font size=+1>Research Details:</font></td>
	          <td align="left"><font size=+1>
	            <textarea wrap=soft cols=60 rows=10 name="research_details">$research_details</textarea></font>
	          </td>
	        </tr>
	        <tr>
	          <td align="left" valign="top"><font size=+1>Fix Details:</font></td>
	          <td align="left"><font size=+1>
	            <textarea wrap=soft cols=60 rows=10 name="fix_details">$fix_details</textarea></font>
	          </td>
	        </tr>
	        <tr>
	          <td colspan="2" align="center"><font size=+1><br>
	            <input type="button" value="Submit Problem Report"
	               onClick="submitForm(this.form);"
	               onMouseOver="window.status='Submit Problem Report'; return true;"
                       onMouseOut="window.status=''; return true;">
         <script language = "JavaScript">
            function isDigit(c) {
                return (c >= 0 || c <= 9);
            }
	    // Validate form
            function submitForm(form) {
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

# Procedure PrintFooter #####################################################################
#
# This procedure prints the (html) footer
#
sub PrintFooter {

    print qq{
	<p>
	<hr width="100%">
        <table border=0 cols=2 width="100%">
        <tr>
        <td align=left valign=top>
        <address>
        <a href="$ENV{SCRIPT_NAME}">Back to Index</a></address>
        </td>

        <td align=right valign=top>
        <address>
	  <font size=-1>Contact: <a href="mailto:bcarlsen\@msdinc.com">Brian A. Carlsen</a><br>},scalar(localtime),qq{<br>};
        &PrintVersion("version");
        print qq{</font></address>

        </td>
        </tr>
        </table>

</body>
</html>
}

}

# Procedure PrintSUBMIT_PROBLEM ####################################################################
#
# This procedure displays problem status page, generates a problem log file and sends mail.
#
sub PrintSUBMIT_PROBLEM {

  #
  # Align work info
  #
  $research_details =~ s/(\r?\n)/\r\n    /g;
  $fix_details =~ s/(\r?\n)/\r\n    /g;
  $original_email =~ s/(\r?\n)/\r\n    /g;
  
  #
  # Prepare the log file
  #
  $log_response = qq{\r
From:             $mail_from\r
Subject:          $subject\r
Original Email:\r
    $original_email
\r
DB Response Link: $db_response_link\r
\r
Keywords:         $keywords\r
Script:           $script\r
Research Details:\r
    $research_details
\r
Fix Details:\r
    $fix_details
\r
};

  #
  # Save email log response
  #    
  $enc_subject = lc("$subject");
  $enc_subject =~ s/ /_/g;
  $log_file = &GetLogFile("$problems_dir/$enc_subject.$date_Ymd");
  open (IO_RFILE, ">$log_file") || 
    ((print "<span id=red>Failed to open $log_file: ($! $?) ($DBI::errstr).</span>")   &&  return);

  print IO_RFILE "$log_response";
  close (IO_RFILE);

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


# Procedure PrintPROBLEM_LOG ###############################################
#
# This procedure displays a response form
# from a previously selected db problem. 
#
sub PrintPROBLEM_LOG {

  #
  # Open the problem log
  #
  open(F, "$problems_dir/$log_name") ||
    ((print "<span id=red>Failed to open $log_file: ($! $?) ($DBI::errstr).</span>")   &&  return);

  #
  # Obtain information from the problem log
  #
  while(<F>) {
    if ($email_flag) { if (/^\r$/) { $email_flag = 0;} else { $original_email .= $_; } }
    elsif ($research_flag) { if (/^\r$/) { $research_flag = 0;} else { $research_details .= $_; } }
    elsif ($fix_flag) { if (/^\r$/) { $fix_flag = 0; } else { $fix_details .= $_; } }
    elsif (/^From:(\W*)(.*)\r$/) { $mail_from=$2; } 
    elsif (/^Subject:(\W*)(.*)\r$/) { $subject=$2; } 
    elsif (/^DB Response Link:(\W*)(.*)\r$/) { $db_response_link=$2; }
    elsif (/^Keywords:(\W*)(.*)\r$/) { $keywords=$2; }
    elsif (/^Script:(\W*)(.*)\r$/) { $script=$2; }
    elsif (/^Original Email:(.*)\r$/) { $email_flag = 1; }
    elsif (/^Research Details:(.*)\r$/) { $research_flag = 1; }
    elsif (/^Fix Details:(.*)\r$/) { $fix_flag = 1; }
  }
  close(F);
  
  $original_email =~ s/\n    /\n/g;
  $original_email =~ s/^    //;
  $original_email =~ s/(\s*)$//;
  $research_details =~ s/\n    /\n/g;
  $research_details =~ s/^    //;
  $research_details =~ s/(\s*)$//;
  $fix_details =~ s/\n    /\n/g;
  $fix_details =~ s/^    //;
  $fix_details =~ s/(\s*)$//;

  #
  # Print the problem form
  #
  &PrintForm;

}

# Procedure PrintVIEW_PROBLEM_LOG ###############################################
#
# This procedure displays a response form
# from a previously selected db problem. 
#
sub PrintVIEW_PROBLEM_LOG {

  #
  # Open the problem log
  #
  open(F, "$problems_dir/$log_name") || 
    ((print "<span id=red>Failed to open $log_file: ($! $?) ($DBI::errstr).</span>")   &&  return);

  #
  # Obtain information from the problem log
  #
  print qq{
  <blockquote>
    <pre>};
  while(<F>) {
    if (/DB Response Link:\s*(.*)\r\n$/ && $1 ne "None") {
      print qq{DB Response Link: <a href="/cgi-lti-oracle/db_request.cgi?command=RESPONSE_LOG&log_name=$1">$1</a>\r\n};
    } else {
      print;
    }
  }
  close(F);
  print qq{</pre>
  </blockquote>
};


}

# Procedure PrintPROBLEM_LOGS ###############################################################
#
# This procedure displays a selection of a list of database problems. 
#
sub PrintPROBLEM_LOGS {

  $lpp = 25;
  
  if ($page > 0) {
    $prev = $page - 1;
    $previous = qq{<p>[<a href="$ENV{SCRIPT_NAME}?command=PROBLEM_LOGS&page=$prev&search=$search&search_field=$search_field">Previous Page</a>]</p>};
  }

  print qq{
  <p>Select one of the following problem reports:</p>
  $previous
  <blockquote>
};
  #
  # Read logs
  #
  chdir $problems_dir;
  if ($search && !$search_field) {
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
  } elsif ($search && $search_field) {
    foreach $word (split / /, $search) {
      unless ($f) {
	@files = `/bin/egrep -i -l "$search_field.*$word" *.html *.txt`;
	$f = 1;
      } elsif (scalar(@files)>0) {
	chomp(@files);
	$files = join " ", @files;
	@files = `/bin/egrep -i -l "$search_field.*$word" $files`;
      }
    }
    @logs = reverse sort @files;
  } else {
    @logs = `ls -t *.html *.txt`;
  }

  if (scalar(@logs)>0) {  
    $j = ($page*$lpp)+$lpp;
    for ($i=$page*$lpp; $i < $j; $i++)  {
      $log = $logs[$i];
      last unless $log;
      chomp($log);
      print qq{
     <tt>$log [<a href="$ENV{SCRIPT_NAME}?command=PROBLEM_LOG&log_name=$log"><tt>edit</tt></a>&nbsp;&nbsp;<a href="$ENV{SCRIPT_NAME}?command=VIEW_PROBLEM_LOG&log_name=$log"><tt>view</tt></a>]</tt><br> };
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
      <p>[<a href="$ENV{SCRIPT_NAME}?command=PROBLEM_LOGS&page=$next&search=$search&search_field=$search_field">Next Page</a>]</p>};
    }

}; # end PrintPROBLEM_LOGS

# SCRIPT INFORMATION ########################################################################
#
#  The following procedures prints helpful information about the script
#
#############################################################################################

sub GetLogFile {
  my($prefix) = @_;
  $prefix =~ s/\(//g;
  $prefix =~ s/\)//g;
  
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
  opendir(D,"$problems_dir");
  @files = readdir(D);
  closedir(D);
  foreach $f (@files) {
    if ($f =~ /\.txt$/ && $f le "$year$mon$mday.oaaaaaa.txt") {
      #print "Removing $f\n";
      unlink "$problems_dir/$f";
    }
  }
  opendir(D,"$response_dir");
  @files = readdir(D);
  closedir(D);
  foreach $f (@files) {
    if ($f =~ /\.txt$/ && $f le "$year$mon$mday.oaaaaaa.txt") {
      unlink "$problems_dir/$f";
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
 This script is a CGI script that generates a searchable problem report
 It takes CGI arguments in the standard form "key=value&key=value...". 

 Paramaters:

  command  :  Tracks the application state
  page     :  Used for searching, to view results a page at a time
  log_name :  Used to review a log
  search   :  Used as a search string
  search_field   :  Used to restrict search to a particular field

  Other parameters are used in the problem report form.  They are,
    mail_from, subject, original_email, db_response_link,
    keywords, research_details, fix_details, script

  Version: $version, $version_date ($version_authority)

};
}
