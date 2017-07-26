#!/bin/csh -f
#
# File:    load_mrdoc.csh
# Author:  Brian Carlsen
#
# REMARKS: This script is used to process a MRDOC.RRF file into the MID.
#
# Changes:
#   04/11/2006 JFW (1-AVWUX): Handle tty_class.
#   03/20/2006 BAC (noticket): Added
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

set s1=0
set s2=0

if ($#argv != 1) then
    echo "Usage: $0 <database>"
    exit 1
endif

set db=$1

if ($?MEME_HOME == 0) then
    echo '$MEME_HOME must be set'
    exit 1
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

set ct=`ls |fgrep -c MRDOC.RRF`
if ($ct == 0) then
    echo "You must run this script from the directory containing MRDOC.RRF"
    exit 1
endif

###################################################################
# Get username/password and work_id
#
# If you are running this script by hand and not as a script
# you have to run the following commands to set $user and $work_id
###################################################################
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
set work_id=0

echo "-------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "-------------------------------------------------"
echo "db:           $db"
echo "work_id:      $work_id"
echo ""

echo "    Load MRDOC.RRF ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -1;
    set serveroutput on size 100000
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;
    
    exec MEME_UTILITY.drop_it('table','t1');
        
    CREATE TABLE t1 (
        dockey    VARCHAR2(100),
        value     VARCHAR2(100),
        type      VARCHAR2(100),
        expl      VARCHAR2(4000)
    );
EOF
if ($status != 0) then
 	echo "Error creating staging table for MRDOC.RRF"
    cat /tmp/t.$$.log
    exit 1
endif

$MEME_HOME/bin/dump_mid.pl -t t1 $db .
if ($status != 0) then
	echo "Error dumping staging table for MRDOC.RRF"
    exit 1
endif
    
/bin/cp -f MRDOC.RRF t1.dat
$ORACLE_HOME/bin/sqlldr $user@$db control="t1.ctl"
if ($status != 0) then
	echo "Error loading staging table for MRDOC.RRF"
    exit 1
endif

echo "    Load MRDOC.RRF data into inverse_rel_attributes ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
	WHENEVER SQLERROR EXIT -1;
    set serveroutput on size 100000
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;
        
    exec MEME_UTILITY.drop_it('table','t2');    
    CREATE TABLE t2 AS
    SELECT value AS relationship_attribute,
        expl AS inverse_rel_attribute_1,
        inverse_rel_attribute AS inverse_rel_attribute_2
    FROM t1 a, inverse_rel_attributes b
    WHERE a.value=b.relationship_attribute AND type='rela_inverse'
      AND expl != inverse_rel_attribute;
        
    BEGIN
        IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
            MEME_UTILITY.put_message('Error: existing inverse_rel_attributes');
            RAISE_APPLICATION_ERROR(-20000,'ERROR: duplicate relationship_attribute in t1');
        END IF;
    END;
/

    exec MEME_UTILITY.drop_it('table','t2');    
    CREATE TABLE t2 AS
    SELECT a.value as value_1, b.value as value_2, 
	a.expl as expl_1, b.expl as expl_2
    FROM t1 a, t1 b
    WHERE a.type='rela_inverse' and b.type='rela_inverse'
      AND ((a.value = b.value AND a.expl != b.expl) OR
           (a.expl = b.expl AND a.value != b.value) OR
           (a.expl = b.value AND a.value != b.expl));

    BEGIN
        IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
            MEME_UTILITY.put_message('Error: duplicate relationship_attribute within t1');
            RAISE_APPLICATION_ERROR(-20000,'ERROR: duplicate relationship_attribute within t1');
        END IF;
    END;
/
            
    INSERT INTO inverse_rel_attributes
        (relationship_attribute,inverse_rel_attribute,rank)
    (SELECT value, expl, 1 FROM t1 
     WHERE type='rela_inverse'
     UNION
     SELECT expl, value, 1 FROM t1 
     WHERE type='rela_inverse')
    MINUS
    SELECT relationship_attribute,inverse_rel_attribute,rank
    FROM inverse_rel_attributes;
EOF
if ($status != 0) then
    echo "Error loading MRDOC.RRF data into inverse_rel_attributes"
    cat /tmp/t.$$.log
    exit 1
endif


echo "    Load MRDOC.RRF data into meme_properties ... `/bin/date`"
   
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -1;
    set serveroutput on size 100000
    alter session set sort_area_size=200000000;
    alter session set hash_area_size=200000000;
        
    exec MEME_UTILITY.drop_it('table','t2');    
    CREATE TABLE t2 AS
    SELECT a.value AS value,
       expl AS description_1,
       description AS description_2
    FROM t1 a, meme_properties b
    WHERE nvl(a.value,'null') = nvl(b.value,'null')
      AND a.type = b.key
      AND a.dockey = b.key_qualifier
      AND a.type = 'expanded_form'
      AND nvl(a.expl,'null') != nvl(b.description,'null');
      
    BEGIN
        IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
           MEME_UTILITY.put_message('Error: existing metadata');
            RAISE_APPLICATION_ERROR(-20001,'ERROR: existing metadata');
        END IF;
    END;
/
            
    exec MEME_UTILITY.drop_it('table','t2');    
    CREATE TABLE t2 AS
    SELECT dockey, nvl(value,'null') value, expl
    FROM t1
    GROUP BY dockey, nvl(value,'null'), expl HAVING count(distinct expl)>1;
    BEGIN
        IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
           MEME_UTILITY.put_message('Error: duplicate metadata within t1');
            RAISE_APPLICATION_ERROR(-20001,'ERROR: duplicate metadata within t1');
        END IF;
    END;
/

    exec MEME_UTILITY.drop_it('table','t2');    
    CREATE TABLE t2 AS
    SELECT dockey,value,type,expl FROM t1 WHERE type='tty_class'
    MINUS
    SELECT key_qualifier,value,key,description FROM meme_properties WHERE key='tty_class';
    BEGIN
        IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
           MEME_UTILITY.put_message('Error: tty_class being added');
            RAISE_APPLICATION_ERROR(-20001,'ERROR: tty_class being added');
        END IF;
    END;
/

    exec MEME_UTILITY.drop_it('table','t2');    
    CREATE TABLE t2 AS
    SELECT key_qualifier,value,key,description FROM meme_properties WHERE key='tty_class'
    AND value in (select value from t1 where type='tty_class')
    MINUS
    SELECT dockey,value,type,expl FROM t1 WHERE type='tty_class';
    BEGIN
        IF MEME_UTILITY.exec_select('select count(*) from t2') > 0 THEN
           MEME_UTILITY.put_message('Error: tty_class being removed');
            RAISE_APPLICATION_ERROR(-20001,'ERROR: tty_class being removed');
        END IF;
    END;
/

    INSERT INTO meme_properties
        (key_qualifier,value,key,description)
    SELECT DISTINCT dockey, value, type, expl FROM t1 
    WHERE type in ('expanded_form','tty_class')
    MINUS
    SELECT key_qualifier,value,key,description
    FROM meme_properties;
EOF
if ($status != 0) then
	echo "Error loading MRDOC.RRF data into meme_properties"
	cat /tmp/t.$$.log
    exit 1
endif

echo "-------------------------------------------------"
echo "Finished (work_id=$work_id) ... `/bin/date`"
echo "-------------------------------------------------"
