package tsk.jgnk.watchdist.controller;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import org.thehecklers.monologfx.MonologFX;
import org.thehecklers.monologfx.MonologFXBuilder;
import org.thehecklers.monologfx.MonologFXButton;
import org.thehecklers.monologfx.MonologFXButtonBuilder;
import tsk.jgnk.watchdist.domain.Soldier;
import tsk.jgnk.watchdist.fx.SoldierFX;
import tsk.jgnk.watchdist.i18n.Language;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.util.Constants;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.FileManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class MainController implements Initializable {
    @FXML
    private TableView<SoldierFX> soldiersTable;
    @FXML
    private TableColumn<SoldierFX, Integer> idColumn;
    @FXML
    private TableColumn<SoldierFX, String> fullNameColumn;
    @FXML
    private TableColumn<SoldierFX, String> dutyColumn;
    @FXML
    private TableColumn<SoldierFX, Boolean> availableColumn;
    @FXML
    private TableColumn<SoldierFX, Double> pointsColumn;
    @FXML
    private TableColumn<SoldierFX, Boolean> sergeantColumn;

    @FXML
    private Button addNewSoldierButton;
    @FXML
    private Button deleteSoldiersButton;
    @FXML
    private Button editWatchPointsButton;
    @FXML
    private Button watchDistributionScreenButton;
    @FXML
    private ComboBox<Language> language;

    public void refreshTableData() {
        List<Soldier> allActiveSoldiers = DbManager.findAllActiveSoldiers();
        List<SoldierFX> soldierFXes = Lists.transform(allActiveSoldiers, Constants.SOLDIER_TO_FX);
        ObservableList<SoldierFX> data = FXCollections.observableArrayList(soldierFXes);
        soldiersTable.setItems(data);
    }

    private void showAddNewSoldierWindow() {
        WindowManager.showAddNewSoldierWindow(this);
    }

    private void initializeTable() {
        soldiersTable.setPlaceholder(new Label(Messages.get("main.no.soldiers")));
        soldiersTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        DateFormatSymbols symbols = new DateFormatSymbols(Messages.getLocale());
        ArrayList<String> weekdays = Lists.newArrayList(symbols.getShortWeekdays());

        // remove empty string at the beginning
        weekdays.remove(0);

        // move sunday to the end
        String lastDay = weekdays.remove(0);
        weekdays.add(lastDay);

        for (int i = 0; i < 7; i++) {
            TableColumn<SoldierFX, String> empty = new TableColumn<>("");
            empty.setMinWidth(60);
            empty.setMaxWidth(60);
            soldiersTable.getColumns().add(empty);
            for (int j = 0; j < 12; j++) {
                String startTime = String.format("%02d", j * 2);
                String endTime = String.format("%02d", (j + 1) * 2);
                String columnName = weekdays.get(i) + " " + startTime + ":00 - " + endTime + ":00";

                TableColumn<SoldierFX, Boolean> column = new TableColumn<>();

                VBox columnNameBox = new VBox();
                columnNameBox.getChildren().add(new Label(columnName));
                columnNameBox.setRotate(-90);
                columnNameBox.setPadding(new Insets(5, 5, 5, 5));
                Group g = new Group(columnNameBox);
                column.setGraphic(g);

                column.setCellFactory(soldierBooleanTableColumn -> new CheckBoxTableCell<>());
                column.setId(String.valueOf(j));

                final int finalI = i;
                final int finalJ = j;
                column.setCellValueFactory(
                        soldierBooleanCellDataFeatures -> {
                            SoldierFX soldierFX = soldierBooleanCellDataFeatures.getValue();
                            return soldierFX.availabilitiesBooleansProperties()[finalI][finalJ];
                        });
                soldiersTable.getColumns().add(column);
            }
        }


        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        fullNameColumn.setCellFactory(soldierStringTableColumn -> new TextFieldTableCell<>(Constants.STRING_STRING_CONVERTER));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        dutyColumn.setCellFactory(soldierStringTableColumn -> new TextFieldTableCell<>(Constants.STRING_STRING_CONVERTER));
        dutyColumn.setCellValueFactory(new PropertyValueFactory<>("duty"));

        availableColumn.setCellFactory(p -> {
            CheckBoxTableCell<SoldierFX, Boolean> cell = new CheckBoxTableCell<>();
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        availableColumn.setCellValueFactory(
                soldierBooleanCellDataFeatures -> soldierBooleanCellDataFeatures.getValue().availableProperty());

        sergeantColumn.setCellFactory(p -> {
            CheckBoxTableCell<SoldierFX, Boolean> cell = new CheckBoxTableCell<>();
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        sergeantColumn.setCellValueFactory(s -> s.getValue().sergeantProperty());

        pointsColumn.setCellFactory(soldierDoubleTableColumn -> new TextFieldTableCell<>(Constants.DOUBLE_STRING_CONVERTER));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
    }


    public void editWatchPoints() {
        WindowManager.showWatchPointsWindow();
    }

    public void openWatchDistributionScreen() {
        WindowManager.showWatchDistributionWindow(this);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        language.setItems(FXCollections.observableArrayList(Arrays.asList(Language.values())));
        language.setValue(Language.forLocale(Messages.getLocale()));
        language.valueProperty().addListener((observableValue, language1, t1) -> {
            WindowManager.switchLanguage(t1);
        });
        initializeTable();
        refreshTableData();
    }

    public void deleteSelectedSoldiers() {
        ObservableList<SoldierFX> selectedItems = soldiersTable.getSelectionModel().getSelectedItems();
        List<Soldier> soldiers = Lists.transform(selectedItems, Constants.FX_TO_SOLDIER);

        List<SoldierFX> filtered = Lists.newCopyOnWriteArrayList(
                Iterables.filter(selectedItems, soldier -> soldier != null));

        if (!filtered.isEmpty()) {
            MonologFXButton yes = MonologFXButtonBuilder.create()
                    .label(Messages.get("yes"))
                    .type(MonologFXButton.Type.YES)
                    .defaultButton(true)
                    .build();
            MonologFXButton no = MonologFXButtonBuilder.create()
                    .label(Messages.get("no"))
                    .type(MonologFXButton.Type.NO)
                    .build();


            MonologFX mono = MonologFXBuilder.create()
                    .modal(true)
                    .titleText(Messages.get("main.soldier.deletion.confirmation.title"))
                    .message(Messages.get("main.soldier.deletion.confirmation.message", filtered.size()))
                    .type(MonologFX.Type.QUESTION)
                    .button(yes)
                    .button(no)
                    .buttonAlignment(MonologFX.ButtonAlignment.RIGHT)
                    .build();

            MonologFXButton.Type result = mono.show();
            if (result == MonologFXButton.Type.YES) {
                DbManager.deleteSoldiers(soldiers);
                refreshTableData();
                soldiersTable.getSelectionModel().clearSelection();
            }
        }
    }

    public void addNewSoldier() {
        showAddNewSoldierWindow();
    }

    public void editExcelTemplate() {
        try {
            Path excelTemplate = FileManager.getExcelTemplate();
            Desktop.getDesktop().open(excelTemplate.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void editWatchValues() {
        WindowManager.showEditWatchValuesWindow();
    }
}
