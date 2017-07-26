# Displays the contents of the termgroup_rank table
# suresh@nlm.nih.gov 7/97
# suresh@nlm.nih.gov 3/06 - EMS3 mods
# SL: Adding the Feature to sort by rank or release rank (Per Brian's Request)
# CGI params
# db=
# sortby=
sub do_termgroup_rank {
  my($html);
  my($sql) = "select COLUMN_NAME from ALL_TAB_COLUMNS where TABLE_NAME='TERMGROUP_RANK'";
  my(@cols) = $dbh->selectAllAsArray($sql);
  my($cols) = join(", ", @cols);
  my($sortby) = uc($query->param('sortby') || "rank");

  $sql = "select $cols from TERMGROUP_RANK";
  $sql .= " order by $sortby" if $sortby;
  $sql .= " desc" if ($sortby eq "RELEASE_RANK" || $sortby eq "RANK");
  
  $n=0;
  my(@u) = map { $query->a({-href=>$query->url() . "?$DBget&action=$action&sortby=$_"}, $_) } @cols;
  push @d, ['', @u];
	    
  foreach $r ($dbh->selectAllAsRef($sql)) {
    $n++;
    push @d, [$n, @$r];
  }

  $html .= <<"EOD";
This table displays the current termgroup rankings.
Select the column name hyperlink to sort by that column.
<P>
Termgroups with higher rank values are higher in precedence.
<P>
EOD
  
  $html .= &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@d, "firstrowisheader");
  &printhtml({title=>'Termgroup rank', h1=>"Termgroup rank", body=>$html});
}
1;
