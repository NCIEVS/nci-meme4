# WMS related utility functions
# suresh@nlm.nih.gov 3/2005

package WMSUtils;

use lib "$ENV{EMS_HOME}/lib";

use Data::Dumper;

use EMSNames;
use EMSTables;
use EMSMaxtab;
use Symbol;

#################### CONCEPT REPORT ####################

# We recognize these options:

# Input can be lists of concept_id, atom_id or CUIs or clusters of these, or files of these
# ids=>{idtype=>concept_id, atom_id or cui, cluster_id=>, list=>[]}
# clusters=>[{idtype=>, cluster_id=>, list=>[]}, ...]
# file=>{clustered=>true or false, idtype=>concept_id, atom_id or cui, name=>}
# reporttype=> 1 is standard, 2 is abbreviated, 3 is for w/o contexts

# maxreviewedrels=> (specifies the maximum number of reviewed relationships)
# lat=> (comma separated list of languages to restrict to)
# outputformat=> one of text, html, enscript
# formfeed=>

# These options deal with which rels to show (see xreports.pl)
# r=> 0,1,2 or 3 for (NONE, DEFAULT, XR, ALL resp.)
# x=> 0,1,2 or 3 for (NONE, DEFAULT, SIB, ALL resp.)

# db=> (data source) or dbh=>(DB handle)
# service=> (data source)
# env_home=>
# env_file=>
# meme_host=>
# meme_port=>

# -url_mid_for_concept_id (a URL prefix for links to reports by concept ID - the ID is appended)
# -url_mid_for_code (a URL prefix for links to reports by code - the code is appended)
# -url_mid_for_cui (a URL prefix for links to reports by CUI - the CUI is appended)
# -url_release_for_cui (a URL prefix for links to release reports by CUI - the CUI is appended)
# -url_release_for_sty (a URL prefix for links to release reports by STY - the STY is appended)

# A wrapper for Apelon's xreports.pl script
# Used to generate a report for a worklist or checklist
# Options are passed in a hashref (see above, these are mapped to xreports options)

sub xreports {
  my($class, $optref) = @_;
  my(@scriptopts);
  my($cmd);
  my($report);

# Input options
  if ($optref->{ids}) {

    if ($optref->{ids}->{cluster_id}) {
      push @scriptopts, "-cluster " . $optref->{ids}->{cluster_id};
    }

    if ($optref->{ids}->{idtype} eq "concept_id") {
      push @scriptopts, "-c " . join(',', @{$optref->{ids}->{list}});
    }
    if ($optref->{ids}->{idtype} eq "atom_id") {
      push @scriptopts, "-a " . join(',', @{$optref->{ids}->{list}});
    }
    if ($optref->{ids}->{idtype} eq "cui") {
      push @scriptopts, "-i " . join(',', @{$optref->{ids}->{list}});
    }

  }

  if ($optref->{clusters}) {
    my($clusters) = $optref->{clusters};
    $optref->{clusters} = undef;
    my($cluster);

    foreach $cluster (@$clusters) {
      $optref->{ids} = $cluster;
      $report .= $class->xreports($optref);
    }
    return $report;
  }

  if ($optref->{file}) {
    my($f) = $optref->{file}->{name};

    if ($optref->{file}->{clustered}) {
      push @scriptopts, "-cluster";
    }

    if ($optref->{file}->{idtype} eq "concept_id") {
      push @scriptopts, "-fc $f";
    } elsif ($optref->{file}->{idtype} eq "atom_id") {
      push @scriptopts, "-fa $f";
    } elsif ($optref->{file}->{idtype} eq "cui") {
      push @scriptopts, "-fi $f";
    }
  }

  if ($optref->{maxreviewedrels}) {
    push @scriptopts, "-max " . $optref->{maxreviewedrels};
  }
  if (defined $optref->{r}) {
    push @scriptopts, "-r " . $optref->{r} || "0";
  }
  if (defined $optref->{x}) {
    push @scriptopts, "-x " . $optref->{x} || "0";
  }
  if ($optref->{lat}) {
    push @scriptopts, "-lat " . $optref->{lat};
  }
  
  if ($optref->{outputformat} eq "html") {
    push @scriptopts, "-html";

# set these for the hyperlinks in the reports
    push @scriptopts, "-url_mid_for_concept_id=" . $optref->{'-url_mid_for_concept_id'};
    push @scriptopts, "-url_mid_for_code=" . $optref->{'-url_mid_for_code'};
    push @scriptopts, "-url_mid_for_cui=" . $optref->{'-url_mid_for_cui'};
    push @scriptopts, "-url_release_for_cui=" . $optref->{'-url_release_for_cui'};
    push @scriptopts, "-url_release_for_sty=" . $optref->{'-url_release_for_sty'};
  }
if ($optref->{reporttype} eq "3") {
   push @scriptopts, "-reporttype=3";
}
  if ($optref->{outputformat} eq "enscript") {
    push @scriptopts, "-enscript";
  }
  push @scriptopts, "-ff" if ($optref->{form_feed});

  push @scriptopts, "-d " . $optref->{db} if ($optref->{db});
  push @scriptopts, "-d " . $optref->{service} if ($optref->{service});
  push @scriptopts, "-host " . $optref->{meme_host} if ($optref->{meme_host});
  push @scriptopts, "-port " . $optref->{meme_port} if ($optref->{meme_port});

  $ENV{ENV_HOME} = $optref->{env_home} if $optref->{env_home};
  $ENV{ENV_FILE} = $optref->{env_file} if $optref->{env_file};

  die "ERROR: ENV_HOME not set" unless $ENV{ENV_HOME};
  die "ERROR: ENV_FILE not set" unless $ENV{ENV_FILE};

  $cmd = $ENV{EMS_HOME} . "/bin/xreports.pl " . join(" ", @scriptopts);
  $report = `$cmd`;
  return $report;
}

