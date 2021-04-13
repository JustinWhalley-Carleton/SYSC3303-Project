package ElevatorSubsystem;

/**
 * @author Gill
 *
 */
public class ElevatorButton {
	
	//Constants
	private final int floorNum;
	private boolean light;
	
	public ElevatorButton(int floorNum, boolean light) {

		this.floorNum = floorNum;
		this.light = light;
	}
	
	public void register() {
		turnOn();
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
	
	public boolean isOn() {
		return light;
	}

}
