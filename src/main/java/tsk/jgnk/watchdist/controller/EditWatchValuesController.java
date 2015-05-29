package tsk.jgnk.watchdist.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import tsk.jgnk.watchdist.fx.WatchValueFX;
import tsk.jgnk.watchdist.util.Constants;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.WatchValues;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EditWatchValuesController implements Initializable {
    public TableView<WatchValueFX> valuesTable;
    public TableColumn<WatchValueFX, String> hoursColumn;
    public TableColumn<WatchValueFX, Double> valueColumn;
    public TextField sergeantDailyPoints;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hoursColumn.setCellValueFactory(param -> {
            int i = param.getValue().hourProperty().get();
            String startTime = String.format("%02d", i * 2);
            String endTime = String.format("%02d", (i + 1) * 2);
            return new SimpleStringProperty(startTime + ":00 - " + endTime + ":00");
        });

        valueColumn.setCellFactory(c -> new TextFieldTableCell<>(Constants.DOUBLE_STRING_CONVERTER));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        Map<Integer, Double> values = WatchValues.getAllValues();

        ObservableList<WatchValueFX> items
                = FXCollections.observableArrayList(values.entrySet().stream().map(
                e -> new WatchValueFX(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));

        valuesTable.setItems(items);

        String dailyPoints = DbManager.getProperty(Constants.SERGEANT_DAILY_POINTS);
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
                DbManager.setProperty(Constants.SERGEANT_DAILY_POINTS, String.valueOf(value));
            }
        });
    }

    public void closeWindow() {
        valuesTable.edit(-1, null);
        ((Stage) valuesTable.getScene().getWindow()).close();
    }
}
