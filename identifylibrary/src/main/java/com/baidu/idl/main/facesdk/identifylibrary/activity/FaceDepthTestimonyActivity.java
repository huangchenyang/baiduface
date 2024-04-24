package com.baidu.idl.main.facesdk.identifylibrary.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.identifylibrary.R;
import com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.identifylibrary.setting.IdentifySettingActivity;
import com.baidu.idl.main.facesdk.identifylibrary.utils.FaceUtils;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.example.datalibrary.activity.BaseOrbbecActivity;
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

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.ImageRegistrationMode;
import org.openni.OpenNI;
import org.openni.PixelFormat;
import org.openni.SensorType;
import org.openni.VideoFrameRef;
import org.openni.VideoMode;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;
import org.openni.android.OpenNIView;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class FaceDepthTestimonyActivity extends BaseOrbbecActivity implements OpenNIHelper.DeviceOpenListener,
        View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int DEPTH_NEED_PERMISSION = 33;

    /*RGB摄像头图像宽和高*/
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    // Depth摄像头图像宽和高
    private int depthWidth = SingleBaseConfig.getBaseConfig().getDepthWidth();
    private int depthHeight = SingleBaseConfig.getBaseConfig().getDepthHeight();

    private Context mContext;
    private RelativeLayout depthRl;

    private TextView testimonyPreviewTv;
    private TextView testimonyDevelopmentTv;
    private ImageView testimonyPreviewLineIv;
    private ImageView testimonyDevelopmentLineIv;

    // 人脸框绘制

    // 摄像头驱动
    private Device mDevice;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream mDepthStream;

    // 设备初始化状态标记
    private boolean initOk = false;
    // 循环取深度图像数据
    private boolean exit = false;
    private Object sync = new Object();

    /*当前摄像头类型*/
    private static int cameraType;
    private Thread thread;
    private OpenNIView mDepthGLView;

    // 摄像头采集数据
    private ImageView rgbDepthTestView;

    private byte[] firstFeature = new byte[512];
    private byte[] secondFeature = new byte[512];


    private static final int PICK_PHOTO_FRIST = 100;

    private TextView tvDepthLiveTime;
    private TextView tvDepthLiveScore;
    private RelativeLayout kaifaRelativeLayout;
    private ImageView depthAddIv;
    private TextView depthUploadFilesTv;
    private RelativeLayout depthShowRl;
    private TextView depthShowAgainTv;
    private TextView hintAdainTv;
    private ImageView hintShowIv;
    private ImageView depthShowImg;
    private RelativeLayout livenessTipsFailRl;
    private TextView livenessTipsFailTv;
    private TextView livenessTipsPleaseFailTv;


    // 定义一个变量判断是预览模式还是开发模式
    boolean isDevelopment = false;
    private RelativeLayout rgbDepthTestLl;
    private RelativeLayout depthTestRl;
    private TextView depthBaiduTv;
    private View view;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private TextView tvFeatureTime;
    private TextView tvFeatureSearchTime;
    private TextView tvAllTime;
    private float score = 0;
    private ImageView rgbTestIv;
    private RelativeLayout developmentAddRl;
    private RelativeLayout hintShowRl;
    private ImageView depthTestIv;
    private float rgbLiveScore;
    private float depthLiveScore;

    // 判断是否有人脸
    private boolean isFace = false;
    private ImageView livenessTipsFailIv;
    private float depthLivenessScore;
    private float rgbLivenessScore;
    // 特征提取
    private RelativeLayout personButtomLl;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;
    private GlMantleSurfacView glSurfaceView;
    private BDFaceImageConfig bdFaceImageConfig;
    private BDFaceImageConfig bdDepthFaceImageConfig;
    private BDFaceCheckConfig bdFaceCheckConfig;
    private BDLiveConfig bdLiveConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListener();
        setContentView(R.layout.activity_face_depth_identifylibrary);
        FaceSDKManager.getInstance().emptyFrame();
        mContext = this;
        PreferencesUtil.initPrefs(this);
        cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
        initFaceCheck();
        initView();

    }
    private void initFaceConfig(int height , int width){
        bdFaceImageConfig = new BDFaceImageConfig(height , width ,
                SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectRGB() ,
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21);
    }
    private void initDepthFaceConfig(int height , int width){
        bdDepthFaceImageConfig = new BDFaceImageConfig(height , width ,
                SingleBaseConfig.getBaseConfig().getNirDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectNIR() ,
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_DEPTH);
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
        depthWidth = SingleBaseConfig.getBaseConfig().getDepthWidth();
        depthHeight = SingleBaseConfig.getBaseConfig().getDepthHeight();
        // 获取总布局
        depthRl = findViewById(R.id.depth_Rl);
        // ****************title****************
        // 返回
        ImageView testimonyBackIv = findViewById(R.id.btn_back);
        testimonyBackIv.setOnClickListener(this);
        testimonyPreviewTv = findViewById(R.id.preview_text);
        testimonyPreviewTv.setOnClickListener(this);
        testimonyPreviewLineIv = findViewById(R.id.preview_view);
        testimonyDevelopmentTv = findViewById(R.id.develop_text);
        testimonyDevelopmentTv.setOnClickListener(this);
        testimonyDevelopmentLineIv = findViewById(R.id.develop_view);
        // 设置
        ImageView testimonySettingIv = findViewById(R.id.btn_setting);
        testimonySettingIv.setOnClickListener(this);

        // ****************预览模式****************
        depthShowRl = findViewById(R.id.testimony_showRl);
        depthShowImg = findViewById(R.id.testimony_showImg);
        // 重新上传
        depthShowAgainTv = findViewById(R.id.testimony_showAgainTv);
        depthShowAgainTv.setOnClickListener(this);
        depthBaiduTv = findViewById(R.id.depth_baiduTv);


        // ****************开发模式****************
        // 深度摄像头数据回显
        mDepthGLView = findViewById(R.id.depth_camera_preview_view);
        mDepthGLView.setVisibility(View.VISIBLE);
        depthTestRl = findViewById(R.id.depth_test_Rl);
        // RGB 检测图片测试
        rgbDepthTestView = findViewById(R.id.rgb_depth_test_view);
        rgbDepthTestLl = findViewById(R.id.test_rgb_rl);

        kaifaRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        hintShowIv = findViewById(R.id.hint_showIv);
        hintAdainTv = findViewById(R.id.hint_adainTv);
        hintAdainTv.setOnClickListener(this);
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        saveCamera.setVisibility(View.GONE);
        spot = findViewById(R.id.spot);

        // ****************buttom****************
        personButtomLl = findViewById(R.id.person_buttomLl);
        depthAddIv = findViewById(R.id.testimony_addIv);
        depthAddIv.setOnClickListener(this);
        depthUploadFilesTv = findViewById(R.id.testimony_upload_filesTv);
        // 失败提示
        livenessTipsFailRl = findViewById(R.id.testimony_tips_failRl);
        livenessTipsFailTv = findViewById(R.id.testimony_tips_failTv);
        livenessTipsPleaseFailTv = findViewById(R.id.testimony_tips_please_failTv);
        livenessTipsFailIv = findViewById(R.id.testimony_tips_failIv);

        view = findViewById(R.id.mongolia_view);

        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        textCompareStatus = findViewById(R.id.text_compare_status);

        // 相似度分数
        tvDepthLiveTime = findViewById(R.id.tv_rgb_live_time);
        // 活体检测耗时
        tvDepthLiveScore = findViewById(R.id.tv_rgb_live_score);
        // 特征抽取耗时
        tvFeatureTime = findViewById(R.id.tv_feature_time);
        // 特征比对耗时
        tvFeatureSearchTime = findViewById(R.id.tv_feature_search_time);
        // 总耗时
        tvAllTime = findViewById(R.id.tv_all_time);
        rgbTestIv = findViewById(R.id.rgb_test_iv);
        developmentAddRl = findViewById(R.id.Development_addRl);
        ImageView developmentAddTv = findViewById(R.id.Development_addIv);
        developmentAddTv.setOnClickListener(this);
        hintShowRl = findViewById(R.id.hint_showRl);
        depthTestIv = findViewById(R.id.depth_test_iv);

        // RGB 阈值
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // depth 阈值
        depthLiveScore = SingleBaseConfig.getBaseConfig().getDepthLiveScore();

        glSurfaceView = findViewById(R.id.camera_textureview);
        glSurfaceView.initSurface(SingleBaseConfig.getBaseConfig().getRgbRevert(),
                SingleBaseConfig.getBaseConfig().getMirrorVideoRGB() , SingleBaseConfig.getBaseConfig().isOpenGl());
        CameraPreviewManager.getInstance().startPreview(/*mContext, */glSurfaceView,
                SingleBaseConfig.getBaseConfig().getRgbVideoDirection() , RGB_WIDTH, RGB_HEIGHT);

    }

    /**
     * 在device 启动时候初始化USB 驱动
     *
     * @param device
     */
    private void initUsbDevice(UsbDevice device) {

        List<DeviceInfo> opennilist = OpenNI.enumerateDevices();
        if (opennilist.size() <= 0) {
            Toast.makeText(this, " openni enumerateDevices 0 devices", Toast.LENGTH_LONG).show();
            return;
        }
        this.mDevice = null;
        // Find mDevice ID
        for (int i = 0; i < opennilist.size(); i++) {
            if (opennilist.get(i).getUsbProductId() == device.getProductId()) {
                this.mDevice = Device.open();
                break;
            }
        }

        if (this.mDevice == null) {
            Toast.makeText(this, " openni open devices failed: " + device.getDeviceName(),
                    Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void startCameraPreview() {
        // 设置前置摄像头
//        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1){
            CameraPreviewManager.getInstance().setCameraFacing(SingleBaseConfig.getBaseConfig().getRBGCameraId());
        }else {
            CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);
        }
        int[] cameraSize =  CameraPreviewManager.getInstance().initCamera();
        initFaceConfig(cameraSize[1] , cameraSize[0]);

        CameraPreviewManager.getInstance().setmCameraDataCallback(new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] rgbData, Camera camera, int srcWidth, int srcHeight) {
                        dealRgb(rgbData);
                    }
                });

