package com.illstop.Utils;

import android.widget.TextView;

import com.IllnessManager.Engine.Element;
import com.IllnessManager.Engine.XMLParser;
import com.IllnessManager.Parse.QUERYTYPE;
import com.IllnessManager.Parse.Response;
import com.IllnessManager.Parse.Rest;

import java.net.URL;

/**
 * Created by jwqe764241 on 2017-05-30.
 */

public class IllTextUtils {

    public static Response getResponse(QUERYTYPE querytype, String areaNo){

        URL url = null;
        Response response = null;

        try{
            url = new URL(Rest.getQueryURL(querytype, Rest.SERVICEKEY, areaNo));
            System.out.println(Rest.sendRequest(url));

            Element[] a = XMLParser.getElements(Rest.sendRequest(url)).getSink();

            response = Rest.getResponse(XMLParser.getElements(Rest.sendRequest(url)));
        } catch (Exception e){
            e.printStackTrace();
        }

        return response;
    }


    public static StringBuilder makeText(Response response){

        StringBuilder sb = new StringBuilder();

        sb.append("오늘 ");
        sb.append(getType(response.getQueryCode()));
        sb.append("보건기상지수는 ");
        sb.append(response.getTodayLevel());
        sb.append(" 이고 내일은 ");
        sb.append(response.getTomorrowLevel());
        sb.append("로 예상됩니다.");

        return sb;
    }


    public static void setText(TextView tv, StringBuilder sb){
        tv.setText(sb.toString());
    }


    public static String getType(int typeCode){
        /*
        switch(querytype){
            case TYPE_ASTHMALUNT:
                return "천식폐질환가능지수";
            case TYPE_WOODYFLOWER:
                return "꽃가루농도위험지수(수목류)";
            case TYPE_PINEFLOWER:
                return "꽃가루농도위험지수(소나무류)";
            case TYPE_WEEDFLOWER:
                return "꽃가루농도위험지수(잡초류)";
            case TYPE_INFLU:
                return "감기지수";
            case TYPE_STROKE:
                return "뇌졸중가능지수";
            case TYPE_SKIN:
                return "피부질환가능지수";
        }
        */

        switch(typeCode){
            case 0:
                return "천식폐질환가능지수";
            case 1:
                return "꽃가루농도위험지수(수목류)";
            case 2:
                return "꽃가루농도위험지수(소나무류)";
            case 3:
                return "꽃가루농도위험지수(잡초류)";
            case 4:
                return "감기지수";
            case 5:
                return "뇌졸중가능지수";
            case 6:
                return "피부질환가능지수";
        }

        return null;
    }

}
