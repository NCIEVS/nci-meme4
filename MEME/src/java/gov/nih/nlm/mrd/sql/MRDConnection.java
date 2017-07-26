/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.sql
 * Object:  MRDConnection
 *
 * 07/01/2008 TTN (1-HYNKQ): change cacheSRCQAReason to use super.getSources
 * 10/30/2007 TTN (1-FJB2L): change cacheSRCQAReason query to use "NOT IN" in subquery 
 * 10/18/2007 TTN (1-FJB2L): restrict qa_adjustment query to current editing adjustments 
 * 10/09/2007 TTN (1-FGOCT): add two new sections to retrieve the NEW and OLD src qa reasons
 * 06/18/2007 TTN (1-EITKP): Missing the last "|" while reconstrucitng value from qa_adjustment for SRCQAReasons in MRDConnection
 * 06/15/2007 TTN (1-EI329): use FieldedStringTokenizer.split
 *****************************************************************************/

package gov.nih.nlm.mrd.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.action.MolecularAction;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.MRDAtom;
import gov.nih.nlm.meme.common.MRDAttribute;
import gov.nih.nlm.meme.common.MRDConcept;
import gov.nih.nlm.meme.common.MRDContextRelationship;
import gov.nih.nlm.meme.common.MRDRelationship;
import gov.nih.nlm.meme.common.Rank;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.common.Termgroup;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.server.ServerConstants;
import gov.nih.nlm.meme.server.ServerToolkit;
import gov.nih.nlm.meme.sql.ActionEngine;
import gov.nih.nlm.meme.sql.MEMEConnection;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.ResultSetIterator;
import gov.nih.nlm.meme.sql.ResultSetMapper;
import gov.nih.nlm.meme.sql.Ticket;
import gov.nih.nlm.mrd.common.QAComparison;
import gov.nih.nlm.mrd.common.QAComparisonReason;
import gov.nih.nlm.mrd.common.QAReason;
import gov.nih.nlm.mrd.common.QAResult;
import gov.nih.nlm.mrd.common.QAResultReason;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.common.SourceType;
import gov.nih.nlm.mrd.server.MRDProcessHandler;
import gov.nih.nlm.mrd.server.RegisteredHandler;
import gov.nih.nlm.mrd.server.ReleaseHandler;
import gov.nih.nlm.util.ColumnStatistics;
import gov.nih.nlm.util.FieldedStringTokenizer;
import gov.nih.nlm.util.FileStatistics;
import gov.nih.nlm.util.MultiMap;
import gov.nih.nlm.util.OrderedHashMap;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a connection to an MRD.
 *
 * @author  MEME Group
 */
public class MRDConnection extends MEMEConnection implements MRDDataSource {


    final String meme_home = ServerToolkit.getProperty(
            ServerConstants.
            MEME_HOME);
	
	//
  // Cache of release history info
  //
	
  private Map release_history_cache = null;

  public final static String READ_CUI_FOR_CONCEPT_ID =
	  " SELECT CUI FROM MRD_CONCEPTS WHERE concept_id = ?";
  
  public final static String READ_CUI_FOR_AUI =
	  " SELECT CUI FROM MRD_CLASSES WHERE AUI = ?";
  
  public final static String READ_MRD_CUI =
      "SELECT concept_id, cui, version_id, status, dead, authority," +
      "  timestamp, insertion_date, preferred_atom_id, released," +
      "  tobereleased, last_molecule_id, last_atomic_action_id, rank," +
      "  editing_authority, editing_timestamp, approval_molecule_id " +
      "FROM concept_status " +
      "WHERE cui = ?" +
      "  AND dead = 'N'";
  
  public final static String READ_AUIS_WITH_NAME_FOR_CODE = 
	  "SELECT a.atom_id, a.version_id, a.source,c.root_source, a.termgroup, c.tty," +
      "  a.termgroup_rank, c.code, c.sui, c.lui, a.generated_status," +
      "  a.last_release_cui, a.dead, a.status, a.authority, a.timestamp," +
      "  a.insertion_date, a.concept_id, a.released, a.tobereleased," +
      "  a.last_molecule_id, a.last_atomic_action_id, a.rank," +
      "  a.last_release_rank, a.suppressible, a.last_assigned_cui," +
      "  c.isui, b.string, b.norm_string, " +
      "  NVL(c.language, 'ENG') AS language, c.source_cui, c.source_dui," +
      "  c.source_aui, c.aui, c.cui " +
      " FROM classes a, string_ui b,mrd_classes c " +
      " where a.sui = b.sui and a.code = ? and a.aui = c.aui " +
      " and a.last_assigned_cui = c.cui and c.expiration_date is null" +
      " and rownum < ?";
 
   public final static String READ_AUIS_WITH_NAME =
      "SELECT a.atom_id, a.version_id, a.source,c.root_source, a.termgroup, c.tty," +
      "  a.termgroup_rank, c.code, c.sui, c.lui, a.generated_status," +
      "  a.last_release_cui, a.dead, a.status, a.authority, a.timestamp," +
      "  a.insertion_date, a.concept_id, a.released, a.tobereleased," +
      "  a.last_molecule_id, a.last_atomic_action_id, a.rank," +
      "  a.last_release_rank, c.suppressible, a.last_assigned_cui," +
      "  c.isui, b.string, b.norm_string, " +
      "  NVL(c.language, 'ENG') AS language, c.source_cui, c.source_dui," +
      "  c.source_aui, c.aui, c.cui " +
      "FROM classes a, string_ui b,mrd_classes c " +
      "WHERE a.concept_id = ?" +
      "  AND a.aui = c.aui" +
      "  AND c.expiration_date is null" +
      "  AND a.tobereleased in ('Y','y')" +
      "  AND a.dead = 'N'" +
      "  AND b.sui = a.sui";
 
  public final static String READ_RELATIONSHIPS = 
	  " SELECT RELATIONSHIP_LEVEL, AUI_1, AUI_2, CUI_1, CUI_2, SG_TYPE_1, SG_TYPE_2, " +
      " RELATIONSHIP_NAME, RELATIONSHIP_ATTRIBUTE, SUPPRESSIBLE, ROOT_SOURCE, " +
      " ROOT_SOURCE_OF_LABEL, RUI, SOURCE_RUI, RELATIONSHIP_GROUP, " +
      " REL_DIRECTIONALITY_FLAG " +
      " FROM MRD_RELATIONSHIPS " +
      " WHERE EXPIRATION_DATE IS NULL AND CUI_1 = ? ";
  
  public final static String READ_INVERSE_RELATIONSHIPS = 
	  " SELECT RELATIONSHIP_LEVEL, AUI_1, AUI_2, CUI_1, CUI_2, SG_TYPE_1, SG_TYPE_2, " +
      " RELATIONSHIP_NAME, RELATIONSHIP_ATTRIBUTE, SUPPRESSIBLE, ROOT_SOURCE, " +
      " ROOT_SOURCE_OF_LABEL, RUI, SOURCE_RUI, RELATIONSHIP_GROUP, " +
      " REL_DIRECTIONALITY_FLAG " +
      " FROM MRD_RELATIONSHIPS " +
      " WHERE EXPIRATION_DATE IS NULL AND CUI_2 = ? ";
  
  public final static String READ_AUIS_CONDITIONS =
      " a " +
      "WHERE a.atom_id = ?" +
      "  AND a.dead = 'N'";

  public final static String READ_ATOM_NAME =
	  "SELECT b.STRING from atoms_ui a, string_ui b  where a.sui = b.sui " +
	  " and a.aui = ?";
  
  public final static String READ_PREFERRED_ATOM_NAME =
	  "SELECT c.STRING from concept_status a, classes b, string_ui c  where c.sui = b.sui " +
	  " and a.cui = ? and a.tobereleased  in ('Y','y') and a.preferred_atom_id = b.atom_id";

  public final static String READ_CONTEXT_RELATIONSHIPS = 
	  " SELECT AUI, PARENT_TREENUM, ROOT_SOURCE,HIERARCHICAL_CODE, RELATIONSHIP_ATTRIBUTE, " +
	  " RELEASE_MODE, RUI, SOURCE_RUI, RELATIONSHIP_GROUP " +
	  " FROM MRD_CONTEXTS WHERE AUI = ? AND EXPIRATION_DATE IS NULL";
  
  public final static String READ_MRD_ATTRIBUTE =
	  " SELECT ATTRIBUTE_LEVEL, UI,CUI,SG_TYPE,SUPPRESSIBLE,ATTRIBUTE_NAME, " +
	  " ATTRIBUTE_VALUE,CODE,ROOT_SOURCE,ATUI,SOURCE_ATUI,HASHCODE "  +
	  " FROM MRD_ATTRIBUTES WHERE CUI = ? and rownum < 10001";
  
  public final static String READ_MRD_LONG_ATTRIBUTE =
	  " SELECT ROW_SEQUENCE, TEXT_TOTAL, TEXT_VALUE " +
      " FROM MRD_STRINGTAB WHERE HASHCODE = ?" +
      " AND expiration_date IS NULL ORDER BY row_sequence ";

  public final static String READ_RUIS_FOR_REL_RELA_SOURCE = 
	  "SELECT RELATIONSHIP_LEVEL, AUI_1, AUI_2, CUI_1, CUI_2, " + 
	  " SG_TYPE_1, SG_TYPE_2, RELATIONSHIP_NAME, RELATIONSHIP_ATTRIBUTE, " +
	  " SUPPRESSIBLE, ROOT_SOURCE, ROOT_SOURCE_OF_LABEL, RUI, SOURCE_RUI, " +
	  " RELATIONSHIP_GROUP, REL_DIRECTIONALITY_FLAG " +
      " FROM mrd_relationships " +
      " where relationship_name = ? and relationship_attribute = ? " + 
      " and root_source = ? and expiration_date is null and rownum < ?";      

  
  public final static String READ_RUIS_FOR_REL_SOURCE = 
	  "SELECT RELATIONSHIP_LEVEL, AUI_1, AUI_2, CUI_1, CUI_2, " + 
	  " SG_TYPE_1, SG_TYPE_2, RELATIONSHIP_NAME, RELATIONSHIP_ATTRIBUTE, " +
	  " SUPPRESSIBLE, ROOT_SOURCE, ROOT_SOURCE_OF_LABEL, RUI, SOURCE_RUI, " +
	  " RELATIONSHIP_GROUP, REL_DIRECTIONALITY_FLAG " +
      " FROM mrd_relationships " +
      " where relationship_name = ? " +
      " and root_source = ? and expiration_date is null and rownum < ?";      


  public final static String READ_RUIS_FOR_REL = 
	  "SELECT RELATIONSHIP_LEVEL, AUI_1, AUI_2, CUI_1, CUI_2, " + 
	  " SG_TYPE_1, SG_TYPE_2, RELATIONSHIP_NAME, RELATIONSHIP_ATTRIBUTE, " +
	  " SUPPRESSIBLE, ROOT_SOURCE, ROOT_SOURCE_OF_LABEL, RUI, SOURCE_RUI, " +
	  " RELATIONSHIP_GROUP, REL_DIRECTIONALITY_FLAG " +
      " FROM mrd_relationships " +
      " where relationship_name = ? " +
      " and expiration_date is null and rownum < ?";      

  
  public final static String READ_RUIS_FOR_REL_RELA = 
	  "SELECT RELATIONSHIP_LEVEL, AUI_1, AUI_2, CUI_1, CUI_2, " + 
	  " SG_TYPE_1, SG_TYPE_2, RELATIONSHIP_NAME, RELATIONSHIP_ATTRIBUTE, " +
	  " SUPPRESSIBLE, ROOT_SOURCE, ROOT_SOURCE_OF_LABEL, RUI, SOURCE_RUI, " +
	  " RELATIONSHIP_GROUP, REL_DIRECTIONALITY_FLAG " +
      " FROM mrd_relationships " +
      " where relationship_name = ? and relationship_attribute = ? " +
      " and expiration_date is null and rownum < ?";      


  public final static String READ_RUIS_FOR_RELA = 
	  "SELECT RELATIONSHIP_LEVEL, AUI_1, AUI_2, CUI_1, CUI_2, " + 
	  " SG_TYPE_1, SG_TYPE_2, RELATIONSHIP_NAME, RELATIONSHIP_ATTRIBUTE, " +
	  " SUPPRESSIBLE, ROOT_SOURCE, ROOT_SOURCE_OF_LABEL, RUI, SOURCE_RUI, " +
	  " RELATIONSHIP_GROUP, REL_DIRECTIONALITY_FLAG " +
      " FROM mrd_relationships " +
      " where relationship_attribute = ? and expiration_date is null and rownum < ?";      

  
  public final static String READ_RUIS_FOR_RELA_SOURCE = 
	  "SELECT RELATIONSHIP_LEVEL, AUI_1, AUI_2, CUI_1, CUI_2, " + 
	  " SG_TYPE_1, SG_TYPE_2, RELATIONSHIP_NAME, RELATIONSHIP_ATTRIBUTE, " +
	  " SUPPRESSIBLE, ROOT_SOURCE, ROOT_SOURCE_OF_LABEL, RUI, SOURCE_RUI, " +
	  " RELATIONSHIP_GROUP, REL_DIRECTIONALITY_FLAG " +
      " FROM mrd_relationships " +
      " where relationship_attribute = ? " + 
      " and root_source = ? and expiration_date is null and rownum < ?";      
//      " and root_source = ? and expiration_date is null and rownum < 10001";

  public static final String READ_AUIS_FOR_TTY = 
	  " select b.STRING, AUI, CUI, a.LANGUAGE, ROOT_SOURCE, TTY, CODE, SOURCE_AUI, " + 
	  " SOURCE_CUI, SOURCE_DUI " +
	  "from mrd_classes a, string_ui b " +
	  " where tty = ? and a.sui = b.sui " + 
	  " and a.expiration_date is null and rownum < ?";

  public static final String READ_AUIS_FOR_TTY_SOURCE = 
	  " select b.STRING, AUI, CUI, a.LANGUAGE, ROOT_SOURCE, TTY, CODE, SOURCE_AUI, " + 
	  " SOURCE_CUI, SOURCE_DUI " +
	  "from mrd_classes a, string_ui b " +
	  " where tty = ? and a.sui = b.sui " + 
	  " and root_source = ? " +
	  " and a.expiration_date is null and rownum < ?";

  
  //
  // source type
  //
  private MultiMap source_type_cache = new MultiMap();
  
  private static MultiMap src_qacompare_cache = null;
  private static MultiMap qa_adjustment_cache = null;
  private MultiMap qa_compare_reason_cache = new MultiMap();
  private static MultiMap MRD_source_rank_cache = null;
  private static MultiMap MRD_termgroup_rank_cache = null;
  
