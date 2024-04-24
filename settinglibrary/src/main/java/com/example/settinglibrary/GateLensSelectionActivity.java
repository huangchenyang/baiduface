package com.example.settinglibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.example.datalibrary.activity.BaseActivity;
import com.example.datalibrary.utils.ToastUtils;


public class GateLensSelectionActivity extends BaseActivity implements View.OnClickListener {

    private RadioButton fltZero;
    private RadioButton fltOne;
    private RadioButton fltTwo;
    private RadioButton fltThree;
    private RadioButton fltFour;
    private RadioButton fltFive;
    private RadioButton fltSix;
    private RadioButton fltSeven;
    private RadioButton fltEight;

    private int type;

    /*0:奥比中光海燕、大白（640*400）*/
    private static final int ZERO = 0;
    /* 1:奥比中光海燕Pro、Atlas（400*640）*/
    private static final int ONE = 1;
    /* 2:奥比中光蝴蝶、Astra Pro\Pro S（640*480）*/
    private static final int TWO = 2;
    /* 3:舜宇Seeker06*/
    private static final int THREE = 3;
    /* 4:螳螂慧视天蝎P1*/
    private static final int FOUR = 4;
    /* 5:瑞识M720N*/
    private static final int FIVE = 5;
    /* 6:奥比中光Deeyea(结构光)*/
    private static final int SIX = 6;
    /* 7:华捷艾米A100S、A200(结构光)*/
    private static final int SEVEN = 7;
    /* 6:Pico DCAM710(ToF)*/
    private static final int EIGHT = 8;
    private RadioGroup flsCameraType;
    private int cameraType;
    /* rgb和nir摄像头宽*/
    private int rgbAndNirWidth;
    /* rgb和nir摄像头高*/
    private int rgbAndNirHeight;
    /* depth摄像头宽*/
    private int depthWidth;
    /* depth摄像头高*/
    private int depthHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_lens_selections);
        init();

    }

    private void init() {
        // 获取Intent对象
        Intent intent = getIntent();
        fltZero = findViewById(R.id.flt_zero);
        fltOne = findViewById(R.id.flt_one);
        fltTwo = findViewById(R.id.flt_two);
        fltThree = findViewById(R.id.flt_three);
        fltFour = findViewById(R.id.flt_four);
        fltFive = findViewById(R.id.flt_five);
        fltSix = findViewById(R.id.flt_six);
        fltSeven = findViewById(R.id.flt_seven);
        fltEight = findViewById(R.id.flt_eight);

        ImageView flsSave = findViewById(R.id.fls_save);
        flsSave.setOnClickListener(this);
        type = intent.getIntExtra("type" , 0);
        cameraType = intent.getIntExtra("cameraType" , 0);
        rgbAndNirWidth = intent.getIntExtra("rgbAndNirWidth" , 0);
        rgbAndNirHeight = intent.getIntExtra("rgbAndNirHeight" , 0);
        depthWidth = intent.getIntExtra("depthWidth" , 0);
        depthHeight = intent.getIntExtra("depthHeight" , 0);
        setlectCamera();
        flsCameraType = findViewById(R.id.fls_camera_type);
        flsCameraType.setOnCheckedChangeListener(cameraTypeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraType == ZERO) {
            fltZero.setChecked(true);
        }
        if (cameraType == ONE) {
            fltOne.setChecked(true);
        }
        if (cameraType == TWO) {
            fltTwo.setChecked(true);
        }
        if (cameraType == THREE) {
            fltThree.setChecked(true);
        }
        if (cameraType == FOUR) {
            fltFour.setChecked(true);
        }
        if (cameraType == FIVE) {
            fltFive.setChecked(true);
        }
        if (cameraType == SIX) {
            fltSix.setChecked(true);
        }
        if (cameraType == SEVEN) {
            fltSeven.setChecked(true);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fls_save) {
            if (fltZero.isChecked() || fltOne.isChecked() || fltTwo.isChecked()
                    || fltThree.isChecked() || fltFour.isChecked() || fltFive.isChecked()
                    || fltSix.isChecked() || fltSeven.isChecked() || fltEight.isChecked()) {
                cameraSelect();
                finish();
            } else {
                ToastUtils.toast(this, "请选择镜头型号在进行返回操作");
            }
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("type", type);
        intent.putExtra("cameraType", cameraType);
        intent.putExtra("rgbAndNirWidth", rgbAndNirWidth);
        intent.putExtra("rgbAndNirHeight", rgbAndNirHeight);
        intent.putExtra("depthWidth", depthWidth);
        intent.putExtra("depthHeight", depthHeight);
        // 设置返回码和返回携带的数据
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    public void cameraSelect() {
        if (cameraType == ZERO) {
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
            depthWidth = 640;
            depthHeight = 400;
        }
        if (cameraType == ONE) {
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
            depthWidth = 640;
            depthHeight = 400;
        }
        if (cameraType == TWO) {
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
            depthWidth = 640;
            depthHeight = 480;
        }
        if (cameraType == THREE) {
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
            depthWidth = 640;
            depthHeight = 480;
        }
        if (cameraType == FOUR) {
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
            depthWidth = 640;
            depthHeight = 480;
        }
        if (cameraType == FIVE) {
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
            depthWidth = 640;
            depthHeight = 480;
        }
        if (cameraType == SIX) {
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
            depthWidth = 640;
            depthHeight = 480;
        }
        if (cameraType == SEVEN) {
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
            depthWidth = 640;
            depthHeight = 480;
        }
    }

    public RadioGroup.OnCheckedChangeListener cameraTypeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            if (checkedRadioButtonId == R.id.flt_zero) {
                cameraType = ZERO;
            } else if (checkedRadioButtonId == R.id.flt_one) {
                cameraType = ONE;
            } else if (checkedRadioButtonId == R.id.flt_two) {
                cameraType = TWO;
            } else if (checkedRadioButtonId == R.id.flt_three) {
                cameraType = THREE;
            } else if (checkedRadioButtonId == R.id.flt_four) {
                cameraType = FOUR;
            } else if (checkedRadioButtonId == R.id.flt_five) {
                cameraType = FIVE;
            } else if (checkedRadioButtonId == R.id.flt_six) {
                cameraType = SIX;
            } else if (checkedRadioButtonId == R.id.flt_seven) {
                cameraType = SEVEN;
            } else if (checkedRadioButtonId == R.id.flt_eight) {
                cameraType = EIGHT;
            }
        }
    };


    public void setlectCamera() {
        if (cameraType == ZERO) {
            fltZero.setChecked(true);
        }
        if (cameraType == ONE) {
            fltOne.setChecked(true);
        }
        if (cameraType == TWO) {
            fltTwo.setChecked(true);
        }
        if (cameraType == THREE) {
            fltThree.setChecked(true);
        }
        if (cameraType == FOUR) {
            fltFour.setChecked(true);
        }
        if (cameraType == FIVE) {
            fltFive.setChecked(true);
        }
        if (cameraType == SIX) {
            fltSix.setChecked(true);
        }
        if (cameraType == SEVEN) {
            fltSeven.setChecked(true);
        }
        if (cameraType == EIGHT) {
            fltEight.setChecked(true);
        }
    }
}