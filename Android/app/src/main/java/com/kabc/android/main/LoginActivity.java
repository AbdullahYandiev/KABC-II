package com.kabc.android.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.kabc.android.R;
import com.kabc.android.color.Color;
import com.kabc.android.connection.InternetConnection;
import com.kabc.android.date.Date;
import com.kabc.android.activity.StartActivity;

public class LoginActivity extends AppCompatActivity {

    DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");

    private EditText loginField;
    private Button logInButton;
    private TextView incorrectLoginView;
    private ProgressBar loadingBar;

    private final InternetConnection networkChangeListener = new InternetConnection();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        loginField = findViewById(R.id.loginField);
        logInButton = findViewById(R.id.logInButton);
        incorrectLoginView = findViewById(R.id.incorrectLoginView);
        loadingBar = findViewById(R.id.loadingView);

        loginField.addTextChangedListener(loginFieldChangedListener);
        logInButton.setOnClickListener(logInListener);
        findViewById(R.id.anonLogInButton).setOnClickListener(anonLogInListener);
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

    TextWatcher loginFieldChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Context context = LoginActivity.this;
            int buttonColor = s.length() == 0 ? R.color.login_inactive : R.color.login_active;
            int textColor = s.length() == 0 ? R.color.log_inactive_text : R.color.log_active_text;

            incorrectLoginView.setVisibility(View.GONE);
            loginField.setBackgroundTintList(Color.getColorState(context, R.color.login_field));
            logInButton.setEnabled(s.length() != 0);
            logInButton.setBackgroundTintList(Color.getColorState(context, buttonColor));
            logInButton.setTextColor(getResources().getColor(textColor));
        }
    };

    View.OnClickListener anonLogInListener = anonLogInButton -> {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Анонимный вход")
                .setMessage("Результаты тестирований не будут отправлены специалистам.")
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Войти анонимно", (dialog, which) -> startMainActivity("", -1));
        AlertDialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        dialog.show();
    };

    View.OnClickListener logInListener = logInButton -> {
        loadingBar.setVisibility(View.VISIBLE);
        String login = loginField.getText().toString();
        log_in(login, dateOfBirth -> {
            if (!dateOfBirth.equals("null")) {
                startMainActivity(login, Date.getAge(dateOfBirth));
            } else {
                incorrectLoginView.setVisibility(View.VISIBLE);
                loginField.setBackgroundTintList(Color.getColorState(this,R.color.login_incorrect));
                loadingBar.setVisibility(View.INVISIBLE);
            }
        });
    };

    void log_in(String login, final Callback callback) {
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onCallback(String.valueOf(snapshot.child(login).getValue()));
            }
        });
    }

    void startMainActivity(String login, int age) {
        Bundle extras = new Bundle();
        extras.putString("login", login);
        extras.putInt("age", age);
        StartActivity.start(LoginActivity.this, MainActivity.class, extras);
    }

    private interface Callback {
        void onCallback(String value);
    }
}