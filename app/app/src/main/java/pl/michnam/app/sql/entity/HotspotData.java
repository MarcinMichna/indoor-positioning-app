package pl.michnam.app.sql.entity;

public class HotspotData {
    private String ssid;
    private String esp;
    private int minRssi;
    private int maxRssi;
    private double avg;
    private double sd;

    public HotspotData(String ssid, String esp, int minRssi, int maxRssi, double avg, double sd) {
        this.ssid = ssid;
        this.esp = esp;
        this.minRssi = minRssi;
        this.maxRssi = maxRssi;
        this.avg = avg;
        this.sd = sd;
    }

    public HotspotData(String esp, int minRssi, int maxRssi, double avg, double sd) {
        this.esp = esp;
        this.minRssi = minRssi;
        this.maxRssi = maxRssi;
        this.avg = avg;
        this.sd = sd;
    }

    @Override
    public String toString() {
        return "HotspotData{" +
                "ssid='" + ssid + '\'' +
                ", esp='" + esp + '\'' +
                ", minRssi=" + minRssi +
                ", maxRssi=" + maxRssi +
                ", avg=" + avg +
                ", sd=" + sd +
                '}';
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getEsp() {
        return esp;
    }

    public void setEsp(String esp) {
        this.esp = esp;
    }

    public int getMinRssi() {
        return minRssi;
    }

    public void setMinRssi(int minRssi) {
        this.minRssi = minRssi;
    }

    public int getMaxRssi() {
        return maxRssi;
    }

    public void setMaxRssi(int maxRssi) {
        this.maxRssi = maxRssi;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public double getSd() {
        return sd;
    }

    public void setSd(double sd) {
        this.sd = sd;
    }
}
