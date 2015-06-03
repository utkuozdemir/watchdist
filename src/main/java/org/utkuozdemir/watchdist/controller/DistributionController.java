package org.utkuozdemir.watchdist.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.utkuozdemir.watchdist.domain.NullSoldier;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.domain.Watch;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.fx.WatchPointFX;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.util.Comparators;
import org.utkuozdemir.watchdist.util.*;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.utkuozdemir.watchdist.Constants.*;

@SuppressWarnings("unused")
public class DistributionController implements Initializable {
	@FXML
	private TableView<DistributionRow> distributionTable;
	@FXML
	private TableColumn<DistributionRow, String> hoursColumn;
	@FXML
	private ComboBox<Integer> day;
	@FXML
	private ComboBox<String> month;
	@FXML
	private ComboBox<Integer> year;
	@FXML
	private Label dayName;


	private MainController mainController;
	private Soldier[] selectedSoldiersBeforeEdit;

	public DistributionController(MainController mainController) {
		this.mainController = mainController;
		checkNotNull(mainController);
	}

	@SuppressWarnings("unused")
	public void distribute() {
		Soldier[][] soldiers = DistributionEngine.createDistribution(getCurrentDate());
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
		DistributionRow[] distributionRows = new DistributionRow[TOTAL_WATCHES_IN_DAY];

		distributionTable.getItems().clear();
		for (int i = 0; i < TOTAL_WATCHES_IN_DAY; i++) {
			String startTime = String.format("%02d", i * 2);
			String endTime = String.format("%02d", ((i + 1) % TOTAL_WATCHES_IN_DAY) * 2);
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
			List<Watch> watches = DbManager.findWatchesByDate(getCurrentDate());
			if (watches.isEmpty()) {
				WindowManager.showWarningInfoAlert(Messages.get("distribution.approve.before.export.title"),
						Messages.get("distribution.approve.before.export.message"));
			} else {
				FileChooser fileChooser = new FileChooser();

				FileChooser.ExtensionFilter extFilter
						= new FileChooser.ExtensionFilter(Messages.get("distribution.excel.file") + " (*.xls)", "*.xls");
				fileChooser.getExtensionFilters().add(extFilter);
				fileChooser.setTitle(Messages.get("distribution.save.distribution.as.excel.file"));
				String fileName = currentDate.toString(DATE_FORMAT) + "-" +
						Messages.get("distribution.excel.file.name.suffix");
				fileChooser.setInitialFileName(fileName);

				File file = fileChooser.showSaveDialog(distributionTable.getScene().getWindow());
				if (file != null) {
					try {
						if (!file.getName().endsWith(".xls")) file = new File(file.getPath() + ".xls");
						ExcelExporter.export(file, currentDate, watches);
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
		LocalDate currentDate = getCurrentDate();
		if (currentDate != null) {
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

					String watchPointId = StringUtils.substringBefore(column.getId(), "-");
					String watchPointSlotId = StringUtils.substringAfter(column.getId(), "-");

					Optional<WatchPoint> watchPointOptional
							= DbManager.findWatchPointById(Integer.parseInt(watchPointId));
					columnWatchPointMap.put(column, watchPointOptional.get());
				}

				ObservableList<DistributionRow> rows = distributionTable.getItems();

				Set<Watch> watchesToBeSaved = new HashSet<>();
				for (int i = 0; i < TOTAL_WATCHES_IN_DAY; i++) {
					Soldier[] soldiers = rows.get(i).getSoldiers();

					List<WatchPoint> used = new ArrayList<>();
					for (int j = 0; j < soldiers.length; j++) {
						// determine watch point
						WatchPoint watchPoint = columnWatchPointMap.get(columns.get(j + 1));
						Soldier soldier = soldiers[j];

						if (soldier instanceof NullSoldier) soldier = null;
						Watch watch = new Watch(soldier, watchPoint, Collections.frequency(used, watchPoint),
								currentDate.toString(DATE_FORMAT), i, WatchValues.get(i));
						watchesToBeSaved.add(watch);

						used.add(watchPoint);
					}
				}
				DbManager.saveWatchesAddPointsDeleteOldWatches(
						getCurrentDate(), watchesToBeSaved
				);

				WindowManager.showInfoAlert(Messages.get("success"), Messages.get("distribution.success.message"));
				mainController.refreshTableData();
			}
		}
	}

	@SuppressWarnings("unused")
	public void goToToday() {
		setCurrentDate(LocalDate.now());
	}

	@SuppressWarnings("unused")
	public void jumpToPreviousDay() {
		LocalDate currentDate = getCurrentDate();
		if (currentDate != null) {
			setCurrentDate(currentDate.minusDays(1));
		}

	}

	@SuppressWarnings("unused")
	public void jumpToNextDay() {
		LocalDate currentDate = getCurrentDate();
		if (currentDate != null) {
			setCurrentDate(currentDate.plusDays(1));
		}
	}

	private String buildConfirmMessage() {
		LocalDate currentDate = getCurrentDate();
		if (currentDate == null) throw new RuntimeException("Current date is null!");

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

		Comparator<Soldier> soldierComparator = (o1, o2) -> {
			if (o1 == null && o2 == null) return 0;
			if (o1 == null) return 1;
			if (o2 == null) return -1;
			return o1.getFullName().compareTo(o2.getFullName());
		};

//		Set<Soldier> firstRowSoldiers = new TreeSet<>(soldierComparator);
//		firstRowSoldiers.addAll(Arrays.asList(rows.get(0).getSoldiers()));
//
//		Set<Soldier> secondRowSoldiers = new TreeSet<>(soldierComparator);
//		secondRowSoldiers.addAll(Arrays.asList(rows.get(1).getSoldiers()));
//
//		//noinspection ConstantConditions
//		List<Watch> previousDays22_24Watches = DbManager.findWatchesByDateAndHour(getCurrentDate().minusDays(1), 11);
//		List<Soldier> previousDays22_24WatchesSoldiers
//				= Lists.transform(previousDays22_24Watches, Watch::getSoldier);
//		previousDays22_24WatchesSoldiers.removeAll(Collections.<Soldier>singleton(null));

//		//noinspection ConstantConditions
//		List<Watch> previousDays20_22watches = DbManager.findWatchesByDateAndHour(getCurrentDate().minusDays(1), 10);
//		List<Soldier> previousDays20_22watchesSoldiers
//				= Lists.transform(previousDays20_22watches, Watch::getSoldier);
//		previousDays20_22watchesSoldiers.removeAll(Collections.<Soldier>singleton(null));
//
//		firstRowSoldiers.retainAll(previousDays22_24WatchesSoldiers);
//		secondRowSoldiers.retainAll(pre)
//		if (!firstRowSoldiers.isEmpty()) {
//			perfect = false;
//
//			String message = Messages.get(firstRowSoldiers.size() > 1 ?
//							"distribution.first.watch.after.yesterdays.last.multiple" :
//							"distribution.first.watch.after.yesterdays.last",
//					Joiner.on(", ").join(firstRowSoldiers));
//
//			approveMessage.append(message)
//					.append(System.lineSeparator())
//					.append(System.lineSeparator());
//		}

		for (int i = 0; i < rows.size(); i++) {
			List<Soldier> previousFirstRowSoldiers;
			List<Soldier> previousSecondRowSoldiers;
			if (i == 0) {
				previousFirstRowSoldiers = DbManager.findWatchesByDateAndHour(currentDate.minusDays(1), 11)
						.stream().map(Watch::getSoldier).collect(Collectors.toList());
				previousSecondRowSoldiers = DbManager.findWatchesByDateAndHour(currentDate.minusDays(1), 10)
						.stream().map(Watch::getSoldier).collect(Collectors.toList());
			} else if (i == 1) {
				previousFirstRowSoldiers = Arrays.asList(rows.get(i - 1).getSoldiers());
				previousSecondRowSoldiers = DbManager.findWatchesByDateAndHour(currentDate.minusDays(1), 11)
						.stream().map(Watch::getSoldier).collect(Collectors.toList());
			} else {
				previousFirstRowSoldiers = Arrays.asList(rows.get(i - 1).getSoldiers());
				previousSecondRowSoldiers = Arrays.asList(rows.get(i - 2).getSoldiers());
			}


			TreeSet<Soldier> currentRowSoldiers = new TreeSet<>(soldierComparator);
			currentRowSoldiers.addAll(Arrays.asList(rows.get(i).getSoldiers()));
			currentRowSoldiers.retainAll(
					Stream.concat(previousFirstRowSoldiers.stream(), previousSecondRowSoldiers.stream())
							.collect(Collectors.toList()));

			currentRowSoldiers.remove(null);
			if (!currentRowSoldiers.isEmpty()) {
				perfect = false;
				String previousStartTime = String.format("%02d",
						((i + (TOTAL_WATCHES_IN_DAY - 1)) % TOTAL_WATCHES_IN_DAY) * 2);
				String currentStartTime = String.format("%02d",
						((i + TOTAL_WATCHES_IN_DAY) % TOTAL_WATCHES_IN_DAY) * 2
				);
				String currentEndTime = String.format("%02d", ((i + 1) % TOTAL_WATCHES_IN_DAY) * 2);

				String previousHourName = previousStartTime + ":00 - " + currentStartTime + ":00";
				String currentHourName = currentStartTime + ":00 - " + currentEndTime + ":00";

				String message = Messages.get(currentRowSoldiers.size() > 1 ?
								"distribution.consequent.watches.multiple" :
								"distribution.consequent.watches",
						Joiner.on(", ").join(currentRowSoldiers),
						previousHourName,
						currentHourName,
						(MIN_WATCHES_BETWEEN_TWO_WATCHES * WATCH_DURATION_IN_HOURS) + WATCH_DURATION_IN_HOURS
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
		hoursColumn.setCellValueFactory(new PropertyValueFactory<>("hours"));

		refreshTableColumns();

		initDateFields();
	}

	@SuppressWarnings("unchecked")
	private void refreshTableColumns() {
		Iterator<TableColumn<DistributionRow, ?>> columnIterator = distributionTable.getColumns().iterator();
		while (columnIterator.hasNext()) {
			TableColumn<DistributionRow, ?> column = columnIterator.next();
			if (column != hoursColumn) columnIterator.remove();
		}

		List<WatchPoint> watchPoints;
		LocalDate currentDate = getCurrentDate();
		List<Watch> watches = DbManager.findWatchesByDate(currentDate != null ? currentDate : LocalDate.now());
		if (watches.isEmpty()) {
			watchPoints = DbManager.findAllActiveWatchPoints();
		} else {
			watchPoints = Lists.transform(watches, input -> {
				if (input == null) return null;
				return input.getWatchPoint();
			});

			watchPoints.removeAll(Collections.<WatchPoint>singleton(null));
			Set<WatchPoint> dupesRemoved = new TreeSet<>(Comparators.WATCH_POINT_ID_ASC_COMPARATOR);
			dupesRemoved.addAll(watchPoints);
			watchPoints = new ArrayList<>(dupesRemoved);
		}

		List<WatchPointFX> watchPointFXes = Lists.transform(watchPoints, Converters.WATCH_POINT_TO_FX);

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
				column.setCellValueFactory(distributionRowSoldierCellDataFeatures -> {
					SimpleObjectProperty<Soldier>[] soldiersProperties
							= distributionRowSoldierCellDataFeatures.getValue().soldiersProperties();
					if (soldiersProperties.length == 0) return null;
					return soldiersProperties[finalNum];
				});

				column.setCellFactory(distributionRowSoldierTableColumn -> {
					final ComboBoxTableCell<DistributionRow, Soldier> cell = new ComboBoxTableCell<>();
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

	private void initDateFields() {
		final List<Integer> years = new ArrayList<>();
		for (int i = 2015; i < 2050; i++) {
			years.add(i);
		}

		year.valueProperty().addListener((observableValue, integer, t1) -> {
			if (day.getValue() != null && month.getValue() != null) {
				refreshDay();
				refreshData();
			}
		});
		month.valueProperty().addListener((observableValue, s, t1) -> {
			if (day.getValue() != null && year.getValue() != null) {
				refreshDay();
				refreshData();
			}
		});
		day.valueProperty().addListener((observableValue, integer, t1) -> {
			if (month.getValue() != null && year.getValue() != null) {
				refreshDayName();
				refreshData();
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
			refreshTableColumns();
			List<Watch> watches = DbManager.findWatchesByDate(date);

			List<WatchPoint> watchPoints;
			if (watches.isEmpty()) {
				watchPoints = DbManager.findAllActiveWatchPoints();
			} else {
				watchPoints = Lists.transform(watches, input -> input == null ? null : input.getWatchPoint());
			}
			watchPoints.removeAll(Collections.<WatchPoint>singleton(null));

			TreeSet<WatchPoint> dupesRemovedSortedWatchPoints = new TreeSet<>(Comparators.WATCH_POINT_ID_ASC_COMPARATOR);
			dupesRemovedSortedWatchPoints.addAll(watchPoints);

			int soldierCount = WatchPointSoldierCalculator
					.getTotalWatchPointSoldierCount(dupesRemovedSortedWatchPoints);
			Soldier[][] soldiers = new Soldier[TOTAL_WATCHES_IN_DAY][soldierCount];
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
						int i = watch.getHour();
						int j = distributionTable.getColumns().indexOf(column) - 1;
						soldiers[i][j] = watch.getSoldier();
					}
				}
			}
			loadDataToTable(soldiers);
		}
	}
}