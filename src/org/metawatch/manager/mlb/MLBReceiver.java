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
  * MLBReceiver.java                                                          *
  * MLBReceiver                                                               *
  * Receiver for update intents from MWM                                      *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager.mlb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// This class catches the MWM broadcast intents. 
public class MLBReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(MLBActivity.TAG,"MLBReceiver");
		if (intent.hasExtra("action_mlbupdate")) {
			MLBActivity.updateMLBScore(context, intent);			
			return;
		}
	}
}
