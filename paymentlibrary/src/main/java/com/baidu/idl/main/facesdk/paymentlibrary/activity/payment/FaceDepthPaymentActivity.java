package com.baidu.idl.main.facesdk.paymentlibrary.activity.payment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.paymentlibrary.R;
import com.baidu.idl.main.facesdk.paymentlibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.paymentlibrary.setting.PaymentSettingActivity;
import com.baidu.idl.main.facesdk.paymentlibrary.utils.FaceUtils;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.example.datalibrary.activity.BaseOrbbecActivity;
import com.example.datalibrary.api.FaceApi;
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
import com.example.datalibrary.model.User;
import com.example.datalibrary.utils.BitmapUtils;
import com.example.datalibrary.utils.FaceOnDrawTexturViewUtil;
import com.example.datalibrary.utils.FileUtils;
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class FaceDepthPaymentActivity extends BaseOrbbecActivity implements OpenNIHelper.DeviceOpenListener,
        ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener {
    private static final int DEPTH_NEED_PERMISSION = 33;

    /*RGB摄像头图像宽和高*/
    private static final int RGB_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int RGB_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();

    // Depth摄像头图像宽和高
    private int depthWidth = SingleBaseConfig.getBaseConfig().getDepthWidth();
    private int depthHeight = SingleBaseConfig.getBaseConfig().getDepthHeight();

    private Context mContext;

    // 调试页面控件
    private ImageView mFaceDetectImageView;
    private TextView mTvDetect;
    private TextView mTvLive;
    private TextView mTvLiveScore;

    // 深度数据显示
    private TextView mTvDepth;
    private TextView mTvDepthScore;

    private TextView mTvFeature;
    private TextView mTvAll;
    private TextView mTvAllTime;
    private TextView mNum;

    // 显示Depth图
    private OpenNIView mDepthGLView;

    // 设备初始化状态标记
    private boolean initOk = false;
    // 摄像头驱动
    private Device mDevice;
    private Thread thread;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream mDepthStream;

    private Object sync = new Object();
    // 循环取深度图像数据
    private boolean exit = false;

    /*当前摄像头类型*/
    private static int cameraType;

    // 人脸框绘制
    private RectF rectF;
    private Paint paint;
    private Paint paintBg;

    private RelativeLayout relativeLayout;
    private float rgbLiveScore;
    private float depthLiveScore;

    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private float[] pointXY = new float[4];
    private boolean isCheck = false;
    private boolean isCompareCheck = false;
    private boolean mIsOnClick = false;
    private TextView preText;
    private TextView deveLop;
    private RelativeLayout preViewRelativeLayout;
    private RelativeLayout deveLopRelativeLayout;
    private ImageView isRgbCheckImage;
    private ImageView isDepthCheckImage;
    private View preView;
    private View developView;
    private TextView detectSurfaceText;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private TextView depthSurfaceText;
    private TextView preToastText;
    private RelativeLayout progressLayout;
    private ImageView progressBarView;

    private RelativeLayout payHintRl;
    private boolean payHint = false;
    private boolean isTime = true;
    private long searshTime;
    private boolean isNeedCamera = true;
    private ImageView isMaskImage;
    private RelativeLayout detectRegLayout;
    private ImageView detectRegImageItem;
    private ImageView isCheckImageView;
    private TextView detectRegTxt;
    private boolean mIsPayHint = true;
    private User mUser;
    private boolean count = true;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;
    private GlMantleSurfacView glMantleSurfacView;
    private BDFaceImageConfig bdFaceImageConfig;
    private BDFaceImageConfig bdDepthFaceImageConfig;
    private BDFaceCheckConfig bdFaceCheckConfig;
    private BDLiveConfig bdLiveConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        FaceSDKManager.getInstance().initDataBases(this);
        setContentView(R.layout.activity_face_rgb_depth_payment);
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

    /**
     * 开启Debug View
     */
    private void initView() {

        depthWidth = SingleBaseConfig.getBaseConfig().getDepthWidth();
        depthHeight = SingleBaseConfig.getBaseConfig().getDepthHeight();
        // 获取整个布局
        relativeLayout = findViewById(R.id.all_relative);
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();

        // 返回
        ImageView mButReturn = findViewById(R.id.btn_back);
        mButReturn.setOnClickListener(this);
        // 设置
        ImageView mBtSetting = findViewById(R.id.btn_setting);
        mBtSetting.setOnClickListener(this);
        // ***************预览模式*************
        // 导航栏
        preText = findViewById(R.id.preview_text);
        preText.setOnClickListener(this);
        preText.setTextColor(Color.parseColor("#ffffff"));
        preView = findViewById(R.id.preview_view);
        // 信息展示
        preViewRelativeLayout = findViewById(R.id.yvlan_relativeLayout);
        preToastText = findViewById(R.id.pre_toast_text);
        progressLayout = findViewById(R.id.progress_layout);
        progressBarView = findViewById(R.id.progress_bar_view);
        // 预览模式下提示
        payHintRl = findViewById(R.id.pay_hintRl);
        detectRegLayout = findViewById(R.id.detect_reg_layout);
        detectRegImageItem = findViewById(R.id.detect_reg_image_item);
        isMaskImage = findViewById(R.id.is_mask_image);
        isCheckImageView = findViewById(R.id.is_check_image_view);
        detectRegTxt = findViewById(R.id.detect_reg_txt);

        // 开发模式
        deveLop = findViewById(R.id.develop_text);
        deveLop.setOnClickListener(this);
        deveLopRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        developView = findViewById(R.id.develop_view);
        developView.setVisibility(View.GONE);
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        layoutCompareStatus.setVisibility(View.GONE);
        textCompareStatus = findViewById(R.id.text_compare_status);

        // ***************开发模式*************
        detectSurfaceText = findViewById(R.id.detect_surface_text);
        detectSurfaceText.setVisibility(View.GONE);
        depthSurfaceText = findViewById(R.id.depth_surface_text);
        depthSurfaceText.setVisibility(View.GONE);
        isRgbCheckImage = findViewById(R.id.is_check_image);
        isDepthCheckImage = findViewById(R.id.depth_is_check_image);
        // RGB 阈值
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // Live 阈值
        depthLiveScore = SingleBaseConfig.getBaseConfig().getDepthLiveScore();

        // 送检RGB 图像回显
        mFaceDetectImageView = findViewById(R.id.face_detect_image_view);
        mFaceDetectImageView.setVisibility(View.INVISIBLE);
        // 深度摄像头数据回显
        mDepthGLView = findViewById(R.id.depth_camera_preview_view);
        mDepthGLView.setVisibility(View.INVISIBLE);

        // 存在底库的数量
        mNum = findViewById(R.id.tv_num);
        mNum.setText(String.format("底库 ： %s 个样本", FaceApi.getInstance().getmUserNum()));
        // 检测耗时
        mTvDetect = findViewById(R.id.tv_detect_time);
        // RGB活体
        mTvLive = findViewById(R.id.tv_rgb_live_time);
        mTvLiveScore = findViewById(R.id.tv_rgb_live_score);
        // depth活体
        mTvDepth = findViewById(R.id.tv_depth_live_time);
        mTvDepthScore = findViewById(R.id.tv_depth_live_score);
        // 特征提取
        mTvFeature = findViewById(R.id.tv_feature_time);
        // 检索
        mTvAll = findViewById(R.id.tv_feature_search_time);
        // 总耗时
        mTvAllTime = findViewById(R.id.tv_all_time);
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        saveCamera.setVisibility(View.GONE);
        spot = findViewById(R.id.spot);
        glMantleSurfacView = findViewById(R.id.camera_textureview);
        glMantleSurfacView.setDraw(true);
        glMantleSurfacView.initSurface(SingleBaseConfig.getBaseConfig().getRgbRevert(),
                SingleBaseConfig.getBaseConfig().getMirrorVideoRGB() , SingleBaseConfig.getBaseConfig().isOpenGl());
        CameraPreviewManager.getInstance().startPreview(glMantleSurfacView,
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

    /**
     * 摄像头图像预览
     */
    private void startCameraPreview() {
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
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
    }

    @Override
    public void onDeviceOpened(UsbDevice usbDevice) {
        initUsbDevice(usbDevice);
        mDepthStream = VideoStream.create(this.mDevice, SensorType.DEPTH);
        if (mDepthStream != null) {
            List<VideoMode> mVideoModes = mDepthStream.getSensorInfo().getSupportedVideoModes();
            for (VideoMode mode : mVideoModes) {
                int x = mode.getResolutionX();
                int y = mode.getResolutionY();
                int fps = mode.getFps();
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

    private void dealDepth(byte[] data) {
        bdDepthFaceImageConfig.setData(data);
        checkData();
    }

    private void dealRgb(byte[] data) {
        bdFaceImageConfig.setData(data);
        glMantleSurfacView.setFrame();
        checkData();
    }

    private synchronized void checkData() {
        if (bdFaceImageConfig.data != null && bdDepthFaceImageConfig.data != null){
            FaceSDKManager.getInstance().onDetectCheck(bdFaceImageConfig, null, bdDepthFaceImageConfig,
                    bdFaceCheckConfig, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // 输出结果
                            checkCloseDebugResult(livenessModel);
                            // 开发模式
                            checkOpenDebugResult(livenessModel);
                            if (isSaveImage) {
                                SaveImageManager.getInstance().saveImage(livenessModel , bdLiveConfig);
                            }
                        }

                        @Override
                        public void onTip(int code, String msg) {
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            showFrame(livenessModel);
                        }
                    });
        }
    }

    // ***************预览模式结果输出*************
    private void checkCloseDebugResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null || livenessModel.getFaceInfo() == null) {
                    if (isTime) {
                        isTime = false;
                        searshTime = System.currentTimeMillis();
                    }
                    long endSearchTime = System.currentTimeMillis() - searshTime;
                    if (endSearchTime < 5000) {
                        preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                        preToastText.setText("请保持面部在取景框内");
                        progressBarView.setImageResource(R.mipmap.ic_loading_grey);
                    } else {
                        payHint(null);
                    }
                    return;
                }

                isTime = true;
                pointXY[0] = livenessModel.getFaceInfo().centerX;
                pointXY[1] = livenessModel.getFaceInfo().centerY;
                pointXY[2] = livenessModel.getFaceInfo().width;
                pointXY[3] = livenessModel.getFaceInfo().width;
                FaceOnDrawTexturViewUtil.converttPointXY(pointXY, glMantleSurfacView,
                        livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);
                float leftLimitX = glMantleSurfacView.circleX - glMantleSurfacView.circleRadius;
                float rightLimitX = glMantleSurfacView.circleX + glMantleSurfacView.circleRadius;
                float topLimitY = glMantleSurfacView.circleY - glMantleSurfacView.circleRadius;
                float bottomLimitY = glMantleSurfacView.circleY + glMantleSurfacView.circleRadius;
                if (pointXY[0] - pointXY[2] / 2 < leftLimitX
                        || pointXY[0] + pointXY[2] / 2 > rightLimitX
                        || pointXY[1] - pointXY[3] / 2 < topLimitY
                        || pointXY[1] + pointXY[3] / 2 > bottomLimitY) {
                    preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                    preToastText.setText("请保持面部在取景框内");
                    progressBarView.setImageResource(R.mipmap.ic_loading_grey);
                    return;
                }
                preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                preToastText.setText("正在识别中...");
                progressBarView.setImageResource(R.mipmap.ic_loading_blue);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //   todo somthing here
                        if (count) {
                            count = false;
                            payHint(livenessModel);
                        }
                    }
                }, 2 * 500);  // 延迟1秒执行

            }
        });
    }

    // ***************开发模式结果输出*************
    private void checkOpenDebugResult(final LivenessModel livenessModel) {

        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    layoutCompareStatus.setVisibility(View.GONE);
                    isRgbCheckImage.setVisibility(View.GONE);
                    isDepthCheckImage.setVisibility(View.GONE);
                    mFaceDetectImageView.setImageResource(R.mipmap.ic_image_video);
                    mTvDetect.setText(String.format("检测耗时 ：%s ms", 0));
                    mTvLive.setText(String.format("RGB活体检测耗时 ：%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB活体得分 ：%s", 0));
                    mTvDepth.setText(String.format("Depth活体检测耗时 ：%s ms", 0));
                    mTvDepthScore.setText(String.format("Depth活体得分 ：%s", 0));
                    mTvFeature.setText(String.format("特征抽取耗时 ：%s ms", 0));
                    mTvAll.setText(String.format("特征比对耗时 ：%s ms", 0));
                    mTvAllTime.setText(String.format("总耗时 ：%s ms", 0));
                    return;
                }

                BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
                if (image != null) {
                    mFaceDetectImageView.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                    image.destory();
                }

                float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                if (rgbLivenessScore < rgbLiveScore) {
                    if (isCheck) {
                        isRgbCheckImage.setVisibility(View.VISIBLE);
                        isRgbCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                    }
                } else {
                    if (isCheck) {
                        isRgbCheckImage.setVisibility(View.VISIBLE);
                        isRgbCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                    }
                }

                float depthLivenessScore = livenessModel.getDepthLivenessScore();
                if (depthLivenessScore < depthLiveScore) {
                    if (isCheck) {
                        isDepthCheckImage.setVisibility(View.VISIBLE);
                        isDepthCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                    }
                } else {
                    if (isCheck) {
                        isDepthCheckImage.setVisibility(View.VISIBLE);
                        isDepthCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                    }
                }

                if (livenessModel .isQualityCheck()){
                    mUser = null;
                    if (isCompareCheck) {
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                                                textCompareStatus.setMaxEms(6);
                        textCompareStatus.setText("请正视摄像头");
                    }
                } else if (rgbLivenessScore < rgbLiveScore || depthLivenessScore < depthLiveScore) {
                    mUser = null;
                    if (isCompareCheck) {
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//
//                            textCompareStatus.setMaxEms(7);
                            textCompareStatus.setText("活体检测未通过");
                    }
                } else {

                    User user = livenessModel.getUser();
                    if (user == null) {
                        mUser = null;
                        if (isCompareCheck) {
                            if (livenessModel.isMultiFrame()) {
                                layoutCompareStatus.setVisibility(View.VISIBLE);
                                textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
                                textCompareStatus.setText("识别未通过");
                            } else {
                                layoutCompareStatus.setVisibility(View.GONE);
                            }
                        }

                    } else {
                        mUser = user;
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#FF00BAF2"));

//                            textCompareStatus.setMaxEms(5);
                            textCompareStatus.setText(FileUtils.spotString(mUser.getUserName()));
                        }
                    }
                }

                mTvDetect.setText(String.format("检测耗时 ：%s ms", livenessModel.getRgbDetectDuration()));
                mTvLive.setText(String.format("RGB活体检测耗时 ：%s ms", livenessModel.getRgbLivenessDuration()));
                mTvLiveScore.setText(String.format("RGB活体得分 ：%s", livenessModel.getRgbLivenessScore()));
                mTvDepth.setText(String.format("Depth活体检测耗时 ：%s ms", livenessModel.getDepthtLivenessDuration()));
                mTvDepthScore.setText(String.format("Depth活体得分 ：%s", livenessModel.getDepthLivenessScore()));
                mTvFeature.setText(String.format("特征抽取耗时 ：%s ms", livenessModel.getFeatureDuration()));
                mTvAll.setText(String.format("特征比对耗时 ：%s ms", livenessModel.getCheckDuration()));
                mTvAllTime.setText(String.format("总耗时 ：%s ms", livenessModel.getAllDetectDuration()));
            }
        });
    }


    @Override
    public void onClick(View v) {
        // 返回
        int id = v.getId();
        if (id == R.id.btn_back) {
            if (mIsOnClick) {
                progressLayout.setVisibility(View.VISIBLE);
                payHintRl.setVisibility(View.GONE);
                preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                preToastText.setText("请保持面部在取景框内");
                progressBarView.setImageResource(R.mipmap.ic_loading_grey);
                isNeedCamera = true;
                count = true;
                mIsOnClick = false;
            } else {
                if (!FaceSDKManager.initModelSuccess) {
                    Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (thread != null) {
                    thread.interrupt();
                }
                finish();
            }
            // 设置
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (thread != null) {
                thread.interrupt();
            }
            startActivity(new Intent(mContext, PaymentSettingActivity.class));
            finish();
        } else if (id == R.id.preview_text) {
            if (payHintRl.getVisibility() == View.VISIBLE) {
                return;
            }
            isRgbCheckImage.setVisibility(View.GONE);
            isDepthCheckImage.setVisibility(View.GONE);
            mFaceDetectImageView.setVisibility(View.GONE);
            detectSurfaceText.setVisibility(View.GONE);
            layoutCompareStatus.setVisibility(View.GONE);
            mDepthGLView.setVisibility(View.GONE);
            depthSurfaceText.setVisibility(View.GONE);
            deveLop.setTextColor(Color.parseColor("#a9a9a9"));
            preText.setTextColor(Color.parseColor("#ffffff"));
            preView.setVisibility(View.VISIBLE);
            developView.setVisibility(View.GONE);
            preViewRelativeLayout.setVisibility(View.VISIBLE);
            deveLopRelativeLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            preToastText.setVisibility(View.VISIBLE);
            progressBarView.setVisibility(View.VISIBLE);
            payHintRl.setVisibility(View.GONE);
            isCheck = false;
            isCompareCheck = false;
            glMantleSurfacView.setDraw(true);
            mIsPayHint = true;
            count = true;
            saveCamera.setVisibility(View.GONE);
            isSaveImage = false;
            isPause = false;
            glMantleSurfacView.onGlDraw(null ,
                    null ,
                    null);
            spot.setVisibility(View.GONE);
        } else if (id == R.id.develop_text) {
            mIsPayHint = false;
            isNeedCamera = true;
            mIsOnClick = false;
            isCheck = true;
            isCompareCheck = true;
            glMantleSurfacView.setDraw(false);
            count = false;
            isRgbCheckImage.setVisibility(View.VISIBLE);
            isDepthCheckImage.setVisibility(View.VISIBLE);
            mFaceDetectImageView.setVisibility(View.VISIBLE);
            detectSurfaceText.setVisibility(View.VISIBLE);
            developView.setVisibility(View.VISIBLE);
            deveLopRelativeLayout.setVisibility(View.VISIBLE);
            mDepthGLView.setVisibility(View.VISIBLE);
            depthSurfaceText.setVisibility(View.VISIBLE);
            deveLop.setTextColor(Color.parseColor("#ffffff"));
            preText.setTextColor(Color.parseColor("#a9a9a9"));
            preView.setVisibility(View.GONE);
            preViewRelativeLayout.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
            preToastText.setVisibility(View.GONE);
            progressBarView.setVisibility(View.GONE);
            payHintRl.setVisibility(View.GONE);
            saveCamera.setVisibility(View.VISIBLE);
            isPause = true;
            judgeFirst();
        } else if (id == R.id.save_camera){
            isSaveImage = !isSaveImage;
            if (isSaveImage){
                spot.setVisibility(View.VISIBLE);
                ToastUtils.toast(FaceDepthPaymentActivity.this, "存图功能已开启再次点击可关闭");
            }else {
                spot.setVisibility(View.GONE);
            }
        }
    }

    private void judgeFirst(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isPaymentFirstSave", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            setFirstView(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFirstView(View.GONE);
                }
            },3000);
            editor.putBoolean("isPaymentFirstSave", false);
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
        mOpenNIHelper = new OpenNIHelper(this);
        mOpenNIHelper.requestDeviceOpen(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    @Override
    public void onDeviceOpenFailed(String msg) {
        showAlertAndExit("Open Device failed: " + msg);
    }

    @Override
    public void onDeviceNotFound() {

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
    private void showFrame(final LivenessModel model) {
        if (model == null) {
            return;
        }
        if (isPause){
            glMantleSurfacView.onGlDraw(model.getTrackFaceInfo() ,
                    model.getBdFaceImageInstance() ,
                    FaceOnDrawTexturViewUtil.drawFaceColor(mUser , model));
        }
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

    private void payHint(final LivenessModel livenessModel) {
        if (livenessModel == null && mIsPayHint) {
            isMaskImage.setImageResource(R.mipmap.ic_mask_fail);
            isCheckImageView.setImageResource(R.mipmap.ic_icon_fail_sweat);
            detectRegTxt.setTextColor(Color.parseColor("#FECD33"));
            detectRegTxt.setText("识别超时");
            progressLayout.setVisibility(View.GONE);
            payHintRl.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //   todo somthing here
                    progressLayout.setVisibility(View.VISIBLE);
                    payHintRl.setVisibility(View.GONE);
                    isTime = true;
                }
            }, 3 * 1000);  // 延迟3秒执行
            return;
        }
        if (mIsPayHint && livenessModel.getUser() == null) {
            // todo: 失败展示
            BDFaceImageInstance bdFaceImageInstance = livenessModel.getBdFaceImageInstance();
            Bitmap instaceBmp = BitmapUtils.getInstaceBmp(bdFaceImageInstance);
            isMaskImage.setImageResource(R.mipmap.ic_mask_fail);
            isCheckImageView.setImageResource(R.mipmap.ic_icon_fail_sweat);
            detectRegImageItem.setImageBitmap(instaceBmp);
            detectRegTxt.setTextColor(Color.parseColor("#FECD33"));
            detectRegTxt.setText("抱歉未能认出您");
            progressLayout.setVisibility(View.GONE);
            payHintRl.setVisibility(View.VISIBLE);
            isNeedCamera = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //   todo somthing here
                    progressLayout.setVisibility(View.VISIBLE);
                    payHintRl.setVisibility(View.GONE);
                    payHint = false;
                    isNeedCamera = true;
                    count = true;
                }
            }, 3 * 1000);  // 延迟3秒执行

        }
        if (mIsPayHint && livenessModel.getUser() != null) {
            // todo: 成功展示kk
            payHintRl.setVisibility(View.VISIBLE);
            String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                    + "/" + livenessModel.getUser().getImageName();
            Bitmap userBitmap = BitmapFactory.decodeFile(absolutePath);
            detectRegImageItem.setImageBitmap(userBitmap);
            isMaskImage.setImageResource(R.mipmap.ic_mask_success);
            isCheckImageView.setImageResource(R.mipmap.ic_icon_success_star);
            detectRegTxt.setTextColor(Color.parseColor("#00BAF2"));
            detectRegTxt.setText(FileUtils.spotString(livenessModel.getUser().getUserName()) + " 识别成功");
            isNeedCamera = false;
            mIsOnClick = true;
        }

    }

}
