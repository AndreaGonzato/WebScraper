import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

public class Email {

    // things to set
    private static final String myAccountEmail = "xxx@gmail.com";
    private static final String myPassword = "xxxxxx";


    private final String subject;
    private final String corpus;
    private static int emailSent = 0;

    public Email(String subject, String text) {
        this.subject = subject;
        corpus = text;
    }


    public void sendMail(String recipient) {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, myPassword);
            }
        });

        Message message = prepareMessage(session, myAccountEmail, recipient);

        try {
            Transport.send(message);
            Main.printAndLog("OK " + new Date() + " Email successfully sent with subject: " + subject);
            emailSent++;
        } catch (MessagingException e) {
            Main.printAndLog("ER " + new Date() + " Can not send email with subject: " + subject + " <:> " + e.getCause() + e.getMessage());
            e.printStackTrace();
        }

    }

    private Message prepareMessage(Session session, String myAccountEmail, String recipient) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.CC, new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);
            message.setText(corpus);
            return message;
        } catch (MessagingException e) {
            Main.printAndLog("ER " + new Date() + " Can not send email with subject: " + subject + " <:> " + e.getCause() + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static int getEmailSent() {
        return emailSent;
    }


}
