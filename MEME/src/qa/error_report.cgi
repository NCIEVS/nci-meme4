#!@PATH_TO_PERL@
#
# File:     error_report.cgi
# Author:   Tim Kao 
# 
# Changes:
# 07/18/2006 TK (1-BPC17) : Missing "None" parameters in the states
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
#
# Version info:
#   12/04/2003 4.1.0: First version
#   01/15/2004 4.1.1: add error detail pop up windows
#
$release = "4";
$version = "1.1";
$version_authority = "TK";
$version_date = "7/18/2006";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";

#
# Parse command line arguments to determine if -v{ersion}, or --help 
# is being used.  Do not Change this part.
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
# This is the set of valid arguments used by readparse. 
# This list should be configured to suit the variables
# that your CGI script will use
#
%meme_utils::validargs = 
  ( 
    "state" => 1, "db" => 1, "meme_home" => 1, "oracle_home" => 1,
    "host" => 1, "port" => 1, "start_date" => 1, "search_str" => 1
  );


#
# Set environment variables
#

#
# Include the meme_utils.pl library
# This library should be installed in the same
# directory as this script on the CGI server
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
$state = "INDEX" unless $state;

#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MID, current MID
# 
$db = &meme_utils::midsvcs("production-db") unless $db;
$host = &meme_utils::midsvcs("meme-server-host") unless $host;
$port = &meme_utils::midsvcs("meme-server-port") unless $port;
$date = `/bin/date +%e-%b-%Y`;
$start_date = `/bin/date +"%e-%b-%Y"` unless $start_date;
$cgi = $ENV{"SCRIPT_NAME"};
$start_time = time;
$style_sheet = &meme_utils::getStyle;
$FATAL = 1;

#
# Currently there is only one state
#
# 1. INDEX. Print the report
#
#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","Error Report","Error Report"],
	     "MEME_ERROR_DETAIL" =>
	         ["PrintMEMEErrorDetail", "None", "None"],
	     "LOG_DETAIL" =>
	         ["PrintLogDetail", "None", "None"],
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
      // Any java script functions should be added here.
      // Things like form validation code are classic candidates

    function popup(mylink, windowname){
      if (! window.focus) return true;
      var href;
      if (typeof(mylink) == 'string')
	href=mylink;
      else
	href=mylink.href;
    
      window.open(href, windowname, 'width=720, height=500, scrollbars=1,toolbar=0,statusbar=0,resizable=1');
      return false;
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
	<table border="0" cols="2" width="100%">
	  <tr>
	    <td align="left" valign="top">
	      <address><a href="$cgi">Back to Index</a></address>
            </td>
	    <td align="right" valign="top">
	      <font size="-1">
	       <address>
	         Contact: <a href="mailto:tkao\@msdinc.com">
	            Tim Kao</a></address>
	       <address>Generated on:},scalar(localtime),qq{</address>
               <address>This page took $elapsed_time seconds to generate.
	       </address>
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
# This procedure prints the error report
#
sub PrintINDEX {

  #
  # Open database connection
  #
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);
  
  #
  # Set environment
  #
  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);
  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

  # script to select a database    
  $selected{$db}="SELECTED";
  $dbs = &meme_utils::midsvcs("databases");
  $db_select_form = qq{
		       <input type="hidden" name="state" value="INDEX">
		       <select name="db">
		       <option>-- SELECT A DATABASE --</option>\n};
  foreach $db (sort split /,/, $dbs) {
    $db_select_form .= "                                        <option $selected{$db}>$db</option>\n";
    $databases{$db} = $db;
  }    
  $db_select_form .= "                                        </select>\n";

  # let the user choose database, host, port, date
  print qq{
   <form action="$cgi" name="form1" method=get>
  <table border="0" cellspacing="2" cellpadding="2" align="center" nosave="">
   <tr>
     <td width="70%" valign="top"><font size="-1">Change Database</font></td>
     <td valign="top"><font size="-2">$db_select_form.</font></td>
   </tr>
   <tr>
     <td width="70%" valign="top"><font size="-1">Change Host</font></td>
     <td valign="top"><font size="-2"><input type =text name=host value=$host></font></td>
   </tr>
  <tr>
     <td width="70%" valign="top"><font size="-1">Change Port</font></td>
     <td valign="top"><font size="-2"><input type =text name=port value=$port></font></td>
   </tr>
  <tr>
     <td width="70%" valign="top"><font size="-1">Enter a start date for the MEME Error report</font></td>
     <td valign="top"><font size="-2"><input type =text name=start_date value=$start_date></font></td>
   </tr>
   <tr colspan=2>
    <td align="center" colspan=2><input type="button" value=" Submit " onClick='this.form.submit(); return true;'></td>
   </tr>
   </table>
   &nbsp;
   &nbsp;
   &nbsp;
  };

  # Output the content of MEME_ERROR table
  # prepare and execute statement
  $sh = $dbh->prepare(qq{
			 SELECT timestamp, detail
			 FROM MEME_ERROR
			 Where timestamp > to_date('$start_date 23:59:59', 'DD-MON-YYYY HH24:MI:SS')-1
			}) ||
			  ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
			   &&  return);    
  $sh->execute ||
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
     &&  return);
  
  print qq{<br><br><table border="1" cellspacing="2" cellpadding="2" width="90%" nosave="" align="center">
	   <tr><th colspan=3>${db}:MEME Error Log from $start_date to $date</th></tr>
	   <tr><th>Timestamp</th><th>Error Message</th><th width="10%">Detail</th>};
  ($timestamp, $detail) = $sh->fetchrow_array;
  unless ($timestamp){ $timestamp = "No MEME errors"}
    print qq{<tr>
             <td>$timestamp</td>
             <td>$detail</td>
	     <td align="center"><form value="window"><a href="$cgi?state=MEME_ERROR_DETAIL&search_str=$time_stamp" onClick="return popup(this, 'meme_error_detail')">view</a></form></td>
             </tr>
            };
  while (($time_stamp, $detail) = $sh->fetchrow_array) {
    print qq{<tr>
	     <td>$time_stamp</td>
	     <td>$detail</td>
	     <td align="center"><form value="window"><a href="$cgi?state=MEME_ERROR_DETAIL&search_str=$time_stamp" onClick="return popup(this, 'meme_error_detail')">view</a></form></td>
	     </tr>
	    };
  };
  print qq{</table><br><br><br>};
