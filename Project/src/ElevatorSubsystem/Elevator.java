package ElevatorSubsystem;
import Scheduler.Scheduler;

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
		
		this.scheduler = scheduler;
	}
	
	public void selectFloor(int floor) {
		this.floorSelection = floor;
	}
	
	public byte[] getInfo() {
		return null;
	}
	
	public void send() {
		scheduler.elevatorAddRequest(1, "Going up");
	}
	
	public void recieve(byte[] info) {
		scheduler.elevatorCheckRequest(floorSelection);
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
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			send();

			for (int i = 0; i < 10; ++i){
				String message = scheduler.elevatorCheckRequest(i);
				if(message != null){
					System.out.println("Elevator received message: " + message);
				}
			}
		}
	}
	
}
