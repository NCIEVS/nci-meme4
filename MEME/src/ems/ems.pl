#!@PATH_TO_PERL@

# The top level CGI script for the Editing Management System (EMS)
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
# action=<EMS action>
# db=alternate database - default is that pointed to by editing-db MID service

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";
push @INC, "$ENV{EMS_HOME}/lib";
use lib "$ENV{EMS_HOME}/lib";

use Getopt::Std;
use Archive::Zip;
use Data::Dumper;

use OracleIF;
use EMSUtils;
use GeneralUtils;
use ZipUtils;
use Midsvcs;
use EMSBinlock;
use LVG;

use File::Path;
use File::Basename;

push @INC, "$ENV{EMS_HOME}/bin";
require "utils.pl";

use CGI;
$query = new CGI;

$config = $query->param('config') || "ems.config";
$ENV{EMS_CONFIG} = $config;
EMSUtils->loadConfig;

#$ENV{MIDSVCS_HOME} = $EMSCONFIG{MIDSVCS_HOME};
#$ENV{LVGIF_HOME} = $EMSCONFIG{LVGIF_HOME};
#$ENV{DBPASSWORD_HOME} = $EMSCONFIG{DBPASSWORD_HOME};


$httpuser = $ENV{REMOTE_USER};
$unixuser = GeneralUtils->username;

$emstimestamp = time;
$SESSIONID = sprintf("%d:%s", $emstimestamp, ($ENV{REMOTE_ADDR} || join(":", GeneralUtils->nodename(), $unixuser)));
$emstitle = "Editing Management System";
$title = $emstitle;
$VERSION = $EMSCONFIG{EMS_VERSION};
$program = "EMS";

$httpuser = $ENV{REMOTE_USER};
$unixuser = GeneralUtils->username;

# predicate for batch call or interactive via Web
$batchcall = ($ENV{REMOTE_ADDR} ? 0 : 1);

$EMSCONFIG{LEVEL2NICKNAME} = "level2" unless $EMSCONFIG{LEVEL2NICKNAME};
$EMSCONFIG{LEVEL1NICKNAME} = "level1" unless $EMSCONFIG{LEVEL1NICKNAME};
$EMSCONFIG{LEVEL0NICKNAME} = "level0" unless $EMSCONFIG{LEVEL0NICKNAME};

$db = $query->param('db') || Midsvcs->get($opt_s || 'editing-db');
$oracleuser = $EMSCONFIG{ORACLE_USER};
$oraclepassword = GeneralUtils->getOraclePassword($oracleuser);
eval { $dbh = new OracleIF("db=$db&user=$oracleuser&password=$oraclepassword"); };
&printhtml({db=>$db, body=>"Database: $db is unavailable", printandexit=>1}) if ($@ || !$dbh);
#$ENV{ORACLE_HOME} = $EMSCONFIG{ORACLE_HOME};

# Used for HTTP GETs
$DBget = "db=$db&config=$config";
$DBpost = "<INPUT TYPE=hidden NAME=\"db\" VALUE=\"$db\"><INPUT TYPE=hidden NAME=\"config\" VALUE=\"$config\">";

# restrict the environment
$ENV{PATH} = "/bin:$ENV{ORACLE_HOME}/bin";
$cgi = $ENV{SCRIPT_NAME};
$fontsize="-1";

$action = $query->param('action') || "ems_home";
$action =~ tr/A-Z/a-z/;

# Global EMS/WMS status (open or closed)
if ($EMSCONFIG{EMS_STATUS} =~ /closed/i) {
  &printhtml({body=>"The EMS and WMS are currently closed for use by a directive in the EMS configuration file.", printandexit=>1});
}

# Are batch EMS scripts allowed?
if ($batchcall) {
  $batchcutoff = EMSMaxtab->get($dbh, $EMSNames::EMSBATCHCUTOFFKEY);
  if ($batchcutoff && lc($batchcutoff->{valuechar}) ne "no") {
    die "Batch processes are currently disallowed in the EMS/WMS.";
  }
}

