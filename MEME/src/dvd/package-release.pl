#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#    old -- !/share_nfs/perl/5.8.6/bin/perl
#
# Packages a release for distribution (as per spec in the nlmreg archive
# under thread "Release format specification" (circa 1/2004)

# Can be used to create both a "change" and a "full" release.
# See http://meow/ems/doc/packaging-release.html for more information.

# Author: suresh@nlm.nih.gov 1/2004

# Command line options:

# -d <TOP>, e.g., /d21/2002AA
# -v <VERSION> e.g., 2002AA
# -t <TIMESTAMP> in YYYYMMDDHHMI.SS (see man touch)
# -s also log progress to STDERR
# -g <compression grade> 1-9
# -f force a run, else only a report is produced
# -r <REVISIONID> upto two digit integer, e.g., 01, 10, 99
# -p # (parallelize to this degree - default=2)
# -M <name> (makes the package for a non-standard Metathesaurus named in the config file: $TOP/mini.config)

use lib "/umls/lib/perl";


use Getopt::Std;
use File::Basename;
use File::Path;
use File::Copy;
use Cwd;
use ParallelExec;
use Data::Dumper;
use Symbol;
use XML::Simple;

getopts("d:v:t:sg:r:p:M:");

$scriptname = basename($0);
$commandline = join(' ', $0, @ARGV);
$starttime = time;
$startdate = &now;
$cpu = $opt_p || 2;

# this will let us change versions and also ensure that the right version gets used
%progpath = (
	  md5=>'/share_nfs/usr/bin/md5',
	  cksum=>'/bin/cksum',
	  tar=>'/bin/tar',
	  zip=>'/bin/zip',
	  unzip=>'/bin/unzip',
	  gzip=>'/share_nfs/usr/bin/gzip',
	  gunzip=>'/share_nfs/usr/bin/gunzip',
	  grep=>'/bin/grep',
	  awk=>'/bin/gawk',
	  find=>'/bin/find',
	  jar=>"/share_nfs/java/1.4.2/bin/jar",
	  split=>"/bin/split",
	  wc=>"/bin/wc",
	  ln=>"/bin/ln",
);


while (($_, $path) = each %progpath) {
  &loganddie("ERROR: $path not executable") unless -e $path && -x $path;
}

# set up globals
$TOP = $opt_d;
$TOP = cwd() . "/$opt_d" unless $opt_d =~ m@^/@;
$TOP =~ s@/\.$@@;
$TMPDIR="$TOP/tmp";
mkpath($TMPDIR, 0, 0775);

$VERSION = $opt_v;
$revisionID = $opt_r+0;
$revisionIDstr = sprintf("%.2d", $revisionID);
$TIMESTAMP = $opt_t;

$VERSION =~ /^(\d{4})([A-Z][A-Z])$/;
$releaseyear = $1;
$releaseversion = $2;
$janversion = $releaseyear . "AA";

$version = $VERSION;
$version =~ tr/A-Z/a-z/;
$METANLMFILE = "$version-meta.nlm";
$OTHERNLMFILE = "$version-otherks.nlm";
$AUTORUNFILE = "Autorun.inf";
$MMSYSZIP = "mmsys.zip";
$MMSYSTOP = "MMSYS"; # top directory in the zip file
$MINIMETA="MINIMETA";
$WELCOMEZIP="mmsys.zip";
$MSWELCOME="windows_mmsys.bat";
$DOCFILENAME = $VERSION . "_ReleaseDoc";
$DOCZIP = $DOCFILENAME . ".zip";
#$BIGLIMIT = (1*1024*1024*1024); # 1.0GB
$BIGLIMIT = (1024*1024*500); # 500MB
$SMALLLIMIT = (25*1024*1024); # 2MB
chomp($BUILDDATE = `/bin/date \'+%Y_%m_%d_%H_%M_%S\'`);

# which files need to be split into multiple NLM files?
@metanlm = (['MRSAT.RRF', 'MRREL.RRF'], ['MRCXT.RRF']);

# This data structure stores information about files in $MASTER
# where they get copied, optional extensions, etc.
# releasetypes are: base, revision, full, change, mini
# processes are: expanded, dvdimage, miniimage, kssdownload
@masterOverrideFiles = (
			'README.txt',
			'Copyright_Notice.txt',
#			'release.dat'
		       );
#@optionalExtensions = qw(htm HTM html HTML pdf PDF TXT);
@optionalExtensions = qw();

# create the log subdirectory
mkpath("$TOP/log", 0, 0775) unless -e "$TOP/log";
chdir $TOP || &loganddie("ERROR: Cannot chdir to $TOP\n");

if ($opt_M) {
  $miniconfig = &load_mini_config;
  foreach $r (@{$miniconfig}) {
    next unless $r->{name} eq $opt_M;
    $miniref = $r;
    last;
  }
  $MINITOP = join('/', $TOP, "mini");
  if ($miniref && $miniref->{name}) {
    $masterlogfile = "$TOP/log/master2dist." . $miniref->{name} . ".log";
  } else {
    die "ERROR: need a name for the MINI being referenced, e.g., NLMMINI";
  }
  die "ERROR: No META subdir in: " . $miniref->{name} unless -d join('/', $MINITOP, $miniref->{name} . "/META");
  die "ERROR: No mmsys.zip in: " . $miniref->{name} unless -e join('/', $MINITOP, $miniref->{name} . "/" . $MMSYSZIP);

} else {
  $masterlogfile = "$TOP/log/master2dist.log";
}
unlink $masterlogfile;

$baserelease = 1;
&set_globals;

&save_master_timestamps;

$wheretolog = [ "file", $masterlogfile ];
&logger($commandline);
&logger(&header("Starting $scriptname at: " . &now), "skiptime");

if ($revisionID) {

} else {

  if ($opt_M) {
    $baserelease=1;
    $fullrelease=1;
    &set_globals;
#    &make_expanded;
    &make_miniimage;
    &archive_log;
    exit 0;
  }

  $baserelease = 1;
  $changerelease = 0;
  $fullrelease = 1;

  &set_globals;

  &check_master_override_files;
  &check_mmsys_zip;

  &make_expanded;
  &make_dvdimage;
  &make_kssdownload;
#  &make_miniimage;
}

$runtime = time - $starttime;
&logger(&header("Successfully completed $scriptname in " . &hms($runtime)));

&archive_log;
exit 0;

# Creates the EXPANDED directory starting with the contents of MASTER
sub make_expanded {
  &logger(&header("Starting make_expanded at: " . &now), "skiptime");
  &loganddie("ERROR: MASTER Dir: " . $MASTERDIR . " does not exist!") unless -d $MASTERDIR;

  foreach (&empty_files($MASTERDIR . "/$VERSION")) {
    &logandwarn("ERROR: File: $_ is empty in $MASTERDIR/$VERSION", "skiptime");
  }

  foreach (&suspicious_files($MASTERDIR . "/$VERSION/META")) {
    &loganddie("ERROR: File: $_ looks suspicious in " . $MASTERDIR . " - please delete", "skiptime");
  }

  rmtree($EXPANDEDDIR, 0, 0);
  mkpath("$EXPANDEDDIR/$VERSION", 0, 0755);

# Copy from current $MASTER
  &logger("Copying contents of $MASTERDIR/$VERSION to $EXPANDEDDIR/$VERSION");
  system "/bin/cp -p -r $MASTERDIR/$VERSION $EXPANDEDDIR" ||
    &loganddie("ERROR: Cannot copy from $MASTERDIR/$VERSION to $EXPANDEDDIR/$VERSION\n");

#  &copy_master_override_files($EXPANDEDDIR);
  &rmbkup($EXPANDEDDIR);

  if ($TIMESTAMP) {
    &logger("Setting timestamp and permissions in $EXPANDEDDIR/$VERSION");
    &settimestamp("$EXPANDEDDIR/$VERSION", $TIMESTAMP);
  }
#  &setpermissions($EXPANDEDDIR);
#  &checkpermissions($EXPANDEDDIR);

# Add a link to the META directory
  system("$progpath{ln} -s $EXPANDEDDIR/$VERSION/META $TOP/META") unless -e "$TOP/META";

  &logger("\nDone make_expanded");
  return;
}

