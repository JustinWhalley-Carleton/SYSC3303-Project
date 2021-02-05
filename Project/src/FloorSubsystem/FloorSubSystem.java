package FloorSubsystem;

import Scheduler.Scheduler;
public class FloorSubSystem implements Runnable{
    // Constants
    private final int MIN_FLOOR;
    private final int MAX_FLOOR;
    private byte[] requestedDir;
    private Floor[] floors;

    //    private SchedulerSubsystem scheduler;
    public FileLoader instructionFile;
    // current Time of the system
    public LocalTime curTime;

    public FloorSubSystem(Scheduler scheduler,int maxFloor) throws Exception{
        // Error checking
        if (maxFloor <= 1) {
            throw new Exception("incompatible setting: maxFloor should be higher than 2.");
        }

        // Init floors
        this.MIN_FLOOR = 1;
        this.MAX_FLOOR = maxFloor;
        floors = new Floor[MAX_FLOOR];
        for (int f = 1; f <= maxFloor; ++maxFloor) {
            floors[f] = new Floor(f, f == MIN_FLOOR, f == MAX_FLOOR);
        }

        // Init instruction reader
        instructionFile = new FileLoader();
    }

    public void run() {
        nextInstruction();
        boolean instructionSent = false;

        // init current time based on time set on first instruction
        curTime = instructionFile.getTime();

        while (true) {
            // send instruction if needed
            if (instructionSent) {
                // read instruction
                nextInstruction();
            } else {
                // compare time stamp
                if (curTime.isAfter(instructionFile.getTime())) {
                    // send it now
                    send(instructionFile.toString());
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
    public void send(String data) {

//        scheduler.AddRequest(Integer(1), data);
    }

    // receive method: save message from scheduler.
    public byte[] receive() {
        // process message from scheduler

//        System.out.println(scheduler.checkRequest(Integer(1));
        return null;
    }

}
