#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";

#!/site/bin/perl5

# Checks the sort order and uniqueness of an MR file
# suresh@nlm.nih.gov
# URIS 2.0 - 9/2003

# Options:
# -f <file>
# -T <tmpdir>
# -l <LANG value>

#use lib "/site/umls/lib/perl";

use Getopt::Std;
use File::Basename;
use GeneralUtils;

getopts("f:T:l:");

#$ENV{'LANG'} = $opt_l || "en_US.UTF-8";

$path = $opt_f;
$file = basename($path);

die "ERROR: Need a Metathesaurus file in the -f option\n" unless $path;
die "ERROR: File $path does not exist or is not readable\n" unless -r $path;

if (-z $path) {
  print STDERR "ERROR: $file is empty\n";
  exit 0;
} else {
  print "OK: $file was non-empty\n";
}

$tmpdir = $opt_T || $ENV{'TMPDIR'} || "/tmp";
$tmpfile = join('/', $tmpdir, "tmp_$$");
unlink $tmpfile;

$cmd = "/share_nfs/usr/bin/sort -T $tmpdir -c $path " . ($file =~ /^MRRANK/ ? "-r " : "") . "1>$tmpfile 2>&1";
$sortstatus = system $cmd;
$sortstatus = 2 if $sortstatus > 0;

unless ($sortstatus) {
  chomp($x = `/share_nfs/usr/bin/uniq -d $path|/bin/head -1`);
  $uniqstatus = ($x ? 1 : 0);
}
if ($sortstatus) {
  print STDERR "ERROR: $file was not sorted\n";
  print STDERR GeneralUtils->file2str($tmpfile) unless -z $tmpfile;
  $error++;
} else {
  print "OK: $file sort order was correct\n";
  if ($uniqstatus) {
    print STDERR "ERROR: $file was not unique\n";
    $error++;
  } else {
    print "OK: $file rows were unique\n";
  }
}
print "\n" unless $error;
unlink $tmpfile;
exit 0;
