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
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.ycp.ModePacket;
import edu.ycp.StartPacket;
import edu.ycp.StartPacket.StartCommand;

import gnu.io.*;

public class CreateHardwareManager implements SerialPortEventListener, Runnable {

	private final String serialPortName;
	private final int baudRate = 57600; // fixed for the Create; no need to set different speeds
	private SerialPort serialPort;
	private InputStream serialInStream;
	private OutputStream serialOutStream;
//	private byte[] inputBuffer = new byte[256];

	private final int updatePeriod;		// in ms
	
	private final BlockingQueue<ByteBuffer> returnQueue;
	private final BlockingQueue<ByteBuffer> commandQueue;
	
	private boolean initialized = false;
	
	private volatile boolean stopRequested;
	private Thread mainThread;
	
	public CreateHardwareManager(String portName, int updatePeriod, BlockingQueue<ByteBuffer> retQueue,
			BlockingQueue<ByteBuffer> commandQueue){
		
		this.serialPortName = portName;
		
		this.updatePeriod = updatePeriod;
		
		this.returnQueue = retQueue;
		this.commandQueue = commandQueue;
		
		CommPortIdentifier portId;
		try {
			
			portId = CommPortIdentifier.getPortIdentifier(portName);
			serialPort = (SerialPort) portId.open("CreateHardwareManager", baudRate);
			serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			// setup the event listening system for getting serial port data
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
			serialOutStream = serialPort.getOutputStream();
			serialInStream = serialPort.getInputStream();
			
			startUp();
			
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

	private final void startUp(){
		stopRequested = false;	
		mainThread = new Thread(this);
		mainThread.setName(CreateHardwareManager.class.getSimpleName());
		mainThread.start();
	}
	
	@Override
	public void serialEvent(SerialPortEvent arg0) {
		
		switch(arg0.getEventType()){
		
		case SerialPortEvent.DATA_AVAILABLE:
			System.out.println("Data came in!");
			
			try {
				
				int sizeOfInput = serialInStream.available();
				System.out.println(sizeOfInput + " bytes ready on serial input.");
				
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
		if(this.initialized){
//			try {
				System.out.println("writing buffer");
				// this.serialOutStream.write(bb.array());
				
//			} catch (IOException e) {
//				System.err.println("Error writing byte array to serial port!");
//				e.printStackTrace();
//			}
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
	public void run() {
		
		while(!stopRequested){
			try {

				// TODO: place data command in queue
				
				handleCommand();
				
				//				ByteBuffer outBB = ByteBuffer.allocate(4);
//				outBB.put((byte) 7);
//				outBB.put((byte) 1);
//				outBB.put((byte) 2);
//				outBB.put((byte) 3);
//				returnQueue.put(outBB);
//				ByteBuffer cmdBuffer = this.commandQueue.take();
//
				
				// sleep for required update period
				Thread.sleep(this.updatePeriod);
				
				System.out.println("Woke up!");
				
			} catch (InterruptedException e) {
				System.out.println("Stopping CreateHardwareManager...");
				this.disconnectSerial();
				Thread.currentThread().interrupt();
			}
		}
		
	}

	/**
	 * Inspects command queue for new command, sends it out the serial port
	 */
	private final void handleCommand() {
		try {
			System.out.println("DEBUG: sending command.");
			writeBuffer(this.commandQueue.take());
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println(Thread.currentThread().getName() + " Error: interrupted during command transmission.");
		}
	}
	
	public final void sendCommand(ByteBuffer inCommand) {
		try {
			this.commandQueue.put(inCommand);
		} catch (InterruptedException e) {
			System.err.println(Thread.currentThread().getName() + " interrupted during sendCommand()");
			System.err.println(e.getMessage());
		}
	}
	
	public static void main(String[] args){

		final BlockingQueue<ByteBuffer> returnQueue = new LinkedBlockingQueue<ByteBuffer>(10);
		final BlockingQueue<ByteBuffer> commandQueue = new LinkedBlockingQueue<ByteBuffer>(10);

		CreateHardwareManager chm = new CreateHardwareManager("/dev/ttyUSB0", 500, returnQueue, commandQueue);
		
		while(!chm.isInitialized());
		
		chm.sendCommand(StartPacket.generateCommand(StartCommand.START));
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		chm.requestStop();
		
	}
}
