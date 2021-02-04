package FloorSubsystem;

public class FloorSubSystem implements Runnable{
    // Constants
    private final int MIN_FLOOR;
    private final int MAX_FLOOR;
    private byte[] requestedDir;
    private Floor[] floors;

    public FileLoader instructionFile;

    public FloorSubSystem(int maxFloor) throws Exception{
        // Error checking
        if (maxFloor <= 1){
            throw new Exception("incompatible setting: maxFloor should be higher than 2.");
        }

        // Init floors
        this.MIN_FLOOR = 1;
        this.MAX_FLOOR = maxFloor;
        floors = new Floor[MAX_FLOOR];
        for (int f = 1; f <= maxFloor; ++ maxFloor){
            floors[f] = new Floor(f, f == MIN_FLOOR, f == MAX_FLOOR);
        }

        // Init instruction reader
        instructionFile = new FileLoader();
    }

    public void run(){

    }

    public void selectFloor(byte[] in){

    }

    // read command from a file
    public void readCommand(String str){

    }

    public byte[] getInfo(){
        return null;
    }


    // send method called by scheduler: return byte[] to scheduler.
    public byte[] send(){
        return null;
    }

    // receive method called by scheduler: save message from scheduler to var.
    public void receive(byte[] message){
        // process message from scheduler
    }

}
