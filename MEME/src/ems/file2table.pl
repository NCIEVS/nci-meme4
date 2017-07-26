#!@PATH_TO_PERL@

# Loads the contents of a file to a table

# Author: suresh@nlm.nih.gov 8/2001
# suresh@nlm 5/2006 (EMS3 mods)

# Command line arguments:
#
# -d <database> default is editing-db
# -t <table name>
# -i (loads the data incrementally with INSERT statements, else uses sqlldr)
# -n <column names> comma separated names - must match the -c option
# -c <field spec>  comma separated field spec, e.g., 'char(80),int,varchar(32)'
# -r create table if necessary
# -x destroy existing table, if any

# Data for the table is read from STDIN

# Example:
# file2table -n 'concept_id,cluster_id' -c 'int,int' -r

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

use File::Basename;

use Getopt::Std;

getopts("d:t:n:c:rix");

die "Need a table name in -t" unless $opt_t;
die "Need field names in -n\n" unless $opt_n;
die "Need field specification in -c\n" unless $opt_c;

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{EMS_HOME};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get($opt_s || 'editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Database: $db is unavailable" unless $dbh;

$tmpdir = "/tmp";
$logdir = $tmpdir;

@names = split /,/, $opt_n;
@cols = split /,/, $opt_c;

die "ERROR: Names and column specs do not match\n" unless @names == @cols;

for ($i=0; $i<@names; $i++) {
  $name = $names[$i];
  $col = $cols[$i];

  push @colspec, {$name => $col};

  $col =~ tr /a-z/A-Z/;
  if ($col =~ /^INT/) {
    push @type, "$name INTEGER";
    push @spec, "$name INTEGER EXTERNAL TERMINATED BY \'|\'";
  } elsif ($col =~ /^CHAR(\(.*\))/ || $col =~ /^VARCHAR(\(.*\))/) {
    push @type, "$name $col";
    push @spec, "$name CHAR" . $1 . " TERMINATED BY \'|\'";
  } elsif ($col eq "DATE") {
    push @type, "$name $col";
    push @spec, "$name DATE" . $1 . " TERMINATED BY \'|\'";
  } else {
    die "Unsupported type: $col\n";
  }
}

$table = $opt_t;
$dbh->dropTable($table) if $opt_x;

$dbh->createTable($table, \@colspec) if (!$dbh->tableExists($table) && $opt_r);

if ($opt_i) {
  $dbh->file2table(\*STDIN, $table, \@colspec);
} else {
  my($tmpfile) = "/tmp/file2table.$$.dat";
  open(T, ">$tmpfile") || die $@;
  while (<>) {
    print T $_;
  }
  $dbh->sqlldr($tmpfile, $table, \@colspec);
}
$dbh->disconnect;
exit 0;
