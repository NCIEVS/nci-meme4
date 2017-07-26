# suresh@nlm.nih.gov 10/98
# suresh@nlm.nih.gov 1/00 - Modified for Oracle
# suresh@nlm.nih.gov 7/05 - Mods for EMS3

# CGI params
# bin_name=
# table_name= (or alternatively, make a checklist from a concept table)
# content_type= (chem, nonchem)
# checklist_suffix=
# limit=
# randomize=
# excludetop=
# excludebeingedited=
# owner=

# Makes a checklist from a bin
sub do_checklist {
  my($html);
  my($bin_name) = $query->param('bin_name');
  my($table_name) = $query->param('table_name');
  my($content_type) = $query->param('content_type');
  my($checklist_suffix) = $query->param('checklist_suffix') || $httpuser || $unixuser || "unknown";
  my($checklist_name) = "chk_" . $checklist_suffix;
  my($bininfo, $bin_type);

  &printhtml({printandexit=>1, body=>"Need a bin name to make checklist from"}) if !$bin_name && !$table_name;
  &printhtml({printandexit=>1, body=>"Could not construct a checklist name - need a suffix."}) unless $checklist_suffix;
  &printhtml({printandexit=>1, body=>"Checklist name: $checklist_name has illegal characters."}) if $checklist_name =~ /\W/;

# is bin locked?
  if (!$table_name && EMSBinlock->islocked($dbh, {bin_name=>$bin_name})) {
    my($l) = EMSBinlock->get($dbh, {bin_name=>$bin_name});
    my($msg);

    $msg = join(" ",
		"Bin:",
		$query->strong($bin_name),
		"is currently locked for use by user:",
		$query->strong($l->{user}),
		"at:",
		$l->{timestamp});
    $msg .= $query->p;
    $msg .= "Reason: " . $l->{reason};

    &printhtml({h1=>'Checklist', body=>$msg, printandexit=>1});
  }

  if (!$table_name) {
    $bininfo = EMSUtils->getBininfo($dbh, $bin_name);
    $bin_type = $bininfo->{bin_type};
  }

  unless ($query->param('doit')) {
    my($remoteIP) = $ENV{REMOTE_ADDR};
    $remoteIP =~ s/\./\_/g;
    $checklist_suffix = $remoteIP || ($httpuser || $unixuser || "unknown");
    $checklist_suffix =~ s/[^0-9a-zA-Z_]/_/g;
    $checklist_suffix = substr($checklist_suffix, 0, 18);

    $html .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    if (!$table_name) {
      $html .= "From the contents of the bin " . $query->strong($bin_name) . ", make a checklist with the following parameters.";
      $html .= "Only chemical content from the bin will be included." if lc($content_type) eq "chem";
      $html .= "Only non-chemical content from the bin will be included." if lc($content_type) eq "nonchem";
    }
    $html .= $query->p;

    push @d, ["Checklist name: " . $query->b("chk_"),
	      $query->textfield({-name=>'checklist_suffix', -value=>$checklist_suffix, -size=>18, -maxlength=>18})];
    push @d, ["Maximum number of clusters:", $query->popup_menu({-name=>'limit', -values=>[500,400,300,200,100,50,10,10000], -default=>50})];
    push @d, ["Randomize the clusters:", $query->checkbox({-name=>'randomize', -label=>''})];
    push @d, ["Or exclude the first:", $query->textfield({-name=>'excludetop', -size=>4}) . " clusters"];
    push @d, ["Exclude clusters currently being edited:", $query->checkbox({-name=>'excludebeingedited', -label=>''})];
    $html .= &toHTMLtable($query, {border=>1, -cellpadding=>5, -cellspacing=>0}, \@d);

    $html .= $DBpost;
    $html .= $query->hidden(-name=>'bin_name', -value=>$bin_name, -override=>1);
    $html .= $query->hidden(-name=>'table_name', -value=>$table_name, -override=>1);
    $html .= $query->hidden(-name=>'content_type', -value=>$content_type, -override=>1);
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $html .= $query->br;

    $html .= $query->submit;
    $html .= $query->end_form;

    &printhtml({h1=>'Checklist', body=>$html});

  } else {

# lock bin for making checklist
    EMSBinlock->lock($dbh, {bin_name=>$bin_name, reason=>"making checklist: $checklist_name", owner=>($httpuser||$unixuser)}) if $bin_name;

    my(%p);
    foreach (qw(bin_name table_name limit excludetop randomize excludebeingedited content_type)) {
      $p{$_} = $query->param($_);
    }
    $p{owner} = ($httpuser || $unixuser) unless $p{owner};
    $p{bin_type} = $bin_type;

    EMSUtils->makeChecklist($dbh, $checklist_name, \%p);

    EMSBinlock->unlock($dbh, {bin_name=>$bin_name}) if $bin_name;
    &printhtml({redirect=>$main::EMSCONFIG{LEVEL0WMSURL} . "?$DBget&action=view&checklist=$checklist_name"})
  }
  return;
}
1;
