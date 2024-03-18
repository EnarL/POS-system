package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.util.ResourceBundle;




/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "Point-of-sale" in the menu). Consists of the purchase menu,
 * current purchase dialog and shopping cart table.
 */
public class PurchaseController implements Initializable {

    private static final Logger log = LogManager.getLogger(PurchaseController.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart shoppingCart;

    @FXML
    private Button newPurchase;
    @FXML
    private Button submitPurchase;
    @FXML
    private Button cancelPurchase;
    @FXML
    private TextField barCodeField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private Button addItemButton;
    @FXML
    private Button removeItemButton;
    @FXML
    private TableView<SoldItem> purchaseTableView;
    @FXML
    private Label totalCostLabel;

    public PurchaseController(SalesSystemDAO dao, ShoppingCart shoppingCart) {
        this.dao = dao;
        this.shoppingCart = shoppingCart;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cancelPurchase.setDisable(true);
        submitPurchase.setDisable(true);
        purchaseTableView.setItems(FXCollections.observableList(shoppingCart.getAll()));
        disableProductField(true);

        this.barCodeField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (!newPropertyValue) {
                    fillInputsBySelectedStockItem();
                }
            }
        });
    }

    /** Event handler for the <code>new purchase</code> event. */
    @FXML
    protected void newPurchaseButtonClicked() {
        log.info("New sale process started");
        try {
            enableInputs();
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Event handler for the <code>cancel purchase</code> event.
     */
    @FXML
    protected void cancelPurchaseButtonClicked() {
        log.info("Sale cancelled");
        try {
            for (int i = 0; i < shoppingCart.getAll().size(); i++) {
                StockItem stockItem = shoppingCart.getAll().get(i).getStockItem();
                stockItem.setQuantity(stockItem.getQuantity() + shoppingCart.getAll().get(i).getQuantity());
            }
            shoppingCart.cancelCurrentPurchase();
            disableInputs();
            purchaseTableView.refresh();
            totalCostLabel.setText("0.0");
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Event handler for the <code>submit purchase</code> event.
     */
    @FXML
    protected void submitPurchaseButtonClicked() {
        log.info("Sale complete");
        try {
            log.debug("Contents of the current basket:\n" + shoppingCart.getAll());
            shoppingCart.submitCurrentPurchase();
            dao.savePurchase(shoppingCart.getAll()); // Save the purchased items
            disableInputs();
            purchaseTableView.refresh();
            totalCostLabel.setText("0.0");
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    // switch UI to the state that allows to proceed with the purchase
    private void enableInputs() {
        resetProductField();
        disableProductField(false);
        cancelPurchase.setDisable(false);
        submitPurchase.setDisable(false);
        newPurchase.setDisable(true);
    }

    // switch UI to the state that allows to initiate new purchase
    private void disableInputs() {
        resetProductField();
        cancelPurchase.setDisable(true);
        submitPurchase.setDisable(true);
        newPurchase.setDisable(false);
        disableProductField(true);
    }

    private void fillInputsBySelectedStockItem() {
        StockItem stockItem = getStockItemByBarcode();
        if (stockItem != null) {
            nameField.setText(stockItem.getName());
            priceField.setText(String.valueOf(stockItem.getPrice()));
        } else {
            resetProductField();
        }
    }

    // Search the warehouse for a StockItem with the bar code entered
    // to the barCode textfield.
    private StockItem getStockItemByBarcode() {
        try {
            long code = Long.parseLong(barCodeField.getText());
            return dao.findStockItem(code);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Finds the SoldItem in the shopping cart that corresponds to the StockItem
    private SoldItem findSoldItem(StockItem item) {
        for (SoldItem soldItem : shoppingCart.getAll()) {
            if (soldItem.getStockItem().getId().equals(item.getId())) {
                return soldItem;
            }
        }
        return null;
    }

    /**
     * Add new item to the cart.
     */
    @FXML
    public void addItemEventHandler() throws Exception {
        // add chosen item to the shopping cart.
        StockItem stockItem = getStockItemByBarcode();
        if (stockItem != null) {
            int quantity;
            try {
                quantity = Integer.parseInt(quantityField.getText());
            } catch (NumberFormatException e) {
                quantity = 1;
            }
            // Check if the item already exists in the shopping cart
            SoldItem existingItem = findSoldItem(stockItem);
            if (existingItem != null) {
                // Check if max quantity exceeded
                int maxQuantity = stockItem.getQuantity() + existingItem.getQuantity();
                if (maxQuantity < quantity || quantity < 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setContentText("Product is not available in the inserted quantities.");
                    alert.showAndWait();
                }
                // If the item already exists in the shopping cart, remove it and add the new item
                stockItem.setQuantity(stockItem.getQuantity()+existingItem.getQuantity());
                shoppingCart.removeItem(existingItem);
                SoldItem soldItem = new SoldItem(stockItem, quantity);
                stockItem.setQuantity(stockItem.getQuantity() - quantity);
                shoppingCart.addItem(soldItem);
                purchaseTableView.refresh();
                setTotalCostLabel();
            } else {
                // Check if max quantity exceeded
                int maxQuantity = stockItem.getQuantity();
                if (maxQuantity < quantity || quantity < 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setContentText("Product is not available in the inserted quantities.");
                    alert.showAndWait();
                }
                // Add the new item
                SoldItem soldItem = new SoldItem(stockItem, quantity);
                shoppingCart.addItem(soldItem);
                purchaseTableView.refresh();
                setTotalCostLabel();
            }
        }
    }

    @FXML
    public void removeItemEventHandler() {
        if (!shoppingCart.getAll().isEmpty()) {
            // Get the last item from the shopping cart
            SoldItem lastItem = shoppingCart.getAll().get(shoppingCart.getAll().size() - 1);
            StockItem stockItem = shoppingCart.getAll().get(shoppingCart.getAll().size() - 1).getStockItem();
            stockItem.setQuantity(stockItem.getQuantity() + lastItem.getQuantity() +1);
            // Remove the last item from the shopping cart
            shoppingCart.removeItem(lastItem);
            // Refresh the table view
            purchaseTableView.refresh();
            setTotalCostLabel();
        }
    }
    @FXML
    public void setTotalCostLabel() {
        double total = 0;
        for (int i = 0; i < shoppingCart.getAll().size(); i++) {
            SoldItem item = shoppingCart.getAll().get(i);
            double price = item.getPrice();
            int quantity = item.getQuantity();
            total += quantity * price;
        }
        totalCostLabel.setText(Double.toString(total));
    }

    /**
     * Sets whether or not the product component is enabled.
     */
    private void disableProductField(boolean disable) {
        this.addItemButton.setDisable(disable);
        this.removeItemButton.setDisable(disable);
        this.barCodeField.setDisable(disable);
        this.quantityField.setDisable(disable);
        this.nameField.setDisable(disable);
        this.priceField.setDisable(disable);
    }

    /**
     * Reset dialog fields.
     */
    private void resetProductField() {
        barCodeField.setText("");
        quantityField.setText("1");
        nameField.setText("");
        priceField.setText("");
    }
}
