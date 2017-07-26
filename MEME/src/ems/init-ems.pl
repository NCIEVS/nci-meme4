#!@PATH_TO_PERL@

# Initializes the EMS by creating tables, MAXTAB entries, etc.

# Command line arguments:

# -d <database>
# -f (forces the tables to be re-created even if they exist)

unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";

use OracleIF;
use EMSUtils;
use GeneralUtils;
use Midsvcs;
use EMSMaxtab;
use EMSNames;

use Getopt::Std;
getopts("d:f");

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Database: $db is unavailable" unless $dbh;

# create tables as needed
foreach $t (EMSTables->tables) {
  $dbh->dropTable($t) if $opt_f;
  EMSTables->createTable($dbh, $t);
}

# clear maxtab keys

$dbh->disconnect;
exit 0;
