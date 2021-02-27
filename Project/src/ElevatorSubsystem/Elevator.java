package ElevatorSubsystem;


/**
 * @author Gill
 *
 */
public class Elevator {
	
	public int curFloor;
	private int[] destFloors;
	private boolean doorStatus;

	public Elevator(int curFloor, boolean doorStatus) {
		this.curFloor = 0;
		this.doorStatus = true;
	}
	
	public void selectFloor(int[] floors) {
		this.destFloors = floors;
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
