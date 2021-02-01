/**
 * 
 */
package Scheduler;

import java.io.*;
import java.net.*;

/**
 * @author jcwha
 *
 */
public class ElevatorController {

	private DatagramPacket sendPacket, recievePacket;
	private DatagramSocket schedulerSocket;
	
	public ElevatorController() {
		try {
			schedulerSocket = new DatagramSocket(3000);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void waitForMessage() {
		byte[] bytes = new byte[100];
		recievePacket = new DatagramPacket(bytes, bytes.length);
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
