package org.utkuozdemir.watchdist.controller;

import org.utkuozdemir.watchdist.app.AppContext;
import org.utkuozdemir.watchdist.app.Constants;
import org.utkuozdemir.watchdist.i18n.Language;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import static org.utkuozdemir.watchdist.type.PasswordType.APP_PASSWORD;
import static org.utkuozdemir.watchdist.type.PasswordType.DB_RESET_PASSWORD;

public class LanguageSelectionController implements Initializable {

    @FXML
    private Button saveAndContinue;

    @FXML
    private ComboBox<Language> language;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        language.setItems(FXCollections.observableArrayList(Language.values()));

        String locale = DbManager.getProperty(Constants.KEY_LOCALE);
        if (locale != null) {
            Language l = Language.valueOf(locale);
            Messages.setLocale(l.getLocale());
        }

        language.setValue(Language.forLocale(Messages.getLocale()));

        language.valueProperty().addListener((observable, oldValue, newValue) -> {
            Messages.setLocale(newValue.getLocale());
            saveAndContinue.setText(Messages.get("save.and.continue"));
        });
    }

    public void saveAndContinue() {
        DbManager.setProperty(Constants.KEY_LOCALE, language.getValue().name());
        Messages.setLocale(language.getValue().getLocale());

        WindowManager.showInitializationInfo(AppContext.get().isNewDbInitialized() ?
                AppContext.get().getInitializedDbDirectory() : null);
        initializePasswordPrompt();
        ((Stage) language.getScene().getWindow()).close();
    }

    private void initializePasswordPrompt() {
        String appPassword = DbManager.getProperty(APP_PASSWORD.getKey());
        String dbResetPassword = DbManager.getProperty(DB_RESET_PASSWORD.getKey());
        if (appPassword == null || appPassword.isEmpty() || dbResetPassword == null || dbResetPassword.isEmpty()) {
            WindowManager.showSetPasswordsWindow();
        } else {
            WindowManager.showAppPasswordWindow();
        }
    }

}
