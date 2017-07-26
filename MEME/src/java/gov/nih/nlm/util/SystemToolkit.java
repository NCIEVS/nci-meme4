/*****************************************************************************
 * Package: gov.nih.nlm.util
 * Object:  SystemToolkit
 *****************************************************************************/
package gov.nih.nlm.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.ProgressMonitor;

/**
 * Utility toolkit.
 *
 * @author  MEME Group
 */

public class SystemToolkit {

  private static final List stop_words = new ArrayList();
  static {
    stop_words.add("of");
    stop_words.add("and");
    stop_words.add("with");
    stop_words.add("for");
    stop_words.add("nos");
    stop_words.add("to");
    stop_words.add("in");
    stop_words.add("by");
    stop_words.add("on");
    stop_words.add("the");
  };

  /**
   * Checks whether the word is one of the stop words.
   * Assumes the word is lowercased
   * @param word lowercase string
   * @return <code>true</code> if the word is a stop word, <code>false</code> otherwise
   */
  public static boolean isStopWord(String word) {
    return stop_words.contains(word);
  }

  /**
   * Lowercases words and returns the list sorted and uniqed. Also removes
   * stop words.
   * @param words words from a string
   * @return sorted uniqued, lowercased word list
   */
  public static String[] getIndexWords(String[] words) {
    final List word_list = new ArrayList();
    Arrays.sort(words, String.CASE_INSENSITIVE_ORDER);
    String prev_word = "";
    for (int i = 0; i < words.length; i++) {
      final String word = words[i].toLowerCase();
      if (word.equals(prev_word)) {
        continue;
      }
      prev_word = word;
      if (isStopWord(word)) {
        continue;
      }
      word_list.add(word);
    }
    return (String[]) word_list.toArray(new String[0]);
  }

  /**
   * Copies the input file to the output file.
   * @param in the input {@link File}
   * @param out the output {@link File}
   * @throws IOException
   */
  public static void copy(File in, File out) throws IOException {
    copy(in, out, null);
  }

  /**
   * Copies the input file to the output file.  Supports ability to monitor progress
   * @param in the input {@link File}
   * @param out the output {@link File}
   * @param pm the {@link ProgressMonitor}
   * @throws IOException
   */
  public static void copy(File in, File out, ProgressMonitor pm) throws
      IOException {

    final int size = (int) (in.length() / (1024 * 1024.0));
    int kb1024_so_far = 0;
    if (pm != null) {
      pm.setMinimum(0);
      pm.setMaximum(size);
    }

    File canonical_in = in.getAbsoluteFile();
    File canonical_out = out.getAbsoluteFile();
    BufferedInputStream bin = new BufferedInputStream(
        new FileInputStream(canonical_in));
    BufferedOutputStream bout = new BufferedOutputStream(
        new FileOutputStream(canonical_out));
    byte[] bytes = new byte[1024 * 1024];
    int ct;
    while ( (ct = bin.read(bytes)) != -1) {
      bout.write(bytes, 0, ct);
      if (pm != null && ++kb1024_so_far < size) {
        pm.setProgress(kb1024_so_far);
        if (pm.isCanceled()) {
          bin.close();
          bout.close();
          return;
        }
      }
    }
    bin.close();
    bout.close();
  }

  /**
   * Compute the MD5 hash of a string.
   * @param text the value to compute a hash for
   * @return the MD5 hash value of the string
   * @throws NoSuchAlgorithmException if failed due to no such algorithm
   * @throws UnsupportedEncodingException if failed due to unsupported encoding
   */
  public static String md5(String text) throws NoSuchAlgorithmException,
      UnsupportedEncodingException {
    return md5(text, "UTF-8");
  }

  /**
   * Compute the MD5 hash of a string using the specified character encoding.
   * @param text the value to compute a hash for
   * @param char_encoding a character encoding (e.g. "UTF-8")
   * @return the MD5 hash value of the string
   * @throws NoSuchAlgorithmException if failed due to no such algorithm
   * @throws UnsupportedEncodingException if failed due to unsupported encoding
   */
  public static String md5(String text, String char_encoding) throws
      NoSuchAlgorithmException, UnsupportedEncodingException {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    if (text == null) {
      md5.update(new byte[0]);
    } else {
      md5.update(text.getBytes(char_encoding));
    }
    return toHexString(md5.digest());
  }

