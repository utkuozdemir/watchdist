package org.utkuozdemir.watchdist.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.app.Constants;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.type.PasswordType;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

@SuppressWarnings("unused")
public class AppPasswordController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(AppPasswordController.class);

    @FXML
    private Label error;
    @FXML
    private PasswordField password;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void login() {
        String appPassword = DbManager.getProperty(PasswordType.APP_PASSWORD.getKey());
        String masterPassword = DbManager.getProperty(PasswordType.MASTER_PASSWORD.getKey());

        String enteredPassword = password.getText();

        if (BCrypt.checkpw(enteredPassword, appPassword) || BCrypt.checkpw(enteredPassword, masterPassword)) {
            ((Stage) error.getScene().getWindow()).close();

            String oneWatchDuration = DbManager.getProperty(Constants.KEY_WATCH_DURATION_IN_HOURS);
            String firstWatchStartHour = DbManager.getProperty(Constants.KEY_FIRST_WATCH_START_HOUR);
            String firstWatchStartMinute = DbManager.getProperty(Constants.KEY_FIRST_WATCH_START_MINUTE);
            String watchesBetweenTwoWatches = DbManager.getProperty(Constants.KEY_WATCHES_BETWEEN_TWO_WATCHES);

            if (oneWatchDuration == null || firstWatchStartHour == null || firstWatchStartMinute == null ||
                    watchesBetweenTwoWatches == null) {
                WindowManager.showSetInitialValuesWindow();
            } else {
                String templatePath = DbManager.getProperty(Constants.KEY_EXCEL_TEMPLATE_PATH_KEY);
                if (templatePath == null) {
                    WindowManager.showSetExcelTemplatePathWindow(Messages.get("excel.template.path.not.set"));
                } else {
                    try {
                        Path path = Paths.get(templatePath);
                        if (!Files.exists(path) || !Files.isWritable(path)) {
                            WindowManager.showSetExcelTemplatePathWindow(
                                    Messages.get("excel.template.path.problem", templatePath)
                            );
                        } else {
                            WindowManager.showMainWindow();
                        }
                    } catch (InvalidPathException | SecurityException e) {
                        logger.debug("Invalid path set for excel template: " + templatePath);
                        WindowManager.showSetExcelTemplatePathWindow(
                                Messages.get("excel.template.path.problem", templatePath)
                        );
                    }
                }
            }
        } else {
            error.setVisible(true);
        }
    }
}
