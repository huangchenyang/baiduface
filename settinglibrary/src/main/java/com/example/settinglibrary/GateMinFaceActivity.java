package com.example.settinglibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.example.datalibrary.activity.BaseActivity;
import com.example.datalibrary.utils.PWTextUtils;

import java.math.BigDecimal;


/**
 * author : shangrog
 * date : 2019/5/27 6:34 PM
 * description :最小人脸界面
 */

public class GateMinFaceActivity extends BaseActivity {
    private EditText mfEtAmount;
    private int minimumFace;
    private int thirty = 30;
    private int twoHundered = 200;
    private static final int TEN = 10;
    private float faceThreshold;
    private float thirtyLevel = 0.3f;
    private float twoHunderedLevel = 0.8f;
    private static final float TEN_LEVEL  = 0.1f;

    private LinearLayout linerMinFace;
    private TextView minFaceText;
    private Button minFace;
    private String tagMsg = "";
    private ViewGroup minRepresent;
    private ViewGroup faceThresholdGroup;
    private EditText mfEtFaceThreshold;
    private Button minFaceThreshold;
    private int showWidth;
    private int showXLocation;

    private BigDecimal faceThresholdDecimal;
    private BigDecimal levelValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_minface);

        init();
    }


    public void init() {
        Intent intent = getIntent();
        levelValue = new BigDecimal(TEN_LEVEL  + "");
        minimumFace = intent.getIntExtra("minimumFace" , 0);
        faceThreshold = intent.getFloatExtra("faceThreshold" , 0);
        minRepresent = findViewById(R.id.minRepresent);

        linerMinFace = findViewById(R.id.linerminface);
        minFaceText = findViewById(R.id.minFaceText);
        minFace = findViewById(R.id.minface);

        ImageView mfDecrease = findViewById(R.id.mf_Decrease);
        ImageView mfIncrease = findViewById(R.id.mf_Increase);
        mfEtAmount = findViewById(R.id.mf_etAmount);
        ImageView mfSave = findViewById(R.id.mf_save);
        mfEtAmount.setText(minimumFace + "");
        // 人脸置信度
        faceThresholdGroup = findViewById(R.id.faceThreshold);
        mfEtFaceThreshold = findViewById(R.id.mf_et_face_threshold);
        ImageView mfFaceThreshold = findViewById(R.id.mf_face_threshold);
        ImageView mfPlusFaceThreshold = findViewById(R.id.mf_plus_face_threshold);
        minFaceThreshold = findViewById(R.id.min_face_threshold);
        mfEtFaceThreshold.setText(faceThreshold + "");

        PWTextUtils.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                minFace.setBackground(getDrawable(R.mipmap.icon_setting_question));
                minFaceThreshold.setBackground(getDrawable(R.mipmap.icon_setting_question));
            }
        });
        // 人脸置信度 增减按钮
        mfFaceThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (faceThreshold > thirtyLevel && faceThreshold <= twoHunderedLevel) {
                    faceThresholdDecimal = new BigDecimal(faceThreshold + "");
                    faceThreshold = faceThresholdDecimal.subtract(levelValue).floatValue();
                    mfEtFaceThreshold.setText(faceThreshold + "");
                }
            }
        });
        mfPlusFaceThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (faceThreshold >= thirtyLevel && faceThreshold < twoHunderedLevel) {
                    faceThresholdDecimal = new BigDecimal(faceThreshold + "");
                    faceThreshold = faceThresholdDecimal.add(levelValue).floatValue();
                    mfEtFaceThreshold.setText(faceThreshold + "");
                }
            }
        });

        // 最小人脸增减按钮
        mfDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (minimumFace > thirty && minimumFace <= twoHundered) {
                    minimumFace = minimumFace - TEN;
                    mfEtAmount.setText(minimumFace + "");
                }
            }
        });

        mfIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (minimumFace >= thirty && minimumFace < twoHundered) {
                    minimumFace = minimumFace + TEN;
                    mfEtAmount.setText(minimumFace + "");
                }
            }
        });

        mfSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        minFace.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (tagMsg.equals(getString(R.string.cw_minface))) {
                    tagMsg = "";
                    return;
                }
                tagMsg = getString(R.string.cw_minface);
                minFace.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(minRepresent, minFaceText, GateMinFaceActivity.this,
                        getString(R.string.cw_minface), showWidth, showXLocation);
            }
        });

        minFaceThreshold.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (tagMsg.equals(getString(R.string.cw_face_threshold))) {
                    tagMsg = "";
                    return;
                }
                tagMsg = getString(R.string.cw_face_threshold);
                minFaceThreshold.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(faceThresholdGroup, minFaceThreshold, GateMinFaceActivity.this,
                        getString(R.string.cw_face_threshold), showWidth, showXLocation);
            }
        });
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("minimumFace", minimumFace);
        intent.putExtra("faceThreshold", faceThreshold);
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        showWidth = minRepresent.getWidth();
        showXLocation = (int) minRepresent.getX();
    }

}
