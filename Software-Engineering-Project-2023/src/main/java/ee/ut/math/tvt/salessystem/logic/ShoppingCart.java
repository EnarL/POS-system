package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Sale;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private final SalesSystemDAO dao;
    private final List<SoldItem> items = new ArrayList<>();

    public ShoppingCart(SalesSystemDAO dao) {
        this.dao = dao;
    }

    /**
     * Add new SoldItem to table.
     */
    public void addItem(SoldItem item) throws Exception {
        if (item == null) {
            throw new SalesSystemException("There is no such item");
        }
        if (item.getQuantity() <= 0) {
            throw new Exception("Quantity cannot be zero or less than zero");
        }
        if (!quantityOfItemCanBeAdded(item) && !itemIsInCart(item)) {
            throw new SalesSystemException("Desired quantity of " + item.getQuantity() + " exceeds the maximum quantity " +
                    "of " + dao.findStockItem(item.getStockItem().getId()).getQuantity());
        }
        if (itemIsInCart(item)) {
            if (moreOfTheItemCanBeAdded(item)) {
                SoldItem existingItem = items
                        .stream()
                        .filter(i -> i.getName().equals(item.getName()))
                        .findFirst()
                        .get();
                existingItem.addMoreQuantity(item.getQuantity());
                StockItem stockItem = dao.findStockItem(item.getStockItem().getId());
                stockItem.lowerQuantity(item.getQuantity());
                dao.removeAmountOfStockItem(stockItem);
            } else {
                int remainingQuantity = dao.findStockItem(item.getStockItem().getId()).getQuantity();
                throw new SalesSystemException("Can't add " + item.getQuantity() + " of " + item.getName() + " to the cart.\nThis " +
                        "exceeds the remaining quantity of " + remainingQuantity + ".");
            }
        } else {
            items.add(item);
            StockItem stockItem = dao.findStockItem(item.getStockItem().getId());
            if (item.getQuantity() < dao.findStockItem(item.getStockItem().getId()).getQuantity()) {
                stockItem.lowerQuantity(item.getQuantity());
                dao.removeAmountOfStockItem(stockItem);
            } else {
                stockItem.setQuantity(0);
                dao.removeStockItemEntirely(stockItem);
            }
        }
       }

    public void removeItem(SoldItem item) {
        if (item.getQuantity() <=0) {
            throw new SalesSystemException("Quantity cannot be zero or less than zero");
        }
        if (items.stream().noneMatch(i -> i.getStockItem().getId() == item.getId())){
            throw new SalesSystemException("There is no such item");
        }

        items.remove(item);
    }

    public List<SoldItem> getAll() {
        return items;
    }

    public void cancelCurrentPurchase() {
        for (SoldItem item: items) {
            StockItem stockItem = item.getStockItem();
            int quantity = stockItem.getQuantity();
            stockItem.setQuantity(item.getQuantity() + quantity);
        }
        items.clear();
    }

    public double calculateTotalPrice(){
        double sum = 0.0;
        for (SoldItem item : items) {
            sum += item.getPrice()*(double)(item.getQuantity());
        }
        return sum;
    }

    public void submitCurrentPurchase() {
        List<SoldItem> currentPurchaseItems = new ArrayList<>(items);
        List<SoldItem> saleItems = new ArrayList<>(items);
        Sale sale = new Sale(calculateTotalPrice());
        dao.saveSale(sale);
        items.clear();

    }
    private boolean itemIsInCart(SoldItem item) {
        return items.stream().anyMatch(e -> e.getName().equals(item.getName()));
    }

    private boolean moreOfTheItemCanBeAdded(SoldItem item) {
        int stockQuantity = dao.findStockItem(item.getStockItem().getId()).getQuantity();
        return item.getQuantity() <= stockQuantity;
    }
    private boolean quantityOfItemCanBeAdded(SoldItem item) {
        return item.getQuantity() <= item.getStockItem().getQuantity();
    }

}
