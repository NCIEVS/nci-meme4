/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.beans
 * Object:  WorkLogBean
 * Changes
 *   01/20/2006 TTN (1-74OKI): Sort activities by timestamp
 *
 *****************************************************************************/
package gov.nih.nlm.meme.beans;

import java.util.Calendar;
import java.util.ArrayList;
import gov.nih.nlm.meme.action.WorkLog;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.action.Activity;

import java.util.Comparator;
import java.util.Collections;

public class WorkLogBean
    extends AuxiliaryDataClientBean {
  private int range, work_id;

  public int getRange() {
    return range;
  }

  public void setRange(int range) {
    this.range = range;
  }

  public int getWork_id() {
    return work_id;
  }

  public void setWork_id(int work_id) {
    this.work_id = work_id;
  }

  public WorkLogBean() throws MEMEException {
    super();
  }

  public ArrayList getWorkLogs() throws MEMEException {
    WorkLog[] works = getAuxiliaryDataClient().getWorkLogs();
    Calendar now = Calendar.getInstance();
    now.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) - range);
    ArrayList logs = new ArrayList(works.length);
    for (int i = 0; i < works.length; i++) {
      if (works[i].getTimestamp().after(now.getTime())) {
        logs.add(works[i]);
      }
    }
    Collections.sort(logs, new Comparator() {
      public int compare(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
          return 0;
        }
        WorkLog w1 = (WorkLog) o1;
        WorkLog w2 = (WorkLog) o2;
        return w2.getTimestamp().compareTo(w1.getTimestamp());
      }
    }
    );
    return logs;
  }

  public WorkLog getWorkLog() throws MEMEException {
    return getAuxiliaryDataClient().getWorkLog(work_id);
  }

  public ArrayList getActivities() throws MEMEException {
      Activity[] activities = getAuxiliaryDataClient().getActivityLogs(new
          WorkLog(work_id));
      ArrayList logs = new ArrayList(activities.length);
      for (int i = 0; i < activities.length; i++) {
        logs.add(activities[i]);
      }
      Collections.sort(logs,new Comparator() {
        public int compare(Object o1, Object o2) {
          if (o1 == o2 ) {
            return 0;
          }
          if(o1 == null) {
            return -1;
          }
          if(o2 == null) {
            return 1;
          }
          Activity v1 = (Activity) o1;
          Activity v2 = (Activity) o2;
          if ( v1.getTimestamp() == v2.getTimestamp() ) {
            return 0;
          }
          if(v1.getTimestamp() == null) {
            return -1;
          }
          if(v2.getTimestamp() == null) {
            return 1;
          }
          return v1.getTimestamp().compareTo(v2.getTimestamp());
        }
      });
      return logs;
  }
}
