/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MEMEConnectionQueries
 *
 * 02/24/2009 BAC (1-GCLNT): Queries no longer read rank, source_rank or preferred_level fields.
 * 
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

/**
 * This interface contains queries used by {@link MEMEConnection}.
 *
 * @author MEME Group
 */
public interface MEMEConnectionQueries {

  //
  // Core data query constants
  //

  public final static String READ_CONCEPT =
      "SELECT concept_id, cui, version_id, status, dead, authority," +
      "  timestamp, insertion_date, preferred_atom_id, released," +
      "  tobereleased, last_molecule_id, last_atomic_action_id, rank," +
      "  editing_authority, editing_timestamp, approval_molecule_id " +
      "FROM concept_status " +
      "WHERE concept_id = ?" +
      "  AND dead = 'N'";

  public final static String READ_DEAD_CONCEPT =
      "SELECT concept_id, cui, version_id, status, dead, authority," +
      "  timestamp, insertion_date, preferred_atom_id, released," +
      "  tobereleased, last_molecule_id, last_atomic_action_id, rank," +
      "  editing_authority, editing_timestamp, approval_molecule_id " +
      "FROM dead_concept_status " +
      "WHERE concept_id = ?";

  public final static String READ_ATOMS =
      "SELECT atom_id, version_id, source, termgroup, tty, termgroup_rank," +
      "  code, sui, lui, generated_status, last_release_cui, dead, status," +
      "  authority, timestamp, insertion_date, concept_id, released," +
      "  tobereleased, last_molecule_id, last_atomic_action_id, sort_key," +
      "  rank, last_release_rank, suppressible, last_assigned_cui, isui, " +
      "  NVL(language, 'ENG') AS language, source_cui, source_dui, source_aui, aui " +
      "FROM classes " +
      "WHERE concept_id = ?" +
      "  AND dead = 'N'";

  public final static String READ_ATOM_WITH_NAME_COLUMNS =
      "SELECT a.atom_id, a.version_id, a.source, a.termgroup, a.tty," +
      "  a.termgroup_rank, a.code, a.sui, a.lui, a.generated_status," +
      "  a.last_release_cui, a.dead, a.status, a.authority, a.timestamp," +
      "  a.insertion_date, a.concept_id, a.released, a.tobereleased," +
      "  a.last_molecule_id, a.last_atomic_action_id, a.sort_key, a.rank," +
      "  a.last_release_rank, a.suppressible, a.last_assigned_cui," +
      "  a.isui, b.string, b.norm_string, NVL(a.language, 'ENG') AS language, " +
      "  a.source_cui, a.source_dui, a.source_aui, a.aui " +
      "FROM ";

  public final static String READ_ATOM_WITH_NAME_CONDITIONS =
      " a, string_ui b " +
      "WHERE a.atom_id = ?" +
      "  AND b.sui = a.sui";

  public final static String READ_ATOM_COLUMNS =
      "SELECT a.atom_id, a.version_id, a.source, a.termgroup, a.tty," +
      "  a.termgroup_rank, a.code, a.sui, a.lui, a.generated_status," +
      "  a.last_release_cui, a.dead, a.status, a.authority, a.timestamp," +
      "  a.insertion_date, a.concept_id, a.released, a.tobereleased," +
      "  a.last_molecule_id, a.last_atomic_action_id, a.sort_key, a.rank," +
      "  a.last_release_rank, a.suppressible, a.last_assigned_cui," +
      "  a.isui, NVL(a.language, 'ENG') AS language, a.source_cui," +
      "  a.source_dui, a.source_aui, a.aui " +
      "FROM ";

  public final static String READ_ATOM_CONDITIONS =
      " a " +
      "WHERE a.atom_id = ?" +
      "  AND a.dead = 'N'";

