/*****************************************************************************
*
* File:  $INIT_HOME/etc/sql/meme_indexes.sql
* Author:  EMW, BAC, others
*
* Remarks:  This script is used to create the indexes on the MEME tables
*	    Note:  It also creates the table meme_indexes & meme_ind_columns.
*
* Changes
*  03/15/2006 BAC (1-AOH1T): x_attr_an now on attribute_name, attribute_value
*
* Version Info:
*   Release: 4
*   Version: 27.0
*   Authority: Brian Carlsen 
*   Date: 12/07/2004
*
* 12/13/2004 3.27.0: Released
* 12/07/2004 3.26.3: +mid_validation_results, +aui_history
* 11/29/2004 3.26.2: +ael_pk, -operations_queue, -ic_violations
* 11/09/2004 3.26.1: Removed foreign_attributes indexes
* 10/06/2004 3.26.0: Released
* 09/29/2004 3.25.1: more primary keys (language, editors, ep, code_map)
* 09/20/2004 3.25.1: +cui_map_pk
* 08/09/2004 3.25.0: Released
* 08/02/2004 3.24.1: + classes.last_release_cui, dead_classes.lrc
* 07/06/2004 3.24.0: Released, +content_view_members
* 06/15/2004 3.23.1: +atom_ordering indexes
* 04/28/2004 3.23.0: released
* 04/12/2004 3.22.2: +code index on source_classes_atoms
* 03/30/2004 3.22.1: attributes ATN bitmap index
* 03/17/2004 3.22.0: coc_headings primary key
* 02/20/2004 3.21.0: source_replacement revised
* 02/06/2004 3.20.0: Released
* 02/04/2004 3.19.2: +source_replacement
* 12/19/2003 3.19.1: for relationships_ui, attribute_ui
*                    index id/type not just id
* 09/08/2003 3.18.0: +ir_ui_pk
* 08/01/2003 3.17.0: Released
* 07/31/2003 3.16.1: Additional indexes:
*                      source_classes_atoms.(source_cui, source_aui)
*		       source_relationships.source_rui
*                      molecular_actions.transaction_id
* 07/18/2003 3.16.0: Released
* 07/09/2003 3.15.1: indexes for {dead_,}foreign_attributes
* 06/16/2003 3.15.0: Final schema used for 2003AB release
* 06/05/2003 3.14.0: Release
* 06/03/2003 3.13.0: attributes_ui, relationships_ui do not have PKs
* 05/12/2003 3.13.0: Released
* 05/02/2003 3.12.3: +attributes_ui, relationships_ui, -sg_rels, sg_atts
* 04/11/2003 3.12.2: indexes on source_coc_{sub,}headings
* 03/17/2003 3.12.1: +relationships_ui
* 11/27/2002 3.12.0: Released.
* 04/16/2002 3.11.0: Released.
* 04/12/2002 3.10.1: +atoms_ui
* 12/05/2001 3.10.0: Released to nlm for oa_mid2002
* 11/19/2001 3.9.1:  Added sims_info table.
* 09/05/2001 3.9.0:  Released to NLM just to ensure that versions are
*		     synchronized
* 06/04/2001 3.8.0:  Released
* 05/04/2001 3.7.1:  indexes for {dead_,}foreign_classes
* 04/20/2001 3.7.0:  Released to NLM
* 04/03/2001 3.6.3:  coc_headings,subheadings
* 03/16/2001 3.6.2:  indexes for context_relationships (x_cr_pn)
* 12/14/2000 3.6.1:  dead_context_relationships_pk in MIDI
* 12/11/2000 3.6.0:  Released to NLM
* 12/05/2000 3.5.1:  context_relationships_pk, mid_qa_history, mid_qa_results
* 11/03/2000 3.5.0:  Changes released to NLM
* 10/24/2000 3.4.3:  INTIAL extents adjusted to account for growing segments
* 8/24/2000  3.4.2:  classes atom_id is primary key, concept_id is secondary index
* 8/16/2000  3.4.1:  concept_status.preferred_atom_id index added,
* 		     increased storage parameters
* 8/09/2000  3.4.0:    Released for MEME deployment
* 8/07/2000  3.3.61: x_classes_isui: include concept_id for ambig_isui view
* 6/29/2000  3.3.6:  additional primary keys: atomic_actions, stringtab 
* 6/21/2000  3.3.5:  rebuild primary keys in MIDI
* 6/07/2000  3.3.4:  use default STORAGE from tablespace
*                    estimate for large indexes to reduce fragmentation
* 5/26/2000  3.3.3:  hetero_relationships->sg_relationships,
*                    source_source_rank, source_termgroup_rank
* 5/17/2000  3.3.2:  added operations_queue
* 5/16/2000  3.3.1:  new indexes for source_classes_atoms & mom_safe_repl
* 5/9/2000   3.3.0:  Released to URL /MEME/Data/meme_indexes.sql
* 03/28/2000 3.2.2:  Support for sg relationships
* 
* 
*****************************************************************************/


