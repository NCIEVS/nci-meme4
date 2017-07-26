# Data about the EMS/WMS from the config file
# suresh@nlm.nih.gov 11/05

sub do_ems_info {
  my($html);
  my(@d);

  push @d, [$query->b('EMS_HOME: '), $ENV{EMS_HOME}];
  push @d, [$query->b('Config file: '), EMSUtils->configFile];
  $html .= &toHTMLtable($query, {-border=>0, -cellpadding=>0, -cellspacing=>2}, \@d);

  $html .= $query->p;

  @d = ();
  foreach $key (sort { $main::EMSCONFIGORDER{$a} <=> $main::EMSCONFIGORDER{$b} } keys %main::EMSCONFIG) {
    $value = $main::EMSCONFIG{$key};
    if (ref($value) && ref($value) eq "ARRAY") {
      push @d, [$key, join(", ", @{ $main::EMSCONFIG{$key} })];
    } else {
      push @d, [$key, $main::EMSCONFIG{$key}];
    }
  }
  $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>5, -cellspacing=>0}, \@d);
  &printhtml({title=>'EMS/WMS info', h1=>"EMS/WMS info", body=>$html});
}
1;
