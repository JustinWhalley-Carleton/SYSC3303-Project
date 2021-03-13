package Timer;

public class TimerThread implements Runnable{
	
	private TimerController classCalled;
	private int time;
	
	public TimerThread(int time, TimerController classCalled) {
		this.classCalled = classCalled;
		this.time = time;
	}
	
	/**
	 * begin the timer
	 */
	private void start() {

		try {
			Thread.sleep(time);

			classCalled.receiveTimerNotification(); // callback when timer is complete
		} catch (InterruptedException e) {// catch an interrupt
			// TODO Auto-generated catch block
			classCalled.receiveTimerNotification(); // call back because timer was stopped
		}
	}
	/**
	 * continuously wait until interrupt occurs
	 */
	public void run() {
		while(true) {
			try {
				Thread.sleep(999999999);
			} catch (InterruptedException e) { // catch an interrupt to start the timer
				start();
			}
			
		}
	}
}
