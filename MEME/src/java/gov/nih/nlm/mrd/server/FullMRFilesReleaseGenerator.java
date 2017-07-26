/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server
 * Object:  FullMRFilesReleaseGenerator
 *
 * Changes:
 *   03/08/2007 TTN (1-DKB57): Add Finish Release method
 *   04/10/2006 RBE (1-AV6XL): add -mrd switch to run medline process in MRD mode
 *   03/24/2006 RBE (1-AQRCB): use MID Services mailing list
 *   01/20/2006 TTN (1-74OL9): bug fix for update
 *   01/06/2006 TTN (1-73ETH): Remove comments
 *
 *****************************************************************************/

package gov.nih.nlm.mrd.server;

import gov.nih.nlm.meme.MEMEMail;
import gov.nih.nlm.meme.MIDServices;
import gov.nih.nlm.meme.common.Authority;
import gov.nih.nlm.meme.common.Parameter;
import gov.nih.nlm.meme.exception.BadValueException;
import gov.nih.nlm.meme.exception.ExecException;
import gov.nih.nlm.meme.exception.ExternalResourceException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.exception.MailException;
import gov.nih.nlm.meme.server.SessionContext;
import gov.nih.nlm.meme.xml.MEMEServiceRequest;
import gov.nih.nlm.mrd.common.QAReason;
import gov.nih.nlm.mrd.common.QAReport;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.common.ReleaseTarget;
import gov.nih.nlm.meme.common.StageStatus;
import gov.nih.nlm.mrd.server.handlers.QAReportParser;
import gov.nih.nlm.mrd.sql.MRDDataSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Handles requests for building a Full set of RRF MR files.
 *
 * @author  MEME Group
 */

