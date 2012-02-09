/**
* BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
*
* Copyright (c) 2010 BigBlueButton Inc. and by respective authors (see below).
*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License as published by the Free Software
* Foundation; either version 2.1 of the License, or (at your option) any later
* version.
*
* BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License along
* with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
* 
*/

package org.bigbluebutton.modules.polling.managers
{
	import com.asfusion.mate.events.Dispatcher;
	
	import mx.collections.ArrayCollection;
	import org.bigbluebutton.common.IBbbModuleWindow;
	import org.bigbluebutton.common.LogUtil;
	import org.bigbluebutton.common.events.CloseWindowEvent;
	import org.bigbluebutton.common.events.OpenWindowEvent;
	
	import org.bigbluebutton.modules.polling.service.PollingService;
	
	import org.bigbluebutton.modules.polling.views.PollingViewWindow;
	import org.bigbluebutton.modules.polling.views.PollingStatsWindow;
	import org.bigbluebutton.modules.polling.views.PollingInstructionsWindow;
	import org.bigbluebutton.modules.polling.views.PollingAcceptWindow;
	
	import org.bigbluebutton.modules.polling.events.PollingViewWindowEvent;
	import org.bigbluebutton.modules.polling.events.PollingInstructionsWindowEvent;
	import org.bigbluebutton.modules.polling.events.AcceptPollingInstructionsWindowEvent;
	import org.bigbluebutton.modules.polling.events.PollingStatsWindowEvent;
	import org.bigbluebutton.modules.polling.events.PollRefreshEvent;
	import org.bigbluebutton.modules.polling.events.StopPollEvent;
	import org.bigbluebutton.modules.polling.events.PollingStatusCheckEvent;
	import org.bigbluebutton.modules.polling.events.PollGetTitlesEvent;
	import org.bigbluebutton.modules.polling.events.OpenSavedPollEvent;
	
	import org.bigbluebutton.modules.polling.model.PollObject;
			
	public class PollingWindowManager {	
			
		private var pollingWindow:PollingViewWindow;
		private var statsWindow:PollingStatsWindow;
		private var instructionsWindow:PollingInstructionsWindow;
		private var acceptInstructionsWindow: PollingAcceptWindow;
		private var service:PollingService;
		private var isViewing:Boolean = false;
		private var globalDispatcher:Dispatcher;
		public static const LOGNAME:String = "[Polling::PollingWindowManager] ";
		
		public function PollingWindowManager(service:PollingService) {
		  LogUtil.debug(LOGNAME +" inside constructor");	
		  this.service = service;
		  globalDispatcher = new Dispatcher();
		}
				
		//PollingInstructionsWindow.mxml Window Event Handlers
		//##########################################################################
		public function handleOpenPollingInstructionsWindow(e:PollingInstructionsWindowEvent):void{
			LogUtil.debug(LOGNAME + "inside handleOpenPollingInstructionsWindow");
			instructionsWindow = new PollingInstructionsWindow();
			
			// Use the PollGetTitlesEvent to fetch a list of already-used titles
			LogUtil.debug(LOGNAME + "Dispatching event to create list of titles on Instruction Window Creation");
			var getTitlesEvent:PollGetTitlesEvent = new PollGetTitlesEvent(PollGetTitlesEvent.CHECK);
			globalDispatcher.dispatchEvent(getTitlesEvent);
			openWindow(instructionsWindow);
		}
		
		public function handleOpenPollingInstructionsWindowWithExistingPoll(e:OpenSavedPollEvent):void{
			LogUtil.debug(LOGNAME + "inside handleOpenPollingInstructionsWindowWithExistingPoll");
			instructionsWindow = new PollingInstructionsWindow();
			
			// Use the PollGetTitlesEvent to fetch a list of already-used titles
			LogUtil.debug(LOGNAME + "Dispatching event to create list of titles on Instruction Window Creation");
			var getTitlesEvent:PollGetTitlesEvent = new PollGetTitlesEvent(PollGetTitlesEvent.CHECK);
			globalDispatcher.dispatchEvent(getTitlesEvent);
			LogUtil.debug(LOGNAME + "Passing poll object to Instruction window.....");
			if (e.poll != null){
				LogUtil.debug(LOGNAME + "Sending a non-null poll object to Instruction window");
				e.poll.checkObject();
				instructionsWindow.incomingPoll = new PollObject();
				instructionsWindow.incomingPoll = e.poll;
				instructionsWindow.editing = true;
				LogUtil.debug(LOGNAME + "Examining poll in instruction window");
				instructionsWindow.incomingPoll.checkObject();
			}else{
				LogUtil.debug(LOGNAME + "Tried to a null poll object to Instruction window");
			}
			
			openWindow(instructionsWindow);
		}
		
		public function handleClosePollingInstructionsWindow(e:PollingInstructionsWindowEvent):void{
			LogUtil.debug(LOGNAME + " inside handleClosePollingInstructionsWindow");
			closeWindow(instructionsWindow);
			//service.setPolling(true);
		}
		
		// Checking the polling status to prevent a presenter from publishing two polls at a time
		  public function handleCheckStatusEvent(e:PollingStatusCheckEvent):void{
			  LogUtil.debug(LOGNAME +" inside handleCheckStatusEvent ");
			  e.allowed = !service.getPollingStatus();
			  instructionsWindow.publishingAllowed = e.allowed;
		  }
		  
		  public function handleCheckTitlesInInstructions(e:PollGetTitlesEvent):void{
			  LogUtil.debug(LOGNAME +" inside handleCheckTitlesInInstructions ");
			  LogUtil.debug(LOGNAME +" BINGO titles going into instructions: " + e.titleList);
			  instructionsWindow.invalidTitles = e.titleList;
		  }
		//PollingAcceptWindow.mxml Window Event Handlers
		//##########################################################################
		public function handleOpenAcceptPollingInstructionsWindow(e:AcceptPollingInstructionsWindowEvent):void{
			LogUtil.debug(LOGNAME + "inside handleOpenAcceptPollingInstructionsWindow");
			acceptInstructionsWindow = new PollingAcceptWindow();
			openWindow(acceptInstructionsWindow);
			//service.setPolling(false);
		}
		
		public function handleCloseAcceptPollingInstructionsWindow(e:AcceptPollingInstructionsWindowEvent):void{
			LogUtil.debug(LOGNAME + " inside handleCloseAcceptPollingInstructionsWindow");
			closeWindow(acceptInstructionsWindow);
		}
		
		
		//##########################################################################
		
		public function handleStartPollingEvent():void{
			LogUtil.debug(LOGNAME+ "calling service to share PollingWindow");
		}
		
		
		// Action makers (function that actually act on the windows )
		//#############################################################################
		public function openWindow(window:IBbbModuleWindow):void{
			LogUtil.debug(LOGNAME + " inside openWindow calling OpwnWindowEvent with window: " + window );				
			var windowEvent:OpenWindowEvent = new OpenWindowEvent(OpenWindowEvent.OPEN_WINDOW_EVENT);
			windowEvent.window = window;
			globalDispatcher.dispatchEvent(windowEvent);
			LogUtil.debug(LOGNAME + " " + window +" is opened ");
		}
		
		private function closeWindow(window:IBbbModuleWindow):void{
			LogUtil.debug(LOGNAME + " inside closeWindow calling CloseWindowEvent with window: " + window );				
			var windowEvent:CloseWindowEvent = new CloseWindowEvent(CloseWindowEvent.CLOSE_WINDOW_EVENT);
			windowEvent.window = window;
			globalDispatcher.dispatchEvent(windowEvent);
			LogUtil.debug(LOGNAME + " " + window +" is closed ");
		}
		//#################################################################################

		
		// PollingViewWindow.mxml Window Handlers 
		//#########################################################################
		public function handleOpenPollingViewWindow(e:PollingViewWindowEvent):void{
			LogUtil.debug(LOGNAME + "inside handleOpenPollingViewWindow");
			LogUtil.debug(LOGNAME + "Event.title = " + e.title);
			pollingWindow = new PollingViewWindow();
			pollingWindow.title = e.title;
			pollingWindow.question = e.question;
			pollingWindow.isMultiple = e.isMultiple;
			pollingWindow.answers = e.answers;
			openWindow(pollingWindow);
			//service.setPolling(true);
		}
		
		public function handleClosePollingViewWindow(e:PollingViewWindowEvent):void{
			LogUtil.debug(LOGNAME + " inside handleClosePollingViewWindow");
			closeWindow(pollingWindow);
			LogUtil.debug(LOGNAME + " After closeWindow");
			//service.setPolling(false);
		}
		
		public function handleStopPolling(e:StopPollEvent):void{
			LogUtil.debug(LOGNAME + " inside handleStopPolling");
			service.setPolling(false);
		}
		//##########################################################################
		
		
		// PollingStatsWindow.mxml Window Handlers 
		//#########################################################################
		public function handleOpenPollingStatsWindow(e:PollingStatsWindowEvent):void{
			LogUtil.debug(LOGNAME + "inside handleOpenPollingStatsWindow");
			LogUtil.debug(LOGNAME + "Event.title = " + e.title);
			statsWindow = new PollingStatsWindow();
			statsWindow.title = e.title;
			statsWindow.question = e.question;
			statsWindow.isMultiple = e.isMultiple;
			statsWindow.answers = e.answers;
			statsWindow.votes = e.votes;
			openWindow(statsWindow);
			service.setPolling(true);
		}
		
		public function handleClosePollingStatsWindow(e:PollingStatsWindowEvent):void{
			LogUtil.debug(LOGNAME + " inside handleClosePollingStatsWindow");
			closeWindow(statsWindow);
		}
		
		public function handleRefreshPollingStatsWindow(e:PollRefreshEvent):void{
			LogUtil.debug(LOGNAME + " inside handleRefreshPollingStatsWindow");
			statsWindow.refresh(e.votes, e.totalVotes);
		}
		//##########################################################################
	}
}