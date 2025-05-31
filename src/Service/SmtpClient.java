/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import Entities.LogOccurrenceModule;
import Enum.LogLevel;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 *
 * @author 320167484
 */
public class SmtpClient {

    private final StringWriter sw = new StringWriter();
    private final PrintWriter pw = new PrintWriter(sw);
    private String host;
    private final ArrayList<LogOccurrenceModule> LogArray = new ArrayList<>();

    public ArrayList<LogOccurrenceModule> PerformServerConnection(String host, String port, String aut, String prot, String stls, String rem, String pwd, String des, String tmsg, String pmsg) {

        this.host = host;
        Properties props = new Properties();
        configureSmtpProperties(props, aut, stls, host, port, prot);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(rem, pwd);
            }
        });

        try {
            Message mensagem = createEmailMessage(session, rem, des, tmsg, pmsg);
            sendEmail(mensagem);
            addToArray("Email sent successfully!", LogLevel.INFO);
        } catch (MessagingException e) {
            e.printStackTrace(pw);
            addToArray("Error sending the email: " + e.getMessage(), LogLevel.ERROR);
            this.addToArray(sw.toString(), LogLevel.DEBUG);
        }

        return getLogArray();
    }

    private void configureSmtpProperties(Properties props, String aut, String stls, String host, String port, String prot) {
        props.put("mail.smtp.auth", aut);
        props.put("mail.smtp.starttls.enable", stls);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.protocols", prot);
    }

    private Message createEmailMessage(Session session, String rem, String des, String tmsg, String pmsg) throws MessagingException {
        Message mensagem = new MimeMessage(session);
        mensagem.setFrom(new InternetAddress(rem));
        mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(des));
        mensagem.setSubject(tmsg);
        mensagem.setText(pmsg);
        return mensagem;
    }

    private void sendEmail(Message mensagem) throws MessagingException {
        Transport.send(mensagem);
    }

    private void addToArray(String input, LogLevel level) {

        LogOccurrenceModule log = new LogOccurrenceModule(input, level, host);
        this.LogArray.add(log);
    }

    public ArrayList<LogOccurrenceModule> getLogArray() {
        return LogArray;
    }

}
