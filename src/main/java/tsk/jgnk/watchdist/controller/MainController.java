package tsk.jgnk.watchdist.controller;

import com.google.common.collect.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
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

import static tsk.jgnk.watchdist.util.Converters.*;

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

	public void refreshTableData() {
		List<Soldier> allActiveSoldiers = DbManager.findAllActiveSoldiers();
		List<SoldierFX> soldierFXes = Lists.transform(allActiveSoldiers, SOLDIER_TO_FX);
		ObservableList<SoldierFX> data = FXCollections.observableArrayList(soldierFXes);
		soldiersTable.setItems(data);
	}

	public void scrollToLastElementInTable() {
		ObservableList<SoldierFX> items = soldiersTable.getItems();
		if (!items.isEmpty()) {
			soldiersTable.scrollTo(items.size() - 1);
		}
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
			for (int j = 0; j < Constants.TOTAL_WATCHES_IN_DAY; j++) {
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

		fullNameColumn.setCellFactory(soldierStringTableColumn -> new TextFieldTableCell<>(STRING_STRING_CONVERTER));
		fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

		dutyColumn.setCellFactory(soldierStringTableColumn -> new TextFieldTableCell<>(STRING_STRING_CONVERTER));
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

		pointsColumn.setCellFactory(soldierDoubleTableColumn -> new TextFieldTableCell<>(DOUBLE_STRING_CONVERTER));
		pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));

		maxWatchCountPerDayColumn.setText(Messages.get("max.watch.count.per.day", Constants.WATCH_DURATION_IN_HOURS));
		maxWatchCountPerDayColumn.setCellFactory(column -> {
			ComboBoxTableCell<SoldierFX, Integer> cell = new ComboBoxTableCell<>(DOUBLE_INTEGER_CONVERTER);
			cell.getItems().setAll(FXCollections.observableArrayList(
							ContiguousSet.create(Range.closed(1, Constants.TOTAL_WATCHES_IN_DAY), DiscreteDomain.integers()))
			);

			return cell;
		});
		makeHeaderWrappable(maxWatchCountPerDayColumn);
		maxWatchCountPerDayColumn.setCellValueFactory(new PropertyValueFactory<>("maxWatchesPerDay"));
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
		WindowManager.showWatchDistributionWindow(this);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		language.setItems(FXCollections.observableArrayList(Arrays.asList(Language.values())));
		language.setValue(Language.forLocale(Messages.getLocale()));
		language.valueProperty().addListener((observableValue, language1, t1) -> {
			WindowManager.switchLanguage(((Stage) language.getScene().getWindow()), t1);
		});
		initializeTable();
		refreshTableData();
	}

	public void deleteSelectedSoldiers() {
		ObservableList<SoldierFX> selectedItems = soldiersTable.getSelectionModel().getSelectedItems();
		List<Soldier> soldiers = Lists.transform(selectedItems, FX_TO_SOLDIER);

		List<SoldierFX> filtered = Lists.newCopyOnWriteArrayList(
				Iterables.filter(selectedItems, soldier -> soldier != null));

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
		WindowManager.showAdministrationWindow(this);
	}
}
