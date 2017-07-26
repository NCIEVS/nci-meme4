/*****************************************************************************
 *
 * Package: gov.nih.nlm.meme
 * Object:  MEMEMail
 *
 *****************************************************************************/

package gov.nih.nlm.meme;

import gov.nih.nlm.meme.exception.MailException;

import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Utility for sending email messages from MEME applications.
 * <p>
 * To use this class, set the various <code>public</code> fields
 * that specify the to and from lists as well as the mail server.
 * Then you call the {@link #send(String,String)} method to
 * send a message with a specified subject.  For example,
 * <pre>
 * try {
 *   // Set who the mail is from
 *   MEMEMail mail = new MEMEMail();
 *   mail.from = "from_address@anywhere.com";
 *
 *   // Set who is on the to list, remember it is a String array
 *   mail.to = new String[] {"to1@anywhere.com", "to2@anywhere.com"};
 *
 *   // If people should appear on the cc list instead of the to list
 *   // specify them here, again it is a String array
 *   mail.cc = new String[] {"cc1@anywhere.com","cc2@anywhere.com"};
 *
 *   // You can even specify addressees on the bcc list
 *   mail.bcc = new String[] {"bcc1@anywhere.com"}
 *
 *   // Then, set the mail host
 *   mail.smtp_host = "mail.anywhere.com";
 *
 *   // Finally, send your message
 *   String subject = "This is my subject";
 *   String message = "This is my message";
 *   mail.send(subject,message);
 *
 * } catch (MailException e) {
 *    // Something went wrong
 * }
 * </pre>
 *
 * In order to ensure thread safety, one of two things should be done.
 * Either use a new <code>MEMEMail</code> object each time a message
 * is to be set, or wrap a use of <code>MEMEMail</code> in a synchronized
 * block.
 *
 * @author MEME Group
 */

public class MEMEMail {

  //
  // Fields
  //

  /**
   * The address that messages will appear as though they come from.
   */
  public String from = null; // Who the message is from

  /**
   * The list of addresses to send the message to.
   */
  public String[] to = null; // Who the message is to

  /**
   * The list of addresses that comprise the CC list for the message.
   */
  public String[] cc = null; // Who is on the CC list

  /**
   * The list of addresses that comprise the BCC list for the message.
   */
  public String[] bcc = null; // Who is on the BCC list

  /**
   * The SMTP host that will be transmitting the message.
   */
  public String smtp_host = null; // mail server?

  //
  // Static Methods
  //

  /**
   * Sends the specified message with the specified subject,
   * from the specified sender, to the specified recipient list
   * using the specified mail host.  This is a convenience method
   * to avoid having to set the public fields.  Note, it clears
   * the values of the {@link #bcc} and {@link #cc} public fields.
   * @param subject The subject of the message
   * @param message_text The message
   * @param from The email address of the sender
   * @param to a {@link String}<code>[]</code> containing the recipients
   * @param smtp_host the mail server domain name
   * @throws MailException if there were any problems sending the message
   */
  public void send(String subject, String message_text,
                   String from, String[] to, String smtp_host) throws
      MailException {

    // configure
    this.from = from;
    this.to = to;
    this.cc = null;
    this.bcc = null;
    this.smtp_host = smtp_host;

    // send
    send(subject, message_text);
  }

  /**
   * Sends the specified message with the specified subject
   * to the addresses listed in the <code>public</code>
   * {@link #to}, {@link #cc}, and {@link #bcc} attributes.
   * The message will come from the address listed in {@link #from}.
   * @param subject the subject of the message
   * @param message_text the message
   * @throws MailException if there were any problems sending the message
   */
  public void send(String subject, String message_text) throws MailException {

    // Get system properties
    Properties props = System.getProperties();

    // Setup mail server
    if (smtp_host == null) {
      throw new MailException("An SMTP host must be specified.");
    }
    props.put("mail.smtp.host", smtp_host);

    // Get session
    Session session = Session.getInstance(props, null);

    // Define message, set Mailer header
    MimeMessage message = new MimeMessage(session);
    try {
      message.setHeader("X-Mailer", "MEMEMail");
    } catch (Exception e) {
      throw new MailException("Failed to set message header.", null, e);
    }

    // If the from address is null, throw an exception
    // else set the from for this message
    if (from == null) {
      throw new MailException("A sender must be specified.");
    }

    try {
      message.setFrom(new InternetAddress(from));
    } catch (Exception e) {
      throw new MailException("Failed to set sender.", from, e);
    }

    // Add "to" recipients
    if (to == null || to.length == 0) {
      throw new MailException("A recipient must be specified.");
    }

    try {
      for (int i = 0; i < to.length; i++) {
        message.addRecipient(Message.RecipientType.TO,
                             new InternetAddress(to[i]));
      }

      if (cc != null) {
        // Add CC recipients
        for (int i = 0; i < cc.length; i++) {
          message.addRecipient(Message.RecipientType.CC,
                               new InternetAddress(cc[i]));
        }
      }

      if (bcc != null) {
        // Add BCC recipients
        for (int i = 0; i < bcc.length; i++) {
          message.addRecipient(Message.RecipientType.BCC,
                               new InternetAddress(bcc[i]));
        }
      }

    } catch (Exception e) {
      throw new MailException("Failed to add recipient.", null, e);
    }

    // Set the subject and the text of the message
    try {
      message.setSubject(subject);
    } catch (Exception e) {
      throw new MailException("Failed to set subject.", subject, e);
    }

    try {
      message.setText(message_text);
    } catch (Exception e) {
      throw new MailException("Failed to set message text.", message_text, e);
    }

    // Send message
    // This may produce an exception
    try {
      Transport.send(message);
    } catch (Exception e) {
      throw new MailException("Failed to send message.", message, e);
    }

  }; // end public static void send (subject,message_text)

  /**
   * Testing method.
   * @param s an array of string arguments.
   */
  public static void main(String[] s) {
    String subject, message_text;
    System.out.println(
        "--------------------------------------------------------");
    System.out.println("Starting ..." + (new Date()));
    System.out.println(
        "--------------------------------------------------------");
    System.out.println("from: test@test.com");
    System.out.println("to[0]: carlsen@apelon.com");
    System.out.println("to[1]: bcarlsen@apelon.com");
    System.out.println("cc[0]: carlsen@apelon.com");
    System.out.println("bcc[0]: carlsen@apelon.com");
    System.out.println("smtp_host: mail.apelon.com");
    System.out.println("subject: test");
    System.out.println("message_text: this is a test");
    MEMEMail mail = new MEMEMail();
    mail.from = "test@test.com";
    mail.to = new String[] {"carlsen@apelon.com", "bcarlsen@apelon.com"};
    mail.cc = new String[] {"carlsen@apelon.com"};
    mail.bcc = new String[] {"carlsen@apelon.com"};
    mail.smtp_host = "mail.apelon.com";
    subject = "test";
    message_text = "This message is a test.";

    System.out.println("\nTest 1:  Send successfully");
    System.out.println("----------------------------------");
    try {
      mail.send(subject, message_text);
      System.out.println("Message sent successfully ..." + (new Date()) + "\n");
    } catch (Exception e) {
      System.out.println("Failed to send message: " + e.getMessage());
      System.exit(1);
    }

    // Make it fail (no smtp_host);
    System.out.println("\nTest 2:  Fail: null smtp_host");
    System.out.println("----------------------------------");
    mail.smtp_host = null;
    try {
      mail.send(subject, message_text);
      System.out.println("Message sent successfully ..." + (new Date()) + "\n");
    } catch (Exception e) {
      System.out.println("Failed to send message: " + e.getMessage());
    }

    // Make it fail (null from);
    System.out.println("\nTest 3:  Fail: null from");
    System.out.println("----------------------------------");
    mail.smtp_host = "mail.apelon.com";
    mail.from = null;
    try {
      mail.send(subject, message_text);
      System.out.println("Message sent successfully ..." + (new Date()) + "\n");
    } catch (Exception e) {
      System.out.println("Failed to send message: " + e.getMessage());
    }

    // Make it fail (null to);
    System.out.println("\nTest 4:  Fail: null to");
    System.out.println("----------------------------------");
    mail.from = "test@test.com";
    mail.to = null;
    try {
      mail.send(subject, message_text);
      System.out.println("Message sent successfully ..." + (new Date()) + "\n");
    } catch (Exception e) {
      System.out.println("Failed to send message: " + e.getMessage());
    }

    // Make it fail (empty to);
    System.out.println("\nTest 5:  Fail: empty to");
    System.out.println("----------------------------------");
    mail.from = "test@test.com";
    mail.to = new String[] {};
    try {
      mail.send(subject, message_text);
      System.out.println("Message sent successfully ..." + (new Date()) + "\n");
    } catch (Exception e) {
      System.out.println("Failed to send message: " + e.getMessage());
    }

    // Make it fail (bad smtp host)
    System.out.println("\nTest 6:  Fail: illegal SMTP host");
    System.out.println("----------------------------------");
    mail.to = new String[] {"carlsen@apelon.com", "carlsen@apelon.com"};
    mail.smtp_host = "badmail.apelon.com";
    try {
      mail.send(subject, message_text);
      System.out.println("Message sent successfully ..." + (new Date()) + "\n");
    } catch (Exception e) {
      System.out.println("Failed to send message: " + e.getMessage());
    }

    // Make it fail (bad smtp host)
    System.out.println("\nTest 6:  Fail: illegal recipient");
    System.out.println("----------------------------------");
    mail.smtp_host = "mail.apelon.com";
    mail.to = new String[] {"badmail"};
    mail.cc = null;
    mail.bcc = null;
    try {
      mail.send(subject, message_text);
      System.out.println("Message sent successfully ..." + (new Date()) + "\n");
    } catch (Exception e) {
      System.out.println("Failed to send message: " + e.getMessage());
    }

    System.out.println(
        "\n--------------------------------------------------------");
    System.out.println("Finished ..." + (new Date()));
    System.out.println(
        "--------------------------------------------------------");

  }; // public static void main

}
