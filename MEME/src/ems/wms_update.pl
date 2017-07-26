# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000
# EMS3 - 12/2005

# Updates information for a worklist (level1 and level2 users)

# CGI params:
# db=
# doit=
# worklist=

# level=(1 or 2)

# $worklist_*= (cols for each worklist, e.g., $worklist_return_date)
# $worklist_edithours=
# $worklist_editmins=

sub do_wms_update {
  my($sql);
  my($html);
  my($WORKLISTINFOTABLE) = $EMSNames::WORKLISTINFOTABLE;
  my(@worklists) = $query->param('worklist');
  my($worklist);
  my($level) = $query->param('level') || 1;
  my(@d);
  my($n) = 0;
  local($na) = "--n/a--";
  %statuslabels = (
		   CR=>'Created',
		   ED=>'Being edited',
		   DO=>'Done'
		  );
  @statuslabels = ($na, sort keys %statuslabels);
  @edithours = ($na, 1..24);
  @editmins = ($na, 0,15,30,45);

  unless ($query->param('doit')) {

    my($grpmenu, $editormenu);

    if ($level == 1) {
      $html .= <<"EOD";
For each worklist, confirm the editor initials and choose the return date
and the approximate time to edit the worklist.
EOD
    } else {
      $html .= <<"EOD";
For each worklist, confirm the group and editor initials and select the
status value for the worklist. Note that to be able to stamp a worklist,
it must be in the "RT" status.
<P>
Note that a status of "Done" retires the worklist without approving the
concepts.  Set the status to "Being Edited" if the worklist needs to be
re-edited.
EOD
    }

    $html .= $query->p;
    $html .= <<"EOD";
When updating multiple worklists, ensure that *all* fields in
all the worklists are set to correct values before submitting
the form.  It will overwrite the old data with the new if changed.
EOD
    $html .= $query->p;
    $html .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $html .= $DBpost;
    $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->hidden(-name=>'worklist', -value=>\@worklists, -override=>1);

    @d = ();
    if ($level == 2) {
      push @d, ["", 'Worklist', 'Grp', 'Editor', 'Worklist Status'];
    } elsif ($level == 1) {
      push @d, ["", 'Worklist', 'Editor', 'Return Date', 'Edit time'];
    }

    foreach $worklist (sort @worklists) {
      $worklistinfo = WMSUtils->getWorklistinfo($dbh, $worklist);

      $grpmenu = $query->popup_menu({-name=>join("_", $worklist, 'grp'), -values=>&current_grps, -default=>$worklistinfo->{grp}});
      $editormenu = $query->popup_menu({-name=>join("_", $worklist, 'editor'), -values=>&current_editors, -default=>$worklistinfo->{editor}});
      $statusmenu = $query->popup_menu({-name=>join("_", $worklist, 'status'), -values=>\@statuslabels, -labels=>\%statuslabels, -default=>$na});
      $returndatemenu = $query->popup_menu({-name=>join("_", $worklist, 'return_date'), -values=>[$na, 'now'], -default=>$na});
      $edithoursmenu = $query->popup_menu({-name=>join("_", $worklist, 'edithours'), -values=>\@edithours, -default=>$na});
      $editminsmenu = $query->popup_menu({-name=>join("_", $worklist, 'editmins'), -values=>\@editmins, -default=>$na});

      $n++;
      if ($level == 2) {
	push @d, [$n, $worklist, $grpmenu, $editormenu, $statusmenu ];
      } elsif ($level == 1) {
	push @d, [$n, $worklist, $editormenu, $returndatemenu, $edithoursmenu . " hours, " . $editminsmenu . " mins" ];
      }
    }

    $html .= &toHTMLtable($query, {-border=>1, -cellspacing=>0, -cellpadding=>2, -width=>'80%'}, \@d, 'firstrowisheader');
    $html .= $query->p;
    $html .= $query->submit({-value=>'Update' . (@worklists > 1 ? " All" : "")});

    $html .= $query->end_form;
  
  } else {

    my($p, $v);
    my(@s, @d);

    @d = ();
    push @d, ['Worklist', 'Columns Updated'];

    foreach $worklist (@worklists) {
      @s = ();
      $worklistinfo = WMSUtils->getWorklistinfo($dbh, $worklist);

      foreach $p ($query->param) {
	next unless $p =~ /^$worklist/;

# grp
	if ($p eq $worklist . "_grp") {
	  $v = $query->param($p);
	  $v = "" if $v eq $na;

	  push @s, "grp=" . ($v ? $dbh->quote($v) : "null") if $v ne $worklistinfo->{grp};
	}

# editor (sets the assign_date if changed)
	if ($p eq $worklist . "_editor") {
	  $v = $query->param($p);
	  $v = "" if $v eq $na;

	  if ($v ne $worklistinfo->{editor}) {
	    unless ($v) {
	      push @s, "editor=null";
	      push @s, "assign_date=null";
	    } else {
	      push @s, "editor=" . $dbh->quote($query->param($p));
	      push @s, "assign_date=SYSDATE";
	    }
	  }
	}

# worklist_status
	if ($p eq $worklist . "_status") {
	  $v = $query->param($p);
	  $v = "" if $v eq $na;

	  if ($v) {
	    push @s, "worklist_status=" . $dbh->quote($v);
	    if ($v eq "DO") {
	      push @s, "stamp_date=SYSDATE";
	      $sql = "delete from $EMSNames::BEINGEDITEDTABLE where worklist_name=" . $dbh->quote($worklist);
	      $dbh->executeStmt($sql);
	    }
	  }
	}

# return_date
	if ($p eq $worklist . "_return_date") {
	  $v = $query->param($p);
	  $v = "" if $v eq $na;

# if already stamped, do not change return_date
	  unless ($worklistinfo->{stamp_date}) {
	    if ($v) {
	      push @s, "return_date=SYSDATE";
	      push @s, "worklist_status=" . $dbh->quote('RT');
	    } else {
	      push @s, "return_date=null";
	      push @s, "worklist_status=" . $dbh->quote('ED');
	    }
	  }
	}

# edit time
	if ($p eq $worklist . "_edithours" || $p eq $worklist . "_editmins") {
	  my($h) = $query->param($worklist . "_edithours");
	  my($m) = $query->param($worklist . "_editmins");
	  my($t) = 3600*$h + 60*$m;

	  $t = -1 unless $t;

	  if ($t ne $worklistinfo->{edit_time}) {
	    $worklistinfo->{edit_time} = $t;
	    push @s, "edit_time=$t";
	  }
	}
      }

      if (@s) {
	$sql = "update $WORKLISTINFOTABLE set " . join(', ', @s) . " where worklist_name=" . $dbh->quote($worklist);
	$dbh->executeStmt($sql);

	push @d, [$worklist, join(', ', map { ((split /=/, $_)[0]) } @s)];
      }
    }
    $html .= &toHTMLtable($query, {-border=>1,-cellspacing=>0, -cellpadding=>2}, \@d, 'firstrowisheader');
  }
  &printhtml({body=>$html});
}

sub current_editors {
  my($sql) = "select distinct initials from editors where cur=" . $dbh->quote('Y') . "order by initials";
  my(@editors) = $dbh->selectAllAsArray($sql);
  return [ $na, @editors ];
}

sub current_grps {
  my($sql) = "select distinct grp from editors where cur=" . $dbh->quote('Y');
  my(@grp) = $dbh->selectAllAsArray($sql);
  return [ $na, @grp ];
}

1;