  public final static String READ_ATOM_WITH_NAME_BY_CODE_TERMGROUP =
      "SELECT a.atom_id, a.version_id, a.source, a.termgroup, a.tty," +
      "  a.termgroup_rank, a.code, a.sui, a.lui, a.generated_status," +
      "  a.last_release_cui, a.dead, a.status, a.authority, a.timestamp," +
      "  a.insertion_date, a.concept_id, a.released, a.tobereleased," +
      "  a.last_molecule_id, a.last_atomic_action_id, a.sort_key, a.rank," +
      "  a.last_release_rank, a.suppressible, a.last_assigned_cui," +
      "  a.isui, b.string, b.norm_string, NVL(a.language, 'ENG') as language, " +
      "  a.source_cui, a.source_dui, a.source_aui, a.aui " +
      "FROM classes a, string_ui b " +
      "WHERE a.code = ?" +
      "  AND a.termgroup = ?" +
      "  AND a.dead = 'N'" +
      "  AND b.sui = a.sui";

  public final static String READ_ATOMS_WITH_NAME =
      "SELECT a.atom_id, a.version_id, a.source, a.termgroup, a.tty," +
      "  a.termgroup_rank, a.code, a.sui, a.lui, a.generated_status," +
      "  a.last_release_cui, a.dead, a.status, a.authority, a.timestamp," +
      "  a.insertion_date, a.concept_id, a.released, a.tobereleased," +
      "  a.last_molecule_id, a.last_atomic_action_id, a.sort_key, a.rank," +
      "  a.last_release_rank, a.suppressible, a.last_assigned_cui," +
      "  a.isui, b.string, b.norm_string, " +
      "  NVL(a.language, 'ENG') AS language, a.source_cui, a.source_dui," +
      "  a.source_aui, a.aui " +
      "FROM classes a, string_ui b " +
      "WHERE a.concept_id = ?" +
      "  AND a.dead = 'N'" +
      "  AND b.sui = a.sui";

  public final static String READ_ATTRIBUTE_COLUMNS =
      "SELECT atom_id, version_id, attribute_id, attribute_level," +
      "  attribute_name, attribute_value, generated_status, source, dead," +
      "  status, authority, timestamp, insertion_date, concept_id," +
      "  released, tobereleased, " +
      "  last_molecule_id, last_atomic_action_id, suppressible, " +
      "  sg_id, sg_type, sg_qualifier, sg_meme_id, sg_meme_data_type, " +
      "  source_atui, atui " +
      "FROM ";

  public final static String READ_ATTRIBUTE_CONDITIONS =
      " attributes " +
      "WHERE attribute_id = ?";

  public final static String READ_ATTRIBUTES =
      "SELECT atom_id, version_id, attribute_id, attribute_level," +
      "  attribute_name, attribute_value, generated_status, source, dead," +
      "  status, authority, timestamp, insertion_date, concept_id," +
      "  released, tobereleased," +
      "  last_molecule_id, last_atomic_action_id, suppressible, " +
      "  sg_id, sg_type, sg_qualifier, sg_meme_id, sg_meme_data_type, " +
      "  source_atui, atui " +
      "FROM attributes " +
      "WHERE concept_id = ?" +
      "  AND dead = 'N'";

  public final static String READ_ATOM_ATTRIBUTES =
      "SELECT atom_id, version_id, attribute_id, attribute_level," +
      "  attribute_name, attribute_value, generated_status, source, dead," +
      "  status, authority, timestamp, insertion_date, concept_id," +
      "  released, tobereleased, " +
      "  last_molecule_id, last_atomic_action_id, suppressible, " +
      "  sg_id, sg_type, sg_qualifier, sg_meme_id, sg_meme_data_type, " +
      "  source_atui, atui " +
      "FROM attributes " +
      "WHERE atom_id = ?" +
      "  AND attribute_level = 'S'" +
      "  AND dead = 'N'";

  public final static String READ_LONG_ATTRIBUTE =
      "SELECT string_id, row_sequence, text_total, text_value " +
      "FROM stringtab WHERE string_id = ? ORDER BY row_sequence";

  public final static String READ_RELATIONSHIP_ATTRIBUTES =
      "SELECT atom_id, version_id, attribute_id, attribute_level," +
      "  attribute_name, attribute_value, generated_status, source, dead," +
      "  status, authority, timestamp, insertion_date, concept_id," +
      "  released, tobereleased, " +
      "  last_molecule_id, last_atomic_action_id, suppressible, " +
      "  sg_id, sg_type, sg_qualifier, sg_meme_id, sg_meme_data_type, " +
      "  source_atui, atui " +
      "FROM attributes " +
      "WHERE sg_meme_id = ?" +
      "  AND sg_meme_data_type = 'R'" +
      "  AND concept_id = ?" +
      "  AND attribute_level = 'S'" +
      "  AND dead = 'N'";

