package common;
import java.io.IOException;
import java.net.*;

public class RPC {
    // Public class for send/ receive Packet
    // Used in Both client and Server class

    // class vars
    private final InetAddress DEST_ADDR;
    private final int DEST_PORT, RECV_PORT;
    private DatagramSocket sendReceiveSocket;
    private final int BUF_SIZE = 100;


    public RPC(InetAddress addr, int destPort, int recvPort) {
        DEST_ADDR = addr;
        DEST_PORT = destPort;
        RECV_PORT = recvPort;

        try {
            // Initialize socket for send/ receive
            sendReceiveSocket = new DatagramSocket(RECV_PORT);
        } catch (SocketException e) {
            // Failed to create socket
            e.printStackTrace();
            System.exit(1);
        }
    }


    public void sendPacket(byte[] msg){
        // Create datagramPacket
        DatagramPacket sendPacket = new DatagramPacket(msg, msg.length, DEST_ADDR, DEST_PORT);
        // Print Packet information
        System.out.println("Sending packet:");
        // Send Packet
        try {
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        // Packet sent
        System.out.println("*** Packet Sent ***");
        System.out.println();
    }


    public byte[] receivePacket(){
        byte[] data = new byte[BUF_SIZE];
        // Init receive packet
        DatagramPacket receivePacket = new DatagramPacket(data, data.length);
        System.out.println(" Waiting to receive packet...");
        // Receive Packet
        try {
            sendReceiveSocket.receive(receivePacket);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        // Print Packet information
        System.out.println("Received packet:");
        System.out.println();
        // Trim data & return
        byte[] result = new byte[receivePacket.getLength()];
        for(int i = 0; i < result.length; ++i) result[i] = data[i];
        return result;
    }

}
