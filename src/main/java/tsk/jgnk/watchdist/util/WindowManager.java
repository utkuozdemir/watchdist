package tsk.jgnk.watchdist.util;

import com.sun.javafx.stage.StageHelper;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tsk.jgnk.watchdist.App;
import tsk.jgnk.watchdist.controller.*;
import tsk.jgnk.watchdist.i18n.Language;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.type.PasswordType;
import tsk.jgnk.watchdist.type.WindowType;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static tsk.jgnk.watchdist.type.WindowType.*;

public class WindowManager {
	private static MainController mainController;
	private static WatchPointsController watchPointsController;

	public static void showSetExcelTemplatePathWindow(String infoMessage) {
		try {
			if (alreadyOpened(SET_EXCEL_TEMPLATE_PATH)) return;

			URL resource = App.class.getClassLoader().getResource("view/set_excel_template_path.fxml");
			checkNotNull(resource);

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
			checkNotNull(resource);

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
			checkNotNull(resource);

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
			checkNotNull(resource);

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
			checkNotNull(resource);

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

			Stage stage = new Stage();
			URL resource = App.class.getClassLoader().getResource("view/main.fxml");
			checkNotNull(resource);

			FXMLLoader fxmlLoader = new FXMLLoader(resource);
			fxmlLoader.setResources(Messages.getBundle());
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			URL cssResource = WindowManager.class.getClassLoader().getResource("css/main.css");
			if (cssResource != null) scene.getStylesheets().add(cssResource.toExternalForm());
			stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
			stage.setTitle(Messages.get("main.window.title"));
			stage.setScene(scene);
			stage.show();
			stage.setUserData(MAIN);
			mainController = fxmlLoader.getController();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void switchLanguage(Stage mainWindow, Language language) {
		checkNotNull(language);
		mainWindow.close();
		Messages.setLocale(language.getLocale());
		WindowManager.showMainWindow();
	}

	public static void showWatchPointsWindow() {
		try {
			if (alreadyOpened(WATCH_POINTS)) return;

			URL resource = App.class.getClassLoader().getResource("view/watch_points.fxml");
			checkNotNull(resource);

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

	public static void showAdministrationWindow(MainController mainController) {
		checkNotNull(mainController);
		try {
			if (alreadyOpened(ADMINISTRATION)) return;

			URL resource = App.class.getClassLoader().getResource("view/administration.fxml");
			checkNotNull(resource);

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

	public static void showWatchDistributionWindow(MainController mainController) {
		try {
			if (alreadyOpened(WATCH_DISTRIBUTION)) return;

			URL resource = App.class.getClassLoader().getResource("view/distribution.fxml");
			checkNotNull(resource);
			FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
			fxmlLoader.setController(new DistributionController(mainController));
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
			checkNotNull(resource);
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
			checkNotNull(resource);

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

	public static void showAddNewSoldierWindow(MainController mainController) {
		try {
			if (alreadyOpened(ADD_NEW_SOLDIER)) return;

			URL resource = App.class.getClassLoader().getResource("view/add_new_soldier.fxml");
			checkNotNull(resource);

			FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
			AddNewSoldierController controller = new AddNewSoldierController(mainController);
			fxmlLoader.setController(controller);

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

		WindowManager.showInfoAlert(Messages.get("new.initializations"), message.toString().trim());
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

		ButtonType continueButtonType = new ButtonType(Messages.get("continue"));
		ButtonType exitButtonType = new ButtonType(Messages.get("exit"));

		alert.getButtonTypes().setAll(continueButtonType, exitButtonType);
		((Button) alert.getDialogPane().lookupButton(continueButtonType)).setDefaultButton(true);
		alert.getDialogPane().lookupButton(exitButtonType).setStyle("-fx-base: #F78181;");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == exitButtonType) {
			Platform.exit();
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
