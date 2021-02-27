package ElevatorSubsystem;

/**
 * 
 */

/**
 * @author Gill
 *
 */
public class ElevatorButton {
	
	//Constants
	private int floorNum;
	private boolean light;
	
	/**
	 *
	 */
	public ElevatorButton() {
		
		this.floorNum = 0;
		this.light = false;
		
	}
	
	public ElevatorButton(int floorNum, boolean light) {
		
		this.floorNum = floorNum;
		this.light = light;
	}
	
	public void register() {
		
	}
	
	public void reached() {
		turnOff();
	}
	
	private void turnOn() {
		light = true;
	}
	
	private void turnOff() {
		light = false;
	}

}
