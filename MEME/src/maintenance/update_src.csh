#! /bin/csh -f
#
# Script: update_src.csh
# Author: Brian Carlsen
#
# Usage:
#     update_src.csh <release_ver> <old_release_ver> <database>
#
# Options:
#     <release_ver>:	 Release version
#     <old_release_ver>: Old release version
#     <database>: 	 Database
#     -v version: 	 Version information
#     -h help:    	 On-line help
#
# Dependencies:
#     Requires MEME_HOME to be set
#     Requires ORACLE_HOME to be set
#
# Version Information:
#     11/19/2004 (4.2.0): Released
#     11/19/2004 (4.1.2): No longer applies to RXNORM
#     09/17/2004 (4.1.1): bequeathal rels get tbr=Y
#     07/31/2003 (4.1.0):
#
set release=4
set version=2.0
set version_date="11/19/2004"
set version_authority="BAC"

#
# Set environment (if configured)
#
if ($?ENV_FILE == 1 && $?ENV_HOME == 1) then
    source $ENV_HOME/bin/env.csh
endif

#
# Check required environment variables
#
if ($?MEME_HOME == 0) then
    echo "\$MEME_HOME must be set"
    exit 1
endif
if ($?ORACLE_HOME == 0) then
    echo "\$ORACLE_HOME must be set"
    exit 1
endif