------------------------------------------------------------------------
-- PRIMARY KEY INDEXES
------------------------------------------------------------------------
-- Rebuild primary key indexes in MIDI tablespace

ALTER INDEX ael_pk REBUILD STORAGE (INITIAL 50M) TABLESPACE MIDI;

ALTER INDEX atomic_actions_pk REBUILD STORAGE (INITIAL 200M) TABLESPACE MIDI;

ALTER INDEX atoms_pk REBUILD STORAGE (INITIAL 60M) TABLESPACE MIDI;

ALTER INDEX atoms_ui_pk REBUILD STORAGE (INITIAL 200M) TABLESPACE MIDI;

ALTER INDEX attributes_pk REBUILD STORAGE (INITIAL 150M) TABLESPACE MIDI;

ALTER INDEX classes_pk REBUILD STORAGE (INITIAL 100M) TABLESPACE MIDI;

--ALTER INDEX coc_headings_pk REBUILD STORAGE (INITIAL 100M) TABLESPACE MIDI;
ALTER INDEX coc_subheadings_pk REBUILD STORAGE (INITIAL 100M) TABLESPACE MIDI;

ALTER INDEX code_map_pk REBUILD TABLESPACE MIDI;

ALTER INDEX concept_status_pk REBUILD STORAGE (INITIAL 40M) TABLESPACE MIDI;

ALTER INDEX context_relationships_pk REBUILD
	STORAGE (INITIAL 350M) TABLESPACE MIDI;

ALTER INDEX cui_map_pk REBUILD STORAGE (INITIAL 30M) TABLESPACE MIDI;

ALTER INDEX dead_context_relationships_pk REBUILD TABLESPACE MIDI;

ALTER INDEX dead_atoms_pk REBUILD STORAGE (INITIAL 10M) TABLESPACE MIDI;

ALTER INDEX dead_attributes_pk REBUILD STORAGE (INITIAL 60M) TABLESPACE MIDI;

ALTER INDEX dead_classes_pk REBUILD STORAGE (INITIAL 10M) TABLESPACE MIDI;

ALTER INDEX dead_concept_status_pk REBUILD 
	STORAGE (INITIAL 10M) TABLESPACE MIDI;

ALTER INDEX d_f_classes_pk REBUILD STORAGE (INITIAL 10M) TABLESPACE MIDI;

ALTER INDEX dead_stringtab_pk REBUILD STORAGE (INITIAL 20M) TABLESPACE MIDI;

ALTER INDEX dead_relationships_pk REBUILD 
	STORAGE (INITIAL 10M) TABLESPACE MIDI;

ALTER INDEX editing_matrix_pk REBUILD STORAGE (INITIAL 20M) TABLESPACE MIDI;

ALTER INDEX editors_pk REBUILD TABLESPACE MIDI;

ALTER INDEX editor_preferences_pk REBUILD TABLESPACE MIDI;

ALTER INDEX f_classes_pk REBUILD STORAGE (INITIAL 50M) TABLESPACE MIDI;

ALTER INDEX sims_info_pk REBUILD TABLESPACE MIDI;

ALTER INDEX ir_ui_pk REBUILD STORAGE (INITIAL 50M) TABLESPACE MIDI;

-- this index was removed
-- ALTER INDEX inverse_relationships_pk REBUILD TABLESPACE MIDI;

