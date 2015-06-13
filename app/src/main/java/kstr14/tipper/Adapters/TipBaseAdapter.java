package kstr14.tipper.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseImageView;

import java.util.List;

import kstr14.tipper.Data.Tip;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.MapsHelper;
import kstr14.tipper.R;

/**
 * Created by Kristine on 16-05-2015.
 */
public class TipBaseAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView titleView;
        TextView locationView;
        ParseImageView imageView;
        TextView priceView;
    }

    private static final String TAG = "TipBaseAdapter";

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

        ViewHolder viewHolder;

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_tip_item, null);

            viewHolder = new ViewHolder();
            viewHolder.titleView = (TextView) view.findViewById(R.id.titleTextView);
            viewHolder.locationView = (TextView) view.findViewById(R.id.locationTextView);
            viewHolder.imageView = (ParseImageView) view.findViewById(R.id.imageView);
            viewHolder.priceView = (TextView) view.findViewById(R.id.priceTextViewListItem);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.titleView.setText(tips.get(position).getTitle());
        viewHolder.priceView.setText("$" + tips.get(position).getPrice());

        ParseFile parseImg = tips.get(position).getImage();
        if(parseImg != null) {
            viewHolder.imageView.setPlaceholder(context.getResources().getDrawable(R.drawable.food));
            viewHolder.imageView.setParseFile(parseImg);
            viewHolder. imageView.loadInBackground();
        } else {
            Bitmap img = null;
            String category = tips.get(position).getCategory();
            if (category.equals("Food")) {
                img = ImageHelper.decodeBitmapFromResource(context.getResources(), R.drawable.food, 128, 128);
                viewHolder.imageView.setImageBitmap(Bitmap.createScaledBitmap(img, 128, 128, false));
            } else if (category.equals("Drinks")) {
                img = ImageHelper.decodeBitmapFromResource(context.getResources(), R.drawable.drinks, 128, 128);
                viewHolder.imageView.setImageBitmap(Bitmap.createScaledBitmap(img, 128, 128, false));
            } else if (category.equals("Other")) {
                img = ImageHelper.decodeBitmapFromResource(context.getResources(), R.drawable.other, 128, 128);
                viewHolder.imageView.setImageBitmap(Bitmap.createScaledBitmap(img, 128, 128, false));
            }
        }

        ParseGeoPoint geoPoint = tips.get(position).getLocation();
        if(geoPoint != null) {
            LatLng latLng = MapsHelper.getLatLngFromParseGeoPoint(geoPoint);
            String address = MapsHelper.getAddressFromLatLng(latLng, context);
            viewHolder.locationView.setText(address);
        } else {
            viewHolder.locationView.setText("Location unknown");
        }

        return view;
    }
}
