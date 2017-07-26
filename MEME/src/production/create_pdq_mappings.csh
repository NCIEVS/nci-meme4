#!/bin/csh -f
#
# This script can be re-run without being destructive. it accommodates any data
# created by past runs.
#
# TODO:
# 1. consider making CODE-based attributes
# 2. consider including all NCI/PDQ tty's instead of just PT (and HT,PSC)

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($?MIDSVCS_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($#argv != 3) then
    echo "Usage: $0 <database> <from_source> <to_source>"
    exit 1
else if ($#argv == 3) then
    set db=$1
    set from_source=$2
    set to_source=$3
endif

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
echo "db: $db"
echo "from_source: $from_source"
echo "to_source: $to_source"
echo ""

set u=`echo $user | cut -d\/ -f 1`


#
# Add PDQ/XM termgroup
#

echo "    Create PDQ/XM atom termgroup ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1
    -- don't add it again if it's already there
    insert into termgroup_rank (termgroup, rank, release_rank, notes, suppressible, tty)
    select '$from_source/XM' as termgroup,
    rank,release_rank,notes,suppressible,tty
    from termgroup_rank where termgroup =
        (select previous_name from source_version where source='PDQ')||'/XM'
    and '$from_source/XM' not in (select termgroup from termgroup_rank);
   
EOF
if ($status != 0) then
    echo "Error adding PDQ/XM termgroup"
   cat /tmp/t.$$.log
    exit 1
endif

#
# Create XM atom
#
echo "    Create new PDQ/XM concept and atom, turn off old one ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1

    -- Insert a new concept
    truncate table source_concept_status;
    update max_tab set max_id = max_id+1 where table_name='CONCEPT_STATUS';
    insert into source_concept_status select 'R',0,'','$from_source','R','N','Y',max_id,0
    from max_tab where table_name='CONCEPT_STATUS';
    COMMIT;
    EXEC dbms_output.put_line(MEME_BATCH_ACTIONS.macro_action( -
            action => 'I',-
            id_type => 'CS',-
            authority => '$from_source',-
            table_name => 'source_concept_status',-
            work_id => 0,-
            status => 'R'));

    -- Insert a new XM atom
    exec MEME_UTILITY.drop_it('table','t_$$_xm');
    CREATE TABLE t_$$_xm as
    SELECT  concept_id,c.atom_id,a.atom_name,termgroup,source,code,
           status,generated_status,released,tobereleased,suppressible,
           source_aui,source_cui,source_dui
    FROM classes c,atoms a WHERE 1=2;
    
    INSERT INTO t_$$_xm
    SELECT concept_id,0,'$from_source to $to_source Mappings',
        '$from_source/XM','$from_source','100001',
        'R','Y','N','Y','N','','',''
    FROM source_concept_status;

    -- Identify any old ones to remove
    exec MEME_UTILITY.drop_it('table','t_$$_d_xm');
    CREATE TABLE t_$$_d_xm AS
    SELECT atom_id As row_id
    FROM classes
    WHERE source in (select source from source_rank where stripped_source = 'PDQ')
    AND tty='XM' and tobereleased in ('Y','y');

    -- Identify any attributes connected to it as well
    exec MEME_UTILITY.drop_it('table','t_$$_d_a');
    CREATE TABLE t_$$_d_a as
    SELECT attribute_id as row_id FROM attributes
    WHERE atom_id =
        (SELECT row_id from t_$$_d_xm);
EOF
if ($status != 0) then
    echo "Error creating PDQ/XM atom"
   cat /tmp/t.$$.log
    exit 1
endif

echo "    Inserting XM atom ...`/bin/date`"
    $MEME_HOME/bin/insert.pl -atoms t_$$_xm $db NCIMTH >&! insert.xm.log
    if ($status != 0) then
        echo "Error inserting XM atom"
        cat insert.xm.log
        exit 1
    endif
endif

echo "    Create atom_ordering entry ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    insert into atom_ordering values (
        (select max(atom_id) as atom_id from classes where concept_id in
            (select concept_id from t_$$_xm)),
        'PDQ',
        (select max(atom_id) as atom_id from classes where concept_id in
            (select concept_id from t_$$_xm)));
EOF
if ($status != 0) then
    echo "Error creating atom_ordering entry"
    exit 1
endif


#
# Remove obsolete XM atom - this also removes the attributes connected to it 
# and makes the concept unreleasable.
#
set ct=`$MEME_HOME/bin/dump_table.pl -u $u -d $db -q "select count(*) ct from t_$$_d_xm"`
echo "    Remove obsolete XM atom ...`/bin/date`"
echo "      ct = $ct"
if ($ct != 0) then
    $MEME_HOME/bin/batch.pl -a T -n N -t C -s t t_$$_d_xm $db NCIMTH >&! batch.d.xm.log
    if ($status != 0) then
        echo "Error deleting obsolete XM atom"
        cat batch.d.xm.log
        exit 1
    endif
endif

set ct=`$MEME_HOME/bin/dump_table.pl -u $u -d $db -q "select count(*) ct from t_$$_d_a"`
echo "    Remove obsolete XM atom ...`/bin/date`"
echo "      ct = $ct"
if ($ct != 0) then
    $MEME_HOME/bin/batch.pl -a T -n N -t A -s t t_$$_d_a $db NCIMTH >&! batch.d.a.log
    if ($status != 0) then
        echo "Error deleting obsolete XM attributes"
        cat batch.d.a.log
        exit 1
    endif
endif



#
# Save concept_id for new XM atom
#
set xmid=`$MEME_HOME/bin/dump_table.pl -u $u -d $db -q "select concept_id from t_$$_xm"`

#
#  Assign XM atom Intellectual Property STY
#

echo "    Add STY for XM atom ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1

    exec MEME_UTILITY.drop_it('table','t_$$_xm_sty');
    CREATE TABLE t_$$_xm_sty AS
    SELECT concept_id,atom_id,attribute_id,attribute_level,
                   attribute_name,attribute_value,source,status,
                   generated_status,released,tobereleased,suppressible,
                   sg_id,sg_type,sg_qualifier,source_atui
    FROM attributes where 1=2;
    
    INSERT INTO t_$$_xm_sty VALUES
    ($xmid,0,0,'C','SEMANTIC_TYPE','Intellectual Product','$from_source','R',
           'Y','N','Y','N','$xmid','CONCEPT_ID','','');
EOF

if ($status != 0) then
    echo "Error creating PDQ/XM atom"
   cat /tmp/t.$$.log
    exit 1
endif

echo "    Inserting XM STY ...`/bin/date`"
    $MEME_HOME/bin/insert.pl -atts t_$$_xm_sty $db NCIMTH >&! insert.xm.sty.log
    if ($status != 0) then
        echo "Error inserting XM STY"
        cat insert.xm.log
        exit 1
    endif
endif


#
# Create and load attributes.src file
#
 
set version=`$MEME_HOME/bin/dump_table.pl -u $u -d $db -q "select version from source_rank where source='$from_source'"`
#
# Create attributes
#
echo "    Create attributes ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1

   exec MEME_UTILITY.drop_it('table','t_$$_map');
    CREATE TABLE t_$$_map AS
    SELECT DISTINCT a.concept_id,a.atom_id,a.source_dui as sg_id_1,b.source_cui as sg_id_2,'Y' as flag
    FROM classes a,classes b
    WHERE a.source in (SELECT current_name FROM source_version where source='PDQ')
      AND b.source in (SELECT current_name FROM source_version where source='NCI')
      AND a.concept_id = b.concept_id
      AND a.tobereleased in ('Y','y') and b.tobereleased in ('Y','y')
      AND a.tty||b.tty IN ('PTPT','PTPSC','PTHT');

    INSERT INTO t_$$_map
    SELECT DISTINCT a.concept_id,a.atom_id,a.source_dui as sg_id_1,b.source_cui as sg_id_2,'N' as flag
    FROM classes a,classes b
    WHERE a.source in (SELECT current_name FROM source_version where source='PDQ')
      AND b.source in (SELECT current_name FROM source_version where source='NCI')
      AND a.concept_id = b.concept_id
      AND a.tobereleased in ('Y','y') and b.tobereleased in ('Y','y')
      AND a.tty||b.tty NOT IN ('PTPT','PTPSC','PTHT');

    exec MEME_UTILITY.drop_it('table','t_$$_xmap');
    CREATE TABLE t_$$_xmap AS
    SELECT concept_id,atom_id,attribute_id,attribute_level,
                   attribute_name,attribute_value,source,status,
                   generated_status,released,tobereleased,suppressible,
                   sg_id,sg_type,sg_qualifier,source_atui
    FROM attributes WHERE 1=0;

    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','MAPSETVERSION','$version','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','FROMVSAB','$from_source','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','FROMRSAB','PDQ','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','TOVSAB','$to_source','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','TORSAB','NCI','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','MAPSETVSAB','$from_source','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','MAPSETRSAB','PDQ','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','MTH_MAPSETCOMPLEXITY','N_TO_N','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','MTH_MAPFROMCOMPLEXITY','SINGLE SDUI','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','MTH_MAPTOCOMPLEXITY','SINGLE SCUI','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','MTH_MAPTOEXHAUSTIVE','N','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');
    INSERT INTO t_$$_xmap VALUES ($xmid,0,0,'S','MTH_MAPFROMEXHAUSTIVE','N','$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ','');

    INSERT INTO t_$$_xmap
    SELECT DISTINCT $xmid as concept_id,0,0,'S','XMAP',
    '~1~'||sg_id_1||'~SY~~'||sg_id_2||'~~~~~~' as attribute_value,
    '$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ',''
     FROM t_$$_map
     WHERE flag='Y';
    
    INSERT INTO t_$$_xmap
    SELECT DISTINCT $xmid as concept_id,0,0,'S','XMAP',
    '~2~'||sg_id_1||'~SY~~'||sg_id_2||'~~~~~~' as attribute_value,
    '$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ',''
     FROM t_$$_map
     WHERE flag='N';

    -- Use the codes as the FROMID and the TOID instead of trying to maintain our own
    -- this makes the maintenance challenge easier     
    INSERT INTO t_$$_xmap
    SELECT DISTINCT $xmid as concept_id,0,0,'S','XMAPFROM',
    sg_id_1||'~~'||sg_id_1||'~SDUI~~' as attribute_value,
    '$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ',''
    FROM t_$$_map;
    
    INSERT INTO t_$$_xmap
    SELECT DISTINCT $xmid as concept_id,0,0,'S','XMAPTO',
    sg_id_2||'~~'||sg_id_2||'~SCUI~~' as attribute_value,
    '$from_source','R','Y','N','Y','N','100001','CODE_ROOT_SOURCE','PDQ',''
    FROM t_$$_map;
    
EOF
if ($status != 0) then
    echo "Error preparing tables for XMAP Attributes"
   cat /tmp/t.$$.log
    exit 1
endif



#
# Convert insert.pl table to attributes.src
#

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF

   WHENEVER SQLERROR EXIT 2;
   ALTER TABLE t_$$_xmap ADD hashcode VARCHAR2(100);
   UPDATE t_$$_xmap
   SET hashcode =
        MEME_UTILITY.md5(attribute_value);
EOF
if ($status != 0) then
    echo "Error converting insert.pl to attributes.src"
    exit 1
 endif

$MEME_HOME/bin/dump_mid.pl -t t_$$_xmap $db .
if ($status != 0) then
    echo "ERROR dumping t_$$_xmap
    exit 1
endif

$PATH_TO_PERL -ne \
  'split /\|/; $satid++; print "$satid|$_[12]|$_[3]|$_[4]|$_[5]|$_[6]|$_[7]|$_[10]|$_[9]|$_[11]|$_[13]|$_[14]|$_[15]|$_[16]|\n";' \
  t_$$_xmap.dat >&! attributes.src


echo "    Inserting attributes ... `/bin/date`"
$MEME_HOME/bin/insert_attributes.csh $db $from_source >&! insert_attributes.log
if ($status != 0) then
    echo "ERROR inserting attributes"
    cat insert_attributes.log
    exit 1
endif


echo "    Cleanup ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1
    drop table t_$$_xm;
    drop table t_$$_d_xm;
    drop table t_$$_xm_sty;
    drop table t_$$_map;
    drop table t_$$_xmap;
EOF

echo "--------------------------------------------------------------"
echo "Done... `/bin/date`"
echo "--------------------------------------------------------------"






