package ElevatorSubsystem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;

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
	private boolean testing = false;

	/**
	 * no port elevator for junit testing
	 */
	public Elevator() {addr=null;testing=true;}
	
	// Constructor
	public Elevator(int elevNum, int curFloor, boolean doorStatus, int destPort, int recPort) throws UnknownHostException {   
		this.curFloor = curFloor;     
		this.doorStatus = doorStatus;            //Initializing variables

		addr = InetAddress.getLocalHost();
		destFloors = new ArrayList<Integer>();
		state = idle;   // setting motor state to idle
		transmitter = new RPC(addr, destPort, recPort);
		this.elevNum = elevNum;
		timer = new TimerController(1000/Test.SPEED, this);
	}

	//Method selectFloor adds more floors to stop at into the destination arrayList "destFloors"
	public void selectFloor(int[] floors) {
		for(int i : floors) {
			destFloors.add((Integer)i);
		}
	}

	//Method addDest sets the new target floor to move towards
	public void addDest(int floor) {
		
		targetFloor = floor;
		destFloors.add((Integer)floor);
		System.out.println("\n Elevator " + elevNum + " going from floor " + curFloor + " to floor " + floor);

		//If statements to checks the location of the destination floor relative to the current floor
		if(curFloor > floor) {
			System.out.println("Elevator " + elevNum + " State Change: GOING DOWN @ time = " + LocalTime.now());  //Going down if target floor is lower
			state = up;
		} else if (floor > curFloor) {
			System.out.println("Elevator " + elevNum + " State Change: GOING UP @ time = " + LocalTime.now());  //Going up if target floor is higher
			state = down;
		} else {
			System.out.println("Same floor. No state change\n");  //No movement if same floor
			removeFloor(floor);
			return;
		}
		timer = new TimerController(1000 * (Math.abs(curFloor - floor))/Test.SPEED, this);
		timer.start();
		
	}

	public void notifyElev() {
		if(testing) return;
		openDoor();
		System.out.println("Elevator " + elevNum + " State Change: IN IDLE @ time = " + LocalTime.now() + "\n");  //Printing time stamps
		state = idle;    // set state to idle
		removeFloor(targetFloor);  //calls method remove floor to remove it from the arraylist
		curFloor = targetFloor;  //sets the new current floor

		byte[] msg = Common.encodeElevator(elevNum, curFloor, state, targetFloor);
		transmitter.sendPacket(msg);
		msg = transmitter.receivePacket();
		closeDoor();
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
	
	// receive method that first sends a check request to elevatorSubsystem
	// and then receives instructions for a specific elevator
	public void receive() {
		
		byte[] checkMsg;  //byte array variables for the msgs
		byte[] receiveMsg;
		checkMsg = Common.encodeConfirmation(Common.CONFIRMATION.CHECK); //using the Common.java to encode check msg
		transmitter.sendPacket(checkMsg);  //sends the msg request to elevator subsystem using UDP

		receiveMsg = transmitter.receivePacket();  //stores the elevatorSubsystem's response in byte array
		
		if (receiveMsg == null) {
			return;
		}
		if (Common.findType(receiveMsg) == Common.TYPE.CONFIRMATION){     // checks to determine the type of msg using Common.java
			if( Common.findConfirmation(receiveMsg) == Common.CONFIRMATION.NO_MSG) {
				// System.out.println("no msg");
			}

		} else{
			int received[] = Common.decode(receiveMsg);  //decode the received msg that stores the info in an integer array
			addDest(received[1]);   //Common.java identifies msg[1] as destination floor

		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub


		while (true) {

			receive();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}

	}

}
