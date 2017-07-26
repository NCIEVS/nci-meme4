#!@PATH_TO_PERL@
#
# File:     validate.pl
# Author:   David Ray Hernandez (01/2001).
#
# Remarks  This script extracts information from the 
#           table mid_validate_queries in order to run 
#           a series of integrity checks on the data in 
#           the MID or MRD (the queries of this test can be 
#           found in mid_validate_queries or mrd_validation_queries)
#
# Usage
#   validate.pl [-{v,version,help,-help}] <db> [mid|mrd]
#
# Options
#   -v[ersion]:	Print version information
#   -[-]help:	On-line help
#
# Changes:
# 09/09/2006 JFW (): Add notification (*) to counts where there is an adjustment, set NLS_DATE_FORMAT
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version Information
#   4.4.0 (11/19/2004): Released
#   4.3.1 (07/15/2004): hash/sort_area_size => 2000000000
#   4.3.0 (01/20/2004): Set sort_area_size, hash_area_size
#   4.2.0 (11/03/2003): Added timestamps for each query.
#
$release = "4";
$version = "4.0";
$version_date = "11/19/2004";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

# set autoflush
$|=1;

#
# Parse arguments
#
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
if ($badargs) {
} 
elsif (scalar(@ARGS) == 2) {
    ($db,$midormrd) = @ARGS;
}
elsif (scalar(@ARGS) == 3) {
    ($db,$midormrd,$type) = @ARGS;
}
else {
    $badargs = 2;
    $badopt = $#ARGS+1;
}

$badargs = 3 if (!($ENV{ORACLE_HOME}));

#
# Print bad argument errors if any found
#
if ($badargs) {
    %errors = (1 => "Illegal switch: $badswitch",
	       2 => "Wrong number of arguments: $badopt",
	       3 => "\ORACLE_HOME must be set.");
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(0);
};

#
# 
#
use DBI;
use DBD::Oracle;

print "-----------------------------------------------------------\n";
print "Starting ...",scalar(localtime),"\n";
print "-----------------------------------------------------------\n";

# set variables
$userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $db`;
($user,$password) = split /\//, $userpass;
chop($password);
print "Database:     $db\n";
print "User:         $user\n\n";

# open connection
$dbh = DBI->connect("dbi:Oracle:$db", "$user", "$password")
   or die "Can't connect to Oracle database: $DBI::errstr\n";

$dbh->do(qq{
	    ALTER SESSION set sort_area_size=200000000
	   }) || 
  ((print "<span id=red>Error setting sort area size ($DBI::errstr).</span>")    &&  return);

$dbh->do(qq{
	    ALTER SESSION set hash_area_size=200000000
	   }) || 
  ((print "<span id=red>Error setting hash area size ($DBI::errstr).</span>")    &&  return);

$dbh->do(qq{
            ALTER SESSION SET NLS_DATE_FORMAT = 'DD-mon-YYYY HH24:MI:SS'
           }) ||
  ((print "<span id=red>Error setting NLS date format ($DBI::errstr).</span>")    &&  return);


# Enable buffer
&EnableBuffer;

# prepare and execute statement_1

if ($type) {
  $type_clause = "WHERE check_type = ?";
}

$sh = $dbh->prepare(qq{
    SELECT DISTINCT check_type 
    FROM ${midormrd}_validation_queries $type_clause
    ORDER BY check_type
}) || die "Failed to prepare statement_1:/$DBI::errstr\n";

if ($type) {
  $sh->execute($type) || die "Error executing statement_1:/$DBI::errstr\n";
} else {
  $sh->execute || die "Error executing statement_1:/$DBI::errstr\n";
}

while (($check_type)=$sh->fetchrow_array) {
    print "Checking $check_type.........", scalar(localtime),"\n\n";
 
    print "    TEST                                                   RESULTS\n";
    print "    ------------------------------------------------------ ----------\n";
 
# prepare and execute statement_2
    
    $sh2=$dbh->prepare (qq{
	SELECT query,check_name,adjustment
	FROM ${midormrd}_validation_queries
	WHERE check_type=?
	ORDER BY check_name
}) || die "Failed to prepare statement_2:/$DBI::errstr\n";

    $sh2->execute($check_type) || die "Error executing statement_2:/$DBI::errstr\n";

# prepare and execute queries

    while (($q,$d,$a)=$sh2->fetchrow_array){
	$qh=$dbh->prepare ("select count(*) from ($q)") || die "Failed to prepare query (  select count(*) from ($q)  ):/$DBI::errstr\n";
    
	$qh->execute || die "Error executing query:/$DBI::errstr\n";

	$i=0;
	# make sure queries only return one row
	while(($tct)=$qh->fetchrow_array) {
	    $i++;
	    $ct=$tct if $i == 1;
	};
	print "More than one row: $description\n" if $i>1;

	$ct=$ct-$a;

        $d = "$d                                                            ";
	$d =~ s/(.{55}).*/$1/;
        # account for adjusted counts with an asterisk
        $ast = "";
        if($a != 0) { $ast = "*"; }
	print "    $d$ct$ast   ",scalar(localtime),"\n";

    }; #end while
	
    print "\n";
    
}; # end while


# disconnect from database

#$dbh->finish;
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
        validate.pl [-{v,version,help,-help}] <db> [mid|mrd]
    };
}

sub PrintHelp {
    &PrintUsage;
    print qq{
	This script extracts information from the 
        table mid_validate_queries or
        mrd_validation_queries in order to run 
        a series of integrity checks on the data in 
        the MID (the queries of this test can be 
        found in mid_validate_queries)

    Options:
        -v[ersion]:	Print version information
        -[-]help:	On-line help

};
    &PrintVersion("version");
    return 1;
}

