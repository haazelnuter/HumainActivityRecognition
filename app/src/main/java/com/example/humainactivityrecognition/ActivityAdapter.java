package com.example.humainactivityrecognition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class ActivityAdapter extends ArrayAdapter<ActivityModel> {

    private Context context;
    private List<ActivityModel> activityList;

    public ActivityAdapter(Context context, List<ActivityModel> activityList) {
        super(context, 0, activityList);
        this.context = context;
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_list_item, parent, false);
        }

        ActivityModel activity = activityList.get(position);

        TextView activityTextView = view.findViewById(R.id.activity_textview);
        TextView dateTextView = view.findViewById(R.id.date_textview);
        TextView timeTextView = view.findViewById(R.id.time_textview);

        activityTextView.setText(activity.getActivity());
        dateTextView.setText(activity.getDate());
        timeTextView.setText(activity.getTime());

        return view;
    }
}

