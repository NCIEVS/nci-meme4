#!@PATH_TO_PERL@
#
# File:     src_report.cgi
# Author:   Brian Carlsen
# 
# Dependencies:  This file requires the following:
#      meme_utils.pl
#
# Description:
#
# Changes:
# 02/29/2008 BAC (1-DG8I1): Use SrcReport.pm instead of system call, index data if not indexed.
# 02/27/2008 BAC (1-DG8I1): first version
#

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use FindBin qw($Bin);
use lib "$Bin/../lib";

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
    elsif ($arg =~ /-{1,2}help$/) {
	$print_help=1;
    }
}


&PrintHelp && exit(0) if $print_help;

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
   "type" => 1, "id" => 1, "sab" => 1, "page" => 1, "SRC_ROOT" => 1
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
use SrcReport;
use Text::Wrap;

#
# Initial CGI param settings
#
our $state = "INDEX";
our $type = "SAID";
our $sab = "";
our $id;
our $page = 0;

#
# Set Other variables, including:
#   style sheet, cgi location, sql command, unedited MID, current MID
# 
our $cgi = $ENV{"SCRIPT_NAME"};
our $db = &meme_utils::midsvcs("production-db") unless $db;
our $date = `/bin/date +%Y%m%d`;
our $start_time = time;
our $style_sheet = &meme_utils::getStyle;
our $FATAL = 1;
our $range = 30 unless $range;
our $SRC_ROOT = "/umls_prod/src_root" unless $ENV{SRC_ROOT};
our $SRC_ROOT = $ENV{SRC_ROOT} unless $SRC_ROOT;
our $pagesize = 50;
our $dir = "";

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
# Determine Which action to run, print the body
# Valid STATES:
#
# 1. INDEX. Form and report

