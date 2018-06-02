package com.migutv.seatviewlib;

import android.content.Context;

/**
 * Created by caominyan on 2018/5/31.
 */

public class Util {

    public static float dip2Px(Context ctx, float value) {
        return ctx.getResources().getDisplayMetrics().density * value;
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
