package com.example.datalibrary.lim;

import android.util.Log;

import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiFrameMode;
import com.hjimi.api.iminect.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class SimpleViewer extends Thread {

    private boolean mShouldRun = false;

    private ImiDevice.ImiStreamType mStreamType;
    private GLPanel mGLPanel;
    private GLPanel mIrGLPanel;
    private DecodePanel mDecodePanel;
    private ImiDevice mDevice;
    private ImiFrameMode mCurrentMode;
    private DataDealListener dataDealInterface;

    public byte[] rgbData;
    public byte[] deptData;
    public byte[] irData;

    public SimpleViewer(ImiDevice device, ImiDevice.ImiStreamType streamType, DataDealListener dealInterface) {
        mDevice = device;
        mStreamType = streamType;
        dataDealInterface = dealInterface;
    }

    public void setGLPanel(GLPanel mGLPanel) {
        this.mGLPanel = mGLPanel;
    }

    public void setIrGLPanel(GLPanel mIrGLPanel) {
        this.mIrGLPanel = mIrGLPanel;
    }

    public void setDecodePanel(DecodePanel decodePanel) {
        this.mDecodePanel = decodePanel;
    }

    public interface DataDealListener {
        void dealDepth(byte[] data);
        void dealRgb(byte[] data);
        void dealIr(byte[] data);
    }

    @Override
    public void run() {
        super.run();
        // get current framemode.
        // int pid = android.os.Process.myPid();
        try {

            if (mDevice == null || mStreamType == null){
                return;
            }
            mCurrentMode = mDevice.getCurrentFrameMode(mStreamType);

            // Log.d("imi_test", "sv runs start, pid:"+pid+", mCurrentMode format:"+mCurrentMode.getFormat()+", mShouldRun:"+mShouldRun);
            // start read frame.
            while (mShouldRun) {
                ImiDevice.ImiFrame nextFrame = mDevice.readNextFrame(mStreamType, 25);
                // Log.d("imi_test", "sv runs , pid:"+pid+", nextFrame size:"+((nextFrame!=null)?""+nextFrame.getSize(): "null"));
                // frame maybe null, if null, continue.
                if (nextFrame == null){
                    continue;
                }
                // Log.d("imi_test", "sv runs , pid:"+pid+", mStreamType:"+mStreamType);
                switch (mStreamType) {
                    case COLOR:
                        // draw color.
                        drawColor(nextFrame);
                        break;
                    case DEPTH:
                        // draw depth.
                        drawDepth(nextFrame);
                        break;
                    case IR:
                        // draw ir
                        drawIr(nextFrame);
                        break;
                    case DEPTH_IR:
                        drawDepthIR(nextFrame);
                        break;
                }
            }

            Log.d("imi_test", "sv run end");
        }catch (Exception e){
            e.fillInStackTrace();
        }
    }

    private void drawDepthIR(ImiDevice.ImiFrame nextFrame) {
        ByteBuffer frameData = nextFrame.getData();
        int width = nextFrame.getWidth();
        int height = nextFrame.getHeight();

        if (null == frameData) {
            Log.d("imi_test", "drawDepthIR frameData is null");
            return;
        }

        ByteBuffer depthData = Utils.depth2RGB888(frameData, width, height, true, false);
        // mDepthGLPanel.paint(null, depthData, width, height);
        if (depthData != null) {
            int depthLen = frameData.remaining();
            byte[] depthByte = new byte[depthLen];
            frameData.get(depthByte);
            deptData = depthByte;
            if (dataDealInterface != null) {
                dataDealInterface.dealDepth(deptData);
            }
            /*FaceDepthGateActivity faceDepthGateActivity = new FaceDepthGateActivity();
            faceDepthGateActivity.dealDepth(depthByte);*/

            if (mGLPanel != null){
                mGLPanel.paint(null, depthData, width, height);
            }
        }

        ByteBuffer irBuffer = ByteBuffer.allocateDirect(width * height * 2);
        irBuffer.order(ByteOrder.nativeOrder());
        irBuffer.position(0);

        frameData.position(width * height * 2);
        irBuffer.put(frameData);

        ByteBuffer irDataBuf =  Utils.ir2RGB888(irBuffer, width, height, false);

        // mIRGLPanel.paint(null, irData, width, height);
        if (irDataBuf != null) {
            int irLen = irDataBuf.remaining();
            byte[] irByte = new byte[irLen];
            irDataBuf.get(irByte);
            irData = irByte;
            if (dataDealInterface != null) {
                dataDealInterface.dealIr(irData);
            }

            if (mIrGLPanel != null) {
                mIrGLPanel.paint(null, irDataBuf, width, height);
            }
        }
    }

    private void drawDepth(ImiDevice.ImiFrame nextFrame) {
        ByteBuffer frameData = nextFrame.getData();
        int width = nextFrame.getWidth();
        int height = nextFrame.getHeight();
        // Log.d("imi_test", "drawColor width:"+width+", height:"+height);

        // TODO: 测试代码
        frameData = Utils.depth2RGB888(nextFrame, true, false);

        // mGLPanel.paint(null, frameData, width, height);
        // Log.d("imi_test", "drawDepth Format:"+mCurrentMode.getFormat());

        if (frameData != null) {
            int depthLen = frameData.remaining();
            byte[] depthByte = new byte[depthLen];
            frameData.get(depthByte);
            deptData = depthByte;
            if (dataDealInterface != null) {
                dataDealInterface.dealDepth(deptData);
            }
            /*FaceDepthGateActivity faceDepthGateActivity = new FaceDepthGateActivity();
            faceDepthGateActivity.dealDepth(depthByte);*/

            if (mGLPanel != null){
                mGLPanel.paint(null, frameData, width, height);
            }
        }
    }

    private void drawIr(ImiDevice.ImiFrame nextFrame) {
        ByteBuffer frameData = nextFrame.getData();
        int width = nextFrame.getWidth();
        int height = nextFrame.getHeight();

        frameData = Utils.ir2RGB888(nextFrame, false);

        if (frameData != null) {
            int irLen = frameData.remaining();
            byte[] irByte = new byte[irLen];
            frameData.get(irByte);
            deptData = irByte;
            if (dataDealInterface != null) {
                dataDealInterface.dealIr(deptData);
            }

            if (mGLPanel != null) {
                mGLPanel.paint(null, frameData, width, height);
            }
        }
    }

    public byte[] getDeptData(){
        return deptData;
    }

    public byte[] getRgbData(){
        return rgbData;
    }

    public byte[] getIrData(){
        return irData;
    }

    private void drawColor(ImiDevice.ImiFrame nextFrame) {
        ByteBuffer frameData = nextFrame.getData();
        int width = nextFrame.getWidth();
        int height = nextFrame.getHeight();
        // Log.d("imi_test", "drawColor width:"+width+", height:"+height);

        if (frameData != null) {
            int rgbByteLen = frameData.remaining();
            byte[] rgbByte = new byte[rgbByteLen];
            frameData.get(rgbByte);
            rgbData = rgbByte;
            if (dataDealInterface != null) {
                dataDealInterface.dealRgb(rgbData);
            }
            /*FaceDepthGateActivity faceDepthGateActivity = new FaceDepthGateActivity();
            faceDepthGateActivity.dealRgb(rgbByte);*/
        }

        // Log.d("imi_test", "drawColor Format:"+mCurrentMode.getFormat());
        // draw color image.
        switch (mCurrentMode.getFormat()) {
            case IMI_PIXEL_FORMAT_IMAGE_H264:
                if (mDecodePanel != null){
                    mDecodePanel.paint(frameData, nextFrame.getTimeStamp());
                }
                break;
            case IMI_PIXEL_FORMAT_IMAGE_YUV420SP:
                frameData = Utils.yuv420sp2RGB(nextFrame);
                if (mGLPanel != null){
                    mGLPanel.paint(null, frameData, width, height);
                }
                break;
            case IMI_PIXEL_FORMAT_IMAGE_RGB24:
                if (mGLPanel != null){
                    mGLPanel.paint(null, frameData, width, height);
                }
                break;
            default:
                break;
        }
    }

    public void onPause(){
        if (mGLPanel != null){
            mGLPanel.onPause();
        }
    }

    public void onResume(){
        if (mGLPanel != null){
            mGLPanel.onResume();
        }
    }

    public void onStart(){
        if (!mShouldRun){
            mShouldRun = true;

            // start read thread
            this.start();
        }
    }

    public void onDestroy(){
        mShouldRun = false;
    }
}
