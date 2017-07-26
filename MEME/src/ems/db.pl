# Change the database
# suresh@nlm.nih.gov 12/98
# suresh@nlm.nih.gov 7/05 - EMS3 mods

sub do_db {
  my(@databases) = split /[,\s*]/, Midsvcs->get('databases');
  my($html);
  my($form) = $query->p;
  my($url) = ($main::program eq "EMS" ? $main::EMSCONFIG{LEVEL0EMSURL} : $main::EMSCONFIG{LEVEL0WMSURL});

  $form .= $query->start_form(-method=>'POST', -action=>$url);
  $form .= "Select database: " . $query->popup_menu({-name=>'db', -default=>$db, -values=>\@databases, -onChange=>'submit();'});
  $form .= $query->p . $query->submit;
  $form .= $query->hidden(-name=>'action', -value=>'', -override=>1);
  $form .= $DBpost;
  $form .= $query->end_form;

  $html .= join(" ", "Use this form to change the default database for", $main::program, "actions for this session.");
  $html .= $form;
  &printhtml({title=>'Change Database', h1=>"Change Database", body=>$html});
}
1;
