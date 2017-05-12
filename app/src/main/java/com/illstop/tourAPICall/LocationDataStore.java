package com.illstop.tourAPICall;

import com.illstop.data.DBManager;

public class LocationDataStore {

    public static DBManager dbManager;
    public static String locationName;//시
    public static String locality;//군+구

    public DBManager getDbManager() {
        return dbManager;
    }

    public void setDbManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }
}
