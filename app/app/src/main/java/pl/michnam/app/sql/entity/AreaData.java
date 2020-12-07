package pl.michnam.app.sql.entity;

public class AreaData {
    private int id;
    private String areaName;
    private String name;
    private String address;
    private String type;
    private int minRssi;
    private int maxRssi;
    private double avg;
    private double sd;


    public AreaData(int id, String name, String address, String type, int minRssi, int maxRssi, String areaName, double avg, double sd) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.type = type;
        this.minRssi = minRssi;
        this.maxRssi = maxRssi;
        this.areaName = areaName;
        this.avg = avg;
        this.sd = sd;
    }

    @Override
    public String toString() {
        return "AreaData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", type='" + type + '\'' +
                ", minRssi=" + minRssi +
                ", maxRssi=" + maxRssi +
                ", areaName='" + areaName + '\'' +
                ", avg=" + avg +
                ", sd=" + sd +
                '}';
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
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
