package com.example.settinglibrary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.datalibrary.activity.BaseActivity;
import com.example.datalibrary.gatecamera.AutoTexturePreviewView;
import com.example.datalibrary.gatecamera.CameraPreviewManager;
import com.example.datalibrary.view.PreviewTexture;


/**
 * author : gengzhi
 * date : 2021/6/11 8:15
 * description :摄像头视频流回显角度
 */
public class CameraDisplayAngleActivity extends BaseActivity {
    private static final int PREFER_WIDTH = 640;
    private static final int PREFER_HEIGHT = 480;
    // RGB+IR 控件
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;

    private AutoTexturePreviewView rgbFaceView;
    private View rgbRotate;
    private ImageView rgbRotateImg;
    private View rgbMirror;
    private TextView rgbMirrorTx;
    private ImageView rgbMirrorImg;
    private View rgbGroup;
    private View rgbFaceGroup;

    private AutoTexturePreviewView nirFaceView;
    private View nirRotate;
    private ImageView nirRotateImg;
    private View nirMirror;
    private TextView nirMirrorTx;
    private ImageView nirMirrorImg;
    private View nirFaceGroup;
    private View nirGroup;
    int rgbVideoDirection;
    int mirrorVideoRGB;
    int nirVideoDirection;
    int mirrorVideoNIR;
    // 摄像头个数
    private int mCameraNum;
    private int rbgCameraId = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_cameradisplayangle);
        initMode();
    }
    private void initMode(){

        // 获取Intent对象
        Intent intent = getIntent();
        // 获取传递的值
        rgbVideoDirection = intent.getIntExtra("rgbVideoDirection" , 0);
        mirrorVideoRGB = intent.getIntExtra("mirrorVideoRGB" , 0);
        nirVideoDirection = intent.getIntExtra("nirVideoDirection" , 0);
        mirrorVideoNIR = intent.getIntExtra("mirrorVideoNIR" , 0);
        rbgCameraId = intent.getIntExtra("rbgCameraId" , -1);
        // rgb view
        rgbFaceView = findViewById(R.id.rbg_face_view);
        rgbRotate = findViewById(R.id.rgb_rotate);
        rgbRotateImg = findViewById(R.id.rgb_rotate_image);
        rgbMirror = findViewById(R.id.rgb_mirror);
        rgbMirrorTx = findViewById(R.id.rgb_mirror_tx);
        rgbMirrorImg = findViewById(R.id.rgb_mirror_image);
        rgbGroup = findViewById(R.id.rgb_group);
        rgbFaceGroup = findViewById(R.id.rbg_face_group);
        // nir view
        nirFaceView = findViewById(R.id.nir_face_view);
        nirRotate = findViewById(R.id.nir_rotate);
        nirRotateImg = findViewById(R.id.nir_rotate_image);
        nirMirror = findViewById(R.id.nir_mirror);
        nirMirrorTx = findViewById(R.id.nir_mirror_tx);
        nirMirrorImg = findViewById(R.id.nir_mirror_image);
        nirFaceGroup = findViewById(R.id.nir_face_group);
        nirGroup = findViewById(R.id.nir_group);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init(){
        mCameraNum = Camera.getNumberOfCameras();
        mPreview = new PreviewTexture[mCameraNum];
        mCamera = new Camera[mCameraNum];
        // 打开摄像头
        if (mCameraNum < 2) {
            setNirView(0.3f , 0 , View.GONE , R.mipmap.texture_default);
            openCan(0, 0, rgbFaceView.textureView);
            setDisplayOrientation(mCamera[0] , rgbVideoDirection , rgbFaceView);
            setRotationY(rgbVideoDirection, mirrorVideoRGB, rgbFaceView);
        } else {
            int rbg = CameraPreviewManager.CAMERA_USB;
            int nir = 1;
            if (rbgCameraId != -1){
                rbg = rbgCameraId;
                nir = Math.abs(rbgCameraId - 1);
            }
            setNirView(1 , 1 , View.VISIBLE , R.drawable.sr_texture_rectangle);
            boolean rbgCameraReady = openCan(0, rbg , rgbFaceView.textureView);
            boolean nirCameraReady = openCan(1, nir , nirFaceView.textureView);
            if (rbgCameraReady){
                setDisplayOrientation(mCamera[0] , rgbVideoDirection , rgbFaceView);
                setRotationY(rgbVideoDirection, mirrorVideoRGB, rgbFaceView);
            }else {
                setRgbView(0.3f , 0 , R.mipmap.texture_default);
            }
            if (nirCameraReady){
                setDisplayOrientation(mCamera[1] , nirVideoDirection , nirFaceView);
                setRotationY(nirVideoDirection, mirrorVideoNIR, nirFaceView);
            }else {
                setNirView(0.3f , 0 , View.GONE , R.mipmap.texture_default);
            }
        }
        setRgbRotate();
        setRbgMirror();
        setNirRotate();
        setNirMirror();
        // RGB旋转
        rgbRotate.setOnClickListener(onClickListener);
        // rgb镜像
        rgbMirror.setOnClickListener(onClickListener);
        // nir旋转
        nirRotate.setOnClickListener(onClickListener);
        // nir镜像
        nirMirror.setOnClickListener(onClickListener);

        ImageView cdaSave = findViewById(R.id.cda_save);
        cdaSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("rgbVideoDirection", rgbVideoDirection);
        intent.putExtra("mirrorVideoRGB", mirrorVideoRGB);
        intent.putExtra("nirVideoDirection", nirVideoDirection);
        intent.putExtra("mirrorVideoNIR", mirrorVideoNIR);
        intent.putExtra("rbgCameraId", rbgCameraId);
        // 设置返回码和返回携带的数据
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    private void setNirView(float alpha , float faceViewAlpha , int visibility , int bgResid){
        View nirTx = findViewById(R.id.nir_tx);
        nirGroup.setAlpha(alpha);
        nirFaceView.setAlpha(faceViewAlpha);
        nirTx.setVisibility(visibility);
        nirFaceGroup.setBackgroundResource(bgResid);
    }

    private void setRgbView(float alpha , float faceViewAlpha , int bgResid){
        rgbGroup.setAlpha(alpha);
        rgbFaceGroup.setAlpha(faceViewAlpha);
        rgbFaceGroup.setBackgroundResource(bgResid);
    }

    private boolean openCan(int displayIndex, int index, TextureView faceView){
        try {
            mCamera[displayIndex] = Camera.open(index);
            mPreview[displayIndex] = new PreviewTexture(this, faceView);
            mPreview[displayIndex].setCamera(mCamera[displayIndex], PREFER_WIDTH, PREFER_HEIGHT);
            return true;
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
        return false;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.rgb_rotate){
                setVideoDirection(0);
                setRgbRotate();
            }else if (id == R.id.rgb_mirror){
                setMirror(0);
                setRbgMirror();
            }else if (id == R.id.nir_rotate){
                setVideoDirection(1);
                setNirRotate();
            }else if (id == R.id.nir_mirror){
                setMirror(1);
                setNirMirror();
            }
        }
    };
    private void setRgbRotate(){
        if (rgbVideoDirection == 0){
            rgbRotateImg.setImageResource(R.mipmap.rotate_0);
        }else if (rgbVideoDirection == 90){
            rgbRotateImg.setImageResource(R.mipmap.rotate_90);
        }else if (rgbVideoDirection == 180){
            rgbRotateImg.setImageResource(R.mipmap.rotate_180);
        }else if (rgbVideoDirection == 270){
            rgbRotateImg.setImageResource(R.mipmap.rotate_270);
        }
    }

    private void setRbgMirror(){
        if (mirrorVideoRGB == 0){
            rgbMirrorImg.setImageResource(R.mipmap.mirror_close);
            rgbMirrorTx.setTextColor(Color.parseColor("#ffffff"));
        }else {
            rgbMirrorImg.setImageResource(R.mipmap.mirror_oppen);
            rgbMirrorTx.setTextColor(Color.parseColor("#00BAF2"));
        }
    }
    private void setNirMirror(){
        if (mirrorVideoNIR == 0){
            nirMirrorImg.setImageResource(R.mipmap.mirror_close);
            nirMirrorTx.setTextColor(Color.parseColor("#ffffff"));
        }else {
            nirMirrorImg.setImageResource(R.mipmap.mirror_oppen);
            nirMirrorTx.setTextColor(Color.parseColor("#00BAF2"));
        }
    }
    private void setNirRotate(){
        if (nirVideoDirection == 0){
            nirRotateImg.setImageResource(R.mipmap.rotate_0);
        }else if (nirVideoDirection == 90){
            nirRotateImg.setImageResource(R.mipmap.rotate_90);
        }else if (nirVideoDirection == 180){
            nirRotateImg.setImageResource(R.mipmap.rotate_180);
        }else if (nirVideoDirection == 270){
            nirRotateImg.setImageResource(R.mipmap.rotate_270);
        }
    }
    // 旋转角度
    private void setVideoDirection(int index){
        if (mCamera == null || mCamera[index] == null) {
            return;
        }
        if (index == 0){
            rgbVideoDirection += 90;
            if (rgbVideoDirection > 270) {
                rgbVideoDirection = 0;
            }
            setDisplayOrientation(mCamera[index], rgbVideoDirection, rgbFaceView);
        }else {
            nirVideoDirection += 90;
            if (nirVideoDirection > 270) {
                nirVideoDirection = 0;
            }
            setDisplayOrientation(mCamera[index], nirVideoDirection, nirFaceView);
        }
    }
    public void setDisplayOrientation(Camera camera, int videoDirection, AutoTexturePreviewView mTextureView){
        camera.setDisplayOrientation(videoDirection);
        ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
        int h = layoutParams.height;
        int w = layoutParams.width;
        if (videoDirection == 90 || videoDirection == 270) {
            if (h > w){

                layoutParams.height = w;
                layoutParams.width = h;
            }
            // 旋转90度或者270，需要调整宽高
            mTextureView.setPreviewSize(PREFER_HEIGHT, PREFER_WIDTH);
        } else {
            if (w > h){

                layoutParams.height = w;
                layoutParams.width = h;
            }
            mTextureView.setPreviewSize(PREFER_WIDTH, PREFER_HEIGHT);
        }
        mTextureView.setLayoutParams(layoutParams);
    }
    // 旋转角度
    private void setMirror(int index){
        if (mCamera == null || mCamera[index] == null) {
            return;
        }
        if (index == 0){
            mirrorVideoRGB = Math.abs(1 - mirrorVideoRGB);
            setRotationY(rgbVideoDirection, mirrorVideoRGB, rgbFaceView);
        }else {
            mirrorVideoNIR = Math.abs(1 - mirrorVideoNIR);
            setRotationY(nirVideoDirection, mirrorVideoNIR, nirFaceView);
        }
    }
    // 镜像
    public void setRotationY(int videoDirection, int isRgbRevert, AutoTexturePreviewView mTextureView){
        if (videoDirection == 90 || videoDirection == 270) {
            if (isRgbRevert == 1) {
                mTextureView.setRotationY(180);
            } else {
                mTextureView.setRotationY(0);
            }
            // 旋转90度或者270，需要调整宽高
        } else {
            if (isRgbRevert == 1) {
                mTextureView.setRotationY(180);
            } else {
                mTextureView.setRotationY(0);
            }
        }
    }
    @Override
    protected void onPause() {
        for (int i = 0; i < mCameraNum; i++) {
            if (mCamera[i] != null) {
                mCamera[i].setPreviewCallback(null);
                mCamera[i].stopPreview();
                mPreview[i].release();
                mCamera[i].release();
                mCamera[i] = null;
            }
        }
        super.onPause();
    }

}
