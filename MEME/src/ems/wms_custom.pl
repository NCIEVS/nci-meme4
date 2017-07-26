# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000
# EMS3 - 12/2005

# CGI params:
# db=

sub do_wms_custom {
  my($sql);
  my($html);
  my($WORKLISTINFOTABLE) = $EMSNames::WORKLISTINFOTABLE;
  my(@d, $a, $b);
  my($n) = 0;

  $html .= $query->p;
  $html .= $query->hr;

  $a = "Worklists currently being edited" . $query->br .
    $query->em("assign_date != null AND return_date == null");
  $b = &make_form({action=>'wms_query', doit=>1, assign_date_criterion=>'not null', return_date_criterion=>'null'});
  push @d, [++$n, $a, $b];

  $a = "Worklists currently awaiting " . $main::EMSCONFIG{LEVEL2NICKNAME} . " review" . $query->br .
    $query->em("return_date != null AND stamp_date == null");
  $b = &make_form({action=>'wms_query', doit=>1, return_date_criterion=>'not null', stamp_date_criterion=>'null'});
  push @d, [++$n, $a, $b];

  $a = "Worklists created but not assigned to a group" . $query->br .
    $query->em("grp == null");
  $b = &make_form({action=>'wms_query', doit=>1, grp=>'--null--'});
  push @d, [++$n, $a, $b];

  $a = "Worklists not assigned to an editor" . $query->br .
    $query->em("editor == null");
  $b = &make_form({action=>'wms_query', doit=>1, editor=>'--null--'});
  push @d, [++$n, $a, $b];

  $a = "Worklists stamped but not returned(!)" . $query->br .
    $query->em("return_date == null AND stamp_date != null");
  $b = &make_form({action=>'wms_query', doit=>1, return_date_criterion=>'null', stamp_date_criterion=>'not null'});
  push @d, [++$n, $a, $b];

  $sql = "select min(to_char(create_date, 'YYYY')) from $WORKLISTINFOTABLE";
  my($earliestyear) = $dbh->selectFirstAsScalar($sql);

  $a = "Worklists created between";
  $b = &make_form({action=>'wms_query', doit=>1, create_date_criterion=>'between', data=>&date_fields("create_date", $earliestyear)});
  push @d, [++$n, $a, $b];

  $a = "Worklists returned to " . $main::EMSCONFIG{LEVEL2NICKNAME} . " between";
  $b = &make_form({action=>'wms_query', doit=>1, return_date_criterion=>'between', data=>&date_fields("return_date", $earliestyear)});
  push @d, [++$n, $a, $b];

  $a = "Worklists stamped or marked done between";
  $b = &make_form({action=>'wms_query', doit=>1, stamp_date_criterion=>'between', data=>&date_fields("stamp_date", $earliestyear)});
  push @d, [++$n, $a, $b];

  $html .= &toHTMLtable($query, {-border=>1, -cellspacing=>0, -cellpadding=>5}, \@d);
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

sub make_form {
  my($p) = @_;
  my($html, $k);

  $html .= $query->start_form(-method=>'POST', -action=>$main::EMSCONFIG{LEVEL0WMSURL});
  $html .= $DBpost;

  foreach $k (keys %$p) {
    next if $k eq "data";
    $html .= $query->hidden(-name=>$k, -value=>$p->{$k}, -override=>1) . "\n";
  }
  $html .= $p->{data} if $p->{data};
  $html .= $query->p;
  $html .= $query->submit;
  $html .= $query->end_form;

  return $html;
}

1;
