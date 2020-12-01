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

import java.util.ArrayList;

import pl.michnam.app.R;

public class DeleteAreaAdapter extends ArrayAdapter<DeleteAreaItem> {
    public DeleteAreaAdapter(@NonNull Context context, int resource, @NonNull ArrayList<DeleteAreaItem> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.delete_area_list, parent, false);

        TextView txt = convertView.findViewById(R.id.deleteItemText);
        CheckBox checkBox = convertView.findViewById(R.id.deleteItemCheckbox);

        DeleteAreaItem item = getItem(position);

        checkBox.setChecked(item.isChecked());
        txt.setText(item.getName());

        checkBox.setOnClickListener(v -> {
            item.setChecked(!item.isChecked());
            checkBox.setChecked(item.isChecked());
        });

        return convertView;
    }
}
