package com.baidu.idl.main.facesdk.attendancelibrary.attendance;

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
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.main.facesdk.attendancelibrary.R;
import com.baidu.idl.main.facesdk.attendancelibrary.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.attendancelibrary.setting.AttendanceSettingActivity;
import com.baidu.idl.main.facesdk.attendancelibrary.utils.FaceUtils;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
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
import com.example.datalibrary.utils.DensityUtils;
import com.example.datalibrary.utils.FaceOnDrawTexturViewUtil;
import com.example.datalibrary.utils.FileUtils;
import com.example.datalibrary.utils.TimeUtils;
import com.example.datalibrary.utils.ToastUtils;

import java.util.Date;


public class FaceRGBAttendanceActivity extends BaseActivity implements View.OnClickListener {

    /*图片越大，性能消耗越大，也可以选择640*480， 1280*720*/
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PERFER_HEIGH = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    private Context mContext;

    private TextureView mDrawDetectFaceView;
    private ImageView mFaceDetectImageView;
    private TextView mTvDetect;
    private TextView mTvLive;
    private TextView mTvLiveScore;
    private TextView mTvFeature;
    private TextView mTvAll;
    private TextView mTvAllTime;

    private RectF rectF;
    private Paint paint;
    private RelativeLayout relativeLayout;
    private int mLiveType;
    private float mRgbLiveScore;

    private boolean isCheck = false;
    private boolean isCompareCheck = false;
    private TextView preText;
    private TextView deveLop;
    private RelativeLayout preViewRelativeLayout;
    private RelativeLayout deveLopRelativeLayout;
    private RelativeLayout textHuanying;
    private TextView nameText;
    private RelativeLayout userNameLayout;
    private TextView detectSurfaceText;
    private ImageView isCheckImage;
    private TextView attendanceTime;
    private TextView attendanceDate;
    private TextView attendanceTimeText;
    private RelativeLayout outRelativelayout;
    private ImageView previewView;
    private ImageView developView;
    private TextView mNum;
    private Paint paintBg;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private User mUser;
    private View saveCamera;
    private boolean isSaveImage;
    private View spot;
    private ImageView nameImage;
    private GlMantleSurfacView glSurfaceView;
    private BDFaceImageConfig bdFaceImageConfig;
    private BDFaceCheckConfig bdFaceCheckConfig;
    private BDLiveConfig bdLiveConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        FaceSDKManager.getInstance().initDataBases(this);
        setContentView(R.layout.activity_face_rgb_attendancelibrary);
        initFaceCheck();
        initView();
        // 屏幕的宽
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // 屏幕的高
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // 当屏幕的宽大于屏幕宽时
        if (displayHeight < displayWidth) {
            // 获取高
            int height = displayHeight;
            // 获取宽
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // 设置布局的宽和高
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // 设置布局居中
            params.gravity = Gravity.CENTER;
            relativeLayout.setLayoutParams(params);
        }
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

    /**
     * View
     */
    private void initView() {
        // 获取整个布局
        relativeLayout = findViewById(R.id.all_relative);
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();
        mDrawDetectFaceView = findViewById(R.id.draw_detect_face_view);
        mDrawDetectFaceView.setOpaque(false);
        mDrawDetectFaceView.setKeepScreenOn(true);
        if (SingleBaseConfig.getBaseConfig().getRgbRevert()){
            mDrawDetectFaceView.setRotationY(180);
        }

        // 返回
        ImageView mButReturn = findViewById(R.id.btn_back);
        mButReturn.setOnClickListener(this);
        // 设置
        ImageView mBtSetting = findViewById(R.id.btn_setting);
        mBtSetting.setOnClickListener(this);
        // 预览模式
        preText = findViewById(R.id.preview_text);
        preText.setOnClickListener(this);
        preText.setTextColor(Color.parseColor("#ffffff"));
        preViewRelativeLayout = findViewById(R.id.yvlan_relativeLayout);
        previewView = findViewById(R.id.preview_view);

        // 开发模式
        deveLop = findViewById(R.id.develop_text);
        deveLop.setOnClickListener(this);
        deveLopRelativeLayout = findViewById(R.id.kaifa_relativeLayout);
        developView = findViewById(R.id.develop_view);
        developView.setVisibility(View.GONE);
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        layoutCompareStatus.setVisibility(View.GONE);
        textCompareStatus = findViewById(R.id.text_compare_status);
        nameImage = findViewById(R.id.detect_reg_image_item);

        // ***************开发模式*************
        isCheckImage = findViewById(R.id.is_check_image);
        // 活体状态
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
        // 活体阈值
        mRgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // 送检RGB 图像回显
        mFaceDetectImageView = findViewById(R.id.face_detect_image_view);
        mFaceDetectImageView.setVisibility(View.VISIBLE);
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
        // 存图按钮
        saveCamera = findViewById(R.id.save_camera);
        saveCamera.setOnClickListener(this);
        spot = findViewById(R.id.spot);


        // ***************预览模式*************
        textHuanying = findViewById(R.id.huanying_relative);
        userNameLayout = findViewById(R.id.user_name_layout);
        nameText = findViewById(R.id.name_text);
        detectSurfaceText = findViewById(R.id.detect_surface_text);
        mFaceDetectImageView.setVisibility(View.GONE);
        saveCamera.setVisibility(View.GONE);
        detectSurfaceText.setVisibility(View.GONE);
        attendanceTime = findViewById(R.id.attendance_time);
        attendanceDate = findViewById(R.id.attendance_date);
        attendanceTimeText = findViewById(R.id.attendance_time_text);
        outRelativelayout = findViewById(R.id.out_relativelayout);

        glSurfaceView = findViewById(R.id.camera_textureview);
        glSurfaceView.initSurface(SingleBaseConfig.getBaseConfig().getRgbRevert(),
                SingleBaseConfig.getBaseConfig().getMirrorVideoRGB() , SingleBaseConfig.getBaseConfig().isOpenGl());
        CameraPreviewManager.getInstance().startPreview(/*mContext, */glSurfaceView,
                SingleBaseConfig.getBaseConfig().getRgbVideoDirection() , PREFER_WIDTH, PERFER_HEIGH);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startTestOpenDebugRegisterFunction();
    }

