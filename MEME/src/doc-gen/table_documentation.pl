#  Must be invoked via "perl table_documentation.pl tables.sql.file"
#
# Changes
# 03/03/2006 RBE (1-AJV1Z): Fixed SQL injection error
# 12/22/2005 BAC (1-719SM): use open ":utf8" added
#
# This script generates an HTML page that 
# is a template for documenting the schema.
#

use DBI;
use DBD::Oracle;
use open ":utf8";

#
# Default comments associated with field names
#
%fields_to_comments = 
  (
   "result_count" => qq{the count resulting from a semantic validation check},
   "result_set_name" => qq{the name of a set of results (like a log file name)},
   "contributor" => qq{the contributor of the content view},
   "is_current" => qq{indicates whether or not the content view is current},
   "is_complete" => qq{indicates whether or not the content view is complete},
   "content_view_id" => qq{unique identifier for a content view},
   "content_view_name" => qq{descriptive name for a content view},
   "content_view_descripttion" => qq{up to paragraph long description of a content view},
   "content_view_algorithm" => qq{SQL query or PL/SQL block implementing the content view.  In other words, running this code loads the members table for this content view},
   "content_view_category" => qq{category to which the content view belongs.  Indicates how it is maintained and how important it is.  For example, REGULATORY is used to indicate a content view created for regulatory compliance},
   "content_view_code" => qq{power of 2, indicates the bit of the CVF field for this content view},
   "cascade" => qq{<tt>Y/N</tt> value indicating whether or not connections are followed.  For example, if an AUI is in a set, are rels connected to that AUI in the set?},
   "meta_ui" => qq{a Metathesaurus identifier (AUI, RUI, ATUI, CUI)},
   "set_name" => qq{the name of the test suite set},
   "suite_name" => qq{the name of the test suite},
   "test_name" => qq{the name of the test},
   "test_insertion_start" => qq{the start date of a the test insertion for this source},
   "test_insertion_end" => qq{the end date of a the test insertion for this source},
   "real_insertion_start" => qq{the start date of a the real insertion for this source},
   "real_insertion_end" => qq{the end date of a the real insertion for this source},
   "editing_start" => qq{the date when editing of this source started},
   "editing_end" => qq{the date when editing of this source was completed},
   "latest_available" => qq{the most recent version of the source},
   "internal_url_list" => qq{a list of URLs related to this source, for internal consumption},
   "release_url_list" => qq{a list of URLs related to this source, for release to the public},
   "character_set" => qq{an ISO character set name},
   "license_info" => qq{information about obtaining a license for this source},
   "last_contacted" => qq{the date the source provider was last contacted regarding this source},
   "citation" => qq{citation information},
   "source_short_name" => qq{the short name (used by UMLSKS) for this source},
   "source_official_name" => qq{the SRC/VPT name for this source},
   "root_cui" => qq{the CUI assigned to the root SRC concept representing this source in the last release},
   "versioned_cui" => qq{the CUI assigned to the root SRC concept representing this source in the last release},
   "term_type_list" => qq{a comma separated list of TTY values},
   "attribute_name_list" => qq{a comma separated list of ATN values},
   "term_frequency" => qq{CUI,SUI frequency of this source in the last release}, 
   "cui_frequency" => qq{CUI frequency of this source in the last release}, 
   "rel_directionality_flag" => qq{a <tt>Y/N</tt> flag indicating whether or not this source asserts the direction of its relationships},
   "order_id" => qq{a field to sort on to provide the recommended ordering of content in this table},
   "make_checklist" => qq{a <Tt>Y/N</tt> flag indicating whether or not checklists can be built from the query},
   "low_ui" => qq{the lower bound of a unique identifier range},
   "high_ui" => qq{the upper bound of a unique identifier range},
   "aui" => qq{Metathesaurus "Atom Unique Identifier".  Computed in the MID and based on the following values: root source (RSAB), SUI, code, term type, source AUI, source CUI, source DUI.  Intended as a stable atom identifier across different versions of the same source. [Range: must be in <a href="atoms_ui.html"><tt>atoms_ui.aui</tt></a>]},
   "aui_1" => qq{The first Metathesaurus "Atom Unique Identifier", if connected to an atom.  Otherwise null.},
   "aui_2" => qq{The second Metathesaurus "Atom Unique Identifier", if connected to an atom.  Otherwise null.},
   "atui" => qq{Metathesaurus "ATtribute Unique Identifier".  Computed in the MID and based on the following: root source (RSAB), attribute name, hashcode, source ATUI, and the "native source identifier" that the attribute is connected to. [Range: must be in <a href="attributes_ui.html"><tt>attributes_ui.atui</tt></a>]},
   "rui" => qq{Metathesaurus "Relationship Unique Identifier".  Computed in the MID and based on the following: root source (RSAB), relationship name, relationship_attribute, source RUI, and the "native source identifiers" that the relationship connects. Every RUI has an "inverse RUI" corresponding to the inverse of the relationship  [Range: must be in <a href="relationships_ui.html"><tt>relationships_ui.rui</tt></a>]},
   "inverse_rui" => qq{The rui corresponding to the inverse of the relationship specified by the rui field.  [Range: both RUI and inverse RUI definitions can be found in <a href="relationships_ui.html"><tt>relationships_ui</tt></a>]}, 
   "mid_event_id" => qq{This is either a <a href="molecular_actions.html">molecule_id</a> (for Undo/Redo actions) or an id generated in the same id space (from the <tt>MOLECULAR_ACTIONS</tt> row of <a href="max_tab.html">max_tab</a>) used to ensure that data changes to core and non-core tables are coordinated},
   "status" => qq{must be in <a href="level_status_rank.html"><tt>level_status_rank.status</tt></a> where the <tt>table_name</tt> matches the code for this core table},
   "relationship_level" => qq{Indicates whether the relationship is attached to a Metathesaurus concept ("concept level", with a value of 'C') or attached to an element of source information like an atom or a relationship ("source level", with a value of 'S'). A special value of 'P' is used for unreleasable atom-atom relationships used to drive certain types of editing.  Demotions are the most common example. [Range: must be in <a href="level_status_rank.html"><tt>level_status_rank.level_value</tt></a> where <tt>table_name = 'R'</tt>]},
   "relationship_level" => qq{must be in <a href="level_status_rank.html"><tt>level_status_rank.level_value</tt></a> where <tt>table_name = 'R'</tt>},
   "attribute_level" => qq{Indicates whether the attribute is attached to a Metathesaurus concept ("concept level", with a value of 'C') or attached to an element of source information like an atom or a relationship ("source level", witha value of 'S'). [Range: must be in <a href="level_status_rank.html"><tt>level_status_rank.level_value</tt></a> where <tt>table_name = 'A'</tt>]},
   "released" => qq{must be in <a href="released_rank.html"><tt>released_rank.released</tt></a>},
   "preferred_atom_id" => qq{the id of the preferred atom for this concept, must be in <a href="classes.html"><tt>classes.atom_id</tt></a> or in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "heading_id" => qq{an id representing an atom/term that was found as a citation in some article, must be in <a href="classes.html"><tt>classes.atom_id</tt></a>},
   "subheading_major_topic" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether this heading or any of its subheadings are considered <i>major topics</i>, this corresponds to a starred (<b>*</b>) entry in the old Medline file format},
   "major_topic" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether this subheading are considered <i>major topics</i>, this corresponds to a *\'d entry in the old Medline file format},
   "normwrd_id" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a> or in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "normstr_id" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a> or in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "version_id" => qq{not used},
   "old_version_id" => qq{not used},
   "new_version_id" => qq{not used},
   "atom_id" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a> or in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "source_atom_id" => qq{a unique id for each atom as it appears in the <a href="/MEME/Data/src_format.html"><tt>.src</tt></a> file, <i>aka</i> <tt>term_id</tt>},
   "source_aui" => qq{The source asserted unique identifier for each atom as it appears in the native source files (e.g. SNOMED DESCRIPTIONID).  Not all sources assert this value.},
   "source_rui" => qq{The source asserted unique id for each relationship as it appears in the native source files (e.g. SNOMED RELATIONSHIPID).  Not all sources assert this value},
   "source_atui" => qq{The source asserted unique id for each attribute as it appears in the native source files.  Not all sources assert this value (in fact none do as of 2005AB).},
   "relationship_group" => qq{A set or group_id that indicates that multiple relationships with the same concept_id_1/cui_1 belong together, and in many cases should be treated as parts of a "macro" relationship.  This is source data and is passed through unchanged.},
   "source_cui" => qq{The source asserted unique id for each concept as it appears in the native source files (e.g. SNOMED CONCEPTID).  Not all sources assert this value.},
   "source_dui" => qq{The source asserted unique id for each descriptor as it appears in the native source files (e.g. MeSH D#).  Not all sources assert this value.},
   "src_atom_id" => qq{a unique id for each atom as it appears in the <a href="/MEME/Data/src_format.html"><tt>.src</tt></a> file, <i>aka</i> <tt>term_id</tt>},
   "source_concept_id" => qq{not used},
   "pref_rank" => qq{not used},
   "atom_id_1" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a> or in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "atom_id_2" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a> or in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "concept_id" => qq{must be in <a href="concept_status.html"><tt>concept_status.concept_id</tt></a> or in <a href="dead_concept_status.html"><tt>dead_concept_status.concept_id</tt></a>},
   "concept_id_1" => qq{must be in <a href="concept_status.html"><tt>concept_status.concept_id</tt></a> or in <a href="dead_concept_status.html"><tt>dead_concept_status.concept_id</tt></a>},
   "concept_id_2" => qq{must be in <a href="concept_status.html"><tt>concept_status.concept_id</tt></a> or in <a href="dead_concept_status.html"><tt>dead_concept_status.concept_id</tt></a>},
   "work_id" => qq{must be either 0 or must be in <a href="meme_work.html"><tt>meme_work.work_id</tt></a>},
   "transaction_id" => qq{must be in <a href="molecular_actions.html"><tt>molecular_actions.transaction_id</tt></a>},
   "sui" => qq{The Metathesaurus computed string identifier.  Each case-sensitive string used as an atom name is assigned a unique SUI.  SUIs do not change over time. [Range: must be in <a href="string_ui.html"><tt>string_ui.sui</tt></a>]},
   "isui" => qq{The Metathesaurus computed case-insensitive string identifier.  Each case-insensitive string assigned a SUI is also assigned a unique ISUI.  ISUIs do not change over time. [Range: must be in <a href="string_ui.html"><tt>string_ui.isui</tt></a>]},
   "lui" => qq{The Metathesaurus computed normalized-string identifier.  Each normalized form (using LVG luiNorm program) of a string assigned a SUI is also assigned a unique LUI.  LUIs may change when the version of LVG changes. [Range: must be in <a href="string_ui.html"><tt>string_ui.lui</tt></a>]},
   "source" => qq{must be in <a href="source_rank.html"><tt>source_rank.source</tt></a>},
   "high_source" => qq{a new or update source to add to <a href="source_rank.html">},
   "low_source" => qq{the source immediately above which the <tt>high_source</tt> should be ranked},
   "high_termgroup" => qq{a new or update termgroup to add to <a href="termgroup_rank.html">},
   "low_termgroup" => qq{the termgroup immediately above which the <tt>high_termgroup</tt> should be ranked},
   "source_of_label" => qq{must be in <a href="source_rank.html"><tt>source_rank.source</tt></a>},
   "source_rank" => qq{comes from <a href="source_rank.html"><tt>source_rank.rank</tt></a> where the <tt>source</tt> fields match},
   "termgroup_rank" => qq{comes from <a href="termgroup_rank.html"><tt>termgroup_rank.rank</tt></a> where the <tt>termgroup</tt> fields match},
   "termgroup" => qq{must be in <a href="termgroup_rank.html"><tt>termgroup_rank.termgroup</tt></a>},
   "suppressible" => qq{A flag indicating whether or not this element should be "suppressed".  This allows users to remove potentially useless content from their subset of the Metathesaurus.  This value can be supplied directly from a source provider, or asserted by an editor. [Range: must be in <a href="suppressible_rank.html"><tt>suppressible_rank.suppressible</tt></a>, "O" means "obsolete", "E" means "editor suppressed", "Y" means "suppressed at termgroup level", and "N" means "not suppressed"]},
   "tobereleased" => qq{must be in <a href="tobereleased_rank.html"><tt>tobereleased_rank.tobereleased</tt></a>},
   "generated_status" => qq{must be in <tt>('Y','N')</tt></a>},
   "table_name" => qq{must be in <tt>(SELECT code FROM code_map WHERE type='table_name')</tt>},
   "row_id" => qq{must be a meme id from one of the core tables (specified by <tt>table_name</tt>)},
   "relationship_name" => qq{Expresses the type of relationship at a higher level. [Range: must be in <a href="inverse_relationships.html"><tt>inverse_relationships.relationship_name</tt></a>]},
   "relationship_attribute" => qq{Expresses the relationship in an (optionally) more specific way. [Range: must be in <a href="inverse_rel_attributes.html"><tt>inverse_rel_attributes.relationship_attribute</tt></a>]},
   "authority" => qq{the authority responsible for this row, can be validated by <a href="/MEME/Documentation/plsql_mr.html#get_source_authority_rank"><tt>MEME_RANKS.get_source_authority_rank</tt></a>},
   "editing_authority" => qq{authority responsible for creating or approving this concept, validated by <a href="/MEME/Documentation/plsql_mr.html#get_source_authority_rank"><tt>MEME_RANKS.get_source_authority_rank</tt></a>},
   "row_sequence" => qq{a positive integer used for ordering rows of the table},
   "timestamp" => qq{indicates when this row was inserted or last modified},
   "editing_timestamp" => qq{indicates when this concept was created or approved},
   "approval_molecule_id" => qq{the id of the last action which approved this concept, must be in <a href="molecular_actions.html"><tt>molecular_actions.molecule_id</tt></a>},
   "insertion_date" => qq{The date this row was inserted.},
   "expiration_date" => qq{The date this row was expired, either due to being removed from the current releasable state, or being changed.},
   "elapsed_time" => qq{an integer number of milliseconds},
   "activity" => qq{a short description of the activity},
   "detail" => qq{a detailed description},
   "description" => qq{a detailed description},
   "application" => qq{the name of an application or component in the <i>MEME</i> system},
   "version" => qq{the version number of a component/application},
   "release" => qq{the major release number for a component/application},
   "object_name" => qq{the name of a component/application},
   "comments" => qq{short comments},
   "current_version" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not this is the current version},
   "action" => qq{a code indicating the action performed, must be in <a href="code_map.html"><tt>code_map</tt></a> where the <tt>type='atomic_action'</tt>},
   "molecular_action" => qq{a code indicating the action performed, must be in <a href="code_map.html"><tt>code_map</tt></a> where the <tt>type='molecular_action'</tt>},
   "source_id" => qq{the id of the primary concept involved in an action},
   "target_id" => qq{the id of the secondary concept involved in an action, e.g. the target of a merge operation},
   "old_value" => qq{the previous value before an action represented as a string},
   "undone" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not this action was undone},
   "undone_by" => qq{the authority responsible for undoing this action, if <tt>undone='Y'</tt>},
   "undone_when" => qq{the timestamp of the undoing of this action, if <tt>undone='Y'</tt>},
   "new_value" => qq{the value after an action represented as a string},
   "action_field" => qq{the name of the field of a core table that was changed by this action},
   "atom_name" => qq{the actual name associated with an atom/term, must be in <a href="string_ui.html"><tt>string_ui.string</tt></a>},
   "string" => qq{a name that has appeared in the Metathesaurus},
   "norm_string" => qq{the result of running <tt>string</tt> through the lvg <tt>luiNorm</tt> program},
   "source_attribute_id" => qq{unique identifier for each attribute that exists in the <a href="/MEME/Data/src_format.html"><tt>.src</tt></a> file},
   "src_attribute_id" => qq{unique identifier for each attribute that exists in the <a href="/MEME/Data/src_format.html"><tt>.src</tt></a> file},
   "attribute_id" => qq{unique identifier for each attribute, primary key, a <i>meme_id</i>},
   "relationship_id" => qq{unique identifier for each relationship, primary key, a <i>meme_id</i>},
   "source_rel_id" => qq{unique identifier for each relationship that exists in the <a href="/MEME/Data/src_format.html"><tt>.src</tt></a> file},
   "src_relationship_id" => qq{unique identifier for each relationship that exists in the <a href="/MEME/Data/src_format.html"><tt>.src</tt></a> file},
   "attribute_name" => qq{the name of this attribute, e.g. <tt>SEMANTIC_TYPE</tt>},
   "attribute_value" => qq{the value of this attribute if it can be represented in less than 100 characters.  Otherwise it is a pointer into the <a href="stringtab.html"><tt>stringtab.string_id</tt></a> field, e.g. <tt>&lt;&gt;Long_Attribute&lt;&gt;:12345},
   "dead" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not this row is <i>dead</i>},
   "preferred_level" => qq{not used},
   "last_molecule_id" => qq{must be in <a href="molecular_actions.html"><tt>molecular_actions.molecule_id</tt></a> and is the id of the last <i>molecular action</i> to affect this core table row},
   "last_atomic_action_id" => qq{must be in <a href="atomic_actions.html"><tt>atomic_actions.atomic_action_id</tt></a> and is the id of the last <i>atomic action</i> to affect this core table row},
   "rank" => qq{the rank of this row, higher values are <i>better</i> ranks},
   "user_name" => qq{the operating system name of a user},
   "tty" => qq{The term type.  The value is based on information provided by sources and an attempt is made to reuse term types applied to other sources.  More information about term types can be found in <a href="meme_properties"><tt>meme_properties</tt></a> where <tt>key_qualifier = 'TTY'</tt>. [Range: must be in <a href="termgroup_rank.html"><tt>termgroup_rank.tty</tt></a>]},
   "last_release_cui" => qq{the cui of this atom/term in the last (major) release},
   "sort_key" => qq{not used},
   "last_release_rank" => qq{a 0-4 value indicating the TS,Stt rank of this row in the last (major) release.<ul><li>4: <tt>P|PF</tt></li><li>3: <tt>S|PF</tt></li><li>2: <tt>P|V*</tt></li><li>1: <tt>S|V*</tt></li><li>0: not in previous release</ul>},
   "last_assigned_cui" => qq{the last cui assigned to this atom/term by the most recent cui assignment run},
   "code" => qq{A value assigned to this atom/term by the source provider, this value has semantics specific to that source provider.  Often the code will have the same value as one of the other "source UI" values.},
   "citation_set_id" => qq{an id assigned to represent a set of citations for an <i>article</i> within a particular source such as Medline},
   "publication_date" => qq{the date of publication of an article from which citations are drawn},
   "subheading_set_id" => qq{an unique id within the <tt>citation_set_id</tt> identifying (groups of) subheadings},
   "coc_type" => qq{indicates the nature of a citation and what type of co-ocurrence it may become.  The values are those from the <tt>MRCOC.COT</tt> field},
   "subheading_qa" => qq{a two-letter abbreviation of a MeSH qualifier, must be in <a href="attributes.html"><tt>attributes.attribute_value</tt></a> where the <tt>attribute_name='QA'</tt>},
   "cui" => qq{The Metathesaurus computed Concept Unique Identifier, e.g. <tt>C0000039</tt>.},
   "cui1" => qq{The first Metathesaurus Concept Unique Identifier, e.g. <tt>C0000039</tt>.},
   "cui_1" => qq{The first Metathesaurus Concept Unique Identifier, e.g. <tt>C0000039</tt>.},
   "cui2" => qq{The second Metathesaurus Concept Unique Identifier, e.g. <tt>C0000039</tt>.},
   "cui_2" => qq{The second Metathesaurus Concept Unique Identifier, e.g. <tt>C0000039</tt>.},
   "release_mode" => qq{A flag indicating whether or not siblings of this row should be released in <tt>MRREL</tt> and/or <tt>MRCXT</tt>.  This field is something of an anacronism, but is still used to determine whether or not to include SIB entries in MRCXT.  The value, in practice, is either 00 or 11.  00 indicates that there should be no siblings in MRREL or MRCXT for this context, 11 indicates that there should be.  At inversion time, whether or not a source's hierarchy will contain SIB relationships is determined and it applies, generally, for the entire source.},
   "atomic_action_id" => qq{a unique identifier for each row, primary key},
   "molecule_id" => qq{a unique identifier for each row, primary key},
   "normstr" => qq{a normalized string, must be in <a href="string_ui.html"><tt>string_ui.norm_string</tt></a>},
   "normwrd" => qq{a word taken from a normalized string by the LVG <tt>wordind</tt> program},
   "word" => qq{a lowercased word taken from a string by the LVG <tt>wordind</tt> program},
   "sg_id" => qq{a generic type if identifier, originally for <i>source group identifier</i>},
   "sg_meme_id" => qq{the actual MEME identifier that this element is connected to (e.g. an atom_id, relationship_id, concept_id, attribute_id).  The <tt>sg_meme_data_type</tt> field will indicate what type of identifier it is},
   "new_sg_meme_id" => qq{the actual MEME identifier of an element that is from the current version of a source and exactly matches (according to the source replacement algorithm) the same element from the previous version. The element with this identifier will be deleted from the corresponding source table (indicated by sg_meme_data_type.},
   "sg_meme_id_1" => qq{the actual MEME identifier that this element is connected to (e.g. an atom_id, relationship_id, concept_id, attribute_id).  The <tt>sg_meme_data_type</tt> field will indicate what type of identifier it is},
   "sg_meme_id_2" => qq{the actual MEME identifier that this element is connected to (e.g. an atom_id, relationship_id, concept_id, attribute_id).  The <tt>sg_meme_data_type</tt> field will indicate what type of identifier it is},
   "sg_meme_data_type" => qq{the type of MEME identifier that this element is connected to (e.g. an atom_id, relationship_id, concept_id, attribute_id).  The <tt>sg_meme_id</tt> field will indicate the actual identifier},
   "sg_meme_data_type_1" => qq{the type of MEME identifier that this element is connected to (e.g. an atom_id, relationship_id, concept_id, attribute_id).  The <tt>sg_meme_id</tt> field will indicate the actual identifier},
   "sg_meme_data_type_2" => qq{the type of MEME identifier that this element is connected to (e.g. an atom_id, relationship_id, concept_id, attribute_id).  The <tt>sg_meme_id</tt> field will indicate the actual identifier},
   "source_sg_id" => qq{a generic type if identifier, originally for <i>source group identifier</i>},
   "target_sg_id" => qq{a generic type if identifier, originally for <i>source group identifier</i>},
   "sg_type" => qq{Indicates the type of identifier found in <tt>sg_id</tt>, [Range: valid values can be found in <a href="code_map.html"><tt>code_map.code</tt></a> where the <tt>type='sg_type'</tt>]},
   "source_sg_type" => qq{a field indicating the type of an <tt>sg_id</tt>, valid values can be found in <a href="code_map.html"><tt>code_map.code</tt></a> where the <tt>type='sg_type'</tt>},
   "target_sg_type" => qq{a field indicating the type of an <tt>sg_id</tt>, valid values can be found in <a href="code_map.html"><tt>code_map.code</tt></a> where the <tt>type='sg_type'</tt>},
   "sg_qualifier" => qq{some <tt>sg_types</tt> require two pieces of information, an id and a qualifier, e.g. for <tt>CODE_SOURCE</tt> it would be a source value},
   "target_sg_qualifier" => qq{some <tt>sg_types</tt> require two pieces of information, an id and a qualifier, e.g. for <tt>CODE_SOURCE</tt> it would be a source value},
   "source_sg_qualifier" => qq{some <tt>sg_types</tt> require two pieces of information, an id and a qualifier, e.g. for <tt>CODE_SOURCE</tt> it would be a source value},
   "sg_id_1" => qq{a generic type if identifier, originally for <i>source group identifier</i>},
   "sg_type_1" => qq{a field indicating the type of an <tt>sg_id</tt>, valid values can be found in <a href="code_map.html"><tt>code_map.code</tt></a> where the <tt>type='sg_type'</tt>},
   "sg_qualifier_1" => qq{some <tt>sg_types</tt> require two pieces of information, an id and a qualifier, e.g. for <tt>CODE_SOURCE</tt> it would be a source value},
   "sg_id_2" => qq{a generic type if identifier, originally for <i>source group identifier</i>},
   "sg_type_2" => qq{a field indicating the type of an <tt>sg_id</tt>, valid values can be found in <a href="code_map.html"><tt>code_map.code</tt></a> where the <tt>type='sg_type'</tt>},
   "sg_qualifier_2" => qq{some <tt>sg_types</tt> require two pieces of information, an id and a qualifier, e.g. for <tt>CODE_SOURCE</tt> it would be a source value},
   "string_id" => qq{an id that groups fragments of a long string together},
   "text_total" => qq{the length of a string when all of the fragments are put together},
   "text_value" => qq{one fragment of a potentially larger string, bounded to 1786 by OpenROAD},
   "birth_version" => qq{the version of the Metathesaurus in which this CUI first appeared, e.g. <tt>2001AA</tt>},
   "death_version" => qq{the version of the Metathesaurus in which this CUI was deleted, e.g. <tt>2002AA</tt>},
   "mapped_to_cui" => qq{the CUI that this CUI was mapped to when it was deleted},
   "map_rel" => qq{the <i>bequeathal</i> relationship, e.g. <tt>BBT</tt>, <tt>BRT</tt>, or <tt>BNT</tt>},
   "map_rela" => qq{a further specification of the <i>bequeathal</i> relationship},
   "map_reason" => qq{an explanation of why this CUI was mapped, if it was mapped},
   "nomap_reason" => qq{an explanation of why this CUI was <i>not</i> mapped, if it was not mapped},
   "map_dead" => qq{an indicator of whether or not this mapping is active},
   "preferred" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not this is the preferred mapping},
   "almost_sy" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not this mapping is <i>nearly</i> synonymy},
   "stripped_source" => qq{a value representing a source independent of version which can be used across time to mean the same source},
   "root_source" => qq{The source abbreviation expressed without any version information.  Remains the same across different versions of the same source.},
   "root_source_of_label" => qq{Functionally same as ROOT_SOURCE.},
   "source_family" => qq{a value representing a group of sources that are connected to one another, e.g. <tt>CPT</tt> and <tt>MTHCH</tt> have the same source family: <tt>CPT</tt>},
   "classes_status" => qq{an indicator of the status of all atoms/terms within a concept},
   "attributes_status" => qq{an indicator of the status of all attributes within a concept},
   "relationships_status" => qq{an indicator of the status of all relationships within a concept},
   "integrity_status" => qq{an indicator of the overall integrity status of a concept},
   "integrity_vector" => qq{a vector of integrity checks, e.g. <tt>&lt;MGV_C:V&gt;&lt;MGV_H1:V&gt;</tt>},
   "clean_molecule_id" => qq{the id of the last action which left this concept in a <i>clean</i> state, must be in <a href="molecular_actions.html"><tt>molecular_actions.molecule_id</tt></a>},
   "last_app_change" => qq{the timestamp of the last approved action},
   "last_app_change_by" => qq{the authority of the last approved action},
   "last_unapp_change" => qq{not used},
   "last_unapp_change_by" => qq{not used},
   "concept_status" => qq{the overall status of the concept, used to determine if the concept needs to be edited},
   "editor_level" => qq{a value indicating editor privileges, e.g. <tt>5</tt> is a <i>guru</i> editor},
   "initials" => qq{3-letter initials used by an editor},
   "grp" => qq{the editor group},
   "cur" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not an editor is current},
   "show_concept" => qq{a <tt>1</tt>/<tt>0</tt> flag indicating a users preference to auto-open the concept frame in the interface},
   "show_classes" => qq{a <tt>1</tt>/<tt>0</tt> flag indicating a users preference to auto-open the classes frame in the interface},
   "show_relationships" => qq{a <tt>1</tt>/<tt>0</tt> flag indicating a users preference to auto-open the relationships frame in the interface},
   "show_attributes" => qq{a <tt>1</tt>/<tt>0</tt> flag indicating a users preference to auto-open the attributes frame in the interface},
   "override_vector" => qq{a type of integrity vector used to override integrity check settings},
   "ic_level" => qq{an <a href="editors.html"><tt>editor level</tt></a> value used to determine the override vector for different editors},
   "ic_name" => qq{name of an integrity check, e.g. <tt>DT_M1</tt>},
   "negation" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether to perform a boolean <i>not</i> operation on this check},
   "type_1" => qq{<tt>SOURCE</tt> or <tt>TERMGROUP</tt>},
   "type_2" => qq{<tt>SOURCE</tt> or <tt>TERMGROUP</tt>},
   "value_1" => qq{a value of type <tt>type_1</tt>},
   "value_2" => qq{a value of type <tt>type_2</tt>},
   "v_actions" => qq{a comma separated list of action codes that can trigger a violation of this check. <i>Not used in MEME4</i>},
   "c_actions" => qq{a comma separated list of action codes that can trigger a correction of a previously violated check, <i>Not used in MEME4</i>},
   "ic_status" => qq{a flag indicating whether the check is <i>active</i> (<tt>A</tt>) or <i>inactive</i> (<tt>I</tt>)},
   "ic_type" => qq{a flag indicating whether the check is <i>reversible</i> (<tt>R</tt>) or <i>not reversible</i> (<tt>I</tt>)},
   "activation_date" => qq{the activation date},
   "deactivation_date" => qq{the deactivation date},
   "ic_short_dsc" => qq{a short description of the check},
   "ic_long_dsc" => qq{a long description of the check},
   "inverse_name" => qq{the inverse relationship name, e.g. the inverse of <tt>BT</tt> is <tt>NT</tt>},
   "inverse_rel_attribute" => qq{the inverse rela, e.g. the inverse of <tt>mapped_from</tt> is <tt>mapped_to</tt>},
   "language" => qq{the name of a language},
   "iso_lat" => qq{the ISO696 language code},
   "lat" => qq{a three letter code for a language},
   "level_value" => qq{a value representing an attribute level or a relationship level, typically this distinction has to do with whether the thing itself is connected to an atom or a concept},
   "old_lui" => qq{an old lui value},
   "new_lui" => qq{the value that an old lui was mapped to after lui assignment},
   "last_action_id" => qq{not used},
   "max_id" => qq{the maximum id, to get the next id add 1 to this},
   "pct_free" => qq{taken from <tt>user_indexes</tt>},
   "pct_increase" => qq{taken from <tt>user_indexes</tt>},
   "initial_extent" => qq{taken from <tt>user_indexes</tt>},
   "next_extent" => qq{taken from <tt>user_indexes</tt>},
   "min_extents" => qq{taken from <tt>user_indexes</tt>},
   "max_extents" => qq{taken from <tt>user_indexes</tt>},
   "tablespace_name" => qq{taken from <tt>user_indexes</tt>},
   "column_name" => qq{taken from <tt>user_ind_columns</tt>},
   "column_position" => qq{taken from <tt>user_ind_columns</tt>},
   "column_length" => qq{taken from <tt>user_ind_columns</tt>},
   "descend" => qq{taken from <tt>user_ind_columns</tt>},
   "key" => qq{the main value being looked up, e.g. <tt>MRSAT</tt>},
   "key_qualifier" => qq{a secondary value being looked up, e.g. <tt>MRFILES</tt>},
   "value" => qq{a value},
   "qa_id" => qq{a unique id given to a <i>Monster QA</i> set},
   "qa_id_1" => qq{a unique id given to a <i>Monster QA</i> set},
   "qa_id_2" => qq{a unique id given to a <i>Monster QA</i> set},
   "count_1" => qq{the count associated with <tt>qa_id_1</tt>},
   "count_2" => qq{the count associated with <tt>qa_id_2</tt>},
   "qa_count" => qq{the count associated with a particular qa check},
   "name" => qq{the name of a qa check},
   "query" => qq{a valid SQL query},
   "check_type" => qq{a category of qa check, e.g. <tt>core table semantics</tt>},
   "check_name" => qq{the name of a qa check},
   "adjustment" => qq{a value used to adjust the result of a check for known exceptions},
   "adjustment_dsc" => qq{a detailed description of an adjustment count},
   "auto_fix" => qq{SQL code that can be executed to resolve issues found by a qa check},
   "code1" => qq{the code of the <tt>atom_id_1</tt>},
   "code2" => qq{the code of the <tt>atom_id_2</tt>},
   "termgroup1" => qq{the termgroup of the <tt>atom_id_1</tt>},
   "termgroup2" => qq{the termgroup of the <tt>atom_id_2</tt>},
   "source1" => qq{the source of the <tt>atom_id_1</tt>},
   "source2" => qq{the source of the <tt>atom_id_2</tt>},
   "lui_1" => qq{the lui of the <tt>atom_id_1</tt>},
   "lui_2" => qq{the lui of the <tt>atom_id_2</tt>},
   "sui_1" => qq{the sui of the <tt>atom_id_1</tt>},
   "sui_2" => qq{the sui of the <tt>atom_id_2</tt>},
   "isui_1" => qq{the isui of the <tt>atom_id_1</tt>},
   "isui_2" => qq{the isui of the <tt>atom_id_2</tt>},
   "merge_level" => qq{a value indicating the type of merge fact, usually one of <tt>MAT</tt> (exact match), <tt>NRM</tt> (norm match), or <tt>SY</tt> (synonym)},
   "merge_set" => qq{the name of the merge set that this fact belongs to},
   "merge_fact_id" => qq{a unique id within a merge set},
   "make_demotion" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not demotion relationships should be created if this merge fact fails},
   "change_status" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not the atoms merged by this fact should be unapproved},
   "violations_vector" => qq{an integrity vector showing which checks failed to pass},
   "merge_order" => qq{a key used to order merge facts},
   "code_prefix" => qq{not used},
   "old_atom_id" => qq{an atom_id from an obsolete source that is being replaced, should be in <a href="classes.html"><tt>classes.atom_id</tt></a>},
   "new_atom_id" => qq{an atom_id from an update source that is being replacing an atom from an obsolete source, should be in <a href="classes.html"><tt>classes.atom_id</tt></a>},
   "document" => qq{an XML document or document fragment},
   "semantic_type" => qq{a valid semantic type value as defined by the semantic network},
   "is_chem" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not this is a chemical semantic type},
   "editing_chem" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not this is a chemical semantic type for the purposes of editing},
   "chem_type" => qq{a flag indicating whether or not this is a functional (<tt>F</tt>) or structural (<tt>S</tt>) semantic type},
   "switch" => qq{a flag indicating whether or not this row is to be inserted, <tt>R</tt> means <i>yes</i>},
   "hashcode" => qq{The MD5 of the full attribute value (even if it is represented as a long attribute).},
   "hierarchical_code" => qq{the tree-number given to the <tt>atom_id_1</tt> by the source provider},
   "parent_treenum" => qq{a tree-number composed of auis separated by periods (.), indicates the tree-position of the <tt>atom_id_2</tt> in <tt>aui</tt> terms},
   "local_row_id" => qq{a <i>meme id</i>, meaning either an <i>atom id</i>, <i>attribute id</i>, or <i>relationship id</i> depending on the value of <tt>table_name</tt>}, 
   "source_row_id" => qq{a <i>source id</i>, meaning either a <i>source atom id</i>, <i>source attribute id</i>, or <i>source relationship id</i>},
   "restriction_level" => qq{a 0-3 value indicating what type of licensing arrangement is necessary to use this source},
   "normalized_source" => qq{used to map a <tt>source</tt> value that does not have an SRC concept to one that does, e.g. <tt>MSH2002HMCE</tt> has a normalized source of <tt>MSH2002</tt>},
   "official_name" => qq{the official name of the source, usually matches the SRC/PT},
   "valid_start_date" => qq{the date that the source becomes valid},
   "valid_end_date" => qq{the date that the source ceases to be valid},
   "insert_meta_version" => qq{the version of the Metathesaurus in which the source first appears, e.g. <tt>2001AA</tt>},
   "remove_meta_version" => qq{the version of the Metathesaurus in which the source is removed, e.g. <tt>2002AA</tt>},
   "nlm_contact" => qq{the person at NLM responsible for this source},
   "inverter" => qq{the person at Apelon responsible for inverting this source},
   "acquisition_contact" => qq{contact information for the person from whom the source can be obtained},
   "content_contact" => qq{contact information for the source provider person who can best answer content questions},
   "license_contact" => qq{contact information for the source provider person who can best answer licensing questions},
   "urls" => qq{a comma separated list of URLS used to obtain more information},
   "context_type" => qq{a value indicating what type of contexts this source has, according to section 2.3.2 of the documentation},
   "notes" => qq{any additional information},
   "string_pre" => qq{the first ten characters of the <tt>string</tt>, used for indexing},
   "norm_string_pre" => qq{the first ten characters of the <tt>norm_string</tt>, used for indexing},
   "lowercase_string_pre" => qq{the first ten characters of the <tt>string</tt> (lowercased), used for indexing},
   "base_string" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not this is a <a href="/MEME/Training/glossary.html#base_string">base string</a>},
   "current_name" => qq{the name of the current version of a source in the Metathesaurus},
   "previous_name" => qq{the name of the immediately previous version of a source that has been updated},
   "ver" => qq{a version of the Metathesaurus, e.g. <tt>2001AA</tt>},
   "eng_atom_id" => qq{Pointer to the the ENG (English) atom corresponding to this row. Must be in <a href="classes.html"><tt>classes.atom_id</tt></a>.},
   "" => ""
   );

