# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000
# EMS3 - 12/2005

# Query for worklists using a variety of criteria

# CGI params:
# db=
# doit=
# epoch=
# prefix=
# namelike=
# grp=
# editor=
# fromnum=
# tonum=
# worklist_status=
# date specific criteria and range fields, e.g., create_date_criterion, create_date_year1, etc

sub do_wms_query {
  my($sql);
  my($html);
  my($EDITINGEPOCHTABLE) = $EMSNames::EDITINGEPOCHTABLE;
  my($WORKLISTINFOTABLE) = $EMSNames::WORKLISTINFOTABLE;
  my(@d);
  local($na) = "--n/a--";
  local($null) = "--null--";
  %statuslabels = (
		   CR=>'Created',
		   ED=>'Being edited',
		   RT=>'Returned',
		   ST=>'Stamped',
		   DO=>'Done'
		  );

  unless ($query->param('doit')) {
    my(@colsforsort);
    
    push @colsforsort, 'worklist_name';
    push @colsforsort, 'grp';
    push @colsforsort, 'editor';
    push @colsforsort, 'create_date';
    push @colsforsort, 'assign_date';
    push @colsforsort, 'return_date';
    push @colsforsort, 'stamp_date';
    push @colsforsort, 'worklist_status';
    push @colsforsort, 'edit_time';

    $sql = "select min(to_char(create_date, 'YYYY')) from $WORKLISTINFOTABLE";
    my($earliestyear) = $dbh->selectFirstAsScalar($sql);

    $sql = <<"EOD";
select count(a.worklist_name) from $WORKLISTINFOTABLE a, $EDITINGEPOCHTABLE b
where  upper(substr(b.epoch, 1, 3)) = upper(substr(a.worklist_name, 4, 3))
and    b.active='Y'
EOD
    my($activeworklists) = $dbh->selectFirstAsScalar($sql);
    my($javascript) = GeneralUtils->file2str($ENV{EMS_HOME} . "/src/wms_query.js");

    $html .= <<"EOD";
Select from the different criteria and submit the form to retrieve
information about matching worklists.  There are currently $activeworklists
worklists in active epochs meeting the default criteria.

$javascript
EOD

    $html .= $query->start_form(-name=>"queryform", -method=>'POST', -action=>$query->url(-absolute=>1));
    $html .= $DBpost;
    $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);

    $html .= $query->p;
    $html .= $query->hr;
    $html .= "Order results by: " .
      $query->popup_menu({-name=>'sortbytop', OnChange=>'syncBOTTOM();', -values=>\@colsforsort}) .
      $query->popup_menu({-name=>'ascdesctop', OnChange=>'syncBOTTOM();', -values=>['ASC', 'DESC']});
    $html .= $query->p;
    $html .= $query->submit;
    $html .= $query->reset;

    $html .= $query->p;

    @x = ();
    @x = $dbh->selectAllAsArray("select epoch from $EDITINGEPOCHTABLE where active=" . $dbh->quote("Y") . " order by epoch desc");
    unshift @x, $na;
    push @d, [&imgurl("Worklist was created in this editing epoch") . "Created in epoch: ", $query->popup_menu({-name=>'epoch', -default=>$na, -values=>[ @x ]})];

    @x = ();
    @x = $dbh->selectAllAsArray(<<"EOD");
select distinct substr(worklist_name, 1, instr(worklist_name, '_', -1, 1)-1) from $WORKLISTINFOTABLE
where  worklist_status != 'DO'
and    worklist_status != 'ST'
EOD
    unshift @x, $na;
    push @d, [&imgurl("Unstamped worklists with names that start with") . "Active worklists with prefix: ", $query->popup_menu({-name=>'prefix', -default=>$na, -values=>[ @x ]})];

    push @d, [&imgurl("The name of the worklist matches %X% where X is the specified pattern") . "Name like: ", $query->textfield({-name=>'namelike', -size=>15})];
    push @d, [&imgurl("Worklist numbers in this range (inclusive)") . "Numbers from: ", $query->textfield({-name=>'fromnum', -size=>4}) . " to " . $query->textfield({-name=>'tonum', -size=>4})];

    @x = ();
    @x = $dbh->selectAllAsArray(<<"EOD");
select distinct grp from $WORKLISTINFOTABLE where extract(year from assign_date) in (
(extract (year from sysdate)),
(extract (year from sysdate)-1)
)
EOD
    unshift @x, $na;
    unshift @x, $null;
    push @d, [&imgurl("Worklists assigned to this editing group") . "Edited by group: ", $query->popup_menu({-name=>'grp', -default=>$na, -values=>[ @x ]})];

    @x = ();
    @x = $dbh->selectAllAsArray(<<"EOD");
select distinct editor from $WORKLISTINFOTABLE where exists (select 1 from editors where editor=initials and cur='Y') order by editor
EOD
    unshift @x, $na;
    unshift @x, $null;
    push @d, [&imgurl("Worklists assigned to this editor") . "Edited by editor: ", $query->popup_menu({-name=>'editor', -default=>$na, -values=>[ @x ]})];

    push @d, [
	      &imgurl("Worklists with create_date in this range or with this criteria") . "Create date is: " . &date_criterion("create_date"),
	      &date_fields("create_date", $earliestyear)
	     ];

    push @d, [
	      &imgurl("Worklists with assign_date in this range or with this criteria") . "Assign date is: " . &date_criterion("assign_date"),
	      &date_fields("assign_date", $earliestyear)
	     ];

    push @d, [
	      &imgurl("Worklists with return_date in this range or with this criteria") . "Return date is: " . &date_criterion("return_date"),
	      &date_fields("return_date", $earliestyear)
	     ];

    push @d, [
	      &imgurl("Worklists with stamp_date in this range or with this criteria") . "Stamp date is: " . &date_criterion("stamp_date"),
	      &date_fields("stamp_date", $earliestyear)
	     ];

    @x = ();
    @x = $dbh->selectAllAsArray(<<"EOD");
select distinct worklist_status from $WORKLISTINFOTABLE
EOD
    unshift @x, $na;
    push @d, [&imgurl("Worklists with this status") . "With status: ", $query->popup_menu({-name=>'worklist_status', -default=>$na, -values=>[ @x ], -labels=>\%statuslabels})];

    $html .= &toHTMLtable($query, {-border=>1, -cellpadding=>10, -cellspacing=>0, -width=>'90%'}, \@d);
	     
    $html .= $query->p;
    $html .= "Order results by: " .
      $query->popup_menu({-name=>'sortbybottom', OnChange=>'syncTOP();', -values=>\@colsforsort}) .
      $query->popup_menu({-name=>'ascdescbottom', OnChange=>'syncTOP();', -values=>['ASC', 'DESC']});
    $html .= $query->p;
    $html .= $query->submit;
    $html .= $query->reset;

    $html .= $query->end_form;
  
  } else {

    my(@whereclause);

    push @whereclause, "upper(worklist_name) like " . $dbh->quote(uc("WRK" . $query->param('epoch') . '%')) if ($query->param('epoch') && $query->param('epoch') ne $na);
    push @whereclause, "worklist_name like " . $dbh->quote($query->param('prefix') . '%') if ($query->param('prefix') && $query->param('prefix') ne $na);
    push @whereclause, "worklist_name like " . $dbh->quote('%' . $query->param('namelike') . '%') if ($query->param('namelike') && $query->param('namelike') ne $na);
    push @whereclause, "substr(worklist_name, instr(worklist_name, '_', -1, 1)+1) >= " . $query->param('fromnum') if ($query->param('fromnum') && $query->param('fromnum') =~ /^\d+$/);
    push @whereclause, "substr(worklist_name, instr(worklist_name, '_', -1, 1)+1) <= " . $query->param('tonum') if ($query->param('tonum') && $query->param('tonum') =~ /^\d+$/);

    if ($query->param('grp') && $query->param('grp') ne $na) {
      push @whereclause, ($query->param('grp') eq $null ? "grp is null" : "grp=" . $dbh->quote($query->param('grp')));
    }
    if ($query->param('editor') && $query->param('editor') ne $na) {
      push @whereclause, ($query->param('editor') eq $null ? "editor is null" : "editor=" . $dbh->quote($query->param('editor')));
    }

    if ($query->param('create_date_criterion') eq "null") {
      push @whereclause, "create_date is null";
    } elsif ($query->param('create_date_criterion') eq "not null") {
      push @whereclause, "create_date is not null";
    } elsif ($query->param('create_date_criterion') eq "between") {
      push @whereclause, &date_whereclause($dbh, "create_date");
    }

    if ($query->param('assign_date_criterion') eq "null") {
      push @whereclause, "assign_date is null";
    } elsif ($query->param('assign_date_criterion') eq "not null") {
      push @whereclause, "assign_date is not null";
    } elsif ($query->param('assign_date_criterion') eq "between") {
      push @whereclause, &date_whereclause($dbh, "assign_date");
    }

    if ($query->param('return_date_criterion') eq "null") {
      push @whereclause, "return_date is null";
    } elsif ($query->param('return_date_criterion') eq "not null") {
      push @whereclause, "return_date is not null";
    } elsif ($query->param('return_date_criterion') eq "between") {
      push @whereclause, &date_whereclause($dbh, "return_date");
    }

    if ($query->param('stamp_date_criterion') eq "null") {
      push @whereclause, "stamp_date is null";
    } elsif ($query->param('stamp_date_criterion') eq "not null") {
      push @whereclause, "stamp_date is not null";
    } elsif ($query->param('stamp_date_criterion') eq "between") {
      push @whereclause, &date_whereclause($dbh, "stamp_date");
    }

    if ($query->param('worklist_status') && $query->param('worklist_status') ne $na) {
      push @whereclause, "worklist_status=" . $dbh->quote($query->param('worklist_status'));
    }

    my($sortby) = $query->param('sortbytop') || "worklist_name";
    my($orderby) = $query->param('orderbytop') || "ASC";
    my($whereclause) = (@whereclause ? "where " . join(' and ', @whereclause) : "");

    my(@cols) = EMSTables->columns($WORKLISTINFOTABLE);
    $sql = "select " . join(", ", @cols) . ", TO_NUMBER(SUBSTR(worklist_name, 1+INSTR(worklist_name, '_', -1))) AS worklist_num from $WORKLISTINFOTABLE $whereclause order by $sortby $orderby, worklist_num asc";
    my(@matches) = $dbh->selectAllAsRef($sql);
    push @cols, "worklist_num";

    my($ref, $i, $w, $num_matches);
    my(@worklists);

    $num_matches = @matches;
    @d = ();
    push @d, ['', 'Worklist', 'Group', 'Editor', 'Concepts (Clusters)', "Dates", "Status", "Actions", "Edit time"];
    foreach $ref (@matches) {
      $w = $dbh->row2ref($ref, @cols);
      push @worklists, $w->{worklist_name};
      $i++;
      push @d, [ [{-align=>'right'}, $i],
		 $w->{worklist_name},
		 $w->{grp},
		 $w->{editor},
		 $w->{n_concepts} . " (" . $w->{n_clusters} . ")",
		 &toHTMLtable($query, {-border=>0}, [
						     ["Create date: ", $w->{create_date}],
						     ["Assign date: ", $w->{assign_date}],
						     ["Return date: ", $w->{return_date}],
						     ["Stamp date: ",  $w->{stamp_date}],
						    ]),
		 $statuslabels{$w->{worklist_status}},
		 &actions_table($w),
		 ($w->{edit_time} == -1 ? "n/a" : GeneralUtils->sec2hms($w->{edit_time}))
	       ];
    }

    if ($num_matches == 0) {
      $html .= "There were no matches to your query.  Please retry.";
    } elsif ($num_matches == 1) {
      $html .= "The following worklist matched your query.";
      $html .= $query->p;
      $html .= &toHTMLtable($query, {-border=>1, -cellspacing=>0, -cellpadding=>5}, \@d, "firstrowisheader");
    } else {
      $html .= "The following $num_matches worklists matched your query.";
      $html .= $query->p;
      $html .= &update_forms(@worklists);
      $html .= $query->p;
      $html .= &toHTMLtable($query, {-border=>1, -cellspacing=>0, -cellpadding=>5}, \@d, "firstrowisheader");
    }
  }
  &printhtml({body=>$html});
}

