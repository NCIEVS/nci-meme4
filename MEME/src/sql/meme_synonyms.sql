/*****************************************************************************
*
* File: $INIT_HOME/etc/sql/meme_synonyms.sql
* Author: BAC
*
* Remarks: This script is used to create the MEME oracle tables.
*  
* Version Info:
*   Release: 4
*   Version: 15.0
*   Authority: BAC
*   Date: 05/16/2005
*
* 06/28/2007 JFW (1-EL38F): added obsolete_ui
*  05/16/2005 3.15.0: Released
*  05/12/2005 3.14.1: +src_obsolete_qa_results
*  12/13/2004 3.14.0: Released
*  12/07/2004 3.13.2: +mid_validation_results
*  11/29/2004 3.13.1: +action_log, +aui_history, 
*                     -operations_queue, -authority_rank,groups
*                     -ic_violations
*  03/17/2004 3.13.0: ic_system_status
*  12/02/2003 3.12.1: +test_suite_statistics
*  09/30/2003 3.12.0: -max_action_tab
*  08/29/2003 3.11.1: +inverse_relationships_ui
*  07/18/2003 3.11.0: Released
*  07/09/2003 3.10.1: foreign_attributes, dead_foreign_attributes
*  06/05/2003 3.10.0: attributes_ui, relationships_ui
*  05/12/2003 3.9.0: dumped sg_rels, sg_atts
*  04/11/2003 3.8.1: source_coc_headings, sourcE_coc_subheadings
*  03/12/2003 3.8.0: Released
*  03/04/2003 3.7.1: dumped dba_cutoff and ic_system_status, sg_status,
* 			added system_status, atom_ordering
*  02/18/2003 3.7.0: +source_replacement, allocated_ui_ranges
*  01/17/2003 3.6.1: +source_replacement, allocated_ui_ranges
*  11/27/2002 3.6.0: +reindex_tables, +sims_info, +dead_sims_info
*  09/06/2002 3.5.0: grant execute on MEME packages
*  04/12/2002 3.4.2: synchronized with tables.sql
*  05/04/2001 3.4.1: foreign_classes, dead_foreign_classes
*  04/20/2001 3.4.0: Released to nlm
*  04/03/2001 3.3.4: +coc_headings, +coc_subheadings, +cui_map
*		     -cui_history, +deleted_cuis
*  03/20/2001 3.3.3: +lui_assignment, meme_properties
*  01/09/2001 3.3.2: +mid_validation_queries
*		     mid_qa* synonyms fixed
*  12/15/2000 3.3.1: +cui_history
*  12/05/2000 3.2.1: +dead_context_relationships
*  8/09/2000 3.2.0: qa tables added, meme_schedule added
*		Released to NLM for MEME deployment
*  5/26/2000 3.1.1: re-synced with meme_tables.dat
*  3.1.0: Creates synonyms for mth tables, views and packages
*         URL /MEME/Data/meme_synonyms.sql
* 
*****************************************************************************/
set autocommit on;
/* tables */
DROP PUBLIC SYNONYM action_log;
DROP PUBLIC SYNONYM activity_log;
DROP PUBLIC SYNONYM allocated_ui_ranges;
DROP PUBLIC SYNONYM application_help;
DROP PUBLIC SYNONYM application_versions;
DROP PUBLIC SYNONYM atomic_actions;
DROP PUBLIC SYNONYM atom_ordering;
DROP PUBLIC SYNONYM atoms;
DROP PUBLIC SYNONYM atoms_ui;
DROP PUBLIC SYNONYM attributes;
DROP PUBLIC SYNONYM attributes_ui;
--DROP PUBLIC SYNONYM authority_rank;
--DROP PUBLIC SYNONYM authority_groups;
DROP PUBLIC SYNONYM aui_history;
DROP PUBLIC SYNONYM classes;
DROP PUBLIC SYNONYM coc_headings;
DROP PUBLIC SYNONYM coc_subheadings;
DROP PUBLIC SYNONYM code_map;
DROP PUBLIC SYNONYM concept_status;
DROP PUBLIC SYNONYM context_relationships;
DROP PUBLIC SYNONYM cui_map;
DROP PUBLIC SYNONYM dba_cutoff;
DROP PUBLIC SYNONYM dead_atomic_actions;
DROP PUBLIC SYNONYM dead_atoms;
DROP PUBLIC SYNONYM dead_attributes;
DROP PUBLIC SYNONYM dead_classes;
DROP PUBLIC SYNONYM dead_concept_status;
DROP PUBLIC SYNONYM dead_context_relationships;
DROP PUBLIC SYNONYM dead_foreign_attributes;
DROP PUBLIC SYNONYM dead_foreign_classes;
DROP PUBLIC SYNONYM dead_normwrd;
DROP PUBLIC SYNONYM dead_normstr;
DROP PUBLIC SYNONYM dead_relationships;
DROP PUBLIC SYNONYM dead_sims_info;
DROP PUBLIC SYNONYM dead_stringtab;
DROP PUBLIC SYNONYM dead_word_index;
DROP PUBLIC SYNONYM deleted_cuis;
DROP PUBLIC SYNONYM editing_matrix;
DROP PUBLIC SYNONYM editors;
DROP PUBLIC SYNONYM editor_preferences;
DROP PUBLIC SYNONYM foreign_attributes;
DROP PUBLIC SYNONYM foreign_classes;
DROP PUBLIC SYNONYM ic_applications;
DROP PUBLIC SYNONYM ic_override;
DROP PUBLIC SYNONYM ic_pair;
DROP PUBLIC SYNONYM ic_single;
DROP PUBLIC SYNONYM ic_system_status;
--DROP PUBLIC SYNONYM ic_violations;
DROP PUBLIC SYNONYM integrity_constraints;
DROP PUBLIC SYNONYM inverse_rel_attributes;
DROP PUBLIC SYNONYM inverse_relationships;
DROP PUBLIC SYNONYM inverse_relationships_ui;
--DROP PUBLIC SYNONYM is_handled_actions;
DROP PUBLIC SYNONYM language;
DROP PUBLIC SYNONYM level_status_rank;
DROP PUBLIC SYNONYM lui_assignment;
DROP PUBLIC SYNONYM max_tab;
DROP PUBLIC SYNONYM meme_cluster_history;
DROP PUBLIC SYNONYM meme_error;
DROP PUBLIC SYNONYM meme_ind_columns;
DROP PUBLIC SYNONYM meme_indexes;
DROP PUBLIC SYNONYM meme_progress;
DROP PUBLIC SYNONYM meme_properties;
DROP PUBLIC SYNONYM meme_schedule;
DROP PUBLIC SYNONYM meme_tables;
DROP PUBLIC SYNONYM meme_work;
DROP PUBLIC SYNONYM mid_qa_history;
DROP PUBLIC SYNONYM mid_qa_queries;
DROP PUBLIC SYNONYM mid_qa_results;
DROP PUBLIC SYNONYM mid_validation_results;
DROP PUBLIC SYNONYM mid_validation_queries;
DROP PUBLIC SYNONYM molecular_actions;
DROP PUBLIC SYNONYM mom_safe_replacement;
DROP PUBLIC SYNONYM mom_precomputed_facts;
DROP PUBLIC SYNONYM mom_candidate_facts;
DROP PUBLIC SYNONYM mom_exclude_list;
DROP PUBLIC SYNONYM mom_facts_processed;
DROP PUBLIC SYNONYM mom_merge_facts;
--DROP PUBLIC SYNONYM mom_new_atoms;
DROP PUBLIC SYNONYM mom_norm_exclude_list;
--DROP PUBLIC SYNONYM operations_queue;
DROP PUBLIC SYNONYM qa_adjustment;
DROP PUBLIC SYNONYM qa_diff_adjustment;
DROP PUBLIC SYNONYM qa_diff_results;
DROP PUBLIC SYNONYM obsolete_ui;
DROP PUBLIC SYNONYM nhsty;
DROP PUBLIC SYNONYM normstr;
DROP PUBLIC SYNONYM normwrd;
--DROP PUBLIC SYNONYM reindex_tables;
DROP PUBLIC SYNONYM relationships;
DROP PUBLIC SYNONYM relationships_ui;
DROP PUBLIC SYNONYM released_rank;
DROP PUBLIC SYNONYM semantic_types;
DROP PUBLIC SYNONYM sims_info;
DROP PUBLIC SYNONYM snapshot_results;
--DROP PUBLIC SYNONYM sort_key;
DROP PUBLIC SYNONYM source_attributes;
DROP PUBLIC SYNONYM source_classes_atoms;
DROP PUBLIC SYNONYM source_coc_headings;
DROP PUBLIC SYNONYM source_coc_headings_todelete;
DROP PUBLIC SYNONYM source_coc_subheadings;
DROP PUBLIC SYNONYM source_concept_status;
DROP PUBLIC SYNONYM source_context_relationships;
DROP PUBLIC SYNONYM source_id_map;
--DROP PUBLIC SYNONYM source_inserter_status;
--DROP PUBLIC SYNONYM source_inserter_tables;
--DROP PUBLIC SYNONYM source_integrity_checks;
--DROP PUBLIC SYNONYM source_mapping;
DROP PUBLIC SYNONYM source_rank;
DROP PUBLIC SYNONYM source_relationships;
DROP PUBLIC SYNONYM source_replacement;
DROP PUBLIC SYNONYM source_source_rank;
DROP PUBLIC SYNONYM source_stringtab;
DROP PUBLIC SYNONYM source_string_ui;
DROP PUBLIC SYNONYM source_termgroup_rank;
DROP PUBLIC SYNONYM source_version;
DROP PUBLIC SYNONYM src_qa_queries;
DROP PUBLIC SYNONYM src_qa_results;
DROP PUBLIC SYNONYM src_obsolete_qa_results;
DROP PUBLIC SYNONYM sr_predicate;
DROP PUBLIC SYNONYM srstre2;
DROP PUBLIC SYNONYM srdef;
DROP PUBLIC SYNONYM string_ui;
DROP PUBLIC SYNONYM stringtab;
DROP PUBLIC SYNONYM suppressible_rank;
DROP PUBLIC SYNONYM system_status;
DROP PUBLIC SYNONYM termgroup_rank;
DROP PUBLIC SYNONYM test_suite_statistics;
DROP PUBLIC SYNONYM tobereleased_rank;
DROP PUBLIC SYNONYM word_index;

