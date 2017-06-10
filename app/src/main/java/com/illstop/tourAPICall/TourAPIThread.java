package com.illstop.tourAPICall;

import android.util.Log;

import com.illstop.data.LocationDataStore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import Definition.Festival;
import Definition.OperationCode;

/**
 * Created by Jeong Taegyun on 2017-04-10.
 */

/**
 * Thread Usage
 * 1. Create thread object
 * 2. Start thread
 * 3. Call join method
 * 4. Call getter method and receive festival ArrayList
 * 5. Set thread null
 */

public class TourAPIThread extends Thread {
    private static HashMap<String, String> defaultParamMap = null;
    private static HashMap<String, String> searchParamMap = null;

    private final static String SERVICEKEY = "mJOEcE4eFDUTJd8nBIasxDwAws4fIdm64UmPnM6qGSJCmLubYafK9MxfKDmp4P0G71WBf8RQC1a09ebXz%2Ffxig%3D%3D";

    private boolean run = true;

    private ArrayList<Festival> nearFestivals = null;

    public ArrayList<Festival> getNearFestivals() {
        return nearFestivals;
    }

    public static String getSERVICEKEY() {
        return SERVICEKEY;
    }

    public void run() {
        nearFestivals = new ArrayList<>();

        TourAPIHTTP tourAPIHTTP = new TourAPIHTTP();

        if (defaultParamMap == null)
            defaultParamMap = new HashMap<>();
        else
            defaultParamMap.clear();

        if (searchParamMap == null)
            searchParamMap = new HashMap<>();
        else
            searchParamMap.clear();

        Document festivalDocument = null;

        /* 가변 파라미터 변수 */
        int totalCount;
        int numOfRows = 100;
        int pageNo = 1;

        /* 날짜 관련 변수 */
        Date today = null;
        String tomorrow = null;
        Date festivalEndDate = null;
        Calendar t = Calendar.getInstance();
        t.add(Calendar.DATE, 1); // 내일 날짜 설정
        SimpleDateFormat tourAPIDateFormat = new SimpleDateFormat("yyyyMMdd"); // API 데이터 형식이 "yyyyMMdd"이므로 포맷 맞춰서 DATE로 변경

        /* 공통 파라미터 설정 */
        defaultParamMap.put("serviceKey", getSERVICEKEY());
        defaultParamMap.put("MobileOS", "AND");      // AND = ANDROID
        defaultParamMap.put("MobileApp", "Map");     // 서비스 이름 (Application Name)
        defaultParamMap.put("pageNo", String.valueOf(pageNo));

        /* searchFestival 파라미터 설정 */
        searchParamMap.putAll(defaultParamMap);
        searchParamMap.put("cat1", "A02");
        // searchParamMap.put("cat2", "A0207");

        /* 지역 코드, 시군구 코드, 축제 검색 날짜 파라미터 설정 */
        LocationDataStore locationDataStore = new LocationDataStore();
        String[] codes = locationDataStore.getDbManager().getCode(locationDataStore.getLocationName(), locationDataStore.getLocality());

        Log.d("checkCodes", codes[0] + " " + codes[1]);
        searchParamMap.put("areaCode", codes[0]);
        searchParamMap.put("sigunguCode", codes[1]);
        searchParamMap.put("numOfRows", String.valueOf(numOfRows));
        // 축제 시작 날짜와 종료 날짜를 오늘로 설정하면 진행 중인 축제만 검색 가능
        searchParamMap.put("eventStartDate", tourAPIDateFormat.format(Calendar.getInstance().getTime()));
        searchParamMap.put("eventEndDate", tourAPIDateFormat.format(Calendar.getInstance().getTime()));

        try { // "yyyyMMdd" 포맷에 맞춰 오늘 날짜 확인
            today = tourAPIDateFormat.parse(tourAPIDateFormat.format(Calendar.getInstance().getTime())); // 오늘 날짜
            tomorrow = tourAPIDateFormat.format(t.getTime());  // 내일 날짜, 축제 종료일이 없을 경우 설정한다
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // searchFestival 호출, Festival 객체 생성 및 데이터(아이디, 주소, 이미지 주소, X, Y, 번호, 축제 이름) 저장
        // 저장된 Festival 객체는 모두 진행 중인 축제 정보
        do {
            /* Get near ongoing festival
            s' information(address, img, title, etc.) */
            festivalDocument = tourAPIHTTP.getNearFestival(searchParamMap, OperationCode.SEARCHFESTIVAL);
            festivalDocument.getDocumentElement().normalize();

            totalCount = Integer.parseInt(festivalDocument.getElementsByTagName("totalCount").item(0).getTextContent());

            if (totalCount == 0) {
                break;
            }

            NodeList nodeList = festivalDocument.getElementsByTagName("item");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                /* 축제 정보에 이미지가 없을 경우 고정 이미지로 대체 */
                if (element.getElementsByTagName("firstimage").item(0) == null) {
                    Element tempElement = festivalDocument.createElement("firstimage");
                    tempElement.setTextContent("http://www.freeiconspng.com/uploads/no-image-icon-11.PNG");
                    element.appendChild(tempElement);
                }

                /* 축제 기간 정보 처리 */
                Element tempElement = festivalDocument.createElement("eventperiod");
                // 축제 종료일이 없는 경우 365일 진행하는 축제로 간주
                if (element.getElementsByTagName("eventenddate").item(0) == null) {
                    tempElement.setTextContent("365일 축제");
                } else {
                    // yyyyMMdd ~ yyyyMMdd 포맷으로 저장
                    tempElement.setTextContent(element.getElementsByTagName("eventstartdate").item(0).getTextContent() + " ~ "
                            + element.getElementsByTagName("eventenddate").item(0).getTextContent());
                }
                element.appendChild(tempElement);

                /* 축제 전화번호 정보가 두 개 이상일 경우 포함되는 '<br>' 제거 */
                String beforeTel = element.getElementsByTagName("tel").item(0).getTextContent();
                String tempTel[] = beforeTel.split("<br>");
                StringBuilder afterTel = new StringBuilder();
                for (int telSequence = 0; telSequence < tempTel.length; telSequence++){
                    afterTel.append(tempTel[telSequence] + " ");
                }

                Festival festival = new Festival(
                        Integer.parseInt(element.getElementsByTagName("contentid").item(0).getTextContent()),
                        element.getElementsByTagName("addr1").item(0).getTextContent(),
                        element.getElementsByTagName("firstimage").item(0).getTextContent(),
                        Double.parseDouble(element.getElementsByTagName("mapx").item(0).getTextContent()),
                        Double.parseDouble(element.getElementsByTagName("mapy").item(0).getTextContent()),
                        afterTel.toString(),
                        element.getElementsByTagName("title").item(0).getTextContent(),
                        element.getElementsByTagName("eventperiod").item(0).getTextContent()
                );

                nearFestivals.add(i, festival);
            }
            pageNo += 1;
        } while (totalCount > numOfRows * (pageNo - 1));
    }
}
