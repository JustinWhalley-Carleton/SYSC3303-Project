/**
 * 
 */
package ElevatorSubsystem;

/**
 * Class to signify the elevator is in idle
 * 
 * @author Justin Whalley
 *
 */
public class Idle implements MotorState{
	private boolean doorOpen;
	
	/**
	 * constructor to set door to closed
	 */
	public Idle() {
		doorOpen = false;
	}
	
	/**
	 * empty method to fill MotorState interface
	 * 
	 * @param floors
	 */
	public void move(int floors) {}
	
	/**
	 * open the door
	 */
	public void openDoor() {
		doorOpen = true;
	}
	
	/**
	 * close the door
	 */
	public void closeDoor() {
		doorOpen = false;
	}
	
	/**
	 * get the door state
	 * 
	 * @return true is open, false otherwise
	 */
	public boolean getDoorState() {
		return doorOpen;
	}
	
	/**
	 * empty method to fill MotorState interface
	 */
	public void stop() {}
}
