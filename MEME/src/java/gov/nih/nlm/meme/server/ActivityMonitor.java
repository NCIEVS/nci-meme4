/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  ActivityMonitor
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.DataSourceException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MissingDataException;
import gov.nih.nlm.meme.sql.MIDDataSource;
import gov.nih.nlm.meme.xml.CGIStyleMEMEServiceRequest;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import net.sourceforge.chart2d.Chart2D;
import net.sourceforge.chart2d.Chart2DProperties;
import net.sourceforge.chart2d.Dataset;
import net.sourceforge.chart2d.GraphChart2DProperties;
import net.sourceforge.chart2d.GraphProperties;
import net.sourceforge.chart2d.LBChart2D;
import net.sourceforge.chart2d.LegendProperties;
import net.sourceforge.chart2d.MultiColorsProperties;
import net.sourceforge.chart2d.Object2DProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Used to generate graphs of editing performance.  May not work anymore
 * as it requires browsers to connect directly.
 *
 * @author MEME Group
 */
public class ActivityMonitor implements MEMEApplicationService {

  //
  // Implementation of MEMEApplicationService interface
  //

  /**
   * Receives requests from the {@link MEMEApplicationServer}.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {

    MIDDataSource data_source = (MIDDataSource) context.getDataSource();
    OutputStream out =
        ( (CGIStyleMEMEServiceRequest) context.getServiceRequest()).
        getOutputStream();

    MEMEServiceRequest request = context.getServiceRequest();
    String function = null;
    if (request.getParameter("function") != null) {
      function = (String) request.getParameter("function").getValue();
    } else {
      throw new BadValueException("Illegal request: missing function parameter");
    }

    Chart2D chart = null;
    Object of_param = "";
    String by_param = "";
    int for_param = 30;
    BufferedImage image = null;

    try {
      if (request.getParameter("of_param") != null) {
        of_param = request.getParameter("of_param").getValue();
      }
      if (request.getParameter("by_param") != null) {
        by_param = (String) request.getParameter("by_param").getValue();
      }
      if (request.getParameter("for_param") != null) {
        for_param = Integer.parseInt( (String) request.getParameter("for_param").
                                     getValue());

      }
      if (function.equals("chart_for_count")) {
        chart = getChartForCount(of_param, by_param, for_param, data_source);
      }
      if (function.equals("chart_for_ratio")) {
        chart = getChartForRatio(of_param, by_param, for_param, data_source);
      }
      if (function.equals("chart_for_time")) {
        chart = getChartForTime(of_param, by_param, for_param, data_source);

      }
      image = chart.getImage();
    } catch (MissingDataException mde) {
      image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = (Graphics2D) image.getGraphics();
      g.setBackground(Color.WHITE);
      g.setColor(Color.BLACK);
      g.clearRect(0, 0, 300, 300);
      g.drawString(mde.getMessage(), 100, 100);
    }

    try {
      Writer x = new OutputStreamWriter(out);
      x.write("HTTP/1.1 200 OK\n");
      x.write("Expires: Fri, 20 Sep 1998 01:01:01 GMT\n");
      x.write("Content-Type: image/jpeg\n");
      x.write("Connection: close\n");
      x.write("\n");
      x.flush();
      JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
      JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
      param.setQuality(0.75f, false);
      encoder.setJPEGEncodeParam(param);
      encoder.encode(image);
      out.flush();
      out.close();
    } catch (IOException ioe) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to write to output stream.", ioe);
      throw ere;
    }
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean requiresSession() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isRunning() {
    return false;
  }

  /**
   * Returns <code>false</code>.
   * @return <code>false</code>
   */
  public boolean isReEntrant() {
    return false;
  }

  //
  // Private Methods
  //