  //
  // Static initializers for caches
  //
  static {
	  src_qacompare_cache = new MultiMap();
	  qa_adjustment_cache = new MultiMap();
	  MRD_source_rank_cache = new MultiMap();
	  MRD_termgroup_rank_cache = new MultiMap();
  }
  /**
   * Puts the source rank into cache.
   * @param sab the {@link String} representation of source abbreviation.
   * @throws DataSourceException if failed to cache source rank.
   */
  private void cacheMRDSourceRank(String sab) throws DataSourceException {
    MEMEToolkit.logComment("  Cache source_rank data - " + sab, true);

    // We need to do transformations for E-* and S-* sources
    String l_sab = sab;

    if (sab != null) {
      if (sab.startsWith("E-") || sab.startsWith("S-") ||
          sab.startsWith("RESCUE") || sab.startsWith("L-")) {
        l_sab = "MTH";
      }
    } else {
      MRD_source_rank_cache.clear(getDataSourceName());
    }

    // Query to retrieve all source row
    String sr_query = "SELECT a.source, a.rank, a.normalized_source, " +
        " a.restriction_level, a.root_source, a.source_official_name," +
        " a.source_short_name, a.citation,a.character_set, a.valid_start_date, a.valid_end_date," +
        "  a.insert_meta_version, a.version, " +
        "  a.inverter_contact, a.nlm_contact, " +
        " a.acquisition_contact,a.license_contact, a.language, " +
        " a.notes, " +
        " a.rel_directionality_flag " +
        "FROM mrd_source_rank a WHERE REMOVE_META_VERSION IS NULL  AND IS_CURRENT = 'Y' ";

    if (sab != null) {
      sr_query = sr_query + " AND a.root_source = ? ";
    }

    try {

      PreparedStatement sr_pstmt = null;
      sr_pstmt = prepareStatement(sr_query);
      if(l_sab != null)
    	  sr_pstmt.setString(1, l_sab);
      ResultSet rs = sr_pstmt.executeQuery();

      // Read
      while (rs.next()) {
        // Create and populate Source object
        String rs_string = rs.getString("ROOT_SOURCE");
        Source source = new Source.Default();

        // If we are using a single SAB value, use it here
        // Note, sab might be E-TPW, but l_sab is MTH
        // We want characteristics of MTH with an abbreviation of E-TPW
        if (sab != null) {
          source.setSourceAbbreviation(sab);
        }
        else {
          source.setSourceAbbreviation(rs_string);

        }
        source.setRank(new Rank.Default(rs.getInt("RANK")));
        source.setNormalizedSourceAbbreviation(rs.getString("NORMALIZED_SOURCE"));
        source.setSourceVersion(rs.getString("VERSION"));
        source.setRestrictionLevel(rs.getString("RESTRICTION_LEVEL"));
        source.setOfficialName(rs.getString("SOURCE_OFFICIAL_NAME"));
        source.setShortName(rs.getString("SOURCE_SHORT_NAME"));
        source.setCitation(rs.getString("CITATION"));
        if (rs.getString("REL_DIRECTIONALITY_FLAG") != null) {
          source.setRelationshipDirectionalityFlag(rs.getString(
              "REL_DIRECTIONALITY_FLAG").equals("Y") ? true : false);

        }
        String language = null;
        try {
          language = rs.getString("LANGUAGE");
          if (language != null) {
            source.setLanguage(getLanguage(language));
          }
        }
        catch (BadValueException bve) {
          DataSourceException dse = new DataSourceException(
              "Failed to set language.", language, bve);
          throw dse;
        }

        source.setIsCurrent(true);

        // Add object to HashMap
        if (sab != null) {
          MRD_source_rank_cache.put(getDataSourceName(), sab, source);
        }
        else {
          MRD_source_rank_cache.put(getDataSourceName(), rs_string, source);
        }
      }

      // Close query set
      sr_pstmt.close();
      rs.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to cache source rank.", sr_query, se);
      dse.setDetail("query", sr_query);
      throw dse;
    }

    // MEMEToolkit.trace(
    //    "MEMEConnection.cacheSourceRank(String) - finished: "
    //    + getSources());
  }

  /**
   * Puts the termgroup rank into cache.
   * @param tg the {@link String} representation of termgroup.
   * @throws DataSourceException if failed to cache termgroup rank.
   * @throws BadValueException if failed due to invalid data value.
   */
  private void cacheMRDTermgroupRank(String tg, Source source) throws DataSourceException,
      BadValueException {
    MEMEToolkit.logComment("  Cache termgroup_rank data " +
                           ( (tg == null) ? "" : tg), true);

    // Query to retrieve termgroup ranks
    String tr_query = "SELECT termgroup, " +
        "SUBSTR(termgroup, 0, INSTR(termgroup,'/')-1) AS source, " +
        "SUBSTR(termgroup, INSTR(termgroup,'/')+1) AS tty, " +
        "rank, suppressible FROM mrd_termgroup_rank";
    if (tg != null) {
      tr_query = tr_query + " WHERE termgroup = '" + tg + "'";
    } else {
    	MRD_termgroup_rank_cache.clear(getDataSourceName());
    }
    
  	try {
      Statement tr_stmt = createStatement();
      ResultSet rs = tr_stmt.executeQuery(tr_query);

      // Read
      while (rs.next()) {
        // Create and populate Termgroup object
        String rs_string = rs.getString("TERMGROUP");
        Termgroup termgroup = new Termgroup.Default();
        termgroup.setTermType(rs.getString("TTY"));
        termgroup.setSource(source);
        termgroup.setRank(new Rank.Default(rs.getInt("RANK")));
        termgroup.setSuppressible(rs.getString("SUPPRESSIBLE"));
        
        // Add object to HashMap
        MRD_termgroup_rank_cache.put(getDataSourceName(), rs_string, termgroup);
      }

      // Close query set
      tr_stmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to cache termgroup rank.", tr_query, se);
      dse.setDetail("query", tr_query);
      throw dse;
    }

    MEMEToolkit.trace(
        "MEMEConnection.cacheTermgroupRank(String) - finished.");
  }
  /**
   * Implements {@link MRDDataSource#getMRDTermgroup(String, Source)}.
   * @param termgroup_name the {@link String} representation of termgroup
   * rank.
   * @param source
   * @return the {@link Termgroup}.
   * @throws DataSourceException if failed get termgroup.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Termgroup getMRDTermgroup(String tg, Source source) throws
      DataSourceException, BadValueException {

	// Look it up in the cache
    Termgroup termgroup = (Termgroup) MRD_termgroup_rank_cache.get(
        getDataSourceName(), tg  );

    // If its not in the cache, look it up again
    if (termgroup == null || termgroup.getSource() == null) {
      cacheMRDTermgroupRank(tg, source);
      termgroup = (Termgroup) MRD_termgroup_rank_cache.get(getDataSourceName(),
    		  tg);
    }

    // If its still not there, it is illegal
    if (termgroup == null) {
      BadValueException bve = new BadValueException("Illegal termgroup value.");
      bve.setDetail("termgroup", tg);
      throw bve;
    }
    return termgroup;
  }
  /**
   * Puts the semantic type into cache.
   * @throws DataSourceException if failed to cache semantic types.
   */

  /**
   * Implements {@link MEMEDataSource#getSource(String)}.
   * @param source_name the {@link String} representation of source name.
   * @return the {@link Source}.
   * @throws DataSourceException if failed to get source.
   * @throws BadValueException if failed due to invalid data value.
   */
  public Source getSource_MRDConceptReport(String source_name) throws DataSourceException,
      BadValueException {

    // Convert ENG- sources to the base source name
    String l_source = null;
    if (source_name.startsWith("ENG-")) {
      l_source = source_name.substring(4);
    }
    else {
      l_source = source_name;

      // Look up the source
    }
    Source source = (Source) MRD_source_rank_cache.get(getDataSourceName(),
        l_source);

    // If its not in the cache, look it up again
    if (source == null) {
      cacheMRDSourceRank(source_name);
      source = (Source) MRD_source_rank_cache.get(getDataSourceName(), l_source);
    }


    return source;
  }
  //
  // Constructors
  //

  /**
   * Instantiates a {@link MRDConnection}.
   * @param conn the JDBC {@link Connection}
   * @throws DataSourceException if failed to load data source
   * @throws BadValueException if failed due to invalid data value
   * @throws ReflectionException if failed to load or instantiate class
   */
  public MRDConnection(Connection conn) throws DataSourceException,
      BadValueException, ReflectionException {
    super(conn);
    enableBuffer();
    // set modes!
    try {
      CallableStatement stmt = prepareCall("{call MEME_UTILITY.set_mode_mrd}");
      stmt.execute();
      stmt.close();
      stmt = prepareCall("{call MEME_UTILITY.set_ddl_commit_off}");
      stmt.execute();
      stmt.close();
      Statement pstmt = createStatement();
      pstmt.executeUpdate("ALTER SESSION SET sort_area_size=100000000");
      pstmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to set modes.", this, se);
      dse.setDetail("execute",
          "MEME_UTILITY.set_mode_mrd, MEME_UTILITY.set_ddl_commit_off");
      MEMEToolkit.handleError(se);
      throw dse;
    }
    if (release_history_cache == null) {
      cacheReleaseInfo();
    }
  }

  /**
   * Refreshes the cached data.   This method is potentially different for an
   * MRD than it is for a MID connection.
   * @throws DataSourceException if failed to load data source
   * @throws BadValueException if failed due to invalid data value
   * @throws ReflectionException if failed to load or instantiate class
   */
  public void refreshCaches() throws DataSourceException, BadValueException,
      ReflectionException {
    super.refreshCaches();
   
    cacheMRDSourceRank(null);
    cacheMRDTermgroupRank(null, null);
    cacheSRCQAReasons();
  }

