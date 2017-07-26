# suresh@nlm.nih.gov 10/98
# suresh@nlm.nih.gov 1/00 - Modified for Oracle
# suresh@nlm.nih.gov 7/05 - Mods for EMS3

# Show the data for currently defined ME bins
sub do_me_bins {
  my($html);
  my($bin, @bins);
  my($binconfig, $bininfo);
  my($MECONFIGTABLE) = $EMSNames::MECONFIGTABLE;
  my($BININFOTABLE) = $EMSNames::BININFOTABLE;
  my($binnum);
  my($x);
  my(@d);

  eval { EMSUtils->meconfig2table($dbh, EMSUtils->configfile($dbh, "ME"), $MECONFIGTABLE) };
  if ($@) {
    $html = $@;
    &printhtml({title=>'EMS: ME Bins', body=>$query->pre($html), printandexit=>1});
  }

  $sql = "select bin_name from $MECONFIGTABLE order by rank";
  @bins = $dbh->selectAllAsArray($sql);

  @d = ();
  $x = EMSMaxtab->get($dbh, $EMSNames::MEPARTITIONINGLOCKKEY);
  if ($x && $x->{valueint}>0) {
    $x = EMSMaxtab->get($dbh, $EMSNames::MEPARTITIONDATEKEY);
    push @d, [$query->font({color=>'red'}, "Partitioning ongoing at: "), $x->{timestamp}] if $x;
  } else {
    $x = EMSMaxtab->get($dbh, $EMSNames::MEPARTITIONDATEKEY);
    push @d, ["Last partitioned on: ", $x->{timestamp}] if ($x);
  }
  $x = EMSMaxtab->get($dbh, $EMSNames::MEPARTITIONUSERKEY);
  push @d, ["Last partitioned by: ", $x->{valuechar}] if ($x);
  $x = EMSMaxtab->get($dbh, $EMSNames::MEPARTITIONTIMEKEY);
  push @d, ["Time to partition: ", GeneralUtils->sec2hms($x->{valueint})] if $x;
  $html .= &toHTMLtable($query, {-border=>0, -cellpadding=>0, -cellspacing=>0}, \@d);
  $html .= $query->p;

# add the table header
  $row = "";
  $row .= $query->th({colspan=>4}, '');
  $row .= $query->th({colspan=>3}, '# Clusters');
  $row .= $query->th({colspan=>2}, '');
  push @rows, $row . "\n";

  $row = "";
  $row .= $query->th(['', "Bin", "Type", "Clusters", "All", "Uneditable", "Editable", "Generated", "Actions"]);
  push @rows, $row . "\n";

  $binnum=0;
  foreach $bin (@bins) {
    $row = "";
    $binnum++;
    $binconfig = EMSUtils->getBinconfig($dbh, $MECONFIGTABLE, $bin);
    $bininfo = EMSUtils->getBininfo($dbh, $bin);
    $rowspan = ($binconfig->{content_type} eq "MIXED" ? 4 : 1);

    $row .= $query->td({-rowspan=>$rowspan, align=>'right'}, $binnum);

    $q = $query->img({-src=>$main::EMSCONFIG{EMSIMGURL} . "/info.gif",
		      -alt=>$binconfig->{description},
		      -title=>$binconfig->{description}});
    $x = ($binconfig->{editable} eq "Y") ? $query->strong($binconfig->{bin_name}) : $binconfig->{bin_name};
    $row .= $query->td({-rowspan=>$rowspan}, $q . " " . $x);
    $row .= $query->td({-rowspan=>$rowspan}, $binconfig->{content_type});

    if ($binconfig->{content_type} eq "MIXED") {
      $row .= $query->td($query->strong("All"));
      $row .= $query->td({-align=>'right'}, $bininfo->{totalClusters});
      $row .= $query->td({-align=>'right'}, $bininfo->{totalUneditableClusters});
      $row .= $query->td({-align=>'right'}, $bininfo->{totalClusters}-$bininfo->{totalUneditableClusters});

      $row .= $query->td({rowspan=>$rowspan, valign=>'top'}, &stats_table($bininfo));
      $row .= $query->td(&action_table($binconfig, $bininfo, "all"));
      $row .= $query->td($query->br);
      push @rows, $row;

      $row = "";
      $row .= $query->td($query->strong("Chem"));
      $row .= $query->td({-align=>'right'}, $bininfo->{chemClusters});
      $row .= $query->td({-align=>'right'}, $bininfo->{chemUneditableClusters});
      $row .= $query->td({-align=>'right'}, $bininfo->{chemClusters}-$bininfo->{chemUneditableClusters});

      $row .= $query->td(&action_table($binconfig, $bininfo, "chem"));
      push @rows, $row;

      $row = "";
      $row .= $query->td($query->strong("Clinical"));
      $row .= $query->td({-align=>'right'}, $bininfo->{clinicalClusters}+0);
      $row .= $query->td({-align=>'right'}, $bininfo->{clinicalUneditableClusters});
      $row .= $query->td({-align=>'right'}, $bininfo->{clinicalClusters}-$bininfo->{clinicalUneditableClusters});

      $row .= $query->td(&action_table($binconfig, $bininfo, "clinical"));
      push @rows, $row;

      $row = "";
      $row .= $query->td($query->strong("Other"));
      $row .= $query->td({-align=>'right'}, $bininfo->{otherClusters}+0);
      $row .= $query->td({-align=>'right'}, $bininfo->{otherUneditableClusters});
      $row .= $query->td({-align=>'right'}, $bininfo->{otherClusters}-$bininfo->{otherUneditableClusters});

      $row .= $query->td(&action_table($binconfig, $bininfo, "other"));
      push @rows, $row;

    } else {

      $row .= $query->td($query->strong("All"));
      $row .= $query->td({-align=>'right'}, $bininfo->{totalClusters});
      if ($binconfig->{editable} eq "Y") {
	$row .= $query->td({-align=>'right'}, $bininfo->{totalUneditableClusters});
	$row .= $query->td({-align=>'right'}, $bininfo->{totalClusters}-$bininfo->{totalUneditableClusters});
      } else {
	$row .= $query->td({align=>'right'}, ["n/a", "n/a"]);
      }

      $row .= $query->td({valign=>'top'}, &stats_table($bininfo));
      $row .= $query->td(&action_table($binconfig, $bininfo, "all"));
      push @rows, $row . "\n";
    }
  }

# tally totals
  $row = "";
  my(%total);
  foreach $bin (@bins) {
    $binconfig = EMSUtils->getBinconfig($dbh, $MECONFIGTABLE, $bin);
    $bininfo = EMSUtils->getBininfo($dbh, $bin);
    next unless $binconfig->{editable} eq "Y";
    $total{all}{all} += $bininfo->{totalClusters};
    $total{all}{editable} += $bininfo->{totalClusters}-$bininfo->{totalUneditableClusters};
    $total{all}{uneditable} += $bininfo->{totalUneditableClusters};

    if ($binconfig->{content_type} eq "MIXED") {
      $total{chem}{all} += $bininfo->{chemClusters};
      $total{chem}{editable} += $bininfo->{chemClusters}-$bininfo->{chemUneditableClusters};
      $total{chem}{uneditable} += $bininfo->{chemUneditableClusters};
      $total{clinical}{all} += $bininfo->{clinicalClusters};
      $total{clinical}{editable} += $bininfo->{clinicalClusters}-$bininfo->{clinicalUneditableClusters};
      $total{clinical}{uneditable} += $bininfo->{clinicalUneditableClusters};
      $total{other}{all} += $bininfo->{otherClusters};
      $total{other}{editable} += $bininfo->{otherClusters}-$bininfo->{otherUneditableClusters};
      $total{other}{uneditable} += $bininfo->{otherUneditableClusters};
    } elsif ($binconfig->{content_type} eq "CHEM") {
      $total{chem}{all} += $bininfo->{chemClusters};
      $total{chem}{editable} += $bininfo->{chemClusters}-$bininfo->{chemUneditableClusters};
      $total{chem}{uneditable} += $bininfo->{chemUneditableClusters};
    } elsif ($binconfig->{content_type} eq "CLINICAL") {
      $total{clinical}{all} += $bininfo->{clinicalClusters};
      $total{clinical}{editable} += $bininfo->{clinicalClusters}-$bininfo->{clinicalUneditableClusters};
      $total{clinical}{uneditable} += $bininfo->{clinicalUneditableClusters};
    } elsif ($binconfig->{content_type} eq "OTHER") {
      $total{other}{all} += $bininfo->{otherClusters};
      $total{other}{editable} += $bininfo->{otherClusters}-$bininfo->{otherUneditableClusters};
      $total{other}{uneditable} += $bininfo->{otherUneditableClusters};
    }
  }
  $row .= $query->td({colspan=>3, rowspan=>4}, "Total cluster count for all editable bins");
  $row .= $query->td("Total");
  $row .= $query->td({align=>'right'}, $total{all}{all});
  $row .= $query->td({align=>'right'}, $total{all}{uneditable});
  $row .= $query->td({align=>'right'}, $total{all}{editable});
  $row .= $query->td({colspan=>2, rowspan=>4}, "");
  push @rows, $row;

  $row = "";
  $row .= $query->td("Chem");
  $row .= $query->td({align=>'right'}, $total{chem}{all});
  $row .= $query->td({align=>'right'}, $total{chem}{uneditable});
  $row .= $query->td({align=>'right'}, $total{chem}{editable});
  push @rows, $row;

  $row = "";
  $row .= $query->td("Clinical");
  $row .= $query->td({align=>'right'}, $total{clinical}{all});
  $row .= $query->td({align=>'right'}, $total{clinical}{uneditable});
  $row .= $query->td({align=>'right'}, $total{clinical}{editable});
  push @rows, $row;

  $row = "";
  $row .= $query->td("Other");
  $row .= $query->td({align=>'right'}, $total{other}{all});
  $row .= $query->td({align=>'right'}, $total{other}{uneditable});
  $row .= $query->td({align=>'right'}, $total{other}{editable});
  push @rows, $row;

  $html .= $query->table({border=>1, cellpadding=>5, cellspacing=>0, width=>'90%'}, $query->Tr(\@rows));

  &printhtml({title=>'EMS: Mutually Exclusive Bins', h1=>"EMS: Mutually Exclusive Bins", body=>$html});
  return;
}

