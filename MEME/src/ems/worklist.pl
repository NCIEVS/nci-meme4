# suresh@nlm.nih.gov 10/98
# suresh@nlm.nih.gov 1/00 - Modified for Oracle
# suresh@nlm.nih.gov 7/05 - Mods for EMS3

# CGI params
# bin_name=
# content_type_wanted= (CHEM, NONCHEM, ALL) default is ALL
# max_clusters= (how many clusters in worklist?)
# worklist_prefix=
# from_num=
# howmany_worklists=
# excludetop=
# doit=

# Makes one or more worklists from a bin
sub do_worklist {
  my($html);
  my($bin_name) = $query->param('bin_name');
  my($content_type_wanted) = $query->param('content_type_wanted');
  my($bininfo, $bin_type);
  my($howmany_worklists);
  my(@worklists);

  &printhtml({printandexit=>1, body=>"Need a bin name to make worklist"}) if !$bin_name;

# is bin locked?
  if (EMSBinlock->islocked($dbh, {bin_name=>$bin_name})) {
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

    &printhtml({h1=>'Worklist', body=>$msg, printandexit=>1});
  }

  $bininfo = EMSUtils->getBininfo($dbh, $bin_name);
  $bin_type = $bininfo->{bin_type};

# If ME bin, is partitioning happening?
  if ($bin_type eq "ME") {
    $r = EMSMaxtab->get($dbh, $EMSNames::MEPARTITIONINGLOCKKEY);
    if ($r && $r->{valueint} > 0) {
      my($msg) = "ME bins are currently being repartitioned by " . $r->{valuechar} . ".";
      &printhtml({db=>$db, body=>$msg, printandexit=>1});
    }
  }

  unless ($query->param('doit')) {
    my($worklist_prefix) = "wrk" . $currentepoch . "_" . $bin_name;
    my($suffix);

    $suffix = "_ch" if uc($content_type_wanted) eq "CHEM";
    $suffix = "_nc" if uc($content_type_wanted) eq "NONCHEM";

    my($from_num) = (
		     uc($content_type_wanted) eq "CHEM" ?
		     $bininfo->{nextChemWorklistNum} :
		     (
		      uc($content_type_wanted) eq "NONCHEM") ?
		     $bininfo->{nextNonchemWorklistNum} :
		     $bininfo->{nextWorklistNum}
		    );

    my(@howmany) = (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    my(@max_clusters) = (100, 200, 300, 400, 500, 600, 700, 800, 900, 1000);

    $html .= $query->start_form(-method=>'POST', -action=>$query->url());

    $html .= <<"EOD";
Please select from the following options and submit this form
to generate one or more worklists from the bin: <B>$bin_name</B>.
EOD
    $html .= "Only chemical content from the bin will be included." if uc($content_type_wanted) eq "CHEM";
    $html .= "Only non-chemical content from the bin will be included." if uc($content_type_wanted) eq "NONCHEM";

    $html .= $query->p;

    push @d, ["Starting with worklist: " . $query->b(sprintf("%s%s_%.2d", $worklist_prefix, $suffix, $from_num)) . ", make the next: ",
	      $query->popup_menu({-name=>'howmany_worklists', -value=>\@howmany})];
    push @d, ["Exclude the first:", $query->textfield({-name=>'excludetop', -size=>4}) . " clusters"];
    push @d, ["Each worklist should have a maximum of: ", $query->popup_menu({-name=>'max_clusters', -values=>\@max_clusters, -default=>300}) . " clusters"];
    $html .= &toHTMLtable($query, {border=>1, -cellpadding=>5, -cellspacing=>0}, \@d);

    $html .= $DBpost;
    $html .= $query->hidden(-name=>'bin_name', -value=>$bin_name, -override=>1);
    $html .= $query->hidden(-name=>'worklist_prefix', -value=>$worklist_prefix, -override=>1);
    $html .= $query->hidden(-name=>'content_type_wanted', -value=>$content_type_wanted, -override=>1);
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $html .= $query->br;
    $html .= $query->submit;
    $html .= $query->end_form;

    &printhtml({h1=>'Worklist', body=>$html});
    return;

  } else {

    my($worklist_prefix) = $query->param('worklist_prefix');
    my($max_clusters) = $query->param('max_clusters') || 300;

    my($worklist_name, $i);
    my(%p);

    &printhtml({printandexit=>1, body=>"Need a worklist prefix."}) unless $worklist_prefix;

    $howmany_worklists = $query->param('howmany_worklists') || 1;

    $p{created_by} = ($httpuser||$unixuser||"unknown");
    $p{limit} = $max_clusters;
    $p{bin_name} = $bin_name;
    $p{content_type_wanted} = uc($content_type_wanted);
    $p{bininfo} = $bininfo;
    $p{worklistdir} = $ENV{EMS_HOME} . "/log/worklists/" . $currentepoch;
    $p{worklist_prefix} = $worklist_prefix;
    $p{chemalgo} = uc($main::EMSCONFIG{DEFAULT_CHEMALGO});
    $p{excludetop} = $query->param('excludetop') if $query->param('excludetop');

# lock bin for making worklist
    EMSBinlock->lock($dbh, {bin_name=>$bin_name, reason=>"making worklists", owner=>($httpuser||$unixuser)});
    EMSMaxtab->set($dbh, $EMSNames::MEWORKLISTLOCKKEY, {valueint=>1, valuechar=>join(":", ($httpuser||$unixuser), $bin_name), timestamp=>"SYSDATE" }) if $bin_type eq "ME";

    for ($i=0; $i<$howmany_worklists; $i++) {
      push @worklists, EMSUtils->nextWorklistName($bininfo, $worklist_prefix, $content_type_wanted);
      eval { EMSUtils->makeWorklist($dbh, \%p); };
      &printhtml({printandexit=>1, body=>"Error: $@"}) if $@;
    }
    EMSMaxtab->remove($dbh, $EMSNames::MEWORKLISTLOCKKEY) if $bin_type eq "ME";
    EMSBinlock->unlock($dbh, {bin_name=>$bin_name});
  }

  my($link);

  if ($howmany_worklists == 1) {
    $html .= "The worklist: " . $worklists[0] . " was successfully made.";
    $link = $query->a({-href=>$EMSCONFIG{LEVEL0WMSURL} . "?$DBget&action=wms_query&namelike=$worklists[0]&doit=1"}, "link to the WMS");
  } else {
    $html .= "The following worklists were successfully made:";
    $html .= $query->ul($query->li(\@worklists));
    $link = $query->a({-href=>$EMSCONFIG{LEVEL0WMSURL} . "?$DBget&action=wms_query"}, "link to the WMS");
  }
  $html .= $query->p;
  $html .= "Use the $link to view the contents.";
  &printhtml({body=>$html});
  return;
}
1;
