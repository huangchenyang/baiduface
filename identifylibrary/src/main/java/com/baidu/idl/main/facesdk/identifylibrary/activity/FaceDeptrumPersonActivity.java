package com.baidu.idl.main.facesdk.identifylibrary.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.identifylibrary.R;
import com.baidu.idl.main.facesdk.identifylibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.identifylibrary.setting.IdentifySettingActivity;
import com.baidu.idl.main.facesdk.identifylibrary.utils.FaceUtils;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.example.datalibrary.callback.FaceDetectCallBack;
import com.example.datalibrary.deptrum.BaseDeptrumActivity;
import com.example.datalibrary.deptrum.GLFrameSurface;
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

import java.io.FileNotFoundException;

public class FaceDeptrumPersonActivity extends BaseDeptrumActivity implements View.OnClickListener {
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
    private ImageView isDepthCheckImage;
    private ImageView isNirCheckImage;

    private int mLiveType;

    private TextureView mDrawDetectFaceView;
    private RelativeLayout personButtomLl;
    private TextView personBaiduTv;
    private ImageView testImageview;
    private Paint paintBg;
    private RectF rectF;
    private Paint paint;
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
    private BDFaceImageConfig bdFaceImageConfig;
    private BDFaceImageConfig bdNirFaceImageConfig;
    private BDFaceImageConfig bdDepthFaceImageConfig;
    private BDFaceCheckConfig bdFaceCheckConfig;
    private BDLiveConfig bdLiveConfig;
    private View showRgbNirDepth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        setContentView(R.layout.activity_face_deptrum_identifylibrary);
        FaceSDKManager.getInstance().emptyFrame();
        initFaceCheck();
        initFaceConfig(RGB_HEIGHT, RGB_WIDTH);
        initNirFaceConfig(RGB_HEIGHT, RGB_WIDTH);
        initDepthFaceConfig(RGB_HEIGHT, RGB_WIDTH);
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

    private void initFaceConfig(int height, int width) {
        bdFaceImageConfig = new BDFaceImageConfig(height, width,
                SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectRGB(),
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB);
    }

    private void initNirFaceConfig(int height, int width) {
        bdNirFaceImageConfig = new BDFaceImageConfig(height, width,
                SingleBaseConfig.getBaseConfig().getNirDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectNIR(),
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGB);
    }

    private void initDepthFaceConfig(int height, int width) {
        bdDepthFaceImageConfig = new BDFaceImageConfig(height, width,
                SingleBaseConfig.getBaseConfig().getNirDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectNIR(),
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_DEPTH);
    }

    private void initFaceCheck() {
        bdFaceCheckConfig = FaceUtils.getInstance().getBDFaceCheckConfig();
        bdLiveConfig = FaceUtils.getInstance().getBDLiveConfig();
    }

    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(mContext,
                    FaceUtils.getInstance().getBDFaceSDKConfig(), new SdkInitListener() {
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
        mRgbSurface = (GLFrameSurface) findViewById(R.id.gl_rgb);
        mIrSurface = (GLFrameSurface) findViewById(R.id.gl_ir);
        mDepSurface = (GLFrameSurface) findViewById(R.id.gl_depth);
        paintBg = new Paint();
        rectF = new RectF();
        paint = new Paint();
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
        testImageview = findViewById(R.id.face_detect_image_view);
        personKaifaIv = findViewById(R.id.is_check_image);
        isNirCheckImage = findViewById(R.id.nir_is_check_image_Iv);
        isDepthCheckImage = findViewById(R.id.depth_is_check_image);
        view = findViewById(R.id.mongolia_view);
        showRgbNirDepth = findViewById(R.id.show_rgb_nir_depth);
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        saveCamera.setVisibility(View.GONE);
        spot = findViewById(R.id.spot);
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);

        // ****************预览模式****************
        testimonyPreviewTv = findViewById(R.id.preview_text);
        testimonyPreviewTv.setOnClickListener(this);
        testimonyPreviewLineIv = findViewById(R.id.preview_view);
        // 添加图库图片  +号添加
        testimonyAddIv = findViewById(R.id.testimony_addIv);
        testimonyAddIv.setOnClickListener(this);
        testimonyShowRl = findViewById(R.id.testimony_showRl);
        testimonyShowImg = findViewById(R.id.testimony_showImg);
        testimonyShowAgainTv = findViewById(R.id.testimony_showAgainTv);
        testimonyShowAgainTv.setOnClickListener(this);
        testimonyUploadFilesTv = findViewById(R.id.testimony_upload_filesTv);
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
        hintAdainIv = findViewById(R.id.hint_adainTv);
        hintAdainIv.setOnClickListener(this);
        // 图片展示
        hintShowIv = findViewById(R.id.hint_showIv);
        kaifaRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        // 提示
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        textCompareStatus = findViewById(R.id.text_compare_status);
        // 上传图片
        developmentAddIv = findViewById(R.id.Development_addIv);
        developmentAddIv.setOnClickListener(this);
        hintShowRl = findViewById(R.id.hint_showRl);
        developmentAddRl = findViewById(R.id.Development_addRl);


    }


