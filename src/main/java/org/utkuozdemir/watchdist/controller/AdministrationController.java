package org.utkuozdemir.watchdist.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.app.Constants;
import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.type.PasswordType;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.FileManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AdministrationController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(AdministrationController.class);
    @FXML
    private Spinner<Integer> watchesBetweenTwoWatches;
    @FXML
    private Label durationLabel;

    @FXML
    private Button done;

    public void editExcelTemplate() {
        try {
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
                        Desktop.getDesktop().open(path.toFile());
                    }
                } catch (InvalidPathException | SecurityException e) {
                    logger.debug("Invalid path set for excel template: " + templatePath);
                    WindowManager.showSetExcelTemplatePathWindow(
                            Messages.get("excel.template.path.problem", templatePath)
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportDatabase() {
        Path databasePath = FileManager.getDatabasePath();

        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter
                = new FileChooser.ExtensionFilter(Messages.get("database.file") + " (*.db)", "*.db");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle(Messages.get("backup.export.database"));

        String finalName = Constants.DB_NAME.split("\\.")[0] + "-"
                + LocalDate.now() + "." + Constants.DB_NAME.split("\\.")[1];

        fileChooser.setInitialFileName(finalName);
        File file = fileChooser.showSaveDialog(done.getScene().getWindow());
        if (file != null) {
            try {
                if (!file.getName().endsWith(".db")) file = new File(file.getPath() + ".db");
                Files.copy(databasePath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                WindowManager.showInfoAlert(Messages.get("success"),
                        Messages.get("export.database.success", file.getAbsolutePath()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void resetExcelTemplate() {
        WindowManager.showSetExcelTemplatePathWindow(Messages.get("excel.template.path.not.set"));
    }

    public void resetDatabase() {
        WindowManager.showResetDbPasswordWindow();

    }

    public void closeWindow() {
        ((Stage) done.getScene().getWindow()).close();
    }

    public void changeAppPassword() {
        WindowManager.showChangePasswordWindow(PasswordType.APP_PASSWORD);
    }

    public void changeDbResetPassword() {
        WindowManager.showChangePasswordWindow(PasswordType.DB_RESET_PASSWORD);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        durationLabel.setText(Messages.get("duration.between.two.watches", Settings.getOneWatchDurationInHours()));
        watchesBetweenTwoWatches.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                0, 8, Settings.getMinWatchesBetweenTwoWatches()
        ));
        watchesBetweenTwoWatches.valueProperty()
                .addListener((observable, oldValue, newValue) -> {
                    DbManager.setProperty(Constants.KEY_WATCHES_BETWEEN_TWO_WATCHES, String.valueOf(newValue));
                    Settings.invalidateCache();
                });
    }
}
