#!@PATH_TO_PERL@
 
# Separated releaseable atoms that are identical per the LVG flow -fguol
# i.e., identical after removing stop words, removing genitives,
# uninversion, removing punctuation and lowercase'ing

# suresh@nlm.nih.gov 5/2001
# suresh@nlm.nih.gov - EMS3 mods

# Command line options:
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
use ZipUtils;
use Midsvcs;
use EMSNames;
use EMSTables;

use File::Basename;
use Getopt::Std;

getopts("d:");

$starttime = time;

die "ERROR: EMS_HOME environment variable not set\n" unless $ENV{'EMS_HOME'};
EMSUtils->loadConfig;

$db = $opt_d || Midsvcs->get('editing-db');
$user = $main::EMSCONFIG{ORACLE_USER};
$password = GeneralUtils->getOraclePassword($user,$db);
$dbh = new OracleIF("db=$db&user=$user&password=$password");
die "Cannot connect to $db" unless $dbh;

$sql = <<"EOD";
SELECT	DISTINCT c1.concept_id, n1.normstr_id, a1.atom_name, c2.concept_id, n2.normstr_id, a2.atom_name
FROM	normstr n1, normstr n2, classes c1, classes c2, atoms a1, atoms a2
WHERE	n1.normstr = n2.normstr
AND	n1.normstr_id<n2.normstr_id
AND	c1.atom_id=n1.normstr_id
AND	c2.atom_id=n2.normstr_id
AND	c1.concept_id<c2.concept_id
AND	c1.atom_id=a1.atom_id
AND	c2.atom_id=a2.atom_id
AND     c1.tobereleased in ('y', 'Y')
AND     c2.tobereleased in ('y', 'Y')
EOD

$tmpfile = EMSUtils->tempFile;
$dbh->selectToFile($sql, $tmpfile);

$lvgopts = "-F2 -fguol";
open(T, $tmpfile) || die "Cannot open $tmpfile\n";
while (<T>) {
  chomp;
  ($c1, $a1, $n1, $c2, $a2, $n2) = split /\|/, $_;

  next if $done{"$c1|$c2"}++;
  $N1 = &transform($n1);
  $N2 = &transform($n2);
  if ($N1 eq $N2) {
# many problems with "other" - best avoided
    next if $N1 eq "other";
    $cluster++;
    print <<"EOD";
$c1|$cluster
$c2|$cluster
EOD
  }
}
close(T);

unlink $tmpfile;
$dbh->disconnect;
exit 0;

# transforms per lvg -fguol just faster :-)
sub transform {
    my($x) = @_;

# genitive
    $x =~ s/\'[sS]\s//g;
    $x =~ s/\'[sS]$//g;
    $x =~ s/[sS]\'\s//g;
    $x =~ s/[sS]\'$//g;

# uninvert
    $x = &uninvert($x);

# substitute space for punctuation
    $x =~ s/\W/ /g;

# lowercase
    $x = lc $x;

# remove leading and trailing spaces
    $x =~ s/^\s+//;
    $x =~ s/\s+$//;
    return $x;
}

sub uninvert {
    my($a) = @_;
    return ($a =~ /(.*),\s+(\w+)$/ ? ($2 . " " . &uninvert($1)) : $a);
}

# do two sets have a non-null intersection?
sub intersect {
    my($s1, $s2) = @_;
    my(%k);

    foreach (@{ $s1 }) { $k{$_}++ };
    foreach (@{ $s2 }) {
	return 1 if $k{$_};
    }
    return 0;
}
