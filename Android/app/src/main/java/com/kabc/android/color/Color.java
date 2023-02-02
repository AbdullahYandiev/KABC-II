package com.kabc.android.color;

import android.content.Context;
import android.content.res.ColorStateList;

public class Color {
    public static ColorStateList getColorState(Context context, int color) {
        return ColorStateList.valueOf(context.getResources().getColor(color));
    }
}
