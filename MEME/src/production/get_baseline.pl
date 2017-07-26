#!@PATH_TO_PERL@
#
# File:     get_baseline_data.pl
# Author:   Brian Carlsen (2001).
#
# Options:
#     -v version: Version information
#     -h help:    On-line help
#
# Version Info
# 05/13/2002 4.1.0:  First Version
$release = "4";
$version = "1.0";
$version_date = "12/15/2004";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

while (@ARGV) {
    $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-version") {
	$print_version="version";
    }
    elsif ($arg eq "-v") {
	$print_version="v";
    }
    elsif ($arg eq "-help" || $arg eq "--help") {
	$print_help=1;
    }
    else {
	$badargs = 1;
	$badswitch = $arg;
    }
}

#
# Print Help/Version info, exit
#
&PrintHelp && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

#
# Get arguments
#

$badargs = 3 if (!($ENV{MEME_HOME}));
$badargs = 4 if (!($ENV{ORACLE_HOME}));

#
# Print bad argument errors if any found
#
if ($badargs) {
    %errors = (1 => "Illegal switch: $badswitch",
	       2 => "Wrong number of arguments: $badopt",
	       3 => "\$MEME_HOME must be set.",
	       5 => "Required switch -db not used.",
	       4 => "\$ORACLE_HOME must be set.");
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
};

$| = 1;

$medline_dir ="$ENV{MEDLINE_DIR}";
$ftp_host="ftp.nlm.nih.gov";
$ftp_dir="nlmdata/.medleasebaseline/gz";
$ftp_user="anonymous";
$ftp_pwd="meme\@msdinc.com";
$gunzip="gunzip";

use lib "$ENV{MEME_HOME}/lib";
use Net::FTP;
use DBI;

print "-----------------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-----------------------------------------------------------\n";
print "ORACLE_HOME:    $ENV{ORACLE_HOME}\n";
print "MEME_HOME:      $ENV{MEME_HOME}\n";
print "MEDLINE_DIR:    $ENV{MEDLINE_DIR}\n";

chdir($medline_dir) or die "Can't cd to $medline_dir: $!\n";

$ftp = Net::FTP->new($ftp_host);

die "FTP to $ftp_host failed: $!\n" unless $ftp;

$ftp->login($ftp_user, $ftp_pwd) || die "FTP $ftp_user failed: $!\n";

# set to binary mode
$ftp->binary;

$ftp->cwd($ftp_dir) || die "Can't cd to $ftp_dir: $!\n";

%ls = map(s/\n// && ($_ => 1),`ls`);

foreach ( sort(grep(/\.gz$/,$ftp->ls))) {
    $file = $_;
    s/\.gz$//;
    print "    Getting $file ",scalar(localtime),"\n";
    $ftp->get($file) || die "Error getting $file: $!\n";

}

$ftp->quit;
	
$file = "medline*.xml.gz";
print "    Unzipping $file\n";
system("$gunzip -f $file") == 0 or die "$gunzip failed: $?";

print "-----------------------------------------------------------\n";
print "Finished ...",scalar(localtime),"\n";
print "-----------------------------------------------------------\n";
exit(0);

######################  LOCAL PROCEDURES ######################
#
#
sub PrintVersion {
    my($type) = @_;
    print "Release $release: version $version, ".
          "$version_date ($version_authority).\n"
          if $type eq "version";
    print "$version\n" if $type eq "v";
    return 1;
}

sub PrintUsage {

    print qq{ This script has the following usage:
        get_baseline_data.pl
    };
}

sub PrintHelp {
    &PrintUsage;
    print qq{
   This script downloads the XML Medline files from nlm ftp server

    Options:
        -v[ersion]:	Print version information
        -[-]help:	On-line help
};
    &PrintVersion("version");
    return 1;
}
