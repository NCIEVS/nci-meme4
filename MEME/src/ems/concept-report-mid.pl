#!@PATH_TO_PERL@

# Prints a concept report using the database
# suresh@nlm.nih.gov 6/97

# Modified to support MEME-II reports
# suresh@nlm.nih.gov 1/98

# Ported to Oracle and MEME-III
# suresh@nlm.nih.gov 3/2000

# CGI parameters:
# action={
#   searchbycui|searchbyconceptid|searchbyatomid|searchbysourcerowid|
#   searchbynormstr|searchbynormword|
#   cuisearchform|normstrsearchform|normwordsearchform}
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

# These were added to support new options 5/99 suresh@nlm.nih.gov
# r={DEFAULT|ALL|XR}
# x={DEFAULT|ALL|XR}

# $Id$

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

#unshift @INC, "/site/umls/scripts";
#unshift @INC, "/site/umls/lvg";
#unshift @INC, "/site/umls/oracle/utils";
#nshift @INC, "/site/umls/oracle/concept-reports";

#require "oracleIF.pl";
use OracleIF;
use EMSUtils;
#require "midsvcs.pl";
use Midsvcs;
#require "utils.pl";
require "conceptutils.pl";
#require "lvg3.pl";
use LVG;

$now = GeneralUtils->date;

# edit these two lines if you decide to change norm versions
#$lvgversion="lv_1.84e";
#$lvgversion="lv_1.83d";
$normfn="luinorm"; # luinorm or norm
#$normfn="norm"; # luinorm or norm

# read configuration file and set defaults
#$configfile = "/etc/umls/concept-report-mid.config";
#&read_config($configfile);

# set the defaults from config file
#$access = @{ $config{"access"}}->[0]; $access =~ tr/A-Z/a-z/;
#$rslimit = @{ $config{"rslimit"}}->[0];
#$rellimit = @{ $config{"rellimit"}}->[0];
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

# first element is for HTML rest are RGB for enscript
%colorCodes = (
	       "RCD" => "#FF0000",
	       "SNMI" => "#00BF00",
	       "MSH" => "#0000FF"
	       );

$mshcolor = $colorCodes{"MSH"};
$rcdcolor = $colorCodes{"RCD"};
$snmicolor = $colorCodes{"SNMI"};

$colorHeader = <<"EOD";
<P>
Color code for atoms: <SPAN STYLE="color: $mshcolor">MSH</SPAN>, 
<SPAN STYLE="color: $rcdcolor">RCD</SPAN>, and
<SPAN STYLE="color: $snmicolor">SNMI</SPAN>
EOD


# ORACLE vars
#&oracleIF::init_oracle;
#$oracleUSER = $query->param('user') || @{ $config{"user"} }->[0] || "meow";
#$oraclePWD = &oracleIF::get_passwd("/etc/umls/oracle.passwd", $oracleUSER);
#$oracleAUTH = "$oracleUSER/$oraclePWD";
#$MIDservice = $query->param('service') || "editing-db";
#$oracleTNS = $mid || $query->param('db') ||
#    @{ $config{"db"} }->[0] || &midsvcs::get_mid_service($MIDservice);
#$defaultTABLESPACE = "MID";

#$oracleDBH = &oracleIF::connectDBD($oracleAUTH, $oracleTNS);
#die "ERROR: Oracle not available for $oracleTNS\n" unless $oracleDBH;
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

# Environment
#$ENV{'PATH'} = "/bin:$ENV{'ORACLE_HOME'}/bin";
#$ENV{'II_SYSTEM'}="/export/home/oracle";
$cgi = $ENV{'SCRIPT_NAME'};
#$emsURL = "/cgi-oracle-meowusers/ems.pl";
#$wmsURL = "/cgi-oracle-meowusers/wms.pl";

$emsURL = $main::EMSCONFIG{LEVEL0EMSURL};
$wmsURL = $main::EMSCONFIG{LEVEL0WMSURL};

# file containing all sources
$sourcefile = "$ENV{'EMS_LOG_DIR'}/log/sources.txt";
#$sourcefile = "/d3/ems/data/styqa/sources.$oracleTNS.cache";
#$sourcefile = "/site2/umls-data/concept-report-data/sources.txt" unless -e $sourcefile;
$allsources = "-- All Sources --";

# want color reports? default is yes
$color = 1 unless $query->param('color') eq "0" || $query->param('nocolor') eq "1";
$colorHeader = "" unless $color;

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

    &print_html("Concept Report from MID Unavailable", <<"EOD");
Concept reports from the MID are unavailable as of <EM>$now</EM>.  Please try again later.
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
    } elsif ($action eq "searchbysourcerowid") {
	&search_by_sourcerowid;
    } elsif ($action eq "searchbycode") {
	&search_by_code;
    } elsif ($action eq "searchbynormstr") {
	&search_by_normstr;
    } elsif ($action eq "searchbynormword") {
	&search_by_normword;
    } elsif ($action eq "normstrsearchform") {
	&normstr_search_form;
    } elsif ($action eq "normwordsearchform") {
	&normword_search_form;
    } elsif ($action eq "cuisearchform") {
	&cui_search_form;
    } elsif ($action eq "randomconcepts") {
	&randomconcepts;
    } elsif ($action eq "searchbyndccode") {
	&search_by_ndc_code;
	} elsif ($action eq "searchbyscui") {
	&search_by_scui;
	
    } else {
	&search_form;
    }

} else {
    &search_form;
}
exit 0;

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
<INPUT TYPE=submit NAME="actionmap" VALUE="Search by Identifier">
</FONT>
</TD>
<TD>
<FONT SIZE=$fontsize>
Use this if you know the unique identifiers for the concept (CUI or concept_id), the
atoms within the concept (atom_id) or by source-specific identifiers (code).
</FONT>
</TD>
</TR>

