package ee.ut.math.tvt.salessystem.ui;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.HibernateSalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Sale;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.dataobjects.TeamInfo;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * A simple CLI (limited functionality).
 */
public class ConsoleUI {
    private static final Logger log = LogManager.getLogger(ConsoleUI.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart cart;

    public ConsoleUI(SalesSystemDAO dao) {
        this.dao = dao;
        cart = new ShoppingCart(dao);
    }

    public static void main(String[] args) throws Exception {
        SalesSystemDAO dao = new HibernateSalesSystemDAO();
        ConsoleUI console = new ConsoleUI(dao);
        console.run();
    }

    /**
     * Run the sales system CLI.
     */
    public void run() throws IOException {
        System.out.println("===========================");
        System.out.println("=       Sales System      =");
        System.out.println("===========================");
        printUsage();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            log.info("> ");
            processCommand(in.readLine().trim().toLowerCase());
            log.info("Done. ");
        }
    }

    private void showStock() {
        List<StockItem> stockItems = dao.findStockItems();
        log.info("-------------------------");
        for (StockItem si : stockItems) {
            log.info(si.getId() + " " + si.getName() + " " + si.getPrice() + "Euro (" + si.getQuantity() + " items)");
        }
        if (stockItems.size() == 0) {
            log.info("\tNothing");
        }
        log.info("-------------------------");
    }

    private void showTeamInfo() {
        TeamInfo team = new TeamInfo();
        log.info("-------------------------");
        log.info("Team info");
        log.info("Team name: " + team.teamInfo()[0] );
        log.info("Team leader email: " +team.teamInfo()[1]);
        log.info("Team members: " + team.teamInfo()[2]);
        log.info("-------------------------");
    }

    private void showCart() {
        log.info("-------------------------");
        double total = 0.0;
        for (SoldItem si : cart.getAll()) {
            log.info(si.getName() + "; " + si.getPrice() + " Euro; (" + si.getQuantity() + " items); Total: " + si.getSum());
            total += si.getSum();
        }
        if (cart.getAll().size() == 0) {
            log.info("\tNothing");
        }
        log.info("Total: " + total);
        log.info("-------------------------");
    }

    private void printUsage() {
        log.info("-------------------------");
        System.out.println("Usage:");
        System.out.println("h\t\t\t\tShow this help");
        System.out.println("t\t\t\t\tShow team info");
        System.out.println("q\t\t\t\tExit system");
        log.info("-------------------------");
        System.out.println("w\t\t\t\tShow warehouse contents");
        System.out.println("wa\t\t\t\tAdd new item to the warehouse");
        System.out.println("wr\t\t\t\tRemove last item in the warehouse");
        System.out.println("ws\t\t\t\tSearch for item in the warehouse");
        log.info("-------------------------");
        System.out.println("c\t\t\t\tShow cart contents");
        System.out.println("ca\t\t\t\tAdd new item to the cart");
        System.out.println("cp\t\t\t\tPurchase the shopping cart");
        System.out.println("cr\t\t\t\tReset the shopping cart");
        log.info("-------------------------");
        System.out.println("ha\t\t\t\tShow history of all purchases");
        System.out.println("hl\t\t\t\tShow history of last 10 purchases");
        System.out.println("hd\t\t\t\tShow history between dates");
        System.out.println("ht\t\t\t\tShow history of specific transaction");
        log.info("-------------------------");
    }
    private String[] command(String a) throws IOException {
        log.info(a);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        return bufferedReader.readLine().trim().split(" ");
    }

