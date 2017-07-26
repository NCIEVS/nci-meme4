/*
 * AuthenticatorRunnable
 * InitialVersion: Soma Lanka: 12/15/2005 -- Seibel Ticket Number: 1-70HJ5 : Authenticate the user 
 */
package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.Ostermiller.util.PasswordDialog;

public class AuthenticatorRunnable implements Runnable {
	private String service = null;

	public AuthenticatorRunnable(String service) {
		this.service = service;
	}
	public void run() {
		PasswordDialog p = new PasswordDialog(null, "Jekyll");
	    // put it in the center of the user's screen
	    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	    p.setLocation(d.width / 2, d.height / 2);
	    p.setVisible(true);
	
	    while (p != null) {
	        if (p.okPressed()) {
	        	String failedMsg = JekyllKit.authenticateUser(p.getName().toLowerCase(),p.getPass().toCharArray());
	            if ( failedMsg == null ){
	            	service = JekyllKit.getDataSource();
	            	p.dispose();
	                p = null;
	                break;
	            } else {
	                MEMEToolkit.notifyUser(p, failedMsg);
	                p.setVisible(true);
	            }
	        } else {
	            p=null;
	            
	        }
	    }
   	}
	public String getService() {
		return service;
	}

}
