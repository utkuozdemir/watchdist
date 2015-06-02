package tsk.jgnk.watchdist.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
    private static Locale locale;
    private static ResourceBundle bundle;

    static {
        // set default language
        setLocale(Language.TR.getLocale());
    }

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(Locale locale) {
        Messages.locale = locale;
        bundle = ResourceBundle.getBundle("messages", locale);
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static String get(String key, Object... args) {
        return MessageFormat.format(bundle.getString(key), args);
    }
}