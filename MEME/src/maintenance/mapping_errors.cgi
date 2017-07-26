#!@PATH_TO_PERL@
# File:     mapping_errors.cgi
# Author:   Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Changes
# 01/30/2006 BAC (1-769EW): support for ROOT_SOURCE_AUI
#
$release = "4";
$version = "0.5";
$version_authority = "BAC";
$version_date = "12/27/2004";

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
		  "state" => 1, "db" => 1, "table" => 1,
		  "suffix" => 1, "max_results" => 1, "sg_type" => 1,
		  "meme_home" => 1, "oracle_home" => 1);

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
	         ["PrintHeader","PrintINDEX","PrintFooter","Insertion Research","Research Unmapped Identifiers"],
	     "LIST_TYPES" => 
	         ["PrintHeader","PrintLIST_TYPES","PrintFooter","Problematic Map Types","List of Problematic Map Types"],
	     "LIST_CASES" => 
	         ["PrintHeader","PrintLIST_CASES","PrintFooter","Problematic Map Type Examples","List of Unmapped identifiers ($sg_type)"],
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
	<table BORDER=0 COLS=2 WIDTH="100%"  >
	  <tr >
	    <td ALIGN=LEFT VALIGN=TOP >
	      <address><a href="$cgi?db=$db">Back to Index</a></address>
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
    $selected{$table}="SELECTED";
    $selected{max_results}="SELECTED";
    $dbs = &meme_utils::midsvcs("databases");
    $mrd = &meme_utils::midsvcs("mrd-db");
    $dbs = "$dbs,$mrd";
    $db_select_form = qq{
			               <select name="db">
			                  <option>-- SELECT A DATABASE --</option>\n};
    foreach $db (sort split /,/, $dbs) {
      $db_select_form .= "			                  <option $selected{$db}>$db</option>\n";
      $databases{$db} = $db;
    }
    $db_select_form .= "			                </select>\n";
    $table_select_form = qq{    
 		                 <select name="table">
			           <option>-- SELECT A TABLE --</option>
			           <option $selected{mom_precomputed_facts}>mom_precomputed_facts</option>
			           <option $selected{source_attributes}>source_attributes</option>
			           <option $selected{source_context_relationships}>source_context_relationships</option>
			           <option $selected{source_relationships}>source_relationships</option>
                                 </select>
};
    $mr_select_form = qq{    
 		                 <select name="max_results">
			           <option $selected{100}>100</option>
			           <option $selected{500}>500</option>
			           <option $selected{1000}>1000</option>
			           <option $selected{5000}>5000</option>
			           <option value="-1" $selected{-1}>Unlimited</option>
                                 </select>
};
   
    print qq{
This is a source insertion assistance tool for helping
to understand why certain identifiers among the source
attributes, relationships, or context relationships could
not be mapped to meme identifiers.  This first stage
will help you in finding which types of identifiers are 
problematic.
<br>&nbsp;
<form action="$cgi">
   <input type="hidden" name="state" value="LIST_TYPES">
<center><table WIDTH="90%"  >
<tr >
<td width="25%">Database:</td><td>$db_select_form</td>
</tr>
<tr >
<td width="25%">Table:</td><td>$table_select_form</td>
</tr>
<tr >
<td width="25%">Max Results:</td><td>$mr_select_form</td>
</tr>
<tr >
<td width="25%" colspan="2"><input type="submit" value="SUBMIT"></td>
</tr>
</table></center>
</form>

<p>
    };
}; # end PrintINDEX

