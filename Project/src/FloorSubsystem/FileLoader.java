package FloorSubsystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class FileLoader {
	private File instructionFile = new File("src/test/testFile.txt");
	private Scanner scanner;

	private boolean endOfFile;

	private String curLine;
	private String[] lineSplit;

	// Saves destination floors for each departure floor
	HashMap<Integer, ArrayList<Integer>> destinations;

	public FileLoader() throws Exception{
		try {
			scanner = new Scanner(instructionFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			endOfFile = true;
		}
		endOfFile = false;

		// Initialize destinations
		destinations = new HashMap<Integer, ArrayList<Integer>>();

		// Call nextLine() to load the first instruction
		this.nextLine();
	}

	public FileLoader(String fileName, boolean isInstruction) throws Exception{
		instructionFile = new File("src/test/"+fileName);
		try {
			scanner = new Scanner(instructionFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			endOfFile = true;
		}
		endOfFile = false;

		destinations = new HashMap<Integer, ArrayList<Integer>>();
		// Call nextLine() to load the first instruction
		if (isInstruction) this.nextLine();
	}
    
    public boolean hasNextInstruction(){
        // When EOF reached, no more instructions to read.
        if(endOfFile) return false;

        endOfFile = !scanner.hasNextLine();
        return !endOfFile;
    }

	// Add curLine instruction into destinations,
	// should only be called by nextLine()
	private void pushDestinations(){
		if (!destinations.containsKey(departFloor())){
			// create new arraylist on demand
			destinations.put(departFloor(), new ArrayList<Integer>());
		}
		// add to arraylist
		destinations.get(departFloor()).add(destFloor());
	}

	// Get destination floors for a specific floor
	// should only be called by elevator when the elevator arrived on demand
	public Integer[] popDestinations(int departFloor, boolean goingUp){
		if(destinations.containsKey(departFloor)){
			ArrayList<Integer> destinationFloors = destinations.get(departFloor);
			ArrayList<Integer> output = new ArrayList<Integer>();

			for (int destination: destinationFloors){
				if((goingUp && destination > departFloor) ||
						(!goingUp && destination < departFloor)){
					// Add destination to output
					output.add(destination);
				}
			}

			for (int destination: output){
				// Remove destination from destinationFloors
				destinationFloors.remove(Integer.valueOf(destination));
			}

			// if destinationFloors is empty, cleanup
			if (destinationFloors.size() == 0){
				destinations.remove(departFloor);
			}

			return output.toArray(new Integer[0]);
		}
		// return empty int[] if no destination.
		return new Integer[0];
	}

	// Returns a new line of the instruction file
	private String readLine(){
		return endOfFile ? "" : scanner.nextLine();
	}

	// Public method to switch to next instruction
	public boolean nextLine() throws Exception {
		if (hasNextInstruction()){
			curLine = readLine();
			if(! curLine.equals("")){
				// Split line into 4 segments: Time, Departure floor, Direction, Destination floor
				lineSplit = curLine.split(" ");
				if (lineSplit.length != 3 || lineSplit.length != 4){
					throw new Exception("Instruction File format unsupported!");
				}
				else if (lineSplit.length == 4) {
					// Update destinations
					pushDestinations();
					return true;
				}
				else if (lineSplit.length == 3) {
					
				}
			}
		}
		return false;
	}

	public boolean nextLineErr() throws Exception {
		if (hasNextInstruction()){
			curLine = readLine();
			if(! curLine.equals("")){
				// Split line into 4 segments: Time, Departure floor, Direction, Destination floor
				lineSplit = curLine.split(" ");
				if (lineSplit.length != 3){
					throw new Exception("Error File format unsupported!");
				}
				return true;
			}
		}
		return false;
	}

	// Public method to get time of current instruction
	public LocalTime getTime(){
		return LocalTime.parse(lineSplit[0], DateTimeFormatter.ISO_TIME);
	}

	// Public method to get departure floor of current instruction
	public int departFloor(){
		int result = 0;
		try{
			result = Integer.parseInt(lineSplit[1]);
		}catch(Exception e){
			System.out.println("ERROR: Parse instruction failed!");
		}
		return result;
	}

	// Public method to get request direction of current instruction
	public boolean requestUp() {
		return lineSplit[2].equals("Up");
	}

	// Public method to get destination floor of current instruction
	public int destFloor(){
		int result = 0;
		try{
			result = Integer.parseInt(lineSplit[3]);
		}catch(Exception e){
			System.out.println("ERROR: Parse instruction failed!");
		}
		return result;
	}

	// Method to read the generated errors for elevators
	public static String errorFileReader (int elevNum) throws FileNotFoundException {
		String output = null;
		File errorFile = new File ("src/test/errorFile.txt");   //file location and name
		Boolean errorFound = false;  //boolean status of file search

		Scanner scanner = new Scanner (errorFile);

		while (scanner.hasNextLine() && errorFound == false) {  //while elevator has not been found
			//output = null;
			String line = scanner.nextLine();
			String[] currentLine = line.split(" ");   //stores each column in separate index of this string[]

			if (currentLine.length == 3) {

				int num = Integer.parseInt(currentLine[1]);  //store the file's elevator number

				if (num == elevNum) {  //if the current line contains the same elevNum as requested
					errorFound = true;    // possible error has been found

					if (LocalTime.now().isAfter(LocalTime.parse(currentLine[0]))) {   // if error time has past
						output = currentLine[2];    // set up output to be the error code
					}
					else {
						output = null;
					}
				}

			}

		}
		scanner.close();
		return output;  // return possible error code
	}

	// toString() override
	public String toString(){
		// print current line information
		return "User at floor " + departFloor() + " requested to move " +
		(requestUp() ? " UP " : "DOWN") + " to floor " +
		destFloor() + " @ " + getTime();
	}


	public static void main(String[] args) throws Exception {
		FileLoader fileLoader = new FileLoader();

		fileLoader.nextLine();

		Integer[] destinations = fileLoader.popDestinations(19, false);

		for (int destination: destinations) {
			System.out.println(destination);
		}
	}

}
