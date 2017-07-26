#!/bin/csh -f
#

set user=mth/umls_tuttle
set db=memedev

$ORACLE_HOME/bin/sqlplus $user@$db << EOF

    update mrd_attributes set root_source='NCIMTH' where root_source='MTH'
    and attribute_level = 'C';
    update mrd_relationships set root_source='NCIMTH', root_source_of_label='NCIMTH' where root_source='MTH'
    and relationship_level = 'C';

EOF
