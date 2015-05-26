package tsk.jgnk.watchdist.fx;

public class WatchFX {
    private int id;
    private int soldier;
    private int watchPoint;
    private int watchPointSlot;
    private String date;
    private int time;

    public WatchFX(int id, int soldier, int watchPoint, int watchPointSlot, String date, int time) {
        this.id = id;
        this.soldier = soldier;
        this.watchPoint = watchPoint;
        this.watchPointSlot = watchPointSlot;
        this.date = date;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSoldier() {
        return soldier;
    }

    public void setSoldier(int soldier) {
        this.soldier = soldier;
    }

    public int getWatchPoint() {
        return watchPoint;
    }

    public void setWatchPoint(int watchPoint) {
        this.watchPoint = watchPoint;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