  public final static String READ_RELATIONSHIP_COLUMNS =
      "SELECT relationship_id, version_id, relationship_level, atom_id_1," +
      "  relationship_name, relationship_attribute, atom_id_2, source," +
      "  generated_status, dead, status, authority, timestamp," +
      "  insertion_date, concept_id_1, concept_id_2, released," +
      "  tobereleased, last_molecule_id," +
      "  last_atomic_action_id, source_of_label, suppressible, " +
      "  sg_id_1, sg_id_2, sg_type_1, sg_type_2, sg_qualifier_1, sg_qualifier_2, " +
      "  sg_meme_id_1, sg_meme_id_2, sg_meme_data_type_1, sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group " +
      "FROM ";

  public final static String READ_RELATIONSHIP_CONDITIONS =
      " relationships " +
      "WHERE relationship_id = ?";

  // It is important to have two queries here
  // because the relationship_name and relationship_attribute
  // will be reversed for the second query
  public final static String READ_RELATIONSHIPS_1 =
      "SELECT relationship_id, version_id, relationship_level, atom_id_1," +
      "  relationship_name, relationship_attribute, atom_id_2, source," +
      "  generated_status, dead, status, authority, timestamp," +
      "  insertion_date, concept_id_1, concept_id_2, released," +
      "  tobereleased, last_molecule_id," +
      "  last_atomic_action_id, source_of_label, suppressible, " +
      "  sg_id_1, sg_id_2, sg_type_1, sg_type_2, sg_qualifier_1, sg_qualifier_2, " +
      "  sg_meme_id_1, sg_meme_id_2, sg_meme_data_type_1, sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group " +
      "FROM relationships " +
      "WHERE concept_id_1 = ?" +
      "  AND dead = 'N'";

  public final static String READ_RELATIONSHIPS_2 =
      "SELECT relationship_id, version_id, relationship_level," +
      "  atom_id_2 as atom_id_1, relationship_name, relationship_attribute," +
      "  atom_id_1 as atom_id_2, source, generated_status, dead, status," +
      "  authority, timestamp, insertion_date, concept_id_2 as concept_id_1," +
      "  concept_id_1 as concept_id_2, released, tobereleased, " +
      "  last_molecule_id, last_atomic_action_id," +
      "  source_of_label, suppressible, " +
      "  sg_id_2 as sg_id_1, sg_id_1 as sg_id_2, " +
      "  sg_type_2 as sg_type_1, sg_type_1 as sg_type_2, " +
      "  sg_qualifier_2 as sg_qualifier_1, sg_qualifier_1 as sg_qualifier_2, " +
      "  sg_meme_id_2 as sg_meme_id_1, sg_meme_id_1 as sg_meme_id_2, " +
      "  sg_meme_data_type_2 as sg_meme_data_type_1, " +
      "  sg_meme_data_type_1 as sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group " +
      "FROM relationships " +
      "WHERE concept_id_2 = ?" +
      //    "  AND concept_id_2 != concept_id_1" +
      "  AND dead = 'N'";

  // It is important to have two queries here
  // because the relationship_name and relationship_attribute
  // will be reversed for the second query
  public final static String READ_RELATIONSHIPS_WITH_NAMES_1 =
      "SELECT a.relationship_id, a.version_id, a.relationship_level," +
      "  a.atom_id_1, a.relationship_name, a.relationship_attribute," +
      "  a.atom_id_2, a.source, a.generated_status, a.dead, a.status," +
      "  a.authority, a.timestamp, a.insertion_date, a.concept_id_1," +
      "  a.concept_id_2, a.released, a.tobereleased, " +
      "  a.last_molecule_id, a.last_atomic_action_id," +
      "  a.source_of_label, a.suppressible, sg_id_1, sg_id_2, " +
      "  sg_type_1, sg_type_2, sg_qualifier_1, sg_qualifier_2, " +
      "  sg_meme_id_1, sg_meme_id_2, sg_meme_data_type_1, sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group, " +
      "  d.atom_id, string, d.code," +
      "  d.sui, d.isui, d.lui, d.source as atom_source, d.termgroup, " +
      "  d.tobereleased as atom_tobereleased " +
      "FROM relationships a, concept_status b, string_ui c, classes d " +
      "WHERE concept_id_1 = ?" +
      "  AND concept_id_2 = b.concept_id (+) " +
      "  AND preferred_atom_id = d.atom_id (+) " +
      "  AND d.sui = c.sui (+) " +
      "  AND a.dead = 'N'";