  /**
   * Returns {@link Chart2D} for specified parameters.
   * @param of_param the "of" parameter
   * @param by_param the "by" parameter
   * @param for_param the "for" parameter
   * @param data_source the {@link MIDDataSource}
   * @return the {@link Chart2D}
   * @throws MEMEException if failed to get chart for count
   */
  private Chart2D getChartForCount(Object of_param, String by_param,
                                   int for_param, MIDDataSource data_source) throws
      MEMEException {

    //
    // Set DB parameters
    //
    data_source.setSortAreaSize(68000000);
    data_source.setHashAreaSize(68000000);

    String[] of_params = null;
    if (of_param instanceof String) {
      of_params = new String[] {
          (String) of_param};
    } else {
      of_params = (String[]) ( (List) of_param).toArray(new String[0]);

      // <-- Begin Chart2D configuration -->

      //
      // Configure object properties
      //
    }
    Object2DProperties object2DProps = new Object2DProperties();
    StringBuffer title = new StringBuffer();
    for (int i = 0; i < of_params.length; i++) {
      if (i > 0) {
        title.append(" ");
      }
      title.append(of_params[i]);
    }
    title.append(" By ").append(by_param);
    object2DProps.setObjectTitleText(title.toString());

    // Configure chart properties
    Chart2DProperties chart2DProps = new Chart2DProperties();
    chart2DProps.setChartDataLabelsPrecision(2);

    // Configure legend properties
    LegendProperties legendProps = new LegendProperties();
    legendProps.setLegendExistence(true);
    String[] legendLabels = of_params;
    legendProps.setLegendLabelsTexts(legendLabels);

    // Configure graph properties
    GraphProperties graphProps = new GraphProperties();
    if (by_param.equals("Hour") ||
        by_param.equals("Day") ||
        by_param.equals("Month")) {
      graphProps.setGraphBarsExistence(false);
      graphProps.setGraphLinesExistence(true);
    } else {
      graphProps.setGraphBarsExistence(true);
      graphProps.setGraphLinesExistence(false);
    }
    graphProps.setGraphOutlineComponentsExistence(false);
    graphProps.setGraphAllowComponentAlignment(true);

    // Configure graph component colors
    MultiColorsProperties multiColorsProps = new MultiColorsProperties();

    // Configure graph chart properties
    GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    graphChart2DProps.setLabelsAxisTicksAlignment(GraphChart2DProperties.CENTERED);

    List[] cat_datasets = new List[of_params.length];
    Map[] count_datasets = new Map[of_params.length];
    Comparator comp = null;

    for (int i = 0; i < of_params.length; i++) {

      // Map query
      String fields = "";
      String from = "";
      String join_table = "";
      String group_by = "";
      String having = "";
      StringBuffer and_condition = new StringBuffer();

      if (by_param.equals("Hour")) {
        comp = new HourEditorActionComparator();
        fields = ", COUNT(*) AS ct, hour as label, 'Hour' as title";
        from = "TO_CHAR(a.timestamp, 'HH24') AS hour";
        group_by = "hour";
      } else if (by_param.equals("Day")) {
        comp = new DayComparator();
        fields = ", COUNT(*) AS ct, day as label, month as title";
        from = " TO_CHAR(a.timestamp, 'DD') AS day," +
            " TO_CHAR(a.timestamp, 'Mon YYYY') AS month";
        group_by = "day, month";
      } else if (by_param.equals("Month")) {
        comp = new MonthComparator();
        fields = ", COUNT(*) AS ct, month as label, year as title";
        from = " TO_CHAR(a.timestamp, 'Mon') AS month, " +
            " TO_CHAR(a.timestamp, 'YYYY') AS year";
        group_by = "month, year";
      } else if (by_param.equals("Editor")) {
        comp = new HourEditorActionComparator();
        fields = ", COUNT(*) AS ct, editor as label, 'Editor' as title";
        from = " UPPER(a.authority) AS editor";
        group_by = "editor";
        and_condition.append(
            " AND (a.authority LIKE 'E-%' OR a.authority LIKE 'S-%')");
      } else if (by_param.equals("Action")) {
        comp = new HourEditorActionComparator();
        fields = ", COUNT(*) AS ct, action as label, 'Action' as title";
        from =
            " SUBSTR(molecular_action, INSTR(molecular_action, '_')+1) AS action";
        group_by = "action";
      } else if (by_param.equals("Source")) {
        comp = new HourEditorActionComparator();
        fields = ", COUNT(DISTINCT concept_id) as ct, " +
            " source as label, 'Source' as title";
        from = "source_id as concept_id," +
            " NVL((SELECT stripped_source FROM source_rank " +
            "      WHERE source IN (" +
            " DECODE(table_name, " +
            " 'C', (SELECT source FROM classes" +
            "    WHERE atom_id = row_id" +
            "    UNION" +
            "    SELECT source FROM dead_classes" +
            "    WHERE atom_id = row_id)," +
            " 'A', (SELECT source FROM attributes" +
            "    WHERE attribute_id = row_id" +
            "    UNION" +
            "    SELECT source FROM dead_attributes" +
            "    WHERE attribute_id = row_id)," +
            " 'R', (SELECT source FROM relationships" +
            "    WHERE relationship_id = row_id" +
            "    UNION" +
            "    SELECT source FROM dead_relationships" +
            "    WHERE relationship_id = row_id), 'X'))), 'MTH') as source";
        group_by = "source";
        join_table = ", atomic_actions b";
        and_condition.append(" AND a.molecule_id = b.molecule_id")
            .append(" AND b.table_name in ('C','A','R')");
        having = " HAVING COUNT(DISTINCT concept_id) > 4 * " + for_param +
            " AND source IN (SELECT source FROM source_version)";
      }

      if (for_param == 0) {
        and_condition.append(
            " AND a.timestamp > TO_DATE(TO_CHAR(sysdate, 'DD-Mon-YYYY'))");
      } else if (for_param > 1) {
        and_condition.append(" AND a.timestamp > TO_DATE(TO_CHAR(sysdate-")
            .append(for_param)
            .append(", 'DD-Mon-YYYY'))");

      }
      if (of_params[i].equals("Interface")) {
        and_condition.append(" AND a.authority LIKE 'E-%'");
      } else if (of_params[i].equals("Stamping")) {
        and_condition.append(" AND a.authority LIKE 'S-%'");
      } else if (of_params[i].equals("Approval")) {
        and_condition.append(
            " AND molecular_action = 'MOLECULAR_CONCEPT_APPROVAL'");
      } else if (of_params[i].equals("Merge")) {
        and_condition.append(" AND molecular_action = 'MOLECULAR_MERGE'");
      } else if (of_params[i].equals("Split")) {
        and_condition.append(" AND molecular_action = 'MOLECULAR_SPLIT'");
      } else if (of_params[i].equals("Insert")) {
        and_condition.append(" AND molecular_action = 'MOLECULAR_INSERT'");
      } else if (of_params[i].equals("Move")) {
        and_condition.append(" AND molecular_action = 'MOLECULAR_MOVE'");

        // Build up a query that searches for dataset fields rows
      }
      String query = "SELECT MIN(rownum)" + fields +
          " FROM (SELECT " + from +
          "  FROM molecular_actions a" + join_table +
          "  WHERE undone='N' " + and_condition.toString() +
          "  ORDER BY a.timestamp) a" +
          " GROUP BY " + group_by + having +
          " ORDER BY MIN(rownum)";

      MEMEToolkit.trace("QUERY: " + query);

      List cats = new ArrayList();
      Map counts = new HashMap();

      // Execute the query
      try {
        PreparedStatement pstmt = data_source.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        // Read
        while (rs.next()) {
          String label = rs.getString("LABEL");
          if (by_param.equals("Editor")) {
            label = rs.getString("LABEL").substring(2);
          }
          Category cat = new Category(label, rs.getString("TITLE"));
          cats.add(cat);
          counts.put(cat, new Integer(rs.getInt("CT")));
        }
        // Close statement
        pstmt.close();
      } catch (Exception e) {
        DataSourceException dse = new DataSourceException(
            "Failed to get dataset fields.", this, e);
        dse.setDetail("query", query);
        throw dse;
      }

      cat_datasets[i] = cats;
      count_datasets[i] = counts;

    } // End for over of_params

    // Configure dataset
    Set all_cats = new HashSet();
    for (int i = 0; i < cat_datasets.length; i++) {
      for (int j = 0; j < cat_datasets[i].size(); j++) {
        all_cats.add(cat_datasets[i].get(j));
      }
    }

    if (all_cats.size() == 0) {
      throw new MissingDataException("No data.");
    }

    Dataset dataset = new Dataset(of_params.length, all_cats.size(), 1);
    Category[] cat_array = (Category[]) all_cats.toArray(new Category[0]);
    Arrays.sort(cat_array, comp);
    for (int i = 0; i < count_datasets.length; i++) {
      for (int j = 0; j < cat_array.length; j++) {
        Integer value = (Integer) count_datasets[i].get(cat_array[j]);
        if (value == null) {
          value = new Integer(0);
        }
        dataset.set(i, j, 0, value.intValue());
      }
    }

    // Map axis labels
    String[] labels = new String[cat_array.length];
    for (int i = 0; i < cat_array.length; i++) {
      labels[i] = cat_array[i].getLabel();
    }
    if (by_param.equals("Hour")) {
      for (int i = 0; i < labels.length; i++) {
        int cat = Integer.valueOf(labels[i]).intValue() + 1;
        if (cat < 12) {
          labels[i] = cat + " AM";
        } else if (cat == 12) {
          labels[i] = cat + " PM";
        } else if (cat > 12) {
          labels[i] = (cat - 12) + " PM";
        }
      }
      graphChart2DProps.setLabelsAxisTitleText("Time (Eastern)");
    } else {
      String start = cat_array[0].getTitle();
      String end = cat_array[cat_array.length - 1].getTitle();
      if (start.equals(end)) {
        graphChart2DProps.setLabelsAxisTitleText(start);
      } else {
        graphChart2DProps.setLabelsAxisTitleText(start + " - " + end);
      }
    }

    graphChart2DProps.setLabelsAxisLabelsTexts(labels);
    graphChart2DProps.setNumbersAxisTitleText("Count");
    graphChart2DProps.setChartDatasetCustomizeGreatestValue(false);
    graphChart2DProps.setNumbersAxisNumLabels(15);

    // Configure chart
    LBChart2D chart2D = new LBChart2D();
    chart2D.setObject2DProperties(object2DProps);
    chart2D.setChart2DProperties(chart2DProps);
    chart2D.setLegendProperties(legendProps);
    chart2D.setGraphChart2DProperties(graphChart2DProps);
    chart2D.addGraphProperties(graphProps);
    chart2D.addDataset(dataset);
    chart2D.addMultiColorsProperties(multiColorsProps);

    // Optional validation:  Prints debug messages if invalid only.
    if (!chart2D.validate(false)) {
      chart2D.validate(true);

      // <-- End Chart2D configuration -->

    }
    data_source.restoreSortAreaSize();
    data_source.restoreHashAreaSize();

    return chart2D;
  }

