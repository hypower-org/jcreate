/*
 Copyright (c) 2013 - York College of Pennsylvania, Patrick J. Martin
 The MIT License
 See license.txt for details. 
*/

package edu.ycp.comm;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.ycp.ActuatorCommand;
import edu.ycp.CreateRobot.CreateMode;
import edu.ycp.InputPacket.InputCommand;
import edu.ycp.ModePacket;
import edu.ycp.ModePacket.ModeCommand;

import gnu.io.*;

/**
 * This class manages the serial connection to the iRobot Create robot. It runs within a thread and communicates
 * with the CreateRobot class via two BlockingQueue objects.
 * @author pjmartin
 *
 */
public class CreateHardwareManager implements SerialPortEventListener, Runnable {

	private final String serialPortName;
	private final int baudRate = 57600; // fixed for the Create; no need to set different speeds
	private SerialPort serialPort;
	private InputStream serialInStream;
	private OutputStream serialOutStream;

	// setting the update rate to always be 30 ms internally
	private final long UPDATE_PERIOD = 30;
	
	private final BlockingQueue<ByteBuffer> returnQueue;
	private final BlockingQueue<ByteBuffer> commandQueue;
	
	private boolean initialized = false;
	
	private volatile boolean stopRequested;
	private Thread mainThread;
	
	public CreateHardwareManager(String portName, CreateMode desMode, BlockingQueue<ByteBuffer> retQueue, BlockingQueue<ByteBuffer> commandQueue){
		
		this.serialPortName = portName;
		
		this.returnQueue = retQueue;
		this.commandQueue = commandQueue;
		
		CommPortIdentifier portId;
		try {
			Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
			Vector<String> commPortList = new Vector<String>();
			
			while(portEnum.hasMoreElements()){
	            CommPortIdentifier commPortId = (CommPortIdentifier) portEnum.nextElement();
	            if(commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL)
	            {
	            	commPortList.add(commPortId.getName());
	            }
	        }
	        
			if(commPortList.contains(serialPortName)){
				System.out.println("Connecting to: " + serialPortName);
			}
			
			portId = CommPortIdentifier.getPortIdentifier(serialPortName);
			
			serialPort = (SerialPort) portId.open("CreateHardwareManager", baudRate);
			serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			// setup the event listening system for getting serial port data
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
			serialOutStream = serialPort.getOutputStream();
			serialInStream = serialPort.getInputStream();
			
			// put robot into desired mode by creating mode packet
			ByteBuffer startBuf = ByteBuffer.allocate(1);
			startBuf.put(edu.ycp.StartCommand.START.getOpcodeVal());
			writeBuffer(startBuf);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(desMode == CreateMode.FULL){
				writeBuffer(ModePacket.generateCommand(ModeCommand.FULL));
			}
			else{
				writeBuffer(ModePacket.generateCommand(ModeCommand.SAFE));
			}
			
			startManager();

			initialized = true;
					
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		} catch (PortInUseException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		}
	}

	private final void startManager(){
		stopRequested = false;	
		mainThread = new Thread(this);
		mainThread.setName(CreateHardwareManager.class.getSimpleName());
		mainThread.start();
	}
	
	@Override
	public final void serialEvent(SerialPortEvent arg0) {
		
		switch(arg0.getEventType()){
		
		case SerialPortEvent.DATA_AVAILABLE:
			
			try {
				
				int sizeOfInput = serialInStream.available();
				
				// read data if it exists
				if(sizeOfInput > 0){
					ByteBuffer inByteBuf = ByteBuffer.allocate(sizeOfInput);
					serialInStream.read(inByteBuf.array());
					// place into return queue
					this.returnQueue.put(inByteBuf);
				}

			} catch (IOException e) {
				System.err.println("Error reading from serial port stream.");
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println(Thread.currentThread().getName() + " Error: interrupted during return queue put.");
			}

			break;
		default:
			System.err.println("Unhandled serial event.");
			break;
		}
		
	}
	
	/**
	 * Transmits a ByteBuffer as an array of bytes out the serial port.
	 * @param bb
	 */
	private final void writeBuffer(ByteBuffer bb) {
		try {
//			System.out.println(Thread.currentThread().getName() + " writing buffer");
			this.serialOutStream.write(bb.array());
			
		} catch (IOException e) {
			System.err.println("Error writing byte array to serial port!");
			e.printStackTrace();
		}
	}
	
	private final void disconnectSerial(){
		if(this.initialized){
			this.serialPort.close();
		}
	}
	
	public final void requestStop(){
		// send a motor stop before killing the threads
		ByteBuffer stopBuf = ByteBuffer.allocate(5);
		stopBuf.put(ActuatorCommand.DRIVE.getOpcodeVal());
		stopBuf.put((byte)0);
		stopBuf.put((byte)0);
		stopBuf.put((byte)0);
		stopBuf.put((byte)0);
		writeBuffer(stopBuf);

		stopRequested = true;
		if(mainThread != null){
			mainThread.interrupt();
		}
	}

	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public final void run() {
		
		while(!stopRequested){
			try {

				ByteBuffer dataPullBB = ByteBuffer.allocate(2);
				dataPullBB.put(InputCommand.SENSORS.getOpcodeVal());
				dataPullBB.put((byte) 6);	// request all data from Create
				writeBuffer(dataPullBB);

				// grab the most recent command to the Create from the commandQueue
				long queueBlockStart = System.currentTimeMillis();
//				System.out.println(commandQueue.size());
				ByteBuffer cmdBB = commandQueue.poll(UPDATE_PERIOD, TimeUnit.MILLISECONDS);
				long queueBlockEnd = System.currentTimeMillis();
				if(cmdBB != null){
					System.out.print(Thread.currentThread().getName() + ": Command received! \n[");
					for(byte b : cmdBB.array()){
						System.out.print(b + " ");
					}
					System.out.println("]");
					writeBuffer(cmdBB);
				}
								
				// compute sleep time
				long queueTotalTime = queueBlockEnd - queueBlockStart;
//				System.out.println("Queue blocking time = " + queueTotalTime);
				if((this.UPDATE_PERIOD - queueTotalTime) < 0){
					Thread.sleep(this.UPDATE_PERIOD);
				}
				else{
					Thread.sleep(this.UPDATE_PERIOD - queueTotalTime);
				}
								
			} catch (InterruptedException e) {
				this.disconnectSerial();
				Thread.currentThread().interrupt();
				System.out.println("CreateHardwareManager stopped.");
			}
		}
		
	}
	
	public final void sendCommand(ByteBuffer inCommand) {
		if(initialized){
			try {
				this.commandQueue.put(inCommand);
			} catch (InterruptedException e) {
				System.err.println(Thread.currentThread().getName() + " interrupted during sendCommand()");
				System.err.println(e.getMessage());
			}
		}
		else{
			System.out.println("Wait! " + CreateHardwareManager.class.getCanonicalName() + " not ready.");
		}
	}
	
}
