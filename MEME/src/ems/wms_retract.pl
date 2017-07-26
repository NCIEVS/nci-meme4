# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000
# EMS3 - 12/2005

# Retracting a worklist
# Only individual worklists can be retracted.

# CGI params:
# db=
# worklist=
# doit=

sub do_wms_retract {
  my($sql);
  my($html);
  my($worklist) = $query->param('worklist');

  unless ($query->param('doit')) {
    $html .= <<"EOD";
Retracting a worklist involves removing all traces of the worklist,
including the worklist table, the rows for it in the EMS and WMS
and any historical information associated with it.
EOD
    $html .= $query->p;

    $html .= "Are you sure you want to retract the worklist: " . $query->b($worklist) . "?";
    my($form1, $form2);
    my(@d);

    $form1 .= $query->start_form(-method=>'POST', -action=>$query->url(-absolute=>1));
    $form1 .= $query->p . $query->submit({-value=>"Yes, I'm sure"});
    $form1 .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $form1 .= $query->hidden(-name=>'worklist', -value=>$worklist, -override=>1);
    $form1 .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $form1 .= $DBpost;
    $form1 .= $query->end_form;

    $form2 .= $query->start_form(-method=>'POST', -action=>$EMSCONFIG{LEVEL0WMSURL});
    $form2 .= $query->p . $query->submit({-value=>"No, take me home!"});
    $form2 .= $query->hidden(-name=>'action', -value=>'', -override=>1);
    $form2 .= $DBpost;
    $form2 .= $query->end_form;

    $html .= &toHTMLtable($query, {border=>0}, [[$form1, $form2]]);

    &printhtml({h1=>'Retract worklist', body=>$html});
    return;
  }

  $html .= $query->p;
  $html .= $query->hr;
  if (!$worklist) {
    $html .= "Worklist not specified!";
  } elsif (!$dbh->tableExists($worklist)) {
    $html .= "Worklist: $worklist does not exist!";
  } else {
    EMSUtils->retractWorklist($dbh, $worklist);
    $html .= "Worklist " . $query->b($worklist) . " successfully retracted.";
  }
  &printhtml({body=>$html});
}
1;