<TR>
<TD VALIGN=top>
<FONT SIZE=$fontsize>
<INPUT TYPE=submit NAME="actionmap" VALUE="Normalized String Search">
</FONT>
</TD>
<TD>
<FONT SIZE=$fontsize>
Use this if you know an exact string representation for the concept you are searching for,
modulo differences in case, inflection and word order.
</FONT>
</TD>
</TR>

<TR>
<TD VALIGN=top>
<FONT SIZE=$fontsize>
<INPUT TYPE=submit NAME="actionmap" VALUE="Approximate Word Search">
</FONT>
</TD>
<TD>
<FONT SIZE=$fontsize>
This is the broadest category of searching.  Given an arbitrary query consisting of one or
more words, the best matching concepts are returned using word level normalization and
frequency data.
Use this option if you are looking for missed synonymy, for example.
</FONT>
</TD>
</TR>

<TR>
<TD VALIGN=top>
<FONT SIZE=$fontsize>
<INPUT TYPE=submit NAME="actionmap" VALUE="Random Concepts">
</FONT>
</TD>
<TD>
<FONT SIZE=$fontsize>
Use this option if you are not looking for any particular concept but want to browse
what is there.
</FONT>
</TD>
</TR>

</TABLE>

<INPUT TYPE=hidden NAME="meme_home" VALUE="$meme_home">
</FORM>
EOD
    return;
}

# Form for normstr search
sub normstr_search_form {
    my(@sources);
    my($sourceHTML);

    $sourceHTML = &source2html();

    &print_html("Concept Report from the MID - Normalized String Search", <<"EOD");
<FONT SIZE=$fontsize>
Submitting the following form returns concepts that match your query using a
flexible, <EM>normalized</EM>
(see: what is <A HREF="http://www.nlm.nih.gov/research/umls/META4.HTML#s48">normalized</A>?)
string matching algorithm.

<P>

The string search algorithm uses the normalized string index which contains the normalized forms
of all the strings in the database.  This is useful in finding concepts with terms whose
general form is known.

Examples: "<A HREF="$cgi?action=searchbynormstr&arg=blood%20clot&meme_home=$meme_home\#report">blood clot</A>"
</FONT>

<P>

<FORM ACTION="$cgi" METHOD="post">

<FONT SIZE=$fontsize>
Enter a string:
<P>
<INPUT TYPE="text" NAME="arg" SIZE=40>
$sourceHTML
<P>
<INPUT TYPE="hidden" NAME="action" VALUE="searchbynormstr">
<INPUT TYPE="hidden" NAME="db" VALUE="$db">
<INPUT TYPE="hidden" NAME="meme_home" VALUE="$meme_home">
<INPUT TYPE="hidden" NAME="meme_server" VALUE="$meme_server">
<INPUT TYPE="submit" VALUE="Get Matching Concepts">
</FONT>
</FORM>
EOD
    return;
}

# Form for normword search
sub normword_search_form {
    my(@sources);
    my($sourceHTML);

    $sourceHTML = &source2html();

    &print_html("Concept Report from the MID - Approximate Word Search", <<"EOD");
<FONT SIZE=$fontsize>
Submitting the following form returns concepts that match your query using an
approximate matching algorithm that uses word level normalization
(see: what is <A HREF="http://www.nlm.nih.gov/research/umls/META4.HTML#s48">normalized</A>?).

<P>

This type of search may be useful for finding concepts that have words in common
with your query.

Examples: "<A HREF="$cgi?action=searchbynormword&arg=food%20allergies&rslimit=10&meme_home=$meme_home\#report">food allergies</A>".
</FONT>

<P>

<FORM ACTION="$cgi" METHOD="post">

<FONT SIZE=$fontsize>
Enter your query:
<P>
<INPUT TYPE="text" NAME="arg" SIZE=40>
$sourceHTML
<P>
Show the best <SELECT NAME="rslimit" SIZE=1><OPTION>100<OPTION>75<OPTION>50<OPTION SELECTED>25<OPTION>10<OPTION>5</SELECT> matches.
<P>
<INPUT TYPE="hidden" NAME="action" VALUE="searchbynormword">
<INPUT TYPE="hidden" NAME="db" VALUE="$db">
<INPUT TYPE="hidden" NAME="meme_home" VALUE="$meme_home">
<INPUT TYPE="hidden" NAME="meme_server" VALUE="$meme_server">
<INPUT TYPE="submit" VALUE="Get Matching Concepts">
</FONT>
</FORM>
EOD
    return;
}

