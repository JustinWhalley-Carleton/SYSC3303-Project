package FloorSubsystem;
import GUI.CommandBridge;
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
    public Floor[] floors;

    public FileLoader instructionFile;
    
    public RPC rpc;
    private boolean GUIFlag;

    private LinkedList<byte[]> messageQueue;

    // Command Bridge
    CommandBridge commandBridge_floor;

    public FloorSubSystem(int maxFloor, boolean GUI) throws Exception{
    	GUIFlag = GUI;
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

        // Get port numbers
        Common.initializeVars();
        rpc = new RPC(InetAddress.getLocalHost(), Common.SCHEDULER_RECV_FLOOR_PORT, Common.FLOOR_SUB_RECV_PORT);
        rpc.setTimeout(2000);
        messageQueue = new LinkedList<byte[]>();

        // Command Bridge
        if (GUI){
            commandBridge_floor = new CommandBridge(CommandBridge.TYPE.FLOOR_BUTTON, false);
        }
    }

    public void run() {
        boolean instructionSent = false;
        boolean endMsgSent = false;

        // init current time based on time set on first instruction
//        curTime = instructionFile.getTime();

        while (true) {
        	if(GUIFlag) {
        		getInstruction();
        	}
            // send instruction if needed
            if (instructionSent && !GUIFlag) {
                instructionSent = !instructionFile.hasNextInstruction();
                // read instruction
                nextInstruction();
                if(instructionSent && !endMsgSent) {
                	//send end of file to scheduler
                    byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.END);
                    rpc.sendPacket(msg);
                    rpc.receivePacket();
                    // set flag, read file finish.
                    System.out.println("Instruction file: EOF.");
                    endMsgSent = true;
                }
            } else if(!GUIFlag){
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


    public void getInstruction() {
        // Retrive instruction from bridge
    	Integer[] buttonPress = commandBridge_floor.getFloorButton();
        // No instruction
    	if(buttonPress == null) {
    		return;
    	}
        // Decode Instruction
    	int departureFloor  = buttonPress[0];
    	boolean goingUp     = buttonPress[1] == 1;

    	System.out.println(departureFloor +" "+goingUp);
    	 if(MIN_FLOOR <= departureFloor && departureFloor <= MAX_FLOOR){
             // Register corresponding button
             floors[departureFloor - 1].register(goingUp);
             byte[] msg = Common.encodeFloor(departureFloor,goingUp);
         	addToQueue(msg);
         }else{
             // Unexpected floor in instruction, ignore.
             System.out.println("WARNING! Departure floor " + departureFloor + " out of range!");
             return;
         }
    	
    	
    }
    
    // send method: send data to scheduler.
    public void readInstruction() {
        // turn on up/ down button correspondingly
        int departureFloor = instructionFile.departFloor();

        // Error check
        if(MIN_FLOOR <= departureFloor && departureFloor <= MAX_FLOOR){
            // Register corresponding button
            floors[departureFloor - 1].register(instructionFile.requestUp());
         // encode and send request to scheduler
            byte[] message = Common.encodeFloor(departureFloor, instructionFile.requestUp());

            addToQueue(message);
        }else{
            // Unexpected floor in instruction, ignore.
            System.out.println("WARNING! Departure floor " + departureFloor + " out of range!");
            return;
        }

        
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
