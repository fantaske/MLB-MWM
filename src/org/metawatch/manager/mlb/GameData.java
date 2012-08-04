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

	public GregorianCalendar getEastStartTime() {
		return this.eastStartTime;
	}

	public GregorianCalendar getLocalStartTime() {
		return this.localStartTime;
	}

	public String getEastStartString() {
		return this.eastStartTimeString;
	}

	public void setEastStartString(String eastStartTimeString) {
		this.eastStartTimeString = eastStartTimeString;
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

	public void setHomeName(String homeName) {
		this.homeName = homeName;
	}

	public Integer getAwayRuns() {
		return this.awayRuns;
	}

	public void setAwayRuns(Integer awayRuns) {
		this.awayRuns = awayRuns;
	}

	public Integer getHomeRuns() {
		return this.homeRuns;
	}

	public void setHomeRuns(Integer homeRuns) {
		this.homeRuns = homeRuns;
	}

	public Integer getInning() {
		return this.inning;
	}

	public void setInning(Integer inning) {
		this.inning = inning;
	}

	public Integer getOuts() {
		return this.outs;
	}

	public void setOuts(Integer outs) {
		this.outs = outs;
	}

	public Boolean getTop() {
		return this.top;
	}

	public void setTop(Boolean top) {
		this.top = top;
	}

	public MLBGameScore.GameState getState() {
		return this.state;
	}

	public void setState(MLBGameScore.GameState state) {
		this.state = state;
	}

	// Convert string of the form "7:05PM" into a Calendar object
	private void convertEastStartTime() {
		try {
			boolean pm = this.eastStartTimeString.endsWith("PM");
			Integer colon = this.eastStartTimeString.indexOf(':');
			Integer hour = Integer.parseInt(this.eastStartTimeString.substring(0,	colon));
			Integer minute = Integer.parseInt(this.eastStartTimeString.substring(colon + 1, colon + 3));
			eastStartTime.set(Calendar.HOUR_OF_DAY, hour + (pm ? 12 : 0));
			eastStartTime.set(Calendar.MINUTE, minute);
			eastStartTime.set(Calendar.SECOND, 0);
			eastStartTime.set(Calendar.MILLISECOND, 0);			
		} catch (Exception e) {
			Log.e("MLBGameData", e.toString());
		}
		return;
	}

	// Convert the eastern time zone time into a local time.
	private void convertLocalStartTime() {
		try {
			localStartTime = (GregorianCalendar)this.eastStartTime.clone();
			TimeZone localtz = TimeZone.getDefault();
			Integer localOffset = localtz.getRawOffset();
			TimeZone etz = TimeZone.getTimeZone("EST5EDT");
			Integer eOffset = etz.getRawOffset();
			Integer diffOffset = localOffset - eOffset; // units of milliseconds
			Integer hourOffset = diffOffset / (1000 * 60 * 60);
			localStartTime.add(Calendar.HOUR_OF_DAY, hourOffset);
//			if (Preferences.logging) Log.d(MLBActivity.TAG,
//					"MLBGameData: time:"+adjustTime.toString());
			return;
		} catch (Exception e) {
//			Log.e(MLBActivity.TAG, "getLocalStartTime exception: "+ e.toString());
			localStartTime = (GregorianCalendar) Calendar.getInstance();
		}
	}

	public String getMyTeamName() {
		return myTeamName;
	}

	public void setMyTeamName(String myTeamName) {
		this.myTeamName = myTeamName;
	}

}
