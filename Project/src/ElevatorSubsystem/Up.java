/**
 * 
 */
package ElevatorSubsystem;

/**
 * Class to specify the elevator is going in the up direction.
 * 
 * @author Justin Whalley
 *
 */
public class Up implements MotorState{
	private boolean doorOpen;
	double accelerateTime = 2.27;
	double decelerateTime = 2.27;
	double timePerFloor = 1.736;
	private boolean moving = false;
	private int state = 0;
	
	/**
	 * Constructor to set the door to closed 
	 */
	public Up() {
		doorOpen=false;
	}
	
	/**
	 * decelerate the elevator to a stop.
	 */
	public void stop() {
		moving = false;
		int i = 0;
		//set state to decelerating
		state = 3;
		// slow down for decelerateTime seconds
		while(i < (decelerateTime/0.1)) {
			try {
				this.wait(100);
				i++;
			} catch (InterruptedException e) {
				continue;
			}
		}
		//set state to stopped
		state = 0;
	}
	
	public void move(int floors) {
		//only accelerate move when stopped
		if(state == 0) {
			moving = true;
			int i = 0;
			//set state to accelerating
			state = 1;
			while(moving && i < (accelerateTime/0.1)) {
				try {
					this.wait(100);
					i++;
				} catch (InterruptedException e) {
					continue;
				}
			}
			i = 0;
			state = 2;
			//move until deceleration is needed and account for acceleration and deceleration
			while(moving && i < (timePerFloor*(floors-4/3)/0.1)) {
				try {
					this.wait(100);
					i++;
				} catch (InterruptedException e) {
					continue;
				}
			}
			stop();
		}
	}
	
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
	 * getter for the state
	 * 
	 * @return 0 for stopped, 1 for accelerating, 2 for max speed, 3 for decelerating
	 */
	public int getState() {
		return state;
	}
	
	/**
	 * getter for door state
	 * 
	 * @return true if door open, false otherwise
	 */
	public boolean getDoorState() {
		return doorOpen;
	}
}
