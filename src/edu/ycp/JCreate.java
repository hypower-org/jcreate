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

import edu.ycp.JCreateModePacket.ModeCommand;
import edu.ycp.JCreateStartPacket.StartCommand;
import edu.ycp.comm.SerialPortManager;

public class JCreate {
	
	private SerialPortManager serialPortMgr;
	private JCreateMode currMode;
	
	public enum JCreateMode {
		OFF, PASSIVE, SAFE, FULL;
	}
	
	public enum JCreateSensorMode {
		REQUEST, SET_FREQ;
	}

	public JCreate(String serialPortName){

		initialize(serialPortName);
		currMode = JCreateMode.OFF;
		
	}

	private final void initialize(String serialPortName){
		serialPortMgr = new SerialPortManager(serialPortName);
	}
	
	public JCreateMode getCurrMode() {
		return currMode;
	}

	/*
	 * JCreate APIs for changing sensor data mode, i.e. request or set frequency updates
	 */
	
	/*
	 * JCreate APIs for robot interaction
	 */
	public final void drive(){
		
	}
	
	public final void driveDirect(){
		
	}
	
	public final void toggleLEDs(){
		
	}
	
	public final void getAllSensorData(){
		
	}
	
	/**
	 * Function that puts the Create in Safe mode. Must be in Passive or Full mode to do so.
	 */
	public final void changeToSafeMode(){
		if(currMode.equals(JCreateMode.PASSIVE) || currMode.equals(JCreateMode.FULL)){
			serialPortMgr.writeBuffer(JCreateModePacket.generateCommand(ModeCommand.SAFE));
			currMode = JCreateMode.SAFE;
		}
		
	}
	
	/** 
	 * Function that puts the Create in Full mode. Must be in Passive or Safe mode to do so.
	 */
	public final void changeToFullMode(){
		if(currMode.equals(JCreateMode.PASSIVE) || currMode.equals(JCreateMode.SAFE)){
			serialPortMgr.writeBuffer(JCreateModePacket.generateCommand(ModeCommand.FULL));
			currMode = JCreateMode.FULL;
		}
		
	}
	
	public final JCreateMode checkMode(){
		return JCreateMode.OFF;
	}
	
	
	public final void disconnectCreate(){
		serialPortMgr.disconnectSerial();	
	}
	
	public final void sendStart(){
		
		serialPortMgr.writeBuffer(JCreateStartPacket.generateCommand(StartCommand.START));
		currMode = JCreateMode.PASSIVE;
	}
	
	public static void main(String[] args){
		
		System.out.println("Start a new JCreate:");
		JCreate jc = new JCreate("/dev/ttyUSB0");
		
		jc.sendStart();
		System.out.println(jc.getCurrMode());
		
		jc.disconnectCreate();
		
	}
}
