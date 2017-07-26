# EMS related utility functions
# suresh@nlm.nih.gov 8/2002

package EMSUtils;

$CHEMNONCHEM_CLASSIFIER="chemnonchem_classifier";
$HISTORYTABLE="EMS_HISTORY";
$EMSUSERSTABLE="EMSUsers";

use String::CRC;
use Data::Dumper;

# returns the root names of sources in the MID using the source_version table
sub getRSABs {
  my($self, $dbh) = @_;
  return $dbh->selectAllAsArray("select distinct source from source_version");
}

# takes a table with concept ID's and splits the content into two tables,
# one with chemical concepts and the other with non-chemical concepts
sub chemSplit {
  my($self, $dbh, $param) = @_;
  my($full, $chem, $nonchem);
  my($sql);

  $full = $param->{full};
  $chem = $param->{chem};
  $nonchem = $param->{nonchem};

  return unless $full && $dbh->tableExists($full);

  $dbh->dropTables([$chem, $nonchem]);
  $sql = <<"EOD";
create table $chem as
select a.concept_id from $EMSUtils::CHEMNONCHEM_CLASSIFIER a, $full b
where  a.concept_id=b.concept_id
and    a.chemnonchem='C'
EOD
  $dbh->executeStmt($sql) if $chem;

  if ($chem) {
    $dbh->createIndex($chem, 'concept_id');
    $sql = <<"EOD";
create table $nonchem as
select a.concept_id from $full a
where  not exists (select concept_id from $chem b where b.concept_id=a.concept_id)
EOD
  } else {
    $sql = <<"EOD";
create table $nonchem as
select a.concept_id from $EMSUtils::CHEMNONCHEM_CLASSIFIER a, $full b
where  a.concept_id=b.concept_id
and    a.chemnonchem='N'
EOD
  }
  $dbh->executeStmt($sql) if $nonchem;
}

# loads data from a table (id, cluster_id) to the history filter table (used to init EMS 3.0)
sub loadHistoryFromTable {
  my($self, $dbh, $table, $canonicalName, $worklist, $idType) = @_;
  my($keyTable);
  my($sql);

  $self->createHistoryTable($dbh) unless $dbh->tableExists($EMSUtils::HISTORYTABLE);
  $keyTable = $self->makeHistoryKeyTable($dbh, $table);

  $dbh->dropIndex($EMSUtils::HISTORYTABLE);
  $sql = <<"EOD";
insert into $EMSUtils::HISTORYTABLE(cluster_id, key, CanonicalName, WorklistName, IdType)
select cluster_id, key, \'$canonicalName\', \'$worklist\', \'$idType\' from $keyTable
EOD
  $dbh->executeStmt($sql);
  $dbh->createIndex($EMSUtils::HISTORYTABLE, 'key');
  $dbh->dropTable($keyTable);
}

# history filter for concept_id bins - removes clusters from a table that
# have been previously edited for this class of bins (named in canonicalName)
sub historyFilter {
  my($self, $dbh, $canonicalName, $idType, $binTable, $filteredTable) = @_;
  my($keyTable, $editedTable);

  $self->createHistoryTable($dbh) unless $dbh->tableExists($EMSUtils::HISTORYTABLE);
  $keyTable = EMSUtils->makeHistoryKeyTable($dbh, $binTable);
  $dbh->createIndex($keyTable, "key");

# find all clusters edited before
  $editedTable = EMSUtils->tempTable($dbh);
  $sql = <<"EOD";
create table $editedTable as select cluster_id from $keyTable k
where  exists (
	       select * from $EMSUtils::HISTORYTABLE h
	       where  h.key=k.key
	       and    h.CanonicalName=\'$canonicalName\'
	       and    h.IdType=\'$idType\'
	      )
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create $filteredTable as select * from $binTable
where  cluster_id NOT IN (select cluster_id from $editedTable)
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTables([$keyTable, $editedTable]);
}

