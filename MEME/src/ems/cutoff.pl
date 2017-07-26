# Cut off editing in the DB (this flag is used by Jekyll, etc)
# suresh@nlm.nih.gov 12/98
# suresh@nlm.nih.gov 12/05 - EMS3 mods

# CGI params
# db=
# config=
# cutoff=Yes|No
# doit=1

sub do_cutoff {
  my($html);
  my($form) = $query->p;
  my($current) = $main::editingCUTOFF;

  unless ($query->param('doit')) {

    $html .= <<"EOD";
You can turn on/off editing in this database by submitting this form.
This does not affect the EMS/WMS directly, but does the other tools
that are used to edit the database content, e.g., Jekyll.
EOD
    $html .= $query->p;
    $form .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $form .= "Cutoff editing? " . $query->radio_group({-name=>'cutoff', -default=>$current, -values=>["Yes", "No"]});
    $form .= $query->p . $query->submit;
    $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $form .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $form .= $DBpost;
    $form .= $query->end_form;
    $html .= $form;

    &printhtml({title=>"Cut off editing", h1=>"Cut off editing in $main::db", body=>$html});

  } else {

    unless ($dbh->tableExists("DBA_CUTOFF")) {
      &printhtml({printandexit=>1, body=>"ERROR: The DBA_CUTOFF table does not exist.  Please have the DBA create this table and try again."});
    }

    my($cutoff) = $query->param('cutoff');
    &printhtml({printandexit=>1, body=>"ERROR: Unknown cutoff value: $cutoff"}) unless grep { $_ eq $cutoff } qw(Yes No);

    $sql = "update DBA_CUTOFF set edit=" . $dbh->quote(($cutoff eq "Yes" ? "n" : 'y'));
    $dbh->executeStmt($sql);
    die $@ if $@;

    my($url) = $main::EMSCONFIG{LEVEL0EMSURL} . "?$DBget&action=ems_home";
    &printhtml({redirect=>$url});
  }
}
1;
