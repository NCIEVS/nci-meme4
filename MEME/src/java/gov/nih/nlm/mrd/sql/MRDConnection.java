/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.sql
 * Object:  MRDConnection
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.action.LoggedAction;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Source;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.meme.sql.ActionEngine;
import gov.nih.nlm.meme.sql.MEMEConnection;
import gov.nih.nlm.meme.sql.MEMEDataSource;
import gov.nih.nlm.meme.sql.ResultSetIterator;
import gov.nih.nlm.meme.sql.ResultSetMapper;
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
import java.util.HashMap;

/**
 * Represents a connection to an MRD.
 *
 * @author  MEME Group
 */
public class MRDConnection extends MEMEConnection implements MRDDataSource {

  //
  // Cache of release history info
  //
  private HashMap release_history_cache = null;

  //
  // source type
  //
  private MultiMap source_type_cache = new MultiMap();;

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
}
