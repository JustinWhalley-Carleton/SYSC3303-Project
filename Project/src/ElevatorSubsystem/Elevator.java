package ElevatorSubsystem;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import FloorSubsystem.FileLoader;
import FloorSubsystem.GUIFileLoader;
import GUI.CommandBridge;
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
	
	public HashMap<Integer,Boolean> map;
	
	private boolean doorStatus;  //True means door is closed, false for open door
	private MotorState state;         
	private Up up = new Up();
	private Down down = new Down();    // The 3 states as per the motor interface
	private Idle idle = new Idle();
	private RPC transmitter;
	private TimerController timer, timer2;
	private boolean testing = false;
	private int NUM_FLOORS = Test.FLOORS;
	private ElevatorButton[] buttons;
	private boolean stuck=false;
	private FileLoader fileLoader;
	private String stuckMsg = null;
	private boolean goingUp;
	private FileLoader file;
	private boolean GUIFlag;
	public static final int floorTiming = (int) 2266/(int)Test.SPEED;
	private long moveStart;
	private boolean doorOpen = false;
	private long doorStart;
	private MotorState prevState;

	// Command bridge
	private CommandBridge commandBridge_fault;
	private CommandBridge commandBridge_button;

	/**
	 * no port elevator for junit testing
	 */
	public Elevator() {testing=true;}
	
	/**
	 * Constructor for regular elev
	 * @param elevNum
	 * @param curFloor
	 * @param doorStatus
	 * @param destPort
	 * @param recPort
	 * @param fileLoader
	 * @param GUI
	 * @param bridge_fault
	 * @param bridge_button
	 * @throws UnknownHostException
	 */
	public Elevator(int elevNum, int curFloor, boolean doorStatus,
					int destPort, int recPort, FileLoader fileLoader,
					boolean GUI, CommandBridge bridge_fault, CommandBridge bridge_button) throws UnknownHostException {

		// Initializing variables
		this.prevState = idle;
		this.curFloor = curFloor;
		this.GUIFlag = GUI;
		this.commandBridge_fault = bridge_fault;
		this.commandBridge_button = bridge_button;
		this.doorStatus = doorStatus;
		this.state = idle;   // setting motor state to idle
		this.transmitter = new RPC(InetAddress.getLocalHost(), destPort, recPort);
		this.elevNum = elevNum;
		this.buttons = new ElevatorButton[NUM_FLOORS];

		for(int i = 0; i < NUM_FLOORS; i++) {
			buttons[i] = new ElevatorButton(i+1,false);
		}

		timer = new TimerController((int)(floorTiming),this);
		timer2 = new TimerController(1500,this);
		map = new HashMap<Integer,Boolean>();
		this.fileLoader = fileLoader;

		try {
			file = new FileLoader();
			while(file.hasNextInstruction()) {
				file.nextLine();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * add a destination to the elevator
	 * @param floor
	 * @param type
	 */
	public void addDest(int floor,boolean type) {
		map.put((Integer)floor, (Boolean)type);
		move();
	}

	/**
	 * move the elevator
	 */
	public void move() {
		int floor = getFloor();
		if(floor!=-1 && state == idle && !stuck && !doorOpen) {
			System.out.println("\nElevator " + elevNum + " going from floor " + curFloor + " to floor " + floor);
			//If statements to checks the location of the destination floor relative to the current floor
			if(curFloor > floor) {
				System.out.println("Elevator " + elevNum + " State Change: GOING DOWN @ time = " + LocalTime.now());  //Going down if target floor is lower
				prevState = state;
				state = down;
			} else if (floor > curFloor) {
				System.out.println("Elevator " + elevNum + " State Change: GOING UP @ time = " + LocalTime.now());  //Going up if target floor is higher
				prevState = state;
				state = up;
			} else {
				System.out.println("Same floor. No state change\n");  //No movement if same floor

				if(stuckMsg != null && !GUIFlag) {
					if(stuckMsg.equals("StuckClose")) {
						makeStuck(2);
					}
				}
				doorStart = System.currentTimeMillis();
				openDoor();

				if(stuckMsg != null && !GUIFlag) {
					if(stuckMsg.equals("StuckOpen")) {
						makeStuck(1);
					}
				}
				timer2.start();
				doorOpen = true;
				byte[] msg = Common.encodeElevator(elevNum, curFloor, state, getFloor() == -1 ? curFloor : getFloor());
				transmitter.sendPacket(msg);
				transmitter.receivePacket();
				removeFloor(getFloor());
				/**
				closeDoor();
				long doorEnd = System.currentTimeMillis();
				String elapsedDoorTime = String.valueOf(doorEnd - doorStart);
				String doorTime = "Elevator Load/Unload Time in ms: " + elapsedDoorTime;
				FileLoader.logToFile(doorTime);**/
				return;
			}
			moveStart = System.currentTimeMillis();
			timer.start();

			if(stuckMsg != null && !GUIFlag) {
				if(stuckMsg.equals("StuckBetween")) {
					makeStuck(0);
				}
			}
		}
	}
	
	/**
	 * receive a callback from the timer
	 */
	public void notifyElev() {
		if(testing) return;
		if(stuck) {
			//if stuck do nothing
			return;
		} else if(doorOpen) { 
			doorOpen = false;
			closeDoor();
			if(map.get(curFloor) == null ? false : (boolean)map.get(curFloor)) {
				removeFloor(curFloor);

				pollCommand();

				return;
			}
			removeFloor(curFloor);
			buttons[curFloor-1].reached();
			long moveEnd = System.currentTimeMillis();
			String elapsedMoveTime = String.valueOf(moveEnd - moveStart);
			String moveTime = "Elevator " + elevNum + " took " + elapsedMoveTime + "ms to move to Floor " + curFloor;
			FileLoader.logToFile("********");
			FileLoader.logToFile(moveTime);
			FileLoader.logToFile("********");
		}else if((state == up && curFloor < getFloor())||(state == down && curFloor > getFloor())){
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
			if(stuckMsg != null && !GUIFlag) {
				if(stuckMsg.equals("StuckClose")) {
					makeStuck(2);
					return;
				}
			}
			openDoor();
			System.out.println("Elevator " + elevNum + " State Change: IN IDLE @ time = " + LocalTime.now() + "\n");  //Printing time stamps
			prevState = state;
			state = idle;    // set state to idle
			
			byte[] msg1 = Common.encodeElevator(elevNum, curFloor, state,curFloor);
			transmitter.sendPacket(msg1);
			if(map.get(curFloor) == null ? false : (boolean)map.get(curFloor)) {
				byte[] msg = Common.encodeElevator(elevNum, curFloor, state, getFloor() == -1 ? curFloor : getFloor());
				transmitter.sendPacket(msg);
				transmitter.receivePacket();
				removeFloor(curFloor);  //calls method remove floor to remove it from the arraylist
				Integer[] nextFloors;
				if(GUIFlag) {
					// Get next floor from bridge
					int nextFloor = commandBridge_button.getElevButton(elevNum);
					nextFloors = nextFloor == -1 ? null : new Integer[]{nextFloor};
				} else {
					nextFloors = file.popDestinations(curFloor, goingUp);
				}
				if(nextFloors != null) {
					for (Integer floorNum: nextFloors){
						buttons[floorNum-1].register();
						addDest(floorNum, false);
					}
				}
				
			} else {
				byte[] msg = Common.encodeElevator(elevNum, curFloor, state, getFloor() == -1 ? curFloor : getFloor());
				transmitter.sendPacket(msg);
				transmitter.receivePacket();
				removeFloor(curFloor);  //calls method remove floor to remove it from the arraylist
				buttons[curFloor-1].reached();
				
			}

			if(stuckMsg != null && !GUIFlag) {
				if(stuckMsg.equals("StuckOpen")) {
					makeStuck(1);
				}
			}
			timer2.start();
			doorOpen= true;
			return;
		}
		byte[] msg = Common.encodeElevator(elevNum, curFloor, state, getFloor() == -1 ? curFloor : getFloor());
		transmitter.sendPacket(msg);
		transmitter.receivePacket();
	}

	/**
	 * remove the floor from the map
	 * @param floor
	 */
	public void removeFloor(int floor) {
		map.remove((Integer)floor);
	}
	
	/**
	 * make the elevator stuck (used for test file mode)
	 * @param reason
	 */
	private void makeStuck(int reason) {
		System.out.println("*******\n\n");
		byte[] msg;
		stuck = true;
		Common.ELEV_ERROR errorType = ELEV_ERROR.STUCK;

		switch(reason) {
		case 0:
			System.out.println("Elevator "+elevNum+" stuck between floors "+
					curFloor+" and "+(state==up?curFloor+1:curFloor-1)+
					" @ time = " + LocalTime.now());
			errorType = ELEV_ERROR.STUCK;
			Thread.currentThread().interrupt();
			break;
		case 1:
			System.out.println("Elevator "+elevNum+" stuck door open on floor "+curFloor+" @ time = " + LocalTime.now());
			errorType = ELEV_ERROR.DOOR_OPEN;
			break;
		case 2:
			System.out.println("Elevator "+elevNum+" stuck door close on floor "+curFloor+" @ time = " + LocalTime.now());
			errorType = ELEV_ERROR.DOOR_CLOSE;
			break;
		}

		timer.stop();
		if (getFloor() == -1){
			System.out.println("No floors to remove");
			msg = Common.encodeElevError(errorType, elevNum,curFloor, -1, goingUp);
			transmitter.sendPacket(msg);
			removeFloor(getFloor());
		}
		while(getFloor() != -1) {
			System.out.println("Removing floors..");
			msg = Common.encodeElevError(errorType, elevNum, curFloor, ((boolean)map.get(getFloor())) ? getFloor(): -1, goingUp);
			transmitter.sendPacket(msg);
			removeFloor(getFloor());
		}

		System.out.println("\n\n*******");
		if(reason == 1 || reason == 2) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stuck = false;
			stuckMsg = null;
			System.out.println("\n\nRECOVERED\n\n");
			msg = Common.encodeElevError(ELEV_ERROR.RECOVER, elevNum, curFloor, -1, goingUp);
			transmitter.sendPacket(msg);
		}
		
	}
	
	/**
	 * check if theres a fault
	 * @throws FileNotFoundException
	 */
	private void pollStop() throws FileNotFoundException {
		if(GUIFlag) {
			if(commandBridge_fault.getFault(elevNum)) {
				int reason = 2;
				if(state == up || state == down) {
					reason = 0;
				}
				makeStuck(reason);
			}
		}
		
		stuckMsg = fileLoader.errorFileReader(elevNum);
		if(stuckMsg != null && !GUIFlag) {
			if(stuckMsg.equals("StuckBetween") && state != idle) {
				makeStuck(0);
			}
		}
		

	}

	/**
	 * check if theres a command from the GUI
	 */
	public void pollCommand() {
		if(GUIFlag) {
			// Get next floor from bridge
			int nextFloor = commandBridge_button.getElevButton(elevNum);
			if (nextFloor != -1){
				// Add floor if floor number valid
				buttons[nextFloor-1].register();
				addDest(nextFloor, false);
			}
		} else {
			Integer[] command =  file.popDestinations(curFloor, goingUp);
			if(command != null) {
				for (Integer floorNum: command) {
					buttons[floorNum-1].register();
					addDest(floorNum, false);
				}
			}
		}
	}
	
	/**
	 * get the floor to currently go to depending on the state
	 * @return Integer
	 */
	public Integer getFloor() {
		int target = -1;
		try {
			if(state==up || prevState == up) {
				Integer min = Integer.MAX_VALUE;
				for(Integer key : map.keySet()) {
					if(key >= curFloor && key < min) {
						min = key;
						target = key;
					}
				}
				if(target == -1) {
					min = -1;
					for(Integer key : map.keySet()) {
						if(key <= curFloor && key > min) {
							min = key;
							target = key;
						}
					}
				}
			} else if(state == down || prevState == down) {
				Integer min = -1;
				for(Integer key : map.keySet()) {
					if(key <= curFloor && key > min) {
						min = key;
						target = key;
					}
				}
				if(target == -1) {
					min = Integer.MAX_VALUE;
					for(Integer key : map.keySet()) {
						if(key >= curFloor && key < min) {
							min = key;
							target = key;
						}
					}
				}
			} else {
				target = Collections.min(map.keySet());
			}
			if(target == -1) {
				return Collections.min(map.keySet());
			}
			return target;
		} catch (NoSuchElementException e) {
			return -1;
		}
	}
	
	/**
	 * open the door
	 */
	private void openDoor() {
		this.doorStatus = false;
	}

	/**
	 * close the door
	 */
	private void closeDoor() {
		this.doorStatus = true;
	}

	/**
	 * get teh current state
	 * @return MotorState
	 */
	public MotorState getCurrState() {
		return state;
	}

	/**
	 * get the current floor
	 * @return int
	 */
	public int getCurrFloor() {
		return curFloor;
	}
	
	/**
	 * get the door status
	 * @return boolean false = door open
	 */
	public boolean getDoorStatus() {
		return doorStatus;
	}
	
//	public static void logFileWriter (String function, String duration) {
//		Logger logger = Logger.getLogger("Elevator Log");  
//		FileHandler fh;
//
//	    try {  
//
//	        fh = new FileHandler("src/test/logFile.txt");
//	        logger.addHandler(fh);
//	        SimpleFormatter formatter = new SimpleFormatter();  
//	        fh.setFormatter(formatter);  
//
//	        logger.info(function + ": " + duration);  
//
//	    } catch (IOException e) {  
//	        e.printStackTrace();  
//	    }
//		
//	}
	
	/**
	 * receive command from scheduler
	 * @throws FileNotFoundException
	 */
	public void receive() throws FileNotFoundException {
		pollStop();
		pollCommand();
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
			goingUp = received[2] == 1 ? true : false;
		}
		pollStop();
		pollCommand();
	}

	/**
	 * run loop 
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub


		while (!stuck) {
			try {
				move();
	
				try {
					receive();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

			
				Thread.sleep(200);
			} catch (InterruptedException e) {
				return;
			}
			
		}

	}

}
