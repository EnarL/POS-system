package ee.ut.math.tvt.salessystem;

import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Sale;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class submitCurrentPurchaseTest {
    private SalesSystemDAO dao = new InMemorySalesSystemDAO();
    private  List<SoldItem> soldItemList;

    private List<Sale> sales;


    @Before
    public void setUp() {
        List<StockItem> items = new ArrayList<StockItem>();
        items.add(new StockItem(1L, "Lays chips", "Potato chips", 11.0, 5));
        items.add(new StockItem(2L, "Chupa-chups", "Sweets", 8.0, 8));
        items.add(new StockItem(3L, "Frankfurters", "Beer sauseges", 15.0, 12));
        items.add(new StockItem(4L, "Free Beer", "Student's delight", 1.0, 100));

        this.soldItemList = new ArrayList<>();
        this.sales = new ArrayList<>();
        //making 12 transactions in total
        for (int i = 0; i < 4; i++) {
            SoldItem soldItem = new SoldItem(items.get(i), 3);
            SoldItem soldItem2 = new SoldItem(items.get(i), 3);
            SoldItem soldItem3 = new SoldItem(items.get(i), 3);
            soldItemList.add(soldItem);
            soldItemList.add(soldItem2);
            soldItemList.add(soldItem3);
            Sale sale = new Sale(items.get(i).getPrice() * soldItem.getQuantity());
            sale.addSoldItem(soldItem);
            sales.add(sale);
            dao.saveSale(sale);
            sale.addSoldItem(soldItem2);
            sales.add(sale);
            dao.saveSale(sale);
            sale.addSoldItem(soldItem3);
            sales.add(sale);
            dao.saveSale(sale);
        }
    }

@Test
    public void testLastTenPurchases(){
        int salesCount = dao.findAllTransactions().size();
        assertTrue("Sales count should not be lower than 10", salesCount >= 10);
        List<Sale> allDatabaseSales = dao.findLastTenTransactions();
        for (int i = 0; i < 10; i++){
            assertEquals(allDatabaseSales.get(i), sales.get(i));
        }
    }
    @Test
    public void testSubmittingCurrentPurchaseDecreasesStockItemQuantity() throws Exception {
        InMemorySalesSystemDAO mockDao = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao);
        SoldItem soldItem = new SoldItem(dao.findStockItem(1L), 2);
        SoldItem soldItem2 = new SoldItem(dao.findStockItem(2L), 2);
        int quantityBefore = mockDao.findStockItem(1L).getQuantity();
        int quantityBefore2 = mockDao.findStockItem(2L).getQuantity();
        testCart.addItem(soldItem);
        testCart.addItem(soldItem2);
        testCart.submitCurrentPurchase();
        int quantityAfter = mockDao.findStockItem(1L).getQuantity();
        int quantityAfter2 = mockDao.findStockItem(2L).getQuantity();
        Assert.assertTrue(quantityBefore > quantityAfter);
        Assert.assertTrue(quantityBefore2 > quantityAfter2);
    }
    @Test
    public void testSubmittingCurrentPurchaseBeginsAndCommitsTransaction() throws Exception {
        SalesSystemDAO dao = mock(SalesSystemDAO.class);
        StockItem stockItemToAdd = new StockItem(12345L, "Lays", "test", 10.99, 2);

        when(dao.findStockItem(anyLong())).thenReturn(stockItemToAdd);
        ArgumentCaptor<StockItem> stockItemCaptor = ArgumentCaptor.forClass(StockItem.class);
        dao.beginTransaction();
        dao.saveStockItem(stockItemToAdd);
        dao.commitTransaction();
        verify(dao).beginTransaction();
        verify(dao).saveStockItem(stockItemCaptor.capture());
        verify(dao).commitTransaction();
        StockItem capturedStockItem = stockItemCaptor.getValue();

        assertEquals(stockItemToAdd, capturedStockItem);
    }
    @Test
    public void testSubmittingCurrentOrderCreatesHistoryItem() throws Exception {
        InMemorySalesSystemDAO mockDao = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao);
        SoldItem soldItem = new SoldItem(mockDao.findStockItem(1L), 2);

        int historyItemsBefore = mockDao.findAllTransactions().size();
        testCart.addItem(soldItem);
        testCart.submitCurrentPurchase();
        int historyItemsAfter = mockDao.findAllTransactions().size();

        Assert.assertTrue(historyItemsAfter > historyItemsBefore);
    }
    @Test
    public void testSubmittingCurrentOrderSavesCorrectTime() throws Exception {
        InMemorySalesSystemDAO mockDao = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao);
        SoldItem soldItem = new SoldItem(dao.findStockItem(1L), 2);
        testCart.addItem(soldItem);
        testCart.submitCurrentPurchase();

        LocalTime currentTime = LocalTime.now();
        List<Sale> transactions = mockDao.findAllTransactions();

        long difference = transactions.get(0).getTimeOfTransaction().toSecondOfDay() - currentTime.toSecondOfDay();
        Assert.assertEquals(1, transactions.size());
        Assert.assertTrue(difference < 1);

    }
    @Test
    public void testCancellingOrder() throws Exception {
        InMemorySalesSystemDAO mockDao = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao);
        SoldItem soldItem = new SoldItem(mockDao.findStockItem(1L), 2);
        SoldItem soldItem1 = new SoldItem(mockDao.findStockItem(2L), 2);

        int quantityBefore = mockDao.findStockItem(1L).getQuantity();
        int quantityBefore2 = mockDao.findStockItem(2L).getQuantity();
        testCart.addItem(soldItem);
        testCart.cancelCurrentPurchase();
        testCart.addItem(soldItem1);
        testCart.submitCurrentPurchase();
        int quantityAfter = mockDao.findStockItem(1L).getQuantity();
        int quantityAfter2 = mockDao.findStockItem(2L).getQuantity();



        Assert.assertEquals(quantityBefore, quantityAfter);
        Assert.assertTrue(quantityBefore2 > quantityAfter2);
    }
    @Test
    public void testCancellingOrderQuanititesUnchanged() throws Exception {
        InMemorySalesSystemDAO mockDao = Mockito.spy(new InMemorySalesSystemDAO());
        ShoppingCart testCart = new ShoppingCart(mockDao);
        SoldItem soldItem = new SoldItem(mockDao.findStockItem(1L), 2);

        int quantityBefore = mockDao.findStockItem(1L).getQuantity();
        testCart.addItem(soldItem);
        testCart.cancelCurrentPurchase();
        int quantityAfter = mockDao.findStockItem(1L).getQuantity();

        Assert.assertEquals(quantityBefore, quantityAfter);
    }
}