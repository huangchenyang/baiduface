package com.example.datalibrary.deptrum;

import android.os.Bundle;
import android.util.Log;

import com.deptrum.usblite.callback.IDeviceListener;
import com.deptrum.usblite.callback.IStreamListener;
import com.deptrum.usblite.param.DTFrameStreamBean;
import com.deptrum.usblite.param.StreamParam;
import com.deptrum.usblite.param.StreamType;
import com.deptrum.usblite.sdk.DeptrumSdkApi;
import com.example.datalibrary.activity.BaseActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Executors;

public abstract class BaseDeptrumActivity extends BaseActivity {


    protected static final int RGB_WIDTH = 480;
    protected static final int RGB_HEIGHT = 768;

    private GLDisplay mRgbisplay;
    private GLDisplay mIrDisplay;
    private GLDisplay mDepthDisplay;

    protected GLFrameSurface mRgbSurface;
    protected GLFrameSurface mIrSurface;
    protected GLFrameSurface mDepSurface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRgbisplay = new GLDisplay();
        mIrDisplay = new GLDisplay();
        mDepthDisplay = new GLDisplay();
        // 摄像头图像预览
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                openDevice();
            }
        });
    }

    private void openDevice() {
        final long startTime = System.currentTimeMillis();
        DeptrumSdkApi.getApi().open(getApplicationContext(), new IDeviceListener() {
            @Override
            public void onAttach() {

            }

            @Override
            public void onDetach() {

            }

            @Override
            public void onOpenResult(int result) {
                if (0 == result) {
//                    mHandler.sendEmptyMessage(MESSAGE_UI);
                    DeptrumSdkApi.getApi().setStreamListener(new IStreamListener() {
                        @Override
                        public void onFrame(final DTFrameStreamBean iFrame) {

                            if (isFinishing()){
                                return;
                            }

                            final byte[] data = iFrame.getData();
                            switch (iFrame.getImageType()) {
                                case RGB: {
                                    long endTime = System.currentTimeMillis();
                                    Log.d("xjk open -> stream ", (endTime - startTime) + "");
                                    convertRGBToRGBA(data, RGB_WIDTH, RGB_HEIGHT);
                                    Log.d("xjk open", "rgb数据接收" + data.length);
                                    dealRgb(data);
                                    if (null != data) {
                                        mRgbSurface.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (null != mRgbisplay && null != mRgbSurface){
                                                    mRgbisplay.render(mRgbSurface, 0, false, data,
                                                            480, 768, 1);
                                                }
                                            }
                                        });
                                    }
                                }
                                break;
                                case IR: {
                                    if (null == data || RGB_WIDTH * RGB_HEIGHT != data.length) {
                                        return;
                                    }
                                    Log.d("xjk open", "nir数据接收" + data.length);
                                    convertGrayToRGBA(data, RGB_WIDTH, RGB_HEIGHT);
                                    dealIr(mIrBits);
                                    mIrSurface.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (null != mIrDisplay && null != mIrSurface) {
                                                mIrDisplay.render(mIrSurface, 0, false, mIrBits,
                                                        RGB_WIDTH, RGB_HEIGHT, 1);
                                            }
                                        }
                                    });
                                }
                                break;
                                case DEPTH: {
                                    if (null != data ) {
                                        Log.d("xjk open", "depth数据接收" + data.length);
                                        dealDepth(data);
                                        convertDepthToRGBA(data, RGB_WIDTH, RGB_HEIGHT);
                                        mDepSurface.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (null != mDepthDisplay && null != mDepSurface) {
                                                    mDepthDisplay.render(mDepSurface, 0, false, depthGLData,
                                                            RGB_WIDTH, RGB_HEIGHT, 1);
                                                }
                                            }
                                        });
                                    }
                                }
                                break;
                            }
                        }
                    });
                    DeptrumSdkApi.getApi().setScanFaceMode();
                    StreamParam param = new StreamParam();
                    param.width = RGB_WIDTH;
                    param.height = RGB_HEIGHT;
                    DeptrumSdkApi.getApi().setStreamParam(param);
                    DeptrumSdkApi.getApi().startStream(StreamType.STREAM_RGB_IR_DEPTH);
                    DeptrumSdkApi.getApi().configSet("enable_dt_face_kit", "1");
                }
            }

            @Override
            public void onErrorEvent(String s, int i) {

            }
        });
    }
    protected abstract void dealRgb(byte[] data);
    protected abstract void dealIr(byte[] data);
    protected abstract void dealDepth(byte[] data);

    private byte[] mRgbBits = null;
    private int mRgbLength = 0;
    public void convertRGBToRGBA(byte[] data, int width, int height) {
        try {
            int len = data.length / 3 * 4;
            if (null == mRgbBits || len != mRgbLength){
                mRgbBits = new byte[data.length / 3 * 4]; // RGBA 数组
                mRgbLength = data.length / 3 * 4;
            }

//            byte[] Bits = new byte[data.length / 3 * 4]; // RGBA 数组
            int i;
            for (i = 0; i < data.length / 3; i++) {
                // 原理：4个字节表示一个灰度，则RGB  = 灰度值，最后一个Alpha = 0xff;
                mRgbBits[i * 4] = data[i * 3];
                mRgbBits[i * 4 + 1] = data[i * 3 + 1];
                mRgbBits[i * 4 + 2] = data[i * 3 + 2];
                mRgbBits[i * 4 + 3] = -1; // 0xff
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }  /**
     * gray convert to rgba
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    private Mat outPutMat;

    protected byte[] decodeIrData(byte[] byteArr, int width, int height){
        byte[] bytes = new byte[width * height * 3];
        if (null == outPutMat){
            return bytes;
        }
        Mat rawMat = new MatOfByte(byteArr);
        rawMat.convertTo(rawMat, CvType.CV_8UC1, 1.0D, 0);
        Imgproc.cvtColor(rawMat, outPutMat, Imgproc.COLOR_GRAY2RGB);
        outPutMat.get(0, 0, bytes);
        rawMat.release();

        return bytes;
    }
    byte[] mIrBits = null;
    public byte[] convertGrayToRGBA(byte[] data, int width, int height) {
        try {
            int mIrLength = 0;
            int len = data.length * 4;
            if (null == mIrBits || len != mIrLength){
//                mIrBits = new byte[data.length * 4]; // RGBA 数组
                mIrLength = data.length * 4;
            }
            mIrBits = decodeIrData(data , width , height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mIrBits ;
    }
    int mDeDataByteLenght = 0;
    // 摄像头采集数据
    private volatile byte[] depthGLData;
    public void convertDepthToRGBA(byte[] data, int width, int height) {
        try {
            if (null == depthGLData || mDeDataByteLenght != width * height * 3){
                mDeDataByteLenght = width * height * 3;
            }
            depthGLData = decodeDepthData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected byte[] decodeDepthData(byte[] byteArr){
        byte[] bytes = new byte[480 * 768 * 3];
        if (null == outPutMat){
            return bytes;
        }
        byte[] colorImage = DeptrumSdkApi.getApi()
                .drawDepthMapInColor(768, 480, 0, 1500, 6000, byteArr);
        // byte转Mat
        Mat mat = new Mat(768, 480, CvType.CV_8UC3);
        mat.put(0, 0, colorImage);

        Imgproc.cvtColor(mat, outPutMat, Imgproc.COLOR_BGR2RGB);
        outPutMat.get(0, 0, bytes);
        mat.release();
        return bytes;
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(getClass().getName(), "Internal OpenCV library not found. Using OpenCV manger for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getApplicationContext(), mLoaderCallback);
        } else {
            Log.d(getClass().getName(), "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    protected final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.e("lbc_opencv", "OpenCV loaded successfully");
                    outPutMat = new Mat(RGB_HEIGHT,RGB_WIDTH, CvType.CV_8UC3);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        DeptrumSdkApi.getApi().stopStream(StreamType.STREAM_RGB_IR_DEPTH);
        DeptrumSdkApi.getApi().setStreamListener(null);
        mRgbisplay.release();
        mRgbisplay = null;

        mIrDisplay.release();
        mIrDisplay = null;

        mDepthDisplay.release();
        mDepthDisplay = null;
    }
}
