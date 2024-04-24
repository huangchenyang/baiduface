package com.baidu.idl.main.facesdk.registerlibrary.user.register;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.registerlibrary.R;
import com.baidu.idl.main.facesdk.registerlibrary.user.model.SingleBaseConfig;
import com.baidu.idl.main.facesdk.registerlibrary.user.utils.FaceUtils;
import com.example.datalibrary.api.FaceApi;
import com.example.datalibrary.callback.FaceDetectCallBack;
import com.example.datalibrary.gatecamera.CameraPreviewManager;
import com.example.datalibrary.lim.BaseLimActivity;
import com.example.datalibrary.lim.GLPanel;
import com.example.datalibrary.lim.MantleGLPanel;
import com.example.datalibrary.lim.SimpleViewer;
import com.example.datalibrary.listener.SdkInitListener;
import com.example.datalibrary.manager.FaceSDKManager;
import com.example.datalibrary.model.BDFaceCheckConfig;
import com.example.datalibrary.model.BDFaceImageConfig;
import com.example.datalibrary.model.BDLiveConfig;
import com.example.datalibrary.model.KeyboardsUtils;
import com.example.datalibrary.model.LivenessModel;
import com.example.datalibrary.model.User;
import com.example.datalibrary.utils.BitmapUtils;
import com.example.datalibrary.utils.DensityUtils;
import com.example.datalibrary.utils.FaceOnDrawTexturViewUtil;
import com.example.datalibrary.utils.FileUtils;
import com.example.datalibrary.utils.ToastUtils;
import com.example.datalibrary.view.CircleImageView;
import com.example.datalibrary.view.FaceRoundProView;
import com.hjimi.api.iminect.ImiDevice;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * 新人脸注册页面
 * Created by v_liujialu01 on 2020/02/19.
 */
public class FaceRegisterLimActivity extends BaseLimActivity implements View.OnClickListener {
    private static final String TAG = FaceRegisterLimActivity.class.getSimpleName();
    private Context mContext;

    /*RGB摄像头图像宽和高*/
    private static final int RGB_WIDTH = 480;
    private static final int RGB_HEIGHT = 640;

    private FaceRoundProView mFaceRoundProView;
    private RelativeLayout mRelativePreview;     // 预览相关布局

    // 采集相关布局
    private RelativeLayout mRelativeCollectSuccess;
    private CircleImageView mCircleHead;
    private EditText mEditName;
    private TextView mTextError;
    private Button mBtnCollectConfirm;
    private ImageView mImageInputClear;

    // 注册成功相关布局
    private RelativeLayout mRelativeRegisterSuccess;
    private CircleImageView mCircleRegSucHead;

    // 包含适配屏幕后的人脸的x坐标，y坐标，和width
    private float[] mPointXY = new float[4];
    private byte[] mFeatures = new byte[512];
    private Bitmap mCropBitmap;
    private boolean mCollectSuccess = false;