###################### Procedure PrintLIST_TYPES ######################
#
sub PrintLIST_TYPES {

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);

    if ($table eq "source_attributes") {
      $query = "SELECT distinct sg_type,null FROM $table WHERE nvl(atom_id,0) = 0";
    } else {
      $query = qq{
SELECT distinct sg_type_1,'_1' FROM $table WHERE nvl(atom_id_1,0) = 0
UNION
SELECT distinct sg_type_2,'_2' FROM $table WHERE nvl(atom_id_2,0) = 0};
    }
   
    # look up descriptions
    $sh = $dbh->prepare($query) || 
      ((print "<span id=red>Error preparing query ($DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    # Print the forms
    print qq{
Following are the problematic types.  Select one to review cases.
<br>&nbsp;
      <center><table cellpadding=2 width="90%" >
};
    
    while (($sg_type,$suffix) = $sh->fetchrow_array) {
      print qq{
  <tr>
    <td>
      <a href="$cgi?db=$db&state=LIST_CASES&max_results=$max_results&table=$table&sg_type=$sg_type&suffix=$suffix">
        $sg_type
      </a>}.($suffix ? " (from sg_type$suffix)" : "").qq{
    </td>
  </tr>
}
  
    }
    print qq{
    </table>
    </center>
  </form>
</p>
};

}; # end PrintLIST_TYPES


###################### Procedure PrintLIST_CASES ######################
#
sub PrintLIST_CASES {

    # set variables
    $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
    ($user,$password) = split /\//, $userpass;
    chop($password);

    # open connection
    $dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password") ||
    ((print "<span id=red>Error opening $db ($DBI::errstr).</span>")
             &&  return);


    #
    # SOURCE_ATOM_ID Case
    #
    if ($sg_type eq "SOURCE_ATOM_ID" || $sg_type eq "SRC_ATOM_ID") {
      $query = qq{
(SELECT sg_id$suffix,null, 'No match' FROM 
  (SELECT to_number(sg_id$suffix) sg_id$suffix FROM $table 
   WHERE sg_type$suffix = '$sg_type'
   MINUS
   SELECT source_row_id FROM source_id_map WHERE table_name = 'C'))
UNION ALL
SELECT a.source_row_id, null, 'Too many matching values'
FROM source_id_map a, $table b
WHERE b.sg_id$suffix = a.source_row_id AND a.table_name='C'
  AND b.sg_type$suffix = '$sg_type' 
GROUP BY source_row_id HAVING count(distinct local_row_id)>1
};


    #
    # CUI Case
    #
    } elsif ($sg_type eq "CUI") {
      $query = qq{
SELECT sg_id$suffix,null, 'No match' FROM 
  (SELECT sg_id$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT last_release_cui FROM classes)
};


    #
    # AUI
    #
    } elsif ($sg_type eq "AUI") {
      #
      # AUI doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, '', 'No match' FROM 
  (SELECT sg_id$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT aui FROM classes)
};

    #
    # SOURCE_CUI Case
    #
    } elsif ($sg_type eq "SOURCE_CUI") {
      #
      # SOURCE_CUI doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, sg_qualifier$suffix, 'No match' FROM 
  (SELECT sg_id$suffix, sg_qualifier$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT source_cui, source FROM classes)
};



    #
    # ROOT_SOURCE_AUI Case
    #
    } elsif ($sg_type eq "ROOT_SOURCE_AUI") {
      #
      # SOURCE_AUI doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, sg_qualifier$suffix, 'No match' FROM 
  (SELECT sg_id$suffix, sg_qualifier$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT source_aui, b.source 
   FROM classes a, source_version b
   WHERE current_name = a.source)
   UNION
SELECT a.sg_id$suffix, a.sg_qualifier$suffix, 'Too many matching values'
FROM $table a, classes b, source_version c
WHERE a.sg_id$suffix = b.source_aui 
  AND b.source = c.current_name
  AND a.sg_type$suffix = '$sg_type' 
  AND a.sg_qualifier$suffix = c.source
GROUP BY a.sg_id$suffix, a.sg_qualifier$suffix HAVING count(distinct b.atom_id)>1
};

    #
    # ROOT_SOURCE_CUI Case
    #
    } elsif ($sg_type eq "ROOT_SOURCE_CUI") {
      #
      # SOURCE_CUI doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, sg_qualifier$suffix, 'No match' FROM 
  (SELECT sg_id$suffix, sg_qualifier$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT source_cui, b.source 
   FROM classes a, source_version b
   WHERE current_name = a.source)
};


    #
    # SOURCE_RUI Case
    #
    } elsif ($sg_type eq "SOURCE_RUI") {
      #
      # SOURCE_RUI doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, sg_qualifier$suffix, 'No match' FROM 
  (SELECT sg_id$suffix, sg_qualifier$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   (SELECT source_rui, source FROM relationships WHERE source_rui IS NOT NULL
    SELECT source_rui, source FROM context_relationships WHERE source_rui IS NOT NULL)
};

    #
    # ROOT_SOURCE_RUI Case
    #
    } elsif ($sg_type eq "ROOT_SOURCE_RUI") {
      #
      # SOURCE_RUI doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, sg_qualifier$suffix, 'No match' FROM 
  (SELECT sg_id$suffix, sg_qualifier$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   (SELECT source_rui, b.source 
    FROM relationships a, source_version b
    WHERE current_name = a.source
      AND source_rui is not null
    UNION
    SELECT source_rui, b.source 
    FROM context_relationships a, source_version b
    WHERE current_name = a.source
      AND source_rui is not null)
  )
};

    #
    # CODE_TERMGROUP
    #
    } elsif ($sg_type eq "CODE_TERMGROUP") {
      #
      # CODE_TERMGROUP doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, sg_qualifier$suffix, 'No match' FROM 
  (SELECT sg_id$suffix, sg_qualifier$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT a.code, a.termgroup
   FROM classes a
   WHERE a.tobereleased in ('Y','y'))
};

    #
    # CODE_ROOT_TERMGROUP
    #
    } elsif ($sg_type eq "CODE_ROOT_TERMGROUP") {
      #
      # CODE_ROOT_TERMGROUP doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, sg_qualifier$suffix, 'No match' FROM 
  (SELECT sg_id$suffix, sg_qualifier$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT a.code, b.source || '/' || a.tty
   FROM classes a, source_version b
   WHERE b.current_name = a.source
     AND a.tobereleased in ('Y','y'))
};


    #
    # CUI
    #
    } elsif ($sg_type eq "CUI") {
      #
      # CUI doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, '', 'No match' FROM 
  (SELECT sg_id$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT last_release_cui
   FROM classes)
};

    #
    # CUI_SOURCE
    #
    } elsif ($sg_type eq "CUI_SOURCE") {
      #
      # CUI_SOURCE doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, sg_qualifier$suffix, 'No match' FROM 
  (SELECT sg_id$suffix, sg_qualifier$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT last_release_cui, source
   FROM classes)
};

    #
    # CUI_ROOT_SOURCE
    #
    } elsif ($sg_type eq "CUI_ROOT_SOURCE") {
      #
      # CUI_ROOT_SOURCE doesn't have a "too many" condition
      # because we map to the highest ranking atom
      #
      $query = qq{
SELECT sg_id$suffix, sg_qualifier$suffix, 'No match' FROM 
  (SELECT sg_id$suffix, sg_qualifier$suffix FROM $table
   WHERE sg_type$suffix = '$sg_type' 
   MINUS
   SELECT last_release_cui, b.stripped_source
   FROM classes a, source_rank b
   WHERE a.source = b.source)
};

    #
    # Not found
    #
    } else {
      print qq{Cannot handle this type ($sg_type).}; 
      return; 
    }

    $sh = $dbh->prepare($query) || 
      ((print "<span id=red>Error preparing query ('$query' $DBI::errstr).</span>")  
             &&  return);

    $sh->execute || 
    ((print "<span id=red>Error executing query ($DBI::errstr).</span>")
             &&  return);

    # Print the forms
    print qq{
The following identifiers cannot be mapped. }.
( ($max_results != -1) ? "Results will be limited to at most $max_results entries." : "").
qq{ 
<br>&nbsp;
      <center><table cellpadding=2 width="90%" >
  <tr><th>SG_ID</th><th>SG_TYPE</th><th>SG_QUALIFIER</th><th>Reason for Error</th></tr>
};
    $ct=0;
    while (($sg_id, $sg_qualifier, $reason) = $sh->fetchrow_array) {
      if ($max_results != -1) {
	last if $ct > $max_results;
      }
      $ct++;
	
      print qq{
  <tr>
    <td>$sg_id</td><td>$sg_type</td><td>$sg_qualifier</td><td>$reason</td>
  </tr>
}
  
    }

    # 
    # inform if nothing found
    ##
    unless ($ct) {
      print qq{<tr><td>
No invalid cases with $sg_type found.  
These are likely things that just 
have not been mapped yet.</td></tr>
};            
    }
    print qq{
    </table>
    </center>
  </form>
</p>
};
    $dbh->disconnect;
}; # end PrintLIST_CASES


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
  sg_type       :  The type of id to investigate
  table         :  The table to look up bad cases in
  max_results   :  When looking up examples, the limit  
  table         :  
  db            :  Database name
  meme_home     :  Value of $MEME_HOME that the script should use.
             
  All other parameters are values used for editing meme properties.  
  They roughly match the fields in the tables that manage meme properties. 
  They include:
      category, key, value, description,

  Version: $version, $version_date ($version_authority)

};      
} # End Procedure PrintHelp


