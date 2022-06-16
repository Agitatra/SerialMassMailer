package de.mk_p.serialmassmailer;


import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;

import org.apache.commons.text.StringSubstitutor;

/**
 * @author mark
 *
 */
public class EmailWorker {

    private static final int INIVECSIZ = 12;
    private static final String RND = "0C2dSmlALfRu98+iE7Iy4U3USyvLF54Aa7b7NXq9QZE=";
    private static final String MAILHOST = "smtp.gmail.com";
    private static final String USERNAME = null;
    private static final String PASSWORD = null;
    private static final String HOST = "host";
    private static final String USER = "user";
    private static final String PORT = "port";
    private static final String AUTH = "auth";
    private static final String TRUST = "trust";
    private static final String TAGMAILHOST = "mailhost";
    private static final String TAGUSERNAME = "username";
    private static final String TAGPASSWORD = "password";
    private static final String SMTP = "smtp";
    private static final String SMTPS = "smtps";
    private static final String POP3S = "pop3s";
    private static final String MAIL = "mail";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String DEBUG = "debug";
    private static final String SSL = "ssl";
    private static final String STARTTLS = "starttls";
    private static final String COMSUNMAILPOP3 = "com.sun.mail.pop3";
    private static final String COMSUNMAILSMTP = "com.sun.mail.smtp";
    private static final String PROTOCOL = "protocol";
    private static final String PROTOCOLS = "protocols";
    private static final String TRANSPORT = "transport";
    private static final String SOCKETFACTORY = "socketfactory";
    private static final String SSLSOCKETFACTORY = ".SSLSocketFactory";
    private static final String CLASS = ".class";
    private static final String JAVAXNET = "javax.net.";
    private static final String ENABLE = "enable";
    private static final String PORT995 = "995";
    private static final String PGPMAGIG = Base64.getEncoder ().encodeToString (new byte[] {(byte) 0x85, (byte) 0x03, (byte) 0x0e, (byte) 0x03});

    public static StringBuilder variableInterpolation (StringBuilder content, Map <String, String> values) {
        if ((values == null) || values.size () <= 0)
            return (content);
        else {
            StringSubstitutor interpolator = new StringSubstitutor (values);
            return (new StringBuilder (interpolator.replace (content)));
        }
    }

    private static Message addRecipient (Session session, String recipientStr, StringBuilder content, String key,
                                         boolean htmlMail, Multipart multipart) throws MessagingException {
        Message msg = new MimeMessage (session);
        Recipient recipient = new Recipient (recipientStr);
        // Create the HTML Part
        BodyPart bodyPart = new MimeBodyPart ();
        content = variableInterpolation (content, recipient.getProperties ());
        if (key != null) {
            try {
                content = AES256encrypt (key, content);
            }
            catch (BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException |
                   InvalidAlgorithmParameterException | IllegalBlockSizeException |
                   InvalidKeyException e) {
                System.out.println ("Error: \"" + e.getMessage () + "\" encrypting message, sending it unencrypted");
            }
        }
        bodyPart.setContent (content.toString (), (htmlMail) ? "text/html" : "text/text");
        multipart.addBodyPart (bodyPart);
        msg.setContent (multipart);
        msg.addRecipient (recipient.getType (), new InternetAddress (recipient.getAddress ()));
        return (msg);
    }

    private Message createdMultipart (Session session, String recipient, String originatorAddr, String subject,
                                      StringBuilder content, boolean htmlMail, DataSource[] attachments,
                                      boolean debug, String key)
            throws MessagingException {
        Message msg;

        Multipart multipart = new MimeMultipart ();
        for (int i = 0; (attachments != null) && (i < attachments.length); i++) {
            BodyPart attachmentBodyPart = new MimeBodyPart ();
            attachmentBodyPart.setDataHandler (new DataHandler (attachments[i]));
            System.out.println ("using: \"" + attachments[i].getName () + "\" as attachment");
            attachmentBodyPart.setFileName (attachments[i].getName ());
            multipart.addBodyPart (attachmentBodyPart);
        }
        msg = addRecipient (session, recipient, content, key, htmlMail, multipart);
        session.setDebug (debug);
        msg.setFrom (new InternetAddress (originatorAddr));
        msg.setSubject (subject);
        msg.saveChanges ();
        return (msg);
    }

