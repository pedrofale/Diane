package com.Diane;

import com.sun.mail.pop3.POP3Store;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.security.Security;
import java.util.Date;
import java.util.Properties;

import com.sun.mail.smtp.SMTPTransport;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by pedro on 06-10-2016.
 *
 * Class to fetch the bus schedule for my bus stop from the CARRIS website.
 * Sends an e-mail to sms@carris.pt with subject C 07413. CARRIS then sends an e-mail
 * from sms@carris.pt with the time of the request and the bus times.
 */
public class Bus {
    // String that holds the raw bus information
    private String[][] busInfo;
    public String[][] getBusInfo() { return busInfo; }
    public String getBusInfo(int i, int j) { return busInfo[i][j]; }

    // String that holds the time of the last request
    private String lastRequestTime = "";
    public String getlastRequestTime() { return lastRequestTime; }

    // True if there are no more at the current time
    private boolean noBuses = false;
    public boolean getNoBuses() { return noBuses; }

    // Number of lines of the timetable
    private int numLines = 0;
    public int getNumLines() { return numLines; }


    private String getUsername(String authFilePath) throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(authFilePath));
        String userName = prop.getProperty("username");
        System.out.println(userName);
        return userName;
    }

    private char[] getPassword(String authFilePath) throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(authFilePath));
        // Password should always be stored in the char array.
        char[] password = null;
        if (prop.getProperty("password") != null) {
            password = prop.getProperty("password").toCharArray();
        }
        System.out.println(password.toString());
        return password;
    }

    public void sendMail(String authFilePath, String recipientEmail, String title, String message) throws IOException {
        String userName = getUsername(authFilePath);
        char[] password = getPassword(authFilePath);
        sendMail(userName, password, recipientEmail, title, message);
    }

    private void sendMail(final String userName, final char[] password, String recipientEmail,
                          String title, String text) {

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, new String(password));
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(userName + "@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail));
            message.setSubject(title);
            message.setText(text);

            Transport.send(message);
            System.out.println("Sent e-mail");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void readEmail(String authFilePath) throws IOException {
        String host = "pop.gmail.com";
        String mailStoreType = "pop3s";
        String userName = getUsername(authFilePath);
        char[] password = getPassword(authFilePath);
        fetch(host, mailStoreType, userName, password);
    }


    private void fetch(String pop3Host, String storeType, String user,
               char[] password) {
        try {
            // create properties field
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "pop3");
            properties.put("mail.pop3.host", pop3Host);
            properties.put("mail.pop3.port", "995");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);
            // emailSession.setDebug(true);

            // create the POP3 store object and connect with the pop server
            POP3Store store = (POP3Store) emailSession.getStore("pop3s");

            store.connect(pop3Host, user, new String(password));

            // create the folder object and open it
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            System.out.println("messages.length---" + messages.length);

            Message message = messages[messages.length - 1];
            System.out.println("---------------------------------");
            getBusTimetable(message);

            // close the store and folder objects
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /*
    * Prints the content of the message
    */
    private void getBusTimetable(Part p) throws Exception {
        Object o = p.getContent();
        String html = (String) o;
        Document doc = Jsoup.parse(html);

        // Header
        Elements header3 = doc.select("h3");
        System.out.println(header3.get(0).text());
        lastRequestTime = header3.get(0).text().replace("Pedido realizado em ", "");

        // Schedule table
        Elements ths = doc.select("th");
        numLines = (ths.size() - 1) / 4;
        busInfo = new String[numLines][4];
        System.out.println(ths.text());
        if (!ths.get(4).text().contains("7")) {
            noBuses = true;
        } else {
            int cnt = 0;
            for (int i = 0; i < numLines; i++) {
                for (int j = 0; j < 4; j++) {
                    busInfo[i][j] = ths.get(cnt).text();
                    cnt++;
                    System.out.println(busInfo[i][j]);
                }
            }
        }
    }
}
