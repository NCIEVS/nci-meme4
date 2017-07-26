package gov.nih.nlm.swing;

import gov.nih.nlm.umls.jekyll.WorkFilesFrame;

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;

/**
 * Rearranges the window layout (used primarily in Work frames)
 *
 * @author Soma Lanka
 */

public class RearrangeWindowLayoutManager {

  //
  // Fields
  //
  private static ArrayList containers = new ArrayList();
  //private static int size = 0;
  
  /**
   * Privately instantiates a {@link RearrangeWindowLayoutManager}.
   * Prevents subclassing and direct instantiation.
   */
  private RearrangeWindowLayoutManager() {
  }

  /**
   * Adds a {@link Container} to the list of Containers whose
   * Window layout will be managed.
   * @param c the {@link Container} to manage
   */
  public synchronized static void addContainer(Container c) {
    if (!containers.contains(c)) {
      containers.add(c);
      }
  }

  /**
   * Removes a {@link Container} from the list of Containers whose
   * window sizewill be re-arranged
   * @param c the {@link Container} to remove
   */
  public synchronized static void removeContainer(Container c) {
    containers.remove(c);
  }

  /**
   * Rearrange the window
   */
  public synchronized static void rearrangeWindow() {
    for (int i = 0; i < containers.size(); i++) {   
  //    rearrangeWindow(c);
      if (containers.get(i)instanceof WorkFilesFrame) {
        ( (WorkFilesFrame) containers.get(i)).rearrangeWindow();
        ( (Window) containers.get(i)).pack();
       // ( (WorkFilesFrame) containers.get(i)).getWorkFiles();
      }
      
    }
  }

  /**
   * Rearrange the window
   */
  public synchronized static void reset() {
    for (int i = 0; i < containers.size(); i++) {   
  //    rearrangeWindow(c);
      if (containers.get(i)instanceof WorkFilesFrame) {
        ( (WorkFilesFrame) containers.get(i)).reset();
        ( (Window) containers.get(i)).pack();
        //( (WorkFilesFrame) containers.get(i)).getWorkFiles();
      }
      
    }
  }
  
  
}