    private Message[] createdMultipart (Session session, String[] recipientAddrs, String originatorAddr, String subject,
                                        StringBuilder content, boolean htmlMail, DataSource[] attachments,
                                        boolean debug, String key)
            throws MessagingException {
        List <Message> messages = new ArrayList <> ();

        for (String recipient : recipientAddrs) {
            messages.add (createdMultipart (session, recipient, originatorAddr, subject, content, htmlMail, attachments,
                    debug, key));
        }
        return (messages.toArray (new Message[] {}));
    }

    public void sendSmtpAuthAfterStartTls (String[] recipientAddrs, String originatorAddr, String subject,
                                           StringBuilder content, boolean htmlMail, DataSource[] attachments,
                                           Properties config, boolean unsecure, boolean debug, String key) throws MessagingException {
        Session session;
        Properties props = System.getProperties ();
        Authenticator auth;
        final Properties myConfig;

        if (config == null) {
            myConfig = new Properties ();
            myConfig.setProperty (TAGMAILHOST, MAILHOST);
            myConfig.setProperty (TAGUSERNAME, USERNAME);
            myConfig.setProperty (TAGPASSWORD, PASSWORD);
        }
        else
            myConfig = config;
        auth = new Authenticator () {
            @Override
            public PasswordAuthentication getPasswordAuthentication () {
                return (new PasswordAuthentication (myConfig.getProperty (TAGUSERNAME), myConfig.getProperty (TAGPASSWORD)));
            }
        };
        props.setProperty (MAIL + "." + SMTP + "." + HOST, myConfig.getProperty (TAGMAILHOST));
        props.setProperty (MAIL + "." + TRANSPORT + "." + PROTOCOL, SMTPS);
        props.setProperty (MAIL + "." + SMTP + "." + AUTH, TRUE);
        props.setProperty (MAIL + "." + SMTP + "." + PORT, "25");
        if (!unsecure) {
            props.setProperty (MAIL + "." + SMTP + "." + STARTTLS + "." + ENABLE, TRUE);
            props.setProperty (MAIL + "." + SMTP + "." + SSL + "." + TRUST, myConfig.getProperty (TAGMAILHOST));
            props.setProperty (MAIL + "." + SMTP + "." + STARTTLS + ".required", TRUE);
        }
        if (debug) {
            props.setProperty (MAIL + "." + DEBUG, TRUE);
            props.setProperty (COMSUNMAILSMTP, DEBUG);
        }
        session = Session.getDefaultInstance (props, auth);
        for (Message msg : createdMultipart (session, recipientAddrs, originatorAddr, subject, content, htmlMail,
                attachments, debug, key))
            try {
                Transport.send (msg);
            }
            catch (MessagingException me) {
                System.out.print ("Error: \"" + me.getMessage () + "\" sending e-mail to: ");
                try {
                    for (Address recipient : msg.getAllRecipients ())
                        System.out.print ("\"" + recipient.toString () + "\"");
                }
                catch (MessagingException e) {
                    // Error during messaging an error
                }
                System.out.println ();
            }
    }


    public void sendSmtps (String [] recipientAddrs, String originatorAddr, String subject, StringBuilder content,
                           boolean htmlMail, DataSource [] attachments, Properties config, boolean debug, String key)
            throws MessagingException {

        Session session;
        Properties props = System.getProperties ();
        Transport transport;
        final Properties myConfig;

        if (config == null) {
            myConfig = new Properties ();
            myConfig.setProperty (TAGMAILHOST, MAILHOST);
            myConfig.setProperty (TAGUSERNAME, USERNAME);
            myConfig.setProperty (TAGPASSWORD, PASSWORD);
        }
        else
            myConfig = config;

        props.setProperty (MAIL + "." + SMTP + "." + HOST, myConfig.getProperty (TAGMAILHOST));
        props.setProperty (MAIL + "." + TRANSPORT + "." + PROTOCOL, SMTPS);
        props.setProperty (MAIL + "." + SMTP + "." + AUTH, TRUE);
        props.setProperty (MAIL + "." + SMTPS + "." + AUTH, TRUE);
        props.setProperty (MAIL + "." + SMTPS + "." + PORT, "465");
        props.setProperty (MAIL + "." + SMTPS + "." + SSL + "." + TRUST, myConfig.getProperty (TAGMAILHOST));
        props.setProperty (MAIL + "." + SMTPS + "." + SSL + "." + ENABLE, TRUE);
        props.setProperty (MAIL + "." + SMTPS + "." + SSL + "." + PROTOCOLS, "TLSv1.1 TLSv1.2");
        if (debug) {
            props.setProperty (MAIL + "." + DEBUG, TRUE);
            props.setProperty (COMSUNMAILSMTP, DEBUG);
        }
        session = Session.getInstance (props, null);
        session.setDebug (true);
        transport = session.getTransport (SMTPS);
        transport.connect (myConfig.getProperty (TAGMAILHOST), myConfig.getProperty (TAGUSERNAME), myConfig.getProperty (TAGPASSWORD));
        for (Message msg : createdMultipart (session, recipientAddrs, originatorAddr, subject, content, htmlMail,
                           attachments, debug, key))
            try {
                transport.sendMessage (msg, msg.getAllRecipients ());
            }
            catch (MessagingException me) {
                System.out.print ("Error: \"" + me.getMessage () + "sending e-mail to: ");
                try {
                    for (Address recipient : msg.getAllRecipients ())
                        System.out.print ("\"" + recipient.toString () + "\"");
                }
                catch (MessagingException e) {
                    // Error during messaging an error
                }
                System.out.println ();
            }
    }

