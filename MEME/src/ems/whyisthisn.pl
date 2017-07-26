# Explanation of why a concept is in N status
# suresh@nlm.nih.gov 7/2002
# suresh@nlm.nih.gov 2/06 - EMS3 mods

sub do_whyisthisn {
  my($html);
  my($concept_id) = $query->param('concept_id');
  my($title) = 'Why is this concept in N status';
  my($sql);
  my(@d, @x);
  my($tableprop) = {-border=>1, -cellspacing=>0, -cellpadding=>5};

  &printhtml({printandexit=>1, body=>"Need a concept_id"}) unless $concept_id;

  my($concept_status) = $dbh->selectFirstAsScalar("select status from concept_status where concept_id=$concept_id");

  $html .= <<"EOD";
A concept has status N (needs review) if any of its component
elements (atoms, relationships or attributes) has status N.
If the concept status is N and there are no status N elements,
the next run of the matrix initializer should reset the status
to R.

<P>
EOD

  push @d, ["Concept_id:", $concept_id];
  push @d, ["Preferred Name:", MIDUtils->conceptPreferredName($dbh, $concept_id)];
  push @d, ["Concept Status:", $concept_status ];

  $qn = $dbh->quote('N');
  $sql = <<"EOD";
SELECT c.atom_id, c.termgroup, a.atom_name, c.tobereleased from classes c, atoms a
where  c.concept_id=$concept_id
and    c.atom_id=a.atom_id
and    c.status=$qn
EOD
  @_ = $dbh->selectAllAsRef($sql);

  if (@_) {
    @x = ();
    push @x, ['atom_id', 'termgroup', 'atom', 'tbr'];
    foreach $r (@_) {
      push @x, $r;
#      push @x, [$r->[0], $r->[1], $r->[2], $r->[3]];
    }
    push @d, ["Status N atoms",
		 &toHTMLtable($query, $tableprop, \@x),
		];
    $statusN++;
  }

  $sql = <<"EOD";
select atom_id_1, concept_id_1, atom_id_2, concept_id_2, relationship_name, relationship_level, source, authority, tobereleased from relationships
where  (concept_id_1 = $concept_id OR concept_id_2 = $concept_id)
and    status=$qn
EOD
  @_ = $dbh->selectAllAsRef($sql);

  if (@_) {
    @x = ();
    push @x, ['atom_id_1', 'concept_id_1', 'atom_id_2', 'concept_id_2', 'rel', 'level', 'source', 'auth', 'tbr'];
    foreach $r (@_) {
      push @x, $r;
    }
    push @d, ["Status N relationships",
		 &toHTMLtable($query, $tableprop, \@x),
		];
    $statusN++;
  }

  $sql = <<"EOD";
select atom_id, attribute_name, attribute_value, source, tobereleased from attributes
where  concept_id = $concept_id
and    status=$qn
EOD
  @_ = $dbh->selectAllAsRef($sql);

  if (@_) {
    @x = ();
    push @x, ['atom_id', 'name', 'value', 'source', 'tbr'];
    foreach $r (@_) {
      push @x, $r;
    }
    push @d, ["Status N attributes",
		 &toHTMLtable($query, $tableprop, \@x),
		];
    $statusN++;
  }

  if ($concept_status eq "N" and !$statusN) {
    @_ = ();
    $sql = <<"EOD";
select atom_id_1, concept_id_1, atom_id_2, concept_id_2, relationship_name, relationship_level, source, authority, tobereleased from context_relationships
where  (concept_id_1 = $concept_id OR concept_id_2 = $concept_id)
and    status=$qn
EOD
    @_ = $dbh->selectAllAsRef($sql);

    if (@_) {
      @x = ();
      push @x, ['atom_id_1', 'concept_id_1', 'atom_id_2', 'concept_id_2', 'rel', 'level', 'source', 'auth', 'tbr'];
      foreach $r (@_) {
	push @x, $r;
      }
      push @d, ["Status N context relationships",
		   &toHTMLtable($query, $tableprop, \@x),
		  ];
      $statusN++;
    }
  }

  if ($statusN == 0) {
    &printhtml({printandexit=>1, title=>$title, body=>"This concept has no status N components."});
  } else {
    $html .= &toHTMLtable($query, $tableprop, \@d);
  }
  &printhtml({title=>$title, h1=>$title, body=>$html});
}
1;
