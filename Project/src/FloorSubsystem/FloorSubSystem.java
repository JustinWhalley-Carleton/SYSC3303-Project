package FloorSubsystem;
import Scheduler.Scheduler;
import common.Common;

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
                    // read instruction now
                    readInstruction();
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
    public void readInstruction() {
        // turn on up/ down button correspondingly
        int departureFloor = instructionFile.departFloor();

        floors[departureFloor - 1].register(instructionFile.requestUp());

        // send request to scheduler
        byte[] message = Common.encodeFloor(departureFloor, instructionFile.requestUp());

//        scheduler.floorAddRequest(message);

        scheduler.floorAddRequest(instructionFile.departFloor(),
                                    instructionFile.toString());
    }


    // receive method: save message from scheduler.
    public void receive() {
        // process message from scheduler
        byte[] message = scheduler.floorCheckRequest();

        int[] decodeMsg = Common.decode(message);

        int floor = decodeMsg[1];
        boolean dismissUp = decodeMsg[2] != 0;

        // turn off up/ down light
        floors[floor - 1].reached(dismissUp);

    }

}
