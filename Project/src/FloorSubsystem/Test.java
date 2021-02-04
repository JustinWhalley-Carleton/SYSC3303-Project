package FloorSubsystem;

public class Test {

    public static void main(String[] args) throws Exception{
        FloorSubSystem floorSubSystem = new FloorSubSystem(7);

        do {
            System.out.println(floorSubSystem.instructionFile);
        } while(floorSubSystem.instructionFile.nextLine());

    }
}
