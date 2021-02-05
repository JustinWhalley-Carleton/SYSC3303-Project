package FloorSubsystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class FileLoader {
    private File instructionFile = new File("src/test/testFile.txt");
    private Scanner scanner;

    private boolean endOfFile;

    private String curLine;
    private String[] lineSplit;

    public FileLoader() throws Exception{
        try {
            scanner = new Scanner(instructionFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            endOfFile = true;
        }
        endOfFile = false;

        // Call nextLine() to load the first instruction
        this.nextLine();
    }

    private boolean hasNextInstruction(){
        return !endOfFile && scanner.hasNextLine();
    }

    private String readLine(){
        return endOfFile ? "" : scanner.nextLine();
    }

    public boolean nextLine() throws Exception {
        if (hasNextInstruction()){
            curLine = readLine();
            if(! curLine.equals("")){
                lineSplit = curLine.split(" ");
                if (lineSplit.length != 4){
                    throw new Exception("Instruction File format unsupported!");
                }
                return true;
            }
        }
        return false;
    }

    public LocalTime getTime(){
//        System.out.println(lineSplit[0]);
        return LocalTime.parse(lineSplit[0], DateTimeFormatter.ISO_TIME);
    }

    public int departFloor(){
        int result = 0;
        try{
            result = Integer.parseInt(lineSplit[1]);
        }catch(Exception e){
            System.out.println("ERROR: Parse instruction failed!");
        }
        return result;
    }

    public boolean requestUp() {
        return lineSplit[2].equals("Up");
    }

    public int destFloor(){
        int result = 0;
        try{
            result = Integer.parseInt(lineSplit[3]);
        }catch(Exception e){
            System.out.println("ERROR: Parse instruction failed!");
        }
        return result;
    }

    public String toString(){
        // print current line information
        return "User at floor " + departFloor() + " requested to move " +
                (requestUp() ? " UP " : "DOWN") + " to floor " +
                destFloor() + " @ " + getTime();
    }

}