# UI search form
sub cui_search_form {
    &print_html("Concept Report from the MID - Search by Identifier", <<"EOD");
<FONT SIZE=$fontsize>
Submitting this form returns a (textual) concept report for the concept
with the identifier specified.  The identifier can be
a CUI (Concept Unique Identifier), a concept_id (Concept ID),
an atom_id (atom ID) source_row_id (internal) or a source\'s code.

Examples:
CUI: <A HREF="$cgi?action=searchbycui&arg=C0267170&db=$db&meme_home=$meme_home\#report">C0267170</A>,
concept_id: <A HREF="$cgi?action=searchbyconceptid&arg=1040879&db=$db&meme_home=$meme_home\#report">1040879</A>, and
atom_id: <A HREF="$cgi?action=searchbyatomid&arg=3397144&db=$db&meme_home=$meme_home\#report">3397144</A>

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
<INPUT TYPE=radio NAME="action" VALUE="searchbyatomid">atom_id
<INPUT TYPE=radio NAME="action" VALUE="searchbysourcerowid">source_row_id
<INPUT TYPE=radio NAME="action" VALUE="searchbycode">code
<INPUT TYPE=radio NAME="action" VALUE="searchbyndccode">NDC code
<INPUT TYPE=radio NAME="action" VALUE="searchbyscui">source_cui
<P>
<INPUT TYPE="submit" VALUE="Get Concept Report">
<INPUT TYPE="hidden" NAME="db" VALUE="$db">
<INPUT TYPE="hidden" NAME="meme_home" VALUE="$meme_home">
<INPUT TYPE="hidden" NAME="meme_server" VALUE="$meme_server">
</FONT>
</FORM>
EOD
    return;
}

# random concepts
sub randomconcepts {
    print <<"EOD";
Location: $conceptReportRelease?action=randomcuis&versionscheme=new

EOD
}

