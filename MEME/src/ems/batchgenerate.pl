#!@PATH_TO_PERL@

# Script to batch generate QA bin contents

# suresh@nlm.nih.gov - EMS 3 (11/2005)

# Environment variables
# EMS_HOME (required)
# EMS_CONFIG (optional)

# Command line arguments:
# -d <database> (by default the MID name server is used)
# -c (alternate config)
# -b (comma separated bin names or bin numbers or numeric bin ranges, e.g., 4-10)
# -t (bin type QA or AH)
# -g (debug messages)

# Default behavior is to generate contents for one or more bins

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use Getopt::Std;
use EMSUtils;
use WMSUtils;
use GeneralUtils;
use EMSReportRequest;
use Midsvcs;
use OracleIF;

use Symbol;
use File::Basename;

getopts("b:d:c:t:g");

die "ERROR: EMS_HOME not set\n" unless $ENV{EMS_HOME};

EMSUtils->loadConfig;

#$ENV{MIDSVCS_HOME} = $EMSCONFIG{MIDSVCS_HOME};
#$ENV{DBPASSWORD_HOME} = $EMSCONFIG{DBPASSWORD_HOME};

die "Bin type needed in -t" unless $opt_t;
die "Bin type should be QA or AH" unless uc($opt_t) eq "QA" || uc($opt_t) eq "AH";

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");

if ($opt_b) {
  my(@configbins) = EMSUtils->getBinNames($dbh, uc($opt_t));

  foreach $x (split /,/, $opt_b) {
    if ($x =~ /^\d+$/) {
      push @bins, $configbins[$x-1];
    } elsif ($x =~ /^(\d+)\-(\d+)$/) {
      my($from) = $1;
      my($to) = $2;
      for ($i=$from; $i<=$to; $i++) {
	push @bins, $configbins[$i-1] if $i>0 && $i<=@configbins;
      }
    } else {
      push @bins, $x;
    }
  }
} else {
  @bins = ("__ALL__");
}

foreach $bin (@bins) {
  @opts = ();
  push @opts, "db=$opt_d" if $opt_d;
  push @opts, "config=$opt_c" if $opt_c;
  push @opts, "action=" . (uc($opt_t) eq "QA" ? "qa_generate" : "ah_generate");
  push @opts, "batch=1";
  push @opts, "bin_name=$bin";

  $cmd = $ENV{EMS_HOME} . "/bin/batchems.pl -c \"" . join(' ', @opts) . "\"";
  if ($opt_g) {
    print STDERR $cmd, "\n";
  } else {
    eval { system $cmd; };
    die $@ if $@;
  }
}
exit 0;
