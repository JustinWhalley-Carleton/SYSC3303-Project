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

	private final byte[] CheckMSG = Common.encodeConfirmation(Common.CONFIRMATION.CHECK);

	private int inState = 0; // 0 is wait; 1 is sending; -1 is receiving  
	private ElevtState[] elevtStates; // also is the state of the scheduler
	private FloorState[] floorStates;
	private Queue<byte[]>   msgFromFloorSub, msgToElevtSub, msgToFloorSub;

	private RPC rpcFloor, rpcElevt;

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


    /**
	 *
	 *
     * @param msg The message sent by the elevator subsystem.
     */
	private void elevtSubAddMsg (byte[] msg) {
		int[] message = Common.decode(msg);
		
    	//System.out.println("Scheduler got message from elevtSub: " + Arrays.toString(message));
		int elevt = message[0];
		int floor = message[1];
		int dir = message[2];
		int dest = message[3];

		// update elevt States
		elevtStates[elevt-1].setFloor(floor);
		elevtStates[elevt-1].setDir(dir);
		elevtStates[elevt-1].setDest(dest);

		//if the the elevator stops on a floor, dismiss floor buttons
		if (dir == 0){
			//TO-DO: find which (uyp/down) button to dismiss
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

        return;
    }

	/**
	 *
	 * @param msg The message sent by the floor subsystem.
	 */
	private void floorSubAddMsg (byte[] msg) {
		int[] message = Common.decode(msg);
		//System.out.println("Scheduler got message from floor sub: " + Arrays.toString(message)  + " @ time = " + LocalTime.now());
		int floor = message[0];
		int dir = message[1];


		//System.out.println("Scheduler sent message to ElevtSub: " +  Arrays.toString(Common.decode(oneMsgToElevtSub)) + " @ time = " + LocalTime.now());
		//msgFromFloorSub.offer(oneMsgFromFloorSub);
		int closestElevt = findClosestElevt(floor,dir);
		
		byte[] oneMsgToElevtSub = Common.encodeScheduler(closestElevt, floor,dir);
		msgToElevtSub.offer(oneMsgToElevtSub);
		return;
	}



	/**
	 * Send massgaes to FloorSub and ElevtSub if there is any
	 */
	private void sendReceiveFloorSub() {
		// send to FloorSub
		this.inState = 1;
		byte[] msgSend = msgToFloorSub.poll();
		if (msgSend != null) {
			rpcFloor.sendPacket(msgSend);
		} else {
			rpcFloor.sendPacket(CheckMSG);
		}
		this.inState = 0;

		// receive from FloorSub
		this.inState = -1;
		byte[] msgReceive = rpcFloor.receivePacket();
		if (Common.findType(msgReceive) != Common.TYPE.CONFIRMATION) {
			floorSubAddMsg(msgReceive);
		}
		this.inState = 0;
	}

	private void sendReceiveElevtSub(){
		// check ElevtSub
		this.inState = 1;
		byte[] msgSend = msgToElevtSub.poll();
		if ( msgSend != null) {
			rpcElevt.sendPacket(msgSend);
		} else{
			rpcElevt.sendPacket(CheckMSG);
		}
		this.inState = 0;
		// receive from ElevtSub

		this.inState = -1;
		byte[] msgReceive = rpcElevt.receivePacket();
		if (Common.findType(msgReceive) != Common.TYPE.CONFIRMATION){
			elevtSubAddMsg(msgReceive);
		}
		this.inState = 0;

	}


	/**
	 * Update elevtStates a msgToElevtSub Schedule based on all data
	 *
	 */
	private int findClosestElevt(int floor, int dir) {
		int result = 0;

		return result;
	}












	@Override
	public void run() {

		while (true) {
			try {
				sendReceiveFloorSub();
				this.wait(200);
				sendReceiveElevtSub();
				this.wait(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}