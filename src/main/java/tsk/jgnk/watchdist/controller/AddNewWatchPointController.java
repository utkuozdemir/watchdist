package tsk.jgnk.watchdist.controller;

import com.google.common.base.Strings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tsk.jgnk.watchdist.domain.WatchPoint;
import tsk.jgnk.watchdist.util.DbManager;

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

	private WatchPointsController watchPointsController;


	public AddNewWatchPointController(WatchPointsController watchPointsController) {
		this.watchPointsController = watchPointsController;
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
		if (Strings.isNullOrEmpty(watchPointName.getText())) {
			errorLabel.setVisible(true);
			return;
		}

		WatchPoint watchPoint = new WatchPoint(watchPointName.getText(), requiredSoldierCount.getValue());
		DbManager.createWatchPoint(watchPoint);

		resetFields();
		watchPointsController.refreshTableData();
		watchPointsController.scrollToLastElementInTable();
		watchPointName.requestFocus();
	}
}
