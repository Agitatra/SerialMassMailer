package de.mk_p.serialmassmailer;

import javax.mail.Message;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

public class Recipient {
    Message.RecipientType type;
    String address;
    Map<String, String> properties;

    public Message.RecipientType getType () {
        return (type);
    }

    public String getAddress () {
        return (address);
    }

    public Map <String, String> getProperties () {
        return (properties);
    }

    public Recipient (String recipientString) {
        Matcher addrMatch = Util.RECTYPEPATTERN.matcher(recipientString);
        properties = new HashMap <> ();
        if (addrMatch.matches()) {
            String type =         addrMatch.group(1);
            String address =      addrMatch.group(2);
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
            if (propFileName != null) {
                String myPropFileName = (Util.hasFilenameSuffix (propFileName)) ? propFileName
                                                                                : propFileName + ".properties";
                try {
                    Properties props = new Properties ();
                    props.load (new FileReader (myPropFileName));
                    Set <String> keys = props.stringPropertyNames ();
                    for (String key: props.stringPropertyNames ())
                        properties.put (key, props.getProperty (key));
                }
                catch (IOException fnfe) {
                    System.out.println ("Error: \"" + fnfe.getMessage () + "\" reading property file \"" +
                                        myPropFileName + "\"");
                    // Error reading properties, no properties are processed
                }
            }
            else
                properties = new HashMap <> ();
            if (firstname != null)
                properties.put ("firstname", firstname);
            if (surname != null)
                properties.put ("surname", surname);
        }
        else
            this.type = Message.RecipientType.TO;
    }
}
