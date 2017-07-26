#!@PATH_TO_PERL@
#
# Starts and stops the MIDSVCS socket server
# Arguments: start|stop|pid
#
unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

$servername = "midsvcs-socket.pl";
$serverpath = "$ENV{MIDSVCS_HOME}/bin/$servername";
$mode=$ARGV[0];

unless ($mode) {
  print "Usage: $0 {start|stop|pid}\n";
  exit 0;
}

if ($mode =~ /^pid/i) {
  print "$servername processes: ", join(', ', &get_pids), "\n";
} elsif ($mode eq "start") {
  &start;
} elsif ($mode eq "stop") {
  &stop;
} elsif ($mode eq "restart") {
  &restart;
}
exit 0;

sub start {
  print "Starting $servername ...\n";
  if (&get_pids) {
    print "$servername already running\n";
    return;
  }

  unless (-e $serverpath) {
    print "ERROR: $serverpath does not exist.\n";
    exit 0;
  }

  unless (-x $serverpath) {
    print "ERROR: $serverpath is not executable!\n";
    exit 0;
  }

  if ($pid=fork()) {
    return;
  } elsif (defined $pid) {
    close(STDOUT);
    close(STDERR);
    close(STDIN);
    exec $serverpath;
  } else {
    die "Can't fork: $!\n";
  }
  exit 2;
}

sub stop {
  #&root;
  foreach $pid (&get_pids) {
    print STDERR "Killing process: $pid\n";
    $n = kill 9, $pid;
    if ($n != 1) {
      print STDERR "ERROR: failed to kill: $pid\n";
    }
  }
}

sub restart {
  &stop;
  sleep 1;
  &start;
  sleep 1;
}

sub get_pids {
  my(@x);
  open(PS, "/bin/ps -ef |/bin/grep midsvcs_socket |") || die "Cannot start ps\n";
  while (<PS>) {
    chomp;
    @_ = split /\s+/, $_;
    push @x, $_[1] unless /\/bin\/grep midsvcs_socket/;
  }
  close(PS);
  return @x;
}
