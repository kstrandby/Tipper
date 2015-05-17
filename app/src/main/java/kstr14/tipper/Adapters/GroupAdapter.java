package kstr14.tipper.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import kstr14.tipper.Data.Group;
import kstr14.tipper.R;

/**
 * Created by Kristine on 14-05-2015.
 */
public class GroupAdapter extends ParseQueryAdapter<Group> {

    public GroupAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<Group>() {
            public ParseQuery<Group> create() {
                ParseQuery query = new ParseQuery("Group");
                return query;
            }
        });
    }

    @Override
    public View getItemView(Group group, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = View.inflate(getContext(), R.layout.list_group_item, null);
        }

        super.getItemView(group, view, viewGroup);

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

        TextView nameView = (TextView) view.findViewById(R.id.groupItem_tv_groupName);
        TextView locationView = (TextView) view.findViewById(R.id.groupItem_tv_location);
        ImageView imageView = (ImageView) view.findViewById(R.id.groupItem_iv_groupImage);
        nameView.setText(group.getName());
        return view;
    }
}
