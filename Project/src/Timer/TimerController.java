/**
 * 
 */
package Timer;

import ElevatorSubsystem.Elevator;

/** 
 * @author jcwha
 *
 */
public class TimerController {

	boolean running;
	Thread timer;
	Elevator elev;
	public TimerController(int time, Elevator elev) {
		timer = new Thread(new TimerThread(time,this));
		timer.start();
		running = false;
		this.elev = elev;
	}
	
	/**
	 * start the timer thread
	 */
	public void start() {
		// if it is already running do nothing
		if(!running) {
			timer.interrupt(); // throw interrupt exception to begin timer
			running = true; 
		}
	}
	
	/**
	 * stop the timer before the timer ends normally
	 */
	public void stop() {
		// if it is not running, do nothing
		if(running) {
			timer.interrupt(); // throw an interrupted excpetion to stop timer
		}
	}
	
	/**
	 * getter for running
	 * @return true if timer is currently running false otherwise
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * callback from timer thread when timer is comlpeted
	 */
	public void receiveTimerNotification() {
		running = false;
		elev.notifyElev(); // notify elevator that the timer is completed
	}
}
