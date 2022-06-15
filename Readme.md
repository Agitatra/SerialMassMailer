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
- **--recipients-file=text-file-containing-recipientes**
- **--subject=subject**
- **--html**
- **--file=message-filename**
- **--from=sender@address**
- **--attachment=attachment-file**
- **--config=java.properties**
- **--receive**
- **--username=name**
- **--password=password**
- **--starttls=(0|1|2)**

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
- java.exe  -cp titus\kim\kim-util\src\main\java;mail-1.4.1.jar;java-getopt-1.0.13.jar;javax.servlet_3.0.0.v201112011016.jar;spring-context-4.3.5.RELEASE.jar;spring-web-4.3.5.RELEASE.jar;activation-1.1.1.jar de.gematik.titus.kim.util.EmailWorker --html --bcc-file=to.txt --file=xmas2020.html --attachment=xmas2020.jpg --from=dm@d-mark.biz --subject="Ein ganz pers�nliches Geschenk von Dagi & Mark." --config=mailconfig.properties --starttls=1
