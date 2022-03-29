package com.example.tymscapemain;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ManageNotification extends AppCompatActivity {


    com.google.android.material.textfield.TextInputLayout eventTitle;

    public void alarmON(int y,int m, int d, int hr, int min)
    {
        Calendar calendar = Calendar.getInstance();

        if (Build.VERSION.SDK_INT >= 23)
        {
            calendar.set(y, m, d, hr, min, 0);

        }

        setAlarm(calendar.getTimeInMillis());
    }


    private void setAlarm(long timeInMillis)
    {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("message", eventTitle.getEditText().getText());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
        alarmManager.setRepeating(AlarmManager.RTC, timeInMillis, AlarmManager.INTERVAL_DAY,pendingIntent);

        Toast.makeText(this,"Alarm set!",Toast.LENGTH_SHORT).show();
    }


}

