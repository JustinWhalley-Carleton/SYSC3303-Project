/**
 * 
 */
package Timer;

/**
 * @author jcwha
 *
 */
public class TimerController {

	boolean running;
	Thread timer;
	public TimerController(int time) {
		timer = new Thread(new TimerThread(time,this));
		timer.start();
		running = false;
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
		System.out.println("STOPPED");
	}
}