# current editing epoch
eval { $currentepoch = EMSUtils->getCurrentEpoch($dbh); };
&printhtml({body=>"Error in determining current epoch." . $query->code($@), printandexit=>1}) if $@;

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
  unless (grep { $_ eq $unixuser } @{ $EMSCONFIG{BATCH_UNIX_USER} }) {
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
  $html .= $query->start_form(-method=>'POST', -action=>$query->url());
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

$logdir = $ENV{EMS_HOME} . "/log";
mkpath $logdir, 0775;

$tmpdir = $logdir;
chomp($currentyear = `/bin/date "+%Y"`);
chomp($currentmonth = `/bin/date "+%m"`);
$emslogfile = join("/", $logdir, join(".", "ems", $db, $currentyear, $currentmonth, "log"));
$partitionlogfile = join("/", $logdir, join(".", "partition", $db, $currentyear, $currentmonth, "log"));

# create the log files if necessary and make the files group writeable
foreach $f ($emslogfile, $partitionlogfile) {
  next if -e $f;
  system "/bin/touch $f";
  chmod(0775, $f) || die "Cannot chmod 0775 $f";
}

# For detailed SQL logging
if ($EMSCONFIG{LOGLEVEL} == 2) {
  $main::SQLLOGGING=1;
  $main::SQLLOGFILE=$emslogfile;
}

# Actions can be restricted by user levels - specify the minimum level a user must be to run
%actionRestriction = (
	    db => 0,
	    access =>2,
	    batch_cutoff => 2,
	    bin_config => 2,
	    ems_info => 0,
	    bin_config => 2,
	    midsvcs => 0,
	    ah_canonical => 0,
	    me_partition => 2,
	    me_checklist => 1,
	    me_worklist => 2,
	    ah_generate => 2,
	    ah_checklist => 1,
	    ah_worklist => [2],
	    qa_generate => [2],
	    qa_checklist => [2,1],
	    qa_worklist => [2],
	    matrixinit => [2],
	    epoch => [2],
	    locks => [2],
	    assigncuis => [2],
	    cutoff => [2],
	    config => 1,
	    db_refresh => 0,
	    concept_report => 0,
	    termgroup_rank => 0,
	    stylist => 0,
);

$now = GeneralUtils->date;


$r = $actionRestriction{$action};
if ($r) {
  if (ref($r) eq "ARRAY") {
    foreach $l (@$r) {
      if ($userlevel < $l) {
	if ($httpuser) {
	  my($m) = "You logged in as user: $httpuser and you currently do not have the privileges to execute the action: $action.";
	  EMSUtils->ems_error_log($emslogfile, $m);
	  &printhtml({body=>$m, printandexit=>1});
	} elsif ($unixuser) {
	  my($m) = "You logged in as UNIX user: $unixuser and you currently do not have the privileges to execute the action: $action.";
	  EMSUtils->ems_error_log($emslogfile, $m);
	  &printhtml({body=>"You logged in as UNIX user: $unixuser and you currently do not have the privileges to execute the action: $action.", printandexit=>1});
	}
      }
    }
  } else {
    if ($userlevel < $r) {
      if ($httpuser) {
	my($m) = "You logged in as user: $httpuser and you currently do not have the privileges to execute the action: $action.";
	EMSUtils->ems_error_log($emslogfile, $m);
	&printhtml({body=>$m, printandexit=>1});
      } elsif ($unixuser) {
	my($m) = "You logged in as UNIX user: $unixuser and you currently do not have the privileges to execute the action: $action";
	EMSUtils->ems_error_log($emslogfile, $m);
	&printhtml({body=>$m, printandexit=>1});
      }
    }
  }
}

&printhtml({body=>"ERROR: No action specified", printandexit=>1}) unless $action;
EMSUtils->ems_log($emslogfile, "Action: $action started.");

$@ = "";

# compile the action module needed
eval { require "$action.pl" };
if ($@) {
  $error = $@;
  EMSUtils->ems_error_log($emslogfile, "ERROR: Perl could not load code for action: $action: $error");
  &printhtml({body=>"Perl could not load code for action: $action." . $query->code($error), printandexit=>1});
} else {
  $fn = "do_" . $action;
  eval { &$fn };
  if ($@) {
    my($x) = $@;
    EMSUtils->ems_error_log($emslogfile, "ERROR: $x");
    &printhtml({h1=>"ERROR in execution", body=>"Action: $action had errors in execution" . $query->p . $query->b($x), printandexit=>1});
  }
}

EMSUtils->ems_log($emslogfile, "Action $action completed.");
$dbh->disconnect;
exit 0;
