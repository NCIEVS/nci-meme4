#!@PATH_TO_PERL@
#
# File:     status_rpt.cgi
# Author:   Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#
# Version info:
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
# 05/12/2003 3.3.0: Shows "Action Items" in addition to status report
#                   and schedule
# 08/16/2001 3.2.0: Shows changes in status reports in bold
# 3/20/2001 3.1.0: First version
#
$release = "3";
$version = "3.0";
$version_authority = "BAC";
$version_date = "05/12/2003";

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
		  "state" => 1, "status_report" => 1, "view_report" => 1
);


#
# Set environment variables
# This section must be updated when script is moved to another machine
#
$inc = $ENV{"SCRIPT_FILENAME"};
$inc =~ s/(.*)\/.*$/$1/;
unshift @INC, "$inc";
require "meme_utils.pl";
$rpt_dir = "StatusReports";

#
# Readparse translates CGI argument string into variables 
# that can be used by the script
#
&ReadParse($_);

#
# This is the HTTP header, it always gets printed
#
print qq{Expires: Fri, 20 Sep 1998 01:01:01 GMT\n};
&meme_utils::PrintHTTPHeader;

#
# Default Settings for CGI parameters
#
$state = "INDEX" unless $state;


#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MID, current MID
# 
$date = `/bin/date +%Y%m%d`;
chop($date);
$cgi = $ENV{"SCRIPT_NAME"};
$start_time = time;
$style_sheet = &meme_utils::getStyle;

$FATAL = 1;

#
# Determine Which action to run, print the body
# Valid STATES:
#
# 1. INDEX. Print the index page
# 2. LIST.  List existing reports
# 3a. UPLOAD_STATUS.  Upload status report
# 3b. UPLOAD_SCHEDULE.  Upload schedule
# 3c. UPLOAD_ACTION.  Upload action items report
# 4. SHOW.  Show a status report
#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","Status Report Management - Index Page","Status Report Management"],
	     "LIST" =>
	         ["PrintHeader","PrintLIST","PrintFooter", "Choose a Status Report", "Choose a Status Report"],
	     "UPLOAD_STATUS" =>
	         ["PrintHeader","PrintUPLOAD_STATUS","PrintFooter","Upload Status Report", "Upload Status report"],
	     "UPLOAD_SCHEDULE" =>
	         ["PrintHeader","PrintUPLOAD_SCHEDULE","PrintFooter","Upload Schedule", "Upload Schedule"],
	     "UPLOAD_ACTION" =>
	         ["PrintHeader","PrintUPLOAD_ACTION","PrintFooter","Upload Action Items", "Upload Action Items"],
	     "SHOW" =>
	         ["None","PrintSHOW","None","", ""]
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


###################### Procedure PrintINDEX ######################
#
# This procedure prints the index page for the application with
# links to the other functionality
#
sub PrintINDEX {

    chdir "$inc/$rpt_dir";
    $current_act = `ls -t action* | head -1`;
    $current_rpt = `ls -t status* | head -1`;
    $current_sch = `ls -t schedule* | head -1`;
    chop($current_rpt);
    chop($current_sch);
    print qq{
<i>Select from one of these choices:</i>
<br>&nbsp;
<center><table WIDTH="90%" NOSAVE >
<tr>
<td><b><a href="$cgi?state=SHOW&view_report=$current_rpt" onMouseOver='window.status="Show Current Report"; return true;' onMouseOut='window.status=""; return true;'>Show Current Report</a></b></td>
</tr>

<tr>
<td><b><a href="$cgi?state=SHOW&view_report=$current_act" onMouseOver='window.status="Show Current Action Items"; return true;' onMouseOut='window.status=""; return true;'>Show Current Action Items Report</a></b></td>
</tr>

<tr>
<td><b><a href="$cgi?state=SHOW&view_report=$current_sch" onMouseOver='window.status="Show Current Schedule"; return true;' onMouseOut='window.status=""; return true;'>Show Current Schedule</a></b></td>
</tr>

<tr>
<td><b><a href="$cgi?state=LIST" onMouseOver='window.status="List All Reports"; return true;' onMouseOut='window.status=""; return true;'>List All Reports</a></b></td>
</tr>

<tr>
<td>&nbsp;
</td>
</tr>

<tr>
<td><b>Upload New Report</b><blockquote><form method=POST action="$cgi" enctype="multipart/form-data">
    <input type="file" name="status_report">
    <input type="hidden" name="state" value="UPLOAD_STATUS">
    &nbsp;&nbsp;<b><input style="text-decoration: bold" type="submit" value="Upload"></b></form></blockquote>
</td>
</tr>

<tr>
<td><b>Upload New Schedule</b><blockquote><form method=POST action="$cgi" enctype="multipart/form-data">
    <input type="file" name="status_report">
    <input type="hidden" name="state" value="UPLOAD_SCHEDULE">
    &nbsp;&nbsp;<b><input style="text-decoration: bold" type="submit" value="Upload"></b></form></blockquote>
</td>
</tr>

<tr>
<td><b>Upload New Action Items Report</b><blockquote><form method=POST action="$cgi" enctype="multipart/form-data">
    <input type="file" name="status_report">
    <input type="hidden" name="state" value="UPLOAD_ACTIONS">
    &nbsp;&nbsp;<b><input style="text-decoration: bold" type="submit" value="Upload"></b></form></blockquote>
</td>
</tr>

</table></center>

<p>
    };
}; # end PrintINDEX


