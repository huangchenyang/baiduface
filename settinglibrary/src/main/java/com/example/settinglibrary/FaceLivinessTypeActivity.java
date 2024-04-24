package com.example.settinglibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.example.datalibrary.activity.BaseActivity;
import com.example.datalibrary.utils.PWTextUtils;
import com.example.datalibrary.utils.PreferencesManager;

import java.math.BigDecimal;
import java.text.DecimalFormat;


/**
 * author : shangrong
 * date : two019/five/two7 six:four8 PM
 * description :活体检测模式
 */
public class FaceLivinessTypeActivity extends BaseActivity implements View.OnClickListener {

    private int type;

   /* 1:rgb活体*/
    private static final int ONE = 1;
    /* 2:rgb+nir活体*/
    private static final int TWO = 2;
    /* 3:rgb+depth活体*/
    private static final int THREE = 3;
    /* 4:rgb+nir+depth活体*/
    private static final int FOUR = 4;

    private Button cwLivetype;
    private Button cwRgb;
    private Button cwRgbAndNir;
    private Button cwRgbAndDepth;

    private LinearLayout linerLiveTpye;
    private TextView tvLivType;

    private CheckBox flsRgbAndNirAndDepthLive;
    private CheckBox flsRgbLive;
    private CheckBox flsRgbAndNirLive;
    private CheckBox flsRgbAndDepthLive;
    private String msgTag = "";
    private int showWidth;
    private int showXLocation;
    private LinearLayout flRepresent;
    private View rgbView;
    private View rgbAndNirView;
    private View rgbAndDepthView;
    private Switch qcLiving;
    private LinearLayout qcLinerLiving;
    private ImageView qcGestureDecrease;
    private EditText qcGestureEtThreshold;
    private ImageView qcGestureIncrease;
    private int framesThreshold;

    private int ten = 10;
    private int zero = 0;
    // RGB活体阀值
    private ImageView thRgbLiveDecrease;
    private ImageView thRgbLiveIncrease;
    private EditText thRgbLiveEtThreshold;

    // NIR活体阀值
    private ImageView thNirLiveDecrease;
    private ImageView thNirLiveIncrease;
    private EditText thNirLiveEtThreshold;

    // Depth活体阀值
    private ImageView thdepthLiveDecrease;
    private ImageView thdepthLiveIncrease;
    private EditText thDepthLiveEtThreshold;

    private float rgbLiveScore;
    private float nirLiveScore;
    private float depthLiveScore;

    private BigDecimal rgbDecimal;
    private BigDecimal nirDecimal;
    private BigDecimal depthDecimal;
    private BigDecimal nonmoralValue;
    private static final float TEMPLE_VALUE = 0.05f;
    private Button cwLiveThrehold;
    private LinearLayout linerLiveThreshold;
    private TextView tvLive;
    private Button gateChangeLensBtn;
    private Button gateChangeLensBtnTwo;
    private TextView gateChangeLensTv;
    private TextView gateChangeLensTvTwo;


