# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000
# EMS3 - 12/2005

# How stale are concepts on a worklist?

# CGI params:
# db=
# worklist=

sub do_wms_stale {
  my($sql);
  my($html);
  my($WORKLISTINFOTABLE) = $EMSNames::WORKLISTINFOTABLE;
  my(@d, @d1);
  my($worklist);
  my(@cols) = EMSTables->columns($WORKLISTINFOTABLE);
  my($create_date, $n_concepts, $n_clusters, $editor);

  foreach $worklist ($query->param('worklist')) {
    $worklistinfo = WMSUtils->getWorklistinfo($dbh, $worklist);
    $create_date = $worklistinfo->{create_date};
    $n_concepts = $worklistinfo->{n_concepts};
    $n_clusters = $worklistinfo->{n_clusters};
    $editor = $worklistinfo->{editor};

    unless ($create_date) {
      $html .= "No information available for worklist: $worklist";
      next;
    }

    $html .= $query->p;
    $html .= $query->hr;
    $html .= $query->h1($worklist);

    push @d, ['Worklist name:', $worklist];
    push @d, ['Create date:', $create_date];
    push @d, ['Concepts:', $n_concepts];
    push @d, ['Clusters:', $n_clusters];
    push @d, ['Editor:', $editor];

# How many concepts no longer exist
    $sql = <<"EOD";
select count(distinct orig_concept_id) from $worklist w where
       not exists (select concept_id from concept_status where
                          concept_id=w.orig_concept_id)
EOD
    my($extinct) = $dbh->selectFirstAsScalar($sql) || 0;

    push @d, ['Concepts on worklist that no longer exist', $extinct];

# How many concepts have more recent timestamps
    my($cd) = $dbh->quote($create_date);

    $sql = <<"EOD";
select count(distinct w.orig_concept_id) from $worklist w, concept_status cs
where  w.orig_concept_id=cs.concept_id
and    cs.timestamp > to_date($cd)
EOD
    my($recent) = $dbh->selectFirstAsScalar($sql) || 0;

    push @d, ['Concepts with content more recent than date worklist was created', $recent];

    $html .= &toHTMLtable($query, {-border=>1, -width=>'80%', -cellspacing=>0, -cellpadding=>2}, \@d);

    $html .= $query->p;
    $html .= $query->h2("Approval Profile");
    $html .= <<"EOD";
The following table shows the current approval profile by editor for the
extant concepts on this worklist.  Highlighted rows are for the editor that
the worklist is currently assigned to.
EOD
    $html .= $query->p;

    $sql = <<"EOD";
SELECT cs.editing_authority, count(distinct w.orig_concept_id) FROM $worklist w, concept_status cs
WHERE  w.orig_concept_id=cs.concept_id
group by cs.editing_authority
order by 2 desc
EOD
    @refs = $dbh->selectAllAsRef($sql);
    @d = ();
    push @d, ['Authority', 'Approval', 'Count'];
    foreach $r (@refs) {
      if ($editor && $r->[0] =~ /$editor/i) {
        $c = {-bgcolor=>'#F6F6C0'};
      } else {
        $c = {};
      }
      push @d, [$c, $r->[0], ($r->[0] =~ /^s/i ? "(stamped)" : "(editor approved)"), $r->[1]];
    }
    $html .= &toHTMLtable($query, {-border=>1, -width=>'80%', -cellspacing=>0, -cellpadding=>2}, \@d, "firstrowisheader");
  }
  &printhtml({body=>$html});
}
1;
