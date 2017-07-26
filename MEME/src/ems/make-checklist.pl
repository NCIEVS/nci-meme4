#!@PATH_TO_PERL@

# Command line interface to making a checklist

# Author: suresh@nlm.nih.gov 8/2002
# suresh@nlm.nih.gov 2/2006 (EMS 3)

# Command line arguments

# -d database
# -c checklist name
# -b (bin name for source of concepts)
# -t (table name for source of concepts)
# -f (file name for source of concepts)
# -m (maximum number of clusters)
# -r (randomize the input before making a checklist)
# -i (input contains CUIs)
# -s (silent)

# default source for concepts is "file" from STDIN

unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use lib "$ENV{EMS_HOME}/lib";

use OracleIF;
use EMSUtils;
use GeneralUtils;
use Midsvcs;
use EMSMaxtab;
use EMSNames;

use Getopt::Std;
getopts("d:c:b:t:f:m:ris");

die "ERROR: EMS_HOME environment variable not set" unless $ENV{EMS_HOME};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user);
$dbh = new OracleIF("db=$db&user=$user&password=$password");

# Can user run this script?
$unixuser = GeneralUtils->username;

unless (grep { $_ eq $unixuser } @{ $EMSCONFIG{BATCH_UNIX_USER} }) {
  die "ERROR: user: $unixuser is not currently allowed to run EMS scripts.";
}

$checklist = $opt_c || "chk_" . $unixuser;
die "ERROR: Checklist name must begin with CHK_" unless $checklist =~ /^chk_/i;
die "ERROR: Checklist name must be fewer than 32 chars" unless length($checklist) <= 32;

if ($opt_b) {

  $bininfo = EMSUtils->getBininfo($dbh, $opt_b);
  die "ERROR: Bin: $opt_b not found in database $db" unless $bininfo;
  $param{bin_name} = $opt_b;
  $param{bin_type} = $bininfo->{bin_type};

} elsif ($opt_f) {

  $param{file} = $opt_f;
  if ($opt_i) {
    $param{file} = &mapcuis($opt_f);
  }

} elsif ($opt_t) {

  die "ERROR: table $opt_t does not exist in the DB: $db.\n" unless $dbh->tableExists($opt_t);
  die "ERROR: table $opt_t does not have a CONCEPT_ID column.\n" unless $dbh->tableHasColumn($opt_t, 'CONCEPT_ID');
  $param{table_name} = $opt_t;

} else {

  $tmpfile = EMSUtils->tempFile($EMSNames::TMPFILEPREFIX);
  open(T, ">$tmpfile") || die "ERROR: Cannot write to $tmpfile";
  while (<>) {
    print T $_;
  }
  close(T);

  push @filestodelete, $tmpfile;

  if ($opt_i) {
    $param{file} = &mapcuis($tmpfile);
    push @filestodelete, $param{file};
  } else {
    $param{file} = $tmpfile;
  }
}
$param{limit} = $opt_m if $opt_m;
$param{randomize} = 1 if $opt_r;
$param{owner} = $unixuser;

eval {
  EMSUtils->makeChecklist($dbh, $checklist, \%param);
};

if ($@) {
  $errormsg = $@;
}
foreach $file (@filestodelete) {
  unlink $file;
}
die $errormsg if $errormsg;
die "ERROR: checklist: $checklist was not made.\n" unless $dbh->tableExists($checklist);

unless ($opt_s) {
  $cc = $dbh->selectFirstAsScalar("select count(distinct orig_concept_id) from $checklist");
  $cl = $dbh->selectFirstAsScalar("select count(distinct cluster_id) from $checklist");
  print "Checklist: $checklist successfully made in DB: $db with $cc concepts and $cl clusters.\n";
}

$dbh->disconnect;
exit 0;

# returns a file of concept_ids given of of CUIs
sub mapcuis {
  my($f) = @_;
  my($tmpfile) = EMSUtils->tempFile($EMSNames::TMPFILEPREFIX);

  open(F, $f) || die "ERROR: Cannot open $f";
  open(T, ">$tmpfile") || die "ERROR: Cannot open $tmpfile";
  while (<F>) {
    chomp;
    ($cui, $cluster_id) = split /\|/, $_, 2;
    foreach $concept_id (MIDUtils->cui2concept_id($dbh, $cui)) {
      print T join("|", $concept_id, $cluster_id), "\n";
    }
  }
  close(T);
  close(F);
  return $tmpfile;
}
