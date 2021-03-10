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
	
	public static void main(String[] args) {
		TimerController t = new TimerController(5000);
		t.start();
		System.out.println("RUNNING");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.stop();
	}
}
