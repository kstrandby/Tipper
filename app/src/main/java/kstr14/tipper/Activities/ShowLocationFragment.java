package kstr14.tipper.Activities;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kstr14.tipper.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShowLocationFragment extends Fragment {


    public ShowLocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_location, container, false);
    }


}
