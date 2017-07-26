# suresh@nlm.nih.gov - 8/97
# Originally implemented - suresh@nlm.nih.gov 7/2002
# EMS3 - 1/2006

# displays STY co-occurrence information

# CGI params:
# db=
# config=
# refresh= (refreshes the cache)
# degree=
# ordercol=
# orderdir=
# doit=

sub do_stycooc {
  my($sql);
  my($html);
  my($title) = "Semantic Type Co-occurrence";
  my(@ordercol) = qw(stys frequency);

  unless ($query->param('doit')) {

    $html .= <<"EOD";
This page shows the frequency of co-occurrences of two or more STYs
and allows checklists to be made from the matching concepts.
EOD
    $html .= $query->start_form(-method=>'POST', -action=>$query->url());
    $html .= $DBpost;

    $html .= "Select the degree of co-occurrence: ";
    $html .= $query->popup_menu({-name=>'degree', -values=>[2,3,4, 5]});
    $html .= $query->p;
    $html .= "Order results by: ";
    $html .= $query->popup_menu(-name=>'ordercol', -values=>\@ordercol);
    $html .= $query->popup_menu(-name=>'orderdir', -values=>['Asc', 'Desc']);
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $html .= $query->p;
    $html .= $query->submit;
    $html .= $query->end_form;

    &printhtml({title=>$title, h1=>$title, body=>$html});
    return;

  }

  my($degree) = $query->param('degree') || 2;
  my($ordercol) = $query->param('ordercol') || 'stys';
  my($orderdir) = $query->param('orderdir') || 'Asc';
  my($STYCOOCTABLE) = $EMSNames::STYCOOCTABLE;

  EMSTables->createTable($dbh, $STYCOOCTABLE);

  if ($query->param('refresh')) {
    my(@degree) = split /,/, $query->param('degree');
    my($starttime);
    my($currentdateq);
    my($styattributes) = $dbh->tempTable($EMSNames::TMPTABLEPREFIX . "_sty");

    foreach $degree (@degree) {
      $starttime = time;
      $currentdateq = $dbh->quote($dbh->currentDate());

      $sql = "delete from $STYCOOCTABLE where degree=$degree";
      $dbh->executeStmt($sql);

      $dbh->dropTable($styattributes);
      $sql = "create table $styattributes as select concept_id, attribute_value from attributes where attribute_name || '' ='SEMANTIC_TYPE'";
      $dbh->executeStmt($sql);
      $dbh->createIndex($styattributes, "concept_id", "x1_" . $styattributes);
      $dbh->createIndex($styattributes, "attribute_value", "x2_" . $styattributes);

      $sql = &make_sql($degree, $styattributes);

      my($t) = 0;
      my($n);
      foreach $ref ($dbh->selectAllAsRef($sql)) {
	$t = time - $starttime unless $t;
	chomp;
	@x = ();
	for ($i=0; $i<$degree; $i++) {
	  push @x, $ref->[$i];
	}
	$stys = join("|", @x);
	$stys = $dbh->quote($stys);
	$frequency = $ref->[$degree];
	$sql = <<"EOD";
insert into $STYCOOCTABLE (degree, stys, frequency, generation_date, generation_time)
values ($degree, $stys, $frequency, to_date($currentdateq), $t)
EOD
	$dbh->executeStmt($sql);
	$n++;
      }
      printf("%d records for stycooc data generated for degree=%d in %d seconds\n", $n, $degree, time-$starttime);
    }
    $dbh->dropTable($styattributes);
    return;
  }

  $sql = <<"EOD";
select stys, frequency from $STYCOOCTABLE where degree=$degree
order by $ordercol $orderdir, stys
EOD
  @refs = $dbh->selectAllAsRef($sql);

  if (@refs == 0) {
    &printhtml({printandexit=>1, body=><<"EOD"});
Data was not yet gathered for this degree of STY co-occurrences.
Please ask the EMS administrator to refresh the cache and try again.
EOD
  }

  my($row, $ref);
  my(@rows);

  my($n);

  foreach $ref (@refs) {
    @s = split /\|/, $ref->[0];
    $n++;
    $row = $query->td({-rowspan=>$degree, -align=>'right'}, $n);
    $row .= $query->td($s[0]);
    $row .= $query->td({rowspan=>$degree}, $ref->[1]);
    
    $s = "styselected=" . CGI->escape(join("\n", @s));
    $url = $query->url() . "?$DBget&action=styqa&subaction=search&doit=1&$s";
    $row .= $query->td({-rowspan=>$degree}, $query->a({-href=>$url}, "Checklist"));
    push @rows, $row;

    foreach (@s[1..$#s]) {
      $row = $query->td($_);
      push @rows, $row;
    }
  }

  $html .= <<"EOD";
The following table shows the co-occurence frequency of Semantic
Types considered $degree at a time.  You can make a checklist from
the matching concepts by following the link.
<P>
EOD
  $html .= $query->startform;
  $html .= $DBpost;

  $html .= "Order results by: ";
  $html .= $query->popup_menu(-name=>'ordercol', -values=>\@ordercol, -default=>$ordercol);
  $html .= $query->popup_menu(-name=>'orderdir', -values=>['Asc', 'Desc'], -default=>$orderdir);
  $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
  $html .= $query->hidden(-name=>'degree', -value=>$degree, -override=>1);
  $html .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
  $html .= $query->submit;
  $html .= $query->p;

  $html .= $query->table({-border=>1, -cellspacing=>0, -cellpadding=>5}, $query->Tr(\@rows));
  $html .= $query->endform;
  &printhtml({title=>$title, h1=>$title, body=>$html});
}

sub make_sql {
  my($n, $attributes) = @_;
  my($i);
  my($sql);
  my(@x);

  $attributes = "attributes" unless $attributes;

  for ($i=0; $i<$n; $i++) {
    $t = chr(97+$i);
    $s = chr(96+$i);

    push @{ $x[0] }, $t . ".attribute_value";
    push @{ $x[1] }, "$attributes $t";
#    push @{ $x[2] }, $t . ".attribute_name || ''='SEMANTIC_TYPE'";
    push @{ $x[3] }, $t . ".concept_id=" . $s . ".concept_id" if $i>0;
    push @{ $x[4] }, $s . ".attribute_value<" . $t . ".attribute_value" if $i>0;

  }

  $hint = "/*+ full(a) full(b) use_hash(a,b) */";
  $hint = "";
  $sql =
    "select $hint " . join(', ', @{ $x[0] }) . ", count(distinct a.concept_id) from " . join(', ', @{ $x[1] }) .
    "\n\twhere " . join("\n\tand ", @{ $x[3]}) .
#    "\n\tand " . join("\n\tand ", @{ $x[2] }) .
    "\n\tand " . join("\n\tand ", @{ $x[4] }) .
    "\ngroup by " . join(', ', @{ $x[0] }) . "\n";
  return $sql;
}
1;
