package be.ugent.degage.db.models;

import org.joda.time.LocalTime;

/**
 * Created by HannesM on 28/04/14.
 */
public class CarAvailabilityInterval {
    private Integer id;
    private DayOfWeek beginDayOfWeek;
    private LocalTime beginTime;
    private DayOfWeek endDayOfWeek;
    private LocalTime endTime;

    public CarAvailabilityInterval(DayOfWeek beginDayOfWeek, LocalTime beginTime, DayOfWeek endDayOfWeek, LocalTime endTime) {
        this(null, beginDayOfWeek, beginTime, endDayOfWeek, endTime);
    }

    public CarAvailabilityInterval(Integer id, DayOfWeek beginDayOfWeek, LocalTime beginTime, DayOfWeek endDayOfWeek, LocalTime endTime) {
        this.id = id;
        this.beginDayOfWeek = beginDayOfWeek;
        this.beginTime = beginTime;
        this.endDayOfWeek = endDayOfWeek;
        this.endTime = endTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DayOfWeek getBeginDayOfWeek() {
        return beginDayOfWeek;
    }

    public void setBeginDayOfWeek(DayOfWeek beginDayOfWeek) {
        this.beginDayOfWeek = beginDayOfWeek;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(LocalTime beginTime) {
        this.beginTime = beginTime;
    }

    public DayOfWeek getEndDayOfWeek() {
        return endDayOfWeek;
    }

    public void setEndDayOfWeek(DayOfWeek endDayOfWeek) {
        this.endDayOfWeek = endDayOfWeek;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }


}
