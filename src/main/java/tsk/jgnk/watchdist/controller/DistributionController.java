package tsk.jgnk.watchdist.controller;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.thehecklers.monologfx.MonologFX;
import org.thehecklers.monologfx.MonologFXBuilder;
import org.thehecklers.monologfx.MonologFXButton;
import org.thehecklers.monologfx.MonologFXButtonBuilder;
import tsk.jgnk.watchdist.domain.Soldier;
import tsk.jgnk.watchdist.domain.Watch;
import tsk.jgnk.watchdist.domain.WatchPoint;
import tsk.jgnk.watchdist.fx.WatchPointFX;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class DistributionController implements Initializable {
    public TableView<DistributionRow> distributionTable;
    public TableColumn<DistributionRow, String> hoursColumn;
    public ComboBox<Integer> day;
    public ComboBox<String> month;
    public ComboBox<Integer> year;
    public Label dayName;
    private MainController mainController;
    private Soldier[] selectedSoldiersBeforeEdit;

    public DistributionController(MainController mainController) {
        this.mainController = mainController;
        checkNotNull(mainController);
    }

    @SuppressWarnings("unused")
    public void distribute() {
        Soldier[][] soldiers = DistributionEngine.makeDistribution(getCurrentDate());
        loadDataToTable(soldiers);
    }

    private LocalDate getCurrentDate() {
        Integer dayValue = day.getValue();
        Integer monthValue = getSelectedMonth();
        Integer yearValue = year.getValue();
        if (dayValue == null || monthValue == null || yearValue == null) return null;
        return new LocalDate(yearValue, monthValue, dayValue);
    }

    private void setCurrentDate(LocalDate date) {
        year.setValue(date.getYear());
        List<String> monthNames = Arrays.asList(new DateFormatSymbols(Messages.getLocale()).getMonths());
        this.month.setValue(monthNames.get(date.getMonthOfYear() - 1));
        day.setValue(date.getDayOfMonth());
        refreshDay();
    }

    private void loadDataToTable(Soldier[][] soldiers) {
        DistributionRow[] distributionRows = new DistributionRow[12];

        distributionTable.getItems().clear();
        for (int i = 0; i < 12; i++) {
            String startTime = String.format("%02d", i * 2);
            String endTime = String.format("%02d", (i + 1) * 2);
            String hours = startTime + ":00 - " + endTime + ":00";
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
        LocalDate currentDate = getCurrentDate();
        if (currentDate != null) {
            FileChooser fileChooser = new FileChooser();

            FileChooser.ExtensionFilter extFilter
                    = new FileChooser.ExtensionFilter(Messages.get("distribution.excel.file") + " (*.xls)", "*.xls");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle(Messages.get("distribution.save.distribution.as.excel.file"));
            String fileName = currentDate.toString(Constants.DATE_FORMAT) + "-" +
                    Messages.get("distribution.excel.file.name.suffix");
            fileChooser.setInitialFileName(fileName);

            //Show save file dialog
            File file = fileChooser.showSaveDialog(distributionTable.getScene().getWindow());
            if (file != null) {
                if (!file.getName().endsWith(".xls")) file = new File(file.getPath() + ".xls");

                try {
                    HSSFWorkbook workbook = new HSSFWorkbook();


                    HSSFSheet sheet = workbook.createSheet(currentDate.toString(Constants.DATE_FORMAT));

                    HSSFRow firstRow = sheet.createRow((short) 0);
                    ObservableList<TableColumn<DistributionRow, ?>> columns = distributionTable.getColumns();
                    int i = 0;
                    for (TableColumn<DistributionRow, ?> column : columns) {
                        firstRow.createCell(i).setCellValue(column.getText());
                        i++;
                    }

                    int rowNum = 1;
                    for (DistributionRow row : distributionTable.getItems()) {
                        HSSFRow excelRow = sheet.createRow(rowNum);
                        excelRow.createCell(0).setCellValue(row.getHours());
                        i = 1;
                        for (Soldier soldier : row.getSoldiers()) {
                            excelRow.createCell(i).setCellValue(soldier.getFullName());
                            i++;
                        }
                        rowNum++;
                    }

                    FileOutputStream outputStream = new FileOutputStream(file);
                    workbook.write(outputStream);
                    outputStream.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void approveDistribution() {
        LocalDate currentDate = getCurrentDate();
        if (currentDate != null) {
            MonologFXButton.Type result = showApproveConfirmMessage();
            if (result == MonologFXButton.Type.YES) {
                Map<TableColumn<DistributionRow, ?>, WatchPoint> columnWatchPointMap = new HashMap<>();
                ObservableList<TableColumn<DistributionRow, ?>> columns = distributionTable.getColumns();
                for (int i = 1; i < columns.size(); i++) {
                    TableColumn<DistributionRow, ?> column = columns.get(i);

                    String watchPointId = StringUtils.substringBefore(column.getId(), "-");
                    String watchPointSlotId = StringUtils.substringAfter(column.getId(), "-");

                    Optional<WatchPoint> watchPointOptional
                            = DbManager.findWatchPointById(Integer.parseInt(watchPointId));
                    columnWatchPointMap.put(column, watchPointOptional.get());
                }

                ObservableList<DistributionRow> rows = distributionTable.getItems();

                Set<Watch> watchesToBeSaved = new HashSet<>();
                for (int i = 0; i < 12; i++) {
                    Soldier[] soldiers = rows.get(i).getSoldiers();

                    List<WatchPoint> used = new ArrayList<>();
                    for (int j = 0; j < soldiers.length; j++) {
                        // determine watch point
                        WatchPoint watchPoint = columnWatchPointMap.get(columns.get(j + 1));
                        Soldier soldier = soldiers[j];

                        if (soldier != null) {
                            Watch watch = new Watch(soldier, watchPoint, Collections.frequency(used, watchPoint),
                                    currentDate.toString(Constants.DATE_FORMAT), i, WatchValue.of(i));
                            watchesToBeSaved.add(watch);
                        }
                        used.add(watchPoint);
                    }
                }
                DbManager.saveWatchesAddPointsDeleteOldWatches(
                        getCurrentDate(), watchesToBeSaved, WatchRemovalMode.UNDO_POINTS
                );

                showDistributionSuccessMessage();
                mainController.refreshTableData();
            }
        }
    }

    private void showDistributionSuccessMessage() {
        MonologFXButton ok = MonologFXButtonBuilder.create()
                .label(Messages.get("ok"))
                .type(MonologFXButton.Type.YES)
                .defaultButton(true)
                .build();
        MonologFX mono = MonologFXBuilder.create()
                .modal(true)
                .titleText(Messages.get("distribution.success"))
                .message(Messages.get("distribution.success.message"))
                .type(MonologFX.Type.INFO)
                .button(ok)
                .buttonAlignment(MonologFX.ButtonAlignment.RIGHT)
                .build();
        mono.show();
    }

    private MonologFXButton.Type showApproveConfirmMessage() {
        String confirmMessage = buildConfirmMessage();

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
                .titleText(Messages.get("distribution.distribution.approval"))
                .message(confirmMessage)
                .type(MonologFX.Type.QUESTION)
                .button(yes)
                .button(no)
                .buttonAlignment(MonologFX.ButtonAlignment.RIGHT)
                .build();

        return mono.show();
    }

    @SuppressWarnings("unused")
    public void goToToday() {
        setCurrentDate(LocalDate.now());
    }

    private String buildConfirmMessage() {
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


        Comparator<Soldier> soldierComparator = new Comparator<Soldier>() {
            @Override
            public int compare(Soldier o1, Soldier o2) {
                if (o1 == null && o2 == null) return 0;
                if (o1 == null) return 1;
                if (o2 == null) return -1;
                return o1.getFullName().compareTo(o2.getFullName());
            }
        };

        Set<Soldier> firstRowSoldiers = new TreeSet<>(soldierComparator);
        firstRowSoldiers.addAll(Arrays.asList(rows.get(0).getSoldiers()));

        //noinspection ConstantConditions
        List<Watch> previousDaysLastWatches = DbManager.findWatchesByDateAndHour(getCurrentDate().minusDays(1), 11);
        List<Soldier> previousDaysLastWatchesSoldiers
                = Lists.transform(previousDaysLastWatches, new Function<Watch, Soldier>() {
            @Override
            public Soldier apply(Watch input) {
                return input.getSoldier();
            }
        });

        firstRowSoldiers.retainAll(previousDaysLastWatchesSoldiers);
        if (!firstRowSoldiers.isEmpty()) {
            perfect = false;

            String message = Messages.get(firstRowSoldiers.size() > 1 ?
                            "distribution.first.watch.after.yesterdays.last.multiple" :
                            "distribution.first.watch.after.yesterdays.last",
                    Joiner.on(", ").join(firstRowSoldiers));

            approveMessage.append(message)
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }

        for (int i = 1; i < rows.size(); i++) {
            DistributionRow previousRow = rows.get(i - 1);
            DistributionRow currentRow = rows.get(i);

            TreeSet<Soldier> currentRowSoldiers = new TreeSet<>(soldierComparator);
            currentRowSoldiers.addAll(Arrays.asList(currentRow.getSoldiers()));
            currentRowSoldiers.retainAll(Arrays.asList(previousRow.getSoldiers()));

            currentRowSoldiers.remove(null);
            if (!currentRowSoldiers.isEmpty()) {
                perfect = false;
                String previousStartTime = String.format("%02d", (i - 1) * 2);
                String currentStartTime = String.format("%02d", i * 2);
                String currentEndTime = String.format("%02d", (i + 1) * 2);

                String previousHourName = previousStartTime + ":00 - " + currentStartTime + ":00";
                String currentHourName = currentStartTime + ":00 - " + currentEndTime + ":00";

                String message = Messages.get(currentRowSoldiers.size() > 1 ?
                                "distribution.consequent.watches.multiple" :
                                "distribution.consequent.watches",
                        Joiner.on(", ").join(currentRowSoldiers),
                        previousHourName,
                        currentHourName
                );
                approveMessage.append(message)
                        .append(System.lineSeparator())
                        .append(System.lineSeparator());
            }
        }


        List<Watch> existingWatches = DbManager.findWatchesByDate(getCurrentDate());
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

    public Integer getSelectedMonth() {
        if (month.getValue() == null) return null;
        List<String> monthNames = Arrays.asList(new DateFormatSymbols(Messages.getLocale()).getMonths());
        return monthNames.indexOf(month.getValue()) + 1;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        distributionTable.setPlaceholder(new Label(Messages.get("distribution.no.data.in.table")));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<DistributionRow, String>("hours"));
        List<WatchPoint> allActiveWatchPoints = DbManager.findAllActiveWatchPoints();
        List<WatchPointFX> watchPointFXes = Lists.transform(allActiveWatchPoints, Constants.WATCH_POINT_TO_FX);

        final ObservableList<Soldier> soldiers
                = FXCollections.observableArrayList(DbManager.findAllActiveSoldiersOrderedByFullName());

        int num = 0;
        for (WatchPointFX watchPointFX : watchPointFXes) {
            for (int i = 0; i < watchPointFX.requiredSoldierCountProperty().get(); i++) {
                String columnName
                        = watchPointFX.requiredSoldierCountProperty().get() > 1 ?
                        watchPointFX.nameProperty().get() + " - " + (i + 1) : watchPointFX.nameProperty().get();
                TableColumn<DistributionRow, Soldier> column = new TableColumn<>(columnName);
                column.setMinWidth(90);

                final int finalNum = num;
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DistributionRow, Soldier>, ObservableValue<Soldier>>() {
                    @Override
                    public ObservableValue<Soldier> call(TableColumn.CellDataFeatures<DistributionRow, Soldier> distributionRowSoldierCellDataFeatures) {
                        SimpleObjectProperty<Soldier>[] soldiersProperties
                                = distributionRowSoldierCellDataFeatures.getValue().soldiersProperties();
                        if (soldiersProperties.length == 0) return null;
                        return soldiersProperties[finalNum];
                    }
                });

                column.setCellFactory(new Callback<TableColumn<DistributionRow, Soldier>, TableCell<DistributionRow, Soldier>>() {
                    @Override
                    public TableCell<DistributionRow, Soldier> call(TableColumn<DistributionRow, Soldier> distributionRowSoldierTableColumn) {
                        final ComboBoxTableCell<DistributionRow, Soldier> cell = new ComboBoxTableCell<>();
                        cell.editingProperty().addListener(new ChangeListener<Boolean>() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                                if (t1) {
                                    TableRow<DistributionRow> tableRow = cell.getTableRow();
                                    DistributionRow distributionRow = tableRow.getItem();
                                    selectedSoldiersBeforeEdit = Arrays.copyOf(distributionRow.getSoldiers(), distributionRow.getSoldiers().length);
                                }
                            }
                        });


                        cell.itemProperty().addListener(new ChangeListener<Soldier>() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public void changed(ObservableValue<? extends Soldier> observableValue, Soldier soldier, Soldier t1) {
                                TableRow<DistributionRow> r = cell.getTableRow();
                                DistributionRow row = r.getItem();
                                if (row != null) {
                                    List<Soldier> rowSoldiers = Arrays.asList(row.getSoldiers());
                                    if (Collections.frequency(rowSoldiers, t1) > 1) {
                                        MonologFXButton ok = MonologFXButtonBuilder.create()
                                                .label(Messages.get("undo"))
                                                .defaultButton(true)
                                                .type(MonologFXButton.Type.OK).build();

                                        MonologFX mono = MonologFXBuilder.create()
                                                .modal(true)
                                                .titleText(Messages.get("error"))
                                                .message(Messages.get("distribution.soldier.already.has.watch.select.another"))
                                                .type(MonologFX.Type.ERROR)
                                                .button(ok)
                                                .buttonAlignment(MonologFX.ButtonAlignment.RIGHT)
                                                .build();
                                        mono.show();

                                        row.setSoldiers(selectedSoldiersBeforeEdit);
                                        cell.setItem(soldier);
                                    }
                                }
                            }
                        });
                        cell.getItems().addAll(soldiers);
                        return cell;
                    }
                });

                column.setId(String.valueOf(watchPointFX.idProperty().get() + "-" + i));
                distributionTable.getColumns().add(column);
                num++;
            }
        }

        initDateFelds();
    }

    private void initDateFelds() {
        final List<Integer> years = new ArrayList<>();
        for (int i = 2015; i < 2050; i++) {
            years.add(i);
        }

        year.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer ınteger, Integer t1) {
                if (day.getValue() != null && month.getValue() != null) {
                    refreshDay();
                    refreshData();
                }
            }
        });
        month.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (day.getValue() != null && year.getValue() != null) {
                    refreshDay();
                    refreshData();
                }
            }
        });
        day.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer ınteger, Integer t1) {
                if (month.getValue() != null && year.getValue() != null) {
                    refreshDayName();
                    refreshData();
                }
            }
        });

        year.setItems(FXCollections.observableArrayList(years));
        month.setItems(FXCollections.observableArrayList(
                        Arrays.asList(new DateFormatSymbols(Messages.getLocale()).getMonths()))
        );
        setCurrentDate(LocalDate.now());
    }

    private void refreshDay() {
        Integer originalValue = day.getValue();
        DateTime temp = new DateTime(year.getValue(), getSelectedMonth(), 1, 0, 0);
        int lastDay = temp.dayOfMonth().getMaximumValue();
        List<Integer> days = new ArrayList<>();
        for (int i = 1; i <= lastDay; i++) {
            days.add(i);
        }
        day.setItems(FXCollections.observableArrayList(days));
        if (day.getValue() == null) {
            day.setValue(1);
            return;
        }
        if (lastDay < day.getValue()) day.setValue(lastDay);
        else day.setValue(originalValue);
        refreshDayName();
    }

    private void refreshDayName() {
        LocalDate watchDate = getCurrentDate();
        if (watchDate != null) {
            String dayName = new SimpleDateFormat("EEEE", Messages.getLocale()).format(watchDate.toDate());
            this.dayName.setText(dayName);
        }
    }

    private void refreshData() {
        LocalDate date = getCurrentDate();
        if (date != null) {
            List<Watch> watches = DbManager.findWatchesByDate(date);
            int soldierCount = WatchPointUtil.getTotalWatchPointSoldierCount();
            Soldier[][] soldiers = new Soldier[12][soldierCount];
            for (Watch watch : watches) {
                for (TableColumn<DistributionRow, ?> column : distributionTable.getColumns()) {
                    String watchPointId = StringUtils.substringBefore(column.getId(), "-");
                    String watchPointSlot = StringUtils.substringAfter(column.getId(), "-");

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
                        soldiers[watch.getHour()][distributionTable.getColumns().indexOf(column) - 1]
                                = watch.getSoldier();
                    }
                }
            }
            loadDataToTable(soldiers);
        }
    }
}