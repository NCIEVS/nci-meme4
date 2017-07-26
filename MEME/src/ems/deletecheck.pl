# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000

# Deletes one or more checklists
# CGI params:
# db=
# list=
# checklist=
# doit=
sub do_deletecheck {
  my(@checklist) = $query->param('list') || $query->param('checklist');
  my($checklist);
  my($sql);
  my($html);
  my($CHECKLISTINFO) = $EMSNames::CHECKLISTINFOTABLE;
  my($checklist) = $query->ol($query->li(\@checklist));

  &printhtml({body=>'ERROR: Need a checklist name.', printandexit=>1}) unless @checklist;

  unless ($query->param('doit')) {
    $html .= $query->startform;
    $html .= "Are you sure you want to delete checklist the following checklists: $checklist";
    $html .= $query->p;
    $html .= $query->submit(-name=>'doit', -value=>'Yes, I\'m sure!');
    $html .= $query->hidden(-name=>'checklist', -value=>\@checklist);
    $html .= $query->hidden(-name=>'action', -value=>$action);
    $html .= $DBpost;
    $html .= $query->endform;
    &printhtml({title=>'Delete checklist', h1=>'Delete checklist', body=>$html});
    return;
  }

  foreach $checklist (@checklist) {
    $dbh->dropTable($checklist);
    $sql = "delete from $CHECKLISTINFO where checklist_name=" . $dbh->quote($checklist);
    $dbh->executeStmt($sql);
  }
  &printhtml({redirect=>$query->url() . "?$DBget&action=pickcheck&random=$$"});
  return;
}
1;
