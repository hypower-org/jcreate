package edu.ycp;

/**
 * This enum organizes the possible script commands for the iRobot
 * create.
 * 
 * @author pjmartin
 *
 */
public enum RoombaScriptCommand {

	SCRIPT((byte) 152),
	PLAY_SCRIPT((byte) 153),
	SHOW_SCRIPT((byte) 154);
	
	private final byte opcodeVal;
	
	RoombaScriptCommand(byte val){
		this.opcodeVal = val;
	}
	
	public byte getOpcodeVal(){
		return opcodeVal;
	}
}
