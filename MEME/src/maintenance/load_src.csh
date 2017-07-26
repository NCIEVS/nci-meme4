#!/bin/csh -f
#
# File:   load_src.csh
# Author: Brian Carlsen
#
# Version info
# 12/01/2004 (4.5.1): additional classes_atoms.src field (last_release_cui)
# 11/19/2004 (4.5.0): Released
# 09/11/2004 (4.4.1): strings.src supports sui|isui|lui fields
# 02/05/2004 (4.4.0): Supports new retired.src file
# 12/19/2003 (4.3.0): Supports "native identifiers" in contexts.src
# 10/16/2003 (4.2.0): Released
# 07/21/2003 (4.1.1): Supports contexts.src source_rui
#                      and relationship_group fields
# 03/18/2003 (4.1.0): Ported to MEME4, 
#                     Upgraded to "rich" SRC format
set release=4
set version="5.1"
set authority="BAC";
set date="12/01/2004";

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

#
# Check required variables
#
if ($?MEME_HOME == 0) then
    echo '$MEME_HOME must be set.'
    exit 1
endif

if ($?ORACLE_HOME == 0) then
    echo '$ORACLE_HOME must be set.'
    exit 1
endif

if ($#argv > 0) then
    if ("-version" == $argv[1]) then
	echo "Release ${release}: version $version, $date ($authority)"
	exit 0
    else if ("$argv[1]" == "-v") then
	echo "$version"
	exit 0
    else if ("$argv[1]" == "--help" || "$argv[1]" == "-help") then
    cat <<EOF
 This script has the following usage:
   Usage: load_src.csh <database> <src file>

    This script loads the src file into the specified database.
    The src file must be in "classes_atoms.src","attributes.src",
	"stringtab.src", "relationships.src", "strings.src",
	"contexts.src", "mergefacts.src", "termgroups.src",
        "sources.src" "replacement.src" "retired.src"
EOF
    exit 0
    endif
endif

if ($#argv != 2) then
    echo "   Usage: load_src.csh <database> <src file>"
    exit 1
endif

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
set db=$1
set dbc="@$db"
set src_file=$2
if (!(-e $src_file)) then
    echo "$src_file does not exist."
    exit 1
endif

set dir=`dirname $src_file`
set src_file=`basename $src_file`
set cur_dir=`pwd`

if ($src_file == "classes_atoms.src") then
    set ctl_file=source_classes_atoms.ctl
    set filebase=$dir/source_classes_atoms
    cat >! $ctl_file <<EOF
options (direct=true)
unrecoverable
load data
infile 'source_classes_atoms'
badfile 'source_classes_atoms'
discardfile 'source_classes_atoms'
truncate
into table source_classes_atoms 
fields terminated by '|' 
trailing nullcols
(switch                  CONSTANT 'R',
 source_atom_id		 integer external,
 source                  char,
 termgroup		 char,
 code		 	 char,
 generated_status	 CONSTANT 'N',
 last_release_rank	 CONSTANT 0,
 status			 char,
 tobereleased            char,
 released                char,
 atom_name		 char(3000),
 suppressible		 char,
 atom_id		 CONSTANT 0,
 concept_id		 CONSTANT 0,
 source_aui              char,
 source_cui              char,
 source_dui              char,
 language                char,
 order_id                char,
 last_release_cui        char
 )
EOF
else if ($src_file == "attributes.src") then
    set ctl_file=source_attributes.ctl
    set filebase=$dir/source_attributes
    cat >! $ctl_file <<EOF
options (direct=true)
unrecoverable
load data
infile 'source_attributes'
badfile 'source_attributes'
discardfile 'source_attributes'
truncate
into table source_attributes 
fields terminated by '|' 
trailing nullcols
(switch  		CONSTANT 'U',
 source_attribute_id    integer external,
 sg_id       		char,
 attribute_level   	char,
 attribute_name    	char,
 attribute_value 	char,
 generated_status	CONSTANT 'N',
 source      		char,
 status   		char,   		
 tobereleased      	char,
 released    		char,
 suppressible      	char,
 sg_type     		char,
 sg_qualifier		char,
 atom_id		CONSTANT 0,
 concept_id		CONSTANT 0,
 attribute_id		CONSTANT 0,
 source_atui            char,
 hashcode               char
)
EOF
else if ($src_file == "relationships.src") then
    set ctl_file=source_relationships.ctl
    set filebase=$dir/source_relationships
    cat >! $ctl_file <<EOF
options (direct=true)
unrecoverable
load data
infile 'source_relationships'
badfile 'source_relationships'
discardfile 'source_relationships'
truncate
into table source_relationships 
fields terminated by '|' 
trailing nullcols
(
 switch    	 	CONSTANT 'U',
 source_rel_id   	integer external,
 relationship_level     char,
 sg_id_1   		char,
 relationship_name      char,
 relationship_attribute char,
 sg_id_2   		char,
 source    		char,
 source_of_label  	char,
 generated_status 	CONSTANT 'N',
 status    		char,
 tobereleased     	char,
 released  		char,
 suppressible     	char,
 sg_type_1		char,
 sg_qualifier_1   	char,
 sg_type_2		char,
 sg_qualifier_2   	char,
 atom_id_1		CONSTANT 0,
 atom_id_2		CONSTANT 0,
 concept_id_1		CONSTANT 0,
 concept_id_2		CONSTANT 0,
 relationship_id	CONSTANT 0,
 source_rui             char,
 relationship_group     char 
)
EOF
else if ($src_file == "strings.src") then
    set ctl_file=source_string_ui.ctl
    set filebase=$dir/source_string_ui
    cat >! $ctl_file <<EOF
options (direct=true)
unrecoverable
load data
infile 'source_string_ui'
badfile 'source_string_ui'
discardfile 'source_string_ui'
truncate
into table source_string_ui 
fields terminated by '|' 
trailing nullcols
(
 atom_id   		CONSTANT 0,
 base_string      	CONSTANT 'Y',
 string_pre		char,
 string    		char(3000),
 norm_string_pre  	char,
 norm_string      	char(3000),
 lowercase_string_pre   char,
 language  		char,
 sui                    char,
 isui                   char,
 lui                    char
)
EOF
else if ($src_file == "stringtab.src") then
    set ctl_file=source_stringtab.ctl
    set filebase=$dir/source_stringtab
    cat >! $ctl_file <<EOF
options (direct=true)
unrecoverable
load data
infile 'source_stringtab'
badfile 'source_stringtab'
discardfile 'source_stringtab'
truncate
into table source_stringtab 
fields terminated by '|' 
trailing nullcols
(string_id		integer external,
 row_sequence		integer external,
 text_total		integer external,
 text_value		char(3000)
 )
EOF
else if ($src_file == "contexts.src") then
    set ctl_file=source_context_relationships.ctl
    set filebase=$dir/source_context_relationships
    cat >! $ctl_file <<EOF
options (direct=true)
unrecoverable
load data
infile 'source_context_relationships'
badfile 'source_context_relationships'
discardfile 'source_context_relationships'
truncate
into table source_context_relationships 
fields terminated by '|' 
trailing nullcols
(
 switch    	 	CONSTANT 'U',
 source_rel_id   	CONSTANT 0,
 relationship_level     CONSTANT 'S',
 atom_id_1   		integer external,
 relationship_name      char,
 relationship_attribute char,
 atom_id_2   		integer external,
 source    		char,
 source_of_label  	char,
 generated_status 	CONSTANT 'N',
 status    		CONSTANT 'R',
 tobereleased     	CONSTANT 'Y',
 released  		CONSTANT 'N',
 suppressible     	CONSTANT 'N',
 concept_id_1		CONSTANT 0,
 concept_id_2		CONSTANT 0,
 relationship_id	CONSTANT 0,
 hierarchical_code	char,
 parent_treenum		char(1000),
 release_mode		char,
 source_rui             char,
 relationship_group     char,
 sg_id_1                char,
 sg_type_1              char,
 sg_qualifier_1         char,
 sg_id_2                char,
 sg_type_2              char,
 sg_qualifier_2         char
)
EOF
else if ($src_file == "mergefacts.src") then
    set ctl_file=mom_precomputed_facts.ctl
    set filebase=$dir/mom_precomputed_facts
    cat >! $ctl_file <<EOF
options (direct=true)
unrecoverable
load data
infile 'mom_precomputed_facts'
badfile 'mom_precomputed_facts'
discardfile 'mom_precomputed_facts'
truncate
into table mom_precomputed_facts
fields terminated by '|' 
trailing nullcols
(
 status			CONSTANT 'U',
 atom_id_1 		CONSTANT 0,
 atom_id_2 		CONSTANT 0,
 sg_id_1      		char,
 merge_level      	char,
 sg_id_2      		char,
 source    		char,
 integrity_vector 	char,
 make_demotion    	char,
 change_status    	char,
 merge_set 		char,
 sg_type_1 		char,
 sg_qualifier_1   	char,
 sg_type_2 		char,
 sg_qualifier_2   	char
 )
EOF
else if ($src_file == "termgroups.src") then
    set ctl_file=source_termgroup_rank.ctl
    set filebase=$dir/source_termgroup_rank
    cat >! $ctl_file <<EOF
options (direct=true)
unrecoverable
load data
infile 'source_termgroup_rank'
badfile 'source_termgroup_rank'
discardfile 'source_termgroup_rank'
truncate
into table source_termgroup_rank 
fields terminated by '|' 
trailing nullcols
(
 high_termgroup		char,
 low_termgroup		char,
 suppressible		char,
 exclude		char,
 norm_exclude		char,
 tty			char
 )
EOF
else if ($src_file == "sources.src") then
    set ctl_file=source_source_rank.ctl
    set filebase=$dir/source_source_rank
    cat >! $ctl_file <<EOF
options (direct=true)
unrecoverable
load data
infile 'source_source_rank'
badfile 'source_source_rank'
discardfile 'source_source_rank'
truncate
into table source_source_rank
fields terminated by '|' 
trailing nullcols
(
 high_source		char,
 low_source		char,
 restriction_level	integer external,
 normalized_source	char,
 stripped_source	char,
 version		char,
 source_family		char,
 source_official_name	char(3000),
 nlm_contact		char,
 acquisition_contact	char(1000),
 content_contact	char(1000),
 license_contact	char(1000),
 inverter_contact	char(1000),
 context_type		char,
 release_url_list	char(1000),
 language		char,
 citation		char(4000),
 license_info		char(4000),
 character_set		char,
 rel_directionality_flag char
 )
EOF
else if ($src_file == "replacement.src") then
    set ctl_file=source_replacement.ctl
    cat >! source_replacement.ctl << EOF
options (direct=true)
unrecoverable
load data
infile 'source_replacement'
badfile 'source_replacement'
discardfile 'source_replacement'
truncate
into table source_replacement 
fields terminated by '|' 
trailing nullcols
(sg_id       		char,
 sg_type     		char,
 sg_qualifier		char,
 atom_id		CONSTANT 0
)
EOF
    set filebase=$dir/source_replacement

else if ($src_file == "retired.src") then
    set ctl_file=source_replacement.ctl
    cat >! source_replacement.ctl << EOF
options (direct=true)
unrecoverable
load data
infile 'source_replacement'
badfile 'source_replacement'
discardfile 'source_replacement'
truncate
into table source_replacement 
fields terminated by '|' 
trailing nullcols
(sg_id       		char,
 sg_type     		char,
 sg_qualifier		char,
 attribute_name		char,
 hashcode		char,
 atom_id		CONSTANT 0,
 relationship_id	CONSTANT 0
)
EOF
    set filebase=$dir/source_replacement
else
    echo "Illegal src file: $src_file"
    exit 1;
endif

echo "--------------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------------"
echo "MEME_HOME:      $MEME_HOME"
echo "ORACLE_HOME:    $ORACLE_HOME"
echo "src file:       $src_file"
echo "dat file:       ${filebase}.dat"
echo "dat directory:  $dir"
echo "cwd:            $cur_dir"
echo "sqlldr command: sqlldr $user$dbc control='$ctl_file'"

mv $dir/$src_file ${filebase}.dat
cd $dir
$ORACLE_HOME/bin/sqlldr $user$dbc control="$ctl_file"
if ($status != 0) then
    echo "Error calling sqlldr"
    mv ${filebase}.dat $dir/$src_file
    exit 1
endif
cat ${filebase}.log
cd $cur_dir
mv ${filebase}.dat $dir/$src_file
if ($status != 0) then
    echo "Error calling: mv ${filebase}.dat $dir/$src_file"
    mv ${filebase}.dat $dir/$src_file
    exit 1
endif

# If log contains 'ORA-'
set ct=`/bin/fgrep -c 'ORA-' ${filebase}.log`
if ($ct > 0) then
    echo "There were errors in the sqlldr log"
    mv ${filebase}.dat $dir/$src_file
    exit 1
endif

# Cleanup
/bin/rm -f ${filebase}.log
/bin/rm -f source_*.ctl mom*ctl

echo "--------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "--------------------------------------------------------------"

exit 0
