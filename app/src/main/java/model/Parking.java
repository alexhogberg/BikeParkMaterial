package model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;


/**
 * Created by Alexander on 2015-09-14.
 */
public class Parking {

    private Date date;
    private int id;
    private LatLng position;

    public Parking(Date date, LatLng latlng) {
        this.date = date;
        this.position = latlng;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