# Retrieves a report by Concept ID
sub search_by_conceptid {
    my($conceptid);
    my($report);
    my($cui);

    $conceptid = $query->param('arg');
    $conceptid =~ s/^\s*//;
    $conceptid =~ s/\s*$//;
    $conceptid =~ s/[^$OK_CHARS]/_/go;
    
    unless ($conceptid && $conceptid =~ /^[0-9]*$/) {
	&print_html("Missing or Malformed concept_id", <<"EOD");
The concept_id you typed (\"$conceptid\") was malformed.  Legitimate concept_id\'s are integers, for example 52076818.
Please refine the query and try again.
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

    $onME = join(', ',  $dbh->selectAllAsArray("SELECT DISTINCT bin_name FROM ems_me_bins WHERE concept_id = $conceptid")) || "[none]";
    $onQA = join(', ', $dbh->selectAllAsArray("SELECT DISTINCT bin_name FROM ems_qa_bins WHERE concept_id = $conceptid")) || "[none]";
    $onAH = join(', ', $dbh->selectAllAsArray("SELECT DISTINCT bin_name FROM ems_ah_bins WHERE concept_id = $conceptid")) || "[none]";
    $onWorklists = join(', ', map{ "<A HREF=\"$wmsURL?action=view&worklist=$_\">$_</A>" } $dbh->selectAllAsArray("SELECT DISTINCT worklist_name FROM ems_being_edited WHERE concept_id = $conceptid")) || "[none]";
    $whyn = $query->a({-href=>"$emsURL?action=whyisthisn&concept_id=$conceptid&db=$db"}, "Why is this concept in N status?");

    &print_html("Concept Report for concept_id: $conceptid", <<"EOD");
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
<TR><TD><FONT SIZE=$fontsize>$whyn</TD><TD><FONT SIZE=$fontsize></FONT></TD></TR>
</TABLE>

<P>
<HR>
<P>
<FORM ACTION="/cgi-bin/showattributes.pl" METHOD="POST" target="_new">
<FONT SIZE=$fontsize>
Click here to view all the attributes of this concept $concept_id  <INPUT TYPE=SUBMIT VALUE="Get All Attributes">
$hiddenHTML
</FONT>
</FORM>
<P>
<HR>

<FONT SIZE=$fontsize>
<A TARGET="sample_report" HREF="/concept-report/sample-report.html">Click here</A> for help on how to read a report (will open a separate window).
</FONT>

<P>
<HR>
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
</UL>

EOD
}

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
<FORM ACTION="/cgi-bin/showattributes.pl" METHOD="POST" target="_new">
<FONT SIZE=$fontsize>
Click here to view all the attributes of this concept $concept_id  <INPUT TYPE=SUBMIT VALUE="Get All Attributes">
$hiddenHTML
</FONT>
</FORM>
<P>
<HR>
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

# search for concepts by source_row_id
sub search_by_sourcerowid {
    my($source_row_id) = $query->param('arg');
    my($SQL);
    my(@rows);
    my(@atomids, $atomid);
    my($n);
    
    $source_row_id =~ s/^\s*//;
    $source_row_id =~ s/\s*$//;

    unless ($source_row_id && $source_row_id =~ /^[0-9]*$/) {
	&print_html("Missing or Malformed source_row_id", <<"EOD");
The source_row_id you typed (\"$source_row_id\") was malformed.
Legitimate source_row_id\'s are integers, for example 47553210.
Please refine the query and try again.
EOD
	return;
    }

    @atomids = &conceptutils::sourcerowid2atomids($dbh, $source_row_id);

    unless (@atomids) {
	&print_html("No matching atoms for source_row_id: $source_row_id", <<"EOD");
<EM>No matching atoms for source_row_id: $source_row_id found.</EM>
EOD
	return;
    }

    foreach $atomid (grep { $U{$_}++ < 1 } sort @atomids) {

	$SQL = <<"EOD";
SELECT c.concept_id, c.atom_id, a.atom_name, c.source, c.termgroup, c.code FROM classes c, atoms a WHERE
    c.atom_id = a.atom_id and
    c.atom_id = \'$atomid\'
EOD
        @fields = $dbh->selectFirstAsArray($SQL);

	$n++;
	$html .= <<"EOD";
<TR>
<TD>$n</TD>
<TD><FONT SIZE=$fontsize>$atomid</FONT><BR></TD>
<TD><FONT SIZE=$fontsize><A HREF="$cgi?action=searchbyconceptid&arg=$fields[0]&meme_home=$meme_home&meme_server=$meme_server&db=$db\#report">$fields[0]</A></FONT><BR></TD>
<TD><FONT SIZE=$fontsize>$fields[2]</FONT><BR></TD>
<TD><FONT SIZE=$fontsize>$fields[4]</FONT><BR></TD>
<TD><FONT SIZE=$fontsize>$fields[5]</FONT><BR></TD>
</TR>
EOD
    }

    &print_html("Matching atoms for source_row_id: $source_row_id", <<"EOD");
<FONT SIZE=$fontsize>
The following atoms were retrieved for the source_row_id: $source_row_id.
Select the concept ID to see the matching concept report.
</FONT>
<P>
<TABLE CELLSPACING=1 CELLPADDING=5 BORDER=1 WIDTH="90%">
<TR>
<TH></TH>
<TH><FONT SIZE=$fontsize>atom_id</FONT></TH>
<TH><FONT SIZE=$fontsize>Concept ID</FONT></TH>
<TH><FONT SIZE=$fontsize>Atom Name</FONT></TH>
<TH><FONT SIZE=$fontsize>Termgroup</FONT></TH>
<TH><FONT SIZE=$fontsize>Code</FONT></TH>
</TR>
$html
</TABLE>
EOD
}

sub search_by_cui {
    my($cui);
    my($tmpfile);
    my($body);
    my($conceptid);
    
    $cui = $query->param('arg');
    $cui =~ s/^\s*//;
    $cui =~ s/\s*$//;
    $cui =~ s/[^$OK_CHARS]/_/go;
    

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
    my($report) = &report($conceptid);

    if ($format eq "text") {
	print $report;
	return;
    }

    $reporttime = time - $reporttime;
    $reportsecs = ($reporttime == 1 ? "second" : "seconds");

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

    &print_html("Concept Report for CUI: $cui in concept with concept_id: $conceptid", <<"EOD");
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
<P>
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

<P>
<HR>
<P>
<FORM ACTION="/cgi-bin/showattributes.pl" METHOD="POST" target="_new">
<FONT SIZE=$fontsize>
Click here to view all the attributes of this concept $concept_id  <INPUT TYPE=SUBMIT VALUE="Get All Attributes">
$hiddenHTML
</FONT>
</FORM>
<P>
<HR>

<FONT SIZE=$fontsize>
<A TARGET="sample_report" HREF="/concept-report/sample-report.html">Click here</A> for help on how to read a report (will open a separate window).
</FONT>

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
<!-- <LI>Raw table data for <A HREF="/cgi-bin/concept-report-mid.pl?db=$db&id=$conceptid">this concept</A> -->
</UL>
</FONT>
EOD
    return;
}

sub search_by_ndc_code {
    my($code) = $query->param('arg');
    my($tmpfile);
    my($body);
    my($conceptid);
    
    $code =~ s/^\s*//;
    $code =~ s/\s*$//;

    unless ($code) {
	&print_html("Malformed or Missing NDC Code", <<"EOD");
Please reformulate your query and try again.
EOD
        return;
    }

    @atom_ids = &conceptutils::ndccode2atomids($dbh, $code);
    unless (@atom_ids) {
	&print_html("No atoms for ndc code: $code", <<"EOD");
<EM>No matching atoms for ndc code: "$code" found.</EM>
EOD
	return;
    }
    my(@cols) = qw(concept_id atom_id atom_name source termgroup code);

    foreach $atomid (@atom_ids) {
	$SQL = <<"EOD";
SELECT c.concept_id, c.atom_id, a.atom_name, c.source, c.termgroup, c.code FROM classes c, atoms a WHERE
    c.atom_id = a.atom_id and
    c.atom_id = \'$atomid\'
EOD
        @fields = $dbh->selectFirstAsRef($SQL);
        $w = $dbh->row2ref(@fields, @cols);
	$conceptid=$w->{concept_id};
	$html .= <<"EOD";

<TR>
<TD ALIGN=right><FONT SIZE=$fontsize><A HREF="$cgi?action=searchbyatomid&arg=$atomid&meme_home=$meme_home&meme_server=$meme_server&db=$db\#report">$atomid</A></FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{atom_name}</FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{source}</FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{termgroup}</FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{code}</FONT></TD>
</TR>

EOD
    }

    $searchtime = time - $searchtime;
    $searchsecs = ($searchtime == 1 ? "second" : "seconds");

    &print_html("Matching Atoms for NDC Code: $code", <<"EOD");
<FONT SIZE=$fontsize>Time to generate report: $searchtime $searchsecs</FONT><BR>
<P>
<FONT SIZE=$fontsize>
Select the atom ID to see the matching concept report.
</FONT>
<P>
<TABLE CELLSPACING=1 CELLPADDING=5 BORDER=1 WIDTH="90%">
<TR>
<TH><FONT SIZE=$fontsize>Atom ID</FONT></TH>
<TH><FONT SIZE=$fontsize>Atom Name</FONT></TH>
<TH><FONT SIZE=$fontsize>Source</FONT></TH>
<TH><FONT SIZE=$fontsize>Termgroup</FONT></TH>
<TH><FONT SIZE=$fontsize>Code</FONT></TH>
</TR>
$html
</TABLE>
EOD
    return;
}

sub search_by_code {
    my($code) = $query->param('arg');
    my($tmpfile);
    my($body);
    my($conceptid);
    
    $code =~ s/^\s*//;
    $code =~ s/\s*$//;

    unless ($code) {
	&print_html("Malformed or Missing Code", <<"EOD");
Please reformulate your query and try again.
EOD
        return;
    }

    @atom_ids = &conceptutils::code2atomids($dbh, $code);
    unless (@atom_ids) {
	&print_html("No atoms for code: $code", <<"EOD");
<EM>No matching atoms for code: "$code" found.</EM>
EOD
	return;
    }
    my(@cols) = qw(concept_id atom_id atom_name source termgroup code);

    foreach $atomid (@atom_ids) {
	$SQL = <<"EOD";
SELECT c.concept_id, c.atom_id, a.atom_name, c.source, c.termgroup, c.code FROM classes c, atoms a WHERE
    c.atom_id = a.atom_id and
    c.atom_id = \'$atomid\'
EOD
        @fields = $dbh->selectFirstAsRef($SQL);
        $w = $dbh->row2ref(@fields, @cols);
	$conceptid=$w->{concept_id};
	$html .= <<"EOD";

<TR>
<TD ALIGN=right><FONT SIZE=$fontsize><A HREF="$cgi?action=searchbyatomid&arg=$atomid&meme_home=$meme_home&meme_server=$meme_server&db=$db\#report">$atomid</A></FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{atom_name}</FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{source}</FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{termgroup}</FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{code}</FONT></TD>
</TR>

EOD
    }

    $searchtime = time - $searchtime;
    $searchsecs = ($searchtime == 1 ? "second" : "seconds");

    &print_html("Matching Atoms for Code: $code", <<"EOD");
<FONT SIZE=$fontsize>Time to generate report: $searchtime $searchsecs</FONT><BR>
<P>
<FONT SIZE=$fontsize>
Select the atom ID to see the matching concept report.
</FONT>
<P>
<TABLE CELLSPACING=1 CELLPADDING=5 BORDER=1 WIDTH="90%">
<TR>
<TH><FONT SIZE=$fontsize>Atom ID</FONT></TH>
<TH><FONT SIZE=$fontsize>Atom Name</FONT></TH>
<TH><FONT SIZE=$fontsize>Source</FONT></TH>
<TH><FONT SIZE=$fontsize>Termgroup</FONT></TH>
<TH><FONT SIZE=$fontsize>Code</FONT></TH>
</TR>
$html
</TABLE>
EOD
    return;
}

# new - search by source_cui
sub search_by_scui {
    my($scui) = $query->param('arg');
    my($tmpfile);
    my($body);
    my($conceptid);
    
    $scui =~ s/^\s*//;
    $scui =~ s/\s*$//;

    unless ($scui) {
	&print_html("Malformed or Missing source_cui", <<"EOD");
Please reformulate your query and try again.
EOD
        return;
    }

    @atom_ids = &conceptutils::scui2atomids($dbh, $scui);
    unless (@atom_ids) {
	&print_html("No atoms for source_cui: $scui", <<"EOD");
<EM>No matching atoms for scui: "$scui" found.</EM>
EOD
	return;
    }
    my(@cols) = qw(concept_id atom_id atom_name source termgroup code);

    foreach $atomid (@atom_ids) {
	$SQL = <<"EOD";
SELECT c.concept_id, c.atom_id, a.atom_name, c.source, c.termgroup, c.code FROM classes c, atoms a WHERE
    c.atom_id = a.atom_id and
    c.atom_id = \'$atomid\'
EOD
        @fields = $dbh->selectFirstAsRef($SQL);
        $w = $dbh->row2ref(@fields, @cols);
	$conceptid=$w->{concept_id};
	$html .= <<"EOD";

<TR>
<TD ALIGN=right><FONT SIZE=$fontsize><A HREF="$cgi?action=searchbyatomid&arg=$atomid&meme_home=$meme_home&meme_server=$meme_server&db=$db\#report">$atomid</A></FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{atom_name}</FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{source}</FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{termgroup}</FONT></TD>
<TD><FONT SIZE=$fontsize>$w->{code}</FONT></TD>
</TR>

EOD
    }

    $searchtime = time - $searchtime;
    $searchsecs = ($searchtime == 1 ? "second" : "seconds");

    &print_html("Matching Atoms for source_cui: $scui", <<"EOD");
<FONT SIZE=$fontsize>Time to generate report: $searchtime $searchsecs</FONT><BR>
<P>
<FONT SIZE=$fontsize>
Select the atom ID to see the matching concept report.
</FONT>
<P>
<TABLE CELLSPACING=1 CELLPADDING=5 BORDER=1 WIDTH="90%">
<TR>
<TH><FONT SIZE=$fontsize>Atom ID</FONT></TH>
<TH><FONT SIZE=$fontsize>Atom Name</FONT></TH>
<TH><FONT SIZE=$fontsize>Source</FONT></TH>
<TH><FONT SIZE=$fontsize>Termgroup</FONT></TH>
<TH><FONT SIZE=$fontsize>Code</FONT></TH>
</TR>
$html
</TABLE>
EOD
    return;
}

# search for concepts by norm string
sub search_by_normstr {
    my($str) = $query->param('arg');
    my($normstr, $cgistr);
    my($conceptid);
    my($matching_ids_ref);
    
    if ($query->param('source') && $query->param('source') ne $allsources) {
	@restrictToSources = $query->param('source');
    }

#    ($normstr = $str) =~ s/[^$OK_CHARS]/_/go;
#    $normstr = LVG->norm($normstr);
#    $normstr = LVG->norm($str);
    
    if ($normfn eq "norm") {
	$normstr = LVG->norm($str);
    } else {
	$normstr = LVG->luinorm($str);
    }

    unless ($normstr) {
	&print_html("Missing Query String", <<"EOD");
Your query norm\'ed to an empty string.
Please refine the query and try again.
EOD
	return;
    }

    $matching_ids_ref = &get_normstr_matches($normstr);

    $cgistr = &cgi_safe($str);

    unless (keys %{ $matching_ids_ref }) {
      $searchtime = time - $searchtime;
      $searchsecs = ($searchtime == 1 ? "second" : "seconds");

	&print_html("Normalized String Query in MID", <<"EOD");
<FONT SIZE=$fontsize>Time to search: $searchtime $searchsecs</FONT><BR>
<P>
<FONT SIZE=$fontsize>
<EM>No matching concept for string: "$str" found in the sources selected.</EM>
</FONT>
EOD
	return;
    }

    $n=0;
    $html="";
    foreach $conceptid (sort preferredStrSort keys %{ $matching_ids_ref }) {
	my($tmpstr) = &conceptutils::conceptid2str($dbh, $conceptid);
	$n++;
	$html .= <<"EOD";
<TR>
<TD ALIGN=right><FONT SIZE=$fontsize>$n</FONT></TD>
<TD ALIGN=right><FONT SIZE=$fontsize><A HREF=\"$cgi?action=searchbyconceptid&arg=$conceptid&db=$db&meme_server=$meme_server&meme_home=$meme_home\#report\">$conceptid</A></FONT></TD>
<TD><FONT SIZE=$fontsize>$tmpstr</FONT></TD>
</TR>
EOD
    }

    $searchtime = time - $searchtime;
    $searchsecs = ($searchtime == 1 ? "second" : "seconds");

    &print_html("Normalized String Query in MID", <<"EOD");
<FONT SIZE=$fontsize>Time to search: $searchtime $searchsecs</FONT><BR>

<P>
<FONT SIZE=$fontsize>
The following string(s) matched.
Select the concept ID to see the matching concept report.
</FONT>
<P>
<TABLE CELLSPACING=1 CELLPADDING=5 BORDER=1 WIDTH="90%">
<TR>
<TH></TH>
<TH WIDTH="10%"><FONT SIZE=$fontsize>Concept ID</FONT></TH>
<TH><FONT SIZE=$fontsize>Preferred Atom Name</FONT></TH>
</TR>
$html
</TABLE>

<P><HR>
<H1>See Also</H1>
<UL>
<LI><FONT SIZE=$fontsize>Search the last release for "<A HREF="$conceptReportRelease?action=searchbynormstr&arg=$cgistr&$filestosearch">$str</A>"</FONT>
<LI><FONT SIZE=$fontsize>Search MEDLINE for "<A HREF=\"http://www.ncbi.nlm.nih.gov/htbin-post/Entrez/query?db=m&form=4&term=$cgistr\">$str</A>" using <A HREF=\"http://www.ncbi.nlm.nih.gov/PubMed\">Pubmed</A></FONT>
</UL>
EOD
}

# search by normword
sub search_by_normword {
    my($str) = $query->param('arg');
    my($normstr, $cgistr);
    my($conceptid);
    my($normword, @normwords);
    my($rslimit) = $query->param('rslimit') || $rslimit || 50;
    my($exact_ids_ref, $normstr_ids_ref, $word_ids_ref);

    if ($query->param('source') && $query->param('source') ne $allsources) {
	@restrictToSources = $query->param('source');
    }

#    ($normstr = $str) =~ s/[^$OK_CHARS]/_/go;
#    $normstr = &lvg::norm($normstr);
#    $normstr = &lvg::norm($str);

    if ($normfn eq "norm") {
	$normstr = LVG->norm($str);
    } else {
	$normstr = LVG->luinorm($str);
    }

    unless ($normstr) {
	&print_html("Missing Query String", <<"EOD");
Your query norm\'ed to an empty string.
Please refine the query and try again.
EOD
	return;
    }

    @normwords = split(/\s+/, $normstr);

    $exact_ids_ref = &get_exact_matches($str);
    $normstr_ids_ref = &get_normstr_matches($normstr);

    foreach $conceptid (keys %{ $normstr_ids_ref }) {
	delete $normstr_ids_ref->{$conceptid} if $exact_ids_ref->{$conceptid};
    }

    foreach $normword (@normwords) {
	$word_ids_ref = &get_normword_matches($normword);
	foreach $conceptid (keys %{ $word_ids_ref }) {
	    if ($normstr_ids_ref->{$conceptid} || $exact_ids_ref->{$conceptid}) {
		delete $word_ids_ref->{$conceptid};
		next;
	    }
	    my($x) = &wordfreq($normword) || 10000;
	    $weight{$conceptid} += 1/$x;
	}
    }

    $n_matches = scalar(keys %{ $exact_ids_ref }) +
	scalar(keys %{ $normstr_ids_ref }) +
	scalar(keys %weight);

# only keep the top $rslimit entries to speed things up later
    foreach $conceptid (keys %weight) {
	for ($i=$rslimit-1; $i>=0; $i--) {
	    if ($wt[$i] >= $weight{$conceptid}) {
		$wt[$i+1] = $weight{$conceptid};
		$tmpmatch[$i+1] = $conceptid;
		last;
	    } else {
		$wt[$i+1] = $wt[$i];
		$tmpmatch[$i+1] = $tmpmatch[$i];
		if ($i>0) {
		    $wt[$i] = $wt[$i-1];
		    $tmpmatch[$i] = $tmpmatch[$i-1];
		} else {
		    $wt[$i] = $weight{$conceptid};
		    $tmpmatch[$i] = $conceptid;
		    last;
		}
	    }
	}
    }

# create matches
    foreach (@tmpmatch) {
	push(@match, $_) if $_;
    }

    unless ($n_matches) {
	&print_html("Approximate Word Query in MID", <<"EOD");
<FONT SIZE=$fontsize>
Your query: "$str" had no matching concepts in the sources selected.  Please refine the query and try again.
</FONT>
EOD
	return;
    }

    $n=0;
    $html="";

    $cgistr = &cgi_safe($str);
    my($best);

    $best = ($n_matches < $rslimit ? "" : "The following are the best $rslimit.");

    foreach $conceptid (keys %{ $exact_ids_ref }, (sort preferredStrSort keys %{ $normstr_ids_ref }), @match) {
	last if (++$n > $rslimit);
	my($tmpstr) = &conceptutils::conceptid2str($dbh, $conceptid);
	$html .= <<"EOD";
<TR>
<TD ALIGN=right><FONT SIZE=$fontsize>$n</FONT></TD>
<TD ALIGN=right><FONT SIZE=$fontsize><A HREF=\"$cgi?action=searchbyconceptid&arg=$conceptid&db=$db&meme_server=$meme_server&meme_home=$meme_home\#report\">$conceptid<A></FONT></TD>
<TD><FONT SIZE=$fontsize>$tmpstr</FONT></TD>
</TR>
EOD
    }

    $searchtime = time - $searchtime;
    $searchsecs = ($searchtime == 1 ? "second" : "seconds");

    $matchstr = ($n_matches == 1 ? "was 1 match" : "were $n_matches matches");

    &print_html("Approximate Word Query in MID", <<"EOD");
<FONT SIZE=$fontsize>Time to search: $searchtime $searchsecs</FONT><BR>

<P>

<FONT SIZE=$fontsize>
There $matchstr to your query: "$str". $best
Select the concept ID to see the matching concept report.
</FONT>

<P>

<TABLE CELLSPACING=1 CELLPADDING=5 BORDER=1 WIDTH="90%">
<TR>
<TH></TH>
<TH WIDTH="10%"><FONT SIZE=$fontsize>Concept ID</FONT></TH>
<TH><FONT SIZE=$fontsize>Preferred Atom Name</FONT></TH>
</TR>
$html
</TABLE>
<P><HR>
<H1>See Also</H1>
<UL>
<LI><FONT SIZE=$fontsize>Search the last release for "<A HREF="$conceptReportRelease?action=searchbynormstr&arg=$cgistr&$filestosearch">$str</A>"</FONT>
<LI><FONT SIZE=$fontsize>Search MEDLINE for "<A HREF=\"http://www.ncbi.nlm.nih.gov/htbin-post/Entrez/query?db=m&form=4&term=$cgistr\">$str</A>" using <A HREF=\"http://www.ncbi.nlm.nih.gov/PubMed\">Pubmed</A></FONT>
</UL>
EOD
}

# makes a report given a concept_id, title and body
sub report {
    my($conceptid) = @_;
#    my($CONCEPT) = "/site/umls/oracle/concept-reports/meme4-report.pl";
    my($cmd);
    my($report);
    my($rellimit) = $rellimit || 500; # max rels per report
#    my($options) = "-d $db -u $oracleUSER -c $conceptid";
    my($options) = "-d $db -c $conceptid";
    my($numrels);
    my($ignorerellimit) = 1 || $query->param('ignorerellimit'); # turned off Suresh 6/27/2001

    $meme_home = $meme_home || $ENV{MEME_HOME};
#    $ENV{'MEME_HOME'} = $meme_home;

    $cmd = "$meme_home/bin/xreports.pl -html -c $conceptid -d $db";
#                    -url_release_for_sty=
    $cmd .= " -r " . $query->param('r') if $query->param('r');
    $cmd .= " -x " . $query->param('x') if $query->param('x');
    if ($meme_server ne "meme_server") {
      $cmd .= " -host " . $midsvcs->{$meme_server . "-host"};
      $cmd .= " -port " . $midsvcs->{$meme_server . "-port"};
    }
    print STDERR $cmd;

    $report = `$cmd`;
#    $cmd = "$CONCEPT $options";
#    open(RPT, "$cmd|") || die "Cannot open report for $cmd";
#    while (<RPT>) {
#	$report .= $_;
#    }
#    close(RPT);
    return($report);
}

# ignores case
sub get_exact_matches {
    my($str) = @_;
    my($normstr);
    my(%cuis);
    my($SQL);
    my($strl);
    my($c, $s);
    my($sourceRestriction);

    if (@restrictToSources) {
	$sourceRestriction = "and c.source in (" . join(',', map { "'$_'" } @restrictToSources) . ")";
    }

    if ($normfn eq "norm") {
	$normstr = &LVG->($str);
    } else {
	$normstr = LVG->luinorm($str);
    }

    ($strl = $str) =~ tr/A-Z/a-z/;

    $SQL = <<"EOD";
SELECT c.concept_id, n.normstr from classes c, normstr n WHERE
	c.atom_id = n.normstr_id AND n.normstr=\'$normstr\' $sourceRestriction
EOD
    my(@rows) = $dbh->selectAllAsRef($SQL);
    foreach $ref (@rows) {
	$c = $ref->[0];
	$s = $ref->[1];
	$s =~ tr/A-Z/a-z/;
	next if $s ne $strl;
	$cuis{$c}++;
    }
    return(\%cuis);
}

sub get_normstr_matches {
    my($normstr) = @_;
    my(%cuis);
    my($SQL);
    my($sourceRestriction);

    if (@restrictToSources) {
	$sourceRestriction = "AND c.source IN (" . join(',', map { "'$_'" } @restrictToSources) . ")";
    }

    $SQL = <<"EOD";
SELECT DISTINCT c.concept_id FROM classes c, normstr n WHERE
	c.atom_id = n.normstr_id AND n.normstr=\'$normstr\' $sourceRestriction
EOD

    my(@cuis) = $dbh->selectAllAsArray($SQL);
    foreach (@cuis) {
	$cuis{$_}++;
    }
    return(\%cuis);
}

sub get_normword_matches {
    my($normword) = @_;
    my(%cuis);
    my($SQL);
    my($sourceRestriction);
    my($status, $logfile, $tmpfile);

    if (@restrictToSources) {
	$sourceRestriction = "and c.source in (" . join(',', map { "'$_'" } @restrictToSources) . ")";
    }

    $SQL = <<"EOD";
SELECT DISTINCT c.concept_id FROM classes c, normwrd n WHERE c.atom_id = n.normwrd_id AND
    n.normwrd=\'$normword\' $sourceRestriction
EOD
    my(@cuis) = $dbh->selectAllAsArray( $SQL);

    $wordfreq{$normword} = 0;
    foreach (@cuis) {
	$cuis{$_}++;
	$wordfreq{$normword}++;
    }
    return(\%cuis);
}

# makes up HTML for source restrictions in norm searches
sub source2html {
    my($html);
    my($refresh) = $query->param('refreshsourcecache');

    $refresh = 1;
    if ($refresh) {
      unlink $sourcefile;
      my($sql) = "select distinct current_name from source_version order by current_name";
      $dbh->selectToFile($sql, $sourcefile);
    }

    if (-e $sourcefile) {
	my($short, $long);
	my($srcfd) = gensym;

	$html = <<"EOD";
<P>
Restrict matches to sources (<A HREF="/Sources/sources.html">descriptions</A>): <SELECT NAME="source" MULTIPLE SIZE=2>
EOD
	open($srcfd, $sourcefile);
	while (<$srcfd>){
	    chop;
	    ($short, $long) = split /\|/, $_;
	    $html .= ($long ? "<OPTION VALUE=\"$short\">$long" : "<OPTION VALUE=\"$short\">$short");
	}
	close($srcfd);
	$html .= "</SELECT>";

    } else {
	$html = "";
    }
    return($html);
}

# returns the word frequency in the database (using normwrd)
sub wordfreq {
    my($normword) = @_;
    my($SQL);
    my($freq);

    return $wordfreq{$normword} if $wordfreq{$normword};

    $SQL = <<"EOD";
SELECT COUNT(*) FROM normwrd where normwrd = \`$normword\'
EOD

    $freq = $dbh->selectFirstAsScalar($SQL) || 0;
    return($freq);
}

# returns the number of rels that a concept has
sub num_rels {
    my($conceptid) = @_;
    my($SQL);
    my($n1, $n2);

    $SQL = <<"EOD";
SELECT COUNT(DISTINCT relationship_id) FROM relationships WHERE concept_id_1 = $conceptid
EOD
    $n1 = $dbh->selectFirstAsScalar($SQL) || 0;
    $SQL = <<"EOD";
SELECT COUNT(DISTINCT relationship_id) FROM relationships WHERE concept_id_2 = $conceptid
EOD
    $n2 = $dbh->selectFirstAsScalar( $SQL) || 0;
    return($n1+$n2);
}

sub print_trailer {
    print <<"EOD";
<P>
<HR WIDTH=600 ALIGN=left>
<FONT SIZE=$fontsize>
<ADDRESS><A HREF="$cgi">Concept reports from current MID</A></ADDRESS>
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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2//EN">
EOD
}

sub print_header {
    my($title, $header, $bodyargs) = @_;

    $title = "Concept Report" unless $title;
    $header = $title unless $header;

    &print_cgi_header("");
    print <<"EOD";
<HTML>
<HEAD>
<TITLE>$title</TITLE>

<STYLE TYPE="text/css">
    BODY { FONT-SIZE: $fontsize }
    SELECT { FONT-SIZE: $fontsize }
    INPUT { FONT-SIZE: $fontsize }
    BUTTON { FONT-SIZE: $fontsize }
</STYLE>

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

