package tsk.jgnk.watchdist.controller;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tsk.jgnk.watchdist.util.Constants;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;

public class AppPasswordController implements Initializable {
	public Label error;
	public PasswordField password;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public void login() {
		String appPassword = DbManager.getProperty(Constants.APP_PASSWORD);
		String masterPassword = DbManager.getProperty(Constants.MASTER_PASSWORD);

		String enteredPassword = password.getText();

		if (BCrypt.checkpw(enteredPassword, appPassword) || BCrypt.checkpw(enteredPassword, masterPassword)) {
			((Stage) error.getScene().getWindow()).close();
			WindowManager.showMainWindow(null);
		} else {
			error.setVisible(true);
		}
	}
}
