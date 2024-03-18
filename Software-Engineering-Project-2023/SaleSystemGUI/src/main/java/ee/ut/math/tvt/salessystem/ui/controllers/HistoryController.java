package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Sale;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.net.URL;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {
    private static final Logger log = LogManager.getLogger(HistoryController.class);
    private SalesSystemDAO dao;
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private TableView<Sale> historyTableView;
    @FXML
    private TableView<SoldItem> historyTransactionView;
    private ObservableList<Sale> saleData;

    public HistoryController(SalesSystemDAO dao) {
        this.dao = dao;
        this.saleData = FXCollections.observableArrayList(dao.findAllTransactions());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupHistoryTableView();
        log.debug("History tab initialized");
    }
    @FXML
    public void showAll() {
        historyTableView.setItems(FXCollections.observableList(dao.findAllTransactions()));
        clearHistoryDetails();
        log.info("All history is shown.");
    }

    @FXML
    public void showBetweenDates() {
        try {
            if (startDate.getValue() == null || endDate.getValue() == null)
                throw new SalesSystemException("Enter start and end date");
            historyTableView.setItems(FXCollections.observableList(dao.findTransactionsBetween(startDate.getValue(), endDate.getValue())));
            clearHistoryDetails();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showLastTen(){
        historyTableView.setItems(FXCollections.observableList(dao.findLastTenTransactions()));
        clearHistoryDetails();
        log.info("History of last 10 transactions is shown.");
    }

    public void clearHistoryDetails() {
        historyTransactionView.getItems().clear();
        historyTransactionView.refresh();
    }


    private void setupHistoryTableView() {
        historyTableView.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Sale>) (observable, oldValue, newValue) -> {
            if (newValue != null) {
                showHistoryItemDetails(newValue);
            }
        });
    }

    @FXML
    private void showHistoryItemDetails(Sale sale) {
        if (historyTransactionView != null) {
            if (sale.getSoldItemList() != null) {
                ObservableList<SoldItem> soldItemObservableList = FXCollections.observableArrayList(sale.getSoldItemList());
                historyTransactionView.setItems(soldItemObservableList);
                historyTransactionView.refresh();
            }
        } else {
            log.error("Cannot display history item details.");
        }
    }

    @FXML
    private void purchases() {
        Sale selectedHistoryItem = historyTableView.getSelectionModel().getSelectedItem();
        if (selectedHistoryItem != null) {
            showHistoryItemDetails(selectedHistoryItem);
        }
    }

}