package com.kabc.android.main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.kabc.android.R;
import com.kabc.android.connection.InternetConnection;
import com.kabc.android.tests.ConceptualThinking;
import com.kabc.android.tests.PatternReasoning;
import com.kabc.android.tests.StoryCompletion;
import com.kabc.android.activity.StartActivity;

public class MainActivity extends AppCompatActivity {

    boolean authorized;

    private final InternetConnection networkChangeListener = new InternetConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        String login = extras.getString("login");
        authorized = !login.equals("");

        if (authorized) {
            ((TextView) findViewById(R.id.loginText)).setText(getString(R.string.user_info, login));
        }

        findViewById(R.id.conceptualThinking).setOnClickListener(
                v -> StartActivity.start(this, ConceptualThinking.class, extras));
        findViewById(R.id.patternReasoning).setOnClickListener(
                v -> StartActivity.start(this, PatternReasoning.class, extras));
        findViewById(R.id.storyCompletion).setOnClickListener(
                v -> StartActivity.start(this, StoryCompletion.class, extras));

        findViewById(R.id.exit).setOnClickListener(exitListener);
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    View.OnClickListener exitListener = v -> {
        if (!authorized) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выход")
                .setMessage("Вы действительно хотите выйти?")
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Выйти",
                        (dialog, which) -> startActivity(new Intent(this, LoginActivity.class)));
        AlertDialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        dialog.show();
    };
}