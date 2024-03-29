package Scheduler;

import java.time.LocalTime;
import java.net.*;
import java.util.*;
import FloorSubsystem.FileLoader;
import common.*;

/**
 * @author Yisheng Li
 *
 */

public class Scheduler implements Runnable {

	private final byte[] CheckMSG = Common.encodeConfirmation(Common.CONFIRMATION.CHECK);

	private int inProcessing = 0; // 0 is idle; 1 is processing;
	private int totalElevts;
	private int totalFloors = 0;
	public ElevtState[] elevtStates; // also is the state of the scheduler
	private FloorState[] floorStates;
	public Queue<byte[]> msgToElevtSub, msgToFloorSub;

	private long startMS, endMS;
	private LocalTime endTime;
	private boolean floorSubEnded, allSubEnded;

	private RPC rpcFloor, rpcElevt, rpcGUI;


	/**
	 * Constructor
	 *
	 * @param isTest
	 */
	public Scheduler (boolean isTest) throws Exception {
		Common.initializeVars();
		this.inProcessing = 0;
		this.totalElevts = Common.ELEVATORS;
		this.totalFloors = Common.FLOORS;;
		this.elevtStates = new ElevtState[totalElevts];
		this.floorStates = new FloorState[totalFloors];
		this.msgToElevtSub = new LinkedList<byte[]>();
		this.msgToFloorSub = new LinkedList<byte[]>();
		this.floorSubEnded = false;
		this.allSubEnded = false;

		for (int i=0;i<elevtStates.length;i++) { elevtStates[i]= new ElevtState(i+1); }
		for (int i=0;i<floorStates.length;i++) { floorStates[i]= new FloorState(i+1); }
		if(!isTest) {
			rpcElevt = new RPC(InetAddress.getLocalHost(), Common.ELEV_SUB_RECV_PORT, Common.SCHEDULER_RECV_ELEV_PORT);
			rpcFloor = new RPC(InetAddress.getLocalHost(),Common.FLOOR_SUB_RECV_PORT,Common.SCHEDULER_RECV_FLOOR_PORT);
			rpcGUI = new RPC(InetAddress.getLocalHost(),Common.GUI_RECV_SCHEDULER_PORT,Common.SCHEDULER_RECV_GUI_PORT);
			rpcElevt.setTimeout(2000);
			rpcFloor.setTimeout(2000);
		}
	}


