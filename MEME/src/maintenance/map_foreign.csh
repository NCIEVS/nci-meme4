#!/bin/csh -f
#
# File:    map_foreign.csh
# Author:  David Hernandez
#
# REMARKS: This script maps foreign sources across
#          safe replacment facts.  You must update
#          the $source_list variable below.
#
set db=cheek_midp
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

#
# To find which sources should be in the list
# run this query:
#
#   select distinct source from foreign_classes
#   where eng_atom_id in
#     (select atom_id from classes where source in
#       (select previous_name from source_version));
#   minus
#   select previous_name from source_version
#   where current_name in (select source from foreign_classes);
#
set source_list="'CPT01SP'"

#################################################################
# 1. Map sources across safe-replacement facts
#    - Edit source list
#    - Report to MRD
#################################################################

$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 100000
    WHENEVER SQLERROR EXIT -1
    alter session set sort_area_size=100000000;
    alter session set hash_area_size=100000000;

    exec MEME_UTILITY.drop_it('table','tbac_$$');
    CREATE TABLE tbac_$$ AS
    SELECT DISTINCT old_atom_id, new_atom_id, b.source
    FROM mom_safe_replacement a, foreign_classes b
    WHERE eng_atom_id = old_atom_id
      AND old_atom_id != new_atom_id
      AND b.source in ($source_list)
      AND a.source in (select current_name from source_version)
      AND (a.rank,old_atom_id) in
        (SELECT max(rank),old_atom_id FROM mom_safe_replacement
         GROUP BY old_atom_id,source);

    exec MEME_UTILITY.drop_it('index','x_tbac');
    CREATE INDEX x_tbac ON tbac_$$(old_atom_id,source)
    COMPUTE STATISTICS PARALLEL;

    UPDATE foreign_classes a
    SET eng_atom_id = (SELECT new_atom_id FROM tbac_$$ b
                       WHERE eng_atom_id = old_atom_id
                        AND a.source = b.source)
    WHERE (eng_atom_id,source) IN (SELECT old_atom_id,source FROM tbac_$$);

    exec MEME_UTILITY.put_message('Finished atom_id Update');

    COMMIT;

    UPDATE foreign_classes a
    SET eng_aui =
     (SELECT aui FROM classes b
      WHERE eng_atom_id = b.atom_id)
    WHERE (eng_atom_id,source) IN
      (SELECT new_atom_id,source FROM tbac_$$);

    exec MEME_UTILITY.put_message(sql%rowcount || ' rows updated.');
    exec MEME_UTILITY.put_message('Finished AUI Update');

    COMMIT;

    exec MEME_UTILITY.put_message('Done.');
EOF
if($status != 0) then
    echo "Error"
    exit 1
endif