###################### Procedure PrintLIST ######################
#
#  List all available reports and schedules.
#
sub PrintLIST {

    print qq{
<i>Select from one of these choices:</i>
<br>&nbsp;
<blockquote><table border=0 ><tr><td>
};
    chdir "$rpt_dir";
    @logs = `ls -t status*`;
    print qq{
   <tr><td valign="top">Select a status report:&nbsp;&nbsp;&nbsp;</td>
       <td valign="top"><form action="$cgi" method="GET">
       <input type="hidden" name="state" value="SHOW">
       <select  name="view_report" onChange="this.form.submit(); return true;">
	 <option>---- SELECT A REPORT ----</option>
};
    foreach $file (@logs) {
	chomp($file);
	print qq{          <option>$file</option>\n};
    }
    print qq{
        </select> <b>OR</b>
    </form></td></tr>
};
    @logs = `ls -t action*`;
    print qq{
   <tr><td valign="top">Select action items report:&nbsp;&nbsp;&nbsp;</td>
       <td valign="top"><form action="$cgi" method="GET">
       <input type="hidden" name="state" value="SHOW">
       <select  name="view_report" onChange="this.form.submit(); return true;">
	 <option>---- SELECT A REPORT ----</option>
};
    foreach $file (@logs) {
	chomp($file);
	print qq{          <option>$file</option>\n};
    }
    print qq{
        </select> <b>OR</b>
    </form></td></tr>
};
    @logs = `ls -t schedule*`;
    print qq{
   <tr><td valign="top">Select a schedule:</td>
       <td valign="top"><form action="$cgi" method="GET">
       <input type="hidden" name="state" value="SHOW">
       <select name="view_report" onChange="this.form.submit(); return true;">
	 <option>---- SELECT A REPORT ----</option>
};
    foreach $file (@logs) {
	chomp($file);
	print qq{          <option>$file</option>\n};
    }
    print qq{
        </select>
    </form></td></tr>
</table></blockquote>
};
}; # end PrintLIST

###################### Procedure PrintSHOW ######################
#
# Show a report, the status_report variable will have the name
#  of the report (it should be a file in the $rpt_dir subdirectory
#
sub PrintSHOW {

  #
  # Find the previous report
  #
  if ($view_report =~ /^status/) {
    @lines = `ls -t $rpt_dir/status*txt`;
    chomp(@lines);
  }

  #
  # Handle current_* cases
  #
  if ($view_report =~ /^current/) {
    ($d,$start) = split /_/,$view_report;
    @lines = `ls -t $rpt_dir/${start}*txt`;
    chomp(@lines);
    ($d,$view_report) = split /\//, $lines[0];
  }

  for ($i=0; $i < scalar(@lines); $i++) {
    if ($lines[$i] eq "$rpt_dir/$view_report") {
      $prev_report = $lines[$i+1];
      last;
    }
  }

  if ($prev_report) {
    open (F,"$prev_report") ||
	((print "<span id=red>Cannot open status report $prev_report: $! $?</span>") && return);
    @prev = <F>;
    close(F);
   }

    open(F,"$rpt_dir/$view_report") ||
	((print "<span id=red>Cannot open status report $view_report.</span>") && return);
    if ($view_report =~ /\.txt$/) {
	print qq{<html><body bgcolor="#ffffff"><a href="$cgi"><i>Back to Index</i></a><pre>
		 };
    }
    $vf = 1 if $view_report =~ /\.txt$/;
    while (<F>) {
	if (/\<body/i) { 
	    print;
	    print qq{<a href="$cgi"><i>Back to Index</i></a><p>};
	}
	if ($vf) {
	    s/(http\:\/\/[^\s]*)(\s)/<a href="$1">$1<\/a>$2/;
	}
	
	$line = $_;
	if ($view_report =~ /^status/) {
	  if (grep $line eq $_, @prev) {
	    print;
	  } else {
	     print qq{<span style="color: #0000A0;"><b>$line</b></span>};
	  }
	} else {
	  print "$line";
	}
    };
    if ($prev_report && $view_report =~ /\.txt$/) {
	print qq{</pre></body></html>
		 };
    }

}; # end PrintSHOW

