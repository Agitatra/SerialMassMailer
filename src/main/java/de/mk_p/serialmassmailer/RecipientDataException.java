package de.mk_p.serialmassmailer;

public class RecipientDataException extends Exception {
    private final String recipient;

    public RecipientDataException (String recipient) {
        this.recipient = recipient;
    }

    public String getRecipient () {
        return this.recipient;
    }
}
