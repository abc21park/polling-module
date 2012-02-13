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

package org.bigbluebutton.conference.service.recorder.polling;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.servlet.ServletRequest;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.Red5;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import org.apache.commons.lang.time.DateFormatUtils;

import org.bigbluebutton.conference.service.poll.Poll;


public class PollInvoker {
    private static Logger log = Red5LoggerFactory.getLogger( PollInvoker.class, "bigbluebutton");
    
    JedisPool redisPool;

    public PollInvoker(){
    	super();
    	log.debug("[TEST] Initializing PollInvoker");
    }
    
    public JedisPool getRedisPool() {
   	 return redisPool;
   }

   public void setRedisPool(JedisPool pool) {
   	 this.redisPool = pool;
   }
   
   public Jedis dbConnect(){
	   	// Reads IP from Java, for portability
	       String serverIP = "INVALID IP";
	       try
	       {
	       	InetAddress addr = InetAddress.getLocalHost();
	           // Get hostname
	           String hostname = addr.getHostName();
	           serverIP = hostname;
	       	log.debug("[TEST] IP capture successful, IP is " + serverIP);
	       } catch (Exception e)
	       {
	       	log.debug("[TEST] IP capture failed...");
	       }
	       
	       JedisPool redisPool = new JedisPool(serverIP, 6379);
	       return redisPool.getResource();
   }
	   
   
   // The invoke method is called after already determining which poll is going to be used
   // (ie, the presenter has chosen this poll from a list and decided to use it, or it is being used immediately after creation)
   public Poll invoke(String pollKey){
	   log.debug("[TEST] inside PollInvoker invoke for key: " + pollKey);
	   
	   Jedis jedis = dbConnect();   
       
       if (jedis.exists(pollKey))
       {
    	   // Get Boolean values from string-based Redis hash
    	   boolean pMultiple = false;
    	   boolean pStatus = false;
    	   if (jedis.hget(pollKey, "multiple").compareTo("true") == 0)
    		   pMultiple = true;
    	   if (jedis.hget(pollKey, "status").compareTo("true") == 0) 
    		   pStatus = true;
		
    	   // ANSWER EXTRACTION
    	   long pollSize = jedis.hlen(pollKey);
    	   // otherFields is defined in Poll.java as the number of fields the hash has which are not answers or votes.
    	   long numAnswers = (pollSize-Poll.getOtherFields())/2;
       
    	   // Create an ArrayList of Strings for answers, and one of ints for answer votes
    	   ArrayList <String> pAnswers = new ArrayList <String>();
    	   ArrayList <Integer> pVotes = new ArrayList <Integer>();
    	   for (int j = 1; j <= numAnswers; j++)
    	   {
    		   pAnswers.add(jedis.hget(pollKey, "answer"+j+"text"));
    		   pVotes.add(Integer.parseInt(jedis.hget(pollKey, "answer"+j)));
    	   }
    	       	   
    	   ArrayList retrievedPoll = new ArrayList();
    	   
    	   retrievedPoll.add(jedis.hget(pollKey, "title"));
    	   retrievedPoll.add(jedis.hget(pollKey, "room"));
    	   retrievedPoll.add(pMultiple);
    	   retrievedPoll.add(jedis.hget(pollKey, "question"));
    	   // answers
		   retrievedPoll.add(pAnswers);
    	   // votes
		   retrievedPoll.add(pVotes);
    	   retrievedPoll.add(jedis.hget(pollKey, "time"));
    	   retrievedPoll.add(jedis.hget(pollKey, "totalVotes"));
    	   retrievedPoll.add(pStatus);
		   retrievedPoll.add(jedis.hget(pollKey, "didNotVote"));    	   
    	   
		   Poll poll = new Poll(retrievedPoll);
    	   return poll;
       }
       log.error("[ERROR] A poll is being invoked that does not exist. Null exception will be thrown.");
       //redisPool.returnResource(jedis);
       return null;
   }
   
   // Gets the ID of the current room, and returns a list of all available polls.
   public ArrayList <String> titleList()
   { 
	   log.debug("Entering PollInvoker titleList");
	   Jedis jedis = dbConnect();
       String roomName = Red5.getConnectionLocal().getScope().getName();
	   ArrayList <String> pollTitleList = new ArrayList <String>(); 
       for (String s : jedis.keys(roomName+"*"))
       {
    	   log.debug("[TEST] Getting titles, key is " + s);
    	   pollTitleList.add(jedis.hget(s, "title"));
    	   log.debug("[TEST] Getting titles, title is " + jedis.hget(s, "title"));
       }
       log.debug("[TEST] titleList is " + pollTitleList);
       log.debug("Leaving PollInvoker titleList");
	   return pollTitleList;
   }
   
   public ArrayList <String> statusList()
   { 
	   log.debug("Entering PollInvoker statusList");
	   Jedis jedis = dbConnect();
       String roomName = Red5.getConnectionLocal().getScope().getName();
	   ArrayList <String> pollStatusList = new ArrayList <String>(); 
       for (String s : jedis.keys(roomName+"*"))
       {
    	   log.debug("[TEST] Getting status, key is " + s);
    	   pollStatusList.add(jedis.hget(s, "status"));
    	   log.debug("[TEST] Getting status, status is " + jedis.hget(s, "status"));
       }
       log.debug("[TEST] statusList is " + pollStatusList);
       log.debug("Leaving PollInvoker statusList");
       return pollStatusList;
   }
}
