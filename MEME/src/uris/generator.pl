#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

use lib "$ENV{EXT_LIB}";
use lib "$ENV{URIS_HOME}/lib";
use lib "$ENV{URIS_HOME}/bin";


#!@PATH_TO_PERL@

# Master script to generate URIS contents
# Uses an XML config file to drive the process

# Supported options:
# -c <config file>
# -s (specific script:host to run)
# -n (run sequentially - no forks)
# -g (debug)

# XML in config file
# <urisconfig>
# <!-- these are global directives and can be overridden in <script> directives below -->
# <releaseversion></releaseversion>
# <type>ORF, RRF, MR or MR+</type>
# <description>a description of the release</description>
# <outputdir></outputdir>
# <releasetopdir></releasetopdir>
# <metadir></metadir>
# <netdir></netdir>
# <run></run>
# <cpu></cpu>
# <force></force>
#
# <script>
#   <name></name>
#   <description></description>
#   <run>no</run>
#   <inputformat>ORF, RRF, MR or MR+</inputformat>
#   <host></host>
#   <outputdir></outputdir>
#   <command></command>
#   <database></database>
#   <releaseversion></releaseversion>
#   <catgories></categories>
#   <releasetopdir></releasetopdir>
#   <metadir></metadir>
#   <netdir></netdir>
#   <input type="file|stdin|arg" except="" stdargs=1></input>
# </script>
# ...
# </urisconfig>

#use lib "/umls/lib/perl";
#use lib "/umls/urisqa/QA/generator";
#use lib "/umls/urisqa/QA/src";

use Getopt::Std;
use XML::Simple;
use Data::Dumper;
use GeneralUtils;
use ParallelExec;
use UrisUtils;

getopts("c:s:ng");

die "Need a configuration file to drive the generator in -c" unless $opt_c;
$config = &read_config($opt_c);
die "ERRORS in reading $opt_c" unless $config;
$opt_n = 1 if $opt_g;

# read list of files into $config from MRFILES
$mrfiles = join('/', $config->{releasetopdir}, "META", ($config->{releaseformat} eq "RRF" ? "MRFILES.RRF" : "MRFILES"));
die "ERROR: Cannot read MRFILES metadata\n" unless -e $mrfiles;
@_ = ();
open(M, $mrfiles) || die "ERROR: Cannot open $mrfiles";
while (<M>) {
  chomp;
  @_ = split /\|/, $_;
  push @f, $_[0];
}
close(M);
$config->{files} = join(',', @f);

# load host specific info
foreach $r (@{ $config->{host} }) {
  $host{$r->{name}} = $r;
  push @host, $r;
}

$scripts = $config->{script};
chomp($myhostname = `/bin/uname -n`);

$config->{type} = "RRF" unless $config->{type};
$config->{description} = "Full " . $config->{type} . "release for: " . $config->{releaseversion} unless $config->{description};

$SIG{'CHLD'} = \&reaper unless $opt_s;
sub reaper {
  my($kid);
  while (($kid = waitpid(-1, WNOHANG)) > 0) {
    if ($childpid{$kid}) {
      &logD("Job queue for " . $childhost{$kid} . " done");
      delete $childpid{$kid};
    }
  }
}

&logD("Starting $0");
&log("Release version: " . $config->{releaseversion} . ": " . $config->{description});

my($canrun);

$config->{run} = "no" unless $config->{run}; # default

foreach (split /,/, $opt_s) {
  @_ = split /:/, $_, 2;
  push @scriptsToRun, $_[0];
  $hostToRunOn{$_[0]} = $_[1] if $_[1];
}

$n=0;
foreach $script (@{ $scripts }) {
  if ($opt_s) {
    next unless grep { $_ eq $script->{name} } @scriptsToRun;
  }

  $canrun = 0;
  if (lc($config->{run}) eq "no" || $config->{run} eq "0") {
    if ($script->{run} && (lc($script->{run}) eq "yes" || $script->{run} eq "1")) {
      $canrun = 1;
    } else {
      $canrun = 0;
    }
  } else {
    if (lc($script->{run}) eq "no" || $script->{run} eq "0") {
      $canrun = 0;
    } else {
      $canrun = 1;
    }
  }

  unless ($canrun) {
    &log($script->{name} . ": Not run because of the run=no directive");
    next;
  }

  $outputdir = $script->{outputdir} || $config->{outputdir} || "/tmp";
  $stdoutfile = join("/", $outputdir, $script->{name} . ".stdout");
  $force = $script->{force} || $config->{force};
  unlink $stdoutfile if lc($force) eq "yes";
  if (-e $stdoutfile) {
    &log($script->{name} . ": Not run because output file: $stdoutfile exists and the force directive was not used");
    next;
  }

  if ($script->{host}) {
    push @{ $queue{$script->{host}} }, $script;
  } elsif ($hostToRunOn{$script->{name}}) {
    push @{ $queue{$hostToRunOn{$script->{name}}} }, $script;
  } else { # pick the next host
    $host = $host[$n++];
    $n=0 if $n == @host;
    push @{ $queue{$host->{name}} }, $script;
  }
}

