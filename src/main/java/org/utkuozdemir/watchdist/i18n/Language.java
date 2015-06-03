package org.utkuozdemir.watchdist.i18n;

import com.google.common.base.Preconditions;

import java.util.Locale;
import java.util.Objects;

public enum Language {
    EN("English", Locale.ENGLISH), TR("Türkçe", Locale.forLanguageTag("tr"));

    private String languageName;
    private Locale locale;

    Language(String languageName, Locale locale) {
        this.languageName = languageName;
        this.locale = locale;
    }

    public static Language forLocale(Locale locale) {
        Preconditions.checkNotNull(locale);
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