#
# Table categories
# a. Are changes explicilty logged with Molecular/Atomic actions?
# a2. Are changes explicitly logged?
# a3. Are changes implicitly logged?
# b. Are changes undoable?
# c. Are changes synchronized to MRD?
#
# 1. "core" 
# 2. "source" 
# 3. "index" 
# 4. "action" 
# 5. "qa" 
# 6. "ui" 
# 7. "metadata" 
# 8. "integrity" 
# 9. "system" 
# . "auxiliary"
# . "not used"
# . "sync"
%table_type_dsc =
  (
   "core" => qq{Core table.},
   "source" => qq{Used as staging area for source insertion data.},
   "index" => qq{Used for lookup.},
   "action" => qq{Used to log changes to system.},
   "qa" => qq{Used to perform Conservation of Mass and Semantic QA.},
   "ui" => qq{Used to tracks UI assignments, AUI, RUI, ATUI, SUI, LUI, ISUI, CUI, etc..},
   "metadata" => qq{Used to track high-level information about the system.},
   "integrity" => qq{Used by integrity system or QA bins based on integrity checks.},
   "system" => qq{Used to track information about MEME or the MID, MRD.},
   "auxiliary" => qq{Used for other processes or just general system maintenance.},
   "not used" => qq{Not currently in use.},
   "sync" => qq{Used for live synchronization of MID->MRD data changes},
   "state" => qq{Tracks historical states of data},
   "temp" => qq{Temporary table, usually a staging area for data}
);

