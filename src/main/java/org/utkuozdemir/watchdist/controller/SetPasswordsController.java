package org.utkuozdemir.watchdist.controller;

import com.google.common.base.Strings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.Constants;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.utkuozdemir.watchdist.type.PasswordType.APP_PASSWORD;
import static org.utkuozdemir.watchdist.type.PasswordType.DB_RESET_PASSWORD;

public class SetPasswordsController {
	private static final Logger logger = LoggerFactory.getLogger(SetPasswordsController.class);
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
		boolean appPasswordsValid = passwordsAreValid(appPassword1, appPassword2, appPasswordError);
		boolean dbResetPasswordsValid = passwordsAreValid(dbResetPassword1, dbResetPassword2, dbResetPasswordError);

		if (appPasswordsValid && dbResetPasswordsValid) {
			DbManager.setProperty(APP_PASSWORD.getKey(), BCrypt.hashpw(appPassword1.getText(), BCrypt.gensalt()));
			DbManager.setProperty(DB_RESET_PASSWORD.getKey(), BCrypt.hashpw(dbResetPassword1.getText(), BCrypt
					.gensalt()));
			WindowManager.showInfoAlert(Messages.get("success"), Messages.get("passwords.saved.successfuly"));
			((Stage) appPasswordError.getScene().getWindow()).close();

			String oneWatchDuration = DbManager.getProperty(Constants.KEY_WATCH_DURATION_IN_HOURS);
			String firstWatchStartHour = DbManager.getProperty(Constants.KEY_FIRST_WATCH_START_HOUR);
			String watchesBetweenTwoWatches = DbManager.getProperty(Constants.KEY_WATCHES_BETWEEN_TWO_WATCHES);

			if (oneWatchDuration == null || firstWatchStartHour == null || watchesBetweenTwoWatches == null) {
				WindowManager.showSetInitialValuesWindow();
			} else {
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
							WindowManager.showMainWindow();
						}
					} catch (InvalidPathException | SecurityException e) {
						logger.debug("Invalid path set for excel template: " + templatePath);
						WindowManager.showSetExcelTemplatePathWindow(
								Messages.get("excel.template.path.problem", templatePath)
						);
					}
				}
			}
		}
	}

	private boolean passwordsAreValid(PasswordField field1, PasswordField field2, Label errorLabel) {
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
