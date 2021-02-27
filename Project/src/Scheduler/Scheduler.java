package Scheduler;

import java.util.*;
import common.*;

/**
 * @author Yisheng Li
 *
 */

public class Scheduler {

	private ElevtState[] elevtStates; // also is the state of the scheduler
	private FloorState[] floorStates;

	private Queue<byte[]>   msgToElevtSub, msgToFloorSub;
	
	private Common common = new Common();




	/**
	 * Constructor
	 *
	 * @param totalElevts total Elevts number
	 * @param totalElevts total Floors number
	 */
	public Scheduler (int totalElevts, int totalFloors) {
		elevtStates = new ElevtState[totalElevts];
		floorStates = new FloorState[totalFloors];

	}



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

		elevtStates[elevt-1].setFloor(floor);
		elevtStates[elevt-1].setDir(dir);
		elevtStates[elevt-1].setDest(dest);


    	updateSchedule();
        return;
    }

	/**
	 *
	 * @param msg The message sent by the floor subsystem.
	 */
	public void floorSubAddMsg (byte[] msg) {
		System.out.println("Scheduler got message from floor sub" );

		int[] message = common.decode(msg);

		int floor = message[0];
		int dir = message[1];


		byte[] oneMsgToElevtSub = common.encodeScheduler(1, floor,0);
		msgToElevtSub.offer(oneMsgToElevtSub);

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



	}






   
    
}