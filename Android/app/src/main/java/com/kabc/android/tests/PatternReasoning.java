package com.kabc.android.tests;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.kabc.android.main.MainActivity;
import com.kabc.android.R;
import com.kabc.android.color.Color;
import com.kabc.android.connection.InternetConnection;
import com.kabc.android.activity.StartActivity;

import java.util.Arrays;
import java.util.Locale;

public class PatternReasoning extends AppCompatActivity {

    private Bundle extras;
    private boolean isAuthorized;

    private boolean isTimeBonuses;
    final private static int[] timeBonuses = {0,0,0,0,0,0,0,0,0,0,10,10,10,10,
            15,15,10,15,15,15,15,15,15,15,20,20,25,25,20,20,20,25,30,45,45,45,45};

    private int level = -1;
    private int startPoint;
    private boolean isTraining = true;

    private ImageView task;

    private final static int NUM_ANSWERS = 6;
    final private static int[] correctAnswers =
            {0,1,2,3,3,1,3,0,2,1,1,5,2,3,1,5,0,4,2,2,4,1,1,0,1,4,1,5,4,2,5,1,0,4,2,3,0};
    private final ShapeableImageView[] answers = new ShapeableImageView[NUM_ANSWERS];
    private final TextView[] letters = new TextView[NUM_ANSWERS];
    private ShapeableImageView selectedView;

    private ImageView resultView;
    private int[] userResults;

    private long timeLeft;
    private TextView timer;
    private CountDownTimer countDownTimer;

