package org.utkuozdemir.watchdist.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.app.Constants;
import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.WatchValue;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class SetInitialValuesController implements Initializable {
	private static final Logger logger = LoggerFactory.getLogger(SetInitialValuesController.class);

	@FXML
	private ComboBox<Integer> oneWatchDurationInHours;
	@FXML
	private ComboBox<String> firstWatchStartHour;
	@FXML
	private ComboBox<String> firstWatchStartMinute;
	@FXML
	private ComboBox<Integer> watchesBetweenTwoWatches;
	@FXML
	private Label errorLabel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		oneWatchDurationInHours.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 6, 8));
		oneWatchDurationInHours.valueProperty().addListener((observable, oldValue, newValue) -> {
			List<String> values = new ArrayList<>();
			if (newValue != null) {
				values.addAll(IntStream.range(0, newValue)
						.boxed().map(i -> String.format("%02d", i)).collect(Collectors.toList()));
			}
			firstWatchStartHour.setItems(FXCollections.observableArrayList(values));
			firstWatchStartHour.setValue(newValue != null ? String.format("%02d", 0) : null);
		});

		firstWatchStartHour.setItems(FXCollections.observableArrayList(String.format("%02d", 0)));
		firstWatchStartHour.setValue(String.format("%02d", 0));
		firstWatchStartMinute.setItems(FXCollections
				.observableArrayList(IntStream.range(0, 60).mapToObj(i -> String.format("%02d", i)).collect
						(Collectors.toList())));
		firstWatchStartMinute.setValue("00");

		String watchDurationInHours = DbManager.getProperty(Constants.KEY_WATCH_DURATION_IN_HOURS);
		oneWatchDurationInHours.setValue(watchDurationInHours != null ? Integer.parseInt(watchDurationInHours) : null);

		List<Integer> watchesBetweenTwoWatchesValues = new ArrayList<>();
		watchesBetweenTwoWatchesValues.addAll(IntStream.rangeClosed(1, 8)
				.boxed().collect(Collectors.toList()));
		watchesBetweenTwoWatches
				.setItems(FXCollections.observableArrayList(watchesBetweenTwoWatchesValues));
		String watchesBetweenTwoWatches = DbManager.getProperty(Constants.KEY_WATCHES_BETWEEN_TWO_WATCHES);
		this.watchesBetweenTwoWatches
				.setValue(watchesBetweenTwoWatches != null ? Integer.parseInt(watchesBetweenTwoWatches) : null);
	}

	public void saveAndContinue() {
		if (oneWatchDurationInHours.getValue() == null || firstWatchStartHour.getValue() == null ||
				watchesBetweenTwoWatches.getValue() == null || firstWatchStartMinute.getValue() == null) {
			errorLabel.setVisible(true);
		} else {
			saveInitialValues();
			WindowManager.showInfoAlert(Messages.get("success"), Messages.get("settings.are.successfully.saved"));
			((Stage) errorLabel.getScene().getWindow()).close();
			goToNextStep();
		}
	}

	private void goToNextStep() {
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

	private void saveInitialValues() {
		DbManager.setProperty(Constants.KEY_WATCH_DURATION_IN_HOURS,
				String.valueOf(oneWatchDurationInHours.getValue()));
		DbManager.setProperty(Constants.KEY_FIRST_WATCH_START_HOUR,
				String.valueOf(Integer.parseInt(firstWatchStartHour.getValue())));
		DbManager.setProperty(Constants.KEY_WATCHES_BETWEEN_TWO_WATCHES,
				String.valueOf(watchesBetweenTwoWatches.getValue()));
		DbManager.setProperty(Constants.KEY_FIRST_WATCH_START_MINUTE,
				String.valueOf(Integer.parseInt(firstWatchStartMinute.getValue())));
		Settings.invalidateCache();

		List<WatchValue> watchValues = IntStream
				.range(0, Settings.getTotalWatchesInDay())
				.boxed().map(i -> new WatchValue(i, 1.0)).collect(Collectors.toList());
		DbManager.saveWatchValues(watchValues);
	}
}
