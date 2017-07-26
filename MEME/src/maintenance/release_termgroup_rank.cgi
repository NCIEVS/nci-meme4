#!@PATH_TO_PERL@
#
# File:     release_termgroup_rank.cgi
# Author:   Tun Tun Naing
# 
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#
# Version info:

# 05/20/2003 1.0: Release version
#
$release = "4";
$version = "1.0";
$version_authority = "BAC";
$version_date = "05/20/2003";

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
		  "db" => 1, "termgps" => 1,
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
	         ["PrintHeader","PrintINDEX","PrintFooter","Termgroup Rank - Index Page","Termgroup Rank"],
	     "EDIT_INDEX" => 
	         ["PrintHeader","PrintEDIT_INDEX","PrintFooter","Edit Termgroup Ranks","Edit Termgroup Ranks"],
	     "EDIT_COMPLETE" => 
	         ["PrintHeader","PrintEDIT_COMPLETE","PrintFooter","Edit Complete","Termgroup Rank"],
	     "RUN_COMPLETE" => 
	         ["PrintHeader","PrintRUN_COMPLETE","PrintFooter","Run Complete","Termgroup Rank"]
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

<SCRIPT LANGUAGE="JavaScript">
// -------------------------------------------------------------------
// selectAllOptions(select_object)
//  This function takes a select box and selects all options (in a 
//  multiple select object). This is used when passing values between
//  two select boxes. Select all options in the right box before 
//  submitting the form so the values will be sent to the server.
// -------------------------------------------------------------------
function selectAllOptions(obj) {
	for (var i=0; i<obj.options.length; i++) {
		obj.options[i].selected = true;
		}
      }

// -------------------------------------------------------------------
// swapOptions(select_object,option1,option2)
//  Swap positions of two options in a select list
// -------------------------------------------------------------------
function swapOptions(obj,i,j) {
	var o = obj.options;
	var i_selected = o[i].selected;
	var j_selected = o[j].selected;
	var temp = new Option(o[i].text, o[i].value, o[i].defaultSelected, o[i].selected);
	var temp2= new Option(o[j].text, o[j].value, o[j].defaultSelected, o[j].selected);
	o[i] = temp2;
	o[j] = temp;
	o[i].selected = j_selected;
	o[j].selected = i_selected;
	}

// -------------------------------------------------------------------
// moveOptionUp(select_object)
//  Move selected option in a select list up one
// -------------------------------------------------------------------
function moveOptionUp(obj) {
	for (i=0; i<obj.options.length; i++) {
		if (obj.options[i].selected) {
			if (i != 0 && !obj.options[i-1].selected) {
				swapOptions(obj,i,i-1);
				obj.options[i-1].selected = true;
				}
			}
		}
	}

// -------------------------------------------------------------------
// moveOptionDown(select_object)
//  Move selected option in a select list down one
// -------------------------------------------------------------------
function moveOptionDown(obj) {
	for (i=obj.options.length-1; i>=0; i--) {
		if (obj.options[i].selected) {
			if (i != (obj.options.length-1) && ! obj.options[i+1].selected) {
				swapOptions(obj,i,i+1);
				obj.options[i+1].selected = true;
			      }
		      }
	      }
      }
</script>
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
	      <font size="-1"><address>Contact: <a href="mailto:tnaing\@apelon.com">Tun Tun Naing</a></address>
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
			               <input type="hidden" name="state" value="EDIT_INDEX">
			               <input id="rr" type="radio" checked name="type" value="release"><label for="rr">release rank</label><BR>
			               <input id="er" type="radio" name="type" value="editing"><label for="er">editing rank</label><BR><BR>

			               <select name="db">
   };
    foreach $db (sort split /,/, $dbs) {
      $db_select_form .= "			                  <option $selected{$db}>$db</option>\n";
      $databases{$db} = $db;
    }
    $db_select_form .= qq{			                </select>&nbsp;  <input type="submit" value="GO" >\n};


    print qq{
<center><table WIDTH="90%" NOSAVE >
<tr>
<td colspan="2" valign="top">To change termgroup rankings, select one of the type of rankings and databases:</td>
</tr>
<tr>
<td colspan="2">&nbsp;</td>
</tr>
<tr><td align="left">$db_select_form</td>
</td>
</tr>
</form>
</table></center>

<p>
    };
}; # end PrintINDEX

