package com.illstop.data;

import android.content.Context;

public class LocationDataStore {

    public static DBManager dbManager;
    public static String locationName;//시
    public static String locality;//군+구

    //이놈들아 코딩을 어떻게 하는거냐!! 니네 이상하다?
    //내가 왜 인자를 이렇게 받았을까??? 후...
    //너네 주먹으로 날려버린다...
    //TODO: 변경할 것;
    public DBManager getDbManager(Context context) {
        if(dbManager != null) {
            dbManager = new DBManager(context);
        }
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
