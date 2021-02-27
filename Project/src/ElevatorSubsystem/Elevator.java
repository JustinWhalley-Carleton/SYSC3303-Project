package ElevatorSubsystem;

import java.util.ArrayList;

/**
 * @author Gill
 *
 */
public class Elevator {
	
	public int curFloor;
	private ArrayList<Integer> destFloors;
	private boolean doorStatus;

	public Elevator(int curFloor, boolean doorStatus) {
		this.curFloor = curFloor;
		this.doorStatus = doorStatus;
		destFloors = new ArrayList<Integer>();
	}
	
	public void selectFloor(int[] floors) {
		for(int i : floors) {
			destFloors.add((Integer)i);
		}
	}
	
	public void addDest(int floor) {
		destFloors.add((Integer)floor);
		if(curFloor > floor) {
			System.out.println("GOING DOWN");
		} else if (floor > curFloor) {
			System.out.println("GOING UP");
		} else {
			removeFloor(floor);
			return;
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("IN IDLE");
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
	
}
