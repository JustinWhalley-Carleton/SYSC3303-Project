/**
 * 
 */
package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * Create a file of commands to test the system
 * 
 * @author Justin Whalley
 *
 */
public class Test {

	static final int ROWS = 20;
	static final int FLOORS = 10;
	static final String UP = "Up";
	static final String DOWN = "Down";
	private Random rand = new Random();
	private String[] time = new String[ROWS];
	private String[] dir = new String[ROWS];
	private int[] floor = new int[ROWS];
	private int[] carButton = new int[ROWS];
	
	/**
	 * constructor to create the file
	 */
	public Test() {
		createFile();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test test = new Test();
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
				writer.write(time[i] + " " + Integer.toString(floor[i]) + " " + dir[i] + " " + Integer.toString(carButton[i])+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * generate the data for the file. Floor and carButton are random integers and the time increments by 30 seconds each time
	 */
	private void generateData() {
		LocalTime referenceTime = LocalTime.now().plus(1, ChronoUnit.MINUTES);
		for(int i = 0; i < ROWS; i++) {
			String temp = (referenceTime.plus(30*i,ChronoUnit.SECONDS)).toString();
			time[i] = temp.substring(0,temp.length() - 8);
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
	}
}
