# suresh@nlm.nih.gov - 3/2000

# Status of requests to the report daemon
# CGI params:
# db=
# start= (where to start from)
# n= (how many to show)
sub do_reportqueue {
  my($REPORTSDIR) = $ENV{EMS_HOME} . "/log";
  my($html);
  my($limit) = 100;
  my($n) = $query->param('n') || 10;
  my($start) = $query->param('start') || 1;
  my($request, @requests);
  my($nextstart) = $start + $n;
  my($prevstart) = $start - $n;
  my($nexturl, $prevurl);
  my(@d, @d0);
  my($i) = $start;

  @requests = EMSReportRequest->getAllRequestsIgnoreStatus($start, $start+$n);

  unless (@requests) {
    $html .= "There were no (more) report requests found.";
    &printhtml({body=>$html, printandexit=>1});
  }

  $nexturl = $query->url() . "?$DBget&action=$action&start=$nextstart&n=$n" if @requests == $n;
  $prevurl = $query->url() . "?$DBget&action=$action&start=$prevstart&n=$n" if $start != 1;

  @d0 = ();
  push @d0, ($prevurl ? [ {-align=>'left'},  $query->a({-href=>$prevurl}, "Prev") ] : []);
  push @d0, ($nexturl ? [ {-align=>'right'}, $query->a({-href=>$nexturl}, "Next") ] : []);

  $html .= &toHTMLtable($query, {-border=>0, width=>'600', -cellpadding=>0, -cellspacing=>0}, [\@d0]);
  $html .= $query->p;

  @d = ();
  push @d, ['', "Report for", "Status", "Action", "Miscellaneous"];

  foreach $request (@requests) {
    my(@d1) = ();
    push @d1, [ { -align=>'right' }, $i ];
    $i++;

    $_ = $request->{list} || $request->{worklist} || $request->{checklist};
    if (/^wrk/i) {
      $y = $_;
      $y =~ s/_\d+$//;
      $x = $query->a({-href=>$EMSCONFIG{LEVEL0WMSURL} . "?$DBget&action=wms_query&namelike=$y&doit=1"}, $_);
    } elsif (/^chk/i) {
      $x = $query->a({-href=>$EMSCONFIG{LEVEL0WMSURL} . "?$DBget&action=pickcheck"}, $_);
    } else {
      $x = $_;
    }

    push @d1, $x;
    push @d1, &status($request->{status});
    push @d1, $query->a({-href=>$query->url() . "?$DBget&action=reportqueuedel&start=$start&n=$n&freezefile=" . basename($request->{freezefile})}, "Delete");

    push @d1, &data_for($request);
    push @d, \@d1;
  }
      
  $html .= $query->p;
  $html .= "Select the link to retrieve WMS information for this and similar worklists or navigate to the WMS checklist page.";
  $html .= $query->p;
  $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>5, -cellspacing=>2}, \@d, "firstrowisheader");
  $html .= $query->p;

  $html .= &toHTMLtable($query, {-border=>0, width=>'600', -cellpadding=>0, -cellspacing=>0}, [\@d0]);
  &printhtml({h1=>"Report Queue", body=>$html});
}

# returns the data for a request
sub data_for {
  my($request) = @_;
  my(@d);
  my($p) = $request->{reportfile};

  $p =~ s/^$ENV{EMS_HOME}/\$EMS_HOME/;
  push @d, [ $query->font(-color=>'red', "ERROR: "), $request->{errmsg}] if $request->{errmsg};
  push @d, [ "Report file: ", $p];
  push @d, [ "Freeze file: ", basename($request->{freezefile})];
  push @d, [ "Report type: ", $request->{reporttype}];
  push @d, [ "Request user: ", $request->{requestuser}];
  push @d, [ "Request date: ", $request->{requestdate}];
  if ($request->{dateended}) {
    push @d, [ "Request started: ", $request->{datestarted}];
    push @d, [ "Request ended: ", $request->{dateended}];
    push @d, [ "Time taken: ", GeneralUtils->sec2hms($request->{timeended} - $request->{timestarted}) ];
  }
  push @d, [ "Mail to: ", $request->{mailto} ] if $request->{mailto};
  push @d, [ "Database: ", $request->{db}];
  push @d, [ "MEME parameters: ", $request->{meme_host} . ":" . $request->{meme_port}];

  push @d, [ "Max reviewed rels: ", $request->{maxreviewedrels}] if $request->{maxreviewedrels};
#  push @d, [ "LAT restriction: ", $request->{lat} ] if $request->{lat};
  foreach $r (@d) {
    push @d1, join("", $r->[0], $r->[1]);
  }
  return $query->start_form . $query->popup_menu(-values=>\@d1) . $query->end_form;
#  return &toHTMLtable($query, {-border=>0, -cellpadding=>1, -cellspacing=>0}, \@d);
}

sub status {
  my($status) = @_;
  my(%s) = (
	    WAITING=>'yellow',
	    RUNNING=>'red',
	    ERROR=>'red',
	    DONE=>'black'
	    );
  
  return ($s{$status} ? $query->font({-color=>$s{$status}}, $status) : $status);
}
1;