ALTER INDEX language_pk REBUILD TABLESPACE MIDI;

ALTER INDEX molecular_actions_pk REBUILD STORAGE (INITIAL 80M) TABLESPACE MIDI;

ALTER INDEX stringtab_pk REBUILD STORAGE (INITIAL 40M) TABLESPACE MIDI;

ALTER INDEX oq_pk REBUILD STORAGE (INITIAL 10M) TABLESPACE MIDI;

ALTER INDEX relationships_pk REBUILD STORAGE (INITIAL 80M) TABLESPACE MIDI;

ALTER INDEX source_rank_pk REBUILD TABLESPACE MIDI;

ALTER INDEX string_ui_pk REBUILD STORAGE (INITIAL 60M) TABLESPACE MIDI;

ALTER INDEX termgroup_rank_pk REBUILD TABLESPACE MIDI;


------------------------------------------------------------------------
-- SECONDARY INDEXES
------------------------------------------------------------------------
-- primary key now
-- CREATE INDEX x_aa_aid ON atomic_actions(atomic_action_id)
--PCTFREE 10 STORAGE (INITIAL 40M) TABLESPACE MIDI;

-- This table is IOT
--CREATE INDEX x_cocsh_csi_ssi ON coc_subheadings
--(citation_set_id, subheading_set_id)
--PCTFREE 10 STORAGE (INITIAL 1200M) TABLESPACE MIDI;

CREATE INDEX x_al_trid ON action_log (transaction_id)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_al_wid ON action_log (work_id)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE BITMAP INDEX x_atom_ordering_rs ON atom_ordering (root_source)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_atom_ordering_atom_id ON atom_ordering (atom_id)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_atom_ordering_order_id ON atom_ordering (order_id)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_aui_sui ON atoms_ui (sui)
PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MIDI;

CREATE INDEX x_atui_atui ON attributes_ui (atui)
PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MIDI;

CREATE INDEX x_atui_sg_id ON attributes_ui (sg_id,sg_type)
PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MIDI;

CREATE INDEX x_rui_rui ON relationships_ui (rui)
PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MIDI;

CREATE INDEX x_rui_sg_id_1 ON relationships_ui (sg_id_1,sg_type_1)
PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MIDI;

CREATE INDEX x_rui_sg_id_2 ON relationships_ui (sg_id_2,sg_type_2)
PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MIDI;

CREATE INDEX x_coch_cid ON coc_headings (citation_set_id)
PCTFREE 10 STORAGE (INITIAL 100M NEXT 100M) TABLESPACE MIDI;

CREATE INDEX x_coch_heading_id ON coc_headings (heading_id)
PCTFREE 10 STORAGE (INITIAL 100M NEXT 100M) TABLESPACE MIDI;

CREATE INDEX x_cs_pid ON concept_status(preferred_atom_id)
PCTFREE 10 STORAGE (INITIAL 40M) TABLESPACE MIDI;

CREATE INDEX x_aa_mid ON atomic_actions(molecule_id)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

CREATE INDEX x_aa_rid ON atomic_actions(row_id)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

CREATE INDEX x_attr_aid ON attributes(atom_id)
PCTFREE 10 STORAGE (INITIAL 120M) TABLESPACE MIDI;

CREATE INDEX x_attr_cid ON attributes(concept_id)
PCTFREE 10 STORAGE (INITIAL 120M) TABLESPACE MIDI;

-- 400M
--CREATE BITMAP INDEX x_attr_an ON attributes(attribute_name)
CREATE INDEX x_attr_an ON attributes(attribute_name,attribute_value)
PCTFREE 10 STORAGE (INITIAL 400M) TABLESPACE MIDI PARALLEL;

-- 200 Md
CREATE INDEX x_atoms_an ON atoms(atom_name)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

CREATE INDEX x_classes_cid ON classes(concept_id)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_classes_lrc ON classes(last_release_cui)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_classes_code ON classes(code)
PCTFREE 10 STORAGE (INITIAL 60M) TABLESPACE MIDI;

CREATE INDEX x_classes_sui ON classes(sui)
PCTFREE 10 STORAGE (INITIAL 60M) TABLESPACE MIDI;