  /**
   * Adds specfied concept id to the "clean concepts" list.
   * @param concept_id the concept id
   * @throws DataSourceException if failed to add the concept id
   */
  public void addCleanConcept(int concept_id) throws DataSourceException {
    StringBuffer query = new StringBuffer(70);
    StringBuffer insert = new StringBuffer(70);
    try {
      query.append("SELECT concept_id FROM clean_concepts WHERE concept_id = ");
      query.append(concept_id);
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query.toString());
      if (!rset.next()) {
        insert.append("INSERT INTO clean_concepts (concept_id) VALUES(");
        insert.append(concept_id);
        insert.append(")");
        stmt.executeUpdate(insert.toString());
      }
      stmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to add data concept_id into clean concepts.", this, se);
      dse.setDetail("insert", insert.toString());
      throw dse;
    }
  }

  /**
   * Removes the specified concept id from the "clean concept" list.
   * @param concept_id the concept id
   * @throws DataSourceException if failed to remove the concept id
   */
  public void removeCleanConcept(int concept_id) throws DataSourceException {
    StringBuffer query = new StringBuffer(60);
    try {
      query.append("DELETE FROM clean_concepts WHERE concept_id = ");
      query.append(concept_id);
      Statement stmt = createStatement();
      stmt.executeUpdate(query.toString());
      stmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to remove concept_id from clean concepts.", this, se);
      dse.setDetail("query", query.toString());
      throw dse;
    }
  }

  /**
   * Indicates whether or not the specified concept is on the "clean concepts" list.
   * @param concept_id the concept id to check
   * @return <code>true</code> if the concept is clean, <code>false</code> otherwise
   * @throws DataSourceException if failed to check the concept id
   */
  public boolean isConceptClean(int concept_id) throws DataSourceException {
    StringBuffer call = new StringBuffer();
    try {
      boolean clean = false;
      call.append("{?= call MRD_OPERATIONS.is_concept_clean( ");
      call.append(concept_id);
      call.append(")}");
      CallableStatement cstmt = prepareCall(call.toString());
      cstmt.registerOutParameter(1, Types.INTEGER);
      cstmt.execute();
      if (cstmt.getInt(1) == 1) {
        clean = true;
      }
      cstmt.close();
      return clean;
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to check concept_id in clean concepts.", this, se);
      dse.setDetail("call", call.toString());
      throw dse;
    }
  }

  /**
   * Returns the id of the last synchronization from MID.
   * @return the id of the last synchronization from MID
   * @throws DataSourceException if failed to get last extraction id
   */
  public int getLastExtractionId() throws DataSourceException {
    final String query = "SELECT last_mid_event_id FROM extraction_history";
    try {
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query);
      rset.next();
      int ext_id = rset.getInt("last_mid_event_id");
      stmt.close();
      return ext_id;
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to get last extraction id.", this, se);
      dse.setDetail("select", query);
      MEMEToolkit.handleError(se);
      throw dse;
    }
  }

  /**
   * Sets the last extraction id.
   * @param id the last extraction id
   * @throws DataSourceException if failed to remove event
   */
  public void setLastExtractionId(int id) throws DataSourceException {
    StringBuffer query = new StringBuffer(100);
    query.append("UPDATE extraction_history set last_mid_event_id = '")
        .append(id)
        .append("'");
    try {
      Statement stmt = createStatement();
      stmt.executeUpdate(query.toString());
      stmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to Set last extraction id.", this, se);
      dse.setDetail("insert", query);
      MEMEToolkit.handleError(se);
      throw dse;
    }
  }

  /**
   * Removes the {@link LoggedAction} from the action log, adds it to
   * the "events processed" list.
   * @param event the {@link LoggedAction}
   * @throws DataSourceException if failed to remove event
   */
  public void removeDataChangeEvent(LoggedAction event) throws DataSourceException {
    // insert event into processed events
    String insert =
        "INSERT INTO events_processed "  +
        "SELECT action_id, elasped_time, action, authority, timestamp " +
        "FROM action_log WHERE action_id = " + event.getIdentifier();
    String delete = "DELETE FROM action_log WHERE action_id = " + event.getIdentifier();

    try {
      Statement stmt = createStatement();
      stmt.executeUpdate(insert);
      stmt.executeUpdate(delete);
      stmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to remove data change event.", this, se);
      dse.setDetail("delete", delete);
      dse.setDetail("insert", insert);
      MEMEToolkit.handleError(se);
      throw dse;
    }

  }


  /**
   * Returns all state {@link MRDProcessHandler}s.
   * @return all state {@link MRDProcessHandler}s
   * @throws DataSourceException if failed to get the registered state handlers
   */
  public MRDProcessHandler[] getRegisteredStateHandlers() throws
      DataSourceException {
    try {
      StringBuffer query = new StringBuffer("SELECT handler_name FROM ");
      query.append(" registered_handlers WHERE process = '")
          .append("GENERATE")
          .append("' AND activated = 'Y' ORDER BY row_sequence");
      ArrayList list = new ArrayList();
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query.toString());
      while (rset.next()) {
        list.add(createHandler(rset.getString("handler_name")));
      }
      stmt.close();
      if (list == null) {
        return new MRDProcessHandler[0];
      }
      return (MRDProcessHandler[]) list.toArray(new MRDProcessHandler[0]);
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Unable to build handler for process 'GENERATE' ", this, e);
      MEMEToolkit.handleError(e);
      throw dse;
    }
  }

  /**
   * Returns all feedback {@link MRDProcessHandler}s.
   * @return all feedback {@link MRDProcessHandler}s
   * @throws DataSourceException if failed to get the registered feedback handlers
   */
  public MRDProcessHandler[] getRegisteredFeedbackHandlers() throws
      DataSourceException {
    try {
      StringBuffer query = new StringBuffer("SELECT handler_name FROM ");
      query.append(" registered_handlers WHERE process = '")
          .append("FEEDBACK")
          .append("' AND activated = 'Y' ORDER BY row_sequence");
      ArrayList list = new ArrayList();
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query.toString());
      while (rset.next()) {
        list.add(createHandler(rset.getString("handler_name")));
      }
      stmt.close();
      if (list.size() == 0) {
        return new MRDProcessHandler[0];
      }
      return (MRDProcessHandler[]) list.toArray(new MRDProcessHandler[0]);
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Unable to build handler for process 'FEEDBACK' ", this, e);
      MEMEToolkit.handleError(e);
      throw dse;
    }
  }

  /**
   * Instantiates the specified handler class.
   * @param class_name class name
   * @return an instantiation of the class name
   * @throws MEMEException if failed to create handler
   */
  private Object createHandler(String class_name) throws MEMEException {
    try {
      return Class.forName("gov.nih.nlm.mrd.server.handlers." + class_name).
          newInstance();
    }
    catch (Exception e) {
      ReflectionException re = new ReflectionException(
          "Failed to load or instantiate class.", null, e);
      throw re;
    }

  }

  /**
   * Returns the {@link ActionEngine}.
   * @return the {@link ActionEngine}
   */
  public ActionEngine getActionEngine() {
    //return new MRDActionEngine(this);
    return new MRDActionEngine(this);
  }

  /**
   * Returns the {@link FileStatistics} for the specified file.
   * @param file the file name
   * @return the {@link FileStatistics} for the specified file
   * @throws DataSourceException if failed to get file statistics
   */
  public FileStatistics getFileStatistics(String file) throws
      DataSourceException {
    try {
      StringBuffer query = new StringBuffer();
      query
          .append("SELECT file_name, column_list, ")
          .append("byte_count, line_count, description ")
          .append("FROM mrd_file_statistics ")
          .append("WHERE file_name = '")
          .append(file)
          .append("' AND expiration_date IS NULL ");
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query.toString());
      ResultSetIterator iterator = new ResultSetIterator(rs, this,
          new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
            MEMEException {
          FileStatistics stats = new FileStatistics();
          stats.setByteCount(rs.getLong("byte_count"));
          stats.setLineCount(rs.getInt("line_count"));
          stats.setFileName(rs.getString("file_name"));
          stats.setDescription(rs.getString("description"));
          FieldedStringTokenizer st =
              new FieldedStringTokenizer(rs.getString("column_list"),
                                         String.valueOf(','));
          ColumnStatistics[] cols = new ColumnStatistics[st.countTokens()];
          int count = 0;
          while (st.hasMoreTokens()) {
            cols[count++] = getColumnStatistics(stats.getFileName(),
                                                st.nextToken());
          }
          stats.setAllColumnStatistics(cols);
          return stats;
        }
      }
      );
      if (iterator.hasNext()) {
        FileStatistics stats = (FileStatistics) iterator.next();
        stmt.close();
        return stats;
      }
      else {
        DataSourceException dse = new DataSourceException("no data is found ");
        dse.setDetail("file", file);
        dse.setDetail("query", query);
        stmt.close();
        throw dse;
      }
    }
    catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Problems getting file statistics.");
      dse.setDetail("file", file);
      throw dse;
    }
  }

  /**
   * Returns the {@link ColumnStatistics} for the specified file and column.
   * @param file the file name
   * @param col the column name
   * @return the {@link ColumnStatistics} for the specified file and column
   * @throws DataSourceException if failed to get columnn statistics of the file
   */
  public ColumnStatistics getColumnStatistics(String file, String col) throws
      DataSourceException {
    try {
      StringBuffer query = new StringBuffer();
      query
          .append("SELECT file_name, column_name, ")
          .append(
          "min_length, max_length, average_length, description, data_type ")
          .append("FROM mrd_column_statistics ")
          .append("WHERE file_name = '")
          .append(file)
          .append("' AND column_name ='")
          .append(col)
          .append("' AND expiration_date IS NULL ");
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query.toString());
      ResultSetIterator iterator = new ResultSetIterator(rs, this,
          new ResultSetMapper() {
        public Object map(ResultSet rs, MEMEDataSource mds) throws SQLException,
            MEMEException {
          ColumnStatistics stats = new ColumnStatistics();
          stats.setFileName(rs.getString("file_name"));
          stats.setColumnName(rs.getString("column_name"));
          stats.setMinLength(rs.getInt("min_length"));
          stats.setMaxLength(rs.getInt("max_length"));
          stats.setAverageLength(
              ( (BigDecimal) rs.getObject("average_length")).doubleValue());
          stats.setDescription(rs.getString("description"));
          stats.setDataType(rs.getString("data_type"));
          return stats;
        }
      }
      );
      if (iterator.hasNext()) {
        ColumnStatistics stats = (ColumnStatistics) iterator.next();
        stmt.close();
        return stats;
      }
      else {
        DataSourceException dse = new DataSourceException(
            "no data is found column ");
        dse.setDetail("file", file);
        dse.setDetail("column", col);
        throw dse;
      }
    }
    catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Problems getting column statistics. ");
      dse.setDetail("file", file);
      dse.setDetail("column", col);
      throw dse;
    }
  }

  /**
   * Applies changes to the specified {@link FileStatistics}.
   * @param stats the {@link FileStatistics} to change
   * @throws DataSourceException if failed to set file statistics
   */
  public void setFileStatistics(FileStatistics stats) throws
      DataSourceException {
    try {
      String update =
          "UPDATE mrd_file_statistics "
          + "SET byte_count = ?,line_count = ?"
          + "WHERE file_name = ? "
          + "  AND expiration_date IS NULL";
      PreparedStatement pstmt = prepareStatement(update);
      pstmt.setLong(1, stats.getByteCount());
      pstmt.setInt(2, stats.getLineCount());
      pstmt.setString(3, stats.getFileName());
      int cnt = pstmt.executeUpdate();
      if (cnt <= 0) {
        DataSourceException dse = new DataSourceException(
            "Can't find corresponding file name to update statistics ");
        dse.setDetail("file", stats.getFileName());
        throw dse;
      }
      ColumnStatistics[] cols = stats.getAllColumnStatistics();
      for (int i = 0; i < cols.length; i++) {
        setColumnStatistics(cols[i]);
      }
      pstmt.close();
    }
    catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Database problem updating file statistics ");
      dse.setDetail("file", stats.getFileName());
      throw dse;
    }
  }

  /**
   * Applies changes to the specified {@link ColumnStatistics}
   * @param stats the {@link ColumnStatistics} to change
   * @throws DataSourceException if failed to set column statistics of the file
   */
  public void setColumnStatistics(ColumnStatistics stats) throws
      DataSourceException {
    try {
      String update =
          "UPDATE mrd_column_statistics "
          + "SET min_length = ?,max_length = ?,average_length = ? "
          + "WHERE file_name = ? "
          + "  AND column_name = ? "
          + "  AND expiration_date IS NULL";
      PreparedStatement pstmt = prepareStatement(update);
      pstmt.setInt(1, stats.getMinLength());
      pstmt.setInt(2, stats.getMaxLength());
      pstmt.setDouble(3, stats.getAverageLength());
      pstmt.setString(4, stats.getFileName());
      pstmt.setString(5, stats.getColumnName());
      int cnt = pstmt.executeUpdate();
      if (cnt <= 0) {
        DataSourceException dse = new DataSourceException(
            "Can't find corresponding column to update statistics");
        dse.setDetail("file", stats.getFileName());
        dse.setDetail("column", stats.getColumnName());
        throw dse;
      }
      pstmt.close();
    }
    catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Database problem updating column statistics ");
      dse.setDetail("file", stats.getFileName());
      dse.setDetail("column", stats.getColumnName());
      throw dse;
    }
  }

  /**
   * Caches the {@link ReleaseInfo}.
   * @throws DataSourceException if failed to cache release info
   */
  private void cacheReleaseInfo() throws DataSourceException {
    MEMEToolkit.logComment("  Cache release info", true);

    // Initialize HashMap
    release_history_cache = new HashMap(200);

    final String query = "SELECT * FROM release_history order by release";

    try {
      Statement stmt = createStatement();
      ResultSet rs = stmt.executeQuery(query);

      // Read
      while (rs.next()) {
        ReleaseInfo release_info = new ReleaseInfo();
        release_info.setName(rs.getString("release"));
        release_info.setReleaseDate(rs.getTimestamp("release_date"));
        release_info.setDescription(rs.getString("description"));
        release_info.setBuildHost(rs.getString("build_host"));
        release_info.setBuildUri(rs.getString("build_uri"));
        release_info.setReleaseHost(rs.getString("release_host"));
        release_info.setReleaseUri(rs.getString("release_uri"));
        release_info.setDocumentationHost(rs.getString("documentation_host"));
        release_info.setDocumentationUri(rs.getString("documentation_uri"));
        release_info.setMEDStartDate(rs.getTimestamp("med_start_date"));
        release_info.setMBDStartDate(rs.getTimestamp("mbd_start_date"));
        release_info.setStartDate(rs.getTimestamp("start_date"));
        release_info.setEndDate(rs.getTimestamp("end_date"));
        release_info.setAuthority(new Authority.Default(rs.getString(
            "authority")));
        release_info.setIsBuilt(rs.getString("built").toLowerCase().equals("y"));
        release_info.setIsPublished(rs.getString("published").toLowerCase().
                                    equals("y"));
        release_info.setGeneratorClass(rs.getString("generator_class"));
        String previous = rs.getString("previous_release");
        if(release_history_cache.containsKey(previous)) {
          release_info.setPreviousReleaseInfo((ReleaseInfo)release_history_cache.get(previous));
        }
        String previous_major = rs.getString("previous_major_release");
        if(release_history_cache.containsKey(previous_major)) {
          release_info.setPreviousMajorReleaseInfo((ReleaseInfo)release_history_cache.get(previous_major));
        }
        release_history_cache.put(release_info.getName(), release_info);
      }
      // Close statement
      stmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up release_history.", this, se);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /**
   * Returns the {@link ReleaseInfo} for the specified release name.
   * @param release_name the release name
   * @return the {@link ReleaseInfo} for the specified release name
   * @throws DataSourceException if failed to get release info
   */
  public ReleaseInfo getReleaseInfo(String release_name) throws
      DataSourceException {
    if (release_history_cache.get(release_name) == null) {
      cacheReleaseInfo();
    }
    return (ReleaseInfo) release_history_cache.get(release_name);
  }

  /**
   * Applies changes to the specified {@link ReleaseInfo}.
   * @param release_info the {@link ReleaseInfo} to change
   * @throws DataSourceException if failed to update release info
   */
  public void setReleaseInfo(ReleaseInfo release_info) throws
      DataSourceException {
    SimpleDateFormat dateformat = MEMEToolkit.getDateFormat();
    StringBuffer update = new StringBuffer();
    update.append("UPDATE release_history SET release = '")
        .append(release_info.getName())
        .append("', release_date = '")
        .append(dateformat.format(release_info.getReleaseDate()))
        .append("', description = '")
        .append(release_info.getDescription())
        .append("', generator_class = '")
        .append(release_info.getGeneratorClass())
        .append("', build_host = '")
        .append(release_info.getBuildHost())
        .append("', build_uri = '")
        .append(release_info.getBuildUri())
        .append("', release_host = '")
        .append(release_info.getReleaseHost())
        .append("', release_uri = '")
        .append(release_info.getReleaseUri())
        .append("', documentation_host = '")
        .append(release_info.getDocumentationHost())
        .append("', documentation_uri = '")
        .append(release_info.getDocumentationUri())
        .append("', start_date = '")
        .append(dateformat.format(release_info.getStartDate()))
        .append("', end_date = '")
        .append(dateformat.format(release_info.getEndDate()))
        .append("', authority = '")
        .append(release_info.getAuthority().toString())
        .append("', med_start_date = '")
        .append(dateformat.format(release_info.getMEDStartDate()))
        .append("', mbd_start_date = '")
        .append(dateformat.format(release_info.getMBDStartDate()))
        .append("', built = '")
        .append(release_info.isBuilt() ? 'Y' : 'N')
        .append("', published = '")
        .append(release_info.isPublished() ? 'Y' : 'N')
        .append("' WHERE release = '")
        .append(release_info.getName())
        .append("'");

    try {
      Statement stmt = createStatement();
      stmt.executeUpdate(update.toString());
      // Close statement
      stmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to update release_history.", this, se);
      dse.setDetail("update", update.toString());
      throw dse;
    }
    release_history_cache.put(release_info.getName(), release_info);
  }

  /**
   * Adds the specified {@link ReleaseInfo}.
   * @param release_info the {ReleaseInfo} to add
   * @throws DataSourceException if failed to add release info
   */
  public void addReleaseInfo(ReleaseInfo release_info) throws
      DataSourceException {
    SimpleDateFormat dateformat = MEMEToolkit.getDateFormat();
    StringBuffer insert = new StringBuffer();
    insert.append(
        "INSERT INTO release_history(release, release_date, description, ")
        .append(
        "generator_class, build_host, build_uri, release_host, release_uri, documentation_host, documentation_uri, ")
        .append(
        "start_date, end_date, authority, med_start_date, mbd_start_date, ")
        .append("built, published, previous_release, previous_major_release) ")
        .append("VALUES ( '")
        .append(release_info.getName())
        .append("', '")
        .append(dateformat.format(release_info.getReleaseDate()))
        .append("', '")
        .append(release_info.getDescription())
        .append("', '")
        .append(release_info.getGeneratorClass())
        .append("', '")
        .append(release_info.getBuildHost())
        .append("', '")
        .append(release_info.getBuildUri())
        .append("', '")
        .append(release_info.getReleaseHost())
        .append("', '")
        .append(release_info.getReleaseUri())
        .append("', '")
        .append(release_info.getDocumentationHost())
        .append("', '")
        .append(release_info.getDocumentationUri())
        .append("', '")
        .append(dateformat.format(release_info.getStartDate()))
        .append("', '")
        .append(dateformat.format(release_info.getEndDate()))
        .append("', '")
        .append(release_info.getAuthority().toString())
        .append("', '")
        .append(dateformat.format(release_info.getMEDStartDate()))
        .append("', '")
        .append(dateformat.format(release_info.getMBDStartDate()))
        .append("', '")
        .append(release_info.isBuilt() ? 'Y' : 'N')
        .append("', '")
        .append(release_info.isPublished() ? 'Y' : 'N')
        .append("', '")
        .append(release_info.getPreviousReleaseInfo().getName())
        .append("', '")
        .append(release_info.getPreviousMajorReleaseInfo().getName())
        .append("' )");

    try {
      Statement stmt = createStatement();
      stmt.executeUpdate(insert.toString());
      // Close statement
      stmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to insert release_history.", this, se);
      dse.setDetail("insert", insert.toString());
      throw dse;
    }
    release_history_cache.put(release_info.getName(), release_info);
  }

  /**
   * Removes the specified {@link ReleaseInfo}.
   * @param release_info the {@link ReleaseInfo} to remove
   * @throws DataSourceException if failed to delete release info
   */
  public void removeReleaseInfo(ReleaseInfo release_info) throws
      DataSourceException {
    String delete = "DELETE FROM release_history WHERE release = '"
        + release_info.getName() + "'";
    try {
      Statement stmt = createStatement();
      stmt.executeUpdate(delete);
      // Close statement
      stmt.close();
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to delete release_history.", this, se);
      dse.setDetail("delete", delete);
      throw dse;
    }
    release_history_cache.remove(release_info.getName());
  }

  /**
   * Returns all {@link ReleaseInfo}.
   * @return all {@link ReleaseInfo}
   * @throws DataSourceException if failed to get release history
   */
  public ReleaseInfo[] getReleaseHistory() throws DataSourceException {
    if (release_history_cache == null) {
      cacheReleaseInfo();
    }
    return (ReleaseInfo[]) release_history_cache.values().toArray(new
        ReleaseInfo[0]);
  }

  /**
   * Returns the {@link ReleaseHandler} for the specified target name.
   * @param type type
   * @param target_name target name
   * @return the {@link ReleaseHandler} for the specified target name
   * @throws MEMEException if failed to get release handler
   */
  public ReleaseHandler getReleaseHandler(String type, String target_name) throws
      MEMEException {
    ReleaseHandler [] handlers = getReleaseHandlers(type);
    for(int i=0; i< handlers.length; i++) {
      if(target_name.equals(handlers[i].getTargetName()))
        return handlers[i];
    }
    throw new MissingDataException("Can't find the releasehandler for taget name " + target_name);
  }

  /**
   * Returns all {@link ReleaseHandler}s for the specified type.
   * @param type the type
   * @return all {@link ReleaseHandler}s for the specified type
   * @throws DataSourceException if failed to get release handlers
   */
  public ReleaseHandler[] getReleaseHandlers(String type) throws
      DataSourceException {
    try {
      StringBuffer query = new StringBuffer("SELECT * FROM ");
      query.append(" registered_handlers WHERE process = '")
          .append("RELEASE")
          .append("' AND type = '")
          .append(type)
          .append("' ORDER BY row_sequence");
      ArrayList list = new ArrayList();
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query.toString());
      while (rset.next()) {
        ReleaseHandler handler = (ReleaseHandler)createHandler(rset.getString("handler_name"));
        handler.setAuthority(new Authority.Default(rset.getString("authority")));
        handler.setDependencies(rset.getString("dependencies"));
        handler.setIsActive(rset.getString("activated").equalsIgnoreCase("Y"));
        handler.setTimestamp(rset.getDate("timestamp"));
        list.add(handler);
      }
      stmt.close();
      if (list == null) {
        return new ReleaseHandler[0];
      }
      return (ReleaseHandler[]) list.toArray(new ReleaseHandler[0]);
    }
    catch (Exception e) {
      DataSourceException dse = new DataSourceException(
          "Unable to build handler", this, e);
      dse.setDetail("process", "RELEASE");
      dse.setDetail("type", type);
      MEMEToolkit.handleError(e);
      throw dse;
    }
  }

  /**
   * Returns all {@link QAResult}s for the specified release target.
   * @param release_name the release name
   * @param target_name the release name
   * @return all {@link QAResult}s for the specified release target
   * @throws DataSourceException if failed to get {@link QAResult}s
   */
  public QAResult[] getQAResults(String release_name, String target_name) throws
      DataSourceException {
    String query = "SELECT * FROM qa_" + target_name + "_" + release_name;
    try {
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query);
      ArrayList list = new ArrayList();
      while (rset.next()) {
        QAResult result = new QAResult();
        result.setName(rset.getString("TEST_NAME"));
        result.setValue(rset.getString("TEST_VALUE"));
        result.setCount(rset.getLong("TEST_COUNT"));
        list.add(result);
      }
      if (list == null) {
        return new QAResult[0];
      }
      return (QAResult[]) list.toArray(new QAResult[0]);
    }
    catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Failed to access qa result", this, e);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /**
   * Returns the {@link QAResultReason}s for the specified release, comparison release, and target.
   * @param release_name the release name
   * @param comparison_name the comparison release name
   * @param target_name the target name
   * @return the {@link QAResultReason}s for the specified release, comparison release, and target
   * @throws DataSourceException if failed to get {@link QAResultReason}s
   */
  public QAResultReason[] getQAResultReasons(String[] release_name,
                                             String[] comparison_name,
                                             String target_name) throws
      DataSourceException {
    StringBuffer query = new StringBuffer("SELECT * FROM qa_result_reasons WHERE " +
        "target_name = '" + target_name + "' AND release_name in (");
    for(int i=0; i<release_name.length; i++) {
      query.append("'").append(release_name[i]).append("'");
      if(i<release_name.length - 1)
        query.append(",");
    }
    query.append(") AND  comparison_name in (");
    for(int i=0; i<comparison_name.length; i++) {
      query.append("'").append(comparison_name[i]).append("'");
      if(i<comparison_name.length - 1)
        query.append(",");
    }
    query.append(")");
    try {
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query.toString());
      ArrayList list = new ArrayList();
      while (rset.next()) {
        QAResultReason qareason = new QAResultReason();
        qareason.setReleaseName(rset.getString("release_name"));
        qareason.setComparisonName(rset.getString("comparison_name"));
        qareason.setName(rset.getString("test_name"));
        qareason.setNameOperator(rset.getString("test_name_operator"));
        qareason.setCount(rset.getLong("test_count"));
        qareason.setCountOperator(rset.getString("test_count_operator"));
        qareason.setValue(rset.getString("test_value"));
        qareason.setValueOperator(rset.getString("test_value_operator"));
        qareason.setReason(rset.getString("reason"));
        list.add(qareason);
      }
      stmt.close();
      MEMEToolkit.trace("getQAResultReasons : release_name = " + release_name +
                        " comparison_name = " + comparison_name +
                        " traget_name = " + target_name + " count = " +
                        list.size());
      if (list == null) {
        return new QAResultReason[0];
      }
      return (QAResultReason[]) list.toArray(new QAResultReason[0]);
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to access qa result reason", this, e);
      throw dse;
    }
  }

  /**
   * Returns the {@link QAComparisonReason}s for the specfied release name,
   * comparison release name, and target name.
   * @param release_name the release name
   * @param comparison_name the comparison release name
   * @param target_name the target name
   * @return the {@link QAComparisonReason}s for the specfied release name,
   * comparison release name, and target nme
   * @throws DataSourceException if failed to get {@link QAComparisonReason}s
   */
  public QAComparisonReason[] getQAComparisonReasons(String[] release_name,
      String[] comparison_name,
      String target_name) throws
      DataSourceException {
    StringBuffer query = new StringBuffer("SELECT * FROM qa_comparison_reasons WHERE " +
        "target_name = '" + target_name + "' AND release_name in (");
    for(int i=0; i<release_name.length; i++) {
      query.append("'").append(release_name[i]).append("'");
      if(i<release_name.length - 1)
        query.append(",");
    }
    query.append(") AND  comparison_name in (");
    for(int i=0; i<comparison_name.length; i++) {
      query.append("'").append(comparison_name[i]).append("'");
      if(i<comparison_name.length - 1)
        query.append(",");
    }
    query.append(")");
    try {
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query.toString());
      ArrayList list = new ArrayList();
      while (rset.next()) {
        QAComparisonReason qareason = new QAComparisonReason();
        qareason.setReleaseName(rset.getString("release_name"));
        qareason.setComparisonName(rset.getString("comparison_name"));
        qareason.setName(rset.getString("test_name"));
        qareason.setNameOperator(rset.getString("test_name_operator"));
        qareason.setCount(rset.getLong("test_count_1"));
        qareason.setCountOperator(rset.getString("test_count_1_operator"));
        qareason.setComparisonCount(rset.getLong("test_count_2"));
        qareason.setComparisonCountOperator(rset.getString(
            "test_count_2_operator"));
        qareason.setDiffCount(rset.getLong("count_diff"));
        qareason.setDiffCountOperator(rset.getString("test_diff_operator"));
        qareason.setValue(rset.getString("test_value"));
        qareason.setValueOperator(rset.getString("test_value_operator"));
        qareason.setReason(rset.getString("reason"));
        list.add(qareason);
      }
      stmt.close();
      MEMEToolkit.trace("getQAComparisonReasons : release_name = " +
                        release_name + " comparison_name = " + comparison_name +
                        " traget_name = " + target_name + " count = " +
                        list.size());
      if (list == null) {
        return new QAComparisonReason[0];
      }
      return (QAComparisonReason[]) list.toArray(new QAComparisonReason[0]);
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to access qa result reason", this, e);
      throw dse;
    }
  }

  /**
   * Returns the "gold" {@link QAResult}s for the specified release.
   * @param release_name the release name
   * @param target_name the target name
   * @return the "gold" {@link QAResult}s for the specified release
   * @throws DataSourceException if failed to get {@link QAResult}s
   */
  public QAResult[] getGoldStandardQAResults(String release_name,
                                             String target_name) throws
      DataSourceException {
    String query = "SELECT * FROM qa_" + target_name + "_gold_" + release_name;
    try {
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query);
      ArrayList list = new ArrayList();
      while (rset.next()) {
        QAResult result = new QAResult();
        result.setName(rset.getString("TESTNAME"));
        result.setValue(rset.getString("TESTVALUE"));
        result.setCount(rset.getLong("COUNT"));
        list.add(result);
      }
      if (list == null) {
        return new QAResult[0];
      }
      stmt.close();
      return (QAResult[]) list.toArray(new QAResult[0]);
    }
    catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Failed to access gold qa result", this, e);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /**
   * Activates the specified {@link RegisteredHandler}.
   * @param handler the {@link RegisteredHandler} to activate
   * @throws DataSourceException if failed to activate handler
   */
  public void activateRegisteredHandler(RegisteredHandler handler) throws
      DataSourceException {
    String handler_name = MEMEToolkit.getUnqualifiedClassName(handler);
    try {
      StringBuffer query = new StringBuffer("SELECT handler_name FROM ");
      query.append(" registered_handlers WHERE process = '")
          .append(handler.getProcess())
          .append("' and type = '")
          .append(handler.getType())
          .append("' and handler_name = '")
          .append(handler_name)
          .append("'");
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query.toString());
      if (rset.next()) {
        StringBuffer update = new StringBuffer(
            "UPDATE registered_handlers SET activated = 'Y' WHERE ");
        update.append(" process = '")
            .append(handler.getProcess())
            .append("' and type = '")
            .append(handler.getType())
            .append("' and handler_name = '")
            .append(rset.getString("handler_name"))
            .append("'");
        stmt.executeUpdate(update.toString());
      }
      else {
        DataSourceException dse = new DataSourceException(
            "Unable to find target handler");
        dse.setDetail("handler_name", handler_name);
        throw dse;
      }
      stmt.close();
    }
    catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Unable to activate target handler", this, e);
      dse.setDetail("handler_name", handler_name);
      throw dse;
    }
  }

  /**
   * Deactivates the specified {@link RegisteredHandler}.
   * @param handler the {@link RegisteredHandler} to deactivate
   * @throws DataSourceException if failed to activate handler
   */
  public void deactivateRegisteredHandler(RegisteredHandler handler) throws
      DataSourceException {
    String handler_name = MEMEToolkit.getUnqualifiedClassName(handler);
    try {
      StringBuffer query = new StringBuffer("SELECT handler_name FROM ");
      query.append(" registered_handlers WHERE process = '")
          .append(handler.getProcess())
          .append("' and type = '")
          .append(handler.getType())
          .append("' and handler_name = '")
          .append(handler_name)
          .append("'");
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query.toString());
      if (rset.next()) {
        StringBuffer update = new StringBuffer(
            "UPDATE registered_handlers SET activated = 'N' WHERE ");
        update.append(" process = '")
            .append(handler.getProcess())
            .append("' and type = '")
            .append(handler.getType())
            .append("' and handler_name = '")
            .append(rset.getString("handler_name"))
            .append("'");
        stmt.executeUpdate(update.toString());
      }
      else {
        DataSourceException dse = new DataSourceException(
            "Unable to find target handler");
        dse.setDetail("handler_name", handler_name);
        throw dse;
      }
      stmt.close();
    }
    catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Unable to deactivate target handler", this, e);
      dse.setDetail("handler_name", handler_name);
      throw dse;
    }
  }

  /**
   * Returns the list of release termgroups.
   * @return the list of release termgroups
   * @throws DataSourceException if failed to get release termgroups
   */
  public ArrayList getReleaseTermgroups() throws DataSourceException {
    ArrayList list = new ArrayList();
    StringBuffer query = new StringBuffer();
    query.append("SELECT DISTINCT a.rank, b.root_source, tty, supres ")
        .append("FROM (SELECT rank, substr(normalized_termgroup, 1, instr(normalized_termgroup,'/')-1) as sab, ")
        .append("       substr(normalized_termgroup, instr(normalized_termgroup,'/')+1) as tty, ")
        .append("       suppressible as supres ")
        .append("      FROM mrd_termgroup_rank ")
        .append("      WHERE expiration_date IS NULL) a, mrd_source_rank b ")
        .append("WHERE b.expiration_date IS NULL AND is_current = 'Y' ")
        .append("  AND a.sab = b.source ")
        .append("  AND (root_source, tty) IN ")
        .append("    (SELECT DISTINCT root_source,tty FROM mrd_classes ")
        .append("     WHERE expiration_date IS NULL) ")
        .append("ORDER BY a.rank DESC, root_source DESC, tty DESC ");
    MEMEToolkit.trace("Query: " + query.toString());
    try {
      Statement stmt = createStatement();
      ResultSet rset = stmt.executeQuery(query.toString());
      while (rset.next()) {
        OrderedHashMap map = new OrderedHashMap();
        map.put("rank", Integer.toString(rset.getInt("rank")));
        map.put("root_source", rset.getString("root_source"));
        map.put("tty", rset.getString("tty"));
        map.put("supres", rset.getString("supres"));
        list.add(map);
      }
      stmt.close();
    }
    catch (SQLException e) {
      DataSourceException dse = new DataSourceException(
          "Unable to retrieve release termgroups", this, e);
      throw dse;
    }
    return list;
  }

  /**
   * Adds the specified {@link QAReason}.
   * @param qareason the reason
   * @param target_name the target name
   * @throws DataSourceException if failed to add reason
   */
  public void addQAReason(QAReason qareason, String target_name) throws
      DataSourceException {
    MEMEToolkit.trace("DDDD " + target_name + qareason.getName());
    if (qareason instanceof QAComparisonReason) {

      QAComparisonReason qacompare = (QAComparisonReason) qareason;
      String insert =
          "INSERT INTO qa_comparison_reasons(target_name,release_name,comparison_name," +
          "test_name,test_name_operator,test_value,test_value_operator," +
          "test_count_1,test_count_1_operator,test_count_2,test_count_2_operator,count_diff,test_diff_operator, reason) " +
          " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(insert);
        pstmt.setString(1, target_name);
        pstmt.setString(2, qareason.getReleaseName());
        pstmt.setString(3, qacompare.getComparisonName());
        pstmt.setString(4, qareason.getName());
        pstmt.setString(5, qareason.getNameOperator());
        pstmt.setString(6, qareason.getValue());
        pstmt.setString(7, qareason.getValueOperator());
        pstmt.setLong(8, qareason.getCount());
        pstmt.setString(9, qareason.getCountOperator());
        pstmt.setLong(10, qacompare.getComparisonCount());
        pstmt.setString(11, qacompare.getComparisonCountOperator());
        pstmt.setLong(12, qacompare.getDiffCount());
        pstmt.setString(13, qacompare.getDiffCountOperator());
        pstmt.setString(14, qareason.getReason());
        int count = pstmt.executeUpdate();
        pstmt.close();
        MEMEToolkit.trace("Query: QAComparison " + qareason.getClass().getName() +
                          " count = " + count);
      }
      catch (SQLException e) {
        e.printStackTrace();
        DataSourceException dse = new DataSourceException(
            "Failed to add qa result reason", this, e);
        dse.setDetail("insert", pstmt.toString());
        throw dse;
      }
    }
    else if (qareason instanceof QAResultReason) {
      String insert =
          "INSERT INTO qa_result_reasons(target_name,release_name,comparison_name," +
          "test_name,test_name_operator,test_value,test_value_operator," +
          "test_count,test_count_operator, reason) " +
          " values(?,?,?,?,?,?,?,?,?,?)";
      PreparedStatement pstmt = null;
      try {
        pstmt = prepareStatement(insert);
        pstmt.setString(1, target_name);
        pstmt.setString(2, qareason.getReleaseName());
        pstmt.setString(3, qareason.getComparisonName());
        pstmt.setString(4, qareason.getName());
        pstmt.setString(5, qareason.getNameOperator());
        pstmt.setString(6, qareason.getValue());
        pstmt.setString(7, qareason.getValueOperator());
        pstmt.setLong(8, qareason.getCount());
        pstmt.setString(9, qareason.getCountOperator());
        pstmt.setString(10, qareason.getReason());
        int count = pstmt.executeUpdate();
        pstmt.close();
        MEMEToolkit.trace("Query: QAResult " + qareason.getClass().getName() +
                          " count = " + count);
      }
      catch (SQLException e) {
        e.printStackTrace();
        DataSourceException dse = new DataSourceException(
            "Failed to add qa result reason", this, e);
        dse.setDetail("insert", pstmt.toString());
        throw dse;
      }
    }
  }

  /**
   * Removes the specified {@link QAReason}.
   * @param qareason the {@link QAReason}
   * @param target_name the target name
   * @throws DataSourceException if failed to remove reason
   */
  public void removeQAReason(QAReason qareason, String target_name) throws
      DataSourceException {
    if (qareason instanceof QAComparisonReason) {
      QAComparisonReason qacompare = (QAComparisonReason) qareason;
      StringBuffer query = new StringBuffer(60);
      try {
        query.append("DELETE FROM qa_comparison_reasons WHERE target_name = '").
            append(target_name).append("'")
            .append(" AND release_name = '").append(qacompare.getReleaseName()).
            append(
            "'")
            .append(" AND comparison_name = '").append(qacompare.
            getComparisonName()).append(
            "'")
            .append(" AND test_name = '").append(qacompare.getName()).append(
            "'")
            .append(" AND test_name = '").append(qacompare.getName()).append(
            "'")
            .append(" AND NVL('").append(qacompare.getNameOperator()).append(
            "','null') = NVL(test_name_operator,'null') ")
            .append(" AND NVL('").append(qacompare.getValue()).append(
            "','null') = NVL(test_value,'null') ")
            .append(" AND NVL('").append(qacompare.getValueOperator()).append(
            "','null') = NVL(test_value_operator,'null') ")
            .append(" AND test_count_1 = ").append(qacompare.getCount())
            .append(" AND NVL('").append(qacompare.getCountOperator()).append(
            "','null') = NVL(test_count_1_operator,'null') ")
            .append(" AND test_count_2 = ").append(qacompare.getComparisonCount())
            .append(" AND NVL('").append(qacompare.getComparisonCountOperator()).
            append(
            "','null') = NVL(test_count_2_operator,'null') ")
            .append(" AND count_diff = ").append(qacompare.getDiffCount())
            .append(" AND NVL('").append(qacompare.getDiffCountOperator()).
            append(
            "','null') = NVL(test_diff_operator,'null') ")
            .append(" AND reason = '").append(qacompare.getReason()).append("'");
        Statement stmt = createStatement();
        stmt.executeUpdate(query.toString());
        stmt.close();
      }
      catch (SQLException se) {
        DataSourceException dse = new DataSourceException(
            "Failed to remove reason from qa_result_reasons.", this, se);
        dse.setDetail("query", query.toString());
        throw dse;
      }
    }
    else if (qareason instanceof QAResultReason) {
      StringBuffer query = new StringBuffer(60);
      try {
        query.append("DELETE FROM qa_result_reasons WHERE target_name = '").
            append(target_name).append("'")
            .append(" AND release_name = '").append(qareason.getReleaseName()).
            append(
            "'")
            .append(" AND comparison_name = '").append(qareason.
            getComparisonName()).append(
            "'")
            .append(" AND test_name = '").append(qareason.getName()).append(
            "'")
            .append(" AND NVL('").append(qareason.getNameOperator()).append(
            "','null') = NVL(test_name_operator,'null') ")
            .append(" AND NVL('").append(qareason.getValue()).append(
            "','null') = NVL(test_value,'null') ")
            .append(" AND NVL('").append(qareason.getValueOperator()).append(
            "','null') = NVL(test_value_operator,'null') ")
            .append(" AND test_count = ").append(qareason.getCount())
            .append(" AND NVL('").append(qareason.getCountOperator()).append(
            "','null') = NVL(test_count_operator,'null') ")
            .append(" AND reason = '").append(qareason.getReason()).append("'");
        Statement stmt = createStatement();
        stmt.executeUpdate(query.toString());
        stmt.close();
      }
      catch (SQLException se) {
        DataSourceException dse = new DataSourceException(
            "Failed to remove reason from qa_result_reasons.", this, se);
        dse.setDetail("query", query.toString());
        throw dse;
      }
    }

  }
  /**
   * Returns the {@link Source}s matching the specified type.
   * @param type the type (@see SourceType})
   * @param release_info the {@link ReleaseInfo}
   * @return the {@link Source}s matching the specified type
   * @throws BadValueException if failed due to invalid data value
   * @throws DataSourceException if failed to get sources by type
   */
  public Source[] getSourceByType(int type, ReleaseInfo release_info) throws
      BadValueException, DataSourceException {
    if (source_type_cache.get(release_info.getName(),new Integer(type)) == null) {
      cacheSourceType(release_info);
    }
    return (Source[]) source_type_cache.get(release_info.getName(),new Integer(type));
  }

  /**
   * Caches the source types for the specified release.
   * @param release_info the {@link ReleaseInfo}
   * @throws DataSourceException if failed to cache sources
   * @throws BadValueException if failed due to invalid data value
   */
  private void cacheSourceType(ReleaseInfo release_info) throws BadValueException, DataSourceException {
    // Initialize HashMap
    String query = "";
    try {
      Statement stmt = createStatement();

      //
      // Get all cases inserted in this or previous release
      // that was not inserted prior to (or equal to) the previous major release
      //
      query =
          "SELECT source FROM mrd_source_rank " +
          "WHERE expiration_date IS NULL " +
          "  AND insert_meta_version in " +
          "    ('" + release_info.getName() + "','" +
                     release_info.getPreviousReleaseInfo().getName() + "')" +
          "  AND root_source NOT IN" +
          "      (SELECT root_source FROM mrd_source_rank" +
          "       WHERE expiration_date IS NULL" +
          "         AND insert_meta_version <= '" +
          release_info.getPreviousMajorReleaseInfo().getName() + "'" +
          "       )";
      ResultSet rset = stmt.executeQuery(query);
      ArrayList source_list = new ArrayList();
      while(rset.next()) {
        source_list.add(getSource(rset.getString(1)));
      }
      source_type_cache.put(release_info.getName(),new Integer(SourceType.NEW),source_list.toArray(new Source[source_list.size()]));

      //
      // Get all cases where root_source has no current version
      //
      query =
          "SELECT source FROM mrd_source_rank " +
          "WHERE expiration_date IS NULL" +
          "  AND root_source NOT IN" +
          "      (SELECT root_source FROM mrd_source_rank" +
          "       WHERE expiration_date IS NULL" +
          "         AND insert_meta_version is not null " +
          "         AND remove_meta_version is null)";
      rset = stmt.executeQuery(query);
      source_list = new ArrayList();
      while(rset.next()) {
        source_list.add(getSource(rset.getString(1)));
      }
      source_type_cache.put(release_info.getName(),new Integer(SourceType.OBSOLETE),source_list.toArray(new Source[source_list.size()]));

      //
      // Get all sources inserted after the previous major release
      // that were also removed before the current release (and presumably since the previous major)
      //
      query =
          "SELECT source FROM mrd_source_rank " +
          "WHERE expiration_date IS NULL " +
          "  AND insert_meta_version > '" +
                release_info.getPreviousMajorReleaseInfo().getName() + "'" +
          "  AND root_source IN" +
          "      (SELECT root_source FROM mrd_source_rank" +
          "       WHERE expiration_date IS NULL" +
          "         AND remove_meta_version >= '" +
                    release_info.getPreviousMajorReleaseInfo().getName() + "'" +
          "       )";
      rset = stmt.executeQuery(query);
      source_list = new ArrayList();
      while(rset.next()) {
        source_list.add(getSource(rset.getString(1)));
      }
      source_type_cache.put(release_info.getName(),new Integer(SourceType.UPDATE),source_list.toArray(new Source[source_list.size()]));
    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up source by type.", this, se);
      throw dse;
    }
  }

  /* (non-Javadoc)
   * @see gov.nih.nlm.mrd.sql.MRDDataSource#isExplainedBySRCQA(java.lang.String, java.lang.String, gov.nih.nlm.mrd.common.QAComparison)
   */
  public boolean isExplainedBySRCQA(String target, QAComparison compare) throws DataSourceException {
	  String key = target + compare.getName() + compare.getValue();
	  //MEMEToolkit.logComment("isExplainedBySRCQA " + key, true);
	  //MEMEToolkit.logComment("src_qa_compare = " + (src_qacompare_cache == null ? "null" : "Not null"));
	  //MEMEToolkit.logComment("key = " + (key == null ? "null" : key));
	  if(src_qacompare_cache.containsKey(getDataSourceName(),key)) {
		  //MEMEToolkit.logComment("isExplainedBySRCQA Found in cache " , true);
		  QAComparison  src_qa_compare = (QAComparison)src_qacompare_cache.get(getDataSourceName(),key);
		  //MEMEToolkit.logComment("src_qa_compare " + src_qa_compare.getName(), true);
		  //MEMEToolkit.logComment("src_qa_compare " + src_qa_compare.getValue(), true);
		  //MEMEToolkit.logComment("src_qa_compare " + src_qa_compare.getCount(), true);
		  //MEMEToolkit.logComment("src_qa_compare " + src_qa_compare.getComparisonCount(), true);
		  //MEMEToolkit.logComment("src_qa_compare " + src_qa_compare.getDiffCount(), true);
		  //MEMEToolkit.logComment("compare " + compare.getName(), true);
		  //MEMEToolkit.logComment("compare " + compare.getValue(), true);
		  //MEMEToolkit.logComment("compare " + compare.getCount(), true);
		  //MEMEToolkit.logComment("compare " + compare.getComparisonCount(), true);
		  //MEMEToolkit.logComment("compare " + compare.getDiffCount(), true);
		  if(src_qa_compare.equals(compare)) {
			  //MEMEToolkit.logComment("isExplainedBySRCQA add reason " , true);
			  QAReason reason = new QAComparisonReason();
			  reason.setName(compare.getName());
			  reason.setReleaseName("Current");
			  reason.setComparisonName("Previous");
			  reason.setReason("Explained by MID QA");
			  qa_compare_reason_cache.put(target,compare, reason);
			  return true;
		  } else if(src_qa_compare.getName().equals(compare.getName()) && (compare.getValue() == null || src_qa_compare.getValue().equals(compare.getValue()))) {
			  if(qa_adjustment_cache.containsKey(getDataSourceName(),key)) {
				  //MEMEToolkit.logComment("isExplainedBySRCQA MID QA with adjustment " , true);
				  long adj_count = ((Long)qa_adjustment_cache.get(getDataSourceName(),key)).longValue();
				  if(src_qa_compare.getDiffCount() + adj_count == compare.getDiffCount()) {
					  QAReason reason = new QAComparisonReason();
					  reason.setName(compare.getName());
					  reason.setReleaseName("Current");
					  reason.setComparisonName("Previous");
					  reason.setReason("Explained by MID QA with adjustment");
					  qa_compare_reason_cache.put(target,compare, reason);
					  return true;
				  }
			  }
		  }
	  } else if(qa_adjustment_cache.containsKey(getDataSourceName(),key)) {
		  long adj_count = ((Long)qa_adjustment_cache.get(getDataSourceName(),key)).longValue();
		  if(adj_count == compare.getDiffCount()) {
			  QAReason reason = new QAComparisonReason();
			  reason.setName(compare.getName());
			  reason.setReleaseName("Current");
			  reason.setComparisonName("Previous");
			  reason.setReason("Explained by QA adjustment");
			  //MEMEToolkit.logComment("isExplainedBySRCQA QA adjustment " , true);
			  qa_compare_reason_cache.put(target,compare, reason);
			  return true;
		  }	
	  	}
	  //MEMEToolkit.logComment("isExplainedBySRCQA  Return false"  , true);
	  return false;
  }

  /* (non-Javadoc)
   * @see gov.nih.nlm.mrd.sql.MRDDataSource#isExplainedBySRCQA(java.lang.String, java.lang.String, gov.nih.nlm.mrd.common.QAResult)
   */
  public boolean isExplainedBySRCQA(String target, String release, String previous_release, QAResult result) throws DataSourceException {
	  QAComparison compare = new QAComparison();
	  compare.setName(result.getName());
	  compare.setValue(result.getValue());
	  if(release.compareTo(previous_release) > 0) {
		  compare.setCount(result.getCount());
		  compare.setComparisonCount(0);
	  } else {
		  compare.setCount(0);
		  compare.setComparisonCount(result.getCount());
	  }
	  return isExplainedBySRCQA(target,compare);
  }

  /* (non-Javadoc)
   * @see gov.nih.nlm.mrd.sql.MRDDataSource#shouldBeExplainedBySRCQA(java.lang.String, gov.nih.nlm.mrd.common.QAComparison)
   */
  public boolean shouldBeExplainedBySRCQA(String target, QAComparison compare) throws DataSourceException {
	  return shouldBeExplainedBySRCQA(target, (QAResult)compare);
  }

  /* (non-Javadoc)
   * @see gov.nih.nlm.mrd.sql.MRDDataSource#shouldBeExplainedBySRCQA(java.lang.String, gov.nih.nlm.mrd.common.QAResult)
   */
  public boolean shouldBeExplainedBySRCQA(String target, QAResult result) throws DataSourceException {
	  try {
		  getCodeByValue("test_name_conversion", target + "~" + result.getName());
		  return true;
	  }catch(BadValueException bve) {
		  return false;
	  }
  }

  /**
   * Puts the code map into cache.
   * @throws DataSourceException if failed to cache code map.
   */
  private void cacheSRCQAReasons() throws BadValueException, DataSourceException {
    MEMEToolkit.logComment("  Cache SRC QAReasons", true);

    // Initialize HashMap
    MultiMap stype_map = new MultiMap();
    Map sources_map = new HashMap();
    Map rel_map = new HashMap();
    
    src_qacompare_cache.clear(getDataSourceName());
    qa_compare_reason_cache.clear(getDataSourceName());

    stype_map.put("stype", "","AUI");
    stype_map.put("stype", "CONCEPT_ID", "CUI");
    stype_map.put("stype", "CUI_SOURCE","CUI");
    stype_map.put("stype", "CUI_ROOT_SOURCE","CUI");
    stype_map.put("stype", "CODE_SOURCE","CODE");
    stype_map.put("stype", "CODE_STRIPPED_SOURCE","CODE");
    stype_map.put("stype", "CODE_ROOT_SOURCE","CODE");
    stype_map.put("stype", "CODE_TERMGROUP","CODE");
    stype_map.put("stype", "CODE_ROOT_TERMGROUP","CODE");
    stype_map.put("stype", "CUI_STRIPPED_SOURCE","CUI");
    stype_map.put("stype", "CUI_ROOT_SOURCE","CUI");
    stype_map.put("stype", "SOURCE_CUI","SCUI");
    stype_map.put("stype", "SOURCE_AUI","SAUI");
    stype_map.put("stype", "SOURCE_DUI","SDUI");
    stype_map.put("stype", "SOURCE_RUI","SRUI");
    stype_map.put("stype", "ROOT_SOURCE_AUI","SAUI");
    stype_map.put("stype", "ROOT_SOURCE_CUI","SCUI");
    stype_map.put("stype", "ROOT_SOURCE_DUI","SDUI");
    stype_map.put("stype", "ROOT_SOURCE_RUI","SRUI");    

    stype_map.put("stype1","","AUI");
    stype_map.put("stype1","CONCEPT_ID","CUI");
    stype_map.put("stype1","CUI_SOURCE","CUI");
    stype_map.put("stype1","CUI_ROOT_SOURCE","CUI");
    stype_map.put("stype1","CODE_SOURCE","CODE");
    stype_map.put("stype1","CODE_STRIPPED_SOURCE","CODE");
    stype_map.put("stype1","CODE_ROOT_SOURCE","CODE");
    stype_map.put("stype1","CODE_TERMGROUP","CODE");
    stype_map.put("stype1","CODE_ROOT_TERMGROUP","CODE");
    stype_map.put("stype1","CUI_STRIPPED_SOURCE","CUI");
    stype_map.put("stype1","CUI_ROOT_SOURCE","CUI");
    stype_map.put("stype1","SOURCE_CUI","SCUI");
    stype_map.put("stype1","SOURCE_AUI","SAUI");
    stype_map.put("stype1","SOURCE_DUI","SDUI");
    stype_map.put("stype1","SOURCE_RUI","SRUI");
    stype_map.put("stype1","ROOT_SOURCE_AUI","SAUI");
    stype_map.put("stype1","ROOT_SOURCE_CUI","SCUI");
    stype_map.put("stype1","ROOT_SOURCE_DUI","SDUI");
    stype_map.put("stype1","ROOT_SOURCE_RUI","SRUI");
    
    stype_map.put("stype2", "","AUI");
    stype_map.put("stype2", "CONCEPT_ID", "CUI");
    stype_map.put("stype2", "CUI_SOURCE","CUI");
    stype_map.put("stype2", "CUI_ROOT_SOURCE","CUI");
    stype_map.put("stype2", "CODE_SOURCE","CODE");
    stype_map.put("stype2", "CODE_STRIPPED_SOURCE","CODE");
    stype_map.put("stype2", "CODE_ROOT_SOURCE","CODE");
    stype_map.put("stype2", "CODE_TERMGROUP","CODE");
    stype_map.put("stype2", "CODE_STRIPPED_TERMGROUP","CODE");
    stype_map.put("stype2", "CODE_ROOT_TERMGROUP","CODE");
    stype_map.put("stype2", "CUI_STRIPPED_SOURCE","CUI");
    stype_map.put("stype2", "CUI_ROOT_SOURCE","CUI");
    stype_map.put("stype2", "SOURCE_CUI","SCUI");
    stype_map.put("stype2", "SOURCE_AUI","SAUI");
    stype_map.put("stype2", "SOURCE_DUI","SDUI");
    stype_map.put("stype2",  "SOURCE_RUI","SRUI");
    stype_map.put("stype2", "ROOT_SOURCE_AUI","SAUI");
    stype_map.put("stype2", "ROOT_SOURCE_CUI","SCUI");
    stype_map.put("stype2", "ROOT_SOURCE_DUI","SDUI");
    stype_map.put("stype2", "ROOT_SOURCE_RUI","SRUI");
    
    final StringBuffer query = new StringBuffer();
    /*
     * This query is based on MEME_INTEGRITY.src_obsolete_qa_diff using previous release name instaed o previous_name from source_version
     * 1. obs != cur
     * 2. obs not cur
     * 3. cur not obs
     * 4. NEW
     * 5. OLD
     */

    // 1. obs != cur
    query.append("SELECT DISTINCT")
    .append("     	obs.name, obs.value, ")
    .append("            obs.qa_count count_1, nvl(adj.qa_count,0) adj_count, ")
    .append("    	src.qa_count count_2")
    .append("        FROM ")
    .append("          (SELECT qa_id, name, REPLACE(value,current_name,previous_name) value,")
    .append("    		sum(qa_count) qa_count")
    .append("           FROM src_qa_results, (SELECT a.current_name, b.source as previous_name")
    .append("           		FROM source_version a, sims_info b, source_rank c")
    .append("           		WHERE b.insert_meta_version IS NOT NULL ")
    .append("           			AND b.remove_meta_version = ? ")
    .append("           			AND b.source = c.source ")
    .append("           			AND a.source = c.stripped_source)")
    .append("           WHERE (value like current_name || ',%' OR value = current_name)")
    .append("           GROUP BY qa_id, name, value, current_name, previous_name) src,")
    .append("          (SELECT qa_id_1, name, value, sum(qa_count) qa_count")
    .append("           FROM qa_diff_adjustment")
    .append("           GROUP BY qa_id_1, name, value) adj, ")
    .append("          (SELECT qa_id, name, value,")
    .append("    		sum(qa_count) qa_count")
    .append("           FROM src_obsolete_qa_results, (SELECT a.current_name, b.source as previous_name")
    .append("           		FROM source_version a, sims_info b, source_rank c")
    .append("           		WHERE b.insert_meta_version IS NOT NULL ")
    .append("           			AND b.remove_meta_version = ? ")
    .append("           			AND b.source = c.source ")
    .append("           			AND a.source = c.stripped_source)")
    .append("           WHERE (value like previous_name || ',%' OR value = previous_name)")
    .append("           GROUP BY qa_id, name, value) obs")
    .append("        WHERE obs.qa_id = adj.qa_id_1 (+)")
    .append("          AND obs.name = adj.name (+)")
    .append("          AND obs.value = adj.value (+)")
    .append("          AND src.qa_id = obs.qa_id")
    .append("          AND src.name = obs.name")
    .append("          AND src.value = obs.value")
    .append("          AND (obs.qa_count != 0 OR")
    .append("               src.qa_count != 0)")
    .append("        UNION ALL")
     // 2. obs not cur
    .append("        SELECT DISTINCT ")
    .append("    	obs.name, obs.value, ")
    .append("     	obs.qa_count count_1, NVL(adj.qa_count,0) adj_count,")
    .append("    	0 count_2")
    .append("        FROM ")
    .append("          (SELECT qa_id, name, value, sum(qa_count) qa_count")
    .append("           FROM src_obsolete_qa_results, (SELECT a.current_name, b.source as previous_name")
    .append("           		FROM source_version a, sims_info b, source_rank c")
    .append("           		WHERE b.insert_meta_version IS NOT NULL ")
    .append("           			AND b.remove_meta_version = ? ")
    .append("           			AND b.source = c.source ")
    .append("           			AND a.source = c.stripped_source)")
    .append("           WHERE value like previous_name || ',%' OR value = previous_name")
    .append("           GROUP BY qa_id, name, value) obs, ")
    .append("          (SELECT qa_id_1, name, value, sum(qa_count) qa_count")
    .append("           FROM qa_diff_adjustment")
    .append("           GROUP BY qa_id_1, name, value) adj")
    .append("        WHERE obs.qa_id = qa_id_1 (+)")
    .append("          AND obs.name = adj.name (+)")
    .append("          AND obs.value = adj.value (+)")
    .append("          AND obs.qa_count != 0 ")
    .append("          AND (obs.qa_id, obs.name, obs.value) IN")
    .append("        (SELECT qa_id, name, value")
    .append("         FROM src_obsolete_qa_results")
    .append("         MINUS")
    .append("         SELECT qa_id, name, REPLACE(value,current_name,previous_name)")
    .append("         FROM src_qa_results, (SELECT a.current_name, b.source as previous_name")
    .append("           		FROM source_version a, sims_info b, source_rank c")
    .append("           		WHERE b.insert_meta_version IS NOT NULL ")
    .append("           			AND b.remove_meta_version = ? ")
    .append("           			AND b.source = c.source ")
    .append("           			AND a.source = c.stripped_source)")
    .append("        WHERE value like current_name || ',%' OR value = current_name)")
    .append("        UNION ALL")
     // 3. cur not obs
    .append("        SELECT DISTINCT ")
    .append("    	src.name, src.value,")
    .append("    	0 count_1, ")
    .append("    	 src.qa_count count_2, NVL(adj.qa_count,0) adj_count")
    .append("        FROM ")
    .append("          (SELECT qa_id, name, REPLACE(value,current_name,previous_name) value,")
    .append("    		sum(qa_count) qa_count")
    .append("           FROM src_qa_results, (SELECT a.current_name, b.source as previous_name")
    .append("           		FROM source_version a, sims_info b, source_rank c")
    .append("           		WHERE b.insert_meta_version IS NOT NULL ")
    .append("           			AND b.remove_meta_version = ? ")
    .append("           			AND b.source = c.source ")
    .append("           			AND a.source = c.stripped_source)")
    .append("           WHERE value like current_name || ',%' OR value = current_name")
    .append("           GROUP BY qa_id, name, value, current_name, previous_name) src,")
    .append("          (SELECT qa_id_1, name, value, sum(qa_count) qa_count")
    .append("           FROM qa_diff_adjustment")
    .append("           GROUP BY qa_id_1, name, value) adj")
    .append("        WHERE src.qa_id = qa_id_1 (+)")
    .append("          AND src.name = adj.name (+)")
    .append("          AND src.value = adj.value (+)")
    .append("          AND src.qa_count != 0 ")
    .append("          AND (src.qa_id, src.name, src.value) NOT IN")
    .append("         (SELECT qa_id, name, value")
    .append("         FROM src_obsolete_qa_results)")
    .append("        UNION ALL")
     // 4. NEW
    .append("        SELECT DISTINCT ")
    .append("    	src.name, REPLACE(src.value,'-NEW') value,")
    .append("    	0 count_1, ")
    .append("    	 src.qa_count count_2, NVL(adj.qa_count,0) adj_count")
    .append("        FROM ")
    .append("          (SELECT qa_id, name, REPLACE(value,current_name,current_name || '-NEW') value,")
    .append("    		sum(qa_count) qa_count")
    .append("           FROM src_qa_results, (SELECT source as current_name FROM sims_info")
    .append("           		WHERE insert_meta_version = ? ")
    .append("           			AND source IN")
    .append("           			(SELECT current_name FROM source_version WHERE previous_name IS NULL))")
    .append("           WHERE value like current_name || ',%' OR value = current_name")
    .append("           GROUP BY qa_id, name, value, current_name) src,")
    .append("          (SELECT qa_id_1, name, value, sum(qa_count) qa_count")
    .append("           FROM qa_diff_adjustment")
    .append("           GROUP BY qa_id_1, name, value) adj")
    .append("        WHERE src.qa_id = qa_id_1 (+)")
    .append("          AND src.name = adj.name (+)")
    .append("          AND src.value = adj.value (+)")
    .append("          AND src.qa_count != 0 ")
    .append("        UNION ALL")
     // 5. OLD
    .append("        SELECT DISTINCT ")
    .append("    	obs.name, obs.value, ")
    .append("     	obs.qa_count count_1, NVL(adj.qa_count,0) adj_count,")
    .append("    	0 count_2")
    .append("        FROM ")
    .append("          (SELECT qa_id, name, value, sum(qa_count) qa_count")
    .append("           FROM src_obsolete_qa_results, (SELECT source as previous_name FROM sims_info")
    .append("           		WHERE remove_meta_version = ? ")
    .append("           			AND insert_meta_version IS NOT NULL")
    .append("           			AND source IN")
    .append("           			(SELECT previous_name FROM source_version WHERE current_name IS NULL")
    .append("           				AND previous_name IS NOT NULL))")
    .append("           WHERE value like previous_name || ',%' OR value = previous_name")
    .append("           GROUP BY qa_id, name, value, previous_name) obs, ")
    .append("          (SELECT qa_id_1, name, value, sum(qa_count) qa_count")
    .append("           FROM qa_diff_adjustment")
    .append("           GROUP BY qa_id_1, name, value) adj")
    .append("        WHERE obs.qa_id = qa_id_1 (+)")
    .append("          AND obs.name = adj.name (+)")
    .append("          AND obs.value = adj.value (+)")
    .append("          AND obs.qa_count != 0 ");

    try {
    	ReleaseInfo[] releases = getReleaseHistory();
    	ReleaseInfo current_release = null;
    	for(int i=0; i<releases.length; i++) {
    		if(current_release == null || releases[i].getName().compareTo(current_release.getName()) > 0 ) {
    			current_release = releases[i];
    		}
    	}
    	/*
    	 * if current_release or preivous release is null, we can't look up the data.
    	 */
	    //MEMEToolkit.logComment("Current release " + (current_release == null ? "null" : current_release.getName()) , true);
    	if(current_release == null || current_release.getPreviousReleaseInfo() == null) {
    		return;
    	}
    	
	    Source[] sources = super.getSources();
	    for(int i=0; i<sources.length; i++) {
	    	//MEMEToolkit.logComment("Sources Map = " + sources[i].getSourceAbbreviation()+ "|" + sources[i].getStrippedSourceAbbreviation(), true);
	    	sources_map.put(sources[i].getSourceAbbreviation(), sources[i].getStrippedSourceAbbreviation());
	    }
	    Statement stmt = createStatement();
        ResultSet rs = stmt.executeQuery("SELECT relationship_name, release_name " +
                "FROM inverse_relationships");
        while (rs.next()) {
          String rel = rs.getString("RELATIONSHIP_NAME");
          String release_rel = rs.getString("RELEASE_NAME");
          rel_map.put(rel, release_rel);
        }
        String previous_release = current_release.getPreviousReleaseInfo().getName();
        PreparedStatement pstmt = prepareStatement(query.toString());
        pstmt.setString(1, previous_release);
        pstmt.setString(2, previous_release);
        pstmt.setString(3, previous_release);
        pstmt.setString(4, previous_release);
        pstmt.setString(5, previous_release);
        pstmt.setString(6, current_release.getName());
        pstmt.setString(7, previous_release);
        rs = pstmt.executeQuery();

      // Read
      while (rs.next()) {
    	long count_1 = rs.getLong("count_1");
    	long adj_count = rs.getLong("adj_count");
    	long count_2 = rs.getLong("count_2");
    	if(count_1 + adj_count == count_2) {
	        QAComparison compare = new QAComparison();
	        compare.setName(rs.getString("name"));
	        compare.setValue(rs.getString("value"));
	        compare.setCount(count_2);
	        compare.setComparisonCount(count_1);
	        String target_test_name = getValueByCode("test_name_conversion",compare.getName());
	        //MEMEToolkit.logComment("target_test_name = " + target_test_name, true);
	        if(target_test_name != null) {
		        String target = target_test_name.substring(0, target_test_name.indexOf("~"));
		        String[] name_list = FieldedStringTokenizer.split(compare.getName(),"_");
		        String[] value_list = FieldedStringTokenizer.split(compare.getValue(),",");
		        for(int i=0; i< value_list.length; i++) {
		        	//MEMEToolkit.logComment("name_list[i] =" + name_list[i], true);
		        	//MEMEToolkit.logComment("value_list[i] =" + value_list[i], true);
		        	if(name_list[i].equals("sab")) {
		        		if(sources_map.containsKey(value_list[i]))
		        			value_list[i] = (String)sources_map.get(value_list[i]);
		        	}
		        	if(name_list[i].startsWith("stype")) {
		        		if(stype_map.containsKey(name_list[i], value_list[i])) {
		        			value_list[i] = (String)stype_map.get(name_list[i], value_list[i]);
		        		}
		        	}
		        	if(name_list[i].equals("rel")) {
		        		if(rel_map.containsKey(value_list[i]))
		        			value_list[i] = (String)rel_map.get(value_list[i]);
		        	}
		        }
		        StringBuffer value = new StringBuffer();
		        for(int i=0; i< value_list.length; i++) {
		        	//MEMEToolkit.logComment("value buffer =" + value_list[i], true);
		        	value.append(value_list[i]);
		        	if((i + 1) < value_list.length) {
		        		value.append("|");
		        	}
		        }
		        compare.setName(target_test_name.substring(target_test_name.indexOf("~") + 1));
		        compare.setValue(value.toString());
		        //MEMEToolkit.logComment("Add to cache " + target + "|" + compare.getName() + "|" + compare.getValue() + "|" + compare.getCount() + "|" + compare.getComparisonCount()  , true);
		        src_qacompare_cache.put(getDataSourceName(),target + compare.getName() + compare.getValue(), compare);
	    	}
    	}
      }
      rs = stmt.executeQuery("SELECT name,value,sum(qa_count) qa_count FROM qa_adjustment where timestamp > (select min(timestamp) from molecular_actions) GROUP BY name,value");
      while (rs.next()) {
          String name = rs.getString("name");
          String target_test_name = getValueByCode("test_name_conversion",name);
          String target = target_test_name.substring(0, target_test_name.indexOf("~"));
          String value = rs.getString("value");
          String[] name_list = FieldedStringTokenizer.split(name,"_");
          String[] value_list = FieldedStringTokenizer.split(value,",");
          for(int i=0; i< value_list.length; i++) {
          	if(name_list[i].equals("sab")) {
          		if(sources_map.containsKey(value_list[i]))
          			value_list[i] = (String)sources_map.get(value_list[i]);
          	}
          	if(name_list[i].startsWith("stype")) {
          		if(stype_map.containsKey(name_list[i], value_list[i])) {
          			value_list[i] = (String)stype_map.get(name_list[i], value_list[i]);
          		}
          	}
          	if(name_list[i].equals("rel")) {
          		if(rel_map.containsKey(value_list[i]))
          			value_list[i] = (String)rel_map.get(value_list[i]);
          	}
          }
          StringBuffer sb_value = new StringBuffer();
          for(int i=0; i< value_list.length; i++) {
        	  sb_value.append(value_list[i]);
          	if((i + 1) < value_list.length) {
          		sb_value.append("|");
          	}
          }
          String test_name = target_test_name.substring(target_test_name.indexOf("~") + 1);
	      //MEMEToolkit.logComment("Add to qa_adjustment_cache " + target + test_name + sb_value.toString()  , true);
          qa_adjustment_cache.put(getDataSourceName(),target + test_name + sb_value.toString(), new Long(rs.getLong("qa_count")));
        }

      // Close statement
      stmt.close();
      pstmt.close();

    }
    catch (SQLException se) {
      DataSourceException dse = new DataSourceException(
          "Failed to look up code_map.", query, se);
      dse.setDetail("query", query);
      throw dse;
    }
  }

  /* (non-Javadoc)
   * @see gov.nih.nlm.mrd.sql.MRDDataSource#getSRCQAReason(java.lang.String, gov.nih.nlm.mrd.common.QAComparison)
   */
  public QAReason getSRCQAReason(String target, QAComparison compare) throws DataSourceException {
	  return (QAReason)qa_compare_reason_cache.get(target,compare);
  }

  /* (non-Javadoc)
   * @see gov.nih.nlm.mrd.sql.MRDDataSource#getSRCQAReason(java.lang.String, gov.nih.nlm.mrd.common.QAComparison)
   */
  public QAReason getSRCQAReason(String target, String release, String previous_release, QAResult result) throws DataSourceException {
	  QAComparison compare = new QAComparison();
	  compare.setName(result.getName());
	  compare.setValue(result.getValue());
	  if(release.compareTo(previous_release) > 0) {
		  compare.setCount(result.getCount());
		  compare.setComparisonCount(0);
	  } else {
		  compare.setCount(0);
		  compare.setComparisonCount(result.getCount());
	  }
	  //MEMEToolkit.logComment("getSRCQAReason " + compare.getName(), true);
	  //MEMEToolkit.logComment("getSRCQAReason " + compare.getValue(), true);
	  //MEMEToolkit.logComment("getSRCQAReason " + (qa_compare_reason_cache.get(target,compare) == null ? "not found in cache" : "in cache"), true);
return (QAReason)qa_compare_reason_cache.get(target,compare);
  }
  /**
   * Determines the query used to read concept.
   * @param ticket the {@link Ticket}.
   * @return the query.
   */
  private String getConceptsQuery(Ticket ticket) {

    // Use empty ticket if not ticket is specified
    if (ticket == null) {
      ticket = Ticket.getEmptyTicket();

    }
    return READ_MRD_CUI;
    
  }
  
  
  
  public MRDConcept getConcept(String cui) throws DataSourceException,
  BadValueException {
  //
  // Use empty ticket if not ticket is specified
  //
  MRDConcept concept = new MRDConcept();
  Ticket ticket = null;
  ticket = Ticket.getReportsTicket();
  //
  // Read Concept Information
  //
  PreparedStatement pstmt = null;
  Map cd_map = new HashMap();
  int row_count = 0;
  int concept_id = 0;
  
  if (ticket.readConcept()) {
    String query = getConceptsQuery(ticket);
    try {
      pstmt = prepareStatement(query);
      pstmt.setString(1, cui);
      ResultSet rs = pstmt.executeQuery();

      row_count = 0;
      MRDConceptMapper default_mapper = new MRDConceptMapper.Default();
      // Read
      while (rs.next()) {
          default_mapper.populate(rs, this, concept);
          concept.setConceptId(rs.getInt("concept_id"));
          concept_id = rs.getInt("concept_id");
        //
        // If reading actions, get the approval action
        //
        if (ticket.readActions()) {
          int molecule_id = rs.getInt("APPROVAL_MOLECULE_ID");
          MolecularAction action = getMolecularAction(molecule_id);
          if (action == null) {
            action = new MolecularAction(molecule_id);
          }
         // concept.setApprovalAction(action);
        }

        row_count++;
      }

      // Close statement
      pstmt.close();

      //
      // There should only have exactly one concept row
      //
      if (row_count == 0) {
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("concept_id", Integer.toString(concept_id));
        throw dse;
      }
      if (row_count > 1) {
        DataSourceException dse = new DataSourceException(
            "Too much data.");
        dse.setDetail("concept_id", Integer.toString(concept_id));
        throw dse;
      }
      
      // set the CUI Name
      concept.setCuiName(getCUIName(cui));
    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load concept.", concept, se);
      dse.setDetail("query", query);
      dse.setDetail("concept_id", Integer.toString(concept_id));
      throw dse;
    }
  }

  //
  // Read Atoms
  //
  if (ticket.readAtoms()) {
    MRDAtom atom = null;
    try {
      //
      // Query to check if there are any ambiguous atoms.
      //
      String tmp_ambig_query = "SELECT COUNT(*) AS CT " +
          "FROM classes WHERE isui = ?" +
          " AND concept_id != ?" +
          " AND tobereleased IN ('Y','y')";
      final String ambig_query = tmp_ambig_query +
          " AND (source != 'MTH' OR termgroup NOT LIKE '%PN')";
      final String pn_ambig_query = tmp_ambig_query +
          " AND source = 'MTH' AND termgroup LIKE '%PN'";
      PreparedStatement pn_ambig_pstmt = null;
      PreparedStatement no_pn_ambig_pstmt = null;
      PreparedStatement ambig_pstmt = null;

      //
      // Check ambiguity (if requested)
      //
      if (ticket.checkAtomAmbiguity()) {
        try {
          pn_ambig_pstmt = prepareStatement(pn_ambig_query);
          no_pn_ambig_pstmt = prepareStatement(ambig_query);
        }
        catch (SQLException se) {
          DataSourceException dse = new DataSourceException(
              "Failed to prepare statement.", atom, se);
          dse.setDetail("query1", pn_ambig_query);
          dse.setDetail("query2", ambig_query);
          throw dse;
        }
      }

      //
      // Prepare and execute READ_ATOMS or READ_ATOMS_WITH_NAME query
      //
      pstmt = prepareStatement(READ_AUIS_WITH_NAME);
      pstmt.setInt(1, concept_id);
      ResultSet rs = pstmt.executeQuery();

      //
      // Read atoms
      //
      row_count = 0;

      MRDAtomMapper default_mapper = new MRDAtomMapper.Default();
      while (rs.next()) {
        //
        // Create and populate atom
        atom = default_mapper.map(rs, this);
        atom.setString(rs.getString("STRING"));
        //
        // Determine if the atom is ambiguous
        //

        if (ticket.checkAtomAmbiguity()) {
          if (atom.getTermgroup().toString().equals("MTH/PN")) {
            ambig_pstmt = pn_ambig_pstmt;
          }
          else {
            ambig_pstmt = no_pn_ambig_pstmt;

          }
          try {
            ambig_pstmt.setString(1, rs.getString("ISUI"));
            ambig_pstmt.setInt(2, concept_id);
            ResultSet ambig_rs = ambig_pstmt.executeQuery();
            while (ambig_rs.next()) {
              if (ambig_rs.getInt("CT") > 0) {
                atom.setIsAmbiguous(true);
              }
              else {
                atom.setIsAmbiguous(false);
              }
            }
          }
          catch (SQLException se) {
            DataSourceException dse = new DataSourceException(
                "Failed to determine if atom is ambiguous.", atom, se);
            if (atom.getTermgroup().toString().equals("MTH/PN")) {
              dse.setDetail("query", pn_ambig_query);
            }
            else {
              dse.setDetail("query", ambig_query);
            }
            dse.setDetail("concept_id",   Integer.toString(concept.getConceptId()));
            throw dse;
          }
        }
        //
        // Connect atom and concept
        //
        atom.setConcept(concept);
        concept.addAtom(atom);

        //
        // Hash atom for later use
        //
        cd_map.put(atom.getAUI(), atom.getString());

        row_count++;
      }

      //
      // Close statement
      //
      pstmt.close();

      //
      // Only if we want to check ambiguity of atoms
      //
      if (ticket.checkAtomAmbiguity()) {
        pn_ambig_pstmt.close();
        no_pn_ambig_pstmt.close();
      }

      //
      // There should be more than 0 rows
      //
      if (row_count < 1) {
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("concept_id",   concept.getConceptId());
        throw dse;
      }

      //
      // Compute preferred atom of concept
      //
      if (ticket.readConcept() && ticket.calculatePreferredAtom()) {
        MRDAtom[] atoms = concept.getAtoms();
        //Arrays.sort(atoms);
        concept.setPreferredAtom(atoms[atoms.length - 1]);
      }

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load atom.", atom, se);
      dse.setDetail("query", READ_AUIS_WITH_NAME);
      dse.setDetail("concept_id",   concept.getConceptId());
      throw dse;
    }
  }

  if (ticket.readRelationships()) {
	    //
	    // Determine queries to use (must use 2 queries)
	    //
	    boolean inverse = false;
	    int count = 0;
	    //
	    // Iterate through queries
	    //
        for (int i = 0; i < 2; i++) {
          try {
	    	 if (!inverse)
	    		 pstmt = prepareStatement(READ_RELATIONSHIPS);
	    	 else
	    		 pstmt = prepareStatement(READ_INVERSE_RELATIONSHIPS);
	    	 
	    	  pstmt.setFetchSize(1000);
	    	  pstmt.setString(1, cui);
	    	  ResultSet rs = pstmt.executeQuery();
              MRDRelationshipMapper relMapper = new MRDRelationshipMapper.Default();
	    	  while (rs.next()) {
	    	 	 count++; 	 		
	    	 	 MRDRelationship rel = relMapper.map(rs, this,cd_map);
	    	 	 
	             if (inverse)
	              	 rel.setInverse_flag(inverse);
	   	         cd_map.put(rel.getRui(), rel);
	   	         concept.addRelationship(rel);
	          }
	        // Close statement
	    	 	 pstmt.close();
	        // Set the inverse flag, this is
	        // because we will reverse the
	        // rel and rela for the next query
	        inverse = true;

	      }
	      catch (SQLException se) {
	        try {
	          pstmt.close();
	        }
	        catch (Exception e) {}
	        DataSourceException dse = new DataSourceException(
	            "Failed to load relationship.", concept, se);
	        dse.setDetail("query",READ_RELATIONSHIPS);
	        dse.setDetail("CUI", cui);
	        throw dse;
	      }
	    } // End for
	  }
  
  // Read the Context Relationships for each atom Context Relationships always belong to an atom
  MRDContextRelationshipMapper cxtMapper = new MRDContextRelationshipMapper.Default();
  MRDAtom[] atoms = concept.getAtoms();
  int total_atoms = atoms.length;
  MEMEToolkit.logComment("Total Number of Elements " + total_atoms);
  for (int i =0; i < atoms.length; i++) {
	  MRDAtom atom = atoms[i];
	  String aui = atom.getAUI();
	  try {
		  pstmt = prepareStatement(READ_CONTEXT_RELATIONSHIPS);
	 	 
	 	  pstmt.setFetchSize(1000);
	 	  pstmt.setString(1, aui);
	 	  ResultSet rs = pstmt.executeQuery();
	 	  while (rs.next()) {
	 		  MRDContextRelationship cxt = cxtMapper.map(rs,this,cd_map);
	 		  concept.addContextRelationship(cxt);
//	 		  if (cxt != null) {
//	 			  MEMEToolkit.logComment("Adding the CXT " + cxt.getRui());
//	 		  }
	 	  }
	 	  pstmt.close();
	  } catch (SQLException se) {
	        try {
		          pstmt.close();
		        }
		        catch (Exception e) {}
	        DataSourceException dse = new DataSourceException(
	            "Failed to load CONTEXT relationship.", concept, se);
	        dse.setDetail("query",READ_CONTEXT_RELATIONSHIPS);
	        dse.setDetail("CUI", cui);
	        throw dse;
	 }
  }
  /* Read all the attributes
   * 
   */
  try {
	  pstmt = prepareStatement(READ_MRD_ATTRIBUTE);
	  pstmt.setFetchSize(1000);
	  pstmt.setString(1, cui);
	  ResultSet rs = pstmt.executeQuery();
	  MRDAttributeMapper attrMapper = new MRDAttributeMapper.Default();
	  while (rs.next()) {
		  MRDAttribute attr = attrMapper.map(rs,this);
//		  MEMEToolkit.logComment("the attr is " + attr.getAName());
		  concept.addAttribute(attr);
	  }
  } catch (SQLException se) {
      try {
          pstmt.close();
        }
        catch (Exception e) {}
    DataSourceException dse = new DataSourceException(
        "Failed to load attributes.", concept, se);
    dse.setDetail("query",READ_MRD_ATTRIBUTE);
    dse.setDetail("CUI", cui);
    throw dse;
}
  return concept;
  }


  /*******************************************************************************************************
  //
  // Read relationships
  //
  AtomMapper pref_atom_mapper = new PreferredAtomMapper();
  if (ticket.readRelationships()) {

    //
    // Determine queries to use (must use 2 queries)
    //
    boolean inverse = false;
    int count = 0;
    int start = ticket.getDataTypeStart(Relationship.class);
    int end = ticket.getDataTypeEnd(Relationship.class);
    boolean using_count = start != -1;

    //
    // Iterate through queries
    //

    for (int i = 0; i < 2; i++) {

      try {
        //
        // Prepare and execute queries
        //
        pstmt = prepareStatement(
            getRelationshipsQuery(ticket, Concept.class, inverse));
        pstmt.setFetchSize(1000);
        pstmt.setInt(1, concept_id);
        ResultSet rs = pstmt.executeQuery();

        //
        // Read data
        //
        RelationshipMapper[] rel_mappers =
            (RelationshipMapper[]) (ticket.getDataMapper(Relationship.class)).
            toArray(new RelationshipMapper[0]);
        for (int j = 0; j < rel_mappers.length; j++) {
          rel_mappers[j].setConcept(concept);
          rel_mappers[j].setCoreDataMap(cd_map);
        }
        RelationshipMapper rel_mapper = new RelationshipMapper.Default(true);
        rel_mapper.setConcept(concept);
        rel_mapper.setCoreDataMap(cd_map);

        while (rs.next()) {
          count++;

          //
          // Skip excluded language relationship
          //
          if (!rs.getString("RELATIONSHIP_LEVEL").equals("C") &&
              excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_1")))) {
            continue;
          }

          if (!rs.getString("RELATIONSHIP_LEVEL").equals("C") &&
              excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_2")))) {
            continue;
          }

          //
          // If reading only a range, continue if index is in range
          //

          if (using_count && count <= start) {
            continue;
          }
          if (using_count && count > end) {
            break;
          }

          Relationship rel = null;

          //
          //  Construct and populate relationship
          //
          boolean found = false;
          if (ticket.mapDataType(Relationship.class)) {
            for (int j = 0; j < rel_mappers.length; j++) {
              if ( (rel = rel_mappers[j].map(rs, this)) != null) {
                found = true;
                break;
              }
            }
          }
          //
          // If relationship was not populated, use default mapper
          //
          if (!found) {
            rel = rel_mapper.map(rs, this);

            //
            // If reading inverse rels, inverse sense of rel and rela
            //
          }
          if (inverse) {
            rel.setName(
                getInverseRelationshipName(rel.getName()));
            rel.setAttribute(
                getInverseRelationshipAttribute(rel.getAttribute()));
          }

          rel.setIsInverse(inverse);

          //
          // If we are expanding an entire graph just recursively call getConcept
          //
          if (ticket.expandGraph()) {
            int concept_id_2 = rs.getInt("CONCEPT_ID_2");
            ticket.decrementRecursiveLevel();
            getConcept(concept_id_2, ticket);
            ticket.incrementRecursiveLevel();
          }

          //
          // If we are just expanding the name, look up the
          // preferred atom on the other side.
          //
          else if (ticket.readRelationshipNames()) {
            int concept_id_2 = rs.getInt("CONCEPT_ID_2");
            Concept rc = new Concept.Default(concept_id_2);
            if (rs.getString("STRING") != null) {
              rc.setPreferredAtom(pref_atom_mapper.map(rs, this));
            }
            else {
              try {
                rc.setPreferredAtom(getPreferredAtom(rc));
              }
              catch (MissingDataException mde) {
                Atom epa = new Atom.Default( -1);
                epa.setLUI(new LUI("L0000000"));
                epa.setSUI(new SUI("S0000000"));
                epa.setISUI(new ISUI("I0000000"));
                epa.setConcept(rc);
                epa.setCode(new Code("NOCODE"));
                epa.setSource(getSource("MTH"));
                epa.setTermgroup(getTermgroup("MTH/PT"));
                epa.setString("This concept has no preferred atom.");
                rc.setPreferredAtom(epa);
              }
            }
            rel.setRelatedConcept(rc);
          }

          //
          // If we are expanding names then get them for all SFO/LFO
          // (at this point, we know that the sfo/lfo is not self-referential)
          //
          if ( (ticket.readRelationshipNames() &&
                rel.getName().equals("SFO/LFO"))) {
            int atom_id_2 = rs.getInt("ATOM_ID_2");
            Ticket l_ticket = Ticket.getEmptyTicket();
            Atom atom = getAtomWithName(atom_id_2, l_ticket);
            rel.setRelatedAtom(atom);
          }

          //
          // Hash rel for later use
          //
          cd_map.put("R" + rel.getIdentifier(), rel);

          //
          // Connect atom and rel
          //
          if (rel.isAtomLevel() && rel.getAtom() != null) {
            rel.getAtom().addRelationship(rel);

          }
        } // End while

        // Close statement
        pstmt.close();

        // Set the inverse flag, this is
        // because we will reverse the
        // rel and rela for the next query
        inverse = true;

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load relationship.", concept, se);
        dse.setDetail("query",
                      getRelationshipsQuery(ticket, Concept.class, inverse));
        dse.setDetail("concept_id", Integer.toString(concept_id));
        throw dse;
      }
    } // End for
  }

  //
  // Read context relationships
  //
  if (ticket.readContextRelationships()) {

    //
    // Determine context rel queries
    //
    boolean inverse = false;
    int count = 0;
    int start = ticket.getDataTypeStart(Relationship.class);
    int end = ticket.getDataTypeEnd(Relationship.class);
    boolean using_count = start != -1;

    //
    // Iterate over queries
    //
    for (int i = 0; i < 2; i++) {

      try {
        //
        // Prepare and execute queries
        //
        pstmt = prepareStatement(
            getContextRelationshipsQuery(ticket, Concept.class, inverse));
        pstmt.setInt(1, concept_id);
        ResultSet rs = pstmt.executeQuery();

        //
        // Read data
        //
        RelationshipMapper[] rel_mappers =
            (RelationshipMapper[]) (ticket.getDataMapper(ContextRelationship.class)).
            toArray(new RelationshipMapper[0]);
        for (int j = 0; j < rel_mappers.length; j++) {
          rel_mappers[j].setConcept(concept);
          rel_mappers[j].setCoreDataMap(cd_map);
        }
        RelationshipMapper rel_mapper = new ContextRelationshipMapper(true);
        rel_mapper.setConcept(concept);
        rel_mapper.setCoreDataMap(cd_map);
        while (rs.next()) {
          count++;

          //
          // Skip excluded language relationship
          //
          if (!rs.getString("RELATIONSHIP_LEVEL").equals("C") &&
              excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_1")))) {
            continue;
          }

          if (!rs.getString("RELATIONSHIP_LEVEL").equals("C") &&
              excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID_2")))) {
            continue;
          }

          //
          // If reading only a range, continue if index is in range
          //

          if (using_count && count <= start) {
            continue;
          }
          if (using_count && count > end) {
            break;
          }

          Relationship cxt_rel = null;

          //
          //  Construct and map context rels
          //
          boolean found = false;
          if (ticket.mapDataType(Relationship.class)) {
            for (int j = 0; j < rel_mappers.length; j++) {
              if ( (cxt_rel = rel_mappers[j].map(rs, this)) != null) {
                found = true;
                break;
              }
            }
          }
          //
          // If relationship was not populated, use default mapper
          //
          if (!found) {
            cxt_rel = rel_mapper.map(rs, this);

            // inverse sense of rel and rela
          }
          if (inverse) {
            cxt_rel.setName(
                getInverseRelationshipName(cxt_rel.getName()));
            cxt_rel.setAttribute(
                getInverseRelationshipAttribute(cxt_rel.getAttribute()));
          }

          //
          // If we are reading relationship names
          // then just get fields from the query.
          //
          if (ticket.readRelationshipNames()) {
            Atom atom = pref_atom_mapper.map(rs, this);
            cxt_rel.setRelatedAtom(atom);
            cxt_rel.setRelatedConcept(new Concept.Default(rs.getInt(
                "CONCEPT_ID_2")));
            cxt_rel.getRelatedConcept().setPreferredAtom(atom);
          }

          //
          // Hash context rel for later use
          //
          cd_map.put("R" + cxt_rel.getIdentifier(), cxt_rel);

          //
          // Connect atom and rel
          //
          if (cxt_rel.isAtomLevel() && cxt_rel.getAtom() != null) {
            cxt_rel.getAtom().addContextRelationship( (ContextRelationship)
                cxt_rel);

          }
        } // End while

        //
        // Close statement
        //
        pstmt.close();

        //
        // Set the inverse flag
        //
        inverse = true;

      }
      catch (SQLException se) {
        try {
          pstmt.close();
        }
        catch (Exception e) {}
        DataSourceException dse = new DataSourceException(
            "Failed to load context relationship.", concept, se);
        dse.setDetail("query",
                      getContextRelationshipsQuery(ticket, Concept.class,
            inverse));
        dse.setDetail("concept_id", Integer.toString(concept_id));
        throw dse;
      }

    } // End for
  }

  //
  // Read attributes
  //
  if (ticket.readAttributes()) {

    int count = 0;
    int start = ticket.getDataTypeStart(Attribute.class);
    int end = ticket.getDataTypeEnd(Attribute.class);
    boolean using_count = start != -1;

    try {
      //
      // Prepare and execute READ_ATTRIBUTES query
      //
      pstmt = prepareStatement(getAttributesQuery(ticket, Concept.class));
      pstmt.setInt(1, concept_id);
      ResultSet rs = pstmt.executeQuery();

      //
      // Read data
      //
      AttributeMapper[] mappers = (AttributeMapper[]) (ticket.getDataMapper(
          Attribute.class)).toArray(new AttributeMapper[0]);
      for (int i = 0; i < mappers.length; i++) {
        mappers[i].setConcept(concept);
        mappers[i].setCoreDataMap(cd_map);
      }
      AttributeMapper default_mapper = new AttributeMapper.Default(true);
      default_mapper.setConcept(concept);
      default_mapper.setCoreDataMap(cd_map);
      LongAttributeMapper long_attribute_mapper = new LongAttributeMapper(this);
      while (rs.next()) {
        count++;

        //
        // Skip excluded language attribute
        //
        if (rs.getString("ATTRIBUTE_LEVEL").equals("S") &&
            excluded_atoms.contains(new Integer(rs.getInt("ATOM_ID")))) {
          continue;
        }

        //
        // If reading only a range, continue if index is in range
        //

        if (using_count && count <= start) {
          continue;
        }
        if (using_count && count > end) {
          break;
        }

        //
        // Create and populate attribute
        //
        Attribute attr = null;
        boolean found = false;
        if (ticket.mapDataType(Attribute.class)) {
          for (int i = 0; i < mappers.length; i++) {
            if ( (attr = mappers[i].map(rs, this)) != null) {
              found = true;
              break;
            }
          }
        }
        //
        // If attribute was not populated, use default mapper
        //
        if (!found) {
          attr = default_mapper.map(rs, this);

          //
          // If the attribute is a long attribute, get the full value
          //
        }
        if (attr.getValue() != null &&
            attr.getValue().startsWith("<>Long_Attribute<>:") &&
            ticket.expandLongAttributes()) {
          try {
            long_attribute_mapper.populate(rs, this, attr);
          }
          catch (SQLException se) {
            long_attribute_mapper.close();
            DataSourceException dse = new DataSourceException(
                "Failed to load long attribute.", attr, se);
            throw dse;
          }
        }

        //
        // Connect atom and attribute
        //
        if (attr.isAtomLevel() && attr.getAtom() != null) {
          attr.getAtom().addAttribute(attr);

        }
      }

      //
      // Close statements
      //
      long_attribute_mapper.close();
      pstmt.close();

    }
    catch (SQLException se) {
      try {
        pstmt.close();
      }
      catch (Exception e) {}
      DataSourceException dse = new DataSourceException(
          "Failed to load the attribute.", concept, se);
      dse.setDetail("query", getAttributesQuery(ticket, Concept.class));
      dse.setDetail("concept_id_id", Integer.toString(concept_id));
      throw dse;
    }
  }
  
*****************************************************************************************************/

