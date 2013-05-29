package edu.ycp;

public enum InputCommand {
	
	SENSORS((byte) 142),
	QUERY_LIST((byte) 149),
	STREAM((byte) 148),
	PAUSE_RESUME_STREAM((byte) 150);
	
	private final byte opcodeVal;
	
	InputCommand(byte val){
		this.opcodeVal = val;
	}
	
	public byte getOpcodeVal(){
		return opcodeVal;
	}
}