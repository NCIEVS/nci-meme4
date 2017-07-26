#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";

#!/share_nfs/perl/5.8.6/bin/perl

# Column syntax by regexp
# suresh@nlm.nih.gov
# URIS-2.0 9/2003

# Options:
# -f <file>
# -d <Meta dir>
# -s <syntax file>

#use lib "/umls/lib/perl";

use File::Basename;
use Getopt::Std;
use Data::Dumper;
getopts("f:d:s:");

$file = $opt_f;
$dir = $opt_d;
$syntaxfile = $opt_s;
die "ERROR: Need a Metathesaurus file in the -f option\n" unless $file;
die "ERROR: File $file does not exist or is not readable\n" unless -r $file;
die "ERROR: Need a Metathesaurus directory in the -d option\n" unless $dir;
die "ERROR: Syntax file $syntaxfile does not exist or is not readable\n" unless -r $syntaxfile;

open(R, $syntaxfile) || die "Cannot open $syntaxfile\n";
while (<R>) {
  chomp;
  next if /^\#/ || /^\s*$/;
  ($f, $col, $exp) = split /\|/, $_, 3;
  next unless ((split /\./, $f)[0]) eq ((split /\./, basename($file))[0]);
  $syntax{$col} = $exp;
#  $var{$col} = $var;
}
close(R);

$mrfiles = "$dir/MRFILES";
$mrfiles = "$dir/MRFILES.RRF" unless -e $mrfiles;
die "ERROR: $mrfiles does not exist or is not readable\n" unless -r $mrfiles;

open(MRFILES, $mrfiles) || die "Cannot open $mrfiles\n";
while (<MRFILES>) {
  chomp;
  @x = split /\|/, $_;
  next unless basename($file) eq $x[0];
  @cols = split /,/, $x[2];
  last;
}
close(MRFILES);

unless (grep { $syntax{$_} } @cols) {
  $b = basename($file);
  print "$b: no columns in this file need to be checked\n";
  print "<HR>\n";
  exit 0;
}

$b = basename($file);

open(F, $file) || die "Cannot open $file\n";
$linenum=0;

for ($n=0; $n<@cols; $n++) {
  next unless ($syntax{$cols[$n]});
#  $var[$n] =$var{$cols[$n]};
  study $syntax{$cols[$n]};
}

while (<F>) {
  $linenum++;
  $line = $_;
  chomp;

  @fields = split /\|/, $_;
  $n=0;
  foreach (@fields) {
    if ($syntax{$cols[$n]}) {
      $_ = $fields[$n];
      $COLVAL = $_ if ($syntax{$cols[$n]} =~ /\$COLVAL/);

      $status = eval $syntax{$cols[$n]};
      unless ($status) {
	$col = $cols[$n];
	$n1 = $n+1;
	print STDERR "ERROR: File: $b, column: $col (#$n1), line: $linenum fails syntax check: ", $syntax{$col}, "\n";
	print STDERR $line;
	print STDERR "\n";
	die "\nToo many errors, quitting...\n" if ($error{$col}++ > 50);
      }
    }
    $n++;
  }
}
close(F);

foreach $col (@cols) {
  $b = basename($file);
  if ($syntax{$col}) {
    print "OK: File $b: checks OK for column: $col", "(", $syntax{$col}, ")", "\n" unless $error{$col};
  } else {
    print "File $b: column: $col has no syntax specification", "\n";
  }
}
print "<HR>\n";
exit 0;
