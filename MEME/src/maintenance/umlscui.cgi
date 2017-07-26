#!@PATH_TO_PERL@
#
# File:     umlscui.cgi
# Author:   Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#      Web form for concept id lookup
$release = "4";
$version = "1.0";
$version_authority = "BAC";
$version_date = "11/23/2008";

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
		  "state" => 1, "type" => 1,
		  "db" => 1, "umlscui" => 1,
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


#
# Determine Which action to run, print the body
# Valid STATES:
#
# 0. CHECK_JAVASCRIPT.  Verify that javascript is enabled and redirect
#    the page.  If not print a message indiciating that JavaScript must
#    be enabled.
# 1. INDEX. Print the index page
# 3. EDIT_INDEX. Index page for meme properties editing.
# 4. EDIT_COMPLETE. Page shown upon insert,modify,delete

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
%states = (
	     "CHECK_JAVASCRIPT" => 
	         ["PrintHeader","PrintCHECK_JAVASCRIPT","PrintFooter","Check JavaScript","This page will redirect if JavaScript is enabled."],
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","UMLSCUI Search - Index Page","UMLS CUI Lookup"],
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
return qq {
}

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
	      <font size="-1"><address>Contact: <a href="mailto:tnaing\@msdinc.com">Tun Tun Naing</a></address>
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
    $selected{$db}="SELECTED";
    $dbs = &meme_utils::midsvcs("databases");
    $mrd = &meme_utils::midsvcs("mrd-db");
    $dbs = "$dbs,$mrd";
    $db_select_form = qq{
              <form action="$cgi">
		  UMLS CUI:
                <input type="hidden" name="state" value="INDEX">
                <input type"text" name="umlscui" value="$umlscui">
               <select name="db">
   };
    foreach $db (sort split /,/, $dbs) {
      $db_select_form .= "			                  <option $selected{$db}>$db</option>\n";
      $databases{$db} = $db;
    }
    $db_select_form .= qq{			                </select>&nbsp;  <input type="submit" value="GO" >
               </form>};

    if ($umlscui) {
	PrintResults();
    }
    print qq{
<p>
$db_select_form
</p>
    };
}; # end PrintINDEX

###################### Procedure PrintResults ######################
#
# Look up $umlscui, print results
#
sub PrintResults {

    # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
  ($user,$password) = split /\//, $userpass;
  chop($password);

  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
      ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
       &&  return);

    # look up descriptions
    $sh = $dbh->prepare(qq{
    SELECT distinct concept_id FROM attributes
    WHERE attribute_name = 'UMLSCUI'
      AND attribute_value = ?
    ORDER BY 1
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute($umlscui) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);


  print qq{<h3>Results:</h3><ul>};
    while (($cid) = $sh->fetchrow_array) {     
	print qq {<li><a href="/cgi-oracle-meowusers/concept-report-mid.pl?action=searchbyconceptid&arg=$cid">$cid</a></li>\n};
    }
  print qq{</ul>};
 
}; # end PrintResults


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
 This script is a CGI script used to look up concept ids by UMLSCUI attribute.
CGI arguments in the standard form "key=value&key=value...". 

 Parmaters:

  state         :  This is an internal variable representing what
                   state the application is in.
  db            :  Database name
  meme_home     :  Value of $MEME_HOME that the script should use.
  umlscui       :  UMLS CUI value to search
  
  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


