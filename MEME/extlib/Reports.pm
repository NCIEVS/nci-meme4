# Concept report related queries

# suresh@nlm.nih.gov 3/2003

package Reports;

use lib "/site/umls/lib/perl";

use GeneralUtils;
use ParallelExec;
use Midsvcs;
use Data::Dumper;
use File::Copy;

# some variables
$MEME_HOME="/d5/MEME4";
$NUMCPU=4;
$REPORT_TOP_DIR="/d3/ems/reports";
$TMPDIR="/tmp";

# CONSTRUCTOR
sub new {
  my($class, $initializer) = @_;
  my($self) = {};
  
  if (defined $initializer) {
    if (ref($initializer)) {
      if (ref($initializer) eq "HASH") {
	foreach (keys %{ $initializer }) {
	  $self->{ $_ } = $initializer->{$_};
	}
      } elsif (ref($initializer) eq "ARRAY") {
	for ($i=0; $i<@{$initializer}-1; $i++) {
	  $_[0] = $initializer->[$i];
	  $_[1] = $initializer->[$i+1];
	  $self->{$_[0]} = $_[1];
	}
      } elsif (ref($initializer) eq "SCALAR") {
	foreach (split /\&/, $$initializer) {
	  @_ = split /\=/, $_;
	  $self->{$_[0]} = $_[1];
	}
      }
    } else {
      foreach (split /\&/, $initializer) {
	@_ = split /\=/, $_;
	$self->{$_[0]} = $_[1];
      }
    }
  }

  bless $self;
  return $self;
}

# CLASS METHODS

# returns MEME concept reports for all (clustered) IDs
#
# params is a hash of:
# db=
# meme-server-host=
# meme-server-port=
# meme_home=
# dbh= (database handle for the inputtable option)
# idtype={concept_id|atom_id|cui}
# id=
# cluster_id=
# inputfile=
# outputfile=
# inputtable=
# format={html|enscript|text}
# highlight= (some string to highlight)
#
# or send a ReportRequest as reportrequest=
sub MIDReport {
  my($self, $param) = @_;
  my($numcpu);
  my(%clusters);

  $numcpu = ($param->{reportrequest} ?
	     $param->{reportrequest}->get('NumCpu') : $param->{cpu}) || 2;

# get clusters
  if ($param->{inputtable} || $param->{worklist}) {
    my($r);
    my($t) = $param->{inputtable} || $param->{worklist};
    die unless $param->{dbh};
    my(@refs) = $param->{dbh}->selectAllAsRef("select distinct orig_concept_id, cluster_id from $t order by cluster_id");

    foreach $r (@refs) {
      push @{ $clusters{$r->[1]} }, $r->[0];
    }
  } elsif ($param->{inputfile}) {
    my($n) = 1;
    my($c);
    open(F, $param->{inputfile}) || die "Cannot open " . $param->{inputfile};
    while (<F>) {
      chomp;
      @_ = split /\|/, $_, 2;
      $c = (($_[1] =~ /^\d+$/) ? $_[1] : $n++);
      push @{ $clusters{$c} }, $_[0];
    }
    close(F);
  } elsif ($param->{id} && ref($param->{id}) eq "ARRAY") {
    my($id);
    my($c) = 1;
    foreach $id (@{ $param->{id} }) {
      $clusters{$c++} = $id;
    }
  } elsif ($param->{id}) {
    my($c) = $param->{cluster_id} || 1;
    $clusters{$c} = [ split /\W+/, $param->{id} ];
  }

  my($outputfile) = $param->{outputfile} || GeneralUtils->tempname("/tmp", "tmprpt");

# generate reports in parallel if needed
  my($tmpfile, @tmpfiles);
  my($i);

  $parallel = new ParallelExec($numcpu) if $numcpu>1;
  for ($i=1; $i<=$numcpu; $i++) {
    push @tmpfiles, GeneralUtils->tempname("/tmp", sprintf("tmprpt%.2d",$i));
  }

  my(@cmd);
  my($n) = 0;
  my(@orderedClusterIds) = sort { $a <=> $b } keys %clusters;
  my($cmd);

# run in parallel on all available cylinders
  for (;;) {
    @cmd = ();

    foreach $tmpfile (@tmpfiles) {
      last if $n >= @orderedClusterIds;
      unlink $tmpfile if -e $tmpfile;
      $cluster_id = $orderedClusterIds[$n++];
      $cmd = &xreports_cmdline($param, $cluster_id, $clusters{$cluster_id}, $tmpfile);
#print STDERR $cmd, "\n";
      push @cmd, $cmd if $cmd;
    }
    if (@cmd) {
      if ($numcpu>1) {
	$parallel->run(\@cmd);
      } else {
#	print STDERR $cmd[0], "\n";
	system $cmd[0];
      }

      foreach $tmpfile (@tmpfiles) {
	next unless -e $tmpfile;
	system "/bin/cat $tmpfile >> $outputfile";
	unlink $tmpfile if -e $tmpfile;
      }
    } else {
      last;
    }
  }

  if ($param->{outputfile} && $param->{outputfile} ne $outputfile) {
    copy($outputfile, $param->{outputfile});
    unlink $outputfile;
    return;
  } elsif ($param->{outputfile}) {
    return;
  } else {
    my($rpt) = GeneralUtils->file2str($outputfile);
    unlink $outputfile;
    return $rpt;
  }
}

