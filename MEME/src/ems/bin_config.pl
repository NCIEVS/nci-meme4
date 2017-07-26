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

    $form .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $form .= "Show the config file for: ";
    $form .= $query->popup_menu({-name=>'config_type', -value=>['ME', 'QA', 'AH']});
    $form .= $query->submit(-value=>'Show!');
    $form .= $DBpost;
    $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $form .= $query->end_form;

    $html .= $form;
    $title = "Show configuration file";

  } elsif($query->param('config_text')) {
  
  	$CONFIG = EMSUtils->configfile($dbh, $query->param('config_type'));
  	&printhtml({printandexit=>1, title=>'Show config', h1=>"Config File", body=>"ERROR: config file: $CONFIG cannot be accessed"}) unless (-e $CONFIG && -r $CONFIG);
    $title = $query->param('config_type') . " config";
    $configText = $query->param('config_text');
    $configText =~ s/\r\n/\n/g;
    GeneralUtils->str2file($configText, $CONFIG );
   
    $html .= $query->font({-color=>'red', size=>-1}, "File Saved!");
    $html .= $query->pre(GeneralUtils->file2str($CONFIG));

  } else {
	
    $CONFIG = EMSUtils->configfile($dbh, $query->param('config_type'));
    &printhtml({printandexit=>1, title=>'Show config', h1=>"Show config", body=>"ERROR: config file: $CONFIG cannot be accessed"}) unless (-e $CONFIG && -r $CONFIG);


    $content = $query->pre(GeneralUtils->file2str($CONFIG));
    $content =~ s/\<pre\>//g;
    $content =~ s/\<\/pre\>//g;
	$content =~ s/\r\n/\n/g;
	
    $html .= $query->p;
    	
	$form .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $form .= $query->textarea (-name => 'config_text', -wrap=>'off' , -default=> $content , rows=>50, cols=>100);
    $form .= $query->p;
    $form .= $query->submit(-value=>'Save');
    $form .= $DBpost;
    $form .= $query->p; 
    $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
  	$form .= $query->hidden(-name=>'config_type', -value=>$query->param('config_type'), -override=>1);
    $form .= $query->end_form;

    $html .= $form;
    $title = "Modify " . $query->param('config_type') . " config";

   }

  &printhtml({title=>$title, h1=>$title, body=>$html});
}
1;
