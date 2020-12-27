package pl.michnam.app.core.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import pl.michnam.app.R;
import pl.michnam.app.http.RequestManager;
import pl.michnam.app.core.view.DeleteAreaAdapter;
import pl.michnam.app.core.view.DeleteAreaItem;
import pl.michnam.app.sql.DbManager;

public class DeleteAreaActivity extends AppCompatActivity {

    private ListView listView;

    private DeleteAreaAdapter listAdapter;

    private ArrayList<DeleteAreaItem> areasArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_area);
        initView();
        initList();
    }

    private void initView() {
        listView = findViewById(R.id.deleteListView);
    }

    private void initList() {
        DbManager dbManager = new DbManager(this);
        ArrayList<String> areas = dbManager.getAreasList();

        for(String area: areas)
            areasArray.add(new DeleteAreaItem(area));

        listAdapter = new DeleteAreaAdapter(this, R.layout.activity_delete_area, areasArray);
        listView.setAdapter(listAdapter);
    }

    public void onDeleteClicked(View v) {
        DbManager dbManager = new DbManager(this);
        ArrayList<String> areasToRemove = new ArrayList<>();

        for (DeleteAreaItem area: areasArray)
            if (area.isChecked()) areasToRemove.add(area.getName());

        if (areasToRemove.size() > 0) {
            dbManager.deleteAreas(this, areasToRemove);
            onBackPressed();
        }
        else
            Toast.makeText(this, getString(R.string.none_areas_selected), Toast.LENGTH_SHORT).show();

        RequestManager requestManager = new RequestManager(this);
        requestManager.updateWatchedDevices(dbManager.watchedDevicesWifi(), dbManager.watchedDevicesBt());

    }

    public void onCancelClicked(View v) {
        onBackPressed();
    }


}