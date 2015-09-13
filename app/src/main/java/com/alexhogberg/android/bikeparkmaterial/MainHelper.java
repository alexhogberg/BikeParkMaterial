package com.alexhogberg.android.bikeparkmaterial;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Alexander on 2015-09-13.
 */
public class MainHelper {

    private Context mContext;
    private PackageManager pm;

    public MainHelper(Context ctx) {
        this.mContext = ctx;
        this.pm = ctx.getApplicationContext().getPackageManager();
    }
    /**
     * Get the saved preferences from previous locations
     *
     * @return a hashmap containing where and when the last parking occured
     */
    public HashMap<String, Object> getSavedPrefs() {
        SharedPreferences settings = mContext.getSharedPreferences("POSITIONS", 0);
        HashMap<String, Object> returnMap = new HashMap<String, Object>();

        returnMap.put("lat",
                Double.longBitsToDouble(settings.getLong("latitude", 0)));
        returnMap.put("lon",
                Double.longBitsToDouble(settings.getLong("longitude", 0)));
        returnMap.put("date", settings.getLong("date", 0));
        return returnMap;
    }

    /**
     * Saves the position in the local phone settings
     *
     * @param lat latitude to save
     * @param lon longitude to save
     */
    public void setSavedPrefs(double lat, double lon) {
        Date d = new Date();
        SharedPreferences settings = mContext.getSharedPreferences("POSITIONS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("latitude", Double.doubleToLongBits(lat));
        editor.putLong("longitude", Double.doubleToLongBits(lon));
        editor.putLong("date", d.getTime());

        // Commit the edits!
        editor.commit();
    }

    /**
     * Removes all saved position (used with the reset-button)
     */
    public void removeSavedPrefs() {
        SharedPreferences settings = mContext.getSharedPreferences("POSITIONS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();

        editor.commit();
    }

    public boolean hasCorrectPermissions() {
        int hasPerm = pm.checkPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                mContext.getApplicationContext().getPackageName());

        if(hasPerm == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }
}
