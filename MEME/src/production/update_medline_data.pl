#!@PATH_TO_PERL@
#
# File:     update_medline_data.pl
# Author:   Brian Carlsen (2001).
#
# Options:
#     -db=<database>: Required
#     -v version: Version information
#     -h help:    On-line help
#
# Version Info
# 04/10/2006 TTN (1-AV6XL) :  add -mrd switch to run on MRD database
# 05/13/2002 4.1.0:  First Version
$release = "4";
$version = "1.0";
$version_date = "05/13/2002";
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
    elsif ($arg =~ /^-db=(.*)$/) {
	$db = $1;
    }
    elsif ($arg =~ /^-db$/) {
        $db = shift(@ARGV);
    }
    elsif ($arg =~ /^-release_date=(.*)$/) {
	$release_date = $1;
    }
    elsif ($arg =~ /^-start_date=(.*)$/) {
	$start_date = $1;
    }
    elsif ($arg =~ /^-mrd$/) {
	$db_mode = "-mrd";
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
$badargs = 5 if (!$db);
$badargs = 6 if (!$start_date);
$badargs = 7 if (!$release_date);

#
# Print bad argument errors if any found
#
if ($badargs) {
    %errors = (1 => "Illegal switch: $badswitch",
	       2 => "Wrong number of arguments: $badopt",
	       3 => "\$MEME_HOME must be set.",
	       5 => "Required switch -db not used.",
	       6 => "Required switch -start_date not used.",
	       7 => "Required switch -release_date not used.",
	       4 => "\$ORACLE_HOME must be set.");
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
};

$| = 1;

$update_dir="$ENV{MEDLINE_DIR}/update";
$ftp_host="ftp.nlm.nih.gov";
$ftp_dir="nlmdata/.medlease/gz";
$ftp_user="anonymous";
$ftp_pwd="meme\@apelon.com";
$parser="$ENV{MEME_HOME}/bin/medline_parser.pl";
$process="$ENV{MEME_HOME}/bin/process_medline_data.csh";
$gunzip="gunzip";
%months = (
           'Jan' => 1, 'Feb' => 2, 'Mar' => 3,
           'Apr' => 4, 'May' => 5, 'Jun' => 6,
           'Jul' => 7, 'Aug' => 8, 'Sep' => 9,
           'Oct' => 10, 'Nov' => 11, 'Dec' => 12);

use lib "$ENV{MRD_HOME}/lib";
use Net::FTP;
use DBI;

print "-----------------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-----------------------------------------------------------\n";
print "ORACLE_HOME:    $ENV{ORACLE_HOME}\n";
print "MEME_HOME:      $ENV{MEME_HOME}\n";
print "database:       $db\n";

chdir($update_dir) or die "Can't cd to $update_dir: $!\n";
die "$parser doesn't exist or is not executable\n"
  unless -f $parser && -x $parser;
die "$process doesn't exist or is not executable\n"
  unless -f $process && -x $process;

$ftp = Net::FTP->new($ftp_host);

die "FTP to $ftp_host failed: $!\n" unless $ftp;

$ftp->login($ftp_user, $ftp_pwd) || die "FTP $ftp_user failed: $!\n";

# set to binary mode
$ftp->binary;

$ftp->cwd($ftp_dir) || die "Can't cd to $ftp_dir: $!\n";

%ls = map(s/\n// && ($_ => 1),`ls`);
($month,$day,$year)=split /\//,$start_date;

foreach ( sort(grep(/\.gz$/,$ftp->ls))) {
    $file = $_;
    s/\.gz$//;
    print "    Skipping $_\n" and next if $ls{$_};
    print "    Getting $file ",scalar(localtime),"\n";
    $ftp = Net::FTP->new($ftp_host);
    die "FTP to $ftp_host failed: $!\n" unless $ftp;
    $ftp->login($ftp_user, $ftp_pwd) || die "FTP $ftp_user failed: $!\n";
    $ftp->binary;
    $ftp->cwd($ftp_dir) || die "Can't cd to $ftp_dir: $!\n";
    s/\.xml$/_stats.html/;
    $ftp->get($_) || die "Error getting $_: $!\n";
    local @ARGV = $_;
    while (<>) {
     s/.*Created:(.*)<.*$/$1/ && (@date = split /[\s\n]/) if /Created:/;
    }
    last if ($date[6] > $year);
    last if ($months{$date[2]} > $month and $date[6] == $year);
    last if ($date[3] > $day and $date[6] == $year and $months{$date[2]} == $month);
    $ftp->get($file) || die "Error getting $file: $!\n";
    $ftp->quit;
    print "    Unzipping $file\n";
    system("$gunzip -f $file") == 0 or die "$gunzip failed: $?";
    $file =~ s/\.gz$//;
    unlink glob("*.dat");
    print "    Calling $parser\n";
    system("$parser -db=$db -release_date=$release_date -u $file") == 0 or die "$parser failed: $?\n";
    print "    Calling $process\n";
    if($db_mode) {
      system("$process -mrd -u $db") == 0 or die "$process failed: $?\n";
    } else {
      system("$process -u $db") == 0 or die "$process failed: $?\n";
    }
    print "    Removing dat files ",scalar(localtime),"\n\n";
    unlink glob("*.dat");
}

$ftp->quit;

# set variables
#$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
#chop($userpass);
#($user,$password) = split /\//, $userpass;
# open connection
#$dbh = DBI->connect("dbi:Oracle:$db",$user,$password)
#  || die "Could not connect to $db: $! $?\n";

#$dbh->do("ALTER TABLE coc_headings MOVE");

#$dbh->disconnect;

print "-----------------------------------------------------------\n";
print "FINISHED ...",scalar(localtime),"\n";
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
        update_medline_data.pl [-mrd] -db=<database> -start_date=<date> -release_date=<date>
    };
}

sub PrintHelp {
    &PrintUsage;
    print qq{
   This script downloads the XML Medline files from nlm ftp server
   and processes them (in order) by calling medline_parser.pl,
   and process_medline_data.csh.  It is currently configured
   to directly interact with the MRD.

    Options:
        -mrd                    Optional switch to indicate the process
                                to run on MRD database
        -db=....                This required switch passes the database name.
	                        The database is used to look up the atom ids
			        matching the various MSH strings found in the
			        Medline files.
        -start_date=....:       Use of this switch will restrict the xml files
                                with created date is equal to or before
                                the specified date.
                                e.g. "10/01/2005"
        -release_date=....:     Use of this switch will restrict the output
	                        to Medline records whose publication date
			        is equal to or before the specified date.
                                e.g. "10/01/2005"
        -v[ersion]:	        Print version information
        -[-]help:	        On-line help
};
    &PrintVersion("version");
    return 1;
}
