/**
 * 
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import org.junit.jupiter.api.Test;

import ElevatorSubsystem.*;
import FloorSubsystem.FileLoader;
import common.Common;

/**
 * @author jcwha
 *
 */
class JunitTestCases {
	static final int ROWS = 20;
	static final int FLOORS = 10;
	static final String UP = "Up";
	static final String DOWN = "Down";
	
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
	
	@Test
	void testEncodeFloorUp() {
		int floor = 5;
		boolean dir = true;
		byte[] msg = Common.encodeFloor(floor, dir);
		
		assertTrue(msg instanceof byte[], "message is an array of bytes");
		assertTrue(1==(int)msg[0], "Floor identifier correct");
		assertTrue(floor == (int)msg[2], "returns correct floor");
		assertTrue(0 == (int)msg[4], "returns correct direction");
	}
	
	@Test
	void testEncodeFloorDown() {
		int floor = 5;
		boolean dir = false;
		byte[] msg = Common.encodeFloor(floor, dir);
		
		assertTrue(msg instanceof byte[], "message is an array of bytes");
		assertTrue(1==(int)msg[0], "Floor identifier correct");
		assertTrue(floor == (int)msg[2], "returns correct floor");
		assertTrue(1 == (int)msg[4], "returns correct direction");
	}
	
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
	
	@Test
	void testDecodeFloorUp() {
		int floor = 5;
		boolean dir = true;
		byte[] msg = Common.encodeFloor(floor, dir);
		
		int[] val = Common.decode(msg);

		assertTrue(val instanceof int[], "message is an array of int");
		assertTrue(val.length == 2, "length of array is 2");
		assertTrue(floor == val[0], "returns the correct floor");
		assertTrue(0 == val[1], "returns the correct direction");
	}
	
	@Test
	void testDecodeFloorDown() {
		int floor = 5;
		boolean dir = false;
		byte[] msg = Common.encodeFloor(floor, dir);
		
		int[] val = Common.decode(msg);

		assertTrue(val instanceof int[], "message is an array of int");
		assertTrue(val.length == 2, "length of array is 2");
		assertTrue(floor == val[0], "returns the correct floor");
		assertTrue(1 == val[1], "returns the correct direction");
	}
	
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
	
	
}