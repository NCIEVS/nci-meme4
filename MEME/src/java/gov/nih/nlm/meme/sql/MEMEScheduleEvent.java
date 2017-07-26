/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MEMEScheduleEvent
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import java.util.EventObject;

/**
 * Indicates a change in the status of the schedule.  Typically used when
 * the "cpu mode" changes.
 *
 * @author MEME Group
 */
public class MEMEScheduleEvent extends EventObject {

  private String cpu_mode;
  private String lock_mode;

  /**
   * Constructor used by the MEMESchedule class.
   * @param schedule the {@link MEMESchedule} representation of schedule.
   * @param cpu_mode the {@link String} representation of cpu mode.
   * @param lock_mode the {@link String} representation of lock mode.
   */
  public MEMEScheduleEvent(MEMESchedule schedule, String cpu_mode,
                           String lock_mode) {
    super(schedule);
    //this.schedule = schedule;
    this.cpu_mode = cpu_mode;
    this.lock_mode = lock_mode;
  }

  /**
   * Returns the new cpu mode.
   * @return the {@link String} representation of cpu mode.
   */
  public String getCpuMode() {
    return cpu_mode;
  }

  /**
   * Returns the new lock mode.
   * @return the {@link String} representation of lock mode.
   */
  public String getLockMode() {
    return lock_mode;
  }

}