  public final static String READ_RELATIONSHIPS_WITH_NAMES_2 =
      "SELECT relationship_id, a.version_id, relationship_level," +
      "  atom_id_2 as atom_id_1, relationship_name, relationship_attribute," +
      "  atom_id_1 as atom_id_2, a.source, a.generated_status, a.dead," +
      "  a.status, a.authority, a.timestamp, a.insertion_date," +
      "  concept_id_2 as concept_id_1, concept_id_1 as concept_id_2," +
      "  a.released, a.tobereleased," +
      "  a.last_molecule_id, a.last_atomic_action_id, source_of_label," +
      "  a.suppressible, sg_id_2 as sg_id_1, sg_id_1 as sg_id_2, " +
      "  sg_type_2 as sg_type_1, sg_type_1 as sg_type_2," +
      "  sg_qualifier_2 as sg_qualifier_1, sg_qualifier_1 as sg_qualifier_2, " +
      "  sg_meme_id_2 as sg_meme_id_1, sg_meme_id_1 as sg_meme_id_2, " +
      "  sg_meme_data_type_2 as sg_meme_data_type_1, " +
      "  sg_meme_data_type_1 as sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group, " +
      "  d.atom_id, string, d.code, d.sui, d.isui, d.lui," +
      "  d.source as atom_source, d.termgroup, " +
      "  d.tobereleased as atom_tobereleased " +
      "FROM relationships a, concept_status b, string_ui c, classes d " +
      "WHERE concept_id_2 = ?" +
      "  AND concept_id_1 = b.concept_id (+) " +
      "  AND preferred_atom_id = d.atom_id (+) " +
      "  AND d.sui = c.sui (+) " +
      "  AND a.dead = 'N'";

  public final static String READ_RELATIONSHIP_COUNT =
      "SELECT SUM(ct) AS COUNT FROM" +
      " (SELECT COUNT(*) ct FROM relationships WHERE concept_id_1 = ? AND dead='N' " +
      "  UNION ALL SELECT count(*) FROM relationships WHERE concept_id_2 = ? AND dead='N')";

  // It is important to have two queries here
  // because the relationship_name and relationship_attribute
  // will be reversed for the second query
  public final static String READ_ATOM_RELATIONSHIPS_1 =
      "SELECT relationship_id, version_id, relationship_level, atom_id_1," +
      "  relationship_name, relationship_attribute, atom_id_2, source," +
      "  generated_status, dead, status, authority, timestamp," +
      "  insertion_date, concept_id_1, concept_id_2, released," +
      "  tobereleased, last_molecule_id," +
      "  last_atomic_action_id, source_of_label, suppressible, " +
      "  sg_id_1, sg_id_2, sg_type_1, sg_type_2, sg_qualifier_1, sg_qualifier_2, " +
      "  sg_meme_id_1, sg_meme_id_2, sg_meme_data_type_1, sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group " +
      "FROM relationships " +
      "WHERE atom_id_1 = ?" +
      "  AND relationship_level != 'C'" +
      "  AND dead = 'N'";

  public final static String READ_ATOM_RELATIONSHIPS_2 =
      "SELECT relationship_id, version_id, relationship_level," +
      "  atom_id_2 as atom_id_1, relationship_name, relationship_attribute," +
      "  atom_id_1 as atom_id_2, source, generated_status, dead, status," +
      "  authority, timestamp, insertion_date, concept_id_2 as concept_id_1," +
      "  concept_id_1 as concept_id_2, released, tobereleased, " +
      "  last_molecule_id, last_atomic_action_id, " +
      "  source_of_label, suppressible, " +
      "  sg_id_2 as sg_id_1, sg_id_1 as sg_id_2, " +
      "  sg_type_2 as sg_type_1, sg_type_1 as sg_type_2, " +
      "  sg_qualifier_1 as sg_qualifier_2, sg_qualifier_2 as sg_qualifier_1, " +
      "  sg_meme_id_2 as sg_meme_id_1, sg_meme_id_1 as sg_meme_id_2, " +
      "  sg_meme_data_type_2 as sg_meme_data_type_1, " +
      "  sg_meme_data_type_1 as sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group " +
      "FROM relationships " +
      "WHERE atom_id_2 = ?" +
      "  AND atom_id_2 != atom_id_1" +
      "  AND relationship_level != 'C'" +
      "  AND dead = 'N'";

