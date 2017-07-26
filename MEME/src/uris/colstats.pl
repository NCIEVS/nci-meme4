#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#!/site/bin/perl58

# Collect column statistics
# Output is a dump of an hash with keys:
# rows, bytes, maxlength, minlength, maxrow, minrow, avglength, uniquecount
# suresh@nlm.nih.gov
# URIS-2.0 9/2003

# Options:
# -f <file>
# -d <Meta dir>

use File::Basename;
use Getopt::Std;
use Data::Dumper;
getopts("f:d:");

$file = $opt_f;
$base = basename($file);
$dir = $opt_d;
die "ERROR: Need a Metathesaurus file in the -f option\n" unless $file;
die "ERROR: File $file does not exist or is not readable\n" unless -r $file;
die "ERROR: Need a Metathesaurus directory in the -d option\n" unless $dir;

$mrfiles = "$dir/MRFILES";
$mrfiles = "$dir/MRFILES.RRF" unless -e $mrfiles;
die "ERROR: $mrfiles does not exist or is not readable\n" unless -r $mrfiles;

open(MRFILES, $mrfiles) || die "Cannot open $mrfiles\n";
while (<MRFILES>) {
  chomp;
  @x = split /\|/, $_;
  next unless basename($file) eq $x[0];
  @colnames = split /,/, $x[2];
  foreach (@colnames) {
    $colstats->{$_}->{maxlength} = -1;
    $colstats->{$_}->{minlength} = 999999999;
  }
}
close(MRFILES);

open(F, $file) || die "Cannot read $file";
while (<F>) {
  $rownum++;
  chomp;
  @x = split /\|/, $_;
  for ($i=0; $i<@x; $i++) {
    $col = $colnames[$i] || "???";

    $colstats->{$col}->{rows} = $rownum;
    $colstats->{$col}->{bytes} += length($x[$i]);

    if (length($x[$i]) > $colstats->{$col}->{maxlength}) {
      $colstats->{$col}->{maxlength} = length($x[$i]);
      $colstats->{$col}->{maxrow} = $rownum;
    }

    if (length($x[$i]) < $colstats->{$col}->{minlength}) {
      $colstats->{$col}->{minlength} = length($x[$i]);
      $colstats->{$col}->{minrow} = $rownum;
    }
    $unique[$i]->{$x[$i]}++;
  }
}
close(F);

for ($i=0; $i<@x; $i++) {
  $col = $colnames[$i] || "???";

  if ($colstats->{$col}->{rows} > 0) {
    $colstats->{$col}->{avglength} = $colstats->{$col}->{bytes}/$colstats->{$col}->{rows};
  } else {
    $colstats->{$col}->{avglength} = -1;
  }

  $colstats->{$col}->{uniquecount} = scalar(keys %{ $unique[$i] });
}

print Dumper($colstats);
exit 0;
