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
	private final int MIN_UPDATE_PERIOD = 30;	//note, OI docs say 15 ms - I use 30 ms just in case
		
	private final CreateHardwareManager hardwareManager;
	
	private boolean stopRequested;	
	private Thread mainThread;
	
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
	
	public enum CreateMode {
		OFF, PASSIVE, SAFE, FULL;
	}
	
	/**
	 * Creates a robot at serialPortName that updates local sensor data at the period of updatePeriod
	 * (in ms) with the robot operating in desired initMode. This class is the main interface to the underlying iRobot
	 * Create.
	 * 
	 * @param serialPortName
	 * @param updatePeriod
	 * @param initMode
	 */
	public CreateRobot(String serialPortName, int updatePeriod, CreateMode initMode){

		//TODO: consider making a factory!
		
		final BlockingQueue<ByteBuffer> dataQueue = new LinkedBlockingQueue<ByteBuffer>(10);
		final BlockingQueue<ByteBuffer> commandQueue = new LinkedBlockingQueue<ByteBuffer>(10);

		hardwareManager = new CreateHardwareManager(serialPortName, updatePeriod, CreateMode.SAFE, dataQueue, commandQueue);

		// TODO: implement mode initialization
		currCreateMode = CreateMode.OFF;
		
		startRobot();
		
	}

	private final void startRobot(){
		stopRequested = false;		
		this.mainThread = new Thread(this);
		mainThread.setName(CreateRobot.class.getSimpleName());
		mainThread.start();
	}
	
	@Override
	public void run() {
		
		//TODO: determine what needs to run at wakeup.
		while(!stopRequested){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public CreateMode getCurrCreateMode() {
		
		return currCreateMode;
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
	
	public final void setDigitalOuts(){
		
	}
	
	public final void setPWMLowSideDrivers(){
		
	}
	
	public final void setLowSideDrivers(){
		
	}
	
	public final void sendIR(){
		
	}
	
	//NOTE: songs not currently planned for implementation
	
	/**
	 * Function that puts the Create in Safe mode. Must be in Passive or Full mode to do so.
	 */
	public final void changeToSafeMode(){
	}
	
	/** 
	 * Function that puts the Create in Full mode. Must be in Passive or Safe mode to do so.
	 */
	public final void changeToFullMode(){
	}
	
	private final CreateMode checkMode(){		
		return this.currCreateMode;
	}
	
	
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
		
		

	}
}
