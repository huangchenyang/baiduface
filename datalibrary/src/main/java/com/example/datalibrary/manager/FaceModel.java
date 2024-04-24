package com.example.datalibrary.manager;

import android.content.Context;

import com.baidu.idl.main.facesdk.FaceCrop;
import com.baidu.idl.main.facesdk.FaceDarkEnhance;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceFeature;
import com.baidu.idl.main.facesdk.FaceLive;
import com.baidu.idl.main.facesdk.FaceSearch;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;
import com.example.datalibrary.factory.specific.CropBuilder;
import com.example.datalibrary.factory.specific.DarkBuilder;
import com.example.datalibrary.factory.specific.DetectBuilder;
import com.example.datalibrary.factory.specific.DetectNirBuilder;
import com.example.datalibrary.factory.specific.DetectQualityBuilder;
import com.example.datalibrary.factory.specific.FeatureBuilder;
import com.example.datalibrary.factory.specific.LiveBuilder;
import com.example.datalibrary.factory.specific.TrackBuilder;
import com.example.datalibrary.listener.SdkInitListener;

public class FaceModel implements SdkInitListener{
    private TrackBuilder trackBuilder;
    private DetectBuilder detectBuilder;
    private DetectQualityBuilder detectQualityBuilder;
    private DetectNirBuilder detectNirBuilder;
    private DarkBuilder darkBuilder;
    private FeatureBuilder featureBuilder;
    private FeatureBuilder featurePersonBuilder;
    private LiveBuilder liveBuilder;
    private CropBuilder cropBuilder;
    private boolean isModelInit;

    public void setListener(SdkInitListener listener) {
        this.listener = listener;
    }

    private SdkInitListener listener;
    public FaceModel(){
        isModelInit = false;
        cropBuilder = new CropBuilder(this);
        trackBuilder = new TrackBuilder(this);
        detectBuilder = new DetectBuilder(this);
        detectQualityBuilder = new DetectQualityBuilder(this);
        detectNirBuilder = new DetectNirBuilder(this);
        darkBuilder = new DarkBuilder(this);
        liveBuilder = new LiveBuilder(this);
        featureBuilder = new FeatureBuilder(this);
        featurePersonBuilder = new FeatureBuilder(this);
    }
    public void init(BDFaceSDKConfig config , Context context){
        if (isModelInit){
            return;
        }
        BDFaceInstance trackInstance = new BDFaceInstance();
        trackInstance.creatInstance();
        BDFaceInstance detectInstance = new BDFaceInstance();
        detectInstance.creatInstance();
        BDFaceInstance detectQualityInstance = new BDFaceInstance();
        detectQualityInstance.creatInstance();
        BDFaceInstance cropInstance = new BDFaceInstance();
        cropInstance.creatInstance();

        trackBuilder.init(trackInstance , config);
        cropBuilder.init(cropInstance);
        detectBuilder.init(detectInstance , config);
        detectNirBuilder.init(detectInstance);
        detectQualityBuilder.init(detectQualityInstance , config);
        darkBuilder.init(null);
        liveBuilder.init(null);
        featurePersonBuilder.init(detectQualityInstance);
        featureBuilder.init(null);

        cropBuilder.initModel(context);
        trackBuilder.initModel(context);
        detectBuilder.initModel(context);
        detectQualityBuilder.initModel(context);
        detectNirBuilder.initModel(context);
        darkBuilder.initModel(context);
        liveBuilder.initModel(context);
        featurePersonBuilder.initModel(context);
        featureBuilder.initModel(context);
    }

    @Override
    public void initStart() {

        listener.initStart();
    }

    @Override
    public void initLicenseSuccess() {

        listener.initLicenseSuccess();
    }

    @Override
    public void initLicenseFail(int errorCode, String msg) {
        listener.initLicenseFail(errorCode , msg);
    }

    @Override
    public void initModelSuccess() {
        listener.initModelSuccess();
        isModelInit = true;
    }

    @Override
    public void initModelFail(int errorCode, String msg) {
        isModelInit = false;
        listener.initModelFail(errorCode , msg);
    }
    public FaceFeature getFacePersonFeature(){
        return featurePersonBuilder.getExample();
    }
    public FaceSearch getFacePersonSearch(){
        return featurePersonBuilder.getFaceSearch();
    }
    public FaceFeature getFaceFeature(){
        return featureBuilder.getExample();
    }
    public FaceSearch getFaceSearch(){
        return featureBuilder.getFaceSearch();
    }
    public FaceDetect getFaceTrack(){
        return trackBuilder.getExample();
    }
    public FaceCrop getFaceCrop(){
        return cropBuilder.getExample();
    }
    public FaceDetect getFaceDetectPerson(){
        return detectQualityBuilder.getExample();
    }
    public FaceDetect getFaceDetect(){
        return detectBuilder.getExample();
    }
    public FaceDetect getFaceNirDetect(){
        return detectNirBuilder.getExample();
    }
    public FaceDarkEnhance getDark(){
        return darkBuilder.getExample();
    }public FaceLive getFaceLive(){
        return liveBuilder.getExample();
    }
}
