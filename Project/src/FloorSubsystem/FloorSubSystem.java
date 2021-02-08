package FloorSubsystem;
import Scheduler.Scheduler;
import java.time.LocalTime;

public class FloorSubSystem implements Runnable{
    // Constants
    private final int MIN_FLOOR;
    private final int MAX_FLOOR;
    private byte[] requestedDir;
    private Floor[] floors;

    private Scheduler scheduler;
    //    private SchedulerSubsystem scheduler;
    public FileLoader instructionFile;

    public FloorSubSystem(Scheduler scheduler, int maxFloor) throws Exception{
        // Error checking
        if (maxFloor <= 1) {
            throw new Exception("incompatible setting: maxFloor should be higher than 2.");
        }

        // Init floors
        this.MIN_FLOOR = 1;
        this.MAX_FLOOR = maxFloor;

        floors = new Floor[MAX_FLOOR];
        for (int f = 0; f < maxFloor; ++f) {
            floors[f] = new Floor(f, f == MIN_FLOOR, f == MAX_FLOOR);
        }

        // Save scheduler
        this.scheduler = scheduler;
        // Init instruction reader
        instructionFile = new FileLoader();
    }

    public void run() {
        boolean instructionSent = false;

        // init current time based on time set on first instruction
//        curTime = instructionFile.getTime();

        while (true) {
            // send instruction if needed
            if (instructionSent) {
                instructionSent = !instructionFile.hasNextInstruction();
                // read instruction
                nextInstruction();
            } else {
                // compare time stamp
                if (LocalTime.now().isAfter(instructionFile.getTime())) {
                    // send it now
                    send();
                    instructionSent = true;
                }
            }

            // receive instruction if needed
            receive();
        }
    }


    // Read next instruction from file
    public void nextInstruction() {
        try {
            instructionFile.nextLine();
        } catch (Exception e) {
            System.out.println("ERROR: Read instruction file failed.");
        }
    }

    public void selectFloor(byte[] in) {

    }

    // read command from a file
    public void readCommand(String str) {

    }

    public byte[] getInfo() {
        return null;
    }


    // send method: send data to scheduler.
    public void send() {
        scheduler.floorAddRequest(instructionFile.departFloor(),
                                    instructionFile.toString());
    }

    // receive method: save message from scheduler.
    public void receive() {
        // process message from scheduler
        for (int i = 0; i < MAX_FLOOR; ++i){
            String message = scheduler.floorCheckRequest(i);
            if(message != null){
                System.out.println("Floor received message: " + message);
            }
        }
    }

}
