package com.baidu.idl.main.facesdk.paymentlibrary.activity.payment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.datalibrary.activity.BaseActivity;
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
import com.example.datalibrary.view.PreviewTexture;


public class FaceNIRPaymentActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "face-rgb-ir";
    /*图片越大，性能消耗越大，也可以选择640*480， 1280*720*/
    private static final int PREFER_WIDTH = 640;
    private static final int PERFER_HEIGH = 480;

    private Context mContext;

    // 调试页面控件
    private ImageView mFaceDetectImageView;
    private TextView mTvDetect;
    private TextView mTvLive;
    private TextView mTvLiveScore;

    // 深度数据显示
    private TextView mTvIr;
    private TextView mTvIrScore;

    private TextView mTvFeature;
    private TextView mTvAll;
    private TextView mTvAllTime;


    // RGB+IR 控件
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;

    private TextureView irPreviewView;

    // 摄像头个数
    private int mCameraNum;
    private float rgbLiveScore;
    private float nirLiveScore;

    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private float[] pointXY = new float[4];
    private boolean isCheck = false;
    private boolean isCompareCheck = false;
    private boolean mIsOnClick = false;
    private TextView preText;
    private TextView deveLop;
    private RelativeLayout preViewRelativeLayout;
    private RelativeLayout deveLopRelativeLayout;
    private TextView detectSurfaceText;
    private ImageView isRgbCheckImage;
    private ImageView isNirCheckImage;
    private View preView;
    private View developView;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private Paint paintBg;
    private RelativeLayout progressLayout;
    private TextView preToastText;
    private ImageView progressBarView;
    private TextView nirSurfaceText;
    private RelativeLayout payHintRl;
    private boolean payHint = false;
    private boolean isTime = true;
    private boolean isNeedCamera = true;
    private long searshTime;
    private ImageView isMaskImage;
    private ImageView detectRegImageItem;
    private ImageView isCheckImageView;
    private TextView detectRegTxt;
    private boolean mIsPayHint = true;
    private User mUser;
    private boolean count = true;
    private TextView mNum;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;
    private GlMantleSurfacView glMantleSurfacView;
    private BDFaceImageConfig bdFaceImageConfig;
    private BDFaceImageConfig bdNirFaceImageConfig;
    private BDFaceCheckConfig bdFaceCheckConfig;
    private BDLiveConfig bdLiveConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        FaceSDKManager.getInstance().initDataBases(this);
        setContentView(R.layout.activity_face_nir_paymentlibrary);
        initFaceCheck();
        initView();
    }

    private void initFaceConfig(int height , int width){
        bdFaceImageConfig = new BDFaceImageConfig(height , width ,
                SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectRGB() ,
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_NV21);
    }
    private void initNirFaceConfig(int height , int width){
        bdNirFaceImageConfig = new BDFaceImageConfig(height , width ,
                SingleBaseConfig.getBaseConfig().getNirDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectNIR() ,
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

    /**
     * 开启Debug View
     */
    private void initView() {

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
        detectRegImageItem = findViewById(R.id.detect_reg_image_item);
        isMaskImage = findViewById(R.id.is_mask_image);
        isCheckImageView = findViewById(R.id.is_check_image_view);
        detectRegTxt = findViewById(R.id.detect_reg_txt);


        // ***************开发模式*************
        // 导航栏
        deveLop = findViewById(R.id.develop_text);
        deveLop.setOnClickListener(this);
        deveLop.setTextColor(Color.parseColor("#a9a9a9"));
        developView = findViewById(R.id.develop_view);
        developView.setVisibility(View.GONE);
        // 信息展示
        deveLopRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        detectSurfaceText = findViewById(R.id.detect_surface_text);
        detectSurfaceText.setVisibility(View.GONE);
        nirSurfaceText = findViewById(R.id.nir_surface_text);
        nirSurfaceText.setVisibility(View.GONE);

        isRgbCheckImage = findViewById(R.id.is_check_image);
        isNirCheckImage = findViewById(R.id.nir_is_check_image);
        // RGB 阈值
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // Live 阈值
        nirLiveScore = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        // 送检RGB 图像回显
        mFaceDetectImageView = findViewById(R.id.face_detect_image_view);
        mFaceDetectImageView.setVisibility(View.GONE);
        // 双目摄像头IR 图像预览
        irPreviewView = findViewById(R.id.ir_camera_preview_view);
        if (SingleBaseConfig.getBaseConfig().getMirrorVideoNIR() == 1) {
            irPreviewView.setRotationY(180);
        }
        // Ir活体
        mTvIr = findViewById(R.id.tv_nir_live_time);
        mTvIrScore = findViewById(R.id.tv_nir_live_score);
        // 存在底库的数量
        mNum = findViewById(R.id.tv_num);
        mNum.setText(String.format("底库 ： %s 个样本", FaceApi.getInstance().getmUserNum()));
        // 检测耗时
        mTvDetect = findViewById(R.id.tv_detect_time);
        // RGB活体
        mTvLive = findViewById(R.id.tv_rgb_live_time);
        mTvLiveScore = findViewById(R.id.tv_rgb_live_score);
        // 特征提取
        mTvFeature = findViewById(R.id.tv_feature_time);
        // 检索
        mTvAll = findViewById(R.id.tv_feature_search_time);
        // 总耗时
        mTvAllTime = findViewById(R.id.tv_all_time);
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        layoutCompareStatus.setVisibility(View.GONE);
        textCompareStatus = findViewById(R.id.text_compare_status);
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        saveCamera.setVisibility(View.GONE);
        spot = findViewById(R.id.spot);


        // 双摄像头
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {

            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
            mPreview[1] = new PreviewTexture(this, irPreviewView);
        }
        glMantleSurfacView = findViewById(R.id.camera_textureview);
        glMantleSurfacView.setDraw(true);
        glMantleSurfacView.initSurface(SingleBaseConfig.getBaseConfig().getRgbRevert(),
                SingleBaseConfig.getBaseConfig().getMirrorVideoRGB() , SingleBaseConfig.getBaseConfig().isOpenGl());
        CameraPreviewManager.getInstance().startPreview(glMantleSurfacView,
                SingleBaseConfig.getBaseConfig().getRgbVideoDirection() , PREFER_WIDTH, PERFER_HEIGH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            try {
                startTestCloseDebugRegisterFunction();
                if (SingleBaseConfig.getBaseConfig().getRBGCameraId() != -1) {
                    mCamera[1] = Camera.open(Math.abs(SingleBaseConfig.getBaseConfig().getRBGCameraId() - 1));
                }else {
                    mCamera[1] = Camera.open(1);
                }
                ViewGroup.LayoutParams layoutParams = irPreviewView.getLayoutParams();
                int w = layoutParams.width;
                int h = layoutParams.height;
                int cameraRotation = SingleBaseConfig.getBaseConfig().getNirVideoDirection();
                mCamera[1].setDisplayOrientation(cameraRotation);
                if (cameraRotation == 90 || cameraRotation == 270) {
                    layoutParams.height = w;
                    layoutParams.width = h;
                    // 旋转90度或者270，需要调整宽高
                } else {
                    layoutParams.height = h;
                    layoutParams.width = w;
                }
                irPreviewView.setLayoutParams(layoutParams);
                mPreview[1].setCamera(mCamera[1], PREFER_WIDTH, PERFER_HEIGH);
                initNirFaceConfig(PERFER_HEIGH , PREFER_WIDTH);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealIr(data);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void startTestCloseDebugRegisterFunction() {
        // TODO ： 临时放置
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        //  CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
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
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // 摄像头预览数据进行人脸检测
                        dealRgb(data);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        CameraPreviewManager.getInstance().stopPreview();

        if (mCameraNum >= 2) {
            for (int i = 0; i < mCameraNum; i++) {
                if (mCameraNum >= 2) {
                    if (mCamera[i] != null) {
                        mCamera[i].setPreviewCallback(null);
                        mCamera[i].stopPreview();
                        mPreview[i].release();
                        mCamera[i].release();
                        mCamera[i] = null;
                    }
                }
            }
        }

        super.onPause();
    }

    private void dealRgb(byte[] data) {
        glMantleSurfacView.setFrame();
        bdFaceImageConfig.setData(data);
        checkData();
    }

    private void dealIr(byte[] data) {
        bdNirFaceImageConfig.setData(data);
        checkData();
    }

    private synchronized void checkData() {
            if (bdFaceImageConfig.data != null && bdNirFaceImageConfig.data != null) {
            FaceSDKManager.getInstance().onDetectCheck(bdFaceImageConfig, bdNirFaceImageConfig, null,
                    bdFaceCheckConfig, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // 输出结果
                            if (glMantleSurfacView.isDraw()) {
                                // 预览模式
                                checkCloseDebugResult(livenessModel);
                            } else {
                                // 开发模式
                                checkOpenDebugResult(livenessModel);
                            }
                            if (isSaveImage){
                                SaveImageManager.getInstance().saveImage(livenessModel , bdLiveConfig);

                            }
                        }

                        @Override
                        public void onTip(int code, String msg) {
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                            // 绘制人脸框
//                            if (!glSurfaceView.isDraw) {
                                showFrame(livenessModel);
//                            }

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
                    isNirCheckImage.setVisibility(View.GONE);
                    isRgbCheckImage.setVisibility(View.GONE);
                    mFaceDetectImageView.setImageResource(R.mipmap.ic_image_video);
                    mTvDetect.setText(String.format("检测耗时：%s ms", 0));
                    mTvLive.setText(String.format("RGB活体检测耗时 ：%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB活体得分 ：%s", 0));
                    mTvIr.setText(String.format("NIR活体检测耗时 ：%s ms", 0));
                    mTvIrScore.setText(String.format("NIR活体得分 ：%s", 0));
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
                float nirLivenessScore = livenessModel.getIrLivenessScore();
                if (nirLivenessScore < nirLiveScore) {
                    if (isCheck) {
                        isNirCheckImage.setVisibility(View.VISIBLE);
                        isNirCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                    }
                } else {
                    if (isCheck) {
                        isNirCheckImage.setVisibility(View.VISIBLE);
                        isNirCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                    }

                }
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

                if (livenessModel .isQualityCheck()){
                    mUser = null;
                    if (isCompareCheck) {
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                                                textCompareStatus.setMaxEms(6);
                        textCompareStatus.setText("请正视摄像头");
                    }
                } else if (rgbLivenessScore < rgbLiveScore || nirLivenessScore < nirLiveScore) {
                    mUser = null;
                    if (isCompareCheck) {
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));

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
                mTvIr.setText(String.format("NIR活体检测耗时 ：%s ms", livenessModel.getIrLivenessDuration()));
                mTvIrScore.setText(String.format("NIR活体得分 ：%s", livenessModel.getIrLivenessScore()));
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
                finish();
            }
            // 设置
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(mContext, PaymentSettingActivity.class));
            finish();
        } else if (id == R.id.preview_text) {
            if (payHintRl.getVisibility() == View.VISIBLE) {
                return;
            }
            isCheck = false;
            isCompareCheck = false;
            glMantleSurfacView.setDraw(true);
            mIsPayHint = true;
            count = true;
            irPreviewView.setAlpha(0);
            isRgbCheckImage.setVisibility(View.GONE);
            isNirCheckImage.setVisibility(View.GONE);
            mFaceDetectImageView.setVisibility(View.GONE);
            detectSurfaceText.setVisibility(View.GONE);
            layoutCompareStatus.setVisibility(View.GONE);
            nirSurfaceText.setVisibility(View.GONE);
            developView.setVisibility(View.GONE);
            deveLopRelativeLayout.setVisibility(View.GONE);

            progressLayout.setVisibility(View.VISIBLE);
            preToastText.setVisibility(View.VISIBLE);
            deveLop.setTextColor(Color.parseColor("#a9a9a9"));
            preText.setTextColor(Color.parseColor("#ffffff"));
            preView.setVisibility(View.VISIBLE);
            preViewRelativeLayout.setVisibility(View.VISIBLE);
            progressBarView.setVisibility(View.VISIBLE);
            payHintRl.setVisibility(View.GONE);
            saveCamera.setVisibility(View.GONE);
            isSaveImage = false;
            spot.setVisibility(View.GONE);
            isPause = false;
            glMantleSurfacView.onGlDraw(null ,
                    null ,
                    null);
        } else if (id == R.id.develop_text) {
            isNeedCamera = true;
            mIsOnClick = false;
            mIsPayHint = false;
            isCheck = true;
            isCompareCheck = true;
            glMantleSurfacView.setDraw(false);
            count = false;
            irPreviewView.setAlpha(1);
            isRgbCheckImage.setVisibility(View.VISIBLE);
            isNirCheckImage.setVisibility(View.VISIBLE);
            mFaceDetectImageView.setVisibility(View.VISIBLE);
            detectSurfaceText.setVisibility(View.VISIBLE);
            nirSurfaceText.setVisibility(View.VISIBLE);
            developView.setVisibility(View.VISIBLE);
            deveLopRelativeLayout.setVisibility(View.VISIBLE);

            isPause = true;
            deveLop.setTextColor(Color.parseColor("#ffffff"));
            preText.setTextColor(Color.parseColor("#a9a9a9"));
            preView.setVisibility(View.GONE);
            preViewRelativeLayout.setVisibility(View.GONE);
            preToastText.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
            progressBarView.setVisibility(View.GONE);
            payHintRl.setVisibility(View.GONE);
            saveCamera.setVisibility(View.VISIBLE);
            judgeFirst();
        } else if (id == R.id.save_camera){
            isSaveImage = !isSaveImage;
            if (isSaveImage){
                spot.setVisibility(View.VISIBLE);
                ToastUtils.toast(FaceNIRPaymentActivity.this, "存图功能已开启再次点击可关闭");
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

    private boolean isPause = false;
    public void showFrame(LivenessModel livenessModel){
        if (livenessModel == null){
            return;
        }

        if (isPause){
            glMantleSurfacView.onGlDraw(livenessModel.getTrackFaceInfo() ,
                    livenessModel.getBdFaceImageInstance() ,
                    FaceOnDrawTexturViewUtil.drawFaceColor(mUser , livenessModel) );
        }
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
