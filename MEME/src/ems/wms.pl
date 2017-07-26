#!@PATH_TO_PERL@

# The top level CGI script for the Worklist Management System (WMS)
# suresh@nlm.nih.gov 2/98
# suresh@nlm.nih.gov 1/00 version 2 for Oracle
# suresh@nlm.nih.gov 5/2005 EMS3 mods
#
# Changes:
#  05/10/2005 BAC (1-B6CE3): Augmented canAccessEMS routine to allow ignore
#    $httpuser when accessing via UNIX
#

# CGI params:
# config=(alternate EMS config file)
# action=<WMS action>
# db=alternate database - default is that pointed to by editing-db MID service

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use Getopt::Std;
use Archive::Zip;
use Data::Dumper;

use OracleIF;
use EMSUtils;
use WMSUtils;
use GeneralUtils;
use ZipUtils;
use Midsvcs;
use EMSReportRequest;

use File::Path;
use File::Basename;

unshift @INC, "$ENV{EMS_HOME}/bin";
require "utils.pl";

use CGI;
$query = new CGI;

$config = $query->param('config') || "ems.config";
$ENV{EMS_CONFIG} = $config;
EMSUtils->loadConfig;

#$ENV{MIDSVCS_HOME} = $EMSCONFIG{MIDSVCS_HOME};
#$ENV{LVGIF_HOME} = $EMSCONFIG{LVGIF_HOME};
#$ENV{DBPASSWORD_HOME} = $EMSCONFIG{DBPASSWORD_HOME};

# WMS specific vars
$wmstimestamp = time;
$SESSIONID = sprintf("%d:%s", $wmstimestamp, $ENV{REMOTE_ADDR});
$wmstitle = "Worklist Management System";
$title = $wmstitle;
$VERSION = $EMSCONFIG{WMS_VERSION};
$program = "WMS";

$httpuser = $ENV{REMOTE_USER};
$unixuser = GeneralUtils->username;

# predicate for batch call or interactive via Web
$batchcall = ($ENV{REMOTE_ADDR} ? 0 : 1);

$EMSCONFIG{LEVEL2NICKNAME} = "level2" unless $EMSCONFIG{LEVEL2NICKNAME};
$EMSCONFIG{LEVEL1NICKNAME} = "level1" unless $EMSCONFIG{LEVEL1NICKNAME};
$EMSCONFIG{LEVEL0NICKNAME} = "level0" unless $EMSCONFIG{LEVEL0NICKNAME};

