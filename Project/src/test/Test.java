/**
 * 
 */
package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import ElevatorSubsystem.ElevatorSubsystem;
import Scheduler.Scheduler;
import FloorSubsystem.FloorSubSystem;
import common.Common;
//import ElevatorSubsystem.ElevatorSubsystem;
/**
 * Create a file of commands to test the system
 * 
 * @author Justin Whalley
 *
 */
public class Test {
	// Setting
	private File instructionFile = new File("src/test/settings.txt");
	// Constants
	private static int ROWS;
	private static int ELEVATORS;
	private static int ELEV_ERR;
	public static int FLOORS;
	public static int SPEED;
	// Port numbers
	public static int ELEV_RECV_PORT;
	public static int ELEV_SUB_ELEV_RECV_PORT;
	public static int FLOOR_SUB_RECV_PORT;
	public static int SCHEDULER_RECV_FLOOR_PORT;
	public static int ELEV_SUB_RECV_PORT;
	public static int SCHEDULER_RECV_ELEV_PORT;
	// Strings
	static final String UP = "Up";
	static final String DOWN = "Down";
	// Class vars
	private Random rand = new Random();
	private String[] time;
	private String[] dir;
	private int[] floor;
	private int[] carButton;
	private Thread floorSubsystem, elevatorSubsystem, scheduler;
	private String[] lines;
	
	/**
	 * constructor to create the file
	 */
	public Test(boolean GUI) throws Exception {
		if(!GUI) {
			readSettings();
			time = new String[ROWS];
			dir = new String[ROWS];
			floor = new int[ROWS];
			carButton = new int[ROWS];
			lines = new String[ROWS];
			createFile();
			scheduler = new Thread(new Scheduler(ELEVATORS,FLOORS));
			floorSubsystem = new Thread(new FloorSubSystem(FLOORS,false), "Producer");
			elevatorSubsystem = new Thread(new ElevatorSubsystem(ELEVATORS,false), "Consumer");
			floorSubsystem.start();
			elevatorSubsystem.start();
			scheduler.start();
		} 
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Test test = new Test(false);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * read the settings file and assign variables locally
	 */
	public void readSettings() {
		try {
			Scanner scanner = new Scanner(instructionFile);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] splitStr = line.trim().split("\\s+");

				// Get value
				int value = Integer.parseInt(splitStr[1]);
				// Assign value to its according variable
				switch(splitStr[0].trim()){
					case "ELEVATORS:" 	-> ELEVATORS 	= value;
					case "ROWS:"		-> ROWS 		= value;
					case "FLOORS:"		-> FLOORS 		= value;
					case "SPEED:"		-> SPEED 		= value;
					case "ELEV_ERR:"	-> ELEV_ERR		= value;
					// Ports
					case "ELEV_RECV_PORT:" 				-> ELEV_RECV_PORT 				= value;
					case "ELEV_SUB_ELEV_RECV_PORT:" 	-> ELEV_SUB_ELEV_RECV_PORT 		= value;
					case "FLOOR_SUB_RECV_PORT:" 		-> FLOOR_SUB_RECV_PORT 			= value;
					case "SCHEDULER_RECV_FLOOR_PORT:" 	-> SCHEDULER_RECV_FLOOR_PORT 	= value;
					case "ELEV_SUB_RECV_PORT:" 			-> ELEV_SUB_RECV_PORT 			= value;
					case "SCHEDULER_RECV_ELEV_PORT:" 	-> SCHEDULER_RECV_ELEV_PORT 	= value;
					default -> System.out.println("Unexpected item in settings file.");
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Generate pathname
	 * @param filename
	 * @return a File
	 */
	private File newFile(String filename) throws IOException {
		String path = System.getProperty("user.dir") +
				System.getProperty("file.separator") + "src" +
				System.getProperty("file.separator") + "test" +
				System.getProperty("file.separator") + filename;

		// create a new file. if file is already made, delete it and make a new one
		File file = new File(path);
		if(file.exists()) {
			file.delete();
			file = new File(path);
		}

		file.createNewFile();

		return file;
	}

	/**
	 * create the file and write to it
	 */
	public void createFile() throws IOException {
		// Local vars
		File file;
		FileWriter writer;

		// Test File
		file = newFile("testFile.txt");
		generateData();
		//write data to the file
		writer = new FileWriter(file.getAbsoluteFile());
		for(int i = 0; i < ROWS; i++) {
			String curLine = time[i] + " " + Integer.toString(floor[i]) + " " +
							  dir[i] + " " + Integer.toString(carButton[i]);
			lines[i] = curLine;
			writer.write(curLine + "\n");
		}
		writer.close();

		// Error File
		file = newFile("errorFile.txt");
		writer = new FileWriter(file.getAbsoluteFile());

		Integer errorIterations[] = generateError(ELEV_ERR, ROWS);
		Integer brokenElevators[] = generateError(ELEV_ERR, ELEVATORS);

		for(int i = 0; i < ELEV_ERR; ++i ){
			// Elev number = elevator that's gonna break (min = 1).
			Integer elevNum = brokenElevators[i] + 1;
			String errorType = Common.ELEV_ERROR.randomError();

			String curLine = time[errorIterations[i]] + " " + elevNum + " " + errorType;

			writer.write(curLine + "\n");
		}
		writer.close();
	}
	
	/**
	 * getter for the lines added to the test file. Used for testing
	 * 
	 * @return String[] of lines for the file
	 */
	public String[] getLines() {
		return lines;
	}
	/**
	 * generate the data for the file. Floor and carButton are random integers and the time increments by 30 seconds each time
	 */
	private void generateData() {
		LocalTime referenceTime = LocalTime.now().plus(10, ChronoUnit.SECONDS);
		for(int i = 0; i < ROWS; i++) {
			String temp = (referenceTime.plus(10*i/SPEED,ChronoUnit.SECONDS)).toString();
			time[i] = temp;
			floor[i] = rand.nextInt(FLOORS)+1;
			carButton[i] = rand.nextInt(FLOORS)+1;
			while(floor[i] == carButton[i]) {
				carButton[i] = rand.nextInt(FLOORS)+1;
			}
			if(floor[i] > carButton[i]) {
				dir[i] = DOWN;
			} else {
				dir[i] = UP;
			}
		}
	}

	/**
	 * Generate a sorted non-repeating Integer[]
	 * length = the length of the output array.
	 * range  = max value of random numbers in the output array (min = 0).
	 * Since generated values are non-repeating, length <= range.
	 * @return Integer[]
	 */
	private Integer[] generateError(int length, int range){
		ArrayList<Integer> possibilities = new ArrayList<Integer>();
		// All possibilities
		for(int i = 0; i < range; ++i){
			possibilities.add(i);
		}
		// Shuffle possibilities
		Collections.shuffle(possibilities);
		// Reduce amount of error as required
		List<Integer> reduced = possibilities.subList(0, length);
		Collections.sort(reduced);

		Integer[] errors = new Integer[length];
		return reduced.toArray(errors);
	}
}
