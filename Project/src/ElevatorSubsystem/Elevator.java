package ElevatorSubsystem;

import java.time.LocalTime;
import java.util.ArrayList;

/**
 * @author Gill
 *
 */
public class Elevator {
	
	public int curFloor;
	private ArrayList<Integer> destFloors;
	private boolean doorStatus;
	private MotorState state;
	private Up up = new Up();
	private Down down = new Down();
	private Idle idle = new Idle();

	public Elevator(int curFloor, boolean doorStatus) {
		this.curFloor = curFloor;
		this.doorStatus = doorStatus;
		destFloors = new ArrayList<Integer>();
		state = idle;
	}
	
	public void selectFloor(int[] floors) {
		for(int i : floors) {
			destFloors.add((Integer)i);
		}
	}
	
	public void addDest(int floor) {
		destFloors.add((Integer)floor);
		System.out.println("\nGoing from floor " + curFloor + " to floor " + floor);
		if(curFloor > floor) {
			System.out.println("Elevator State Change: GOING DOWN @ time = " + LocalTime.now());
			state = up;
		} else if (floor > curFloor) {
			System.out.println("Elevator State Change: GOING UP @ time = " + LocalTime.now());
			state = down;
		} else {
			System.out.println("Same floor. No state change");
			removeFloor(floor);
			return;
		}
		try {
			Thread.sleep(1000 * (Math.abs(curFloor - floor)));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Elevator State Change: IN IDLE @ time = " + LocalTime.now() + "\n");
		state = idle;
		removeFloor(floor);
		curFloor = floor;
	}
	
	public void removeFloor(int floor) {
		destFloors.remove((Integer)floor);
	}
	
	public byte[] getInfo() {
		return null;
	}
	
	private void openDoor() {
		this.doorStatus = false;
	}
	
	private void closeDoor() {
		this.doorStatus = true;
	}
	
	public MotorState getCurrState() {
		return state;
	}
	
	public int getCurrFloor() {
		return curFloor;
	}
}