public String getAUIName(String aui)  throws DataSourceException, BadValueException {
	String auiName = "";
	PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(READ_ATOM_NAME);
      pstmt.setString(1, aui);
      
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
    	  auiName = rs.getString("STRING");
      }
      pstmt.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to add qa result reason", this, e);
      dse.setDetail("insert", pstmt.toString());
      throw dse;
    }
    return auiName;
}

public String getCUIName(String cui)  throws DataSourceException, BadValueException {
	String auiName = "";
	PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(READ_PREFERRED_ATOM_NAME);
      pstmt.setString(1, cui);
      
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
    	  auiName = rs.getString("STRING");
      }
      pstmt.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to add qa result reason", this, e);
      dse.setDetail("insert", pstmt.toString());
      throw dse;
    }
    return auiName;
}

public MRDAtom[] getMRDAtomsForCode(String code, int size) throws DataSourceException, BadValueException {
	ArrayList<MRDAtom> atomArray = new ArrayList<MRDAtom>();
	MRDAtom[] mrdAtoms = null;
//	int size = 10001;
	PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(READ_AUIS_WITH_NAME_FOR_CODE);
      pstmt.setString(1, code);
      pstmt.setInt(2, size);
      int row_count = 0;
      ResultSet rs = pstmt.executeQuery();
      
      while (rs.next()) {
        // Create and populate atom
    	MRDAtom atom = new MRDAtom();
    	atom.setAUI(rs.getString("AUI"));
        atom.setString(rs.getString("STRING"));
        atom.setSource(getSource_MRDConceptReport(rs.getString("ROOT_SOURCE")));
        atom.setTty(rs.getString("TTY"));
        atomArray.add(atom);
        row_count++;
      } 
      pstmt.close();
      if (row_count == 0) {
    	  DataSourceException dse = new DataSourceException("Missing or no Data found for the code [" + code + "]", this,null);
    	  throw dse;
      }
      mrdAtoms =  atomArray.toArray(new MRDAtom[] {});
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to get the AUI for code " + code, this, e);
      dse.setDetail("insert", pstmt.toString());
      throw dse;
    }
    return mrdAtoms;
}

