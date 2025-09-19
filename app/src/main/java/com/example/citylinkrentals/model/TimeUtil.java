package com.example.citylinkrentals.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.widget.TextView;

public class TimeUtil {

    public static void setRelativeTime(TextView timeText, Property property) {
        String dateText = "Posted recently";

        if (property.getCreatedAt() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date createdDate = inputFormat.parse(property.getCreatedAt());

                if (createdDate != null) {
                    long diffInMillies = System.currentTimeMillis() - createdDate.getTime();
                    long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    long diffInHours = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);

                    if (diffInDays > 0) {
                        dateText = "Posted " + diffInDays + " day" + (diffInDays > 1 ? "s" : "") + " ago";
                    } else if (diffInHours > 0) {
                        dateText = "Posted " + diffInHours + " hour" + (diffInHours > 1 ? "s" : "") + " ago";
                    } else if (diffInMinutes > 0) {
                        dateText = "Posted " + diffInMinutes + " minute" + (diffInMinutes > 1 ? "s" : "") + " ago";
                    } else {
                        dateText = "Posted just now";
                    }
                }
            } catch (ParseException e) {
                dateText = "Posted recently";
            }
        }
        timeText.setText(dateText);
    }
}