# a helper function to construct the xreports.pl command line
sub xreports_cmdline {
  my($param, $cluster_id, $idref, $file) = @_;
  my($cmd) = ($param->{meme_home} || $Reports::MEME_HOME ) . "/bin/xreports.pl";
  my($port);
  return undef unless -e $cmd;

  $ENV{'MEME_HOME'} = $param->{meme_home} || $ENV{'MEME_HOME'} || $Reports::MEME_HOME;
  $ENV{'ORACLE_HOME'} = "/export/home/oracle" unless $ENV{'ORACLE_HOME'};

  my($idtype) = $param->{idtype} || "concept_id";

  if (ref($idref) eq "ARRAY") {
    foreach (@{ $idref }) {
      $_ = sprintf("C%07d", $_) if ($idtype eq "cui" && /^\d+$/);
    }
    $ids = join(',', @{ $idref });
  } else {
    $ids = $idref;
  }
  return "" unless $ids;
  $cmd .= (($idtype eq "concept_id") ? " -c $ids" : ($idtype eq "atom_id") ? " -a $ids" : " -i $ids");
  $cmd .= " -cluster $cluster_id" if $cluster_id;

  $cmd .= " -d " . (($param->{reportrequest} ?
		    $param->{reportrequest}->get('Database') : $param->{db}) || Midsvcs->get("editing-db"));
  if ($param->{reportrequest}) {
    $cmd .= " -host " . Midsvcs->get($param->{reportrequest}->get('MEMEServerHost') || "meme-server-host");
  } else {
    $cmd .= " -host " . ($param->{'meme-server-host'} || Midsvcs->get("meme-server-host"));
  }
  $port  = ($param->{reportrequest} ?
	    $param->{reportrequest}->get('MEMEServerPort') : $param->{'meme-server-port'});
  $cmd .= " -port $port" if $port;

  $cmd .= " -html" if $param->{format} eq "html";
  $cmd .= " -enscript" if (($param->{reportrequest}) || ($param->{format} eq "enscript"));

# highlights - what, where and how
  my($highlight) = ($param->{reportrequest} ? ($param->{reportrequest}->get('Highlight')) : ($param->{highlight}));
  if ($highlight) {
    my($h) = $highlight;
    my(@h);
    $h =~ s/\*/\\*/;
    $h =~ s/\+/\\+/;
    $h =~ s/\./\\./;
    $h =~ s/\[/\\[/;
    $h =~ s/\]/\\]/;
    $h =~ s/\(/\\(/;
    $h =~ s/\)/\\)/;
    $h =~ s/\'//;
    $h =~ s/\"//;
    $h =~ s/\-/\\-/;
    push @h, "regexp=.*$h.*";
#    push @h, ($param->{reportrequest} ? 
#	      $param->{reportrequest}->get('HighlightStyle') :
#	      $param->{highlightstyle}) || "shade=0.9;";
    push @h, "shade=0.9";
    push @h, ($param->{reportrequest} ?
	      ($param->{reportrequest}->get('HighlightSections')) :
	      ($param->{highlightsections}));
    $cmd .= " -style \"" . join(';', @h) . "\"" if @h;
  }
  $cmd .= ">$file";
  return $cmd;
}

# returns a MEME concept report given a ID
# params is a hash of:
# db=
# meme-server-host=
# meme-server-port=
# concept_id=
# atom_id=
# cui=
# format={html|enscript|text}
# highlight= (some string to highlight)
sub getMIDReportForID {
  my($self, $params);
}
#----------------------------------------
1;
