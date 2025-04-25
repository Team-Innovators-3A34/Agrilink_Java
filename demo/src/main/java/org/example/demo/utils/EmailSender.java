package org.example.demo.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailSender {

    public static void send(String recipient , String msg) throws MessagingException {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        String myEmail = "mehdiboughdiri75@gmail.com";
        String password = "jhud rmwm kthe uwey";

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmail, password);
            }
        });

        Message message = prepareMessage(session, myEmail, recipient, msg);

        if (message != null) {
            System.out.println("Message sent");
            Transport.send(message);
        } else {
            System.out.println("Message not sent");
        }
    }

    private static Message prepareMessage(Session session, String myAccountEmail, String recipientEmail, String msg) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(recipientEmail)});
            message.setSubject("Messages");
            message.setText(msg);
            return message;
        } catch (Exception e) {
            Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, "Error: ", e);
        }
        return null;
    }
}