#
# The states array maps commands to procedures for
# printing the header, body, and footer
#
our %states = (
	     "INDEX" => 
	         ["PrintHeader","PrintINDEX","PrintFooter","SRC Reporter","SRC Reporter $sab $type"],
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
    my $header = $states{$state}->[0];
    my $body = $states{$state}->[1];
    my $footer = $states{$state}->[2];

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
    my $end_time=time;
    my $elapsed_time = $end_time - $start_time;

    #
    # Print the Footer
    #
    print qq{
    <hr width="100%">
	<table BORDER=0 COLS=2 WIDTH="100%"  >
	  <tr >
	    <td ALIGN=LEFT VALIGN=TOP >
	      <address><a href="$cgi">Back to Index</a></address>
            </td>
	    <td ALIGN=RIGHT VALIGN=TOP >
	      <font size="-1"><address>Contact: <a href="mailto:brian.a.carlsen\@lmco.com">Brian Carlsen</a></address>
	      <address>Generated on:},scalar(localtime),qq{</address>
              <address>This page took $elapsed_time seconds to generate.</address>
	      <address>};
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
    my %selected;
    $selected{$type}="SELECTED";
    $selected{$sab}="SELECTED";
    print qq{
Choose a source, a type, and a value.  Leave the value blank to see a list of values for the specified type.<br>
<form action="$cgi" method="GET">
   <select name="sab">
};
    my @files = `ls $SRC_ROOT`;
    chomp(@files);
    my $file;
    foreach $file (@files) {
		if ($file =~ /^[A-Z]/) {
    	    my $needs_indexes = "";
        	if (!(-e "$SRC_ROOT/$file/tmp/indexes/classes_atoms.CODE.x")) {
            	$needs_indexes = " (needs indexes)";
	        }
	    	print qq{<option $selected{$file} value="$file">$file$needs_indexes</option>\n};
		}
    }

    print qq{
   </select>
   <select name="type">
     <option $selected{SAID}>SAID</option>
     <option $selected{CODE}>CODE</option>
     <option $selected{SAUI}>SAUI</option>
     <option $selected{SCUI}>SCUI</option>
     <option $selected{SDUI}>SDUI</option>
     <option $selected{STR} value="STR">Lowercase STR</option>
   </select>
   <input type="text" name="id" value="$id" size="40" /><br>
   <input type="submit" />
</form>

};

    # Handle null id case (show page of ids)
    if ($type && (!$id || $id =~ /\*$/) && $sab) {
        # Ensure data is indexed
        SrcReport::setDir("$SRC_ROOT/$sab/src");
        SrcReport::indexData();

        print "[ ";
        print qq{<a href="$cgi?id=$id&sab=$sab&page=}.($page+1).qq{&type=$type">Next Page</a>};
        print qq{ (<a href="$cgi?id=$id&sab=$sab&page=}.($page+10).qq{&type=$type">+10</a>) };
        print qq{ (<a href="$cgi?id=$id&sab=$sab&page=}.($page+100).qq{&type=$type">+100</a>) };
        print qq{ (<a href="$cgi?id=$id&sab=$sab&page=}.($page+1000).qq{&type=$type">+1000</a>) };
        if ($page > 0) {
            print " / ";
            print qq{<a href="$cgi?id=$id&sab=$sab&page=}.($page-1).qq{&type=$type">Prev Page</a>};
        }
        print " ]";
        my $index = $pagesize * $page;
        my $INDEXFH;
        if ($type ne "SAID") {
            open ($INDEXFH, "<:utf8", "$SRC_ROOT/$sab/tmp/indexes/classes_atoms.$type.x")
                || (print "Error opening index file: classes_atoms.$type.x\n" && return);
        } else {
            open ($INDEXFH, "<:utf8", "$SRC_ROOT/$sab/src/classes_atoms.src")
                || (print "Error opening file: classes_atoms.src\n" && return);
        }
        my $ct = 0;
        print "<ul>";

        if ($id =~ /\*$/) {
            my $prev_id = "";
            my @lines = SrcReport::getMatchingLines($INDEXFH,$id);
            my $line;
            foreach $line (@lines) {
                ($lid) = split /\|/, $line;
                next if $lid eq $prev_id;
                $prev_id = $lid;
                next if $ct++ < $index;
                print qq{<li><a href="$cgi?type=$type&id=}.&meme_utils::escape($lid).qq{&sab=$sab">$lid</a></li>};
                last if $ct == ($index+$pagesize);
            }
            if (!$prev_id) {
                print "NO RESULTS FOUND FOR $type\n";
            }

        } else {

            my $prev_id = "";
            while (<$INDEXFH>) {
                my ($lid) = split /\|/;
                next if $lid eq $prev_id;
                $prev_id = $lid;
                next if $ct++ < $index;
                print qq{<li><a href="$cgi?type=$type&id=}.&meme_utils::escape($lid).qq{&sab=$sab">$lid</a></li>};
                last if $ct == ($index+$pagesize);
            }
            if (!$prev_id) {
                print "NO RESULTS FOUND FOR $type\n";
            }
        }
        close($INDEXFH);
        print "</ul>\n";
        print "[ ";
        print qq{<a href="$cgi?id=$id&sab=$sab&page=}.($page+1).qq{&type=$type">Next Page</a>};
        print qq{ (<a href="$cgi?id=$id&sab=$sab&page=}.($page+10).qq{&type=$type">+10</a>) };
        print qq{ (<a href="$cgi?id=$id&sab=$sab&page=}.($page+100).qq{&type=$type">+100</a>) };
        print qq{ (<a href="$cgi?id=$id&sab=$sab&page=}.($page+1000).qq{&type=$type">+1000</a>) };
        if ($page > 0) {
            print " / ";
            print qq{<a href="$cgi?id=$id&sab=$sab&page=}.($page-1).qq{&type=$type">Prev Page</a>};
        }
        print " ]";
        
    # Handle generating an actual report
    } elsif ($type && $id && $sab) {
	print qq{<pre>};
	SrcReport::setDir("$SRC_ROOT/$sab/src");
	SrcReport::setScript($cgi);
	SrcReport::setVsab($sab);
	SrcReport::cacheData();
	SrcReport::indexData();
	SrcReport::openFiles();
	SrcReport::setType($type);
	SrcReport::printReport($id);
	print qq{</pre>};
    }

}; # end PrintINDEX



################################# PROCEDURES #################################
#
#  The following are useful procedures
#
#############################################################################

###################### Procedure PrintHelp ######################
#
# This prints help information from the command line
#
sub PrintHelp {
    print qq{
Generates an SRC report.

 Parmaters:
  sab           :  source in /umls_prod/src_root to generate for
  type          :  id type
  id            :  id
             
  All other parameters are values used for editing meme properties.  
  They roughly match the fields in the tables that manage meme properties. 
  They include:
      category, key, value, description,

};      
} # End Procedure PrintHelp