# returns popup_menus for (to and from) date fields.
sub date_fields {
  my($popup_name_prefix, $earliestyear) = @_;
  my(%monthlabels) = (
		      1=>'Jan',
		      2=>'Feb',
		      3=>'Mar',
		      4=>'Apr',
		      5=>'May',
		      6=>'Jun',
		      7=>'Jul',
		      8=>'Aug',
		      9=>'Sep',
		      10=>'Oct',
		      11=>'Nov',
		      12=>'Dec'
		     );
  my($html);
  my(@x);
  my($currentyear, $currentmonth, $currentdate) = split /:/, GeneralUtils->date("+%Y:%m:%d");

  my(@years) = ($earliestyear .. $currentyear);
  my(@months) = (1..12);
  my(@dates) = (1..31);

# from fields
  $html .= $query->popup_menu({-name=>$popup_name_prefix . "_year1",  -values=>\@years, -default=>$earliestyear});
  $html .= $query->popup_menu({-name=>$popup_name_prefix . "_month1", -values=>\@months, -labels=>\%monthlabels, -default=>1});
  $html .= $query->popup_menu({-name=>$popup_name_prefix . "_date1",  -values=>\@dates, -default=>1});

  $html .= $query->em(" and") . $query->br;

# to fields
  $html .= $query->popup_menu({-name=>$popup_name_prefix . "_year2",  -values=>\@years, -default=>$currentyear});
  $html .= $query->popup_menu({-name=>$popup_name_prefix . "_month2", -values=>\@months, -labels=>\%monthlabels, -default=>$currentmonth});
  $html .= $query->popup_menu({-name=>$popup_name_prefix . "_date2",  -values=>\@dates, -default=>$currentdate});

  return $html;
}

