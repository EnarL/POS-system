package ee.ut.math.tvt.salessystem.dao;
import ee.ut.math.tvt.salessystem.dataobjects.Sale;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.util.List;

public class HibernateSalesSystemDAO implements SalesSystemDAO {
    private final EntityManagerFactory emf;
    private final EntityManager em;

    public HibernateSalesSystemDAO() {
// if you get ConnectException / JDBCConnectionException then you
// probably forgot to start the database before starting the application
        emf = Persistence.createEntityManagerFactory("pos");
        em = emf.createEntityManager();
    }

    // TODO implement missing methods
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void updateStockItemQuantity(Long id, int quantity) {
        StockItem stockItem = em.find(StockItem.class, id);
        if (stockItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("Stock can not be negative");
        }
        stockItem.setQuantity(stockItem.getQuantity() + quantity);

        em.merge(stockItem);
    }

    @Override
    public void updateStockItemPrice(Long id, double price) {
        if (price < 0)
            throw new IllegalArgumentException("Price can not be negative");
        StockItem stockItem = em.find(StockItem.class, id);
        if (stockItem != null) {
            stockItem.setPrice(price);
            em.merge(stockItem);
        }
    }

    //Warehouse
    @Override
    public List<StockItem> findStockItems() {
        return em.createQuery("from StockItem", StockItem.class).getResultList();
    }

    //Transactions
    @Override
    public List<Sale> findLastTenTransactions() {
        return em.createQuery("from Sale order by date desc, time desc", Sale.class)
                .setMaxResults(10)
                .getResultList();
    }

    @Override
    public List<Sale> findTransactionsBetween(LocalDate startDate, LocalDate endDate) {
        return em.createQuery("from Sale where date between :startDate and :endDate", Sale.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @Override
    public List<Sale> findAllTransactions() {
        return em.createQuery("from Sale order by date desc, time desc", Sale.class).getResultList();
    }
    @Override
    public void removeStockItemEntirely(StockItem stockItem) {
        beginTransaction();
        em.merge(stockItem);
        commitTransaction();
    }

    @Override
    public void removeAmountOfStockItem(StockItem stockItem) {
        beginTransaction();
        em.merge(stockItem);
        commitTransaction();
    }


    @Override
    public StockItem findStockItem(long id) {
        return findStockItems().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<SoldItem> findSoldItems(Long id) {
        return em.createQuery("from SoldItem where sale = :sale", SoldItem.class)
                .setParameter("sale", id)
                .getResultList();
    }

    @Override
    public void saveStockItem(StockItem stockItem) {
        if (stockItem.getPrice() < 0)
            throw new IllegalArgumentException("Error");
        beginTransaction();
        em.persist(stockItem);
        commitTransaction();
    }

    @Override
    public void removeStockItem(StockItem stockItem) {
        stockItem = em.find(StockItem.class, stockItem.getId());
        em.remove(stockItem);
    }

    @Override
    public void savePurchase(List<SoldItem> purchaseItems) {
        beginTransaction();
        try {
            for (SoldItem item : purchaseItems) {
                em.persist(item);
            }
            commitTransaction();
        } catch (Exception e) {
            rollbackTransaction();
        }
    }

    @Override
    public void saveSoldItem(SoldItem item) {
        em.persist(item);
        em.flush();
        solditemToSale(item);
    }

    private void solditemToSale(SoldItem item) {
        Sale sale = item.getSale();
        if (sale != null) {
            sale.addSoldItem(item);
            em.merge(sale);
        }
    }

    @Override
    public void saveSale(Sale sale) {
        beginTransaction();
        em.persist(sale);
        em.flush();
        commitTransaction();
    }


    @Override
    public void beginTransaction() {
        em.getTransaction().begin();
    }

    @Override
    public void rollbackTransaction() {
        em.getTransaction().rollback();
    }

    @Override
    public void commitTransaction() {
        em.getTransaction().commit();
    }

    public void close() {
        em.close();
        emf.close();
    }
}
