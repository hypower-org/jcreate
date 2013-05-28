/*
 Copyright (c) 2013 - York College of Pennsylvania, Patrick J. Martin
 The MIT License
 See license.txt for details. 
*/

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
