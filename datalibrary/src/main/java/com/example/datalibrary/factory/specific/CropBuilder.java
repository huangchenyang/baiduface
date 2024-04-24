package com.example.datalibrary.factory.specific;

import android.content.Context;

import com.baidu.idl.main.facesdk.FaceCrop;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.example.datalibrary.factory.builder.ModelBuilder;
import com.example.datalibrary.listener.SdkInitListener;

public class CropBuilder  extends ModelBuilder<FaceCrop> {

    private FaceCrop faceCrop;
    private SdkInitListener listener;

    public CropBuilder(SdkInitListener listener) {
        this.listener = listener;
    }

    @Override
    public void init(BDFaceInstance bdFaceInstance) {
        if (bdFaceInstance == null) {
            faceCrop = new FaceCrop();
        } else {
            faceCrop = new FaceCrop(bdFaceInstance);
        }
    }

    @Override
    public void init() {
        faceCrop = new FaceCrop();

    }

    @Override
    public void initModel(Context context) {
        faceCrop.initFaceCrop(new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });
    }

    @Override
    public FaceCrop getExample() {
        return faceCrop;
    }
}

