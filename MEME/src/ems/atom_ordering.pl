# Check the contents of the atom_ordering table
# suresh@nlm.nih.gov 8/03
# suresh@nlm.nih.gov 1/06 - EMS3 mods

# CGI params
# rsab=

sub do_atom_ordering {
  my($rsab) = $query->param('rsab');
  my($html);
  my($sql);

  unless ($rsab) {
    $sql = "select distinct root_source from atom_ordering";
    my(@rsabs) = $dbh->selectAllAsArray($sql);

    &printhtml({printandexit=>1, body=>"ERROR: The atom_ordering table was empty."}) unless @rsabs;

    $form .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $form .= "Select source: " . $query->popup_menu({-name=>'rsab', -values=>\@rsabs, -onChange=>'submit();'});
    $form .= $query->p;
    $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $form .= $DBpost;
    $form .= $query->submit;
    $form .= $query->end_form;

    $html .= <<"EOD";
The ATOM_ORDERING table contains a suggested ordering for all
the atoms of a source.  This is populated in the inversion process
so that worklists made from the bin for the source will have
the optimal ordering of concepts.
<P>
This form checks the table for a given source to ensure that
all atoms have a distinct order_id and that atom counts in
classes and the atom_ordering table match.
<P>
Note that only sources with rows in ATOM_ORDERING are shown.
<P>
EOD
    $html .= $form;
    &printhtml({title=>'Atom ordering', h1=>"Atom ordering", body=>$html});
    return;
  }

  my($vsab) = MIDUtils->makeVersionedSAB($dbh, $rsab);
  my($qvsab) = $dbh->quote($vsab);
  my($qrsab) = $dbh->quote($rsab);
  my($sql);
  my($classes_count, $atom_ordering_count, $zero_count);
  my($unique_atoms, $unique_ordering);

  &printhtml({printandexit=>1, body=>"ERROR: No data in source_version for source: $rsab"}) unless $vsab;

  $sql = "select count(*) from atom_ordering where root_source=$qrsab";
  $atom_ordering_count = $dbh->selectFirstAsScalar($sql);

  &printhtml({printandexit=>1, body=>"There was no data for source: $rsab in ATOM_ORDERING"}) unless $atom_ordering_count>0;

  $sql = "select count(*) from classes where source=$qvsab";
  $classes_count = $dbh->selectFirstAsScalar($sql);

  $sql = <<"EOD";
select count(*) from (
  select atom_id, count(*) from atom_ordering
  where  root_source=$qrsab
  group  by atom_id
  having count(*) > 1
)
EOD
  $unique_atoms = $dbh->selectFirstAsScalar($sql);

  $sql = <<"EOD";
select count(*) from (
  select order_id, count(*) from atom_ordering
  where  root_source=$qrsab
  group  by order_id
  having count(*) > 1
)
EOD
  $unique_ordering = $dbh->selectFirstAsScalar($sql);

  $sql = "select count(*) from atom_ordering where root_source=$qrsab and atom_id=0";
  $zero_count = $dbh->selectFirstAsScalar($sql);

  my(@d);

  push @d, ["Versionless source", $rsab];
  push @d, ["Versioned source", $vsab];
  push @d, ["Atom count in classes", [{align=>'right'}, $classes_count]];
  push @d, [
	    ($classes_count != $atom_ordering_count ? $query->font({-color=>'red'}, "Atom count in ATOM_ORDERING") : "Atom count in ATOM_ORDERING"),
	    [{align=>'right'}, $atom_ordering_count]
	   ];
  push @d, [($zero_count > 0 ? $query->font({-color=>'red'}, "Count in ATOM_ORDERING where atom_id=0") : "Count in ATOM_ORDERING where atom_id=0"),
	    [{align=>'right'}, $zero_count]
	   ];
  push @d, [($unique_atoms ? $query->font({-color=>'red'}, "Unique atom_id?") : "Unique atom_id?"),
	    $unique_atoms == 0 ? "Yes" : "No"
	   ];
  push @d, [
	    ($unique_ordering ? $query->font({-color=>'red'}, "Unique order_id?") : "Unique order_id?"),
	    $unique_ordering == 0 ? "Yes" : "No"
	   ];

  $html .= <<"EOD";
For the EMS to function correctly, the atom_ordering count and the
classes count must be identical so that every source atom is
accounted for.  Also the atom_id and order_id columns must be
unique, i.e., an atom cannot have multiple orderings nor can multiple
atoms share an ordering.  Finally, atom_ids cannot be zero in the
atom_ordering table.
<P>
EOD
  if ($atom_ordering_count != $classes_count ||
      $zero_count > 0 ||
      $unique_atoms > 0 ||
      $unique_ordering > 0) {
    $html .= $query->font({-color=>'red'}, "There are errors in the data.");
  } else {
    $html .= "The data for $vsab looks correct.";
  }
  $html .= $query->p;

  $html .= &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@d);
  &printhtml({title=>'Atom ordering', body=>$html});
}
1;
