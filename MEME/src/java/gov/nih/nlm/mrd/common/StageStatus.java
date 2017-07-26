/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.common
 * Object:  StageStatus
 *
 *****************************************************************************/
package gov.nih.nlm.mrd.common;

import java.util.*;

import gov.nih.nlm.meme.*;

/**
 * Represents the status of one stage of a multi-part operation.
 * @author TTN, BAC
 */
public class StageStatus {

  public static final int NONE = 0;
  public static final int ERROR = 2;
  public static final int QUEUED = 4;
  public static final int RUNNING = 8;
  public static final int NEEDSREVIEW = 16;
  public static final int FINISHED = 32;
  public static final String PREVQA = "prevQA";
  public static final String GOLD = "gold";
  public static final String BUILD = "build";
  public static final String VALIDATE = "validate";
  public static final String PUBLISH = "publish";


  private int code;
  private String name, log;
  private String parallelIdentifier;
  private Date endTime;
  private Date startTime;
  private String targetName;

  /**
   * Instantiates a {@link StageStatus} with the specified name.
   * @param name the stage name
   */
  public StageStatus(String name) {
    this.name = name;
  }

  /**
   * Returns the code.
   * @return the code
   */
  public int getCode() {
    return code;
  }

  /**
   * Sets the code.
   * @param code the code
   */
  public void setCode(int code) {
    this.code = this.code | code;
  }

  /**
   * Returns the name.
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the log.
   * @return the log
   */
  public String getLog() {
    return log;
  }

  /**
   * Sets the log.
   * @param log the log
   */
  public void setLog(String log) {
    this.log = log;
  }

  public void setParallelIdentifier(String parallelIdentifier) {
    this.parallelIdentifier = parallelIdentifier;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  public String getElapsedTime() {
    if(startTime != null && endTime != null) {
      return MEMEToolkit.timeToString(MEMEToolkit.timeDifference(endTime,
          startTime));
    }
    return null;
  }

  public String getParallelIdentifier() {
    return parallelIdentifier;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public String getTargetName() {
    return targetName;
  }

}
