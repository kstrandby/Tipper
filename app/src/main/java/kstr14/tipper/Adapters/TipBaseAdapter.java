package kstr14.tipper.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.List;

import kstr14.tipper.BitmapHelper;
import kstr14.tipper.Data.Category;
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
        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        TextView priceView = (TextView) view.findViewById(R.id.priceTextViewListItem);

        titleView.setText(tips.get(position).getTitle());
        priceView.setText("$" + tips.get(position).getPrice());
        Bitmap img = null;
        tips.get(position).getCategoriesRelation().getQuery().findInBackground(new FindCallback<Category>() {
            @Override
            public void done(List<Category> list, ParseException e) {
                if (list.get(0).getName().equals("food")) {
                    Bitmap img = BitmapHelper.decodeBitmapFromResource(context.getResources(), R.drawable.food, 128, 128);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(img, 100, 100, false));
                } else if (list.get(0).getName().equals("drinks")) {
                    Bitmap img = BitmapHelper.decodeBitmapFromResource(context.getResources(), R.drawable.drinks, 128, 128);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(img, 100, 100, false));
                } else if (list.get(0).getName().equals("other")) {
                    Bitmap img = BitmapHelper.decodeBitmapFromResource(context.getResources(), R.drawable.other, 128, 128);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(img, 100, 100, false));
                }
            }
        });

        return view;
    }
}