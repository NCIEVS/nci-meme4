#!@PATH_TO_PERL@
#
# File:     mthtm.pl
# Author:   Brian Carlsen
#
# This script is used to run mthtm sets.
#
# Changes:
# 02/24/2009 BAC (1-GCLNT): Some optimization, formatting fixes.
# 10/01/2007 BAC (1-FE0OS): Fix problems with MID validation (e.g. DEPRECATED ~ WBC-ACNC <117>);
# 07/03/2006 RBE (1-BLZTP): Delete empty concepts after completing the process
# 05/08/2006 BAC (1-B4JGB): Added set_preference after each batch action.
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
#
# Version Information
#
#  08/29/2006 SL changing the path to perl variable
# 01/20/2005 4.4.1: Much improved queries, support for -c (check only)
# 12/30/2004 4.4.0: Log operation call correctly computes elapsed time
# 05/05/2004 4.3.0: Handle cases of deleting where only one MTH/TM with
#                   a certain base string is left
# 01/08/2004 4.2.1: drop index x_wrktbl_cid
# 09/30/2003 4.2.0: Create index for the long delete, makes it faster
# 03/19/2003 4.1.0: Release
# 02/07/2003 4.1.0: First version
#

$release = "4";
$version = "4.1";
$version_date = "01/20/2005";
$version_authority="BAC";

unshift @INC,"$ENV{ENV_HOME}/bin";
require "env.pl";

#
# Set Defaults & Environment
#
unless ($ENV{MEME_HOME}) {
  $badvalue="MEME_HOME";
  $badargs=4;
}
$work_id=0;
$termgroup="MTH/TM";
$status="R";
$time = time;
$check_only = 0;

# Check options
@ARGS=();
while (@ARGV) {
  $arg = shift(@ARGV);
  if ($arg !~ /^-/) {
    push @ARGS, $arg;
    next;
  }

  if ($arg =~ /^-w=(.*)/) {
    $work_id = "$1"; } 
  elsif ($arg =~ /^-w/) {
    $work_id = shift(@ARGV) } 
  elsif ($arg =~ /^-c$/) {
    $check_only = "1"; }
  elsif ($arg =~ /^-d=(.*)/) {
    $delete = "$1"; }
  elsif ($arg =~ /^-d/) {
    $delete = shift(@ARGV) }
  elsif ($arg =~ /^-i$/) {
    $insert = 1; }
  elsif ($arg =~ /^-t=(.*)/) {
    $termgroup = "$1"; }
  elsif ($arg =~ /^-t/) {
    $termgroup = shift(@ARGV) }
  elsif ($arg =~ /^-s=(.*)/) {
    $status = "$1"; }
  elsif ($arg =~ /^-s/) {
    $status = shift(@ARGV) }
  elsif ($arg =~ /^-prop=(.*)/) {
    $prop = "-prop=$1"; }
  elsif ($arg =~ /^-prop/) {
    $prop = "-prop=".shift(@ARGV) }
  elsif ($arg =~ /^-host=(.*)/) {
    $host = "-host=$1"; }
  elsif ($arg =~ /^-host$/) {
    $host = "-host=".shift(@ARGV); }
  elsif ($arg =~ /^-port=(.*)/) {
    $port = "-port=$1"; }
  elsif ($arg =~ /^-port$/) {
    $port = "-port=".shift(@ARGV); }
  elsif ($arg eq "-version") {
    print "Version $version, $version_date ($version_authority).\n";
    exit(0); }
  elsif ($arg eq "-v") {
    print "$version\n";
    exit(0); }
  elsif ($arg eq "-help" || $arg eq "--help") {
    &PrintHelp;
    exit(0); }
  else {
    # invalid mthtm switches may
    # be valid switches for the class being called
    push @ARGS, $arg;
  }
}

#
# Get command line params
#
if ($delete && $delete ne "ALL" && $delete ne "MERGED") {
  $badargs = 5;
} elsif (scalar(@ARGS) == 2) {
  ($database, $authority) = @ARGS;
} else {
  $badargs = 3;
  $badvalue = scalar(@ARGS);
}

