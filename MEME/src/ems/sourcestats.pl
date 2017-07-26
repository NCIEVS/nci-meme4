# suresh@nlm.nih.gov - 8/97
# Originally implemented - suresh@nlm.nih.gov 5/2003
# EMS3 - 1/2006

# Generate and display cached data of counts and tallies for sources
# e.g., atom counts, rel counts, attr counts, etc

# CGI params:
# db=
# config=
# source=

sub do_sourcestats {
  my($sql);
  my($html);
  my($title) = "Source statistics";
  my($SOURCESTATSTABLE) = $EMSNames::SOURCESTATSTABLE;
  my($source) = $query->param('source');

  EMSTables->createTable($dbh, $SOURCESTATSTABLE);

  unless ($source) {

    my(@alphavsabs);
    my(@timevsabs);
    my(%currentvsabs);

    %currentvsabs = map { $_ => 1 } MIDUtils->getSources($dbh, "versioned");
    $sql = "select vsab, generation_date from $SOURCESTATSTABLE order by vsab";
    foreach $r ($dbh->selectAllAsRef($sql)) {
      $generation_date{$r->[0]} = $r->[1];
      push @alphavsabs, $r->[0];
    }

    $sql = "select vsab, generation_date from $SOURCESTATSTABLE order by generation_date desc";
    foreach $r ($dbh->selectAllAsRef($sql)) {
      push @timevsabs, $r->[0];
    }
#    @timevsabs = sort { $generation_date{$b} cmp $generation_date{$a} } @alphavsabs;

    unless (@alphavsabs) {
      &printhtml({printandexit=>1, title=>$title, body=>"There is no cached data for any source in this database at this time."})
    }

    %labels = map { $_ => ($currentvsabs{$_} ? "$_" : "*$_") . " (generated on " . ($generation_date{$_} || "???") . ")"} @timevsabs;

    $html .= <<"EOD";
Get counts of atoms, rels, attributes, etc. for a source along with
data about how it overlaps with other sources.  Data is generated
periodically and cached.  This query only returns cached data.
<P>
Note: Sources prefixed with '*' are not current and no longer present in the Metathesaurus.
EOD

    $html .= $query->p;
    $html .= $query->hr;

    $html .= $query->start_form(-method=>'POST', -action=>$query->url());
    $html .= $DBpost;
    $html .= "Select a source (alphabetic order): ";
    $html .= $query->popup_menu({-name=>'source', -values=>\@alphavsabs, -labels=>\%labels});
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->p;
    $html .= $query->submit;
    $html .= $query->end_form;

    $html .= $query->p;
    $html .= "or," . $query->p . $query->hr;

    $html .= $query->start_form(-method=>'POST', -action=>$query->url());
    $html .= $DBpost;
    $html .= "Select a source (chronological order): ";
    $html .= $query->popup_menu({-name=>'source', -values=>\@timevsabs, -labels=>\%labels});
    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->p;
    $html .= $query->submit;
    $html .= $query->end_form;

    &printhtml({title=>$title, h1=>$title, body=>$html});

  } else {

    my($q) = $dbh->quote($source);

# For retrieving CLOBs, this must be set
    $dbh->setAttributes({LongReadLen=>1024*1024, LongTruncOk=>0});

    $sql = <<"EOD";
select htmldata from $SOURCESTATSTABLE where vsab=$q
EOD

    $html .= $dbh->selectFirstAsScalar($sql) || "No data found.";

    &printhtml({body=>$html});
  }
}
1;
