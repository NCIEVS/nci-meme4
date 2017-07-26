#!/bin/csh -f
#
# File:   load_src.csh
# Author: Brian Carlsen
#
# Changes
# 02/24/2009 BAC (1-GCLNT): parallelize contexts loading. Fix formatting
# 03/24/2008 BAC (1-GMAC4): Needed to set table_name variable for qaStats.src
# 02/28/2008 TK (1-GMAC4): Added the option to load qaStats.src.
# 01/31/2008 BAC (1-GCLNT): Take max_id parameter and use SQL*loader sequence for meme ids.
#       also assign tty for source_classes_atoms
#       also assign string_id for source_stringtab
# 02/23/2007 BAC (1-DKO45): Change handling of contexts.src to
#   call cxt_ptr.pl, thus allowing load to include AUI parent_treenums
#   and correct RELAs.
#  11/03/2006 BAC (1-COX53): Use initial last_release_rank value of 5.
#
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
    echo 'ERROR $MEME_HOME must be set.'
    exit 1
endif

if ($?ORACLE_HOME == 0) then
    echo 'ERROR $ORACLE_HOME must be set.'
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
   Usage: load_src.csh <database> <src file> [<max_id>]

    This script loads the src file into the specified database.
    The src file must be in "classes_atoms.src","attributes.src",
        "stringtab.src", "relationships.src", "strings.src",
        "contexts.src", "mergefacts.src", "termgroups.src",
        "sources.src" "replacement.src" "retired.src"
EOF
    exit 0
    endif
endif

