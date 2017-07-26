# Shows the AH canonical bin name map
# suresh@nlm.nih.gov 12/98
# suresh@nlm.nih.gov 9/05 - EMS3 mods

# CGI params
# db=
sub do_ah_canonical {
  my($html);
  my($form) = $query->p;

  $sql = "select bin_name, canonical_name from $EMSNames::AHCANONICALNAMETABLE";
  $n=0;
  push @d, ['', "bin_name", "canonical_name"];
  foreach $r ($dbh->selectAllAsRef($sql)) {
    $n++;
    push @d, [$n, $r->[0], $r->[1]];
  }

  $html .= <<"EOD";
AH bins have history.  In other words if a cluster of concepts
belonged to an AH bin, then the cluster (if it contains
the same concepts) is never edited again for that purpose.
<P>
You can map multiple AH bins to the same canonical name
so that for history purposes, multiple bins that share a
canonical name are treated as identical.  Thus, for example,
"needsrel", "needs_rel", "needsrel_go" can all be mapped to
the canonical name "needsrel".  The default canonical name
is identical to the bin name and need not be explicitly
stated.  A bin can map to only one canonical name.
<P>
This table shows you all the current mappings.
To add or delete a map, talk to the EMS administrator.
<P>
EOD
  
  $html .= &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@d, "firstrowisheader");
  &printhtml({title=>'AH Canonical', h1=>"AH Canonical", body=>$html});
}
1;
