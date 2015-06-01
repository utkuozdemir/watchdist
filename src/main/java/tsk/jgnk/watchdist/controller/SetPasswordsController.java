package tsk.jgnk.watchdist.controller;

import com.google.common.base.Strings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.util.Constants;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.util.Objects;

public class SetPasswordsController {

	@FXML
	private Label appPasswordError;
	@FXML
	private Label dbResetPasswordError;
	@FXML
	private PasswordField appPassword1;
	@FXML
	private PasswordField appPassword2;
	@FXML
	private PasswordField dbResetPassword1;
	@FXML
	private PasswordField dbResetPassword2;

	public void savePasswordsAndContinue() {
		boolean appPasswordsValid = checkPasswords(appPassword1, appPassword2, appPasswordError);
		boolean dbResetPasswordsValid = checkPasswords(dbResetPassword1, dbResetPassword2, dbResetPasswordError);

		if (appPasswordsValid && dbResetPasswordsValid) {
			DbManager.setProperty(Constants.APP_PASSWORD, BCrypt.hashpw(appPassword1.getText(), BCrypt.gensalt()));
			DbManager.setProperty(Constants.DB_RESET_PASSWORD, BCrypt.hashpw(dbResetPassword1.getText(), BCrypt.gensalt()));
			WindowManager.showInfoAlert(Messages.get("success"), Messages.get("passwords.saved.successfuly"));
			WindowManager.showMainWindow(null);
			((Stage) appPasswordError.getScene().getWindow()).close();
		}
	}

	private boolean checkPasswords(PasswordField field1, PasswordField field2, Label errorLabel) {
		if (Strings.isNullOrEmpty(field1.getText()) || Strings.isNullOrEmpty(field2.getText())) {
			errorLabel.setVisible(true);
			errorLabel.setText(Messages.get("please.enter.a.password"));
			return false;
		}

		if (!Objects.equals(field1.getText(), field2.getText())) {
			errorLabel.setVisible(true);
			errorLabel.setText(Messages.get("passwords.dont.match"));
			return false;
		}

		errorLabel.setVisible(false);
		return true;
	}
}