  /**
   * Returns the MD5 value for the given {@link File}.
   * @param file for which to determine the MD5
   * @return MD5 string
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static String md5(File file) throws IOException,
      NoSuchAlgorithmException {
    return md5(file, null);
  }

  /**
   * Returns the MD5 value for the given {@link File}.
   * @param file for which to determine the MD5
   * @param pm an optional progress monitor to track whether operation should be cancelled
   * @return MD5 string
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static String md5(File file, ProgressMonitor pm) throws IOException,
      NoSuchAlgorithmException {
    final int BUFSIZE = 1024 * 1024;
    byte buffer[];
    final int size = (int) (file.length() / ( (double) BUFSIZE)) + 1;
    int mb_so_far = 0;
    int l;

    if (pm != null) {
      pm.setMaximum(size);
      pm.setProgress(0);
    }

    MessageDigest md5 = MessageDigest.getInstance("MD5");
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
    buffer = new byte[BUFSIZE];

    while ( (l = in.read(buffer, 0, BUFSIZE)) > 0) {
      if (pm != null && ++mb_so_far < size) {
        pm.setProgress(mb_so_far);
        if (pm.isCanceled()) {
          in.close();
          return "Cancelled...";
        }
      }
      md5.update(buffer, 0, l);
    }
    in.close();

    if (l > 0) {
      md5.update(buffer, 0, l);
    }

    return toHexString(md5.digest());
  }

  /**
   * Returns the MD5 value for the given {@link File}.
   * @param file for which to determine the MD5
   * @return MD5 string
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static String md5CrossPlatform(File file) throws IOException,
      NoSuchAlgorithmException {
    return md5CrossPlatform(file, null);
  }

  /**
   * Returns the MD5 value for the given {@link File}.
   * @param file for which to determine the MD5
   * @param pm an optional progress monitor to track whether operation should be cancelled
   * @return MD5 string
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static String md5CrossPlatform(File file, ProgressMonitor pm) throws
      IOException, NoSuchAlgorithmException {
    final int size = (int) (file.length() / 1000);
    int mb_so_far = 0;

    if (pm != null) {
      pm.setMaximum(size);
      pm.setProgress(0);
    }

    MessageDigest md5 = MessageDigest.getInstance("MD5");
    FileInputStream fin = new FileInputStream(file);
    final byte[] lt = "\n".getBytes("UTF-8");
    BufferedReader in = new BufferedReader(new InputStreamReader(fin, "UTF-8"),
                                           1024 * 1024);
    String l;
    while ( (l = in.readLine()) != null) {
      mb_so_far = (int) (fin.getChannel().position() / 1000);
      if (pm != null && ++mb_so_far < size) {
        pm.setProgress(mb_so_far);
        if (pm.isCanceled()) {
          in.close();
          return "Cancelled...";
        }
      }
      md5.update(l.getBytes("UTF-8"));
      md5.update(lt);
    }
    in.close();

    return toHexString(md5.digest());
  }

  /**
   * Converts a byte<code>[]</code> to a hex string.
   * @param v the byte<code>[]</code>
   * @return the hex string
   */
  public static String toHexString(byte[] v) {
    StringBuffer sb = new StringBuffer();
    byte n1, n2;
    for (int c = 0; c < v.length; c++) {
      n1 = (byte) ( (v[c] & 0xF0) >>> 4);
      n2 = (byte) ( (v[c] & 0x0F));
      sb.append(n1 >= 0xA ? (char) (n1 - 0xA + 'a') : (char) (n1 + '0'));
      sb.append(n2 >= 0xA ? (char) (n2 - 0xA + 'a') : (char) (n2 + '0'));
    }
    return sb.toString();
  }

