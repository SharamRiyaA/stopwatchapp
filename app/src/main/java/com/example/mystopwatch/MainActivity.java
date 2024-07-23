package com.example.mystopwatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView tvTime;
    private Button btnStart, btnStop, btnReset;
    private ListView listSavedTimes;
    private ArrayAdapter<String> savedTimesAdapter;
    private ArrayList<String> savedTimesList;

    private Handler handler;
    private Runnable runnable;
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long updatedTime = 0L;
    private int seconds, minutes, milliseconds;
    private boolean isRunning = false;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "StopwatchPrefs";
    private static final String KEY_SAVED_TIMES = "SavedTimes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        tvTime = findViewById(R.id.tvTime);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnReset = findViewById(R.id.btnReset);
        listSavedTimes = findViewById(R.id.listSavedTimes);

        // Initialize adapter for ListView
        savedTimesList = new ArrayList<>();
        savedTimesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedTimesList);
        listSavedTimes.setAdapter(savedTimesAdapter);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadSavedTimes();

        // Initialize handler for stopwatch
        handler = new Handler();

        // Start button click listener
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRunning) {
                    startTime = System.currentTimeMillis() - timeInMilliseconds;
                    handler.postDelayed(runnable, 0);
                    isRunning = true;
                }
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnReset.setEnabled(true);
            }
        });

        // Stop button click listener
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeInMilliseconds = System.currentTimeMillis() - startTime;
                handler.removeCallbacks(runnable);
                isRunning = false;

                seconds = (int) (timeInMilliseconds / 1000);
                minutes = seconds / 60;
                seconds = seconds % 60;
                milliseconds = (int) (timeInMilliseconds % 1000);

                final String time = String.format("%02d:%02d:%03d", minutes, seconds, milliseconds);

                // Show AlertDialog to save the time
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Save Time");
                builder.setMessage("Do you want to save this time?");
                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSaveDialog(time);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();

                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnReset.setEnabled(true);
            }
        });

        // Reset button click listener
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                startTime = 0L;
                timeInMilliseconds = 0L;
                updatedTime = 0L;
                tvTime.setText("00:00:000");
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnReset.setEnabled(false);
                isRunning = false;
            }
        });

        // Runnable for updating stopwatch time
        runnable = new Runnable() {
            public void run() {
                timeInMilliseconds = System.currentTimeMillis() - startTime;
                updatedTime = timeInMilliseconds;

                seconds = (int) (updatedTime / 1000);
                minutes = seconds / 60;
                seconds = seconds % 60;
                milliseconds = (int) (updatedTime % 1000);

                tvTime.setText(String.format("%02d:%02d:%03d", minutes, seconds, milliseconds));

                handler.postDelayed(this, 10);
            }
        };

        // Long-click listener for deleting saved times
        listSavedTimes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // Show a confirmation dialog to delete the selected time
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Delete Time");
                builder.setMessage("Do you want to delete this time?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savedTimesList.remove(position);
                        savedTimesAdapter.notifyDataSetChanged();
                        saveTimes();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                return true;
            }
        });
    }

    // Method to show dialog for saving time with a name
    private void showSaveDialog(final String time) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Time");
        builder.setMessage("Enter a name for this time record:");

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String recordName = input.getText().toString().trim();
                if (!recordName.isEmpty()) {
                    savedTimesList.add(recordName + " - " + time);
                    savedTimesAdapter.notifyDataSetChanged();
                    saveTimes();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    // Save the list of saved times to SharedPreferences
    private void saveTimes() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> set = new HashSet<>(savedTimesList);
        editor.putStringSet(KEY_SAVED_TIMES, set);
        editor.apply();
    }

    // Load the list of saved times from SharedPreferences
    private void loadSavedTimes() {
        Set<String> set = sharedPreferences.getStringSet(KEY_SAVED_TIMES, new HashSet<String>());
        savedTimesList.clear();
        savedTimesList.addAll(set);
        savedTimesAdapter.notifyDataSetChanged();
    }
}
