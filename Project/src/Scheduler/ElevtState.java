package Scheduler;

/**
 * @author Yisheng Li
 *
 */

public class ElevtState {
	private int number; 
	private int floor;
	private Direction dir; 
	
	
	/**
	 * Constructor
	 * initial state: at floor 1, idle 
	 * 
	 * @param number elevator number
	 */
	public ElevtState (int number) {
		this.number = number;
		floor = 1;
		dir = Direction.IDLE;
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
	public Direction getDir() {
		return this.dir;
	}
	
	
	
	
	//setter:
	/**
	 * set the floor the elevator currently at
	 * 
	 * @param the floor the elevator currently at
	 */
	public void setFloor(int floor) {
		this.floor = floor;
	}
	
	
	/**
	 * set the direction the elevator is moving
	 * 
	 * @param get the direction the elevator is moving
	 */
	public void setDir(Direction dir) {
		this.dir = dir;
		
	}
	
	

}
