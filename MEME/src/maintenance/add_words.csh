#! /bin/csh -f 
#
# File		add_words.csh
# Written by 	Brian Carlsen (4/2000)
#
# This script loads word_index, normwrd, and normstr. 
# Call with -help for more info
#
# Dependencies: 
#   requires LVG_HOME to be set
#   requires MEME_HOME to be set
#   $MEME_HOME/bin/oraclerc must exist
#   source_classes_atoms must be current
#
# 12/30/2004 (4.5.0): Accepts a -w flag to pass in work id
# 12/13/2004 (4.4.0): no more report table change
# 11/19/2004 (4.3.0): Released
# 05/27/2004 (4.2.1): Reindex tables when using -all
# 04/01/2004 (4.2.0): No longer sorts $DATA/word_index.dat (problem PDQ2004)
# 03/18/2003 (4.1.0): Ported to MEME4
#
set release=4
set version=5.0
set authority="BAC"
set date="12/30/2004"

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

if ($?LVG_HOME != 1) then
    echo '$LVG_HOME must be set'
    exit 1
endif

if ($?MEME_HOME != 1) then
    echo '$MEME_HOME must be set'
    exit 1
endif

if ($?ORACLE_HOME != 1) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

#
# Environment
#
set CAT=/bin/cat
set AWK=/bin/awk
set FGREP=/bin/fgrep
set SORT=/bin/sort
set DATA=/tmp
set SED=/bin/sed
set all=0
set undo=0
set cmd=append
set table_name=source_classes_atoms
set work_id=0

#
# Parse arguments
#

