#!@PATH_TO_PERL@

# Prints a attributes using the database

# CGI parameters:
# action={
#   searchbycui|searchbyconceptid|searchbyatomid|searchbysourcerowid|
# color={1}
# nocolor={1}
# font=<name>
# fontsize=<size>
# meme_home=<the path to the MEME_HOME environment variable, for debug>
# db=<the database to search in - default is editing-db>
# service=<Use a different MID service for the TNS name>
# meme_server=<the MID service for the MEME server params, e.g., meme-server or test-meme-server>
# user=<set to wth, don't use>
# arg=<the argument value, e.g., concept_id or string to search>
# source=<array of sources to restrict norm-based searches to>
# rslimit=<only display these many records from the result of a norm-based search>
# ignorerellimit=<Ignore rel limit and get report> rel limit is set to 250
# format=text|html
# refreshsourcecache=

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use OracleIF;
use EMSUtils;
use Midsvcs;
require "conceptutils.pl";
require "utils.pl";
use LVG;

$now = GeneralUtils->date;

# set the defaults from config file
#$access = @{ $config{"access"}}->[0]; $access =~ tr/A-Z/a-z/;
$fontsize="+0";
$fontsize="-1";

use CGI;

$query = new CGI;

$ENV{EMS_CONFIG} = $query->param('config') if $query->param('config');
EMSUtils->loadConfig;
$user = $main::EMSCONFIG{ORACLE_USER};
# maps a prompt to an action
%action_map = (
  "Search by Identifier" => "cuisearchform",
  "Normalized String Search" => "normstrsearchform",
  "Approximate Word Search" => "normwordsearchform",
  "Random Concepts" => "randomconcepts"
);


# ORACLE vars
$db = $query->param('db');
$db = "" if $db eq $na;
$db = $db || Midsvcs->get($query->param('service') || 'editing-db');
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF({user=>$user, password=>$password, db=>$db});
unless ($dbh) {
  &print_html("Sorry, the Oracle database: $db is not available at the current time.");
  exit 0;
}

#modify for time format, alter oracle session
$dbh->executeStmt("alter session set NLS_DATE_FORMAT='dd-MON-yyyy hh24:mi:ss'");
$dbh->executeStmt("ALTER SESSION set sort_area_size=200000000");
$dbh->executeStmt("ALTER SESSION set hash_area_size=200000000");

# Environment
$cgi = $ENV{'SCRIPT_NAME'};

$emsURL = $main::EMSCONFIG{LEVEL0EMSURL};
$wmsURL = $main::EMSCONFIG{LEVEL0WMSURL};

# file containing all sources
$allsources = "-- All Sources --";

# fonts
$fontsize = $query->param('fontsize') || $fontsize;
$fontfamily = $query->param('font-family');
# font-family : Verdana, Arial, Helvetica, Sans-serif;

$format = $query->param('format') || "html";

# from CERT
$OK_CHARS='a-zA-Z0-9_.'; # things that are allowed in type-ins

$midsvcs = Midsvcs->load;
$lvghost = $midsvcs->{'lvg-server-host'};
$lvgport = $midsvcs->{'lvg-server-port'};


$cgi = ($ENV{'SCRIPT_NAME'} || "/cgi-bin/concept-report-mid.pl");
$cgiraw = ($ENV{'SCRIPT_NAME'} || "/cgi-bin/concept-report-mid.pl");
$conceptReportRelease = "/cgi-bin/concept-report-mid.pl";
#$conceptReportRelease = "/cgi-bin/ems2-release-cgi.pl";
$filestosearch="filestosearch=mrcon,mrso,mrsty,mrrel,mrsat,mrlo,mrdef,mrcxt,mratx";

$meme_home = $query->param('meme_home') || $ENV{MEME_HOME};
$meme_server = $query->param('meme_server') || "meme-server";

$action = $action_map{$query->param('actionmap')} if (!$query->param('action') && $query->param('actionmap'));
$action = $query->param('action') unless $action;

unless ($action eq "randomconcepts") {
    if ($format eq "text") {
	&print_cgi_header("text/plain");	
    } else {
	&print_cgi_header("text/html");	
    }
}

