package com.example.peter.mytodomanager;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;


public class ToDoManagerActivity extends AppCompatActivity {
    private ToDoListAdapter mAdapter;




    // Add a ToDoItem Request Code
    private static final int ADD_TODO_ITEM_REQUEST = 0;
    private static final String FILE_NAME = "TodoManagerActivityData.txt";
    private static final String TAG = "Lab-UserInterface";

    // IDs for menu items
    private static final int MENU_DELETE = Menu.FIRST;
    private static final int MENU_DUMP = Menu.FIRST + 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RecyclerView mRecyclerView;
        RecyclerView.LayoutManager mLayoutManager;

        Bundle b = getIntent().getExtras(); // Get value from priority/deadline sort button
        boolean sortBydeadline = true; // default value
        if(b != null)
            sortBydeadline = b.getBoolean("sort");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdapter = new ToDoListAdapter(getBaseContext(),sortBydeadline);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use a linear layout manager to style recyclerView
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //TODO(DONE) - Attach the adapter to this ListActivity's ListView (replaced with recyclerView)
        mRecyclerView.setAdapter(mAdapter);

        // TODO 5 (DONE) Add two tab buttons to sort recyclerView -
        // Buttons restart the activity with an extra boolean value,
        //this value is passed to ToDoListAdapter which returns the list in the new order
        final Button tabDeadline = (Button) findViewById(R.id.TabDeadline);
        tabDeadline.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ToDoManagerActivity.this, ToDoManagerActivity.class);
                Bundle b = new Bundle();
                b.putBoolean("sort", true); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();
            }
        });
        final Button tabPriority = (Button) findViewById(R.id.TabPriority);
        tabPriority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ToDoManagerActivity.this, ToDoManagerActivity.class);
                Bundle b = new Bundle();
                b.putBoolean("sort", false); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();
            }
        });
        //TODO - Add footerView to ListView(Replaced with recylerView)
        //TODO(Done) - Inflate footerView for footer_view.xml file
        final TextView footerView = (TextView) findViewById(R.id.footerView);

        //TODO(DONE) - Attach Listener to FooterView. Implement onClick()
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("Entered footerView.OnClickListener.onClick()");
                Intent myIntent = new Intent(getBaseContext(), AddToDoActivity.class);
                startActivityForResult(myIntent, ADD_TODO_ITEM_REQUEST);//
            }
        });


    }




   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        log("Entered onActivityResult()");

        // TODO(DONE) - Check result code and request code
        if(requestCode==ADD_TODO_ITEM_REQUEST){

            if(resultCode==RESULT_OK){
                // create new ToDoItem with data
                ToDoItem newItem = new ToDoItem(data);
                Date date = new Date();
                int count = newItem.getTime() - (int)date.getTime();
                //TODO(DONE) 2 - Schedule Notification with item submitted from AddToDoActivity
                scheduleNotification(getBaseContext(), count, newItem.getUniqueID() ,newItem.getTitle()); // Context, delay,ID, Title for Text Content

                // add this item to the adapter
                mAdapter.add(newItem);
            }

            if(resultCode==RESULT_CANCELED){
                // do nothing/ Add LogCat Message
            }
        }
    }
    //TODO schedule notification with alarm five minutes before due
    public void scheduleNotification(Context context, long delay, int notificationId, String title){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("Task Due!")
                .setContentText(title)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        Intent intent = new Intent(context, ToDoListAdapter.class);
        PendingIntent activity = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(activity);

        Notification notification = builder.build();

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + (delay - (5*60*1000));
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }


// Do not modify below here

    @Override
    public void onResume() {
        super.onResume();

        // Load saved ToDoItems, if necessary
        if (mAdapter.getItemCount() == 0)
            loadItems();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save ToDoItems

        saveItems();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, "Delete all");
        menu.add(Menu.NONE, MENU_DUMP, Menu.NONE, "Dump to log");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DELETE:
                mAdapter.clear();
                return true;
            case MENU_DUMP:
                dump();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dump() {

        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            String data = ((ToDoItem) mAdapter.getItem(i)).toLog();
            log("Item " + i + ": " + data.replace(ToDoItem.ITEM_SEP, ","));
        }

    }

    // Load stored ToDoItems
   private void loadItems() {
        BufferedReader reader = null;
        try {
            FileInputStream fis = openFileInput(FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(fis));

            String title = null;
            String priority = null;
            String status = null;
            Date date = null;

            while (null != (title = reader.readLine())) {
                priority = reader.readLine();
                status = reader.readLine();
                date = ToDoItem.FORMAT.parse(reader.readLine());
                mAdapter.add(new ToDoItem(title, ToDoItem.Priority.valueOf(priority),
                        ToDoItem.Status.valueOf(status), date));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Save ToDoItems to file
    private void saveItems() {
        PrintWriter writer = null;
        try {
            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    fos)));

            for (int idx = 0; idx < mAdapter.getItemCount(); idx++) {

                writer.println(mAdapter.getItem(idx));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != writer) {
                writer.close();
            }
        }
    }

    private void log(String msg) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, msg);
    }

}
