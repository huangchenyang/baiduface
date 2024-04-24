package com.baidu.idl.main.facesdk.identifylibrary.ZKNIDFPSensor;

public interface ZKNIDFPSensorListener {
    /**
     * image captured
     * @param image raw image data
     */
    void onCapture(byte[] image);

    /**
     * connection exception occurd
     * @param count exception times.
     */
    void onException(int count);
}
