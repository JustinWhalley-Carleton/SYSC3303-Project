package Timer;

import ElevatorSubsystem.Elevator;

public class TestTimer{

	private static final int TIME = 5000;
	public static TestTimer testTimer;
	private TimerController timer;
	public TestTimer() {
		timer = new TimerController(TIME,new Elevator(1,false)); //create a 5second timer
	}
	
	public void run() {
		timer.start();   								//start the timer 
		System.out.println(timer.isRunning()==true);    // confirm that the timer is running
		try {
			Thread.sleep(1000);							// sleep 1second so print outs easier to read
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timer.stop();									// stop timer before timer runs out
		try {
			Thread.sleep(200);							// wait 200ms to have the running variable update
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(timer.isRunning()==false);	// confirm the timer is not running
		timer.start();									// start the timer again
		try {
			Thread.sleep(200);							// wait 200ms to have the running variable update
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(timer.isRunning()==true);	// confirm the timer is running
		try {
			Thread.sleep(1000);							// wait 1second to make print outs easier
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		testTimer = new TestTimer();
		testTimer.run();
	}

}
