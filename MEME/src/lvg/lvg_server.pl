#!@PATH_TO_PERL@
#
# Starts and stops the LVG server
# Arguments: start|stop|pid|restart
#
# Changes: 20051212 BAC: no longer needs to run as root
# 01/12/2006 BAC (1-D8MTJ): change in ps command to kill server.
#
unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

$servername = "LVGServer";
$serverpath = "$ENV{LVGIF_HOME}/bin/$servername" . ".pl";
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

# Running as root?
sub root {
  die "ERROR: Need to run this as root.\n" unless (($< == 0) || ($> == 0));
}

sub start {
  #&root;
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
  open(PS, "/bin/ps -ef |/bin/grep java | grep $ENV{LVG_HOME} | grep -v grep |") || die "Cannot start ps\n";
  while (<PS>) {
    chomp;
    @_ = split /\s+/, $_;
    push @x, $_[1];
  }
  close(PS);
  return @x;
}