CREATE INDEX x_classes_lui ON classes(lui)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_classes_isui ON classes(isui, concept_id)
PCTFREE 10 STORAGE (INITIAL 120M) TABLESPACE MIDI;

CREATE BITMAP INDEX x_classes_source ON classes(source,tty)
PCTFREE 10 STORAGE (INITIAL 60M) TABLESPACE MIDI;

CREATE INDEX x_cs_cui on concept_status (cui)
PCTFREE 10 STORAGE (INITIAL 40M) TABLESPACE MIDI;
	
CREATE INDEX x_dns_nsid ON dead_normstr(normstr_id)
PCTFREE 10 TABLESPACE MIDI;

CREATE INDEX x_dnw_nwid ON dead_normwrd(normwrd_id)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_dwi_tid ON dead_word_index(atom_id)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

-- primary key
--CREATE INDEX x_dead_stringtab_sid_rs ON dead_stringtab(string_id, row_sequence)
--PCTFREE 10 TABLESPACE MIDI;

CREATE INDEX x_cvm_mui ON content_view_members(meta_ui)
PCTFREE 10 STORAGE (INITIAL 300M) TABLESPACE MIDI;

CREATE INDEX x_cr_a1 ON context_relationships(atom_id_1)
PCTFREE 10 STORAGE (INITIAL 300M) TABLESPACE MIDI;

CREATE INDEX x_cr_a2 ON context_relationships(atom_id_2)
PCTFREE 10 STORAGE (INITIAL 300M) TABLESPACE MIDI;

--CREATE INDEX x_cr_pn ON context_relationships(parent_treenum)
--PCTFREE 10 STORAGE (INITIAL 500M) TABLESPACE MIDI;

CREATE INDEX x_foreign_classes_eaid ON foreign_classes(eng_atom_id)
PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI;

--CREATE INDEX x_foreign_attributes_aid ON foreign_attributes(atom_id)
--PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI;

CREATE INDEX x_mch_1 ON meme_cluster_history (concept_id_1)
PCTFREE 10 STORAGE (INITIAL 20M) TABLESPACE MIDI;

CREATE INDEX x_mch_2 ON meme_cluster_history (concept_id_2)
PCTFREE 10 STORAGE (INITIAL 20M) TABLESPACE MIDI;

CREATE INDEX x_mvr ON mid_validation_results (result_set_name)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_mqh ON mid_qa_history (qa_id,name,value)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_mqr ON mid_qa_results (qa_id,name,value)
PCTFREE 10 TABLESPACE MIDI;

CREATE INDEX x_ma_tid ON molecular_actions(target_id)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_ma_trid ON molecular_actions(transaction_id)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_ma_sid ON molecular_actions(source_id)
PCTFREE 10 STORAGE (INITIAL 90M) TABLESPACE MIDI;

CREATE INDEX x_msr_1 ON mom_safe_replacement (new_atom_id)
PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI;

CREATE INDEX x_msr_2 ON mom_safe_replacement (old_atom_id)
PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI;

CREATE INDEX x_msr_3 ON mom_safe_replacement (source)
PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI;

CREATE INDEX x_mmf_id ON mom_merge_facts (merge_fact_id)
PCTFREE 10 TABLESPACE MIDI;

CREATE INDEX x_mmf_atom_id_1 ON mom_merge_facts (atom_id_1)
PCTFREE 10 TABLESPACE MIDI;

CREATE INDEX x_mmf_atom_id_2 ON mom_merge_facts (atom_id_2)
PCTFREE 10 TABLESPACE MIDI;

CREATE INDEX x_mfp_s_ms ON mom_facts_processed (source,merge_set)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

CREATE INDEX x_nw_nwid ON normwrd(normwrd_id)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

-- 200M
CREATE INDEX x_nw_nw ON normwrd(normwrd)
PCTFREE 10 STORAGE (INITIAL 250M) TABLESPACE MIDI;

CREATE INDEX x_ns_nsid ON normstr (normstr_id)
PCTFREE 10 STORAGE (INITIAL 100M) TABLESPACE MIDI;

-- 200M
CREATE INDEX x_ns_ns ON normstr (normstr)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

