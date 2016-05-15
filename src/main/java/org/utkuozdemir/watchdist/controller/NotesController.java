package org.utkuozdemir.watchdist.controller;

import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.util.DbManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class NotesController {
    @FXML
    private Label title;

    @SuppressWarnings("unused")
    @FXML
    private TextArea notes;

    private DistributionController distributionController;
    private LocalDate date;

    private String originalNote;

    public void initData(DistributionController distributionController, LocalDate date) {
        this.distributionController = distributionController;
        this.date = date;
        title.setText(Messages.get("notes.of.day", date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))));
        String note = DbManager.getNote(date);
        originalNote = note;
        notes.setText(note);
    }

    public void saveNote() {
        DbManager.saveNote(date, notes.getText().trim());
        distributionController.refreshNote();
        ((Stage) title.getScene().getWindow()).close();
    }

    public void deleteNote() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Messages.get("confirmation"));
        alert.setHeaderText(Messages.get("confirmation"));
        alert.setContentText(Messages.get("delete.note.confirmation"));
        ButtonType delete = new ButtonType(Messages.get("delete.note"));
        ButtonType cancel = new ButtonType(Messages.get("cancel"));
        alert.getButtonTypes().setAll(delete, cancel);
        alert.getDialogPane().lookupButton(delete).setStyle("-fx-base: #F78181;");
        ((Button) alert.getDialogPane().lookupButton(cancel)).setCancelButton(true);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == delete) {
                DbManager.saveNote(date, "");
                distributionController.refreshNote();
                ((Stage) title.getScene().getWindow()).close();
            }
        });
    }

    public void closeAttempt() {
        if (Objects.equals(notes.getText(), originalNote)) {
            ((Stage) title.getScene().getWindow()).close();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(Messages.get("confirmation"));
            alert.setHeaderText(Messages.get("confirmation"));
            alert.setContentText(Messages.get("note.changed.save.prompt"));
            ButtonType discard = new ButtonType(Messages.get("discard.changes.and.exit"));
            ButtonType saveAndExit = new ButtonType(Messages.get("save.and.exit"));
            alert.getButtonTypes().setAll(discard, saveAndExit);
            alert.getDialogPane().lookupButton(discard).setStyle("-fx-base: #F78181;");
            ((Button) alert.getDialogPane().lookupButton(saveAndExit)).setDefaultButton(true);
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == saveAndExit) {
                    saveNote();
                } else {
                    ((Stage) title.getScene().getWindow()).close();
                }
            });
        }
    }
}
