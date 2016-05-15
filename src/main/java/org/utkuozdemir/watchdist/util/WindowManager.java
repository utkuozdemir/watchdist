package org.utkuozdemir.watchdist.util;

import com.sun.javafx.stage.StageHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.app.App;
import org.utkuozdemir.watchdist.app.AppContext;
import org.utkuozdemir.watchdist.app.Constants;
import org.utkuozdemir.watchdist.controller.ChangePasswordController;
import org.utkuozdemir.watchdist.controller.DistributionController;
import org.utkuozdemir.watchdist.controller.MainController;
import org.utkuozdemir.watchdist.controller.NotesController;
import org.utkuozdemir.watchdist.controller.SetExcelTemplatePathController;
import org.utkuozdemir.watchdist.controller.WatchPointsController;
import org.utkuozdemir.watchdist.i18n.Language;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.type.PasswordType;
import org.utkuozdemir.watchdist.type.WindowType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static org.utkuozdemir.watchdist.type.WindowType.ADD_NEW_SOLDIER;
import static org.utkuozdemir.watchdist.type.WindowType.ADD_NEW_WATCH_POINT;
import static org.utkuozdemir.watchdist.type.WindowType.ADMINISTRATION;
import static org.utkuozdemir.watchdist.type.WindowType.APP_PASSWORD;
import static org.utkuozdemir.watchdist.type.WindowType.CHANGE_APP_PASSWORD;
import static org.utkuozdemir.watchdist.type.WindowType.LANGUAGE_SELECTION;
import static org.utkuozdemir.watchdist.type.WindowType.MAIN;
import static org.utkuozdemir.watchdist.type.WindowType.NOTES;
import static org.utkuozdemir.watchdist.type.WindowType.RESET_DB_PASSWORD;
import static org.utkuozdemir.watchdist.type.WindowType.SET_EXCEL_TEMPLATE_PATH;
import static org.utkuozdemir.watchdist.type.WindowType.SET_INITIAL_VALUES;
import static org.utkuozdemir.watchdist.type.WindowType.SET_PASSWORDS;
import static org.utkuozdemir.watchdist.type.WindowType.WATCH_DISTRIBUTION;
import static org.utkuozdemir.watchdist.type.WindowType.WATCH_POINTS;
import static org.utkuozdemir.watchdist.type.WindowType.WATCH_VALUES;

public class WindowManager {
    private static final Logger logger = LoggerFactory.getLogger(WindowManager.class);

    private static MainController mainController;
    private static WatchPointsController watchPointsController;

