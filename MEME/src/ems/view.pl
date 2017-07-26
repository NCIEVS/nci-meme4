# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000

# Displays the contents of a worklist/checklist
# CGI params:
# db=
# list=
# worklist=
# checklist=
sub do_view {
  my($list) = $query->param('list') || $query->param('worklist') || $query->param('checklist');
  my($sql);
  my($html);
  my($parent_bin, $bininfo, $binconfig, $listinfo);
  my($n)=0;
  my($description);
  my(@d, $r, %rowspan, %cluster, %name, $cluster_id, $concept_id);
  my($row, @rows, $url);
  my($CONFIGTABLE);

  $listinfo = WMSUtils->getListinfo($dbh, $list);
  $parent_bin = WMSUtils->getParentBin($dbh, $list);
  if ($parent_bin) {
    $bininfo = EMSUtils->getBininfo($dbh, $parent_bin);
    $CONFIGTABLE = ($bininfo->{bin_type} eq "QA" ? $EMSNames::QACONFIGTABLE :
		    $bininfo->{bin_type} eq "AH" ? $EMSNames::QACONFIGTABLE : $EMSNames::MECONFIGTABLE);
    $binconfig = EMSUtils->getBinconfig($dbh, $CONFIGTABLE, $parent_bin);
  }

  if (WMSUtils->isChecklist($list)) {
    push @d, ["Checklist name: ", $list];
    if ($parent_bin) {
      push @d, ["Parent bin: ", $parent_bin];
      push @d, ["Bin type: ", $bininfo->{bin_type}];
      push @d, ["Description: ", ($binconfig ? $binconfig->{description} : "")];
    } else {
      push @d, ["Parent bin: ", "none"];
    }
    push @d, ["Create date: ", $listinfo->{create_date}];
    push @d, ["Owner: ", $listinfo->{owner}];

  } else {

    push @d, ["Worklist name: ", $list];
    push @d, ["Parent bin: ", $parent_bin];
    push @d, ["Bin type: ", $bininfo->{bin_type}];
    push @d, ["Description: ", ($binconfig ? $binconfig->{description} : "")];
    push @d, ["Create date: ", $listinfo->{create_date}];
    push @d, ["Created by: ", $listinfo->{created_by}];
  }
  $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>5, -cellspacing=>1}, \@d);
  $html .= $query->p;
  $html .= $query->hr;
  $html .= "The following table shows the current concept membership of the atoms in the checklist or worklist.";
  $html .= "Follow the concept_id link to see the full report for the concept.";
  $html .= $query->p;

# collate the clusters by current atom memberships
  $sql = <<"EOD";
select cluster_id, count(distinct a.concept_id) from classes a, $list b
where  a.atom_id=b.atom_id
group by cluster_id
EOD
  foreach $r ($dbh->selectAllAsRef($sql)) {
    $rowspan{$r->[0]} = $r->[1];
  }

  $sql = <<"EOD";
select distinct b.cluster_id, a.concept_id from classes a, $list b
where  a.atom_id=b.atom_id
order by cluster_id, concept_id
EOD

  foreach $r ($dbh->selectAllAsRef($sql)) {
    $cluster_id = $r->[0];
    $concept_id = $r->[1];
    push @{ $cluster{$cluster_id} }, $concept_id;
    $name{$concept_id} = MIDUtils->conceptPreferredName($dbh, $concept_id);
  }

  push @rows, $query->th(['', 'Cluster ID', 'Concepts']);
  foreach $cluster_id (sort { $a <=> $b } keys %cluster) {
    $row = "";
    $n++;

    $row .= $query->td({-rowspan=>$rowspan{$cluster_id}, -align=>'right'}, $n) . "\n";
    $row .= $query->td({-rowspan=>$rowspan{$cluster_id}, -align=>'right'}, $cluster_id) . "\n";

    foreach $concept_id (@{ $cluster{$cluster_id} }) {
      $url = $query->a({href=>$main::EMSCONFIG{MIDCONCEPTREPORTURL} . "?action=search&subaction=concept_id&arg=$concept_id&$DBget" . "#report"}, $concept_id);
      $row .= $query->td($name{$concept_id} . " (" . $url . ")") . "\n";
      push @rows, $row;
      $row = "";
    }
  }

  $html .= $query->table({border=>1, cellpadding=>5, cellspacing=>0}, $query->Tr(\@rows));
  &printhtml({title=>"WMS: Contents of $list", h1=>"Contents of $list", body=>$html});
  return;
}
1;