# makes the DVD image
sub make_dvdimage {
  my(@z, $d);
  my($r, $f);

  &logger(&header("Starting make_dvdimage at: " . &now), "skiptime");

# make a gzip'ed compressed version of the EXPANDED directory
  rmtree($COMPRESSDIR, 0, 0);

# Make first NLM file
  rmtree($DVDIMAGEDIR, 0, 0);
  mkpath($DVDIMAGEDIR, 0, 0755);

  $n=1;
  $metanlmfile = join("-", $version, $n, "meta.nlm");
  system "/bin/cp -p -r $EXPANDEDDIR $COMPRESSDIR" ||
    &loganddie("ERROR: Cannot copy from $EXPANDEDDIR to $COMPRESSDIR");
  foreach $r (@metanlm) {
    foreach $f (@$r) {
      unlink "$COMPRESSDIR/$VERSION/META/$f";
    }
  }
  &splitBigFiles($BIGLIMIT, "$COMPRESSDIR/$VERSION/META");
  &gzip("$COMPRESSDIR/$VERSION/META");
  &check_gzip("$COMPRESSDIR/$VERSION/META");

  if ($TIMESTAMP) {
    &logger("Setting timestamp and permissions in $COMPRESSDIR/$VERSION");
    &settimestamp("$COMPRESSDIR/$VERSION", $TIMESTAMP);
  }
  &make_zip("$VERSION/META", $COMPRESSDIR, "$DVDIMAGEDIR/$metanlmfile", '-0');
  rmtree($COMPRESSDIR, 0, 0);

# make remaining NLM files
  $n=1;
  foreach $r (@metanlm) {
    $n++;
    $metanlmfile = join("-", $version, $n, "meta.nlm");
    mkpath("$COMPRESSDIR/$VERSION/META", 0, 0755);
    foreach $f (@$r) {
      system "/bin/cp $EXPANDEDDIR/$VERSION/META/$f $COMPRESSDIR/$VERSION/META/." ||
	&loganddie("ERROR: Cannot copy from $EXPANDEDDIR/$VERSION/META/$f to $COMPRESSDIR/$VERSION/META");
    }
    &splitBigFiles($BIGLIMIT, "$COMPRESSDIR/$VERSION/META");
    &gzip("$COMPRESSDIR/$VERSION/META");
    &check_gzip("$COMPRESSDIR/$VERSION/META");
    if ($TIMESTAMP) {
      &logger("Setting timestamp and permissions in $COMPRESSDIR/$VERSION");
      &settimestamp("$COMPRESSDIR/$VERSION", $TIMESTAMP);
    }
    &make_zip("$VERSION/META", $COMPRESSDIR, "$DVDIMAGEDIR/$metanlmfile", '-0');
    rmtree($COMPRESSDIR, 0, 0);
  }

# Package other knowledge sources
  opendir(D, "$EXPANDEDDIR/$VERSION") || &loganddie("ERROR: Cannot open $EXPANDEDDIR/$VERSION");
  foreach $d (readdir(D)) {
    next if (($d eq ".") || ($d eq ".."));
    next if $d eq "META";
    push @z, "$VERSION/$d";
  }
  closedir(D);

  &make_zip(\@z, $EXPANDEDDIR, "$DVDIMAGEDIR/$OTHERNLMFILE", '-0');

  &unpack_mmsys($MASTERDIR, $DVDIMAGEDIR);
  &copy_master_override_files($MASTERDIR, $DVDIMAGEDIR);

  &make_autorun($DVDIMAGEDIR);
  &make_welcomezip($MASTERDIR, $DVDIMAGEDIR);
  &compute_signatures($DVDIMAGEDIR);

# Documentation
  if (-e "$MASTERDIR/$DOCZIP") {
# ensure that the top directory in the ZIP is $DOCZIP
    open(T, "$progpath{unzip} -l $MASTERDIR/$DOCZIP") || &loganddie("ERROR: Cannot unzip $DOCZIP");
    while (<T>) {
      chomp;
      s/^\s*//;
      @x = split /\s+/, $_;
      next unless $x[0] =~ /^\d+/;
      next unless @x == 4;
      &loganddie("ERROR: $MASTERDIR/$DOCZIP does not have correct directory structure") unless $x[3] =~ m!^$DOCFILENAME/!;
    }
    close(T);

    my($cmd) = <<"EOD";
$progpath{unzip} -o -d $DVDIMAGEDIR $MASTERDIR/$DOCZIP
EOD
    $status = &runcmd($cmd);
#    &loganddie("ERROR: failed to unzip $DOCZIP (status: $status)") if ($status & 0x7F)>0;
    &loganddie("ERROR: failed to unzip $DOCZIP (status: $status)") if ($status >> 8)>0;

    my($t);
#   $t = rezip("$MASTERDIR/$DOCZIP");
    $t = "$MASTERDIR/$DOCZIP";
    copy($t, "$DVDIMAGEDIR/$DOCZIP") || &loganddie("ERROR: Failed to copy $DOCZIP to $DVDIMAGEDIR");
#    unlink $t;
  }

  if ($TIMESTAMP) {
    &logger("Setting timestamp and permissions in $DVDIMAGEDIR");
    &settimestamp($DVDIMAGEDIR, $TIMESTAMP);
  }
#  &make_zip($DVDIMAGE, $TOP, "$TOP/$DVDIMAGE.zip", '-0');
#  &make_zip($DVDIMAGE, $TOP, "$TOP/$DVDIMAGE.zip");
  &make_tar($DVDIMAGE, $TOP, "$TOP/$DVDIMAGE.tar");

  &logger("\nDone make_dvdimage");
  return;
}

