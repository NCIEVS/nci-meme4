# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000
# EMS3 - 1/2006

# Produces reports of editing progress; data for the reports
# are gathered daily by cron'ed scripts

# CGI params:
# db=
# config=
# yyyy=
# mm=
# dd=
# date=YYYYMMDD (alternate YYYYMMDD)

sub do_daily_report {
  my($sql);
  my($html);
  my(@months) = qw(Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec);
  my($title) = "Daily Editing Report";
  my($SNAPSHOTTABLE) = $EMSNames::DAILYSNAPSHOTTABLE;
  my($ACTIONTABLE) = $EMSNames::DAILYACTIONCOUNTTABLE;
  my($doit) = $query->param('yyyy') && $query->param('mm') && $query->param('dd');

  unless ($doit) {

# find earliest year
    $sql = "select to_char(min(snapshot_date), 'YYYY') from $SNAPSHOTTABLE"; 
    my($minyear) = $dbh->selectFirstAsScalar($sql);

    my(%monthlabels);
    for (my $i=0; $i<@months; $i++) {
      $monthlabels{$i+1} = $months[$i];
    }

# default to yesterday
    my(@y) = localtime time-24*60*60;

    $yyyy = $y[5]+1900;
    $mm = $y[4]+1;
    $dd = $y[3];

    my(@years) = ($minyear .. $yyyy);

    $html .= <<"EOD";
This form produces an editing report for the day specified.
<P>
EOD

    $html .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $html .= $DBpost;

    $html .= "Show daily report for: ";
    $html .= $query->popup_menu({-name=>'yyyy', -default=>$yyyy, -values=>\@years});
    $html .= $query->popup_menu({-name=>'mm', -default=>$mm, -values=>[1..12], -labels=>\%monthlabels});
    $html .= $query->popup_menu({-name=>'dd', -default=>$dd, -values=>[1..31]});

    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $html .= $query->submit;
    $html .= $query->end_form;

    &printhtml({title=>$title, h1=>$title, body=>$html});

  } else {

    $yyyy = sprintf("%4d", $query->param('yyyy'));
    $mm = sprintf("%.2d", $query->param('mm'));
    $dd = sprintf("%.2d", $query->param('dd'));

    $theday = $months[$mm-1] . " $dd, $yyyy";

# quoted forms
    my($ymd) = $dbh->quote(join('-', $yyyy, $mm, $dd));
    my($fmt) = $dbh->quote('YYYY-MM-DD');

    @rows = ();

    $html .= $query->h1("Daily snapshot data");

# Any data gathered for this day?
    $sql = "select count(snapshot_date) from $SNAPSHOTTABLE where to_char(snapshot_date, $fmt) = $ymd";
    $snapshot_count = $dbh->selectFirstAsScalar($sql);
    if ($snapshot_count == 0) {

      &printhtml({printandexit=>1, body=>"There was no snapshot data collected for $theday"});

    } else {

      $q = $dbh->quote("CS");

      my($t, $x, %count, $r, @x);

      $t = 0;
      $sql = <<"EOD";
select snapshot_attr, snapshot_count from $SNAPSHOTTABLE
where  to_char(snapshot_date, $fmt) = $ymd
and    snapshot_type=$q
EOD
      foreach $r ($dbh->selectAllAsRef($sql)) {
	$count{$r->[0]} = $r->[1];
	$t += $r->[1];
      }

      @x = (["Status", "Concepts"]);
      foreach $x (qw(R N U E)) {
	push @x, [$x . ": ", [{-align=>'right'}, $count{$x} || 0] ];
      }
      push @x, ["Total: ", [{-align=>'right'}, $t] ];
      push @rows, ["Counts by concept status", &toHTMLtable($query, {-border=>1, -width=>'100%', -cellpadding=>5, -cellspacing=>1}, \@x, "firstrowisheader")];

# approvals
      @x = ();
      $q = $dbh->quote("APPR");
      $sql = <<"EOD";
select snapshot_count from $SNAPSHOTTABLE
where  to_char(snapshot_date, $fmt) = $ymd
and    snapshot_type=$q
EOD
      push @x, ["All: ", [{-align=>'right'}, $dbh->selectFirstAsScalar($sql)]];

      $q = $dbh->quote("DISTAPPR");
      $sql = <<"EOD";
select snapshot_count from $SNAPSHOTTABLE
where  to_char(snapshot_date, $fmt) = $ymd
and    snapshot_type=$q
EOD
      push @x, ["Distinct: ", [{-align=>'right'}, $dbh->selectFirstAsScalar($sql)]];

      push @rows, ["Concepts approved on $theday", &toHTMLtable($query, {-border=>1, -width=>'100%', -cellpadding=>5, -cellspacing=>1}, \@x)];

      @x = ();
      $q = $dbh->quote("APPR");
      $yyyyq = $dbh->quote($yyyy);

      $sql = <<"EOD";
select sum(snapshot_count) from $SNAPSHOTTABLE
where  to_char(snapshot_date, 'YYYY') = $yyyyq
and    snapshot_type=$q
EOD
      push @x, ["All: ", [{-align=>'right'}, $dbh->selectFirstAsScalar($sql)]];

      $q = $dbh->quote("DISTAPPR");
      $sql = <<"EOD";
select snapshot_count from $SNAPSHOTTABLE
where  to_char(snapshot_date, 'YYYY') = $yyyyq
and    snapshot_type=$q
EOD
      push @x, ["Distinct: ", [{-align=>'right'}, $dbh->selectFirstAsScalar($sql)]];

      push @rows, ["Concepts approved year-to-date", &toHTMLtable($query, {-border=>1, -width=>'100%', -cellpadding=>5, -cellspacing=>1}, \@x)];

      $html .= &toHTMLtable($query, {-border=>1, -width=>'80%', -cellpadding=>5, -cellspacing=>1}, \@rows);
    }

# by Authority
    $html .= $query->h1("Actions summary by authority");
    $sql = "select count(report_date) from $ACTIONTABLE where to_char(report_date, $fmt) = $ymd";
    if ($dbh->selectFirstAsScalar($sql) == 0) {

      $html .= "There was no action data collected for $theday";

    } else {

      @rows = ();
      %x = ();
      @x = ();
      $n=0;

      push @rows, ['', "Authority", "Actions", "Touched", "Approved", "Rels", "STYs", "Splits", "Merges"];

      $sql = <<"EOD";
SELECT  authority, total_actions, concepts_touched, concepts_approved,
       rels_inserted, stys_inserted, splits, merges from $ACTIONTABLE
WHERE  to_char(report_date, $fmt) = $ymd
ORDER BY total_actions DESC
EOD
      foreach $r ($dbh->selectAllAsRef($sql)) {
	$n++;
        push @rows, [
          [{-align=>'right'}, $n],
	  $r->[0],
	  [{-align=>'right'}, $r->[1] || 0],
	  [{-align=>'right'}, $r->[2] || 0],
	  [{-align=>'right'}, $r->[3] || 0],
	  [{-align=>'right'}, $r->[4] || 0],
	  [{-align=>'right'}, $r->[5] || 0],
	  [{-align=>'right'}, $r->[6] || 0],
	  [{-align=>'right'}, $r->[7] || 0],
        ];

	push @rows, \@x;
      }
      $html .= &toHTMLtable($query, {-border=>1, -width=>'80%', -cellpadding=>5, -cellspacing=>1}, \@rows);
    }

    if ($snapshot_count > 0) {
      $html .= $query->h1("STY distribution for concepts approved on $theday");
      $html .= "The following table shows the Semantic Type distribution of the concepts approved on this day in reverse frequency order.";
      $html .= $query->p;

      $q = $dbh->quote("STY");
      $sql = <<"EOD";
SELECT snapshot_attr, snapshot_count from $SNAPSHOTTABLE
WHERE  snapshot_type=$q
AND    to_char(snapshot_date, $fmt) = $ymd
ORDER BY snapshot_count desc
EOD

      $n = 0;
      @rows = ();
      push @rows, ['', 'Semantic Type', 'Count'];
      foreach $r ($dbh->selectAllAsRef($sql)) {
	$n++;
	push @rows, [[{-align=>'right'}, $n], $r->[0], [{align=>'right'}, $r->[1]]];
      }
      $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>5, -cellspacing=>1, -width=>'80%'}, \@rows, "firstrowisheader");

      $html .= $query->h1("Source distribution for concepts approved on $theday");
      $html .= "The following table shows the Source (SAB) distribution of the atoms in the concepts approved on this day in reverse frequency order.";
      $html .= $query->p;

      $n = 0;
      $q = $dbh->quote("SO");
      $sql = <<"EOD";
SELECT snapshot_attr, snapshot_count from $SNAPSHOTTABLE
WHERE  snapshot_type=$q
AND    to_char(snapshot_date, $fmt) = $ymd
ORDER BY snapshot_count desc
EOD

      @rows = ();
      push @rows, ['', 'Source', 'Count'];
      foreach $r ($dbh->selectAllAsRef($sql)) {
	$n++;
	push @rows, [[{-align=>'right'}, $n], $r->[0], [{align=>'right'}, $r->[1]]];
      }
      $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>5, -cellspacing=>1, -width=>'80%'}, \@rows, "firstrowisheader");

    }

    $title = "Daily editing report for $theday";
    &printhtml({title=>$title, h1=>$title, body=>$html});
  }
}
1;
