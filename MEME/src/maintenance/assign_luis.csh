#! /bin/csh -f
#
# Script: assign_luis.csh
# Author: Bobby Edrosa, Joanne Wong
#
# Usage:
#     assign_luis.csh [-cleanup] <database>
#
# Options:
#     <database>: Required
#     -v version: Version information
#     -h help:    On-line help
#
# Dependencies:
#     Requires LVG_HOME to be set
#     Requires MEME_HOME to be set
#     Requires ORACLE_HOME to be set
#
# Version Information:
# 10/15/2007 JFW (1-FIFPN): Use unique /tmp directory based on pid
# 10/12/2007 JFW (1-FHPA1): Adjust regex for ambiguous strings to " <[0-9]{1,3}>"
# 10/05/2007 JFW (1-FFKUL): Change rebuild_table call due to Oracle 10 bug (rebuild_flag='Y').
# 10/01/2007 PM  (1-FE03J): Remove extra backslash from the sql call to drop temporary report tables.
# 09/10/2007 JFW (1-DBSLY): Remove hard-coded references to L0028429 (null LUI)
# 11/30/2006 BAC (1-CXX0G): Use rank char(34) instead of rank char(23) and use "whenever sqlerror" clauses
# 08/31/2006 TTN (1-C261E): use the ranking algorithm from MEME_RANKS
# 08/16/2006 BAC (1-BV4YB): code rank lookups for MTH/TM,MTH/MM to guarantee result,
#                           Avoid using AWK, use perl.
# 04/24/2006 JFW (1-AYXSB): write reports on split,merge,split/merge to files
# 12/13/2004 (4.2.0): no more report table change
# 03/18/2003 (4.1.0): Ported to MEME4
# 01/17/2009  SL : Updating the RANK to use 34 character ( because of the UI changes)
#
set release=4
set version=2.0
set version_date="12/13/2004"
set version_authority="BAC"
set TMP_SPACE=.

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

#
# Check required environment variables
#
if ($?LVG_HOME == 0) then
    echo "\$LVG_HOME must be set"
    exit 1
endif
if ($?MEME_HOME == 0) then
    echo "\$MEME_HOME must be set"
    exit 1
endif
if ($?ORACLE_HOME == 0) then
    echo "\$ORACLE_HOME must be set"
    exit 1
endif

if ($?TMP_SPACE != 1) then
    echo '$TMP_SPACE must be set'
    exit 1
endif

#
# Environment variables
#

setenv PATH "/bin:/usr/bin:/usr/local/bin"
set id=$$
set AWK=awk
set CAT=cat
set FGREP=fgrep
set JOIN=join
set SED=sed
set SORT="sort -T ."
set PERL=$PATH_TO_PERL
set META_WORK=$TMP_SPACE/assignluis_$$
set NORM=$LVG_HOME/bin/luiNorm

setenv LVG_HOME_LANG AMERICAN_AMERICA.UTF8
setenv LANG en_US.UTF-8

