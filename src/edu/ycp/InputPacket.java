/*
 Copyright (c) 2013 - York College of Pennsylvania, Patrick J. Martin
 The MIT License
 See license.txt for details. 
*/

package edu.ycp;

import java.nio.ByteBuffer;

public class InputPacket {
	
	public static ByteBuffer generateCommand(InputCommand inCmd, PacketID...pids){
		
		ByteBuffer outBuf;
		switch( inCmd ) {
			case SENSORS:
				outBuf = ByteBuffer.allocate(2);
				outBuf.put(inCmd.getOpcodeVal());
				outBuf.put(pids[0].getPacketID());
				break;
				
			case QUERY_LIST:
				outBuf = ByteBuffer.allocate(2);
				break;
				
			case STREAM:
				outBuf = ByteBuffer.allocate(2);
				break;
				
			case PAUSE_RESUME_STREAM:
				outBuf = ByteBuffer.allocate(2);
				break;
			default:
				outBuf = null;
				break;
		}
		return outBuf;
		
	}
	
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

	public static void main(String[] args){
		
		ByteBuffer bb = InputPacket.generateCommand(InputCommand.SENSORS, PacketID.OI_MODE);
		
		for(byte b : bb.array()){
			System.out.println(b);
		}
		
	}
	
}
