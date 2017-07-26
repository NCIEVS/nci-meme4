# Display checklists
# suresh@nlm.nih.gov 12/98
# suresh@nlm.nih.gov 7/05 - EMS3 mods

# CGI params
# oldest_create_date=
# sort_by=
# order_by=
# owner=
# max=

sub do_pickcheck {
  my($html);
  my($wmsurl) = $main::EMSCONFIG{LEVEL0WMSURL};
  my($maxval) = "100000";
  my(@max) = ('n/a', 50, 100, 200, 500);
  my($defaultmax) = 50;
  my(@datevalues) = ('n/a', '1 month', '6 months', '1 year', '2 years', '5 years', 'All');
  my($defaultdate) = 'n/a';

  my(@owner);
  my($defaultowner) = "n/a";
  my($owner) = $query->param('owner') || $defaultowner;
  my(@sort_by) = qw(owner checklist_name create_date);
  my($defaultsort) = "create_date";
  my($sort_by) = $query->param('sort_by') || $defaultsort;
  my(@order_by) = qw(asc desc);
  my($defaultorder) = "desc";
  my($order_by) = $query->param('order_by') || $defaultorder;
  my($max) = $query->param('max') || $defaultmax;
  $max = $maxval if $max eq "All";
  my($oldest_create_date) = $query->param('oldest_create_date') || "n/a";
  my($CHECKLISTINFO) = $EMSNames::CHECKLISTINFOTABLE;
  my($sql);
  my(@d);
  my($oldest_in_days);

  $oldest_in_days = $maxval if $oldest_create_date eq "All";
  $oldest_in_days = $1*30 if ($oldest_create_date =~ /(^\d+)\s+mon/i);
  $oldest_in_days = $1*30*12 if ($oldest_create_date =~ /(^\d+)\s+year/i);

  $sql = "select distinct owner from $CHECKLISTINFO order by owner";
  @owner = $dbh->selectAllAsArray($sql);
  unshift @owner, $defaultowner;

  $sql = "select count(checklist_name) from $CHECKLISTINFO where 1=1";
  $sql .= " and create_date > SYSDATE-$oldest_in_days" if ($oldest_create_date ne "n/a");
  $sql .= " and owner=" . $dbh->quote($owner) if ($owner ne "n/a");
  $num = $dbh->selectFirstAsScalar($sql);

  if ($num == 0) {
    &printhtml({title=>$wmstitle, h1=>$wmstitle, body=>"There are no matching checklists.  Please refine the query and try again.", printandexit=>1});
  } elsif ($num == 1) {
    $html .= "There is one checklist known to the WMS shown below.";
  } else {
    if ($query->param('max') eq "n/a" || $query->param('max') eq "All" || $max>$num) {
      $html .= "There are $num checklists, all of which are shown below.";
    } else {
      $html .= "The first $max of $num checklists are shown below.";
    }
  }
  $html .= $query->p;

  $html .= $query->start_form(-method=>'POST', -action=>$wmsurl);
  push @d, ["Show checklists created in the past: ", $query->popup_menu({-name=>'oldest_create_date', -values=>\@datevalues, -labels=>\%datelabels, -default=>$defaultdate})];
  push @d, ["Show checklists owned by: ", $query->popup_menu({-name=>'owner', -values=>\@owner}, -default=>$owner)];
  push @d, ["Sort by: ", $query->popup_menu({-name=>'sort_by', -values=>\@sort_by, -default=>$sort_by}) . " " . $query->popup_menu({-name=>'order_by', -values=>\@order_by, -default=>$order_by})];
  push @d, ["Show the top: ", $query->popup_menu({-name=>'max', -default=>$defaultmax, -values=>\@max}) . " matches"];

  $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
  $html .= &toHTMLtable($query, {border=>0, cellspacing=>0, cellpadding=>10}, [[&toHTMLtable($query, {border=>1, cellspacing=>1, cellpadding=>1}, \@d), $query->submit({-name=>'Refresh List'})]]);
  $html .= $DBpost;
  $html .= $query->end_form;
  $html .= $query->p;
  $html .= $query->hr;
  $html .= $query->p;

  $html .= $query->start_form(-method=>'POST', -action=>$main::EMSCONFIG{LEVEL2WMSURL});
  $html .= $query->hidden(-name=>'action', -value=>'deletecheck', -override=>1);
  $html .= $DBpost;
  $html .= $query->submit({-name=>'Delete selected checklists (' . $main::EMSCONFIG{LEVEL2NICKNAME} . ')'});
  $html .= $query->p;

  my(@cols) = map { lc } EMSTables->columns($CHECKLISTINFO);
  my($cols) = join(',', @cols);
  my($r, $h, $n);

  $sql = "select $cols from $CHECKLISTINFO where 1=1";
  $sql .= " and create_date > SYSDATE-$oldest_in_days" if ($oldest_create_date ne "n/a");
  $sql .= " and owner=" . $dbh->quote($owner) if ($owner ne "n/a");
  $sql .= "  order by $sort_by $order_by";
  $sql = "select * from ($sql) where rownum<=$max" unless $max eq "n/a";

  my($cb);
  @d = ();
  push @d, ['', '', "Checklist", "Concepts", "Clusters", "Action", "Info"];
  foreach $r ($dbh->selectAllAsRef($sql)) {
    $h = $dbh->row2ref($r, @cols);
    $n++;
    $cb = $query->checkbox({-name=>'checklist', -value=>$h->{checklist_name}, -label=>''});
    push @d, [$n, $cb, $h->{checklist_name}, $h->{concepts}, $h->{clusters}, &actions($h), &info($h)];
  }
  $html .= &toHTMLtable($query, {border=>1, cellspacing=>1, cellpadding=>5, width=>'90%'}, \@d, "firstrowisheader");
  $html .= $query->end_form;
  &printhtml({title=>"WMS: Checklists", h1=>"View or delete checklists", body=>$html});
}

sub info {
  my($h) = @_;
  my(@d);
  push @d, ["Created on: ", $h->{create_date}];
  push @d, ["Created by: ", $h->{owner}];
  if ($h->{bin_name}) {
    push @d, ["Parent Bin: ", $h->{bin_name} . " (" . $h->{bin_type} . ")"];
  } else {
    push @d, ["No parent Bin", ''];
  }
  return &toHTMLtable($query, {-border=>0, -cellpadding=>0, -cellspacing=>0}, \@d);
}

sub actions {
  my($h) = @_;
  my(@d);
  push @d, [$query->a({href=>$query->url() . "?action=view&checklist=$h->{checklist_name}&$DBget"}, "View")];
  push @d, [$query->a({href=>$query->url() . "?action=report&checklist=$h->{checklist_name}&$DBget"}, "Report")];
  push @d, [$query->a({href=>$main::EMSCONFIG{LEVEL2WMSURL} . "?action=deletecheck&checklist=$h->{checklist_name}&$DBget"}, "Delete (" . $main::EMSCONFIG{LEVEL2NICKNAME} . ")")];
  return &toHTMLtable($query, {-border=>0, -cellpadding=>0, -cellspacing=>0}, \@d);
}

1;
