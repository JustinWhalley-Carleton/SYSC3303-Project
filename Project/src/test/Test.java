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
import java.util.Random;
import java.util.Scanner;

import ElevatorSubsystem.ElevatorSubsystem;
import Scheduler.Scheduler;
import FloorSubsystem.FloorSubSystem;
//import ElevatorSubsystem.ElevatorSubsystem;
/**
 * Create a file of commands to test the system
 * 
 * @author Justin Whalley
 *
 */
public class Test {

	private File instructionFile = new File("src/test/settings.txt");
	private int ROWS;
	private int ELEVATORS;
	public static int FLOORS;
	public static int SPEED;
	static final String UP = "Up";
	static final String DOWN = "Down";
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
	public Test() throws Exception {
		readSettings();
		time = new String[ROWS];
		dir = new String[ROWS];
		floor = new int[ROWS];
		carButton = new int[ROWS];
		lines = new String[ROWS];
		createFile();
		scheduler = new Thread(new Scheduler(ELEVATORS,9));
		floorSubsystem = new Thread(new FloorSubSystem(FLOORS), "Producer");
		elevatorSubsystem = new Thread(new ElevatorSubsystem(ELEVATORS), "Consumer");
		floorSubsystem.start();
		elevatorSubsystem.start();
		scheduler.start();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Test test = new Test();
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
			for (int i = 0; i < 4; i++) {
				String line = scanner.nextLine();
				String[] splitStr = line.trim().split("\\s+");
				if(splitStr[0].trim().equals("ELEVATORS:")) {
					this.ELEVATORS = Integer.parseInt(splitStr[1]);
				} else if (splitStr[0].trim().equals("ROWS:")) {
					this.ROWS = Integer.parseInt(splitStr[1]);
				} else if (splitStr[0].trim().equals("FLOORS:")) {
					this.FLOORS = Integer.parseInt(splitStr[1]);
				} else if (splitStr[0].trim().equals("SPEED:")) {
					SPEED = Integer.parseInt(splitStr[1]);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * create the file and write to it
	 */
	public void createFile() {
		try {
			//get the path to file
			String path = System.getProperty("user.dir")+System.getProperty("file.separator")+"src"+System.getProperty("file.separator")+"test"+System.getProperty("file.separator")+"testFile.txt";
			// create a new file. if file is already made, delete it and make a new one
			File file = new File(path);
			if(file.exists()) {
				file.delete();
				file = new File(path);
			}
			file.createNewFile();
			
			generateData();
			
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
			floor[i] = rand.nextInt(9)+1;
			carButton[i] = rand.nextInt(9)+1;
			while(floor[i] == carButton[i]) {
				carButton[i] = rand.nextInt(9)+1;
			}
			if(floor[i] > carButton[i]) {
				dir[i] = DOWN;
			} else {
				dir[i] = UP;
			}
		}
	}
}
