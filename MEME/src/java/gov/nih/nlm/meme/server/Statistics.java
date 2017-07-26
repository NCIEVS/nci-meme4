/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.server
 * Object:  Statistics
 *
 *****************************************************************************/

package gov.nih.nlm.meme.server;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Server statistics.
 *
 * @author MEME Group
 */
public class Statistics {

  //
  // Fields
  //

  private static Date server_start_time = new Date();
  private static int request_ct = 0;
  private static int processed_request_ct = 0;
  private static int active_request_ct = 0;
  private static int active_session_ct = 0;
  private static HashMap sessions_by_service = new HashMap();
  private static HashMap requests_by_service = new HashMap();
  private static HashMap requests_by_type = new HashMap();
  private static HashMap requests_by_mid_service = new HashMap();
  private static HashMap exceptions = new HashMap();

  //
  // Constructors
  //

  /**
   * Instantiates {@link Statistics}.
   */
  private Statistics() {}

  //
  // Methods
  //

  /**
   * Reset all statistics fields to initial state.
   */
  public static void reset() {
    server_start_time = new Date();
    request_ct = 0;
    processed_request_ct = 0;
    active_request_ct = 0;
    active_session_ct = 0;
    sessions_by_service = new HashMap();
    requests_by_service = new HashMap();
    requests_by_type = new HashMap();
    exceptions = new HashMap();
  }

  /**
   * Returns the thread {@link PoolStatistics}.
   * @return the thread {@link PoolStatistics}
   */
  public static PoolStatistics getThreadPoolStatistics() {
    return ServerToolkit.getThreadPool().getStatistics();
  }

  /**
   * Returns the data source {@link PoolStatistics}.
   * @return the data source {@link PoolStatistics}
   */
  public static PoolStatistics getDataSourcePoolStatistics() {
    return ServerToolkit.getDataSourcePool().getStatistics();
  }

  /**
   * Returns the server start time.
   * @return the server start time
   */
  public static Date getServerStartTime() {
    return server_start_time;
  }

  /**
   * Returns the server run time.
   * @return the server run time
   */
  public static long getServerRunTime() {
    return (new Date()).getTime() - server_start_time.getTime();
  }

  /**
   * Tracks sessions by service name when initiated.
   * @param context the {@link SessionContext}
   */
  public static void sessionInitiated(SessionContext context) {
    MEMEServiceRequest request = context.getServiceRequest();

    active_session_ct++;
    Integer ct = (Integer) sessions_by_service.get(request.getService());
    if (ct == null) {
      sessions_by_service.put(request.getService(), new Integer(1));
    } else {
      sessions_by_service.put(request.getService(),
                              new Integer(ct.intValue() + 1));
    }
  }

  /**
   * Tracks sessions by service name when initiated.
   * @param context the {@link SessionContext}
   */
  public static void sessionTerminated(SessionContext context) {
    active_session_ct--;
    Iterator iter = context.values().iterator();
    while (iter.hasNext()) {
      Object o = iter.next();
      if (o instanceof MEMEServiceRequest) {
        MEMEServiceRequest request = (MEMEServiceRequest) o;
        Integer ct = (Integer) sessions_by_service.get(request.getService());
        if (ct == null) {
          continue;
        } else if (ct.intValue() == 1) {
          sessions_by_service.remove(request.getService());
        } else {
          sessions_by_service.put(request.getService(),
                                  new Integer(ct.intValue() - 1));
        }
      }
    }
  }

