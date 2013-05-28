/*
 Copyright (c) 2013 - York College of Pennsylvania, Patrick J. Martin
 The MIT License
 See license.txt for details. 
*/

package edu.ycp;

import java.nio.ByteBuffer;

public class ModePacket {

	public static ByteBuffer generateCommand(ModeCommand cmd){
		ByteBuffer outBuf = ByteBuffer.allocate(1);
		outBuf.put(cmd.getOpcodeVal());
		return outBuf;
	}
	
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
	
}
