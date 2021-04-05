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
        this.BUTTON_UP = isMaxFloor  ? null : new FloorButton();
        this.BUTTON_DN = isBaseFloor ? null : new FloorButton();

    }

    // Push button: turn on button light
    public void register(boolean up){
        setButton(true, up);
    }

    // An elevator reached the floor: turn off button light.
    public void reached(boolean up){
        setButton(false, up);
    }

    // Turn on/ off button light for current floor
    private void setButton(boolean on, boolean up){
        FloorButton targetButton;
        // Check error, select button
        if (up){
            if (IS_MAX_FLOOR){
                System.out.println("Warning! Max floor doesn't have up button!");
                return;
            }
            targetButton = BUTTON_UP;
        }
        else{
            if (IS_MIN_FLOOR){
                System.out.println("Warning! Min floor doesn't have down button!");
                return;
            }
            targetButton = BUTTON_DN;
        }
        // update button
        if (on){ targetButton.turnOn(); }
        else { targetButton.turnOff(); }
    }
    
    public boolean buttonUpOn() {
    	return BUTTON_UP.isOn();
    }
    
    public boolean buttonDownOn() {
    	return BUTTON_DN.isOn();
    }

}