#
# Process errors
#
%errors = (1 => "Illegal switch: $badvalue",
           3 => "Bad number of arguments: $badvalue",
           4 => "$badvalue must be set",
	   5 => "Bad value for -d switch: $delete");

if ($badargs) {
  &PrintUsage;
  print "\nERROR $errors{$badargs}\n";
  exit(1);
}

print "------------------------------------------------------------\n";
print "Starting $0 ... ".scalar(localtime)."\n"; 
print "------------------------------------------------------------\n";
$uniq="t_$$";
print "Delete:    $delete \n";
print "Insert:    $insert \n";
print "Status:    $status \n";
print "Termgroup: $termgroup \n";
print "Work ID:   $work_id \n";
print "Host:      $host \n";
print "Port:      $port \n";
print "Database:  $database \n";
print "Authority: $authority\n";
print "uniq:      $uniq\n\n";

use DBD::Oracle;

if ($delete || $insert) {
  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $database`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$database", "$user", "$password") ||
    ((print "Error opening $database ($DBI::errstr).")
    &&  return);
  $dbh->do(qq{ALTER SESSION SET hash_area_size=400000000});
  $dbh->do(qq{ALTER SESSION SET sort_area_size=400000000});

}


#
# DELETE ALL MTH/TM
#
if ($delete eq "ALL") {

  #
  # Delete all MTH/TM atoms
  #
  print "    Delete all MTH/TM atoms ... ".scalar(localtime)."\n";
  $dbh->do("BEGIN MEME_UTILITY.drop_it('table','${uniq}_tbac'); END;") ||
    die "ERROR dropping ${uniq}_bac ($DBI::errstr).";
  $dbh->do(qq{
        CREATE table ${uniq}_tbac AS
        SELECT atom_id as row_id FROM classes
        WHERE source='MTH' AND termgroup='MTH/TM'
    }) || die "ERROR executing create 1 ($DBI::errstr).";



  $sh = $dbh->prepare(qq{
              SELECT count(*) FROM ${uniq}_tbac
    }) || die "ERROR counting cases being handled 1 ($DBI::errstr).";
  $sh->execute || 
    die "ERROR executing count 1 ($DBI::errstr).";
  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print "      Count = $row_ct\n";



  if ($row_ct > 0 && !$check_only) {
    system("$ENV{MEME_HOME}/bin/batch.pl $port $host $prop -w=$work_id -a=D -t=C ${uniq}_tbac $database $authority | /bin/sed 's/^/      /'");
    die "ERROR executing batch.pl ($? $!)" if ($? != 0);
  }


   
  $dbh->do("BEGIN MEME_UTILITY.drop_it('table','${uniq}_tbac'); END;") ||
    die "ERROR dropping ${uniq}_tbac} ($DBI::errstr).";
}


#
# DELETE ONLY REDUNDANT MTH/TM ATOMS
# 
elsif ($delete eq "MERGED") {

  print "    Delete flag is MERGED, delete all unnecessary MTH/TM atoms ... ".scalar(localtime)."\n";

 
  print "      Get ambiguous strings ... ".scalar(localtime)."\n";
  $dbh->do("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_ambiguities'); END;") ||
    die "ERROR dropping ${uniq}_ambiguities ($DBI::errstr).";
  $dbh->do(qq{
        CREATE TABLE ${uniq}_ambiguities AS
        SELECT DISTINCT LOWER(atom_name_1) as atom_name
        FROM   separated_strings_full
    }) || die "ERROR executing create 2 ($DBI::errstr).";



  print "      Get MTH/MM and MTH/TM atoms and strip names ... ".scalar(localtime)."\n";
  $dbh->do("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tmms'); END;") ||
    die "ERROR dropping ${uniq}_tmms ($DBI::errstr).";
  $dbh->do(qq{
        CREATE TABLE ${uniq}_tmms AS
        SELECT concept_id, LOWER(substr(atom_name,0,length(atom_name)-4)) as atom_name,
               termgroup, a.atom_id, lui
        FROM classes a, atoms b
        WHERE a.atom_id = b.atom_id
        AND a.source = 'MTH'
        AND a.termgroup in ('MTH/TM','MTH/MM')
	  AND atom_name like '% <_>'
	UNION
        SELECT concept_id, LOWER(substr(atom_name,0,length(atom_name)-5)) as atom_name,
               termgroup, a.atom_id, lui
        FROM classes a, atoms b
        WHERE a.atom_id = b.atom_id
        AND a.source = 'MTH'
        AND a.termgroup in ('MTH/TM','MTH/MM')
	  AND atom_name like '% <__>'
    }) || die "ERROR executing create 3 ($DBI::errstr).";



  print "      Find MTH/TMs to remove ... ".scalar(localtime)."\n";
  print "      1. Merged MTH/TMs\n";
  print "      2. MTH/TMs merged with MTH/MMs\n";
  print "      3. MTH/TMs no longer ambiguous\n";
  $dbh->do("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_todelete'); END;") ||
    die "ERROR dropping ${uniq}_todelete ($DBI::errstr).";
  $dbh->do(qq{
        CREATE TABLE ${uniq}_todelete AS
        SELECT atom_id as row_id
        FROM ${uniq}_tmms
        WHERE termgroup = 'MTH/TM'
          AND atom_id IN
            (SELECT atom_id FROM ${uniq}_tmms
	     WHERE (concept_id,atom_name) IN (
	 	SELECT concept_id,atom_name FROM ${uniq}_tmms
		WHERE termgroup = 'MTH/TM'
	 	GROUP BY concept_id,atom_name HAVING count(*)>1)
             MINUS SELECT min(atom_id)
             FROM ${uniq}_tmms
             WHERE termgroup = 'MTH/TM'
             GROUP BY concept_id, atom_name HAVING count(*)>1)
        UNION
        SELECT a.atom_id
        FROM ${uniq}_tmms a, ${uniq}_tmms b
        WHERE a.concept_id = b.concept_id
        AND a.termgroup='MTH/TM'
        AND b.termgroup='MTH/MM'
        AND a.atom_name = b.atom_name
	UNION
        SELECT atom_id as row_id
        FROM ${uniq}_tmms 
        WHERE termgroup = 'MTH/TM'
	  AND atom_name IN
	  (SELECT atom_name FROM ${uniq}_tmms
	   MINUS SELECT atom_name FROM ${uniq}_ambiguities)
    }) || die "ERROR executing create 6.2 ($DBI::errstr).";


  $sh = $dbh->prepare(qq{
              SELECT count(*) FROM ${uniq}_todelete
    }) || die "ERROR counting cases being handled 2 ($DBI::errstr).";
  $sh->execute || 
    die "ERROR executing count 2 ($DBI::errstr).";
  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print "      Count= $row_ct\n";


  if ($row_ct > 0 && !$check_only) {
    print "      Delete unnecessary MTH/[MT]M atoms ... ".scalar(localtime)."\n";
    #print "$ENV{MEME_HOME}/bin/batch.pl $port $host $prop -w=$work_id -a=D -t=C -s=t ${uniq}_todelete $database $authority | /bin/sed 's/^/      /'\n";
    system("$ENV{MEME_HOME}/bin/batch.pl $port $host $prop -w=$work_id -a=D -t=C -s=t ${uniq}_todelete $database $authority | /bin/sed 's/^/      /'");
    die "ERROR executing batch.pl ($? $!)" if ($?);
  }

}


#
# INSERT NEW ONES
#
if ($insert) {

  #  Create ${uniq}_wrktbl which is atom_id_1, atom_id_2, 
  #      concept_id_1, concept_id_2 atom_name_1, atom_name_2 for all   
  #      pairs of ambiguous atoms only where releasable.
  print "    Get candidate pairs ... ".scalar(localtime)."\n";
  $dbh->do("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_candidate_pairs'); END;") ||
    die "ERROR dropping ${uniq}_candidate_pairs ($DBI::errstr).";
  $dbh->do(qq{
        CREATE TABLE  ${uniq}_candidate_pairs AS
        SELECT * FROM separated_strings_full
    }) || die "ERROR executing create 7 ($DBI::errstr).";

  $dbh->do(qq{
        DELETE FROM ${uniq}_candidate_pairs WHERE atom_id_1 IN
         (SELECT atom_id from classes WHERE  source='MTH' 
            AND tty in ('MM','TM'))
    }) || die "ERROR executing create 7.1($DBI::errstr).";
  $dbh->do(qq{
        DELETE FROM ${uniq}_candidate_pairs WHERE atom_id_2 IN
         (SELECT atom_id from classes WHERE  source='MTH' 
            AND tty in ('MM','TM'))
    }) || die "ERROR executing create 7.2 ($DBI::errstr).";


  print "    Find Concepts containing MM,TM atoms ... ".scalar(localtime)."\n";
  #      Create table of all bracketed MM terms.
  $dbh->do("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tmms'); END;") ||
    die "ERROR dropping ${uniq}_tmms ($DBI::errstr).";
  $dbh->do(qq{
        CREATE TABLE ${uniq}_tmms as
        SELECT c.concept_id,
               substr(a.atom_name, 0, length(a.atom_name)-4) AS stripped_str,
               to_number(substr(a.atom_name, length(a.atom_name)-1, 1)) br_num
        FROM atoms a, classes c
        WHERE termgroup in ('MTH/MM','MTH/TM')
	  AND c.source = 'MTH'
          AND a.atom_id=c.atom_id 
          AND c.tobereleased NOT IN ('n','N')
	  AND a.atom_name like '% <_>'
	UNION
        SELECT c.concept_id,
               substr(a.atom_name, 0, length(a.atom_name)-5) AS stripped_str,
               to_number(substr(a.atom_name,length(a.atom_name)-2,2)) AS br_num
        FROM atoms a, classes c
        WHERE termgroup in ('MTH/MM','MTH/TM')
	  AND c.source = 'MTH'
          AND a.atom_id=c.atom_id 
          AND c.tobereleased NOT IN ('n','N')
	  AND a.atom_name like '% <__>'
      UNION
        SELECT c.concept_id,
               substr(a.atom_name, 0, length(a.atom_name)-6) AS stripped_str,
               to_number(substr(a.atom_name,length(a.atom_name)-3,3)) AS br_num
        FROM atoms a, classes c
        WHERE termgroup in ('MTH/MM','MTH/TM')
	  AND c.source = 'MTH'
          AND a.atom_id=c.atom_id 
          AND c.tobereleased NOT IN ('n','N')
	  AND a.atom_name like '% <___>'
    }) || die "ERROR executing create 8 ($DBI::errstr).";



  print "    Find candidate $termgroup atoms ... ".scalar(localtime)."\n";
  $dbh->do("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_candidates'); END;") ||
    die "ERROR droping ${uniq}_candidates ($DBI::errstr).";
  $dbh->do(qq{
	CREATE TABLE ${uniq}_candidates AS
	SELECT concept_id_1 concept_id, atom_name_1 atom_name
	FROM ${uniq}_candidate_pairs
	WHERE (concept_id_1, upper(atom_name_1)) IN
	  (SELECT concept_id_1, upper(atom_name_1)
	   FROM ${uniq}_candidate_pairs
	   MINUS
	   SELECT concept_id, upper(stripped_str) from ${uniq}_tmms)
	UNION
	SELECT concept_id_2 concept_id, atom_name_2 atom_name
	FROM ${uniq}_candidate_pairs
	WHERE (concept_id_2, upper(atom_name_2)) IN
	  (SELECT concept_id_2, upper(atom_name_2)
	   FROM ${uniq}_candidate_pairs
	   MINUS
	   SELECT concept_id, upper(stripped_str) from ${uniq}_tmms)
    }) || die "ERROR executing create 9 ($DBI::errstr).";



  print "    Compute $termgroup strings and bracket numbers ... ".scalar(localtime)."\n";
  $dbh->do("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_new_mm'); END;") ||
    die "ERROR dropping ${uniq}_new_mm ($DBI::errstr).";
  $dbh->do(qq{
        CREATE TABLE ${uniq}_new_mm AS 
        SELECT a.concept_id,
               max(a.atom_name) as string, 
               nvl(max(b.br_num),0) AS high_mm_num,
	       upper(a.atom_name) as ambig
        FROM ${uniq}_candidates a, ${uniq}_tmms b
	WHERE upper(a.atom_name) = upper(b.stripped_str (+))
        GROUP by a.concept_id, upper(a.atom_name)
    }) || die "ERROR executing create 14 ($DBI::errstr).";



  $dbh->do("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tmms_new'); END;") ||
    die "ERROR ${uniq}_tmms_new ($DBI::errstr).";
  $dbh->do(qq{
        CREATE TABLE ${uniq}_tmms_new AS
        SELECT concept_id,a.atom_id,atom_name,termgroup,source,code,
        status,generated_status,released,tobereleased, suppressible
        FROM classes a, atoms b WHERE 1=0
    }) || die "ERROR executing create 15 ($DBI::errstr).";



  $sh = $dbh->prepare(qq{
              SELECT concept_id,string,high_mm_num,ambig
              FROM ${uniq}_new_mm
              ORDER BY 4
    }) || die "ERROR preparing query 16 ($DBI::errstr).";
  $sh->execute || 
    die "ERROR executing query 16 ($DBI::errstr).";
  while (($concept_id, $string, $high_mm_num, $ambig) = $sh->fetchrow_array){
    if (lc($string) eq $last_ambig) {
      $bc++;	
    } else {
      $bc = $high_mm_num+1;
    }
    $string = "$string <$bc>";
    #$string =~ s/'/''/g;
    $dbh->do(qq{
	  INSERT INTO ${uniq}_tmms_new 
	    (concept_id,atom_id,atom_name,termgroup,source,code,
             status,generated_status,released,tobereleased, suppressible)
 	  VALUES (?, 0, ?, ?, 'MTH', 'U',
	     ?, 'Y', 'N', 'Y', 'Y')
    }, undef, $concept_id, $string, $termgroup, $status) || die "ERROR executing insert 5 ($DBI::errstr).";
    $last_ambig = lc($ambig);
  } 


  #
  # Fix up the U codes
  # 
  $dbh->do(qq{
        UPDATE ${uniq}_tmms_new a
          SET code = 'U' || 
          LPAD((SELECT NVL(max(to_number(ltrim(b.code,'U'))),0)
        FROM classes b
        WHERE source = 'MTH'
        AND termgroup = ?)+rownum,6,0)
    }, undef, $termgroup) || die "ERROR executing update 5 ($DBI::errstr).";



  #
  # Insert the new atoms.
  # 
  $sh = $dbh->prepare(qq{
              SELECT count(*) FROM ${uniq}_tmms_new
    }) || die "ERROR counting cases being handled 3 ($DBI::errstr).";
  $sh->execute || 
    die "ERROR executing count 3 ($DBI::errstr).";
  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print "      Count = $row_ct\n";


  if ($row_ct > 0 && !$check_only) {
    system("$ENV{MEME_HOME}/bin/insert.pl $port $host $prop -w=$work_id -atoms ${uniq}_tmms_new $database $authority | /bin/sed 's/^/      /'");
    die "ERROR executing insert.pl ($? $!)" if ($? != 0);
    print "    Finished inserting bracketed string ... ".scalar(localtime)."\n";
  }

  $dbh->do(qq{
    BEGIN 
      MEME_UTILITY.drop_it('table', '${uniq}_candidate_pairs'); 
      MEME_UTILITY.drop_it('table', '${uniq}_candidates'); 
      MEME_UTILITY.drop_it('table', '${uniq}_tmms');
      MEME_UTILITY.drop_it('table', '${uniq}_tmms_new');
      MEME_UTILITY.drop_it('table', '${uniq}_new_mm');
    END;}) ||
    die "ERROR dropping tmp tables ($DBI::errstr).";
}

print "    Completed TM_Wrapper ... ".scalar(localtime)."\n";
# Log the operation
$et = (time - $time)*1000;
$dbh->do(qq{
  BEGIN
    MEME_UTILITY.log_operation (
      authority => ?, 
      activity => 'MTH/TM Management',
      detail => 'Done handling TM atoms (delete_flag=' || ? || ', insert_flag=' || ? || ', mm_termgroup= ' || ? ||')',
      transaction_id => 0, 
      work_id => ?,
      elapsed_time => ?
      );
  END;}, undef, $authority, $delete, $insert, $termgroup, $work_id, $et) 
  || die "ERROR logging operation ($DBI::errstr).";

&Handle_delete_empty_concepts;
        
print "------------------------------------------------------------\n";
print "Finished $0 ... ".scalar(localtime)."\n"; 
print "------------------------------------------------------------\n";

exit(0);

######################### LOCAL PROCEDURES #######################


##############################################################################
# Run delete empty concept patch script
sub Handle_delete_empty_concepts {
  $| = 1;

  open(L,">>MIDLogs/$log_name") || 
    ((print "Error opening $log_name.") &&
     return);

  # set variables
  $userpass = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl -d $database`;
  ($user,$password) = split /\//, $userpass;
  chop($password);
  
  # open connection
  $dbh = DBI->connect("dbi:Oracle:$database", "$user", "$password") ||
    ((print "Error opening $database ($DBI::errstr).") &&
     return);
  $dbh->do(qq{
    ALTER SESSION set sort_area_size=33445532
    }) || 
    ((print "Error setting sort area size ($DBI::errstr).")    &&  return);

  $dbh->do(qq{
    ALTER SESSION set hash_area_size=33445532
    }) || 
    ((print "Error setting hash area size ($DBI::errstr).")    &&  return);

  #
  # The following code is from $MEME_HOME/Patch/delete_empty_concept.csh converted into perl
  #
  print "    Find empty concepts ... ".scalar(localtime)."\n";
  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tbac'); END;") ||
    ((print "Error preparing drop 1 ($DBI::errstr).")
     &&  return);
  $sh->execute || 
    ((print "Error executing drop 1 ($DBI::errstr).")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_tbac AS
        SELECT DISTINCT concept_id AS row_id FROM concept_status
        MINUS SELECT concept_id FROM classes
    }) ||
    ((print "Error executing create 1 ($DBI::errstr).")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_tbac
    }) ||
    ((print "Error preparing count 1 ($DBI::errstr).")
     &&  return);
  $sh->execute ||
    ((print "Error executing count 1 ($DBI::errstr).")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print "      Count == $row_ct\n";
  # Delete concepts
  if ($row_ct > 0) {
    print "    Delete empty concepts ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'CS',
                authority => 'MTH',
                table_name => '${uniq}_tbac',
                work_id => $work_id,
                status => 'R');
            MEME_UTILITY.drop_it('table','${uniq}_tbac');			    
        END;
    }) ||
    ((print "Error preparing batch action 1 ($DBI::errstr).")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print "Error executing batch action 1 ($DBI::errstr).")
     &&  return);
    print "      Transaction_id == $transaction_id\n";
  }

  print "    Find attributes attached to empty concepts ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tbac2'); END;") ||
    ((print "Error preparing drop 11 ($DBI::errstr).")
     &&  return);
  $sh->execute || 
    ((print "Error executing drop 11 ($DBI::errstr).")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_tbac2 AS
        SELECT DISTINCT attribute_id AS row_id FROM attributes
	WHERE concept_id IN (SELECT * FROM ${uniq}_tbac )
          AND attribute_level = 'C'
    }) ||
    ((print "Error executing create 11 ($DBI::errstr).")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_tbac2
    }) ||
    ((print "Error preparing count 11 ($DBI::errstr).")
     &&  return);
  $sh->execute ||
    ((print "Error executing count 11 ($DBI::errstr).")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print "      Count == $row_ct\n";

  # Delete attributes
  if ($row_ct > 0) {
    print "    Delete attributes attached to empty concepts ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'A',
                authority => 'MTH',
                table_name => '${uniq}_tbac2',
                work_id => $work_id,
                status => 'R');
            MEME_UTILITY.drop_it('table','${uniq}_tbac2');			    
        END;
    }) ||
    ((print "Error preparing batch action 11 ($DBI::errstr).")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print "Error executing batch action 11 ($DBI::errstr).")
     &&  return);
    print "      Transaction_id == $transaction_id\n";
  }


  print "    Find relationships attached to empty concepts ... ".scalar(localtime)."\n";

  $sh = $dbh->prepare("BEGIN MEME_UTILITY.drop_it('table', '${uniq}_tbac2'); END;") ||
    ((print "Error preparing drop 12 ($DBI::errstr).")
     &&  return);
  $sh->execute || 
    ((print "Error executing drop 12 ($DBI::errstr).")
     &&  return);

  $dbh->do(qq{
        CREATE TABLE ${uniq}_tbac2 AS
        SELECT DISTINCT relationship_id AS row_id FROM relationships
	WHERE concept_id_2 IN (SELECT * FROM ${uniq}_tbac)
          AND relationship_level = 'C'
        UNION
        SELECT DISTINCT relationship_id FROM relationships
	WHERE concept_id_1 IN (SELECT * FROM ${uniq}_tbac)
          AND relationship_level = 'C'
    }) ||
    ((print "Error executing create 12 ($DBI::errstr).")
     &&  return);

  $sh = $dbh->prepare(qq{
              SELECT COUNT(*) FROM ${uniq}_tbac2
    }) ||
    ((print "Error preparing count 12 ($DBI::errstr).")
     &&  return);
  $sh->execute ||
    ((print "Error executing count 12 ($DBI::errstr).")
     &&  return);

  while (($ct) = $sh->fetchrow_array){
    $row_ct = $ct;
  }
  print "      Count == $row_ct\n";

  # Delete relationships
  if ($row_ct > 0) {
    print "    Delete relationships attached toempty concepts ... ",scalar(localtime),"\n";
    $sh = $dbh->prepare( qq{
        BEGIN
            :transaction_id := MEME_BATCH_ACTIONS.macro_action (
                action => 'D',
                id_type => 'R',
                authority => 'MTH',
                table_name => '${uniq}_tbac2',
                work_id => $work_id,
                status => 'R');
            MEME_UTILITY.drop_it('table','${uniq}_tbac2');			    
        END;
    }) ||
    ((print "Error preparing batch action 11 ($DBI::errstr).")
     &&  return);

    $sh->bind_param_inout(":transaction_id",\$transaction_id,12);
    $sh->execute || 
    ((print "Error executing batch action 11 ($DBI::errstr).")
     &&  return);
    print "      Transaction_id == $transaction_id\n";
  }


  # disconnect
  $dbh->disconnect;
  
  close(L);
  return 1;
}

sub PrintUsage {

  print qq{ This script has the following usage:
   mthtm.pl [-prop=<file>] [-host=<host>] [-port=<port>]
	    [-w=<work_id>] [-d={ALL,MERGED}] [-i] [-c]
	    [-t=<termgroup>] [-s=<status>]
            <database> <authority>
};
}

sub PrintHelp {
 &PrintUsage;
 print qq{
 This script is use to manage MTH/TM and MTH/MM atoms.

 Options:
       -prop=<file>:        Name properties file 
                            Default is $ENV{MEME_HOME}/bin/meme.prop
       -host=<host>:        Name of the machine where server is running 
                            Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-host`,qq{       -port=<port>:        The port number that the server is listening on 
                            Default is },`$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s meme-server-port`,qq{       -t:                  The termgroup to insert
       -w:                  The optional work_id
       -s:                  The status of atoms to insert
       -d:                  Delete mode (one of ALL,MERGED)
       -i:                  Flag indicating that atoms should be inserted 
       -c:                  Check counts only (do no delete/insert)
       -v[ersion]:          Print version information.
       -[-]help:            On-line help

 Arguments:
       authority:           The authority responsible for this mthtm set
       database:            The database

 Version $version, $version_date ($version_authority)
};
}
