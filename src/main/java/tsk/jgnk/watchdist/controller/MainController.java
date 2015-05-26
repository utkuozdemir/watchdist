package tsk.jgnk.watchdist.controller;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tsk.jgnk.watchdist.util.WindowManager;

import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);


    public TableView<SoldierFX> soldiersTable;
    public TableColumn<SoldierFX, Integer> idColumn;
    public TableColumn<SoldierFX, String> fullNameColumn;
    public TableColumn<SoldierFX, String> dutyColumn;
    public TableColumn<SoldierFX, Boolean> availableColumn;
    public TableColumn<SoldierFX, Double> pointsColumn;

    public Button addNewSoldierButton;
    public Button deleteSoldiersButton;
    public Button editWatchPointsButton;
    public Button watchDistributionScreenButton;
    public ComboBox<Language> language;

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

                column.setCellFactory(new Callback<TableColumn<SoldierFX, Boolean>, TableCell<SoldierFX, Boolean>>() {
                    @Override
                    public TableCell<SoldierFX, Boolean> call(TableColumn<SoldierFX, Boolean> soldierBooleanTableColumn) {
                        return new CheckBoxTableCell<>();
                    }
                });
                column.setId(String.valueOf(j));

                final int finalI = i;
                final int finalJ = j;
                column.setCellValueFactory(
                        new Callback<TableColumn.CellDataFeatures<SoldierFX, Boolean>, ObservableValue<Boolean>>() {
                            @Override
                            public ObservableValue<Boolean> call(
                                    TableColumn.CellDataFeatures<SoldierFX, Boolean> soldierBooleanCellDataFeatures) {
                                SoldierFX soldierFX = soldierBooleanCellDataFeatures.getValue();
                                return soldierFX.availabilitiesBooleansProperties()[finalI][finalJ];
                            }
                        });
                soldiersTable.getColumns().add(column);
            }
        }


        idColumn.setCellValueFactory(new PropertyValueFactory<SoldierFX, Integer>("id"));

        fullNameColumn.setCellFactory(new Callback<TableColumn<SoldierFX, String>, TableCell<SoldierFX, String>>() {
            @Override
            public TableCell<SoldierFX, String> call(TableColumn<SoldierFX, String> soldierStringTableColumn) {
                return new TextFieldTableCell<>(Constants.STRING_STRING_CONVERTER);
            }
        });
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<SoldierFX, String>("fullName"));

        dutyColumn.setCellFactory(new Callback<TableColumn<SoldierFX, String>, TableCell<SoldierFX, String>>() {
            @Override
            public TableCell<SoldierFX, String> call(TableColumn<SoldierFX, String> soldierStringTableColumn) {
                return new TextFieldTableCell<>(Constants.STRING_STRING_CONVERTER);
            }
        });
        dutyColumn.setCellValueFactory(new PropertyValueFactory<SoldierFX, String>("duty"));

        availableColumn.setCellFactory(new Callback<TableColumn<SoldierFX, Boolean>, TableCell<SoldierFX, Boolean>>() {
            @Override
            public TableCell<SoldierFX, Boolean> call(TableColumn<SoldierFX, Boolean> p) {
                CheckBoxTableCell<SoldierFX, Boolean> cell = new CheckBoxTableCell<>();
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        });
        availableColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<SoldierFX, Boolean>, ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(
                            TableColumn.CellDataFeatures<SoldierFX, Boolean> soldierBooleanCellDataFeatures) {
                        return soldierBooleanCellDataFeatures.getValue().availableProperty();
                    }
                });

        pointsColumn.setCellFactory(new Callback<TableColumn<SoldierFX, Double>, TableCell<SoldierFX, Double>>() {
            @Override
            public TableCell<SoldierFX, Double> call(TableColumn<SoldierFX, Double> soldierDoubleTableColumn) {
                return new TextFieldTableCell<>(new StringConverter<Double>() {
                    @Override
                    public String toString(Double aDouble) {
                        return String.valueOf(aDouble);
                    }

                    @Override
                    public Double fromString(String s) {
                        double value = -1;
                        try {
                            value = Double.parseDouble(s);
                        } catch (NumberFormatException e) {
                            logger.debug(e.getMessage(), e);
                            //logger.debug
                            e.printStackTrace();
                        }

                        return value;
                    }
                });
            }
        });
        pointsColumn.setCellValueFactory(new PropertyValueFactory<SoldierFX, Double>("points"));
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
        language.valueProperty().addListener(new ChangeListener<Language>() {
            @Override
            public void changed(ObservableValue<? extends Language> observableValue, Language language, Language t1) {
                WindowManager.switchLanguage(t1);
            }
        });
        initializeTable();
        refreshTableData();
    }

    public void deleteSelectedSoldiers() {
        ObservableList<SoldierFX> selectedItems = soldiersTable.getSelectionModel().getSelectedItems();
        List<Soldier> soldiers = Lists.transform(selectedItems, Constants.FX_TO_SOLDIER);

        List<SoldierFX> filtered = Lists.newCopyOnWriteArrayList(
                Iterables.filter(selectedItems, new Predicate<SoldierFX>() {
                    @Override
                    public boolean apply(SoldierFX soldier) {
                        return soldier != null;
                    }
                }));

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

}
