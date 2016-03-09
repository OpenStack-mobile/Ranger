package com.andressantibanez.android.rangerexample;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.andressantibanez.ranger.Ranger;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Ranger ranger = (Ranger) findViewById(R.id.listener_ranger);
        ranger.setDayViewOnClickListener(new Ranger.DayViewOnClickListener() {
            @Override
            public void onDaySelected(DateTime date) {
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, "Seleted date: " + date, Snackbar.LENGTH_SHORT).show();
            }
        });

        DateTime startDate = new DateTime(new Date()).withTime(0, 0, 0, 0);
        DateTime endDate = startDate.plusDays(5).withTime(23, 59, 59, 999);

        final DateTime startDateTime = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0, 0, 0);
        final DateTime endDateTime = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23, 59, 59);

        ArrayList<DateTime> disabledDates = new ArrayList<>();
        disabledDates.add(startDateTime);
        disabledDates.add(startDateTime.plusDays(3));

        ranger.setStartAndEndDateWithDisabledDates(startDateTime, endDateTime, disabledDates);

        Button changeDisableDatesButton = (Button)findViewById(R.id.change_disable_dates_button);
        changeDisableDatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<DateTime> disabledDates = new ArrayList<>();
                disabledDates.add(startDateTime.plusDays(1));
                disabledDates.add(startDateTime.plusDays(3));
                ranger.setDisabledDates(disabledDates);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
