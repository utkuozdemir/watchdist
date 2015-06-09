package org.utkuozdemir.watchdist.i18n;

import java.util.Locale;
import java.util.Objects;

public enum Language {
    EN("English", Locale.ENGLISH), TR("Türkçe", Locale.forLanguageTag("tr"));

    private final String languageName;
    private final Locale locale;

    Language(String languageName, Locale locale) {
        this.languageName = languageName;
        this.locale = locale;
    }

    public static Language forLocale(Locale locale) {
        if (locale == null) throw new NullPointerException();
        for (Language language : values()) {
            if (Objects.equals(language.getLocale(), locale)) return language;
        }
        throw new IllegalArgumentException("Language not found!");
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return languageName;
    }
}
