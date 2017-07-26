#!@PATH_TO_PERL@

# Currently active Oracle sessions
# From a suggestion by Brian (carlsen@apelon.com)
# suresh@nlm.nih.gov 8/00

#$umlsOracleDir="/site/umls/oracle";
#$utilsDir="$umlsOracleDir/utils";
#$emsDir="$umlsOracleDir/ems-2.1";
#$emsSrcDir = "$emsDir/src";

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use OracleIF;
require "utils.pl";
use EMSUtils;
use GeneralUtils;
#require "ems-utils.pl";
use Midsvcs;
#require "midsvcs.pl";

use File::Basename;

use CGI;

# CGI params:
# service=
# db= (default is editing-db)

$query = new CGI;

$config = $query->param('config') || "ems.config";
$ENV{EMS_CONFIG} = $config;
EMSUtils->loadConfig;

$statedir="/tmp";
$tmpdir = (-e $statedir ? $statedir : "/tmp");
#$logoSmall = "/ems/logo-small.gif";

# ORACLE vars
#&oracleIF::init_oracle;
#$oracleUSER = "meow";
#$oraclePWD = &oracleIF::get_passwd("/etc/umls/oracle.passwd", $oracleUSER);
#$oracleAUTH = "$oracleUSER/$oraclePWD";
# either the MID service or the DB should be set, not both
#$MIDservice = $query->param('service');
#$oracleTNS = $query->param('db');
## Used for HTTP GETs
#$DBsvc = ($MIDservice ? "service=$MIDservice" : "db=$oracleTNS");
#$DBget = $DBsvc;
#$MIDservice = "editing-db" unless $MIDservice || $oracleTNS;
#$oracleTNS = &midsvcs::get_mid_service($MIDservice) unless $oracleTNS;
#$oracleDBH = undef; # DBD handle
$defaultTABLESPACE = "MID";

$db = $query->param('db') || Midsvcs->get($opt_s || 'editing-db');
$oracleuser = $EMSCONFIG{ORACLE_USER};
$oraclepassword = GeneralUtils->getOraclePassword($oracleuser,$db);
eval { $dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword"); };

$now = GeneralUtils->date;
$title="Oracle Sessions";
$cgi = $ENV{'SCRIPT_NAME'};

$fontsize = "-1";

unless ($dbh) {
    &printhtml($title, "Failed to find TNS for service: $db");
    return;
}

#$oracleDBH = &oracleIF::connectDBD($oracleAUTH, $oracleTNS);

# Is Oracle up?
if (!defined($dbh)) {
    &printhtml("", "Error: Oracle Not Available for TNS: $db.");
    exit 0;
}
$httpuser = $ENV{REMOTE_USER};
$unixuser = GeneralUtils->username;
#&print_cgi_header;
unless ($query->param('doit')) {
     my(@databases) = split /[,\s*]/, Midsvcs->get('databases');
    my($servicesHTML, $databasesHTML);
    @databases = @defaultDBs unless @databases;
    foreach (@databases) {
	my($service) = $_;
	$servicesHTML .= "<OPTION VALUE=$service><FONT SIZE=$fontsize>$service [$tns]</FONT>";
    }
$html = "<FONT SIZE=$fontsize>";
$html.= "This program will display information about active sessions in the Oracle database.
Please select a MID service or a specific database and submit this form:
<P>";
$html .= $query->p;
$html .= $query->start_form(-method=>'POST', -action=>$cgi);
$html .= "Click here for MID service:";
$html .= $query->submit(-value=>"editing-db"); 
$html .= $query->hidden(-name=>'doit', -value=>'1', -override=>1);
$html .= $query->hidden(-name=>'service', -value=>'editing-db', -override=>1); 
$html .= "</FORM>
<P>";
 $html .= $query->end_form;

$html .= $query->p;
  $html .= $query->start_form(-method=>'POST', -action=>$cgi);
$html .= "Or, select another MID database to use:";
  $html .= $query->popup_menu({-name=>'db', -default=>$db, -values=>\@databases, -onChange=>'submit();'});
  $html .= $query->p . $query->submit;
  $html .= $query->hidden(-name=>'doit', -value=>'1', -override=>1);
$html .= $query->end_form;
$html .= "<P>";
$html .= "</FONT>";
    &printhtml({body=>$html . "."  , printandexit=>1});

} else {
   $html = "";
    @rowRefs = &OracleIF::selectAllAsRef($dbh, <<"EOD");
SELECT a.username, a.osuser, a.schemaname, a.machine, a.terminal, a.type,
       a.logon_time, a.program, a.status, a.lockwait, b.sql_text
FROM   V\$SESSION a, V\$SQL b
WHERE  a.username IS NOT NULL
AND    a.sql_address=b.address
ORDER  BY a.logon_time DESC
EOD

    foreach $ref (@rowRefs) {
	$html .= "<TR>";
	foreach $i (0..9) {
	    $rowspan = ($i < 1 ? "ROWSPAN=2" : "");
	    $html .= "<TD $rowspan><FONT SIZE=$fontsize>$ref->[$i]</FONT><BR></TD>";
        }
	$html .= "</TR>";
	$html .= "<TR><TD COLSPAN=9><FONT SIZE=$fontsize>$ref->[10]</FONT><BR></TD></TR>";
    }

    unless (@rowRefs) {
	&printhtml($body=>"There were no active sessions.");
    } else {
	$html_print = "Currently Active Sessions";
$html_print .= "<P> <TABLE BORDER=1 CELLPADDING=5 CELLSPACING=0> <TR> <TH><FONT SIZE=$fontsize>User Name</FONT></TH> <TH><FONT SIZE=$fontsize>OS user</FONT></TH>";
$html_print .= "<TH><FONT SIZE=$fontsize>Schema Name</FONT></TH> <TH><FONT SIZE=$fontsize>Machine</FONT></TH> <TH><FONT SIZE=$fontsize>Terminal</FONT></TH> <TH><FONT SIZE=$fontsize>Session Type</FONT></TH>";

$html_print .= "<TH><FONT SIZE=$fontsize>Logon Time</FONT></TH> <TH><FONT SIZE=$fontsize>Program</FONT></TH> <TH><FONT SIZE=$fontsize>Status</FONT></TH>";
$html_print .= "<TH><FONT SIZE=$fontsize>Lockwait</FONT></TH> </TR>";
$html_print .= $html;
$html_print .= "</TABLE>";
	&printhtml({body=>$html_print . "."  , printandexit=>1});
    }
}
$dbh->disconnect;
exit 0;
