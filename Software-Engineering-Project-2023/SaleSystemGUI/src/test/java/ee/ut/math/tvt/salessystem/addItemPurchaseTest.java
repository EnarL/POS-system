package ee.ut.math.tvt.salessystem;

import ee.ut.math.tvt.salessystem.dao.*;
import static org.junit.Assert.*;


import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import static org.junit.Assert.assertThrows;


public class addItemPurchaseTest {
    private SalesSystemDAO dao;
    private StockItem stockItem;
    private StockItem stockItem2;



    @Before
    public void setUp() {
        stockItem = new StockItem(12345L, "Lays", "test", 10.99, 2);
        stockItem2 = new StockItem(10L, "Lays", "xd", 10, 1);

        dao = new InMemorySalesSystemDAO();

    }

    //tests for shoppingCart addItem method
    @Test
    public void testAddingExistingItem() throws Exception {
        InMemorySalesSystemDAO mockDao = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao);
        StockItem stockItem = mockDao.findStockItem(1L);
        SoldItem soldItem = new SoldItem(stockItem, 3);
        dao.saveStockItem(stockItem);
        testCart.addItem(soldItem);
        int ItemQuantityAfterAddingFirstTime = testCart.getAll().get(0).getQuantity();
        SoldItem soldItem1 = new SoldItem(stockItem, 2);
        testCart.addItem(soldItem1);
        int ItemQuantityAfterAddingSecondTime = testCart.getAll().get(0).getQuantity();
        assertNotEquals(ItemQuantityAfterAddingFirstTime, ItemQuantityAfterAddingSecondTime);
    }
    @Test
    public void testAddingNewItem() throws Exception {
        InMemorySalesSystemDAO mockDao = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao);
        StockItem stockItem = mockDao.findStockItem(1L);
        SoldItem soldItem = new SoldItem(stockItem, 1);
        dao.saveStockItem(stockItem);
        int initialSize = testCart.getAll().size();
        testCart.addItem(soldItem);
        assertNotEquals(initialSize, testCart.getAll().size());
    }
    @Test
    public void testAddingItemWithNegativeQuantity() throws Exception {
        InMemorySalesSystemDAO mockDao = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao);
        int quantity = -1;
        StockItem stockItem = mockDao.findStockItem(4L);
        SoldItem item = new SoldItem(stockItem, quantity);
        assertThrows(Exception.class, () -> testCart.addItem(item));
    }

    @Test
    public void testAddingItemWithQuantityTooLarge() throws Exception {
        InMemorySalesSystemDAO mockDao2 = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao2);
        int quantity = 200;
        StockItem stockItem = mockDao2.findStockItem(4L);
        SoldItem item = new SoldItem(stockItem, quantity);
        assertThrows(Exception.class, () -> testCart.addItem(item));
    }
    @Test
    public void testAddingItemWithQuantitySumTooLarge() throws Exception {
        InMemorySalesSystemDAO mockDao = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao);
        int validQuantity = 1;
        int exceedingQuantity = 1000;
        StockItem stockItem = mockDao.findStockItem(4L);
        SoldItem item = new SoldItem(stockItem, validQuantity);
        testCart.addItem(item);
        SoldItem additionalItem = new SoldItem(stockItem, exceedingQuantity);
        assertThrows(Exception.class, () -> testCart.addItem(additionalItem));
    }
}
