# This is an interface to the EMS3_MAXTAB table where the EMS stores
# meta information about itself.

# suresh@nlm.nih.gov 4/2003

package EMSMaxtab;
unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";
use lib "$ENV{'EMS_HOME'}/lib";

use EMSNames;
use EMSTables;
use Data::Dumper;

# some variables
$EMSMAXTABTABLE = $EMSNames::MAXTABTABLE;

# Clears the EMSMAXTAB table
sub clear {
  my($self, $dbh) = @_;

  $dbh->dropTable($EMSMAXTABTABLE);
  $self->createTable($dbh);
}

sub dumpAll {
  my($self, $dbh) = @_;
  foreach $key ($dbh->selectAllAsArray("select key from $EMSMAXTABTABLE")) {
    my($m) = $self->readFromDB($dbh, $key);
    print Dumper($m);
  }
}

sub createTable {
  my($self, $dbh) = @_;
  EMSTables->createTable($dbh, $EMSMAXTABTABLE);
}

# Does record exist?
sub recordExists {
  my($self, $dbh, $key) = @_;
  my($k);

  $k = $self->readFromDB($dbh, $key);
  return $k;
}

# returns the record as a hash given a key
# e.g., EMSMaxtab->get({dbh=>$dbh, key=>$key});
sub get {
  my($self, $dbh, $key) = @_;
  return $self->readFromDB($dbh, $key);
}

# sets the value of a slot.  If undef, the previous value is preserved
# e.g., EMSMaxtab->set($dbh, $key, { valueint=>0, valuechar='', timestamp=})
sub set {
  my($self, $dbh, $key, $params) = @_;
  return unless $key;
  my($previous) = $self->readFromDB($dbh, $key);
  my($new) = {};

  $new->{key} = $key;
  $new->{valueint} = $params->{valueint} || $previous->{valueint} || 0;
  $new->{valuechar} = $params->{valuechar} || $previous->{valuechar} || "";
  $new->{timestamp} = $params->{timestamp} || $previous->{timestamp} || "SYSDATE";

  $self->writeToDB($dbh, $key, $new);
  return;
}

# removes the entry
# e.g., EMSMaxtab->remove($dbh, $key);
sub remove {
  my($self, $dbh, $key) = @_;
  my($sql, $k);

  if (ref($key) eq "ARRAY") {
    foreach $k (@$key) {
      $self->remove($dbh, $k);
    }
  } elsif (!ref($key)) {
    $sql = "delete from $EMSMAXTABTABLE where key=" . $dbh->quote($key);
    eval {
      $dbh->executeStmt($sql);
    };
  }
  return;
}

# reads a row from the DB given a key
sub readFromDB {
  my($self, $dbh, $key) = @_;

  $self->createTable($dbh);
  my($r) = $dbh->selectFirstAsRef("select valueint, valuechar, timestamp from $EMSMAXTABTABLE where key=" . $dbh->quote($key));
  return undef unless $r;
  return { key=>$key, valueint=>$r->[0], valuechar=>$r->[1], timestamp=>$r->[2] };
}

# Writes a request to the database
sub writeToDB {
  my($self, $dbh, $key, $maxtab) = @_;

  $self->createTable($dbh);
  if ($self->recordExists($dbh, $key)) {
    my(@x);
    push @x, "valueint=" . $maxtab->{valueint};
    push @x, "valuechar=" . $dbh->quote($maxtab->{valuechar});
    push @x, "timestamp=" . (($maxtab->{timestamp} eq "SYSDATE") ? "SYSDATE" : "TO_DATE(" . $dbh->quote($maxtab->{timestamp}) . ")");

    $sql = "update $EMSMAXTABTABLE set " . join(", ", @x) . " where key=" . $dbh->quote($key);
  } else {
    my(@x) = ();
    push @x, $dbh->quote($key);
    push @x, $maxtab->{valueint};
    push @x, $dbh->quote($maxtab->{valuechar});
    push @x, (($maxtab->{timestamp} eq "SYSDATE") ? "SYSDATE" : "TO_DATE(" . $dbh->quote($maxtab->{timestamp}) . ")");
    $sql = "insert into $EMSMAXTABTABLE(key, valueint, valuechar, timestamp) values(" . join(", ", @x) . ")";
  }
  $dbh->executeStmt($sql);
}

# increments the valueint column
sub increment {
  my($self, $dbh, $key) = @_;

  if ($self->readFromDB($dbh, $key)) {
    $sql = "update $EMSMAXTABTABLE set valueint=valueint+1 where key=" . $dbh->quote($key);
    $dbh->executeStmt($sql);
  } else {
    $self->writeToDB($dbh, $key, { valueint=>1, valuechar=>'', timestamp=>"SYSDATE" });
  }
}
#----------------------------------------

1;