###################### Procedure PrintUPLOAD_STATUS ######################
#
# This procedure is responsible for processing the status_report 
# document 
#
sub PrintUPLOAD_STATUS {
  
    open(F,">$rpt_dir/status.$date.txt") || 
	((print "<span id=red>Could not open file $inc/$rpt_dir/status.$date.txt</span>") && return);
    print F $status_report;
    print F "\n";
    close(F);

    print qq{
<center><table WIDTH="90%" NOSAVE >
<tr>
<td>
The status report was successfully loaded.  Click <a href="$cgi?state=SHOW&view_report=status.$date.txt">here</a> to see it.
</td>
</tr>
</table></center>

<p>
};
}; # end PrintUPLOAD_STATUS

###################### Procedure PrintUPLOAD_ACTION ######################
#
# This procedure is responsible for processing the action items
# document 
#
sub PrintUPLOAD_ACTION {
  
    open(F,">$rpt_dir/action-items.$date.txt") || 
	((print "<span id=red>Could not open file $inc/$rpt_dir/action-items.$date.txt</span>") && return);
    print F $status_report;
    print F "\n";
    close(F);

    print qq{
<center><table WIDTH="90%" NOSAVE >
<tr>
<td>
The action items report was successfully loaded.  Click <a href="$cgi?state=SHOW&view_report=action-items.$date.txt">here</a> to see it.
</td>
</tr>
</table></center>

<p>
};
}; # end PrintUPLOAD_ACTION

###################### Procedure PrintUPLOAD_SCHEDULE ######################
#
# This procedure is responsible for processing the schedule
# document 
#
sub PrintUPLOAD_SCHEDULE {
  
    open(F,">$rpt_dir/schedule.$date.txt") || 
	((print "<span id=red>Could not open file $inc/$rpt_dir/schedule.$date.txt</span>") && return);
    print F $status_report;
    print F "\n";
    close(F);

    print qq{
<center><table WIDTH="90%" NOSAVE >
<tr>
<td>
The schedule was successfully loaded.  Click <a href="$cgi?state=SHOW&view_report=schedule.$date.txt">here</a> to see it.
</td>
</tr>
</table></center>

<p>
};
}; # end PrintUPLOAD_SCHEDULE



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
 This script is a CGI script used to manage status reports.  It takes
CGI arguments in the standard form "key=value&key=value...". 

 Parmaters: 
   
    state         : This is an internal variable representing
	            the state of the application.
    status_report : When uploading a report this is the filename.
                    It must be the name of a file in the $rpt_dir
                    subdirectory of the CGI directory.

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


