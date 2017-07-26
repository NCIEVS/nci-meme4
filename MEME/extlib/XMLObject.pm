# Class to store information about a perl object in the database as an
# XML CLOB.  The interface hides the details of the XML and access to
# and from the DB.

# suresh@nlm.nih.gov 3/2003

package XMLObject;

use lib "/site/umls/lib/perl";

use ParallelExec;
use EMSUtils;
use GeneralUtils;
use Data::Dumper;
use XML::Simple;

# some variables
$XMLROOTNAME="XMLDATA";

# The object is initialized with the database handle, the name of the
# table where it is stored (this will have the two columns: key and XMLCLOB),
# the type for the 'key' column.

# initializers:
# dbh=<database handle> (required)
# table=(name of table to store object in)
# key= (key to access this record by)
# keytype= the type for the key, e.g., integer, VARCHAR, etc.
# object=(a hash ref of the object to initialize with) optional
# debug=

# CONSTRUCTOR
sub new {
  my($class, $params) = @_;
  my($self) = {};
  
  $self->{dbh} = $params->{dbh};
  die "ERROR: Need a database handle" unless $self->{dbh};
  $self->{table} = $params->{table};
  die "ERROR: Need a table name" unless $self->{table};
  $self->{key} = $params->{key};
  die "ERROR: Need a key" unless $self->{key};
  $self->{keytype} = $params->{keytype} || "integer";
  die "ERROR: Need a column specification for the key column" unless $self->{keytype};

  $self->{qkey} = $self->{keytype} eq "integer" ? $self->{key} : $self->{dbh}->quote($key);

  $self->dupDbRef unless $self->{dbh}->{dbh};
  bless $self;

  $self->createTable;
  $self->set($params->{object}) if ($params->{object});
  return $self;
}

# Reads an object from the database
# need: dbh=, key=, table=, keytype=
sub readFromDB {
  my($self, $params) = @_;
  my($xmlobject);
  my($dbh) = $params->{dbh};
  my($key) = $params->{key};
  my($qkey);

  $qkey = $dbh->quote($key) if ($params->{keytype} ne "integer");
  $dbh->setAttributes({ LongReadLen=>20000 });

  my($xml) = $dbh->selectFirstAsArray("select XMLCLOB from " . $params->{table} . " where key=$qkey");
  $params->{object} = $self->fromXML($xml) if $xml;
  $params->{key} = $key;
  $xmlobject = new XMLObject($params);

  $dbh->setAttributes({ LongReadLen=>0 });
  return $xmlobject;
}

# XML transformations
sub toXML {
  my($self, $ref, $r) = @_;

  $rootname = $r || $XMLObject::XMLROOTNAME;
  $xmlsimple = new XML::Simple(rootname=>$rootname) unless $xmlsimple;
  return $xmlsimple->XMLout($ref);
}

sub fromXML {
  my($self, $xml, $r) = @_;

  $rootname = ($r || $XMLObject::XMLROOTNAME);
  $xmlsimple = new XML::Simple(rootname=>$rootname) unless $xmlsimple;
  return $xmlsimple->XMLin($xml);
}

# Is there a record in the database for an/this object?
sub isInDB {
  my($self, $params) = @_;

  if (ref($self)) { # instance method
    return $self->{dbh}->selectFirstAsScalar("select key from " . $self->{table} . " where key=" . $self->{qkey});
  } else { # class method
    my($dbh) = $params->{dbh};
    my($qkey) = ($params->{keytype} eq "integer" ? $params->{key} : $self->{dbh}->quote($params->{key}));
    return $self->{dbh}->selectFirstAsScalar("select key from " . $params->{table} . " where key=$qkey");
  }
}

#------------------------------------------------------------
# INSTANCE METHODS

# Creates the table to store the object if needed
sub createTable {
  my($self) = @_;
  my($colspec) = [{key=>$self->{keytype}}, {XMLCLOB=>'CLOB'}];

  return if $self->{dbh}->tableExists($self->{table});
  $self->{dbh}->createTable($self->{table}, $colspec);
}

# Writes this object to the database
sub writeToDB {
  my($self) = @_;
  my($xml, $sql, $clob);
  my($key) = $self->{key};

  $key = $self->{dbh}->quote($key) if $self->{keytype} ne "integer";

  $xml = $self->toXML($self->{OBJECT});
  $clob = $self->{dbh}->quote($xml);

  if ($self->isInDB($key)) {
    $sql = "update " . $self->{table} . " set XMLCLOB=$clob where key=$key";
  } else {
    $sql = "insert into " . $self->{table} . " (key,XMLCLOB) VALUES ($key,$clob)";
  }
  $self->{dbh}->executeStmt($sql);
}

# returns the value of a slot(s)
sub get {
  my($self, $slot) = @_;

  if (ref($slot) eq "ARRAY") {
    my(@x);
    foreach (@{ $slot }) {
      push @x, $self->{OBJECT}->{$_};
    }
    return @x;
  } else {
    return $self->{OBJECT}->{$slot};
  }
}

# sets the value(s) of one or more slots
sub set {
  my($self, $slot, $value) = @_;

  if (ref($slot) eq "HASH") {
    # for hashes, the values are in the hash
    foreach (keys %{ $slot }) {
      $self->{OBJECT}->{$_} = $slot->{$_};
    }
  } elsif (ref($slot) eq "ARRAY") {
    if ($value && ref($value) eq "ARRAY") {
      my($i);
      for ($i=0; $i<@{ $slot }; $i++) {
	$self->{OBJECT}->{$slot->[$i]} = $value->[$i];
      }
    } else {
      foreach (@{ $slot }) {
	@_ = split /=/, $_;
	$self->{OBJECT}->{$_[0]} = $slot->{$_[1]};
      }
    }
  } elsif (!$value && $slot =~ /^([^=]+)=([^=]+)$/) {
    $self->{OBJECT}->{$1} = $2;
  } elsif ($slot) {
    $self->{OBJECT}->{$slot} = $value;
  }
  return;
}

# deletes this bin from the database
sub removeFromDB {
  my($self) = @_;
  my($key) = $self->{key};

  $key = $self->{dbh}->quote($key) if ($self->{keytype} ne "integer");
  my($sql) = "delete from " . $self->{table} . " where key=$key";

  eval {
    $self->{dbh}->executeStmt($sql);
  };
  die $@ if $@;
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

sub dump {
  my($self) = @_;
  print Dumper($self);
}
