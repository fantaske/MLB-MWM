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
  * MLBScoreHandler.java                                                      *
  * MLBScoreHandler                                                           *
  * Implements the callback routines for a SAX parser which handles the XML   *
  * data supplied by Major League Baseball.                                   *
  * See http://gdx.mlb.com/components/copyright.txt                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager.mlb;

//import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class MLBScoreHandler extends DefaultHandler {

	private GameData game = new GameData();	
	private boolean in_game;
	private boolean found_team;
	private boolean visitor;
	private String idAttribute;
	private String statusAttribute;
	private String startAttribute;
	private String myTeam;
	private Integer runs;

	// The xml file uses abbreviations I don't always like and they're lower case.
//	private static String[][] nameConvert = {
//		{"ana","ari","atl","bal","bos","cha","chn","cin","cle","col","det","hou","kca","lan","mia",
//		 "mil","min","nya","nyn","phi","pit","oak","sea","sdn","sfn","sln","tba","tex","tor","was"
//		},
//		{"LAA","ARI","ATL","BAL","BOS","CWS","CHC","CIN","CLE","COL","DET","HOU"," KC","LAD","MIA",
//		 "MIL","MIN","NYY","NYM","PHI","PIT","OAK","SEA"," SD"," SF","STL"," TB","TEX","TOR","WAS"
//			}
//	};
//	private Hashtable<String,String> hashNames = new Hashtable<String, String>(30);
//	
//	private void parseId(){
//		this.game.setAwayName(hashNames.get(this.idAttribute.substring(11,14)));
//		this.game.setHomeName(hashNames.get(this.idAttribute.substring(18,21)));	
//	}
	
	private final static String smallAbbrev = "ana,ari,atl,bal,bos,cha,chn,cin,cle,col,det,hou,kca,lan,mia,mil,min,nya,nyn,phi,pit,oak,sea,sdn,sfn,sln,tba,tex,tor,was";
	private final static String capAbbrev =   "LAA,ARI,ATL,BAL,BOS,CWS,CHC,CIN,CLE,COL,DET,HOU, KC,LAD,MIA,MIL,MIN,NYY,NYM,PHI,PIT,OAK,SEA, SD, SF,STL, TB,TEX,TOR,WAS";
						
	
	private void parseId(){
		int awayPos = smallAbbrev.indexOf(this.idAttribute.substring(11,14));
		this.game.setAwayName(capAbbrev.substring(awayPos, awayPos+3));
		int homePos = smallAbbrev.indexOf(this.idAttribute.substring(18,21));
		this.game.setHomeName(capAbbrev.substring(homePos, homePos+3));
	}
	
	public MLBScoreHandler(String name){
		this.myTeam = name;
//		for(int i = 0; i < nameConvert[0].length; i++){
//			hashNames.put(nameConvert[0][i], nameConvert[1][i]);
//		}
	}
	
	public GameData getGameData() {
		return this.game;
	}
	
	@Override
	public void startDocument() throws SAXException {
		this.game = new GameData();
		this.in_game = false;
		this.found_team = false;
	}

	@Override
	public void endDocument() throws SAXException {
		if(!this.found_team){
			this.game.setState(MLBGameScore.GameState.NONE);
		}
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		// If we already found our team and its game has been completely processed
		// then just skip the rest of the elements.
		if(found_team  && !in_game)return;
		// game elements have several forms, but they end with _game.
		if(qName.endsWith("_game")){
			this.in_game = true;
			// the home team is the first team element
			this.visitor = false;
			// game must be in progress for 'outs' to appear as an attribute
			if(atts.getIndex("outs") >= 0){
				this.game.setOuts(Integer.parseInt(atts.getValue("outs")));
			}
		} else if(qName.equals("game")){
			// the id attribute contains short team names
			this.idAttribute = atts.getValue("id");
			this.statusAttribute = atts.getValue("status");
			// this time is always for the eastern time zone
			this.startAttribute = atts.getValue("start_time");
		} else if(qName.equals("team")){
			if(myTeam.equalsIgnoreCase(atts.getValue("name"))) found_team=true;
		} else if(qName.equals("gameteam")){
			// assign runs to proper team when end of element is reached
			this.runs = Integer.parseInt(atts.getValue("R"));
		} else if(qName.equals("inningnum")){
			this.game.setInning(Integer.parseInt(atts.getValue("inning")));
			this.game.setTop(atts.getValue("half").equals("T"));
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if(qName.endsWith("_game")){
			in_game = false;
			if(this.found_team){
				this.game.setEastStartString(this.startAttribute);
				this.parseId();
				if(this.statusAttribute.equals("IN_PROGRESS") ||
				   this.statusAttribute.equals("DELAYED"))
					this.game.setState(MLBGameScore.GameState.PLAYING);
				else if(this.statusAttribute.equals("PRE_GAME") ||
						this.statusAttribute.equals("IMMEDIATE_PREGAME"))
					this.game.setState(MLBGameScore.GameState.PREGAME);
				else if(this.statusAttribute.equals("FINAL")  ||
						this.statusAttribute.equals("GAME_OVER"))
					this.game.setState(MLBGameScore.GameState.FINAL);
			}
		}
		if(qName.equals("team")){
			if(visitor){
				this.game.setAwayRuns(runs);
			} else {
				this.game.setHomeRuns(runs);				
			}
			visitor = true;
		}
	}
}
