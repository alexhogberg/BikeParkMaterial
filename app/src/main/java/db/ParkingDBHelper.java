package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import model.Parking;

/**
 * Created by Alexander on 2015-09-19.
 */
public class ParkingDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Stuff";
    private static final int DATABASE_VERSION = 2;
    private static final String PARKING_TABLE_NAME = "parking";

    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "date";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";

    public ParkingDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String CREATE_PARKING_TABLE = "CREATE TABLE " + PARKING_TABLE_NAME +
                "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_DATE + " int, " +
                KEY_LAT + " double, " + KEY_LON + " double)";
        db.execSQL(CREATE_PARKING_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int ext1, int ext2) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + PARKING_TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void addParking(Parking parking) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, persistDate(parking.getDate())); // Parking Date
        Log.d("DATE","ADDING DATE: " + parking.getDate().toString());
        values.put(KEY_LAT, parking.getPosition().latitude); // Parking Latitude
        values.put(KEY_LON, parking.getPosition().longitude); // Parking Longitude

        // Inserting Row
        db.insert(PARKING_TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    public Parking getParking(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(PARKING_TABLE_NAME, new String[]{KEY_ID,
                        KEY_DATE, KEY_LAT, KEY_LON}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        LatLng position = new LatLng(cursor.getDouble(1), cursor.getDouble(2));
        Parking contact = new Parking(loadDate(cursor,0),position);
        // return contact
        return contact;
    }

    // Getting All Contacts
    public List<Parking> getAllParkings() {
        List<Parking> parkingList = new ArrayList<Parking>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + PARKING_TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LatLng position = new LatLng(cursor.getDouble(2), cursor.getDouble(3));
                Parking parking = new Parking(loadDate(cursor, 1), position);
                parking.setId(Integer.parseInt(cursor.getString(0)));
                // Adding contact to list
                parkingList.add(parking);
            } while (cursor.moveToNext());
        }

        // return contact list
        return parkingList;
    }

    // Getting contacts Count
    public int getParkingsCount() {
        String countQuery = "SELECT  * FROM " + PARKING_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
    // Updating single contact
    public int updateParking(Parking parking) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, parking.getDate().toString()); // Parking Date
        values.put(KEY_LAT, parking.getPosition().latitude); // Parking Latitude
        values.put(KEY_LON, parking.getPosition().longitude); // Parking Longitude

        // updating row
        return db.update(PARKING_TABLE_NAME, values, KEY_ID + " = ?",
                new String[]{String.valueOf(parking.getId())});
    }

    // Deleting single contact
    public void deleteParking(Parking parking) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PARKING_TABLE_NAME, KEY_ID + " = ?",
                new String[]{String.valueOf(parking.getId())});
        db.close();
    }

    public static Long persistDate(Date date) {
        if (date != null) {
            return date.getTime();
        }
        return null;
    }

    public static Date loadDate(Cursor cursor, int index) {
        if (cursor.isNull(index)) {
            return null;
        }
        return new Date(cursor.getLong(index));
    }
}
