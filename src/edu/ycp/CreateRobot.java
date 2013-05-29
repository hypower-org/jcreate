/*
 Copyright (c) 2013 - York College of Pennsylvania, Patrick J. Martin
 The MIT License
 See license.txt for details. 
*/

package edu.ycp;

import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import edu.ycp.comm.CreateHardwareManager;

/**
 * Main class for interacting with the Create robot using the OI specification.
 * 
 * @author profmartin
 *
 */
public class CreateRobot {
	
	private CreateMode currCreateMode;
	private final int MIN_UPDATE_PERIOD = 30;	//note, OI docs say 15 ms - I use 30 ms just in case
	
	private final int MAX_CREATE_VEL = 500; // mm/s
	private final int MAX_CREATE_RAD = 2000; // mm
	
	private final float WHEELBASE = 266.7f; // in mm; 10.5 inches measured
	private final float WHEEL_RADIUS = 38.1f; // in mm; 1.5 inches measured
		
	private final CreateHardwareManager hardwareManager;
	final BlockingQueue<ByteBuffer> dataQueue;
	final BlockingQueue<ByteBuffer> commandQueue;
	final SensorDataParser dataParser;
	
	private volatile boolean robotStopRequested;	
	
	private final ExecutorService executor; // executor service for the data and command Q management
	private final Vector<Future<?>> tasks;
	
	
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
	
	private volatile float reqVelocity;			// in mm/s
	private volatile float reqRadius;			// in mm
	private volatile float reqRightVelocity;	// in mm/s
	private volatile float reqLeftVelocity;		// in mm/s
	
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
	private volatile float batteryCharge;	// in mAh
	private volatile short batteryTemp; // in C
	private volatile float batteryCapacity; // in mAh
	
	private volatile int wallSignal;
	private volatile int cliffLeftSignal;
	private volatile int cliffLeftFrontSignal;
	private volatile int cliffRightFrontSignal;
	private volatile int cliffRightSignal;
	
