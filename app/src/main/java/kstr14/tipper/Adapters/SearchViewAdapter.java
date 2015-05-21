package kstr14.tipper.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.List;

import kstr14.tipper.R;

/**
 * Created by Kristine on 21-05-2015.
 */
public class SearchViewAdapter extends CursorAdapter {

    private List<String> items;
    private AutoCompleteTextView text;

    public SearchViewAdapter(Context context, Cursor cursor, List<String> items) {
        super(context, cursor, false);
        this.items = items;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        text.setText(items.get(cursor.getPosition()));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_item, parent, false);
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, items);
        text = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
        text.setThreshold(1);
        text.setAdapter(adapter);
        return view;
    }
}
