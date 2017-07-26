# Class to store information about and implement actions for a bin.
# A bin's configuration information is stored as XML in the DB.

# suresh@nlm.nih.gov 1/2003

package Bin;

use lib "/site/umls/lib/perl";

use ParallelExec;
use EMSUtils;
use EMSMaxtab;
use GeneralUtils;

require "xmlutils.pl";

# some variables
$CONFIGTABLE = "EMS_BIN_CONFIG2";
$MEBINS="ME_BINS2";
$BINTABLE="QA_BINS2";
$CONCEPTS_BEING_EDITED="CONCEPTS_BEING_EDITED";
$PARTITION_KEY="MEPARTITION";
$NUMCPU=4;
$EMSSCRIPTDIR="/site/umls/ems-3.0/utils";
$TMPDIR="/tmp";
$MAXRANK = 100000;
$XMLROOTNAME = "XMLDATA"; # in XML

@BINCONTENTS=qw(mixed chem nonchem);

# initializers:
# dbh=>
# bin=>
# active=>
# type=>
# timestamp=>
# object=>
# debug=>

# Class methods
# partition
# createConfigTable
# getAllBinNames
# getMEBinNames
# getBinRefs
# getMEBinRefs
# partitionParallel
# partitionSerial
# writeAll
# disconnectAll

# Instance methods
# readFromDB
# writeToDB
# get
# set
# toXML
# fromXML
# dupDbRef
# disconnect
# defaults
# generate
# count
# filter
# worklist
# checklist

# CONSTRUCTOR
sub new {
  my($class, $initializer) = @_;
  my($self) = {};
  
  die "ERROR: Need a database handle" unless $initializer->{dbh};
  $self->{dbh} = $initializer->{dbh};
  die "ERROR: Need a bin name" unless $initializer->{bin};
  $self->{bin} = $initializer->{bin};

  $self->{active} = $initializer->{active} || "Y";
  $self->{type} = $initializer->{type} || "QA";
  $self->{timestamp} = $initializer->{timestamp} || $self->{dbh}->currentDate;

  bless $self;

  $self->set($initializer->{object}) if $initializer->{object};
  $self->set('Bin', $self->{bin});
  return $self;
}

# CLASS METHODS

# Object exists?
sub recordExists {
  my($self, $dbh, $bin) = @_;
  my($qb) = $dbh->quote($bin);
  my($r);

  $r = $dbh->selectFirstAsScalar("select bin from $Bin::CONFIGTABLE where bin=$qb");
  return ($r ? 1 : 0);
}

