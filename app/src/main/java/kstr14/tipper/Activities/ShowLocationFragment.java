package kstr14.tipper.Activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import kstr14.tipper.R;

/**
 * Fragment to show a location using a box containing TextView
 */
public class ShowLocationFragment extends Fragment {

    private TextView locationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_location, container, false);
        locationView = (TextView) view.findViewById(R.id.showLocation_tv_location);
        return view;
    }

    public TextView getLocationView() {
        return locationView;
    }

    /**
     * necessary as the fragment view is not created instantly - when onResume is called, the
     * fragment is ready and we can initialize the TextView
     */
    @Override
    public void onResume() {
        super.onResume();
        ((MapsActivity)getActivity()).updateAddress();
    }
}
