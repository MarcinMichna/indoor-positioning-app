package pl.michnam.app.core.view;

public class DeleteAreaItem {
    private String name;
    private boolean checked;

    public DeleteAreaItem(String name) {
        this.name = name;
        this.checked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
