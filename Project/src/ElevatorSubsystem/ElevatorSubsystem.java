/**
 * 
 */
package ElevatorSubsystem;
import FloorSubsystem.FileLoader;
import Scheduler.Scheduler;
import common.Common;
import common.RPC;
import test.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ElevatorSubsystem implements Runnable{
	// Ports (maybe move this to Common class?)
	private final InetAddress SCHEDULER_ADDR;
	private final InetAddress ELEVATOR_ADDR;
	private static final int SCHEDULER_RECV_PORT = Test.SCHEDULER_RECV_ELEV_PORT;
	private static final int ELEV_SUB_RECV_PORT  = Test.ELEV_SUB_RECV_PORT;

	/* The Initial port number used for receiving from elevator
	*  (max 100 elevators)
	*  1st elevator = 10011
	*  2nd elevator = 10012
	*  3rd elevator = 10013
	*  etc... */
	private static final int ELEV_SUB_ELEV_RECV_PORT = Test.ELEV_SUB_ELEV_RECV_PORT;

	/* The Initial port number used for receiving by elevator
	 *  (max 100 elevators)
	 *  1st elevator = 10201
	 *  2nd elevator = 10202
	 *  3rd elevator = 10203
	 *  etc... */
	private static final int ELEV_RECV_PORT = Test.ELEV_RECV_PORT;

	// Class vars
	private final int NUM_ELEV;
	public Elevator[] elevs;
	private Thread[] elevators;
	private int serial = 0;

	// Buffer
	private HashMap<Integer, LinkedList<byte[]>> msgToElevators;
	private LinkedList<byte[]> msgToScheduler;


	/* Common code */

	public ElevatorSubsystem(int numElev,boolean GUI,boolean isTest) throws Exception {
		if (numElev <= 0){
			throw new Exception("incompatible setting: numElev should be at least 1.");
		}
		// Init inet address
		SCHEDULER_ADDR = InetAddress.getLocalHost();
		ELEVATOR_ADDR = InetAddress.getLocalHost();

		// Init elevators
		this.NUM_ELEV = numElev;
		elevators = new Thread[NUM_ELEV];
		elevs = new Elevator[NUM_ELEV];
		FileLoader fileLoader = new FileLoader("errorFile.txt", false);
		if(!isTest) {
			for (int i = 0; i < NUM_ELEV; ++i){
				int serialNum = i + 1;
				elevs[i] = new
						Elevator(serialNum, 1, true,
						ELEV_SUB_ELEV_RECV_PORT + serialNum,
						ELEV_RECV_PORT + serialNum,
						fileLoader,GUI);
				elevators[i] = new Thread (elevs[i]);
			}
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
			// start elevator thread
			System.out.println("Starting elevator " + (i + 1) + " thread.");
			elevators[i].start();
		}
	}


	/* Synchronized functions */

	// ONLY used when starting new elevator communicators
	private synchronized int assignSerial(){
		return ++serial;
	}

	// Initialize buffer for elevator with serialNumber
	// ONLY used when starting new elevator communicators
	public synchronized void initElevatorsBuffer(Integer serialNum){
		msgToElevators.put(serialNum, new LinkedList<byte[]>());
	}

	// Add msg to scheduler queue
	public synchronized void sendToScheduler(byte[] msg){
//		System.out.println("ElevSub holding msg for Scheduler...");
		// add msg to scheduler's queue
		msgToScheduler.add(msg);
	}

	// Add msg to elevator queue
	public synchronized void sendToElevator(byte[] msg){
//		System.out.println("ElevSub holding msg for Elevator...");
		// message should be a scheduler msg
		Common.TYPE messageType = Common.findType(msg);
		if(messageType == Common.TYPE.SCHEDULER){
			// decodeScheduler: index 0 corresponds to elev number
			Integer elevatorNum = Common.decode(msg)[0];
			// add msg to elevator's queue
			msgToElevators.get(elevatorNum).add(msg);
		}else{
			System.out.println("ERR! Unexpected msg from Scheduler: " + messageType);
		}
	}

	// Get msg for scheduler
	public synchronized byte[] getMsgScheduler(){
		if(msgToScheduler.isEmpty()){
			return null;
		}
//		System.out.println("ElevSub sending msg to Scheduler...");
		return msgToScheduler.pop();
	}

	// Get msg for elevator (providing elevator number)
	public synchronized byte[] getMsgElevator(Integer serialNum){
		if(msgToElevators.get(serialNum).isEmpty()){
			return null;
		}
//		System.out.println("ElevSub sending msg to Elevator...");
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

			if(Common.findType(msg) == Common.TYPE.CONFIRMATION){
				// Received confirmation
				Common.CONFIRMATION confirmationType = Common.findConfirmation(msg);
				if(confirmationType == Common.CONFIRMATION.CHECK){
					// Scheduler wants to check message
					// send anything needs to be sent to scheduler
					msg = getMsgScheduler();
					if (msg == null){
						// no message for scheduler, send a confirmation instead
						msg = Common.encodeConfirmation(Common.CONFIRMATION.NO_MSG);
					}

				}else{
					System.out.println("Unexpected msg from Scheduler: " + confirmationType);
					msg = Common.encodeConfirmation(Common.CONFIRMATION.RECEIVED);
				}

			}else{
				// Received message for elevator
				// make received message available for elevator
				sendToElevator(msg);
				msg = Common.encodeConfirmation(Common.CONFIRMATION.RECEIVED);
			}
			// Reply to Scheduler
			transmitter.sendPacket(msg);
		}
	}


	/* Elevator communicator code */

	// This thread communicates with a single elevator
	private void elevatorCommunicator(){
		// serialNumber = elevator number
		int serialNum = assignSerial();
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

			if(Common.findType(msg) == Common.TYPE.CONFIRMATION){
				// Received confirmation
				Common.CONFIRMATION confirmationType = Common.findConfirmation(msg);
				if(confirmationType == Common.CONFIRMATION.CHECK) {
					// Elevator wants to check message
					// send anything needs to be sent to elevator
					msg = getMsgElevator(serialNum);
					if (msg == null) {
						// no message for elevator, send a confirmation instead
						msg = Common.encodeConfirmation(Common.CONFIRMATION.NO_MSG);
					}

				}else{
					System.out.println("Unexpected msg from Elevator " + serialNum + ": " + confirmationType);
					msg = Common.encodeConfirmation(Common.CONFIRMATION.RECEIVED);
				}

			}else{
				// Elevator wants to send message
				// make received message available for scheduler
				sendToScheduler(msg);
				msg = Common.encodeConfirmation(Common.CONFIRMATION.RECEIVED);

			}
			// Reply to Elevator
			transmitter.sendPacket(msg);
		}
	}


	/* Static code */

	// For testing use ONLY!
	// Spawn elevator subsystem
	public static void main(String[] args) throws Exception{
		ElevatorSubsystem elevatorSubsystem = new ElevatorSubsystem(3,false,false);
		elevatorSubsystem.run();
	}

}
