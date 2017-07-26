# Link to the concept report generator
# suresh@nlm.nih.gov 12/98
# suresh@nlm.nih.gov 7/05 - EMS3 mods

sub do_conceptreport {
  my($conceptreporturl) = $main::EMSCONFIG{MIDCONCEPTREPORTURL} . "?$DBget";
  &printhtml({redirect=>$conceptreporturl});
}
1;
