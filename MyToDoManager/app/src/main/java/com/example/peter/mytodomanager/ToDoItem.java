package com.example.peter.mytodomanager;


import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ToDoItem { //implements Comparable<ToDoItem>

    public static final String ITEM_SEP = System.getProperty("line.separator");
    String title;

    public enum Priority {
        HIGH, MED, LOW
    };

    public enum Status {
        NOTDONE, DONE
    };

    public final static String TITLE = "title";
    public final static String PRIORITY = "priority";
    public final static String STATUS = "status";
    public final static String DATE = "date";
    public final static String FILENAME = "filename";



    public final static SimpleDateFormat FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    private String mTitle = new String();
    private Priority mPriority = Priority.LOW;
    private Status mStatus = Status.NOTDONE;
    private Date mDate = new Date();
    private int uniqueID;

    ToDoItem(String title, Priority priority, Status status, Date date) {
        this.mTitle = title;
        this.mPriority = priority;
        this.mStatus = status;
        this.mDate = date;
        this.uniqueID = UUID.randomUUID().variant();//TODO 4(DONE)- Set unique ID for each task
    }

    // Create a new ToDoItem from data packaged in an Intent

    ToDoItem(Intent intent) {

        mTitle = intent.getStringExtra(ToDoItem.TITLE);
        mPriority = Priority.valueOf(intent.getStringExtra(ToDoItem.PRIORITY));
        mStatus = Status.valueOf(intent.getStringExtra(ToDoItem.STATUS));

        try {
            mDate = ToDoItem.FORMAT.parse(intent.getStringExtra(ToDoItem.DATE));
        } catch (ParseException e) {
            mDate = new Date();
        }
    }

    public int getUniqueID()
    {
        return uniqueID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(Priority priority) {
        mPriority = priority;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    public Date getDate() {
        return mDate;
    }

    public int getTime() {
        return (int) mDate.getTime();
    }
    public void setDate(Date date) {
        mDate = date;
    }

    // Take a set of String data values and
    // package them for transport in an Intent

    public static void packageIntent(Intent intent, String title,
                                     Priority priority, Status status, String date) {

        intent.putExtra(ToDoItem.TITLE, title);
        intent.putExtra(ToDoItem.PRIORITY, priority.toString());
        intent.putExtra(ToDoItem.STATUS, status.toString());
        intent.putExtra(ToDoItem.DATE, date);

    }

    public String toString() {
        return mTitle + ITEM_SEP + mPriority + ITEM_SEP + mStatus + ITEM_SEP
                + FORMAT.format(mDate);
    }

    public String toLog() {
        return "Title:" + mTitle + ITEM_SEP + "Priority:" + mPriority
                + ITEM_SEP + "Status:" + mStatus + ITEM_SEP + "Date:"
                + FORMAT.format(mDate);
    }

   /* public int compareTo(ToDoItem compareItem) {

        int compareQuantity = ((ToDoItem) compareItem).getTime();

        //ascending order
        return  this.getTime() - compareQuantity;
        //descending order
        //return compareQuantity - this.quantity;
    }*/
}