  // It is important to have two queries here
  // because the relationship_name and relationship_attribute
  // will be reversed for the second query
  public final static String READ_ATOM_RELATIONSHIPS_WITH_NAMES_1 =
      "SELECT a.relationship_id, a.version_id, a.relationship_level," +
      "  a.atom_id_1, a.relationship_name, a.relationship_attribute," +
      "  a.atom_id_2, a.source, a.generated_status, a.dead, a.status," +
      "  a.authority, a.timestamp, a.insertion_date, a.concept_id_1," +
      "  a.concept_id_2, a.released, a.tobereleased, " +
      "  a.last_molecule_id, a.last_atomic_action_id," +
      "  a.source_of_label, a.suppressible, " +
      "  a.sg_id_1, a.sg_id_2, a.sg_type_1, a.sg_type_2, " +
      "  a.sg_meme_id_1, a.sg_meme_id_2, " +
      "  a.sg_meme_data_type_1, a.sg_meme_data_type_2, " +
      "  a.sg_qualifier_1, a.sg_qualifier_2, a.source_rui, " +
      "  a.rui, a.relationship_group, d.atom_id, string, d.code," +
      "  d.sui, d.isui, d.lui, d.source as atom_source, d.termgroup, " +
      "  d.tobereleased as atom_tobereleased " +
      "FROM relationships a, concept_status b, string_ui c, classes d " +
      "WHERE atom_id_1 = ?" +
      "  AND concept_id_2 = b.concept_id (+) " +
      "  AND preferred_atom_id = d.atom_id (+) " +
      "  AND d.sui = c.sui (+) " +
      "  AND relationship_level != 'C'" +
      "  AND a.dead = 'N'";

  public final static String READ_ATOM_RELATIONSHIPS_WITH_NAMES_2 =
      "SELECT relationship_id, a.version_id, relationship_level," +
      "  atom_id_2 as atom_id_1, relationship_name, relationship_attribute," +
      "  atom_id_1 as atom_id_2, a.source, a.generated_status, a.dead," +
      "  a.status, a.authority, a.timestamp, a.insertion_date," +
      "  concept_id_2 as concept_id_1, concept_id_1 as concept_id_2," +
      "  a.released, a.tobereleased, " +
      "  a.last_molecule_id, a.last_atomic_action_id, source_of_label," +
      "  a.suppressible, sg_id_2 as sg_id_1, sg_id_1 as sg_id_2, " +
      "  sg_type_2 as sg_type_1, sg_type_1 as sg_type_2, " +
      "  sg_meme_id_2 as sg_meme_id_1, sg_meme_id_1 as sg_meme_id_2, " +
      "  sg_meme_data_type_2 as sg_meme_data_type_1, " +
      "  sg_meme_data_type_1 as sg_meme_data_type_2, " +
      "  sg_qualifier_2 as sg_qualifier_1, sg_qualifier_1 as sg_qualifier_2, " +
      "  source_rui, rui, relationship_group, " +
      "  d.atom_id, string, d.code, d.sui, d.isui, d.lui," +
      "  d.source as atom_source, d.termgroup, " +
      "  d.tobereleased as atom_tobereleased " +
      "FROM relationships a, concept_status b, string_ui c, classes d " +
      "WHERE atom_id_2 = ?" +
      "  AND atom_id_2 != atom_id_1" +
      "  AND concept_id_1 = b.concept_id (+) " +
      "  AND preferred_atom_id = d.atom_id (+) " +
      "  AND d.sui = c.sui (+) " +
      "  AND relationship_level != 'C'" +
      "  AND a.dead = 'N'";

