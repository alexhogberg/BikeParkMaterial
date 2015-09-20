package parking;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alexhogberg.android.R;

import java.util.ArrayList;

import db.ParkingDBHelper;
import model.Parking;

/**
 * Created by Alexander on 2015-09-14.
 */
public class ParkingFragment extends Fragment {

    private ParkingDBHelper db;

    public ParkingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new ParkingDBHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_parking, container, false);

        ArrayList<Parking> parkings = (ArrayList) db.getAllParkings();
        // Construct the data source
        // Create the adapter to convert the array to views
        ParkingAdapter adapter = new ParkingAdapter(getContext(), parkings);
        // Attach the adapter to a ListView
        ListView listView = (ListView) rootView.findViewById(R.id.parkings);
        listView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}