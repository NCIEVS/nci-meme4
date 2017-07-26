# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000

# Generates a report for one or more worklist or checklist
# CGI params:
# db=
# sourcedb=
# meme-server-hostport=
# list=
# checklist=
# worklist=
# whentorun=
# email=
# domain=
# reporttype=
# mailto=
# maxreviewedrels=
# outputformat=
# lat=
# doit=

# reportinbrowser

sub do_report {
  my(@list);

  @list = $query->param('list') unless @list;
  @list = $query->param('worklist') unless @list;
  @list = $query->param('checklist') unless @list;

  my($list) = join(", ", @list);
  my($sql);
  my($html);
  my(@d);
  my($hour) = GeneralUtils->date('+%H');
  my(@hrs) = map { $_ - $hour } $hour .. 23;
  my($emails) = $main::EMSCONFIG{USEREMAIL};
  my(@reporttype) = (1, 2,3);
  my(%reportlabel) = (1=>'Full concept report', 2=>'Atoms and STY', 3=>'No Context Data');
  my(@maxreviewedrels) = ('n/a', 50, 20, 10, 5);
  my(@lat) = ('n/a');
  my($REPORTSDIR) = $ENV{EMS_LOG_DIR}. "/reports/$currentepoch";
  my($LEVEL0URL) = $main::EMSCONFIG{LEVEL0WMSURL};
  my($url, $mailmessage);

  my($midsvcs) = Midsvcs->load;

  my(@sourcedbs) = split /,/, $midsvcs->{databases};
  my($sourcedb) = $query->param('sourcedb') || $db;

  my(@hostports) = split /,/, $midsvcs->{'meme-server-hostport'};
  my($defaulthost) = $midsvcs->{'meme-server-host'};
  my($defaultport) = $midsvcs->{'meme-server-port'};
  my($hostport) = $query->param('meme-server-hostport') || join(":", $defaulthost, $defaultport);

  &printhtml({body=>'ERROR: Need a worklist or checklist name.', printandexit=>1}) unless @list>0;

  unless (-e $REPORTSDIR) {
    unless (mkpath($REPORTSDIR, 0, 0775)) {
      $html .= <<"EOD";
Cannot create the reports directory: $REPORTSDIR.  Check the permissions and retry.
EOD
      &printhtml({title=>'Generate report', h1=>'Generate report', body=>$html, printandexit=>1});
      return;
    }
    unless (chmod 0775, $REPORTSDIR) {
      $html .= <<"EOD";
Cannot chmod the reports directory: $REPORTSDIR.  Check the permissions and retry.
EOD
      &printhtml({title=>'Generate report', h1=>'Generate report', body=>$html, printandexit=>1});
      return;
    }
  }

  unless ($query->param('doit')) {
    push @$emails, "n/a";
    $sql = "select distinct a.language from sims_info a, source_version b where a.source=b.current_name and a.language is not null";
    @lat = $dbh->selectAllAsArray($sql);
    if (@list == 1) {
      $html .= "Generates a report for " . $query->em($list) . ". ";
    } else {
      $html .= "Generates reports for all the following worklists or checklists." . $query->p . $list .     $html .= $query->p;
    }
    $html .= <<"EOD";
The report files are stored in \$EMS_HOME/log.  A report request can
be queued for generation or generated inline in the browser for convenient printing.
EOD
    $html .= $query->p;
    $html .= $query->start_form({-method=>'POST', -action=>$query->url(-absolute=>1)});
    push @d, ["Use data source: ", $query->popup_menu({-name=>'sourcedb', -values=>\@sourcedbs, -default=>$sourcedb}) . "\n" ];
    push @d, ["Use MEME server/port: ", $query->popup_menu({-name=>'meme-server-hostport', -values=>\@hostports, -default=>$hostport}) . "\n" ];
    push @d, ["Start the generator in: ", $query->popup_menu({-name=>'whentorun', -values=>\@hrs}) . " hours\n" ];
    push @d, ["Optionally, notify me after these reports have been generated at: ", $query->popup_menu({-name=>'mailto', -values=>$emails, -default=>'n/a'}) . "\n" ];
    push @d, ["Type of report: ", $query->popup_menu({-name=>'reporttype', -values=>\@reporttype, -labels=>\%reportlabel}) . "\n" ];
    push @d, ["Limit reviewed relationships to: ", $query->popup_menu({-name=>'maxreviewedrels', -values=>\@maxreviewedrels}) . "\n" ];
    push @d, ["In reports, limit atoms to these languages: ", $query->scrolling_list({-name=>'lat', -values=>\@lat, -size=>4, -default=>\@lat, multiple=>1}) . "\n" ];
    $html .= &toHTMLtable($query, {border=>1, -cellpadding=>5, -cellspacing=>0}, \@d);

    $html .= $query->br;
    $html .= $query->submit({-value=>"Submit for generation"});
    $html .= " ";
    $html .= $query->submit({-name=>'reportinbrowser', -value=>"Generate report in browser"});
    $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->hidden(-name=>'list', -value=>\@list, -override=>1);
    $html .= $DBpost;
    $html .= $query->end_form;
    &printhtml({title=>'Generate report', h1=>'Generate report', body=>$html});
    return;
  }

# legitimate email address?
  if ($query->param('mailto') && $query->param('mailto') ne "n/a") {
    my($e) = $query->param('mailto');
    &printhtml({printandexit=>1, body=>"ERROR: the email address specified: $e is not on the allowed list."})
      unless grep {lc($_) eq lc($e)} @$emails;
  }

  $url = $LEVEL0URL . "?$DBget&action=reportqueue";
  if (@list > 1) {
    $mailmessage .= "Reports for the following worklists and checklists have completed: " . $query->p . $query->ul($query->li(\@list)) . $query->p;
  } else {
    $mailmessage .= "Report for $list has been completed.  Refer to the report queue for details.";
  }
  my($lastlist) = $list[$#list];

  my($ordernum) = 1;
  foreach $list (@list) {
    my(%request);

    $request{list} = $list;
    $request{db} = $sourcedb;
    $request{dbh} = $dbh;
    $request{env_home} = $ENV{ENV_HOME};
    $request{env_file} = $ENV{ENV_FILE};
    $request{meme_host} = ((split /:/, $hostport)[0]) || $defaulthost;
    $request{meme_port} = ((split /:/, $hostport)[1]) || $defaultport;

    $request{reportfile} = sprintf("%s/%s.%s", $REPORTSDIR, $list, "rpt");
    $request{reporttype} = $query->param('reporttype') || 1;
    $request{whentorun} = $query->param('whentorun') ? (time + 3600*$query->param('whentorun') - 1) : (time-1);
    $request{ordernum} = $ordernum++;
    $request{requestuser} = $httpuser || $unixuser;

    if ($query->param('mailto') ne "n/a") {
      if ($list eq $lastlist) {
	$request{mailto} = $query->param('mailto') if $query->param('mailto') && $query->param('mailto') ne "n/a";
	$request{mailmessage} = $mailmessage if $mailmessage;
      }
    }

    $request{maxreviewedrels} = $query->param('maxreviewedrels') if $query->param('maxreviewedrels') ne "n/a";
    $request{lat} = join(",", $query->param('lat')) if $query->param('lat');

    if ($query->param('reportinbrowser')) {
      delete $request{reportfile};
      $html .= $query->pre(EMSReportRequest->report($dbh, \%request));
      unlink $request{freezefile} if -e $request{freezefile};
    } else {
      EMSReportRequest->freeze(\%request);
    }
  }

  if ($query->param('reportinbrowser')) {
    &printhtml({title=>"Report for $list", h1=>"Report for $list", body=>$html});
    return;
  }

# start the report generator
#  $cmd = $ENV{EMS_HOME} . "/bin/reportqueue.pl" . ($query->param('config') ? " -c " . $ENV{EMS_CONFIG} : "");
#  system $cmd;
#  sleep 4;
  $url = $query->a({-href=>$query->url(). "?$DBget&action=reportqueue"}, "report queue");
  $html .= <<"EOD";
The report request for $list was submitted.  Please check
the $url periodically to check its status.
EOD
  &printhtml({body=>$html});
}
1;
