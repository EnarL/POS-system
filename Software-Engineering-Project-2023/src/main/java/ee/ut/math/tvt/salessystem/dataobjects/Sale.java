package ee.ut.math.tvt.salessystem.dataobjects;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;


@Entity
@Table(name = "SALE")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private LocalTime time;

    @OneToMany(mappedBy="sale",cascade = CascadeType.ALL)
    private List<SoldItem> soldItemList = new ArrayList<>();

    @Column(name = "total")
    private double total;

    public Sale(double total) {
        this.date = LocalDate.now();
        this.time = LocalTime.now();
        this.total = total;
    }

    public Sale() {
    }

    public long getID() {
        return id;
    }

    public void setID(long ID) {
        this.id = ID;
    }
    public LocalDate getDate() {
        return date;
    }

    public void addSoldItem(SoldItem soldItem) {
        soldItem.setSale(this);
        this.soldItemList.add(soldItem);
    }
    public List<SoldItem> getSoldItemList() {
        return soldItemList;
    }

    public void setDateOfTransaction(LocalDate dateOfTransaction) {
        this.date = dateOfTransaction;
    }

    public LocalTime getTimeOfTransaction() {
        return time;
    }

    public void setTimeOfTransaction(LocalTime timeOfTransaction) {
        this.time = timeOfTransaction;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

}