# suresh@nlm.nih.gov - 8/97
# Originally implemented - suresh@nlm.nih.gov 5/2003
# EMS3 - 1/2006

# Infer the insertion dates of sources into the MID

# CGI params:
# db=
# config=
# years= (how many years in the past?)
# doit=

sub do_sourceinsertion {
  my($sql);
  my($html);
  my($title) = "Source insertion metadata";

  unless ($query->param('doit')) {

    my(@years) = (1..5);

    $html .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $html .= $DBpost;

    $html .= "Show inferred insertion dates for sources in the past: ";
    $html .= $query->popup_menu({-name=>'years', -default=>1, -values=>\@years}) . " year(s)";
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $html .= $query->p;
    $html .= $query->submit;
    $html .= $query->end_form;

    &printhtml({title=>$title, h1=>$title, body=>$html});

  } else {

    my($years) = $query->param('years') || 1;
    my($m) = $years*12;
    my($fmt) = $dbh->quote('YYYY-MM-DD');

# get all the VSAB (VAB) atoms and their insertion dates
    $sql = <<"EOD";
select c.concept_id, a.atom_name, TO_CHAR(c.insertion_date, 'YYYY-MM-DD') from classes c, atoms a
where  c.source='SRC'
and    c.termgroup='SRC/VAB'
and    c.atom_id=a.atom_id
and    c.tobereleased in ('y', 'Y')
and    MONTHS_BETWEEN(SYSDATE, c.insertion_date) <= $m
order by insertion_date desc
EOD

    my(@refs) = $dbh->selectAllAsRef($sql);
    my($n) = 0;
    my($r);
    my($vsab, $rsab, $insertion_date, $description, $concept_id);

    @rows = ();
    push @rows, ["", "SRC Concept", "VSAB", "RSAB", "Insertion Date", "Description"];

    foreach $r (@refs) {
      $n++;
      $concept_id = $r->[0];
      $vsab = $r->[1];
      $insertion_date = $r->[2];

      $rsab = MIDUtils->makeVersionlessSAB($dbh, $vsab);
      $description = MIDUtils->officialSABName($dbh, $vsab);

      $url = $query->a({href=>$main::EMSCONFIG{MIDEMS3CONCEPTREPORTURL} . "?action=search&subaction=concept_id&arg=$concept_id&$DBget"}, $concept_id);
      push @rows, [$n, $url, $vsab, $rsab, $insertion_date, $description];
    }

    $html .= $query->h1($title);
    $html .= <<"EOD";
The following table shows the insertion dates of
sources into the MID.  This is heuristically derived from
the data attached to SRC/VAB atoms and may not be completely
accurate for some older sources.
<P>
EOD
    $html .= &toHTMLtable($query, {-border=>1, -width=>'80%', -cellpadding=>5, -cellspacing=>1}, \@rows, "firstrowisheader");

    &printhtml({title=>$title, h1=>$title, body=>$html});
  }
}
1;