public class FullMRFilesReleaseGenerator
    extends ReleaseGenerator {

  //
  // Implementation of MEMEApplicationService interface
  //

  //
  // Cache for release target objects
  //
  private Hashtable release_target_cache = new Hashtable(50);

  private Hashtable stage_processes = new Hashtable(5);

  /**
   * Processes request from the {@link MRDApplicationServer}.
   * @param context the {@link SessionContext}
   * @throws MEMEException if failed to process the request
   */
  public void processRequest(SessionContext context) throws MEMEException {
    final MRDDataSource data_source = (MRDDataSource) context.getDataSource();
    final MEMEServiceRequest request = context.getServiceRequest();
    final String function = (String) request.getParameter("function").getValue();
    if (function.equals("activateTargetHandler")) {
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      activateTargetHandler(target_name, data_source);
    }
    else if (function.equals("activateTargetHandlers")) {
      final String[] target_names =
          (String[]) request.getParameter("target_names").getValue();
      activateTargetHandlers(target_names, data_source);
    }
    else if (function.equals("deactivateTargetHandler")) {
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      deactivateTargetHandler(target_name, data_source);
    }
    else if (function.equals("deactivateTargetHandlers")) {
      final String[] target_names =
          (String[]) request.getParameter("target_names").getValue();
      deactivateTargetHandlers(target_names, data_source);
    }
    else if (function.equals("clearStatus")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      final String stage =
          (String) request.getParameter("stage").getValue();
      clearStatus(data_source.getReleaseInfo(release_name), target_name, stage);
    }
    else if (function.equals("clearMedlineStatus")) {
      final String stage =
          (String) request.getParameter("stage").getValue();
      clearMedlineStatus(stage);
    }
    else if (function.equals("doProcess")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      final String process =
          (String) request.getParameter("process").getValue();
      doProcess(process, release_name, data_source.getDataSourceName());
    }
    else if (function.equals("generateCuiComparisonReport")) {
      final String cui =
          (String) request.getParameter("cui").getValue();
      final ReleaseInfo current =
          data_source.getReleaseInfo(
              (String) request.getParameter("current").getValue());
      final ReleaseInfo compareTo =
          data_source.getReleaseInfo(
              (String) request.getParameter("compareTo").getValue());
      generateCuiComparisonReport(cui, current, compareTo);
    }
    else if (function.equals("getCuiComparisonReport")) {
      final String cui =
          (String) request.getParameter("cui").getValue();
      final ReleaseInfo release =
          data_source.getReleaseInfo(
              (String) request.getParameter("release_name").getValue());
      request.addReturnValue(new Parameter.Default("report",
          getCuiComparisonReport(cui, release)));
    }
    else if (function.equals("getTargetNames")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      request.addReturnValue(new Parameter.Default("targets",
          getTargetNames(release_name, data_source)));
    }
    else if (function.equals("getTargets")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      request.addReturnValue(new Parameter.Default("targets",
          getTargets(release_name, data_source)));
    }
    else if (function.equals("getReleaseHistory")) {
      request.addReturnValue(new Parameter.Default("release_history",
          getReleaseHistory(data_source)));
    }
    else if (function.equals("getReleaseInfo")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      request.addReturnValue(new Parameter.Default("release_info",
          getReleaseInfo(release_name, data_source)));
    }
    else if (function.equals("getTarget")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      ReleaseTarget target;
      if (release_target_cache.containsKey(target_name + release_name)) {
        target =
            (ReleaseTarget) release_target_cache.get(target_name + release_name);
      }
      else {
        target = getTarget(release_name, target_name, data_source);
        release_target_cache.put(target_name + release_name, target);
      }
      request.addReturnValue(new Parameter.Default("target",
          target));
    }
    else if (function.equals("getTargetStatus")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      request.addReturnValue(new Parameter.Default("target",
          getTargetStatus(release_name, target_name, data_source)));
    }
    else if (function.equals("getTargetQAReport")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      request.addReturnValue(new Parameter.Default("target",
          getTargetQAReport(release_name, target_name, data_source)));
    }
    else if (function.equals("getGoldStandardQAResults")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      request.addReturnValue(
          new Parameter.Default("goldStandardQAResults",
                                getGoldStandardQAResults(release_name,
          target_name, data_source)));
    }
    else if (function.equals("getQAResults")) {
      final String release_name =
          (String) request.getParameter("release_name").getValue();
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      request.addReturnValue(
          new Parameter.Default("QAResults",
                                getQAResults(release_name, target_name,
                                             data_source)));
    }
    else if (function.equals("isFinished")) {
      final ReleaseInfo release =
          data_source.getReleaseInfo(
              (String) request.getParameter("release_name").getValue());
      request.addReturnValue(
          new Parameter.Default("isFinished", isFinished(release)));
    }
    else if (function.equals("getReleaseStatus")) {
      final ReleaseInfo release =
          data_source.getReleaseInfo(
              (String) request.getParameter("release_name").getValue());
      request.addReturnValue(
          new Parameter.Default("releaseStatus", getReleaseStatus(release)));
    }
    else if (function.equals("isReadyForPublish")) {
      final ReleaseInfo release =
          data_source.getReleaseInfo(
              (String) request.getParameter("release_name").getValue());
      request.addReturnValue(
          new Parameter.Default("isReadyForPublish", isReadyForPublish(release)));
    }
    else if (function.equals("prepareRelease")) {
      final ReleaseInfo release =
          (ReleaseInfo) request.getParameter("releaseinfo").getValue();
      prepareRelease(release, data_source);
    }
    else if (function.equals("finishRelease")) {
        final ReleaseInfo release =
            (ReleaseInfo) request.getParameter("releaseinfo").getValue();
        finishRelease(release, data_source);
      }
    else if (function.equals("previewTarget")) {
      final ReleaseInfo release =
          data_source.getReleaseInfo(
              (String) request.getParameter("release_name").getValue());
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      final int lines = ( (Integer) request.getParameter("lines").getValue()).
          intValue();
      request.addReturnValue(
          new Parameter.Default("previewTarget",
                                previewTarget(release, data_source, target_name,
                                              lines)));
    }
    else if (function.equals("setReleaseInfo")) {
      final ReleaseInfo release_info =
          (ReleaseInfo) request.getParameter("release_info").getValue();
      setReleaseInfo(release_info, data_source);
    }
    else if (function.equals("removeReleaseInfo")) {
      final ReleaseInfo release_info =
          (ReleaseInfo) request.getParameter("release_info").getValue();
      removeReleaseInfo(release_info, data_source);
    }
    else if (function.equals("addQAReason")) {
      final QAReason qareason = (QAReason) request.getParameter("qareason").
          getValue();
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      addQAReason(qareason, target_name, data_source);
      final List toremove = new ArrayList();
      for (Iterator e = release_target_cache.keySet().iterator(); e.hasNext(); ) {
        final String name = (String) e.next();
        if (name.startsWith(target_name)) {
          toremove.add(name);
        }
      }
      for (Iterator e = toremove.iterator(); e.hasNext(); ) {
        release_target_cache.remove(e.next());
      }
    }
    else if (function.equals("removeQAReason")) {
      final QAReason qareason = (QAReason) request.getParameter("qareason").
          getValue();
      final String target_name =
          (String) request.getParameter("target_name").getValue();
      removeQAReason(qareason, target_name, data_source);
      final List toremove = new ArrayList();
      for (Iterator e = release_target_cache.keySet().iterator(); e.hasNext(); ) {
        final String name = (String) e.next();
        if (name.startsWith(target_name)) {
          toremove.add(name);
        }
      }
      for (Iterator e = toremove.iterator(); e.hasNext(); ) {
        release_target_cache.remove(e.next());
      }
    }
    else if (function.equals("downloadMedlineBaseline")) {
      downloadMedlineBaseline();
    }
    else if (function.equals("parseMedlineBaseline")) {
      ReleaseInfo release =
          data_source.getReleaseInfo(
              (String) request.getParameter("release_name").getValue());
      parseMedlineBaseline(data_source.getDataSourceName(), release);
    }
    else if (function.equals("processMedlineBaseline")) {
      processMedlineBaseline(data_source.getDataSourceName());
    }
    else if (function.equals("updateMedline")) {
      ReleaseInfo release =
          data_source.getReleaseInfo(
              (String) request.getParameter("release_name").getValue());
      updateMedline(data_source.getDataSourceName(),release);
    }
    else if (function.equals("getMedlineStageStatus")) {
      request.addReturnValue(
          new Parameter.Default("status",
                                getMedlineStageStatus( (String) request.
          getParameter("stage_name").getValue())));
    }
    else if (function.equals("getMedlineStatus")) {
      request.addReturnValue(
          new Parameter.Default("status", getMedlineStatus()));
    }
    else if (function.equals("deleteUpdateMedlineXML")) {
        final String file_name =
                (String) request.getParameter("file_name").getValue();
        deleteUpdateMedlineXML(file_name);
    }

  }

  /**
   * builds targets for all active handlers in separate thread
   * if all required targets are built, updates release_history built='Y'
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   * @throws MEMEException if failed to build.
   */
  public void build(String release_name, String service) throws MEMEException {
    final String release = release_name;
    final String service_name = service;
    ServerToolkit.getThread(new Runnable() {
      public void run() {
        buildHelper(release, service_name);
      }
    }).start();
  }

  /**
   * runs qa for previous release on all active handlers in separate thread
   * if all required targets are built, updates release_history built='Y'
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   * @throws MEMEException if failed to run qa.
   */
  public void prevQA(String release_name, String service) throws MEMEException {
    final String release = release_name;
    final String service_name = service;
    ServerToolkit.getThread(new Runnable() {
      public void run() {
        prevQAHelper(release, service_name);
      }
    }).start();
  }

  /**
   * runs gold script counts on all active handlers in separate thread
   * if all required targets are built, updates release_history built='Y'
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   * @throws MEMEException if failed to run qa.
   */
  public void gold(String release_name, String service) throws MEMEException {
    final String release = release_name;
    final String service_name = service;
    ServerToolkit.getThread(new Runnable() {
      public void run() {
        goldHelper(release, service_name);
      }
    }).start();
  }

  /**
   * run the process for all active handlers
   * @param process An object {@link String} representation of process name.
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   * @throws MEMEException if failed to process.
   */
  public void doProcess(String process, String release_name, String service) throws
      MEMEException {
    if ("prevQA".equals(process)) {
      prevQA(release_name, service);
    }
    else if ("gold".equals(process)) {
      gold(release_name, service);
    }
    else if ("build".equals(process)) {
      build(release_name, service);
    }
    else if ("validate".equals(process)) {
      validate(release_name, service);
    }
    else if ("publish".equals(process)) {
      publish(release_name, service);
    }
    else {
      BadValueException bve = new BadValueException("Invalid process value");
      bve.setDetail("process", process);
      throw bve;
    }
  }

  /**
   * a helper method for build
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   */
  public void buildHelper(String release_name, String service) {
    MRDDataSource data_source = null;
    try {
      data_source = ServerToolkit.getMRDDataSource(service, null, null);
      final ReleaseHandler[] handlers = data_source.getReleaseHandlers("Full");
      final ReleaseInfo release_info = data_source.getReleaseInfo(release_name);
      ArrayList target_names = new ArrayList();
      for (int i = 0; i < handlers.length; i++) {
        if (handlers[i].isActive()) {
          target_names.add(handlers[i].getTargetName() + release_name);
          release_target_cache.remove(handlers[i].getTargetName() + release_name);
        }
      }
      Map processes = new HashMap();
      if (stage_processes.containsKey(StageStatus.BUILD)) {
        processes = (HashMap) stage_processes.get(StageStatus.BUILD);
      }
      String parallel = "P" + System.currentTimeMillis();
      processes.put(parallel, target_names);
      stage_processes.put(StageStatus.BUILD, processes);
      for (int i = 0; i < handlers.length; i++) {
        final ReleaseHandler handler = handlers[i];
        if (handler.isActive()) {
          final StringBuffer log = new StringBuffer();
          final String target_name = handler.getTargetName();
          release_target_cache.remove(target_name + release_name);
          PrintWriter writer = null;
          try {
            writer = new PrintWriter(new FileWriter(new File(release_info.
                getBuildUri() + "/log", target_name + ".log")));
            final File qa_log = new File(release_info.getBuildUri() + "/QA",
                                         "qa_" + target_name + ".rpt");
            if (qa_log != null && qa_log.exists()) {
              qa_log.renameTo(new File(release_info.getBuildUri() + "/QA",
                                       "qa_" + target_name +
                                       ".rpt.bak"));
            }
            handler.setLog(log);
            ServerToolkit.logStartTime(target_name + "-BUILD");
            ServerToolkit.logCommentToBuffer("PARALLEL ID " + parallel, true,
                                             log);
            ServerToolkit.logCommentToBuffer("STARTING " + target_name +
                                             " PRODUCTION - " + "Full" +
                                             target_name + "ReleaseHandler", true,
                                             log);
            handler.setDataSource(data_source);
            handler.setReleaseInfo(release_info);
            handler.prepare();
            ServerToolkit.logCommentToBuffer(data_source.flushBuffer(), false,
                                             log);
            ServerToolkit.logCommentToBuffer("GENERATE RELEASE DATA", true, log);
            handler.generate();
            ServerToolkit.logCommentToBuffer("FEEDBACK RELEASE DATA", true, log);
            handler.feedback();
            ServerToolkit.logCommentToBuffer(data_source.flushBuffer(), false,
                                             log);
            ServerToolkit.logCommentToBuffer("FINISHED " + target_name +
                                             " PRODUCTION - " +
                                             ServerToolkit.getElapsedTime(
                                                 target_name + "-BUILD"), true,
                                             log);
            writer.write(log.toString());
            writer.close();
            if (target_name.equals("MRFILESCOLS")) {
              release_info.setIsBuilt(true);
              data_source.setReleaseInfo(release_info);
            }
            target_names.remove(target_name + release_name);
            release_target_cache.remove(target_name + release_name);
          }
          catch (Exception e) {
            log.append("ERROR building " + target_name);
            ServerToolkit.logCommentToBuffer(data_source.flushBuffer(), false,
                                             log);
            try {
              writer.write(log.toString());
              e.printStackTrace(writer);
              writer.close();
            }
            catch (Exception we) {}
            ServerToolkit.handleError(e);
            if (release_info.getAdministrator() != null) {
              try {
                // Set who the mail is from
                final MEMEMail mail = new MEMEMail();
                mail.from =
                		MIDServices.getService(
                				ServerToolkit.getServiceForProperty("mrd.from"));
                if (mail.from.equals("")) {
                  mail.from = "tuttle@nlm.nih.gov";
                }

                // Set who is on the to list, remember it is a String array
                mail.to = new String[] {
                    ( (Authority) release_info.getAdministrator()).toString()};

                // Then, set the mail host
                mail.smtp_host =
                	ServerToolkit.getProperty(
              			ServerToolkit.getServiceForProperty(ServerConstants.MAIL_HOST));
                if (mail.smtp_host.equals("")) {
                  mail.smtp_host =
                  	ServerToolkit.getProperty(ServerConstants.MAIL_HOST);
                }

                // Finally, send your message
                String subject = "Meta" + release_info.getName() + ": " +
                    target_name + " Error Building";
                StringBuffer message = new StringBuffer();
                message.append("Following is the " + target_name + "log.\n\n")
                    .append("--------------------------------")
                    .append("--------------------------------\n\n");
                message.append(log);

                mail.send(subject, message.toString());

              }
              catch (MailException me) {
                // Something went wrong
                ServerToolkit.handleError(me);
              }
            }
          }
          if (release_info.getAdministrator() != null) {
            try {
              // Set who the mail is from
              final MEMEMail mail = new MEMEMail();
              mail.from =
          			MIDServices.getService(
          				ServerToolkit.getServiceForProperty("mrd.from"));
              if (mail.from.equals("")) {
                mail.from = "tuttle@nlm.nih.gov";
              }

              // Set who is on the to list, remember it is a String array
              mail.to = new String[] {
                  ( (Authority) release_info.getAdministrator()).toString()};

              // Then, set the mail host
              mail.smtp_host =
              	ServerToolkit.getProperty(
              			ServerToolkit.getServiceForProperty(ServerConstants.MAIL_HOST));
              if (mail.smtp_host.equals("")) {
                mail.smtp_host =
                	ServerToolkit.getProperty(ServerConstants.MAIL_HOST);
              }

              // Finally, send your message
              String subject = "Meta" + release_info.getName() + ": " +
                  target_name + " Build Complete";
              StringBuffer message = new StringBuffer();
              message.append("Following is the " + target_name + "log.\n\n")
                  .append("--------------------------------")
                  .append("--------------------------------\n\n");
              BufferedReader in = new BufferedReader(new FileReader(
                  new File(release_info.getBuildUri() + "/log",
                           target_name + ".log")));
              String line = null;
              while ( (line = in.readLine()) != null) {
                message.append(line);

              }
              mail.send(subject, message.toString());

            }
            catch (Exception e) {
              // Something went wrong
              ServerToolkit.handleError(e);
            }
          }
        }
      }
      processes.remove(parallel);
    }
    catch (MEMEException dse) {
      ServerToolkit.handleError(dse);
    }
    finally {
      if (data_source != null) {
        try {
          ServerToolkit.returnDataSource(data_source);
        }
        catch (BadValueException bve) {
          ServerToolkit.handleError(bve);
        }
      }
    }
  }

  /**
   * a helper method for previousQA
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   */
  public void prevQAHelper(String release_name, String service) {
    MRDDataSource data_source = null;
    try {
      data_source = ServerToolkit.getMRDDataSource(service, null, null);
      final ReleaseHandler[] handlers = data_source.getReleaseHandlers("Full");
      final ReleaseInfo release_info = data_source.getReleaseInfo(release_name);
      ArrayList target_names = new ArrayList();
      for (int i = 0; i < handlers.length; i++) {
        if (handlers[i].isActive()) {
          target_names.add(handlers[i].getTargetName() + release_name);
          release_target_cache.remove(handlers[i].getTargetName() + release_name);
        }
      }
      Map processes = new HashMap();
      if (stage_processes.containsKey(StageStatus.PREVQA)) {
        processes = (HashMap) stage_processes.get(StageStatus.PREVQA);
      }
      String parallel = "P" + System.currentTimeMillis();
      processes.put(parallel, target_names);
      stage_processes.put(StageStatus.PREVQA, processes);
      for (int i = 0; i < handlers.length; i++) {
        if (handlers[i].isActive()) {
          final ReleaseHandler handler = handlers[i];
          final StringBuffer log = new StringBuffer();
          final String target_name = handler.getTargetName();
          final String mrd_home = ServerToolkit.getProperty(ServerConstants.
              MRD_HOME);
          release_target_cache.remove(target_name + release_name);
          PrintWriter writer = null;
          try {
            final File file = new File(release_info.getBuildUri() + "/QA",
                                       "prev_" + target_name + ".log");
            writer = new PrintWriter(new BufferedWriter(new FileWriter(
                file)));
            try {
              String dir = "/META";
              if (target_name.equals("ORF")) {
                dir = "/METAO";
              }
              final String qa_log =
                  ServerToolkit.exec(
                      new String[] {
                      mrd_home + "/bin/qa_previous.csh",
                      data_source.getDataSourceName(),
                      release_info.getPreviousReleaseInfo().getBuildUri() + dir,
                      release_info.getPreviousReleaseInfo().getName(),
                      release_info.getPreviousMajorReleaseInfo().getBuildUri() +
                      dir,
                      release_info.getPreviousMajorReleaseInfo().getName(),
                      target_name}, new String[0],
                      new File(release_info.getBuildUri() + "/QA"));
              writer.println("PARALLEL ID " + parallel);
              writer.println(qa_log);
              target_names.remove(target_name + release_name);
            }
            catch (ExecException exece) {
              log.append("ERROR running qa_preivous.csh on " + target_name);
              ServerToolkit.logCommentToBuffer(data_source.flushBuffer(), false,
                                               log);
              writer.write(log.toString());
              writer.println(exece.toString());
              writer.close();
              ServerToolkit.handleError(exece);
            }
            writer.close();
            release_target_cache.remove(target_name + release_name);
          }
          catch (IOException ioe) {
            ServerToolkit.handleError(ioe);
          }
        }
      }
      processes.remove(parallel);
    }
    catch (MEMEException dse) {
      ServerToolkit.handleError(dse);
    }
    finally {
      if (data_source != null) {
        try {
          ServerToolkit.returnDataSource(data_source);
        }
        catch (BadValueException bve) {
          ServerToolkit.handleError(bve);
        }
      }
    }
  }

  /**
   * a helper method for gold
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   */
  public void goldHelper(String release_name, String service) {
    MRDDataSource data_source = null;
    try {
      data_source = ServerToolkit.getMRDDataSource(service, null, null);
      final ReleaseHandler[] handlers = data_source.getReleaseHandlers("Full");
      final ReleaseInfo release_info = data_source.getReleaseInfo(release_name);
      ArrayList target_names = new ArrayList();
      for (int i = 0; i < handlers.length; i++) {
        if (handlers[i].isActive()) {
          target_names.add(handlers[i].getTargetName() + release_name);
          release_target_cache.remove(handlers[i].getTargetName() + release_name);
        }
      }
      Map processes = new HashMap();
      if (stage_processes.containsKey(StageStatus.GOLD)) {
        processes = (HashMap) stage_processes.get(StageStatus.GOLD);
      }
      String parallel = "P" + System.currentTimeMillis();
      processes.put(parallel, target_names);
      stage_processes.put(StageStatus.GOLD, processes);
      for (int i = 0; i < handlers.length; i++) {
        final ReleaseHandler handler = handlers[i];
        if (handlers[i].isActive()) {
          final StringBuffer log = new StringBuffer();
          final String target_name = handler.getTargetName();
          final String mrd_home = ServerToolkit.getProperty(ServerConstants.
              MRD_HOME);
          release_target_cache.remove(target_name + release_name);
          PrintWriter writer = null;
          try {
            final File file = new File(release_info.getBuildUri() + "/QA",
                                       "gold_" + target_name + ".log");
            writer = new PrintWriter(new BufferedWriter(new FileWriter(
                file)));
            try {
              final String qa_log =
                  ServerToolkit.exec(
                      new String[] {
                      mrd_home + "/bin/gold_script.csh",
                      "-" + target_name.toLowerCase(),
                      data_source.getDataSourceName(),
                      release_info.getName(),
                      release_info.getPreviousReleaseInfo().getName()},
                      new String[0],
                      new File(release_info.getBuildUri() + "/QA"));
              writer.println("PARALLEL ID " + parallel);
              writer.println(qa_log);
              target_names.remove(target_name + release_name);
              release_target_cache.remove(target_name + release_name);
            }
            catch (ExecException exece) {
              log.append("ERROR running gold_script.csh on " + target_name);
              ServerToolkit.logCommentToBuffer(data_source.flushBuffer(), false,
                                               log);
              writer.write(log.toString());
              exece.printStackTrace(writer);
              ServerToolkit.handleError(exece);
            }
            writer.close();
            release_target_cache.remove(target_name + release_name);
          }
          catch (IOException ioe) {
            ServerToolkit.handleError(ioe);
          }
        }
      }
      processes.remove(parallel);
    }
    catch (MEMEException dse) {
      ServerToolkit.handleError(dse);
    }
    finally {
      if (data_source != null) {
        try {
          ServerToolkit.returnDataSource(data_source);
        }
        catch (BadValueException bve) {
          ServerToolkit.handleError(bve);
        }
      }
    }
  }

  /**
   * a helper method for validate
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   */
  public void validateHelper(String release_name, String service) {
    MRDDataSource data_source = null;
    try {
      data_source = ServerToolkit.getMRDDataSource(service, null, null);
      final ReleaseHandler[] handlers = data_source.getReleaseHandlers("Full");
      final ReleaseInfo release_info = data_source.getReleaseInfo(release_name);
      final String mrd_home = ServerToolkit.getProperty(ServerConstants.
          MRD_HOME);
      ArrayList target_names = new ArrayList();
      for (int i = 0; i < handlers.length; i++) {
        if (handlers[i].isActive()) {
          target_names.add(handlers[i].getTargetName() + release_name);
          release_target_cache.remove(handlers[i].getTargetName() + release_name);
        }
      }
      Map processes = new HashMap();
      if (stage_processes.containsKey(StageStatus.VALIDATE)) {
        processes = (HashMap) stage_processes.get(StageStatus.VALIDATE);
      }
      String parallel = "P" + System.currentTimeMillis();
      processes.put(parallel, target_names);
      stage_processes.put(StageStatus.VALIDATE, processes);
      for (int i = 0; i < handlers.length; i++) {
        final ReleaseHandler handler = handlers[i];
        if (handler.isActive()) {
          final StringBuffer log = new StringBuffer();
          final String target_name = handler.getTargetName();
          release_target_cache.remove(target_name + release_name);

          try {
            final File file = new File(release_info.getBuildUri() + "/QA",
                                       "qa_" + target_name + ".rpt");
            final PrintWriter out = new PrintWriter(new BufferedWriter(new
                FileWriter(
                    file)));
            try {
              String dir = "/META ";
              if (target_name.equals("ORF")) {
                dir = "/METAO ";
              }
              final String qa_log =
                  ServerToolkit.exec(
                      new String[] {
                      mrd_home + "/bin/qa_report.csh",
                      release_info.getBuildUri() + dir,
                      data_source.getDataSourceName(),
                      release_info.getName(),
                      target_name,
                      release_info.getPreviousMajorReleaseInfo().getName(),
                      release_info.getPreviousReleaseInfo().getName(),
                      release_info.getPreviousReleaseInfo().getBuildUri() + dir
              },
                  new String[0],
                  new File(release_info.getBuildUri() + "/QA"));
              out.println("PARALLEL ID " + parallel);
              out.println(qa_log);
              target_names.remove(target_name + release_name);
            }
            catch (ExecException exece) {
              log.append("ERROR validating on " + target_name);
              ServerToolkit.logCommentToBuffer(data_source.flushBuffer(), false,
                                               log);
              out.write(log.toString());
              exece.printStackTrace(out);
              ServerToolkit.handleError(exece);
            }
            out.close();
            release_target_cache.remove(target_name + release_name);
          }
          catch (IOException ioe) {
            ServerToolkit.handleError(ioe);
          }
        }
      }
      processes.remove(parallel);
    }
    catch (MEMEException dse) {
      ServerToolkit.handleError(dse);
    }
    finally {
      if (data_source != null) {
        try {
          ServerToolkit.returnDataSource(data_source);
        }
        catch (BadValueException bve) {
          ServerToolkit.handleError(bve);
        }
      }
    }
  }

  /**
   * validate the target files
   * @param release An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   * @throws MEMEException if failed to validate.
   */
  public void validate(String release, String service) throws MEMEException {
    final String release_name = release;
    final String service_name = service;
    ServerToolkit.getThread(new Runnable() {
      public void run() {
        validateHelper(release_name, service_name);
      }
    }).start();
  }

  /**
   * Build directory structure (2002AC 2002AC/META 2002AC/QA 2002AC/log)
   * Run the qa for previous release
   * Insert a row into release_history table (delete/insert)
   * @param release An object {@link ReleaseInfo} representation of release name.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @throws MEMEException if failed to validate.
   */
  public void prepareRelease(ReleaseInfo release, MRDDataSource data_source) throws
      MEMEException {
    final ReleaseInfo release_info = release;
    File file = new File(release_info.getBuildUri());
    if (file.exists()) {
      ServerToolkit.exec(new String[] {"/bin/rm", "-r", "-f",
                         release_info.getBuildUri()});
    }
    file.mkdir();
    file = new File(release_info.getBuildUri() + File.separator + "META");
    file.mkdir();
    file = new File(release_info.getBuildUri() + File.separator + "META" +
                    File.separator + "CHANGE");
    file.mkdir();
    file = new File(release_info.getBuildUri() + File.separator + "QA");
    file.mkdir();
    file = new File(release_info.getBuildUri() + File.separator + "log");
    file.mkdir();

    //
    // Make documentation host directory
    //
    /***** This is disabled due to security issues related to RSH use
    ServerToolkit.exec(
        new String[] {
        "/bin/rsh", release_info.getDocumentationHost(),
        "/bin/mkdir " + release_info.getDocumentationUri()}
        ,
        new String[] {}
        ,
        true,
        ServerConstants.USE_INPUT_STREAM,
        false);
     *********/
    data_source.removeReleaseInfo(release_info);
    data_source.addReleaseInfo(release_info);

  }

  /**
   * Prepare the "log/Finished.log" file for the release.
   * @param release the release name
   * @param data_source the {@link MRDDataSource}
   * @throws MEMEException if failed to finish release
   */
  public void finishRelease(ReleaseInfo release,
		  MRDDataSource data_source) throws MEMEException {
	  try {
		  File file = new File(release.getBuildUri() + "/log/Finished.log");
		  final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
              file)));
          final StringBuffer log = new StringBuffer();
		  final String mrd_home = ServerToolkit.getProperty(ServerConstants.
              MRD_HOME);
          ServerToolkit.logStartTime("FINISH RELEASE");
          ServerToolkit.logCommentToBuffer("STARTING FINISH RELEASE" , true,
                                           log);
          ServerToolkit.logCommentToBuffer("The following QA Report files are generated on " + release.getDocumentationUri() + ":" , true,
                  log);
          File docuri = new File(release.getDocumentationUri());
          String[] files = docuri.list(new FilenameFilter() {
        	    public boolean accept(File dir, String name) {
        	        return (name.startsWith("qa_"));
        	    }
        	}
          );
          for(int i=0; i<files.length; i++) {
        	  ServerToolkit.logCommentToBuffer("\t" + files[i], true, log);
          }
	      final String copy_log =
		  ServerToolkit.exec(new String[] {
                             mrd_home +
                             "/bin/copy_qareasons.csh",
                             data_source.getDataSourceName()}
                             ,
                             new String[] {"MRD_HOME=" +
                             mrd_home,
                             "ORACLE_HOME=" +
                             ServerToolkit.
                             getProperty(ServerConstants.
                                         ORACLE_HOME)}
                             );
	      ServerToolkit.logCommentToBuffer(copy_log, true, log);
          ServerToolkit.logCommentToBuffer("FINISHED RELEASE - " +
                  ServerToolkit.getElapsedTime(
                      "FINISH RELEASE"), true,
                  log);
          out.write(log.toString());
	      out.close();
	  }
	  catch (IOException ioe) {
		  final ExternalResourceException ere = new ExternalResourceException(
	      "Failed to crete the finish log file ", ioe);
	  	ere.setDetail("release", release.getBuildUri());
	  	throw ere;
	
	  }
}

  /**
   * verifies that upload was correct
   * builds MMS config file
   * builds MMS DISTRIBUTION
   * uploads files to release_host in release_uri
   * set published = 'Y' in release_history
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   * @throws MEMEException if failed to publish targets.
   */
  public void publish(String release_name, String service) throws MEMEException {
    final String release = release_name;
    final String service_name = service;
    ServerToolkit.getThread(new Runnable() {
      public void run() {
        publishHelper(release, service_name);
      }
    }).start();
  }

  /**
   * a helper method for publish
   * @param release_name An object {@link String} representation of release name.
   * @param service An object {@link String} representation of service.
   */
  public void publishHelper(String release_name, String service) {
    MRDDataSource data_source = null;
    try {
      data_source = ServerToolkit.getMRDDataSource(service, null, null);
      final ReleaseInfo release_info = data_source.getReleaseInfo(release_name);
      PrintWriter writer = null;
      final ReleaseHandler[] handlers =
          data_source.getReleaseHandlers("Full");
      ArrayList target_names = new ArrayList();
      for (int i = 0; i < handlers.length; i++) {
        if (handlers[i].isActive()) {
          target_names.add(handlers[i].getTargetName() + release_name);
          release_target_cache.remove(handlers[i].getTargetName() + release_name);
        }
      }
      Map processes = new HashMap();
      if (stage_processes.containsKey(StageStatus.PUBLISH)) {
        processes = (HashMap) stage_processes.get(StageStatus.PUBLISH);
      }
      String parallel = "P" + System.currentTimeMillis();
      processes.put(parallel, target_names);
      stage_processes.put(StageStatus.PUBLISH, processes);
      for (int i = 0; i < handlers.length; i++) {
        if (handlers[i].isActive()) {
          final StringBuffer log = new StringBuffer();
          try {
            final String target_name = handlers[i].getTargetName();
            release_target_cache.remove(target_name + release_name);
            ServerToolkit.logStartTime(target_name + "-PUBLISH");
            writer = new PrintWriter(new FileWriter(new File(release_info.
                getBuildUri() + "/log", "publish_" + target_name + ".log")));
            ServerToolkit.logCommentToBuffer("PARALLEL ID " + parallel, true,
                                             log);
            ServerToolkit.logCommentToBuffer("COPYING " + target_name, true,
                                             log);
            handlers[i].setLog(log);
            handlers[i].setDataSource(data_source);
            handlers[i].setReleaseInfo(release_info);
            if (!handlers[i].publish()) {
              final String[] target_files = handlers[i].getFiles();
              for (int j = 0; j < target_files.length; j++) {
                final String target_file = target_files[j];
                ServerToolkit.exec(
                    new String[] {
                    "/bin/rcp",
                    release_info.getBuildUri() + "/META/" + target_file +
                    ".RRF",
                    release_info.getReleaseHost() + ":" +
                    release_info.getReleaseUri() +
                    "/MASTER/" + release_info.getName() + "/META/" +
                    target_file +
                    ".RRF"});
                final String local_digest = localDigest(
                    release_info.getBuildUri() + "/META/" + target_file +
                    ".RRF");
                ServerToolkit.logCommentToBuffer("MD5 digest " +
                                                 release_info.getBuildUri() +
                                                 "/META/" + target_file +
                                                 " - " + local_digest, true,
                                                 log);

                final String remote_digest = remoteDigest(
                    release_info.getReleaseUri() + "/MASTER/" +
                    release_info.getName() + "/META/" + target_file + ".RRF",
                    release_info.getReleaseHost());
                ServerToolkit.logCommentToBuffer("MD5 digest " +
                                                 release_info.getReleaseUri() +
                                                 "/MASTER/" +
                                                 release_info.getName() +
                                                 "/META/" + target_file +
                                                 " - " + remote_digest, true,
                                                 log);

                if (!local_digest.equals(remote_digest)) {
                  throw new Exception(
                      "Remote digest does not match local digest.");
                }
              }

            }

            ServerToolkit.logCommentToBuffer("FINISHED COPYING " +
                                             target_name + "-" +
                                             ServerToolkit.getElapsedTime(
                                                 target_name + "-PUBLISH"), true,
                                             log);
            writer.write(log.toString());
            writer.flush();
            writer.close();
            release_info.setIsPublished(true);
            data_source.setReleaseInfo(release_info);
            target_names.remove(target_name + release_name);
            release_target_cache.remove(target_name + release_name);
          }
          catch (Exception e) {
            log.append("ERROR publishing META files");
            try {
              ServerToolkit.logCommentToBuffer(data_source.flushBuffer(), false,
                                               log);
              writer.write(log.toString());
              writer.flush();
              e.printStackTrace(writer);
              writer.close();
            }
            catch (Exception we) {}
            ServerToolkit.handleError(e);
            if (release_info.getAdministrator() != null) {
              try {
                // Set who the mail is from
                final MEMEMail mail = new MEMEMail();
                mail.from =
                	MIDServices.getService(
                			ServerToolkit.getServiceForProperty("mrd.from"));
                if (mail.from.equals("")) {
                  mail.from = "tuttle@nlm.nih.gov";
                }

                // Set who is on the to list, remember it is a String array
                mail.to = new String[] {
                    ( (Authority) release_info.getAdministrator()).toString()};

                // Then, set the mail host
                mail.smtp_host =
                	ServerToolkit.getProperty(
                			ServerToolkit.getServiceForProperty(ServerConstants.MAIL_HOST));
                if (mail.smtp_host.equals("")) {
                  mail.smtp_host =
                  	ServerToolkit.getProperty(ServerConstants.MAIL_HOST);
                }

                // Finally, send your message
                String subject = "Meta" + release_info.getName() + ": " +
                    " Error Publishing";
                StringBuffer message = new StringBuffer();
                message.append("Following is the publish log.\n\n")
                    .append("--------------------------------")
                    .append("--------------------------------\n\n");
                message.append(log);

                mail.send(subject, message.toString());

              }
              catch (MailException me) {
                // Something went wrong
                ServerToolkit.handleError(me);
              }
            }
          }
        }
      }
      processes.remove(parallel);
      if (release_info.getAdministrator() != null) {
        try {
          // Set who the mail is from
          final MEMEMail mail = new MEMEMail();
          mail.from =
          	MIDServices.getService(
        			ServerToolkit.getServiceForProperty("mrd.from"));
          if (mail.from.equals("")) {
            mail.from = "tuttle@nlm.nih.gov";
          }

          // Set who is on the to list, remember it is a String array
          mail.to = new String[] {
              ( (Authority) release_info.getAdministrator()).toString()};

          // Then, set the mail host
          mail.smtp_host =
          	ServerToolkit.getProperty(
          			ServerToolkit.getServiceForProperty(ServerConstants.MAIL_HOST));
          if (mail.smtp_host.equals("")) {
            mail.smtp_host =
            	ServerToolkit.getProperty(ServerConstants.MAIL_HOST);
          }

          // Finally, send your message
          String subject = "Meta" + release_info.getName() + ": " +
              " Publish Complete";
          StringBuffer message = new StringBuffer();
          message.append("Following is the publish log.\n\n")
              .append("--------------------------------")
              .append("--------------------------------\n\n");
          for (int i = 0; i < handlers.length; i++) {
            BufferedReader in = new BufferedReader(new FileReader(
                new File(release_info.getBuildUri() + "/log",
                         "publish_" + handlers[i].getTargetName() + ".log")));
            String line = null;
            while ( (line = in.readLine()) != null) {
              message.append(line);

            }
          }
          mail.send(subject, message.toString());

        }
        catch (Exception e) {
          // Something went wrong
          ServerToolkit.handleError(e);
        }
      }
    }
    catch (MEMEException dse) {
      ServerToolkit.handleError(dse);
    }
    finally {
      if (data_source != null) {
        try {
          ServerToolkit.returnDataSource(data_source);
        }
        catch (BadValueException bve) {
          ServerToolkit.handleError(bve);
        }
      }
    }
  }

  /**
   * Returns the first n lines of the target.
   * @param release the release name
   * @param data_source the {@link MRDDataSource}
   * @param target_name the target name
   * @param lines number of lines
   * @return the first n lines of the target
   * @throws MEMEException if failed to preview target
   */
  public String previewTarget(ReleaseInfo release, MRDDataSource data_source,
                              String target_name, int lines) throws
      MEMEException {
    final ReleaseHandler handler =
        data_source.getReleaseHandler("Full", target_name);
    handler.setDataSource(data_source);
    handler.setReleaseInfo(release);
    final String preview = handler.preview(lines);
    if (preview != null) {
      return preview;
    }
    final String[] target_files = handler.getFiles();
    String line = null;
    final StringBuffer sb = new StringBuffer();
    for (int i = 0; i < target_files.length; i++) {
      try {
        sb.append("Target file: ").append(target_files[i]).append("\n");
        final BufferedReader in = new BufferedReader(new FileReader(
            new File(release.getBuildUri() + "/META/" + target_files[i] +
                     ".RRF")));
        int count = 0;
        while ( (line = in.readLine()) != null && count++ < lines) {
          sb.append(line).append("\n");
        }
        sb.append("\n");
      }
      catch (Exception e) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to read the target for the preview ", e);
        ere.setDetail("file",
                      release.getBuildUri() + "/META/" + target_files[i] +
                      ".RRF");
        throw ere;
      }
    }
    return sb.toString();

  }

  /**
   * Get the target with the status only
   * @param release_name An object {@link String} representation of release name.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @param target_name An object {@link ReleaseInfo} representation of target name.
   * @return An object {@link ReleaseTarget} representation of target.
   * @throws MEMEException if failed to get target.
   */
  public ReleaseTarget getTargetStatus(String release_name, String target_name,
                                       MRDDataSource data_source) throws
      MEMEException {
    ReleaseTarget target;
    if (release_target_cache.containsKey(target_name + release_name)) {
      target = (ReleaseTarget) release_target_cache.get(target_name +
          release_name);
    }
    else {
      target = getTarget(release_name, target_name, data_source);
      release_target_cache.put(target_name + release_name, target);
    }
    return target.newInstanceWithStatus();

  }

  /**
   * Get the targets without the stage status and QAReport
   * @param release_name An object {@link String} representation of release name.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @return An object {@link ReleaseTarget} representation of target.
   * @throws MEMEException if failed to get target.
   */
  public ReleaseTarget[] getTargets(String release_name,
                                    MRDDataSource data_source) throws
      MEMEException {
    final String[] targets = getTargetNames(release_name, data_source);
    final ReleaseTarget[] release_targets = new ReleaseTarget[targets.length];
    for (int i = 0; i < targets.length; i++) {
      ReleaseTarget target;
      if (release_target_cache.containsKey(targets[i] + release_name)) {
        target = (ReleaseTarget) release_target_cache.get(targets[i] + release_name);
      }
      else {
        target = getTarget(release_name, targets[i], data_source);
      }
      release_targets[i] = target.newInstance();
    }
    return release_targets;
  }

  /**
   * Get the target with need review only qareport
   * @param release_name An object {@link String} representation of release name.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @param target_name An object {@link ReleaseInfo} representation of target name.
   * @return An object {@link ReleaseTarget} representation of target.
   * @throws MEMEException if failed to get target.
   */
  public ReleaseTarget getTargetQAReport(String release_name,
                                         String target_name,
                                         MRDDataSource data_source) throws
      MEMEException {
    ReleaseTarget target;
    if (release_target_cache.containsKey(target_name + release_name)) {
      target = (ReleaseTarget) release_target_cache.get(target_name + release_name);
    }
    else {
      target = getTarget(release_name, target_name, data_source);
    }
    return target.newInstanceWithQAReport();
  }

  /**
   * Get the CuiComparisonReport
   * @param cui An object {@link String} representation of cui.
   * @param release An object {@link ReleaseInfo} representation of ReleaseInfo.
   * @return object {@link QAReport} representation of cui comparioson report.
   * @throws MEMEException if failed to remove reason.
   */

  public QAReport getCuiComparisonReport(String cui, ReleaseInfo release) throws
      MEMEException {
    final QAReportParser parser = new QAReportParser();
    final QAReport report =
        parser.parse("1.5",
                     release.getBuildUri() + "/QA/" + cui + ".xml");
    report.setName(cui);
    return report;
  }

  /**
   * Generate the CuiComparisonReport
   * @param cui An object {@link String} representation of cui.
   * @param current An object {@link ReleaseInfo} representation of ReleaseInfo.
   * @param compareTo An object {@link ReleaseInfo} representation of ReleaseInfo.
   * @throws MEMEException if failed to remove reason.
   */

  public void generateCuiComparisonReport(String cui, ReleaseInfo current,
                                          ReleaseInfo compareTo) throws
      MEMEException {
    try {
      final File file = new File(current.getBuildUri() + "/QA", cui + ".xml");
      final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
          file)));
      final String mrd_home = ServerToolkit.getProperty(ServerConstants.
          MRD_HOME);
      final String qa_log =
          ServerToolkit.exec(
              new String[] {
              mrd_home + "/bin/cui_compare.csh",
              cui,
              compareTo.getName(),
              current.getName(),
              compareTo.getBuildUri() + "/META",
              current.getBuildUri() + "/META"
      },
          new String[0],
          new File(current.getBuildUri() + "/QA"));
      out.println(qa_log);
      out.close();
    }
    catch (IOException ioe) {
      final ExternalResourceException ere = new ExternalResourceException(
          "Failed to generate the report for the cui ", ioe);
      ere.setDetail("cui", cui);
      throw ere;

    }
  }

  /**
   * Get the target
   * @param release_name An object {@link String} representation of release name.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @param target_name An object {@link ReleaseInfo} representation of target name.
   * @return An object {@link ReleaseTarget} representation of target.
   * @throws MEMEException if failed to get target.
   */
  public ReleaseTarget getTarget(String release_name, String target_name,
                                 MRDDataSource data_source) throws
      MEMEException {
    final ReleaseTarget target = new ReleaseTarget();
    target.setName(target_name);
    final ReleaseInfo release_info = data_source.getReleaseInfo(release_name);
    final ReleaseHandler release_handler =
        data_source.getReleaseHandler("Full", target_name);
    target.setIsActive(release_handler.isActive());
    target.setDependencies(release_handler.getDependencies());
    target.setReleaseInfo(release_info);
    final StageStatus[] status = new StageStatus[5];
    try {
      final File file = new File(release_info.getBuildUri() + "/QA",
                                 "prev_" + target_name + ".log");
      final StageStatus stage = new StageStatus(StageStatus.PREVQA);
      stage.setTargetName(target.getName());
      stage.setCode(StageStatus.NONE);
      if (file.exists()) {
        stage.setCode(StageStatus.RUNNING);
        stage.setStartTime(new Date(file.lastModified()));
        final BufferedReader in = new BufferedReader(new FileReader(file));
        String line = null;
        final StringBuffer sb = new StringBuffer();
        while ( (line = in.readLine()) != null) {
          if (line.indexOf("Starting") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss z yyyy");
            try {
              stage.setStartTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "... ") + 4)));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid start time in log file " +
                                          file.getName(), e);
            }
          }
          if (line.indexOf("ERROR") != -1) {
            stage.setCode(StageStatus.ERROR);
          }
          if (line.indexOf("Finished") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss z yyyy");
            try {
              stage.setEndTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "... ") + 4)));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid end time in log file " +
                                          file.getName(), e);
            }
            stage.setCode(StageStatus.FINISHED);
          }
          if (line.indexOf("PARALLEL ID") != -1) {
            stage.setParallelIdentifier(line.substring(line.indexOf(
                "PARALLEL ID ") + 12));
          }
          sb.append(line).append("\n");
        }
        stage.setLog(sb.toString());
      }
      if (stage_processes.containsKey(StageStatus.PREVQA)) {
        Map processes = (HashMap) stage_processes.get(StageStatus.PREVQA);
        for (Iterator iterator = processes.keySet().iterator();
             iterator.hasNext(); ) {
          String parallel = (String) iterator.next();
          ArrayList target_names = (ArrayList) processes.get(parallel);
          if (target_names.contains(target_name + release_name)) {
            if ( ( (stage.getCode() | StageStatus.RUNNING) !=
                  StageStatus.RUNNING) ||
                ( (stage.getCode() | StageStatus.NONE) == StageStatus.NONE)
                ) {
              stage.setCode(StageStatus.QUEUED);
            }
            stage.setParallelIdentifier(parallel);
          }
        }
      }
      status[0] = stage;
    }
    catch (IOException e) {
      final ExternalResourceException ere = new ExternalResourceException(
          "Failed to read the log for the target gold", e);
      ere.setDetail("file",
                    release_info.getBuildUri() + "/QA/qa_" +
                    target_name + "_" +
                    release_info.getPreviousReleaseInfo().getName());
      throw ere;
    }
    try {
      final File file = new File(release_info.getBuildUri() + "/QA",
                                 "gold_" + target_name + ".log");
      final StageStatus stage = new StageStatus(StageStatus.GOLD);
      stage.setTargetName(target.getName());
      stage.setCode(StageStatus.NONE);
      if (file.exists()) {
        stage.setCode(StageStatus.RUNNING);
        stage.setStartTime(new Date(file.lastModified()));
        final BufferedReader in = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuffer sb = new StringBuffer();
        while ( (line = in.readLine()) != null) {
          if (line.indexOf("Starting") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss z yyyy");
            try {
              stage.setStartTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "... ") + 4)));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid start time in log file " +
                                          file.getName(), e);
            }
          }
          if (line.indexOf("ERROR") != -1) {
            stage.setCode(StageStatus.ERROR);
          }
          if (line.indexOf("Finished") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss z yyyy");
            try {
              stage.setEndTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "... ") + 4)));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid end time in log file " +
                                          file.getName(), e);
            }
            stage.setCode(StageStatus.FINISHED);
          }
          if (line.indexOf("PARALLEL ID") != -1) {
            stage.setParallelIdentifier(line.substring(line.indexOf(
                "PARALLEL ID ") + 12));
          }
          sb.append(line).append("\n");
        }
        stage.setLog(sb.toString());
      }
      if (stage_processes.containsKey(StageStatus.GOLD)) {
        Map processes = (HashMap) stage_processes.get(StageStatus.GOLD);
        for (Iterator iterator = processes.keySet().iterator();
             iterator.hasNext(); ) {
          String parallel = (String) iterator.next();
          ArrayList target_names = (ArrayList) processes.get(parallel);
          if (target_names.contains(target_name + release_name)) {
            if ( ( (stage.getCode() | StageStatus.RUNNING) !=
                  StageStatus.RUNNING) ||
                ( (stage.getCode() | StageStatus.NONE) == StageStatus.NONE)
                ) {
              stage.setCode(StageStatus.QUEUED);
            }
            stage.setParallelIdentifier(parallel);
          }
        }
      }
      status[1] = stage;
    }
    catch (IOException e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to read the log for the target gold", e);
      ere.setDetail("file",
                    release_info.getBuildUri() + "/QA" + target_name +
                    "_gold.log");
      throw ere;
    }
    try {
      final File file = new File(release_info.getBuildUri() + "/log",
                                 target_name + ".log");
      final StageStatus stage = new StageStatus(StageStatus.BUILD);
      stage.setTargetName(target.getName());
      stage.setCode(StageStatus.NONE);
      if (file.exists()) {
        stage.setCode(StageStatus.RUNNING);
        stage.setStartTime(new Date(file.lastModified()));
        final BufferedReader in = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuffer sb = new StringBuffer();
        while ( (line = in.readLine()) != null) {
          if (line.indexOf("STARTING") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "dd-MMM-yyyy HH:mm:ss");
            try {
              stage.setStartTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "[") + 1, line.indexOf("]"))));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid start time in log file " +
                                          file.getName(), e);
            }
          }
          if (line.indexOf("ERROR") != -1) {
            stage.setCode(StageStatus.ERROR);
          }
          if (line.indexOf("FINISHED") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "dd-MMM-yyyy HH:mm:ss");
            try {
              stage.setEndTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "[") + 1, line.indexOf("]"))));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid end time in log file " +
                                          file.getName(), e);
            }
            stage.setCode(StageStatus.FINISHED);
          }
          if (line.indexOf("PARALLEL ID") != -1) {
            stage.setParallelIdentifier(line.substring(line.indexOf(
                "PARALLEL ID ") + 12));
          }
          sb.append(line).append("\n");
        }
        stage.setLog(sb.toString());
      }
      if (stage_processes.containsKey(StageStatus.BUILD)) {
        Map processes = (HashMap) stage_processes.get(StageStatus.BUILD);
        for (Iterator iterator = processes.keySet().iterator();
             iterator.hasNext(); ) {
          String parallel = (String) iterator.next();
          ArrayList target_names = (ArrayList) processes.get(parallel);
          if (target_names.contains(target_name + release_name)) {
            if ( ( (stage.getCode() | StageStatus.RUNNING) !=
                  StageStatus.RUNNING) ||
                ( (stage.getCode() | StageStatus.NONE) == StageStatus.NONE)
                ) {
              stage.setCode(StageStatus.QUEUED);
            }
            stage.setParallelIdentifier(parallel);
          }
        }
      }
      status[2] = stage;
    }
    catch (IOException e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to read the log for the target build", e);
      ere.setDetail("file",
                    release_info.getBuildUri() + "/log/" + target_name +
                    ".log");
      throw ere;
    }
    try {
      final File file = new File(release_info.getBuildUri() + "/QA",
                                 "qa_" + target_name + ".rpt");
      final StageStatus stage = new StageStatus(StageStatus.VALIDATE);
      stage.setTargetName(target.getName());
      stage.setCode(StageStatus.NONE);
      if (file.exists()) {
        stage.setCode(StageStatus.RUNNING);
        stage.setStartTime(new Date(file.lastModified()));
        final BufferedReader in = new BufferedReader(new FileReader(file));

        String line = null;
        StringBuffer sb = new StringBuffer();
        while ( (line = in.readLine()) != null) {
          if (line.indexOf("Starting") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss z yyyy");
            try {
              stage.setStartTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "... ") + 4)));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid start time in log file " +
                                          file.getName(), e);
            }
          }
          if (line.indexOf("ERROR") != -1) {
            stage.setCode(StageStatus.ERROR);
          }
          if (line.indexOf("Finished") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "EEE MMM d HH:mm:ss z yyyy");
            try {
              stage.setEndTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "... ") + 4)));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid end time in log file " +
                                          file.getName(), e);
            }
            stage.setCode(StageStatus.FINISHED);
          }
          if (line.indexOf("PARALLEL ID") != -1) {
            stage.setParallelIdentifier(line.substring(line.indexOf(
                "PARALLEL ID ") + 12));
          }
          sb.append(line).append("\n");
        }
        if ( (stage.getCode() & StageStatus.ERROR) == StageStatus.ERROR) {
          stage.setLog(sb.toString());
        }
        if ( (stage.getCode() & StageStatus.FINISHED) == StageStatus.FINISHED) {
          final QAReportParser parser = new QAReportParser();
          final QAReport report = parser.parse("1.5",
                                               release_info.getBuildUri() +
                                               "/QA/qa_" +
                                               target_name + ".xml");
          if (report != null) {
            target.setQAReport(report);
            if (target.needsReviewQAReport()) {
              stage.setCode(StageStatus.NEEDSREVIEW);
            }
          }
          target.setQAResults(data_source.getQAResults(release_name,
              target_name));
        }
      }
      if (stage_processes.containsKey(StageStatus.VALIDATE)) {
        Map processes = (HashMap) stage_processes.get(StageStatus.VALIDATE);
        for (Iterator iterator = processes.keySet().iterator();
             iterator.hasNext(); ) {
          String parallel = (String) iterator.next();
          ArrayList target_names = (ArrayList) processes.get(parallel);
          if (target_names.contains(target_name + release_name)) {
            if ( ( (stage.getCode() | StageStatus.RUNNING) !=
                  StageStatus.RUNNING) ||
                ( (stage.getCode() | StageStatus.NONE) == StageStatus.NONE)
                ) {
              stage.setCode(StageStatus.QUEUED);
            }
            stage.setParallelIdentifier(parallel);
          }
        }
      }
      status[3] = stage;
    }
    catch (IOException e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to read the qa report for the target build ", e);
      ere.setDetail("file",
                    release_info.getBuildUri() + "/QA/qa_" +
                    target_name + ".rpt");
      throw ere;
    }
    try {
      final File file = new File(release_info.getBuildUri() + "/log/publish_" +
                                 target_name + ".log");
      final StageStatus stage = new StageStatus(StageStatus.PUBLISH);
      stage.setTargetName(target.getName());
      stage.setCode(StageStatus.NONE);
      if (file.exists()) {
        stage.setCode(StageStatus.RUNNING);
        stage.setStartTime(new Date(file.lastModified()));
        final BufferedReader in = new BufferedReader(new FileReader(file));

        String line = null;
        final StringBuffer sb = new StringBuffer();
        while ( (line = in.readLine()) != null) {
          if (line.indexOf("COPYING") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "dd-MMM-yyyy HH:mm:ss");
            try {
              stage.setStartTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "[") + 1, line.indexOf("]"))));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid start time in log file " +
                                          file.getName(), e);
            }
          }
          if (line.indexOf("ERROR") != -1) {
            stage.setCode(StageStatus.ERROR);
          }
          if (line.indexOf("FINISHED") != -1) {
            DateFormat dateformat = new SimpleDateFormat(
                "dd-MMM-yyyy HH:mm:ss");
            try {
              stage.setEndTime(dateformat.parse(line.substring(line.
                  indexOf(
                      "[") + 1, line.indexOf("]"))));
            }
            catch (ParseException e) {
              throw new BadValueException("Invalid end time in log file " +
                                          file.getName(), e);
            }
            stage.setCode(StageStatus.FINISHED);
          }
          if (line.indexOf("PARALLEL ID") != -1) {
            stage.setParallelIdentifier(line.substring(line.indexOf(
                "PARALLEL ID ") + 12));
          }
          sb.append(line).append("\n");
        }
        stage.setLog(sb.toString());
      }
      if (stage_processes.containsKey(StageStatus.PUBLISH)) {
        Map processes = (HashMap) stage_processes.get(StageStatus.PUBLISH);
        for (Iterator iterator = processes.keySet().iterator();
             iterator.hasNext(); ) {
          String parallel = (String) iterator.next();
          ArrayList target_names = (ArrayList) processes.get(parallel);
          if (target_names.contains(target_name + release_name)) {
            if ( ( (stage.getCode() | StageStatus.RUNNING) !=
                  StageStatus.RUNNING) ||
                ( (stage.getCode() | StageStatus.NONE) == StageStatus.NONE)
                ) {
              stage.setCode(StageStatus.QUEUED);
            }
            stage.setParallelIdentifier(parallel);
          }
        }
      }
      status[4] = stage;
      target.setStageStatus(status);
    }
    catch (IOException e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to read the publish log ", e);
      ere.setDetail("file",
                    release_info.getBuildUri() + "/log/publish_" +
                    target_name + ".log");
      throw ere;
    }

    return target;
  }

  /**
   * Returns the complete list of target names
   * this should just be hardcoded by the specific release generator
   * @param release_name An object {@link String} representation of release name.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @return An array of object {@link String}.
   * @throws MEMEException if failed to get target.
   */
  public String[] getTargetNames(String release_name,
                                 MRDDataSource data_source) throws
      MEMEException {
    final String[] target_list = new String[] {
        "MRDOC", "MRSAB", "MRRANK",
        "MRCONSO", "MRSTY", "MRDEF",
        "AMBIG", "MRX",
        "MRHIST", "MRCUI", "MRAUI",
        "MRHIER",
        "MRREL", "MRSAT", "MRMAP",
        "MetaMorphoSys",
        "MRFILESCOLS",
        "ORF", "DOC", "ActiveSubset","Optimization"};
    return target_list;
  }

  /**
   * activate target handler
   * @param target_name An object {@link String} representation of target name.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @throws MEMEException if failed to get target.
   */
  public void activateTargetHandler(String target_name,
                                    MRDDataSource data_source) throws
      MEMEException {
    data_source.activateRegisteredHandler(
        data_source.getReleaseHandler("Full", target_name));
    final List toremove = new ArrayList();
    for (Iterator e = release_target_cache.keySet().iterator(); e.hasNext(); ) {
      final String name = (String) e.next();
      if (name.startsWith(target_name)) {
        toremove.add(name);
      }
    }
    for (Iterator e = toremove.iterator(); e.hasNext(); ) {
      release_target_cache.remove(e.next());
    }
  }

  /**
   * activate target handlers
   * @param target_names An array of object {@link String} representation of target names.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @throws MEMEException if failed to activate target.
   */
  public void activateTargetHandlers(String[] target_names,
                                     MRDDataSource data_source) throws
      MEMEException {
    for (int i = 0; i < target_names.length; i++) {
      activateTargetHandler(target_names[i].trim(), data_source);
    }
  }

  /**
   * deactivate target handler
   * @param target_name An object {@link String} representation of target name.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @throws MEMEException if failed to deactivate target.
   */
  public void deactivateTargetHandler(String target_name,
                                      MRDDataSource data_source) throws
      MEMEException {
    data_source.deactivateRegisteredHandler(data_source.getReleaseHandler(
        "Full", target_name));
    final List toremove = new ArrayList();
    for (Iterator e = release_target_cache.keySet().iterator(); e.hasNext(); ) {
      final String name = (String) e.next();
      if (name.startsWith(target_name)) {
        toremove.add(name);
      }
    }
    for (Iterator e = toremove.iterator(); e.hasNext(); ) {
      release_target_cache.remove(e.next());
    }
  }

  /**
   * deactivate target handlers
   * @param target_names An array of object {@link String} representation of target names.
   * @param data_source An object {@link MRDDataSource} representation of service.
   * @throws MEMEException if failed to deactivate target.
   */
  public void deactivateTargetHandlers(String[] target_names,
                                       MRDDataSource data_source) throws
      MEMEException {
    for (int i = 0; i < target_names.length; i++) {
      deactivateTargetHandler(target_names[i].trim(), data_source);
    }
  }

  /**
   * remove the Medline process log file
   * @param stage An object {@link String} representation of stage.
   * @throws MEMEException if failed to process the request.
   */
  public void clearMedlineStatus(String stage) throws MEMEException {
    final Map stage_files_map = new HashMap(4);
    final String medline_dir = ServerToolkit.getProperty("env.MEDLINE_DIR");
    stage_files_map.put("download",
                        new String[] {
                        medline_dir + "/get_baseline.log",
                        medline_dir + "/medline_parser.log",
                        medline_dir + "/process_medline_data.log",
                        medline_dir + "/update/update_medline_data.log"});
    stage_files_map.put("parse",
                        new String[] {
                        medline_dir + "/medline_parser.log",
                        medline_dir + "/process_medline_data.log",
                        medline_dir + "/update/update_medline_data.log"});
    stage_files_map.put("process",
                        new String[] {
                        medline_dir + "/process_medline_data.log",
                        medline_dir + "/update/update_medline_data.log"});
    stage_files_map.put("update",
                        new String[] {
                        medline_dir + "/update/update_medline_data.log"});
    if (stage_files_map.containsKey(stage)) {
      final String[] files = (String[]) stage_files_map.get(stage);
      for (int i = 0; i < files.length; i++) {
        File file = new File(files[i]);
        file.delete();
      }
    }
    else {
      BadValueException bve = new BadValueException("Invalid stage value");
      bve.setDetail("stage", stage);
      throw bve;
    }
  }

  /**
   * remove the stage's log file
   * @param release_info An object {@link ReleaseInfo} representation of release name.
   * @param target_name An object {@link String} representation of target name.
   * @param stage An object {@link String} representation of stage.
   * @throws MEMEException if failed to process the request.
   */
  public void clearStatus(ReleaseInfo release_info, String target_name,
                          String stage) throws MEMEException {
    final Map stage_files_map = new HashMap(5);
    stage_files_map.put("prevQA", new String[] {
                        release_info.getBuildUri() + "/QA/" +
                        "prev_" + target_name + ".log",
                        release_info.getBuildUri() + "/QA/" +
                        "gold_" + target_name + ".log",
                        release_info.getBuildUri() + "/log/" +
                        target_name + ".log",
                        release_info.getBuildUri() + "/QA/" +
                        "qa_" + target_name + ".rpt",
                        release_info.getBuildUri() + "/log/publish_" +
                        target_name + ".log"
    }
        );
    stage_files_map.put("gold", new String[] {
                        release_info.getBuildUri() + "/QA/" +
                        "gold_" + target_name + ".log",
                        release_info.getBuildUri() + "/log/" +
                        target_name + ".log",
                        release_info.getBuildUri() + "/QA/" +
                        "qa_" + target_name + ".rpt",
                        release_info.getBuildUri() + "/log/publish_" +
                        target_name + ".log"
    }
        );
    stage_files_map.put("build", new String[] {
                        release_info.getBuildUri() + "/log/" +
                        target_name + ".log",
                        release_info.getBuildUri() + "/QA/" +
                        "qa_" + target_name + ".rpt",
                        release_info.getBuildUri() + "/log/publish_" +
                        target_name + ".log"
    }
        );
    stage_files_map.put("validate", new String[] {
                        release_info.getBuildUri() + "/QA/" +
                        "qa_" + target_name + ".rpt",
                        release_info.getBuildUri() + "/log/publish_" +
                        target_name + ".log"
    }
        );
    stage_files_map.put("publish",
                        new String[] {release_info.getBuildUri() +
                        "/log/publish_" +
                        target_name + ".log"});
    if (stage_files_map.containsKey(stage)) {
      release_target_cache.remove(target_name + release_info.getName());
      final String[] files = (String[]) stage_files_map.get(stage);
      for (int i = 0; i < files.length; i++) {
        File file = new File(files[i]);
        file.delete();
      }
    }
    else {
      BadValueException bve = new BadValueException("Invalid stage value");
      bve.setDetail("stage", stage);
      throw bve;
    }
  }

  /**
   * Deletes the xml file from medline update
   * @param file_name An object {@link String} representation of file name.
   * @throws MEMEException if failed to get status.
   */
  public void deleteUpdateMedlineXML(String file_name) throws MEMEException {
    final String medline_dir = ServerToolkit.getProperty("env.MEDLINE_DIR");
    try {
      final File file = new File(medline_dir + "/update", file_name);
      file.delete();
    }
    catch (Exception e) {
      ExternalResourceException ere = new ExternalResourceException(
          "Failed to delete the xml file", e);
      ere.setDetail("file",
                    medline_dir + "/update/" + file_name);
      throw ere;

    }

  }

  /**
   * Computes an MD5 digest for a local file.
   * @param path the path
   * @returns the digest
   * @throws ExecException
   */
  private String localDigest(String path) throws ExecException {
    final String digest = ServerToolkit.exec(
        new String[] {
        "/bin/sh", "-c",
        "/bin/cat " + path + " | " +
        ServerToolkit.getProperty("env.PATH_TO_MD5")}
        ,
        new String[] {}
        ,
        true,
        ServerConstants.USE_INPUT_STREAM,
        false);
    return " " + digest;
  }

  /**
   * Computes an MD5 digest for a remote file.
   * @param path th epath
   * @param remotehost the remote host
   * @return the digest
   * @throws ExecException
   */
  private String remoteDigest(String path, String remotehost) throws
      ExecException {

    final String digest = ServerToolkit.exec(
        new String[] {
        "/bin/rsh", remotehost,
        "/bin/cat " + path + " | " +
        ServerToolkit.getProperty("env.PATH_TO_MD5")}
        ,
        new String[] {}
        ,
        true,
        ServerConstants.USE_INPUT_STREAM,
        false);
    return " " + digest;
  }

  /**
   * Downloads the Medline Baseline files from NLM machine to $MEDLINE_DIR.
   * @throws MEMEException if failed to download the files
   */
  public void downloadMedlineBaseline() throws MEMEException {
    ServerToolkit.getThread(new Runnable() {
      public void run() {
        final String medline_dir = ServerToolkit.getProperty(
            "env.MEDLINE_DIR");
        final String meme_home = ServerToolkit.getProperty("env.MEME_HOME");
        PrintWriter writer = null;
        try {
          final File file = new File(medline_dir,
                                     "get_baseline.log");
          writer = new PrintWriter(new BufferedWriter(new FileWriter(
              file)));
          try {
            ServerToolkit.exec(
                new String[] {
                meme_home + "/bin/get_baseline.pl"},
                new String[0],
                new File(medline_dir),
                writer);
          }
          catch (ExecException exece) {
            writer.write("Error downloading Medline baseline XML files");
            exece.printStackTrace(writer);
            ServerToolkit.handleError(exece);
          }
          writer.close();
        }
        catch (IOException ioe) {
          ServerToolkit.handleError(ioe);
        }
      }
    }).start();

  }

  /**
   * Parses the Medline Baseline XML files by calling $MRD_HOME/bin/medline_parser.pl.
   * @param service the service name
   * @param release the release name
   * @throws MEMEException if failed to parse the files
   */
  public void parseMedlineBaseline(String service, ReleaseInfo release) throws MEMEException {
    final String service_name = service;
    final ReleaseInfo release_info = release;
    ServerToolkit.getThread(new Runnable() {
      public void run() {
        final String medline_dir = ServerToolkit.getProperty(
            "env.MEDLINE_DIR");
        final String meme_home = ServerToolkit.getProperty("env.MEME_HOME");
        PrintWriter writer = null;
        try {
          File file = new File(medline_dir,
                               "coc_headings.dat");
          file.delete();
          file = new File(medline_dir,
                          "coc_subheadings.dat");
          file.delete();
          file = new File(medline_dir,
                          "medline_parser.log");
          writer = new PrintWriter(new BufferedWriter(new FileWriter(
              file)));
          try {
            DateFormat dateformat = new SimpleDateFormat(
                "MM/dd/yyyy");
            ServerToolkit.exec(new String[] {
                               meme_home +
                               "/bin/medline_parser.pl",
                               "-db=" + service_name,
                               "-release_date=" + dateformat.format(release_info.getReleaseDate()),
                               "-i",
                               "medline*.xml"}
                               ,
                               new String[] {"MEME_HOME=" +
                               ServerToolkit.getProperty(
                                   ServerConstants.MEME_HOME),
                               "ORACLE_HOME=" +
                               ServerToolkit.
                               getProperty(ServerConstants.
                                           ORACLE_HOME)}
                               ,
                               new File(medline_dir),
                               writer);
          }
          catch (ExecException exece) {
            writer.write("ERROR parsing Medline baseline XML files");
            exece.printStackTrace(writer);
            ServerToolkit.handleError(exece);
          }
          writer.close();
        }
        catch (IOException ioe) {
          ServerToolkit.handleError(ioe);
        }
      }
    }).start();
  }

  /**
   * Process the Medline Baseline data by calling $MRD_HOME/bin/process_medline_data.csh.
   * @param service the service name
   * @throws MEMEException if failed to process the data
   */
  public void processMedlineBaseline(String service) throws MEMEException {
    final String service_name = service;
    ServerToolkit.getThread(new Runnable() {
      public void run() {
        final String meme_home = ServerToolkit.getProperty(ServerConstants.
            MEME_HOME);
        final String medline_dir = ServerToolkit.getProperty(
            "env.MEDLINE_DIR");
        PrintWriter writer = null;
        try {
          File file = new File(medline_dir,
                               "process_medline_data.log");
          writer = new PrintWriter(new BufferedWriter(new FileWriter(
              file)));
          try {
            file = new File(medline_dir + "/update");
            if (file.exists()) {
              ServerToolkit.exec(
                  new String[] {"/bin/rm", "-r", "-f",
                  medline_dir + "/update"});
            }
            ServerToolkit.exec(
                new String[] {
                meme_home + "/bin/process_medline_data.csh",
                "-mrd", "-i", service_name},
                new String[0],
                new File(medline_dir), writer);
          }
          catch (ExecException exece) {
            writer.write("ERROR process Medline baseline XML files");
            exece.printStackTrace(writer);
            ServerToolkit.handleError(exece);
          }
          writer.close();
        }
        catch (IOException ioe) {
          ServerToolkit.handleError(ioe);
        }
      }
    }).start();
  }

  /**
   * Downloads, parses and process the Medline Update XML files by calling
   * $MRD_HOME/bin/update_medline_data.pl
   * @param service the service name
   * @param release the release name
   * @throws MEMEException if failed to process the data
   */
  public void updateMedline(String service, ReleaseInfo release) throws MEMEException {
    final String service_name = service;
    final ReleaseInfo release_info = release;
    ServerToolkit.getThread(new Runnable() {
      public void run() {
        final String meme_home = ServerToolkit.getProperty(ServerConstants.
            MEME_HOME);
        final String medline_dir = ServerToolkit.getProperty(
            "env.MEDLINE_DIR");
        PrintWriter writer = null;
        try {
          File file = new File(medline_dir + "/update");
          if (!file.exists()) {
            file.mkdir();
          }
          file = new File(medline_dir + "/update",
                               "update_medline_data.log");
          writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
          try {
            DateFormat dateformat = new SimpleDateFormat(
                "MM/dd/yyyy");
            ServerToolkit.exec(
                new String[] {
                meme_home + "/bin/update_medline_data.pl",
                "-mrd",
                "-db=" + service_name,
                "-start_date=" + dateformat.format(release_info.getStartDate()),
                "-release_date=" + dateformat.format(release_info.getReleaseDate()),
            },
                new String[0],
                new File(medline_dir + "/update"), writer);
          }
          catch (ExecException exece) {
            writer.write("ERROR processing Medline update XML files");
            exece.printStackTrace(writer);
            ServerToolkit.handleError(exece);
          }
          writer.close();
        }
        catch (IOException ioe) {
          ServerToolkit.handleError(ioe);
        }
      }
    }).start();
  }

  /**
   * Returns the {@link StageStatus} of the specified Meldine  processing stage.
   * @param stage_name the stage name
   * @return the {@link StageStatus} of the specified Meldine  processing stage
   * @throws MEMEException if failed to get status
   */
  public StageStatus getMedlineStageStatus(String stage_name) throws
      MEMEException {
    final String medline_dir = ServerToolkit.getProperty("env.MEDLINE_DIR");
    StageStatus stage = null;
    if (stage_name.equals("download")) {
      try {
        File file = new File(medline_dir,
                             "get_baseline.log");
        stage = new StageStatus("download");
        stage.setCode(StageStatus.NONE);
        if (file.exists()) {
          stage.setCode(StageStatus.RUNNING);
          BufferedReader in = new BufferedReader(new FileReader(file));
          String line = null;
          StringBuffer sb = new StringBuffer();
          while ( (line = in.readLine()) != null) {
            if (line.indexOf("Error") != -1) {
              stage.setCode(StageStatus.ERROR);
            }
            if (line.indexOf("Finished") != -1) {
              stage.setCode(StageStatus.FINISHED);
            }
            sb.append(line).append("\n");
          }
          stage.setLog(sb.toString());
        }
        return stage;
      }
      catch (IOException e) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to read the download log file", e);
        ere.setDetail("file", medline_dir + "/get_baseline.log");
        throw ere;
      }
    }
    if (stage_name.equals("parse")) {
      try {
        final File file = new File(medline_dir,
                                   "medline_parser.log");
        stage = new StageStatus("parse");
        stage.setCode(StageStatus.NONE);
        if (file.exists()) {
          stage.setCode(StageStatus.RUNNING);
          BufferedReader in = new BufferedReader(new FileReader(file));
          String line = null;
          StringBuffer sb = new StringBuffer();
          while ( (line = in.readLine()) != null) {
            if (line.indexOf("ERROR") != -1) {
              stage.setCode(StageStatus.ERROR);
            }
            if (line.indexOf("Finished") != -1) {
              stage.setCode(StageStatus.FINISHED);
            }
            sb.append(line).append("\n");
          }
          stage.setLog(sb.toString());
        }
        return stage;
      }
      catch (IOException e) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to read the parse log file", e);
        ere.setDetail("file", medline_dir + "/medline_parser.log");
        throw ere;
      }
    }
    if (stage_name.equals("process")) {
      try {
        final File file = new File(medline_dir,
                                   "process_medline_data.log");
        stage = new StageStatus("process");
        stage.setCode(StageStatus.NONE);
        if (file.exists()) {
          stage.setCode(StageStatus.RUNNING);
          BufferedReader in = new BufferedReader(new FileReader(file));
          String line = null;
          StringBuffer sb = new StringBuffer();
          while ( (line = in.readLine()) != null) {
            if (line.indexOf("ORA-") != -1 || line.indexOf("Error") != -1) {
              stage.setCode(StageStatus.ERROR);
            }
            if (line.indexOf("Finished") != -1) {
              stage.setCode(StageStatus.FINISHED);
            }
            sb.append(line).append("\n");
          }
          stage.setLog(sb.toString());
        }
        return stage;
      }
      catch (IOException e) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to read the process log file", e);
        ere.setDetail("file", medline_dir + "/process_medline_data.log");
        throw ere;
      }
    }
    if (stage_name.equals("update")) {
      try {
        final File file = new File(medline_dir + "/update",
                                   "update_medline_data.log");
        stage = new StageStatus("update");
        stage.setCode(StageStatus.NONE);
        if (file.exists()) {
          stage.setCode(StageStatus.RUNNING);
          BufferedReader in = new BufferedReader(new FileReader(file));
          String line = null;
          StringBuffer sb = new StringBuffer();
          while ( (line = in.readLine()) != null) {
            if (line.indexOf("ORA-") != -1 || line.indexOf("ERROR") != -1) {
              stage.setCode(StageStatus.ERROR);
            }
            if (line.indexOf("FINISHED") != -1) {
              stage.setCode(StageStatus.FINISHED);
            }
            sb.append(line).append("\n");
          }
          stage.setLog(sb.toString());
        }
        return stage;
      }
      catch (IOException e) {
        ExternalResourceException ere = new ExternalResourceException(
            "Failed to read the update log file", e);
        ere.setDetail("file",
                      medline_dir + "/update/update_medline_data.log");
        throw ere;
      }
    }
    return stage;
  }

  /**
   * Returns all {@link StageStatus} for Meldine processing stages.
   * @return all {@link StageStatus} for Meldine processing stages
   * @throws MEMEException if failed to get status
   */
  public StageStatus[] getMedlineStatus() throws MEMEException {
    final String[] stages = new String[] {
        "download", "parse", "process", "update"};
    final StageStatus[] status = new StageStatus[stages.length];
    for (int i = 0; i < stages.length; i++) {
      status[i] = getMedlineStageStatus(stages[i]);
    }
    return status;
  }
  /**
   * Indicates whether or not the release is finished.
   * @param release the {@link ReleaseInfo}
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws MEMEException if failed to process the request
   */
  public StageStatus getReleaseStatus(ReleaseInfo release_info) throws MEMEException {

	  StageStatus status = new StageStatus(release_info.getName());
  try {
      final File file = new File(release_info.getBuildUri() + "/log",
                                 "Finished.log");
      if (file.exists()) {
        final BufferedReader in = new BufferedReader(new FileReader(file));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ( (line = in.readLine()) != null) {
        	if (line.indexOf("ERROR") != -1) {
                status.setCode(StageStatus.ERROR);
             }
        	if (line.indexOf("FINISHED") != -1) {
        		status.setCode(StageStatus.FINISHED);
        	}
        	sb.append(line).append("\n");
        }
        status.setLog(sb.toString());
      }
  } catch (IOException ioe) {
      ExternalResourceException ere = new ExternalResourceException(
              "Failed to read the finish log file", ioe);
          ere.setDetail("file",
        		  release_info.getBuildUri() + "/log/Finished.log");
          throw ere;
  }
   return status;
  }

}
