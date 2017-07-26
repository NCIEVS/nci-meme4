# Dumps the ME/QA/AH config file
# suresh@nlm.nih.gov 2/06

# config_type={ME/QA/AH}

sub do_bin_config {
  my($html);
  my($form) = $query->p;

  unless ($query->param('config_type')) {
    $html .= <<"EOD";
Submitting the form below, produces a dump of the ME/QA/AH
config file currently in use for this database.
EOD
    $html .= $query->p;

    $form .= $query->start_form(-method=>'POST', -action=>$query->url());
    $form .= "Show the config file for: ";
    $form .= $query->popup_menu({-name=>'config_type', -value=>['ME', 'QA', 'AH']});
    $form .= $query->submit(-value=>'Show!');
    $form .= $DBpost;
    $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $form .= $query->end_form;

    $html .= $form;
    $title = "Show configuration file";

  } else {

    $CONFIG = EMSUtils->configfile($dbh, $query->param('config_type'));
    &printhtml({printandexit=>1, title=>'Show config', h1=>"Show config", body=>"ERROR: config file: $CONFIG cannot be accessed"}) unless (-e $CONFIG && -r $CONFIG);
    $title = $query->param('config_type') . " config";
    $html .= $query->pre(GeneralUtils->file2str($CONFIG));
  }
  &printhtml({title=>$title, h1=>$title, body=>$html});
}
1;