    private void processCommand(String command) throws IOException {
        String[] c = command.split(" ");

        if (c[0].equals("h") && c.length == 1)
            printUsage();
        else if (c[0].equals("t"))
            showTeamInfo();
        else if (c[0].equals("q"))
            System.exit(0);
        else if (c[0].equals("w"))
            showStock();
        else if (c[0].equals("wa")) {
            long id = Long.parseLong(command("Enter barcode: ")[0]);
            String name = command("Enter name: ")[0];
            String description = command("Enter description: ")[0];
            double price = Double.parseDouble(command("Enter price: ")[0]);
            int quantity = Integer.parseInt(command("Enter quantity: ")[0]);
            StockItem item = new StockItem(id, name, description, price, quantity);
            dao.saveStockItem(item);
            log.info("Item added");
        }
        else if (c[0].equals("wr")) {
            if (!dao.findStockItems().isEmpty()) {
                StockItem lastItem = dao.findStockItems().get(dao.findStockItems().size() - 1);
                dao.removeStockItem(lastItem);
                log.info("Last item removed");
            } else {
                log.error("No items in the warehouse to remove");
            }
        }
        else if (c[0].equals("ws")){
            long id = Long.parseLong(command("Enter barcode: ")[0]);
            StockItem item = dao.findStockItem(id);
            if (item != null) {
                System.out.println("Id: " + dao.findStockItem(id).getId() +
                        ", name: " + dao.findStockItem(id).getName() +
                        ", price: " + dao.findStockItem(id).getPrice() +
                        ", quantity: " + dao.findStockItem(id).getQuantity());
            }
            else {
                log.info("No stock item with id " + id);
            }
        }
        else if (c[0].equals("c"))
            showCart();
        else if (c[0].equals("ca")) {
            try {
                long idx = Long.parseLong(command("Enter barcode: ")[0]);
                int amount = Integer.parseInt(command("Enter quantity: ")[0]);
                StockItem item = dao.findStockItem(idx);
                int maxQuantity = item.getQuantity();
                if (maxQuantity < amount || amount < 0) {
                    System.out.println("Product is not available in the inserted quantities.");
                }
                else {
                    if (item != null) {
                        cart.addItem(new SoldItem(item, amount));
                    } else {
                        log.info("no stock item with id " + idx);
                    }
                }
            } catch (SalesSystemException | NoSuchElementException e) {
                log.error("Failed to add item to basket", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else if (c[0].equals("cp"))
            cart.submitCurrentPurchase();
        else if (c[0].equals("cr")) {
            for (int i = 0; i < cart.getAll().size(); i++) {
                StockItem stockItem = cart.getAll().get(i).getStockItem();
                stockItem.setQuantity(stockItem.getQuantity() + cart.getAll().get(i).getQuantity());
            }
            cart.cancelCurrentPurchase();
        }
        else if (c[0].equals("ha")) {
            List<Sale> sales = dao.findAllTransactions();
            if (sales.isEmpty()) {
                log.info("There are have not been any transactions.");
                return;
            }
            for (int i = 0; i < sales.size(); i++) {
                log.info("Transaction. Date: " + sales.get(i).getDate() + "; time: " + sales.get(i).getTimeOfTransaction());
            }
        }
        else if (c[0].equals("hl")) {
            List<Sale> sales = dao.findAllTransactions();
            if (sales.isEmpty()) {
                log.info("There are have not been any transactions.");
                return;
            }
            List<Sale> lastTen = new ArrayList<>();
            if (sales.size() < 10) {
                for (int i = sales.size() - 1; i >= 0; i--) {
                    lastTen.add(sales.get(i));
                }
            }
            else {
                for (int i = sales.size() - 1; i >= sales.size() - 10; i--) {
                    lastTen.add(sales.get(i));
                }
            }
            for (int i = lastTen.size() - 1; i >= 0; i--) {
                log.info("Transaction. Date: " + lastTen.get(i).getDate() + "; time: " + lastTen.get(i).getTimeOfTransaction());
            }
        }
        else if (c[0].equals("hd")) {
            String sd = command("Enter start date (date format: dd/MM/yyyy): ")[0];
            LocalDate startDate = LocalDate.of(Integer.parseInt(sd.split("/")[2]), Integer.parseInt(sd.split("/")[1]), Integer.parseInt(sd.split("/")[0]));
            String ed = command("Enter end date (date format: dd/MM/yyyy): ")[0];
            LocalDate endDate = LocalDate.of(Integer.parseInt(ed.split("/")[2]), Integer.parseInt(ed.split("/")[1]), Integer.parseInt(ed.split("/")[0]));
            List<Sale> sales = dao.findTransactionsBetween(startDate, endDate);
            if (sales.isEmpty()) {
                log.info("There have been no transactions between dates " + sd + " and " + ed + ".");
                return;
            }
            log.info("Transactions between dates " + sd + " and " + ed + ":");
            for (Sale sale : sales) {
                log.info("Transaction. Date: " + sale.getDate() + "; time: " + sale.getTimeOfTransaction());
            }
        }
        else if (c[0].equals("ht")) {
            List<Sale> sales = dao.findAllTransactions();
            List<SoldItem> soldItems = new ArrayList<>();
            if (sales.isEmpty()) {
                log.info("There have been no transactions");
                return;
            }
            String date = command("Enter transaction date (date format: dd/MM/yyyy): ")[0];
            LocalDate d = LocalDate.of(Integer.parseInt(date.split("/")[2]), Integer.parseInt(date.split("/")[1]), Integer.parseInt(date.split("/")[0]));
            String time = command("Enter transaction time (time format: hour:minute:second): ")[0];
            LocalTime t = LocalTime.of(Integer.parseInt(time.split(":")[0]), Integer.parseInt(time.split(":")[1]), Integer.parseInt(time.split(":")[2]));
            int number = 0;
            for (int i = 0; i < sales.size(); i++) {
                if (sales.get(i).getDate().equals(d) && sales.get(i).getTimeOfTransaction().getHour() == (t.getHour()) && sales.get(i).getTimeOfTransaction().getMinute() == (t.getMinute()) && sales.get(i).getTimeOfTransaction().getSecond() == (t.getSecond())) {
                    for (int j = 0; j < soldItems.size(); j++) {
                        System.out.println("Id: " + soldItems.get(j).getId() + "; name: " + soldItems.get(j).getName()+ "; price: " + soldItems.get(j).getPrice()+ "; quantity: " + soldItems.get(j).getQuantity()+ "; sum: " + soldItems.get(j).getSum());
                    }
                    number += 1;
                }
            }
            if (number == 0) {
                log.info("There have been no transactions in " + date + " " + time);
            }
        }
        else {
            log.info("unknown command");
        }
    }
}