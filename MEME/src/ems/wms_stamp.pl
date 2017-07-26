# suresh@nlm.nih.gov - 8/97
# Ported to Oracle - suresh@nlm.nih.gov 3/2000
# EMS3 - 12/2005

# Stamps (batch approves) contents of a worklist
# Only individual worklists can be stamped.

# CGI params:
# db=
# worklist=
# subaction= one of /stamp/i and /done/i
# doit=

sub do_wms_stamp {
  my($sql);
  my($html);
  my($worklist) = $query->param('worklist');
  my($editor);
  my($bininfo, $submit);
  my($subaction);

  unless ($query->param('doit')) {

    my($bin) = WMSUtils->worklist2bin($worklist);
    my($bininfo) = EMSUtils->getBininfo($dbh, $bin) if $bin;

    $html .= <<"EOD";
Stamping is batch approval of concepts on the worklist with the authority of the
editor assigned to edit the worklist.  However, concepts with content more recent
than when the worklist was created will be excluded from the approval process
so these will show up on future worklists.
EOD

# QA and AH bins are marked as Done
    $html .= $query->p;

    if ($bininfo && $bininfo->{bin_type} eq "ME") {
#      $html .= <<"EOD";
#This worklist appears to have been generated from an ME bin and
#can thus be stamped.  You can also simply mark this as DONE
#which will simply affect the WMS metadata for this worklist
#and not affect individual concepts.
#EOD

    } else {

      $html .= <<"EOD";
This worklist appears to have been generated from a QA or
AH bin.  These are typically marked as DONE and not stamped.
EOD
    }

    my(@d) = [[$query->submit({-name=>'subaction', -value=>'OK, stamp it!'}),
	       $query->submit({-name=>'subaction', -value=>'Mark as Done'})]];

    $html .= $query->p;
    $html .= "For the worklist: " . $query->b($worklist) . ", select one of the following:";
    $html .= $query->p;

    my($form);

    $form .= $query->start_form(-method=>'POST', -action=>$query->url());
    $form .= &toHTMLtable($query, {border=>0, cellspacing=>5, cellpadding=>2}, \@d);
    $form .= $query->hidden(-name=>'action', -value=>$action, -override=>1);
    $form .= $query->hidden(-name=>'worklist', -value=>$worklist, -override=>1);
    $form .= $query->hidden(-name=>'doit', -value=>1, -override=>1);
    $form .= $DBpost;
    $form .= $query->end_form;
    $html .= $form;

    &printhtml({h1=>'Retract worklist', body=>$html});
    return;
  }

  unless ($worklist) {
    &printhtml({printandexit=>1, body=>"No worklist specified!"});
  }

  unless ($dbh->tableExists($worklist)) {
    &printhtml({printandexit=>1, body=>"Worklist: $worklist does not exist!"});
  }

  $worklistinfo = WMSUtils->getWorklistinfo($dbh, $worklist);
  unless ($worklistinfo) {
    &printhtml({printandexit=>1, body=>"Information for worklist: $worklist could not be found!"});
  }

  $editor = $worklistinfo->{editor};
  unless ($editor) {
    &printhtml({printandexit=>1, body=>"Worklist: $worklist was not assigned to any editor!"});
  }

  if ($worklistinfo->{worklist_status} eq "ST" || $worklistinfo->{worklist_status} eq "DO") {
    &printhtml({printandexit=>1, body=>"Worklist: $worklist has already been stamped.  Please change the status to RT first."});
  }

  $subaction = ($query->param('subaction') =~ /stamp/i ? "stamp" : "done");
  $stamplogfile = join("/", $main::logdir, join(".", "stamplog", $db, $currentyear, $currentmonth, "log"));
  unless (-e $stamplogfile) {
    system "/bin/touch $stamplogfile";
    chmod(0775, $stamplogfile) || die "Cannot chmod 0775 $stamplogfile";
  }

  $html .= ($subaction eq "stamp" ?
	    &stamp_worklist($worklist, $worklistinfo) :
	    &markasdone_worklist($worklist, $worklistinfo)
	   );
  &printhtml({body=>$html, title=>"Stamping"});
}

# Only affects metadata for the worklist
sub markasdone_worklist {
  my($worklist, $worklistinfo) = @_;
  my($sql);
  my(@d);
  my(@text, $html, %info);

  $info{worklist_status} = $dbh->quote("DO");
  $info{stamp_date} = "SYSDATE";
  $info{stamp_time} = time-$starttime;
  $dbh->updateRow($EMSNames::WORKLISTINFOTABLE, "worklist_name", $dbh->quote($worklist), \%info);

# set other dates if needed
  $sql = "update $WORKLISTINFO set return_date=stamp_date where worklist_name=$qw and return_date is null";
  $dbh->executeStmt($sql);
  $sql = "update $WORKLISTINFO set assign_date=stamp_date where worklist_name=$qw and assign_date is null";
  $dbh->executeStmt($sql);

  @d = ();
  push @text, "\n\n" . "-" x 60 . "\n";
  $html .= $query->h1("Stamping $worklist");

  foreach $x (
	      ["Worklist", $worklist],
	      ["Time now", $now],
	      ["Database", $db],
	      ["Create date", $worklistinfo->{create_date}],
	     ) {
    
    push @text, join(': ', @$x) . "\n";
    push @d, $x;
  }
  $html .= &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@d);
  $html .= "Worklist was marked DONE.";

  push @text, "\nWorklist was marked DONE.";

  GeneralUtils->str2file(join('', @text), $stamplogfile, "append");
  return $html;
}

