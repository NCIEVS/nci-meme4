#!@PATH_TO_PERL@
#
# File:     activity_logs.cgi
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
# 12/27/2003 1.0:  First version
#
$release = "4";
$version = "0.5";
$version_authority = "BAC";
$version_date = "12/28/2004";

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
		  "state" => 1, "db" => 1, "work" => 1,
		  "work_id" => 1, "range" => 1,
		  "meme_home" => 1, "oracle_home" => 1);

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
use Text::Wrap;

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
$db = &meme_utils::midsvcs("production-db") unless $db;
$date = `/bin/date +%Y%m%d`;
$cgi = $ENV{"SCRIPT_NAME"};
$start_time = time;
$style_sheet = &meme_utils::getStyle;
$FATAL = 1;
$range = 30 unless $range;

#
# Determine Which action to run, print the body
# Valid STATES:
#
# 0. CHECK_JAVASCRIPT.  Verify that javascript is enabled and redirect
#    the page.  If not print a message indiciating that JavaScript must
#    be enabled.
# 1. INDEX. Print the index page
# 2. LIST_TYPES. Lists the problematic sg_types/suffixes for the specified table
# 3. LIST_CASES. Lists actual cases that do not match

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	     "CHECK_JAVASCRIPT" => 
	         ["PrintHeader","PrintCHECK_JAVASCRIPT","PrintFooter","Check JavaScript","This page will redirect if JavaScript is enabled."],
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","Work Logs","Review Activity/Work Logs"],
	     "LIST_ACTIVITIES" => 
	         ["PrintHeader","PrintLIST_ACTIVITIES","PrintFooter","List Activities ($work_id)","List Activites ($work_id)"],
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
      function ack(url) {
   ok = confirm("This may involve a lot of actions\\r\\n"+
                "Please click OK if you want to proceed.");
   if (ok) {
      location.href=url;
   }
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
	<table BORDER=0 COLS=2 WIDTH="100%"  >
	  <tr >
	    <td ALIGN=LEFT VALIGN=TOP >
	      <address><a href="$cgi?db=$db&range=$range">Back to Index</a></address>
            </td>
	    <td ALIGN=RIGHT VALIGN=TOP >
	      <font size="-1"><address>Contact: <a href="mailto:jwong\@apelon.com">Joanne Wong</a></address>
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
	    window.location.href='$cgi?state=INDEX&db=$db&range=$range';
	</script>
        <p><blockquote>
	    You must use a JavaScript enabled browser to run this
	    application (<a href="http://www.netscape.com">Netscape</a>
	    is recommended).  If you are using
	    a JavaScript enabled browser and just have JavaScript
	    disabled, please enable it and click
	    <a href="$cgi?state=INDEX&db=$db&range=$range">here</a>.
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
    $selected{$work}="SELECTED";
    $selected{$range}="SELECTED";
    $dbs = &meme_utils::midsvcs("databases");
    $mrd = &meme_utils::midsvcs("mrd-db");
    $dbs = "$dbs,$mrd";
    $db_select_form = qq{
                                     <form action="$cgi">
                                       <input type="hidden" name="state" value="INDEX">
                                       <input type="hidden" name="range" value="$range">
                                       <select name="db" onChange="this.form.submit(); return true;">
			                  <option>-- SELECT A DATABASE --</option>\n};
    foreach $db (sort split /,/, $dbs) {
      $db_select_form .= "			                  <option $selected{$db}>$db</option>\n";
      $databases{$db} = $db;
    }
    $db_select_form .= "			                </select></form>\n";
    $range_select_form = qq{
                                     <form action="$cgi">
                                       <input type="hidden" name="state" value="INDEX">
                                       <input type="hidden" name="db" value="$db">
                                       <select name="range" onChange="this.form.submit(); return true;">
			                  <option $selected{30} value="30">Past Month</option>
			                  <option $selected{60} value="60">Past Two Months</option>
			                  <option $selected{120} value="120">Past Three Months</option>
			                  <option $selected{365} value="365">Past Year</option>
                                     </select></form>
};

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    $dbh->do("ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS'")||
      ((print "<span id=red>Error setting date format $db ($DBI::errstr).</span>")
                                                 &&      return);
    $query = qq{
SELECT work_id,type,description,timestamp 
FROM meme_work WHERE timestamp > (sysdate-?)
ORDER BY timestamp DESC
};
   
    # look up descriptions
    $sh = $dbh->prepare($query) || 
      ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute($range) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

   
    print qq{
Choose the database and range of dates to search for database
work.  Then select the work item to see details of that operation.
<br>&nbsp;
<center><table WIDTH="90%"  >
<tr >
<td width="25%">Database:</td><td>$db_select_form</td>
</tr>
<tr >
<td width="25%">Range:</td><td>$range_select_form</td>
</tr>
<tr >
<td width="25%">Work:</td><td>$mr_select_form

                                     <form action="$cgi">
                                       <input type="hidden" name="state" value="LIST_ACTIVITIES">
                                       <input type="hidden" name="range" value="$range">
                                       <input type="hidden" name="db" value="$db">
                                       <select name="work_id" onChange="this.form.submit(); return true;">
			                  <option>-- SELECT DB WORK --</option>
};

    while (($work_id,$type,$description,$timestamp) = $sh->fetchrow_array) {
      print qq{                                         <option value="$work_id">$type - $description ($timestamp)</option>
};
    }
    print qq{
                                       </select></form>

</td>
</tr>
<tr >
<td width="25%" colspan="2"><br><input type="submit" value="SUBMIT"></td>
</tr>
</table></center>
<p>
    };
}; # end PrintINDEX

