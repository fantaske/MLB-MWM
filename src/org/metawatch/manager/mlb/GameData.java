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
  * MLBGameScore.java                                                         *
  * MLBGameScore                                                              *
  * This class is basically a structure which holds information about the     *
  * current state of a particular game. The only heavy lifting it does is     *
  * converting a string into a Calendar and then adding an offset to that     *
  * Calendar for a time zone shift.                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager.mlb;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

//import org.metawatch.manager.MetaWatch;
//import org.metawatch.manager.MetaWatchService.Preferences;

import android.util.Log;

public class GameData {
	private String myTeamName = "Favorite";
	private String awayName = "UNK";
	private String homeName = "UNK";
	private Integer awayRuns = 0;
	private Integer homeRuns = 0;
	private Integer inning = 0;
	private Integer outs = 0;
	private Boolean top = true;
	private String eastStartTimeString = "7:05 PM";
	private MLBGameScore.GameState state = MLBGameScore.GameState.NONE;
	private GregorianCalendar eastStartTime = (GregorianCalendar) Calendar.getInstance();
	private GregorianCalendar localStartTime = (GregorianCalendar) Calendar.getInstance();

	public void resetValues(){
//		myTeamName = "Favorite";
		awayName = "UNK";
		homeName = "UNK";
		awayRuns = 0;
		homeRuns = 0;
		inning = 0;
		outs = 0;
		top = true;
		eastStartTimeString = "7:05 PM";
		state = MLBGameScore.GameState.NONE;
	}
	
	public GregorianCalendar getEastStartTime() {
		return this.eastStartTime;
	}

	public GregorianCalendar getLocalStartTime() {
		return this.localStartTime;
	}

	public String getEastStartString() {
		return this.eastStartTimeString;
	}

	public void setEastStartString(String newEastStartTimeString) {
		this.eastStartTimeString = newEastStartTimeString;
		convertEastStartTime();
		convertLocalStartTime();
	}

	public String getAwayName() {
		return this.awayName;
	}

	public void setAwayName(String awayName) {
		this.awayName = awayName;
	}

	public String getHomeName() {
		return this.homeName;
	}

	public void setHomeName(String newHomeName) {
		this.homeName = newHomeName;
	}

	public Integer getAwayRuns() {
		return this.awayRuns;
	}

	public void setAwayRuns(Integer newAwayRuns) {
		this.awayRuns = newAwayRuns;
	}

	public Integer getHomeRuns() {
		return this.homeRuns;
	}

	public void setHomeRuns(Integer newHomeRuns) {
		this.homeRuns = newHomeRuns;
	}

	public Integer getInning() {
		return this.inning;
	}

	public void setInning(Integer newInning) {
		this.inning = newInning;
	}

	public Integer getOuts() {
		return this.outs;
	}

	public void setOuts(Integer newOuts) {
		this.outs = newOuts;
	}

	public Boolean getTop() {
		return this.top;
	}

	public void setTop(Boolean newTop) {
		this.top = newTop;
	}

	public MLBGameScore.GameState getState() {
		return this.state;
	}

	public void setState(MLBGameScore.GameState newState) {
		this.state = newState;
	}

	// Convert string of the form "7:05PM" into a Calendar object
	private void convertEastStartTime() {
		try {
			boolean pm = this.eastStartTimeString.endsWith("PM");
			Integer colon = this.eastStartTimeString.indexOf(':');
			Integer hour = Integer.parseInt(this.eastStartTimeString.substring(0,	colon));
			Integer minute = Integer.parseInt(this.eastStartTimeString.substring(colon + 1, colon + 3));
			this.eastStartTime.set(Calendar.HOUR_OF_DAY, hour + (pm ? 12 : 0));
			this.eastStartTime.set(Calendar.MINUTE, minute);
			this.eastStartTime.set(Calendar.SECOND, 0);
			this.eastStartTime.set(Calendar.MILLISECOND, 0);			
			Log.d(MLBActivity.TAG,"MLBGameData: east time");
			Log.v(MLBActivity.TAG,"MLBGameData: east time:"+eastStartTime.toString());
		} catch (Exception e) {
			this.eastStartTime = (GregorianCalendar) Calendar.getInstance();
			Log.e(MLBActivity.TAG, "convertEastStartTime exception: "+ e.toString());
		}
		return;
	}

	// Convert the eastern time zone time into a local time.
	private void convertLocalStartTime() {
		try {
			this.localStartTime = (GregorianCalendar)this.eastStartTime.clone();
			TimeZone localtz = TimeZone.getDefault();
			Integer localOffset = localtz.getRawOffset();
			TimeZone etz = TimeZone.getTimeZone("EST5EDT");
			Integer eOffset = etz.getRawOffset();
			Integer diffOffset = localOffset - eOffset; // units of milliseconds
			Integer hourOffset = diffOffset / (1000 * 60 * 60);
			this.localStartTime.add(Calendar.HOUR_OF_DAY, hourOffset);
			Log.d(MLBActivity.TAG,"MLBGameData: local time");
			Log.v(MLBActivity.TAG,"MLBGameData: local time:"+localStartTime.toString());
			return;
		} catch (Exception e) {
			this.localStartTime = (GregorianCalendar) Calendar.getInstance();
			Log.e(MLBActivity.TAG, "convertLocalStartTime exception: "+ e.toString());
		}
	}

	public String getMyTeamName() {
		return myTeamName;
	}

	public void setMyTeamName(String newTeamName) {
		this.myTeamName = newTeamName;
	}

}
