/**
 * 
 */
package common;

import ElevatorSubsystem.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.util.Random;
import java.util.Scanner;

/**
 * @author jcwha
 *
 */
public class Common {

	private static boolean initialized = false;
	public static int ELEVATORS;
	public static int FLOORS;
	public static double SPEED;
	// Port numbers
	public static int ELEV_RECV_PORT;
	public static int ELEV_SUB_ELEV_RECV_PORT;
	public static int FLOOR_SUB_RECV_PORT;
	public static int SCHEDULER_RECV_FLOOR_PORT;
	public static int ELEV_SUB_RECV_PORT;
	public static int SCHEDULER_RECV_ELEV_PORT;
	// GUI Ports
	public static int SCHEDULER_RECV_GUI_PORT;
	public static int GUI_RECV_SCHEDULER_PORT;


	public static void initializeVars() {
		// If already initialized, abort.
		if(initialized) return;

		try {
			Scanner scanner = new Scanner(new File("src/test/settings.txt"));
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] splitStr = line.trim().split("\\s+");

				if (splitStr.length == 1 || line.contains("/")){
					// Empty line or Comment in setting file.
					continue;
				}

				// Get value
				int value = splitStr[1].contains(".") ? 0 : Integer.parseInt(splitStr[1]);
				// Assign value to its according variable
				switch(splitStr[0].trim()){
					case "ELEVATORS:" 	-> ELEVATORS 	= value;
					case "FLOORS:"		-> FLOORS 		= value;
					case "SPEED:"		-> SPEED 		= Double.parseDouble(splitStr[1]);
					// Ports
					case "ELEV_RECV_PORT:" 				-> ELEV_RECV_PORT 				= value;
					case "ELEV_SUB_ELEV_RECV_PORT:" 	-> ELEV_SUB_ELEV_RECV_PORT 		= value;
					case "FLOOR_SUB_RECV_PORT:" 		-> FLOOR_SUB_RECV_PORT 			= value;
					case "SCHEDULER_RECV_FLOOR_PORT:" 	-> SCHEDULER_RECV_FLOOR_PORT 	= value;
					case "ELEV_SUB_RECV_PORT:" 			-> ELEV_SUB_RECV_PORT 			= value;
					case "SCHEDULER_RECV_ELEV_PORT:" 	-> SCHEDULER_RECV_ELEV_PORT 	= value;
					// GUI Ports
					case "GUI_RECV_SCHEDULER_PORT:"		-> GUI_RECV_SCHEDULER_PORT 		= value;
					case "SCHEDULER_RECV_GUI_PORT:"		-> SCHEDULER_RECV_GUI_PORT		= value;
					// Unsupported settings
					default -> {}
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Update init flag
		initialized = true;
	}


	/* Message types */
	public enum TYPE{
		INVALID			((byte) -1),
		ELEVATOR		((byte) 0),
		FLOOR			((byte) 1),
		SCHEDULER		((byte) 2),
		CONFIRMATION	((byte) 3),
		ELEV_ERROR		((byte) 4);

		// Enum initializer
		private final byte value;
		private TYPE(byte b){
			this.value = b;
		}

		// Determine which type the byte corresponds to
		public static TYPE findType(byte b){
			for (TYPE type: TYPE.values()){
				if(type.value == b) return type;
			}
			return INVALID;
		}
	}

	/* Confirmation messages types */
	public enum CONFIRMATION{
		INVALID			((byte) -1, "INVALID"),
		CHECK			((byte) 0, "Check request"),
		RECEIVED		((byte) 1, "Received"),
		NO_MSG			((byte) 2, "No new message"),
		END				((byte) 3, "End of file");

		// Enum initializer
		private final byte value;
		private final String name;
		private CONFIRMATION(byte b, String n){
			this.value = b;
			this.name = n;
		}

		// Determine which type the byte corresponds to
		public static CONFIRMATION findConfirmation(byte b){
			for (CONFIRMATION conf: CONFIRMATION.values()){
				if(conf.value == b) return conf;
			}
			return INVALID;
		}
	}

	/* Elevator error reporting messages types */
	public enum ELEV_ERROR{
		INVALID			((byte) -1, "Invalid"),
		UNKNOWN			((byte) 0, "Unknown"),
		STUCK			((byte) 1, "StuckBetween"),
		DOOR_OPEN		((byte) 2, "StuckOpen"),
		DOOR_CLOSE		((byte) 3, "StuckClose"),
		RECOVER			((byte) 4, "Recover");

		// Enum initializer
		private final byte value;
		private final String name;
		private byte elevNum;
		private byte curFloor;
		private byte destFloor;
		private byte dirFloor;

		private ELEV_ERROR(byte b, String n){
			this.value = b;
			this.name = n;
		}

		private byte[] encode(){
			byte[] msg = new byte[6];
			msg[0] = TYPE.ELEV_ERROR.value;
			msg[1] = value;
			msg[2] = elevNum;
			msg[3] = curFloor;
			msg[4] = destFloor;
			msg[5] = dirFloor;
			return msg;
		}

		// Determine which type the byte corresponds to
		public static ELEV_ERROR findError(byte b){
			for (ELEV_ERROR err: ELEV_ERROR.values()){
				if(err.value == b) return err;
			}
			return INVALID;
		}

		// Determine which type the byte corresponds to & init details.
		public static ELEV_ERROR decode(byte[] msg){
			ELEV_ERROR elevError = findError(msg[1]);
			elevError.elevNum 				= msg[2];
			elevError.curFloor 				= msg[3];
			elevError.destFloor 			= msg[4];
			elevError.dirFloor				= msg[5];
			return elevError;
		}

		// Decode the elev error msg to int[]
		// {elevNum, curFloor, destFloor, dirFloor}
		private static int[] decodeToInt(byte[] msg){
			int[] result = new int[4];
			result[0] 		= msg[2];
			result[1] 		= msg[3];
			result[2] 		= msg[4];
			result[3]		= msg[5];
			return result;
		}

		public static String randomError(){
			Random random = new Random();
			ELEV_ERROR possibleErrors[] = new ELEV_ERROR[]{STUCK, DOOR_OPEN, DOOR_CLOSE};
			int randomIndex = random.nextInt(possibleErrors.length);
			return possibleErrors[randomIndex].name;
		}
	}

	/**
	 * encode the data in the form 0(for elevator), 127(seperator),
	 * elevator number, 127(seperator),
	 * current floor, 127(seperator),
	 * 1(Up) or -1(Down) or 0(Idle), 127(seperator),
	 * destination floor, 127(end file)
	 * 
	 * @param curr
	 * @param state
	 * @param dest
	 * @return message to send
	 */
	public static byte[] encodeElevator(int elevatorNum, int curr, MotorState state, int dest) {
		byte[] msg = new byte[10];
		msg[0] = TYPE.ELEVATOR.value;
		msg[1] = (byte)127;
		msg[2] = (byte)elevatorNum;
		msg[3] = (byte)127;
		msg[4] = (byte)curr;
		msg[5] = (byte)127;
		if(state instanceof Up) {
			msg[6] = (byte)1;
		} else if (state instanceof Down) {
			msg[6] = (byte)-1;
		} else {
			msg[6] = (byte)0;
		}
		msg[7] = (byte)127;
		msg[8] = (byte)dest;
		msg[9] = (byte)127;
		return msg;
	}
	
	/**
	 * encode the data in the form: 1(floor message),127(seperator),
	 * floor clicked, 127(seperator),
	 * 0(down) or 1(up)
	 * 
	 * @param floor clicked
	 * @param dir true for up, false for down
	 * @return message to send
	 */
	public static byte[] encodeFloor(int floor, boolean dir) {
		byte[] msg = new byte[6];
		msg[0] = TYPE.FLOOR.value;
		msg[1] = (byte)127;
		msg[2] = (byte)floor;
		msg[3] = (byte)127;
		msg[4] = dir ? (byte) 1 : (byte) 0;
		msg[5]=(byte)127;
		return msg;
	}
	
	/**
	 * encode the data in the form: 2(for scheduler), 127(seperator),
	 * elevt#, 127(seperator),
	 * floor# (shared by floor and elevt), 127(seperator),
	 * floor button to dismiss 0(down) or 1(up), 127(seperator)
	 * 
	 * @param elevt, floor, dir
	 * @return message to send
	 */
	public static byte[] encodeScheduler(int elevt, int floor, int dir) {
		byte[] msg = new byte[8];
		msg[0] = TYPE.SCHEDULER.value;
		msg[1] = (byte)127;
		msg[2] = (byte)elevt;
		msg[3] = (byte)127;
		msg[4] = (byte)floor;
		msg[5] = (byte)127;
		msg[6] = (byte)dir;
		msg[7] = (byte)127;
		return msg;
	}
	
	/**
	 * encode confirmation message into byte[]
	 */
	public static byte[] encodeConfirmation(CONFIRMATION conf) {
		byte[] msg = new byte[2];
		msg[0] = TYPE.CONFIRMATION.value;
		msg[1] = conf.value;
		return msg;
	}

	/**
	 * encode elevator error message into byte[]
	 */
	public static byte[] encodeElevError(ELEV_ERROR err,
										 int elevNum,
										 int curFloor,
										 int destFloor,
										 boolean dirFloor) {
		ELEV_ERROR elevError 	= err;
		elevError.elevNum 		= (byte) elevNum;
		elevError.curFloor 		= (byte) curFloor;
		elevError.destFloor 	= (byte) destFloor;
		elevError.dirFloor		= (byte) (dirFloor ? 1 : 0);
		return err.encode();
	}

	/**
	 *
	 * @param msg byte[] of message
	 * @return TYPE that this message belongs to
	 */
	public static TYPE findType(byte[] msg){
		return TYPE.findType(msg[0]);
	}

	/**
	 * decode the byte array received by a subsystem. See specialized decode methods for return format
	 * 
	 * @param msg
	 * @return int[] of what was received
	 */
	public static int[] decode(byte[] msg) {
		switch(findType(msg)) {
			case ELEVATOR:
				return decodeElevator(msg);
			case FLOOR:
				return decodeFloor(msg);
			case SCHEDULER:
				return decodeScheduler(msg);
			case CONFIRMATION:
				// Please use findConfirmation(byte[] msg) to get its type.
				return new int[]{1};
			case ELEV_ERROR:
				// Please use findElevError(byte[] msg) to get its type.
				return ELEV_ERROR.decodeToInt(msg);
			default:
				return null;
		}
	}
	
	/**
	 * decode an elevator message. return in form of
	 * index 0: current floor,
	 * index 1: direction (1 = up,0=Idle,-1=down),
	 * index 2: destination floor
	 * 
	 * @param msg
	 * @return int[] containing the decoded data
	 */
	private static int[] decodeElevator(byte[] msg) {
		int[] decodedMsg = new int[4];
		decodedMsg[0] = (int)msg[2];
		decodedMsg[1] = (int)msg[4];
		decodedMsg[2] = (int)msg[6];
		decodedMsg[3] = (int)msg[8];
		return decodedMsg;
	}
	
	/**
	 * decode floor message. return in the form
	 * index 0: floor number,
	 * index 1: direction 0(down) or 1(up)
	 * 
	 * @param msg
	 * @return int[] containing decoded data
	 */
	private static int[] decodeFloor(byte[] msg) {
		int[] decodedMsg = new int[2];
		decodedMsg[0] = (int)msg[2];
		decodedMsg[1] = (int)msg[4];
		return decodedMsg;
	}
	
	/**
	 * decode scheduler message. return in the form
	 * index 0: elevator number,
	 * index 1: floor,
	 * index 2: direction
	 * 
	 * @param msg
	 * @return int[] containing decoded data
	 */
	private static int[] decodeScheduler(byte[] msg) {
		int[] decodedMsg = new int[3];
		decodedMsg[0] = (int)msg[2];
		decodedMsg[1] = (int)msg[4];
		decodedMsg[2] = (int)msg[6];
		return decodedMsg;
	}


	/**
	 *
	 * @param msg byte[] of message
	 * @return CONFIRMATION that this message belongs to
	 */
	public static CONFIRMATION findConfirmation(byte[] msg){
		return CONFIRMATION.findConfirmation(msg[1]);
	}

	/**
	 *
	 * @param msg byte[] of message
	 * @return ELEV_ERROR that this message belongs to
	 */
	public static ELEV_ERROR findElevError(byte[] msg){
		return ELEV_ERROR.decode(msg);
	}


	/**
	 * Print out the message to console from original byte[]
	 * @param msg
	 */
	public static void print(byte[] msg){

		int[] results = decode(msg);
		String text = "[" + LocalTime.now() + "] MSG ";

		switch (findType(msg)){
			case ELEVATOR -> text +=
					"from elevator #" + results[0] + ": " +
					"currently at floor " + results[1] +
					", going " + (results[2] == 1 ? "up" : results[2] == 0 ? "idle" : "down") +
					", destination floor " + results[3];

			case FLOOR -> text +=
					"from floor #" + results[0] + ": " +
					(results[1] == 1 ? "up" : "down") + " button pressed";

			case SCHEDULER -> text +=
					"from scheduler: " +
					"elevator #" + results[0] +
					", floor #" + results[1] +
					", dismiss " + (results[2] == 1 ? "up" : "down");

			case CONFIRMATION -> text +=
					"confirmation: " + findConfirmation(msg).name;

			case ELEV_ERROR -> text +=
					"elevator #" + results[0] + " error " + findElevError(msg).name +
					"currently at floor " + results[1] +
					", going " + (results[3] == 1 ? "up" : "down") +
					" to floor " + results[2];

			default ->
					text += "unknown source.";
		}

		System.out.println(text);
		System.out.println();
	}


	public static void main(String[] args){

		// How to use ELEV_ERROR msg?
		// For example: When the elevator Stuck/ Door Open/ Door Close
		// There are 2 ways of doing it:


		// First way: Manual
		// Create an ELEV_ERROR
		ELEV_ERROR errorMsg_1 = ELEV_ERROR.STUCK;
		// Fill in details of this error message:
		errorMsg_1.elevNum 		= 1;
		errorMsg_1.curFloor 	= 2;
		errorMsg_1.destFloor 	= 3;
		errorMsg_1.dirFloor		= 0;
		// Encode to byte array
		byte[] errorMsg_1_byte = errorMsg_1.encode();


		// Second way: Automatic
		// Let the encoder do the job
		byte[] errorMsg_2_byte = encodeElevError(ELEV_ERROR.DOOR_OPEN,
												4,
												5,
												6,
												true);


		/**
		 *    errorMsg_byte ---> UDP ---> errorMsg_byte
		 */


		// Change this to test the other encoding method:
		byte[] errorMsg_byte = errorMsg_1_byte;

		// Decode the message:

		// Check if the message type is ELEV_ERROR
		if (findType(errorMsg_byte) == TYPE.ELEV_ERROR){

			// Generate a new ELEV_ERROR that's identical to the one sent.
			ELEV_ERROR errorMsg_receive = ELEV_ERROR.decode(errorMsg_byte);

			// Determine the type of the error
			if (errorMsg_receive == ELEV_ERROR.STUCK){
				System.out.println("This is a STUCK error.");
			}
			if (errorMsg_receive == ELEV_ERROR.DOOR_OPEN){
				System.out.println("This is a DOOR_OPEN error.");
			}
			if (errorMsg_receive == ELEV_ERROR.DOOR_CLOSE){
				System.out.println("This is a DOOR_CLOSE error.");
			}

			// Get detail of the error.
			int elevNum 	= errorMsg_receive.elevNum;
			int curFloor 	= errorMsg_receive.curFloor;
			int destFloor	= errorMsg_receive.destFloor;
			int dirFloor	= errorMsg_receive.dirFloor;

			System.out.println("elevNum: " + elevNum +
								", curFloor: " + curFloor +
								", destFloor: " + destFloor +
								", dirFloor: " + dirFloor);

		}
	}
}
