package be.ugent.degage.db.models;

/**
 * Created by HannesM on 28/04/14.
 */
public enum DayOfWeek {
    // 1 = sunday, ..., 7 = saterday, same as MySQL
    SUNDAY(1, "Zondag"), MONDAY(2, "Maandag"), TUESDAY(3, "Dinsdag"), WEDNESDAY(4, "Woensdag"), THURSDAY(5, "Donderdag"), FRIDAY(6, "Vrijdag"), SATERDAY(7, "Zaterdag");

    private int i;
    private String description;

    DayOfWeek(int i, String description) {
        this.i = i;
        this.description = description;
    }

    public int getI() {
        return i;
    }

    public String getDescription() {
        return description;
    }

    public static DayOfWeek getDayFromString(String s) {
        for(DayOfWeek f : values()) {
            if(f.getDescription().equals(s)) {
                return f;
            }
        }
        return null;
    }

    public static DayOfWeek getDayFromInt(int i) {
        for(DayOfWeek f : values()) {
            if(f.getI() == i) {
                return f;
            }
        }
        return null;
    }
}
