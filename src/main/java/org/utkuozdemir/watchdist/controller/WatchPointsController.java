package org.utkuozdemir.watchdist.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.utkuozdemir.watchdist.fx.WatchPointFX;
import org.utkuozdemir.watchdist.i18n.Messages;
import org.utkuozdemir.watchdist.util.Converters;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WindowManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.utkuozdemir.watchdist.app.Constants.SERIALIZED_MIME_TYPE;

@SuppressWarnings("unused")
public class WatchPointsController implements Initializable {
	@FXML
	private TableView<WatchPointFX> watchPointsTable;
	@FXML
	private TableColumn<WatchPointFX, String> nameColumn;
	@FXML
	private TableColumn<WatchPointFX, Integer> requiredSoldierCountColumn;
	@FXML
	private TableColumn<WatchPointFX, Integer> orderColumn;

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
		watchPointsTable.setRowFactory(wpfx -> buildDraggableTableRow());

		orderColumn.prefWidthProperty().bind(watchPointsTable.widthProperty().multiply(0.15));
		nameColumn.prefWidthProperty().bind(watchPointsTable.widthProperty().multiply(0.6));
		requiredSoldierCountColumn.prefWidthProperty().bind(watchPointsTable.widthProperty().multiply(0.24));

		nameColumn.setCellFactory(watchPointStringTableColumn -> new TextFieldTableCell<>(Converters.STRING_STRING_CONVERTER));
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
        requiredSoldierCountColumn
                .setCellFactory(c -> {
                    ComboBoxTableCell<WatchPointFX, Integer> cell = new ComboBoxTableCell<>();
                    cell.getItems().setAll(IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList()));
                    return cell;
                });

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        requiredSoldierCountColumn.setCellValueFactory(new PropertyValueFactory<>("requiredSoldierCount"));
		orderColumn.setCellValueFactory(new PropertyValueFactory<>("order"));

        refreshTableData();
    }

	private TableRow<WatchPointFX> buildDraggableTableRow() {
		TableRow<WatchPointFX> row = new TableRow<>();

		row.setOnDragDetected(event -> {
			if (!row.isEmpty()) {
				Integer index = row.getIndex();
				Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
				db.setDragView(row.snapshot(null, null));
				IntStream.range(0, watchPointsTable.getItems().size())
						.filter(i -> i != index).forEach(i -> watchPointsTable.getSelectionModel().clearSelection(i));
				ClipboardContent cc = new ClipboardContent();
				cc.put(SERIALIZED_MIME_TYPE, index);
				db.setContent(cc);
				event.consume();
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
				WatchPointFX dragged = watchPointsTable.getItems().remove(draggedIndex);

				int dropIndex;
				if (row.isEmpty()) {
					dropIndex = watchPointsTable.getItems().size();
				} else {
					dropIndex = row.getIndex();
				}

				watchPointsTable.getItems().add(dropIndex, dragged);

				event.setDropCompleted(true);
				watchPointsTable.getSelectionModel().clearSelection();
				watchPointsTable.getSelectionModel().select(dropIndex);
				saveOrders();
				event.consume();
			}
		});
		return row;
	}

	public void removeSelectedWatchPoints() {
		ObservableList<WatchPointFX> selectedItems = watchPointsTable.getSelectionModel().getSelectedItems();
        List<WatchPointFX> filtered = selectedItems.stream().filter(wp -> wp != null).collect(Collectors.toList());

        if (!filtered.isEmpty()) {
			boolean approved = WindowManager.showWarningConfirmationAlert(
					Messages.get("watchpoints.watch.point.removal.approval"),
					Messages.get("watchpoints.watch.point.removal.approval.message", filtered.size()),
					Messages.get("delete.selected.watch.points"),
					Messages.get("cancel")
			);

			if (approved) {
                DbManager.deleteWatchPoints(
                        filtered.stream().map(Converters.FX_TO_WATCH_POINT).collect(Collectors.toList())
                );
                refreshTableData();
				watchPointsTable.getSelectionModel().clearSelection();
            }
        }
    }

    public void refreshTableData() {
        List<WatchPointFX> watchPointFXes
				= DbManager.findAllActiveWatchPointsOrdered().stream()
				.map(Converters.WATCH_POINT_TO_FX).collect(Collectors.toList());
        ObservableList<WatchPointFX> items = FXCollections.observableArrayList(watchPointFXes);
		watchPointsTable.setItems(items);
    }

	public void saveOrders() {
		IntStream.range(0, watchPointsTable.getItems().size()).forEach(i ->
				watchPointsTable.getItems().get(i).orderProperty().set(i + 1));
	}

	public void scrollToLastElementInTable() {
		ObservableList<WatchPointFX> items = watchPointsTable.getItems();
		if (!items.isEmpty()) {
			watchPointsTable.scrollTo(items.size() - 1);
		}
	}

	public int getTableItemsSize() {
		return watchPointsTable.getItems().size();
	}

    public void closeWindow() {
        ((Stage) addWatchPointButton.getScene().getWindow()).close();
    }

}
