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
	private Elevator elevator;
	/**
	 * 
	 */
	public ElevatorSubsystem(Scheduler scheduler) {
		// TODO Auto-generated constructor stub
		this.scheduler = scheduler;
		this.elevator = new Elevator(1, true);
	}
	
	private void send(byte[] info) {
		//scheduler.elevatorAddRequest(1, "Going up");
		
		//byte[] outgoingMsg = Common.encodeElevator(0, 0, null, 0);
		scheduler.elevtSubAddMsg(info);
	}
	
	//Method receive that takes in a byte[] and then decodes it using Common.java
	private void receive(byte[] receivedMsg) {
		
		int[] decodedMsg = Common.decode(receivedMsg);  //calls Common's decode method to receive and store int[] version of the msg
		
		int elevatorNum = decodedMsg[0];  //Set each index of the msg to its own variable, elevatorNum and floor
		int floor = decodedMsg[1];
		
		elevator.addDest(floor); //Add the received floor to the destination arraylist in elevator.java
		
		send(Common.encodeElevator(1, floor, new Idle(), floor));   //calls the send method to report back to the scheduler
																	//encodes the msg into byte[] format using encode method in Common.java before sending
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			byte[] message = scheduler.elevtSubCheckMsg();
			if(message != null){
				System.out.println("Elevator received message: " + message);
				receive(message);
			}
	
		}
	}

}