public MRDAtom[] getMRDAtomsForTty(String tty, String source, int size) throws DataSourceException,BadValueException {
	
		ArrayList<MRDAtom> atomArray = new ArrayList<MRDAtom>();
		MRDAtom[] mrdAtoms = null;
		PreparedStatement pstmt = null;
//		int size = 10001;
		
	    try {
	    	if(source != null && source.trim().length() > 0)
	    	{
	    		pstmt = prepareStatement(READ_AUIS_FOR_TTY_SOURCE);
	    		pstmt.setString(1, tty);
	    		pstmt.setString(2, source);
	    		pstmt.setInt(3, size);
	    	}
	    	else
	    	{
	    		pstmt = prepareStatement(READ_AUIS_FOR_TTY);
	    		pstmt.setString(1, tty);
	    		pstmt.setInt(2, size);
	    	}

	      int row_count = 0;
	      ResultSet rs = pstmt.executeQuery();


	      while (rs.next()) {
	        // Create and populate atom
	    	MRDAtom atom = new MRDAtom();
	    	atom.setAUI(rs.getString("AUI"));
	    	atom.setCUI(rs.getString("CUI"));
	    	atom.setSource(getSource_MRDConceptReport(rs.getString("ROOT_SOURCE")));
	    	atom.setCode(rs.getString("CODE"));
	        atom.setString(rs.getString("STRING"));
	        atomArray.add(atom);
	        row_count++;
	      } 
	      pstmt.close();
	      if (row_count == 0) {
	    	  DataSourceException dse = new DataSourceException("Missing or no Data found for the code [" + tty + "]", this,null);
	    	  throw dse;
	      }
	      mrdAtoms =  atomArray.toArray(new MRDAtom[] {});
	      
	      //check the data- debugging code
//	      for(MRDAtom a : mrdAtoms)
//	      {
//	    	  MEMEToolkit.logComment("\t\t *** the aui is " + a.getAUI());
//	    	  MEMEToolkit.logComment("\t\t *** the cui is " + a.getCUI());
//	    	  MEMEToolkit.logComment("\t\t *** the source is " + a.getSource());
//	    	  MEMEToolkit.logComment("\t\t *** the string is " + a.getString());
//	    	  
//	      }
	    }
	    catch (SQLException e) {
	      e.printStackTrace();
	      DataSourceException dse = new DataSourceException(
	          "Failed to get the AUI for code " + tty, this, e);
	      dse.setDetail("insert", pstmt.toString());
	      throw dse;
	    }
	    return mrdAtoms;
}


