# Change the epoch
# suresh@nlm.nih.gov 12/98
# suresh@nlm.nih.gov 9/05 - EMS3 mods

# CGI params
# db=
# epoch=
# doit=
sub do_epoch {
  my($html);
  my($form) = $query->p;

  unless ($query->param('doit')) {

    $html .= <<"EOD";
Generally, the epoch is changed within a release cycle after one or more
major insertions.  There are typically just a few epochs in a year.
Make this change with care, since they cannot be easily reversed.
Changing the epoch implies the following:
EOD
    $html .= $query->p;
    $html .= $query->ul($query->li(
    [
    "Worklist numbers for all bins will be reset to 1",
    "The WMS query menu will change",
    "A subdirectory for this epoch will be created in the reports and worklists directories",
    "All worklists generated from now on will have this epoch in its prefix",
    ]));
    $html .= $query->p;

    $form .= $query->start_form(-method=>'POST', -action=>$query->url());
    $form .= "Change the current epoch from: " . $query->em($currentepoch) . " to " . $query->textfield({-name=>'epoch', -size=>3, -maxlength=>3});
    $form .= $query->submit(-value=>'OK');
    $form .= $DBpost;
    $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $form .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $form .= $query->end_form;

    $html .= $form;

  } else {

    my($epoch) = $query->param('epoch');

    unless ($epoch =~ /^\d\d[a-z]$/) {
      $html .= "Sorry!  Epochs should have the last two digits of the editing year followed by a single lowercase alphabetic character, e.g., 06b.";
      $html .= $query->p;
      $html .= "You had entered: " . $query->em($epoch);
      $html .= $query->p;
      &printhtml({title=>'Change Epoch', h1=>"Change Epoch", body=>$html, printandexit=>1});
    }
    EMSUtils->setCurrentEpoch($dbh, $epoch);
    foreach $bin_type (qw(ME QA AH)) {
      foreach $bin_name (EMSUtils->getBinNames($dbh, $bin_type)) {
	$bininfo = EMSUtils->getBininfo($dbh, $bin_name);

	my(%b) = ();
	$b{bin_name} = $bininfo->{bin_name};
	$b{bin_type} = $bininfo->{bin_type};
	$b{nextWorklistNum} = 1;
	$b{nextChemWorklistNum} = 1;
	$b{nextNonchemWorklistNum} = 1;
	EMSUtils->updateBininfo($dbh, \%b);
      }
    }

# create the subdirectory in data/reports
    $reportsdir = $ENV{EMS_HOME} . "/log/reports/$epoch";
    unless (-e $reportsdir) {
      unless (mkdir $reportsdir, 0775) {
	$html = "ERROR: failed to create directory: $reportsdir";
	&printhtml({title=>'Change Epoch', h1=>"Change Epoch", body=>$html, printandexit=>1});
	return;
      }
      unless (chmod 0775, $reportsdir) {
	$html = "ERROR: failed to chmod 0775 directory: $reportsdir";
	&printhtml({title=>'Change Epoch', h1=>"Change Epoch", body=>$html, printandexit=>1});
	return;
      }
    }
    $html .= "The current editing epoch was successfully changed to: " . $query->em($epoch);
  }
  &printhtml({title=>'Change Epoch', h1=>"Change Epoch", body=>$html});
}
1;
