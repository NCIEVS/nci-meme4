#!@PATH_TO_PERL@
#
# File:     sos.cgi
# Author:   Bobby Edrosa
# 
# Dependencies:  This file requires the following:
#      $MEME_HOME/utils/meme_utils.pl
#
# Description:
#
# Changes:
# 03/22/2006 RBE (1-AQRCB): use MID Services mailing list
#
# Version info:
# 06/12/2003 4.1.0: 1st version created
#
$release = "4";
$version = "1.0";
$version_authority = "RBE";
$version_date = "06/12/2003";

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
                  "command" => 1,
		  "subject" => 1,
		  "mail_to" => 1,
                  "cause" => 1,
                  "cutoff_editing" => 1,
                  "fix_time" => 1, 
		  "comment" => 1,
		  "original_msg" => 1
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
$mail_from = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s sos-from`;
chop($mail_from); }
unless ($mail_to) {
$mail_to = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s sos-list`;
chop($mail_to); }

#
# Default Settings for CGI parameters
#
$subject = "" unless $subject;
$cause = "Unknown" unless $cause;
$cutoff_editing = "No" unless $cutoff_editing;
$fix_time = "" unless $fix_time;
$comment = "" unless $comment;
$original_msg = "" unless $original_msg;
$command = "" unless $command;
$reply_dir = "SOSReplies";
$date_Ymd = `/bin/date +%Y%m%d`;
$date_Ymd =~ s/\n//;

#
# The commands array maps commands to procedures for
# printing the header, body, and footer
#
%commands = (
	     "SUBMIT_REPLY" => 
	         ["PrintHeader","PrintReply","PrintFooter"],
	     "" => 
	         ["PrintHeader","PrintForm","PrintFooter"]
             );

