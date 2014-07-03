package be.ugent.degage.db.models;

/**
 * Created by Cedric on 4/17/2014.
 */
public class Tuple<T, V> {
    protected T t;
    protected V v;

    public Tuple(T t, V v){
        this.t = t;
        this.v = v;
    }

    public T getFirst(){
        return t;
    }

    public V getSecond(){
        return v;
    }

    public void setFirst(T t){
        this.t = t;
    }

    public void setSecond(V v){
        this.v = v;
    }
}
