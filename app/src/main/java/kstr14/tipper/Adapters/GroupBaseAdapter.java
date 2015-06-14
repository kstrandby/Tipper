package kstr14.tipper.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseFile;
import com.parse.ParseImageView;

import java.util.List;

import kstr14.tipper.Data.Group;
import kstr14.tipper.ImageHelper;
import kstr14.tipper.R;

/**
 * Adapter used for showing lists of Groups
 * Each ListItem shows the name of the group and the Image of the group
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
        ParseImageView imageView = (ParseImageView) view.findViewById(R.id.groupItem_iv_groupImage);
        nameView.setText(groups.get(position).getName());

        ParseFile parseImg = groups.get(position).getImage();
        if(parseImg != null) {
            imageView.setPlaceholder(context.getResources().getDrawable(R.drawable.ic_action_group_big));
            imageView.setParseFile(parseImg);
            imageView.loadInBackground();
        } else {
            Bitmap img = ImageHelper.decodeBitmapFromResource(context.getResources(), R.drawable.ic_action_group_big, 128, 128);
            imageView.setImageBitmap(Bitmap.createScaledBitmap(img, 100, 100, false));
        }
        return view;
    }
}