$db = $query->param('db') || Midsvcs->get($opt_s || 'editing-db');
$oracleuser = $EMSCONFIG{ORACLE_USER};
$oraclepassword = GeneralUtils->getOraclePassword($oracleuser,$db);
eval { $dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword"); };
&printhtml({db=>$db, body=>"Database: $db is unavailable", printandexit=>1}) if ($@ || !$dbh);
#modify for time format, alter oracle session
$dbh->executeStmt("alter session set NLS_DATE_FORMAT='dd-MON-yyyy hh24:mi:ss'");
#$ENV{ORACLE_HOME} = $EMSCONFIG{ORACLE_HOME};

# Used for HTTP GETs
$DBget = "db=$db&config=$config";
$DBpost = "<INPUT TYPE=hidden NAME=\"db\" VALUE=\"$db\"><INPUT TYPE=hidden NAME=\"config\" VALUE=\"$config\">";

# restrict the environment
$ENV{PATH} = "/bin:$ENV{ORACLE_HOME}/bin";
$cgi = $ENV{SCRIPT_NAME};
$fontsize="-1";

$action = $query->param('action') || "wms_home";
$action =~ tr/A-Z/a-z/;

# Global EMS/WMS status (open or closed)
if ($EMSCONFIG{EMS_STATUS} =~ /closed/i) {
  &printhtml({body=>"The EMS and WMS are currently closed for use by a directive in the EMS configuration file.", printandexit=>1});
}

# Are batch WMS scripts allowed?
if ($batchcall) {
  $batchcutoff = EMSMaxtab->get($dbh, $EMSNames::EMSBATCHCUTOFFKEY);
  if ($batchcutoff && lc($batchcutoff->{valuechar}) ne "no") {
    die "Batch processes are currently disallowed in the EMS/WMS.";
  }
}

# current editing epoch
$currentepoch = EMSUtils->getCurrentEpoch($dbh);

# Is editing CUTOFF? (reverse the sense of the "edit" column to be more user-friendly)
$editingCUTOFF = (lc($dbh->selectFirstAsScalar("select edit from DBA_CUTOFF") eq "n") ? "Yes" : "No");

# what is the highest level for this user
$userlevel = 0;
foreach $level (2, 1, 0) {
  $p = "HTTP_LEVEL" . $level . "_USER";
  if (grep { $_ eq $httpuser } @{ $EMSCONFIG{$p} }) {
    $userlevel = $level;
    last;
  }
}

# Check access
unless ($httpuser && EMSUtils->canAccessEMS($httpuser, $db)) {
  unless (grep { $_ eq $unixuser } @{ $main::EMSCONFIG{BATCH_UNIX_USER} }) {
    &printhtml({body=>"Neither the HTTP user: $httpuser, nor the UNIX user: $unixuser are allowed access at this time.", printandexit=>1});
  }
  $userlevel = 2;
}

# The EMS/WMS can be further restricted using MAXTAB data per DB
$x = EMSMaxtab->get($dbh, $EMSNames::EMSACCESSKEY);
$emsaccess = ($x ? $x->{valuechar} : "CONFIG");
$ACCESS = $emsaccess;
if ($emsaccess eq "CLOSED" && ($action ne 'db' && $action ne "access")) {
  my($html) = "The EMS and WMS are currently closed for use for database " . $query->b($db);

  $html .= $query->p;
  $html .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
  $html .= $query->hidden(-name=>'action', -value=>'db', -override=>1);
  $html .= $query->submit(-value=>"Change DB");
  $html .= $DBpost;
  $html .= $query->end_form;

  $html .= $query->p;
  $html .= $query->start_form(-method=>'POST', -action=>$main::EMSCONFIG{LEVEL2EMSURL});
  $html .= $query->hidden(-name=>'action', -value=>'access', -override=>1);
  $html .= $query->submit(-value=>"Change Access Control");
  $html .= $DBpost;
  $html .= $query->end_form;
  
  &printhtml({body=>$html . ".", printandexit=>1});
}

$logdir = $ENV{EMS_LOG_DIR} . "/log";
mkpath $logdir, 0775;
$tmpdir = $logdir;
chomp($currentyear = `/bin/date "+%Y"`);
chomp($currentmonth = `/bin/date "+%m"`);
$wmslogfile = join("/", $logdir, join(".", "wms", $db, $currentyear, $currentmonth, "log"));

# create the log files if necessary and make the files group writeable
foreach $f ($wmslogfile) {
  next if -e $f;
  system "/bin/touch $f";
  chmod(0775, $f) || die "Cannot chmod 0775 $f";
}

# For detailed SQL logging
if ($EMSCONFIG{LOGLEVEL} == 2) {
  $main::SQLLOGGING=1;
  $main::SQLLOGFILE=$wmslogfile;
}

# Actions can be restricted by user levels - specify the minimum level a user must be to run
%actionRestriction = (
	    db => 0,
	    config => 0,
	    pickwick => 0,
	    access => 2,
	    cutoff => 2,
	    make_checklist => 2,
	    wms_deleteworklists => 2,
);

$now = GeneralUtils->date;

$r = $actionRestriction{$action};
if ($r) {
  if (ref($r) eq "ARRAY") {
    foreach $l (@$r) {
      if ($userlevel < $l) {
	if ($httpuser) {
	  &printhtml({body=>"You logged in as user: $httpuser and you currently do not have the privileges to execute the action: $action.", printandexit=>1});
	} elsif ($unixuser) {
	  &printhtml({body=>"You logged in as UNIX user: $unixuser and you currently do not have the privileges to execute the action: $action.", printandexit=>1});
	}
      }
    }
  } else {
    if ($userlevel < $r) {
      if ($httpuser) {
	&printhtml({body=>"You logged in as user: $httpuser and you currently do not have the privileges to execute the action: $action.", printandexit=>1});
      } elsif ($unixuser) {
	&printhtml({body=>"You logged in as UNIX user: $unixuser and you currently do not have the privileges to execute the action: $action.", printandexit=>1});
      }
    }
  }
}

&printhtml({body=>"ERROR: No action specified", printandexit=>1}) unless $action;
EMSUtils->ems_log($wmslogfile, "Action: $action started.");

$@ = "";

# compile the action module needed
eval { require "$action.pl" };
if ($@) {
  &printhtml({body=>"Perl could not load code for action: $action." . $query->code($@), printandexit=>1});
} else {
  $fn = "do_" . $action;
  eval { &$fn };
  if ($@) {
    my($x) = $@;
    EMSUtils->ems_error_log($wmslogfile, "ERROR: $x");
    &printhtml({h1=>"ERROR in execution", body=>"Action: $action had errors in execution" . $query->p . $query->pre($x), printandexit=>1});
  }
}

EMSUtils->ems_log($wmslogfile, "Action $action completed.");
$dbh->disconnect;
exit 0;
