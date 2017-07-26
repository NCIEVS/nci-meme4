#!/bin/csh -f
#
# This script creates data for three tabs for an Excel spreadsheet requested of NCI.
#
# tab 1: CUI | NCI code | NCI PT | CUI PT [Source/Term Type] | definition | definition source
#

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

set u=`echo $user | cut -d\/ -f 1`

echo "    Create mappings ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1

    -- get NCI CUIs, codes, atom names, atom ids, concept ids, tty and rank.

    exec MEME_UTILITY.drop_it('table','t_$$_nci');
    create table t_$$_nci as select last_release_cui as cui, aui, code, atom_name as name, a.atom_id, concept_id, tty, rank
    from classes c, atoms a
    where c.atom_id = a.atom_id
    and c.tty='PT'
    and c.tobereleased in ('Y','y')
    and c.source = (select current_name from source_version where source='NCI');

--    update t_$$_nci tr
--    set cui=
--    (select cui from mrd_classes mc where tr.aui = mc.aui)
--    where cui is null;

    exec MEME_UTILITY.drop_it('index','x_nci');
    create index x_nci on t_$$_nci (concept_id) compute statistics parallel;



    -- TAB 1: CUI | NCI code | NCI PT | CUI PT | definition | definition source

    -- make table of defs and index it
    exec MEME_UTILITY.drop_it('table','t_$$_defs');
    create table t_$$_defs as select concept_id,attribute_value,source
    from attributes
    where attribute_name='DEFINITION' and tobereleased in ('Y','y')
    and source in (select current_name from source_version where source='NCI')
    and concept_id in (select concept_id from t_$$_nci);

    -- deal with long attributes
    alter table t_$$_defs modify attribute_value VARCHAR2(3000);

    update t_$$_defs a set attribute_value=
        (select b1.text_value||b2.text_value from stringtab b1, stringtab b2
         where substr(a.attribute_value,20) = b1.string_id
         and b1.string_id = b2.string_id
         and b1.row_sequence=1 and b2.row_sequence=2)
    where attribute_value like '<>Long_Attribute<>:%'
    and substr(a.attribute_value,20) in
        (select string_id from stringtab where row_sequence>1);

    update t_$$_defs a set attribute_value=
      (select text_value from stringtab b
       where substr(a.attribute_value,20) = string_id)
    where attribute_value like '<>Long_Attribute<>:%';
 

    exec MEME_UTILITY.drop_it('index','x_defs');
    create index x_defs on t_$$_defs (concept_id) compute statistics parallel;

    exec MEME_UTILITY.drop_it('table','t_$$_nci_defs');
    create table t_$$_nci_defs as
    select tr.cui, tr.code, tr.name, d.attribute_value, d.source from
    (select concept_id, cui, code, name from t_$$_nci where tty='PT') tr
    left outer join
    t_$$_defs d
    on d.concept_id = tr.concept_id
    order by to_number(substr(tr.code,2));

EOF
if ($status != 0) then
    echo "ERROR creating tables"
    cat /tmp/t.$$.log
    exit 1
endif


echo "    Dump mappings to file... `/bin/date`"
$MEME_HOME/bin/dump_mid.pl -t t_$$_nci_defs $db .
if ($status != 0) then
    echo "ERROR dumping t_$$_nci_defs"
    exit 1
endif


echo ""
echo "--------------------------------------------------------------"
echo "Done `/bin/date`"
echo "--------------------------------------------------------------"

