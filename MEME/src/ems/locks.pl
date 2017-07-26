# Manage EMS locks
# suresh@nlm.nih.gov 11/05 - EMS3 mods

# CGI params
# db=
# bin_name=
# me_partition=
# me_worklist=

sub do_locks {
  my($html);
  my($form) = $query->p;
  my($sql);

  if ($query->param('me_partition')) {
    EMSMaxtab->remove($dbh, $EMSNames::MEPARTITIONINGLOCKKEY);
    &printhtml({redirect=>$query->url() . "?$DBget&action=$action"});
  } elsif ($query->param('me_worklist')) {
    EMSMaxtab->remove($dbh, $EMSNames::MEWORKLISTLOCKKEY);
    &printhtml({redirect=>$query->url() . "?$DBget&action=$action"});
  } elsif ($query->param('bin_name')) {
    my($bin_name) = $query->param('bin_name');
    EMSBinlock->unlock($dbh, {bin_name=>$bin_name});
    &printhtml({redirect=>$query->url() . "?$DBget&action=$action"});
  } else {
    my(@l) = EMSBinlock->get_all($dbh);
    my(@d, $l, $x);

    $x = EMSMaxtab->get($dbh, $EMSNames::MEPARTITIONINGLOCKKEY);
    if ($x && $x->{valueint} > 0) {
      my($t);

      $t .= "The ME bins partitioning lock is currently active.";
      $t .= $query->p;
      @d = ();
      push @d, ["Locked by: ", $x->{valuechar}];
      push @d, ["Locked since: ", $x->{timestamp}];
      my($t1) .= &toHTMLtable($query, {-border=>0, -cellpadding=>2, -cellspacing=>1}, \@d);
      $t .= &toHTMLtable($query, {-border=>1, -cellpadding=>2, -cellspacing=>1}, [[$t1]]);
      $t .= $query->p;
      $t .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
      $t .= $DBpost;
      $t .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
      $t .= $query->hidden(-name=>'me_partition', -value=>1, -override=>1);
      $t .= $query->submit(-value=>'Clear');
      $t .= $query->endform;

      $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>5, -cellspacing=>1}, [[$t]]);

    } else {
      $html .= "The ME Bins are not currently being partitioned.";
    }

    @d = ();
    $html .= $query->p;
    $html .= $query->hr;

    $x = EMSMaxtab->get($dbh, $EMSNames::MEWORKLISTLOCKKEY);
    if ($x && $x->{valueint} > 0) {
      my($t);

      $t .= "A worklist is being from an ME bin.";
      $t .= $query->p;
      ($b, $u) = split /:/, $x->{valuechar};
      @d = ();
      push @d, ["Bin name: ", $b];
      push @d, ["Locked by: ", $u];
      push @d, ["Locked since: ", $x->{timestamp}];
      my($t1) .= &toHTMLtable($query, {-border=>0, -cellpadding=>2, -cellspacing=>1}, \@d);
      $t .= &toHTMLtable($query, {-border=>1, -cellpadding=>2, -cellspacing=>1}, [[$t1]]);
      $t .= $query->p;
      $t .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
      $t .= $DBpost;
      $t .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
      $t .= $query->hidden(-name=>'me_worklist', -value=>1, -override=>1);
      $t .= $query->submit(-value=>'Clear');
      $t .= $query->endform;

      $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>5, -cellspacing=>1}, [[$t]]);

    } else {
      $html .= "No ME worklists are currently being made.";
    }

    @d = ();
    $html .= $query->p;
    $html .= $query->hr;

    unless (@l) {
      $html .= "There are no locked bins in this database.";
    } else {
      push @d, ['Bin', 'Timestamp', 'Owner', 'Reason', ''];
      $html .= <<"EOD";
The following bins are locked in the database.  Clear the locks
with care.
EOD
      $html .= $query->p;
      foreach $l (@l) {
	push @d, [$l->{bin_name}, $l->{timestamp}, $l->{owner}, $l->{reason}, &clear_button($l->{bin_name})];
      }
      $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>5, -cellspacing=>1}, \@d, "firstrowisheader");
    }
    &printhtml({title=>'Bin locks', h1=>"Bin locks", body=>$html});

  }
  return;
}

sub clear_button {
  my($bin_name) = @_;
  return $query->a({-href=>$query->url() . "?action=$action&bin_name=$bin_name&$DBget"}, "Clear");
}
1;
