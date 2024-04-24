package com.baidu.idl.main.facesdk.identifylibrary.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.datalibrary.view.PreviewTexture;

import java.io.FileNotFoundException;

import static android.content.ContentValues.TAG;

public class FaceIRTestimonyActivity extends BaseActivity implements View.OnClickListener {
    private static final int PICK_PHOTO_FRIST = 100;

    private byte[] firstFeature = new byte[512];
    private byte[] secondFeature = new byte[512];

    private Context mContext;
    // 摄像头个数
    private int mCameraNum;
    // RGB+IR 控件
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;
    private ImageView testImageview;
    private ImageView testimonyPreviewLineIv;
    private ImageView testimonyDevelopmentLineIv;
    /*图片越大，性能消耗越大，也可以选择640*480， 1280*720*/
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    // 判断摄像头数据源
    private int camemra1DataMean;
    private int camemra2DataMean;
    private volatile boolean camemra1IsRgb = false;
    // 摄像头采集数据
    private RelativeLayout livenessAgainRl;
    private ImageView livenessAddIv;
    private TextView livenessUpdateTv;
    private ImageView livenessShowIv;
    private ImageView hintShowIv;
    private TextView tvNirLiveScore;
    private RelativeLayout livenessTipsFailRl;
    private TextView livenessTipsFailTv;
    private TextView livenessTipsPleaseFailTv;
    private TextView tvNirLiveTime;
    private TextureView irTexture;
    private float score = 0;
    private TextView testimonyDevelopmentTv;
    private TextView testimonyPreviewTv;

