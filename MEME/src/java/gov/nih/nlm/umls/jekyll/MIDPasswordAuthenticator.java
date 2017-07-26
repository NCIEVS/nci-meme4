/*
 * MIDPasswordAuthenticator.java
 */

package gov.nih.nlm.umls.jekyll;

import gov.nih.nlm.meme.MEMEToolkit;
import gov.nih.nlm.meme.common.EditorPreferences;
import gov.nih.nlm.meme.common.PasswordAuthentication;
import gov.nih.nlm.meme.common.PasswordAuthenticator;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
class MIDPasswordAuthenticator extends PasswordAuthenticator {
    private PasswordAuthentication pa = null;

    public void setUsernameAndPassword(String username, char[] password) {
        super.setUsernameAndPassword(username, password);

        try {
            Date exp_date = JekyllKit.getAdminClient()
                    .getPasswordExpirationDate(username,
                            String.valueOf(password));

            EditorPreferences ep = JekyllKit.getAdminClient().authenticate(
                    username, new String(password));

            if (ep == null) {
                authenticationFailed("Invalid user/password.");
            } else {
                if (exp_date == null) {
                    MEMEToolkit.notifyUser("This is just warning.\n"
                            + "No password expiration date set for this user account.\n"
                            + "Please alert project's DBA about this.");
                    JekyllKit.setDaysLeftTillExpiration(-1);
                    return;
                }
                
                Calendar exp_time = Calendar.getInstance();                
                exp_time.setTime(exp_date);

                Calendar current_time = Calendar.getInstance();

                int days_left = 0;

                // The expiration date for user password can be in the next
                // year.
                // That's the rationale behind the IF below.
                if (exp_time.get(Calendar.YEAR) > current_time
                        .get(Calendar.YEAR)) {
                    Calendar end_of_this_year = Calendar.getInstance();
                    end_of_this_year.set(current_time.get(Calendar.YEAR), 11,
                            31, 23, 59);
                    days_left = exp_time.get(Calendar.DAY_OF_YEAR)
                            + (end_of_this_year.get(Calendar.DAY_OF_YEAR) - current_time
                                    .get(Calendar.DAY_OF_YEAR));
                } else {
                    days_left = exp_time.get(Calendar.DAY_OF_YEAR)
                            - current_time.get(Calendar.DAY_OF_YEAR);
                }

                JekyllKit.setDaysLeftTillExpiration(days_left);

                if (days_left < 5) {
                    MEMEToolkit
                            .notifyUser("Your password will expire in "
                                    + days_left
                                    + " days.\n"
                                    + "Please change it at your earliest convenience by clicking\n"
                                    + "Change Password Form on the Tools menu of the Main frame.\n");
                }

                pa = new PasswordAuthentication(username, password);

            }
        } catch (Exception ex) {
            // Matching an Oracle error code to check whether username/password is invalid.
            Pattern pattern_01017 = Pattern.compile("ORA-01017");
            Matcher matcher_01017 = pattern_01017.matcher(ex.toString());
            // Matching an Oracle error code to check whether account is locked.
            Pattern pattern_28000 = Pattern.compile("ORA-28000");
            Matcher matcher_28000 = pattern_28000.matcher(ex.toString());
            // Matching an Oracle error code to check whether password is expired.
            Pattern pattern_28001 = Pattern.compile("ORA-28001");
            Matcher matcher_28001 = pattern_28001.matcher(ex.toString());
            if (matcher_01017.find()) {
                authenticationFailed("Invalid user/password.");
            } else if (matcher_28001.find()) {
                authenticationFailed("Your password has been expired.\n"
                        + "Please ask project's DBA to reset a password for you.\n");
            } else if (matcher_28000.find()) {
                authenticationFailed("Your account is currently locked.\n"
                        + "Please contact project's DBA to resolve the issue.\n");
            } else {
                authenticationFailed("Failed to authenticate user.\n"
                        + "Log file may contain more information.");
                ex.printStackTrace(JekyllKit.getLogWriter());
            }
        }
    }

    public PasswordAuthentication getAuthentication() {
        return pa;
    }
}