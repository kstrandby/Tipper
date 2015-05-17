package kstr14.tipper.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;

/**
 * Created by Kristine on 16-05-2015.
 */
public class TipBaseAdapter extends BaseAdapter {

    private Context context;
    private List<Tip> tips;

    public TipBaseAdapter(Context context, List<Tip> tips) {
        this.context = context;
        this.tips = tips;
    }

    @Override
    public int getCount() {
        return tips.size();
    }

    @Override
    public Tip getItem(int position) {
        return tips.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_tip_item, null);
        }

        TextView titleView = (TextView) view.findViewById(R.id.titleTextView);
        TextView locationView = (TextView) view.findViewById(R.id.locationTextView);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        TextView priceView = (TextView) view.findViewById(R.id.priceTextViewListItem);

        titleView.setText(tips.get(position).getTitle());
        priceView.setText("$" + tips.get(position).getPrice());

        return view;
    }
}
