#!/bin/csh -f

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

if ($#argv != 1) then
    echo "Usage: $0 <database>"
    exit 1
else if ($#argv == 1) then
    set db=$1
endif

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`
echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
echo "db: $db"
echo ""

#
# Create attributes
#
echo "    Create attributes ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1

    exec MEME_UTILITY.drop_it('table','t_$$');
    CREATE TABLE t_$$ AS
    SELECT concept_id,atom_id,attribute_id, attribute_level,
                   attribute_name,attribute_value,source,status,
                   generated_status, released,tobereleased, suppressible,
                   sg_id, sg_type, sg_qualifier, source_atui
    FROM attributes WHERE 1=0;

    -- entries to insert - cases of overlap that don't already have current version attributes.
    INSERT INTO t_$$
    SELECT DISTINCT a.concept_id, a.atom_id, 0, 'S', 'NCI_THESAURUS_CODE', b.code, a.source, 'R',
            'Y', 'N', 'Y', 'N', '', '', '', ''
    FROM classes a, classes b
    WHERE a.source in (SELECT current_name FROM source_version where source='PDQ')
      AND b.source in (SELECT current_name FROM source_version where source='NCI')
      AND a.concept_id = b.concept_id
      AND a.tty||b.tty IN ('PTPT','PTPSC','PTHT')
      AND a.tobereleased in ('Y','y') and b.tobereleased in ('Y','y');

    exec MEME_UTILITY.drop_it('table','ti_$$');
    CREATE TABLE ti_$$ AS
    SELECT * from t_$$
    MINUS
    SELECT DISTINCT concept_id, atom_id, 0, 'S', 'NCI_THESAURUS_CODE', attribute_value, source, 'R',
            'Y', 'N', 'Y', 'N', '', '', '', ''
    FROM attributes
    WHERE tobereleased in ('Y','y')
      AND source in (SELECT current_name FROM source_version where source='PDQ')
      AND attribute_name = 'NCI_THESAURUS_CODE';


    -- cases to delete - current version attributes minus new cases
    exec MEME_UTILITY.drop_it('table','td_$$');
    CREATE TABLE td_$$ AS
    SELECT attribute_id row_id FROM attributes
    WHERE (concept_id, atom_id, attribute_value) IN
       (SELECT concept_id, atom_id, attribute_value FROM
         ( SELECT DISTINCT concept_id, atom_id, 0, 'S', 'NCI_THESAURUS_CODE', attribute_value, source, 'R',
            'Y', 'N', 'Y', 'N', '', '', '', ''
           FROM attributes
           WHERE tobereleased in ('Y','y')
           AND source in (SELECT current_name FROM source_version where source='PDQ')
           AND attribute_name = 'NCI_THESAURUS_CODE'
           MINUS SELECT concept_id,atom_id,attribute_id, attribute_level,
                   attribute_name,attribute_value,source,status,
                   generated_status, released,tobereleased, suppressible,
                   sg_id, sg_type, sg_qualifier, source_atui
           FROM t_$$));

EOF
if ($status != 0) then
    echo "Error preparing tables for NCI_THESAURUS_CODE Attributes"
   cat /tmp/t.$$.log
    exit 1
endif

set u=`echo $user | cut -d\/ -f 1`
set ct=`$MEME_HOME/bin/dump_table.pl -u $u -d $db -q "select count(*) ct from ti_$$"`
echo "    Inserting attributes ...`/bin/date`"
echo "      ct=$ct"
if ($ct > 0) then
    $MEME_HOME/bin/insert.pl -atts ti_$$ $db NCIMTH >&! insert.a.log
    if ($status != 0) then
        echo "Error deleting attributes"
        cat insert.a.log
        exit 1
    endif
endif

set ct=`$MEME_HOME/bin/dump_table.pl -u $u -d $db -q "select count(*) ct from td_$$"`
echo "    Remove obsolete attributes ...`/bin/date`"
echo "      ct = $ct"
if ($ct != 0) then
    $MEME_HOME/bin/batch.pl -a D -t A -s t td_$$ $db NCIMTH >&! batch.d.a.log
    if ($status != 0) then
        echo "Error deleting attributes"
        cat batch.d.a.log
        exit 1
    endif
endif

echo "    Cleanup ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1
    drop table t_$$;
    drop table ti_$$;
    drop table td_$$;
EOF

echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