/* Packages */
DROP PUBLIC SYNONYM MEME_CONSTANTS;
DROP PUBLIC SYNONYM MEME_UTILITY;
DROP PUBLIC SYNONYM MEME_SYSTEM;
DROP PUBLIC SYNONYM MEME_RANKS;
DROP PUBLIC SYNONYM MEME_APROCS;
DROP PUBLIC SYNONYM MEME_BATCH_ACTIONS;
DROP PUBLIC SYNONYM MEME_INTEGRITY_PROC;
DROP PUBLIC SYNONYM MEME_SNAPSHOT_PROC;
DROP PUBLIC SYNONYM MEME_INTEGRITY;
DROP PUBLIC SYNONYM MEME_SOURCE_PROCESSING;
DROP PUBLIC SYNONYM MEME_OPERATIONS;

/* views */
DROP PUBLIC SYNONYM ambig_isui;
DROP PUBLIC SYNONYM separated_strings;
DROP PUBLIC SYNONYM separated_strings_full;
DROP PUBLIC SYNONYM ic_definitions;
DROP PUBLIC SYNONYM srsty;

/* tables */
CREATE PUBLIC SYNONYM action_log FOR mth.action_log;
CREATE PUBLIC SYNONYM activity_log FOR mth.activity_log;
CREATE PUBLIC SYNONYM allocated_ui_ranges FOR mth.allocated_ui_ranges;
CREATE PUBLIC SYNONYM application_help FOR mth.application_help;
CREATE PUBLIC SYNONYM application_versions FOR mth.application_versions;
CREATE PUBLIC SYNONYM atomic_actions FOR mth.atomic_actions;
CREATE PUBLIC SYNONYM atom_ordering FOR mth.atom_ordering;
CREATE PUBLIC SYNONYM atoms FOR mth.atoms;
CREATE PUBLIC SYNONYM atoms_ui FOR mth.atoms_ui;
CREATE PUBLIC SYNONYM attributes FOR mth.attributes;
CREATE PUBLIC SYNONYM attributes_ui FOR mth.attributes_ui;
--CREATE PUBLIC SYNONYM authority_rank FOR mth.authority_rank;
--CREATE PUBLIC SYNONYM authority_groups FOR mth.authority_groups;
CREATE PUBLIC SYNONYM aui_history FOR mth.aui_history;
CREATE PUBLIC SYNONYM classes FOR mth.classes;
CREATE PUBLIC SYNONYM coc_headings FOR mth.coc_headings;
CREATE PUBLIC SYNONYM coc_subheadings FOR mth.coc_subheadings;
CREATE PUBLIC SYNONYM code_map FOR mth.code_map;
CREATE PUBLIC SYNONYM concept_status FOR mth.concept_status;
CREATE PUBLIC SYNONYM context_relationships FOR mth.context_relationships;
CREATE PUBLIC SYNONYM cui_map FOR mth.cui_map;
CREATE PUBLIC SYNONYM dba_cutoff FOR mth.dba_cutoff;
CREATE PUBLIC SYNONYM dead_atomic_actions FOR mth.dead_atomic_actions;
CREATE PUBLIC SYNONYM dead_atoms FOR mth.dead_atoms;
CREATE PUBLIC SYNONYM dead_attributes FOR mth.dead_attributes;
CREATE PUBLIC SYNONYM dead_classes FOR mth.dead_classes;
CREATE PUBLIC SYNONYM dead_concept_status FOR mth.dead_concept_status;
CREATE PUBLIC SYNONYM dead_context_relationships FOR mth.dead_context_relationships;
CREATE PUBLIC SYNONYM dead_sims_info FOR mth.dead_sims_info;
CREATE PUBLIC SYNONYM dead_foreign_attributes FOR mth.dead_foreign_attributes;
CREATE PUBLIC SYNONYM dead_foreign_classes FOR mth.dead_foreign_classes;
CREATE PUBLIC SYNONYM dead_normstr FOR mth.dead_normstr;
CREATE PUBLIC SYNONYM dead_normwrd FOR mth.dead_normwrd;
CREATE PUBLIC SYNONYM dead_relationships FOR mth.dead_relationships;
CREATE PUBLIC SYNONYM dead_stringtab FOR mth.dead_stringtab;
CREATE PUBLIC SYNONYM dead_word_index FOR mth.dead_word_index;
CREATE PUBLIC SYNONYM deleted_cuis FOR mth.deleted_cuis;
CREATE PUBLIC SYNONYM editing_matrix FOR mth.editing_matrix;
CREATE PUBLIC SYNONYM editors FOR mth.editors;
CREATE PUBLIC SYNONYM editor_preferences FOR mth.editor_preferences;
CREATE PUBLIC SYNONYM foreign_attributes FOR mth.foreign_attributes;
CREATE PUBLIC SYNONYM foreign_classes FOR mth.foreign_classes;
CREATE PUBLIC SYNONYM ic_applications FOR mth.ic_applications;
CREATE PUBLIC SYNONYM ic_override FOR mth.ic_override;
CREATE PUBLIC SYNONYM ic_pair FOR mth.ic_pair;
CREATE PUBLIC SYNONYM ic_single FOR mth.ic_single;
CREATE PUBLIC SYNONYM ic_system_status FOR mth.ic_system_status;
--CREATE PUBLIC SYNONYM ic_violations FOR mth.ic_violations;
CREATE PUBLIC SYNONYM integrity_constraints FOR mth.integrity_constraints;
CREATE PUBLIC SYNONYM inverse_rel_attributes FOR mth.inverse_rel_attributes;
CREATE PUBLIC SYNONYM inverse_relationships FOR mth.inverse_relationships;
CREATE PUBLIC SYNONYM inverse_relationships_ui FOR mth.inverse_relationships_ui;
--CREATE PUBLIC SYNONYM is_handled_actions FOR mth.is_handled_actions;
CREATE PUBLIC SYNONYM language FOR mth.language;
CREATE PUBLIC SYNONYM level_status_rank FOR mth.level_status_rank;
CREATE PUBLIC SYNONYM lui_assignment FOR mth.lui_assignment;
CREATE PUBLIC SYNONYM max_tab FOR mth.max_tab;
CREATE PUBLIC SYNONYM meme_cluster_history FOR mth.meme_cluster_history;
CREATE PUBLIC SYNONYM meme_error FOR mth.meme_error;
CREATE PUBLIC SYNONYM meme_ind_columns FOR mth.meme_ind_columns;
CREATE PUBLIC SYNONYM meme_indexes FOR mth.meme_indexes;
CREATE PUBLIC SYNONYM meme_progress FOR mth.meme_progress;
CREATE PUBLIC SYNONYM meme_properties FOR mth.meme_properties;
CREATE PUBLIC SYNONYM meme_schedule FOR mth.meme_schedule;
CREATE PUBLIC SYNONYM meme_tables FOR mth.meme_tables;
CREATE PUBLIC SYNONYM meme_work FOR mth.meme_work;
CREATE PUBLIC SYNONYM mid_qa_history FOR mth.mid_qa_history;
CREATE PUBLIC SYNONYM mid_qa_queries FOR mth.mid_qa_queries;
CREATE PUBLIC SYNONYM mid_qa_results FOR mth.mid_qa_results;
CREATE PUBLIC SYNONYM mid_validation_results FOR mth.mid_validation_results;
CREATE PUBLIC SYNONYM mid_validation_queries FOR mth.mid_validation_queries;
CREATE PUBLIC SYNONYM molecular_actions FOR mth.molecular_actions;
CREATE PUBLIC SYNONYM mom_safe_replacement FOR mth.mom_safe_replacement;
CREATE PUBLIC SYNONYM mom_precomputed_facts FOR mth.mom_precomputed_facts;
CREATE PUBLIC SYNONYM mom_candidate_facts FOR mth.mom_candidate_facts;
CREATE PUBLIC SYNONYM mom_exclude_list FOR mth.mom_exclude_list;
CREATE PUBLIC SYNONYM mom_facts_processed FOR mth.mom_facts_processed;
CREATE PUBLIC SYNONYM mom_merge_facts FOR mth.mom_merge_facts;
--CREATE PUBLIC SYNONYM mom_new_atoms FOR mth.mom_new_atoms;
CREATE PUBLIC SYNONYM mom_norm_exclude_list FOR mth.mom_norm_exclude_list;
--CREATE PUBLIC SYNONYM operations_queue FOR mth.operations_queue;
CREATE PUBLIC SYNONYM qa_adjustment FOR mth.qa_adjustment;
CREATE PUBLIC SYNONYM qa_diff_adjustment FOR mth.qa_diff_adjustment;
CREATE PUBLIC SYNONYM qa_diff_results FOR mth.qa_diff_results;
CREATE PUBLIC SYNONYM obsolete_ui FOR mth.obsolete_ui;
CREATE PUBLIC SYNONYM nhsty FOR mth.nhsty;
CREATE PUBLIC SYNONYM normstr FOR mth.normstr;
CREATE PUBLIC SYNONYM normwrd FOR mth.normwrd;
--CREATE PUBLIC SYNONYM reindex_tables FOR mth.reindex_tables;
CREATE PUBLIC SYNONYM relationships FOR mth.relationships;
CREATE PUBLIC SYNONYM relationships_ui FOR mth.relationships_ui;
CREATE PUBLIC SYNONYM released_rank FOR mth.released_rank;
CREATE PUBLIC SYNONYM semantic_types FOR mth.semantic_types;
CREATE PUBLIC SYNONYM sims_info FOR mth.sims_info;
CREATE PUBLIC SYNONYM snapshot_results FOR mth.snapshot_results;
--CREATE PUBLIC SYNONYM sort_key FOR mth.sort_key;
CREATE PUBLIC SYNONYM source_attributes FOR mth.source_attributes;
CREATE PUBLIC SYNONYM source_classes_atoms FOR mth.source_classes_atoms;
CREATE PUBLIC SYNONYM source_coc_headings FOR mth.source_coc_headings;
CREATE PUBLIC SYNONYM source_coc_headings_todelete FOR mth.source_coc_headings_todelete;
CREATE PUBLIC SYNONYM source_coc_subheadings FOR mth.source_coc_subheadings;
CREATE PUBLIC SYNONYM source_concept_status FOR mth.source_concept_status;
CREATE PUBLIC SYNONYM source_context_relationships FOR mth.source_context_relationships;
CREATE PUBLIC SYNONYM source_id_map FOR mth.source_id_map;
--CREATE PUBLIC SYNONYM source_inserter_status FOR mth.source_inserter_status;
--CREATE PUBLIC SYNONYM source_inserter_tables FOR mth.source_inserter_tables;
--CREATE PUBLIC SYNONYM source_integrity_checks FOR mth.source_integrity_checks;
--CREATE PUBLIC SYNONYM source_mapping FOR mth.source_mapping;
CREATE PUBLIC SYNONYM source_rank FOR mth.source_rank;
CREATE PUBLIC SYNONYM source_relationships FOR mth.source_relationships;
CREATE PUBLIC SYNONYM source_replacement FOR mth.source_replacement;
CREATE PUBLIC SYNONYM source_source_rank FOR mth.source_source_rank;
CREATE PUBLIC SYNONYM source_stringtab FOR mth.source_stringtab;
CREATE PUBLIC SYNONYM source_string_ui FOR mth.source_string_ui;
CREATE PUBLIC SYNONYM source_termgroup_rank FOR mth.source_termgroup_rank;
CREATE PUBLIC SYNONYM source_version FOR mth.source_version;
CREATE PUBLIC SYNONYM src_qa_queries FOR mth.src_qa_queries;
CREATE PUBLIC SYNONYM src_obsolete_qa_results FOR mth.src_obsolete_qa_results;
CREATE PUBLIC SYNONYM src_qa_results FOR mth.src_qa_results;
CREATE PUBLIC SYNONYM sr_predicate FOR mth.sr_predicate;
CREATE PUBLIC SYNONYM srstre2 FOR mth.srstre2;
CREATE PUBLIC SYNONYM srdef FOR mth.srdef;
CREATE PUBLIC SYNONYM string_ui FOR mth.string_ui;
CREATE PUBLIC SYNONYM stringtab FOR mth.stringtab;
CREATE PUBLIC SYNONYM suppressible_rank FOR mth.suppressible_rank;
CREATE PUBLIC SYNONYM system_status FOR mth.system_status;
CREATE PUBLIC SYNONYM termgroup_rank FOR mth.termgroup_rank;
CREATE PUBLIC SYNONYM test_suite_statistics FOR mth.test_suite_statistics;
CREATE PUBLIC SYNONYM tobereleased_rank FOR mth.tobereleased_rank;
CREATE PUBLIC SYNONYM word_index FOR mth.word_index;

