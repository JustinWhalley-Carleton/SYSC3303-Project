/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import ElevatorSubsystem.*;
import FloorSubsystem.FileLoader;
import common.Common;
import Timer.TimerController;
import common.RPC;

/**
 * @author jcwha
 *
 */
class JunitTestCases {
	static final int ROWS = 20;
	static final int FLOORS = 10;
	static final String UP = "Up";
	static final String DOWN = "Down";
	
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
	
	static void createSettingsFile(int rows, int floors, int elevators) {
		try {
			String path = System.getProperty("user.dir")+System.getProperty("file.separator")+"src"+System.getProperty("file.separator")+"test"+System.getProperty("file.separator")+"settings.txt";
			File file = new File(path);
			if(file.exists()) {
				file.delete();
				file = new File(path);
			}
			file.createNewFile();
			FileWriter writer = new FileWriter(file.getAbsoluteFile());
			writer.write("FLOORS: "+floors+"\n");
			writer.write("ROWS: "+rows+"\n");
			writer.write("ELEVATORS: "+elevators+"\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static int[] readSettingsFile() {
		int[] data = new int[3];
		try {
			Scanner scanner = new Scanner(new File("src/test/settings.txt"));
			for (int i = 0; i < 3; i++) {
				String line = scanner.nextLine();
				String[] splitStr = line.trim().split("\\s+");
				if(splitStr[0].trim().equals("ELEVATORS:")) {
					data[0] = Integer.parseInt(splitStr[1]);
				} else if (splitStr[0].trim().equals("ROWS:")) {
					data[1] = Integer.parseInt(splitStr[1]);
				} else if (splitStr[0].trim().equals("FLOORS:")) {
					data[2] = Integer.parseInt(splitStr[1]);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * test the fileloader to ensure lines read properly
	 */
	@Test
	void testFileReader() {
		String[] lines = createFile();
		try {
			FileLoader fileLoader = new FileLoader("JunitTestFile.txt");
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
		try {
			timer = new TimerController(2000, new Elevator(1,1,false,12,22));
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
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * test the timer completes 
	 */
	@Test
	void testTimerNoInterrupt() {
		TimerController timer;
		try {
			timer = new TimerController(1000, new Elevator(1,1,false,100,200));
			timer.start();
			assertTrue(timer.isRunning());
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assertFalse(timer.isRunning());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * test the timer stop will not trigger a start interrupt when not started
	 */
	@Test
	void testTimerNoStartStop() {
		TimerController timer;
		try {
			timer = new TimerController(2000, new Elevator(1,1,false,10,20));
			timer.stop();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assertFalse(timer.isRunning());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * test the timer double start will not trigger a stop interrupt
	 */
	@Test
	void testTimerDoubleStart() {
		TimerController timer;
		try {
			timer = new TimerController(2000, new Elevator(1,1,false,1,2));
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
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
		createSettingsFile(10,5,15);
		int[] newData = readSettingsFile();
		assertEquals(newData[1], 10);
		createSettingsFile(prevData[1],prevData[2],prevData[0]);
	}
	
	/**
	 * test read from settings floors
	 */
	@Test
	void testReadSettingsFloors() {
		int[] prevData = readSettingsFile();
		createSettingsFile(10,5,15);
		int[] newData = readSettingsFile();
		assertEquals(newData[2], 5);
		createSettingsFile(prevData[1],prevData[2],prevData[0]);
	}
	
	/**
	 * test read from settings elevators
	 */
	@Test
	void testReadSettingselevators() {
		int[] prevData = readSettingsFile();
		createSettingsFile(10,5,15);
		int[] newData = readSettingsFile();
		assertEquals(newData[0], 15);
		createSettingsFile(prevData[1],prevData[2],prevData[0]);
	}
	
	/**
	 * test common enum confirmation
	 */
	@Test
	void testEnumConfirmation() {
		byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.RECEIVED);
		Common.TYPE type = Common.findType(msg);
		assertEquals(type, Common.TYPE.CONFIRMATION);
	}
	
	/**
	 * test common enum confirmation check
	 */
	@Test
	void testEnumConfirmationCheck() {
		byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.CHECK);
		Common.TYPE type = Common.findType(msg);
		assertEquals(type, Common.TYPE.CONFIRMATION);
		Common.CONFIRMATION confirmation = Common.findConfirmation(msg);
		assertEquals(confirmation, Common.CONFIRMATION.CHECK);
	}
	
	/**
	 * test common enum confirmation received
	 */
	@Test
	void testEnumConfirmationReceived() {
		byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.RECEIVED);
		Common.TYPE type = Common.findType(msg);
		assertEquals(type, Common.TYPE.CONFIRMATION);
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
		assertEquals(type, Common.TYPE.CONFIRMATION);
		Common.CONFIRMATION confirmation = Common.findConfirmation(msg);
		assertEquals(confirmation, Common.CONFIRMATION.NO_MSG);
	}
	
	/**
	 * test common enum Scheduler
	 */
	@Test
	void testEnumScheduler() {
		byte[] msg = Common.encodeScheduler(1,2,3);
		Common.TYPE type = Common.findType(msg);
		assertEquals(type, Common.TYPE.SCHEDULER);
	}
	
	/**
	 * test common enum floor
	 */
	@Test
	void testEnumFloor() {
		byte[] msg = Common.encodeFloor(1,true);
		Common.TYPE type = Common.findType(msg);
		assertEquals(type, Common.TYPE.FLOOR);
	}
	
	/**
	 * test common enum elevator
	 */
	@Test
	void testEnumElevator() {
		byte[] msg = Common.encodeElevator(1,2,(MotorState)new Idle(),3);
		Common.TYPE type = Common.findType(msg);
		assertEquals(type, Common.TYPE.ELEVATOR);
	}
	
	/**
	 * test common enum invalid
	 */
	@Test
	void testEnumInvalid() {
		byte[] msg = Common.encodeConfirmation(Common.CONFIRMATION.INVALID);
		Common.TYPE type = Common.findType(msg);
		assertEquals(type, Common.TYPE.CONFIRMATION);
		Common.CONFIRMATION confirmation = Common.findConfirmation(msg);
		assertEquals(confirmation, Common.CONFIRMATION.INVALID);
	}
}
