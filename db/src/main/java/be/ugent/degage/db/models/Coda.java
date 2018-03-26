
package be.ugent.degage.db.models;

import com.google.gson.annotations.Expose;
import java.time.LocalDate;

public class Coda {

    @Expose
    private int id;
    @Expose
    private LocalDate date;
    @Expose
    private String filename;
    @Expose
    private User user;

    public Coda(LocalDate date, String filename, User user) {
        this.id = id;
        this.date = date;
        this.filename = filename;
        this.user = user;
    }

    public Coda(int id, LocalDate date, String filename, User user) {
        this.id = id;
        this.date = date;
        this.filename = filename;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
