#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
use open ":utf8";

use IO::Socket;
$PORT = 5125;# pick something not in use
$server = IO::Socket::INET->new( Proto     => 'tcp',
                                 LocalPort => $PORT,
                                 Listen    => SOMAXCONN,
                                 Reuse     => 1);

die "can't setup server" unless $server;

# possible interrupt signals
use sigtrap 'handler' => \&interrupt, 'HUP', 'INT', 'ABRT', 'QUIT', 'TERM';

# set up logging
($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$mon++; $year += 1900;
$date = sprintf "%04d%02d%02d", $year, $mon, $mday;
$time = sprintf "%02d%02d%02d", $hour, $min, $sec;

# remove old logfiles
&RemoveOldLogFiles;

# open and initializer log file
$logFile = "$ENV{MIDSVCS_HOME}/log/midsvcs_socket.$date.$time.log";
open(LOGFILE, ">$logFile") || exit 0;
# set autoflush=1
autoflush LOGFILE 1;
my $date = getLogDate();
print LOGFILE "[$date] Server is accepting connections $0\n";

while (1) {

    print LOGFILE "[$date] Prepare to handle new request.\n";
    eval {
        handleRequest();
        1;
    } or do {                       # catch
        my $date = getLogDate();
        print LOGFILE "[$date] Unexpected Error $@\n";
    };

}
print LOGFILE "Server is being shut down.\n";
close(LOGFILE);
exit (0);


######################### LOCAL PROCEDURES #######################

sub handleRequest {
    my $client = $server->accept();
    $client->autoflush(1);

    $clientHost = $client->peerhost();
#    $clientAddr = $client->peeraddr();
#    $clientPort = $client->peerport();
    my $date = getLogDate();
    print LOGFILE "[$date] Client connected from $clientHost. Status: $!\n";

    $midnameservicesFile = "$ENV{MIDSVCS_HOME}/etc/mid-services-data";
    open(FILE, $midnameservicesFile) || exit(0);
    while (<FILE>) {
        next if /^#/ || /^\s*$/;
        chop;
        s/^\s+//;
        s/\s+$//;
        print $client $_, "\n";
    }
    close(FILE);
    close $client;
}

sub RemoveOldLogFiles {

  #
  # 1209600 is the number of seconds in 14 days
  #
  ($d,$d,$d,$mday,$mon,$year) = localtime(time-1209600);
  $year += 1900;
  $mon += 1;
  $mon = "0$mon" if length($mon) == 1;
  $mday = "0$mday" if length($mday) == 1;
  print "Removing log files on or before $year$mon$mday\n";
  opendir(D,"$ENV{MIDSVCS_HOME}/log");
  @files = readdir(D);
  closedir(D);
  foreach $f (@files) {
    if ($f =~ /\.log$/ && $f le "$year$mon$mday.000000.log") {
      print "Removing $f\n";
      unlink "$ENV{MIDSVCS_HOME}/log/$f";
    }
  }
}

sub interrupt {
  my($signal)=@_;
  my $date = getLogDate();
  print LOGFILE "[$date] Killed by interrupt signal $signal.\n";
  close(LOGFILE);
  exit 1;
 }

sub getLogDate {
    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
    $mon++; $year += 1900;
    return sprintf "%04d-%02d-%02d %02d:%02d:%02d", $year, $mon, $mday, $hour, $min, $sec;
}

