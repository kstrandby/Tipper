package kstr14.tipper.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import kstr14.tipper.R;

/**
 * Adapter for SearchView
 * Each item in list has a TextView containing the suggestion
 */
public class SearchViewAdapter extends CursorAdapter {

    private List<String> items;
    private TextView text;

    public SearchViewAdapter(Context context, Cursor cursor, List<String> items) {
        super(context, cursor, false);
        this.items = items;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        text.setText(items.get(cursor.getPosition()));
        text.setFocusable(true);
        text.setClickable(true);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_item, parent, false);
        text = (TextView) view.findViewById(R.id.textView);
        return view;
    }
}
