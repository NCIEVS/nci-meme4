# Access control to the EMS/WMS
# suresh@nlm.nih.gov 12/98
# suresh@nlm.nih.gov 7/05 - EMS3 mods

# CGI params
# access=

sub do_access {
  my($html);
  my($form) = $query->p;
  my(@d);

  my(%labels) = (
		 'CONFIG'=>'Same as config file',
		 'CLOSED'=>'Close EMS/WMS for all users',
		 'LEVEL2ONLY'=>'Open EMS/WMS to level 2 users only'
		 );

  if ($query->param('doit')) {
    my($access) = $query->param('access');
    EMSMaxtab->set($dbh, $EMSNames::EMSACCESSKEY, {valuechar=>$access});
    $main::ACCESS = $access;
    $html = "EMS/WMS access changed to: " . $access;
  } else {

    my($accesskey) = EMSMaxtab->get($dbh, $EMSNames::EMSACCESSKEY);
    my($currentaccess) = $accesskey->{valuechar} if $accesskey;
    $currentaccess = $currentaccess || $query->param('access') || 'CONFIG';

    $form .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    
    $form .= "Set EMS and WMS access to: ";
    $form .= $query->popup_menu({-name=>'access',
				 -values=>['CONFIG', 'CLOSED', 'LEVEL2ONLY'],
				 -labels=>\%labels,
				 -default=>$currentaccess});
    $form .= $query->p;
    $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $form .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $form .= $query->p;
    $form .= $query->submit(-value=>"Change");
    $form .= $DBpost;
    $form .= $query->end_form;

    $html .= <<"EOD";
Access to the EMS and WMS is controlled via the config file.  But this form
allows you to override those settings for this database with the following
restrictions.  Submit the form if you want to change the value.
<P>
$form
EOD
  }
  &printhtml({h1=>"Access Control for EMS/WMS", body=>$html});
}
1;
