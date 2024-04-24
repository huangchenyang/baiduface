package com.baidu.idl.main.facesdk.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.gatelibrary.R;
import com.baidu.idl.main.facesdk.license.BDFaceLicenseAuthInfo;
import com.baidu.idl.main.facesdk.utils.GateConfigUtils;
import com.baidu.idl.main.facesdk.utils.RegisterConfigUtils;
import com.example.datalibrary.activity.BaseActivity;
import com.example.datalibrary.utils.PreferencesManager;
import com.baidu.idl.main.facesdk.model.SingleBaseConfig;
import com.example.settinglibrary.FaceLivinessTypeActivity;
import com.example.settinglibrary.GateConfigQualtifyActivity;
import com.example.settinglibrary.GateFaceDetectActivity;
import com.example.settinglibrary.GateLensSettingsActivity;
import com.example.settinglibrary.GateMinFaceActivity;
import com.example.settinglibrary.LogSettingActivity;
import com.example.settinglibrary.PictureOptimizationActivity;
import com.example.settinglibrary.VersionMessageActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GateSettingActivity extends BaseActivity implements View.OnClickListener {

    private ImageView gateSetttingBack;
    private LinearLayout gateFaceDetection;
    private LinearLayout gateConfigQualtify;
    private LinearLayout gateHuotiDetection;
    private LinearLayout gateFaceRecognition;
    private LinearLayout gateLensSettings;
    private View gatePictureOptimization;
    private View gateLogSettings;
    private TextView tvSettingQualtify;
    private TextView logSettingQualtify;
    private TextView tvSettingLiviness;
    private LinearLayout configVersionMessage;
    private TextView tvSettingEffectiveDate;
    private FaceAuth faceAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_setting);
        init();
    }

    private void init() {
        faceAuth = new FaceAuth();
        // 返回
        gateSetttingBack = findViewById(R.id.gate_settting_back);
        gateSetttingBack.setOnClickListener(this);
        // 人脸检测
        gateFaceDetection = findViewById(R.id.gate_face_detection);
        gateFaceDetection.setOnClickListener(this);
        // 质量检测
        gateConfigQualtify = findViewById(R.id.gate_config_qualtify);
        gateConfigQualtify.setOnClickListener(this);
        // 活体检测
        gateHuotiDetection = findViewById(R.id.gate_huoti_detection);
        gateHuotiDetection.setOnClickListener(this);
        // 人脸识别
        gateFaceRecognition = findViewById(R.id.gate_face_recognition);
        gateFaceRecognition.setOnClickListener(this);
        // 镜头设置
        gateLensSettings = findViewById(R.id.gate_lens_settings);
        gateLensSettings.setOnClickListener(this);
        // 图像优化
        gatePictureOptimization = findViewById(R.id.gate_picture_optimization);
        gatePictureOptimization.setOnClickListener(this);
        // 日志设置
        gateLogSettings = findViewById(R.id.gate_log_settings);
        gateLogSettings.setOnClickListener(this);
        // 版本信息
        configVersionMessage = findViewById(R.id.configVersionMessage);
        configVersionMessage.setOnClickListener(this);
        tvSettingQualtify = findViewById(R.id.tvSettingQualtify);
        logSettingQualtify = findViewById(R.id.logSettingQualtify);
        tvSettingLiviness = findViewById(R.id.tvSettingLiviness);

        tvSettingEffectiveDate = findViewById(R.id.tvSettingEffectiveDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SingleBaseConfig.getBaseConfig().isLog()) {
            logSettingQualtify.setText("开启");
        } else {
            logSettingQualtify.setText("关闭");
        }
        if (SingleBaseConfig.getBaseConfig().isQualityControl()) {
            tvSettingQualtify.setText("开启");
        } else {
            tvSettingQualtify.setText("关闭");
        }
        if (SingleBaseConfig.getBaseConfig().isLivingControl()) {
            tvSettingLiviness.setText("开启");
        } else {
            tvSettingLiviness.setText("关闭");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        BDFaceLicenseAuthInfo bdFaceLicenseAuthInfo = faceAuth.getAuthInfo(this);
        Date dateLong = new Date(bdFaceLicenseAuthInfo.expireTime * 1000L);
        String dateTime = simpleDateFormat.format(dateLong);

        tvSettingEffectiveDate.setText("有效期至" + dateTime);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.gate_settting_back) {
            PreferencesManager.getInstance(this.getApplicationContext())
                    .setType(SingleBaseConfig.getBaseConfig().getType());
            finish();
        } else if (id == R.id.gate_face_detection) {
            Intent intent = new Intent(GateSettingActivity.this, GateMinFaceActivity.class);
            intent.putExtra("minimumFace", SingleBaseConfig.getBaseConfig().getMinimumFace());
            intent.putExtra("faceThreshold", SingleBaseConfig.getBaseConfig().getFaceThreshold());
            startActivityForResult(intent , 100);
        } else if (id == R.id.gate_config_qualtify) {
            Intent intent = new Intent(GateSettingActivity.this, GateConfigQualtifyActivity.class);
            intent.putExtra("gesture", SingleBaseConfig.getBaseConfig().getGesture());
            intent.putExtra("illum", SingleBaseConfig.getBaseConfig().getIllumination());
            intent.putExtra("blur", SingleBaseConfig.getBaseConfig().getBlur());
            intent.putExtra("eye", SingleBaseConfig.getBaseConfig().getLeftEye());
            intent.putExtra("cheek", SingleBaseConfig.getBaseConfig().getLeftCheek());
            intent.putExtra("nose", SingleBaseConfig.getBaseConfig().getNose());
            intent.putExtra("mouth", SingleBaseConfig.getBaseConfig().getMouth());
            intent.putExtra("chinContour", SingleBaseConfig.getBaseConfig().getChinContour());
            intent.putExtra("qualityControl", SingleBaseConfig.getBaseConfig().isQualityControl());
            startActivityForResult(intent , 101);
        } else if (id == R.id.gate_huoti_detection) {
            Intent intent = new Intent(GateSettingActivity.this, FaceLivinessTypeActivity.class);
            intent.putExtra("framesThreshold", SingleBaseConfig.getBaseConfig().getFramesThreshold());
            intent.putExtra("rgbLiveScore", SingleBaseConfig.getBaseConfig().getRgbLiveScore());
            intent.putExtra("nirLiveScore", SingleBaseConfig.getBaseConfig().getNirLiveScore());
            intent.putExtra("depthLiveScore", SingleBaseConfig.getBaseConfig().getDepthLiveScore());
            intent.putExtra("type", SingleBaseConfig.getBaseConfig().getType());
            intent.putExtra("cameraType", SingleBaseConfig.getBaseConfig().getCameraType());
            intent.putExtra("livingControl", SingleBaseConfig.getBaseConfig().isLivingControl());
            intent.putExtra("rgbAndNirWidth", SingleBaseConfig.getBaseConfig().getRgbAndNirWidth());
            intent.putExtra("rgbAndNirHeight", SingleBaseConfig.getBaseConfig().getRgbAndNirHeight());
            intent.putExtra("depthWidth", SingleBaseConfig.getBaseConfig().getDepthWidth());
            intent.putExtra("depthHeight", SingleBaseConfig.getBaseConfig().getDepthHeight());
            startActivityForResult(intent , 102);
        } else if (id == R.id.gate_face_recognition) {
            Intent intent = new Intent(GateSettingActivity.this, GateFaceDetectActivity.class);

            intent.putExtra("activeModel", SingleBaseConfig.getBaseConfig().getActiveModel());
            intent.putExtra("liveScoreThreshold", SingleBaseConfig.getBaseConfig().getLiveThreshold());
            intent.putExtra("idScoreThreshold", SingleBaseConfig.getBaseConfig().getIdThreshold());
            intent.putExtra("rgbAndNirScoreThreshold", SingleBaseConfig.getBaseConfig().getRgbAndNirThreshold());
            intent.putExtra("cameraLightThreshold", SingleBaseConfig.getBaseConfig().getCameraLightThreshold());
            startActivityForResult(intent , 103);
        } else if (id == R.id.gate_lens_settings) {
            Intent intent = new Intent(GateSettingActivity.this, GateLensSettingsActivity.class);
            intent.putExtra("rgbRevert", SingleBaseConfig.getBaseConfig().getRgbRevert());
            intent.putExtra("rgbDetectDirection" , SingleBaseConfig.getBaseConfig().getRgbDetectDirection());
            intent.putExtra("mirrorDetectRGB" , SingleBaseConfig.getBaseConfig().getMirrorDetectRGB());
            intent.putExtra("nirDetectDirection" , SingleBaseConfig.getBaseConfig().getNirDetectDirection());
            intent.putExtra("mirrorDetectNIR" , SingleBaseConfig.getBaseConfig().getMirrorDetectNIR());
            intent.putExtra("rgbVideoDirection" , SingleBaseConfig.getBaseConfig().getRgbVideoDirection());
            intent.putExtra("mirrorVideoRGB" , SingleBaseConfig.getBaseConfig().getMirrorVideoRGB());
            intent.putExtra("nirVideoDirection" , SingleBaseConfig.getBaseConfig().getNirVideoDirection());
            intent.putExtra("mirrorVideoNIR" , SingleBaseConfig.getBaseConfig().getMirrorVideoNIR());
            intent.putExtra("rbgCameraId" , SingleBaseConfig.getBaseConfig().getRBGCameraId());
            startActivityForResult(intent , 104);
        } else if (id == R.id.configVersionMessage) {
            Intent intent = new Intent(GateSettingActivity.this, VersionMessageActivity.class);
            startActivityForResult(intent , 105);
        } else if (id == R.id.gate_picture_optimization){
            Intent intent = new Intent(GateSettingActivity.this, PictureOptimizationActivity.class);
            intent.putExtra("darkEnhance" , SingleBaseConfig.getBaseConfig().isDarkEnhance());
            intent.putExtra("bestImage" , SingleBaseConfig.getBaseConfig().isBestImage());
            startActivityForResult(intent , 106);
        } else if (id == R.id.gate_log_settings) {
            Intent intent = new Intent(GateSettingActivity.this, LogSettingActivity.class);
            startActivityForResult(intent , 107);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == 100){
                SingleBaseConfig.getBaseConfig().setMinimumFace(data.getIntExtra("minimumFace" , 30));
                SingleBaseConfig.getBaseConfig().setFaceThreshold(data.getFloatExtra("faceThreshold" , 0.5f));
            }else if (requestCode == 101){
                SingleBaseConfig.getBaseConfig().setGesture(data.getFloatExtra("gesture" , 0.8f));
                SingleBaseConfig.getBaseConfig().setIllumination(data.getFloatExtra("illum" , 0.8f));
                SingleBaseConfig.getBaseConfig().setBlur(data.getFloatExtra("blur" , 0.8f));
                SingleBaseConfig.getBaseConfig().setLeftEye(data.getFloatExtra("eye" , 0.8f));
                SingleBaseConfig.getBaseConfig().setRightEye(data.getFloatExtra("eye" , 0.8f));
                SingleBaseConfig.getBaseConfig().setLeftCheek(data.getFloatExtra("cheek" , 0.8f));
                SingleBaseConfig.getBaseConfig().setRightCheek(data.getFloatExtra("cheek" , 0.8f));
                SingleBaseConfig.getBaseConfig().setNose(data.getFloatExtra("nose" , 0.8f));
                SingleBaseConfig.getBaseConfig().setMouth(data.getFloatExtra("mouth" , 0.8f));
                SingleBaseConfig.getBaseConfig().setChinContour(data.getFloatExtra("chinContour" , 0.8f));
                SingleBaseConfig.getBaseConfig().setQualityControl(data.getBooleanExtra("qualityControl" , true));
            }else if (requestCode == 102){
                SingleBaseConfig.getBaseConfig().setCameraType(data.getIntExtra("cameraType" , 0));
                SingleBaseConfig.getBaseConfig().setFramesThreshold(data.getIntExtra("framesThreshold" , 3));
                SingleBaseConfig.getBaseConfig().setRgbLiveScore(data.getFloatExtra("rgbLiveScore" , 0.8f));
                SingleBaseConfig.getBaseConfig().setNirLiveScore(data.getFloatExtra("nirLiveScore" , 0.8f));
                SingleBaseConfig.getBaseConfig().setDepthLiveScore(data.getFloatExtra("depthLiveScore" , 0.8f));
                SingleBaseConfig.getBaseConfig().setType(data.getIntExtra("type" , 1));
                SingleBaseConfig.getBaseConfig().setLivingControl(data.getBooleanExtra("livingControl" , true));

                SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(data.getIntExtra("rgbAndNirWidth" , 640));
                SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(data.getIntExtra("rgbAndNirHeight" , 480));
                SingleBaseConfig.getBaseConfig().setDepthWidth(data.getIntExtra("depthWidth" , 640));
                SingleBaseConfig.getBaseConfig().setDepthHeight(data.getIntExtra("depthHeight" , 400));
            }else if (requestCode == 103){
                SingleBaseConfig.getBaseConfig().setActiveModel(data.getIntExtra("activeModel" , 1));
                SingleBaseConfig.getBaseConfig().setLiveThreshold(data.getFloatExtra("liveScoreThreshold" , 0.8f));
                SingleBaseConfig.getBaseConfig().setIdThreshold(data.getFloatExtra("idScoreThreshold" , 0.8f));
                SingleBaseConfig.getBaseConfig().setRgbAndNirThreshold(
                        data.getFloatExtra("rgbAndNirScoreThreshold" , 0.8f));
                SingleBaseConfig.getBaseConfig().setCameraLightThreshold(
                        data.getIntExtra("cameraLightThreshold" , 50));
            }else if (requestCode == 104){
                SingleBaseConfig.getBaseConfig().setRgbRevert(data.getBooleanExtra("rgbRevert" , false));
                SingleBaseConfig.getBaseConfig().setRgbDetectDirection(data.getIntExtra("rgbDetectDirection" , 0));
                SingleBaseConfig.getBaseConfig().setMirrorDetectRGB(data.getIntExtra("mirrorDetectRGB" , 0));
                SingleBaseConfig.getBaseConfig().setNirDetectDirection(data.getIntExtra("nirDetectDirection" , 0));

                SingleBaseConfig.getBaseConfig().setMirrorDetectNIR(data.getIntExtra("mirrorDetectNIR" , 0));
                SingleBaseConfig.getBaseConfig().setRgbVideoDirection(data.getIntExtra("rgbVideoDirection" , 0));
                SingleBaseConfig.getBaseConfig().setMirrorVideoRGB(data.getIntExtra("mirrorVideoRGB" , 0));
                SingleBaseConfig.getBaseConfig().setNirVideoDirection(data.getIntExtra("nirVideoDirection" , 0));
                SingleBaseConfig.getBaseConfig().setMirrorVideoNIR(data.getIntExtra("mirrorVideoNIR" , 0));

            }else if (requestCode == 106){
                SingleBaseConfig.getBaseConfig().setDarkEnhance(data.getBooleanExtra("darkEnhance" , false));
                SingleBaseConfig.getBaseConfig().setBestImage(data.getBooleanExtra("bestImage" , true));

            }else if (requestCode == 107){
                SingleBaseConfig.getBaseConfig().setLog(data.getBooleanExtra("isLog" , true));

                if (SingleBaseConfig.getBaseConfig().isLog()) {
                    logSettingQualtify.setText("开启");
                } else {
                    logSettingQualtify.setText("关闭");
                }
            }
            GateConfigUtils.modityJson();
            RegisterConfigUtils.modityJson();
        }
    }
}