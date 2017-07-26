/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MEMEDataSourceFactory
 *
 * Changes:
 * 12/21/2005 BAC (1-719SM): LANGUAGE and CHARSET properties passed to JDBC driver
 * 
 ****************************************************************************/
package gov.nih.nlm.meme.sql;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.exception.DataSourceConnectionException;
import gov.nih.nlm.meme.exception.MidsvcsException;
import gov.nih.nlm.meme.exception.ReflectionException;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.lang.reflect.Constructor;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Responsible for creating the "right" kind of
 * {@link gov.nih.nlm.meme.sql.MEMEDataSource}
 * based on structured set of properties.
 *
 * @author MEME Group
 */
public class MEMEDataSourceFactory {

  /**
   * Creates a MEMEDataSource by using five properties. <ul>
   * <li>driver_class: A JDBC2.0 compliant Driver</li>
   * <li>service: A value appended with "-jdbc" or "-tns" and passed to
   * {@link gov.nih.nlm.meme.MIDServices#getService(String)}.</li>
   * <li>user: the database username </li>
   * <li>password: the database password</li>
   * <li>connection: The fully qualified classname of an implementation
   * of {@link gov.nih.nlm.meme.sql.MEMEDataSource}.</li></ul>
   *
   * @param properties the {@link Properties} representation of the above properties.
   * @return the {@link MEMEDataSource} representation of data source.
   * @throws ReflectionException if failed to open the connection.
   * @throws MidsvcsException if the midsvcs server is not available.
   * @throws DataSourceConnectionException if cannot establish data source connection.
   */
  public MEMEDataSource newMEMEDataSource(Properties properties) throws
      ReflectionException, MidsvcsException, DataSourceConnectionException {

    String driver = properties.getProperty("driver_class");
    try {
      // Load the Oracle JDBC2.0 Driver and register it.
      DriverManager.registerDriver( (Driver) Class.forName(driver).newInstance());
    } catch (Exception e) {
      ReflectionException re = new ReflectionException(
          "Failed to load or instantiate database driver.");
      re.setDetail("driver", driver);
      throw re;
    }

    // Get the DB service
    // As of 1/30/2002, there are new midservices names
    // and so we may well get a service name like editing-db, giving
    // us editing-db-jdbc where we just want editing-jdbc.  Strip out
    // the "-db"

    String jdbc_string = null;
    String service = properties.getProperty("service");

    if (service.endsWith("-db")) {
      service = MIDServices.getService(service);

      // Append -jdbc to service name and look up using MIDService
    }
    String jdbc_service = service + "-jdbc";
    jdbc_string = MIDServices.getService(jdbc_service);

    String ds_name = null;
    String machine = null;
    String db_name = null;

    if (jdbc_string == null || jdbc_string == "") {
      String[] fields = FieldedStringTokenizer.split(service, "_");
      if (fields.length == 2) {
        machine = fields[0];
        db_name = fields[1];
      } else {
        DataSourceConnectionException dsce = new DataSourceConnectionException(
            "Failed to connect to data source.");
        dsce.setDetail("service", service);
        throw dsce;
      }

      // Construct jdbc string
      if (machine.indexOf(".") == -1) {
        jdbc_string = "jdbc:oracle:thin:@" + machine + ".nlm.nih.gov:1521:" +
            db_name;
      } else {
        jdbc_string = "jdbc:oracle:thin:@" + machine + ":1521:" + db_name;
      }
    } else {
      // Extract the 1st 18 char that must be "jdbc:oracle:thin:@" and
      // this will give something like machine:1521:db_name.
      FieldedStringTokenizer st =
          new FieldedStringTokenizer(jdbc_string.substring(18), ":");

      // Only care about the machine and db_name, so here skip the 2nd token
      machine = st.nextToken().toString();
      st.nextToken();
      db_name = st.nextToken().toString();

      // If machine is like oa.nlm.nih.gov, make it just oa
      if (machine.indexOf('.') != -1) {
        machine = machine.substring(0, machine.indexOf('.'));
      }
    }

    // Set data source name
    ds_name = machine + "_" + db_name;

    // Service must be set correctly here
    service = jdbc_string;

    // Get the username and password
    String user = properties.getProperty("user");
    String password = properties.getProperty("password");

    String password_mask = "*****************************************";

    Class connection_class = null;
    Constructor constructor = null;
    try {
      // Open the database connection
      MEMEToolkit.logComment(
          "Initializing database session - open connection " +
          service + " " + user + "/" +
          password_mask.substring(0, password.length()), true);
      connection_class = Class.forName(properties.getProperty("connection"));
      Class[] args = new Class[] {
          Class.forName("java.sql.Connection")};
      constructor = connection_class.getConstructor(args);
    } catch (Exception e) {
      ReflectionException re = new ReflectionException(
          "Constructor failed.");
      re.setDetail("class", connection_class.getName());
    }

    try {
      // Create the MEMEConnection instance dynamically
      Properties props = new Properties();
      props.setProperty("LANGUAGE", "AMERICAN_AMERICA");
      props.setProperty("CHARSET", "UTF8");
      props.put("user",user);
      props.put("password",password);
      MEMEConnection conn =
          (MEMEConnection) constructor.newInstance(new Object[] {
          DriverManager.getConnection(service, props)});
      conn.setServiceName(properties.getProperty("service"));
      conn.setDataSourceName(ds_name);
      if (!conn.isCacheLoaded()) {
        conn.refreshCaches();
      }
      conn.enableBuffer();

      // Cast to MEMEDataSource & return it
      MEMEDataSource source = (MEMEDataSource) conn;
      MEMEToolkit.trace("Connection established");

      return source;

    } catch (Exception e) {
      DataSourceConnectionException dsce = new DataSourceConnectionException(
          "Failed to open data source.", e);
      dsce.setDetail("driver_class", properties.getProperty("driver_class"));
      dsce.setDetail("service", properties.getProperty("service"));
      dsce.setDetail("user", properties.getProperty("user"));
      dsce.setDetail("connection", properties.getProperty("connection"));
      throw dsce;
    }
  }

}
