#!@PATH_TO_PERL@

# MID naming service

# Examples of supported services:
# editing-db
# editing-jdbc
# production-db
# testsrc-db
# testsw-db
# testmisc-db
# mrd-db
# databases

# See also inetd.conf and /etc/services
# suresh@nlm.nih.gov 9/98
#
# Changes:
# 12/22/2005 BAC (1-719SM): use open ":utf8"  added
#

use Socket;
use Getopt::Std;

getopts("s:h:");

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";

$midsvcsfile = "$ENV{MIDSVCS_HOME}/etc/mid-services-data";

$service = $opt_s;
$service = "???" if !$service || $opt_h;

if (-e $midsvcsfile) {
  open(P, $midsvcsfile) || die "Cannot open $midsvcsfile\n";
} else {

  $proto = getprotobyname("tcp");
  socket(P, PF_INET, SOCK_STREAM, $proto);
  $sin = sockaddr_in($ENV{"MIDSVCS_PORT"}, inet_aton($ENV{"MIDSVCS_HOST"}));
  connect(P, $sin);
  binmode(P,":utf8");
}

while (<P>) {
    chomp;
    next if /^\#/ || /^\s*$/;
    @_ = split /\|/, $_;
    if ($service eq "???") {
	push @services, $_;
    } elsif ($_[0] eq $service) {
	print $_[1], "\n";
	last;
    }
}
close(P);
print join("\n", @services), "\n" if $service eq "???";
exit 0;
