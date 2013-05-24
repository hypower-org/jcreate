package edu.ycp;

public enum ModeCommand {
	
	SAFE((byte) 131),
	FULL((byte) 132);
	
	private final byte opcodeVal;
	
	ModeCommand(byte val){
		this.opcodeVal = val;
	}
	
	public byte getOpcodeVal(){
		return opcodeVal;
	}
}
