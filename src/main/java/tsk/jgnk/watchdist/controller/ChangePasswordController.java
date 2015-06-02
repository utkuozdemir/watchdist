package tsk.jgnk.watchdist.controller;

import com.google.common.base.Strings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.type.PasswordType;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.util.Objects;

public class ChangePasswordController {
	@FXML
	private Label infoHeader;
	@FXML
	private PasswordField currentPassword;
	@FXML
	private PasswordField newPassword1;
	@FXML
	private PasswordField newPassword2;
	@FXML
	private Label errorLabel;

	private PasswordType passwordType;

	public void initialize(PasswordType passwordType) {
		this.passwordType = passwordType;
		infoHeader.setText(Messages.get(passwordType.getMessageKey()));
	}

	public void changePassword() {
		if (passwordsAreValid()) {
			DbManager.setProperty(passwordType.getKey(), BCrypt.hashpw(newPassword1.getText(), BCrypt.gensalt()));
			WindowManager.showInfoAlert(Messages.get("success"), Messages.get("password.change.success"));
			((Stage) infoHeader.getScene().getWindow()).close();
		}
	}

	private boolean passwordsAreValid() {
		String masterPassword = DbManager.getProperty(PasswordType.MASTER_PASSWORD.getKey());
		String password = DbManager.getProperty(passwordType.getKey());
		if (!BCrypt.checkpw(currentPassword.getText(), password) &&
				!BCrypt.checkpw(currentPassword.getText(), masterPassword)) {
			errorLabel.setVisible(true);
			errorLabel.setText(Messages.get("invalid.current.password"));
			return false;
		}

		if (Strings.isNullOrEmpty(newPassword1.getText()) || Strings.isNullOrEmpty(newPassword2.getText())) {
			errorLabel.setVisible(true);
			errorLabel.setText(Messages.get("please.enter.a.password"));
			return false;
		}

		if (!Objects.equals(newPassword1.getText(), newPassword2.getText())) {
			errorLabel.setVisible(true);
			errorLabel.setText(Messages.get("passwords.dont.match"));
			return false;
		}

		errorLabel.setVisible(false);
		return true;
	}
}