$search_str="";
  
  # connect to the server
  @server_stat = split /\n/, `$ENV{MEME_HOME}/bin/admin.pl -s stats -host $host -port $port | grep Exception`;
  # remove the first line, which is the title of MEME Errors
  shift @server_stat;

  # Output the content of Server Log
  print qq{<table border="1" cellspacing="2" cellpadding="2" width="90%" nosave="" align="center">
	   <tr><th colspan=3>$host MEMEException Stats</th></tr>
           <tr><th>Exception Type</th><th width="5">Frequency</th><th width="10%">Detail</th></tr>};
  foreach $line (@server_stat) {
    #find search_str
    $line =~ s/\s*.*Exception: (.*)\s(\d{1,})\s*/$1|$2/;
    ($search_str, $num)= split /\|/, $line;
    $search_str=~ s/\s*$//;  
    
    print qq{     <tr><td>$search_str</td>
                      <td align="center">$num</td>
                      <td align="center"><form value="pop up log window"><a href="$cgi?state=LOG_DETAIL&search_str=$search_str" onClick="return popup(this, 'log_detail')">view</a></form></td></tr>};
    
  };
  print qq{</table></form>
   <br>};

  # Check for missing indexes
  # prepare and execute statement
  $sh = $dbh->prepare(qq{
                         SELECT index_name
                         FROM meme_indexes
                            MINUS
			 SELECT index_name
			 FROM user_indexes
                        }) ||
                          ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
                           &&  return);
  $sh->execute ||
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
     &&  return);

  print qq{<br><br><table border="1" cellspacing="2" cellpadding="2" width="90%" nosave="" align="center">
           <tr><th colspan=1>The following indexes are missing in ${db}</th></tr>};

  ($index_name) = $sh->fetchrow_array;
  unless ($index_name){ $index_name = "No missing indexes"}
    print qq{<tr>
             <td>$index_name</td>
             </tr>
            };
  while (($index_name) = $sh->fetchrow_array) {
    print qq{<tr>
             <td>$index_name</td>
             </tr>
            };
  };
  print qq{</table><br>};

 # Check for missing stats
  # prepare and execute statement
  $sh = $dbh->prepare(qq{
                         SELECT table_name
                         FROM user_tables
                         WHERE last_analyzed is null
			   AND table_name in (select table_name from meme_tables)
                        }) ||
                          ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
                           &&  return);
  $sh->execute ||
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
     &&  return);

  print qq{<br><br><table border="1" cellspacing="2" cellpadding="2" width="90%" nosave="" align="center">
           <tr><th colspan=1>The following ${db} tables are missing stats</th></tr>};
  ($table_name) = $sh->fetchrow_array;
  unless ($table_name){ $table_name = "No missing statistics"}
    print qq{<tr>
             <td>$table_name</td>
             </tr>
            };
  while (($table_name) = $sh->fetchrow_array) {
    print qq{<tr>
             <td>$table_name</td>
             </tr>
            };
  };
  print qq{</table><br>};

  # Check for unusable indexes
  # prepare and execute statement
  $sh = $dbh->prepare(qq{
                         SELECT table_name, index_name
                         FROM dba_indexes
                         WHERE status = 'UNUSABLE'
                        }) ||
                          ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
                           &&  return);
  $sh->execute ||
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
     &&  return);

  print qq{<br><br><table border="1" cellspacing="2" cellpadding="2" width="90%" nosave="" align="center">
           <tr><th colspan=2>The following ${db} indexes are unusable</th></tr>
           <tr><th>Table Name</th><th>Index</th></tr>};

  ($table_name, $index_name) = $sh->fetchrow_array;
  unless ($table_name){ $table_name = "No unusable indexes"}
    print qq{<tr>
             <td>$table_name</td>
             <td>$index_name</td>
             </tr>
            };
  while (($table_name, $index_name) = $sh->fetchrow_array) {
    print qq{<tr>
             <td>$table_name</td>
             <td>$index_name</td>
             </tr>
            };
  };
  print qq{</table><br><br><br>};

}; # end PrintINDEX

