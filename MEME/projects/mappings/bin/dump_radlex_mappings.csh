#!/bin/csh -f
#
# This script creates data for three tabs for an Excel spreadsheet requested for RADLEX.
#
# tab 1: CUI | RADLEX code | RADLEX PT | CUI PT [Source/Term Type] | definition | definition source
# tab 2: CUI | RADLEX code | RADLEX PT | CUI PT [Source/Term Type] | SNOMEDCT term | SNOMEDCT code
# tab 3: CUI | RADLEX code | RADLEX PT | CUI PT [Source/Term Type] | RADLEX SY
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

    -- get RADLEX CUIs, codes, atom names, atom ids, concept ids, tty and rank.

    exec MEME_UTILITY.drop_it('table','t_$$_radlex');
    create table t_$$_radlex as select last_release_cui as cui, aui, code, atom_name as name, a.atom_id, concept_id, tty, rank
    from classes c, atoms a
    where c.atom_id = a.atom_id
    and c.tobereleased in ('Y','y')
    and c.source = (select current_name from source_version where source='RADLEX');

    update t_$$_radlex tr
    set cui=
    (select cui from mrd_classes mc where tr.aui = mc.aui)
    where cui is null;

    create index x_radlex on t_$$_radlex (concept_id) compute statistics parallel;

    -- get CUI (concept) PTs for related concepts.
    -- get all atoms and delete those which don't have the max release rank.

    exec MEME_UTILITY.drop_it('table','t_$$_cui_pt');
    create table t_$$_cui_pt as
    select last_release_cui as cui, code, atom_name as name, a.atom_id, concept_id, tty, termgroup, rank, source, sui, aui, last_release_rank
    from classes c, atoms a
    where c.atom_id = a.atom_id
    and c.tobereleased in ('Y','y')
    and c.concept_id in (select concept_id from t_$$_radlex);

    -- substitute the termgroup release_rank for the termgroup name
    update t_$$_cui_pt t set termgroup =
         (select release_rank from termgroup_rank tr
          where t.termgroup = tr.termgroup);

    -- set the rank based on tbr_rank || termgroup_release_rank || last_release_rank || sui rank || aui rank
    update t_$$_cui_pt set rank = 9||to_number(termgroup)||last_release_rank||10000000000-to_number(substr(sui,2))||10000000000-to_number(substr(aui,2));

    -- remove the ones that are not max release rank per concept_id
    delete from t_$$_cui_pt where (concept_id,rank) not in
    (select concept_id,max(rank) from t_$$_cui_pt group by concept_id);

    create index x_cui_pt on t_$$_cui_pt (concept_id) compute statistics parallel;

    -- add CUI pts to RADLEX table

    alter table t_$$_radlex add (cui_pt VARCHAR2 (3000));

    update t_$$_radlex tr set cui_pt =
        (select name||' ['||source||'/'||tty||']' from t_$$_cui_pt tcp
         where tr.concept_id = tcp.concept_id);


    -- TAB 1: CUI | RADLEX code | RADLEX PT | CUI PT | definition | definition source

    -- make table of defs and index it
    exec MEME_UTILITY.drop_it('table','t_$$_defs');
    create table t_$$_defs as select concept_id,attribute_value,source
    from attributes
    where attribute_name='DEFINITION' and tobereleased in ('Y','y')
    and concept_id in (select concept_id from t_$$_radlex);

    -- deal with long attributes
    alter table t_$$_defs modify attribute_value VARCHAR2(3000);

    update t_$$_defs a set attribute_value=
        (select text_$$_value from stringtab b
         where substr(a.attribute_value,20) = string_id)
    where attribute_value like '<>Long_Attribute<>:%';

    create index x_defs on t_$$_defs (concept_id) compute statistics parallel;

    exec MEME_UTILITY.drop_it('table','t_$$_radlex_defs');
    create table t_$$_radlex_defs as
    select tr.cui, tr.code, tr.name, tr.cui_pt, d.attribute_value, d.source from
    (select concept_id, cui, code, name, cui_pt from t_$$_radlex where tty='PT') tr
    left outer join
    t_$$_defs d
    on d.concept_id = tr.concept_id
    order by to_number(substr(tr.code,4));

    -- tab 2: CUI | RADLEX code | RADLEX PT | CUI PT | SNOMEDCT term | SNOMEDCT code

    -- get SNOMEDCT terms in the same concepts as RADLEX atoms
    exec MEME_UTILITY.drop_it('table','t_$$_snomedct');
    create table t_$$_snomedct as
    select concept_id, atom_name as name, code
    from classes c, atoms a
    where c.atom_id = a.atom_id
    and c.source=(select current_name from source_version where source='SNOMEDCT')
    and c.tobereleased in ('Y','y')
    and c.concept_id in
        (select concept_id from t_$$_radlex);

    -- create concept_id index on t_$$_cui_pt
    create index x_snomedct on t_$$_snomedct (concept_id) compute statistics parallel;

    -- map from RADLEX PTs to SNOMEDCT terms
    exec MEME_UTILITY.drop_it('table','t_$$_radlex_snomedct');
    create table t_$$_radlex_snomedct as
    select tr.cui, tr.code as radlex_code, tr.name as radlex_name, tr.cui_pt,ts.name as snomedct_$$_name, ts.code as snomedct_$$_code
    from
    (select concept_id, cui, code, name, cui_pt from t_$$_radlex where tty='PT') tr
    left outer join
    t_$$_snomedct ts
    on ts.concept_id = tr.concept_id
    order by to_number(substr(tr.code,4));


    -- tab 3: CUI | RADLEX code | RADLEX PT | RADLEX SY
    -- this can be done with t_$$_radlex left outer join on itself

    exec MEME_UTILITY.drop_it('table','t_$$_radlex_sy');
    create table t_$$_radlex_sy as
    select a.cui,a.code,a.name as pt_$$_name, a.cui_pt, b.name as sy_name from
    (select concept_id, cui, code, name, cui_pt from t_$$_radlex where tty='PT') a
    left outer join
    (select concept_id, cui, code, name from t_$$_radlex where tty!='PT') b
    on a.concept_id = b.concept_id
    order by to_number(substr(a.code,4));

EOF
if ($status != 0) then
    echo "ERROR creating tables"
    cat /tmp/t.$$.log
    exit 1
endif


echo "    Dump mappings to file... `/bin/date`"
$MEME_HOME/bin/dump_mid.pl -t t_$$_radlex_defs $db .
if ($status != 0) then
    echo "ERROR dumping t_$$_radlex_defs"
    exit 1
endif

$MEME_HOME/bin/dump_mid.pl -t t_$$_radlex_snomedct $db .
if ($status != 0) then
    echo "ERROR dumping t_$$_radlex_snomedct"
    exit 1
endif

$MEME_HOME/bin/dump_mid.pl -t t_$$_radlex_sy $db .
if ($status != 0) then
    echo "ERROR dumping t_$$_radlex_sy"
    exit 1
endif

echo ""
echo "--------------------------------------------------------------"
echo "Done `/bin/date`"
echo "--------------------------------------------------------------"

