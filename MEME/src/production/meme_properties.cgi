#!@PATH_TO_PERL@
#
# File:     meme_properties.cgi
# Author:   Tun Tun Naing
# 
# Dependencies:  This file requires the following:
#      /d5/MEME4/cgi-lti/meme_utils.pl
#      /d5/MEME4/cgi-lti/bin/meme_properties.pl
#
# Description:
#
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version info:
#
# 05/20/2003 1.0: Release version
#
$release = "4";
$version = "1.0";
$version_authority = "BAC";
$version_date = "05/20/2003";

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
		  "state" => 1, "rowid" => 1, "category" => 1,
		  "command" => 1, "key" => 1, "value" => 1,
		  "description" => 1, "db" => 1, 
		  "meme_home" => 1);

#
# Set environment variables after parsing arguments
#
$ENV{"ORACLE_HOME"} = "/d1/app/oracle/product/8.1.7" unless $ENV{"ORACLE_HOME"};

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
$ENV{"MEME_HOME"} = $meme_home || $ENV{"MEME_HOME"} || "/d5/MEME4/cgi-lti";
$ENV{"ORACLE_HOME"} = $oracle_home || $ENV{"ORACLE_HOME"} || "/d1/app/oracle/product/8.1.7";


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
# 2. LIST_DATA. Print the list of meme properties.
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
	         ["PrintHeader","PrintINDEX","PrintFooter","MEME Properties - Index Page","MEME Properties"],
	     "LIST_DATA" => 
	         ["PrintHeader","PrintLIST_DATA","PrintFooter","Cateogry List","List of <i>$category</i> Properties"],
	     "EDIT_INDEX" => 
	         ["PrintHeader","PrintEDIT_INDEX","PrintFooter","Edit MEME properties","Edit MEME properties"],
	     "EDIT_COMPLETE" => 
	         ["PrintHeader","PrintEDIT_COMPLETE","PrintFooter","Edit Complete","$command category <i>$category</i>"],
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
			               <input type="hidden" name="state" value="INDEX">
			               <select name="db" onChange="this.form.submit(); return true;">
			                  <option>-- SELECT A DATABASE --</option>\n};
    foreach $db (sort split /,/, $dbs) {
      $db_select_form .= "			                  <option $selected{$db}>$db</option>\n";
      $databases{$db} = $db;
    }
    $db_select_form .= "			                </select>\n</form>";

    $cat_select_form = qq{
                                     <form action="$cgi">
			               <input type="hidden" name="state" value="LIST_DATA">
			               <input type="hidden" name="db" value="$db">
			               <select name="category" onChange="this.form.submit(); return true;">\n
			                  <option>-- SELECT A CATEGORY --</option>\n    };

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    # prepare and execute statement
    $sh = $dbh->prepare(qq{
    SELECT DISTINCT key_qualifier FROM meme_properties
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    while (($category) = $sh->fetchrow_array) {
      $enc_cat = &meme_utils::escape($category);
      $cat_select_form .= "			                  <option>$enc_cat</option>\n";
    }
    $cat_select_form .= "			                </select>\n</form>";
   
    # disconnect
    $dbh->disconnect;

    $new_cat_form = qq{
                                     <form action="$cgi">
			               <input type="hidden" name="state" value="EDIT_INDEX">
	                               <input type="hidden" name="db" value="$db">
			               <input type="hidden" name="command" value="Insert">
		                        <input type="text" name="category" >
		                        <input type="submit" value="GO" >
		                    </form>
    };

    print qq{
<i>Select from one of these choices:</i>
<br>&nbsp;
<center><table WIDTH="90%" NOSAVE >
<tr nosave>
<td width="25%">Change Database:</td><td>$db_select_form</td>
</tr>
<tr nosave>
<td width="25%">Select a category to edit:</td><td>$cat_select_form</td>
</tr>
<tr NOSAVE>
<td width="25%">New category</td><td>$new_cat_form</td>
</tr>


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
    if($command eq "Edit") {

    # look up descriptions
    $sh = $dbh->prepare(qq{
    SELECT key_qualifier, key, value, description
    FROM meme_properties WHERE rowid = ?
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute($rowid) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    ($category, $key,$value,$description) =
	   $sh->fetchrow_array; 
  }
    # Print the forms
    print qq{
<br>&nbsp;
    <form method="GET" action="$cgi">
      <input type="hidden" name="state" value="EDIT_COMPLETE">
      <input type="hidden" name="db" value="$db"> 
      <input type="hidden" name="command" value="">
      <input type="hidden" name="rowid" value="$rowid">
    };
  if($command eq "Insert") {
    print qq {
      <input type="hidden" name="category" value="$category">
    };
  }
  print qq {
      <center><table CELLPADDING=2 WIDTH="90%" NOSAVE >
      <tr NOSAVE>
	<td width="25%">Category:</td><td>
  };
  if($command eq "Insert") {
    print qq {
	      $category
};
  } else {
    $selected{$category} = "SELECTED";
    # look up category
    $sh = $dbh->prepare(qq{
    SELECT distinct key_qualifier
    FROM meme_properties
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);
    print qq {<select name="category">\n };
    while (($cat) = $sh->fetchrow_array) {     
      print qq { <option $selected{$cat}>$cat</option>\n };
    }
    print "</select>\n";
	      
  }
  print qq {
	     </td>	     
      </tr>
      <tr NOSAVE>
	<td width="25%">Key:</td><td>
	     <input type="text" name="key" value = "$key">
	     </td>	     
      </tr>
      <tr NOSAVE>
	<td width="25%">Value:</td><td>
	     <textarea name="value" wrap="soft" cols="60" rows="2">$value</textarea>
	     </td>	     
      </tr>
      <tr NOSAVE>
	<td align="left" width="25%">Description:</td>
	     <td><textarea name="description" wrap="soft" cols="60" rows="4">$description</textarea></td>
      </tr>
      <tr NOSAVE>
        <td colspan=2>
	  <center></b>
          <font size="-1">
	  <input type="button" value="Insert"
	      onMouseOver='window.status="Insert New Category"; return true;'
              onClick=' this.form.command.value="Insert"; this.form.submit(); return true;'>
};

    if($command eq "Edit") { 
    print qq{
          &nbsp;&nbsp;&nbsp;
	  <input type="button" value="Update"
	      onMouseOver='window.status="Update selected Property"; return true;'
              onClick='this.form.command.value="Update"; this.form.submit(); return true;'>
          &nbsp;&nbsp;&nbsp;
	  <input type="button" value="Delete"
	      onMouseOver='window.status="Delete selected Category"; return true;'
              onClick='this.form.command.value="Delete"; this.form.submit(); return true;'>
};
  }
    print qq{
          &nbsp;&nbsp;&nbsp;
           <input type="button" value="Cancel" onClick="history.go(-1)"></b>
          </font></b>
	  </center>
	</td>
      </tr>
    </table>
    </center>
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

    if ($command eq "Insert") {

      # Insert code 

      # Verify that no rows with this key name already exist
      #$row_count = $dbh->do( qq{
      #  UPDATE meme_properties
      #	SET value = ?
      #  WHERE key_qualifier = ? 
      #	AND key = ?},
      #  {"dummy"=>1}, $value, $category, $key );
      #
      #if ($row_count > 0) {
      #  print qq{<b>You may not $command a category with the key <i>$key</i> because one already exists.  Please go back and choose a different key.</b> };  
      #  return;
      #}     

      # insert a row
      $row_count = $dbh->do( qq{
        INSERT INTO meme_properties
        (key_qualifier,key,value,description)
        VALUES (?,?,?,?)
      }, {"dummy"=>1}, $category, $key, $value, $description);

      # check for error
      (! $DBI::err) || ((print "<span id=red>Error Inserting row ($DBI::errstr).</span>")
			&&  return);


      # Print response
      print qq{
        A category with the following information was inserted:<p>
	};
      &PrintCategoryInfo;

    } elsif ($command eq "Delete") {

     # Delete code

     # delete row
      $row_count = $dbh->do( qq{
        DELETE FROM meme_properties WHERE rowid= ? },
        {"dummy"=>1},$rowid);

     # check for error
      (! $DBI::err) || ((print "<span id=red>Error deleting row ($category,($DBI::errstr).</span>")
			&&  return);
 
     # verify row was deleted
      if ($row_count != 1) {
        print qq{ 
           There was a problem deleting the key <i>$key</i>.
	       $row_count rows were deleted instead of 1.
};
        return;
      } else {
        print qq{
           The key <i>$key</i> was successfully deleted.<p>
	Click <a href="$cgi?state=LIST_DATA&db=$db&category=$category">here</a> to return to the category list page.
}
      };

    } elsif ($command eq "Update") {

     # Update code here

      #update row
      $row_count = $dbh->do( qq{
        UPDATE meme_properties SET
            key_qualifier = ?,
            key = ?,
            value = ?,
            description = ? 
        WHERE rowid = ? }, {"dummy"=>1},
        $category, $key, $value, $description, $rowid);
          
      # check for error
      (! $DBI::err) || ((print "<span id=red>Error updating row ($key,($DBI::errstr).</span>")
			&&  return);
          
      # verify row was updated
      if ($row_count != 1) {
        print qq{
           There was a problem updating the key <i>$key</i>. 
	   Category:    $category
	   Key:         $key
	   Value:       $value
	   Description: $description
$row_count rows were updated instead of 1.
};        
        return;
      }
      # Print response
      print qq{
        The key <i>$key</i> was modified using the following information:<p> 
	};
      &PrintCategoryInfo;
    }
    $dbh->do(qq {
          BEGIN
	   MEME_UTILITY.report_table_change(
	       authority => 'MTH',
               table_name => 'meme_properties',
               query_table => 'meme_properties',
	       command => 'truncate' );
	   
          END; }) || ((print "<span id=red>Error reporting table change event ($$key,($DBI::errstr).</span>")
			&&  return);

}; # end PrintEDIT_COMPLETE


###################### Procedure PrintLIST_DATA ######################
#
# This procedure prints the list of keys in category
#
sub PrintLIST_DATA {
    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    # prepare and execute statement
    $sh = $dbh->prepare(qq{
    SELECT rowid, key_qualifier, key, value, description
    FROM meme_properties
    WHERE key_qualifier = ?
    ORDER BY key_qualifier, key,value
    }) || 
    ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute($category) || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    print qq{ <i> Click <a href="$cgi?state=EDIT_INDEX&db=$db&command=EditNew&category=$category">here</a> to add a new key in this category.</i>};
    print qq{
    <blockquote>
    $mc_text
    <br>&nbsp;<br>
    <form method="GET" action="$cgi">
      <input type="hidden" name="state" value="EDIT_INDEX">
      <input type="hidden" name="db" value="$db"> 
      <input type="hidden" name="command" value="Edit"> 
      <input type="hidden" name="rowid" value=""> 
      <center><table CELLPADDING=2 WIDTH="90%" BORDER=1 NOSAVE >
      <COLGROUP span="2" width="10%">
      <COLGROUP span="2" width="40%">
    <tr><td valign="top"><b><font size="-1">Category</font></b></td>
           <td valign="top"><b><font size="-1">Key</font></b></td>
           <td valign="top"><b><font size="-1">Value</font></b>
           <td valign="top"><b><font size="-1">Description</font></b></td>
           <td valign="top">&nbsp;</td>
    </tr>
};

    # fetch the data & print to screen
    while (($rowid,$category,$key,$value,$description) =
	   $sh->fetchrow_array) {
        $Text::Wrap::break = ",";
        $Text::Wrap::columns = 40;
        $value = wrap("","",$value);
        print "      <tr>";
        print qq { <td valign="top">$category</td> <td valign="top">$key</td> <td valign="top">$value</td> 
		   <td valign="top">$description</td> 
		   <td valign="top">
	  <input type="button" value="Edit"
	      onMouseOver='window.status="Edit Property"; return true;'
              onClick=' this.form.rowid.value="$rowid"; this.form.submit(); return true;'> </td>
        };
        print "</tr>\n";
    }


    # end the table
    print qq{        
    </table></center>
    </blockquote>\n
    };
}; # end PrintLIST_DATA

###################### Procedure PrintCategoryInfo ######################
#
# This procedure prints info for a category.  It uses global variables
# so make sure they are set.
#
sub PrintCategoryInfo {

      # Print response
      print qq{
        <center>
        <table width="90%">
          <tr><td width="25%" valign="top"><font size="-1"><b>Category:</b></font></td>
          <td><font size="-1">$category</font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Key:</b></font></td>   
          <td><font size="-1">$key</font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Value:</b></font></td>   
          <td><font size="-1">$value</font></td></tr>
          <tr><td width="25%" valign="top"><font size="-1"><b>Description:</b></font></td>
          <td><font size="-1">$description</font></td></tr>
        </table>
        </center><p>
	Click <a href="$cgi?state=LIST_DATA&db=$db&category=$category">here</a> to return to the category list page.
};   
}; # end PrintCategoryInfo

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
                   key
  db            :  Database name
  meme_home     :  Value of $MEME_HOME that the script should use.
             
  All other parameters are values used for editing meme properties.  
  They roughly match the fields in the tables that manage meme properties. 
  They include:
      category, key, value, description,

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


