package com.baidu.idl.main.facesdk.identifylibrary.ZKNIDFPSensor;

import android.content.Context;
import android.util.Log;

import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.module.fingerprintreader.ZKFingerService;
import com.zkteco.android.biometric.nidfpsensor.NIDFPFactory;
import com.zkteco.android.biometric.nidfpsensor.NIDFPSensor;
import com.zkteco.android.biometric.nidfpsensor.exception.NIDFPException;

import java.util.HashMap;
import java.util.Map;

public class FingerprintSensor {
    private ZKNIDFPSensorListener listener = null;
    private NIDFPSensor nidfpSensor = null;
    private ZKNIDFPSensorCaptureThread zknidfpSensorCaptureThread = new ZKNIDFPSensorCaptureThread(this);
    private int devIndex = 0;
    //同步锁
    protected Object mDeviceLock = new Object();

    private NIDFPSensor createNIDFPSensor(Context context, int vid, int pid)
    {
        NIDFPSensor sensor = null;
        // Define output log level
        LogHelper.setLevel(Log.VERBOSE);
        LogHelper.setNDKLogLevel(Log.ASSERT);
        // Start fingerprint sensor
        Map deviceParams = new HashMap();
        //set vid
        deviceParams.put(ParameterHelper.PARAM_KEY_VID, vid);
        //set pid
        deviceParams.put(ParameterHelper.PARAM_KEY_PID, pid);
        sensor = NIDFPFactory.createNIDFPSensor(context, TransportType.USBSCSI, deviceParams);
        return sensor;
    }

    private void destroyNIDFPSensor(NIDFPSensor sensor)
    {
        NIDFPFactory.destroy(sensor);
    }

    protected ZKNIDFPSensorListener getDeviceListener()
    {
        return this.listener;
    }


    public void setDeviceListener(ZKNIDFPSensorListener listener)
    {
        this.listener = listener;
    }

    public int getImageWidth()
    {
        if (null != nidfpSensor)
        {
            return nidfpSensor.getFpImgWidth();
        }
        else
        {
            return 0;
        }
    }

    public int getImageHeight()
    {
        if (null != nidfpSensor)
        {
            return nidfpSensor.getFpImgHeight();
        }
        else
        {
            return 0;
        }
    }

    public String getSDKVersion()
    {
        return NIDFPSensor.version();
    }

    public String getFirmwareVersion()
    {
        if (null != nidfpSensor)
        {
            return nidfpSensor.getFirmwareVersion();
        }
        return "";
    }

    public String getSerialNumber()
    {
        if (null != nidfpSensor)
        {
            return nidfpSensor.getSerialNumber();
        }
        return "";
    }

    public int getStatus()
    {
        synchronized (mDeviceLock) {
            if (null != nidfpSensor) {
                return nidfpSensor.getDeviceStatus(devIndex);
            }
            return -1;
        }
    }

    public boolean open(int index, Context context, int vid, int pid)
    {
        synchronized (mDeviceLock) {
            if (null != nidfpSensor) {
                destroyNIDFPSensor(nidfpSensor);
            }
            nidfpSensor = createNIDFPSensor(context, vid, pid);
            try {
                nidfpSensor.open(index);
                devIndex = index;
                return true;
            } catch (NIDFPException e) {
                //e.printStackTrace();
                try {
                    nidfpSensor.rebootDevice(index);
                } catch (NIDFPException ex) {
                    ex.printStackTrace();
                }
                destroyNIDFPSensor(nidfpSensor);
                nidfpSensor = null;
                return false;
            }
        }
    }

    public void close()
    {
        synchronized (mDeviceLock) {
            if (null != nidfpSensor) {
                try {
                    nidfpSensor.close(devIndex);
                } catch (NIDFPException e) {
                    e.printStackTrace();
                }
                destroyNIDFPSensor(nidfpSensor);
                nidfpSensor = null;
                ZKFingerService.free();
            }
        }
    }

    public void startCapture()
    {
        synchronized (mDeviceLock) {
            if (null != nidfpSensor) {
                if (zknidfpSensorCaptureThread.isRunning()) {
                    return;
                }
                new Thread(zknidfpSensorCaptureThread).start();
            }
        }
    }

    public void stopCapture()
    {
        if (zknidfpSensorCaptureThread.isRunning())
        {
            zknidfpSensorCaptureThread.cancel();
        }
    }

    public void reset()
    {
        synchronized (mDeviceLock) {
            if (null != nidfpSensor)
            {
                try {
                    nidfpSensor.resetUSB(devIndex);
                } catch (NIDFPException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void resetEx(int index, Context context, int vid, int pid)
    {
        synchronized (mDeviceLock) {
            NIDFPSensor sensor = createNIDFPSensor(context, vid, pid);
            try {
                sensor.rebootDevice(index);
            } catch (NIDFPException e) {
                e.printStackTrace();
            }
            destroyNIDFPSensor(sensor);
        }
    }

    public boolean capture(byte[] fpImage)
    {
        synchronized (mDeviceLock) {
            if (null == nidfpSensor) {
                return false;
            }
            try {
                nidfpSensor.GetFPRawData(devIndex, fpImage);
                return true;
            } catch (NIDFPException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public byte getImageQuality(byte[] fpImage)
    {
        synchronized (mDeviceLock) {
            if (null == nidfpSensor) {
                return 0;
            }
            byte[] score = new byte[1];
            int retVal = nidfpSensor.getQualityScore(fpImage, score);
            if (retVal == 1) {
                return score[0];
            }
            else
            {
                return 0;
            }
        }
    }

    public boolean extract(byte[] fpImage, byte[] feature)
    {
        synchronized (mDeviceLock) {
            if (null == nidfpSensor) {
                return false;
            }
            if (null == fpImage || null == feature || feature.length < 512) {
                return false;
            }
            int retVal = nidfpSensor.FeatureExtract(devIndex, fpImage, feature);
            if (1 == retVal) {
                return true;
            }
            return false;
        }
    }

    public float imageMatch(byte[] fpImage, byte[] feature)
    {
        synchronized (mDeviceLock) {
            if (null == nidfpSensor) {
                return 0;
            }
            if (null == fpImage || null == feature || feature.length < 512) {
                return 0;
            }
            float score = nidfpSensor.ImageMatch(devIndex, fpImage, feature);
            return score;
        }
    }

    public float featureMatch(byte[] feature1, byte[] feature2)
    {
        synchronized (mDeviceLock) {
            if (null == nidfpSensor) {
                return 0;
            }
            if (null == feature1 || feature1.length < 512 || null == feature2 || feature2.length < 512) {
                return 0;
            }
            float score = nidfpSensor.FeatureMatch(devIndex, feature1, feature2);
            return score;
        }
    }
}