  /**
   * Returns an HTML document stripped of its tags.
   * @param html an html document
   * @return plain text document
   */
  public static String removeTags(String html) {

    int line_count = 0;

    StringBuffer sb = new StringBuffer(10000);
    boolean head_found = false; // search for <head
    boolean head_end = false; // search for </head>
    boolean script_found = false; // search for <script
    boolean script_end = false; // search for </script>
    boolean tag_found = false; // search for matching < && >
    boolean comment_found = false; // search for matching <! and ->

    // Loop through the document
    for (int i = 0; i < html.length(); i++) {
      char token = html.charAt(i);

      // skip head tag
      if (token == '<' && html.length() >= i + 4 &&
          String.valueOf(html.charAt(i + 1)).toLowerCase().equals("h") &&
          String.valueOf(html.charAt(i + 2)).toLowerCase().equals("e") &&
          String.valueOf(html.charAt(i + 3)).toLowerCase().equals("a") &&
          String.valueOf(html.charAt(i + 4)).toLowerCase().equals("d")) {
        head_found = true;
        continue;
      }
      if (token == '<' && html.length() >= i + 5 && html.charAt(i + 1) == '/' &&
          String.valueOf(html.charAt(i + 1)).toLowerCase().equals("h") &&
          String.valueOf(html.charAt(i + 2)).toLowerCase().equals("e") &&
          String.valueOf(html.charAt(i + 3)).toLowerCase().equals("a") &&
          String.valueOf(html.charAt(i + 4)).toLowerCase().equals("d") &&
          html.charAt(i + 5) == '>') {
        i = i + 5;
        head_end = true;
        continue;
      }
      if (head_found && !head_end) {
        continue;
      }

      // skip script tag
      if (token == '<' && html.length() >= i + 6 &&
          String.valueOf(html.charAt(i + 1)).toLowerCase().equals("s") &&
          String.valueOf(html.charAt(i + 2)).toLowerCase().equals("c") &&
          String.valueOf(html.charAt(i + 3)).toLowerCase().equals("r") &&
          String.valueOf(html.charAt(i + 4)).toLowerCase().equals("i") &&
          String.valueOf(html.charAt(i + 5)).toLowerCase().equals("p") &&
          String.valueOf(html.charAt(i + 6)).toLowerCase().equals("t")) {
        script_found = true;
        script_end = false; // need to switch off for multiple script tag
        continue;
      }
      if (token == '<' && html.length() >= i + 7 && html.charAt(i + 1) == '/' &&
          String.valueOf(html.charAt(i + 2)).toLowerCase().equals("s") &&
          String.valueOf(html.charAt(i + 3)).toLowerCase().equals("c") &&
          String.valueOf(html.charAt(i + 4)).toLowerCase().equals("r") &&
          String.valueOf(html.charAt(i + 5)).toLowerCase().equals("i") &&
          String.valueOf(html.charAt(i + 6)).toLowerCase().equals("p") &&
          String.valueOf(html.charAt(i + 7)).toLowerCase().equals("t") &&
          html.charAt(i + 8) == '>') {
        i = i + 8;
        script_end = true;
        script_found = false; // need to switch off for multiple script tag
        continue;
      }
      if (script_found && !script_end) {
        continue;
      }

      // skip html comment
      if (token == '<' && html.length() >= i + 1 && html.charAt(i + 1) == '!') {
        comment_found = true;
        continue;
      }
      if (comment_found) {
        if (token == '>' && i > 0 && html.charAt(i - 1) == '-') {
          comment_found = false;
        }
        continue;
      }

      // skip html tag
      if (token == '<') {
        tag_found = true;
        continue;
      }
      if (tag_found) {
        // replace br to line.separator
        if (String.valueOf(token).toLowerCase().equals("b") &&
            html.length() >= i + 1 &&
            String.valueOf(html.charAt(i + 1)).toLowerCase().equals("r")) {
          sb.append(System.getProperty("line.separator"));
          line_count++;
        }

        if (token == '>') {
          tag_found = false;

        }
        continue;
      }

      // Handle &nbsp; and &#160;
      if (token == '&' && html.length() > i + 5 &&
          String.valueOf(html.charAt(i + 1)).toLowerCase().equals("n") &&
          String.valueOf(html.charAt(i + 2)).toLowerCase().equals("b") &&
          String.valueOf(html.charAt(i + 3)).toLowerCase().equals("s") &&
          String.valueOf(html.charAt(i + 4)).toLowerCase().equals("p") &&
          String.valueOf(html.charAt(i + 5)).toLowerCase().equals(";")) {
        sb.append(" ");
        i = i + 5;
        continue;
      }

      if (token == '&' && html.length() > i + 5 &&
          String.valueOf(html.charAt(i + 1)).toLowerCase().equals("#") &&
          String.valueOf(html.charAt(i + 2)).toLowerCase().equals("1") &&
          String.valueOf(html.charAt(i + 3)).toLowerCase().equals("6") &&
          String.valueOf(html.charAt(i + 4)).toLowerCase().equals("0") &&
          String.valueOf(html.charAt(i + 5)).toLowerCase().equals(";")) {
        sb.append(" ");
        i = i + 5;
        continue;
      }

      // Handle &lt; and &gt;
      if (token == '&' && html.length() > i + 3 &&
          String.valueOf(html.charAt(i + 1)).toLowerCase().equals("l") &&
          String.valueOf(html.charAt(i + 2)).toLowerCase().equals("t") &&
          String.valueOf(html.charAt(i + 3)).toLowerCase().equals(";")) {
        sb.append("<");
        i = i + 3;
        continue;
      }

      if (token == '&' && html.length() > i + 3 &&
          String.valueOf(html.charAt(i + 1)).toLowerCase().equals("g") &&
          String.valueOf(html.charAt(i + 2)).toLowerCase().equals("t") &&
          String.valueOf(html.charAt(i + 3)).toLowerCase().equals(";")) {
        sb.append(">");
        i = i + 3;
        continue;
      }

      if (token != '\r' && token != '\n') {
        sb.append(token);

      }
    }

    return sb.toString();
  }

