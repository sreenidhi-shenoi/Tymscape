package com.example.tymscapemain;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
public class AddEventActivity extends AppCompatActivity {
    //Date and Time related variables
    private int mDate, mMonth, mYear;
    private int curHour, curMinute;
    TimePickerDialog timePickerDialog;
    Calendar calendar;
    //String variables
    String ampm;
    String eDate, eTime, eTitle, eDescription, eCategory, ePriority;
    String uid, eid;
    //View related variables
    com.google.android.material.textfield.TextInputLayout nameTxt, descriptionTxt, dateTxt, timeTxt;
    RadioGroup categoryRbg, priorityRbg;
    RadioButton categoryRb, priorityRb;
    ImageView date, time;
    Button create_btn;
    //DB related variables
    private DatabaseReference eventRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //Alarm related variables
    com.google.android.material.checkbox.MaterialCheckBox setNoti;
    int d,m,y,hr,min;
    private int notificationId=1;
    com.google.android.material.textfield.TextInputLayout eventTITLE;

    Calendar cal;
    AlarmManager alarmManager;
    public static int i=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        //Reference to other input widgets
        dateTxt = findViewById(R.id.eventDate);
        timeTxt = findViewById(R.id.eventTime);
        nameTxt = findViewById(R.id.eventTitle);
        descriptionTxt = findViewById(R.id.eventDescription);
        categoryRbg = findViewById(R.id.radioCategory);
        priorityRbg = findViewById(R.id.radioPriority);
        create_btn = findViewById(R.id.addEventBtn);
        //Reference to the calendar and clock icons - to set Click Listeners
        date = findViewById(R.id.datepicker);
        time = findViewById(R.id.timechooser);

        //getting the reference for alarm
        setNoti = findViewById(R.id.setAlarm);

        //Set and Display date
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new
                        DatePickerDialog(AddEventActivity.this,
                        android.R.style.Theme_DeviceDefault_Dialog, new
                        DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                                d=date;
                                m=month;
                                y=year;
                                eDate = "" + date + "/" + (month + 1) + "/" + year;
                                dateTxt.getEditText().setText(eDate);
                            }
                        }, mYear, mMonth, mDate);
                datePickerDialog.show();
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            }
        });
        //Set and Display time
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                curHour = calendar.get(Calendar.HOUR_OF_DAY);
                curMinute = calendar.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(AddEventActivity.this, new
                        TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                                hr=hourOfDay;
                                min=minutes;
                                if (hourOfDay >= 12) {
                                    ampm = "PM";
                                } else {
                                    ampm = "AM";
                                }
                                eTime = "" + hourOfDay + ":" + minutes;
                                timeTxt.getEditText().setText(eTime + " " + ampm);
                            }
                        }, curHour, curMinute, true);
                timePickerDialog.show();
            }
        });


        //Create a new event on click of create button
        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting reference to current user
                mAuth = FirebaseAuth.getInstance();
                currentUser = mAuth.getCurrentUser();
                //Getting reference to the event node
                eventRef = FirebaseDatabase.getInstance().getReference("event");
                //If all fields are entered correctly, set values into model class object and add it to the db
                if(isValidName() & isValidDate() &isValidTime() & isValidCategory() &
                        isValidPriority()) {
                    getValues();
                    uid = currentUser.getUid(); //get the user id of the current user
                    eid = eventRef.push().getKey(); //generate a unique key for the event
                    EventModel eventModel = new EventModel();
                    //Set values for fields of EventModel class
                    eventModel.setEname(eTitle);
                    eventModel.setDescription(eDescription);
                    eventModel.setDate(eDate);
                    eventModel.setTime(eTime);
                    eventModel.setCategory(eCategory);
                    eventModel.setPriority(ePriority);
                    eventModel.setEid(eid);
                    eventModel.setUid(uid);
                    //Enter event details to the database
                    eventRef.child(eid).setValue(eventModel);
                    //if successfully added, then display toast
                    Toast.makeText(AddEventActivity.this, "Event created succesfully!", Toast.LENGTH_SHORT).show();

                    //setting alarm
                    if(setNoti.isChecked())
                    {
                        i++;
                          cal=Calendar.getInstance();
                            eventTITLE=findViewById(R.id.eventTitle);
                            if (Build.VERSION.SDK_INT >= 23)
                            {
                                cal.set(y, m, d, hr, min, 0);
                            }

                            setAlarm(cal.getTimeInMillis());
                    }

                    //Navigate back Home after adding event
                    startActivity(new Intent(AddEventActivity.this, Home.class));
                    finish();
                }
                else{
                    //If all fields are not filled, display error and toast
                    Toast.makeText(AddEventActivity.this, "Couldn't create event!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void setAlarm(long timeInMillis)
    {

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent= new Intent(this, AlarmReceiver.class);
        intent.putExtra("notificationid",notificationId);
        intent.putExtra("message",eventTITLE.getEditText().getText().toString());

        PendingIntent pending_intent= PendingIntent.getBroadcast(this,i,intent,0);
        alarmManager .set(AlarmManager.RTC_WAKEUP, timeInMillis, pending_intent);

        // alarmManager[i].setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, AlarmManager.INTERVAL_DAY,pendingIntent);

        Toast.makeText(this,"Alarm set!",Toast.LENGTH_SHORT).show();
    }

    //Get values from views [Does not include date and time, automatically set when user selects date and time]
    private void getValues() {
        eTitle = nameTxt.getEditText().getText().toString().trim();
        eDescription = descriptionTxt.getEditText().getText().toString().trim();
        categoryRb = findViewById(categoryRbg.getCheckedRadioButtonId());
        eCategory = categoryRb.getText().toString().trim();
        priorityRb = findViewById(priorityRbg.getCheckedRadioButtonId());
        ePriority = priorityRb.getText().toString().trim();
    }
    //Validation methods
    private Boolean isValidName() {
        String name = nameTxt.getEditText().getText().toString();
        if (name.isEmpty()) {
            nameTxt.setError("Field cannot be empty");
            return false;
        } else {
            nameTxt.setError(null);
            return true;
        }
    }
    private Boolean isValidDate() {
        String date = dateTxt.getEditText().getText().toString();
        if (date.isEmpty()) {
            dateTxt.setError("Field cannot be empty");
            return false;
        } else {
            dateTxt.setError(null);
            return true;
        }
    }
    private Boolean isValidTime() {
        String time = timeTxt.getEditText().getText().toString();
        if (time.isEmpty()) {
            timeTxt.setError("Field cannot be empty");
            return false;
        } else {
            timeTxt.setError(null);
            return true;
        }
    }
    private Boolean isValidCategory(){
        //If not radio button is selected, display error on the first button in the group
        RadioButton first = findViewById(R.id.radioHome);
        int id = categoryRbg.getCheckedRadioButtonId();
        if(id <= 0){
            first.setError("Please select a category");
            return false;
        }
        else{
            first.setError(null);
            return true;
        }
    }
    private Boolean isValidPriority(){
        //If not radio button is selected, display error on the first button in the group
        RadioButton first = findViewById(R.id.radioCritical);
        int id = priorityRbg.getCheckedRadioButtonId();
        if(id <= 0){
            first.setError("Please select a category");
            return false;
        }
        else{
            first.setError(null);
            return true;
        }
    }
}

