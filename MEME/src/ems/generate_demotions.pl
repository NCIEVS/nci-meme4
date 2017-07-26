#!@PATH_TO_PERL@

# Generates the contents of the demotions bin.  Produces a list of clustered concepts (concept_id, cluster_id)
# suresh@nlm.nih.gov - 7/98
# suresh@nlm.nih.gov - 2/2000 - ported to Oracle
# suresh@nlm.nih.gov - 3/2005 - EMS 3 framework

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";
push @INC, "$ENV{EMS_HOME}/bin";
use OracleIF;
use GeneralUtils;

use File::Path;
use File::Basename;

use Getopt::Std;

# Options
# -d <database>
# -u <Oracle user>

getopts("d:u:");

$dbh = new OracleIF({ db=>$opt_d, user=>$opt_u, password=>GeneralUtils->getOraclePassword($opt_u) });
die "Oracle not available for $opt_d: $DBI::errstr\n" if (!defined($dbh) || $DBI::errstr);

$sql = <<"EOD";
select concept_id_1, concept_id_2 from relationships where  status='D'
union
select concept_id_2, concept_id_1 from relationships where  status='D'
EOD

@refs = $dbh->selectAllAsRef($sql);
$clusterid = 0;

foreach $r (sort { $a->[0] <=> $b->[0] } @refs) {
  $c1 = $r->[0];
  $c2 = $r->[1];
  next unless $c1<$c2;
  if ($c1 != $prev) {
    foreach $c (sort { $a <=> $b } keys %cluster) {
      print join('|', $c, $clusterid), "\n";
    }
    $clusterid++;
    %cluster = ();
  }
  $cluster{$c1}++;
  $cluster{$c2}++;
  $prev = $c1;
}
$dbh->disconnect;
exit 0;
