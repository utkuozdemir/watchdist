package tsk.jgnk.watchdist.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.FileManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.util.Optional;

import static tsk.jgnk.watchdist.type.PasswordType.*;

@SuppressWarnings("unused")
public class ResetDbPasswordController {
	@FXML
	private Label error;
	@FXML
	private PasswordField password;

	public void login() {
		String appPassword = DbManager.getProperty(APP_PASSWORD.getKey());
		String masterPassword = DbManager.getProperty(MASTER_PASSWORD.getKey());

		String enteredPassword = password.getText();

		if (BCrypt.checkpw(enteredPassword, appPassword) || BCrypt.checkpw(enteredPassword, masterPassword)) {
			((Stage) error.getScene().getWindow()).close();
			WindowManager.showMainWindow();
		} else {
			error.setVisible(true);
		}
	}

	public void resetDatabase() {
		String appPassword = DbManager.getProperty(APP_PASSWORD.getKey());
		String dbResetPassword = DbManager.getProperty(DB_RESET_PASSWORD.getKey());
		String masterPassword = DbManager.getProperty(MASTER_PASSWORD.getKey());

		String enteredPassword = password.getText();

		if (BCrypt.checkpw(enteredPassword, dbResetPassword) || BCrypt.checkpw(enteredPassword, masterPassword)) {
			boolean approved = showResetDatabaseConfirmationDialog();
			if (approved) {
				DbManager.close();
				FileManager.resetDatabase();
				DbManager.initialize(FileManager.getDatabasePath());
				DbManager.setProperty(APP_PASSWORD.getKey(), appPassword);
				DbManager.setProperty(DB_RESET_PASSWORD.getKey(), dbResetPassword);
				WindowManager.getMainController().refreshTableData();
				WindowManager.showInfoAlert(Messages.get("success"), Messages.get("database.reset.success.message"));
			}
			((Stage) error.getScene().getWindow()).close();
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