CREATE INDEX x_r_a1 ON relationships(atom_id_1)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE INDEX x_r_a2 ON relationships(atom_id_2)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE index x_r_c1 ON relationships(concept_id_1)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

CREATE index x_r_c2 ON relationships(concept_id_2)
PCTFREE 10 STORAGE (INITIAL 80M) TABLESPACE MIDI;

-- 200 M
CREATE index x_sim_lrid ON source_id_map(local_row_id)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

-- 200 M
CREATE index x_sim_srid ON source_id_map(source_row_id)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

-- Primary key
--CREATE INDEX x_su_sui ON string_ui (sui)
--PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI;

CREATE INDEX x_su_lui ON string_ui (lui)
PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI;

CREATE INDEX x_su_isui ON string_ui (isui)
PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI;

-- 200 M
CREATE INDEX x_su_nsp ON string_ui (norm_string_pre)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

-- 200 M
CREATE INDEX x_su_sp ON string_ui (string_pre)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

-- 200 M
CREATE INDEX x_su_lp ON string_ui (lowercase_string_pre)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

CREATE INDEX x_ssu_nsp ON source_string_ui (norm_string_pre)
PCTFREE 10 STORAGE (INITIAL 40M) TABLESPACE MIDI;

CREATE INDEX x_ssu_sp ON source_string_ui (string_pre)
PCTFREE 10 STORAGE (INITIAL 40M) TABLESPACE MIDI;

CREATE INDEX x_ssu_lp ON source_string_ui (lowercase_string_pre)
PCTFREE 10 STORAGE (INITIAL 40M) TABLESPACE MIDI;

-- 200 M
--CREATE INDEX x_stringtab_sid_rs ON stringtab(string_id, row_sequence)
--PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

CREATE INDEX x_wi_ai ON word_index (atom_id)
PCTFREE 10 STORAGE (INITIAL 200M) TABLESPACE MIDI;

-- 200 M
CREATE INDEX x_wi_wd ON word_index (word)
PCTFREE 10 STORAGE (INITIAL 250M) TABLESPACE MIDI;

CREATE INDEX x_srstre2_sty1 ON srstre2 ( sty1 )
PCTFREE 10 TABLESPACE MIDI;

CREATE INDEX x_srstre2_sty2 ON srstre2 ( sty2 )
PCTFREE 10 TABLESPACE MIDI;

CREATE INDEX x_sca_1 on source_classes_atoms (atom_id)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_sca_code on source_classes_atoms (code)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_sca_saui on source_classes_atoms (source_aui)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_sca_scui on source_classes_atoms (source_cui)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_sca_2 on source_classes_atoms (concept_id)
PCTFREE 10 STORAGE (INITIAL 30M) TABLESPACE MIDI;

CREATE INDEX x_sch_1 on source_coc_headings (citation_set_id)
PCTFREE 10 STORAGE (INITIAL 20M) TABLESPACE MIDI;

CREATE INDEX x_scsh_1 on source_coc_subheadings (citation_set_id)
PCTFREE 10 STORAGE (INITIAL 20M) TABLESPACE MIDI;

CREATE INDEX x_scs_1 on source_concept_status (concept_id)
PCTFREE 10 STORAGE (INITIAL 20M) TABLESPACE MIDI;

CREATE INDEX x_sa_1 on source_attributes (attribute_id)
PCTFREE 10 STORAGE (INITIAL 50M) TABLESPACE MIDI;

CREATE INDEX x_sr_1 on source_relationships (relationship_id)
PCTFREE 10 STORAGE (INITIAL 30M) TABLESPACE MIDI;

CREATE INDEX x_sr_srui on source_relationships (source_rui)
PCTFREE 10 STORAGE (INITIAL 30M) TABLESPACE MIDI;

CREATE INDEX x_scr_1 on source_context_relationships (relationship_id)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_source_repl_id on source_replacement 
  (sg_meme_id,sg_meme_data_type)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_cui_history on cui_history(cui1)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;

CREATE INDEX x_aui_history1 on aui_history(aui1)
PCTFREE 10 STORAGE (INITIAL 10M) TABLESPACE MIDI;