# Returns a type #2 report (concept name + STY)
sub report_2 {
  my($class, $optref) = @_;
  my($report);

# Input options
  if ($optref->{ids}) {
    my($concept_id, $name);

    if ($optref->{ids}->{cluster_id}) {
      my($cluster_id) = $optref->{ids}->{cluster_id};
      my($label) = " Cluster \#" . $cluster_id . " ";
      my($n) = (80-length($label))/2;
      $report .= join("", "-" x $n, $label, "-" x $n) . "\n";
    }

    foreach $concept_id (@{ $optref->{ids}->{list} }) {
      $name = MIDUtils->conceptPreferredName($optref->{dbh}, $concept_id);
      unless ($name) {
	$report .= <<"EOD";
CN\#:\t$concept_id: *** Concept does not exist any more ****

EOD
      } else {
	my(@stys) = MIDUtils->getSTYs($optref->{dbh}, $concept_id);
	my($stys) = join("\nSTY:\t", @stys);
	$report .= <<"EOD";
CN\#:\t$concept_id
Name:\t$name
STY:\t$stys

EOD
      }
    }
    return $report;
  }

  if ($optref->{clusters}) {
    my($clusters) = $optref->{clusters};
    $optref->{clusters} = undef;
    my($cluster);

    foreach $cluster (@$clusters) {
      $optref->{ids} = $cluster;
      $report .= $class->report_2($optref);
    }
    return $report;
  }

  if ($optref->{file}) {
    my($f) = $optref->{file}->{name};
    $optref->{file} = undef;
    $optref->{clusters} = WMSUtils->file2clusters($f);
    return $class->report_2($optref);
  }
}

# Refreshes the current concept membership of the atoms in a worklist and writes the concepts to a file
sub worklist2file {
  my($self, $dbh, $worklist, $conceptfile) = @_;
  my($sql);

# refresh the worklist table to find current atom memberships
  $sql = <<"EOD";
select distinct concept_id, cluster_id from (
  select c.concept_id, c.atom_id, w.row_id, w.cluster_id from classes c, $worklist w
  where  w.atom_id=c.atom_id
  order  by cluster_id, row_id, concept_id
) order by cluster_id, concept_id
EOD
  $dbh->selectToFile($sql, $conceptfile);
}

# makes a report for (clustered) concept_ids in a file
sub file2report {
  my($class, $conceptfile, $optref) = @_;
  my(%o);

  $o{file} = { clustered=>1, idtype=>'concept_id', name=>$conceptfile };
  foreach $key (keys %$optref) {
    $o{$key} = $optref->{$key};
  }
  if ($optref->{reporttype}==1 || $optref->{reporttype}==3) {
    return WMSUtils->xreports(\%o);
  } else {
    return WMSUtils->report_2(\%o);
  }
}

# helper function for returning a report for a single concept
sub conceptreport {
  my($class, $concept_id, $db, $outputformat) = @_;
  $outputformat = "text" unless $outputformat;
  return $class->xreports({ids=>{idtype=>'concept_id', list=>[$concept_id]}, env_home=>$main::EMSCONFIG{ENV_HOME, env_file=>$main::EMSCONFIG{ENV_FILE}}, db=>$db, outputformat=>$outputformat});
}