  /**
   * Returns {@link Chart2D} for specified parameters.
   * @param of_param the "of" parameter
   * @param by_param the "by" parameter
   * @param for_param the "for" parameter
   * @param data_source the {@link MIDDataSource}
   * @return the {@link Chart2D}
   * @throws MEMEException if failed to get chart for time
   */
  private Chart2D getChartForTime(Object of_param, String by_param,
                                  int for_param, MIDDataSource data_source) throws
      MEMEException {

    String[] of_params = null;

    if (of_param instanceof String) {
      of_params = new String[] {
          (String) of_param};
    } else {
      of_params = (String[]) ( (List) of_param).toArray(new String[0]);

      // <-- Begin Chart2D configuration -->

      // Configure object properties
    }
    Object2DProperties object2DProps = new Object2DProperties();
    String[] legend_labels = new String[of_params.length];
    StringBuffer title = new StringBuffer();
    for (int i = 0; i < of_params.length; i++) {
      if (i > 0) {
        title.append(" ");
      }
      if (of_params[i].equals("MIN")) {
        legend_labels[i] = "Minimum";
      }
      if (of_params[i].equals("MAX")) {
        legend_labels[i] = "Maximum";
      }
      if (of_params[i].equals("AVG")) {
        legend_labels[i] = "Average";
      }
      title.append(legend_labels[i]);
    }
    title.append(" Elapsed Time By ").append(by_param);
    object2DProps.setObjectTitleText(title.toString());

    // Configure chart properties
    Chart2DProperties chart2DProps = new Chart2DProperties();
    chart2DProps.setChartDataLabelsPrecision( -1);

    // Configure legend properties
    LegendProperties legendProps = new LegendProperties();
    legendProps.setLegendExistence(true);
    legendProps.setLegendLabelsTexts(legend_labels);

    // Configure graph properties
    GraphProperties graphProps = new GraphProperties();
    if (by_param.equals("Hour") ||
        by_param.equals("Day") ||
        by_param.equals("Month")) {
      graphProps.setGraphBarsExistence(false);
      graphProps.setGraphLinesExistence(true);
    } else {
      graphProps.setGraphBarsExistence(true);
      graphProps.setGraphLinesExistence(false);
    }
    graphProps.setGraphOutlineComponentsExistence(false);
    graphProps.setGraphAllowComponentAlignment(true);

    // Configure graph component colors
    MultiColorsProperties multiColorsProps = new MultiColorsProperties();

    // Configure graph chart properties
    GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    graphChart2DProps.setLabelsAxisTicksAlignment(GraphChart2DProperties.CENTERED);

    List[] cat_datasets = new List[of_params.length];
    Map[] count_datasets = new Map[of_params.length];
    Comparator comp = null;

    for (int i = 0; i < of_params.length; i++) {

      // Map query
      String fields = "";
      String from = "";
      String join_table = "";
      String group_by = "";
      String having = "";
      StringBuffer and_condition = new StringBuffer();

      if (by_param.equals("Hour")) {
        comp = new HourEditorActionComparator();
        fields = ", " + of_params[i] +
            "(elapsed_time) AS ct, hour as label, 'Hour' as title";
        from = "elapsed_time, TO_CHAR(a.timestamp, 'HH24') AS hour";
        group_by = "hour";
      } else if (by_param.equals("Day")) {
        comp = new DayComparator();
        fields = ", " + of_params[i] +
            "(elapsed_time) AS ct, day as label, month as title";
        from = " elapsed_time, TO_CHAR(a.timestamp, 'DD') AS day," +
            " TO_CHAR(a.timestamp, 'Mon YYYY') AS month";
        group_by = "day, month";
      } else if (by_param.equals("Month")) {
        comp = new MonthComparator();
        fields = ", " + of_params[i] +
            "(elapsed_time) AS ct, month as label, year as title";
        from = " elapsed_time, TO_CHAR(a.timestamp, 'Mon') AS month, " +
            " TO_CHAR(a.timestamp, 'YYYY') AS year";
        group_by = "month, year";
      } else if (by_param.equals("Editor")) {
        comp = new HourEditorActionComparator();
        fields = ", COUNT(*) AS ct, editor as label, 'Editor' as title";
        from = " UPPER(a.authority) AS editor";
        group_by = "editor";
        and_condition.append(
            " AND (a.authority LIKE 'E-%' OR a.authority LIKE 'S-%')");
      } else if (by_param.equals("Action")) {
        comp = new HourEditorActionComparator();
        fields = ", COUNT(*) AS ct, action as label, 'Action' as title";
        from =
            " SUBSTR(molecular_action, INSTR(molecular_action, '_')+1) AS action";
        group_by = "action";
      } else if (by_param.equals("Source")) {
        comp = new HourEditorActionComparator();
        fields = ", COUNT(DISTINCT concept_id) as ct, " +
            " source as label, 'Source' as title";
        from = "source_id as concept_id," +
            " NVL((SELECT stripped_source FROM source_rank " +
            "      WHERE source IN (" +
            " DECODE(table_name, " +
            " 'C', (SELECT source FROM classes" +
            "    WHERE atom_id = row_id" +
            "    UNION" +
            "    SELECT source FROM dead_classes" +
            "    WHERE atom_id = row_id)," +
            " 'A', (SELECT source FROM attributes" +
            "    WHERE attribute_id = row_id" +
            "    UNION" +
            "    SELECT source FROM dead_attributes" +
            "    WHERE attribute_id = row_id)," +
            " 'R', (SELECT source FROM relationships" +
            "    WHERE relationship_id = row_id" +
            "    UNION" +
            "    SELECT source FROM dead_relationships" +
            "    WHERE relationship_id = row_id), 'X'))), 'MTH') as source";
        group_by = "source";
        join_table = ", atomic_actions b";
        and_condition.append(" AND a.molecule_id = b.molecule_id")
            .append(" AND b.table_name in ('C','A','R')");
        having = " HAVING COUNT(DISTINCT concept_id) > 4 * " + for_param +
            " AND source IN (SELECT source FROM source_version)";
      }

      if (for_param == 0) {
        and_condition.append(
            " AND a.timestamp > TO_DATE(TO_CHAR(sysdate, 'DD-Mon-YYYY'))");
      } else if (for_param == 7) {
        and_condition.append(
            " AND a.timestamp > TO_DATE(TO_CHAR(sysdate-7, 'DD-Mon-YYYY'))");
      } else if (for_param == 14) {
        and_condition.append(
            " AND a.timestamp > TO_DATE(TO_CHAR(sysdate-14, 'DD-Mon-YYYY'))");
      } else if (for_param == 30) {
        and_condition.append(
            " AND a.timestamp > TO_DATE(TO_CHAR(sysdate-30, 'DD-Mon-YYYY'))");

        // Build up a query that searches for dataset fields rows
      }
      String query = "SELECT MIN(rownum)" + fields +
          " FROM (SELECT " + from +
          "  FROM molecular_actions a" + join_table +
          "  WHERE undone='N' " + and_condition.toString() +
          "  ORDER BY a.timestamp) a" +
          " GROUP BY " + group_by + having +
          " ORDER BY MIN(rownum)";

      MEMEToolkit.trace("QUERY: " + query);

      List cats = new ArrayList();
      Map counts = new HashMap();

      // Execute the query
      try {
        PreparedStatement pstmt = data_source.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        // Read
        while (rs.next()) {
          Category cat = new Category(rs.getString("LABEL"),
                                      rs.getString("TITLE"));
          cats.add(cat);
          counts.put(cat, new Integer(rs.getInt("CT")));
        }
        // Close statement
        pstmt.close();
      } catch (Exception e) {
        DataSourceException dse = new DataSourceException(
            "Failed to get dataset fields.", this, e);
        dse.setDetail("query", query);
        throw dse;
      }

      cat_datasets[i] = cats;
      count_datasets[i] = counts;

    } // End for over of_params

    // Configure dataset
    Set all_cats = new HashSet();
    for (int i = 0; i < cat_datasets.length; i++) {
      for (int j = 0; j < cat_datasets[i].size(); j++) {
        all_cats.add(cat_datasets[i].get(j));
      }
    }

    Dataset dataset = new Dataset(of_params.length, all_cats.size(), 1);
    Category[] cat_array = (Category[]) all_cats.toArray(new Category[0]);
    Arrays.sort(cat_array, comp);
    for (int i = 0; i < count_datasets.length; i++) {
      for (int j = 0; j < cat_array.length; j++) {
        Integer value = (Integer) count_datasets[i].get(cat_array[j]);
        if (value == null) {
          value = new Integer(0);
        }
        dataset.set(i, j, 0, value.intValue());
      }
    }

    // Map axis labels
    String[] labels = new String[cat_array.length];
    for (int i = 0; i < cat_array.length; i++) {
      labels[i] = cat_array[i].getLabel();
    }
    if (by_param.equals("Hour")) {
      for (int i = 0; i < labels.length; i++) {
        int cat = Integer.valueOf(labels[i]).intValue() + 1;
        if (cat < 12) {
          labels[i] = cat + " AM";
        } else if (cat == 12) {
          labels[i] = cat + " PM";
        } else if (cat > 12) {
          labels[i] = (cat - 12) + " PM";
        }
      }
      graphChart2DProps.setLabelsAxisTitleText("Time (Eastern)");
    } else {
      String start = cat_array[0].getTitle();
      String end = cat_array[cat_array.length - 1].getTitle();
      if (start.equals(end)) {
        graphChart2DProps.setLabelsAxisTitleText(start);
      } else {
        graphChart2DProps.setLabelsAxisTitleText(start + " - " + end);
      }
    }

    graphChart2DProps.setLabelsAxisLabelsTexts(labels);
    graphChart2DProps.setNumbersAxisTitleText("Milliseconds");
    graphChart2DProps.setChartDatasetCustomizeGreatestValue(false);
    graphChart2DProps.setNumbersAxisNumLabels(15);

    // Configure chart
    LBChart2D chart2D = new LBChart2D();
    chart2D.setObject2DProperties(object2DProps);
    chart2D.setChart2DProperties(chart2DProps);
    chart2D.setLegendProperties(legendProps);
    chart2D.setGraphChart2DProperties(graphChart2DProps);
    chart2D.addGraphProperties(graphProps);
    chart2D.addDataset(dataset);
    chart2D.addMultiColorsProperties(multiColorsProps);

    // Optional validation:  Prints debug messages if invalid only.
    if (!chart2D.validate(false)) {
      chart2D.validate(true);

      // <-- End Chart2D configuration -->

    }
    return chart2D;
  }