# makes keys (using cksum) for a table that has id and cluster_id attributes
# returns table name
sub makeHistoryKeyTable {
  my($self, $dbh, $inputTable) = @_;
  my($keyTable) = EMSUtils->tempTable($dbh);
  my($tmpfile, $x, $y, @x);
  my(%ids);

# create keys for clusters in $binTable
  $tmpfile = EMSUtils->tempFile;
  $dbh->selectToFile("select id, cluster_id from $inputTable", $tmpfile);
  open(T, $tmpfile) || die "Cannot open $tmpfile\n";
  while (<T>) {
    chomp;
    @x = split /\|/, $_, 2;
    $ids{$x[1]}->{$x[0]}++;
  }
  close(T);
#  unlink $tmpfile;

  open(T, ">$tmpfile") || die "Cannot write to $tmpfile\n";
  while (($x, $y) = each %ids) {
    $k = crc(join(',', sort { $a <=> $b } keys %{ $y }), 32);
    print T join('|', $x, $k), "\n";
  }
  close(T);
  $dbh->file2table($keyTable, $tmpfile, ['cluster_id', 'key']);
  unlink $tmpfile;
  return $keyTable;
}

sub createHistoryTable {
  my($self, $dbh) = @_;
  my($colspec) = [{CanonicalName=>'varchar(128)'}, {WorklistName=>'varchar(128)'}, 'cluster_id', {IdType=>'varchar(128)'}, 'key'];
  $dbh->createTable($EMSUtils::HISTORYTABLE, $colspec);
}

# escapes quotes for VARCHAR
# must be passed a reference to a scalar
sub quote {
  my($self, $dbh, $ref) = @_;

  $dbh->quote($$ref) if (ref($ref) eq "SCALAR");
}

sub tmpTable {
  return tempTable(@_);
}

# Makes up a temporary table name
sub tempTable {
  my($self, $dbh, $prefix) = @_;
  my($key) = "TEMPTABLENUM";
  my($maxtab) = EMSMaxtab->readFromDB($dbh, $key);

  $prefix = "EMSTMP" unless $prefix;
  my($new) = $maxtab->get('Next')+1;
  $maxtab->set('Next', $new);
  $maxtab->flush;

# add a random component
  my($r) = sprintf("%.2d%.2d", int(rand 99), $$ % 100);
  my($table) = substr(sprintf("%s_%d_%d", $prefix, $new, $r), 0, 32);
#  return ($dbh->tableExists($table) ? &getTempTable($dbh, $prefix) : $table);
  return $table;
}

# Makes up a temporary file name
sub tempFile {
  my($self, $prefix) = @_;
  my($tmpdir) = $main::tmpdir || "/tmp";
  my($n, $file);

  $prefix = "EMSTMP" unless $prefix;
  $n = $$;
  for (;;) {
    $file = sprintf("%s/%s.%d", $tmpdir, $prefix, $n);
    last unless -e $file;
    if (($n-$$) > 20) {
      unlink $file;
      last;
    } else {
      $n++;
    }
  }
  return $file;
}

# modifies a table to conform to bin structure
# by adding columns with suitable defaults as needed
sub addBinColumns {
  my($dbh, $table) = @_;
  my($sql);

  unless ($dbh->tableHasColumn($table, "idtype")) {
    $sql = <<"EOD";
ALTER TABLE $table ADD (idtype VARCHAR(32) DEFAULT 'concept_id')
EOD
    $dbh->executeStmt($sql);
  }

  unless ($dbh->tableHasColumn($table, "cluster_id")) {
    $sql = <<"EOD";
ALTER TABLE $table ADD (cluster_id INTEGER DEFAULT -5000)
EOD
    $dbh->executeStmt($sql);
    $sql = <<"EOD";
UPDATE $table SET cluster_id=ROWNUM WHERE cluster_id = -5000
EOD
    $dbh->executeStmt($sql);
  }

  unless ($dbh->tableHasColumn($table, "intracluster_id")) {
    $sql = <<"EOD";
ALTER TABLE $table ADD (cluster_id INTEGER DEFAULT 1)
EOD
    $dbh->executeStmt($sql);
  }
}

# adds a cluster_id column to a table and initializes it if needed
sub clusterize {
  my($dbh, $t) = @_;
  my($SQL) = <<"EOD";
BEGIN  EMS_UTILITY.clusterize(table => $t); END;
EOD
  my($sth) = $dbh->prepare($SQL);
  $sth->execute;
}

# aid to debugging
sub dumper {
  my($self, $s, $f) = @_;
  my($file) = "/tmp/emsdebug.$$" unless $f;

  if ($f) {
    print $f Dumper($s), "\n";
    return;
  }

  open(F, ">>$file") || return;
  print F Dumper($s), "\n";
  close(F);
}

# -----------------------------
1;
