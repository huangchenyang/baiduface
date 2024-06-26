package com.baidu.idl.face.main.drivermonitor.setting;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.baidu.idl.face.main.drivermonitor.activity.DrivermonitorBaseActivity;
import com.baidu.idl.face.main.drivermonitor.manager.FaceSDKManager;
import com.baidu.idl.face.main.drivermonitor.model.SingleBaseConfig;
import com.baidu.idl.face.main.drivermonitor.utils.DriverMonitorConfigUtils;
import com.baidu.idl.face.main.drivermonitor.utils.PWTextUtils;
import com.baidu.idl.main.facesdk.drivermonitor.R;

public class LogSettingActivity extends DrivermonitorBaseActivity {
    private Switch swLog;
    private Button tipsLog;
    private View groupLog;
    private TextView tvLog;
    private View groupFunLog;
    private String msgTag = "";
    private int showWidth;
    private int showXLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_setting);
        init();
        initListener();
    }
    private void init(){
        swLog = findViewById(R.id.sw_log);
        // log开关
        tipsLog = findViewById(R.id.tips_log);
        tvLog = findViewById(R.id.tv_log);
        groupLog = findViewById(R.id.group_log);
        groupFunLog = findViewById(R.id.group_fun_log);
        if (SingleBaseConfig.getBaseConfig().isLog()) {
            swLog.setChecked(true);
        } else {
            swLog.setChecked(false);
        }
    }
    private void initListener(){
        tipsLog.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (msgTag.equals(getString(R.string.cw_log))) {
                    msgTag = "";
                    return;
                }
                msgTag = getString(R.string.cw_log);
                tipsLog.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(groupFunLog, tvLog, LogSettingActivity.this,
                        getString(R.string.cw_log), showWidth, showXLocation);
            }
        });
        PWTextUtils.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDismiss() {
                tipsLog.setBackground(getDrawable(R.mipmap.icon_setting_question));
            }
        });

        findViewById(R.id.qc_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swLog.isChecked()) {
                    SingleBaseConfig.getBaseConfig().setLog(true);
                } else {
                    SingleBaseConfig.getBaseConfig().setLog(false);
                }
                DriverMonitorConfigUtils.modityJson();
                FaceSDKManager.getInstance().setActiveLog();
                finish();
            }
        });
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        showWidth = groupFunLog.getWidth();
        showXLocation = (int) groupLog.getX();
    }
}
