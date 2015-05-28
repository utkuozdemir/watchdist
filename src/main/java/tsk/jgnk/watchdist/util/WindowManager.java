package tsk.jgnk.watchdist.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.thehecklers.monologfx.MonologFX;
import org.thehecklers.monologfx.MonologFXBuilder;
import org.thehecklers.monologfx.MonologFXButton;
import org.thehecklers.monologfx.MonologFXButtonBuilder;
import tsk.jgnk.watchdist.App;
import tsk.jgnk.watchdist.controller.*;
import tsk.jgnk.watchdist.i18n.Language;
import tsk.jgnk.watchdist.i18n.Messages;

import java.io.IOException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

public class WindowManager {
    private static Stage mainStage;

    public static void showMainWindow(Stage stage) {
        try {
            if (stage == null) stage = new Stage();
            mainStage = stage;
            URL resource = App.class.getClassLoader().getResource("view/Main.fxml");
            checkNotNull(resource);
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            fxmlLoader.setResources(Messages.getBundle());
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);

            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setTitle(Messages.get("main.window.title"));
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void switchLanguage(Language language) {
        checkNotNull(language);
        mainStage.close();
        Messages.setLocale(language.getLocale());
        WindowManager.showMainWindow(mainStage);
    }

    public static void showWatchPointsWindow() {
        try {
            URL resource = App.class.getClassLoader().getResource("view/WatchPoints.fxml");
            checkNotNull(resource);

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle(Messages.get("watchpoints.title"));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showWatchDistributionWindow(MainController mainController) {
        try {
            URL resource = App.class.getClassLoader().getResource("view/Distribution.fxml");
            checkNotNull(resource);
            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            fxmlLoader.setController(new DistributionController(mainController));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle(Messages.get("distribution.title"));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAddNewWatchPointWindow(WatchPointsController watchPointsController) {
        try {
            URL resource = App.class.getClassLoader().getResource("view/AddNewWatchPoint.fxml");
            checkNotNull(resource);

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            AddNewWatchPointController addNewWatchPointController
                    = new AddNewWatchPointController(watchPointsController);
            fxmlLoader.setController(addNewWatchPointController);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setResizable(false);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(Messages.get("addnewwatchpoint.title"));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAddNewSoldierWindow(MainController mainController) {
        try {
            URL resource = App.class.getClassLoader().getResource("view/AddNewSoldier.fxml");
            checkNotNull(resource);

            FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
            AddNewSoldierController controller = new AddNewSoldierController(mainController);
            fxmlLoader.setController(controller);

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setTitle(Messages.get("addnewsoldier.title"));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showInitializationInfo(String dbPath, String templatePath) {
        if (dbPath == null && templatePath == null) return;

        StringBuilder message = new StringBuilder();
        if (dbPath != null) {
            message.append(Messages.get("new.db.initialized.message", Constants.DB_NAME, dbPath));
        }

        message.append(System.lineSeparator());
        message.append(System.lineSeparator());

        if (templatePath != null) {
            message.append(Messages.get("new.template.initialized.message", Constants.TEMPLATE_NAME, templatePath));
        }

        MonologFXButton continueButton = MonologFXButtonBuilder.create()
                .label(Messages.get("app.continue"))
                .type(MonologFXButton.Type.YES)
                .defaultButton(true)
                .build();
        MonologFX mono = MonologFXBuilder.create()
                .modal(true)
                .titleText(Messages.get("new.initializations"))
                .message(message.toString().trim())
                .type(MonologFX.Type.INFO)
                .button(continueButton)
                .buttonAlignment(MonologFX.ButtonAlignment.RIGHT)
                .build();
        mono.show();
    }
}