  /**
   * Removes any links within a specified HTML document.
   * @param html an html document
   * @return document without any links
   */
  public static String removeLinks(String html) {

    StringBuffer sb = new StringBuffer(10000);
    boolean tag_found = false; // search for matching <a></a> tags

    // Loop through the document
    for (int i = 0; i < html.length(); i++) {
      char token = html.charAt(i);

      // skip html tag
      if (token == '<' && html.length() > i + 1 &&
          (String.valueOf(html.charAt(i + 1)).toLowerCase().equals("\\") ||
           String.valueOf(html.charAt(i + 1)).toLowerCase().equals("a"))) {
        tag_found = true;
        continue;
      }
      if (tag_found) {
        if (token == '>') {
          tag_found = false;
        }
        continue;
      }
      sb.append(token);
    }

    return sb.toString();
  }

  /**
   * Unzips the specified file into the specified directory using.  Makes use
   * of the system unzip.native property to determine whether to use the unzip
       * program stored in unzip.path system property or to use a pure java solution.
   *
   * @param zip_file the zip file name
   * @param output_dir the directory to unzip to
   * @param archive_subdir the portion of the archive to extract
   * @param pm the progrss monitor
   * @throws IOException
   */
  public static void unzip(final String zip_file, final String output_dir,
                           final String archive_subdir,
                           final ProgressMonitor pm) throws IOException {

    final String unzip_native = System.getProperty("unzip.native");
    if (unzip_native != null && unzip_native.equals("true")) {
      unzip(System.getProperty("unzip.path"), zip_file, output_dir,
            archive_subdir, pm);
    } else {
      unzipInternal(zip_file, output_dir, archive_subdir, pm);
    }
  }

