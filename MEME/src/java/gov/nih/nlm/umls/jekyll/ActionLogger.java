/*
 * ActionLogger.java
 * 
 * Jan 23, 2005: 1st version
 */
package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Vlad
 *  
 */
public class ActionLogger {
    //
    // Status fields
    //
    public static final String STARTED = "started";

    public static final String INCOMPLETE = "incomplete, error encountered";

    public static final String COMPLETE = "completed successfully";

    //
    // Private fields
    //
    private String action_name = null;

    private Date then = null;

    private Date now = null;

    private int action_id = 0;

    //
    // Constructors
    //
    
    public ActionLogger (String action_name) {
        this(action_name, false);
    }
    
    public ActionLogger(String action_name, boolean set_start_time) {
        this.action_name = action_name;
        action_id = JekyllKit.getNextActionId();
        if (set_start_time) {
            logStartTime();
        }
    }

    public void logStartTime() {
        then = Calendar.getInstance().getTime();
        MEMEToolkit.logComment("[action=" + action_name + " action_id="
                + action_id + " status=" + ActionLogger.STARTED + "]", true);
    } //logStartTime()

    /**
     * Logs elapsed time of the action. This method requires that
     * <code>logStartTime()</code> method has been invoked beforehand.
     */
    public void logElapsedTime() {
        if (then == null) {
            MEMEToolkit.logComment("[action=" + action_name + " action_id="
                    + action_id + " elapsed_time=failed to compute]");
        } else {
            now = Calendar.getInstance().getTime();
            MEMEToolkit.logComment("[action="
                    + action_name
                    + " action_id="
                    + action_id
                    + " elapsed_time="
                    + MEMEToolkit.timeToString(MEMEToolkit.timeDifference(now,
                            then)) + "]", true);
        }
    } //logElapsedTime()
    
    public String getActionName() {
        return action_name;
    } //getActionName()
    
    public void setActionName(String name) {
        action_name = name;
    } //setActionName()
    
    public boolean isLogged() {
        if (now != null) {
            return true;
        } else {
            return false;
        }
    } //isLogged()
}