	private volatile byte cargoDIN;
	private volatile int cargoAIN;
	
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
	public CreateRobot(String serialPortName, CreateMode initMode){

		dataQueue = new LinkedBlockingQueue<ByteBuffer>();
		commandQueue = new LinkedBlockingQueue<ByteBuffer>();
		
		hardwareManager = new CreateHardwareManager(serialPortName, initMode, dataQueue, commandQueue);
		while(!hardwareManager.isInitialized());
		
		// TODO: implement mode initialization
		currCreateMode = initMode;
		
		dataParser = new SensorDataParser();
		
		this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		robotStopRequested = false;		
		
		// create the command thread for issuing commands to the robot.
		Runnable cmdRunner = new Runnable(){

			@Override
			public void run() {
				// take from the command Q - it blocks if the queue is full
				while(true){
					
				//TODO: need to develop this!
					
//					try {
//						
//					} catch (InterruptedException e) {
//						System.err.println(Thread.currentThread().getName() + ": Command task stopped.");
//					}
				}
				
			}

			
		};
		Runnable dataRunner = new Runnable(){

			@Override
			public void run() {
				// take from the data Q - blocks if there is no command
				while(true){
					try{
						ByteBuffer incomingBuf = dataQueue.take();
						
						// parse the sensor data
						dataParser.parseData(incomingBuf);
						if(dataParser.isDataBufReady()){
							// get it and populate the local variables!
							int lengthOfData = dataParser.getSensorDataBuffer().array().length;
							byte[] freshData = new byte[lengthOfData];			
							System.arraycopy(dataParser.getSensorDataBuffer().array(), 0, freshData, 0, lengthOfData);
							processData(freshData);
							
						}
					} catch(InterruptedException e){
						System.err.println(Thread.currentThread().getName() + ": Data task stopped.");
					}
				}
			}
			
		};

		Runnable createRunner = new Runnable(){

			@Override
			public void run() {
				
				while(!robotStopRequested){
					try {
						Thread.sleep(MIN_UPDATE_PERIOD);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				hardwareManager.requestStop();
				for(Future<?> currTask : tasks){
					currTask.cancel(true);
				}
				executor.shutdown();
				System.out.println(Thread.currentThread().getName() + ": CreateRobot stopped.");
				
			}
			
		};
		
		tasks = new Vector<Future<?>>();
		tasks.add(this.executor.submit(cmdRunner));
		tasks.add(this.executor.submit(dataRunner));
		tasks.add(this.executor.submit(createRunner));
		
	}

	public final void requestStop() {		
		robotStopRequested = true;
		System.out.println(Thread.currentThread() + " requesting stop...");
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
		int distanceInt = ((int) freshData[12]) << 8; // load high byte
		distanceInt |= ((int) freshData[13]);
		this.distance += (float) distanceInt; // accumulates the linear distance traveled		
		
		int angleInt = ((int) freshData[14]) << 8; // load high byte
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
		
		// need to handle unsigned int here!		
		// help from http://darksleep.com/player/JavaAndUnsignedTypes.html
		char voltValue = bytesToChar(freshData[17], freshData[18]);
		this.batteryVoltage = voltValue;
		
		int currentInt = ( ((int) freshData[19]) << 8 | ((int) freshData[20]) );
		this.batteryCurrent = (float) currentInt;
		
		this.batteryTemp = (short) freshData[21];
		
		char chargeValue = bytesToChar(freshData[22], freshData[23]);
		this.batteryCharge = chargeValue;
		
		char capValue = bytesToChar(freshData[24], freshData[25]);
		this.batteryCapacity = capValue;
		
		// strength of the wall and cliff sensors
		this.wallSignal = bytesToChar(freshData[26], freshData[27]);
		this.cliffLeftSignal = bytesToChar(freshData[28], freshData[29]);
		this.cliffLeftFrontSignal = bytesToChar(freshData[30], freshData[31]);
		this.cliffRightFrontSignal = bytesToChar(freshData[32], freshData[33]);
		this.cliffRightSignal = bytesToChar(freshData[34], freshData[35]);
		
		this.cargoDIN = freshData[36];
		this.cargoAIN = bytesToChar(freshData[37], freshData[38]);
		
		// skipping all other bytes up to the requested velocity, etc.
		this.reqVelocity = bytesToInt(freshData[44], freshData[45]);		
		this.reqRadius = (float) ( ((int) freshData[46]) << 8 | (int) freshData[47] );
		this.reqRightVelocity = bytesToInt(freshData[48], freshData[49]);
		this.reqLeftVelocity = bytesToInt(freshData[50], freshData[51]);
		
	}

	public CreateMode getCurrCreateMode() {
		
		return currCreateMode;
	}

	/**
	 * This method abstracts the control of the robot to use linear and rotational
	 * speeds based on a unicycle model of a robot. It uses the driveDirect() method 
	 * to send the final command to the Create. Note: you must send commands according
	 * to the units listed below.
	 * @param v - linear speed in mm/s
	 * @param omega - rotational speed in rad/s
	 */
	public final void go(float v, float omega){
		
		float rightSpeed = (2*v + omega*this.WHEELBASE)/2;

		float leftSpeed = (2*v - omega*this.WHEELBASE)/2;
		
		System.err.println("Right speed: " + (int)rightSpeed + ", Left speed: " + (int)leftSpeed);
		
		this.driveDirect(rightSpeed, leftSpeed);

	}
	
	/**
	 * This method queues a drive command for the Create robot.
	 * @param velocity - in mm/s
	 * @param radius - in mm
	 */
	public final void drive(float velocity, float radius){
				
		int vel = Math.round(velocity);
		if(vel > this.MAX_CREATE_VEL){
			vel = this.MAX_CREATE_VEL;
		}
		if(vel < -this.MAX_CREATE_VEL){
			vel = -this.MAX_CREATE_VEL;
		}
		
		int rad = Math.round(radius);
		if(rad > this.MAX_CREATE_RAD){
			rad = this.MAX_CREATE_RAD;
		}
		if(rad < -this.MAX_CREATE_RAD){
			rad = -this.MAX_CREATE_RAD;
		}
		
		ByteBuffer outBuf = ByteBuffer.allocate(5);
		outBuf.put(ActuatorCommand.DRIVE.getOpcodeVal());
		outBuf.put((byte) ((vel & 0xFFFF) >> 8 ));
		outBuf.put((byte) (vel & 0xFF));
		outBuf.put((byte) ((rad & 0xFFFF) >> 8 ));
		outBuf.put((byte) (rad & 0xFF));
		
		this.hardwareManager.sendCommand(outBuf);
		
	}
	
	/**
	 * This method implements a clockwise spin. **Does not appear to work! Not sure why.
	 */
//	public final void spinClockwise(){
//		ByteBuffer outBuf = ByteBuffer.allocate(3);
//		outBuf.put(ActuatorCommand.DRIVE.getOpcodeVal());
//		outBuf.put((byte) 0xFF);
//		outBuf.put((byte) 0xFF);
//		this.hardwareManager.sendCommand(outBuf);
//	}
	
	/**
	 * This method implements a clockwise spin. **Does not appear to work! Note sure why.
	 */
//	public final void spinCounterClockwise(){
//		ByteBuffer outBuf = ByteBuffer.allocate(3);
//		outBuf.put(ActuatorCommand.DRIVE.getOpcodeVal());
//		outBuf.put((byte) 0x00);
//		outBuf.put((byte) 0x01);
//		this.hardwareManager.sendCommand(outBuf);
//	}
	
	/**
	 * This method allows direct speed control oofver the left and right wheels.
	 * @param rightMotorSpeed
	 * @param leftMotorSpeed
	 */
	public final void driveDirect(float rightMotorSpeed, float leftMotorSpeed){
		
		int rightSpeed = Math.round(rightMotorSpeed);
		int leftSpeed = Math.round(leftMotorSpeed);
		
		if(rightSpeed > this.MAX_CREATE_VEL){
			rightSpeed = this.MAX_CREATE_VEL;
		}
		if(rightSpeed < -this.MAX_CREATE_VEL){
			rightSpeed = -this.MAX_CREATE_VEL;
		}
		
		if(leftSpeed > this.MAX_CREATE_VEL){
			leftSpeed = this.MAX_CREATE_VEL;
		}
		if(leftSpeed < -this.MAX_CREATE_VEL){
			leftSpeed = -this.MAX_CREATE_VEL;
		}
		
		ByteBuffer outBuf = ByteBuffer.allocate(5);
		outBuf.put(ActuatorCommand.DRIVE_DIRECT.getOpcodeVal());
		outBuf.put((byte) ((rightSpeed & 0xFFFF) >> 8 ));
		outBuf.put((byte) (rightSpeed & 0xFF));
		outBuf.put((byte) ((leftSpeed & 0xFFFF) >> 8 ));
		outBuf.put((byte) (leftSpeed & 0xFF));

		this.hardwareManager.sendCommand(outBuf);
	}
	
	/**
	 * This method requires three byte parameters that follow the iRobot Create Open Interface
	 * specification.
	 * @param leds
	 * @param color - 0 to 255 represents green to red
	 * @param intensity - 0 to 255 represents off to full intensity
	 */
	public final void toggleLEDs(byte leds, byte color, byte intensity){
		
		ByteBuffer outBuf = ByteBuffer.allocate(4);
		outBuf.put(ActuatorCommand.LEDS.getOpcodeVal());
		outBuf.put(leds);
		outBuf.put(color);
		outBuf.put(intensity);
		
		this.hardwareManager.sendCommand(outBuf);
		
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

	public short getBatteryTemp() {
		return batteryTemp;
	}

	public float getBatteryCapacity() {
		return batteryCapacity;
	}

	public int getWallSignal() {
		return wallSignal;
	}

	public int getCliffLeftSignal() {
		return cliffLeftSignal;
	}

	public int getCliffLeftFrontSignal() {
		return cliffLeftFrontSignal;
	}

	public int getCliffRightFrontSignal() {
		return cliffRightFrontSignal;
	}

	public int getCliffRightSignal() {
		return cliffRightSignal;
	}

	public byte getCargoDIN() {
		return cargoDIN;
	}

	public int getCargoAIN() {
		return cargoAIN;
	}

	/**
	 * This function effectively creates an unsigned integer. Promotes both input
	 * bytes to ints and then chops off the top bytes.
	 * @return
	 */
	private final char bytesToChar(byte hb, byte lb){
		
		return (char) ((0x000000FF & ((int)hb)) << 8 | (0x000000FF & ((int)lb)));
		
	}
	
	/**
	 * This functions simply combines two incoming bytes into a signed int value.
	 * @param hb
	 * @param lb
	 * @return
	 */
	private final int bytesToInt(byte hb, byte lb){
		
		return (int) ((int)hb << 8) | (int)lb;
	}
	
	public static void main(String[] args){
		
		System.out.println("Start a new CreateRobot:");
		CreateRobot robot = new CreateRobot("/dev/ttyUSB0", CreateMode.FULL);
		int execCount = 0;
		
		byte leds = 0xA;
		byte color = 0x10;
		byte intensity = 0x7F;

		
		while(execCount < 8){
			
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
			System.out.println("Battery charge " + robot.getBatteryCharge() + " mAh");
			System.out.println("Battery capacity " + robot.getBatteryCapacity() + " mAh");
			
			System.out.println("Current requested velocity " + robot.getReqVelocity());
			System.out.println("Current requested radius " + robot.getReqRadius());

			System.out.println("Current requested left wheel velocity " + robot.getReqLeftVelocity());
			System.out.println("Current requested right wheel velocity " + robot.getReqRightVelocity());

//			System.out.println("Signal strengths: ");
//			System.out.print((int)robot.getWallSignal() + " " + (int)robot.getCliffLeftSignal()
//					+ " " + (int)robot.getCliffLeftFrontSignal()
//					+ " " + (int)robot.getCliffRightFrontSignal() 
//					+ " " + (int)robot.getCliffRightSignal() +"\n");
			
//			robot.drive(400, 200);
			
//			robot.driveDirect(100, 100);
			
//			robot.toggleLEDs(leds, color, intensity);
//			color *= 2;
//			intensity += 0x10;
			
			robot.go(0, -(float)(Math.PI)/4);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			execCount++;
		}
		robot.requestStop();
		
	}

}