  /**
   * Returns {@link Chart2D} for specified parameters.
   * @param of_param the "of" parameter
   * @param by_param the "by" parameter
   * @param for_param the "for" parameter
   * @param data_source the {@link MIDDataSource}
   * @return the {@link Chart2D}
   * @throws MEMEException if failed to get chart for ratio
   */
  private Chart2D getChartForRatio(Object of_param, String by_param,
                                   int for_param, MIDDataSource data_source) throws
      MEMEException {

    String[] of_params = null;

    if (of_param instanceof String) {
      of_params = new String[] {
          (String) of_param};
    } else {
      of_params = (String[]) ( (List) of_param).toArray(new String[0]);

      // <-- Begin Chart2D configuration -->

      // Configure object properties
    }
    Object2DProperties object2DProps = new Object2DProperties();
    StringBuffer title = new StringBuffer();
    for (int i = 0; i < of_params.length; i++) {
      if (i > 0) {
        title.append(" ");
      }
      title.append(of_params[i]);
    }
    title.append(" Actions/Approval By ").append(by_param);
    object2DProps.setObjectTitleText(title.toString());

    // Configure chart properties
    Chart2DProperties chart2DProps = new Chart2DProperties();
    chart2DProps.setChartDataLabelsPrecision( -1);

    // Configure legend properties
    LegendProperties legendProps = new LegendProperties();
    legendProps.setLegendExistence(true);
    String[] legendLabels = of_params;
    legendProps.setLegendLabelsTexts(legendLabels);

    // Configure graph properties
    GraphProperties graphProps = new GraphProperties();
    if (by_param.equals("Hour") ||
        by_param.equals("Day") ||
        by_param.equals("Month")) {
      graphProps.setGraphBarsExistence(false);
      graphProps.setGraphLinesExistence(true);
    } else {
      graphProps.setGraphBarsExistence(true);
      graphProps.setGraphLinesExistence(false);
    }
    graphProps.setGraphOutlineComponentsExistence(false);
    graphProps.setGraphAllowComponentAlignment(true);

    // Configure graph component colors
    MultiColorsProperties multiColorsProps = new MultiColorsProperties();

    // Configure graph chart properties
    GraphChart2DProperties graphChart2DProps = new GraphChart2DProperties();
    graphChart2DProps.setLabelsAxisTicksAlignment(GraphChart2DProperties.CENTERED);

    List[] cat_datasets = new List[of_params.length];
    Map[] count_datasets = new Map[of_params.length];
    Comparator comp = null;

    for (int i = 0; i < of_params.length; i++) {

      // Map query
      StringBuffer fields = new StringBuffer();
      StringBuffer from = new StringBuffer();
      String group_by = "";
      String having = "";
      StringBuffer and_condition = new StringBuffer();

      fields.append(
          ", SUM(x)/DECODE(COUNT(DISTINCT y)-1, 0, 1, COUNT(DISTINCT y)-1) as ratio");
      from.append("1 as x, DECODE(molecular_action, 'MOLECULAR_CONCEPT_APPROVAL', source_id, 0) as y");
      having = " HAVING COUNT(DISTINCT y) > 0";

      if (of_params[i].equals("Interface")) {
        and_condition.append(" AND authority LIKE 'E-%'");
      } else if (of_params[i].equals("All")) {
        and_condition.append(
            " AND (authority LIKE 'S-%' OR authority LIKE 'E-%')");

      }
      if (by_param.equals("Hour")) {
        comp = new HourEditorActionComparator();
        fields.append(", hour as label, 'Hour' as title");
        from.append(", TO_CHAR(timestamp, 'HH24') AS hour");
        group_by = "hour";
      } else if (by_param.equals("Day")) {
        comp = new DayComparator();
        fields.append(", day as label, month as title");
        from.append(", TO_CHAR(timestamp, 'DD') AS day")
            .append(", TO_CHAR(timestamp, 'Mon YYYY') AS month");
        group_by = "day, month";
      } else if (by_param.equals("Month")) {
        comp = new MonthComparator();
        fields.append(", month as label, year as title");
        from.append(", TO_CHAR(timestamp, 'Mon') AS month")
            .append(", TO_CHAR(timestamp, 'YYYY') AS year");
        group_by = "month, year";
      } else if (by_param.equals("Editor")) {
        comp = new HourEditorActionComparator();
        fields.append(", authority as label, 'Editor' as title");
        from.append(", authority");
        group_by = "authority";
      }

      if (for_param == 0) {
        and_condition.append(
            " AND timestamp > TO_DATE(TO_CHAR(sysdate, 'DD-Mon-YYYY'))");
      } else if (for_param == 7) {
        and_condition.append(
            " AND timestamp > TO_DATE(TO_CHAR(sysdate-7, 'DD-Mon-YYYY'))");
      } else if (for_param == 14) {
        and_condition.append(
            " AND timestamp > TO_DATE(TO_CHAR(sysdate-14, 'DD-Mon-YYYY'))");
      } else if (for_param == 30) {
        and_condition.append(
            " AND timestamp > TO_DATE(TO_CHAR(sysdate-30, 'DD-Mon-YYYY'))");

        // Build up a query that searches for dataset fields rows
      }
      String query = "SELECT MIN(rownum)" + fields.toString() +
          " FROM (SELECT " + from.toString() +
          "  FROM molecular_actions" +
          "  WHERE undone='N' " + and_condition.toString() +
          "  ORDER BY timestamp) a" +
          " GROUP BY " + group_by + having +
          " ORDER BY MIN(rownum)";

      MEMEToolkit.trace("QUERY: " + query);

      List cats = new ArrayList();
      Map counts = new HashMap();

      // Execute the query
      try {
        PreparedStatement pstmt = data_source.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        // Read
        while (rs.next()) {
          String label = rs.getString("LABEL");
          if (by_param.equals("Editor")) {
            label = rs.getString("LABEL").substring(2);
          }
          Category cat = new Category(label, rs.getString("TITLE"));
          cats.add(cat);
          counts.put(cat, new Integer(rs.getInt("RATIO")));
        }
        // Close statement
        pstmt.close();
      } catch (Exception e) {
        DataSourceException dse = new DataSourceException(
            "Failed to get dataset fields.", this, e);
        dse.setDetail("query", query);
        throw dse;
      }

      cat_datasets[i] = cats;
      count_datasets[i] = counts;

    } // End for over of_params

    // Configure dataset
    Set all_cats = new HashSet();
    for (int i = 0; i < cat_datasets.length; i++) {
      for (int j = 0; j < cat_datasets[i].size(); j++) {
        all_cats.add(cat_datasets[i].get(j));
      }
    }

    Dataset dataset = new Dataset(of_params.length, all_cats.size(), 1);
    Category[] cat_array = (Category[]) all_cats.toArray(new Category[0]);
    Arrays.sort(cat_array, comp);
    for (int i = 0; i < count_datasets.length; i++) {
      for (int j = 0; j < cat_array.length; j++) {
        Integer value = (Integer) count_datasets[i].get(cat_array[j]);
        if (value == null) {
          value = new Integer(0);
        }
        dataset.set(i, j, 0, value.intValue());
      }
    }

    // Map axis labels
    String[] labels = new String[cat_array.length];
    for (int i = 0; i < cat_array.length; i++) {
      labels[i] = cat_array[i].getLabel();
    }
    if (by_param.equals("Hour")) {
      for (int i = 0; i < labels.length; i++) {
        int cat = Integer.valueOf(labels[i]).intValue() + 1;
        if (cat < 12) {
          labels[i] = cat + " AM";
        } else if (cat == 12) {
          labels[i] = cat + " PM";
        } else if (cat > 12) {
          labels[i] = (cat - 12) + " PM";
        }
      }
      graphChart2DProps.setLabelsAxisTitleText("Time (Eastern)");
    } else {
      String start = cat_array[0].getTitle();
      String end = cat_array[cat_array.length - 1].getTitle();
      if (start.equals(end)) {
        graphChart2DProps.setLabelsAxisTitleText(start);
      } else {
        graphChart2DProps.setLabelsAxisTitleText(start + " - " + end);
      }
    }

    graphChart2DProps.setLabelsAxisLabelsTexts(labels);
    graphChart2DProps.setNumbersAxisTitleText("Ratio");
    graphChart2DProps.setChartDatasetCustomizeGreatestValue(false);
    graphChart2DProps.setNumbersAxisNumLabels(15);

    // Configure chart
    LBChart2D chart2D = new LBChart2D();
    chart2D.setObject2DProperties(object2DProps);
    chart2D.setChart2DProperties(chart2DProps);
    chart2D.setLegendProperties(legendProps);
    chart2D.setGraphChart2DProperties(graphChart2DProps);
    chart2D.addGraphProperties(graphProps);
    chart2D.addDataset(dataset);
    chart2D.addMultiColorsProperties(multiColorsProps);

    // Optional validation:  Prints debug messages if invalid only.
    if (!chart2D.validate(false)) {
      chart2D.validate(true);

      // <-- End Chart2D configuration -->

    }
    return chart2D;
  }

