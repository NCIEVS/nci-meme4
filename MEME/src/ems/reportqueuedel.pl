# suresh@nlm.nih.gov - 12/2005

# Deletes one or more report requests
# CGI params:
# config=
# db=
# freezefile=
# start=
# n=
sub do_reportqueuedel {
  my($freezefile) = $query->param('freezefile');
  my($REPORTSDIR) = $ENV{EMS_LOG_DIR} . "/requests";

  &printhtml({body=>'ERROR: Need a request freezefile name.', printandexit=>1}) unless $freezefile;

  $freezefile = join("/", $REPORTSDIR, $freezefile);
  unlink $freezefile if -e $freezefile;

  my($url) = $query->url() . "?$DBget&action=reportqueue&start=" . $query->param('start') . "&n=" . $query->param('n');
  &printhtml({redirect=>$url});
  return;
}
1;
