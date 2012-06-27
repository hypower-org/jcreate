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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.ycp.InputPacket.InputCommand;
import edu.ycp.ModePacket.ModeCommand;
import edu.ycp.StartPacket.StartCommand;
import edu.ycp.comm.CreateHardwareManager;

/**
 * Main class for interacting with the Create robot using the OI specification.
 * 
 * @author profmartin
 *
 */
public class CreateRobot implements Runnable {
	
	private CreateMode currCreateMode;
	private final int sensorUpdatePeriod;		// in ms!
	private final int MIN_UPDATE_PERIOD = 20;	//note, OI docs say 15 ms - I use 20 ms just in case
		
	private final CreateCommander commander;
	private final CreateHardwareManager serialPortManager;
	
	/*
	 * Private, volatile data members for outside access.
	 */
	private volatile float reqVelocity;
	private volatile float reqRadius;
	private volatile float reqRightVelocity;
	private volatile float reqLeftVelocity;
	
	private volatile float distance;	// distance traveled in mm since last request
	private volatile float angle;		// angle rotated in degrees since last request
	
	private volatile boolean wall;
	private volatile boolean cliffLeft;
	private volatile boolean cliffLeftFront;
	private volatile boolean cliffRightFront;
	private volatile boolean cliffRight;
	private volatile boolean virtualWall;
	
	private volatile float batteryVoltage;
	private volatile float batteryCurrent;
	private volatile float batteryCharge;
	 
	
	
	private boolean running;
	
	private Thread mainThread;
	
	public enum CreateMode {
		OFF, PASSIVE, SAFE, FULL;
	}
	/**
	 * Creates a robot at serialPortName that updates local sensor data at the period of updatePeriod
	 * (in ms) with the robot operating in desired initMode.
	 * 
	 * @param serialPortName
	 * @param updatePeriod
	 * @param initMode
	 */
	public CreateRobot(String serialPortName, int updatePeriod, CreateMode initMode){

		//TODO: consider making a factory!
		
		final BlockingQueue<ByteBuffer> dataQueue = new LinkedBlockingQueue<ByteBuffer>(10);
		final BlockingQueue<ByteBuffer> commandQueue = new LinkedBlockingQueue<ByteBuffer>(10);

		commander = new CreateCommander(dataQueue, commandQueue);
		serialPortManager = new CreateHardwareManager(serialPortName, 100, dataQueue, commandQueue);

		// check to make sure the update period is not less allowable by Create robot.
		if(updatePeriod < MIN_UPDATE_PERIOD){
			this.sensorUpdatePeriod = this.MIN_UPDATE_PERIOD;
		}
		else{
			this.sensorUpdatePeriod = updatePeriod;	
		}
		

		// attempt to change the mode to desired initMode
		// TODO: implement mode initialization
		currCreateMode = CreateMode.OFF;
		
	}

	public CreateMode getCurrCreateMode() {
		
		return currCreateMode;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * methods for robot actuation
	 */
	public final void drive(){
		
	}
	
	public final void driveDirect(){
		
	}
	
	public final void toggleLEDs(){
		
	}
	
	/**
	 * Function that puts the Create in Safe mode. Must be in Passive or Full mode to do so.
	 */
	public final void changeToSafeMode(){
		if(currCreateMode.equals(CreateMode.PASSIVE) || currCreateMode.equals(CreateMode.FULL)){
			
			// TODO: use new threading arch
			
			currCreateMode = CreateMode.SAFE;
		}
		
	}
	
	/** 
	 * Function that puts the Create in Full mode. Must be in Passive or Safe mode to do so.
	 */
	public final void changeToFullMode(){
		if(currCreateMode.equals(CreateMode.PASSIVE) || currCreateMode.equals(CreateMode.SAFE)){
			
			//TODO: use new threading arch
			
			currCreateMode = CreateMode.FULL;
		}
		
	}
	
	private final CreateMode checkMode(){		
		return this.currCreateMode;
	}
	
	
	public final void disconnectCreate(){
//		serialPortMgr.disconnectSerial();	
	}
	
//	public final void sendStart(){
//		
////		serialPortMgr.writeBuffer(JCreateStartPacket.generateCommand(StartCommand.START));
//		
//		//TODO: use new threading arch
//		
//		currCreateMode = CreateMode.PASSIVE;
//	}

	public float getReqVelocity() {
		return reqVelocity;
	}

	public float getReqRadius() {
		return reqRadius;
	}

	public float getReqRightVelocity() {
		return reqRightVelocity;
	}

	public float getReqLeftVelocity() {
		return reqLeftVelocity;
	}

	public float getDistance() {
		return distance;
	}

	public float getAngle() {
		return angle;
	}

	public boolean isWall() {
		return wall;
	}

	public boolean isCliffLeft() {
		return cliffLeft;
	}

	public boolean isCliffLeftFront() {
		return cliffLeftFront;
	}

	public boolean isCliffRightFront() {
		return cliffRightFront;
	}

	public boolean isCliffRight() {
		return cliffRight;
	}

	public boolean isVirtualWall() {
		return virtualWall;
	}

	public float getBatteryVoltage() {
		return batteryVoltage;
	}

	public float getBatteryCurrent() {
		return batteryCurrent;
	}

	public float getBatteryCharge() {
		return batteryCharge;
	}

	public boolean isRunning() {
		return running;
	}

	public static void main(String[] args){
		
		System.out.println("Start a new JCreate:");
		CreateRobot robot = new CreateRobot("/dev/ttyUSB0", 100, CreateMode.SAFE);
		
//		robot.sendStart();
//
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		jc.checkMode();
//		
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		jc.disconnectCreate();
	}
}
