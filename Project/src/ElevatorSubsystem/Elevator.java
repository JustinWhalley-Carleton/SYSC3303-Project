package ElevatorSubsystem; /**
 * 
 */

/**
 * @author Gill
 *
 */
public class Elevator implements Runnable{
	
	public int curFloor;
	private int floorSelection;
	private boolean doorStatus;

	public Elevator() {
		this.curFloor = 0;
		this.doorStatus = true;
	}
	
	public void selectFloor(int floor) {
		this.floorSelection = floor;
	}
	
	public byte[] getInfo() {
		return null;
	}
	
	public byte[] send() {
		return null;
	}
	
	public void recieve(byte[] info) {
		
	}
	
	private void openDoor() {
		this.doorStatus = false;
	}
	
	private void closeDoor() {
		this.doorStatus = true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