  /**
   * Unzips the specified file into the specified directory using
   * the specified operating system command.  This method makes use
   * of a progress monitor (if usingView() returns true).
   * @param unzip_cmd the OS command to use to unzip
   * @param zip_file the zip file name
   * @param output_dir the directory to unzip to
   * @param archive_subdir the portion of the archive to extract
   * @param pm the progrss monitor
   * @throws IOException
   */
  public static void unzip(final String unzip_cmd, final String zip_file,
                           final String output_dir, final String archive_subdir,
                           final ProgressMonitor pm) throws IOException {

    Process p = null;
    try {

      //
      // Count entries (only if using progress monitor)
      //
      int ct = 0;
      if (pm != null) {
        ZipFile zf = new ZipFile(zip_file);
        Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
          ct++;
          e.nextElement();
        }
        pm.setMaximum(ct);
        pm.setProgress(1);
        zf.close();
      }

      //
      // Execute command
      //
      String[] cmd = new String[] {
          unzip_cmd, "-o", zip_file, archive_subdir, "-d", output_dir};
      p = Runtime.getRuntime().exec(cmd);

      //
      // Read from input stream
      //
      BufferedReader in =
          new BufferedReader(
          new InputStreamReader(
          p.getInputStream()));
      String line;
      ct = 0;
      while ( (line = in.readLine()) != null) {
        //
        // If progress was cancelled, return false
        //
        if (pm != null) {
          if (pm.isCanceled()) {
            p.destroy();
            return;
          }
          pm.setNote("Done" + line);
          pm.setProgress(++ct);
        } else {
          System.out.println("Done" + line);
        }
      }

      //
      // Wait for process to complete, throw exception if bad return value
      //
      p.waitFor();
      if (p.exitValue() != 0) {
        throw new Exception("Bad Return Value");
      }

    } catch (Exception e) {

      //
      // Attempt to get error message
      //
      StringBuffer err_msg = new StringBuffer();
      try {
        BufferedReader err =
            new BufferedReader(
            new InputStreamReader(
            p.getErrorStream()));
        String line;
        while ( (line = err.readLine()) != null) {
          err_msg.append(line);
        }
      } catch (Exception e2) {}

      IOException ioe = new IOException(err_msg.toString());
      ioe.initCause(e);
      throw ioe;
    }
    return;
  }

  /**
   * Unzips the specified file into the specified directory using pure java.
   * @param zip_file the {@link ZipFile} name to unzip
   * @param output_dir the output directory
   * @param archive_subdir indicates what to unzip, (use * for everything)
   * @throws IOException if anything goes wrong manipulating the files.
   */
  public static void unzipInternal(final String zip_file,
                                   final String output_dir,
                                   final String archive_subdir) throws
      IOException {
    unzipInternal(zip_file, output_dir, archive_subdir, null);
  }

  /**
   * Unzips the specified file into the specified directory using pure java.
   * @param zip_file the {@link ZipFile} name to unzip
   * @param output_dir the output directory
   * @param archive_subdir indicates what to unzip, (use * for everything)
   * @param pm the {@link ProgressMonitor} used to track progress.
   * @throws IOException if anything goes wrong manipulating the files.
   */
  public static void unzipInternal(final String zip_file,
                                   final String output_dir,
                                   final String archive_subdir,
                                   final ProgressMonitor pm) throws IOException {

    String pattern = archive_subdir.replaceAll("\\*", ".*");

    //
    // Count entries (only if using progress monitor)
    //
    int ct = 0;
    if (pm != null) {
      ZipFile zf = new ZipFile(zip_file);
      Enumeration e = zf.entries();
      while (e.hasMoreElements()) {
        ZipEntry ze = (ZipEntry) e.nextElement();
        if (ze.getName().matches(pattern)) {
          ct++;
        }
      }
      pm.setMaximum(ct);
      pm.setProgress(1);
      zf.close();
    }

    //
    // Read from input stream and write to output stream
    //
    ZipFile zf = new ZipFile(zip_file);
    Enumeration e = zf.entries();
    int local_ct = 0;
    while (e.hasMoreElements()) {
      ZipEntry this_entry = (ZipEntry) e.nextElement();

      //
      // Only process those matching entries
      //
      if (this_entry.getName().matches(pattern)) {

        //
        // If it's a directory, make it
        //
        if (this_entry.isDirectory()) {
          File new_dir = new File(output_dir, this_entry.getName());
          if (!new_dir.exists()) {
            new_dir.mkdirs();
          }
          local_ct++;
        }

        //
        // If it is a file, write it.  Don't use character strings but
        // write raw bytes instead
        //
        else {

          BufferedInputStream in =
              new BufferedInputStream(zf.getInputStream(this_entry));

          File output_file = new File(output_dir, this_entry.getName());
          if (!output_file.getParentFile().exists()) {
            output_file.getParentFile().mkdirs();
          }
          BufferedOutputStream out = new BufferedOutputStream(
              new FileOutputStream(output_file));

          local_ct++;
          byte[] bytes = new byte[1024 * 1024];
          int byte_ct;
          while ( (byte_ct = in.read(bytes)) != -1) {
            out.write(bytes, 0, byte_ct);

            //
            // If progress was cancelled, return false
            //
            if (pm != null) {
              if (pm.isCanceled()) {
                in.close();
                out.close();
                return;
              }
              pm.setNote(this_entry.getName());
              pm.setProgress(local_ct);
            }
          }
          in.close();
          out.close();
        }
      }
    }
    return;
  }

  /**
   * Seek to the location in the {@link RandomAccessFile} where the first
   * line of text starts with the search string.  This implements a
       * binary search function in a file (like the UNIX <code>look</codE> command).
   * @param raf the {@link RandomAccessFile} to search
   * @param search_string the search string
   * @param char_set the character set to use for the search_string
   * @return the index into the file where to start looking
   * @throws IOException if anything goes wrong
   */
  public static long seekstr(RandomAccessFile raf, String search_string,
                             String char_set) throws
      IOException {
    // file length
    final long size = raf.length();

    // low pos
    long l = 0;

    // high pos
    long h = size - 1;

    // current pos
    long pos = 0;
    raf.seek(0);
    String line = null;

    while (l < h - 16) {
      pos = (long) Math.floor( (l + h) / 2.0);
      raf.seek(pos);
      if (pos != 0) {

        //raf.readLine();
        readLine(raf, char_set);
        //line = raf.readLine();
      }
      line = readLine(raf, char_set);
      if (line == null || line.equals("")) {
        h = pos - 1;
        continue;
      }
      if (line.compareTo(search_string) >= 0) {
        h = pos - 1;
      } else {
        l = pos + 1;

      }
    }
    raf.seek(l);
    if (l != 0) {

      //raf.readLine();
      readLine(raf, char_set);
    }
    pos = raf.getFilePointer();
    while (true) {
      //line = raf.readLine();
      line = readLine(raf, char_set);
      if (line == null || line.equals("")) {
        return -1;
      }
      if (line.startsWith(search_string)) {
        raf.seek(pos);
        return pos;
      } else if (line.compareTo(search_string) > 0) {
        return -1;
      }
      pos = raf.getFilePointer();
    }

  }

  /**
   * Reads and returns a line from the {@link RandomAccessFile} using
   * the specified character set.
   * @param raf the {@link RandomAccessFile}
   * @param char_set the character set
   * @return a line from the {@link RandomAccessFile} in the specified character set
   * @throws IOException
   */
  public static String readLine(RandomAccessFile raf, String char_set) throws
      IOException {
    long pos = raf.getFilePointer();
    final byte[] buf = new byte[256];
    final StringBuffer sb = new StringBuffer(150);
    long ct = 0;
    // Read 1024 bytes at a time
    while ( (ct = raf.read(buf)) != -1) {

      // look for new line/line feed
      for (int i = 0; i < ct; i++) {
        if (buf[i] == '\n' || buf[i] == '\r') {
          // seek to correct file position (after newline)
          if (i < 255 && (buf[i + 1] == '\n' || buf[i + 1] == '\r')) {
            raf.seek(pos + i + 2);
          } else {
            raf.seek(pos + i + 1);
            // Append string up to newline to buffer && return
          }
          sb.append(new String(buf, 0, i, char_set));
          return sb.toString();
        }
      }
      sb.append(new String(buf, char_set));
      pos += 256;
    }
    return sb.toString();
  }

  /**
   * Returns an {@link InputStream} for the {@link ZipEntry} in
   * the specified {@link ZipFile}. Uses the native unzip program
   * found in the system "unzip.path" property.
   * @param zf the {@link ZipFile}
   * @param ze the {@link ZipEntry}
   * @return the {@link InputStream}
   * @throws IOException if anything goes wrong
   */
  public static InputStream getZipInputStream(String zf, String ze) throws
      IOException {
    try {
      final String unzip_native = System.getProperty("unzip.native");

      //
      // Use native program
      //
      if (unzip_native != null && unzip_native.equals("true")) {

        final String[] cmd = new String[] {
            System.getProperty("unzip.path"),
            "-p", zf, ze
        };
        final Process p = Runtime.getRuntime().exec(cmd);

        final InputStream is = new BufferedInputStream(p.getInputStream()) {
          public void close() throws IOException {
            super.close();
            p.destroy();
          }
        };
        return is;

      }

      //
      // Use pure-java solution
      //
      else {
        final ZipFile zip_file = new ZipFile(zf);
        final Enumeration e = zip_file.entries();
        ZipEntry zip_entry = null;
        while (e.hasMoreElements()) {
          zip_entry = (ZipEntry) e.nextElement();
          if (zip_entry.getName().equals(ze)) {
            break;
          }
        }
        return zip_file.getInputStream(zip_entry);
      }
    } catch (Exception e) {
      IOException ioe = new IOException("falied to get zip input stream");
      ioe.initCause(e);
      throw ioe;
    }

  }

  /**
   * Returns a list of zip entry file names.  Uses the native unzip program
   * found in the system "unzip.path" property.
   * @param zip_file_name the zip file name
   * @return a list of zip entry file names
   * @throws IOException
   */
  public static String[] getZipEntries(String zip_file_name) throws IOException {
    try {
      final String unzip_native = System.getProperty("unzip.native");

      //
      // Use native program
      //
      if (unzip_native != null && unzip_native.equals("true")) {
        final String[] cmd = new String[] {
            System.getProperty("unzip.path"), "-l",
            zip_file_name};
        final Process p = Runtime.getRuntime().exec(cmd);
        String line = null;
        final List results = new ArrayList(500);
        final BufferedReader in = new BufferedReader(new InputStreamReader(
            p.getInputStream(), "UTF-8"));
        boolean found = false;
        while ( (line = in.readLine()) != null) {
          final String[] tokens = line.split("\\s+");
          if (!found && line.indexOf("-----") != -1) {
            found = true;
          } else if (found && line.indexOf("-----") != -1) {
            found = false;
          } else if (found) {
            results.add(tokens[tokens.length - 1]);
          }
        }
        p.waitFor();
        return (String[]) results.toArray(new String[0]);
      }

      //
      // Use pure-java solution
      //
      else {
        ZipFile zf = new ZipFile(zip_file_name);
        final List results = new ArrayList();
        Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
          results.add( ( (ZipEntry) e.nextElement()).getName());
        }
        zf.close();
        return (String[]) results.toArray(new String[0]);
      }
    } catch (Exception e) {
      IOException ioe = new IOException(e.getMessage());
      ioe.initCause(e);
      throw ioe;
    }
  }

  public static void main(String[] s) {
    try {

      System.out.println("Sorting: "+ s[0] + " " + new java.util.Date());
      sort(s[0]);
      System.out.println("Done: " + new java.util.Date());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Sort the specified file.
   * @param filename the file to sort
   * @throws IOException if failed to sort
   */
  public static void sort(String filename) throws IOException {
    sort(filename, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ( (String) o1).compareTo((String)o2);
      }
    });
  }

  /**
   * Sort the specified file.
   * @param filename the file to sort
   * @param pm {@link ProgressMonitor}
   * @throws IOException if failed to sort
   */
  public static void sort(String filename, ProgressMonitor pm) throws
      IOException {
    sort(filename, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ( (String) o1).compareTo((String)o2);
      }
    }

    , false, pm);
  }

  /**
   * Sort the specified file (optionally uniquely).
   * @param filename the file to sort
   * @param unique a <code>boolean</code> which detemine duplicate lines
   * @throws IOException if failed to sort
   */
  public static void sort(String filename, boolean unique) throws IOException {
    sort(filename, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ( (String) o1).compareTo((String)o2);
      }
    }

    , unique);
  }

  /**
   * Sort the specified file (optionally uniquely).
   * @param filename the file to sort
   * @param unique a <code>boolean</code> which detemine duplicate lines
   * @param pm {@link ProgressMonitor}
   * @throws IOException if failed to sort
   */
  public static void sort(String filename, boolean unique, ProgressMonitor pm) throws
      IOException {
    sort(filename, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ( (String) o1).compareTo((String)o2);
      }
    }

    , unique, pm);
  }

  /**
   * Sort the specified file using the specified {@link Comparator}.
   * @param filename the file to sort
   * @param comp the {@link Comparator}
   * @throws IOException if failed to sort
   */
  public static void sort(String filename, Comparator comp) throws IOException {
    sort(filename, comp, false);
  }

  /**
   * Sort the specified file using the specified {@link Comparator} and
   * optionally sort uniquely.
   * @param filename the file to sort
   * @param comp the {@link Comparator}
   * @param unique a <code>boolean</code> which detemine duplicate lines
   * @throws IOException if failed to sort
   */
  public static void sort(String filename, Comparator comp, boolean unique) throws
      IOException {
    sort(filename, comp, unique, null);
  }

  /**
   * Sort the specified file using the specified {@link Comparator} and
   * optionally sort uniquely.
   * @param filename the file to sort
   * @param comp the {@link Comparator}
   * @param unique a <code>boolean</code> which detemine duplicate lines
   * @param pm a {@link ProgressMonitor}
   * @throws IOException if failed to sort
   */
  public static void sort(String filename, Comparator comp, boolean unique,
                          ProgressMonitor pm) throws IOException {

    //
    // Initialize Progress Monitor
    //
    if (pm != null) {
      pm.setMaximum(100);
      pm.setProgress(0);
    }

    //
    // Vars
    //
    List lines = null;
    List files1 = new ArrayList();
    List files2 = new ArrayList();
    String line;
    final File orig_file = new File(filename).getAbsoluteFile();
    final File sortdir = new File(orig_file.getParent());

    //
    // Open file
    //
    final long orig_file_size = orig_file.length();
    final FileInputStream fis = new FileInputStream(orig_file);
    final UTF8InputStreamReader uisr = new UTF8InputStreamReader(new BufferedInputStream(fis));
    final BufferedReader in = new BufferedReader(uisr, 5 * 1024 * 1024);

    //
    // Break input file into files with max size of 16MB and then sort it
    //
    int size_so_far = 0;
    final int segment_size = 32*1024*1024;
    while ( (line = in.readLine()) != null) {
      if (size_so_far == 0) {
        lines = new ArrayList(10000);
      }
      lines.add(line);
      size_so_far += line.length();
      if (size_so_far > segment_size) {
        sortHelper( (String[]) lines.toArray(new String[0]), files1, comp,
                   sortdir, unique, uisr.hasByteOrderMark());
        size_so_far = 0;

        //
        // Update pm (up to 20%) for this first stage
        //
        if (pm != null) {
          int pct = (int) ( (fis.getChannel().position() * 18) / orig_file_size);
          pm.setProgress(pct);
        }
      }
    }

    //
    // If there are left-over lines, create final tmp file
    //
    if (lines != null && lines.size() != 0 && size_so_far <= segment_size) {
      sortHelper( (String[]) lines.toArray(new String[0]), files1, comp,
                 sortdir, unique, uisr.hasByteOrderMark());
    }

    //
    // Set progress to 18%
    //
    if (pm != null) {
      pm.setProgress(18);
    }
    in.close();

    //
    // Calculations for pm
    //
    int total_files = files1.size();
    int tmp = total_files;
    while (tmp > 1) {
      tmp = (int) Math.ceil(tmp / 2.0);
      total_files += tmp;
    }

    //
    // Merge sorted files
    //
    tmp = 0;
    while (files1.size() > 1) {
      for (int i = 0; i < files1.size(); i += 2) {
        tmp += 2;

        //
        // Calculate progress (from 18-98%)
        //
        if (pm != null) {
          pm.setProgress(18 + ( (tmp * (98 - 18)) / total_files));
        }

        if (files1.size() == i + 1) {
          files2.add(files1.get(i));
          break;
        } else {
          final File f = mergeSortedFiles( (File) files1.get(i),
                                    (File) files1.get(i + 1),
                                    comp, sortdir, unique, uisr.hasByteOrderMark());
          files2.add(f);
          ( (File) files1.get(i)).delete();
          ( (File) files1.get(i + 1)).delete();
        }
      }
      files1 = new ArrayList(files2);
      files2.clear();
    }

    //
    // Set progress to 99
    //
    if (pm != null) {
      pm.setProgress(98);
    }
    if (files1.size() > 0) {
      orig_file.delete();
      ( (File) files1.get(0)).renameTo(orig_file);
    }

    //
    // Set progress to 99
    //
    if (pm != null) {
      pm.setProgress(99);
    }

  }

  /**
   * Helper function to perform sort operations.
   * @param lines the lines to sort
   * @param all_tmp_files the list of files
   * @param comp the comparator
   * @param sortdir the sort dir
   * @param unique whether or not to sort unique
   * @param bom_present indicates if a Byte Order Mark was present on file
   * @throws IOException
   */
  private static void sortHelper(String[] lines, List all_tmp_files,
                                 Comparator comp, File sortdir, boolean unique,
                                 boolean bom_present) throws
      IOException {

    //
    // Create temp file
    //
    final File f = File.createTempFile("t+~", ".tmp", sortdir);

    //
    // Sort data for this segment
    //
    Arrays.sort(lines, comp);

    //
    // Write lines to file f
    //
    final BufferedWriter out =
        new BufferedWriter(new UTF8OutputStreamWriter(new
        FileOutputStream(f), bom_present), 5 * 1024 * 1024);
    String prev_line = null;
    for (int i = 0; i < lines.length; i++) {
      final String line = lines[i];
      if (!unique || !line.equals(prev_line)) {
        out.write(line);
        out.newLine();
        //out.flush();
      }
      prev_line = line;
    }

    out.flush();
    out.close();

    all_tmp_files.add(f);
  }

  /**
   * Merge-sort two files.
   * @return the sorted {@link File}
   * @param files1 the first set of files
   * @param files2 the second set of files
   * @param comp the comparator
   * @param dir the sort dir
   * @param unique whether or not to sort unique
   * @param bom_present indicates if a Byte Order Mark was present on file
   * @throws IOException
   */
  private static File mergeSortedFiles(File files1, File files2,
                                       Comparator comp, File dir,
                                       boolean unique,
                                       boolean bom_present) throws IOException {
    final BufferedReader in1 =
        new BufferedReader(new UTF8InputStreamReader(
        new BufferedInputStream(new FileInputStream(files1))), 5 * 1024 * 1024);
    final BufferedReader in2 =
        new BufferedReader(new UTF8InputStreamReader(
        new BufferedInputStream(new FileInputStream(files2))), 5 * 1024 * 1024);

    final File out_file = File.createTempFile("t+~", ".tmp", dir);

    final BufferedWriter out =
        new BufferedWriter(new UTF8OutputStreamWriter(new
        FileOutputStream(out_file), bom_present), 5 * 1024 * 1024);

    String line1 = in1.readLine();
    String line2 = in2.readLine();
    String line = null;
    String prev_line = null;
    while (line1 != null || line2 != null) {
      if (line1 == null) {
        line = line2;
        line2 = in2.readLine();
      } else if (line2 == null) {
        line = line1;
        line1 = in1.readLine();
      } else if (comp.compare(line1, line2) < 0) {
        line = line1;
        line1 = in1.readLine();
      } else {
        line = line2;
        line2 = in2.readLine();
      }
      if (!unique || !line.equals(prev_line)) {
        out.write(line);
        out.newLine();
      }
      prev_line = line;
    }
    out.flush();
    out.close();
    in1.close();
    in2.close();
    return out_file;
  }

  /**
   * Reads the fielded file into a two-dimensional string array.
   * @param file the file to read
   * @param delim the field separator in the file
   * @return a {@link String}<code>[][]</code> of the lines/fields
   * @throws IOException if anything goes wrong
   */
  public static String[][] readFieldedFile(File file, String delim) throws
      IOException {

    final List results = new ArrayList();

    //
    // Open file (read as UTF-8)
    //
    BufferedReader in =
        new BufferedReader(
        new UTF8InputStreamReader(
        new BufferedInputStream(
        new FileInputStream(file))));

    String line;
    while ( (line = in.readLine()) != null) {

      if (line.charAt(0) == '#') {
        continue;
      }

      final String[] fields = line.split("[" + delim + "]");
      results.add(fields);

    }

    in.close();

    return (String[][]) results.toArray(new String[0][0]);
  }

  /**
   * Reads the fielded file into a two-dimensional string array.
   * @param in the {@link BufferedReader} input
   * @param delim the field separator in the file
   * @return a {@link String}<code>[][]</code> of the lines/fields
   * @throws IOException if anything goes wrong
   */
  public static String[][] readFieldedReader(BufferedReader in, String delim) throws
      IOException {

    final List results = new ArrayList();

    //
    // Open file (read as UTF-8)
    //
    String line;
    while ( (line = in.readLine()) != null) {

      if (line.charAt(0) == '#') {
        continue;
      }

      final String[] fields = line.split("[" + delim + "]");
      results.add(fields);

    }

    in.close();

    return (String[][]) results.toArray(new String[0][0]);
  }
}