package com.baidu.idl.main.facesdk.identifylibrary.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.baidu.idl.main.facesdk.identifylibrary.R;
import com.baidu.idl.main.facesdk.identifylibrary.ZKNIDFPSensor.FingerprintSensor;
import com.baidu.idl.main.facesdk.identifylibrary.ZKNIDFPSensor.ZKNIDFPSensorListener;
import com.baidu.idl.main.facesdk.identifylibrary.ZKUSBManager.ZKUSBManager;
import com.baidu.idl.main.facesdk.identifylibrary.ZKUSBManager.ZKUSBManagerListener;
import com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.identifylibrary.setting.IdentifySettingActivity;
import com.baidu.idl.main.facesdk.identifylibrary.utils.FaceUtils;
import com.baidu.idl.main.facesdk.identifylibrary.utils.PermissionUtils;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.example.datalibrary.activity.BaseActivity;
import com.example.datalibrary.callback.CameraDataCallback;
import com.example.datalibrary.callback.FaceDetectCallBack;
import com.example.datalibrary.gatecamera.CameraPreviewManager;
import com.example.datalibrary.gl.view.GlMantleSurfacView;
import com.example.datalibrary.listener.SdkInitListener;
import com.example.datalibrary.manager.FaceSDKManager;
import com.example.datalibrary.manager.SaveImageManager;
import com.example.datalibrary.model.BDFaceCheckConfig;
import com.example.datalibrary.model.BDFaceImageConfig;
import com.example.datalibrary.model.BDLiveConfig;
import com.example.datalibrary.model.LivenessModel;
import com.example.datalibrary.utils.BitmapUtils;
import com.example.datalibrary.utils.FaceOnDrawTexturViewUtil;
import com.example.datalibrary.utils.ImageUtils;
import com.example.datalibrary.utils.ToastUtils;
import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.core.utils.ToolUtils;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.IDCardType;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import com.zkteco.android.biometric.module.idcard.meta.IDPRPCardInfo;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FaceRGBPersonActivity extends BaseActivity implements View.OnClickListener {
    private String TAG = "hcy--FaceRGBPersonActivity";
    private Context mContext;
    private ImageView testimonyBackIv;
    private ImageView testimonySettingIv;
    private ImageView testimonyAddIv;

    private static final int PICK_PHOTO_FRIST = 100;

    private ImageView testimonyDevelopmentLineIv;
    private TextView testimonyDevelopmentTv;
    private ImageView testimonyPreviewLineIv;
    private TextView testimonyPreviewTv;
    private RelativeLayout testimonyShowRl;
    private ImageView testimonyShowImg;
    private TextView testimonyShowAgainTv;
    private TextView testimonyUploadFilesTv;
    private RelativeLayout testimonyTipsFailRl;

    /*RGB摄像头图像宽和高*/
    /*图片越大，性能消耗越大，也可以选择640*480， 1280*720*/
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private int mLiveType;

    private RelativeLayout personButtomLl;
    private TextView personBaiduTv;
    private ImageView testImageview;
    private Paint paintBg;
    private TextView tvRgbLiveTime;
    private TextView tvRgbLiveScore;
    private RelativeLayout kaifaRelativeLayout;
    private TextView hintAdainIv;
    private ImageView hintShowIv;
    private TextView testimonyTipsFailTv;
    private TextView testimonyTipsPleaseFailTv;
    private float score = 0;
    // 定义一个变量判断是预览模式还是开发模式
    boolean isDevelopment = false;
    private RelativeLayout testRelativeLayout;
    private View view;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private ImageView personKaifaIv;
    private TextView tvFeatureTime;
    private TextView tvFeatureSearchTime;
    private TextView tvAllTime;
    private ImageView developmentAddIv;
    private RelativeLayout hintShowRl;
    private RelativeLayout developmentAddRl;
    private float mRgbLiveScore;
    private ImageView testimonyTipsFailIv;
    // 判断是否有人脸
    private boolean isFace = false;
    private float rgbLivenessScore = 0.0f;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;
    private GlMantleSurfacView glSurfaceView;
    private BDFaceImageConfig bdFaceImageConfig;
    private BDFaceCheckConfig bdFaceCheckConfig;
    private BDLiveConfig bdLiveConfig;

    //身份证读卡器
    private CheckBox checkRepeat = null;
    private ImageView imageView = null;
//    private TextView textSuccessCount = null;
//    private TextView textFailCount = null;
    private TextView textTimeCost = null;
    private TextView textResult = null;
    private TextView textMaxTimeCost = null;
    private CheckBox checkReadFp = null;
    private EditText editSerial = null;
    private boolean bRepeatMode = false;
    private boolean bReadFp = false;

    private IDCardReader idCardReader = null;
    private boolean bStarted = false;
    private boolean bCancel = true;
    private CountDownLatch countDownLatch = null;

    private long timeCostAll = 0;
    private long  timeCostCurrent = 0;
    private int  readSuccessTimes = 0;
    private int  readFailTimes = 0;
    private long maxTimeCost = 0;

    //指纹
    private static final int ZKTECO_VID =   0x1b55;
    private final int REQUEST_PERMISSION_CODE = 9;
    private ZKUSBManager zkusbManager = null;
    private TextView textView = null;
    private ImageView fpImageView = null;
    private EditText editText = null;
    private FingerprintSensor fingerprintSensor = new FingerprintSensor();
    private int usb_vid = ZKTECO_VID;
    private int usb_pid = 0;
    private boolean bFpStarted = false;
    private int deviceIndex = 0;
    private boolean isReseted = false;
    private byte[]  idTemplate1 = null;
    private byte[]  idTemplate2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        setContentView(R.layout.activity_face_rgb_identifylibrary);
        FaceSDKManager.getInstance().emptyFrame();
        initFaceCheck();
        initView();
        checkStoragePermission();
        zkusbManager = new ZKUSBManager(this.getApplicationContext(), zkusbManagerListener);
        zkusbManager.registerUSBPermissionReceiver();
    }

    private void initFaceConfig(int height , int width){
        bdFaceImageConfig = new BDFaceImageConfig(height , width ,
                SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectRGB() ,
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21);
    }
    private void initFaceCheck(){
        bdFaceCheckConfig = FaceUtils.getInstance().getBDFaceCheckConfig();
        bdLiveConfig = FaceUtils.getInstance().getBDLiveConfig();
    }
    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(mContext,
                    FaceUtils.getInstance().getBDFaceSDKConfig() , new SdkInitListener() {
                        @Override
                        public void initStart() {
                        }

                        @Override
                        public void initLicenseSuccess() {
                        }

                        @Override
                        public void initLicenseFail(int errorCode, String msg) {
                        }

                        @Override
                        public void initModelSuccess() {
                            FaceSDKManager.initModelSuccess = true;
                            ToastUtils.toast(mContext, "模型加载成功，欢迎使用");
                        }

                        @Override
                        public void initModelFail(int errorCode, String msg) {
                            FaceSDKManager.initModelSuccess = false;
                            if (errorCode != -12) {
                                ToastUtils.toast(mContext, "模型加载失败，请尝试重启应用");
                            }
                        }
                    });
        }
    }

    private void initView() {
        paintBg = new Paint();
        // 返回
        testimonyBackIv = findViewById(R.id.btn_back);
        testimonyBackIv.setOnClickListener(this);
        // 设置
        testimonySettingIv = findViewById(R.id.btn_setting);
        testimonySettingIv.setOnClickListener(this);
        // 活体状态
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        // 活体阈值
        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // buttom
        personButtomLl = findViewById(R.id.person_buttomLl);
        // 百度大脑技术支持
        personBaiduTv = findViewById(R.id.person_baiduTv);
        // 送检RGB 图像回显
        testImageview = findViewById(R.id.test_rgb_view);
        testRelativeLayout = findViewById(R.id.test_rgb_rl);
        testRelativeLayout.setVisibility(View.GONE);
        personKaifaIv = findViewById(R.id.person_kaifaIv);
        view = findViewById(R.id.mongolia_view);
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        saveCamera.setVisibility(View.GONE);
        spot = findViewById(R.id.spot);

        // ****************预览模式****************
        testimonyPreviewTv = findViewById(R.id.preview_text);
        testimonyPreviewTv.setOnClickListener(this);
        testimonyPreviewLineIv = findViewById(R.id.preview_view);
        // 添加图库图片  +号添加
//        testimonyAddIv = findViewById(R.id.testimony_addIv);
//        testimonyAddIv.setOnClickListener(this);
//        testimonyShowRl = findViewById(R.id.testimony_showRl);
//        testimonyShowImg = findViewById(R.id.testimony_showImg);
//        testimonyShowAgainTv = findViewById(R.id.testimony_showAgainTv);
//        testimonyShowAgainTv.setOnClickListener(this);
//        testimonyUploadFilesTv = findViewById(R.id.testimony_upload_filesTv);
        // 失败提示
        testimonyTipsFailRl = findViewById(R.id.testimony_tips_failRl);
        testimonyTipsFailTv = findViewById(R.id.testimony_tips_failTv);
        testimonyTipsPleaseFailTv = findViewById(R.id.testimony_tips_please_failTv);
        testimonyTipsFailIv = findViewById(R.id.testimony_tips_failIv);

        // ****************开发模式****************
        testimonyDevelopmentTv = findViewById(R.id.develop_text);
        testimonyDevelopmentTv.setOnClickListener(this);
        testimonyDevelopmentLineIv = findViewById(R.id.develop_view);
        // 相似度分数
        tvRgbLiveTime = findViewById(R.id.tv_rgb_live_time);
        // 活体检测耗时
        tvRgbLiveScore = findViewById(R.id.tv_rgb_live_score);
        // 特征抽取耗时
        tvFeatureTime = findViewById(R.id.tv_feature_time);
        // 特征比对耗时
        tvFeatureSearchTime = findViewById(R.id.tv_feature_search_time);
        // 总耗时
        tvAllTime = findViewById(R.id.tv_all_time);
        // 重新上传
//        hintAdainIv = findViewById(R.id.hint_adainTv);
//        hintAdainIv.setOnClickListener(this);
        // 图片展示
        hintShowIv = findViewById(R.id.hint_showIv);
        kaifaRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        // 提示
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        textCompareStatus = findViewById(R.id.text_compare_status);
        // 上传图片
//        developmentAddIv = findViewById(R.id.Development_addIv);
//        developmentAddIv.setOnClickListener(this);
//        hintShowRl = findViewById(R.id.hint_showRl);
//        developmentAddRl = findViewById(R.id.Development_addRl);



        glSurfaceView = findViewById(R.id.camera_textureview);
        glSurfaceView.initSurface(SingleBaseConfig.getBaseConfig().getRgbRevert(),
                SingleBaseConfig.getBaseConfig().getMirrorVideoRGB() , SingleBaseConfig.getBaseConfig().isOpenGl());
        CameraPreviewManager.getInstance().startPreview(/*mContext, */glSurfaceView,
                SingleBaseConfig.getBaseConfig().getRgbVideoDirection() , RGB_WIDTH, RGB_HEIGHT);

        //身份证读取卡器
        imageView = (ImageView)findViewById(R.id.imageView);
        textResult = (TextView)findViewById(R.id.textResult);
        editSerial = (EditText)findViewById(R.id.editSerial);

        //指纹
        textView = (TextView)findViewById(R.id.txtResult);
        imageView = (ImageView)findViewById(R.id.imageFP);
        editText = (EditText)findViewById(R.id.editFeatureFileName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraPreview();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_back) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            finish();
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            // 跳转设置页面
            startActivity(new Intent(mContext, IdentifySettingActivity.class));
            finish();
            // 上传图片
        } else if (id == R.id.testimony_addIv) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_PHOTO_FRIST);
            // 开发模式
        } else if (id == R.id.develop_text) {
            isDevelopment = true;
//            if (testimonyShowImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
//                testimonyTipsFailRl.setVisibility(View.GONE);
//                layoutCompareStatus.setVisibility(View.VISIBLE);
//            } else {
//                testimonyTipsFailRl.setVisibility(View.GONE);
//                layoutCompareStatus.setVisibility(View.GONE);
//            }
            // title显隐
            testimonyDevelopmentLineIv.setVisibility(View.VISIBLE);
            testimonyPreviewLineIv.setVisibility(View.GONE);
            testimonyDevelopmentTv.setTextColor(getResources().getColor(R.color.white));
            testimonyPreviewTv.setTextColor(Color.parseColor("#FF999999"));
            // 百度大脑技术支持隐藏
            personBaiduTv.setVisibility(View.GONE);
            // 预览模式显示buttom隐藏
            personButtomLl.setVisibility(View.GONE);
            // 开发模式显示buttom显示
            kaifaRelativeLayout.setVisibility(View.VISIBLE);
            // RGB 检测图片测试
            testRelativeLayout.setVisibility(View.VISIBLE);
            // 开启保存图片按钮
            saveCamera.setVisibility(View.VISIBLE);
            judgeFirst();
            // 预览模式
        } else if (id == R.id.preview_text) {
            isDevelopment = false;
            if (testimonyShowImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                testimonyTipsFailRl.setVisibility(View.VISIBLE);
                layoutCompareStatus.setVisibility(View.GONE);
            } else {
                testimonyTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            // title显隐
            testimonyDevelopmentLineIv.setVisibility(View.GONE);
            testimonyPreviewLineIv.setVisibility(View.VISIBLE);
            testimonyDevelopmentTv.setTextColor(Color.parseColor("#FF999999"));
            testimonyPreviewTv.setTextColor(getResources().getColor(R.color.white));
            // 百度大脑技术支持显示
            personBaiduTv.setVisibility(View.VISIBLE);
            // RGB 检测图片测试
            testRelativeLayout.setVisibility(View.GONE);
            // 预览模式显示buttom显示
            personButtomLl.setVisibility(View.VISIBLE);
            // 开发模式显示buttom隐藏
            kaifaRelativeLayout.setVisibility(View.GONE);
            // 预览模式重新上传
            // 隐藏保存按钮
            saveCamera.setVisibility(View.GONE);
            isSaveImage = false;
            spot.setVisibility(View.GONE);
        } else if (id == R.id.testimony_showAgainTv) {
            Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent2, PICK_PHOTO_FRIST);
            // 上传图片
        } else if (id == R.id.hint_adainTv) {
            Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent1, PICK_PHOTO_FRIST);
            // 开发模式重新上传
        } else if (id == R.id.Development_addIv) {
            Intent intent3 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent3, PICK_PHOTO_FRIST);
        } else if (id == R.id.save_camera){
            isSaveImage = !isSaveImage;
            if (isSaveImage){
                spot.setVisibility(View.VISIBLE);
                ToastUtils.toast(FaceRGBPersonActivity.this, "存图功能已开启再次点击可关闭");
            }else {
                spot.setVisibility(View.GONE);
            }
        }
    }

    private void judgeFirst(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isIdentifyFirstSave", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            setFirstView(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFirstView(View.GONE);
                }
            },3000);
            editor.putBoolean("isIdentifyFirstSave", false);
            editor.commit();
        }
    }

    private void setFirstView(int visibility){
        findViewById(R.id.first_text_tips).setVisibility(visibility);
        findViewById(R.id.first_circular_tips).setVisibility(visibility);
    }

    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        // TODO ： 临时放置
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        } else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }
        int[] cameraSize =  CameraPreviewManager.getInstance().initCamera();
        initFaceConfig(cameraSize[1] , cameraSize[0]);
        isPause = true;
        CameraPreviewManager.getInstance().setmCameraDataCallback(new CameraDataCallback() {
            @Override
            public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                bdFaceImageConfig.setData(data);
                // 预览模式或者开发模式上传图片成功开始
                if (bdFaceCheckConfig.getSecondFeature() != null) {
                    glSurfaceView.setFrame();
                    // rgb回显图显示
//                    testImageview.setVisibility(View.VISIBLE);
                    // 拿到相机帧数据
                    // 摄像头预览数据进行人脸检测
                    FaceSDKManager.getInstance().onDetectCheck(bdFaceImageConfig, null, null,
                            bdFaceCheckConfig, new FaceDetectCallBack() {
                                @Override
                                public void onFaceDetectCallback(final LivenessModel livenessModel) {
                                    // 预览模式
                                    checkCloseDebugResult(livenessModel);
                                    // 开发模式
//                                    checkOpenDebugResult(livenessModel);
                                    if (isSaveImage){
                                        SaveImageManager.getInstance().saveImage(livenessModel , bdLiveConfig);
                                    }
                                }

                                @Override
                                public void onTip(int code, final String msg) {

                                }

                                @Override
                                public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                                    // 人脸框显示
                                    showFrame(livenessModel);
                                }
                            });


                } else {
                    glSurfaceView.onGlDraw();
                    // 如果开发模式或者预览模式没上传图片则显示蒙层
                    testImageview.setImageResource(R.mipmap.ic_image_video);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.85f, 0.0f);
                    animator.setDuration(3000);
                    view.setBackgroundColor(Color.parseColor("#ffffff"));
                    animator.start();
                }
            }
        });
    }

    /**
     * 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断。实际应用中可注释掉
     *
     * @param rgb
     */
    private void showDetectImage(byte[] rgb) {
        if (rgb == null) {
            return;
        }
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, RGB_HEIGHT,
                RGB_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21,
                SingleBaseConfig.getBaseConfig().getRgbVideoDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorVideoRGB());
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        final Bitmap bitmap = BitmapUtils.getInstaceBmp(imageInstance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testImageview.setVisibility(View.VISIBLE);
                testImageview.setImageBitmap(bitmap);
            }
        });
        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();
    }

    // 预览模式
    private void checkCloseDebugResult(final LivenessModel model) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Log.d(TAG,"checkCloseDebugResult");
                if (model == null) {
//                    Log.d(TAG,"model == null");
                    // 提示隐藏
                    testimonyTipsFailRl.setVisibility(View.GONE);
                    if (testimonyPreviewLineIv.getVisibility() == View.VISIBLE) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.85f, 0.0f);
                        animator.setDuration(3000);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }

                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                view.setBackgroundColor(Color.parseColor("#ffffff"));
                            }
                        });
                        animator.start();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Log.d(TAG,"model != null");
                            score = model.getScore();

                            if (isDevelopment == false) {
//                                Log.d(TAG,"isDevelopment == false");
                                layoutCompareStatus.setVisibility(View.GONE);
                                testimonyTipsFailRl.setVisibility(View.VISIBLE);
                                if (isFace == true) {
//                                    Log.d(TAG,"上传图片不包含人脸");
                                    testimonyTipsFailTv.setText("上传图片不包含人脸");
                                    testimonyTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                                    testimonyTipsPleaseFailTv.setText("无法进行人证比对");
                                    testimonyTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                } else {
                                    if (mLiveType == 0) {
//                                        Log.d(TAG,"mLiveType == 0");
                                        if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                            Log.d(TAG,"人证核验通过");
                                            testimonyTipsFailTv.setText("人证核验通过");
                                            testimonyTipsFailTv.setTextColor(
                                                    Color.parseColor("#FF00BAF2"));
                                            testimonyTipsPleaseFailTv.setText("识别成功");
                                            testimonyTipsFailIv.setImageResource(R.mipmap.tips_success);
                                        } else {
//                                            Log.d(TAG,"人证核验未通过");
                                            testimonyTipsFailTv.setText("人证核验未通过");
                                            testimonyTipsFailTv.setTextColor(
                                                    Color.parseColor("#FFFEC133"));
                                            testimonyTipsPleaseFailTv.setText("请上传正面人脸照片");
                                            testimonyTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                        }
                                    } else {
//                                        Log.d(TAG,"mLiveType != 0");
                                        // 活体阈值判断显示
                                        rgbLivenessScore = model.getRgbLivenessScore();
                                        if (rgbLivenessScore < mRgbLiveScore) {
//                                            Log.d(TAG,"人证核验未通过");
                                            testimonyTipsFailTv.setText("人证核验未通过");
                                            testimonyTipsFailTv.setTextColor(
                                                    Color.parseColor("#FFFEC133"));
                                            testimonyTipsPleaseFailTv.setText("请上传正面人脸照片");
                                            testimonyTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                        } else {
                                            Log.d(TAG,"rgbLivenessScore >= mRgbLiveScore");
                                            if (score > SingleBaseConfig.getBaseConfig()
                                                    .getIdThreshold()) {
                                                Log.d(TAG,"人证核验通过");
                                                testimonyTipsFailTv.setText("人证核验通过");
                                                testimonyTipsFailTv.setTextColor(
                                                        Color.parseColor("#FF00BAF2"));
                                                testimonyTipsPleaseFailTv.setText("识别成功");
                                                testimonyTipsFailIv.setImageResource(
                                                        R.mipmap.tips_success);
                                            } else {
//                                                Log.d(TAG,"人证核验未通过");
                                                testimonyTipsFailTv.setText("人证核验未通过");
                                                testimonyTipsFailTv.setTextColor(
                                                        Color.parseColor("#FFFEC133"));
                                                testimonyTipsPleaseFailTv.setText("请上传正面人脸照片");
                                                testimonyTipsFailIv.setImageResource(
                                                        R.mipmap.tips_fail);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });


                }
            }
        });
    }

    // 开发模式
    private void checkOpenDebugResult(final LivenessModel model) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (model == null) {
                    // 提示隐藏
                    layoutCompareStatus.setVisibility(View.GONE);
                    // 阈值
                    personKaifaIv.setVisibility(View.GONE);
                    // 显示默认图片
                    testImageview.setImageResource(R.mipmap.ic_image_video);
                    // 默认值为0
                    tvRgbLiveTime.setText(String.format("相似度分数：%s", 0));
                    tvRgbLiveScore.setText(String.format("活体检测耗时：%s ms", 0));
                    tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", 0));
                    tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms", 0));
                    tvAllTime.setText(String.format("总耗时：%s ms", 0));
                } else {
                    // rgb回显图赋值显示
                    BDFaceImageInstance image = model.getBdFaceImageInstance();
                    if (image != null) {
                        testImageview.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                    }
                    tvRgbLiveTime.setText(String.format("相似度分数：%s", score));
                    tvRgbLiveScore.setText(String.format("活体检测耗时：%s ms", model.getRgbLivenessDuration()));
                    tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", model.getFeatureDuration()));
                    tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms", model.getCheckDuration()));


                    if (isDevelopment) {
                        testimonyTipsFailRl.setVisibility(View.GONE);
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        if (model .isQualityCheck()) {
                            tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", 0));
                            tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms", 0));
                            long l = model.getRgbDetectDuration() + model.getRgbLivenessDuration();
                            tvAllTime.setText(String.format("总耗时：%s ms", l));
                            personKaifaIv.setVisibility(View.VISIBLE);
                            personKaifaIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                            textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                            /*textCompareStatus.setMaxEms(6)*/;
                            textCompareStatus.setText("请正视摄像头");
                        } else if (isFace == true) {
                            textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                            textCompareStatus.setText("比对失败");
                        } else {
                            if (mLiveType == 0) {
                                tvAllTime.setText(String.format("总耗时：%s ms", model.getAllDetectDuration()));
                                if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                    textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                                    textCompareStatus.setText("比对成功");
                                } else {
                                    textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                                    textCompareStatus.setText("比对失败");
                                }
                            } else {
                                // 活体阈值判断显示
                                rgbLivenessScore = model.getRgbLivenessScore();
                                if (rgbLivenessScore < mRgbLiveScore) {
                                    personKaifaIv.setVisibility(View.VISIBLE);
                                    personKaifaIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                                    textCompareStatus.setTextColor(Color.parseColor("#FECD33"));

//                            textCompareStatus.setMaxEms(7);
                                    textCompareStatus.setText("活体检测未通过");
                                } else {
                                    personKaifaIv.setVisibility(View.VISIBLE);
                                    personKaifaIv.setImageResource(R.mipmap.ic_icon_develop_success);
                                    if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                        textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                                        textCompareStatus.setText("比对成功");
                                    } else {
                                        textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                                        textCompareStatus.setText("比对失败");
                                    }
                                }
                                tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", model.getFeatureDuration()));
                                tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms",
                                        model.getCheckDuration()));

                                tvAllTime.setText(String.format("总耗时：%s ms", model.getAllDetectDuration()));
                            }
                        }
                    }
                }
            }
        });
    }

    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FRIST && (data != null && data.getData() != null)) {
            Uri uri1 = ImageUtils.geturi(data, this);
            try {
                final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri1));
                if (bitmap != null) {
                    byte[] secondFeature = new byte[512];
                    // 提取特征值
                    float ret = FaceSDKManager.getInstance().personDetect(bitmap, secondFeature,
                            FaceUtils.getInstance().getBDFaceCheckConfig() , this);
                    // 提取特征值
                    // 上传图片有人脸显示
                    hintShowIv.setVisibility(View.VISIBLE);
                    testimonyShowImg.setVisibility(View.VISIBLE);
                    hintShowIv.setImageBitmap(bitmap);
                    testimonyShowImg.setImageBitmap(bitmap);
                    if (ret != -1) {
                        isFace = false;
                        // 判断质量检测，针对模糊度、遮挡、角度
                        if (ret == 128) {
                            bdFaceCheckConfig.setSecondFeature(secondFeature);
                            toast("图片特征抽取成功");
                            hintShowRl.setVisibility(View.VISIBLE);
                            testimonyShowRl.setVisibility(View.VISIBLE);
                            testimonyAddIv.setVisibility(View.GONE);
                            testimonyUploadFilesTv.setVisibility(View.GONE);
                            developmentAddRl.setVisibility(View.GONE);
                        } else {
                            ToastUtils.toast(mContext, "图片特征抽取失败");
                        }
                    } else {
                        isFace = true;
                        // 上传图片无人脸隐藏
                        hintShowIv.setVisibility(View.GONE);
                        testimonyShowImg.setVisibility(View.GONE);
                        hintShowRl.setVisibility(View.VISIBLE);
                        testimonyShowRl.setVisibility(View.VISIBLE);
                        testimonyAddIv.setVisibility(View.GONE);
                        testimonyUploadFilesTv.setVisibility(View.GONE);
                        developmentAddRl.setVisibility(View.GONE);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CameraPreviewManager.getInstance().stopPreview();
        closeDevice();

        if (bFpStarted)
        {
            closeFpDevice();
        }
        zkusbManager.unRegisterUSBPermissionReceiver();
    }

    private boolean isPause = false;
    public void showFrame(LivenessModel livenessModel){
        if (livenessModel == null){
            return;
        }

        if (isPause){
            glSurfaceView.onGlDraw(livenessModel.getTrackFaceInfo() ,
                    livenessModel.getBdFaceImageInstance() ,
                    FaceOnDrawTexturViewUtil.drawFaceColor(score > FaceUtils.getInstance().getThreshold()));
        }
    }

    void dealCardImage(Bitmap bitmap){
        if (bitmap != null) {
            byte[] secondFeature = new byte[512];
            // 提取特征值
            float ret = FaceSDKManager.getInstance().personDetect(bitmap, secondFeature,
                    FaceUtils.getInstance().getBDFaceCheckConfig() , this);
            // 提取特征值
            // 上传图片有人脸显示
            if (ret != -1) {
                isFace = false;
                // 判断质量检测，针对模糊度、遮挡、角度
                if (ret == 128) {
                    bdFaceCheckConfig.setSecondFeature(secondFeature);
                    toast("图片特征抽取成功");
                } else {
                    ToastUtils.toast(mContext, "图片特征抽取失败");
                }
            } else {
                isFace = true;
                // 上传图片无人脸隐藏
                ToastUtils.toast(mContext, "上传图片无人脸隐藏");
            }
        }

    }
    private void startIDCardReader(String serialName, int baudRate) {
        if (null != idCardReader)
        {
            IDCardReaderFactory.destroy(idCardReader);
            idCardReader = null;
        }
        // Define output log level
        LogHelper.setLevel(Log.VERBOSE);
        // Start fingerprint sensor
        Map idrparams = new HashMap();
        idrparams.put(ParameterHelper.PARAM_SERIAL_SERIALNAME, serialName);
        idrparams.put(ParameterHelper.PARAM_SERIAL_BAUDRATE, baudRate);
        idCardReader = IDCardReaderFactory.createIDCardReader(this, TransportType.SERIALPORT, idrparams);
    }

//    private void updateStatus()
//    {
//        runOnUiThread(new Runnable() {
//            public void run() {
//                textSuccessCount.setText(String.valueOf(readSuccessTimes));
//                textFailCount.setText(String.valueOf(readFailTimes));
//                if (readSuccessTimes > 0) {
//                    textTimeCost.setText("本次读卡:" + timeCostCurrent + "ms, 平均耗时:" +
//                            timeCostAll / readSuccessTimes + "ms");
//                    textMaxTimeCost.setText(maxTimeCost + "ms");
//                }
//            }
//        });
//    }

    private void setResult(String strText)
    {
        final String flStrText = strText;
        runOnUiThread(new Runnable() {
            public void run() {
                textResult.setText(flStrText);
            }
        });
    }

    private void openDevice(String strSerialName, int baudrate)
    {
        startIDCardReader(strSerialName, baudrate);
        try {
            idCardReader.open(0);
            countDownLatch = new CountDownLatch(1);
            new Thread(new Runnable() {
                public void run() {
                    bCancel = false;
                    while (!bCancel) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                        updateStatus();

                        boolean ret = false;
                        final long nTickstart = System.currentTimeMillis();
                        try {
                            idCardReader.findCard(0);
                            idCardReader.selectCard(0);
                        }catch (IDCardReaderException e)
                        {
                            if (!bRepeatMode)
                            {
                                continue;
                            }
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int cardType = 0;
                        try {
                            if (bReadFp)
                            {
                                cardType = idCardReader.readCardEx(0, 1);
                            }
                            else {
                                cardType = idCardReader.readCardEx(0, 0);
                            }
                        }
                        catch (IDCardReaderException e)
                        {
                            setResult("读卡失败，错误信息：" + e.getMessage());
                            readFailTimes++;
//                            updateStatus();
                            continue;
                        }

                        if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_PRP ||
                                cardType == IDCardType.TYPE_CARD_GAT || cardType == IDCardType.TYPE_CARD_PRP2)
                        {
                            readSuccessTimes++;
                            timeCostCurrent = System.currentTimeMillis()-nTickstart;
                            timeCostAll += timeCostCurrent;
                            if (timeCostCurrent > maxTimeCost)
                            {
                                maxTimeCost = timeCostCurrent;
                            }
                            final long nTickCommuUsed = (System.currentTimeMillis()-nTickstart);
                            if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_GAT)
                            {
                                IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                                final String name = idCardInfo.getName();
                                final String sex = idCardInfo.getSex();
                                final String nation = idCardInfo.getNation();
                                final String born = idCardInfo.getBirth();
                                final String licid = idCardInfo.getId();
                                final String depart = idCardInfo.getDepart();
                                final String expireDate = idCardInfo.getValidityTime();
                                final String addr = idCardInfo.getAddress();
                                final String passNo = idCardInfo.getPassNum();
                                final int visaTimes = idCardInfo.getVisaTimes();
                                Bitmap bmpPhoto = null;
                                if (idCardInfo.getPhotolength() > 0) {
                                    byte[] buf = new byte[WLTService.imgLength];
                                    if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                                        bmpPhoto = IDPhotoHelper.Bgr2Bitmap(buf);
                                    }
                                }
                                final int final_cardType = cardType;
                                final Bitmap final_bmpPhoto = bmpPhoto;
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        imageView.setImageBitmap(final_bmpPhoto);
                                        dealCardImage(final_bmpPhoto);
                                        String result = "";
                                        if (final_cardType == IDCardType.TYPE_CARD_SFZ)
                                        {
                                            result += "读取居民身份证成功！";
                                            result += "姓名：" + name;
                                            result += ",性别：" + sex;
                                            result += ",民族：" + nation;
                                            result += ",出生日期：" + born;
                                            result += ",地址：" + addr;
                                            result += ",身份号码：" + licid;
                                            result += "，签发机关：" + depart;
                                            result += "，有效期：" + expireDate;
                                            setResult(result);
                                        }
                                        else
                                        {
                                            result += "读取港澳台居住证成功！";
                                            result += "姓名：" + name;
                                            result += ",性别：" + sex;
                                            result += ",出生日期：" + born;
                                            result += ",身份号码：" + licid;
                                            result += "，签发机关：" + depart;
                                            result += "，有效期：" + expireDate;
                                            result += "，签发次数：" + visaTimes;
                                            result += "，通行证号码：" + passNo;
                                            setResult(result);
                                        }
                                    }
                                });
                            }
                            else
                            {
                                IDPRPCardInfo idprpCardInfo = idCardReader.getLastPRPIDCardInfo();
                                final String cnName = idprpCardInfo.getCnName();
                                final String enName = idprpCardInfo.getEnName();
                                final String sex = idprpCardInfo.getSex();
                                final String country = idprpCardInfo.getCountry() + "/" + idprpCardInfo.getCountryCode();//国家/国家地区代码
                                final String born = idprpCardInfo.getBirth();
                                final String licid = idprpCardInfo.getId();
                                final String expireDate = idprpCardInfo.getValidityTime();
                                final String relatecode = idprpCardInfo.getRelateCode();
                                final String oldLicId = idprpCardInfo.getOldId();
                                final int    visaTimes = idprpCardInfo.getVisaTimes();

                                Bitmap bmpPhoto = null;
                                if (idprpCardInfo.getPhotolength() > 0) {
                                    byte[] buf = new byte[WLTService.imgLength];
                                    if (1 == WLTService.wlt2Bmp(idprpCardInfo.getPhoto(), buf)) {
                                        bmpPhoto = IDPhotoHelper.Bgr2Bitmap(buf);
                                    }
                                }
                                final int final_cardType = cardType;
                                final Bitmap final_bmpPhoto = bmpPhoto;
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        imageView.setImageBitmap(final_bmpPhoto);
                                        String result = "";
                                        if (final_cardType == IDCardType.TYPE_CARD_PRP)
                                        {
                                            result += "读取外国人永久居留身份证(2017)成功！";
                                            result += "中文姓名：" + cnName;
                                            result += "英文姓名：" + enName;
                                            result += ",性别：" + sex;
                                            result += ",国家：" + country;
                                            result += ",出生日期：" + born;
                                            result += ",身份号码：" + licid;
                                            result += "，有效期：" + expireDate;
                                            setResult(result);
                                        }
                                        else
                                        {
                                            result += "读取外国人永久居留身份证(2023)成功！";
                                            result += "中文姓名：" + cnName;
                                            result += "英文姓名：" + enName;
                                            result += ",性别：" + sex;
                                            result += ",国家：" + country;
                                            result +=",出生日期：" + born;
                                            result += ",身份号码：" + licid;
                                            result += "，有效期：" + expireDate;
                                            result += "，换证次数：" + visaTimes;
                                            if (!relatecode.isEmpty())
                                            {
                                                result += "，既往身份号码关联项：" + relatecode;
                                                result += "，既往身份号码：" + oldLicId;
                                            }
                                            setResult(result);
                                        }
                                    }
                                });
                            }
                        }
                        else
                        {
                            readFailTimes++;
                        }
                    }
                    countDownLatch.countDown();
                }
            }).start();
            bStarted = true;
            setResult("打开设备成功，SAMID:" + idCardReader.getSAMID(0));
        } catch (IDCardReaderException e) {
            e.printStackTrace();
            setResult("打开设备失败");
        }
    }


    public void onBnStart(View view) {
        bRepeatMode = false;
        bReadFp = false;
        readFailTimes = 0;
        readSuccessTimes = 0;
        timeCostAll = 0;
        timeCostCurrent = 0;
        maxTimeCost = 0;

        String strSerialName = editSerial.getText().toString();
        if (null == strSerialName || strSerialName.isEmpty())
        {
            setResult("请输入串口路径！");
            return;
        }

        bRepeatMode = true;
//        if (checkRepeat.isChecked())
//        {
//
//        }

        bReadFp = true;
//        if (checkReadFp.isChecked())
//        {
//            bReadFp = true;
//        }
        openDevice(strSerialName, 115200);
    }

    private void closeDevice()
    {
        if (bStarted)
        {
            bCancel = true;
            if (null != countDownLatch)
            {
                try {
                    countDownLatch.await(2*1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch = null;
            }
            try {
                idCardReader.close(0);
            } catch (IDCardReaderException e) {
                e.printStackTrace();
            }
            bStarted = false;
        }
    }

    public void onBnStop(View view) {
        closeDevice();
        setResult("设备断开连接");
    }

    private ZKUSBManagerListener zkusbManagerListener = new ZKUSBManagerListener() {
        @Override
        public void onCheckPermission(int result) {
            afterGetUsbPermission();
        }

        @Override
        public void onUSBArrived(UsbDevice device) {
            if (bStarted)
            {
                closeDevice();
                tryGetUSBPermission();
            }
        }

        @Override
        public void onUSBRemoved(UsbDevice device) {
            LogHelper.d("usb removed!");
        }
    };

    /**
     * storage permission
     */
    private void checkStoragePermission() {
        String[] permission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        ArrayList<String> deniedPermissions = PermissionUtils.checkPermissions(this, permission);
        if (deniedPermissions.isEmpty()) {
            //permission all granted
            Log.i(TAG, "[checkStoragePermission]: all granted");
        } else {
            int size = deniedPermissions.size();
            String[] deniedPermissionArray = deniedPermissions.toArray(new String[size]);
            PermissionUtils.requestPermission(this, deniedPermissionArray, REQUEST_PERMISSION_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                boolean granted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        granted = false;
                    }
                }
                if (granted) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied,The application can't run on this device", Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
    }

    private ZKNIDFPSensorListener zknidfpSensorListener = new ZKNIDFPSensorListener() {
        @Override
        public void onCapture(byte[] fpImage) {
            final Bitmap bitmap = ToolUtils.renderCroppedGreyScaleBitmap(fpImage, fingerprintSensor.getImageWidth(), fingerprintSensor.getImageHeight());
            runOnUiThread(new Runnable() {
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
            byte imageQuality = fingerprintSensor.getImageQuality(fpImage);
            String strText;
            strText = "image quality:" + imageQuality;
            if (imageQuality < 45 || null == idTemplate1)
            {
                setFpResult(strText);
                return;
            }

            /*
            {
                // 测试提取特征和特征比对;
                byte[] feature = new byte[512];
                if (fingerprintSensor.extract(fpImage, feature))
                {
                    float score = fingerprintSensor.featureMatch(feature, idTemplate1);
                    if (score < 0.35 && null != idTemplate2)    //第一个比对失败比对第二个
                    {
                        score = fingerprintSensor.featureMatch(feature, idTemplate2);
                    }
                    strText += "\r\n";
                    strText += "Match result:" + score;
                    setResult(strText);
                }
                else
                {
                    strText += "\r\n";
                    strText += "extract failed";
                    setResult(strText);
                }
            }
             */

            float score = fingerprintSensor.imageMatch(fpImage, idTemplate1);
            if (score < 0.35 && null != idTemplate2)    //第一个比对失败比对第二个
            {
                score = fingerprintSensor.imageMatch(fpImage, idTemplate2);
            }
            strText += "\r\n";
            strText += "Match result:" + score;
            setFpResult(strText);
        }

        @Override
        public void onException(int count) {
            LogHelper.e("usb exception!!!");
            if (!isReseted) {
                fingerprintSensor.resetEx(deviceIndex, getApplicationContext(), usb_vid, usb_pid);
                isReseted = true;
            }
        }
    };

    private boolean enumSensor()
    {
        UsbManager usbManager = (UsbManager)this.getSystemService(Context.USB_SERVICE);
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            int device_vid = device.getVendorId();
            int device_pid = device.getProductId();
            if (device_vid == ZKTECO_VID && (device_pid >= 0x0300 && device_pid <= 0x0310))
            //if (device_vid == ZKTECO_VID && device_pid == 0x0304) //FS200-R
            {
                usb_pid = device_pid;
                return true;
            }
        }
        return false;
    }


    private void tryGetUSBPermission() {
        zkusbManager.initUSBPermission(usb_vid, usb_pid);
    }

    private void afterGetUsbPermission()
    {
        openFpDevice();
    }

    private void openFpDevice()
    {
        isReseted = false;
        boolean bResult = fingerprintSensor.open(deviceIndex, getApplicationContext(), usb_vid, usb_pid);
        if (bResult)
        {
            {
                // device parameter
                LogHelper.d("sdk version" + fingerprintSensor.getSDKVersion());
                LogHelper.d("firmware version" + fingerprintSensor.getFirmwareVersion());
                LogHelper.d("serial:" + fingerprintSensor.getSerialNumber());
                LogHelper.d("width=" + fingerprintSensor.getImageWidth() + ", height=" + fingerprintSensor.getImageHeight());
            }
            fingerprintSensor.setDeviceListener(zknidfpSensorListener);
            fingerprintSensor.startCapture();
            bStarted = true;
            textView.setText("connect success!");
        }
        else
        {
            fingerprintSensor.resetEx(deviceIndex, getApplicationContext(), usb_vid, usb_pid);
            textView.setText("connect failed!");
        }
    }

    private void closeFpDevice()
    {
        if (bStarted)
        {
            fingerprintSensor.stopCapture();
            fingerprintSensor.close();
            bStarted = false;
        }
    }

    public void onBnFpStart(View view) {
        if (bStarted)
        {
            textView.setText("Device already connected!");
            return;
        }
        if (!enumSensor())
        {
            textView.setText("Device not found!");
            return;
        }
        tryGetUSBPermission();
    }

    public void onBnFpStop(View view) {
        if (!bStarted)
        {
            textView.setText("Device not connected!");
            return;
        }
        closeFpDevice();
        textView.setText("Device closed!");
    }

    public void onBnFpImport(View view) {
        if (!bStarted)
        {
            textView.setText("Device not connected!");
            return;
        }
        String strFileName = editText.getText().toString();
        if (null == strFileName || strFileName.isEmpty())
        {
            textView.setText("Please input filepath!");
            return;
        }
        byte[] tempData = readFile(strFileName);
        if (null == tempData || (tempData.length != 512 && tempData.length != 1024))
        {
            textView.setText("invalid template data!");
            return;
        }
        idTemplate1 = new byte[512];
        idTemplate2 = null;
        System.arraycopy(tempData, 0, idTemplate1, 0, 512);
        if (tempData.length == 1024)
        {
            idTemplate2 = new byte[512];
            System.arraycopy(tempData, 512, idTemplate2, 0, 512);
        }
        textView.setText("Import success!");
    }

    public static byte[] readFile(final String filePath) {
        DataInputStream dis = null;
        ByteArrayOutputStream baos = null;

        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
            byte[] buffer = new byte[4096];
            int len = 0;
            int count = 0;
            baos = new ByteArrayOutputStream();

            while ((count = dis.read(buffer, 0, buffer.length)) > 0) {
                len += count;
                baos.write(buffer, 0, count);
            }
            if (len > 0) {
                return baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private void setFpResult(String result)
    {
        final String mStrText = result;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(mStrText);
            }
        });
    }

}