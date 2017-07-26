#!@PATH_TO_PERL@
#
# File:     assign_cuis.pl
# Author:   Deborah Shapiro (6/2000).
#
# Remarks
#   This script calls the assign_cuis function in MEME_OPERATIONS.sql
#   First, it gathers all concepts that have undergone a merge or a
#   split and the cuis and atoms associated with these concepts.
#   Next, it ranks all of the atoms associated with these suspect
#   concepts.  Third, it assigns cuis to concepts.  Fourth, it does
#   some QA on the assignments.
#
# Usage
#     assign_cuis.pl [-new (YES,prefix)] <dbname>
#
# Options
#   -v[ersion]:	Print version information
#   -[-]help:	On-line help
#
# Changes:
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version Info
# 
# 06/17/2005 (4.3.1): Fixes handling of -new prefix
# 12/30/2004 (4.3.0): Supports -w flag to pass work_id through
# 11/19/2003 (4.2.0): Allows CUI prefix to be passed in -new flag
# 03/18/2003 (4.1.0): Ported to MEME4
#
$release = "4";
$version = "3.1";
$version_date = "06/17/2005";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

$| = 1; # auto flush the log

#
# Parse arguments
#
$new_cui_flag = "MEME_CONSTANTS.NO";
$work_id = 0;
while (@ARGV) {
    $arg = shift(@ARGV);
    push (@ARGS, $arg) && next unless $arg =~ /^-/;

    if ($arg eq "-version") {
	$print_version="version";
    }
    elsif ($arg eq "-v") {
      $print_version="v";
    } elsif ($arg =~ /^-w=(.*)/) {
      $work_id = $1; 
    } elsif ($arg =~ /^-w/) {
      $work_id = shift(@ARGV) 
    } elsif ($arg =~ /^-new=(.*)$/) {
      $new_cui_flag = $1;  
      if ($new_cui_flag eq "YES") {
	$new_cui_flag = "MEME_CONSTANTS.YES";
      } else {
	$new_cui_flag = "'$new_cui_flag'";
      }
    } elsif ($arg =~ /^-new$/) {
      $new_cui_flag = shift(@ARGV);
      if ($new_cui_flag eq "YES") {
	$new_cui_flag = "MEME_CONSTANTS.YES";
      } else {
	$new_cui_flag = "'$new_cui_flag'";
      }
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
if ($badargs) {
} 
elsif (scalar(@ARGS) == 1) {
    ($dbname) = @ARGS;
}
elsif (scalar(@ARGS) == 2) {
    ($dbname, $opt_param) = @ARGS;
}
else {
    $badargs = 2;
    $badopt = $#ARGS+1;
}

$badargs = 3 if (!($ENV{MEME_HOME}));

#
# Print bad argument errors if any found
#
if ($badargs) {
    %errors = (1 => "Illegal switch: $badswitch",
	       2 => "Wrong number of arguments: $badopt",
	       3 => "\$MEME_HOME must be set.");
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
};

#
# This section calls the MEME_OPERATIONS function assign_cuis
#
use DBI;
use DBD::Oracle;

print "-----------------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-----------------------------------------------------------\n";

# set variables
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $dbname`;
($user,$password) = split /\//, $userpass;
chop($password);
print "Database:     $dbname\n";
print "User:         $user\n\n";

# open connection
print "    Open connection ...",scalar(localtime),"\n";
$dbh = DBI->connect("dbi:Oracle:$dbname", "$user", "$password")
   or die "Can't connect to Oracle database: $DBI::errstr\n";

# Enable buffer
&EnableBuffer;

# sort area size
$dbh->do(qq{ALTER SESSION SET sort_area_size=200000000});
$dbh->do(qq{ALTER SESSION SET hash_area_size=200000000});
$dbh->do(qq{ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS'});

# prepare statement
my $work_id = 0;
$sh = $dbh->prepare(qq{
    BEGIN
        :work_id := MEME_OPERATIONS.assign_cuis (
                    authority => MEME_CONSTANTS.MW_MAINTENANCE, 
		    work_id => $work_id,
		    table_name => MEME_CONSTANTS.EMPTY_TABLE,
		    new_cui_flag => $new_cui_flag,
		    all_flag => MEME_CONSTANTS.YES,
		    qa_flag => MEME_CONSTANTS.YES );
    END;});

# bind parameters
my $max_len = 12;
$sh->bind_param_inout(":work_id", \$work_id, $max_len);

# execute statement
print "    Call MEME_OPERATIONS.assign_cuis  ...",scalar(localtime),"\n";
$sh->execute;

# Flush the buffer
&FlushBuffer;

print "    Finished MEME_OPERATIONS.assign_cuis ...",scalar(localtime),"\n\n";

# prepare statement
my $work_id = 0;
$sh = $dbh->prepare(q{
    BEGIN
        MEME_SOURCE_PROCESSING.map_sg_data (
	            authority => MEME_CONSTANTS.MW_MAINTENANCE,
		    work_id => ?);
    END;});

# bind parameters
my $max_len = 12;
$sh->bind_param(1, $work_id);

# execute statement
print "    Call MEME_SOURCE_PROCESSING.map_sg_data  ...",scalar(localtime),"\n";
$sh->execute;

# Flush the buffer
&FlushBuffer;

# look work_id up in activity_log
if ($work_id) {
    $sh = $dbh->prepare( qq{
               SELECT activity,elapsed_time, detail FROM activity_log
	       WHERE work_id = ?
       }) || die "Can't prepare statement: $DBI::errstr";

    $sh->execute($work_id);
    print "    Work ID: $work_id\n"; 
    while (($activity, $et, $detail) = $sh->fetchrow_array) {
	print "    Activity: $activity\n";
	print "    Elapsed Time: $et seconds\n";
	print "    Detail: $detail\n";
    }
    die "Error: $sh->errstr" if $sh->err;
}

# disconnect from database
print "\n    Disconnect ...\n";
$dbh->disconnect;

print "-----------------------------------------------------------\n";
print "Finished ...",scalar(localtime),"\n";
print "-----------------------------------------------------------\n";
exit(0);

####### Local Procedures #######
sub EnableBuffer {
    my($size) = @_;
    $size = 100000 unless $size;
    $sh = $dbh->prepare(qq{
    BEGIN
	dbms_output.enable(?);
    END;});
    $sh->execute($size);
} # end EnableBuffer

sub FlushBuffer {
   #prepare stmt
    $sh = $dbh->prepare(q{
    BEGIN
	dbms_output.get_line(:line,:status);
    END;});
    #bind parms
    $sh->bind_param_inout(":line", \$line, 256);
    $sh->bind_param_inout(":status", \$status,38);

    # flush buffer
    do {
	$sh->execute;
	print "$line\n";
    } while (!$status);

} # end FlushBuffer

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
        assign_cuis.pl [-new (YES,prefix)] <dbname>
    };
}

sub PrintHelp {
    &PrintUsage;
    print qq{
This script calls the assign_cuis function in MEME_OPERATIONS
First, it gathers all concepts that have undergone a merge or a
split and the cuis and atoms associated with these concepts.
Next, it ranks all of the atoms associated with these suspect
concepts.  Third, it assigns cuis to concepts.  Fourth, it does
some QA on the assignments.

Additionally, this procedure calls MEME_SOURCE_PROCESSING.map_sg_data
which re-maps any CUI relationships that changed as a result of
reassignment.

    Options:
        -new:           Assign new CUIS (for use before produdtion)
        -v[ersion]:	Print version information
        -[-]help:	On-line help

};
    &PrintVersion("version");
    return 1;
}
