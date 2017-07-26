/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme.sql
 * Object:  MEMEScheduleListener
 *
 *****************************************************************************/
package gov.nih.nlm.meme.sql;

import java.util.EventListener;

/**
 * Generically represents an application that listens for schedule events.
 *
 * @author MEME Group
 */
public interface MEMEScheduleListener extends EventListener {

  /**
   * Gives the {@link MEMESchedule} the possibility to inform a MEMEScheduleListener
   * of a cpu mode change. The implementation of this method should contain an
   * appropriate reaction to such a change.
   * @param event the {@link MEMEScheduleEvent} representation of event.
   */
  public void cpuModeChanged(MEMEScheduleEvent event);

  /**
   * Gives the {@link MEMESchedule} the possibility to inform a MEMEScheduleListener
   * of a lock mode change. The implementation of this method should contain an
   * appropriate reaction to such a change.
   * @param event the {@link MEMEScheduleEvent} representation of event.
   */
  public void lockModeChanged(MEMEScheduleEvent event);

  /**
       * Returns the name of an application implementing this interface. This name is
   * used in the meme_schedule table and should be therfore the same as in this
   * table (uppercase/lowercase is not important).
   * @return the {@link String} representation of application name.
   */
  public String getApplicationName();
}
