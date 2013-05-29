/*
 Copyright (c) 2013 - York College of Pennsylvania, Patrick J. Martin
 The MIT License
 See license.txt for details. 
*/

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