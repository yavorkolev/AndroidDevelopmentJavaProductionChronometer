package yavor.kolev.chronometer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import yavor.kolev.chronometer.R;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends Activity 
{

	private SQLiteAdapter mySQLiteAdapter;
	ListView listContent;
	
	SimpleCursorAdapter cursorAdapter;
	Cursor cursor;
	boolean paused = false;
	long millisecondsHistory, secsHistory, minsHistory, hoursHistory, daysHistory, timeInMillisecondsHistory, timeSwapBuffHistory, updatedTimeHistory, startTimeHistory;
	private Button startButton, stopButton, pauseButton, resumeButton;
	
	private TextView tvMins, tvSecs, tvMills, tvHours, tvDays;
	
	Handler customHandler = new Handler();
	
	long timeInMilliseconds = 0L, timeSwapBuff = 0L, updatedTime = 0L, startTime = 0L;
	long secs = 0L, mins = 0L, milliseconds = 0L, hours = 0L, days = 0L;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listContent = (ListView)findViewById(R.id.contentlist);

        mySQLiteAdapter = new SQLiteAdapter(this);
        mySQLiteAdapter.openToWrite();

        cursor = mySQLiteAdapter.queueAll();
        String[] from = new String[]{SQLiteAdapter.KEY_ID, SQLiteAdapter.KEY_CONTENT1, SQLiteAdapter.KEY_CONTENT2};
        int[] to = new int[]{R.id.id, R.id.text1, R.id.text2};
        cursorAdapter = new SimpleCursorAdapter(this, R.layout.row, cursor, from, to, 1);
        listContent.setAdapter(cursorAdapter);
        listContent.setOnItemClickListener(listContentOnItemClickListener);
        
		tvDays = (TextView) findViewById(R.id.tvDays);
		tvHours = (TextView) findViewById(R.id.tvHours);
		tvMins = (TextView) findViewById(R.id.tvMins);
		tvSecs = (TextView) findViewById(R.id.tvSecs);
		tvMills = (TextView) findViewById(R.id.tvMills);
		
		pauseButton = (Button) findViewById(R.id.pauseButton);
		startButton = (Button) findViewById(R.id.startButton);
		stopButton = (Button) findViewById(R.id.stopButton);
		resumeButton = (Button) findViewById(R.id.resumeButton);
		
		startButton.setVisibility(View.VISIBLE);
		stopButton.setVisibility(View.INVISIBLE);
		resumeButton.setVisibility(View.INVISIBLE);
		pauseButton.setVisibility(View.INVISIBLE);
			
		startButton.setOnClickListener(new View.OnClickListener() 
		{		
			public void onClick(View view) 
			{
				startTime = System.currentTimeMillis();
				customHandler.postDelayed(updateTimerThread, 0);
				paused = false;
				startButton.setVisibility(View.INVISIBLE);
				resumeButton.setVisibility(View.INVISIBLE);
				pauseButton.setVisibility(View.VISIBLE);
				stopButton.setVisibility(View.VISIBLE);
			}
		});	
		
        stopButton.setOnClickListener(new View.OnClickListener() 
        {
			
			public void onClick(View view) 
			{
				Calendar stopDateTime = Calendar.getInstance();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
				String formattedDate = df.format(stopDateTime.getTime());
				customHandler.removeCallbacks(updateTimerThread);
				String data1 = formattedDate;
				String data2 = String.format("%05d", days) +":" +
				               String.format("%02d", hours) +":" + 
						        String.format("%02d", mins) +":" +
				                String.format("%02d", secs) +":" +
						        String.format("%03d", milliseconds);
				
				mySQLiteAdapter.insert(data1, data2);
				updateList();
				paused = false;
				stopButton.setVisibility(View.INVISIBLE);
				startButton.setVisibility(View.VISIBLE);
				pauseButton.setVisibility(View.INVISIBLE);
				resumeButton.setVisibility(View.INVISIBLE);
				days = hours = mins= secs = milliseconds = 0L;
				tvDays.setText(String.format("%05d", days));
				tvHours.setText(String.format("%02d", hours));
				tvMins.setText(String.format("%02d", mins));
				tvSecs.setText(String.format("%02d", secs));
				tvMills.setText(String.format("%03d", milliseconds));
				updatedTime = timeSwapBuff = startTime = 0L;
			}
		});	
		
		pauseButton.setOnClickListener(new View.OnClickListener() 
		{
		
			public void onClick(View view) 
			{
				customHandler.removeCallbacks(updateTimerThread);
				paused = true;
				pauseButton.setVisibility(View.INVISIBLE);
				resumeButton.setVisibility(View.VISIBLE);
			}
		});
		
		resumeButton.setOnClickListener(new View.OnClickListener() 
		{
			
			public void onClick(View view) 
			{
				timeSwapBuff += timeInMilliseconds;
				startTime = System.currentTimeMillis();
				customHandler.postDelayed(updateTimerThread, 0);
				paused = false;
				pauseButton.setVisibility(View.VISIBLE);
				resumeButton.setVisibility(View.INVISIBLE);
			}
		});
    }
    private ListView.OnItemClickListener listContentOnItemClickListener = new ListView.OnItemClickListener()
    {

		@SuppressWarnings("deprecation")
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
		{	
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			final int item_id = cursor.getInt(cursor.getColumnIndex(SQLiteAdapter.KEY_ID));
            String item_content1 = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_CONTENT1));
            String item_content2 = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_CONTENT2));
            
            AlertDialog.Builder myDialog = new AlertDialog.Builder(MainActivity.this);
            
            myDialog.setTitle("Delete/Edit?");
            
            TextView dialogTxt_id = new TextView(MainActivity.this);
            LayoutParams dialogTxt_idLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            dialogTxt_id.setLayoutParams(dialogTxt_idLayoutParams);
            dialogTxt_id.setText("Run: " + String.valueOf(item_id));
            
            final EditText dialogC1_id = new EditText(MainActivity.this);
            LayoutParams dialogC1_idLayoutParams 
             = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
            dialogC1_id.setLayoutParams(dialogC1_idLayoutParams);
            dialogC1_id.setText(item_content1);
            
            final EditText dialogC2_id = new EditText(MainActivity.this);
            LayoutParams dialogC2_idLayoutParams 
             = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
            dialogC2_id.setLayoutParams(dialogC2_idLayoutParams);
            dialogC2_id.setText(item_content2);
            
            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(dialogTxt_id);
            layout.addView(dialogC1_id);
            layout.addView(dialogC2_id);
            myDialog.setView(layout);
            
            myDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() 
            {
                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) 
                {
                	mySQLiteAdapter.delete_byID(item_id);
        			updateList();
                }
            });
            
            myDialog.setNeutralButton("Update", new DialogInterface.OnClickListener() 
            {
                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) 
                {
                	String value1 = dialogC1_id.getText().toString();
                	String value2 = dialogC2_id.getText().toString();
                	mySQLiteAdapter.update_byID(item_id, value1, value2);
        			updateList();
                }
            });
            
            myDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
            {
                // do something when the button is clicked
                public void onClick(DialogInterface arg0, int arg1) 
                {
         
                }
            });
            
            myDialog.show();          
		}
	};

	protected void onDestroy() 
	{
		super.onDestroy();
		mySQLiteAdapter.close();
	}

	protected void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);	
		millisecondsHistory = milliseconds;
        secsHistory = secs;
        minsHistory = mins;
        hoursHistory = hours;
        daysHistory = days;
        
        timeInMillisecondsHistory = timeInMilliseconds;
        timeSwapBuffHistory = timeSwapBuff;
        updatedTimeHistory = updatedTime;
        startTimeHistory = startTime;
		
		outState.putSerializable("millisecondsHistory", millisecondsHistory);
		outState.putSerializable("secsHistory", secsHistory);
		outState.putSerializable("minsHistory", mins);
		outState.putSerializable("hoursHistory", hours);
		outState.putSerializable("daysHistory", days);
		
		outState.putSerializable("timeInMillisecondsHistory", timeInMillisecondsHistory);
		outState.putSerializable("timeSwapBuffHistory", timeSwapBuffHistory);
		outState.putSerializable("updatedTimeHistory", updatedTimeHistory);
		outState.putSerializable("startTimeHistory", startTimeHistory);
		
		outState.putSerializable("paused", paused);
	}
	
	protected void onRestoreInstanceState(Bundle savedState) 
	{	
    	millisecondsHistory = (Long) savedState.getSerializable("millisecondsHistory");
		milliseconds = millisecondsHistory;
		tvMills.setText(String.format("%03d", millisecondsHistory));
		
		secsHistory = (Long) savedState.getSerializable("secsHistory");
		secs = secsHistory;
		tvSecs.setText(String.format("%02d", secsHistory));
		
		minsHistory = (Long) savedState.getSerializable("minsHistory");
		mins = minsHistory;
		tvMins.setText(String.format("%02d", minsHistory));
		
		hoursHistory = (Long) savedState.getSerializable("hoursHistory");
		hours = hoursHistory;
		tvHours.setText(String.format("%02d", hoursHistory));
		
		daysHistory = (Long) savedState.getSerializable("daysHistory");
		days = daysHistory;
		tvDays.setText(String.format("%05d", daysHistory));
		
		timeInMillisecondsHistory = (Long) savedState.getSerializable("timeInMillisecondsHistory");
		timeInMilliseconds = timeInMillisecondsHistory; 
		
		timeSwapBuffHistory = (Long) savedState.getSerializable("timeSwapBuffHistory");
		timeSwapBuff = timeSwapBuffHistory;
		
		updatedTimeHistory = (Long) savedState.getSerializable("updatedTimeHistory");
		updatedTime = updatedTimeHistory;
		
		startTimeHistory = (Long) savedState.getSerializable("startTimeHistory");
		startTime = startTimeHistory;
		
		paused = (Boolean) savedState.getSerializable("paused");
		
		if(startTime != 0 && paused != true)
		{
		  startButton.setVisibility(View.INVISIBLE);
		  stopButton.setVisibility(View.VISIBLE);
		  resumeButton.setVisibility(View.INVISIBLE);
		  pauseButton.setVisibility(View.VISIBLE);	
		  customHandler.postDelayed(updateTimerThread, 0);
		}
		if(paused == true)
		{
		  startButton.setVisibility(View.INVISIBLE);
		  stopButton.setVisibility(View.VISIBLE);
	      resumeButton.setVisibility(View.VISIBLE);
		  pauseButton.setVisibility(View.INVISIBLE);
		}
	}
	
	private Runnable updateTimerThread = new Runnable() 
	{
		public void run() 
		{		
			timeInMilliseconds = System.currentTimeMillis() - startTime;		
			updatedTime = timeSwapBuff + timeInMilliseconds;
			secs = (updatedTime / 1000);
			mins = secs / 60;
			hours = mins / 60;	
			days = hours / 24;
			hours = hours % 24;
			secs = secs % 60;
			mins = mins % 60;
			milliseconds = (updatedTime % 1000);
			tvDays.setText(String.format("%05d", days));
			tvHours.setText(String.format("%02d", hours));
			tvMins.setText(String.format("%02d", mins));
			tvSecs.setText(String.format("%02d", secs));
			tvMills.setText(String.format("%03d", milliseconds));
			customHandler.postDelayed(this, 0);
		}
	};
	
	@SuppressWarnings("deprecation")
	private void updateList()
	{
		cursor.requery();
    }
}