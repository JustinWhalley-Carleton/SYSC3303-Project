package Scheduler;


/**
 * @author Yisheng Li
 *
 */

public class FloorState {

    private int floor;
    private int up;
    private int down;


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
     * get the light state of up button
     *
     * @return the light state of up button
     */
    public int getUp() {
        return up;
    }

    /**
     * get the light state of down button
     *
     * @return the light state of down button
     */
    public int getDown() {
        return down;
    }


    //setter:

    /**
     * set the light state of up button
     *
     * @param up the light state of up button
     */
    public void setUp(int up) {
        this.up = up;
    }


    /**
     * set the light state of down button
     *
     * @param down the light state of down button
     */
    public void setDown(int down) { this.down = down; }

}

