package org.utkuozdemir.watchdist.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.SaveMode;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class AddNewWatchPointController implements Initializable {
	@FXML
	private TextField watchPointName;
	@FXML
	private ComboBox<Integer> requiredSoldierCount;
	@FXML
	private Label errorLabel;
	@FXML
	private Button saveWatchPointButton;

	public AddNewWatchPointController() {
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		resetFields();
	}

	private void resetFields() {
		watchPointName.clear();
		requiredSoldierCount.setValue(1);
		errorLabel.setVisible(false);
	}

	@SuppressWarnings("unused")
	public void saveWatchPoint() {
		if (watchPointName.getText() == null || watchPointName.getText().isEmpty()) {
			errorLabel.setVisible(true);
			return;
		}

		WatchPoint watchPoint = new WatchPoint(watchPointName.getText(),
				requiredSoldierCount.getValue(), true,
				WindowManager.getWatchPointsController().getTableItemsSize() + 1);
		DbManager.saveWatchPoint(watchPoint, SaveMode.INSERT_OR_UPDATE);

		resetFields();
		WindowManager.getWatchPointsController().refreshTableData();
		WindowManager.getWatchPointsController().scrollToLastElementInTable();
		watchPointName.requestFocus();
	}
}
