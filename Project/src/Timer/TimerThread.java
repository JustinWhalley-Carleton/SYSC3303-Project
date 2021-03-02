package Timer;

public class TimerThread implements Runnable{
	
	private TestTimer classCalled;
	private int time;
	
	public TimerThread(int time, TestTimer classCalled) {
		this.classCalled = classCalled;
		this.time = time;
	}
	
	private void start() {
		System.out.println("Starting");
		try {
			Thread.sleep(time);
			System.out.println("Normal completion");
			classCalled.notifyTimer();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("interrupted");
			classCalled.notifyTimer();
		}
	}
	
	public void run() {
		while(true) {
			try {
				Thread.sleep(999999999);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				start();
			}
		}
	}
}
