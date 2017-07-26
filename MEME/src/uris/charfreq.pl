#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";

#!/site/bin/perl58

# Counts the frequencies of UTF-8 characters in the input

# Options:
# -f <file>

#use lib "/site/umls/uris-2.0/src";

use Getopt::Std;
getopts("f:");

use Encode;
use charnames(":full");
use utf8;
use Symbol;
use Unicode::UCD 'charinfo';
use Data::Dumper;
use UrisUtils;

$file = $opt_f;
if ($opt_f) {
  die "ERROR: File $file does not exist or is not readable\n" unless -r $file;
}

if ($opt_f) {
  $fd = gensym;
  open($fd, $file) || die "Cannot open $file\n";
} else {
  $fd = \*STDIN;
}
binmode($fd, ":utf8");
while (<$fd>) {
  map { $count->{$_}->{count}++ } unpack("U*", $_);
}
close($fd);

$blocks = UrisUtils->parse_nameslist();
foreach $c (keys %{ $count }) {
  $count->{$c}->{charname} = charnames::viacode($c);
  $info = charinfo($c);
  $count->{$c}->{unicode} = $info->{code};
  $x = UrisUtils->char2block($blocks, $c);
  $blockname = ($x ? $x->[0] : "UNKNOWN");
  $features = ($x ? $x->[1] : "[]");
  $count->{$c}->{blockname} = $blockname;
  $count->{$c}->{features} = $features;
}
print Dumper($count);
exit 0;
