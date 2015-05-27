package tsk.jgnk.watchdist.controller;

import com.google.common.base.Strings;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import tsk.jgnk.watchdist.domain.Soldier;
import tsk.jgnk.watchdist.exception.ValidationException;
import tsk.jgnk.watchdist.util.DbManager;

import java.net.URL;
import java.util.ResourceBundle;

public class AddNewSoldierController implements Initializable {
    public TextField fullName;
    public TextField duty;
    public Button saveButton;
    public CheckBox available;
    public Label errorLabel;
    private MainController mainController;

    public AddNewSoldierController(MainController mainController) {
        this.mainController = mainController;
    }

    private void addEventHandlers() {
        EventHandler<KeyEvent> eventHandler = keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                saveSoldier();
            }
        };
        fullName.setOnKeyPressed(eventHandler);
        duty.setOnKeyPressed(eventHandler);
    }

    private void addListenersToButtons() {
        saveButton.setOnAction(actionEvent -> saveSoldier());
    }

    private void saveSoldier() {
        try {
            validateFields();

            Soldier soldier = new Soldier(fullName.getText(), duty.getText(), available.isSelected());
            DbManager.createSoldier(soldier);

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

            mainController.refreshTableData();
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
        addEventHandlers();
        addListenersToButtons();
    }
}