# create the config table if needed
# if new, add predefined ME bin configuration information
sub createTable {
  my($self, $dbh) = @_;
  my($spec) = [
	       {bin=>"VARCHAR2(200)"},
	       {active=>'CHAR(1)'},
	       {type=>'VARCHAR2(10)'},
	       {timestamp=>'DATE'},
	       {xmlclob=>"CLOB"}
	      ];
  my($bc, $rank);

  return if $dbh->tableExists($Bin::CONFIGTABLE);
  $dbh->createTable($Bin::CONFIGTABLE, $spec);

# preload ME bin data

# DEMOTIONS
  $bc = Bin->new({dbh=>$dbh, bin=>'demotions', active=>'Y', type=>'ME',
		  object=>{
			   Type=>'ME',
			   Rank=>++$rank,
			   Description=>'Concepts with ambiguity in atom membership',
			   Editable=>'Y',
			   Predefined=>'Y',
			   GeneratorType=>'SELECT',
			   Generator=>"SELECT DISTINCT concept_id_1 AS concept_id FROM relationships WHERE status='D' UNION SELECT DISTINCT concept_id_2 AS concept_id FROM relationships WHERE status='D'",
			   OrderBeforeCount=>'Y',
			   OrdererType=>'SCRIPT'
			   }
		 }
		);
  $bc->flush;

# EMBRYOS
  $bc = Bin->new({dbh=>$dbh, bin=>'embryos', type=>'ME',
		  object=>{
			   Type=>'ME',
			   Rank=>++$rank,
			   Predefined=>'Y',
			   Description=>'Concepts in embryo status - not yet fully-formed',
			   GeneratorType=>'SELECT',
			   Generator=>"SELECT DISTINCT concept_id FROM concept_status WHERE status='E'"
			  }
		 }
		);
  $bc->flush;

# NORELEASE
  $bc = Bin->new({dbh=>$dbh, bin=>'norelease', type=>'ME',
		 object=>{
			  Type=>'ME',
			  Rank=>++$rank,
			  Predefined=>'Y',
			  Description=>'Concepts where all content is unreleasable',
			  GeneratorType=>'SELECT',
			  Generator=>"SELECT DISTINCT concept_id FROM concept_status WHERE tobereleased IN ('n', 'N')"
			 }
		 }
		);
  $bc->flush;

# NOREVIEW (the old 'U' status concepts)
  $bc = Bin->new({dbh=>$dbh, bin=>'noreview', type=>'ME',
		 object=>{
			  Type=>'ME',
			  Rank=>++$rank,
			  Predefined=>'Y',
			  Description=>'Concepts that will be released without re-review (U status)',
			  GeneratorType=>'SELECT',
			  Generator=>"SELECT DISTINCT concept_id FROM classes WHERE status='U' AND tobereleased in ('y', 'Y') AND concept_id NOT IN (SELECT concept_id FROM classes WHERE status!='U' AND tobereleased IN ('y','Y'))"
			 }
		 }
		);
  $bc->flush;

# REVIEWED
  $bc = Bin->new({dbh=>$dbh, bin=>'reviewed', type=>'ME',
		 object=>{
			  Type=>'ME',
			  Rank=>++$rank,
			  Predefined=>'Y',
			  Description=>'Concepts already reviewed',
			  GeneratorType=>'SELECT',
			  Generator=>"SELECT DISTINCT concept_id FROM concept_status WHERE status='R'"
			 }
		 }
		);
  $bc->flush;

# leftovers
  $bc = Bin->new({dbh=>$dbh, bin=>'leftovers', type=>'ME',
		 object=>{
			  Type=>'ME',
			  Rank=>$Bin::MAXRANK,
			  Editable=>'Y',
			  Predefined=>'Y',
			  Description=>'Unclassified, leftover Concepts',
			  GeneratorType=>"",
			  Generator=>""
			 }
		 }
		);
  $bc->flush;
  return;
}

# Returns bin names given optional constraints on the DB columns
sub getBinNames {
  my($self, $dbh, $constraints) = @_;
  my($where);

  if ($constraints) {
    foreach (@{ $constraints }) {
      $where .= ($where ? " and" : " where");
      $where .= " $_";
    }
  }
  return $dbh->selectAllAsArray("select distinct bin from " . $Bin::CONFIGTABLE . $where);
}

# Reads a bin's data from the database
sub readFromDB {
  my($self, $dbh, $bin) = @_;
  my($object) = {};
  my($xml, $r);

  if (ref($dbh) ne "OracleIF") {
    my($p) = $dbh;
    $dbh = $p->{dbh};
    $bin = $p->{bin};
  }

# create config table if needed
  Bin->createTable($dbh);
  $dbh->setAttributes({ LongReadLen=>20000 });

  $r = $dbh->selectFirstAsRef("select bin,active,type,timestamp,xmlclob from $Bin::CONFIGTABLE where bin=" . $dbh->quote($bin));
  $object = &xmlutils::fromXML($r->[4], $XMLROOTNAME) if $r->[4];
  $dbh->setAttributes({ LongReadLen=>0 });
  return new Bin({dbh=>$dbh, bin=>$bin, active=>$r->[1], type=>$r->[2], timestamp=>$r->[3], object=>$object});
}

# Returns the reference for one or more bins in an array
# constraints refer to the table columns
# properties refers to the XML clob (ALL properties have to be satisfied)
sub getBins {
  my($self, $dbh, $constraints, $properties) = @_;
  my($bin);
  my($b, @b);
  my($p, $f);

  foreach $bin ($self->getBinNames($dbh, $constraints)) {
    $b = Bin->readFromDB($dbh, $bin);

    if ($properties) {
      $f = 0;
      foreach $p (@{ $properties }) {
	next if eval $p;
	$f = 1;
	last;
      }
      next if $f;
    }

    push @b, $b;
  }
  return @b;
}

# returns the references to ME bins in rank order
sub getMEBins {
  my($self, $dbh) = @_;
  my(@me);

  foreach $b (Bin->getBins($dbh, [ "active='Y'" , "type='ME'" ])) {
    push @me, $b;
  }
  return sort { $a->get('Rank') <=> $b->get('Rank') } @me;
}

