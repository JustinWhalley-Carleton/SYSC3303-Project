/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JTextArea;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ElevatorSubsystem.*;
import FloorSubsystem.FileLoader;
import FloorSubsystem.GUIFileLoader;
import GUI.ElevatorPanel;
import GUI.GUI;
import GUI.Helper;
import GUI.TextManager;
import Scheduler.Scheduler;
import common.Common;
import Timer.TimerController;
import common.RPC;
import common.Common.ELEV_ERROR;
import test.*;

/**
 * @author jcwha
 *
 */
class JunitTestCases {
	static final String UP = "Up";
	static final String DOWN = "Down";
	static int ELEVATORS;
	static int SPEED;
	static int ELEV_ERR;
	static int ROWS;
	static int FLOORS;
	static int ELEV_RECV_PORT;
	static int ELEV_SUB_ELEV_RECV_PORT;
	static int FLOOR_SUB_RECV_PORT;
	static int SCHEDULER_RECV_FLOOR_PORT;
	static int ELEV_SUB_RECV_PORT;
	static int SCHEDULER_RECV_ELEV_PORT;
	static int GUI_RECV_SCHEDULER_PORT;
	static int SCHEDULER_RECV_GUI_PORT;
	static GUI gui;
	private static int floorTiming;
	
	/**
	 * create a new file to use as a test for file loader
	 * 
	 * @return string[] containing each line added to the file
	 */
	static String[] createFile() {
		String[] lines = new String[ROWS];
		int[] floor = new int[ROWS];
		String[] dir = new String[ROWS];
		String[] time = new String[ROWS];
		Random rand = new Random();
		int[] carButton = new int[ROWS];
		try {
			//get the path to file
			String path = System.getProperty("user.dir")+System.getProperty("file.separator")+"src"+System.getProperty("file.separator")+"test"+System.getProperty("file.separator")+"JunitTestFile.txt";
			// create a new file. if file is already made, delete it and make a new one
			File file = new File(path);
			if(file.exists()) {
				file.delete();
				file = new File(path);
			}
			file.createNewFile();
			
			LocalTime referenceTime = LocalTime.now().plus(10, ChronoUnit.SECONDS);
			for(int i = 0; i < ROWS; i++) {
				String temp = (referenceTime.plus(10*i,ChronoUnit.SECONDS)).toString();
				time[i] = temp;
				floor[i] = rand.nextInt(10);
				carButton[i] = rand.nextInt(10);
				while(floor[i] == carButton[i]) {
					carButton[i] = rand.nextInt(10);
				}
				if(floor[i] > carButton[i]) {
					dir[i] = DOWN;
				} else {
					dir[i] = UP;
				}
			}
			
			//write data to the file
			FileWriter writer = new FileWriter(file.getAbsoluteFile());
			for(int i = 0; i < ROWS; i++) {
				lines[i] = time[i] + " " + Integer.toString(floor[i]) + " " + dir[i] + " " + Integer.toString(carButton[i]);
				writer.write(time[i] + " " + Integer.toString(floor[i]) + " " + dir[i] + " " + Integer.toString(carButton[i])+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	static void createSettingsFile(int rows, int floors, int elevators, int speed, int elev_err, int port1, int port2, int port3,int port4, int port5, int port6, int port7, int port8) {
		try {
			String path = System.getProperty("user.dir")+System.getProperty("file.separator")+"src"+System.getProperty("file.separator")+"test"+System.getProperty("file.separator")+"settings.txt";
			File file = new File(path);
			if(file.exists()) {
				file.delete();
				file = new File(path);
			}
			file.createNewFile();
			FileWriter writer = new FileWriter(file.getAbsoluteFile());
			writer.write("// General settings\n");
			writer.write("FLOORS: "+floors+"\n");
			writer.write("ROWS: "+rows+"\n");
			writer.write("ELEVATORS: "+elevators+"\n");
			writer.write("SPEED: "+speed+"\n");
			writer.write("ELEV_ERR: " + elev_err+"\n");
			writer.write("\n// Subsystem ports\n");
			writer.write("ELEV_RECV_PORT: "+port1+"\n");
			writer.write("ELEV_SUB_ELEV_RECV_PORT: "+port2+"\n");
			writer.write("FLOOR_SUB_RECV_PORT: "+port3+"\n");
			writer.write("SCHEDULER_RECV_FLOOR_PORT: "+port4+"\n");
			writer.write("ELEV_SUB_RECV_PORT: "+port5+"\n");
			writer.write("SCHEDULER_RECV_ELEV_PORT: "+port6+"\n");
			writer.write("\n// GUI ports\n");
			writer.write("GUI_RECV_SCHEDULER_PORT: "+port7+"\n");
			writer.write("SCHEDULER_RECV_GUI_PORT: "+port8+"\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static int[] readSettingsFile() {
		int[] data = new int[13];
		try {
			Scanner scanner = new Scanner(new File("src/test/settings.txt"));
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] splitStr = line.trim().split("\\s+");

				if (splitStr.length == 1 || splitStr[0].trim().equals("//")){
					// Empty line or Comment in setting file.
					continue;
				}

				// Get value
				int value = Integer.parseInt(splitStr[1]);
				// Assign value to its according variable
				switch(splitStr[0].trim()){
					case "ELEVATORS:" 	-> data[0]	= value;
					case "ROWS:"		-> data[1]		= value;
					case "FLOORS:"		-> data[2]		= value;
					case "SPEED:"		-> data[3]		= value;
					case "ELEV_ERR:"	-> data[4]	= value;
					// Ports
					case "ELEV_RECV_PORT:" 				-> data[5] 				= value;
					case "ELEV_SUB_ELEV_RECV_PORT:" 	-> data[6] 		= value;
					case "FLOOR_SUB_RECV_PORT:" 		-> data[7] 			= value;
					case "SCHEDULER_RECV_FLOOR_PORT:" 	-> data[8] 	= value;
					case "ELEV_SUB_RECV_PORT:" 			-> data[9]			= value;
					case "SCHEDULER_RECV_ELEV_PORT:" 	-> data[10]	= value;
					
					case "GUI_RECV_SCHEDULER_PORT:"		-> data[11] 		= value;
					case "SCHEDULER_RECV_GUI_PORT:"		-> data[12]		= value;
					// Unsupported settings
					default -> System.out.println("Unexpected item in settings file.");
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}

	@BeforeAll
	public static void init() {
		int[] settings = readSettingsFile();
		ELEVATORS = settings[0];
		ROWS = settings[1];
		FLOORS = settings[2];
		SPEED = settings[3];
		ELEV_ERR = settings[4];
		ELEV_RECV_PORT = settings[5];
		ELEV_SUB_ELEV_RECV_PORT = settings[6];
		FLOOR_SUB_RECV_PORT = settings[7];
		SCHEDULER_RECV_FLOOR_PORT = settings[8];
		ELEV_SUB_RECV_PORT = settings[9];
		SCHEDULER_RECV_ELEV_PORT = settings[10];
		GUI_RECV_SCHEDULER_PORT = settings[11];
		SCHEDULER_RECV_GUI_PORT = settings[12];
		createSettingsFile(ROWS,FLOORS,29,4,ELEV_ERR,ELEV_RECV_PORT-20,ELEV_SUB_ELEV_RECV_PORT,FLOOR_SUB_RECV_PORT,SCHEDULER_RECV_FLOOR_PORT,ELEV_SUB_RECV_PORT,SCHEDULER_RECV_ELEV_PORT,GUI_RECV_SCHEDULER_PORT,SCHEDULER_RECV_GUI_PORT);
		GUIFileLoader.deleteFile();
		 gui = new GUI(true);
		 floorTiming  = Elevator.floorTiming*2;
		 for(ElevatorPanel panel : GUI.elevatorPanels) {
			 Helper.turnAllButtonsOn(panel.buttons);
		 }
	}
	
	@AfterAll
	public static void destroy() {
		createSettingsFile(ROWS,FLOORS,ELEVATORS,SPEED,ELEV_ERR,ELEV_RECV_PORT,ELEV_SUB_ELEV_RECV_PORT,FLOOR_SUB_RECV_PORT,SCHEDULER_RECV_FLOOR_PORT,ELEV_SUB_RECV_PORT,SCHEDULER_RECV_ELEV_PORT,GUI_RECV_SCHEDULER_PORT,SCHEDULER_RECV_GUI_PORT);
		GUIFileLoader.deleteFile();
	}
	
	@AfterEach
	public void destroyFile() {
		GUIFileLoader.deleteFile();
	}
	
	/**
	 * test the fileloader to ensure lines read properly
	 */
	@Test
	void testFileReader() {
		String[] lines = createFile();
		try {
			FileLoader fileLoader = new FileLoader("JunitTestFile.txt", true);
			int i = 0;
			while(fileLoader.hasNextInstruction()) {
				String text = fileLoader.getTime() + " " + fileLoader.departFloor() + " " + (fileLoader.requestUp() ? "Up" : "Down") + " " + fileLoader.destFloor();
				assertTrue(lines[i].equals(text), lines[i] + "    Does not equal    "+ text + "    on line =    "+Integer.toString(i) + "   ");
				fileLoader.nextLine();
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * test the encode elevator message going up gives correct byte[]
	 */
	@Test
	void testEncodeElevatorUp() {
		int elevatorNum = 1;
		int curr = 2;
		MotorState state = new Up();
		int dest = 5;
		
		byte[] msg = Common.encodeElevator(elevatorNum, curr, state, dest);
		
		assertTrue(msg instanceof byte[], "message is an array of bytes");
		assertTrue(0==(int)msg[0], "Elevator identifier correct");
		assertTrue(elevatorNum == (int)msg[2], "returns correct elevator number");
		assertTrue(curr == (int)msg[4], "returns correct current floor");
		assertTrue(1 == (int)msg[6], "returns the correct state");
		assertTrue(dest == (int)msg[8], "returns correct destination floor");
	}

	/**
	 * test the encode elevator message going Donw gives correct byte[]
	 */
	@Test
	void testEncodeElevatorDown() {
		int elevatorNum = 1;
		int curr = 9;
		MotorState state = new Down();
		int dest = 5;
		
		byte[] msg = Common.encodeElevator(elevatorNum, curr, state, dest);
		
		assertTrue(msg instanceof byte[], "message is an array of bytes");
		assertTrue(0==(int)msg[0], "Elevator identifier correct");
		assertTrue(elevatorNum == (int)msg[2], "returns correct elevator number");
		assertTrue(curr == (int)msg[4], "returns correct current floor");
		assertTrue(-1 == (int)msg[6], "returns the correct state");
		assertTrue(dest == (int)msg[8], "returns correct destination floor");
	}
	
	/**
	 * test the encode elevator message in idle gives correct byte[]
	 */
	@Test
	void testEncodeElevatorIdle() {
		int elevatorNum = 1;
		int curr = 2;
		MotorState state = new Idle();
		int dest = 5;
		
		byte[] msg = Common.encodeElevator(elevatorNum, curr, state, dest);
		
		assertTrue(msg instanceof byte[], "message is an array of bytes");
		assertTrue(0==(int)msg[0], "Elevator identifier correct");
		assertTrue(elevatorNum == (int)msg[2], "returns correct elevator number");
		assertTrue(curr == (int)msg[4], "returns correct current floor");
		assertTrue(0 == (int)msg[6], "returns the correct state");
		assertTrue(dest == (int)msg[8], "returns correct destination floor");
	}
	
	/**
	 * test decode invalid elevator error
	 */
	@Test
	void testEnumInvalidElevError() {
		byte[] msg = Common.encodeElevError(ELEV_ERROR.INVALID, 1, 1, 1, true);
		assertEquals(Common.TYPE.ELEV_ERROR,Common.findType(msg));
		assertEquals(Common.ELEV_ERROR.INVALID,Common.ELEV_ERROR.decode(msg));
	}
	
	/**
	 * test decode unknown elevator error
	 */
	@Test
	void testEnumUnknownElevError() {
		byte[] msg = Common.encodeElevError(ELEV_ERROR.UNKNOWN, 1, 1, 1, true);
		assertEquals(Common.TYPE.ELEV_ERROR,Common.findType(msg));
		assertEquals(Common.ELEV_ERROR.UNKNOWN,Common.ELEV_ERROR.decode(msg));
	}
	
	/**
	 * test decode stuck elevator error
	 */
	@Test
	void testEnumStuckElevError() {
		byte[] msg = Common.encodeElevError(ELEV_ERROR.STUCK, 1, 1, 1, true);
		assertEquals(Common.TYPE.ELEV_ERROR,Common.findType(msg));
		assertEquals(Common.ELEV_ERROR.STUCK,Common.ELEV_ERROR.decode(msg));
	}
	
	/**
	 * test decode door open elevator error
	 */
	@Test
	void testEnumDoorOpenError() {
		byte[] msg = Common.encodeElevError(ELEV_ERROR.DOOR_OPEN, 1, 1, 1, true);
		assertEquals(Common.TYPE.ELEV_ERROR,Common.findType(msg));
		assertEquals(Common.ELEV_ERROR.DOOR_OPEN,ELEV_ERROR.decode(msg));
	}
	
	/**
	 * test decode door close elevator error
	 */
	@Test
	void testEnumDoorCloseElevatorError() {
		byte[] msg = Common.encodeElevError(ELEV_ERROR.DOOR_CLOSE, 1, 1, 1, true);
		assertEquals(Common.TYPE.ELEV_ERROR,Common.findType(msg));
		assertEquals(Common.ELEV_ERROR.DOOR_CLOSE,Common.ELEV_ERROR.decode(msg));
	}
	
	/**
	 * test decode recover elevator error
	 */
	@Test
	void testEnumRecoverElev() {
		byte[] msg = Common.encodeElevError(ELEV_ERROR.RECOVER, 1, 1, 1, true);
		assertEquals(Common.TYPE.ELEV_ERROR,Common.findType(msg));
		assertEquals(Common.ELEV_ERROR.RECOVER,Common.ELEV_ERROR.decode(msg));
	}	
	
	/**
	 * test the encode floor message up gives correct byte[]
	 */
	@Test
	void testEncodeFloorUp() {
		int floor = 5;
		boolean dir = true;
		byte[] msg = Common.encodeFloor(floor, dir);
		
		assertTrue(msg instanceof byte[], "message is an array of bytes");
		assertTrue(1==(int)msg[0], "Floor identifier correct");
		assertTrue(floor == (int)msg[2], "returns correct floor");
		assertTrue(1 == (int)msg[4], "returns correct direction");
	}
	
	/**
	 * test the encode floor message down gives correct byte[]
	 */
	@Test
	void testEncodeFloorDown() {
		int floor = 5;
		boolean dir = false;
		byte[] msg = Common.encodeFloor(floor, dir);
		
		assertTrue(msg instanceof byte[], "message is an array of bytes");
		assertTrue(1==(int)msg[0], "Floor identifier correct");
		assertTrue(floor == (int)msg[2], "returns correct floor");
		assertTrue(0 == (int)msg[4], "returns correct direction");
	}
	
	/**
	 * test the encode scheduler gives correct byte[]
	 */
	@Test
	void testEncodeScheduler() {
		int floor = 6;
		int elevatorNum = 1;
		int dir = 1;
		byte[] msg = Common.encodeScheduler(elevatorNum,floor,dir);
		
		assertTrue(msg instanceof byte[], "message is an array of bytes");
		assertTrue(2 == (int) msg[0], "Scheduler identifier correct");
		assertTrue(floor == (int)msg[4], "returns correct floor");
		assertTrue(elevatorNum == (int)msg[2], "returns correct elevator number");
		assertTrue(dir == (int)msg[6], "returns correct direction");
	}
	
	/**
	 * test the decode elevator message going up gives correct int[]
	 */
	@Test
	void testDecodeElevatorUp() {
		int elevatorNum = 1;
		int curr = 2;
		MotorState state = new Up();
		int dest = 5;
		
		byte[] msg = Common.encodeElevator(elevatorNum, curr, state, dest);
		
		int[] val = Common.decode(msg);
		
		assertTrue(val instanceof int[], "message is an array of int");
		assertTrue(4 == val.length, "length of array is 4");
		assertTrue(elevatorNum == val[0], "returns the correct elevator number");
		assertTrue(curr == val[1], "returns the correct current floor");
		assertTrue(1 == val[2], "returns the correct state");
		assertTrue(dest == val[3], "returns the correct destination floor");
	}
	
	/**
	 * test the decode elevator message going down gives correct int[]
	 */
	@Test
	void testDecodeElevatorDown() {
		int elevatorNum = 1;
		int curr = 2;
		MotorState state = new Down();
		int dest = 5;
		
		byte[] msg = Common.encodeElevator(elevatorNum, curr, state, dest);
		
		int[] val = Common.decode(msg);
		
		assertTrue(val instanceof int[], "message is an array of int");
		assertTrue(4 == val.length, "length of array is 4");
		assertTrue(elevatorNum == val[0], "returns the correct elevator number");
		assertTrue(curr == val[1], "returns the correct current floor");
		assertTrue(-1 == val[2], "returns the correct state");
		assertTrue(dest == val[3], "returns the correct destination floor");
	}
	
	/**
	 * test the decode elevator message in idle gives correct int[]
	 */
	@Test
	void testDecodeElevatorIdle() {
		int elevatorNum = 1;
		int curr = 2;
		MotorState state = new Idle();
		int dest = 5;
		
		byte[] msg = Common.encodeElevator(elevatorNum, curr, state, dest);
		
		int[] val = Common.decode(msg);
		
		assertTrue(val instanceof int[], "message is an array of int");
		assertTrue(4 == val.length, "length of array is 4");
		assertTrue(elevatorNum == val[0], "returns the correct elevator number");
		assertTrue(curr == val[1], "returns the correct current floor");
		assertTrue(0 == val[2], "returns the correct state");
		assertTrue(dest == val[3], "returns the correct destination floor");
	}
	
	/**
	 * test the decode floor message up gives correct int[]
	 */
	@Test
	void testDecodeFloorUp() {
		int floor = 5;
		boolean dir = true;
		byte[] msg = Common.encodeFloor(floor, dir);
		
		int[] val = Common.decode(msg);

		assertTrue(val instanceof int[], "message is an array of int");
		assertTrue(val.length == 2, "length of array is 2");
		assertTrue(floor == val[0], "returns the correct floor");
		assertTrue(1 == val[1], "returns the correct direction");
	}
	
	/**
	 * test the decode floor message down gives correct int[]
	 */
	@Test
	void testDecodeFloorDown() {
		int floor = 5;
		boolean dir = false;
		byte[] msg = Common.encodeFloor(floor, dir);
		
		int[] val = Common.decode(msg);

		assertTrue(val instanceof int[], "message is an array of int");
		assertTrue(val.length == 2, "length of array is 2");
		assertTrue(floor == val[0], "returns the correct floor");
		assertTrue(0 == val[1], "returns the correct direction");
	}
	
	/**
	 * test the decode scheduler gives correct int[]
	 */
	@Test
	void testDecodeScheduler() {
		int floor = 6;
		int elevatorNum = 1;
		int dir = 1;
		byte[] msg = Common.encodeScheduler(elevatorNum, floor, dir);
		int[] val = Common.decode(msg);
		assertTrue(val instanceof int[], "message is an array of int");
		assertTrue(val.length == 3, "length of array is 1");
		assertTrue(elevatorNum == val[0], "return correct elevator number");
		assertTrue(floor == val[1], "return correct floor value");
		assertTrue(dir == val[2], "return correct direction");
	}
	
	
	/**
	 * test the timer gets interrupted before completion
	 */
	@Test
	void testTimerInterrupt() {
		TimerController timer;
		timer = new TimerController(2000, new Elevator());
		timer.start();
		assertTrue(timer.isRunning());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timer.stop();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse(timer.isRunning());
		
	}
	
	/**
	 * test the timer completes 
	 */
	@Test
	void testTimerNoInterrupt() {
		TimerController timer;
		timer = new TimerController(1000, new Elevator());
		timer.start();
		assertTrue(timer.isRunning());
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse(timer.isRunning());
		
	}
	
	/**
	 * test the timer stop will not trigger a start interrupt when not started
	 */
	@Test
	void testTimerNoStartStop() {
		TimerController timer;
		timer = new TimerController(2000, new Elevator());
		timer.stop();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse(timer.isRunning());
		
	}
	
	/**
	 * test the timer double start will not trigger a stop interrupt
	 */
	@Test
	void testTimerDoubleStart() {
		TimerController timer;
		timer = new TimerController(2000, new Elevator());
		timer.start();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timer.start();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(timer.isRunning());
		
	}
	
	/**
	 * test rpc send and receive
	 */
	@Test
	void testRPCSendAndReceive() {
		RPC rpc1;
		try {
			rpc1 = new RPC(InetAddress.getLocalHost(),3,4);
			RPC rpc2 = new RPC(InetAddress.getLocalHost(),4,3);
			byte[] data = Common.encodeFloor(10, false);
			rpc1.sendPacket(data);
			byte[] received = rpc2.receivePacket();
			assertArrayEquals(data,received);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * test read from settings rows
	 */
	@Test
	void testReadSettingsRows() {
		int[] prevData = readSettingsFile();
		createSettingsFile(10,5,15,3,1, 1,2,3,4,5,6,7,8);
		int[] newData = readSettingsFile();
		assertEquals(newData[1], 10);
		createSettingsFile(prevData[1],prevData[2],prevData[0],prevData[3],prevData[4],prevData[5],prevData[6],prevData[7],prevData[8],prevData[9],prevData[10],prevData[11],prevData[12]);
	}
	
	/**
	 * test read from settings floors
	 */
	@Test
	void testReadSettingsFloors() {
		int[] prevData = readSettingsFile();
		createSettingsFile(10,5,15,3,1,2,3,4,5,6,7,8,9);
		int[] newData = readSettingsFile();
		assertEquals(newData[2], 5);
		createSettingsFile(prevData[1],prevData[2],prevData[0],prevData[3],prevData[4],prevData[5],prevData[6],prevData[7],prevData[8],prevData[9],prevData[10],prevData[11],prevData[12]);
	}
	
	/**
	 * test read from settings elevators
	 */
	@Test
	void testReadSettingsElevators() {
		int[] prevData = readSettingsFile();
		createSettingsFile(10,5,15,3,1,2,3,4,5,6,7,8,9);
		int[] newData = readSettingsFile();
		assertEquals(newData[0], 15);
		createSettingsFile(prevData[1],prevData[2],prevData[0],prevData[3],prevData[4],prevData[5],prevData[6],prevData[7],prevData[8],prevData[9],prevData[10],prevData[11],prevData[12]);
	}
	
	/**
	 * test read from settings speed
	 */
	@Test
	void testReadSettingsSpeed() {
		int[] prevData = readSettingsFile();
		createSettingsFile(10,5,15,3,1,2,3,4,5,6,7,8,9);
		int[] newData = readSettingsFile();
		assertEquals(newData[3], 3);
		createSettingsFile(prevData[1],prevData[2],prevData[0],prevData[3],prevData[4],prevData[5],prevData[6],prevData[7],prevData[8],prevData[9],prevData[10],prevData[11],prevData[12]);
	}
	
	/**
	 * test common enum confirmation
	 */
	@Test
	void testEnumConfirmation() {
		byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.RECEIVED);
		Common.TYPE type = Common.findType(msg);
		assertEquals(Common.TYPE.CONFIRMATION,type);
	}
	
	/**
	 * test common enum confirmation check
	 */
	@Test
	void testEnumConfirmationCheck() {
		byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.CHECK);
		Common.TYPE type = Common.findType(msg);
		assertEquals(Common.TYPE.CONFIRMATION,type);
		Common.CONFIRMATION confirmation = Common.findConfirmation(msg);
		assertEquals(Common.CONFIRMATION.CHECK,confirmation);
	}
	
	/**
	 * test common enum confirmation received
	 */
	@Test
	void testEnumConfirmationReceived() {
		byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.RECEIVED);
		Common.TYPE type = Common.findType(msg);
		assertEquals(Common.TYPE.CONFIRMATION,type);
		Common.CONFIRMATION confirmation = Common.findConfirmation(msg);
		assertEquals(confirmation, Common.CONFIRMATION.RECEIVED);
	}
	
	/**
	 * test common enum confirmation no msg
	 */
	@Test
	void testEnumConfirmationNoMsg() {
		byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.NO_MSG);
		Common.TYPE type = Common.findType(msg);
		assertEquals(Common.TYPE.CONFIRMATION,type);
		Common.CONFIRMATION confirmation = Common.findConfirmation(msg);
		assertEquals(Common.CONFIRMATION.NO_MSG,confirmation);
	}
	
	/**
	 * test common enum Scheduler
	 */
	@Test
	void testEnumScheduler() {
		byte[] msg = Common.encodeScheduler(1,2,3);
		Common.TYPE type = Common.findType(msg);
		assertEquals(Common.TYPE.SCHEDULER,type);
	}
	
	/**
	 * test common enum floor
	 */
	@Test
	void testEnumFloor() {
		byte[] msg = Common.encodeFloor(1,true);
		Common.TYPE type = Common.findType(msg);
		assertEquals(Common.TYPE.FLOOR,type);
	}
	
	/**
	 * test common enum elevator
	 */
	@Test
	void testEnumElevator() {
		byte[] msg = Common.encodeElevator(1,2,(MotorState)new Idle(),3);
		Common.TYPE type = Common.findType(msg);
		assertEquals(Common.TYPE.ELEVATOR,type);
	}
	
	/**
	 * test common enum invalid
	 */
	@Test
	void testEnumInvalid() {
		byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.INVALID);
		Common.TYPE type = Common.findType(msg);
		assertEquals(Common.TYPE.CONFIRMATION,type);
		Common.CONFIRMATION confirmation = Common.findConfirmation(msg);
		assertEquals(Common.CONFIRMATION.INVALID,confirmation);
	}
	
	/**
	 * test fault between floor stops elevator
	 */
	@Test
	void testFaultBetweenFloors() {
		GUI.elevatorPanels[20].buttons[21].doClick();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GUI.elevatorPanels[20].faultButton.doClick();
		try {
			Thread.sleep(5500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("State: FAULT",GUI.elevatorPanels[20].stateLabel.getText());
		try {
			Thread.sleep(3*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("State: FAULT",GUI.elevatorPanels[20].stateLabel.getText());
	}
	
	/**
	 * test fault recovered
	 */
	@Test 
	void testFaultRecovered() {
		GUI.elevatorPanels[16].faultButton.doClick();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("State: FAULT",GUI.elevatorPanels[16].stateLabel.getText());
		try {
			Thread.sleep(9*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotEquals("State: FAULT",GUI.elevatorPanels[16].stateLabel.getText());
	}
	
	/**
	 * test floor rescheduled
	 */
	@Test
	void testFloorNotRescheduled() {
		GUI.elevatorPanels[15].buttons[20].doClick();
		GUI.elevatorPanels[15].faultButton.doClick();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("State: FAULT",GUI.elevatorPanels[15].stateLabel.getText());
		try {
			Thread.sleep(3*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("State: FAULT",GUI.elevatorPanels[15].stateLabel.getText());
		boolean flag = false;
		for(Elevator elev : gui.getElev().elevs) {
			if(elev.map.get(21) != null) {
				flag = true;
			}
		}
		assertFalse(flag);
	}
	
	/**
	 * test elevator up state
	 */
	@Test
	void testUpState() {
		
	}
	
	/**
	 * test elevator down state
	 */
	@Test
	void testDownState() {
		
	}
	
	/**
	 * test elevator idle state
	 */
	@Test 
	void testIdleState() {
		
	}
	
	/**
	 * test floor increments in up state
	 */
	@Test
	void testIncrementCurFloor() {
		GUI.elevatorPanels[19].buttons[5].doClick();
		try {
			Thread.sleep(5*floorTiming);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int curFloor = Integer.parseInt(GUI.elevatorPanels[19].curFloorLabel.getText().split(" ")[2]);
		GUI.elevatorPanels[19].buttons[7].doClick();
		try {
			Thread.sleep(floorTiming + 250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(curFloor < Integer.parseInt(GUI.elevatorPanels[19].curFloorLabel.getText().split(" ")[2]));
	}
	
	/**
	 * test floor decrements in down state
	 */
	@Test
	void testDecrementCurFloor() {
		GUI.elevatorPanels[7].buttons[4].doClick();
		try {
			Thread.sleep(5*floorTiming);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int curFloor = Integer.parseInt(GUI.elevatorPanels[7].curFloorLabel.getText().split(" ")[2]);
		GUI.elevatorPanels[7].buttons[2].doClick();
		try {
			Thread.sleep(2*floorTiming + 250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(curFloor > Integer.parseInt(GUI.elevatorPanels[7].curFloorLabel.getText().split(" ")[2]));
	}
	
	/**
	 * test time elapsed between floors
	 */
	@Test
	void testTimeElapsed() {
		GUI.elevatorPanels[0].buttons[15].doClick();
		int curFloor = Integer.parseInt(GUI.elevatorPanels[0].curFloorLabel.getText().split(" ")[2]);
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(curFloor,Integer.parseInt(GUI.elevatorPanels[0].curFloorLabel.getText().split(" ")[2]));
		try {
			Thread.sleep(3*floorTiming+500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotEquals(curFloor,Integer.parseInt(GUI.elevatorPanels[0].curFloorLabel.getText().split(" ")[2]));
	}
	
	/**
	 * test num elevators created
	 */
	@Test
	void testNumberOfElevatorsCreate() {
		int[] settings = readSettingsFile();
		assertEquals(settings[0],gui.getElev().elevs.length);
	}
	
	/**
	 * test ports used
	 */
	@Test 
	void testPortsUsed() {
		assertEquals(ELEV_SUB_ELEV_RECV_PORT,test.Test.ELEV_SUB_ELEV_RECV_PORT);
		assertEquals(ELEV_SUB_RECV_PORT,test.Test.ELEV_SUB_RECV_PORT);
		assertEquals(ELEV_RECV_PORT-20,test.Test.ELEV_RECV_PORT);
		assertEquals(FLOOR_SUB_RECV_PORT,test.Test.FLOOR_SUB_RECV_PORT);
		assertEquals(SCHEDULER_RECV_FLOOR_PORT,test.Test.SCHEDULER_RECV_FLOOR_PORT);
		assertEquals(SCHEDULER_RECV_ELEV_PORT,test.Test.SCHEDULER_RECV_ELEV_PORT);
	}
	
	/**
	 * test proper number of floors read
	 */
	@Test
	void testNumFloors() {
		int[] settings = readSettingsFile();
		assertEquals(settings[2],gui.getFloor().floors.length);
	}
	
	/**
	 * test read command floorsubsystem up
	 */
	@Test
	void testReadCommandFloorUp() {
		GUIFileLoader.writeToFile(1,5,1);
		LocalTime curTime = LocalTime.now();
		String[] floorCommand = GUIFileLoader.readLineFloor();
		LocalTime commandTime = LocalTime.parse(floorCommand[0]);
		Duration timeDiff = Duration.between(curTime,commandTime);
		assertEquals(0,timeDiff.toHoursPart());
		assertEquals(0,timeDiff.toMinutesPart());
		assertEquals(0,timeDiff.toSecondsPart(),1); // allow a 1 second difference
		assertEquals("UP", floorCommand[2]);
	}
	
	/**
	 * test read command floorsubsystem dowm
	 */
	@Test
	void testReadCommandFloorDown() {
		GUIFileLoader.writeToFile(1,5,0);
		LocalTime curTime = LocalTime.now();
		String[] floorCommand = GUIFileLoader.readLineFloor();
		LocalTime commandTime = LocalTime.parse(floorCommand[0]);
		Duration timeDiff = Duration.between(curTime,commandTime);
		assertEquals(0,timeDiff.toHoursPart());
		assertEquals(0,timeDiff.toMinutesPart());
		assertEquals(0,timeDiff.toSecondsPart(),1); // allow a 1 second difference
		assertEquals(5,Integer.parseInt(floorCommand[1]));
		assertEquals("DOWN", floorCommand[2]);
	}
	
	/**
	 * test read floor lien delets command
	 */
	@Test
	void testDeleteAfterReadFloor() {
		GUIFileLoader.writeToFile(1,5,0);
		GUIFileLoader.readLineFloor();
		String[] floorCommand = GUIFileLoader.readLineFloor();
		assertNull(floorCommand);
	}
	
	/**
	 * test read command elevatorSubsystem
	 */
	@Test
	void testReadCommandElev() {
		GUIFileLoader.writeToFile(2, 1, 10);
		assertTrue(GUIFileLoader.elevHasCommand(1));
		assertFalse(GUIFileLoader.elevHasCommand(2));
		assertNull(GUIFileLoader.getElevButton(3));
		assertEquals((Integer)10,GUIFileLoader.getElevButton(1)[0]);
	}
	
	/**
	 * test read command elevatorSubsystem multiple dest
	 */
	@Test
	void testReadCommandElevMultipleDest() {
		GUIFileLoader.writeToFile(2, 1, 10);
		GUIFileLoader.writeToFile(2, 1, 11);
		GUIFileLoader.writeToFile(2, 1, 12);
		assertTrue(GUIFileLoader.elevHasCommand(1));
		assertFalse(GUIFileLoader.elevHasCommand(2));
		Integer[] result = GUIFileLoader.getElevButton(1);
		assertEquals((Integer)10,result[0]);
		assertEquals((Integer)11,result[1]);
		assertEquals((Integer)12,result[2]);
	}
	
	/**
	 * test read elev deletes command
	 */
	@Test
	void testElevCommandDeletesCommand() {
		GUIFileLoader.writeToFile(2, 1, 10);
		GUIFileLoader.writeToFile(2, 1, 11);
		GUIFileLoader.writeToFile(2, 1, 12);
		GUIFileLoader.getElevButton(1);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNull(GUIFileLoader.getElevButton(1));
	}
	
	/**
	 * test read fault command
	 */
	@Test
	void testReadFaultCommand() {
		GUIFileLoader.writeToFile(0, 1, -1);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse(GUIFileLoader.getFault(2));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(GUIFileLoader.getFault(1));
	}
	
	/**
	 * test delete command after read fault
	 */
	@Test
	void testDeleteFaultCommandAfterRead() {
		GUIFileLoader.writeToFile(0, 1, -1);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GUIFileLoader.getFault(1);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse(GUIFileLoader.getFault(1));
	}
	
	/**
	 * test elevator receive message
	 */
	@Test 
	void testElevReceive() {
		GUI.upButtons[10].doClick();
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean flag = false;
		for(ElevatorPanel elev : GUI.elevatorPanels) {
			if(elev.destLabel.getText().equals("Destination Floor: 11")) {
				flag = true;
			}
		}
		assertTrue(flag);
	}
	
	/**
	 * test floor receive message
	 */
	@Test 
	void testFloorReceive() {
		GUI.upButtons[8].doClick();
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(gui.getFloor().floors[8].buttonUpOn());
		try {
			Thread.sleep(8*floorTiming);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse(gui.getFloor().floors[8].buttonUpOn());
	}
	
	/**
	 * test bad floor input
	 */
	@Test
	void testBadFloorInput() {
		GUIFileLoader.writeToFile(1,25, 0);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean flag = false;
		for(ElevatorPanel elev : GUI.elevatorPanels) {
			if(elev.destLabel.getText().equals("Destination Floor: 25")) {
				flag = true;
			}
		}
		assertFalse(flag);
	}
	
	/**
	 * test bad elev Input
	 */
	@Test
	void testBadElevInput() {
		GUIFileLoader.writeToFile(1,100, 25);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean flag = false;
		for(ElevatorPanel elev : GUI.elevatorPanels) {
			if(elev.destLabel.getText().equals("Destination Floor: 25")) {
				flag = true;
			}
		}
		assertFalse(flag);
	}
	
	/**
	 * test elev button changes color when clicked
	 */
	@Test
	void testGUIButtonColorElev() {
		Helper.turnAllButtonsOn(GUI.elevatorPanels[10].buttons);
		GUI.elevatorPanels[10].buttons[4].doClick();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(Color.YELLOW, GUI.elevatorPanels[10].buttons[4].getBackground());
	}
	
	/**
	 * test elev button disable once click
	 */
	@Test
	void testElevButtonDisabled() {
		GUI.upButtons[2].doClick();
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse(GUI.upButtons[2].isEnabled());
		try {
			Thread.sleep(2*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test elev button reenabled
	 */
	@Test
	void testElevButtonReenabled() {
		GUI.upButtons[5].doClick();
		try {
			Thread.sleep(7*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(GUI.upButtons[2].isEnabled());
	}
	
	/**
	 * test scroll panel gets text
	 */
	@Test
	void testPrintToScrollPanel() {
		JTextArea textArea = GUI.textPanel;
		TextManager.print("This is a test message");
		String[] text = textArea.getText().split("\n");
		assertEquals("This is a test message",text[text.length-1].trim());
	}
	
	/**
	 * test number of buttons created Floor panel
	 */
	@Test 
	void testNumFloorsGUI() {
		int floors = readSettingsFile()[2];
		assertEquals(floors, GUI.upButtons.length);
		assertEquals(floors, GUI.downButtons.length);
	}
	
	/**
	 * test number of buttons in elevator panel
	 */
	@Test
	void testNumFloorsInElevGUI() {
		int[] settings = readSettingsFile();
		int floors = settings[2];
		int elevators = settings[0];
		for(int i = 0; i < elevators;i++) {
			assertEquals(floors,GUI.elevatorPanels[i].buttons.length);
		}
	}
	
	/**
	 * test number of elevators
	 */
	@Test
	void testNumElevsGUI() {
		int elevators = readSettingsFile()[0];
		assertEquals(elevators, GUI.elevatorPanels.length);
	}
	
	/**
	 * test get floor change in GUI
	 */
	@Test
	void testCurFloorLabelGUI() {
		Helper.turnAllButtonsOn(GUI.elevatorPanels[0].buttons);
		GUI.elevatorPanels[0].buttons[1].doClick();
		try {
			Thread.sleep(3*floorTiming+250+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int curFloor = gui.getElev().elevs[0].curFloor;
		assertEquals("Current Floor: "+curFloor, GUI.elevatorPanels[0].curFloorLabel.getText());
		GUI.elevatorPanels[0].buttons[2].doClick();
		try {
			Thread.sleep(2*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		curFloor = gui.getElev().elevs[0].curFloor;
		assertEquals("Current Floor: "+curFloor, GUI.elevatorPanels[0].curFloorLabel.getText());
	}
	
	/**
	 * test get dest floor label GUI
	 */
	@Test
	void testDestFloorLabelGUI() {
		Helper.turnAllButtonsOn(GUI.elevatorPanels[11].buttons);
		GUI.elevatorPanels[11].buttons[2].doClick();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("Destination Floor: 3", GUI.elevatorPanels[11].destLabel.getText());
	}
	
	/**
	 * test get fault state label GUI 
	 */
	@Test
	void testStateFaultLabelGUI() {
		GUI.elevatorPanels[14].faultButton.doClick();
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("State: FAULT", GUI.elevatorPanels[14].stateLabel.getText());
		try {
			Thread.sleep(5*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test get Idle state label GUI 
	 */
	@Test
	void testStateIdleLabelGUI() {
		assertEquals("State: Idle",GUI.elevatorPanels[3].stateLabel.getText());
	}
	
	/**
	 * test get up state label GUI 
	 */
	@Test
	void testStateUpLabelGUI() {
		Helper.turnAllButtonsOn(GUI.elevatorPanels[6].buttons);
		GUI.elevatorPanels[6].buttons[0].doClick();
		try {
			Thread.sleep(5*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GUI.elevatorPanels[6].buttons[3].doClick();
		try {
			Thread.sleep(floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("State: Up",GUI.elevatorPanels[6].stateLabel.getText());
		try {
			Thread.sleep(2*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test get down state label GUI 
	 */
	@Test
	void testStateDownLabelGUI() {
		Helper.turnAllButtonsOn(GUI.elevatorPanels[0].buttons);
		GUI.elevatorPanels[11].buttons[3].doClick();
		try {
			Thread.sleep(5*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GUI.elevatorPanels[11].buttons[0].doClick();
		try {
			Thread.sleep(floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("State: Down",GUI.elevatorPanels[11].stateLabel.getText());
		try {
			Thread.sleep(2*floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test floor button on up
	 */
	@Test
	void testFloorButtonOnUp() {
		GUI.upButtons[1].doClick();
		try {
			Thread.sleep(7*floorTiming+250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(GUI.upButtons[3].isEnabled());
	}
	
	/**
	 * test floor button on down
	 */
	@Test
	void testFloorButtonOnDown() {
		GUI.downButtons[3].doClick();
		try {
			Thread.sleep(5*floorTiming+250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(GUI.upButtons[3].isEnabled());
	}
	
	/**
	 * test floor button off up
	 */
	@Test
	void testFloorButtonOffUp() {
		GUI.upButtons[15].doClick();
		assertFalse(GUI.upButtons[15].isEnabled());
		try {
			Thread.sleep(floorTiming+250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test scheduler picks an alternate elev
	 */
	@Test
	void testSchedulerPicksNewClosestElev() {
		GUI.upButtons[16].doClick();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GUI.upButtons[1].doClick();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean flag = false;
		for(Elevator elev : gui.getElev().elevs) {
			if(elev.map.get(16) != null && elev.map.get(15) != null) {
				flag = true;
			}
		}
		assertFalse(flag);
		try {
			Thread.sleep(5*floorTiming);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test scheduler picks an alternate elev with direction
	 */
	@Test
	void testSchedulerPicksProperDirElev() {
		GUI.upButtons[16].doClick();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GUI.downButtons[7].doClick();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean flag = false;
		for(Elevator elev : gui.getElev().elevs) {
			if(elev.map.get(16) != null && elev.map.get(15) != null) {
				flag = true;
			}
		}
		assertFalse(flag);
		try {
			Thread.sleep(5*floorTiming);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test scheduler holds up state
	 */
	@Test
	void testSchedulerHoldsUpState() {
		GUI.elevatorPanels[5].buttons[1].doClick();
		try {
			Thread.sleep(3*floorTiming);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GUI.elevatorPanels[5].buttons[3].doClick();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(1,gui.getScheduler().elevtStates[5].getDir());
		try {
			Thread.sleep(3*floorTiming);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test scheduler holds down state
	 */
	@Test
	void testSchedulerHoldsDownState() {
		GUI.elevatorPanels[8].buttons[3].doClick();
		try {
			Thread.sleep(6*floorTiming);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GUI.elevatorPanels[8].buttons[0].doClick();
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(-1,gui.getScheduler().elevtStates[8].getDir());
		try {
			Thread.sleep(4*floorTiming);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test scheduler holds idle state
	 */
	@Test
	void testSchedulerHoldsIdleState() {
		assertEquals(0,gui.getScheduler().elevtStates[21].getDir());
	}
	
	/**
	 * test scheduler holds transient fault state
	 */
	@Test
	void testSchedulerHoldsTransientFaultState() {
		GUI.elevatorPanels[13].faultButton.doClick();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(gui.getScheduler().elevtStates[13].isStuck);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse(gui.getScheduler().elevtStates[13].isStuck);
	}
	
	/**
	 * test scheduler holds actual fualt state
	 */
	@Test
	void testSchedulerHoldsPermanentFaultState() {
		GUI.elevatorPanels[12].buttons[10].doClick();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GUI.elevatorPanels[12].faultButton.doClick();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(gui.getScheduler().elevtStates[12].isStuck);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(gui.getScheduler().elevtStates[12].isStuck);
	}
	
	/**
	 * test scheduler elevator message queue
	 */
	@Test
	void testSchedulerElevMessageQueue() {
		try {
			Scheduler scheduler = new Scheduler(3,4,true);
			byte[] data = Common.encodeFloor(1, false);
			scheduler.floorSubAddMsg(data);
			int[] msg = Common.decode(scheduler.msgToElevtSub.poll());
			boolean flag = false;
			for(int i = 0; i < 19; i++) {
				int closestElev = i;
				data = Common.encodeScheduler(closestElev, 1,0);
				int[] msg2 = Common.decode(data);
				if(msg[0] == msg2[0] && msg[1] == msg2[1] && msg[2] == msg2[2]) {
					flag = true;
				}
			}
			assertTrue(flag);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test scheduler floor message queue
	 */
	@Test 
	void testSchedulerFloorMessageQueue() {
		try {
			Scheduler scheduler = new Scheduler(3,4,true);
			byte[] data = Common.encodeElevator(1, 10,new Idle(), 10);
			scheduler.elevtSubAddMsg(data);
			data = Common.encodeScheduler(1, 10,0);
			int[] decodedData = Common.decode(data);
			int[] decodedMsg = Common.decode(scheduler.msgToFloorSub.poll());
			assertEquals(decodedData[0],decodedMsg[0]);
			assertEquals(decodedData[1],decodedMsg[1]);
			assertEquals(decodedData[2],decodedMsg[2]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * test elevator subsystem message queue for elevators
	 */
	@Test 
	void testElevatorSubMessageQueueElev() {
		try {
			ElevatorSubsystem elevSub = new ElevatorSubsystem(5,false,true);
			byte[] data = Common.encodeScheduler(1, 1, 1);
			elevSub.initElevatorsBuffer(1);
			elevSub.initElevatorsBuffer(2);
			elevSub.sendToElevator(data);
			assertEquals(data,elevSub.getMsgElevator(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * test elevator subsystem message queue for scheduler
	 */
	@Test 
	void testElevatorSubMessageQueueScheduler() {
		try {
			ElevatorSubsystem elevSub = new ElevatorSubsystem(5,false,true);
			byte[] data = Common.encodeElevator(1, 1, new Idle(), 1);
			elevSub.sendToScheduler(data);
			assertEquals(data,elevSub.getMsgScheduler());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test elevator subsystem deletes message from queue once gotten scheduler
	 */
	@Test
	void testElevatorSubMessageQueueDeletesScheduler() {
		try {
			ElevatorSubsystem elevSub = new ElevatorSubsystem(5,false,true);
			byte[] data = Common.encodeElevator(1, 1, new Idle(), 1);
			elevSub.sendToScheduler(data);
			assertEquals(data,elevSub.getMsgScheduler());
			assertNotEquals(data,elevSub.getMsgScheduler());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test elevator subsystem deletes message from queue once gotten elevator
	 */
	@Test
	void testElevatorSubMessageQueueDeletesElevator() {
		try {
			ElevatorSubsystem elevSub = new ElevatorSubsystem(5,false,true);
			byte[] data = Common.encodeScheduler(1, 1, 1);
			elevSub.initElevatorsBuffer(1);
			elevSub.initElevatorsBuffer(2);
			elevSub.sendToElevator(data);
			assertEquals(data,elevSub.getMsgElevator(1));
			assertNotEquals(data,elevSub.getMsgElevator(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test elevator subsystem adds to the proper queue
	 */
	@Test
	void testElevatorSubMessageQueueElevatorProperQueue() {
		try {
			ElevatorSubsystem elevSub = new ElevatorSubsystem(5,false,true);
			byte[] data = Common.encodeScheduler(1, 1, 1);
			elevSub.initElevatorsBuffer(1);
			elevSub.initElevatorsBuffer(2);
			elevSub.sendToElevator(data);
			assertNotEquals(data,elevSub.getMsgElevator(2));
			assertEquals(data,elevSub.getMsgElevator(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * test helper class turns buttons off and on
	 */
	@Test
	void testHelper() {
		for(ElevatorPanel elev : GUI.elevatorPanels) {
			Helper.turnAllButtonsOff(elev.buttons);
			for(JButton button : elev.buttons) {
				assertFalse(button.isEnabled());
			}
			Helper.turnAllButtonsOn(elev.buttons);
			for(JButton button : elev.buttons) {
				assertTrue(button.isEnabled());
			}
		}
	}
}
