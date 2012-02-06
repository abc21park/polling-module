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
    	   // POPULATING POLL OBJECT WITH INFORMATION
    	   String pTitle = jedis.hget(pollKey, "title");
    	   String pQuestion = jedis.hget(pollKey, "question");
    	   boolean pMultiple = false;
    	   if (jedis.hget(pollKey, "multiple").compareTo("true") == 0) {pMultiple = true;}
    	   String pRoom = jedis.hget(pollKey, "room");
    	   String pTime = jedis.hget(pollKey, "time");
    	   String pTotalVotes = jedis.hget(pollKey, "totalVotes");
		
    	   // ANSWER EXTRACTION
    	   long pollSize = jedis.hlen(pollKey);
    	   // IMPORTANT! 
    	   // Increase the value of otherFields for each field you add to the hash which is not a new answer
    	   // (locales, langauge, etc)
    	   int otherFields = 6;
    	   long numAnswers = (pollSize-otherFields)/2;
       
    	   // Create an ArrayList of Strings for answers, and one of ints for answer votes
    	   ArrayList <String> pAnswers = new ArrayList <String>();
    	   ArrayList <Integer> pVotes = new ArrayList <Integer>();
    	   for (int j = 1; j <= numAnswers; j++)
    	   {
    		   pAnswers.add(jedis.hget(pollKey, "answer"+j+"text"));
    		   pVotes.add(Integer.parseInt(jedis.hget(pollKey, "answer"+j)));
    	   }
    	   
    	   Poll poll = new Poll(pTitle, pQuestion, pAnswers, pMultiple, pRoom, pVotes, pTime, Integer.parseInt(pTotalVotes));
    	   
    	   try
    	   {
    		   //redisPool.returnResource(jedis);
    		   log.debug("[TEST] Returning resource successfully in invoke");
    	   }
    	   finally{return poll;}
       }
       log.error("[ERROR] A poll is being invoked that does not exist. Null exception will be thrown.");
       //redisPool.returnResource(jedis);
       return null;
   }
   
   // Gets the ID of the current room, and returns a list of all available polls.
   public ArrayList <String> pollList()
   {
	   //Jedis jedis = PollApplication.dbConnect(); 
	   Jedis jedis = dbConnect();
	   
       String roomName = Red5.getConnectionLocal().getScope().getName();
	   ArrayList <String> pollKeyList = new ArrayList <String>(); 
       for (String s : jedis.keys(roomName+"\"*\""))
       {
    	   pollKeyList.add(s);
       }
	   
	   log.debug("[TEST] Listing available polls for room " + roomName);
	   for (String s : pollKeyList)
	   {
		   log.debug("[TEST] " + s);
	   }
	   
	   //redisPool.returnResource(jedis);
	   return pollKeyList;
   }
}
