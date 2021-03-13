package ElevatorSubsystem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;

import Timer.TestTimer;
import Timer.TimerController;
import common.RPC;
import common.Common;
import test.Test;

/**
 * @author Gill
 *
 */
public class Elevator implements Runnable {
	// Constants
	public int curFloor;
	private int elevNum;
	private int targetFloor;
	private ArrayList<Integer> destFloors; //ArrayList to store the floors to stop at
	private boolean doorStatus;  //True means door is closed, false for open door
	private MotorState state;         
	private Up up = new Up();
	private Down down = new Down();    // The 3 states as per the motor interface
	private Idle idle = new Idle();
	private RPC transmitter;
	private final InetAddress addr;
	private TimerController timer;
	private int destPort;
	private int recPort;

	// Constructor
	public Elevator(int elevNum, int curFloor, boolean doorStatus, int destPort, int recPort) throws UnknownHostException {   
		this.curFloor = curFloor;     
		this.doorStatus = doorStatus;            //Initializing variables
		this.destPort = destPort;
		this.recPort = recPort;
		addr = InetAddress.getLocalHost();
		destFloors = new ArrayList<Integer>();
		state = idle;   // setting motor state to idle
		transmitter = new RPC(addr, destPort, recPort);
		this.elevNum = elevNum;
		timer = new TimerController(1000/Test.SPEED, new Elevator(elevNum, curFloor, false, destPort, recPort));
	}

	//Method selectFloor adds more floors to stop at into the destination arrayList "destFloors"
	public void selectFloor(int[] floors) {
		for(int i : floors) {
			destFloors.add((Integer)i);
		}
	}

	//Method addDest sets the new target floor to move towards
	public void addDest(int floor) throws UnknownHostException {
		
		targetFloor = floor;
		destFloors.add((Integer)floor);
		System.out.println("\nGoing from floor " + curFloor + " to floor " + floor);

		//If statements to checks the location of the destination floor relative to the current floor
		if(curFloor > floor) {
			System.out.println("Elevator State Change: GOING DOWN @ time = " + LocalTime.now());  //Going down if target floor is lower
			state = up;
		} else if (floor > curFloor) {
			System.out.println("Elevator State Change: GOING UP @ time = " + LocalTime.now());  //Going up if target floor is higher
			state = down;
		} else {
			System.out.println("Same floor. No state change\n");  //No movement if same floor
			removeFloor(floor);
			return;
		}
		timer = new TimerController(1000 * (Math.abs(curFloor - floor))/Test.SPEED, new Elevator(elevNum, curFloor, false, destPort, recPort));
		timer.start();

		//		try {
		//			Thread.sleep(1000 * (Math.abs(curFloor - floor)));  //delay
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		
	}

	public void notifyElev() {
		openDoor();
		System.out.println("Elevator State Change: IN IDLE @ time = " + LocalTime.now() + "\n");  //Printing time stamps
		state = idle;    // set state to idle
		removeFloor(targetFloor);  //calls method remove floor to remove it from the arraylist
		curFloor = targetFloor;  //sets the new current floor
	}

	//Method removeFloor takes chosen floor and removes it from the Arraylist of destination floors
	public void removeFloor(int floor) {
		destFloors.remove((Integer)floor);
	}

	public byte[] getInfo() {
		return null;
	}

	//Method openDoor sets the door status to false to signify an open door
	private void openDoor() {
		this.doorStatus = false;
	}

	//Method closeDoor sets door status to true for closed door
	private void closeDoor() {
		this.doorStatus = true;
	}

	// Getter getCurrState to retrieve the current motor state
	public MotorState getCurrState() {
		return state;
	}

	//getter getCurrFloor to return the current floor number
	public int getCurrFloor() {
		return curFloor;
	}
	
	public void receive() {
		
		byte[] checkMsg;
		byte[] receiveMsg;
		checkMsg = encodeConfirmation(CONFIRMATION.CHECK);
		transmitter.sendPacket(checkMsg);
		receiveMsg = transmitter.receivePacket();
		
		if (receiveMsg == null) {
			return;
		}
		if (Common.findType(receiveMsg) == Common.CONFIRMATION.NO_MSG) {
			System.out.println("no msg");
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			receive();
			transmitter.sendPacket(null);
			transmitter.receivePacket();
			//Common.type.Confiormation.check
		}

	}

}
