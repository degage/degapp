package be.ugent.degage.db.models;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class Receipt {

    private int id;
    private String name;
    private File files;
    private LocalDate date;
    private User user;
    private BigDecimal price;

    public Receipt(int id, String name, File files, LocalDate date, User user, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.files = files;
        this.date = date;
        this.user=user;
        this.price=price;
    }

    public Receipt(int id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFiles() {
        return files;
    }

    public void setFiles(File files) {
        this.files = files;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}