# Returns the names of all ME bins in rank order
sub getMEBinNames {
  my($self, $dbh) = @_;
  return map { $_->{bin} } $self->getMEBins($dbh);
}

# Adjusts the value of the bin ranks, preserving the order
sub reRank {
  my($self, $dbh, $bins) = @_;
  my($bin);

  if (ref($bins) && ref($bins) eq "ARRAY") {
    my($max, $delta);
    $max = $Bin::MAXRANK;
    $delta = int ($max/(scalar @{ $bins }));
    $rank = 1;
    foreach $bin ( sort { $a->get('Rank') <=> $b->get('Rank') } @{ $bins } ) {
      $bin->set('Rank', $rank);
      $rank += $delta;
    }
  } elsif (ref($bins) && ref($bins) eq "HASH") {
    my(@refs) = keys %{ $bins };
    $self->reRank($dbh, \@refs);
  }
  return;
}

sub flushAll {
  my($self, $dbh, $bins) = @_;
  my($bin);

  if (ref($bins) && ref($bins) eq "ARRAY") {
    foreach $bin (@{ $bins }) {
      $bin->flush;
    }
  } elsif (ref($bins) && ref($bins) eq "HASH") {
    my(@refs) = keys %{ $bins };
    $self->flushAll($dbh, \@refs);
  }
}

# Partitions ME bins
sub partition {
  my($self, $params) = @_;
  return $self->partitionParallel($params);
}

# partitions the ME bins in parallel
sub partitionParallel {
  my($self, $params) = @_;
  my($dbh) = $params->{dbh};
  my($binref);
  my(@mebinrefs);
  my($parallel);
  my(@cmd);
  my(@status);
  my($maxtab, $who);
  my($starttime) = time;

  return 0 unless $dbh;
  return $self->partitionSerial($params) if $params->{cpu}==1;

  $who = ($params->{emsuser} ||
	  GeneralUtils->ip2name($ENV{'REMOTE_HOST'}) ||
	  GeneralUtils->nodename ||
	  GeneralUtils->username ||
	  'unknown');

# already running?
  $maxtab = EMSMaxtab->readFromDB($dbh, $Bin::PARTITION_KEY);
  if (!$params->{force} && $maxtab->get('Status') eq "RUNNING") {
    return 0;
  }

  $maxtab->set({User=>$who, StartTime=>time, StartDate=>$dbh->currentDate, Status=>'RUNNING'} );
  $maxtab->flush;

# needed to keep from going stale across hidden forks
  $dbh->setInactiveDestroy;

  @mebinrefs = $self->getMEBins($dbh);
  foreach $binref (@mebinrefs) {
#    $binref->dupDbRef;
#    $binref->{dbh}->setInactiveDestroy;
    $binref->set('ContentTable', "tmp_me_" . $binref->{bin});
    $dbh->dropTable($binref->get('ContentTable'));
  }

# generate bin contents in parallel to content tables
  $parallel = new ParallelExec({ degree=>($params->{cpu} || $Bin::NUMCPU)});
  foreach $binref (@mebinrefs) {
    push @cmd, sub { $self->generate($dbh, {binref=>$binref, parallel=>1}) };
  }
  @status = $parallel->run(\@cmd);

# read back bin info from DB
  $dbh->setInactiveDestroy;
  @mebinrefs = $self->getMEBins($dbh);

#  --- serial section ----
  $dbh->dropTable($Bin::MEBINS);
  $dbh->createTable($Bin::MEBINS, ['concept_id', {bin_name=>'VARCHAR2(32)'}]);
  $dbh->executeStmt(<<"EOD", {debug=>1});
insert into $Bin::MEBINS (concept_id, bin_name)
SELECT concept_id, \'leftovers\' FROM concept_status
EOD

# Update MEBINS
  my($b, $t);
  foreach $binref (@mebinrefs) {
    next if $binref->get('Bin') eq "leftovers";

    $t = $binref->get('ContentTable');
    $b = $binref->{dbh}->quote($binref->get('Bin'));
    $sql = <<"EOD";
UPDATE $Bin::MEBINS SET bin_name=$b
WHERE  bin_name=\'leftovers\'
AND    concept_id IN (SELECT DISTINCT concept_id FROM $t)
EOD
    $binref->{dbh}->executeStmt($sql);
  }

# get counts in parallel
  @cmd = ();
  foreach $binref (@mebinrefs) {
    push @cmd, sub { $binref->count({inputtable=>$binref->get('ContentTable'), parallel=>1}) };
  }

  @status = $parallel->run(\@cmd);

  $dbh->setInactiveDestroy;

# create indexes
  $dbh->createIndex($Bin::MEBINS, 'concept_id');
  $dbh->createIndex($Bin::MEBINS, 'bin_name');

# read back bin info from DB
#  @mebinrefs = Bin->getMEBins($dbh);
#  $self->flushAll($dbh, \@mebinrefs);

  foreach $binref (@mebinrefs) {
#    print EMSUtils->dumper($binref, \*STDERR);
    $dbh->dropTable($binref->get('ContentTable'));
  }
  $maxtab->set({Status=>'DONE', EndTime=>time, EndDate=>$dbh->currentDate });
  $maxtab->flush;

  return 1;
}

