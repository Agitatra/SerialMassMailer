package de.mk_p.serialmassmailer;

import java.util.regex.Pattern;

public class Constants {
    public static final Pattern RECTYPEPATTERN =   Pattern.compile ("^(bcc|cc|to)?[: \t]*([^: \t]*)[: \t]*(([^\t]*)\t([^\t]*))?[: \t]*(.*)?$", Pattern.CASE_INSENSITIVE);
}
