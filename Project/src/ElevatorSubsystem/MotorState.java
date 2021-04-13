package ElevatorSubsystem;

/**
 * @author jcwha
 *
 */
public interface MotorState {
	public void move(int floors);
	public void openDoor();
	public void closeDoor();
	public boolean getDoorState();
	public void stop();
}