if ($access eq "deny") {
    my($comment) = join(' ', @{ @{ $config{"access"} }->[1] });

    &print_html("Attribute Report from MID Unavailable", <<"EOD");
Attribute reports from the MID are unavailable as of <EM>$now</EM>.  Please try again later.
<P>
$comment
EOD
    &print_trailer;
    exit(0);
}

$searchtime = time;

unless ($dbh) {
    &print_html("Error: Oracle Database Not Available", <<"EOD");
<EM>Oracle database: $db not available.</EM>
EOD
    exit 0;
}

if ($action) {

    if ($action eq "searchbycui") {
	&search_by_cui;
    } elsif ($action eq "searchbyconceptid") {
	&search_by_conceptid;
    } elsif ($action eq "searchbyatomid") {
	&search_by_atomid;
    } elsif ($action eq "cuisearchform") {
        &cui_search_form;
    }

} else {
    &search_form;
}
exit 0;


# UI search form
sub cui_search_form {
    &print_html("Attribute Report from the MID - Search by Identifier", <<"EOD");
<FONT SIZE=$fontsize>
Submitting this form returns a (textual) attribute report for the concept
with the identifier specified.  The identifier can be
a CUI (Concept Unique Identifier), a concept_id (Concept ID)

Examples:
CUI: <A HREF="$cgi?action=searchbycui&arg=C0267170&db=$db&meme_home=$meme_home\#report">C0267170</A>,
concept_id: <A HREF="$cgi?action=searchbyconceptid&arg=1040879&db=$db&meme_home=$meme_home\#report">1040879</A>

</FONT>

<P><HR>
<FORM ACTION="$cgi" METHOD="post">

<FONT SIZE=$fontsize>
Enter ID: <INPUT TYPE="text" NAME="arg">
</FONT>
<BR>
<FONT SIZE=$fontsize>
Treat ID as:
<INPUT TYPE=radio NAME="action" VALUE="searchbycui">CUI
<INPUT TYPE=radio checked NAME="action" VALUE="searchbyconceptid">concept_id
<P>
<INPUT TYPE="submit" VALUE="Get Attribute Report">
<INPUT TYPE="hidden" NAME="db" VALUE="$db">
<INPUT TYPE="hidden" NAME="meme_home" VALUE="$meme_home">
<INPUT TYPE="hidden" NAME="meme_server" VALUE="$meme_server">
</FONT>
</FORM>
EOD
    return;
}
# Basic search form
sub search_form {
  my($servicesHTML);
  my($dbHTML);
  my(%db);

  
  @databases = split /,/, $midsvcs->{databases};
  push @databases, $na;
  $default{databases} = $na;
   %x = ();
  map { $x{$_}++ if /-db$/ } keys %$midsvcs;
  @services = sort keys %x;
  $default{services} = 'editing-db';

  foreach (@services) {
    my($service, $database) = split /\|/, $_;
    next if $service !~ /-db$/;
    $db{$database}++;
    $servicesHTML .= "<OPTION VALUE=\"$service\"" . ($service eq "editing-db" ? " SELECTED" : "") . "><FONT SIZE=$fontsize>$service  [$midsvcs->{$service}]</FONT>";
  }
  $servicesHTML .= "<OPTION VALUE=\"\"><FONT SIZE=$fontsize>n/a</FONT>";

  foreach $datbase (@databases) {
#    next if $db{$datbase};
    $dbHTML .= "<OPTION VALUE=\"$datbase\"><FONT SIZE=$fontsize>$datbase</FONT>";
  }
  $dbHTML .= "<OPTION VALUE=\"\" SELECTED><FONT SIZE=$fontsize>n/a</FONT>";

  $javascript = <<"EOD";
<SCRIPT LANGUAGE="JavaScript">
function clearDB() {
  for (var i=0; i<document.forms[0].db.length; i++) {
    document.forms[0].db.options[i].selected = false;
    if (document.forms[0].db.options[i].value == "") {
      document.forms[0].db.options[i].selected = true;
    }
  }
  return true;
}
function clearService() {
  for (var i=0; i<document.forms[0].service.length; i++) {
    document.forms[0].service.options[i].selected = false;
    if (document.forms[0].service.options[i].value == "") {
      document.forms[0].service.options[i].selected = true;
    }
  }
  return true;
}
</SCRIPT>
EOD

  foreach $m ("meme-server", "test-meme-server") {
    my($host) = $m . "-host";
    my($port) = $m . "-port";
    my($h) = $midsvcs->{$host};
    my($p) = $midsvcs->{$port};
    $memeHTML .= "<OPTION VALUE=\"$m\"><FONT SIZE=$fontsize>$m ($h, $p)</FONT>";
  }

  &print_html("Concept Report from the MID", <<"EOD");
<FONT SIZE=$fontsize>
This interface can be used to query the MID for concept reports.
You can query for concepts using a flexible string matching algorithm or by typing
in the concept identifiers directly.
</FONT>

<P>

$javascript

<FORM ACTION="$cgi" METHOD=post>

<P>

<TABLE CELLSPACING=1 CELLPADDING=10 BORDER=0>

<TR>
<TD>
<FONT SIZE=$fontsize>Select a MID Service:</FONT>
</TD>
<TD>
<FONT SIZE=$fontsize><SELECT NAME="service" OnChange="clearDB();">$servicesHTML</SELECT></FONT>
</TD>
</TR>

<TR>
<TD>
<FONT SIZE=$fontsize>or a database:</FONT>
</TD>
<TD>
<FONT SIZE=$fontsize><SELECT NAME="db" OnChange="clearService();">$dbHTML</SELECT></FONT>
</TD>
</TR>

<TR>
<TD>
<FONT SIZE=$fontsize>MEME server parameters:</FONT>
</TD>
<TD>
<FONT SIZE=$fontsize><SELECT NAME="meme_server">$memeHTML</SELECT></FONT>
</TD>
</TR>

<TR>
<TD VALIGN=top>
<FONT SIZE=$fontsize>
<INPUT TYPE=submit NAME="test" VALUE="Submit">
<INPUT TYPE=hidden NAME="action" VALUE="cuisearchform">

</FONT>
</TD>
<TD>
<FONT SIZE=$fontsize>
Use this if you know the unique identifiers for the concept (CUI or concept_id)
</FONT>
</TD>
</TR>

</TABLE>

<INPUT TYPE=hidden NAME="meme_home" VALUE="$meme_home">
</FORM>
EOD
    return;
}