/* Packages */
CREATE PUBLIC SYNONYM MEME_CONSTANTS FOR mth.MEME_CONSTANTS;
CREATE PUBLIC SYNONYM MEME_UTILITY FOR mth.MEME_UTILITY;
CREATE PUBLIC SYNONYM MEME_SYSTEM FOR mth.MEME_SYSTEM;
CREATE PUBLIC SYNONYM MEME_RANKS FOR mth.MEME_RANKS;
CREATE PUBLIC SYNONYM MEME_APROCS FOR mth.MEME_APROCS;
CREATE PUBLIC SYNONYM MEME_BATCH_ACTIONS FOR mth.MEME_BATCH_ACTIONS;
CREATE PUBLIC SYNONYM MEME_INTEGRITY_PROC FOR mth.MEME_INTEGRITY_PROC;
CREATE PUBLIC SYNONYM MEME_SNAPSHOT_PROC FOR mth.MEME_SNAPSHOT_PROC;
CREATE PUBLIC SYNONYM MEME_INTEGRITY FOR mth.MEME_INTEGRITY;
CREATE PUBLIC SYNONYM MEME_SOURCE_PROCESSING FOR mth.MEME_SOURCE_PROCESSING;
CREATE PUBLIC SYNONYM MEME_OPERATIONS FOR mth.MEME_OPERATIONS;

