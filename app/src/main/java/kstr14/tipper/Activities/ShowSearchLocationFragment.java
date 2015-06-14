package kstr14.tipper.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import kstr14.tipper.R;

/**
 * Fragment showing EditText to allow user to search for location
 */
public class ShowSearchLocationFragment extends Fragment {

    private EditText addressInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_search_location, container, false);
        addressInput = (EditText) view.findViewById(R.id.showSearchLocation_ed_address);
        // Inflate the layout for this fragment
        return view;
    }

    public EditText getAddressInput() {
        return addressInput;
    }
}
