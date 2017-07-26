# suresh@nlm.nih.gov 10/98
# suresh@nlm.nih.gov 1/00 - Modified for Oracle
# suresh@nlm.nih.gov 7/05 - Mods for EMS3

# CGI params
# bin_name= (if bin_name is __ALL__, then all bins are regenerated)
# order_by=
# batch= (for batch runs, prints a useful message after generation)

# Generates contents for a AH bin
sub do_ah_generate {
  my($html);
  my($bin_name) = $query->param('bin_name');
  my($order_by) = $query->param('order_by');

  &printhtml({printandexit=>1, body=>"Missing a bin name to generate contents"}) unless $bin_name;

  if ($bin_name eq "__ALL__") {
    my(@bins) = EMSUtils->getBinNames($dbh, "AH");
    foreach $bin_name (@bins) {
      EMSBinlock->unlock($dbh, {bin_name=>$bin_name});
      $query->param(-name=>'bin_name', -value=>$bin_name);
      &do_ah_generate;
    }
    &printhtml({redirect=>$query->url() . "?$DBget&action=ah_bins&order_by=$order_by"});

  } else {

    if (EMSBinlock->islocked($dbh, {bin_name=>$bin_name})) {
      my($l) = EMSBinlock->get($dbh, {bin_name=>$bin_name});
      my($msg);

      $msg = join(" ",
		  "Bin:",
		  $query->strong($bin_name),
		  "is currently locked for use by user:",
		  $query->strong($l->{owner}),
		  "at:",
		  $l->{timestamp});
      $msg .= $query->p;
      $msg .= "Reason: " . $l->{reason};

      if ($query->param('batch')) {
	print "Bin: $bin_name is currently locked for use by ", $l->{owner}, "\n";
      } else {
	&printhtml({h1=>'Checklist', body=>$msg, printandexit=>1});
      }
    }

    EMSBinlock->lock($dbh, {bin_name=>$bin_name, reason=>"generating contents", owner=>($httpuser||$unixuser)});
    EMSUtils->bin_generate($dbh, $bin_name, 'AH');
    EMSBinlock->unlock($dbh, {bin_name=>$bin_name});

    if ($query->param('batch')) {
      print "Bin: $bin_name regenerated", "\n";
    } else {
      &printhtml({redirect=>$query->url() . "?$DBget&action=ah_bins&bin_name=$bin_name&order_by=$order_by"});
    }
  }
  return;
}
1;
