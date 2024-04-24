package com.baidu.idl.face.main.finance.activity.finance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.finance.model.SingleBaseConfig;
import com.baidu.idl.face.main.finance.setting.FinanceSettingActivity;
import com.baidu.idl.face.main.finance.utils.FaceUtils;
import com.baidu.idl.face.main.finance.utils.TestPopWindow;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.financelibrary.R;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.example.datalibrary.callback.FaceDetectCallBack;
import com.example.datalibrary.deptrum.BaseDeptrumActivity;
import com.example.datalibrary.deptrum.GLFrameSurface;
import com.example.datalibrary.deptrum.MantleGLFrameSurface;
import com.example.datalibrary.listener.SdkInitListener;
import com.example.datalibrary.manager.FaceSDKManager;
import com.example.datalibrary.manager.SaveImageManager;
import com.example.datalibrary.model.BDFaceCheckConfig;
import com.example.datalibrary.model.BDFaceImageConfig;
import com.example.datalibrary.model.BDLiveConfig;
import com.example.datalibrary.model.LivenessModel;
import com.example.datalibrary.utils.BitmapUtils;
import com.example.datalibrary.utils.FaceOnDrawTexturViewUtil;
import com.example.datalibrary.utils.ToastUtils;


public class FaceDeptrumFinanceActivity extends BaseDeptrumActivity implements View.OnClickListener {

    /*图片越大，性能消耗越大，也可以选择640*480， 1280*720*/
    private Context mContext;
    private boolean isCheck = false;
    private boolean isTime = true;
    private long searshTime;
    private boolean isCompareCheck = false;
    private boolean isNeedCamera = true;
    // 包含适配屏幕后后的人脸的x坐标，y坐标，和width
    private float[] pointXY = new float[4];

    private TextView preText;
    private ImageView previewView;
    private RelativeLayout preViewRelativeLayout;

    private TextView deveLop;
    private RelativeLayout deveLopRelativeLayout;
    private ImageView developView;
    private TextView preToastText;
    private TextView detectSurfaceText;
    private ImageView isCheckImage;
    private ImageView mFaceDetectImageView;
    private TextView mTvDetect;
    private TextView mTvLive;
    private TextView mTvLiveScore;
    private TextView mTvAllTime;
    private RelativeLayout progressLayout;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private ImageView progressBarView;
    private RelativeLayout payHintRl;
    private boolean mIsPayHint = true;
    private boolean count = true;
    private RelativeLayout financeQualityTestFailed;
    private TextView qualityTestTimeTv;
    private TextView qualityDetectedTv;
    private TextView qualityShelteredPart;
    private Button qualityRetestDetectBtn;
    private RelativeLayout financeByLivingDetection;
    private RelativeLayout financeFailedInVivoTest;
    private TextView failedInVivoTestRgb;
    private TextView failedInVivoTestNir;
    private TextView failedInVivoTestDepth;
    private TextView failedInVivoTestTime;
    private TextView failedInVivoTestFrames;
    private TextView byLivingDetectionRgb;
    private TextView byLivingDetectionNir;
    private TextView byLivingDetectionDepth;
    private TextView byLivingTetectionTime;
    private TextView byLivingDetectionFrames;
    private ImageView qualityDetectRegImageItem;
    private ImageView noDetectRegImageItem;
    private ImageView detectRegImageItem;
    private TestPopWindow testPopWindow;
    private ImageView isNirCheckImage;
    private ImageView isDepthCheckImage;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;
    private boolean liveStatus;
    // 深度数据显示
    private TextView mTvIr;
    private TextView mTvIrScore;
    /*深度数据显示*/
    private TextView mTvDepth;
    private TextView mTvDepthScore;
    private View showRgbNirDepth;


