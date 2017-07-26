#!@PATH_TO_PERL@
#
# File:     log_operation.pl
# Author:   Brian Carlsen (2004).
#
# This script has the following usage:
#    log_operation.pl <db> <authority> <activity> <detail> <work_id> <trans_id> <start time>
#
#       -v[ersion]  : Print version information.
#       -[-]help    : On-line help
#
# Parameters
#    db
#    authority
#    activity
#    detail
#    work_id
#    transaction_id
#    start_time (in seconds)
#
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version Information
# 09/01/2004 4.1.0: First version
#
$release = "4";
$version = "1.0";
$version_date = "09/01/2004";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set defaults
#
$end_time = time;

%badargs = (
	    1 => "Cannot use both update and init flags",
	    2 => "Invalid switch",
	    3 => "Wrong number of arguments",
	    4 => "Too many run modes specified",
	    5 => "-r should only be used with -u",
	    6 => "\$MEME_HOME and \$ORACLE_HOME must be set.");

# Check options
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
	$badargs = 2;
    }
}

#
# If necessary print help or version information
#
&PrintHelp && exit(0) if $print_help;
&PrintVersion($print_version) && exit(0) if $print_version;

#
# Check for errors
#
if (!$ENV{"ORACLE_HOME"} || !$ENV{"MEME_HOME"}) {
    $badargs = 6;
};

if ($badargs) {
} 
elsif (scalar(@ARGS) == 7) {
    ($db,$authority,$activity,$detail,$work_id,$transaction_id,$start_time) = @ARGS; 
    $elapsed_time = ($end_time - $start_time) * 1000;
}
else {
    $badargs = 3;
}

if ($badargs) {
    print "$badargs{$badargs}\n";
    &PrintUsage;
    exit(0);
}

#
# Get user
# 
$user = "-u $user" if $user;
$user=`$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl $user -d $db`;
if ($user =~ /E_Password_Error/) {
    print $user;
    exit(1);
};
chop($user);
($just_user) = split /\//, $user;

use DBI;
use DBD::Oracle;

#
# Run log operation
#
print "------------------------------------------------------------\n";
print "Starting  ...", scalar localtime,"\n";
print "------------------------------------------------------------\n";
print "DB:             $db\n";
print "Authority:      $authority\n";
print "Activity:       $activity\n";
print "Detail:         $detail\n";
print "Work ID:        $work_id\n";
print "Transaction ID: $transaction_id\n";
print "Elapsed time:   $elapsed_time\n";

#
# Open Connection
#
print "    Opening connection ...",scalar(localtime),"\n";
$dbh = DBI->connect("dbi:Oracle:$db","$user") ||
  die "Can't connect to Oracle database: $DBI::errstr\n";

if ($detail =~ /merge.pl/) {
  $sh = $dbh->prepare(qq{select ct,merge_set,status from mdba_mom}) ||
    die "Can't prepare statement: $DBI::errstr\n";
  $sh -> execute ||
    die "Can't execute statement: $DBI::errstr\n";
  $detail .= " (";
  while (($ct,$merge_set,$status) = $sh->fetchrow_array) {
    if ($found) { $detail .= ", "; }
    $found++;
    if ($status eq "M") {
      $detail .= "$ct $merge_set merges";
    }
    if ($status eq "D") {
      $detail .= "$ct $merge_set demotions";
    }
  }
  $detail .= ")";
}

print "    Log operation ...",scalar(localtime),"\n";
$sh = $dbh->prepare( 
qq{
   BEGIN
   MEME_UTILITY.log_operation (
			       authority => ?,
			       activity => ?,
			       detail => ?,
			       work_id => ?,
			       transaction_id => ?,
			       elapsed_time => ?);
   END;
  }) || die "Can't prepare statement: $DBI::errstr.\n";
	
$sh->execute($authority, $activity, $detail, $work_id, $transaction_id, $elapsed_time) || die "Error: $DBI::errstr\n";	
print "------------------------------------------------------------\n";
print "Finished ...", scalar localtime,"\n";
print "------------------------------------------------------------\n";
exit(0);

############################# Local Procedures ############################
sub PrintHelp {
    
    &PrintUsage;
    print qq{

 It is used to add an entry to the MID activity_log table.  If a
 transaction_id of 0 is supplied, a "new" one will be created.

 Options:
       -v[ersion]  : Print version information.
       -[-]help    : On-line help
};
    ($user) = split /\//, (`$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`);
    print " Default User is: $user\n ";
    
    &PrintVersion("version");
}

sub PrintVersion {
    my($type) = @_;
    print "Release $release: version $version, $version_date ($version_authority).\n" 
	if $type eq "version";

    print "$version\n" if $type eq "v";
    return 1;
}

sub PrintUsage {
    print qq{This script has the following usage:
    log_operation.pl <db> <authority> <activity> <detail> 
                     <work_id> <trans_id> <start time>
};
}
