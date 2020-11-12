package pl.michnam.app.core.model;

public class AreaItemList {
    private String name;
    private int avgRssi;
    private int minRssi;
    private int maxRssi;
    private boolean isChecked;
    private boolean bt;

    public AreaItemList(String name) {
        this.name = name;
        this.avgRssi = 0;
        this.minRssi = 0;
        this.maxRssi = 0;
    }

    public AreaItemList(String name, int avgRssi, int minRssi, int maxRssi, boolean isChecked, boolean bt) {
        this.name = name;
        this.avgRssi = avgRssi;
        this.minRssi = minRssi;
        this.maxRssi = maxRssi;
        this.isChecked = isChecked;
        this.bt = bt;
    }

    @Override
    public String toString() {
        return name + ", avg: " + avgRssi + " (" + minRssi + " to " + maxRssi + ")";
    }

    public boolean isBt() {
        return bt;
    }

    public void setBt(boolean bt) {
        this.bt = bt;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvgRssi() {
        return avgRssi;
    }

    public void setAvgRssi(int avgRssi) {
        this.avgRssi = avgRssi;
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
