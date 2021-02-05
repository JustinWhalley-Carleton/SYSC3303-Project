package Scheduler;

import java.util.*;

public class Scheduler {
	
	private Hashtable<Integer, String> requestFromElevator = new Hashtable<Integer, String>();
	private Hashtable<Integer, String> requestFromFloor = new Hashtable<Integer, String>();
	private Hashtable<Integer, String> messageForElevator = new Hashtable<Integer, String>();
	private Hashtable<Integer, String> messageForFloor = new Hashtable<Integer, String>();
	

    /**
     * 
     * @param num   which elevator
     * @param message The message sent by the elevator object.
     */
    public synchronized void elevatorAddRequest(Integer num, String message) {
    	System.out.println("Scheduler got request from Elevator# " + num + " request " + message );
    	requestFromElevator.put(num, message);
    	updateMessageForFloor(num);
        notifyAll();
        return;
    }
    
    /**
     * 
     * @return message for the elevator sub system
     */
    public synchronized String elevatorCheckRequest(Integer num) {
    	
    	if (messageForElevator.get(num) == null) {
    		return null;
    	}
    	else {
    		String message = messageForElevator.get(num);
            messageForElevator.remove(num);
            return message;
    	}
       
        
    }
 
    
    
    /**
     * 
     * @param num   which floor
     * @param message The message sent by the floor object.
     */
    public synchronized void floorAddRequest(Integer num, String message) {
    	System.out.println("Scheduler got request from Floor# " + num + " request " + message );
    	
    	requestFromFloor.put(num, message);
    	updateMessageForElevator(num);
    	notifyAll();
    }
    
    /**
     * 
     * 
     * @return message for the floor sub system
     */
    public synchronized String floorCheckRequest(Integer num) {
    	if (messageForFloor.get(num) == null) {
    		return null;
    	} else {
    		String message = messageForFloor.get(num);
            messageForFloor.remove(num);
            return message;
    	}
    	
    	
    }
    
    
    /**
     * Update message for floor sub system. i.e. schedule for floors
     *
     */
    
    public void updateMessageForFloor(Integer num) {
    	messageForFloor.put(num, requestFromElevator.get(num));
		System.out.println("Finished updating schedule");
	}
    
    /**
     * Update message for floor sub system. i.e. schedule for Elevator
     *
     */
    
    public void updateMessageForElevator(Integer num) {
    	messageForElevator.put(num, requestFromFloor.get(num));
		System.out.println("Finished updating schedule");
	}
    
   
    
}
