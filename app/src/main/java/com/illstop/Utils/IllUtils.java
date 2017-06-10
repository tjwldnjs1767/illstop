package com.illstop.Utils;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.IllnessManager.Engine.Element;
import com.IllnessManager.Engine.XMLParser;
import com.IllnessManager.Parse.QUERYTYPE;
import com.IllnessManager.Parse.Response;
import com.IllnessManager.Parse.Rest;
import com.illstop.R;

import java.net.URL;

/**
 * Created by jwqe764241 on 2017-05-30.
 */

public class IllUtils {

    public static int MAX_STATE_NUM = 7;
    public static int DISABLE_ALPHA = 40;

    public static Response getResponse(QUERYTYPE querytype, String areaNo){

        URL url;
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

    public static QUERYTYPE getEnumVal(int val){
        switch(val){
            case 0:
                return QUERYTYPE.TYPE_ASTHMALUNT;
            case 1:
                return QUERYTYPE.TYPE_WOODYFLOWER;
            case 2:
                return QUERYTYPE.TYPE_PINEFLOWER;
            case 3:
                return QUERYTYPE.TYPE_WEEDFLOWER;
            case 4:
                return QUERYTYPE.TYPE_INFLU;
            case 5:
                return QUERYTYPE.TYPE_STROKE;
            case 6:
                return QUERYTYPE.TYPE_SKIN;
        }

        return null;
    }

    public static void setActive(View view, int level){
        Log.d("PRINT--->", String.valueOf(level));

        ImageView imageView;

        switch(level){
            case 0:
                imageView = (ImageView)view.findViewById(R.id.tile_normal);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_high);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_veryhigh);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                break;
            case 1:
                imageView = (ImageView)view.findViewById(R.id.tile_low);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_high);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_veryhigh);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                break;
            case 2 :
                imageView = (ImageView)view.findViewById(R.id.tile_low);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_normal);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_veryhigh);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                break;
            case 3 :
                imageView = (ImageView)view.findViewById(R.id.tile_low);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_normal);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_high);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                break;
            case 5:
                imageView = (ImageView)view.findViewById(R.id.tile_low);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_normal);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_high);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                imageView = (ImageView)view.findViewById(R.id.tile_veryhigh);
                imageView.getDrawable().setAlpha(DISABLE_ALPHA);
                break;
        }
    }

}
