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

import gnu.io.*;

public class SerialPortManager implements SerialPortEventListener {

	private final String serialPortName;
	private final int baudRate = 57600; // fixed for the Create; no need to set different speeds
	private SerialPort serialPort;
	private InputStream serialInStream;
	private OutputStream serialOutStream;
	private byte[] inputBuffer = new byte[256];
	
	private boolean initialized = false;
	
	public SerialPortManager(String portName){
		
		this.serialPortName = portName;
		CommPortIdentifier portId;
		try {
			
			portId = CommPortIdentifier.getPortIdentifier(portName);
			serialPort = (SerialPort) portId.open("JCreate", baudRate);
			serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			serialOutStream = serialPort.getOutputStream();
			serialInStream = serialPort.getInputStream();
			
			serialPort.notifyOnDataAvailable(true);
			
			initialized = true;
					
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		} catch (PortInUseException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		
		if(arg0.equals(SerialPortEvent.DATA_AVAILABLE)){
			System.out.println("Data came in!");
			int data;
			int bufferIdx = 0;
			
			try {
				
				while( (data = this.serialInStream.read()) != -1){
					this.inputBuffer[bufferIdx++] = (byte) data;
				}
				
				System.out.println(new String(this.inputBuffer, 0, bufferIdx));
				
			} catch (IOException e) {
				System.err.println("Error reading from serial port stream.");
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Transmits a ByteBuffer as an array of bytes out the serial port.
	 * @param bb
	 */
	public final void writeBuffer(ByteBuffer bb){
		if(this.initialized){
			try {
				this.serialOutStream.write(bb.array());
			} catch (IOException e) {
				System.err.println("Error writing byte array to serial port!");
				e.printStackTrace();
			}
		}
	}
	
	public final void disconnectSerial(){
		if(this.initialized){
			//TODO: how should the serial streams be cleaned up? does it matter?
//			try {
//
//				this.serialInStream.reset();
//				this.serialOutStream.flush();
//
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			this.serialPort.close();
		}
	}
	
}