# makes the mini-meta image
sub make_miniimage {
  my(@z, $d);
  my($r, $f);

  &logger(&header("Starting make_miniimage: " . $miniref->{name} . " at: " . &now), "skiptime");
  my($minimetadir) = join('/', $MINITOP, $miniref->{name} . "/META");
  my($miniimagedir) = join('/', $MINITOP, $miniref->{name}, $miniref->{name} . "_" . "IMAGE");
  
  rmtree($miniimagedir, 0, 0);
  mkpath($miniimagedir, 0, 0755);

  @z = ();
  rmtree($COMPRESSDIR, 0, 0);
  mkpath("$COMPRESSDIR/$VERSION", 0, 0755);
  opendir(D, "$MASTERDIR/$VERSION") || &loganddie("ERROR: Cannot open $MASTERDIR/$VERSION");
  foreach $d (readdir(D)) {
    next if (($d eq ".") || ($d eq ".."));
    next if $d eq "META";
    next unless -d "$MASTERDIR/$VERSION/$d";
    push @z, "$COMPRESSDIR/$VERSION/$d";
  }
  closedir(D);
  mkpath(\@z, 0, 0755);
  &make_zip($VERSION, $COMPRESSDIR, "$miniimagedir/$OTHERNLMFILE");
  rmtree($COMPRESSDIR, 0, 0);

# Make first NLM file
  $n=1;
  $metanlmfile = join("-", $version, $n, "meta.nlm");
  mkpath("$COMPRESSDIR/$VERSION", 0, 0755);
  system "/bin/cp -r $minimetadir $COMPRESSDIR/$VERSION/META" ||
    &loganddie("ERROR: Cannot copy from $minimetadir to $COMPRESSDIR/$VERSION/META");
  foreach $r (@metanlm) {
    foreach $f (@$r) {
      unlink "$COMPRESSDIR/$VERSION/META/$f";
    }
  }
  &splitBigFiles($SMALLLIMIT, "$COMPRESSDIR/$VERSION/META");
  &gzip("$COMPRESSDIR/$VERSION/META");
  &make_zip("$VERSION/META", $COMPRESSDIR, "$miniimagedir/$metanlmfile");
  rmtree($COMPRESSDIR, 0, 0);

# make remaining NLM files
  $n=1;
  foreach $r (@metanlm) {
    $n++;
    $metanlmfile = join("-", $version, $n, "meta.nlm");
    mkpath("$COMPRESSDIR/$VERSION/META", 0, 0755);
    foreach $f (@$r) {
      system "/bin/cp $minimetadir/$f $COMPRESSDIR/$VERSION/META/." ||
	&loganddie("ERROR: Cannot copy from $minimetadir/$f to $COMPRESSDIR/$VERSION/META");
    }
    &splitBigFiles($SMALLLIMIT, "$COMPRESSDIR/$VERSION/META");
    &gzip("$COMPRESSDIR/$VERSION/META");
    &make_zip("$VERSION/META", $COMPRESSDIR, "$miniimagedir/$metanlmfile");
    rmtree($COMPRESSDIR, 0, 0);
  }

  &unpack_mmsys(join('/', $MINITOP, $miniref->{name}), $miniimagedir);
  &copy_master_override_files(join('/', $MINITOP, $miniref->{name}), $miniimagedir);

  if (-e join('/', $MINITOP, $miniref->{name}, $DOCZIP)) {
    copy(join('/', $MINITOP, $miniref->{name}, $DOCZIP), "$miniimagedir/$DOCZIP") || &loganddie("ERROR: Failed to copy $DOCZIP to $miniimagedir");
  }

  &make_autorun($miniimagedir);
  &make_welcomezip(join('/', $MINITOP, $miniref->{name}), $miniimagedir);
  &compute_signatures($miniimagedir);

  $minizip = join("/", $MINITOP, $miniref->{name}, $miniref->{name} . ".zip");
  unlink $minizip;
  &make_zip($miniref->{name} . "_IMAGE", $MINITOP . "/" . $miniref->{name}, $minizip);

  &logger("\nDone make_miniimage");
  return;
}

# makes the files for download via the KSS
sub make_kssdownload {
  my($status) = 0;
  &logger(&header("Starting make_kssdownload at: " . &now), "skiptime");

  rmtree($KSSDOWNLOADDIR, 0, 0);
  mkpath($KSSDOWNLOADDIR, 0, 0755);

  opendir(D, $DVDIMAGEDIR) || &loganddie("ERROR: Cannot open $DVDIMAGEDIR");
  foreach $f (readdir(D)) {
    next unless $f =~ /^$version\-\d+\-meta\.nlm$/;
    copy("$DVDIMAGEDIR/$f", "$KSSDOWNLOADDIR/$f") ||
      &loganddie("ERROR: Cannot copy $DVDIMAGEDIR/$f to $KSSDOWNLOADDIR");
  }
  copy("$DVDIMAGEDIR/$OTHERNLMFILE", "$KSSDOWNLOADDIR/$OTHERNLMFILE") ||
    &loganddie("ERROR: Cannot copy $DVDIMAGEDIR/$OTHERNLMFILE to $KSSDOWNLOADDIR");

  &make_welcomezip($MASTERDIR, $KSSDOWNLOADDIR);
  &compute_signatures($KSSDOWNLOADDIR);

  if (-e "$MASTERDIR/$DOCZIP") {
    copy("$MASTERDIR/$DOCZIP", "$KSSDOWNLOADDIR/$DOCZIP") || &loganddie("ERROR: Failed to copy $DOCZIP to $KSSDOWNLOADDIR");
  }

  if ($TIMESTAMP) {
    &logger("Setting timestamp and permissions in $KSSDOWNLOADDIR");
    &settimestamp($KSSDOWNLOADDIR, $TIMESTAMP);
  }
  &logger("\nDone make_kssdownload");
  return;
}

# makes the WELCOME.ZIP file
sub make_welcomezip {
  my($from, $to) = @_;

  &logger("Making $to/$WELCOMEZIP");

  return unless -e $to;
  rmtree("$to/$WELCOMEZIP", 0, 0);

  rmtree($COMPRESSDIR, 0, 0);
  mkpath($COMPRESSDIR, 0, 0755);
  &unpack_mmsys($from, $COMPRESSDIR);
  &copy_master_override_files($from, $COMPRESSDIR);
#  &copy_signature_files($to, $COMPRESSDIR);
  &make_autorun($COMPRESSDIR);
  if ($TIMESTAMP) {
    &settimestamp($COMPRESSDIR, $TIMESTAMP);
  }
  &make_zip(".", $COMPRESSDIR, "$to/$WELCOMEZIP");

  rmtree($COMPRESSDIR, 0, 0);

  &logger("Done making $to/$WELCOMEZIP");
  return;
}

# splits large files in the directory
sub splitBigFiles {
  my($limit, $d) = @_;
  my(@d);
  my($cmd);
  my($p);

  if (ref($d) && ref($d) eq "ARRAY") {
    foreach (@{ $d }) {
      push @d, $_;
    }
  } elsif (ref($d) && ref($d) eq "HASH") {
    foreach (keys %{ $d }) {
      push @d, $_;
    }
  } elsif (!ref($d)) {
    foreach (@_) {
      push @d, $_;
    }
  }

  foreach $d (@d) {
    if (-f $d) {
      my(@s) = stat($d);
      $size = $s[7];

      if ($size > $limit) {
# estimate bytes per line
#	$cmd = "/bin/head -5000 $d | $progpath{wc} -c|$progpath{awk} '{print \$1}'";
#	open(C, "$cmd|") || &loganddie("Cannot open pipe to wc");
#	$_ = <C>;
#	chomp;
#	close(C);
#	$bytesperline = int $_/5000;
#	if ($bytesperline > 0) {
#	  my($prefix) = $d . ".";
#	  $lines = int $limit/$bytesperline;
#	  &logger("Splitting file $d every $lines lines");
#	  $cmd = "$progpath{split} -l $lines $d $prefix";
#	  &runcmd($cmd);
#	  unlink $d;
#	}
	&logger("Splitting $d every $limit bytes");
	&split($d, $limit);
      }

    } elsif (-d $d) {
      my($a, @a);
      opendir(D, $d) || &loganddie("ERROR: Cannot open $d");
      @a = readdir(D);
      close(D);
      &splitBigFiles($limit, map { "$d/$_" } grep { $_ ne "." && $_ ne ".." } @a);
    }
  }
  return;
}

