# Class for managing concept report request

# suresh@nlm.nih.gov 4/2003

package ReportRequest;

use lib "/site/umls/lib/perl";

use EMSUtils;
use EMSMaxtab;
use GeneralUtils;
use Data::Dumper;

require "xmlutils.pl";

# some variables
$REPORTREQUESTTABLE = "EMS_REPORT_REQUEST";
$XMLROOTNAME = "XMLDATA"; # in XML
$MAXTABKEY="REPORT_ID";

# initializers:
# dbh=>
# report_id=>
# timestamp=>
# status=>
# object=>

# CONSTRUCTOR
sub new {
  my($class, $initializer) = @_;
  my($self) = {};
  
  bless $self;

  die "ERROR: Need a database handle" unless $initializer->{dbh};
  $self->{dbh} = $initializer->{dbh};

  ReportRequest->createTable($self->{dbh});

  $self->{report_id} = $initializer->{report_id} || $self->nextID($self->{dbh});
  $self->{timestamp} = $initializer->{timestamp} || $self->{dbh}->currentDate;
  $self->{status} = $initializer->{status} || 'WAIT';

  $self->set($initializer->{object}) if $initializer->{object};
  $self->set('ReportID', $self->{report_id});
  $self->set('RequestStatus', $self->{status});
  return $self;
}

# CLASS METHODS

# returns new request ID and increments MAXTAB
sub nextID {
  my($self, $dbh) = @_;
  my($sql, $report_id);

  $sql = "select max(report_id)+1 from $REPORTREQUESTTABLE";
  eval {
    $report_id = $dbh->selectFirstAsScalar($sql);
  };
  $report_id = 1 unless $report_id;
  return $report_id;
}

# create the report request table if needed
sub createTable {
  my($self, $dbh) = @_;
  my($spec) = ['report_id',
	       {status=>'VARCHAR2(16)'},
	       {timestamp=>'DATE'},
	       {xmlclob=>"CLOB"}
	      ];

  return if $dbh->tableExists($REPORTREQUESTTABLE);
  $dbh->createTable($REPORTREQUESTTABLE, $spec);
}

# reads object from the DB
sub readFromDB {
  my($self, $dbh, $report_id) = @_;
  my($object) = {};
  my($xml, $r);

  $self->createTable($dbh);
  $dbh->setAttributes({ LongReadLen=>20000 });

  $r = $dbh->selectFirstAsRef("select report_id, timestamp, status, xmlclob from $REPORTREQUESTTABLE where report_id=$report_id");
  $object = &xmlutils::fromXML($r->[3], $XMLROOTNAME) if $r->[3];

  $dbh->setAttributes({ LongReadLen=>0 });
  return new ReportRequest({dbh=>$dbh, report_id=>$report_id, timestamp=>$r->[1], status=>$r->[2], object=>$object});
}

# Object exists?
sub recordExists {
  my($self, $dbh, $report_id) = @_;
  my($r);

  eval {
    $r = $dbh->selectFirstAsScalar("select report_id from $REPORTREQUESTTABLE where report_id=$report_id");
    return $r;
  };
  return 0;
}

# Returns an array of ReportRequests that can be run now
sub getPendingRequests {
  my($self, $dbh) = @_;
  my(@h);
  my($r, $t, $now);
  my($sql) = <<"EOD";
select report_id from $REPORTREQUESTTABLE
where  status != 'DONE'
EOD

  return @h unless $dbh->tableExists($REPORTREQUESTTABLE);

  $now = time;
  foreach $r ($dbh->selectAllAsArray($sql)) {
    my($reportRequest) = $self->readFromDB($dbh, $r);
    $t = $reportRequest->get('GenerateBeforeTime');
    next if $t && ($now>$t);
    $t = $reportRequest->get('GenerateAfterTime');
    next if $t && ($now<$t);
    push @h, $reportRequest;
  }
  return @h;
}

#------------------------------------------------------------
# CLASS or INSTANCE METHODS

# returns the value of a slot for the object
# Can be called as a class or instance method
# e.g., $request->get('timestamp');
#       ReportRequest->get({dbh=>$dbh, report_id=>$rid, slot=>'GenerateBeforeDate'});
sub get {
  my($self, $slot) = @_;

  if (ref($self)) {
    return undef unless $self->{dbh}->tableExists($REPORTREQUESTTABLE);
    return $self->{object}->{$slot};
  } else {
    # class method
    my($params) = $slot;
    my($m) = $self->readFromDB($params->{dbh}, $params->{report_id});
    return $m->get($params->{slot});
  }
}

# sets the value of a slot
# If called as a class method, it sets all the parameters
# e.g., $request->set('WorklistName', 'chk_foo');
#       $request->set({WorklistBase=>'wrk04b_missyn_ch', WorklistStartNum=>20, WorklistEndNum=>22});
#       ReportRequest->set({dbh=>$dbh, report_id=>23, object=>{WorklistName=>'chk_suresh', Database=>'oa_mid2004'}});
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
    my($m) = $self->readFromDB($params->{dbh}, $params->{report_id});
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
#       ReportRequest->remove({dbh=>$dbh, report_id=>$key});
sub remove {
  my($self, $params) = @_;
  my($dbh, $id);

  if (ref($self)) {
    $dbh = $self->{dbh};
    $id = $self->{report_id};
  } else {
    $dbh = $params->{dbh};
    $id = $params->{report_id};
  }
  eval {
    $sql = "delete from $REPORTREQUESTTABLE where report_id=$id";
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
  my($qx, $id, $qs);

  $self->createTable($dbh);

  $xml = &xmlutils::toXML($self->{object}, $XMLROOTNAME);
  $qx = $dbh->quote($xml);
  $id = $self->{report_id};
  $qs = $dbh->quote($self->{status});

  if ($dbh->selectFirstAsScalar("select report_id from $REPORTREQUESTTABLE where report_id=$id")) {
    $sql = "update $REPORTREQUESTTABLE set timestamp=SYSDATE,status=$qs,xmlclob=$qx where report_id=$id";
  } else {
    $sql = "insert into $REPORTREQUESTTABLE(report_id,timestamp,status,xmlclob) values($id,SYSDATE,$qs,$qx)";
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
  print Dumper($self->{object});
}

#----------------------------------------
1;