    private BDFaceImageConfig bdFaceImageConfig;
    private BDFaceImageConfig bdNirFaceImageConfig;
    private BDFaceImageConfig bdDepthFaceImageConfig;
    private BDFaceCheckConfig bdFaceCheckConfig;
    private BDLiveConfig bdLiveConfig;
    private MantleGLFrameSurface mantleGLFrameSurface;
    // 调试页面控件
    private TextureView mDrawDetectFaceView;
    // 人脸框绘制
    private RectF rectF;
    private Paint paint;
    private Paint paintBg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        setContentView(R.layout.activity_face_deptrum_finance);
        initFaceConfig(RGB_HEIGHT , RGB_WIDTH);
        initNirFaceConfig(RGB_HEIGHT , RGB_WIDTH);
        initDepthFaceConfig(RGB_HEIGHT , RGB_WIDTH);
        initFaceCheck();
        initView();
    }

    @Override
    protected void dealRgb(final byte[] data) {
        bdFaceImageConfig.setData(data);
        checkData();
    }

    @Override
    protected void dealIr(final byte[] data) {
        bdNirFaceImageConfig.setData(data);
    }

    @Override
    protected void dealDepth(byte[] data) {
        bdDepthFaceImageConfig.setData(data);
    }


    private void initFaceConfig(int height , int width){
        bdFaceImageConfig = new BDFaceImageConfig(height , width ,
                SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectRGB() ,
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB);
    }
    private void initNirFaceConfig(int height , int width){
        bdNirFaceImageConfig = new BDFaceImageConfig(height , width ,
                SingleBaseConfig.getBaseConfig().getNirDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectNIR() ,
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB);
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
     * View
     */
    private void initView() {
        showRgbNirDepth = findViewById(R.id.show_rgb_nir_depth);
        mantleGLFrameSurface = findViewById(R.id.gl_rgb);
        mantleGLFrameSurface.initSurface();
        mRgbSurface = mantleGLFrameSurface.getGLFrameSurface();
        mIrSurface = (GLFrameSurface) findViewById(R.id.gl_ir);
        mDepSurface = (GLFrameSurface) findViewById(R.id.gl_depth);
        mantleGLFrameSurface.setDraw(true);
        isNirCheckImage = findViewById(R.id.nir_is_check_image_Iv);
        isDepthCheckImage = findViewById(R.id.depth_is_check_image);
        // Ir活体
        mTvIr = findViewById(R.id.tv_nir_live_time);
        mTvIrScore = findViewById(R.id.tv_nir_live_score);
        // depth活体
        mTvDepth = findViewById(R.id.tv_depth_live_time);
        mTvDepthScore = findViewById(R.id.tv_depth_live_score);
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
        previewView = findViewById(R.id.preview_view);
        // 信息展示
        preViewRelativeLayout = findViewById(R.id.yvlan_relativeLayout);
        preToastText = findViewById(R.id.pre_toast_text);
        progressLayout = findViewById(R.id.progress_layout);
        progressBarView = findViewById(R.id.progress_bar_view);
        // 预览模式下提示
        payHintRl = findViewById(R.id.pay_hintRl);

        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()){
            mDrawDetectFaceView.setRotationY(180);
        }
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        // ***************开发模式*************
        // 导航栏
        deveLop = findViewById(R.id.develop_text);
        deveLop.setOnClickListener(this);
        deveLop.setTextColor(Color.parseColor("#a9a9a9"));
        developView = findViewById(R.id.develop_view);
        developView.setVisibility(View.GONE);
        // 信息展示
        deveLopRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        isCheckImage = findViewById(R.id.is_check_image);
        detectSurfaceText = findViewById(R.id.detect_surface_text);
        detectSurfaceText.setVisibility(View.GONE);
        // 送检RGB 图像回显
        mFaceDetectImageView = findViewById(R.id.face_detect_image_view);
        mFaceDetectImageView.setVisibility(View.GONE);
        // 检测耗时
        mTvDetect = findViewById(R.id.tv_detect_time);
        // RGB活体
        mTvLive = findViewById(R.id.tv_rgb_live_time);
        mTvLiveScore = findViewById(R.id.tv_rgb_live_score);
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

        // 质量检测未通过
        financeQualityTestFailed = findViewById(R.id.finance_quality_test_failed);
        qualityTestTimeTv = findViewById(R.id.quality_test_timeTv);
        qualityDetectedTv = findViewById(R.id.quality_detectedTv);
        qualityShelteredPart = findViewById(R.id.quality_sheltered_part);
        qualityRetestDetectBtn = findViewById(R.id.quality_retest_detectBtn);
        qualityRetestDetectBtn.setOnClickListener(this);
        qualityDetectRegImageItem = findViewById(R.id.quality_detect_reg_image_item);


        // 活体通过
        financeFailedInVivoTest = findViewById(R.id.finance_failed_in_vivo_test);
        byLivingDetectionRgb = findViewById(R.id.by_living_detection_rgb);
        byLivingDetectionNir = findViewById(R.id.by_living_detection_nir);
        byLivingDetectionDepth = findViewById(R.id.by_living_detection_depth);
        byLivingTetectionTime = findViewById(R.id.by_living_detection_time);
        byLivingDetectionFrames = findViewById(R.id.by_living_detection_Frames);
        Button byLivingDetectionBtn = findViewById(R.id.by_living_detection_btn);
        byLivingDetectionBtn.setOnClickListener(this);
        detectRegImageItem = findViewById(R.id.detect_reg_image_item);

        // 活体未通过
        financeByLivingDetection = findViewById(R.id.finance_by_living_detection);
        failedInVivoTestRgb = findViewById(R.id.failed_in_vivo_test_rgb);
        failedInVivoTestNir = findViewById(R.id.failed_in_vivo_test_nir);
        failedInVivoTestDepth = findViewById(R.id.failed_in_vivo_test_depth);
        failedInVivoTestTime = findViewById(R.id.failed_in_vivo_test_time);
        failedInVivoTestFrames = findViewById(R.id.failed_in_vivo_test_Frames);
        Button failedInVivoTestBtn = findViewById(R.id.failed_in_vivo_testBtn);
        failedInVivoTestBtn.setOnClickListener(this);
        noDetectRegImageItem = findViewById(R.id.no_detect_reg_image_item);

        testPopWindow = new TestPopWindow(this,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        testPopWindow.setmOnClickFinance(new TestPopWindow.OnClickFinance() {
            @Override
            public void rester(boolean isReTest) {
                // 重新检测
                if (isReTest) {
                    testPopWindow.closePopupWindow();
                    progressLayout.setVisibility(View.VISIBLE);
                    payHintRl.setVisibility(View.GONE);
                    count = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (count) {
                                isNeedCamera = true;
                            }
                        }
                    }, 500);  // 延迟1秒执行
                    // 返回首页
                } else {
                    testPopWindow.closePopupWindow();
                    finish();
//                    financeNoDetect.setVisibility(View.GONE);
                }
            }
        });
    }

    private void checkData() {
        // 摄像头预览数据进行人脸检测
        if (isNeedCamera) {
            if (bdFaceImageConfig.data != null && bdNirFaceImageConfig.data != null &&
                    bdDepthFaceImageConfig.data != null) {
                FaceSDKManager.getInstance().onDetectSilentLiveCheck(bdFaceImageConfig,
                        bdNirFaceImageConfig, bdDepthFaceImageConfig,
                        bdFaceCheckConfig, new FaceDetectCallBack() {
                            @Override
                            public void onFaceDetectCallback(LivenessModel livenessModel) {
                                // 输出结果
                                if (mantleGLFrameSurface.isDraw()) {
                                    // 预览模式
                                    checkCloseDebugResult(livenessModel);
                                } else {
                                    // 开发模式
                                    checkOpenDebugResult(livenessModel);
                                }

                                if (isSaveImage) {
                                    SaveImageManager.getInstance().saveImage(livenessModel, bdLiveConfig);
                                    ;
                                }
                            }

                            @Override
                            public void onTip(int code, String msg) {
                            }

                            @Override
                            public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                                // 绘制人脸框
                                if (!mantleGLFrameSurface.isDraw()) {
                                    showFrame(livenessModel);
                                }
                            }
                        });
            }
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
                    if (endSearchTime < 3000) {
                        preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                        preToastText.setText("请保持面部在取景框内");
                        progressBarView.setImageResource(R.mipmap.ic_loading_grey);
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (count) {
                                    count = false;
                                    payHint(null);
                                }
                            }
                        }, 500);  // 延迟1秒执行
                    }
                    return;
                }

                isTime = true;
                pointXY[0] = livenessModel.getFaceInfo().centerX;
                pointXY[1] = livenessModel.getFaceInfo().centerY;
                pointXY[2] = livenessModel.getFaceInfo().width;
                pointXY[3] = livenessModel.getFaceInfo().width;
                FaceOnDrawTexturViewUtil.converttPointXY(pointXY, mantleGLFrameSurface,
                        livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);
                float leftLimitX = mantleGLFrameSurface.circleX - mantleGLFrameSurface.circleRadius;
                float rightLimitX = mantleGLFrameSurface.circleX + mantleGLFrameSurface.circleRadius;
                float topLimitY = mantleGLFrameSurface.circleY - mantleGLFrameSurface.circleRadius;
                float bottomLimitY = mantleGLFrameSurface.circleY + mantleGLFrameSurface.circleRadius;
                if (pointXY[0] - pointXY[2] / 2 < leftLimitX
                        || pointXY[0] + pointXY[2] / 2 > rightLimitX
                        || pointXY[1] - pointXY[3] / 2 < topLimitY
                        || pointXY[1] + pointXY[3] / 2 > bottomLimitY) {
                    preToastText.setTextColor(Color.parseColor("#FFFFFF"));
                    preToastText.setText("请保持面部在取景框内");
                    progressBarView.setImageResource(R.mipmap.ic_loading_grey);
                    return;
                }
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

    //  ***************开发模式结果输出*************
    private void checkOpenDebugResult(final LivenessModel livenessModel) {

        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (livenessModel == null) {
                    liveStatus = false;
                    layoutCompareStatus.setVisibility(View.GONE);
                    isCheckImage.setVisibility(View.GONE);
                    mFaceDetectImageView.setImageResource(R.mipmap.ic_image_video);
                    mTvDetect.setText(String.format("检测耗时 ：%s ms", 0));
                    mTvLive.setText(String.format("RGB活体检测耗时 ：%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB活体检测结果 ：%s", false));
                    mTvIr.setText(String.format("NIR活体检测耗时 ：%s ms", 0));
                    mTvIrScore.setText(String.format("NIR活体检测结果 ：%s", false));
                    mTvDepth.setText(String.format("Depth活体检测耗时 ：%s ms", 0));
                    mTvDepthScore.setText(String.format("Depth活体检测结果 ：%s", false));
                    mTvAllTime.setText(String.format("总耗时 ：%s ms", 0));

                    if (isCompareCheck) {
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#fec133"));
                        textCompareStatus.setText("未检测到人脸");
                    }
                    return;
                }

                BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();
                if (image != null) {
                    mFaceDetectImageView.setImageBitmap(BitmapUtils.getInstaceBmp(image));
                    image.destory();
                }
                if (livenessModel.isQualityCheck()){
                    if (isCheck) {
                        isCheckImage.setVisibility(View.VISIBLE);
                        isCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                        isNirCheckImage.setVisibility(View.VISIBLE);
                        isNirCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                        isDepthCheckImage.setVisibility(View.VISIBLE);
                        isDepthCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                    }
                    if (isCompareCheck) {
                        liveStatus = false;
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                                                textCompareStatus.setMaxEms(6);
                        textCompareStatus.setText("请正视摄像头");
                    }
                } else {
                    if (!livenessModel.isRGBLiveStatus()) {
                        if (isCheck) {
                            isCheckImage.setVisibility(View.VISIBLE);
                            isCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                        }
                    } else {
                        if (isCheck) {
                            isCheckImage.setVisibility(View.VISIBLE);
                            isCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                        }
                    }
                    if (!livenessModel.isNIRLiveStatus()) {
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
                    if (!livenessModel.isDepthLiveStatus()) {
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
                    liveStatus = true;
                    if (!livenessModel.isDepthLiveStatus() ||
                            !livenessModel.isNIRLiveStatus() ||
                            !livenessModel.isRGBLiveStatus()) {
                        liveStatus = false;
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#fec133"));
                            textCompareStatus.setText("未通过活体检测");
                        }
                    }else {
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#00BAF2"));
                            textCompareStatus.setText("通过活体检测");
                        }
                    }
                }
                mTvDetect.setText(String.format("检测耗时 ：%s ms", livenessModel.getRgbDetectDuration()));
                mTvLive.setText(String.format("RGB活体检测耗时 ：%s ms", livenessModel.getRgbLivenessDuration()));
                mTvLiveScore.setText(String.format("RGB活体检测结果 ：%s", livenessModel.isRGBLiveStatus()));
                mTvIr.setText(String.format("NIR活体检测耗时 ：%s ms", livenessModel.getIrLivenessDuration()));
                mTvIrScore.setText(String.format("NIR活体检测结果 ：%s", livenessModel.isNIRLiveStatus()));
                mTvDepth.setText(String.format("Depth活体检测耗时 ：%s ms", livenessModel.getDepthtLivenessDuration()));
                mTvDepthScore.setText(String.format("Depth活体检测结果 ：%s", livenessModel.isDepthLiveStatus()));
                mTvAllTime.setText(String.format("总耗时 ：%s ms", livenessModel.getAllDetectDuration()));
            }
        });
    }


    @Override
    public void onClick(View v) {
        // 返回
        int id = v.getId();
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
            startActivity(new Intent(mContext, FinanceSettingActivity.class));
            finish();
        } else if (id == R.id.preview_text) {
            if (payHintRl.getVisibility() == View.VISIBLE) {
                return;
            }
            mantleGLFrameSurface.setDraw(true);
            previewView.setVisibility(View.VISIBLE);
            preText.setTextColor(Color.parseColor("#ffffff"));
            preViewRelativeLayout.setVisibility(View.VISIBLE);
            preToastText.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.VISIBLE);
            progressBarView.setVisibility(View.VISIBLE);
            payHintRl.setVisibility(View.GONE);
            layoutCompareStatus.setVisibility(View.GONE);
            developView.setVisibility(View.GONE);
            deveLop.setTextColor(Color.parseColor("#a9a9a9"));
            deveLopRelativeLayout.setVisibility(View.GONE);
            detectSurfaceText.setVisibility(View.GONE);
            detectSurfaceText.setVisibility(View.GONE);
            mFaceDetectImageView.setVisibility(View.GONE);
            isCheckImage.setVisibility(View.GONE);
            mIrSurface.setVisibility(View.INVISIBLE);
            mDepSurface.setVisibility(View.INVISIBLE);
            isCompareCheck = false;
            isCheck = false;
            mIsPayHint = true;
            count = true;
            saveCamera.setVisibility(View.GONE);
            isSaveImage = false;
            spot.setVisibility(View.GONE);
            isPause = false;
            showRgbNirDepth.setVisibility(View.GONE);
            Canvas canvas = mDrawDetectFaceView.lockCanvas();
            if (canvas != null){
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        } else if (id == R.id.develop_text) {
            isNeedCamera = true;
            mIsPayHint = false;
            mantleGLFrameSurface.setDraw(false);
            previewView.setVisibility(View.GONE);
            showRgbNirDepth.setVisibility(View.VISIBLE);
            mIrSurface.setVisibility(View.VISIBLE);
            mDepSurface.setVisibility(View.VISIBLE);
            preText.setTextColor(Color.parseColor("#a9a9a9"));
            preViewRelativeLayout.setVisibility(View.GONE);
            preToastText.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);
            progressBarView.setVisibility(View.GONE);
            payHintRl.setVisibility(View.GONE);

            testPopWindow.closePopupWindow();

            financeQualityTestFailed.setVisibility(View.GONE);
            financeFailedInVivoTest.setVisibility(View.GONE);
            financeByLivingDetection.setVisibility(View.GONE);

            developView.setVisibility(View.VISIBLE);
            deveLop.setTextColor(Color.parseColor("#ffffff"));
            deveLopRelativeLayout.setVisibility(View.VISIBLE);
            detectSurfaceText.setVisibility(View.VISIBLE);
            mFaceDetectImageView.setVisibility(View.VISIBLE);
            isCheckImage.setVisibility(View.VISIBLE);
            isCompareCheck = true;
            isCheck = true;
            count = false;
            saveCamera.setVisibility(View.VISIBLE);
            judgeFirst();
            isPause = true;
        } else if (id == R.id.quality_retest_detectBtn) {
            progressLayout.setVisibility(View.VISIBLE);
            payHintRl.setVisibility(View.GONE);
            financeQualityTestFailed.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    count = true;
                    if (count) {
                        isNeedCamera = true;
                    }
                }
            }, 1 * 1000);  // 延迟1秒执行
        } else if (id == R.id.failed_in_vivo_testBtn) {
            progressLayout.setVisibility(View.VISIBLE);
            payHintRl.setVisibility(View.GONE);
            financeFailedInVivoTest.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    count = true;
                    if (count) {
                        isNeedCamera = true;
                    }
                }
            }, 1 * 1000);  // 延迟1秒执行
        } else if (id == R.id.by_living_detection_btn) {
            progressLayout.setVisibility(View.VISIBLE);
            payHintRl.setVisibility(View.GONE);
            financeByLivingDetection.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    count = true;
                    if (count) {
                        isNeedCamera = true;
                    }
                }
            }, 1 * 1000);  // 延迟1秒执行
        } else if (id == R.id.save_camera){
            isSaveImage = !isSaveImage;
            if (isSaveImage){
                spot.setVisibility(View.VISIBLE);
                ToastUtils.toast(FaceDeptrumFinanceActivity.this, "存图功能已开启再次点击可关闭");
            }else {
                spot.setVisibility(View.GONE);
            }
        }
    }

    private void judgeFirst(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFinanceFirstSave", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            setFirstView(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFirstView(View.GONE);
                }
            },3000);
            editor.putBoolean("isFinanceFirstSave", false);
            editor.commit();
        }
    }

    private void setFirstView(int visibility){
        findViewById(R.id.first_text_tips).setVisibility(visibility);
        findViewById(R.id.first_circular_tips).setVisibility(visibility);
    }


    @SuppressLint("NewApi")
    private void payHint(final LivenessModel livenessModel) {
        if (livenessModel == null && mIsPayHint) {
            progressLayout.setVisibility(View.GONE);
            if (!this.isFinishing()) {
                testPopWindow.showPopupWindow(FaceDeptrumFinanceActivity.this.getWindow().getDecorView());
                isNeedCamera = false;
            }
        }

        if (mIsPayHint && livenessModel != null) {
            BDFaceImageInstance bdFaceImageInstance = livenessModel.getBdFaceImageInstance();
            Bitmap instaceBmp = BitmapUtils.getInstaceBmp(bdFaceImageInstance);
            testPopWindow.closePopupWindow();
            progressLayout.setVisibility(View.VISIBLE);
            payHintRl.setVisibility(View.VISIBLE);
            if (livenessModel.getQualityDetect() != null ||
                    livenessModel.getQualityOcclusion() != null) {
                if (mIsPayHint) {
                    qualityDetectRegImageItem.setImageBitmap(instaceBmp);
                    financeQualityTestFailed.setVisibility(View.VISIBLE);
                    isNeedCamera = false;
                    count = false;

                    qualityTestTimeTv.setText("检测耗时：" + livenessModel.getAllDetectDuration() + " ms");
                    StringBuffer stringBufferDetected = new StringBuffer();
                    StringBuffer stringBufferOcclusion = new StringBuffer();
                    stringBufferDetected.append(livenessModel.getQualityDetect());
                    stringBufferOcclusion.append(livenessModel.getQualityOcclusion());
                    if (stringBufferDetected.toString() == "") {
                        qualityDetectedTv.setVisibility(View.GONE);
                    } else {
                        qualityDetectedTv.setVisibility(View.VISIBLE);
                    }
                    if (stringBufferOcclusion.toString() == "") {
                        qualityShelteredPart.setVisibility(View.GONE);
                    } else {
                        qualityShelteredPart.setVisibility(View.VISIBLE);
                    }

                    qualityDetectedTv.setText("检测到：" + stringBufferDetected.toString());
                    qualityShelteredPart.setText("遮挡部位：" + stringBufferOcclusion.toString());

                }
            } else {
                if (livenessModel.isRGBLiveStatus()) {
                    financeByLivingDetection.setVisibility(View.VISIBLE);
                    financeFailedInVivoTest.setVisibility(View.GONE);
                    detectRegImageItem.setImageBitmap(instaceBmp);
                    isNeedCamera = false;
                    byLivingDetectionRgb.setText("RGB活体检测耗时：" + livenessModel.getRgbLivenessDuration() + " ms");
                    byLivingDetectionNir.setText("NIR活体检测耗时：" + livenessModel.getIrLivenessDuration() + " ms");
                    byLivingDetectionDepth.setText("Depth活体检测耗时：" + livenessModel.getDepthtLivenessDuration() + " ms");
                    byLivingTetectionTime.setText("活体检测总耗时：" + livenessModel.getAllDetectDuration() + " ms");
                    byLivingDetectionFrames.setText("活体检测帧数：" +
                            SingleBaseConfig.getBaseConfig().getFramesThreshold() + " 帧");
                } else {
                    financeFailedInVivoTest.setVisibility(View.VISIBLE);
                    financeByLivingDetection.setVisibility(View.GONE);
                    noDetectRegImageItem.setImageBitmap(instaceBmp);
                    isNeedCamera = false;
                    failedInVivoTestRgb.setText("RGB活体检测结果：" + livenessModel.isRGBLiveStatus());
                    failedInVivoTestNir.setText("NIR活体检测结果：" + livenessModel.isNIRLiveStatus());
                    failedInVivoTestDepth.setText("Depth活体检测结果：" + livenessModel.isDepthLiveStatus());
                    failedInVivoTestTime.setText("活体检测总耗时：" + livenessModel.getAllDetectDuration() + " ms");
                    failedInVivoTestFrames.setText("活体检测帧数：" +
                            SingleBaseConfig.getBaseConfig().getFramesThreshold() + " 帧");
                }
            }
        }
    }
    private boolean isPause = false;
    public void showFrame(final LivenessModel model){
        if (model == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                if (model == null) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mantleGLFrameSurface, model.getBdFaceImageInstance());
                // 人脸框颜色
                FaceOnDrawTexturViewUtil.drawFaceColor(liveStatus, paint, paintBg, model);
                // 绘制人脸框
                FaceOnDrawTexturViewUtil.drawRect(canvas,
                        rectF, paint, 5f, 50f , 25f);
                // 清空canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRgbSurface.onPause();
        mIrSurface.onPause();
        mDepSurface.onPause();
    }
}
