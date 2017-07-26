# When was the DB refreshed?
# suresh@nlm.nih.gov 3/06

sub do_db_refresh {
  my($html);
  my($sql) = "select created from " . 'V$DATABASE';
  my($x) = $dbh->selectFirstAsScalar($sql);
  
  if ($x) {
    $html .= "The database: " . $dbh->getDB() . " was last refreshed on: $x";
  } else {
    $html .= "Could not determine the refresh date for database: " . $dbh->getDB();
  }
  &printhtml({title=>'Database refresh', h1=>"Database refresh", body=>$html});
}
1;