    private TextView rgbThresholdTv;
    private TextView nirThresholdTv;
    private TextView depthThresholdTv;
    private ImageView thRgbLiveDecreaseAshDisposal;
    private ImageView thRgbLiveIncreaseAshDisposal;
    private ImageView thNirLiveDecreaseAshDisposal;
    private ImageView thNirLiveIncreaseAshDisposal;
    private ImageView thDepthLiveDecreaseAshDisposal;
    private ImageView thDepthLiveIncreaseAshDisposal;
    // rgb和nir摄像头宽
    private int rgbAndNirWidth;
    // rgb和nir摄像头高
    private int rgbAndNirHeight;
    // depth摄像头宽
    private int depthWidth;
    // depth摄像头高
    private int depthHeight;
    // 是否开启活体检测开关
    private boolean livingControl;
    private int cameraType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_facelivinesstype);
        init();
    }

    public void init() {
        // 获取Intent对象
        Intent intent = getIntent();
        framesThreshold = intent.getIntExtra("framesThreshold" , 3);
        rgbLiveScore = intent.getFloatExtra("rgbLiveScore" , 0.8f);
        nirLiveScore = intent.getFloatExtra("nirLiveScore" , 0.8f);
        depthLiveScore = intent.getFloatExtra("depthLiveScore" , 0.8f);
        type = intent.getIntExtra("type" , 1);
        cameraType = intent.getIntExtra("cameraType" , 0);
        livingControl = intent.getBooleanExtra("livingControl" , true);
        rgbAndNirWidth = intent.getIntExtra("rgbAndNirWidth" , 640);
        rgbAndNirHeight = intent.getIntExtra("rgbAndNirHeight" , 480);
        depthWidth = intent.getIntExtra("depthWidth" , 640);
        depthHeight = intent.getIntExtra("depthHeight" , 400);
        flRepresent = findViewById(R.id.flRepresent);
        rgbView = findViewById(R.id.rgbView);
        rgbAndNirView = findViewById(R.id.rgbAndNirView);
        rgbAndDepthView = findViewById(R.id.rgbAndDepthView);
        linerLiveTpye = findViewById(R.id.linerlivetpye);
        tvLivType = findViewById(R.id.tvlivetype);

        cwLivetype = findViewById(R.id.cw_livetype);
        cwLivetype.setOnClickListener(this);
        cwRgb = findViewById(R.id.cw_rgb);
        cwRgb.setOnClickListener(this);
        cwRgbAndNir = findViewById(R.id.cw_rgbandnir);
        cwRgbAndNir.setOnClickListener(this);
        cwRgbAndDepth = findViewById(R.id.cw_rgbanddepth);
        cwRgbAndDepth.setOnClickListener(this);

        flsRgbAndNirAndDepthLive = findViewById(R.id.fls_rgbandniranddepth_live);
        flsRgbLive = findViewById(R.id.fls_rgb_live);
        flsRgbAndNirLive = findViewById(R.id.fls_rgbandnir_live);
        flsRgbAndDepthLive = findViewById(R.id.fls_rgbanddepth_live);

        // 返回
        ImageView flsSave = findViewById(R.id.fls_save);
        flsSave.setOnClickListener(this);
        // 活体检测开关
        qcLiving = findViewById(R.id.qc_Living);
        qcLinerLiving = findViewById(R.id.qc_LinerLiving);

        // 帧数阈值
        qcGestureDecrease = findViewById(R.id.qc_GestureDecrease);
        qcGestureDecrease.setOnClickListener(this);
        qcGestureEtThreshold = findViewById(R.id.qc_GestureEtThreshold);
        qcGestureIncrease = findViewById(R.id.qc_GestureIncrease);
        qcGestureIncrease.setOnClickListener(this);
        // rgb活体
        thRgbLiveDecrease = findViewById(R.id.th_RgbLiveDecrease);
        thRgbLiveDecrease.setOnClickListener(this);
        thRgbLiveIncrease = findViewById(R.id.th_RgbLiveIncrease);
        thRgbLiveIncrease.setOnClickListener(this);
        thRgbLiveEtThreshold = findViewById(R.id.th_RgbLiveEtThreshold);
        // nir活体
        thNirLiveDecrease = findViewById(R.id.th_NirLiveDecrease);
        thNirLiveDecrease.setOnClickListener(this);
        thNirLiveIncrease = findViewById(R.id.th_NirLiveIncrease);
        thNirLiveIncrease.setOnClickListener(this);
        thNirLiveEtThreshold = findViewById(R.id.th_NirLiveEtThreshold);
        // depth活体
        thdepthLiveDecrease = findViewById(R.id.th_depthLiveDecrease);
        thdepthLiveDecrease.setOnClickListener(this);
        thdepthLiveIncrease = findViewById(R.id.th_depthLiveIncrease);
        thdepthLiveIncrease.setOnClickListener(this);
        thDepthLiveEtThreshold = findViewById(R.id.th_depthLiveEtThreshold);

        cwLiveThrehold = findViewById(R.id.cw_livethrehold);
        cwLiveThrehold.setOnClickListener(this);
        linerLiveThreshold = findViewById(R.id.linerlivethreshold);
        tvLive = findViewById(R.id.tvlive);

        nonmoralValue = new BigDecimal(TEMPLE_VALUE + "");

        rgbThresholdTv = findViewById(R.id.rgb_thresholdTv);
        nirThresholdTv = findViewById(R.id.nir_thresholdTv);
        depthThresholdTv = findViewById(R.id.depth_thresholdTv);
        thRgbLiveDecreaseAshDisposal = findViewById(R.id.th_RgbLiveDecrease_Ash_disposal);
        thRgbLiveIncreaseAshDisposal = findViewById(R.id.th_RgbLiveIncrease_Ash_disposal);
        thNirLiveDecreaseAshDisposal = findViewById(R.id.th_NirLiveDecrease_Ash_disposal);
        thNirLiveIncreaseAshDisposal = findViewById(R.id.th_NirLiveIncrease_Ash_disposal);
        thDepthLiveDecreaseAshDisposal = findViewById(R.id.th_depthLiveDecrease_Ash_disposal);
        thDepthLiveIncreaseAshDisposal = findViewById(R.id.th_depthLiveIncrease_Ash_disposal);

        // 更换镜头按钮
        gateChangeLensBtn = findViewById(R.id.gate_change_lens_btn);
        gateChangeLensBtn.setOnClickListener(this);

        gateChangeLensBtnTwo = findViewById(R.id.gate_change_lens_btn_two);
        gateChangeLensBtnTwo.setOnClickListener(this);


        gateChangeLensTv = findViewById(R.id.gate_change_lens_tv);
        gateChangeLensTvTwo = findViewById(R.id.gate_change_lens_tv_two);

        PWTextUtils.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDismiss() {
                cwLivetype.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRgb.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRgbAndNir.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRgbAndDepth.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLiveThrehold.setBackground(getDrawable(R.mipmap.icon_setting_question));
            }
        });

        qcLiving.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (type == 1) {
                        flsRgbLive.setChecked(true);
                    } else if (type == 2) {
                        flsRgbAndNirLive.setChecked(true);
                    } else if (type == 3) {
                        flsRgbAndDepthLive.setChecked(true);
                    } else if (type == 4) {
                        flsRgbAndNirAndDepthLive.setChecked(true);
                    } else {
                        type = 1;
                        flsRgbLive.setChecked(true);
                    }
                    qcLiving.setChecked(true);
                    livingControl = true;
                    qcLinerLiving.setVisibility(View.VISIBLE);
                } else {
                    qcLiving.setChecked(false);
                    livingControl = false;
                    qcLinerLiving.setVisibility(View.INVISIBLE);
                    justify();
                }
            }
        });

        flsRgbLive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // 镜头类型
                    flsRgbLive.setChecked(true);
                    flsRgbLive.setEnabled(false);
                    flsRgbAndNirLive.setChecked(false);
                    flsRgbAndDepthLive.setChecked(false);
                    flsRgbAndNirAndDepthLive.setChecked(false);
                    gateChangeLensBtn.setVisibility(View.GONE);
                    gateChangeLensBtnTwo.setVisibility(View.GONE);
                    type = ONE;
                    // nir 置灰
                    nirThresholdTv.setTextColor(getResources().getColor(R.color.hui_color));
                    thNirLiveDecrease.setVisibility(View.GONE);
                    thNirLiveEtThreshold.setTextColor(getResources().getColor(R.color.hui_color));
                    thNirLiveIncrease.setVisibility(View.GONE);
                    thNirLiveDecreaseAshDisposal.setVisibility(View.VISIBLE);
                    thNirLiveIncreaseAshDisposal.setVisibility(View.VISIBLE);
                    // depth 置灰
                    depthThresholdTv.setTextColor(getResources().getColor(R.color.hui_color));
                    thdepthLiveDecrease.setVisibility(View.GONE);
                    thDepthLiveEtThreshold.setTextColor(getResources().getColor(R.color.hui_color));
                    thdepthLiveIncrease.setVisibility(View.GONE);
                    thDepthLiveDecreaseAshDisposal.setVisibility(View.VISIBLE);
                    thDepthLiveIncreaseAshDisposal.setVisibility(View.VISIBLE);
                    justify();

                    if (cameraType == zero) {
                        gateChangeLensTv.setText("奥比中光海燕、大白（640*400）");
                    } else if (cameraType == ONE) {
                        gateChangeLensTv.setText("奥比中光海燕Pro、Atlas（400*640）");
                    } else if (cameraType == TWO) {
                        gateChangeLensTv.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）");
                    }

                    if (cameraType == zero) {
                        gateChangeLensTvTwo.setText("奥比中光海燕、大白（640*400）");
                    } else if (cameraType == ONE) {
                        gateChangeLensTvTwo.setText("奥比中光海燕Pro、Atlas（400*640）");
                    } else if (cameraType == TWO) {
                        gateChangeLensTvTwo.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）");
                    }

                } else {
                    flsRgbLive.setEnabled(true);
                }
            }
        });
        flsRgbAndNirLive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    flsRgbAndNirLive.setChecked(true);
                    flsRgbAndNirLive.setEnabled(false);
                    flsRgbLive.setChecked(false);
                    flsRgbAndDepthLive.setChecked(false);
                    flsRgbAndNirAndDepthLive.setChecked(false);
                    gateChangeLensBtn.setVisibility(View.GONE);
                    gateChangeLensBtnTwo.setVisibility(View.GONE);
                    type = TWO;

                    nirThresholdTv.setTextColor(getResources().getColor(R.color.white));
                    thNirLiveDecrease.setVisibility(View.VISIBLE);
                    thNirLiveEtThreshold.setTextColor(getResources().getColor(R.color.white));
                    thNirLiveIncrease.setVisibility(View.VISIBLE);
                    thNirLiveDecreaseAshDisposal.setVisibility(View.GONE);
                    thNirLiveIncreaseAshDisposal.setVisibility(View.GONE);

                    // depth 置灰
                    depthThresholdTv.setTextColor(getResources().getColor(R.color.hui_color));
                    thdepthLiveDecrease.setVisibility(View.GONE);
                    thDepthLiveEtThreshold.setTextColor(getResources().getColor(R.color.hui_color));
                    thdepthLiveIncrease.setVisibility(View.GONE);
                    thDepthLiveDecreaseAshDisposal.setVisibility(View.VISIBLE);
                    thDepthLiveIncreaseAshDisposal.setVisibility(View.VISIBLE);
                    justify();

                    if (cameraType == zero) {
                        gateChangeLensTv.setText("奥比中光海燕、大白（640*400）");
                    } else if (cameraType == ONE) {
                        gateChangeLensTv.setText("奥比中光海燕Pro、Atlas（400*640）");
                    } else if (cameraType == TWO) {
                        gateChangeLensTv.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）");
                    }

                    if (cameraType == zero) {
                        gateChangeLensTvTwo.setText("奥比中光海燕、大白（640*400）");
                    } else if (cameraType == ONE) {
                        gateChangeLensTvTwo.setText("奥比中光海燕Pro、Atlas（400*640）");
                    } else if (cameraType == TWO) {
                        gateChangeLensTvTwo.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）");
                    }

                } else {
                    flsRgbAndNirLive.setEnabled(true);
                }
            }
        });
        flsRgbAndDepthLive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // 镜头类型
                    flsRgbAndDepthLive.setChecked(true);
                    flsRgbAndDepthLive.setEnabled(false);
                    flsRgbLive.setChecked(false);
                    flsRgbAndNirLive.setChecked(false);
                    flsRgbAndNirAndDepthLive.setChecked(false);
                    gateChangeLensBtn.setVisibility(View.VISIBLE);
                    gateChangeLensBtnTwo.setVisibility(View.GONE);
                    type = THREE;

                    // nir 置灰
                    nirThresholdTv.setTextColor(getResources().getColor(R.color.hui_color));
                    thNirLiveDecrease.setVisibility(View.GONE);
                    thNirLiveEtThreshold.setTextColor(getResources().getColor(R.color.hui_color));
                    thNirLiveIncrease.setVisibility(View.GONE);
                    thNirLiveDecreaseAshDisposal.setVisibility(View.VISIBLE);
                    thNirLiveIncreaseAshDisposal.setVisibility(View.VISIBLE);

                    depthThresholdTv.setTextColor(getResources().getColor(R.color.white));
                    thdepthLiveDecrease.setVisibility(View.VISIBLE);
                    thDepthLiveEtThreshold.setTextColor(getResources().getColor(R.color.white));
                    thdepthLiveIncrease.setVisibility(View.VISIBLE);
                    thDepthLiveDecreaseAshDisposal.setVisibility(View.GONE);
                    thDepthLiveIncreaseAshDisposal.setVisibility(View.GONE);
                    justify();

                    if (cameraType == zero) {
                        gateChangeLensTv.setText("奥比中光海燕、大白（640*400）".substring(0, 12) + "...");
                    } else if (cameraType == ONE) {
                        gateChangeLensTv.setText("奥比中光海燕Pro、Atlas（400*640）".substring(0, 12) + "...");
                    } else if (cameraType == TWO) {
                        gateChangeLensTv.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）".substring(0, 12) + "...");
                    } else {
                        gateChangeLensTv.setText("此模态下需设定镜头型号");
                    }

                    if (cameraType == zero) {
                        gateChangeLensTvTwo.setText("奥比中光海燕、大白（640*400）");
                    } else if (cameraType == ONE) {
                        gateChangeLensTvTwo.setText("奥比中光海燕Pro、Atlas（400*640）");
                    } else if (cameraType == TWO) {
                        gateChangeLensTvTwo.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）");
                    } else {
                        gateChangeLensTvTwo.setText("此模态下需设定镜头型号");
                    }

                } else {
                    flsRgbAndDepthLive.setEnabled(true);
                }

            }
        });
        flsRgbAndNirAndDepthLive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    flsRgbAndNirAndDepthLive.setChecked(true);
                    flsRgbAndNirAndDepthLive.setEnabled(false);
                    flsRgbLive.setChecked(false);
                    flsRgbAndNirLive.setChecked(false);
                    flsRgbAndDepthLive.setChecked(false);
                    gateChangeLensBtn.setVisibility(View.GONE);
                    gateChangeLensBtnTwo.setVisibility(View.VISIBLE);
                    type = FOUR;

                    if (cameraType == zero) {
                        gateChangeLensTv.setText("奥比中光海燕、大白（640*400）");
                    } else if (cameraType == ONE) {
                        gateChangeLensTv.setText("奥比中光海燕Pro、Atlas（400*640）");
                    } else if (cameraType == TWO) {
                        gateChangeLensTv.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）");
                    } else {
                        gateChangeLensTvTwo.setText("此模态下需设定镜头型号");
                    }

                    if (cameraType == zero) {
                        gateChangeLensTvTwo.setText("奥比中光海燕、大白（640*400）".substring(0, 12) + "...");
                    } else if (cameraType == ONE) {
                        gateChangeLensTvTwo.setText("奥比中光海燕Pro、Atlas（400*640）".substring(0, 12) + "...");
                    } else if (cameraType == TWO) {
                        gateChangeLensTvTwo.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）".substring(0, 12) + "...");
                    } else {
                        gateChangeLensTvTwo.setText("此模态下需设定镜头型号");
                    }

                    nirThresholdTv.setTextColor(getResources().getColor(R.color.white));
                    thNirLiveDecrease.setVisibility(View.VISIBLE);
                    thNirLiveEtThreshold.setTextColor(getResources().getColor(R.color.white));
                    thNirLiveIncrease.setVisibility(View.VISIBLE);
                    thNirLiveDecreaseAshDisposal.setVisibility(View.GONE);
                    thNirLiveIncreaseAshDisposal.setVisibility(View.GONE);

                    depthThresholdTv.setTextColor(getResources().getColor(R.color.white));
                    thdepthLiveDecrease.setVisibility(View.VISIBLE);
                    thDepthLiveEtThreshold.setTextColor(getResources().getColor(R.color.white));
                    thdepthLiveIncrease.setVisibility(View.VISIBLE);
                    thDepthLiveDecreaseAshDisposal.setVisibility(View.GONE);
                    thDepthLiveIncreaseAshDisposal.setVisibility(View.GONE);
                    justify();
                } else {
                    flsRgbAndNirAndDepthLive.setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (gateChangeLensBtn.getVisibility() == View.VISIBLE) {
            // 镜头类型
            PreferencesManager.getInstance(this.getApplicationContext())
                    .setRgbDepth(cameraType);
            if (cameraType == zero) {
                gateChangeLensTv.setText("奥比中光海燕、大白（640*400）"
                        .substring(0, 12) + "...");
            } else if (cameraType == ONE) {
                gateChangeLensTv.setText("奥比中光海燕Pro、Atlas（400*640）"
                        .substring(0, 12) + "...");
            } else if (cameraType == TWO) {
                gateChangeLensTv.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）"
                        .substring(0, 12) + "...");
            } else {
                gateChangeLensTv.setText("此模态下需设定镜头型号");
            }
//        }

//        if (gateChangeLensBtnTwo.getVisibility() == View.VISIBLE) {
//            // 镜头类型
//            int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
            PreferencesManager.getInstance(this.getApplicationContext())
                    .setRgbNirDepth(cameraType);
            if (cameraType == zero) {
                gateChangeLensTvTwo.setText("奥比中光海燕、大白（640*400）"
                        .substring(0, 12) + "...");
            } else if (cameraType == ONE) {
                gateChangeLensTvTwo.setText("奥比中光海燕Pro、Atlas（400*640）"
                        .substring(0, 12) + "...");
            } else if (cameraType == TWO) {
                gateChangeLensTvTwo.setText("奥比中光蝴蝶、Astra Pro\\Pro S（640*480）"
                        .substring(0, 12) + "...");
            } else {
                gateChangeLensTvTwo.setText("此模态下需设定镜头型号");
            }
//        }

        if (livingControl) {
            qcLiving.setChecked(true);
            qcLinerLiving.setVisibility(View.VISIBLE);
        } else {
            qcLiving.setChecked(false);
            qcLinerLiving.setVisibility(View.INVISIBLE);
        }

        qcGestureEtThreshold.setText(framesThreshold + "");
        thRgbLiveEtThreshold.setText(roundByScale(rgbLiveScore));
        thNirLiveEtThreshold.setText(roundByScale(nirLiveScore));
        thDepthLiveEtThreshold.setText(roundByScale(depthLiveScore));


        if (type == ONE) {
            flsRgbLive.setChecked(true);
            flsRgbAndNirLive.setChecked(false);
            flsRgbAndDepthLive.setChecked(false);
            flsRgbAndNirAndDepthLive.setChecked(false);
        }
        if (type == TWO) {
            flsRgbAndNirLive.setChecked(true);
            flsRgbLive.setChecked(false);
            flsRgbAndDepthLive.setChecked(false);
            flsRgbAndNirAndDepthLive.setChecked(false);
        }
        if (type == THREE) {
            flsRgbAndDepthLive.setChecked(true);
            flsRgbLive.setChecked(false);
            flsRgbAndNirLive.setChecked(false);
            flsRgbAndNirAndDepthLive.setChecked(false);
        }
        if (type == FOUR) {
            flsRgbAndNirAndDepthLive.setChecked(true);
            flsRgbLive.setChecked(false);
            flsRgbAndNirLive.setChecked(false);
            flsRgbAndDepthLive.setChecked(false);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        showWidth = flRepresent.getWidth();
        showXLocation = (int) flRepresent.getLeft();
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fls_save) {
            if (qcLiving.isChecked()) {
                livingControl = true;
            } else {
                livingControl = false;
                type = zero;
            }
            framesThreshold = Integer.valueOf(qcGestureEtThreshold.getText().toString());
            rgbLiveScore = Float.parseFloat(thRgbLiveEtThreshold.getText().toString());
            nirLiveScore = Float.parseFloat(thNirLiveEtThreshold.getText().toString());
            depthLiveScore = Float.parseFloat(thDepthLiveEtThreshold.getText().toString());

            justify();
            finish();
        } else if (id == R.id.cw_livetype) {
            if (msgTag.equals(getString(R.string.cw_livedetecttype))) {
                msgTag = "";
                return;
            }
            msgTag = getString(R.string.cw_livedetecttype);
            cwLivetype.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
            PWTextUtils.showDescribeText(linerLiveTpye, tvLivType, FaceLivinessTypeActivity.this,
                    getString(R.string.cw_livedetecttype)
                    , showWidth, showXLocation);
        } else if (id == R.id.cw_rgb) {
            if (msgTag.equals(getString(R.string.cw_rgblive))) {
                msgTag = "";
                return;
            }
            msgTag = getString(R.string.cw_rgblive);
            cwRgb.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
            PWTextUtils.showDescribeText(rgbView, rgbView, FaceLivinessTypeActivity.this,
                    getString(R.string.cw_rgblive)
                    , showWidth, 0);
        } else if (id == R.id.cw_rgbandnir) {
            if (msgTag.equals(getString(R.string.cw_rgbandnir))) {
                msgTag = "";
                return;
            }
            msgTag = getString(R.string.cw_rgbandnir);
            cwRgbAndNir.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
            PWTextUtils.showDescribeText(rgbAndNirView, rgbAndNirView,
                    FaceLivinessTypeActivity.this, getString(R.string.cw_rgbandnir)
                    , showWidth, 0);
        } else if (id == R.id.cw_rgbanddepth) {
            if (msgTag.equals(getString(R.string.cw_rgbanddepth))) {
                msgTag = "";
                return;
            }
            msgTag = getString(R.string.cw_rgbanddepth);
            cwRgbAndDepth.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
            PWTextUtils.showDescribeText(rgbAndDepthView, rgbAndDepthView,
                    FaceLivinessTypeActivity.this, getString(R.string.cw_rgbanddepth)
                    , showWidth, 0);
            // 减
        } else if (id == R.id.qc_GestureDecrease) {
            if (framesThreshold > ONE && framesThreshold <= ten) {
                framesThreshold = framesThreshold - 1;
                qcGestureEtThreshold.setText(framesThreshold + "");
            }
            // 加
        } else if (id == R.id.qc_GestureIncrease) {
            if (framesThreshold >= ONE && framesThreshold < ten) {
                framesThreshold = framesThreshold + 1;
                qcGestureEtThreshold.setText(framesThreshold + "");
            }
        } else if (id == R.id.th_RgbLiveDecrease) {
            if (rgbLiveScore > zero && rgbLiveScore <= ONE) {
                rgbDecimal = new BigDecimal(rgbLiveScore + "");
                rgbLiveScore = rgbDecimal.subtract(nonmoralValue).floatValue();
                thRgbLiveEtThreshold.setText(roundByScale(rgbLiveScore));
            }
        } else if (id == R.id.th_RgbLiveIncrease) {
            if (rgbLiveScore >= zero && rgbLiveScore < ONE) {
                rgbDecimal = new BigDecimal(rgbLiveScore + "");
                rgbLiveScore = rgbDecimal.add(nonmoralValue).floatValue();
                thRgbLiveEtThreshold.setText(roundByScale(rgbLiveScore));
            }
        } else if (id == R.id.th_NirLiveDecrease) {
            if (nirLiveScore > zero && nirLiveScore <= ONE) {
                nirDecimal = new BigDecimal(nirLiveScore + "");
                nirLiveScore = nirDecimal.subtract(nonmoralValue).floatValue();
                thNirLiveEtThreshold.setText(roundByScale(nirLiveScore));
            }
        } else if (id == R.id.th_NirLiveIncrease) {
            if (nirLiveScore >= zero && nirLiveScore < ONE) {
                nirDecimal = new BigDecimal(nirLiveScore + "");
                nirLiveScore = nirDecimal.add(nonmoralValue).floatValue();
                thNirLiveEtThreshold.setText(roundByScale(nirLiveScore));
            }
        } else if (id == R.id.th_depthLiveDecrease) {
            if (depthLiveScore > zero && depthLiveScore <= ONE) {
                depthDecimal = new BigDecimal(depthLiveScore + "");
                depthLiveScore = depthDecimal.subtract(nonmoralValue).floatValue();
                thDepthLiveEtThreshold.setText(roundByScale(depthLiveScore));
            }
        } else if (id == R.id.th_depthLiveIncrease) {
            if (depthLiveScore >= zero && depthLiveScore < ONE) {
                depthDecimal = new BigDecimal(depthLiveScore + "");
                depthLiveScore = depthDecimal.add(nonmoralValue).floatValue();
                thDepthLiveEtThreshold.setText(roundByScale(depthLiveScore));
            }
        } else if (id == R.id.cw_livethrehold) {
            if (msgTag.equals(getString(R.string.cw_livethrehold))) {
                msgTag = "";
                return;
            }
            msgTag = getString(R.string.cw_livethrehold);
            cwLiveThrehold.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
            PWTextUtils.showDescribeText(linerLiveThreshold, tvLive, FaceLivinessTypeActivity.this,
                    getString(R.string.cw_livethrehold), showWidth, showXLocation);
        } else if (id == R.id.gate_change_lens_btn) {
            Intent intent = new Intent(FaceLivinessTypeActivity.this, GateLensSelectionActivity.class);
            intent.putExtra("type", type);
            intent.putExtra("cameraType", cameraType);
            intent.putExtra("rgbAndNirWidth", rgbAndNirWidth);
            intent.putExtra("rgbAndNirHeight", rgbAndNirHeight);
            intent.putExtra("depthWidth", depthWidth);
            intent.putExtra("depthHeight", depthHeight);
            startActivityForResult(intent , 100);
        } else if (id == R.id.gate_change_lens_btn_two) {
            Intent intent = new Intent(FaceLivinessTypeActivity.this, GateLensSelectionActivity.class);
            intent.putExtra("type", type);
            intent.putExtra("cameraType", cameraType);
            intent.putExtra("rgbAndNirWidth", rgbAndNirWidth);
            intent.putExtra("rgbAndNirHeight", rgbAndNirHeight);
            intent.putExtra("depthWidth", depthWidth);
            intent.putExtra("depthHeight", depthHeight);
            startActivityForResult(intent , 100);
        }
    }

    @Override
    public void finish() {

        Intent intent = new Intent();
        intent.putExtra("framesThreshold", framesThreshold);
        intent.putExtra("rgbLiveScore", rgbLiveScore);
        intent.putExtra("nirLiveScore", nirLiveScore);
        intent.putExtra("depthLiveScore", depthLiveScore);
        intent.putExtra("type", type);
        intent.putExtra("cameraType", cameraType);
        intent.putExtra("rgbAndNirWidth", rgbAndNirWidth);
        intent.putExtra("rgbAndNirHeight", rgbAndNirHeight);
        intent.putExtra("depthWidth", depthWidth);
        intent.putExtra("depthHeight", depthHeight);
        intent.putExtra("livingControl", livingControl);
        // 设置返回码和返回携带的数据
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    public void justify() {
        if (type == ONE) {
            type = ONE;
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
        }
        if (type == TWO) {
            type = TWO;
            rgbAndNirWidth = 640;
            rgbAndNirHeight = 480;
        }
        if (type == THREE) {
            type = THREE;
        }
        if (type == FOUR) {
            type = FOUR;
        }
        if (type == zero) {
            type = 0;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        switch (requestCode) {
            case 100: // 返回的结果是来自于Activity B
                type = data.getIntExtra("type" , 0);
                cameraType = data.getIntExtra("cameraType" , 0);
                rgbAndNirWidth = data.getIntExtra("rgbAndNirWidth" , 640);
                rgbAndNirHeight = data.getIntExtra("rgbAndNirHeight" , 480);
                depthWidth = data.getIntExtra("depthWidth" , 640);
                depthHeight = data.getIntExtra("depthHeight" , 400);
                break;
        }
    }

    public static String roundByScale(float numberValue) {
        // 构造方法的字符格式这里如果小数不足2位,会以0补足.
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        // format 返回的是字符串
        String resultNumber = decimalFormat.format(numberValue);
        return resultNumber;
    }
}
