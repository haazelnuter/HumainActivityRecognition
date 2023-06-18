package com.example.humainactivityrecognition;

public class ActivityModel {
    private String activity;
    private String date;
    private String time;

    public ActivityModel(String activity, String date, String time) {
        this.activity = activity;
        this.date = date;
        this.time = time;
    }

    public String getActivity() {
        return activity;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}

