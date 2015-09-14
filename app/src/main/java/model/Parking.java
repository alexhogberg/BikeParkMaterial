package model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by Alexander on 2015-09-14.
 */
public class Parking {

    private Date date;
    private LatLng position;

    public Parking(Date date, LatLng latlng) {
        this.date = date;
        this.position = latlng;
    }
}
