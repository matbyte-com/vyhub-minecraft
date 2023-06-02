package net.vyhub.config;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


public class I18n {
    private ResourceBundle bundle;

    public I18n(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle("messages", locale);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        }
    }

    public String get(String key, Object... params) {
        try {
            return MessageFormat.format(bundle.getString(key), params);
        } catch (Exception e) {
            return key;
        }
    }

}
