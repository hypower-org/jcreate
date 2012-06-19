package edu.ycp;

import java.nio.ByteBuffer;

public class RoombaStartPacket {

	public static ByteBuffer generateCommand(StartCommand cmd, byte...baudCode){

		if(cmd == StartCommand.START){
			ByteBuffer outBuf = ByteBuffer.allocate(1);
			outBuf.put(StartCommand.START.getOpcodeVal());
			return outBuf;
		}
		else{
			ByteBuffer outBuf = ByteBuffer.allocate(2);
			outBuf.put(StartCommand.BAUD.getOpcodeVal());
			outBuf.put(baudCode[0]);
			return outBuf;
		}
		
	}
	
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
	
	public static void main(String[] args){
		ByteBuffer testBuf1 = RoombaStartPacket.generateCommand(StartCommand.START);
		ByteBuffer testBuf2 = RoombaStartPacket.generateCommand(StartCommand.BAUD, (byte)10);
		
		System.out.println("Test buffer 1: ");
		for(byte b : testBuf1.array()){
			System.out.print((int) (b & 0xFF) + " ");
		}
		
		System.out.println("\nTest buffer 2: ");
		for(byte b : testBuf2.array()){
			System.out.print((int) (b & 0xFF)  + " ");
		}
		
		
	}
}
