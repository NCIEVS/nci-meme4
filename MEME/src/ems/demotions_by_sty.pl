#!@PATH_TO_PERL@

# Generates clusters of demotions by STY
# Needs the SRSTY table to be present in the database

# Use for creating checklists

# suresh@nlm.nih.gov 11/2003
# suresh@nlm.nih.gov 5/2005 - modified for EMS3

# Options:
# -d database
# -c (counts by STY only)
# -n (order by reverse frequency in the counts)
# -s (list of STYs - if concept has ANY of these STYs the cluster is output)

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";

use OracleIF;
use EMSUtils;
use GeneralUtils;
use Midsvcs;
use EMSNames;
use EMSTables;

use File::Basename;
use File::Path;
use Getopt::Std;

getopts("d:s:cn");

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get($opt_s || 'editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");

$logdir = $ENV{EMS_HOME} . "/log";

if ($opt_s) {
  foreach (split /\s*,\s*/, $opt_s) {
    if (m/^\d+$/) {
      $tnum = "T" . sprintf("%.3d", $_);
    } elsif (m/^[tT]\d{3}$/) {
      tr/a-z/A-Z/;
      $tnum = $_;
    } else {
      push @stytomatch, $_;
    }

    if ($tnum) {
      $sql = "select STY from SRSTY where UI=" . $dbh->quote($tnum);
      $sty = $dbh->selectFirstAsScalar($sql);
      if ($sty) {
	push @stytomatch, $sty;
      } else {
	die "ERROR: Cannot find STY for UI=$tnum\n";
      }
      $tnum = "";
    }
  }
}

$demotionstable = $dbh->tempTable($EMSNames::TMPTABLEPREFIX . "_DEMBYSTY1");
$demotionsindex = $dbh->tempTable($EMSNames::TMPTABLEPREFIX . "_x_DEMBYSTY1");
$stytable = $dbh->tempTable($EMSNames::TMPTABLEPREFIX . "_DEMBYSTY2");
$dbh->dropTables([$demotionstable, $stytable]);

EMSUtils->clusterizeDemotions($dbh, $demotionstable);
$dbh->createIndex($demotionstable, 'concept_id', $demotionsindex);

$sql = <<"EOD";
create table $stytable as
select /*+ INDEX(concept_id) */ cluster_id, attribute_value as sty from attributes a, $demotionstable b
where  a.concept_id=b.concept_id
and    a.attribute_name || '' ='SEMANTIC_TYPE'
EOD
$dbh->executeStmt($sql);

$orderby = "order by " . ($opt_n ? "count(*) desc" : "sty");
if ($opt_c) {
  $sql = <<"EOD";
select a.sty, b.UI, count(*) from $stytable a, SRSTY b
where a.sty=b.sty
group by a.sty, b.UI $orderby
EOD
} else {
  my($qsty) = join(",", map { $dbh->quote($_) } @stytomatch);
  $sql = <<"EOD";
select concept_id, cluster_id from $demotionstable
where  cluster_id in (select cluster_id from $stytable where sty in ($qsty))
order  by cluster_id
EOD
}
$dbh->selectToFile($sql, \*STDOUT);
$dbh->dropTables([$demotionstable, $stytable]);
$dbh->disconnect;
exit 0;
