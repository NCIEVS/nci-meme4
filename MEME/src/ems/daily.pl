#!@PATH_TO_PERL@

# Wrapper script for daily tasks
# suresh@nlm.nih.gov 3/2006
unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";
die "ERROR: Need $EMS_HOME to be set" unless $ENV{EMS_HOME};

foreach $c ("daily-snapshot.pl", "daily-action-counts.pl", "mail-daily-report.pl") {
  $cmd = $ENV{EMS_HOME} . "/bin/$c";
  eval { system $cmd; };
  die "ERROR: $@" if $@;
}
exit 0;
