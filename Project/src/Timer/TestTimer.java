package Timer;

public class TestTimer implements Runnable{

	private static final int TIME = 5000;
	public static TestTimer testTimer;
	Thread timer;
	public TestTimer() {
		timer = new Thread(new TimerThread(TIME,this));
		timer.start();
	}
	
	public void notifyTimer() {
		System.out.println("timer done");
	}
	
	public void startTimer() {
		this.notifyAll();
	}
	
	private void interruptTimer() {
		timer.interrupt();
	}
	
	public void run() {
		synchronized(this) {
			this.notifyAll();
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
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		testTimer = new TestTimer();
		new Thread(testTimer).start();
	}

}
