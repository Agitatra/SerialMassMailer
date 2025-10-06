package de.mk_p.serialmassmailer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static final Pattern INCLUDE_PROPERTY =
                                Pattern.compile ("^ *[#!]include *[:=\t][ \t]*(.*) *$", Pattern.CASE_INSENSITIVE);
    public static final Pattern COMMENT =
                                Pattern.compile ("^ *([#!].*|\\s*)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern RECTYPEPATTERN =
                                Pattern.compile ("^ *(bcc|cc|to)?[: \t]*([^: \t]+)[: \t]*(([^\t]*)\t([^\t]*))?[: \t]*(.*)?$", Pattern.CASE_INSENSITIVE);
    public static final Pattern FILENAME_WITH_SUFFIX =
                                Pattern.compile ("^.*\\.([^.]+)$", Pattern.CASE_INSENSITIVE);

    public static String getFilenameSuffix (String filename) {
        Matcher match = FILENAME_WITH_SUFFIX.matcher (filename);
        if (match.matches () && (match.group (1).length () > 0))
            return (match.group (1));
        else
            return ("");
    }
    public static boolean hasFilenameSuffix (String filename) {
        String suffix = getFilenameSuffix (filename);
        return ((suffix != null) && (suffix.length () > 0));
    }
}
