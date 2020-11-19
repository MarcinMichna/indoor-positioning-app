package pl.michnam.app.sql.entity;

public class AreaData {
    private int id;
    private String name;
    private String address;
    private String type;
    private int minRssi;
    private int maxRssi;
    private String areaName;

    public AreaData(int id, String name, String address, String type, int minRssi, int maxRssi, String areaName) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.type = type;
        this.minRssi = minRssi;
        this.maxRssi = maxRssi;
        this.areaName = areaName;
    }

    @Override
    public String toString() {
        return "AreaData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", minRssi=" + minRssi +
                ", maxRssi=" + maxRssi +
                ", areaName='" + areaName + '\'' +
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
}
