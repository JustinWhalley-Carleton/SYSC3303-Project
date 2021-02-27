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

	/**
	 * encode the data in the form 0(for elevator), 127(seperator), current floor, 127(seperator), 1(Up) or -1(Down) or 0(Idle), 127(seperator), destination floor, 127(end file)
	 * 
	 * @param curr
	 * @param state
	 * @param dest
	 * @return message to send
	 */
	public static byte[] encodeElevator(int curr, MotorState state, int dest) {
		byte[] msg = new byte[8];
		msg[0] = (byte)0;
		msg[1] = (byte)127;
		msg[2] = (byte)curr;
		msg[3] = (byte)127;
		if(state instanceof Up) {
			msg[4] = (byte)1;
		} else if (state instanceof Down) {
			msg[4] = (byte)-1;
		} else {
			msg[4] = (byte)0;
		}
		msg[5] = (byte)127;
		msg[6] = (byte)dest;
		msg[7] = (byte)127;
		return msg;
	}
	
	/**
	 * encode the data in the form: 1(floor message),127(seperator),floor clicked, 127(seperator), 0(up) or 1(down)
	 * 
	 * @param floor clicked
	 * @param true for up, false for down
	 * @return message to send
	 */
	public static byte[] encodeFloor(int floor, boolean dir) {
		byte[] msg = new byte[6];
		msg[0] = (byte)1;
		msg[1] = (byte)127;
		msg[2] = (byte)floor;
		msg[3] = (byte)127;
		if(dir) {
			msg[4]=(byte)0;
		} else {
			msg[4] = (byte)1;
		}
		msg[5]=(byte)127;
		return msg;
	}
	
	/**
	 * encode the data in the form: 2(for scheduler), 127(seperator), elevt#, 127(seperator), floor# (shared by floor and elevt), 127(seperator), floor button to dismiss 0(up) or 1(down), 127(seperator)
	 * 
	 * @param elevt, floor, dir
	 * @return message to send
	 */
	public static byte[] encodeScheduler(int elevt, int floor, int dir) {
		byte[] msg = new byte[8];
		msg[0] = (byte)2;
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
	 * decode the byte array received by a subsystem. See specialized decode methods for return format
	 * 
	 * @param msg
	 * @return int[] of what was received
	 */
	public static int[] decode(byte[] msg) {
		switch((int)msg[0]) {
			case 0:
				return decodeElevator(msg);
			case 1:
				return decodeFloor(msg);
			case 2:
				return decodeScheduler(msg);
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
		int[] decodedMsg = new int[3];
		decodedMsg[0] = (int)msg[2];
		decodedMsg[1] = (int)msg[4];
		decodedMsg[2] = (int)msg[6];
		return decodedMsg;
	}
	
	/**
	 * decode floor message. return in the form index 0: floor number, index 1: direction (0=up,-1=down)
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
	 * decode scheduler message. return in the form index 0: floor
	 * 
	 * @param msg
	 * @return int[] containing decoded data
	 */
	private static int[] decodeScheduler(byte[] msg) {
		int[] decodedMsg = new int[1];
		decodedMsg[0] = (int)2;
		return decodedMsg;
	}
}