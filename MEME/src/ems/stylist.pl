# Displays Semantic Types and their definitions
# suresh@nlm.nih.gov 7/97
# suresh@nlm.nih.gov 3/06 - EMS3 mods

# CGI params
# db=
# sortby=
sub do_stylist {
  my($html);
  my($sql);
  my(@cols) = qw(STY_RL UI STN_RTN DEF);
  my($cols) = join(", ", @cols);
  my($sortby) = uc($query->param('sortby') || "STY_RL");

  $sql = "select $cols from SRDEF where RT='STY'";
  $sql .= " order by $sortby" if $sortby;

  $n=0;
  my(@u) = map { $query->a({-href=>$query->url() . "?$DBget&action=$action&sortby=$_"}, $_) } @cols;
  push @d, ['', @u];
	    
  foreach $r ($dbh->selectAllAsRef($sql)) {
    $n++;
    push @d, [$n, @$r];
  }

  $html .= <<"EOD";
This table displays the current semantic types as obtained from the SRDEF table.
Select the column name hyperlink to sort by that column.
<P>
EOD
  
  $html .= &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@d, "firstrowisheader");
  &printhtml({title=>'UMLS Semantic Types', h1=>"UMLS Semantic Types", body=>$html});
}
1;