# faster, byte-based implementation of /bin/split
sub split {
  my($file, $limit) = @_;
  my($splitfile);
  my($suffix);
  my($remaining);
  my($start);

  my($chunksize) = $limit || 500*1024*1024;
  @_ = stat($file);
  $remaining = $_[7];
  $start=0;

  while ($remaining >= $chunksize) {
    $suffix = &nextsuffix($suffix);
    &loganddie("ERROR: Too many split files for $file\n") unless defined($suffix);
    $splitfile = join('.', $file, $suffix);
    &fastcopy($file, $splitfile, $start, $chunksize);
    $start += $chunksize;
    $remaining -= $chunksize;
  }
  if ($remaining > 0) {
    $suffix = &nextsuffix($suffix);
    &loganddie("ERROR: Too many split files for $file\n") unless defined($suffix);
    $splitfile = join('.', $file, $suffix);
    &fastcopy($file, $splitfile, $start, $remaining);
  }
  unlink $file;
}

# Copies from first file to second file a range of bytes starting at an offset
sub fastcopy {
  my($fromfile, $tofile, $offset, $remaining) = @_;
  my($buf, $bufsize, $bytesAlreadyRead);

  $bufsize = 1024*1024;

  open(FROM, $fromfile) || &loganddie("ERROR: Cannot open $fromfile");
  open(TO, ">$tofile") || &loganddie("ERROR: Cannot open $tofile");;

  seek FROM, $offset, SEEK_SET if $offset > 0;
  $bytesAlreadyRead = 0;

  while ($remaining >= $bufsize) {
    if (read(FROM, $buf, $bufsize) > 0) {
      print TO $buf;
    } else {
      &loganddie("ERROR: Cannot read buffer for file: $fromfile\n");
    }
    $remaining -= $bufsize;
  }
  if ($remaining > 0) {
    if (read(FROM, $buf, $remaining) > 0) {
      print TO $buf;
    } else {
      &loganddie("ERROR: Cannot read buffer for file: $fromfile\n");
    }
  }
  close(TO);
  close(FROM);
}

# returns suffix for split
sub nextsuffix {
  my($p) = @_;
  return 'aa' unless $p;
  return undef unless $p =~ /^(\w)(\w)$/;
  $a = $1;
  $b = $2;
  if ($a eq "z") {
    if ($b eq "z") {
      return undef;
    } else {
      $b = chr(ord($b)+1);
    }
  } else {
    if ($b eq "z") {
      $a = chr(ord($a)+1);
      $b = "a";
    } else {
      $b = chr(ord($b)+1);
    }
  }
  return join('', $a, $b);
}

# makes a zip file of a directory
sub make_zip {
  my($d, $cddir, $zipfile, $opt) = @_;
  my(@d, $status, $cwd);

  if (ref($d) && ref($d) eq "ARRAY") {
    foreach (@{ $d }) {
      push @d, $_;
    }
  } elsif (!ref($d)) {
    push @d, $d;
  }

  unlink $zipfile;

  $cmd = $progpath{zip} . " $opt -r $zipfile " . join(' ', @d);
  $cwd = cwd();
  chdir $cddir;
  $status = &runcmd($cmd);
#  &logger("ERROR: $cmd had status: " . $status) if ($status & 0x7F) > 0;
  &logger("ERROR: $cmd had status: " . $status) if ($status >> 8) > 0;
  chdir $cwd;
}

sub make_tar {
  my($d, $cddir, $tarfile) = @_;
  my(@d, $status, $cwd);

  if (ref($d) && ref($d) eq "ARRAY") {
    foreach (@{ $d }) {
      push @d, $_;
    }
  } elsif (!ref($d)) {
    push @d, $d;
  }

  unlink $tarfile;

  $cmd = $progpath{tar} . " cvf $tarfile -C $cddir " . join(' ', @d);
  $cwd = cwd();
  chdir $cddir;
  $status = &runcmd($cmd);
#  &logger("ERROR: $cmd had status: " . $status) if ($status & 0x7F) > 0;
  &logger("ERROR: $cmd had status: " . $status) if ($status >> 8) > 0;
  chdir $cwd;
}

# recursively calls GZIP on the arguments
sub gzip {
  my($d) = @_;
  my(@d);
  my(@cmd);
  my($p);

  if (ref($d) && ref($d) eq "ARRAY") {
    foreach (@{ $d }) {
      push @d, $_;
    }
  } elsif (ref($d) && ref($d) eq "HASH") {
    foreach (keys %{ $d }) {
      push @d, $_;
    }
  } elsif (!ref($d)) {
    foreach (@_) {
      push @d, $_;
    }
  }

  foreach $d (@d) {
    if (-f $d) {
      push @cmd, "$progpath{gzip} " . ($opt_g && ($opt_g =~ /^[1-9]$/) ? ("-" . $opt_g) : "") . " $d";
    } elsif (-d $d) {
      my($a, @a);
      opendir(D, $d) || &loganddie("ERROR: Cannot open $d");
      @a = readdir(D);
      close(D);
      &gzip(map { "$d/$_" } grep { $_ ne "." && $_ ne ".." } @a);
    }
  }

  return unless @cmd;

  if ($cpu > 1) {
    $p = new ParallelExec($cpu);
    @status = $p->run(\@cmd);
    for ($i=0; $i<@cmd; $i++) {
      $r = $status[$i];
#      if (($r->{status} & 0x7F) > 0) {
      if (($r->{status} >> 8) > 0) {
	&logger("ERROR: Command: " . $cmd[$i] . " had status: " . $r->{status});
      } else {
	&logger($cmd[$i] . " completed in " . &hms($r->{runtime}));
      }
    }
  } else {
    foreach $cmd (@cmd) {
      $t = time;
      $status = &runcmd($cmd);
#      &logger("ERROR: Command: " . $cmd . " had status: " . $status) if ($status & 0x7F) > 0;
      &logger("ERROR: Command: " . $cmd . " had status: " . $status) if ($status >> 8) > 0;
      &logger($cmd . " completed in " . &hms(time-$t));
    }
  }
}

# recursively calls GZIP on the arguments
sub check_gzip {
  my($d) = @_;
  my(@d);
  my(@cmd);
  my($p);

  if (ref($d) && ref($d) eq "ARRAY") {
    foreach (@{ $d }) {
      push @d, $_;
    }
  } elsif (ref($d) && ref($d) eq "HASH") {
    foreach (keys %{ $d }) {
      push @d, $_;
    }
  } elsif (!ref($d)) {
    foreach (@_) {
      push @d, $_;
    }
  }

  foreach $d (@d) {
    if (-f $d) {
      push @cmd, "$progpath{gzip} -t $d";
    } elsif (-d $d) {
      my($a, @a);
      opendir(D, $d) || &loganddie("ERROR: Cannot open $d");
      @a = readdir(D);
      close(D);
      &check_gzip(map { "$d/$_" } grep { $_ ne "." && $_ ne ".." } @a);
    }
  }

  return unless @cmd;

  if ($cpu > 1) {
    $p = new ParallelExec($cpu);
    @status = $p->run(\@cmd);
    for ($i=0; $i<@cmd; $i++) {
      $r = $status[$i];
#      if (($r->{status} & 0x7F) > 0) {
      if (($r->{status} >> 8) > 0) {
	&logger("ERROR: Command: " . $cmd[$i] . " had status: " . $r->{status});
      } else {
	&logger($cmd[$i] . " completed in " . &hms($r->{runtime}));
      }
    }
  } else {
    foreach $cmd (@cmd) {
      $t = time;
      $status = &runcmd($cmd);
#      &logger("ERROR: Command: " . $cmd . " had status: " . $status) if ($status & 0x7F) > 0;
      &logger("ERROR: Command: " . $cmd . " had status: " . $status) if ($status >> 8) > 0;
      &logger($cmd . " completed in " . &hms(time-$t));
    }
  }
}

