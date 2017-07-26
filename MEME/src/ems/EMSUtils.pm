# EMS related utility functions
# suresh@nlm.nih.gov 8/2002

package EMSUtils;
unshift @INC, "$ENV{ENV_HOME}/bin";

require "env.pl";
use lib "$ENV{EMS_HOME}/lib";

use Data::Dumper;
use File::Copy;
use File::Path;
use Symbol;

use EMSNames;
use EMSTables;
use EMSMaxtab;
use MIDUtils;
use GeneralUtils;

$BIGRANK = 1000000;
# Can be one of "ONE" or "ALL" - determines how demotions cluster are treated as chem or nonchem
# If ONE, a single chemical concept makes the cluster a chemical, if ALL, a single non-chem concept
# makes the cluster a nonchem cluster
$DEFAULTCHEMALGO="ONE";

# Initializes the EMS
sub init {
  my($self, $dbh) = @_;
  my($table);
  die "The EMS_HOME environment variable needs to be set\n" unless $ENV{'EMS_HOME'};

  $self->loadConfig;

# set environment variables


# ensure that all EMS tables are present
  foreach $table (keys %EMSTables::TABLESPEC) {
    EMSTables->createTable($dbh, $table) unless $dbh->tableExists($table);
  }
}

# constructs the config file name
sub configFile {
  my($class) = @_;
  my($config);

# config file is either specified via EMS_CONFIG or the default in EMS_HOME/config
  if ($ENV{EMS_CONFIG}) {
    $config = join("/", $ENV{EMS_HOME}, "etc", $ENV{EMS_CONFIG});
  } else {
    $config = join("/", $ENV{EMS_HOME}, "etc", "ems.config");
  }
  return $config;
}

# reads in the config file for the EMS (in $EMS_HOME/config/ems.config)
sub loadConfig {
  my($self) = @_;
  my($config) = $self->configFile;
  my(%multi);

  return if $EMSCONFIGLOADED++;

# first pass just looks for the MULTI_VALUED_PROPERTY property, which itself is
  $multi{MULTI_VALUED_PROPERTY}++;
  open(C, $config) || die $@;
  while (<C>) {
    chomp;
    next if /^\s*$/ || /^\#/;
    @_ = split /=/, $_;
    next unless $_[0] eq "MULTI_VALUED_PROPERTY";
    $multi{$_[1]}++;
  }
  close(C);

  open(C, $config) || die $@;
  my($n) = 1;
  while (<C>) {
    chomp;
    next if /^\s*$/ || /^\#/;
    @_ = split /=/, $_;
    if ($multi{$_[0]}) {
      if ($main::EMSCONFIG{$_[0]}) {
	push @{ $main::EMSCONFIG{$_[0]} }, $_[1];
      } else {
	$main::EMSCONFIG{$_[0]} = [ $_[1] ];
      }
    } else {
      $main::EMSCONFIG{$_[0]} = $_[1];
    }
    $main::EMSCONFIGORDER{$_[0]} = $n++;
  }
  close(C);
}

# adds the predefined ME bins
sub addPredefinedMEbins {
  my($self, $dbh) = @_;
  my($MECONFIG) = $EMSNames::MECONFIGTABLE;
  my($sql);
  my($nullstr) = $dbh->quote('');

  $sql = "delete from $MECONFIG where predefined = 'Y'";
  $dbh->executeStmt($sql);

  my($rank) = 1;

  $dbh->insertRow($MECONFIG, {
			      bin_name=>$dbh->quote('testconcepts'),
			      description=>$dbh->quote('Concepts use for testing only - not releasable'),
			      editable=>$dbh->quote('N'),
			      predefined=>$dbh->quote('Y'),
			      rank=>$rank++,
			      content_type=>$nullstr,
			      generator=>$dbh->quote('SUB'),
			      orderer=>$nullstr,
			      chemalgo=>$nullstr,
			     });
  $dbh->insertRow($MECONFIG, {
			      bin_name=>$dbh->quote('demotions'),
			      description=>$dbh->quote('Clustered concepts that share a similar atom.  Must be related or merged'),
			      editable=>$dbh->quote('Y'),
			      predefined=>$dbh->quote('Y'),
			      rank=>$rank++,
			      content_type=>$dbh->quote("MIXED"),
			      generator=>$dbh->quote('SUB'),
			      orderer=>$nullstr,
			      chemalgo=>$dbh->quote("ONE"),
			     });
  $dbh->insertRow($MECONFIG, {
			      bin_name=>$dbh->quote('embryos'),
			      description=>$dbh->quote('Concepts still being formed - cannot be edited'),
			      editable=>$dbh->quote('N'),
			      predefined=>$dbh->quote('Y'),
			      rank=>$rank++,
			      content_type=>$nullstr,
			      generator=>$dbh->quote('SUB'),
			      orderer=>$nullstr,
			      chemalgo=>$nullstr,
			     });
  $dbh->insertRow($MECONFIG, {
			      bin_name=>$dbh->quote('norelease'),
			      description=>$dbh->quote('Concepts where all atoms are unreleasable'),
			      editable=>$dbh->quote('N'),
			      predefined=>$dbh->quote('Y'),
			      rank=>$rank++,
			      content_type=>$nullstr,
			      generator=>$dbh->quote('SUB'),
			      orderer=>$nullstr,
			      chemalgo=>$nullstr,
			     });
  $dbh->insertRow($MECONFIG, {
			      bin_name=>$dbh->quote('noreview'),
			      description=>$dbh->quote('Concepts that will not be re-reviewed'),
			      editable=>$dbh->quote('N'),
			      predefined=>$dbh->quote('Y'),
			      rank=>$rank++,
			      content_type=>$nullstr,
			      generator=>$dbh->quote('SUB'),
			      orderer=>$nullstr,
			      chemalgo=>$nullstr,
			     });
  $dbh->insertRow($MECONFIG, {
			      bin_name=>$dbh->quote('reviewed'),
			      description=>$dbh->quote('Concepts already reviewed'),
			      editable=>$dbh->quote('N'),
			      predefined=>$dbh->quote('Y'),
			      rank=>$rank++,
			      content_type=>$nullstr,
			      generator=>$dbh->quote('SUB'),
			      orderer=>$nullstr,
			      chemalgo=>$nullstr,
			     });
  $dbh->insertRow($MECONFIG, {
			      bin_name=>$dbh->quote('leftovers'),
			      description=>$dbh->quote('Unclassified but editable concepts'),
			      editable=>$dbh->quote('Y'),
			      predefined=>$dbh->quote('Y'),
			      rank=>$BIGRANK,
			      content_type=>$dbh->quote('MIXED'),
			      generator=>$nullstr,
			      orderer=>$dbh->quote('CONCEPT_ID'),
			      chemalgo=>$dbh->quote("ONE"),
			     });
}

# returns a bin's configuration as a hash ref
sub getBinconfig {
  my($class, $dbh, $configtable, $bin) = @_;
  return EMSTables->row2hash($dbh, $configtable, 'bin_name', $dbh->quote($bin));
}

# returns a bin's metadata as a hash ref
sub getBininfo {
  my($class, $dbh, $bin) = @_;
  return EMSTables->row2hash($dbh, $EMSNames::BININFOTABLE, 'bin_name', $dbh->quote($bin));
}

# returns the current editing epoch
sub getCurrentEpoch {
  my($class, $dbh) = @_;
  my($sql) = "select max(epoch) from $EMSNames::EDITINGEPOCHTABLE where active=" . $dbh->quote("Y");
  my($epoch) = $dbh->selectFirstAsScalar($sql);

  die "ERROR: Current editing epoch not set in $EMSNames::EDITINGEPOCHTABLE" unless $epoch;
  return $epoch;
}

# sets the current editing epoch
sub setCurrentEpoch {
  my($class, $dbh, $newepoch) = @_;
  $dbh->insertRow($EMSNames::EDITINGEPOCHTABLE, {epoch=>$dbh->quote($newepoch), active=>$dbh->quote("Y"), create_date=>"SYSDATE"});
  die $@ if $@;
}

