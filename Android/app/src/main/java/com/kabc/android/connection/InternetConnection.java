package com.kabc.android.connection;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;

public class InternetConnection extends BroadcastReceiver {

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            for (NetworkInfo info : connectivityManager.getAllNetworkInfo()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isConnectedToInternet(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Не удалось подключиться к сети")
                    .setMessage("Проверьте свое интернет-соединение и попробуйте снова.")
                    .setNeutralButton("Перезапустить", (dialog, which) -> {
                        dialog.dismiss();
                        onReceive(context, intent);
                    });
            AlertDialog dialog = builder.create();
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.setCancelable(false);
            dialog.show();
        }
    }
}
