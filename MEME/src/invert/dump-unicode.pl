#!@PATH_TO_PERL@

# Script to create some random text in UTF-8 or UTF-16 given a
# range of Unicode code points

# Command line options:
# -r (U+xxxx - U+yyyy) e.g., -r U+0900-U+097F
# -c (or a single unicode character)
# -n (print names for this range)
# -o {8|16} - output format desired (UTF-8 or UCS-2)
# -C (number of characters per line, default is 10)
# -l (# lines of text needed - default is 10)
# -u (a sequence of UTF-8 chars) - will dump the UCD-2 char
# -f Data read from STDIN should contain Unicode chars specified as xxxx
#    The script will dump these out as UTF-8 in the same
#    order on each line.

# Some predefined ranges
# -h hindi

use Getopt::Std;
getopts("r:no:c:C:l:hu:f");

use Encode;
use charnames(":full");

$opt_o = 8 unless $opt_o;
if ($opt_o == 8) {
  $outputencoding = "utf8";
} elsif ($opt_o == 16) {
  $outputencoding = "encoding(ucs2)";
} else {
  die "Unrecognized output encoding (use 8 or 16)";
}

if ($opt_f) {
  my($x, $i);
  binmode(STDOUT, ":" . $outputencoding);
  while (<>) {
    chomp;
    $i=0;
    while ($x = substr($_, $i, 4)) {
      $x =~ tr/a-z/A-Z/;
      die "ERROR: Bad hex data (offset=$i): $x\n" unless $x =~ /[0-9A-F]{4}/;
      print chr(hex $x);
      $i += 4;
    }
    print "\n";
  }
  exit 0;
}

if ($opt_c) {
  $opt_r = $opt_c . "-" . $opt_c;
}

if ($opt_h) {
  $opt_r = "U+0900-U+097F";
}
die "Need a range in -r or a character in -c" unless $opt_r;

($b,$e) = split /-/, $opt_r;
die "Specify range as U+xxxx-U+yyyy" unless $b && $e;

$b =~ s/^[uU]\+//;
$b =~ tr/A-Z/a-z/;
die "Need a 4 digit hex number for start of range, e.g., 00F3" unless $b =~ /^[0-9a-f]*$/;
$b = hex $b;

$e =~ s/^[uU]\+//;
$e =~ tr/A-Z/a-z/;
die "Need a 4 digit hex number for end of range, e.g., 00F3" unless $e =~ /^[0-9a-f]*$/;
$e = hex $e;

die "The range has to be increasing" if $e<$b;

$charsPerLine = $opt_C || 10;
$lines = $opt_l || 10;

foreach ($b..$e) {
  push @c, $_;
}

if ($opt_n) {
  foreach $c (@c) {
    printf("%04X|%s\n", $c, charnames::viacode($c));
  }
  exit 0;
}

$n=0;
foreach (@c) {
  $n++;
  push @data, chr($_);
  if ($n == $charsPerLine) {
    push @data, "\n";
    $n=0;
  }
}

binmode(STDOUT, ":" . $outputencoding);
print join("", @data), "\n";
exit 0;
