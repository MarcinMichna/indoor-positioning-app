package pl.michnam.app.core.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import pl.michnam.app.R;

public class AreaListItemAdapter extends ArrayAdapter<AreaItem> {
    public AreaListItemAdapter(@NonNull Context context, int resource, @NonNull List<AreaItem> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.area_list, parent, false);
        TextView txt = convertView.findViewById(R.id.itemText);
        CheckBox checkBox = convertView.findViewById(R.id.itemCheckbox);
        ImageView imageView = convertView.findViewById(R.id.imageView);

        AreaItem areaListItem = getItem(position);

        checkBox.setChecked(areaListItem.isChecked());

        if (areaListItem.isBt())
            imageView.setImageResource(R.drawable.ic_bluetooth);

        txt.setText(areaListItem.toString());

        checkBox.setOnClickListener(v -> {
            areaListItem.setChecked(!areaListItem.isChecked());
            checkBox.setChecked(areaListItem.isChecked());
        });
        return convertView;
    }
}
