package gov.nih.nlm.mrd.web;

import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.web.ViewReportActionForm;
import gov.nih.nlm.mrd.beans.ReleaseBean;
import gov.nih.nlm.mrd.client.ReleaseClient;
import gov.nih.nlm.mrd.common.ReleaseInfo;
import gov.nih.nlm.mrd.common.ReleaseTarget;
import gov.nih.nlm.mrd.common.StageStatus;
import gov.nih.nlm.util.OrderedHashMap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class ViewReleaseReportAction
    extends Action {
  public ActionForward execute(ActionMapping actionMapping,
                               ActionForm actionForm,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws
      MEMEException {
    ViewReportActionForm viewReportActionForm = (ViewReportActionForm)
        actionForm;
    HttpSession session = servletRequest.getSession();
    ReleaseBean releaseBean = null;
    if ( (session != null) && (session.getAttribute("release_bean") != null)) {
      releaseBean = (ReleaseBean) session.getAttribute("release_bean");
    }
    if (releaseBean == null) {
      releaseBean = new ReleaseBean();
    }
    try {
      ReleaseClient rc = releaseBean.getReleaseClient();
      ReleaseInfo release_info = rc.getReleaseInfo(viewReportActionForm.
          getRelease());
      String[] targets = rc.getTargetNames(release_info.getName());
      if ("elapsedTime".equals(viewReportActionForm.getType())) {
        List list = new ArrayList();
        for (int i = 0; i < targets.length; i++) {
          OrderedHashMap map = new OrderedHashMap();
          map.put("name", targets[i]);
          ReleaseTarget target = rc.getTargetStatus(release_info.getName(),
              targets[i]);
          StageStatus[] stagestatus = target.getStageStatus();
          for (int j = 0; j < stagestatus.length; j++) {
            String elapsedTime = "";
            if ( (stagestatus[j].getCode() & StageStatus.FINISHED) ==
                StageStatus.FINISHED) {
              elapsedTime = stagestatus[j].getElapsedTime();
            }
            map.put(stagestatus[j].getName(), elapsedTime);
          }
          list.add(map);
        }
        servletRequest.setAttribute("targets", list);
      }
      else if ("daily".equals(viewReportActionForm.getType())) {
        OrderedHashMap stage_processes = new OrderedHashMap();
        String date = viewReportActionForm.getDate();
        Map process_queue = new HashMap();
        stage_processes.put(StageStatus.PREVQA, new OrderedHashMap());
        stage_processes.put(StageStatus.GOLD, new OrderedHashMap());
        stage_processes.put(StageStatus.BUILD, new OrderedHashMap());
        stage_processes.put(StageStatus.VALIDATE, new OrderedHashMap());
        stage_processes.put(StageStatus.PUBLISH, new OrderedHashMap());
        for (int i = 0; i < targets.length; i++) {
          ReleaseTarget target = rc.getTargetStatus(release_info.getName(),
              targets[i]);
          StageStatus[] stagestatus = target.getStageStatus();
          for (int j = 0; j < stagestatus.length; j++) {
            OrderedHashMap process_status = (OrderedHashMap) stage_processes.
                get(stagestatus[j].getName());
            String parallelId = stagestatus[j].getParallelIdentifier();
            if (parallelId == null) {
              parallelId = "<br>";
            }
            if ( ( (stagestatus[j].getCode() & StageStatus.QUEUED) ==
                  StageStatus.QUEUED)) {
              List queueList = new ArrayList();
              if (process_queue.containsKey(parallelId)) {
                queueList = (ArrayList) process_queue.get(parallelId);
              }
              queueList.add(stagestatus[j].getTargetName());
              process_queue.put(parallelId, queueList);
            }
            else {
              if ( ( (stagestatus[j].getCode() | StageStatus.NONE) !=
                    StageStatus.NONE) &&
                  (date.equals(releaseBean.getDateFormat().format(stagestatus[j].
                  getStartTime())))) {
                List statusList = new ArrayList();
                if (process_status.containsKey(parallelId)) {
                  statusList = (ArrayList) process_status.get(parallelId);
                }
                statusList.add(stagestatus[j]);
                process_status.put(parallelId, statusList);
              }
            }
          }
        }

        Calendar calendar = Calendar.getInstance();
        DateFormat dateformat = new SimpleDateFormat("hh a");
        DateFormat hourformat = new SimpleDateFormat("h:mm");
        List rowList = new ArrayList();
        for (int i = 0; i < 24; i++) {
          List row = new ArrayList();
          calendar.set(Calendar.HOUR_OF_DAY, i);
          row.add(dateformat.format(calendar.getTime()));
          for (Iterator stageIterator = stage_processes.orderedKeySet().
               iterator();
               stageIterator.hasNext(); ) {
            String stage = (String) stageIterator.next();
            OrderedHashMap process_status = (OrderedHashMap) stage_processes.
                get(
                    stage);
            if (process_status.isEmpty()) {
              row.add("");
            }
            for (Iterator processIterator = process_status.orderedKeySet().
                 iterator();
                 processIterator.hasNext(); ) {
              List statusList = (ArrayList) process_status.get(
                  processIterator.
                  next());
              Collections.sort(statusList, new Comparator() {
                public int compare(Object o1, Object o2) {
                  if (o1 == null || o2 == null) {
                    return 0;
                  }
                  // sort on startTime
                  StageStatus s1 = (StageStatus) o1;
                  StageStatus s2 = (StageStatus) o2;
                  if (s1.getStartTime() == null || s2.getStartTime() == null) {
                    return 0;
                  }
                  return s1.getStartTime().compareTo(s2.getStartTime());
                }

              });
              StringBuffer column = new StringBuffer();
              for (Iterator iterator = statusList.iterator();
                   iterator.hasNext(); ) {
                StageStatus status = (StageStatus) iterator.next();
                calendar.setTime(status.getStartTime());
                if (calendar.get(Calendar.HOUR_OF_DAY) == i) {
                  column.append("Start " + status.getTargetName())
                      .append(" ")
                      .append(hourformat.format(status.getStartTime())).append(
                          "<br />");
                }
                if ( ( (status.getCode() & StageStatus.FINISHED) ==
                      StageStatus.FINISHED)) {
                  calendar.setTime(status.getEndTime());
                  if (calendar.get(Calendar.HOUR_OF_DAY) == i &&
                      date.equals(releaseBean.getDateFormat().format(status.
                      getEndTime()))) {
                    column.append("Finish " + status.getTargetName())
                        .append(" ")
                        .append(hourformat.format(status.getEndTime()))
                        .append("<br />");
                  }
                }
              }
              row.add(column.toString());
            }
          }
          rowList.add(row);
        }
        List processIds = new ArrayList();
        OrderedHashMap stage_size = new OrderedHashMap();
        for (Iterator stageIterator = stage_processes.orderedKeySet().iterator();
             stageIterator.hasNext(); ) {
          String stage = (String) stageIterator.next();
          OrderedHashMap process_status = (OrderedHashMap) stage_processes.get(
              stage);
          int size = 0;
          if (process_status.isEmpty()) {
            processIds.add("<br>");
          }
          for (Iterator processIterator = process_status.orderedKeySet().
               iterator();
               processIterator.hasNext(); ) {
            String processId = (String) processIterator.next();
            processIds.add(processId);
            size++;
          }
          stage_size.put(stage, String.valueOf(size));
        }
        for (Iterator iterator = stage_size.orderedKeySet().iterator();
             iterator.hasNext(); ) {
          String key = (String) iterator.next();
          servletRequest.setAttribute(key + "Size", stage_size.get(key));
        }
        List queueList = new ArrayList();
        for (Iterator iterator = process_queue.keySet().iterator();
             iterator.hasNext(); ) {
          Map map = new HashMap();
          String processId = (String) iterator.next();
          StringBuffer sb = new StringBuffer();
          List targetList = (ArrayList) process_queue.get(processId);
          Iterator targetIterator = targetList.iterator();
          sb.append(targetIterator.next());
          while (targetIterator.hasNext()) {
            sb.append(",").append(targetIterator.next());
          }
          map.put("parallelId", processId);
          map.put("targets", sb.toString());
          queueList.add(map);
        }
        if (!queueList.isEmpty()) {
          servletRequest.setAttribute("queue", queueList);
        }
        servletRequest.setAttribute("processIds", processIds);
        servletRequest.setAttribute("rows", rowList);
      }
    }
    catch (MEMEException e) {
      servletRequest.setAttribute("MEMEException", e);
      throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
      MEMEException me = new MEMEException("Non MEMEException", e);
      me.setPrintStackTrace(true);
      servletRequest.setAttribute("MEMEException", me);
      throw me;
    }
    return actionMapping.findForward("success");
  }
}
