package FloorSubsystem;
import common.Common;
import common.RPC;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.LinkedList;
public class FloorSubSystem implements Runnable{
    // Constants
    private final int MIN_FLOOR;
    private final int MAX_FLOOR;
    private byte[] requestedDir;
    private Floor[] floors;

    public FileLoader instructionFile;
    
    public RPC rpc;

    private LinkedList<byte[]> messageQueue;
    public FloorSubSystem(int maxFloor) throws Exception{
        // Error checking
        if (maxFloor <= 1) {
            throw new Exception("incompatible setting: maxFloor should be at least 2.");
        }

        // Init floors
        this.MIN_FLOOR = 1;
        this.MAX_FLOOR = maxFloor;

        floors = new Floor[MAX_FLOOR];
        for (int f = 1; f <= maxFloor; ++f) {
            floors[f - 1] = new Floor(f, f == MIN_FLOOR, f == MAX_FLOOR);
        }

        // Init instruction reader
        instructionFile = new FileLoader();
        
        rpc = new RPC(InetAddress.getLocalHost(),10002,10000);
        rpc.setTimeout(2000);
        messageQueue = new LinkedList<byte[]>();
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

        // Error check
        if(MIN_FLOOR <= departureFloor && departureFloor <= MAX_FLOOR){
            // Register corresponding button
            floors[departureFloor - 1].register(instructionFile.requestUp());
        }else{
            // Unexpected floor in instruction, ignore.
            System.out.println("WARNING! Departure floor " + departureFloor + " out of range!");
            return;
        }

        // encode and send request to scheduler
        byte[] message = Common.encodeFloor(departureFloor, instructionFile.requestUp());

        addToQueue(message);
    }

    private void addToQueue(byte[] msg) {
    	messageQueue.add(msg);
    }
    
    private byte[] getMessageFromQueue() {
    	if(messageQueue.isEmpty()) {
    		return null;
    	}
    	return messageQueue.pop();
    }

    // receive method: save message from scheduler.
    public void receive() {
        // process message from scheduler
        byte[] message = rpc.receivePacket();

        // terminate if no message
        if (message == null){
            return;
        }
        byte[] msg;
        if(Common.findType(message) == Common.TYPE.CONFIRMATION){ 
        	msg = getMessageFromQueue();
        	if(msg == null) {
        		msg = Common.encodeConfirmation(Common.CONFIRMATION.NO_MSG);
        	}
        } else {
        

	        int[] decodeMsg = Common.decode(message);
	
	        int arrivalFloor = decodeMsg[1];
	        boolean dismissUp = decodeMsg[2] != 0;
	
	        System.out.println("FLOOR: " + arrivalFloor + " Going " + (dismissUp ? "Up" : "Down") + " @ time = " + LocalTime.now());
	        msg = Common.encodeConfirmation(Common.CONFIRMATION.RECEIVED);
	        if(MIN_FLOOR <= arrivalFloor && arrivalFloor <= MAX_FLOOR) {
	            // Elevator reached requested floor
	            floors[arrivalFloor - 1].reached(dismissUp);
	        }else{
	            // Unexpected floor received, ignore.
	            System.out.println("WARNING! Arrival floor " + arrivalFloor + " out of range!");
	            return;
	        }
        }
        rpc.sendPacket(msg);

    }

}
