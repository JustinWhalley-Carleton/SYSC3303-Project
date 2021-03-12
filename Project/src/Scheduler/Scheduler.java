package Scheduler;

import java.time.LocalTime;
import java.io.IOException;
import java.net.*;
import java.util.*;
import common.*;

/**
 * @author Yisheng Li
 *
 */

public class Scheduler implements Runnable {

	private int inState = 0; // 0 is wait; 1 is sending; -1 is receiving  
	private ElevtState[] elevtStates; // also is the state of the scheduler
	private FloorState[] floorStates;
	private Queue<byte[]>   msgFromFloorSub, msgToElevtSub, msgToFloorSub;

	public RPC rpcFloor, rpcElevt;

	/**
	 * Constructor
	 *
	 * @param totalElevts total Elevts number
	 * @param totalElevts total Floors number
	 */
	public Scheduler (int totalElevts, int totalFloors) throws Exception {
		elevtStates = new ElevtState[totalElevts];
		floorStates = new FloorState[totalFloors];
		msgFromFloorSub = new LinkedList<byte[]>();
		msgToElevtSub = new LinkedList<byte[]>();
		msgToFloorSub = new LinkedList<byte[]>();
		
		for (int i=0;i<elevtStates.length;i++) { elevtStates[i]= new ElevtState(i+1); }
		for (int i=0;i<floorStates.length;i++) { floorStates[i]= new FloorState(i+1); }

		rpcElevt = new RPC(InetAddress.getLocalHost(),10001,10000);
		rpcFloor = new RPC(InetAddress.getLocalHost(),10002,10000);

	}



	// sub send:

    /**
	 * update
	 *
     * @param msg The message sent by the elevator subsystem.
     */
    public void elevtSubAddMsg (byte[] msg) {
    	this.inState = -1;
    	int[] message = Common.decode(msg);
		
    	System.out.println("Scheduler got message from elevtSub: " + Arrays.toString(message));
	
		
		int elevt = message[0];
		int floor = message[1];
		int dir = message[2];
		int dest = message[3];

		elevtStates[elevt-1].setFloor(floor);
		elevtStates[elevt-1].setDir(dir);
		elevtStates[elevt-1].setDest(dest);


		//if the the elevator stops on a floor, dismiss floor buttons
		if (dir == 0){
			if (floor != 1) {
				byte[] oneMsgToFloorSub = Common.encodeScheduler(1, floor,0);
				System.out.println("Scheduler sent message to FloorSub: " +  Arrays.toString(Common.decode(oneMsgToFloorSub)) + " @ time = " + LocalTime.now());
				msgToFloorSub.offer(oneMsgToFloorSub);
			}
			if (floor != floorStates.length) {
				byte[] oneMsgToFloorSub = Common.encodeScheduler(1, floor,1);
				System.out.println("Scheduler sent message to FloorSub: " +  Arrays.toString(Common.decode(oneMsgToFloorSub)) + " @ time = " + LocalTime.now());
				msgToFloorSub.offer(oneMsgToFloorSub);
			}
		}

    	updateSchedule();
    	this.inState = 0;
        return;
    }

	/**
	 *
	 * @param msg The message sent by the floor subsystem.
	 */
	public void floorSubAddMsg (byte[] msg) {
		this.inState = -1;




		int[] message = Common.decode(msg);
		System.out.println("Scheduler got message from floor sub: " + Arrays.toString(message)  + " @ time = " + LocalTime.now());
		int floor = message[0];
		int dir = message[1];


		byte[] oneMsgToElevtSub = Common.encodeScheduler(1, floor,0);
		System.out.println("Scheduler sent message to ElevtSub: " +  Arrays.toString(Common.decode(oneMsgToElevtSub)) + " @ time = " + LocalTime.now());

		msgToElevtSub.offer(oneMsgToElevtSub);

		updateSchedule();
		this.inState = 0;
		return;
	}




    // sub get:
    /**
     * 
     * @return a message to the elevator subsystem
     */
    public byte[] elevtSubCheckMsg() {
    	this.inState = 1;
    	byte[] msg = msgToElevtSub.poll();
    	this.inState = 0;
    	return msg;
	}


	/**
	 *
	 * @return message to the floor sub system
	 */
	public byte[] floorSubCheckMsg() {
    	this.inState = 1;
    	byte[] msg = msgToFloorSub.poll();
    	this.inState = 0;

		return msg;
	}



    /**
	 * Update elevtStates a msgToElevtSub Schedule based on all data
	 *
	 */
	private void updateSchedule() {

	}
	/**
	 * Recevice massgaes from FloorSub and ElevtSub
	 */
	private void receive() {
		byte[] message1 = rpcFloor.receivePacket();
		if (message1 != null){
			floorSubAddMsg(message1);
		}

		byte[] message2 = rpcElevt.receivePacket();
		if (message2 != null){
			elevtSubAddMsg(message2);
		}
		return;
	}

	/**
	 * Send massgaes to FloorSub and ElevtSub if there is any
	 */
	private void send(){
		byte[] msg = floorSubCheckMsg();
		if ( msg != null) {
			rpcFloor.sendPacket(msg);
		}
		msg = elevtSubCheckMsg();
		if ( msg != null) {
			rpcElevt.sendPacket(msg);
		}
	}

	@Override
	public void run() {
		while (true) {
			receive();
			send();
		}
	}
}