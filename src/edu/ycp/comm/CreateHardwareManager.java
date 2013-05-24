/**

Copyright (c) 2012 Patrick Martin <prof.pmartin10@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

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

	private final long updatePeriod;		// in ms
	private final long MIN_BLOCK_TIME = 30;
	
	private final BlockingQueue<ByteBuffer> returnQueue;
	private final BlockingQueue<ByteBuffer> commandQueue;
	
	private boolean initialized = false;
	
	private volatile boolean stopRequested;
	private Thread mainThread;
	
	public CreateHardwareManager(String portName, long updatePeriod, CreateMode desMode, BlockingQueue<ByteBuffer> retQueue,
			BlockingQueue<ByteBuffer> commandQueue){
		
		this.serialPortName = portName;
		
		// check to make sure the update period is not less allowable by Create robot.
		if(updatePeriod < this.MIN_BLOCK_TIME){
			this.updatePeriod = this.MIN_BLOCK_TIME;
			System.err.println("WARNING: desired update period too small. Currently set to minimum: " + this.MIN_BLOCK_TIME + "ms");
		}
		else{
			this.updatePeriod = updatePeriod;
		}
		
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
				ByteBuffer cmdBB = commandQueue.poll(MIN_BLOCK_TIME, TimeUnit.MILLISECONDS);
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
				System.out.println("Queue blocking time = " + queueTotalTime);
				if((this.updatePeriod - queueTotalTime) < 0){
					Thread.sleep(this.MIN_BLOCK_TIME);
				}
				else{
					Thread.sleep(this.updatePeriod - queueTotalTime);
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
	
	public static void main(String[] args){

		final BlockingQueue<ByteBuffer> returnQueue = new LinkedBlockingQueue<ByteBuffer>(10);
		final BlockingQueue<ByteBuffer> commandQueue = new LinkedBlockingQueue<ByteBuffer>(10);

		CreateHardwareManager chm = new CreateHardwareManager("/dev/ttyUSB0", 500, CreateMode.FULL, returnQueue, commandQueue);
		
		while(!chm.isInitialized());
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		while(true);
		
	}
}
