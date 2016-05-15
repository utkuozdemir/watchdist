package org.utkuozdemir.watchdist.controller;

import org.utkuozdemir.watchdist.app.Constants;
import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

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
    @FXML
    private ProgressIndicator progressIndicator;

    private Service<Integer> saveSoldierService;

    private Soldier soldierToSave;

    public void saveSoldier() {
        if (fullName.getText() == null || fullName.getText().isEmpty() ||
                duty.getText() == null || duty.getText().isEmpty()) {
            errorLabel.setVisible(true);
        } else {
            soldierToSave = new Soldier(fullName.getText(), duty.getText(), available.isSelected(),
                    sergeant.isSelected(), maxWatchCountPerDay.getValue(),
                    WindowManager.getMainController().getTableItemsSize() + 1,
                    false
            );
            saveSoldierService.restart();
            resetFields();
            fullName.requestFocus();
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveSoldierService = new Service<Integer>() {
            @Override
            protected Task<Integer> createTask() {
                return new Task<Integer>() {
                    @Override
                    protected Integer call() throws Exception {
                        return DbManager.saveSoldier(soldierToSave);
                    }
                };
            }
        };
        saveSoldierService.setOnSucceeded(event -> {
            WindowManager.getMainController().refreshTableData();
            WindowManager.getMainController().scrollToLastElementInTable();
        });
        saveSoldierService.setOnFailed(event -> {
            throw new RuntimeException(event.getSource().getException());
        });

        progressIndicator.visibleProperty().bind(saveSoldierService.runningProperty());

        maxWatchCountPerDayLabel.setText(Messages.get("max.watch.count.per.day", Settings.getOneWatchDurationInHours()));

        sergeant.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                available.selectedProperty().setValue(false);
            }
            available.setDisable(newValue);
        });

        ObservableList<Integer> hours = FXCollections.observableArrayList(
                IntStream.rangeClosed(1, Settings.getTotalWatchesInDay()).boxed().collect(Collectors.toList())
        );
        maxWatchCountPerDay.setItems(hours);
        resetFields();
    }

    private void resetFields() {
        fullName.clear();
        duty.clear();
        sergeant.setSelected(false);
        available.setSelected(true);
        maxWatchCountPerDay.setValue(Constants.DEFAULT_MAX_WATCHES_IN_A_DAY);
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
