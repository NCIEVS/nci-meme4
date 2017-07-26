#!@PATH_TO_PERL@

# Program to process requests in the report queue
# Should be run via cron every 5 minutes or so.

# Author: suresh@nlm.nih.gov 1/2003
#         suresh@nlm         3/2005 (EMS 3)

# Command line arguments:
#
# -c <alternate EMS config file, e.g., ems.config.12.12.2005>
# -g (debug)

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use Getopt::Std;
use EMSUtils;
use GeneralUtils;
use EMSReportRequest;
use Midsvcs;
use OracleIF;
use File::Path;
use Data::Dumper;

getopts("c:g");

die "ERROR: EMS_HOME not set\n" unless $ENV{EMS_HOME};
$ENV{EMS_CONFIG} = $opt_c if $opt_c;
EMSUtils->loadConfig;

#$ENV{MIDSVCS_HOME} = $EMSCONFIG{MIDSVCS_HOME};
#$ENV{DBPASSWORD_HOME} = $EMSCONFIG{DBPASSWORD_HOME};

# default database to use
$db = Midsvcs->get('editing-db');

$logdir = $ENV{EMS_LOG_DIR} . "/log";
mkpath $logdir, 0775 unless -d $logdir;

chomp($currentyear = `/bin/date "+%Y"`);
chomp($currentmonth = `/bin/date "+%m"`);

$logfile = join("/", $logdir, join(".", "reportqueue", $currentyear, "log"));
unless (-e $logfile) {
  system "/bin/touch $logfile";
  chmod(0775, $logfile) || die "Cannot chmod 0775 $logfile";
}

unless ($opt_g) {
  $runningfile = $ENV{EMS_HOME} . "/log/report-daemon.pid";

  $SIG{QUIT} = sub { unlink $runningfile; die "SIGQUIT"; };
  $SIG{INT} = sub { unlink $runningfile; die "SIGINT"; };
  $SIG{HUP} = sub { unlink $runningfile; die "SIGHUP"; };

  exit 0 if -e $runningfile;

  GeneralUtils->str2file(sprintf("%d\n", $$), $runningfile);

} else {

  foreach $request (EMSReportRequest->getAllRequests()) {
    print Dumper($request);
  }
  exit 0;
}

while (1) {
  $request = EMSReportRequest->getOldest();
  last unless $request;

  $starttime = time;
  $startdate = GeneralUtils->date;

  &log("\n" . "-" x 20 . $startdate . "-" x 20 . "\n");
  &log("EMS_HOME: " . $ENV{EMS_HOME});
  &log("\nBefore processing:\n" . "-" x 17 . "\n" . &request2str($request));

  &process_request($request);

  &log("\nAfter processing:\n" . "-" x 16 . "\n" . &request2str($request));

  &log("\n\nFinished processing request");
}
unlink $runningfile;
exit 0;

# processes the request
sub process_request {
  my($request) = @_;
  my($db) = $request->{db} || $db;
  my($msg);
  my($user, $password, $dbh);
  my($errmsg1, $errmsg2);

  unless ($db) {
    EMSReportRequest->error($request, "Database name not specified for report");
    return;
  }

  $user = $main::EMSCONFIG{ORACLE_USER};
  $password = GeneralUtils->getOraclePassword($user,$db);
  $dbh = new OracleIF({user=>$user, password=>$password, db=>$db});
  $request->{dbh} = $dbh;
  die "Error: Oracle Not Available for database: $db" unless $dbh;

  eval { EMSReportRequest->report($dbh, $request) };

  $errmsg1 = $@;
  if ($errmsg1) {
    EMSReportRequest->error($request, $errmsg1);
    $msg = "ERROR: Failed to generate report: $@\n";
  } else {
    $msg = "";
  }

  if ($request->{mailto}) {
    unless (grep { lc($request->{mailto}) eq lc($_) } @{  $EMSCONFIG{USEREMAIL} }) {
      EMSReportRequest->error($request, "Email address not in allowable list per config file: " . $request->{mailto});
    } else {
      eval {
	GeneralUtils->mailsender(
				 {
				  smtp=>$EMSCONFIG{SMTP},
				  from=>$EMSCONFIG{ADMIN},
				  fake_from=>"EMS_Report_Request",
				  replyto=>$EMSCONFIG{ADMIN},
				  to=>$request->{mailto},
				  ctype=>'text/html',
				  subject=>'WMS report request for ' . $request->{list},
				  msg=>$msg . $request->{mailmessage},
				 }
				);
      };

      $errmsg2 = $@;
      if ($errmsg2) {
	EMSReportRequest->error($request, $errmsg2);
      }
    }

    &log("\nERROR in report generator: $errmsg1") if $errmsg1;
    &log("\nERROR in mailing report: $errmsg2") if $errmsg2;

    &log("\nReport was generated successfully.") unless $errmsg2 || $errmsg1;

    
#    GeneralUtils->sendmail({
#			    subject=>"WMS report request fulfilled at: " . GeneralUtils->date,
#			    to=>$request->{mailto},
#			    legaladdresses=>$EMSCONFIG{email},
#			    from=>"WMS_report_generator",
#			    msg=>$msg . $request->{mailmessage},
#			   });
  }
  $dbh->disconnect;
}

sub request2str {
  my($request) = @_;

  return join("\n", map { join("=", $_, $request->{$_}) } sort keys %$request);
}

sub log {
  my($msg) = @_;
  open(L, ">>$logfile") || return;
  print L $msg, "\n";
  close(L);
}

sub log_and_die {
  my($msg) = @_;
  &log($msg);
  die $msg;
}
