package org.utkuozdemir.watchdist.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.utkuozdemir.watchdist.Constants;
import org.utkuozdemir.watchdist.Settings;
import org.utkuozdemir.watchdist.fx.WatchValueFX;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WatchValues;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static org.utkuozdemir.watchdist.util.Converters.DOUBLE_STRING_CONVERTER;

@SuppressWarnings("unused")
public class EditWatchValuesController implements Initializable {
    @FXML
    private TableView<WatchValueFX> valuesTable;
    @FXML
    private TableColumn<WatchValueFX, String> hoursColumn;
    @FXML
    private TableColumn<WatchValueFX, Double> valueColumn;
    @FXML
    private TextField sergeantDailyPoints;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hoursColumn.setCellValueFactory(param -> {
            int i = param.getValue().hourProperty().get();
            String startTime = String.format("%02d",
                    (((i) * Settings.getOneWatchDurationInHours()) + Settings.getFirstWatchStartHour()) % 24);
            String endTime = String.format("%02d",
                    (((i + 1) * Settings.getOneWatchDurationInHours()) + Settings.getFirstWatchStartHour()) % 24);
			String minute = String.format("%02d", Settings.getFirstWatchStartMinute());
			return new SimpleStringProperty(startTime + ":" + minute + " - " + endTime + ":" + minute);
		});

        valueColumn.setCellFactory(c -> new TextFieldTableCell<>(DOUBLE_STRING_CONVERTER));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        Map<Integer, Double> values = WatchValues.getAllValues();

        ObservableList<WatchValueFX> items
                = FXCollections.observableArrayList(values.entrySet().stream().map(
                e -> new WatchValueFX(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));

        valuesTable.setItems(items);

        String dailyPoints = DbManager.getProperty(Constants.KEY_SERGEANT_DAILY_POINTS);
        sergeantDailyPoints.setText(dailyPoints);

        sergeantDailyPoints.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-9]*\\.?[0-9]*")) {
                sergeantDailyPoints.textProperty().setValue(oldValue);
                return;
            }
            double value;
            try {
                value = Double.parseDouble(newValue);
            } catch (NumberFormatException e) {
                value = -1;
            }
            if (value > 0) {
                DbManager.setProperty(Constants.KEY_SERGEANT_DAILY_POINTS, String.valueOf(value));
            }
        });
    }

    public void closeWindow() {
        valuesTable.edit(-1, null);
        ((Stage) valuesTable.getScene().getWindow()).close();
    }
}
