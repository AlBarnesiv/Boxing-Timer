package com.example.boxingtimer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private NumberPicker roundsPicker, timePicker, restPicker;
    private Button startButton, pauseButton, resumeButton, stopButton;
    private TextView statusText, roundTimer, currentRound, restLabel, timeLabel, roundsLabel, restText, restTimer;
    private int rounds, roundTime, restTime;
    private CountDownTimer roundCountDownTimer, restCountDownTimer;
    private int currentRoundNumber = 1;
    long timeRemaining;

    // Bools
    private boolean isPaused = false;
    private boolean warningSound = false;
    private boolean fightSound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Layout
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Find Pickers
        roundsPicker = findViewById(R.id.rounds_picker);
        timePicker = findViewById(R.id.time_picker);
        restPicker = findViewById(R.id.rest_picker);

        // Find Buttons
        startButton = findViewById(R.id.start_button);
        pauseButton = findViewById(R.id.pause_button);
        resumeButton = findViewById(R.id.resume_button);
        stopButton = findViewById(R.id.stop_button);

        // Find Text
        statusText = findViewById(R.id.status_text);
        roundTimer = findViewById(R.id.round_timer);
        currentRound = findViewById(R.id.current_round);
        restLabel = findViewById(R.id.rest_label);
        timeLabel = findViewById(R.id.time_label);
        roundsLabel = findViewById(R.id.rounds_label);
        restText = findViewById(R.id.rest_text);
        restTimer = findViewById(R.id.rest_timer);


        // Setting values for the pickers
        restPicker.setDisplayedValues(new String[]{"30", "60", "90", "120"});
        restPicker.setMinValue(1);
        restPicker.setMaxValue(4);
        timePicker.setMinValue(1);
        timePicker.setMaxValue(5);
        roundsPicker.setMinValue(1);
        roundsPicker.setMaxValue(12);

        // Sets Home Screen Visibility
        homeVisibility();


        // Picker Functions
        startButton.setOnClickListener(s -> {
            rounds = roundsPicker.getValue();
            roundTime = timePicker.getValue() * 60 * 1000;
            restTime = restPicker.getValue() * 30 * 1000;
            startRound();

        });

        // Button Functions
        pauseButton.setOnClickListener(pause -> pauseTimer());
        resumeButton.setOnClickListener(resume -> resumeTimer());
        stopButton.setOnClickListener(stop -> resetToSelection());

        // Layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    // Start Round
    private void startRound() {
        statusText.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.GONE);
        restLabel.setVisibility(View.GONE);
        timeLabel.setVisibility(View.GONE);
        roundsLabel.setVisibility(View.GONE);
        roundsPicker.setVisibility(View.GONE);
        timePicker.setVisibility(View.GONE);
        restPicker.setVisibility(View.GONE);
        restText.setVisibility(View.GONE);
        restTimer.setVisibility(View.GONE);

        // Play "Ready Fight" sound
        if (!fightSound) {
            fightSound = true;
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.ready_fight);
            mediaPlayer.start();
        }

        // Starts "Ready Fight" Timer
        statusText.setText("READY");
        new CountDownTimer(2000, 1000) {
            public void onFinish() {
                statusText.setText("FIGHT");
                new CountDownTimer(1000, 1000) {
                    public void onFinish() {
                        startRoundTimer();
                    }

                    public void onTick(long millisUntilFinished) {
                    }
                }.start();
            }

            public void onTick(long millisUntilFinished) {
            }
        }.start();
    }

    // Start Round Timer
    private void startRoundTimer() {
        fightSound = false; // resetting fightSound for next round

        // Setting Round Timer Visibilities
        statusText.setVisibility(View.GONE);
        roundTimer.setVisibility(View.VISIBLE);
        currentRound.setVisibility(View.VISIBLE);
        currentRound.setText("Round " + currentRoundNumber + "/" + rounds);
        pauseButton.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);
        restText.setVisibility(View.GONE);

        // Starting Round Count Down Timer
        roundCountDownTimer = new CountDownTimer(roundTime, 1000) {
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                roundTimer.setText(formatTime(millisUntilFinished));
                if (millisUntilFinished <= 13000 & !warningSound) {
                    warningSound = true;
                    // Play "Warning" sound
                    MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.warning);
                    mediaPlayer.start();
                }
            }

            // End of Round
            public void onFinish() {
                roundTimer.setText("0:00");
                // Play "Ding Ding" sound
                MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.ding_ding);
                mediaPlayer.start();
                warningSound = false;
                if (currentRoundNumber < rounds) {
                    startRestTimer();
                } else {
                    finishSession();
                }
            }
        }.start();
    }

    // Resumeing After a Pause
    private void onResumeTimer() {
        roundCountDownTimer = new CountDownTimer(timeRemaining, 1000) {
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                roundTimer.setText(formatTime(millisUntilFinished));
                if (millisUntilFinished <= 13000 & !warningSound) {
                    warningSound = true;
                    // Play "Warning" sound
                    MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.warning);
                    mediaPlayer.start();
                }
            }

            // Finishes the round when the timer hits 0
            public void onFinish() {
                roundTimer.setText("0:00");
                // Play "Ding Ding" sound
                MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.ding_ding);
                mediaPlayer.start();
                warningSound = false;
                if (currentRoundNumber < rounds) {
                    startRestTimer();
                } else {
                    finishSession();
                }
            }
        }.start();
    }


    // Starts rest timer
    private void startRestTimer() {
        currentRoundNumber++;
        currentRound.setVisibility(View.GONE);
        roundTimer.setVisibility(View.GONE);
        pauseButton.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.GONE);
        restText.setVisibility(View.VISIBLE);
        restTimer.setVisibility(View.VISIBLE);

        restCountDownTimer = new CountDownTimer(restTime, 1000) {
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                restTimer.setText(formatTime(millisUntilFinished));
                if (millisUntilFinished <= 5000) {
                    statusText.setText("Round Starting in: " + millisUntilFinished / 1000);
                    if (millisUntilFinished <= 3000 & !fightSound) {
                        // Play "Ready Fight" sound
                        fightSound = true;
                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.ready_fight);
                        mediaPlayer.start();
                    } else {
                        // Play "Beep" sound
                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.beep);
                        mediaPlayer.start();
                    }
                }
            }

            public void onFinish() {
                restTimer.setText("0:00");
                startRound();
            }
        }.start();
    }

    private void finishSession() { homeVisibility(); }

    private void pauseTimer() {
        isPaused = true;
        roundCountDownTimer.cancel();
    }

    private void resumeTimer() {
        isPaused = false;
        onResumeTimer();
    }

    private void resetToSelection() {
        if (roundCountDownTimer != null) {
            roundCountDownTimer.cancel();
        }
        if (restCountDownTimer != null) {
            restCountDownTimer.cancel();
        }
        homeVisibility();
        currentRoundNumber = 1;
    }

    private String formatTime(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Home Screen Visibility
    private void homeVisibility() {
        roundsPicker.setVisibility(View.VISIBLE);
        timePicker.setVisibility(View.VISIBLE);
        restPicker.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
        roundTimer.setVisibility(View.GONE);
        currentRound.setVisibility(View.GONE);
        restLabel.setVisibility(View.VISIBLE);
        timeLabel.setVisibility(View.VISIBLE);
        roundsLabel.setVisibility(View.VISIBLE);
        restText.setVisibility(View.GONE);
    }
}