#
%mid_table_types = 
  (
   "action_log" => "action",
   "activity_log" => "action",
   "allocated_ui_ranges" => "not used",
   "application_help" => "auxiliary",
   "application_versions" => "auxiliary",
   "atomic_actions" => "action",
   "atoms" => "auxiliary",
   "atoms_ui" => "ui",
   "attributes_ui" => "ui",
   "atom_ordering" => "auxiliary",
   "attributes" => "core",
   "authority_groups" => "not used",
   "authority_rank" => "not used",
   "aui_history" => "auxiliary",
   "classes" => "core",
   "coc_headings" => "auxiliary",
   "coc_subheadings" => "auxiliary",
   "code_map" => "metadata",
   "concept_status" => "core",
   "content_views" => "auxiliary",
   "content_view_members" => "auxiliary",
   "context_relationships" => "core",
   "cui_history" => "soon to be not used",
   "cui_map" => "auxiliary",
   "dead_atoms" => "auxiliary",
   "dead_attributes" => "core",
   "dead_classes" => "core",
   "dead_concept_status" => "core",
   "dead_context_relationships" => "core",
   "dead_foreign_classes" => "auxiliary",
   "dead_normstr" => "index",
   "dead_normwrd" => "index",
   "dead_relationships" => "core",
   "dead_sims_info" => "metadata",
   "dead_stringtab" => "auxilary",
   "dead_word_index" => "index",
   "deleted_cuis" => "not used",
   "editing_matrix" => "qa",
   "editors" => "metadata",
   "editor_preferences" => "metadata",
   "foreign_classes" => "auxiliary",
   "ic_applications" => "integrity",
   "ic_override" => "integrity",
   "ic_pair" => "integrity",
   "ic_single" => "integrity",
   "ic_violations" => "not used",
   "integrity_constraints" => "integrity",
   "inverse_relationships" => "auxiliary,metadata",
   "inverse_relationships_ui" => "ui",
   "inverse_rel_attributes" => "auxiliary,metadata",
   "language" => "auxiliary,metadata",
   "level_status_rank" => "auxiliary,metadata",
   "lui_assignment" => "ui",
   "max_tab" => "auxiliary",
   "meme_cluster_history" => "integrity",
   "meme_error" => "system",
   "meme_indexes" => "system",
   "meme_ind_columns" => "system",
   "meme_progress" => "system",
   "meme_properties" => "metadata",
   "meme_schedule" => "system",
   "meme_tables" => "system",
   "meme_work" => "system",
   "mid_qa_history" => "qa",
   "mid_qa_queries" => "qa",
   "mid_qa_results" => "qa",
   "mrd_validation_results" => "qa",
   "mrd_validation_queries" => "qa",
   "mid_validation_results" => "qa",
   "mid_validation_queries" => "qa",
   "molecular_actions" => "action",
   "mom_candidate_facts" => "source",
   "mom_exclude_list" => "source",
   "mom_facts_processed" => "source",
   "mom_merge_facts" => "source",
   "mom_norm_exclude_list" => "source",
   "mom_precomputed_facts" => "source",
   "mom_safe_replacement" => "auxiliary",
   "nysty" => "integrity",
   "normstr" => "index",
   "normwrd" => "index",
   "operations_queue" => "action",
   "qa_adjustment" => "qa",
   "qa_diff_adjustment" => "qa",
   "qa_diff_results" => "qa",
   "relationships" => "core",
   "relationships_ui" => "ui",
   "released_rank" => "auxiliary,metadata",
   "semantic_types" => "metadata",
   "sims_info" => "metadata,source",
   "snapshot_results" => "integrity",
   "source_attributes" => "source",
   "source_classes_atoms" => "source",
   "source_coc_headings" => "source",
   "source_coc_subheadings" => "source",
   "source_concept_status" => "source",
   "source_context_relationships" => "source",
   "source_id_map" => "auxiliary",
   "source_rank" => "auxiliary,metadata",
   "source_relationships" => "source",
   "source_replacement" => "source",
   "source_source_rank" => "source",
   "source_stringtab" => "source",
   "source_string_ui" => "source",
   "source_termgroup_rank" => "source",
   "source_version" => "auxiliary,metadata",
   "src_qa_queries" => "qa",
   "src_qa_results" => "qa",
   "src_obsolete_qa_results" => "qa",
   "srdef" => "metadata",
   "srstre2" => "integrity",
   "sr_predicate" => "auxiliary",
   "stringtab" => "auxiliary",
   "string_ui" => "ui",
   "suppressible_rank" => "auxiliary,metadata",
   "system_status" => "system",
   "termgroup_rank" => "auxiliary,metadata",
   "test_suite_statistics" => "system",
   "tobereleased_rank" => "auxiliary,metadata",
   "word_index" => "index" 
);