#
# Parse arguments
#
if ($#argv == 0) then
    echo "ERROR: Bad argument"
    echo "Usage: $0 <release_ver> <old_release_ver> <database>"
    exit 1
endif
set i=1
while ($i <= $#argv)
    switch ($argv[$i])
        case '-*help':
            echo "Usage: $0 <release_ver> <old_release_ver> <database>"
            echo "Version $version, $version_date ($version_authority)"
            exit 0
        case '-v':
            echo $version
            exit 0
        case '-version':
            echo "Version $version, $version_date ($version_authority)"
            exit 0
        default:
            breaksw
    endsw
    set i=`expr $i + 1`
end

#
# Check arguments
#
if ($#argv == 3) then
    set release_ver=$1
    set old_release_ver=$2
    set db=$3
else
    echo "ERROR: Bad argument"
    echo "Usage: $0 <release_ver> <old_release_ver> <database>"
    exit 1
endif


#
# Begin program logic
#
echo "----------------------------------------------------------------"
echo "Starting $0 ... `/bin/date`"
echo "----------------------------------------------------------------"
echo "MEME_HOME:      $MEME_HOME"
echo "ORACLE_HOME:    $ORACLE_HOME"
echo "RELEASE_VER:    $release_ver"
echo "OLD_RELEASE_VER:$old_release_ver"
echo "DATABASE:       $db"
echo ""

#
# Set variables
#
set user=`$MIDSVCS_HOME/bin/get-oracle-pwd.pl`
set release_year=`echo $release_ver | sed 's/..$//'`
set old_release_year=`echo $old_release_ver | sed 's/..$//'`
set meta_year=`expr $release_year + 1`

set s_release_ver=`echo $release_ver | sed 's/^..//'`
set s_old_release_ver=`echo $old_release_ver | sed 's/^..//'`
set s_release_year=`echo $release_year | sed 's/^..//'`
set s_old_release_year=`echo $old_release_year | sed 's/^..//'`

#RXNORM removed, add back in iff needed
foreach rsource (MED MBD)

    # Set version source parameters
    if ($rsource == RXNORM) then
        set vsource = RXNORM_$s_release_ver
        set source_ver = $s_release_ver
        set old_vsource = RXNORM_$s_old_release_ver
        set old_source_ver = $s_old_release_ver
        set pt_name = "RXNORM Project, META$release_ver"
	set citation = "RxNorm work done by NLM. National Library of Medicine (NLM). Bethesda (MD): National Library of Medicine, META$release_ver release."
    else
	set vsource = ${rsource}$s_release_year
	set source_ver = $s_release_year
	set old_vsource = ${rsource}$s_old_release_year
	set old_source_ver = $s_old_release_year
	if ($rsource == MED) then
	    set start = `expr $release_year - 5`
	    set end = $release_year
	    set citation = "MEDLINE Current Files (${start}-$end). Bethesda (MD): National Library of Medicine. Contact: http://www.nlm.nih.gov."
        else
	    set start = `expr $release_year - 10`
	    set end = `expr $release_year - 6`
	    set citation = "MEDLINE Backfiles (${start}-$end). Bethesda (MD): National Library of Medicine. Contact: http://www.nlm.nih.gov."
        endif
        set pt_name = "MEDLINE (${start}-$end)"
    endif

    echo "  Fix src data for $rsource ... `/bin/date`"
    echo "    current source = $vsource"
    echo "    current version = $source_ver"
    echo "    previous source = $old_vsource"
    echo "    previous version = $old_source_ver"
    echo "    preferred name = $pt_name"
    echo "    citation = $citation"
    echo "    release year = $release_year"
    echo "    previous release year = $old_release_year"
    echo ""

    set ct=`$MEME_HOME/bin/dump_table.pl -d $db -u $user -q "SELECT COUNT(*) as ct FROM source_version WHERE source = '$rsource' AND current_name = '$vsource'"`
    if ($ct == 1) then
      echo "    $rsource already updated to vsource, continuing ..."
      continue;
    endif

    echo "    Updating source metadata ... `/bin/date`"
    #cat <<EOF >&! /tmp/t.$$.log
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        whenever sqlerror exit 1
        set serveroutput on size 100000
        set feedback off

        UPDATE source_version
        SET current_name = '$vsource', previous_name = '$old_vsource'
        WHERE source = '$rsource';

	UPDATE source_rank
	SET normalized_source = '$vsource' 
	WHERE normalized_source = '$old_vsource'
          AND source != '$old_vsource';

        INSERT INTO source_rank (
	    source, rank, restriction_level, 
	    normalized_source, stripped_source, 
	    source_family, version, notes)
        SELECT '$vsource', rank, restriction_level, '$vsource', 
	    stripped_source, source_family, '$source_ver' , notes
	FROM source_rank 
	WHERE source = '$old_vsource';

        INSERT INTO sims_info (
            source, date_created, meta_year, init_rcpt_date, 
	    clean_rcpt_date, test_insert_date,
            real_insert_date, source_contact, inverter_contact, 
	    nlm_path, apelon_path,
            inversion_script, inverter_notes_file, conserve_file, 
	    sab_list, meow_display_name,
            source_desc, status, worklist_sortkey_loc, 
	    termgroup_list, attribute_list,
            inversion_notes, release_url_list, internal_url_list, 
	    notes, inv_recipe_loc,
            suppress_edit_rec, source_official_name, 
	    source_short_name, valid_start_date,
            valid_end_date, insert_meta_version, 
	    remove_meta_version, nlm_contact,
            acquisition_contact, content_contact, 
	    license_contact, context_type, language,
            character_set, citation, latest_available, 
	    last_contacted, license_info,
            versioned_cui, root_cui, attribute_name_list, 
	    term_type_list, cui_frequency,
            term_frequency, test_insertion_start, 
	    test_insertion_end, real_insertion_start,
            real_insertion_end, editing_start, editing_end, 
	    rel_directionality_flag)
	SELECT '$vsource', sysdate, $meta_year, sysdate, sysdate, 
	    null, null, source_contact,
	    inverter_contact, nlm_path, apelon_path, null, 
	    null, null, '$vsource',
            meow_display_name, source_desc, status, null, 
	    termgroup_list, attribute_list,
            inversion_notes, release_url_list, internal_url_list, 
	    notes, inv_recipe_loc,
            suppress_edit_rec, '$pt_name', source_short_name, sysdate,
            null, '$release_ver', null, nlm_contact,
            acquisition_contact, content_contact, license_contact, 
	    context_type, language, character_set, '$citation',
            '$vsource', sysdate, license_info, null, root_cui, 
	    attribute_name_list, term_type_list, null, null, null, 
	    null, null, null, null, null, null
        FROM sims_info WHERE source = '$old_vsource';

	UPDATE sims_info
	SET remove_meta_version = '$old_release_ver',
	    valid_end_date = sysdate
        WHERE source = '$old_vsource';

EOF
    if ($status != 0) then
        /bin/cat /tmp/t.$$.log
        echo "Error updating source metadata, see /tmp/t.$$.log"
	exit 1
    endif

    echo "    Writing into classes_atoms.src ... `/bin/date`"
    /bin/cat >! classes_atoms.src <<EOF
1|SRC|SRC/VPT|V-$vsource|N|Y|N|$pt_name|N||||ENG|1|
2|SRC|SRC/VAB|V-$vsource|N|Y|N|$vsource|N||||ENG|2|
EOF

    echo "    Writing into attributes.src ... `/bin/date`"
    set hashcode = `echo -n "SEMANTIC_TYPE" | md5`
    /bin/cat >! attributes.src <<EOF
1|1|C|SEMANTIC_TYPE|Intellectual Product|SRC|R|Y|N|N|SOURCE_ATOM_ID|||3d9e88091cf4ebbab774e90c8f6d4052|
EOF

    echo "    Writing into relationships.src ... `/bin/date`"
    /bin/cat >! relationships.src <<EOF
1|S|V-$rsource|NT|version_of|1|SRC|SRC|R|Y|N|N|CODE_SOURCE|SRC|SOURCE_ATOM_ID||||
EOF

    echo "    Running load_section.csh ... `/bin/date`"
    touch termgroups.src
    touch sources.src
    $MEME_HOME/bin/load_section.csh SRC $old_vsource $vsource $rsource $db 0 3 >&! /tmp/t.$$.log
    if ($status != 0) then 
        echo "Error running load section, see /tmp/t.$$.log"
        exit 1
    endif

    echo "    Removing *.src ... `/bin/date`"
    /bin/rm -f *.src

    echo "    Cleaning up ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        whenever sqlerror exit 1
        set serveroutput on size 100000
        set feedback off

	DELETE from source_id_map
	WHERE source = '$vsource' AND source_row_id < 10;

        exec MEME_SYSTEM.truncate('mom_merge_facts');

	INSERT INTO mom_merge_facts (
	    merge_fact_id, atom_id_1, merge_level, atom_id_2, source, integrity_vector,
	    make_demotion, change_status, authority, merge_set, violations_vector,
	    status, merge_order, molecule_id, work_id)
	SELECT 1, a.atom_id, 'MAT', b.atom_id, 'SRC', '', 'N', 'N', 'SRC', 'SRC-SY', '',
	    'R', 0, 0, 0
        FROM source_classes_atoms a, source_classes_atoms b
	WHERE a.tty = 'VPT' AND b.tty = 'VAB';
EOF
    if ($status != 0) then
        echo "Error cleaning up, see /tmp/t.$$.log"
	exit 1
    endif

    echo "    Merging ... `/bin/date`"
    $MEME_HOME/bin/merge.pl SRC-SY SRC 0 $db >&! /tmp/t.$$.log
    if ($status != 0) then
        echo "Error merging, see /tmp/t.$$.log"
        exit 1
    endif

    #
    # Make old SRC concept unreleasable
    #
    echo "    Make $old_vsource SRC concept unreleasable ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -2
        set serveroutput on size 100000
        set feedback off
	exec MEME_UTILITY.drop_it('table','t1');
	CREATE TABLE t1 AS
	SELECT atom_id AS row_id
	FROM classes WHERE code = 'V-$old_vsource';
EOF
    if ($status != 0) then
        echo "Error finding atoms to make unreleasable, see /tmp/t.$$.log"
        exit 1
    endif

    $MEME_HOME/bin/batch.pl -a T -t C -s t -n n t1 $db $vsource >&! /tmp/t.$$.log
    if ($status != 0) then
        echo "Error making unreleasable, see /tmp/t.$$.log"
        exit 1
    endif

    #
    # Bequeath old versioned SRC concepts
    #
    echo "    Preparing bequethal rels ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -2
        set serveroutput on size 100000
        set feedback off

        exec MEME_UTILITY.drop_it('table','t_$old_vsource');    
        CREATE TABLE t_$old_vsource (
            row_id NUMBER );
        -- Remove MMSL01
        INSERT INTO t_$old_vsource
        SELECT atom_id FROM classes
        WHERE source = 'SRC'
          AND concept_id IN
          (SELECT concept_id FROM classes a, atoms b
          WHERE source='SRC'
            AND termgroup = 'SRC/VAB'
            AND a.atom_id = b.atom_id
            AND atom_name = '$old_vsource' );

        exec MEME_UTILITY.drop_it('table','t_rel_$old_vsource');
        CREATE TABLE t_rel_$old_vsource AS
        SELECT  concept_id_1,concept_id_2,atom_id_1,atom_id_2,
                relationship_name,relationship_attribute,
                source, source_of_label,status,generated_status,
                relationship_level,released,tobereleased,
                relationship_id, suppressible,sg_id_1,sg_type_1,sg_qualifier_1,
		sg_id_2,sg_type_2,sg_qualifier_2
        FROM relationships WHERE 1=0;

        INSERT INTO t_rel_$old_vsource
        SELECT concept_id_1,concept_id_2,0,0,
               'BRT','',
               '$vsource', '$vsource','R','Y',
               'C', 'N', 'Y', 0, 'N','','','','','',''
        FROM relationships 
        WHERE atom_id_1 IN (SELECT * FROM t_$old_vsource) 
          AND relationship_attribute = 'has_version'
          AND relationship_level = 'S'
        UNION
        SELECT concept_id_2,concept_id_1,0,0,
               'BRT','',
               '$vsource', '$vsource','R','Y',
               'C', 'N', 'Y', 0, 'N','','','','','',''
        FROM relationships 
        WHERE atom_id_2 IN (SELECT * FROM t_$old_vsource) 
          AND relationship_attribute = 'version_of'
          AND relationship_level = 'S';
EOF
    if ($status != 0) then
        echo "Error preparing bequethal rels, see /tmp/t.$$.log"
        exit 1
    endif

    echo "    Inserting bequethal rels ... `/bin/date`"
    $MEME_HOME/bin/insert.pl -rels t_rel_$old_vsource $db $vsource >&! /tmp/t.$$.log

    if ($status != 0) then
        echo "Error inserting bequethal rels, see /tmp/t.$$.log"
        exit 1
    endif

    #
    # Insert new year MED<year> MRDOC ATN entry
    #
    echo "    Inserting new MED<year> ATN MRDOC entry ... `/bin/date`"
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -2
        set serveroutput on size 100000
        set feedback off

	-- Insert MRDOC ATN row.
	INSERT INTO meme_properties 
	  (key, key_qualifier, value, description, definition,
	   example, reference) 
	SELECT 'expanded_form','ATN', 'MED$release_year',
	    'Medline citation counts from articles dated $release_year.',
	    '','',''
        FROM dual
	MINUS
	SELECT key, key_qualifier, value, description, definition,
	   example, reference
	FROM meme_properties;
EOF

    #
    # Cleanup
    #
    $ORACLE_HOME/bin/sqlplus -s $user@$db <<EOF >&! /tmp/t.$$.log
        WHENEVER SQLERROR EXIT -2
        set serveroutput on size 100000
        set feedback off
        exec MEME_UTILITY.drop_it('table','t_$old_vsource');    
        exec MEME_UTILITY.drop_it('table','t1');
EOF

end

echo "--------------------------------------------------------------"
echo "Finished `/bin/date`"
echo "--------------------------------------------------------------"