public MRDRelationship[] getMRDRelationsForRelName(String rel, String rela, String source, int size) 
throws DataSourceException,BadValueException {
	long start = System.nanoTime();
//	int size = 10001;
	ArrayList<MRDRelationship> relsArray = new ArrayList<MRDRelationship>();
	source = (source != null && source.trim().length() > 0) ? source : null;
	rela = (rela != null && rela.trim().length() > 0) ? rela : null;

	MRDRelationship[] mrdRels = null;
	PreparedStatement pstmt = null;
    try {
      if (source != null && rela != null)
      {
         pstmt = prepareStatement(READ_RUIS_FOR_REL_RELA_SOURCE);
         pstmt.setString(1, rel);
         pstmt.setString(2, rela);
         pstmt.setString(3, source);
         pstmt.setInt(4, size);
      }
      else if (source != null && rela == null)
      {
     	  pstmt = prepareStatement(READ_RUIS_FOR_REL_SOURCE);
          pstmt.setString(1, rel);
          pstmt.setString(2, source);
          pstmt.setInt(3, size);
      }
      else if (source == null && rela != null)
      {
     	  pstmt = prepareStatement(READ_RUIS_FOR_REL_RELA);
          pstmt.setString(1, rel);
          pstmt.setString(2, rela);
          pstmt.setInt(3, size);
      }
      else
      {
         pstmt = prepareStatement(READ_RUIS_FOR_REL);
         pstmt.setString(1, rel);
         pstmt.setInt(2, size);
      }
           

      int row_count = 0;
      ResultSet rs = pstmt.executeQuery();
      MRDRelationshipMapper default_mapper = new MRDRelationshipMapper.Default();
      
      while (rs.next()) {
        //
        // Create and populate atom
        MRDRelationship thisRel = default_mapper.map(rs, this, null);
        relsArray.add(thisRel);
        row_count++;
      } 
      pstmt.close();
      if (row_count == 0) {
    	  DataSourceException dse = new DataSourceException("Missing or no Data found for the code [" + rel + "]", this,null);
    	  throw dse;
      }
      mrdRels =  relsArray.toArray(new MRDRelationship[] {});
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to get the AUI for code " + rel, this, e);
      dse.setDetail("insert", pstmt.toString());
      throw dse;
    }
    catch (Exception e) {
        e.printStackTrace();
        DataSourceException dse = new DataSourceException(
            "Failed to get the AUI for code " + rel, this, e);
        dse.setDetail("insert", pstmt.toString());
        throw dse;
    }
    // debug code
//    for (MRDRelationship rel1 : mrdRels)
//    {
//    	MEMEToolkit.logComment(" \t\t *********Relationship_NAME********************\t ");
//    	MEMEToolkit.logComment(" the rui is " + rel1.getRui());
//    	MEMEToolkit.logComment(" the relationship name is " + rel1.getRelationship_name());
//    	MEMEToolkit.logComment(" the relationship attribute is " + rel1.getRelationship_attribute());
//    }

    long end = System.nanoTime();
    
    return mrdRels;
}

