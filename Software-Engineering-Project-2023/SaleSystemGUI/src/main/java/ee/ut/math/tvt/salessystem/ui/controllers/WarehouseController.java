package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WarehouseController implements Initializable {

    private static final Logger log = LogManager.getLogger(WarehouseController.class);
    private final SalesSystemDAO dao;

    @FXML
    private Button addItem;
    @FXML
    public TextField barCodeField;
    @FXML
    public TextField nameField;
    @FXML
    public TextField descriptionField;
    @FXML
    public TextField priceField;
    @FXML
    public TextField quantityField;
    @FXML
    private TextField searchField;
    @FXML
    public TableView<StockItem> warehouseTableView;

    public WarehouseController(SalesSystemDAO dao) {
        this.dao = dao;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.debug("Initializing WarehouseController");
        refreshStockItems();
    }

    @FXML
    public void refreshButtonClicked() {
        log.debug("Refresh button clicked");
        refreshStockItems();
    }

    @FXML
    public void addItemButtonClicked() {
        log.debug("Add item button clicked");
        StockItem item = new StockItem(Long.parseLong(barCodeField.getText()), nameField.getText(), descriptionField.getText(), Double.parseDouble(priceField.getText()), Integer.parseInt(quantityField.getText()));
        dao.saveStockItem(item);
        warehouseTableView.refresh();
    }

    @FXML
    public void removeItemButtonClicked() {
        log.debug("Remove item button clicked");
        // Check if there are items in the warehouse
        if (!dao.findStockItems().isEmpty()) {
            // Get the last item from the warehouse
            StockItem lastItem = dao.findStockItems().get(dao.findStockItems().size() - 1);
            // Remove the last item from the warehouse
            dao.removeStockItem(lastItem);
            // Refresh the table view
            warehouseTableView.refresh();
        } else {
            log.error("No items in the warehouse to remove");
        }
    }
    @FXML
    public void searchButtonClicked() {
        log.debug("Search button clicked");
        long id = Long.parseLong(searchField.getText());
        List<StockItem> items = new ArrayList<StockItem>();
        items.add(dao.findStockItem(id));
        warehouseTableView.setItems(FXCollections.observableList(items));
    }

    private void refreshStockItems() {
        log.debug("Refreshing stock items");
        warehouseTableView.setItems(FXCollections.observableList(dao.findStockItems()));
        warehouseTableView.refresh();
    }
}