# computes MD5 and checksums for all files in a directory
sub compute_signatures {
  my($d) = @_;
  my($p, @cmd, $cwd);
  my($chkfile, $md5file);
  my($cmd);

  $cwd = cwd();
  chdir $d || &loganddie("ERROR: Cannot chdir to $d\n");

  $chkfile = $VERSION . ".CHK";
  $md5file = $VERSION . ".MD5";

  foreach $p ($chkfile, $md5file) {
    unlink $p;
    system "/bin/touch $p";
  }

  my(@f);
  open(C, "$progpath{find} . -type file -print|") || &loganddie("ERROR: Cannot open pipe from find");
  while (<C>) {
    chomp;
    s!^./!!;
    next if ($_ eq $chkfile) || ($_ eq $md5file);
    $f = "'" . $_ . "'"; # some file names have spaces!
    push @f, $f;
  }
  
  
  foreach $f (sort @f) {
    $filename = $f;
    $filename =~ s/\'//g;
    foreach $cmd ("$progpath{cksum} $f | sed 's/ /\t/;s/ /\t/;' >> $chkfile", " { echo -n 'MD5 ($filename) = ' && $progpath{md5} $f | cut -c1-32; }  >> $md5file") {
      $status = system $cmd;
      &logger("ERROR: Command: " . $cmd . " had status: " . $status) if ($status >> 8) > 0;
    }
  }
  close(C);

#  opendir(D, ".") || &loganddie("ERROR: Cannot chdir to $d\n");
#  foreach $f (readdir D) {
#    next unless -f $f;
#    next if ($f eq $chkfile) || ($f eq $md5file);
#    next unless $f =~ /\.nlm$/;
#
#    $cmd = "$progpath{cksum} $f > $f.TMPCHK";
#    push @cmd, $cmd;
#    $cmd = "$progpath{md5} $f > $f.TMPMD5";
#    push @cmd, $cmd;
#  }
#  closedir(D);

#  if ($cpu > 1) {
#    $p = new ParallelExec($cpu);
#    @status = $p->run(\@cmd);
#    for ($i=0; $i<@cmd; $i++) {
#      $r = $status[$i];
#      if (($r->{status} >> 8) > 0) {
#	&logger("ERROR: Command: " . $cmd[$i] . " had status: " . $r->{status});
#      } else {
#	&logger($cmd[$i] . " completed in " . &hms($r->{runtime}));
#      }
#    }
#  } else {
#    foreach $cmd (@cmd) {
#      $t = time;
#      $status = system($cmd);
#      &logger("ERROR: Command: " . $cmd . " had status: " . $status) if ($status >> 8) > 0;
#      &logger($cmd . " completed in " . &hms(time-$t));
#    }
#  }

#  opendir(D, ".") || &loganddie("ERROR: Cannot chdir to $d\n");
#  foreach $f (sort readdir D) {
#    next unless -f $f;
#    next if ($f eq $chkfile) || ($f eq $md5file);
#    if ($f =~ /\.TMPCHK/) {
#      system "/bin/cat $f >> $chkfile";
#      unlink $f;
#    } elsif ($f =~ /\.TMPMD5/) {
#      system "/bin/cat $f >> $md5file";
#      unlink $f;
#    }
#  }
#  closedir(D);

  chdir $cwd || &loganddie("ERROR: Cannot chdir to: $cwd\n");
}

# copies the signature files
sub copy_signature_files {
  my($from, $to) = @_;
  my($chkfile, $md5file);

  $chkfile = $VERSION . ".CHK";
  $md5file = $VERSION . ".MD5";

  &loganddie("ERROR: $from/$chkfile not found - cannot copy\n") unless -e "$from/$chkfile" && -f "$from/$chkfile";
  &loganddie("ERROR: $from/$md5file not found - cannot copy\n") unless -e "$from/$md5file" && -f "$from/$md5file";

  copy("$from/$chkfile", "$to/$chkfile") || &loganddie("ERROR: Cannot copy $from/$chkfile to $to");
  copy("$from/$md5file", "$to/$md5file") || &loganddie("ERROR: Cannot copy $from/$md5file to $to");
}

# Checks the contents of the revision MASTER directory
# 2. Is the MD5 file in MASTER and do the contents of MASTER match these MD5's?
# 2.1 Is the timestamp of the MD5 file later than the timstamps of all files in MASTER_YYYYMMDD?
# 3. Does the contents of $MASTER match the hierarchy in MASTER?
sub check_revision_dir {
  my($current, $previous);
  my($md5);
  my($path);

  &loganddie("ERROR: Missing $MASTERDIR/$VERSION\n") unless -e "$MASTERDIR/$VERSION";

  foreach $current (`$progpath{find} $MASTERDIR/$VERSION -type f -print`) {
    chomp($current);
    $previous = $current;
    $previous =~ s/$MASTER/MASTER/;
    &loganddie("ERROR: File: $current does not exist in MASTER, though you appear to be updating it in $MASTER.") unless -e $previous;
  }

  foreach $current (`$progpath{find} $MASTERDIR/$VERSION -type f -print`) {
    chomp($current);

    my(@m) = &get_all_revision_masters;
    unshift @m, "MASTER";
    foreach (@m) {
      $previous = $current;
      $previous =~ s/$MASTER/$_/;
      next if $previous eq $current;
      next unless -e $current && -e $previous;
      next if
	$current =~ m-/MRFILES\.ISO- ||
	$current =~ m-/MRCOLS\.ISO- ||
	$current =~ m-/MRFILES\.UNIX- ||
	$current =~ m-MRCOLS\.UNIX-;

      &loganddie("ERROR: File: $current is identical to the version in $previous.") if (&file2md5($current) eq &file2md5($previous));
    }
  }

  &loganddie("ERROR: File: $MASTERDIR/md5 is missing.") unless -e "$MASTERDIR/md5";
  foreach (`$progpath{find} $MASTERDIR/$VERSION -newer "$MASTERDIR/md5" -type f -print`) {
    chomp;
    &loganddie("ERROR: File: $_ is newer than the MD5 file: $MASTERDIR/md5");
  }

  open(M, "$MASTERDIR/md5") || &loganddie("ERROR: Cannot open $MASTERDIR/md5");
  while (<M>) {
    chomp;
    next unless m/MD5 \((.*)\) = (.*)$/;
    $path = $1;
    $md5 = $2;
    &loganddie("ERROR: MD5 differs for file: $path than what is in $MASTERDIR/md5 file.") unless $md5 eq &file2md5("$MASTERDIR/$path");
  }
  close(M);
  &loganddie("ERROR: The two versions of release.dat (in $MASTERDIR and MASTER) are the same.") if (&file2md5("$MASTERDIR/$VERSION/release.dat") eq &file2md5("$TOP/MASTER/$VERSION/release.dat"));
}

