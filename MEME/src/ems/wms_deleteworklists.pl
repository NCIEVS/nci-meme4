# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000
# EMS3 - 12/2005

# Updates information for a worklist (level1 and level2 users)

# CGI params:
# db=
# epoch=
# n=
# doit=

sub do_wms_deleteworklists {
  my($sql);
  my($html);
  my($WORKLISTINFOTABLE) = $EMSNames::WORKLISTINFOTABLE;
  my(@d);
  my($n) = 0;
  my(%all, %stamped, $oldest, $newest);
  my(@rows, $r);
  my($epoch);

  unless ($query->param('epoch')) {
    $sql = <<"EOD";
select substr(b.worklist_name, 4, 3) as epoch, count(*) as c from all_objects a, $WORKLISTINFOTABLE b where
a.object_name like 'WRK%' and
a.object_type='TABLE' and
lower(a.object_name) = lower(b.worklist_name) and
b.create_date < (SYSDATE-365)
group by substr(b.worklist_name, 4, 3)
EOD
    @rows = $dbh->selectAllAsRef($sql);
    foreach $r (@rows) {
      $all{$r->[0]} = $r->[1];
    }

    $sql = <<"EOD";
select substr(b.worklist_name, 4, 3) as epoch, count(*) as c from all_objects a, $WORKLISTINFOTABLE b where
a.object_name like 'WRK%' and
a.object_type='TABLE' and
lower(a.object_name) = lower(b.worklist_name) and
b.create_date < (SYSDATE-365) and
b.stamp_date is not null
group by substr(b.worklist_name, 4, 3)
EOD
    @rows = $dbh->selectAllAsRef($sql);
    foreach $r (@rows) {
      $stamped{$r->[0]} = $r->[1];
    }

    foreach $epoch (sort keys %all) {
      my($x) = "wrk" . lc($epoch) . "%";
      $sql = <<"EOD";
select max(create_date), min(create_date) from $WORKLISTINFOTABLE where
worklist_name like '$x' and
create_date < (SYSDATE-365)
EOD
      @rows = $dbh->selectAllAsRef($sql);
      foreach $r (@rows) {
	$newest{$epoch} = $r->[0];
	$oldest{$epoch} = $r->[1];
      }
    }

    @rows = ();
    push @rows, ["Editing epoch", "All worklists", "Stamped worklists", "Oldest create_date", "Newest create_date", "Action"];
    my($form);

    foreach $epoch (sort keys %all) {
      $form = "";
      $form .= $query->start_form({-method=>'POST', -action=>$query->url(-absolute=>1)});
      $form .= $query->submit(-value=>"Delete $epoch worklists");
      $form .= $query->hidden(-name=>"action", -value=>$action);
      $form .= $query->hidden(-name=>"epoch", -value=>$epoch, -override=>1);
      $form .= $query->hidden(-name=>"n", -value=>$all{$epoch}, -override=>1);
      $form .= $DBpost;
      $form .= $query->end_form() . $query->p();

      push @rows, [$epoch, $all{$epoch}, $stamped{$epoch}, $oldest{$epoch}, $newest{$epoch}, [{-valign=>'center', -align=>'center'}, $form]];
    }

    $html .= <<"EOD";
The following table lists the number of worklists created over a year ago
that are currently in the database, shown by editing epoch.  The oldest and newest worklist
creation date for each epoch are also shown.  Select the "Delete" button to delete all
stamped worklists from that epoch.
<P>
EOD
    $html .= &toHTMLtable($query, {-border=>1, -cellspacing=>0, -cellpadding=>5}, \@rows, "firstisheader");

    &printhtml({body=>$html});
    return;
  }

  $epoch = $query->param('epoch');

  unless ($query->param('doit')) {
    my($n) = $query->param('n');

    $html .= $query->p;
    $html .= "Are you sure you want to delete all $n worklists for epoch: " . $query->em($epoch) . "?";
    $html .= $query->p;
    $html .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $html .= $DBpost;
    $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->hidden(-name=>'epoch', -value=>$epoch, -override=>1);

    &printhtml({body=>$html});
    return;
  }

  my($p) = $dbh->quote("WRK" . $epoch . '%');
  $sql = <<"EOD";
select a.object_name from all_objects a, $WORKLISTINFOTABLE b where
a.object_name like $p and
a.object_type='TABLE' and
lower(a.object_name) = lower(b.worklist_name) and
b.create_date < (SYSDATE-365) and
b.stamp_date is not null
group by substr(b.worklist_name, 4, 3)
EOD
  my(@worklists) = $dbh->selectAllAsArray($sql);
  $dbh->dropTables(\@worklists);

  $html .= "All stamped worklists for the epoch " . $query->em($epoch) . " were removed from the database.";
  &printhtml({body=>$html});
  return;
}

1;
