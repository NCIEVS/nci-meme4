#!@PATH_TO_PERL@

# Script to stamp one or more worklists in batch mode

# suresh@nlm.nih.gov - EMS 3 (1/2006)

# Environment variables
# EMS_HOME (required)
# EMS_CONFIG (optional)

# Command line arguments:
# -d <database> (by default the MID name server is used).
# -c (alternate config file)
# -w (one or more worklist or checklist names comma separated)

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use Getopt::Std;
getopts("d:w:c:");

die "ERROR: EMS_HOME not set\n" unless $ENV{EMS_HOME};
die "ERROR: No worklists specified\n" unless $opt_w;

foreach $worklist (split /,/, $opt_w) {
  @opts = ();
  push @opts, "db=$opt_d" if $opt_d;
  push @opts, "config=$opt_c" if $opt_c;
  push @opts, "action=report";
  push @opts, "list=$worklist";
  push @opts, "doit=1";

  $cmd = $ENV{EMS_HOME} . "/bin/batchwms.pl -c \"" . join(" ", @opts) . "\"";
  system $cmd;
}
exit 0;
