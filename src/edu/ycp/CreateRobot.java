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
	final BlockingQueue<ByteBuffer> dataQueue;
	final BlockingQueue<ByteBuffer> commandQueue;
	final SensorDataParser dataParser;
	
	private boolean stopRequested;	
	private Thread mainThread;
	
	/*
	 * Private, volatile data members for outside access.
	 */
	private volatile boolean wheelDropLeft;
	private volatile boolean wheelDropRight;
	private volatile boolean casterDrop;
	private volatile boolean bumpLeft;
	private volatile boolean bumpRight;
	private volatile boolean advanceButtonPress;
	private volatile boolean playButtonPress;
	
	private volatile ChargingState currChargeState;
	
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
	
	private volatile float batteryVoltage;	// mV
	private volatile float batteryCurrent;	// mA
	private volatile float batteryCharge;
	private volatile int batteryTemp; // in C
	
	public enum CreateMode {
		OFF, PASSIVE, SAFE, FULL;
	}
	
	public enum ChargingState {
		NOT, RECOND, FULL, TRICKLE, WAITING, FAULT;
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
		
		dataQueue = new LinkedBlockingQueue<ByteBuffer>();
		commandQueue = new LinkedBlockingQueue<ByteBuffer>();
		
		hardwareManager = new CreateHardwareManager(serialPortName, updatePeriod, CreateMode.SAFE, dataQueue, commandQueue);
		while(!hardwareManager.isInitialized());
		
		// TODO: implement mode initialization
		currCreateMode = initMode;
		
		dataParser = new SensorDataParser();
		
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
		
		while(!stopRequested){
			try {
				Thread.sleep(MIN_UPDATE_PERIOD);
				// consume a ByteBuffer from the incoming queue
				ByteBuffer incomingBuf = this.dataQueue.take();
//				System.out.println(Thread.currentThread().getName() + " consumed incoming data.");
				
				// parse the sensor data
				this.dataParser.parseData(incomingBuf);
				if(dataParser.isDataBufReady()){
					// get it and populate the local variables!
					System.out.print("New sensor data ready!\n[");
					byte[] freshData = dataParser.getSensorDataBuffer().array();
					for(byte b : freshData){
						System.out.print(b + " ");
					}
					System.out.println("]");
					
					processData(freshData);
					
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				this.hardwareManager.requestStop();
				Thread.currentThread().interrupt();
				System.out.println("CreateRobot stopped.");
			}
		}
	}

	private final void processData(byte[] freshData) {

		// convert the bits to booleans based on location in the first byte
		this.bumpRight = ((freshData[0] & 0x01) != 0);
		this.bumpLeft = (( (freshData[0] >> 1) & 0x01) != 0);
		this.wheelDropRight = (( (freshData[0] >> 2) & 0x01) != 0);
		this.wheelDropLeft = (( (freshData[0] >> 3) & 0x01) != 0);
		this.casterDrop = (( (freshData[0] >> 4) & 0x01) != 0);
		
		// other boolean data values - bytes 1->6
		this.wall = (freshData[1] != 0);
		this.cliffLeft = (freshData[2] != 0);
		this.cliffLeftFront = (freshData[3] != 0);
		this.cliffRightFront = (freshData[4] != 0);
		this.cliffRight = (freshData[5] != 0);
		this.virtualWall = (freshData[6] != 0);
		
		// skipping low side driver for now...byte index 7
		// bytes 8and 9 unused		
		// IR byte not implemented...byte index 10
		// buttons- byte 11
		this.advanceButtonPress = ( ((freshData[11] >> 2) & 0x01) != 0 );
		this.playButtonPress = ((freshData[11] & 0x01) != 0);
		
		// convert the 2 bytes for distance/angle into a single int, then float
		int distanceInt = ((int) freshData[12]) << 4; // load high byte
		distanceInt |= ((int) freshData[13]);
		this.distance += (float) distanceInt; // accumulates the linear distance traveled		
		
		int angleInt = ((int) freshData[14]) << 4; // load high byte
		angleInt |= ((int) freshData[15]);
		this.angle += (float) angleInt; // accumulates the angle rotated

		//set charging state
		switch(freshData[16]){
		case 0:
			this.currChargeState = ChargingState.NOT;
			break;
		case 1:
			this.currChargeState = ChargingState.RECOND;
			break;
		case 2:
			this.currChargeState = ChargingState.FULL;
			break;
		case 3:
			this.currChargeState = ChargingState.TRICKLE;
			break;
		case 4:
			this.currChargeState = ChargingState.WAITING;
			break;
		case 5:
			this.currChargeState = ChargingState.FAULT;
			break;
		}
		
		// need to handle unsigned here! hmm...
		int voltInt = ((int) freshData[17]) << 8; // load high byte
		voltInt |= ((int) freshData[18]);
		this.batteryVoltage = (float) voltInt;
		
		int currentInt = ((int) freshData[19]) << 8;
		currentInt |= ((int) freshData[20]);
		this.batteryCurrent = (float) currentInt;
		
		this.batteryTemp = (int) freshData[21];
		
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
	
	public float getReqVelocity() {
		return reqVelocity;
	}

	public ChargingState getCurrChargeState() {
		return currChargeState;
	}

	public boolean isAdvanceButtonPress() {
		return advanceButtonPress;
	}

	public boolean isPlayButtonPress() {
		return playButtonPress;
	}

	public boolean isWheelDropLeft() {
		return wheelDropLeft;
	}

	public boolean isWheelDropRight() {
		return wheelDropRight;
	}

	public boolean isCasterDrop() {
		return casterDrop;
	}

	public boolean isBumpLeft() {
		return bumpLeft;
	}

	public boolean isBumpRight() {
		return bumpRight;
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

	public int getBatteryTemp() {
		return batteryTemp;
	}

	public final void requestStop() {		
		stopRequested = true;
		if(mainThread != null){
			mainThread.interrupt();
		}
	}

	public static void main(String[] args){
		
		byte test1 = 57;
		byte test2 = -42;
		int testInt = ((int)test1) << 8;
		
		System.out.println("Start a new CreateRobot:");
		CreateRobot robot = new CreateRobot("/dev/ttyUSB0", 400, CreateMode.FULL);
		int execCount = 0;
		
		while(execCount < 15){
			
			if(robot.isBumpRight()){
				System.out.println("Bumped right side!");
			}
			if(robot.isBumpLeft()){
				System.out.println("Bumped left side!");
			}
			if(robot.wheelDropRight){
				System.out.println("Wheel dropped right side!");
			}
			if(robot.wheelDropLeft){
				System.out.println("Wheel dropped left side!");
			}
			if(robot.casterDrop){
				System.out.println("Caster dropped!");
			}
			
			System.out.println("Charging state: " + robot.getCurrChargeState());
			System.out.println("Battery voltage: " + robot.getBatteryVoltage() + " mV");
			System.out.println("Battery current "+ robot.getBatteryCurrent() + " mA");
			System.out.println("Battery temp  "+ robot.getBatteryTemp() + " degrees");
			
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			execCount++;
		}
		robot.requestStop();
		
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		robot.requestStop();

	}

}