# partitions the ME bins serially
sub partitionSerial {
  my($self, $params) = @_;
  my($dbh) = $params->{dbh};
  my($binref);
  my(@mebinrefs);
  my($maxtab, $who);
  my($starttime)=time;
  my($tmptable);

  return 0 unless $dbh;

  $who = ($params->{emsuser} ||
	  GeneralUtils->ip2name($ENV{'REMOTE_HOST'}) ||
	  GeneralUtils->nodename ||
	  GeneralUtils->username ||
	  'unknown');

# already running?
  $maxtab = EMSMaxtab->readFromDB($dbh, $Bin::PARTITION_KEY);
  if (!$params->{force} && $maxtab->get('Status') eq "RUNNING") {
    return 0;
  }

  $maxtab->set({User=>$who, StartTime=>time, StartDate=>$dbh->currentDate, Status=>'RUNNING'} );
  $maxtab->flush;

  $dbh->dropTable($Bin::MEBINS);
  $dbh->createTable($Bin::MEBINS, ['concept_id', {bin_name=>'VARCHAR2(32)'}]);
  $dbh->executeStmt("insert into " . $Bin::MEBINS . "(concept_id, bin_name) SELECT concept_id, 'leftovers' FROM concept_status");

  @mebinrefs = $self->getMEBins($dbh);
  my($b);

  foreach $binref (@mebinrefs) {
    next if $binref->get('Bin') eq "leftovers";
    $tmptable = "tmp_me_" . $binref->{bin};
    $dbh->dropTable($tmptable);
    $binref->set('ContentTable', $tmptable);
    $binref->generate;
    $binref->count({inputtable=>$tmptable});

    $b = $binref->{dbh}->quote($binref->get('Bin'));
    $dbh->executeStmt(<<"EOD");
UPDATE $Bin::MEBINS SET bin_name=$b
WHERE  bin_name=\'leftovers\'
AND    concept_id IN (SELECT DISTINCT concept_id FROM $tmptable)
EOD
    $dbh->dropTable($tmptable);
  }

# count leftovers
  foreach $binref (@mebinrefs) {
    next unless $binref->get('Bin') eq "leftovers";
    $binref->count;
  }

  $dbh->createIndex($Bin::MEBINS, 'concept_id');
  $dbh->createIndex($Bin::MEBINS, 'bin_name');

  $self->flushAll($dbh, \@mebinrefs);

  $maxtab->set({Status=>'DONE', EndTime=>time, EndDate=>$dbh->currentDate });
  $maxtab->flush;

  return 1;
}

# disconnects all active database connections
sub disconnectAll {
  my($self, $bins) = @_;
  my($br);

  if (ref($bins) eq "ARRAY") {
    foreach $br (@{ $bins }) {
      $br->disconnect;
    }
  } elsif (ref($bins) eq "HASH") {
    foreach $br (keys %{ $bins }) {
      $br->disconnect;
    }
  } elsif (!ref($bins)) {
    $bins->disconnect;
  }
}

#------------------------------------------------------------
# INSTANCE METHODS

