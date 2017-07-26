#!@PATH_TO_PERL@

# Simple client to LUI normalize input
# suresh@nlm.nih.gov 10/2002
#
# Changes
# 12/22/2005 BAC (1-719SM): use ":utf8" binmode for socket connections

# Command line options are:
# -h host (default is lvg-server-host)
# -p <port num> (default is lvg-server-port)
# -l (run locally)

use Getopt::Std;
use utf8;
use Encode;
use Socket;
use charnames(":full");

getopts("h:p:l");

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

binmode(STDIN, ":utf8");
binmode(STDOUT, ":utf8");

if ($opt_l) {
  my($cmd) = "$ENV{LVGIF}/bin/LVGServer.pl";
  die "ERROR: Program $cmd does not exist\n" unless ((-e $cmd) && (-x $cmd));
  system "$cmd -f luinorm";
  exit 0;
}

$server = $opt_h || &midsvc("lvg-server-host");
$port = $opt_p || &midsvc("lvg-server-port");

$proto = getprotobyname("tcp");
socket(SOCK, PF_INET, SOCK_STREAM, $proto) || die "ERROR: Cannot create socket: $!";;
$sin = sockaddr_in($port, inet_aton($server));
connect(SOCK, $sin) || die "ERROR: Cannot connect to $server: $!";
binmode(SOCK, ":utf8");

select(SOCK);
$| = 1;
select(STDOUT);

if (@ARGV) {
    print SOCK "L|" . join(' ', @ARGV), "\n";
    $_ = <SOCK>;
    chomp;
    s/\r//;
    s/^[^\|]*\|//;
    print $_, "\n";
} else {
    while (<>) {
	chomp;
	next if /^\s*$/;
#&utf8_dump($_);
	print SOCK "L|$_", "\n";
	$_ = <SOCK>;
	chomp;
	s/\r//;
	s/^[^\|]*\|//;
	print $_, "\n";
    }
}
close(SOCK);
exit 0;

sub midsvc {
  my($s) = @_;
  my($midsvcs) = "$ENV{MIDSVCS_HOME}/bin/midsvcs.pl";
  $midsvcs = "midsvcs.pl" unless -x $midsvcs;

  $_ = `$midsvcs -s $s`;
  chomp;
  return $_;
}

sub utf8_dump {
  my($s) = @_;
  print join(" + ", map { $_ >= 128 ? charnames::viacode($_) : chr } unpack("U*", $s)), "\n";
}