# helper function for returning a report for a cluster of concept_id
sub clusterreport {
  my($class, $concept_ids, $cluster_id, $db, $outputformat) = @_;
  $outputformat = "text" unless $outputformat;
  $cluster_id=1 unless $cluster_id;
  return $class->xreports({ids=>{idtype=>'concept_id', cluster_id=>$cluster_id, list=>$concept_ids}, env_home=>$main::EMSCONFIG{ENV_HOME}, env_file=>$main::EMSCONFIG{ENV_FILE}, db=>$db, outputformat=>$outputformat});
}

# takes a file of concept_id|cluster_id pairs and returns a
# list ref suitable for xreports' ids argument
sub file2clusters {
  my($self, $file) = @_;
  my($fd);
  my($cluster_id, $last_cluster_id);
  my($internal_cluster_id) = 1;
  my($conceptlist) = [];
  my(@clusters);

  if (ref($file) ne "GLOB") {
    $fd = gensym;
    open($fd, $file) || die "Cannot open $file";
  } else {
    $fd = $file;
  }

  $last_cluster_id = -1;
  while (<$fd>) {
    chomp;
    ($concept_id, $cluster_id) = split /\|/, $_, 2;
    $cluster_id = $internal_cluster_id unless $cluster_id;

    if ($last_cluster_id > 0 && $cluster_id != $last_cluster_id) {
      push @clusters, {
		       idtype=>'concept_id',
		       cluster_id=>$last_cluster_id,
		       list=>$conceptlist,
		       };

      $conceptlist = [];
      $internal_cluster_id++;
    }
    push @$conceptlist, $concept_id;
    $last_cluster_id = $cluster_id;
  }
  if (@$conceptlist > 0) {
    push @clusters, {
		     idtype=>'concept_id',
		     cluster_id=>$last_cluster_id,
		     list=>$conceptlist,
		    };
  }
  return \@clusters;
}

# returns worklist or checklist metadata as hash
sub getListinfo {
  my($class, $dbh, $list) = @_;
  if ($class->isChecklist($list)) {
    return EMSTables->row2hash($dbh, $EMSNames::CHECKLISTINFOTABLE, 'checklist_name', $dbh->quote($list));
  } else {
    return EMSTables->row2hash($dbh, $EMSNames::WORKLISTINFOTABLE, 'worklist_name', $dbh->quote($list));
  }
}


# returns worklist metadata as hash
sub getWorklistinfo {
  my($class, $dbh, $worklist) = @_;
  return EMSTables->row2hash($dbh, $EMSNames::WORKLISTINFOTABLE, 'worklist_name', $dbh->quote($worklist));
}

# returns checklist metadata as hash
sub getChecklistinfo {
  my($class, $dbh, $checklist) = @_;
  return EMSTables->row2hash($dbh, $EMSNames::CHECKLISTINFOTABLE, 'checklist_name', $dbh->quote($checklist));
}

# returns the parent bin for a worklist or checklist
sub getParentBin {
  my($class, $dbh, $list) = @_;
#  my($LISTINFO) = ($class->isChecklist($list) ? $EMSNames::CHECKLISTINFOTABLE : $EMSNames::WORKLISTINFOTABLE);
#  my($key) = ($class->isChecklist($list) ? 'checklist_name' : 'worklist_name');
#  my($sql) = "select bin_name from $LISTINFO where $key=" . $dbh->quote($list);

  return $class->worklist2bin($list) unless $class->isChecklist($list);
  return $dbh->selectFirstAsScalar("select bin_name from $EMSNames::CHECKLISTINFOTABLE where checklist_name=" . $dbh->quote($list));
}

# Infers a bin name from a worklist name
sub worklist2bin {
  my($class, $worklist) = @_;
  my($bin);

  if ($worklist =~ /^wrk\d\d[a-z]_(.*)_ch_\d+$/i) {
    $bin = $1;
  } elsif ($worklist =~ /^wrk\d\d[a-z]_(.*)_nc_\d+$/i) {
    $bin = $1;
  } elsif ($worklist =~ /^wrk\d\d[a-z]_(.*)_\d+$/i) {
    $bin = $1;
  }
  $bin =~ tr/A-Z/a-z/;
  return $bin;
}

# Returns an SQL expression to compute a bin name from a worklist name
sub worklist2binSQL {
  my($class, $col) = @_;

  $col = "worklist_name" unless $col;
  my($sql) = <<"EOD";
decode(
	lower(substr(substr($col, 1, instr($col, '_', -1)-1), -3, 3)),
		'_ch',	substr($col, 8, instr($col, '_', -1)-11),
		'_nc',	substr($col, 8, instr($col, '_', -1)-11),
			substr($col, 8, instr($col, '_', -1)-8))
EOD
  return $sql;
}

# true if a list is a checklist
sub isChecklist {
  my($class, $list) = @_;
  return $list =~ /^CHK_/i;
}

# -----------------------------
1;