###################### Procedure PrintEDIT_INDEX ######################
#
# This procedure prints a page that refreshes while
# the script is running.  
#
sub PrintEDIT_INDEX {

    # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
  ($user,$password) = split /\//, $userpass;
  chop($password);

  # open connection
  $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
     &&  return);

    $col = $type eq "release" ? "release_rank" : "rank";

    # look up descriptions
    $sh = $dbh->prepare(qq{
    SELECT termgroup FROM termgroup_rank
    WHERE SUBSTR(termgroup,0,INSTR(termgroup,'/')-1) IN
       (SELECT current_name FROM source_version)
    ORDER BY $col DESC
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);
    while (($termgp) = $sh->fetchrow_array) {     
      #($sab,$tty) = split/\//,$termgp;
      #$view_termgp = sprintf "%-20s%s",$sab,$tty;
      #$view_termgp =~ s/ /&nbsp;/g;
      $options .= qq{ <OPTION VALUE="$termgp">$termgp</OPTION>\n };
    }

    # Print the forms
    print qq{
<blockquote>
The following list reflects the current termgroup $type ranking in the $db database.
<ul>
  <li>Select one or more rows</li>
  <li>Use "Up" to increase the $type rank of selected rows</li>
  <li>Use "Down" to decrease the $type rank of selected rows</li>
</ul>
</blockquote>
    <form method="POST" action="$cgi">
      <input type="hidden" name="state" value="EDIT_COMPLETE">
      <input type="hidden" name="db" value="$db"> 
      <input type="hidden" name="type" value="$type"> 
     <CENTER>
    <TABLE BORDER="0">
     <TR>
	<TD>
	<SELECT NAME="termgps" SIZE="20" MULTIPLE>
	$options
	</SELECT>
	</TD>
	<TD ALIGN="CENTER" VALIGN="MIDDLE">
	<INPUT TYPE="button" VALUE="  Up  " onClick="moveOptionUp(this.form['termgps'])">
	<BR><BR>
	<INPUT TYPE="button" VALUE="Down" onClick="moveOptionDown(this.form['termgps'])">
	</TD>
</TR>
<TR><TD>&nbsp;</TD></TR>
     <TR>
	<TD ALIGN="CENTER">
           <input type="button" onClick="selectAllOptions(this.form['termgps']);this.form.submit();return true;" value="Submit"></b>
	     &nbsp;&nbsp;&nbsp;
           <input type="button" value="Cancel" onClick="history.go(-1)"></b>
          </font></b>
	</TD>
</TR>
</TABLE>
</CENTER>
  </form>
</p>
};

}; # end PrintEDIT_INDEX

###################### Procedure PrintEDIT_COMPLETE ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintEDIT_COMPLETE {

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);


    $rank = 0;
    $inc =  $type eq "editing" ? 4 : 1;
    $col = $type eq "release" ? "release_rank" : "rank";
    
    foreach $termgp(reverse(@termgps)) {
      #update row
      $row_count += $dbh->do( qq{
        UPDATE termgroup_rank SET
            $col = ?
        WHERE termgroup = ?} , {"dummy"=>1},
        $rank, $termgp);
      $rank += $inc;
      # check for error
      (! $DBI::err) || ((print "<span id=red>Error updating row ($code,($DBI::errstr).</span>")
			&&  return);
    }
          
     unless (defined($row_count)) { 
    ((print "<span id=red>Error updating $type termgroup rank ($DBI::errstr).</span>")  
             &&  return);
    }
     # verify row was updated
        print qq{
           The $type rank $row_count rows were updated successfully. 
	Click <a href="$cgi?state=EDIT_INDEX&db=$db&type=$type">here</a> to return to the list page.
      };
        print qq{
	<BR><BR>
	Click <a href="$cgi?state=RUN_COMPLETE&db=$db">here</a> to set the rank field.
      } if $type eq "editing";
}; # end PrintEDIT_COMPLETE

###################### Procedure PrintRUN_COMPLETE ######################
#
# This procedure prints a page that refreshes while
# the script is running.  An empty $log_name parameter indicates
# that we have not started yet.
#
sub PrintRUN_COMPLETE {

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);


  $sh = $dbh->prepare( qq{
    BEGIN
      MEME_RANKS.set_ranks(
         classes_flag => 'Y',
         attributes_flag => 'N',
         relationships_flag => 'N');
    END;
    }) ||
    ((print "<span id=red>Error preparing set ranks  ($DBI::errstr).</span>")
     &&  return);
  $sh->execute || 
    ((print "<span id=red>Error executing set ranks  ($DBI::errstr).</span>")
     &&  return);

     # verify the process was finished
        print qq{
            The rank is set successfully. 
	Click <a href="$cgi?state=EDIT_INDEX&db=$db&type=editing">here</a> to return to the list page.<BR>
      };

}; # end PrintRUN_COMPLETE


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
  command       :  Some states (EDIT_*) require the use of a command
                   to know whether to insert, clone, modify or delete a
                   code
  db            :  Database name
  meme_home     :  Value of $MEME_HOME that the script should use.
             
  All other parameters are values used for editing meme properties.  
  They roughly match the fields in the tables that manage meme properties. 
  They include:
      category, code, value, description,

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


