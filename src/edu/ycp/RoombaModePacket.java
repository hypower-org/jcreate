package edu.ycp;

public class RoombaModePacket {

	
	
	public enum RoombaModeCommand {

		SAFE((byte) 131),
		FULL((byte) 132);
		
		private final byte opcodeVal;
		
		RoombaModeCommand(byte val){
			this.opcodeVal = val;
		}
		
		public byte getOpcodeVal(){
			return opcodeVal;
		}
	}
	
}
