package de.mk_p.serialmassmailer;

import javax.mail.Message;
import java.io.*;
import java.nio.charset.MalformedInputException;
import java.util.Properties;
import java.util.regex.Matcher;

public class Recipient {
    Message.RecipientType type;
    String address;
    Properties properties;

    public Message.RecipientType getType () {
        return (type);
    }

    public String getAddress () {
        return (address);
    }

    public Properties getProperties () {
        return (properties);
    }

    private void readProperties (String propFileName, Properties properties) throws RecipientDataException {
        String currentPropFileName;
        if ((propFileName != null) && (propFileName.length() > 0)) {
            currentPropFileName =
                    (Util.hasFilenameSuffix (propFileName)) ? propFileName : propFileName + ".properties";
            try {
                Properties props = new Properties ();
                StringBuilder propsString = new StringBuilder();
                try (BufferedReader br = new BufferedReader (new FileReader (currentPropFileName))) {
                    for (String line; (line = br.readLine ()) != null; ) {
                        Matcher includeMatch = Util.INCLUDE_PROPERTY.matcher (line);
                        if (includeMatch.matches ())
                            readProperties (currentPropFileName = includeMatch.group (1), properties);
                        else
                            propsString.append (line).append ('\n');
                    }
                }
                if (propsString.length () > 0) {
                    props.load (new StringReader (propsString.toString ()));
                    for (String key: props.stringPropertyNames ()) {
                        properties.put (key, props.getProperty (key));
                    }
                }
            }
            catch (IOException fnfe) {
                if (fnfe instanceof MalformedInputException)
                    System.err.println ("Property-file: \"" + currentPropFileName + "\" contains non UTF-8 characters.");
                else if (fnfe instanceof FileNotFoundException)
                    System.err.println ("Property-file: \"" + currentPropFileName +
                            "\" not found. Message wasn't sent to: \"" + address + "\"");
                else
                    System.err.println ("Error: \"" + fnfe.getMessage () + "\" reading property file: \"" +
                            currentPropFileName + "\". Message wasn't sent to: \"" + address + "\"");
                // Error reading properties, no properties are processed
                throw new RecipientDataException (getAddress ());
            }
        }
    }

    public Recipient (String recipientString) throws RecipientDataException {
        Matcher addrMatch = Util.RECTYPEPATTERN.matcher (recipientString);
        properties = new Properties ();
        if (addrMatch.matches()) {
            String type =         addrMatch.group(1);
            String address =      addrMatch.group(2);
            String name =         addrMatch.group(3);
            String firstname =    addrMatch.group(4);
            String surname =      addrMatch.group(5);
            String propFileName = addrMatch.group(6);
            if ((type == null) || (type.length() <= 0) || type.equalsIgnoreCase("to"))
                this.type = Message.RecipientType.TO;
            else if (type.equalsIgnoreCase("cc"))
                this.type = Message.RecipientType.CC;
            else if (type.equalsIgnoreCase("bcc"))
                this.type = Message.RecipientType.BCC;
            else
                this.type = Message.RecipientType.TO;
            this.address = address;
            readProperties (propFileName, properties);
            if (name != null)
                properties.put ("name", name);
            if (firstname != null)
                properties.put ("firstname", firstname);
            if (surname != null)
                properties.put ("surname", surname);
        }
        else
            this.type = Message.RecipientType.TO;
    }
}
