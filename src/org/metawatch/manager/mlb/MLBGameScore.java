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
  * This class gets game data from gd2.mlb.com for a certain date, typically  *
  * today.                                                                    *
  * The xml parsing is done in another class, MLBScoreHandler.                *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager.mlb;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;

public class MLBGameScore {
	
	public static enum GameState {
		PREGAME, PLAYING, FINAL, NONE
	}

	private String myTeam;
	private GregorianCalendar gameDate;
	private GameData game;	

	private String formUrlDir() {
		Integer year;
		Integer month;
		Integer day;

		try {
			year = this.gameDate.get(Calendar.YEAR);
			month = this.gameDate.get(Calendar.MONTH) + 1;
			day = this.gameDate.get(Calendar.DAY_OF_MONTH);
		} catch (Exception e) {
			year = 2012;
			month = 5;
			day = 1;
			Log.e(MLBActivity.TAG,"formatUrlDir" + e.toString());
		}
		return String.format("http://gd2.mlb.com/components/game/mlb/year_%1$4d/month_%2$02d/day_%3$02d",
				year, month, day);
	}
	
	private void updateScore(){
		try{	
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			
			MLBScoreHandler mlbsh = new MLBScoreHandler(this.myTeam);
			xr.setContentHandler(mlbsh);
			
			URL url = new URL(formUrlDir() + "/scoreboard.xml");
			InputSource src = new InputSource(url.openStream());
			src.setEncoding("ISO-8859-1");
			xr.parse(src);
			this.game = mlbsh.getGameData();
		} catch (SAXException e){
			Log.e(MLBActivity.TAG,"updateScore SAX" + e.toString());
		} catch (IOException e){
			Log.e(MLBActivity.TAG,"updateScore IO" + e.toString());
		} catch (Exception e){
			Log.e(MLBActivity.TAG,"updateScore" + e.toString());
		}
	}

	public GameData getScore(){
		updateScore();
		while(this.game == null){
			Log.e(MLBActivity.TAG,"updateScore null game try again");
			updateScore();
		}
		return this.game;
	}
	
	public String getTeamName(){
		return this.myTeam;
	}
		
	public MLBGameScore(String teamName, GregorianCalendar date){
		this.myTeam = teamName;
		this.gameDate = date;
	}

	public MLBGameScore(String teamName){
		this.myTeam = teamName;
		this.gameDate = (GregorianCalendar) Calendar.getInstance();
	}

}
