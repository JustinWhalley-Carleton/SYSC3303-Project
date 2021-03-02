package Timer;

public class TestTimer {

	static Object syncObj = new Object();
	
	Thread timer;
	public TestTimer() {
		timer = new Thread(new TimerThread(5000,this));
		timer.start();
		runLoop();
	}
	
	public void notifyTimer() {
		System.out.println("timer done");
	}
	
	public void startTimer() {
		timer.notify();
	}
	
	private void interruptTimer() {
		timer.interrupt();
	}
	
	private void runLoop() {
		System.out.println("Beggining loop");
		for(int i = 0; i < 5; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("beginning timer");
			interruptTimer();
			if(i%2 == 0) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
				interruptTimer();
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new TestTimer();
	}

}
