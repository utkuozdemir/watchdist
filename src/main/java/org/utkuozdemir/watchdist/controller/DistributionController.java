package org.utkuozdemir.watchdist.controller;

import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.NullSoldier;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.domain.Watch;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.engine.DistributionEngine;
import org.utkuozdemir.watchdist.fx.WatchPointFX;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.DistributionRow;
import org.utkuozdemir.watchdist.util.ExcelExporter;
import org.utkuozdemir.watchdist.util.FileManager;
import org.utkuozdemir.watchdist.util.WatchValues;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.converter.LocalDateStringConverter;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@SuppressWarnings("unused")
public class DistributionController implements Initializable {
    @FXML
    private DatePicker currentDate;
    @FXML
    private TableView<DistributionRow> distributionTable;
    @FXML
    private TableColumn<DistributionRow, String> hoursColumn;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button distribute;
    @FXML
    private Button exportToExcel;
    @FXML
    private Button approve;
    @FXML
    private Button today;
    @FXML
    private Button previousDay;
    @FXML
    private Button nextDay;
    @FXML
    private Label notes;
    @FXML
    private Button addOrEditNotes;
    @FXML
    private CheckBox showColors;

    private Soldier[] selectedSoldiersBeforeEdit;
    private Service<Soldier[][]> distributionService;

    @SuppressWarnings("unused")
    public void distribute() {
        distributionTable.getScene().getWindow().setOnCloseRequest(event -> {
            if (distributionService.isRunning()) event.consume();
        });
        distributionService.restart();
    }

    private void loadDataToTable(Soldier[][] soldiers) {
        DistributionRow[] distributionRows = new DistributionRow[Settings.getTotalWatchesInDay()];

        distributionTable.getItems().clear();
        for (int i = 0; i < Settings.getTotalWatchesInDay(); i++) {
            String startTime = String.format("%02d",
                    (((i) * Settings.getOneWatchDurationInHours()) + Settings.getFirstWatchStartHour()) % 24);
            String endTime = String.format("%02d",
                    (((i + 1) * Settings.getOneWatchDurationInHours()) + Settings.getFirstWatchStartHour()) % 24);
            String minute = String.format("%02d", Settings.getFirstWatchStartMinute());
            String hours = startTime + ":" + minute + " - " + endTime + ":" + minute;
            distributionRows[i] = new DistributionRow(hours, new Soldier[0]);
            if (soldiers.length > 0) {
                distributionRows[i].setSoldiers(soldiers[i]);
            }
        }
        ObservableList<DistributionRow> rows = FXCollections.observableArrayList(Arrays.asList(distributionRows));
        distributionTable.setItems(rows);
    }