# returns the value of a slot for the object
# Can be called as a class or instance method
# e.g., $bin->get('Description');
#       Bin->get({dbh=>$dbh, bin=>'demotions', slot=>'ConceptCount'});
sub get {
  my($self, $slot) = @_;

  if (ref($self)) {
    return $self->{object}->{$slot};
  } else {
    # class method
    my($params) = $slot;
    my($m) = $self->readFromDB($params->{dbh}, $params->{bin});
    return $m->get($params->{slot});
  }
}

# sets the value of a slot
# If called as a class method, it sets all the parameters
# e.g., $request->set('WorklistName', 'chk_foo');
#       $request->set({WorklistBase=>'wrk04b_missyn_ch', WorklistStartNum=>20, WorklistEndNum=>22});
#       ReportRequest->set({dbh=>$dbh, report_id=>23, object=>{WorklistName=>'chk_suresh', Database=>'oa_mid2004'}});
sub set {
  my($self, $slot, $value, $param) = @_;

  if (ref($self)) {
    if (ref($slot) eq "HASH") {
      my($s, $p);
      $p = $value;
      foreach $s (keys %{ $slot }) {
	$self->set($s, $slot->{$s}, $p);
      }
    } elsif (ref($slot) eq "ARRAY") {
      my($s, $v, $p);
      $p = $value;
      foreach (@{ $slot }) {
	($s, $v) = split /=/, $_;
	$self->set($s, $v, $p);
      }
    } elsif (!$value && $slot =~ /^([^=]+)=([^=]+)$/) {
      $self->set($1, $2);
    } elsif ($slot) {
      if ($param->{noforce}) {
	$self->{object}->{$slot} = $value unless defined $self->{object}->{$slot};
      } else {
	$self->{object}->{$slot} = $value;
      }
    }
  } else {
    my($params) = $slot;
    my($m) = $self->readFromDB($params->{dbh}, $params->{bin});
    $m->set($params->{object});
    $m->flush;
  }
  return;
}

sub flush {
  my($self) = @_;
  $self->writeToDB;
}

# Writes a bin's data to the database
sub writeToDB {
  my($self) = @_;
  my($dbh) = $self->{dbh};

  my($xml, $sql);
  my($qb, $qa, $qt, $qy, $qx);

  $self->createTable($dbh);
  $self->defaults;

  $xml = &xmlutils::toXML($self->{object}, $XMLROOTNAME);
  $qx = $dbh->quote($xml);
  $qb = $dbh->quote($self->{bin});
  $qa = $dbh->quote($self->{active});
  $qy = $dbh->quote($self->{type});
  $qt = $dbh->quote($self->{timestamp});

  if (Bin->recordExists($dbh, $self->{bin})) {
    $sql = "update $Bin::CONFIGTABLE set active=$qa,type=$qy,timestamp=$qt,xmlclob=$qx where bin=$qb";
  } else {
    $sql = "insert into $Bin::CONFIGTABLE(bin,active,type,timestamp,xmlclob) values ($qb,$qa,$qy,$qt,$qx)";
  }
  $dbh->executeStmt($sql);
}

# deletes this bin from the database
sub remove {
  my($self) = @_;
  my($sql) = "delete from $Bin::CONFIGTABLE where bin=" . $self->{dbh}->quote($self->{bin});

  eval {
    $self->{dbh}->executeStmt($sql);
  };
  die $@ if $@;
}

# PRIVATE METHODS

