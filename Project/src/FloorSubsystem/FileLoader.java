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

    public boolean hasNextInstruction(){
        return !endOfFile && scanner.hasNextLine();
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
                    // Remove destination from destinationFloors
                    destinationFloors.remove(destination);
                }
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
                if (lineSplit.length != 4){
                    throw new Exception("Instruction File format unsupported!");
                }
                // Update destinations
                pushDestinations();
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

    // toString() override
    public String toString(){
        // print current line information
        return "User at floor " + departFloor() + " requested to move " +
                (requestUp() ? " UP " : "DOWN") + " to floor " +
                destFloor() + " @ " + getTime();
    }

}
