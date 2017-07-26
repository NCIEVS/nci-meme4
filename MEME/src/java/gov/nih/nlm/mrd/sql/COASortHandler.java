/*****************************************************************************
 *
 * Package: gov.nih.nlm.mrd.server
 * Object:  COASortHandler
 *
 *****************************************************************************/
package gov.nih.nlm.mrd.sql;

import gov.nih.nlm.meme.exception.DeveloperException;
import gov.nih.nlm.meme.exception.MEMEException;
import gov.nih.nlm.meme.sql.DataWriterHandler;
import gov.nih.nlm.util.FieldedStringTokenizer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * {@link DataWriterHandler} to sort the COA field when writing <code>MRCOC.RRF</code>.
 */
public class COASortHandler implements DataWriterHandler {

  /**
   * Instantiates a {@link COASortHandler}.
   */
  public COASortHandler() { }

  /**
   * Sorts COA field.
   * @param line the line to process
   * @return the processed line
   * @throws MEMEException if failed to process line
   */
  public String processLine(String line) throws MEMEException {
    HashMap hm = new HashMap();
    try {
      String[] fields = FieldedStringTokenizer.split(line, String.valueOf('|'));
      String[] coas = FieldedStringTokenizer.split(fields[7], String.valueOf(','));
      for (int i = 0; i < coas.length; i++) {
        String[] coa = FieldedStringTokenizer.split(coas[i], String.valueOf('='));
        StringBuffer sb = new StringBuffer("000000");
        sb.append(100000 - Integer.parseInt(coa[1]));
        hm.put(sb.substring(sb.length() - 6, sb.length()) + coa[0], coas[i]);
      }
      StringBuffer sb = new StringBuffer(line.length());
      for (int i = 0; i < fields.length - 2; i++)
        sb.append(fields[i]).append("|");
      Iterator i = new TreeSet(hm.keySet()).iterator();
      if (i.hasNext())
        sb.append(hm.get(i.next()));
      while (i.hasNext())
        sb.append(",").append(hm.get(i.next()));
      sb.append("|")
        .append(fields[fields.length - 1])
        .append("|");
      return sb.toString();
    } catch (Exception e) {
      DeveloperException dev =
          new DeveloperException(
          "Problems sorting COA field of MRCOC. " + "error message: " + e.getMessage());
      dev.setEnclosedException(e);
      throw dev;
    }
  }
}