# process each host's queue in parallel
foreach $hostname (keys %queue) {
  &processQueue($hostname);
}

# wait for all jobs to be done
unless ($opt_n) {
  do {
    $kid = waitpid(-1, WNOHANG);
  } until $kid == -1;
}

&logD("Done $0");
exit 0;

# processes a host's queue of jobs in a subprocess
sub processQueue {
  my($hostname) = @_;
  my($script);
  my($kid);

  unless ($opt_n) {
    if ($kid = fork) {
      $childpid{$kid}++;
      $childhost{$kid} = $hostname;
      return;
    }
  }

  &logD("Starting job queue for " . $hostname . " (" . join(',', map { $_->{name} } @{ $queue{$hostname} }) . ")");
  foreach $script (@{ $queue{$hostname}}) {
    &doit($script, $hostname);
  }

  return if $opt_n;
  exit 0;
}

# calls the script with the appropriate arguments
sub doit {
  my($script, $hostname) = @_;
  my($cmd);
  my($outputdir) = $script->{outputdir} || $config->{outputdir} || "/tmp";
  
  my($stdoutfile, $stderrfile, $progressfile, $metadatafile, $runningfile);
  my($name) = $script->{name};
  my($starttime, $endtime);
  my($r, $ref, $n, $p);
  my(%metadata);
  my($hostref) = $host->{$hostname};

  $substitute{releaseversion} = $script->{releaseversion} || $config->{releaseversion};
  $substitute{previousversion} = $script->{previousversion} || $config->{previousversion};
  $substitute{releasetopdir} = $hostref->{releasetopdir} || $config->{releasetopdir};
  if (lc($config->{releasetype}) eq "full") {
    $substitute{metadir} = $hostref->{metadir} || $config->{metadir} || ($substitute{releasetopdir} . "/META");
    $substitute{netdir} = $hostref->{netdir} || $config->{netdir} || ($substitute{releasetopdir} . "/NET");
  } else {
    $substitute{metadir} = $hostref->{metadir} || $config->{metadir} || $substitute{releasetopdir};
  }

# standard files
  $stdoutfile = join("/", $outputdir, $name . ".stdout");
  $stderrfile = join("/", $outputdir, $name . ".stderr");
  $progressfile = join("/", $outputdir, $name . ".progress");
  $metadatafile = join("/", $outputdir, $name . ".metadata");
  $runningfile = join("/", $outputdir, $name . ".running");
  unlink $stdoutfile unless $script->{appendstdout};
  unlink $stderrfile, $progressfile, $metadatafile, $runningfile;

  GeneralUtils->str2file(GeneralUtils->date, $runningfile);

  $metadata{host} = $hostname;
  $metadata{starttime} = time;
  $metadata{startdate} = GeneralUtils->date;

  &logD($script->{name} . ": Starting on $hostname");

# Get all the input files
  if (lc($script->{input}->{content}) eq "*") {
    @input = split /,/, $config->{files};
  } else {
    my(@except) = split /,/, $script->{input}->{except};
    my($file);

    foreach $file (split /,/, $script->{input}->{content}) {
      next if grep { $_ eq $file } @except;
      push @input, $file;
    }
  }

  $n=0;
  $cpu = $hostref->{cpu} || $config->{cpu} || 2;
  $cpu = @input if @input && ($cpu > @input);
  $p = new ParallelExec($cpu);
  $metadata{cpu} = $cpu;

  unless (@input) {
    $cmd = "";
    $cmd .= "/bin/rsh $hostname " if $hostname ne $myhostname;

    $ref[0]->{stdout} = GeneralUtils->tempname(
					       ($script->{tmpdir} || $config->{tmpdir} || "/tmp"),
					       sprintf("urisgenstdout_%.2d", $n)
					      );
    $ref[0]->{stderr} = GeneralUtils->tempname(
					       ($script->{tmpdir} || $config->{tmpdir} || "/tmp"),
					       sprintf("urisgenstderr_%.2d", $n)
					      );
    $script->{command} = $config->{scriptdir} . "/" . $script->{command} unless $script->{command} =~ m%^/%;
    $cmd .= &varSubstitute($script->{command});
    $logcmd = $cmd;
    $cmd .= " 1>" . $ref[0]->{stdout} . " 2>" . $ref[0]->{stderr};
    $ref[0]->{cmd} = $cmd;
    $ref[0]->{logcmd} = $logcmd;

  } else {

    foreach $file (@input) {
      $cmd = "";
      $cmd .= "/bin/rsh $hostname " if $hostname ne $myhostname;

      $ref[$n]->{file} = $file;
      $ref[$n]->{stdout} = GeneralUtils->tempname(
						  ($script->{tmpdir} || $config->{tmpdir} || "/tmp"),
						  sprintf("urisgenstdout_%.2d", $n)
						 );
      $ref[$n]->{stderr} = GeneralUtils->tempname(
						  ($script->{tmpdir} || $config->{tmpdir} || "/tmp"),
						  sprintf("urisgenstderr_%.2d", $n)
						 );

      $path = join("/", $substitute{metadir}, $file);

      if (lc($script->{input}->{type}) eq "file") {
	$cmd .= &varSubstitute($script->{command});
	$cmd .= " -f $path";
	if ($script->{input}->{stdargs}) {
	  $cmd .= " -d " . $script->{database} if $script->{database};
	  $_ = $script->{releaseversion} || $config->{releaseversion};
	  $cmd .= " -v $_" if $_;
	}

      } elsif (lc($script->{input}->{type}) eq "stdin") {
	$cmd .= &varSubstitute($script->{command});
	if ($script->{input}->{stdargs}) {
	  $cmd .= " -d " . $script->{database} if $script->{database};
	  $_ = $script->{releaseversion} || $config->{releaseversion};
	  $cmd .= " -v $_" if $_;
	}
	$cmd .= " < $path";

      } elsif (lc($script->{input}->{type}) eq "arg") {
	$cmd .= &varSubstitute($script->{command});
	if ($script->{input}->{stdargs}) {
	  $cmd .= " -d " . $script->{database} if $script->{database};
	  $_ = $script->{releaseversion} || $config->{releaseversion};
	  $cmd .= " -v $_" if $_;
	}
	$cmd .= " " . $path;
      }
      $logcmd = $cmd;
      $cmd .= " 1>" . $ref[$n]->{stdout} . " 2>" . $ref[$n]->{stderr};
      $ref[$n]->{cmd} = $cmd;
      $ref[$n]->{logcmd} = $logcmd;

      $n++;
    }
  }

  @cmd = ();
  foreach $r (@ref) {
    &log($r->{cmd}) if $opt_g;
    push @cmd, $r->{cmd};
  }
  return if $opt_g;

  $txt = "Starting $name at: " . $metadata{startdate} . "\n";
  &append($progressfile, $txt);
  $txt = "Commands run (parallelism=" . $cpu . "):\n" . join("\n", map { $_->{logcmd} } @ref) . "\n\n";
  &append($progressfile, $txt);

  my(@status) = $p->run(\@cmd);
  my($perldata);
  $n = 0;

  foreach $r (@status) {
    &append($progressfile, "Processed " . $ref[$n]->{file} . " in: " . GeneralUtils->sec2hms($r->{runtime}) . ": status: " . $r->{status} . "\n");

    if (lc($script->{outputformat}) eq "perldata") {
      my($f) = $ref[$n]->{file};

      open(F, $ref[$n]->{stdout}) || die "Cannot open " . $ref[$n]->{stdout};
      @_ = <F>;
      close(F);
      $d = eval(join("\n", @_));
      die "ERROR in $f: $@" if $@;
      $perldata->{$f} = $d;
    } else {
      system "/bin/cat " . $ref[$n]->{stdout} . " >> $stdoutfile";
    }
    system "/bin/cat " . $ref[$n]->{stderr} . " >> $stderrfile";
    unlink $ref[$n]->{stdout};
    unlink $ref[$n]->{stderr};
    $n++;
  }

  if (lc($script->{outputformat}) eq "perldata") {
    open(D, ">$stdoutfile") || die "Cannot open $stdoutfile\n";
    print D Dumper($perldata);
    close(D);
  }
  $endtime = time;
  $metadata{endtime} = $endtime;
  $metadata{enddate} = GeneralUtils->date;
  $metadata{elapsed} = GeneralUtils->sec2hms($metadata{endtime}-$metadata{starttime});
  &append($progressfile, "\nDone $name at: " . GeneralUtils->date . " (" . $metadata{elapsed} . ")");

  open(M, ">$metadatafile") || die "Cannot open $metadatafile";
  print M Dumper(\%metadata);
  close(M);

  unlink $runningfile;
}

# Expands variables in the string using $script and $config params
sub varSubstitute {
  my($s) = @_;

  while ($s =~ m/^(.*)\$(\w+)(.*)$/) {
    $a = $1;
    $v = $2;
    $b = $3;
    if ($substitute{$v}) {
      $s = join('', $a, $substitute{$v}, $b);
    } else {
      $s = join('', $a, $b);
    }
  }
  return $s;
}

# reads the XML and returns a list reference containing the script hashes
sub read_config {
  my($file) = @_;
  my($x) = new XML::Simple(rootname=>'urisconfig', keyattr=>'script');
  my($z) = $x->XMLin($file);
  return $z;
}

sub log {
  my($msg) = @_;
  chomp($msg);
  print $msg, "\n";
}

# logs with date appended
sub logD {
  my($msg) = @_;
  chomp($msg);
  chomp($d=`/bin/date`);
  print $msg, " at $d", "\n";
}

sub append {
  my($file, $msg) = @_;
  open(F, ">>$file") || die "Cannot append to $file";
  print F $msg;
  close(F);
}