    private final Handler handler = new Handler();
    private final InternetConnection networkChangeListener = new InternetConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pattern_reasoning);

        extras = getIntent().getExtras();
        int age = extras.getInt("age");
        isAuthorized = age != -1;
        isTimeBonuses = age >= 7;
        startPoint = !isAuthorized || age >= 5 && age <= 8 ? 1 : 6;
        userResults = new int[correctAnswers.length - startPoint];
        Arrays.fill(userResults, -1);

        task = findViewById(R.id.task);
        timer = findViewById(R.id.timer);
        resultView = findViewById(R.id.resultView);

        findViewById(R.id.homeButton).setOnClickListener(homeButtonListener);
        findViewById(R.id.nextLevelButton).setOnClickListener(nextLevelListener);

        for (int i = 0; i < NUM_ANSWERS; ++i) {
            answers[i] = findViewById(getResources().getIdentifier(
                    "answer" + i, "id", getPackageName()
            ));
            letters[i] = findViewById(getResources().getIdentifier(
                    "letter" + i, "id", getPackageName()
            ));
            answers[i].setOnClickListener(selectView);
        }

        doLevel();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
                || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            handler.postDelayed(this::syncSizes, 100);
        }
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

    View.OnClickListener homeButtonListener = v -> {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выход")
                .setMessage("При выходе все накопленные результаты будут сброшены.")
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Выйти", (dialog, which) ->
                        StartActivity.start(this, MainActivity.class, extras));
        AlertDialog dialog = builder.create();
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        dialog.show();
    };

    View.OnClickListener nextLevelListener = v -> {
        if (selectedView == null) {
            return;
        }

        int score = countScore();
        writeResult(score);
        if (isTraining) {
            makeTraining(score > 0);
        } else {
            if (!isAuthorized) {
                showReaction(score > 0);
            }
            checkBasalRule();
            checkDiscontinueCondition();
        }
    };

    private void doLevel() {
        ++level;
        if (level == 1) {
            level = startPoint;
        }

        if (level - startPoint == 2) {
            isTraining = false;
        }

        if (level < correctAnswers.length) {
            setTask();
            setAnswers();
            if (level == 0) {
                syncSizes();
            }
            setTimer();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Тестирование успешно завершено")
                    .setMessage(isAuthorized ? "Ваши результаты отправлены специалистам." : "")
                    .setPositiveButton("Вернуться в меню", (dialog, which) ->
                            StartActivity.start(this, MainActivity.class, extras));
            AlertDialog dialog = builder.create();
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    private void setTask() {
        task.setImageResource(getResources().getIdentifier(
                String.format(Locale.getDefault(), "pr%02d", level),
                "drawable", getPackageName())
        );
    }

    private void setAnswers() {
        unselectView();
        if (level == 6) {
            answers[4].setVisibility(View.VISIBLE);
            letters[4].setVisibility(View.VISIBLE);
            answers[5].setVisibility(View.VISIBLE);
            letters[5].setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < NUM_ANSWERS - (level < 6 ? 2 : 0); ++i) {
            answers[i].setImageResource(getResources().getIdentifier(
                    String.format(Locale.getDefault(), "pr%02d%c", level, 'a' + i),
                    "drawable", getPackageName())
            );
        }
    }

    private void syncSizes() {
        task.post(() -> {
            int size = task.getMeasuredHeight();
            for (int i = 0; i < NUM_ANSWERS; ++i) {
                answers[i].getLayoutParams().width = size;
                letters[i].getLayoutParams().width = size;
                answers[i].requestLayout();
                letters[i].requestLayout();
            }
        });
    }

    private void setTimer() {
        timeLeft = timeBonuses[level];
        if (timeLeft == 0 || !isTimeBonuses) {
            return;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished / 1000;
                timer.setText(
                        String.format(Locale.getDefault(), "%02d:%02d",
                                (int) timeLeft / 60, (int) timeLeft % 60)
                );
            }
            @Override
            public void onFinish() {
                timeLeft = 0;
                timer.setText(R.string.timer_end);
                resultView.setVisibility(View.VISIBLE);
                resultView.setImageResource(R.drawable.incorrect_answer);
                handler.postDelayed(() -> doLevel(), 300);
            }
        }.start();
    }

    View.OnClickListener selectView = v -> {
        ShapeableImageView view = (ShapeableImageView) v;
        ShapeableImageView selected = selectedView;
        unselectView();
        if (view != selected) {
            view.setStrokeWidth(5);
            selectedView = view;
        }
    };

    private void unselectView() {
        if (selectedView != null) {
            selectedView.setStrokeWidth(0);
            selectedView = null;
        }
    }

    private int countScore() {
        return getAnswerNumber() == correctAnswers[level] ? (timeLeft > 0 ? 2 : 1) : 0;
    }

    private int getAnswerNumber() {
        return java.util.Arrays.asList(answers).indexOf(selectedView);
    }

    private void writeResult(int score) {
        int i = level - startPoint;
        if (level != 0 && userResults[i] == -1) {
            userResults[i] = score;
        }
    }

    private void makeTraining(boolean isCorrect) {
        if (!isCorrect) {
            showCorrectAnswer();
        } else {
            showReaction(true);
            doLevel();
        }
    }

    private void showCorrectAnswer() {
        int correctAnswerIndex = correctAnswers[level];

        selectedView.setStrokeWidth(5);
        answers[correctAnswerIndex].setStrokeWidth(5);
        selectedView.setStrokeColor(Color.getColorState(this, R.color.red));
        answers[correctAnswerIndex].setStrokeColor(Color.getColorState(this, R.color.green));

        handler.postDelayed(() -> {
            selectedView.setStrokeWidth(0);
            answers[correctAnswerIndex].setStrokeWidth(0);
            selectedView.setStrokeColor(Color.getColorState(this, R.color.black));
            answers[correctAnswerIndex].setStrokeColor(Color.getColorState(this, R.color.black));
        }, 500);
    }

    private void showReaction(boolean isCorrect) {
        resultView.setImageResource(isCorrect ?
                R.drawable.correct_answer : R.drawable.incorrect_answer);
        resultView.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> resultView.setVisibility(View.INVISIBLE), 300);
    }

    private void checkBasalRule() {
        if (level == startPoint + 2 && countIncorrect(3) != 0) {
            level = 0;
            Arrays.fill(userResults, -1);
        }
    }

    private void checkDiscontinueCondition() {
        boolean discontinueCondition = level >= startPoint + 4 && countIncorrect(5) == 4;
        if (discontinueCondition) {
            StartActivity.start(this, MainActivity.class, extras);
        } else {
            doLevel();
        }
    }

    private int countIncorrect(int last) {
        int numberOfIncorrect = 0;
        for (int i = last - 1; i >= 0; --i) {
            if (userResults[i] == 0) {
                ++numberOfIncorrect;
            }
        }
        return numberOfIncorrect;
    }
}