    @SuppressWarnings("unused")
    public void exportToExcel() {
        if (currentDate.getValue() != null) {
            List<Watch> watches = DbManager.findWatchesByDate(currentDate.getValue());
            if (watches.isEmpty()) {
                WindowManager.showWarningInfoAlert(Messages.get("distribution.approve.before.export.title"),
                        Messages.get("distribution.approve.before.export.message"));
            } else {
                FileChooser fileChooser = new FileChooser();

                FileChooser.ExtensionFilter extFilter
                        = new FileChooser.ExtensionFilter(Messages.get("distribution.excel.file") + " (*.xls)", "*.xls");
                fileChooser.getExtensionFilters().add(extFilter);
                fileChooser.setTitle(Messages.get("distribution.save.distribution.as.excel.file"));
                String fileName = currentDate.getValue().format(ISO_LOCAL_DATE) + "-" +
                        Messages.get("distribution.excel.file.name.suffix");
                fileChooser.setInitialFileName(fileName + ".xls");

                File file = fileChooser.showSaveDialog(distributionTable.getScene().getWindow());
                if (file == null) {
                    try {
                        String path = Paths.get(FileManager.class.getProtectionDomain()
                                .getCodeSource().getLocation().toURI()).getParent().toAbsolutePath()
                                + File.separator + "Excel" + File.separator + fileName + ".xls";
                        boolean confirmed = WindowManager
                                .showConfirmationAlert(Messages.get("confirmation"),
                                        Messages.get("file.could.not.be.selected", path),
                                        Messages.get("save.and.continue"), Messages.get("dont.save"));
                        if (confirmed) {
                            file = Paths.get(path).toFile();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (file != null) {
                    try {
                        if (!file.getName().endsWith(".xls"))
                            file = new File(file.getPath() + ".xls");
                        ExcelExporter.export(file, currentDate.getValue(), watches);
                        Desktop.getDesktop().open(file);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void approveDistribution() {
        if (currentDate.getValue() != null) {
            for (DistributionRow row : distributionTable.getItems()) {
                Soldier[] soldiers = Arrays.copyOf(row.getSoldiers(), row.getSoldiers().length);
                for (int i = 0; i < row.getSoldiers().length; i++) {
                    if (row.getSoldiers()[i] instanceof NullSoldier) {
                        soldiers[i] = null;
                    }
                }
                row.setSoldiers(soldiers);
            }

            boolean approved = WindowManager.showConfirmationAlert(
                    Messages.get("distribution.distribution.approval"), buildConfirmMessage(),
                    Messages.get("APPROVE"), Messages.get("cancel"));
            if (approved) {
                Map<TableColumn<DistributionRow, ?>, WatchPoint> columnWatchPointMap = new HashMap<>();
                ObservableList<TableColumn<DistributionRow, ?>> columns = distributionTable.getColumns();
                for (int i = 1; i < columns.size(); i++) {
                    TableColumn<DistributionRow, ?> column = columns.get(i);

                    String watchPointId = !column.getId().contains("-") ? column.getId() :
                            column.getId().substring(0, column.getId().indexOf("-"));

                    Optional<WatchPoint> watchPointOptional
                            = DbManager.findWatchPointById(Integer.parseInt(watchPointId));
                    //noinspection OptionalGetWithoutIsPresent
                    columnWatchPointMap.put(column, watchPointOptional.get());
                }

                ObservableList<DistributionRow> rows = distributionTable.getItems();

                Set<Watch> watchesToBeSaved = new HashSet<>();
                for (int i = 0; i < Settings.getTotalWatchesInDay(); i++) {
                    Soldier[] soldiers = rows.get(i).getSoldiers();

                    List<WatchPoint> used = new ArrayList<>();
                    for (int j = 0; j < soldiers.length; j++) {
                        // determine watch point
                        WatchPoint watchPoint = columnWatchPointMap.get(columns.get(j + 1));
                        Soldier soldier = soldiers[j];

                        if (soldier instanceof NullSoldier) soldier = null;
                        Watch watch = new Watch(soldier, watchPoint, Collections.frequency(used, watchPoint),
                                currentDate.getValue().format(ISO_LOCAL_DATE), i, WatchValues.get(i));
                        watchesToBeSaved.add(watch);

                        used.add(watchPoint);
                    }
                }
                DbManager.saveWatchesAddPointsDeleteOldWatches(
                        currentDate.getValue(), watchesToBeSaved
                );

                WindowManager.showInfoAlert(Messages.get("success"), Messages.get("distribution.success.message"));
                WindowManager.getMainController().refreshTableData();
            }
        }
    }

    @SuppressWarnings("unused")
    public void goToToday() {
        currentDate.setValue(LocalDate.now());
    }

    @SuppressWarnings("unused")
    public void jumpToPreviousDay() {
        if (currentDate.getValue() != null) {
            currentDate.setValue(currentDate.getValue().minusDays(1));
        }
    }

    @SuppressWarnings("unused")
    public void jumpToNextDay() {
        if (currentDate.getValue() != null) {
            currentDate.setValue(currentDate.getValue().plusDays(1));
        }
    }

    private String buildConfirmMessage() {
        if (currentDate.getValue() == null) throw new RuntimeException("Current date is null!");

        boolean perfect = true;

        StringBuilder approveMessage = new StringBuilder();
        ObservableList<DistributionRow> rows = distributionTable.getItems();

        for (DistributionRow row : rows) {
            for (Soldier soldier : row.getSoldiers()) {
                if (soldier == null) {
                    perfect = false;
                    approveMessage.append(Messages.get("distribution.empty.points"))
                            .append(System.lineSeparator())
                            .append(System.lineSeparator());
                    break;
                }
            }
            if (!perfect) break;
        }

        Set<Soldier> collidedSoldiers = getCollidedSoldiers(currentDate.getValue(), rows);
        if (!collidedSoldiers.isEmpty()) {
            perfect = false;
            String message = Messages.get(collidedSoldiers.size() > 1 ?
                            "distribution.consequent.watches.multiple" :
                            "distribution.consequent.watches",
                    collidedSoldiers.stream().map(String::valueOf).collect(Collectors.joining(", ")),
                    (Settings.getMinWatchesBetweenTwoWatches() + 1) * Settings.getOneWatchDurationInHours()
            );
            approveMessage.append(message)
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        List<Watch> existingWatches = DbManager.findWatchesByDate(currentDate.getValue());
        if (!existingWatches.isEmpty()) {
            perfect = false;
            approveMessage.append(Messages.get("distribution.already.existing.records"))
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        approveMessage.append(System.lineSeparator());

        return perfect ? Messages.get("distribution.perfect.approval") :
                approveMessage.append(Messages.get("distribution.imperfect.approval")).toString();
    }

    private Set<Soldier> getCollidedSoldiers(LocalDate currentDate, ObservableList<DistributionRow> rows) {
        Comparator<Soldier> soldierComparator = (o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            return o1.getFullName().compareTo(o2.getFullName());
        };
        Set<Soldier> collidedSoldiers = new TreeSet<>(soldierComparator);
        for (int i = 0; i < rows.size(); i++) {
            List<Soldier> previousSoldiers
                    = IntStream.range(i - Settings.getMinWatchesBetweenTwoWatches(), i).boxed().flatMap(n -> {
                if (n < 0) {
                    return DbManager.findWatchesByDateAndHour(currentDate.minusDays(1),
                            Settings.getTotalWatchesInDay() + n).stream()
                            .map(Watch::getSoldier);
                } else {
                    Soldier[] soldiers = rows.get(n).getSoldiers();
                    return Arrays.stream(soldiers);
                }
            }).collect(Collectors.toList());

            Set<Soldier> currentRowSoldiers = new HashSet<>();
            currentRowSoldiers.addAll(Arrays.asList(rows.get(i).getSoldiers()));
            currentRowSoldiers.retainAll(previousSoldiers);
            currentRowSoldiers.remove(null);
            collidedSoldiers.addAll(currentRowSoldiers);
        }
        return collidedSoldiers;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showColors.selectedProperty().addListener((observable, oldValue, newValue) -> refreshCellColors());

        initializeDistributionService();

        distributionTable.setPlaceholder(new Label(Messages.get("distribution.no.data.in.table")));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("hours"));
        hoursColumn.setStyle("-fx-alignment: CENTER;");

        refreshTableColumns();
        currentDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            refreshData();
            refreshNote();
        });
        currentDate.setConverter(new LocalDateStringConverter(FormatStyle.LONG,
                Messages.getLocale(), currentDate.getChronology()));
        currentDate.setValue(LocalDate.now());
    }

    private void initializeDistributionService() {
        distributionService = new Service<Soldier[][]>() {
            @Override
            protected Task<Soldier[][]> createTask() {
                return new Task<Soldier[][]>() {
                    @Override
                    protected Soldier[][] call() throws Exception {
                        List<Watch> watches = DbManager.findWatchesByDate(currentDate.getValue());
                        List<WatchPoint> watchPoints = watches.isEmpty() ?
                                DbManager.findAllActiveWatchPointsOrdered() :
                                watches.stream().map(Watch::getWatchPoint).distinct().collect(Collectors.toList());
                        return DistributionEngine.create(currentDate.getValue())
                                .distribute(DbManager.findAllActiveSoldiersOrdered(), watchPoints);
                    }
                };
            }
        };
        distributionService.setOnSucceeded(event -> loadDataToTable(distributionService.getValue()));
        distributionService.setOnFailed(event -> {
            throw new RuntimeException(event.getSource().getException());
        });

        progressIndicator.visibleProperty().bind(distributionService.runningProperty());
        Arrays.asList(addOrEditNotes, distribute, exportToExcel, approve, today, previousDay, nextDay, currentDate)
                .forEach(control -> control.disableProperty().bind(distributionService.runningProperty()));
        distributionTable.editableProperty().bind(distributionService.runningProperty().not());
    }

    @SuppressWarnings("unchecked")
    private void refreshTableColumns() {
        Iterator<TableColumn<DistributionRow, ?>> columnIterator = distributionTable.getColumns().iterator();
        while (columnIterator.hasNext()) {
            TableColumn<DistributionRow, ?> column = columnIterator.next();
            if (column != hoursColumn) columnIterator.remove();
        }

        List<WatchPoint> watchPoints;
        List<Watch> watches = DbManager.findWatchesByDate(currentDate.getValue() != null ? currentDate.getValue() : LocalDate.now());
        if (watches.isEmpty()) {
            watchPoints = DbManager.findAllActiveWatchPointsOrdered();
        } else {
            watchPoints = watches.stream().map(w -> w != null ? w.getWatchPoint() : null).collect(Collectors.toList());

            watchPoints.removeAll(Collections.<WatchPoint>singleton(null));
            Set<WatchPoint> dupesRemoved = new TreeSet<>((o1, o2) -> o1.getOrder() - o2.getOrder());
            dupesRemoved.addAll(watchPoints);
            watchPoints = new ArrayList<>(dupesRemoved);
        }

        List<WatchPointFX> watchPointFXes =
                watchPoints.stream().map(WatchPointFX::new).collect(Collectors.toList());

        final ObservableList<Soldier> soldiers
                = FXCollections.observableArrayList(DbManager.findAllActiveSoldiersOrderedByFullName());

        int num = 0;
        int count = watchPointFXes.stream().mapToInt(value -> value.requiredSoldierCountProperty().get()).sum();
        for (WatchPointFX watchPointFX : watchPointFXes) {
            for (int i = 0; i < watchPointFX.requiredSoldierCountProperty().get(); i++) {
                String columnName
                        = watchPointFX.requiredSoldierCountProperty().get() > 1 ?
                        watchPointFX.nameProperty().get() + " - " + (i + 1) : watchPointFX.nameProperty().get();
                TableColumn<DistributionRow, Soldier> column = new TableColumn<>(columnName);
                column.setMinWidth(90);
                column.setStyle("-fx-alignment: CENTER;");
                column.prefWidthProperty().bind(distributionTable.widthProperty().subtract(
                        hoursColumn.widthProperty()
                ).multiply((0.99 / count)));

                final int finalNum = num;
                column.setCellValueFactory(distributionRowSoldierCellDataFeatures -> {
                    SimpleObjectProperty<Soldier>[] soldiersProperties
                            = distributionRowSoldierCellDataFeatures.getValue().soldiersProperties();
                    if (soldiersProperties.length == 0) return null;
                    return soldiersProperties[finalNum];
                });

                column.setCellFactory(distributionRowSoldierTableColumn -> {
                    final ComboBoxTableCell<DistributionRow, Soldier> cell = new ComboBoxTableCell<DistributionRow, Soldier>() {
                        @Override
                        public void updateItem(Soldier item, boolean empty) {
                            super.updateItem(item, empty);
                            refreshCellColors();
                        }
                    };

                    cell.editingProperty().addListener((observableValue, aBoolean, t1) -> {
                        if (t1) {
                            TableRow<DistributionRow> tableRow = cell.getTableRow();
                            DistributionRow distributionRow = tableRow.getItem();
                            selectedSoldiersBeforeEdit = Arrays.copyOf(distributionRow.getSoldiers(), distributionRow.getSoldiers().length);
                        }
                    });
                    cell.itemProperty().addListener((observableValue, soldier, t1) -> {
                        ComboBoxTableCell<DistributionRow, Soldier> cellBean
                                = (ComboBoxTableCell<DistributionRow, Soldier>) ((SimpleObjectProperty) observableValue)
                                .getBean();
                        if (t1 instanceof NullSoldier) cell.setItem(null);
                        if (t1 != null && !(t1 instanceof NullSoldier)) {
                            TableRow<DistributionRow> r = cell.getTableRow();
                            DistributionRow row = r.getItem();
                            if (row != null) {
                                List<Soldier> rowSoldiers = Arrays.asList(row.getSoldiers());
                                if (Collections.frequency(rowSoldiers, t1) > 1) {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle(Messages.get("error"));
                                    alert.setHeaderText(Messages.get("error"));
                                    alert.setContentText(Messages.get("distribution.soldier.already.has.watch.select.another"));

                                    ButtonType undoButtonType = new ButtonType(Messages.get("continue"));

                                    alert.getButtonTypes().setAll(undoButtonType);
                                    ((Button) alert.getDialogPane().lookupButton(undoButtonType)).setDefaultButton(true);
                                    alert.showAndWait();

                                    row.setSoldiers(selectedSoldiersBeforeEdit);
                                    cell.setItem(soldier);
                                }
                            }
                        }
                    });


                    cell.getItems().add(new NullSoldier());
                    cell.getItems().addAll(soldiers);
                    return cell;
                });

                column.setId(String.valueOf(watchPointFX.idProperty().get() + "-" + i));
                distributionTable.getColumns().add(column);
                num++;
            }
        }
    }

    void refreshNote() {
        if (currentDate.getValue() != null) {
            String note = DbManager.getNote(currentDate.getValue());
            notes.setText((note == null || "".equals(note.trim())) ? Messages.get("no.notes") : note);
        }
    }

    private void refreshData() {
        if (currentDate.getValue() != null) {
            refreshTableColumns();
            List<Watch> watches = DbManager.findWatchesByDate(currentDate.getValue());

            List<WatchPoint> watchPoints;
            if (watches.isEmpty()) {
                watchPoints = DbManager.findAllActiveWatchPointsOrdered();
            } else {
                watchPoints = watches.stream()
                        .map(input -> input == null ? null : input.getWatchPoint()).collect(Collectors.toList());
            }
            watchPoints.removeAll(Collections.<WatchPoint>singleton(null));

            TreeSet<WatchPoint> dupesRemovedSortedWatchPoints = new TreeSet<>((o1, o2) -> o1.getOrder() - o2.getOrder());
            dupesRemovedSortedWatchPoints.addAll(watchPoints);

            int soldierCount = dupesRemovedSortedWatchPoints
                    .stream().mapToInt(WatchPoint::getRequiredSoldierCount).sum();
            Soldier[][] soldiers = new Soldier[Settings.getTotalWatchesInDay()][soldierCount];
            for (Watch watch : watches) {
                for (TableColumn<DistributionRow, ?> column : distributionTable.getColumns()) {
                    String colId = column.getId();
                    String watchPointId = colId.contains("-") ?
                            colId.substring(0, colId.indexOf("-")) : colId;

                    String watchPointSlot = colId.contains("-") ?
                            colId.substring(colId.indexOf("-") + 1, colId.length()) : colId;

                    boolean isInteger = true;
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        Integer.parseInt(watchPointId);
                    } catch (NumberFormatException e) {
                        isInteger = false;
                    }

                    if (isInteger &&
                            Integer.parseInt(watchPointId) == watch.getWatchPoint().getId() &&
                            Integer.parseInt(watchPointSlot) == watch.getWatchPointSlot()
                            ) {
                        int i = watch.getHour();
                        int j = distributionTable.getColumns().indexOf(column) - 1;
                        soldiers[i][j] = watch.getSoldier();
                    }
                }
            }
            loadDataToTable(soldiers);
        }
    }

    private void refreshCellColors() {
        if (showColors.selectedProperty().get()) {

            List<Soldier> soldiers =
                    distributionTable.getItems().stream()
                            .flatMap(r -> Arrays.stream(r.getSoldiers()))
                            .filter(s -> s != null)
                            .collect(Collectors.toList());

            Integer maxOccurrence = soldiers.stream()
                    .reduce(BinaryOperator.maxBy((o1, o2) -> Collections.frequency(soldiers, o1) -
                            Collections.frequency(soldiers, o2)))
                    .map(s -> Collections.frequency(soldiers, s)).orElse(0);
            Integer minOccurrence = soldiers.stream()
                    .reduce(BinaryOperator.minBy((o1, o2) -> Collections.frequency(soldiers, o1) -
                            Collections.frequency(soldiers, o2)))
                    .map(s -> Collections.frequency(soldiers, s)).orElse(0);

            distributionTable.lookupAll(".table-row-cell").forEach(r -> r.lookupAll(".table-cell").forEach(c -> {
                if (c instanceof ComboBoxTableCell) {
                    @SuppressWarnings("unchecked")
                    ComboBoxTableCell<DistributionRow, Soldier> cell = (ComboBoxTableCell<DistributionRow, Soldier>) c;
                    Soldier soldier = cell.getItem();
                    if (soldier != null) {
                        int stepSize = !Objects.equals(maxOccurrence, minOccurrence) ?
                                120 / (maxOccurrence - minOccurrence) : 0;
                        int frequency = Collections.frequency(soldiers, soldier);
                        float hue = (120f - (stepSize * (frequency - minOccurrence))) / 360f;
                        Color color = Color.getHSBColor(hue, 0.7f, 0.9f);
                        String rgba = "rgba(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ", .35)";
                        cell.setStyle(cell.getStyle() + "-fx-background-color:" + rgba + ";");
                    } else {
                        cell.setStyle(cell.getStyle().replaceAll(
                                "\\-fx\\-background\\-color:rgba\\(.*\\);", ""));
                    }
                }
            }));
        } else {
            distributionTable.lookupAll(".table-row-cell").forEach(r -> r.lookupAll(".table-cell").forEach(c -> {
                if (c instanceof ComboBoxTableCell) {
                    @SuppressWarnings("unchecked")
                    ComboBoxTableCell<DistributionRow, Soldier> cell = (ComboBoxTableCell<DistributionRow, Soldier>) c;
                    cell.setStyle(cell.getStyle().replaceAll(
                            "\\-fx\\-background\\-color:rgba\\(.*\\);", ""));
                }
            }));
        }
    }

    public void addOrEditNotes() {
        if (currentDate.getValue() != null) {
            WindowManager.showNotesWindow(this, currentDate.getValue());
        }
    }
}