//        boolean isRGBDisplay = SingleBaseConfig.getBaseConfig().getDisplay();
//        if (isRGBDisplay) {
//            showDetectImage(rgbData);
//        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        // 返回
        if (id == R.id.btn_back) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            finish();
            // 设置
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            // 跳转设置页面
            startActivity(new Intent(mContext, IdentifySettingActivity.class));
            finish();
            // 预览模式
        } else if (id == R.id.preview_text) {
            if (depthShowImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                livenessTipsFailRl.setVisibility(View.VISIBLE);
                layoutCompareStatus.setVisibility(View.GONE);
            } else {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            isDevelopment = false;
            testimonyDevelopmentTv.setTextColor(Color.parseColor("#FF999999"));
            testimonyPreviewTv.setTextColor(getResources().getColor(R.color.white));
            testimonyPreviewLineIv.setVisibility(View.VISIBLE);
            testimonyDevelopmentLineIv.setVisibility(View.GONE);
            rgbDepthTestLl.setVisibility(View.GONE);
            depthTestRl.setVisibility(View.GONE);
            personButtomLl.setVisibility(View.VISIBLE);
            kaifaRelativeLayout.setVisibility(View.GONE);
            depthBaiduTv.setVisibility(View.VISIBLE);
            mDepthGLView.setVisibility(View.INVISIBLE);
            saveCamera.setVisibility(View.GONE);
            isSaveImage = false;
            spot.setVisibility(View.GONE);

            // 开发模式
        } else if (id == R.id.develop_text) {
            if (depthShowImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.VISIBLE);
            } else {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            isDevelopment = true;
            testimonyDevelopmentTv.setTextColor(getResources().getColor(R.color.white));
            testimonyPreviewTv.setTextColor(Color.parseColor("#FF999999"));
            testimonyPreviewLineIv.setVisibility(View.GONE);
            testimonyDevelopmentLineIv.setVisibility(View.VISIBLE);
            rgbDepthTestLl.setVisibility(View.VISIBLE);
            depthTestRl.setVisibility(View.VISIBLE);
            personButtomLl.setVisibility(View.GONE);
            kaifaRelativeLayout.setVisibility(View.VISIBLE);
            depthBaiduTv.setVisibility(View.GONE);
            mDepthGLView.setVisibility(View.VISIBLE);
            saveCamera.setVisibility(View.VISIBLE);
            judgeFirst();
        } else if (id == R.id.testimony_addIv) {
            // 从相册取图片
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_PHOTO_FRIST);
        } else if (id == R.id.testimony_showAgainTv) {
            // 从相册取图片
            Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent1, PICK_PHOTO_FRIST);
        } else if (id == R.id.hint_adainTv) {
            // 从相册取图片
            Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent2, PICK_PHOTO_FRIST);
        } else if (id == R.id.Development_addIv) {
            // 从相册取图片
            Intent intent3 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent3, PICK_PHOTO_FRIST);
        } else if (id == R.id.save_camera){
            isSaveImage = !isSaveImage;
            if (isSaveImage){
                spot.setVisibility(View.VISIBLE);
                ToastUtils.toast(FaceDepthTestimonyActivity.this, "存图功能已开启再次点击可关闭");
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
    @Override
    protected void onResume() {
        super.onResume();
        // 摄像头图像预览
        startCameraPreview();
        // 初始化 深度摄像头
        exit = false;
        mOpenNIHelper = new OpenNIHelper(this);
        mOpenNIHelper.requestDeviceOpen(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        CameraPreviewManager.getInstance().stopPreview();
        exit = true;
        if (initOk) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDepthStream != null) {
                mDepthStream.stop();
                mDepthStream.destroy();
                mDepthStream = null;
            }
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        }
        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
            mOpenNIHelper = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        exit = true;
        if (initOk) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mDepthStream != null) {
                mDepthStream.stop();
                mDepthStream.destroy();
                mDepthStream = null;
            }
            if (mDevice != null) {
                mDevice.close();
                mDevice = null;
            }
        }
        if (mOpenNIHelper != null) {
            mOpenNIHelper.shutdown();
            mOpenNIHelper = null;
        }
    }


    private void dealRgb(byte[] data) {
        glSurfaceView.setFrame();
        bdFaceImageConfig.setData(data);
        checkData();
    }

    private void dealDepth(byte[] data) {
        bdDepthFaceImageConfig.setData(data);
        checkData();
    }

    private synchronized void checkData() {
        if (bdFaceImageConfig.data != null && bdDepthFaceImageConfig != null){
            if (bdFaceCheckConfig.getSecondFeature() != null) {
                FaceSDKManager.getInstance().onDetectCheck(bdFaceImageConfig, null, bdDepthFaceImageConfig,
                    bdFaceCheckConfig, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(final LivenessModel livenessModel) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 预览模式
                                    checkCloseDebugResult(livenessModel);
                                    // 开发模式
                                    checkOpenDebugResult(livenessModel);
                                    if (isSaveImage){
                                        SaveImageManager.getInstance().saveImage(livenessModel , bdLiveConfig);
                                    }
                                }


//                            }
                            });
                        }

                        @Override
                        public void onTip(int code, String msg) {
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            showFrame(livenessModel);
                        }
                    });
            } else {
                glSurfaceView.onGlDraw();
                // 如果开发模式或者预览模式没上传图片则显示蒙层
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.85f, 0.0f);
                animator.setDuration(3000);
                view.setBackgroundColor(Color.parseColor("#ffffff"));
                animator.start();
            }
        }

    }

    /**
     * 获取系统相册内的某一张图片
     *
     * @return
     */
    private Bitmap getBitmap() {
        Intent intent = getIntent();
        byte[] imageBitmaps = intent.getByteArrayExtra("imageBitmap");
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBitmaps, 0, imageBitmaps.length);
        if (bitmap != null) {
            return bitmap;
        }
        return null;
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
                rgbDepthTestView.setVisibility(View.VISIBLE);
                rgbDepthTestView.setImageBitmap(bitmap);
            }
        });

        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();

    }


    @Override
    public void onDeviceOpened(UsbDevice device) {
        initUsbDevice(device);
        mDepthStream = VideoStream.create(this.mDevice, SensorType.DEPTH);
        if (mDepthStream != null) {
            List<VideoMode> mVideoModes = mDepthStream.getSensorInfo().getSupportedVideoModes();
            for (VideoMode mode : mVideoModes) {
                int x = mode.getResolutionX();
                int y = mode.getResolutionY();
                if (cameraType == 1) {
                    if (x == depthHeight && y == depthWidth && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                } else {
                    if (x == depthWidth && y == depthHeight && mode.getPixelFormat() == PixelFormat.DEPTH_1_MM) {
                        mDepthStream.setVideoMode(mode);
                        this.mDevice.setImageRegistrationMode(ImageRegistrationMode.DEPTH_TO_COLOR);
                        break;
                    }
                }

            }
            initDepthFaceConfig(depthHeight , depthWidth);
            startThread();
        }
    }

    @Override
    public void onDeviceOpenFailed(String msg) {
        showAlertAndExit("Open Device failed: " + msg);
    }

    @Override
    public void onDeviceNotFound() {

    }

    /**
     * 开启线程接收深度数据
     */
    private void startThread() {
        initOk = true;
        thread = new Thread() {

            @Override
            public void run() {

                List<VideoStream> streams = new ArrayList<VideoStream>();

                streams.add(mDepthStream);
                mDepthStream.start();
                while (!exit) {

                    try {
                        OpenNI.waitForAnyStream(streams, 2000);

                    } catch (TimeoutException e) {
                        e.printStackTrace();
                        continue;
                    }

                    synchronized (sync) {
                        if (mDepthStream != null) {
                            mDepthGLView.update(mDepthStream);
                            VideoFrameRef videoFrameRef = mDepthStream.readFrame();
                            ByteBuffer depthByteBuf = videoFrameRef.getData();
                            if (depthByteBuf != null) {
                                int depthLen = depthByteBuf.remaining();
                                byte[] depthByte = new byte[depthLen];
                                depthByteBuf.get(depthByte);
                                dealDepth(depthByte);
                            }
                            videoFrameRef.release();
                        }
                    }

                }
            }
        };

        thread.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FRIST && (data != null && data.getData() != null)) {
            Uri uri1 = ImageUtils.geturi(data, this);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri1));
                if (bitmap != null) {
//                    syncFeature(bitmap, secondFeature, 2, true);
                    // 提取特征值
                    float ret = FaceSDKManager.getInstance().personDetect(bitmap, secondFeature,
                            FaceUtils.getInstance().getBDFaceCheckConfig() , this);
                    depthShowImg.setVisibility(View.VISIBLE);
                    hintShowIv.setVisibility(View.VISIBLE);
                    depthShowImg.setImageBitmap(bitmap);
                    hintShowIv.setImageBitmap(bitmap);

                    if (ret != -1) {
                        isFace = false;
                        // 判断质量检测，针对模糊度、遮挡、角度
                        if (ret == 128) {
                            bdFaceCheckConfig.setSecondFeature(secondFeature);
                            toast("图片特征抽取成功");
                            developmentAddRl.setVisibility(View.GONE);
                            depthUploadFilesTv.setVisibility(View.GONE);
                            depthAddIv.setVisibility(View.GONE);
                            hintShowRl.setVisibility(View.VISIBLE);
                            depthShowRl.setVisibility(View.VISIBLE);
                        } else {
                            ToastUtils.toast(mContext, "图片特征抽取失败");
                        }
                    } else {
                        isFace = true;
                        // 上传图片无人脸隐藏
                        depthShowImg.setVisibility(View.GONE);
                        hintShowIv.setVisibility(View.GONE);
                        developmentAddRl.setVisibility(View.GONE);
                        depthUploadFilesTv.setVisibility(View.GONE);
                        depthAddIv.setVisibility(View.GONE);
                        hintShowRl.setVisibility(View.VISIBLE);
                        depthShowRl.setVisibility(View.VISIBLE);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 预览模式
    private void checkCloseDebugResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    livenessTipsFailRl.setVisibility(View.GONE);

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
                            long startCompareTime = System.currentTimeMillis();
                            score = livenessModel.getScore();
                            if (isDevelopment == false) {
                                layoutCompareStatus.setVisibility(View.GONE);
                                livenessTipsFailRl.setVisibility(View.VISIBLE);
                                if (isFace == true) {
                                    livenessTipsFailTv.setText("上传图片不包含人脸");
                                    livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                                    livenessTipsPleaseFailTv.setText("无法进行人证比对");
                                    livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                } else {
                                    rgbLivenessScore = livenessModel.getRgbLivenessScore();
                                    depthLivenessScore = livenessModel.getDepthLivenessScore();
                                    if (rgbLivenessScore > rgbLiveScore && depthLivenessScore
                                            > depthLiveScore) {
                                        if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                            livenessTipsFailTv.setText("人证核验通过");
                                            livenessTipsFailTv.setTextColor(
                                                    Color.parseColor("#FF00BAF2"));
                                            livenessTipsPleaseFailTv.setText("识别成功");
                                            livenessTipsFailIv.setImageResource(R.mipmap.tips_success);
                                        } else {
                                            livenessTipsFailTv.setText("人证核验未通过");
                                            livenessTipsFailTv.setTextColor(
                                                    Color.parseColor("#FFFEC133"));
                                            livenessTipsPleaseFailTv.setText("请上传正面人脸照片");
                                            livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                        }
                                    } else {
                                        livenessTipsFailTv.setText("人证核验未通过");
                                        livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                                        livenessTipsPleaseFailTv.setText("请上传正面人脸照片");
                                        livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
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
    private void checkOpenDebugResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    layoutCompareStatus.setVisibility(View.GONE);
                    rgbTestIv.setVisibility(View.GONE);
                    depthTestIv.setVisibility(View.GONE);
                    rgbDepthTestView.setImageResource(R.mipmap.ic_image_video);
                    tvDepthLiveTime.setText(String.format("相似度分数：%s", 0));
                    tvDepthLiveScore.setText(String.format("活体检测耗时：%s ms", 0));
                    tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", 0));
                    tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms", 0));
                    tvAllTime.setText(String.format("总耗时：%s ms", 0));

                } else {
                    BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
                    if (image != null) {
                        rgbDepthTestView.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                        image.destory();
                    }
                    tvDepthLiveTime.setText(String.format("相似度分数：%s", score));
                    tvDepthLiveScore.setText(String.format("活体检测耗时：%s ms", livenessModel.getDepthtLivenessDuration()));
                    //  比较两个人脸
                    if (firstFeature == null || secondFeature == null) {
                        return;
                    }
                    tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", livenessModel.getFeatureDuration()));
                    tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms",
                            livenessModel.getCheckDuration()));
                    tvAllTime.setText(String.format("总耗时：%s ms", livenessModel.getAllDetectDuration()));
                    if (isDevelopment) {
                        livenessTipsFailRl.setVisibility(View.GONE);
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        rgbLivenessScore = livenessModel.getRgbLivenessScore();
                        if (rgbLivenessScore < rgbLiveScore) {
                            rgbTestIv.setVisibility(View.VISIBLE);
                            rgbTestIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                        } else {
                            rgbTestIv.setVisibility(View.VISIBLE);
                            rgbTestIv.setImageResource(R.mipmap.ic_icon_develop_success);
                        }

                        depthLivenessScore = livenessModel.getDepthLivenessScore();
                        if (depthLivenessScore < depthLiveScore) {
                            depthTestIv.setVisibility(View.VISIBLE);
                            depthTestIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                        } else {
                            depthTestIv.setVisibility(View.VISIBLE);
                            depthTestIv.setImageResource(R.mipmap.ic_icon_develop_success);
                        }
                        if (livenessModel .isQualityCheck()){
                            textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                        textCompareStatus.setMaxEms(6);
                        textCompareStatus.setText("请正视摄像头");
                        } else if (rgbLivenessScore < rgbLiveScore || depthLivenessScore < depthLiveScore) {
                            textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));

//                            textCompareStatus.setMaxEms(7);
                            textCompareStatus.setText("活体检测未通过");
                        } else {
                            if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                                textCompareStatus.setText("比对成功");
                            } else {
                                textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                                textCompareStatus.setText("比对失败");
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

    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == DEPTH_NEED_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 绘制人脸框
     */
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
}