GRANT EXECUTE ON MEME_CONSTANTS TO PUBLIC;
GRANT EXECUTE ON MEME_UTILITY TO PUBLIC;
GRANT EXECUTE ON MEME_SYSTEM TO PUBLIC;
GRANT EXECUTE ON MEME_RANKS TO PUBLIC;
GRANT EXECUTE ON MEME_APROCS TO PUBLIC;
GRANT EXECUTE ON MEME_BATCH_ACTIONS TO PUBLIC;
GRANT EXECUTE ON MEME_INTEGRITY_PROC TO PUBLIC;
GRANT EXECUTE ON MEME_SNAPSHOT_PROC TO PUBLIC;
GRANT EXECUTE ON MEME_INTEGRITY TO PUBLIC;
GRANT EXECUTE ON MEME_SOURCE_PROCESSING TO PUBLIC;
GRANT EXECUTE ON MEME_OPERATIONS TO PUBLIC;

/* views */
CREATE PUBLIC SYNONYM ambig_isui FOR mth.ambig_isui;
CREATE PUBLIC SYNONYM separated_strings FOR mth.separated_strings;
CREATE PUBLIC SYNONYM separated_strings_full FOR mth.separated_strings_full;
CREATE PUBLIC SYNONYM ic_definitions FOR mth.ic_definitions;
CREATE PUBLIC SYNONYM srsty FOR mth.srsty;


