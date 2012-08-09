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
  * MLBScoreWidge.java                                                        *
  * MLBScoreWidget                                                            *
  * Draws the game score onto a bitmap, put it into an intent and then        *
  * broadcasts it to MWM for display as a widget on the watch.                *
  *                                                                           *
  *****************************************************************************/


package org.metawatch.manager.mlb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.metawatch.manager.mlb.GameData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;

public class MLBScoreWidget extends BroadcastReceiver {

	public final static String id_0 = "mlbscore_48_32";
	final static String desc_0 = "MLB Score (48x32)";
	static Typeface typeface = null;
	static Typeface typefaceMed = null;
	static TextPaint paintSmall = null;
	static TextPaint paintMed = null;
	
    private static GameData score;
	
	private static final float xAwayName = 3.0f;
	private static final float xHomeName = 23.0f;
	private static final float yNames = 11.0f;
	private static final float xAwayScore = 3.0f;
	private static final float xHomeScore = 23.0f;
	private static final float yScores = 27.0f;
	private static final float yInning = 19.0f;
	private static final float xInning = 41.0f;
	private static final float yTop = 11.0f;
	private static final float yBottom = 27.0f;
	
	@Override
	// About every 30 minutes, MWM wants the widge updated.
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i(MLBActivity.TAG, "onReceive() " + action);
		
		if (action !=null && action.equals("org.metawatch.manager.REFRESH_WIDGET_REQUEST")) {

			Log.d(MLBActivity.TAG, "Received intent");

			Bundle bundle = intent.getExtras();

			boolean getPreviews = bundle.containsKey("org.metawatch.manager.get_previews");
			if (getPreviews)
				Log.d(MLBActivity.TAG, "get_previews");

			ArrayList<String> widgets_desired = null;

			if (bundle.containsKey("org.metawatch.manager.widgets_desired")) {
				Log.d(MLBActivity.TAG, "widgets_desired");
				widgets_desired = new ArrayList<String>(Arrays.asList(bundle.getStringArray("org.metawatch.manager.widgets_desired")));
			}

			boolean active = (widgets_desired != null) && (widgets_desired.contains(id_0));
			
			if (getPreviews || active) {
				if(score == null){
					Log.i(MLBActivity.TAG, "null score in onReceive");
//					MLBActivity.updateMLBScore(context, intent);
					MLBActivity.updateMLBScore();
				} else {
					genWidget(context, id_0, score);
				}
			}
		}
		
	}
	
    public synchronized static void setScoreData(GameData newScore){
    	score = newScore;
    }
 
   private static void drawNames(Canvas canvas, GameData drawScore){
        canvas.drawText(drawScore.getAwayName(), xAwayName, yNames, paintSmall);
        canvas.drawText(drawScore.getHomeName(), xHomeName, yNames, paintSmall);    
    }

   private static void drawScores(Canvas canvas, GameData drawScore){
        canvas.drawText(drawScore.getAwayRuns().toString(), xAwayScore, yScores, paintMed);
        canvas.drawText(drawScore.getHomeRuns().toString(), xHomeScore, yScores, paintMed);
        Log.v(MLBActivity.TAG,drawScore.getAwayName() + " " + drawScore.getAwayRuns().toString() + " " +  
        		              drawScore.getAwayRuns().toString()  + " " + drawScore.getHomeRuns().toString() +
        		              drawScore.getOuts().toString() + "inning");
   }

	public synchronized static void update(Context context, GameData updateScore) {
		Log.d(MLBActivity.TAG, "Updating widget");
		score = updateScore;
		genWidget(context, id_0, updateScore);
	}
   
	private synchronized static void genWidget(Context context, String id, GameData updateScore) {
		Log.d(MLBActivity.TAG, "genWidget() start - "+id);
    	AssetManager assetmgr = context.getAssets();
		
		if (typeface == null) {
			typeface = Typeface.createFromAsset(assetmgr, "metawatch_8pt_5pxl_CAPS.ttf");
		}
		if (typefaceMed == null) {
			typefaceMed = Typeface.createFromAsset(assetmgr, "metawatch_16pt_11pxl.ttf");
		}
		
		if(paintSmall == null){
			paintSmall = new TextPaint();
			paintSmall.setColor(Color.BLACK);
			paintSmall.setTextSize(8);
			paintSmall.setTypeface(typeface);
			paintSmall.setTextAlign(Align.LEFT);
		}
		if(paintMed == null){
			paintMed = new TextPaint();
			paintMed.setColor(Color.BLACK);
			paintMed.setTextSize(16);
			paintMed.setTypeface(typefaceMed);
			paintMed.setTextAlign(Align.LEFT);
		}
		Bitmap bitmap = Bitmap.createBitmap(48, 32, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
 
		switch(updateScore.getState())
        {
        case NONE:
        	canvas.drawText(updateScore.getMyTeamName(), xAwayName, yNames, paintSmall);
            canvas.drawText("NONE", xAwayScore, yScores, paintMed);
        	break;
        case PREGAME:
        	drawNames(canvas, updateScore);
        	String time;
        	try{
        		SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
        		Date localdate = updateScore.getLocalStartTime().getTime();
        		time = sdf.format(localdate);
        	} catch (Exception e) {
        		time = "0:00PM";
        	}
        	canvas.drawText(time, xAwayScore, yScores, paintMed);
        	break;
        case PLAYING:
           	drawNames(canvas, updateScore);
        	drawScores(canvas, updateScore);
        	String inning = updateScore.getInning().toString();
           	canvas.drawText(inning, xInning, yInning, paintSmall);
        	String outs = updateScore.getOuts().toString();
        	float yOuts = (updateScore.getTop() ? yTop : yBottom);
           	canvas.drawText(outs,   xInning, yOuts,   paintSmall);       		
        	break;
        case FINAL:
        	drawNames(canvas, updateScore);
        	drawScores(canvas, updateScore);
        	canvas.drawText("F", xInning, yInning, paintSmall);
        	break;
        }
 	
		Intent i = createUpdateIntent(bitmap, id, desc_0, 1);
		context.sendBroadcast(i);

		Log.d(MLBActivity.TAG, "genWidget() end");
	}
	
	/**
	 * @param bitmap Widget image to send
	 * @param id ID of this widget - should be unique, and sensibly identify
	 *        the widget
	 * @param description User friendly widget name (will be displayed in the
	 * 		  widget picker)
	 * @param priority A value that indicates how important this widget is, for
	 * 		  use when deciding which widgets to discard.  Lower values are
	 *        more likely to be discarded.
	 * @return Filled-in intent, ready for broadcast.
	 */
	private static Intent createUpdateIntent(Bitmap bitmap, String id, String description, int priority) {
		int pixelArray[] = new int[bitmap.getWidth() * bitmap.getHeight()];
		bitmap.getPixels(pixelArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

		Intent intent = new Intent("org.metawatch.manager.WIDGET_UPDATE");
		Bundle b = new Bundle();
		b.putString("id", id);
		b.putString("desc", description);
		b.putInt("width", bitmap.getWidth());
		b.putInt("height", bitmap.getHeight());
		b.putInt("priority", priority);
		b.putIntArray("array", pixelArray);
		intent.putExtras(b);

		return intent;
	}


}
