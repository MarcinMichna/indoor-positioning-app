package pl.michnam.app.core.view;

public class AreaItem {
    private String name;
    private String address;
    private int minRssi;
    private int maxRssi;
    private boolean isChecked;
    private boolean bt;

    public AreaItem(String name, boolean bt) {
        this.name = name;
        this.bt = bt;
        this.isChecked = true;
        this.minRssi = 0;
        this.maxRssi = 0;
    }

    public AreaItem(String name, String address, int minRssi, int maxRssi, boolean isChecked, boolean bt) {
        this.name = name;
        this.address = address;
        this.minRssi = minRssi;
        this.maxRssi = maxRssi;
        this.isChecked = isChecked;
        this.bt = bt;
    }

    @Override
    public String toString() {
        return name + ", (" + minRssi + " to " + maxRssi + ")";
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
}