  public final static String READ_CONTEXT_RELATIONSHIP_COLUMNS =
      "SELECT relationship_id, version_id, relationship_level," +
      "  atom_id_1, relationship_name, relationship_attribute," +
      "  atom_id_2, source, generated_status, dead, status," +
      "  authority, timestamp, insertion_date, concept_id_1, concept_id_2," +
      "  released, tobereleased, " +
      "  last_molecule_id, last_atomic_action_id," +
      "  source_of_label, suppressible, hierarchical_code, " +
      "  parent_treenum, release_mode, sg_id_1, sg_type_1, " +
      "  sg_qualifier_1, sg_id_2, sg_type_2, sg_qualifier_2, " +
      "  sg_meme_id_1, sg_meme_id_2, sg_meme_data_type_1, sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group FROM ";

  public final static String READ_CONTEXT_RELATIONSHIP_CONDITIONS =
      " WHERE relationship_id = ?";

  // It is important to have two queries here
  // because the relationship_name and relationship_attribute
  // will be reversed for the second query
  public final static String READ_CONTEXT_RELATIONSHIPS_1 =
      "SELECT distinct 1 as relationship_id, a.version_id, relationship_level," +
      "  a.atom_id_1, a.relationship_name, a.relationship_attribute," +
      "  a.atom_id_2, a.source, a.generated_status, a.dead, a.status," +
      "  a.authority, a.timestamp, a.insertion_date," +
      "  b.concept_id as concept_id_1, c.concept_id as concept_id_2," +
      "  a.released, a.tobereleased, " +
      "  a.last_molecule_id, a.last_atomic_action_id," +
      "  a.source_of_label, a.suppressible, d.atom_name as string, c.atom_id," +
      "  c.source as atom_source, c.termgroup, c.tobereleased as atom_tobereleased, " +
      "  c.code, c.sui, c.isui, c.lui, " +
      "  a.sg_id_1, a.sg_type_1, a.sg_qualifier_1, " +
      "  a.sg_id_2, a.sg_type_2, a.sg_qualifier_2, " +
      "  a.sg_meme_id_1, a.sg_meme_id_2, " +
      "  a.sg_meme_data_type_1, a.sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group " +
      "FROM context_relationships a, classes b, classes c, atoms d " +
      "WHERE b.concept_id = ?" +
      "  AND a.dead = 'N'" +
      "  AND b.atom_id = a.atom_id_1" +
      "  AND c.atom_id = a.atom_id_2" +
      "  AND c.atom_id = d.atom_id";

  public final static String READ_CONTEXT_RELATIONSHIPS_2 =
      "SELECT distinct 1 as relationship_id, a.version_id, a.relationship_level," +
      "  a.atom_id_1 as atom_id_2, a.relationship_name," +
      "  a.relationship_attribute, a.atom_id_2 as atom_id_1, a.source," +
      "  a.generated_status, a.dead, a.status, a.authority, a.timestamp," +
      "  a.insertion_date, b.concept_id as concept_id_1," +
      "  c.concept_id as concept_id_2, a.released, a.tobereleased," +
      "  a.last_molecule_id," +
      "  a.last_atomic_action_id, a.source_of_label," +
      "  a.suppressible, d.atom_name as string, c.atom_id," +
      "  c.source as atom_source, c.termgroup, c.tobereleased as atom_tobereleased, " +
      "  c.code, c.sui, c.isui, c.lui, " +
      "  a.sg_id_1, a.sg_type_1, a.sg_qualifier_1, " +
      "  a.sg_id_2, a.sg_type_2, a.sg_qualifier_2, " +
      "  a.sg_meme_id_1, a.sg_meme_id_2, " +
      "  a.sg_meme_data_type_1, a.sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group " +
      "FROM context_relationships a, classes b, classes c, atoms d " +
      "WHERE b.concept_id = ?" +
      "  AND b.atom_id = a.atom_id_2" +
      //    "  AND b.concept_id != c.concept_id" +
      "  AND c.atom_id = a.atom_id_1" +
      "  AND c.atom_id = d.atom_id" +
      "  AND a.dead = 'N'";

  public final static String READ_CONTEXT_RELATIONSHIP_COUNT =
      "SELECT COUNT(*) AS COUNT FROM" +
      " (SELECT relationship_id FROM context_relationships a, classes b" +
      "  WHERE concept_id = ? AND atom_id_1 = atom_id " +
      "  UNION " +
      "  SELECT relationship_id FROM context_relationships a, classes b" +
      "  WHERE concept_id = ? AND atom_id_2 = atom_id)";

