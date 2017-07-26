#!@PATH_TO_PERL@
 
# All pairs of concepts with identical, releaseable norm strings (LUIs) in them
# but without a relationship connecting them.  The -s option specifies sources
# restrictions for the atoms involved.  If the -b option is specified, both
# atoms must be from the source(s), else just one.

# wth@nlm.nih.gov and
# suresh@nlm.nih.gov 5/6/98; to Oracle 8/00
# suresh@nlm.nih.gov 5/2005 EMS-3 mods

# Options:
# -d database
# -s sources
# -b

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
use ZipUtils;
use Midsvcs;
use EMSNames;
use EMSTables;

use File::Basename;
use Getopt::Std;

getopts("d:s:b");

$starttime = time;

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Cannot connect to $db" unless $dbh;

$needsrel = $dbh->tempTable($EMSNames::TMPTABLEPREFIX . "_needrl");
$needsrel2 = $dbh->tempTable($EMSNames::TMPTABLEPREFIX . "_needrl2");
$dbh->dropTables([$needsrel, $needsrel2]);

if ($opt_s) {
  @sources = MIDUtils->makeVersionedSAB($dbh, [ split /\,/, $opt_s ]);
  $sources = join(', ', map { $dbh->quote($_) } @sources);
  my($s) = (@sources == 1 ? "=$sources" : "in ($sources)");
  unless ($opt_b) {
    $sourcesql = "and (a.source $s or b.source $s)";
  } else {
    $sourcesql = "and (a.source $s and b.source $s)";
  }
}

$sql = <<"EOD";
create table $needsrel AS
select a.concept_id as concept_id_1, b.concept_id as concept_id_2, a.atom_id as a1, b.atom_id as a2 from classes a, classes b
where  a.lui = b.lui
and    a.concept_id < b.concept_id
and    a.tobereleased IN ('y', 'Y')
and    b.tobereleased IN ('y', 'Y')
and a.language='ENG' and b.language='ENG'
$sourcesql
EOD
$dbh->executeStmt($sql);

# remove cases where the atoms norm to null
$sql = <<"EOD";
delete from $needsrel where a1 in (select normstr_id from normstr where normstr is null)
EOD
$dbh->executeStmt($sql);

$sql = <<"EOD";
delete from $needsrel where a2 in (select normstr_id from normstr where normstr is null)
EOD
$dbh->executeStmt($sql);

$sql = "create table $needsrel2 as select distinct concept_id_1, concept_id_2 from $needsrel";
$dbh->executeStmt($sql);

# remove pairs that have rels
$sql = <<"EOD";
delete from $needsrel2 where (concept_id_1, concept_id_2) in
(
  select concept_id_1, concept_id_2 from relationships where  tobereleased in ('y', 'Y')
  union
  select concept_id_2, concept_id_1 from relationships where  tobereleased in ('y', 'Y')
)
EOD
$dbh->executeStmt($sql);

$tmpfile = "/tmp/tmpneedsrel.$$";
$dbh->selectToFile("select * from $needsrel2", $tmpfile);
$dbh->dropTables([$needsrel, $needsrel2]);
$dbh->disconnect;

open(T, "$tmpfile") || die "Cannot open $tmpfile\n";
while (<T>) {
  chomp;
  ($c1, $c2) = split /\|/, $_;

  next if $done{"$c1|$c2"}++;
    $cluster++;
    print <<"EOD";
$c1|$cluster
$c2|$cluster
EOD
}
close(T);
unlink $tmpfile;
exit 0
