#!/bin/csh -f
#
# File:    gen_obsolete_uis.csh
# Author:  Joanne Wong (joanne.f.wong@lmco.com)
#
# This script creates a report of obsolete UIs for
# potential cleanup.
#
#
# Changes
# 08/28/2007 BAC (1-EL38F): Need to account for inverse R and inverse CR.
# 07/31/2007 BAC (1-EL38F): Separate R and CR sections were making inaccurate results, consolidate them.
# 06/28/2007 JFW (1-EL38F): First Version
#
if ($?MIDSVCS_HOME == 0) then
    echo '$MIDSVCS_HOME must be set'
    exit 1
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set'
    exit 1
endif

if ($#argv != 1) then
    echo "Usage: $0 <database>"
    exit 1
else if ($#argv == 1) then
    set db=$1
endif

echo "--------------------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "--------------------------------------------------------------"
echo "db:         $db"
echo ""
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $db`

#
# Generate obsolete UI table
#
echo "    Generate obsolete UI table ... `/bin/date`"
$ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF
    set serveroutput on size 1000000
    set feedback off
    WHENEVER SQLERROR EXIT -1
   
    --
    -- start clean
    --
    exec MEME_SYSTEM.truncate('obsolete_ui');

    --
    -- handle obsolete AUI
    --
    exec MEME_UTILITY.put_message('Handle Atoms');
    INSERT INTO obsolete_ui
      (type, ct, root_source, tty,
       code_flag, saui_flag, scui_flag, sdui_flag,
       relationship_name, relationship_attribute,
       sg_type_1, sg_type_2, attribute_name, sg_type)
    SELECT 'AUI', COUNT(*), stripped_source as root_source, tty,
        decode(code,null,'N','CODE') code_flag,
        decode(source_aui,null,'N','SAUI') saui_flag,
        decode(source_cui,null,'N','SCUI') scui_flag,
        decode(source_dui,null,'N','SDUI') sdui_flag,
        '','','','','',''
    FROM atoms_ui
    WHERE (stripped_source, tty,
           decode(code,null,'N','CODE'),
           decode(source_aui,null,'N','SAUI'),
           decode(source_cui,null,'N','SCUI'),
           decode(source_dui,null,'N','SDUI')) IN
       (SELECT stripped_source AS root_source, tty,
               decode(code,null,'N','CODE') code_flag,
               decode(source_aui,null,'N','SAUI') saui_flag,
               decode(source_cui,null,'N','SCUI') scui_flag,
               decode(source_dui,null,'N','SDUI') sdui_flag 
        FROM atoms_ui
        MINUS
        (SELECT /*+ PARALLEL(a,2) */ b.source root_source, tty,
             decode(code,null,'N','CODE'),
             decode(source_aui,null,'N','SAUI'),
             decode(source_cui,null,'N','SCUI'),
             decode(source_dui,null,'N','SDUI')
         FROM classes a, source_version b
         WHERE a.source=current_name
           AND tobereleased not in ('N','n')
         UNION ALL
         SELECT /*+ PARALLEL(a,2) */ b.source root_source, tty,
             decode(code,null,'N','CODE'),
             decode(source_aui,null,'N','SAUI'),
             decode(source_cui,null,'N','SCUI'),
             decode(source_dui,null,'N','SDUI')
             from foreign_classes a, source_version b 
         WHERE a.source=current_name
           AND tobereleased not in ('N','n')))
    GROUP BY stripped_source, tty,
     decode(code,null,'N','CODE'),
     decode(source_aui,null,'N','SAUI'),
     decode(source_cui,null,'N','SCUI'),
     decode(source_dui,null,'N','SDUI');

    --
    -- handle obsolete RUI (R, CR)
    --
    exec MEME_UTILITY.put_message('Handle Relationships, Context Relationships');
    INSERT INTO obsolete_ui
      (type, ct, root_source, tty,
       code_flag, saui_flag, scui_flag, sdui_flag,
       relationship_name, relationship_attribute,
       sg_type_1, sg_type_2, attribute_name, sg_type)
    SELECT 'RUI', count(*),root_source, '',
        '','','','', relationship_name,
    	relationship_attribute, sg_type_1, sg_type_2, 
    	'',''
    FROM relationships_ui
    WHERE (root_source, relationship_name,
           nvl(relationship_attribute,'null'), 
    	   sg_type_1, sg_type_2) IN
         (SELECT root_source, relationship_name,
                 nvl(relationship_attribute,'null'),
                 sg_type_1, sg_type_2
          FROM relationships_ui
          MINUS 
          SELECT b.source root_source, relationship_name, 
                 nvl(relationship_attribute,'null'),
                 sg_type_1, sg_type_2
          FROM relationships a, source_version b
          WHERE a.source=current_name AND tobereleased NOT IN ('N','n')
          MINUS
          SELECT b.source root_source, inverse_name, 
                 nvl(inverse_rel_attribute,'null'),
                 sg_type_1, sg_type_2
          FROM relationships a, source_version b, 
                 inverse_relationships c, inverse_rel_attributes d
          WHERE a.source=current_name AND tobereleased NOT IN ('N','n')
             AND a.relationship_name = c.relationship_name
             AND nvl(a.relationship_attribute,'n') = nvl(d.relationship_attribute,'n')
          MINUS          
          SELECT b.source root_source, relationship_name,
                nvl(relationship_attribute,'null'),
                sg_type_1, sg_type_2
          FROM context_relationships a, source_version b
          WHERE a.source=current_name AND tobereleased NOT IN ('N','n')
          MINUS
          SELECT b.source root_source, inverse_name,
                nvl(inverse_rel_attribute,'null'),
                sg_type_1, sg_type_2
          FROM context_relationships a, source_version b,
                 inverse_relationships c, inverse_rel_attributes d
          WHERE a.source=current_name AND tobereleased NOT IN ('N','n')
             AND a.relationship_name = c.relationship_name
             AND nvl(a.relationship_attribute,'n') = nvl(d.relationship_attribute,'n')
         )
    GROUP BY root_source, relationship_name, relationship_attribute, 
             sg_type_1, sg_type_2;


    --
    -- handle obsolete ATUI
    --
    exec MEME_UTILITY.put_message('Handle Attributes');
    INSERT INTO obsolete_ui
      (type, ct, root_source, tty,
       code_flag, saui_flag, scui_flag, sdui_flag,
       relationship_name, relationship_attribute,
       sg_type_1, sg_type_2, attribute_name, sg_type)
    SELECT 'ATUI', COUNT(*), root_source, '',
           '','','','', '','','','',
           attribute_name, sg_type
    FROM attributes_ui where (root_source, attribute_name, sg_type) IN
        (SELECT /*+ PARALLEL(a) */ root_source, attribute_name, sg_type 
         FROM attributes_ui a
         MINUS
         SELECT /*+ PARALLEL(a) */ b.source root_source, attribute_name, sg_type
         FROM attributes a, source_version b 
         WHERE a.source=current_name AND tobereleased not in ('N','n'))
    GROUP BY root_source, attribute_name, sg_type;

EOF
if ($status != 0) then
    echo "Error generating obsolete UI table"
    exit 1
endif
 
#
# Output report
#
echo "    Creating report (obsolete.ui.rpt) ... `/bin/date`"
$MEME_HOME/bin/dump_table.pl -q "select * from obsolete_ui order by 1,2 desc" -d $db -u mth >&! obsolete.ui.rpt
if ($status != 0) then
    echo "Error generating obsolete UI report`"
    exit 1
endif

echo "--------------------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "--------------------------------------------------------------"
