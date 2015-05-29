package tsk.jgnk.watchdist.controller;

import com.google.common.base.Strings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
        sergeant.setOnKeyPressed(eventHandler);
    }

    public void saveSoldier() {
        try {
            validateFields();

            Soldier soldier
                    = new Soldier(fullName.getText(), duty.getText(), available.isSelected(), sergeant.isSelected());
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

        sergeant.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                available.selectedProperty().setValue(false);
            }
            available.setDisable(newValue);
        });
    }
}
