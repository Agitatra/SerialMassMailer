# SerialMassMailer: Mini Java E-Mail Client

This is a very simple Java-E-Mail Client

## Parameters
The application takes the following parameters:
- **-a attachment-file** (see --attachment).
- **-c java.properties** (see --config).
- **-d** (see --debug).
- **-D** (see --dry-run).
- **-h** (see --html).
- **-f message-filename** (see --file).
- **-F sender@address** (see --from).
- **-r** (see --receive),
- **-p password** (see --password).
- **-R text-file-containing-recipients** (see --recipients-file).
- **-s subject** (see --subject).
- **-S** (see --strict-error-handling).
- **-T (0|1|2)** (see --starttls).
- **-t recipient@address** (see --to).
- **-u name** (see --username).
- **--attachment=attachment-file**
A file containing an attachment.
- **--config=java.properties**
Java Properties file containing the mail-server configuration (defaults to application internal constants).
Currently, three properties are supported: "MAILHOST", the name or ip-address of the mail-host, "USER" & "PASSWORD" the username and the password to identify to the mail-server.  The latter two parameters are only relevant for sending messages.
- **--debug**
Print debug messages, including the complete SMTP-protocol (defaults to: *false*).
- **--dry-run**
Do not actually send e-mails, instead print what would have been sent.
- **--html**
The Message is in the HTML-format (defaults to: *false*).
- **--file=message-filename**
A file containing the message (defaults to *stdin*).
The file may contain variables (see section Recipient-File).
- **--from=sender@address**
The sender (defaults to: *noreply@default.default*).
- **--password=password**
Password of user for whom e-mails should be received (implies --receive, requires --username).
- **--receive**
Receiving mails from server instead of sending it (defaults to *false* unless --username & --password are provided, requires --username & --password)
- **--recipients-file=text-file-containing-recipients**
A text-file containing one recipient per line (see section Recipient-File)
- **--starttls=(0|1|2)**
E-mail is sent using TLS over port 465 ("2"), over port 25, using STARTTLS ("1") or without TLS ("0") (Defaults to TLS/2).
- **--strict-error-handling**
Do not continue after errors that only affect one recipient (defaults to: *false*). Normally when a recipient related error occurs only this recipient is discarded. 
- **--subject=subject**
The subject (defaults to: "You hava mail").
- **--to=recipient@address**
The recipient (required unless --recipients-file is provided)
- **--username=name**
User for whom e-mails should be received (implies --receive, requires --password).

## Recipient-File
The recipient file is a text file containing the e-mail address of a recipient per line.
However, the information provided can also be enriched with additional information, seperated by a tab-character:
- type of address ("To", "CC" and "BCC", defaults to: "To"),
- recipient e-mail address
- first name,
- surname,
- name of a Java property file
The Recipient-File may contain one or more dirctives:
- #include = property-filename
that allows the inclusion of other property files.

The type is used to determine how to send the message.
First- and surname are stored in the variables "${firstname}" and "${surname}" respectively.
### Variables/properties
The template file for the message, as provided by the "--file" parameter, may contain variable references in the fashion of bash-variables.
The variables are replaced before a message is sent, by their respective properties, their possible defaults - provided in the reference, or ignored if no value is provided.
Two properties (${firstname} and ${surname}) get their values from the recipient line, the rest has to be stored in a Java properties files whose name is provided as the last parameter of the recipients line.
For further reference example templates: "mail.txt", "to.txt", "friend.properties", "woman.properties", "madame.properties", "man.properties" and "sir.properties" are part of the project.

CAVEAT: The local part of the recipients may unfortunately not contain international (i.e. UTF-8) characters.

## Required dependencies
Dependencies are satisfied by the Maven POM, however if manual compilation is required the following libraries have to be provided.
The application requires the following Java-archives:
- activation-1.1.1.jar,
- mail-1.4.1.jar
- java-getopt-1.0.13.jar
- javax.servlet_3.0.0.v201112011016.jar
- mail-1.6.2.jar
- spring-context-4.3.5.RELEASE.jar
- spring-web-4.3.5.RELEASE.jar

## Compilation
Normally the project should be compiled by Maven, however, if this is not an option, the following command will do:
- javac.exe -Xlint:deprecation -cp .;mail-1.4.1.jar;java-getopt-1.0.13.jar;javax.servlet_3.0.0.v201112011016.jar;spring-context-4.3.5.RELEASE.jar;spring-web-4.3.5.RELEASE.jar;activation-1.1.1.jar  de/mk_p/serialmassmailer/EmailWorker.java

## Example
The following example sends a pure text e-mail to the recipients in to.txt using a TLS connection:
- java -jar serialmassmailer-1.0.0.jar  --recipients-file=to.txt --file=mail.txt --attachment=xmas2020.jpg --from=dm@d-mark.biz --subject="A very special present by Pille & Palle." --config=mailconfig.properties --starttls=1

### Licenses
The project is licensed under both the Apache 2 and the MIT license.
**HOWEVER**, any usage of the source code or executable for distributing unsolicited messages is strictly prohibited.
