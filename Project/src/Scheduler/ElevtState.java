package Scheduler;

/**
 * @author Yisheng Li
 *
 */

public class ElevtState {
	private int number; 
	private int floor;
	private int dir; // 1(Up) or -1(Down) or 0(Idle)
	private int dest;
	
	
	/**
	 * Constructor
	 * initial state: at floor 1, idle, destination of floor 1
	 * 
	 * @param number elevator number
	 */
	public ElevtState (int number) {
		this.number = number;
		this.floor = 1;
		this.dir = 0;
		this.dest = 1;

	}
	
	//getter:
	/**
	 * get elevator number
	 * 
	 * @return elevator number
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * get the floor the elevator currently at
	 * 
	 * @return the floor the elevator currently at
	 */
	public int getFloor() {
		return floor;
	}
	
	
	/**
	 * get the direction the elevator is moving
	 * 
	 * @return the direction the elevator is moving
	 */
	public int getDir() {
		return this.dir;
	}


	/**
	 * get the destination floor of the elevator
	 *
	 * @return the destination floor of the elevator
	 */
	public int getDest() { return this.dest; }
	
	
	
	
	//setter:
	/**
	 * set the floor the elevator currently at
	 *
	 * @param floor the floor the elevator currently at
	 */
	public void setFloor(int floor) {
		this.floor = floor;
	}
	
	
	/**
	 * set the direction the elevator is moving
	 * 
	 * @param dir  the direction the elevator is moving
	 */
	public void setDir(int dir) { this.dir = dir; }


	/**
	 * set the destination floor of the elevator
	 *
	 * @param dest the destination floor of the elevator
	 */
	public void setDest(int dest) { this.dest = dest; }




}
