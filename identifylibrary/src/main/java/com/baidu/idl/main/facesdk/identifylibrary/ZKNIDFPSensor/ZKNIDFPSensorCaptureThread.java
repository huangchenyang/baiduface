package com.baidu.idl.main.facesdk.identifylibrary.ZKNIDFPSensor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZKNIDFPSensorCaptureThread implements Runnable {
    private CountDownLatch countdownLatch = null;
    private boolean isCancel = false;
    private int mExceptCnt = 0;
    private final int MAX_TRY_CNT = 5;
    private byte[] mfpImage = null;
    private FingerprintSensor fingerprintSensor = null;
    private boolean mbRunning = false;

    public ZKNIDFPSensorCaptureThread(final FingerprintSensor fingerprintSensor) {
        this.fingerprintSensor = fingerprintSensor;
    }

    @Override
    public void run() {
        countdownLatch = new CountDownLatch(1); //!!!重要，让线程优雅的退出!!!
        mbRunning = true;
        isCancel = false;
        while (!isCancel) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (null == mfpImage)
            {
                mfpImage = new byte[640*480];
            }
            final ZKNIDFPSensorListener listener = fingerprintSensor.getDeviceListener();
            if (null == listener)   //未设置listener，不需要通知上层
            {
                continue;
            }

            if (fingerprintSensor.capture(mfpImage))
            {
                listener.onCapture(mfpImage);
            }
            else
            {
                int status = fingerprintSensor.getStatus();
                if (0 == status) //状态恢复后重置状态
                {
                    mExceptCnt = 0;
                }
                else
                {
                    mExceptCnt++;
                }
                if (mExceptCnt >= MAX_TRY_CNT)  //连续5次异常后，每次异常都通知上层
                {
                    listener.onException(mExceptCnt);
                }
            }
        }
        countdownLatch.countDown();
        mbRunning = false;
    }

    public boolean isRunning()
    {
        return this.mbRunning;
    }

    public void cancel() {
        this.isCancel = true;
        try {
            if (null != countdownLatch) {
                countdownLatch.await(2, TimeUnit.SECONDS);
            }
            if (null != mfpImage)
            {
                mfpImage = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isCancel() {
        return this.isCancel;
    }
}