# Checks the override files in the MASTER directory
sub check_master_override_files {
  my($type, $r, $f, $fe, $ext);

  foreach $f (@masterOverrideFiles) {
    &loganddie("ERROR: Cannot find $f in $MASTERDIR") unless -e "$MASTERDIR/$f";
    &logandwarn("ERROR: File $f in $MASTERDIR is empty") if -z "$MASTERDIR/$f";
    &loganddie("ERROR: Expecting UNIX line termination for $f in $MASTERDIR")
      if ((! &is_binary("$MASTERDIR/$f")) && (&iso_line_termination("$MASTERDIR/$f")));
    &loganddie("ERROR: 8-bit ASCII characters found in $f in $MASTERDIR") if (&has_8bit("$MASTERDIR/$f"));

    foreach $ext (@optionalExtensions) {
      $fe = &substitute_ext($f, $ext);

      if (-e "$MASTERDIR/$fe") {
	&loganddie("ERROR: Expecting UNIX line termination for $fe in $MASTERDIR")
	  if ((! &is_binary("$MASTERDIR/$fe")) && (&iso_line_termination("$MASTERDIR/$fe")));
	&logandwarn("ERROR: File $fe in $MASTERDIR is empty") if -z "$MASTERDIR/$fe";
	&loganddie("ERROR: 8-bit ASCII characters found in $fe in $MASTERDIR") if (&has_8bit("$MASTERDIR/$fe"));
      }
    }
  }
}

#  Copies all master override files
sub copy_master_override_files {
  my($from, $to) = @_;
  my($e, $f, $d, $f2);

  foreach $f (@masterOverrideFiles) {
    next unless -e "$from/$f";

    copy("$from/$f", "$to/$f") ||
      &loganddie("ERROR: Copy of $from/$f to $to/$f failed");

#    &append_build_date("$to/$f") if ($f eq "release.dat");

    foreach $e (@optionalExtensions) {
      $f2 = &substitute_ext($f, $e);
      next unless -e "$from/$f2";
      copy("$from/$f2", "$to/$f2") ||
	&loganddie("ERROR: Copy of $from/$f2 to $to/$f2 failed");
    }
  }
}

# applies a revision from a revision dir, e.g., $TOP/MASTER_01 to the
# destination directory.  If dosify is set, converts to DOS line termination
# along the way
sub apply_revision {
  my($revmaster, $todir, $dosify) = @_;
  my($f);

  &logger("Applying the revisions " . ($dosify ? "(after converting to DOS) " : "") . "from $revmaster to $todir");
  if ($dosify) {
    &transform_all("$revmaster/$VERSION", "$todir/$VERSION", [ 'METAMSYS' ]);
  } else {
    system "/bin/cp -p -r $revmaster/$VERSION $todir" ||
      &loganddie("ERROR: Cannot copy from $revmaster/$VERSION to $todir/$VERSION\n");
  }

# MRFILES and MRCOLS
  foreach $f (qw(MRFILES MRCOLS)) {
    my($e) = ($dosify ? "ISO" : "UNIX");
    $mf = $f . ($mrplus ? ".MRP" : "");
    if (-e "$revmaster/$VERSION/META/$f.$e") {
      &logger("Copying $MASTERDIR/$VERSION/META/$f.$e to $todir/$VERSION/META/$f");
      copy("$revmaster/$VERSION/META/$f.$e", "$todir/$VERSION/META/$mf");
    } elsif (-e "$MASTERDIR/$VERSION/META/$f") {
      &logger("Copying $f from $revmaster/$VERSION/META to $todir/$VERSION/META");
      copy("$revmaster/$VERSION/META/$f", "$todir/$VERSION/META/$mf");
    }
  }

  foreach $f (qw(MRFILES MRCOLS)) {
    unlink "$todir/$VERSION/META/$f.UNIX";
    unlink "$todir/$VERSION/META/$f.ISO";
  }

# unpack MMSYS if present
  &unpack_mmsys($revmaster, $todir);
}

# returns all revision master names in order
sub get_all_revision_masters {
  my(@masters);

  open(F, "/bin/find $TOP -name 'MASTER_[0-9][0-9]*' -print|") || &loganddie("ERROR: Cannot open find.\n");
  while (<F>) {
    chomp;
    $m = basename($_);
    push @masters, $m;
  }
  close(F);
  return sort @masters;
}

# unpacks MMSYS
sub unpack_mmsys {
  my($from, $to) = @_;
  my($status);

  return unless -e "$from/$MMSYSZIP";

  rmtree("$to/$MMSYSTOP", 0, 0);
  mkpath("$to/$MMSYSTOP", 0, 0755);

  &logger("Unpacking $from/$MMSYSZIP to $to");
  my($cmd) = <<"EOD";
$progpath{unzip} -o -d $to $from/$MMSYSZIP
EOD
  $status = &runcmd($cmd);
#  &loganddie("ERROR: failed to unzip $MMSYSZIP (status: $status)") if ($status & 0x7F)>0;
  &loganddie("ERROR: failed to unzip $MMSYSZIP (status: $status)") if ($status >> 8)>0;
  &loganddie("ERROR: Expecting a $MMSYSTOP directory in $to after unzipping $MMSYSZIP\n") unless -d "$to/$MMSYSTOP";

  &append_build_date("$to/release.dat");
}

# unzips, sets the timestamps and rezips
sub rezip {
  my($zipfile) = @_;
  my($status);
  my($copy);

  return unless -e $zipfile;

  my($tmpdir) = "$TMPDIR/rezip";
  my($unzipdir) = "$tmpdir/unzip";

  rmtree($tmpdir, 0, 0);
  mkpath($unzipdir, 0, 0775);
  $copy = "$tmpdir/t.$$.zip";

  if ($TIMESTAMP) {
    my($cmd) = $progpath{unzip} . " -o -d $unzipdir $zipfile";
    $status = &runcmd($cmd);
#    &loganddie("ERROR: failed to unzip $zipfile (status: $status)") if ($status & 0x7F)>0;
    &loganddie("ERROR: failed to unzip $zipfile (status: $status)") if ($status >> 8)>0;
    &settimestamp($unzipdir, $TIMESTAMP);
    $cmd = $progpath{zip} . " -r $copy .";
    my($cwd) = cwd();
    chdir $unzipdir;
    $status = &runcmd($cmd);
#    &logger("ERROR: $cmd had status: " . $status) if ($status & 0x7F) > 0;
    &logger("ERROR: $cmd had status: " . $status) if ($status >> 8) > 0;
    chdir $cwd;
    rmtree($unzipdir, 0, 0);
  } else {
    copy($zipfile, $copy) || &loganddie("ERROR: Failed to copy $zipfile to $copy");
  }
  return $copy;
}

# checks the Metamorphosys ZIP file for structural integrity
sub check_mmsys_zip {
  my($mmsyszip) = join("/", $MASTERDIR, $MMSYSZIP);
  my($cmd);
  my($x);

  unless (-e $mmsyszip) {
    &loganddie("$mmsyszip file is not present\n");
  }

  $cmd = "$progpath{unzip} -t $mmsyszip | $progpath{awk} '\$1==\"testing:\"' | $progpath{grep} -v \"OK\"";
  open(C, "$cmd|") || &loganddie("ERROR: Cannot unzip $mmsyszip\n");
  while (<C>) {
    $x .= $_;
  }
  close(C);
  &loganddie($x) if $x;

# Top directory in the zip should be $mmsysTOP
  $cmd = "$progpath{unzip} -l $mmsyszip | $progpath{awk} 'NR>3{print \$4}' | $progpath{awk} '\$0!=\"\"' | $progpath{awk} -F'/' 'NF>1 && \$1!=\"$MMSYSTOP\"'";
  open(C, "$cmd|") || return "ERROR: Cannot unzip $mmsyszip\n";
  while (<C>) {
    $x = "Top directory of $mmsyszip is not $MMSYSTOP\n";
    last;
  }
  close(C);
  &loganddie($x) if $x;
}

