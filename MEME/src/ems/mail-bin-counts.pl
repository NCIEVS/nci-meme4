#!@PATH_TO_PERL@

# Mails a report summarizing the editing actions for a specific day (default is the previous day)
# suresh@nlm.nih.gov 8/99
# - modified for MEME-III 8/00
# - EMS-3 mods 3/2005

# Command line options:
# -d <database>
# -t YYYYMMDD (report for this day - default is yesterday)
# -c (alternate config file in $EMS_HOME/config)
# -g (debug - just print the report to STDOUT)

# -r <alternative list of recipients - debug>
# -b <alternative subject line>
BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use OracleIF;
use GeneralUtils;
use EMSUtils;
use Midsvcs;
use EMSNames;
use EMSTables;

use Getopt::Std;

use CGI;
$query = new CGI;

getopts("d:t:c:gr:b:");

$starttime = time;

@months = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);

# day is yesterday unless specified
unless ($opt_t) {
  my(@y) = localtime time-24*60*60;
  $yyyymmdd = sprintf("%.4d%.2d%.2d", $y[5]+1900, $y[4]+1, $y[3]);
} else {
  $yyyymmdd = $opt_t;
}

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
$config = $opt_c || "ems.config";
$ENV{EMS_CONFIG} = $config;
EMSUtils->loadConfig;

#$ENV{MIDSVCS_HOME} = $EMSCONFIG{MIDSVCS_HOME};
#$ENV{LVGIF_HOME} = $EMSCONFIG{LVGIF_HOME};
#$ENV{DBPASSWORD_HOME} = $EMSCONFIG{DBPASSWORD_HOME};


$db = $opt_d || Midsvcs->get($opt_s || 'editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "ERROR: Database $db is unavailable\n" unless $dbh;

$now = GeneralUtils->date;
$currentdate = GeneralUtils->date("+%d");
$currentmonth = GeneralUtils->date("+%m");
$currentyear = GeneralUtils->date("+%Y");

if ($opt_r) {
  @recipients = split /[,\s]+/, $opt_r;
} else {
  @recipients = @{ $main::EMSCONFIG{EDITING_REPORT_RECIPIENT} };
}
$emsurl = $main::EMSCONFIG{HOMEPAGEURL} . $main::EMSCONFIG{LEVEL0EMSURL} . "?db=$db&action=daily_report";

unless (@recipients) {
  die "No mail recipients spacified.\n" unless $opt_t;
}
$to = \@recipients;
#$from = "EMS_Daily_editing_report";

# Environment
$ENV{'PATH'} = "/bin:$ENV{'ORACLE_HOME'}/bin";

$SNAPSHOTTABLE = $EMSNames::DAILYSNAPSHOTTABLE;
$ACTIONCOUNTTABLE = $EMSNames::DAILYACTIONCOUNTTABLE;

die "ERROR: date expected in format YYYYMMDD" unless $yyyymmdd =~ /^(\d{4})(\d{2})(\d{2})$/;
$yyyy = $1;
$mm = $2;
$dd = $3;
die "ERROR: Year should be a valid year!" unless $yyyy > 1980 && $yyyy < 2500;
die "ERROR: Month should be a valid month!" unless $mm >= 1 && $mm <= 12;
die "ERROR: Date should be a valid date!" unless $dd >= 1 && $dd <= 31;

$theday = $months[$mm-1] . " $dd, $yyyy";

$title = ($opt_b || "Bin counts before insertion report") . " for $db";

# quoted forms
$ymd = $dbh->quote(join('-', $yyyy, $mm, $dd));
$fmt = $dbh->quote('YYYY-MM-DD');

$rpt .= <<"EOD";
$title
Database: $db
Time now: $now

EOD



    $sql = <<"EOD";
with qry as (
select bin_name, totalclusters, chemclusters, clinicalclusters, otherclusters from meow.ems3_bininfo where bin_name in ('demotions', 'nci', 'needsrel','ambig_no_pn')
)
select bin_name|| ' all' bin_type, totalclusters count from qry
union
select bin_name||' chem', chemclusters from qry
union
select bin_name||' clinical', clinicalclusters from qry
union
select bin_name||' others', otherclusters from qry
EOD
  @rows = $dbh->selectAllAsRef($sql);
  $rpt .= <<"EOD";
EOD

  $rpt .= <<"EOD";

Shown below are the total counts for the bins.

     Bin Type               Count
     ---------             -------
EOD

  foreach $ref (@rows) {
    ($bin_type, $count) = @{ $ref };
    $rpt .= "\n" ;
    $rpt .= sprintf("%20.30s  %8d\n",
                    $bin_type, $count);

}


if ($opt_g) {
  print $rpt;
} else {
  GeneralUtils->mailsender({
                            from=>$EMSCONFIG{ADMIN},
                            replyto=>$EMSCONFIG{ADMIN},
                            smtp=>$EMSCONFIG{SMTP},
                            to=>$to,
                            msg=>$query->pre($rpt),
                            subject=>$title,
                            ctype=>'text/html',
                           });

#  GeneralUtils->sendmail({subject=>$title, from=>$from, to=>$to, msg=>$rpt});
}
exit 0;
