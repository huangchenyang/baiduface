package com.example.datalibrary.lim;

import java.nio.ByteBuffer;

/**
 * Create by lixby
 */
public interface OBRgbCameraFrameCallback {
    /**
     * @param byteBuffer   预览数据
     * @param width  预览宽
     * @param height 预览高
     */
    void onFramceCallback(ByteBuffer byteBuffer, int width, int height);
}
