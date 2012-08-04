/*****************************************************************************
  *  Copyright (c) 2012 Stephen Greer
  *                                                                           *
  =============================================================================
  *                                                                           *
  *  Licensed under the Apache License, Version 2.0 (the "License");          *
  *  you may not use this file except in compliance with the License.         *
  *  You may obtain a copy of the License at                                  *
  *                                                                           *
  *    http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                           *
  *  Unless required by applicable law or agreed to in writing, software      *
  *  distributed under the License is distributed on an "AS IS" BASIS,        *
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
  *  See the License for the specific language governing permissions and      *
  *  limitations under the License.                                           *
  *                                                                           *
  *****************************************************************************/

 /*****************************************************************************
  * MLBActivity.java                                                          *
  * MLBActivity                                                               *
  * Main file for MLB Score for MetaWatch. Gathers configuration data and     *
  * starts an alarm for updating the score.                                   *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager.mlb;

import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.metawatch.manager.mlb.R;

import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

public class MLBActivity extends Activity {

	public static final String TAG = "MLB-MWM";
	static int teamIndex = 0;
	static Integer updateTimeMinutes = 2;
	static String teamName;
	static String[] teams;
	private static AlarmManager alarmManager;
	private static Intent intent;
	private static PendingIntent sender;
	private static MLBGameScore.GameState oldState = MLBGameScore.GameState.NONE;
	static Context context;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlb);
        context = (Context)this;
        
        final SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);       
        teamIndex = sharedPreferences.getInt("teamIndex", 0);
        updateTimeMinutes = sharedPreferences.getInt("timing", 2);
        
        Resources res = getResources();
        teams = res.getStringArray(R.array.all_mlb_teams);
        teamName = teams[teamIndex];
        
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		intent = new Intent(context, MLBReceiver.class).putExtra("action_mlbupdate", "update");
		sender = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        final EditText textTiming = (EditText) findViewById(R.id.editTextTime);
        
        final Button teamButton = (Button) findViewById(R.id.buttonTeam);
        teamButton.setText(teamName);
        teamButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Pick a team");
				builder.setItems(teams, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				        teamIndex = item;
				        teamName = teams[teamIndex];
				        teamButton.setText(teamName);
				    }
				});
				builder.show();
			};
        });
        
        
        Button doneButton = (Button) findViewById(R.id.buttonDone);
        doneButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String updateTimeString = textTiming.getText().toString();
				try{
					updateTimeMinutes = Integer.parseInt(updateTimeString);					
				} catch (Exception e){
					updateTimeMinutes = 2;
				}

				if (updateTimeMinutes < 2 || updateTimeMinutes > 60)
					updateTimeMinutes = 2;

				textTiming.setText(updateTimeMinutes.toString());

				Editor editor = sharedPreferences.edit();
				editor.putInt("teamIndex", teamIndex);
				editor.putInt("timing", updateTimeMinutes);
								
				editor.commit();

				startMLBTicker(new GregorianCalendar(2000,1,1), MLBGameScore.GameState.PREGAME);		
			};
        });
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_mlb, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.about:
	    	showAbout();
	        return true;
	    case R.id.exit:	        
			stopMLBTicker();
			finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void showAbout(){
    	
    	Resources res = getResources();
    	AssetManager assetmgr = res.getAssets();
    	StringBuilder html = new StringBuilder(1000);
    	try{
    		InputStreamReader isr = new InputStreamReader(assetmgr.open("about.html")); 
    		int c;
    		while((c = isr.read()) != -1){
    			html.append((char)c);
    		}
    	} catch (Exception e){
    		html.replace(0,10000,"Failed to retrieve about text");
    	}
    	WebView webView = new WebView(this);
        webView.loadDataWithBaseURL("file:///android_asset/", html.toString(), "text/html", "utf-8", null);
        
        new AlertDialog.Builder(this).setView(webView).setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {			
			//@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();        
		
		
	}
	
	static void startMLBTicker(GregorianCalendar startTime, MLBGameScore.GameState newState) {
		// During the game, check the score every two minutes
		if (newState == MLBGameScore.GameState.PLAYING) {
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0,
					MLBActivity.updateTimeMinutes * 60 * 1000, sender);
			return;
		}

//		if (newState == MLBGameScore.GameState.PREGAME) {
//	Don't adjust time
//		}

		// If no game today, check again tomorrow at 1AM
		if (newState == MLBGameScore.GameState.NONE ){
			startTime.add(Calendar.DATE, 1);
			startTime.set(Calendar.HOUR_OF_DAY, 1);
		}
		
		// When today's game is over, check again tomorrow at 10AM
		if (newState == MLBGameScore.GameState.FINAL ){
			startTime.add(Calendar.DATE, 1);
			startTime.set(Calendar.HOUR_OF_DAY, 10);
    		Intent i = createVibrateIntent();
    		context.sendBroadcast(i);
//			if (Preferences.logging) Log.d(MLBActivity.TAG,
//					"final wakeup");
		}
		long stMilli = startTime.getTimeInMillis();
		alarmManager.set(AlarmManager.RTC_WAKEUP,stMilli, sender);
				
	}
   
	static void stopMLBTicker() {
		alarmManager.cancel(sender);
	}
	
	public static void updateMLBScore(final Context context, final Intent intent){
		Thread thread = new Thread("MLBScoreUpdater") {
			@Override
			public void run() {
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mlb");
				wl.acquire();

				if (intent.hasExtra("action_mlbupdate")) {
					Log.d(MLBActivity.TAG,"updateMLBScore");

					MLBGameScore myScore = new MLBGameScore(MLBActivity.teamName);
					GameData score = myScore.getScore();
					switch(score.getState())
					{
					case NONE:
					case PREGAME:
					case FINAL:
						startMLBTicker(score.getLocalStartTime(), score.getState());
						oldState = score.getState();
						break;
					case PLAYING:
						if(oldState != MLBGameScore.GameState.PLAYING){
							startMLBTicker(score.getEastStartTime(), score.getState());
							oldState = score.getState();
						}
						break;
					}
					MLBScoreWidget.setScoreData(score);
					MLBScoreWidget.update(context,score);

				}
				wl.release();
			}
		};
		thread.start();
	};

	private static Intent createVibrateIntent() {
		Intent intent = new Intent("org.metawatch.manager.VIBRATE");
		Bundle b = new Bundle();
		b.putInt("vibrate_on", 500);
		b.putInt("vibrate_off", 500);
		b.putInt("vibrate_cycles", 3);
		intent.putExtras(b);

		return intent;
	}
	
}
