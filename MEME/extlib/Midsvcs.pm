# MID services are name/value mappings to provide a level of indirection.
# The names and values are arbitrary strings and are stored in the file
# /etc/umls/mid-services.

# suresh@nlm.nih.gov 9/98 - original
# suresh@nlm 6/2002 - updated

package Midsvcs;

use Socket;

$HOST = 'midns.nlm.nih.gov';
$PORT = '5125';

# CONSTRUCTOR
sub new {
  my($class) = @_;
  my($self) = {};
  
  bless $self;
  return $self;
}

# CLASS METHOD
sub get {
  my($self, $svc) = @_;

  $services = $self->load;
  if ($svc) {
    return $services->{$svc};
  } else {
    return $services;
  }
}

# returns all the services and values in a hash
sub load {
  my($self) = @_;
  my($sin);
  my($services);

  $services = {};
  my($proto) = getprotobyname("tcp");
  socket(P, PF_INET, SOCK_STREAM, $proto) || die "Cannot open socket: $!";
  $sin = sockaddr_in($PORT, inet_aton($HOST));
  connect(P, $sin) || die "Cannot connect to $HOST: $!";

  while (<P>) {
    chomp;
    @x = split /\|/, $_, 2;
    $services->{$x[0]} = $x[1];
  }
  close(P);
  return $services;
}
1;
