# ME bins related functionality
# suresh@nlm.nih.gov 8/2002

package ME;

use lib "/site/umls/lib/perl";

use ParallelExec;

# initializers
# bins=<Bins handle>
# MEBINS=<new ME_BINS table name>
# CPU=<number of available CPUs for parallelizing partitioning>
# NAPTIME=<seconds between polls for partitioning>

$MEBINS="ME_BINS2";
$NUMCPU=2;

# partitions the ME bins (parallel)
sub partition {
  my($self, $dbh, $cpu) = @_;
  my($binref);
  my(@mebinrefs) = Bin->getMEBinRefs($dbh);

# needed to handle keep from going stale across a fork
  $dbh->setInactiveDestroy;

  foreach $binref (@mebinrefs) {
    $binref->{tmptable} = "tmp_" . $binref->{bin} . "_" . $$;
    $dbh->dropTable($binref->{tmptable});
  }

# generate bin contents in parallel
  $parallel = new ParallelExec($cpu || $self::NUMCPU);
  foreach $binref (@mebinrefs) {
    push @cmd, $binref->generate($binref->{tmptable});
  }
  $parallel->run(\@cmd);

#  --- serial section ----
  $dbh->dropTable($self->MEBINS);
  $dbh->createTable($self->MEBINS, ['concept_id', {bin_name=>'VARCHAR2(32)'}]);
  $dbh->executeStmt("insert into " . $self::MEBINS . "(concept_id, bin_name) SELECT concept_id, 'leftovers' FROM concept_status");

  foreach $binref (@mebinrefs) {
    $dbh->executeStmt(<<"EOD");
UPDATE $self::MEBINS SET bin_name=\'$binref->{bin}\'
WHERE  bin_name=\'leftovers\'
AND    concept_id IN (SELECT DISTINCT concept_id FROM $binref->{tmptable})
EOD
    $dbh->dropTable($binref->{tmptable});
  }

# create indexes in parallel
  @cmd = ();
  push @cmd, $dbh->createIndex($self::MEBINS, 'concept_id');
  push @cmd, $dbh->createIndex($self::MEBINS, 'bin_name');
  $parallel->run(\@cmd);
}

# -----------------------------
1;
