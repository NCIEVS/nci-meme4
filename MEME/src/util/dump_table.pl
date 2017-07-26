#!@PATH_TO_PERL@
# Dumps a table's contents to stdout
# suresh@nlm.nih.gov 7/00
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
# 12/22/2005 BAC (1-719SM): use open ":utf8" added

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";

use Getopt::Std;

# Options:
# -t <table name>
# -d <database>
# -u <user/schema>
# -q <some select SQL>
# -x <some SQL>
# -Q <File containing select STMT>

getopts("t:d:u:q:Q:x:");

die "Only -t OR -q should be specified, not both.\n" if $opt_t && ($opt_q || $opt_Q);
die "Need a table name in -t or a select statement in -q/-Q\n" unless $opt_t || $opt_q || $opt_Q || $opt_x;

$statedir="/tmp";
$tmpdir = (-e $statedir ? $statedir : "/tmp");

#
# open connection
#
($oracleUSER,$oraclePWD) = split /\//,$opt_u;
unless ($oraclePWD) {
  $oraclePWD = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl $oracleUSER | sed 's/.*\\///'`;
  chop($oraclePWD);
}
$oracleTNS = $opt_d || `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s editing-db`;
chop($oracleTNS) unless $opt_d;
$oracleDBH = undef; # DBD handle
$defaultTABLESPACE = "MTH";

use DBI;
use DBD::Oracle;
$oracleDBH = DBI->connect("dbi:Oracle:$oracleTNS",$oracleUSER,$oraclePWD) ||
  die "Could not connect to $oracleTNS\n";
$oracleDBH->{LongReadLen}=20000;

$table=$opt_t;

if ($opt_Q) {
    open(Q, $opt_Q) || die "Cannot open $opt_Q\n";
    while (<Q>) {
	chomp;
	next if /^\s*$/;
	s/^\s*//;
	s/\s*$//;
	$opt_q .= $_ . " ";
    }
    close(Q);
}

if ($opt_q) {
    $table = "tmp_dumptable_$$";
    @stmt = split /\s*;\s*/, $opt_q;
    for ($i=0; $i<@stmt; $i++) {
	$stmt = $stmt[$i];

	if ($stmt =~ /drop table (.*)$/i) {
	  $oracleDBH->do(qq{BEGIN MEME_UTILITY.drop_it('table',?); END}, undef, $1) ||
	    die "Error dropping $1\n";
	  next;
	}

	if ($i == $#stmt) {
	  $oracleDBH->do("CREATE TABLE $table AS $stmt") ||
	    die "Error creating $table from '$stmt'\n";
	} else {
	  $oracleDBH->do($stmt) ||
	    die "Error running '$stmt'\n";
	}
    }
}

if ($opt_t || $opt_q) {
  $oracleSH = $oracleDBH->prepare("SELECT * FROM $table") ||
    die "Could not prepare select statement\n";
  $oracleSH->execute() || die "Error executing select statement\n";
  while (@f = $oracleSH->fetchrow_array) {
    print join "|", @f;
    print "\n";
  }
  if ($opt_q) {
    $oracleDBH->do("DROP TABLE $table") ||
      die "Error dropping $table\n";
  }
}

if ($opt_x) {
  die "$@" if (&oracleDBH->do($opt_x, {'RaiseError'=>1,'PrintError'=>1}));
}

$oracleDBH->disconnect;
exit 0;
