package com.kabc.android.tests;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabc.android.main.MainActivity;
import com.kabc.android.R;
import com.kabc.android.connection.InternetConnection;
import com.kabc.android.activity.StartActivity;

import java.util.Arrays;
import java.util.Locale;

public class ConceptualThinking extends AppCompatActivity {

    private Bundle extras;
    private boolean isAuthorized;

    private int level = -1;
    private int startPoint;
    private boolean isTraining = true;

    private TextView taskText;

    private static final int NUM_ANSWERS = 5;
    private static final int[] correctAnswers =
            {1,1,0,1,2,2,0,3,3,1,0,1,0,2,4,0,0,2,4,0,3,0,1,1,3,1,2,2,3};
    private static final ImageButton[] answers = new ImageButton[NUM_ANSWERS];
    private ImageButton selectedView;

    private ImageView resultView;
    private int[] userResults;

    private final Handler handler = new Handler();
    private final InternetConnection networkChangeListener = new InternetConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conceptual_thinking);

        extras = getIntent().getExtras();
        int age = extras.getInt("age");
        isAuthorized = age != -1;
        startPoint = !isAuthorized || age >= 3 && age <= 5 ? 1 : 6;
        userResults = new int[correctAnswers.length - startPoint];
        Arrays.fill(userResults, -1);

        taskText = findViewById(R.id.taskText);
        resultView = findViewById(R.id.resultView);

        findViewById(R.id.homeButton).setOnClickListener(homeButtonListener);
        findViewById(R.id.nextLevelButton).setOnClickListener(nextLevelListener);

        for (int i = 0; i < NUM_ANSWERS; ++i) {
            answers[i] = findViewById(getResources().getIdentifier(
                    "answer" + i, "id", getPackageName()
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
            setAnswers();
            if (level >= 14 && level <= 16) {
                syncSizes();
            }
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

    private void setAnswers() {
        unselectView();
        if (level == 14 || level == 16) {
            answers[0].setVisibility(View.VISIBLE);
        } else if (level == 15) {
            answers[0].setVisibility(View.GONE);
        }
        for (int i = level <= 13 || level == 15 ? 1 : 0; i < answers.length; ++i) {
            answers[i].setImageResource(getResources().getIdentifier(
                    String.format(Locale.getDefault(), "ct%02d%c", level, 'a' + i),
                    "drawable", getPackageName())
            );
        }
    }

    private void syncSizes() {
        answers[1].post(() -> {
            int width = answers[1].getMeasuredWidth();
            int height = answers[1].getMeasuredHeight();
            for (int i = 3; i < NUM_ANSWERS; ++i) {
                answers[i].getLayoutParams().width = width;
                answers[i].getLayoutParams().height = height;
                answers[i].requestLayout();
            }
        });
    }

    View.OnClickListener selectView = v -> {
        ImageButton view = (ImageButton) v;
        ImageButton selected = selectedView;
        unselectView();
        if (view != selected) {
            view.setBackgroundResource(R.drawable.ct_select_background);
            selectedView = view;
        }
    };

    private void unselectView() {
        if (selectedView != null) {
            selectedView.setBackgroundResource(0);
            selectedView = null;
        }
    }

    private int countScore() {
        return getAnswerNumber() == correctAnswers[level] ? 1 : 0;

    }

    private int getAnswerNumber() {
        int answerNumber = java.util.Arrays.asList(answers).indexOf(selectedView);
        if (level < 14 || level == 15) {
            --answerNumber;
        }
        return answerNumber;
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
        int correctAnswerIndex = correctAnswers[level] + 1;

        selectedView.setBackgroundResource(R.drawable.ct_incorrect_background);
        answers[correctAnswerIndex].setBackgroundResource(R.drawable.ct_correct_background);

        handler.postDelayed(() -> {
            selectedView.setBackgroundResource(0);
            answers[correctAnswerIndex].setBackgroundResource(0);
        }, 500);
    }

    private void showReaction(boolean isCorrect) {
        taskText.setVisibility(View.INVISIBLE);
        resultView.setImageResource(isCorrect ?
                R.drawable.correct_answer : R.drawable.incorrect_answer);
        resultView.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> {
            taskText.setVisibility(View.VISIBLE);
            resultView.setVisibility(View.INVISIBLE);
        }, 300);
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