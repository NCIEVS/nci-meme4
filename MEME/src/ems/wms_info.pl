# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000
# EMS3 - 12/2005

# Metadata and other information about the contents of worklists

# CGI params:
# db=
# worklist=

sub do_wms_info {
  my($sql);
  my($html);
  my($WORKLISTINFOTABLE) = $EMSNames::WORKLISTINFOTABLE;
  my(@d);
  my(@worklists) = $query->param('worklist');
  my($worklist);
  my(@cols) = EMSTables->columns($WORKLISTINFOTABLE);

  $html .= <<"EOD";
Shown below are data for one or more worklists.
EOD

  foreach $worklist (@worklists) {
    $sql = "select " . join(", ", @cols) . " from $WORKLISTINFOTABLE where worklist_name=" . $dbh->quote($worklist);
    $ref = $dbh->selectFirstAsRef($sql);
    $w = $dbh->row2ref($ref, @cols);

    @d = ();
    push @d, [ 'Attribute', 'Value' ];
    foreach $key (@cols) {
      push @d, [ $key, $w->{$key} ];
    }
    $html .= $query->p;
    $html .= $query->hr;
    $html .= $query->h1("Metadata from $WORKLISTINFOTABLE table");
    $html .= &toHTMLtable($query, {-border=>1, -cellspacing=>0, -cellpadding=>5}, \@d, "firstrowisheader");

    $sql = <<"EOD";
select editing_authority, count(*) from concept_status
where  concept_id in (
  select distinct concept_id from classes a, $worklist b
  where  a.atom_id=b.atom_id
)
group by editing_authority
order by count(*) desc
EOD
    @d = ();
    push @d, ['Editing authority', 'Count'];
    foreach $ref ($dbh->selectAllAsRef($sql)) {
      push @d, [$ref->[0], $ref->[1]];
    }
    $html .= $query->p;
    $html .= $query->hr;
    $html .= $query->h1("Editing authority profile");
    $html .= <<"EOD";
This table shows the last approving authority for concepts
on the worklist determined from the current atom membership.
<P>
EOD
    $html .= &toHTMLtable($query, {-border=>1, -cellspacing=>0, -cellpadding=>5}, \@d, "firstrowisheader");
  }
  &printhtml({body=>$html});
}
1;
