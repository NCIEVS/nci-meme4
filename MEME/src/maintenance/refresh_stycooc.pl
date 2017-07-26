#!@PATH_TO_PERL@

# referesh STY co-occurrence information in EMS3_STYCOOC table
# 2007.07

BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use EMSUtils;
use OracleIF;
use Midsvcs;
use GeneralUtils;

use Getopt::Std;
getopts("d:");

EMSUtils->loadConfig;

chomp($currentyear = `/bin/date "+%Y"`);
$logfile = join("/", $ENV{EMS_LOG_DIR}, "log",
                join(".", "stycooc", $currentyear, "log"));
unless (-e $logfile) {
  system "/bin/touch $logfile";
  chmod(0775, $logfile) || die $@;
}

@degrees = (2,3,4,5);
$STYCOOCTABLE = $EMSNames::STYCOOCTABLE;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
eval { $dbh = new OracleIF("db=$db&user=$user&password=$password"); };

print "$db is unavailable: $user/$password \n" if ($@ || !$dbh); 
&log_and_die("Database: $db is unavailable") unless $dbh;


$startdate = GeneralUtils->date;

&log("\n" . "-" x 20 . $startdate . "-" x 20 . "\n");
&log("EMS_HOME: " . $ENV{EMS_HOME});
&log("Database: " . $dbh->getDB());

$styattributes = $dbh->tempTable($EMSNames::TMPTABLEPREFIX . "_sty");

$sql = "delete $STYCOOCTABLE";
$dbh->executeStmt($sql);

my($starttime);
my($currentdateq);

foreach $degree (@degrees) {
  $starttime = time;
  $currentdateq = $dbh->quote($dbh->currentDate());
  $dbh->dropTable($styattributes);
  $sql = "create table $styattributes as select concept_id, attribute_value from attributes where attribute_name ='SEMANTIC_TYPE'";
  $dbh->executeStmt($sql);
  $dbh->createIndex($styattributes, "concept_id", "x1_" . $styattributes);
  $dbh->createIndex($styattributes, "attribute_value", "x2_" . $styattributes);

  $sql = &make_sql($degree, $styattributes);

  my($t) = 0;
  my($n);
  foreach $ref ($dbh->selectAllAsRef($sql)) {
    $t = time - $starttime unless $t;
    chomp;
    @x = ();
    for ($i=0; $i<$degree; $i++) {
      push @x, $ref->[$i];
    }
    $stys = join("|", @x);
    $stys = $dbh->quote($stys);
    $frequency = $ref->[$degree];
    $sql = <<"EOD";
insert into $STYCOOCTABLE (degree, stys, frequency, generation_date, generation_time)
values ($degree, $stys, $frequency, to_date($currentdateq), $t)
EOD
    $dbh->executeStmt($sql);
    $n++;
  }
  &log("$n records for stycooc data generated for degree=$degree in $t seconds.");
}

$dbh->dropTable($styattributes);
$dbh->disconnect;
exit 0;

#===========================
sub make_sql {
  my($n, $attributes) = @_;
  my($i);
  my($sql);
  my(@x);

  $attributes = "attributes" unless $attributes;

  for ($i=0; $i<$n; $i++) {
    $t = chr(97+$i);
    $s = chr(96+$i);

    push @{ $x[0] }, $t . ".attribute_value";
    push @{ $x[1] }, "$attributes $t";
#    push @{ $x[2] }, $t . ".attribute_name ='SEMANTIC_TYPE'";
    push @{ $x[3] }, $t . ".concept_id=" . $s . ".concept_id" if $i>0;
    push @{ $x[4] }, $s . ".attribute_value<" . $t . ".attribute_value" if $i>0;

  }

  $hint = "/*+ full(a) full(b) use_hash(a,b) */";
  $hint = "";
  $sql =
    "select $hint " . join(', ', @{ $x[0] }) . ", count(distinct a.concept_id) from " . join(', ', @{ $x[1]
}) .
    "\n\twhere " . join("\n\tand ", @{ $x[3]}) .
#    "\n\tand " . join("\n\tand ", @{ $x[2] }) .
    "\n\tand " . join("\n\tand ", @{ $x[4] }) .
    "\ngroup by " . join(', ', @{ $x[0] }) . "\n";
  return $sql;
}

sub log {
  my($msg) = @_;
  open(L, ">>$logfile") || return;
  print L $msg, "\n";
  close(L);
}

sub log_and_die {
  my($msg) = @_;
  &log($msg);
  die $msg;
}
