# Makes a checklist from a uploaded file or by sampling from a table
# suresh@nlm.nih.gov 3/06

# CGI params
# checklist_suffix=
# uploadfile=
# table_name=
# limit=
# randomize=

# NOTE: there is a max limit of 1000 lines in the input file or
# clusters in existing tables to safeguard against malicious hacking

sub do_make_checklist {
  my($html);
  my($wmsurl) = $main::EMSCONFIG{LEVEL0WMSURL};
  my($limit) = "1000";
  my($table_name) = $query->param('table_name');
  my($uploadfile) = $query->param('uploadfile');
  my($title) = "Make checklist";
  my($checklist_suffix) = $query->param('checklist_suffix');

  unless ($checklist_suffix) {
    my($form1, $form2);
    my(@d);
    my(@limit) = (500,400,300,200,100,50,10,10000);
    my($defaultlimit) = 300;

    my($remoteIP) = $ENV{REMOTE_ADDR};
    $remoteIP =~ s/\./\_/g;
    $checklist_suffix = $remoteIP || ($httpuser || $unixuser || "unknown");
    $checklist_suffix =~ s/[^0-9a-zA-Z_]/_/g;
    $checklist_suffix = substr($checklist_suffix, 0, 18);

    $html .= $query->h2("Checklist from table");
    $html .= "Make a checklist by selecting concepts from a MID table.";
    $html .= $query->p;

    $html .= $query->start_form({-method=>'POST', -action=>$query->url()});
    push @d, ["Enter name of concept table: ", $query->textfield(-name=>'table_name', -size=>32, -limit=>32)];
    push @d, ["Checklist name: " . $query->b("chk_"),
	      $query->textfield({-name=>'checklist_suffix', -value=>$checklist_suffix, -size=>18, -maxlength=>18})];
    push @d, ["Maximum number of clusters:", $query->popup_menu({-name=>'limit', -values=>\@limit, -default=>$defaultlimit})];
    push @d, ["Randomize the clusters:", $query->checkbox({-name=>'randomize', -label=>''})];
    $html .= &toHTMLtable($query, {border=>1, -cellpadding=>5, -cellspacing=>0}, \@d);

    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->br;
    $html .= $query->submit;

    $html .= $DBpost;
    $html .= $query->end_form();

    @d = ();
    $html .= $query->p;
    $html .= $query->hr;

    $html .= $query->h2("Checklist from local file");
    $html .= "Make a checklist by uploading local file of concepts.";
    $html .= $query->p;

    $html .= $query->start_multipart_form({-method=>'POST', -action=>$query->url()});
    $html .= "Select file: " . $query->filefield(-name=>'uploadfile');
    $html .= $query->p;

    push @d, ["Checklist name: " . $query->b("chk_"),
	      $query->textfield({-name=>'checklist_suffix', -value=>$checklist_suffix, -size=>18, -maxlength=>18})];
    push @d, ["Maximum number of clusters:", $query->popup_menu({-name=>'limit', -values=>\@limit, -default=>$defaultlimit})];
    push @d, ["Randomize the clusters:", $query->checkbox({-name=>'randomize', -label=>''})];
    $html .= &toHTMLtable($query, {border=>1, -cellpadding=>5, -cellspacing=>0}, \@d);

    $html .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $html .= $query->br;
    $html .= $query->submit;

    $html .= $DBpost;
    $html .= $query->end_form();

    $html .= $query->p;
    &printhtml({title=>$title, h1=>$title, body=>$html});
    return;
  }

  $limit = $query->param('limit') || $defaultlimit;
  $randomize = $query->param('randomize');
  $checklist_name = "chk_" . $checklist_suffix;

  if ($query->param('table_name')) {
    my(%p);
    foreach (qw(table_name limit randomize)) {
      $p{$_} = $query->param($_);
    }
    $p{owner} = ($httpuser || $unixuser);

    &printhtml({printandexit=>1, body=>"ERROR: table $table_name does not exist or has insufficient privileges."})
      unless $dbh->tableExists($table_name);
    &printhtml({printandexit=>1, body=>"ERROR: table $table_name does not have a CONCEPT_ID column."})
      unless $dbh->tableHasColumn($table_name, "CONCEPT_ID");

    EMSUtils->makeChecklist($dbh, $checklist_name, \%p);
    &printhtml({redirect=>$main::EMSCONFIG{LEVEL0WMSURL} . "?$DBget&action=pickcheck"})

  } elsif ($uploadfile) {
    my($tmpfile) = EMSUtils->tempFile($EMSNames::TMPFILEPREFIX);
    my($errmsg);

    unlink $tmpfile;
    $n = 0;
    open(T, ">$tmpfile") || die "Cannot open $tmpfile";
    while (<$uploadfile>) {
      chomp;
      s/^\s*//;
      s/\s*$//;
      s/\r$//;

      next unless /^\d/;

      ($concept_id, $cluster_id) = split /\|/, $_;

      unless ($concept_id =~ /^\d+$/) {
	$errmsg = "ERROR: Need a concept_id in the first field in file: $uploadfile";
	last;
      }

      if ($cluster_id) {
	if ($cluster_id !~ /^\d+$/) {
	  $errmsg = "ERROR: Bad cluster_id: $cluster_id in the second field in file: $uploadfile";
	  last;
	} else {
	  $n++;
	}
      } else {
	$n++;
	$cluster_id=$n;
      }
      last if $n>=$limit;
      print T join("|", $concept_id, $cluster_id), "\n";
    }
    close($uploadfile);
    close(T);

    &printhtml({printandexit=>1, body=>$errmsg}) if $errmsg;

    my(%p);
    foreach (qw(limit randomize)) {
      $p{$_} = $query->param($_);
    }
    $p{owner} = ($httpuser || $unixuser);
    $p{file} = $tmpfile;

    EMSUtils->makeChecklist($dbh, $checklist_name, \%p);
    unlink $tmpfile;
    &printhtml({redirect=>$main::EMSCONFIG{LEVEL0WMSURL} . "?$DBget&action=pickcheck"})
  }
  &printhtml({title=>"WMS: Checklists", h1=>"View or delete checklists", body=>$html});
}

1;
