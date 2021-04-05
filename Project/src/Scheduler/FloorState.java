package Scheduler;


/**
 * @author Yisheng Li
 *
 */

public class FloorState {

    private int floor;
    private int up;  // the elevt# that is assigned for it; 0 means the button is off
    private int down; // the elevt# that is assigned for it; 0 means the button is off


    /**
     * Constructor
     * initial state: both light off
     *
     * @param floor
     */
    public FloorState(int floor) {
        this.floor = floor;
        this.up = 0;
        this.down = 0;
    }

    //getter:

    /**
     * get the  state of up button
     *
     * @return the  state of up button
     */
    public int getUp() {
        return up;
    }

    /**
     * get state of down button
     *
     * @return state of down button
     */
    public int getDown() {
        return down;
    }


    //setter:

    /**
     * set state of up button
     *
     * @param up the state of up button
     */
    public void setUp(int up) {
        this.up = up;
    }


    /**
     * set state of down button
     *
     * @param down state of down button
     */
    public void setDown(int down) { this.down = down; }

}

