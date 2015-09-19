package parking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alexhogberg.android.R;

import java.util.ArrayList;

import model.Parking;

/**
 * Created by Alexander on 2015-09-19.
 */
public class ParkingAdapter extends ArrayAdapter<Parking> {
    public ParkingAdapter(Context context, ArrayList<Parking> parkings) {
        super(context, 0, parkings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Parking parking = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.parking_item, parent, false);
        }
        // Lookup view for data population
        TextView parkingDate = (TextView) convertView.findViewById(R.id.parkingDate);
        TextView parkingPos = (TextView) convertView.findViewById(R.id.parkingPos);
        // Populate the data into the template view using the data object
        parkingDate.setText(parking.getDate().toString());
        parkingPos.setText(parking.getPosition().toString());
        // Return the completed view to render on screen
        return convertView;
    }
}