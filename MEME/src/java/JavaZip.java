/**************************************************************************
 *
 * A Zip program using java.util.zip classes.
 * Needed to be able to compress files > 2.5GB and to create archives > 2.5GB
 *
 * Command line:
 * First argument must be the archive name e.g., 2004AA.ZIP
 * Then follows all the files and directories to be zip'ped.
 *
 * Properties:
 * excludes= (comma separated list of paths to be excluded)
 * level=BEST_SPEED, DEFAULT_COMPRESSION or BEST_COMPRESSION
 * buffersize=(size in bytes)
 * comment= (a comment stored in the ZIP file)
 *
 * suresh@nlm.nih.gov 12/2003
 *************************************************************************/

import java.util.zip.*;
import java.io.*;

public class JavaZip {
    public static final String DEFAULT_COMMENT = "UMLS Release Distribution";
    public static final int DEFAULT_BUFFERSIZE = 1024*1024*20;

    int size = DEFAULT_BUFFERSIZE;
    int level = Deflater.DEFAULT_COMPRESSION;
    String comment = DEFAULT_COMMENT;
    byte buffer[];
    ZipOutputStream zipStream;
    String excludes[];

    public void setComment(String s) {
	comment = s;
    }

    public void setLevel(int i) {
	level = i;
    }

    public void setBuffer(int i) {
	size = i;
    }

    public void setExcludes(String s[]) {
	excludes = s;
    }

    public void zip(String archive, String[] stuffToZip) {
	buffer = new byte[size];

	// remove archive
	// check that everything in stuffToZip exists

	try {
	    zipStream = new ZipOutputStream(new FileOutputStream(archive));
	    zipStream.setLevel(level);

	    for (int i=0; i<stuffToZip.length; i++) {
		doPath(stuffToZip[i]);
	    }
	    zipStream.close();

	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (FileNotFoundException e2) {
	    e2.printStackTrace();
	} catch (IOException e3) {
	    e3.printStackTrace();
	}
    }

    private void doPath(String path) throws IOException {
	File p = new File(path);
	boolean excludeThis = false;

	if (excludes != null) {
	    for (int i=0; i<excludes.length; i++) {
		if (p.getPath().equals(excludes[i])) {
		    System.out.println("Excluding: " + p.getPath());
		    excludeThis = true;
		}
	    }
	    if (excludeThis)
		return;
	}

	if (p.isDirectory()) {
	    doDir(p);
	} else if (p.isFile()) {
	    doFile(p);
	}
    }

    private void doDir(File d) throws IOException {
	String l[] = d.list();

	System.out.println("Doing directory: " + d.getPath());
	zipStream.putNextEntry(new ZipEntry(d.getPath() + File.separator));
	zipStream.closeEntry();
	for (int i=0; i<l.length; i++) {
	    if (l[i].equals(".") || l[i].equals(".."))
		continue;
	    doPath(d.getPath() + File.separator + l[i]);
	}
    }

    private void doFile(File f) throws IOException {
	FileInputStream inputStream;
	int len;

	try {
	    System.out.println("Compressing " + f.getPath() + "...");
	    inputStream = new FileInputStream(f);

	    zipStream.putNextEntry(new ZipEntry(f.getPath()));
	    while ((len = inputStream.read(buffer)) > 0) {
		zipStream.write(buffer, 0, len);
	    }
	    zipStream.closeEntry();
	    inputStream.close();

	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (FileNotFoundException e2) {
	    e2.printStackTrace();
	} catch (Exception e3) {
	    e3.printStackTrace();
	}
    }

    // first argument is the archive and second
    public static void main(String args[]) {
	String s;
	JavaZip zip = new JavaZip();

	s = System.getProperty("comment");
	if (s == null) {
	    zip.setComment(JavaZip.DEFAULT_COMMENT);
	} else {
	    zip.setComment(s);
	}

	s = System.getProperty("level");
	if (s == null) {
	    zip.setLevel(Deflater.DEFAULT_COMPRESSION);
	} else {
	    if (s.equalsIgnoreCase("DEFAULT_COMPRESSION")) {
		zip.setLevel(Deflater.DEFAULT_COMPRESSION);
	    } else if (s.equalsIgnoreCase("BEST_SPEED")) {
		zip.setLevel(Deflater.BEST_SPEED);
	    } else if (s.equalsIgnoreCase("BEST_COMPRESSION")) {
		zip.setLevel(Deflater.BEST_COMPRESSION);
	    } else {
		System.err.println("ERROR: level must be one of the values of java.util.zip.Deflater");
		System.exit(2);
	    }
	}

	s = System.getProperty("buffer");
	if (s == null) {
	    zip.setBuffer(DEFAULT_BUFFERSIZE);
	} else {
	    try {
		zip.setBuffer(Integer.parseInt(s));
	    } catch (NumberFormatException e) {
		System.err.println("ERROR: buffer must be an integer.");
		System.exit(2);
	    }
	}

	s = System.getProperty("excludes");
	if (s == null) {
	    zip.setExcludes(null);
	} else {
	    zip.setExcludes(s.split(","));
	}

	String stuff[] = new String[args.length-1];
	for (int i=1; i<args.length; i++) {
	    stuff[i-1] = args[i];
	}
	zip.zip(args[0], stuff);
    }
}
