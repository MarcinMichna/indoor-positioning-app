package pl.michnam.app.core.http.model;

public class HotspotData {
    private String ssid;
    private int rssi;
    private String esp;
    private String timestamp;

    public HotspotData(String ssid, int rssi, String esp) {
        this.ssid = ssid;
        this.rssi = rssi;
        this.esp = esp;
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

    public String getEsp() {
        return esp;
    }

    public void setEsp(String esp) {
        this.esp = esp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "HotspotData{" +
                "ssid='" + ssid + '\'' +
                ", rssi=" + rssi +
                ", esp='" + esp + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
