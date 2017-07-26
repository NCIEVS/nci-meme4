# Tests the LVGIF interface
# suresh@nlm.nih.gov 2/06 - EMS3

# CGI params
# norm=
# luinorm=
# wordind=

sub do_lvgif{
  my($html);
  my($form) = $query->p;
  my($size) = 20;

  my($norm) = $query->param('norm');
  my($luinorm) = $query->param('luinorm');
  my($wordind) = $query->param('wordind');
  my($config) = $query->pre(GeneralUtils->file2str(join("/", $ENV{LVGIF_HOME}, "config", "lvgif.config")));

# check to see if the server is up by norming "dogs"
  unless ($norm || $luinorm || $wordind) {
    unless (LVG->luinorm("dogs")) {
      $html .= <<"EOD";
ERROR: It appears that the server is not currently responding.
See LVGIF configuration information below.
EOD
      $html .= $query->p;
      $html .= $query->hr;
      $html .= $config;
      &printhtml({printandexit=>1, body=>$html});
    }
  }

  $form .= $query->start_form(-method=>'POST', -action=>$url);
  $form .= "Norm forms of: " . $query->textfield({-name=>'norm', -default=>$norm, -size=>$size, -limit=>80, -onChange=>"submit();"}) . ", e.g., \"leaves\", \"canuli\"";
  if ($norm) {
    $form .= $query->p . &toHTMLtable($query, {border=>1, cellspacing=>1, cellpadding=>5}, [[ join($query->br, LVG->norm($norm)) ]]);
  }
  $form .= $query->br;
#  $form .= $query->submit;

  $form .= $query->p;

  $form .= $query->start_form(-method=>'POST', -action=>$url);
  $form .= "Luinorm form of: " . $query->textfield({-name=>'luinorm', -default=>$luinorm, -size=>$size, -limit=>80, -onChange=>"submit();"}) . ", e.g., \"arteries\", \"datum\"";
  if ($luinorm) {
    $form .= $query->p . &toHTMLtable($query, {border=>1, cellspacing=>1, cellpadding=>5}, [[ join($query->br, LVG->luinorm($luinorm)) ]]);
  }
  $form .= $query->br;
#  $form .= $query->submit;

  $form .= $query->p;

  $form .= $query->start_form(-method=>'POST', -action=>$url);
  $form .= "Wordind tokens for: " . $query->textfield({-name=>'wordind', -default=>$wordind, -size=>$size, -onChange=>"submit();"});
  if ($wordind) {
    $form .= $query->p . &toHTMLtable($query, {border=>1, cellspacing=>1, cellpadding=>5}, [[ join($query->br, LVG->wordind($wordind)) ]]);
  }
  $form .= $query->br;
#  $form .= $query->submit;

  $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
  $form .= $DBpost;
  $form .= $query->end_form;

  $html .= <<"EOD";
The forms in this page allow you to test the LVG software
used in the production of the UMLS Metathesaurus.
It can also report the fact that the server is either not
available or is unresponsive.
EOD
  $html .= $query->p;
  $html .= $form;
  $html .= $query->p . $query->hr({-noshade=>1, -size=>4}) . $query->p;
  $html .= $query->h2("LVGIF configuration information");
  $html .= "The LVGIF_HOME is currently set to: " . $EMSCONFIG{LVGIF_HOME} . ".";
  $html .= $query->p;
  $html .= <<"EOD";
The configuration information in that installation directory determines which
server, and on which TCP port is accessed.  The LVGIF configuration information
is presented below, essentially to aid debugging.
<P>
EOD
  $html .= $config;

  $title = "Testing the LVGIF interface and LVG software";
  &printhtml({title=>$title, h1=>$title, body=>$html});
}
1;
