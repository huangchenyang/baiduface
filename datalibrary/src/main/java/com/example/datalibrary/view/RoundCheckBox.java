package com.example.datalibrary.view;

import android.content.Context;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.example.datalibrary.R;

/**
 * author : shangrong
 * date : 2019/6/10 10:12 PM
 * description :
 */
public class RoundCheckBox extends AppCompatCheckBox {
    public RoundCheckBox(Context context) {
        this(context, null);
    }

    public RoundCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.radioButtonStyle);
    }

    public RoundCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
