# EmailWorker: Mini Java E-Mail Client

This is a very simple Java-E-Mail Client

## Parameters
The application takes the following parametes:
- **-t recipient@address** (see --to).
- **-R text-file-containing-recipientes** (see --recipients-file).
- **-s subject** (see --subject).
- **-h** (see --html).
- **-f message-filename** (see --file).
- **-F sender@address** (see --from).
- **-a attachment-file** (see --attachment).
- **-c java.properties** (see --config).
- **-r** (see --receive),
- **-u name** (see --username).
- **-p password** (see --password).
- **-S (0|1|2)** (see --starttls).
- **--to=recipient@address**
The recipient (required unless --recipients-file is provided)
- **--recipients-file=text-file-containing-recipientes**A text-file containing one recipient per line.  Each line may be preceded by a key to define if it is a normal ("To:"), a carbon copy ("CC:") or an undisclosed ("BCC:") address (required unless --to is provided)
- **--subject=subject**The subject (defaults to: "You hava mail").
- **--html**The Message is in the HTML-format (defaults to: *false*).
- **--file=message-filename**A file containing the message (defaults to *stdin*).
- **--from=sender@address**The sender (defaults to: *mk@mk-p.de*).
- **--attachment=attachment-file**A file containing an attachment.
- **--config=java.properties**Java Properties file confaining the mail-server configuration (defaults to application internal constants).  Currently three properties are supported: "MAILHOST", the name or ip-address of the mail-host, "USER" & "PASSWORD" the username and the password to identify to the mail-server.  The latter two parameters are only relevant for sending messages.
- **--receive**Receiving mails from server instead of sending it (defaults to *false* unless --username & --password are provided, requires --username & --password)
- **--username=name**User for whom e-mails should be received (implies --receive, requires --password).
- **--password=password**Password of user for whom e-mails should be received (implies --receive, requires --username).
- **--starttls=(0|1|2)**E-mail is sent using TLS over port 465 ("2"), over port 25, using STARTTLS ("1") or without TLS ("0") (Defaults to TLS/2).

## Required libraries
The application requires the following Java-archives:
- activation-1.1.1.jar,
- mail-1.4.1.jar
- java-getopt-1.0.13.jar
- javax.servlet_3.0.0.v201112011016.jar
- mail-1.4.1.jar
- spring-context-4.3.5.RELEASE.jar
- spring-web-4.3.5.RELEASE.jar

## Compilation
- javac.exe -Xlint:deprecation -cp .;mail-1.4.1.jar;java-getopt-1.0.13.jar;javax.servlet_3.0.0.v201112011016.jar;spring-context-4.3.5.RELEASE.jar;spring-web-4.3.5.RELEASE.jar;activation-1.1.1.jar  titus\kim\kim-util\src\main\java\de\gematik\titus\kim\util\EmailWorker.java

## Example
- java.exe  -cp titus\kim\kim-util\src\main\java;mail-1.4.1.jar;java-getopt-1.0.13.jar;javax.servlet_3.0.0.v201112011016.jar;spring-context-4.3.5.RELEASE.jar;spring-web-4.3.5.RELEASE.jar;activation-1.1.1.jar de.gematik.titus.kim.util.EmailWorker --html --bcc-file=to.txt --file=xmas2020.html --attachment=xmas2020.jpg --from=dm@d-mark.biz --subject="Ein ganz persönliches Geschenk von Dagi & Mark." --config=mailconfig.properties --starttls=1

