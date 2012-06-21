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

import edu.ycp.JCreateInputPacket.InputCommand;
import edu.ycp.JCreateModePacket.ModeCommand;
import edu.ycp.JCreateStartPacket.StartCommand;
import edu.ycp.comm.SerialPortManager;

/**
 * Main class for interacting with the Create robot using the OI specification.
 * 
 * @author profmartin
 *
 */
public class JCreate implements Runnable {
	
	private SerialPortManager serialPortMgr;
	private JCreateMode currMode;
	private JCreateSensorMode sensorMode;
	
	private boolean running;
	
	public enum JCreateMode {
		OFF, PASSIVE, SAFE, FULL;
	}
	
	public enum JCreateSensorMode {
		REQUEST, // request data individually from the Create robot  
		SENSOR_PUSH; // get a sensor stream pushed at some set frequency
	}

	public JCreate(String serialPortName, JCreateSensorMode sensorMode){

		initialize(serialPortName, sensorMode);
		
		this.currMode = JCreateMode.OFF;
		
	}

	private final void initialize(String serialPortName, JCreateSensorMode sensorMode){
		
		serialPortMgr = new SerialPortManager(serialPortName);
		//check to see how this Create robot will get sensor data
		if(sensorMode == JCreateSensorMode.SENSOR_PUSH){
			this.sensorMode = sensorMode;
			running = true;
		}
		else {
			running = false;
		}
	}
	
	public JCreateMode getCurrMode() {
		
		return currMode;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
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
		
		serialPortMgr.writeBuffer(JCreateInputPacket.generateCommand(InputCommand.SENSORS, JCreatePacketID.OI_MODE));
		
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
		JCreate jc = new JCreate("/dev/ttyUSB0", JCreateSensorMode.REQUEST);
		
		jc.sendStart();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		jc.checkMode();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		jc.disconnectCreate();
	}

}