    // 定义一个变量判断是预览模式还是开发模式
    boolean isDevelopment = false;
    private RelativeLayout livenessButtomLl;
    private RelativeLayout kaifaRelativeLayout;
    private RelativeLayout testNirRl;
    private TextView hintAdainTv;
    private TextView livenessBaiduTv;
    private View view;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private ImageView testNirIv;
    private ImageView testRgbIv;
    private TextView tvFeatureTime;
    private TextView tvFeatureSearchTime;
    private TextView tvAllTime;
    private RelativeLayout hintShowRl;
    private RelativeLayout developmentAddRl;
    private float rgbLiveScore;
    private float nirLiveScore;
    // 判断是否有人脸
    private boolean isFace = false;
    private ImageView livenessTipsFailIv;
    private float nirLivenessScore = 0.0f;
    private float rgbLivenessScore = 0.0f;
    // rgb
    private RelativeLayout testRelativeLayout;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;
    private GlMantleSurfacView glSurfaceView;
    private BDFaceImageConfig bdFaceImageConfig;
    private BDFaceImageConfig bdNirFaceImageConfig;
    private BDFaceCheckConfig bdFaceCheckConfig;
    private BDLiveConfig bdLiveConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        setContentView(R.layout.activity_face_ir_identifylibrary);
        FaceSDKManager.getInstance().emptyFrame();
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
                    ToastUtils.toast(FaceIRTestimonyActivity.this, "模型加载成功，欢迎使用");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    FaceSDKManager.initModelSuccess = false;
                    if (errorCode != -12) {
                        ToastUtils.toast(FaceIRTestimonyActivity.this, "模型加载失败，请尝试重启应用");
                    }
                }
            });
        }
    }

    private void initView() {
        // 双目摄像头IR 图像预览
        irTexture = findViewById(R.id.texture_preview_ir);
        if (SingleBaseConfig.getBaseConfig().getMirrorVideoNIR() == 1) {
            irTexture.setRotationY(180);
        }
        // 百度
        livenessBaiduTv = findViewById(R.id.liveness_baiduTv);
        // view
        view = findViewById(R.id.mongolia_view);
        // RGB 阈值
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // Live 阈值
        nirLiveScore = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        /* title */
        // 返回
        ImageView testimonyBackIv = findViewById(R.id.btn_back);
        testimonyBackIv.setOnClickListener(this);
        // 预览模式
        testimonyPreviewTv = findViewById(R.id.preview_text);
        testimonyPreviewTv.setOnClickListener(this);
        testimonyPreviewLineIv = findViewById(R.id.preview_view);
        // 开发模式
        testimonyDevelopmentTv = findViewById(R.id.develop_text);
        testimonyDevelopmentTv.setOnClickListener(this);
        testimonyDevelopmentLineIv = findViewById(R.id.develop_view);
        // 设置
        ImageView testimonySettingIv = findViewById(R.id.btn_setting);
        testimonySettingIv.setOnClickListener(this);

        // ****************开发模式****************
        // RGB
        testImageview = findViewById(R.id.test_rgb_ir_view);
        testRgbIv = findViewById(R.id.test_rgb_iv);
        testRelativeLayout = findViewById(R.id.test_rgb_rl);
        testRelativeLayout.setVisibility(View.GONE);
        // 图片显示
        hintShowIv = findViewById(R.id.hint_showIv);
        // 重新上传
        hintAdainTv = findViewById(R.id.hint_adainTv);
        hintAdainTv.setOnClickListener(this);
        hintShowRl = findViewById(R.id.hint_showRl);
        // 上传图片
        ImageView developmentAddIv = findViewById(R.id.Development_addIv);
        developmentAddIv.setOnClickListener(this);
        developmentAddRl = findViewById(R.id.Development_addRl);
        // nir
        testNirRl = findViewById(R.id.test_nir_Rl);
        testNirRl.setVisibility(View.GONE);
        testNirIv = findViewById(R.id.test_nir_iv);
        // 提示
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        textCompareStatus = findViewById(R.id.text_compare_status);
        // 相似度分数
        tvNirLiveTime = findViewById(R.id.tv_rgb_live_time);
        // 活体检测耗时
        tvNirLiveScore = findViewById(R.id.tv_rgb_live_score);
        // 特征抽取耗时
        tvFeatureTime = findViewById(R.id.tv_feature_time);
        // 特征比对耗时
        tvFeatureSearchTime = findViewById(R.id.tv_feature_search_time);
        // 总耗时
        tvAllTime = findViewById(R.id.tv_all_time);
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        saveCamera.setVisibility(View.GONE);
        spot = findViewById(R.id.spot);

        // ****************预览模式****************
        // 未通过提示
        livenessTipsFailRl = findViewById(R.id.testimony_tips_failRl);
        livenessTipsFailTv = findViewById(R.id.testimony_tips_failTv);
        livenessTipsPleaseFailTv = findViewById(R.id.testimony_tips_please_failTv);
        livenessTipsFailIv = findViewById(R.id.testimony_tips_failIv);
        // 预览模式buttom
        livenessButtomLl = findViewById(R.id.person_buttomLl);
        kaifaRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        livenessAddIv = findViewById(R.id.testimony_addIv);
        livenessAddIv.setOnClickListener(this);
        livenessUpdateTv = findViewById(R.id.testimony_upload_filesTv);
        livenessAgainRl = findViewById(R.id.testimony_showRl);
        livenessShowIv = findViewById(R.id.testimony_showImg);
        TextView livenessAgainTv = findViewById(R.id.testimony_showAgainTv);
        livenessAgainTv.setOnClickListener(this);

        // 双摄像头
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            Toast.makeText(this, "未检测到2个摄像头", Toast.LENGTH_LONG).show();
            return;
        } else {
            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
            mPreview[1] = new PreviewTexture(this, irTexture);
        }
        glSurfaceView = findViewById(R.id.camera_textureview);
        glSurfaceView.initSurface(SingleBaseConfig.getBaseConfig().getRgbRevert(),
                SingleBaseConfig.getBaseConfig().getMirrorVideoRGB() , SingleBaseConfig.getBaseConfig().isOpenGl());
        CameraPreviewManager.getInstance().startPreview(/*mContext, */glSurfaceView,
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
                ViewGroup.LayoutParams layoutParamsNirRl = testNirRl.getLayoutParams();
                ViewGroup.LayoutParams layoutParams = irTexture.getLayoutParams();
                int w = layoutParams.width;
                int h = layoutParams.height;
                int cameraRotation = SingleBaseConfig.getBaseConfig().getNirVideoDirection();
                mCamera[1].setDisplayOrientation(cameraRotation);
                if (cameraRotation == 90 || cameraRotation == 270) {
                    layoutParams.height = h > w ? h : w;
                    layoutParams.width = h > w ? w : h;
                    layoutParamsNirRl.width = h > w ? w : h;
                    // 旋转90度或者270，需要调整宽高
                } else {
                    layoutParams.height = h > w ? w : h;
                    layoutParams.width = h > w ? h : w;
                    layoutParamsNirRl.width = h > w ? h : w;
                }
                irTexture.setLayoutParams(layoutParams);
                testNirRl.setLayoutParams(layoutParamsNirRl);
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
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        //  CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
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
//      CameraPreviewManager.getInstance().stopPreview();
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
        glSurfaceView.setFrame();
        bdFaceImageConfig.setData(data);
        checkData();
    }

    private void dealIr(byte[] data) {
//        SystemClock.sleep(30);
        bdNirFaceImageConfig.setData(data);
        checkData();
    }

    private synchronized void checkData() {
        if (bdFaceImageConfig.data != null && bdNirFaceImageConfig.data != null) {
            if (bdFaceCheckConfig.getSecondFeature() != null) {
                FaceSDKManager.getInstance().onDetectCheck(bdFaceImageConfig, bdNirFaceImageConfig, null,
                        bdFaceCheckConfig, new FaceDetectCallBack() {
                            @Override
                            public void onFaceDetectCallback(final LivenessModel livenessModel) {
                                // 预览模式
                                checkCloseDebugResult(livenessModel);
                                // 开发模式
                                checkOpenDebugResult(livenessModel);
                                if (isSaveImage){
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
                    return;
                }
                score = livenessModel.getScore();
                if (isDevelopment == false) {
                    layoutCompareStatus.setVisibility(View.GONE);
                    livenessTipsFailRl.setVisibility(View.VISIBLE);
                    if (isFace == true) {
                        livenessTipsFailTv.setText("上传图片不包含人脸");
                        livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                        livenessTipsPleaseFailTv.setText("无法进行人证比对");
                        livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                        return;
                    }
                    rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    nirLivenessScore = livenessModel.getIrLivenessScore();
                    if (rgbLivenessScore < rgbLiveScore || nirLivenessScore <
                            nirLiveScore) {
                        livenessTipsFailTv.setText("人证核验未通过");
                        livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                        livenessTipsPleaseFailTv.setText("请上传正面人脸照片");
                        livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                        return;
                    }
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
                }
            }
        });
    }

    // 开发模式
    private void checkOpenDebugResult(final LivenessModel model) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (model != null) {
                    BDFaceImageInstance image = model.getBdFaceImageInstance();
                    if (image != null) {
                        testImageview.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                    }

                    tvNirLiveTime.setText(String.format("相似度分数：%s", score));
                    tvNirLiveScore.setText(String.format("活体检测耗时：%s ms", model.getIrLivenessDuration()));

                    //  比较两个人脸
                    if (firstFeature == null || secondFeature == null) {
                        return;
                    }

//                    if (rgbLivenessScore < rgbLiveScore || nirLivenessScore < nirLiveScore) {
//                        tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", 0));
//                        tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms", 0));
//                        tvAllTime.setText(String.format("总耗时：%s ms", model.getAllDetectDuration()));
//                    } else {
//                    }
                    tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", model.getFeatureDuration()));
                    tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms",
                            model.getCheckDuration()));
                    tvAllTime.setText(String.format("总耗时：%s ms", model.getAllDetectDuration()));

                    if (isDevelopment) {
                        livenessTipsFailRl.setVisibility(View.GONE);
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        rgbLivenessScore = model.getRgbLivenessScore();
                        nirLivenessScore = model.getIrLivenessScore();
                        if (nirLivenessScore < nirLiveScore) {
                            testNirIv.setVisibility(View.VISIBLE);
                            testNirIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                        } else {
                            testNirIv.setVisibility(View.VISIBLE);
                            testNirIv.setImageResource(R.mipmap.ic_icon_develop_success);
                        }
                        if (rgbLivenessScore < rgbLiveScore) {
                            testRgbIv.setVisibility(View.VISIBLE);
                            testRgbIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                        } else {
                            testRgbIv.setVisibility(View.VISIBLE);
                            testRgbIv.setImageResource(R.mipmap.ic_icon_develop_success);
                        }
                    } else {
                        testRgbIv.setVisibility(View.VISIBLE);
                        testRgbIv.setImageResource(R.mipmap.ic_icon_develop_success);
                    }
                    if (model .isQualityCheck()){
                            textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                        textCompareStatus.setMaxEms(6);
                        textCompareStatus.setText("请正视摄像头");
                    } else if (rgbLivenessScore < rgbLiveScore || nirLivenessScore < nirLiveScore) {
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
                } else {
                    layoutCompareStatus.setVisibility(View.GONE);
                    testNirIv.setVisibility(View.GONE);
                    testRgbIv.setVisibility(View.GONE);
                    // 开发模式
                    testImageview.setImageResource(R.mipmap.ic_image_video);
                    tvNirLiveTime.setText(String.format("相似度分数：%s", 0));
                    tvNirLiveScore.setText(String.format("活体检测耗时：%s ms", 0));
                    tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", 0));
                    tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms", 0));
                    tvAllTime.setText(String.format("总耗时：%s ms", 0));
                }
            }
        });
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
            // 预览模式
        } else if (id == R.id.preview_text) {
            isDevelopment = false;
            if (livenessShowIv.getDrawable() != null || hintShowIv.getDrawable() != null) {
                livenessTipsFailRl.setVisibility(View.VISIBLE);
                layoutCompareStatus.setVisibility(View.GONE);
            } else {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            testimonyPreviewLineIv.setVisibility(View.VISIBLE);
            testimonyDevelopmentLineIv.setVisibility(View.GONE);
            testimonyDevelopmentTv.setTextColor(Color.parseColor("#FF999999"));
            testimonyPreviewTv.setTextColor(getResources().getColor(R.color.white));
            testNirRl.setVisibility(View.GONE);
            livenessButtomLl.setVisibility(View.VISIBLE);
            kaifaRelativeLayout.setVisibility(View.GONE);
            livenessBaiduTv.setVisibility(View.VISIBLE);
//                test_nir_view.setVisibility(View.GONE);
            testRelativeLayout.setVisibility(View.GONE);
            irTexture.setAlpha(0);
            testImageview.setVisibility(View.GONE);
            saveCamera.setVisibility(View.GONE);
            isSaveImage = false;
            spot.setVisibility(View.GONE);
            // 开发模式
        } else if (id == R.id.develop_text) {
            if (livenessShowIv.getDrawable() != null || hintShowIv.getDrawable() != null) {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.VISIBLE);
            } else {
                livenessTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
            isDevelopment = true;
            testimonyPreviewLineIv.setVisibility(View.GONE);
            testimonyDevelopmentLineIv.setVisibility(View.VISIBLE);
            testimonyDevelopmentTv.setTextColor(getResources().getColor(R.color.white));
            testimonyPreviewTv.setTextColor(Color.parseColor("#FF999999"));
            testNirRl.setVisibility(View.VISIBLE);
            livenessButtomLl.setVisibility(View.GONE);
            kaifaRelativeLayout.setVisibility(View.VISIBLE);
            livenessBaiduTv.setVisibility(View.GONE);
            irTexture.setAlpha(1);
            testImageview.setVisibility(View.VISIBLE);
            testRelativeLayout.setVisibility(View.VISIBLE);
            saveCamera.setVisibility(View.VISIBLE);
            judgeFirst();
        } else if (id == R.id.btn_setting) {
            if (!FaceSDKManager.initModelSuccess) {
                Toast.makeText(mContext, "SDK正在加载模型，请稍后再试",
                        Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(mContext, IdentifySettingActivity.class));
            finish();
        } else if (id == R.id.testimony_addIv) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_PHOTO_FRIST);
        } else if (id == R.id.testimony_showAgainTv) {
            Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent2, PICK_PHOTO_FRIST);
        } else if (id == R.id.hint_adainTv) {
            Intent intent3 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent3, PICK_PHOTO_FRIST);
        } else if (id == R.id.Development_addIv) {
            Intent intent4 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent4, PICK_PHOTO_FRIST);
        } else if (id == R.id.save_camera){
            isSaveImage = !isSaveImage;
            if (isSaveImage){
                spot.setVisibility(View.VISIBLE);
                ToastUtils.toast(FaceIRTestimonyActivity.this, "存图功能已开启再次点击可关闭");
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


    private synchronized void rgbOrIr(int index, byte[] data) {
        byte[] tmp = new byte[PREFER_WIDTH * PERFER_HEIGH];
        try {
            System.arraycopy(data, 0, tmp, 0, PREFER_WIDTH * PERFER_HEIGH);
        } catch (Exception e) {
            Log.e("qing", String.valueOf(e.getStackTrace()));
        }
        int count = 0;
        int total = 0;
        for (int i = 0; i < PREFER_WIDTH * PERFER_HEIGH; i = i + 10) {
            total += byteToInt(tmp[i]);
            count++;
        }

        if (count == 0) {
            return;
        }

        if (index == 0) {
            camemra1DataMean = total / count;
        } else {
            camemra2DataMean = total / count;
        }
        if (camemra1DataMean != 0 && camemra2DataMean != 0) {
            if (camemra1DataMean > camemra2DataMean) {
                camemra1IsRgb = true;
            } else {
                camemra1IsRgb = false;
            }
        }
    }

    public int byteToInt(byte b) {
        // Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    private void choiceRgbOrIrType(int index, byte[] data) {
        // camera1如果为rgb数据，调用dealRgb，否则为Ir数据，调用Ir
        if (index == 0) {
            if (camemra1IsRgb) {
                dealRgb(data);
            } else {
                dealIr(data);
            }
        } else {
            if (camemra1IsRgb) {
                dealIr(data);
            } else {
                dealRgb(data);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PHOTO_FRIST && (data != null && data.getData() != null)) {
            Uri uri1 = ImageUtils.geturi(data, this);
            try {
                final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri1));
                if (bitmap != null) {
                    // 提取特征值
//                    syncFeature(bitmap, secondFeature, 2, true);
                    float ret = FaceSDKManager.getInstance().personDetect(bitmap, secondFeature,
                            FaceUtils.getInstance().getBDFaceCheckConfig() , this);
                    livenessShowIv.setVisibility(View.VISIBLE);
                    hintShowIv.setVisibility(View.VISIBLE);
                    livenessShowIv.setImageBitmap(bitmap);
                    hintShowIv.setImageBitmap(bitmap);
                    if (ret != -1) {
                        isFace = false;
                        if (ret == 128) {
                            bdFaceCheckConfig.setSecondFeature(secondFeature);
                            toast("图片特征抽取成功");
                            hintShowIv.setVisibility(View.VISIBLE);
                            livenessShowIv.setVisibility(View.VISIBLE);
                            hintShowRl.setVisibility(View.VISIBLE);
                            livenessAgainRl.setVisibility(View.VISIBLE);
                            livenessAddIv.setVisibility(View.GONE);
                            livenessUpdateTv.setVisibility(View.GONE);
                            developmentAddRl.setVisibility(View.GONE);
                        } else {
                            ToastUtils.toast(mContext, "图片特征抽取失败");
                        }
                    } else {
                        isFace = true;
                        isFace = true;
                        // 上传图片无人脸隐藏
                        livenessShowIv.setVisibility(View.GONE);
                        hintShowIv.setVisibility(View.GONE);
                        livenessAddIv.setVisibility(View.GONE);
                        livenessUpdateTv.setVisibility(View.GONE);
                        livenessAgainRl.setVisibility(View.VISIBLE);
                        hintShowIv.setVisibility(View.GONE);
                        livenessShowIv.setVisibility(View.GONE);
                        hintShowRl.setVisibility(View.VISIBLE);
                        developmentAddRl.setVisibility(View.GONE);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
            }
        });
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