public MRDRelationship[] getMRDRelationsForRelAttribute(String rela, String rel, String source, int size) throws DataSourceException,BadValueException {
	ArrayList<MRDRelationship> relsArray = new ArrayList<MRDRelationship>();
	rel = (rel != null && rel.trim().length() > 0) ? rel : null;
	source = (source != null && source.trim().length() > 0) ? source : null;
	MRDRelationship[] mrdRels = null;
	PreparedStatement pstmt = null;
    try {
    	if(rel != null && source !=null)
    	{
    		pstmt = prepareStatement(READ_RUIS_FOR_REL_RELA_SOURCE);
    		pstmt.setString(1, rel);
    		pstmt.setString(2, rela);
    		pstmt.setString(3, source);
    		pstmt.setInt(4, size);
    	}
    	else if (rel != null && source ==null)
    	{
    		pstmt = prepareStatement(READ_RUIS_FOR_REL_RELA);
    		pstmt.setString(1, rel);
    		pstmt.setString(2, rela);
    		pstmt.setInt(3, size);
    	}
    	else if (rel != null && source !=null)
    	{
    		pstmt = prepareStatement(READ_RUIS_FOR_RELA_SOURCE);
    		pstmt.setString(1, rela);
    		pstmt.setString(2, source);
    		pstmt.setInt(3, size);
    	}
    	else
    	{
    	      pstmt = prepareStatement(READ_RUIS_FOR_RELA);
    	      pstmt.setString(1, rela);
    	      pstmt.setInt(2, size);
    	}
    	
      int row_count = 0;
      ResultSet rs = pstmt.executeQuery();
      MRDRelationshipMapper default_mapper = new MRDRelationshipMapper.Default();
      
      while (rs.next()) {
        //
        // Create and populate atom
        MRDRelationship thisRel = default_mapper.map(rs, this, null);
        relsArray.add(thisRel);
        row_count++;
      } 
      pstmt.close();
      if (row_count == 0) {
    	  DataSourceException dse = new DataSourceException("Missing or no Data found for the code [" + rela + "]", this,null);
    	  throw dse;
      }
      mrdRels =  relsArray.toArray(new MRDRelationship[] {});
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to get the AUI for code " + rela, this, e);
      dse.setDetail("insert", pstmt.toString());
      throw dse;
    }
    //debug code
//    for (MRDRelationship rel : mrdRels)
//    {
//    	MEMEToolkit.logComment(" \t\t **********RELATIONSHIP_ATTRIBUTE*******************\t ");
//    	MEMEToolkit.logComment(" the rui is " + rel.getRui());
//    	MEMEToolkit.logComment(" the relationship name is " + rel.getRelationship_name());
//    	MEMEToolkit.logComment(" the relationship attribute is " + rel.getRelationship_attribute());
//    }
    
    return mrdRels;
}

	public String getCUIForAUI(String aui) throws DataSourceException,BadValueException {
	String cui = "";
	PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(READ_CUI_FOR_AUI);
      pstmt.setString(1, aui);
      
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
    	  cui = rs.getString("CUI");
      }
      pstmt.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to get the cui for AUI " + aui, this, e);
      dse.setDetail("insert", pstmt.toString());
      throw dse;
    }
    return cui;
}
	
