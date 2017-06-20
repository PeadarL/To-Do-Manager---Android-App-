package com.example.peter.mytodomanager;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static android.content.ContentValues.TAG;


public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ViewHolder>{

    // List of ToDoItems
    private ArrayList<ToDoItem> mItems = new ArrayList<ToDoItem>();
    private Context context;
    private LayoutInflater inflater;
    private boolean sortByDeadline;
    private static final String TAG = "Lab-UserInterface";




    public ToDoListAdapter(Context context,  boolean sortByDeadline )
    {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.sortByDeadline = sortByDeadline;
    }

    @Override
    public ToDoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //TODO(DONE) - Inflate the View for this ToDoItem
        View v = inflater.inflate(R.layout.todo_item, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ToDoListAdapter.ViewHolder holder, final int position) {
        sort(sortByDeadline);//TODO 5 (DONE) Call sort method using constructor parametre

        final ToDoItem current = mItems.get(position);//TODO(DONE) - Get the current ToDoItem
        //TODO(DONE) - Fill in specific ToDoItem data
        holder.title.setText(current.getTitle());//TODO(DONE) - Display Title in TextView
        holder.title.setTextColor(Color.parseColor("white"));
        holder.priority.setText(current.getPriority().toString());//TODO(DONE) - Display Priority in a TextView
        holder.date.setText((current.FORMAT.format(current.getDate())));// TODO(DONE) - Display Time and Date.
        holder.checkBox.setChecked(current.getStatus()== ToDoItem.Status.DONE);//TODO(DONE) - Set up Status CheckBox


        // TODO 2(DONE) - Set Warning (TextColour) if Task Deadline Approaching
        checkTime(current,holder);

        //TODO 1 (DONE) - Set titleView background colour(Seni-Magenta/Cyan according to status.
        if(current.getStatus()== ToDoItem.Status.NOTDONE)
        {holder.title.setBackgroundColor(Color.parseColor("#80e600e6")); // Magenta NotDone
        }
        else
        {
            holder.title.setBackgroundColor(Color.parseColor("#8000e6e6")); //Cyan Done
        }


        // TODO 3 (DONE) - Set Spinner for priority selection
        ArrayAdapter<CharSequence> adapter;
        adapter = ArrayAdapter.createFromResource(holder.itemView.getContext(),R.array.priority_Spinner,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinner.setAdapter(adapter);
        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int place, long id) {
                //Spinner will attempt to reset each priority to position 0(High) every time
                //the view refreshes. To avoid this, if the priority passed in is at position 0 the
                // spinner will do nothing. To accomodate this postion 0 is replaced with "Options:"
                //and high moved to position 1 etc.
                if(place != 0)
                {
                    holder.priority.setText(parent.getItemAtPosition(place).toString());
                    current.setPriority(ToDoItem.Priority.valueOf(parent.getItemAtPosition(place).toString()));// If it crashes drop this
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //TODO 1 (DONE) - Chenge colour on checkbox status change
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                log("Entered onCheckedChanged()");
                if(isChecked){
                    current.setStatus(ToDoItem.Status.DONE);
                    holder.checkBox.setChecked(true);
                    holder.title.setBackgroundColor(Color.parseColor("#8000e6e6"));
                }
                else{
                    current.setStatus(ToDoItem.Status.NOTDONE);
                    holder.checkBox.setChecked(false);
                    holder.title.setBackgroundColor(Color.parseColor("#80e600e6"));
                }
            }
        });

        //TODO 4 (DONE) - Delete on longclick, vibrate, alert dialog, delete pending notification
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                Vibrator vibrator = (Vibrator) v.getRootView().getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                new AlertDialog.Builder( v.getRootView().getContext())
                        .setTitle( "Delete Task" )
                        .setMessage( "Are you sure you want to remove this task?" )
                        .setPositiveButton( "Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mItems.remove(position);
                                checkTime(current,holder);
                                Intent intent = new Intent(context, NotificationPublisher.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                        current.getUniqueID(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                                am.cancel(pendingIntent);
                                // Cancel the `PendingIntent` after alarm cancelled
                                pendingIntent.cancel();
                                notifyDataSetChanged();
                                Toast.makeText(context, "Deleted",
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(context, "Cancelled",
                                        Toast.LENGTH_LONG).show();
                            }
                        } )
                        .show();
                return true;
            }
        });
    }

    private void checkTime(ToDoItem item, ViewHolder holder)
    {
        Date date = new Date();
        int count = item.getTime()- (int)date.getTime();
        if(count < (24 * 60 * 60 * 1000) ) //if Deadline is less than one day
        {
            holder.title.setTextColor(Color.parseColor("red"));
        }else
        {
            holder.title.setTextColor(Color.parseColor("white"));
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView priority;
        TextView date;
        CheckBox checkBox;
        Spinner spinner;


        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.titleView);
            priority = (TextView) itemView.findViewById(R.id.priorityView);
            checkBox = (CheckBox) itemView.findViewById((R.id.statusCheckBox));
            date = (TextView) itemView.findViewById(R.id.dateView);
            spinner = (Spinner) itemView.findViewById(R.id.prioritySpinner);
        }
    }

    // Returns the number of ToDoItems
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void add(ToDoItem item) {

        mItems.add(item);
        notifyDataSetChanged();
    }

    // Clears the list adapter of all items.

    public void clear(){

        mItems.clear();
        notifyDataSetChanged();
    }

    //@Override
    public Object getItem(int pos) {
        return mItems.get(pos);

    }

    // Get the ID for the ToDoItem
    // In this case it's just the position

    @Override
    public long getItemId(int pos) {

        return pos;

    }

    private  void sort(boolean sortByDeadline)
    {
        Comparator<ToDoItem> rankDeadline =  new Comparator<ToDoItem>()
        {
            public int compare(ToDoItem item1, ToDoItem item2) {
                return item1.getTime() - item2.getTime();
            }
        };
        Comparator<ToDoItem> rankPriority = new Comparator<ToDoItem>(){

            public int compare(ToDoItem item1, ToDoItem item2) {
                //return item1.getPriority().compareTo(item2.getPriority());
                int priority = item1.getPriority().compareTo(item2.getPriority());
                if (priority != 0) {
                    return priority;
                }
                int date = item1.getTime()- item2.getTime();
                if (date!= 0) {
                    return date;
                }
                return item1.title.compareTo(item2.title);
            }
        };
        if(sortByDeadline)
        {
            Collections.sort(mItems,rankDeadline);
        }
        else
        {
            Collections.sort(mItems,rankPriority);
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
