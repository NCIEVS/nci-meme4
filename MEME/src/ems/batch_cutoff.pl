# Cut off any batch scripts that run on the behalf of the EMS/WMS
# suresh@nlm.nih.gov 1/06

# CGI params
# db=
# config=
# batch_cutoff=Yes|No
# doit=1

sub do_batch_cutoff {
  my($html);
  my($form) = $query->p;
  my($x) = EMSMaxtab->get($dbh, $EMSNames::EMSBATCHCUTOFFKEY);
  my($current);
  my(@values) = qw(Yes No);

  unless ($x) {
    EMSMaxtab->set($dbh, $EMSNames::EMSBATCHCUTOFFKEY, {valuechar=>'No'});
    $x = EMSMaxtab->get($dbh, $EMSNames::EMSBATCHCUTOFFKEY);
  }
  die "ERROR: No value for $EMSNames::EMSBATCHCUTOFFKEY in maxtab" unless $x;

  $current = $x->{valuechar};

  unless ($query->param('doit')) {

    $html .= <<"EOD";
This facility lets you turn on and off any batch scripts run on
the behalf of the EMS/WMS such as the daily editing counts.
EOD
    $html .= $query->p;
    $form .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $form .= "Cutoff batch scripts? " . $query->radio_group({-name=>'batch_cutoff', -default=>$current, -values=>\@values});
    $form .= $query->p . $query->submit . " " . $query->reset;
    $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $form .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $form .= $DBpost;
    $form .= $query->end_form;
    $html .= $form;

    &printhtml({title=>"Batch cut off", body=>$html});

  } else {

    EMSMaxtab->set($dbh, $EMSNames::EMSBATCHCUTOFFKEY, {valuechar=>$query->param('batch_cutoff')});
    $x = EMSMaxtab->get($dbh, $EMSNames::EMSBATCHCUTOFFKEY);
    $current = $x->{valuechar};

    &printhtml({body=>"Batch cutoff now set to: $current"});
  }
}
1;