%mrd_table_types = 
  (
   "available_elements" => "sync",
   "clean_concepts" => "sync",
   "connected_set" => "sync",
   "connected_sets" => "sync",
   "connected_concepts" => "sync",
   "dtd_versions" => "system,not used",
   "events_processed" => "sync",
   "event_queue" => "sync",
   "extraction_history" => "sync",
   "feedback_queue" => "sync",
   "mrd_attributes" => "core,state",
   "mrd_classes" => "core,state",
   "mrd_coc_headings" => "core,state",
   "mrd_coc_subheadings" => "core,state",
   "mrd_content_views" => "auxiliary,state",
   "mrd_content_view_members" => "auxiliary,state",
   "mrd_column_statistics" => "metadata,state",
   "mrd_contexts" => "core,state",
   "mrd_cui_history" => "auxiliary,state",
   "mrd_file_statistics" => "metadata,state",
   "mrd_properties" => "metadata,state",
   "mrd_relationships" => "core,state",
   "mrd_source_rank" => "metadata,state",
   "mrd_termgroup_rank" => "metadata,state",
   "mrd_stringtab" => "core,state",
   "mrd_validation_queres" => "qa,state",
   "registered_handlers" => "system",
   "release_history" => "system,metadata",
   "tmp_attributes" => "temp,core",
   "tmp_classes" => "temp,core",
   "tmp_concepts" => "temp,core",
   "tmp_relationships" => "temp,core",
   "tmp_properties" => "temp,metadata"
);
#
# Comments about fields of specific tables where they
# differ from the default descriptsions above.
#
%tables_fields_to_comments = 
  (
   "mrd_contexts-rui" => qq{The Metathesaurus "Relationship Unique Identifier" that represents the relationship from this atom's parent to itself.  This field is only present for QA purposes.},
   "mrd_contexts-source_rui" => qq{The source asserted "Relationship Unique Identifier" that represents the relationship from this atom's parent to itself.  This field is only present for QA purposes.},
   "mrd_contexts-relationship_group" => qq{<b>NOT USED</b>},
   "mrd_contexts-aui" => qq{The Metathesaurus "Atom Unique Identifier" that this row represents a context for.   Each atom may have one or more contexts (aka "tree positions", "contexts", "paths to the root").  Each one would be expressed by an entry in this table. [Range: must be in <a href="mrd_classes.html"><tt>mrd_classes.aui</tt></a>]}, 
   "mrd_contexts-parent_treenum" => qq{A dot (.) separated list of AUIs from the context tree-top to the parent of this atom.  This is, in fact, the "context" of the atom.  From the information in this field, the ANC list of this atom in this context can be computed.},
   "mrd_contexts-hierarchical_code" => qq{A code, expressed by the source, which indicates the relative tree-position of this atom within the source's own conception of its hierarchy.  Typically, it will be a tree-number as expressed by the source, or nothing.},
   "mrd_contexts-relationship_attribute" => qq{The specific type of relationship expressed by the source between this atom's parent and itself.  Many source hierarchies are expressed as "isa" relationships from the tree-top down to each atom. [Range: must be in <a href="inverse_rel_attributes.html"><tt>inverse_rel_attributes.relationship_attribute</tt></a>},
   "mrd_relationships-rel_directionality_flag" => qq{a <tt>Y/N</tt> flag indicating whether or not this row expresses the relationship in the direction in which it was asserted by the source.  For example if the source asserts "A is broader than B" and this row of the table says the same, the value would be "Y". However, if this row says "B is narrower than A", the value would be "N".  In most cases, sources do not assert directionality explicitly and so the value is null.},
   "content_views-timestamp" => qq{indicates when the content view was last upadted},
   "mrd_content_views-timestamp" => qq{indicates when the content view was last upadted},
   "rel_directionality-long_form" => qq{The long form of a relationship name.},
   "rel_directionality-short_form" => qq{The short form of a relationship name.  Must be in <a href="inverse_relationships.html"><tt>inverse_relationships</tt></a>.},
   "allocated_ui_ranges-type" => qq{a value indicating the type of identifier that has been allocated},
   "source_rank-source" => qq{Unique id, primary key},
   "attributes-hashcode" => qq{an MD5 hash code on the attribute value source asserted unique id for each attribute as it appears in the native source files},
   "sg_attributes-attribute_id" => qq{Must be in <a href="attributes.html"><tt>attributes.attribute_id</tt></a>},
   "sg_relationships-relationship_id" => qq{Must be in <a href="relationships.html"><tt>relationships.relationship_id</tt></a>},
   "mom_precomputed_facts-status" => qq{Used to determine if this fact has completely mapped <tt>atom_id</tt> and <tt>concept_id</tt> values}, 
   "meme_work-work_id" => qq{The id used elsewhere in the database to identify this operation},
   "action_log-authority" => qq{the authority responsible for this action},
   "activity_log-authority" => qq{the authority responsible for this activity},
   "source_version-source" => qq{must be in <a href="source_rank.html"><tt>source_rank.stripped_source</tt></a>},
   "meme_work-work_id" => qq{a unique id representing some amount and some kind of database work},
   "molecular_actions-authority" => qq{the authority of the person or process who performed the action},
   "meme_work-authority" => qq{the authority of the person or process who is performing the work},
   "meme_error-authority" => qq{the authority of the person or process who caused this error},
   "meme_progress-authority" => qq{the authority of the person or process who is performing the long running operation},
   "inverse_relationships-relationship_name" => qq{a valid relationship name, primary key},
   "inverse_rel_attributes-relationship_attribute" => qq{a valid relationship attribute, primary key},
   "dead_normwrd-normwrd_id" => qq{must be in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "dead_normstr-normstr_id" => qq{must be in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "dead_word_index-atom_id" => qq{must be in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "dead_atoms-atom_id" => qq{must be in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "attributes-atom_id" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt>, or <tt>0</tt></a>},
   "attributes-concept_id" => qq{must be in <a href="classes.html"><tt>classes.concept_id</tt></a>},
   "dead_attributes-atom_id" => qq{must be in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "dead_relationships-atom_id_1" => qq{must be in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "dead_relationships-atom_id_2" => qq{must be in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "context_relationships-atom_id_1" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a>},
   "context_relationships-atom_id_2" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a>},
   "dead_context_relationships-atom_id_1" => qq{must be in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "dead_context_relationships-atom_id_2" => qq{must be in <a href="dead_classes.html"><tt>dead_classes.atom_id</tt></a>},
   "context_relationships-concept_id_1" => qq{this field is not maintained by the actions},
   "context_relationships-concept_id_2" => qq{this field is not maintained by the actions},
   "dead_context_relationships-concept_id_1" => qq{this field is not maintained by the actions},
   "dead_context_relationships-concept_id_2" => qq{this field is not maintained by the actions},
   "classes-language" => qq{The abbreviation for the language of this atom. [Range: must be in <a href="language.html"><tt>language.lat</tt></a>]},
   "mrd_classes-language" => qq{The abbreviation for the language of this atom. [Range: must be in <a href="language.html"><tt>language.lat</tt></a>]},
   "mrd_classes-tty" => qq{The term type.  The value is based on information provided by sources and an attempt is made to reuse term types applied to other sources.  More information about term types can be found in <a href="mrd_properties"><tt>mrd_properties</tt></a> where <tt>key_qualifier = 'TTY'</tt>. [Range: must be in <a href="mrd_termgroup_rank.html"><tt>mrd_termgroup_rank.tty</tt></a>]},

   "classes-concept_id" => qq{must be in <a href="classes.html"><tt>classes.concept_id</tt></a>},
   "editing_matrix-concept_id" => qq{must be in <a href="classes.html"><tt>classes.concept_id</tt></a>},
   "relationships-concept_id_1" => qq{must be in <a href="classes.html"><tt>classes.concept_id</tt></a>},
   "relationships-concept_id_2" => qq{must be in <a href="classes.html"><tt>classes.concept_id</tt></a>},
   "relationships-atom_id_1" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a> for non concept level relationships</a>},
   "relationships-atom_id_2" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a> for non concept level relationships</a>},
   "concept_status-preferred_atom_id" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a>},
   "concept_status-concept_id" => => qq{unique identifier for each concept, primary key, a <i>meme_id</i>},
   "concept_status-cui" => => qq{the Concept Unique Identifier (e.g. <tt>C0000039</tt>) currently assigned to this concept},
   "classes-atom_id" => qq{unique identifier for each atom, primary key, a <i>meme_id</i>},
   "atoms_ui-aui" => qq{unique identifier for each versionless atom, also a primary key.  A versionless atom is represented by the other fields of this table.},
   "attributes_ui-atui" => qq{unique identifier for each versionless attribute, also a primary key.  A versionless attribute is represented by the other fields of this table.},
   "relationships_ui-rui" => qq{unique identifier for each versionless relationship, also a primary key.  A versionless relationship is represented by the other fields of this table.},
   "dead_classes-atom_id" => qq{unique identifier for each atom, primary key, a <i>meme_id</i>},
   "dead_concept_status-concept_id" =>  => qq{unique identifier for each concept, primary key, a <i>meme_id</i>},
   "relationships-atom_id" => qq{Primary key, each relationship is assigned its own id},
   "context_relationships-atom_id" => qq{Primary key, each relationship is assigned its own id},
   "editing_matrix-integrity_vector" => qq{a vector of violations of integrity checks , e.g. <tt>&lt;DT_M1:V&gt;&lt;DT_PN1:W&gt;</tt>},
   "cui_history-crel" => qq{a value indicating the fate of <tt>cui1</tt>, either it was merged (<tt>SY</tt>), deleted (<tt>DEL</tt>), or bequeathed (a rel like <tt>RB</tt>)},
   "molecular_actions-transaction_id" => qq{this is an id that groups molecular actions together so they can be retrieved as a set},
   "sr_predicate-replaced_termgroups" => qq{a comma separated list of obsolete termgroups that were replaced},
   "sr_predicate-replacement_termgroups" => qq{a comma separated list of new termgroups that were replace obsolete ones},
   "sr_predicate-replaced_sources" => qq{a comma separated list of obsolete sources that were replaced},
   "sr_predicate-replacement_sources" => qq{a comma separated list of new sources that were replace obsolete ones},
   "sr_predicate-string_match" => qq{a value indicating how strings were matched in the safe replacement predicate},
   "sr_predicate-tty_match" => qq{a value indicating how ttys were matched in the safe replacement predicate},
   "sr_predicate-code_match" => qq{a value indicating how codes were matched in the safe replacement predicate},
   "termgroup_rank-exclude" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not the termgroup should be placed on the exclude list, <a href="mom_exclude_list.html"><tt>mom_exclude_list</tt></a>},
   "termgroup_rank-norm_exclude" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not the termgroup should be placed on the norm exclude list, <a href="mom_norm_exclude_list.html"><tt>mom_norm_exclude_list</tt></a>},
   "string_ui-sui" => qq{a <a href="/MEME/Training/glossary.html#sui">string unique identifier</a>, it is a code that represents the <tt>string</tt>},
   "string_ui-lui" => qq{a <a href="/MEME/Training/glossary.html#lui">lexical class identifier</a>, it is a code that represents the <tt>norm_string</tt>},
   "string_ui-isui" => qq{a <a href="/MEME/Training/glossary.html#isui">case insensitive string unique identifier</a>, it is a code that represents the lowercased <tt>string</tt>},
   "source_id_map-origin" => qq{the origin of the id, so far <i>local</i> is the only origin},
   "source_id_map-table_name" => qq{a code for a table_name, this must be one of <tt>C</tt> (classes), <tt>A</tt> (attributes), <tt>R</tt> (relationships), or <tt>CR</tt> (context relationships)},
   "qa_diff_adjustment-qa_count" => qq{the adjustment value between the sets defined by <tt>qa_id_1</tt> and <tt>qa_id_2</tt>},
   "operations_queue-type" => qq{a value indicating what type of data is in this row, should be one of:<ul>
    <li><tt>REDO</tt>: A molecular <i>redo</i> operation</li>
    <li><tt>UNDO</tt>: A molecular <i>undo</i> operation</li>
    <li><tt>TABLE</tt>: A MRD <tt>TableEvent</tt> XML document fragment</li>
    <li><tt>INSERT</tt>: A MRD<tt>ActionEvent</tt> XML document fragment</li>
	</ul>},
   "nhsty-ui" => qq{the code for the STY},
   "nhsty-sty" => qq{a valid STY value, must be in <a href="semantic_types.html"><tt>semantic_types.semantic_type</tt></a>},
   "nhsty-stn" => qq{the hierarchical code for this semantic type in the semantic network},
   "nhsty-def" => qq{the definition for the semantic type},
   "mom_candidate_facts-source" => qq{the source of this fact},
   "mom_candidate_facts-status" => qq{not used},
   "mom_merge_facts-molecule_id" => qq{the <tt>molecule_id</tt> responsible for the merging of this fact},
   "mom_merge_facts-source" => qq{the source of this fact},
   "mom_merge_facts-status" => qq{a flag indicating how this fact has been treated.  There are several possibilities:
    <ul><li><tt>R</tt>: The fact has yet to be processed</li>
        <li><tt>D</tt>: At least one integrity check failed</li>
        <li><tt>F</tt>: It is known before checking that the fact will fail integrity checks</li>
        <li><tt>M</tt>: The atoms were merged successfully</li>
	<li><tt>P</tt>: The atoms were previously merged</li></ul>},
   "mom_facts_processed-molecule_id" => qq{the <tt>molecule_id</tt> responsible for the merging of this fact},
   "mom_facts_processed-source" => qq{the source of this fact},
   "mom_facts_processed-status" => qq{see <a href="mom_merge_facts.html"><tt>mom_merge_facts</tt></a>},
   "editors-name" => qq{the operating system name of a user},
   "meme_work-type" => qq{a general indicator of the type of work done, usually one of: <tt>INITIALIZE</tt>, <tt>MATRIX</tt>, <tt>MAINTENANCE</tt>, <tt>INSERTION</tt> (each of these has a <a href="/MEME/Documentation/plsql_mc.html"><tt>MEME_CONSTANTS</tt></a> variable)},
   "meme_tables-table_name" => qq{a name of a table in the <i>MID</i> that is tracked, must be in <tt>user_tables.table_name</tt>},
   "meme_schedule-specific_date" => qq{a value in this field is used to have a schedule entry apply to a specific date, e.g. <tt>01-jan-2002</tt>},
   "meme_schedule-day_of_week" => qq{a 0-6 (sun-sat) value indicates that the schedule entry should apply to that day of the week},
   "meme_schedule-start_time" => qq{a value in here indicates that the schedule entry applies beginning at this time of day},
   "meme_schedule-end_time" => qq{a value in here indicates that the schedule entry ceases to apply at this time of day},
   "meme_schedule-cpu_mode" => qq{the cpu mode that an application should run under if the conditions are satisfied, one of: <tt>SHUTDOWN</tt>, <tt>STANDBY</tt>, <tt>DELAY</tt>, <tt>NODELAY</tt>},
   "meme_schedule-lock_mode" => qq{not used yet, but will have values that indicate to applications whether or not it is OK to lock core data tables},
   "max_tab-table_name" => qq{a label used to track a max_id, e.g. <tt>ATOMIC_ACTIONS</tt> tracks the highest <a href="atomic_actions.html"><tt>atomic_actions.atomic_action_id</tt></q>},
   "inverse_relationships-weak_flag" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not this is a weak type of relationship},
   "ic_single-type" =>  qq{<tt>SOURCE</tt> or <tt>TERMGROUP</tt>},
   "ic_single-value" => qq{a value of type <tt>type</tt>},
   "application_help-topic" => qq{if null, this row is general help for a component/application, otherwise this row contains more specific help for a particular topic},
   "application_help-text" => qq{the text of the help info for this component/application},
   "application_versions-enforce_flag" => qq{a <tt>Y</tt>/<tt>N</tt> flag indicating whether or not to enforce this version},
   "atomic_actions-molecule_id" => qq{an id found in <a href="molecular_actions.html"><tt>molecular_actions.molecule_id</tt></a> that groups multiple rows together},
   "dead_atomic_actions-molecule_id" => qq{an id found in <a href="molecular_actions.html"><tt>molecular_actions.molecule_id</tt></a> that groups multiple rows together},
   "atomic_actions-status" => qq{an <tt>R</tt>/<tt>N</tt> flag indicating whether or not this action is <i>approved</i>.  Currently, this always has a value of <tt>R</tt>},
   "molecular_actions-status" => qq{an <tt>R</tt>/<tt>N</tt> flag indicating whether or not this action is <i>approved</i>},
   "code_map-type" => qq{a code type, must be in <tt>code_map.code</tt> where <tt>type='valid_code_type'</tt>},
   "code_map-value" => qq{the value that a code is mapped to},
   "dba_cutoff-edit" => qq{a <tt>y</tt>/<tt>n</tt> flag indicating whether or not editors are allowed to edit},
   "system_status-system" => qq{a label indicating a switch of some kind},
   "system_status-status" => qq{the status of the switch corresponding to the <tt>system</tt> field},
   "meme_indexes-table_name" => qq{taken from <tt>user_indexes</tt>},
   "meme_indexes-index_name" => qq{taken from <tt>user_indexes</tt>},
   "meme_ind_columns-table_name" => qq{taken from <tt>user_ind_columns</tt>},
   "meme_ind_columns-index_name" => qq{taken from <tt>user_ind_columns</tt>},
   "ic_system_status-status" => qq{one of <tt>ON</tt>, <tt>OFF</tt>, or <tt>W</tt> (warning mode)},
   "atomic_actions-authority" => qq{the authority responsible for the action},
   "atoms-atom_id" => qq{must be in <a href="classes.html"><tt>classes.atom_id</tt></a>},
   "authority_rank-authority" => qq{a value that represents a person/process responsible for some change in the database},
   "test_suite_statistics-value" => qq{the value generated by this test},
   "test_suite_statistics-error" => qq{the error details, if an error occurred while running this test},
   "mrd_attributes-ui" => qq{Any of the variety of Metathesaurus identifiers to which an attribute can be attached, including CUI, AUI, RUI and eventually ATUI and possibly SUI and LUI.  This is the actual Metathesaurus object to which this attribute is attached, not necessarily the thing to which the attributing source asserts it is attached.},
   "mrd_attributes-sui" => qq{This field exists for backwards compatability (to the ORF MRSAT).  If the UI field contains an AUI, this is the SUI of that AUI.},
   "mrd_attributes-lui" => qq{This field exists for backwards compatability (to the ORF MRSAT).  If the UI field contains an AUI, this is the LUI of that AUI.},
   "mrd_attributes-code" => qq{This field exists for backwards compatability (to the ORF MRSAT).  If the UI field contains an AUI, this is the source code of that AUI.},
   "mrd_attributes-sg_type" => qq{Indicates the level at which the attributing source asserted this attribute.  For example, SNOMEDCT asserts the SNOMEDID attribute at the SNOMEDCT concept level, meaning that it is attached to a SNOMEDCT concept.  Here, we represent that attribute as attached to the highest ranking AUI with that SNOMEDCT source concept_id, with an SG_TYPE value of SCUI, indicating that the source asserted the attribute to be connected to that AUI's SCUI.  As a rule of thumb, if the UI is an AUI value, this will be a MRCONSO field name, and if the UI is a RUI value, this will be a MRREL field name, and if the UI is a CUI, this will be 'CUI'.},
   "mrd_attributes-attribute_value" => qq{the value of this attribute if it can be represented in less than 100 characters.  Otherwise it is a pointer into the <a href="mrd_stringtab.html"><tt>mrd_stringtab.hashcode</tt></a> field, e.g. <tt>&lt;&gt;Long_Attribute&lt;&gt;:0bee89b07a248e27c83fc3d5951213c1</tt>.},
   "mrd_relationships-relationship_level" => qq{Indicates whether the relationship is attached to Metathesaurus concepts ("concept level", with a value of 'C') or attached to elements of source information like atoms ("source level", with a value of 'S'). [Range: C, S]},
   "mrd_relationships-sg_type_1" => qq{Indicates the level at which the first identifier of the attributing source asserted this relationship.  For example, SNOMEDCT asserts relationships at the SNOMEDCT concept level, meaning that it is attached to a SNOMEDCT concepts.  Here, we represent that relationship as attached to the highest ranking AUI with that SNOMEDCT source concept_id, with an SG_TYPE_1 value of SCUI, indicating that the source asserted that the relationship is connected to that AUI's SCUI.  As a rule of thumb, if the AUI value is not null, this will be a MRCONSO field name, otherwise if it is concept level, it will be 'CUI'.},
   "mrd_relationships-sg_type_2" => qq{Same as SG_TYPE_1 except for the AUI_2,CUI_2},
      "" => ""
   );

#
# Descriptions of each table
#
%descriptions =
  (
   "source_coc_headings_todelete" => qq{Staging area for deleted heading citation set information.},
   "source_coc_headings" => qq{Staging area for new heading citations.},
   "source_coc_subheadings" => qq{Staging area for new subheading citations.},
   "dead_foreign_attributes" => qq{Container for deleted foreign attributes.},
   "dead_foreign_classes" => qq{Container for deleted foreign atoms.},
   "rel_directionality" => qq{Contains long/short forms of rel names. This should really be subsumed into inverse_rel_attributes.},
   "dead_sims_info" => qq{Tracks deleted source metadata.},
   "sims_info" => qq{Tracks source metadata.  There is a lot of information about sources in this table, much of it details the inversion and insertion process.},
   "allocated_ui_ranges" => qq{Tracks ranges of UIs allocated for certain reasons. These UIs can be of any type, but will likely be things like SUIs, LUIs, and AUIs.},
   "atoms_ui" => qq{Maps sets of fields to unique identifiers.  This table is used like <a href="string_ui.html"><tt>string_ui</tt></a> in the sense that it tracks AUI assignments historically over time.  The AUI is intended as a versionless atom identifier (somewhere between a SUI and an atom id).},
   "relationships_ui" => qq{Maps sets of fields to unique relationship identifiers.  This table is used like <a href="atoms_ui.html"><tt>string_ui</tt></a> in the sense that it tracks RUI assignments historically over time.  The RUI is intended as a versionless relationship identifier.},
   "inverse_relationships_ui" => qq{Maps relationship identifiers to the identifiers corresponding to the inverse of the relationship specified by the first identifier.},
   "attributes_ui" => qq{Maps sets of fields to unique attribute identifiers.  This table is used like <a href="atoms_ui.html"><tt>string_ui</tt></a> in the sense that it tracks ATUI assignments historically over time.  The ATUI is intended as a versionless attribute identifier.},
   "sr_predicate" => qq{Contains descriptions of the matching predicates used to compute safe replacement for the various current sources in the <i>MID</i>.},
   "word_index" => qq{A word index. Looking up a word in here will yield a set of atoms whose lowercased string contains that word.},
   "termgroup_rank" => qq{Normalizes and ranks termgroup values.},
   "test_suite_statistics" => qq{Stores statistics gathered during test suite runs.},
   "tobereleased_rank" => qq{Normalizes and ranks values of <tt>tobereleased</tt> fields in the various core tables.},
   "suppressible_rank" => qq{Normalizes and ranks values of <tt>suppressible</tt> fields in the various core tables.},
   "suppressible_rank" => qq{Normalizes and ranks values of <tt>suppressible</tt> fields in the various core tables.},
   "string_ui" => qq{Contains one row for each string that has ever been in the <i>MID</i>.  This table is only ever inserted into (unless QA problems require updates/deletes).  It maintains a record of every string that has ever been present in the MID or in the Metathesaurus and maps those strings to a normalized string, a SUI, a LUI, and an ISUI.},
   "stringtab" => qq{Contains long attribute values.  The <a href="attributes.html"><tt>attributes</tt></a> table has only 100 characters alloted for <tt>attribute_value</tt>.  For any attributes with longer attribute values (such as definitions), a pointer into the stringtab table is constructed of the form <tt>'&lt;&gt;Long_Attribute&lt;&gt;:&lt;string_id&gt;'</tt> where <tt>&lt;string_id&gt;</tt> is a valid string id in this table.}, 
   "srstre2" => qq{Contains relationships between semantic types.  Used by the data-driven QA bin <a href="/MEME/Documentation/plsql_mip.html#styisa"><tt>styisa</tt></a>.},
   "srdef" => qq{Contains data from the UMLS file <tt>SRDEF</tt>.  This data contains the semantic types and relationship attributes defined in the semantic network.},
   "src_qa_results" => qq{A record of the last <i>Monster QA</i> run against the <i>source core tables</i>. Click here for <a href="/MEME/Documentation/automated_qa.html">Automated QA System documentation</a>.},
   "src_obsolete_qa_results" => qq{A record of <i>Monster QA</i> runs against the <i>source core tables</i> for obsolete sources. Click here for <a href="/MEME/Documentation/automated_qa.html">Automated QA System documentation</a>.},
   "src_qa_queries" => qq{Contains queries used for <i>Monster QA</i> of new data added to the <i>MID</i>.  Click here for <a href="/MEME/Documentation/automated_qa.html">Automated QA System documentation</a>.},
   "source_version" => qq{Maintains a list of <i>current</i> and <i>previous</i> versions of sources.  These values can be looked up by querying the table with a stripped source value.},
   "source_string_ui" => qq{Staging area for new names.},
   "source_stringtab" => qq{Staging area for new long attributes. },
   "source_termgroup_rank" => qq{Staging area for new termgroups.},
   "source_source_rank" => qq{Staging area for new sources.},
   "source_relationships" => qq{Staging area for new relationships.},
   "source_rank" => qq{Maintains metadata information about the sources that contribute to Metathesaurus content.  This table also serves as a mechanism to normalize valid source values. },
   "source_id_map" => qq{Maintains a mapping of ids used in source files (<i>source ids</i>) to ids used in the <i>MID</i> (<i>meme ids</i>).},
   "source_context_relationships" => qq{Staging area for new context relationships. },
   "source_concept_status" => qq{Staging area for new concepts.},
   "source_classes_atoms" => qq{Staging area for new atoms.},
   "source_attributes" => qq{Staging area for new attributes.},
   "source_replacement" => qq{Staging area for a set of identifiers that comprise a subset of a source that has not changed from the previous version(s) to the present version.},
   "snapshot_results" => qq{Contains results of an integrity snapshot run.},
   "sg_status" => qq{Not currently used.  This table was envisioned as a parallel to <a href="concept_status.html"><tt>concept_status</tt></a> for normalizing identifiers that sources use to group atoms together.},
   "sg_molecular_actions" => qq{Not currently used. This table was envisioned as a parallel to <a href="molecular_actions.html"><tt>molecular_actions</tt></a> for integrated vocabulary development (editing of sources within the <i>MID</i>).},
   "sg_relationships" => qq{This table is used to keep track of the original identifiers used to connect relationships to the <i>MID</i>.},
   "sg_attributes" => qq{This table is used to keep track of the original identifiers used to connect attributes to the <i>MID</i>.},
   "semantic_types" => qq{Contains valid semantic type values.  Additionally, this table tracks which semantic types are <i>chemical semantic types</i>.},
   "released_rank" => qq{Normalizes and ranks values of <tt>released</tt> fields in the various core tables.},
   "relationships" => qq{Contains relationships between atoms and concepts.  This is a <i>core table</i>.  This table is very intimately connected with <a href="classes.html"><tt>classes</tt></a> and <a href="sg_relationships.html"><tt>sg_relationships</tt></a>.},
   "qa_diff_results" => qq{This is where discrepancies from <i>Monster QA</i> runs are stored.  Click here for <a href="/MEME/Documentation/automated_qa.html">Automated QA System documentation</a>.},
   "qa_diff_adjustment" => qq{Tracks adjustments to <i>Monster QA</i> discrepancies between current and historical <i>MID</i> counts. Click here for <a href="/MEME/Documentation/automated_qa.html">Automated QA System documentation</a>.},
   "qa_adjustment" => qq{Tracks adjustments to <i>Monster QA</i> discrepancies between SRC and <i>MID</i> counts. Click here for <a href="/MEME/Documentation/automated_qa.html">Automated QA System documentation</a>.}, 
   "operations_queue" => qq{A repository of changes to non-core tables. There are actually four kinds of data stored in this table: (i) Undo actions, (ii) Redo actions, (iii) MRD ActionEvent document fragments containing initial data values for core-table inserts, (iv) MRD TableEvent document fragments containing data changes to non-core tables.},
   "normwrd" => qq{A normalized word index. Looking up a normalized word in here will yield a set of atoms whose normalized string contains that word.},
   "normstr" => qq{A normalized string index.  Looking up a normalized string here will  yield a set of atoms matching it.},
   "nhsty" => qq{A list of semantic types that cannot accompany <tt>NH</tt> flags.},
   "mom_safe_replacement" => qq{A ranked mapping of atoms from obsolete sources to updated sources.  This data is produced by the safe replacement process and can be used to identify the atom from an updated version of a source that "best" replaces a given atom from the obsolete version.},
   "mom_precomputed_facts" => qq{The repository of facts loaded from <tt>mergefacts</tt> files.  The <tt>\$MEME_HOME/bin/load_src.csh</tt> script can be used to load a <a href="/MEME/Data/src_format.html#mergefacts"><tt>mergefacts.src</tt></a> file into this table.  From here it is processed and loaded into <a href="mom_candidate_facts.html"><tt>mom_candidate_facts</tt></a>.},
   "mom_merge_facts" => qq{Contains merge facts that are to be processed.  Facts are loaded from the <a href="mom_candidate_facts.html"><tt>mom_candidate_facts</tt></a> table into here and then processed by the merge engine.  After that, they are loaded into <a href="mom_facts_processed"><tt>mom_facts_processed</tt></a>.},
   "mom_facts_processed" => qq{An archive of all of the merge facts that have been processed in a given calendar year.  All facts are stored here, including ones that fail to merge, produce demotions, or are already merged.},
   "mom_exclude_list" => qq{A list of termgroups used as a filter when generating some merge facts.  Typically the termgroups on this list are abbreviations.},
   "mom_norm_exclude_list" => qq{A list of termgroups used as a filter when generating merge facts.  The purpose of this list is to exclude termgroups that are expected to perform poorly in norm matching, such as termgroups known to be chemicals},
   "mom_candidate_facts" => qq{Contains facts being considered for merging.  The processes that load data from <tt><a href="/MEME/Data/src_format.html#mergefacts">mergefacts.src</a></tt> files or generate facts from the database put those candidate facts here.},
   "molecular_actions" => qq{The log of editor actions.  Each row in this table is a log of a <i>higher level</i> action on the core data in the <i>MID</i>.},
   "mid_validation_results" => qq{This table tracks results from the <i>MID Validation</i> system.},
   "mid_validation_queries" => qq{This is the data that drives the online <i>MID Validation</i> system.},
   "mid_qa_results" => qq{A record of the last <i>Monster QA</i> run against the <i>MID</i>.  Click here for <a href="/MEME/Documentation/automated_qa.html">Automated QA System documentation</a>.},
   "mid_qa_queries" => qq{Contains queries used for <i>Monster QA</i> of the <i>MID</i> data. Click here for <a href="/MEME/Documentation/automated_qa.html">Automated QA System documentation</a>.},
   "mid_qa_history" => qq{Historical record of <i>Monster QA</i> runs.  Click here for <a href="/MEME/Documentation/automated_qa.html">Automated QA System documentation</a>.},
   "meme_work" => qq{A record of large database operations.  Insertions, Matrix init runs, CUI assignments and other maintenance work should request a <tt>work_id</tt> and log in this table the details of the work at hand.  This is done by calling <a href="/MEME/Documentation/plsql_mu.html#new_work"><tt>MEME_UTILITY.new_work</tt></a>. Eventually editing should be tracked through this table as well so that anywhere that one encounters a <tt>work_id</tt>, it could be looked up here to find a description of what work was involved.}, 
   "meme_tables" => qq{A list of tables maintained by the <i>MEME</i> system.  Tables in this list generally are created by the <tt>$MEME_HOME/etc/sql/meme_tables.sql</tt> schema script and have their indexes maintained in the <a href="meme_indexes.html"><tt>meme_indexes</tt></a> and <a href="meme_ind_columns.html"><tt>meme_ind_columns</tt></a> tables.  In the future this table will likely track additional information such as what type of table each entry is and what the formal update model for those tables should be.},
   "meme_schedule" => qq{An advanced table used for scheduling server processes.  This table is complex to use. In a generic sense, it is used to inform server applications of various different run and lock mode states at different times.  Another "listener" type of application must be used that understands the semantics of this table.},
   "meme_properties" => qq{A catch-all table for tracking release data.  The configuration data for <tt>MRCOLS</tt>/<tt>MRFILES</tt> as well as the CUI-ranges data for the <tt>MRSAT DA</tt> flag are stored in this table, but it can be used in a larger sense for any kind of data that fits the key-value property model.},
   "meme_progress" => qq{Allows long-running PL/SQL operations to report their progress.  Since a PL/SQL procedure cannot report any output to STDOUT until it finishes, we needed a way for long-running processes like the matrix initializer to report their activity as they ran along.  There is a <a href="/MEME/Documentation/plsql_mu.html#log_progress">MEME_UTILITY</a> procedure that PL/SQL processes can use to log to this table.  Logging happens in an autonomous transaction scope so that users can see the state of things regardless of commit states.},
   "meme_ind_columns" => qq{A projection of <tt>user_ind_columns</tt> that tracks index columns for <i>MEME</i> tables},
   "meme_indexes" => qq{A projection of <tt>user_indexes</tt> that tracks indexes for <i>MEME</i> tables},
   "meme_error" => qq{Tracks errors in the <i>MEME</i> system.  Every time an error occurs, it is logged in this table.},
   "meme_cluster_history" => qq{Maintains map of "connections" between concepts based on integrity violations.  This table is used by the matrix updater to track relationships between concepts that need to be explored when computing the subset of the database that it needs to operate on.},
   "max_tab" => qq{Maintains the maximum id values for various kinds of IDs.  Each row has a <tt>table_name</tt> which specifies what kind of id is being tracked and a maximum value.  The value in the <tt>max_id</tt> field is the maximum id used so far, so if is 12345 for table "ATOMS" it means that the maximum <tt>atom_id</tt> used so far is 12345.},
   "lui_assignment" => qq{Maps old <tt>LUI</tt> to new <tt>LUI</tt> values after <tt>LUI</tt> re-assignment.  This table is produced by <tt>\$MEME_HOME/bin/assign_<tt>LUI</tt>s.csh</tt> and it contains a mapping for every <tt>LUI</tt> from its old value to its new value (even for cases where they are the same.},
   "level_status_rank" => qq{Normalizes and ranks level, status values across core data.  Not only does this table supply valid status and level values (as well as level-status combinations) for the core tables, but it ranks them as well so that they can be used in the ranking algorithm for the core data element.},
   "language" => qq{Maps language names to 3 letter language codes.  Also serves as a table listing the valid <tt>MRCON.LAT</tt> values.},
   "inverse_rel_attributes" => qq{Maps relationship attributes to their inverses.  Additionally it allowed for the ranking of relationship attributes so that it can participate in the overall rank of the relationship.},
   "inverse_relationships" => qq{Maps relationship names to their inverses.  Additionally it allowed for the ranking of relationship names so that it can participate in the overall rank of the relationship.  The <tt>weak_flag</tt> field can be used to indicate that some relationships (like <tt>RT?</tt>) are <i>weaker</i> relationships than others.  This can be useful in terms of integrity checks like <tt>MGV_E</tt> which may be altered to say that a merge can be stopped if there exists a relationship between two concepts, <i>unless</i> that relationship is a <i>weak</i> relationship.},
   "integrity_constraints" => qq{Tracks integrity checks.  This table contains a list of all valid integrity checks with descriptions, lists of actions that cause violations (or can correct violations) as well as information about whether or not checks are active or inactive and the type of check.},
   "ic_violations" => qq{Tracks integrity violations, <i>not being used</i>.  This table was added in <i>MEME2</i> but not ever effectively used.  For all intents and purposes, this table is ignored in <i>MEME3</i>.},
   "ic_system_status" => qq{This view acts like a <i>switch</i> for the integrity system.  If the switch is set to <tt>OFF</tt> checks are ignored during editing actions.  If the switch is set to <tt>ON</tt> checks are performed according to the integrity vectors used.  If the switch is <tt>W</tt> checks are <i>all</i> performed with <a href="/MEME/Training/glossary.html#ic_code">ic_code</a> of <tt>W</tt>.},
   "ic_single" => qq{Tracks data for unary integrity checks.  A check like <tt>MGV_B</tt> requires a single piece of additional information which would be stored in this table.},
   "ic_pair" => qq{Tracks data for binary integrity checks.  A check like <tt>MGV_K</tt> requires pieces of additional information which would be stored in this table.},
   "ic_override" => qq{Maps editor levels to override integrity vectors.  An application like the editing interface will start with a default integrity vector and then for each editor (or editor level) allows an <i>override</i> vector which specifies integrity check levels for certain checks that should override the default.  For example, if the default vector says <tt>&lt;MGV_H1:E&gt;</tt> then by default check <tt>MGV_H1</tt> is enforced, however you may want certain higher level editors to not be subjected to this constraint in which case you can specify an override vector having this <tt>&lt;MGV_H1:&gt;</tt> which will produce only a warning instead of a violation},
   "ic_applications" => qq{Maps applications to default integrity vectors.  For example, the matrix initializer uses an entry where the application name is <tt>MATRIX_INITIALIZER</tt> and the editing interface uses an entry where the application name is <tt>DEFAULT</tt>.},
   "editor_preferences" => qq{Tracks preference settings used by the editing interface.  In particular, it allows users to specify which frames should open when the interface is started},
   "editors" => qq{Tracks valid meme editors.  Each editor is mapped to a set of initials and an <i>editor level</i> which defines their range of editing privileges.  Currently there are only two levels of editor <tt>0</tt> and <tt>5</tt>.  Level <tt>0</tt> editors have normal privileges and level <tt>5</tt> editors are considered <i>guru</i> editors and can do things like insert new atoms.},
   "editing_matrix" => qq{Tracks aggregate status information for a concept.  This table is used primarily by the <a href="/MEME/Training/glossary.html#matrix_initializer">matrix initializer</a> to aggregate status information about a concepts classes, attributes, and relationships.  Furthermore, it tracks any integrity violations that are found at the time of the matrix calculation.},
   "deleted_cuis" => qq{Not yet being effectively used, this table tracks important information associated with deleted CUIs.  Other tables manage mappings of deleted CUIs, but this table retains information such as what strings and sources at one time occupied that concept so that the CUI has some possibility for being resurrected in the future.},
   "dead_stringtab" => qq{Contains <i>deleted</i> long attributes.},
   "dead_sg_attributes" => qq{Contains <i>deleted</i> SG attributes.},
   "dead_sg_relationships" => qq{Contains <i>deleted</i> SG relationships.},
   "dead_relationships" => qq{Contains <i>deleted</i> relationships.},
   "dead_word_index" => qq{Contains lowercased words <i>deleted</i> atom names.},
   "dead_normwrd" => qq{Contains normalized index words of <i>deleted</i> atoms.},
   "dead_normstr" => qq{Contains normalized index strings of <i>deleted</i> atoms.},
   "dead_context_relationships" => qq{Contains <i>deleted</i> context relationships.},
   "dead_concept_status" => qq{Contains <i>deleted</i> concepts.},
   "dead_classes" => qq{Contains <i>deleted</i> atoms.},
   "dead_attributes" => qq{Contains <i>deleted</i> attributes.},
   "dead_atoms" => qq{Contains names of <i>deleted</i> atoms.},
   "dead_atomic_actions" => qq{Serves as a repository for rows removed from dead atomic actions.  Occasionally rows are removed from atomic actions in violation of the <i>action model</i> for efficiency reasons, to preserve the record of data changes we keep these actions around to preserve our ability to reconstruct what happened.},
   "dba_cutoff" => qq{This view is used as a <i>switch</i> to control whether or not editors are allowed in the database},
   "system_status" => qq{Generic mechanism for applications that need an ON/OFF switch. Specifically, it aggregates the functionality of <a href="dba_cutoff.html"><tt>dba_cutoff</tt></a> and <a href="ic_system_status"><tt>ic_system_status</tt></a> which are now views into this table.},
   "cui_map" => qq{Tracks the fate of CUIs that are no longer active.  Eventually this table will become core data and will support direct editing.  Editors will create mappings between previously deleted cuis and currently active ones.},
   "code_map" => qq{Contains a variety of useful mappings.  This table is used for mapping almost every kind of code in the <i>MEME</i> system to a relevant value.  To see the types of codes mapped, look up the rows where the <tt>type='valid_code_type'</tt>.},
   "cui_history" => qq{Contains MRCUI data.  Eventually this table will be deprecated by the <a href="cui_map.html"><tt>cui_map</tt></a> table, but for now it tracks all cui dependences across versions of the Metathesaurus.},
   "action_log" => qq{Tracks all non-implied actions in the database. Every data change negotiated by MEME4 will be logged here.},
   "activity_log" => qq{Tracks major actions in the database.  Things like matrix init runs, CUI assignment, and insertion steps.},
   "application_help" => qq{For use in a data-driven help application.  Also, components could load their own help info into this table when they are loaded into the database.},
   "application_versions" => qq{For use in a data-driven software version dependency tracking application.  This table should track each version of every component, including which is the current version.},
   "atomic_actions" => qq{Log of all row-level changes to core data in the <i>MID</i>.  In theory, the <i>action model</i> guarantees that core data is <i>only</i> manipulated by atomic actions.},
   "atom_ordering" => qq{Maintains an ordering of atom_ids.  This ordering is used to sort content to place on worklists for editing.},
   "atoms" => qq{Holds atom names for each atom. This table is redundant because the data is in <a href="string_ui.html"><tt>string_ui.string</tt></a>.},
   "attributes" => qq{Contains the attributes, including definitions, semantic types, atom/concept notes, source-specific attributes, etc.  This is a <i>core</i> table.},
   "authority_groups" => qq{This table is not used.},
   "aui_history" => qq{Tracks history of AUI movement (birth,death,splits, etc).},
   "authority_rank" => qq{This table is not used but would contain the list of valid authority values with ranks.},
   "classes" => qq{This table contains the actual terms grouped together into synonymy classes, or concepts.  This is a <i>core</i> table},
   "foreign_attributes" => qq{This table contains attributes connected to the terms from languages other than <i>English</i>. Each row in this table is linked to a row in <a href="foreign_classes.html"><tt>foreign_classes</tt></a> by <tt>atom_id</tt>. This is a <i>core</i> table},
   "foreign_classes" => qq{This table contains the terms from languages other than <i>English</i>. Each row in this table is linked to a row in <a href="classes.html"><tt>classes</tt></a>. This is a <i>core</i> table},
   "coc_headings" => qq{Contains our representation of the Medline and other COC data.  Each <i>article</i> is given a citation set id (for Medline from the <tt>PMID</tt> tag)number and any two heading ids (<tt>atom_id</tt>s) that have the same citation set id are considered to be co-occurring headings.},
   "coc_subheadings" => qq{Contains subheadings that qualify headings in the Medline citation data.},
   "concept_status" => qq{Contains one row for each concept in the database.  This table is used to track high level information about a concept, such as the last time the concept was approved. This is a <i>core table</i>.},
   "context_relationships" => qq{Contains hierarchical relationships between atoms in the <i>MID</i>.  The data is represented as PAR and SIB rels with the source-dependent hierarchical code and meme_id tree positions intact. This is a <i>core table</i>},
   "content_views" => qq{Contains one entry for each defined content view.  Membership in the concept view is indicated by entries in <a href="content_view_members.html"><tt>content_view_members</tt></a>.},
   "content_view_members" => qq{Contains Metathesaurus UIs (AUI, RUI, ATUI, CUI) indicating membership in a content view.  Links back to <a href="content_views.html"><tt>content_views</tt></a> via the <tt>content_view_id</tt> field.},
   "available_elements" => qq{Tracks elements that are ready to receive new MRD states.  It is not used at the moment (4/4/2001)},
   "clean_concepts" => qq{Tracks concepts that have elements ready to receive new MRD states.  A concept gets added, if there was an action after which the concept is in a clean state and is removed when an action happens which leaves this concept no longer clean. In particular, concepts deleted.   They get deleted from this table if they get expired in the MRD.},
   "connected_concepts" => qq{Tracks connections between concepts arising from atomic_actions which change <tt>aui</tt>, <tt>aui_1</tt>, <tt>aui_2</tt>, <tt>concept_id</tt>, <tt>concept_id_1</tt> or <tt>concept_id_2</tt>.  See <tt>MEME_APROCS.connect_concepts</tt>.},
   "connected_set" => qq{This table is used during the computation of a connected set of concepts by the MRDStateManager},
   "connected_sets" => qq{This table is used to track multiple sets of connected concepts.  It is used by the MRDStateManager when batch actions are used to change the core tables.},
   "dtd_versions" => qq{This table tracks the document type definitions used by the MRD.},
   "event_queue" => qq{The MRDSyncManager creates MRDEvent documents from the MID tables molecular_actions and operations_queue.  Those event documents are loaded into this table.  The MRDState engine reads events in mid_event_id order from this table.},
   "events_processed" => qq{Once the MRDStateManager processes events from the event_queue, they are moved into this table for permanent storage.  They are removed at that point from event_queue.},
   "extraction_history" => qq{This table is used by the MRDSyncManager to remember what data changes have been extracted from the MID.  When the application starts, it looks up the data in this table to determine where to start extracting new data changes.},
   "feedback_queue" => qq{The MIDSyncManager reads events from this table and applies the data changes to the MID.  The documents in this table will be MRDEvents.},
   "registered_handlers" => qq{This table contains class names of components which handle various stages in the processing of MRD data and release states.},
   "release_history" => qq{This table contains a history of releases made by the MRD.},
   "mrd_content_views" => qq{Tracks content view states over time.  Membership in the concept view is indicated by entries in <a href="mrd_content_view_members.html"><tt>mrd_content_view_members</tt></a>.},
   "mrd_content_view_members" => qq{Tracks content view members over time.  Links back to <a href="mrd_content_views.html"><tt>mrd_content_views</tt></a> via the <tt>content_view_id</tt> field.},
   "mrd_validation_results" => qq{This table tracks results from the <i>MRD Validation</i> system.},
   "mrd_validation_queries" => qq{This table holds queries used by the <tt>$MEME_HOME/bin/validate_mrd.pl</tt> script to validate various aspects of the MRD database.},
   "mrd_attributes" => qq{This table tracks all releasable states for attributes. New states and expired states are computed from the data in the MID <a href="attributes.html"><tt>attributes</tt></a> table.  Data for the following release files is produced from this table: <tt>MRDEF</tt>, <tt>MRHIST</tt>, <tt>MR(S)MAP</tt>, and <tt>MRSAT</tt>. },
   "mrd_classes" => qq{This table tracks all releasable states for atoms.  New states and expired states are computed based on the MID <a href="classes.html"><tt>classes</tt></a> table.  Data for the following release files is produced from this table: <tt>AMBIGLUI.RRF</tt>, <tt>AMBIGSUI.RRF</tt>, <tt>MRCONSO.RRF</tt>, <tt>MRX*.RRF</tt>.},
   "mrd_coc_headings" => qq{This table is part of a two-table representation of co-occurrence data in the MRD.  States are added/expired whenever the COC data changes.},
   "mrd_coc_subheadings" => qq{This table is the second part of the two-table representation of co-occurrence data in the MRD.  Headings can co-occur in a citation and have certain subheadings connected to them.  Those subheading relationships are reprsented here.},
   "mrd_column_statistics" => qq{This table is used to track statistics about columns for various release files. Eventually it is used in the building of <tt>MRCOLS</tt>.},
   "mrd_concepts" => qq{This table tracks mrd states that come from concept_status.  It can be used to look up a list of CUIs that were actvie during a particular time.},
   "mrd_file_statistics" => qq{This table is used to track statistics about the various release files. Eventually it is used in the building of <tt>MRFILES</tt>.},
   "mrd_properties" => qq{This table tracks configuration data for <tt>MRCOLS/MRFILES</tt> and potentially other system config data.  To accurately reconstruct an old <tt>MRCOLS</tt>, we need the old data so this tracks the changes to the properties over time. This table maintains the <a href="meme_properties.html"><tt>meme_properties</tt></a> over time.},
   "mrd_relationships" => qq{This table tracks all releasable states for relationships.  New states and expired states are computed based on the MID <a href="relationships.html"><tt>relationships</tt></a> table.  Data for the following release files is produced from this table: <tt>MRREL</tt>.},
   "mrd_contexts" => qq{This table tracks all releasable states for hierarchies/contexts.  New states and expired states are computed based on the MID <a href="context_relationships.html"><tt>context_relationships</tt></a> table.  Data for the following release files are produced from this table: <tt>MRCXT</tt>, <tt>MRHIER</tt>.},
   "mrd_source_rank" => qq{This table tracks mrd states for sources.  The data comes from <a href="source_rank.html"><tt>source_rank</tt></a>.},
   "mrd_stringtab" => qq{This table represents mrd long attribute valuestates.  This is basically a historical version of <a href="stringtab.html"><tt>stringtab</tt></a>.},
   "mrd_termgroup_rank" => qq{This table tracks mrd states for termgroups.  The data for this table comes from <a href="termgroup_rank.html"><tt>termgroup_rank</tt></a>.},
   "tmp_classes" => qq{This table is a staging area for classes before insertion into the MRD.},
   "tmp_relationships" => qq{This table is a staging area for relationships before insertion into the MRD.},
   "tmp_attributes" => qq{This table is a staging area for attributes before insertion into the MRD.},
   "tmp_concepts" => qq{This table is a staging area for concepts before insertion into the MRD.},
   "" => ""
   );

#
# Role of tables in insertion
#
%insertion_roles = 
  (
   "source_coc_headings_todelete" => qq{When updating coc heading information from Medline update files (<tt>$MEME_HOME/bin/update_medline_data.csh</tt>) citation set ids to delete are loaded into this table by the <tt>$MEME_HOME/bin/process_medline_data.csh</tt> script, then removed from the <a href="coc_headings.html"><tt>coc_headings</tt></a> and <a href="coc_subheadings.html"><tt>coc_subheadings</tt></a> tables.},
   "source_coc_headings" => qq{During an insertion, citation information is loaded into this table by the <tt>$MEME_HOME/bin/process_medline_data.csh</tt> script, then it is loaded into <a href="coc_headings.html"><tt>coc_headings</tt></a>.},
   "source_coc_subheadings" => qq{During an insertion, citation subheading information is loaded into this table by the <tt>$MEME_HOME/bin/process_medline_data.csh</tt> script, then it is loaded into <a href="coc_subheadings.html"><tt>coc_subheadings</tt></a>.},
   "sims_info" => qq{This table is populated manually during the inversion through the use of <a href="/cgi-lti-oracle/SIMS.cgi">SIMS</a>. Additional information is added during the insertion process from <a href="/MEME/Data/src_format.html#ranks">sources.src</a>.},
   "sr_predicate" => qq{During the insertion of an update source, this table is populated by one or more calls to <a href="/MEME/Documentation/plsql_msp.html#safe_replacement"><tt>MEME_SOURCE_PROCESSING.safe_replacement</tt></a>.},
   "source_version" => qq{When inserting an update source, this table is updated to reflect the current/previous versions of that source.},
   "source_termgroup_rank" => qq{ First, they are loaded from <a href="/MEME/Data/src_format.html#termgroups"><tt>termgroups.src</tt></a>, then the termgroups are prepared for insertion and finally they are loaded into <a href="termgroup_rank.html"><tt>termgroup_rank</tt></a>.},
   "source_string_ui" => qq{  First, they are loaded from <a href="/MEME/Data/src_format.html#strings"><tt>strings.src</tt></a>, then the strings are prepared for insertion and finally they are loaded into <a href="string_ui.html"><tt>string_ui</tt></a>.},
   "source_stringtab" => qq{First, they are loaded from <a href="/MEME/Data/src_format.html#attributes"><tt>attributes.src</tt></a>, then the data is loaded into <a href="stringtab.html"><tt>stringtab</tt></a>.},
   "source_source_rank" =>  qq{First, they are loaded from <a href="/MEME/Data/src_format.html#sources"><tt>sources.src</tt></a>, then the sources are prepared for insertion and finally they are loaded into <a href="source_rank.html"><tt>source_rank</tt></a>.},
   "source_rank" => qq{When data for a new or update source is going to be inserted, we first insert information about that source into this table.  The data is loaded from <a href="source_source_rank.html"><tt>source_source_rank</tt></a>.},   
   "termgroup_rank" => qq{During a source insertion, we load into this table all termgroups used by the atoms within the source being inserted.  The data is loaded from <a href="source_termgroup_rank.html"><tt>source_termgroup_rank</tt></a>.  This table is also used to compute the ranks of the new atoms and determine the new preferred atom ids for each concept.}, 
   "source_id_map" => qq{During source insertion, this table is used to map identifiers used in the files (<i>source ids</i>) to identifiers used in the <i>MID</i> (<i>meme ids</i>).  For example, it maps <tt>source_atom_ids</tt> to <tt>atom_ids</tt>.},
   "source_context_relationships" => qq{ First, they are loaded from <a href="/MEME/Data/src_format.html#contexts"><tt>contexts.src</tt></a>, then the context relationships are prepared for insertion and finally they are loaded into <a href="context_relationships.html"><tt>context_relationships</tt></a>.},
   "source_concept_status" => qq{ Data is generated from the <a href="source_classes_atoms.html"><tt>source_classes_atoms</tt></a> table, then the concepts are loaded into <a href="concept_status.html"><tt>concept_status</tt></a>.},
   "source_classes_atoms" => qq{During an insertion, atoms are loaded into this table from <a href="/MEME/Data/src_format.html#classes_atoms"><tt>classes_atoms.src</tt></a>, then prepared for insertion and finally they are loaded into <a href="classes.html"><tt>classes</tt></a> and <a href=".html"><tt>atoms</tt></a>, or into  <a href="foreign_classes.html"><tt>foreign_classes</tt></a>.},
   "source_relationships" => qq{During an insertion, relationships are loaded into this table from <tt><a href="/MEME/Data/src_format.html#relationships">relationships.src</a></tt>, then prepared for insertion and finally they are loaded into <a href="relationships.html"><tt>relationships</tt></a>.},
   "source_attributes" => qq{During an insertion, attributes are loaded into this table from <tt><a href="/MEME/Data/src_format.html#attributes">attributes.src</a></tt>, then prepared for insertion and finally they are loaded into <a href="attributes.html"><tt>attributes</tt></a>.},
   "source_replacement" => qq{During an insertion, identifiers are loaded into this table from <tt><a href="/MEME/Data/src_format.html#replacement">replacement.src</a></tt>.  This data is used during an update insertion to preserver part of an older version of a source that has not changed in the update.},
   "operations_queue" => qq{During a source insertion, XML documents containing a view of the data at insertion time are inserted into this table so that these additions to the <i>MID</i> can be set to the <i>MRD</i>.},
   "mom_safe_replacement" => qq{During the insertion of an update source, this table is populated by one or more calls to <a href="/MEME/Documentation/plsql_msp.html#safe_replacement"><tt>MEME_SOURCE_PROCESSING.safe_replacement</tt></a>.  These calls rank each potential replacement fact based on the similarity of the atoms involved.  This allows processes later to determine the <i>best</i> safe replacement fact by finding the <tt>max(rank)</tt> for a given <tt>old_atom_id</tt> and <tt>new_atom_id</tt>.},
   "level_status_rank" => qq{When any of the <a href="/MEME/Documentation/plsql_ma.html"><tt>MEME_APROCS.aproc_insert</tt> procedures</a> are called, this table is used to validate the <tt>status</tt> and level (<tt>relationship_level</tt> or <tt>attribute_level</tt>) fields.},
   "mom_merge_facts" => qq{This table is loaded just before the <i>merge engine</i> is run by <a href="/MEME/Documentation/plsql_msp.html#load_facts"><tt>MEME_SOURCE_PROCESSING.load_facts</tt></a>.  },
   "mom_precomputed_facts" => qq{This table is loaded from <tt><a href="/MEME/Data/src_format.html#mergefacts">mergefacts.src</a></tt> by the script <tt>$MEME_HOME/bin/load_src.csh</tt>.  Once the table is loaded, the <tt>sg_id</tt> fields are mapped to valid <tt>atom_id</tt> and <tt>concept_id</tt> fields by <a href="/MEME/Documentation/plsql_msp.html#prepare_src_mergefacts"><tt>MEME_SOURCE_PROCESSING.prepare_src_mergefacts</tt></a>.},
   "mom_facts_processed" => qq{After a merge set has been processed, the resulting facts are loaded into this table by <a href="/MEME/Documentation/plsql_msp.html#move_processed_facts"><tt>MEME_SOURCE_PROCESSING.move_processed_facts</tt></a>.},
   "mom_exclude_list" => qq{Used by <a href="/MEME/Documentation/plsql_msp.html#filter_facts"><tt>MEME_SOURCE_PROCESSING.filter_facts</tt></a> to remove merge facts from consideration which have termgroups on the list.},
   "mom_norm_exclude_list" => qq{Used by <a href="/MEME/Documentation/plsql_msp.html#filter_facts"><tt>MEME_SOURCE_PROCESSING.filter_facts</tt></a> to remove merge facts from consideration which have termgroups on the list.},
   "mom_candidate_facts" => qq{This table is used by the <i>merge engine</i> as a staging area for merge facts to process.  This table is loaded by either the <a href="/MEME/Documentation/plsql_msp.html#prepare_src_mergefacts"><tt>MEME_SOURCE_PROCESSING.prepare_src_mergefacts</tt></a> or by <a href="/MEME/Documentation/plsql_msp.html#generate_facts"><tt><tt>MEME_SOURCE_PROCESSING.generate_facts</tt></a>},
   "molecular_actions" => qq{Any actions performed during source insertion are logged as either <tt>MOLECULAR</tt> or <tt>MACRO</tt> actions.},
   "meme_work" => qq{Every source insertion has a <tt>work_id</tt> associated with it that can be found here.},
   "meme_progress" => qq{Some of the long running insertion processes (like setting of rank fields) log their progress in here.},
   "meme_error" => qq{If any major errors happen during the insertion process, they are logged here.},
   "max_tab" => qq{When new core data is added during the insertion process, the <i>meme ids</i> used are generated from the max values held in this table.  The same goes for the new string identifiers (SUI, LUI, ISUI).},		       
   "ic_single" => qq{A major insertion process is the running of the <i>merge engine</i> which often takes a set of merge inhibitors which it uses to prevent certain merges.  Some merge inhibitors are known as <i>unary integrity checks</i> which means that they require one pieces of information drawn from a list in order to function (e.g. <tt>MGV_B</tt> which requires a list of sources).  That information comes from this table.},
   "ic_pair" => qq{A major insertion process is the running of the <i>merge engine</i> which often takes a set of merge inhibitors which it uses to prevent certain merges.  Some merge inhibitors are known as <i>binary integrity checks</i> which means that they require two pieces of information drawn from a list in order to function (e.g. <tt>MGV_K</tt> which requires a list of pairs of sources).  That information comes from this table.},
   "action_log" => qq{Each insertion operation that changes the database will create an entry.},
   "activity_log" => qq{Each major insertion step adds a row.  To see all of the <i>activities</i> associated with a source insertion, find that insertion's <tt>work_id</tt> and look up all rows in this table having a matching <tt>work_id</tt>.},
   "atomic_actions" => qq{One row is added to this table for each core table row that is inserted.},
   "atom_ordering" => qq{Loaded when <a href="classes.html"><tt>classes</tt></a> is loaded, from the source_classes_atoms table},
   "atoms" => qq{Atom names are loaded into this table by <a href="/MEME/Documentation/plsql_mba.html#macro_insert"><tt>MEME_BATCH_ACTIONS.macro_insert</tt></a>.},
   "foreign_attributes" => qq{Foreign attributes are one type of core data loaded during the source insertion process.  There are several steps:<ul><li>Generate stringtab data (<tt>\$MEME_HOME/bin/atts_to_stringtab.csh</tt>)</li><li>Load attribute and stringtab data into source tables (<tt>\$MEME_HOME/bin/load_src.csh</tt>)</li><li>Assign attribute and string ids (<a href="/MEME/Documentation/plsql_msp.html#assign_meme_ids"><tt>MEME_SOURCE_PROCESSING.assign_meme_ids</tt></a>)</li><li>Insert <tt>source_id_map</tt> rows (<a href="/MEME/Documentation/plsql_msp.html#insert_source_ids"><tt>MEME_SOURCE_PROCESSING.insert_source_ids</tt></a>)</li><li>Map attribute sg_ids to atom/concept ids (<a href="/MEME/Documentation/plsql_msp.html#map_to_meme_ids"><tt>MEME_SOURCE_PROCESSING.map_to_meme_ids</tt></a>)</li><li>Insert attributes and stringtab rows into <i>live</i> core tables (<a href="/MEME/Documentation/plsql_msp.html#core_table_insert"><tt>MEME_SOURCE_PROCESSING.foreign_attributes_insert</tt></a>)</li></ul>},
   "attributes" => qq{Attributes are one type of core data loaded during the source insertion process.  There are several steps:<ul><li>Generate stringtab data (<tt>\$MEME_HOME/bin/atts_to_stringtab.csh</tt>)</li><li>Load attribute and stringtab data into source tables (<tt>\$MEME_HOME/bin/load_src.csh</tt>)</li><li>Assign attribute and string ids (<a href="/MEME/Documentation/plsql_msp.html#assign_meme_ids"><tt>MEME_SOURCE_PROCESSING.assign_meme_ids</tt></a>)</li><li>Insert <tt>source_id_map</tt> rows (<a href="/MEME/Documentation/plsql_msp.html#insert_source_ids"><tt>MEME_SOURCE_PROCESSING.insert_source_ids</tt></a>)</li><li>Map attribute sg_ids to atom/concept ids (<a href="/MEME/Documentation/plsql_msp.html#map_to_meme_ids"><tt>MEME_SOURCE_PROCESSING.map_to_meme_ids</tt></a>)</li><li>Insert attributes and stringtab rows into <i>live</i> core tables (<a href="/MEME/Documentation/plsql_msp.html#core_table_insert"><tt>MEME_SOURCE_PROCESSING.core_table_insert</tt></a>)</li></ul>},
   "sg_attributes" => qq{When <a href="attributes.html"><tt>attributes</tt></a> are inserted, any rows with sg identifiers other than <tt>ATOM_ID</tt> or <tt>SOURCE_ATOM_ID<tt> are also stored in this table, so that the original state of the attribute is preserved. },
   "sg_relationships" => qq{When <a href="relationships.html"><tt>relationships</tt></a> are inserted, any rows with sg identifiers other than <tt>ATOM_ID</tt> or <tt>SOURCE_ATOM_ID<tt> are also stored in this table, so that the original state of the relationship is preserved. },
   "classes" => qq{Classes are one type of core data loaded during the source insertion process.  There are several steps:<ul><li>Generate string_ui data <tt>\$MEME_HOME/bin/classes_to_strings.csh</tt></li><li>Load string_ui and classes data into source tables <tt>\$MEME_HOME/bin/load_src.csh</tt></li><li>Assign string identifiers - SUI,ISUI,LUI (<a href="/MEME/Documentation/plsql_msp.html#assign_string_uis"><tt>MEME_SOURCE_PROCESSING.assign_string_uis</tt></a>)</li><li>Assign AUIs (<a href="/MEME/Documentation/plsql_msp.html#assign_auis"><tt>MEME_SOURCE_PROCESSING.assign_auis</tt></a>)</li><li>Assign atom ids (<a href="/MEME/Documentation/plsql_msp.html#assign_meme_ids"><tt>MEME_SOURCE_PROCESSING.assign_meme_ids</tt></a>)</li><li>Generate source_concept_status rows for new atoms</li><li>Perform any pre-insert merging (<a href="/MEME/Documentation/plsql_msp.html#replacement_merges"><tt>MEME_SOURCE_PROCESSING.replacement_merges</tt></a>)</li><li>Load classes and string_ui rows into <i>live</i> core tables (<a href="/MEME/Documentation/plsql_msp.html#core_table_insert"><tt>MEME_SOURCE_PROCESSING.core_table_insert</tt></a>)</li></ul>},
   "foreign_classes" => qq{Foreign classes are one type of core data loaded during the source insertion process.  There are several steps:<ul><li>Generate string_ui data <tt>\$MEME_HOME/bin/classes_to_strings.csh</tt></li><li>Load string_ui and classes data into source tables <tt>\$MEME_HOME/bin/load_src.csh</tt></li><li>Assign string identifiers - SUI,ISUI,LUI (<a href="/MEME/Documentation/plsql_msp.html#assign_string_uis"><tt>MEME_SOURCE_PROCESSING.assign_string_uis</tt></a>)</li><li>Assign atom ids (<a href="/MEME/Documentation/plsql_msp.html#assign_meme_ids"><tt>MEME_SOURCE_PROCESSING.assign_meme_ids</tt></a>)</li><li>Generate source_concept_status rows for new atoms</li><li>Map the new atoms to existing rows in <a href="classes.html"><tt>classes</tt></a>.</li><li>Load classes and string_ui rows into <i>live</i> core tables (<a href="/MEME/Documentation/plsql_msp.html#foreign_classes_insert"><tt>MEME_SOURCE_PROCESSING.foreign_classes_insert</tt></a>)</li></ul>},         
   "coc_headings" => qq{A complex process is used to load this data from about 40GB of Medline XML files.  The primary scripts involved are the <tt>\$MEME_HOME/bin</tt> scripts: <tt>medline_parser.pl</tt> (which uses <tt>MedlineHandler.pm</tt>), <tt>update_medline_data.pl</tt>, and <tt>process_medline_data.csh</tt>.  There are two flavors of Medline XML files: <i>initialization</i>, and <i>update</i>.  The initialization files come when the whole set is recomputed for a new version of MSH (baseline), the updates should come at other times throughout the year.  The initialization files can all be processed at once by the parser script and loaded by the .csh script.  The update files should be processed in chronological order, one at a time because they main contain indications to delete or update records from earlier files in the update set (this is handled by <tt>update_medline_data.pl</tt>.},
   "coc_subheadings" => qq{This table is loaded by the same process that loads <a href="coc_headings.html"><tt>coc_headings</tt></a>.},
   "concept_status" => qq{Concepts are one type of core data that is loaded during the source insertion process.  There are several steps:<ul><li>Generate new concepts from <a href="source_classes_atoms.html"><tt>source_classes_atoms</tt></a></li><li>Assign concept_ids</li> <li>Load concepts into <i>live</i> core table</li><ul>},
   "relationships" => qq{Relationships are one type of core data loaded during the source insertion process.  There are several steps:<ul><li>Load the data into the <a href="source_context_relationships.html"><tt>source_context_relationships</tt></a> table from a <a href="/MEME/Data/src_format.html#relationships"><tt>relationships.src</tt></a> file.</li><li>Assign meme ids</li><li>Insert source_id_map rows</li><li>Map the source data to atom and concept ids</li><li>Insert the rows into the <i>live</i> <tt>relationships</tt> table</li></ul>},
   "context_relationships" => qq{Context rels are one type of core data loaded during the source insertion process.  There are several steps:<ul><li>Generate a relationship format from the context format (.raw3)</li><li>Load the data into the <a href="source_context_relationships.html"><tt>source_context_relationships</tt></a> table</li><li>Assign meme ids</li><li>Insert source_id_map rows</li><li>Map the source data to atom and concept ids</li><li>Insert the rows into the <i>live</i> <tt>context_relationships</tt> table</li></ul>},
   "stringtab" => qq{When <a href="attributes.html"><tt>attributes</tt></a> are loaded by the source insertion process, any rows that have an attribute value longer than 100 characters also get loaded here.},
   "string_ui" => qq{When  <a href="classes.html"><tt>classes</tt></a> is loaded by the source insertion process, any strings that have never yet existed in the system are assigned SUI, LUI, and ISUI values and loaded into this table.  This operation is performed by <a href="/MEME/Documentation/plsql_msp.html#assign_string_uis"><tt>MEME_SOURCE_PROCESSING.assign_string_uis</tt><a>.},
   "atoms_ui" => qq{When  <a href="classes.html"><tt>classes</tt></a> is loaded by the source insertion process, any combination of stripped source, sui, term type and code that have never yet existed in the system are assigned AUI values and loaded into this table.  This operation is performed by <a href="/MEME/Documentation/plsql_msp.html#assign_auis"><tt>MEME_SOURCE_PROCESSING.assign_auis</tt><a>.},
   "relationships_ui" => qq{When  <a href="relationships.html"><tt>relationships</tt></a> is loaded by the source insertion process, any combination of stripped source, level, name, attribute, and connected ids that have never yet existed in the system are assigned RUI values and loaded into this table.  The RUi for the inverse is also loaded.  This operation is performed by <a href="/MEME/Documentation/plsql_msp.html#assign_ruis"><tt>MEME_SOURCE_PROCESSING.assign_ruis</tt><a>.},
   "inverse_relationships_ui" => qq{When  <a href="relationships_ui.html"><tt>relationships_ui</tt></a> is loaded, an entry is added to this table for each RUI, inverse RUI pair.},
   "attributes_ui" => qq{When  <a href="attributes.html"><tt>attributes</tt></a> is loaded by the source insertion process, any combination of stripped source, level, name, value (MD5 hash), and connected id that have never yet existed in the system are assigned ATUI values and loaded into this table.  This operation is performed by <a href="/MEME/Documentation/plsql_msp.html#assign_atuis"><tt>MEME_SOURCE_PROCESSING.assign_atuis</tt><a>.},
   "" => ""
   );

#
# Role of tables in editing
#
%editing_roles = 
  (
   "srstre2" => qq{Used by the <a href="/MEME/Documentation/plsql_mip.html#styisa"><tt>MEME_INTEGRITY_PROC.styisa</tt></a> qa bin.},
   "source_rank" => qq{During the editing cycle, this table must be maintained using the <a href="/cgi-lti-oracle/src_info.cgi">SRC Info Editor</a>.  Much of the semantics of this table can be validated using the <a href="cgi-lti-oracle/validate_mid.cgi">MID Validation System</a>.},
   "sg_attributes" => qq{During the editing cycle, CUIs are reassigned in the <i>MID</i> on a daily basis.  Each time this assignment takes place, the system re-maps (to new <tt>atom_id</tt>/<tt>concept_id</tt>) any attributes that were originally connected to a CUI.  This table is used to find attributes originally connected to CUI values.},
   "sg_relationships" => qq{During the editing cycle, CUIs are reassigned in the <i>MID</i> on a daily basis.  Each time this assignment takes place, the system re-maps (to new <tt>atom_id</tt>/<tt>concept_id</tt>) any relationships that were originally connected to a CUI.  This table is used to find relationships originally connected to CUI values.},
   "qa_diff_results" => qq{Used by the <a href="/cgi-lti-oracle/automated_qa.cgi">Automated QA System</a>.},
   "qa_diff_adjustment" => qq{Used by the <a href="/cgi-lti-oracle/automated_qa.cgi">Automated QA system</a> to adjust counts that are off for known reasons.},
   "src_qa_queries" => qq{Used by the <a href="/cgi-lti-oracle/automated_qa.cgi">Automated QA System</a> to generate counts from the <i>source core tables</i>. The queries can be edited online.},
   "src_qa_results" => qq{These results can be compared against the <i>MID</i> results in <a href="mid_qa_results.html"><tt>mid_qa_results</tt></a> by using the <a href="/cgi-lti-oracle/automated_qa.cgi">Automated QA system</a>.},
   "src_obsolete_qa_results" => qq{These results can be compared against the <i>SRC</i> results in <a href="src_qa_results.html"><tt>src_qa_results</tt></a> by using the <a href="/cgi-lti-oracle/automated_qa.cgi">Automated QA system</a>.},
   "qa_adjustment" => qq{Used by the <a href="/cgi-lti-oracle/automated_qa.cgi">Automated QA system</a> to adjust counts that are off for known reasons.},
   "operations_queue" => qq{Any UNDO or REDO operations performed during the editing cycle are logged here.  An operations that call the atomic insert actions will cause XML document fragments representing the initial state of the inserted data to be stored here for synchronization with <i>MRD</i>.  Finally, any changes to non-core tables are logged also XML document fragments for <i>MRD</i> synchronization.},
   "normwrd" => qq{During the editing cycle, this table is used by the <i>Finder</i> to perform normalized word lookups in the <a href="classes.html"><tt>classes</tt></a> table.},
   "normstr" => qq{During the editing cycle, this table is used by the <i>Finder</i> to perform normalized string lookups in the <a href="classes.html"><tt>classes</tt></a> table.},
   "word_index" => qq{During the editing cycle, this table is used by the <i>Finder</i> to perform word lookups in the <a href="classes.html"><tt>classes</tt></a> table.},
   "nhsty" => qq{This data drives the <a href="/MEME/Documentation/plsql_mip.html"><tt>MEME_INTEGRITY_PROC.nhsty</tt></a> integrity check.},
   "mid_validation_results" => qq{During the editing cycle, the <i>MID Validation System</i> should be used to look for known types of errors in the database.  This data-driven qa system uses this table to store results.},
   "mid_validation_queries" => qq{During the editing cycle, the <a href="/cgi-lti-oracle/validate_mid.cgi">MID Validation System</a> should be used to look for known types of errors in the database.  This data-driven qa system uses queries from this table.},
   "mid_qa_queries" => qq{During the editing cycle, the <a href="/cgi-lti-oracle/automated_qa.cgi">Automated QA System</a> should run once a week and generate database-wide qa counts based on queries in this table.},
   "mid_qa_history" => qq{During the editing cycle, the <a href="/cgi-lti-oracle/automated_qa.cgi">Automated QA System</a> should run once a week and generate database-wide qa counts.  Counts for past runs are archived in this table and can be compared against the most recent counts found in <a href="mid_qa_results.html"><tt>mid_qa_results</tt></a>.},
   "mid_qa_results" => qq{During the editing cycle, the <a href="/cgi-lti-oracle/automated_qa.cgi">Automated QA System</a> should run once a week and generate database-wide qa counts.  Counts for the most recent run are stored in this table and can be compared against counts from past runs found in <a href="mid_qa_history.html"><tt>mid_qa_history</tt></a>.},
   "molecular_actions" => qq{During the editing cycle, actions performed by automated processes or by editors are logged here using one row per action.},
   "meme_work" => qq{Every matrix initializer and cui assignment run has a <tt>work_id</tt> associated with it.  Eventually, editor sessions, or editors should be assigned <tt>work_ids</tt> so that their work over time can be tracked in the action tables.},
   "meme_progress" => qq{The <a href="/MEME/Training/glossary.html#matrix_initializer">matrix initializer</i> logs its progress here.},
   "meme_tables" => qq{The various mid-wide system management procedures in <a href="/MEME/Documentation/plsql_ms.html">MEME_SYSTEM</a> make use of this table (e.g. <a href="/MEME/Documentation/plsql_ms.html#analyze_mid">MEME_SYSTEM.analyze_mid</a>.},
   "meme_indexes" => qq{During the editing cycle, the core tables are reindexed daily and the structure of those indexes is taken from this table and from <a href="meme_ind_columns.html"><tt>meme_ind_columns</tt></a>.  The various reindexing procedures in <a href="/MEME/Documentation/plsql_ms.html">MEME_SYSTEM</a> make use of this table.},
   "meme_ind_columns" => qq{During the editing cycle, the core tables are reindexed daily and the structure of those indexes is taken from this table and from <a href="meme_indexes.html"><tt>meme_indexes</tt></a>. The various reindexing procedures in <a href="/MEME/Documentation/plsql_ms.html">MEME_SYSTEM</a> make use of this table.},
   "meme_error" => qq{If any major errors happen during the editing cycle, they are logged here.},
   "max_tab" => qq{Every time an action happens, the action identifiers are incremented in this table and used to log the action.  Each time a piece of core data is inserted, or a new string is inserted, the max identifiers for those things are incremented and used.},
   "level_status_rank" => qq{Same as insertion role.},
   "tobereleased_rank" => qq{When any of the <a href="/MEME/Documentation/plsql_ma.html"><tt>MEME_APROCS.aproc_insert</tt> procedures</a> are called, this table is used to validate the <tt>tobereleased</tt> field},
   "released_rank" => qq{When any of the <a href="/MEME/Documentation/plsql_ma.html"><tt>MEME_APROCS.aproc_insert</tt> procedures</a> are called, this table is used to validate the <tt>released</tt> field},
   "suppressible_rank" => qq{When any of the <a href="/MEME/Documentation/plsql_ma.html"><tt>MEME_APROCS.aproc_insert</tt> procedures</a> are called, this table is used to validate the <tt>suppressible</tt> field},
   "inverse_rel_attributes" => qq{When reading a concept from the database for a given <tt>concept_id</tt>, relationships must be read in both directions (where <tt>concept_id_1=concept_id</tt> and where <tt>concept_is_2=concept_id</tt>).  When reading the inverse direction (where the current concept we want is the <tt>concept_id_2</tt> of the relationship), we must reverse the sense of the relationship attribute (e.g. <i>mapped_to</i> becomes <i>mapped_from</i>) by mapping it through this table.},
   "inverse_relationships" => qq{When reading a concept from the database for a given <tt>concept_id</tt>, relationships must be read in both directions (where <tt>concept_id_1=concept_id</tt> and where <tt>concept_is_2=concept_id</tt>).  When reading the inverse direction (where the current concept we want is the <tt>concept_id_2</tt> of the relationship), we must reverse the sense of the relationship (e.g. <i>broader</i> becomes <i>narrower</i>) by mapping it through this table.},
   "integrity_constraints" => qq{If during the course of the editing cycle, it becomes important to globally turn integrity checks off or on, this can be done by setting the <tt>ic_status</tt> field for the necessary checks.},
   "ic_override" => qq{By default, the editing interface is configured to override the default integrity vector with the override vector from this table matching the editor\'s editor level},
   "ic_applications" => qq{By default, the editing interface is configured to perform actions using the integrity vector found in this table with the application name <tt>DEFAULT</tt>.},
   "editing_matrix" => qq{The matrix initializer is run daily to determine which concepts still need editing.  The results of its run are (partially) stored in this table, including the aggregated status of the atoms, attributes, and relationships as well as the overall integrity state for each concept.  Concepts that require editing of any of their core elements or require editing for integrity reasons are <i>unapproved</i> by this process.  Generally, if one wants to know why something was unapproved, this table can provide the answer.}, 
   "dead_context_relationships" => qq{In <i>MEME3</i> there are no actions that control the deletion of context relationships, they occupy a <i>pseudo-core</i> data state.  However, on occasion it becomes important to remove rows from the live table with the ability to recover the deletion, so an ad-hoc process is run to store the rows in this table in case they need to be resurrected.},
   "dead_concept_status" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete a concept, a row gets inserted into this table},
   "dead_attributes" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete an attribute, a row gets inserted into this table},
   "dead_stringtab" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete an attribute, any <a href="stringtab.html"><tt>stringtab</tt></a> rows for that attribute get inserted into this table},
   "dead_sg_attributes" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete an attribute, any <a href="sg_attributes.html"><tt>sg_attributes</tt></a> rows for that attribute get inserted into this table},
   "dead_sg_relationships" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete an relationship, any <a href="sg_relationships.html"><tt>sg_relationships</tt></a> rows for that relationship get inserted into this table},
   "dead_relationships" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete a relationship, a row gets inserted into this table},
   "dead_atoms" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete an atom, a row gets inserted into this table},
   "dead_classes" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete an atom, a row gets inserted into this table},
   "dead_normwrd" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete an atom or when an atom is made unreleasable, any <a href="normwrd.html"><tt>normwrd</tt></a> rows for that atom get moved to this table.},
   "dead_normstr" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete an atom or when an atom is made unreleasable, any <a href="normstr.html"><tt>normstr</tt></a> rows for that atom get moved to this table.},
   "dead_word_index" => qq{When <a href="/MEME/Documentation/plsql_ma.html#aproc_change_dead"><tt>MEME_APROCS.aproc_change_dead</tt></a> is called to delete an atom or when an atom is made unreleasable, any <a href="word_index.html"><tt>word_index</tt></a> rows for that atom get moved to this table.},
   "cui_map" => qq{The goal is to make this a <i>core table</i> in <i>MEME4</i> allowing it to be directly manipulated by actions and editors},
   "atomic_actions" => qq{Each row level change to a core table produced by a molecular action is logged in this table.},
   "attributes" => qq{This is one kind of core data that editors can manipulate with molecular actions},
   "classes" => qq{This is one kind of core data that editors can manipulate with molecular actions.},
   "relationships" => qq{This is one kind of core data that editors can manipulate with molecular actions.},
   "concept_status" => qq{This is one kind of core data that editors can manipulate with molecular actions.},
   "" => ""
   );

