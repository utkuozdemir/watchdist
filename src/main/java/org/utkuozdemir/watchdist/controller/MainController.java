package org.utkuozdemir.watchdist.controller;

import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.fx.SoldierFX;
import org.utkuozdemir.watchdist.i18n.Language;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.util.Converters;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import static org.utkuozdemir.watchdist.app.Constants.H;
import static org.utkuozdemir.watchdist.app.Constants.L;
import static org.utkuozdemir.watchdist.app.Constants.SERIALIZED_MIME_TYPE;

@SuppressWarnings("unused")
public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private AnchorPane pane;
    @FXML
    private TableView<SoldierFX> soldiersTable;
    @FXML
    private TableColumn<SoldierFX, Integer> orderColumn;
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
    private TableColumn<SoldierFX, Boolean> fixedWatchColumn;
    @FXML
    private TableColumn<SoldierFX, Integer> maxWatchCountPerDayColumn;
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
    @FXML
    private TextField filter;
    @FXML
    private ProgressIndicator progressIndicator;

    private ObservableList<SoldierFX> masterTableData = FXCollections.observableArrayList();

    private int cc = 0;
    private long ts = 0;
    private ImageView iv;

    private Timer searchTimer;
    private Service<List<SoldierFX>> refreshTableDataService;

    public MainController() {
        iv = new ImageView(new Image(new ByteArrayInputStream(Base64.getDecoder().decode(H))));
        iv.setOnMouseClicked(event -> {
            try {
                String m = new String(java.util.Base64.getDecoder().decode(L),
                        StandardCharsets.UTF_8.name());
                WindowManager.showInfoAlert(m, m);
            } catch (UnsupportedEncodingException ignored) {
            } finally {
                pane.getChildren().remove(iv);
            }
        });
    }


    public void refreshTableData() {
        refreshTableDataService.restart();
    }

    public void scrollToLastElementInTable() {
        ObservableList<SoldierFX> items = soldiersTable.getItems();
        if (!items.isEmpty()) {
            soldiersTable.scrollTo(items.size() - 1);
        }
    }

    private void showAddNewSoldierWindow() {
        WindowManager.showAddNewSoldierWindow();
    }

    private void initializeTable() {
        soldiersTable.setPlaceholder(new Label(Messages.get("main.no.soldiers")));
        soldiersTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        soldiersTable.setRowFactory(param -> buildDraggableTableRow());

        MenuItem refresh = new MenuItem(Messages.get("refresh"));
        MenuItem selectAllDaysAndHours = new MenuItem(Messages.get("select.all.days.and.hours"));
        MenuItem deselectAllDaysAndHours = new MenuItem(Messages.get("deselect.all.days.and.hours"));
        refresh.setOnAction(event -> refreshTableData());

        selectAllDaysAndHours.setOnAction(event -> {
            soldiersTable.getSelectionModel().getSelectedItems().stream()
                    .forEach(soldierFX -> {
                        soldierFX.setImplicitCommit(false);
                        Arrays.stream(soldierFX.availabilitiesProperties())
                                .flatMap(Arrays::stream)
                                .forEach(simpleBooleanProperty -> simpleBooleanProperty.set(true));
                        soldierFX.setImplicitCommit(true);
                        soldierFX.commit();

                    });
            refreshTableData();
        });

        deselectAllDaysAndHours.setOnAction(event -> {
            soldiersTable.getSelectionModel().getSelectedItems().stream().forEach(soldierFX -> {
                soldierFX.setImplicitCommit(false);
                Arrays.stream(soldierFX.availabilitiesProperties())
                        .flatMap(Arrays::stream)
                        .forEach(simpleBooleanProperty -> simpleBooleanProperty.set(false));
                soldierFX.setImplicitCommit(true);
                soldierFX.commit();
            });
            refreshTableData();
        });

        soldiersTable.setContextMenu(new ContextMenu(refresh, selectAllDaysAndHours, deselectAllDaysAndHours));

        DateFormatSymbols symbols = new DateFormatSymbols(Messages.getLocale());
        List<String> weekdays = new ArrayList<>(Arrays.stream(symbols.getShortWeekdays()).collect(Collectors.toList()));

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
            for (int j = 0; j < Settings.getTotalWatchesInDay(); j++) {
                String startTime = String.format("%02d",
                        ((j * Settings.getOneWatchDurationInHours()) + Settings.getFirstWatchStartHour()) % 24);
                String endTime = String.format("%02d",
                        (((j + 1) * Settings.getOneWatchDurationInHours()) + Settings.getFirstWatchStartHour()) % 24);
                String minute = String.format("%02d", Settings.getFirstWatchStartMinute());
                String columnName = weekdays.get(i) + " " + startTime + ":" + minute + " - " + endTime + ":" + minute;

                TableColumn<SoldierFX, Boolean> column = new TableColumn<>();
                column.setMaxWidth(30);

                VBox columnNameBox = new VBox();
                columnNameBox.getChildren().add(new Label(columnName));
                columnNameBox.setRotate(-90);
                columnNameBox.setPadding(new Insets(5, 5, 5, 5));
                Group g = new Group(columnNameBox);
                column.setGraphic(g);

                final int finalI = i;
                final int finalJ = j;
                column.setCellFactory(soldierBooleanTableColumn -> {
                    CheckBoxTableCell<SoldierFX, Boolean> cell = new CheckBoxTableCell<>();
                    String blueColor = finalJ % 2 == 0 ? "rgba(133, 194, 255, .2)" : "rgba(36, 107, 178, .2)";
                    String greenColor = finalJ % 2 == 0 ? "rgba(102, 255, 51, .2)" : "rgba(51, 128, 26, .2)";
                    cell.setStyle("-fx-background-color:" + (finalI % 2 == 0 ? blueColor : greenColor) + ";");
                    return cell;
                });
                column.setId(String.valueOf(j));
                column.setCellValueFactory(value -> value.getValue().availabilitiesProperties()[finalI][finalJ]);
                soldiersTable.getColumns().add(column);
            }
        }

        orderColumn.setCellValueFactory(new PropertyValueFactory<>("order"));

        fullNameColumn.setCellFactory(col -> new TextFieldTableCell<>(Converters.STRING_STRING_CONVERTER));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        dutyColumn.setCellFactory(col -> new TextFieldTableCell<>(Converters.STRING_STRING_CONVERTER));
        dutyColumn.setCellValueFactory(new PropertyValueFactory<>("duty"));

        availableColumn.setCellFactory(p -> {
            CheckBoxTableCell<SoldierFX, Boolean> cell = new CheckBoxTableCell<>();
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        availableColumn.setCellValueFactory(value -> value.getValue().availableProperty());

        sergeantColumn.setCellFactory(p -> {
            CheckBoxTableCell<SoldierFX, Boolean> cell = new CheckBoxTableCell<>();
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        sergeantColumn.setCellValueFactory(s -> s.getValue().sergeantProperty());

        fixedWatchColumn.setCellFactory(p -> {
            CheckBoxTableCell<SoldierFX, Boolean> cell = new CheckBoxTableCell<>();
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

        fixedWatchColumn.setCellValueFactory(s -> s.getValue().fixedWatchProperty());

        pointsColumn.setCellFactory(soldierDoubleTableColumn ->
                new TextFieldTableCell<>(Converters.DOUBLE_STRING_CONVERTER));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));

        maxWatchCountPerDayColumn.setText(Messages.get("max.watch.count.per.day",
                Settings.getOneWatchDurationInHours()));
        maxWatchCountPerDayColumn.setCellFactory(column -> {
            ComboBoxTableCell<SoldierFX, Integer> cell = new ComboBoxTableCell<>(Converters.DOUBLE_INTEGER_CONVERTER);
            cell.getItems().setAll(FXCollections.observableArrayList(
                    IntStream.rangeClosed(1, Settings.getTotalWatchesInDay()).boxed().collect(Collectors.toList())
            ));

            return cell;
        });
        makeHeaderWrappable(maxWatchCountPerDayColumn);
        maxWatchCountPerDayColumn.setCellValueFactory(new PropertyValueFactory<>("maxWatchesPerDay"));
    }

    private TableRow<SoldierFX> buildDraggableTableRow() {
        TableRow<SoldierFX> row = new TableRow<>();
        row.itemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                row.setTooltip(new Tooltip(newValue.fullNameProperty().get()));
            }
        });

        row.setOnDragDetected(event -> {
            ObservableList<TableColumn<SoldierFX, ?>> sortOrder = soldiersTable.getSortOrder();
            boolean sortedByOrder = sortOrder.size() == 1 && sortOrder.get(0) == orderColumn &&
                    orderColumn.getSortType() == TableColumn.SortType.ASCENDING;
            if (!sortedByOrder) {
                Notifications.create().hideCloseButton()
                        .position(Pos.CENTER)
                        .text(Messages.get("order.by.order.col.before.ordering", orderColumn.getText()))
                        .hideAfter(new Duration(2000)).showWarning();
            } else {
                boolean unfiltered = filter.getText() == null || "".equals(filter.getText().trim());
                if (!unfiltered) {
                    Notifications.create().hideCloseButton()
                            .position(Pos.CENTER)
                            .text(Messages.get("clear.filter.before.ordering"))
                            .hideAfter(new Duration(2000)).showWarning();
                } else if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    IntStream.range(0, soldiersTable.getItems().size())
                            .filter(i -> i != index).forEach(i -> soldiersTable.getSelectionModel().clearSelection(i));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            }
        });

        row.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                if (row.getIndex() != (Integer) db.getContent(SERIALIZED_MIME_TYPE)) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    event.consume();
                }
            }
        });

        row.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                SoldierFX dragged = soldiersTable.getItems().remove(draggedIndex);

                int dropIndex;
                if (row.isEmpty()) {
                    dropIndex = soldiersTable.getItems().size();
                } else {
                    dropIndex = row.getIndex();
                }

                soldiersTable.getItems().add(dropIndex, dragged);

                event.setDropCompleted(true);
                soldiersTable.getSelectionModel().clearSelection();
                soldiersTable.getSelectionModel().select(dropIndex);
                saveOrders();
                event.consume();
            }
        });
        return row;
    }

    public void saveOrders() {
        IntStream.range(0, soldiersTable.getItems().size()).forEach(i ->
                soldiersTable.getItems().get(i).orderProperty().set(i + 1));
    }

    private void makeHeaderWrappable(TableColumn col) {
        Label label = new Label(col.getText());
        label.setStyle("-fx-padding: 8px;");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);

        StackPane stack = new StackPane();
        stack.getChildren().add(label);
        stack.prefWidthProperty().bind(col.widthProperty().subtract(5));
        label.prefWidthProperty().bind(stack.prefWidthProperty());
        col.setGraphic(stack);
    }


    public void editWatchPoints() {
        WindowManager.showWatchPointsWindow();
    }

    public void openWatchDistributionScreen() {
        WindowManager.showWatchDistributionWindow();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        soldiersTable.getSortOrder().setAll(Collections.singletonList(orderColumn));
        orderColumn.setSortType(TableColumn.SortType.ASCENDING);

        initializeRefreshTableDataService();

        language.getItems().setAll(Arrays.asList(Language.values()));

        language.setValue(Language.forLocale(Messages.getLocale()));
        language.valueProperty().addListener((observableValue, language1, t1) -> {
            WindowManager.switchLanguage(((Stage) language.getScene().getWindow()), t1);
        });

        filter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (searchTimer != null) searchTimer.cancel();
            searchTimer = new Timer();
            searchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    soldiersTable.getItems().setAll(filterSoldiers(masterTableData, newValue));
                }
            }, 500);
        });

        initializeTable();
        refreshTableData();
    }

    private void initializeRefreshTableDataService() {
        refreshTableDataService = new Service<List<SoldierFX>>() {
            @Override
            protected Task<List<SoldierFX>> createTask() {
                return new Task<List<SoldierFX>>() {
                    @Override
                    protected List<SoldierFX> call() throws Exception {
                        List<Soldier> soldiers = DbManager.findAllActiveSoldiersOrdered();
                        List<SoldierFX> soldierFXes = soldiers.stream()
                                .map(SoldierFX::new).collect(Collectors.toList());
                        masterTableData.setAll(soldierFXes);
                        return filterSoldiers(soldierFXes, filter.getText());
                    }
                };
            }
        };
        refreshTableDataService.setOnSucceeded(event ->
                soldiersTable.getItems().setAll(refreshTableDataService.getValue()));
        refreshTableDataService.setOnFailed(event -> {
            throw new RuntimeException(event.getSource().getException());
        });

        progressIndicator.visibleProperty().bind(refreshTableDataService.runningProperty());
        soldiersTable.editableProperty().bind(refreshTableDataService.runningProperty().not());
    }

    private List<SoldierFX> filterSoldiers(List<SoldierFX> list, String text) {
        return list.stream().filter(sfx -> {
            if (text == null || "".equals(text.trim())) {
                return true;
            } else {
                String data = (sfx.fullNameProperty().get() + " " + sfx.dutyProperty().get())
                        .toLowerCase(Messages.getLocale());
                return (data.matches("(.*\\s+)?" + text.toLowerCase(Messages.getLocale()) + ".*"));
            }
        }).collect(Collectors.toList());
    }

    public void deleteSelectedSoldiers() {
        ObservableList<SoldierFX> selectedItems = soldiersTable.getSelectionModel().getSelectedItems();
        List<Soldier> soldiers = selectedItems.stream().map(SoldierFX::getEntity).collect(Collectors.toList());
        List<SoldierFX> filtered = selectedItems.stream().filter(s -> s != null).collect(Collectors.toList());

        if (!filtered.isEmpty()) {
            boolean approved = WindowManager.showWarningConfirmationAlert(
                    Messages.get("main.soldier.deletion.confirmation.title"),
                    Messages.get("main.soldier.deletion.confirmation.message", filtered.size()),
                    Messages.get("delete.selected.soldiers"),
                    Messages.get("cancel"));

            if (approved) {
                DbManager.deleteSoldiers(soldiers);
                refreshTableData();
                soldiersTable.getSelectionModel().clearSelection();
            }
        }
    }

    public void addNewSoldier() {
        showAddNewSoldierWindow();
    }

    public void editWatchValues() {
        WindowManager.showWatchValuesWindow();
    }

    public void showAdministrationWindow() {
        WindowManager.showAdministrationWindow();
    }

    public void c() {
        try {
            if (iv != null) {
                cc = (System.currentTimeMillis() - ts) > 1024 ? 0 : cc + 1;
                ts = System.currentTimeMillis();
                if (cc > 32) {
                    if (!pane.getChildren().contains(iv)) pane.getChildren().add(iv);
                    double w = iv.getImage().getWidth();
                    double h = iv.getImage().getHeight();
                    AnchorPane.setBottomAnchor(iv, (pane.getHeight() - h) / 2);
                    AnchorPane.setLeftAnchor(iv, (pane.getWidth() - h) / 2);
                    AnchorPane.setTopAnchor(iv, (pane.getHeight() - h) / 2);
                    AnchorPane.setRightAnchor(iv, (pane.getWidth() - w) / 2);
                    cc = 0;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void showAboutInfo() {
        WindowManager.showInfoAlert(Messages.get("about"), Messages.get("about.text"));
    }

    public int getTableItemsSize() {
        return soldiersTable.getItems().size();
    }

    public void soldiersTableKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            deleteSelectedSoldiers();
        }
    }
}
