package com.kabc.android.tests;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.kabc.android.R;
import com.kabc.android.main.MainActivity;
import com.kabc.android.connection.InternetConnection;
import com.kabc.android.activity.StartActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class StoryCompletion extends AppCompatActivity {

    private Bundle extras;
    private boolean isAuthorized;

    private boolean isTimeBonuses;
    private final static int[][] timeLimits = {
            {5},{30},{30},{30},{30},{30},{45},{30},{15,25,60},{20,30,60},{20,30,60},{20,30,45},
            {20,30,60},{20,35,90},{25,45,120},{25,45,120},{25,45,120},{30,45,120},{30,50,120}
    };

    private final static int EMPTY_PIC_INDEX = -1;
    private final static int[] backgroundColors = {
            R.color.blue, R.color.yellow, R.color.yellow, R.color.purple, R.color.blue,
            R.color.blue, R.color.blue, R.color.yellow, R.color.blue, R.color.yellow,
            R.color.blue, R.color.blue, R.color.grey, R.color.pink, R.color.blue,
            R.color.blue, R.color.blue, R.color.yellow, R.color.orange
    };

    private int level = -1;
    private int startPoint;
    private boolean isTraining = true;

    private final static int MAX_TASKS = 6;
    private final static int[] numTasks  =  {3,3,3,3,4,4,5,4,4,5,6,5,5,6,6,6,6,6,6};
    private final static int[][] activeTaskIndices = {
            {1}, {2}, {2}, {1}, {1,2}, {2}, {0,2,4}, {0,2}, {0,2,3}, {0,3,4}, {0,1,4}, {2,4},
            {0,3,4}, {0,3,4,5}, {1,2,3,4}, {0,1,2,4}, {1,2,3,5}, {1,2,4,5}, {1,2,3,4}
    };
    private final ImageView[] tasks = new ImageView[MAX_TASKS];
    private ArrayList<ImageView> activeTasks;

    private final static int MAX_ANSWERS = 6;
    private final static int[] numAnswers = {3,3,3,3,3,3,4,4,4,5,4,4,5,6,6,6,6,6,6};
    private final static Integer[][] correctAnswers = {
            {1}, {2}, {1}, {0}, {2,0}, {1}, {0,1,3}, {1,0}, {3,2,0}, {3,1,4}, {0,2,1}, {1,3},
            {2,1,0}, {0,2,5,4}, {0,5,3,2}, {1,4,2,5}, {5,3,4,1}, {1,0,5,2}, {3,0,5,1}
    };
    private final ImageView[] answers = new ImageView[MAX_ANSWERS];
    private ImageView selectedView;

    private int[] userResults;
    private ImageView resultView;

    private long timeLeft;
    private TextView timer;
    private CountDownTimer countDownTimer;

    private final Handler handler = new Handler();
    private final InternetConnection networkChangeListener = new InternetConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_completion);

        extras = getIntent().getExtras();
        int age = extras.getInt("age");
        isAuthorized = age != -1;
        isTimeBonuses = age >= 7;
        startPoint = !isAuthorized || age >= 6 && age <= 8 ? 1 : (age >= 9 && age <= 13 ? 4 : 8);
        userResults = new int[correctAnswers.length - startPoint];
        Arrays.fill(userResults, -1);

        timer = findViewById(R.id.timer);
        resultView = findViewById(R.id.resultView);

        findViewById(R.id.homeButton).setOnClickListener(homeButtonListener);
        findViewById(R.id.nextLevelButton).setOnClickListener(nextLevelListener);
        findViewById(R.id.mainLayout).setOnClickListener(v -> unselectView());
        findViewById(R.id.answerLayout).setOnDragListener(answerLayoutDragListener);

        for (int i = 0; i < MAX_TASKS; ++i) {
            tasks[i] = findViewById(getResources().getIdentifier(
                    "task" + i, "id", getPackageName()
            ));
            tasks[i].setOnClickListener(taskClickListener);
            tasks[i].setOnLongClickListener(taskLongClickListener);
            tasks[i].setOnDragListener(taskDragListener);
        }
        for (int i = 0; i < MAX_ANSWERS; ++i) {
            answers[i] = findViewById(getResources().getIdentifier(
                    "answer" + i, "id", getPackageName()
            ));
            answers[i].setOnClickListener(selectView);
            answers[i].setOnLongClickListener(answerLongClickListener);
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
        unselectView();
        if (!isEnoughFilled()) {
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

    View.OnClickListener taskClickListener = view -> {
        ImageView task = (ImageView) view;
        if (!activeTasks.contains(task)) {
            unselectView();
            return;
        }

        int answerIndex = getImageIndex(task);
        if (answerIndex == EMPTY_PIC_INDEX && selectedView == null) {
            return;
        }

        if (selectedView == null) {
            answers[answerIndex].setVisibility(View.VISIBLE);
            setDrawableByIndex(task, EMPTY_PIC_INDEX);
        } else {
            setDrawableByIndex(task, getImageIndex(selectedView));
            selectedView.setVisibility(View.INVISIBLE);
            unselectView();
            if (answerIndex != EMPTY_PIC_INDEX) {
                answers[answerIndex].setVisibility(View.VISIBLE);
            }
        }
    };

    View.OnLongClickListener taskLongClickListener = view -> {
        ImageView task = (ImageView) view;
        if (!activeTasks.contains(task) || getImageIndex(task) == EMPTY_PIC_INDEX) {
            return false;
        }
        task.setBackgroundResource(0);
        startDrag(task);
        task.setImageResource(R.drawable.sc_empty);
        task.setBackgroundResource(R.drawable.sc_task_background);
        return true;
    };

    View.OnLongClickListener answerLongClickListener = view -> {
        startDrag(view);
        view.setVisibility(View.INVISIBLE);
        return true;
    };

    View.OnDragListener taskDragListener = (view, event) -> {
        ImageView task = (ImageView) view;
        if (!activeTasks.contains(task)) {
            return false;
        }
        int action = event.getAction();
        int previousIndex = getImageIndex(task);
        int previousDrawable = getDrawableByIndex(previousIndex);

        ImageView draggedView = (ImageView) event.getLocalState();
        if (draggedView != task) {
            int newIndex = getImageIndex(draggedView);
            int newDrawable = getDrawableByIndex(newIndex);
            boolean isDraggedTask = activeTasks.contains(draggedView);

            switch (action) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    task.setImageResource(newDrawable);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    task.setImageResource(previousDrawable);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        if (isDraggedTask) {
                            draggedView.setImageResource(newDrawable);
                        } else {
                            draggedView.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case DragEvent.ACTION_DROP:
                    task.setTag(newIndex);
                    if (previousIndex != EMPTY_PIC_INDEX) {
                        answers[previousIndex].setVisibility(View.VISIBLE);
                    }
                    if (isDraggedTask) {
                        draggedView.setTag(EMPTY_PIC_INDEX);
                    }
            }
        } else if (action == DragEvent.ACTION_DROP
                || action == DragEvent.ACTION_DRAG_ENDED && !event.getResult()) {
            task.setImageResource(previousDrawable);
        }
        return true;
    };

    View.OnDragListener answerLayoutDragListener = (layout, event) -> {
        ImageView draggedView = (ImageView) event.getLocalState();
        if (!activeTasks.contains(draggedView)) {
            return false;
        }

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_ENTERED:
                answers[getImageIndex(draggedView)].setVisibility(View.VISIBLE);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                answers[getImageIndex(draggedView)].setVisibility(View.INVISIBLE);
                break;
            case DragEvent.ACTION_DROP:
                draggedView.setTag(EMPTY_PIC_INDEX);
        }
        return true;
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
            setTasks();
            setAnswers();
            setTimer();
            syncSizes();
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

    private void setTasks() {
        findViewById(R.id.taskLayout).setBackgroundResource(backgroundColors[level]);
        activeTasks = new ArrayList<>(activeTaskIndices[level].length);

        for (int taskIndex : activeTaskIndices[level]) {
            activeTasks.add(tasks[taskIndex]);
        }
        for (int i = 0; i < numTasks[level]; ++i) {
            tasks[i].setVisibility(View.VISIBLE);
            if (activeTasks.contains(tasks[i])) {
                setDrawableByIndex(tasks[i], EMPTY_PIC_INDEX);
                tasks[i].setBackgroundResource(R.drawable.sc_task_background);
            } else {
                tasks[i].setBackgroundResource(0);
                tasks[i].setImageResource(getResources().getIdentifier(
                        String.format(Locale.getDefault(), "sc%02dt%d", level, i),
                        "drawable", getPackageName())
                );
                tasks[i].setBackgroundResource(0);
            }
        }
        for (int i = numTasks[level]; i < MAX_TASKS; ++i) {
            tasks[i].setVisibility(View.GONE);
        }
    }

    private void setAnswers() {
        unselectView();
        for (int i = 0; i < numAnswers[level]; ++i) {
            answers[i].setVisibility(View.VISIBLE);
            setDrawableByIndex(answers[i], i);
        }
        for (int i = numAnswers[level]; i < MAX_ANSWERS; ++i) {
            answers[i].setVisibility(View.GONE);
        }
    }

    private void syncSizes() {
        if (numTasks[level] > numAnswers[level]) {
            tasks[0].post(() -> {
                int width = tasks[0].getMeasuredWidth();
                int height = tasks[0].getMeasuredHeight();
                for (ImageView answer : answers) {
                    answer.getLayoutParams().width = width;
                    answer.getLayoutParams().height = height;
                    answer.requestLayout();
                }
            });
        } else {
            for (ImageView answer : answers) {
                answer.getLayoutParams().width = TableRow.LayoutParams.MATCH_PARENT;
                answer.getLayoutParams().height = TableRow.LayoutParams.MATCH_PARENT;
                answer.requestLayout();
            }
        }
    }

    private void setTimer() {
        timeLeft = timeLimits[level][timeLimits[level].length - 1];
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

                writeResult(countScore());
                if (!isTraining) {
                    doLevel();
                }
            }
        }.start();
    }

    View.OnClickListener selectView = v -> {
        ImageView view = (ImageView) v;
        ImageView selected = selectedView;
        unselectView();
        if (view != selected) {
            view.setBackgroundResource(R.drawable.sc_select_background);
            selectedView = view;
        }
    };

    private void unselectView() {
        if (selectedView != null) {
            selectedView.setBackgroundResource(0);
            selectedView = null;
        }
    }

    private void startDrag(View view) {
        unselectView();
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(view);
        view.startDrag(data, dragShadowBuilder, view, 0);
    }

    private int getDrawableByIndex(int index) {
        return index == EMPTY_PIC_INDEX ? R.drawable.sc_empty : getResources().getIdentifier(
                String.format(Locale.getDefault(), "sc%02da%d", level, index),
                "drawable", getPackageName()
        );
    }

    private void setDrawableByIndex(ImageView view, int index) {
        view.setImageResource(getDrawableByIndex(index));
        view.setTag(index);
    }

    private int getImageIndex(ImageView view) {
        return (Integer) view.getTag();
    }

    private boolean isEnoughFilled() {
        int numberOfEmpty = 0;
        for (ImageView activeTask : activeTasks) {
            if (activeTask != null && getImageIndex(activeTask) == EMPTY_PIC_INDEX) {
                ++numberOfEmpty;
            }
            if (level < 12 && numberOfEmpty > 0 || level >= 12 && numberOfEmpty > 1)
                return false;
        }
        return true;
    }

    private void writeResult(int score) {
        int i = level - startPoint;
        if (level != 0 && userResults[i] == -1) {
            userResults[i] = score;
        }
    }

    private int countScore() {
        int incorrectAnswers = countIncorrectAnswers();
        return countBasicScore(incorrectAnswers) + countTimeBonuses(incorrectAnswers == 0);
    }

    private int countIncorrectAnswers() {
        int incorrectCount = 0;
        for (int i = 0; i < activeTasks.size(); ++i) {
            if (activeTasks.get(i) != null) {
                if (getImageIndex(activeTasks.get(i)) != correctAnswers[level][i]) {
                    ++incorrectCount;
                }
            }
        }
        return incorrectCount;
    }

    private int countBasicScore(int incorrectAnswers) {
        if (timeLeft == 0 && !isTraining) {
            return 0;
        }

        int basicScore;
        if (level < 12) {
            basicScore = incorrectAnswers == 0 ? 1 : 0;
        } else {
            basicScore = incorrectAnswers <= 1 ? (incorrectAnswers == 0 ? 2 : 1) : 0;
        }
        return basicScore;
    }

    private int countTimeBonuses(boolean isCorrect) {
        int timeBonuses = 0;
        if (isTimeBonuses && timeLeft > 0 && isCorrect && timeLimits[level].length == 3) {
            int timePassed = timeLimits[level][2] - (int) timeLeft;
            if (timePassed <= timeLimits[level][1]) {
                ++timeBonuses;
            }
            if (timePassed <= timeLimits[level][0]) {
                ++timeBonuses;
            }
        }
        return timeBonuses;
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
        for (int i = 0; i < activeTasks.size(); ++i) {
            if (activeTasks.get(i) != null) {
                if (getImageIndex(activeTasks.get(i)) == correctAnswers[level][i]) {
                    activeTasks.get(i).setBackgroundResource(R.drawable.sc_correct_background);
                    activeTasks.set(i, null);
                } else {
                    activeTasks.get(i).setBackgroundResource(R.drawable.sc_incorrect_background);
                }
            }
        }

        handler.postDelayed(() -> {
            for (ImageView activeTask : activeTasks) {
                if (activeTask != null) {
                    answers[getImageIndex(activeTask)].setVisibility(View.VISIBLE);
                    setDrawableByIndex(activeTask, EMPTY_PIC_INDEX);
                    activeTask.setBackgroundResource(R.drawable.sc_task_background);
                }
            }
        }, 700);
    }

    private void showReaction(boolean isCorrect) {
        resultView.setImageResource(isCorrect ?
                R.drawable.correct_answer : R.drawable.incorrect_answer);
        resultView.setVisibility(View.VISIBLE);
        handler.postDelayed(() -> resultView.setVisibility(View.INVISIBLE), 300);
    }

    private void checkBasalRule() {
        if (level == startPoint + 2 && countIncorrectInLastThree() != 0) {
            level = 0;
            Arrays.fill(userResults, -1);
        }
    }

    private void checkDiscontinueCondition() {
        boolean discontinueCondition = level >= startPoint + 2 && countIncorrectInLastThree() == 3;
        if (discontinueCondition) {
            StartActivity.start(this, MainActivity.class, extras);
        } else {
            doLevel();
        }
    }

    private int countIncorrectInLastThree() {
        int numberOfIncorrect = 0;
        for (int i = 2; i >= 0; --i) {
            if (userResults[i] == 0) {
                ++numberOfIncorrect;
            }
        }
        return numberOfIncorrect;
    }
}