# returns a popup_menu with matching criteria for date fields.
sub date_criterion {
  my($popup_name_prefix) = @_;
  my($html);
  my(@values) = ($na, 'null', 'not null', 'between');

  $html .= $query->popup_menu({-name=>$popup_name_prefix . "_criterion",  -values=>\@values});
  return $html;
}

# makes up a where clause for a date field
sub date_whereclause {
  my($dbh, $prefix) = @_;
  my($x) = $dbh->quote(sprintf("%.4d:%.2d:%.2d",
			       $query->param($prefix . "_year1"),
			       $query->param($prefix . "_month1"),
			       $query->param($prefix . "_date1")));
  my($y) = $dbh->quote(sprintf("%.4d:%.2d:%.2d",
			       $query->param($prefix . "_year2"),
			       $query->param($prefix . "_month2"),
			       $query->param($prefix . "_date2")));
  return "$prefix>=TO_DATE($x, 'YYYY:MM:DD') and $prefix<=TO_DATE($y, 'YYYY:MM:DD')";
}

sub actions_table {
  my($w) = @_;
  my(@d);
  my($url0) = $main::EMSCONFIG{LEVEL0WMSURL} . "?$DBget";
  my($update2url) = $EMSCONFIG{LEVEL2WMSURL} . "?$DBget";
  my($update1url) = $EMSCONFIG{LEVEL1WMSURL} . "?$DBget";

  push @d, [ $query->a({-href=>$url0 . "&action=view&worklist=" . $w->{worklist_name}}, "View") ];
  push @d, [ $query->a({-href=>$url0 . "&action=wms_info&worklist=" . $w->{worklist_name}}, "Info") ];
  push @d, [ $query->a({-href=>$url0 . "&action=wms_stale&worklist=" . $w->{worklist_name}}, "Stale") ];
  push @d, [ $query->a({-href=>$url0 . "&action=report&worklist=" . $w->{worklist_name}}, "Report") ];
  push @d, [ $query->a({-href=>$update2url . "&level=2&action=wms_stamp&worklist=" . $w->{worklist_name}}, "Stamp (" . $main::EMSCONFIG{LEVEL2NICKNAME} . ")") ];
  push @d, [ $query->a({-href=>$update2url . "&level=2&action=wms_retract&worklist=" . $w->{worklist_name}}, "Retract (" . $main::EMSCONFIG{LEVEL2NICKNAME} . ")") ];

  push @d, [ $query->a({-href=>$update2url . "&level=2&action=wms_update&worklist=" . $w->{worklist_name}}, "Update (" . $main::EMSCONFIG{LEVEL2NICKNAME} . ")") ];
  push @d, [ $query->a({-href=>$update1url . "&level=1&action=wms_update&worklist=" . $w->{worklist_name}}, "Update (" . $main::EMSCONFIG{LEVEL1NICKNAME} . ")") ];

  return &toHTMLtable($query, {-border=>0, -cellspacing=>0, -cellpadding=>0}, \@d);
}

