package GUI;

import common.RPC;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Scanner;

public class CommandBridge{

    /* Message types */
    public enum TYPE{
        INVALID         ((byte) -1),
        FAULT           ((byte) 0),
        FLOOR_BUTTON    ((byte) 1),
        ELEV_BUTTON     ((byte) 2);

        private final byte value;
        private TYPE(byte b){ this.value = b; }
        // Payload
        private int payload1, payload2;

        // Determine which type the byte corresponds to
        public static TYPE findType(byte b){
            for (TYPE type: TYPE.values()){
                if(type.value == b) return type;
            }
            return INVALID;
        }

        // Initialize payload
        public void setPayload(int p1, int p2){
            payload1 = p1;
            payload2 = p2;
        }

        // Generate a byte[] with current payload
        public byte[] encode(){
            byte[] result = new byte[3];
            result[0] = value;
            result[1] = (byte) payload1;
            result[2] = (byte) payload2;
            return result;
        }

        // Convert a byte[] back to TYPE
        public static TYPE decode(byte[] msg){
            TYPE command = findType(msg[0]);
            command.payload1 = msg[1];
            command.payload2 = msg[2];
            return command;
        }
    }


    // Class vars
    private final TYPE msgType;
    private final Boolean isSender;
    private int senderPort, receiverPort;
    private RPC transmitter;

    // Receiver buffer
    private LinkedList<TYPE> receiverBuffer;

    public CommandBridge(TYPE type, Boolean sender){
        // Initial identity
        msgType = type;
        isSender = sender;

        readSettings();

        // Initialize transmitter
        try {
            transmitter = new RPC(InetAddress.getLocalHost(),
                    sender ? receiverPort : senderPort,
                    sender ? senderPort : receiverPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Initialize Receiver
        if (!sender){
            // Initialize receiver buffer
            receiverBuffer = new LinkedList<TYPE>();
            // Start receiver thread in background.
            Thread receiverThread = new Thread(this::backgroundReceiver);
            receiverThread.start();
        }
    }

    private void readSettings(){
        try {
            Scanner scanner = new Scanner(new File("src/test/settings.txt"));
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitStr = line.trim().split("\\s+");

                if (splitStr.length == 1 || line.contains("/")){
                    // Empty line or Comment in setting file.
                    continue;
                }

                // Get value
                int value = splitStr[1].contains(".") ? 0 : Integer.parseInt(splitStr[1]);
                // Assign value to its according variable
                switch(splitStr[0].trim()){
                    case "BRIDGE_FAULT_SEND:" -> { if (msgType == TYPE.FAULT) senderPort   = value; }
                    case "BRIDGE_FAULT_RECV:" -> { if (msgType == TYPE.FAULT) receiverPort = value; }
                    case "BRIDGE_FLOOR_SEND:" -> { if (msgType == TYPE.FLOOR_BUTTON) senderPort   = value; }
                    case "BRIDGE_FLOOR_RECV:" -> { if (msgType == TYPE.FLOOR_BUTTON) receiverPort = value; }
                    case "BRIDGE_ELEV_SEND:"  -> { if (msgType == TYPE.ELEV_BUTTON) senderPort   = value; }
                    case "BRIDGE_ELEV_RECV:"  -> { if (msgType == TYPE.ELEV_BUTTON) receiverPort = value; }
                    // Unsupported settings
                    default -> {}
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    // send function call by sender, to send payload.
    public void send(int payload1, int payload2){
        if (!isSender) {
            System.out.println("Unexpected behaviour! CommandBridge::send() called by receiver. Abort.");
            return;
        }

        // Encode
        msgType.setPayload(payload1, payload2);
        byte[] msg = msgType.encode();

        // Send
        transmitter.sendPacket(msg);
    }


    /* Background Thread for receiver */
    public void backgroundReceiver() {

        while(true){
            // Receive msg
            byte[] msg = transmitter.receivePacket();

            // Decode
            TYPE received = TYPE.decode(msg);

            // Error check
            if (received != msgType){
                System.out.println("Unexpected " + received + "command received! Expecting " + msgType);
                continue;
            }

            // Update local buffer
            storeMsg(received);
        }
    }


    /* Synchronized functions for receivers */

    private synchronized void storeMsg(TYPE received){
        receiverBuffer.add(received);
    }

    /* Receiver caller functions */

    public synchronized boolean getFault(int elevNum){
        if (msgType != TYPE.FAULT){
            System.out.println(
                    "Unexpected behaviour! CommandBridge::getFault() called by " + msgType + ". Abort.");
            return false;
        }

        if (receiverBuffer.size() == 0) {
            // buffer is empty
            return false;
        }

        TYPE targetFault = TYPE.INVALID;

        // Check buffer
        for (TYPE fault: receiverBuffer){
            if (fault.payload1 == elevNum){
                targetFault = fault;
                break;
            }
        }

        // Update buffer and return
        if (targetFault != TYPE.INVALID){
            receiverBuffer.remove(targetFault);
            return true;
        }

        return false;
    }


    /**
     * old name: readlineFloor()
     * For FloorSubsystem
     * @return { FloorNum, Up (1) / Down (0) }
     */
    public synchronized Integer[] getFloorButton(){
        if (msgType != TYPE.FLOOR_BUTTON){
            System.out.println(
                    "Unexpected behaviour! CommandBridge::readlineFloor() called by " + msgType + ". Abort.");
            return null;
        }

        if (receiverBuffer.size() == 0) {
            // buffer is empty
            return null;
        }

        // Pop the first item from buffer
        TYPE targetLine = receiverBuffer.pop();

        // Return
        return new Integer[]{targetLine.payload1, targetLine.payload2};
    }


    /**
     *
     * @param elevNum
     * @return -1 if not found. Floor number if button clicked.
     */
    public synchronized int getElevButton(int elevNum){
        if (msgType != TYPE.ELEV_BUTTON){
            System.out.println(
                    "Unexpected behaviour! CommandBridge::getElevButton() called by " + msgType + ". Abort.");
            return -1;
        }

        // Check / update msg pool
        if (receiverBuffer.size() == 0) {
            // buffer is empty
            return -1;
        }

        TYPE targetButton = TYPE.INVALID;

        // Check buffer
        for (TYPE button: receiverBuffer){
            if(button.payload1 == elevNum){
                targetButton = button;
                break;
            }
        }

        // Update buffer and return
        if (targetButton != TYPE.INVALID){
            receiverBuffer.remove(targetButton);
            return targetButton.payload2;
        }

        return -1;
    }

}
