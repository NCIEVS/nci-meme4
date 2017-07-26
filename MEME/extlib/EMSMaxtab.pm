# EMS Maxtab is where the EMS stores meta information about itself.
# This class is the interface to it.

# suresh@nlm.nih.gov 4/2003

package EMSMaxtab;

use lib "/site/umls/lib/perl";
push @INC, "/site/umls/lib/perl";

use Data::Dumper;

require "xmlutils.pl";

# some variables
$EMSMAXTABTABLE = "EMS_MAX_TAB";
$XMLROOTNAME = "XMLDATA"; # in XML

# initializers:
# dbh=>
# key=>
# object=>

# CONSTRUCTOR
sub new {
  my($class, $initializer) = @_;
  my($self) = {};
  
  die "ERROR: Need a database handle" unless $initializer->{dbh};
  $self->{dbh} = $initializer->{dbh};
  die "ERROR: Need a key" unless $initializer->{key};
  $self->{key} = $initializer->{key};

  bless $self;

  $self->createTable($self->{dbh});

  if ($initializer->{object}) {
    $self->set($initializer->{object});
  }
  return $self;
}

# CLASS METHODS

sub dumpAll {
  my($self, $dbh) = @_;
  foreach $key ($dbh->selectAllAsArray("select key from $EMSMAXTABTABLE")) {
    my($m) = $self->readFromDB($dbh, $key);
    $m->dump;
  }
}

sub createTable {
  my($self, $dbh) = @_;
  my($spec) = [
	       {key=>'varchar2(128)'},
	       {xmlclob=>"CLOB"}
	      ];
  return if $dbh->tableExists($EMSMAXTABTABLE);
  $dbh->createTable($EMSMAXTABTABLE, $spec);
}

# reads an EMSMaxtab row from the DB and returns it as an object
sub readFromDB {
  my($self, $dbh, $key) = @_;
  my($object) = {};
  my($xml);

  $self->createTable($dbh);
  $dbh->setAttributes({ LongReadLen=>20000 });

  $xml = $dbh->selectFirstAsScalar("select xmlclob from $EMSMAXTABTABLE where key=" . $dbh->quote($key));
  $object = &xmlutils::fromXML($xml, $XMLROOTNAME) if $xml;

  $dbh->setAttributes({ LongReadLen=>0 });
  return new EMSMaxtab({dbh=>$dbh, key=>$key, object=>$object});
}

# Does record exist?
sub recordExists {
  my($self, $dbh, $key) = @_;
  my($k);

  eval {
    $k = $dbh->selectFirstAsScalar("select key from $EMSMAXTABTABLE where key=" . $dbh->quote($key));
    return $k unless $@;
  };
  return undef;
}

# CLASS/INSTANCE METHODS

# returns the value of a slot for the object
# Can be called as a class or instance method
# e.g., $request->get('timestamp');
#       EMSMaxtab->get({dbh=>$dbh, key=>$key, slot=>'timestamp'});
sub get {
  my($self, $slot) = @_;

  if (ref($self)) {
    return undef unless $self->{dbh}->tableExists($EMSMAXTABTABLE);
    return $self->{object}->{$slot};
  } else {
    # class method
    my($params) = $slot;
    my($m) = $self->readFromDB($params->{dbh}, $params->{key});
    return $m->get($params->{slot});
  }
}

# sets the value of a slot
# If called as a class method, it sets all the parameters
# e.g., $request->set('Initials', 'SS');
#       $request->set({Initials=>'SS', First=>'Suresh'});
#       EMSMaxtab->set({dbh=>$dbh, key=>'SS', object=>{Initials=>'SS', First=>'Suresh'}});
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
    my($m) = $self->readFromDB($params->{dbh}, $params->{key});
    $m->set($params->{object});
    $m->flush;
  }
  return;
}

sub flush {
  my($self) = @_;
  $self->writeToDB;
}

# e.g., $request->remove;
#       EMSMaxtab->remove({dbh=>$dbh, key=>$key});
sub remove {
  my($self, $params) = @_;
  my($dbh);

  if (ref($self)) {
    $dbh = $self->{dbh};
    $sql = "delete from $EMSMAXTABTABLE where key=" . $dbh->quote($self->{key});
  } else {
    $dbh = $params->{dbh};
    $sql = "delete from $EMSMAXTABTABLE where key=" . $dbh->quote($params->{key});
  }
  eval {
    $dbh->executeStmt($sql);
  };
  return;
}

# INSTANCE METHODS

# Writes a request to the database
sub writeToDB {
  my($self) = @_;
  my($dbh) = $self->{dbh};
  my($sql, $xml);
  my($qx, $qk);

  $self->createTable($dbh);

  $xml = &xmlutils::toXML($self->{object}, $XMLROOTNAME);
  $qx = $dbh->quote($xml);
  $qk = $dbh->quote($self->{key});

  if ($dbh->selectFirstAsScalar("select key from $EMSMAXTABTABLE where key=$qk")) {
    $sql = "update $EMSMAXTABTABLE set xmlclob=$qx where key=$qk";
  } else {
    $sql = "insert into $EMSMAXTABTABLE(key,xmlclob) values($qk,$qx)";
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

# for debugging
sub dump {
  my($self) = @_;
  print Dumper($self);
}

#----------------------------------------
1;
