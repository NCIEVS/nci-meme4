/************************************************************************
 * Package:     gov.nih.nlm.util
 * Object:      ClasspathToolkit.java
 ***********************************************************************/
package gov.nih.nlm.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class sets the classpath (java.class.path) when Java Web Start is used.
 */
public class ClasspathToolkit {

  private static final FilenameFilter JAR_FILE_FILTER = new FilenameFilter() {
    public final boolean accept(File dir, String name) {
      if (name.endsWith("jar")) {
        return true;
      }
      return false;
    }
  };

  /*
   * .jar file with "RM" prefix is the real .jar file we want.
   * There are other prefixes like "RC" and "RT" but they are for
   * administration purposes.
   */
  private static final String JWS_REAL_JAR_PREFIX = "RM";

  /**
   * Returns the directory where the jar for the specified class is
   * installed.
   * @param clazz the class object the Main class.
   * @return the directory the .jar file, which contains the Main class.
   */
  private static final File getJarInstallDirectory(Class clazz) {
    File file = new File(clazz.getProtectionDomain().getCodeSource().
                         getLocation().getFile());
    return new File(file.getParent());
  }

  /**
   * Recursively finds jar files in the specified directory ("dir").
   * @param dir the {@link File} directory containing the jar files
   * @param jars the list of jar files in the directory
   */
  private static final void getJarFiles(File dir, Vector jars) {
    if (!dir.isDirectory()) {
      return;
    }

    /* get a list of .jar files */
    File[] entries = dir.listFiles(JAR_FILE_FILTER);
    String name = "";
    for (int i = 0; i < entries.length; i++) {
      name = entries[i].getName();
      //retrieve just a name.
      if (name.startsWith(JWS_REAL_JAR_PREFIX)) { // we want RMxxx.jar!
        jars.add(entries[i]);
      }
    }
    /* find directories */
    entries = dir.listFiles();
    for (int i = 0; i < entries.length; i++) {
      if (entries[i].isDirectory()) {
        getJarFiles(entries[i], jars);
      }
    }
  }

  /**
   * Returns the .jar files installed by Java(TM) Web Start.
   * We assume that all .jar files are in the sub-directory or the directory
   * where the .jar file of the specified class resides.
   * @param clazz the {@link Class} to look for
   * @return the set of jar files
   */
  private static final File[] getInstalledJarFiles(Class clazz) {
    File[] result = {};
    File parentDir = getJarInstallDirectory(clazz);
    if (parentDir == null) {
      return result;
    }

    /* search .jar files for each directories */
    Vector jars = new Vector();
    getJarFiles(parentDir, jars);
    result = new File[jars.size()];
    for (int i = 0; i < jars.size(); i++) {
      result[i] = (File) jars.get(i);
    }
    return result;
  }

  /**
   * Construct a string which can be used as a part of a classpath.
   * @param clazz the {@link Class} to search for
   * @return the classpath containing this class
   */
  private static final String getInstalledJarFilesAsClassPath(Class clazz) {
    File[] jars = getInstalledJarFiles(clazz);
    /* construct classpath */
    StringBuffer sbuf = new StringBuffer();
    for (int i = 0; i < jars.length; i++) {
      if (i > 0) {
        sbuf.append(java.io.File.pathSeparator);
      }
      sbuf.append(jars[i].getAbsolutePath());
    }
    return sbuf.toString();
  }

  /**
   * Sets the system classpath.
   * If the application is launched by Java(TM) Web Start, this method should
   * be called in order to set the classpath (java.class.path).
   * @param clazz the {@link Class} to search for
   */
  public static final void setSystemClasspathForJWS(Class clazz) {
    /* begin: set class path */
    String system_classpath = System.getProperty("java.class.path");
    StringTokenizer st = new StringTokenizer(system_classpath,
                                             java.io.File.pathSeparator);
    int tokenNum = st.countTokens();
    if (tokenNum > 0) {
      String firstToken = st.nextToken();
      if (firstToken.indexOf("javaws.jar") > -1) { // launched by JWS.
        /*
         * I'd like to put the additional classpath info
         * before the original system classpath.
         */
        system_classpath = getInstalledJarFilesAsClassPath(clazz)
            + java.io.File.pathSeparator + system_classpath;

        /* set java.class.path */
        System.setProperty("java.class.path", system_classpath);
      }
    }
    /* end: set class path */
  }

}
