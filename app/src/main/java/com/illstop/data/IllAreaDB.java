package com.illstop.data;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class IllAreaDB {

    public static final String DBNAME = "area.db";
    public static final String PACKPATH = "/data/data/com.illstop/databases";

    private Connection m_Connection = null;
    private boolean m_bIsOpened = false;

    private File m_file = null;

    public IllAreaDB(File source) throws IllegalArgumentException {

        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }

        m_file = source;
    }

    private boolean open() {
        /*
		* 		URL 수정할것
		* */
        try {
            Class.forName("org.sqldroid.SQLDroidDriver");
            m_Connection = DriverManager.getConnection("jdbc:sqldroid:" + m_file);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        m_bIsOpened = true;
        return true;
    }

    private boolean close() {

        if (m_bIsOpened == false) {
            return true;
        }

            try {
            m_Connection.close();
            m_Connection = null;
            m_bIsOpened = false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //이미 있으면 -> true;
    //없으면 	  -> false;
    public boolean isValid() {

        open();

        try {
            DatabaseMetaData mt = m_Connection.getMetaData();
            ResultSet rs = mt.getTables(null, null, "%", null);
            while (rs.next()) {
                if (rs.getString(3).equals("Area")) {
                    close();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        close();
        return false;
    }


    public ArrayList<String> executeQuery(String query) {

        open();

        //Statement statement = null;
        //ResultSet resultSet = null;

        try {

            Statement statement = m_Connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            ArrayList<String> resultSink = new ArrayList<String>();

            while (resultSet.next()) {
                resultSink.add(resultSet.getString(1));
            }

            return resultSink;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        close();
        return null;
    }

    /*
    *   val1 -> 광역시, 특별시, 도
    *   val2 -> 시, 군, 구
    *   val3 -> 읍, 면, 동
     */
    public ArrayList<String> getAreaNo(String val1, String val2, String val3) throws IllegalArgumentException {

        //--Error Checking
        {
            if (!isValid())
                throw new IllegalArgumentException("source is invalid, Maybe have no Area.db at ../Source.Area.db");

            if (val1 == null)
                throw new IllegalArgumentException("val1 cannot be null");

            if ((val2 == null) && (val3 != null))
                throw new IllegalArgumentException("val2 cannot be null with notnull val3");
            //Error Checking--
        }

        ArrayList<String> resultSink = null;

        if (val2 == null) {
            StringBuilder builder = new StringBuilder();
            builder.append("Select AreaNo from Area where ")
                    .append("Area1=").append("\"").append(val1).append("\";");

            System.out.println(builder.toString());

            resultSink = executeQuery(builder.toString());
        } else if (val3 == null) {
            StringBuilder builder = new StringBuilder();
            builder.append("Select AreaNo from Area where ")
                    .append("Area1=").append("\"").append(val1).append("\" and ")
                    .append("Area2=").append("\"").append(val2).append("\";");

            resultSink = executeQuery(builder.toString());
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Select AreaNo from Area where ")
                    .append("Area1=").append("\"").append(val1).append("\" and ")
                    .append("Area2=").append("\"").append(val2).append("\" and ")
                    .append("Area3=").append("\"").append(val3).append("\";");

            resultSink = executeQuery(builder.toString());
        }

        return resultSink;
    }

    public static void copyDBFile(Context context) {
        File folder = new File(IllAreaDB.PACKPATH);
        folder.mkdirs();

        File outfile = new File(IllAreaDB.PACKPATH + "/" + IllAreaDB.DBNAME);

        if (outfile.length() <= 0) {
            AssetManager assetManager = context.getResources().getAssets();
            try {
                InputStream is = assetManager.open(IllAreaDB.DBNAME, AssetManager.ACCESS_BUFFER);
                long filesize = is.available();
                byte[] tempdata = new byte[(int) filesize];
                is.read(tempdata);
                is.close();
                outfile.createNewFile();
                FileOutputStream fo = new FileOutputStream(outfile);
                fo.write(tempdata);
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //--TestMethod
    public void printCurTable(File dest) {

        open();

        try {
            DatabaseMetaData mt = m_Connection.getMetaData();
            ResultSet rs = mt.getTables(null, null, "%", null);
            while (rs.next()) {
                System.out.println(rs.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        close();
    }

    public void printCurColumn(File dest) {

        open();

        try {
            Statement statement = m_Connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM AREA");
            ResultSetMetaData rsmd = rs.getMetaData();
            String name = rsmd.getColumnName(4);
            System.out.println(name);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        close();
    }
    //TestMethod--

}
