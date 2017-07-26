# Locking for EMS bins

# suresh@nlm.nih.gov 4/2003

package EMSBinlock;
BEGIN
{
unshift @INC, "$ENV{ENV_HOME}/bin";
require "env.pl";
unshift @INC, "$ENV{EMS_HOME}/lib";
unshift @INC, "$ENV{EMS_HOME}/bin";
}

use EMSNames;
use EMSTables;
use Data::Dumper;

# some variables
$BINLOCKTABLE = $EMSNames::BINLOCKTABLE;

# data is a hash ref containing the lock elements
sub lock {
  my($self, $dbh, $data) = @_;

  $self->unlock($dbh, $data) if $self->islocked($dbh, $data);

  $data->{bin_name} = $dbh->quote($data->{bin_name});
  $data->{timestamp} = "SYSDATE" unless $data->{timestamp};
  $data->{reason} = $dbh->quote($data->{reason} || "unknown");
  $data->{owner} = $dbh->quote($data->{owner} || "unknown");

  $dbh->insertRow($BINLOCKTABLE, $data);
}

sub unlock {
  my($self, $dbh, $data) = @_;
  my($sql);

  $sql = "delete from $BINLOCKTABLE where bin_name=" . $dbh->quote($data->{bin_name});
  $dbh->executeStmt($sql);
}

# true if bin is locked
sub islocked {
  my($self, $dbh, $data) = @_;
  $sql = "select bin_name from $BINLOCKTABLE where bin_name=" . $dbh->quote($data->{bin_name});
  return ($dbh->selectFirstAsScalar($sql) ? 1 : 0);
}

# returns lock data as a hash ref
sub get {
  my($self, $dbh, $data) = @_;
  return EMSTables->row2hash($dbh, $BINLOCKTABLE, 'bin_name', $dbh->quote($data->{bin_name}));
}

# returns all locks in a array of references
sub get_all {
  my($self, $dbh) = @_;
  my(@l, $bin_name);

  foreach $bin_name ($dbh->selectAllAsArray("select bin_name from $BINLOCKTABLE")) {
    push @l, $self->get($dbh, { bin_name=>$bin_name });
  }
  return @l;
}

1;
