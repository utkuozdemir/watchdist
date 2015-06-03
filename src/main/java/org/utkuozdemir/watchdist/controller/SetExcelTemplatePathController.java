package org.utkuozdemir.watchdist.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.utkuozdemir.watchdist.util.FileManager;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.Constants;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@SuppressWarnings("unused")
public class SetExcelTemplatePathController {
	@FXML
	private Label info;
	@FXML
	private Label errorLabel;
	@FXML
	private TextField filePath;

	private Path path;

	private String infoMessage;

	public void chooseFile() {
		FileChooser fileChooser = new FileChooser();

		FileChooser.ExtensionFilter extFilter
				= new FileChooser.ExtensionFilter(Messages.get("excel.file") + " (*.xls)", "*.xls");
		fileChooser.getExtensionFilters().add(extFilter);
		fileChooser.setTitle(Messages.get("set.excel.template.path"));

		fileChooser.setInitialFileName(Constants.TEMPLATE_NAME);
		File file = fileChooser.showSaveDialog(filePath.getScene().getWindow());
		if (file != null) {
			if (!file.getName().endsWith(".xls")) file = new File(file.getPath() + ".xls");
			filePath.setText(file.getAbsolutePath());
			path = file.toPath();
			errorLabel.setVisible(false);
		}
	}

	public void saveAndContinue() {
		try {
			if (path != null) {
				Files.copy(FileManager.getCleanExcelTemplateInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				DbManager.setProperty(Constants.EXCEL_TEMPLATE_PATH_KEY, path.toFile().getAbsolutePath());
				WindowManager.showInfoAlert(
						Messages.get("success"),
						Messages.get("excel.template.path.set.success", path.toFile().getAbsolutePath())
				);
				((Stage) filePath.getScene().getWindow()).close();
				WindowManager.showMainWindow();
			} else {
				errorLabel.setText(Messages.get("file.not.chosen"));
				errorLabel.setVisible(true);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setInfoMessage(String infoMessage) {
		info.setText(infoMessage);
	}
}
