package Timer;

public class TimerThread implements Runnable{
	
	private TimerController classCalled;
	private int time;
	
	public TimerThread(int time, TimerController classCalled) {
		this.classCalled = classCalled;
		this.time = time;
	}
	
	private void start() {
		System.out.println("Starting");
		try {
			Thread.sleep(time);
			System.out.println("Normal completion");
			classCalled.interrupt();
			classCalled.receiveTimerNotification();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("interrupted");
			classCalled.receiveTimerNotification();
		}
	}
	
	public void run() {
		while(true) {
			synchronized(classCalled) {
				System.out.println("IN LOOP");
				try {
					classCalled.wait();
					//Thread.sleep(999999999);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//start();
				}
				System.out.println("STARTING");
				start();
			}
		}
	}
}
