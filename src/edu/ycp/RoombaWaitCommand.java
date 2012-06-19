package edu.ycp;

/**
 * This enum organizes the possible wait commands for the iRobot
 * create.
 * 
 * @author pjmartin
 *
 */
public enum RoombaWaitCommand {

	WAIT_TIME((byte) 155),
	WAIT_DIST((byte) 156),
	WAIT_ANGLE((byte) 157),
	WAIT_EVENT((byte) 158);
	
	private final byte opcodeVal;
	
	RoombaWaitCommand(byte val){
		this.opcodeVal = val;
	}

	public byte getOpcodeVal() {
		return opcodeVal;
	}
	
	
}
