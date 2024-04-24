package com.example.datalibrary.lim;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.datalibrary.activity.BaseActivity;
import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiDeviceState;
import com.hjimi.api.iminect.ImiFrameMode;
import com.hjimi.api.iminect.ImiNect;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public abstract class BaseLimActivity extends BaseActivity{
    protected ImiDevice huajiemDevice;
    private MainListener mainlistener;
    private SimpleViewer mColorViewer;
    private SimpleViewer mDepthViewer;
    protected boolean destroyFlag;
    private boolean huajiemDeviceOpen;


    private static final int DEVICE_OPEN_SUCCESS = 0;
    private static final int DEVICE_OPEN_FALIED = 1;
    private static final int DEVICE_DISCONNECT = 2;

    protected void openLim(){
        if(!destroyFlag){
            try {
                Log.e("Lim_camera", "initialize");
                ImiNect.initialize();
                Log.e("Lim_camera", "initializeOK");

                Log.e("Lim_camera", "ImiDevice.getInstance");
                huajiemDevice = ImiDevice.getInstance();
                Log.e("Lim_camera", "new MainListener");
                mainlistener = new MainListener();
                Log.e("Lim_camera", "huajiemDevice.open");
                huajiemDevice.open(this, 0, mainlistener);
            }catch (Exception e){
                e.fillInStackTrace();
            }
            Log.e("Lim_camera", "huajiemDevice.openOk");
        }
    }

    protected MyHandler mainHandler = new MyHandler(this);
    public class MainListener implements ImiDevice.OpenDeviceListener, ImiDevice.DeviceStateListener {

        @Override
        public void onOpenDeviceSuccess() {
            huajiemDeviceOpen = true;

            Log.d("Lim_camera", "imi onOpenDeviceSuccess");
            mainHandler.sendEmptyMessage(DEVICE_OPEN_SUCCESS);
        }

        @Override
        public void onOpenDeviceFailed(String errorMsg) {
            if (destroyFlag){
                ImiDevice.destroy();
            }
            // open device falied.
            Log.d("Lim_camera", "imi onOpenDeviceFailed:" + errorMsg);
            mainHandler.sendMessage(mainHandler.obtainMessage(DEVICE_OPEN_FALIED, errorMsg));
        }

        @Override
        public void onDeviceStateChanged(String deviceInfo, ImiDeviceState state) {
            if (destroyFlag){
                ImiDevice.destroy();
            }
            Log.d("Lim_camera", "imi onDeviceStateChanged, deviceInfo:" + deviceInfo + ", state:" + state);
            if (state == ImiDeviceState.IMI_DEVICE_STATE_CONNECT) {
                Toast.makeText(BaseLimActivity.this, deviceInfo + " CONNECT", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(BaseLimActivity.this, deviceInfo + " DISCONNECT", Toast.LENGTH_LONG).show();
            }
        }
    }
    protected SimpleViewer.DataDealListener imiDataDealListener = new SimpleViewer.DataDealListener() {
        @Override
        public void dealDepth(byte[] data) {
            // FaceLimActivity.this.dealDepth(data);
        }

        @Override
        public void dealRgb(byte[] data) {
            // FaceLimActivity.this.dealRgb(data);
        }

        @Override
        public void dealIr(byte[] data) {

        }
    };

    static class MyHandler extends Handler {
        WeakReference<BaseLimActivity> mActivity;
        public MyHandler(BaseLimActivity activity) {
            mActivity = new WeakReference<BaseLimActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            BaseLimActivity mainActivity = mActivity.get();

            Log.d("Lim_camera", "msg:" + msg.what);
            switch (msg.what) {
                case DEVICE_OPEN_FALIED:
                case DEVICE_DISCONNECT:
                    break;
                case DEVICE_OPEN_SUCCESS:
                    mainActivity.runViewer();
                    break;
            }
        }
    }
    /**
     * ByteBuffer 转换 byte[]
     */

    @Override
    protected void onResume() {
        super.onResume();
        if (mDepthViewer != null) {
            mDepthViewer.onResume();
        }

        if (mColorViewer != null) {
            mColorViewer.onResume();
        }
    }
    protected byte[] decodeValue(ByteBuffer byteBuffer) {
        int len = byteBuffer.limit() - byteBuffer.position();
        byte[] bytes1 = new byte[len];
        byteBuffer.get(bytes1);
        return bytes1;

    }
    private void runViewer(){
        if (huajiemDevice == null){
            Log.d("Lim_camera", "huajiemDevice == null");
            return;
        }
        Log.d("Lim_camera", "runViewer");
        if (huajiemDevice.getAttribute().isPortraitDevice()) {
            huajiemDevice.setFramesSync(false);
        }

        huajiemDevice.setImageRegistration(true);

        // set depth frame mode
        ImiFrameMode depthFrameMode = huajiemDevice.getCurrentFrameMode(ImiDevice.ImiStreamType.DEPTH);
        huajiemDevice.setFrameMode(ImiDevice.ImiStreamType.DEPTH, depthFrameMode);

        // set color frame mode
        ImiFrameMode colorFrameMode = huajiemDevice.getCurrentFrameMode(ImiDevice.ImiStreamType.COLOR);
        huajiemDevice.setFrameMode(ImiDevice.ImiStreamType.COLOR, colorFrameMode);

        huajiemDevice.startStream(ImiDevice.ImiStreamType.DEPTH.toNative() | ImiDevice.ImiStreamType.COLOR.toNative());

        mColorViewer = new SimpleViewer(huajiemDevice, ImiDevice.ImiStreamType.COLOR, imiDataDealListener);
        mDepthViewer = new SimpleViewer(huajiemDevice, ImiDevice.ImiStreamType.DEPTH, imiDataDealListener);

        Log.d("Lim_camera", "runViewerOk");
        showViewer(mColorViewer , mDepthViewer);
    }
    protected abstract void showViewer(SimpleViewer mColorViewer , SimpleViewer mDepthViewer);

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyFlag = true;
        // 关闭摄像头
        if (huajiemDevice != null) {
            if (mDepthViewer != null) {
                mDepthViewer.onPause();
            }

            if (mColorViewer != null) {
                mColorViewer.onPause();
            }
            Log.e("Lim_camera", "mDepthViewer.onDestroy()");
            if (mDepthViewer != null) {
                mDepthViewer.onDestroy();
            }
            Log.e("Lim_camera", "mColorViewer.onDestroy()");
            if (mColorViewer != null){
                mColorViewer.onDestroy();
            }
            Log.e("Lim_camera", "huajiemDevice.close");
            huajiemDevice.close();
            huajiemDevice = null;
            Log.e("Lim_camera", "ImiDevice.destroy");
            if (huajiemDeviceOpen){
                ImiDevice.destroy();
            }
        }
    }
}
