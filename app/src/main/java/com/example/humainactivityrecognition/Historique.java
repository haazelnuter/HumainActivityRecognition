package com.example.humainactivityrecognition;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class Historique extends AppCompatActivity {
    private ListView listView;
    private ActivityAdapter adapter;
    private List<ActivityModel> activityList;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);
        // use ActionBar utility methods
        ActionBar actionBar = getSupportActionBar();
        // providing title for the ActionBar
        actionBar.setTitle("Historical");

        dbHelper = new DBHelper(this);

        listView = findViewById(R.id.listview);
        activityList = new ArrayList<>();
        adapter = new ActivityAdapter(this, activityList);
        listView.setAdapter(adapter);

        retrieveDataFromSQLite();
    }

    //
    private void retrieveDataFromSQLite() {
        String currentUserEmail = getCurrentUserEmail();

        if (currentUserEmail == null) {
            // Handle the case when the user is not authenticated or not logged in
            return;
        }

        Cursor cursor = dbHelper.retrieveActivityLogs(currentUserEmail);

        if (cursor.moveToFirst()) {
            int activityIndex = cursor.getColumnIndex(DBHelper.COLUMN_ACTIVITY);
            int dateIndex = cursor.getColumnIndex(DBHelper.COLUMN_DATE);
            int timeIndex = cursor.getColumnIndex(DBHelper.COLUMN_TIME);

            do {
                String activity = cursor.getString(activityIndex);
                String date = cursor.getString(dateIndex);
                String time = cursor.getString(timeIndex);

                ActivityModel activityModel = new ActivityModel(activity, date, time);
                activityList.add(activityModel);
            } while (cursor.moveToNext());

            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No activity data found", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }






    private String getCurrentUserEmail() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getEmail();
        } else {
            // Handle the case when the user is not authenticated or not logged in
            return null;
        }
    }
}