package com.kabc.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class StartActivity {

    public static void start(Context context, Class<? extends Activity> activityClass, Bundle extras) {
        Intent intent = new Intent(context, activityClass);
        intent.putExtras(extras);
        context.startActivity(intent);
    }
}
