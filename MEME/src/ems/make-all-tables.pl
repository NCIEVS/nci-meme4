#!@PATH_TO_PERL@

# Creates EMS tables if needed
# Resets the Maxtab entries
# Clears locks

# Command line arguments:

# -d database

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use OracleIF;
use EMSUtils;
use GeneralUtils;
use Midsvcs;
use EMSMaxtab;
use EMSNames;

use Getopt::Std;
getopts("d:");

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Database: $db is unavailable" unless $dbh;

foreach $t (EMSTables->tables) {
  EMSTables->createTable($dbh, $t);
}

$dbh->disconnect;
exit 0;
