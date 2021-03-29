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
	private int totalElevts;
	private int totalFloors = 0;
	private ElevtState[] elevtStates; // also is the state of the scheduler
	private FloorState[] floorStates;
	private Queue<byte[]> msgToElevtSub, msgToFloorSub;

	private RPC rpcFloor, rpcElevt;

	/**
	 * Constructor
	 *
	 * @param totalElevts total Elevts number
	 * @param totalElevts total Floors number
	 */
	public Scheduler (int totalElevts, int totalFloors) throws Exception {
		this.inState = 0;
		this.totalElevts = totalElevts;
		this.totalFloors = totalFloors;
		this.elevtStates = new ElevtState[totalElevts];
		this.floorStates = new FloorState[totalFloors];
		this.msgToElevtSub = new LinkedList<byte[]>();
		this.msgToFloorSub = new LinkedList<byte[]>();
		
		for (int i=0;i<elevtStates.length;i++) { elevtStates[i]= new ElevtState(i+1); }
		for (int i=0;i<floorStates.length;i++) { floorStates[i]= new FloorState(i+1); }

		rpcElevt = new RPC(InetAddress.getLocalHost(),10003, 10004);
		rpcFloor = new RPC(InetAddress.getLocalHost(),10001,10002);

	}


    /**
	 *
	 *
     * @param msg The message sent by the elevator subsystem.
     */
	private void elevtSubAddMsg (byte[] msg) {
		int[] message = Common.decode(msg);
		int elevt = message[0];
		int floor = message[1];
		int dir = message[2];
		int dest = message[3];

		// update elevt States
		if(elevtStates[elevt-1] == null) return;
		elevtStates[elevt-1].setFloor(floor);
		elevtStates[elevt-1].setDir(dir);
		elevtStates[elevt-1].setDest(dest);

		//if the the elevator stops on a floor, dismiss floor buttons
		if (dir == 0){

			if (dest < floor){
				byte[] oneMsgToFloorSub = Common.encodeScheduler(1, floor,0);
				msgToFloorSub.offer(oneMsgToFloorSub);
			}

			if (floor != 1 && (dest < floor || dest == floor)) {
				byte[] oneMsgToFloorSub = Common.encodeScheduler(1, floor,0);
				msgToFloorSub.offer(oneMsgToFloorSub);
			}
			if (floor != floorStates.length && (dest > floor || dest == floor)) {
				byte[] oneMsgToFloorSub = Common.encodeScheduler(1, floor,1);
				msgToFloorSub.offer(oneMsgToFloorSub);
			}

		}

        return;
    }


	/**
	 *
	 *
	 * @param msg The ERROR message sent by the elevator subsystem.
	 */
	private void elevtSubAddErrorMsg (byte[] msg) {
		int[] message = Common.decode(msg);
		int elevNum   = message[0];
		int curFloor  = message[1];
		int destFloor = message[2];
		int dirFloor  = message[3];


		// remove the stuck elev from scheduling list
		elevtStates[elevNum-1] = null;
		if(destFloor == -1) return;

		int closestElevt = findClosestElevt(destFloor,dirFloor);
		byte[] oneMsgToElevtSub = Common.encodeScheduler(closestElevt, destFloor, dirFloor);
		msgToElevtSub.offer(oneMsgToElevtSub);
		return;
	}


	/**
	 *
	 * @param msg The message sent by the floor subsystem.
	 */
	private void floorSubAddMsg (byte[] msg) {
		int[] message = Common.decode(msg);
		int floor = message[0];
		int dir = message[1];

		int closestElevt = findClosestElevt(floor,dir);
		if (closestElevt < 0 ) return;

		byte[] oneMsgToElevtSub = Common.encodeScheduler(closestElevt, floor,dir);
		msgToElevtSub.offer(oneMsgToElevtSub);
		return;
	}


	/**
	 *
	 * @param floor floor# of a pressed floor button
	 * @param dir direction of a pressed floor button
	 *
	 * @return the elevator# of which has the smallest distance to the floor
	 */

	private int findClosestElevt(int floor, int dir) {
		int result = 0;
		int[] distances = new int[totalElevts];
		// find distance for all elevators
		for (int i = 0; i < totalElevts; i++) {
			// skip stuck elevts
			if (elevtStates[i] != null) {
				int dis = findDistance(floor, dir, elevtStates[i]);
				distances[i] = dis;
			}
			else{
				distances[i] = 99999;
			}
		}

		// get the elevt# of which has the smallest distance to the floor
		int index = 0;
		int min = distances[0];
		for (int i = 1; i < distances.length; i++){
			if (distances[i] <= min){
				min = distances[i];
				index = i;
			}
		}

		if (min >= 99999){
			System.out.println("No elevator available!");
			System.exit(1);
		}

		result = index + 1;
		return result;
	}


	/**
	 *
	 * @param floor floor# of a pressed floor button
	 * @param dir direction of a pressed floor button
	 * @param es state of an elevator
	 *
	 * @return the distance of the floor and the elevator
	 */
	private int findDistance(int floor, int dir, ElevtState es){
		int distance = 0;
		int elevtFloor = es.getFloor();
		int elevtDir = 	es.getDir();
		int elevtDest = es.getDest();

		int floorDiff = elevtFloor - floor; // positive means floor is below the elevt; negative means above ;
		if (elevtFloor == elevtDest){
			distance = Math.abs(floorDiff);
		} else if ((floorDiff < 0 && dir == -1 && elevtDir == -1) || (floorDiff > 0 && dir == 1 && elevtDir == 1)) { // on elevt's way
			distance = Math.min(Math.abs(elevtFloor - floor),Math.abs(elevtDest - floor));
		} else { // elevt needs turn around
			if (elevtDir == -1) { // elevt is going down
				distance = elevtFloor + floor;
			} else{ // elevt is going up
				distance = (this.totalFloors - elevtFloor) + (this.totalFloors - floor);
			}
		}
		return distance;
	}


	// communicate with FloorSub
	private void sendReceiveFloorSub() {
		// send to FloorSub
		this.inState = 1;
		byte[] msgSend = msgToFloorSub.poll();
		if (msgSend != null) {
			rpcFloor.sendPacket(msgSend);
			System.out.println("Scheduler sent message to FloorSub: " +  Arrays.toString(Common.decode(msgSend)) + " @ time = " + LocalTime.now());

		} else {
			rpcFloor.sendPacket(CheckMSG);
		}
		this.inState = 0;

		// receive from FloorSub
		this.inState = -1;
		byte[] msgReceive = rpcFloor.receivePacket();
		if (Common.findType(msgReceive) != Common.TYPE.CONFIRMATION) {
			floorSubAddMsg(msgReceive);
			System.out.println("Scheduler received message from FloorSub: " + Arrays.toString(msgReceive)  + " @ time = " + LocalTime.now());

		}
		this.inState = 0;
	}

	// communicate with ElevtSub
	private void sendReceiveElevtSub(){
		// check ElevtSub
		this.inState = 1;
		byte[] msgSend = msgToElevtSub.poll();
		if ( msgSend != null) {
			rpcElevt.sendPacket(msgSend);
			System.out.println("Scheduler sent message to ElevtSub: " +  Arrays.toString(Common.decode(msgSend)) + " @ time = " + LocalTime.now());

		} else{
			rpcElevt.sendPacket(CheckMSG);
		}
		this.inState = 0;

		// receive from ElevtSub
		this.inState = -1;
		byte[] msgReceive = rpcElevt.receivePacket();


		if (Common.findType(msgReceive) == Common.TYPE.ELEV_ERROR){
			System.out.println("Scheduler received ERROR message from ElevtSub: " + Arrays.toString(msgReceive)  + " @ time = " + LocalTime.now());
			//if it is an ELEV_ERROR message
			elevtSubAddErrorMsg(msgReceive);

		} else if (Common.findType(msgReceive) != Common.TYPE.CONFIRMATION) {
			System.out.println("Scheduler received message from ElevtSub: " + Arrays.toString(msgReceive)  + " @ time = " + LocalTime.now());
			// if it is an normal ELEV message
			elevtSubAddMsg(msgReceive);

		}
		this.inState = 0;

	}


	@Override
	public void run() {
		while (true) {
			try {
				sendReceiveFloorSub();
				Thread.sleep(200);
				sendReceiveElevtSub();
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}