#
# Role in production
#
%production_roles = 
  (
   "mrd_content_views" => qq{Source of information for the CVF <tt>MRDOC.RRF</tt> entries.},
   "content_views" => qq{Source of information for the CVF <tt>MRDOC.RRF</tt> entries.},
   "content_view_members" => qq{Source of information for the CVF fields of the various content files.},
   "mrd_content_view_members" => qq{Source of information for the CVF fields of the various content files.},
   "sims_info" => qq{Source of information for <tt>MRSAB</tt>.},
   "termgroup_rank" => qq{The ranking of termgroups from this table is used in the production of <tt>MRRANK</tt>.},
   "stringtab" => qq{Data from this table is used in the production of <tt>MRDEF</tt> and <tt>MRSAT</tt> that have values greater than 100 characters.},
   "source_version" => qq{This table is used to determine what the current version of each source is.},
   "source_rank" => qq{This is the source of data for <tt>MRSAB</tt>.},
   "mom_safe_replacement" => qq{This table is also used by the cui assignment algorithm (<a href="/MEME/Documentation/plsql_mo.html#assign_cuis"><tt>MEME_OPERATIONS.assign_cuis</tt></a>) to inherit <tt>last_release_cui</tt> values from an obsolete atom to its replacement.  Additionally, it is used in the computation of <tt>MRCUI</tt>, <tt>MERGED.CUI</tt> and <tt>DELETED.CUI</tt>.},
   "lui_assignment" => qq{This table is used for the computation of <tt>MERGED.LUI</tt> and <tt>DELETED.LUI</tt>.},
   "meme_properties" => qq{This table contains configuration data used to build <tt>MRCOLS</tt> and <tt>MRFILES</tt>.  It also contains CUI ranges used in the building of the <tt>DA</tt> attribute in <tt>MRSAT</tt>.},
   "max_tab" => qq{When new CUIs are assigned for production, this table tracks the previous max CUI value and all new ones are assigned in the range immediately beyond that one.},
   "cui_map" => qq{Eventually this table will be used in the production of MRCUI, thus deprecating <a href="cui_history.html"><tt>cui_history</tt></a>},
   "cui_history" => qq{This table is used in the production of MRCUI.  Essentially MRCUI is built by starting with the previous year's MRCUI and then adding new <tt>SY</tt>, <tt>DEL</tt> and bequeathal rel rows.},
   "attributes" => qq{This table is the source of data for MRSTY, MRDEF, MRSAT, MRATX, and MRLO.},
   "classes" => qq{This table is the source of data for <tt>MRCON</tt> and <tt>MRSO</tt> and defines which termgroups should appear in <tT>MRRANK</tt>.},
   "foreign_attributes" => qq{This table is the source of data for rows in <tt>MRSAT</tt> connected to the non-english rows in <tt>MRCONSO</tt>.},
   "foreign_classes" => qq{This table is the source of data for the non English rows in <tt>MRCON</tt> and <tt>MRSO</tt> and defines which termgroups should appear in MRRANK.},
   "relationships" => qq{This table is the source of data for <tt>MRREL</tt> and for the bequeathal rels in <tt>MRCUI</tt>.},
   "coc_headings" => qq{This data is used in conjunction with <a href="coc_subheadings.html"><tt>coc_subheadings</tt></a> to produce MRCOC data as well as the <tt>MED&lt;year&gt; MRSAT</tt> rows and the <tt>*CITATIONS MRLO</tt> rows.},
   "coc_subheadings" => qq{This data is used in conjunction with <a href="coc_headings.html"><tt>coc_headings</tt></a> to produce MRCOC data as well as the <tt>MED&lt;year&gt; MRSAT</tt> rows and the <tt>*CITATIONS MRLO</tt> rows.},
   "concept_status" => qq{This data is the source of the DA, MR, and ST rows of MRSAT},
   "context_relationships" => qq{This data is used to generate <tt>MRCXT</tt> and the <tt>PAR</tt>, <tt>CHD</tt>, and <tt>SIB</tt> rows in <tt>MRREL</tt>.},
   "" => ""
);


($db,$meme) = @ARGV;
$user = `$ENV{MIDSVCS_HOME}/bin/get-oracle-pwd.pl`;
chop($user);
($justuser) = split /\//, $user;
$db = `$ENV{MIDSVCS_HOME}/bin/midsvcs.pl -s $db`;
chop($db);

#
# Start
#
print "------------------------------------------------------------\n";
print "Starting  ...", scalar localtime,"\n";
print "------------------------------------------------------------\n";
print "db:         $db\n";
print "user:       $justuser\n\n";

#
# Open Connection
#
print "    Opening connection ...",scalar(localtime),"\n";
$dbh = DBI->connect("dbi:Oracle:$db","$user") ||
    die "Can't connect to Oracle database: $DBI::errstr\n";

#
# Open file
#
open(TA,">tables_all.html") || die "could not open tables.html: $! $?\n";

&PrintHeader;

#
# Get tables
#
$sh = $dbh->prepare("SELECT table_name FROM meme_tables order by table_name");
$sh->execute;

while (($table_name) = $sh->fetchrow_array) {
    print "    processing table $table_name\n";
    &Printtable($table_name);
}

&PrintFooter;

close(T);

print "------------------------------------------------------------\n";
print "Finished  ...", scalar localtime,"\n";
print "------------------------------------------------------------\n";

exit(0);



############## procedures #############

#
# We will print a HTML table for each table.
# row 1: table name
# row 2: fields
# row 3: involvement in source insertion
# row 4: involvement in editing
# row 5: involvement in production
#
sub Printtable {
    my($table_name) = @_;
    my($sh);
    my($type);
    my($type_dsc);

    $table_name = lc($table_name);

    $short_dsc = $descriptions{$table_name};
    $short_dsc =~ s/([^\.]*)\..*/$1\./;
    if ($mid_table_types{$table_name}) {
      $type = "$mid_table_types{$table_name}";
      $morm = "MID "; 
    }
    if ($mrd_table_types{$table_name}) {
      $type = "$mrd_table_types{$table_name}";
      $morm = "MRD "; 
    }
    @types = split /,/, $type;
    $type_dsc = "";
    foreach $t (@types) {
      $type_dsc .= "<p>$table_type_dsc{$t}</p>";
    }
    $type = qq{<a href="javascript:openDescription('$type','$type_dsc')"><tt>$morm$type</tt></a>} if $type;

    # write to tables_all.html
    print TA qq{
   <tr><td valign="top"><a href="tables/${table_name}.html"
       onMouseOver="window.status='Click here to see table $table_name'; return true;"
       onMouseOut="window.status=''; return true;"><tt>$table_name</tt></a></td>
       <td valigh="top">$type</td>
       <td valigh="top"><font size="-1">$short_dsc</font></td>
  </tr>
};

    # open this table
    open (T,">tables/${table_name}.html") || 
	die "Error, could not open tables/$table_name.html: $! $?\n";

    print T qq{
<html>
<head>
   <title>$meme Tables Documentation - $table_name</title>
    <script language="javascript">
	function openDescription (thing,dsc) {
	    var html = "<html><head><title>Description: "+thing;
	    html = html + "</title></head><body bgcolor=#ffffff>" + dsc + "<center><form><input type=button onClick='window.close(); return true' value='Close'></form></center></body></html>";
	    var win = window.open("","","scrollbars,width=500,height=250,resizable");
	    win.document.open();
	    win.document.write(html);
	    win.document.close();
	}; // end openDescription
    </script>
</head>

<body text="#000000" bgcolor="#FFFFFF" link="#3333FF" vlink="#999999" alink="#FF0000">

<center>

<h2>$meme Tables</h2></center>

<hr width="100%">

<!-- Nav bar -->
  <p><center>[ <a href="#overview">overview</a> | <a href="#details">details</a> | <a href="#references">references</a> ]</center></p>

<!-- Image -->
<p><center><tt><b>$table_name</b></tt><br>
   <img src="/images/table.gif" alt="Table Icon"></center></p>

<!-- Content section -->

<blockquote>

  <a name="overview"></a><h3>Overview/Objective</h3>
  This document contains a description of <tt>$table_name</tt> 
     and its various fields.  It also (may) include information
  about how this table is used in the various stages of MID processing.
<br>&nbsp;
<a name="details"></a><h3>Details</h3>

};

    if ($mid_table_types{$table_name}) {
      $type = "$mid_table_types{$table_name}";
      $morm = "MID "; 
    }
    if ($mrd_table_types{$table_name}) {
      $type = "$mrd_table_types{$table_name}";
      $morm = "MRD "; 
    }
    unless ($type) { $type = "unknown"; }
    @types = split /,/, $type;
    $type_dsc = "";
    foreach $t (@types) {
      $type_dsc .= "<p>$morm$table_type_dsc{$t}</p>";
    }

    print T qq{
    <p>
	
	<!-- $table_name -->

    <a name="$table_name"></a>
    <center>
      <table border="0" width="90%">
        <tr><td valign="top" width="20%"><b>Table name:</b></td><td valign="top" width="80%"><b><tt>$table_name</tt></b></td></tr>
        <tr><td valign="top"><b>Table Type:</b></td><td valign="top">
          <a href="javascript:openDescription('$type','$type_dsc')">$type</a></td></tr>
        <tr><td valign="top"><b>Description:</b></td><td valign="top">$descriptions{$table_name}</td></tr>
        <tr><td valign="top"><b>Fields:</b></td><td valign="top">
	    <table border="1" cellpadding="2" width="90%">
};

    #
    # Get columns.
    #
    $sh = $dbh->prepare(qq{
	SELECT column_name,data_type,data_length,data_precision,nullable
	FROM user_tab_columns where upper(table_name)=upper(?) 
        });
    $sh->execute($table_name);
    while (($column_name,$data_type,$data_length,$data_precision,$nullable)=
	   $sh->fetchrow_array) {

	$data_type = uc($data_type);
	$column_name = lc($column_name);
	if ($data_type eq "NUMBER") {
	    $data_type = "$data_type($data_precision)";
	} elsif ($data_type eq "VARCHAR2") {
	    $data_type = "$data_type($data_length)";
	}
	if ($nullable eq "N") {
	    $data_type = "<b>$data_type</b>";
	}

	# print column name, data type(size), description
	$comments = $tables_fields_to_comments{"$table_name-$column_name"};
	$comments = $fields_to_comments{$column_name} unless $comments;
	print T qq{
	<tr><td width="20%" valign="top"><tt>$column_name</tt></td>
	    <td width="20%" valign="top"><font size="-1">$data_type</font></td>
 	    <td width="60%" valign="top"><font size="-1">$comments&nbsp;</font></td>
	</tr>}
    }

    print T qq{
	    </table>
        </td></tr>
        <tr><td valign="top"><b>Indexes:</b></td><td valign="top">
};

    #
    # Get indexes
    #
    $sh = $dbh->prepare(qq{
	SELECT index_name, column_name FROM meme_ind_columns
	WHERE upper(table_name) = upper(?)
	ORDER BY index_name,column_position
    });
    $sh->execute($table_name);
    $found =0;
    %indexes = ();
    while (($index_name, $column_name) = $sh->fetchrow_array) {
	$found=1;
	$index_name = lc($index_name);
	$column_name= lc($column_name);
	$indexes{$index_name} .= ", $column_name";
    }
    unless ($found) {
	print T qq{
            <font size="-1"><tt>No indexes</tt></font>
	    }
    }

    foreach $key (sort keys %indexes) {
	$col_list = $indexes{$key};
	$col_list =~ s/^, //;
	print T qq{
	     <li><tt>$key ON $col_list</tt></li>
};
    };

    # print sections for roles.
    print T qq{
        </td></tr>
};

    if ($insertion_roles{$table_name}) {
	print T qq{
	<tr><td valign="top"><font size="-1"><b>Insertion&nbsp;Role:</b></font></td><td valign="top"><font size="-1">$insertion_roles{$table_name}&nbsp;</font></td></tr>
};
    };
    
    if ($editing_roles{$table_name}) {
	print T qq{
        <tr><td valign="top"><font size="-1"><b>Editing&nbsp;Role:</b></font></td><td valign="top"><font size="-1">$editing_roles{$table_name}&nbsp;</font></td></tr>
};
    };

    if ($production_roles{$table_name}) {
	print T qq{
	<tr><td valign="top"><font size="-1"><b>Production&nbsp;Role:</b></font></td><td valign="top"><font size="-1">$production_roles{$table_name}&nbsp;</font></td></tr>
};
    };

    print T qq{
      </table>
    </center>
    </p>
};

    ($d,$d,$d,$pday,$pmon,$year) = localtime;
    $pmon++;
    $year+=1900;
    $pday = "00$pday";
    $pday =~ /(..)$/;
    $day = $1;
    $pmon = "00$pmon";
    $pmon =~ /(..)$/;
    $mon = $1;
    $date = $mon."/".$day."/".$year;
    $comments_date = $year."/".$mon."/".$day;

    print T  qq{
<p>
<a name="references"></a><h3>References/Links</h3>
Use the following references for related information.
<ol>
  <li><a href="../tables_all.html" alt="All Tables Info">All $meme tables</a></li>
</ol>
</p>
</blockquote>

<p><center>[ <a href="#overview">overview</a> | <a href="#details">details</a> | <a href="#references">references</a> ]</center></p>

<hr WIDTH="100%">
<table BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
<tr NOSAVE>
<td ALIGN=LEFT VALIGN=TOP NOSAVE>
<address>
<a href="/MEME/">MEME Home</a></address>
</td>

<td ALIGN=RIGHT VALIGN=TOP NOSAVE>
<address>
<font size=-1>Contact: <a href="mailto:bcarlsen\@apelon.com">Brian A. Carlsen</a></font></address>

<address>
<font size=-1>Created: 7/27/2001</font></address>

<address>
<font size=-1>Last Updated: $date</font></address>

</td>
</tr>
</table>

</body>
<!-- These comments are used by the What\'s new Generator -->
<!-- Changed On: $comments_date -->
<!-- Changed by: Brian Carlsen -->
<!-- Change Note: MEME Schema documentation - $table_name  -->
<!-- Fresh for: 1 month -->
</html>
}



}

sub PrintHeader {

    print TA qq{
<html>
<head>
   <title>$meme Tables Documentation</title>
    <script language="javascript">
	function openDescription (thing,dsc) {
	    var html = "<html><head><title>Description: "+thing;
	    html = html + "</title></head><body bgcolor=#ffffff>" + dsc + "<center><form><input type=button onClick='window.close(); return true' value='Close'></form></center></body></html>";
	    var win = window.open("","","scrollbars,width=500,height=250,resizable");
	    win.document.open();
	    win.document.write(html);
	    win.document.close();
	}; // end openDescription
    </script>
</head>
<body text="#000000" bgcolor="#FFFFFF" link="#3333FF" vlink="#999999" alink="#FF0000">

<center>
<h2>$meme Tables</h2></center>

<!-- Nav bar -->
  <p><center>[ <a href="#overview">overview</a> | <a href="#details">details</a> | <a href="#references">references</a> ]</center></p>

<!-- Image -->
<p><center><img src="/images/table.gif" alt="Table Icon">
<img src="/images/table.gif" alt="Table Icon">
<img src="/images/table.gif" alt="Table Icon">
</center></p>

<!-- Content section -->

<blockquote>

  <a name="overview"></a><h3>Overview/Objective</h3>
  This document links to more detailed descriptions of the $meme tables.
<br>&nbsp;
<a name="details"></a><h3>Details</h3>
This document was machine generated from the list of tables found in 
the <tt>meme_tables</tt> table in the $db database.  The links below
contain specific details about the tables themselves, including: 
the field data types, whether they are null or not, the indexes present
on each table, and how the table is used during insertion, editing,
and production.
<br>&nbsp;

<a name="details"></a><h3>References/Links</h3>
<center><table width=90%>
   <tr><th valign="top">Table Name</th>
       <th valigh="top">Type</th>
       <th valigh="top">Description</th>
  </tr>
};

}

sub PrintFooter {
    
    ($d,$d,$d,$pday,$pmon,$year) = localtime;
    $pmon++;
    $year+=1900;
    $pday = "00$pday";
    $pday =~ /(..)$/;
    $day = $1;
    $pmon = "00$pmon";
    $pmon =~ /(..)$/;
    $mon = $1;
    $date = $mon."/".$day."/".$year;
    $comments_date = $year."/".$mon."/".$day;

    print TA  qq{
</table>
<p>
<hr WIDTH="100%">
<table BORDER=0 COLS=2 WIDTH="100%" NOSAVE >
<tr NOSAVE>
<td ALIGN=LEFT VALIGN=TOP NOSAVE>
<address>
<a href="/MEME/">MEME Home</a></address>
</td>

<td ALIGN=RIGHT VALIGN=TOP NOSAVE>
<address>
<font size=-1>Contact: <a href="mailto:bcarlsen\@apelon.com">Brian A. Carlsen</a></font></address>

<address>
<font size=-1>Created: 7/27/2001</font></address>

<address>
<font size=-1>Last Updated: $date</font></address>

</td>
</tr>
</table>

<!-- Nav bar -->
  <p><center>[ <a href="#overview">overview</a> | <a href="#details">details</a> | <a href="#references">references</a> ]</center></p>

</body>
<!-- These comments are used by the What\'s new Generator -->
<!-- Changed On: $comments_date -->
<!-- Changed by: Brian Carlsen -->
<!-- Change Note: Index for MEME Schema documentation -->
<!-- Fresh for: 1 month -->
</html>
}

}

