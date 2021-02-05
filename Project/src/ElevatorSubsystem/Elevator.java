/**
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
	
	private Scheduler scheduler;

	public Elevator(Scheduler scheduler) {
		this.curFloor = 0;
		this.doorStatus = true;
		
		scheduler = new Scheduler();
	}
	
	public void selectFloor(int floor) {
		this.floorSelection = floor;
	}
	
	public byte[] getInfo() {
		return null;
	}
	
	public byte[] send() {
		scheduler.elevatorAddRequest(Integer(1), curFloor);
	}
	
	public void recieve(byte[] info) {
		scheduler.elevatorCheckRequest(Integer(1), curFloor);
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
