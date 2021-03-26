/**
 * 
 */
package common;

import ElevatorSubsystem.*;

/**
 * @author jcwha
 *
 */
public class Common {

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
		INVALID			((byte) -1),
		CHECK			((byte) 0),
		RECEIVED		((byte) 1),
		NO_MSG			((byte) 2);

		// Enum initializer
		private final byte value;
		private CONFIRMATION(byte b){ this.value = b; }

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
		INVALID			((byte) -1),
		UNKNOWN			((byte) 0),
		STUCK			((byte) 1),
		DOOR_OPEN		((byte) 2),
		DOOR_CLOSE		((byte) 3);

		// Enum initializer
		private final byte value;
		private byte elevNum;
		private byte curFloor;
		private byte destFloor;
		private byte dirFloor;

		private ELEV_ERROR(byte b){ this.value = b; }

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
			int[] result = new int[3];
			result[0] 		= msg[2];
			result[1] 		= msg[3];
			result[2] 		= msg[4];
			result[3]		= msg[5];
			return result;
		}
	}

	/**
	 * encode the data in the form 0(for elevator), 127(seperator), elevator number, 127(seperator), current floor, 127(seperator), 1(Up) or -1(Down) or 0(Idle), 127(seperator), destination floor, 127(end file)
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
	 * encode the data in the form: 1(floor message),127(seperator),floor clicked, 127(seperator), 0(down) or 1(up)
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
	 * encode the data in the form: 2(for scheduler), 127(seperator), elevt#, 127(seperator), floor# (shared by floor and elevt), 127(seperator), floor button to dismiss 0(down) or 1(up), 127(seperator)
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
	 * decode an elevator message. return in form of index 0: current floor, index 1: direction (1 = up,0=Idle,-1=down),index 2: destination floor
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
	 * decode floor message. return in the form index 0: floor number, index 1: direction 0(down) or 1(up)
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
	 * decode scheduler message. return in the form index 0: elevator number, index 1: floor, index 2: direction
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