    /**
	 *
	 *
     * @param msg The message sent by the elevator subsystem.
     */
	public void elevtSubAddMsg (byte[] msg) {

		int[] message = Common.decode(msg);
		int elevt = message[0];
		int floor = message[1];
		int dir = message[2];
		int dest = message[3];

		if(elevtStates[elevt-1].isStuck) return;

		// update elevt States
		elevtStates[elevt-1].setFloor(floor);
		elevtStates[elevt-1].setDir(dir);
		elevtStates[elevt-1].setDest(dest);

		//if the the elevator stops on a floor, dismiss floor buttons
		if (dir == 0 ){
			// dismiss down button
			if (floorStates[floor-1].getDown() == elevt) {
				floorStates[floor-1].setDown(0);
				byte[] oneMsgToFloorSub = Common.encodeScheduler(elevt, floor,0);
				msgToFloorSub.offer(oneMsgToFloorSub);
			}
			// dismiss up button
			if (floorStates[floor-1].getUp() == elevt) {
				floorStates[floor-1].setUp(0);
				byte[] oneMsgToFloorSub = Common.encodeScheduler(elevt, floor,1);
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

		Common.ELEV_ERROR errorMsg_receive = Common.ELEV_ERROR.decode(msg);

		if (errorMsg_receive == Common.ELEV_ERROR.RECOVER){
			System.out.println("Scheduler received RECOVER message from ElevtSub: ");
			Common.print(msg);
			//recover elevt
			elevtStates[elevNum-1].isStuck = false;
		}
		else {
			System.out.println("Scheduler received ERROR message from ElevtSub: ");
			Common.print(msg);
			//if it is an ELEV_ERROR message
			//set elevt stuck state
			elevtStates[elevNum-1].isStuck = true;
			if (destFloor == -1) return;

			//re schedule for destFloor
			doSchedule(destFloor, dirFloor);
		}

		return;
	}


	/**
	 *
	 * @param msg The message sent by the floor subsystem.
	 */
	public void floorSubAddMsg (byte[] msg) {
		int[] message = Common.decode(msg);
		int floor = message[0];
		int dir = message[1];

		doSchedule(floor, dir);
		return;
	}


	/**
	 *
	 * @param floor floor# of a pressed floor button
	 * @param dir direction of a pressed floor button
	 *
	 */

	private void doSchedule(int floor, int dir) {
		int closestElevt = findClosestElevt(floor,dir);
		if (closestElevt < 0 ) return;

		if( dir ==0 ){
			// set assigned elevt# to floor button
			floorStates[floor-1].setDown(closestElevt);
			// update elevt destion immediately in case the other floor button pressed immediately
			elevtStates[closestElevt-1].setDest(floor);
		} else {
			floorStates[floor-1].setUp(closestElevt);
			elevtStates[closestElevt-1].setDest(floor);
		}

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
			if (elevtStates[i].isStuck) {
				distances[i] = Integer.MAX_VALUE;
			}
			else {
				// if there is already an elevator coming to this floor, assign another elevator if there is one
				if (elevtStates[i].getDest() == floor && elevtStates[i].getDir() !=0){
					distances[i] = 90000;
				} else {
					distances[i] = findDistance(floor, dir, elevtStates[i]);
				}
				
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
		}
		else if ((floorDiff > 0 && elevtDest < elevtFloor && elevtDir == -1) || (floorDiff < 0 && elevtDest > elevtFloor && elevtDir == 1)) { // on elevt's way
			distance = Math.min(Math.abs(elevtFloor - floor),Math.abs(elevtDest - floor));
		}
		else { // elevt needs turn around
			if (elevtDir == -1) { // elevt is going down
				distance = elevtFloor + floor;
			} else{ // elevt is going up
				distance = (this.totalFloors - elevtFloor) + (this.totalFloors - floor);
			}
		}
		return distance;
	}

	/**
	 *
	 * @return whether all elevators are idle
	 */
	private boolean checkElevtSubEnded() {
		boolean result = true;
		for (int i = 0; i < totalElevts; i++) {
			if (elevtStates[i].getDest() != elevtStates[i].getFloor()) {
				result = false;
			}
		}
		return result;
	}




	// communicate with FloorSub
	private void sendReceiveFloorSub() {
		// send to FloorSub
		byte[] msgSend = msgToFloorSub.poll();
		if (msgSend != null) {
			rpcFloor.sendPacket(msgSend);
			System.out.println("Scheduler sending message to FloorSub: ");
			Common.print(msgSend);
			rpcGUI.sendPacket(msgSend);
		}
		else {
			rpcFloor.sendPacket(CheckMSG);
		}

		// receive from FloorSub
		byte[] msgReceive = rpcFloor.receivePacket();
		if(msgReceive == null) {
			return;
		}
		this.inProcessing = 1;
		long s = System.nanoTime();
		FileLoader.logToFile("Scheduler received processing for FloorSub request at nanoseconds: " + s);
		if (Common.findType(msgReceive) == Common.TYPE.CONFIRMATION) {

			Common.CONFIRMATION comfirmMsg_receive = Common.CONFIRMATION.findConfirmation(msgReceive[1]);

			if (comfirmMsg_receive == Common.CONFIRMATION.END) {
				System.out.println("FloorSub ended");
				floorSubEnded = true;
			}

		}
		else {
			floorSubAddMsg(msgReceive);
			System.out.println("Scheduler received message from FloorSub: ");
			Common.print(msgReceive);

		}
		this.inProcessing = 0;
		long e = System.nanoTime();
		FileLoader.logToFile("Scheduler finished processing request for FloorSub at nanoseconds: " + e);
		long pTime = e-s;
		FileLoader.logToFile("Scheduler processing time for FloorSub in nanoseconds: " + pTime);

	}

	// communicate with ElevtSub
	private void sendReceiveElevtSub(){
		// check ElevtSub
		byte[] msgSend = msgToElevtSub.poll();
		if ( msgSend != null) {
			rpcElevt.sendPacket(msgSend);
			System.out.println("Scheduler sent message to ElevtSub: ");
			Common.print(msgSend);

		} else{
			rpcElevt.sendPacket(CheckMSG);
		}

		// receive from ElevtSub
		byte[] msgReceive = rpcElevt.receivePacket();

		if(msgReceive ==null) {
			return;
		}

		this.inProcessing = 1;
		long s = System.nanoTime();
		FileLoader.logToFile("Scheduler received processing for ElevtSub request at nanoseconds: " + s);
		if (Common.findType(msgReceive) == Common.TYPE.ELEV_ERROR){
			//rpcGUI.sendPacket(msgReceive);
			elevtSubAddErrorMsg(msgReceive);
			rpcGUI.sendPacket(msgReceive);

		} else if (Common.findType(msgReceive) != Common.TYPE.CONFIRMATION) {
			System.out.println("Scheduler received message from ElevtSub: ");
			Common.print(msgReceive);
			//rpcGUI.sendPacket(msgReceive);


			// if it is an normal ELEV message
			elevtSubAddMsg(msgReceive);
			System.out.println("Sending");
			rpcGUI.sendPacket(msgReceive);
		}
		this.inProcessing = 0;
		long e = System.nanoTime();
		FileLoader.logToFile("Scheduler finished processing for ElevtSub request at nanoseconds: " + e);
		long pTime = e-s;
		FileLoader.logToFile("Scheduler processing for ElevtSub time in nanoseconds: " + pTime);
	}


	@Override
	public void run() {
		//Scheduler start
		startMS = System.currentTimeMillis();
		String start = "Scheduler started at: " + LocalTime.now();
		System.out.println(start);

		while (true) {
			try {
				sendReceiveFloorSub();
				Thread.sleep(200);
				sendReceiveElevtSub();
				Thread.sleep(200);

				if(floorSubEnded && checkElevtSubEnded() && msgToFloorSub.isEmpty() && msgToElevtSub.isEmpty()){
					if(!allSubEnded) { // bothSubEnded from false -> ture, updated timing result
						allSubEnded = true;
						endMS = System.currentTimeMillis();
						endTime = LocalTime.now();
					}
				} else {
					allSubEnded = false;
				}

				// if no elevt goes back to moving in 2.5s, finish and log timing into the file
				if (allSubEnded) {
					if ((System.currentTimeMillis() - endMS) > 2500){
						break;
					}
				}

			} catch (InterruptedException e) {
				System.exit(1);
			}
		}
		//Scheduler finish
		System.out.println("\n*******\n");
		String end = "Scheduler finished at: " + endTime;
		System.out.println(end);

		//Scheduler exection time
		long execTime =  endMS - startMS;
		String exec = "Scheduler execution time in ms: " + execTime;
		System.out.println(exec);

		// log Scheduler timing into the file
		FileLoader.logToFile(start);
		FileLoader.logToFile(end);
		FileLoader.logToFile(exec);
		FileLoader.logToFile("\n\n");
	}

	public static void main(String[] args) throws Exception {
		Thread scheduler = new Thread(new Scheduler(false));
		scheduler.start();
	}
}