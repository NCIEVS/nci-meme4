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
use GeneralUtils;
use ZipUtils;
use Midsvcs;
use EMSBinlock;
use LVG;

use File::Path;
use File::Basename;

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


Additions To NOT_sy

MESH|AMBIG
alzheimer disease|dementia
amnesia|lom - loss of memory
amnesia|loss of memory
amnesia|memory loss
amnesia|memory loss nos
ancylostomiasis|hookworm infection
ankle joint|tarsal joint
ankle joint|tarsus
atherosclerosis|arteriosclerosis
atherosclerosis|arteriosclerotic vascular disease
blood grouping and crossmatching|blood type
cardiomyopathy, hypertrophic, familial|cardiomyopathies, idiopathic hypertrophic
cardiomyopathy, hypertrophic, familial|cardiomyopathy, idiopathic hypertrophic
cardiomyopathy, hypertrophic, familial|hypertrophic cardiomyopathies, idiopathic
cardiomyopathy, hypertrophic, familial|hypertrophic cardiomyopathy, idiopathic
cardiomyopathy, hypertrophic, familial|hypertrophic subaortic stenosis
cardiomyopathy, hypertrophic, familial|hypertrophic subaortic stenosis (idiopathic)
cardiomyopathy, hypertrophic, familial|idiopathic hypertrophic cardiomyopathies
cardiomyopathy, hypertrophic, familial|idiopathic hypertrophic cardiomyopathy
cardiomyopathy, hypertrophic, familial|idiopathic hypertrophic subaortic stenosis
cardiomyopathy, hypertrophic, familial|idiopathic hypertrophic subaortic stenosis (disorder)
cardiomyopathy, hypertrophic, familial|idiopathic hypertrophic subaortic stenosis (ihss)
cardiomyopathy, hypertrophic, familial|idiopathic hypertrophic subvalvular stenosis
cardiomyopathy, hypertrophic, familial|ihss
cardiomyopathy, hypertrophic, familial|ihss (idiopathic hypertrophic subaortic stenosis)
cardiomyopathy, hypertrophic, familial|ihsss
cardiomyopathy, hypertrophic, familial|muscular subaortic stenosis
cardiomyopathy, hypertrophic, familial|subvalvular stenosis, idiopathic hypertrophic
cocaine|coca
coronary artery disease|coronary disease
coronary artery disease|coronary heart disease
coronary disease|coronary (artery) disease
deafness|hearing loss
dental clinics|dental
fetus|fetal mummification
fetus|fetal mummifications
fetus|fetus, mummified
fetus|mummification, fetal
fetus|mummifications, fetal
fetus|mummified fetus
fetus|mummified fetus (disorder)
fetus|mummified foetus
hearing loss|deafness
hip dislocation, congenital|hip dysplasia
hypoxia, brain|encephalopathies, hypoxic-ischemic
hypoxia, brain|encephalopathies, ischemic-hypoxic
hypoxia, brain|encephalopathy hypoxic ischemic
hypoxia, brain|encephalopathy hypoxic-ischemic
hypoxia, brain|encephalopathy, hypoxic ischemic
hypoxia, brain|encephalopathy, hypoxic-ischemic
hypoxia, brain|encephalopathy, ischemic-hypoxic
hypoxia, brain|hypoxic ischemic brain injury
hypoxia, brain|hypoxic-ischaemic brain injury
hypoxia, brain|hypoxic-ischemic brain injury
hypoxia, brain|hypoxic-ischemic encephalopathies
hypoxia, brain|hypoxic-ischemic encephalopathy
hypoxia, brain|hypoxic-ischemic encephalopathy (disorder)
hypoxia, brain|ischemic hypoxic encephalopathy
hypoxia, brain|ischemic-hypoxic encephalopathies
hypoxia, brain|ischemic-hypoxic encephalopathy
hypoxia-ischemia, brain|anoxic encephalopathy
hypoxia-ischemia, brain|brain damage due to hypoxia
hypoxia-ischemia, brain|brain damage, hypoxic
hypoxia-ischemia, brain|brain disorder resulting from a period of impaired oxygen delivery to the brain
hypoxia-ischemia, brain|brain disorder resulting from a period of impaired oxygen delivery to the brain (disorder)
hypoxia-ischemia, brain|damage, hypoxic brain
hypoxia-ischemia, brain|encephalopathies, hypoxic
hypoxia-ischemia, brain|encephalopathy hypoxic
hypoxia-ischemia, brain|encephalopathy, hypoxic
hypoxia-ischemia, brain|hypoxic brain damage
hypoxia-ischemia, brain|hypoxic brain injuries
hypoxia-ischemia, brain|hypoxic brain injury
hypoxia-ischemia, brain|hypoxic encephalopathies
hypoxia-ischemia, brain|hypoxic encephalopathy
information systems|information management system
information systems|information management systems
information systems|information system, management
information systems|information systems management
information systems|information systems, management
information systems|management information system
information systems|management information systems
information systems|system management information
information systems|system, information
information systems|system, management information
information systems|systems, management information
lemuridae|lemur
leukemia, feline|felv
lymphoma, non-hodgkin|germinoblastic sarcoma
lymphoma, non-hodgkin|germinoblastic sarcomas
lymphoma, non-hodgkin|germinoblastoma
lymphoma, non-hodgkin|germinoblastomas
lymphoma, non-hodgkin|lymphoma
lymphoma, non-hodgkin|lymphoma (clinical)
lymphoma, non-hodgkin|lymphoma [disease/finding]
lymphoma, non-hodgkin|lymphoma malignant
lymphoma, non-hodgkin|lymphoma nos
lymphoma, non-hodgkin|lymphoma, malignant
lymphoma, non-hodgkin|lymphoma, nos
lymphoma, non-hodgkin|lymphomas
lymphoma, non-hodgkin|lymphomas malignant
lymphoma, non-hodgkin|lymphomas, malignant
lymphoma, non-hodgkin|lymphomatous
lymphoma, non-hodgkin|malignant lymphoma
lymphoma, non-hodgkin|malignant lymphoma - category
lymphoma, non-hodgkin|malignant lymphoma - category (morphologic abnormality)
lymphoma, non-hodgkin|malignant lymphoma (clinical)
lymphoma, non-hodgkin|malignant lymphoma (disorder)
lymphoma, non-hodgkin|malignant lymphoma nos
lymphoma, non-hodgkin|malignant lymphoma, no icd-o subtype
lymphoma, non-hodgkin|malignant lymphoma, no international classification of diseases for oncology subtype
lymphoma, non-hodgkin|malignant lymphoma, no international classification of diseases for oncology subtype (morphologic abnormality)
lymphoma, non-hodgkin|malignant lymphoma, nos
lymphoma, non-hodgkin|malignant lymphomas
lymphoma|diffuse lymphoma
lymphoma|diffuse lymphomas
lymphoma|lymphatic sarcoma
lymphoma|lymphatic sarcomas
lymphoma|lymphoma diffuse
lymphoma|lymphoma, diffuse
lymphoma|lymphomas, diffuse
lymphoma|lymphosarcoma
lymphoma|lymphosarcoma (disorder)
lymphoma|lymphosarcoma [obs]
lymphoma|lymphosarcoma diffuse nos
lymphoma|lymphosarcoma nos
lymphoma|lymphosarcoma type malignant lymphoma
lymphoma|lymphosarcoma, diffuse
lymphoma|lymphosarcoma, diffuse [obs]
lymphoma|lymphosarcoma, malignant
lymphoma|lymphosarcoma, nos
lymphoma|lymphosarcomas
lymphoma|malignant lymphoma, lymphosarcoma type
lymphoma|reticulolymphosarcoma
lymphoma|reticulolymphosarcoma, diffuse
lymphoma|reticulolymphosarcomas
lymphoma|sarcoma, lymphatic
lymphoma|sarcomas, lymphatic
malnutrition|disease nutrition
malnutrition|disease nutritional
malnutrition|diseases nutrition
malnutrition|diseases nutritional
malnutrition|diseases of nutrition
malnutrition|disorder nutrition
malnutrition|disorder of nutrition
malnutrition|disorders nutrition
malnutrition|nutrition disease
malnutrition|nutrition diseases
malnutrition|nutrition disorder
malnutrition|nutrition disorders
malnutrition|nutrition disorders [disease/finding]
malnutrition|nutritional disease
malnutrition|nutritional disease, nos
malnutrition|nutritional disorder
malnutrition|nutritional disorder (disorder)
malnutrition|nutritional disorder, nos
malnutrition|nutritional disorders
malnutrition|nutritional disorders: general terms
malnutrition|nutritional problem
malnutrition|nutritional problems
malocclusion, angle class ii|angle class ii
malocclusion, angle class ii|bite, deep
malocclusion, angle class ii|bites, deep
malocclusion, angle class ii|brachygnathia
malocclusion, angle class ii|brachygnathism
malocclusion, angle class ii|brachygnathism (disorder)
malocclusion, angle class ii|deep bite
malocclusion, angle class ii|deep bites
malocclusion, angle class ii|deep-bite
malocclusion, angle class ii|deep-bites
malocclusion, angle class ii|mandibular distoclusion
malocclusion, angle class ii|ob - overbite
malocclusion, angle class ii|overbite
malocclusion, angle class ii|overbite (observable entity)
malocclusion, angle class ii|overbite [disease/finding]
malocclusion, angle class ii|overbites
malocclusion, angle class ii|overshot jaw
malocclusion, angle class ii|parrot mouth
malocclusion, angle class ii|receding jaw
management information systems|information system
management information systems|information systems
management information systems|system information
management information systems|system, information
management information systems|systems, information
mesothelioma|mesothelial neoplasm
mesothelioma|mesothelial neoplasm (morphologic abnormality)
mesothelioma|mesothelial neoplasms
mesothelioma|mesothelial tumor
mesothelioma|neoplasm, mesothelial
mesothelioma|neoplasms, mesothelial
mesothelioma|neoplasms, mesothelial [disease/finding]
metacarpal bones|distal segment of hand proper
metacarpal bones|metacarpal part of hand
metacarpal bones|metacarpal region
metacarpal bones|metacarpus
metacarpal bones|regio metacarpalis
metacarpus|bone structure of metacarpal
metacarpus|bone structure of metacarpal (body structure)
metacarpus|bones metacarpal
metacarpus|bones, metacarpal
metacarpus|distal segment of hand proper
metacarpus|metacarpal
metacarpus|metacarpal bone
metacarpus|metacarpal bone, nos
metacarpus|metacarpal bones
metacarpus|metacarpal, nos
metacarpus|metacarpals
metacarpus|os metacarpale
metatarsal bones|metatarsus
metatarsus|metatarsals
multicystic dysplastic kidney|kidney, polycystic
multicystic dysplastic kidney|kidneys, polycystic
multicystic dysplastic kidney|polycystic kidney
multicystic dysplastic kidney|polycystic kidney - body part
multicystic dysplastic kidney|polycystic kidneys
neoplasms, mesothelial|mesothelioma
neoplasms, mesothelial|mesothelioma [disease/finding]
neoplasms, mesothelial|mesothelioma, nos
neoplasms, mesothelial|mesothelioma, undetermined
neoplasms, mesothelial|mesothelioma, unspecified
neoplasms, mesothelial|mesotheliomas
nutrition disorders|acquired deficiency
nutrition disorders|deficiencies disorders nutritional
nutrition disorders|deficiencies nutritional
nutrition disorders|deficiency
nutrition disorders|deficiency disorders nutritional
nutrition disorders|deficiency nutritional
nutrition disorders|deficiency state
nutrition disorders|dietary deficiency
nutrition disorders|malnourished
nutrition disorders|malnutrition
nutrition disorders|malnutrition (e40-e46)
nutrition disorders|malnutrition [disease/finding]
nutrition disorders|malnutrition nos
nutrition disorders|malnutrition syndrome
nutrition disorders|nutrition poor
nutrition disorders|nutritional deficiencies
nutrition disorders|nutritional deficiency
nutrition disorders|nutritional deficiency (disorder)
nutrition disorders|nutritional deficiency (finding)
nutrition disorders|nutritional deficiency disorder
nutrition disorders|nutritional deficiency disorder, nos
nutrition disorders|nutritional deficiency, nos
nutrition disorders|nutritional deficiency, unspecified
nutrition disorders|poor nutrition
nutrition disorders|underfed
nutrition disorders|undernourished
nutrition disorders|undernourished (finding)
nutrition disorders|undernutrition
nutrition disorders|undernutrition (disorder)
nutrition disorders|undernutrition syndrome
nutrition disorders|undernutrition syndrome, nos
nutrition disorders|undernutrition, nos
nutrition disorders|unspecified nutritional deficiency
overbite|angle class ii
overbite|brachygnathia
overbite|brachygnathism
overbite|brachygnathism (disorder)
overbite|class ii malocclusion
overbite|class ii, angle
overbite|malocclusion, angle class ii
overbite|malocclusion, angle class ii (disorder)
overbite|malocclusion, angle class ii [disease/finding]
overbite|malocclusion, angle's class ii
overbite|mandibular distoclusion
overbite|overshot jaw
overbite|parrot mouth
overbite|receding jaw
paramyxoviridae infections|disease due to paramyxovirus, nos
paramyxoviridae infections|disease due to respirovirus
paramyxoviridae infections|disease due to respirovirus (disorder)
paramyxoviridae infections|infections, respirovirus
paramyxoviridae infections|respirovirus infections
paramyxoviridae infections|respirovirus infections [disease/finding]
parrots|psittacine
passeriformes|flycatcher
polycystic kidney diseases|dysplasia, multicystic kidney
polycystic kidney diseases|dysplasia, multicystic renal
polycystic kidney diseases|dysplasias, multicystic kidney
polycystic kidney diseases|dysplasias, multicystic renal
polycystic kidney diseases|kidney dysplasia, multicystic
polycystic kidney diseases|kidney dysplasias, multicystic
polycystic kidney diseases|mckd - multicystic kidney disease
polycystic kidney diseases|multicystic dysplastic kidney
polycystic kidney diseases|multicystic dysplastic kidney [disease/finding]
polycystic kidney diseases|multicystic dysplastic kidneys
polycystic kidney diseases|multicystic kidney
polycystic kidney diseases|multicystic kidney (disorder)
polycystic kidney diseases|multicystic kidney dysplasia
polycystic kidney diseases|multicystic kidney dysplasias
polycystic kidney diseases|multicystic kidneys
polycystic kidney diseases|multicystic renal dysplasia
polycystic kidney diseases|multicystic renal dysplasias
polycystic kidney diseases|renal dysplasia, multicystic
polycystic kidney diseases|renal dysplasias, multicystic
pongo|orangutan
proboscidea mammal|elephants
psittaciformes|parrot
respirovirus infections|disease due to paramyxoviridae
respirovirus infections|disease due to paramyxoviridae (disorder)
respirovirus infections|disease due to paramyxovirus, nos
respirovirus infections|diseases due to paramyxoviridae
respirovirus infections|diseases due to paramyxovirus
respirovirus infections|infection, paramyxoviridae
respirovirus infections|infections, paramyxoviridae
respirovirus infections|paramyxoviridae disease
respirovirus infections|paramyxoviridae infection
respirovirus infections|paramyxoviridae infections
respirovirus infections|paramyxoviridae infections [disease/finding]
respirovirus infections|paramyxovirus infection
respirovirus infections|paramyxovirus infection, nos
respirovirus infections|respirovirus infections
salmon|humpback
scandentia|tree shrew
severe acute respiratory syndrome|coronavirus infections
sirenia|manatee
sirenia|sea cow
thymoma|neoplasm of the thymus
thymoma|neoplasm of thymus
thymoma|neoplasm of thymus (disorder)
thymoma|neoplasm, thymic
thymoma|neoplasm, thymus
thymoma|neoplasms, thymic
thymoma|neoplasms, thymus
thymoma|thymic neoplasm
thymoma|thymic neoplasms
thymoma|thymic tumor
thymoma|thymic tumors
thymoma|thymus neoplasm
thymoma|thymus neoplasms
thymoma|thymus neoplasms [disease/finding]
thymoma|thymus tumor
thymoma|thymus tumors
thymoma|tumor of the thymus
thymoma|tumor of thymus
thymoma|tumor, thymic
thymoma|tumor, thymus
thymoma|tumors, thymic
thymoma|tumors, thymus
thymoma|tumour of thymus
thymoma|tumour thymus
thymus neoplasms|thymoma
thymus neoplasms|thymoma (disorder)
thymus neoplasms|thymoma [disease/finding]
thymus neoplasms|thymoma, no icd-o subtype
thymus neoplasms|thymoma, no international classification of diseases for oncology subtype
thymus neoplasms|thymoma, no international classification of diseases for oncology subtype (morphologic abnormality)
thymus neoplasms|thymoma, nos
thymus neoplasms|thymoma, undetermined
thymus neoplasms|thymomas
trichechus manatus|sea cow
tympanic membrane|tympanum
vaginal smears|cervical pap smear
vaginal smears|cervical pap test
vaginal smears|cervical papanicolaou test
vaginal smears|pap smear
vaginal smears|pap smear procedure
vaginal smears|pap smears
vaginal smears|pap test
vaginal smears|pap test, cervical
vaginal smears|pap testing
vaginal smears|pap tests
vaginal smears|papanicolaou smear
vaginal smears|papanicolaou smear procedure
vitamin d deficiency|hypovitaminosis d
wrist joint|carpal joint





















































































































































































































































































































































































































































































































































































































































































































































































































































