if ($#argv == 0) then
    echo "Error: Bad argument"
    echo "Usage: $0 [{-all,-undo}]  [-w <work_id>] <database>"
    exit 1
endif

set i=1
while ($i <= $#argv)
    switch ($argv[$i])
	case '-*help':
	    cat <<EOF
 This script has the following usage:
   Usage: $0 [{-all,-undo}]  [-w <work_id>] <database>

   This script dumps the atom_id|atom_name to generate index entries
   for.  This data comes from source_classes_atoms unless -all is used
   in which case it comes from string_ui.

   The luiNorm program is used to generate the normalized strings for 
   each atom name, and this plus the string itself are used to generate
   three load files:  normstr.dat, word_index.dat, and normwrd.dat files.  
   These are loaded into the corresponding tables using a direct path
   load.  Append mode is used unless -all is specified, in which case
   truncate mode is used.  This script writes, uses, and then removes
   the needed Oracle .ctl files.

   For foreign atoms, only word_index.dat is created, i.e. the strings
   are not normalized.

   The -all flag will reinitialize the word indexes for all atoms
   The -undo flag will remove rows that were created from the previous
       add_words transaction

EOF
	    exit 0
        case '-version':
            echo "Release ${release}: version $version, $date ($authority)"
            exit 0
        case '-v':
            echo "$version"
            exit 0
        case '-undo':
	    set undo=1
	    breaksw
        case '-all':
            set cmd=truncate
            set table_name=classes
            set all=1
	    breaksw
	case '-w':
	    set i=`expr $i + 1`
	    set work_id=$argv[$i]
	    breaksw
        default :
	    set arg_count=1
	    set all_args=`expr $i + $arg_count - 1`
	    if ($all_args != $#argv) then
		echo "Error: Incorrect number of arguments: $all_args, $#argv"
                echo "Usage: $0 [{-all,-undo}]  [-w <work_id>] <database>"
		exit 1
	    endif
	    set db=$argv[$i]
	    set i=`expr $i + 1`
    endsw
    set i=`expr $i + 1`
end

# Cannot use both -all and -undo
if ($all == 1 && $undo == 1) then
    echo "${0}: Cannot use both -all and -undo."
    exit 1
endif

if ($?db != 1) then
    echo "Usage: $0 [{-all,-undo}] <database>"
    exit 1
endif

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`

echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
echo "MEME_HOME:      $MEME_HOME"
echo "ORACLE_HOME:    $ORACLE_HOME"
echo "database:       $db"
echo "cmd:            $cmd"
echo "table_name:     $table_name"
echo "work_id:        $work_id"
echo ""
set start_t=`$PATH_TO_PERL -e 'print time'`

if ($undo == 1) then
    echo "Inform MRD of index table changes ... `/bin/date`"

    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF | /bin/sed 's/^/    /' 
    WHENEVER SQLERROR EXIT 2;
    SET FEEDBACK OFF;
    SET AUTOCOMMIT ON;
    SET SERVEROUTPUT ON SIZE 100000;
    SET ECHO OFF;

    exec MEME_UTILITY.drop_it('table','taw_$$');
    CREATE TABLE taw_$$ AS
    SELECT atom_id FROM word_index
    WHERE atom_id IN (SELECT atom_id FROM $table_name);

    DELETE FROM word_index 
    WHERE atom_id IN (SELECT atom_id FROM $table_name);

    exec MEME_UTILITY.put_message('word_index changes reported');

    exec MEME_UTILITY.drop_it('table','taw_$$');
    CREATE TABLE taw_$$ AS
    SELECT normwrd_id FROM normwrd
    WHERE normwrd_id IN (SELECT atom_id FROM $table_name);

    DELETE FROM normwrd
    WHERE normwrd_id IN (SELECT atom_id FROM $table_name);

    exec MEME_UTILITY.put_message('normwrd changes reported');

    exec MEME_UTILITY.drop_it('table','taw_$$');
    CREATE TABLE taw_$$ AS
    SELECT normstr_id FROM normstr
    WHERE normstr_id IN (SELECT atom_id FROM $table_name);

    DELETE FROM normstr
    WHERE normstr_id IN (SELECT atom_id FROM $table_name);

    exec MEME_UTILITY.drop_it('table','taw_$$');
    exec MEME_UTILITY.put_message('normstr changes reported');

EOF
    if ($status != 0) then
        echo "Error reporting index table changes to MRD"
        exit 1
    endif

    echo "Informing the MRD successfully completed ...`/bin/date`"

    echo "--------------------------------------------------------------"
    echo "Finished `/bin/date`"
    echo "--------------------------------------------------------------"

    exit 0
endif


#
# Write strings to file
#
echo "Dump $table_name to $DATA/strings...`/bin/date`"

if ($all == 1) then

    set options="options (direct=true) unrecoverable"

    #
    # Write out strings for classes
    #
    set query="select atom_id,c.language,string,norm_string from classes b, string_ui c where b.sui=c.sui"
    $MEME_HOME/bin/dump_table.pl -u $user -d $db -q "$query" \
    >! $DATA/strings
    echo "    Finished dumping classes data ...`/bin/date`"

    #
    # Drop indexes on index tables
    #
    $ORACLE_HOME/bin/sqlplus $user@$db >&! $DATA/sql.$$ <<EOF
        WHENEVER SQLERROR EXIT -2
        exec MEME_SYSTEM.drop_indexes('normstr');
        exec MEME_SYSTEM.drop_indexes('normwrd');
        exec MEME_SYSTEM.drop_indexes('word_index');
        exec MEME_SYSTEM.drop_indexes('dead_normstr');
        exec MEME_SYSTEM.drop_indexes('dead_normwrd');
        exec MEME_SYSTEM.drop_indexes('dead_word_index');
EOF
    if ($status != 0 || `$FGREP -c 'ORA-' $DATA/sql.$$` > 0) then
        echo "Error dropping indexes"
        set error_flag=1
    else
        echo "    Indexes dropped successfully ...`/bin/date`"
    endif

else

    set options="options (rows=100000, bindsize=10000000, readsize=10000000)"

    #
    # Write out strings for atoms being added
    #
    $MEME_HOME/bin/dump_table.pl -u $user -d $db -q "select b.atom_id || '|' || c.language || '|' || string || '|' || norm_string as line from $table_name b, string_ui c where c.sui=b.sui" >! $DATA/strings
    echo "    Finished dumping $table_name data ...`/bin/date`"

endif

#
# Append dead strings
#
if ($all == 1) then
    echo "Dump dead_$table_name to $DATA/strings...`/bin/date`"
    set query="select atom_id,c.language,string,norm_string from dead_classes b, string_ui c where b.sui=c.sui"
    $MEME_HOME/bin/dump_table.pl -u $user -d $db -q "$query" \
     >> $DATA/strings
endif

#
# luiNorm the strings
#
echo "luiNorm strings ...`/bin/date`"
# strip strings has problems
#$MEME_HOME/bin/strip_strings.exe -t2 < $DATA/strings |\
#$PATH_TO_PERL -ne 'chop; ($a,$l,$b) = split /\|/; \
#          if ($b =~ /(.*) <[0-9]{1,2}>$/) { $c = $1; $d = "N"; } \
#          else { $c = "$b"; $d = "Y"} \
#	  print join("|",($a,$l,$b,$c,$d)),"\n";' $DATA/strings |\
#  $AWK -F\| '{print $1"|"$2"|"$4}' | $LVG_HOME/bin/luiNorm -t:3 -n |\
#  $SED 's/-No Output-//' >! $DATA/strings.norm
#if ($status != 1) then
#    echo "ERROR::: segmentation fault while norming strings"
#endif

# We already have the norm_string from string_ui, so just copy the file
/bin/cp -f $DATA/strings $DATA/strings.norm


#
# Create the data files
#
echo "Creating data files ...`/bin/date`"
$LVG_HOME/bin/wordInd -t:3 -F:1 < $DATA/strings.norm |\
    tr 'A-Z' 'a-z' | $SED 's/$/\|/'  >! $DATA/word_index.dat
echo "    $DATA/word_index.dat created ...`/bin/date`"
$PATH_TO_PERL -ne 'split /\|/; print if $_[1] eq "ENG";' $DATA/strings.norm | $LVG_HOME/bin/wordInd -t:4 -F:1  |\
   $SED 's/$/\|/' | $SORT -u >! $DATA/normwrd.dat
echo "    $DATA/normwrd.dat created ...`/bin/date`"
$PATH_TO_PERL -ne 'chop; split /\|/; print "$_[0]|$_[3]|\n" if $_[1] eq "ENG";' $DATA/strings.norm >! $DATA/normstr.dat
echo "    $DATA/normstr.dat created ...`/bin/date`"

#
# Load database
#
echo "Load database ...`/bin/date`"
$CAT <<EOF >! normstr.ctl
$options
load data
infile '$DATA/normstr'  "str X'7c0a'"
badfile '$DATA/normstr.bad'
$cmd into table normstr 
fields terminated by '|' 
trailing nullcols
(normstr_id             integer external,
 normstr                char(3000)
)
EOF
$CAT <<EOF >! normwrd.ctl
$options
load data
infile '$DATA/normwrd' "str X'7c0a'"
badfile '$DATA/normwrd.bad'
$cmd into table normwrd 
fields terminated by '|' 
trailing nullcols
(normwrd_id             integer external,
 normwrd                char(3000)
)
EOF
$CAT <<EOF >! word_index.ctl
$options
load data
infile '$DATA/word_index' "str X'7c0a'"
badfile '$DATA/word_index.bad'
$cmd into table word_index 
fields terminated by '|'
trailing nullcols
(atom_id                integer external,
 word                   char(3000) 
  )
EOF

#
# Load normstr
#
set error_flag=0
$ORACLE_HOME/bin/sqlldr $user@$db control="normstr.ctl" |\
    /bin/grep 'logical record count' | /bin/sed 's/^/    normstr /'
if ($status != 0 || `$FGREP -c 'ORA-' normstr.log` > 0) then
    echo "Error loading normstr.ctl"
    set error_flag=1
endif

#
# Load normwrd
#
$ORACLE_HOME/bin/sqlldr $user@$db control="normwrd.ctl" |\
    /bin/grep 'logical record count' | /bin/sed 's/^/    normwrd /'
if ($status != 0 || `$FGREP -c 'ORA-' normwrd.log` > 0) then
    echo "Error loading normwrd.ctl"
    set error_flag=1
endif

#
# Load word_index
#
$ORACLE_HOME/bin/sqlldr $user@$db control="word_index.ctl" |\
    /bin/grep 'logical record count' | /bin/sed 's/^/    word_index /'
if ($status != 0 || `$FGREP -c 'ORA-' word_index.log` > 0) then
    echo "Error loading word_index.ctl"
    set error_flag=1
endif

#
# Truncate dead index tables if -all is set
#
if ($all == 1) then
    echo "Run final processes ... `/bin/date`"

    echo "    Truncate dead index tables ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus $user@$db >&! $DATA/sql.$$ <<EOF
        WHENEVER SQLERROR EXIT -2;
	TRUNCATE TABLE dead_word_index;
	TRUNCATE TABLE dead_normwrd;
	TRUNCATE TABLE dead_normstr;
EOF

    if ($status != 0 || `$FGREP -c 'ORA-' $DATA/sql.$$` > 0) then
	echo "Error truncating dead tables"
	set error_flag=1
    else
	echo "    Dead index tables truncated successfully ...`/bin/date`"
    endif

    echo "    Reindex index tables ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus $user@$db >&! $DATA/sql.$$ <<EOF
         WHENEVER SQLERROR EXIT -2;
         alter session set sort_area_size=100000000;
         exec MEME_SYSTEM.reindex('normstr','N',' ');
         exec MEME_SYSTEM.reindex('normwrd','N',' ');
         exec MEME_SYSTEM.reindex('word_index', 'N',' ');
EOF
    
    if ($status != 0 || `$FGREP -c 'ORA-' $DATA/sql.$$` > 0) then
        echo "Error reindexing index tables"
        set error_flag=1
    else
        echo "    Index tables reindexed successfully ...`/bin/date`"
    endif


    echo "    Load dead tables and reindex ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus $user@$db >&! $DATA/sql.$$ <<EOF
    WHENEVER SQLERROR EXIT -2
    SET AUTOCOMMIT ON;

    --
    -- Move tbr='N' rows to dead tables
    -- Move dead_classes rows to dead tables
    --
    INSERT /*+ APPEND */ INTO dead_normstr 
    SELECT * FROM normstr WHERE normstr_id IN
     (SELECT atom_id FROM classes WHERE tobereleased='N'
      UNION SELECT atom_id FROM dead_classes); 

    INSERT /*+ APPEND */ INTO dead_normwrd 
    SELECT * FROM normwrd WHERE normwrd_id IN
     (SELECT atom_id FROM classes WHERE tobereleased='N'
      UNION SELECT atom_id FROM dead_classes); 

    INSERT /*+ APPEND */ INTO dead_word_index 
    SELECT * FROM word_index WHERE atom_id IN
     (SELECT atom_id FROM classes WHERE tobereleased='N'
      UNION SELECT atom_id FROM dead_classes); 

    DELETE FROM normstr WHERE normstr_id IN
     (SELECT normstr_id from dead_normstr);

    DELETE FROM normwrd WHERE normwrd_id IN
     (SELECT normwrd_id FROM dead_normwrd);

    DELETE FROM word_index WHERE atom_id IN
     (SELECT atom_id FROM dead_word_index);

    exec MEME_SYSTEM.reindex('dead_normstr','N',' ');
    exec MEME_SYSTEM.reindex('dead_normwrd','N',' ');
    exec MEME_SYSTEM.reindex('dead_word_index', 'N',' ');

EOF
    if ($status != 0 || `$FGREP -c 'ORA-' $DATA/sql.$$` > 0) then
        echo "Error moving rows to dead tables"
        set error_flag=1
    else
        echo "    dead rows successfully handled ...`/bin/date`"
    endif
endif

if ($error_flag != 0) then
    echo "Exiting ...`/bin/date`"
    exit 1
endif


echo "Cleaning up... `/bin/date`"
\rm -f {normstr,normwrd,word_index}.{log,ctl} 
\rm -f $DATA/{normstr,normwrd,word_index}.dat
\rm -f $DATA/strings $DATA/sql.$$

if ($work_id != 0) then
    $MEME_HOME/bin/log_operation.pl $db MTH "Manage word indexes" \
      "Done loading word indexes ($cmd, $table_name)" $work_id 0 \
      $start_t >> /dev/null
endif

echo "--------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "--------------------------------------------------------------"

