package com.raybritton.uiinspectorserver.ui.views;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.f2prateek.rx.preferences2.Preference;

public class PrefCheckbox extends AppCompatCheckBox {
    public PrefCheckbox(Context context) {
        super(context);
    }

    public PrefCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrefCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPreference(Preference<Boolean> pref) {
        this.setChecked(pref.get());
        setOnCheckedChangeListener((buttonView, isChecked) -> pref.set(isChecked));


    }
}