# true if file has crlf
sub iso_line_termination {
  my($file) = @_;
  open(F, $file) || return 0;
  $_ = <F>;
  close(F);
  return 1 if /\r\n/;
}

# true if file contains 8-bit chars
sub is_binary {
  my($f) = @_;

  return (
	  ($f =~ /\.pdf$/i) ||
	  ((-e $f) && (-B $f))
	 );
}

# real check for 8bit chars
sub has_8bit {
  my($f) = @_;
  my($bit8) = 0;
  my(@x);
  my($linenum);

  return 0 unless $f =~ /\.txt/i || $f =~ /\.html/i;

  open(F, $f) || &loganddie("ERROR: Cannot open $f\n");
  while (<F>) {
    $linenum++;
    chomp;
    @x = unpack("C*", $_);
    foreach (@x) {
      if ($_ > 127) {
	my($h) = sprintf("%lx", $_);
	my($o) = sprintf("%lo", $_);
	&logandwarn("ERROR: ASCII $_ (0x$h, \\0$o) found at line: $linenum in file: $f\n");
	$bit8++;
      }
    }
  }
  close(F);
  return $bit8;
}

# files that should not be in MASTER
sub suspicious_files {
  my($d) = @_;
  my(@f);
  my(@bad) = ( # these should be 'find' regexps or file names
	      '\.*',
	      '*~',
	      '\#*',
	      'foo',
	      'foo\.*',
	      't',
	     );

  my($cmd) = "$progpath{find} $d -follow \\( " . join(' -o ', map { "-name \'$_\'" } @bad) . " \\) -print";
  open(C, "$cmd|") || &loganddie("ERROR: $progpath{find} failed in suspicious_files");
  while (<C>) {
    chomp;
    push @f, $_;
  }
  close(C);
  return @f;
}

# empty files present?
sub empty_files {
  my($d) = @_;
  my(@f);
  my($cmd) = "$progpath{find} $d -follow -type f -size 0 -print";
  open(C, "$cmd|") || &loganddie("ERROR: $progpath{find} failed in empty_files");
  while (<C>) {
    chomp;
    push @f, $_;
  }
  close(C);
  return @f;
}

# strips the extension off a filename
sub substitute_ext {
  my($file, $extension) = @_;

  $file =~ /^(.*)\.[^\.]*$/;
  return join('.', $1, $extension);
}

# logging functions
# if $wheretolog is a typeglob it is a file
# if it is a reference to an array where the
# first element is "string" or "file" it is appended
# to the second element
sub logger {
  my($msg, $skiptime) = @_;
  my($out);

  chomp($msg);
  $out = ($indent ? ".." x $indent . " $msg" : $msg);
  if ($skiptime) {
    $out .= "\n";
  } else {
    $out .= " at: " . &now . "\n";
  }

  print STDERR $out if $opt_s;

  if (ref($wheretolog) eq "GLOB") {
    print $wheretolog $out;
  } elsif (ref($wheretolog) eq "ARRAY") {
    if ($wheretolog->[0] =~ /^STR/i) {
      my($strref) = $wheretolog->[1];
      $$strref .= $out;
    } elsif ($wheretolog->[0] =~ /^FILE/i) {
      my($file) = $wheretolog->[1];
      open(F, ">>$file") || &loganddie("ERROR: Cannot open $file\n");
      print F $out;
      close(F);
    }
  }

}

sub loganddie {
  my($msg) = @_;
  &logger($msg, "skiptime");
  die $msg;
}

sub logandwarn {
  my($msg) = @_;
  &logger($msg, "skiptime");
#  warn "";
}

sub runcmd {
  my($cmd, $msg) = @_;
  my($status);
  my($tmplog) = "/tmp/log.$$";

  unlink $tmplog;
  &logger($cmd || $msg);
  $cmd .= " 1>$tmplog 2>&1";
  $status = system($cmd);
  &logger(&file2str($tmplog));
  unlink $tmplog;
  return $status;
}

sub header {
  my($msg) = @_;
  my($l) = length($msg);
  my($header);
  
  $header .= "\n\n" . "+-+-" x 20 . "\n";
  $header .= $msg . "\n\n";
  return $header;
}

sub file2str {
  my($file) = @_;
  my($str);

  open(FILE, $file) || return;
  while (<FILE>) {
    $str .= $_;
  }
  close(FILE);
  return $str;
}

# makes an autorun.inf file
sub make_autorun {
  my($dir) = @_;
  open(F, "> $dir/$AUTORUNFILE") || &loganddie("ERROR: Cannot open $dir/$AUTORUNFILE");
  print F <<"EOD";
[autorun]
open=$MSWELCOME
EOD
  close(F);
}

# sets global vars: MASTER, MASTERDIR, etc.
sub set_globals {

  if ($baserelease) {

    $MASTER = "MASTER";
    $MASTERDIR = "$TOP/$MASTER";
    $EXPANDED = 'EXPANDED';
    $EXPANDEDDIR = "$TOP/$EXPANDED";
    $COMPRESS = 'COMPRESS';
    $COMPRESSDIR = "$TOP/$COMPRESS";
    $DVDIMAGE = 'DVDIMAGE';
    $DVDIMAGEDIR = "$TOP/$DVDIMAGE";
    $KSSDOWNLOAD = 'KSSDOWNLOAD';
    $KSSDOWNLOADDIR = "$TOP/$KSSDOWNLOAD";
    $MINIIMAGE = "MINIIMAGE";
    $MINIIMAGEDIR="$TOP/$MINIIMAGE";

  } else {

    if ($fullrelease) {
      $MASTER = "MASTER_FULL_$revisionIDstr";
      $MASTERDIR = "$TOP/$MASTER";
      $EXPANDED = "EXPANDED_FULL_$revisionIDstr";
      $EXPANDEDDIR = "$TOP/$EXPANDED";
      $COMPRESS = "COMPRESS_FULL_$revisionIDstr";
      $COMPRESSDIR = "$TOP/$COMPRESS";
      $DVDIMAGE = "DVDIMAGE_FULL_$revisionIDstr";
      $DVDIMAGEDIR = "$TOP/$DVDIMAGE";
      $KSSDOWNLOAD = "KSSDOWNLOAD_FULL_$revisionIDstr";
      $KSSDOWNLOADDIR = "$TOP/$KSSDOWNLOAD";
      $MINIIMAGE = "MINIIMAGE_FULL_$revisionIDstr";
      $MINIIMAGEDIR = "$TOP/$MINIIMAGE";
    } elsif ($changerelease) {
      $MASTER = "MASTER_CHANGE_$revisionIDstr";
      $MASTERDIR = "$TOP/$MASTER";
      $EXPANDED = "EXPANDED_CHANGE_$revisionIDstr";
      $EXPANDEDDIR = "$TOP/$EXPANDED";
      $COMPRESS = "COMPRESS_CHANGE_$revisionIDstr";
      $COMPRESSDIR = "$TOP/$COMPRESS";
      $DVDIMAGE = "DVDIMAGE_CHANGE_$revisionIDstr";
      $DVDIMAGEDIR = "$TOP/$DVDIMAGE";
      $KSSDOWNLOAD = "KSSDOWNLOAD_CHANGE_$revisionIDstr";
      $KSSDOWNLOADDIR = "$TOP/$KSSDOWNLOAD";
      $MINIIMAGE = "MINIIMAGE_CHANGE_$revisionIDstr";
      $MINIIMAGEDIR = "$TOP/$MINIIMAGE";
    }
  }
}

