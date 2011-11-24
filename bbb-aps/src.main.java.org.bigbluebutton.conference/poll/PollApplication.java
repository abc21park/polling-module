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

package org.bigbluebutton.conference.service.poll;

import org.slf4j.Logger;
import org.red5.logging.Red5LoggerFactory;
import org.red5.compatibility.flex.messaging.io.ArrayCollection;   

import java.util.Map;
import java.util.Arrays;
import org.bigbluebutton.conference.RoomsManager;
import org.bigbluebutton.conference.Room;
import org.bigbluebutton.conference.Participant;
import org.bigbluebutton.conference.IRoomListener;


public class PollApplication {

	private static Logger log = Red5LoggerFactory.getLogger( PollApplication.class, "bigbluebutton" );

	private RoomsManager roomsManager;
	private String LOGNAME = "[PollApplication]";
	
	public boolean createRoom(String name) {
		if(!roomsManager.hasRoom(name)){
			log.info("Creating room " + name);
			roomsManager.addRoom(new Room(name));
			return true;
		}
		return false;
	}
	
	public boolean destroyRoom(String name) {
		if (roomsManager.hasRoom(name)) {
			log.info("Destroying room " + name);
			roomsManager.removeRoom(name);
		} else {
			log.warn("Destroying non-existing room " + name);
		}
		return true;
	}
	
	public void destroyAllRooms() {
		roomsManager.destroyAllRooms();
	}
	
	public boolean hasRoom(String name) {
		return roomsManager.hasRoom(name);
	}
	
	public boolean addRoomListener(String room, IRoomListener listener) {
		if (roomsManager.hasRoom(room)){
			roomsManager.addRoomListener(room, listener);
			return true;
		}
		log.warn("Adding listener to a non-existant room " + room);
		return false;
	}
	
	public void savePoll(ArrayCollection answers, String question , String title , Boolean isMultiple, String room ) {	
	  log.info(LOGNAME + " Received answers " + answers + " compile testing On/OFF red 5 server, for tomorrows development ");
	}
}	
	
