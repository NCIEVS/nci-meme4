#!@PATH_TO_PERL@
#
# File:     matrixinit.pl
# Author:   Brian Carlsen (1999).
#
# Dependencies:
#    $MEME_HOME/bin/ORWeL.pl
#    $MEME_HOME/w4glapps/ORWeL.img
#
# This script has the following usage:
#    matrixinit.pl [-[UI]] [-r] [-verbose] database [date]
#
# Options:
#       -U    : Matrix Updater, instead of initializer
#       -I    : Matrix Initializer
#       -w    : work id
#       -r    : Specify run mode
#          (C)ATCHUP  : Run matrixinit on all changes since last update.
#          (TA)BLE    : Run matrixinit on concepts in a specified table. 
#          (TI)MESTAMP: Run matrixinit on all changes since specified date.
#       -v[ersion]  : Print version information.
#       -[-]help    : On-line help
#
# Parameters
#    database:  Name of the database
#    [date]:    For the Updater, the timestamp
#    [table]:   For the Updater, the specified table (-rTIMESTAMP option)
#
# Changes
# 05/03/2006 BAC (1-B2U3R): activity_log query more selective
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
$release = "4";
$version = "3.0";
$version_date = "12/30/2004";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set defaults
#
$| = 1;
$update_flag = 'N';
$timestamp = "";
$table_name = "EMPTY_TABLE";
$run_mode = "";
$editing_matrix_file = "/tmp/editing_matrix";
$verbose = "meme_integrity.set_trace_off";
$work_id = "0";

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
    elsif ($arg =~ /^-w=(.*)/) {
      $work_id = $1; }
    elsif ($arg =~ /^-w/) {
      $work_id = shift(@ARGV) }
    elsif ($arg =~ /^-U$/) {
	$update_flag = 'Y';
	$update=1; }
    elsif ($arg eq "-verbose") {
	$verbose = "meme_integrity.set_trace_on";
    }
    elsif ($arg =~ /^-I$/) {
	$init = 1; }
    elsif ($arg =~ /^-r(.*)$/) {
	$badargs = 4 if $ropt;
	$ropt = $1;
	if ($ropt =~ /^C/) {
	    $run_mode = "CATCHUP";
	}
	elsif ($ropt =~ /^TA/) {
	    $run_mode = "TABLE";
	}
	elsif ($ropt =~ /^TI/) {
	    $run_mode = "TIMESTAMP";
	}
	else {
	    $badargs = 2; last;
	}
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
elsif ($init && $update) {
    $badargs = 2;
}
elsif ($init && $ropt) {
    $badargs = 5;
}
elsif ($init && scalar(@ARGS) == 1) {
    ($db) = @ARGS; 
}
elsif ($update && scalar(@ARGS) == 2 && $ropt =~ /^TI/) {
    ($db,$timestamp) = @ARGS; 
}
elsif ($update && scalar(@ARGS) == 2 && $ropt =~ /^TA/) {
    ($db,$table_name) = @ARGS; 
}
elsif ($update && scalar(@ARGS) == 1 && $ropt =~ /^C/) {
    ($db) = @ARGS; 
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
($user,$pwd) = split /\//, $user;

use DBI;
use DBD::Oracle;

#
# Start Matrix Initializer
#
print "------------------------------------------------------------\n";
print "Starting  ...", scalar localtime,"\n";
print "------------------------------------------------------------\n";

#
# Display settings
#
if ($update_flag eq "Y") {
    print "Running Matrix Updater\n" if $update_flag;
    print "Run mode: $run_mode\n" if $run_mode;
    print "Timestamp: $timestamp\n" if $timestamp;
    print "Table Name: $table_name\n" if $table_name;
}
else {
    print "Running Matrix Initializer\n";
};
print "Database: $db\n";
print "User: $user\n";

print qq{
This PL/SQL process will not print out its log until finished.
To monitor progress, find the work_id in meme_work and look it
up in meme_progress. 

};

#
# Open Connection
#
print "    Opening connection ...",scalar(localtime),"\n";
$dbh = DBI->connect("dbi:Oracle:$db","$user","$pwd", {RaiseError=>1}) ||
    die "Can't connect to Oracle database: $DBI::errstr\n";

$dbh->do(qq{alter session set sort_area_size=33445532}) ||
    die "Can't set oracle environment: $DBI::errstr\n";

# 
# Enable buffer
#
print "    Enable output buffer.\n";
&EnableBuffer;

#
# Set NLS_DATE_FORMAT
#
print "    Set date format.\n";
$dbh->do("ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS'");

#
# Set trace mode
#
print "    Set trace mode.\n";
$dbh->do("BEGIN $verbose; END;");

#
# Call matrix updater/initializer
#
if ($update_flag eq "Y") {
    print "    Start Matrix updater...",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
	BEGIN
	    :work_id := MEME_INTEGRITY.matrix_updater (
                        run_mode => '$run_mode',
			table_name => '$table_name',
  	                timestamp => '$timestamp',
			work_id => $work_id);
        END;
    }) || die "Can't prepare statement: $DBI::errstr.\n";

} else {
    print "    Start Matrix initializer...",scalar(localtime),"\n";
    $sh = $dbh->prepare ( qq{
	BEGIN
	    :work_id := MEME_INTEGRITY.matrix_initializer( 
			  work_id => $work_id);
        END;
    }) || die "Can't prepare statement: $DBI::errstr.\n";
};

$sh->bind_param_inout(":work_id",\$work_id,12);
$sh->execute;

#
# Flush the buffer
#
&FlushBuffer;

print "    Matrix completed ...",scalar(localtime),"\n\n";

#
# Look up activity log
#
$sh = $dbh->prepare( qq{
               SELECT activity, elapsed_time, detail FROM activity_log
	       WHERE work_id = ?
	         AND timestamp > to_date(to_char(sysdate,'DD-Mon-YYYY'))
       }) || die "Can't prepare statement: $DBI::errstr";

$sh->execute($work_id);

print "    Work ID: $work_id\n"; 
while (($activity, $et, $detail) = $sh->fetchrow_array) {
  $et = int($et/1000);
    print "    Activity: $activity\n";
    print "    Elapsed Time: $et seconds\n";
    print "    Detail: $detail\n";
}
die "Error: $sh->errstr" if $sh->err;

#
# disconnect from database
#
print "\n    Disconnect ...\n";
$dbh->disconnect;
	
print "------------------------------------------------------------\n";
print "Finished ...", scalar localtime,"\n";
print "------------------------------------------------------------\n";
exit(0);

############################# Local Procedures ############################
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

sub PrintHelp {
    
    &PrintUsage;
    print qq{
 Options:
       -U    : Matrix Updater, instead of initializer
       -I    : Matrix Initializer
       -r    : Specify run mode
          (C)ATCHUP  : Run matrixinit on all changes since last update.
          (TA)BLE    : Run matrixinit on concepts in a specified table. 
          (TI)MESTAMP: Run matrixinit on all changes since specified date.

       -verbose    : Print (very) verbose output
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
    matrixinit.pl [-[UI]] [-r{CATCHUP,TABLE,TIMESTAMP}] database [date/table]
}
};
