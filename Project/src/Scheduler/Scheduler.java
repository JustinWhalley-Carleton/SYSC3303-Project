package Scheduler;

import java.util.*;
import common.*;

/**
 * @author Yisheng Li
 *
 */

public class Scheduler {

	private ElevtState[] elevtStates;
	private Queue<byte[]>  msgToElevtSub, msgToFloorSub;
	
	private Common common = new Common();
			

	// sub send:

    /**
	 * update
	 *
     * @param msg The message sent by the elevator subsystem.
     */
    public void elevtSubAddMsg (byte[] msg) {
    	System.out.println("Scheduler got message from elevt sub" );
		int[] message = common.decode(msg);

		int elevt = message[0];
		int floor = message[1];
		int dir = message[2];
		int dest = message[3];



		elevtStates[]


		//msgFromElevtSub.offer(msg);
    	updateSchedule();
        return;
    }

	/**
	 *
	 * @param msg The message sent by the floor subsystem.
	 */
	public void floorSubAddMsg (byte[] msg) {
		System.out.println("Scheduler got message from floor sub" );
		//msgFromFloorSub.offer(msg);
		updateSchedule();
		return;
	}




    // sub get:
    /**
     * 
     * @return a message to the elevator subsystem
     */
    public byte[] elevtSubCheckMsg() {

    	return msgToElevtSub.poll();
	}


	/**
	 *
	 * @return message to the floor sub system
	 */
	public byte[] floorSubCheckMsg() {

		return msgToFloorSub.poll();
	}




    /**
	 * Update elevtStates a msgToElevtSub Schedule based on all data
	 *
	 */
	private void updateSchedule() {
		while()
	}






   
    
}