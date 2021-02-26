package FloorSubsystem;

public class Floor {
    // Constants
    private final int FLOOR_NUM;
    private final boolean IS_MIN_FLOOR;
    private final boolean IS_MAX_FLOOR;

    // Each floor have 1 or 2 buttons
    private final FloorButton BUTTON_UP;
    private final FloorButton BUTTON_DN;

    public Floor(int FLOOR_NUM, boolean isBaseFloor, boolean isMaxFloor) throws Exception {
        // Error checking for base/ max floor
        if(isBaseFloor && isMaxFloor){
            throw new Exception("Incompatible setting: Both BaseFloor and MaxFloor set!");
        }

        this.FLOOR_NUM = FLOOR_NUM;
        this.IS_MIN_FLOOR = isBaseFloor;
        this.IS_MAX_FLOOR = isMaxFloor;

        // Init floor buttons
        this.BUTTON_UP   = isMaxFloor  ? null : new FloorButton();
        this.BUTTON_DN = isBaseFloor ? null : new FloorButton();

    }

    // Push button
    public void register(boolean up){
        if (up){
            BUTTON_UP.turnOn();
        }
        else{
            BUTTON_DN.turnOn();
        }
    }

    // An elevator reached the floor: turn off button light.
    public void reached(boolean up){
        if (up){
            BUTTON_UP.turnOff();
        }
        else{
            BUTTON_DN.turnOff();
        }
    }

}