if ($#argv != 3 && $#argv != 2) then
    echo "   Usage: load_src.csh <database> <src file> [<max_id>]"
    exit 1
endif

set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl -d $1`
set db=$1
set dbc="@$db"
set src_file=$2

if ($#argv == 2) then
        # for backwards compat with recipe writer
        if ($src_file != "mergefacts.src") then
                echo "ERROR: max_id is only optional for mergefacts.src"
                exit 1
        endif
else
        set max_id=`expr $3 + 1`
endif

if (!(-e $src_file)) then
    echo "ERROR: $src_file does not exist."
    exit 1
endif

set dir=`dirname $src_file`
set src_file=`basename $src_file`
set cur_dir=`pwd`

if ($src_file == "classes_atoms.src") then
    set ctl_file=source_classes_atoms.ctl
    set filebase=$dir/source_classes_atoms
    set table_name=source_classes_atoms
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
 source_atom_id          integer external,
 source                  char,
 termgroup               char,
 code                    char,
 generated_status        CONSTANT 'N',
 last_release_rank       CONSTANT 5,
 status                  char,
 tobereleased            char,
 released                char,
 atom_name               char(3000),
 suppressible            char,
 atom_id                 SEQUENCE($max_id,1),
 concept_id              CONSTANT 0,
 source_aui              char,
 source_cui              char,
 source_dui              char,
 language                char,
 order_id                char,
 last_release_cui        char,
 tty                     "SUBSTR(:termgroup,INSTR(:termgroup,'/')+1)"
 )
EOF
else if ($src_file == "attributes.src") then
    set ctl_file=source_attributes.ctl
    set filebase=$dir/source_attributes
    set table_name=source_attributes
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
(switch                 CONSTANT 'U',
 source_attribute_id    integer external,
 sg_id                  char,
 attribute_level        char,
 attribute_name         char(100),
 attribute_value        char(350),
 generated_status       CONSTANT 'N',
 source                 char,
 status                 char,
 tobereleased           char,
 released               char,
 suppressible           char,
 sg_type                char,
 sg_qualifier           char,
 atom_id                CONSTANT 0,
 concept_id             CONSTANT 0,
 source_rank            CONSTANT 9999,
 attribute_id           SEQUENCE($max_id,1),
 source_atui            char,
 hashcode               char
)
EOF
else if ($src_file == "relationships.src") then
    set ctl_file=source_relationships.ctl
    set filebase=$dir/source_relationships
    set table_name=source_relationships
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
 switch                 CONSTANT 'U',
 source_rel_id          integer external,
 relationship_level     char,
 sg_id_1                char,
 relationship_name      char,
 relationship_attribute char,
 sg_id_2                char,
 source                 char,
 source_of_label        char,
 generated_status       CONSTANT 'N',
 status                 char,
 tobereleased           char,
 released               char,
 suppressible           char,
 sg_type_1              char,
 sg_qualifier_1         char,
 sg_type_2              char,
 sg_qualifier_2         char,
 atom_id_1              CONSTANT 0,
 atom_id_2              CONSTANT 0,
 concept_id_1           CONSTANT 0,
 concept_id_2           CONSTANT 0,
 source_rank            CONSTANT 9999,
 relationship_id        SEQUENCE($max_id,1),
 source_rui             char,
 relationship_group     char
)
EOF
else if ($src_file == "strings.src") then
    set ctl_file=source_string_ui.ctl
    set filebase=$dir/source_string_ui
    set table_name=source_string_ui
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
 atom_id                CONSTANT 0,
 base_string            CONSTANT 'Y',
 string_pre             char,
 string                 char(3000),
 norm_string_pre        char,
 norm_string            char(3000),
 lowercase_string_pre   char,
 language               char
)
EOF
else if ($src_file == "stringtab.src") then
    set ctl_file=source_stringtab.ctl
    set filebase=$dir/source_stringtab
    set table_name=source_stringtab
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
(string_id              "$max_id + to_number(:string_id)",
 row_sequence           integer external,
 text_total             integer external,
 text_value             char(3000)
 )
EOF
else if ($src_file == "contexts.src") then
    /bin/mv $dir/contexts.src $dir/contexts.src.bak

    #
    # Determine parallelism.  Divide into 6 parts if > 100M bytes
    #
    set lines = `cat $dir/contexts.src.bak | wc -l`
    if ($lines < 150000) then
        set part = 150000
    else
        set part = `echo "$lines 1000 + 6 / p q" | dc `
    endif
    if ($lines > 0) then
        split -l $part $dir/contexts.src.bak $dir/t$$
        foreach f ($dir/t$$*)
            $MEME_HOME/bin/cxt_ptr.pl $db $f >&! $f.src &
        end
        wait
        if ($status != 0) then
           echo "ERROR: Failed to convert parent_treenum to AUIs"
           echo "See contexts.src... original is contexts.src.bak"
           exit 1
        endif
        cat $dir/t$$*.src >! $dir/contexts.src
        /bin/rm -f $dir/t$$*
    else
        /bin/rm -f $dir/context.src
        touch $dir/contexts.src
    endif
    set ctl_file=source_context_relationships.ctl
    set filebase=$dir/source_context_relationships
    set table_name=source_context_relationships
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
 switch                 CONSTANT 'U',
 source_rel_id          CONSTANT 0,
 relationship_level     CONSTANT 'S',
 atom_id_1              integer external,
 relationship_name      char,
 relationship_attribute char,
 atom_id_2              integer external,
 source                 char,
 source_of_label        char,
 generated_status       CONSTANT 'N',
 status                 CONSTANT 'R',
 tobereleased           CONSTANT 'Y',
 released               CONSTANT 'N',
 suppressible           CONSTANT 'N',
 concept_id_1           CONSTANT 0,
 concept_id_2           CONSTANT 0,
 relationship_id        SEQUENCE($max_id,1),
 hierarchical_code      char,
 parent_treenum         char(1000),
 release_mode           char,
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
    set table_name=mom_precomputed_facts
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
 status                 CONSTANT 'U',
 atom_id_1              CONSTANT 0,
 atom_id_2              CONSTANT 0,
 sg_id_1                char,
 merge_level            char,
 sg_id_2                char,
 source                 char,
 integrity_vector       char,
 make_demotion          char,
 change_status          char,
 merge_set              char,
 sg_type_1              char,
 sg_qualifier_1         char,
 sg_type_2              char,
 sg_qualifier_2         char
 )
EOF
else if ($src_file == "termgroups.src") then
    set ctl_file=source_termgroup_rank.ctl
    set filebase=$dir/source_termgroup_rank
    set table_name=source_termgroup_rank
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
 high_termgroup         char,
 low_termgroup          char,
 suppressible           char,
 exclude                char,
 norm_exclude           char,
 tty                    char
 )
EOF
else if ($src_file == "sources.src") then
    set ctl_file=source_source_rank.ctl
    set filebase=$dir/source_source_rank
    set table_name=source_source_rank
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
 high_source            char,
 low_source             char,
 restriction_level      integer external,
 normalized_source      char,
 stripped_source        char,
 version                char,
 source_family          char,
 source_official_name   char(3000),
 nlm_contact            char,
 acquisition_contact    char(1000),
 content_contact        char(1000),
 license_contact        char(1000),
 inverter_contact       char(1000),
 context_type           char,
 release_url_list       char(1000),
 language               char,
 citation               char(4000),
 license_info           char(4000),
 character_set          char,
 rel_directionality_flag char
 )
EOF
else if ($src_file == "replacement.src") then
    set ctl_file=source_replacement.ctl
    set table_name=source_replacement
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
(sg_id                  char,
 sg_type                char,
 sg_qualifier           char,
 atom_id                CONSTANT 0
)
EOF
    set filebase=$dir/source_replacement

else if ($src_file == "retired.src") then
    set ctl_file=source_replacement.ctl
    set table_name=source_replacement
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
(sg_id                  char,
 sg_type                char,
 sg_qualifier           char,
 attribute_name         char,
 hashcode               char,
 atom_id                CONSTANT 0,
 relationship_id        CONSTANT 0
)
EOF
    set filebase=$dir/source_replacement
else if ($src_file == "qaStats.src") then
    set ctl_file=qaStats.ctl
    set table_name=inv_qa_results
    cat >! qaStats.ctl << EOF
options (direct=true)
unrecoverable
load data
infile 'qaStats'
badfile 'qaStats'
discardfile 'qaStats'
append
into table inv_qa_results
fields terminated by '|'
trailing nullcols
( qa_name                        filler char,
         qa_id                   CONSTANT 1002,
        name                    char,
        value                   char,
        qa_count                integer external,
        timestamp               sysdate
)
EOF
    set filebase=$dir/qaStats
else
    echo "ERROR: Illegal src file: $src_file"
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
echo "table_name:     $table_name"
echo "cwd:            $cur_dir"
echo "sqlldr command: sqlldr $user$dbc control='$ctl_file'"

mv $dir/$src_file ${filebase}.dat
cd $dir
$ORACLE_HOME/bin/sqlldr $user$dbc control="$ctl_file"
if ($status != 0) then
    echo "ERROR calling sqlldr"
    mv ${filebase}.dat $dir/$src_file
    exit 1
endif
cat ${filebase}.log
cd $cur_dir
mv ${filebase}.dat $dir/$src_file
if ($status != 0) then
    echo "ERROR calling: mv ${filebase}.dat $dir/$src_file"
    mv ${filebase}.dat $dir/$src_file
    exit 1
endif

# If log contains 'ORA-'
set ct=`/bin/fgrep -c 'ORA-' ${filebase}.log`
if ($ct > 0) then
    echo "ERROR There were errors in the sqlldr log"
    mv ${filebase}.dat $dir/$src_file
    exit 1
endif

# Compute statistics
echo "   Analyze $table_name ...`/bin/date`"
$ORACLE_HOME/bin/sqlplus $user$dbc <<EOF >&! /tmp/t.$$.log
    WHENEVER SQLERROR EXIT -1
    exec MEME_SYSTEM.analyze('$table_name');
EOF
if ($status != 0) then
    echo "ERROR calling sqlplus"
    cat /tmp/t.$$.log
    exit 1
endif

# Cleanup
/bin/rm -f ${filebase}.log
/bin/rm -f $ctl_file


#
# Rename .bak file if all went well
#
if ($src_file == "contexts.src") then
  /bin/mv -f $dir/contexts.src.bak $dir/contexts.src
endif
echo "--------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "--------------------------------------------------------------"

exit 0
