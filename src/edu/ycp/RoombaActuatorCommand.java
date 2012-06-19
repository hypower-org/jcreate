package edu.ycp;

/**
 * This enum organizes the possible opcodes for sending commands
 * for iRobot create actuators.
 * 
 * @author pjmartin
 *
 */
public enum RoombaActuatorCommand {
	
	DRIVE((byte) 137),
	DRIVE_DIRECT((byte) 145),
	LEDS((byte) 139),
	DOUT((byte) 147),
	PWM_LOWSIDE((byte) 144),
	LOWSIDE((byte) 138),
	SEND_IR((byte) 151),
	SONG((byte) 140),
	PLAY_SONG((byte) 141),
	SENSORS((byte) 142),
	QUERY_LIST((byte) 149),
	STREAM((byte) 148),
	PAUSE_RESUME_STREAM((byte) 150);
	
	private final byte opcodeVal;
	
	RoombaActuatorCommand(byte val){
		this.opcodeVal = val;
	}
	
	public byte getOpcodeVal(){
		return opcodeVal;
	}
	
}
