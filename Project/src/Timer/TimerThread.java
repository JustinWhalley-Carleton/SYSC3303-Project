package Timer;

public class TimerThread implements Runnable{
	
	private TimerController classCalled;
	private int time;
	
	public TimerThread(int time, TimerController classCalled) {
		this.classCalled = classCalled;
		this.time = time;
	}
	
	private void start() {

		try {
			Thread.sleep(time);

			classCalled.receiveTimerNotification();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			classCalled.receiveTimerNotification();
		}
	}
	
	public void run() {
		while(true) {
			try {
				Thread.sleep(999999999);
			} catch (InterruptedException e) {
				start();
			}
			
		}
	}
}
