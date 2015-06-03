package org.utkuozdemir.watchdist.controller;

import com.google.common.base.Strings;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.exception.ValidationException;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.Constants;
import org.utkuozdemir.watchdist.util.DbManager;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class AddNewSoldierController implements Initializable {
	@FXML
	private TextField fullName;
	@FXML
	private TextField duty;
	@FXML
	private Button saveButton;
	@FXML
	private CheckBox available;
	@FXML
	private CheckBox sergeant;
	@FXML
	private Label errorLabel;
	@FXML
	private Label maxWatchCountPerDayLabel;
	@FXML
	private ComboBox<Integer> maxWatchCountPerDay;

	private MainController mainController;

	public AddNewSoldierController(MainController mainController) {
		this.mainController = mainController;
	}

	public void saveSoldier() {
		try {
			validateFields();
			Soldier soldier
					= new Soldier(fullName.getText(), duty.getText(), available.isSelected(),
					sergeant.isSelected(), maxWatchCountPerDay.getValue()
			);
			DbManager.createSoldier(soldier);
			resetFields();
			mainController.refreshTableData();
			mainController.scrollToLastElementInTable();
			fullName.requestFocus();
		} catch (ValidationException e) {
			errorLabel.setVisible(true);
		}
	}

	private void validateFields() {
		if (Strings.isNullOrEmpty(fullName.getText()) || Strings.isNullOrEmpty(duty.getText())) {
			throw new ValidationException();
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		maxWatchCountPerDayLabel.setText(Messages.get("max.watch.count.per.day", Constants.WATCH_DURATION_IN_HOURS));

		sergeant.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				available.selectedProperty().setValue(false);
			}
			available.setDisable(newValue);
		});

		ObservableList<Integer> hours = FXCollections.observableArrayList(
				ContiguousSet.create(Range.closed(1, Constants.TOTAL_WATCHES_IN_DAY + 1), DiscreteDomain.integers())
		);
		maxWatchCountPerDay.setItems(hours);
		resetFields();
	}

	private void resetFields() {
		fullName.clear();
		duty.clear();
		sergeant.setSelected(false);
		available.setSelected(true);
		maxWatchCountPerDay.setValue(Constants.MAX_WATCHES_IN_A_DAY);
		errorLabel.setVisible(false);
	}


	private boolean isValidInteger(String string) {
		try {
			//noinspection ResultOfMethodCallIgnored
			Integer.parseInt(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