  // It is important to have two queries here
  // because the relationship_name and relationship_attribute
  // will be reversed for the second query
  public final static String READ_ATOM_CONTEXT_RELATIONSHIPS_1 =
      "SELECT distinct 1 as relationship_id, a.version_id, relationship_level," +
      "  a.atom_id_1, a.relationship_name, a.relationship_attribute," +
      "  a.atom_id_2, a.source, a.generated_status, a.dead, a.status," +
      "  a.authority, a.timestamp, a.insertion_date," +
      "  b.concept_id as concept_id_1, c.concept_id as concept_id_2," +
      "  a.released, a.tobereleased, " +
      "  a.last_molecule_id, a.last_atomic_action_id, " +
      "  a.source_of_label, a.suppressible, d.atom_name as string," +
      "  c.source as atom_source, c.tobereleased as atom_tobereleased, " +
      "  c.termgroup, c.code, c.sui, c.isui, c.lui, " +
      "  a.sg_id_1, a.sg_type_1, a.sg_qualifier_1, " +
      "  a.sg_id_2, a.sg_type_2, a.sg_qualifier_2, " +
      "  a.sg_meme_id_1, a.sg_meme_id_2, " +
      "  a.sg_meme_data_type_1, a.sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group " +
      "FROM context_relationships a, classes b, classes c, atoms d " +
      "WHERE b.atom_id = ?" +
      "  AND a.dead = 'N'" +
      "  AND b.atom_id = a.atom_id_1" +
      "  AND c.atom_id = a.atom_id_2" +
      "  AND c.atom_id = d.atom_id";

  public final static String READ_ATOM_CONTEXT_RELATIONSHIPS_2 =
      "SELECT distinct 1 as relationship_id, a.version_id," +
      "  a.relationship_level, a.atom_id_1 as atom_id_2, a.relationship_name," +
      "  a.relationship_attribute, a.atom_id_2 as atom_id_1, a.source," +
      "  a.generated_status, a.dead, a.status, a.authority, a.timestamp," +
      "  a.insertion_date, b.concept_id as concept_id_1," +
      "  c.concept_id as concept_id_2, a.released, a.tobereleased," +
      "  a.last_molecule_id," +
      "  a.last_atomic_action_id, a.source_of_label, a.suppressible," +
      "  d.atom_name as string, c.source as atom_source, " +
      "  c.tobereleased as atom_tobereleased, c.termgroup, c.code," +
      "  c.sui, c.isui, c.lui, " +
      "  a.sg_id_1, a.sg_type_1, a.sg_qualifier_1, " +
      "  a.sg_id_2, a.sg_type_2, a.sg_qualifier_2, " +
      "  a.sg_meme_id_1, a.sg_meme_id_2, " +
      "  a.sg_meme_data_type_1, a.sg_meme_data_type_2, " +
      "  source_rui, rui, relationship_group " +
      "FROM context_relationships a, classes b, classes c, atoms d " +
      "WHERE b.atom_id = ?" +
      "  AND b.atom_id = a.atom_id_2" +
      "  AND b.atom_id != c.atom_id" +
      "  AND c.atom_id = a.atom_id_1" +
      "  AND c.atom_id = d.atom_id" +
      "  AND a.dead = 'N'";

  public final static String READ_INVERSE_REL_COLUMNS =
      "SELECT relationship_id, version_id, relationship_level, " +
      "  atom_id_1 as atom_id_2," +
      "  inverse_name as relationship_name, inverse_rel_attribute as relationship_attribute, " +
      "  atom_id_2 as atom_id_1, source," +
      "  generated_status, dead, status, authority, timestamp," +
      "  insertion_date, concept_id_1 as concept_id_2, " +
      "  concept_id_2 as concept_id_1, released," +
      "  tobereleased, last_molecule_id," +
      "  last_atomic_action_id, r.source_of_label, r.suppressible, " +
      "  r.sg_id_1 as sg_id_2, r.sg_id_2 as sg_id_1, sg_type_1 as sg_type_2, " +
      "  sg_type_2 as sg_type_1, sg_qualifier_1 as sg_qualifier_2, " +
      "  sg_qualifier_2 as sg_qualifier_1, " +
      "  sg_meme_id_1 as sg_meme_id_2, sg_meme_id_2 as sg_meme_id_1, " +
      "  sg_meme_data_type_1 as sg_meme_data_type_2, sg_meme_data_type_2 as sg_meme_data_type_1, " +
      "  source_rui, inverse_rui as rui, relationship_group " +
      "FROM ";

