package org.utkuozdemir.watchdist.controller;

import com.sun.javafx.stage.StageHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.type.PasswordType;
import org.utkuozdemir.watchdist.type.WindowType;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.FileManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ResetDbPasswordController {
	@FXML
	private Label error;
	@FXML
	private PasswordField password;

	public void login() {
		String appPassword = DbManager.getProperty(PasswordType.APP_PASSWORD.getKey());
		String masterPassword = DbManager.getProperty(PasswordType.MASTER_PASSWORD.getKey());

		String enteredPassword = password.getText();

		if (BCrypt.checkpw(enteredPassword, appPassword) || BCrypt.checkpw(enteredPassword, masterPassword)) {
			((Stage) error.getScene().getWindow()).close();
			WindowManager.showMainWindow();
		} else {
			error.setVisible(true);
		}
	}

	public void resetDatabase() {
		String appPassword = DbManager.getProperty(PasswordType.APP_PASSWORD.getKey());
		String dbResetPassword = DbManager.getProperty(PasswordType.DB_RESET_PASSWORD.getKey());
		String masterPassword = DbManager.getProperty(PasswordType.MASTER_PASSWORD.getKey());

		String enteredPassword = password.getText();

		if (BCrypt.checkpw(enteredPassword, dbResetPassword) || BCrypt.checkpw(enteredPassword, masterPassword)) {
			boolean approved = showResetDatabaseConfirmationDialog();
			if (approved) {
				DbManager.close();
				FileManager.resetDatabase();
				DbManager.initialize(FileManager.getDatabasePath());
				DbManager.setProperty(PasswordType.APP_PASSWORD.getKey(), appPassword);
				DbManager.setProperty(PasswordType.DB_RESET_PASSWORD.getKey(), dbResetPassword);
				WindowManager.getMainController().refreshTableData();
				WindowManager.showInfoAlert(Messages.get("success"), Messages.get("database.reset.success.message"));
			}
			((Stage) error.getScene().getWindow()).close();

			WindowManager.showLanguageSelectionWindow();
			List<Stage> stages = StageHelper.getStages()
					.stream().filter(s -> s.getUserData() != WindowType.LANGUAGE_SELECTION)
					.collect(Collectors.toList());
			stages.forEach(Stage::close);
		} else {
			error.setVisible(true);
		}
	}

	private boolean showResetDatabaseConfirmationDialog() {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(Messages.get("reset.database"));
		alert.setHeaderText(Messages.get("confirmation"));
		alert.setContentText(Messages.get("reset.database.confirmation"));

		ButtonType resetButtonType = new ButtonType(Messages.get("reset.database"));
		ButtonType cancelButtonType = new ButtonType(Messages.get("cancel"));

		alert.getButtonTypes().setAll(resetButtonType, cancelButtonType);
		alert.getDialogPane().lookupButton(resetButtonType).setStyle("-fx-base: #F78181;");

		Optional<ButtonType> buttonType = alert.showAndWait();
		return buttonType.isPresent() && buttonType.get() == resetButtonType;
	}
}
