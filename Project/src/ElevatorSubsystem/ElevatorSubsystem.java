/**
 * 
 */
package ElevatorSubsystem;
import Scheduler.Scheduler;
import common.Common;
import common.RPC;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ElevatorSubsystem implements Runnable{
	// Ports (maybe move this to Common class?)
	private final InetAddress SCHEDULER_ADDR;
	private final InetAddress ELEVATOR_ADDR;
	private static final int SCHEDULER_RECV_PORT = 10000;
	private static final int ELEV_SUB_RECV_PORT  = 10001;

	/* The Initial port number used for receiving from elevator
	*  (max 100 elevators)
	*  1st elevator = 10011
	*  2nd elevator = 10012
	*  3rd elevator = 10013
	*  etc... */
	private static final int ELEV_SUB_ELEV_RECV_PORT = 10010;

	/* The Initial port number used for receiving by elevator
	 *  (max 100 elevators)
	 *  1st elevator = 10201
	 *  2nd elevator = 10202
	 *  3rd elevator = 10203
	 *  etc... */
	private static final int ELEV_RECV_PORT = 10200;

	// Class vars
	private final int NUM_ELEV;
	private Elevator[] elevators;
	private int serial = 0;

	// Buffer
	private HashMap<Integer, LinkedList<byte[]>> msgToElevators;
	private LinkedList<byte[]> msgToScheduler;


	/* Common code */

	public ElevatorSubsystem(int numElev) throws Exception {
		if (numElev <= 0){
			throw new Exception("incompatible setting: numElev should be at least 1.");
		}
		// Init inet address
		SCHEDULER_ADDR = InetAddress.getLocalHost();
		ELEVATOR_ADDR = InetAddress.getLocalHost();

		// Init elevators
		this.NUM_ELEV = numElev;
		elevators = new Elevator[NUM_ELEV];
		for (int i = 0; i < NUM_ELEV; ++i){
			elevators[i] = new Elevator(1, true);
		}

		// Init Buffer
		msgToElevators = new HashMap<Integer, LinkedList<byte[]>>();
		msgToScheduler = new LinkedList<byte[]>();
	}

	public void run() {
		// init & start scheduler communicator
		Thread schedulerCommunicator = new Thread(this::schedulerCommunicator);
		schedulerCommunicator.start();

		// init & start elevator communicator(s)
		Thread[] elevatorCommunicators = new Thread[NUM_ELEV];
		for(int i = 0; i < NUM_ELEV; ++i){
			elevatorCommunicators[i] = new Thread(this::elevatorCommunicator);
			elevatorCommunicators[i].start();
		}
	}


	/* Synchronized functions */

	// ONLY used when starting new elevator communicators
	private synchronized int getNewSerial(){
		serial += 1;
		return serial;
	}

	// Initialize buffer for elevator with serialNumber
	// ONLY used when starting new elevator communicators
	private synchronized void initElevatorsBuffer(Integer serialNum){
		msgToElevators.put(serialNum, new LinkedList<byte[]>());
	}

	// Add msg to scheduler queue
	private synchronized void sendToScheduler(byte[] msg){
		// add msg to scheduler's queue
		msgToScheduler.add(msg);
	}

	// Add msg to elevator queue
	private synchronized void sendToElevator(byte[] msg){
		// TODO: find out which elevator should receive this message
		Integer serialNum = 0;
		// add msg to elevator's queue
		msgToElevators.get(serialNum).add(msg);
	}

	// Get msg for scheduler
	private synchronized byte[] getMsgScheduler(){
		if(msgToScheduler.isEmpty()){
			return null;
		}
		return msgToScheduler.pop();
	}

	// Get msg for elevator (providing elevator number)
	private synchronized byte[] getMsgElevator(Integer serialNum){
		if(msgToElevators.get(serialNum).isEmpty()){
			return null;
		}
		return msgToElevators.get(serialNum).pop();
	}


	/* Scheduler communicator code */

	// This thread communicates with scheduler
	private void schedulerCommunicator() {
		// initialize vars
		RPC transmitter = new RPC(SCHEDULER_ADDR, SCHEDULER_RECV_PORT, ELEV_SUB_RECV_PORT);
		byte[] msg;

		while(true){
			// receive message from scheduler:
			// 1. scheduler ready to receive a new message
			// 2. scheduler have a message for one of the elevators
			msg = transmitter.receivePacket();

			// check: if msg is for elevator
			// make received message available for elevator
			sendToElevator(msg);

			// send anything needs to be sent to scheduler
			msg = getMsgScheduler();
			if (msg == null){
				// no message for scheduler, send a confirmation instead

			}
			transmitter.sendPacket(msg);
		}
	}


	/* Elevator communicator code */

	// This thread communicates with a single elevator
	private void elevatorCommunicator(){
		// serialNumber = elevator number
		int serialNum = getNewSerial();
		// Enable buffer for current elevator
		initElevatorsBuffer(serialNum);
		// initialize vars
		RPC transmitter = new RPC(ELEVATOR_ADDR,
						ELEV_RECV_PORT + serialNum,
						ELEV_SUB_ELEV_RECV_PORT + serialNum);
		byte[] msg;

		while(true){
			// receive message from elevator:
			// 1. elevator ready to receive a new message
			// 2. elevator have a message for scheduler
			msg = transmitter.receivePacket();

			// check: if msg if for scheduler
			// make received message available for scheduler
			sendToScheduler(msg);

			msg = getMsgElevator(serialNum);
			if (msg == null){
				// no message for elevator, send a confirmation instead

			}
			transmitter.sendPacket(msg);
		}
	}


	/* Static code */

	// For testing use ONLY!
	// Spawn elevator subsystem
	public static void main(String[] args) throws Exception{
		ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(3);
		Thread elevSubSystemThread = new Thread(elevatorSubsystem);

		elevSubSystemThread.run();
	}

}