###################### Procedure ReadParse ######################
# This procedure is responsible for breaking down the CGI argument
# string into variables.  Only if the $name part is valid will it
# succeed.
sub ReadParse {
    my($string) =@_;
    my($pair,@pairs,$mainname,$mainencname,$value,$filetype);
    my($name,$filename,$boundary,%readparse);
    if (!$string) {
	if ($ENV{"REQUEST_METHOD"} eq "GET") {
	    $string = $ENV{"QUERY_STRING"};
	}
	else {
	    $string = (<STDIN> || &CGIError("Method $ENV{REQUEST_METHOD} not supported at this time.",1) );
	}
    }
    
    #
    # Deal with enctype="multipart/form-data", method must be POST
    #
    if ($ENV{"CONTENT_TYPE"} =~ /multipart\/form\-data; boundary=\-*(.*)/) {
	$readparse{"boundary"} = "$1";
	if ($string !~ /$readparse{boundary}/) {
	    print "Error1";
	    &CGIError("Cannot parse form ($string,$readparse{boundary}, $ENV{CONTENT_TYPE}), Exiting...",1);
	};
	while (<STDIN>) {
	    $boundary=0;
	    if (/Content-Disposition: form-data; name=\"(.*)\"; filename=\"(.*)\"/) {
		$name = $1; $filename = $2; $_=<STDIN>;
		if (/Content\-Type: image\/(.*)\r\n$/i) {
		    $filetype = $1; 
		    $filetype = "jpeg" if $filetype =~ /jpe{0,1}g/;
		    $readparse_content_type{"$name"} = "$filetype";
		}
		elsif (/Content\-Type: text\/(html|plain)/) {
		    $filetype = "html";
		    $readparse_content_type{"$name"} = "$filetype";
		}
		elsif (!$filename) {}
		else {
		    &CGIError("Illegal content-type: $_",1);
		};
		$_=<STDIN>;      # read blank line
		if ($filename) {
		    $value = "";
		    while (<STDIN>) {
			if ($_ =~ /$readparse{boundary}/) {
			    $boundary=1;
			    last;
			}
			else {
			    $value .= "$_";
			};		    
		    };
		} else {
		    $value="";
		    while(<STDIN>) {
			if ($_ =~ /$readparse{boundary}/) {
			    $boundary=1;
			    last;
			}
		    }
		}
		&CGIError("Improperly formatted form-data.",1) unless $boundary;
	    }
	    elsif (/Content-Disposition: form-data; name=\"(.*)\"/) {
		$name = $1; 
		$_ = <STDIN>;                 # read blank line

		#
		# Read until boundary, convert \r\n into <BR>
		#
		$value = "";
		#print "Content-type: text/plain\n\n";
		while (<STDIN>) {

		    if ($_ =~ /$readparse{boundary}/) {
			$boundary=1;
			# chop \r\n or \n off the end
			$value =~ s/(\r?\n)*$//;
			last;
		    }
		    else {
# chop at the end	chop; chop;           # chop \r \n off the end
			$value .= "$_";
		    };		    
		};
#
# This is done where needed
#		$value =~ s/\r\n/<BR>/g;
	    };

	    &CGIError("Improperly formatted form-data.",1) unless $boundary;
	
	    next if (!$meme_utils::validargs{$name});
	    $mainname="main::$name";
	    $mainencname="main::enc_$name";
	    if ($$mainname) {
		$$mainname = qq{$$mainname|$value};
		$$mainencname = &meme_utils::escape($$mainname);
	    }
	    else {
		$$mainname = qq{$value};
		$$mainencname = &meme_utils::escape(qq{$value});
	    }
	
	    #print "$mainname = $$mainname<BR>\n";
	}
    
	#
	# If multipart/form-data then don't do next section
	#
	return;
    }

#    print "Content-type: text/plain\n\n";
    @pairs=split(/\&/,$string);
    foreach $pair (@pairs) {
        if ($pair =~/(.*?)=(.*)/) {
	    $name= $1;
	    $value = $2;
#	    print "name....$value...";
#	    print "$name: $meme_utils::validargs{$name}\n";
	    next if (!$meme_utils::validargs{$name});
	    $value =~ s/\+/ /g;
	    $name =~ s/%([0-9a-fA-F]{2})/pack("c",hex($1))/ge;
	    $value =~ s/%([0-9a-fA-F]{2})/pack("c",hex($1))/ge;
	    # strip newlines off the end
  	    $value =~ s/(\r?\n)*$//;
	    $mainname="main::$name";
	    $mainencname="main::enc_$name";
	    if ($$mainname) {
		$$mainname = qq{$$mainname|$value};
		$$mainencname = &meme_utils::escape($$mainname);
	    }
	    else {
		$$mainname = qq{$value};
		$$mainencname = &meme_utils::escape(qq{$value});
	    }
            #print "$mainname = ",qq{$value};
	}
    }
}


###################### Procedure CGIError ######################
# Prints an error message
sub CGIError { 
    my($err_msg,$fatal_flag) = @_;
    my($msg);
    #
    # if flag is passed, Print a complete error page
    #
    $msg = "<span id=red> $err_msg </span>";
    if ($fatal_flag) {
	print "Content-type: text/html\n\n<HTML><BODY>$msg</BODY></HTML>";
	exit(0);
    };
    
    print $msg;
}
