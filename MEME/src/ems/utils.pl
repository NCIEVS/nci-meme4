# suresh@nlm.nih.gov 2/98
# suresh@nlm.nih.gov 9/99 Ported to Oracle
# suresh@nlm 2/02 - modified to support atomic bins/worklists
# suresh@nlm 6/2005 - EMS3 mods

# EMS/WMS related utility functions

# Prints the HTTP header and associated HTML as given in the properties hash
# h1=> title=> body=> mime_type=> redirect=> db=> version=> httpuser=> printandexit=>, returnhtml=>, javascript=>
# width=>
sub printhtml {
  my($h) = @_;
  my($html);
  my(@data);
  my($width) = $h->{width} || $main::EMSCONFIG{HTMLTABLEWIDTH} || 600;
  my($title) = $h->{title} || $main::title;
  my($config) = ($h->{config} || $ENV{EMS_CONFIG} || $main::config || "???");

  if ($h->{redirect}) {
    print $query->redirect(-location=>$h->{redirect});
    return;
  }

  $html .= $query->header(($h->{mime_type} || "text/html"));
  if ($h->{javascript}) {
    $html .= $query->start_html(-title=>$title, -script=>{
							  -language=>'JavaScript',
							  -code=>$h->{javascript}
							 });
  } else {
    $html .= $query->start_html(-title=>$title);
  }

  $html .= $query->h1($h->{h1}) if $h->{h1};

  push @data, [
	       [{align=>'left'}, $query->font({-color=>'red', size=>-1}, "Database: " . ($h->{db} || $db || "???"))],
	       [{align=>'right'}, $query->font({-size=>-1}, "Version: " . $main::VERSION)]
	      ];
  push @data, [
	       [{align=>'left'}, $query->font({size=>-1}, "User: " . ($h->{httpuser} || $httpuser || $unixuser || "???"))],
	       [{align=>'right'}, $query->font({-size=>-1}, $now)]
	      ];
  push @data, [
	       [{align=>'left'}, $query->font({size=>-1}, "Config: $config")],
	       [{align=>'right'}, $query->font({-size=>-1},
					       'Access: ' .
					       ($main::ACCESS !~ /config/i ?
						$query->font({-color=>'red'}, $main::ACCESS) :
						$main::ACCESS) .
					       ", Cutoff: " .
					       ($main::editingCUTOFF =~ /Yes/i ? $query->font({-color=>'red'}, $main::editingCUTOFF) : $main::editingCUTOFF))]
	      ];
  $html .= &toHTMLtable($query, {-width=>600, -border=>0, -cellpadding=>0, -cellspacing=>0}, \@data);

  $html .= $query->hr({-align=>'left', -size=>6, -width=>$width, noshade=>1});

  $html .= $h->{body};

  $html .= $query->p;
  $html .= $query->hr({-align=>'left', -width=>600});
  $html .= $query->address($query->a({-href=>"/"}, $main::EMSCONFIG{HOMEPAGENAME}));;
  $html .= $query->address($query->a({-href=>$main::EMSCONFIG{EMSURL} . "?$DBget"}, "EMS"));;
  $html .= $query->address($query->a({-href=>$main::EMSCONFIG{WMSURL} . "?$DBget"}, "WMS"));;
  $html .= $query->end_html;

  return $html if $h->{returnhtml};
  print $html;
  exit 0 if $h->{printandexit};
  return;
}

# Given an array of arrays (row/cols), returns an HTML table
# The first element of the a row can be a hash ref specifying row properties
# Each element of a row can be a list ref where the first is a hash ref of properties
sub toHTMLtable {
  my($cgi, $tableprop, $data, $firstrowisheader) = @_;
  my($row, $datum);
  my($rowprop, $datumprop);
  my($d);
  my(@rows, @datum);
  my($firstdatum);
  my($firstrow) = 1;

  foreach $row (@$data) {
    @datum = ();

    $firstdatum = 1;
    $rowprop = "";
    foreach $d (@$row) {
      if ($firstdatum && ref($d) eq "HASH") {
	$rowprop = $d;
      } else {
	$datumprop = {};
	if (ref($d) eq "ARRAY") {
	  $datumprop = $d->[0];
	  $datum = $d->[1];
	} else {
	  $datum = $d;
	}
	push @datum, (($firstrowisheader && $firstrow) ? $cgi->th($datumprop, $datum) : $cgi->td($datumprop, $datum));
      }
      $firstdatum = 0;
    }
    push @rows, $cgi->Tr($rowprop, @datum) . "\n";
    $firstrow = 0;
  }
  return $cgi->table($tableprop, @rows);
}
1;