sub stamp_worklist {
  my($worklist, $worklistinfo) = @_;
  my($newerT); # recently approved concepts
  my($newerCAR); # concepts with more recent atoms, attributes, rels than worklist creation date
  my($extinctConcepts);
  my($willBeStamped, $willNotBeStamped);
  my($sql);
  my($cd) = "TO_DATE(" . $dbh->quote($worklistinfo->{create_date}) . ")";
  my($prefix) = $EMSNames::TMPTABLEPREFIX;
  my(@d, @d1, %d);
  my(@text, $html);
  my($starttime) = time;
  my(%info);

# Determine which concepts will actually be approved.  Those with newer atoms
# attributes or relationships will not be stamped

  $newerT = $dbh->tempTable($prefix . "_nT");
  $newerCAR = $dbh->tempTable($prefix . "_nCAR");
  $extinctConcepts = $dbh->tempTable($prefix . "_x");
  $willBeStamped = $dbh->tempTable($prefix . "_wS");
  $willNotBeStamped = $dbh->tempTable($prefix . "_nS");

  $dbh->dropTables([$newerT, $newerCAR, $extinctConcepts, $willBeStamped, $willNotBeStamped]);

  $sql = <<"EOD";
create table $newerT as
select distinct a.concept_id from concept_status a, $worklist b
where  a.concept_id=b.orig_concept_id
and    a.editing_timestamp>$cd
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $newerCAR as
select a.concept_id from classes a, $worklist b
where  a.concept_id=b.orig_concept_id
and    a.timestamp>$cd
and    a.status in ('N', 'n', 'U')
union
select a.concept_id from attributes a, $worklist b
where  a.concept_id=b.orig_concept_id
and    a.timestamp>$cd
and    a.status in ('N', 'n')
union
select a.concept_id_1 from relationships a, $worklist b
where  a.concept_id_1=b.orig_concept_id
and    a.timestamp>$cd
and    a.status in ('N', 'n')
union
select a.concept_id_2 from relationships a, $worklist b
where  a.concept_id_2=b.orig_concept_id
and    a.timestamp>$cd
and    a.status in ('N', 'n')
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $extinctConcepts as
select distinct orig_concept_id as concept_id from $worklist a
where  not exists (select concept_id from concept_status where concept_id=a.orig_concept_id)
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $willNotBeStamped as
select distinct concept_id from $newerT
union
select distinct concept_id from $newerCAR
union
select distinct concept_id from $extinctConcepts
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $willBeStamped as
select distinct orig_concept_id as concept_id from $worklist
where  orig_concept_id not in (select * from $willNotBeStamped)
EOD
  $dbh->executeStmt($sql);

  $info{n_not_stamped} = $dbh->selectFirstAsScalar("select count(*) from $willNotBeStamped");
  $info{n_stamped} = $dbh->selectFirstAsScalar("select count(*) from $willBeStamped");
  my($qe) = $dbh->quote("E-" . $worklistinfo->{editor});
  $info{n_approved_by_editor} = $dbh->selectFirstAsScalar(<<"EOD");
select count(*) from $newerT a, concept_status b
where  a.concept_id=b.concept_id
and    b.editing_authority=$qe
EOD

# Run the stamper
#  $ENV{ENV_HOME} = $main::EMSCONFIG{ENV_HOME};
#  $ENV{ENV_FILE} = $main::EMSCONFIG{ENV_FILE};

  $memelogfile = EMSUtils->tempFile;

  $memecmd = join(" ",
	      join("/", $ENV{EMS_HOME}, "bin", "stamping.pl"),
	      "-t=" . join('.', $main::EMSCONFIG{EMSSCHEMA}, $willBeStamped),
	      $db,
	      "S-" . $worklistinfo->{editor});

  system "$memecmd 2>&1 1>$memelogfile";
  $memelogstr = GeneralUtils->file2str($memelogfile);
  unlink  $memelogfile;

  my(%x1) = map { $_ => 1 } $dbh->selectAllAsArray("select distinct concept_id from $newerT"), $dbh->selectAllAsArray("select distinct concept_id from $newerCAR");
  my(%x2) = map { $_ => 1 } $dbh->selectAllAsArray("select distinct concept_id from $extinctConcepts");

  @d = ();
  push @text, "\n\n" . "-" x 60 . "\n";
  $html .= $query->h1("Stamping $worklist");

  foreach $x (
	      ["Worklist", $worklist],
	      ["Time now", $now],
	      ["Database", $db],
	      ["Create date", $worklistinfo->{create_date}],
	      ["Total original concepts", $dbh->selectFirstAsScalar("select distinct orig_concept_id from $worklist")],
	      ["Editor approved in interface", $info{n_approved_by_editor}],
	      ["Concepts stamped", $info{n_stamped}],
	      ["Concepts not stamped", $info{n_not_stamped}],
	      ["Concepts not stamped due to newer content", scalar(keys %x1)],
	      ["Concepts not stamped due to extinction", scalar(keys %x2)],
	      ["Authority", "S-" . $worklistinfo->{editor}],
	     ) {
    
    push @text, join(': ', @$x) . "\n";
    push @d, $x;
  }
  $html .= &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@d);

  @d = ();
  @d = $dbh->selectAllAsArray("select distinct concept_id from $willNotBeStamped");
  %d = map { $_ => MIDUtils->conceptPreferredName($dbh, $_) } @d;
  my($reason);
  my($n) = 0;

  push @text, "\n\n" . "-" x 60 . "\n";
  push @text, "Concepts that will not be stamped:\n\n";
  foreach $concept_id (@d) {
    $n++;
    $reason = ($x1{$concept_id} ? "Newer content" :
               $x2{$concept_id} ? "Extinct" : "");
    push @text, join("|", $n, $concept_id, $reason, $d{$concept_id}) . "\n";
  }

  $html .= $query->h2("Concepts not stamped");
  $html .= <<"EOD";
