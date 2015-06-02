package tsk.jgnk.watchdist.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.type.PasswordType;
import tsk.jgnk.watchdist.util.Constants;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.FileManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@SuppressWarnings("unused")
public class AdministrationController {
	private static final Logger logger = LoggerFactory.getLogger(AdministrationController.class);

	@FXML
	private Button done;

	@SuppressWarnings("unused")
	public void editExcelTemplate() {
		try {
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
						Desktop.getDesktop().open(path.toFile());
					}
				} catch (InvalidPathException | SecurityException e) {
					logger.debug("Invalid path set for excel template: " + templatePath);
					WindowManager.showSetExcelTemplatePathWindow(
							Messages.get("excel.template.path.problem", templatePath)
					);
				}
			}


		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unused")
	public void exportDatabase() {
		Path databasePath = FileManager.getDatabasePath();

		FileChooser fileChooser = new FileChooser();

		FileChooser.ExtensionFilter extFilter
				= new FileChooser.ExtensionFilter(Messages.get("database.file") + " (*.db)", "*.db");
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setTitle(Messages.get("backup.export.database"));

		String fileName = databasePath.getFileName().toString();
		String extension = com.google.common.io.Files.getFileExtension(fileName);
		String nameWithoutExtension = com.google.common.io.Files.getNameWithoutExtension(fileName);
		String finalName = nameWithoutExtension + "-" + org.joda.time.LocalDate.now().toString() + "." + extension;

		fileChooser.setInitialFileName(finalName);
		File file = fileChooser.showSaveDialog(done.getScene().getWindow());
		if (file != null) {
			try {
				if (!file.getName().endsWith(".db")) file = new File(file.getPath() + ".db");
				Files.copy(databasePath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				WindowManager.showInfoAlert(Messages.get("success"),
						Messages.get("export.database.success", file.getAbsolutePath()));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("unused")
	public void resetExcelTemplate() {
		WindowManager.showSetExcelTemplatePathWindow(Messages.get("excel.template.path.not.set"));
	}

	@SuppressWarnings("unused")
	public void resetDatabase() {
		WindowManager.showResetDbPasswordWindow();

	}

	@SuppressWarnings("unused")
	public void closeWindow() {
		((Stage) done.getScene().getWindow()).close();
	}

	private boolean showResetExcelTemplateConfirmationDialog() {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(Messages.get("reset.excel.template"));
		alert.setHeaderText(Messages.get("confirmation"));
		alert.setContentText(Messages.get("reset.excel.template.confirmation"));

		ButtonType resetButtonType = new ButtonType(Messages.get("reset.excel.template"));
		ButtonType cancelButtonType = new ButtonType(Messages.get("cancel"));

		alert.getButtonTypes().setAll(resetButtonType, cancelButtonType);
		alert.getDialogPane().lookupButton(resetButtonType).setStyle("-fx-base: #F78181;");

		Optional<ButtonType> buttonType = alert.showAndWait();
		return buttonType.isPresent() && buttonType.get() == resetButtonType;
	}

	public void changeAppPassword() {
		WindowManager.showChangePasswordWindow(PasswordType.APP_PASSWORD);
	}

	public void changeDbResetPassword() {
		WindowManager.showChangePasswordWindow(PasswordType.DB_RESET_PASSWORD);
	}
}
