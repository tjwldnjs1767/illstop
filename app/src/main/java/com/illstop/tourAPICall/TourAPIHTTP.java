package com.illstop.tourAPICall;

import com.illstop.data.GeoCoderConverter;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import Definition.OperationCode;
import Definition.OperationURL;

public class TourAPIHTTP {
    // API 호출, 두 번째 인자로 오퍼레이션 구분
    public Document getNearFestival(HashMap<String, String> paramMap, OperationCode operation) {
        if (paramMap == null || paramMap.size() == 0) {
            return null;
        }

        String parameters = "?";

        /* URL 파라미터 설정 */
        Set<String> keySet = paramMap.keySet();
        Iterator<String> iterator = keySet.iterator();

        while (iterator.hasNext()) {
            String paramKey = iterator.next();
            String paramValue = paramMap.get(paramKey);

            if (parameters.equals("?")) {
                parameters += paramKey + "=" + paramValue;
            } else {
                parameters += "&" + paramKey + "=" + paramValue;
            }
        }

        OutputStream outputStream = null;
        BufferedReader bufferedReader = null;
        Document document = null;

        /* API 호출 */
        try {
            String url = "";
            switch (operation) {
                case LOCATIONBASEDLIST:
                    url = OperationURL.LOCATIONBASEDLISTURL + parameters;
                    break;

                case SEARCHFESTIVAL:
                    url = OperationURL.SEARCHFESTIVAL + parameters;
                    break;

                case DETAILINTRO:
                    url = OperationURL.LOCATIONBASEDLISTURL + parameters;
                    break;

                case NAVERREVERSEGEOCODE:
                    url = OperationURL.NAVERREVERSEGEOCODE + parameters;
                    break;

                case UNKNOWN:
                    url = "";
                    break;
            }

            URL festivalAPIURL = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) festivalAPIURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Accept", "application/xml");
            if (operation == OperationCode.NAVERREVERSEGEOCODE) {
                httpURLConnection.setRequestProperty("X-Naver-Client-Id", GeoCoderConverter.getCLIENTID());
                httpURLConnection.setRequestProperty("X-Naver-Client-Secret", GeoCoderConverter.getSECRET());
            }
            InputStream xml = httpURLConnection.getInputStream();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(xml);

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return document;
    }
}
