#!@PATH_TO_PERL@

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#   old -- !/share_nfs/perl/5.8.6/bin/perl

use lib "/umls/lib/perl";


use Getopt::Std;
use XML::Simple;

# -M <names> just makes the named minis
# -f (force restart)
getopts("M:f");

$packer = "package-release.pl";
$version = "2005AB";
$TOP = "/umls/Releases/2005AB";
# 5am EST is midnight(11:59:59pm) Kiritibati, and midnight (12:00:01am) Honolulu - same day
$TIMESTAMP = "200506150500";

#die "You need to run this as user umlsadm\n" unless ($< == 3341 || $> == 3341);

$running = "/tmp/packit.$version.running";
unlink $running if $opt_f;
die "Package script is already running!\nRemove $running if you want to force a restart.\n" if (-e $running);

system "/bin/touch $running";

if ($opt_M) {
   $miniconfig = &load_mini_config;
   if ($miniconfig) {
     foreach $r (@{$miniconfig}) {
       $cmd = "$packer -d $TOP -v $version -p 1 -M " . $r->{name};
       system $cmd;
     }
  }
} else {
#  $cmd = "$packer -d $TOP -v $version -p 3 -s";
  $cmd = "$packer -d $TOP -v $version -p 3 -s -t $TIMESTAMP";
  system $cmd;

  $miniconfig = &load_mini_config;
  if ($miniconfig) {
  foreach $r (@{$miniconfig}) {
    $cmd = "$packer -d $TOP -v $version -p 1 -M " . $r->{name};
    system $cmd;
  }
  }
}
unlink $running;
exit 0;

# parses the file $TOP/mini.config
sub load_mini_config {
  my($x) = new XML::Simple(rootname=>'miniconfig', keyattr=>'mini');
  my($m) = "$TOP/mini.config";
  return "" unless -e $m;
  my($z) = $x->XMLin($m);
  if (ref($z->{mini}) ne "ARRAY") {
    $z->{mini} = [ $z->{mini} ];
  }
  return $z->{mini};
}