sub stats_table {
  my($b) = @_;
  my(@d);

  push @d, [[{align=>'right'}, $query->strong("On:")], $b->{generation_date}];
  push @d, [[{align=>'right'}, $query->strong("In:")], GeneralUtils->sec2hms($b->{generation_time})];

  return &toHTMLtable($query, {-border=>0, -cellpadding=>0, -cellspacing=>0}, \@d);
}

sub action_table {
  my($binconfig, $bininfo, $content_type) = @_;
  my(@d);
  my($url2) = $main::EMSCONFIG{LEVEL2EMSURL} . "?$DBget&";

  return $query->br if $binconfig->{editable} ne 'Y';
  if ($content_type eq "all" || !$content_type) {
    push @d, [$query->a({href=>$url2 . "action=update_counts&bin_name=" . $binconfig->{bin_name}}, "Refresh counts")];
    push @d, [$query->a({href=>'https://wiki.nlm.nih.gov/confluence/display/UE/Unimed+Bin+Comments#UnimedBinComments-'.$binconfig->{bin_name}}, "Comments")];
  }
  if (
      (uc($binconfig->{content_type}) ne "MIXED" || lc($content_type) ne "all" || !$content_type)) {
    push @d, [$query->a({href=>$url2 . "action=checklist&content_type=$content_type&bin_name=" . $binconfig->{bin_name}}, "Checklist")];
    push @d, [$query->a({href=>$url2 . "action=worklist&content_type_wanted=$content_type&bin_name=" . $binconfig->{bin_name}}, "Worklist")];
  }

  return &toHTMLtable($query, {-border=>0, -cellpadding=>0, -cellspacing=>0}, \@d);
}

1;