        public static StringBuilder AES256encrypt (String key, StringBuilder content)
                                       throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException,
                                              NoSuchPaddingException, InvalidAlgorithmParameterException,
                                              BadPaddingException {
        byte []          initVect =     new byte [INIVECSIZ];
        byte []          cipherContent;
        byte []          cipherContentMessage;
        ByteBuffer       byteBuffer;
        SecretKey        secretKey;
        SecureRandom     secureRandom = new SecureRandom ();
        GCMParameterSpec parameterSpec;
        StringBuilder    hashKey =      new StringBuilder (key);
        StringBuilder    retVal =       new StringBuilder ();
        Cipher           cipher =       Cipher.getInstance ("AES/GCM/NoPadding");

        hashKey.append (RND);
        hashKey.setLength (32);
        secretKey = new SecretKeySpec (hashKey.toString ().getBytes (StandardCharsets.UTF_8), "AES");
        secureRandom.nextBytes (initVect);

        parameterSpec = new GCMParameterSpec (128, initVect);
        cipher.init (Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        cipherContent = cipher.doFinal (content.toString ().getBytes (StandardCharsets.UTF_8));
        byteBuffer = ByteBuffer.allocate (initVect.length + cipherContent.length);
        byteBuffer.put (initVect);
        byteBuffer.put (cipherContent, initVect.length, cipherContent.length);
        cipherContentMessage = byteBuffer.array ();
        retVal.append (PGPMAGIG);
        retVal.append ((Base64.getEncoder ().encodeToString (cipherContentMessage)));
        return (retVal);
    }


    public StringBuilder AES256decrypt (String key, String cipherContentStr) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException, BadPaddingException {
        SecretKey               secretKey;
        StringBuilder           hashKey =       new StringBuilder (key);
        StringBuilder           retVal =        new StringBuilder ();
        Cipher                  cipher =        Cipher.getInstance ("AES/GCM/NoPadding");
        byte []                 cipherContent = Base64.getDecoder().decode (cipherContentStr.substring (PGPMAGIG.length ()));
        AlgorithmParameterSpec  gcmIv =         new GCMParameterSpec (128, cipherContent, 0, INIVECSIZ);

        hashKey.append (RND);
        hashKey.setLength (32);
        secretKey = new SecretKeySpec (hashKey.toString ().getBytes (StandardCharsets.UTF_8), "AES");
        cipher.init (Cipher.DECRYPT_MODE, secretKey, gcmIv);
        retVal.append (new String (cipher.doFinal (cipherContent, INIVECSIZ, cipherContent.length - INIVECSIZ), StandardCharsets.UTF_8));
        return (retVal);
    }

    public Message [] receivePop3s (String user, String password, Properties config) throws MessagingException {
        Authenticator auth;
        Folder        inbox;
        Message []    messages;
        Session       session;
        Properties    myConfig;
        Properties    properties = System.getProperties ();
        Store         store;

	final String  innerUser =     user;
	final String  innerPassword = password;

        if (config == null) {
            myConfig = new Properties ();
            myConfig.setProperty (TAGMAILHOST, MAILHOST);
            myConfig.setProperty (TAGUSERNAME, USERNAME);
            myConfig.setProperty (TAGPASSWORD, PASSWORD);
        }
        else
            myConfig = config;
        properties.put (MAIL + "." + POP3S + "." + SOCKETFACTORY + CLASS,                JAVAXNET + SSL + SSLSOCKETFACTORY);
        properties.put (MAIL + "." + POP3S + "." + SSL + "." + SOCKETFACTORY + CLASS,    JAVAXNET + SSL + SSLSOCKETFACTORY);
        properties.setProperty (MAIL + "." + DEBUG, TRUE);
        properties.setProperty (MAIL + "." + "store." + PROTOCOL,                        POP3S);
        properties.setProperty (MAIL + "." + POP3S + "." + SOCKETFACTORY + ".fallback",  FALSE);
        properties.setProperty (MAIL + "." + POP3S + "." + SOCKETFACTORY + "." + PORT,   PORT995);
        properties.setProperty (MAIL + "." + POP3S + "." + PORT,                         PORT995);
        properties.setProperty (MAIL + "." + POP3S + "." + HOST,                         myConfig.getProperty (TAGMAILHOST));
        properties.setProperty (MAIL + "." + POP3S + "." + USER,                         innerUser);
        properties.setProperty (MAIL + "." + POP3S + "." + SSL + "." + TRUST,            myConfig.getProperty (TAGMAILHOST));
        properties.setProperty (MAIL + "." + POP3S + "." + SSL + "." + PROTOCOLS,        "TLSv1.3 TLSv1.2 TLSv1.1 TLSv1 SSLv3 SSLv2Hello");
        properties.setProperty (MAIL + "." + POP3S + "." + SSL + ".checkserveridentity", FALSE);
        properties.setProperty (COMSUNMAILPOP3,                                          DEBUG);
        auth = new Authenticator () {
            @Override
            protected PasswordAuthentication getPasswordAuthentication () {
                return (new PasswordAuthentication (innerUser, innerPassword));
            }
        };
        session = Session.getDefaultInstance (properties, auth);
        session.setDebug (true);
        store =   session.getStore (POP3S);
        store.connect (myConfig.getProperty (TAGMAILHOST), innerUser, innerPassword);
        inbox = store.getFolder ("INBOX");
        inbox.open (Folder.READ_ONLY);

        // get the list of inbox messages
        messages = inbox.getMessages ();

        inbox.close (true);
        store.close ();
        return (messages);

    }

    public static void main (String [] args) throws Exception {
        boolean              debug =                 false;
        boolean              htmlMail =              false;
        boolean              sendOrReceive =         true;
        int                  c;
        int                  cc;
        int                  contentC;
        int                  tlsLevel =              2;
        String []            to =                    null;
        String               from =                  null;
        String               subject =               null;
        String               key =                   null;
        String               user =                  null;
        String               password =              null;
        DataSource           attachment =            null;
        Message []           messages;
        StringBuilder        content =               new StringBuilder ();
        StringBuffer         shortCommand =          new StringBuffer ();
        BufferedReader       contentReader =         null;
        Properties           config =                null;
        EmailWorker          sendMail =              new EmailWorker ();
        LongOpt []           longopts =              new LongOpt [] {
                                                         new LongOpt ("debug", LongOpt.NO_ARGUMENT,
                                                                      shortCommand, 'd'),
                                                         new LongOpt ("to", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 't'),
                                                         new LongOpt ("recipients-file", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 'R'),
                                                         new LongOpt ("html-mail", LongOpt.NO_ARGUMENT,
                                                                      shortCommand, 'h'),
                                                         new LongOpt ("attachment-file", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 'a'),
                                                         new LongOpt ("from", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 'F'),
                                                         new LongOpt ("file", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 'f'),
                                                         new LongOpt ("key", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 'k'),
                                                         new LongOpt ("password", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 'p'),
                                                         new LongOpt ("receive", LongOpt.NO_ARGUMENT,
                                                                      shortCommand, 'r'),
                                                         new LongOpt ("subject", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 's'),
                                                         new LongOpt ("starttls", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 'S'),
                                                         new LongOpt ("config-file", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 'c'),
                                                         new LongOpt ("username", LongOpt.REQUIRED_ARGUMENT,
                                                                      shortCommand, 'u')
        };
        Getopt               getOpt =                new Getopt(EmailWorker.class.getName (),
                                                                args, "a:u:p:t:c:R:F:f:k:s:S:rhd", longopts);

        getOpt.setOpterr (false); // We'll do our own error handling

        for (c = cc = -1; (cc >= 0) || (c = getOpt.getopt ()) != -1;) {
            if (cc >= 0) {
                c = cc;
                cc = -1;
            }
            switch (c) {
                case 0:
                    /* arg = */ getOpt.getOptarg ();
                    cc = (char) Integer.parseInt (shortCommand.toString ());
                    break;
                case 1:
                    System.out.println ("I see you have return in order set and that " +
                            "a non-option argv element was just found " +
                            "with the value '" + getOpt.getOptarg() + "'");
                    break;
                case 2:
                    getOpt.getOptarg ();
                    break;
                case 't':
                    to = new String [] {getOpt.getOptarg ()};
                    break;
                case 'd':
                    debug = true;
                    break;
                case 'F':
                    from = getOpt.getOptarg ();
                    break;
                case 'R':
                    try {
                        to = (Files.readAllLines (FileSystems.getDefault ().getPath (getOpt.getOptarg ())).toArray (new String [] {}));
                    }
                    catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace ();
                        throw new RuntimeException (fnfe);
                    }
                    break;
                case 'c':
                    try {
                        config = new Properties ();
                        config.load (new FileReader (getOpt.getOptarg ()));
                    }
                    catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace ();
                        throw new RuntimeException (fnfe);
                    }
                    break;
                case 'f':
                    try {
                        contentReader = new BufferedReader (new FileReader (getOpt.getOptarg ()));
                    }
                    catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace ();
                        throw new RuntimeException (fnfe);
                    }
                    break;
                case 'S':
                    try {
                        tlsLevel = Integer.parseInt (getOpt.getOptarg ());
                    }
                    catch (NumberFormatException nfe) {
                        // This can perfectly happen and is not an error.
                    }
                    break;
                case 'k':
                    key = getOpt.getOptarg ();
                    break;
                case 'p':
                    password = getOpt.getOptarg ();
                    sendOrReceive = false;
                    break;
                case 'u':
                    user = getOpt.getOptarg ();
                    sendOrReceive = false;
                    break;
                case 's':
                    subject = getOpt.getOptarg ();
                    break;
                case 'h':
                    htmlMail = true;
                    break;
                case 'a':
                    attachment = new FileDataSource (getOpt.getOptarg ());
                    System.out.println ("using: \"" + attachment.getName () + "\" as attachment: \"" + getOpt.getOptarg () + "\"");
                    break;
                case ':':
                    throw new IllegalArgumentException ("Doh! You need an argument for option '" + (char) getOpt.getOptopt() + "'");
                case '?':
		    if (getOpt.getOptopt() <= 0)
			    break;
		    throw new IllegalArgumentException ("The option " + getOpt.getOptopt() + ", '" + (char) getOpt.getOptopt() + "'/'" + getOpt.getOptarg () + "' is not valid");
                default:
                    System.out.println ("getopt() returned " + c);
                    break;
            }
        }
        if (sendOrReceive) {
            if (contentReader == null)
                contentReader = new BufferedReader (new InputStreamReader (System.in));
            while ((contentC = contentReader.read ()) >= 0)
                content.append ((char) contentC);
            if (from == null)
                from = "mark@meadhbh.brokenerror.de";
            if (subject == null)
                subject = "You have Mail!";
            if (to == null)
                throw new RuntimeException ("Please supply a recipient");
            for (String rep: to) {
                System.out.println ("Rescipient: \"" + rep + "\"");
                if (tlsLevel < 2)
                    sendMail.sendSmtpAuthAfterStartTls (new String [] {rep}, from, subject, content, htmlMail,
                                                        (attachment != null) ? new DataSource [] {attachment} : null,
                                                        config, tlsLevel == 0, debug, key);
                else
                    sendMail.sendSmtps (new String [] {rep}, from, subject, content, htmlMail,
                                        (attachment != null) ? new DataSource [] {attachment} : null,
                                        config, debug, key);
            }
        }
        else {
            messages = sendMail.receivePop3s ((user != null) ? user : USERNAME, (password != null) ? password : PASSWORD, config);
            if (messages.length == 0)
                System.out.println ("No messages found.");

            for (int i = 0; i < messages.length; i++) {
                System.out.println ("Message: " + (i + 1));
                System.out.println ("From: " +      messages [i].getFrom () [0]);
                System.out.println ("Subject: " +   messages [i].getSubject ());
                System.out.println ("Sent Date: " + messages [i].getSentDate ());
                if (messages [i].getContent () instanceof String) {
                    String messageText = (String) messages [i].getContent ();
                    if ((key instanceof String) && messageText.startsWith (PGPMAGIG))
                        System.out.println ("Messagetext: " + sendMail.AES256decrypt (key, messageText));
                    else
                        System.out.println ("Messagetext: " + messageText);
                }
                else
                    System.out.println ("Multipart message");
                System.out.println ();
            }
        }

    }
}

