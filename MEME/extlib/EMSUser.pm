# Stores data about EMS users

# suresh@nlm.nih.gov 4/2003

package EMSUser;

use lib "/site/umls/lib/perl";

use EMSMaxtab;
use GeneralUtils;
use Data::Dumper;

require "xmlutils.pl";

# some variables
$EMSUSERSTABLE = "EMS_USERS";
$XMLROOTNAME = "XMLDATA"; # in XML

# initializers:
# dbh=>
# initials=>
# object=>

# CONSTRUCTOR
sub new {
  my($class, $initializer) = @_;
  my($self) = {};
  
  die "ERROR: Need a database handle" unless $initializer->{dbh};
  $self->{dbh} = $initializer->{dbh};

  die "ERROR: Need user's initials" unless $initializer->{initials};
  $self->{initials} = $initializer->{initials};

  bless $self;

  if ($initializer->{object}) {
    $self->set($initializer->{object});
    $self->flush;
  }
  $self->set('Initials', $self->{initials});
  return $self;
}

# CLASS METHODS

# create the report request table if needed
sub createTable {
  my($self, $dbh) = @_;
  my($spec) = [{initials=>'VARCHAR2(8)'},
	       {xmlclob=>"CLOB"}
	      ];

  return if $dbh->tableExists($EMSUSERSTABLE);
  $dbh->createTable($EMSUSERSTABLE, $spec);
}

# reads object from the DB
sub readFromDB {
  my($self, $dbh, $initials) = @_;
  my($object) = {};
  my($xml);

  $self->createTable($dbh);
  $dbh->setAttributes({ LongReadLen=>20000 });

  $xml = $dbh->selectFirstAsScalar("select xmlclob from $EMSUSERSTABLE where initials=" . $dbh->quote($initials));
  return undef unless $xml;
  $object = &xmlutils::fromXML($xml, $XMLROOTNAME) if $xml;

  $dbh->setAttributes({ LongReadLen=>0 });
  return new EMSUser({dbh=>$dbh, initials=>$initials, object=>$object});
}

# returns all current users
sub getAllUsers {
  my($self, $dbh) = @_;
  my(@u);
  my($initials);

  my($sql) = <<"EOD";
select distinct initials from $EMSUSERSTABLE order by initials
EOD

  return @u unless $dbh->tableExists($EMSUSERSTABLE);
  foreach $initials ($dbh->selectAllAsArray($sql)) {
    push @u, $self->readFromDB($dbh, $initials);
  }
  return @u;
}

# Looks at CGI environment variables and returns a single best matching user
sub getUser {
  my($self, $dbh) = @_;
  my(@u);
  my($initials);

  my($sql) = <<"EOD";
select distinct initials from $EMSUSERSTABLE
EOD

  return undef unless $dbh->tableExists($EMSUSERSTABLE);

  foreach $initials ($dbh->selectAllAsArray($sql)) {
    my($u) = $self->readFromDB($dbh, $initials);

    return $initials if grep { $_ eq $ENV{'REMOTE_ADDR'} }
      split /,/, $u->get('IP');

    if ($ENV{'REMOTE_HOST'}) {
      my($h) = $ENV{'REMOTE_HOST'};
      $h .= ".nlm.nih.gov" unless $h =~ /\./;
      return $initials if grep { lc($_) eq lc($ENV{'REMOTE_HOST'}) } split /,/, $u->get('Host');
    }
  }
  return 'SS' if ((GeneralUtils->nodename eq "astra" || GeneralUtils->nodename eq "smis")) && GeneralUtils->username eq "suresh";
  return undef;
}

#------------------------------------------------------------
# CLASS or INSTANCE METHODS

# returns the value of a slot for the object
# Can be called as a class or instance method
# e.g., $user->get('timestamp');
#       EMSUser->get({dbh=>$dbh, initials=>'SS'});
sub get {
  my($self, $slot) = @_;

  if (ref($self)) {
    return undef unless $self->{dbh}->tableExists($EMSUSERSTABLE);
    return $self->{object}->{$slot};
  } else {
    # class method
    my($params) = $slot;
    my($m) = $self->readFromDB($params->{dbh}, $params->{initials});
    return $m->get($params->{slot});
  }
}

# sets the value of a slot
# If called as a class method, it sets all the parameters
# e.g., $user->set('Email', 'suresh\@nlm.nih.gov');
#       $user->set({IP=>'130.14.33.144,130.14.45.234'});
#       EMSUser->set({dbh=>$dbh, initials=>'SS', object=>{..}});
sub set {
  my($self, $slot, $value) = @_;

  if (ref($self)) {
    if (ref($slot) eq "HASH") {
      my($s);
      foreach $s (keys %{ $slot }) {
	$self->set($s, $slot->{$s});
      }
    } elsif (ref($slot) eq "ARRAY") {
      my($s, $v);
      foreach (@{ $slot }) {
	($s, $v) = split /=/, $_;
	$self->set($s, $v);
      }
    } elsif (!$value && $slot =~ /^([^=]+)=([^=]+)$/) {
      $self->set($1, $2);
    } elsif ($slot) {
      $self->{object}->{$slot} = $value;
    }
  } else {
    my($params) = $slot;
    my($m) = $self->readFromDB($params->{dbh}, $params->{initials});
    $m->set($params->{object});
    $m->flush;
  }
  return;
}

sub flush {
  my($self) = @_;
  $self->writeToDB;
}

# e.g., $user->remove;
#       EMSUser->remove({dbh=>$dbh, initials=>$i});
sub remove {
  my($self, $params) = @_;
  my($dbh, $initials);

  if (ref($self)) {
    $dbh = $self->{dbh};
    $initials = $self->{initials};
  } else {
    $dbh = $params->{dbh};
    $initials = $params->{initials};
  }
  eval {
    $sql = "delete from $EMSUSERSTABLE where initials=" . $dbh->quote($initials);
    $dbh->executeStmt($sql);
  };
  return;
}

# INSTANCE METHOD

# Writes a request to the database
sub writeToDB {
  my($self) = @_;
  my($dbh) = $self->{dbh};
  my($xml, $sql);
  my($qx, $initials, $qi);

  $self->createTable($dbh);

  $xml = &xmlutils::toXML($self->{object}, $XMLROOTNAME);
  $qx = $dbh->quote($xml);
  $initials = $self->{initials};
  $qi = $dbh->quote($initials);

  if ($dbh->selectFirstAsScalar("select initials from $EMSUSERSTABLE where initials=$qi")) {
    $sql = "update $EMSUSERSTABLE set xmlclob=$qx where initials=$qi";
  } else {
    $sql = "insert into $EMSUSERSTABLE(initials,xmlclob) values($qi,$qx)";
  }
  $dbh->executeStmt($sql);
}

# DUP's the database reference
sub dupDbRef {
  my($self) = @_;
  my($new) = $self->{dbh}->dup;

#  $self->{dbh}->disconnect;
  $self->{dbh} = $new;
}

sub disconnect {
  my($self) = @_;
  $self->{dbh}->disconnect;
}

# Dumps the state of the bin
sub dump {
  my($self) = @_;
  print Dumper($self);
}

#----------------------------------------
1;