sub update_forms {
  my(@w) = @_;
  my($html1, $html2);
  my($html);

  $html1 .= $query->start_form(-method=>'POST', -action=>$main::EMSCONFIG{LEVEL2WMSURL});
  $html1 .= $DBpost;
#  $html1 .= $query->hidden(-name=>'action', -value=>'report', -override=>1);
  $html1 .= $query->hidden(-name=>'worklist', -value=>\@w, -override=>1);
  $html1 .= $query->hidden(-name=>'level', -value=>2, -override=>1);
  $html1 .= $query->popup_menu({-name=>'action', -value=>['report', 'wms_update'], -labels=>{report=>'Generate all reports', wms_update=>'Update all worklists'}});
  $html1 .= " " . $query->submit;
  $html1 .= $query->end_form;

  $html2 .= $query->start_form(-method=>'POST', -action=>$main::EMSCONFIG{LEVEL1WMSURL});
  $html2 .= $DBpost;
#  $html2 .= $query->hidden(-name=>'action', -value=>'wms_update', -override=>1);
  $html2 .= $query->hidden(-name=>'worklist', -value=>\@w, -override=>1);
  $html2 .= $query->hidden(-name=>'level', -value=>1, -override=>1);
  $html2 .= $query->popup_menu({-name=>'action', -value=>['wms_update'], -labels=>{'wms_update'=>'Update all worklists'}});
  $html2 .= " " . $query->submit;
  $html2 .= $query->end_form;

  $html .= &toHTMLtable($query, {-border=>1}, [[
						&toHTMLtable($query, {-border=>0}, [["As " . $main::EMSCONFIG{LEVEL2NICKNAME} . ", ", $html1],
										    ["As " . $main::EMSCONFIG{LEVEL1NICKNAME} . ", ", $html2]
										   ])
					       ]]);
  $html .= $query->p;
  return $html;
}

sub imgurl {
  my($description) = @_;
  return $query->img({-src=>$main::EMSCONFIG{EMSIMGURL} . "/info.gif",
		      -alt=>$description,
		      -title=>$description}) . " ";
}


1;
