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

public class JCreateInputPacket {
	
	public static ByteBuffer generateCommand(InputCommand inCmd, JCreatePacketID...pids){
		
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
		
		ByteBuffer bb = JCreateInputPacket.generateCommand(InputCommand.SENSORS, JCreatePacketID.OI_MODE);
		
		for(byte b : bb.array()){
			System.out.println(b);
		}
		
	}
	
}
