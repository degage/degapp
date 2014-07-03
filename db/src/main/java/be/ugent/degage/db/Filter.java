package be.ugent.degage.db;

/**
 * Created by HannesM on 17/03/14.
 */
public interface Filter {
    public void putValue(FilterField field, String string);
    public String getValue(FilterField field);
}