#
# Check to see if command exists
#
if ($commands{$command}) {
    
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

# Procedure PrintHeader #####################################################################
#
# This procedure prints the form (html) header
#
sub PrintHeader {

    $title="SOS Reply Form";
    if ($command eq "SUBMIT_REPLY") { 
	$title="SOS Reply Log"; 
    }
    $fheader = "<CENTER><FONT SIZE=+4><B>$title</B></FONT></CENTER>";
    &meme_utils::PrintSimpleHeader($title, $fheader)

}

# Procedure PrintForm #######################################################################
#
# This procedure displays the form.
#
sub PrintForm {

    $selected{$cause}=" SELECTED";
    $selected{$cutoff_editing}=" SELECTED";

    print qq{
        <FORM METHOD="GET" ACTION="$ENV{SCRIPT_NAME}"> 
	<INPUT TYPE="hidden" NAME="command" VALUE="SUBMIT_REPLY">
	<CENTER>
        <TABLE WIDTH="90%" CELLSPACING="2" CELLPADDING="2">
        <TR>
            <TD ALIGN="left"><FONT SIZE=+1>Subject:</FONT></TD>
            <TD ALIGN="left"><FONT SIZE=+1><INPUT TYPE="text" NAME="subject" VALUE="$subject" SIZE=60></FONT></TD>
        </TR>
        <TR>
            <TD ALIGN="left"><FONT SIZE=+1>To:</FONT></TD>
            <TD ALIGN="left"><FONT SIZE=+1><INPUT TYPE="text" NAME="mail_to" VALUE="$mail_to" SIZE=60></FONT></TD>
        </TR>
    };

    print qq{
    </SELECT></FONT>
    </TD>
    </TR>
    <TR>
        <TD ALIGN="left"><FONT SIZE=+1>Cause of Problem:</FONT></TD>
        <TD ALIGN="left"><FONT SIZE=+1><SELECT NAME="cause">
           <OPTION>Bad Data</OPTION>
           <OPTION>Bug in Code</OPTION>
           <OPTION>Unknown</OPTION></SELECT></FONT></TD>
    </TR>
    <TR>
        <TD ALIGN="left"><FONT SIZE=+1>Cutoff Editing:</FONT></TD>
        <TD ALIGN="left"><FONT SIZE=+1><SELECT NAME="cutoff_editing">
           <OPTION $selected{"Yes"}>Yes</OPTION>
           <OPTION $selected{"No"}>No</OPTION>
           <OPTION $selected{"Limited"}>Limited</OPTION></SELECT></FONT></TD>
    </TR>
    <TR>
        <TD ALIGN="left"><FONT SIZE=+1>Estimated time to fix:</FONT></TD>
        <TD ALIGN="left"><FONT SIZE=+1><INPUT TYPE="text" NAME="fix_time" VALUE="$fix_time" SIZE=10></FONT></TD>
    </TR>
    <TR>
        <TD ALIGN="left" VALIGN="top"><FONT SIZE=+1>Comments:</FONT></TD>
        <TD ALIGN="left"><FONT SIZE=+1><TEXTAREA WRAP=soft COLS=60 ROWS=10
        NAME="comment">$comment</TEXTAREA></FONT></TD>
    </TR>
    <TR>
        <TD ALIGN="left" VALIGN="top"><FONT SIZE=+1>Original Message:</FONT></TD>
        <TD ALIGN="left"><FONT SIZE=+1><TEXTAREA WRAP=soft COLS=60 ROWS=10
        NAME="original_msg">$original_msg</TEXTAREA></FONT></TD>
    </TR>
    <TR>
        <TD COLSPAN="2" ALIGN="center">
        <FONT SIZE=+1><INPUT TYPE="button" VALUE="Send SOS Reply"
        onClick="submitForm(this.form);"
        onMouseOver="window.status='Send SOS Reply'; return true;"
        onMouseOut="window.status=''; return true;">
        <SCRIPT LANGUAGE = "JavaScript">
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
        <TD ALIGN=LEFT VALIGN=TOP NOSAVE>
        <ADDRESS>
        <A HREF="/MEME/">MEME Home</A></ADDRESS>
        </TD>

        <TD ALIGN=RIGHT VALIGN=TOP NOSAVE>
        <ADDRESS>
	  <FONT SIZE=-1>Contact: <A HREF="mailto:carlsen\@apelon.com">Brian A. Carlsen</A><br>},scalar(localtime),qq{<br>};
        &PrintVersion("version");
        print qq{</FONT></ADDRESS>

        </TD>
        </TR>
        </TABLE>

</BODY>
</HTML>
}

}

# Procedure PrintReply ######################################################################
#
# This procedure displays status page, generates a log file and sends mail.
#
sub PrintReply() {

    use Text::Wrap;
    $Text::Wrap::columns = 76;
    $pre1 = "\n\t";
    $pre2 = "\t";
    $comment = wrap($pre1, $pre2, $comment);
    $pre1 = "\n>";
    $pre2 = ">";
    $original_msg = wrap($pre1, $pre2, $original_msg);

    $sos_msg = qq{
Cause of Problem:      $cause
Cutoff Editing:        $cutoff_editing
Estimated time to fix: $fix_time
Comments:              $comment

Original message:      $original_msg
};

    #
    # Prepare and send email response
    #
    use Mail::Sender;
    $sender = new Mail::Sender{
        smtp => "$smtp_host", 
	from => "$mail_from"};
    $sender->MailMsg({to => "$mail_to", cc => 'meme@apelon.com',
		      subject => "Re: $subject",
		      msg => "$sos_msg"});
    if ($sender->{error}) {
      $msg = qq{<B><FONT COLOR="A00000"> There was an error sending the following (}.$sender->{error_msg}.qq{):</font></B>};
    } else {
      $msg = "The following message was successfully sent:";
    }


    $response = qq {
        $msg
        <blockquote>
        <pre>
To:                    $mail_to
Cause of Problem:      $cause
Cutoff Editing:        $cutoff_editing
Estimated time to fix: $fix_time
Comments:              $comment

Original message:      $original_msg
        </pre>
        </blockquote>
        Click <A HREF="$ENV{SCRIPT_NAME}"
        onMouseOver="window.status='$ENV{SCRIPT_NAME}'; return true;"
        onMouseOut="window.status=''; return true;">Here</A> to send another reply.
    };

    print $response;
    open (F, ">$reply_dir/$date_Ymd.html") || print qq{<B><FONT COLOR="A00000">Couldn't open file: $reply_dir/$date_Ymd.html</FONT></B>};
    print F $response;
    close(F);

    unlink "$reply_dir/$date_Ymd.html";
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
 This script is a CGI script that generates a form used to reply for SOS email received from NLM.
 It takes CGI arguments in the standard form "key=value&key=value...". 

 Paramaters:

  command  :  The command for the script to use.  Valid values are:
              submit (Generates SOS reply form)
              "" (The empty value just reloads the form)
  subject  :  Mail subject
  mail_to  :  Mail to list of recipients
  cause    :  Cause of Problem
  cutoff_editing   :  "Yes" or "No"
  fix_time :  Estimated time to fix
  comment  :  Comments
  original_msg  :  Original message

  Version: $version, $version_date ($version_authority)

};
}