    private BDFaceImageConfig bdFaceImageConfig;
    private BDFaceImageConfig bdDepthFaceImageConfig;
    private BDFaceCheckConfig bdFaceCheckConfig;
    private BDLiveConfig bdLiveConfig;
    // 华捷摄像头
    private MantleGLPanel hjcolorView;
    private GLPanel hjdepthView;
    private boolean mIsExitLoop = true;
    private boolean isFaceRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initListener();
        setContentView(R.layout.activity_new_lim_registerlibrary);
        initFaceConfig(RGB_HEIGHT , RGB_WIDTH);
        initDepthFaceConfig(RGB_HEIGHT , RGB_WIDTH);
        initFaceCheck();
        initView();
        // 摄像头图像预览
        openLim();
//            huajiemDevice.addDeviceStateListener(mainlistener);
        hjcolorView.setVisibility(View.VISIBLE);
    }
    private void initFaceConfig(int height , int width){
        bdFaceImageConfig = new BDFaceImageConfig(height , width ,
                SingleBaseConfig.getBaseConfig().getRgbDetectDirection(),
                SingleBaseConfig.getBaseConfig().getMirrorDetectRGB() ,
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

    private void initView() {
        // 华捷彩色图像预览
        hjcolorView = findViewById(R.id.sv_color_view);

        // 华捷深度图像预览
        hjdepthView = findViewById(R.id.sv_depth_view);
        mFaceRoundProView = findViewById(R.id.round_view);
        mRelativePreview = findViewById(R.id.relative_preview);

        mRelativeCollectSuccess = findViewById(R.id.relative_collect_success);
        mCircleHead = findViewById(R.id.circle_head);
        mCircleHead.setBorderWidth(DensityUtils.dip2px(FaceRegisterLimActivity.this, 3));
        mCircleHead.setBorderColor(Color.parseColor("#0D9EFF"));
        mEditName = findViewById(R.id.edit_name);
        mTextError = findViewById(R.id.text_error);
        mBtnCollectConfirm = findViewById(R.id.btn_collect_confirm);
        mBtnCollectConfirm.setOnClickListener(this);
        mImageInputClear = findViewById(R.id.image_input_delete);
        mImageInputClear.setOnClickListener(this);

        mRelativeRegisterSuccess = findViewById(R.id.relative_register_success);
        mCircleRegSucHead = findViewById(R.id.circle_reg_suc_head);
        findViewById(R.id.btn_return_home).setOnClickListener(this);
        findViewById(R.id.btn_continue_reg).setOnClickListener(this);

        ImageView imageBack = findViewById(R.id.image_register_back);
        imageBack.setOnClickListener(this);

        // 输入框监听事件
        mEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mImageInputClear.setVisibility(View.VISIBLE);
                    mBtnCollectConfirm.setEnabled(true);
                    mBtnCollectConfirm.setTextColor(Color.WHITE);
                    mBtnCollectConfirm.setBackgroundResource(R.drawable.button_selector);
                    List<User> listUsers = FaceApi.getInstance().getUserListByUserName(s.toString());
                    if (listUsers != null && listUsers.size() > 0) {     // 出现用户名重复
                        mTextError.setVisibility(View.VISIBLE);
                        mBtnCollectConfirm.setEnabled(false);
                    } else {
                        mTextError.setVisibility(View.INVISIBLE);
                        mBtnCollectConfirm.setEnabled(true);
                    }
                } else {
                    mImageInputClear.setVisibility(View.GONE);
                    mBtnCollectConfirm.setEnabled(false);
                    mBtnCollectConfirm.setTextColor(Color.parseColor("#666666"));
                    mBtnCollectConfirm.setBackgroundResource(R.mipmap.btn_all_d);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        hjcolorView.initSurface();
        hjcolorView.setDraw(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void showViewer(SimpleViewer mColorViewer , SimpleViewer mDepthViewer) {

        hjcolorView.setVisibility(View.VISIBLE);
        mColorViewer.setGLPanel(hjcolorView.getGLPanel());

//        hjdepthView.setVisibility(View.VISIBLE);
        mDepthViewer.setGLPanel(hjdepthView);

        mColorViewer.onStart();
        mDepthViewer.onStart();

        Log.d("test_camera", "showViewerOk");
        depthImiDate();

    }
    /**
     * 获取华捷艾米RGB、Depth 视频帧数据
     */
    private void depthImiDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    ImiDevice.ImiFrame colorFrame = null;
                    ImiDevice.ImiFrame depthFrame = null;
                    while (mIsExitLoop) {
                        if (!isFaceRegister){
                            Thread.sleep(10);
                            // 获取rgb数据帧
                            if (colorFrame == null) {
                                if (huajiemDevice != null && !isFaceRegister && !destroyFlag &&  !huajiemDevice.isOpenning() ){
                                    colorFrame = huajiemDevice.readNextFrame(ImiDevice.ImiStreamType.COLOR, 100);
                                }
                            }

                            // 获取depth数据帧
                            if (null == depthFrame) {

                                if (huajiemDevice != null  && !isFaceRegister && !destroyFlag && !huajiemDevice.isOpenning()){
                                    depthFrame = huajiemDevice.readNextFrame(ImiDevice.ImiStreamType.DEPTH, 100);
                                }
                            }

                            // color and depth sync， find the right delta value
                            if (colorFrame != null && depthFrame != null) {
                                long ut = colorFrame.getTimeStamp() / 1000;
                                long dt = depthFrame.getTimeStamp() / 1000;

                                long delta = (dt - ut);

                                if (delta < -25) {
                                    depthFrame = null;
                                } else if (delta > 25) {
                                    colorFrame = null;
                                }

                            }

                            if (null == depthFrame || null == colorFrame) {
                                continue;
                            }

                            ByteBuffer mColorBuffer = colorFrame.getData();
                            ByteBuffer mDepthBuffer = depthFrame.getData();

                            if (mColorBuffer != null && mDepthBuffer != null) {
                                // 将rgb、depth ByteBuffer 转换为byte[]
                                Log.e("test_camera", "rgbValue");
                                byte[] rgbData = decodeValue(mColorBuffer);
                                dealRgb(rgbData);
                                Log.e("test_camera", "DepthValue");
                                byte[] depthDate = decodeValue(mDepthBuffer);
                                dealDepth(depthDate);
                            }
                            colorFrame = null;
                            depthFrame = null;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();

    }
    private void dealDepth(byte[] data) {
        bdDepthFaceImageConfig.setData(data);
        checkData();
    }

    private void dealRgb(byte[] data) {
        bdFaceImageConfig.setData(data);
        checkData();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    /**
     * 摄像头数据处理
     */
    private void checkData() {

        if (bdFaceImageConfig.data != null && bdDepthFaceImageConfig.data != null) {
            FaceSDKManager.getInstance().onDetectCheck(bdFaceImageConfig, null, bdDepthFaceImageConfig,
                    bdFaceCheckConfig, new FaceDetectCallBack() {
                        @Override
                        public void onFaceDetectCallback(LivenessModel livenessModel) {
                            // 输出结果
                            if (!isFaceRegister) {
                                checkFaceBound(livenessModel);
                            }
                        }

                        @Override
                        public void onTip(final int code, String msg) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mFaceRoundProView == null) {
                                        return;
                                    }
                                    if (code == 0) {
                                        mFaceRoundProView.setTipText("请保持面部在取景框内");
                                        mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey, false);
                                    } else {
                                        mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
                                        mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue, true);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                        }
                    });
        }
    }
    /**
     * 检查人脸边界
     *
     * @param livenessModel LivenessModel实体
     */
    private void checkFaceBound(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCollectSuccess) {
                    return;
                }

                if (livenessModel == null || livenessModel.getFaceInfo() == null) {
                    mFaceRoundProView.setTipText("请保持面部在取景框内");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey , false);
                    return;
                }
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey , false);

                if (livenessModel.getFaceSize() > 1){
                    mFaceRoundProView.setTipText("请保证取景框内只有一个人脸");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                    return;
                }


                mPointXY[0] = livenessModel.getFaceInfo().centerX;   // 人脸X坐标
                mPointXY[1] = livenessModel.getFaceInfo().centerY;   // 人脸Y坐标
                mPointXY[2] = livenessModel.getFaceInfo().width;     // 人脸宽度
                mPointXY[3] = livenessModel.getFaceInfo().height;    // 人脸高度

                FaceOnDrawTexturViewUtil.converttPointXY(mPointXY, hjcolorView,
                        livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);

                float leftLimitX = hjcolorView.circleX - hjcolorView.circleRadius;
                float rightLimitX = hjcolorView.circleX + hjcolorView.circleRadius;
                float topLimitY = hjcolorView.circleY - hjcolorView.circleRadius;
                float bottomLimitY = hjcolorView.circleY + hjcolorView.circleRadius;
                float previewWidth = hjcolorView.circleRadius * 2;

//                Log.e(TAG, "faceX = " + mPointXY[0] + ", faceY = " + mPointXY[1]
//                        + ", faceW = " + mPointXY[2] + ", prw = " + previewWidth);
//                Log.e(TAG, "leftLimitX = " + leftLimitX + ", rightLimitX = " + rightLimitX
//                        + ", topLimitY = " + topLimitY + ", bottomLimitY = " + bottomLimitY);
//                Log.e(TAG, "cX = " + AutoTexturePreviewView.circleX + ", cY = " + AutoTexturePreviewView.circleY
//                        + ", cR = " + AutoTexturePreviewView.circleRadius);

                if (mPointXY[2] < 50 || mPointXY[3] < 50) {
                    mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                    // 释放内存
                    return;
                }

                if (mPointXY[2] > previewWidth || mPointXY[3] > previewWidth) {
                    mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                    // 释放内存
                    return;
                }

                if (mPointXY[0] - mPointXY[2] / 2 < leftLimitX
                        || mPointXY[0] + mPointXY[2] / 2 > rightLimitX
                        || mPointXY[1] - mPointXY[3] / 2 < topLimitY
                        || mPointXY[1] + mPointXY[3] / 2 > bottomLimitY) {
                    mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                    // 释放内存
                    return;
                }

//                if ((Math.abs(AutoTexturePreviewView.circleX - mPointXY[0]) < mPointXY[2] / 2)
//                        && (Math.abs(AutoTexturePreviewView.circleY - mPointXY[1]) < mPointXY[2] / 2)
//                        && (mPointXY[2] <= previewWidth && mPointXY[3] <= previewWidth)) {
//
//                }
                mFaceRoundProView.setTipText("请保持面部在取景框内");
                // 检验活体分值
                checkLiveScore(livenessModel);
            }
        });
    }

    /**
     * 检验活体分值
     *
     * @param livenessModel LivenessModel实体
     */
    private void checkLiveScore(LivenessModel livenessModel) {
        if (livenessModel == null || livenessModel.getFaceInfo() == null) {
            mFaceRoundProView.setTipText("请保持面部在取景框内");
            return;
        }

        // 获取活体类型
        int liveType = SingleBaseConfig.getBaseConfig().getType();
        // int liveType = 2;

        if (livenessModel.isQualityCheck()){
            mFaceRoundProView.setTipText("请保证人脸区域清晰无遮挡");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
            return;
        } else if (bdLiveConfig == null) {         // 无活体
            getFeatures(livenessModel);
        } else { // RGB活体检测
            float rgbLivenessScore = livenessModel.getRgbLivenessScore();
            float liveThreadHold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
            // Log.e(TAG, "score = " + rgbLivenessScore);
            if (rgbLivenessScore < liveThreadHold) {
                mFaceRoundProView.setTipText("请保证采集对象为真人");
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                return;
            }
            // 提取特征值
            getFeatures(livenessModel);
        }
    }

    /**
     * 提取特征值
     *
     * @param model 人脸数据
     */
    private void getFeatures(final LivenessModel model) {
        if (model == null) {
            return;
        }
        ;
        displayCompareResult(model.getFeatureCode(), model.getFeature(), model);

    }

    // 根据特征抽取的结果 注册人脸
    private void displayCompareResult(float ret, byte[] faceFeature, LivenessModel model) {
        if (model == null) {
            mFaceRoundProView.setTipText("请保持面部在取景框内");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_grey , false);
            return;
        }

        // 特征提取成功
        if (ret == 128) {
            // 抠图
            BDFaceImageInstance cropInstance =
                    FaceSDKManager.getInstance().getCopeFace(
                           BitmapUtils.getInstaceBmp(model.getBdFaceImageInstance()) ,
                            model.getLandmarks(),
                            0
                    ); ;
            if (cropInstance == null) {
                mFaceRoundProView.setTipText("抠图失败");
                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
                return;
            }
            mCropBitmap = BitmapUtils.getInstaceBmp(cropInstance);
            // 获取头像
            if (mCropBitmap != null) {
                mCollectSuccess = true;
                mCircleHead.setImageBitmap(mCropBitmap);
            }
            cropInstance.destory();

            mRelativeCollectSuccess.setVisibility(View.VISIBLE);
            mRelativePreview.setVisibility(View.GONE);
            mFaceRoundProView.setTipText("");

            for (int i = 0; i < faceFeature.length; i++) {
                mFeatures[i] = faceFeature[i];
            }
            isFaceRegister = true;
        } else {
            mFaceRoundProView.setTipText("特征提取失败");
            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue , true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsExitLoop = false;
        if (mCropBitmap != null && !mCropBitmap.isRecycled()){
            mCropBitmap.recycle();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.image_register_back) {    // 返回
            finish();

        } else if (id == R.id.btn_collect_confirm) {   // 用户名注册
            String userName = mEditName.getText().toString();
//                if (TextUtils.isEmpty(userName)) {
//                    ToastUtils.toast(getApplicationContext(), "请先输入用户名");
//                    return;
//                }
//                if (userName.length() > 10) {
//                    ToastUtils.toast(getApplicationContext(), "用户名长度不得大于10位");
//                    return;
//                }
            // 姓名过滤
            String nameResult = FaceApi.getInstance().isValidName(userName);
            if (!"0".equals(nameResult)) {
                ToastUtils.toast(getApplicationContext(), nameResult);
                return;
            }
            String imageName = userName + ".jpg";
            // 注册到人脸库
            boolean isSuccess = FaceApi.getInstance().registerUserIntoDBmanager(null,
                    userName, imageName, null, mFeatures);
            if (isSuccess) {
                // 保存人脸图片
                File faceDir = FileUtils.getBatchImportSuccessDirectory();
                File file = new File(faceDir, imageName);
                FileUtils.saveBitmap(file, mCropBitmap);
                // 数据变化，更新内存
//                FaceSDKManager.getInstance().initDatabases();
                // 更新UI
                mRelativeCollectSuccess.setVisibility(View.GONE);
                mRelativeRegisterSuccess.setVisibility(View.VISIBLE);
                mCircleRegSucHead.setImageBitmap(mCropBitmap);
            } else {
                ToastUtils.toast(getApplicationContext(), "保存数据库失败，" +
                        "可能是用户名格式不正确");
            }
            if (mCropBitmap != null && !mCropBitmap.isRecycled()){
                mCropBitmap.recycle();
            }
        } else if (id == R.id.btn_continue_reg) {      // 继续注册
            if (mRelativeRegisterSuccess.getVisibility() == View.VISIBLE) {
                mRelativeRegisterSuccess.setVisibility(View.GONE);
            }
            mRelativePreview.setVisibility(View.VISIBLE);
            mCollectSuccess = false;
            mFaceRoundProView.setTipText("");
            mEditName.setText("");
            isFaceRegister = false;
            if (mCropBitmap != null && !mCropBitmap.isRecycled()){
                mCropBitmap.recycle();
            }
        } else if (id == R.id.btn_return_home) {       // 回到首页
            // 关闭摄像头
            CameraPreviewManager.getInstance().stopPreview();
            finish();
        } else if (id == R.id.image_input_delete) {   // 清除输入
            mEditName.setText("");
            mTextError.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 点击非编辑区域收起键盘
     * 获取点击事件
     */
    @CallSuper
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (KeyboardsUtils.isShouldHideKeyBord(view, ev)) {
                KeyboardsUtils.hintKeyBoards(view);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
