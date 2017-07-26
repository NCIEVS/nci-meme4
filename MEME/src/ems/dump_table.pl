#!@PATH_TO_PERL@

# Dumps a table's contents to stdout, or
# with the -q runs a single select statement, or
# with the -Q runs a file of commands some of which can be drop statements,
#    but the last must be a select, or
# with the -x runs a DDL statement (create, drop, etc)

# suresh@nlm.nih.gov 7/00

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use Getopt::Std;

use OracleIF;
use EMSUtils;
use GeneralUtils;
use ZipUtils;
use Midsvcs;

# Options:
# -t <table name>
# -d <database>
# -q <some select SQL>
# -x <some SQL>
# -Q <File containing select STMT>

getopts("t:d:q:Q:x:");

die "Only -t OR -q should be specified, not both.\n" if $opt_t && ($opt_q || $opt_Q);
die "Need a table name in -t or a select statement in -q/-Q\n" unless $opt_t || $opt_q || $opt_Q || $opt_x;

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{EMS_HOME};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get($opt_s || 'editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Database: $db is unavailable" unless $dbh;

$logdir = $ENV{EMS_HOME} . "/log";

$tmpdir = $logdir;

$table=$opt_t;
$dbh->{dbh}->{LongReadLen}=20000;

if ($opt_Q) {
  open(Q, $opt_Q) || die "Cannot open $opt_Q\n";
  while (<Q>) {
    chomp;
    next if /^\s*$/;
    s/^\s*//;
    s/\s*$//;
    $select .= $_ . " ";
  }
  close(Q);
} elsif ($opt_q) {
  $select = $opt_q;
}

if ($select) {
  $table = "tmp_dumptable_$$";
  @stmt = split /\s*;\s*/, $select;
  for ($i=0; $i<@stmt; $i++) {
    $stmt = $stmt[$i];

    if ($stmt =~ /drop table (.*)$/i) {
      $dbh->dropTable($1);
      next;
    }

    if ($i == $#stmt) {
      $dbh->selectToFile($stmt, \*STDOUT);
    } else {
      $dbh->executeStmt($stmt);
    }
  }
}

$dbh->table2file($table, \*STDOUT) if $opt_t && $dbh->tableExists($table);
$dbh->executeStmt($opt_x) if ($opt_x);
$dbh->disconnect;
exit 0;
