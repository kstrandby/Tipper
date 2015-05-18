package kstr14.tipper.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kstr14.tipper.Data.Group;
import kstr14.tipper.R;

/**
 * Created by Kristine on 17-05-2015.
 */
public class GroupBaseAdapter extends BaseAdapter {
    private Context context;
    private List<Group> groups;

    public GroupBaseAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Group getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_group_item, null);
        }

        TextView nameView = (TextView) view.findViewById(R.id.groupItem_tv_groupName);
        TextView locationView = (TextView) view.findViewById(R.id.groupItem_tv_location);
        ImageView imageView = (ImageView) view.findViewById(R.id.groupItem_iv_groupImage);
        nameView.setText(groups.get(position).getName());

        return view;
    }
}
