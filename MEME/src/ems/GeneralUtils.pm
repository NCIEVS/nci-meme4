# General Utilities
# Author: suresh@nlm.nih.gov 1/2003

package GeneralUtils;
unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";

use Archive::Zip;
use Digest::MD5 qw(md5_hex);
use Mail::Sender;
use List::Util;

# CONSTRUCTOR
sub new {
  my($class, $initializer) = @_;
  my($self) = {};
  
  if (defined $initializer) {
    if (ref($initializer)) {
      if (ref($initializer) eq "HASH") {
	foreach (keys %{ $initializer }) {
	  $self->{ $_ } = $initializer->{$_};
	}
      } elsif (ref($initializer) eq "ARRAY") {
	for ($i=0; $i<@{$initializer}-1; $i++) {
	  $_[0] = $initializer->[$i];
	  $_[1] = $initializer->[$i+1];
	  $self->{$_[0]} = $_[1];
	}
      } elsif (ref($initializer) eq "SCALAR") {
	foreach (split /\&/, $$initializer) {
	  @_ = split /\=/, $_;
	  $self->{$_[0]} = $_[1];
	}
      }
    } else {
      if ($initializer =~ /^\d+$/) {
	$self->{degree} = $initializer;
      } else {
	foreach (split /\&/, $initializer) {
	  @_ = split /\=/, $_;
	  $self->{$_[0]} = $_[1];
	}
      }
    }
  }
  bless $self, $class;
  return $self;
}

# CLASS METHODS

# Authentication information for the database
# gets the password for the user; if user is not given,
# the password for the first user is returned.
sub getOraclePassword {
  my($self, $user) = @_;
  my($cmd) = $ENV{MIDSVCS_HOME} . "/bin/get-oracle-pwd.pl";
  die "ERROR: DBPASSWORD_HOME not set\n" unless $ENV{MIDSVCS_HOME};
  die "ERROR: password script not found\n" unless (-e $cmd && -x $cmd);

  $_ = `$cmd -u $user`;
  my($u, $p) = split /\//, $_;
  chomp($p);
  return $p;
}

# positions the file glob on the first matching line
# returns -1 if it fails to find matches
sub seekstr {
    # Seek a given filehandle to the first line starting with a given string.
    # The corresponding file must be sorted on the first |-separated field.
    local($self, *FILE, $value) = @_;
    my($SIZE, $l, $h, $pos);
    $SIZE = (stat(FILE))[7];
    $l = 0;   $h=$SIZE-1;
    while ($l < $h-16) {
        $pos = int(($l + $h) / 2);
        seek(FILE, $pos, 0)  ||  die "can't seek to $pos\n";
        $_ = <FILE>  if $pos != 0;
        $_ = <FILE>;
        if ($_ eq '') {
            $h = $pos - 1;
            next;
        }
#        ($inval) = /([^|]*)/;
        ($inval) = /([^|]*)/;
	$inval = substr($_, 0, length($value));
        if ($inval ge $value) {
            $h = $pos - 1;
        } else {
            $l = $pos + 1;
        }
    }
    seek(FILE, $l, 0);
    $_ = <FILE> if $l != 0;
    $pos=tell(FILE);
    while (1) {
        $_ = <FILE>;
        return -1 if $_ eq '';
#        ($inval) = /([^|]*)/;
	$inval = substr($_, 0, length($value));
        if ($inval eq $value) {
            seek(FILE, $pos, 0);
            return $pos;
        } elsif ($inval gt $value) {
            return -1;
        }
        $pos=tell(FILE);
    }
}

# returns the modification time of a file as seconds since epoch
sub modtime {
  my($class, $file) = @_;
  return (stat($file))[9];
}

# makes up a temporary file name
sub tempname {
  my($self, $dir, $prefix) = @_;
  my($file);
  my($n) = $$;

  $dir = $ENV{TMPDIR} || "/tmp" unless $dir && -e $dir;
  $prefix = "tmpnam" unless $prefix;
  $file = join('/', $dir, join('.', $prefix, $n));
  while (-e $file) {
    $file = join('/', $dir, join('.', $prefix, ++$n));
  }
  return($file);
}

# returns the node name, i.e., the name of current host
sub nodename {
  my($self) = @_;
  my($h);

  $h = `/bin/uname -n`;
  chomp($h);
  return $h;
}

# some identity of the user running the program
sub username {
  my($self) = @_;
  my($u) = getlogin() || (getpwuid($<))[0];
  $u = ($ENV{'USER'} || "???") unless $u;
  chomp($u);
  return $u;
}

# maps an IP to a name
sub ip2name {
  my($self, $ip) = @_;

  return "" unless $ip;
  return "" unless $ip =~ /^[\d\.]+$/;
  @_ = gethostbyaddr(pack("C4", split(/\./, $ip)), 2);
  return $_[0];
}

