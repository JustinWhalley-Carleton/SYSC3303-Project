/**
 * 
 */
package ElevatorSubsystem;
import Scheduler.Scheduler;
import common.Common;

/**
 * @author Gill
 *
 */
public class ElevatorSubsystem implements Runnable{

	private Scheduler scheduler;
	/**
	 * 
	 */
	public ElevatorSubsystem(Scheduler scehduler) {
		// TODO Auto-generated constructor stub
		this.scheduler = scheduler;
	}
	
	private void send(byte[] info) {
		//scheduler.elevatorAddRequest(1, "Going up");
		
		byte[] outgoingMsg = Common.encodeElevator(0, null, 0);
		scheduler.elevatorAddRequest(outgoingMsg);
	}
	
	private void receive(byte[] receivedMsg) {
		
		int[] decodedMsg = Common.decode(receivedMsg);
		
		int currFloor = decodedMsg[0];
		int direction = decodedMsg[1];
		int destFloor = decodedMsg[2];
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//receive();
			byte[] message = scheduler.elevatorCheckRequest();
			if(message != null){
				System.out.println("Elevator received message: " + message);
			}
	
		}
	}

}