# fills in the default values for slots
sub defaults {
  my($self) = @_;
  my(%x) = (
	    Bin=>$self->{bin},
	    CanonicalName=>$self->{bin},
	    Type=>'QA',
	    GroupName=>'default',
	    Description=>'Description for bin: ' . $self->{bin},
	    Editable=>'N',
	    Predefined=>'N',
	    Owner=>'EMS',
	    Hat=>'MTH',
	    Rank=>1,
	    IdType=>'concept_id',
	    Contents=>'mixed',
	    ClusterChemAlgo=>'all',
	    OrderBeforeCount=>'N',
	    ChecklistOnly=>'N',
	    CreatedOn=>$self->{dbh}->currentDate,
	    LastModifiedOn=>$self->{dbh}->currentDate,
	    NumClusters=>-1,
	    EditableClusters=>-1,
	    ChemClusters=>-1,
	    EditableChemClusters=>-1,
	    NonchemClusters=>-1,
	    EditableNonchemClusters=>-1,
	    GenerationTime=>-1,
	    CountTime=>-1,
	   );

  $self->set(\%x, { noforce=>1 });

  if ($self->get('IdType') eq "concept_id") {
    $self->set('BinToWorklistFilters', ['embryo', 'beingedited', 'nonexistent']);
  } else {
    $self->set('BinToWorklistFilters', []);
  }
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

# BIN METHODS

# generates the contents of a bin into a table.  The GeneratorType slot
# specifies the type of generator used.  The stats for the generation
# are stored back in Bin configuration.

# param are passed as a hash ref
# outputtable=>

# Here are the conventions
# methods that generate to tables must at least generate an id column
sub generate {
  my($self, $param) = @_;
  my($dbh) = $self->{dbh};
  my($generatorType) = $self->get('GeneratorType');
  my($starttime) = time;
  my($outputtable) = $self->get('ContentTable');

  $dbh->dropTable($outputtable);

  if ($generatorType eq "SELECT") {

    my($sql) = $self->get('Generator');
    my($stmt) = "create table $outputtable AS $sql";

    $dbh->executeStmt($stmt);

  } elsif ($generatorType eq "SOURCECONCEPTS") {

    my(@sources, $rsab, $vsab, $sql, $cols);

# sources can be versioned, versionless
# if versionless, map to versioned
    foreach $rsab (split /[,\s]+/, $self->get('Generator')) {
      $vsab = $dbh->selectFirstAsScalar("select distinct current_name from source_version where source=\'$rsab\'");
      push @sources, ($vsab || $rsab);
    }
    $sources = (@sources > 1 ? join(',', map { $dbh->quote($_) } @sources) : $dbh->quote($sources[0]));

    $idtype = $self->get('IdType');
    die "Cannot generate SOURCECONCEPTS content for this IdType: $idtype" unless grep { $_ eq $idtype } qw(concept_id atom_id);

    if (@sources > 1) {
      $sql = <<"EOD";
CREATE TABLE $outputtable AS
SELECT DISTINCT $idtype from classes
WHERE  source IN ($sources)
EOD
    } else {
      $sql = <<"EOD";
CREATE TABLE $outputtable AS
SELECT DISTINCT $idtype from classes
WHERE  source=$sources
EOD
    }
    $dbh->executeStmt($sql);

  } elsif ($generatorType eq "TERMGROUPCONCEPTS") {

    my($termgroups) = $self->get('Generator');
    my($idtype) = $self->get('IdType');
    my(@termgroups);
    my($rsab, $vsab, $tty);

    foreach (split /[,\s]+/, $termgroups) {
      ($rsab, $tty) = split /\//, $_;
      $vsab = $dbh->selectFirstAsScalar("select distinct current_name from source_version where source=\'$rsab\'");
      push @termgroups, ($vsab || $rsab) . "/$tty";
    }

    if (@termgroups > 1) {
      my($t) = join(',', map { $dbh->quote($_) } @termgroups);
      $dbh->executeStmt(<<"EOD");
CREATE TABLE $outputtable AS
SELECT DISTINCT $idtype from classes
WHERE  termgroup IN ($t)
EOD
    } else {
      my($t) = $dbh->quote($termgroups[0]);
      $dbh->executeStmt(<<"EOD");
CREATE TABLE $outputtable AS
SELECT DISTINCT $idtype from classes
WHERE  termgroup=$t
EOD
    }

  } elsif ($generatorType eq "TABLE") {

    my($sql) = "CREATE TABLE $outputtable AS SELECT * from " . $self->get('Generator');
    $dbh->executeStmt($sql);

  } elsif ($generatorType eq "MEME_INTEGRITY_PROC" || $generatorType eq "PLSQL") {
    my($proc) = $self->get('Generator');
    my($t);
    my($sql) = <<"EOD";
BEGIN
  :t := $proc;
END;
EOD
    my($sth) = $dbh->{dbh}->prepare($sql);
    $sth->bind_param_inout(":t", \$t, 64);
    die ($@ || $DBI::errstr) if ($@ || $DBI::errstr);
    $sth->execute;
    die ($@ || $DBI::errstr) if ($@ || $DBI::errstr);

    my($idtype) = $self->get('IdType');
    $dbh->executeStmt("CREATE TABLE $outputtable AS SELECT DISTINCT $idtype from $t");
    $dbh->dropTable($t);

  } elsif ($generatorType eq "FILE") {

    my($file) = $self->get('Generator');
    my($tmpfile) = EMSUtils::getTempFile("gen");
    open(A, $file) || die "Cannot open $file";
    open(B, ">$tmpfile") || die "Cannot open $tmpfile";
    while (<A>) {
      chomp;
      @f = split /\|/, $_;
      if (@f == 1) {
	print B join('|', $f[0], ++$cluster_id, 'concept_id', 1), "\n";
      } elsif (@f == 2) {
	print B join('|', $f[0], $f[1], 'concept_id', 1), "\n";
      } elsif (@f == 3) {
	print B join('|', $f[0], $f[1], $f[2], 1), "\n";
      } else {
	print B join('|', @f), "\n";
      }
    }
    close(B);
    close(A);
    $dbh->file2table($outputtable, $tmpfile, ['id', 'cluster_id', {idtype=>'VARCHAR(24)'}, 'intracluster_id']);
    unlink $tmpfile;

  } elsif ($generatorType eq "SCRIPT") {

    my($script) = $self->{config}->getProperty($bin, 'Generator');
    my($additionalparams) = "-d " . $dbh->{db};
    my($tmpfile) = EMSUtils::getTempFile("gen");
    open(A, "$script $additionalparams|") || die "Cannot open $script";
    open(B, ">$tmpfile") || die "Cannot open $tmpfile";
    while (<A>) {
      chomp;
      @f = split /\|/, $_;
      if (@f == 1) {
	print B join('|', $f[0], ++$cluster_id, 'concept_id', 1), "\n";
      } elsif (@f == 2) {
	print B join('|', $f[0], $f[1], 'concept_id', 1), "\n";
      } elsif (@f == 3) {
	print B join('|', $f[0], $f[1], $f[2], 1), "\n";
      } else {
	print B join('|', @f), "\n";
      }
    }
    close(B);
    close(A);
    $dbh->file2table($outputtable, $tmpfile, ['id', 'cluster_id', {idtype=>'VARCHAR(24)'}, 'intracluster_id']);
    unlink $tmpfile;
    
  } elsif ($generatorType eq "TERMGROUPCONCEPTS") {

    my($termgroups) = $self->getProperty($bin, 'Generator');
    my($t) = join(', ', map { "\"" . $_ . "\'" } split /[,\s]+/, $termgroups);
    $dbh->executeStmt(<<"EOD");
CREATE TABLE $outputtable AS
SELECT DISTINCT concept_id from classes
WHERE  termgroup IN ($t)
EOD
  } else {
  }

  $self->set({
	      GenerationTime=>time-$starttime,
	      GenerationStatus=>$@,
	      GenerationDate=>$dbh->currentDate,
	     }
	    );

#  print STDERR "Generated ($dbh, $dbh->{dbh})", $outputtable, " in: ", GeneralUtils->sec2hms(time-$starttime), "\n";
  if ($param->{parallel}) {
#    $dbh->disconnect;
    exit 0;
  }
  return;
}

# orders the concepts given the ordered and unordered tables
sub order {
  my($self, $unordered, $ordered) = @_;
  my($dbh) = $self->{dbh};
  my($ordererType) = $self->get('OrdererType');
  my($orderer) = $self->get('Orderer');
  my($starttime) = time;

  $dbh->dropTable($ordered);

  if ($ordererType eq "PERL") {
    eval {
      require $EMSSCRIPTDIR . "/" . $orderer;
      &order($self, $ordered);
    }
  } elsif ($ordererType eq "SOURCEORDER") {
    my($source) = $orderer;
  } elsif ($ordererType eq "ID") {
    $dbh->executeStmt(<<"EOD");
create table $ordered as select * from $unordered order by id
EOD
  } elsif ($ordererType eq "ALPHABETICAL") {
  } elsif ($ordererType eq "STY") {
  } else {
    $dbh->executeStmt(<<"EOD");
create table $ordered as select * from $unordered order by cluster_id
EOD
  }
}

# takes an input table of id|cluster_id and removes rows by applying
# filters in order.
# Currently recognized filters are:
# embryo - removes concepts in status E
# beingedited - removes concepts that are currently being edited
# nonexistent - removes concepts that do not currently exist

sub filter {
  my($self, $unfiltered, $filtered) = @_;
  my($tmpf1) = EMSUtils->tempTable($self->{dbh}, "tmpfilter1");
  my($tmpf2) = EMSUtils->tempTable($self->{dbh}, "tmpfilter2");
  my($dbh) = $self->{dbh};

  my($f);

  $dbh->dropTables([$filtered, $tmpf1, $tmpf2]);
  $dbh->copyTable($unfiltered, $tmpf1);

  foreach $f (@{ $self->get('BinToWorklistFilters') }) {
    $dbh->dropTable($tmpf2);
    $dbh->createIndex($tmpf1, 'id');

    if ($f eq "embryo") {

      if ($self->get('IdType') eq 'concept_id') {
	$dbh->executeStmt(<<"EOD");
create table $tmpf2 as
select a.* from $tmpf1 a, concept_status b
where  a.id=b.concept_id
and    b.status='E'
EOD
      } else {
        next;
      }

    } elsif ($f eq "nonexistent") {

      if ($self->get('IdType') eq 'concept_id') {
	$dbh->executeStmt(<<"EOD");
create table $tmpf2 as
select a.* from $tmpf1 a, concept_status b
where  a.id=b.concept_id
EOD
      }

    } elsif ($f eq "beingedited") {
      $dbh->executeStmt(<<"EOD");
create table $tmpf2 as
select a.* from $tmpf1 a
where  not exists (
		   select * from $Bin::CONCEPTS_BEING_EDITED b
		   where  a.id=b.concept_id
		  )
EOD
    }
    $dbh->copyTable($tmpf2, $tmpf1);
  }
  $dbh->copyTable($tmpf1, $filtered);
  $dbh->dropTables([$tmpf1, $tmpf2]);
}

# generates the counts for this bin
# params are passed in the reference
sub count {
  my($self, $param) = @_;
  my($bin) = $self->get('Bin');
  my($starttime) = time;
  my($dbh) = $self->{dbh};
  my($inputtable) = $param->{inputtable};

  if ($self->get('Type') eq "ME") {

    unless ($inputtable && $dbh->tableExists($inputtable)) {
      $inputtable = EMSUtils->tempTable($dbh, "TMP_" . $bin);
      $dbh->dropTable($inputtable);
      $dbh->executeStmt("create table $inputtable as select concept_id from $Bin::MEBINS where bin_name=" . $dbh->quote($bin));
      $dbh->createIndex($inputtable, "concept_id");
    }

    if ($self->get('OrderBeforeCount' eq "Y")) {
    } else {
      my($c);

      $self->set('NumClusters', ($dbh->selectFirstAsScalar("select count(concept_id) from $inputtable") || 0));

      if (lc($self->get('Contents')) eq lc("mixed")) {
	my($chemtable) = EMSUtils->tempTable($dbh, "TMPC_" . $bin);
	my($nonchemtable) = EMSUtils->tempTable($dbh, "TMPN_" . $bin);

	if ($dbh->tableExists($inputtable)) {
	  EMSUtils->chemSplit($dbh, {full=>$inputtable, chem=>$chemtable});

	  $c = $dbh->selectFirstAsScalar("select count(distinct concept_id) from $chemtable");
	  $self->set('ChemClusters',  $c);
	  $c = $self->get('NumClusters') - $self->get('ChemClusters');
	  $self->set('NonchemClusters', $c);
	} else {
	  $self->set({ChemClusters=>0, NonchemClusters=>0});
	}

	$dbh->dropTables([$chemtable, $nonchemtable]);
	$dbh->dropTable($inputtable) unless $param->{inputtable};

      } elsif (lc($self->get('Contents')) eq lc("chem")) {

	$self->set('ChemClusters', $self->get('NumClusters'));
	$self->set('NonchemClusters', 0);

      } elsif (lc($self->get('Contents')) eq lc("nonchem")) {

	$self->set('NonchemClusters', $self->get('NumClusters'));
	$self->set('ChemClusters', 0);
      }
    }
  }
  $self->set('CountTime', time-$starttime);
#  print STDERR "Counted ", $self->{bin}, " in: ", GeneralUtils->sec2hms(time-$starttime), "\n";
  $self->flush;
}

# makes a worklist or a checklist
sub bin2list {

# randomize bin contents?
}

# refreshes the counts for a bin
sub refresh {
}

sub makeWorklist {
}

#----------------------------------------
1;
