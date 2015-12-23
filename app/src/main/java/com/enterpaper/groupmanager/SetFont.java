package com.enterpaper.groupmanager;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Kim on 2015-08-02.
 */
// 폰트 설정
public class SetFont {

    public static void setGlobalFont(Context context,View view){
        if (view != null) {
            if (view instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) view;
                int len = vg.getChildCount();
                for (int i = 0; i < len; i++) {
                    View v = vg.getChildAt(i);
                    if (v instanceof TextView) {
                        ((TextView) v).setTypeface(Typeface.createFromFile("/system/fonts/DroidSans.ttf"));
                    }
                    setGlobalFont(context, v);
                }

            }
        } else {
           // Log.e("err","This is null");
        }

    }
}
