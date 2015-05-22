package kstr14.tipper.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import kstr14.tipper.Data.Group;
import kstr14.tipper.R;

/**
 * Created by Kristine on 21-05-2015.
 */
public class SpinnerGroupAdapter extends ArrayAdapter<Group> {

    private Context context;
    private List<Group> values;

    public SpinnerGroupAdapter(Context context, int resource, List<Group> objects) {
        super(context, resource, objects);
        this.context = context;
        this.values = objects;
        super.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
    }

    public int getCount(){
        return values.size();
    }

    public Group getItem(int position){
        return values.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = View.inflate(getContext(), R.layout.simple_spinner_item, null);
        }
        TextView label = (TextView) view.findViewById(R.id.spinner_item_text);
        label.setText(values.get(position).getName());

        return label;
    }


    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = View.inflate(getContext(), R.layout.simple_spinner_dropdown_item, null);
        }
        TextView label = (TextView) view.findViewById(R.id.spinner_dropdown_item_text);
        label.setText(values.get(position).getName());

        return label;
    }
}
