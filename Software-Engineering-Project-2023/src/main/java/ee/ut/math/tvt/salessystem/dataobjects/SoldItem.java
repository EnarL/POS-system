package ee.ut.math.tvt.salessystem.dataobjects;
import javax.persistence.*;




/**
 * Already bought StockItem. SoldItem duplicates name and price for preserving history.
 */

@Entity
@Table(name = "SOLDITEM")
public class SoldItem {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private double price;

    @Column(name = "total")
    private double total;
    @ManyToOne
    @JoinColumn(name = "STOCKITEM_ID", nullable = false)
    private StockItem stockItem;

    @ManyToOne
    @JoinColumn(name = "SALE_ID", nullable = false)
    private Sale sale;

    public SoldItem() {
    }

    public SoldItem(StockItem stockItem, int quantity) {
        this.id = stockItem.getId();
        this.stockItem = stockItem;
        this.name = stockItem.getName();
        this.price = stockItem.getPrice();
        this.quantity = quantity;
        this.total = stockItem.getPrice()*quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }
    public void addMoreQuantity(Integer quantity) {
        this.quantity += quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public double getSum() {
        return price * ((double) quantity);
    }

    public StockItem getStockItem() {
        return stockItem;
    }

    public void setStockItem(StockItem stockItem) {
        this.stockItem = stockItem;
    }

    @Override
    public String toString() {
        return String.format("SoldItem{id=%d, name='%s'}", id, name);
    }

    public void setSale(Sale sale) {this.sale = sale;}

    public Sale getSale() { return sale; }
}