# sends email using Mail::Sender
# known params (see perldoc Mail::Sender)
# from=>, fake_from=>, to=>, cc=>, fake_to=>, fake_cc=>,
# bcc=>, smtp=>, subject=>, headers=>, multipart=>, boundary=>,
# ctype=>, encoding=>, charset=>, client=>,
# priority=>
sub mailsender {
  my($self, $senderparams) = @_;
  my($sender) = new Mail::Sender($senderparams);

  die "ERROR in mailsender: $Mail::Sender::Error" unless $sender;
  $sender->MailMsg($senderparams);
  die $sender->{error} if $Mail::Sender::Error;
}

# sends email
# $params is a hash ref containing to, from, subject, msg fields
# some of which can be an array reference or a scalar
# to, from are required
sub sendmail {
  my($self, $params) = @_;
  my($from, $to, $subject, $msg);

  $subject = $params->{subject} || "No subject";
  $from = $params->{from} || return;
  $to = $params->{to} || return;
  if (ref $to eq "ARRAY") {
    $to = join(', ', @{ $to });
  }
  $msg = $params->{msg} || "No message given";

  open(SENDMAIL, "|/usr/lib/sendmail -oi -t -odq")
    || die "Can't fork for sendmail: $!\n";
  print SENDMAIL <<"EOD";
From: $from
To: $to
Subject: $subject

$msg
EOD
  close(SENDMAIL);
  return;
}

# sorts a list unique
sub uniquesort {
  my($self, @x) = @_;
  my(%x);

  foreach (@x) {
    $x{$_}++;
  }
  return sort keys %x;
}

sub date {
  my($self, $opt) = @_;
  my($d);

  if ($opt) {
    $d = `/bin/date \'$opt\'`;
  } else {
    $d = `/bin/date`;
  }
  chomp($d);
  return $d;
}

# removes leading and trailing space chars
sub trim {
  my($self, $s) = @_;

  $s =~ s/^\s*//;
  $s =~ s/\s*$//;

  return $s;
}

# converts seconds to HH:MM:SS
sub sec2hms {
  my($self, $s) = @_;
  return ($s == 0 ? "00:00:00" : sprintf("%02d:%02d:%02d",
					 int($s/3600),
					 int(($s % 3600)/60),
					 int($s % 60)));
}

# returns the contents of a file in a string
# $prop is a hash of properties (skipcomments, skipspaces, flatten)
sub file2str {
  my($self, $file, $props) = @_;
  my($s);

  open(F, $file) || return "";
  while (<F>) {
    chomp if $props->{flatten};
    next if $props->{skipcomments} and /^\#/;
    next if $props->{skipspaces} and /^\s*$/;

    $s .= ($props->{flatten} ? ($s ? " $_" : $_) : $_);
  }
  close(F);
  $s =~ s/;$// if $props->{removetrailingsemicolon};
  return $s;
}

# Writes or appends a string to a file
sub str2file {
  my($self, $str, $file, $appendflag) = @_;
  my($s);

  if ($appendflag) {
    open(F, ">>$file") || die "ERROR: Cannot append to $file";
  } else {
    open(F, ">$file") || die "ERROR: Cannot write to $file";
  }
  print F $str;
  close(F);
  return;
}

# Converts key/value pairs to a hash (a=b&c=d..)
sub str2hash {
  my($self, $s) = @_;
  my(%h);
  my($p, $k, $v);

  foreach $p (split /&/, $s) {
    ($k, $v) = split /=/, $p;
    $h{$k} = $v;
  }
  return \%h;
}

# Converts hash to key/value pairs (a=b&c=d..)
sub hash2str {
  my($self, $h) = @_;
  my(@s);
  my($k, $v);

  foreach $k (keys %$h) {
    push @s, join('=', $k, $h->{$k});
  }
  return join('&', @s);
}

# Computes the MD5 for a string as a 32-byte hex digest
sub md5 {
  my($class, $str) = @_;
  return ($str ? md5_hex($str) : "");
}

# randomizes the letters in a string
sub str2random {
  my($class, $s) = @_;
  my($i);
  my($l) = length($s);
  my(@x);

  for ($i=0; $i<length($s); $i++) {
    push @x, substr($s, $i, 1);
  }
  return join('', List::Util::shuffle(@x));
}

######## ZIP utilities ########
sub createZip {
  my($self, $zipfile) = @_;
  my($zipmodule) = Archive::Zip->new();
}

sub addFileToZip {
  my($self, $zipfile, $file) = @_;
  my($zipmodule) = Archive::Zip->new($zipfile);

  $zipmodule->addFile($file);
}

sub addDirToZip {
  my($self, $zipfile, $dir) = @_;
  my($zipmodule) = Archive::Zip->new($zipfile);

  $zipmodule->addDirectory($dir);
}

sub removeFromZip {
  my($self, $zipfile, $path) = @_;
}

#----------------------------------------------------------------------
1;
