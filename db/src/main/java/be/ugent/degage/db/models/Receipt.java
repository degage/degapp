package be.ugent.degage.db.models;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class Receipt {

    private int id;
    private String name;
    private File files;
    private DateTime date;
    private User user;
    private BigDecimal price;

    public Receipt(int id, String name, File files, DateTime date, User user, BigDecimal price) {
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

    public DateTime getDate() {
        return date;
    }

    public String getDateString() {
        return new SimpleDateFormat("dd-MM-yyyy").format(date.toDate());
    }

    public void setDate(DateTime date) {
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

