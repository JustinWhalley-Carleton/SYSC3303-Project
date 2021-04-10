package GUI;

import common.RPC;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CommandBridge{


    /* Message types */
    public enum TYPE{
        INVALID         ((byte) -1),
        FAULT           ((byte) 0),
        FLOOR_BUTTON    ((byte) 1),
        ELEV_BUTTON     ((byte) 2);

        private final byte value;
        private TYPE(byte b){ this.value = b; }

        // Determine which type the byte corresponds to
        public static TYPE findType(byte b){
            for (TYPE type: TYPE.values()){
                if(type.value == b) return type;
            }
            return INVALID;
        }
    }


    // Class vars
    private final TYPE msgType;
    private final Boolean isSender;
    private final int senderPort, receiverPort;
    private RPC transmitter;


    public CommandBridge(TYPE type, Boolean sender){
        // Initial identity
        msgType = type;
        isSender = sender;
        // Initialize ports
        senderPort = 0;
        receiverPort = 0;
        // Initialize transmitter
        try {
            transmitter = new RPC(InetAddress.getLocalHost(),
                    sender ? receiverPort : senderPort,
                    sender ? senderPort : receiverPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Start receiver thread in background.
        if (!sender){
            Thread receiverThread = new Thread(this::backgroundReceiver);
            receiverThread.start();
        }
    }


    // send function call by sender, to send payload.
    public void send(int payload1, int payload2){
        if (!isSender) {
            System.out.println("Unexpected behaviour! CommandBridge::send() called by receiver. Abort.");
            return;
        }

        // Encode

        // Send

    }


    /* Background Thread for receiver */
    public void backgroundReceiver() {

    }


    /* Receiver caller functions */

    public boolean getFault(int elevNum){
        if (msgType != TYPE.FAULT){
            System.out.println(
                    "Unexpected behaviour! CommandBridge::getFault() called by " + msgType + ". Abort.");
            return false;
        }
        // Check / update msg pool

        return false;
    }


    public String[] readlineFloor(){
        if (msgType != TYPE.FLOOR_BUTTON){
            System.out.println(
                    "Unexpected behaviour! CommandBridge::readlineFloor() called by " + msgType + ". Abort.");
            return null;
        }
        // Check / update msg pool

        return null;
    }


    public Integer[] getElevButton(int elevNum){
        if (msgType != TYPE.ELEV_BUTTON){
            System.out.println(
                    "Unexpected behaviour! CommandBridge::getElevButton() called by " + msgType + ". Abort.");
            return null;
        }
        // Check / update msg pool

        return null;
    }

}
