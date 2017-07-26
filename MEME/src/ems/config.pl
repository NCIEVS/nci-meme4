# Change the config file used by the EMS/WMS
# suresh@nlm.nih.gov 11/05

# doit=
sub do_config {
  my($html);
  my($form) = $query->p;
  my($config) = $query->param('config') || $ENV{EMS_CONFIG} || "ems.config";
  my($url) = ($main::program eq "EMS" ? $main::EMSCONFIG{LEVEL0EMSURL} : $main::EMSCONFIG{LEVEL0WMSURL});

  $html .= <<"EOD";
The EMS and WMS use a configuration file in \$EMS_HOME/config
for initialization.  Using a different config file typically
allows for an alternate setting for the MEME environment and
can be used for testing new software.
<P>
Select a different configuration file from the following menu for this
session.
EOD
  $html .= $query->p;

  $dir = $ENV{EMS_HOME} . "/etc";
  opendir(D, $dir) || die $@;
  foreach $c (readdir(D)) {
    next if -d "$dir/$c";
    next unless -r "$dir/$c" && $c =~ /^ems\./;
    next if $c =~ /~$/ || $c =~ \#$/;
    push @configs, $c;
  }
  closedir(D);

  $form .= $query->start_form(-method=>'POST', -action=>$url);
  $form .= $query->popup_menu({-name=>'config', -value=>\@configs, -default=>$config});
  $form .= $query->submit(-value=>'OK');
  $form .= $DBpost;
  $form .= $query->hidden(-name=>'action', -value=>'', -override=>1);
  $form .= $query->end_form;

  $html .= $form;
  &printhtml({title=>'Change config', h1=>"Change config", body=>$html});
}
1;
