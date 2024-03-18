package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.dataobjects.Sale;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;

public class InMemorySalesSystemDAO implements SalesSystemDAO {

    private final List<StockItem> stockItemList;
    private final List<SoldItem> soldItemList;

    private List<Sale> sales;
    private Sale sale;


    public InMemorySalesSystemDAO() {
        List<StockItem> items = new ArrayList<StockItem>();
        items.add(new StockItem(1L, "Lays chips", "Potato chips", 11.0, 5));
        items.add(new StockItem(2L, "Chupa-chups", "Sweets", 8.0, 8));
        items.add(new StockItem(3L, "Frankfurters", "Beer sauseges", 15.0, 12));
        items.add(new StockItem(4L, "Free Beer", "Student's delight", 0.0, 100));
        this.stockItemList = items;
        this.soldItemList = new ArrayList<>();
        this.sales = new ArrayList<>();
    }
    @Override
    public void removeStockItemEntirely(StockItem item) {
        stockItemList.removeIf(i -> i.getId().equals(item.getId()));
    }

    @Override
    public void removeAmountOfStockItem(StockItem item) {

    }

    @Override
    public List<StockItem> findStockItems() {
        return stockItemList;
    }

    @Override
    public List<SoldItem> findSoldItems(Long id) {
        return null;
    }

    @Override
    public StockItem findStockItem(long id) {
        for (StockItem item : stockItemList) {
            if (item.getId() == id)
                return item;
        }
        return null;
    }

    @Override
    public void updateStockItemPrice(Long id, double price) {
        StockItem stockItem = findStockItem(id);
        if (stockItem != null) {
            stockItem.setPrice(price);
        }
    }

    @Override
    public List<Sale> findAllTransactions() {
        return sales;
    }

    @Override
    public List<Sale> findLastTenTransactions() {
        return sales;
    }

    @Override
    public void saveSale(Sale sale) {
        this.sale = sale;
        commitTransaction();
    }

    public SoldItem findSoldItem(long id){
        for (SoldItem item : soldItemList) {
            if(item.getId() == id)
                return item;
        }
        return null;
    }


    @Override
    public void savePurchase(List<SoldItem> purchaseItems) {
        // Add the purchase items to the list of sold items
        soldItemList.addAll(purchaseItems);
    }

    @Override
    public void saveSoldItem(SoldItem item) {

    }

    @Override
    public List<Sale> findTransactionsBetween(LocalDate startDate, LocalDate endDate) {
        List<Sale> matching = new ArrayList<>();
        for (Sale sale : sales) {
            System.out.println(startDate);
            if (!sale.getDate().isBefore(startDate) && sale.getDate().compareTo(endDate) < 1) {
                matching.add(sale);
            }
        }
        return matching;
    }

    // Adds a new warehouse product to the list
    @Override
    public void saveStockItem(StockItem stockItem) {
        if(stockItem.getQuantity() > 0)
            stockItemList.add(stockItem);
        else
            throw new RuntimeException("error");
    }

    @Override
    public void removeStockItem(StockItem stockItem) {
        stockItemList.remove(stockItem);
    }

    @Override
    public void beginTransaction() {
        soldItemList.addAll(soldItemList);
    }

    @Override
    public void rollbackTransaction() {
    }

    @Override
    public void commitTransaction() {
        sales.add(sale);
    }

    @Override
    public void updateStockItemQuantity(Long id, int i) {

    }

}