  //
  // Inner Classes
  //

  /**
   * Inner class for tracking categories.
   */
  private class Category {

    // Fields
    private String label = null;
    private String title = null;

    // Constructor
    public Category(String label, String title) {
      this.label = label;
      this.title = title;
    }

    public String getLabel() {
      return label;
    }

    public String getTitle() {
      return title;
    }

    public boolean equals(Object o) {
      if (o == null || ! (o instanceof Category)) {
        return false;
      }
      Category c = (Category) o;
      return title.equals(c.getTitle()) && label.equals(c.getLabel());
    }

    public int hashCode() {
      return (label + title).hashCode();
    }
  }

  /**
   * Inner class for comparing hour categories.
   */
  public class HourEditorActionComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      Category c1 = (Category) o1;
      Category c2 = (Category) o2;
      return c1.getLabel().compareTo(c2.getLabel());
    }
  }

  /**
   * Inner class for comparing month categories.
   */
  public class MonthComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      SimpleDateFormat f = new SimpleDateFormat("MMM yyyy");
      Category c1 = (Category) o1;
      Category c2 = (Category) o2;
      String s1 = c1.getLabel() + " " + c1.getTitle();
      String s2 = c2.getLabel() + " " + c2.getTitle();
      int result = 0;
      try {
        result = f.parse(s1).compareTo(f.parse(s2));
      } catch (Exception e) {}
      return result;
    }
  }

  /**
   * Inner class for comparing day categories.
   */
  public class DayComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      SimpleDateFormat f = new SimpleDateFormat("MMM yyyy");
      Category c1 = (Category) o1;
      Category c2 = (Category) o2;
      int result = 0;
      try {
        result = f.parse(c1.getTitle()).compareTo(f.parse(c2.getTitle()));
        if (result == 0) {
          return c1.getLabel().compareTo(c2.getLabel());
        }
      } catch (Exception e) {}
      return result;
    }
  }

}