package FloorSubsystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileLoader {
    private File instructionFile = new File("FloorSubsystem/timeTable.txt");
    private Scanner scanner;

    private boolean endOfFile;

    public FileLoader(){
        try {
            scanner = new Scanner(instructionFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            endOfFile = true;
        }
        endOfFile = false;
    }

    public boolean hasNextInstruction(){
        return !endOfFile && scanner.hasNextLine();
    }

    public String readLine(){
        return endOfFile ? "" : scanner.nextLine();
    }

}
