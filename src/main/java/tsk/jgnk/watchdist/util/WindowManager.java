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

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static tsk.jgnk.watchdist.AppWindow.*;

public class WindowManager {
	private static Stage mainStage;

	public static void showMainWindow(Stage stage) {
		try {
			if (stage == null) stage = new Stage();
			mainStage = stage;
			URL resource = App.class.getClassLoader().getResource("view/main.fxml");
			checkNotNull(resource);

			FXMLLoader fxmlLoader = new FXMLLoader(resource);
			fxmlLoader.setResources(Messages.getBundle());
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
			stage.setTitle(Messages.get("main.window.title"));
			stage.setScene(scene);
			stage.show();
			stage.setUserData(MAIN);
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
			for (Stage stage : StageHelper.getStages()) {
				if (stage.getUserData() == WATCH_POINTS) {
					stage.requestFocus();
					return;
				}
			}

			URL resource = App.class.getClassLoader().getResource("view/watch_points.fxml");
			checkNotNull(resource);

			FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initOwner(mainStage);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.getIcons().add(new Image(WindowManager.class.getClassLoader().getResourceAsStream("icon.png")));
			stage.setScene(scene);
			stage.setTitle(Messages.get("watchpoints.title"));
			stage.show();
			stage.setUserData(WATCH_POINTS);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void showAdministrationWindow(MainController mainController) {
		checkNotNull(mainController);
		try {
			for (Stage stage : StageHelper.getStages()) {
				if (stage.getUserData() == ADMINISTRATION) {
					stage.requestFocus();
					return;
				}
			}

			URL resource = App.class.getClassLoader().getResource("view/administration.fxml");
			checkNotNull(resource);

			FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
			fxmlLoader.setController(new AdministrationController(mainController));
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initOwner(mainStage);
			stage.initModality(Modality.WINDOW_MODAL);
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
			for (Stage stage : StageHelper.getStages()) {
				if (stage.getUserData() == WATCH_DISTRIBUTION) {
					stage.requestFocus();
					return;
				}
			}

			URL resource = App.class.getClassLoader().getResource("view/distribution.fxml");
			checkNotNull(resource);
			FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
			fxmlLoader.setController(new DistributionController(mainController));
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initOwner(mainStage);
			stage.initModality(Modality.WINDOW_MODAL);
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
			for (Stage stage : StageHelper.getStages()) {
				if (stage.getUserData() == WATCH_VALUES) {
					stage.requestFocus();
					return;
				}
			}

			URL resource = App.class.getClassLoader().getResource("view/watch_values.fxml");
			checkNotNull(resource);
			FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initOwner(mainStage);
			stage.initModality(Modality.WINDOW_MODAL);
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

	public static void showAddNewWatchPointWindow(WatchPointsController watchPointsController) {
		try {
			for (Stage stage : StageHelper.getStages()) {
				if (stage.getUserData() == ADD_NEW_WATCH_POINT) {
					stage.requestFocus();
					return;
				}
			}

			URL resource = App.class.getClassLoader().getResource("view/add_new_watch_point.fxml");
			checkNotNull(resource);

			FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
			AddNewWatchPointController addNewWatchPointController
					= new AddNewWatchPointController(watchPointsController);
			fxmlLoader.setController(addNewWatchPointController);
			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
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
			for (Stage stage : StageHelper.getStages()) {
				if (stage.getUserData() == ADD_NEW_SOLDIER) {
					stage.requestFocus();
					return;
				}
			}

			URL resource = App.class.getClassLoader().getResource("view/add_new_soldier.fxml");
			checkNotNull(resource);

			FXMLLoader fxmlLoader = new FXMLLoader(resource, Messages.getBundle());
			AddNewSoldierController controller = new AddNewSoldierController(mainController);
			fxmlLoader.setController(controller);

			Parent root = fxmlLoader.load();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.initOwner(mainStage);
			stage.initModality(Modality.WINDOW_MODAL);
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
}
