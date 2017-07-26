CREATE OR REPLACE PACKAGE MEME_SOURCE_PROCESSING AS
/*******************************************************************************
 *
 * PL/SQL File: meme_source_processing.sql
 *
 * This package contains procedures
 * to perform SOURCE PROCESSING operations
 * 
 * Changes
 * 08/03/2012 AR:  Removed PARALLEL hints from "source_replacment" and "assign_atuis"
 * 03/15/2012 PM : Optimized source_replacement() for attributes and context_relationships.
 * 03/07/2012 BAC (artf1424): remove parallel hints from sections that were very slow during SCT insertion
 * 12/15/2011 BAC (artf1344): improve performance of map_sg_fields_all for SRUI cases
 * 08/30/2010 BAC (1-Q6GN9): One more bug fix in delete_demotions that leaves too many demotions around.
 * 05/26/2010 BAC (1-Q6GN9): Fix bug in delete_demotions that leaves too many demotions around.
 * 05/06/2009 BAC (1-GCLINT): Remove ORACLE PARALLEL hint in queries from assign_atuis and assign_ruis.
 * 04/17/2009 BAC (1-GCLINT): Allow insert_ranks to run where the source.src or termgroups.src
 *     contain references to sources/termgroups already inserted.
 *  02/24/2009 BAC (1-GCLINT): 
 *     Avoid analyzing tables
 *     Parallelize assign_atuis "normalizing uis" routine
 *     Improve performance of assign_ruis
 *     Support more logging statements for better tracking
 *     Improve assign_string_uis performance
 *     Avoid creating indexes (they already exist now)
 *     Remove atom ordering from assign meme ids
 *     Improve source_replacement performance (optimized queries)
 *     Improve update_releasability performance by avoiding actions
 *     Improve delete_demotions performance
 *     Improve handling of sg_meme_id/data_type for rels
 *     Avoid creating/removing insertion indexes
 *  09/05/2008 BAC (1-IKI05): Fixed query that updates ids in src_qa_samples during source_replacement.
 *  01/31/2008 BAC (1-GCLNT): Insertion improvements (works with updated load_src.csh).
 *  01/24/2008 TTN (1-GAF41): STY ATUI changes - do not assign ATUIs for STY and NON_HUMAN attributes during insertion 
 *  10/17/2007 JFW (1-FJ4OX): Add set_atom_ordering procedure
 *  09/10/2007 JFW (1-FHBK3): Properly update source_id_map for R during source_replacement
 *  09/10/2007 JFW (1-DBSLY): Remove hard-coded references to L0028429 (null LUI)
 *  09/06/2007 JFW (1-F6VI1): Add source_classes_atoms to "Set official name" in insert_ranks
 *  08/20/2007 SL  (1-F1AMN): Removing the VSAB dependency on stripped source.
 *  06/14/2007 JFW (1-EHXLB): Add "Set official name" functionality to insert_ranks 
 *  03/30/2007 JFW (1-DYD4R): reinstate ADD_HASH without PARALLEL hints in source_replacement
 *  03/02/2007 TTN (1-D3BQF): add SRC_REL_ID sg_type
 *  12/22/2006 BAC (1-D44YB): fix to map_sg_fields for 'CUI' types.
 *  02/28/2007 BAC (1-DM5MR): safe_replacement needs to use string_clause.
 *  02/23/2007 BAC (1-DKO45): assign_meme_ids no longer reverses RELAs for 'CR'
 *                            map_to_meme_ids no longer maps 'CR' parent_treenum
 *  12/22/2006 BAC (1-D44YB): fix to map_sg_fields for 'CUI' types.
 *  11/16/2006 BAC (1-CTST3): Patch to ISUI,LUI query of assign_string_uis to accommodate
 *                            LVG bug that allows same ISUI to have >1 LUI.
 *  10/30/2006 BAC (1-CNG9V): Bug in "new" AUI and RUI reporting was fixed.
 *  08/31/2006 TTN(1-C261E): use the ranking algorithm from MEME_RANKS
 *  08/29/2006 SL (1-C17ND)  Adding Oracle10g performance like analyze staments
 *  08/08/2006 BAC(1-BV4YH): delete_demotions no longer allows C level rels
 *                           to be approved if a demotion still exists.
 *  08/01/2006 BAC (1-BTDSF): Support SRC_ATOM_ID qualifiers: CODE,SOURCE_[ACD]UI
 *  06/09/2006 BAC (noticket): minor cleanup with tmp4||'_2' table in replacement_merges
 *  05/30/2006 BAC (1-BCQ39): map_obsolete_rels allowed to move "mappable" types.  
 *                            map_sg_data may remap them later, but this saves some strife here.
 *  05/03/2006 BAC (1-B2U2L): Minor map_obsolete_rels changes to account for source_replacement
 *                            and no_remap_sg_type logic.
 *  03/20/2006 BAC (1-AQDMZ): source_repl for 'A' handles SEMANTIC_TYPE properly
 *  03/09/2006 BAC (1-AMQFF): Make SRC rels connected to old version SRC/V* atoms
 *     unreleasable in update_releasability
 *  02/03/2006 RBE (1-76X3H): Change requests to this procedure to add new parameter 
 *     set_ranks
 *  02/01/2006 BAC (1-76JTS): assign_string_ids joins also on language at location 300
 *     when updating source_classes_atoms from source_string_ui
 *  01/24/2006 BAC (1-754Y3): resolve_stys uses ||'' to avoid attribute_name index
 *     lookups when finding SEMANTIC_TYPE attributes
 *
 * Version Information
 * 12/22/2005 3.62.2: (1-719S2): add check to assign_meme_ids('A',...) to prevent
 *                    procedure from running unless min(string_id) in source_stringtab = 1
 * 12/12/2005 3.62.1: CREATE INDEX ... now use TABLESPACE MIDI also
 * 10/27/2005 3.62.0: assign_ruis turns off relationships connected to unreleasable atoms.
 *                    assign_atuis turns off attributes connected to unreleasable atoms.
 * 09/26/2005 3.61.4: resolve_stys limits SAB to 20 chars, should resolve E-SAB issue for long SABs.
 * 09/??/2005 3.61.3: Variety of changes including progress monitoring, etc.
 * 08/24/2005 3.61.2: map_sg_fields doesn't check s_c_a for 'AUI' types, 
 *		      rather assumes that they've already been inserted. 
 * 08/08/2005 3.61.1: map_sg_fields operates first on s_c_a but checks switch flag
 *                    This should guarantee that highest ranking atom is always attached to
 * 08/02/2005 3.61.0: Released
 * 07/15/2005 3.60.8: core_table_insert('C') sets classes ranks
 *                    so map_sg_fields has accurate data to work with
 * 07/14/2005 3.60.7: improvements to mapping SOURCE_RUI.
 * 06/23/2005 3.60.6: code for setting s_c_a suppressible is in
 *                    assign_meme_ids now, not core_table insert
 *                    that way it is available to source_replacement
 *                    also, this way it is no longer required in f_c_i
 *                    source_replacement now requires matchings suppressible
 *                    to consider atom/attribute/rel source replacement 
 * 06/22/2005 3.60.5: report_tables strips '.' from SAB names when building
 *                    new tables
 * 06/21/2005 3.60.4: Only rebuild atom_ordering if there are >50K rows
 *                    core_table_insert, foreign_classes_insert handle
 *                    suppressibility='O'.
 * 06/20/2005 3.60.3: delete_demotions removes demotions regardless of
 *                    the safe-replacement fact source
 * 06/07/2005 3.60.2: safe_replacement does a better job handling variations
 *                    in CUI prefixes and length when computing safe 
 *                    replacement fact rankings
 * 05/26/2005 3.60.1: Minor changes to assign_atuis, assign_ruis, only update
 *                    atui where the attribute_id is in mapping table, or
 *                    only update rui where relationship_id is in the mapping
 *                    table.
 * 05/23/2005 3.60.0: Released
 * 05/17/2005 3.59.1: Fixed core_table_insert to not insert concepts
 *                    where atoms were source replaced.
 * 04/18/2005 3.59.0: Released
 * 04/14/2005 3.58.3: Moved code to set suppressible in s_c_a to 
 *                    core_table_insert.  Added code to maintain suppressible
 * 		      in classes in the cases where an termgroup changes
 *                    suppressibility from one version to the next.
 * 04/07/2005 3.58.2: Bug in CODE_ROOT_TERMGROUP code in assign_rui causing
 *                    sg_qualifier_2 to be set to null.
 * 03/30/2005 3.58.1: 
 * 03/25/2005 3.58.0: Released, Support AUI map type
 * 03/16/2005 3.57.2: Better source_replaement(C) performance
 * 03/16/2005 3.57.1: Better maintenance of atom_ordering
 * 03/11/2005 3.57.0: Supports CUI_SOURCE, safe_replacement variables
 *                    made larger to accommodate big termgroup lists
 *                    Bug fix 400a, better sampling
 * 02/15/2005 3.56.1: Supports CUI_SOURCE, safe_replacement variables
 *                    made larger to accommodate big termgroup lists
 * 12/30/2004 3.56.0: Released
 * 12/29/2004 3.55.4: Removed report_table_changed references.
 *                    Updated log_operation comments.
 * 12/22/2004 3.55.3: Merge_level now takes into account tty and string
 *                    matches and orders appropriately.  
 *                    The norm exclude filter was broken due to a bug
 *                    in the previous merge level computation that would
 *                    make exact string matches look like norm string matches.
 * 12/20/2004 3.55.2: generate_facts takes tty, sdui, scui, saui,
 *                    safe_replacement takes tty, sdui, scui, saui
 * 12/14/2004 3.55.1: Bug fix to replacement_merges, ambig_flag not being
 *                     handled properly
 * 12/13/2004 3.55.0: Released
 * 12/08/2004 3.54.3: loading merge facts now puts TTY matches ahead of none.
 * 12/01/2004 3.54.2: Remove LAST_RELEASE_CUI mechanism, better asextension of 
 *                    classes_atoms.src
 * 11/24/2004 3.54.1: Better support for CODE_ROOT_{SOURCE,TERMGROUP}, 
 *                    mapping thru source_classes_atoms is enabled.
 *                    Also UI assignments now data-driven for format 
 *                    (SUI,LUI,ISUI,AUI,CUI,RUI,ATUI)
 *                    All termgroup_rank references use release_rank
 * 11/19/2004 3.54.0: Released
 * 11/18/2004 3.53.1: insert_ranks inserts basic sims_info row if none exists.
 *                    bug fix to replacement merges
 *                    NLM02 AUI exception removed.
 *                    NLM03 RUI exception removed (NLM03 data not batch loaded)
 *                    Set classes.last_release_cui (by atom_id/concept_id)
 *                    from source_attributes 'LAST_RELEASE_CUI' attributes.
 * 10/06/2004 3.53.0: Released
 *                    Added SRC concepts to <SAB>_sample algorithm
 * 10/05/2004 3.52.2: source_replacement fix to prevent things like
 *                    ROOT_SOURCE_AUI from attaching to a previous version
 *                    higher termgroup atom instead of a current version
 *                    lower termgroup atom (same for ROOT_SOURCE_RUI).
 *                    For classes/relationships source_replacement now
 *                    fakes the rank of the previous version atoms and rels
 *                    to appear as though already unreleasable.
 * 09/17/2004 3.52.1: Cleaned up queries and comments, improved parallelization
 * 08/19/2004 3.52.0: Released, fixes for sampling and source_rank +1 issue
 * 08/09/2004 3.51.0: Released
 * 08/04/2004 3.50.1: added <SAB>_sample table to REPORT_TABLES procedure
 * 07/16/2004 3.50.0: Insert into termgroup_rank.release_rank in insert_ranks
 *		      assign_meme_ids now checks termgroup rank is set.
 *                    Released.
 * 07/07/2004 3.49.0: Released
 * 06/23/2004 3.48.2: insert_ranks now ranks things with the same root
 *                    source at the same level (also with termgroups)
 *                    bug fix for attributes.
 * 06/15/2004 3.48.1: source_replacement more efficient.  Assign_atuis
 *                    uses CONCEPT_ID sg_type for SEMANTIC_TYPE.
 * 06/09/2004 3.48.0: Perf improvement to assign_auis, code review
 *                    of source_replacement.  Released
 * 05/19/2004 3.47.0: Released
 * 05/03/2004 3.46.1: Minor improvements.  Delete_demotions preserves
 *                    C level rel status.
 * 05/03/2004 3.46.0: Bug Fix
 * 04/30/2004 3.45.1: Bug Fix
 * 04/28/2004 3.45.0: Released
 * 04/26/2004 3.44.1: AUI assignment takes tbr into account.
 * 04/19/2004 3.44.0: Released
 * 04/15/2004 3.43.2: Improvements to map_sg_fields (ROOT_SOURCE)
 * 04/01/2004 3.43.1: insert into foreign_classes now uses source_aui, etc..
 * 03/29/2004 3.43.0: insert into foreign_classes now uses source_aui, etc..
 * 03/19/2004 3.42.1: map_to_meme_ids uses map_sg_fields_all
 * 03/17/2004 3.42.0: Various changes.  Updated source_replacement, 
 * 		      update_releasasbility turns off all old version stuff.
 * 03/08/2004 3.41.0: Various changes.  Updated source_replacement, 
 * 		      update_releasasbility turns off all old version stuff.
 *
 * 02/05/2004 3.40.1: New procedure (map_sg_fields_to_rels) for mapping 
 *                    RUI sg_types to relationship_ids
 * 01/20/2004 3.40.0: Released
 * 12/29/2003 3.39.1: Again, assign_atui,rui were optimized
 * 12/19/2003 3.39.0: Again, assign_atui,rui were optimized
 * 12/15/2003 3.38.0: Released
 * 12/05/2003 3.37.1: assign_atui and assign_rui were optimized. 
 *                    source_replacement handles replacement attributes 
 *                    (section 1b)
 * 12/01/2003 3.37.0: Released
 * 11/26/2003 3.36.1: map_sg_data calls map_sg_fields with qa_flag=>'N'
 * 10/22/2003 3.36.0: Released
 * 10/17/2003 3.35.2: replacement_merges no longer analyzes 
 *                    source_classes_atoms because it should already
 *                    be analyzed.
 * 10/15/2003 3.35.1: source_version maintained better with respect to
 *                    new sources by insert_ranks
 * 10/14/2003 3.35.0: Fix to map_sg_fields to fix ambiguous column errors
 * 10/09/2003 3.34.0: Released
 * 10/03/2003 3.33.3: Small fix to assign_atuis, the assigning of 
 *                    sg_type='CONCEPT_ID'
 *		      was using != on a potentiall null value without an NVL
 * 10/02/2003 3.33.2: bug fix: resolve_stys
 * 10/01/2003 3.33.1: Supports ROOT_SOURCE_[CADR]UI id types
 * 09/30/2003 3.33.0: Released
 * 09/2003    3.32.2: generate_facts and safe_replacement allow
 *                    source_cui clauses.  replacement_merges
 *                    only allows merges from new-old concepts if
 *                    the merging is "1-1".  For N-N there is no
 *                    additional restriction.
 *                    Further optimizations to assign* procedures.
 * 07/22/2003 3.32.1: optimizations to assign_* procedures,
 *                    +foreign_attributes_insert, additional types
 *                    supported in map_sg_types
 * 05/28/2003 3.32.0: assign_atui, assign_rui, fix to map_obsolete_rels
 * 05/16/2003 3.31.0: Fix to update_releasability
 * 05/14/2003 3.30.1: Fix to update_releasability
 * 05/12/2003 3.30.0: Released
 * 05/07/2003 3.29.2: support sg_ fields in attributes/relationships
 * 04/09/2003 3.29.1: core_table_insert maintains atom_ordering.
 * 03/27/2003 3.29.0: Small fix to sims_info field names. Released
 * 03/25/2003 3.28.0: map_to_meme_ids ('CR') updated to map parent tree numbers
 *                    to AUIs instead of meme atom_ids.
 *                    Released.
 * 03/19/2003 3.27.2: Small fixes to source_replacement,
 *                    Update releasability only updates if the
 *                    tobereleased is not already set
 * 03/04/2003 3.27.1: no longer track source_attribute/source_relationship ids
 *                    foreign_classes gets eng_aui
 * 02/18/2003 3.27.0: Released.
 * 02/13/2003 3.26.3: Fixed NEW_ATOMS (finally).
 * 01/16/2003 3.26.2: Code to handle source_replacement table. changes
 * 		  	to map_to_meme_ids, and +source_replacement
 * 12/18/2002 3.26.1: load_facts improvement.  Get rid of redundant facts.
 * 12/17/2002 3.26.0: bug-fix to safe_replacement "suppressible" section.
 *		      the CREATE TABLE Query was bad. RELEASED
 * 12/02/2002 3.25.0: AUI logic updated to assume null codes for NLM02
 * 		      Inherit suppressible=Y across safe-replacement facts
 *		      where old and new have same ISUI and old is suppressible
 *                    In core_table_insert, TTY is set to termgroup after '/'
 *                    if it is null in source_classes_atoms.
 * 11/27/2002 3.24.0: Cleaned up map_obsolete_rels code.
 * 11/04/2002 3.23.0: Released
 * 10/28/2002 3.22.1: bug fix to CR update_releasability section.
 *                    resolve_stys won't fail on no rows.
 *                    update_releasability doesn't fail if a source
 *                    has no relationships.
 * 10/24/2002 3.22.0: replacement_merges supports n_to_n.
 *                    update_releasability turns off old versioned SRC atoms
 *                    and turns off old source context relationships.
 * 09/09/2002 3.21.0: safe_replacement fixes t_sr_lrr query to user varchar2 
 *                    instead of number. Cleaned up exception handling
 *                    routines.
 * 09/06/2002 3.20.0: foreign_classes gets tty,aui when inserted.
 *		      Released
 * 08/26/2002 3.19.2: +assign_auis
 * 08/26/2002 3.19.1: filter_facts supports SEMANTIC_TYPE, SOURCE, TERMGROUP,
 *                    and STATUS filtering.
 * 08/16/2002 3.19.0: filter_facts did not have a NON_CHEM_STY section
 *                    for type_2, instead it had two CHEM_STY sections.
 * 		      this was fixed.
 * 07/28/2002 3.18.0: safe_replacement algorith was updated to map
 *                    both last_release_rank AND last_release_cui
 *                    across safe-replacement facts.  It chooses
 *                    only the highest ranking fact for each new_atom_id.
 * 05/28/2002 3.17.0: source_rank now has language field, this is supported
 *                    by insert_ranks.
 *                    Released.
 * 05/15/2002 3.16.3: map_sg_fields does not need to map across 
 *                    safe-replacement facts.
 * 05/13/2002 3.16.2: insert_source_ids only does it where source id !=0
 * 04/15/2002 3.16.1: filter_facts fixed NEW_ATOMS section.
 * 02/28/2002 3.16.0: Released.
 * 2/25/2002  3.15.2: safe_replacement cannot use temporary table name
 *		      called mom_replace_<source> because of source's
 *		      with names like MSH2002-02-10
 * 	              report_tables was changed to eliminate 
 *                    dash characters in source names when creating
 *                    report tables.
 * 2/22/2002  3.15.1: For MSH insertion, we need to commit in map_sg_fields
 *  		      otherwise, log fills up.
 * 12/14/2001 3.14.5: all calls to MEME_BATCH_ACTIONS.batch_action
 *		      were converted to MEME_BATCH_ACTIONS.macro_action.
 *		      all calls to this involving the classes table
 *		      were augmented with set_preferred_flag=>'N'
 *		      since we know we will be setting preference later
 *  		      during an insertion.  This should significantly
 *		      improve insertion of classes, calculation of status
 *		      during safe replacement, and the update_releasability
 *		      procedure.  RELEASED.
 * 10/30/2001 3.14.4: parent_treenum variables in map-to_meme_ids were
 * 		      extended to 512 chars.
 * 10/16/2001 3.14.3: load_facts has better location tracking
 * 10/04/2001 3.14.2: Fix to replacement_merges in "daisy chain" managment code
 *		      make sure and update tmp4 only when status=R
 * 09/25/2001 3.14.1: in safe_replacement when updating lrr,
 *		      only update where the value is changing.
 * 09/05/2001 3.14.0: Release 13.* changes
 * 09/05/2001 3.13.2: insert_ranks supports context_type field
 *
 * 07/19/2001 3.13.1: assign_string_uis upgraded to better support
 *  			sui,isui,lui semantics for foreign strings.
 * 07/18/2001 3.13.0: Released.
 * 07/11/2001 3.12.1: insert_ranks was upgraded to insert ALL fields from
 * 			source_source_rank into source_rank.  This change
 *			is completely backwards compatible
 * 06/04/2001 3.12.0: Released.
 * 05/17/2001 3.11.2: +foreign_classes_insert: for inserting foreign
 *			classes.
 * 04/24/2001 3.11.1: update_releasability should avoid sg_* data because
 *                    it may cause CUI relationships to become unreleasable
 *                    when map_sg_data could remap the rel/attribute to 
 *		      another atom.
 * 04/20/2001 3.11.0: Released to NLM                    
 * 04/18/2001 3.10.4: changes map_sg_fields to join query for mapping 
 *                    CUI and CUI_STRIPPED_SOURCE;
 *                    replaces mapping of concept_ids which atom_ids != 0 to
 *                    handle every case at once.
 * 04/13/2001 3.10.3: changes map_sg_fields to map additional fields.
 * 04/09/2001 3.10.2: changes map_to_meme_ids to convert a source_atom_id tree 
 *                    number format to atom_ids
 * 04/06/2001 3.10.1: map_sg_data should NOT re-activate tbr=n rels/attributes
 * 03/28/2001 3.10.0: Released version 
 * 03/20/2001 3.9.2:  changes to assign_string_ui to support foreign language.
 * 03/15/2001 3.9.1:  changes to safe_replacement, update_releasability,
 * 03/15/2001 3.9.1:  changes to safe_replacement, update_releasability,
 *                    resolve_stys, core_table_in2sert, map_obsolete_rels,
 *                    map_sg_data, prepare_src_mergefacts to log transactions.
 *                    created delete_demotions.
 * 02/14/2001 3.9.0:  Released
 * 02/12/2001 3.8.5:  filter_facts: safe_replacement fixed to allow
 *                    type_{1,2} to be null.  core_table_insert should
 *                    calculate source_rank differently for C vs S level
 * 02/08/2001 3.8.4:  prepare_src_mergefacts was calling map_sg_data instead
 *                    of map_sg_fields, this was fixed.
 *		      map_sg_fields.CONCEPT_ID was joining concept_id field
 *                     from the driving table to sg_id field instead of
 *                     the concept_id field of classes.
 *                    map_to_meme_ids was not setting the switch value for
 *                     source_attributes/rels that were correctly  mapped.
 * 01/10/2001 3.8.3:  map_sg_data, changes to map_to_meme_ids,
 *                    prepare_src_mergefacts, map_sg_fields
 * 01/03/2001 3.8.2:  filter_facts was upgraded to understand CHEM_STY.
 *                    Logic was also cleaned up so EXCLUDE_LIST for type_1
 *                    only filters where atom_id_1's have termgroups on
 *                    the list.  The code should now be easy to extend for
 *                    sources/termgroups, and other filter categories
 * 12/21/2000 3.8.1:  Prepare src_mergefacts location 240 status changed to
 *			b.status
 * 12/11/2000 3.8.0:  Released to NLM
 * 12/04/2000 3.7.5:  insert_ranks maintains source_rank.  undo_insert_ranks
 * 		      informs MRD of sourcE_rank changes.
 * 11/22/2000 3.7.4:  Rework 3.7.3, changes to map_to_meme_ids, map_sg_fields
 * 11/17/2000 3.7.3:  Changes to assign_string_uis insert_source_ids,
 *                    reset_safe_replacement to call report_table_change.
 * 11/15/2000 3.7.2:  Fixes to code that checks whether sg_ids have been
 *		      correctly mapped.  See comments in map_to_meme_ids
 * 11/13/2000 3.7.1:  Changes to map_to_meme_ids, prepare_src_mergefacts,
 *                    map_sg_fields, map_obsolete_rels
 *		      safe_replacement: exception for CMesH
 * 11/10/2000 3.7.0:  Released
 * 11/07/2000 3.6.8:  Query changes (Map CUI)
 * 11/06/2000 3.6.7:  changes to map_obsolete_rels
 * 10/31/2000 3.6.6:  map_obsolete_rels, map_sg_fields,
 *		      changes to map_to_meme_ids, call to report_table_change
 * 10/30/2000 3.6.5:  safe_replacement uses new_atom_id and new_atom_id in
 *		      the mom_safe_replacement rank calculation
 * 10/20/2000 3.6.4:  all procedures throw exceptions on errors now.
 *		      In some cases procedures were only raising exceptions
 *		      if "other" exceptions were caught.  Also, all calls
 *		      to MEME_BATCH_ACTIONS now flag for return values!
 * 10/20/2000 3.6.3:  conform batch_actions calls to new version.
 * 09/28/2000 3.6.22: load_facts uses work_id
 * 		      filter_facts supports "NEW_ATOMS" filter.
 * 09/20/2000 3.6.21: replacement_merges (ambig_flag uses DISTINCT concept_id
 *		      flag).
 * 09/14/2000 3.6.2:  insert_ranks: allows sources.src to specify low_termgroup
 * 09/12/2000 3.6.1:  Safe_replacement does not clean up ( better sr_predicate
 *		      logging). reset_safe_replacement was added.
 * 		      tty_parameter added to safe_replacement,generate_facts
 *		      core_table_insert (C) sets preferred ids.
 * 		      report_table_change calls use correct params
 * 08/01/2000 3.6.0:  Package handover version
 * 07/26/2000 3.5.7:  insert_ranks udpated
 * 		      safe_replacement, formatted calls to batch_action 
 * 07/20/2000 3.5.6:  Core_Table_insert accepts 'CR'
 *		      assign_meme_ids('A') assigns string_ids
 * 07/05/2000 3.5.5:  Changes to: insert_ranks
 * 06/23/2000 3.5.4:  Changes to: replacement_merges
 * 06/22/2000 3.5.3:  Small changes to: assign_string_uis, replacement_merges,
 *		      core_table_insert
 * 04/09/2000:	      Package completion
 * 09/09/1999:	      First version created and compiled
 *
 * Status:
 *	Functionality:
 *	    setup_partitions unimplemented (8i)
 *          Every time MEME_BATCH_ACTIONS is called we need to log
 *          the transaction_id (in activity_log) so that it can be
 *          algorithmically undone later.
 *	Testing:
 *	Enhancements:
 *
 ******************************************************************************/

   /* package info */
   package_name 		 VARCHAR2(25) := 'MEME_SOURCE_PROCESSING';
   release_number		 VARCHAR2(1)  := '4';
   version_number		 VARCHAR2(5)  := '62.0';
   version_date 		 DATE	      := '27-Oct-2005';
   version_authority		 VARCHAR2(10)  := 'BAC';

   /* public variables */
   msp_debug			 BOOLEAN := FALSE;
   msp_trace			 BOOLEAN := FALSE;
   msp_exception		 EXCEPTION;

   msp_method			 VARCHAR2(100);
   msp_location 		 VARCHAR2(10);
   msp_error_code		 INTEGER;
   msp_error_detail		 VARCHAR2(4000);

   /* functions and procedures */

   FUNCTION release RETURN INTEGER;
   FUNCTION version RETURN FLOAT;

   PROCEDURE version;

   FUNCTION version_info RETURN VARCHAR2;
   PRAGMA RESTRICT_REFERENCES(version_info,WNDS,RNDS,WNPS);

   PROCEDURE help;
   PROCEDURE help(topic IN VARCHAR2);

   PROCEDURE register_package;
   PROCEDURE set_trace_on;
   PROCEDURE set_trace_off;
   PROCEDURE set_debug_on;
   PROCEDURE set_debug_off;

   PROCEDURE trace(message IN VARCHAR2);

   PROCEDURE local_exec(query IN VARCHAR2);
   FUNCTION  local_exec(query IN VARCHAR2) RETURN INTEGER;

   PROCEDURE initialize_trace(method IN VARCHAR2);

   PROCEDURE error_log(
      method		  IN VARCHAR2,
      location		  IN VARCHAR2,
      error_code	  IN INTEGER,
      detail		  IN VARCHAR2);

   PROCEDURE assign_auis(
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0,
      table_name	  IN VARCHAR2 := 'SC');

   PROCEDURE assign_atuis(
      table_name 	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE assign_ruis(
      table_name 	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE assign_string_uis(
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE assign_source_ids(
      table_name	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE assign_meme_ids(
      table_name	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE map_to_meme_ids(
      table_name	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      unique_flag	  IN VARCHAR2 := MEME_CONSTANTS.NO,
      work_id		  IN INTEGER  := 0);

   PROCEDURE insert_source_ids(
      table_name	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE safe_replacement(
      string_parameter	  IN VARCHAR2 := 'NONE',
      code_parameter	  IN VARCHAR2 := 'NONE',
      tty_parameter	  IN VARCHAR2 := 'NONE',
      source_aui_parameter IN VARCHAR2 := 'NONE',
      source_cui_parameter IN VARCHAR2 := 'NONE',
      source_dui_parameter IN VARCHAR2 := 'NONE',
      old_source_table	  IN VARCHAR2 := '',
      new_source_table	  IN VARCHAR2 := '',
      old_termgroup_table IN VARCHAR2 := '',
      new_termgroup_table IN VARCHAR2 := '',
      change_status	  IN VARCHAR2 := MEME_CONSTANTS.YES,
      source		  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE reset_safe_replacement (
      source		  IN VARCHAR2,
      authority           IN VARCHAR2);

   PROCEDURE source_replacement(
      table_name	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE generate_facts(
      termgroup_table_1   IN VARCHAR2,
      termgroup_table_2   IN VARCHAR2,
      merge_set 	  IN VARCHAR2,
      string_parameter	  IN VARCHAR2,
      code_parameter	  IN VARCHAR2,
      tty_parameter	  IN VARCHAR2 := 'NONE',
      source_aui_parameter IN VARCHAR2 := 'NONE',
      source_cui_parameter IN VARCHAR2 := 'NONE',
      source_dui_parameter IN VARCHAR2 := 'NONE',
      table_name	  IN VARCHAR2,
      source		  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE filter_facts(
      type_1		  IN VARCHAR2,
      type_2		  IN VARCHAR2,
      arg_1		  IN VARCHAR2,
      arg_2		  IN VARCHAR2,
      not_1		  IN VARCHAR2,
      not_2		  IN VARCHAR2,
      merge_set 	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      source		  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE load_facts(
      authority 	  IN VARCHAR2,
      merge_set 	  IN VARCHAR2,
      integrity_vector	  IN VARCHAR2,
      change_status	  IN VARCHAR2,
      make_demotion	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0,
      truncate_flag	  IN VARCHAR2 := MEME_CONSTANTS.YES);

   PROCEDURE move_processed_facts(
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE replacement_merges(
      merge_set 	  IN VARCHAR2,
      normalization_flag  IN VARCHAR2,
      ambig_flag	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE prepare_src_mergefacts(
      unique_flag	  IN VARCHAR2 := MEME_CONSTANTS.NO,
      authority 	  IN VARCHAR2,
      merge_set 	  IN VARCHAR2 := '',
      work_id		  IN INTEGER  := 0);

   PROCEDURE setup_partitions;

   PROCEDURE insert_ranks(
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0,
      set_ranks 	  IN VARCHAR2 :='N');

   PROCEDURE undo_insert_ranks(
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE report_tables(
      root_source	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0,
      threshold		  IN INTEGER  := 6);

   PROCEDURE update_releasability(
      old_source	  IN VARCHAR2,
      new_source	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      new_value 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE resolve_stys(
      source		  IN VARCHAR2,
      sty_fate		  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE delete_demotions(
      source		  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE foreign_classes_insert (
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE core_table_insert(
      table_name	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE map_obsolete_rels(
      stripped_source	  IN VARCHAR2,
      authority 	  IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   FUNCTION create_rank_table(
      sg_type             IN VARCHAR2,
      table_name          IN VARCHAR2,
      str_pad             IN VARCHAR2
   ) RETURN VARCHAR2;

   PROCEDURE map_sg_fields(
      table_name	  IN VARCHAR2,
      pair_flag 	  IN VARCHAR2 := MEME_CONSTANTS.NO,
      concept_flag	  IN VARCHAR2 := MEME_CONSTANTS.YES,
      qa_flag	  	  IN VARCHAR2 := MEME_CONSTANTS.YES
   );

   PROCEDURE map_sg_fields_all (
      table_name	  IN VARCHAR2,
      pair_flag 	  IN VARCHAR2 := MEME_CONSTANTS.NO,
      concept_flag	  IN VARCHAR2 := MEME_CONSTANTS.NO,
      qa_flag	  	  IN VARCHAR2 := MEME_CONSTANTS.YES
   );

   PROCEDURE map_sg_data(
      authority	          IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE create_insertion_indexes(
      authority	          IN VARCHAR2,
      work_id		  IN INTEGER  := 0);

   PROCEDURE drop_insertion_indexes(
      authority	          IN VARCHAR2,
      work_id		  IN INTEGER  := 0);
   
   PROCEDURE set_atom_ordering(
       source		  IN VARCHAR2,
       ordering		  IN VARCHAR2,
       authority	  IN VARCHAR2,
       work_id		  IN INTEGER := 0);

END meme_source_processing;
/
SHOW ERRORS
CREATE OR REPLACE PACKAGE BODY meme_source_processing AS

/* FUNCTION RELEASE ************************************************************
 */
FUNCTION release
RETURN INTEGER
IS
BEGIN
   version;
   RETURN TO_NUMBER(release_number);
END release;

/* FUNCTION VERSION ************************************************************
 */
FUNCTION version
RETURN FLOAT
IS
BEGIN
   version;
   RETURN TO_NUMBER(version_number);
END version;

/* PROCEDURE VERSION ***********************************************************
 */
PROCEDURE version
IS
BEGIN
   MEME_UTILITY.put_message('Package: '||package_name);
   MEME_UTILITY.put_message('Release '||release_number||': '||
			'version '||version_number||', '||
			version_date||' ('||
			version_authority||')');
END version;

/* FUNCTION VERSION_INFO *******************************************************
 */
FUNCTION version_info
RETURN VARCHAR2
IS
BEGIN
   RETURN package_name||' Release '||release_number||': '||
	      'version '||version_number||' ('||version_date||')';
END version_info;

/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help
IS
BEGIN
   help('');
END;

/* PROCEDURE HELP **************************************************************
 */
PROCEDURE help(topic IN VARCHAR2)
IS
BEGIN
   /* This procedure requires SET SERVEROUTPUT ON */
-- DBMS_OUTPUT.ENABLE(1000000);

   /* Print version */
   version;
END help;

/* PROCEDURE REGISTER_VERSION **************************************************
 */
PROCEDURE register_package
IS
BEGIN
   register_version(
      MEME_SOURCE_PROCESSING.release_number,
      MEME_SOURCE_PROCESSING.version_number,
      SYSDATE,
      MEME_SOURCE_PROCESSING.version_authority,
      MEME_SOURCE_PROCESSING.package_name,
      '',
      'Y',
      'Y'
   );
END register_package;

/* PROCEDURE SET_TRACE_ON ******************************************************
 */
PROCEDURE set_trace_on
IS
BEGIN
   msp_trace := TRUE;
END set_trace_on;

/* PROCEDURE SET_TRACE_OFF *****************************************************
 */
PROCEDURE set_trace_off
IS
BEGIN
   msp_trace := FALSE;
END set_trace_off;

/* PROCEDURE SET_DEBUG_ON ******************************************************
 */
PROCEDURE set_debug_on
IS
BEGIN
   msp_debug := TRUE;
END set_debug_on;

/* PROCEDURE SET_DEBUG_OFF *****************************************************
 */
PROCEDURE set_debug_off
IS
BEGIN
   msp_debug := FALSE;
END set_debug_off;

/* PROCEDURE TRACE *************************************************************
 */
PROCEDURE trace(message IN VARCHAR2)
IS
BEGIN
   IF msp_trace = TRUE THEN
      MEME_UTILITY.put_message(message);
   END IF;
END trace;

/* PROCEDURE LOCAL_EXEC ********************************************************
 */
PROCEDURE local_exec(query IN VARCHAR2)
IS
BEGIN
   IF msp_trace = TRUE THEN
      MEME_UTILITY.put_message(query);
   END IF;
   IF msp_debug = FALSE THEN
       MEME_UTILITY.exec(query);
   END IF;
END local_exec;

/* FUNCTION LOCAL_EXEC *********************************************************
 */
FUNCTION local_exec(query IN VARCHAR2)
RETURN INTEGER
IS
BEGIN
   IF msp_trace = TRUE THEN
       MEME_UTILITY.put_message(query);
   END IF;
   IF msp_debug = FALSE THEN
       RETURN MEME_UTILITY.exec(query);
   END IF;
   RETURN 0;
END local_exec;

/* PROCEDURE INITIALIZE_TRACE **************************************************
 */
PROCEDURE initialize_trace(
   method IN VARCHAR2
)
IS
BEGIN
   /* must be called at the begining of every procedure or function. */
   msp_method	    := UPPER(method);
   msp_location     := '00';
   msp_error_code   := 1;
   msp_error_detail := '';

END initialize_trace;

/* PROCEDURE SOURCE_PROCESSING_ERROR *******************************************
 */
PROCEDURE error_log(
   method     IN VARCHAR2,
   location   IN VARCHAR2,
   error_code IN INTEGER,
   detail     IN VARCHAR2
)
IS
   error_msg VARCHAR2(256);
BEGIN

   ROLLBACK;

   IF error_code = 1 THEN
      error_msg := 'MI0001: System error.';

   /* Call to other package error */
   ELSIF error_code = 10 THEN
      error_msg := 'MI0010: MEME_BATCH_ACTIONS.batch_action failed.';
   ELSIF error_code = 11 THEN
      error_msg := 'MI0011: MEME_BATCH_ACTIONS.macro_action failed.';

   /* Specific procedure error */
   ELSIF error_code = 20 THEN
      error_msg := 'MI0020: Column name not found in table.';
   ELSIF error_code = 21 THEN
      error_msg := 'MI0021: Null concept_id(s) found in table.';
   ELSIF error_code = 22 THEN
      error_msg := 'MI0022: Null value(s) found in table.';
   ELSIF error_code = 23 THEN
      error_msg := 'MI0023: Null atom_id(s) found in table.';

   ELSIF error_code = 30 THEN
      error_msg := 'MI0030: Invalid string parameter.';
   ELSIF error_code = 31 THEN
      error_msg := 'MI0031: Invalid code parameter.';
   ELSIF error_code = 32 THEN
      error_msg := 'MI0032: Invalid change status.';
   ELSIF error_code = 33 THEN
      error_msg := 'MI0033: Invalid tty parameter.';
   ELSIF error_code = 34 THEN
      error_msg := 'MI0034: Invalid table name.';
   ELSIF error_code = 35 THEN
      error_msg := 'MI0035: Invalid type parameter.';
   ELSIF error_code = 36 THEN
      error_msg := 'MI0036: Invalid sty fate.';

   /* Generic error */
   ELSIF error_code = 42 THEN
      error_msg := 'MI0042: Table does not exist.';
   ELSIF error_code = 43 THEN
      error_msg := 'MI0043: Usage blocked.';
   ELSIF error_code = 44 THEN
      error_msg := 'MI0044: No record found.';
   ELSIF error_code = 45 THEN
      error_msg := 'MI0045: Bad count.';
   ELSIF error_code = 46 THEN
      error_msg := 'MI0046: Bad return.';
   ELSIF error_code = 47 THEN
      error_msg := 'MI0047: Bad relationship_attributes in source_context_relationships.';

   ELSIF error_code = 50 THEN
      error_msg := 'MI0050: Error calling report_table_change.';

   /* Other error */
   ELSE
      error_msg := 'MI0000: Unknown error.';
   END IF;

   MEME_UTILITY.put_error('Error in MEME_SOURCE_PROCESSING. '||
      ' Method: '||method||' Location: '||location||' Error Code: '||error_msg||
      ' Detail: '||detail);

END error_log;

/* PROCEDURE ASSIGN_AUIS *************************************************
 */
PROCEDURE assign_auis(
   authority 	IN VARCHAR2,
   work_id   	IN INTEGER := 0,
   table_name	IN VARCHAR2 := 'SC'
)
IS
   TYPE curvar_type	IS REF CURSOR;
   curvar		curvar_type;

   aui_ct		NUMBER(12);
   l_aui		VARCHAR2(20);
   l_table		VARCHAR2(50);
   l_rowid		ROWID;
   stats_ct		NUMBER(12);

   assign_auis_exc 	EXCEPTION;

   aui_prefix		VARCHAR2(10);
   aui_length		INTEGER;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_auis',
    	'Starting assign_auis',0,work_id,0,1);

   initialize_trace('ASSIGN_AUIS');

   SELECT nvl(min(value),table_name) INTO l_table FROM code_map
   WHERE code = assign_auis.table_name AND type='table_name';

   MEME_UTILITY.sub_timing_start;
   
/**
   msp_location := '10';
   MEME_SYSTEM.analyze(l_table);

   -- ANALYZE atoms_ui if no statistics
   msp_location := '20';
   SELECT count(*) INTO stats_ct
   FROM user_indexes WHERE table_name ='ATOMS_UI'
   AND (last_analyzed is null OR last_analyzed < (sysdate-1));

    -- Soma Changing for 10g performance
   IF stats_ct > 0 THEN
     MEME_SYSTEM.analyze('atoms_ui');
   END IF;
**/
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_auis',
    	'Done preparing statistics.',0,work_id,0,20);

   msp_location := '40';
   -- Assign aui 
   EXECUTE IMMEDIATE
       'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
   	SET aui =
          (SELECT aui FROM atoms_ui b, source_rank c, source_rank d
           WHERE a.sui = b.sui 
	     AND b.stripped_source = d.stripped_source
	     AND a.source = c.source 
	     AND c.normalized_source = d.source
	     AND a.tty = b.tty
  	     AND NVL(a.source_aui,''null'') = NVL(b.source_aui,''null'')
  	     AND NVL(a.source_cui,''null'') = NVL(b.source_cui,''null'')
  	     AND NVL(a.source_dui,''null'') = NVL(b.source_dui,''null'')
  	     AND NVL(a.code,''null'') = NVL(b.code,''null'') )
       WHERE aui IS NULL AND a.tobereleased in (''Y'',''y'') ';

   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' auis assigned.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_auis',
    	'Done assigning AUI',0,work_id,0,10);

   -- At this point, any auis that are null are new
   msp_location := '50';
   MEME_UTILITY.drop_it('table','source_classes_aui');
   msp_location := '60';
   local_exec('
	CREATE TABLE source_classes_aui NOLOGGING AS
	SELECT DISTINCT 0 as aui, sui, d.stripped_source,c.tty,
	   code, a.source_aui,
	   a.source_cui, a.source_dui
	FROM ' || l_table || ' a, source_rank b, 
	    source_rank d, termgroup_rank c
	WHERE a.source = b.source 
	  AND a.termgroup = c.termgroup
  	  AND b.normalized_source = d.source
          AND a.aui IS NULL AND a.tobereleased in (''Y'',''y'') ');

   msp_location := '70';
   EXECUTE IMMEDIATE 'UPDATE source_classes_aui SET aui = rownum';
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' new auis assigned.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_auis',
    	'Done assigning new AUI',0,work_id,0,40);

   msp_location := '70.1';
   --MEME_SYSTEM.analyze('source_classes_aui');

   msp_location := '80';
   aui_ct :=MEME_UTILITY.exec_select('SELECT COUNT(*) FROM source_classes_aui');

   msp_location := '90';
   UPDATE max_tab SET max_id = max_id + aui_ct
   WHERE table_name = 'AUI';

   msp_location := '100';
   local_exec('
	UPDATE source_classes_aui
	SET aui = (SELECT aui + max_id - ' || aui_ct || '
	FROM max_tab WHERE table_name = ''AUI'')  
   ');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_auis',
    	'Done updating source_classes_aui',0,work_id,0,60);

   msp_location := '110.0';
   MEME_UTILITY.drop_it('table','new_atoms_ui');
   msp_location := '110.1';
   SELECT value INTO aui_prefix FROM code_map
   WHERE type = 'ui_prefix' AND code = 'AUI';
   msp_location := '110.2';
   SELECT to_number(value) INTO aui_length FROM code_map
   WHERE type = 'ui_length' AND code = 'AUI';
   msp_location := '110.3';
   EXECUTE IMMEDIATE
	'CREATE TABLE new_atoms_ui 
	  (aui, sui, stripped_source, tty, code, source_aui, source_cui, source_dui) NOLOGGING AS
	SELECT /*+ PARALLEL(a) */ ''' || aui_prefix || ''' || 
		LPAD(aui, ' || aui_length || ', 0), sui, 
	  stripped_source, tty, code, source_aui, source_cui, source_dui
	FROM source_classes_aui a';

   msp_location := '115';
   local_exec('	CREATE INDEX x_new_atoms_ui on new_atoms_ui (sui,stripped_source,tty) TABLESPACE MIDI NOLOGGING');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_auis',
    	'Done creating table new_atoms_ui',0,work_id,0,80);

   msp_location := '40';
   -- Assign aui 
   msp_location := '120.0' ;
  EXECUTE IMMEDIATE
       'UPDATE /*+ PARALLEL(a) */  ' || l_table || ' a
   	SET aui =
          (SELECT aui FROM new_atoms_ui b, source_rank c, source_rank d
           WHERE a.sui = b.sui 
	     AND b.stripped_source = d.stripped_source
	     AND a.source = c.source 
	     AND c.normalized_source = d.source
	     AND (a.termgroup like ''%/''||b.tty OR
	          a.tty = b.tty)
  	     AND NVL(a.source_aui,''null'') = NVL(b.source_aui,''null'')
  	     AND NVL(a.source_cui,''null'') = NVL(b.source_cui,''null'')
  	     AND NVL(a.source_dui,''null'') = NVL(b.source_dui,''null'')
  	     AND NVL(a.code,''null'') = NVL(b.code,''null'') )
       WHERE aui IS NULL AND a.tobereleased in (''Y'',''y'') ';

   msp_location := '130';
   EXECUTE IMMEDIATE 
	'INSERT INTO atoms_ui
	   (aui, sui, stripped_source, tty, code, 
	    source_aui, source_cui, source_dui)
	 SELECT DISTINCT * FROM new_atoms_ui ';
   msp_location := '310';
   MEME_UTILITY.drop_it('table','source_classes_aui');
   MEME_UTILITY.drop_it('table','new_atoms_ui');


   msp_location := '320';
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Assign Identifiers',
	detail => 'Assign atom identifiers (AUI, ' || l_table || ')',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_auis',
    	'Done assign_auis',0,work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN assign_auis_exc THEN
      MEME_UTILITY.drop_it('table','source_classes_aui');
      MEME_UTILITY.drop_it('table','new_atoms_ui');
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table','source_classes_aui');
      MEME_UTILITY.drop_it('table','new_atoms_ui');
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END assign_auis;


/* PROCEDURE ASSIGN_ATUIS *************************************************

  Must be called after map_to_meme_ids.
 */
PROCEDURE assign_atuis(
   table_name IN VARCHAR2,
   authority IN VARCHAR2,
   work_id   IN INTEGER
)
IS
   TYPE curvar_type	IS REF CURSOR;
   curvar		curvar_type;

   atui_ct		NUMBER(12);
   l_atui		VARCHAR2(20);
   l_table		VARCHAR2(50);
   l_rowid		ROWID;
   stats_ct		NUMBER(12);

   assign_atuis_exc 	EXCEPTION;

   atui_prefix		VARCHAR2(10);
   atui_length		INTEGER;

BEGIN

   MEME_UTILITY.put_message('Starting assign_atuis');
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Starting assign_atuis',0,work_id,0,1);

   initialize_trace('ASSIGN_ATUIS');

   msp_location := '0';
   SELECT nvl(min(value),table_name) INTO l_table FROM codE_map
   WHERE code = assign_atuis.table_name AND type = 'table_name';

   MEME_UTILITY.sub_timing_start;

   -- Prep the table by setting sg_id, sg_type, sg_qualifier

   -- Use RUI where the type is SRC_REL_ID
   msp_location := '5.0';
   EXECUTE IMMEDIATE
     'UPDATE ' || l_table || ' a
      SET (sg_id,sg_type,sg_qualifier) = 
         (SELECT rui,''RUI'',null 
          FROM relationships b 
          WHERE relationship_id = sg_meme_id
          UNION ALL
          SELECT rui,''RUI'', null
          FROM context_relationships b
          WHERE relationship_id = sg_meme_id)
      WHERE sg_type = ''SRC_REL_ID'' ';

   COMMIT;

   -- Use AUI where the type is not mappable and the level is S
   msp_location := '5.1';
   EXECUTE IMMEDIATE
     'UPDATE ' || l_table || ' a
      SET (sg_id,sg_type,sg_qualifier) = 
	(SELECT aui,''AUI'',null FROM classes b WHERE a.atom_id = b.atom_id)
      WHERE attribute_level = ''S''
        AND NVL(sg_type,''null'') NOT IN 
	  (SELECT code FROM code_map WHERE type=''map_sg_type'')
      AND nvl(sg_type,''null'') != ''AUI'' ';
   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done preparing table',0,work_id,0,10);

   -- OLD: Do not convert CUI assigned things (e.g. insertion STYs)
   --    is there still a reason for that??? changing it
   --    STYs should always go in as CONCEPT_ID with a null ATUI value
   --    STY ATUIs are entirely and completely assigned as a pre-production op.
   -- Use CONCEPT_ID where the type is not mappable and the level is C
   msp_location := '5.2';
   EXECUTE IMMEDIATE
     'UPDATE ' || l_table || ' a
      SET sg_id = to_char(concept_id),
          sg_type = ''CONCEPT_ID'',
          sg_qualifier = null
      WHERE attribute_level = ''C''
        AND ( attribute_name = ''SEMANTIC_TYPE''
              OR
              NVL(sg_type,''null'') NOT IN
                   (SELECT code FROM code_map WHERE type=''map_sg_type''))
        AND nvl(sg_type,''null'') NOT IN (''CONCEPT_ID'',''CUI'') ';
   COMMIT;


   --
   -- To preserver ATUI values over time, we should use the ROOT  
   -- types instead of the versioned types.  This converts the sg_id
   -- and sg_qualifier.  The semantics of the id remain the same
   -- but it makes it easier to track over time
   --
   msp_location := '5.3';
   EXECUTE IMMEDIATE
     'UPDATE ' || l_table || ' a
      SET sg_type = ''ROOT_''||sg_type,
	  sg_qualifier = 
	    (SELECT source FROM source_version 
	     WHERE current_name = sg_qualifier)
      WHERE sg_type in (''SOURCE_CUI'',''SOURCE_AUI'',''SOURCE_DUI'',
			''SOURCE_RUI'') ';
   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done converting the sg_id and sg_qualifier',0,work_id,0,30);

   msp_location := '5.4';
   EXECUTE IMMEDIATE
     'UPDATE ' || l_table || ' a
      SET sg_type = ''CODE_ROOT_SOURCE'',
	  sg_qualifier = 
	    (SELECT source FROM source_version 
	     WHERE current_name = sg_qualifier)
      WHERE sg_type = ''CODE_SOURCE'' ';

   COMMIT;

   msp_location := '5.5';
   EXECUTE IMMEDIATE
     'UPDATE ' || l_table || ' a
      SET sg_type = ''CODE_ROOT_TERMGROUP'',
	  sg_qualifier = 
	    (SELECT source || ''/'' || 
		substr(sg_qualifier,instr(sg_qualifier,''/'')+1) 
	     FROM source_version 
	     WHERE current_name = 
		substr(sg_qualifier,1,instr(sg_qualifier,''/'')-1))
      WHERE sg_type = ''CODE_TERMGROUP'' ';

   COMMIT;

   MEME_UTILITY.put_message('Done normalizing UI types');
/**
   -- ANALYZE l_table if no statistics
   msp_location := '10';
   SELECT count(*) INTO stats_ct
   FROM user_indexes WHERE table_name = upper(l_table)
   AND (last_analyzed is null OR last_analyzed < (sysdate-1));

   IF stats_ct > 0 THEN
       MEME_SYSTEM.analyze(l_table);
   END IF;
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done analyzing table',0,work_id,0,40);

   -- ANALYZE attributes_ui if no statistics
   msp_location := '20';
   SELECT count(*) INTO stats_ct
   FROM user_indexes WHERE table_name ='ATTRIBUTES_UI'
   AND (last_analyzed is null OR last_analyzed < (sysdate-1));
   IF stats_ct > 0 THEN
       MEME_SYSTEM.analyze('ATTRIBUTES_UI');
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done analyzing attributes_ui',0,work_id,0,60);
   MEME_UTILITY.put_message('Done analyzing attributes_ui');
**/
   
   -- Assign ATUI (only where not assigned and where releasable)
   -- Note: the hashcode can be null in the case of DA,MR,ST,and MED<year>
   -- attributes
   msp_location := '40.1';
   MEME_UTILITY.drop_it('table','source_attributes_atui_map');
   msp_location := '40.2';
   IF l_table = 'source_attributes' THEN
     EXECUTE IMMEDIATE
        'CREATE TABLE source_attributes_atui_map NOLOGGING AS
         SELECT a.attribute_id, b.atui
         FROM ' || l_table || ' a, attributes_ui b, source_rank ns, source_rank rs
         WHERE a.atui IS NULL
         AND a.tobereleased not in (''N'',''n'')
         AND b.root_source = rs.stripped_source
         AND ns.normalized_source = rs.source
         AND a.source = ns.source
         AND a.attribute_level || a.attribute_name || a.source_atui || a.hashcode ||
             a.sg_id || a.sg_type || a.sg_qualifier =
             b.attribute_level || b.attribute_name || b.source_atui || b.hashcode ||
             b.sg_id || b.sg_type || b.sg_qualifier
         AND a.attribute_name||a.sg_type != ''SEMANTIC_TYPECONCEPT_ID''';
    ELSE
     EXECUTE IMMEDIATE
        'CREATE TABLE source_attributes_atui_map NOLOGGING AS
         SELECT attribute_id, b.atui
         FROM
           (SELECT attribute_id, attribute_level,
                DECODE(attribute_level,
                 ''C'',''MTH'',
                 ''S'',DECODE(SUBSTR(source,1,2),
                        ''E-'',''MTH'',
                        ''L-'',''MTH'',
                        ''S-'',''MTH'', source)) source,
                sg_id, sg_type, sg_qualifier, attribute_name,
                hashcode, source_atui
            FROM ' || l_table || ' aa
            WHERE atui IS NULL
              AND tobereleased not in (''N'',''n'') ) a,
         attributes_ui b, source_rank ns, source_rank rs
       WHERE a.attribute_level = b.attribute_level
         AND b.root_source = rs.stripped_source
         AND ns.normalized_source = rs.source
         AND a.source = ns.source
         AND a.attribute_name = b.attribute_name
         AND nvl(a.source_atui,''null'') = nvl(b.source_atui,''null'')
         AND nvl(a.hashcode,''null'') = nvl(b.hashcode,''null'')
         AND b.sg_type = a.sg_type
         AND nvl(b.sg_qualifier,''null'') = nvl(a.sg_qualifier,''null'')
         AND b.sg_id = a.sg_id
         AND a.attribute_name||a.sg_type != ''SEMANTIC_TYPECONCEPT_ID''';
    END IF;
    msp_location := '40.3';

    EXECUTE IMMEDIATE
	'ALTER TABLE source_attributes_atui_map ADD PRIMARY KEY (attribute_id)'; 
 	
	MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done analyzing source_attributes_atui_map',0,work_id,0,40);
    
    MEME_UTILITY.put_message('Done creating and analyzing assignment table for existing ATUIs');
    
    msp_location := '40.4';
    
    EXECUTE IMMEDIATE           
           'ALTER SESSION ENABLE PARALLEL DML';
    msp_location := '40.4a';           
    	EXECUTE IMMEDIATE
    	    'MERGE /*+ first_rows parallel(a) parallel(new) */ INTO ' || l_table || ' a
             USING source_attributes_atui_map new ON (a.attribute_id = new.attribute_id)
             WHEN MATCHED THEN UPDATE SET
             atui = new.atui where a.tobereleased in (''Y'',''y'') and a.atui is NULL';
    
/*    EXECUTE IMMEDIATE
       'UPDATE (SELECT a.atui, b.atui new_atui 
                FROM ' || l_table || ' a, source_attributes_atui_map b
                WHERE a.tobereleased not in (''N'',''n'')
                  AND a.atui IS NULL
                  AND a.attribute_id = b.attribute_id)
        SET atui = new_atui';
*/
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' atuis assigned');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done assigning ATUI',work_id,0,70);

    -- At this point, any atuis that are null are new
    -- Except where they are unreleasable
    msp_location := '50';
    MEME_UTILITY.drop_it('table','source_attributes_atui_map');
    msp_location := '60';
    EXECUTE IMMEDIATE
       'CREATE TABLE source_attributes_atui_map NOLOGGING AS
        SELECT
           attribute_id, rs.stripped_source as root_source,
           attribute_level, attribute_name, hashcode,
           sg_id, sg_type, sg_qualifier, a.source_atui
        FROM ' || l_table || ' a, source_rank ns, source_rank rs
        WHERE DECODE(attribute_level,
                 ''C'',''MTH'',
                 ''S'',DECODE(SUBSTR(a.source,1,2),
                        ''E-'',''MTH'',
                        ''L-'',''MTH'',
                        ''S-'',''MTH'', a.source)) = ns.source
          AND ns.normalized_source = rs.source
          AND atui is null
          AND tobereleased not in (''N'',''n'')
         AND a.attribute_name||a.sg_type != 
                        ''SEMANTIC_TYPECONCEPT_ID''';

 	 MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done analyzing source_attributes_atui_map',0,work_id,0,40);
    MEME_UTILITY.put_message('Done creating and analyzing table for new ATUIs');

   msp_location := '50.1';
   MEME_UTILITY.drop_it('table','source_attributes_atui');
   msp_location := '60.1';
   EXECUTE IMMEDIATE
       'CREATE TABLE source_attributes_atui NOLOGGING AS
        SELECT rownum as atui, root_source,
               attribute_level, attribute_name, hashcode,
               sg_id, sg_type, sg_qualifier, source_atui
        FROM (SELECT DISTINCT 0 as atui, root_source,
                     attribute_level, attribute_name, hashcode,
                    sg_id, sg_type, sg_qualifier, source_atui
              FROM source_attributes_atui_map) ';

   msp_location := '80';
   atui_ct :=
     MEME_UTILITY.exec_select('SELECT COUNT(*) FROM source_attributes_atui');

   msp_location := '90';
   UPDATE max_tab SET max_id = max_id + atui_ct
   WHERE table_name = 'ATUI';

   msp_location := '100';
   EXECUTE IMMEDIATE 
       'UPDATE source_attributes_atui
	SET atui = (SELECT atui + max_id - ' || atui_ct || '
	FROM max_tab WHERE table_name = ''ATUI'') '; 

	
   msp_location := '110.0';
   MEME_UTILITY.drop_it('table','new_attributes_ui');
   msp_location := '110.1';
   SELECT value INTO atui_prefix FROM code_map
   WHERE type = 'ui_prefix' AND code = 'ATUI';
   msp_location := '110.2';
   SELECT to_number(value) INTO atui_length FROM code_map
   WHERE type = 'ui_length' AND code = 'ATUI';
   msp_location := '110.3';
   EXECUTE IMMEDIATE
       'CREATE TABLE new_attributes_ui (atui, root_source, attribute_level,
		attribute_name, hashcode, sg_id, sg_type, sg_qualifier, source_atui)
	NOLOGGING AS SELECT ''' || atui_prefix || ''' ||
		LPAD(atui, ' || atui_length || ', 0), root_source, attribute_level,
	 	attribute_name, hashcode, sg_id, sg_type, sg_qualifier, source_atui
	FROM source_attributes_atui';
  
   msp_location := '110.1';
   EXECUTE IMMEDIATE
	'CREATE INDEX x_naui on new_attributes_ui (sg_id, sg_type) TABLESPACE MIDI
	 COMPUTE STATISTICS PARALLEL NOLOGGING';

	 /**
   msp_location := '115.1';
   MEME_SYSTEM.analyze('source_attributes_atui');
   msp_location := '115.2';
   MEME_SYSTEM.analyze('new_attributes_ui');
**/
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done assigning new ATUI',work_id,0,80);
   MEME_UTILITY.put_message('Done assigning new ATUIs');

   -- Assign ATUI (only where not assigned and where releasable)
   msp_location := '120.0';
   MEME_UTILITY.drop_it('table','source_attributes_atui_map');
   msp_location := '120.1';
   IF l_table = 'source_attributes' THEN
     EXECUTE IMMEDIATE
        'CREATE TABLE source_attributes_atui_map AS
         SELECT /*+ USE_MERGE(a,b) */ attribute_id, b.atui
         FROM ' || l_table || ' a, new_attributes_ui b, source_rank ns, source_rank rs
         WHERE a.atui IS NULL
         AND a.tobereleased not in (''N'',''n'')
         AND b.root_source = rs.stripped_source
         AND ns.normalized_source = rs.source
         AND a.source = ns.source
         AND a.attribute_level || a.attribute_name || a.source_atui || a.hashcode ||
             a.sg_id || a.sg_type || a.sg_qualifier =
             b.attribute_level || b.attribute_name || b.source_atui || b.hashcode ||
             b.sg_id || b.sg_type || b.sg_qualifier
         AND a.attribute_name||a.sg_type != ''SEMANTIC_TYPECONCEPT_ID''';
    ELSE
      EXECUTE IMMEDIATE
        'CREATE TABLE source_attributes_atui_map NOLOGGING AS
            -- Haining, Siebel C17ND, remove USE_HASH for 10g
         SELECT attribute_id, b.atui
         FROM
           (SELECT attribute_id, attribute_level,
                DECODE(attribute_level,
                 ''C'',''MTH'',
                 ''S'',DECODE(SUBSTR(source,1,2),
                        ''E-'',''MTH'',
                        ''L-'',''MTH'',
                        ''S-'',''MTH'', source)) source,
                sg_id, sg_type, sg_qualifier, attribute_name,
                hashcode, source_atui
            FROM ' || l_table || ' aa
            WHERE atui IS NULL
              AND tobereleased not in (''N'',''n'') ) a,
         new_attributes_ui b, source_rank ns, source_rank rs
       WHERE a.attribute_level = b.attribute_level
         AND b.root_source = rs.stripped_source
         AND ns.normalized_source = rs.source
         AND a.source = ns.source
         AND a.attribute_name = b.attribute_name
         AND nvl(a.hashcode,''null'') = nvl(b.hashcode,''null'')
         AND b.sg_type = a.sg_type
         AND nvl(b.sg_qualifier,''null'') = nvl(a.sg_qualifier,''null'')
         AND nvl(b.source_atui,''null'') = nvl(a.source_atui,''null'')
         AND b.sg_id = a.sg_id';
    END IF;

    msp_location := '120.2';
    EXECUTE IMMEDIATE
	'ALTER TABLE source_attributes_atui_map ADD PRIMARY KEY (attribute_id)'; 
 	 MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done analyzing source_attributes_atui_map',0,work_id,0,40);

    MEME_UTILITY.put_message('Done creating and analyzing assignment table for new ATUIs');

    msp_location := '125';
    EXECUTE IMMEDIATE
       'UPDATE (SELECT a.atui, b.atui new_atui 
                FROM ' || l_table || ' a, source_attributes_atui_map b
                WHERE a.tobereleased not in (''N'',''n'')
                  AND a.atui IS NULL
                  AND a.attribute_id = b.attribute_id)
        SET atui = new_atui';

   msp_location := '127';
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' new atuis assigned.');

   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done assigning ATUI',work_id,0,90);

   msp_location := '130';
   EXECUTE IMMEDIATE 
	'INSERT /*+ APPEND */ INTO attributes_ui 
	   (atui, root_source, attribute_level,
	    attribute_name, hashcode, sg_id, sg_type, 
	    sg_qualifier, source_atui)
	 SELECT DISTINCT * FROM new_attributes_ui ';
   MEME_UTILITY.put_message('Done loading new ATUIs');

   msp_location := '310';
   MEME_UTILITY.drop_it('table','source_attributes_atui');
   MEME_UTILITY.drop_it('table','source_attributes_atui_map');
   MEME_UTILITY.drop_it('table','new_attributes_ui');

   msp_location := '320';
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Assign Identifiers',
	detail => 'Assign attribute identifiers (ATUI, ' || l_table || ')',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_atuis',
    	'Done processing assign_atuis',work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN assign_atuis_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END assign_atuis;

/* PROCEDURE ASSIGN_RUIS *************************************************
  Must be called after map_to_meme_ids
 */
PROCEDURE assign_ruis(
   table_name IN VARCHAR2,
   authority IN VARCHAR2,
   work_id   IN INTEGER
)
IS
   TYPE curvar_type	IS REF CURSOR;
   curvar		curvar_type;

   rui_ct		NUMBER(12);
   l_rui		VARCHAR2(20);
   l_rowid		ROWID;
   l_table		VARCHAR2(50);
   stats_ct		NUMBER(12);

   assign_ruis_exc 	EXCEPTION;

   rui_prefix		VARCHAR2(10);
   rui_length		INTEGER;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Starting assign_ruis',work_id,0,1);

   initialize_trace('ASSIGN_RUIS');

   MEME_UTILITY.put_message('Starting assign_ruis');

   SELECT NVL(min(value),table_name) INTO l_table FROM code_map
   WHERE type = 'table_name'
     AND code = assign_ruis.table_name;

   MEME_UTILITY.sub_timing_start;

   
   -- Prep the table by setting sg_id_[12], sg_type_[12], sg_qualifier_[12]
   -- It's AUI where the type is not mappable and the level is S
   -- It's CONCEPT_ID where the type is not mappable and the level is C
   msp_location := '5.11';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET (sg_id_1, sg_type_1, sg_qualifier_1) = 
	(SELECT aui,''AUI'',null FROM classes b WHERE a.atom_id_1 = b.atom_id)
     WHERE relationship_level = ''S''
        AND NVL(sg_type_1,''null'') NOT IN 
	  (SELECT code FROM code_map WHERE type=''map_sg_type'')
      AND nvl(sg_type_1,''null'') != ''AUI'' ';

   COMMIT;

   msp_location := '5.12';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET (sg_id_2, sg_type_2, sg_qualifier_2) = 
	(SELECT aui,''AUI'',null FROM classes b WHERE a.atom_id_2 = b.atom_id)
      WHERE relationship_level = ''S''
        AND NVL(sg_type_2,''null'') NOT IN 
	  (SELECT code FROM code_map WHERE type=''map_sg_type'') 
      AND nvl(sg_type_2,''null'') != ''AUI'' ';

   COMMIT;

   msp_location := '5.21';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET sg_id_1 = to_char(concept_id_1), 
	  sg_type_1 = ''CONCEPT_ID'', 
	  sg_qualifier_1 = null
      WHERE relationship_level = ''C''
        AND NVL(sg_type_1,''null'') NOT IN 
	  (SELECT code FROM code_map WHERE type=''map_sg_type'') 
      AND nvl(sg_type_1,''null'') != ''CONCEPT_ID'' ';

   COMMIT;

   msp_location := '5.22';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET sg_id_2 = to_char(concept_id_2), 
	  sg_type_2 = ''CONCEPT_ID'', 
	  sg_qualifier_2 = null
      WHERE relationship_level = ''C''
        AND NVL(sg_type_2,''null'') NOT IN 
	  (SELECT code FROM code_map WHERE type=''map_sg_type'')
      AND nvl(sg_type_2,''null'') != ''CONCEPT_ID'' ';

   COMMIT;
   
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done preparing table',work_id,0,10);

   --
   -- To preserver ATUI values over time, we should use the ROOT  
   -- types instead of the versioned types.  This converts the sg_id
   -- and sg_qualifier.  The semantics of the id remain the same
   -- but it makes it easier to track over time
   --
   msp_location := '5.31';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET sg_type_1 = ''ROOT_''||sg_type_1,
	  sg_qualifier_1 = 
	    (SELECT source FROM source_version 
	     WHERE current_name = sg_qualifier_1)
      WHERE sg_type_1 in (''SOURCE_CUI'',''SOURCE_AUI'',''SOURCE_DUI'',
			''SOURCE_RUI'') ';

   COMMIT;

   msp_location := '5.32';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET sg_type_2 = ''ROOT_''||sg_type_2,
	  sg_qualifier_2 = 
	    (SELECT source FROM source_version 
	     WHERE current_name = sg_qualifier_2)
      WHERE sg_type_2 in (''SOURCE_CUI'',''SOURCE_AUI'',''SOURCE_DUI'',
			''SOURCE_RUI'') ';

   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done converting the sg_id and sg_qualifier',work_id,0,20);

   --
   -- Use CODE_ROOT_SOURCE, CODE_ROOT_TERMGROUP
   --
   msp_location := '5.41';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET sg_type_1 = ''CODE_ROOT_SOURCE'',
	  sg_qualifier_1 = 
	    (SELECT source FROM source_version 
	     WHERE current_name = sg_qualifier_1)
      WHERE sg_type_1 = ''CODE_SOURCE'' ';

   COMMIT;

   msp_location := '5.41';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET sg_type_2 = ''CODE_ROOT_SOURCE'',
	  sg_qualifier_2 = 
	    (SELECT source FROM source_version 
	     WHERE current_name = sg_qualifier_2)
      WHERE sg_type_2 = ''CODE_SOURCE'' ';

   COMMIT;

   msp_location := '5.51';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET sg_type_1 = ''CODE_ROOT_TERMGROUP'',
	  sg_qualifier_1 = 
	    (SELECT source || ''/'' || 
		substr(sg_qualifier_1,instr(sg_qualifier_1,''/'')+1) 
	     FROM source_version 
	     WHERE current_name = 
		substr(sg_qualifier_1,1,instr(sg_qualifier_1,''/'')-1))
      WHERE sg_type_1 = ''CODE_TERMGROUP'' ';

   COMMIT;

   msp_location := '5.52';
   EXECUTE IMMEDIATE
     'UPDATE /*+ PARALLEL(a) */ ' || l_table || ' a
      SET sg_type_2 = ''CODE_ROOT_TERMGROUP'',
	  sg_qualifier_2 = 
	    (SELECT source || ''/'' || 
		substr(sg_qualifier_2,instr(sg_qualifier_2,''/'')+1) 
	     FROM source_version 
	     WHERE current_name = 
		substr(sg_qualifier_2,1,instr(sg_qualifier_2,''/'')-1))
      WHERE sg_type_2 = ''CODE_TERMGROUP'' ';

   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done CODE_ROOT_SOURCE and CODE_ROOT_TERMGROUP',work_id,0,30);

   MEME_UTILITY.put_message('Done normalizing UI types');

/**
   -- ANALYZE l_table if no statistics
   msp_location := '10';
   SELECT count(*) INTO stats_ct
   FROM user_indexes WHERE table_name = upper(l_table)
   AND (last_analyzed is null OR last_analyzed < (sysdate-1));

   -- Soma changing 10g performance
   IF stats_ct > 0 THEN
       MEME_SYSTEM.analyze(l_table);
   END IF;
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done analyzing table',work_id,0,40);

   -- ANALYZE relationships_ui if no statistics
   msp_location := '20';
   SELECT count(*) INTO stats_ct
   FROM user_indexes WHERE table_name ='RELATIONSHIPS_UI'
   AND (last_analyzed is null OR last_analyzed < (sysdate-1));
   IF stats_ct > 0 THEN
       MEME_SYSTEM.analyze('RELATIONSHIPS_UI');
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done analyzing relationship_ui',work_id,0,50);

   MEME_UTILITY.put_message('Done analyzing tables');
**/
    msp_location := '40';
    -- Find matching RUIs 
    -- For C level rels, ignore rela if source != MTHRELA
    -- Ignore P level rels and unreleasable rels

    -- Create a table to do assignment
    msp_location := '50.1';
    MEME_UTILITY.drop_it('table','source_relationships_rui_map');
    IF lower(l_table) = 'source_relationships' OR
       lower(l_table) = 'source_context_relationships' THEN
        msp_location := '50.2a';
        EXECUTE IMMEDIATE
            'CREATE TABLE source_relationships_rui_map NOLOGGING AS
             SELECT a.relationship_id, b.rui
             FROM ' || l_table || ' a, relationships_ui b,
              source_rank ns, source_rank ss
             WHERE a.relationship_level = b.relationship_level
               AND b.root_source = ss.stripped_source
               AND ns.normalized_source = ss.source
               AND ns.source = a.source
               AND a.relationship_name = b.relationship_name
               AND a.relationship_attribute || a.source_rui || a.sg_qualifier_1 || a.sg_qualifier_2 ||
                   a.sg_id_1 || a.sg_type_1 || a.sg_id_2 || a.sg_type_2 = 
                   b.relationship_attribute || b.source_rui || b.sg_qualifier_1 || b.sg_qualifier_2 ||
                   b.sg_id_1 || b.sg_type_1 || b.sg_id_2 || b.sg_type_2
              AND a.relationship_level != ''P''
              AND a.tobereleased not in (''N'',''n'') ';
    ELSE
        msp_location := '50.2b';
        EXECUTE IMMEDIATE
            'CREATE TABLE source_relationships_rui_map NOLOGGING AS
            -- Haining, Siebel C17ND, remove USE_HASH for 10g
             SELECT a.relationship_id, b.rui
             FROM (SELECT relationship_id, DECODE(relationship_level,
                         ''C'',DECODE(source,''MTHRELA'',''MTHRELA'',''MTH''),
                         ''S'',DECODE(SUBSTR(source,1,2),
                                ''E-'',''MTH'',
                                ''L-'',''MTH'',
                                ''S-'',''MTH'', source)) source,
                        relationship_level, relationship_name,
                        relationship_attribute,
                        sg_id_1, sg_type_1, sg_qualifier_1,
                        sg_id_2, sg_type_2, sg_qualifier_2, source_rui
               FROM ' || l_table || ' aa
               WHERE rui IS NULL AND relationship_level != ''P''
                AND tobereleased not in (''N'',''n'')
               ) a, relationships_ui b,
              source_rank ns, source_rank ss
             WHERE a.relationship_level = b.relationship_level
               AND b.root_source = ss.stripped_source
               AND ns.normalized_source = ss.source
               AND ns.source = a.source
               AND a.relationship_name = b.relationship_name
               AND (NVL(a.relationship_attribute,''null'') =
                    NVL(b.relationship_attribute,''null'') OR
                        (a.source not in (''MTHRELA'') AND a.relationship_level=''C'' AND
                       b.relationship_attribute IS NULL))
               AND NVL(a.source_rui,''null'') =
                   NVL(b.source_rui,''null'')
               AND b.sg_type_1 = a.sg_type_1
               AND NVL(b.sg_qualifier_1,''null'') =
                   NVL(a.sg_qualifier_1,''null'')
               AND b.sg_id_1 = a.sg_id_1
               AND b.sg_type_2 = a.sg_type_2
               AND NVL(b.sg_qualifier_2,''null'') =
                   NVL(a.sg_qualifier_2,''null'')
               AND b.sg_id_2 = a.sg_id_2';
    END IF;
    
    msp_location := '60.1';
    EXECUTE IMMEDIATE
	'ALTER TABLE source_relationships_rui_map ADD PRIMARY KEY (relationship_id)';

	MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done creating source_relationships_rui_map',work_id,0,50);
   MEME_UTILITY.put_message('Done creating and analyzing table for assigned RUIs');
    
    msp_location := '60.2';
    EXECUTE IMMEDIATE
       'UPDATE (SELECT a.rui, b.rui new_rui
                FROM ' || l_table || ' a, source_relationships_rui_map b
                WHERE a.relationship_id = b.relationship_id
                  AND a.tobereleased not in (''N'',''n'')
                  AND a.rui IS NULL)
        SET rui = new_rui';

    MEME_UTILITY.put_message(SQL%ROWCOUNT || 
		' ruis assigned (' || l_table || ')');

    COMMIT;
 
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done find matching RUI',work_id,0,60);

    -- At this point, any ruis that are null are new
    -- Ignore P level rels and unreleasable rels
    msp_location := '110';
    MEME_UTILITY.drop_it('table','source_relationships_rui_map');
    msp_location := '120';
    EXECUTE IMMEDIATE
           'CREATE TABLE source_relationships_rui_map NOLOGGING AS
            SELECT
                   relationship_id as rui, rs.stripped_source as root_source,
                   relationship_level, relationship_name,
                   DECODE(a.relationship_level,
                            ''S'',relationship_attribute,
                            DECODE(a.source,''MTHRELA'',relationship_attribute,
                            null))
                        as relationship_attribute,
                   sg_id_1, sg_type_1, sg_qualifier_1,
                   sg_id_2, sg_type_2, sg_qualifier_2, a.source_rui
            FROM ' || l_table || ' a, source_rank ns, source_rank rs
            WHERE DECODE(relationship_level,
                 ''C'',''MTH'',
                 ''S'',DECODE(SUBSTR(a.source,1,2),
                        ''E-'',''MTH'',
                        ''L-'',''MTH'',
                        ''S-'',''MTH'', a.source)) = ns.source
              AND ns.normalized_source = rs.source
              AND rui is null
              AND a.relationship_level != ''P''
              AND a.tobereleased not in (''N'',''n'') ';

              MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done ignoring P level and unreleasable rels',work_id,0,70);
   MEME_UTILITY.put_message('Done creating and analyzing table for new RUIs');

    -- Insert inverses (don't make duplicates).
    msp_location := '130';
    EXECUTE IMMEDIATE
	   'INSERT INTO source_relationships_rui_map
	      SELECT 0, root_source, relationship_level, inverse_name,
		   inverse_rel_attribute, sg_id_2, sg_type_2, sg_qualifier_2,
		   sg_id_1, sg_type_1, sg_qualifier_1, a.source_rui
	      FROM source_relationships_rui_map a, inverse_relationships b, 
	  	   inverse_rel_attributes c
	      WHERE a.relationship_name = b.relationship_name
	        AND NVL(a.relationship_attribute,''null'') = 
		    NVL(c.relationship_attribute,''null'') ';
    COMMIT;

    msp_location := '300';
    MEME_UTILITY.drop_it('table','source_relationships_rui');
    msp_location := '310';
    EXECUTE IMMEDIATE
       'CREATE TABLE source_relationships_rui NOLOGGING AS
	SELECT rownum as rui, root_source,
	  	relationship_level, relationship_name, 
		relationship_attribute,
	   	sg_id_1, sg_type_1, sg_qualifier_1,
		sg_id_2, sg_type_2, sg_qualifier_2, source_rui
 	FROM (SELECT DISTINCT 0 as rui, root_source,
	  		relationship_level, relationship_name, 
			relationship_attribute,
	   		sg_id_1, sg_type_1, sg_qualifier_1,
			sg_id_2, sg_type_2, sg_qualifier_2, source_rui
	      FROM source_relationships_rui_map)';
	
    msp_location := '340';
    rui_ct := MEME_UTILITY.exec_select(
	'SELECT COUNT(*) FROM source_relationships_rui');

    msp_location := '350';
    UPDATE max_tab SET max_id = max_id + rui_ct
    WHERE table_name = 'RUI';

    msp_location := '360';
    EXECUTE IMMEDIATE
    'UPDATE source_relationships_rui
	 SET rui = (SELECT rui + max_id - ' || rui_ct || '
	 FROM max_tab WHERE table_name = ''RUI'')';
	
    msp_location := '370.0';
    MEME_UTILITY.drop_it('table','new_relationships_ui');
    msp_location := '380.1';
    SELECT value INTO rui_prefix FROM code_map
    WHERE type = 'ui_prefix' AND code = 'RUI';
    msp_location := '380.2';
    SELECT to_number(value) INTO rui_length FROM code_map
    WHERE type = 'ui_length' AND code = 'RUI';
    msp_location := '380.3';
    EXECUTE IMMEDIATE
       'CREATE TABLE new_relationships_ui (rui, root_source, 
		relationship_level,
		relationship_name, relationship_attribute, 
		sg_id_1, sg_type_1, sg_qualifier_1,
		sg_id_2, sg_type_2, sg_qualifier_2, source_rui) NOLOGGING AS
	SELECT ''' || rui_prefix || ''' || 
		LPAD(rui, ' || rui_length || ', 0), root_source, relationship_level,
	 	relationship_name, relationship_attribute, 
		sg_id_1, sg_type_1, sg_qualifier_1,
		sg_id_2, sg_type_2, sg_qualifier_2, source_rui
	FROM source_relationships_rui';

    msp_location := '381';
    EXECUTE IMMEDIATE
	'CREATE INDEX xnrui_1 on new_relationships_ui (sg_id_1,sg_type_1) TABLESPACE MIDI
	 COMPUTE STATISTICS PARALLEL NOLOGGING';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done inserting inverses',work_id,0,80);
   MEME_UTILITY.put_message('Done preparing tables for new RUI assignment');

    -- Set remaining UIs
    -- Ignore P level rels and unreleasable rels
    msp_location := '400.0';
    MEME_UTILITY.drop_it('table','source_relationships_rui_map');
    msp_location := '400.1';
    IF lower(l_table) = 'source_relationships' OR
       lower(l_table) = 'source_context_relationships' THEN
        msp_location := '50.2a';
        EXECUTE IMMEDIATE
            'CREATE TABLE source_relationships_rui_map NOLOGGING AS
             SELECT a.relationship_id, b.rui
             FROM ' || l_table || ' a, new_relationships_ui b,
              source_rank ns, source_rank ss
             WHERE a.relationship_level = b.relationship_level
               AND b.root_source = ss.stripped_source
               AND ns.normalized_source = ss.source
               AND ns.source = a.source
               AND a.relationship_name = b.relationship_name
               AND a.relationship_attribute || a.source_rui || a.sg_qualifier_1 || a.sg_qualifier_2 ||
                   a.sg_id_1 || a.sg_type_1 || a.sg_id_2 || a.sg_type_2 = 
                   b.relationship_attribute || b.source_rui || b.sg_qualifier_1 || b.sg_qualifier_2 ||
                   b.sg_id_1 || b.sg_type_1 || b.sg_id_2 || b.sg_type_2';
    ELSE
       EXECUTE IMMEDIATE
        'CREATE TABLE source_relationships_rui_map NOLOGGING AS
        -- Haining, Siebel C17ND, remove USE_HASH for 10g
         SELECT a.relationship_id, b.rui
         FROM (SELECT relationship_id, DECODE(relationship_level,
                      ''C'',DECODE(source,''MTHRELA'',''MTHRELA'',''MTH''),
                      ''S'',DECODE(SUBSTR(source,1,2),
                                ''E-'',''MTH'',
                                ''L-'',''MTH'',
                                ''S-'',''MTH'', source)) source,
                        relationship_level, relationship_name,
                        relationship_attribute,
                        sg_id_1, sg_type_1, sg_qualifier_1,
                        sg_id_2, sg_type_2, sg_qualifier_2, source_rui
               FROM ' || l_table || ' aa
               WHERE rui IS NULL AND relationship_level != ''P''
                AND tobereleased not in (''N'',''n'')
               ) a, new_relationships_ui b,
              source_rank ns, source_rank ss
         WHERE a.relationship_level = b.relationship_level
           AND b.root_source = ss.stripped_source
           AND ns.normalized_source = ss.source
           AND ns.source = a.source
           AND a.relationship_name = b.relationship_name
           AND (NVL(a.relationship_attribute,''null'') =
                NVL(b.relationship_attribute,''null'') OR
                    (a.source != ''MTHRELA'' AND a.relationship_level=''C'' AND
                   b.relationship_attribute IS NULL))
           AND NVL(a.source_rui,''null'') =
               NVL(b.source_rui,''null'')
           AND b.sg_type_1 = a.sg_type_1
           AND NVL(b.sg_qualifier_1,''null'') =
               NVL(a.sg_qualifier_1,''null'')
           AND b.sg_id_1 = a.sg_id_1
           AND b.sg_type_2 = a.sg_type_2
           AND NVL(b.sg_qualifier_2,''null'') =
               NVL(a.sg_qualifier_2,''null'')
           AND b.sg_id_2 = a.sg_id_2';
    END IF;
    msp_location := '400.2';

    EXECUTE IMMEDIATE
	'ALTER TABLE source_relationships_rui_map ADD PRIMARY KEY (relationship_id)';

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done creating source_relationships_rui_map (new RUI)',work_id,0,50);
   MEME_UTILITY.put_message('Done creating and analyzing assignment table for new RUIs');
    
    msp_location := '400.3';
    EXECUTE IMMEDIATE
       'UPDATE (SELECT a.rui, b.rui new_rui
                FROM ' || l_table || ' a, source_relationships_rui_map b
                WHERE a.relationship_id = b.relationship_id
                  AND a.tobereleased not in (''N'',''n'')
                  AND a.rui IS NULL)
        SET rui = new_rui';

    MEME_UTILITY.put_message(SQL%ROWCOUNT || ' new ruis assigned (' || l_table || ')');

    msp_location := '410';
    EXECUTE IMMEDIATE 
	'INSERT INTO relationships_ui 
	       (rui, root_source, relationship_level,
		relationship_name, relationship_attribute,
		sg_id_1, sg_type_1, sg_qualifier_1,
		sg_id_2, sg_type_2, sg_qualifier_2, source_rui)
	 SELECT DISTINCT * FROM new_relationships_ui ';

    msp_location := '415.1';
    MEME_UTILITY.drop_it('table','new_inverse_relationships_ui');
    msp_location := '415.2';
    EXECUTE IMMEDIATE
       'CREATE TABLE new_inverse_relationships_ui (rui, inverse_rui) NOLOGGING AS
	SELECT DISTINCT /*+ USE_MERGE(a,b) */
		a.rui as rui, b.rui as inverse_rui
	FROM new_relationships_ui a, 
  	 (SELECT rui, sg_id_1, sg_type_1, sg_qualifier_1,
		sg_id_2, sg_type_2, sg_qualifier_2, 
		root_source, inverse_name, 
		inverse_rel_attribute, relationship_level, bb.source_rui
          FROM new_relationships_ui bb, inverse_relationships c, 
	       inverse_rel_attributes d
	  WHERE bb.relationship_name = c.relationship_name
	   AND NVL(bb.relationship_attribute,''null'') = 
	       NVL(d.relationship_attribute,''null'')
 	 ) b 
  	WHERE a.root_source = b.root_source
	  AND a.sg_id_1 = b.sg_id_2
	  AND a.sg_id_2 = b.sg_id_1
	  AND a.sg_type_1 = b.sg_type_2
	  AND a.sg_type_2 = b.sg_type_1
	  AND a.relationship_level = b.relationship_level
	  AND NVL(a.source_rui,''null'') = NVL(b.source_rui,''null'')
	  AND NVL(a.sg_qualifier_1,''null'') = NVL(b.sg_qualifier_2,''null'')
	  AND NVL(a.sg_qualifier_2,''null'') = NVL(b.sg_qualifier_1,''null'')
	  AND a.relationship_name = b.inverse_name
	  AND NVL(a.relationship_attribute,''null'') = 
	      NVL(b.inverse_rel_attribute,''null'') ';

    msp_location := '415';
    EXECUTE IMMEDIATE 
    	'INSERT /*+ APPEND */ INTO inverse_relationships_ui 
	   (rui, inverse_rui)
	 SELECT DISTINCT rui, inverse_rui FROM new_inverse_relationships_ui';

   MEME_UTILITY.put_message('Done loading new RUIs and inverse RUIs');

   msp_location := '430';
    MEME_UTILITY.drop_it('table','source_relationships_rui');
    MEME_UTILITY.drop_it('table','source_relationships_rui_map');
    MEME_UTILITY.drop_it('table','new_relationships_ui');
    MEME_UTILITY.drop_it('table','new_inverse_relationships_ui');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done set remaining UIs',work_id,0,90);

    msp_location := '440';
    MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Assign Identifiers',
	detail => 'Assign relationship identifiers (RUI, ' || l_table || ')',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_ruis',
    	'Done processing assign_ruis',work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN assign_ruis_exc THEN
      MEME_UTILITY.drop_it('table','source_relationships_rui_map');
      MEME_UTILITY.drop_it('table','source_relationships_rui');
      MEME_UTILITY.drop_it('table','new_relationships_ui');
      MEME_UTILITY.drop_it('table','new_inverse_relationships_ui');
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table','source_relationships_rui_map');
      MEME_UTILITY.drop_it('table','source_relationships_rui');
      MEME_UTILITY.drop_it('table','new_relationships_ui');
      MEME_UTILITY.drop_it('table','new_inverse_relationships_ui');
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END assign_ruis;

/* PROCEDURE ASSIGN_STRING_UIS *************************************************
 */
PROCEDURE assign_string_uis(
   authority IN VARCHAR2,
   work_id   IN INTEGER
)
IS
   TYPE curvar_type	 IS REF CURSOR;
   curvar		 curvar_type;
   l_string_pre		 VARCHAR2(30);
   l_string		 VARCHAR2(3000);
   l_language		 VARCHAR2(30);
   l_ui			 INTEGER;
   l_sui		 VARCHAR2(30);
   l_isui		 VARCHAR2(30);
   l_lui		 VARCHAR2(30);
   row_id		 ROWID;
   sui_ct		 INTEGER;
   isui_ct		 INTEGER;
   lui_ct		 INTEGER;
   null_ct		 INTEGER;
   stats_ct		 INTEGER;
   assign_string_uis_exc EXCEPTION;
 
   sui_prefix		 VARCHAR2(10);
   sui_length		 INTEGER;
   isui_prefix		 VARCHAR2(10);
   isui_length		 INTEGER;
   lui_prefix		 VARCHAR2(10);
   lui_length		 INTEGER;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Starting assign_string_uis',0,work_id,0,1);

   initialize_trace('ASSIGN_STRING_UIS');

   MEME_UTILITY.sub_timing_start;
   MEME_UTILITY.put_message('Starting assign_string_uis');
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done preparing statistics.',0,work_id,0,5);

   --
   -- Assign sui, lui, isui to source_string_ui
   --
   msp_location := '100';
   UPDATE (SELECT /*+ PARALLEL(a) */ a.sui sui1, b.sui sui2, a.lui lui1, b.lui lui2,
                  a.isui isui1, b.isui isui2
           FROM source_string_ui a, string_ui b
           WHERE a.string=b.string and a.language=b.language)
   SET sui1=sui2, lui1=lui2, isui1=isui2;
         
         
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done assigning SUI,ISUI,LUI where available.',0,work_id,0,10);
   MEME_UTILITY.put_message('Done assigning SUI,ISUI,LUI where available.');

   --
   -- Assign lui, isui to source_string_ui */
   -- This should only be performed for ENG strings
   -- The problem is that LUIs are assigned exactly
   -- as SUIs, the norm string is the string.  
   -- This allows an ISUI assigned (like in english)
   -- to have >! LUIs. 
   --
   -- The added "norm_string" clause ensures that
   -- cases where an ISUI has >1 LUI, we keep only the
   -- matching LUI example.  This was needed for CPT2007
   -- insertion because of a bug in LVG.
   --
   msp_location := '110';
   UPDATE /*+ NOPARALLEL */ source_string_ui a
   SET (a.lui, a.isui) =
      (SELECT DISTINCT lui, isui 
       FROM string_ui b
       WHERE LOWER(a.string) = LOWER(b.string)
         AND a.language = b.language 
         AND a.norm_string = b.norm_string) 
   WHERE a.sui IS NULL AND a.language='ENG';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done assigning ISUI,LUI where available.',0,work_id,0,15);
   MEME_UTILITY.put_message('Done assigning ISUI,LUI where available.');

   --
   -- For foreign sources, assign isui where STRINGS match
   -- but this is already done above.
   --

   --
   -- Assign lui to source_string_ui
   -- For foreign strings this will produce zero rows
   --
   msp_location := '120';
   UPDATE /*+ NOPARALLEL */ source_string_ui a
   SET lui =
       (SELECT DISTINCT lui 
     	FROM string_ui b
        WHERE a.norm_string = b.norm_string
          AND a.language = b.language)
   WHERE a.sui IS NULL AND a.isui IS NULL;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',   
    	'Done assigning LUI where available.',0,work_id,0,20);
   MEME_UTILITY.put_message('Done assigning LUI where available.');

   --
   -- Handle strings with null norm strings.
   --
   -- Soma changed for processing SNOMEDCT null norm strings.
   msp_location := '125';
   UPDATE /*+ NOPARALLEL */ source_string_ui a
   SET lui =
       (SELECT DISTINCT lui 
	FROM string_ui b
      	WHERE b.norm_string_pre IS NULL)
   WHERE a.sui IS NULL AND a.isui IS NULL
   AND norm_string_pre IS NULL;
   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done handling null norm strings.',0,work_id,0,25);
   MEME_UTILITY.put_message('Done handling null norm strings.');

   --
   -- Create source_string_sui
   --
   msp_location := '130';
   MEME_UTILITY.drop_it('table','source_string_sui');
   msp_location := '135';
   EXECUTE IMMEDIATE
       'CREATE TABLE source_string_sui NOLOGGING AS
	SELECT string, language, rownum AS sui
	FROM source_string_ui
	WHERE sui IS NULL';
   msp_location := '136';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done finding new strings.',0,work_id,0,30);
   MEME_UTILITY.put_message('Done identifying new strings.');

   --
   -- Create source_string_isui table
   --
   msp_location := '140';
   MEME_UTILITY.drop_it('table','source_string_isui');
   msp_location := '145';
   EXECUTE IMMEDIATE
       'CREATE TABLE source_string_isui NOLOGGING AS
	SELECT DISTINCT (LOWER(string)) AS lower_string,
	  language, 0 AS isui
	FROM source_string_ui
	WHERE isui IS NULL';
   msp_location := '147';
   local_exec('UPDATE source_string_isui a SET isui = rownum');
   msp_location := '148';
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done finding new case-insensitive strings.',0,work_id,0,35);
   MEME_UTILITY.put_message('Done identifying new case-insensitive strings.');

   --
   -- Create source_string_lui table
   --
   msp_location := '150';
   MEME_UTILITY.drop_it('table','source_string_lui');
   msp_location := '155';
   EXECUTE IMMEDIATE
       'CREATE TABLE source_string_lui NOLOGGING AS
	SELECT DISTINCT norm_string, language, 0 AS lui
	FROM source_string_ui
	WHERE lui IS NULL';
   msp_location := '157';
   local_exec('UPDATE source_string_lui SET lui = rownum');
   msp_location := '158';
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done finding new norm strings.',0,work_id,0,40);
   MEME_UTILITY.put_message('Done identifying new norm strings.');

   --
   -- Count new SUI, ISUI, and LUIs
   --
   msp_location := '160.1';
   sui_ct :=MEME_UTILITY.exec_select('SELECT COUNT(*) FROM source_string_sui');
   msp_location := '160.2';
   isui_ct:=MEME_UTILITY.exec_select('SELECT COUNT(*) FROM source_string_isui');
   msp_location := '160.3';
   lui_ct :=MEME_UTILITY.exec_select('SELECT COUNT(*) FROM source_string_lui');

   --
   -- Reserve max_tab space
   --
   msp_location := '170';
   UPDATE max_tab SET max_id = max_id + sui_ct
   WHERE table_name = 'SUI';
   msp_location := '180';
   UPDATE max_tab SET max_id = max_id + isui_ct
   WHERE table_name = 'ISUI';
   msp_location := '190';
   UPDATE max_tab SET max_id = max_id + lui_ct
   WHERE table_name = 'LUI';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done looking up new SUI,ISUI,LUI ranges.',0,work_id,0,45);

   --
   -- Assign new SUI, ISUI, LUI values
   --
   msp_location := '200';
   EXECUTE IMMEDIATE
       'UPDATE /*+ PARALLEL(a) */ source_string_sui a
	SET sui = (SELECT sui + max_id - '||sui_ct||
	' FROM max_tab WHERE table_name = ''SUI'')';
   msp_location := '200.2';
   EXECUTE IMMEDIATE 
	'CREATE UNIQUE INDEX x_source_string_sui ON source_string_sui(string) TABLESPACE MIDI 
	 COMPUTE STATISTICS PARALLEL NOLOGGING';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done assigning new SUI values.',0,work_id,0,50);

   msp_location := '210';
   EXECUTE IMMEDIATE
       'UPDATE /*+ PARALLEL(a) */ source_string_isui a
	SET isui = (SELECT isui + max_id - '||isui_ct||
	' FROM max_tab WHERE table_name = ''ISUI'')';
   msp_location := '210.2';
   EXECUTE IMMEDIATE 
	'CREATE UNIQUE INDEX x_source_string_isui ON source_string_isui(lower_string) TABLESPACE MIDI 
	 COMPUTE STATISTICS PARALLEL NOLOGGING';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done assigning new ISUI values.',0,work_id,0,55);

   msp_location := '220';
   EXECUTE IMMEDIATE
       'UPDATE /*+ PARALLEL(a) */ source_string_lui a
	SET lui = (SELECT lui + max_id - '||lui_ct||
	' FROM max_tab WHERE table_name = ''LUI'')';
   msp_location := '220.2';
   EXECUTE IMMEDIATE 
	'CREATE UNIQUE INDEX x_source_string_lui ON source_string_lui(norm_string) TABLESPACE MIDI 
	 COMPUTE STATISTICS PARALLEL NOLOGGING';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done assigning new LUI values.',0,work_id,0,60);
   MEME_UTILITY.put_message('Done assigning new SUI, ISUI, and LUI values.');

    msp_location := '230.1';
    SELECT value INTO sui_prefix FROM code_map
    WHERE type = 'ui_prefix' AND code = 'SUI';
    msp_location := '230.2';
    SELECT to_number(value) INTO sui_length FROM code_map
    WHERE type = 'ui_length' AND code = 'SUI';
    msp_location := '230.3';
    EXECUTE IMMEDIATE
	'UPDATE source_string_ui a
   	 SET sui = 
	  (SELECT :prefix||LPAD(b.sui, :len, 0)
	   FROM source_string_sui b
	   WHERE a.string = b.string
	     AND a.language = b.language)
   	 WHERE sui IS NULL'
   USING sui_prefix, sui_length;   
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' suis assigned.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done setting new SUIs.',0,work_id,0,65);

    msp_location := '240.1';
    SELECT value INTO isui_prefix FROM code_map
    WHERE type = 'ui_prefix' AND code = 'ISUI';
    msp_location := '240.2';
    SELECT to_number(value) INTO isui_length FROM code_map
    WHERE type = 'ui_length' AND code = 'ISUI';
    msp_location := '240.3';
   EXECUTE IMMEDIATE
   	'UPDATE source_string_ui a
   	 SET isui = 
	  (SELECT :prefix || LPAD(b.isui, :len, 0)
	   FROM source_string_isui b
	   WHERE lower(a.string) = b.lower_string
	     AND a.language = b.language)
   	 WHERE isui IS NULL'
   USING isui_prefix, isui_length;   
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' isuis assigned.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done setting new ISUIs.',0,work_id,0,70);

    msp_location := '250.1';
    SELECT value INTO lui_prefix FROM code_map
    WHERE type = 'ui_prefix' AND code = 'LUI';
    msp_location := '250.2';
    SELECT to_number(value) INTO lui_length FROM code_map
    WHERE type = 'ui_length' AND code = 'LUI';
    msp_location := '250.3';
   EXECUTE IMMEDIATE
   	'UPDATE source_string_ui a
       	 SET lui = 
	  (SELECT :prefix ||LPAD(b.lui, :len, 0)
	   FROM source_string_lui b
	   WHERE a.norm_string = b.norm_string
	     AND a.language = b.language)
   	 WHERE lui IS NULL'
   USING lui_prefix, lui_length;   
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' luis assigned.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done setting new LUIs.',0,work_id,0,75);

   --
   -- Error check, no null sui,lui,isui
   --
   msp_location := '255';
   SELECT /*+ PARALLEL(a) */ COUNT(*) INTO null_ct 
   FROM source_string_ui a
   WHERE sui IS NULL OR isui IS NULL OR lui IS NULL;
   IF null_ct > 0 THEN
      msp_error_code := 22;
      msp_error_detail := 'table_name=source_string_ui';
      RAISE assign_string_uis_exc;
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done with QA checks',0,work_id,0,80);

   --
   -- New SUIs
   --
   msp_location := '270.1';
   MEME_UTILITY.drop_it('table','new_string_ui');
   msp_location := '270.2';
   EXECUTE IMMEDIATE
      	'CREATE TABLE new_string_ui NOLOGGING AS
  	 SELECT DISTINCT lui, sui, string_pre, norm_string_pre, language, base_string,
	  string, norm_string, isui, lowercase_string_pre
	 FROM source_string_ui a
 	 WHERE sui IN (SELECT ''' || sui_prefix || ''' || 
		LPAD(b.sui, ' || sui_length || ', 0) from source_string_sui b)';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done preparing new string_ui entries',0,work_id,0,90);

   msp_location := '280';
   EXECUTE IMMEDIATE
       'INSERT INTO string_ui
	 (lui, sui, string_pre, norm_string_pre, language, base_string,
	  string, norm_string, isui, lowercase_string_pre)
	SELECT 
	  lui, sui, string_pre, norm_string_pre, language, base_string,
	  string, norm_string, isui, lowercase_string_pre
	FROM new_string_ui';

   msp_location := '300.1';
   EXECUTE IMMEDIATE 'CREATE UNIQUE INDEX x_ssui_string_lat on source_string_ui(string,language) NOLOGGING';
    msp_location := '300';
   UPDATE /*+ PARALLEL(a) */ source_classes_atoms a
   SET (sui,isui,lui) =
	(SELECT sui, isui, lui FROM source_string_ui b
	 WHERE string = atom_name
	   AND a.language = b.language );
   msp_location := '300.2';
   MEME_UTILITY.drop_it('index','x_ssui_string_lat');


   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_string_uis',
    	'Done loading new string_ui rows',0,work_id,0,95);

   COMMIT;

   msp_location := '310';
   MEME_UTILITY.drop_it('table','source_string_sui');
   MEME_UTILITY.drop_it('table','source_string_isui');
   MEME_UTILITY.drop_it('table','source_string_lui');
   MEME_UTILITY.drop_it('table','new_sui');
   MEME_UTILITY.drop_it('table','new_string_ui');

   msp_location := '320';
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Assign Identifiers',
	detail => 'Assign string identifiers (LUI, SUI, ISUI)',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.reset_progress(work_id => work_id );

   COMMIT;

EXCEPTION
   WHEN assign_string_uis_exc THEN
      MEME_UTILITY.drop_it('table','source_string_sui');
      MEME_UTILITY.drop_it('table','source_string_isui');
      MEME_UTILITY.drop_it('table','source_string_lui');
      MEME_UTILITY.drop_it('table','new_sui');
      MEME_UTILITY.drop_it('table','new_string_ui');
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table','source_string_sui');
      MEME_UTILITY.drop_it('table','source_string_isui');
      MEME_UTILITY.drop_it('table','source_string_lui');
      MEME_UTILITY.drop_it('table','new_sui');
      MEME_UTILITY.drop_it('table','new_string_ui');
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END assign_string_uis;

/* PROCEDURE ASSIGN_SOURCE_IDS *************************************************
 */
PROCEDURE assign_source_ids(
   table_name IN VARCHAR2,
   authority  IN VARCHAR2,   /* not used */
   work_id    IN INTEGER
)
IS
   ct	      INTEGER;
   assign_source_ids_exc EXCEPTION;
BEGIN

   initialize_trace('ASSIGN_SOURCE_IDS');

   msp_error_code := 43; /* Usage blocked. */
   msp_error_detail := 'This procedure should not be called.';
   RAISE assign_source_ids_exc;

EXCEPTION
   WHEN assign_source_ids_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;
 
   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END assign_source_ids;

/* PROCEDURE ASSIGN_MEME_IDS ***************************************************
 */
PROCEDURE assign_meme_ids(
   table_name IN VARCHAR2,
   authority  IN VARCHAR2,   /* not used */
   work_id    IN INTEGER
)
IS
   ct	      INTEGER;
   l_start	INTEGER;
   l_end	INTEGER;
   assign_meme_ids_exc EXCEPTION;

BEGIN
   msp_error_detail := '';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_meme_ids',
    	'Starting assign_meme_ids',work_id,0,1);

   initialize_trace('ASSIGN_MEME_IDS');
   MEME_UTILITY.sub_timing_start;

   IF UPPER(table_name) = 'C' OR UPPER(table_name) = 'ALL' THEN

	--
 	-- Report cases of illegal termgroup
	--
      	msp_location := '100.3';
	SELECT count(*) INTO ct
        FROM (SELECT termgroup,suppressible FROM source_classes_atoms
              WHERE suppressible not in ('E','O') MINUS
              SELECT termgroup,suppressible y FROM termgroup_rank);
	IF ct != 0 THEN
	    msp_error_detail := 'Illegal termgroup,suppressible combo in source_classes_atoms';
            msp_error_code := 45;
            RAISE msp_exception;
        END IF;

   	MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_meme_ids',
    		'Done checking correct termgroup_ranks, ttys, and suppressible values',work_id,0,20);

      	-- set max atom id in max_tab
      	msp_location := '105';
      	UPDATE max_tab SET max_id = NVL((select max(atom_id) from source_classes_atoms),max_id)
      	WHERE table_name = 'ATOMS';

   	MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_meme_ids',
    		'Done assigning meme_ids for source_classes_atoms',work_id,0,40);

    		
    -- Atom ordering section is now handled as an explicit recipe step, no need to do it here.


   	MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_meme_ids',
    		'Done assigninig ids for classes',work_id,0,80);

      --MEME_SYSTEM.analyze('source_classes_atoms');

   END IF;

   IF UPPER(table_name) = 'R' OR UPPER(table_name) = 'ALL' THEN

      msp_location := '125';
      UPDATE max_tab SET max_id = 
              NVL((SELECT max(max_id) FROM 
                    (SELECT max(relationship_id) max_id FROM source_relationships
                     UNION SELECT max(relationship_id) max_id FROM source_context_relationships)),
                  max_id)
      WHERE table_name = 'RELATIONSHIPS';

      --MEME_SYSTEM.analyze('source_relationships');

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_meme_ids',
    	      'Done assigning meme_id for relationship',work_id,0,80);

   END IF;

   IF UPPER(table_name) = 'CR' OR UPPER(table_name) = 'ALL' THEN

      msp_location := '145';
      UPDATE max_tab SET max_id = 
              NVL((SELECT max(max_id) FROM 
                    (SELECT max(relationship_id) max_id FROM source_relationships
                     UNION SELECT max(relationship_id) max_id FROM source_context_relationships)),
                  max_id)
      WHERE table_name = 'RELATIONSHIPS';

      --MEME_SYSTEM.analyze('source_context_relationships');

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_meme_ids',
    	      'Done assigning meme_ids for context_relationships',work_id,0,80);

   END IF;

   IF UPPER(table_name) = 'A' OR UPPER(table_name) = 'ALL' THEN

      	msp_location := '165.2';
      	UPDATE max_tab SET max_id = NVL((select max(attribute_id) from source_attributes),max_id)
      	WHERE table_name = 'ATTRIBUTES';

        -- Get max string id to update attributes
      	msp_location := '175';
        SELECT row_sequence INTO ct
        FROM stringtab
        WHERE string_id = -1;

      	msp_location := '175.4';
      	UPDATE stringtab a
        SET row_sequence = NVL((select max(string_id) from source_stringtab),row_sequence)
      	WHERE string_id = -1;
	    COMMIT;

      	msp_location := '175.7';
        EXECUTE IMMEDIATE
	   'UPDATE /*+ PARALLEL(a) */ source_attributes a 
	    SET attribute_value = ''<>Long_Attribute<>:'' ||
	      (to_number(substr(attribute_value,20))+ ' || ct || ')
	    WHERE attribute_value like ''<>Long_Attribute<>:%'' ' ;

      --MEME_SYSTEM.analyze('source_stringtab');
      --MEME_SYSTEM.analyze('source_attributes');

        MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_meme_ids',
    	      'Done assigning meme_ids for attributes',work_id,0,80);

   END IF;

   IF UPPER(table_name) = 'CS' OR UPPER(table_name) = 'ALL' THEN

      msp_location := '180.1';
      SELECT COUNT(*) INTO ct FROM source_classes_atoms;

      msp_location := '180.2';
      UPDATE max_tab SET max_id = max_id + ct
      WHERE table_name = 'CONCEPT_STATUS';

      msp_location := '180.3';
      SELECT max_id-ct INTO ct FROM max_tab
      WHERE table_name = 'CONCEPT_STATUS';

      msp_location := '190.1';
      EXECUTE IMMEDIATE
	 'UPDATE source_classes_atoms
	  SET concept_id = rownum + ' || ct;

      msp_location := '190.2';
      MEME_SYSTEM.truncate('source_concept_status');

      msp_location := '190.3';
      MEME_SYSTEM.drop_indexes('source_concept_status');

      msp_location := '200';
      INSERT INTO source_concept_status
	 (switch, source_concept_id, cui,
	 source, status, released, tobereleased,
	 concept_id, preferred_atom_id)
      SELECT 'R', 0, last_assigned_cui, source, 'E', released,
	 tobereleased, concept_id, atom_id
      FROM source_classes_atoms;

      msp_location := '210';
      MEME_SYSTEM.reindex('source_concept_status','N',' ');
	
      -- Soma Changing for 10g performance
      --MEME_SYSTEM.analyze('source_concept_status');

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_meme_ids',
          'Done assigning meme_ids for concept_status',work_id,0,80);

   END IF;

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   msp_location := '201';
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Assign Identifiers',
	detail => 'Assign MEME identifiers (atom_id, attribute_id, relationship_id, concept_id)',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::assign_meme_ids',
          'Done assigning meme_ids',work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,
        msp_error_detail || ': ' || SQLERRM);
      RAISE msp_exception;

END assign_meme_ids;

/* PROCEDURE MAP_TO_MEME_IDS ***************************************************
 */
PROCEDURE map_to_meme_ids(
   table_name  IN VARCHAR2,
   authority   IN VARCHAR2,   /* not used */
   unique_flag IN VARCHAR2,   /* not used */
   work_id     IN INTEGER
)
IS
   row_count           INTEGER;
   loop_ctr            INTEGER;
   map_to_meme_ids_exc EXCEPTION;

   l_row_id            ROWID;
   l_aui	       VARCHAR2(10);
   l_source_atom_id    INTEGER;
   ct  	 	       INTEGER;
   l_parent_treenum    VARCHAR2(512);
   map_parent_treenum  VARCHAR2(512);

   TYPE scr_type IS REF CURSOR;
   scr_curs scr_type;

BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Starting map_to_meme_ids',work_id,0,1);

   initialize_trace('MAP_TO_MEME_IDS');

   row_count := 0;

   IF UPPER(table_name) = 'A' OR UPPER(table_name) = 'ALL' THEN

      -- Call map_sg_fields to map source_attributes
      msp_location := '100';
      MEME_UTILITY.put_message(msp_method ||
	': Executing MEME_SOURCE_PROCESSING.map_sg_fields '||
        'to map source_attributes ...');

      msp_location := '110';
      MEME_SOURCE_PROCESSING.map_sg_fields_all (
         table_name   => 'source_attributes',
         pair_flag    => MEME_CONSTANTS.NO,
         concept_flag => MEME_CONSTANTS.YES);

      msp_location := '111';
      msp_method := 'MAP_TO_MEME_IDS';

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done calling map_sg_fields to map source_attributes',work_id,0,20);

      -- Set atom_id = 0 for concept level attributes
      msp_location := '115';
      UPDATE /*+ PARALLEL(a) */ source_attributes a
      SET atom_id = 0
      WHERE attribute_level = 'C';
      COMMIT;

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done setting atom_id = 0 for concept level attributes',work_id,0,40);

      -- Check tbr
      msp_location := '115';   
      SELECT count(*) INTO ct
      FROM source_attributes WHERE tobereleased not in ('N','n')
        AND atom_id in (SELECT atom_id FROM classes WHERE tobereleased in ('n','N'))
        AND attribute_level = 'S';
      IF ct != 0 THEN
         msp_error_detail := 'Releasable source_attributes entry connected to unreleasable atom';
         msp_error_code := 45;
         RAISE msp_exception;
      END IF;

      -- Set the switch for rows that were mapped
      msp_location := '120';
      UPDATE /*+ PARALLEL(a) */ source_attributes a SET switch='R'
      WHERE (NVL(atom_id,0) != 0 OR attribute_level = 'C') 
	AND NVL(concept_id,0) != 0;
      COMMIT;

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done setting the switch for rows that were mapped',work_id,0,60);

      -- All ids must be mapped unless they are 
      -- CUI or concept level attributes */
      msp_location := '130';
      SELECT COUNT(*) INTO row_count FROM source_attributes
      WHERE ((NVL(atom_id,0) = 0 AND attribute_level != 'C') 
	     OR NVL(concept_id,0) = 0)
        AND sg_type not like 'CUI%';
 
      IF row_count > 0 THEN
 	 msp_error_code := 45;
	 msp_error_detail := 'Source attributes not fully mapped.';
	 RAISE map_to_meme_ids_exc;
      END IF;
	
	-- Delete source_rel_id mappings from source_id_map
	DELETE FROM source_id_map WHERE table_name = 'R';

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done mapping all ids',work_id,0,80);

   END IF;

   IF UPPER(table_name) = 'R' OR UPPER(table_name) = 'ALL' THEN

      -- Call map_sg_fields to map source_relationships
      msp_location := '200';
      MEME_UTILITY.put_message(msp_method||
	': Executing MEME_SOURCE_PROCESSING.map_sg_fields '||
        'to map source_relationships ...');

      msp_location := '210';
      MEME_SOURCE_PROCESSING.map_sg_fields_all (
         table_name   => 'source_relationships',
         pair_flag    => MEME_CONSTANTS.YES,
         concept_flag => MEME_CONSTANTS.YES);

      msp_location := '211';
      msp_method := 'MAP_TO_MEME_IDS';

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done calling map_sg_fields',work_id,0,20);

      -- Set atom_id = 0 for concept level relationships
      msp_location := '215';
      UPDATE /*+ PARALLEL(a) */ source_relationships a
      SET atom_id_1 = 0, atom_id_2 = 0
      WHERE relationship_level = 'C';
      COMMIT;

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done setting atom_id = 0 for concept level relationships',work_id,0,40);

      -- Check tbr
      msp_location := '220';   
      SELECT count(*) INTO ct
      FROM source_relationships WHERE tobereleased not in ('N','n')
        AND atom_id_1 in (SELECT atom_id FROM classes WHERE tobereleased in ('n','N'))
        AND relationship_level = 'S';
      IF ct != 0 THEN
         msp_error_detail := 'Releasable source_relationships entry connected to unreleasable atom';
         msp_error_code := 45;
         RAISE msp_exception;
      END IF;

      msp_location := '221';   
      SELECT count(*) INTO ct
      FROM source_relationships WHERE tobereleased not in ('N','n')
        AND atom_id_2 in (SELECT atom_id FROM classes WHERE tobereleased in ('n','N'))
        AND relationship_level = 'S';
      IF ct != 0 THEN
         msp_error_detail := 'Releasable source_relationships entry connected to unreleasable atom';
         msp_error_code := 45;
         RAISE msp_exception;
      END IF;

      msp_location := '225';
      -- Set switch to R for fully mapped relationships
      UPDATE /*+ PARALLEL(a) */  source_relationships a SET switch='R'
      WHERE (((NVL(atom_id_1,0) != 0 OR relationship_level = 'C') 
	 AND NVL(concept_id_1,0) != 0))
         AND (((NVL(atom_id_2,0) != 0 OR relationship_level = 'C') 
	 AND NVL(concept_id_2,0) != 0));
      COMMIT;

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done setting switch to R for flly mapped relationships',work_id,0,60);

      --
      -- All ids must be mapped unless they are CUI 
      -- or concept level relationships 
      --
      -- Error conditions are:
      -- 1. sg_id_1 goes unmapped OR
      -- 2. sg_id_2 goes unmapped (unless sg_id_1 was an unmapped CUI)
      --
      msp_location := '230';

      SELECT COUNT(*) INTO row_count FROM source_relationships
      WHERE (((NVL(atom_id_1,0) = 0 AND relationship_level != 'C') 
	     OR NVL(concept_id_1,0) = 0) 
            AND sg_type_1 not like 'CUI%')
         OR (((NVL(atom_id_2,0) = 0 AND relationship_level != 'C') 
	 OR NVL(concept_id_2,0) = 0)
            AND sg_type_2 not like 'CUI%');
  
      IF row_count > 0 THEN
	 msp_error_code := 45;
	 msp_error_detail := 'Source relationships not fully mapped.';
	 RAISE map_to_meme_ids_exc;
      END IF;

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done mapping all ids',work_id,0,80);

   END IF;

   IF UPPER(table_name) = 'CR' OR UPPER(table_name) = 'ALL' THEN

      -- Map SOURCE_ATOM_ID
      msp_location := '260';
      MEME_SOURCE_PROCESSING.map_sg_fields_all (
         table_name   => 'source_context_relationships',
         pair_flag    => MEME_CONSTANTS.YES,
         concept_flag => MEME_CONSTANTS.YES);

      msp_location := '261';
      msp_method := 'MAP_TO_MEME_IDS';

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done mapping source atom id',work_id,0,30);

      -- Set switch to R for fully mapped relationships
      msp_location := '262';
      UPDATE /*+ PARALLEL(a) */ source_context_relationships a
      SET switch='R'
      WHERE NVL(atom_id_1,0) != 0 
        AND NVL(atom_id_2,0) != 0
        AND NVL(concept_id_1,0) != 0
        AND NVL(concept_id_2,0) != 0;

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done setting switch to R for fully mapped relationships',work_id,0,60);


      --
      -- Mapping of parent_treenum is now done by cxt_ptr.pl
      -- called within load_src.csh
      --

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done mapping the treenum field',work_id,0,80);

   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_to_meme_ids',
          'Done process map_to_meme_ids',work_id,0,100);

   msp_location := '300';
   MEME_UTILITY.drop_it('table','t_source_context_relationships');
   MEME_UTILITY.put_message(msp_method||' successfully completed.');
   COMMIT;

EXCEPTION
   WHEN map_to_meme_ids_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM || 
	        l_source_atom_id);
      RAISE msp_exception;

END map_to_meme_ids;

/* PROCEDURE INSERT_SOURCE_IDS *************************************************
 */
PROCEDURE insert_source_ids(
   table_name IN VARCHAR2,
   authority  IN VARCHAR2,
   work_id    IN INTEGER
)
IS
   insert_source_ids_exc EXCEPTION;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::insert_source_ids',
          'Starting insert_source_ids',work_id,0,1);

   initialize_trace('INSERT_SOURCE_IDS');
   MEME_UTILITY.sub_timing_start;

   IF UPPER(table_name) = 'C' OR UPPER(table_name) = 'ALL' THEN

      msp_location := '110';
      MEME_UTILITY.drop_it('table','new_source_id_map');
      msp_location := '110.2';
      EXECUTE IMMEDIATE
		'CREATE TABLE new_source_id_map NOLOGGING AS 
		 SELECT atom_id as local_row_id, ''Local'' as origin, 
			''C'' as table_name, source_atom_id AS source_row_id,
       	         '''|| insert_source_ids.authority ||''' AS source
         FROM source_classes_atoms 
		 WHERE switch = ''R'' AND source_atom_id != 0 ';

		 msp_location := '110.3';
      EXECUTE IMMEDIATE
		'UPDATE new_source_id_map
		 SET source = ''SRC''
		 WHERE local_row_id IN 
		   (SELECT atom_id FROM source_classes_atoms
		    WHERE source=''SRC'')';
-- Not sure about the following query but having will not cause any problems.
      msp_location := '110.3';
      EXECUTE IMMEDIATE
		'UPDATE source_id_map
		 SET source = ''SRC''
		 WHERE local_row_id IN 
		   (SELECT atom_id FROM source_classes_atoms
		    WHERE source=''SRC'')';
		    
      msp_location := '112';
      EXECUTE IMMEDIATE
		'INSERT INTO source_id_map
	      (local_row_id,origin,table_name,source_row_id,source)
	     SELECT * FROM new_source_id_map';

   		msp_location := '410';
   		MEME_UTILITY.drop_it('table','new_source_id_map');
   END IF;


   MEME_UTILITY.put_message(msp_method||' successfully completed.');
	
	-- We are saving source_rel_id, relationship_id map 
	-- for map_sg_fields's SRC_REL_ID sg_type
    -- the data will be removed in map_to_meme_id's attribute section
   IF UPPER(table_name) = 'R' OR UPPER(table_name) = 'ALL' THEN
     msp_location := '112';
      EXECUTE IMMEDIATE
		'INSERT INTO source_id_map
	      (local_row_id,origin,table_name,source_row_id,source)
	     SELECT  relationship_id as local_row_id, ''Local'' as origin, 
	     	''R'' as table_name, source_rel_id AS source_row_id,
	     	 source
		 FROM source_relationships
		 	WHERE source_rel_id IN
		 		(SELECT TO_NUMBER(sg_id) FROM source_attributes 
		 			WHERE sg_type = ''SRC_REL_ID'')';
	END IF;

   msp_location := '500';
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Assign Identifiers',
	detail => 'Insert SRC identifiers (source_atom_id)',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::insert_source_ids',
          'Done process insert_source_ids',work_id,0,100);


EXCEPTION
   WHEN insert_source_ids_exc THEN
      MEME_UTILITY.drop_it('table','new_source_id_map');
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;
	
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table','new_source_id_map');
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END insert_source_ids;

/* PROCEDURE SAFE_REPLACEMENT *********************************************
 *
 * (each of the 1st value is DEFAULT parameter)
 * string_parameter    => NONE, EXACT, NORM, BOTH (= norm)
 * code_parameter      => NONE, EXACT, NOT
 * tty_parameter       => NONE, EXACT, NOT
 * source_aui_parameter => NONE, EXACT, NOT
 * source_cui_parameter => NONE, EXACT, NOT
 * source_dui_parameter => NONE, EXACT, NOT
 * old_source_table    => NULL, <table_name>
 * new_source_table    => NULL, <table_name>
 * old_termgroup_table => NULL, <table_name>
 * new_termgroup_table => NULL, <table_name>
 * change_status       => N, Y
 * source	       => <source_name>
 * authority	       => authority
 */

PROCEDURE safe_replacement(
   string_parameter    IN VARCHAR2 := 'NONE',
   code_parameter      IN VARCHAR2 := 'NONE',
   tty_parameter       IN VARCHAR2 := 'NONE',
   source_aui_parameter IN VARCHAR2 := 'NONE',
   source_cui_parameter IN VARCHAR2 := 'NONE',
   source_dui_parameter IN VARCHAR2 := 'NONE',
   old_source_table    IN VARCHAR2,
   new_source_table    IN VARCHAR2,
   old_termgroup_table IN VARCHAR2,
   new_termgroup_table IN VARCHAR2,
   change_status       IN VARCHAR2 := MEME_CONSTANTS.YES,
   source	       IN VARCHAR2,
   authority	       IN VARCHAR2,
   work_id	       IN INTEGER
)
IS
   string_clause	 VARCHAR2(3000);
   code_clause		 VARCHAR2(3000);
   tty_clause		 VARCHAR2(3000);
   saui_clause		 VARCHAR2(3000);
   scui_clause		 VARCHAR2(3000);
   sdui_clause		 VARCHAR2(3000);
   old_source_clause	 VARCHAR2(3000);
   new_source_clause	 VARCHAR2(3000);
   old_termgroup_clause  VARCHAR2(3000);
   new_termgroup_clause  VARCHAR2(3000);
   drop_clause		 VARCHAR2(3000);
   temp_safe_replacement VARCHAR2(3000);
   t_repl		 VARCHAR2(3000);
   t_cs 		 VARCHAR2(3000);
   t1_opt		 VARCHAR2(3000);
   t2_opt		 VARCHAR2(3000);
   s1_opt		 VARCHAR2(3000);
   s2_opt		 VARCHAR2(3000);
   code_opt		 VARCHAR2(3000);
   tty_opt		 VARCHAR2(3000);
   saui_opt		 VARCHAR2(3000);
   scui_opt		 VARCHAR2(3000);
   sdui_opt		 VARCHAR2(3000);
   insert_opt		 VARCHAR2(3000);
   string_opt		 VARCHAR2(3000);
   status_opt		 VARCHAR2(3000);
   retval		 INTEGER;
   r_seq		 INTEGER;
   safe_replacement_exc  EXCEPTION;

   cui_prefix		VARCHAR2(10);
   cui_length		INTEGER;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Starting safe_replacement',work_id,0,1);

   MEME_UTILITY.sub_timing_start;

   initialize_trace('SAFE_REPLACEMENT');

   retval := 0;

   IF UPPER(string_parameter) = 'EXACT' THEN
      string_clause := ' AND a.isui = b.isui';
      string_opt := 'EXACT';
   ELSIF UPPER(string_parameter) = 'NORM' OR
         UPPER(string_parameter) = 'BOTH' THEN
      string_clause := ' AND a.lui = b.lui';
      string_opt := 'BOTH';
   ELSIF UPPER(string_parameter) = 'NONE' THEN
      string_clause := '';
      string_opt := 'NONE';
   ELSE
      msp_error_code := 30;
      msp_error_detail := 'string_parameter='||string_parameter;
      RAISE safe_replacement_exc;
   END IF;

   msp_location := '110';
   IF UPPER(code_parameter) = 'EXACT' THEN
      code_clause := ' AND a.code = b.code';
      code_opt := 'EXACT';
   ELSIF UPPER(code_parameter) = 'NONE' THEN
      code_clause := '';
      code_opt := 'NONE';
   ELSE
      msp_error_code := 31;
      msp_error_detail := 'code_parameter='||code_parameter;
      RAISE safe_replacement_exc;
   END IF;

   msp_location := '115';
   tty_opt := 'N';
   tty_clause := '';
   IF UPPER(tty_parameter) = 'EXACT' OR
      UPPER(tty_parameter) = 'Y' THEN
      tty_clause :=
	 ' AND SUBSTR(a.termgroup,INSTR(a.termgroup,''/'')+1) = ' ||
	 ' SUBSTR(b.termgroup,INSTR(b.termgroup,''/'')+1) ';
      tty_opt := 'Y';
   ELSIF UPPER(tty_parameter) = 'NOT' THEN
      tty_clause :=
	 ' AND SUBSTR(a.termgroup,INSTR(a.termgroup,''/'')+1) != ' ||
	 ' SUBSTR(b.termgroup,INSTR(b.termgroup,''/'')+1) ';
      tty_opt := 'Y';
   ELSIF UPPER(tty_parameter) = 'N' OR
         UPPER(tty_parameter) = 'NONE' THEN
      tty_clause := '';
      tty_opt := 'N';
   END IF;

   msp_location := '117.1';
   IF UPPER(source_aui_parameter) = 'EXACT' THEN
      saui_clause := ' AND a.source_aui = b.source_aui ';
      saui_opt := 'EXACT';
   ELSIF UPPER(source_aui_parameter) = 'NOT' THEN
      saui_clause := ' AND a.source_aui != b.source_aui ';
      saui_opt := 'NOT';
   ELSIF UPPER(source_aui_parameter) = 'NONE' THEN
      saui_clause := '';
      saui_opt := 'NONE';
   ELSE
      msp_error_code := 31;
      msp_error_detail := 'source_aui_parameter='||source_aui_parameter;
      RAISE safe_replacement_exc;
   END IF;

   msp_location := '117.2';
   IF UPPER(source_cui_parameter) = 'EXACT' THEN
      scui_clause := ' AND a.source_cui = b.source_cui ';
      scui_opt := 'EXACT';
   ELSIF UPPER(source_cui_parameter) = 'NOT' THEN
      scui_clause := ' AND a.source_cui != b.source_cui ';
      scui_opt := 'NOT';
   ELSIF UPPER(source_cui_parameter) = 'NONE' THEN
      scui_clause := '';
      scui_opt := 'NONE';
   ELSE
      msp_error_code := 31;
      msp_error_detail := 'source_cui_parameter='||source_cui_parameter;
      RAISE safe_replacement_exc;
   END IF;

   msp_location := '117.3';
   IF UPPER(source_dui_parameter) = 'EXACT' THEN
      sdui_clause := ' AND a.source_dui = b.source_dui ';
      sdui_opt := 'EXACT';
   ELSIF UPPER(source_dui_parameter) = 'NOT' THEN
      sdui_clause := ' AND a.source_dui != b.source_dui ';
      sdui_opt := 'NOT';
   ELSIF UPPER(source_dui_parameter) = 'NONE' THEN
      sdui_clause := '';
     sdui_opt := 'NONE';
   ELSE
      msp_error_code := 31;
      msp_error_detail := 'source_dui_parameter='||source_dui_parameter;
      RAISE safe_replacement_exc;
   END IF;

   msp_location := '130';
   IF UPPER(change_status) NOT IN ('Y','N') THEN
      msp_error_code := 32;
      msp_error_detail := 'change_status='||change_status;
      RAISE safe_replacement_exc;
   ELSIF UPPER(change_status) = 'Y' THEN
      status_opt := 'SET STATUS';
   END IF;

   msp_location := '140';
   IF old_termgroup_table IS NULL THEN
      old_termgroup_clause := '';
   ELSIF MEME_UTILITY.object_exists('table',old_termgroup_table) = 1 THEN
      --MEME_SYSTEM.analyze(old_termgroup_table);
      old_termgroup_clause := ' AND b.termgroup IN (SELECT * FROM '||
	 old_termgroup_table||')';
      t1_opt := MEME_UTILITY.table_to_string(old_termgroup_table);
   ELSE
      msp_error_code := 42;
      msp_error_detail := 'old_termgroup_table='||old_termgroup_table;
      RAISE safe_replacement_exc;
   END IF;

   msp_location := '150';
   IF new_termgroup_table IS NULL THEN
      new_termgroup_clause := '';
   ELSIF MEME_UTILITY.object_exists('table',new_termgroup_table) = 1 THEN
      --MEME_SYSTEM.analyze(new_termgroup_table);
      new_termgroup_clause := ' AND a.termgroup IN (SELECT * FROM '||
	 new_termgroup_table||')';
      t2_opt := MEME_UTILITY.table_to_string(new_termgroup_table);
   ELSE
      msp_error_code := 42;
      msp_error_detail := 'new_termgroup_table='||new_termgroup_table;
      RAISE safe_replacement_exc;
   END IF;

   msp_location := '160';
   IF old_source_table IS NULL THEN
      old_source_clause := '';
   ELSIF MEME_UTILITY.object_exists('table',old_source_table) = 1 THEN
      --MEME_SYSTEM.analyze(old_source_table);
      old_source_clause := ' AND b.source IN (SELECT * FROM '||
	 old_source_table||')';
      s1_opt := MEME_UTILITY.table_to_string(old_source_table);
   ELSE
      msp_error_code := 42;
      msp_error_detail := 'old_source_table='||old_source_table;
      RAISE safe_replacement_exc;
   END IF;

   msp_location := '170';
   IF new_source_table IS NULL THEN
      new_source_clause := '';
   ELSIF MEME_UTILITY.object_exists('table',new_source_table) = 1 THEN
      --MEME_SYSTEM.analyze(new_source_table);
      new_source_clause := ' AND a.source IN (SELECT * FROM '||
	 new_source_table||')';
      s2_opt := MEME_UTILITY.table_to_string(new_source_table);
   ELSE
      msp_error_code := 42;
      msp_error_detail := 'new_source_table='||new_source_table;
      RAISE safe_replacement_exc;
   END IF;

   msp_location := '180';
   temp_safe_replacement := MEME_UTILITY.get_unique_tablename;

   -- a is the new table
   -- b is the old table
   -- The rank field is '000' flags set later ||
   --			CUI padded to 10 chars ||
   --			atom_id padded to 10 chars || other atom id padded to 10 chars
   msp_location := '190.1';
   SELECT value INTO cui_prefix FROM code_map
   WHERE type = 'ui_prefix' AND code = 'CUI';
   msp_location := '190.2';
   SELECT to_number(value) INTO cui_length FROM code_map
   WHERE type = 'ui_length' AND code = 'CUI';
   msp_location := '190.3';
   EXECUTE IMMEDIATE
      'CREATE TABLE ' || temp_safe_replacement || '
           (atom_id_1, atom_id_2, status_1, status_2, sui_1, sui_2,
            isui_1, isui_2, lui_1, lui_2, termgroup_1, termgroup_2,
            status_rank, rank, source, code_1, code_2, last_release_cui) NOLOGGING AS
            -- Haining, Siebel C17ND, remove USE_HASH for 10g
        SELECT /*+ PARALLEL(a)*/ DISTINCT 
	    a.atom_id, b.atom_id, a.status, b.status, a.sui, b.sui,
            a.isui, b.isui, a.lui, b.lui, a.tty, b.tty, a.rank,
	    LPAD(0,3,0)  ||  
       	     (to_number(''' || RPAD(9,cui_length,9) || ''') 
		- TO_NUMBER(TRANSLATE(NVL(b.last_release_cui,1),
			''' || cui_prefix || ''',''00000''))) ||
             LPAD(b.atom_id,10,0)||LPAD(a.atom_id,10,0), 
            '''||safe_replacement.source||''',
            a.code, b.code, b.last_release_cui
        FROM classes a, classes b
        WHERE a.concept_id = b.concept_id ' ||
       old_termgroup_clause||
       old_source_clause||
       new_termgroup_clause||
       new_source_clause||
       tty_clause||
       scui_clause||
       scui_clause||
       sdui_clause||
       code_clause || string_clause;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done ranking of fields',work_id,0,10);

   -- To help disambiguate facts
   -- we augment the ranks if certain things match
   -- but were not part of the predicate for matching
   msp_location := '195';

   -- First is codes
   IF UPPER(code_parameter) = 'NONE' THEN
      msp_location := '195.1';
      local_exec
    	('UPDATE /*+ PARALLEL(a) */ ' || temp_safe_replacement || ' a
       	  SET rank = ''1'' || SUBSTR(rank,2)
       	  WHERE code_1 = code_2' );
      COMMIT;
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done augmenting the rank for codes',work_id,0,20);

   -- Second is termgroups
   IF UPPER(tty_parameter) = MEME_CONSTANTS.NO OR
      UPPER(tty_parameter) = 'NONE' THEN
      msp_location := '195.2';
      local_exec
      	('UPDATE /*+ PARALLEL(a) */ ' || temp_safe_replacement || ' a
	  SET rank = SUBSTR(rank,1,1) || ''1'' || SUBSTR(rank,3)
	 WHERE termgroup_1 = termgroup_2');
      COMMIT;
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done augmenting the rank for termgroups',work_id,0,30);

   -- Third is string match (lui)
   IF UPPER(string_parameter) = 'NONE' THEN
      msp_location := '195.3';
      local_exec
      	('UPDATE /*+ PARALLEL(a)*/ ' || temp_safe_replacement || ' a
	  SET rank = SUBSTR(rank,1,2) || ''1'' || SUBSTR(rank,4)
	  WHERE lui_1 = lui_2');
      COMMIT;
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done augmenting the rank for string match (lui)',work_id,0,40);

   -- Then string_match (isui)
   IF UPPER(string_parameter) IN ('NONE','BOTH','NORM') THEN
      msp_location := '195.4';
      local_exec
      	('UPDATE /*+ PARALLEL(a)*/ ' || temp_safe_replacement || ' a
	  SET rank = SUBSTR(rank,1,2) || ''2'' || SUBSTR(rank,4)
	  WHERE isui_1 = isui_2');
      COMMIT;
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done augmenting the rank for string match (isui)',work_id,0,50);

   -- Then string_match(sui)
   IF UPPER(string_parameter) IN ('NONE','BOTH','NORM','EXACT') THEN
      msp_location := '195.5';
      local_exec
      	('UPDATE /*+ PARALLEL(a) */ ' || temp_safe_replacement || ' a
	  SET rank = SUBSTR(rank,1,2) || ''3'' || SUBSTR(rank,4)
	  WHERE sui_1 = sui_2');
      COMMIT;
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done augmenting the rank for string match (sui)',work_id,0,60);

   msp_location := '200';
   MEME_UTILITY.drop_it('table','t_mom_safe_replacement');

   msp_location := '210';
   local_exec
      ('CREATE TABLE t_mom_safe_replacement NOLOGGING AS
        SELECT DISTINCT atom_id_1, atom_id_2, status_1, status_2, rank
        FROM ' || temp_safe_replacement);

   msp_location := '220';
   local_exec
      ('UPDATE /*+PARALLEL(a)*/ t_mom_safe_replacement a
        SET rank = 1 WHERE status_2 = ''N''');
   COMMIT;

   msp_location := '230';
   local_exec
      ('UPDATE /*+PARALLEL(a)*/ t_mom_safe_replacement a
        SET rank = 2 WHERE status_2 = ''U''');
   COMMIT;

   msp_location := '240';
   local_exec
      ('UPDATE /*+PARALLEL(a)*/ t_mom_safe_replacement a
        SET rank = 3 WHERE status_2 = ''R''');
   COMMIT;

   msp_location := '250';
   MEME_UTILITY.drop_it('table','new_msr');
   local_exec(
	'CREATE TABLE new_msr NOLOGGING AS
         SELECT atom_id_1 AS new_atom_id, atom_id_2 AS old_atom_id,
         last_release_cui, rank, ''' || source || ''' AS source
         FROM ' || temp_safe_replacement );

   msp_location := '260';
   local_exec
      ('INSERT INTO mom_safe_replacement
          (old_atom_id, new_atom_id, 
	   last_release_cui, rank, source)
        SELECT old_atom_id,new_atom_id,
	       last_release_cui,rank,source 
	FROM new_msr');

   -- Inherit last_release_rank values
   -- across safe-replacement facts
   msp_location := '263.1';
   MEME_UTILITY.drop_it('table','t_sr_lrr');

   -- Inherit highest ranking fact
   msp_location := '263.2';
   local_exec (
	'CREATE TABLE t_sr_lrr NOLOGGING AS 
   	 SELECT new_atom_id as row_id,
		to_char(last_release_rank) as new_value
         FROM classes a, new_msr b, 
	      (SELECT max(rank) as rank FROM new_msr
	       GROUP BY new_atom_id) c
   	 WHERE a.atom_id = old_atom_id
           AND b.rank=c.rank '
    );

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done inheriting highest ranking fact across safe_replacement facts',work_id,0,70);

   msp_location := '263.4'; 
   local_exec (
	'DELETE FROM t_sr_lrr WHERE (row_id,new_value) in
	 (SELECT atom_id, to_char(last_release_rank) from classes)');

   COMMIT;

   msp_location := '263.3';
   retval := MEME_BATCH_ACTIONS.macro_action (
	action => 'CF',
	id_type => 'C',
	authority => authority,
	table_name => 't_sr_lrr',
	work_id => work_id,
	status => 'R',
	action_field => 'last_release_rank',
	set_preferred_flag => MEME_CONSTANTS.NO );

   IF retval < 0 THEN
	msp_error_code := 10;
	msp_error_detail := 'action=CF,id_type=C,action_field=last_release_rank';
	RAISE safe_replacement_exc;
   END IF;


   msp_location := '263.5';
   MEME_UTILITY.drop_it('table','t_sr_lrr');

   -- Inherit last_release_cui values
   -- across safe-replacement facts
   msp_location := '263.12';
   MEME_UTILITY.drop_it('table','t_sr_lrc');

   -- Inherit highest ranking fact
   msp_location := '263.22';
   local_exec (
	'CREATE TABLE t_sr_lrc NOLOGGING AS 
   	 SELECT new_atom_id as row_id,
		a.last_release_cui as new_value
         FROM classes a, new_msr b, 
	      (SELECT max(rank) as rank FROM new_msr
	       GROUP BY new_atom_id) c
   	 WHERE a.atom_id = old_atom_id
           AND b.rank=c.rank '
    );

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done inheriting last release cui values across safe_replacement facts',work_id,0,80);

   msp_location := '263.42'; 
   local_exec (
	'DELETE FROM t_sr_lrc WHERE new_value IS NULL');

   COMMIT;

   msp_location := '263.32';
   retval := MEME_BATCH_ACTIONS.macro_action (
	action => 'CF',
	id_type => 'C',
	authority => authority,
	table_name => 't_sr_lrc',
	work_id => work_id,
	status => 'R',
	action_field => 'last_release_cui',
	set_preferred_flag => MEME_CONSTANTS.NO );

   IF retval < 0 THEN
	msp_error_code := 10;
	msp_error_detail := 'action=CF,id_type=C,action_field=last_release_cui';
	RAISE safe_replacement_exc;
   END IF;

   -- Inherit suppressible values ('E', and new one says 'N')
   -- across safe-replacement facts where ISUIs match
   msp_location := '263.451';
   MEME_UTILITY.drop_it('table','t_sr_supp');

   -- Inherit highest ranking fact
   msp_location := '263.452';
   local_exec (
	'CREATE TABLE t_sr_supp NOLOGGING AS 
   	 SELECT DISTINCT new_atom_id as row_id, a.suppressible as new_value
         FROM classes a, new_msr b, classes b
   	 WHERE a.atom_id = old_atom_id
	   AND b.atom_id = new_atom_id
	   AND a.isui = b.isui
	   AND a.suppressible in (''E'')
	   AND b.suppressible in (''N'') '
    );

   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done inheriting suppressible values across safe_replacement facts where ISUIs match',work_id,0,90);

   msp_location := '263.32';
   retval := MEME_BATCH_ACTIONS.macro_action (
	action => 'CF',
	id_type => 'C',
	authority => authority,
	table_name => 't_sr_supp',
	work_id => work_id,
	status => 'R',
	action_field => 'suppressible',
	set_preferred_flag => MEME_CONSTANTS.NO );

   IF retval < 0 THEN
	msp_error_code := 10;
	msp_error_detail := 'action=CF,id_type=C,action_field=suppressible';
	RAISE safe_replacement_exc;
   END IF;

   msp_location := '263.52';
   MEME_UTILITY.drop_it('table','t_sr_lrc');
   MEME_UTILITY.drop_it('table','t_sr_supp');

   msp_location := '263.6';
   MEME_UTILITY.drop_it('table','new_msr');

   msp_location := '265';
   SELECT NVL(MAX(row_sequence),0) INTO r_seq FROM sr_predicate
      WHERE source = safe_replacement.source;

   msp_location := '270';
   INSERT INTO sr_predicate
      (source, current_name, previous_name, string_match,
      code_match, tty_match, source_cui_match,
      replaced_termgroups, replacement_termgroups,
      replaced_sources, replacement_sources, row_sequence)
   VALUES (source, source, source, string_opt, code_opt, 
	tty_opt, scui_opt, t1_opt,
        t2_opt, s1_opt, s2_opt, r_seq+1);

   msp_location := '275';
   MEME_UTILITY.drop_it('table','new_srp');
   local_exec('CREATE TABLE new_srp NOLOGGING AS '||
      'SELECT * FROM sr_predicate '||
      'WHERE source = '''||safe_replacement.source||'''');

   MEME_UTILITY.drop_it('table','new_srp');

   COMMIT;

   IF UPPER(change_status) = 'Y' THEN
      t_repl := MEME_UTILITY.get_unique_tablename;

      msp_location := '280';
      local_exec
	 ('CREATE TABLE ' || t_repl || ' NOLOGGING AS 
  	   SELECT * FROM t_mom_safe_replacement');

      msp_location := '285';
      t_cs := MEME_UTILITY.get_unique_tablename;

      msp_location := '290';
      local_exec
	 ('CREATE TABLE ' || t_cs || ' NOLOGGING AS 
	   SELECT DISTINCT atom_id_1 AS row_id
	   FROM ' || t_repl || '
	   WHERE rank = 1 AND status_1 != ''N''');

      MEME_UTILITY.put_message('count: ' ||
      MEME_UTILITY.exec_count(t_cs));

      msp_location := '295';
      IF MEME_UTILITY.exec_count(t_cs) > 0 THEN
	  msp_location := '295.2';
	  retval := MEME_BATCH_ACTIONS.macro_action (
		action => 'S',
		id_type => 'C',
		authority => authority,
		table_name => t_cs,
		work_id => work_id,
		status => 'R',
		new_value => 'N',
	  	set_preferred_flag => MEME_CONSTANTS.NO );
	  IF retval < 0 THEN
	     msp_error_code := 10;
	     msp_error_detail := 'action=s,id_type=c,new_value=N';
	     RAISE safe_replacement_exc;
	  END IF;

      END IF;

      IF retval = -1 THEN
	 msp_error_code := 10;
	 MEME_UTILITY.drop_it('table',t_cs);
	 MEME_UTILITY.drop_it('table',t_repl);
	 MEME_UTILITY.drop_it('table','t_mom_safe_replacement');
	 RAISE safe_replacement_exc;
      END IF;

      MEME_UTILITY.drop_it('table',t_cs);

      msp_location := '300';
      local_exec
	 ('CREATE TABLE ' || t_cs || ' NOLOGGING AS 
  	   SELECT DISTINCT atom_id_1 as row_id
	   FROM ' || t_repl || '
	   WHERE rank = 2 AND status_1 != ''U''');

      -- CMeSH status R atoms (roach motel)
      -- should stay status R
      local_exec(
	'DELETE FROM ' || t_cs || '
	 WHERE row_id in 
	  (SELECT atom_id FROM classes
	   WHERE code like ''C%''
	     AND source like ''MSH%''
	     AND status = ''R'') '
      );

      msp_location := '305';
      IF MEME_UTILITY.exec_count(t_cs) > 0 THEN
	  msp_location := '305.2';
	  retval := MEME_BATCH_ACTIONS.macro_action (
		action => 'S',
		id_type => 'C',
		authority => authority,
		table_name => t_cs,
		work_id => work_id,
		status => 'R',
		new_value => 'U',
		set_preferred_flag => MEME_CONSTANTS.NO );
	  IF retval < 0 THEN
	     msp_error_code := 10;
	     msp_error_detail := 'action=S,id_type=C,new_value=U';
	     RAISE safe_replacement_exc;
	  END IF;

      END IF;

      IF retval = -1 THEN
	 msp_error_code := 10;
	 RAISE safe_replacement_exc;
      END IF;

      MEME_UTILITY.drop_it('table',t_cs);

      msp_location := '310';
      local_exec(
	  'CREATE TABLE ' || t_cs || ' NOLOGGING AS
	   SELECT DISTINCT atom_id_1 as row_id
	   FROM ' || t_repl || '
	   WHERE rank = 3 AND status_1 != ''R''');

      msp_location := '315';
      IF MEME_UTILITY.exec_count(t_cs) > 0 THEN
	  msp_location := '315.2';
	  retval := MEME_BATCH_ACTIONS.macro_action (
		action => 'S',
		id_type => 'C',
		authority => authority,
		table_name => t_cs,
		work_id => work_id,
		status => 'R',
		new_value => 'R',
		set_preferred_flag => MEME_CONSTANTS.NO );
	  IF retval < 0 THEN
	     msp_error_code := 10;
	     msp_error_detail := 'action=S,id_type=C,new_value=R';
	     RAISE safe_replacement_exc;
	  END IF;

      END IF;

      msp_location := '320';
      MEME_UTILITY.drop_it('table',t_cs);
      MEME_UTILITY.drop_it('table',t_repl);
      MEME_UTILITY.drop_it('table','mom_replace_'||source);
   END IF;

   MEME_UTILITY.drop_it('table',temp_safe_replacement);

   msp_location := '400';
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Compute safe replacement',
	detail => 'Done computing safe replacement: ' ||
	  code_opt || '|' || string_opt || '|' || 
	  s1_opt || '|' || s2_opt || '|' || 
          t1_opt || '|' || t2_opt || '|' ||
	  insert_opt || '|' || status_opt,
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::safe_replacement',
          'Done process safe_replacement',work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN safe_replacement_exc THEN
      MEME_UTILITY.drop_it('table',t_cs);
      MEME_UTILITY.drop_it('table',t_repl);
      MEME_UTILITY.drop_it('table','mom_replace_'||source);
      MEME_UTILITY.drop_it('table',temp_safe_replacement);
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',t_cs);
      MEME_UTILITY.drop_it('table',t_repl);
      MEME_UTILITY.drop_it('table','mom_replace_'||source);
      MEME_UTILITY.drop_it('table',temp_safe_replacement);
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END safe_replacement;

/* PROCEDURE RESET_SAFE_REPLACEMENT *****************************************
 * source	       => <source_name>
 */

PROCEDURE reset_safe_replacement(
   source	       IN VARCHAR2,
   authority           IN VARCHAR2
)
IS
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::reset_safe_replacement',
          'Starting reset_safe_replacement',1,0,1);

   initialize_trace('RESET_SAFE_REPLACEMENT');

   msp_location := '10';
   MEME_UTILITY.drop_it('table','t_reset_sr');
   local_exec('CREATE TABLE t_reset_sr NOLOGGING AS '||
      'SELECT DISTINCT source FROM mom_safe_replacement '||
      'WHERE source = '''||source||'''');

   msp_location := '11';
   local_exec
   ('DELETE FROM mom_safe_replacement ' ||
    'WHERE source = '''||source||'''');

   msp_location := '20';
   MEME_UTILITY.drop_it('table','t_reset_sr');
   local_exec('CREATE TABLE t_reset_sr NOLOGGING AS '||
      'SELECT distinct source FROM sr_predicate '||
      'WHERE source = '''||source||'''');

   msp_location := '21';
   local_exec
   ('DELETE FROM sr_predicate ' ||
    'WHERE source = '''||source||'''');

   MEME_UTILITY.drop_it('table','t_reset_sr');

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::reset_safe_replacement',
          'Done process reset_safe_replacement',1,0,100);

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table','t_reset_sr');
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END reset_safe_replacement;

/* PROCEDURE SOURCE_REPLACEMENT *********************************************
 * 
 * This implements the "partial update insertion" model.
 *
 * For each core data type it compares what is to be inserted with
 * what is in the MID and "replaces" the old version content by
 * updating its source to the current version
 *  
 * This procedure must be called AFTER assign_{aui,atui,rui} and
 * map_to_meme_ids
 */
PROCEDURE source_replacement(
   table_name          IN VARCHAR2,
   authority	       IN VARCHAR2,
   work_id	       IN INTEGER
)
IS
   tmp			VARCHAR2(256);
   retval		NUMBER;
   source_repl_exc	EXCEPTION;

   before_ct        NUMBER;
   repl_ct        NUMBER;
   after_ct		NUMBER;
   pct_replaced		NUMBER(10,2);
   cui_prefix		VARCHAR2(10);
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::source_safe_replacement',
          'Starting source_safe_replacement',work_id,0,1);

   MEME_UTILITY.sub_timing_start;
   initialize_trace('SOURCE_REPLACEMENT');
   msp_location := '0';
   tmp := MEME_UTILITY.get_unique_tablename;
   msp_location := '5';

   --
   -- Truncate source_replacement   
   --
   MEME_SYSTEM.truncate('source_replacement');

   --
   -- Atom source replacement
   --   1. Matching AUI
   --   2. Matching suppressible
   --   3. prev/current versions of source
   --
   IF UPPER(table_name) = 'C' OR UPPER(table_name) = 'ALL' THEN

       msp_location := '10.01';
       MEME_UTILITY.drop_it('table','source_replacement_c'); 

       --
       -- Generate "before" count
       --
       SELECT count(*) INTO before_ct
       FROM source_classes_atoms WHERE switch='R';

       --
       -- Compare atoms and move replacements to source_replacement_c
       -- AUI, SUPPRESS, and LAST_RELEASE_CUI (if not null) must match
       --
       msp_location := '10.1';
       EXECUTE IMMEDIATE
           'CREATE TABLE source_replacement_c 
                    (atom_id, sg_meme_data_type, sg_meme_id, new_sg_meme_id) NOLOGGING
            AS SELECT /*+ USE_HASH(a) */
                  a.atom_id, ''C'', a.atom_id, b.atom_id
            FROM classes a, source_classes_atoms b, source_version c
            WHERE a.aui=b.aui AND a.source=previous_name
              AND b.source=current_name
	          AND a.suppressible = b.suppressible
	          AND NVL(NVL(b.last_release_cui,a.last_release_cui),''null'') = 
		          NVL(a.last_release_cui,''null'')
              AND a.tobereleased in (''Y'',''y'')';

       MEME_UTILITY.put_message('Done loading source_replacement_c - ' || SQL%ROWCOUNT);
       COMMIT;

       --
       -- Index for efficiency (cannot be unique because old version source may have dups)
       --
       MEME_UTILITY.drop_it('index','x_sr_new_id'); 
       msp_location := '10.2';
       EXECUTE IMMEDIATE
           'CREATE INDEX x_sr_new_id ON source_replacement_c (new_sg_meme_id) TABLESPACE MIDI 
     	    COMPUTE STATISTICS PARALLEL NOLOGGING';

       msp_location := '10.3';
		-- Soma Changing for 10g 
       --MEME_SYSTEM.analyze('source_replacement_c');
       MEME_UTILITY.put_message('Done indexing source_replacement_c');

       
       --
       -- Fix the references in source_id_map (for contexts)
       --

       -- This query may report a duplicate row in single-row subquery error.
       -- If so, it is a problem with the data in classes.  
       -- DON'T change this query.
       msp_location := '10.4';
       EXECUTE IMMEDIATE
           'UPDATE source_id_map a
             SET local_row_id = 
                (SELECT sg_meme_id FROM source_replacement_c b 
                 WHERE local_row_id = new_sg_meme_id)
             WHERE local_row_id IN (select new_sg_meme_id FROM source_replacement_c)
             AND table_name=''C''
          AND source = :x'
       USING authority;

       MEME_UTILITY.put_message('Done updating source_id_map - ' || SQL%ROWCOUNT);

       --
       -- No need to touch atom_ordering as it is handled later
       --
      
       --
       -- delete replacements from source_classes_atoms
       --
       msp_location := '10.5';
       EXECUTE IMMEDIATE
           'DELETE FROM source_classes_atoms a
            WHERE atom_id IN
             (SELECT new_sg_meme_id FROM source_replacement_c
              WHERE sg_meme_data_type = ''C'' )';
       MEME_UTILITY.put_message('Done deleting from source_classes_atoms - ' || SQL%ROWCOUNT);

       --
       -- Compute new source, termgroup values for classes
       --
       msp_location := '15';
       MEME_UTILITY.drop_it('table',tmp);
       msp_location := '20';
       EXECUTE IMMEDIATE
           'CREATE TABLE ' || tmp || ' NOLOGGING AS
           -- Haining, Siebel C17ND, remove ORDERED USE_HASH for 10g
            SELECT 
		distinct a.atom_id, current_name as source,
    	       current_name || ''/'' || tty as termgroup
    	    FROM classes a, source_replacement_c b, source_version c
            WHERE a.source = c.previous_name
    	    AND a.atom_id = b.sg_meme_id
   	    AND b.sg_meme_data_type=''C''';
    
       --
       -- Index the table
       --

       msp_location := '25';
       -- Soma Changing for Oracle 10g performance ADDING UNIQUE
       EXECUTE IMMEDIATE
           'ALTER TABLE ' || tmp || ' ADD PRIMARY KEY (atom_id)';
       
        -- Soma Changing for 10g performance
    	--MEME_SYSTEM.analyze(tmp);
       --
       --  Update source,termgroup in classes for replacements
       --  WE are not updating classes.rank, but it will get picked up
       --  when core_table_insert('C') is called
       --
       msp_location := '30';
       IF MEME_UTILITY.exec_count(tmp) > 0 THEN
    	msp_location := '40.1';
    	EXECUTE IMMEDIATE
    	    'UPDATE (SELECT a.source, a.termgroup, 
                            b.source new_source, b.termgroup new_termgroup
                     FROM classes a, ' || tmp || ' b
                     WHERE a.atom_id = b.atom_id)
             SET source = new_source, termgroup = new_termgroup';
           MEME_UTILITY.put_message('Done updating source_classes_atoms - ' || SQL%ROWCOUNT);
    
       END IF;

       --
       -- load safe replacement facts
       -- 
       msp_location :='10.15';
       SELECT value INTO cui_prefix FROM code_map
       WHERE type = 'ui_prefix' AND code='CUI'; 
       msp_location :='10.16';
       EXECUTE IMMEDIATE 
           'INSERT INTO mom_safe_replacement(
               old_atom_id, new_atom_id, last_release_cui, source, rank)
           SELECT /*+ USE_HASH(a) */
	         a.atom_id, a.atom_id, b.last_release_cui, b.source,
               ''113'' || lpad(replace(translate(b.last_release_cui,:x,''XXXXXXX''),''X'',''''),10,0) ||
               lpad(a.atom_id, 10, 0) || lpad(a.atom_id, 10, 0)
           FROM source_replacement_c a, classes b
           WHERE a.atom_id = b.atom_id'
       USING cui_prefix;
       MEME_UTILITY.put_message('Done loading mom_safe_replacement - ' || SQL%ROWCOUNT);

       --
       -- Generate "after" count
       --
       msp_location := '40.1';
       SELECT count(*) INTO after_ct
       FROM source_classes_atoms WHERE switch='R';

       --
       -- Compute percentage
       --
       msp_location := '40.2';
       pct_replaced := ((before_ct - after_ct)*100) / (before_ct+.0000001);

       msp_location := '50';
       MEME_UTILITY.drop_it('table',tmp);
       MEME_UTILITY.sub_timing_stop;
       MEME_UTILITY.put_message(pct_replaced || '% atoms replaced.');
       MEME_UTILITY.log_operation
          (authority,'MEME_SOURCE_PROCESSING.source_replacement',
           pct_replaced || '% atoms replaced.',0,work_id,MEME_UTILITY.sub_elapsed_time);

       MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::source_safe_replacement',
   		'Done atom source_safe_replacement',work_id,0,20);

   END IF;


   --
   -- Attribute source replacement (if releasable)
   --   1. Matching ATUI
   --   2. Matching atom ids
   --   3. Matching suppressible
   --   4. prev/current versions of source
   --
   -- Attribute source replacement (if not releasable)
   --   1. Matching atom ids
   --   2. Matching attribute name
   --   3. Matching hashcode
   --   4. Matching attribute level
   --   5. prev/current versions of source
   --
   IF UPPER(table_name) = 'A' OR UPPER(table_name) = 'ALL' THEN     

       msp_location := '10.01';
       MEME_UTILITY.drop_it('table','source_replacement_a'); 

       --
       -- Generate "before" count
       --
       SELECT count(*) INTO before_ct
       FROM source_attributes WHERE switch='R';
		
       --
       -- compare attributes and move replacements to source_replacement
       --
       msp_location := '260.1';
       EXECUTE IMMEDIATE    
            'CREATE TABLE source_replacement_a
                 (atom_id, sg_meme_data_type, sg_meme_id, new_sg_meme_id) NOLOGGING AS
             SELECT /*+ USE_HASH(a) */
                    a.atom_id, ''A'', a.attribute_id, b.attribute_id
             FROM attributes a, source_attributes b, source_version c
             WHERE a.tobereleased in (''Y'',''y'') 
               AND a.atui=b.atui and a.atom_id = b.atom_id
	       AND a.suppressible = b.suppressible 
               AND a.source=previous_name and b.source=current_name';

       MEME_UTILITY.put_message('Done loading source_replacement_a - ' || SQL%ROWCOUNT);
       COMMIT;

       --
	   -- Handle unreleasable case
	   --
       msp_location := '263';
       EXECUTE IMMEDIATE    
            'INSERT into source_replacement_a
                (atom_id, sg_meme_data_type, sg_meme_id, new_sg_meme_id)
             SELECT 
                    a.atom_id, ''A'', a.attribute_id, b.attribute_id
             FROM attributes a, source_attributes b, source_version c
             WHERE a.tobereleased in (''N'',''n'')
	         AND b.tobereleased in (''N'',''n'')
             AND a.atom_id = b.atom_id
	         AND a.attribute_name = b.attribute_name 
	         AND a.hashcode = b.hashcode
	         AND a.attribute_level = ''S''
	         AND b.attribute_level = ''S''
             AND a.source=previous_name 
             AND b.source=current_name';

       MEME_UTILITY.put_message('Done loading source_replacement_a (unreleasable atts) - ' || SQL%ROWCOUNT);

       COMMIT;
      
       -- 
       -- SEMANTIC_TYPE case
       --
       msp_location := '264';
       EXECUTE IMMEDIATE    
            'INSERT into source_replacement_a
                (atom_id, sg_meme_data_type, sg_meme_id, new_sg_meme_id)
             SELECT /*+ USE_HASH(a) */ DISTINCT
                    a.atom_id, ''A'', a.attribute_id, b.attribute_id
             FROM attributes a, source_attributes b
             WHERE a.tobereleased in (''Y'',''y'') 
               AND a.attribute_value = b.attribute_value 
               AND a.concept_id = b.concept_id
               AND a.attribute_name=''SEMANTIC_TYPE''
               AND b.attribute_name=''SEMANTIC_TYPE''';
       MEME_UTILITY.put_message('Done loading source_replacement_a (STYs) - ' || SQL%ROWCOUNT);

       COMMIT;
		
       --
       -- Index for efficiency (cannot be unique because old version source may have dups)
       --
       MEME_UTILITY.drop_it('index','x_sr_new_id'); 
       msp_location := '261';
       EXECUTE IMMEDIATE
           'CREATE INDEX x_sr_new_id ON source_replacement_a (new_sg_meme_id) TABLESPACE MIDI 
     	    COMPUTE STATISTICS PARALLEL NOLOGGING';

       msp_location := '262';
       --MEME_SYSTEM.analyze('source_replacement_a');

       --
       -- delete replacements from source_attributes
       --
--       msp_location := '265';
       --EXECUTE IMMEDIATE
         --  'DELETE FROM source_attributes a
           -- WHERE attribute_id IN
--             (SELECT new_sg_meme_id
              --FROM source_replacement_a 
              --WHERE sg_meme_data_type=''A'') '; 
       --MEME_UTILITY.put_message('Done deleting from source_attributes - ' || SQL%ROWCOUNT);
       
       msp_location := '265a';
       
       MEME_UTILITY.drop_it('table','tmp_sa');
       
       msp_location := '265b';
       
       EXECUTE IMMEDIATE
       		'CREATE table tmp_sa as
             select * from source_attributes where attribute_id NOT IN 
             (SELECT new_sg_meme_id
              FROM source_replacement_a 
              WHERE sg_meme_data_type=''A'')';
              
    	COMMIT;
       msp_location := '265c';    	
	    MEME_UTILITY.drop_it('table','source_attributes');
       msp_location := '265a';	    
    	EXECUTE IMMEDIATE
    		'RENAME tmp_sa TO source_attributes';
   		
       msp_location := '265d';
    	EXECUTE IMMEDIATE
        	'CREATE INDEX x_sa_1 on source_attributes (attribute_id)
PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI';
        
        EXECUTE IMMEDIATE 'GRANT ALL ON source_attributes TO MID_USER';              
       		
        MEME_UTILITY.put_message('Done deleting from source_attributes');
       		

       --
       -- delete replacements from source_stringtab
       --
       msp_location := '266';
--       EXECUTE IMMEDIATE
--           'DELETE FROM source_stringtab
--            WHERE string_id IN
--             (SELECT string_id from source_stringtab
--              minus
--              SELECT to_number(substr(attribute_value,20)) 
--	      FROM source_attributes
--              WHERE attribute_value like ''<>Long_Attribute<>:%'') ';

       MEME_UTILITY.drop_it('table','tmp_sst');
       
       msp_location := '266a';
       
       EXECUTE IMMEDIATE
       		'CREATE table tmp_sst as
             select * from source_stringtab a where exists 
             (select 1 from source_attributes b where a.string_id= TO_NUMBER (SUBSTR (b.attribute_value, 20)) 
             and b.attribute_value LIKE ''<>Long_Attribute<>:%'')';
              
    	COMMIT;
       msp_location := '266b';    	
	    MEME_UTILITY.drop_it('table','source_stringtab');
       msp_location := '266c';	    
    	EXECUTE IMMEDIATE
    		'RENAME tmp_sst TO source_stringtab';
   		
        EXECUTE IMMEDIATE 'GRANT ALL ON source_stringtab TO MID_USER';   
       
       
       MEME_UTILITY.put_message('Done deleting from source_stringtab');

       --
       -- Compute new source values for replacement attributes
     	-- Handles unreleasable case also
       --
       msp_location := '267';
       MEME_UTILITY.drop_it('table',tmp);
       msp_location := '270';
       
       -- Soma Changing for 10g performance Adding NO LOGGING

       EXECUTE IMMEDIATE
           'CREATE TABLE ' || tmp || ' NOLOGGING AS
            SELECT attribute_id, current_name as source
    	    FROM attributes a, source_version b 
    	    WHERE attribute_level=''S'' 
    	      AND attribute_id IN 
    	        (SELECT sg_meme_id from source_replacement_a
    	         WHERE sg_meme_data_type=''A'')
    	      AND a.source = previous_name';

       --
       -- Index for efficiency (cannot be unique because old version source may have dups)
       --
       msp_location := '280';
       -- Soma Changing for 10g performance Adding UNIQUE

       EXECUTE IMMEDIATE
           'ALTER TABLE ' || tmp || ' ADD PRIMARY KEY (attribute_id)';
    
       EXECUTE IMMEDIATE           
           'ALTER SESSION ENABLE PARALLEL DML';
           
       -- Soma Changing for 10g performance
       --MEME_SYSTEM.analyze(tmp);
       --
       -- Update source of replacement attributes
       --
       msp_location := '290';
       IF MEME_UTILITY.exec_count(tmp) > 0 THEN
    	msp_location := '290.1';
    	EXECUTE IMMEDIATE
    	    'MERGE /*+ first_rows parallel(a) parallel(new) */ INTO attributes a
             USING ' || tmp || ' new ON (a.attribute_id = new.attribute_id)
             WHEN MATCHED THEN UPDATE SET
             source = new.source';
           MEME_UTILITY.put_message('Done updating attributes - ' || SQL%ROWCOUNT);

       END IF;
    
       --
       -- Generate "after" count
       --
       msp_location := '300.1';
       SELECT count(*) INTO after_ct
       FROM source_attributes WHERE switch='R';

       --
       -- Compute percentage
       --
       msp_location := '300.2';
       pct_replaced := ((before_ct - after_ct)*100) / (before_ct+.0000001);

       msp_location := '400';
       MEME_UTILITY.drop_it('table',tmp);
       MEME_UTILITY.sub_timing_stop;
       MEME_UTILITY.put_message(pct_replaced || '% attributes replaced.');
       MEME_UTILITY.log_operation
          (authority,'MEME_SOURCE_PROCESSING.source_replacement',
           pct_replaced || '% attributes replaced.',	
	       0,work_id,MEME_UTILITY.sub_elapsed_time);
    

   	MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::source_safe_replacement',
   		'Done attribute source_safe_replacement',work_id,0,40);

   END IF;


   --
   -- Relationship source replacement (if releasable)
   --   1. Matching RUI
   --   2. Matching atom_id_1, atom_id_2
   --   3. Matching relationship group
   --   4. Matching suppressible
   --   4. prev/current versions of source
   --   
   IF UPPER(table_name) = 'R' OR UPPER(table_name) = 'ALL' THEN

       msp_location := '10.01';
       MEME_UTILITY.drop_it('table','source_replacement_r'); 
            
       --
       -- Generate "before" count
       --
       SELECT count(*) INTO before_ct
       FROM source_relationships WHERE switch='R';

       --
       -- compare relationships and move replacements to source_replacement
       --
       msp_location := '110.1';
       EXECUTE IMMEDIATE    
            'CREATE TABLE source_replacement_r
              (atom_id, sg_meme_data_type, sg_meme_id, new_sg_meme_id) NOLOGGING AS
             SELECT /*+ USE_HASH(a) */ a.atom_id_1, ''R'', a.relationship_id, b.relationship_id
             FROM relationships a, source_relationships b, source_version c
             WHERE a.tobereleased in (''Y'',''y'') 
		       AND a.rui = b.rui AND a.suppressible = b.suppressible
	  	       AND a.atom_id_1=b.atom_id_1 AND a.atom_id_2=b.atom_id_2
	 	       AND nvl(a.relationship_group,0) = nvl(b.relationship_group,0)
               AND a.source=previous_name AND b.source=current_name';
        MEME_UTILITY.put_message('Done loading source_replacement_r - ' || SQL%ROWCOUNT);

        -- Soma chanfing for 10g performance
        COMMIT;

        --
        -- Index for efficiency (cannot be unique because old version source may have dups)
        --
        MEME_UTILITY.drop_it('index','x_sr_new_id'); 
        msp_location := '110.3';
        EXECUTE IMMEDIATE
           'CREATE INDEX x_sr_new_id ON source_replacement_r (new_sg_meme_id) TABLESPACE MIDI 
     	    COMPUTE STATISTICS PARALLEL NOLOGGING';

        msp_location := '110.3b';
        --MEME_SYSTEM.analyze('source_replacement_r');


        -- Assumes there are not left over records from prior insertions (no source= cla
        msp_location := '110.4';
        EXECUTE IMMEDIATE
        'UPDATE source_id_map a
         SET local_row_id =
            (SELECT sg_meme_id FROM source_replacement_r b
            WHERE local_row_id = new_sg_meme_id)
         WHERE local_row_id IN (select new_sg_meme_id FROM source_replacement_r)
           AND table_name=''R'''; 
        MEME_UTILITY.put_message('Done updating source_id_map - ' || SQL%ROWCOUNT);

       --
       -- delete replacements from source_relationships
       --
       msp_location := '110.2';
       EXECUTE IMMEDIATE
           'DELETE FROM source_relationships a
            WHERE relationship_id IN
             (SELECT new_sg_meme_id FROM source_replacement_r
              WHERE sg_meme_data_type=''R'')';
        MEME_UTILITY.put_message('Done deleting from source_rels - ' || SQL%ROWCOUNT);
              
       --
       -- Compute new source values for replacement relationships
       --
       msp_location := '115';
       MEME_UTILITY.drop_it('table',tmp);
       msp_location := '120';
       -- Soma Changing for 10g performance Adding NOLOGGING
       EXECUTE IMMEDIATE
           'CREATE TABLE ' || tmp || ' NOLOGGING AS
    	    SELECT a.relationship_id, b.current_name source, 
    	       c.current_name as source_of_label
    	    FROM relationships a, source_version b, source_version c
            WHERE relationship_level=''S'' 
      	      AND a.source = b.previous_name 
              AND source_of_label = c.previous_name
    	      AND relationship_id IN
    	       (SELECT sg_meme_id FROM source_replacement_r
    	        WHERE sg_meme_data_type=''R'')';
    
       --
       -- Index for efficiency
       --
       msp_location := '127';
       -- Soma Changing for 10g performance Adding UNIQUE
       EXECUTE IMMEDIATE
           'ALTER TABLE ' || tmp || ' ADD PRIMARY KEY (relationship_id)';
    
       -- Soma Changing for 10g performance
		--MEME_SYSTEM.analyze(tmp);
       --
       -- Update source, source_of_label to new values for replacement rels
       --
       msp_location := '130';
       IF MEME_UTILITY.exec_count(tmp) > 0 THEN
      	msp_location := '140.1';
    	EXECUTE IMMEDIATE
    	    'UPDATE (SELECT a.source, a.source_of_label,
                            b.source new_source, b.source_of_label new_sl
                     FROM relationships a, ' || tmp || ' b
                     WHERE a.relationship_id = b.relationship_id)
             SET source = new_source, source_of_label = new_sl';
        MEME_UTILITY.put_message('Done updating relationships - ' || SQL%ROWCOUNT);
       END IF;
    
       --
       -- Generate "after" count
       --
       msp_location := '145.1';
       SELECT count(*) INTO after_ct
       FROM source_relationships WHERE switch='R';

       --
       -- Compute percentage
       --
       msp_location := '145.2';
       pct_replaced := ((before_ct - after_ct)*100) / (before_ct+.0000001);

       msp_location := '150';
       MEME_UTILITY.drop_it('table',tmp);
       MEME_UTILITY.sub_timing_stop;
       MEME_UTILITY.put_message(pct_replaced || '% relationships replaced.');
       MEME_UTILITY.log_operation
          (authority,'MEME_SOURCE_PROCESSING.source_replacement',
           pct_replaced || '% relationships replaced.',
	   0,work_id,MEME_UTILITY.sub_elapsed_time);

   	MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::source_safe_replacement',
   		'Done relationship source_safe_replacement',work_id,0,60);

   END IF;


   --
   -- Context Relationship source replacement (if releasable)
   --   1. Matching RUI
   --   2. Matching atom_id_1, atom_id_2
   --   3. Matching relationship group
   --   4. Matching parent treenum
   --   5. prev/current versions of source
   --   
   IF UPPER(table_name) = 'CR' OR UPPER(table_name) = 'ALL' THEN
    
       msp_location := '10.01';
       MEME_UTILITY.drop_it('table','source_replacement_cr'); 
   
       --
       -- Generate "before" count
       --
       SELECT count(*) INTO before_ct
       FROM source_context_relationships WHERE switch='R';

       --
       -- compare context_relationships and move 
       -- replacements to source_replacement
       --
       msp_location := '200';
       EXECUTE IMMEDIATE 
            'CREATE TABLE source_replacement_cr
                (atom_id, sg_meme_data_type, sg_meme_id, new_sg_meme_id) NOLOGGING AS
             SELECT /*+ USE_HASH(a) */ 
                a.atom_id_1, ''CR'', a.relationship_id, b.relationship_id
             FROM context_relationships a, 
		          source_context_relationships b, source_version c
             WHERE a.tobereleased in (''Y'',''y'') 
               AND a.rui=b.rui
	 	       AND a.atom_id_1 = b.atom_id_1 AND a.atom_id_2 = b.atom_id_2
		       AND nvl(a.parent_treenum,''null'') = 
                      nvl(b.parent_treenum,''null'')
		       AND nvl(a.relationship_group,0) = nvl(b.relationship_group,0)
               AND a.source=previous_name and b.source=current_name
			AND nvl(a.HIERARCHICAL_CODE,''null'') = nvl(b.HIERARCHICAL_CODE,''null'')';
      
       repl_ct := SQL%ROWCOUNT;
        MEME_UTILITY.put_message('Done loading source_replacement_cr - ' || repl_ct);
       -- Soma Changing for 10g performance
       COMMIT;

        --
        -- Index for efficiency
        --
        MEME_UTILITY.drop_it('index','x_sr_new_id'); 
        msp_location := '201';
        EXECUTE IMMEDIATE
           'CREATE INDEX x_sr_new_id ON source_replacement_cr (new_sg_meme_id) TABLESPACE MIDI 
     	    COMPUTE STATISTICS PARALLEL NOLOGGING';

        msp_location := '202';
        --MEME_SYSTEM.analyze('source_replacement_cr');
        
       --
       -- delete replacements from source_context_relationships
       --
       IF (repl_ct *  2) > before_ct AND repl_ct > 1000000 THEN
           msp_location := '210.2';
           tmp := MEME_UTILITY.get_unique_tablename;
           msp_location := '210.2b';
           EXECUTE IMMEDIATE
           'CREATE TABLE ' || tmp || ' NOLOGGING AS
            SELECT * FROM source_context_relationships
            WHERE relationship_id IN
               (SELECT relationship_id FROM source_context_relationships
                MINUS 
                SELECT new_sg_meme_id FROM source_replacement_cr
                WHERE sg_meme_data_type = ''CR'') ';
           MEME_UTILITY.put_message('Done creating new source_context_rels');
           msp_location := '210.2c';
           MEME_SYSTEM.truncate('source_context_relationships');
           EXECUTE IMMEDIATE     
           'INSERT /*+ APPEND */ INTO source_context_relationships 
            SELECT * FROM ' || tmp;
           MEME_UTILITY.put_message('Done loading new source_context_rels - ' || SQL%ROWCOUNT);
          
       ELSE
           EXECUTE IMMEDIATE
           'DELETE FROM source_context_relationships a
            WHERE relationship_id IN
               (SELECT new_sg_meme_id FROM source_replacement_cr
                WHERE sg_meme_data_type = ''CR'')';
            MEME_UTILITY.put_message('Done deleting from source_context_rels - ' || SQL%ROWCOUNT);
       END IF;
       COMMIT;

        --
       -- Compute new source values for replacement cxt rels
       --
       msp_location := '215';
       MEME_UTILITY.drop_it('table',tmp);
       msp_location := '220';
       -- Soma changing for 10g performance Adding NOLOGGING
       EXECUTE IMMEDIATE
           'CREATE TABLE ' || tmp || ' NOLOGGING AS
    	    SELECT a.relationship_id, b.current_name source, 
    	       c.current_name as source_of_label
    	    FROM context_relationships a, 
	       source_version b, source_version c
            WHERE relationship_level=''S'' 
      	    AND a.source = b.previous_name 
            AND source_of_label = c.previous_name
    	    AND a.relationship_id IN
    	      (SELECT sg_meme_id FROM source_replacement_cr
    	       WHERE sg_meme_data_type=''CR'')';

       --
       -- Index for efficiency
       --
       msp_location := '227';
       -- Soma changing for 10g performance Adding UNIQUE
       EXECUTE IMMEDIATE
           'ALTER TABLE ' || tmp || ' ADD PRIMARY KEY (relationship_id)';
           
       -- Soma changing for 10g performance 
	   MEME_SYSTEM.analyze(tmp);
		
       EXECUTE IMMEDIATE
           'ALTER SESSION ENABLE PARALLEL DML';           
           
       --
       -- Update to new source values for replacement cxt rels
       --
       msp_location := '230';
       IF repl_ct > 0 THEN
      	msp_location := '240.1';
    	EXECUTE IMMEDIATE
    	     'MERGE /*+ first_rows parallel(a) parallel(new) */ INTO context_relationships a
                 USING ' || tmp || ' new ON (a.relationship_id = new.relationship_id)
                 WHEN MATCHED THEN UPDATE SET
                 source = new.source, source_of_label = new.source_of_label';
            MEME_UTILITY.put_message('Done updating context_relationships - ' || SQL%ROWCOUNT);

       END IF;
    
       --
       -- Generate "after" count
       --
       msp_location := '245.1';
       SELECT count(*) INTO after_ct
       FROM source_context_relationships WHERE switch='R';

       --
       -- Compute percentage
       --
       msp_location := '245.2';
       pct_replaced := ((before_ct - after_ct)*100) / (before_ct+.0000001);

       msp_location := '250';
       MEME_UTILITY.drop_it('table',tmp);
       MEME_UTILITY.sub_timing_stop;
       MEME_UTILITY.put_message(pct_replaced || '% context_relationships replaced.');
       MEME_UTILITY.log_operation
          (authority,'MEME_SOURCE_PROCESSING.source_replacement',
           pct_replaced || '% context_relationships replaced.',
           0,work_id,MEME_UTILITY.sub_elapsed_time);

       MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::source_safe_replacement',
   		'Done context relationship source_safe_replacement',work_id,0,80);

   END IF;


       
   msp_location := '1030';
   EXECUTE IMMEDIATE
      'UPDATE src_qa_samples a
       SET sample_id = 
           (SELECT sg_meme_id FROM source_replacement_' ||table_name|| ' b 
            WHERE sample_id = new_sg_meme_id
              AND a.id_type = b.sg_meme_data_type)
       WHERE (sample_id,id_type) IN 
         (select new_sg_meme_id, sg_meme_data_type FROM source_replacement_' || table_name || ')';

   MEME_UTILITY.put_message('Done updating src_qa_samples - ' || SQL%ROWCOUNT);

   COMMIT;

   msp_location := '2000';
   MEME_UTILITY.drop_it('index','x_sr_new_id'); 
   MEME_UTILITY.drop_it('table','source_replacement_'||table_name);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::source_safe_replacement',
   	'Done process source_safe_replacement',work_id,0,100);

EXCEPTION
   WHEN source_repl_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END source_replacement;


/* PROCEDURE GENERATE_FACTS ****************************************************
 *
 * User Input Parameters:
 *
 * termgroups_table_1 => not null <table_name> (new termgroups)
 * termgroup_table_2 => not null <table_name>  (old termgroups)
 * merge_set	     => not null
 * string_parameter  => NORM, EXACT, BOTH, NONE, NOT
 * code_parameter    => EXACT, NOT, NONE
 * tty_parameter     => EXACT, NOT, NONE
 * source_aui_parameter => EXACT, NOT, NONE
 * source_cui_parameter => EXACT, NOT, NONE
 * source_dui_parameter => EXACT, NOT, NONE
 * table_name	     => RELATIONSHIPS, CLASSES, SOURCE_CLASSES_ATOMS
 * source	     => not null
 * authority	     =>
 * work_id	     =>
 *
 *
 * NOTE: does not support matching source_classes_atoms to iteself!
 */

PROCEDURE generate_facts(
   termgroup_table_1 IN VARCHAR2,
   termgroup_table_2 IN VARCHAR2,
   merge_set	     IN VARCHAR2,
   string_parameter  IN VARCHAR2,
   code_parameter    IN VARCHAR2,
   tty_parameter     IN VARCHAR2 := 'NONE',
   source_aui_parameter IN VARCHAR2 := 'NONE',
   source_cui_parameter IN VARCHAR2 := 'NONE',
   source_dui_parameter IN VARCHAR2 := 'NONE',
   table_name	     IN VARCHAR2,
   source	     IN VARCHAR2,
   authority	     IN VARCHAR2,
   work_id	     IN INTEGER
)
IS
   string_clause      VARCHAR2(1000);
   code_clause	      VARCHAR2(1000);
   tty_clause	      VARCHAR2(1000);
   saui_clause	      VARCHAR2(1000);
   scui_clause	      VARCHAR2(1000);
   sdui_clause	      VARCHAR2(1000);
   from_clause	      VARCHAR2(1000);
   where_clause       VARCHAR2(1000);
   insert_clause      VARCHAR2(1000);
   generate_facts_exc EXCEPTION;

BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::generate_facts',
   	'Starting generate_facts',work_id,0,1);

   MEME_UTILITY.sub_timing_start;

   --MEME_SYSTEM.analyze(termgroup_table_1);
   --MEME_SYSTEM.analyze(termgroup_table_2);

   initialize_trace('GENERATE_FACTS');

   -- set up query clause
   IF UPPER(table_name) IN ('CLASSES', 'SOURCE_CLASSES_ATOMS') THEN

      -- set up string clause
      msp_location := '110';
      IF UPPER(string_parameter) IN ('NORM','EXACT','BOTH') THEN
	 string_clause := 'a.lui = b.lui AND';
      ELSIF UPPER(string_parameter) = 'NOT' THEN
	 string_clause := 'a.lui != b.lui AND';
      ELSIF UPPER(string_parameter) = 'NONE' THEN
	 string_clause := '';
      ELSE
	 msp_error_code := 30;
	 msp_error_detail := 'string_parameter='||string_parameter;
	 RAISE generate_facts_exc;
      END IF;

      -- set up code clause
      msp_location := '120';
      IF UPPER(code_parameter) = 'EXACT' THEN
	 code_clause := ' AND a.code = b.code';
      ELSIF UPPER(code_parameter) = 'NOT' THEN
	 code_clause := ' AND a.code != b.code';
      ELSIF UPPER(code_parameter) = 'NONE' THEN
	 code_clause := '';
      ELSE
	 msp_error_code := 31;
	 msp_error_detail := 'code_parameter='||code_parameter;
	 RAISE generate_facts_exc;
      END IF;

      msp_location := '125';
      tty_clause := '';
      IF UPPER(tty_parameter) = 'EXACT' OR
         UPPER(tty_parameter) = 'Y' THEN
        tty_clause := 
	  ' AND SUBSTR(a.termgroup,INSTR(a.termgroup,''/'')+1) = ' ||
	  ' SUBSTR(b.termgroup,INSTR(b.termgroup,''/'')+1) ';
      ELSIF UPPER(tty_parameter) = 'NOT' THEN
        tty_clause := 
	  ' AND SUBSTR(a.termgroup,INSTR(a.termgroup,''/'')+1) != ' ||
	  ' SUBSTR(b.termgroup,INSTR(b.termgroup,''/'')+1) ';
      ELSIF UPPER(tty_parameter) = 'N' OR
	    UPPER(tty_parameter) = 'NONE' THEN
        tty_clause := '';
      ELSE
	msp_error_code := 33;
	msp_error_detail := 'tty_parameter='||tty_parameter;
	RAISE generate_facts_exc;
      END IF;

      -- set up source_aui clause
      msp_location := '127.1';
      IF UPPER(source_aui_parameter) = 'EXACT' THEN
	 saui_clause := ' AND a.source_aui = b.source_aui ';
      ELSIF UPPER(source_aui_parameter) = 'NOT' THEN
	 saui_clause := ' AND a.source_aui != b.source_aui ';
      ELSIF UPPER(source_aui_parameter) = 'NONE' THEN
	 saui_clause := '';
      ELSE
	 msp_error_code := 31;
	 msp_error_detail := 'source_aui_parameter='||source_aui_parameter;
	 RAISE generate_facts_exc;
      END IF;

      msp_location := '127.2';
      IF UPPER(source_cui_parameter) = 'EXACT' THEN
	 scui_clause := ' AND a.source_cui = b.source_cui ';
      ELSIF UPPER(source_cui_parameter) = 'NOT' THEN
	 scui_clause := ' AND a.source_cui != b.source_cui ';
      ELSIF UPPER(source_cui_parameter) = 'NONE' THEN
	 scui_clause := '';
      ELSE
	 msp_error_code := 31;
	 msp_error_detail := 'source_cui_parameter='||source_cui_parameter;
	 RAISE generate_facts_exc;
      END IF;

      msp_location := '127.3';
      IF UPPER(source_dui_parameter) = 'EXACT' THEN
	 sdui_clause := ' AND a.source_dui = b.source_dui ';
      ELSIF UPPER(source_dui_parameter) = 'NOT' THEN
	 sdui_clause := ' AND a.source_dui != b.source_dui ';
      ELSIF UPPER(source_dui_parameter) = 'NONE' THEN
	 sdui_clause := '';
      ELSE
	 msp_error_code := 31;
	 msp_error_detail := 'source_dui_parameter='||source_dui_parameter;
	 RAISE generate_facts_exc;
      END IF;

      -- set up from and where clause
      IF upper(table_name) = 'SOURCE_CLASSES_ATOMS' THEN
         from_clause := ' FROM source_classes_atoms a, 
            (select atom_id,code,source,termgroup,isui,sui,lui,
              source_cui,source_dui,source_aui,concept_id,tobereleased from classes
             union all
             select atom_id,code,source,termgroup,isui,sui,lui,
              source_cui,source_dui,source_aui,concept_id,tobereleased from source_classes_atoms
             where switch != ''I'') b, '|| termgroup_table_1||' c, '||
            termgroup_table_2||' d';
      ELSE
         from_clause := ' FROM classes a, classes b, '||
            termgroup_table_1||' c, '||
            termgroup_table_2||' d';
      END IF;

      where_clause := string_clause||
	 ' a.concept_id != b.concept_id'||
	 ' AND a.tobereleased IN (''Y'',''y'',''?'')'||
	 ' AND b.tobereleased IN (''Y'',''y'',''?'')'||
	 ' AND a.termgroup = c.termgroup'||
	 ' AND b.termgroup = d.termgroup';

      IF UPPER(string_parameter) IN ('NORM','EXACT','BOTH') THEN
	 where_clause := where_clause||
	    ' AND a.lui !=
	        (SELECT min(lui) FROM string_ui WHERE norm_string IS NULL AND language=''ENG'')';
      END IF;

      IF UPPER(string_parameter) = 'EXACT' THEN
	 where_clause := where_clause||' AND a.isui = b.isui';
      ELSIF UPPER(string_parameter) = 'NORM' THEN
	 where_clause := where_clause||' AND a.isui != b.isui';
      END IF;

      where_clause := ' WHERE '||where_clause||code_clause||tty_clause||saui_clause||scui_clause||sdui_clause;

      -- set up insert clause
      insert_clause :=
	 'INSERT INTO mom_candidate_facts
	     (atom_id_1, atom_id_2, code1, code2, termgroup1, termgroup2,
	      source1, source2, merge_level, status, lui_1, lui_2,
	      isui_1, isui_2, sui_1, sui_2, merge_set, source)
	  SELECT /*+ PARALLEL(a) */
	      a.atom_id, b.atom_id, a.code, b.code,
	      a.termgroup, b.termgroup, a.source, b.source,
	      ''NOT'', ''R'', a.lui, b.lui, a.isui, b.isui, a.sui, b.sui,
	      '''||generate_facts.merge_set||''''||',
	      '''||generate_facts.source||'''';

   ELSIF UPPER(table_name) = 'RELATIONSHIPS' THEN

      /* set up from and where clause */
      from_clause := ' FROM '||LOWER(table_name)||' r, classes a, classes b';

      where_clause := 
	 ' WHERE relationship_name = '||'''SY'''|| ' 
	     AND atom_id_1 = a.atom_id AND atom_id_2 = b.atom_id
	     AND a.concept_id != b.concept_id
	     AND a.tobereleased != ''N'' AND b.tobereleased != ''N'' ';

      /* set up insert clause */
      insert_clause :=
	 'INSERT INTO mom_candidate_facts
	     (atom_id_1, atom_id_2, code1, code2, termgroup1, termgroup2,
	      source1, source2, merge_level, status, merge_set, source)
	  SELECT /*+ PARALLEL(r) */
	      r.atom_id_1, r.atom_id_2, a.code, b.code,
	      a.termgroup, b.termgroup, a.source, b.source,
	      ''NOT'', ''R'', 
	      ''' || generate_facts.merge_set || ''''||',
	      ''' || generate_facts.source || '''';
   ELSE
      msp_location := '130';
      msp_error_code := 34;
      msp_error_detail := 'table_name='||table_name;
      RAISE generate_facts_exc;
   END IF;

   msp_location := '140';

   -- truncate table mom_candidate_facts
   MEME_SYSTEM.truncate('mom_candidate_facts');

   msp_location := '150';

   -- combine all clauses and execute
   local_exec(insert_clause||from_clause||where_clause);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::generate_facts',
   	'Done inserting data into mom_candidate_facts',work_id,0,20);

   -- update mom_candidate_facts to compute merge level

   -- exact string and matching tty
   msp_location := '160';
   UPDATE /*+ PARALLEL(a) */ mom_candidate_facts a
   SET merge_level='MTY'
   WHERE isui_1 = isui_2
     AND substr(termgroup1,INSTR(termgroup1,'/')) =
	 substr(termgroup2,INSTR(termgroup2,'/'));
   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::generate_facts',
   	'Done exact string and matching tty',work_id,0,40);

   -- exact string 
   msp_location := '160.1';
   UPDATE /*+ PARALLEL(a) */ mom_candidate_facts a
   SET merge_level='MAT'
   WHERE isui_1 = isui_2
     AND substr(termgroup1,INSTR(termgroup1,'/')) !=
	 substr(termgroup2,INSTR(termgroup2,'/'));
   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::generate_facts',
   	'Done exact string',work_id,0,60);

   -- norm string 
   msp_location := '160.2a';
   UPDATE /*+ PARALLEL(a) */ mom_candidate_facts a
   SET merge_level='NTY'
   WHERE lui_1 = lui_2
     AND isui_1 != isui_2
     AND substr(termgroup1,INSTR(termgroup1,'/')) =
	 substr(termgroup2,INSTR(termgroup2,'/'));
   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::generate_facts',
   	'Done norm string',work_id,0,70);

   -- norm string 
   msp_location := '160.2b';
   UPDATE /*+ PARALLEL(a) */ mom_candidate_facts a
   SET merge_level='NRM'
   WHERE lui_1 = lui_2
     AND isui_1 != isui_2
     AND substr(termgroup1,INSTR(termgroup1,'/')) !=
	 substr(termgroup2,INSTR(termgroup2,'/'));
   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::generate_facts',
   	'Done norm string',work_id,0,80);

   -- tty match
   msp_location := '160.2';
   UPDATE /*+ PARALLEL(a) */ mom_candidate_facts a
   SET merge_level='TTY'
   WHERE isui_1 != isui_2
     AND lui_1 != lui_2
     AND substr(termgroup1,INSTR(termgroup1,'/')) =
	 substr(termgroup2,INSTR(termgroup2,'/'));
   COMMIT;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::generate_facts',
   	'Done norm string',work_id,0,90);

   msp_location := '170';
   --MEME_SYSTEM.analyze('mom_candidate_facts');

   msp_location := '180';
   -- log operation
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Merging',
	detail => 'Generate merge facts ('||merge_set||')',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::generate_facts',
   	'Done process generate facts',work_id,0,100);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');
   COMMIT;

EXCEPTION
   WHEN generate_facts_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END generate_facts;

/* PROCEDURE FILTER_FACTS ******************************************************
 *
 * Currently there are only 6 options for
 * type_1, type_2:
 * EXCLUDE_LIST, NORM_EXECLUE_LIST, SAFE_REPLACEMENT, NEW_ATOMS, CHEM_STY
 * NON_CHEM_STY, SEMANTIC_TYPE, SOURCE, TERMGROUP, STATUS.
 *
 * More will be added later.
 */
PROCEDURE filter_facts(
   type_1    IN VARCHAR2,
   type_2    IN VARCHAR2,   
   arg_1     IN VARCHAR2,   
   arg_2     IN VARCHAR2,   
   not_1     IN VARCHAR2,   
   not_2     IN VARCHAR2,   
   merge_set IN VARCHAR2,   
   authority IN VARCHAR2,   
   source    IN VARCHAR2,
   work_id   IN INTEGER
)
IS
   select_clause		VARCHAR2(1000);
   where_clause 		VARCHAR2(1000);
   reverse_where_clause 	VARCHAR2(1000);
   where_clause1 		VARCHAR2(1000);
   reverse_where_clause1 	VARCHAR2(1000);
   where_clause2 		VARCHAR2(1000);
   reverse_where_clause2	VARCHAR2(1000);
   not_clause1			VARCHAR2(10);
   not_clause2			VARCHAR2(10);
   params			VARCHAR2(1000);
   filter_facts_exc		EXCEPTION;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::filter_facts',
   	'Starting filter_facts',work_id,0,1);

   initialize_trace('FILTER_FACTS');
   MEME_UTILITY.sub_timing_start;

   where_clause1 := 'EMPTY';
   reverse_where_clause1 := 'EMPTY';
   where_clause2 := 'EMPTY';
   reverse_where_clause2 := 'EMPTY';

   IF not_1 = MEME_CONSTANTS.YES THEN
	not_clause1 := ' NOT ';
   ELSE
	not_clause1 := ' ';
   END IF;

   IF not_2 = MEME_CONSTANTS.YES THEN
	not_clause2 := ' NOT ';
   ELSE
	not_clause2 := ' ';
   END IF;

   -- 
   -- EXCLUDE_LIST, NORM_EXCLUDE_LIST, SAFE_REPLACEMENT, and NEW_ATOMS
   -- really are unary checks. If type_1 is set to NEW_ATOMS, this
   -- should filter facts where atom_id_1 is a new atom, not facts
   -- where atom_id_1 or atom_id_2 are facts.  However, for something
   -- like type_1= CHEM_STY and type_2 = TERMGROUP, it means that
   -- rows where either atom_id_1 has a CHEM_STY and atom_id_2 matches
   -- a particular termgroup, OR Atom_id_2 has a CHEM_STY and atom_id_1
   -- matches a particular termgroup should be deleted. 
   --   This behavior is NOT currently enforced.
   -- 
   IF UPPER(type_1) = 'EXCLUDE_LIST' THEN
      select_clause :=
	 '(SELECT termgroup FROM mom_exclude_list) ';
      where_clause1 :=
	 ' termgroup1 ' || not_clause1 || ' IN '||select_clause;

   ELSIF UPPER(type_1) = 'NORM_EXCLUDE_LIST' THEN
      select_clause :=
	 '(SELECT termgroup FROM mom_norm_exclude_list) ';
      where_clause1 :=
	 ' merge_level in (''NRM'',''NTY'') AND termgroup1 ' || not_clause1 || ' IN '||select_clause;

   ELSIF UPPER(type_1) = 'SAFE_REPLACEMENT' THEN
      select_clause :=
	 '(SELECT new_atom_id FROM mom_safe_replacement'||
	 ' WHERE source='||''''||filter_facts.source||''''||') ';
      where_clause1 :=
	 ' atom_id_1 ' || not_clause1 || ' IN ' || select_clause;

   ELSIF UPPER(type_1) = 'NEW_ATOMS' THEN
      IF not_1 = MEME_CONSTANTS.YES THEN
          where_clause1 := 
	    ' atom_id_1 IN (SELECT atom_id FROM source_classes_atoms) ';
      ELSE
          where_clause1 := 
	    ' atom_id_1 NOT IN (SELECT atom_id FROM source_classes_atoms) ';
      END IF;

   ELSIF UPPER(type_1) = 'CHEM_STY' THEN
      select_clause := 
	'(SELECT a.atom_id FROM classes a, attributes b, semantic_types c ' ||
	' WHERE a.concept_id=b.concept_id ' ||
	' AND attribute_name=''SEMANTIC_TYPE'' ' ||
	' AND attribute_value = semantic_type ' ||
	' AND is_chem = ''Y'' ) ';
      where_clause1 :=
	'atom_id_1 ' || not_clause1 || ' IN ' || select_clause;
      reverse_where_clause1 :=
	'atom_id_2 ' || not_clause1 || ' IN ' || select_clause;
   
   ELSIF UPPER(type_1) = 'NON_CHEM_STY' THEN
      select_clause := 
	'(SELECT a.atom_id FROM classes a, attributes b, semantic_types c ' ||
	' WHERE a.concept_id=b.concept_id ' ||
	' AND attribute_name=''SEMANTIC_TYPE'' ' ||
	' AND attribute_value = semantic_type ' ||
	' AND is_chem != ''Y'' ) ';
      where_clause1 :=
	'atom_id_1 ' || not_clause1 || ' IN ' || select_clause;
      reverse_where_clause1 :=
	'atom_id_2 ' || not_clause1 || ' IN ' || select_clause;

   ELSIF UPPER(type_1) = 'SEMANTIC_TYPE' THEN
      select_clause := 
	'(SELECT a.atom_id FROM classes a, attributes b, ' || arg_1 || ' c
	  WHERE a.concept_id=b.concept_id
	  AND attribute_name=''SEMANTIC_TYPE''
	  AND attribute_value = semantic_type ) ';
      where_clause1 :=
	'atom_id_1 ' || not_clause1 || ' IN ' || select_clause;
      reverse_where_clause1 :=
	'atom_id_2 ' || not_clause1 || ' IN ' || select_clause;

   ELSIF UPPER(type_1) = 'SOURCE' THEN
      select_clause := 
	'(SELECT source FROM ' || arg_1 || ')';
      where_clause1 :=
	'source1 ' || not_clause1 || ' IN ' || select_clause;
      reverse_where_clause1 :=
	'source2 ' || not_clause1 || ' IN ' || select_clause;

   ELSIF UPPER(type_1) = 'TERMGROUP' THEN
      select_clause := 
	'(SELECT termgroup FROM ' || arg_1 || ')';
      where_clause1 :=
	'termgroup1 ' || not_clause1 || ' IN ' || select_clause;
      reverse_where_clause1 :=
	'termgroup2 ' || not_clause1 || ' IN ' || select_clause;

   ELSIF UPPER(type_1) = 'STATUS' THEN
      select_clause := 
	'(SELECT atom_id FROM classes a, ' || arg_1 || 
	' b WHERE a.status=b.status)';
      where_clause1 :=
	'atom_id_1 ' || not_clause1 || ' IN ' || select_clause;
      reverse_where_clause1 :=
	'atom_id_2 ' || not_clause1 || ' IN ' || select_clause;

   ELSIF UPPER(type_1) = 'NO_FILTER' THEN
      where_clause1 := ' 1=1 ';

   ELSIF type_1 IS NOT NULL THEN
      msp_error_code := 35;
      msp_error_detail := 'type_1='||type_1;
      RAISE filter_facts_exc;
   END IF;

   IF UPPER(type_2) = 'EXCLUDE_LIST' THEN
      select_clause :=
	 '(SELECT termgroup FROM mom_exclude_list) ';
      where_clause2 :=
	 ' termgroup2 ' || not_clause2 || ' IN '||select_clause;

   ELSIF UPPER(type_2) = 'NORM_EXCLUDE_LIST' THEN
      select_clause :=
	 '(SELECT termgroup FROM mom_norm_exclude_list) ';
      where_clause2 :=
	 ' merge_level IN (''NRM'',''NTY'') AND termgroup2 ' || not_clause2 || ' IN ' || select_clause;

   ELSIF UPPER(type_2) = 'SAFE_REPLACEMENT' THEN
      select_clause :=
	 '(SELECT new_atom_id FROM mom_safe_replacement'||
	 ' WHERE source='||''''||filter_facts.source||''''||') ';
      where_clause2 :=
	 ' atom_id_2 ' || not_clause2 || ' IN ' || select_clause;

   ELSIF UPPER(type_2) = 'NEW_ATOMS' THEN
      IF not_2 = MEME_CONSTANTS.YES THEN
          where_clause2 := 
	    ' atom_id_2 IN (SELECT atom_id FROM source_classes_atoms) ';
      ELSE
          where_clause2 := 
	    ' atom_id_2 NOT IN (SELECT atom_id FROM source_classes_atoms) ';
      END IF;

   ELSIF UPPER(type_2) = 'CHEM_STY' THEN
      select_clause := 
	'(SELECT a.atom_id FROM classes a, attributes b, semantic_types c ' ||
	' WHERE a.concept_id=b.concept_id ' ||
	' AND attribute_name=''SEMANTIC_TYPE'' ' ||
	' AND attribute_value = semantic_type ' ||
	' AND is_chem = ''Y'' ) ';
      where_clause2 :=
	'atom_id_2 ' || not_clause2 || ' IN ' || select_clause;
      reverse_where_clause2 :=
	'atom_id_1 ' || not_clause2 || ' IN ' || select_clause;

   ELSIF UPPER(type_2) = 'NON_CHEM_STY' THEN
      select_clause := 
	'(SELECT a.atom_id FROM classes a, attributes b, semantic_types c ' ||
	' WHERE a.concept_id=b.concept_id ' ||
	' AND attribute_name=''SEMANTIC_TYPE'' ' ||
	' AND attribute_value = semantic_type ' ||
	' AND is_chem != ''Y'' ) ';
      where_clause2 :=
	'atom_id_2 ' || not_clause2 || ' IN ' || select_clause;
      reverse_where_clause2 :=
	'atom_id_1 ' || not_clause2 || ' IN ' || select_clause;

   ELSIF UPPER(type_2) = 'SEMANTIC_TYPE' THEN
      select_clause := 
	'(SELECT a.atom_id FROM classes a, attributes b, ' || arg_2 || ' c
	  WHERE a.concept_id=b.concept_id
	  AND attribute_name=''SEMANTIC_TYPE''
	  AND attribute_value = semantic_type ) ';
      where_clause2 :=
	'atom_id_2 ' || not_clause2 || ' IN ' || select_clause;
      reverse_where_clause2 :=
	'atom_id_1 ' || not_clause2 || ' IN ' || select_clause;

   ELSIF UPPER(type_2) = 'SOURCE' THEN
      select_clause := 
	'(SELECT source FROM ' || arg_2 || ')';
      where_clause2 :=
	'source2 ' || not_clause2 || ' IN ' || select_clause;
      reverse_where_clause2 :=
	'source1 ' || not_clause2 || ' IN ' || select_clause;

   ELSIF UPPER(type_2) = 'TERMGROUP' THEN
      select_clause := 
	'(SELECT termgroup FROM ' || arg_2 || ')';
      where_clause2 :=
	'termgroup2 ' || not_clause2 || ' IN ' || select_clause;
      reverse_where_clause2 :=
	'termgroup1 ' || not_clause2 || ' IN ' || select_clause;

   ELSIF UPPER(type_2) = 'STATUS' THEN
      select_clause := 
	'(SELECT atom_id FROM classes a, ' || arg_2 || 
	' b WHERE a.status=b.status)';
      where_clause2 :=
	'atom_id_2 ' || not_clause2 || ' IN ' || select_clause;
      reverse_where_clause2 :=
	'atom_id_1 ' || not_clause2 || ' IN ' || select_clause;

   ELSIF UPPER(type_2) = 'NO_FILTER' THEN
      where_clause2 := ' 1=1 ';

   ELSIF type_2 IS NOT NULL THEN
      msp_error_code := 35;
      msp_error_detail := 'type_2='||type_2;
      RAISE filter_facts_exc;
   END IF;

   -- If both where clauses have values, add an AND
   IF where_clause1 != 'EMPTY' AND where_clause2 != 'EMPTY' THEN
	where_clause := 'WHERE ' || where_clause1 ||
			' AND ' || where_clause2;
   ELSIF where_clause1 != 'EMPTY' THEN
	where_clause := 'WHERE ' || where_clause1;
   ELSIF where_clause2 != 'EMPTY' THEN
	where_clause := 'WHERE ' || where_clause2;
   ELSE
	where_clause := 'WHERE 1=0';
   END IF;

   -- If both where clauses have values, add an AND
   IF reverse_where_clause1 != 'EMPTY' AND reverse_where_clause2 != 'EMPTY' THEN
	reverse_where_clause := 'WHERE ' || reverse_where_clause1 ||
			' AND ' || reverse_where_clause2;
   ELSIF reverse_where_clause1 != 'EMPTY' THEN
	reverse_where_clause := 'WHERE ' || reverse_where_clause1;
   ELSIF reverse_where_clause2 != 'EMPTY' THEN
	reverse_where_clause := 'WHERE ' || reverse_where_clause2;
   ELSE
	reverse_where_clause := 'WHERE 1=0';
   END IF;

   msp_location := '110';
   local_exec(
	'DELETE /*+ PARALLEL(a) */ 
         FROM mom_candidate_facts a ' || 
	 where_clause);

   msp_location := '120';
   local_exec(
	'DELETE /*+ PARALLEL(a) */ 
 	 FROM mom_candidate_facts a ' || 
	 reverse_where_clause);
 
   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   params := 
	not_1 || ',' || type_1 || ',' ||
	not_2 || ',' || type_2;

   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Merging',
	detail => 'Filter merge set (' || merge_set || ', ' || params || ')',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::filter_facts',
   	'Done filter_facts',work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN filter_facts_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END filter_facts;

/* PROCEDURE LOAD_FACTS ********************************************************
 */
PROCEDURE load_facts(
   authority	     IN VARCHAR2,
   merge_set	     IN VARCHAR2,
   integrity_vector  IN VARCHAR2,
   change_status     IN VARCHAR2,
   make_demotion     IN VARCHAR2,
   work_id	     IN INTEGER,
   truncate_flag     IN VARCHAR2 := MEME_CONSTANTS.YES
)
IS
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::load_facts',
   	'Starting load_facts',work_id,0,1);

   initialize_trace('LOAD_FACTS');
   MEME_UTILITY.sub_timing_start;

   IF truncate_flag = MEME_CONSTANTS.YES THEN
	MEME_SYSTEM.truncate('mom_merge_facts');
   END IF;

   msp_location := '10';
   MEME_SYSTEM.drop_indexes('mom_merge_facts');

   -- 
   -- We would like to ignore redundant
   -- facts.  This is tricky because figuring
   -- out the atom_id/concept_id mapping
   -- requires some work.
   --
   msp_location := '15.1';
   MEME_UTILITY.drop_it('table','mom_merge_facts_h1');
   msp_location := '15.2';
   EXECUTE IMMEDIATE
      'CREATE TABLE mom_merge_facts_h1 NOLOGGING AS
       SELECT atom_id, concept_id FROM classes a, mom_candidate_facts b
       WHERE atom_id_1 = atom_id
       UNION
       SELECT atom_id, concept_id FROM classes a, mom_candidate_facts b
       WHERE atom_id_2 = atom_id';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::load_facts',
   	'Done mapping of atom and concept id',work_id,0,20);

   -- We only want source_classes_atoms rows if they haven't been inserted yte
   -- After insert, the switch will be 'I'
   msp_location := '15.31';
   EXECUTE IMMEDIATE
      'INSERT INTO mom_merge_facts_h1
       SELECT atom_id, concept_id FROM source_classes_atoms a, mom_candidate_facts b
       WHERE atom_id_1 = atom_id AND switch = ''R''
       UNION
       SELECT atom_id, concept_id FROM source_classes_atoms a, mom_candidate_facts b
       WHERE atom_id_2 = atom_id AND switch = ''R'' ';

   msp_location := '15.4';
   MEME_UTILITY.drop_it('table','mom_merge_facts_h2');
   msp_location := '15.5';
   EXECUTE IMMEDIATE
      'CREATE TABLE mom_merge_facts_h2 NOLOGGING AS
       SELECT min(m.rowid) as row_id
       FROM mom_candidate_facts m, mom_merge_facts_h1 a, mom_merge_facts_h1 b
       WHERE a.concept_id != b.concept_id
	 AND a.atom_id = atom_id_1
	 AND b.atom_id = atom_id_2
	 AND merge_set = ''' || load_facts.merge_set || '''
       GROUP BY a.concept_id, b.concept_id';

   msp_location := '20';
   EXECUTE IMMEDIATE '
      INSERT INTO mom_merge_facts
      	(merge_fact_id, atom_id_1, merge_level,
       	 atom_id_2, source, integrity_vector,
       	 make_demotion, change_status, authority,
      	 merge_set, violations_vector, status,
      	 merge_order, molecule_id, work_id)
      SELECT rownum, atom_id_1, merge_level, atom_id_2, source,
      	:integrity_vector, :make_demotion,
      	:change_status, :authority, merge_set,
      	null,''R'', 
	DECODE(merge_level,''SY'',6,''MTY'',5,''MAT'',4,
			   ''NTY'',3,''NRM'',2,''TTY'',1,0), 
	0, :x
      FROM mom_candidate_facts
      WHERE merge_set = :x 
	AND rowid IN (SELECT * FROM mom_merge_facts_h2)'
   USING load_facts.integrity_vector, load_facts.make_demotion,
	 load_facts.change_status, load_facts.authority,
	 load_facts.work_id, load_facts.merge_set;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::load_facts',
   	'Done inserting data into mom_merge_facts',work_id,0,40);

   -- this also analyzes
   msp_location := '40';
   MEME_SYSTEM.reindex('mom_merge_facts');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::load_facts',
   	'Done analyzing mom_merge_facts',work_id,0,80);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');
   msp_location := '45';
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Merging',
	detail => 'Load merge set ('||merge_set||')',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   msp_location := '45.1';
   MEME_UTILITY.drop_it('table','mom_merge_facts_h1');
   msp_location := '45.2';
   MEME_UTILITY.drop_it('table','mom_merge_facts_h2');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::load_facts',
   	'Done process load_facts',work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END load_facts;

/* PROCEDURE MOVE_PROCESSED_FACTS **********************************************
 */
PROCEDURE move_processed_facts(
   authority IN VARCHAR2,
   work_id   IN INTEGER
)
IS
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::move_processed_facts',
   	'Starting move_processed_facts',work_id,0,1);

   initialize_trace('MOVE_PROCESSED_FACTS');
   MEME_UTILITY.sub_timing_start;

   INSERT INTO mom_facts_processed
   SELECT * FROM mom_merge_facts;

   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Merging',
	detail => 'Move processed merge facts',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::move_processed_facts',
   	'Done move_processed_facts',work_id,0,100);

EXCEPTION
   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END move_processed_facts;

/* PROCEDURE REPLACEMENT_MERGES **********************************************
 * ambig_flag=1 -> one to one
 * ambig_flag=0 -> n to n
 *
 * normalization_flag=1 -> merge into lowest concept_id
 */
PROCEDURE replacement_merges(
   merge_set	      IN VARCHAR2,
   normalization_flag IN VARCHAR2,
   ambig_flag	      IN VARCHAR2,
   authority	      IN VARCHAR2,   /* not used */
   work_id	      IN INTEGER
)
IS
   tmp1 	 VARCHAR2(20);
   tmp2 	 VARCHAR2(20);
   tmp3 	 VARCHAR2(20);
   tmp4 	 VARCHAR2(20);
   tmp5 	 VARCHAR2(20);
   row_id	 ROWID;
   from_clause	 VARCHAR2(100);
   merge_set_pre VARCHAR2(30);

   qry_str	 VARCHAR2(500);
   table_name	 VARCHAR2(50);
   id_1 	 NUMBER(12);
   id_2 	 NUMBER(12);
   ct		 NUMBER(12);
   concept_id	 NUMBER(12);

   TYPE curvar_type IS REF CURSOR;
   curvar curvar_type;

BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Starting replacement_merges',work_id,0,1);

   initialize_trace('REPLACEMENT_MERGES');
   MEME_UTILITY.sub_timing_start;

   msp_location := '10';
   --MEME_SYSTEM.analyze('mom_merge_facts');

   msp_location := '20';
   merge_set_pre := replacement_merges.merge_set || '-PRE';
   local_exec(
       'UPDATE mom_merge_facts
	SET merge_set = ''' || merge_set_pre || ''' '
   );

   msp_location := '30';
   tmp1 := MEME_UTILITY.get_unique_tablename;
 
   --
   -- Here, maybe we should remove facts involving atoms
   -- from previous merge sets that did not merge
   -- i.e.
   --  delete from mom_merge_facts a
   --  where atom_id_1 in
   --   (select atom_id_1 from mom_facts_processed b
   --	where a.source = b.source and status !='M')
   --  delete from mom_merge_facts a
   --  where atom_id_1 in
   --   (select atom_id_2 from mom_facts_processed b
   --	where a.source = b.source and status !='M')
   --  delete from mom_merge_facts a
   --  where atom_id_2 in
   --   (select atom_id_1 from mom_facts_processed b
   --	where a.source = b.source and status !='M')
   --  delete from mom_merge_facts a
   --  where atom_id_2 in
   --   (select atom_id_2 from mom_facts_processed b
   --	where a.source = b.source and status !='M')

   local_exec(
       'CREATE TABLE ' || tmp1 || ' NOLOGGING AS
        SELECT atom_id_1 AS atom_id
        FROM mom_merge_facts WHERE merge_set = ''' || merge_set_pre || '''
        UNION
        SELECT atom_id_2
        FROM mom_merge_facts WHERE merge_set = ''' || merge_set_pre || ''' ');

   msp_location := '120';
   tmp2 := MEME_UTILITY.get_unique_tablename;

   msp_location := '125';
   --MEME_SYSTEM.analyze(tmp1);

   local_exec(
       'CREATE TABLE ' || tmp2 || ' NOLOGGING AS
        SELECT atom_id, concept_id FROM classes
        WHERE atom_id IN (SELECT atom_id FROM ' || tmp1 || ') 
        UNION 
        SELECT atom_id, concept_id FROM source_classes_atoms
        WHERE atom_id IN (SELECT atom_id FROM ' || tmp1 || ') ');

   msp_location := '130';
   tmp3 := MEME_UTILITY.get_unique_tablename;

   msp_location := '135';
   --MEME_SYSTEM.analyze(tmp2);

   local_exec(
	'CREATE TABLE ' || tmp3 || ' NOLOGGING AS
	 SELECT /*+ PARALLEL(a) */ b.concept_id as concept_id_1,
	        c.concept_id as concept_id_2
	 FROM mom_merge_facts a, ' || tmp2 || ' b, ' || tmp2 || ' c
	 WHERE atom_id_1 = b.atom_id
	   AND atom_id_2 = c.atom_id
	   AND b.concept_id != c.concept_id ');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Done removing facts involving atoms',work_id,0,10);

   -- Generate clusters
   msp_location := '137';
   tmp4 := MEME_UTILITY.cluster_pair_recursive(tmp3);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Done generate clusters',work_id,0,20);

   -- set cluster_id to the concept_id into which to merge.
   --
   -- If normalization flag is 1 merge into lowest concept id
   msp_location := '138';
   MEME_UTILITY.drop_it('table',tmp4||'_2');
   IF normalization_flag = '1' THEN
      msp_location := '140';
      local_exec(
	'CREATE TABLE ' || tmp4 || '_2 NOLOGGING AS
	 SELECT a.concept_id AS concept_id_1, b.concept_id as concept_id_2
	 FROM ' || tmp4 || ' a, 
	  (SELECT min(concept_id) as concept_id, cluster_id
	   FROM ' || tmp4 || ' 
	   GROUP BY cluster_id) b
	 WHERE a.cluster_id = b.cluster_id ');
   ELSE
      msp_location := '150';
      local_exec(
	'CREATE TABLE ' || tmp4 || '_2 NOLOGGING AS
	 SELECT a.concept_id AS concept_id_1, b.concept_id as concept_id_2
	 FROM ' || tmp4 || ' a, 
	  (SELECT max(concept_id) as concept_id, cluster_id
	   FROM ' || tmp4 || '
	   GROUP BY cluster_id) b
	 WHERE a.cluster_id = b.cluster_id ');
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Done setting cluster or merging into lowest concept id',work_id,0,30);

    -- Remove self-referential rows
    local_exec('DELETE FROM ' || tmp4 || '_2 WHERE concept_id_1 = concept_id_2');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Done removing self-referential rows',work_id,0,40);

    -- if we are using 1-1 merges, remove all cases where a concept_id
    -- will be merged with more than one other concept_id
    -- Also remove cases that would merge old concepts -> old concepts
    IF ambig_flag = 1 THEN
	msp_location := '160.1';
	local_exec('DELETE FROM ' || tmp4 || '_2 
		    WHERE concept_id_2 IN 
	   	     (SELECT concept_id_2 FROM ' || tmp4 || '_2
		      GROUP BY concept_id_2 
	 	      HAVING count(distinct concept_id_1) > 1)' );

	msp_location := '160.1b';
	local_exec('DELETE FROM ' || tmp4 || '_2 
		    WHERE concept_id_1 IN 
	   	     (SELECT concept_id_1 FROM ' || tmp4 || '_2
		      GROUP BY concept_id_1 
	  	      HAVING count(distinct concept_id_2) > 1)' );

	msp_location := '160.2';
	local_exec('DELETE FROM ' || tmp4 || '_2 
		    WHERE concept_id_1 < 
	   	     (SELECT min(concept_id) FROM source_concept_status)
		      AND concept_id_2 < 
	   	     (SELECT min(concept_id) FROM source_concept_status) ');

    END IF;

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Done removing cases that merged more than one if using 1-1 merges',work_id,0,50);

    -- Now, we have a correct tmp4 table, update source_classes_atoms
    msp_location := '180.1';
    EXECUTE IMMEDIATE
	'CREATE INDEX x_' || tmp4 || '_2 on ' || tmp4 || '_2 (concept_id_1) TABLESPACE MIDI
	 COMPUTE STATISTICS PARALLEL NOLOGGING';

    -- Soma changing for 10g Performance
    --MEME_SYSTEM.analyze(tmp4);
	
    msp_location := '180.2';
    EXECUTE IMMEDIATE
        'UPDATE source_classes_atoms a
	 SET concept_id = 
	    (SELECT DISTINCT b.concept_id_2 FROM ' || tmp4 || '_2 b
	     WHERE a.concept_id = b.concept_id_1)
	 WHERE concept_id IN
	    (SELECT b.concept_id_1 FROM ' || tmp4 || '_2 b)';
    MEME_UTILITY.put_message(SQL%ROWCOUNT || ' source_classes_atoms rows updated.');
	COMMIT;

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Done updating source_classes_atoms',work_id,0,60);

/**    qry_str := 
	'SELECT DISTINCT a.rowid, a.concept_id, b.concept_id_2
	 FROM source_classes_atoms a, ' || tmp4 || '_2 b
	 WHERE a.concept_id = b.concept_id_1 ';
    ct := 0;
    OPEN curvar FOR qry_str;
    LOOP
	msp_location := '185';
	FETCH curvar INTO row_id, id_1, id_2;
	EXIT WHEN curvar%NOTFOUND;

	msp_location := '190';
	UPDATE source_classes_atoms
	SET concept_id = id_2
	WHERE rowid = row_id;
	ct := ct + 1;

    END LOOP;
    MEME_UTILITY.put_message(ct || ' source_classes_atoms rows updated.');
    CLOSE curvar;
*****/
	-- Soma changing for 10g performance
    --MEME_SYSTEM.analyze('source_classes_atoms');
    
    msp_location := '210';
    local_exec(
	'UPDATE source_concept_status SET switch = ''N''
	 WHERE concept_id IN 
	  (SELECT concept_id FROM source_concept_status
           WHERE switch != ''N''
           MINUS SELECT concept_id FROM source_classes_atoms) ');

    --MEME_SYSTEM.analyze('source_concept_status');
	
    msp_location := '220';
    MEME_UTILITY.drop_it('table',tmp2);
    local_exec(
      'CREATE TABLE ' || tmp2 || ' NOLOGGING AS
       SELECT atom_id, concept_id FROM classes
       WHERE atom_id IN (SELECT atom_id FROM ' || tmp1 || ') 
       UNION 
       SELECT atom_id, concept_id FROM source_classes_atoms
       WHERE atom_id IN (SELECT atom_id FROM ' || tmp1 || ') ');

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Done updating source_concept_status',work_id,0,70);
	
   -- create and use the index
   msp_location := '230';
   local_exec(
      'CREATE INDEX x_' || tmp2 || ' ON ' || tmp2 || ' (atom_id) TABLESPACE MIDI
       COMPUTE STATISTICS PARALLEL NOLOGGING');

   msp_location := '240';
   --MEME_SYSTEM.analyze(tmp2);

   msp_location := '250';
   local_exec
      ('UPDATE mom_merge_facts SET status = ''M'' 
	WHERE merge_set = ''' || merge_set_pre || ''' 
	  AND (atom_id_1, atom_id_2) IN
	   (SELECT a.atom_id, b.atom_id
	    FROM ' || tmp2 || ' a, ' || tmp2 || ' b
	    WHERE a.concept_id = b.concept_id
	      AND atom_id_1 = a.atom_id
	      AND atom_id_2 = b.atom_id) '); 

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Done creating index',work_id,0,80);

   MEME_UTILITY.drop_it('table',tmp1);
   MEME_UTILITY.drop_it('table',tmp2);
   MEME_UTILITY.drop_it('table',tmp3);
   MEME_UTILITY.drop_it('table',tmp4);
   MEME_UTILITY.drop_it('table',tmp4||'_2');

   msp_location := '260';
   MEME_UTILITY.put_message(msp_method||' successfully completed.');
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Merging',
	detail => 'Perform pre-insert merging set ('||merge_set||')',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::replacement_merges',
   	'Done process replacement_merges',work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',tmp1);
      MEME_UTILITY.drop_it('table',tmp2);
      MEME_UTILITY.drop_it('table',tmp3);
      MEME_UTILITY.drop_it('table',tmp4);
      MEME_UTILITY.drop_it('table',tmp4||'_2');
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END replacement_merges;

/* PROCEDURE PREPATE_SRC_MERGEFACTS ********************************************
 */
PROCEDURE prepare_src_mergefacts(
   unique_flag	      IN VARCHAR2 := MEME_CONSTANTS.NO, /* not used */
   authority	      IN VARCHAR2,
   merge_set	      IN VARCHAR2 := '',
   work_id	      IN INTEGER := 0
)
IS
   row_count	              INTEGER;
   prepare_src_mergefacts_exc EXCEPTION;

BEGIN

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::prepare_src_mergefacts',
   	'Starting prepare_src_mergefacts',work_id,0,1);

   MEME_UTILITY.sub_timing_start;

   initialize_trace('PREPARE_SRC_MERGEFACTS');

   msp_location := '100';
   MEME_UTILITY.put_message(msp_method||': Executing MEME_SOURCE_PROCESSING.map_sg_fields ...');

    IF merge_set IS NOT NULL THEN
        -- Load only facts for specified merge set
   	msp_location := '100.1';
   	MEME_UTILITY.drop_it('table','mom_precomputed_facts_t1');
   	msp_location := '100.2';
   	EXECUTE IMMEDIATE
	    'CREATE TABLE mom_precomputed_facts_t1 NOLOGGING AS
   	     SELECT * FROM mom_precomputed_facts
	     WHERE merge_set = ''' || merge_set || ''' ';

   	msp_location := '100.3';
   	MEME_SYSTEM.truncate('mom_precomputed_facts');
   	msp_location := '100.4';
   	EXECUTE IMMEDIATE
	    'INSERT INTO mom_precomputed_facts SELECT * FROM mom_precomputed_facts_t1';
   	msp_location := '100.5';
   	MEME_UTILITY.drop_it('table','mom_precomputed_facts_t1');
   END IF;

   -- Analyze mom_precomputed_facts
   msp_location := '110';
   --MEME_SYSTEM.analyze('mom_precomputed_facts');

   MEME_SOURCE_PROCESSING.map_sg_fields(
      table_name => 'mom_precomputed_facts',
      pair_flag => MEME_CONSTANTS.YES,
      concept_flag => MEME_CONSTANTS.NO
   );
      
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::prepare_src_mergefacts',
   	'Done analyzing mom_precomputed_facts',work_id,0,30);

   msp_method := 'PREPARE_SRC_MERGEFACTS';

   -- All ids must be mapped unless they are CUIs
   -- It is a problem if id_1 is not mapped or
   -- if id_2 is not mapped and id_1 IS mapped
   msp_location := '200';

   UPDATE /*+ PARALLEL(a) */ mom_precomputed_facts a 
   SET status='R'
   WHERE NVL(atom_id_1,0) !=0 AND NVL(atom_id_2,0) !=0;
   COMMIT;

   msp_location := '210';
   row_count := 0;
   SELECT COUNT(*) INTO row_count FROM mom_precomputed_facts
   WHERE (NVL(atom_id_1,0) = 0 AND sg_type_1 not like 'CUI%')
      OR (NVL(atom_id_2,0) = 0 AND sg_type_2 not like 'CUI%'
	  AND (sg_type_1 not like 'CUI%' OR NVL(atom_id_1,0)=0));

   IF row_count > 0 THEN
      msp_error_code := 45;
      msp_error_detail := 'mom_precomputed_facts not fully mapped.';
      RAISE prepare_src_mergefacts_exc;
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::prepare_src_mergefacts',
   	'Done mapping all ids unless they are CUIs',work_id,0,60);

   -- load data into mom_candidate_facts
   msp_location := '300';
   MEME_SYSTEM.truncate('mom_candidate_facts');

   msp_location := '400';
   INSERT INTO mom_candidate_facts
    (atom_id_1, atom_id_2, code1, code2, termgroup1, termgroup2,
     source1, source2, merge_level, status, lui_1, lui_2, isui_1,
     isui_2, sui_1, sui_2, merge_set, source)
   SELECT atom_id_1, atom_id_2, '', '', '', '', '', '',
     merge_level, status, '', '', '', '', '', '', merge_set, source
   FROM mom_precomputed_facts WHERE status = 'R';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::prepare_src_mergefacts',
   	'Done load data into mom_candidate_facts',work_id,0,80);

   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Merging',
	detail => 'Prepare mergefacts.src set  ('||merge_set||')',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed. ');
   --   SQL%ROWCOUNT||' facts loaded.');  (not helpful because all merge sets are loaded)

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::prepare_src_mergefacts',
   	'Done process prepare_src_mergefacts',work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN prepare_src_mergefacts_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END prepare_src_mergefacts;

/* PROCEDURE SETUP_PARTITIONS **************************************************
 */
PROCEDURE setup_partitions
IS
BEGIN
   initialize_trace('SETUP_PARTITIONS');

EXCEPTION
   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END setup_partitions;

/* PROCEDURE INSERT_RANKS ******************************************************
 */
PROCEDURE insert_ranks(
   authority IN VARCHAR2,
   work_id   IN INTEGER,
   set_ranks IN VARCHAR2
)
IS
   tg_changed		INTEGER := 0;

   ir_rank		INTEGER :=0;
   ir_release_rank	INTEGER :=0;
   high_termgroup_count INTEGER :=0;
   low_termgroup_count	INTEGER :=0;
   duplicate_rank	INTEGER :=0;
   termgroup_rank_count INTEGER :=0;
   source_rank_count	INTEGER :=0;
   insert_counter	INTEGER :=0;
   ir_exception 	EXCEPTION;
   l_start	INTEGER;
   l_end	INTEGER;

   CURSOR ir_strcur IS 
   SELECT a.*, b.stripped_source||'/'||a.tty as high_root_termgroup, 
	NVL(c.stripped_source,b.stripped_source)||'/'||
	  substr(low_termgroup,instr(low_termgroup,'/')+1) as low_root_termgroup
   FROM source_termgroup_rank a, source_source_rank b, source_rank c
   WHERE substr(a.high_termgroup,1,instr(a.high_termgroup,'/')-1) = b.high_source
     AND substr(a.low_termgroup,1,instr(a.low_termgroup,'/')-1) = c.source (+)
     AND a.high_termgroup NOT IN (SELECT termgroup FROM termgroup_rank)
   UNION
   SELECT a.*, b.stripped_source||'/'||a.tty as high_root_termgroup, 
	NVL(c.stripped_source,b.stripped_source)||'/'||
	  substr(low_termgroup,instr(low_termgroup,'/')+1) as low_root_termgroup
   FROM source_termgroup_rank a, source_rank b, source_rank c
   WHERE substr(a.high_termgroup,1,instr(a.high_termgroup,'/')-1) = b.source
     AND substr(a.low_termgroup,1,instr(a.low_termgroup,'/')-1) = c.source (+)
     AND a.high_termgroup NOT IN (SELECT termgroup FROM termgroup_rank);

   ir_strec ir_strcur%ROWTYPE;

   CURSOR ir_ssrcur IS 
   SELECT a.*, NVL(b.stripped_source,a.stripped_source) low_root_source
   FROM source_source_rank a, source_rank b
   WHERE a.low_source = b.source(+)
     AND a.high_source NOT IN (SELECT source FROM source_rank);

   ir_ssrrec ir_ssrcur%ROWTYPE;

BEGIN

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::insert_ranks',
    	'Starting insert_ranks',0,work_id,0,1);

   initialize_trace('INSERT_RANKS');

   MEME_UTILITY.put_message('Begin insert termgroups.');
   l_start := DBMS_UTILITY.get_time;

   msp_location := '10';
   UPDATE source_termgroup_rank   
   SET tty = SUBSTR(high_termgroup,INSTR(high_termgroup,'/')+1)
   WHERE tty IS NULL;

   LOOP

      -- Find out how many termgroups there are left
      -- to assign, and exit if the count is zero
      msp_location := '5';
      termgroup_rank_count := MEME_UTILITY.exec_select (
	 'SELECT count(*) FROM
	  (SELECT high_termgroup FROM source_termgroup_rank
	   MINUS 
	   SELECT termgroup FROM termgroup_rank) ' );

      EXIT WHEN termgroup_rank_count = 0;

      insert_counter := 0;

      OPEN ir_strcur;
      LOOP

	 -- select * from source_termgroup_rank
	 FETCH ir_strcur INTO ir_strec;
	 EXIT WHEN ir_strcur%NOTFOUND;

	 -- Determine if this termgroup has been assigned
	 msp_location := '10';
	 high_termgroup_count := MEME_UTILITY.exec_select(
	    'SELECT COUNT(*) FROM termgroup_rank 
	     WHERE termgroup = ''' || ir_strec.high_termgroup || ''' ');

	 -- Determine if the low termgroup has been assigned
	 msp_location := '10.2';
	 low_termgroup_count := MEME_UTILITY.exec_select(
	    'SELECT COUNT(*) FROM termgroup_rank
	     WHERE termgroup = ''' || ir_strec.low_termgroup || ''' ');

	 -- high termgroup does not exist, low one does => insert
	 IF high_termgroup_count = 0 AND low_termgroup_count = 1 THEN

	    msp_location := '11';

	    --
	    -- Get the rank of the current low_termgroup
	    --
	    ir_rank := MEME_UTILITY.exec_select(
	       'SELECT NVL(MAX(rank),1) FROM termgroup_rank
	        WHERE termgroup = ''' || ir_strec.low_termgroup || ''' ');

	    ir_release_rank := MEME_UTILITY.exec_select(
	       'SELECT NVL(MAX(release_rank),1) FROM termgroup_rank
	        WHERE termgroup = ''' || ir_strec.low_termgroup || ''' ');

	    --
	    -- If the high termgroup is just the newer version of the low
	    -- termgroup, then use the same rank, otherwise increment it by 1
	    --
	    IF ir_strec.low_root_termgroup != ir_strec.high_root_termgroup THEN

		ir_rank := ir_rank + 1;

	    	--
	    	-- Check if this rank value exists already
	    	--
	    	duplicate_rank :=  MEME_UTILITY.exec_select(
	    	   'SELECT count(*) FROM termgroup_rank
	    	    WHERE rank = ' || ir_rank );

	    	-- If the rank does exist, increment all ranks
	    	-- equal to or higher than the candidate rank
	    	IF duplicate_rank > 0 THEN
	    	     local_exec (
			  'UPDATE termgroup_rank 
			   SET rank = rank + 1
			   WHERE rank >= ' || ir_rank );
	        END IF;


		ir_release_rank := ir_release_rank + 1;

	    	--
	    	-- Check if this release rank value exists already
	    	--
	    	duplicate_rank :=  MEME_UTILITY.exec_select(
	    	   'SELECT count(*) FROM termgroup_rank
	    	    WHERE release_rank = ' || ir_release_rank );

	    	-- If the release rank does exist, increment all release ranks
	    	-- equal to or higher than the candidate release rank
	    	IF duplicate_rank > 0 THEN
	    	     local_exec (
			  'UPDATE termgroup_rank 
			   SET release_rank = release_rank + 1
			   WHERE release_rank >= ' || ir_release_rank );
	        END IF;

	    END IF;


	    -- Insert the high_termgroup
	    msp_location := '20';
	    local_exec (
	       'INSERT INTO termgroup_rank
	         (termgroup, rank, release_rank, notes, suppressible, tty ) 
	        VALUES (''' || ir_strec.high_termgroup || ''', 
			' || ir_rank || ',
			' || ir_release_rank || ', '''', 
			''' || ir_strec.suppressible || ''',
			''' || ir_strec.tty || ''') ');

	    insert_counter := insert_counter + 1;

	    msp_location := '30';
	    IF ir_strec.exclude = 'Y' THEN
	       local_exec (
		  'INSERT INTO mom_exclude_list (termgroup)
		   VALUES (''' || ir_strec.high_termgroup || ''')');
	    END IF;

	    msp_location := '40';
	    IF ir_strec.norm_exclude = 'Y' THEN
	       local_exec (
		  'INSERT INTO mom_norm_exclude_list (termgroup)
		   VALUES (''' || ir_strec.high_termgroup || ''')');
	    END IF;

	 END IF; -- high = 0, low = 1

      END LOOP;
      CLOSE ir_strcur;

      IF insert_counter = 0 THEN
	 msp_error_code := 45;
	 msp_error_detail := 'No rows inserted.';
	 RAISE ir_exception;
      END IF;

   END LOOP;

    --
    -- Set ranks in classes for mapping data
    --
    IF insert_counter > 0 AND set_ranks = 'Y' THEN
	msp_location := '45';
   	MEME_RANKS.set_ranks(
    	    classes_flag => 'Y', 
	    attributes_flag => 'N', 
	    relationships_flag => 'N');
    END IF;

   MEME_UTILITY.put_message('End insert termgroups.');
   l_end := DBMS_UTILITY.get_time;
   MEME_UTILITY.log_operation(
       authority => authority,
       activity => 'Load new sources/termgroups',
       detail => 'Insert termgroups',
       work_id => work_id,
       transaction_id => 0,
       elapsed_time => (l_end - l_start) * 10);

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::insert_ranks',
    	'Done with termgroups',0,work_id,0,50);

   -- Assign Source Ranks
   MEME_UTILITY.put_message('Begin insert sources.');
   l_start := DBMS_UTILITY.get_time;
   LOOP

      -- Determine how many sources still need
      -- to be inserted, exit when count is zero
      msp_location := '50';
      source_rank_count := MEME_UTILITY.exec_select(
	 'SELECT COUNT(*) FROM
	  (SELECT high_source FROM source_source_rank
	   MINUS
	   SELECT source FROM source_rank) ' );

      EXIT WHEN source_rank_count = 0;

      insert_counter := 0;

      msp_location := '52';
      OPEN ir_ssrcur;
      LOOP

	 -- select * from source_source_rank
	 FETCH ir_ssrcur INTO ir_ssrrec;
	 EXIT WHEN ir_ssrcur%NOTFOUND;

	 -- If the high_source matches any high_termgroups in
	 -- source_termgroup_rank, convert low_termgroups for those
	 -- rows to sources and find the highest ranking one.
	 -- This is for   SOURCE1/PT|SOURCE2/PT
 	 --               SOURCE1/HT|SOURCE3/PT
	 -- It would make SOURCE1 outrank the highest of SOURCE2 and SOURCE3.
	 msp_location := '53';
	 ir_rank := MEME_UTILITY.exec_select (
	    'SELECT NVL(MAX(a.rank),1)
	     FROM source_rank a, source_termgroup_rank b
	     WHERE SUBSTR(b.high_termgroup,1,INSTR(b.high_termgroup,''/'')-1)
	      		= ''' || ir_ssrrec.high_source || ''' 
	       AND a.source =  SUBSTR(b.low_termgroup,1,INSTR(b.low_termgroup,''/'')-1) ');

	 --
	 -- if not, then just use low_source
	 --
	 msp_location := '54';
	 IF ir_rank = 1 THEN
	    ir_rank := MEME_UTILITY.exec_select (
	       'SELECT NVL(MAX(a.rank),1) 
	        FROM source_rank a
	        WHERE source = ''' || ir_ssrrec.low_source || ''' ');
	 END IF;

	 --
	 -- If the high termgroup is just the newer version of the low
	 -- termgroup, then use the same rank, otherwise increment it by 1
	 --
	 IF ir_ssrrec.low_root_source != ir_ssrrec.stripped_source THEN
		
		ir_rank := ir_rank + 1;

	 	-- Check to see if candidate rank is already in
	 	-- source rank.  
	 	duplicate_rank := MEME_UTILITY.exec_select (
	 	   'SELECT count(*) FROM source_rank
	 	    WHERE rank = ' || ir_rank );

	 	-- If the rank does exist, increment all ranks
	 	-- equal to or higher than the candidate rank
	 	IF duplicate_rank > 0 THEN
	 	   msp_location := '60';
	 	   local_exec (
	 	      'UPDATE source_rank SET rank = rank+1
	 	       WHERE rank >= ' || ir_rank );
	 	END IF;

	 END IF;

	 msp_location := '70';
	 INSERT INTO source_rank 
	     (source,rank,restriction_level, normalized_source,
	      stripped_source, notes, version, source_family)
         VALUES (ir_ssrrec.high_source, ir_rank,
	  	 ir_ssrrec.restriction_level, 
	 	 ir_ssrrec.normalized_source,
		 ir_ssrrec.stripped_source, '',
		 ir_ssrrec.version, ir_ssrrec.source_family);

	 IF ir_ssrrec.normalized_source = ir_ssrrec.high_source THEN

	    UPDATE sims_info
	    SET source_official_name = ir_ssrrec.source_official_name,
	     	nlm_contact = ir_ssrrec.nlm_contact,
	     	acquisition_contact = ir_ssrrec.acquisition_contact,
	     	content_contact = ir_ssrrec.content_contact,
	     	license_contact = ir_ssrrec.license_contact,
	     	inverter_contact = ir_ssrrec.inverter_contact,
	     	context_type = ir_ssrrec.context_type,
	     	language = ir_ssrrec.language,
  	     	release_url_list = ir_ssrrec.release_url_list,
	     	valid_start_date = ir_ssrrec.valid_start_date,
	     	valid_end_date = ir_ssrrec.valid_end_date,
	     	citation = ir_ssrrec.citation,
	     	license_info = ir_ssrrec.license_info,
	     	character_set = ir_ssrrec.character_set,
	     	rel_directionality_flag = ir_ssrrec.rel_directionality_flag
   	     WHERE source = ir_ssrrec.high_source;


	     --
	     -- If 0 rows returned, insert a row into sims_info
 	     --
	     IF SQL%ROWCOUNT = 0 THEN

	       INSERT INTO sims_info
	       (source, date_created, meta_year,
		source_official_name, nlm_contact,
		acquisition_contact, content_contact,
		license_contact, inverter_contact,
		context_type, language,
		release_url_list, valid_start_date,
		valid_end_date,	citation, 
		license_info, character_set,
		rel_directionality_flag)
	       VALUES 
	       (ir_ssrrec.high_source, sysdate, -1,
		ir_ssrrec.source_official_name, ir_ssrrec.nlm_contact,
		ir_ssrrec.acquisition_contact, ir_ssrrec.content_contact,
		ir_ssrrec.license_contact, ir_ssrrec.inverter_contact,
		ir_ssrrec.context_type, ir_ssrrec.language,
		ir_ssrrec.release_url_list, ir_ssrrec.valid_start_date,
		ir_ssrrec.valid_end_date, ir_ssrrec.citation,
		ir_ssrrec.license_info, ir_ssrrec.character_set,
		ir_ssrrec.rel_directionality_flag);

	     END IF;
	 END IF;

	 insert_counter := insert_counter + 1;
      END LOOP;
      CLOSE ir_ssrcur;

      IF insert_counter = 0 THEN
	 msp_error_code := 45;
	 msp_error_detail := 'No rows inserted.';
	 RAISE ir_exception;
      END IF;

   END LOOP;
   l_end := DBMS_UTILITY.get_time;
    MEME_UTILITY.log_operation(
        authority => authority,
        activity => 'Load new sources/termgroups',
        detail => 'Insert sources',
        work_id => work_id,
        transaction_id => 0,
        elapsed_time => (l_end - l_start) * 10);

   msp_location := '100';
   msp_error_code := 50;
   msp_error_detail := '';
   MEME_UTILITY.put_message('Update source_version.');
   l_start := DBMS_UTILITY.get_time;

   -- Soma : Removing the dependency on vsab with stripped source.
   -- Delete old row
   msp_location := '110';
   msp_error_code := 1;
   msp_error_detail := 'Error deleting from source_version';
     DELETE FROM source_version WHERE source IN
    (SELECT a.stripped_source
     FROM source_source_rank a, source_rank b
     WHERE a.stripped_source = b.stripped_source
       AND a.low_source = b.source);

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::insert_ranks',
        'Done with source_rank',0,work_id,0,90);

   -- Insert all new sources
   -- This is so complex because of things like MSH2001PA
   -- and the fact that HCPCS and CPT have multiple sources.
   msp_location := '110';
   msp_error_detail := 'Error inserting into source_version';
 --  INSERT INTO source_version (source,current_name,previous_name)
 --  SELECT DISTINCT a.stripped_source, a.normalized_source, b.normalized_source
 --  FROM source_source_rank a, source_rank b
  -- WHERE a.normalized_source like a.stripped_source||'%'
 --  AND b.normalized_source like a.stripped_source||'%'
  -- AND a.low_source = b.source;

   msp_location := '111';
   msp_error_detail :='Error inserting into source_version';
   INSERT INTO source_version (source,current_name,previous_name)
   SELECT DISTINCT a.stripped_source, a.high_source, a.low_source
   FROM source_source_rank a, source_rank b
   WHERE a.stripped_source = b.stripped_source
   AND a.low_source = b.source
   MINUS
   SELECT source, current_name, previous_name FROM source_version;

   msp_location := '115';
   msp_error_detail := 'Error inserting into source_version (new)';
   INSERT INTO source_version (source,current_name,previous_name)
   SELECT DISTINCT a.stripped_source, a.normalized_source, ''
   FROM source_source_rank a
   WHERE stripped_source not in
        (select stripped_source from source_rank b
         where a.normalized_source != b.normalized_source)
   MINUS
   SELECT source, current_name, previous_name FROM source_version;

   msp_location := '118';
   msp_error_detail := 'Error setting source_official_name';
   
    UPDATE sims_info
    SET source_official_name = (SELECT min(pt) FROM 
    (SELECT a.atom_name ab, b.atom_name pt
        FROM atoms a, atoms b, classes c, classes d
        WHERE a.atom_id=c.atom_id AND b.atom_id=d.atom_id
        AND c.source='SRC' AND d.source='SRC'
        AND c.tobereleased ='Y'
        AND d.tobereleased='Y'
        AND c.termgroup='SRC/VAB'
        AND d.termgroup='SRC/VPT'
        AND c.concept_id=d.concept_id
	UNION
	SELECT c.atom_name ab, d.atom_name pt
        FROM source_classes_atoms c, source_classes_atoms d
        WHERE c.source='SRC' AND d.source='SRC'
        AND c.tobereleased ='Y'
        AND d.tobereleased='Y'
        AND c.termgroup='SRC/VAB'
        AND d.termgroup='SRC/VPT'
        AND c.code = d.code
	UNION
	SELECT a.atom_name ab, b.atom_name pt
        FROM atoms a, atoms b, classes c, classes d
        WHERE a.atom_id=c.atom_id AND b.atom_id=d.atom_id
        AND c.source='SRC' AND d.source='SRC'
        AND c.tobereleased ='Y'
        AND d.tobereleased='Y'
        AND c.termgroup='SRC/RAB'
        AND d.termgroup='SRC/RPT'
        AND c.concept_id=d.concept_id
    AND a.atom_name IN ('SRC','MTH','NLM-MED')) tmp_tf
    WHERE source=ab)
    WHERE source IN
        (SELECT high_source from source_source_rank);
    
   msp_location := '119';
   msp_error_detail := 'Error setting source_short_name';
       
    UPDATE sims_info
    SET source_short_name = (SELECT min(pt) FROM
    (SELECT
      (SELECT current_name FROM source_version
       WHERE source=a.atom_name) ab, b.atom_name pt
    FROM atoms a, atoms b, classes c, classes d
    WHERE a.atom_id=c.atom_id AND b.atom_id=d.atom_id
    AND c.source='SRC' AND d.source='SRC'
    AND c.tobereleased ='Y'
    AND d.tobereleased='Y'
    AND c.termgroup='SRC/RAB'
    AND d.termgroup='SRC/SSN'
    AND c.concept_id=d.concept_id
	UNION
	SELECT
      (SELECT current_name FROM source_version
       WHERE source=c.atom_name) ab, d.atom_name pt
    FROM source_classes_atoms c, source_classes_atoms d
    WHERE c.source='SRC' AND d.source='SRC'
    AND c.tobereleased ='Y'
    AND d.tobereleased='Y'
    AND c.termgroup='SRC/RAB'
    AND d.termgroup='SRC/SSN'
    AND c.code = d.code
	UNION
	SELECT a.atom_name ab, b.atom_name pt
    FROM atoms a, atoms b, classes c, classes d
    WHERE a.atom_id=c.atom_id AND b.atom_id=d.atom_id
    AND c.source='SRC' AND d.source='SRC'
    AND c.tobereleased ='Y'
    AND d.tobereleased='Y'
    AND c.termgroup='SRC/RAB'
    AND d.termgroup='SRC/SSN'
    AND c.concept_id=d.concept_id
    AND a.atom_name IN ('SRC','MTH','NLM-MED')) tmp_sn
    WHERE source=ab)
    WHERE source IN (SELECT high_source from source_source_rank);
    
   l_end := DBMS_UTILITY.get_time;
   MEME_UTILITY.log_operation(
       authority => authority,
       activity => 'Load new sources/termgroups',
       detail => 'Update source_version',
       work_id => work_id,
       transaction_id => 0,
       elapsed_time => (l_end - l_start) * 10);

   -- Inform MRD 
   msp_location := '120';
   msp_error_code := 50;
   msp_error_detail := '';
   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.reset_progress(work_id => work_id);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::insert_ranks',
    	'Done process insert_ranks',0,work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN ir_exception THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END insert_ranks;

/* PROCEDURE UNDO_INSERT_RANKS **********************************************
 * This procedure removes rows from termgroup_rank, source_rank
 */
PROCEDURE undo_insert_ranks(
   authority IN VARCHAR2,
   work_id   IN INTEGER
)
IS
   ct	      INTEGER;
BEGIN

   initialize_trace('UNDO_INSERT_RANKS');
 
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::undo_insert_ranks',
    	'Starting undo_insert_ranks',0,work_id,0,1);

   -- Delete termgroups
   msp_location := '120';
   msp_error_code := 1;
   msp_error_detail := 'Error deleting from termgroup_rank';
   DELETE FROM termgroup_rank WHERE termgroup IN
    (SELECT high_termgroup FROM source_termgroup_rank);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::undo_insert_ranks',
    	'Done with termgroups',0,work_id,0,50);

   -- Delete sources
   msp_location := '120';
   msp_error_code := 1;
   msp_error_detail := 'Error deleting from source_rank';
   DELETE FROM source_rank WHERE source IN
    (SELECT high_source FROM source_source_rank);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::undo_insert_ranks',
    	'Done with sources',0,work_id,0,99);

   MEME_UTILITY.reset_progress(work_id => work_id);

   COMMIT;


EXCEPTION

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

END undo_insert_ranks;

/* PROCEDURE REPORT_TABLES *****************************************************
 * This procedure generates the tables:
 *    <source>_demotions
 *    <source>_merges
 *    <source>_need_review
 *    <source>_replaced
 *    <source>_sample
 */
PROCEDURE report_tables(
    root_source		IN VARCHAR2,
    authority 		IN VARCHAR2,
    work_id    		IN INTEGER := 0,
    threshold		IN INTEGER := 6
) IS
    ct			INTEGER;
    l_source		VARCHAR2(100);
    source		VARCHAR2(100);

BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Starting report_tables',0,work_id,0,1);

    initialize_trace('REPORT_TABLES');

    l_source := report_tables.root_source;
   
    --
    -- Strip dash (-) and dot (.) characters from table name base
    --
    l_source := REPLACE(l_source,'-');
    l_source := REPLACE(l_source,'.');

    msp_location := '100';
    SELECT current_name INTO source 
    FROM source_version 
    WHERE source = report_tables.root_source;
    
    if length(l_source) > 18 then
	MEME_UTILITY.put_message('Source name too long: ' || l_source);
	RETURN;
    end if;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done stripping dash and dot characters from table name base',0,work_id,0,5);

    msp_location := '100.1';
    -- Generate temporary table <source>_atoms.
    MEME_UTILITY.drop_it('table',l_source||'_atoms');
    local_exec (
	'CREATE TABLE ' || l_source || '_atoms NOLOGGING AS 
	 SELECT /*+ PARALLEL(a) */ atom_id FROM classes a
	 WHERE source LIKE ''' || report_tables.source || '%''
	 AND tobereleased in (''Y'',''y'')'
    );

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done generating temporary table <source>_atoms',0,work_id,0,10);

    msp_location := '110';
    -- Generate table <source>_demotions.
    MEME_UTILITY.drop_it('table',l_source||'_demotions');
    local_exec(
	'CREATE TABLE ' || l_source || '_demotions AS
          WITH rels AS (SELECT concept_id_1, concept_id_2, atom_id_1, atom_id_2 FROM relationships
                        WHERE status = ''D'')
	 SELECT concept_id_1 AS concept_id FROM rels
	 WHERE atom_id_1 IN
	    (SELECT * FROM ' || l_source || '_atoms)
	 UNION
	 SELECT concept_id_2 FROM rels
	 WHERE atom_id_1 IN
	    (SELECT * FROM ' || l_source || '_atoms)
	 UNION
	 SELECT concept_id_2 FROM rels
	 WHERE atom_id_2 IN
	    (SELECT * FROM ' || l_source || '_atoms)
	 UNION
	 SELECT concept_id_1 FROM rels
	 WHERE atom_id_2 IN
	    (SELECT * FROM ' || l_source || '_atoms) '
    );

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done generating table <source>_atoms',0,work_id,0,15);

    msp_location := '120';
    -- Generate table <source>_merges.
    MEME_UTILITY.drop_it('table',l_source||'_merges');
    local_exec(
	 'CREATE TABLE  ' || l_source || '_merges AS
	  SELECT DISTINCT concept_id FROM classes
	  WHERE source LIKE ''' || report_tables.source || '%''
	  AND authority LIKE ''ENG-%''
          MINUS SELECT concept_id FROM  ' || l_source || '_demotions'
    );

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done generating table <source>_merges',0,work_id,0,20);

    msp_location := '130';
    -- Generate table <source>_need_review.
    MEME_UTILITY.drop_it('table',l_source||'_need_review');
    local_exec (
	'CREATE TABLE ' || l_source || '_need_review AS
	 SELECT DISTINCT concept_id FROM classes
	 WHERE source LIKE ''' || report_tables.source || '%''
	    AND status = ''N'' AND tobereleased in (''Y'',''y'') 
         MINUS SELECT concept_id FROM ' || l_source || '_demotions
         MINUS SELECT concept_id FROM ' || l_source || '_merges'
    );

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done generating table <source>_need_review',0,work_id,0,30);

    msp_location := '140';
    -- Generate table <source>_replaced.
    MEME_UTILITY.drop_it('table',l_source||'_replaced');
    local_exec (
	'CREATE TABLE ' || l_source || '_replaced AS
	 SELECT DISTINCT concept_id FROM classes
	 WHERE source LIKE ''' || report_tables.source || '%''
	    AND status != ''N'' AND tobereleased in (''Y'',''y'')
         MINUS SELECT concept_id FROM ' || l_source || '_demotions
         MINUS SELECT concept_id FROM ' || l_source || '_merges
         MINUS SELECT concept_id FROM ' || l_source || '_need_review'
    );

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done generating table <source>_replaced',0,work_id,0,45);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done removing records that already processed in source_need_review',0,work_id,0,60);

    -- Sample each merge set
    --  By source
    --  By D/M
    --  By status
    --  By Chemical/non-Chemical

    msp_location := '148';
    -- Generate table <source>_sample.
    MEME_UTILITY.drop_it('table',l_source||'_sample');
    EXECUTE IMMEDIATE 
	 'CREATE TABLE ' || l_source || '_sample
	  (concept_id NUMBER(12), reason VARCHAR2(1000))';    

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done generating table <source>_sample',0,work_id,0,65);

    msp_location := '148.11';
    -- Root SRC concept
    EXECUTE IMMEDIATE
        'INSERT into ' || l_source || '_sample
         SELECT concept_id, :reason FROM 
           (SELECT DISTINCT concept_id FROM classes
            WHERE code=''V-'' || :root_source
              AND tty=''RAB''
              AND source=''SRC'')
         WHERE rownum < :threshold'
    USING 'ROOT SRC CONCEPT', root_source, threshold;    

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done root SRC concept',0,work_id,0,70);

    msp_location := '148.12';
    -- Versioned SRC concept           
    EXECUTE IMMEDIATE
        'INSERT into ' || l_source || '_sample
         SELECT concept_id, :reason FROM
           (SELECT DISTINCT concept_id FROM classes
            WHERE code=''V-'' || :source                   
              AND tty=''VAB''
              AND source=''SRC'')     
         WHERE rownum < :threshold'
    USING 'VERSIONED SRC CONCEPT', source, threshold;
  
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done versioned SRC concept',0,work_id,0,75);

    msp_location := '148.15';
    -- Sample each source by TTY.
    EXECUTE IMMEDIATE
    	 'INSERT into ' || l_source || '_sample
	  SELECT distinct concept_id,''TERM TYPE '' ||tty
          FROM (
              SELECT a.concept_id, a.tty,
                row_number() over (partition by a.termgroup order by 1) rn
              FROM classes a
              WHERE a.source = :source
          )
          WHERE rn < :threshold'
    USING source, threshold;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done sample each source by TTY',0,work_id,0,80);

    msp_location := '148.3';
    EXECUTE IMMEDIATE
        'INSERT into ' || l_source || '_sample
         SELECT DISTINCT concept_id, merge_set || '', STATUS '' || cs ||
	     '', MERGE STATUS '' || ms ||
             '', IS_CHEM '' || is_chem reason
    	 FROM (
           SELECT 
                  a.concept_id, merge_set, a.status cs,
                  b.status ms, is_chem,
                  row_number() over
             (PARTITION BY merge_set, a.status, b.status, is_chem
              ORDER BY dbms_random.value) rn
           FROM (SELECT DISTINCT a.concept_id,
                        DECODE((SELECT count(*) FROM semantic_types 
                                WHERE b.attribute_value = semantic_type),0,''N'',''Y'') is_chem,
                        a.status, a.atom_id
                 FROM classes a, attributes b
                 WHERE b.attribute_name = ''SEMANTIC_TYPE''
                   AND a.concept_id = b.concept_id
                   AND a.source = :source
                   AND a.status in (''R'',''N'') ) a, mom_facts_processed b
           WHERE a.atom_id = b.atom_id_1
             AND b.authority = :source
             AND b.merge_set like :root_source
             AND b.status in (''D'',''M'') )
         WHERE rn < :threshold'
    USING source, source, root_source||'%', threshold;
    
    msp_location := '148.5';
    --ambiguous SUI (by source)
    EXECUTE IMMEDIATE
        'INSERT into ' || l_source || '_sample
    	 SELECT concept_id, :reason FROM
           (SELECT DISTINCT concept_id FROM classes
            WHERE isui IN (SELECT isui FROM ambig_isui)
              AND source like :source)
         WHERE rownum < :threshold'
    USING 'AMBIGUOUS', report_tables.source||'%', report_tables.threshold;
    
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done ambiguous SUI (by source)',0,work_id,0,85);
    	
    msp_location := '148.7';
    -- Generate table <source>_nosty
    MEME_UTILITY.drop_it('table',l_source||'_nosty');
    EXECUTE IMMEDIATE 
	 'CREATE TABLE ' || l_source || '_nosty
	  (concept_id NUMBER(12))';    


    msp_location := '148.71';
    --new concepts without STY
    EXECUTE IMMEDIATE
        'INSERT into ' || l_source || '_nosty
    	 SELECT concept_id 
               FROM classes a
               WHERE atom_id IN
	             (SELECT * FROM ' || l_source || '_atoms)
         MINUS 
         SELECT /*+ X_ATTR_AN*/ concept_id 
                     FROM attributes x 
                    WHERE attribute_name = ''SEMANTIC_TYPE''';
                    
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done generating table <source>_nosty',0,work_id,0,65);                    
                                
    	
    msp_location := '148.8';
    -- Generate table <source>_bad_merge.
    MEME_UTILITY.drop_it('table',l_source||'_bad_merge');
    EXECUTE IMMEDIATE 
	 'CREATE TABLE ' || l_source || '_bad_merge
	  (concept_id NUMBER(12))';    

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done generating table <source>_bad_merge',0,work_id,0,65);
    	

    msp_location := '148.81';
    --concepts with multiple CUIs
    EXECUTE IMMEDIATE
        'INSERT into ' || l_source || '_bad_merge
    	 SELECT concept_id
    FROM (SELECT concept_id, last_release_cui
            FROM classes a
           WHERE     EXISTS
                        (SELECT 1
                           FROM classes b
                          WHERE     a.concept_id = b.concept_id
                                AND b.authority = :authority
                                AND last_release_cui IS NOT NULL)
                 AND last_release_cui IS NOT NULL)
GROUP BY concept_id
  HAVING COUNT (DISTINCT last_release_cui) > 1'
                                USING authority;                                
    	
    	
    msp_location := '150';
    -- Drop temporary table <source>_atoms.
    MEME_UTILITY.drop_it('table', l_source || '_atoms');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done dropping temporary table <source>_atoms',0,work_id,0,90);

    msp_location := '160';
    -- Count the total number of rows inserted in all generated tables.
    ct := MEME_UTILITY.exec_select
	('SELECT COUNT(*) FROM ' || l_source || '_demotions');
    MEME_UTILITY.put_message
	(ct||' rows inserted in table ' || l_source || '_demotions');
    ct := MEME_UTILITY.exec_select
	('SELECT COUNT(*) FROM ' || l_source || '_merges');
    MEME_UTILITY.put_message
	(ct||' rows inserted in table ' || l_source || '_merges');
    ct := MEME_UTILITY.exec_select
	('SELECT COUNT(*) FROM ' || l_source || '_need_review');
    MEME_UTILITY.put_message
	(ct||' rows inserted in table ' || l_source || '_need_review');
    ct := MEME_UTILITY.exec_select
	('SELECT COUNT(*) FROM ' || l_source || '_replaced');
    MEME_UTILITY.put_message
	(ct||' rows inserted in table ' || l_source || '_replaced');
    ct := MEME_UTILITY.exec_select
	('SELECT COUNT(*) FROM ' || l_source || '_sample');
    MEME_UTILITY.put_message
	(ct||' rows inserted in table ' || l_source || '_sample');
    ct := MEME_UTILITY.exec_select
	('SELECT COUNT(*) FROM ' || l_source || '_nosty');
    MEME_UTILITY.put_message
	(ct||' rows inserted in table ' || l_source || '_nosty');
    ct := MEME_UTILITY.exec_select
	('SELECT COUNT(*) FROM ' || l_source || '_bad_merge');
    MEME_UTILITY.put_message
	(ct||' rows inserted in table ' || l_source || '_bad_merge');
    MEME_UTILITY.put_message(msp_method ||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::report_tables',
    	'Done processed report_tables',0,work_id,0,100);

    COMMIT;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
	return;    
    WHEN OTHERS THEN
	error_log(msp_method,msp_location,msp_error_code,SQLERRM);
	RAISE msp_exception;

END report_tables;

/* PROCEDURE UPDATE_RELEASABILITY ******************************************
 * This procedure makes an obsolete source (and everything connected
 * to it) unreleasable.  However, for SG data that is not connected
 * to this source, we should not disable it (CUI rels for example)
 * because map_sg_data will take care of it.
 */
PROCEDURE update_releasability(
   old_source IN VARCHAR2,
   new_source IN VARCHAR2,
   authority  IN VARCHAR2,
   new_value  IN VARCHAR2,
   work_id    IN INTEGER
)
IS
   tmp_table_1	VARCHAR2(50);
   tmp_table_2	VARCHAR2(50);
   tmp_table_3	VARCHAR2(50);
   retval	INTEGER;
   update_releasability_exc EXCEPTION;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Starting update_releasability',0,work_id,0,1);

   MEME_UTILITY.sub_timing_start;

   initialize_trace('UPDATE_RELEASABILITY');

   msp_location := '100';
   retval := 0;
   --
   -- Find all old version atoms
   --
   msp_location := '110.1';
   tmp_table_1 := MEME_UTILITY.get_unique_tablename;
   msp_location := '110.2';
   MEME_UTILITY.drop_it('table',tmp_table_1);
   msp_location := '110.3';
   EXECUTE IMMEDIATE
      'CREATE TABLE ' || tmp_table_1 || ' NOLOGGING AS
       SELECT /*+ PARALLEL(a) */ DISTINCT atom_id AS row_id
       FROM classes a
       WHERE source = ''' || old_source || '''';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done finding all old version atoms',0,work_id,0,10);

   --
   -- Find old-version SRC atoms also!
   --
   msp_location := '110.4';
   EXECUTE IMMEDIATE
 	'INSERT INTO ' || tmp_table_1 || '
	 SELECT atom_id FROM classes WHERE source=''SRC''
	 AND tobereleased != ''' || new_value || '''
	 AND concept_id IN
	   (SELECT concept_id FROM classes a, atoms b
	    WHERE a.atom_id = b.atom_id
	      AND a.source = ''SRC'' AND termgroup = ''SRC/VAB''
	      AND tobereleased != ''' || new_value || '''
	      AND atom_name = ''' || old_source || ''') ';

   msp_location := '110.5';
   EXECUTE IMMEDIATE
       'CREATE UNIQUE INDEX x_' || tmp_table_1 || ' on  ' || tmp_table_1 || ' (row_id) TABLESPACE MIDI
	COMPUTE STATISTICS PARALLEL NOLOGGING';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done old-version SRC atoms',0,work_id,0,20);
	
	-- Soma Changing for 10g performance
   --BAC Removed MEME_SYSTEM.analyze(tmp_table_1);

   --
   -- Find attributes attached to old version atoms
   -- Plus attributes with old source name
   --
   msp_location := '120.1';
   tmp_table_2 := MEME_UTILITY.get_unique_tablename;
   msp_location := '120.2';
   MEME_UTILITY.drop_it('table',tmp_table_2);
   msp_location := '120.3';
   EXECUTE IMMEDIATE
      'CREATE TABLE ' || tmp_table_2 || ' NOLOGGING AS
       SELECT /*+ PARALLEL(a) */ attribute_id AS row_id
       FROM attributes a
       WHERE source != ''' || old_source || '''
         AND atom_id IN
           (SELECT row_id FROM ' || tmp_table_1 || ')
         AND attribute_level = ''S''
         AND tobereleased != ''' || new_value || '''
         AND ((nvl(sg_type,''null'') NOT IN 
                (SELECT code FROM code_map WHERE type=''map_sg_type'')) 
         	  OR source = ''SRC'')
      UNION
       SELECT /*+ PARALLEL(a) */ attribute_id AS row_id
       FROM attributes a
       WHERE source = ''' || old_source || '''
         AND attribute_level = ''S''
         AND tobereleased != ''' || new_value || ''' ';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done finding attributes attached to old version atoms',0,work_id,0,30);
	-- Soma Changing for 10g Performance
   -- BAC removed: MEME_SYSTEM.analyze(tmp_table_2);

    	--
   -- Find all relationships connected to old atoms
   -- Plus relationships with old source
   --
   msp_location := '130.1';
   tmp_table_3 := MEME_UTILITY.get_unique_tablename;
   msp_location := '130.2';
   MEME_UTILITY.drop_it('table',tmp_table_3);
   msp_location := '130.3';
   EXECUTE IMMEDIATE
      'CREATE TABLE ' || tmp_table_3 || ' NOLOGGING AS 
       SELECT /*+ PARALLEL(r) */ relationship_id AS row_id
       FROM relationships r
       WHERE source != ''' || old_source || '''
         AND atom_id_1 IN
         (SELECT row_id FROM ' || tmp_table_1 || ')
         AND relationship_level = ''S''
         AND tobereleased != ''' || new_value || '''
         AND ((nvl(sg_type_1,''null'') NOT IN
                (SELECT code FROM code_map WHERE type=''map_sg_type'')) 
         	  OR source = ''SRC'')
       UNION
       SELECT /*+ PARALLEL(r) */ relationship_id 
       FROM relationships r
       WHERE source != ''' || old_source || '''
         AND atom_id_2 IN
           (SELECT row_id FROM ' || tmp_table_1 || ')
         AND relationship_level = ''S''
         AND tobereleased != ''' || new_value || '''
         AND ((nvl(sg_type_2,''null'') NOT IN
                (SELECT code FROM code_map WHERE type=''map_sg_type'')) 
         	  OR source = ''SRC'')
       UNION
       SELECT /*+ PARALLEL(a) */ relationship_id
       FROM relationships a
       WHERE source = ''' || old_source || '''
         AND relationship_level = ''S''
         AND tobereleased != ''' || new_value || ''' ';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done finding all relationships connected to old atoms',0,work_id,0,40);
	-- Soma Changing for 10g Performance
    -- BAC removed: MEME_SYSTEM.analyze(tmp_table_3);
   MEME_UTILITY.put_message('Done finding data to update');

    	
    --
    -- Change releasability of old atoms
    --
    msp_location := '140.1';
    EXECUTE IMMEDIATE
       'UPDATE /*+ PARALLEL(c) */ classes c SET tobereleased = ''' || new_value || '''
        WHERE atom_id IN (SELECT row_id FROM ' || tmp_table_1 || ')';
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' atoms updated');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done change releasability of old atoms',0,work_id,0,50);

    --
    -- Change releasability of old attributes
    --
    msp_location := '145.1';
    EXECUTE IMMEDIATE
       'UPDATE /*+ PARALLEL(a) */ attributes a SET tobereleased = ''' || new_value || '''
        WHERE attribute_id IN (SELECT row_id FROM ' || tmp_table_2 || ')';
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' attributes updated');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done change releasability of old attributes',0,work_id,0,60);

    --
    -- Change releasability of old relationships
    --
    msp_location := '150.1';
    EXECUTE IMMEDIATE
       'UPDATE /*+ PARALLEL(r) */ relationships r SET tobereleased = ''' || new_value || '''
        WHERE relationship_id IN (SELECT row_id FROM ' || tmp_table_3 || ')';
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' relationships updated');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done change releasability of old relationships',0,work_id,0,70);

   --
   -- Find all old context rels
   -- 
   msp_location := '160.1';
   MEME_UTILITY.drop_it('table',tmp_table_1);
   msp_location := '160.2';
   local_exec
      ('CREATE TABLE ' || tmp_table_1 || ' NOLOGGING AS
        SELECT /*+ PARALLEL(a) */ relationship_id AS row_id
        FROM context_relationships a
        WHERE source = ''' || old_source || '''
          AND tobereleased != ''' || new_value || '''');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done all old context rels',0,work_id,0,80);
	-- Soma Changing for 10g performance
   -- BAC Removed: MEME_SYSTEM.analyze(tmp_table_1);
   -- 
   -- Change releasability of old version context relationships
   -- 
   msp_location := '170.1';
    EXECUTE IMMEDIATE
       'UPDATE /*+ PARALLEL(r) */ context_relationships r SET tobereleased = ''' || new_value || '''
        WHERE relationship_id IN (SELECT row_id FROM ' || tmp_table_1 || ')';
   MEME_UTILITY.put_message(SQL%ROWCOUNT || ' cxt relationships updated');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done change releasability of old version context relationships',0,work_id,0,90);

   MEME_UTILITY.drop_it('table',tmp_table_1);
   MEME_UTILITY.drop_it('table',tmp_table_2);
   MEME_UTILITY.drop_it('table',tmp_table_3);

   MEME_UTILITY.sub_timing_stop;
    MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Update releasability',
	detail => 'Done setting releasability of source ' || old_source,
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::update_releasability',
    	'Done process update_releasability',0,work_id,0,100);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');
   COMMIT;

EXCEPTION
   WHEN update_releasability_exc THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_2);
      MEME_UTILITY.drop_it('table',tmp_table_3);
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_2);
      MEME_UTILITY.drop_it('table',tmp_table_3);
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END update_releasability;

/* PROCEDURE RESOLVE_STYS ******************************************************
 * This procedure resolves whose STYs are under consideration.
 * Fate of STYs:
 *    W - STYs from <source> win, all others are deleted.
 *    L - STYs from <source> lose and get deleted.
 */
PROCEDURE resolve_stys(
   source    IN VARCHAR2,
   sty_fate  IN VARCHAR2,
   authority IN VARCHAR2,
   work_id   IN INTEGER
)
IS
   tmp_table_1	 VARCHAR2(50);
   tmp_table_2	 VARCHAR2(50);
   tmp_table_3	 VARCHAR2(50);
   e_source	 VARCHAR2(50);
   l_source	 VARCHAR2(50);
   qry_condition VARCHAR2(10);
   retval	 INTEGER;
   resolve_stys_exc EXCEPTION;

BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::resolve_stys',
    	'Starting resolve_stys',0,work_id,0,1);

   MEME_UTILITY.sub_timing_start;

   initialize_trace('RESOLVE_STYS');

   retval := 0;

   --
   -- set query condition
   --
   IF UPPER(sty_fate) = 'W' THEN
      qry_condition := ' not in ';
   ELSIF UPPER(sty_fate) = 'L' THEN
      qry_condition := ' in ';
   ELSE
      msp_error_code := 36;
      msp_error_detail := 'sty_fate='||sty_fate;
      RAISE resolve_stys_exc;
   END IF;

   -- Check E- sab also, restrict to 20 chars
   e_source := 'E-' || source;
   l_source := substr(source,1,20);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::resolve_stys',
    	'Done query condition',0,work_id,0,30);

   --
   -- Get source owned STYs
   --
   msp_location := '100.1';
   tmp_table_1 := MEME_UTILITY.get_unique_tablename;
   msp_location := '100.2';
   MEME_UTILITY.drop_it('table',tmp_table_1);
   msp_location := '100.3';
   EXECUTE IMMEDIATE
       'CREATE TABLE ' || tmp_table_1 || ' NOLOGGING AS
        SELECT concept_id
        FROM attributes a
        WHERE attribute_name = ''SEMANTIC_TYPE''
          AND source in (''' || substr(e_source,1,20) || ''', ''' || l_source || ''', ''' 
            || source || ''', ''' || e_source || ''')';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::resolve_stys',
    	'Done get source owned STYs',0,work_id,0,50);

   -- Get non source owned STYs
   msp_location := '110.1';
   --MEME_SYSTEM.analyze(tmp_table_1);
   msp_location := '110.2';
   tmp_table_2 := MEME_UTILITY.get_unique_tablename;
   msp_location := '160.2';
   MEME_UTILITY.drop_it('table',tmp_table_2);
   msp_location := '160.3';
   EXECUTE IMMEDIATE
       'CREATE TABLE ' || tmp_table_2 || ' NOLOGGING AS
        SELECT concept_id
        FROM attributes a
        WHERE attribute_name = ''SEMANTIC_TYPE''
        AND source not in (''' || substr(e_source,1,20) || ''', ''' || l_source || ''', ''' 
            || source || ''', ''' || e_source || ''')
        AND concept_id IN
            (SELECT concept_id FROM ' || tmp_table_1 || ') ';

   msp_location := '120.1';
   --MEME_SYSTEM.analyze(tmp_table_2);
   msp_location := '120.2';
   tmp_table_3 := MEME_UTILITY.get_unique_tablename;
   msp_location := '120.2';
   MEME_UTILITY.drop_it('table',tmp_table_3);
   msp_location := '120.3';
   EXECUTE IMMEDIATE
       'CREATE TABLE ' || tmp_table_3 || ' NOLOGGING AS
        SELECT DISTINCT attribute_id as row_id
        FROM attributes a, ' || tmp_table_2 || ' b
        WHERE attribute_name = ''SEMANTIC_TYPE''
        AND source ' || qry_condition || ' 
		(''' || substr(e_source,1,20) || ''', ''' || l_source || ''', ''' 
            || source || ''', ''' || e_source || ''')
        AND a.concept_id = b.concept_id ';
   
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::resolve_stys',
    	'Done Get non source source owned STYs',0,work_id,0,70);

   msp_location := '130.1';
   retval := MEME_UTILITY.exec_select('SELECT COUNT(*) FROM '||tmp_table_3);
   -- Continue only if there are rows
   IF retval > 0 THEN
       retval :=
         MEME_BATCH_ACTIONS.macro_action(
	 	action => 'D',
	 	id_type => 'A',
	 	authority => authority,
	 	table_name => tmp_table_3,
	 	work_id => work_id,
	 	status => 'R');
      	IF retval < 0 THEN
   	    msp_location := '130.2';
            msp_error_code := 10;
            msp_error_detail := 'action=D,id_type=A,new_value=Y';
            RAISE resolve_stys_exc;
        END IF;

      MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::resolve_stys',
    	'Done continuing process if there are rows',0,work_id,0,90);

   END IF;

   MEME_UTILITY.drop_it('table',tmp_table_1);
   MEME_UTILITY.drop_it('table',tmp_table_2);
   MEME_UTILITY.drop_it('table',tmp_table_3);

   msp_location := '140';
   MEME_UTILITY.sub_timing_stop;
    MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Resolve semantic types',
	detail => 'Done removing redundant/bad default semantic types',
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::resolve_stys',
    	'Done process resolve_stys',0,work_id,0,100);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');
   COMMIT;

EXCEPTION
   WHEN resolve_stys_exc THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_2);
      MEME_UTILITY.drop_it('table',tmp_table_3);
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_2);
      MEME_UTILITY.drop_it('table',tmp_table_3);
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END resolve_stys;

/* PROCEDURE DELETE_DEMOTIONS **************************************************
 * This procedure delete redundant demotions. Redundant demotions are those
 * that are connected to safe replacement atoms but link concepts already
 * connected by a C level rels.
 */
PROCEDURE delete_demotions(
   source     IN VARCHAR2,
   authority  IN VARCHAR2,
   work_id    IN INTEGER
)
IS
   tmp_table_1	        VARCHAR2(50);
   l_query	        VARCHAR2(2000);
   retval               NUMBER;
   delete_demotions_exc EXCEPTION;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::delete_demotions',
    	'Starting delete_demotions',0,work_id,0,1);

   MEME_UTILITY.sub_timing_start;

   initialize_trace('DELETE_DEMOTIONS');

   msp_location := '100';
   tmp_table_1 := MEME_UTILITY.get_unique_tablename;
   MEME_UTILITY.drop_it('table',tmp_table_1);
   msp_location := '110';
   EXECUTE IMMEDIATE
      'CREATE TABLE ' || tmp_table_1 || ' NOLOGGING AS
       WITH tmpd AS (
               SELECT /*+ PARALLEL(r)*/  relationship_id AS row_id, atom_id_1, atom_id_2,
                  concept_id_1, concept_id_2, last_molecule_id
               FROM relationships r
               WHERE status = ''D'' AND source = ''' || source || '''),
            msp as (SELECT /*+ PARALLEL(a) */ new_atom_id FROM mom_safe_replacement a 
               WHERE source = ''' || source || ''')
       SELECT row_id, concept_id_1, concept_id_2, last_molecule_id
       FROM tmpd a where atom_id_1 in 
           (SELECT * FROM msp)
       UNION
       SELECT row_id, concept_id_1, concept_id_2, last_molecule_id
       FROM tmpd a where atom_id_2 in 
           (SELECT * FROM msp) ';
   MEME_UTILITY.put_message('Done Creating table of demotions');

   --
   -- Keep only rows where concept level rels are present.
   -- Delete cases where there is no matching C level rel
   --
   msp_location := '200';
   EXECUTE IMMEDIATE
      'DELETE FROM ' || tmp_table_1 || ' a
       WHERE (concept_id_1, concept_id_2) NOT IN (
          SELECT concept_id_1, concept_id_2 FROM relationships b
          WHERE relationship_level = ''C'' AND a.concept_id_1 = b.concept_id_1
          UNION ALL
          SELECT concept_id_2, concept_id_1 FROM relationships b
          WHERE relationship_level = ''C'' AND a.concept_id_2 = b.concept_id_1)';

   MEME_UTILITY.put_message('Done removing cases where C level rels do not exist');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::delete_demotions',
    	'Done keeping only rows where concept level rels are present',0,work_id,0,20);

   --
   -- Set concept level relationship status back to R
   --
   msp_location := '300';
   MEME_UTILITY.drop_it('table',tmp_table_1 || '_2');
   EXECUTE IMMEDIATE
      'CREATE TABLE ' || tmp_table_1 || '_2 NOLOGGING AS
       SELECT relationship_id AS row_id, concept_id_1, concept_id_2
       FROM relationships 
       WHERE relationship_level = ''C''
	 AND (concept_id_1,concept_id_2,last_molecule_id) IN
	    (SELECT concept_id_1, concept_id_2,last_molecule_id 
	     FROM ' || tmp_table_1 || ' UNION ALL
	     SELECT concept_id_2, concept_id_1,last_molecule_id 
  	     FROM ' || tmp_table_1 || ')';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::delete_demotions',
    	'Done concept level relationship status back to R',0,work_id,0,40);
   MEME_UTILITY.put_message('Done finding C level rels to convert back to status R');

   -- Delete the remaining rows 
   -- These are relationships which are demotions (status=D) 
   -- which are connected to safe-replacement atoms from the current
   -- source where concept level rels ALREADY exist between the same
   -- pair of concepts.
   msp_location := '400';
   retval := MEME_BATCH_ACTIONS.macro_action(
      action => 'D',
      id_type => 'R',
      table_name => tmp_table_1,
      status => 'R',
      authority => delete_demotions.source,
      work_id => delete_demotions.work_id);

   IF retval < 0 THEN
      msp_error_code := 10;
      msp_error_detail := 'action=D,id_type=R';
      RAISE delete_demotions_exc;
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::delete_demotions',
    	'Done deleting remaining rows',0,work_id,0,60);

   --
   -- Change status of concept level rels back to R
   -- First, remove cases where there is still a demotion!
   --
   msp_location := '500a';
   EXECUTE IMMEDIATE
      'DELETE FROM ' || tmp_table_1 || '_2 a
       WHERE (concept_id_1, concept_id_2) IN
        (SELECT concept_id_1,concept_id_2 FROM relationships  b
         WHERE status=''D'' AND a.concept_id_1 = b.concept_id_1)';
   msp_location := '500a2';
   EXECUTE IMMEDIATE
      'DELETE FROM ' || tmp_table_1 || '_2 a
       WHERE (concept_id_2, concept_id_1) IN
        (SELECT concept_id_1,concept_id_2 FROM relationships  b
         WHERE status=''D'' AND a.concept_id_2 = b.concept_id_1)';

   msp_location := '500b';
   retval := MEME_BATCH_ACTIONS.macro_action(
      action => 'S',
      id_type => 'R',
      table_name => tmp_table_1 || '_2',
      new_value => 'R',
      status => 'R',
      authority => delete_demotions.source,
      work_id => delete_demotions.work_id);

   IF retval < 0 THEN
      msp_error_code := 10;
      msp_error_detail := 'action=D,id_type=R';
      RAISE delete_demotions_exc;
   END IF;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::delete_demotions',
    	'Done changing status of concept level rels back to R',0,work_id,0,80);

   msp_location := '500';
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation
      (authority,'MEME_SOURCE_PROCESSING.delete_demotions',
       'Delete relationships',retval,work_id,MEME_UTILITY.sub_elapsed_time);
 
   MEME_UTILITY.drop_it('table',tmp_table_1);
   MEME_UTILITY.drop_it('table',tmp_table_1 || '_2');

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::delete_demotions',
    	'Done process delete_demotions',0,work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN delete_demotions_exc THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_1 || '_2');
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_1 || '_2');
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END delete_demotions;


/* PROCEDURE FOREIGN_CLASSES_INSERT ***************************************
 */
PROCEDURE foreign_classes_insert( 
   authority  IN VARCHAR2,
   work_id    IN INTEGER
)
IS
   TYPE cur_type IS REF CURSOR;
   cur_var      cur_type;
   concept_id   NUMBER;
   retval       NUMBER;
   rpt_tbl	VARCHAR2(100);
   foreign_classes_insert_exc EXCEPTION;
  
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::foreign_classes_insert',
    	'Starting foreign_classes_insert',0,work_id,0,1);

   MEME_UTILITY.sub_timing_start;

   initialize_trace('FOREIGN_CLASSES_INSERT');

   retval := 0;

   msp_location := '114';
   INSERT INTO foreign_classes
        (atom_id, eng_atom_id, eng_aui, language, version_id,
         source, termgroup, tty, termgroup_rank, code,
         sui, lui, generated_status, last_release_cui,
         dead, status, authority, timestamp, insertion_date,
         concept_id, released, tobereleased, last_molecule_id,
         last_atomic_action_id, sort_key, rank, last_release_rank,
         suppressible, last_assigned_cui, isui, aui,
	 source_aui, source_cui, source_dui)
   SELECT
        a.atom_id, b.atom_id, b.aui, c.language, 0,
        a.source, a.termgroup, a.tty, a.termgroup_rank, a.code,
        a.sui, a.lui, a.generated_status, '', 'N', a.status,
        foreign_classes_insert.authority, sysdate, sysdate,
        b.concept_id, a.released, b.tobereleased, 0,0, a.sort_key,
        MEME_RANKS.get_rank(a.atom_id,MEME_CONSTANTS.TN_SOURCE_CLASSES),
        a.last_release_rank, a.suppressible, 
	a.last_assigned_cui, a.isui, a.aui,
	a.source_aui, a.source_cui, a.source_dui
    FROM source_classes_atoms a, classes b, string_ui c,
        mom_precomputed_facts d
   WHERE a.atom_id = atom_id_1 
     AND b.atom_id = atom_id_2
     AND a.sui = c.sui
   UNION
   SELECT
        a.atom_id, b.atom_id, b.aui, c.language, 0,
        a.source, a.termgroup, a.tty, a.termgroup_rank, a.code,
        a.sui, a.lui, a.generated_status, '', 'N', a.status,
        foreign_classes_insert.authority, sysdate, sysdate,
        b.concept_id, a.released, b.tobereleased, 0,0, a.sort_key,
        MEME_RANKS.get_rank(a.atom_id,MEME_CONSTANTS.TN_SOURCE_CLASSES),
        a.last_release_rank, a.suppressible, 
	a.last_assigned_cui, a.isui, a.aui,
	a.source_aui, a.source_cui, a.source_dui
    FROM source_classes_atoms a, classes b, string_ui c,
        mom_precomputed_facts d
   WHERE a.atom_id = atom_id_2 
     AND b.atom_id = atom_id_1
     AND a.sui = c.sui;

   msp_location := '200';
   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
        authority, 'MEME_SOURCE_PROCESSING.foreign_classes_insert',
       'Foreign classes insert successfully completed.',
	0,work_id,MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::foreign_classes_insert',
    	'Done foreign_classes_insert',0,work_id,0,100);

   --COMMIT

EXCEPTION
   WHEN foreign_classes_insert_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END foreign_classes_insert;


/* PROCEDURE CORE_TABLE_INSERT *********************************************
 */
PROCEDURE core_table_insert(
   table_name IN VARCHAR2,
   authority  IN VARCHAR2,
   work_id    IN INTEGER
)
IS
   TYPE cur_type IS REF CURSOR;
   cur_var	cur_type;
   concept_id 	NUMBER;
   retval 	NUMBER;
   core_table_insert_exc EXCEPTION;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::core_table_insert',
    	'Starting core_table_insert',0,work_id,0,1);

   MEME_UTILITY.sub_timing_start;

   initialize_trace('CORE_TABLE_INSERT');

   retval := 0;

   IF UPPER(table_name) = 'C' OR UPPER(table_name) = 'ALL' THEN

      initialize_trace('CORE_TABLE_INSERT');

      msp_location := '114';
      retval :=
	 MEME_BATCH_ACTIONS.macro_action(
	    action => 'I',
	    id_type => 'C',
	    authority => authority,
	    table_name => table_name,
	    work_id => work_id,
	    status => 'R',
	    set_preferred_flag => MEME_CONSTANTS.NO );

      IF retval < 0 THEN
	 msp_error_code := 11;
	 msp_error_detail := 'action=I,id_type=C';
	 RAISE core_table_insert_exc;
      END IF;

      msp_location := '114.1';
      UPDATE source_classes_atoms SET switch='I';
  
      COMMIT;      

   END IF;

   IF UPPER(table_name) = 'R' OR UPPER(table_name) = 'ALL' THEN

      msp_location := '122';
      retval :=
	 MEME_BATCH_ACTIONS.macro_action(
	    action => 'I',
	    id_type => 'R',
	    authority => authority,
	    table_name => table_name,
	    work_id => work_id,
	    status => 'R');
      IF retval < 0 THEN
	 msp_error_code := 11;
	 msp_error_detail := 'action=I,id_type=R';
	 RAISE core_table_insert_exc;
      END IF;

      msp_location := '122.1';
      UPDATE source_relationships SET switch='I';
  
      COMMIT;      

   END IF;

   IF UPPER(table_name) = 'CR' OR UPPER(table_name) = 'ALL' THEN

      initialize_trace('CORE_TABLE_INSERT');

      msp_location := '124';
      retval :=
	 MEME_BATCH_ACTIONS.macro_action(
	    action => 'I',
	    id_type => 'CR',
	    authority => authority,
	    table_name => table_name,
	    work_id => work_id,
	    status => 'R');
      IF retval < 0 THEN
	 msp_error_code := 11;
	 msp_error_detail := 'action=I,id_type=CR';
	 RAISE core_table_insert_exc;
      END IF;

      msp_location := '124.1';
      UPDATE source_context_relationships SET switch='I';
  
      COMMIT;      
   END IF;

   IF UPPER(table_name) = 'A' OR UPPER(table_name) = 'ALL' THEN

      initialize_trace('CORE_TABLE_INSERT');

      msp_location := '133';
      retval :=
	 MEME_BATCH_ACTIONS.macro_action(
	    action => 'I',
	    id_type => 'A',
	    authority => authority,
	    table_name => table_name,
	    work_id => work_id,
	    status => 'R',
	    set_preferred_flag => MEME_CONSTANTS.NO);

      IF retval < 0 THEN
	 msp_error_code := 11;
	 msp_error_detail := 'action=I,id_type=A';
	 RAISE core_table_insert_exc;
      END IF;

      msp_location := '133.1';
      UPDATE source_attributes SET switch='I';
  
      COMMIT;      

   END IF;

   IF UPPER(table_name) = 'CS' OR UPPER(table_name) = 'ALL' THEN

      msp_location := '140a';
      UPDATE source_concept_status SET switch='N'
      WHERE switch='R'
      AND concept_id IN
          (SELECT concept_id FROM source_concept_status
           MINUS
           SELECT concept_id from source_classes_atoms
           WHERE switch='R');

      msp_location := '140b';
      retval :=
	 MEME_BATCH_ACTIONS.macro_action(
	    action => 'I',
	    id_type => 'CS',
	    authority => authority,
	    table_name => table_name,
	    work_id => work_id,
	    status => 'R');
      IF retval < 0 THEN
	 msp_error_code := 11;
	 msp_error_detail := 'action=I,id_type=CS';
	 RAISE core_table_insert_exc;
      END IF;

   END IF;

   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Insert core data',
	detail => 'Insert ' || table_name || ' data.',
	transaction_id => retval,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::core_table_insert',
    	'Done process core_table_insert',0,work_id,0,100);

EXCEPTION
   WHEN core_table_insert_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END core_table_insert;

/* PROCEDURE MAP_OBSOLETE_RELS *************************************************
 * This  procedure replaces  old atom_id of a relationships to new atom_id.  The
 * process looks up all of the relationship connected to atoms of an old version
 * of  a source  and finds  which ATOMS  in the  new  version of the source best
 * replace old atoms.  The  atom_ids in  relationships are  mapped  to these new
 * atoms.
 */
PROCEDURE map_obsolete_rels(
   stripped_source IN VARCHAR2,
   authority	   IN VARCHAR2,
   work_id	   IN INTEGER
)
IS
   tmp_table_1	   VARCHAR2(50);
   tmp_table_2	   VARCHAR2(50);
   tmp_table_3	   VARCHAR2(50);
   retval	   INTEGER;
   l_query	   VARCHAR2(2000);
   map_obsolete_rels_exc EXCEPTION;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_obsolete_rels',
    	'Starting map_obsolete_rels',0,work_id,0,1);

   MEME_UTILITY.sub_timing_start;

   initialize_trace('MAP_OBSOLETE_RELS');

   retval := 0;

   -- Find atoms of an old version of a source */
   msp_location := '100';
   tmp_table_1 := MEME_UTILITY.get_unique_tablename;
   msp_location := '101';
   MEME_UTILITY.drop_it('table',tmp_table_1);
   msp_location := '102';
   l_query :=
      'CREATE TABLE ' || tmp_table_1 || ' NOLOGGING AS
       SELECT atom_id FROM classes a, source_version b
       WHERE b.source = ''' || map_obsolete_rels.stripped_source || '''
       AND a.source = b.previous_name ';
   local_exec(l_query);

   msp_location := '103';
   --MEME_SYSTEM.analyze(tmp_table_1);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_obsolete_rels',
    	'Done finding atoms of an old version of a source',0,work_id,0,20);

   -- Find relationships connected to obsolete atoms
   -- restrict to ones that are not going to be re-mapped by map_sg_data
   msp_location := '110';
   tmp_table_2 := MEME_UTILITY.get_unique_tablename;
   msp_location := '111';
   MEME_UTILITY.drop_it('table',tmp_table_2);
   msp_location := '112';
   l_query :=
      'CREATE TABLE ' || tmp_table_2 || ' NOLOGGING AS 
       SELECT a.*
       FROM relationships a, source_version b, ' || tmp_table_1 || ' c
       WHERE b.source = ''' || map_obsolete_rels.stripped_source || '''	
         AND a.source NOT LIKE b.previous_name || ''%''
         AND a.relationship_level = ''S''
         AND a.atom_id_1 = c.atom_id
       UNION SELECT a.*
       FROM relationships a, source_version b, ' || tmp_table_1 || ' c
       WHERE b.source = ''' || map_obsolete_rels.stripped_source || '''
         AND a.source NOT LIKE b.previous_name || ''%''
         AND a.relationship_level = ''S''
         AND a.atom_id_2 = c.atom_id ';
   local_exec(l_query);

   msp_location := '116';
   --MEME_SYSTEM.analyze(tmp_table_2);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_obsolete_rels',
    	'Done finding relationships connected to obsolete atoms',0,work_id,0,40);

   -- Find best safe replacement atom for the obsolete atom
   -- Join back to tmp_table_1 to ensure that we are only considering replacing "prev source" atoms
   msp_location := '120';
   tmp_table_3 := MEME_UTILITY.get_unique_tablename;
   msp_location := '121';
   MEME_UTILITY.drop_it('table',tmp_table_3);
   msp_location := '122';
   l_query :=
      'CREATE TABLE ' || tmp_table_3 || ' NOLOGGING AS 
       SELECT relationship_id AS row_id, atom_id_1 AS old_value,
              to_number(SUBSTR(max_rank, INSTR(max_rank,''/'')+1)) as new_value
       FROM (SELECT relationship_id, atom_id_1,
      		    max(b.rank || ''/'' || new_atom_id) AS max_rank
       	     FROM ' || tmp_table_2 || ' a, mom_safe_replacement b, 
		  ' || tmp_table_1 || ' c
       	     WHERE a.atom_id_1 = b.old_atom_id
               AND a.atom_id_1 = c.atom_id
       	     GROUP BY relationship_id, atom_id_1)
       UNION 
       SELECT relationship_id AS row_id, atom_id_2 AS old_value,
       	      to_number(SUBSTR(max_rank, INSTR(max_rank,''/'')+1)) as new_value
       FROM (SELECT relationship_id, atom_id_2,
       		    max(b.rank || ''/'' || new_atom_id) AS max_rank
             FROM ' || tmp_table_2 || ' a, mom_safe_replacement b, 
		  ' || tmp_table_1 || ' c
       	     WHERE a.atom_id_2 = b.old_atom_id
       	       AND a.atom_id_2 = c.atom_id
     	     GROUP BY relationship_id, atom_id_2) ';
   local_exec(l_query);

   msp_location := '125';
   MEME_UTILITY.drop_it('table',tmp_table_1);
   MEME_UTILITY.drop_it('table',tmp_table_2);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_obsolete_rels',
    	'Done finding best safe replacement atom for the obsolete atom',0,work_id,0,60);

   -- Exclude rels where both atom ids are going to be mapped
   -- i.e. within-source rels
   msp_location := '130';
   l_query :=
      'DELETE FROM ' || tmp_table_3 || '
       WHERE row_id IN
         (SELECT row_id FROM ' || tmp_table_3 || '
          GROUP BY row_id HAVING COUNT(*) > 1) ';
   local_exec(l_query);

   -- Exclude cases where old_value=new_value (these are source-replacements)
   msp_location := '130b';
   l_query :=
      'DELETE FROM ' || tmp_table_3 || '
       WHERE old_value = new_value';
   local_exec(l_query);


   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_obsolete_rels',
    	'Done excluding rels where both atom ids are going to be mapped',0,work_id,0,80);

   -- Make the atom id change
   msp_location := '140';
   retval := MEME_BATCH_ACTIONS.macro_action(
      action => 'A',
      id_type => 'R',
      authority => map_obsolete_rels.authority,
      table_name => tmp_table_3,
      work_id => map_obsolete_rels.work_id,
      status => 'R');

   MEME_UTILITY.put_message('transaction_id='||retval);

   IF retval = -1 THEN
      msp_error_code := 11;
      msp_error_detail := 'action=A,id_type=R';
      MEME_UTILITY.drop_it('table',tmp_table_3);
      RAISE map_obsolete_rels_exc;
   END IF;

   msp_location := '160';
   MEME_UTILITY.drop_it('table',tmp_table_3);

    MEME_UTILITY.sub_timing_stop;
    msp_location := '150';
    MEME_UTILITY.log_operation (
	authority => authority,
	activity => 'Map obsolete relationships',
	detail => 'Done mapping obsolete relationships for ' || stripped_source,
	transaction_id => 0,
	work_id => work_id,
	elapsed_time => MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_obsolete_rels',
    	'Done process map_obsolete_rels',0,work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN map_obsolete_rels_exc THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_2);
      MEME_UTILITY.drop_it('table',tmp_table_3);
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_2);
      MEME_UTILITY.drop_it('table',tmp_table_3);
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);

END map_obsolete_rels;

/* PROCEDURE CREATE_RANK_TABLE *****************************************************
 * 
 * Creat sg_id,sg_qualifier=>atom_id mapping for an sg_type and a table.
 */
FUNCTION create_rank_table(
      sg_type             IN VARCHAR2,
      table_name          IN VARCHAR2,
      str_pad             IN VARCHAR2
) RETURN VARCHAR2
IS
  l_ret_table     VARCHAR2(50);
  l_rank_field    VARCHAR2(50);
  l_id_field      VARCHAR2(50);
  l_qual_field    VARCHAR2(50);
  l_qual_clause   VARCHAR2(4000);
  l_table         VARCHAR2(4000);
  l_rank_length   INTEGER;
  l_atom_id_func  VARCHAR2(4000);
  sui_pre_len     INTEGER;
  aui_pre_len     INTEGER;
BEGIN

   msp_location := '100';
   l_ret_table := MEME_UTILITY.get_unique_tablename;

   --
   -- Look up id and qualifier fields
   --
   msp_location := '110';
   l_id_field := MEME_UTILITY.get_value_by_code(sg_type,'map_sg_id_field');
   msp_location := '120';
   l_qual_field := MEME_UTILITY.get_value_by_code(sg_type,'map_sg_qual_field');
   IF l_qual_field = '' THEN
     l_qual_field := 'NULL';
   END IF;
   msp_location := '130';
   l_table := MEME_UTILITY.get_value_by_code(sg_type,'map_sg_table');

   --
   -- Look up rank length
   --
   msp_location := '140';
   SELECT LENGTH(MEME_RANKS.get_atom_editing_rank(tbr.rank, tr.release_rank, 
      last_release_rank, sui , aui, atom_id)) INTO l_rank_length
   FROM classes a, termgroup_rank tr, tobereleased_rank tbr
   WHERE rownum = 1
     AND a.termgroup = tr.termgroup
     AND a.tobereleased = tbr.tobereleased;
   msp_location := '150';

   --
   -- Customize table settings
   --
   IF l_table = 'classes' THEN
     IF sg_type like '%ROOT%' THEN
       l_table := 
         '(SELECT atom_id, last_release_rank, sui rank_sui, aui rank_aui, termgroup,
               tobereleased, stripped_source || ''/'' || tty root_termgroup, 
               stripped_source root_source, ' || l_id_field || '
           FROM classes a, source_rank b WHERE a.source=b.source
           UNION ALL
           SELECT atom_id, last_release_rank, sui, aui, termgroup,
               tobereleased, stripped_source || ''/'' || tty root_termgroup, 
               stripped_source root_source, ' || l_id_field || '
           FROM source_classes_atoms a, source_rank b
           WHERE switch != ''I'' AND a.source = b.source)';
     ELSE
       l_table := 
         '(SELECT atom_id, last_release_rank, sui rank_sui, aui rank_aui, 
           tobereleased, termgroup, source, ' || l_id_field || '
           FROM classes
           UNION ALL
           SELECT atom_id, last_release_rank, sui, aui,
                tobereleased, termgroup, source, ' || l_id_field || '
           FROM source_classes_atoms
           WHERE switch != ''I'')';
     END IF;
     sui_pre_len := LENGTH(MEME_UTILITY.get_value_by_code('SUI','ui_prefix'))+1;
     aui_pre_len := LENGTH(MEME_UTILITY.get_value_by_code('AUI','ui_prefix'))+1;
     -- SPECIAL NOTE: this is an additional implementation to MEME_RANKS.get_atom_editing_rank
     --  for performance.  If that algorithm changes, this one must also.
     l_atom_id_func :=
       'NVL(SUBSTR(MAX(
              DECODE(a.tobereleased,''Y'',9,''y'',7,''n'',3,1)||LPAD(c.release_rank,4,0)||
              a.last_release_rank||(999999999-SUBSTR(rank_sui,' || sui_pre_len || ')) ||
              (999999999-SUBSTR(rank_aui,' || aui_pre_len || '))||LPAD(a.atom_id,10,0)||''/''||a.atom_id),
              ' || l_rank_length || ' +2),0)';
     l_rank_field := 'termgroup';
   ELSE
     IF sg_type like '%ROOT%' THEN
       l_table := 
         '(SELECT atom_id_1, relationship_id, a.source, stripped_source root_source,
                  tobereleased, ' || l_id_field || '
           FROM relationships a, source_rank b WHERE a.source=b.source
           UNION ALL
           SELECT atom_id_1, relationship_id, a.source, stripped_source root_source,
                  tobereleased, ' || l_id_field || '
           FROM source_relationships a, source_rank b WHERE a.source=b.source
           UNION ALL
           SELECT atom_id_1, relationship_id, a.source, stripped_source root_source,
                  tobereleased, ' || l_id_field || '
           FROM context_relationships a, source_rank b WHERE a.source=b.source
           UNION ALL
           SELECT atom_id_1, relationship_id, a.source, stripped_source root_source,
                  tobereleased, ' || l_id_field || '
           FROM source_context_relationships a, source_rank b WHERE a.source=b.source)';
     ELSE
       l_table := 
         '(SELECT atom_id_1, relationship_id, source, tobereleased, ' || l_id_field || '
           FROM relationships
           UNION ALL
           SELECT atom_id_1, relationship_id, source, tobereleased, ' || l_id_field || '
           FROM source_relationships
           UNION ALL
           SELECT atom_id_1, relationship_id, source, tobereleased, ' || l_id_field || '
           FROM context_relationships
           UNION ALL
           SELECT atom_id_1, relationship_id, source, tobereleased, ' || l_id_field || '
           FROM source_context_relationships)';
     END IF;
     l_atom_id_func :=
        'NVL(SUBSTR(MAX(DECODE(a.tobereleased,''Y'',9,''y'',7,''n'',3,1)
             ||LPAD(c.rank,5,0) || ''/'' || a.atom_id_1),
             INSTR(MAX(DECODE(a.tobereleased,''Y'',9,''y'',7,''n'',3,1)
             ||LPAD(c.rank,5,0) || ''/'' || a.atom_id_1),''/'')+1),0)';
     l_rank_field := 'source';
   END IF;
      
   msp_location := '160';

   --
   -- CREATE table
   --
   IF sg_type = 'SRC_ATOM_ID' THEN
      --
      -- Map the SRC_ATOM_ID type
      --
      EXECUTE IMMEDIATE
     	'CREATE TABLE ' || l_ret_table || ' NOLOGGING AS
        SELECT /*+ PARALLEL(a) */ DISTINCT local_row_id atom_id, 
             b.sg_id' || str_pad || ' sg_id,
             b.sg_qualifier' || str_pad || ' sg_qualifier
        FROM source_id_map a, ' || table_name || ' b
        WHERE source_row_id = to_number(sg_id' || str_pad || ')
          AND table_name = ''C''
          AND b.sg_type' || str_pad || ' = ''SRC_ATOM_ID''';
   ELSIF sg_type = 'AUI' THEN
      --
      -- Map the AUI type
      --
      EXECUTE IMMEDIATE
        'CREATE TABLE ' || l_ret_table || ' NOLOGGING AS
             SELECT /*+ PARALLEL(a) */ DISTINCT NVL(SUBSTR(MAX(DECODE(a.tobereleased,''Y'',9,''y'',7,''n'',3,1)
             ||LPAD(c.rank,5,0) || ''/'' || a.atom_id),
             INSTR(MAX(DECODE(a.tobereleased,''Y'',9,''y'',7,''n'',3,1)
             ||LPAD(c.rank,5,0) || ''/'' || a.atom_id),''/'')+1),0) atom_id,
              b.sg_id' || str_pad || ' sg_id,
              b.sg_qualifier' || str_pad || ' sg_qualifier
         FROM classes a, ' || table_name || ' b, source_rank c
         WHERE a.aui = sg_id' || str_pad || '
           AND a.tobereleased in (''Y'',''y'')
           AND a.source = c.source
           AND a.tobereleased in( ''Y'',''y'')
           AND b.sg_type' || str_pad || ' = ''AUI''
           GROUP BY sg_id' || str_pad || ', sg_qualifier' || str_pad || '';

   ELSIF sg_type = 'SRC_REL_ID' THEN
      --
      -- Map the SRC_REL_ID type
      --
      EXECUTE IMMEDIATE
        'CREATE TABLE ' || l_ret_table || ' NOLOGGING AS
         SELECT /*+ PARALLEL(a) */ DISTINCT c.atom_id_1 atom_id, 
              b.sg_id' || str_pad || ' sg_id,
              b.sg_qualifier' || str_pad || ' sg_qualifier
         FROM source_id_map a, ' || table_name || ' b, relationships c
         WHERE source_row_id = to_number(sg_id' || str_pad || ')
           AND table_name = ''R''
           AND a.local_row_id = c.relationship_id
           AND b.sg_type' || str_pad || ' = ''SRC_REL_ID''';

   ELSE
      IF l_qual_field = 'none' THEN
         l_qual_clause := 'b.sg_qualifier' || str_pad || ' IS NULL';
      ELSE
         l_qual_clause := 'a.' || l_qual_field || ' = b.sg_qualifier' || str_pad;
      END IF;
      EXECUTE IMMEDIATE
        'CREATE TABLE ' || l_ret_table || ' NOLOGGING AS
         SELECT ' || l_atom_id_func || ' atom_id,
              b.sg_id' || str_pad || ' sg_id,
              b.sg_qualifier' || str_pad || ' sg_qualifier
         FROM ' || l_table || ' a, ' || table_name || ' b,
              ' || l_rank_field || '_rank c
         WHERE b.sg_type' || str_pad || ' = ''' || sg_type || '''
           AND a.' || l_rank_field || ' = c.' || l_rank_field || '
           AND a.' || l_id_field || ' = b.sg_id' || str_pad || '
           AND ' || l_qual_clause || '
         GROUP BY b.sg_id' || str_pad || ', b.sg_qualifier' || str_pad || '';
   END IF;

   msp_location := '900';
   EXECUTE IMMEDIATE 'CREATE INDEX x_' || l_ret_table || ' on ' || l_ret_table || ' (sg_id) COMPUTE STATISTICS';
  
   RETURN l_ret_table;
END create_rank_table;

/* PROCEDURE MAP_SG_FIELDS *****************************************************
 * 
 * Maps "native identifiers" to atom ids.
 *
 * If pair_flag is on it uses atom_id_1, atom_id_2,
 * if concept_Flag is on, it assigns concept_ids also.
 * if qa_flag is on, it verifies that everything was mapped
 */
PROCEDURE map_sg_fields(
   table_name	   IN VARCHAR2,
   pair_flag	   IN VARCHAR2 := MEME_CONSTANTS.NO,
   concept_flag    IN VARCHAR2 := MEME_CONSTANTS.YES,
   qa_flag         IN VARCHAR2 := MEME_CONSTANTS.YES
)
IS
    row_count	    	INTEGER;
    update_ctr	    	INTEGER;

    loop_column	    	DBMS_SQL.VARCHAR2_TABLE;
    table_column    	DBMS_SQL.VARCHAR2_TABLE;
    str_pad	    	VARCHAR2(10);

    l_query	    	VARCHAR2(4000);
    l_type	    	VARCHAR2(1000);
    l_rank_table        VARCHAR2(30);
    l_rowid	    	ROWID;
    l_cui              	VARCHAR2(10);
    l_max_rank	    	VARCHAR2(50);
    l_atom_id         	INTEGER;
    l_concept_id     	INTEGER;
    l_qual_field        VARCHAR2(50);
    l_qual_clause       VARCHAR2(1000);

    TYPE cur_type IS REF CURSOR;
    cur_var		cur_type;

    map_sg_fields_exc 	EXCEPTION;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_fields',
    	'Starting map_sg_fields',0,1,0,1);

    initialize_trace('MAP_SG_FIELDS');
    msp_location := '100';
    -- Map default dynamic table fields

    loop_column(1) := 'atom_id';
    loop_column(2) := 'sg_id';
    loop_column(3) := 'sg_type';
    loop_column(4) := 'sg_qualifier';

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_fields',
    	'Done map default dynamic table fields',0,1,0,30);

    -- If concept_flag is yes, set concept_id
    IF concept_flag = MEME_CONSTANTS.YES THEN
	loop_column(5) := 'concept_id';
    END IF;

    --
    -- Validate table structure
    --
    msp_location := '200';
    FOR loop_ctr IN 1..loop_column.LAST LOOP
	msp_location := '210';
	FOR sub_loop_ctr IN 1..2 LOOP
 	    -- Only one loop if no pair flag (also don't use str_pad)
	    IF map_sg_fields.pair_flag = MEME_CONSTANTS.NO THEN

	    	IF sub_loop_ctr = 2 THEN
		    EXIT;		    
	     	END IF;
	     	table_column(sub_loop_ctr) := loop_column(loop_ctr);
	    -- If pair flag check ###_{1,2} instead of just ###
	    ELSIF map_sg_fields.pair_flag = MEME_CONSTANTS.YES THEN
	     	table_column(sub_loop_ctr) := loop_column(loop_ctr) ||
		 '_'||sub_loop_ctr;
	    END IF;
            -- Check field type
	    msp_location := '220';
	    l_type := MEME_UTILITY.get_field_type(
	 	     UPPER(map_sg_fields.table_name), 
			  table_column(sub_loop_ctr));
	    IF l_type = MEME_CONSTANTS.FIELD_NOT_FOUND THEN
	     	msp_error_code := 20; 
		msp_error_detail :=
		   'table_name='||map_sg_fields.table_name||', '||
		   'column_name='||table_column(sub_loop_ctr)||'.';
	     	RAISE map_sg_fields_exc;
	    END IF;
	END LOOP; -- loop counter
    END LOOP; -- column validation list
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_fields',
    	'Done validating table structures',0,1,0,70);

    --
    -- LOOP through <>_1 and <>_2
    --
    msp_location := '300';
    FOR loop_ctr IN 1..2 LOOP

        --
        -- Configure str_pad
        --
	IF map_sg_fields.pair_flag = MEME_CONSTANTS.NO THEN
	    IF loop_ctr = 2 THEN
	     	EXIT;
	    END IF;
	    str_pad := '';
	ELSIF map_sg_fields.pair_flag = MEME_CONSTANTS.YES THEN
	    str_pad := '_'||loop_ctr;
	END IF;


        MEME_UTILITY.put_message(msp_method||' prep table.');

	--
	-- Reset atom_ids to zero
	--
	msp_location := '310';
	EXECUTE IMMEDIATE 
	    'UPDATE /*+ PARALLEL(a) */ ' || map_sg_fields.table_name || ' a
	     SET atom_id' || str_pad || ' = 0 
	     WHERE atom_id' || str_pad || ' != 0';
        COMMIT;
 
	--
	-- Map any CUI cases through cui_history
	--
	msp_location := '360';
	LOOP
	    msp_location := '362';
	    -- Loop until 0 rows processed
	    update_ctr := 0;
	    trace(msp_method||': Updating merged CUIs ...');

	    l_query :=
		'SELECT a.rowid, cui2
		 FROM ' || map_sg_fields.table_name || ' a, cui_history b
		 WHERE b.relationship_name = ''SY'' AND sg_id' || str_pad || ' = cui1
		    AND sg_type' || str_pad || ' like ''CUI%''';

	    OPEN cur_var FOR l_query;
	    LOOP
		FETCH cur_var INTO l_rowid, l_cui;
		EXIT WHEN cur_var%NOTFOUND;

		msp_location := '362.10';
		EXECUTE IMMEDIATE
		    'UPDATE '|| map_sg_fields.table_name || '
		     SET sg_id' || str_pad || ' = :x
		     WHERE rowid = :y'
	     USING l_cui,l_rowid;

		update_ctr := update_ctr + 1;

	    END LOOP;
	    CLOSE cur_var;

	    trace(msp_method||': '||update_ctr||' rows processed.');
	    EXIT WHEN update_ctr = 0;

	END LOOP;
	COMMIT;


	--
	-- Loop across sg_types and assign atom ids
	--
	msp_location := '500';
        OPEN cur_var FOR 'SELECT distinct sg_type' || str_pad || ' FROM ' || table_name;
        LOOP
            FETCH cur_var INTO l_type;
            EXIT WHEN cur_var%NOTFOUND;
            msp_error_detail := l_type;

            MEME_UTILITY.put_message(msp_method||' create rank table (' || l_type || ').');
            msp_location := '510';
            l_rank_table := 
		create_rank_table(sg_type=>l_type, table_name=>table_name, str_pad=>str_pad);

            msp_location := '515';
            l_qual_field := MEME_UTILITY.get_value_by_code(l_type,'map_sg_qual_field');
            IF l_qual_field = 'none' THEN
               l_qual_clause := '';
            ELSE
               l_qual_clause := 'AND a.sg_qualifier' || str_pad || ' = b.sg_qualifier';
            END IF;
            --
            -- Perform these mappings
            --
            MEME_UTILITY.put_message(msp_method||' set atom ids (' || l_type || ').');
            msp_location := '520';
            EXECUTE IMMEDIATE
                'UPDATE /*+ PARALLEL(a) */ ' || table_name || ' a
                 SET atom_id' || str_pad || ' =
     	          NVL((SELECT atom_id
                   FROM ' || l_rank_table || ' b
                   WHERE a.sg_id' || str_pad || ' = b.sg_id
                     ' || l_qual_clause || '),0)
                 WHERE sg_type' || str_pad || ' = :x'
           USING l_type;

           COMMIT;

           msp_location := '530';
	   MEME_UTILITY.drop_it('table',l_rank_table);

        END LOOP;

        msp_location := '600';

            MEME_UTILITY.put_message(msp_method||' final steps.');
	--
	-- Convert the non-null qualifier SRC_ATOM_ID type
	--    looks in classes only
	--
	msp_location := '350.1';
	trace(msp_method||': Mapping SOURCE_ATOM_ID,CODE ...');
	EXECUTE IMMEDIATE
      	'UPDATE /*+ PARALLEL(a) */ ' || map_sg_fields.table_name || ' a
	    SET (sg_id' || str_pad || ', sg_type' || str_pad || ',sg_qualifier' || str_pad || ' ) =
	      (SELECT code, ''CODE_SOURCE'', source FROM classes b
	       WHERE a.atom_id' || str_pad || ' = b.atom_id)
	    WHERE sg_type' || str_pad || ' = ''SRC_ATOM_ID'' 
	      AND sg_qualifier' || str_pad || ' = ''CODE''';
	COMMIT;
	msp_location := '350.2';
	trace(msp_method||': Mapping SOURCE_ATOM_ID,SOURCE_CUI ...');
	EXECUTE IMMEDIATE
      	'UPDATE /*+ PARALLEL(a) */ ' || map_sg_fields.table_name || ' a
	    SET (sg_id' || str_pad || ', sg_type' || str_pad || ',sg_qualifier' || str_pad || ' ) =
	      (SELECT source_cui, ''SOURCE_CUI'', source FROM classes b
	       WHERE a.atom_id' || str_pad || ' = b.atom_id)
	    WHERE sg_type' || str_pad || ' = ''SRC_ATOM_ID'' 
	      AND sg_qualifier' || str_pad || ' = ''SOURCE_CUI''';
	COMMIT;
	msp_location := '350.3';
	trace(msp_method||': Mapping SOURCE_ATOM_ID,SOURCE_DUI ...');
	EXECUTE IMMEDIATE
      	'UPDATE /*+ PARALLEL(a) */ ' || map_sg_fields.table_name || ' a
	    SET (sg_id' || str_pad || ', sg_type' || str_pad || ',sg_qualifier' || str_pad || ' ) =
	      (SELECT source_dui, ''SOURCE_DUI'', source FROM classes b
	       WHERE a.atom_id' || str_pad || ' = b.atom_id)
	    WHERE sg_type' || str_pad || ' = ''SRC_ATOM_ID'' 
	      AND sg_qualifier' || str_pad || ' = ''SOURCE_DUI''';
	COMMIT;
	msp_location := '350.4';
	trace(msp_method||': Mapping SOURCE_ATOM_ID,SOURCE_AUI ...');
	EXECUTE IMMEDIATE
      	'UPDATE /*+ PARALLEL(a) */ ' || map_sg_fields.table_name || ' a
	    SET (sg_id' || str_pad || ', sg_type' || str_pad || ',sg_qualifier' || str_pad || ' ) =
	      (SELECT source_aui, ''SOURCE_AUI'', source FROM classes b
	       WHERE a.atom_id' || str_pad || ' = b.atom_id)
	    WHERE sg_type' || str_pad || ' = ''SRC_ATOM_ID'' 
	      AND sg_qualifier' || str_pad || ' = ''SOURCE_AUI''';
	COMMIT;


	--
	-- We now have atom ids, map to the concept_id level
	--
	-- Here we should map concept_id where NVL(atom_id,0) != 0
	-- This takes care of EVERY case without having to deal with 
	-- them one by one.
	IF map_sg_fields.concept_flag = MEME_CONSTANTS.YES THEN
    	 msp_location := '850';
	    trace(msp_method||': Mapping CONCEPT_ID which atom_id' || str_pad || ' != 0 ...');

	    EXECUTE IMMEDIATE
	      'UPDATE ' || map_sg_fields.table_name || ' a
		SET (concept_id' || str_pad || ') =
		  (SELECT b.concept_id FROM classes b
		    WHERE a.atom_id' || str_pad || ' = b.atom_id)
		WHERE NVL(atom_id' || str_pad || ',0) != 0'; 
	END IF;

	COMMIT;

	--
	-- QA Check for null or 0 atom ids
	--
	msp_location := '900';
	-- Here there is no "switch" so we try to map both sides
	-- if any went unmapped that should've been mapped it is an error
	l_query := 
	 'SELECT COUNT(*) FROM ' || map_sg_fields.table_name || '
	     WHERE ((NVL(atom_id' || str_pad || ',0) = 0) AND 
		     sg_type' || str_pad || ' not like ''CUI%'')
		 OR ((NVL(atom_id' || str_pad || ',0) = 0) AND 
		  sg_type' || str_pad || ' not like ''CUI%'') ';

	row_count := MEME_UTILITY.exec_select(l_query);
	IF qa_flag = MEME_CONSTANTS.YES AND row_count > 0 THEN
	 msp_error_code := 23; msp_error_detail := 'table_name='||map_sg_fields.table_name;
	 RAISE map_sg_fields_exc;
	END IF;

	--
	-- QA Check for null or 0 concept ids
	--
	msp_location := '950';
	IF map_sg_fields.concept_flag = MEME_CONSTANTS.YES AND
	 map_sg_fields.qa_flag = MEME_CONSTANTS.YES THEN
	 l_query := 	
	  'SELECT COUNT(*) FROM ' || map_sg_fields.table_name || '
	      WHERE ((NVL(concept_id' || str_pad || ',0) = 0) AND 
	      	    sg_type' || str_pad || ' not like ''CUI%'')
		  OR ((NVL(concept_id' || str_pad || ',0) = 0) AND 
		    sg_type' || str_pad || ' not like ''CUI%'')';
	 row_count := MEME_UTILITY.exec_select(l_query);
	 IF row_count > 0 THEN
	     msp_error_code := 21; msp_error_detail := 'table_name='||map_sg_fields.table_name;
	     RAISE map_sg_fields_exc;
	 END IF;
	END IF;

    END LOOP;

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_fields',
    	'Done process map_sg_fields',0,1,0,100);

    msp_location := '999';
    MEME_UTILITY.put_message(msp_method||' successfully completed.');

    COMMIT;

EXCEPTION
    WHEN map_sg_fields_exc THEN
	error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
		   MEME_UTILITY.put_message('exc');
	RAISE msp_exception;
    WHEN OTHERS THEN
	error_log(msp_method,msp_location,msp_error_code,msp_error_detail||': '||SQLERRM);
		   MEME_UTILITY.put_message('exc');
	RAISE msp_exception;

END map_sg_fields;

/* PROCEDURE MAP_SG_FIELDS_ALL *************************************
 * 
 * Maps "native identifiers" to their respective core table meme ids.
 *
 * If pair_flag is on it uses _1 and _2 suffixes
 * if qa_flag is on, it verifies that everything was mapped
 *
 */
PROCEDURE map_sg_fields_all (
   table_name	   IN VARCHAR2,
   pair_flag	   IN VARCHAR2 := MEME_CONSTANTS.NO,
   concept_flag	   IN VARCHAR2 := MEME_CONSTANTS.NO,
   qa_flag         IN VARCHAR2 := MEME_CONSTANTS.YES
)
IS
    row_count	    	INTEGER;
    update_ctr	    	INTEGER;

    loop_column	    	DBMS_SQL.VARCHAR2_TABLE;
    table_column    	DBMS_SQL.VARCHAR2_TABLE;
    str_pad	    	VARCHAR2(10);

    l_query	    	VARCHAR2(1000);
    l_update	    	VARCHAR2(1000);
    l_string	    	VARCHAR2(1000);

    retval             	VARCHAR2(30);

    l_rowid	    	ROWID;
    l_cui              	VARCHAR2(10);
    l_max_rank	    	VARCHAR2(50);
    l_rank_table    	VARCHAR2(50);
    l_atom_id         	INTEGER;
    l_concept_id     	INTEGER;

    TYPE cur_type IS REF CURSOR;
    cur_var		cur_type;

    map_sg_fields_all_exc 	EXCEPTION;
BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_fields_all',
    	'Starting process map_sg_fields_all',0,1,0,1);

    initialize_trace('MAP_SG_FIELDS_ALL');

    msp_location := '100';

    -- 
    -- Fields to validate
    --
    loop_column(1) := 'atom_id';
    loop_column(2) := 'sg_id';
    loop_column(3) := 'sg_type';
    loop_column(4) := 'sg_qualifier';
    loop_column(5) := 'sg_meme_data_type';
    loop_column(6) := 'sg_meme_id';

    --
    -- Additional if concept flag is on
    --
    IF concept_flag = MEME_CONSTANTS.YES THEN
	loop_column(7) := 'concept_id';
    END IF;

    --
    -- Validate table structure
    --
    msp_location := '200';
    FOR loop_ctr IN 1..loop_column.LAST LOOP
        msp_location := '210';
        FOR sub_loop_ctr IN 1..2 LOOP
            -- Only one loop if no pair flag (also don't use str_pad)
            IF map_sg_fields_all.pair_flag = MEME_CONSTANTS.NO THEN
                IF sub_loop_ctr = 2 THEN
                    EXIT;
                END IF;
                table_column(sub_loop_ctr) := loop_column(loop_ctr);

            -- If pair flag check ###_{1,2} instead of just ###
            ELSIF map_sg_fields_all.pair_flag = MEME_CONSTANTS.YES THEN
                  table_column(sub_loop_ctr) := loop_column(loop_ctr) ||
                  '_'||sub_loop_ctr;
            END IF;

            msp_location := '220';
            retval := MEME_UTILITY.get_field_type(
               UPPER(map_sg_fields_all.table_name), 
               table_column(sub_loop_ctr));

            IF retval = MEME_CONSTANTS.FIELD_NOT_FOUND THEN
                msp_error_code := 20; 
                msp_error_detail :=
                    'table_name='||map_sg_fields_all.table_name||', '||
                    'column_name='||table_column(sub_loop_ctr)||'.';
                    RAISE map_sg_fields_all_exc;
            END IF;

        END LOOP; -- loop counter

    END LOOP; -- column validation list

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_fields_all',
    	'Done validating table structures',0,1,0,40);

    --
    -- Start out by mapping sg_fields the normal way
    --
    map_sg_fields(
   	table_name => table_name,
   	pair_flag => pair_flag,
   	concept_flag => concept_flag,
   	qa_flag => qa_flag);

    initialize_trace('MAP_SG_FIELDS_ALL');
    msp_location := '300';

    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_fields_all',
    	'Done starting out by mapping sg_fields the normal way',0,1,0,80);

    	
	--
	-- Create rank table for upcoming comparisons
	--
 	msp_location := '400';
    l_rank_table := MEME_UTILITY.get_unique_tablename;
 	msp_location := '401';
	MEME_UTILITY.drop_it('table',l_rank_table);
 	msp_location := '402';
	EXECUTE IMMEDIATE
	 	'CREATE TABLE ' || l_rank_table || ' AS
         WITH sabs AS 
 			(SELECT DISTINCT sg_qualifier source FROM source_attributes
	         WHERE sg_type IN (''SOURCE_RUI'')),
		      root_sabs AS
 			(SELECT DISTINCT sg_qualifier root_source FROM source_attributes
	         WHERE sg_type IN (''ROOT_SOURCE_RUI''))
		 SELECT tbr.RANK || LPAD (c.RANK, 5, 0) || ''~'' || atom_id_1 || ''/'' || relationship_id rank,
                r.atom_id_1, r.source, c.stripped_source root_source, source_rui
         FROM relationships r, source_rank c, tobereleased_rank tbr
         WHERE r.source = c.source AND r.tobereleased = tbr.tobereleased
           AND r.relationship_level = ''S'' 
           AND c.stripped_source IN 
             (SELECT root_source FROM root_sabs UNION
   			  SELECT stripped_source FROM source_rank a, sabs b WHERE a.source = b.source)
	     UNION ALL
		 SELECT tbr.RANK || LPAD (c.RANK, 5, 0) || ''~'' || atom_id_1 || ''/'' || relationship_id rank,
                r.atom_id_1, r.source, c.stripped_source root_source, source_rui
         FROM context_relationships r, source_rank c, tobereleased_rank tbr
         WHERE r.source = c.source AND r.tobereleased = tbr.tobereleased
           AND r.relationship_level = ''S'' 
           AND c.stripped_source IN 
             (SELECT root_source FROM root_sabs UNION
   			  SELECT stripped_source FROM source_rank a, sabs b WHERE a.source = b.source)';

        
 	msp_location := '410';
    EXECUTE IMMEDIATE
    	'CREATE INDEX x_' || l_rank_table || ' ON ' || l_rank_table || ' (atom_id_1) COMPUTE STATISTICS';  	
    	
    --
    -- MAP sg_meme_data_type and sg_meme_id
    --
    FOR loop_ctr IN 1..2 LOOP

	--
	-- Only one pass if not working with pairs, ignore str_pad
	--
	IF map_sg_fields_all.pair_flag = MEME_CONSTANTS.NO THEN
	    IF loop_ctr = 2 THEN
	     	EXIT;
	    END IF;
	    str_pad := '';

	--
	-- Use str_pad if we have pairs
	--
	ELSIF map_sg_fields_all.pair_flag = MEME_CONSTANTS.YES THEN
	    str_pad := '_'||loop_ctr;
	END IF;


	--
	-- For most types, the sg_meme_data_type is 'C'
 	-- and the sg_meme_id is an atom id
	--
	msp_location := '310';
	EXECUTE IMMEDIATE 
	'UPDATE ' || table_name || '
	 SET sg_meme_data_type' || str_pad || ' = ''C'',
	     sg_meme_id' || str_pad || ' = atom_id' || str_pad || '
	 WHERE sg_type' || str_pad || ' NOT LIKE ''%RUI%''
	   AND sg_type' || str_pad || ' != ''SRC_REL_ID'' ';

    MEME_UTILITY.put_message('Done setting id and data_type for non RUI - ' || SQL%ROWCOUNT);

	COMMIT;
	
	--
	-- Map the SOURCE_RUI type
	-- We look in relationships, source_relationships, 
	-- and then context_relationships
	--

 	msp_location := '805.41';
	EXECUTE IMMEDIATE
	 	'UPDATE ' || map_sg_fields_all.table_name || ' a
	    SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'', 
		NVL(SUBSTR(MAX(rank),
		           INSTR(MAX(rank),''/'')+1),0)
		  FROM ' || l_rank_table || ' b
		  WHERE b.source_rui = a.sg_id' || str_pad || ' 
		  AND b.source = a.sg_qualifier' || str_pad || '
          AND b.atom_id_1 = a.atom_id' || str_pad || ')
	    WHERE sg_type' || str_pad || ' = ''SOURCE_RUI''
	    AND NVL(sg_meme_id' || str_pad || ',0) = 0';

    MEME_UTILITY.put_message('Done setting id and data_type for SOURCE_RUI(r) - ' || SQL%ROWCOUNT);
	COMMIT;

	msp_location := '805.42';
	EXECUTE IMMEDIATE
	 	'UPDATE ' || map_sg_fields_all.table_name || ' a
	    SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'', 
		NVL(SUBSTR(MAX(LPAD(b.source_rank,5,0) || ''~'' || 
			       atom_id_1 || ''/'' ||
			       relationship_id),
		           INSTR(MAX(LPAD(b.source_rank,5,0) || ''~'' || 
				     atom_id_1 || ''/'' ||
				     relationship_id),''/'')+1),0)
		  FROM source_relationships b
		  WHERE b.source_rui = a.sg_id' || str_pad || ' 
		  AND b.source = a.sg_qualifier' || str_pad || '
		  AND b.atom_id_1 = a.atom_id' || str_pad || ')
	    WHERE sg_type' || str_pad || ' = ''SOURCE_RUI''
	    AND NVL(sg_meme_id' || str_pad || ',0) = 0';

    MEME_UTILITY.put_message('Done setting id and data_type for SOURCE_RUI(sr) - ' || SQL%ROWCOUNT);
	COMMIT;

	msp_location := '805.43';
	EXECUTE IMMEDIATE
	 	'UPDATE ' || map_sg_fields_all.table_name || ' a
	    SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'', 
		NVL(SUBSTR(MAX(rank),
		           INSTR(MAX(rank),''/'')+1),0)
		  FROM ' || l_rank_table || ' b
		  WHERE b.source_rui = a.sg_id' || str_pad || ' 
		  AND b.source = a.sg_qualifier' || str_pad || '
		  AND b.atom_id_1 = a.atom_id' || str_pad || ')
	    WHERE sg_type' || str_pad || ' = ''SOURCE_RUI''
	    AND nvl(sg_meme_id' || str_pad || ',0) = 0';

    MEME_UTILITY.put_message('Done setting id and data_type for SOURCE_RUI(cr) - ' || SQL%ROWCOUNT);
	COMMIT;

	--
	-- Map the ROOT_SOURCE_RUI type
	-- We look in relationships, source_relationships, 
	-- and then context_relationships
	--
	msp_location := '805.51';
	
	EXECUTE IMMEDIATE           
        'ALTER SESSION ENABLE PARALLEL DML';
	
	EXECUTE IMMEDIATE
		'UPDATE /*+ first_rows parallel(a) parallel(b)*/ ' || map_sg_fields_all.table_name || ' a
	    SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'',
		NVL(SUBSTR(MAX(rank),
		 	   INSTR(MAX(rank),''/'')+1),0)
		  FROM ' || l_rank_table || ' b 
		  WHERE b.source_rui = a.sg_id' || str_pad || ' 
		  AND b.root_source = a.sg_qualifier' || str_pad || '
		  AND b.atom_id_1 = a.atom_id' || str_pad || ')
	    WHERE sg_type' || str_pad || ' IN (''STRIPPED_SOURCE_RUI'',''ROOT_SOURCE_RUI'')
	    AND NVL(sg_meme_id' || str_pad || ',0) = 0';

    MEME_UTILITY.put_message('Done setting id and data_type for ROOT_SOURCE_RUI(r) - ' || SQL%ROWCOUNT);

	COMMIT;

	msp_location := '805.52';
	EXECUTE IMMEDIATE
		'UPDATE ' || map_sg_fields_all.table_name || ' a
	    SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'',
		NVL(SUBSTR(MAX(LPAD(b.source_rank,5,0) || ''~'' || 
			       atom_id_1 || ''/'' ||
			       relationship_id),
		 	   INSTR(MAX(LPAD(b.source_rank,5,0) || ''~'' || 
				     atom_id_1 || ''/'' ||
				     relationship_id),''/'')+1),0)
		  FROM source_relationships b, source_rank c
		  WHERE b.source_rui = a.sg_id' || str_pad || ' 
            AND b.source = c.source
		    AND c.stripped_source = a.sg_qualifier' || str_pad || '
		    AND b.atom_id_1 = a.atom_id' || str_pad || ')
	    WHERE sg_type' || str_pad || ' IN (''STRIPPED_SOURCE_RUI'',''ROOT_SOURCE_RUI'')
	    AND NVL(sg_meme_id' || str_pad || ',0) = 0';

    MEME_UTILITY.put_message('Done setting id and data_type for ROOT_SOURCE_RUI(sr) - ' || SQL%ROWCOUNT);
	COMMIT;

	msp_location := '805.53';
	EXECUTE IMMEDIATE
		'UPDATE ' || map_sg_fields_all.table_name || ' a
	    SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'',
		NVL(SUBSTR(MAX(rank),
		 	   INSTR(MAX(rank),''/'')+1),0)
		  FROM ' || l_rank_table || ' b
		  WHERE b.source_rui = a.sg_id' || str_pad || ' 
		  AND b.root_source = a.sg_qualifier' || str_pad || '
		  AND b.atom_id_1 = a.atom_id' || str_pad || ')
	    WHERE sg_type' || str_pad || ' IN (''STRIPPED_SOURCE_RUI'',''ROOT_SOURCE_RUI'')
	    AND NVL(sg_meme_id' || str_pad || ',0) = 0';

    MEME_UTILITY.put_message('Done setting id and data_type for ROOT_SOURCE_RUI(cr) - ' || SQL%ROWCOUNT);
	COMMIT;

	--
	-- Map the RUI type
	-- TODO; this could be optimized too, but we never have data for these cases, so not worth it yet
	--
 	msp_location := '810.1';
	EXECUTE IMMEDIATE
	 	'UPDATE ' || map_sg_fields_all.table_name || ' a
	    SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'', 
		NVL(SUBSTR(MAX(tbr.rank||LPAD(b.source_rank,5,0) || ''~'' || 
			       atom_id_1 || ''/'' || 
			       relationship_id),
		           INSTR(MAX(tbr.rank||LPAD(b.source_rank,5,0) || ''~'' || 
				     atom_id_1 || ''/'' ||
				     relationship_id),''/'')+1),0)
		  FROM relationships b, tobereleased_rank tbr
		  WHERE b.rui = a.sg_id' || str_pad || ' 
		    AND b.tobereleased = tbr.tobereleased 
		  AND b.atom_id_1 = a.atom_id' || str_pad || ')
	    WHERE sg_type' || str_pad || ' = ''RUI''
	    AND nvl(sg_meme_id' || str_pad || ',0) = 0';

    MEME_UTILITY.put_message('Done setting id and data_type for RUI(r) - ' || SQL%ROWCOUNT);
	COMMIT;

	msp_location := '810.2';
	EXECUTE IMMEDIATE
	 	'UPDATE ' || map_sg_fields_all.table_name || ' a
	    SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'', 
		NVL(SUBSTR(MAX(LPAD(b.source_rank,5,0) || ''~'' || 
			       atom_id_1 || ''/'' ||
			       relationship_id),
		           INSTR(MAX(LPAD(b.source_rank,5,0) || ''~'' || 
				     atom_id_1 || ''/'' ||
				     relationship_id),''/'')+1),0)
		  FROM source_relationships b
		  WHERE b.rui = a.sg_id' || str_pad || '
		  AND b.atom_id_1 = a.atom_id' || str_pad || ')
	    WHERE sg_type' || str_pad || ' = ''RUI''
	    AND nvl(sg_meme_id' || str_pad || ',0) = 0';

    MEME_UTILITY.put_message('Done setting id and data_type for RUI(r) - ' || SQL%ROWCOUNT);
	COMMIT;

	msp_location := '810.3';
	EXECUTE IMMEDIATE
	 	'UPDATE ' || map_sg_fields_all.table_name || ' a
	    SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'', 
		NVL(SUBSTR(MAX(tbr.rank||LPAD(b.source_rank,5,0) || ''~'' || 
			       atom_id_1 || ''/'' ||
			       relationship_id),
		           INSTR(MAX(tbr.rank||LPAD(b.source_rank,5,0) || ''~'' || 
				     atom_id_1 || ''/'' ||
				     relationship_id),''/'')+1),0)
		  FROM context_relationships b, tobereleased_rank tbr
		  WHERE b.rui = a.sg_id' || str_pad || ' 
		    AND b.tobereleased = tbr.tobereleased
		  AND b.atom_id_1 = a.atom_id' || str_pad || ')
	    WHERE sg_type' || str_pad || ' = ''RUI''
	    AND nvl(sg_meme_id' || str_pad || ',0) = 0';

    MEME_UTILITY.put_message('Done setting id and data_type for RUI(r) - ' || SQL%ROWCOUNT);
	COMMIT;

	--
	-- Map the SRC_REL_ID type
	--
	msp_location := '820';
	EXECUTE IMMEDIATE
		'UPDATE ' || map_sg_fields_all.table_name || ' a
	     SET (sg_meme_data_type' || str_pad || ', sg_meme_id' || str_pad || ') =
     	      (SELECT ''R'',local_row_id FROM source_id_map m
     	       	WHERE m.table_name = ''R''
     	          AND m.source_row_id = a.sg_id' || str_pad || '
     	       )
      	WHERE sg_type' || str_pad || ' = ''SRC_REL_ID'' ';

    MEME_UTILITY.put_message('Done setting id and data_type for SRC_REL_ID - ' || SQL%ROWCOUNT);

    --
	-- Check for 0 sg_meme_ids
	--
	msp_location := '900';
	-- Here there is no "switch" so we try to map both sides
	-- if any went unmapped that should've been mapped it is an error
	l_query := 
	 'SELECT COUNT(*) FROM ' || map_sg_fields_all.table_name || '
	  WHERE NVL(sg_meme_id' || str_pad ||',0) = 0 
   	    AND sg_type' || str_pad || ' not like ''CUI%'' ';

	row_count := MEME_UTILITY.exec_select(l_query);
	IF qa_flag = MEME_CONSTANTS.YES AND row_count > 0 THEN
	    msp_error_code := 23; 
	    msp_error_detail := 'table_name='||table_name;
	    RAISE map_sg_fields_all_exc;
	END IF;

    END LOOP;

 	msp_location := '1000';
	MEME_UTILITY.drop_it('table',l_rank_table);

    
    MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_fields_all',
    	'Done process map_sg_fields_all',0,1,0,100);

    msp_location := '999';
    MEME_UTILITY.put_message(msp_method||' successfully completed.');

    COMMIT;

EXCEPTION
    WHEN map_sg_fields_all_exc THEN
	error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
	RAISE msp_exception;
    WHEN OTHERS THEN
	error_log(msp_method,msp_location,msp_error_code,SQLERRM);
	RAISE msp_exception;

END map_sg_fields_all;



/* PROCEDURE MAP_SG_DATA *******************************************************
 * This procedure dynamically maps attributes and relationships
 * connected to codes/cuis/sources/etc to the proper atom_id/concept_id.
 */
PROCEDURE map_sg_data(
   authority	   IN VARCHAR2,
   work_id 	   IN INTEGER
)
IS
   TYPE ct IS REF CURSOR;
   cv	ct;
   l_table_name    VARCHAR2(50);
   l_table_id      VARCHAR2(50);
   l_id_type       VARCHAR2(50);
   l_sg_qualifier  VARCHAR2(50) := 'sg_qualifier';
   l_sg_type       VARCHAR2(20) := 'sg_type';
   l_sg_id         VARCHAR2(100) := 'sg_id';
   l_atom_id       VARCHAR2(50) := 'atom_id';
   sg_table	   DBMS_SQL.VARCHAR2_TABLE;
   sty_clause	   DBMS_SQL.VARCHAR2_TABLE;
   tmp_table_1	   VARCHAR2(50);
   tmp_table_2	   VARCHAR2(50);
   l_row_id	   INTEGER;
   l_authority	   VARCHAR2(50);
   retval          INTEGER;
   map_sg_data_exc EXCEPTION;
BEGIN

   MEME_UTILITY.sub_timing_start;

   initialize_trace('MAP_SG_DATA');

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_data',
    	'Starting map_sg_data',0,work_id,0,1);

   --
   -- Make sure insertion indexes exist
   --
   msp_location := '5';
   create_insertion_indexes(authority => authority, work_id => work_id);

   retval := 0;

   -- Define tables to map
   msp_location := '10';
   sg_table(1) := 'attributes';
   sg_table(2) := 'relationships';
   sty_clause(1) := ' and attribute_name != ''SEMANTIC_TYPE''';
   sty_clause(2) := '';

   -- Reset default loop variables
   l_sg_type := 'sg_type';
   l_sg_qualifier := 'sg_qualifier';

   -- Loop thru tables in array to map sg data
   msp_location := '20';
   FOR loop_ctr_1 IN 1..sg_table.LAST LOOP

      -- Map table name and get its primary key and id_type
      msp_location := '20.0';
      l_table_name := sg_table(loop_ctr_1);
      l_table_id := MEME_UTILITY.get_value_by_code(l_table_name,'primary_key');
      l_id_type := MEME_UTILITY.get_code_by_value(l_table_name,'table_name');

      msp_location := '20.1';
      MEME_UTILITY.put_message
         (msp_method||': Mapping '||sg_table(loop_ctr_1)||' ...');

      -- loop to dynamically process table with 1st and 2nd field name
      FOR loop_ctr_2 IN 1..2 LOOP

         IF sg_table(loop_ctr_1) = sg_table(2) THEN
            -- Current table is relationships
            -- Set dynamic loop variables 
            l_sg_id := 'sg_id_'||loop_ctr_2;
            l_atom_id := 'atom_id_'||loop_ctr_2;
            l_sg_type := 'sg_type_'||loop_ctr_2;
            l_sg_qualifier := 'sg_qualifier_'||loop_ctr_2;
         ELSE
            -- Current table is attributes
            -- Should only pass loop_ctr_2 once
            EXIT WHEN loop_ctr_2 > 1;
         END IF;

         msp_location := '20.11';
         -- Get cases where the current sg_id (1 or 2) is CUI or CODE%
         tmp_table_1 := MEME_UTILITY.get_unique_tablename;
         MEME_UTILITY.drop_it('table',tmp_table_1);

	 --
	 -- Re-map all "mappable" types
	 -- except for SOURCE_[CADR]UI
	 -- !attention If the source of the thing is different
	 --            from the sg_qualifier, we should probably re-map it
     --	  	e.g. MEDLINEPLUS connecting to MSH, or SNOMEDCT connecting to ICD
	 --		Or should we rely on "map obsolete data" to handle it?
         EXECUTE IMMEDIATE
            'CREATE TABLE ' || tmp_table_1 || ' (
	 	' || l_table_id || ' ,
                atom_id ,
                sg_id ,
                sg_type ,
                sg_qualifier 
	    ) NOLOGGING as
	       SELECT /*+ PARALLEL(a) */  ' || l_table_id || ', cast(0 as NUMBER(12)), ' || l_sg_id || ', 
		    ' || l_sg_type || ', ' || l_sg_qualifier || '
             FROM ' || sg_table(loop_ctr_1) || ' a
             WHERE tobereleased in (''Y'',''y'')  ' || sty_clause(loop_ctr_1) || '
	       AND ' || l_sg_type || ' IN
               (SELECT code FROM code_map WHERE type = ''map_sg_type'' 
	        AND code NOT IN (SELECT code FROM code_map WHERE type = ''no_remap_sg_type'')) '; 

	 COMMIT;

         msp_location := '20.13';
         --MEME_SYSTEM.analyze(tmp_table_1);

	 --
     -- Call map_sg_fields
	 --
         msp_location := '20.13';
         MEME_UTILITY.put_message
            (msp_method||': Executing MEME_SOURCE_PROCESSING.map_sg_fields ...');
         MEME_SOURCE_PROCESSING.map_sg_fields(
            table_name   => tmp_table_1,
            pair_flag    => MEME_CONSTANTS.NO,
            concept_flag => MEME_CONSTANTS.NO,
            qa_flag      => MEME_CONSTANTS.NO);

         msp_method := 'MAP_SG_DATA';

         --MEME_SYSTEM.analyze(tmp_table_1);

         -- Check where the current atom_id (1 or 2) are different in the */
         -- current table (relationships or attributes) and current tmp_table (1 or 2) */
         msp_location := '20.14';
         MEME_UTILITY.put_message
            (msp_method||': Finding atom_ids that are different in '||
            l_table_name||' ...');

         tmp_table_2 := MEME_UTILITY.get_unique_tablename;
         MEME_UTILITY.drop_it('table',tmp_table_2);

	 -- Before we call macro_action, we need to preserve
	 -- the authority values of the rows we are going to 
	 -- change so we can set them back afterwards.
	 msp_location := '20.14.1';
	 EXECUTE IMMEDIATE
            'CREATE TABLE '||tmp_table_2||' (
		row_id,
		old_value,
		new_value,
		authority ) 
		NOLOGGING AS SELECT /*+ PARALLEL(a) USE_NL(a,b) */ a.' || l_table_id || ' AS row_id,
              	    b.' || l_atom_id || ' AS old_value,
	      	    a.atom_id AS new_value, b.authority 
             FROM ' || tmp_table_1 || ' a, ' || l_table_name || ' b
             WHERE NVL(a.atom_id,0) != 0
              AND a.atom_id != b.' || l_atom_id || '
              AND a.' || l_table_id || ' = b.' || l_table_id;
	
	 COMMIT;

	 --
	 -- Move atoms
	 --
         msp_location := '20.15';
         IF MEME_UTILITY.exec_count(tmp_table_2) > 0 THEN
            MEME_UTILITY.put_message
               (msp_method||': Found atom_ids that are different in '||
               l_table_name||' ...');

            MEME_UTILITY.put_message
               (msp_method||': Executing MEME_BATCH_ACTIONS.macro_action '||
                  'to move atoms ...');

	    msp_location := '20.16';
            retval := MEME_BATCH_ACTIONS.macro_action(
               action     => 'A',
               id_type    => l_id_type,
               authority  => map_sg_data.authority,
               table_name => tmp_table_2,
               status     => 'R',
               work_id    => map_sg_data.work_id);

            IF retval < 0 THEN
	       msp_error_code := 11;
	       msp_error_detail := 'action=A,id_type='||l_id_type;
	       RAISE map_sg_data_exc;
            END IF;

	    -- Put authority values back the way they were
	    msp_location := '20.17';
	    OPEN cv FOR 'SELECT row_id,authority FROM ' || tmp_table_2;
	    LOOP
		FETCH cv INTO l_row_id,l_authority;
		EXIT WHEN cv%NOTFOUND;

		msp_location := '20.18';
		EXECUTE IMMEDIATE 'UPDATE ' || l_table_name || 
		   ' SET authority = :x WHERE ' || l_table_id ||
		   ' = :x '
		USING l_authority, l_row_id;
	   
	    END LOOP;
	    CLOSE cv;

            MEME_UTILITY.log_operation
              (authority,'MEME_SOURCE_PROCESSING.map_sg_data',
               'Change atom ID for '||l_id_type,retval,work_id,0);
         END IF;

         -- Find cases where current atom_id (1 or 2) 
	 -- could not be mapped, set tobereleased = 'n'
         msp_location := '20.16';
         MEME_UTILITY.drop_it('table',tmp_table_2);
         MEME_UTILITY.put_message
            (msp_method||': Finding cases where current atom_id could not be mapped ...');
	 msp_location := '20.16.1';
         EXECUTE IMMEDIATE
            'CREATE TABLE ' || tmp_table_2 || ' 
		     NOLOGGING AS SELECT ' || l_table_id || ' AS row_id
             FROM ' || l_table_name || '
             WHERE tobereleased NOT IN (''N'', ''n'')
             AND ' || l_table_id || ' IN
                (SELECT ' || l_table_id || ' 
		 FROM ' || tmp_table_1 || ' 
	 	 WHERE NVL(atom_id,0) = 0)';

         -- Turn off unmapped ones
         msp_location := '20.17';
         IF MEME_UTILITY.exec_count(tmp_table_2) > 0 THEN
            MEME_UTILITY.put_message
               (msp_method||': Found cases where current atom_id could not be mapped ...');
            MEME_UTILITY.put_message
               (msp_method||': Executing MEME_BATCH_ACTIONS.macro_action '||
                  'to turn off unmapped concepts ...' );

            retval := MEME_BATCH_ACTIONS.macro_action(
               action     => 'T',
               id_type    => l_id_type,
               authority  => map_sg_data.authority,
               table_name => tmp_table_2,
               status     => 'R',
               work_id    => map_sg_data.work_id,
               new_value  => 'n');

            IF retval < 0 THEN
	       msp_error_code := 11;
	       msp_error_detail := 'action=T,id_type='||l_id_type;
	       RAISE map_sg_data_exc;
            END IF;
            MEME_UTILITY.log_operation
              (authority,'MEME_SOURCE_PROCESSING.map_sg_data',
               'Change tobereleased to n for '||l_id_type,retval,work_id,0);
         END IF;

         MEME_UTILITY.drop_it('table',tmp_table_1);
         MEME_UTILITY.drop_it('table',tmp_table_2);

      END LOOP; -- loop_ctr_2

   END LOOP; -- loop_ctr_1

   MEME_UTILITY.sub_timing_stop;
   MEME_UTILITY.log_operation
      (authority,'MEME_SOURCE_PROCESSING.map_sg_data',
       '',0,work_id,MEME_UTILITY.sub_elapsed_time);

   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   drop_insertion_indexes(authority => authority, work_id => work_id);

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::map_sg_data',
    	'Done map_sg_data indexes',0,work_id,0,100);

   COMMIT;

EXCEPTION
   WHEN map_sg_data_exc THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_2);
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;
   WHEN OTHERS THEN
      MEME_UTILITY.drop_it('table',tmp_table_1);
      MEME_UTILITY.drop_it('table',tmp_table_2);
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END map_sg_data;

/* PROCEDURE CREATE_INSERTION_INDEXES *********************************
 * Does NOTHING, indexes made permanent.
 * Call is left around for backwards compatability.
 */
PROCEDURE create_insertion_indexes (
   authority	   IN VARCHAR2,
   work_id 	   IN INTEGER
)
IS
BEGIN
	-- DO NOTHING
    MEME_UTILITY.reset_progress(work_id => work_id);
END create_insertion_indexes;

/* PROCEDURE DROP_INSERTION_INDEXES *********************************
 * Does NOTHING, indexes made permanent.
 * Call is left around for backwards compatability.
 */
PROCEDURE drop_insertion_indexes(
   authority	   IN VARCHAR2,
   work_id 	   IN INTEGER
)
IS
BEGIN
	-- DO NOTHING
    MEME_UTILITY.reset_progress(work_id => work_id);
END drop_insertion_indexes;


/* PROCEDURE SET_ATOM_ORDERING ******************************************************
 * This procedure inserts atoms with their appropriate order_id into atom_ordering.
 * The "ordering" parameter should take the form of:
 * 'DEPTH-FIRST,{CODE|SDUI|SCUI|NONE}' or '{STRING|CODE|SDUI|SCUI|NONE}'
 */
PROCEDURE set_atom_ordering(
   source    IN VARCHAR2,
   ordering  IN VARCHAR2,
   authority IN VARCHAR2,
   work_id   IN INTEGER
)
IS
   rsab	 VARCHAR2(50);
   start_id  INTEGER := 1000000000;
   order_clause VARCHAR2(80);
   equiv_clause VARCHAR2(80);
   l_ordering VARCHAR2(50);
   set_atom_ordering_exc EXCEPTION;

BEGIN

   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::set_atom_ordering',
        'Starting set_atom_ordering',0,work_id,0,1);
   MEME_UTILITY.sub_timing_start;
   initialize_trace('SET_ATOM_ORDERING');

   msp_location :='10.0';
   -- Initialize any needed variables
   SELECT stripped_source INTO rsab
   FROM source_rank WHERE source = set_atom_ordering.source;
   l_ordering := ordering;

   msp_location :='10.1';
   -- Delete any atom_ordering that already exists
   DELETE FROM atom_ordering WHERE root_source = set_atom_ordering.rsab;

   msp_location := '10.21';

   -- Depth-first based ordering, if any; otherwise proceed to next section

   IF (l_ordering LIKE 'DEPTH%') THEN

      -- prune ordering down to second ordering criterion
       msp_location := '10.21b';
       l_ordering := SUBSTR(l_ordering,instr(l_ordering,',')+1);

       msp_location := '10.21c';
       equiv_clause :=
          CASE l_ordering
             WHEN 'CODE' THEN ' AND NVL(a.code,''null'') = NVL(c.code,''null'')'
             WHEN 'SCUI' THEN ' AND NVL(a.source_cui,''null'') = NVL(c.source_cui,''null'')'
             WHEN 'SDUI' THEN ' AND NVL(a.source_dui,''null'') = NVL(c.source_dui,''null'')'
             ELSE ' AND a.atom_id = c.atom_id'
          END;

       msp_location := '10.21d';
       EXECUTE IMMEDIATE
       'INSERT INTO atom_ordering
       SELECT atom_id, :x1 AS root_source,rownum + '||start_id||' as order_id FROM
           (SELECT atom_id FROM
               (SELECT MIN(parent_treenum) as ptr, c.aui a1, a.aui a2, a.atom_id
                FROM classes a, context_relationships b, classes c
                WHERE a.source = :x2 AND a.tobereleased in (''Y'',''y'')
                AND b.source = a.source AND b.tobereleased in (''Y'',''y'')
                AND c.source = a.source AND c.tobereleased in (''Y'',''y'')
                '|| equiv_clause ||'
                AND b.atom_id_1 = c.atom_id
                AND b.relationship_name=''PAR''
                GROUP BY c.aui, a.aui, a.atom_id)
            ORDER BY ptr,a1)'
       USING rsab, source;

       msp_location := '10.21e';
       SELECT NVL(MAX(TO_NUMBER(order_id)),0) INTO start_id FROM atom_ordering
       WHERE root_source = rsab;
       
       msp_location := '10.21f';
       dbms_output.put_line(start_id || 'depth-first atom_ordering entries added for ' || rsab || '.'); 
       
   END IF;


   msp_location := '10.22';

   -- Identifier based ordering:
   -- Check for STRING first (requires join on atoms), then catch all other identifiers.
   -- Unsupported identifiers will default to NONE.


   IF (l_ordering = 'STRING') THEN
      msp_location := '10.22a';
      EXECUTE IMMEDIATE
     'INSERT INTO atom_ordering
      SELECT atom_id, root_source, rownum + :x1 AS order_id FROM
        (SELECT c.atom_id, :x2 AS root_source
         FROM atoms a, classes c
         WHERE c.source = :x3
         AND c.atom_id NOT IN (SELECT atom_id FROM atom_ordering)
         AND a.atom_id = c.atom_id ORDER BY lower(atom_name))'
      USING start_id, rsab, source;

   ELSE
       msp_location := '10.22b';
       order_clause :=
          CASE l_ordering
             WHEN 'CODE' THEN ' ORDER BY code, atom_id'
             WHEN 'SCUI' THEN ' ORDER BY source_cui, atom_id'
             WHEN 'SDUI' THEN ' ORDER BY source_dui, atom_id'
             ELSE ''
          END;

       msp_location := '10.22c';
       EXECUTE IMMEDIATE
       'INSERT INTO atom_ordering
        SELECT atom_id, root_source, rownum + :x1 AS order_id FROM
          (SELECT c.atom_id, :x2 AS root_source
           FROM classes c
           WHERE c.source = :x3
           AND c.atom_id NOT IN (SELECT atom_id FROM atom_ordering)'
           || order_clause || ')'
        USING start_id, rsab, source;
   END IF;

   msp_location := '10.22d';
   MEME_UTILITY.log_progress('MTH','MEME_SOURCE_PROCESSING::set_atom_ordering',
        'Done process resolve_stys',0,work_id,0,100);

   msp_location := '10.22e';
   MEME_UTILITY.put_message(msp_method||' successfully completed.');

   COMMIT;

EXCEPTION

   WHEN set_atom_ordering_exc THEN
      error_log(msp_method,msp_location,msp_error_code,msp_error_detail);
      RAISE msp_exception;

   WHEN OTHERS THEN
      error_log(msp_method,msp_location,msp_error_code,SQLERRM);
      RAISE msp_exception;

END set_atom_ordering;


END meme_source_processing;
/
SHOW ERRORS

set serveroutput on size 100000
execute MEME_SOURCE_PROCESSING.help;
execute MEME_SOURCE_PROCESSING.register_package;