# Retrieves a report by Concept ID

sub search_by_atomid {
    my($atomid);
    my($tmpfile);
    my($body);
    my($conceptid);
    my(@x);
    my($SQL);
    my(@rows);
    
    $atomid = $query->param('arg');
    $atomid =~ s/^\s*//;
    $atomid =~ s/\s*$//;
    $atomid =~ s/[^$OK_CHARS]/_/go;

    unless ($atomid && $atomid =~ /^[0-9]*$/) {
	&print_html("Missing or Malformed atom_id", <<"EOD");
The atom_id you typed (\"$atomid\") was malformed.  Legitimate atom_id\'s are integers, for example 52076818.
Please refine the query and try again.
EOD
	return;
    }

  my(@cols) = qw(concept_id atom_id atom_name source termgroup code);

# Get matching concept_id
    $SQL = <<"EOD";
SELECT c.concept_id, c.atom_id, a.atom_name, c.source, c.termgroup, c.code FROM classes c, atoms a WHERE
    c.atom_id = a.atom_id AND
    c.atom_id = \'$atomid\'
EOD

   @fields = $dbh->selectFirstAsRef($SQL);
   $w = $dbh->row2ref(@fields, @cols);
   $conceptid=$w->{concept_id};

    unless ($conceptid && $conceptid =~ /^\d+/) {
	&print_html("Concept Report for atom_id: $atomid", <<"EOD");
<EM>No matching concept for atom_id: $atomid found.</EM>
EOD
	return;
    }

    $reporttime = time;
    $report = &report($conceptid);

    if ($format eq "text") {
	print $report;
	return;
    }

    $reporttime = time - $reporttime;
    $reportsecs = ($reporttime == 1 ? "second" : "seconds");

    @cuis = &conceptutils::conceptid2cuis($dbh, $conceptid);
    $cui = $cuis[0];

    $colorCoded = <<"EOD";
<A HREF=\"$cgi?action=searchbyconceptid&arg=$conceptid&color=1&db=$db&meme_home=$meme_home&meme_server=$meme_server\#report\">color-coded</A>
EOD
    $notColorCoded = <<"EOD";
<A HREF=\"$cgi?action=searchbyconceptid&arg=$conceptid&nocolor=1&db=$db&meme_home=$meme_home&meme_server=$meme_server\#report\">not color-coded</A>
EOD

    $searchtime = time - $searchtime;
    $searchsecs = ($searchtime == 1 ? "second" : "seconds");

    $rHTML = &ropt2html;
    $xHTML = &xopt2html;
    $hiddenHTML = &args2html;

    $onME = join(', ', $dbh->selectAllAsArray("SELECT DISTINCT bin_name FROM ems_me_bins WHERE concept_id = $conceptid")) || "[none]";
    $onQA = join(', ', $dbh->selectAllAsArray("SELECT DISTINCT bin_name FROM ems_qa_bins WHERE concept_id = $conceptid")) || "[none]";
    $onAH = join(', ', $dbh->selectAllAsArray("SELECT DISTINCT bin_name FROM ems_ah_bins WHERE concept_id = $conceptid")) || "[none]";
    $onWorklists = join(', ', map{ "<A HREF=\"$wmsURL?action=view&worklist=$_\">$_</A>" } $dbh->selectAllAsArray("SELECT DISTINCT worklist_name FROM ems_being_edited WHERE concept_id = $conceptid")) || "[none]";

    &print_html("Concept Report for atom_id: $atomid (in concept: $conceptid)", <<"EOD");
<FONT SIZE=$fontsize>Time to generate report: $searchtime $searchsecs (DB: $reporttime $reportsecs)</FONT><BR>
<P>

<FONT SIZE=$fontsize>
$colorHeader
</FONT>
<P>

<HR>
<H2>Report Display Options</H2>

<FORM ACTION="$cgi" METHOD=POST>
<FONT SIZE=$fontsize>
Relationship View: $rHTML Context View: $xHTML <INPUT TYPE=SUBMIT VALUE="Refresh">
$hiddenHTML
</FONT>
</FORM>
<FORM ACTION="$cgi" METHOD=POST>
<FONT SIZE=$fontsize>
CUI-centric report for CUI editing: <INPUT TYPE=SUBMIT VALUE="Get It">
$hiddenHTML
<INPUT TYPE=hidden NAME="M" VALUE="1">
</FONT>
</FORM>

<P>

<HR>
<H2>EMS and WMS Information for this Concept</H2>
<TABLE CELLPADDING=1>
<TR><TD><FONT SIZE=$fontsize>Concept is in ME bin:</FONT></TD><TD><FONT SIZE=$fontsize>$onME</FONT></TD></TR>
<TR><TD><FONT SIZE=$fontsize>Concept is in QA bins:</FONT></TD><TD><FONT SIZE=$fontsize>$onQA</FONT></TD></TR>
<TR><TD><FONT SIZE=$fontsize>Concept is in AH bins:</FONT></TD><TD><FONT SIZE=$fontsize>$onAH</FONT></TD></TR>
<TR><TD><FONT SIZE=$fontsize>Concept is on worklist:</TD><TD><FONT SIZE=$fontsize>$onWorklists</FONT></TD></TR>
</TABLE>


<P><HR>
<P>

<FONT SIZE=$fontsize>
<A TARGET="sample_report" HREF="/concept-report/sample-report.html">Click here</A> for help on how to read a report (will open a separate window).
</FONT>

<P>
<HR>
<H1>Matching Atom</H1>
<TABLE BORDER=1 CELLSPACING=1 CELLPADDING=5 WIDTH="90%">
<TR>
<TH><FONT SIZE=$fontsize>Atom ID</FONT></TH>
<TH><FONT SIZE=$fontsize>Atom Name</FONT></TH>
<TH><FONT SIZE=$fontsize>Source</FONT></TH>
<TH><FONT SIZE=$fontsize>Termgroup</FONT></TH>
<TH><FONT SIZE=$fontsize>Code</FONT></TH>
</TR>
<TR>
<TD ALIGN=center><FONT SIZE=$fontsize>$atomid</FONT></TD>
<TD ALIGN=center><FONT SIZE=$fontsize>$w->{atom_name}</FONT></TD>
<TD ALIGN=center><FONT SIZE=$fontsize>$w->{source}</FONT></TD>
<TD ALIGN=center><FONT SIZE=$fontsize>$w->{termgroup}</FONT></TD>
<TD ALIGN=center><FONT SIZE=$fontsize>$w->{code}</FONT></TD>
</TR>
</TABLE>
<P>

<A NAME="report"><P></A>

<TABLE BORDER=1 CELLPADDING=20>

<TD>
<H1>Concept Report</H1>
$report
</TD>
</TABLE>

<P><HR>
<H1>See Also</H1>
<FONT SIZE=$fontsize>
<UL>
<LI>This report with atoms $colorCoded and $notColorCoded
<LI>Search the last release for this concept (CUI: <A HREF="$conceptReportRelease?action=searchbycui&arg=$cui&$filestosearch">$cui</A>)
<LI>Show frequently <A HREF="$conceptReportRelease?action=showcooc&arg=$cui&conceptid=$conceptid&db=$db&meme_home=$meme_home">co-occurring concepts</A> from the last release
<!-- <LI>Raw table data for <A HREF="/cgi-bin/concept-report-raw.pl?db=$db&id=$conceptid">this concept</A> -->
</UL>
</FONT>
EOD
    return;
}

sub search_by_cui {
    my($cui) = $query->param('arg');
    my($tmpfile);
    my($body);
    my($conceptid);
    
    $cui =~ tr/a-z/A-Z/;
    $cui =~ s/[^$OK_CHARS]/_/go;
    $cui =~ s/^\s*//;
    $cui =~ s/\s*$//;

    unless ($cui && $cui =~ /^[cC][0-9]*$/) {
	&print_html("Malformed or Missing CUI", <<"EOD");
The CUI you typed (\"$cui\") was missing or malformed.  CUI\'s consist of a sequence of
digits preceded by the character 'C'.  Example: C0344595.  Please reformulate your query
and try again.
EOD
        return;
    }

    $conceptid = &conceptutils::cui2conceptid($dbh, $cui);

    unless ($conceptid && $conceptid =~ /^\d+/) {
	&print_html("Concept Report for CUI: $cui", <<"EOD");
<EM>No matching concept for CUI: $cui found.</EM>
EOD
        return;
    }

    $reporttime = time;
#    my($report) = &report($conceptid);
    # Now we have the concept id
    # write a conceptutis method to get the attribute
    # [attributes id, attribute_name, atom_id, atom_name, attribute_value, status,tobereleased] are the columns that need to be displayed.
    @y = &conceptutils::attrbutesfromconceptid($dbh,$conceptid);
    @x = ();
    if (@y) {
    foreach $r (@y) {
      push @x, $r;
    }
  }  else {
  }
  $testhtml = &toHTMLtableForAttributes($query, {class=>"attribute_table table-autosort:0 table-stripeclass:alternate table-autofilter"}, \@y);
    $reporttime = time - $reporttime;
    $reportsecs = ($reporttime == 1 ? "second" : "seconds");

    $searchtime = time - $searchtime;
    $searchsecs = ($searchtime == 1 ? "second" : "seconds");

    &print_html("Attribute Report for CUI: $cui in concept with concept_id: $conceptid", <<"EOD");
<FONT SIZE=$fontsize>Time to generate report: $searchtime $searchsecs (DB: $reporttime $reportsecs)</FONT><BR>
<FORM ACTION="$cgi" METHOD=POST>
<FONT SIZE=$fontsize>
$testhtml
</FONT>
</FORM>
<P>
<P>
EOD
    return;
}
sub search_by_conceptid {
    my($conceptid) = $query->param('arg');
    my($report);
    my($cui);

    $conceptid =~ s/[^$OK_CHARS]/_/go;
    $conceptid =~ s/^\s*//;
    $conceptid =~ s/\s*$//;
    unless ($conceptid && $conceptid =~ /^[0-9]*$/) {
        &print_html("Missing or Malformed concept_id", <<"EOD");
The concept_id you typed (\"$conceptid\") was malformed.  Legitimate concept_id\'s are integers, for example 52076818.
Please refine the query and try again.
EOD
        return;
    }

    $reporttime = time;
    # Now we have the concept id
    # write a conceptutis method to get the attribute
    # [attributes id, attribute_name, atom_id, atom_name, attribute_value, status,tobereleased] are the columns that need to be displayed.
    @y = &conceptutils::attrbutesfromconceptid($dbh,$conceptid);
    @x = ();
    if (@y) {
    foreach $r (@y) {
      push @x, $r;
    }
  }  else {
     &print_html("No Data Found", <<"EOD");
Could not find any iattributes for the concept_id you typed (\"$conceptid\")     
Please refine the query and try again.
EOD
     return;
  }
#  $testhtml = &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@x);
  $testhtml = &toHTMLtableForAttributes($query, {class=>"attribute_table table-autosort:0 table-stripeclass:alternate table-autofilter"}, \@y);
    $reporttime = time - $reporttime;
    $reportsecs = ($reporttime == 1 ? "second" : "seconds");

    $searchtime = time - $searchtime;
    $searchsecs = ($searchtime == 1 ? "second" : "seconds");

    &print_html("Attribute Report for concept ID: $conceptid", <<"EOD");
<FONT SIZE=$fontsize>Time to generate report: $searchtime $searchsecs (DB: $reporttime $reportsecs)</FONT><BR>
<FORM ACTION="$cgi" METHOD=POST>
<FONT SIZE=$fontsize>
$testhtml
</FONT>
</FORM>
<P>
<P>
EOD
    return;
}



# makes a report given a concept_id, title and body

# ignores case
# returns the number of rels that a concept has
sub print_trailer {
    print <<"EOD";
<P>
<HR WIDTH=600 ALIGN=left>
<FONT SIZE=$fontsize>
<ADDRESS><A HREF="$cgi">Attribute reports from current MID</A></ADDRESS>
<ADDRESS><A HREF="http://unimed.nlm.nih.gov">Meta News Home</A></ADDRESS>
</FONT>
</BODY>
</HTML>
EOD
}

sub print_cgi_header {
    my($mime) = @_;

    $mime = "text/html" unless $mime;
    return if $cgi_header_printed++;
    print <<"EOD";
Content-type: $mime

EOD
    print <<"EOD" if $mime eq "text/html";
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
EOD
}

sub print_header {
    my($title, $header, $bodyargs) = @_;

    $title = "Concept Report" unless $title;
    $header = $title unless $header;

#    my($javascript) = GeneralUtils->file2str("../www/js/table.js");

    &print_cgi_header("");
    print <<"EOD";
<HTML>
<HEAD>
<TITLE>$title</TITLE>

<script type="text/javascript" src="/js/table.js"></script>
<link rel="stylesheet" type="text/css" href="/js/attr_table.css" media="all">
</HEAD>
<BODY $bodyargs>

<TABLE BORDER="0" WIDTH="600" CELLSPACING="0" CELLPADDING="0">
<TR>
<TD ALIGN="left">
<FONT SIZE="-1" COLOR="red">Oracle Database: $db</FONT>
</TD>
<TD ALIGN="right">
<FONT SIZE="-1">$now</FONT>
</TD>
</TR>
</TABLE>

<!-- Horizontal bar -->

<HR WIDTH=600 SIZE=6 NOSHADE ALIGN=left>

<H1>$header</H1>

EOD
    return;
}

sub print_html {
    my($title, $html) = @_;

    $title = "Concept Report: $action" unless $title;
    $html = $title unless $html;
    &print_header($title);
    print <<"EOD";

$html
EOD
    &print_trailer;
    return;
}

# returns the r option in HTML
sub ropt2html {
    my($html, $opt);
    my(@opts) = (
		[ "DEFAULT" => "Winning Rels" ],
		[ "XR" => "Winning + XR Rels" ],
		[ "ALL" => "All Rels" ],
		);

    $html = "<SELECT NAME=\"r\" SIZE=\"1\">";
    my($short, $long, $ropt);
    $ropt = $query->param('r');
    foreach $opt (@opts) {
	$short = $opt->[0];
	$long = $opt->[1];
	if ($ropt eq $short) {
	    $html .= "<OPTION SELECTED VALUE=\"$short\">$long";
	} else {
	    $html .= "<OPTION VALUE=\"$short\">$long";
	}
    }
    return $html . "</SELECT>" . "\n";
}

# returns the x option in HTML
sub xopt2html {
    my($html, $opt);
    my(@opts) = (
		[ "DEFAULT" => "Par+Chd if no Cxt Rels" ],
		[ "SIB" => "Par+Chd+Sib if no Cxt Rels" ],
		[ "ALL" => "All Contexts" ],
		);

    $html = "<SELECT NAME=\"x\" SIZE=\"1\">";

    my($short, $long, $ropt);
    $ropt = $query->param('x');
    foreach $opt (@opts) {
	$short = $opt->[0];
	$long = $opt->[1];
	if ($ropt eq $short) {
	    $html .= "<OPTION SELECTED VALUE=\"$short\">$long";
	} else {
	    $html .= "<OPTION VALUE=\"$short\">$long";
	}
    }
    return $html . "</SELECT>" . "\n";
}

# converts CGI args to hidden fields
sub args2html {
    my($n, $v);
    foreach $n ($query->param) {
	$v = $query->param($n);
	$html .= "<INPUT TYPE=hidden NAME=\"$n\" VALUE=\"$v\">";
    }
    return $html;
}

# read configuration file
sub read_config {
    my($file) = @_;
    my($fd) = gensym;

    open($fd, $file) || return;
    while (<$fd>) {
	my(@comment);

	chop;
	next if /^\#/ || /^\s*$/;
	($slot, $value, @comment) = split /\s+/, $_;
	$slot =~ tr/A-Z/a-z/;
	$config{$slot} = [ $value, [ @comment ] ];
    }
    close($fd);
}

# Function to sort by preferred string (lowercased)
sub preferredStrSort {
    my($strA, $strB);

    $strA = &conceptutils::conceptid2str($dbh, $a);
    $strB = &conceptutils::conceptid2str($dbh, $b);
    $strA =~ tr/A-Z/a-z/;
    $strB =~ tr/A-Z/a-z/;
    return $strA cmp $strB;
}

# returns an array
sub fmt {
    my($str, $maxcol) = @_;
    my(@result);
    my($i);
    my($newline)=1;
    my($word);
    my($l);

    $maxcol = 72 unless $maxcol;

    $str =~ s/^\s*//;
    $str =~ s/\s*$//;

    foreach $word (split /\s+/, $str) {
	$result[$i] .= ($newline ? "" : " ") . $word;
	$l += ($newline ? 0 : 1) + length($word);

	if ($l >= $maxcol) {
	    $newline = 1;
	    $l=0;
	    $i++;
	} else {
	    $newline = 0;
	}
    }
    return(@result);
}

sub cgi_safe {
    my($in) = @_;

    $in =~ s/\s/%20/g;
    $in =~ s/\&/%26/g;
    $in =~ s/\?/%3f/g;
    return($in);
}

sub toHTMLtableForAttributes {
  my($cgi, $tableprop, $data, $firstrowisheader) = @_;
  my($row, $datum);
  my($rowprop, $datumprop);
  my($d);
  my(@rows, @datum);
  my($firstdatum);
  my($firstrow) = 1;

  foreach $row (@$data) {
    @datum = ();
    if ($firstrow) {
       push @rows, "<thead>". "\n";
       push @datum, $cgi->th({class=>"table-sortable:numeric"},"AttributeId");
       push @datum, $cgi->th({class=>"table-sortable:default table-filterable"},"Attribute Level");
       push @datum, $cgi->th({class=>"table-sortable:numeric"},"Atom ID");
       push @datum, $cgi->th({class=>"table-sortable:default table-filterable"},"Attribute Name");
       push @datum ,$cgi->th({class=>"table-sortable:default" },"Attribute Value");
       push @datum ,$cgi->th({class=>"table-sortable:default table-filterable"},"Attribute Status");
       push @datum, $cgi->th({class=>"table-sortable:default table-filterable"},"ToBeReleased");
       push @datum, $cgi->th({class=>"table-sortable:default table-filterable"},"Source");
       push @rows, $cgi->Tr($rowprop, @datum) . "\n";
       push @rows, "</thead>\n";
    }
    @datum = ();
    $firstdatum = 1;
    $rowprop = "";
    foreach $d (@$row) {
      if ($firstdatum && ref($d) eq "HASH") {
        $rowprop = $d;
      } else {
        $datumprop = {};
        if (ref($d) eq "ARRAY") {
          $datumprop = $d->[0];
          $datum = $d->[1];
        } else {
          $datum = $d;
        }
        push @datum, $cgi->td($datumprop, $datum);
      }
      $firstdatum = 0;
    }
    push @rows, $cgi->Tr($rowprop, @datum) . "\n";
    $firstrow = 0;
  }
  return $cgi->table($tableprop, @rows);
}
