package tsk.jgnk.watchdist.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.type.PasswordType;
import tsk.jgnk.watchdist.util.Constants;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class AppPasswordController implements Initializable {
	private static final Logger logger = LoggerFactory.getLogger(AppPasswordController.class);

	@FXML
	private Label error;
	@FXML
	private PasswordField password;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public void login() {
		String appPassword = DbManager.getProperty(PasswordType.APP_PASSWORD.getKey());
		String masterPassword = DbManager.getProperty(PasswordType.MASTER_PASSWORD.getKey());

		String enteredPassword = password.getText();

		if (BCrypt.checkpw(enteredPassword, appPassword) || BCrypt.checkpw(enteredPassword, masterPassword)) {
			((Stage) error.getScene().getWindow()).close();

			String templatePath = DbManager.getProperty(Constants.EXCEL_TEMPLATE_PATH_KEY);
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
		} else {
			error.setVisible(true);
		}
	}
}
