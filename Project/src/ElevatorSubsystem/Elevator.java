package ElevatorSubsystem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;

import FloorSubsystem.FileLoader;
import Timer.TimerController;
import common.RPC;
import common.Common.ELEV_ERROR;
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
	
	private HashMap<Integer,Boolean> map;
	
	private boolean doorStatus;  //True means door is closed, false for open door
	private MotorState state;         
	private Up up = new Up();
	private Down down = new Down();    // The 3 states as per the motor interface
	private Idle idle = new Idle();
	private RPC transmitter;
	private TimerController timer;
	private boolean testing = false;
	private int NUM_FLOORS = Test.FLOORS;
	private ElevatorButton[] buttons;
	private boolean stuck=false;
	private FileLoader fileLoader;
	private String stuckMsg;
	

	/**
	 * no port elevator for junit testing
	 */
	public Elevator() {testing=true;}
	
	// Constructor
	public Elevator(int elevNum, int curFloor, boolean doorStatus, int destPort, int recPort/**,FileLoader fileLoader**/) throws UnknownHostException {   
		this.curFloor = curFloor;     
		this.doorStatus = doorStatus;            //Initializing variables
		state = idle;   // setting motor state to idle
		transmitter = new RPC(InetAddress.getLocalHost(), destPort, recPort);
		this.elevNum = elevNum;
		buttons = new ElevatorButton[NUM_FLOORS];
		for(int i = 0; i < NUM_FLOORS; i++) {
			buttons[i] = new ElevatorButton(i+1,false);
		}
		timer = new TimerController(1000/Test.SPEED,this);
		
		map = new HashMap<Integer,Boolean>();
		//this.filLoader = fileLoader;
	}

	//Method addDest sets the new target floor to move towards
	public void addDest(int floor,boolean type) {
		map.put((Integer)floor, (Boolean)type);
		move();
	}

	public void move() {
		int floor = getFloor();
		if(floor!=-1 && state == idle && !stuck) {
			System.out.println("\nElevator " + elevNum + " going from floor " + curFloor + " to floor " + floor);
			//If statements to checks the location of the destination floor relative to the current floor
			if(curFloor > floor) {
				System.out.println("Elevator " + elevNum + " State Change: GOING DOWN @ time = " + LocalTime.now());  //Going down if target floor is lower
				state = down;
			} else if (floor > curFloor) {
				System.out.println("Elevator " + elevNum + " State Change: GOING UP @ time = " + LocalTime.now());  //Going up if target floor is higher
				state = up;
			} else {
				System.out.println("Same floor. No state change\n");  //No movement if same floor
				/**
				if(stuckMsg != null) {
					if(stuckMsg.equals("StuckClosed")) {
						makeStuck(2);
					}
				}**/
				openDoor();
				/**
				if(stuckMsg != null) {
					if(stuckMsg.equals("StuckOpen")) {
						makeStuck(1);
					}
				}**/
				closeDoor();
				if(map.get(floor)) {
					removeFloor(floor);
					/**
					int nextFloor = fileLoader.popDestination();
					buttons[nextfloor-1].register();
					addDest(nextFloor, false);
					**/
					return;
				}
				removeFloor(floor);
				buttons[floor-1].reached();
				return;
			}
			timer.start();
			/**
			if(stuckMsg != null) {
				if(stuckMsg.equals("StuckBetween")) {
					makeStuck(0);
				}
			}**/
		}
	}
	
	public void notifyElev() {
		if(testing) return;
		if(stuck) {
			//if stuck do nothing
			return;
		} else if(curFloor != getFloor() && getFloor() != -1) {
			// continue going in current direction
			if(state == up) {
				curFloor++;
				timer.start();
			} else if(state == down) {
				curFloor--;
				timer.start();
			}
		} else {
			// arrived at floor 
			/**
			if(stuckMsg != null) {
				if(stuckMsg.equals("StuckClosed")) {
					makeStuck(2);
				}
			}**/
			openDoor();
			System.out.println("Elevator " + elevNum + " State Change: IN IDLE @ time = " + LocalTime.now() + "\n");  //Printing time stamps
			state = idle;    // set state to idle
			byte[] msg = Common.encodeElevator(elevNum, curFloor, state,curFloor);
			transmitter.sendPacket(msg);
			msg = transmitter.receivePacket();
			if(map.get(curFloor)) {
				removeFloor(curFloor);  //calls method remove floor to remove it from the arraylist
				/**
				int nextFloor = fileLoader.popDestination();
				buttons[nextfloor-1].register();
				addDest(nextFloor, false);
				**/
				
			} else {
				removeFloor(curFloor);  //calls method remove floor to remove it from the arraylist
				buttons[curFloor-1].reached();
				
			}
			/**
			if(stuckMsg != null) {
				if(stuckMsg.equals("StuckOpen")) {
					makeStuck(1);
				}
			}**/
			closeDoor();
		}
		
		
		
		
	}

	//Method removeFloor takes chosen floor and removes it from the Arraylist of destination floors
	public void removeFloor(int floor) {
		map.remove((Integer)floor);
	}
	
	private void makeStuck(int reason) {
		byte[] msg;
		switch(reason) {
		case 0:
			stuck = true;
			timer.stop();
			System.out.println("Elevator "+elevNum+" stuck between floors "+curFloor+" and "+(state==up?curFloor+1:curFloor-1)+ " @ time = " + LocalTime.now());
			while(getFloor() != -1) {
				msg = Common.encodeElevError(Common.ELEV_ERROR.STUCK, elevNum,curFloor,map.get(getFloor()) ? -1 : getFloor(), !buttons[getFloor()-1].isOn());
				transmitter.sendPacket(msg);
				transmitter.receivePacket();
				removeFloor(getFloor());
			}
			break;
		case 1:
			stuck = true;
			timer.stop();
			System.out.println("Elevator "+elevNum+" stuck with doors open at floor "+curFloor+" @ time = " + LocalTime.now());
			while(getFloor() != -1) {
				msg = Common.encodeElevError(Common.ELEV_ERROR.DOOR_OPEN, elevNum,curFloor,map.get(getFloor()) ? -1 : getFloor(), !buttons[getFloor()-1].isOn());
				transmitter.sendPacket(msg);
				transmitter.receivePacket();
				removeFloor(getFloor());
			}
			break;
		case 2:
			stuck = true;
			timer.stop();
			System.out.println("Elevator "+elevNum+" stuck with doors closed at floor "+curFloor+" @ time = " + LocalTime.now());
			while(getFloor() != -1) {
				msg = Common.encodeElevError(Common.ELEV_ERROR.DOOR_CLOSE, elevNum,curFloor,map.get(getFloor()) ? -1 : getFloor(), !buttons[getFloor()-1].isOn());
				transmitter.sendPacket(msg);
				transmitter.receivePacket();
				removeFloor(getFloor());
			}
			break;
		}
	}
	
	private void pollStop() {
		/**
		stuckMsg = fileLoader.getStop(elevNum);
		if(stuckMsg != null) {
			if(stuckMsg.equals("StuckBetween") && state != idle) {
				makeStuck(0);
			}
		}
		**/
	}

	public Integer getFloor() {
		int target = -1;
		try {
			if(state==up) {
				target = Collections.min(map.keySet());
			} else {
				target = Collections.max(map.keySet());
			}
			return target;
		} catch (NoSuchElementException e) {
			return -1;
		}
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
	
	//getter to return door status
	public boolean getDoorStatus() {
		return doorStatus;
	}
	
	// receive method that first sends a check request to elevatorSubsystem
	// and then receives instructions for a specific elevator
	public void receive() {
		pollStop();
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
			addDest(received[1],true);   //Common.java identifies msg[1] as destination floor

		}
		pollStop();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub


		while (true) {
			move();
			receive();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}

	}

}
