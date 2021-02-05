package FloorSubsystem;

public class FloorButton {
    // Indicate if a button lights up
    private boolean light = false;

    // Setter for turning on
    public void turnOn(){
        light = true;
    }

    // Setter for turning off
    public void turnOff(){
        light = false;
    }
}
