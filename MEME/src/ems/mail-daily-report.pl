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
unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";

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
$password = GeneralUtils->getOraclePassword($user);
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
$from = "EMS_Daily_editing_report";

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

$title = ($opt_b || "EMS v3 Daily Editing Report") . " for $theday";

# quoted forms
$ymd = $dbh->quote(join('-', $yyyy, $mm, $dd));
$fmt = $dbh->quote('YYYY-MM-DD');

$rpt .= <<"EOD";
$title
Database: $db
Time now: $now

EOD

# Is there any approval data for this day?
$sql = "select count(*) from $SNAPSHOTTABLE where to_char(snapshot_date, $fmt)=$ymd";
if ($dbh->selectFirstAsScalar($sql) > 0) {

# Approvals on this day
  $sql = <<"EOD";
SELECT snapshot_count from $SNAPSHOTTABLE
WHERE  snapshot_type='APPR'
AND    to_char(snapshot_date, $fmt) = $ymd
EOD
$approvals = $dbh->selectFirstAsScalar($sql) || 0;

# Distinct Approvals on this day
$sql = <<"EOD";
SELECT snapshot_count from $SNAPSHOTTABLE
WHERE  snapshot_type='DISTAPPR'
AND    to_char(snapshot_date, $fmt) = $ymd
EOD
$distinctApprovals = $dbh->selectFirstAsScalar($sql) || 0;

$rpt .= <<"EOD";
Concepts Approved this day: $approvals
                  Distinct: $distinctApprovals
EOD
} else {

  $rpt .= "No snapshot data was collected for $theday.";
}

# Any action data for this day?
$sql = "select count(*) from $ACTIONCOUNTTABLE where to_char(report_date, $fmt)=$ymd";
if ($dbh->selectFirstAsScalar($sql) > 0) {

  $sql = <<"EOD";
SELECT SUBSTR(authority,-3,3) AS editor, authority, concepts_approved, rels_inserted, stys_inserted, splits, merges, total_actions FROM $ACTIONCOUNTTABLE
WHERE  to_char(report_date, $fmt) = $ymd
AND    (authority LIKE 'S-%' OR authority LIKE 'E-%')
ORDER BY editor, authority
EOD
  @rows = $dbh->selectAllAsRef($sql);
  $total_actions = 0;
  foreach $ref (@rows) {
    $total_actions += $ref->[7];
  }
  $rpt .= <<"EOD";
Number of actions this day: $total_actions
EOD

  $rpt .= <<"EOD" if $total_actions > 0;

Shown below are editing statistics for each authority.  The E-{initials}
authority shows approvals done in the interface while the S-{initials}
authority counts batch or stamping approvals.  The percentages show
the proportion of each, by editor.

Authority  Actions  Concepts Approved  Rels Inserted  STYs Inserted  Splits  Merges
---------  -------  -----------------  -------------  -------------  ------  ------
EOD

# Compute totals
  foreach $ref (@rows) {
    ($editor, $auth, $appr, $rels, $stys, $splits, $merges, $actions) = @{ $ref };
    next unless $editor;
    $total{$editor} += $appr;
  }

  foreach $ref (@rows) {
    ($editor, $auth, $appr, $rels, $stys, $splits, $merges, $actions) = @{ $ref };
    next unless $editor;
    $frac = ($total{$editor} > 0 ? $appr*100.00/$total{$editor} : 0.0);
    $rpt .= "\n" if ($lastEditor && $lastEditor ne $editor);
    $rpt .= sprintf("%8.6s  %8d  %8d (%5.1f%%) %10d  %12d  %10d  %6d\n",
		    $auth, $actions, $appr, $frac, $rels, $stys, $splits, $merges);
    $lastEditor = $editor;
  }

} else {

  $rpt .= "\nNo action data was collected for $theday.\n";
}

$query = new CGI;
$x = "For more detail, follow this link to the EMS";
$link = "\n" . "-" x length($x) . "\n" . $query->a({-href=>$emsurl}, $x) . "\n";

$rpt .= $link;

if ($opt_g) {
  print $rpt;
} else {
  GeneralUtils->mailsender({
			    from=>$EMSCONFIG{ADMIN},
			    fake_from=>$from,
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
