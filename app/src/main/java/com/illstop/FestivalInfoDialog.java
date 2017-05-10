package com.illstop;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


import Definition.Festival;


public class FestivalInfoDialog extends DialogFragment {
    View festivalImg;
    Bitmap bitmap;
    Thread t;

    TextView telBtn, addrTv, periodTv, remainingDistanceTv, titleTv;
    Button finishBtn;

    String remainingDistance = "100m";
    ArrayList<Festival> festivalItems;

    int markerIndex;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_festival_info, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        initView(v);

        initMarkerIndex();

        setContentToView();

        return v;
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }

    private void initView(View v) {
        telBtn = (TextView) v.findViewById(R.id.festival_tel);
        addrTv = (TextView) v.findViewById(R.id.festival_addr);
        periodTv = (TextView) v.findViewById(R.id.festival_end_day);
        finishBtn = (Button) v.findViewById(R.id.end_btn);
        festivalImg = v.findViewById(R.id.festival_img);
        remainingDistanceTv = (TextView) v.findViewById(R.id.remaining_distance);
        titleTv = (TextView) v.findViewById(R.id.festival_title);
        titleTv.setSelected(true);
        addrTv.setSelected(true);
        remainingDistanceTv.setPaintFlags(remainingDistanceTv.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
    }

    private void initMarkerIndex() {

        festivalItems = ((MainActivity) getActivity()).festivalItems;

        for (int i = 0; i < festivalItems.size(); i++) {
            if (((MainActivity) getActivity()).contentId == festivalItems.get(i).getContentid()) {
                markerIndex = i;
                break;
            }
        }
    }

    private void setContentToView() {

        titleTv.setText(festivalItems.get(markerIndex).getTitle());
        addrTv.setText(festivalItems.get(markerIndex).getAddr1());

        setTelBtn(festivalItems.get(markerIndex).getTel());
        telBtn.setOnClickListener(clickTelBtn);

        periodTv.setText(String.valueOf(festivalItems.get(markerIndex).getFestivalPeriod()));

        finishBtn.setOnClickListener(clickFinishBtn);

        setImage();
    }

    private View.OnClickListener clickTelBtn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setPhoneNumToDial("tel:" + festivalItems.get(markerIndex).getTel());
        }
    };

    private View.OnClickListener clickFinishBtn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    private void setTelBtn(String tel) {
        String[] telArr = null;
        if (tel.contains(",")) {
            telArr = tel.split(", ");

        } else if (tel.contains("~")) {
            telArr = tel.split("~");
        } else {
            telBtn.setText(tel);
            return;
        }
        telBtn.setText(telArr[0]);
    }

    private void setImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    remainingDistanceTv.setText(remainingDistance);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    bitmap = getBitmap(festivalItems.get(markerIndex).getFirstImage());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bitmap != null) {

                        if (getActivity() == null) {
                            return;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @SuppressLint("NewApi")
                            public void run() {
                                ((ImageView) festivalImg).setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private Bitmap getBitmap(String URL) {
        URL url;
        HttpURLConnection connection = null;
        InputStream inputStream;

        Bitmap retBitmap = null;

        try {
            url = new URL(URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            inputStream = connection.getInputStream();
            retBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            return retBitmap;
        }
    }


    private void setPhoneNumToDial(String phoneNum) {
        try {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(phoneNum));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}