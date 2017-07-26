#!@PATH_TO_PERL@

# Output the frequency of each byte

# Command line options:
# -8 (only bytes with high bit set)
# -7 (only bytes with high bit 0)
# -r (reverse sort by frequency)

use Getopt::Std;
getopts("87r");

while (<>) {
  @bytes = unpack("C*", $_);
  foreach $b (@bytes) {
    if ($opt_8) {
      next unless ($b>>7) > 0;
    } elsif ($opt_7) {
      next if ($b>>7) > 0;
    }
    $freq{$b}++;
  }
}

if ($opt_r) {
  foreach $k (sort {$freq{$b} <=> $freq{$a} } keys %freq) {
    printf("%x|%d|%o|%s|%c|%d\n", $k, $k, $k, unpack("B*", pack("C", $k)), (($k>>7) > 0 ? "" : $k), $freq{$k});
  }
} else {
  foreach $k (sort { $a <=> $b } keys %freq) {
    $c = "";
    if (($k >> 7) == 0) {
      $c = chr($k);
      $c = sprintf("^%c", ord('A')+$k) if ($k < 26);
      $c = "SPACE" if ($k == ord(" "));
      $c = "BELL" if ($k == 0x7);
      $c = "\\t" if ($k == ord("\t"));
      $c = "\\n" if ($k == ord("\n"));
      $c = "\\r" if ($k == ord("\r"));
    }
    printf("%x|%d|%o|%s|%s|%d\n", $k, $k, $k, unpack("B*", pack("C", $k)), (($k>>7) > 0 ? "" : $c), $freq{$k});
  }
}
exit 0;
