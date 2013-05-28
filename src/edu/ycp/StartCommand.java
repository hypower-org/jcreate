/*
 Copyright (c) 2013 - York College of Pennsylvania, Patrick J. Martin
 The MIT License
 See license.txt for details. 
*/

package edu.ycp;

public enum StartCommand {
	START((byte) 128),
	BAUD((byte) 129);
	
	private final byte opcodeVal;
	
	StartCommand(byte val){
		this.opcodeVal = val;
	}
	
	public byte getOpcodeVal(){
		return opcodeVal;
	}
}
