package kstr14.tipper.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import kstr14.tipper.Data.Tip;
import kstr14.tipper.R;

/**
 * Created by Kristine on 09-05-2015.
 */
public class TipAdapter extends ParseQueryAdapter<Tip> {

    public TipAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<Tip>() {
            public ParseQuery<Tip> create() {
                // configure parse query to displaying all tips
                ParseQuery query = new ParseQuery("Tip");
                return query;
            }
        });

    }

    @Override
    public View getItemView(Tip tip, View view, ViewGroup parent) {

        if (view == null) {
            view = View.inflate(getContext(), R.layout.list_tip_item, null);
        }

        super.getItemView(tip, view, parent);

        /*
        ParseImageView mealImage = (ParseImageView) v.findViewById(R.id.icon);
        ParseFile photoFile = meal.getParseFile("photo");
        if (photoFile != null) {
            mealImage.setParseFile(photoFile);
            mealImage.loadInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    // nothing to do
                }
            });
        }
        */

        TextView titleView = (TextView) view.findViewById(R.id.titleTextView);
        TextView locationView = (TextView) view.findViewById(R.id.locationTextView);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        TextView priceView = (TextView) view.findViewById(R.id.priceTextViewListItem);

        titleView.setText(tip.getTitle());
        priceView.setText("$" + tip.getPrice());
        return view;
    }
}