    public static void showLanguageSelectionWindow() {
        try {
            if (alreadyOpened(LANGUAGE_SELECTION)) return;

            URL resource = App.class.getClassLoader().getResource("view/language_selection.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle(Messages.get("language.selection"));
            stage.setUserData(LANGUAGE_SELECTION);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void showSetInitialValuesWindow() {
        try {
            if (alreadyOpened(SET_INITIAL_VALUES)) return;

            URL resource = App.class.getClassLoader().getResource("view/set_initial_values.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle(Messages.get("set.initial.values"));
            stage.setUserData(SET_INITIAL_VALUES);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void showSetExcelTemplatePathWindow(String infoMessage) {
        try {
            if (alreadyOpened(SET_EXCEL_TEMPLATE_PATH)) return;

            URL resource = App.class.getClassLoader().getResource("view/set_excel_template_path.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();

            SetExcelTemplatePathController controller = fxmlLoader.getController();
            controller.setInfoMessage(infoMessage);

            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle(Messages.get("set.excel.template.path"));
            stage.setUserData(SET_EXCEL_TEMPLATE_PATH);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void showChangePasswordWindow(PasswordType passwordType) {
        WindowType windowType;
        switch (passwordType) {
            case APP_PASSWORD:
                windowType = CHANGE_APP_PASSWORD;
                break;
            case DB_RESET_PASSWORD:
                windowType = RESET_DB_PASSWORD;
                break;
            default:
                throw new IllegalArgumentException("Invalid password type to change: " + passwordType);
        }
        try {
            if (alreadyOpened(windowType)) return;

            URL resource = App.class.getClassLoader().getResource("view/change_password.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();

            ChangePasswordController controller = fxmlLoader.getController();
            controller.initialize(passwordType);

            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle(Messages.get("set.passwords"));
            stage.show();
            stage.setUserData(windowType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static boolean alreadyOpened(WindowType windowType) {
        for (Stage stage : StageHelper.getStages()) {
            if (stage.getUserData() == windowType) {
                stage.requestFocus();
                return true;
            }
        }
        return false;
    }

    public static void showSetPasswordsWindow() {
        try {
            if (alreadyOpened(SET_PASSWORDS)) return;

            URL resource = App.class.getClassLoader().getResource("view/set_passwords.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle(Messages.get("set.passwords"));
            stage.setUserData(SET_PASSWORDS);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAppPasswordWindow() {
        try {
            if (alreadyOpened(APP_PASSWORD)) return;

            URL resource = App.class.getClassLoader().getResource("view/app_password.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle(Messages.get("app.password"));
            stage.setUserData(APP_PASSWORD);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showResetDbPasswordWindow() {
        try {
            if (alreadyOpened(RESET_DB_PASSWORD)) return;

            URL resource = App.class.getClassLoader().getResource("view/reset_db_password.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle(Messages.get("app.password"));
            stage.show();
            stage.setUserData(RESET_DB_PASSWORD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showMainWindow() {
        try {
            if (alreadyOpened(MAIN)) return;

            Stage stage = AppContext.get().getMainStage();
            URL resource = App.class.getClassLoader().getResource("view/main.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            fxmlLoader.setResources(Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setTitle(Messages.get("main.window.title"));
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> Platform.exit());
            stage.show();
            stage.setUserData(MAIN);
            mainController = fxmlLoader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void switchLanguage(Stage mainWindow, Language language) {
        if (language == null) throw new NullPointerException("Language cannot be null!");
        mainWindow.close();
        Messages.setLocale(language.getLocale());
        DbManager.setProperty(Constants.KEY_LOCALE, language.name());
        WindowManager.showMainWindow();
    }

    public static void showWatchPointsWindow() {
        try {
            if (alreadyOpened(WATCH_POINTS)) return;

            URL resource = App.class.getClassLoader().getResource("view/watch_points.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setTitle(Messages.get("watchpoints.title"));
            stage.show();
            stage.setUserData(WATCH_POINTS);

            watchPointsController = fxmlLoader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showNotesWindow(DistributionController distributionController, LocalDate date) {
        if (date == null) throw new NullPointerException("Date should not be null!");
        try {
            if (alreadyOpened(NOTES)) return;

            URL resource = App.class.getClassLoader().getResource("view/notes.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();

            NotesController controller = fxmlLoader.getController();
            controller.initData(distributionController, date);

            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.setOnCloseRequest(event -> controller.closeAttempt());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setTitle(Messages.get("notes.of.day", date.format(DateTimeFormatter.ofPattern("dd EEEE yyyy"))));
            stage.show();
            stage.setUserData(NOTES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAdministrationWindow() {
        try {
            if (alreadyOpened(ADMINISTRATION)) return;

            URL resource = App.class.getClassLoader().getResource("view/administration.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setTitle(Messages.get("watchpoints.title"));
            stage.show();
            stage.setUserData(ADMINISTRATION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showWatchDistributionWindow() {
        try {
            if (alreadyOpened(WATCH_DISTRIBUTION)) return;

            URL resource = App.class.getClassLoader().getResource("view/distribution.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");
            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle(Messages.get("distribution.title"));
            stage.show();
            stage.setUserData(WATCH_DISTRIBUTION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showWatchValuesWindow() {
        try {
            if (alreadyOpened(WATCH_VALUES)) return;

            URL resource = App.class.getClassLoader().getResource("view/watch_values.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");
            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setScene(scene);
            stage.setTitle(Messages.get("edit.watch.values"));
            stage.show();
            stage.setUserData(WATCH_VALUES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAddNewWatchPointWindow() {
        try {
            if (alreadyOpened(ADD_NEW_WATCH_POINT)) return;

            URL resource = App.class.getClassLoader().getResource("view/add_new_watch_point.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();

            Stage owner = StageHelper.getStages().stream()
                    .filter(o -> o.getUserData() == WATCH_POINTS).findFirst().get();
            stage.initOwner(owner);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.setTitle(Messages.get("addnewwatchpoint.title"));
            stage.show();
            stage.setUserData(ADD_NEW_WATCH_POINT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAddNewSoldierWindow() {
        try {
            if (alreadyOpened(ADD_NEW_SOLDIER)) return;

            URL resource = App.class.getClassLoader().getResource("view/add_new_soldier.fxml");
            if (resource == null) throw new NullPointerException("Resources is null!");

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
            if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.setTitle(Messages.get("addnewsoldier.title"));
            stage.show();
            stage.setUserData(ADD_NEW_SOLDIER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showInitializationInfo(String dbPath) {
        if (dbPath == null) return;

        WindowManager.showInfoAlert(Messages.get("new.initializations"),
                Messages.get("new.db.initialized.message", Constants.DB_NAME, dbPath));
    }

    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(Messages.get("information"));
        alert.setContentText(message);

        ButtonType continueButtonType = new ButtonType(Messages.get("continue"));
        alert.getButtonTypes().setAll(continueButtonType);
        ((Button) alert.getDialogPane().lookupButton(continueButtonType)).setDefaultButton(true);
        alert.showAndWait();
    }

    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(Messages.get("error"));
        alert.setContentText(message);

        ButtonType exportLogButtonType = new ButtonType(Messages.get("export.log"));
        ButtonType continueButtonType = new ButtonType(Messages.get("continue"));
        ButtonType exitButtonType = new ButtonType(Messages.get("exit"));

        alert.getButtonTypes().setAll(exportLogButtonType, continueButtonType, exitButtonType);
        ((Button) alert.getDialogPane().lookupButton(continueButtonType)).setDefaultButton(true);
        alert.getDialogPane().lookupButton(exitButtonType).setStyle("-fx-base: #F78181;");

        Button exportLogButton = (Button) alert.getDialogPane().lookupButton(exportLogButtonType);
        exportLogButton.setTooltip(new Tooltip(Messages.get("export.log")));
        exportLogButton.setDisable(!Files.exists(FileManager.getLogFilePath()));

        alert.setOnCloseRequest(event -> {
            if (((Alert) event.getTarget()).resultProperty().getValue() == exportLogButtonType) {
                FileChooser fileChooser = new FileChooser();

                FileChooser.ExtensionFilter extFilter
                        = new FileChooser.ExtensionFilter(Messages.get("log.file") + " (*.log)", "*.log");
                fileChooser.getExtensionFilters().add(extFilter);
                fileChooser.setTitle(Messages.get("export.log"));

                fileChooser.setInitialFileName(Constants.LOG_FILE_NAME);
                File file = fileChooser.showSaveDialog(exportLogButton.getScene().getWindow());
                if (file != null) {
                    if (!file.getName().endsWith(".log")) file = new File(file.getPath() + ".log");
                    try {
                        Files.copy(FileManager.getLogFilePath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                event.consume();
            }
        });

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == exitButtonType) {
                Platform.exit();
            }
        }
    }

    public static void showWarningInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(Messages.get("warning"));
        alert.setContentText(message);

        ButtonType okButtonType = new ButtonType(Messages.get("ok"));

        alert.getButtonTypes().setAll(okButtonType);
        ((Button) alert.getDialogPane().lookupButton(okButtonType)).setDefaultButton(true);

        alert.showAndWait();
    }

    public static boolean showConfirmationAlert(String title, String message,
                                                String confirmButtonText, String cancelButtonText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(Messages.get("confirmation"));
        alert.setContentText(message);

        ButtonType confirmButtonType = new ButtonType(confirmButtonText);
        ButtonType cancelButtonType = new ButtonType(cancelButtonText);

        alert.getButtonTypes().setAll(confirmButtonType, cancelButtonType);
        ((Button) alert.getDialogPane().lookupButton(confirmButtonType)).setDefaultButton(true);

        Optional<ButtonType> result = alert.showAndWait();
        return (result.isPresent() && result.get() == confirmButtonType);
    }

    public static boolean showWarningConfirmationAlert(String title, String message,
                                                       String dangerousConfirmButtonText, String cancelButtonText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(Messages.get("warning"));
        alert.setContentText(message);

        ButtonType dangerousConfirmButtonType = new ButtonType(dangerousConfirmButtonText);
        ButtonType cancelButtonType = new ButtonType(cancelButtonText);

        alert.getButtonTypes().setAll(dangerousConfirmButtonType, cancelButtonType);
        alert.getDialogPane().lookupButton(dangerousConfirmButtonType).setStyle("-fx-base: #F78181;");
        ((Button) alert.getDialogPane().lookupButton(dangerousConfirmButtonType)).setDefaultButton(true);

        Optional<ButtonType> result = alert.showAndWait();
        return (result.isPresent() && result.get() == dangerousConfirmButtonType);
    }

    public static MainController getMainController() {
        return mainController;
    }

    public static WatchPointsController getWatchPointsController() {
        return watchPointsController;
    }
}
