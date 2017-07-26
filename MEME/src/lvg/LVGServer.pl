#!@PATH_TO_PERL@

# Script to start LVGServer via rc
# suresh@nlm.nih.gov 8/2002

# Options:
# -p <alternative port for the LVG server default comes from MID services>
# -f {luinorm|norm|wordind} for local access (without TCP)
# -v <alternate LVG version> the default is $LVG_HOME
# -g debug

use Getopt::Std;
use File::Basename;

getopts("p:v:f:g");

$package="gov.nih.nlm.umls.ems.lvg.";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";
$ENV{"PATH"}="$ENV{JAVA_HOME}:/bin";

# What is the current version?
$lvgdir="$ENV{LVG_HOME}";
if (-e $lvgdir) {
#  begin mod by NH 5/27/05
  $lvgversion="lvg2005";
#  $_ =  readlink $lvgdir;
#  if ($_) {
#    $lvgversion = basename($_);
#  end mod by NH 5/27/05
#  } else {
#    $lvgversion = basename($lvgdir);
#  }
} else {
  die "ERROR: directory: $lvgdir does not exist\n";
}

$java = "$ENV{JAVA_HOME}/bin/java";

$lvgdir =~ s!/$!!;
if ($lvgversion gt "lvg2002") {
  $jardir = "$lvgdir/lib";
#  $jarpath = "lib/" . $lvgversion . "api.jar";
} else {
  $jardir = "$lvgdir/classes";
#  $jarpath = "classes/lvg2002.jar";
}
opendir(D, $jardir) || die "Cannot open $jardir";
foreach $f (readdir(D)) {
  next unless -f "$jardir/$f" && $f =~ /\.jar$/i;
  push @jarpath, "$jardir/$f";
}
close(D);

die "Cannot find LVG Jar files in: $lvgdir" unless @jarpath;

#$classpath = join(":", $ENV{CLASSPATH}, "$ENV{LVG_HOME}/hsqldbserver/LVGServer.jar", $lvgdir, @jarpath);
$classpath = join(":", $ENV{CLASSPATH}, "$ENV{LVGIF_HOME}/lib/lvgif.jar", $lvgdir, @jarpath);
$ENV{'CLASSPATH'} = $classpath;

# local - not a server
if ($opt_f eq "luinorm") {
  system "$lvgdir/bin/luiNorm";
  exit 0;
} elsif ($opt_f eq "norm") {
  system "$lvgdir/bin/norm";
  exit 0;
} elsif ($opt_f eq "wordind") {
  system "$lvgdir/bin/wordInd";
  exit 0;
}

$server = "LVGServer";

unless ($opt_f) {
  $port = $opt_p || `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s "lvg-server-port"`;
  chomp($port);
} else {
  $local = $opt_f;
}

$mainclass = $package . $server;
push @c, "$java -server -classpath $classpath";
push @c, "-Dlvg-server-port=$port" if $port;
push @c, "-Dlvg-dir=$lvgdir/";
push @c, $mainclass;

$cmd = join(' ', @c);
print STDERR $cmd, "\n" if $opt_g;
exec $cmd || die "$!: $@";
exit 0;
