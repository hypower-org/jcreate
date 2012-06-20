/**

Copyright (c) 2012 Patrick Martin <prof.pmartin10@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

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
