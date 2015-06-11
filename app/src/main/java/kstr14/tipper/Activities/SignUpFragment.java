package kstr14.tipper.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import kstr14.tipper.ImageHelper;
import kstr14.tipper.R;

public class SignUpFragment extends Fragment {

    private ImageView tipper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        tipper = (ImageView) view.findViewById(R.id.app_logo);
        Bitmap img = ImageHelper.decodeBitmapFromResource(getResources(), R.drawable.tipper, 256, 256);
        tipper.setImageBitmap(img);
        return view;
    }
}