The following concepts were not stamped because they either do not exist
anymore, or have newer content added to them since this worklist was created.
<P>
EOD
  @d1 = ();
  $n = 0;
  foreach $concept_id (@d) {
    $n++;
    $url = $query->a({href=>$main::EMSCONFIG{MIDCONCEPTREPORTURL} . "?action=search&subaction=concept_id&arg=$concept_id&$DBget"}, $concept_id);
    $reason = ($x1{$concept_id} ? "Newer content" :
               $x2{$concept_id} ? "Extinct" : "");
    push @d1, [$n, $url, $reason, $d{$concept_id}];
  }
  $html .= &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@d1);

  @d = ();
  %d = ();
  @d = $dbh->selectAllAsArray("select distinct concept_id from $willBeStamped");
  %d = map { $_ => MIDUtils->conceptPreferredName($dbh, $_) } @d;

  push @text, "\n\n" . "-" x 60 . "\n";
  push @text, "Concepts that will be stamped:\n\n";

  $n = 0;
  foreach $concept_id (@d) {
    $n++;
    push @text, join("|", $n, $concept_id, $d{$concept_id}) . "\n";
  }

  $html .= $query->h2("Concepts stamped");
  $html .= <<"EOD";
The following concepts were stamped (see appended log from the stamping script).
<P>
EOD
  @d1 = ();
  $n=0;
  foreach $concept_id (@d) {
    $n++;
    $url = $query->a({href=>$main::EMSCONFIG{MIDCONCEPTREPORTURL} . "?action=search&subaction=concept_id&arg=$concept_id&$DBget"}, $concept_id);
    push @d1, [$n, $url, $d{$concept_id}];
  }
  $html .= &toHTMLtable($query, {border=>1, cellpadding=>5, cellspacing=>0}, \@d1);

  push @text, "\n\n" . "-" x 60 . "\n";
  push @text, "MEME stamp.pl log\n\n" . $memelogstr;

  $html .= $query->h2("MEME stamp.pl log");
  $html .= $query->pre($memelogstr);

  my($qw) = $dbh->quote($worklist);

# handle worklist metadata
  $sql = "delete from $EMSNames::BEINGEDITEDTABLE where worklist_name=$qw";
  $dbh->executeStmt($sql);

# set WORKLISTINFO dates
  my($WORKLISTINFO) = $EMSNames::WORKLISTINFOTABLE;

  $info{worklist_status} = $dbh->quote("ST");
  $info{stamp_date} = "SYSDATE";
  $info{stamp_time} = time-$starttime;
  $dbh->updateRow($WORKLISTINFO, "worklist_name", $qw, \%info);

# set other dates if needed
  $sql = "update $WORKLISTINFO set return_date=stamp_date where worklist_name=$qw and return_date is null";
  $dbh->executeStmt($sql);
  $sql = "update $WORKLISTINFO set assign_date=stamp_date where worklist_name=$qw and assign_date is null";
  $dbh->executeStmt($sql);

  $dbh->dropTables([$newerT, $newerCAR, $extinctConcepts, $willBeStamped, $willNotBeStamped]);

  GeneralUtils->str2file(join('', @text), $stamplogfile, "append");
  return $html;
}

1;