# Removes backup files from a directory
sub rmbkup {
    my($dir) = @_;
    my($f);

    foreach $f (&findall($dir, '*.BAK'),
		&findall($dir, '*.CKP'),
		&findall($dir, '#*#'),
		&findall($dir, '.*~'),
		&findall($dir, '*~')) {
      &logger("Removing backup file: $f");
      unlink $f;
    }
}

sub settimestamp {
    my($dir, $t) = @_;
    my($f);

    &logger("$prog: Setting timestamp in $dir");
    foreach $f (&findall($dir, '*'), &findall($dir, '.*')) {
	next if $f eq "..";
	system "/bin/touch -t $t \'$f\'";
    }
}

# Any old content?
sub checktimestamp {
    my($dir, $t) = @_;
    my($fminus1) = "/tmp/tminus1.$$";
    my($fplus1) = "/tmp/tplus1.$$";
    my($tminus1, $tplus1);
    my($tt);
    my($therewereerrors);

# Create two files with +1 and -1 timstamps
    $t =~ /^(\d+)/;
    $tminus1 = $1-1;
    $tplus1 = $1+1;

    unlink $fminus1, $fplus1;
    system "/bin/touch -t $tminus1 $fminus1";
    system "/bin/touch -t $tplus1 $fplus1";

# find all files older than fminus1 and newer than fplus1
    $cmd = "$progpath{find} $dir -newer $fplus1 -o \! -newer $fminus1 -print|";
    open(CMD, $cmd);
    while (<CMD>) {
	chomp;
	&logger("$prog: ERROR: Timestamp older than $t for $_");
	$therewereerrors = 1;
    }
    close(CMD);
    unlink $fplus1, $fminus1;
    &loganddie("ERROR: there were errors in checktimestamp") if $therewereerrors;
}

# Sets permissions
sub setpermissions {
    my($dir) = @_;
    my($f);

    &logger("Setting permissions in $dir");
    foreach $f (&findall($dir, '*'), &findall($dir, '.*')) {
      next if basename($f) eq ".." || $f eq $dir;
      next if $f =~ m%$mmsysTOP/.%;
      next if $f =~ m%^METASUBSET%;
      chmod 0755, $f if -d $f;
      chmod 0644, $f unless -d $f;
    }
    chmod 0755, $dir if -d $dir;
    chmod 0644, $dir unless -d $dir;
}

# Make sure permissions are OK
sub checkpermissions {
    my($dir) = @_;
    my($f);
    my(@s);
    my($therewereerrors);

    foreach $f (&findall($dir, '*'), &findall($dir, '.*')) {
	next if basename($f) eq ".." || $f eq $dir;
	next if $f =~ m%^METASUBSET%;
	next if $f =~ m%$mmsysTOP/.%;
	@s = stat($f);
	if (($s[2] & 0777) != (-d $f ? 0755 : 0644)) {
	  &logger("$prog: ERROR: Incorrect permissions for $f");
	  $therewereerrors = 1;
	}
    }
    @s = stat($dir);
    if (($s[2] & 0777) != (-d $dir ? 0755 : 0644)) {
      &logger("$prog: ERROR: Incorrect permissions for $dir");
      $therewereerrors = 1;
    }
    &loganddie("ERROR: there were errors in checkpermissions") if $therewereerrors;
}

sub archive_log {
  my($archive) = join(".", $masterlogfile, $BUILDDATE);
  copy($masterlogfile, $archive) || &loganddie("ERROR: Failed to archive: $archive");
}

sub file2md5 {
  my($f) = @_;
  &loganddie("ERROR: file: $f does not exist in file2md5\n") unless -e $f;
  my($md5) = `$progpath{md5} < $f`;
  chomp($md5);
  return $md5;
}

sub findall {
  my($dir, $file) = @_;
  my($find) = "$progpath{find} $dir -name \'$file\' -print|";
  my(@paths, $path);

  open(FIND, $find) || &loganddie("ERROR: Cannot open path to \"$find\"\n");
  while (<FIND>) {
    chomp;
    $path = $_;
    push @paths, $path;
  }
  close(FIND);
  return(@paths);
}

# saves the timestamps of the contents of the MASTER directory
sub save_master_timestamps {
  my($timestampfile) = "$TOP/log/timestamp.$BUILDDATE";
  my($cmd);
  my(@cmd);

  @cmd = ("/bin/ls -lR $MASTERDIR");
  push @cmd, "$progpath{unzip} -l $MASTERDIR/$MMSYSZIP" if -e "$MASTERDIR/$MMSYSZIP";
  push @cmd, "$progpath{unzip} -l $MASTERDIR/$DOCZIP" if -e "$MASTERDIR/$DOCZIP";

  unlink $timestampfile;
  system "/bin/touch $timestampfile";

  foreach $cmd (@cmd) {
    open(F, ">>$timestampfile") || &loganddie("ERROR: Cannot append to $timestampfile");
    print F "\n", "-" x 80, "\n", $cmd, "\n\n";
    close(F);
    system "$cmd >> $timestampfile";
  }
}

# writes the build date to the release.dat file
sub append_build_date {
  my($releasedatfile) = @_;
  my($tmpfile) = "/tmp/release.dat.$$";

  return unless basename($releasedatfile) eq "release.dat";

  unlink $tmpfile;
  open(T, ">$tmpfile") || &loganddie("ERROR: Cannot write to $tmpfile");
  open(R, $releasedatfile) || &loganddie("ERROR: Cannot access $releasedatfile");
  while (<R>) {
    $line = $_;
    chomp;
    @_ = split /=/, $_;
    next if $_[0] eq "nlm.build.date";
    print T $line;
  }
  print T "nlm.build.date=$BUILDDATE\n";
  close(T);
  close(R);
  system "/bin/mv -f $tmpfile $releasedatfile";
  # Need to write release.dat to config/<release> directory
  # Need to get release.dat file into mmsys.zip
}

# copies the directory structure
sub copydir {
  my($from, $to) = @_;
  my($fd) = gensym;
  my($d);

  opendir($fd, $from) || &loganddie("ERROR: Cannot opendir $from");
  foreach $d (readdir($fd)) {
    next unless -d "$from/$d";
    next if $d eq "." || $d eq "..";
    mkpath("$to/$d", 0, 0755);
    &copydir("$from/$d", "$to/$d");
  }
  closedir($fd);
}

# Date string for current time
sub now {
    my($now) = `/bin/date`;
    chomp($now);
    return $now;
}

# hours, min, secs
sub hms {
  my($a) = @_;
  return ($a == 0 ? "00:00:00" :
	  sprintf("%02d:%02d:%02d",
		  int($a/3600),
		  int(($a % 3600)/60),
		  int($a % 60)));
}

# parses the file $TOP/mini.config
sub load_mini_config {
  my($x) = new XML::Simple(rootname=>'miniconfig', keyattr=>'mini');
  my($z) = $x->XMLin("$TOP/mini.config");
  if (ref($z->{mini}) ne "ARRAY") {
    $z->{mini} = [ $z->{mini} ];
  }
  return $z->{mini};
}
