package pl.michnam.app.sql.entity;

import java.util.Date;

public class WifiCurrent {
    private int id;
    private String ssid;
    private int rssi;
    private Date timestamp;

    public WifiCurrent(int id, String ssid, int rssi, Date timestamp) {
        this.id = id;
        this.ssid = ssid;
        this.rssi = rssi;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
