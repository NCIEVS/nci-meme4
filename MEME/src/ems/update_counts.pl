# updates the counts for a bin
# suresh@nlm.nih.gov 2/00
# suresh@nlm.nih.gov 2/06 - Mods for EMS3

# CGI params
# bin_name=

# updates the counts for a bin
sub do_update_counts {
  my($html);
  my($bin_name) = $query->param('bin_name');
  my($bininfo, $binconfig);
  my($sql);

  &printhtml({printandexit=>1, body=>"Need a bin name to update counts"}) if !$bin_name;
  $bininfo = EMSUtils->getBininfo($dbh, $bin_name);
  &printhtml({printandexit=>1, body=>"No information found for bin: $bin_name"}) if !$bininfo;

  if ($bininfo->{bin_type} eq "ME") {
    $configtable = $EMSNames::MECONFIGTABLE;
    $newaction = "me_bins";
  } elsif ($bininfo->{bin_type} eq "QA") {
    $configtable = $EMSNames::QACONFIGTABLE;
    $newaction="qa_bins";
  } elsif ($bininfo->{bin_type} eq "AH") {
    $configtable = $EMSNames::AHCONFIGTABLE;
    $newaction="ah_bins";
  } else {
    &printhtml({printandexit=>1, body=>"Unknown bin_type for $bin_name: " . $bininfo->{bin_type}});
  }

  $binconfig = EMSUtils->getBinconfig($dbh, $configtable, $bin_name);
  &printhtml({printandexit=>1, body=>"No config information found for bin: $bin_name"}) if !$binconfig;

  if ($bin_name eq "demotions") {
    my($tmptable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_COUNTDEM");
    EMSUtils->clusterizeDemotions($dbh, $tmptable);
    $dbh->createIndex($tmptable, 'concept_id', EMSUtils->tempIndex($dbh));
    $counts = EMSUtils->getDemotionsCounts($dbh, $tmptable);
    $dbh->dropTable($tmptable);
  } else {
    $counts = EMSUtils->getBinCounts($dbh, $binconfig, $bininfo);
  }

  my(%bi);
  $bi{bin_name} = $bininfo->{bin_name};
  $bi{bin_type} = $bininfo->{bin_type};
  foreach (keys %$counts) {
    $bi{$_} = $counts->{$_};
  }
  EMSUtils->updateBininfo($dbh, \%bi);

  &printhtml({redirect=>$query->url() . "?$DBget&action=$newaction"});
  return;
}
1;
