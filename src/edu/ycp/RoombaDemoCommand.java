package edu.ycp;

/**
 * This enum organizes the possible opcodes for starting demos
 * on the iRobot create.
 * @author pjmartin
 *
 */
public enum RoombaDemoCommand {
	
	DEMO((byte) 136),
	COVER((byte) 135),
	COVER_AND_DOCK((byte) 143),
	SPOT((byte) 134);
	
	private final byte opcodeVal;

	RoombaDemoCommand(byte val){
		this.opcodeVal = val;
	}
	
	public byte getOpcodeVal(){
		return opcodeVal;
	}
}
