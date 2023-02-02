package com.kabc.android.dropbox;

import android.content.Context;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;

import java.io.FileInputStream;
import java.io.IOException;

public class Dropbox {

    private static final String ACCESS_TOKEN = "sl.BVl9hzeyGEe05TQW92TOwGp_RANLtQnfH5spJspFI7COLjm5frmVcsyCAiFvW_FbDwkyFaEgluwZflEEk3tOUTjv5bobb-YoNdUk6kXKS4Nma1Xh_8QLmZhINCwrj6gVq-BCO81RpW7t";
    private static final String REFRESH_TOKEN = "AMLh2IdQsv0AAAAAAAAAAbrJhhvFVOrfHCjysQE6DevI6vemALGYrgOKTMN7LP78";
    private static final String APP_KEY = "vhlpyb0vt0dlj1d";
    private static final String APP_SECRET = "m8atemwz3ka36cv";

    public static DbxClientV2 getClient() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("").build();
        DbxCredential credentials = new DbxCredential(
                ACCESS_TOKEN, -1L, REFRESH_TOKEN, APP_KEY, APP_SECRET
        );
        return new DbxClientV2(config, credentials);
    }

    public static void uploadFile(Context context, DbxClientV2 client, String fileName) {
        new Thread(() -> {
            try {
                FileInputStream inputStream = context.openFileInput(fileName);
                client.files().uploadBuilder("/" + fileName).uploadAndFinish(inputStream);
            } catch (IOException | DbxException e) {
                e.printStackTrace();
            }
        }).start();
    }
}