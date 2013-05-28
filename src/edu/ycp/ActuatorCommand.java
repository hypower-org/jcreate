/*
 Copyright (c) 2013 - York College of Pennsylvania, Patrick J. Martin
 The MIT License
 See license.txt for details. 
*/

package edu.ycp;

public enum ActuatorCommand {

	DRIVE((byte) 137),
	DRIVE_DIRECT((byte) 145),
	LEDS((byte) 139),
	DOUT((byte) 147),
	PWM_LOWSIDE((byte) 144),
	LOWSIDE((byte) 138),
	SEND_IR((byte) 151);
	//NOTE: songs not currently planned for implementation
//	SONG((byte) 140),
//	PLAY_SONG((byte) 141);
	
	private final byte opcodeVal;
	
	ActuatorCommand(byte val){
		this.opcodeVal = val;
	}
	
	public byte getOpcodeVal(){
		return opcodeVal;
	}
}
