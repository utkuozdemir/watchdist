package org.utkuozdemir.watchdist.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.type.PasswordType;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.util.Objects;

@SuppressWarnings("unused")
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

		if (newPassword1.getText() == null || newPassword1.getText().isEmpty() ||
				newPassword2.getText() == null || newPassword2.getText().isEmpty()) {
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
