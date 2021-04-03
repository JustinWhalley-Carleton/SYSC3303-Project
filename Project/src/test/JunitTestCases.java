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
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.Scanner;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ElevatorSubsystem.*;
import FloorSubsystem.FileLoader;
import FloorSubsystem.GUIFileLoader;
import common.Common;
import Timer.TimerController;
import common.RPC;
import common.Common.ELEV_ERROR;

/**
 * @author jcwha
 *
 */
class JunitTestCases {
	static final int ROWS = 20;
	static final int FLOORS = 10;
	static final String UP = "Up";
	static final String DOWN = "Down";
	static final GUIFileLoader loader = new GUIFileLoader();
	/**
	 * common code to run before each test
	 */
	@BeforeEach
	void init() {
		System.out.println("Deleting");
		//GUIFileLoader.deleteFile();
	}
	
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
	
	static void createSettingsFile(int rows, int floors, int elevators, int speed) {
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
			writer.write("SPEED: "+speed+"\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static int[] readSettingsFile() {
		int[] data = new int[4];
		try {
			Scanner scanner = new Scanner(new File("src/test/settings.txt"));
			for (int i = 0; i < 4; i++) {
				String line = scanner.nextLine();
				String[] splitStr = line.trim().split("\\s+");
				if(splitStr[0].trim().equals("ELEVATORS:")) {
					data[0] = Integer.parseInt(splitStr[1]);
				} else if (splitStr[0].trim().equals("ROWS:")) {
					data[1] = Integer.parseInt(splitStr[1]);
				} else if (splitStr[0].trim().equals("FLOORS:")) {
					data[2] = Integer.parseInt(splitStr[1]);
				} else if (splitStr[0].trim().equals("SPEED:")) {
					data[3] = Integer.parseInt(splitStr[1]);
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
		createSettingsFile(10,5,15,3);
		int[] newData = readSettingsFile();
		assertEquals(newData[1], 10);
		createSettingsFile(prevData[1],prevData[2],prevData[0],prevData[3]);
	}
	
	/**
	 * test read from settings floors
	 */
	@Test
	void testReadSettingsFloors() {
		int[] prevData = readSettingsFile();
		createSettingsFile(10,5,15,3);
		int[] newData = readSettingsFile();
		assertEquals(newData[2], 5);
		createSettingsFile(prevData[1],prevData[2],prevData[0],prevData[3]);
	}
	
	/**
	 * test read from settings elevators
	 */
	@Test
	void testReadSettingsElevators() {
		int[] prevData = readSettingsFile();
		createSettingsFile(10,5,15,3);
		int[] newData = readSettingsFile();
		assertEquals(newData[0], 15);
		createSettingsFile(prevData[1],prevData[2],prevData[0],prevData[3]);
	}
	
	/**
	 * test read from settings speed
	 */
	@Test
	void testReadSettingsSpeed() {
		int[] prevData = readSettingsFile();
		createSettingsFile(10,5,15,3);
		int[] newData = readSettingsFile();
		assertEquals(newData[3], 3);
		createSettingsFile(prevData[1],prevData[2],prevData[0],prevData[3]);
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
		
	}
	
	/**
	 * test fault door closed
	 */
	@Test
	void testFaultDoorClosed() {
		
	}
	
	/**
	 * test fault recovered
	 */
	@Test 
	void testFaultRecovered() {
		
	}
	
	/**
	 * test floor rescheduled
	 */
	@Test
	void testFloorRescheduled() {
		
	}
	
	/**
	 * test floor not rescheduled
	 */
	@Test
	void testFloorNotRescheduled() {
		
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
		
	}
	
	/**
	 * test floor decrements in down state
	 */
	@Test
	void testDecrementCurFloor() {
		
	}
	
	/**
	 * test fault state
	 */
	@Test 
	void testFaultState() {
		
	}
	
	/**
	 * test time elapsed between floors
	 */
	@Test
	void testTimeElapsed() {
		
	}
	
	/**
	 * test scheduler get elevator messages
	 */
	@Test
	void testschedulerReceiveElev() {
		
	}
	
	/**
	 * test scheduler get floor messages
	 */
	@Test
	void testSchedulerReceiveFloor() {
		
	}
	
	/**
	 * test scheduler response to confirmation elev
	 */
	@Test
	void testSchedulerResponseConfirmElev() {
		
	}
	
	/**
	 * test scheduler response to confirmation floor
	 */
	@Test
	void testSchedulerResponseConfirmFloor() {
		
	}
	
	/**
	 * test scheduler response to regular message elev
	 */
	@Test
	void testSchedulerResponseMsgElev() {
		
	}
	
	/**
	 * test scheduler response to regular message floor
	 */
	@Test
	void testSchedulerResponseMsgFloor() {
		
	}
	
	/**
	 * test num elevators created
	 */
	@Test
	void testNumberOfElevatorsCreate() {
		
	}
	
	/**
	 * test ports used
	 */
	@Test 
	void testPortsUsed() {
		
	}
	
	/**
	 * test proper number of floors read
	 */
	@Test
	void testNumFloors() {
		
	}
	
	/**
	 * test read command floorsubsystem up
	 */
	@Test
	void testReadCommandFloorUp() {
		loader.writeToFile(1,5,1);
		LocalTime curTime = LocalTime.now();
		String[] floorCommand = loader.readLineFloor();
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
		loader.writeToFile(1,5,0);
		LocalTime curTime = LocalTime.now();
		String[] floorCommand = loader.readLineFloor();
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
		loader.writeToFile(1,5,0);
		loader.readLineFloor();
		String[] floorCommand = loader.readLineFloor();
		assertNull(floorCommand);
	}
	
	/**
	 * test read command elevatorSubsystem
	 */
	@Test
	void testReadCommandElev() {
		loader.writeToFile(2, 1, 10);
		assertTrue(loader.elevHasCommand(1));
		assertFalse(loader.elevHasCommand(2));
		assertNull(loader.getElevButton(3));
		assertEquals((Integer)10,loader.getElevButton(1)[0]);
	}
	
	/**
	 * test read command elevatorSubsystem multiple dest
	 */
	@Test
	void testReadCommandElevMultipleDest() {
		loader.writeToFile(2, 1, 10);
		loader.writeToFile(2, 1, 11);
		loader.writeToFile(2, 1, 12);
		assertTrue(loader.elevHasCommand(1));
		assertFalse(loader.elevHasCommand(2));
		Integer[] result = loader.getElevButton(1);
		System.out.println("\n\n\n\tVal1: "+result[0]+" Val2: "+result[1]+" Val3: " +result[2]+"\n\n\n");
		assertEquals((Integer)10,result[0]);
		assertEquals((Integer)11,result[1]);
		assertEquals((Integer)12,result[2]);
	}
	
	/**
	 * test read elev deletes command
	 */
	@Test
	void testElevCommandDeletesCommand() {
		loader.writeToFile(2, 1, 10);
		loader.writeToFile(2, 1, 11);
		loader.writeToFile(2, 1, 12);
		loader.getElevButton(1);
		assertNull(loader.getElevButton(1));
	}
	
	/**
	 * test read fault command
	 */
	@Test
	void testReadFaultCommand() {
		loader.writeToFile(0, 1, -1);
		assertFalse(loader.getFault(2));
		assertTrue(loader.getFault(1));
	}
	
	/**
	 * test delete command after read fault
	 */
	@Test
	void testDeleteFaultCommandAfterRead() {
		loader.writeToFile(0, 1, -1);
		loader.getFault(1);
		assertFalse(loader.getFault(1));
	}
	
	/**
	 * test proper command deleted after read
	 */
	@Test
	void testProperCommandDeletedAfterRead() {
		loader.writeToFile(0, 1, -1);
		loader.writeToFile(0, 2, -1);
		loader.writeToFile(1, 4, 1);
		loader.writeToFile(1, 5, 0);
		loader.writeToFile(2, 1, 5);
		loader.writeToFile(2, 4, 3);
		
		assertTrue(loader.getFault(1));
		assertTrue(loader.getFault(2));
		assertFalse(loader.getFault(1));
		assertFalse(loader.getFault(2));
		
		assertEquals(5,loader.getElevButton(1));
		assertEquals(3,loader.getElevButton(4));
		assertNull(loader.getElevButton(1));
		assertNull(loader.getElevButton(4));
		
		assertNotNull(loader.readLineFloor());
		assertNotNull(loader.readLineFloor());
		assertNull(loader.readLineFloor());
	}
	
	/**
	 * test elevator receive message
	 */
	@Test 
	void testElevReceive() {
		
	}
	
	/**
	 * test floor receive message
	 */
	@Test 
	void testFloorReceive() {
		
	}
	
	/**
	 * test receive elevSubsystem
	 */
	@Test
	void testReceiveElevSub() {
		
	}
	
	/**
	 * test send elevSub
	 */
	@Test
	void testSendElevSub() {
		
	}
	
	/**
	 * test bad floor input
	 */
	@Test
	void testBadFloorInput() {
		
	}
	
	/**
	 * test bad elev Input
	 */
	@Test
	void testBadElevInput() {
		
	}
	
	/**
	 * test elev button changes color when clicked
	 */
	@Test
	void testGUIButtonColorElev() {
		
	}
	
	/**
	 * test elev button disable once click
	 */
	@Test
	void testElevButtonDisabled() {
		
	}
	
	/**
	 * test elev button reenabled
	 */
	@Test
	void testElevButtonReenabled() {
		
	}
	
	/**
	 * test elev button color change back when reached
	 */
	@Test
	void testElevColorChangeArrival() {
		
	}
	
	/**
	 * test scroll panel gets text
	 */
	@Test
	void testPrintToScrollPanel() {
		
	}
	
	/**
	 * test write to gui command
	 */
	@Test 
	void testWriteToGUICommand() {
		
	}
	
	/**
	 * test get floor command 
	 */
	@Test
	void testGetFloorCommand() {
		
	}
	
	/**
	 * test get elev command
	 */
	@Test 
	void testGetElevCommand() {
		
	}
	
	/**
	 * test get fault command
	 */
	@Test
	void testGetFaultCommand() {
		
	}
	
	/**
	 * test number of buttons created Floor panel
	 */
	@Test 
	void testNumFloorsGUI() {
		
	}
	
	/**
	 * test number of buttons in elevator panel
	 */
	@Test
	void testNumFloorsInElevGUI() {
		
	}
	
	/**
	 * test number of elevators
	 */
	@Test
	void testNumElevsGUI() {
		
	}
	
	/**
	 * test get floor change in GUI
	 */
	@Test
	void testCurFloorLabelGUI() {
		
	}
	
	/**
	 * test get dest floor label GUI
	 */
	@Test
	void testDestFloorLabelGUI() {
		
	}
	
	/**
	 * test get fault state label GUI 
	 */
	@Test
	void testStateFaultLabelGUI() {
		
	}
	
	/**
	 * test get Idle state label GUI 
	 */
	@Test
	void testStateIdleLabelGUI() {
		
	}
	
	/**
	 * test get up state label GUI 
	 */
	@Test
	void testStateUpLabelGUI() {
		
	}
	
	/**
	 * test get down state label GUI 
	 */
	@Test
	void testStateDownLabelGUI() {
		
	}
	
	/**
	 * test floor button on up
	 */
	@Test
	void testFloorButtonOnUp() {
		
	}
	
	/**
	 * test floor button on down
	 */
	@Test
	void testFloorButtonOnDown() {
		
	}
	
	/**
	 * test floor button off up
	 */
	@Test
	void testFloorButtonOffUp() {
		
	}
	
	/**
	 * test floor button off down
	 */
	@Test
	void testFloorButtonOffDown() {
		
	}
	
	/**
	 * test elev button on
	 */
	@Test 
	void testElevButtonOn() {
		
	}
	
	/**
	 * test elev button off
	 */
	@Test
	void testElevButtonOff() {
		
	}
	
	/**
	 * test scheduler picks closeset elev using 1 elev path
	 */
	@Test
	void testSchedulerPicksClosestElev() {
		
	}
	
	/**
	 * test scheduler picks an alternate elev
	 */
	@Test
	void testSchedulerPicksNewClosestElev() {
		
	}
	
	/**
	 * test scheduler picks an alternate elev with direction
	 */
	@Test
	void testSchedulerPicksProperDirElev() {
		
	}
	
	/**
	 * test scheduler holds up state
	 */
	@Test
	void testSchedulerHoldsUpState() {
		
	}
	
	/**
	 * test scheduler holds down state
	 */
	@Test
	void testSchedulerHoldsDownState() {
		
	}
	
	/**
	 * test scheduler holds idle state
	 */
	@Test
	void testSchedulerHoldsIdleState() {
		
	}
	
	/**
	 * test scheduler holds transient fault state
	 */
	@Test
	void testSchedulerHoldsTransientFaultState() {
		
	}
	
	/**
	 * test scheduler holds actual fualt state
	 */
	@Test
	void testSchedulerHoldsPermanentFaultState() {
		
	}
	
	/**
	 * test scheduler elevator message queue
	 */
	@Test
	void testSchedulerElevMessageQueue() {
		
	}
	
	/**
	 * test scheduler floor message queue
	 */
	@Test 
	void testSchedulerFloorMessageQueue() {
		
	}
	
	
	/**
	 * test elevator subsystem message queue
	 */
	@Test 
	void testElevatorMessageQueue() {
		
	}
}