    private void startTestOpenDebugRegisterFunction() {
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
                glSurfaceView.setFrame();

                bdFaceImageConfig.setData(data);
                FaceSDKManager.getInstance().onDetectCheck(bdFaceImageConfig, null, null,
                        bdFaceCheckConfig, new FaceDetectCallBack() {
                            @Override
                            public void onFaceDetectCallback(LivenessModel livenessModel) {
                                // 预览模式
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
                                // 绘制人脸框
                                showFrame(livenessModel);

                            }
                        });


            }
        });
    }

    // ***************预览模式结果输出*************
    private void checkCloseDebugResult(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                attendanceTime.setText(TimeUtils.getTimeShort(date));
                attendanceDate.setText(TimeUtils.getStringDateShort(date) + " "
                        + TimeUtils.getWeek(date));
                if (livenessModel == null) {
                    textHuanying.setVisibility(View.VISIBLE);
                    userNameLayout.setVisibility(View.GONE);
                    return;
                }
                User user = livenessModel.getUser();
                if (user == null) {
                    mUser = null;
                    if (livenessModel.isMultiFrame()) {
                        textHuanying.setVisibility(View.GONE);
                        userNameLayout.setVisibility(View.VISIBLE);
                        nameImage.setImageResource(R.mipmap.ic_tips_gate_fail);
                        nameText.setTextColor(Color.parseColor("#fec133"));
                        nameText.setText("考勤失败");
                        attendanceTimeText.setText("持续识别中......");
                    } else {
                        textHuanying.setVisibility(View.VISIBLE);
                        userNameLayout.setVisibility(View.GONE);
                    }
                } else {
                    mUser = user;
                    textHuanying.setVisibility(View.GONE);
                    userNameLayout.setVisibility(View.VISIBLE);
                    String absolutePath = FileUtils.getBatchImportSuccessDirectory()
                            + "/" + user.getImageName();
                    Bitmap bitmap = BitmapFactory.decodeFile(absolutePath);
                    nameImage.setImageBitmap(bitmap);
                    nameText.setTextColor(Color.parseColor("#00BAF2"));
                    nameText.setText(FileUtils.spotString(user.getUserName()) + " 考勤成功");
                    attendanceTimeText.setText("考勤时间：" + TimeUtils.getTimeShort(date));
                }

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
                    isCheckImage.setVisibility(View.GONE);
                    mFaceDetectImageView.setImageResource(R.mipmap.ic_image_video);
                    mTvDetect.setText(String.format("检测耗时 ：%s ms", 0));
                    mTvLive.setText(String.format("RGB活体检测耗时 ：%s ms", 0));
                    mTvLiveScore.setText(String.format("RGB活体得分 ：%s", 0));
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
                if (livenessModel.isQualityCheck()){
                    if (isCheck) {
                        isCheckImage.setVisibility(View.VISIBLE);
                        isCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                    }
                    if (isCompareCheck) {
                        layoutCompareStatus.setVisibility(View.VISIBLE);
                        textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                        textCompareStatus.setMaxEms(6);
                        textCompareStatus.setText("请正视摄像头");
                    }
                } else if (mLiveType == 0) {
                    User user = livenessModel.getUser();
                    if (user == null) {
                        mUser = null;
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
                            textCompareStatus.setText("识别未通过");
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

                } else {
                    float rgbLivenessScore = livenessModel.getRgbLivenessScore();
                    if (rgbLivenessScore < mRgbLiveScore) {
                        if (isCheck) {
                            isCheckImage.setVisibility(View.VISIBLE);
                            isCheckImage.setImageResource(R.mipmap.ic_icon_develop_fail);
                        }
                        if (isCompareCheck) {
                            layoutCompareStatus.setVisibility(View.VISIBLE);
                            textCompareStatus.setTextColor(Color.parseColor("#FFFEC133"));
//                            textCompareStatus.setMaxEms(10);

//                            textCompareStatus.setMaxEms(7);
                            textCompareStatus.setText("活体检测未通过");
                        }
                    } else {
                        if (isCheck) {
                            isCheckImage.setVisibility(View.VISIBLE);
                            isCheckImage.setImageResource(R.mipmap.ic_icon_develop_success);
                        }

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
//                                textCompareStatus.setMaxEms(5);
                                textCompareStatus.setText(FileUtils.spotString(mUser.getUserName()));
                            }
                        }
                    }
                }
                mTvDetect.setText(String.format("检测耗时 ：%s ms", livenessModel.getRgbDetectDuration()));
                mTvLive.setText(String.format("RGB活体检测耗时 ：%s ms", livenessModel.getRgbLivenessDuration()));
                mTvLiveScore.setText(String.format("RGB活体得分 ：%s", livenessModel.getRgbLivenessScore()));
                mTvFeature.setText(String.format("特征抽取耗时 ：%s ms", livenessModel.getFeatureDuration()));
                mTvAll.setText(String.format("特征比对耗时 ：%s ms", livenessModel.getCheckDuration()));
                mTvAllTime.setText(String.format("总耗时 ：%s ms", livenessModel.getAllDetectDuration()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
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
            startActivity(new Intent(mContext, AttendanceSettingActivity.class));
            finish();
        } else if (id == R.id.preview_text) {
            isCheckImage.setVisibility(View.GONE);
            mFaceDetectImageView.setVisibility(View.GONE);
            saveCamera.setVisibility(View.GONE);
            detectSurfaceText.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            developView.setVisibility(View.GONE);
            layoutCompareStatus.setVisibility(View.GONE);
            deveLop.setTextColor(Color.parseColor("#a9a9a9"));
            preText.setTextColor(Color.parseColor("#ffffff"));
            preViewRelativeLayout.setVisibility(View.VISIBLE);
            deveLopRelativeLayout.setVisibility(View.GONE);
            outRelativelayout.setVisibility(View.VISIBLE);
            isCheck = false;
            isCompareCheck = false;
            isSaveImage = false;
            spot.setVisibility(View.GONE);
        } else if (id == R.id.develop_text) {
            isCheck = true;
            isCompareCheck = true;
            isCheckImage.setVisibility(View.VISIBLE);
            mFaceDetectImageView.setVisibility(View.VISIBLE);
            saveCamera.setVisibility(View.VISIBLE);
            detectSurfaceText.setVisibility(View.VISIBLE);
            previewView.setVisibility(View.GONE);
            developView.setVisibility(View.VISIBLE);
            deveLop.setTextColor(Color.parseColor("#ffffff"));
            preText.setTextColor(Color.parseColor("#a9a9a9"));
            deveLopRelativeLayout.setVisibility(View.VISIBLE);
            preViewRelativeLayout.setVisibility(View.GONE);
            outRelativelayout.setVisibility(View.GONE);
            judgeFirst();
        } else if (id == R.id.save_camera){
            isSaveImage = !isSaveImage;
            if (isSaveImage){
                ToastUtils.toast(FaceRGBAttendanceActivity.this, "存图功能已开启再次点击可关闭");
                spot.setVisibility(View.VISIBLE);
            }else {
                spot.setVisibility(View.GONE);
            }
        }
    }

    private void judgeFirst(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isAttendanceFirstSave", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            setFirstView(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFirstView(View.GONE);
                }
            },3000);
            editor.putBoolean("isAttendanceFirstSave", false);
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
            glSurfaceView.onGlDraw(livenessModel.getTrackFaceInfo() ,
                    livenessModel.getBdFaceImageInstance() ,
                    FaceOnDrawTexturViewUtil.drawFaceColor(mUser , livenessModel));
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        isPause = false;
        CameraPreviewManager.getInstance().stopPreview();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