# Partitions the concepts into Mutually exclusive bins
sub ME_partition {
  my($self, $dbh) = @_;
  my($r);
  my($sql, $bin);
  my($startsecs, $endsecs);
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($MECONFIGTABLE) = $EMSNames::MECONFIGTABLE;
  my(%bininfo, $binconfig);
  my($user);
  
  $startsecs = time;
  $user = $ENV{REMOTE_USER} || GeneralUtils->username() || "unknown";

# partition log contains more details than the EMS log
  EMSUtils->ems_log($main::emslogfile, "STARTING ME_BINS partitioning");
  EMSUtils->plain_log($main::partitionlogfile, "Starting ME partitioning");

  EMSMaxtab->set($dbh, $EMSNames::MEPARTITIONINGLOCKKEY, {valueint=>1, valuechar=>$user, timestamp=>"SYSDATE" });
  EMSMaxtab->set($dbh, $EMSNames::MEPARTITIONDATEKEY, {timestamp=>'SYSDATE'});
  EMSMaxtab->set($dbh, $EMSNames::MEPARTITIONUSERKEY, {valuechar=>$user});

  $self->meconfig2table($dbh, $self->configfile($dbh, "ME"), $MECONFIGTABLE);

# initialize the ME bins table
  EMSUtils->plain_log($main::partitionlogfile, "Initializing $MEBINSTABLE table");

  $dbh->dropTable($MEBINSTABLE);
  EMSTables->createTable($dbh, $MEBINSTABLE);
  $sql = <<"EOD";
insert into $MEBINSTABLE (bin_name, concept_id, cluster_id, ischemical)
select 'leftovers' as bin_name, concept_id, rownum as cluster_id, 'N' as ischemical from concept_status
EOD
  $dbh->executeStmt($sql);

# create indexes
#  $dbh->createIndex($MEBINSTABLE, 'concept_id', EMSUtils->tempIndex($dbh));
#  $dbh->createIndex($MEBINSTABLE, 'bin_name');
#  $dbh->createIndex($MEBINSTABLE, 'cluster_id');

# generate the bin contents in rank order
  $sql = "select bin_name from $MECONFIGTABLE order by rank";
  @bins = $dbh->selectAllAsArray($sql);
  foreach $bin (@bins) {
    $generator = "";
    $generatortype = "";
    $generatorcomponent = "";

    EMSUtils->plain_log($main::partitionlogfile, "Generating contents for $bin");
    $binconfig = EMSUtils->getBinconfig($dbh, $MECONFIGTABLE, $bin);
    $generator = $binconfig->{generator};

    %bininfo = ();
    $bininfo{bin_name} = $bin;
    $bininfo{bin_type} = 'ME';
    $bininfo{generation_date} = "SYSDATE";
    $bininfo{generation_time} = time;
    $bininfo{generation_user} = $main::httpuser || $main::unixuser || "unknown";

    if ($generator) {
      $generator =~ /^([^:]+):?(.*)$/;
      $generatortype = uc($1);
      $generatorcomponent = $2;

      $orderer = uc($binconfig->{orderer});
      $editable = uc($binconfig->{editable});

      EMSUtils->ems_log($main::emslogfile, "Starting to generate contents for $bin");

      if ($generatortype eq "SUB") {
	my($gensub) = "generate_" . $bin;
	$self->$gensub($dbh, $bin, $orderer);
      } elsif ($generatortype eq "SAB") {
	$self->generate_by_sab($dbh, $bin, [ split /[,\s]+/, $generatorcomponent ]);
      } elsif ($generatortype eq "SCRIPT") {
	$self->generate_by_script($dbh, $bin, $generatorcomponent);
      } elsif ($generatortype eq "ORDER_ID") {
	$self->generate_by_order_id($dbh, $bin, $generatorcomponent);
      }

      EMSUtils->ems_log($main::emslogfile, "Done generating contents for $bin");
      EMSUtils->plain_log($main::partitionlogfile, "Finished generating contents for $bin");
    }

    $orderer = uc($binconfig->{orderer}) || "CONCEPT_ID";

    EMSUtils->plain_log($main::partitionlogfile, "Starting to order contents for $bin");
    EMSUtils->ems_log($main::emslogfile, "Starting to order contents for $bin");
    if ($orderer eq "ATOM_ORDERING") {
      die "ERROR: generator must be SAB for ATOM_ORDERING" if ($generatortype ne "SAB");
      my(@rsabs) = MIDUtils->makeVersionlessSAB($dbh, [ split /[,\s]+/, $generatorcomponent ]);
      $self->order_by_atom_ordering($dbh, $bin, \@rsabs);
    } elsif ($orderer eq "CONCEPT_ID") {
      $self->order_by_concept_id($dbh, $bin);
    } elsif ($orderer eq "ALPHA") {
#      die "ERROR: generator must be SAB for ALPHA ordering" if ($generatortype ne "SAB");
      my(@vsabs);
      if ($generatorcomponent) {
	@vsabs = MIDUtils->makeVersionedSAB($dbh, [ split /[,\s]+/, $generatorcomponent ]);
	$self->order_by_alpha($dbh, $bin, \@vsabs);
      } else {
	$self->order_by_alpha($dbh, $bin);
      }
    }
    EMSUtils->ems_log($main::emslogfile, "Done ordering contents for $bin");
    EMSUtils->plain_log($main::partitionlogfile, "Finished ordering contents for $bin");

    my($counts);

    EMSUtils->plain_log($main::partitionlogfile, "Assigning chem/nonchem for $bin");
    $self->assignChemical($dbh, {table=>$EMSNames::MEBINSTABLE, clause=>'bin_name=' . $dbh->quote($bin)}) if $editable eq "Y";

    EMSUtils->ems_log($main::emslogfile, "Getting counts for $bin");
    EMSUtils->plain_log($main::partitionlogfile, "Starting to get counts for $bin");
    if ($bin eq "demotions") {
      my($tmptable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_COUNTDEM");
      $self->clusterizeDemotions($dbh, $tmptable);
      $dbh->createIndex($tmptable, 'concept_id', "x_" . $tmptable);
      $counts = $self->getDemotionsCounts($dbh, $tmptable);
      $dbh->dropTable($tmptable);
    } else {
      $counts = $self->getBinCounts($dbh, $binconfig, \%bininfo, $DEFAULTCHEMALGO);
    }
    EMSUtils->plain_log($main::partitionlogfile, "Finished getting counts for $bin");
    EMSUtils->ems_log($main::emslogfile, "Finished getting counts for $bin");

    foreach (keys %$counts) {
      $bininfo{$_} = $counts->{$_};
    }

    $bininfo{generation_time} = time - $bininfo{generation_time};
    EMSUtils->plain_log($main::partitionlogfile, "Updating $EMSNames::BININFOTABLE for $bin");
    $self->updateBininfo($dbh, \%bininfo);
  }

# done partitioning
  $endsecs = time;
  $timetorun = $endsecs-$startsecs;
  EMSMaxtab->remove($dbh, $EMSNames::MEPARTITIONINGLOCKKEY);
  EMSMaxtab->set($dbh, $EMSNames::MEPARTITIONTIMEKEY, {valueint=>$timetorun});

  EMSUtils->ems_log($main::emslogfile, "COMPLETED ME_BINS partitioning");
  EMSUtils->plain_log($main::partitionlogfile, "Done partitioning");
  return;
}

# gets counts for bins (other than demotions)
sub getBinCounts {
  my($class, $dbh, $binconfig, $bininfo, $chemalgo) = @_;
  my($qb) = $dbh->quote($bininfo->{bin_name});
  my($bin_type) = $bininfo->{bin_type};
  my($sql);
  my(%bincounts);

  my($BINSTABLE) = ($bin_type eq "ME" ? $EMSNames::MEBINSTABLE :
		    $bin_type eq "QA" ? $EMSNames::QABINSTABLE :
		    $bin_type eq "AH" ? $EMSNames::AHBINSTABLE : "");

  $sql = "select count(distinct concept_id) from $BINSTABLE where bin_name=$qb";
  $bincounts{totalConcepts} = $dbh->selectFirstAsScalar($sql) + 0;
  $sql = "select count(distinct cluster_id) from $BINSTABLE where bin_name=$qb";
  $bincounts{totalClusters} = $dbh->selectFirstAsScalar($sql) + 0;

  foreach (qw(totalUneditableClusters chemConcepts chemClusters chemUneditableClusters nonchemConcepts nonchemClusters nonchemUneditableClusters)) {
    $bincounts{$_} = 0;
  }

  return \%bincounts if ($bininfo->{bin_type} eq "ME" && $binconfig->{editable} ne 'Y');

  $bincounts{totalUneditableClusters} = EMSUtils->uneditableCount($dbh, $BINSTABLE, "a.bin_name=$qb");
  if ($binconfig->{content_type} eq "CHEM") {
    $bincounts{chemConcepts} = $bincounts{totalConcepts};
    $bincounts{chemClusters} = $bincounts{totalClusters};
    $bincounts{chemUneditableClusters} = $bincounts{totalUneditableClusters};
    $bincounts{nonchemUneditableClusters} = 0;

  } elsif ($binconfig->{content_type} eq "NONCHEM") {
    $bincounts{nonchemConcepts} = $bincounts{totalConcepts};
    $bincounts{nonchemClusters} = $bincounts{totalClusters};
    $bincounts{nonchemUneditableClusters} = $bincounts{totalUneditableClusters};
    $bincounts{chemUneditableClusters} = 0;

  } else { # MIXED
    $chemalgo = $DEFAULTCHEMALGO unless $chemalgo;

    $sql = "select count(distinct concept_id) from $BINSTABLE where bin_name=$qb and ischemical='Y'";
    $bincounts{chemConcepts} = $dbh->selectFirstAsScalar($sql) + 0;
    $bincounts{nonchemConcepts} = $bincounts{totalConcepts} - $bincounts{chemConcepts};

    if (lc($chemalgo) eq "one") {
      $sql = "select count(distinct cluster_id) from $BINSTABLE where bin_name=$qb and ischemical='Y'";
      $bincounts{chemClusters} = $dbh->selectFirstAsScalar($sql) + 0;
      $bincounts{nonchemClusters} = $bincounts{totalClusters} - $bincounts{chemClusters};

    } else {
      $sql = <<"EOD";
select count(distinct cluster_id) from $BINSTABLE
where  bin_name=$qb
and    ischemical != 'Y'
EOD
      $bincounts{nonchemClusters} = $dbh->selectFirstAsScalar($sql) || 0;
      $bincounts{chemClusters} = $bincounts{totalClusters}-$bincounts{nonchemClusters};
    }

    $bincounts{chemUneditableClusters} = EMSUtils->uneditableCount($dbh, $BINSTABLE, "a.bin_name=$qb and a.ischemical='Y'");
    $bincounts{nonchemUneditableClusters} = $bincounts{totalUneditableClusters}-$bincounts{chemUneditableClusters};
  }
  return \%bincounts;
}

# returns the count of uneditable clusters for a non-demotions bin
# $type is either empty or one of 'chem', 'nonchem'
sub uneditableCount {
  my($class, $dbh, $concepttable, $clause) = @_;
  my($sql);
  my($c) = 0;

  $clause = "and $clause" if $clause;

# cluster is uneditable if concept is an embryo
  $sql = <<"EOD";
select count(distinct a.cluster_id) from $concepttable a, concept_status b
where  a.concept_id=b.concept_id
and    b.status='E'
$clause
EOD
  $c += $dbh->selectFirstAsScalar($sql);

# concept no longer exists
  $sql = <<"EOD";
select count(distinct a.cluster_id) from $concepttable a
where  a.concept_id not in (select concept_id from concept_status)
$clause
EOD
  $sql = <<"EOD";
select count(distinct cluster_id) from $concepttable
where  concept_id in (
  select concept_id from $concepttable a where 1=1 $clause
  minus
  select concept_id from concept_status
)
EOD
  $c += $dbh->selectFirstAsScalar($sql);

# concept is currently on a worklist being edited
  $sql = <<"EOD";
select count(distinct a.cluster_id) from $concepttable a
where  a.concept_id in (select concept_id from $EMSNames::BEINGEDITEDTABLE)
$clause
EOD
  $c += $dbh->selectFirstAsScalar($sql);
  return $c;
}

# gets counts for demotions bins
# The clustering of demoted concepts is tricky and some concepts may be
# involved in multiple demotions.
sub getDemotionsCounts {
  my($class, $dbh, $table) = @_;
  my($chemconceptstable) = $EMSNames::CHEMCONCEPTSTABLE;
  my($sql);
  my($bincounts);

  $chemalgo = uc($main::EMSCONFIG{DEMOTIONS_CHEMALGO}) || uc($DEFAULTCHEMALGO);

  $sql = "select count(distinct concept_id) from $table";
  $bincounts{totalConcepts} = $dbh->selectFirstAsScalar($sql) || 0;
  $sql = "select count(distinct cluster_id) from $table";
  $bincounts{totalClusters} = $dbh->selectFirstAsScalar($sql) || 0;
  $bincounts{totalUneditableClusters} = EMSUtils->uneditableCount($dbh, $table);

# demotions are always mixed chem and nonchem concepts
  $sql = "select count(distinct concept_id) from $table where concept_id in (select concept_id from $chemconceptstable)";
  $bincounts{chemConcepts} = $dbh->selectFirstAsScalar($sql) || 0;
  $bincounts{nonchemConcepts} = $bincounts{totalConcepts}-$bincounts{chemConcepts};

  if ($chemalgo eq "ONE") {
    $sql = "select count(distinct cluster_id) from $table where concept_id in (select concept_id from $chemconceptstable)";
    $bincounts{chemClusters} = $dbh->selectFirstAsScalar($sql) || 0;
    $bincounts{nonchemClusters} = $bincounts{totalClusters}-$bincounts{chemClusters};
  } else {
    $sql = "select count(distinct cluster_id) from $table where concept_id not in (select concept_id from $chemconceptstable)";
    $bincounts{nonchemClusters} = $dbh->selectFirstAsScalar($sql) || 0;
    $bincounts{chemClusters} = $bincounts{totalClusters}-$bincounts{nonchemClusters};
  }
  $bincounts{chemUneditableClusters} = EMSUtils->uneditableCount($dbh, $table, "a.ischemical='Y'");
  $bincounts{nonchemUneditableClusters} = $bincounts{totalUneditableClusters}-$bincounts{chemUneditableClusters};

  return \%bincounts;
}

# Loads data from ME.config to the table
sub meconfig2table {
  my($class, $dbh, $MECONFIGFILE, $MECONFIGTABLE) = @_;
  my(@x, $rank);
  my($key) = $EMSNames::MECONFIGLOADTIMEKEY;
  my($r);
  my (@errors);
  my($line, $linenum);
  my($s);

# create config table if needed
  unless ($dbh->tableExists($MECONFIGTABLE)) {
    EMSMaxtab->remove($dbh, $EMSNames::MECONFIGLOADTIMEKEY);
  }

# if nothing is changed in the file since last load, return
  $whenloaded = EMSMaxtab->get($dbh, $EMSNames::MECONFIGLOADTIMEKEY);
  return if $whenloaded && GeneralUtils->modtime($MECONFIGFILE) <= $whenloaded->{valueint};
  EMSMaxtab->set($dbh, $EMSNames::MECONFIGLOADTIMEKEY, {valueint=>time, valuechar=>'',timestamp=>''});

  $dbh->dropTable($MECONFIGTABLE);
  EMSTables->createTable($dbh, $MECONFIGTABLE);

  $class->addPredefinedMEbins($dbh);

# read the ME.config file
  open(MECONFIG, $MECONFIGFILE) || die "Cannot open $MECONFIGFILE: $!\n";
  while (<MECONFIG>) {
    $linenum++;
    chomp;
    $line = $_;
    last if /^\#\s+HISTORICAL/;
    next if /^\#/ || /^\s*$/;
    @x = split /\|/, $_;

    $rank = $class->nextMERank($dbh);

    $row{bin_name} = lc($dbh->quote($x[0]));
    $s = EMSTables->colsize($MECONFIGTABLE, "bin_name");
    if (length($x[0]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: bin_name column should be $s characters or fewer";
    }
    if ($x[0] !~ /^[a-z][\w\d]*$/i) {
      push @errors, "$line\nERROR: line: $linenum: bin_name column should only contain alphanumeric characters";
    }

    $row{editable} = $dbh->quote("Y");
    $row{predefined} = $dbh->quote("N");
    $row{rank} = $rank;
    $row{description} = $dbh->quote($x[1]);
    $s = EMSTables->colsize($MECONFIGTABLE, "description");
    if (length($x[1]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: description column should be $s characters or fewer";
    }

    $row{generator} = $dbh->quote($x[2]);
    $s = EMSTables->colsize($MECONFIGTABLE, "generator");
    if (length($x[2]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: generator column should be $s characters or fewer";
    }

    $row{orderer} = $dbh->quote($x[3]);
    $s = EMSTables->colsize($MECONFIGTABLE, "orderer");
    if (length($x[3]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: orderer column should be $s characters or fewer";
    }

    $row{chemalgo} = $dbh->quote($DEFAULTCHEMALGO);

    if (uc($x[4]) eq "CHEM" || uc($x[4] eq "C")) {
      $row{content_type} = $dbh->quote("CHEM");
    } elsif (uc($x[4]) eq "NONCHEM" || uc($x[4] eq "N")) {
      $row{content_type} = $dbh->quote("NONCHEM");
    } elsif (uc($x[4]) eq "MIXED" || uc($x[4]) eq "M") {
      $row{content_type} = $dbh->quote("MIXED");
    } else {
      push @errors, "$line\nERROR: line: $linenum: content_type column should be N, C or M";
    }
    $dbh->insertRow($MECONFIGTABLE, \%row) unless @errors;
  }
  close(MECONFIG);

  if (@errors) {
    EMSMaxtab->remove($dbh, $EMSNames::MECONFIGLOADTIMEKEY);
    die join("\n", @errors) . "\n" if @errors;
  }
}

# Loads data from QA config to the table
sub qaconfig2table {
  my($class, $dbh, $QACONFIGFILE, $QACONFIGTABLE, $reload) = @_;
  my(@x, $rank);
  my(%row);
  my($r);
  my (@errors);
  my($line, $linenum);
  my($s);

# create config table if needed
  EMSMaxtab->remove($dbh, $EMSNames::QACONFIGLOADTIMEKEY) if !($dbh->tableExists($QACONFIGTABLE)) || $reload;

# if nothing is changed in the file since last load, return
  $r = EMSMaxtab->get($dbh, $EMSNames::QACONFIGLOADTIMEKEY);
  return if $r && GeneralUtils->modtime($QACONFIGFILE) <= $r->{valueint};
  EMSMaxtab->set($dbh, $EMSNames::QACONFIGLOADTIMEKEY, {valueint=>time, valuechar=>'',timestamp=>''});

  $dbh->dropTable($QACONFIGTABLE);
  EMSTables->createTable($dbh, $QACONFIGTABLE);

# read the config file
  open(QACONFIG, $QACONFIGFILE) || die "Cannot open $QACONFIGFILE $!\n";
  while (<QACONFIG>) {
    $linenum++;
    chomp;
    $line = $_;
    next if /^\#/ || /^\s*$/;
    @x = split /\|/, $_;

    $row{bin_name} = lc($dbh->quote($x[0]));
    $s = EMSTables->colsize($QACONFIGTABLE, "bin_name");
    if (length($x[0]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: bin_name column should be $s characters or fewer";
    }

    $row{rank} = $class->nextQARank($dbh);
    $row{description} = $dbh->quote($x[1]);
    $s = EMSTables->colsize($QACONFIGTABLE, "description");
    if (length($x[1]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: description column should be $s characters or fewer";
    }

    $row{generator} = $dbh->quote($x[2]);
    $s = EMSTables->colsize($QACONFIGTABLE, "generator");
    if (length($x[2]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: generator column should be $s characters or fewer";
    }

    if (uc($x[3]) eq "CHEM" || uc($x[3]) eq "C") {
      $row{content_type} = $dbh->quote("CHEM");
    } elsif (uc($x[3]) eq "NONCHEM" || uc($x[3]) eq "N") {
      $row{content_type} = $dbh->quote("NONCHEM");
    } elsif (uc($x[3]) eq "MIXED" || uc($x[3]) eq "M") {
      $row{content_type} = $dbh->quote("MIXED");
    } else {
      push @errors, "$line\nERROR: line: $linenum: content_type column should be N, C or M";
    }

    if (uc($x[4]) eq "ONE" || uc($x[4]) eq "O") {
      $row{chemalgo} = $dbh->quote("ONE");
    } elsif (uc($x[4]) eq "ALL" || uc($x[4]) eq "A") {
      $row{chemalgo} = $dbh->quote("ALL");
    } elsif ($x[4] =~ /./) {
      push @errors, "$line\nERROR: line: $linenum: chemalgo column should be O or A";
    } else {
      $row{chemalgo} = uc($dbh->quote($DEFAULTCHEMALGO));
    }
    $dbh->insertRow($QACONFIGTABLE, \%row) unless @errors;
  }
  close(QACONFIG);

  if (@errors) {
    EMSMaxtab->remove($dbh, $EMSNames::QACONFIGLOADTIMEKEY);
    die join("\n", @errors) . "\n" if @errors;
  }
}

# Loads data from AH config to the table
sub ahconfig2table {
  my($class, $dbh, $AHCONFIGFILE, $AHCONFIGTABLE, $reload) = @_;
  my(@x, $rank);
  my(%row);
  my($modtime);
  my($r);
  my (@errors);
  my($line, $linenum);
  my($s);

# create config table if needed
  EMSMaxtab->remove($dbh, $EMSNames::AHCONFIGLOADTIMEKEY) if !($dbh->tableExists($AHCONFIGTABLE)) || $reload;

# if nothing is changed in the file return, else reload
  $r = EMSMaxtab->get($dbh, $EMSNames::AHCONFIGLOADTIMEKEY);
  return if $r && GeneralUtils->modtime($AHCONFIGFILE) <= $r->{valueint};
  EMSMaxtab->set($dbh, $EMSNames::AHCONFIGLOADTIMEKEY, {valueint=>time, valuechar=>'',timestamp=>''});

  $dbh->dropTable($AHCONFIGTABLE);
  EMSTables->createTable($dbh, $AHCONFIGTABLE);

# read the config file
  open(AHCONFIG, $AHCONFIGFILE) || die "Cannot open $AHCONFIGFILE $!\n";
  while (<AHCONFIG>) {
    $linenum++;
    chomp;
    $line = $_;
    next if /^\#/ || /^\s*$/;
    @x = split /\|/, $_;

    $row{bin_name} = lc($dbh->quote($x[0]));
    $s = EMSTables->colsize($AHCONFIGTABLE, "bin_name");
    if (length($x[0]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: bin_name column should be $s characters or fewer";
    }

    $row{description} = $dbh->quote($x[1]);
    $s = EMSTables->colsize($AHCONFIGTABLE, "description");
    if (length($x[1]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: description column should be $s characters or fewer";
    }

    $row{generator} = $dbh->quote($x[2]);
    $s = EMSTables->colsize($AHCONFIGTABLE, "generator");
    if (length($x[2]) > $s) {
      push @errors, "$line\nERROR: line: $linenum: generator column should be $s characters or fewer";
    }

    $row{rank} = ++$rank;

    if (uc($x[3]) eq "CHEM" || uc($x[3]) eq "C") {
      $row{content_type} = $dbh->quote("CHEM");
    } elsif (uc($x[3]) eq "NONCHEM" || uc($x[3]) eq "N") {
      $row{content_type} = $dbh->quote("NONCHEM");
    } elsif (uc($x[3]) eq "MIXED" || uc($x[3]) eq "M") {
      $row{content_type} = $dbh->quote("MIXED");
    } else {
      push @errors, "$line\nERROR: line: $linenum: content_type column should be N, C or M";
    }

    if (uc($x[4]) eq "ONE") {
      $row{chemalgo} = $dbh->quote("ONE");
    } elsif (uc($x[4]) eq "ALL") {
      $row{chemalgo} = $dbh->quote("ALL");
    } elsif ($x[4] =~ /./) {
      push @errors, "$line\nERROR: line: $linenum: chemalgo column should be O or A";
    } else {
      $row{chemalgo} = uc($dbh->quote($DEFAULTCHEMALGO));
    }

    if (uc($x[5]) eq "Y" || uc($x[5]) eq "YES") {
      $row{track_history} = $dbh->quote("Y");
    } elsif (uc($x[5]) eq "N" || uc($x[5]) eq "NO") {
      $row{track_history} = $dbh->quote("N");
    } elsif ($x[5] =~ /./) {
      push @errors, "$line\nERROR: line: $linenum: track_history column should be Y or N";
    } else {
      $row{track_history} = $dbh->quote("Y");
    }
    $dbh->insertRow($AHCONFIGTABLE, \%row) unless @errors;
  }
  close(AHCONFIG);

  if (@errors) {
    EMSMaxtab->remove($dbh, $EMSNames::AHCONFIGLOADTIMEKEY);
    die join("\n", @errors) . "\n" if @errors;
  }
}

# next rank for an ME bin
sub nextMERank {
  my($class, $dbh) = @_;
  my($MECONFIGTABLE) = $EMSNames::MECONFIGTABLE;
  my($sql) = "select max(rank) from $MECONFIGTABLE where predefined='Y' and bin_name != 'leftovers'";
  my($rank) = $dbh->selectFirstAsScalar($sql);
  return $rank+1;
}

# returns the next rank for a QA bin
sub nextQARank {
  my($class, $dbh) = @_;
  my($CONFIGTABLE) = $EMSNames::QACONFIGTABLE;
  my($sql);
  my($rank);

  $sql = "select max(rank) from $CONFIGTABLE";
  $rank = $dbh->selectFirstAsScalar($sql) || 0;
  return $rank + 1;
}

# returns the name of the config file for this DB
sub configfile {
  my($class, $dbh, $bin_type) = @_;
  my($dir) = join("/", $ENV{EMS_HOME}, "etc");
  my($db) = $dbh->getDB;

  return "$dir/" . $bin_type . ".config.$db" if -e "$dir/" . $bin_type . ".config.$db";
  return "$dir/" . $bin_type . ".config";
}

sub foo {
  my($self, $dbh, $sql) = @_;
  $sql = "select concept_id from a_non_existent_table";
  $dbh->executeStmt($sql);
}

#------------------------------------------------------------
# Predefined ME bins section

# these concepts are used for testing and are not part of the Metathesaurus
sub generate_testconcepts {
  my($self, $dbh, $bin) = @_;
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($b) = $dbh->quote($bin);
  my($sql);

  EMSUtils->ems_log($main::emslogfile, "Starting to generate contents for $bin");
  $sql = <<"EOD";
update $MEBINSTABLE m
set bin_name=$b, ischemical='N'
where bin_name = 'leftovers'
and   concept_id in (select concept_id from concept_status where concept_id>=100 and concept_id<=1000)
EOD
  $dbh->executeStmt($sql);
  EMSUtils->ems_log($main::emslogfile, "Done generating contents for $bin");
}

# The generator merely collates the concepts that participate in demotions
sub generate_demotions {
  my($self, $dbh, $bin) = @_;
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($b) = $dbh->quote($bin);
  my($sql);

  $sql = <<"EOD";
update $MEBINSTABLE
set bin_name='demotions', ischemical='N'
where bin_name='leftovers'
and   concept_id in (
  select concept_id_1 as concept_id from relationships where status='D'
  union
  select concept_id_2 as concept_id from relationships where status='D'
)
EOD
  $dbh->executeStmt($sql);
}

# embryos
sub generate_embryos {
  my($self, $dbh, $bin) = @_;
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($b) = $dbh->quote($bin);
  my($sql);

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE m
set bin_name=$b, ischemical='N'
where bin_name = 'leftovers'
and   concept_id in (select concept_id from concept_status where status='E')
EOD
  $dbh->executeStmt($sql);
}

# these concepts are used for testing and are not part of the Metathesaurus
sub generate_norelease {
  my($self, $dbh, $bin) = @_;
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($b) = $dbh->quote($bin);
  my($sql);

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE m
set bin_name=$b, ischemical='N'
where bin_name = 'leftovers'
and   concept_id in (select concept_id from concept_status where tobereleased in ('n','N'))
EOD
  $dbh->executeStmt($sql);
}

# noreview
sub generate_noreview {
  my($self, $dbh, $bin) = @_;
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($b) = $dbh->quote($bin);
  my($sql);

  $sql = <<"EOD";
select /*+ PARALLEL */ concept_id from classes where tobereleased in ('y','Y') and status='U'
minus
select /*+ PARALLEL */ concept_id from classes where tobereleased in ('y','Y') and status!='U'
EOD

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE a
set   bin_name = $b, ischemical='N'
where bin_name = 'leftovers'
and   concept_id in (
  select concept_id from classes where tobereleased in ('y','Y') and status='U'
  minus
  select concept_id from classes where tobereleased in ('y','Y') and status!='U'
)
EOD
  $dbh->executeStmt($sql);
}

# reviewed
sub generate_reviewed {
  my($self, $dbh, $bin) = @_;
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($b) = $dbh->quote($bin);
  my($sql);

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE a
set bin_name=$b, ischemical='N'
where bin_name = 'leftovers'
and   concept_id in (select concept_id from concept_status where status='R')
EOD
  $dbh->executeStmt($sql);
}

# generate concepts with atoms from one or more SABs
# SABs can be versioned or versionless
sub generate_by_sab {
  my($self, $dbh, $bin, $sabref) = @_;
  my($sql, $clause);
  my($b) = $dbh->quote($bin);
  my(@vsabs, @rsabs, $vsab, $rsab, $sab);
  my($ta) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_GENSAB_A");
  my($tb) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_GENSAB_B");
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;

  @vsabs = MIDUtils->makeVersionedSAB($dbh, $sabref);
  @rsabs = MIDUtils->makeVersionlessSAB($dbh, $sabref);

  if (@$sabref == 1) {
    $clause = "source=" . $dbh->quote($vsabs[0]); # faster than IN?
  } else {
    $clause = "source in (" . join(",", map { $dbh->quote($_); } @vsabs) . ")";
  }

  $dbh->dropTable([$ta, $tb]);

  $sql = <<"EOD";
create table $ta as
select distinct concept_id from classes where $clause
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $tb as
select concept_id, rownum as cluster_id from $ta
EOD
  $dbh->executeStmt($sql);

  if ($dbh->selectFirstAsScalar("select count(*) as c from $tb") > 5000) {
    $dbh->createIndex($tb, "concept_id", "x_" . $tb);
  }

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE m
  set bin_name=$b, ischemical='N',
      cluster_id=(select cluster_id from $tb where concept_id=m.concept_id)
where bin_name='leftovers'
and   concept_id in (select concept_id from $tb)
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTable([$ta, $tb]);
}

# generate concepts as the output of a script
sub generate_by_script {
  my($self, $dbh, $bin, $pathandargs) = @_;
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($b) = $dbh->quote($bin);
  my($tmpfile) = $self->tempFile;
  my($tmptable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_GENSCRIPT");
  my($cmd);
  my($sql);

  $dbh->dropTable($tmptable);

# append the -d DB argument
  $cmd = $pathandargs . " -d " . $dbh->getDB();
  eval { system "$cmd > $tmpfile" };
  if ($@) {
    die "ERROR in running script: $cmd: $@" if $@;
  }
  my($clusterfile) = $self->clusterizeFile($tmpfile);
  my($colspec) = ['concept_id', 'cluster_id'];

  $dbh->createTable($tmptable, $colspec);
  $dbh->file2table($clusterfile, $tmptable, $colspec);

  unlink $clusterfile;
  unlink $tmpfile;

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE m
  set bin_name=$b, ischemical='N',
      cluster_id=(select cluster_id from $tmptable where concept_id=m.concept_id)
where bin_name='leftovers'
and   concept_id in (select concept_id from $tmptable)
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTable($tmptable);
}

# generate concepts from range of order_id (e.g., MSH:234:5433)
sub generate_by_order_id {
  my($self, $dbh, $bin, $arg) = @_;
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($b) = $dbh->quote($bin);
  my($tmptable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_GENSCRIPT");
  my($sql);

  my($rsab, $order_id_from, $order_id_to) = split /\:/, $arg, 3;
  $rsab = MIDUtils->makeVersionlessSAB($dbh, $rsab);

  unless ($rsab) {
    die "ERROR: Unknown RSAB in $arg";
  }
  unless ($order_id_from =~ /^\d+$/ && $order_id_to =~ /^\d+$/) {
    die "ERROR: The order_id ranges are not numeric: $range";
  }
  unless ($order_id_from <= $order_id_to) {
    die "ERROR: Incorrect order_id range: $range";
  }

  my($qs) = $dbh->quote($rsab);
  $dbh->dropTable($tmptable);
  $sql = <<"EOD";
create table $tmptable as
select concept_id, rownum as cluster_id from (
  select distinct a.concept_id from classes a, atom_ordering b
  where  a.atom_id=b.atom_id
  and    b.root_source=$qs
  and    b.order_id>=$order_id_from
  and    b.order_id<=$order_id_to
)
EOD

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE m
  set bin_name=$b, ischemical='N',
      cluster_id=(select cluster_id from $tmptable where concept_id=m.concept_id)
where bin_name='leftovers'
and   concept_id in (select concept_id from $tmptable)
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTable($tmptable);
}

# assigns the ordering for a bin (uses the cluster_id as an order_id, while preserving the clustering)
sub order_by_concept_id {
  my($self, $dbh, $bin) = @_;
  my($sql);
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($tmptable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_ORDER");
  my($qb) = $dbh->quote($bin);

  $dbh->dropTable($tmptable);
  $sql = <<"EOD";
create table $tmptable as
select concept_id, rownum as order_id from $MEBINSTABLE
where  bin_name=$qb
order  by concept_id
EOD
  $dbh->executeStmt($sql);
  $dbh->createIndex($tmptable, "concept_id", "x_" . $tmptable);

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE a
set cluster_id=(select order_id from $tmptable where concept_id=a.concept_id)
where bin_name=$qb
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTable($tmptable);
}

# orders a bin using the atom ordering for specific source or sources
sub order_by_atom_ordering {
  my($self, $dbh, $bin, $rsabs) = @_;
  my($sql);
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($tmptable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_ORDER");
  my($qb) = $dbh->quote($bin);
  my($clause);

  if (@$rsabs == 1) {
    $clause = "b.root_source=" . $dbh->quote($rsabs->[0]); # faster than IN?
  } else {
    $clause = "b.root_source in (" . join(",", map { $dbh->quote($_); } @$rsabs) . ")";
  }

  $dbh->dropTable($tmptable);
  $sql = <<"EOD";
create table $tmptable as
select concept_id, atom_id, rownum as order_id from
(
 select a.concept_id, a.atom_id, b.order_id from classes a, atom_ordering b
 where  a.concept_id in (select concept_id from $MEBINSTABLE where bin_name=$qb)
 and    a.atom_id=b.atom_id
 and    $clause
 order by order_id
)
EOD
  $dbh->executeStmt($sql);
  $dbh->createIndex($tmptable, "concept_id", "x_" . $tmptable);

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE a
set cluster_id=
(
 select max(order_id) from $tmptable
 where concept_id=a.concept_id
 group by concept_id
)
where bin_name=$qb
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTable($tmptable);
}

# orders a bin using the alphabetic ordering of atoms in the concept
sub order_by_alpha {
  my($self, $dbh, $bin, $vsabs) = @_;
  my($sql);
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($tmptable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_ORDER");
  my($qb) = $dbh->quote($bin);
  my($clause);

  if ($vsabs && ref($vsabs)) {
    if (@$vsabs == 1) {
      $clause = "a.source=" . $dbh->quote($vsabs->[0]); # faster than IN?
    } else {
      $clause = "a.source in (" . join(",", map { $dbh->quote($_); } @$vsabs) . ")";
    }
  }
  $clause = "1=1" unless $clause;

  $dbh->dropTable($tmptable);
  $sql = <<"EOD";
create table $tmptable as
select concept_id, atom_id, rownum as order_id from
(
 select a.concept_id, a.atom_id, b.atom_name from classes a, atoms b
 where  a.concept_id in (select concept_id from $MEBINSTABLE where bin_name=$qb)
 and    a.atom_id=b.atom_id
 and    $clause
 order by atom_name
)
EOD
  $dbh->executeStmt($sql);
  $dbh->createIndex($tmptable, "concept_id", "x_" . $tmptable);

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE a
set cluster_id=
(
 select min(order_id) from $tmptable
 where concept_id=a.concept_id
 group by concept_id
)
where bin_name=$qb
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTable($tmptable);
}

# orders by atom code - ties are broken by atom_id
sub order_by_code {
  my($self, $dbh, $bin, $vsabs) = @_;
  my($sql);
  my($MEBINSTABLE) = $EMSNames::MEBINSTABLE;
  my($tmptable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_ORDER");
  my($qb) = $dbh->quote($bin);
  my($clause);

  if (@$vsabs == 1) {
    $clause = "source=" . $dbh->quote($vsabs->[0]); # faster than IN?
  } else {
    $clause = "source in (" . join(",", map { $dbh->quote($_); } @$vsabs) . ")";
  }

  $dbh->dropTable($tmptable);
  $sql = <<"EOD";
create table $tmptable as
select concept_id, atom_id, rownum as order_id from
(
 select concept_id, atom_id, code from classes
 where  concept_id in (select concept_id from $MEBINSTABLE where bin_name=$qb)
 and    $clause
 order by code, atom_id
)
EOD
  $dbh->executeStmt($sql);
  $dbh->createIndex($tmptable, "concept_id", "x_" . $tmptable);

  $sql = <<"EOD";
update /*+ PARALLEL */ $MEBINSTABLE a
set cluster_id=
(
 select max(order_id) from $tmptable
 where concept_id=a.concept_id
 group by concept_id
)
where bin_name=$qb
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTable($tmptable);
}

# Creates clusters out of concepts in the demotions bin
sub clusterizeDemotions {
  my($class, $dbh, $table) = @_;
  my($sql);
  my($t1) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_CLUSDEM1");
  my($t2) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_CLUSDEM2");
  my($t3) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_CLUSDEM3");
  my($t4) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_CLUSDEM4");
  my($t5) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_CLUSDEM5");

  $dbh->dropTable([$table, $t1, $t2, $t3, $t4, $t5]);

  $sql = <<"EOD";
create table $t1 as
select concept_id_1 as concept_id_1, concept_id_2 as concept_id_2 from relationships where status='D'
union
select concept_id_2 as concept_id_1, concept_id_1 as concept_id_2 from relationships where status='D'
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $t2 as
select distinct concept_id_1, concept_id_2 from $t1
where  concept_id_1 < concept_id_2
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $t3 as
select concept_id_1 as concept_id, concept_id_1 as cluster_id from $t2
union
select concept_id_2 as concept_id, concept_id_1 as cluster_id from $t2
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $t4 as
select distinct cluster_id from $t3
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $t5 as
select cluster_id, rownum as r from $t4
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create table $table as
select a.concept_id, b.r as cluster_id, 'N' as ischemical from $t3 a, $t5 b
where  a.cluster_id=b.cluster_id
order  by cluster_id, concept_id
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
update table $table set ischemical='Y'
where  concept_id in (select concept_id from $EMSNames::CHEMCONCEPTSTABLE)
EOD
  $dbh->dropTables([$t1, $t2, $t3, $t4, $t5]);
}

# Draws concepts from a bin into a table.  Caller specifies if only chem,
# nonchem clusters are desired.  Returns the table name.
# also filters out concepts that are not currently present in the MID
# params->  content_type=>{chem/nonchem}, chemalgo=>, bin_name=>, bin_type=>
# bininfo->
sub drawFromBin {
  my($class, $dbh, $param) = @_;
  my($sql, $qb);
  my($BINTABLE) = $EMSNames::MEBINSTABLE;
  my($p) = $EMSNames::TMPTABLEPREFIX;
  my($allconceptstable) = EMSUtils->tempTable($dbh, $p . "_a");
  my($chemtable) = EMSUtils->tempTable($dbh, $p . "_c");
  my($nonchemtable) = EMSUtils->tempTable($dbh, $p . "_n");
  my($outtable) = EMSUtils->tempTable($dbh, $p . "_o");
  my($bin_type) = $param->{bin_type} || $param->{bininfo}->{bin_type};
  
  $qb = $dbh->quote($param->{bin_name});
  $dbh->dropTables([$allconceptstable, $chemtable, $nonchemtable]);

  $BINTABLE = $EMSNames::QABINSTABLE if ($bin_type eq "QA");
  $BINTABLE = $EMSNames::AHBINSTABLE if ($bin_type eq "AH");

  if (!$param->{chemalgo} && $param->{bin_name} && $param->{bin_type}) {
    my($CONFIGTABLE);
    $CONFIGTABLE = ($param->{bin_type} eq "QA" ? $EMSNames::QACONFIGTABLE :
		    $param->{bin_type} eq "AH" ? $EMSNames::AHCONFIGTABLE : $EMSNames::MECONFIGTABLE);

    $binconfig = EMSUtils->getBinconfig($dbh, $CONFIGTABLE, $param->{bin_name});
    $param->{chemalgo} = $binconfig->{chemalgo} if $binconfig;
  }

# demotions need to be reclustered
  if ($param->{bin_name} eq "demotions") {

    my($tmptable) = EMSUtils->tempTable($dbh, $p . "_t");
    $dbh->dropTable($tmptable);
    EMSUtils->clusterizeDemotions($dbh, $tmptable);
    $dbh->createTable($allconceptstable, [{concept_id=>'integer'}, {cluster_id=>'integer'}, {ischemical=>'char(1)'}]);
    $sql = "insert into $allconceptstable select concept_id, cluster_id, 'N' as ischemical from $tmptable";
    $dbh->executeStmt($sql);
    EMSUtils->assignChemical($dbh,  {table=>$allconceptstable});
    $dbh->dropTable($tmptable);

  } else {

    $sql = <<"EOD";
create table $allconceptstable as
select a.concept_id, a.cluster_id, a.ischemical from $BINTABLE a, concept_status b
where  a.bin_name=$qb
and    a.concept_id=b.concept_id
EOD
    $dbh->executeStmt($sql);
#    EMSUtils->assignChemical($dbh,  {table=>$allconceptstable});
  }

  if (lc($param->{content_type}) eq "chem") {
    EMSUtils->splitContents($dbh, $allconceptstable, $chemtable, $nonchemtable, $param->{chemalgo});
    $sql = "create table $outtable as select * from $chemtable";
  } elsif (lc($param->{content_type}) eq "nonchem") {
    EMSUtils->splitContents($dbh, $allconceptstable, $chemtable, $nonchemtable, $param->{chemalgo});
    $sql = "create table $outtable as select * from $nonchemtable";
  } else {
    $sql = "create table $outtable as select * from $allconceptstable";
  }
  $dbh->executeStmt($sql);
  $dbh->dropTables([$allconceptstable, $chemtable, $nonchemtable]);
  return $outtable;
}

# Splits the contents of a concept/cluster table into chemical and nonchemical
# tables
sub splitContents {
  my($class, $dbh, $concepttable, $chemtable, $nonchemtable, $chemalgo) = @_;
  my($sql);

  $chemalgo = "one" unless $chemalgo;

  if (lc($chemalgo) eq "one") {

    $sql = <<"EOD";
create table $chemtable as
select concept_id, cluster_id from $concepttable
where  cluster_id in (select cluster_id from $concepttable where ischemical='Y')
EOD
    $dbh->executeStmt($sql);

    $sql = <<"EOD";
create table $nonchemtable as
select concept_id, cluster_id from $concepttable
where  cluster_id not in (select cluster_id from $chemtable)
EOD
    $dbh->executeStmt($sql);

  } else {

    $sql = <<"EOD";
create table $nonchemtable as
select concept_id, cluster_id from $concepttable
where  cluster_id in (select cluster_id from $concepttable where ischemical='N')
EOD
    $dbh->executeStmt($sql);

    $sql = <<"EOD";
create table $chemtable as
select concept_id, cluster_id from $concepttable
where  cluster_id not in (select cluster_id from $nonchemtable)
EOD
    $dbh->executeStmt($sql);
  }
}

# assigns the ischemical attribute to a table
# param-> table=>, clause=>
sub assignChemical {
  my($class, $dbh, $param) = @_;
  my($sql);
  my($chemconceptstable) = $EMSNames::CHEMCONCEPTSTABLE;
  my($c) = " and " . $param->{clause} if $param->{clause};
  my($table) = $param->{table};

  $sql = <<"EOD";
update /*+ PARALLEL */ $table
set ischemical='Y'
where concept_id in (select concept_id from $chemconceptstable) $c
EOD
  $dbh->executeStmt($sql);
}

# updates data in the BININFO table with information for a bin
sub updateBininfo {
  my($class, $dbh, $newrow) = @_;
  my($bin_name) = $newrow->{bin_name};
  my($bin_type) = $newrow->{bin_type};
  my($oldrow);
  my($sql);
  my($BININFOTABLE) = $EMSNames::BININFOTABLE;
  my($ref);
  my(%m);

  EMSTables->createTable($dbh, $BININFOTABLE);

  return unless $bin_name && $bin_type;
  $sql = "select bin_name from $BININFOTABLE where bin_name=" . $dbh->quote($bin_name);
  $ref = $dbh->selectFirstAsScalar($sql);

  unless ($ref) {
    %m = ();
    $m{bin_name} = $dbh->quote($bin_name);
    $m{bin_type} = $dbh->quote($bin_type);
    $m{generation_date} = "SYSDATE";
    $m{generation_time} = 0;
    $m{generation_user} = $dbh->quote("unknown");
    $m{nextWorklistNum} = 1;
    $m{nextChemWorklistNum} = 1;
    $m{nextNonchemWorklistNum} = 1;
    $m{totalClusters} = 0;
    $m{totalConcepts} = 0;
    $m{totalUneditableClusters} = 0;
    $m{chemClusters} = 0;
    $m{chemConcepts} = 0;
    $m{chemUneditableClusters} = 0;
    $m{nonchemClusters} = 0;
    $m{nonchemConcepts} = 0;
    $m{nonchemUneditableClusters} = 0;
    $dbh->insertRow($BININFOTABLE, \%m);
  }

  %m = ();
  foreach (keys %$newrow) {
    if (EMSTables->coltype($BININFOTABLE, $_) =~ /char/i) {
      $m{uc($_)} = $dbh->quote($newrow->{$_});
    } else {
      $m{uc($_)} = $newrow->{$_};
    }
  }
  $dbh->updateRow($BININFOTABLE, "bin_name", $m{BIN_NAME}, \%m);
}

# QA/AH BINS Utilities

# generates the contents of QA and AH bins
sub bin_generate {
  my($self, $dbh, $bins, $bin_type) = @_;
  my($bin_name);
  my($sql);
  my($BINSTABLE);
  my($CONFIGTABLE);
  my(%bininfo, $binconfig);
  my($tmptable);
  my($qb);
  my($chemalgo);
  my(@bins);

  if (ref($bins) eq "ARRAY") {
    foreach (@$bins) {
      push @bins, $_;
    }
  } elsif (ref($bins) eq "HASH") {
    foreach (keys %$bins) {
      push @bins, $_;
    }
  } else {
    push @bins, $bins;
  }

  $tmptable = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_" . $bin_type);

  foreach $bin_name (@bins) {

    if ($bin_type eq "QA") {
      $BINSTABLE = $EMSNames::QABINSTABLE;
      $CONFIGTABLE = $EMSNames::QACONFIGTABLE;
    } else {
      $BINSTABLE = $EMSNames::AHBINSTABLE;
      $CONFIGTABLE = $EMSNames::AHCONFIGTABLE;
    }

    $qb = $dbh->quote($bin_name);
    $binconfig = EMSUtils->getBinconfig($dbh, $CONFIGTABLE, $bin_name);
    EMSTables->createTable($dbh, $BINSTABLE);

    $chemalgo = $binconfig->{chemalgo} || $DEFAULTCHEMALGO;
    $generator = $binconfig->{generator};
    next unless $generator;

    %bininfo = ();
    $bininfo{bin_name} = $bin_name;
    $bininfo{bin_type} = $bin_type;
    $bininfo{generation_date} = "SYSDATE";
    $bininfo{generation_time} = time;
    $bininfo{generation_user} = $ENV{REMOTE_USER} || GeneralUtils->username() || "unknown";

    $generator =~ /^([^:]+):?(.*)$/;
    $generatortype = uc($1);
    $generatorcomponent = $2;

    EMSUtils->ems_log($main::emslogfile, "Generating contents for $bin_name using the generator: $generator");

    if ($generatortype eq "SCRIPT") {
      my($tmpfile) = $self->tempFile;
      my($opts) = "-d " . $dbh->getDB;
      my($cmd) = join("/", $ENV{EMS_HOME}, "scripts", $generatorcomponent) . " $opts > $tmpfile";
      system $cmd;
      my($clusterfile) = $self->clusterizeFile($tmpfile);
      $dbh->createTable($tmptable, ['concept_id', 'cluster_id']);
      $dbh->file2table($clusterfile, $tmptable, ['concept_id', 'cluster_id']);
      unlink $clusterfile;
      unlink $tmpfile;

    } elsif ($generatortype =~ /^MEME_/) {
      my($sth);
      my($cluster_table);
      my($memeproc) = join(".", $generatortype, $generatorcomponent);
      $sql = <<"EOD";
BEGIN
    :cluster_table := $memeproc(cluster_flag => MEME_CONSTANTS.CLUSTER_YES);
END;
EOD
      $sth = $dbh->{dbh}->prepare($sql);
      $sth->bind_param_inout(":cluster_table", \$cluster_table, 64);
      $sth->execute;
      $sql = "create table $tmptable as select * from " . join('.', $main::EMSCONFIG{MTHSCHEMA}, $cluster_table);
      $dbh->executeStmt($sql);

      $errmsg = $@ || $DBI::errstr;
      die $errmsg if $errmsg;

      $dbh->dropTable(join('.', $main::EMSCONFIG{MTHSCHEMA}, $cluster_table));

    } elsif ($generatortype eq "SQLFILE") {
      my($f) = join("/", $ENV{EMS_HOME}, "sql", $generatorcomponent);
      die "File: $f does not exist" unless -f $f;
      $sql = "create table $tmptable as " . GeneralUtils->file2str($f, {flatten=>1, skipcomments=>1, skipspaces=>1, removetrailingsemicolon=>1});
      EMSUtils->ems_log($main::emslogfile, "SQL: $sql");
      $dbh->executeStmt($sql);

    } elsif ($generatortype eq "FILE") {
      my($f) = join("/", $ENV{EMS_HOME}, "log", $generatorcomponent);
      die "File: $f does not exist" unless -f $f;
      my($tmpfile) = $self->tempFile;
      copy($f, $tmpfile);
      my($clusterfile) = $self->clusterizeFile($tmpfile);
      $dbh->createTable($tmptable, ['concept_id', 'cluster_id']);
      $dbh->file2table($clusterfile, $tmptable, ['concept_id', 'cluster_id']);
      unlink $clusterfile;
      unlink $tmpfile;

    } elsif ($generatortype eq "TABLE") {
      my($t) = $generatorcomponent;
      $sql = "create table $tmptable as select concept_id, cluster_id from $t";
      $dbh->executeStmt($sql);
    }

    EMSUtils->pivotTable($dbh, $tmptable);
    EMSUtils->clusterizeTable($dbh, $tmptable);
    $sql = "delete from $BINSTABLE where bin_name=$qb";
    $dbh->executeStmt($sql);

    $sql = <<"EOD";
insert into $BINSTABLE
select $qb as bin_name, concept_id, cluster_id, 'N' as ischemical from $tmptable
EOD
    $dbh->executeStmt($sql);
    $dbh->dropTable($tmptable);
    $self->assignChemical($dbh, {table=>$BINSTABLE, clause=>'bin_name=' . $dbh->quote($bin_name)});

    my($counts) = $self->getBinCounts($dbh, $binconfig, \%bininfo, $chemalgo);
    foreach (keys %$counts) {
      $bininfo{$_} = $counts->{$_};
    }
    $bininfo{generation_time} = time-$bininfo{generation_time};
    $self->updateBininfo($dbh, \%bininfo);
  }
}

# returns the names of all bins of a certain type
sub getBinNames {
  my($class, $dbh, $bin_type) = @_;
  my($sql);
  my($CONFIGTABLE);
  my(@bins);
  
  if ($bin_type eq "ME") {
    $CONFIGTABLE = $EMSNames::MECONFIGTABLE;
  } elsif ($bin_type eq "QA") {
    $CONFIGTABLE = $EMSNames::QACONFIGTABLE;
  } elsif ($bin_type eq "AH") {
    $CONFIGTABLE = $EMSNames::AHCONFIGTABLE;
  } else {
    return ();
  }

  $sql = "select bin_name from $CONFIGTABLE";
  @bins = $dbh->selectAllAsArray($sql);
  return @bins;
}

# Makes a checklist from a bin
# param is a hash ref: bin_name=>, limit=>, excludetop=>, randomize=>, excludebeingedited=>
# owner=>, bin_type=>, file=>, table_name=>, content_type=>
sub makeChecklist {
  my($class, $dbh, $checklist, $param) = @_;
  my($bin_name) = $param->{bin_name};
  my($sql);
  my($concepttable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_co");
  my($clustertable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_cl");
  my($qb) = $dbh->quote($bin_name);
  my(%info);
  my($sql0, $sql1, $sql2, $sql3);
  my($BEINGEDITEDTABLE) = $EMSNames::BEINGEDITEDTABLE;

# pick out clusters where all concepts are extant and not currently being edited
  $sql1 = "and    a.concept_id not in (select concept_id from $BEINGEDITEDTABLE)" if ($param->{excludebeingedited});

  if ($param->{file}) {
    my($tmpfile) = EMSUtils->clusterizeFile($param->{file});

    $dbh->dropTable($concepttable);
    $dbh->createTable($concepttable, ['concept_id', 'cluster_id']);
    $dbh->file2table($tmpfile, $concepttable, ['concept_id', 'cluster_id']);
    unlink $tmpfile;

  } elsif ($param->{table_name}) {

    EMSUtils->clusterizeTable($dbh, $param->{table_name});
    my($sql) = "create table $concepttable as select concept_id, cluster_id from " . $param->{table_name};
    $dbh->executeStmt($sql);

  } else {

    $concepttable = EMSUtils->drawFromBin($dbh, $param);

  }

  $dbh->dropTable($checklist);

  if ($param->{randomize}) {

    $sql0 = <<"EOD";
select cluster_id, rownum as row_id from (
  select cluster_id, dbms_random.value as r from (
    select distinct cluster_id from $concepttable a, concept_status b
    where  b.concept_id=a.concept_id $sql1
  ) order by r
)
EOD

    if ($param->{limit}) {
      if ($param->{excludetop}) {
	$sql = "select * from (select * from ($sql0) where row_id>" . $param->{excludetop} . ") where rownum<=" . $param->{limit};
      } else {
	$sql = "select * from ($sql0) where rownum<=" . $param->{limit};
      }
    } else {
      if ($param->{excludetop}) {
	$sql = "select * from ($sql0) where row_id>" . $param->{excludetop};
      } else {
	$sql = $sql0;
      }
    }
    $dbh->executeStmt("create table $clustertable as $sql");

    $sql = <<"EOD";
create table $checklist as
select a.concept_id as orig_concept_id, a.atom_id, c.row_id, c.row_id as cluster_id from classes a, $concepttable b, $clustertable c
where  a.concept_id=b.concept_id
and    b.cluster_id=c.cluster_id
EOD
    $dbh->executeStmt($sql);

  } else {

    $sql0 = <<"EOD";
select cluster_id, rownum as row_id from (
  select distinct cluster_id from $concepttable a, concept_status b
  where  b.concept_id=a.concept_id $sql1
  order by cluster_id
)
EOD

    if ($param->{limit}) {
      if ($param->{excludetop}) {
	$sql = "select * from (select * from ($sql0) where row_id>" . $param->{excludetop} . ") where rownum<=" . $param->{limit};
      } else {
	$sql = "select * from ($sql0) where rownum<=" . $param->{limit};
      }
    } else {
      if ($param->{excludetop}) {
	$sql = "select * from ($sql0) where row_id>" . $param->{excludetop};
      } else {
	$sql = $sql0;
      }
    }
    $dbh->executeStmt("create table $clustertable as $sql");

    $sql = <<"EOD";
create table $checklist as
select a.concept_id as orig_concept_id, a.atom_id, c.row_id, c.cluster_id from classes a, $concepttable b, $clustertable c
where  a.concept_id=b.concept_id
and    b.cluster_id=c.cluster_id
EOD
    $dbh->executeStmt($sql);
  }

  $dbh->createIndex($checklist, 'orig_concept_id', "x1_" . $checklist);
  $dbh->createIndex($checklist, ['atom_id', 'cluster_id'], "x2_" . $checklist);

  $sql = "GRANT ALL ON $checklist TO PUBLIC";
  $dbh->executeStmt($sql);
  $dbh->analyzeStats($checklist);

  $dbh->dropTables([$concepttable, $clustertable]);

  $info{checklist_name} = $dbh->quote(lc($checklist));
  $info{bin_name} = $dbh->quote($bin_name);
  $info{bin_type} = $dbh->quote(($param->{bin_type} || "??"));
  $info{create_date} = "SYSDATE";
  $info{owner} = $dbh->quote(($param->{owner} || "unknown"));
  $info{concepts} = $dbh->selectFirstAsScalar("select count(distinct orig_concept_id) from $checklist") || 0;
  $info{clusters} = $dbh->selectFirstAsScalar("select count(distinct cluster_id) from $checklist") || 0;

  my($checklistinfo) = $EMSNames::CHECKLISTINFOTABLE;
  $sql = "delete from $checklistinfo where checklist_name=" . $info{checklist_name};
  $dbh->executeStmt($sql);
  $dbh->insertRow($checklistinfo, \%info);
}

# Makes a worklist from a bin
# param is a hash ref: bin_name=>, limit=>, content_type=>, created_by=>, bininfo=>, worklistdir=>
# worklist_prefix=>, chemalgo=>, excludetop=>
sub makeWorklist {
  my($class, $dbh, $param) = @_;
  my($bin_name) = $param->{bin_name};
  my($worklist_name);
  my($bininfo);
  my($sql);
  my($concepttable);
  my($clustertable) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_cl");
  my($md5table) = EMSUtils->tempTable($dbh, $EMSNames::TMPTABLEPREFIX . "_md5");
  my($qb) = $dbh->quote($bin_name);
  my($qc) = $qb;
  my(%info);
  my($sql1, $sql2, $sql0);
  my($limit) = $param->{limit} || 300; # clusters per worklist
  my($worklistdir) = $param->{worklistdir} || "/tmp";
  my($AHHISTORY) = $EMSNames::AHHISTORYTABLE;
  my($BININFOTABLE) = $EMSNames::BININFOTABLE;
  my($WORKLISTINFOTABLE) = $EMSNames::WORKLISTINFOTABLE;
  my($BEINGEDITEDTABLE) = $EMSNames::BEINGEDITEDTABLE;
  my($histtable) = $dbh->tempTable($EMSNames::TMPTABLEPREFIX . "_hist");
  my($content_type_wanted) = $param->{content_type_wanted};

  unless (-e $worklistdir) {
    mkpath $worklistdir, 0, 0775 || die "Cannot mkpath $worklistdir";
    chmod 0775, $worklistdir || die "Cannot chmod $worklistdir";
  }
  
  $bininfo = $param->{bininfo} || EMSUtils->getBininfo($dbh, $bin_name);
  $worklist_prefix = $param->{worklist_prefix};

# intuit the worklist name and increment the number
  $worklist_name = $class->nextWorklistName($bininfo, $worklist_prefix, $content_type_wanted);
  $class->incrementWorklistNum($dbh, $bininfo, $content_type_wanted);

  my($worklistfile) = join("/", $worklistdir, $worklist_name . ".dat");
  my($worklistconceptsfile) = join("/", $worklistdir, $worklist_name . ".input");

# pick out clusters where all concepts are extant and not currently being edited
  $sql1 = "and    a.concept_id not in (select concept_id from $BEINGEDITEDTABLE)";
  $sql2 = "where  rownum<=" . $param->{limit} if ($param->{limit});
  $sql2 .= ($sql2 ? " and " : "where ") . "row_id>" . $param->{excludetop} if ($param->{excludetop});

  $concepttable = EMSUtils->drawFromBin($dbh, $param);

# For worklists from AH bins, eliminate clusters that have already been edited
  if ($bininfo->{bin_type} eq "AH") {
    $dbh->dropTable($histtable);
    EMSUtils->list2history($dbh, $concepttable, $histtable);

    $sql = <<"EOD";
select canonical_name from $EMSNames::AHCANONICALNAMETABLE where bin_name=$qb
EOD
    my($canonical_name) = $dbh->selectFirstAsScalar($sql);
    $qc = $dbh->quote($canonical_name) if $canonical_name;

# eliminate clusters that have MD5s identical to ones in the history table
    $sql = <<"EOD";
delete from $concepttable where cluster_id in (
  select distinct a.cluster_id from $histtable a, $AHHISTORY b
  where  a.min_concept_id=b.min_concept_id
  and    b.canonical_name=$qc
  and    a.md5=b.md5
)
EOD
    $dbh->executeStmt($sql);
  }

  $sql0 = <<"EOD";
select cluster_id, rownum as row_id from (
  select distinct cluster_id from $concepttable a, concept_status b
  where  b.concept_id=a.concept_id $sql1
  order by cluster_id
)
EOD

  if ($param->{limit}) {
    if ($param->{excludetop}) {
      $sql = "select * from (select * from ($sql0) where row_id>" . $param->{excludetop} . ") where rownum<=" . $param->{limit};
    } else {
      $sql = "select * from ($sql0) where rownum<=" . $param->{limit};
    }
  } else {
    if ($param->{excludetop}) {
      $sql = "select * from ($sql0) where row_id>" . $param->{excludetop};
    } else {
      $sql = $sql0;
    }
  }
  $dbh->executeStmt("create table $clustertable as $sql");

  my($worklist_table) = $worklist_name;

  $dbh->dropTable($worklist_table);
  $sql = <<"EOD";
create table $worklist_table as
select a.concept_id as orig_concept_id, a.atom_id, c.row_id, c.cluster_id from classes a, $concepttable b, $clustertable c
where  a.concept_id=b.concept_id
and    b.cluster_id=c.cluster_id
EOD
  $dbh->executeStmt($sql);
  $dbh->createIndex($worklist_table, 'orig_concept_id', "x1_" . $worklist_table);
  $dbh->createIndex($worklist_table, 'atom_id', "x2_" . $worklist_table);

  $dbh->dropTables([$concepttable, $clustertable]);

  $sql = "GRANT ALL ON $worklist_table TO PUBLIC";
  $dbh->executeStmt($sql);
  $dbh->analyzeStats($worklist_table);

  my($qw) = $dbh->quote($worklist_name);

# Add to being edited
  $sql = <<"EOD";
delete from $BEINGEDITEDTABLE where worklist_name=$qw
EOD

  $sql = <<"EOD";
insert into $BEINGEDITEDTABLE
select distinct $qb as bin_name, $qw as worklist_name, orig_concept_id as concept_id from $worklist_table
EOD
  $dbh->executeStmt($sql);

# Archive contents of worklist
  unlink $worklistfile if -e $worklistfile;
  $sql = <<"EOD";
select orig_concept_id, atom_id, row_id, cluster_id from $worklist_table order by row_id
EOD
  $dbh->selectToFile($sql, $worklistfile);

  unlink $worklistconceptsfile if -e $worklistconceptsfile;
  $sql = <<"EOD";
select distinct orig_concept_id, cluster_id from $worklist_table order by cluster_id
EOD
  $dbh->selectToFile($sql, $worklistconceptsfile);

# Add to history if AH worklist
  if ($bininfo->{bin_type} eq "AH") {
    $sql = "delete from $AHHISTORY where worklist_name=$qw";
    $dbh->executeStmt($sql);

    $sql = <<"EOD";
insert into $AHHISTORY
  select
    $qb as bin_name,
    $qc as canonical_name,
    $qw as worklist_name,
    cluster_id,
    min_concept_id,
    md5 from $histtable
EOD
    $dbh->executeStmt($sql);
    $dbh->dropTable($histtable);
  }

# insert record in WORKLISTINFOTABLE
  $info{worklist_name} = $qw;
  $info{worklist_description} = $dbh->quote("Worklist from bin: " . $bin_name);
  $info{worklist_status} = $dbh->quote('CR');
  $info{n_concepts} = $dbh->selectFirstAsScalar("select count(distinct orig_concept_id) from $worklist_table" || 0);
  $info{n_clusters} = $dbh->selectFirstAsScalar("select count(distinct cluster_id) from $worklist_table" || 0);
  $info{grp} = "null";
  $info{editor} = "null";

  $info{create_date} = "SYSDATE";
  $info{created_by} = $param->{created_by} ? $dbh->quote($param->{created_by}) : "null";

  $info{assign_date} = "null";
  $info{return_date} = "null";

  $info{edit_time} = -1;
  $info{stamp_date} = "null";
  $info{stamped_by} = "null";
  $info{stamp_time} = -1;
  $info{exclude_flag} = $dbh->quote("Y");

  $info{n_actions} = -1;
  $info{n_approved} = -1;
  $info{n_approved_by_editor} = -1;
  $info{n_stamped} = -1;
  $info{n_not_stamped} = -1;
  $info{n_rels_inserted} = -1;
  $info{n_stys_inserted} = -1;
  $info{n_splits} = -1;
  $info{n_merges} = -1;

  $sql = "delete from $WORKLISTINFOTABLE where worklist_name=$qw";
  $dbh->executeStmt($sql);
  $dbh->insertRow($WORKLISTINFOTABLE, \%info);
}

# returns the name of the next worklist from this bin
sub nextWorklistName {
  my($class, $bininfo, $prefix, $content_type_wanted) = @_;
  my($n) = $class->nextWorklistNum($bininfo, $content_type_wanted);
  my($worklist_name);

  if (uc($content_type_wanted) eq "CHEM") {
    $worklist_name = sprintf("%s_ch_%.2d", $prefix, $n);
  } elsif (uc($content_type_wanted) eq "NONCHEM") {
    $worklist_name = sprintf("%s_nc_%.2d", $prefix, $n);
  } else {
    $worklist_name = sprintf("%s_%.2d", $prefix, $n);
  }
  return $worklist_name;
}

# returns the number of the next worklist to be made from this bin
sub nextWorklistNum {
  my($class, $bininfo, $content_type_wanted) = @_;

  if (uc($content_type_wanted) eq "CHEM") {
    return $bininfo->{nextChemWorklistNum};
  } elsif (uc($content_type_wanted) eq "NONCHEM") {
    return $bininfo->{nextNonchemWorklistNum};
  } else {
    return $bininfo->{nextWorklistNum};
  }
}

# increments the number of the next worklist to be made from this bin
sub incrementWorklistNum {
  my($class, $dbh, $bininfo, $content_type_wanted) = @_;
  my($bin_name) = $bininfo->{bin_name};
  my($qb) = $dbh->quote($bin_name);
  my($BININFOTABLE) = $EMSNames::BININFOTABLE;

  if (uc($content_type_wanted) eq "CHEM") {
    $bininfo->{nextChemWorklistNum}++;
    $dbh->updateRow($BININFOTABLE, 'bin_name', $qb, {nextChemWorklistNum=>$bininfo->{nextChemWorklistNum}});
  } elsif (uc($content_type_wanted) eq "NONCHEM") {
    $bininfo->{nextNonchemWorklistNum}++;
    $dbh->updateRow($BININFOTABLE, 'bin_name', $qb, {nextNonchemWorklistNum=>$bininfo->{nextNonchemWorklistNum}});
  } else {
    $bininfo->{nextWorklistNum}++;
    $dbh->updateRow($BININFOTABLE, 'bin_name', $qb, {nextWorklistNum=>$bininfo->{nextWorklistNum}});
  }
}

# Given a AH bin's canonical name and a worklist,
# Returns in a table the cluster_ids of clusters
# that have been previously edited
sub historyFilter {
  my($class, $dbh, $canonical_name, $worklist, $editedClustersTable) = @_;
  my($md5table);
  my($sql);
  my($tmpfile) = $class->tempFile;
  my($qc) = $dbh->quote($canonical_name);
  my($AHHISTORY) = $EMSNames::AHHISTORYTABLE;
  my($CANONICAL) = $EMSNames::AHCANONICALNAMETABLE;

  $md5table = EMSutils->tempTable($dbh, $EMSNames::PREFIX . "md5");
  $dbh->dropTables([$md5table, $editedClustersTable]);

# create MD5 for the worklist clusters
  $sql = <<"EOD";
select distinct orig_concept_id, cluster_id from $worklist
EOD
  $dbh->selectToFile($sql, $tmpfile);
  open(T, $tmpfile) || die "Cannot open $tmpfile";
  while (<T>) {
    chomp;
    ($concept_id, $cluster_id) = split /\|/, $_, 2;
    push @{ $cluster{$cluster_id} }, $concept_id;
  }
  close(T);
  unlink $tmpfile;
  open(T, ">$tmpfile") || die "Cannot open $tmpfile: $!\n";
  foreach $cluster_id (sort { $a <=> $b } keys %cluster) {
    @concept_id = sort { $a <=> $b } @{ $cluster{$cluster_id} };
    $min_concept_id = $concept_id[0];
    $md5 = GeneralUtils->md5(join(",", @concept_id));
    $line = join("|", $cluster_id, $min_concept_id, $md5);
    print T $line, "\n";
  }
  close(T);
  $spec = [{cluster_id=>'integer'}, {min_concept_id=>'integer'}, {md5=>'char(32)'}];
  $dbh->createTable($md5table, $spec);
  $dbh->file2table($tmpfile, $md5table, $spec);
  unlink $tmpfile;

  $sql = <<"EOD";
create $editedClustersTable as
select distinct cluster_id from $md5table a, $AHHISTORY b, $CANONICALNAME c
where  a.min_concept_id=b.min_concept_id
and    a.md5=b.md5
and    b.bin_name=c.bin_name
and    c.canonical_name=$qc
EOD
  $dbh->execute($sql);
  return;
}

# retracts a worklist table
sub retractWorklist {
  my($class, $dbh, $worklist_name) = @_;
  my($sql);

  $sql = "delete from $EMSNames::BEINGEDITEDTABLE where worklist_name=" . $dbh->quote($worklist_name);
  $dbh->executeStmt($sql);

  $sql = "delete from $EMSNames::WORKLISTINFOTABLE where worklist_name=" . $dbh->quote($worklist_name);
  $dbh->executeStmt($sql);

# CLEAR HISTORY if AH bin
  my($binconfig);
  my($bin_name) =  WMSUtils->worklist2bin($worklist_name);

  $binconfig = EMSUtils->getBinconfig($dbh, $EMSNames::MECONFIGTABLE,);
  unless ($binconfig) {
    $binconfig = EMSUtils->getBinconfig($dbh, $EMSNames::QACONFIGTABLE, $bin_name);
    unless ($binconfig) {
      $sql = "delete from $EMSNames::AHHISTORYTABLE where worklist_name=" . $dbh->quote($worklist_name);
      $dbh->executeStmt($sql);
    }
  }
  $dbh->dropTable($worklist_name);

# reset the worklist counter in BININFO? No - for now
}


# Generates data from a worklist or checklist to load into a history table
sub list2history {
  my($class, $dbh, $list, $histtable) = @_;
  my($concept_id_col) = "concept_id";
  my($sql, $tmpfile);
  my($line, @concept_id, $min_concept_id, $md5);
  my(%cluster);
  my($cluster_id, $concept_id);
  my($bin_name, $spec);

  $concept_id_col = "orig_concept_id" if (($list =~ m/^wrk/i) || ($list =~ m/^chk/i));
  $tmpfile = $class->tempFile;
  $sql = "select distinct cluster_id, $concept_id_col from $list order by cluster_id";

  $dbh->selectToFile($sql, $tmpfile);
  open(T, $tmpfile) || die "Cannot open $tmpfile: $!\n";
  while (<T>) {
    chomp;
    ($cluster_id, $concept_id) = split /\|/, $_, 2;
    push @{ $cluster{$cluster_id} }, $concept_id;
  }
  close(T);

  unlink $tmpfile;
  open(T, ">$tmpfile") || die "Cannot open $tmpfile: $!\n";
  foreach $cluster_id (sort { $a <=> $b } keys %cluster) {
    @concept_id = sort { $a <=> $b } @{ $cluster{$cluster_id} };
    $min_concept_id = $concept_id[0];
    $md5 = GeneralUtils->md5(join(",", @concept_id));
    $line = join("|", $cluster_id, $min_concept_id, $md5);
    print T $line, "\n";
  }
  close(T);

  $spec = [{cluster_id=>'integer'}, {min_concept_id=>'integer'}, {md5=>'char(32)'}];
  $dbh->createTable($histtable, $spec);
  $dbh->file2table($tmpfile, $histtable, $spec); # slow but portable
#  $dbh->sqlldr($tmpfile, $histtable, $spec);

  unlink $tmpfile;

  $dbh->createIndex($histtable, "md5",  "x_" . $histtable . "_1");
  $dbh->createIndex($histtable, "min_concept_id", "x_" . $histtable . "_2");
}

# Can user access EMS for this DB?
sub canAccessEMS {
  my($class, $user, $db) = @_;
  my($r, $prop, $level, $configlevel);

# what is the highest level for this user
  foreach (2, 1, 0) {
    $prop = sprintf("%s%s%s", "HTTP_LEVEL", $_, "_USER");
    next unless (grep { $_ eq $user } @{ $main::EMSCONFIG{$prop} });
    $level = $_;
    last;
  }

  if (lc($main::EMSCONFIG{EMS_ACCESS_MODE}) eq "allow/deny") {

    foreach (@{ $main::EMSCONFIG{EMS_ACCESS}} ) {
      ($configaction, $configdb, $configlevel) = split /:/, $_, 3;
      next if (lc($configaction) eq "allow");
      next if ($configdb ne '*' && lc($configdb) ne lc($db));
      next if ($configlevel ne '*' && $configlevel != $level);
      return 0;
    }
    return 1;

  } elsif (lc($main::EMSCONFIG{EMS_ACCESS_MODE}) eq "deny/allow") {

    foreach (@{ $main::EMSCONFIG{EMS_ACCESS}} ) {
      ($configaction, $configdb, $configlevel) = split /:/, $_, 3;
      next if (lc($configaction) eq "deny");
      next if ($configdb ne '*' && lc($configdb) ne lc($db));
      next if ($configlevel ne '*' && $configlevel != $level);
      return 1;
    }
    return 0;

  } else {
    return 0;
  }
}

# Generic logging function for EMS and WMS
sub ems_log {
    my($class, $logfile, $msg) = @_;
    my(@log, $logmsg);

    $main::LASTLOGTIME = time unless $main::LASTLOGTIME>0;

    push @log, "Program: " . $0;
    push @log, "Version: " . $main::VERSION;
    push @log, "Session ID: " . $main::SESSIONID;
    push @log, "Date: " . GeneralUtils->date("");
    push @log, "Time since last log entry for this session (secs): " . (time-$main::LASTLOGTIME);
    push @log, "Action: " . $main::action;
    push @log, "HTTP user: " . $main::httpuser;
    push @log, "UNIX user: " . $main::unixuser;

    push @log, "Remote host: " . $ENV{'REMOTE_HOST'};
    push @log, "Remote IP: " . $ENV{'REMOTE_ADDR'};
    push @log, "Database: " . $main::db;
    push @log, "Config: " . $ENV{EMS_CONFIG};
    push @log, "ERROR: " . $@ if $@;
    push @log, "DBI ERROR: " . $DBI::errstr if $DBI::errstr;
    push @log, "Message: " . join("\t", split /\n/, $msg) if $msg;

    if ($main::EMSCONFIG{LOGLEVEL} > 1) {
      $logmsg = "\n" . "-" x 40 . "\n" . join("\n", @log);
    }
    open(LOG, ">>$logfile");
    print LOG $logmsg, "\n";
    close(LOG);

    $main::LASTLOGTIME = time;
}

# logs error messages for EMS and WMS
sub ems_error_log {
    my($class, $logfile, $msg) = @_;
    my($old) = $main::EMSCONFIG{LOGLEVEL};

    $main::EMSCONFIG{LOGLEVEL} = 2;
    $class->ems_log($logfile, $msg);
    $main::EMSCONFIG{LOGLEVEL} = $old;
}

# simple logging (adds time info)
sub plain_log {
    my($class, $logfile, $msg) = @_;

    $LASTPLAINLOGTIME = time unless $LASTPLAINLOGTIME>0;

    open(LOG, ">>$logfile");
    print LOG "\n", "-" x 10, "Time to execute previous command: ", GeneralUtils->sec2hms(time-$LASTPLAINLOGTIME), "-" x 10, "\n\n";
    print LOG "Current time: ", GeneralUtils->date, "\n";
    print LOG "Session ID: ", $main::SESSIONID, "\n";
    print LOG $msg, "\n";
    close(LOG);

    $LASTPLAINLOGTIME = time;
}

#------------------------------------------------------------

sub ME_refresh {
  my($self, $dbh) = @_;
}

sub ME_worklist {
}

sub ME_checklist {
}

# makes one or more worklists from a QA bin
sub QA_worklist {
}

# makes a checklist from a QA bin
sub QA_checklist {
}

# re-generates the contents of one or more AH bin
sub AH_generate {
}

# Calls the MEME matrix initializer to recompute all concept status values
sub matrixinit {
}

# calls the MEME CUI assignment procedure
sub cuiassign {
}

#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
#################################################################################
# takes a table with concept ID's and splits the content into two tables,
# one with chemical concepts and the other with non-chemical concepts
sub chemSplit {
  my($self, $dbh, $param) = @_;
  my($full, $chem, $nonchem);
  my($sql);

  $full = $param->{full};
  $chem = $param->{chem};
  $nonchem = $param->{nonchem};

  return unless $full && $dbh->tableExists($full);

  $dbh->dropTables([$chem, $nonchem]);
  $sql = <<"EOD";
create table $chem as
select a.concept_id from $EMSUtils::CHEMNONCHEM_CLASSIFIER a, $full b
where  a.concept_id=b.concept_id
and    a.chemnonchem='C'
EOD
  $dbh->executeStmt($sql) if $chem;

  if ($chem) {
    $dbh->createIndex($chem, 'concept_id', EMSUtils->tempIndex($dbh));
    $sql = <<"EOD";
create table $nonchem as
select a.concept_id from $full a
where  not exists (select concept_id from $chem b where b.concept_id=a.concept_id)
EOD
  } else {
    $sql = <<"EOD";
create table $nonchem as
select a.concept_id from $EMSUtils::CHEMNONCHEM_CLASSIFIER a, $full b
where  a.concept_id=b.concept_id
and    a.chemnonchem='N'
EOD
  }
  $dbh->executeStmt($sql) if $nonchem;
}

# loads data from a table (id, cluster_id) to the history filter table (used to init EMS 3.0)
sub loadHistoryFromTable {
  my($self, $dbh, $table, $canonicalName, $worklist, $idType) = @_;
  my($keyTable);
  my($sql);

  $self->createHistoryTable($dbh) unless $dbh->tableExists($EMSUtils::HISTORYTABLE);
  $keyTable = $self->makeHistoryKeyTable($dbh, $table);

  $dbh->dropIndex($EMSUtils::HISTORYTABLE);
  $sql = <<"EOD";
insert into $EMSUtils::HISTORYTABLE(cluster_id, key, CanonicalName, WorklistName, IdType)
select cluster_id, key, \'$canonicalName\', \'$worklist\', \'$idType\' from $keyTable
EOD
  $dbh->executeStmt($sql);
  $dbh->createIndex($EMSUtils::HISTORYTABLE, 'key', EMSUtils->tempIndex($dbh));
  $dbh->dropTable($keyTable);
}

# history filter for concept_id bins - removes clusters from a table that
# have been previously edited for this class of bins (named in canonicalName)
sub historyFilterOLD {
  my($self, $dbh, $canonicalName, $idType, $binTable, $filteredTable) = @_;
  my($keyTable, $editedTable);

  $self->createHistoryTable($dbh) unless $dbh->tableExists($EMSUtils::HISTORYTABLE);
  $keyTable = EMSUtils->makeHistoryKeyTable($dbh, $binTable);
  $dbh->createIndex($keyTable, "key", EMSUtils->tempIndex($dbh));

# find all clusters edited before
  $editedTable = EMSUtils->tempTable($dbh);
  $sql = <<"EOD";
create table $editedTable as select cluster_id from $keyTable k
where  exists (
	       select * from $EMSUtils::HISTORYTABLE h
	       where  h.key=k.key
	       and    h.CanonicalName=\'$canonicalName\'
	       and    h.IdType=\'$idType\'
	      )
EOD
  $dbh->executeStmt($sql);

  $sql = <<"EOD";
create $filteredTable as select * from $binTable
where  cluster_id NOT IN (select cluster_id from $editedTable)
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTables([$keyTable, $editedTable]);
}

# makes keys (using cksum) for a table that has id and cluster_id attributes
# returns table name
sub makeHistoryKeyTable {
  my($self, $dbh, $inputTable) = @_;
  my($keyTable) = EMSUtils->tempTable($dbh);
  my($tmpfile, $x, $y, @x);
  my(%ids);

# create keys for clusters in $binTable
  $tmpfile = EMSUtils->tempFile;
  $dbh->selectToFile("select id, cluster_id from $inputTable", $tmpfile);
  open(T, $tmpfile) || die "Cannot open $tmpfile\n";
  while (<T>) {
    chomp;
    @x = split /\|/, $_, 2;
    $ids{$x[1]}->{$x[0]}++;
  }
  close(T);
#  unlink $tmpfile;

  open(T, ">$tmpfile") || die "Cannot write to $tmpfile\n";
  while (($x, $y) = each %ids) {
    $k = crc(join(',', sort { $a <=> $b } keys %{ $y }), 32);
    print T join('|', $x, $k), "\n";
  }
  close(T);
  $dbh->file2table($tmpfile, $keyTable, ['cluster_id', 'key']);
  unlink $tmpfile;
  return $keyTable;
}

sub createHistoryTable {
  my($self, $dbh) = @_;
  my($colspec) = [{CanonicalName=>'varchar(128)'}, {WorklistName=>'varchar(128)'}, 'cluster_id', {IdType=>'varchar(128)'}, 'key'];
  $dbh->createTable($EMSUtils::HISTORYTABLE, $colspec);
}

# Given a file of concept_id's and (optional) cluster_id's in the second field
# it returns a file that is guaranteed to have the concept_id|cluster_id structure
# Caller must delete the returned file
sub clusterizeFile {
  my($class, $file) = @_;
  my($tmpfile, $alreadyclustered);

  $tmpfile = $class->tempFile;
  $cluster_id = 1;

  open(O, ">$tmpfile") || return;

  if (ref($file) eq "GLOB") {
    $fd = $file;
  } else {
    use Symbol;
    $fd = gensym;
    open($fd, $file) || die "ERROR: Cannot open $file";
  }

  while (<$fd>) {
    chomp;
    @f = split /\|/, $_;

    if (@f>1 && $f[1] =~ /^\d+$/) {
      print O join("|", $f[0], $f[1]), "\n";
    } else {
      print O join("|", $f[0], $cluster_id++), "\n";
    }
  }
  close($fd) unless ref($file);
  close(O);
  return $tmpfile;
}

# adds a cluster_id column to a table and initializes it if needed
sub clusterizeTable {
  my($class, $dbh, $table) = @_;
  my($sql);

  return if $dbh->tableHasColumn($table, 'CLUSTER_ID');
  $dbh->addColumn($table, "CLUSTER_ID", "INTEGER");
  $sql = <<"EOD";
update $table a set CLUSTER_ID=
(select cluster_id from
 (select concept_id, rownum as cluster_id from $table) where concept_id=a.concept_id
)
EOD
  $dbh->executeStmt($sql);
}

# pivots a table of concept_id pairs to a clusterized table
sub pivotTable {
  my($class, $dbh, $table) = @_;
  my($sql);
  return if $dbh->tableHasColumn($table, 'CLUSTER_ID');
  return unless ($dbh->tableHasColumn($table, 'CONCEPT_ID_1') && $dbh->tableHasColumn($table, 'CONCEPT_ID_2'));

  my($tmptable) = EMSUtils->tempTable($dbh);

  $sql = <<"EOD";
create table $tmptable as
select concept_id, cluster_id from (
  SELECT concept_id_1 as concept_id, rownum as cluster_id from $table
  union
  SELECT concept_id_2 as concept_id, rownum as cluster_id from $table
)
EOD
  $dbh->executeStmt($sql);
  $dbh->dropTable($table);

  $sql = <<"EOD";
create table $table as select concept_id, cluster_id from $tmptable
EOD
  $dbh->executeStmt($sql);
}

# escapes quotes for VARCHAR
# must be passed a reference to a scalar
sub quote {
  my($self, $dbh, $ref) = @_;

  $dbh->quote($$ref) if (ref($ref) eq "SCALAR");
}

sub tmpTable {
  return tempTable(@_);
}

# Makes up a temporary table name
sub tempTable {
  my($self, $dbh, $prefix) = @_;

  my($key) = $EMSNames::TMPTABLENUMKEY;
  my($maxtab) = EMSMaxtab->get($dbh, $key);
  my($num) = sprintf("%.16d",$maxtab->{valueint});

  if ($num >= 500000) {
    EMSMaxtab->set($dbh, $key, {valueint=>1});
  } else {
    EMSMaxtab->increment($dbh, $key);
  }

  $prefix = $EMSNames::TMPTABLEPREFIX unless $prefix;

# add a random component
  my($r) = sprintf("%.2d%.2d", int(rand 99), $$ % 100);
  my($table) = substr(sprintf("%s_%d_%d", $prefix, $num, $r), 0, 32);
  return $table;
}

# Makes up a temporary index name
sub tempIndex {
  my($self, $dbh, $prefix) = @_;

  my($key) = $EMSNames::TMPTABLENUMKEY;
  my($maxtab) = EMSMaxtab->get($dbh, $key);
  my($num) = sprintf("%.12d",$maxtab->{valueint});

  if ($num >= 500000) {
    EMSMaxtab->set($dbh, $key, {valueint=>1});
  } else {
    EMSMaxtab->increment($dbh, $key);
  }

  $prefix = $EMSNames::TMPTABLEPREFIX unless $prefix;

# add a random component
  my($r) = sprintf("%.2d%.2d", int(rand 99), $$ % 100);
  my($index) = substr(sprintf("x_%s_%d_%d", $prefix, $num, $r), 0, 24);
  return $index;
}

# Makes up a temporary file name
sub tempFile {
  my($self, $prefix) = @_;
  my($tmpdir) = $EMSNames::TMPDIR || "/tmp";
  my($tries, $file);

  $prefix = $EMSNames::TMPTABLEPREFIX unless $prefix;

  $tries = 10;
  while ($tries-- > 0) {
    $file = sprintf("%s/%s_%.2d%.2d", $tmpdir, $prefix, int(rand 99), $$ % 100);
    last unless -e $file;
  }
  return $file;
}

# modifies a table to conform to bin structure
# by adding columns with suitable defaults as needed
sub addBinColumns {
  my($dbh, $table) = @_;
  my($sql);

  unless ($dbh->tableHasColumn($table, "idtype")) {
    $sql = <<"EOD";
ALTER TABLE $table ADD (idtype VARCHAR(32) DEFAULT 'concept_id')
EOD
    $dbh->executeStmt($sql);
  }

  unless ($dbh->tableHasColumn($table, "cluster_id")) {
    $sql = <<"EOD";
ALTER TABLE $table ADD (cluster_id INTEGER DEFAULT -5000)
EOD
    $dbh->executeStmt($sql);
    $sql = <<"EOD";
UPDATE $table SET cluster_id=ROWNUM WHERE cluster_id = -5000
EOD
    $dbh->executeStmt($sql);
  }

  unless ($dbh->tableHasColumn($table, "intracluster_id")) {
    $sql = <<"EOD";
ALTER TABLE $table ADD (cluster_id INTEGER DEFAULT 1)
EOD
    $dbh->executeStmt($sql);
  }
}

# aid to debugging
sub dumper {
  my($self, $s, $f) = @_;
  my($file) = "/tmp/emsdebug.$$" unless $f;

  if ($f) {
    print $f Dumper($s), "\n";
    return;
  }

  open(F, ">>$file") || return;
  print F Dumper($s), "\n";
  close(F);
}

# -----------------------------
1;