public String getCUIForConceptId(int conceptId)  throws DataSourceException, BadValueException {
	String cui = "";
	PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(READ_CUI_FOR_CONCEPT_ID);
      pstmt.setInt(1, conceptId);
      
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
    	  cui = rs.getString("CUI");
      }
      pstmt.close();
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to get the cui for CONCEPTID " + conceptId, this, e);
      dse.setDetail("insert", pstmt.toString());
      throw dse;
    }
    return cui;
}
public String getLongAttributeValue(String hashCode)  throws DataSourceException, BadValueException {
	String auiName = "";
	PreparedStatement pstmt = null;
    try {
      pstmt = prepareStatement(READ_MRD_LONG_ATTRIBUTE);
      pstmt.setString(1, hashCode);
      ResultSet long_attribute_rs = pstmt.executeQuery();

      int row_count = 0;
      StringBuffer sb = new StringBuffer(1784);
      int text_total = 0;

      // Read
      while (long_attribute_rs.next()) {
        text_total = long_attribute_rs.getInt("TEXT_TOTAL");
        sb.append(long_attribute_rs.getString("TEXT_VALUE"));
        row_count++;
      }

      // The length of the string buffer should match the text total
      if (sb.length() != text_total) {
        //long_attribute_pstmt.close();
        DataSourceException dse = new DataSourceException(
            "Long attribute text value length does not match text_total.");
        //throw dse;
        dse.setFatal(false);
        dse.setInformAdministrator(true);
        dse.setInformUser(false);
        dse.setDetail("HashCode", hashCode);
        MEMEToolkit.handleError(dse);
      }

      // At least one row should've been read
      if (row_count < 1) {
        pstmt.close();
        MissingDataException dse = new MissingDataException("Missing data.");
        dse.setDetail("hashcode", hashCode);
        throw dse;
      }
      return sb.toString();
      // Set new attribute value
      
    }
    catch (SQLException e) {
      e.printStackTrace();
      DataSourceException dse = new DataSourceException(
          "Failed to add qa result reason", this, e);
      dse.setDetail("insert", pstmt.toString());
      throw dse;
    }
}

/**
 * Implements {@link MRDDataSource#getMRDSources()}.
 * @return An array of object {@link Source}.
 */
public Source[] getMRDSources() {
  Source[] sources = (Source[]) MRD_source_rank_cache.values(getDataSourceName()).
      toArray(new Source[0]);
  Arrays.sort(sources);
  return sources;
}


}