############################# PrintMEMEErrorDetail ##################################
#
# This procedure prints the log detail                    
#
sub PrintMEMEErrorDetail {


  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

 # prepare and execute statement
  $sh = $dbh->prepare(qq{
	      SELECT detail
	      FROM MEME_ERROR
	      Where timestamp = to_date('$search_str', 'DD-MON-YYYY HH24:MI:SS')
			}) ||
  ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")
			   &&  return);    
  $sh->execute ||
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
     &&  return);

  while (($detail) = $sh->fetchrow_array) {
    print qq{$detail</br>};
  };

  PrintPopUpFooter();
}; # end PrintMEMEErrorDetail

############################# PrintLogDetail ##################################
#
# This procedure prints the log detail                    
#
sub PrintLogDetail {
  @server_log = split /\n/, `$ENV{MEME_HOME}/bin/admin.pl -s server_log -host $host -port $port | grep -v Developer`;
  @search_log = grep /exception/i, @server_log;

  @rough_log = grep /$search_str/, @search_log;

  @content=();
  #find the time stamp for the exception
  foreach $log (@rough_log) {
    @temp = ();
    $temp = substr($log,1,20);
    @temp = join '<br>', grep /$temp/, @server_log;
    push @content, @temp;
    push @content, "<br>        -------------------------------------------------------------------------------------<br>";
  };
  print qq{@content};
  PrintPopUpFooter();
}; # end PrintLogDetail

###################### Procedure PrintPopUpFooter ######################
#
# This procedure prints a standard pop up window footer including time to generate
# the page, the current date, and some links
#
sub PrintPopUpFooter {

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
	<table border="0" cols="2" width="100%">
	  <tr>
	    <td align="left" valign="top">
	      <input type="button" value = " Close " onClick="window.close()">
            </td>
	    <td align="right" valign="top">
	      <font size="-1">
	       <address>
	         Contact: <a href="mailto:tkao\@msdinc.com">
	            Tim Kao</a></address>
	       <address>Generated on:},scalar(localtime),qq{</address>
               <address>This page took $elapsed_time seconds to generate.
	       </address>
	       <address>};
    &PrintVersion("version");
    print qq{</address></font>
            </td>
          </tr>
        </table>
    </body>
</html>
};
} # End of PrintPopUpFooter


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
 This script is a CGI script used to generate an error report
 for the current day, including lookups in the server log
 and the meme_errors table.

 Parmaters:

  state         :  This is an internal variable representing what
                   state the application is in.
  db            :  Database name
  host          :  MEME server host
  port          :  MEME server port
  meme_home     :  Value of $MEME_HOME that the script should use.

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


