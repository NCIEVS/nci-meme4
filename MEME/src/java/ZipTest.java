/**************************************************************************
 *
 * Test if files > 2.5GB can be zipped
 * suresh@nlm.nih.gov 12/2003
 *************************************************************************/

import java.util.zip.*;
import java.io.*;

public class ZipTest {
    String archive;
    String tozip[];

    public ZipTest(String archive, String[] tozip) {
	this.archive = archive;
	this.tozip = tozip;
    }

    public void zip() {
	int len;
	byte[] buffer = new byte[1024*1024];

	try {
	    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archive));
	    out.setLevel(Deflater.BEST_SPEED);
	    //	    out.setLevel(Deflater.DEFAULT_COMPRESSION);
	    //	    out.setLevel(Deflater.BEST_COMPRESSION);
	    for (int i=0; i<tozip.length; i++) {
		System.out.println("Compressing " + tozip[i] + "...");
		FileInputStream in = new FileInputStream(tozip[i]);
		out.putNextEntry(new ZipEntry(tozip[i]));

		while ((len = in.read(buffer)) > 0) {
		    out.write(buffer, 0, len);
		}

		out.closeEntry();
		in.close();
	    }
	    out.close();
	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (FileNotFoundException e2) {
	    e2.printStackTrace();
	} catch (IOException e3) {
	    e3.printStackTrace();
	}
    }

    // first argument is the archive and second
    public static void main(String args[]) {
	String files[] = new String[args.length-1];
	for (int i=1; i<args.length; i++) {
	    files[i-1] = args[i];
	}
	ZipTest z = new ZipTest(args[0], files);
	z.zip();
    }
}