#
# Parse arguments
#
if ($#argv == 0) then
    echo "ERROR: Bad argument"
    echo "Usage: $0 [-cleanup] <database>"
    exit 1
endif
set i=1
while ($i <= $#argv)
    switch ($argv[$i])
        case '-*help':
            echo "Usage: $0 [-cleanup] <database>"
	    cat <<EOF
  This script should be run when the version of LVG changes.
  It recomputes normalized strings in string_ui using the
  LVG luiNorm program and then recomputes the LUI assignments
  based on the changes.  The computation is necessary as a LUI
  should have a 1-1 correspondence with a norm_string value.
  To resolve conflicts, relative ranks of the atoms that have 
  the various LUIs are used.  Highest ranking atoms within a LUI
  class retain their LUIs, lower ranking ones with different
  normalized strings will get different/new LUI values.

  This script takes a long time to run and makes permanent
  changes to the string_ui, classes,
  dead_classes and word index tables.  Internally it makes
  use of add_words.csh.

EOF
            exit 0
        case '-v':
            echo $version
            exit 0
        case '-version':
            echo "Version $version, $version_date ($version_authority)"
            exit 0
        case '-cleanup'
            set cleanup=1
            breaksw
        default:
            set cleanup=0
            breaksw
    endsw
    set i=`expr $i + 1`
end

#
# Check arguments
#
if ($#argv == 1) then
    set db=$1
else if ($#argv == 2) then
    set db=$2
    set cleanup=1
else
    echo "ERROR: Bad argument"
    echo "Usage: $0 [-cleanup] <database>"
    exit 1
endif


#
# Begin program logic
#
echo "----------------------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "----------------------------------------------------------------"
echo "LVG_HOME:       $LVG_HOME"
echo "MEME_HOME:      $MEME_HOME"
echo "ORACLE_HOME:    $ORACLE_HOME"
echo "META_WORK:      $META_WORK"
echo "DATABASE:       $db"
echo ""
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
#
# 5 second delay before starting to cancel if desired.
#
foreach num (5 4 3 2 1)
    echo "Starting in $num seconds..."
    sleep 1
end

echo "    Creating the log directory $META_WORK ... `/bin/date`"
/bin/mkdir $META_WORK
#
# Cleaning old temporary tables
#
if ($cleanup == 1) then
    echo "    Dropping old temporary tables ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! sql_cleanup.$id.log
        whenever sqlerror exit 1
        set serveroutput on size 100000
        set feedback off
        exec MEME_UTILITY.drop_it('table','lui_facts');
        exec MEME_UTILITY.drop_it('table','lui_merge');
        exec MEME_UTILITY.drop_it('table','lui_split');
        exec MEME_UTILITY.drop_it('table','lui_split_merge');
        exec MEME_UTILITY.drop_it('table','tmp_lui_assignment');
        exec MEME_UTILITY.drop_it('table','wrk_table');
        exec MEME_UTILITY.drop_it('table','new_luis');
	-- keep this table around for production
        -- exec MEME_UTILITY.drop_it('table','lui_assignment');
        exec MEME_UTILITY.drop_it('table','t_string_ui');
EOF
    if ($status != 0) then
        cat sql_cleanup.$id.log
	exit 1
    endif
endif

#
# Copy string_ui to data file (sui|lui|string)
# Only copy ENG strings
#    (Add an AND sui > '?' clause if we have a previous norm file already)
#
echo "    Copying strings to $META_WORK/string.$id ... `/bin/date`"
$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select * from string_ui where language='ENG'" |\
$PERL -ne 'split /\|/; print "$_[1]|$_[0]|$_[6]\n";' >! $META_WORK/string.$id

#
# Normalize strings (sui|lui|string|norm string)
#
echo "    Normalizing strings to $META_WORK/string.norm.$id ... `/bin/date`"
$PERL -ne 'chop; ($a,$b,$c) = split /\|/; \
          if ($c =~ /(.*) <[0-9]{1,3}>$/) { $d = $1; $e = "N"; } \
          else { $d = "$c"; $e = "Y"} \
	  print join("|",($a,$b,$d)),"\n";' $META_WORK/string.$id |\
$NORM -t:3 -n |\
$SED 's/-No Output-//' >! $META_WORK/string.norm.$id

#
# Norm string count should equal string count
#
echo "    Comparing $META_WORK/string.$id to $META_WORK/string.norm.$id ... `/bin/date`"
set LineCount1=(`wc -l $META_WORK/string.$id`)
set LineCount2=(`wc -l $META_WORK/string.norm.$id`)
if ($LineCount1[1] != $LineCount2[1]) then
    echo "      ERROR: Missing data"
    echo "      $LineCount1[1], $META_WORK/string.$id"
    echo "      $LineCount2[1], $META_WORK/string.norm.$id"
    exit 1
endif

#
# Assign temporary luis to each unique norm strings
# 
echo "    Assigning unique norm string to $META_WORK/string.norm.$id ... `/bin/date`"
$PERL -ne '@fields = split /\|/; print $fields[3]."\n";' $META_WORK/string.norm.$id |\
$SORT -u | $PERL -ne 'print ++$i."|$_"' >! $META_WORK/string.tmpluis.$id

#
# Sort and Join luis to create lui_facts.dat file
#
echo "    Creating data files to $META_WORK/lui_facts.dat ... `/bin/date`"
$SORT -t\| -k 4,4 -o $META_WORK/string.norm.$id{,}
$JOIN -t\| -j1 4 -j2 2 -o 1.1 1.2 2.1 2.2 $META_WORK/string.norm.$id $META_WORK/string.tmpluis.$id >! $META_WORK/lui_facts.dat

#
# Validate luis
#
echo "    Comparing $META_WORK/string.norm.$id to $META_WORK/lui_facts.dat ... `/bin/date`"
set LineCount1=(`wc -l $META_WORK/string.norm.$id`)
set LineCount2=(`wc -l $META_WORK/lui_facts.dat`)
if ($LineCount1[1] != $LineCount2[1]) then
    echo "      ERROR: Missing data"
    echo "      $LineCount1[1], $META_WORK/string.norm.$id"
    echo "      $LineCount2[1], $META_WORK/lui_facts.dat"
    exit 1
endif

#
# Create table lui_facts
#
echo "    Creating table lui_facts ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! sql_create.$id.log
    whenever sqlerror exit 1
    -- lui_facts table limits the field rank up to 23 char.

    exec MEME_UTILITY.drop_it('table','lui_facts');
    CREATE TABLE lui_facts(
        sui           VARCHAR2(10),
        lui           VARCHAR2(10),
        lui_dash      VARCHAR2(10),
        norm_string   VARCHAR2(3000),
        rank          CHAR(34)
    );
    
    CREATE INDEX X_LUIFACTS_DASH ON LUI_FACTS
		(LUI_DASH)
			LOGGING
			NOPARALLEL;
			
    exec MEME_UTILITY.drop_it('table','lui_merge');
    CREATE TABLE lui_merge(
        lui           VARCHAR2(10)
    );
    exec MEME_UTILITY.drop_it('table','lui_split');
    CREATE TABLE lui_split(
        lui_dash      VARCHAR2(10)
    );
    exec MEME_UTILITY.drop_it('table','lui_split_merge');
    CREATE TABLE lui_split_merge(
        lui           VARCHAR2(10),
        lui_dash      VARCHAR2(10)
    );
EOF
if ($status != 0) then
    cat sql_create.$id.log
    exit 1
endif

#
# Create control files
#
echo "    Creating control files to $META_WORK/lui_facts.ctl ... `/bin/date`"
$CAT <<EOF >! $META_WORK/lui_facts.ctl
options (direct=true)
unrecoverable
load data
infile '$META_WORK/lui_facts'
badfile '$META_WORK/lui_facts.bad'
append into table lui_facts
fields terminated by '|'
trailing nullcols
(
sui          char,
lui          char,
lui_dash     char,
norm_string  char(3000)
)
EOF

#
# Load files
#
echo "    Loading lui_facts to database $db ... `/bin/date`"
$ORACLE_HOME/bin/sqlldr $user@$db control=$META_WORK/lui_facts.ctl
if ($status != 0 || `$FGREP -c 'ORA-' lui_facts.log` > 0) then
    echo "      ERROR: Error found in lui_facts.log"
    exit 1
endif

#
# Rank lui lui' pairs
#
echo "    Ranking lui lui prime pairs ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! sql_rank.$id.log 
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
                                      
    alter session set hash_area_size=268435456;

    DECLARE 
        mm_rank        NUMBER;
        tm_rank        NUMBER;
        location       VARCHAR2(10);
        rank_luis_exc  EXCEPTION;

    BEGIN

	-- MTH/MMs have 0 rank for this purpose
        location := '10';
        SELECT nvl(min(rank),0) INTO mm_rank
        FROM termgroup_rank
        WHERE termgroup = 'MTH/MM';
        SELECT nvl(min(rank),0) INTO tm_rank
        FROM termgroup_rank
        WHERE termgroup = 'MTH/TM';

        location := '20';
        UPDATE termgroup_rank
        SET rank = 0
        WHERE termgroup in  ('MTH/MM','MTH/TM');
        COMMIT;

        location := '30';
        UPDATE LUI_FACTS A
           SET RANK =
           (  SELECT /*+ USE_MERGE(TBR,C,B) */
                   MAX (MEME_RANKS.GET_ATOM_EDITING_RANK (TBR.RANK,
                                                          C.RELEASE_RANK,
                                                          LAST_RELEASE_RANK,
                                                          B.SUI,
                                                          AUI,
                                                          ATOM_ID))
               FROM CLASSES B, TERMGROUP_RANK C, TOBERELEASED_RANK TBR
              WHERE     B.SUI = A.SUI
                    AND B.TERMGROUP = C.TERMGROUP
                    AND B.TOBERELEASED = TBR.TOBERELEASED
           GROUP BY B.SUI)
           WHERE TO_NUMBER (SUBSTR (a.sui, 2)) < 2000000;
        COMMIT;
        
        location := '31';
        UPDATE LUI_FACTS A
           SET RANK =
           (  SELECT /*+ USE_MERGE(TBR,C,B) */
                   MAX (MEME_RANKS.GET_ATOM_EDITING_RANK (TBR.RANK,
                                                          C.RELEASE_RANK,
                                                          LAST_RELEASE_RANK,
                                                          B.SUI,
                                                          AUI,
                                                          ATOM_ID))
               FROM CLASSES B, TERMGROUP_RANK C, TOBERELEASED_RANK TBR
              WHERE     B.SUI = A.SUI
                    AND B.TERMGROUP = C.TERMGROUP
                    AND B.TOBERELEASED = TBR.TOBERELEASED
           GROUP BY B.SUI)
           WHERE TO_NUMBER (SUBSTR (a.sui, 2)) >= 2000000 
           and TO_NUMBER (SUBSTR (a.sui, 2)) < 4000000;
        COMMIT;

        location := '32';
        UPDATE LUI_FACTS A
           SET RANK =
           (  SELECT /*+ USE_MERGE(TBR,C,B) */
                   MAX (MEME_RANKS.GET_ATOM_EDITING_RANK (TBR.RANK,
                                                          C.RELEASE_RANK,
                                                          LAST_RELEASE_RANK,
                                                          B.SUI,
                                                          AUI,
                                                          ATOM_ID))
               FROM CLASSES B, TERMGROUP_RANK C, TOBERELEASED_RANK TBR
              WHERE     B.SUI = A.SUI
                    AND B.TERMGROUP = C.TERMGROUP
                    AND B.TOBERELEASED = TBR.TOBERELEASED
           GROUP BY B.SUI)
           WHERE TO_NUMBER (SUBSTR (a.sui, 2)) >= 4000000 
           and TO_NUMBER (SUBSTR (a.sui, 2)) < 6000000;
        COMMIT;
        
        location := '33';
        UPDATE LUI_FACTS A
           SET RANK =
           (  SELECT /*+ USE_MERGE(TBR,C,B) */
                   MAX (MEME_RANKS.GET_ATOM_EDITING_RANK (TBR.RANK,
                                                          C.RELEASE_RANK,
                                                          LAST_RELEASE_RANK,
                                                          B.SUI,
                                                          AUI,
                                                          ATOM_ID))
               FROM CLASSES B, TERMGROUP_RANK C, TOBERELEASED_RANK TBR
              WHERE     B.SUI = A.SUI
                    AND B.TERMGROUP = C.TERMGROUP
                    AND B.TOBERELEASED = TBR.TOBERELEASED
           GROUP BY B.SUI)
           WHERE TO_NUMBER (SUBSTR (a.sui, 2)) >= 6000000 
           and TO_NUMBER (SUBSTR (a.sui, 2)) < 8000000;
        COMMIT;
        
        location := '34';
        UPDATE LUI_FACTS A
           SET RANK =
           (  SELECT /*+ USE_MERGE(TBR,C,B) */
                   MAX (MEME_RANKS.GET_ATOM_EDITING_RANK (TBR.RANK,
                                                          C.RELEASE_RANK,
                                                          LAST_RELEASE_RANK,
                                                          B.SUI,
                                                          AUI,
                                                          ATOM_ID))
               FROM CLASSES B, TERMGROUP_RANK C, TOBERELEASED_RANK TBR
              WHERE     B.SUI = A.SUI
                    AND B.TERMGROUP = C.TERMGROUP
                    AND B.TOBERELEASED = TBR.TOBERELEASED
           GROUP BY B.SUI)
           WHERE TO_NUMBER (SUBSTR (a.sui, 2)) >= 8000000 
           and TO_NUMBER (SUBSTR (a.sui, 2)) < 10000000;
        COMMIT;
        
        location := '35';        
       UPDATE LUI_FACTS A
           SET RANK =
           (  SELECT /*+ USE_MERGE(TBR,C,B) */
                   MAX (MEME_RANKS.GET_ATOM_EDITING_RANK (TBR.RANK,
                                                          C.RELEASE_RANK,
                                                          LAST_RELEASE_RANK,
                                                          B.SUI,
                                                          AUI,
                                                          ATOM_ID))
               FROM CLASSES B, TERMGROUP_RANK C, TOBERELEASED_RANK TBR
              WHERE     B.SUI = A.SUI
                    AND B.TERMGROUP = C.TERMGROUP
                    AND B.TOBERELEASED = TBR.TOBERELEASED
           GROUP BY B.SUI)
           WHERE TO_NUMBER (SUBSTR (a.sui, 2)) >= 10000000 
           and TO_NUMBER (SUBSTR (a.sui, 2)) < 12000000;
        COMMIT;
        
        location := '36';
      UPDATE LUI_FACTS A
           SET RANK =
           (  SELECT /*+ USE_MERGE(TBR,C,B) */
                   MAX (MEME_RANKS.GET_ATOM_EDITING_RANK (TBR.RANK,
                                                          C.RELEASE_RANK,
                                                          LAST_RELEASE_RANK,
                                                          B.SUI,
                                                          AUI,
                                                          ATOM_ID))
               FROM CLASSES B, TERMGROUP_RANK C, TOBERELEASED_RANK TBR
              WHERE     B.SUI = A.SUI
                    AND B.TERMGROUP = C.TERMGROUP
                    AND B.TOBERELEASED = TBR.TOBERELEASED
           GROUP BY B.SUI)
           WHERE TO_NUMBER (SUBSTR (a.sui, 2)) >= 12000000 
           and TO_NUMBER (SUBSTR (a.sui, 2)) < 14000000;
        COMMIT;
        
        location := '37';
        UPDATE LUI_FACTS A
           SET RANK =
           (  SELECT /*+ USE_MERGE(TBR,C,B) */
                   MAX (MEME_RANKS.GET_ATOM_EDITING_RANK (TBR.RANK,
                                                          C.RELEASE_RANK,
                                                          LAST_RELEASE_RANK,
                                                          B.SUI,
                                                          AUI,
                                                          ATOM_ID))
               FROM CLASSES B, TERMGROUP_RANK C, TOBERELEASED_RANK TBR
              WHERE     B.SUI = A.SUI
                    AND B.TERMGROUP = C.TERMGROUP
                    AND B.TOBERELEASED = TBR.TOBERELEASED
           GROUP BY B.SUI)
           WHERE TO_NUMBER (SUBSTR (a.sui, 2)) >= 14000000 ;
        COMMIT;
        

        location := '40';
        UPDATE termgroup_rank
        SET rank = mm_rank
        WHERE termgroup = 'MTH/MM';
        UPDATE termgroup_rank
        SET rank = tm_rank
        WHERE termgroup = 'MTH/TM';
        COMMIT;

        location := '50';
        UPDATE lui_facts
        SET rank = LPAD(TRIM('L' FROM lui) || LPAD(TRIM('S' FROM sui),9,0),34,0)
        WHERE rank = '00000000000000000000000'
	  OR rank is null;
        COMMIT;

    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE(
	      'SQL Error at location: '||location||'; SQLERRM: '||SQLERRM);
            ROLLBACK;
            RAISE rank_luis_exc;
    END;
/
EOF
if ($status != 0 || `$FGREP -c 'ORA-' sql_rank.$id.log` > 0) then
    echo "      ERROR: Error found in sql_rank.$id.log"
    exit 1
endif

#
# Assign lui_merge
#
echo "    Assigning lui_merge ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! LuiMerge.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    INSERT INTO lui_merge
    SELECT DISTINCT lui FROM lui_facts
    WHERE lui_dash IN 
     (SELECT lui_dash
      FROM lui_facts GROUP BY lui_dash HAVING COUNT(DISTINCT lui) > 1);

    COMMIT;
EOF
if ($status != 0 || `$FGREP -c 'ORA-' LuiMerge.$id.log` > 0) then
    echo "      ERROR: Error found in LuiMerge.$id.log"
    exit 1
endif

#
# Assign lui_split
#
echo "    Assigning lui_split ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! LuiSplit.$id.log
    set feedback off
	whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    INSERT INTO lui_split
    SELECT DISTINCT lui_dash FROM lui_facts
    WHERE lui IN 
     (SELECT lui
      FROM lui_facts GROUP BY lui HAVING COUNT(DISTINCT lui_dash) > 1);

EOF
if ($status != 0 || `$FGREP -c 'ORA-' LuiSplit.$id.log` > 0) then
    echo "      ERROR: in LuiSplit.$id.log"
    exit 1
endif

#
# Assign lui_split_merge
#
echo "    Assigning lui_split_merge ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! LuiSplitMerge.$id.log
    set feedback off
	whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    INSERT INTO lui_split_merge (lui, lui_dash)
    SELECT DISTINCT lui, lui_dash FROM lui_facts
    WHERE lui IN (SELECT DISTINCT lui FROM lui_merge)
    AND lui_dash IN (SELECT DISTINCT lui_dash FROM lui_split);

	COMMIT;
	
    exec MEME_SYSTEM.analyze('lui_split_merge');

EOF
if ($status != 0 || `$FGREP -c 'ORA-' LuiSplitMerge.$id.log` > 0) then
    echo "      ERROR: Error found in LuiSplitMerge.$id.log"
    exit 1
endif

#
# Clean lui_merge
#
echo "    Cleaning lui_merge ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! LuiMergeOnly.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    DELETE FROM lui_merge
    WHERE lui IN (SELECT lui FROM lui_split_merge);

EOF
if ($status != 0 || `$FGREP -c 'ORA-' LuiMergeOnly.$id.log` > 0) then
    echo "      ERROR: in LuiMergeOnly.$id.log"
    exit 1
endif

#
# Clean lui_split
#
echo "      Cleaning lui_split ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! LuiSplitOnly.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    DELETE FROM lui_split
    WHERE lui_dash IN (SELECT lui_dash FROM lui_split_merge);
EOF
if ($status != 0 || `$FGREP -c 'ORA-' LuiSplitOnly.$id.log` > 0) then
    echo "      ERROR: in LuiSplitOnly.$id.log"
    exit 1
endif

# Assign 1-1 cases
#
echo "    Assigning 1-1 cases ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! OneToOne.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    exec MEME_UTILITY.drop_it('table','tmp_lui_assignment');

    CREATE TABLE tmp_lui_assignment AS
    SELECT DISTINCT lui, lui_dash
    FROM lui_facts
    WHERE lui IN
     (SELECT lui FROM lui_facts GROUP BY lui HAVING COUNT(DISTINCT lui_dash)=1)
    AND lui_dash IN
     (SELECT lui_dash FROM lui_facts GROUP BY lui_dash HAVING COUNT(DISTINCT lui)=1);

EOF
if ($status != 0 || `$FGREP -c 'ORA-' OneToOne.$id.log` > 0) then
    echo "      ERROR: in OneToOne.$id.log"
    exit 1
endif

#
# Assign N-1 cases
#
echo "    Assigning N-1 cases ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! NthToOne.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    INSERT INTO tmp_lui_assignment (lui, lui_dash)
    SELECT a.lui, b.lui_dash
    FROM lui_facts a,
     (SELECT MAX(rank) AS rank, lui_dash
      FROM lui_facts c, lui_merge d
      WHERE c.lui=d.lui GROUP BY lui_dash) b
    WHERE a.rank=b.rank; 

EOF
if ($status != 0 || `$FGREP -c 'ORA-' NthToOne.$id.log` > 0) then
    echo "      ERROR: in NthToOne.$id.log"
    exit 1
endif

#
# Assign 1-N cases
#
echo "    Assigning 1-N cases ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! OneToNth.$id.log
    set feedback off
    whenever sqlerror exit 1

    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    INSERT INTO tmp_lui_assignment (lui, lui_dash)
    SELECT b.lui, a.lui_dash
    FROM lui_facts a,
     (SELECT MAX(rank) AS rank, lui
      FROM lui_facts c, lui_split d
      WHERE c.lui_dash=d.lui_dash GROUP BY lui) b
    WHERE a.rank=b.rank; 

EOF
if ($status != 0 || `$FGREP -c 'ORA-' OneToNth.$id.log` > 0) then
    echo "    ERROR: in OneToNth.$id.log"
    exit 1
endif

#
# Assign N-N cases
#
echo "    Assigning N-N cases ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! NthToNth.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    exec MEME_UTILITY.drop_it('table','wrk_table');

    CREATE TABLE wrk_table AS
    SELECT a.lui, a.lui_dash, rank
    FROM lui_facts a, lui_split_merge b
    WHERE a.lui=b.lui
    AND a.lui_dash=b.lui_dash
    AND rank IS NOT NULL;

    COMMIT;

    DECLARE
        wt_rec_count  NUMBER;
        wt_max_rank   NUMBER;
        wt_lui        VARCHAR2(10);
        wt_lui_dash   VARCHAR2(10);
        location      VARCHAR2(3);
        n_to_n_exc    EXCEPTION;

    BEGIN
        LOOP 
            location := '10';
            wt_rec_count := MEME_UTILITY.exec_count('wrk_table');
            EXIT WHEN wt_rec_count = 0;

            location := '20';
            SELECT MAX(rank) INTO wt_max_rank FROM wrk_table;

            location := '30';
            SELECT DISTINCT lui, lui_dash INTO wt_lui, wt_lui_dash
            FROM wrk_table
            WHERE rank = wt_max_rank;

            location := '40';
            INSERT INTO tmp_lui_assignment (lui, lui_dash)
            VALUES (wt_lui, wt_lui_dash);

            location := '50';
            DELETE FROM wrk_table WHERE lui = wt_lui;

            location := '60';
            DELETE FROM wrk_table WHERE lui_dash = wt_lui_dash;
        END LOOP;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('SQL Error at location: '||location||'; SQLERRM: '||SQLERRM);
            ROLLBACK;
            RAISE n_to_n_exc;
    END;
/
EOF
if ($status != 0 || `$FGREP -c 'ORA-' NthToNth.$id.log` > 0) then
    echo "      ERROR: in NthToNth.$id.log"
    exit 1
endif

#
# Assign Null Luis
#
echo "    Assigning Null Luis ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! NullLuis.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    DECLARE
        null_lui          VARCHAR2(10);
        l_lui          VARCHAR2(10);
        l_luip     VARCHAR2(10);
	    l_luip2     VARCHAR2(10);
        location                VARCHAR2(3);
        null_lui_exc            EXCEPTION;

    BEGIN
        location := '10';
        SELECT DISTINCT b.lui, b.lui_dash INTO l_lui, l_luip
        FROM lui_facts a, tmp_lui_assignment b
        WHERE a.norm_string IS NULL
        AND a.lui_dash = b.lui_dash;

        location := '15';
        SELECT DISTINCT lui INTO null_lui FROM string_ui
        WHERE language='ENG' AND norm_string IS NULL;

        location := '20';
        IF l_lui != null_lui THEN
            -- Null luis has not been assigned to empty norm string,
            -- This block will re-assign null luis to empty norm string.
            location := '30';
            SELECT nvl(min(lui_dash),'null') INTO l_luip2
            FROM tmp_lui_assignment
            WHERE lui = null_lui;

	    IF l_luip2 != 'null' THEN
                location := '40';
                UPDATE tmp_lui_assignment
                SET lui_dash = l_luip
                WHERE lui = null_lui;
     
                location := '50';
                UPDATE tmp_lui_assignment
                SET lui_dash = l_luip2
                WHERE lui = l_lui;
	    ELSE
                location := '60';
                UPDATE tmp_lui_assignment
                SET lui = null_lui
                WHERE lui_dash = l_luip; 
	    END IF;
        END IF;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('SQL Error at location: '||location||'; SQLERRM: '||SQLERRM);
            ROLLBACK;
            RAISE null_lui_exc;
    END;
/
EOF
if ($status != 0 || `$FGREP -c 'ORA-' NullLuis.$id.log` > 0) then
    echo "      ERROR: in NullLuis.$id.log"
    exit 1
endif

#
# Assign New Luis
#
echo "    Assigning New Luis ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! NewLuis.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    exec MEME_UTILITY.drop_it('table','new_luis');
    CREATE TABLE new_luis AS
    SELECT DISTINCT lui_dash FROM lui_facts
    MINUS
    SELECT lui_dash FROM tmp_lui_assignment;

    DECLARE
        mt_max_lui              NUMBER;
        location                VARCHAR2(3);
        new_lui_exc             EXCEPTION;

    BEGIN

    location := '10';
    SELECT max_id INTO mt_max_lui
    FROM max_tab
    WHERE table_name = 'LUI';

    location := '20';
    UPDATE max_tab a
    SET max_id = (SELECT a.max_id + COUNT(*) FROM new_luis);

    location := '30';
    INSERT INTO tmp_lui_assignment (lui, lui_dash)
    SELECT 'L'||LPAD(mt_max_lui+rownum,
    	(select to_number(value) from code_map where code='LUI' and type='ui_length'),
    	0), lui_dash
    FROM new_luis;
    
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('SQL Error at location: '||location||'; SQLERRM: '||SQLERRM);
            ROLLBACK;
            RAISE new_lui_exc;
    END;
/
EOF
if ($status != 0 || `$FGREP -c 'ORA-' NewLuis.$id.log` > 0) then
    echo "      ERROR: in NewLuis.$id.log"
    exit 1
endif

#
# QA Counts
#
echo "    QA Counting ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! QACounts.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    exec MEME_UTILITY.drop_it('table','lui_assignment');
    CREATE TABLE lui_assignment AS
    SELECT a.sui AS sui, a.lui AS old_lui, b.lui AS new_lui
    FROM lui_facts a, tmp_lui_assignment b
    WHERE a.lui_dash = b.lui_dash;
    
    CREATE INDEX X_LUIASSIGN_OLD ON LUI_ASSIGNMENT
		(OLD_LUI)
			LOGGING
			NOPARALLEL;

    set feedback on

    DECLARE
        qa_count                NUMBER;
        location                VARCHAR2(3);
        qa_count_exc            EXCEPTION;

    BEGIN

        location := '10';
        qa_count := MEME_UTILITY.exec_count('lui_merge');
        DBMS_OUTPUT.PUT_LINE('Lui Merge: '||qa_count);

        location := '20';
        qa_count := MEME_UTILITY.exec_count('lui_split');
        DBMS_OUTPUT.PUT_LINE('Lui Split: '||qa_count);

        location := '30';
        qa_count := MEME_UTILITY.exec_count('lui_split_merge');
        DBMS_OUTPUT.PUT_LINE('Lui Split Merge: '||qa_count);

        location := '40';
        qa_count := MEME_UTILITY.exec_count('new_luis');
        DBMS_OUTPUT.PUT_LINE('New Luis: '||qa_count);

        location := '50';
        qa_count := MEME_UTILITY.exec_select(
            'SELECT COUNT(DISTINCT old_lui) '||
            'FROM lui_assignment '||
            'WHERE old_lui = new_lui');
        DBMS_OUTPUT.PUT_LINE('Lui Assignment: '||qa_count);

    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('SQL Error at location: '||location||'; SQLERRM: '||SQLERRM);
            ROLLBACK;
            RAISE qa_count_exc;
    END;
/
EOF
if ($status != 0 || `$FGREP -c 'ORA-' QACounts.$id.log` > 0) then
    echo "      ERROR: in QACounts.$id.log"
    exit 1
endif

#
# Compare record count (string_ui vs lui_facts)
#
echo "    Comparing record counts (string_ui vs lui_facts) ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! QAValid.$id.log
    set feedback off
    whenever sqlerror exit 1
    set serveroutput on size 100000
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    DECLARE
        string_ui_ct              NUMBER;
        lui_facts_ct              NUMBER;
        location                  VARCHAR2(3);
        qa_valid_exc              EXCEPTION;

    BEGIN
        location := '10';
	-- Only count ENG strings
        string_ui_ct  := MEME_UTILITY.exec_select(
		'select count(*) from string_ui where language=''ENG''');
        lui_facts_ct  := MEME_UTILITY.exec_count('lui_facts');

        IF string_ui_ct != lui_facts_ct THEN
            RAISE qa_valid_exc;
        END IF;
    EXCEPTION
        WHEN qa_valid_exc THEN
            DBMS_OUTPUT.PUT_LINE('SQL Error at location: '||location||'; SQLERRM: '||SQLERRM);
            DBMS_OUTPUT.PUT_LINE('string_ui_ct: '||string_ui_ct);
            DBMS_OUTPUT.PUT_LINE('lui_facts_ct: '||lui_facts_ct);
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('SQL Error at location: '||location||'; SQLERRM: '||SQLERRM);
            RAISE qa_valid_exc;
    END;
/
EOF
if ($status != 0 || `$FGREP -c 'SQL Error at location' QAValid.$id.log` > 0) then
    echo "      ERROR: in QAValid.$id.log"
    exit 1
endif


#
# Rebuild string_ui
#
echo "    Rebuilding string_ui ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! string_ui.$id.log
    set feedback off
    whenever sqlerror exit 1
    alter session set sort_area_size=268435456;
    alter session set hash_area_size=268435456;

    exec MEME_UTILITY.drop_it('table','t_string_ui');
    CREATE TABLE t_string_ui AS
    SELECT /*+ parallel(b) */ b.lui, a.sui, string_pre, 
        SUBSTR(b.norm_string,1,10) AS norm_string_pre,
        language, base_string, string, b.norm_string, isui, 
        lowercase_string_pre
    FROM string_ui a, lui_facts b
    WHERE a.sui = b.sui;

    -- Add foreign ones back in
    INSERT /*+ append */ INTO t_string_ui
    SELECT /*+ parallel(s) */ * FROM string_ui s WHERE language!='ENG';

    exec MEME_SYSTEM.truncate('string_ui');
    exec MEME_SYSTEM.drop_indexes('string_ui');

    ALTER TABLE string_ui MOVE;
    INSERT /*+ APPEND */ INTO string_ui 
    SELECT /*+ parallel(s) */ * FROM t_string_ui s;
    COMMIT;

    exec meme_system.rebuild_table('string_ui','Y',' ');

    set feedback on

    DECLARE
        su_lui                    VARCHAR2(10);
        su_sui                    VARCHAR2(10);
        table_name                VARCHAR2(50);
        query_ctr                 NUMBER;
	ctr                       NUMBER;
        location                  VARCHAR2(3);
        string_ui_exc             EXCEPTION;

        TYPE cur_qry_type IS REF CURSOR;
        cur_qry cur_qry_type;
    BEGIN

        DBMS_OUTPUT.PUT_LINE('Update string_ui LUIs.');
        OPEN cur_qry FOR 
           'SELECT  a.new_lui, a.sui FROM lui_assignment a 
            WHERE old_lui != new_lui';
        LOOP
            FETCH cur_qry INTO su_lui, su_sui;
            EXIT WHEN cur_qry%NOTFOUND;

	    UPDATE string_ui SET lui = su_lui 
	    WHERE sui = su_sui AND lui != su_lui;

	    COMMIT;
        END LOOP;

	-- Report LUI and norm_string for LUIS that changed.
        MEME_UTILITY.drop_it('table','tmp_string_ui_$id');
	execute immediate '
	    CREATE TABLE tmp_string_ui_$id AS
	    SELECT sui, lui, norm_string FROM string_ui
	    WHERE sui IN (SELECT b.sui FROM lui_assignment b 
		      WHERE old_lui != new_lui) ';

	DBMS_OUTPUT.PUT_LINE(cur_qry%ROWCOUNT || ' rows updated.');
        CLOSE cur_qry;
    END;
/

    DECLARE
        su_lui                    VARCHAR2(10);
        su_sui                    VARCHAR2(10);
        table_name                VARCHAR2(50);
        query_ctr                 NUMBER;
	ctr                       NUMBER;
        location                  VARCHAR2(3);
        string_ui_exc             EXCEPTION;

        TYPE cur_qry_type IS REF CURSOR;
        cur_qry cur_qry_type;
    BEGIN
        MEME_UTILITY.drop_it('table','tmp_string_ui_$id');

        table_name := 'classes';
        FOR query_ctr IN 1..2 LOOP
	    DBMS_OUTPUT.PUT_LINE('Update ' || table_name || ' LUIs.');
            OPEN cur_qry FOR 
               'SELECT DISTINCT a.lui, a.sui FROM string_ui a, '||table_name||' b '||
               'WHERE a.sui = b.sui AND a.lui != b.lui';
            LOOP
                FETCH cur_qry INTO su_lui, su_sui;
                EXIT WHEN cur_qry%NOTFOUND;

                IF table_name = 'classes' THEN
		    UPDATE classes set lui = su_lui
		    WHERE sui = su_sui;
                ELSIF table_name = 'dead_classes' THEN
		    UPDATE dead_classes set lui = su_lui
		    WHERE sui = su_sui;
		END IF;

		COMMIT;
            END LOOP;
	    DBMS_OUTPUT.PUT_LINE(cur_qry%ROWCOUNT || ' rows updated.');
            CLOSE cur_qry;
            table_name := 'dead_'||table_name;
        END LOOP;

    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('SQL Error at location: '||location||'; SQLERRM: '||SQLERRM);
            RAISE string_ui_exc;
    END;
/
EOF
if ($status != 0 || `$FGREP -c 'ORA-' string_ui.$id.log` > 0) then
    echo "      ERROR: in string_ui.$id.log"
    exit 1
endif

#
# Call add_words
#
echo "    Calling add_words.csh ...`/bin/date`"
$MEME_HOME/bin/add_words.csh -all $db

#
# Cleanup
#
echo "    Cleaning up ...`/bin/date`"
#\rm -f $META_WORK/lui_facts.ctl
#\rm -f $META_WORK/string.$id
#\rm -f $META_WORK/string.norm.$id
#\rm -f $META_WORK/string.tmpluis.$id
#\rm -f $META_WORK/lui_facts.dat
#\rm -f {lui_facts.log,sql_create.$id.log,sql_rank.$id.log,LuiMerge.$id.log,LuiSplit.$id.log,LuiSplitMerge.$id.log,LuiMergeOnly.$id.log,LuiSplitOnly.$id.log,OneToOne.$id.log,NthToOne.$id.log,OneToNth.$id.log,NthToNth.$id.log,NullLuis.$id.log,NewLuis.$id.log,QACounts.$id.log,QAValid.$id.log,string_ui.$id.log}



echo "    Creating report files ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! $META_WORK/t.$$.log
    whenever sqlerror exit 1
    set serveroutput on size 100000
    set feedback off
    
    exec MEME_UTILITY.drop_it('table','split_merge');
    create table split_merge as
    select old_lui,new_lui, a.sui, string
    from string_ui a,
    (select * from lui_assignment where old_lui in 
      (select lui from lui_split_merge)
    union
    select * from lui_assignment where new_lui in 
      (select new_lui from lui_assignment, lui_split_merge
       where lui=old_lui)) b
    where a.sui=b.sui;
    
    exec MEME_UTILITY.drop_it('table','split');
    create table split as
    select old_lui, new_lui, a.sui, string
    from string_ui a,
    (select * from lui_assignment where old_lui in
     (select lui from lui_facts a, lui_split b
      where a.lui_dash=b.lui_dash)) b
    where a.sui=b.sui;
    
    exec MEME_UTILITY.drop_it('table','merge');
    create table merge as
    select old_lui, new_lui, a.sui, string
    from string_ui a,
    (select * from lui_assignment where old_lui in
     (select lui from lui_merge)) b
    where a.sui=b.sui;

EOF
if ($status != 0) then
    echo "error creating report tables"
    cat $META_WORK/t.$$.log
    exit 1
endif

$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select * from merge order by 2,1" >! merge.rpt
$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select * from split order by 1,2" >! split.rpt
$MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select * from split_merge order by 1,2" >! split_merge.rpt

echo "    Removing temporary report tables ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >! $META_WORK/t.$$.log
    whenever sqlerror exit 1
    set serveroutput on size 100000
    set feedback off
    
    exec MEME_UTILITY.drop_it('table','split_merge');
    exec MEME_UTILITY.drop_it('table','split');
    exec MEME_UTILITY.drop_it('table','merge');

EOF
if ($status != 0) then
    echo "error dropping report tables"
    cat $META_WORK/t.$$.log
    exit 1
endif

echo "    Clean up temp space ($META_WORK) ... `/bin/date`"
/bin/rm -Rf $META_WORK


#
# End program logic
#
echo "----------------------------------------------------------------"
echo "Finished $0 ...`/bin/date`"
echo "----------------------------------------------------------------"

