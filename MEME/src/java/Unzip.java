import java.io.*;
import java.util.*;
import java.util.zip.*;

/**

Program to unzip - for testing large ZIP files

 **/
public class Unzip {

    public static void main(String argv[]) {
	ZipFile zipfile;

	if (argv.length != 1) {
	    System.out.println("No zip file specified as argument");
	    System.exit(0);
	}

	try {
	    zipfile = new ZipFile(argv[0]);

	    make_dirs(zipfile);
	    Enumeration entries = zipfile.entries();

	    while (entries.hasMoreElements()) {
		ZipEntry entry = (ZipEntry) entries.nextElement();
		if (!entry.isDirectory()) {
		    extract(zipfile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(entry.getName())));
		}
	    }
	} catch (IOException e1) {
	    e1.printStackTrace();
	    System.exit(2);
	} catch (Exception e2) {
	    e2.printStackTrace();
	    System.exit(2);
	}
	System.exit(0);
    }

    public static void extract(InputStream in, OutputStream out) throws IOException {
	final int SIZE=1024*1024;
	byte[] buffer = new byte[SIZE];
	int len;

	while ((len = in.read(buffer)) >= 0) {
	    out.write(buffer, 0, len);
	}
	in.close();
	out.close();
    }

    // makes the directories first
    public static void make_dirs(ZipFile zipfile) {
	Enumeration entries = zipfile.entries();

	while (entries.hasMoreElements()) {
	    ZipEntry entry = (ZipEntry) entries.nextElement();
	    if (entry.isDirectory()) {
		System.out.println("Making dir: " + entry.getName());
		(new File(entry.getName())).mkdir();
	    }
	}
    }

}
