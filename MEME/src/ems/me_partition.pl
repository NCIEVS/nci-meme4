# suresh@nlm.nih.gov 10/98
# suresh@nlm.nih.gov 1/00 - Modified for Oracle
# suresh@nlm.nih.gov 7/05 - EMS3 mods

# Repartitions the ME bins
sub do_me_partition {
  my($r);
  my($html);

# while partitioning is running, we cannot make worklists or checklists from ME bins
  $r = EMSMaxtab->get($dbh, $EMSNames::MEPARTITIONINGLOCKKEY);
  if ($r && $r->{valueint} > 0) {
    my($msg) = "ME bins are currently being repartitioned by " . $r->{valuechar};
    $msg .= " since " . $r->{timestamp} . ".";
    $msg .= $query->p;
    $msg .= $query->a({-href=>$query->url() . "?$DBget&action=locks"}, "Clear this lock") . " and proceed.";
    &printhtml({db=>$db, body=>$msg, printandexit=>1});
  }

  $r = EMSMaxtab->get($dbh, $EMSNames::MEWORKLISTLOCKKEY);
  if ($r && $r->{valueint} > 0) {
    my($msg) = "ME bins are currently locked by " . $r->{valuechar};
    $msg .= $query->p;
    $msg .= $query->a({-href=>$query->url() . "?$DBget&action=locks"}, "Clear this lock") . " and proceed.";
    &printhtml({db=>$db, body=>$msg, printandexit=>1});
  }

  $r = EMSMaxtab->get($dbh, $EMSNames::MEPARTITIONTIMEKEY);
  unless ($query->param('doit')) {
    my($t) = GeneralUtils->sec2hms($r->{valueint}) if $r;

    if ($r && $r->{valueint} > 0) {
      $html = "Partitioning is currently estimated to take $t.  Are you sure you want to proceed?";
    } else {
      $html = "Are you sure you want to proceed with partitioning?";
    }
    $html .= $query->p;
    $html .= $query->start_form(-method=>'POST', -action=>$query->url());
    $html .= $query->p . $query->submit({-value=>'Yes!'});
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $DBpost;
    $html .= $query->hidden(-name=>'doit', -value=>'1', -override=>1);
    $html .= $query->end_form;

    &printhtml({h1=>"ME bins partitioning", body=>$html, printandexit=>1});
  }

  EMSUtils->ME_partition($dbh);

# redirect back to the me_bins URL (randomize to bust cache)
  my($redirect) = $query->url() . "?$DBget&action=me_bins";
  &printhtml({redirect=>$redirect});
  return;
}
1;