  /**
   * Calculates active and successful process request when the process request
   * has started.
   * @param request the {@link MEMEServiceRequest}
   */
  public static void requestStarted(MEMEServiceRequest request) {
    active_request_ct++;
    request_ct++;

    Integer ct = (Integer) requests_by_service.get(request.getService());
    if (ct == null) {
      requests_by_service.put(request.getService(), new Integer(1));
    } else {
      requests_by_service.put(request.getService(),
                              new Integer(ct.intValue() + 1));

    }
    ct = (Integer) requests_by_type.get(request.getClass());
    if (ct == null) {
      requests_by_type.put(request.getClass(), new Integer(1));
    } else {
      requests_by_type.put(request.getClass(), new Integer(ct.intValue() + 1));

    }
    if (request.getMidService() != null) {
      String service = request.getMidService();
      if (service.equals("")) {
        service = MEMEToolkit.getProperty(ServerConstants.MID_SERVICE);
      }
      ct = (Integer) requests_by_mid_service.get(service);
      if (ct == null) {
        requests_by_mid_service.put(service, new Integer(1));
      } else {
        requests_by_mid_service.put(service, new Integer(ct.intValue() + 1));
      }
    }

  }

  /**
   * Calculates active and successful process request when the process request
   * has finished.
   */
  public static void requestFinished() {
    processed_request_ct++;
    active_request_ct--;
  }

  /**
   * Returns the request count.
   * @return the request count
   */
  public static int getRequestCount() {
    return request_ct;
  }

  /**
   * Returns the process request count.
   * @return the process request count
   */
  public static int getProcessedRequestCount() {
    return processed_request_ct;
  }

  /**
   * Returns the active request count.
   * @return the active request count
   */
  public static int getActiveRequestCount() {
    return active_request_ct;
  }

  /**
   * Returns the active session count.
   * @return the session request count
   */
  public static int getActiveSessionCount() {
    return active_session_ct;
  }

  /**
   * Returns the session count by service.
   * @param service the service name
   */
  public static int getSessionCountByService(String service) {
    return ( (Integer) sessions_by_service.get(service)).intValue();
  }

  /**
   * Returns the session by service map.
   * @return the session by service map
   */
  public static HashMap getSessionCountsByService() {
    return sessions_by_service;
  }

  /**
   * Returns the request count by service name.
   * @param service service name
   * @return the request count by service name
   */
  public static int getRequestCountByService(String service) {
    return ( (Integer) requests_by_service.get(service)).intValue();
  }

  /**
   * Returns the request by service map.
   * @return the request by service map
   */
  public static HashMap getRequestCountsByService() {
    return requests_by_service;
  }

  /**
   * Returns the request count by type.
   * @param c the request {@link Class} type
   */
  public static int getRequestCountByType(Class c) {
    return ( (Integer) requests_by_type.get(c)).intValue();
  }

  /**
   * Returns the request by type map.
   * @return the requesty by type map
   */
  public static HashMap getRequestCountsByType() {
    return requests_by_type;
  }

  /**
   * Returns the request count by data source.
   * @param s the data source name
   */
  public static int getRequestCountByMidService(String s) {
    return ( (Integer) requests_by_mid_service.get(s)).intValue();
  }

  /**
   * Returns the request by data source map.
   * @return the request by data source map
   */
  public static HashMap getRequestCountsByMidService() {
    return requests_by_mid_service;
  }

  /**
   * Adds specified {@link Exception} to array list.
   * @param e the {@link Exception} to add
   */
  public static void addException(Exception e) {
    String key = e.getClass().getName() + ": " + e.getMessage();
    Integer ct = (Integer) exceptions.get(key);
    if (ct == null) {
      exceptions.put(key, new Integer(1));
    } else {
      exceptions.put(key, new Integer(ct.intValue() + 1));
    }
  }

  /**
   * Returns the {@link Exception} map.
   * @return the {@link Exception} map
   */
  public static HashMap getExceptions() {
    return exceptions;
  }

  /**
   * Returns the exception count by type.
   * @param s the exception count by type
   */
  public static int getExceptionCountByType(String s) {
    return ( (Integer) exceptions.get(s)).intValue();
  }

  /**
   * Returns the runtime free memory.
   * @return the runtime free memory
   */
  public static long getFreeMemory() {
    return Runtime.getRuntime().freeMemory();
  }

  /**
   * Returns the runtime total memory.
   * @return the runtime total memory
   */
  public static long getTotalMemory() {
    return Runtime.getRuntime().totalMemory();
  }

}
