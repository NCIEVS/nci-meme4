# Data about the MIDSVCS being used
# suresh@nlm.nih.gov 2/06

sub do_midsvcs {
  my($html);
  my(@d);

  $html .= <<"EOD";
The following table shows the current key/value mappings
specified in the MIDSVCS modules.
EOD
  $html .= $query->p;
  $html .= "Current MIDSVCS_HOME: " . $ENV{MIDSVCS_HOME};
  $html .= $query->p;

  my($midsvcs) = Midsvcs->load;
  foreach $key (sort keys %$midsvcs) {
    push @d, [$query->b($key), $midsvcs->{$key}];
  }
  $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>5, -cellspacing=>0}, \@d);
  &printhtml({title=>'MIDSVCS info', h1=>"MIDSVCS info", body=>$html});
}
1;
