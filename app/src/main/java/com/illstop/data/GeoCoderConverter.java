package com.illstop.data;

import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import com.illstop.activity.MainActivity;
import com.illstop.tourAPICall.TourAPIHTTP;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Definition.OperationCode;

public class GeoCoderConverter {
    private MainActivity mainActivity;
    private double latitude, longitude;

    private static final String CLIENTID = "JsKHQRWDhiS59MEOcFil";
    private static final String SECRET = "gTBXnPGIiv";

    public static String getCLIENTID(){return CLIENTID;}
    public static String getSECRET(){return SECRET;}

    public GeoCoderConverter(MainActivity mainActivity, double latitude, double longitude) {
        this.mainActivity = mainActivity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // TourAPIHTTP 클래스 이용해서 네이버 API 호출
    public class RequestReverseGeocode extends Thread{
        private ArrayList<String> geocode;

        RequestReverseGeocode(){geocode = new ArrayList<>();}

        public ArrayList<String> getGeocode(){return geocode;}

        @Override
        public void run() {
            TourAPIHTTP tourAPIHTTP = new TourAPIHTTP();

            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("query", "" + longitude + "," + latitude);

            Document document = tourAPIHTTP.getNearFestival(parameters, OperationCode.NAVERREVERSEGEOCODE);
            document.normalize();

            // 시도와 시군구 정보는 같으므로 여러 개 주소 정보 중 첫 번째 아이템만 가지고 온다
            Element element = (Element) document.getElementsByTagName("item").item(0);

            if (element != null){
                geocode.add(0, element.getElementsByTagName("sido").item(0).getTextContent()); // 광역시, 도 정보
                String sigungu = element.getElementsByTagName("sigugun").item(0).getTextContent().split(" ")[0];
                geocode.add(1, sigungu); // 시, 군, 구 정보
            }
        }
    }

    public ArrayList<String> requestReverseGeocode(){
        RequestReverseGeocode requestReverseGeocode = new RequestReverseGeocode();
        requestReverseGeocode.start();

        try{
            requestReverseGeocode.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        return requestReverseGeocode.getGeocode();
    }

    public void handlingGeocoder() {

        Geocoder geocoder = new Geocoder(mainActivity);

        ArrayList<String> addresses = requestReverseGeocode();

        /*
        try {
            addresses = geocoder.getFromLocation(
                    this.latitude,
                    this.longitude,
                    1);
        } catch (IOException ioException) {
            Toast.makeText(mainActivity, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(mainActivity, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
        }
        */

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(mainActivity, "주소 미발견", Toast.LENGTH_LONG).show();

        } else {
            final DBManager dbManager = new DBManager(mainActivity);
            LocationDataStore locationDataStore = new LocationDataStore();
            locationDataStore.setDbManager(dbManager);
            locationDataStore.setLocationName(addresses.get(0));
            locationDataStore.setLocality(addresses.get(1));
        }
    }
}