###################### Procedure PrintLIST_TYPES ######################
#
sub PrintLIST_ACTIVITIES {

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    $dbh->do("ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS'")||
      ((print "<span id=red>Error setting date format $db ($DBI::errstr).</span>")
                                                 &&      return);

    $query = qq{
SELECT description, authority
FROM meme_work WHERE work_id = ?
};
    # look up descriptions
    $sh = $dbh->prepare($query) || 
      ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute($work_id) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    ($work, $authority) = $sh->fetchrow_array;

    $query = qq{
SELECT transaction_id, timestamp, elapsed_time, authority, activity, detail
FROM activity_log WHERE work_id = ?
ORDER BY row_sequence
};
    # look up descriptions
    $sh = $dbh->prepare($query) || 
      ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute($work_id) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    # Print the forms
    print qq{
Following are the activities for this work ($work_id).
<br>&nbsp;
<table border="0">
<tr><td><B>Work</b></td><td>$work</td></tr>
<tr><td><B>Authority</b></td><td>$authority</td></tr>
</table>
<br>
      <center><table cellpadding=2 width="90%" >
<tr> 
  <th width="15%">Transaction ID</th>
  <th width="20%">Timestamp</th>
  <th width="15%">Elapsed Time</th>
  <th width="50%">Activity</th>
};
    
    while (($transaction_id, $timestamp, $et, $authority, $activity, $detail) = 
	   $sh->fetchrow_array) {
      
      $milli = $et%1000; $et = int($et/1000);
      $milli =~ s/0*$//;
      $seconds = "00".int($et % 60); $seconds =~ s/.*(..)/$1/; 
      $et = int($et/60);
      $minutes = "00".int($et % 60); $minutes =~ s/.*(..)/$1/;
      $et = int($et/60);
      $hours = "00".int($et % 60); $hours =~ s/.*(..)/$1/;
      $et = "$hours:$minutes:$seconds.$milli";
      $et =~ s/\.$//;
      $brdetail = " - $detail" if $detail;
      $brdetail = "" unless $detail;
      if ($transaction_id =~ /^(1|0)$/) {
	$transaction_id = "n/a";
      } else {
	$transaction_id = qq{<a href="javascript:ack('/webapps-meme/meme/controller?state=ActionHarvester&midService=$db&transactionId=$transaction_id')">$transaction_id</a>};
      }
      print qq{
  <tr>
    <td valign="top" align="left">$transaction_id</td>
    <td valign="top" align="left">$timestamp</td>
    <td valign="top" align="left">$et</td>
    <td valign="top" align="left">$activity$brdetail</td>
  </tr>
}
  
    }
    print qq{
    </table>
    </center>
  </form>
</p>
};

}; # end PrintLIST_ACTIVITIES


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
 This script is a CGI script used to access the Validate MID tools.  It takes
CGI arguments in the standard form "key=value&key=value...". 

 Parmaters:

  state         :  This is an internal variable representing what
                   state the application is in.
  db            :  Database name
  meme_home     :  Value of $MEME_HOME that the script should use.
  oracle_home   :  Value of $ORACLE_HOME that the script should use.
             
  All other parameters are values used for editing meme properties.  
  They roughly match the fields in the tables that manage meme properties. 
  They include:
      category, key, value, description,

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


