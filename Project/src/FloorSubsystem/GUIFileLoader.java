package FloorSubsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

public class GUIFileLoader {

	
	/**
	 * return first floor button command 
	 * @return String [] with [time,floor,direction], if no commnd for floor return null
	 */
	public static String[] readLineFloor() {
		//open command file
		File instructionFile = new File("src/test/GUICommands.txt");
		try {
			//create a scanner to read the file
			Scanner scanner = new Scanner(instructionFile);
			// if the file is empty return null
			if(!scanner.hasNextLine()) {
				scanner.close();
				return null;
			}
			//iterate through the file 
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				//if command is for floor 
				if(line.split(" ")[0].equals("1")) {
					// remove the type from return type and return the area
					String[] lineValues = line.split(" ");
					String[] returnArr = new String[lineValues.length-1];
					for(int i = 1; i < lineValues.length; i++) {
						returnArr[i-1] = lineValues[i];
					}
					scanner.close();
					// remove the command from file
					removeLine(line);
					return returnArr;
				}
			}
			// not found return null
			scanner.close();
			return null;
		} catch (FileNotFoundException e) {
			// return null if exception thrown
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * check if there is a fault for the elevator
	 * @param elevNum
	 * @return true if fault found, false otherwise
	 */
	public static boolean getFault(int elevNum) {
		//open command file
		File instructionFile = new File("src/test/GUICommands.txt");
		try {
			//create a scanner for the command file
			Scanner scanner = new Scanner(instructionFile);
			//if file is empty return no command found
			if(!scanner.hasNextLine()) {
				scanner.close();
				return false;
			}
			//iterate over the file
			while(scanner.hasNextLine()) {
				// if fault for elevator is found return true
				String line = scanner.nextLine();
				if(line.split(" ")[0].equals("0") && line.split(" ")[2].equals(Integer.toString(elevNum))) {
					System.out.println("\n\n\n"+line+"\n\n\n");
					scanner.close();
					removeLine(line);
					return true;
				}
			}
			//no command found return false
			scanner.close();
			return false;
		}catch(FileNotFoundException e) {
			//error so no command found
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * get elevator button command 
	 * @param elevNum
	 * @return int floor clicked, null if no command found
	 */
	public static Integer[] getElevButton(int elevNum)  {
		//open command file
		File instructionFile = new File("src/test/GUICommands.txt");
		try {
			ArrayList<String> list = new ArrayList<String>();
			//create scanner for command file
			Scanner scanner = new Scanner(instructionFile);
			//if file is empty return -1
			if(!scanner.hasNextLine()) {
				scanner.close();
				return null;
			}
			// iterate over the file
			while(scanner.hasNextLine()) {
				// if command found return destination floor
				String line = scanner.nextLine();
				if(line.split(" ")[0].equals("2") && line.split(" ")[2].equals(Integer.toString(elevNum))) {
					removeLine(line);
					list.add(line);
				}
			}
			if(list.size() == 0) {
				//no command found return -1
				scanner.close();
				return null;
			} 
			scanner.close();
			Integer[] result = new Integer[list.size()];
			int i = 0;
			while(list.size()!=0) {
				removeLine(list.get(0));
				String line = list.get(0);
				list.remove(0);
				result[i] = (Integer)Integer.parseInt(line.split(" ")[3]);
				i++;
			}
			
			return result;
		}catch(FileNotFoundException e) {
			// no command found return null
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * remove line containing msg from command file (Note the same command cannot appear twice in the file because of timestamp)
	 * @param msg
	 */
	private static void removeLine(String msg) {
		// open command file and a temperary file
		File instructionFile = new File("src/test/GUICommands.txt");
		File tempFile = new File("tmp.txt");
		
		try {
			// create a buffered reader for the command file
			BufferedReader reader = new BufferedReader(new FileReader(instructionFile));
			// create a buffered writer for the temperary file
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			String line;
			while((line = reader.readLine()) != null) {
				if(!line.trim().equals(msg)) {
					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			reader.close();
			if(!instructionFile.delete() ) {
				System.out.println("Error deleting file");
			}
			if(!tempFile.renameTo(instructionFile)) {
				System.out.println("Error renaming file");
			}
			
			/*
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			
			// iterate over all lines in the command file and copy every line that does not equal msg
			String curLine;
			while((curLine = reader.readLine())!=null) {
				String trimmed = curLine.trim();
				if(trimmed.equals(msg)) continue;
				writer.write(curLine+"\n");
			}
			writer.close();
			reader.close();
			// rename temparary file to the command file
			instructionFile.delete();
			tempFile.renameTo(instructionFile);
			*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static boolean elevHasCommand(int elevNum) {
		//open command file
		File instructionFile = new File("src/test/GUICommands.txt");
		try {
			ArrayList<String> list = new ArrayList<String>();
			//create scanner for command file
			Scanner scanner = new Scanner(instructionFile);
			//if file is empty return -1
			if(!scanner.hasNextLine()) {
				scanner.close();
				return false;
			}
			// iterate over the file
			while(scanner.hasNextLine()) {
				// if command found return destination floor
				String line = scanner.nextLine();
				if(line.split(" ")[0].equals("2") && line.split(" ")[2].equals(Integer.toString(elevNum))) {
					return true;
				}
			}
			scanner.close();
			return false;
		}catch(FileNotFoundException e) {
			// no command found return null
			e.printStackTrace();
			return false;
		}
	}
	
	public static void deleteFile() {
		File instructionFile = new File("src/test/GUICommands.txt");
		instructionFile.delete();
		try {
			instructionFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * write a line to the command file
	 * @param type
	 * @param index2
	 * @param index3
	 */
	public static void writeToFile(int type, int index2, int index3) {
		// open command file
		File instructionFile = new File("src/test/GUICommands.txt");
		try {
			// create the file if it does not exist
			instructionFile.createNewFile();
			// create writer to write to end of file
			FileWriter writer = new FileWriter(instructionFile.getAbsoluteFile(),true);
			BufferedWriter br = new BufferedWriter(writer);
			// write command to file depending on the type of command
			if(type == 0) {
				br.write(Integer.toString(type) + " " +LocalTime.now().toString()+" " + Integer.toString(index2)+"\n");
			} else if(type == 1) {
				br.write(Integer.toString(type) + " " +LocalTime.now().toString()+" " + Integer.toString(index2) + " " + (index3==1 ? "UP" : "DOWN")+"\n");
			} else if(type == 2) {
				br.write(Integer.toString(type) + " " +LocalTime.now().toString()+" " + Integer.toString(index2) + " " + Integer.toString(index3)+"\n");
			}
			br.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