    private synchronized void checkData() {
        if (bdFaceImageConfig.data != null && bdNirFaceImageConfig.data != null &&
                bdDepthFaceImageConfig.data != null) {
            if (bdFaceCheckConfig.getSecondFeature() != null) {
                FaceSDKManager.getInstance().onDetectCheck(bdFaceImageConfig,
                        bdNirFaceImageConfig, bdDepthFaceImageConfig,
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
                                        if (isSaveImage) {
                                            SaveImageManager.getInstance().saveImage(livenessModel, bdLiveConfig);
                                        }
                                    }
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // 如果开发模式或者预览模式没上传图片则显示蒙层
                        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.85f, 0.0f);
                        animator.setDuration(3000);
                        view.setBackgroundColor(Color.parseColor("#ffffff"));
                        animator.start();
                    }
                });
            }
        }
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
            if (testimonyShowImg.getDrawable() != null || hintShowIv.getDrawable() != null) {
                testimonyTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.VISIBLE);
            } else {
                testimonyTipsFailRl.setVisibility(View.GONE);
                layoutCompareStatus.setVisibility(View.GONE);
            }
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
            // 开启保存图片按钮
            saveCamera.setVisibility(View.VISIBLE);
            showRgbNirDepth.setVisibility(View.VISIBLE);
            mIrSurface.setVisibility(View.VISIBLE);
            mDepSurface.setVisibility(View.VISIBLE);
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
            showRgbNirDepth.setVisibility(View.GONE);
            mDepSurface.setVisibility(View.INVISIBLE);
            mIrSurface.setVisibility(View.INVISIBLE);
            // title显隐
            testimonyDevelopmentLineIv.setVisibility(View.GONE);
            testimonyPreviewLineIv.setVisibility(View.VISIBLE);
            testimonyDevelopmentTv.setTextColor(Color.parseColor("#FF999999"));
            testimonyPreviewTv.setTextColor(getResources().getColor(R.color.white));
            // 百度大脑技术支持显示
            personBaiduTv.setVisibility(View.VISIBLE);
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
        } else if (id == R.id.save_camera) {
            isSaveImage = !isSaveImage;
            if (isSaveImage) {
                spot.setVisibility(View.VISIBLE);
                ToastUtils.toast(FaceDeptrumPersonActivity.this, "存图功能已开启再次点击可关闭");
            } else {
                spot.setVisibility(View.GONE);
            }
        }
    }

    private void judgeFirst() {
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
            }, 3000);
            editor.putBoolean("isIdentifyFirstSave", false);
            editor.commit();
        }
    }

    private void setFirstView(int visibility) {
        findViewById(R.id.first_text_tips).setVisibility(visibility);
        findViewById(R.id.first_circular_tips).setVisibility(visibility);
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
                if (model == null) {
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
                            score = model.getScore();

                            if (isDevelopment == false) {
                                layoutCompareStatus.setVisibility(View.GONE);
                                testimonyTipsFailRl.setVisibility(View.VISIBLE);
                                if (isFace == true) {
                                    testimonyTipsFailTv.setText("上传图片不包含人脸");
                                    testimonyTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                                    testimonyTipsPleaseFailTv.setText("无法进行人证比对");
                                    testimonyTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                } else {
                                    if (mLiveType == 0) {
                                        if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                            testimonyTipsFailTv.setText("人证核验通过");
                                            testimonyTipsFailTv.setTextColor(
                                                    Color.parseColor("#FF00BAF2"));
                                            testimonyTipsPleaseFailTv.setText("识别成功");
                                            testimonyTipsFailIv.setImageResource(R.mipmap.tips_success);
                                        } else {
                                            testimonyTipsFailTv.setText("人证核验未通过");
                                            testimonyTipsFailTv.setTextColor(
                                                    Color.parseColor("#FFFEC133"));
                                            testimonyTipsPleaseFailTv.setText("请上传正面人脸照片");
                                            testimonyTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                        }
                                    } else {
                                        // 活体阈值判断显示
                                        rgbLivenessScore = model.getRgbLivenessScore();
                                        if (rgbLivenessScore < mRgbLiveScore) {
                                            testimonyTipsFailTv.setText("人证核验未通过");
                                            testimonyTipsFailTv.setTextColor(
                                                    Color.parseColor("#FFFEC133"));
                                            testimonyTipsPleaseFailTv.setText("请上传正面人脸照片");
                                            testimonyTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                        } else {
                                            if (score > SingleBaseConfig.getBaseConfig()
                                                    .getIdThreshold()) {
                                                testimonyTipsFailTv.setText("人证核验通过");
                                                testimonyTipsFailTv.setTextColor(
                                                        Color.parseColor("#FF00BAF2"));
                                                testimonyTipsPleaseFailTv.setText("识别成功");
                                                testimonyTipsFailIv.setImageResource(
                                                        R.mipmap.tips_success);
                                            } else {
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
                        if (model.isQualityCheck()) {
                            tvFeatureTime.setText(String.format("特征抽取耗时：%s ms", 0));
                            tvFeatureSearchTime.setText(String.format("特征比对耗时：%s ms", 0));
                            long l = model.getRgbDetectDuration() + model.getRgbLivenessDuration();
                            tvAllTime.setText(String.format("总耗时：%s ms", l));
                            personKaifaIv.setVisibility(View.VISIBLE);
                            personKaifaIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                            textCompareStatus.setTextColor(Color.parseColor("#FECD33"));
                            /*textCompareStatus.setMaxEms(6)*/
                            ;
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


                                float rgbLivenessScore = model.getRgbLivenessScore();
                                if (rgbLivenessScore < mRgbLiveScore) {
                                        personKaifaIv.setVisibility(View.VISIBLE);
                                        personKaifaIv.setImageResource(R.mipmap.ic_icon_develop_fail);
                                } else {
                                        personKaifaIv.setVisibility(View.VISIBLE);
                                        personKaifaIv.setImageResource(R.mipmap.ic_icon_develop_success);
                                }
                                float nirLivenessScore = model.getIrLivenessScore();
                                float nirLiveScore = SingleBaseConfig.getBaseConfig().getNirLiveScore();
                                if (nirLivenessScore < nirLiveScore) {
                                        isNirCheckImage.setVisibility(View.VISIBLE);
                                        isNirCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                                } else {
                                        isNirCheckImage.setVisibility(View.VISIBLE);
                                        isNirCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                                }
                                float depthLivenessScore = model.getDepthLivenessScore();
                                float depthLiveScore = SingleBaseConfig.getBaseConfig().getDepthLiveScore();
                                if (depthLivenessScore < depthLiveScore) {
                                        isDepthCheckImage.setVisibility(View.VISIBLE);
                                        isDepthCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                                } else {
                                        isDepthCheckImage.setVisibility(View.VISIBLE);
                                        isDepthCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                                }
                                if (rgbLivenessScore < mRgbLiveScore ||
                                        nirLivenessScore < nirLiveScore ||
                                        depthLivenessScore < depthLiveScore) {
                                    textCompareStatus.setTextColor(Color.parseColor("#FECD33"));

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
                            FaceUtils.getInstance().getBDFaceCheckConfig(), this);
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

        mRgbSurface.onPause();
        mIrSurface.onPause();
        mDepSurface.onPause();
    }

    private boolean isPause = false;

    public void showFrame(final LivenessModel model) {
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
                        mRgbSurface, model.getBdFaceImageInstance());
                // 人脸框颜色
                if (score > FaceUtils.getInstance().getThreshold()) {
                    paint.setColor(Color.parseColor("#00baf2"));
                    paintBg.setColor(Color.parseColor("#00baf2"));
                } else {
                    paint.setColor(Color.parseColor("#FECD33"));
                    paintBg.setColor(Color.parseColor("#FECD33"));
                }
                // 绘制人脸框
                FaceOnDrawTexturViewUtil.drawRect(canvas,
                        rectF, paint, 5f, 50f, 25f);
                // 清空canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }
}