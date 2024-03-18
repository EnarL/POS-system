package ee.ut.math.tvt.salessystem;

import ee.ut.math.tvt.salessystem.dao.*;
import java.util.List;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
public class addItemStockTest {

    private SalesSystemDAO dao;
    private StockItem stockItem;
    private StockItem stockItem2;



    @Before
    public void setUp() {
        stockItem = new StockItem(12345L, "Lays", "test", 10.99, 2);
        stockItem2 = new StockItem(10L, "Lays", "xd", 10, 1);
        dao = new InMemorySalesSystemDAO();




    }

    //tests for stockItem adding and removing
    @Test
    public void testAddingItemBeginsAndCommitsTransaction(){SalesSystemDAO dao = mock(SalesSystemDAO.class);
        StockItem stockItemToAdd = new StockItem(12345L, "Lays", "test", 10.99, 2);

        // Set up the SalesSystemDAO to return the stock item when finding by ID
        when(dao.findStockItem(anyLong())).thenReturn(stockItemToAdd);

        ArgumentCaptor<StockItem> stockItemCaptor = ArgumentCaptor.forClass(StockItem.class);
        dao.beginTransaction();
        dao.saveStockItem(stockItemToAdd);
        dao.commitTransaction();
        verify(dao).beginTransaction();
        verify(dao).saveStockItem(stockItemCaptor.capture());
        verify(dao).commitTransaction();
        // Retrieve the captured argument
        StockItem capturedStockItem = stockItemCaptor.getValue();


        // if saveStockItem modifies the stockItem, you can check that here
        assertEquals(stockItemToAdd, capturedStockItem);

    }

    @Test
    public void testAddingNewItem() {
        // Initial stock item count
        int initialItemCount = dao.findStockItems().size();

        dao.saveStockItem(stockItem);

        // Check if the stock item was added
        int newItemCount = dao.findStockItems().size();
        assertEquals(initialItemCount + 1, newItemCount);

        // Check if the added item exists in the database
        StockItem newItem = dao.findStockItem(12345L);
        assertNotNull(newItem);
        assertEquals("Lays", newItem.getName());
        assertEquals("test", newItem.getDescription());
        assertEquals(10.99, newItem.getPrice(), 0.001);
        assertEquals(2, newItem.getQuantity());
    }
    @Test
    public void testAddingExistingItem(){
        StockItem existingItem = new StockItem(1235L, "Existing Item", "Description", 10.0, 20);
        dao.saveStockItem(existingItem);

        // Get the initial item count and quantity in the warehouse
        int initialItemCount = dao.findStockItems().size();
        int initialQuantity = existingItem.getQuantity();



        // Verify that the item's quantity is increased
        assertTrue(initialQuantity + 20 > existingItem.getQuantity());

        // Verify that the item was not saved again
        assertFalse(dao.findStockItems().size() > initialItemCount);


        assertEquals(initialItemCount, dao.findStockItems().size());


    }
    @Test
    public void testAddingItemWithNegativeQuantity(){
        int initialItemCount = dao.findStockItems().size();
        StockItem existingItem = new StockItem(1235L, "Existing Item", "Description", 10.0, -20);
        int ItemCountAfter = dao.findStockItems().size();
        assertEquals(initialItemCount, ItemCountAfter);

    }
    @Test
    public void TestRemovingExistingItem(){
        //First adding a new item
        StockItem newItem = new StockItem(12L, "Burger", "Description", 3, 2);
        dao.saveStockItem(newItem);
        //the initial number of items
        int initialItemCount = dao.findStockItems().size();
        dao.removeStockItem(newItem);
        //number of items after removing the item
        int updatedItemCount = dao.findStockItems().size();
        assertEquals(initialItemCount -1,updatedItemCount );
    }



}
