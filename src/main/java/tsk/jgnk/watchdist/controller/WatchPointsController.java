package tsk.jgnk.watchdist.controller;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tsk.jgnk.watchdist.fx.WatchPointFX;
import tsk.jgnk.watchdist.i18n.Messages;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.WindowManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static tsk.jgnk.watchdist.util.Converters.*;

@SuppressWarnings("unused")
public class WatchPointsController implements Initializable {
    @FXML
    private TableView<WatchPointFX> watchPointsTable;
    @FXML
    private TableColumn<WatchPointFX, Integer> idColumn;
    @FXML
    private TableColumn<WatchPointFX, String> nameColumn;
    @FXML
    private TableColumn<WatchPointFX, Integer> requiredSoldierCountColumn;

    @FXML
    private Button addWatchPointButton;
    @FXML
    private Button removeSelectedWatchPointsButton;

    public void addWatchPoint() {
        showAddNewWatchPointWindow();
    }

    private void showAddNewWatchPointWindow() {
        WindowManager.showAddNewWatchPointWindow();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        watchPointsTable.setPlaceholder(new Label(Messages.get("watchpoints.no.watch.points")));
        watchPointsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		nameColumn.setCellFactory(watchPointStringTableColumn -> new TextFieldTableCell<>(STRING_STRING_CONVERTER));

        requiredSoldierCountColumn
                .setCellFactory(watchPointIntegerTableColumn -> new TextFieldTableCell<>(new StringConverter<Integer>() {
                    @Override
                    public String toString(Integer integer) {
                        return Integer.toString(integer);
                    }

                    @Override
                    public Integer fromString(String s) {
                        try {
                            return Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            return -1;
                        }
                    }
                }));

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        requiredSoldierCountColumn.setCellValueFactory(new PropertyValueFactory<>("requiredSoldierCount"));

        refreshTableData();
    }

    public void removeSelectedWatchPoints() {
        ObservableList<WatchPointFX> selectedItems = watchPointsTable.getSelectionModel().getSelectedItems();
        List<WatchPointFX> filtered = Lists.newCopyOnWriteArrayList(
                Iterables.filter(selectedItems, input -> input != null));

        if (!filtered.isEmpty()) {
			boolean approved = WindowManager.showWarningConfirmationAlert(
					Messages.get("watchpoints.watch.point.removal.approval"),
					Messages.get("watchpoints.watch.point.removal.approval.message", filtered.size()),
					Messages.get("delete.selected.watch.points"),
					Messages.get("cancel")
			);

			if (approved) {
				DbManager.deleteWatchPoints(Lists.transform(filtered, FX_TO_WATCH_POINT));
				refreshTableData();
				watchPointsTable.getSelectionModel().clearSelection();
            }
        }
    }

    public void refreshTableData() {
        List<WatchPointFX> watchPointFXes
				= Lists.transform(DbManager.findAllActiveWatchPoints(), WATCH_POINT_TO_FX);
		ObservableList<WatchPointFX> items = FXCollections.observableArrayList(watchPointFXes);
		watchPointsTable.setItems(items);
    }

	public void scrollToLastElementInTable() {
		ObservableList<WatchPointFX> items = watchPointsTable.getItems();
		if (!items.isEmpty()) {
			watchPointsTable.scrollTo(items.size() - 1);
		}
	}

    public void closeWindow() {
        ((Stage) addWatchPointButton.getScene().getWindow()).close();
    }
}