  public final static String READ_INVERSE_REL_CONDITIONS =
      " r, inverse_relationships ir, " +
      " inverse_rel_attributes ira, " +
      " inverse_relationships_ui iru " +
      " WHERE relationship_id = ? " +
      " AND r.relationship_name = ir.relationship_name " +
      " AND NVL(r.rui, 'null') = iru.rui (+)" +
      " AND NVL(r.relationship_attribute, 'null') = NVL(ira.relationship_attribute, 'null')";

  public final static String READ_PREFERRED_ATOM =
      "SELECT b.atom_id, NVL(b.source,'MTH') as atom_source, " +
      " NVL(b.termgroup,'MTH/PT') as termgroup, " +
      "  NVL(b.tobereleased,'Y') as atom_tobereleased, " +
      "  b.code, b.sui, b.lui, b.isui, " +
      "  NVL(c.string,'Missing preferred name') as string " +
      "FROM classes b, string_ui c, concept_status d " +
      "WHERE preferred_atom_id = b.atom_id (+) " +
      "  AND b.sui=c.sui (+) AND d.concept_id = ?";

  public final static String READ_MOLECULAR_ACTION =
      "SELECT transaction_id, molecule_id, authority," +
      "  timestamp, molecular_action, source_id, target_id," +
      "  undone, undone_by, undone_when, status, elapsed_time," +
      "  work_id " +
      "FROM molecular_actions WHERE molecule_id = ?";

  public final static String READ_ATOMIC_ACTION =
      "SELECT molecule_id, atomic_action_id, action, table_name," +
      "  row_id, old_value, new_value, authority, timestamp," +
      "  status, action_field " +
      "FROM atomic_actions WHERE atomic_action_id = ?";

  public final static String READ_ATOMIC_ACTIONS =
      "SELECT molecule_id, atomic_action_id, action, table_name," +
      "  row_id, old_value, new_value, authority, timestamp," +
      "  status, action_field " +
      "FROM atomic_actions WHERE molecule_id = ? ORDER BY atomic_action_id";

  public final static String READ_INSERT_DATA =
      "SELECT document " +
      "FROM action_log WHERE mid_event_id = ? " +
      "AND rowid = ?";

  public final static String READ_LAST_ACTION =
      "SELECT NVL(max(molecule_id),0) AS molecule_id FROM" +
      "  (SELECT molecule_id" +
      "    FROM molecular_actions" +
      "    WHERE source_id = ?" +
      "    AND undone='N' " +
      "  UNION " +
      "  SELECT molecule_id" +
      "    FROM molecular_actions" +
      "    WHERE target_id = ?" +
      "    AND undone='N')";

  public final static String READ_IC_APPLICATIONS =
      "SELECT application, integrity_vector " +
      "FROM ic_applications WHERE application = ?";

  public final static String READ_IC_OVERRIDE =
      "SELECT ic_level, override_vector " +
      "FROM ic_override WHERE ic_level = ?";

  public final static String READ_CONTEXT_PATH =
      "SELECT b.atom_id, c.string, b.code, c.lui, c.sui," +
      "       c.isui, b.source as atom_source," +
      "       b.tobereleased as atom_tobereleased, " +
      "       b.termgroup, b.concept_id, b.aui " +
      "FROM " +
      "(SELECT atom_id_2, parent_treenum " +
      " FROM context_relationships " +
      " START WITH relationship_id = ? " +
      " CONNECT BY atom_id_1 = PRIOR atom_id_2 " +
      "     AND parent_treenum || '.' || atom_id_1 = PRIOR parent_treenum) a, " +
      " classes b, string_ui c " +
      "WHERE atom_id = atom_id_2 " +
      "AND b.sui = c.sui " +
      "ORDER BY length(parent_treenum) DESC";
}
