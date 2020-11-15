package pl.michnam.app.core.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import pl.michnam.app.R;
import pl.michnam.app.core.view.AreaItemList;

public class AreaListItemAdapter extends ArrayAdapter<AreaItemList> {
    public AreaListItemAdapter(@NonNull Context context, int resource, @NonNull List<AreaItemList> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AreaItemList areaListItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.area_list, parent, false);
        }
        TextView txt = convertView.findViewById(R.id.itemText);
        CheckBox checkBox = convertView.findViewById(R.id.itemCheckbox);
        txt.setText(areaListItem.toString());
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                areaListItem.setChecked(!areaListItem.isChecked());
            }
        });
        return convertView;
    }
}
