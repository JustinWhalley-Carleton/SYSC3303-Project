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
	public TimerController(int time,Elevator elev) {
		timer = new Thread(new TimerThread(time,this));
		timer.start();
		running = false;
		this.elev = elev;
	}
	
	public void start() {
		if(!running) {
			timer.interrupt();
			running = true;
		}
	}
	
	public void stop() {
		if(running) {
			timer.interrupt();
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	public void receiveTimerNotification() {
		running = false;
		//elev.notify();
		System.out.println("STOPPED");
	}
}
